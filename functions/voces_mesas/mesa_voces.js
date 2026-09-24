"use strict";

const { onRequest } = require("firebase-functions/v2/https");
const axios = require("axios");
const admin = require("firebase-admin");

if (!admin.apps.length) {
  admin.initializeApp();
}
const bucket = admin.storage().bucket(); // gs://geinzworkapp.appspot.com

const DEFAULT_VOICE_ID = "huuKaN52HlVabecRQ4Jt";
const DEFAULT_MODEL_ID = "eleven_multilingual_v2";
const OUTPUT_FORMAT = "mp3_44100_128";

const generarSonidosMesas = onRequest(
  { region: "us-central1", timeoutSeconds: 540, memory: "512MiB", cors: true },
  async (req, res) => {
    if (req.method === "OPTIONS") { res.status(204).send(""); return; }
    if (req.method !== "POST" && req.method !== "GET") {
      res.status(405).json({ ok: false, error: "Método no permitido." });
      return;
    }

    const body = req.body || {};
    const totalMesas = parseInt(body.totalMesas || req.query.totalMesas || process.env.TOTAL_MESAS || "50", 10);
    const voiceId = body.voiceId || DEFAULT_VOICE_ID;
    const modelId = body.modelId || DEFAULT_MODEL_ID;
    const storagePrefix = process.env.STORAGE_PREFIX || "sounds/mesas";

    if (!totalMesas || totalMesas < 1) {
      res.status(400).json({ ok: false, error: "totalMesas debe ser un número mayor a 0." });
      return;
    }

    const ELEVENLABS_API_KEY = process.env.APIKEY_ELEVENLABS_MUCHOS_CREDITOS;
    if (!ELEVENLABS_API_KEY) {
      res.status(500).json({ ok: false, error: "Configuración de API faltante (APIKEY_ELEVENLABS_MUCHOS_CREDITOS)." });
      return;
    }

    const piezas = [
      { nombre: "pedido_prefijo", texto: "Nuevo pedido en" },
      { nombre: "cuenta_prefijo", texto: "Solicitan la cuenta en" },
      { nombre: "mozo_prefijo", texto: "Llaman al mozo en" },
      { nombre: "reserva_prefijo", texto: "Nueva reserva asignada para" },
      { nombre: "y", texto: "y" },
    ];
    for (let n = 1; n <= totalMesas; n++) {
      piezas.push({ nombre: `mesa_${n}`, texto: `Mesa ${n}` });
    }

    async function generarPieza(pieza) {
      const response = await axios.post(
        `https://api.elevenlabs.io/v1/text-to-speech/${voiceId}?output_format=${OUTPUT_FORMAT}`,
        {
          text: pieza.texto,
          model_id: modelId,
          voice_settings: { stability: 0.5, similarity_boost: 0.75 },
        },
        {
          headers: { "Content-Type": "application/json", "xi-api-key": ELEVENLABS_API_KEY },
          responseType: "arraybuffer",
          timeout: 45000,
        }
      );

      const audioBuffer = Buffer.from(response.data);
      if (!audioBuffer || audioBuffer.length === 0) {
        throw new Error(`Audio vacío para "${pieza.nombre}"`);
      }

      const destino = `${storagePrefix}/${pieza.nombre}.mp3`;
      const file = bucket.file(destino);

      await file.save(audioBuffer, {
        metadata: { contentType: "audio/mpeg" },
        resumable: false,
      });

      const [url] = await file.getSignedUrl({
        action: "read",
        expires: "03-01-2035",
      });

      return { nombre: pieza.nombre, path: destino, url };
    }

    try {
      const resultados = [];
      const fallidas = [];
      const CONCURRENCY = 4;
      let index = 0;

      async function worker() {
        while (index < piezas.length) {
          const pieza = piezas[index++];
          try {
            resultados.push(await generarPieza(pieza));
          } catch (err) {
            fallidas.push({ nombre: pieza.nombre, error: String(err?.response?.data || err.message) });
          }
        }
      }

      await Promise.all(Array.from({ length: CONCURRENCY }, () => worker()));

      res.status(fallidas.length > 0 ? 207 : 200).json({
        ok: true,
        format: "mp3",
        totalMesas,
        bucket: bucket.name,
        storagePrefix,
        piezas: resultados,
        fallidas,
      });
    } catch (error) {
      const raw = error?.response?.data;
      let detalleError = raw instanceof Buffer ? (() => { try { return JSON.parse(raw.toString()); } catch { return raw.toString(); } })() : raw || error.message;

      res.status(502).json({
        ok: false,
        error: "Error al generar las piezas de audio con ElevenLabs.",
        detalle: detalleError,
      });
    }
  }
);

module.exports = { generarSonidosMesas };