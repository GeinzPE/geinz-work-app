"use strict";

// Local: carga el .env. En Cloud Functions no hace falta (usa variables de entorno del deploy).
try {
  require("dotenv").config();
} catch (_) {
  // dotenv no instalado: se asume que las variables ya vienen del entorno
}

const axios = require("axios");
const { HttpsProxyAgent } = require("https-proxy-agent");

// =============================================================================
// POOL DE CREDENCIALES (desde variables de entorno)
// Lee todas las variables llamadas eleven1, eleven2, ... elevenN
// =============================================================================
function loadCredentialsFromEnv() {
  return Object.keys(process.env)
    .map((name) => {
      const match = name.match(/^eleven(\d+)$/i);
      return match ? { n: Number(match[1]), name } : null;
    })
    .filter(Boolean)
    .sort((a, b) => a.n - b.n)
    .map(({ name }) => ({
      apiKey: (process.env[name] || "").trim(),
      egressProxy: null,
    }))
    .filter((c) => c.apiKey.length > 0);
}

const API_CREDENTIALS_POOL = loadCredentialsFromEnv();

console.log(`Pool ElevenLabs cargado: ${API_CREDENTIALS_POOL.length} credenciales.`);

const VOICE_PRESETS = ["CwhRBWXzGAHq8TQ4Fs17"];

const ELEVENLABS_BASE_URL = "https://api.elevenlabs.io/v1";

// Índice de rotación round-robin entre invocaciones de la misma instancia.
let rotationIndex = 0;

// Revisa cada key y decide si está sana, sin créditos, o muerta (inválida/falla).
function classifyKeys(details) {
  const toDelete = [];
  const healthy = [];

  details.forEach((d) => {
    if (!d.ok) {
      toDelete.push({
        index: d.index,
        reason: `No responde (HTTP ${d.statusCode || "N/A"}: ${d.error || "desconocido"})`,
      });
    } else if (d.remaining <= 0) {
      toDelete.push({ index: d.index, reason: "0 créditos restantes" });
    } else {
      healthy.push(d);
    }
  });

  return { toDelete, healthy };
}

// Recorre todo el pool y suma cuánto crédito queda en total.
async function getTotalCredits() {
  let totalLimit = 0;
  let totalRemaining = 0;
  let totalUsed = 0;
  let workingKeys = 0;
  let failedKeys = 0;
  const details = [];

  for (let i = 0; i < API_CREDENTIALS_POOL.length; i++) {
    const credential = API_CREDENTIALS_POOL[i];
    try {
      const quota = await checkSubscriptionQuota(credential);
      totalLimit += quota.characterLimit;
      totalRemaining += quota.remaining;
      totalUsed += quota.characterCount;
      workingKeys++;
      details.push({ index: i, ...quota, ok: true });
    } catch (error) {
      failedKeys++;
      const statusCode = error.response ? error.response.status : "N/A";
      console.error(
        `Credencial índice ${i} falló al consultar cuota (HTTP ${statusCode}): ${error.message}`,
      );
      details.push({ index: i, ok: false, error: error.message, statusCode });
    }
  }

  return {
    totalLimit,
    totalRemaining,
    totalUsed,
    workingKeys,
    failedKeys,
    poolSize: API_CREDENTIALS_POOL.length,
    details,
  };
}

function buildAxiosConfig(credential) {
  const config = { headers: { "xi-api-key": credential.apiKey } };
  if (credential.egressProxy) {
    config.httpsAgent = new HttpsProxyAgent(credential.egressProxy);
    config.proxy = false;
  }
  return config;
}

async function checkSubscriptionQuota(credential) {
  const axiosConfig = buildAxiosConfig(credential);
  const response = await axios.get(`${ELEVENLABS_BASE_URL}/user/subscription`, {
    ...axiosConfig,
    timeout: 10000,
  });
  const { character_limit: characterLimit, character_count: characterCount } =
    response.data;
  return {
    characterLimit,
    characterCount,
    remaining: characterLimit - characterCount,
  };
}

async function synthesizeSpeech(credential, voiceId, texto) {
  const axiosConfig = buildAxiosConfig(credential);
  const response = await axios.post(
    `${ELEVENLABS_BASE_URL}/text-to-speech/${voiceId}`,
    {
      text: texto,
      model_id: "eleven_multilingual_v2",
      voice_settings: { stability: 0.5, similarity_boost: 0.75 },
    },
    {
      ...axiosConfig,
      headers: {
        ...axiosConfig.headers,
        "Content-Type": "application/json",
        Accept: "audio/mpeg",
      },
      responseType: "arraybuffer",
      timeout: 30000,
    },
  );
  return Buffer.from(response.data);
}

function pickRandomVoice() {
  return VOICE_PRESETS[Math.floor(Math.random() * VOICE_PRESETS.length)];
}

// true  -> vale la pena intentar con la siguiente credencial (401/429/timeout/sin respuesta)
// false -> error no recuperable con otra key
function isFailoverEligibleError(error) {
  if (!error.response) return true;
  const status = error.response.status;
  return status === 401 || status === 429;
}

async function textToSpeech_granja_eleven(req, res) {
  res.set("Access-Control-Allow-Origin", "*");
  if (req.method === "OPTIONS") {
    res.set("Access-Control-Allow-Methods", "POST");
    res.set("Access-Control-Allow-Headers", "Content-Type");
    return res.status(204).send("");
  }

  if (req.method !== "POST") {
    return res
      .status(405)
      .json({ status: "error", message: "Método no permitido. Utilice POST." });
  }

  const { texto, voiceId: requestedVoiceId } = req.body || {};

  if (!texto || typeof texto !== "string" || texto.trim().length === 0) {
    return res.status(400).json({
      status: "error",
      message: 'El campo "texto" es requerido y debe ser una cadena no vacía.',
    });
  }

  if (
    requestedVoiceId !== undefined &&
    (typeof requestedVoiceId !== "string" ||
      requestedVoiceId.trim().length === 0)
  ) {
    return res.status(400).json({
      status: "error",
      message: 'El campo "voiceId" debe ser una cadena no vacía si se envía.',
    });
  }

  if (API_CREDENTIALS_POOL.length === 0) {
    console.error(
      "El pool de credenciales está vacío. Revisa las variables eleven1, eleven2, ...",
    );
    return res.status(503).json({
      status: "error",
      message: "Servicio no disponible: no hay credenciales configuradas.",
    });
  }

  const textLength = texto.length;
  let lastErrorMessage = "No se intentó ninguna credencial.";
  const poolSize = API_CREDENTIALS_POOL.length;

  for (let offset = 0; offset < poolSize; offset++) {
    const i = (rotationIndex + offset) % poolSize;
    const credential = API_CREDENTIALS_POOL[i];

    try {
      const quota = await checkSubscriptionQuota(credential);

      if (quota.remaining < textLength) {
        lastErrorMessage = `Credencial índice ${i}: cuota insuficiente (restante: ${quota.remaining}, requerido: ${textLength}).`;
        console.warn(lastErrorMessage);
        continue;
      }

      const voiceId = requestedVoiceId || pickRandomVoice();
      const audioBuffer = await synthesizeSpeech(credential, voiceId, texto);

      rotationIndex = (i + 1) % poolSize;

      return res.status(200).json({
        status: "success",
        voiceIdUsed: voiceId,
        keyIndex: i,
        remainingCharacters: quota.remaining - textLength,
        audioBase64: audioBuffer.toString("base64"),
      });
    } catch (error) {
      const statusCode = error.response ? error.response.status : "N/A";
      lastErrorMessage = `Credencial índice ${i} falló (HTTP ${statusCode}): ${error.message}`;
      console.error(lastErrorMessage);

      if (!isFailoverEligibleError(error)) {
        return res.status(statusCode === "N/A" ? 500 : statusCode).json({
          status: "error",
          message: "Error no recuperable al generar el audio.",
          detail: lastErrorMessage,
        });
      }
      continue;
    }
  }

  console.error("Todas las credenciales del pool fallaron.", lastErrorMessage);
  return res.status(503).json({
    status: "error",
    message:
      "Servicio no disponible: todas las credenciales del pool fallaron o no tienen cuota suficiente.",
    detail: lastErrorMessage,
  });
}

module.exports = { textToSpeech_granja_eleven, getTotalCredits, classifyKeys };