/**
 * screenaiQuery.js — Cloud Function v2
 * Usa db2 (segunda app de Firebase) para buscar usuarios en /trabajos_ia/{alias}
 */

"use strict";

const { onRequest } = require("firebase-functions/v2/https");
const admin = require("firebase-admin");
const axios = require("axios");

const GEMINI_FLASH_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent";
const GEMINI_PRO_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-pro:generateContent";
const OPENAI_URL = "https://api.openai.com/v1/chat/completions";
const CLAUDE_URL = "https://api.anthropic.com/v1/messages";
const CLAUDE_VERSION = "2023-06-01";

const USERS_COLLECTION = "trabajos_ia";
// ─── MODEL MAP ────────────────────────────────────────────────────────────────
const MODEL_MAP = {
  "gemini-flash": {
    family: "gemini",
    endpoint: GEMINI_FLASH_URL,
  },
  "gemini-pro": {
    family: "gemini",
    endpoint: GEMINI_PRO_URL,
  },
  "gpt-4o": {
    family: "openai",
    model: "gpt-4o",
  },
  "gpt-4o-mini": {
    family: "openai",
    model: "gpt-4o-mini",
  },
  "claude-haiku": {
    family: "claude",
    model: "claude-haiku-4-5",
  },
  "claude-sonnet": {
    family: "claude",
    model: "claude-sonnet-4-6",
  },
};
// ─── DB2 (segunda app Firebase) ───────────────────────────────────────────────
let db2 = null;

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
        "app2"
      );
      db2 = app2.firestore();
    }
  } catch (e) {
    console.error("❌ Error inicializando app2:", e.message);
    db2 = null;
  }
  return db2;
};

// ─── Modelos ──────────────────────────────────────────────────────────────────
const SYSTEM_PROMPTS = {
  general: `Eres un asistente de exámenes. Responde SOLO con las respuestas, sin explicar.

FORMATO OBLIGATORIO — una línea por pregunta:
  número) respuesta

REGLAS:
- Opción múltiple → texto EXACTO de la opción correcta (no la letra).
- Verdadero/Falso → solo "Verdadero" o "Falso".
- Completar → solo la palabra o frase exacta.
- Respuesta corta → máximo 6 palabras.
- Si dice "explica", "describe" o "justifica" → 1 oración, máx 20 palabras.
- NUNCA escribas "La respuesta es", introducciones ni markdown.`,

  matematicas: `Eres asistente de exámenes de matemáticas. Da SOLO el resultado final.

FORMATO: número) resultado

REGLAS:
- Solo el valor final, sin desarrollo.
- Fracciones en forma reducida (ej: 3/4). Decimales: máx 2 cifras.
- Ecuaciones: solo el valor (ej: x=5).
- Geometría: incluye unidad (ej: 12 cm²).
- Estadística: 2 decimales.
- Si pide procedimiento → pasos mínimos, 1 línea cada uno.
- NUNCA narrativa ni markdown.`,

  programacion: `Eres asistente de exámenes de programación. Da SOLO la respuesta exacta.

FORMATO: número) respuesta

REGLAS:
- Output de código → exactamente lo que imprime, con saltos de línea si los hay.
- Errores → tipo exacto (ej: IndexError).
- Complejidad → solo Big-O (ej: O(n log n)).
- Concepto → máx 8 palabras.
- Si pide código → solo el código, sin comentarios.
- Si pide "explica" → 1 oración técnica, máx 15 palabras.
- NUNCA introducciones ni markdown fuera del código.`,

  lectura: `Eres asistente de exámenes de comprensión lectora. Da SOLO la respuesta.

FORMATO: número) respuesta

REGLAS:
- Opción múltiple → texto EXACTO de la opción correcta.
- Idea principal → 1 oración, máx 15 palabras.
- Inferencia → 1 oración directa, máx 15 palabras.
- Vocabulario → solo la palabra o sinónimo.
- Pregunta abierta → máx 2 oraciones sin introducción.
- NUNCA "según el texto", contexto ni markdown.`,

  medicina: `Eres asistente de exámenes de medicina y ciencias de la salud. Da SOLO la respuesta.

FORMATO: número) respuesta

REGLAS:
- Anatomía/fisiología → término o estructura exacta.
- Diagnóstico → nombre clínico exacto de la enfermedad o síndrome.
- Fármaco → nombre genérico + mecanismo en máx 6 palabras si se pide.
- Opción múltiple → texto EXACTO de la opción correcta.
- Si pide "explica el mecanismo" → 1 oración clínica, máx 20 palabras.
- NUNCA introducciones, "se debe a", ni markdown.`,

  quimica: `Eres asistente de exámenes de química. Da SOLO la respuesta exacta.

FORMATO: número) respuesta

REGLAS:
- Fórmulas → notación química estándar (ej: H₂SO₄, NaCl).
- Balanceo → ecuación balanceada completa en una línea.
- Cálculo estequiométrico → solo el valor con unidad (ej: 2.5 mol).
- Nomenclatura → nombre IUPAC exacto o fórmula según lo que pida.
- pH/concentración → resultado con 2 decimales.
- Si pide "explica" → 1 oración, máx 15 palabras.
- NUNCA desarrollo ni markdown.`,

  fisica: `Eres asistente de exámenes de física. Da SOLO el resultado.

FORMATO: número) resultado

REGLAS:
- Cálculo → valor numérico con unidad SI (ej: 9.8 m/s²).
- Fórmula → escríbela directamente (ej: F=ma).
- Ley o principio → nombre exacto + máx 6 palabras si pide definición.
- Vectores → magnitud y dirección si aplica.
- Decimales: máx 2 cifras significativas.
- Si pide "explica" → 1 oración física, máx 15 palabras.
- NUNCA desarrollo de operaciones ni markdown.`,

  historia: `Eres asistente de exámenes de historia. Da SOLO la respuesta.

FORMATO: número) respuesta

REGLAS:
- Fecha → formato exacto pedido (año, década, siglo).
- Personaje → nombre completo si se pide.
- Evento → nombre oficial exacto.
- Opción múltiple → texto EXACTO de la opción correcta.
- Causa/consecuencia → 1 oración directa, máx 15 palabras.
- NUNCA contexto adicional, relatos ni markdown.`,

  ingles: `You are an exam assistant. Answer ONLY with the correct answer.

FORMAT: número) answer

RULES:
- Multiple choice → EXACT text of the correct option.
- Grammar → corrected word or phrase only.
- Fill in the blank → exact word(s) that complete the sentence.
- Vocabulary → exact synonym or definition in max 5 words.
- Translation → direct translation, no alternatives.
- If asked to "explain" → 1 sentence, max 15 words.
- NEVER write introductions, "the answer is", or markdown.`,

  biologia: `Eres asistente de exámenes de biología. Da SOLO la respuesta exacta.

FORMATO: número) respuesta

REGLAS:
- Taxonomía → clasificación exacta pedida (reino, filo, clase, etc.).
- Proceso biológico → nombre técnico exacto (ej: fotosíntesis, mitosis).
- Estructura celular → nombre de la organela o parte exacta.
- Opción múltiple → texto EXACTO de la opción correcta.
- Genética → genotipo/fenotipo en notación estándar (ej: Aa, dominante).
- Si pide "explica" → 1 oración, máx 15 palabras.
- NUNCA descripciones largas ni markdown.`,
};

const SYSTEM_PROMPT_VISION = `Eres un asistente de exámenes. Analiza la imagen y responde TODAS las preguntas visibles.

FORMATO OBLIGATORIO — una línea por pregunta:
  número) respuesta

REGLAS:
- Identifica cada pregunta numerada y respóndela en orden.
- Opción múltiple → texto EXACTO de la opción correcta (no la letra).
- Cálculo → solo el valor final con unidad si aplica.
- Diagrama o gráfica → responde lo que pide, no describas la imagen.
- Si pide "explica" o "describe" → máx 2 oraciones muy directas.
- Si no ves claramente → número) [ilegible]
- NUNCA describas la imagen, des introducción ni uses markdown.`;

function maxTokens(category) {
  switch (category) {
    case "matematicas": return 150;
    case "fisica":      return 150;
    case "quimica":     return 180;
    case "programacion":return 200;
    case "ingles":      return 200;
    case "biologia":    return 200;
    case "medicina":    return 220;
    case "historia":    return 200;
    case "lectura":     return 300;
    case "general":     return 200;
    default:            return 200;
  }
}


// ─── Helper: verificar alias en db2 ──────────────────────────────────────────
async function verificarAlias(alias) {
  const database = initDb2();
  if (!database) throw new Error("No se pudo conectar a la base de datos.");

  const snap = await database.collection(USERS_COLLECTION).doc(alias.toLowerCase()).get();

  if (!snap.exists) return { ok: false, status: 404, error: "Alias no encontrado. Contacta al administrador." };

  const data = snap.data();

  if (data.suspended === true) return { ok: false, status: 403, error: "Cuenta suspendida." };

  const credits = data.credits ?? data.creditos ?? null;
  if (typeof credits === "number" && credits <= 0) return { ok: false, status: 402, error: "Sin créditos disponibles." };

  return { ok: true, credits, data };
}

// ─── Helper: descontar crédito en db2 ────────────────────────────────────────
async function descontarCredito(alias) {
  const database = initDb2();
  if (!database) return;

  const snap = await database.collection(USERS_COLLECTION).doc(alias).get();
  const data = snap.data() || {};
  const field = data.credits !== undefined ? "credits" : "creditos";

  await database.collection(USERS_COLLECTION).doc(alias).update({
    [field]: admin.firestore.FieldValue.increment(-1),
    lastQuery: admin.firestore.FieldValue.serverTimestamp(),
    totalQueries: admin.firestore.FieldValue.increment(1),
  });
}

// ─── CLOUD FUNCTION ───────────────────────────────────────────────────────────
const screenaiQuery = onRequest(
  { region: "us-central1", timeoutSeconds: 60, memory: "256MiB", cors: true },
  async (req, res) => {

    if (req.method === "OPTIONS") { res.status(204).send(""); return; }

    // ── GET ?check=1&alias=xxx ────────────────────────────────────────────────
    if (req.method === "GET" && req.query.check === "1") {
      const alias = (req.query.alias || "").trim().toLowerCase();
      if (!alias) { res.status(400).json({ ok: false, error: "Alias requerido." }); return; }

      try {
        const result = await verificarAlias(alias);
        if (!result.ok) { res.status(result.status).json({ ok: false, error: result.error }); return; }
        res.status(200).json({ ok: true, credits: result.credits });
      } catch (e) {
        console.error("[ScreenAI] verificarAlias error:", e.message);
        res.status(500).json({ ok: false, error: "Error al verificar alias: " + e.message });
      }
      return;
    }

    // ── GET ?config=1&alias=xxx — leer configuración del usuario ──────────────
    if (req.method === "GET" && req.query.config === "1") {
      const alias = (req.query.alias || "").trim().toLowerCase();
      if (!alias) { res.status(400).json({ ok: false, error: "Alias requerido." }); return; }

      try {
        const database = initDb2();
        if (!database) { res.status(500).json({ ok: false, error: "No se pudo conectar a la base de datos." }); return; }

        const snap = await database.collection(USERS_COLLECTION).doc(alias).get();
        if (!snap.exists) { res.status(404).json({ ok: false, error: "Alias no encontrado." }); return; }

        const data = snap.data();
        res.status(200).json({
          ok: true,
          provider: data.provider || null,
          category: data.category || null,
          hotkeyToggle: data.hotkeyToggle || null,
          hotkeyQuery: data.hotkeyQuery || null,
        });
      } catch (e) {
        console.error("[ScreenAI] config GET error:", e.message);
        res.status(500).json({ ok: false, error: "Error al leer config: " + e.message });
      }
      return;
    }

    // ── POST ?saveconfig=1 — guardar configuración del usuario ────────────────
    if (req.method === "POST" && req.query.saveconfig === "1") {
const { alias, provider, category, hotkeyToggle, hotkeyQuery } = req.body || {};
if (!alias || !alias.trim()) { res.status(400).json({ ok: false, error: "Alias requerido." }); return; }
try {
  const database = initDb2();
  if (!database) { res.status(500).json({ ok: false, error: "No se pudo conectar a la base de datos." }); return; }
  const updateData = { updatedAt: admin.firestore.FieldValue.serverTimestamp() };
  if (provider  !== undefined)           updateData.provider          = provider;
  if (category  !== undefined)           updateData.category          = category;
  if (hotkeyToggle !== undefined)        updateData.hotkeyToggle      = hotkeyToggle;
  if (hotkeyQuery  !== undefined)        updateData.hotkeyQuery       = hotkeyQuery;

  await database.collection(USERS_COLLECTION).doc(alias.trim().toLowerCase()).update(updateData);

        res.status(200).json({ ok: true });
      } catch (e) {
        console.error("[ScreenAI] saveconfig error:", e.message);
        res.status(500).json({ ok: false, error: "Error al guardar config: " + e.message });
      }
      return;
    }

    // ── POST → consulta IA ────────────────────────────────────────────────────
    if (req.method !== "POST") { res.status(405).json({ ok: false, error: "Método no permitido." }); return; }

    const {
      alias = "",
      mode = "text",
      provider = "gemini-flash",
      category = "general",
      text = "",
      imageBase64 = "",
      mimeType = "image/png",
      textHint = "",
    } = req.body || {};

    if (!alias.trim()) { res.status(401).json({ ok: false, error: "Alias requerido." }); return; }
    if (mode === "text" && !text.trim()) { res.status(400).json({ ok: false, error: "Campo 'text' requerido." }); return; }
    if (mode === "image" && !imageBase64.trim()) { res.status(400).json({ ok: false, error: "Campo 'imageBase64' requerido." }); return; }

    // Verificar alias
    let verification;
    try {
      verification = await verificarAlias(alias);
      if (!verification.ok) { res.status(verification.status).json({ ok: false, error: verification.error }); return; }
    } catch (e) {
      res.status(500).json({ ok: false, error: "Error al verificar cuenta: " + e.message }); return;
    }

    // Seleccionar modelo
    const modelInfo = MODEL_MAP[provider];
    if (!modelInfo) { res.status(400).json({ ok: false, error: `Proveedor desconocido: ${provider}` }); return; }

    // Keys desde env
    const GEMINI_KEY = process.env.PIRVATE_KEY_GEMINI_APITRABAJO ;
    const OPENAI_KEY = process.env.PIRVATE_KEY_OPENIA_APITRABAJO;
    const CLAUDE_KEY = process.env.ANTHROPIC_API_KEY || process.env.CLAUDE_API_KEY || "";

    // Llamar IA
    let answer = "";
    try {
      const systemPrompt = SYSTEM_PROMPTS[category] || SYSTEM_PROMPTS.general;
      const tokens = maxTokens(category);

      if (mode === "text") {
        switch (modelInfo.family) {
          case "gemini": answer = await callGeminiText(text, GEMINI_KEY, modelInfo.endpoint, systemPrompt, tokens); break;
          case "openai": answer = await callOpenAIText(text, OPENAI_KEY, modelInfo.model, systemPrompt, tokens); break;
          case "claude": answer = await callClaudeText(text, CLAUDE_KEY, modelInfo.model, systemPrompt, tokens); break;
        }
      } else {
        switch (modelInfo.family) {
          case "gemini": answer = await callGeminiVision(imageBase64, mimeType, textHint, GEMINI_KEY, modelInfo.endpoint); break;
          case "openai": answer = await callOpenAIVision(imageBase64, mimeType, textHint, OPENAI_KEY, modelInfo.model); break;
          case "claude": answer = await callClaudeVision(imageBase64, mimeType, textHint, CLAUDE_KEY, modelInfo.model); break;
        }
      }

      // Descontar crédito
      try {
        await descontarCredito(alias.toLowerCase());
      } catch (e) {
        console.warn("[ScreenAI] No se pudo descontar crédito:", e.message);
      }

      res.status(200).json({ ok: true, answer });

    } catch (aiErr) {
      console.error("[ScreenAI] AI error:", aiErr.message);
      res.status(502).json({ ok: false, error: aiErr.message });
    }
  }
);

// ─── GEMINI TEXT ──────────────────────────────────────────────────────────────
async function callGeminiText(text, apiKey, endpoint, systemPrompt, tokens) {
  const { data } = await axios.post(`${endpoint}?key=${apiKey}`, {
    system_instruction: { parts: [{ text: systemPrompt }] },
    contents: [{ role: "user", parts: [{ text }] }],
    generationConfig: { temperature: 0.0, maxOutputTokens: tokens },
  }, { headers: { "Content-Type": "application/json" }, timeout: 30000 });
  return data?.candidates?.[0]?.content?.parts?.[0]?.text?.trim() ?? "";
}

async function callGeminiVision(imageBase64, mimeType, textHint, apiKey, endpoint) {
  const { data } = await axios.post(`${endpoint}?key=${apiKey}`, {
    system_instruction: { parts: [{ text: SYSTEM_PROMPT_VISION }] },
    contents: [{
      role: "user", parts: [
        { inline_data: { mime_type: mimeType, data: imageBase64 } },
        { text: "Responde TODAS las preguntas." + (textHint ? `\nContexto: ${textHint}` : "") },
      ]
    }],
    generationConfig: { temperature: 0.2, maxOutputTokens: 2048 },
  }, { headers: { "Content-Type": "application/json" }, timeout: 45000 });
  return data?.candidates?.[0]?.content?.parts?.[0]?.text?.trim() ?? "Sin respuesta.";
}

// ─── OPENAI ───────────────────────────────────────────────────────────────────
async function callOpenAIText(text, apiKey, model, systemPrompt, tokens) {
  const { data } = await axios.post(OPENAI_URL, {
    model, messages: [{ role: "system", content: systemPrompt }, { role: "user", content: text }],
    max_tokens: tokens, temperature: 0.0,
  }, { headers: { "Content-Type": "application/json", "Authorization": `Bearer ${apiKey}` }, timeout: 30000 });
  return data?.choices?.[0]?.message?.content?.trim() ?? "";
}

async function callOpenAIVision(imageBase64, mimeType, textHint, apiKey, model) {
  const { data } = await axios.post(OPENAI_URL, {
    model,
    messages: [
      { role: "system", content: SYSTEM_PROMPT_VISION },
      {
        role: "user", content: [
          { type: "image_url", image_url: { url: `data:${mimeType};base64,${imageBase64}` } },
          { type: "text", text: "Responde todas las preguntas." + (textHint ? `\nContexto: ${textHint}` : "") },
        ]
      },
    ],
    max_tokens: 2048, temperature: 0.2,
  }, { headers: { "Content-Type": "application/json", "Authorization": `Bearer ${apiKey}` }, timeout: 45000 });
  return data?.choices?.[0]?.message?.content?.trim() ?? "Sin respuesta.";
}

// ─── CLAUDE ───────────────────────────────────────────────────────────────────
async function callClaudeText(text, apiKey, model, systemPrompt, tokens) {
  const { data } = await axios.post(CLAUDE_URL, {
    model, max_tokens: tokens, system: systemPrompt,
    messages: [{ role: "user", content: text }],
  }, { headers: { "Content-Type": "application/json", "x-api-key": apiKey, "anthropic-version": CLAUDE_VERSION }, timeout: 30000 });
  return data?.content?.find(b => b.type === "text")?.text?.trim() ?? "";
}

async function callClaudeVision(imageBase64, mimeType, textHint, apiKey, model) {
  const { data } = await axios.post(CLAUDE_URL, {
    model, max_tokens: 2048, system: SYSTEM_PROMPT_VISION,
    messages: [{
      role: "user", content: [
        { type: "image", source: { type: "base64", media_type: mimeType, data: imageBase64 } },
        { type: "text", text: "Responde todas las preguntas." + (textHint ? `\nContexto: ${textHint}` : "") },
      ]
    }],
  }, { headers: { "Content-Type": "application/json", "x-api-key": apiKey, "anthropic-version": CLAUDE_VERSION }, timeout: 45000 });
  return data?.content?.find(b => b.type === "text")?.text?.trim() ?? "Sin respuesta.";
}

const getUserData = onRequest(
  { region: "us-central1", timeoutSeconds: 20, memory: "256MiB", cors: true },
  async (req, res) => {
    if (req.method === "OPTIONS") { res.status(204).send(""); return; }
    if (req.method !== "GET" && req.method !== "POST") {
      res.status(405).json({ ok: false, error: "Método no permitido." });
      return;
    }

    const alias = (
      req.method === "GET"
        ? req.query.alias
        : (req.body || {}).alias
    )?.toString().trim().toLowerCase();

    if (!alias) { res.status(400).json({ ok: false, error: "Alias requerido." }); return; }

    try {
      const database = initDb2();
      if (!database) { res.status(500).json({ ok: false, error: "No se pudo conectar a la base de datos." }); return; }

      const snap = await database.collection(USERS_COLLECTION).doc(alias).get();
      if (!snap.exists) { res.status(404).json({ ok: false, error: "Alias no encontrado." }); return; }

      const data = snap.data();

      // Normaliza timestamps de Firestore a ISO string para que sean serializables
      const clean = {};
      for (const [key, value] of Object.entries(data)) {
        clean[key] = value?.toDate ? value.toDate().toISOString() : value;
      }

      res.status(200).json({ ok: true, alias, data: clean });
    } catch (e) {
      console.error("[getUserData] error:", e.message);
      res.status(500).json({ ok: false, error: "Error al obtener datos: " + e.message });
    }
  }
);

module.exports = { screenaiQuery, getUserData };