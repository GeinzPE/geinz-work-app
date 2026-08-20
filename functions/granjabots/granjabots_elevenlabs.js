'use strict';

const functions = require('@google-cloud/functions-framework');
const axios = require('axios');
const { HttpsProxyAgent } = require('https-proxy-agent');

// =============================================================================
// CONFIGURACIÓN ESTÁTICA DEL POOL DE CREDENCIALES
// ⚠️ En producción real, usa Secret Manager en vez de hardcodear esto.
// =============================================================================
const API_CREDENTIALS_POOL = [
  { apiKey: 'KEY_1', egressProxy: 'http://user:pass@proxy1.com:8080' },
  { apiKey: 'KEY_2', egressProxy: null },
  { apiKey: 'KEY_3', egressProxy: null },
];

const VOICE_PRESETS = [
  '21m00Tcm4TlvDq8ikWAM',
  'AZnzlk1XvdvUeBnXmlld',
  'EXAVITQu4vr4xnSDxMaL',
];

const ELEVENLABS_BASE_URL = 'https://api.elevenlabs.io/v1';

function buildAxiosConfig(credential) {
  const config = { headers: { 'xi-api-key': credential.apiKey } };
  if (credential.egressProxy) {
    config.httpsAgent = new HttpsProxyAgent(credential.egressProxy);
    config.proxy = false;
  }
  return config;
}

async function checkSubscriptionQuota(credential) {
  const axiosConfig = buildAxiosConfig(credential);
  const response = await axios.get(
    `${ELEVENLABS_BASE_URL}/user/subscription`,
    { ...axiosConfig, timeout: 10000 }
  );
  const { character_limit: characterLimit, character_count: characterCount } = response.data;
  return { characterLimit, characterCount, remaining: characterLimit - characterCount };
}

async function synthesizeSpeech(credential, voiceId, texto) {
  const axiosConfig = buildAxiosConfig(credential);
  const response = await axios.post(
    `${ELEVENLABS_BASE_URL}/text-to-speech/${voiceId}`,
    {
      text: texto,
      model_id: 'eleven_multilingual_v2',
      voice_settings: { stability: 0.5, similarity_boost: 0.75 },
    },
    {
      ...axiosConfig,
      headers: { ...axiosConfig.headers, 'Content-Type': 'application/json', Accept: 'audio/mpeg' },
      responseType: 'arraybuffer',
      timeout: 30000,
    }
  );
  return Buffer.from(response.data);
}

function pickRandomVoice() {
  return VOICE_PRESETS[Math.floor(Math.random() * VOICE_PRESETS.length)];
}

function isFailoverEligibleError(error) {
  if (!error.response) return true;
  const status = error.response.status;
  return status === 401 || status === 429;
}

functions.http('textToSpeech', async (req, res) => {
  if (req.method !== 'POST') {
    return res.status(405).json({ status: 'error', message: 'Método no permitido. Utilice POST.' });
  }

  const { texto } = req.body || {};

  if (!texto || typeof texto !== 'string' || texto.trim().length === 0) {
    return res.status(400).json({
      status: 'error',
      message: 'El campo "texto" es requerido y debe ser una cadena no vacía.',
    });
  }

  const textLength = texto.length;
  let lastErrorMessage = 'No se intentó ninguna credencial.';

  for (let i = 0; i < API_CREDENTIALS_POOL.length; i++) {
    const credential = API_CREDENTIALS_POOL[i];
    try {
      const quota = await checkSubscriptionQuota(credential);

      if (quota.remaining < textLength) {
        lastErrorMessage = `Credencial índice ${i}: cuota insuficiente (restante: ${quota.remaining}, requerido: ${textLength}).`;
        console.warn(lastErrorMessage);
        continue;
      }

      const voiceId = pickRandomVoice();
      const audioBuffer = await synthesizeSpeech(credential, voiceId, texto);

      return res.status(200).json({
        status: 'success',
        voiceIdUsed: voiceId,
        keyIndex: i,
        remainingCharacters: quota.remaining - textLength,
        audioBase64: audioBuffer.toString('base64'),
      });
    } catch (error) {
      const statusCode = error.response ? error.response.status : 'N/A';
      lastErrorMessage = `Credencial índice ${i} falló (HTTP ${statusCode}): ${error.message}`;
      console.error(lastErrorMessage);
      if (isFailoverEligibleError(error)) continue;
      continue;
    }
  }

  console.error('Todas las credenciales del pool fallaron.', lastErrorMessage);
  return res.status(503).json({
    status: 'error',
    message: 'Servicio no disponible: todas las credenciales del pool fallaron o no tienen cuota suficiente.',
    detail: lastErrorMessage,
  });
});