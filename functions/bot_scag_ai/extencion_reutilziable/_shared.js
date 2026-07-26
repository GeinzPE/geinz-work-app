"use strict";

const admin = require("firebase-admin");
const axios = require("axios");

// ── URLs de los modelos ───────────────────────────────────────────────────────
const GEMINI_FLASH_URL =
  "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent";
const GEMINI_PRO_URL =
  "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-pro:generateContent";
const OPENAI_URL = "https://api.openai.com/v1/chat/completions";

const USERS_COLLECTION = "trabajos_ia";

const MODEL_MAP = {
  "gemini-flash": { family: "gemini", endpoint: GEMINI_FLASH_URL },
  "gemini-pro": { family: "gemini", endpoint: GEMINI_PRO_URL },
  "gpt-4o": { family: "openai", model: "gpt-4o" },
  "gpt-4o-mini": { family: "openai", model: "gpt-4o-mini" },
  "gpt4o-mini": { family: "openai", model: "gpt-4o-mini" },
  gpt4o: { family: "openai", model: "gpt-4o" },
};

const PRECIO_USD_POR_MILLON = {
  "gemini-flash": { input: 0.3, output: 2.5 },
  "gemini-pro": { input: 1.25, output: 10.0 },
  "gpt-4o": { input: 2.5, output: 10.0 },
  "gpt-4o-mini": { input: 0.15, output: 0.6 },
  "gpt4o-mini": { input: 0.15, output: 0.6 },
  gpt4o: { input: 2.5, output: 10.0 },
};

const TIPO_CAMBIO_USD_PEN = 3.75;

function calcularCostoRealUSD(provider, tokensInput, tokensOutput) {
  const precio = PRECIO_USD_POR_MILLON[provider];
  if (!precio) return 0;
  const costoInput = ((Number(tokensInput) || 0) / 1_000_000) * precio.input;
  const costoOutput = ((Number(tokensOutput) || 0) / 1_000_000) * precio.output;
  return costoInput + costoOutput;
}

let db2 = null;
const CACHE_TTL = 5 * 60 * 1000;

// ── Inicialización de Firestore secundario (app2) ─────────────────────────────
const initDb2 = () => {
  if (db2) return db2;
  try {
    const appExistente = admin.apps.find((app) => app.name === "app2");
    if (appExistente) {
      db2 = appExistente.firestore();
    } else {
      const app2 = admin.initializeApp(
        {
          credential: admin.credential.cert({
            projectId: process.env.PROJECT_ID_2,
            clientEmail: process.env.CLIENT_EMAIL_2,
            privateKey: process.env.PRIVATE_KEY_2?.replace(/\\n/g, "\n"),
          }),
        },
        "app2",
      );
      db2 = app2.firestore();
    }
  } catch (e) {
    console.error("❌ Error inicializando app2:", e.message);
    db2 = null;
  }
  return db2;
};

// ── GEMINI TEXT ───────────────────────────────────────────────────────────────
async function callGeminiText(
  text,
  apiKey,
  endpoint,
  systemPrompt,
  tokens,
  thinkingBudget = 800,
) {
  const isPro = endpoint.includes("2.5-pro");

  // 🛠️ FIX 1: Limpiamos la instrucción del usuario. No dupliques órdenes si ya están en el SYSTEM_PROMPT.
  // Dejamos que el systemPrompt controle la estructura de la respuesta de forma limpia.
  const userText = text;

  try {
    const { data } = await axios.post(
      `${endpoint}?key=${apiKey}`,
      {
        system_instruction: { parts: [{ text: systemPrompt }] },
        contents: [{ role: "user", parts: [{ text: userText }] }],
        generationConfig: {
          temperature: 0.0,
          maxOutputTokens: tokens,
          ...(isPro &&
            thinkingBudget > 0 && { thinkingConfig: { thinkingBudget } }),
        },
      },
      {
        headers: { "Content-Type": "application/json" },
        timeout: isPro ? 60000 : 30000,
      },
    );

    // 🛠️ FIX 2: Extracción segura y robusta del texto de respuesta
    let answer = "";
    const parts = data?.candidates?.[0]?.content?.parts;

    if (parts && Array.isArray(parts)) {
      // Buscamos el bloque de texto que contenga la respuesta final de forma segura
      // Ignoramos los bloques que el SDK de Google marque nativamente como thoughts o bloques de razonamiento interno
      const textPart = parts.find(
        (p) => p.text && p.thought !== true && !p.hasOwnProperty("thought"),
      );

      // Si el filtro estricto falla, tomamos el último elemento de texto disponible (que suele ser la respuesta final post-razonamiento)
      answer = textPart
        ? textPart.text.trim()
        : (parts[parts.length - 1]?.text?.trim() ?? "");
    }

    // 🛠️ FIX 3: Control de respuestas vacías o fallas lógicas
    if (!answer || answer === "" || answer === "SIN_CONTENIDO") {
      console.warn("⚠️ Gemini Text retornó un resultado vacío o restrictivo.");
      answer = "Sin respuesta.";
    }

    // Estructura de metadatos de uso limpia (Mantiene tu lógica de cobros intacta)
    const usage = {
      tokensInput: data?.usageMetadata?.promptTokenCount ?? 0,
      tokensOutput:
        (data?.usageMetadata?.candidatesTokenCount ?? 0) +
        (data?.usageMetadata?.thoughtsTokenCount ?? 0),
      tokensTotal: data?.usageMetadata?.totalTokenCount ?? 0,
    };

    return { answer, usage };
  } catch (error) {
    console.error(
      "❌ Error catastrófico en la llamada a Gemini Text:",
      error.message,
    );
    // Retornamos estructura limpia para proteger el flujo del backend
    return {
      answer: "Sin respuesta.",
      usage: { tokensInput: 0, tokensOutput: 0, tokensTotal: 0 },
    };
  }
}

// ── GEMINI VISION ─────────────────────────────────────────────────────────────
async function callGeminiVision(
  imageBase64,
  mimeType,
  textHint,
  apiKey,
  endpoint,
  systemPrompt,
  tokens,
  thinkingBudget = 512,
) {
  const isPro = endpoint.includes("2.5-pro");
  console.log("=== DEBUG callGeminiVision ===");
  console.log("mimeType:", mimeType);
  console.log("textHint:", textHint);
  console.log("tokens:", tokens);

  // FIX: Limpieza automática del prefijo Base64 si el frontend lo envía mal
  let cleanBase64 = imageBase64;
  if (imageBase64 && imageBase64.startsWith("data:")) {
    console.log(
      "⚠️ Detectado prefijo data: en Base64. Limpiando automáticamente...",
    );
    cleanBase64 = imageBase64.split(",")[1];
  }

  if (!cleanBase64) {
    console.error("❌ El contenido de la imagen está vacío o corrupto.");
    return {
      answer: "Sin respuesta.",
      usage: { tokensInput: 0, tokensOutput: 0, tokensTotal: 0 },
    };
  }

  try {
    const userPromptText = textHint
      ? `Analiza detalladamente esta imagen siguiendo las instrucciones del sistema.\nContexto adicional: ${textHint}`
      : "Analiza detalladamente esta imagen siguiendo las instrucciones del sistema.";

    const { data } = await axios.post(
      `${endpoint}?key=${apiKey}`,
      {
        system_instruction: { parts: [{ text: systemPrompt }] },
        contents: [
          {
            role: "user",
            parts: [
              { inline_data: { mime_type: mimeType, data: cleanBase64 } },
              { text: userPromptText },
            ],
          },
        ],
        generationConfig: {
          temperature: 0.2,
          maxOutputTokens: tokens,
          ...(isPro &&
            thinkingBudget > 0 && { thinkingConfig: { thinkingBudget } }),
        },
      },
      { headers: { "Content-Type": "application/json" }, timeout: 45000 },
    );

    console.log("=== RESPUESTA GEMINI ===");
    const finishReason = data?.candidates?.[0]?.finishReason;
    console.log("finish_reason:", finishReason);

    // ✅ FIX PRINCIPAL: igual que callGeminiText, ignorar bloques de thinking
    let answer = "";
    const parts = data?.candidates?.[0]?.content?.parts;

    if (parts && Array.isArray(parts)) {
      console.log("🧩 Total de partes recibidas:", parts.length);
      parts.forEach((p, i) => {
        console.log(
          `  part[${i}]: thought=${p.thought}, chars=${p.text?.length ?? 0}`,
        );
      });

      // Busca el primer bloque que NO sea thinking
      const textPart = parts.find(
        (p) => p.text && p.thought !== true && !p.hasOwnProperty("thought"),
      );

      // Si el filtro falla, toma el último bloque disponible
      answer = textPart
        ? textPart.text.trim()
        : (parts[parts.length - 1]?.text?.trim() ?? "");
    }

    if (!answer || answer === "" || answer === "SIN_CONTENIDO") {
      console.warn(
        "⚠️ Gemini Vision retornó una estructura vacía o restrictiva.",
      );
      answer = "Sin respuesta.";
    }

    const usage = {
      tokensInput: data?.usageMetadata?.promptTokenCount ?? 0,
      tokensOutput:
        (data?.usageMetadata?.candidatesTokenCount ?? 0) +
        (data?.usageMetadata?.thoughtsTokenCount ?? 0),
      tokensTotal: data?.usageMetadata?.totalTokenCount ?? 0,
    };

    return { answer, usage };
  } catch (error) {
    console.error(
      "❌ Error catastrófico en la llamada a Gemini Vision:",
      error.message,
    );
    return {
      answer: "Sin respuesta.",
      usage: { tokensInput: 0, tokensOutput: 0, tokensTotal: 0 },
    };
  }
}

// ── OPENAI TEXT ───────────────────────────────────────────────────────────────
async function callOpenAIText(text, apiKey, model, systemPrompt, tokens) {
  try {
    const { data } = await axios.post(
      OPENAI_URL,
      {
        model,
        messages: [
          { role: "system", content: systemPrompt },
          { role: "user", content: text },
        ],
        max_tokens: tokens,
        temperature: 0.0, // Perfecto para exámenes (busca la respuesta más exacta posible)
      },
      {
        headers: {
          "Content-Type": "application/json",
          Authorization: `Bearer ${apiKey}`,
        },
        timeout: 30000, // 30 segundos está bien para texto plano
      },
    );

    let answer = data?.choices?.[0]?.message?.content?.trim();

    // 🛠️ FIX 1: Control de respuestas vacías o fallas lógicas de escape
    if (!answer || answer === "" || answer === "SIN_CONTENIDO") {
      console.warn("⚠️ OpenAI Text retornó un resultado vacío o restrictivo.");
      answer = "Sin respuesta.";
    }

    // 🛠️ FIX 2: Conteo de tokens ultra seguro
    const tokensInput = data?.usage?.prompt_tokens ?? 0;
    const tokensOutput = data?.usage?.completion_tokens ?? 0;
    const tokensTotal = data?.usage?.total_tokens ?? tokensInput + tokensOutput;

    const usage = {
      tokensInput,
      tokensOutput,
      tokensTotal,
    };

    return { answer, usage };
  } catch (error) {
    // 🛠️ FIX 3: Captura de errores de API (ej: Insufficient Quota, Rate Limit, Invalid API Key)
    console.error(
      "❌ Error catastrófico en la llamada a OpenAI Text:",
      error?.response?.data?.error?.message || error.message,
    );

    // Retornamos estructura limpia para que tu backend no sufra un crash y el usuario reciba el string controlado
    return {
      answer: "Sin respuesta.",
      usage: { tokensInput: 0, tokensOutput: 0, tokensTotal: 0 },
    };
  }
}

// ── OPENAI VISION ─────────────────────────────────────────────────────────────
async function callOpenAIVision(
  imageBase64,
  mimeType,
  textHint,
  apiKey,
  model,
  systemPrompt,
  tokens,
) {
  console.log("=== DEBUG callOpenAIVision ===");
  console.log("model:", model);
  console.log("mimeType:", mimeType);

  // 🛠️ FIX 1: Estandarizar el Base64 (OpenAI requiere el prefijo obligatoriamente)
  let finalImageUrl = imageBase64;
  if (imageBase64 && !imageBase64.startsWith("data:")) {
    finalImageUrl = `data:${mimeType};base64,${imageBase64}`;
  } else if (imageBase64 && imageBase64.startsWith("data:")) {
    finalImageUrl = imageBase64;
  }

  if (!finalImageUrl) {
    console.error("❌ El contenido de la imagen de OpenAI está vacío.");
    return {
      answer: "Sin respuesta.",
      usage: { tokensInput: 0, tokensOutput: 0, tokensTotal: 0 },
    };
  }

  try {
    // 🛠️ FIX 2: Alinear el prompt del usuario con las instrucciones del sistema
    const userPromptText = textHint
      ? `Analiza detalladamente esta imagen siguiendo las instrucciones del sistema.\nContexto adicional: ${textHint}`
      : "Analiza detalladamente esta imagen siguiendo las instrucciones del sistema.";

    const { data } = await axios.post(
      OPENAI_URL,
      {
        model,
        messages: [
          { role: "system", content: systemPrompt },
          {
            role: "user",
            content: [
              {
                type: "image_url",
                image_url: { url: finalImageUrl },
              },
              {
                type: "text",
                text: userPromptText,
              },
            ],
          },
        ],
        max_tokens: tokens,
        temperature: 0.2,
      },
      {
        headers: {
          "Content-Type": "application/json",
          Authorization: `Bearer ${apiKey}`,
        },
        timeout: 45000,
      },
    );

    let answer = data?.choices?.[0]?.message?.content?.trim();

    // 🛠️ FIX 3: Control de respuestas vacías o comodines de escape de tu negocio
    if (!answer || answer === "" || answer === "SIN_CONTENIDO") {
      console.warn("⚠️ OpenAI retornó un string vacío o restringido.");
      answer = "Sin respuesta.";
    }

    // 🛠️ FIX 4: Mapeo de tokens ultra seguro para asegurar la precisión de tu ganancia
    const tokensInput = data?.usage?.prompt_tokens ?? 0;
    const tokensOutput = data?.usage?.completion_tokens ?? 0;

    // Si OpenAI cambia la estructura o devuelve el total de forma explícita, lo priorizamos
    const tokensTotal = data?.usage?.total_tokens ?? tokensInput + tokensOutput;

    const usage = {
      tokensInput,
      tokensOutput,
      tokensTotal,
    };

    return { answer, usage };
  } catch (error) {
    // Si la API de OpenAI explota por falta de saldo, clave inválida o timeout, esto salva tu backend
    console.error(
      "❌ Error catastrófico en la llamada a OpenAI Vision:",
      error?.response?.data?.error?.message || error.message,
    );
    return {
      answer: "Sin respuesta.",
      usage: { tokensInput: 0, tokensOutput: 0, tokensTotal: 0 },
    };
  }
}

function limpiarRespuesta(texto) {
  const lineasCorte = [
    "Re-Identifico",
    "Re-Aplico",
    "Re-Re",
    "Tampoco coincide",
    "Sigue sin coincidir",
    "No coincide",
    "Se reevalúa",
    "Forzando la opción",
    "El problema está mal planteado",
  ];

  const lines = texto.split("\n");
  const resultado = [];

  for (const line of lines) {
    if (lineasCorte.some((frase) => line.includes(frase))) {
      break;
    }
    resultado.push(line);
  }

  return resultado.join("\n").trim();
}

module.exports = {
  admin,
  axios,
  GEMINI_FLASH_URL,
  GEMINI_PRO_URL,
  OPENAI_URL,
  USERS_COLLECTION,
  MODEL_MAP,
  PRECIO_USD_POR_MILLON,
  TIPO_CAMBIO_USD_PEN,
  calcularCostoRealUSD,
  initDb2,
  callGeminiText,
  callGeminiVision,
  callOpenAIText,
  callOpenAIVision,
  limpiarRespuesta,
};
