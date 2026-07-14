"use strict";

const { onRequest } = require("firebase-functions/v2/https");
const axios = require("axios");

// ── Configuración ElevenLabs ──────────────────────────────────────────────────
const DEFAULT_VOICE_ID = "onwK4e9ZLuTAKqWW03F9"; // voice_id actualizado
const DEFAULT_MODEL_ID = "eleven_multilingual_v2";

// ── Cloud Function: Texto → Voz (ElevenLabs) para n8n / WhatsApp ─────────────
// Devuelve { audioContent: "<base64>" } igual que OpenAI TTS.
// El binario decodificado es Opus (contenedor OGG).
const textoAVozn8n_elevenlabs_2 = onRequest(
  {
    region: "us-central1",
    timeoutSeconds: 60,
    memory: "256MiB",
    cors: true,
  },

  async (req, res) => {
    console.log(
      `[TTS-ElevenLabs] Nueva petición. Método: ${req.method}`,
    );

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
      voiceId = DEFAULT_VOICE_ID,
      modelId = DEFAULT_MODEL_ID,
    } = req.body || {};

    console.log("📦 [TTS-ElevenLabs] Body recibido:", JSON.stringify(req.body));

    if (!text || !text.trim()) {
      res.status(400).json({ ok: false, error: "Campo 'text' requerido." });
      return;
    }

    const MAX_CHARS_TTS = 2500;
    if (text.trim().length > MAX_CHARS_TTS) {
      res.status(413).json({
        ok: false,
        code: "TEXT_TOO_LONG",
        error: `El texto tiene ${text.trim().length} caracteres. El máximo permitido es ${MAX_CHARS_TTS}.`,
      });
      return;
    }

    const ELEVENLABS_API_KEY = process.env.CLAVE_API_ELEVENLABS;
    if (!ELEVENLABS_API_KEY) {
      console.error("[TTS-ElevenLabs] Falta CLAVE_API_ELEVENLABS en las variables de entorno.");
      res.status(500).json({
        ok: false,
        error: "Configuración de API faltante (CLAVE_API_ELEVENLABS).",
      });
      return;
    }

    try {
      console.log(
        `[TTS-ElevenLabs] Generando audio. voiceId="${voiceId}", modelId="${modelId}", chars=${text.length}`,
      );

      const OUTPUT_FORMAT = "opus_48000_128"; // Opus/OGG, compatible con notas de voz de WhatsApp

      const response = await axios.post(
        `https://api.elevenlabs.io/v1/text-to-speech/${voiceId}?output_format=${OUTPUT_FORMAT}`,
        {
          text,
          model_id: modelId,
          voice_settings: {
            stability: 0.5,
            similarity_boost: 0.75,
          },
        },
        {
          headers: {
            "Content-Type": "application/json",
            "xi-api-key": ELEVENLABS_API_KEY,
          },
          responseType: "arraybuffer",
          timeout: 45000,
        },
      );

      const audioBuffer = Buffer.from(response.data);
      console.log(`[TTS-ElevenLabs] Audio generado correctamente. bytes=${audioBuffer.length}`);

      if (!audioBuffer || audioBuffer.length === 0) {
        console.error("[TTS-ElevenLabs] El audio generado llegó vacío.");
        res.status(502).json({ ok: false, error: "El audio generado está vacío." });
        return;
      }

      // Mismo formato de salida que OpenAI TTS: { audioContent: "<base64>" }
      // El contenido decodificado es Opus (contenedor OGG), compatible con WhatsApp voice notes.
      res.status(200).json({
        audioContent: audioBuffer.toString("base64"),
        format: "opus",
        mimeType: "audio/ogg",
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

      console.error("❌ [TTS-ElevenLabs] Error catastrófico:", detalleError);

      res.status(502).json({
        ok: false,
        error: "Error al generar el audio con ElevenLabs.",
        detalle: detalleError,
      });
    }
  },
);


module.exports = { textoAVozn8n_elevenlabs_2 };