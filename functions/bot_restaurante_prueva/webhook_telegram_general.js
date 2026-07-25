// ============================================================
// telegram_dispensador_webhook.js
// Webhook de Telegram para el DISPENSADOR (proyecto actual, no Geinz).
// Reutiliza exactamente tu clasificador (dispensador.js) y tus 7 ramas:
//   general | negocio | carta_visual | busqueda_algolia |
//   pedidos_carrito | pagos_voucher | reclamos
//
// Diferencias clave frente a WhatsApp:
//   - No hay plantillas de Meta. Todo se manda como sendMessage / sendPhoto.
//   - No hay verificación GET tipo hub.challenge; Telegram solo hace POST.
//   - El audio (voice) se transcribe con Whisper igual que en WhatsApp.
//
// Pégalo junto a tu index.js y agrega:
//   const { dispensador_webhook_telegram } = require("./telegram_dispensador_webhook.js");
//   exports.dispensador_webhook_telegram = dispensador_webhook_telegram;
// ============================================================

const { onRequest } = require("firebase-functions/v2/https");
const admin = require("firebase-admin");
const OpenAI = require("openai");

if (!admin.apps.length) {
  admin.initializeApp();
}

const db = admin.firestore();

// 👇 AJUSTAR si tu key de Whisper vive en otra env var (en tus archivos
// vi tanto OPENAI_API_KEY como API_KEYO_OPEN_IA usados en distintos sitios).
const openai = new OpenAI({
  apiKey: process.env.OPENAI_API_KEY || process.env.API_KEYO_OPEN_IA,
});

// ---- Tus 5 ramas ya implementadas ----
const { clasificarRama } = require("./dispensador.js");
const { responderGeneral } = require("./general.js");
const { responderNegocio } = require("./negocio.js");
const { responderCartaVisual } = require("./carta_visual.js");
const { responderBusquedaAlgolia } = require("./busqueda_algolia.js");
const { responderReclamos } = require("./reclamos.js");

// 👇 "pedidos_carrito" y "pagos_voucher" TODAVÍA NO están implementados.
// El dispensador (clasificarRama) sí puede devolver esas 2 ramas porque
// siguen en su catálogo (RAMAS), así que no se hace require de archivos
// que no existen — se responde con un mensaje predeterminado más abajo
// en MENSAJES_RAMA_NO_IMPLEMENTADA, sin llamar a ninguna IA.

// ============================================================
// CONFIG TELEGRAM
// ============================================================
const TELEGRAM_TOKEN = process.env.TELEGRAM_BOT_TOKEN_CHIFA_GEINZ;
const TG_API = `https://api.telegram.org/bot${TELEGRAM_TOKEN}`;
const TG_FILE_API = `https://api.telegram.org/file/bot${TELEGRAM_TOKEN}`;

const CONTEXTO_DEFAULT = { tipo: "general", extra: "null" };

// Ramas del dispensador que todavía no tienen implementación propia.
// Si clasificarRama devuelve una de estas, se responde directo con un
// mensaje fijo, sin gastar ninguna llamada a IA.
const MENSAJES_RAMA_NO_IMPLEMENTADA = {
  pedidos_carrito:
    "Por ahora todavía no puedo mostrarte el detalle de tu pedido o carrito por aquí, pero muy pronto vas a poder hacerlo 🙌 mientras tanto cuéntame si quieres ver la carta o buscar algo puntual.",
  pagos_voucher:
    "Por ahora todavía no puedo procesar pagos o vouchers por aquí, pero muy pronto vas a poder hacerlo 🙌 mientras tanto cuéntame en qué más te ayudo.",
};

// ============================================================
// ENVÍO — solo mensaje/imagen, nunca plantillas (Telegram no las usa)
// ============================================================
async function enviarMensajeTelegram(chatId, texto) {
  const resp = await fetch(`${TG_API}/sendMessage`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ chat_id: chatId, text: texto }),
  });
  const json = await resp.json();
  if (!resp.ok) {
    console.error("[telegram] Error sendMessage:", JSON.stringify(json));
  }
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
  if (!resp.ok) {
    console.error("[telegram] Error sendPhoto:", JSON.stringify(json));
  }
  return json;
}

/**
 * Normaliza las distintas formas en que cada rama puede devolver imágenes
 * (imagenes: [], imagen: "", imagen_bot: "") a un solo array.
 */
function extraerImagenes(resultado) {
  if (!resultado) return [];
  if (Array.isArray(resultado.imagenes)) return resultado.imagenes.filter(Boolean);
  if (resultado.imagen) return [resultado.imagen];
  if (resultado.imagen_bot) return [resultado.imagen_bot];
  return [];
}

/**
 * Envía la respuesta de cualquier rama: texto siempre, y si hay imágenes,
 * la primera va con el mensaje como caption y el resto sueltas.
 * Así queda uniforme para las 7 ramas sin usar plantillas.
 */
async function enviarRespuestaRama(chatId, resultado) {
  const texto = (resultado && resultado.mensaje) || null;
  const imagenes = extraerImagenes(resultado);

  if (imagenes.length === 0) {
    if (texto) await enviarMensajeTelegram(chatId, texto);
    return;
  }

  // Primera imagen con el texto como caption (así no se duplica el mensaje).
  try {
    await enviarImagenTelegram(chatId, imagenes[0], texto || "");
  } catch (e) {
    console.error("[telegram] Falló imagen, texto de respaldo:", e.message);
    if (texto) await enviarMensajeTelegram(chatId, texto);
  }

  // Imágenes adicionales, sin caption.
  for (const img of imagenes.slice(1)) {
    try {
      await enviarImagenTelegram(chatId, img, "");
    } catch (e) {
      console.error("[telegram] Falló imagen adicional:", e.message);
    }
  }
}

// ============================================================
// AUDIO (voice notes) → texto con Whisper
// ============================================================
async function obtenerUrlArchivoTelegram(fileId) {
  const resp = await fetch(`${TG_API}/getFile?file_id=${fileId}`);
  const data = await resp.json();
  if (!data.ok) {
    throw new Error(`Telegram getFile error: ${JSON.stringify(data)}`);
  }
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
  return transcription.text || "";
}

async function procesarAudioTelegram(fileId) {
  const fileUrl = await obtenerUrlArchivoTelegram(fileId);
  const buffer = await descargarBinario(fileUrl);
  return transcribirAudio(buffer);
}

// ============================================================
// MENSAJES ENLATADOS para tipos no soportados
// ============================================================
function mensajeNoSoportado(tipo) {
  const mapa = {
    sticker: "😂 Buen sticker, aún no puedo verlos, pero cuéntame qué necesitas 🙌",
    photo: "📸 Recibí tu imagen, aún no puedo analizarla, pero dime qué buscas 😊",
    video: "🎥 Gracias por el video, aún no puedo verlo, pero cuéntame de qué trata 🙌",
    document: "📄 Documento recibido, aún no puedo abrirlo, pero explícame qué necesitas 😊",
    location: "📍 Ubicación recibida, aún no puedo procesarla, pero dime qué buscas 🙌",
    contact: "👤 Contacto recibido, aún no puedo guardarlo, pero dime en qué te ayudo 😊",
  };
  return mapa[tipo] || "Gracias por tu mensaje 🙌 aún no puedo procesar ese tipo de contenido.";
}

function detectarTipoNoSoportado(mensaje) {
  if (mensaje.sticker) return "sticker";
  if (mensaje.photo) return "photo";
  if (mensaje.video) return "video";
  if (mensaje.document) return "document";
  if (mensaje.location) return "location";
  if (mensaje.contact) return "contact";
  return null;
}

// ============================================================
// USUARIO / CONTEXTO — colección propia para Telegram, separada de
// cualquier colección que ya tengas para WhatsApp.
// 👈 AJUSTAR el nombre de la colección si ya usas otra en tu proyecto.
// ============================================================
async function obtenerOCrearUsuarioTelegram(chatId, nombreTg) {
  const numero_usuario = `tg_${chatId}`;
  const ref = db.collection("usuarios_telegram_dispensador").doc(numero_usuario);
  const snap = await ref.get();

  if (!snap.exists) {
    const nuevo = {
      nombre_usuario: nombreTg || "Usuario",
      chat_id: chatId,
      contexto: CONTEXTO_DEFAULT,
      createdAt: admin.firestore.FieldValue.serverTimestamp(),
      updated_at: admin.firestore.FieldValue.serverTimestamp(),
    };
    await ref.set(nuevo);
    return { numero_usuario, ...nuevo, contexto: CONTEXTO_DEFAULT };
  }

  return { numero_usuario, ...snap.data() };
}

async function actualizarContextoUsuarioTelegram(numero_usuario, nuevoContexto) {
  const ref = db.collection("usuarios_telegram_dispensador").doc(numero_usuario);
  await ref.set(
    {
      contexto: nuevoContexto,
      updated_at: admin.firestore.FieldValue.serverTimestamp(),
    },
    { merge: true },
  );
}

// ============================================================
// DESPACHO POR RAMA — llama a la función de cada archivo con la
// misma forma de parámetros ({ mensaje, nombre_usuario, extra_anterior })
// y devuelve lo que sea que esa rama entregue ({ mensaje, extra, ... }).
// ============================================================
async function despacharRama(rama, { mensaje, nombre_usuario, extra_anterior }) {
  const params = { mensaje, nombre_usuario, extra_anterior };

  // Ramas aún no implementadas: mensaje fijo, sin tocar ninguna IA.
  if (MENSAJES_RAMA_NO_IMPLEMENTADA[rama]) {
    return {
      mensaje: MENSAJES_RAMA_NO_IMPLEMENTADA[rama],
      extra: `rama ${rama} aun no implementada`,
    };
  }

  switch (rama) {
    case "general":
      return responderGeneral(params);
    case "negocio":
      return responderNegocio(params);
    case "carta_visual":
      return responderCartaVisual(params);
    case "busqueda_algolia":
      return responderBusquedaAlgolia(params);
    case "reclamos":
      return responderReclamos(params);
    default:
      return { mensaje: "No entendí bien eso 😅 ¿me lo explicas de otra forma?", extra: "rama_desconocida" };
  }
}

// ============================================================
// WEBHOOK PRINCIPAL
// ============================================================
exports.dispensador_webhook_telegram = onRequest(
  { concurrency: 20, cpu: 1 },
  async (req, res) => {
    // Telegram no hace handshake GET como WhatsApp; solo responde ok
    // para health checks manuales.
    if (req.method === "GET") {
      return res.status(200).send("ok");
    }

    const inicio = Date.now();
    let chatIdParaError = null; // se setea apenas se conoce, para poder avisar al usuario si algo falla

    try {
      const mensaje = req.body?.message;

      if (!mensaje) {
        return res.status(200).json({ ok: true, info: "Update sin mensaje procesable" });
      }

      const chatId = mensaje.chat.id;
      chatIdParaError = chatId;
      const nombreTg = mensaje.from?.first_name || "Usuario";

      // ---- Tipos no soportados (foto, sticker, video, doc, ubicación, contacto) ----
      const tipoNoSoportado = detectarTipoNoSoportado(mensaje);
      if (tipoNoSoportado) {
        await enviarMensajeTelegram(chatId, mensajeNoSoportado(tipoNoSoportado));
        return res.status(200).json({ ok: true, tipo_mensaje: tipoNoSoportado });
      }

      // ---- Resolver texto: audio (voice) o texto plano ----
      let mensajeFinal = "";
      if (mensaje.voice) {
        mensajeFinal = await procesarAudioTelegram(mensaje.voice.file_id);
      } else if (mensaje.text) {
        mensajeFinal = mensaje.text;
      } else {
        return res.status(200).json({ ok: true, info: "Tipo de mensaje no manejado" });
      }

      if (!mensajeFinal.trim()) {
        return res.status(200).json({ ok: true, info: "Mensaje vacío tras resolución" });
      }

      // ---- Usuario + contexto previo (para extra_anterior) ----
      const usuarioInfo = await obtenerOCrearUsuarioTelegram(chatId, nombreTg);
      const contextoUsuario = usuarioInfo.contexto || CONTEXTO_DEFAULT;

      // ---- Clasificar con el MISMO dispensador que usas en WhatsApp ----
      const rama = await clasificarRama(mensajeFinal);

      // ---- Ejecutar la rama correspondiente ----
      const resultado = await despacharRama(rama, {
        mensaje: mensajeFinal,
        nombre_usuario: nombreTg,
        extra_anterior: contextoUsuario.extra,
      });

      // ---- Responder por Telegram: mensaje + imagen si aplica, sin plantillas ----
      await enviarRespuestaRama(chatId, resultado);

      // 👇 reclamos.js puede devolver humano:true cuando el caso ya necesita
      // que un asesor lo tome. Por ahora solo se deja registrado; si más
      // adelante quieres notificar a un chat de Telegram interno o a WhatsApp,
      // aquí es el punto exacto para hacerlo.
      if (rama === "reclamos" && resultado?.humano === true) {
        console.warn("⚠️ [reclamos] Caso requiere asesor humano | chatId:", chatId);
      }

      // ---- Guardar contexto para la próxima vuelta ----
      await actualizarContextoUsuarioTelegram(usuarioInfo.numero_usuario, {
        tipo: rama,
        extra: resultado?.extra || "null",
      });

      return res.status(200).json({
        ok: true,
        rama,
        mensaje_usuario: mensajeFinal,
        numero_usuario: usuarioInfo.numero_usuario,
        tiempo_ms: Date.now() - inicio,
      });
    } catch (error) {
      console.error("❌ Error dispensador_webhook_telegram:", error.message);

      // Cada rama ya tiene su propio try/catch con mensaje de respaldo, así
      // que llegar aquí significa algo no controlado por ninguna (ej. el
      // doc de negocio.js no existe en Firestore, un require roto, etc).
      // Aun así, nunca dejamos al usuario sin respuesta.
      if (chatIdParaError) {
        enviarMensajeTelegram(
          chatIdParaError,
          "Perdón, tuve un problema respondiéndote. ¿Puedes intentar de nuevo en un momento? 🙏",
        ).catch(() => {});
      }

      return res.status(500).json({ ok: false, error: error.message });
    }
  },
);