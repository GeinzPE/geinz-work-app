const { onRequest } = require("firebase-functions/v2/https");
const OpenAI = require("openai");

// ============================================================
// CONFIG
// ============================================================
const WHATSAPP_TOKEN = process.env.ID_API_WHATSAPP; // Bearer token (empieza con EAA...)
const WHATSAPP_PHONE_NUMBER_ID = process.env.ID_NUMBER_WHATSAPP; // el ID numérico
const WHATSAPP_API_VERSION = "v20.0";

const openai = new OpenAI({ apiKey: process.env.API_KEYO_OPEN_IA });
// Precio oficial de OpenAI Whisper: $0.006 USD por minuto de audio
const PRECIO_WHISPER_USD_POR_MINUTO = 0.006;

// Tipo de cambio USD -> PEN (soles). AJUSTAR periódicamente a mano,
// o reemplazar por una consulta a una API de cambio si se quiere en vivo.
const TIPO_CAMBIO_USD_PEN = 3.75;

// ============================================================
// UTILIDADES
// ============================================================
function pick(arr) {
  return arr[Math.floor(Math.random() * arr.length)];
}

// ---- 1) Mensaje predeterminado "ya te escuché..." ----
function construirMensajeEscucha(nombreUsuario) {
  const n = nombreUsuario || "amigo";
  const opciones = [
    `${n} ya te escuché 👂 dame un momento...`,
    `captado ${n} 🎙️ ya voy...`,
    `oki ${n}, en un seg te respondo 👌`,
    `ya te escuché ${n}, espérame un momento ⏳`,
    `recibido ${n} 📨 ya estoy en eso...`,
    `sí sí ${n}, ya escuché todo 😄 un momento...`,
    `ok ${n} ya voy 🏃`,
    `escuchado ${n}, dame un seg 🙌`,
  ];
  return pick(opciones);
}

// ---- 2) Enviar mensaje de texto por WhatsApp ----
async function enviarMensajeWhatsapp(recipientPhoneNumber, textBody) {
  const url = `https://graph.facebook.com/${WHATSAPP_API_VERSION}/${WHATSAPP_PHONE_NUMBER_ID}/messages`;

  const resp = await fetch(url, {
    method: "POST",
    headers: {
      Authorization: `Bearer ${WHATSAPP_TOKEN}`,
      "Content-Type": "application/json",
    },
    body: JSON.stringify({
      messaging_product: "whatsapp",
      to: recipientPhoneNumber,
      type: "text",
      text: { body: textBody },
    }),
  });

  if (!resp.ok) {
    const errText = await resp.text();
    throw new Error(`Error enviando mensaje WhatsApp: ${resp.status} ${errText}`);
  }

  return resp.json();
}

// ---- 3) Obtener la URL de descarga del media (audio) ----
async function obtenerUrlMedia(mediaId) {
  const url = `https://graph.facebook.com/${WHATSAPP_API_VERSION}/${mediaId}`;

  const resp = await fetch(url, {
    method: "GET",
    headers: { Authorization: `Bearer ${WHATSAPP_TOKEN}` },
  });

  if (!resp.ok) {
    const errText = await resp.text();
    throw new Error(`Error obteniendo URL de media: ${resp.status} ${errText}`);
  }

  const data = await resp.json();
  return data.url;
}

// ---- 4) Descargar el binario del audio ----
async function descargarAudioBinario(mediaUrl) {
  const resp = await fetch(mediaUrl, {
    method: "GET",
    headers: { Authorization: `Bearer ${WHATSAPP_TOKEN}` },
  });

  if (!resp.ok) {
    const errText = await resp.text();
    throw new Error(`Error descargando audio: ${resp.status} ${errText}`);
  }

  const arrayBuffer = await resp.arrayBuffer();
  return Buffer.from(arrayBuffer);
}

// ---- 5) Transcribir el audio con OpenAI Whisper ----
//    Pide "verbose_json" para obtener la duración real del audio en segundos,
//    necesaria para calcular el costo (Whisper cobra por minuto, no por tokens).
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

// ============================================================
// ORQUESTADOR
// ============================================================
async function procesarAudioWhatsapp({ mediaId, recipientPhoneNumber, nombreUsuario }) {
  const mensajeEscucha = construirMensajeEscucha(nombreUsuario);

  const [, mediaUrl] = await Promise.all([
    enviarMensajeWhatsapp(recipientPhoneNumber, mensajeEscucha),
    obtenerUrlMedia(mediaId),
  ]);

  const audioBuffer = await descargarAudioBinario(mediaUrl);
  const resultadoTranscripcion = await transcribirAudio(audioBuffer);

  return {
    tipo_mensaje: "audio",
    mensajefinal: resultadoTranscripcion.texto,
    whisper: {
      duracion_segundos: resultadoTranscripcion.duracion_segundos,
      costo_usd: resultadoTranscripcion.costo_usd,
      costo_soles: resultadoTranscripcion.costo_soles,
    },
  };
}

// ============================================================
// CLOUD FUNCTION PRINCIPAL
// ============================================================
exports.procesar_audio_whatsapp = onRequest(async (req, res) => {
  const inicio = Date.now();
  try {
    const { mediaId, recipientPhoneNumber, nombreUsuario } = req.body;

    if (!mediaId || !recipientPhoneNumber) {
      return res.status(400).json({
        ok: false,
        error: "Los campos 'mediaId' y 'recipientPhoneNumber' son requeridos",
      });
    }

    const resultado = await procesarAudioWhatsapp({
      mediaId,
      recipientPhoneNumber,
      nombreUsuario,
    });

    const tiempo_ms = Date.now() - inicio;

    console.log(
      "💰 WHISPER:",
      JSON.stringify(resultado.whisper),
      "| ⏱️ TIEMPO_MS:",
      tiempo_ms,
    );

    return res.status(200).json({
      ok: true,
      tipo_mensaje: resultado.tipo_mensaje,
      mensajefinal: resultado.mensajefinal,
      tokens_usados: {
        whisper: resultado.whisper,
      },
      tiempo_ms,
    });
  } catch (error) {
    console.error("❌ Error procesar_audio_whatsapp:", error.message);
    const tiempo_ms = Date.now() - inicio;
    return res.status(500).json({ ok: false, error: error.message, tiempo_ms });
  }
});