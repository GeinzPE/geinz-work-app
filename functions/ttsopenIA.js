"use strict";

const { onRequest } = require("firebase-functions/v2/https");
const axios = require("axios");

// ── Configuración OpenAI TTS ──────────────────────────────────────────────────
const DEFAULT_VOICE = "alloy"; // alloy, echo, fable, onyx, nova, shimmer
const DEFAULT_MODEL = "tts-1"; // o "tts-1-hd" para mayor calidad
const VOCES_VALIDAS = ["alloy", "echo", "fable", "onyx", "nova", "shimmer"];
const RESPONSE_FORMAT = "opus"; // Ogg/Opus: formato requerido por WhatsApp para notas de voz (audio.voice=true)

// ── Cloud Function: Texto → Voz (OpenAI) para n8n / WhatsApp ─────────────────
// Devuelve { audioContent: "<base64>" } igual que Google TTS.
// El binario decodificado ahora es OGG_OPUS, compatible con WhatsApp voice notes.
const textoAVozOpenAI = onRequest(
  {
    region: "us-central1",
    timeoutSeconds: 60,
    memory: "256MiB",
    cors: true,
  },
  async (req, res) => {
    console.log(`[TTS-OpenAI] Nueva petición. Método: ${req.method}`);

    if (req.method === "OPTIONS") {
      res.status(204).send("");
      return;
    }

    if (req.method !== "POST") {
      res.status(405).json({ ok: false, error: "Método no permitido." });
      return;
    }

    const {
      text = "",
      voice = DEFAULT_VOICE,
      model = DEFAULT_MODEL,
    } = req.body || {};

    console.log("📦 [TTS-OpenAI] Body recibido:", JSON.stringify(req.body));

    if (!text || !text.trim()) {
      res.status(400).json({ ok: false, error: "Campo 'text' requerido." });
      return;
    }

    const vozFinal = VOCES_VALIDAS.includes(voice) ? voice : DEFAULT_VOICE;
    if (voice && voice !== vozFinal) {
      console.warn(`[TTS-OpenAI] Voz "${voice}" no válida, usando "${DEFAULT_VOICE}".`);
    }

    const MAX_CHARS_TTS = 4096; // límite de OpenAI TTS por request
    if (text.trim().length > MAX_CHARS_TTS) {
      res.status(413).json({
        ok: false,
        code: "TEXT_TOO_LONG",
        error: `El texto tiene ${text.trim().length} caracteres. El máximo permitido es ${MAX_CHARS_TTS}.`,
      });
      return;
    }

    const OPENAI_API_KEY = process.env.PIRVATE_KEY_OPENIA_APITRABAJO;
    if (!OPENAI_API_KEY) {
      console.error("[TTS-OpenAI] Falta PIRVATE_KEY_OPENIA_APITRABAJO en las variables de entorno.");
      res.status(500).json({
        ok: false,
        error: "Configuración de API faltante (PIRVATE_KEY_OPENIA_APITRABAJO).",
      });
      return;
    }

    try {
      console.log(
        `[TTS-OpenAI] Generando audio. voice="${vozFinal}", model="${model}", format="${RESPONSE_FORMAT}", chars=${text.length}`,
      );

      const response = await axios.post(
        "https://api.openai.com/v1/audio/speech",
        {
          model,
          voice: vozFinal,
          input: text,
          response_format: RESPONSE_FORMAT,
        },
        {
          headers: {
            "Content-Type": "application/json",
            Authorization: `Bearer ${OPENAI_API_KEY}`,
          },
          responseType: "arraybuffer",
          timeout: 45000,
        },
      );

      const audioBuffer = Buffer.from(response.data);
      console.log(`[TTS-OpenAI] Audio generado correctamente. bytes=${audioBuffer.length}`);

      if (!audioBuffer || audioBuffer.length === 0) {
        console.error("[TTS-OpenAI] El audio generado llegó vacío.");
        res.status(502).json({ ok: false, error: "El audio generado está vacío." });
        return;
      }

      // Mismo formato de salida que Google TTS: { audioContent: "<base64>" }
      // El contenido decodificado es Ogg/Opus, listo para WhatsApp voice notes.
      res.status(200).json({
        audioContent: audioBuffer.toString("base64"),
        format: RESPONSE_FORMAT, // "opus" — útil para que n8n arme el mime type/extensión correctos
        mimeType: "audio/ogg", // referencia directa para el nodo "Convert to File"
      });
    } catch (error) {
      const raw = error?.response?.data;
      let detalleError = raw;

      if (raw instanceof Buffer) {
        try {
          detalleError = JSON.parse(raw.toString());
        } catch (_) {
          detalleError = raw.toString();
        }
      }
      detalleError = detalleError || error.message;

      console.error("❌ [TTS-OpenAI] Error catastrófico:", detalleError);

      res.status(502).json({
        ok: false,
        error: "Error al generar el audio con OpenAI.",
        detalle: detalleError,
      });
    }
  },
);

module.exports = { textoAVozOpenAI };