// ============================================================
// telegram_webhook.js
// Webhook de Telegram — reutiliza EXACTAMENTE la misma lógica de
// negocio que ya tienes en index.js (geinz_bot/*.js, historial,
// tasks.js, etc). Solo cambian: cómo entra el mensaje y cómo se
// envían las respuestas.
//
// Pégalo junto a tu index.js y agrega, al final de tu index.js:
//   const { geinz_webhook_telegram } = require("./telegram_webhook.js");
//   exports.geinz_webhook_telegram = geinz_webhook_telegram;
// ============================================================

const { onRequest } = require("firebase-functions/v2/https");
const admin = require("firebase-admin");
if (!admin.apps.length) admin.initializeApp();
const db = admin.firestore();

// ---- Reutilizamos tus mismos módulos de negocio ----
const {
  procesarBusquedaServiciosBasicos,
} = require("./servicios_basicos.js");
const {
  procesarBusquedaTienda,
  resolverInfoNegocio,
} = require("./negocio.js");
const { procesarPromociones } = require("./promociones.js");
const { procesarEmergencia } = require("./emergencia.js");
const { llamarGeminiGeinz } = require("./geinz.js");
const {
  resolverInfoTurismo,
  procesarBusquedaTurismo,
} = require("./turismo.js");
const { guardarMensajeHistorial } = require("../historial_whatsapp.js"); // sirve igual, es genérico por numero_usuario
const OpenAI = require("openai");
const openai = new OpenAI({ apiKey: process.env.API_KEYO_OPEN_IA });
const BASE_URL_PERFIL_TIENDA = "https://geinztech.com/"; // + alias_tienda
const BASE_URL_CONTACTO_TIENDA = "https://geinztech.com/"; // + ?id_tienda=...&contacto=...
const BASE_URL_PROMOCIONES = "https://geinztech.com/"; // + api/share?t=pmspls...

// ============================================================
// CONFIG TELEGRAM
// ============================================================
const TELEGRAM_TOKEN = process.env.TELEGRAM_BOT_TOKEN; // "8874023011:AAHy..."
const TG_API = `https://api.telegram.org/bot${TELEGRAM_TOKEN}`;
const TG_FILE_API = `https://api.telegram.org/file/bot${TELEGRAM_TOKEN}`;

const CONTEXTO_DEFAULT = {
  tipo: "GEINZ",
  categoria: null,
  extra: "null",
  id: null,
  nombre: null,
};

// ============================================================
// TTS — se llama por HTTP a la Cloud Function ya desplegada
// (textoAVozn8n_elevenlabs_2), igual que hace WhatsApp.
// ============================================================
const TTS_ELEVENLABS_URL =
  "https://us-central1-geinzworkapp.cloudfunctions.net/textoAVozn8n_elevenlabs_2";
const TTS_VOICE_ID_DEFAULT = "KFBj2OnpjcE1zKB9CGb8";
const PROBABILIDAD_AUDIO = 0.8;

// ============================================================
// ADAPTADORES DE ENVÍO — equivalentes a enviarMensajeWhatsapp, etc.
// ============================================================
async function enviarMensajeTelegram(chatId, texto) {
  const resp = await fetch(`${TG_API}/sendMessage`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ chat_id: chatId, text: texto }),
  });
  const json = await resp.json();
  if (!resp.ok)
    throw new Error(`Telegram sendMessage error: ${JSON.stringify(json)}`);

  guardarMensajeHistorial({
         canal : "telegram", 
    numero_usuario: `tg_${chatId}`,
    remitente: "bot",
    tipo: "texto",
    contenido: texto,
  }).catch(() => {});

  return json;
}

async function enviarImagenTelegram(chatId, imagenUrl, caption) {
  const resp = await fetch(`${TG_API}/sendPhoto`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({
      chat_id: chatId,
      photo: imagenUrl,
      caption: caption || "",
    }),
  });
  const json = await resp.json();
  if (!resp.ok)
    throw new Error(`Telegram sendPhoto error: ${JSON.stringify(json)}`);

  guardarMensajeHistorial({
         canal : "telegram", 
    numero_usuario: `tg_${chatId}`,
    remitente: "bot",
    tipo: "imagen",
    contenido: caption || "",
    extra: { imagen: imagenUrl },
  }).catch(() => {});

  return json;
}

async function enviarStickerTelegram(chatId, stickerUrl) {
  // Telegram necesita un file_id o un .webp/.tgs válido; si tu sticker
  // de WhatsApp es un link a imagen normal, mejor usar sendPhoto.
  const resp = await fetch(`${TG_API}/sendSticker`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ chat_id: chatId, sticker: stickerUrl }),
  });
  return resp.json();
}

// ---- Botones inline (equivalente simplificado de tus plantillas con botón) ----
async function enviarMensajeConBoton(chatId, texto, textoBoton, urlBoton) {
  const resp = await fetch(`${TG_API}/sendMessage`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({
      chat_id: chatId,
      text: texto,
      reply_markup: {
        inline_keyboard: [[{ text: textoBoton, url: urlBoton }]],
      },
    }),
  });
  return resp.json();
}

// ============================================================
// AUDIO — descarga + Whisper (igual que WhatsApp, pero el flujo
// de obtener la URL del archivo es distinto)
// ============================================================
async function obtenerUrlArchivoTelegram(fileId) {
  const resp = await fetch(`${TG_API}/getFile?file_id=${fileId}`);
  const data = await resp.json();
  if (!data.ok)
    throw new Error(`Telegram getFile error: ${JSON.stringify(data)}`);
  return `${TG_FILE_API}/${data.result.file_path}`;
}

async function descargarBinario(url) {
  const resp = await fetch(url);
  if (!resp.ok) throw new Error(`Error descargando archivo: ${resp.status}`);
  return Buffer.from(await resp.arrayBuffer());
}

async function transcribirAudio(bufferAudio, nombreArchivo = "audio.ogg") {
  const file = new File([bufferAudio], nombreArchivo, { type: "audio/ogg" });
  const transcription = await openai.audio.transcriptions.create({
    file,
    model: "whisper-1",
    response_format: "verbose_json",
  });
  return { texto: transcription.text || "" };
}

async function procesarAudioTelegram({ fileId, chatId }) {
  const fileUrl = await obtenerUrlArchivoTelegram(fileId);
  const buffer = await descargarBinario(fileUrl);
  const resultado = await transcribirAudio(buffer);
  return resultado.texto;
}

// ============================================================
// USUARIO — misma lógica de validarUsuario/actualizarContexto,
// pero en una colección separada para no mezclar con WhatsApp,
// y usando "tg_<chatId>" como identificador único.
// ============================================================
async function obtenerOCrearUsuarioTelegram(chatId, nombreTg) {
  const numero_usuario = `tg_${chatId}`;
  const ref = db
    .collection("Trabajadores_Usuarios_Drivers")
    .doc("usuario_bot_geinz")
    .collection("usuario_bot_geinz")
    .doc(numero_usuario);

  const snap = await ref.get();
  if (!snap.exists) {
    const nuevo = {
      nombre_user: nombreTg || "Usuario",
      numero_user: numero_usuario,
      plataforma: "telegram",
      chat_id: chatId,
      status: "activo",
      contexto: CONTEXTO_DEFAULT,
      createdAt: admin.firestore.FieldValue.serverTimestamp(),
      updated_at: admin.firestore.FieldValue.serverTimestamp(),
    };
    await ref.set(nuevo);
    return { numero_usuario, ...nuevo };
  }
  return { numero_usuario, ...snap.data() };
}

async function actualizarContextoUsuarioTelegram(
  numero_usuario,
  nuevoContexto,
) {
  const ref = db
    .collection("Trabajadores_Usuarios_Drivers")
    .doc("usuario_bot_geinz")
    .collection("usuario_bot_geinz")
    .doc(numero_usuario);
  await ref.update({
    contexto: nuevoContexto,
    updated_at: admin.firestore.FieldValue.serverTimestamp(),
  });
}

const { clasificarIntencion } = require("./clasificador.js");

// ============================================================
// WEBHOOK PRINCIPAL DE TELEGRAM
// ============================================================
exports.geinz_webhook_telegram = onRequest(
  { concurrency: 20, cpu: 1 },
  async (req, res) => {
    // Telegram NO hace handshake GET como WhatsApp — solo valida
    // que sea POST. Puedes responder 200 a cualquier GET (health check).
    if (req.method === "GET") {
      return res.status(200).send("ok");
    }

    const inicio = Date.now();
    try {
      const update = req.body;
      const mensaje = update?.message;

      if (!mensaje) {
        return res
          .status(200)
          .json({ ok: true, info: "Update sin mensaje procesable" });
      }

      const chatId = mensaje.chat.id;
      const nombreTg = mensaje.from?.first_name || "Usuario";

      // ---- Resolver texto según tipo ----
      let mensajeFinal = "";

      if (mensaje.voice) {
        // Telegram manda voz como .ogg/opus directo — no hace falta
        // "mensaje de escucha" previo como en WhatsApp, pero puedes
        // agregarlo igual si quieres esa UX.
        mensajeFinal = await procesarAudioTelegram({
          fileId: mensaje.voice.file_id,
          chatId,
        });
      } else if (mensaje.text) {
        mensajeFinal = mensaje.text;
      } else if (mensaje.sticker) {
        await enviarMensajeTelegram(
          chatId,
          "😂 Buen sticker, aún no puedo verlos pero cuéntame qué necesitas 🙌",
        );
        return res.status(200).json({ ok: true, info: "sticker" });
      } else if (mensaje.photo) {
        await enviarMensajeTelegram(
          chatId,
          "📸 Recibí tu imagen, aún no puedo analizarla, pero dime qué necesitas 😊",
        );
        return res.status(200).json({ ok: true, info: "imagen" });
      } else {
        return res.status(200).json({
          ok: true,
          info: `Tipo no soportado: ${Object.keys(mensaje)}`,
        });
      }

      if (!mensajeFinal.trim()) {
        return res.status(200).json({ ok: true, info: "Mensaje vacío" });
      }

      guardarMensajeHistorial({
             canal : "telegram", 
        numero_usuario: `tg_${chatId}`,
        nombre_usuario: nombreTg,
        remitente: "usuario",
        tipo: mensaje.voice ? "audio" : "texto",
        contenido: mensajeFinal,
        mensaje_id: mensaje.message_id,
      }).catch(() => {});

      // ---- Usuario + contexto ----
      const usuarioInfo = await obtenerOCrearUsuarioTelegram(chatId, nombreTg);
      const contextoUsuario = usuarioInfo.contexto || CONTEXTO_DEFAULT;

      // ---- Clasificar intención (mismo clasificador que WhatsApp) ----
      const { categoria } = await clasificarIntencion(
        mensajeFinal,
        contextoUsuario,
      );

      // ---- A partir de aquí: MISMA estructura de branching que
      // geinz_procesar_buffer en index.js (NEGOCIO, TURISMO,
      // PROMOCIONES, GEINZ, EMERGENCIA, etc), solo que llamando a
      // enviarMensajeTelegram / enviarImagenTelegram en vez de
      // las funciones de WhatsApp. Ejemplo con NEGOCIO: ----

      if (categoria === "NEGOCIO") {
        const resultadoTienda = await procesarBusquedaTienda({
          mensaje: mensajeFinal,
          contexto_previo: contextoUsuario,
          localidad: "barranca",
          excluir_id: contextoUsuario?.id || null,
          nombre_usuario: nombreTg,
        });

        const contextoActualizado = {
          tipo: "NEGOCIO",
          categoria: resultadoTienda.cat_detectada || null,
          id: resultadoTienda.id || null,
          nombre: resultadoTienda.nombre_negocio || null,
          extra: resultadoTienda.data || "null",
        };
        await actualizarContextoUsuarioTelegram(
          usuarioInfo.numero_usuario,
          contextoActualizado,
        );

        // 👇 Si en WhatsApp se mandaba plantilla con botones, aquí armamos
        // los mismos 2 botones para Telegram (perfil + contacto).
        if (resultadoTienda.plantilla === true) {
          const botones = [
            {
              text: resultadoTienda.alias_tienda || "Ver perfil",
              url: `${BASE_URL_PERFIL_TIENDA}${resultadoTienda.alias_tienda}`,
            },
            {
              text: "💬 Contactar",
              url: `${BASE_URL_CONTACTO_TIENDA}?id_tienda=${resultadoTienda.id}&contacto=${resultadoTienda.token_wsap}`,
            },
          ];

          try {
            if (resultadoTienda.imagen) {
              await enviarImagenConBotones(
                chatId,
                resultadoTienda.imagen,
                resultadoTienda.mensaje_safe,
                botones,
              );
            } else {
              await enviarMensajeConBotones(
                chatId,
                resultadoTienda.mensaje_safe,
                botones,
              );
            }
          } catch (e) {
            console.error(
              "❌ [NEGOCIO Telegram] Falló botones, texto de respaldo:",
              e.message,
            );
            if (resultadoTienda.mensaje_safe) {
              await enviarMensajeTelegram(chatId, resultadoTienda.mensaje_safe);
            }
          }
        } else if (resultadoTienda.imagen) {
          try {
            await enviarImagenTelegram(
              chatId,
              resultadoTienda.imagen,
              resultadoTienda.mensaje_safe,
            );
          } catch (e) {
            console.error(
              "❌ [NEGOCIO Telegram] Falló imagen, texto de respaldo:",
              e.message,
            );
            if (resultadoTienda.mensaje_safe) {
              await enviarMensajeTelegram(chatId, resultadoTienda.mensaje_safe);
            }
          }
        } else if (resultadoTienda.mensaje_safe) {
          await enviarMensajeTelegram(chatId, resultadoTienda.mensaje_safe);
        }

        return res.status(200).json({
          ok: true,
          categoria: "NEGOCIO",
          tiempo_ms: Date.now() - inicio,
        });
      }

      if (categoria === "GEINZ") {
        const { resultado: respuestaGeinz } = await llamarGeminiGeinz(
          mensajeFinal,
          nombreTg,
        );
        await actualizarContextoUsuarioTelegram(usuarioInfo.numero_usuario, {
          tipo: "GEINZ",
          extra: respuestaGeinz.extra || "null",
        });

        if (respuestaGeinz.mensaje) {
          const debeIntentarAudio =
            !!mensaje.voice &&
            !contieneNumeroOLink(respuestaGeinz.mensaje) &&
            Math.random() < PROBABILIDAD_AUDIO;

          let audioEnviado = false;
          if (debeIntentarAudio) {
            audioEnviado = await intentarResponderConAudioTelegram({
              chatId,
              texto: respuestaGeinz.mensaje,
            });
          }

          if (!audioEnviado) {
            await enviarMensajeTelegram(chatId, respuestaGeinz.mensaje);
          }
        }

        return res.status(200).json({
          ok: true,
          categoria: "GEINZ",
          tiempo_ms: Date.now() - inicio,
        });
      }
      if (categoria === "TURISMO") {
        const resultadoTurismo = await procesarBusquedaTurismo({
          mensaje: mensajeFinal,
          contexto_previo: contextoUsuario,
          localidad: "barranca",
          usuario: nombreTg,
        });

        const contextoActualizado = {
          tipo: "TURISMO",
          categoria: resultadoTurismo.categoria || null,
          id: resultadoTurismo.id || null,
          nombre: resultadoTurismo.nombre || null,
          extra: resultadoTurismo.data || "null",
        };
        await actualizarContextoUsuarioTelegram(
          usuarioInfo.numero_usuario,
          contextoActualizado,
        );

        if (resultadoTurismo.imagen) {
          try {
            await enviarImagenTelegram(
              chatId,
              resultadoTurismo.imagen,
              resultadoTurismo.mensaje_safe,
            );
          } catch (e) {
            console.error(
              "❌ [TURISMO] Falló imagen, texto de respaldo:",
              e.message,
            );
            if (resultadoTurismo.mensaje_safe)
              await enviarMensajeTelegram(
                chatId,
                resultadoTurismo.mensaje_safe,
              );
          }
        } else if (resultadoTurismo.mensaje_safe) {
          await enviarMensajeTelegram(chatId, resultadoTurismo.mensaje_safe);
        }

        return res.status(200).json({
          ok: true,
          categoria: "TURISMO",
          tiempo_ms: Date.now() - inicio,
        });
      }

      if (categoria === "SERVICIOS_BASICOS") {
        const resultadoServicio = await procesarBusquedaServiciosBasicos({
          mensaje: mensajeFinal,
          contexto_previo: contextoUsuario,
          localidad: "barranca",
          nombre_usuario: nombreTg,
        });

        const contextoActualizado = {
          tipo: "SERVICIOS_BASICOS",
          categoria: null,
          id: resultadoServicio.id || null,
          nombre: resultadoServicio.nombre_servicio || null,
          extra: resultadoServicio.data || "null",
        };
        await actualizarContextoUsuarioTelegram(
          usuarioInfo.numero_usuario,
          contextoActualizado,
        );

        if (resultadoServicio.imagen) {
          try {
            await enviarImagenTelegram(
              chatId,
              resultadoServicio.imagen,
              resultadoServicio.mensaje_safe,
            );
          } catch (e) {
            console.error(
              "❌ [SERVICIOS_BASICOS] Falló imagen, texto de respaldo:",
              e.message,
            );
            if (resultadoServicio.mensaje_safe)
              await enviarMensajeTelegram(
                chatId,
                resultadoServicio.mensaje_safe,
              );
          }
        } else if (resultadoServicio.mensaje_safe) {
          await enviarMensajeTelegram(chatId, resultadoServicio.mensaje_safe);
        }

        return res.status(200).json({
          ok: true,
          categoria: "SERVICIOS_BASICOS",
          tiempo_ms: Date.now() - inicio,
        });
      }

      if (categoria === "PROMOCIONES") {
        const resultadoPromo = await procesarPromociones({
          mensaje: mensajeFinal,
          contexto_previo: contextoUsuario,
          nombre_usuario: nombreTg,
        });

        // Caso: falta info, hay que preguntar tienda/categoría
        if (resultadoPromo.preguntar_mejor) {
          const contextoActualizado = {
            tipo: "PROMOCIONES",
            categoria: null,
            id: null,
            nombre: null,
            extra:
              "ESPERANDO_NOMBRE_PROMO: se le pidió al usuario un nombre de negocio o categoría para buscar promociones",
          };
          await actualizarContextoUsuarioTelegram(
            usuarioInfo.numero_usuario,
            contextoActualizado,
          );
          await enviarMensajeTelegram(
            chatId,
            `${nombreTg}, cuéntame qué tienda o categoría te interesa y te busco las promos 🛍️`,
          );
          return res.status(200).json({
            ok: true,
            categoria: "PROMOCIONES",
            subcaso: "preguntar_mejor",
            tiempo_ms: Date.now() - inicio,
          });
        }

        // Caso: no encontró promos para lo que pidió
        if (resultadoPromo.sin_resultados) {
          const contextoActualizado = {
            tipo: "PROMOCIONES",
            categoria: null,
            id: null,
            nombre: null,
            extra:
              "pedi al usuario otro nombre o categoria para darle las promociones",
          };
          await actualizarContextoUsuarioTelegram(
            usuarioInfo.numero_usuario,
            contextoActualizado,
          );
          await enviarMensajeTelegram(
            chatId,
            `No encontré promociones de ${resultadoPromo.referencia} 😅 ¿tienes otra tienda o categoría en mente?`,
          );
          return res.status(200).json({
            ok: true,
            categoria: "PROMOCIONES",
            subcaso: "sin_resultados",
            tiempo_ms: Date.now() - inicio,
          });
        }

        // Caso: sí hay promo(s)
        const contextoActualizado = {
          ...contextoUsuario,
          ...resultadoPromo.data,
        };
        await actualizarContextoUsuarioTelegram(
          usuarioInfo.numero_usuario,
          contextoActualizado,
        );

        const tieneImagen = !!resultadoPromo.imagen;
        const esUnaSola = (resultadoPromo.data?.ids_promos?.length || 0) < 2;

        if (tieneImagen && !esUnaSola) {
          // 2+ promos con imagen -> imagen + botón (antes era plantilla en WhatsApp)
          const ids = resultadoPromo.data?.ids_promos || [];
          const boton = [
            {
              text: "🔥 Ver promociones",
              url: `${BASE_URL_PROMOCIONES}api/share?t=pmspls&l=ba&p=${ids[0] || ""},${ids[1] || ""}`,
            },
          ];
          try {
            await enviarImagenConBotones(
              chatId,
              resultadoPromo.imagen,
              resultadoPromo.mensaje || resultadoPromo.mensaje_safe,
              boton,
            );
          } catch (e) {
            console.error(
              "❌ [PROMOCIONES Telegram] Falló botón, texto de respaldo:",
              e.message,
            );
            if (resultadoPromo.mensaje_safe) {
              await enviarMensajeTelegram(chatId, resultadoPromo.mensaje_safe);
            }
          }
        } else if (tieneImagen && esUnaSola) {
          // 1 sola promo, con imagen -> igual que antes, sin botón
          try {
            await enviarImagenTelegram(
              chatId,
              resultadoPromo.imagen,
              resultadoPromo.mensaje_safe,
            );
          } catch (e) {
            console.error("❌ [PROMOCIONES Telegram] Falló imagen:", e.message);
            if (resultadoPromo.mensaje_safe) {
              await enviarMensajeTelegram(chatId, resultadoPromo.mensaje_safe);
            }
          }
        } else if (resultadoPromo.mensaje_safe) {
          // Sin imagen (1 o varias promos) -> texto plano directo, nunca botón sin imagen
          await enviarMensajeTelegram(chatId, resultadoPromo.mensaje_safe);
        }

        return res.status(200).json({
          ok: true,
          categoria: "PROMOCIONES",
          tiempo_ms: Date.now() - inicio,
        });
      }

      if (categoria === "CONTINUIDAD_INFO") {
        const tiposValidos = ["NEGOCIO", "PROMOCIONES", "TURISMO"];
        const tieneContextoValido =
          tiposValidos.includes(contextoUsuario?.tipo) &&
          (contextoUsuario?.id || contextoUsuario?.nombre);

        // Sin contexto útil → fallback a búsqueda de negocio normal
        if (!tieneContextoValido) {
          const resultadoFallback = await procesarBusquedaTienda({
            mensaje: mensajeFinal,
            contexto_previo: contextoUsuario,
            localidad: "barranca",
            excluir_id: null,
            nombre_usuario: nombreTg,
          });

          const contextoActualizado = {
            tipo: "NEGOCIO",
            categoria: resultadoFallback.cat_detectada || null,
            id: resultadoFallback.id || null,
            nombre: resultadoFallback.nombre_negocio || null,
            extra: resultadoFallback.data || "null",
          };
          await actualizarContextoUsuarioTelegram(
            usuarioInfo.numero_usuario,
            contextoActualizado,
          );

          if (resultadoFallback.mensaje_safe) {
            await enviarMensajeTelegram(chatId, resultadoFallback.mensaje_safe);
          }

          return res.status(200).json({
            ok: true,
            categoria: "NEGOCIO",
            subcaso: "fallback_desde_continuidad_invalida",
            tiempo_ms: Date.now() - inicio,
          });
        }

        // Continuidad sobre TURISMO
        if (contextoUsuario.tipo === "TURISMO") {
          const resultadoContinuidad = await resolverInfoTurismo({
            id: contextoUsuario.id,
            nombre: contextoUsuario.nombre,
            mensaje: mensajeFinal,
            localidad: "barranca",
            nombre_usuario: nombreTg,
          });

          const contextoActualizado = {
            tipo: "TURISMO",
            categoria: contextoUsuario.categoria || null,
            id: resultadoContinuidad.id || contextoUsuario.id || null,
            nombre:
              resultadoContinuidad.nombre_lugar ||
              contextoUsuario.nombre ||
              null,
            extra: resultadoContinuidad.data || "null",
          };
          await actualizarContextoUsuarioTelegram(
            usuarioInfo.numero_usuario,
            contextoActualizado,
          );

          if (resultadoContinuidad.imagen) {
            try {
              await enviarImagenTelegram(
                chatId,
                resultadoContinuidad.imagen,
                resultadoContinuidad.mensaje_safe,
              );
            } catch (e) {
              console.error(
                "❌ [CONTINUIDAD turismo] Falló imagen, texto de respaldo:",
                e.message,
              );
              if (resultadoContinuidad.mensaje_safe)
                await enviarMensajeTelegram(
                  chatId,
                  resultadoContinuidad.mensaje_safe,
                );
            }
          } else if (resultadoContinuidad.mensaje_safe) {
            await enviarMensajeTelegram(
              chatId,
              resultadoContinuidad.mensaje_safe,
            );
          }

          return res.status(200).json({
            ok: true,
            categoria: "CONTINUIDAD_INFO",
            subcaso: "turismo",
            tiempo_ms: Date.now() - inicio,
          });
        }

        // Continuidad sobre NEGOCIO / PROMOCIONES
        const resultadoContinuidad = await resolverInfoNegocio({
          id: contextoUsuario.id,
          nombre: contextoUsuario.nombre,
          mensaje: mensajeFinal,
          localidad: "barranca",
          nombre_usuario: nombreTg,
        });

        const contextoActualizado = {
          tipo: "NEGOCIO",
          categoria:
            resultadoContinuidad.cat_detectada ||
            contextoUsuario.categoria ||
            null,
          id: resultadoContinuidad.id || contextoUsuario.id || null,
          nombre:
            resultadoContinuidad.nombre_negocio ||
            contextoUsuario.nombre ||
            null,
          extra: resultadoContinuidad.data || "null",
        };
        await actualizarContextoUsuarioTelegram(
          usuarioInfo.numero_usuario,
          contextoActualizado,
        );

        if (resultadoContinuidad.mensaje_safe) {
          await enviarMensajeTelegram(
            chatId,
            resultadoContinuidad.mensaje_safe,
          );
        }

        return res.status(200).json({
          ok: true,
          categoria: "CONTINUIDAD_INFO",
          tiempo_ms: Date.now() - inicio,
        });
      }

      if (categoria === "EMERGENCIA") {
        const contextoActualizado = { tipo: "EMERGENCIA" };
        await actualizarContextoUsuarioTelegram(
          usuarioInfo.numero_usuario,
          contextoActualizado,
        );

        await procesarEmergencia({
          localidad: "barranca",
          mensaje: mensajeFinal,
          nombreUsuario: nombreTg,
          numero_usuario: `tg_${chatId}`,
          canal: "telegram",
        });

        // 👇 Ya NO llames a construirMensajeEmergenciaConMaps ni enviarMensajeTelegram aquí,
        // procesarEmergencia ya lo hizo internamente.

        return res.status(200).json({
          ok: true,
          categoria: "EMERGENCIA",
          tiempo_ms: Date.now() - inicio,
        });
      }

      if (categoria === "PELIGRO") {
        const contextoActualizado = { tipo: "PELIGRO" };
        await actualizarContextoUsuarioTelegram(
          usuarioInfo.numero_usuario,
          contextoActualizado,
        );

        await enviarMensajeTelegram(
          chatId,
          `⚠️ ${nombreTg}, tu mensaje ha sido detectado como contenido que incumple las normas de uso de Geinz. Te pedimos mantener una comunicación respetuosa.`,
        );

        return res.status(200).json({
          ok: true,
          categoria: "PELIGRO",
          tiempo_ms: Date.now() - inicio,
        });
      }

      // Fallback de seguridad — nunca debería llegar aquí si el
      // clasificador solo devuelve categorías válidas, pero por si
      // acaso, nunca dejamos al usuario sin respuesta.
      await actualizarContextoUsuarioTelegram(usuarioInfo.numero_usuario, {
        tipo: categoria,
      });
      await enviarMensajeTelegram(
        chatId,
        `Perdón ${nombreTg}, no entendí bien eso 😅 ¿me lo puedes explicar de otra forma?`,
      );

      return res
        .status(200)
        .json({ ok: true, categoria, tiempo_ms: Date.now() - inicio });
    } catch (error) {
      console.error("❌ Error geinz_webhook_telegram:", error.message);
      return res.status(500).json({ ok: false, error: error.message });
    }
  },
);

async function enviarNotaDeVozTelegram(chatId, bufferAudio) {
  const form = new FormData();
  form.append("chat_id", chatId);
  form.append(
    "voice",
    new Blob([bufferAudio], { type: "audio/ogg" }),
    "audio.ogg",
  );

  const resp = await fetch(`${TG_API}/sendVoice`, {
    method: "POST",
    body: form,
  });
  const json = await resp.json();
  if (!resp.ok)
    throw new Error(`Telegram sendVoice error: ${JSON.stringify(json)}`);
  return json;
}

function contieneNumeroOLink(texto) {
  if (!texto) return false;
  if (/https?:\/\/[^\s]+/i.test(texto)) return true;
  if (/\bwww\.[^\s]+/i.test(texto)) return true;
  if (/(\+?51[\s-]?)?9\d{2}[\s.-]?\d{3}[\s.-]?\d{3}\b/.test(texto)) return true;
  if (/\d[\d\s.-]{6,}\d/.test(texto)) return true;
  return false;
}

async function generarAudioTTS(texto, voiceId = TTS_VOICE_ID_DEFAULT) {
  const resp = await fetch(TTS_ELEVENLABS_URL, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ text: texto, voiceId }),
  });

  if (!resp.ok) {
    throw new Error(`Error generando TTS: ${resp.status} ${await resp.text()}`);
  }

  const data = await resp.json();
  if (!data.audioContent) throw new Error("TTS no devolvió audioContent");

  return Buffer.from(data.audioContent, "base64");
}

async function enviarNotaDeVozTelegram(chatId, bufferAudio) {
  const form = new FormData();
  form.append("chat_id", chatId);
  form.append(
    "voice",
    new Blob([bufferAudio], { type: "audio/ogg" }),
    "audio.ogg",
  );

  const resp = await fetch(`${TG_API}/sendVoice`, {
    method: "POST",
    body: form,
  });
  const json = await resp.json();
  if (!resp.ok)
    throw new Error(`Telegram sendVoice error: ${JSON.stringify(json)}`);

  guardarMensajeHistorial({
         canal : "telegram", 
    numero_usuario: `tg_${chatId}`,
    remitente: "bot",
    tipo: "audio",
    contenido: "",
  }).catch(() => {});

  return json;
}

async function intentarResponderConAudioTelegram({ chatId, texto }) {
  try {
    const audioBuffer = await generarAudioTTS(texto);
    await enviarNotaDeVozTelegram(chatId, audioBuffer);
    console.log(
      "🔊 [TTS Telegram] Audio enviado correctamente | chatId:",
      chatId,
    );
    return true;
  } catch (e) {
    console.error(
      "❌ [TTS Telegram] Falló el envío de audio (se mantiene solo texto):",
      e.message,
    );
    return false;
  }
}

// ============================================================
// 👇 CONFIGURA AQUÍ tus URLs base reales (las mismas que usan tus
// plantillas de WhatsApp por detrás). Si no las tienes a la mano,
// dime el dominio y las completo yo.
// ============================================================

// ---- Botón(es) inline genérico ----
async function enviarMensajeConBotones(
  chatId,
  texto,
  botones,
  caption = false,
) {
  // botones: [{ text: "...", url: "..." }, { text: "...", url: "..." }]
  const inline_keyboard = [botones]; // todos en una sola fila; usa [[b1],[b2]] si los quieres apilados

  const body = {
    chat_id: chatId,
    text: texto,
    reply_markup: { inline_keyboard },
  };

  const resp = await fetch(`${TG_API}/sendMessage`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(body),
  });

  const json = await resp.json();
  if (!resp.ok)
    throw new Error(
      `Telegram sendMessage (botones) error: ${JSON.stringify(json)}`,
    );

  guardarMensajeHistorial({
      canal : "telegram", 
    numero_usuario: `tg_${chatId}`,
    remitente: "bot",
    tipo: "texto",
    contenido: texto,
    extra: { botones },
  }).catch(() => {});

  return json;
}

// ---- Imagen + botones (equivalente a la plantilla "entidades_data") ----
async function enviarImagenConBotones(chatId, imagenUrl, caption, botones) {
  const resp = await fetch(`${TG_API}/sendPhoto`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({
      chat_id: chatId,
      photo: imagenUrl,
      caption: caption || "",
      reply_markup: { inline_keyboard: [botones] },
    }),
  });

  const json = await resp.json();
  if (!resp.ok)
    throw new Error(
      `Telegram sendPhoto (botones) error: ${JSON.stringify(json)}`,
    );

  guardarMensajeHistorial({
         canal : "telegram", 
    numero_usuario: `tg_${chatId}`,
    remitente: "bot",
    tipo: "imagen",
    contenido: caption || "",
    extra: { imagen: imagenUrl, botones },
  }).catch(() => {});

  return json;
}
