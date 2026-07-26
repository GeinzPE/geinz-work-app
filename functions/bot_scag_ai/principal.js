const { onRequest } = require("firebase-functions/v2/https");
const OpenAI = require("openai");
const admin = require("firebase-admin");

if (!admin.apps.length) {
  admin.initializeApp();
}

const db = admin.firestore();

// ============================================================
// CONFIG
// ============================================================
const WHATSAPP_TOKEN = process.env.SCAG_AI_WHATSAP_KEY; // Bearer token (empieza con EAA...)
const WHATSAPP_PHONE_NUMBER_ID = process.env.SCAG_WHATSAP_ID; // el ID numérico
const WHATSAPP_VERIFY_TOKEN_SCAG = process.env.WHATSAPP_VERIFY_TOKEN_SCAG;

const TELEGRAM_BOT_TOKEN_SCAG_AI = process.env.TELEGRAM_BOT_TOKEN_SCAG_AI;
// Base de la API de Telegram para este bot (sendMessage, getFile, answerCallbackQuery, etc.)
const TELEGRAM_API_BASE = `https://api.telegram.org/bot${TELEGRAM_BOT_TOKEN_SCAG_AI}`;
const TELEGRAM_FILE_BASE = `https://api.telegram.org/file/bot${TELEGRAM_BOT_TOKEN_SCAG_AI}`;

const WHATSAPP_API_VERSION = "v20.0";

const openai = new OpenAI({ apiKey: process.env.API_KEYO_OPEN_IA });

const PRECIO_WHISPER_USD_POR_MINUTO = 0.006;
const TIPO_CAMBIO_USD_PEN = 3.75;

const {
  screenaiQuery,
  getUserData,
  getUserData_Extencion,
  suggestConfig,
  getPreciosPlanes,
  obtener_prompt,
  obtener_prompt_vision,
  obtenerDatosUsuario,
} = require("./functions_trabajo");

const {
  guardarContextoTemporal,
  leerContextoTemporal,
} = require("./contextos/contexto_temporal");

const { clasificarMensaje } = require("./clasificador.js");
const { limpiarContextoRespuesta } = require("./limpiador_context_scag_ai");
const { responderOcio } = require("./ocio.js");
const { responderCambio } = require("./cambio.js");
const { responderInfo } = require("./info.js");
const { responderHistorial } = require("./historial.js");
const { responderScag } = require("./scag.js");
const { responderRecarga } = require("./recargas.js");
const { responderSoporte } = require("./soporte.js");
const {
  obtenerPreciosPlanes,
} = require("./extencion_reutilziable/precios_planes.js");
const { responderRegistrador } = require("./registrar_scag.js");

const {
  obtenerUrlMediaImagen,
  descargarImagenBinaria,
  analizarImagenGemini,
  parsearRespuestaVision,
  construirMensajeConfirmacionCreditos,
  enviarBotonesConfirmacionImagen,
  guardarConsultaPendiente,
} = require("./imagen/foto_creditos.js");

const {
  parsearBotonRespuesta,
  obtenerMensajeAleatorioNo,
  obtenerConsultaPendienteHttp,
  resolverConsultaImagen,
  resolverConsultaTexto,
} = require("./botones/manejar_boton.js");

const {
  sanitizeForJSON,
  guardarConsultaPendienteTexto,
  construirMensajeConfirmacionTexto,
  enviarBotonesConfirmacionTexto,
} = require("./problema.js");

const { guardarMensajeHistorial } = require("../historial_chats/historial_scag.js");

// ============================================================
// WEBHOOK PRINCIPAL (WHATSAPP)
// ============================================================
exports.geinz_webhook_principal_scag_ai = onRequest(
  {
    concurrency: 20,
    cpu: 1,
  },
  async (req, res) => {
    // ---------- Verificación del webhook (Meta) ----------
    if (req.method === "GET") {
      const VERIFY_TOKEN = process.env.WHATSAPP_VERIFY_TOKEN_SCAG;
      const mode = req.query["hub.mode"];
      const token = req.query["hub.verify_token"];
      const challenge = req.query["hub.challenge"];

      if (mode === "subscribe" && token === VERIFY_TOKEN) {
        return res.status(200).send(challenge);
      }
      return res.sendStatus(403);
    }

    const inicio = Date.now();
    console.log(
      "🔥 [geinz_webhook_principal] POST recibido:",
      JSON.stringify(req.body),
    );

    try {
      const entry = req.body?.entry?.[0];
      const value = entry?.changes?.[0]?.value;
      const mensajeWa = value?.messages?.[0];
      const contacto = value?.contacts?.[0];

      console.log("🔎 [debug] mensajeWa:", JSON.stringify(mensajeWa));
      console.log("🔎 [debug] contacto:", JSON.stringify(contacto));

      if (!mensajeWa || !contacto) {
        console.log(
          "⚠️ [debug] Evento sin mensaje procesable (mensajeWa o contacto vacío). Probablemente es un status update (sent/delivered/read), no un mensaje nuevo.",
        );
        return res
          .status(200)
          .json({ ok: true, info: "Evento sin mensaje procesable" });
      }

      const numero_usuario = contacto.wa_id;
      const id_user = value.metadata?.phone_number_id || "";

      // ================= 1) Detectar tipo (equivalente a "Switch5") =================
      const tipoNoSoportado = detectarTipoMensajeNoSoportado(mensajeWa);
      console.log(
        "🔎 [debug] tipo detectado:",
        mensajeWa.type,
        "| tipoNoSoportado:",
        tipoNoSoportado,
      );

      if (tipoNoSoportado) {
        // Los botones se detectan aparte: no se descartan, se enrutan a su
        // propio manejador (en tu n8n esa salida aún no tiene conexión).
        if (tipoNoSoportado === "boton_respuesta") {
          return await manejarBotonRespuesta({
            mensajeWa,
            numero_usuario,
            id_user,
            res,
          });
        }

        const mensajeEnlatado = construirMensajeNoSoportado(tipoNoSoportado);
        console.log(
          "📨 [debug] Voy a enviar mensaje enlatado:",
          mensajeEnlatado,
          "-> a:",
          numero_usuario,
        );

        guardarMensajeHistorial({
          canal: "whatsapp",
          numero_usuario,
          remitente: "usuario",
          tipo: tipoNoSoportado,
          contenido: "",
          mensaje_id: mensajeWa.id,
        }).catch((e) =>
          console.error("❌ [debug] Error guardando historial:", e.message),
        );

        const envio = await enviarMensajeWhatsapp(
          numero_usuario,
          mensajeEnlatado,
        );
        console.log(
          "📨 [debug] Resultado de enviarMensajeWhatsapp:",
          JSON.stringify(envio),
        );

        console.log(
          "📦 [geinz_webhook_principal] Tipo no soportado:",
          tipoNoSoportado,
          "| 👤 NUMERO:",
          numero_usuario,
        );

        return res.status(200).json({
          ok: true,
          tipo_mensaje: tipoNoSoportado,
          mensaje_enviado: mensajeEnlatado,
          numero_usuario,
        });
      }

      // ================= 2) Mapear tipo (equivalente a "validador_wsap") =================
      // text -> texto | audio -> audio | image -> foto
      const tipo = mapearTipoMensaje(mensajeWa.type);

      // ================= 3) Obtener datos del usuario (equivalente a "validador_datos") =================
      const nombrePerfil = contacto.profile?.name || "Usuario";
      const usuarioInfo = await obtenerDatosUsuario({
        numero: numero_usuario,
        nombre: nombrePerfil,
      });

      const nombre_user = usuarioInfo.nombre_user || nombrePerfil;

      // ---------- Spam ----------
      if (usuarioInfo.is_spam) {
        await enviarMensajeWhatsapp(numero_usuario, usuarioInfo.mensaje_spam);
        return res.status(200).json({
          ok: true,
          bloqueado: true,
          motivo: usuarioInfo.mensaje_spam,
        });
      }

      // ---------- Baneado ----------
      if (usuarioInfo.fecha_bloqueo && usuarioInfo.motivo_bloqueo) {
        const mensajeBan = construirMensajeBaneado(
          usuarioInfo.fecha_bloqueo,
          usuarioInfo.motivo_bloqueo,
        );
        await enviarMensajeWhatsapp(numero_usuario, mensajeBan);
        return res
          .status(200)
          .json({ ok: true, baneado: true, mensaje: mensajeBan });
      }

      // ================= 4) Enrutar (equivalente a "Switch1") =================
      const esNuevo = usuarioInfo.esNuevo === true;

      if (tipo === "foto" && esNuevo) {
        guardarMensajeHistorial({
          canal: "whatsapp",
          numero_usuario,
          nombre_usuario: nombre_user,
          remitente: "usuario",
          tipo,
          contenido: "",
          mensaje_id: mensajeWa.id,
        }).catch(() => {});

        return await rutaImagenSinRegistro({
          mensajeWa,
          numero_usuario,
          nombre_user,
          id_user,
          res,
        });
      }

      if (tipo === "foto") {
        guardarMensajeHistorial({
          canal: "whatsapp",
          numero_usuario,
          nombre_usuario: nombre_user,
          remitente: "usuario",
          tipo,
          contenido: "",
          mensaje_id: mensajeWa.id,
        }).catch(() => {});

        return await rutaFotoCredito({
          mensajeWa,
          numero_usuario,
          nombre_user,
          id_user,
          res,
        });
      }

      // ---------- Resolver mensajeFinal (texto directo o audio transcrito) ----------
      let mensajeFinal = "";

      if (tipo === "audio") {
        const resultadoAudio = await procesarAudioWhatsapp({
          mediaId: mensajeWa.audio.id,
          recipientPhoneNumber: numero_usuario,
          nombreUsuario: nombre_user,
        });
        mensajeFinal = resultadoAudio.mensajefinal;
        console.log(
          "🎧 [debug] Audio transcrito:",
          JSON.stringify(resultadoAudio.whisper),
        );
      } else if (tipo === "texto") {
        mensajeFinal = mensajeWa.text?.body || "";
      }

      guardarMensajeHistorial({
        canal: "whatsapp",
        numero_usuario,
        nombre_usuario: nombre_user,
        remitente: "usuario",
        tipo,
        contenido: mensajeFinal,
        mensaje_id: mensajeWa.id,
      }).catch(() => {});

      if (!mensajeFinal.trim()) {
        console.log("⚠️ [debug] mensajeFinal vacío tras resolver texto/audio.");
        return res
          .status(200)
          .json({ ok: true, info: "Mensaje vacío tras resolución" });
      }

      // ============ AQUÍ EL CORTE PARA USUARIOS NUEVOS (texto/audio) ============
      if (esNuevo) {
        return await rutaRegistrador({
          mensajeFinal,
          numero_usuario,
          nombre_user,
          id_user,
          tipo,
          res,
        });
      }
      // ================= 5) Clasificar (equivalente a "clasificador" + "Switch") =================
      const contextoUser =
        usuarioInfo.context_bot || usuarioInfo.data?.context_bot || "";

      const categoria = await clasificarMensaje({
        texto: mensajeFinal,
        contextoUser,
      });

      console.log(
        "🧭 [debug] Categoría asignada:",
        categoria,
        "| mensajeFinal:",
        mensajeFinal,
      );

      switch (categoria) {
        case "TRABAJO":
          return await rutaTrabajo({
            mensajeFinal,
            numero_usuario,
            nombre_user,
            id_user,
            tipo,
            res,
          });
        case "RECARGA":
          return await rutaRecarga({
            mensajeFinal,
            numero_usuario,
            nombre_user,
            id_user,
            tipo,
            res,
            usuarioInfo, // ← agregar
          });
        case "HISTORIAL":
          return await rutaHistorial({
            mensajeFinal,
            numero_usuario,
            nombre_user,
            id_user,
            tipo,
            res,
            usuarioInfo,
          });
        case "CAMBIO":
          return await rutaCambio({
            mensajeFinal,
            numero_usuario,
            nombre_user,
            id_user,
            tipo,
            res,
            usuarioInfo,
          });
        case "INFO":
          return await rutaInfo({
            mensajeFinal,
            numero_usuario,
            nombre_user,
            id_user,
            tipo,
            res,
            usuarioInfo,
          });
        case "SCAG":
          return await rutaScag({
            mensajeFinal,
            numero_usuario,
            nombre_user,
            id_user,
            tipo,
            res,
            usuarioInfo,
          });
        case "SOPORTE":
          return await rutaSoporte({
            mensajeFinal,
            numero_usuario,
            nombre_user,
            id_user,
            tipo,
            res,
            usuarioInfo, // ← agregar
          });
        case "OCIO":
        default:
          return await rutaOcio({
            mensajeFinal,
            numero_usuario,
            nombre_user,
            id_user,
            tipo,
            res,
          });
      }
    } catch (error) {
      console.error("❌ Error geinz_webhook_principal:", error.message);
      const tiempo_ms = Date.now() - inicio;
      return res
        .status(500)
        .json({ ok: false, error: error.message, tiempo_ms });
    }
  },
);

// ============================================================
// WEBHOOK PRINCIPAL (TELEGRAM)
// ------------------------------------------------------------
// Mismo bot, mismo cerebro: recibe los updates de Telegram, los
// traduce al mismo "lenguaje" interno (numero_usuario, tipo,
// mensajeFinal, canal) y llama exactamente a las mismas rutas
// (rutaTrabajo, rutaOcio, rutaFotoCredito, etc.) que usa WhatsApp.
// No se tocó ninguna regla de negocio: solo se agregó la capa de
// entrada/salida propia de Telegram.
//
// Recuerda registrar este webhook en Telegram con:
//   https://api.telegram.org/bot<TOKEN>/setWebhook?url=<URL_DE_ESTA_FUNCION>
// ============================================================
exports.geinz_webhook_telegram_scag_ai = onRequest(
  {
    concurrency: 20,
    cpu: 1,
  },
  async (req, res) => {
    if (req.method === "GET") {
      return res.status(200).send("OK - webhook Telegram activo");
    }

    const inicio = Date.now();
    console.log(
      "🔥 [geinz_webhook_telegram] POST recibido:",
      JSON.stringify(req.body),
    );

    try {
      const update = req.body || {};

      // ================= 0) Botones (equivalente a "boton_respuesta") =================
      if (update.callback_query) {
        return await manejarCallbackQueryTelegram({
          callbackQuery: update.callback_query,
          res,
        });
      }

      const mensajeTg = update.message;

      if (!mensajeTg) {
        console.log(
          "⚠️ [debug-telegram] Update sin 'message' procesable (probablemente edited_message u otro evento).",
        );
        return res
          .status(200)
          .json({ ok: true, info: "Update sin mensaje procesable" });
      }

      const chatId = String(mensajeTg.chat?.id || "");
      // Namespace propio para no chocar con los números de teléfono de WhatsApp
      const numero_usuario = `tg_${chatId}`;
      const id_user = "telegram";

      // ================= 1) Detectar tipo no soportado =================
      const tipoNoSoportado = detectarTipoMensajeNoSoportadoTelegram(mensajeTg);
      console.log(
        "🔎 [debug-telegram] tipo detectado | tipoNoSoportado:",
        tipoNoSoportado,
      );

      if (tipoNoSoportado) {
        const mensajeEnlatado = construirMensajeNoSoportado(tipoNoSoportado);

        guardarMensajeHistorial({
          canal: "telegram",
          numero_usuario,
          remitente: "usuario",
          tipo: tipoNoSoportado,
          contenido: "",
          mensaje_id: mensajeTg.message_id,
        }).catch((e) =>
          console.error(
            "❌ [debug-telegram] Error guardando historial:",
            e.message,
          ),
        );

        const envio = await enviarMensajeTelegram(chatId, mensajeEnlatado);
        console.log(
          "📨 [debug-telegram] Resultado de enviarMensajeTelegram:",
          JSON.stringify(envio),
        );

        return res.status(200).json({
          ok: true,
          tipo_mensaje: tipoNoSoportado,
          mensaje_enviado: mensajeEnlatado,
          numero_usuario,
        });
      }

      // ================= 2) Mapear tipo =================
      const tipo = mapearTipoMensajeTelegram(mensajeTg);

      // ================= 3) Obtener datos del usuario =================
      const nombrePerfil =
        mensajeTg.from?.first_name || mensajeTg.from?.username || "Usuario";
      const usuarioInfo = await obtenerDatosUsuario({
        numero: numero_usuario,
        nombre: nombrePerfil,
      });

      const nombre_user = usuarioInfo.nombre_user || nombrePerfil;

      // ---------- Spam ----------
      if (usuarioInfo.is_spam) {
        await enviarMensajeTelegram(chatId, usuarioInfo.mensaje_spam);
        return res.status(200).json({
          ok: true,
          bloqueado: true,
          motivo: usuarioInfo.mensaje_spam,
        });
      }

      // ---------- Baneado ----------
      if (usuarioInfo.fecha_bloqueo && usuarioInfo.motivo_bloqueo) {
        const mensajeBan = construirMensajeBaneado(
          usuarioInfo.fecha_bloqueo,
          usuarioInfo.motivo_bloqueo,
        );
        await enviarMensajeTelegram(chatId, mensajeBan);
        return res
          .status(200)
          .json({ ok: true, baneado: true, mensaje: mensajeBan });
      }

      // ================= 4) Enrutar =================
      const esNuevo = usuarioInfo.esNuevo === true;

      if (tipo === "foto" && esNuevo) {
        guardarMensajeHistorial({
          canal: "telegram",
          numero_usuario,
          nombre_usuario: nombre_user,
          remitente: "usuario",
          tipo,
          contenido: "",
          mensaje_id: mensajeTg.message_id,
        }).catch(() => {});

        return await rutaImagenSinRegistro({
          numero_usuario,
          nombre_user,
          id_user,
          canal: "telegram",
          res,
        });
      }

      if (tipo === "foto") {
        guardarMensajeHistorial({
          canal: "telegram",
          numero_usuario,
          nombre_usuario: nombre_user,
          remitente: "usuario",
          tipo,
          contenido: "",
          mensaje_id: mensajeTg.message_id,
        }).catch(() => {});

        // Descarga de imagen equivalente a "Download media5 + imagen_whatsap1",
        // pero usando la API de archivos de Telegram.
        const fotos = mensajeTg.photo || [];
        const mejorFoto = fotos[fotos.length - 1]; // Telegram manda varias resoluciones, la última es la más grande
        const { buffer: imageBuffer } = await descargarImagenTelegram(
          mejorFoto.file_id,
        );
        const imageBase64 = imageBuffer.toString("base64");
        const mimeType = "image/jpeg";

        return await rutaFotoCredito({
          imageBase64,
          mimeType,
          numero_usuario,
          nombre_user,
          id_user,
          canal: "telegram",
          res,
        });
      }

      // ---------- Resolver mensajeFinal (texto directo o audio transcrito) ----------
      let mensajeFinal = "";

      if (tipo === "audio") {
        const fileId = mensajeTg.voice?.file_id || mensajeTg.audio?.file_id;
        const resultadoAudio = await procesarAudioTelegram({
          fileId,
          chatId,
          nombreUsuario: nombre_user,
        });
        mensajeFinal = resultadoAudio.mensajefinal;
        console.log(
          "🎧 [debug-telegram] Audio transcrito:",
          JSON.stringify(resultadoAudio.whisper),
        );
      } else if (tipo === "texto") {
        mensajeFinal = mensajeTg.text || "";
      }

      guardarMensajeHistorial({
        canal: "telegram",
        numero_usuario,
        nombre_usuario: nombre_user,
        remitente: "usuario",
        tipo,
        contenido: mensajeFinal,
        mensaje_id: mensajeTg.message_id,
      }).catch(() => {});

      if (!mensajeFinal.trim()) {
        console.log(
          "⚠️ [debug-telegram] mensajeFinal vacío tras resolver texto/audio.",
        );
        return res
          .status(200)
          .json({ ok: true, info: "Mensaje vacío tras resolución" });
      }

      // ============ CORTE PARA USUARIOS NUEVOS (texto/audio) ============
      if (esNuevo) {
        return await rutaRegistrador({
          mensajeFinal,
          numero_usuario,
          nombre_user,
          id_user,
          tipo,
          canal: "telegram",
          res,
        });
      }

      // ================= 5) Clasificar =================
      const contextoUser =
        usuarioInfo.context_bot || usuarioInfo.data?.context_bot || "";

      const categoria = await clasificarMensaje({
        texto: mensajeFinal,
        contextoUser,
      });

      console.log(
        "🧭 [debug-telegram] Categoría asignada:",
        categoria,
        "| mensajeFinal:",
        mensajeFinal,
      );

      switch (categoria) {
        case "TRABAJO":
          return await rutaTrabajo({
            mensajeFinal,
            numero_usuario,
            nombre_user,
            id_user,
            tipo,
            canal: "telegram",
            res,
          });
        case "RECARGA":
          return await rutaRecarga({
            mensajeFinal,
            numero_usuario,
            nombre_user,
            id_user,
            tipo,
            canal: "telegram",
            res,
            usuarioInfo,
          });
        case "HISTORIAL":
          return await rutaHistorial({
            mensajeFinal,
            numero_usuario,
            nombre_user,
            id_user,
            tipo,
            canal: "telegram",
            res,
            usuarioInfo,
          });
        case "CAMBIO":
          return await rutaCambio({
            mensajeFinal,
            numero_usuario,
            nombre_user,
            id_user,
            tipo,
            canal: "telegram",
            res,
            usuarioInfo,
          });
        case "INFO":
          return await rutaInfo({
            mensajeFinal,
            numero_usuario,
            nombre_user,
            id_user,
            tipo,
            canal: "telegram",
            res,
            usuarioInfo,
          });
        case "SCAG":
          return await rutaScag({
            mensajeFinal,
            numero_usuario,
            nombre_user,
            id_user,
            tipo,
            canal: "telegram",
            res,
            usuarioInfo,
          });
        case "SOPORTE":
          return await rutaSoporte({
            mensajeFinal,
            numero_usuario,
            nombre_user,
            id_user,
            tipo,
            canal: "telegram",
            res,
            usuarioInfo,
          });
        case "OCIO":
        default:
          return await rutaOcio({
            mensajeFinal,
            numero_usuario,
            nombre_user,
            id_user,
            tipo,
            canal: "telegram",
            res,
          });
      }
    } catch (error) {
      console.error("❌ Error geinz_webhook_telegram:", error.message);
      const tiempo_ms = Date.now() - inicio;
      return res
        .status(500)
        .json({ ok: false, error: error.message, tiempo_ms });
    }
  },
);

// ============================================================
// DETECCIÓN DE TIPOS (equivalente a "Switch5" de n8n) - WHATSAPP
// ============================================================
function detectarTipoMensajeNoSoportado(mensajeWa) {
  if (!mensajeWa) return null;

  // 0. POLL WHATSAPP → llega como type "unsupported"
  if (mensajeWa.type === "unsupported") return "pool";

  // 1. CONTACTO
  if (mensajeWa.type === "contacts") return "contacto";

  // 2. LOCATION
  if (mensajeWa.location) return "location";

  // 3. STICKER
  if (mensajeWa.type === "sticker") return "sticker";

  // 4. VIDEO
  if (mensajeWa.type === "video") return "video";

  // 5. DOCUMENTO
  if (mensajeWa.document) return "documento";

  // 6. CONTIENE_URL — solo si es texto con un link adentro
  if (mensajeWa.text?.body && /https?:\/\/[^\s]+/.test(mensajeWa.text.body)) {
    return "url";
  }

  // 7. BOTÓN / RESPUESTA INTERACTIVA
  if (mensajeWa.type === "interactive") return "boton_respuesta";

  // 8. IMAGEN / AUDIO / TEXTO → null = sigue el flujo normal.
  // (igual que en tu n8n: estos 3 tipos van a "validador_wsap", NO se
  // consideran "no soportados")
  return null;
}

// ============================================================
// MAPEO DE TIPO (equivalente al nodo "validador_wsap") - WHATSAPP
// ============================================================
function mapearTipoMensaje(waType) {
  const tiposMap = {
    text: "texto",
    audio: "audio",
    image: "foto",
  };
  return tiposMap[waType] || waType;
}

// ============================================================
// DETECCIÓN DE TIPOS Y MAPEO - TELEGRAM
// (mismo criterio que la versión de WhatsApp, adaptado al shape
// de los updates de Telegram)
// ============================================================
function detectarTipoMensajeNoSoportadoTelegram(mensajeTg) {
  if (!mensajeTg) return null;

  // 0. ENCUESTA
  if (mensajeTg.poll) return "pool";

  // 1. CONTACTO
  if (mensajeTg.contact) return "contacto";

  // 2. LOCATION
  if (mensajeTg.location || mensajeTg.venue) return "location";

  // 3. STICKER
  if (mensajeTg.sticker) return "sticker";

  // 4. VIDEO
  if (mensajeTg.video || mensajeTg.video_note) return "video";

  // 5. DOCUMENTO
  if (mensajeTg.document) return "documento";

  // 6. CONTIENE_URL — solo si es texto con un link adentro
  if (mensajeTg.text && /https?:\/\/[^\s]+/.test(mensajeTg.text)) {
    return "url";
  }

  // 7. FOTO / AUDIO(VOICE) / TEXTO → null = sigue el flujo normal.
  return null;
}

function mapearTipoMensajeTelegram(mensajeTg) {
  if (mensajeTg.photo) return "foto";
  if (mensajeTg.voice || mensajeTg.audio) return "audio";
  if (mensajeTg.text) return "texto";
  return "desconocido";
}

// ============================================================
// DISPATCHER DE ENVÍO (elige WhatsApp o Telegram según el canal)
// ------------------------------------------------------------
// Por defecto canal = "whatsapp" para no romper ni un solo call
// site que ya existía antes de agregar Telegram.
// ============================================================
function extraerChatIdTelegram(numero_usuario) {
  return String(numero_usuario).replace(/^tg_/, "");
}

async function enviarMensajeCanal(canal, numero_usuario, mensaje) {
  if (canal === "telegram") {
    return await enviarMensajeTelegram(
      extraerChatIdTelegram(numero_usuario),
      mensaje,
    );
  }
  return await enviarMensajeWhatsapp(numero_usuario, mensaje);
}

// ============================================================
// RUTAS (equivalente a las salidas del "Switch1")
// Aquí metes la lógica real de cada flujo.
// ============================================================
async function rutaImagenSinRegistro({
  mensajeWa,
  numero_usuario,
  nombre_user,
  id_user,
  canal = "whatsapp",
  res,
}) {
  // TODO: usuario nuevo que manda una foto (p.ej. comprobante) sin estar
  // registrado todavía. (Funciona igual para WhatsApp y Telegram)
  console.log("🖼️ [imagen_sin_Registro] 👤:", numero_usuario, "| canal:", canal);

  return res
    .status(200)
    .json({ ok: true, ruta: "imagen_sin_Registro", numero_usuario });
}

async function rutaFotoCredito({
  mensajeWa,
  imageBase64: imageBase64Param,
  mimeType: mimeTypeParam,
  numero_usuario,
  nombre_user,
  id_user,
  canal = "whatsapp",
  res,
}) {
  console.log("📷 [FOTO_CREDITO] 👤:", numero_usuario, "| canal:", canal);

  // 1) datos/config del usuario (validador_datos)
  const usuarioInfo = await obtenerDatosUsuario({
    numero: numero_usuario,
    nombre: nombre_user,
  });
  const config = usuarioInfo.data || {};
  const creditos = config.creditos ?? 0;
  const costo = config.costo_creditos_captura ?? 0;

  // 2) verificador_precios + If -> ¿tiene saldo?
  const tieneSaldo = creditos > costo;
  if (!tieneSaldo) {
    const msgSinSaldo = `Che ${nombre_user}, no tienes créditos suficientes para procesar esta imagen 😅. Recarga en https://scag.site/ para seguir.`;
    await enviarMensajeCanal(canal, numero_usuario, msgSinSaldo);
    return res
      .status(200)
      .json({ ok: true, ruta: "FOTO_CREDITO", numero_usuario, sinSaldo: true });
  }

  // 3) Descargar imagen (Download media5 + imagen_whatsap1)
  // Si viene imageBase64 ya resuelto (p.ej. desde Telegram) lo usamos tal
  // cual; si no, seguimos el camino original de WhatsApp sin tocarlo.
  let imageBase64 = imageBase64Param;
  let mimeType = mimeTypeParam || "image/jpeg";

  if (!imageBase64) {
    const mediaId = mensajeWa.image?.id;
    const { url: mediaUrl, mimeType: mimeTypeMedia } =
      await obtenerUrlMediaImagen(mediaId);
    const imageBuffer = await descargarImagenBinaria(mediaUrl);
    imageBase64 = imageBuffer.toString("base64");
    mimeType = mimeTypeMedia || "image/jpeg";
  }

  // 4) binario_imagen1 -> payload con config del usuario
  const provider = config.provider || "gemini-flash";
  const category = config.category || "general";
  const solutionMode = config.solutionMode || "detallado";

  // 5) Analyze an image (Gemini vision)
  const rawVision = await analizarImagenGemini({ imageBase64, mimeType });

  // 6) limpiado_respuesta
  const { esAcademica, descripcion } = parsearRespuestaVision(rawVision);
  console.log("🔎 [FOTO_CREDITO] Vision ->", { esAcademica, descripcion });

  // 7) If1 -> ¿es académica?
  if (esAcademica) {
    await guardarConsultaPendiente({
      alias: usuarioInfo.alias,
      provider,
      category,
      solutionMode,
      imageBase64,
      mimeType,
    });

    const mensajeConfirmacion = construirMensajeConfirmacionCreditos({
      nombre: nombre_user,
      creditos_costo: costo,
      categoria: category,
      modelo: provider,
      solucion: solutionMode,
    });

    const envio =
      canal === "telegram"
        ? await enviarBotonesConfirmacionImagenTelegram(
            extraerChatIdTelegram(numero_usuario),
            mensajeConfirmacion,
          )
        : await enviarBotonesConfirmacionImagen(
            numero_usuario,
            mensajeConfirmacion,
          );
    console.log("📨 [FOTO_CREDITO] Botones enviados:", JSON.stringify(envio));

    return res.status(200).json({
      ok: true,
      ruta: "FOTO_CREDITO",
      numero_usuario,
      esAcademica: true,
      mensaje: mensajeConfirmacion,
    });
  }

  // 8) NO académica -> var si problemas -> flujo "ocio" (promptFotoNoAcademica)
  const resultadoIA = await responderOcio({
    nombreUsuario: nombre_user,
    tipoMensajeTexto: descripcion, // = respuesta_vision en tu n8n
    tieneImagen: true,
    esAcademico: false, // clave para que use promptFotoNoAcademica, no promptNormal
  });

  return await enviarRespuestaFinal({
    numero_usuario,
    nombre_user,
    tipo: "foto",
    resultadoIA,
    ruta: "FOTO_CREDITO_NO_ACADEMICA",
    canal,
    res,
  });
}

// ============================================================
// RUTAS POR CATEGORÍA (equivalente a las salidas del "Switch" del clasificador)
// Aquí metes la lógica real de cada una.
// ============================================================
async function rutaOcio({
  mensajeFinal,
  numero_usuario,
  nombre_user,
  id_user,
  tipo,
  canal = "whatsapp",
  res,
}) {
  console.log("🎈 [OCIO] 👤:", numero_usuario, "| msg:", mensajeFinal);

  const resultadoIA = await responderOcio({
    nombreUsuario: nombre_user,
    tipoMensajeTexto: mensajeFinal,
    tieneImagen: true, // aquí siempre viene de texto/audio (foto se corta antes, en rutaFotoCredito)
  });

  return await enviarRespuestaFinal({
    numero_usuario,
    nombre_user,
    tipo,
    resultadoIA,
    ruta: "OCIO",
    canal,
    res,
  });
}

async function rutaCambio({
  mensajeFinal,
  numero_usuario,
  nombre_user,
  id_user,
  tipo,
  canal = "whatsapp",
  res,
  usuarioInfo,
}) {
  console.log("🔧 [CAMBIO] 👤:", numero_usuario, "| msg:", mensajeFinal);

  const resultadoIA = await responderCambio({
    tipoMensaje: mensajeFinal,
    nombreUser: nombre_user,
    data: usuarioInfo.data || usuarioInfo, // config completa del usuario (ajusta si el shape real es otro)
  });

  return await enviarRespuestaFinal({
    numero_usuario,
    nombre_user,
    tipo,
    resultadoIA,
    ruta: "CAMBIO",
    canal,
    res,
  });
}

async function rutaInfo({
  mensajeFinal,
  numero_usuario,
  nombre_user,
  id_user,
  tipo,
  canal = "whatsapp",
  res,
  usuarioInfo,
}) {
  console.log("ℹ️ [INFO] 👤:", numero_usuario, "| msg:", mensajeFinal);

  const resultadoIA = await responderInfo({
    tipoMensaje: mensajeFinal,
    nombreUser: nombre_user,
    data: usuarioInfo.data || usuarioInfo,
  });

  return await enviarRespuestaFinal({
    numero_usuario,
    nombre_user,
    tipo,
    resultadoIA,
    ruta: "INFO",
    canal,
    res,
  });
}

async function rutaHistorial({
  mensajeFinal,
  numero_usuario,
  nombre_user,
  id_user,
  tipo,
  canal = "whatsapp",
  res,
  usuarioInfo,
}) {
  console.log("🗂️ [HISTORIAL] 👤:", numero_usuario, "| msg:", mensajeFinal);

  const resultadoIA = await responderHistorial({
    alias: usuarioInfo.alias,
    tipoMensaje: mensajeFinal,
    nombreUser: nombre_user,
  });

  return await enviarRespuestaFinal({
    numero_usuario,
    nombre_user,
    tipo,
    resultadoIA,
    ruta: "HISTORIAL",
    canal,
    res,
  });
}

async function rutaRecarga({
  mensajeFinal,
  numero_usuario,
  nombre_user,
  id_user,
  tipo,
  canal = "whatsapp",
  res,
}) {
  console.log("💳 [RECARGA] 👤:", numero_usuario, "| msg:", mensajeFinal);

  let planes = {};
  try {
    planes = await obtenerPreciosPlanes();
  } catch (e) {
    console.error(
      "❌ [RECARGA] Error obteniendo precios de planes:",
      e.message,
    );
  }

  const resultadoIA = await responderRecarga({
    tipoMensaje: mensajeFinal,
    nombreUser: nombre_user,
    planes,
  });

  return await enviarRespuestaFinal({
    numero_usuario,
    nombre_user,
    tipo,
    resultadoIA,
    ruta: "RECARGA",
    canal,
    res,
  });
}
async function rutaSoporte({
  mensajeFinal,
  numero_usuario,
  nombre_user,
  id_user,
  tipo,
  canal = "whatsapp",
  res,
  usuarioInfo,
}) {
  console.log("🛠️ [SOPORTE] 👤:", numero_usuario, "| msg:", mensajeFinal);

  const resultadoIA = await responderSoporte({
    tipoMensaje: mensajeFinal,
    nombreUser: nombre_user,
    data: usuarioInfo?.data || usuarioInfo,
  });

  return await enviarRespuestaFinal({
    numero_usuario,
    nombre_user,
    tipo,
    resultadoIA,
    ruta: "SOPORTE",
    canal,
    res,
  });
}

async function rutaScag({
  mensajeFinal,
  numero_usuario,
  nombre_user,
  id_user,
  tipo,
  canal = "whatsapp",
  res,
  usuarioInfo,
}) {
  console.log("🤖 [SCAG] 👤:", numero_usuario, "| msg:", mensajeFinal);

  const resultadoIA = await responderScag({
    tipoMensaje: mensajeFinal,
    nombreUser: nombre_user,
    data: usuarioInfo.data || usuarioInfo,
  });

  return await enviarRespuestaFinal({
    numero_usuario,
    nombre_user,
    tipo,
    resultadoIA,
    ruta: "SCAG",
    canal,
    res,
  });
}

async function rutaTrabajo({
  mensajeFinal,
  numero_usuario,
  nombre_user,
  id_user,
  tipo,
  canal = "whatsapp",
  res,
}) {
  console.log("📚 [TRABAJO] 👤:", numero_usuario, "| msg:", mensajeFinal);

  // 1) datos/config del usuario (validador_datos)
  const usuarioInfo = await obtenerDatosUsuario({
    numero: numero_usuario,
    nombre: nombre_user,
  });
  const config = usuarioInfo.data || {};
  const creditos = config.creditos ?? 0;
  const costo = config.costo_creditos_texto ?? 0;

  // 2) verificador_precios1 + If2 -> ¿tiene saldo?
  const tieneSaldo = creditos > costo;
  if (!tieneSaldo) {
    const msgSinSaldo = `Che ${nombre_user}, no tienes créditos suficientes para esta consulta 😅. Recarga en https://scag.site/ para seguir.`;
    await enviarMensajeCanal(canal, numero_usuario, msgSinSaldo);
    return res
      .status(200)
      .json({ ok: true, ruta: "TRABAJO", numero_usuario, sinSaldo: true });
  }

  // 3) limpiar_consulta -> sanitizar el texto
  const textoLimpio = sanitizeForJSON(mensajeFinal);

  // 4) guardar en db -> guardarConsultaPendiente (con textHint)
  const provider = config.provider || "gemini-flash";
  const category = config.category || "general";
  const solutionMode = config.solutionMode || "detallado";

  try {
    await guardarConsultaPendienteTexto({
      alias: usuarioInfo.alias,
      provider,
      category,
      solutionMode,
      textoLimpio,
    });
  } catch (e) {
    console.error(
      "❌ [TRABAJO] Error guardando consulta pendiente:",
      e.message,
    );
    const msgError =
      "Uy, tuve un problema guardando tu consulta 😔. Intenta de nuevo en un momento.";
    await enviarMensajeCanal(canal, numero_usuario, msgError);
    return res
      .status(200)
      .json({ ok: true, ruta: "TRABAJO", numero_usuario, error: e.message });
  }

  // 5) msje_predeterminados -> mensaje aleatorio de confirmación
  const mensajeConfirmacion = construirMensajeConfirmacionTexto({
    nombre: nombre_user,
    creditos_costo: costo,
    categoria: category,
    modelo: provider,
    solucion: solutionMode,
  });

  // 6) Enviar Botones Confirmacion Creditos2 -> botones Sí/No
  const envio =
    canal === "telegram"
      ? await enviarBotonesConfirmacionTextoTelegram(
          extraerChatIdTelegram(numero_usuario),
          mensajeConfirmacion,
        )
      : await enviarBotonesConfirmacionTexto(
          numero_usuario,
          mensajeConfirmacion,
        );
  console.log("📨 [TRABAJO] Botones enviados:", JSON.stringify(envio));

  guardarMensajeHistorial({
    canal,
    numero_usuario,
    nombre_usuario: nombre_user,
    remitente: "bot",
    tipo: "texto",
    contenido: mensajeConfirmacion,
    mensaje_id: null,
  }).catch(() => {});

  return res.status(200).json({
    ok: true,
    ruta: "TRABAJO",
    numero_usuario,
    mensaje: mensajeConfirmacion,
  });
}

async function rutaRegistrador({
  mensajeFinal,
  numero_usuario,
  nombre_user,
  id_user,
  tipo,
  canal = "whatsapp",
  res,
}) {
  console.log("🆕 [REGISTRADOR] 👤:", numero_usuario, "| msg:", mensajeFinal);

  // Traer contexto temporal previo (igual que "getContextoTemporal" en n8n)
  let contextoTemporal = "";
  try {
    const ctx = await leerContextoTemporal(numero_usuario.replace(/^51/, ""));
    contextoTemporal = ctx?.contexto_temporal || "";
  } catch (e) {
    console.error(
      "❌ [REGISTRADOR] Error obteniendo contexto temporal:",
      e.message,
    );
  }

  const resultado = await responderRegistrador({
    mensajeUsuario: mensajeFinal,
    nombreUsuario: nombre_user,
    contextoTemporal,
  });

  const envio = await enviarMensajeCanal(canal, numero_usuario, resultado.output);
  console.log(
    "📨 [REGISTRADOR] Resultado de enviarMensaje:",
    JSON.stringify(envio),
  );

  // Guardar contexto (equivalente al nodo HTTP "contexto_temporal" de tu n8n)
  guardarContextoTemporal(
    numero_usuario.replace(/^51/, ""),
    resultado.context,
  ).catch((e) =>
    console.error(
      "❌ [REGISTRADOR] Error guardando contexto temporal:",
      e.message,
    ),
  );

  guardarMensajeHistorial({
    canal,
    numero_usuario,
    nombre_usuario: nombre_user,
    remitente: "bot",
    tipo: "texto",
    contenido: resultado.output,
    mensaje_id: null,
  }).catch(() => {});

  return res.status(200).json({
    ok: true,
    ruta: "REGISTRADOR",
    numero_usuario,
    reply: resultado.output,
    context: resultado.context,
  });
}

// ============================================================
// Equivalente a "limpiador_context" + "codicion_audio" + "tienda_categoria_1"
// (limpia/parsea la respuesta de la IA, decide si se manda audio, y manda
// por el canal correspondiente — si es texto, la manda; si es audio, queda
// el TODO porque tu n8n tampoco muestra ahí cómo se genera/envía el audio).
// ============================================================
async function enviarRespuestaFinal({
  numero_usuario,
  nombre_user,
  tipo,
  resultadoIA,
  ruta,
  canal = "whatsapp",
  res,
}) {
  const raw = resultadoIA._raw || JSON.stringify(resultadoIA);

  const final = limpiarContextoRespuesta({
    raw,
    tipoMsjeUsuario: tipo,
  });

  console.log(`🧹 [${ruta}] limpiador_context ->`, JSON.stringify(final));

  if (final.audio) {
    console.log(
      `🔊 [${ruta}] Se sugirió audio (TTS no implementado), se envía como texto igual.`,
    );
  }

  const envio = await enviarMensajeCanal(canal, numero_usuario, final.reply);
  console.log(
    `📨 [${ruta}] Resultado de enviarMensaje:`,
    JSON.stringify(envio),
  );

  guardarMensajeHistorial({
    canal,
    numero_usuario,
    nombre_usuario: nombre_user,
    remitente: "bot",
    tipo: "texto",
    contenido: final.reply,
    mensaje_id: null,
  }).catch(() => {});

  return res.status(200).json({
    ok: true,
    ruta,
    numero_usuario,
    reply: final.reply,
    context: final.context,
    audio: final.audio,
    parseOk: final.parseOk,
  });
}

// ============================================================
// PROCESAMIENTO DE AUDIO (tal cual como me lo pasaste) - WHATSAPP
// ============================================================
async function transcribirAudio(bufferAudio, nombreArchivo = "audio.ogg") {
  const file = new File([bufferAudio], nombreArchivo, { type: "audio/ogg" });
  const transcription = await openai.audio.transcriptions.create({
    file,
    model: "whisper-1",
    response_format: "verbose_json",
  });

  const duracionSegundos = transcription.duration || 0;
  const duracionMinutos = duracionSegundos / 60;
  const costoUsd = duracionMinutos * PRECIO_WHISPER_USD_POR_MINUTO;
  const costoSoles = costoUsd * TIPO_CAMBIO_USD_PEN;

  return {
    texto: transcription.text || "",
    duracion_segundos: Number(duracionSegundos.toFixed(2)),
    costo_usd: Number(costoUsd.toFixed(6)),
    costo_soles: Number(costoSoles.toFixed(6)),
  };
}

async function procesarAudioWhatsapp({
  mediaId,
  recipientPhoneNumber,
  nombreUsuario,
}) {
  const mensajeEscucha = construirMensajeEscucha(nombreUsuario);
  const [, mediaUrl] = await Promise.all([
    enviarMensajeWhatsapp(recipientPhoneNumber, mensajeEscucha),
    obtenerUrlMedia(mediaId),
  ]);
  const audioBuffer = await descargarAudioBinario(mediaUrl);
  const resultado = await transcribirAudio(audioBuffer);
  return { mensajefinal: resultado.texto, whisper: resultado };
}

// ---------- Helpers que necesita procesarAudioWhatsapp ----------
// (no me los pasaste, así que los dejo implementados sobre la Graph API de
// Meta, que es el flujo estándar para bajar media de WhatsApp. Ajusta si ya
// tenías otra implementación en functions_trabajo.)

function construirMensajeEscucha(nombreUsuario) {
  return `Dame un segundo ${nombreUsuario ? nombreUsuario : ""}, estoy escuchando tu audio 🎧`.trim();
}

async function obtenerUrlMedia(mediaId) {
  const url = `https://graph.facebook.com/${WHATSAPP_API_VERSION}/${mediaId}`;

  const respuesta = await fetch(url, {
    headers: { Authorization: `Bearer ${WHATSAPP_TOKEN}` },
  });

  if (!respuesta.ok) {
    const errorBody = await respuesta.text();
    console.error(
      "❌ [obtenerUrlMedia] Error obteniendo URL del media:",
      respuesta.status,
      errorBody,
    );
    throw new Error(`No se pudo obtener la URL del media ${mediaId}`);
  }

  const data = await respuesta.json();
  return data.url;
}

async function descargarAudioBinario(mediaUrl) {
  const respuesta = await fetch(mediaUrl, {
    headers: { Authorization: `Bearer ${WHATSAPP_TOKEN}` },
  });

  if (!respuesta.ok) {
    console.error(
      "❌ [descargarAudioBinario] Error descargando el audio:",
      respuesta.status,
    );
    throw new Error("No se pudo descargar el audio");
  }

  const arrayBuffer = await respuesta.arrayBuffer();
  return Buffer.from(arrayBuffer);
}

// ============================================================
// PROCESAMIENTO DE AUDIO / IMAGEN - TELEGRAM
// (misma idea que los helpers de WhatsApp, pero usando la Bot API
// de Telegram: getFile -> URL de descarga -> Buffer)
// ============================================================
async function obtenerArchivoTelegram(fileId) {
  const url = `${TELEGRAM_API_BASE}/getFile?file_id=${fileId}`;

  const respuesta = await fetch(url);

  if (!respuesta.ok) {
    const errorBody = await respuesta.text();
    console.error(
      "❌ [obtenerArchivoTelegram] Error obteniendo el archivo:",
      respuesta.status,
      errorBody,
    );
    throw new Error(`No se pudo obtener el archivo de Telegram ${fileId}`);
  }

  const data = await respuesta.json();
  const filePath = data.result?.file_path;
  const fileUrl = `${TELEGRAM_FILE_BASE}/${filePath}`;
  return { filePath, fileUrl };
}

async function descargarBinarioTelegram(fileUrl) {
  const respuesta = await fetch(fileUrl);

  if (!respuesta.ok) {
    console.error(
      "❌ [descargarBinarioTelegram] Error descargando el archivo:",
      respuesta.status,
    );
    throw new Error("No se pudo descargar el archivo de Telegram");
  }

  const arrayBuffer = await respuesta.arrayBuffer();
  return Buffer.from(arrayBuffer);
}

async function descargarImagenTelegram(fileId) {
  const { fileUrl, filePath } = await obtenerArchivoTelegram(fileId);
  const buffer = await descargarBinarioTelegram(fileUrl);
  return { buffer, filePath };
}

async function procesarAudioTelegram({ fileId, chatId, nombreUsuario }) {
  const mensajeEscucha = construirMensajeEscucha(nombreUsuario);
  const [, archivo] = await Promise.all([
    enviarMensajeTelegram(chatId, mensajeEscucha),
    obtenerArchivoTelegram(fileId),
  ]);
  const audioBuffer = await descargarBinarioTelegram(archivo.fileUrl);
  const resultado = await transcribirAudio(audioBuffer, "audio.ogg");
  return { mensajefinal: resultado.texto, whisper: resultado };
}

// ============================================================
// BOTONES / RESPUESTAS INTERACTIVAS - WHATSAPP
// ============================================================
async function manejarBotonRespuesta({
  mensajeWa,
  numero_usuario,
  id_user,
  canal = "whatsapp",
  res,
}) {
  const { button_id, aceptado, origen } = parsearBotonRespuesta(mensajeWa);

  console.log(
    "🔘 [BOTON_RESPUESTA] 👤:",
    numero_usuario,
    "| id:",
    button_id,
    "| aceptado:",
    aceptado,
    "| origen:",
    origen,
  );

  return await procesarDecisionBoton({
    aceptado,
    origen,
    numero_usuario,
    canal,
    res,
  });
}

// ============================================================
// BOTONES / CALLBACK QUERIES - TELEGRAM
// ============================================================
async function manejarCallbackQueryTelegram({ callbackQuery, res }) {
  const chatId = String(callbackQuery.message?.chat?.id || "");
  const numero_usuario = `tg_${chatId}`;
  const data = callbackQuery.data || "";

  // Formato esperado (definido por enviarBotonesConfirmacionTelegram):
  // "si_imagen" | "no_imagen" | "si_texto" | "no_texto"
  const aceptado = data.startsWith("si_");
  const origen = data.endsWith("_imagen") ? "imagen" : "texto";

  console.log(
    "🔘 [BOTON_RESPUESTA-telegram] 👤:",
    numero_usuario,
    "| data:",
    data,
    "| aceptado:",
    aceptado,
    "| origen:",
    origen,
  );

  // Le avisamos a Telegram que ya procesamos el tap (quita el "cargando" del botón)
  responderCallbackQueryTelegram(callbackQuery.id).catch(() => {});

  return await procesarDecisionBoton({
    aceptado,
    origen,
    numero_usuario,
    canal: "telegram",
    res,
  });
}

// ============================================================
// LÓGICA COMPARTIDA DE DECISIÓN DE BOTÓN (Sí/No)
// Idéntica a la que tenías para WhatsApp, ahora reutilizada por
// ambos canales.
// ============================================================
async function procesarDecisionBoton({
  aceptado,
  origen,
  numero_usuario,
  canal = "whatsapp",
  res,
}) {
  // ---------- If Aceptado == false -> Mensaje Aleatorio No ----------
  if (!aceptado) {
    const mensajeNo = obtenerMensajeAleatorioNo();
    const envio = await enviarMensajeCanal(canal, numero_usuario, mensajeNo);
    console.log(
      "📨 [BOTON_RESPUESTA] (No aceptado) enviado:",
      JSON.stringify(envio),
    );

    guardarMensajeHistorial({
      canal,
      numero_usuario,
      remitente: "bot",
      tipo: "texto",
      contenido: mensajeNo,
      mensaje_id: null,
    }).catch(() => {});

    return res.status(200).json({
      ok: true,
      ruta: "BOTON_RESPUESTA",
      numero_usuario,
      aceptado: false,
      mensaje: mensajeNo,
    });
  }

  // ---------- Aceptado == true -> buscar consulta pendiente ----------
  const aliasParaConsulta = numero_usuario.replace(/^51/, "");

  let consultaPendiente;
  try {
    consultaPendiente = await obtenerConsultaPendienteHttp(aliasParaConsulta);
  } catch (e) {
    console.error(
      "❌ [BOTON_RESPUESTA] Error obteniendo consulta pendiente:",
      e.message,
    );
    const msgError =
      "Uy, no encontré tu consulta pendiente 😅. Mándamela de nuevo porfa.";
    await enviarMensajeCanal(canal, numero_usuario, msgError);
    return res.status(200).json({
      ok: true,
      ruta: "BOTON_RESPUESTA",
      numero_usuario,
      error: e.message,
    });
  }

  // ---------- If Origen Imagen (aceptado) ----------
  let resultadoIA;
  try {
    resultadoIA =
      origen === "imagen"
        ? await resolverConsultaImagen(consultaPendiente)
        : await resolverConsultaTexto(consultaPendiente);
  } catch (e) {
    console.error(
      "❌ [BOTON_RESPUESTA] Error resolviendo consulta:",
      e.message,
    );
    const msgError =
      "Tuve un problema resolviendo tu consulta 😔. Intenta de nuevo en un momento.";
    await enviarMensajeCanal(canal, numero_usuario, msgError);
    return res.status(200).json({
      ok: true,
      ruta: "BOTON_RESPUESTA",
      numero_usuario,
      error: e.message,
    });
  }

  // ---------- RESPUESTA_FINAL ----------
  const reply = resultadoIA.answer || "No pude generar una respuesta 😅";
  const context = "resolviste una pregunta del usuario";

  const envio = await enviarMensajeCanal(canal, numero_usuario, reply);
  console.log(
    "📨 [BOTON_RESPUESTA] Resultado de enviarMensaje:",
    JSON.stringify(envio),
  );

  // ---------- If5: solo guarda contexto si viene no vacío ----------
  if (context && context.trim()) {
    guardarContextoTemporal(aliasParaConsulta, context).catch((e) =>
      console.error(
        "❌ [BOTON_RESPUESTA] Error guardando contexto temporal:",
        e.message,
      ),
    );
  }

  guardarMensajeHistorial({
    canal,
    numero_usuario,
    remitente: "bot",
    tipo: "texto",
    contenido: reply,
    mensaje_id: null,
  }).catch(() => {});

  return res.status(200).json({
    ok: true,
    ruta: "BOTON_RESPUESTA",
    numero_usuario,
    origen,
    aceptado: true,
    reply,
    context,
    charged: resultadoIA.charged,
  });
}

// ============================================================
// FUNCIONES DE APOYO (mensajes, historial, envío)
// Ajusta la implementación si ya las tienes en otro archivo.
// ============================================================
function construirMensajeNoSoportado(tipo) {
  const mensajes = {
    pool: "Por ahora no puedo leer encuestas 🙈. ¿Me lo cuentas en texto o audio?",
    contacto:
      "No puedo procesar contactos compartidos. Cuéntamelo en texto o audio 🙂",
    location: "No puedo procesar ubicaciones. Cuéntamelo en texto o audio 🙂",
    sticker: "Los stickers no los proceso 😅. Mándame texto o audio.",
    video: "Por ahora no proceso videos. Mándame texto, audio o foto 🙂",
    documento:
      "Por ahora no proceso documentos. Mándame texto, audio o foto 🙂",
    url: "No puedo abrir links. Cuéntame en tus palabras qué necesitas 🙂",
  };
  return mensajes[tipo] || "Ese tipo de mensaje no lo puedo procesar todavía.";
}

function construirMensajeBaneado(fecha_bloqueo, motivo_bloqueo) {
  return `Tu cuenta está bloqueada desde ${fecha_bloqueo} por: ${motivo_bloqueo}.`;
}



async function enviarMensajeWhatsapp(numero_usuario, mensaje) {
  const url = `https://graph.facebook.com/${WHATSAPP_API_VERSION}/${WHATSAPP_PHONE_NUMBER_ID}/messages`;

  console.log("📤 [enviarMensajeWhatsapp] URL:", url);
  console.log(
    "📤 [enviarMensajeWhatsapp] TOKEN presente:",
    !!WHATSAPP_TOKEN,
    "| PHONE_NUMBER_ID presente:",
    !!WHATSAPP_PHONE_NUMBER_ID,
  );

  if (!WHATSAPP_TOKEN || !WHATSAPP_PHONE_NUMBER_ID) {
    console.error(
      "❌ [enviarMensajeWhatsapp] Faltan variables de entorno SCAG_AI_WHATSAP_KEY o SCAG_WHATSAP_ID. No se puede enviar nada.",
    );
    return { ok: false, error: "faltan_credenciales" };
  }

  const body = {
    messaging_product: "whatsapp",
    to: numero_usuario,
    type: "text",
    text: { body: mensaje },
  };

  try {
    const respuesta = await fetch(url, {
      method: "POST",
      headers: {
        Authorization: `Bearer ${WHATSAPP_TOKEN}`,
        "Content-Type": "application/json",
      },
      body: JSON.stringify(body),
    });

    const data = await respuesta.json();

    if (!respuesta.ok) {
      console.error(
        "❌ [enviarMensajeWhatsapp] Meta respondió con error:",
        respuesta.status,
        JSON.stringify(data),
      );
      return { ok: false, status: respuesta.status, error: data };
    }

    console.log(
      "✅ [enviarMensajeWhatsapp] Mensaje enviado OK:",
      JSON.stringify(data),
    );
    return { ok: true, data };
  } catch (error) {
    console.error(
      "❌ [enviarMensajeWhatsapp] Excepción al llamar a Graph API:",
      error.message,
    );
    return { ok: false, error: error.message };
  }
}

// ============================================================
// FUNCIONES DE APOYO - TELEGRAM (envío de mensajes / botones)
// ============================================================
async function enviarMensajeTelegram(chatId, mensaje) {
  const url = `${TELEGRAM_API_BASE}/sendMessage`;

  console.log("📤 [enviarMensajeTelegram] chatId:", chatId);
  console.log(
    "📤 [enviarMensajeTelegram] TOKEN presente:",
    !!TELEGRAM_BOT_TOKEN_SCAG_AI,
  );

  if (!TELEGRAM_BOT_TOKEN_SCAG_AI) {
    console.error(
      "❌ [enviarMensajeTelegram] Falta variable de entorno TELEGRAM_BOT_TOKEN_SCAG_AI. No se puede enviar nada.",
    );
    return { ok: false, error: "faltan_credenciales" };
  }

  const body = {
    chat_id: chatId,
    text: mensaje,
  };

  try {
    const respuesta = await fetch(url, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(body),
    });

    const data = await respuesta.json();

    if (!respuesta.ok || data.ok === false) {
      console.error(
        "❌ [enviarMensajeTelegram] Telegram respondió con error:",
        respuesta.status,
        JSON.stringify(data),
      );
      return { ok: false, status: respuesta.status, error: data };
    }

    console.log(
      "✅ [enviarMensajeTelegram] Mensaje enviado OK:",
      JSON.stringify(data),
    );
    return { ok: true, data };
  } catch (error) {
    console.error(
      "❌ [enviarMensajeTelegram] Excepción al llamar a la Bot API:",
      error.message,
    );
    return { ok: false, error: error.message };
  }
}

// Botones inline Sí/No, equivalentes a "enviarBotonesConfirmacionImagen" /
// "enviarBotonesConfirmacionTexto" de WhatsApp, pero con el formato de
// teclado inline de Telegram. El callback_data queda "si_<origen>" /
// "no_<origen>" para que manejarCallbackQueryTelegram lo pueda parsear.
async function enviarBotonesConfirmacionTelegram(chatId, mensaje, origen) {
  const url = `${TELEGRAM_API_BASE}/sendMessage`;

  const body = {
    chat_id: chatId,
    text: mensaje,
    reply_markup: {
      inline_keyboard: [
        [
          { text: "✅ Sí", callback_data: `si_${origen}` },
          { text: "❌ No", callback_data: `no_${origen}` },
        ],
      ],
    },
  };

  try {
    const respuesta = await fetch(url, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(body),
    });

    const data = await respuesta.json();

    if (!respuesta.ok || data.ok === false) {
      console.error(
        "❌ [enviarBotonesConfirmacionTelegram] Error:",
        respuesta.status,
        JSON.stringify(data),
      );
      return { ok: false, status: respuesta.status, error: data };
    }

    return { ok: true, data };
  } catch (error) {
    console.error(
      "❌ [enviarBotonesConfirmacionTelegram] Excepción:",
      error.message,
    );
    return { ok: false, error: error.message };
  }
}

async function enviarBotonesConfirmacionImagenTelegram(chatId, mensaje) {
  return enviarBotonesConfirmacionTelegram(chatId, mensaje, "imagen");
}

async function enviarBotonesConfirmacionTextoTelegram(chatId, mensaje) {
  return enviarBotonesConfirmacionTelegram(chatId, mensaje, "texto");
}

async function responderCallbackQueryTelegram(callbackQueryId, texto = "") {
  const url = `${TELEGRAM_API_BASE}/answerCallbackQuery`;
  try {
    await fetch(url, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ callback_query_id: callbackQueryId, text: texto }),
    });
  } catch (error) {
    console.error(
      "❌ [responderCallbackQueryTelegram] Excepción:",
      error.message,
    );
  }
}