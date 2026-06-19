/**
 * screenaiQuery.js — Cloud Function v2
 * Usa db2 (segunda app de Firebase) para buscar usuarios en /trabajos_ia/{alias}
 */

"use strict";

const { onRequest } = require("firebase-functions/v2/https");
const admin = require("firebase-admin");
const axios = require("axios");

const GEMINI_FLASH_URL =
  "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent";
const GEMINI_PRO_URL =
  "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-pro:generateContent";
const OPENAI_URL = "https://api.openai.com/v1/chat/completions";

const USERS_COLLECTION = "trabajos_ia";
// ─── MODEL MAP ────────────────────────────────────────────────────────────────
const MODEL_MAP = {
  "gemini-flash": { family: "gemini", endpoint: GEMINI_FLASH_URL },
  "gemini-pro": { family: "gemini", endpoint: GEMINI_PRO_URL },
  "gpt-4o": { family: "openai", model: "gpt-4o" },
  "gpt-4o-mini": { family: "openai", model: "gpt-4o-mini" },
  "gpt4o-mini": { family: "openai", model: "gpt-4o-mini" }, // ← alias
  gpt4o: { family: "openai", model: "gpt-4o" }, // ← alias
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

// ─── Modelos ──────────────────────────────────────────────────────────────────
const SYSTEM_PROMPTS = {
  general: `Eres un asistente de exámenes. Responde SOLO con las respuestas, sin explicar.

FORMATO OBLIGATORIO — una línea por pregunta:
  número) respuesta

REGLAS:
- Opción múltiple → devuelve el texto COMPLETO de la opción correcta.
- NUNCA respondas únicamente con letras (A, B, C, D, E).
- Si existen alternativas, copia el contenido exacto de la respuesta correcta.
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
- Opción múltiple → devuelve el texto COMPLETO de la opción correcta.
- NUNCA respondas únicamente con letras (A, B, C, D, E).
- Si existen alternativas, copia el contenido exacto de la respuesta correcta.
- Cálculo → solo el valor final con unidad si aplica.
- Diagrama o gráfica → responde lo que pide, no describas la imagen.
- Si pide "explica" o "describe" → máx 2 oraciones muy directas.
- Si no ves claramente → número) [ilegible]
- NUNCA describas la imagen, des introducción ni uses markdown.`;

function maxTokens(category) {
  switch (category) {
    case "matematicas":
      return 400;
    case "fisica":
      return 400;
    case "quimica":
      return 450;
    case "programacion":
      return 500;
    case "ingles":
      return 500;
    case "biologia":
      return 500;
    case "medicina":
      return 550;
    case "historia":
      return 500;
    case "lectura":
      return 600;
    case "general":
      return 500;
    default:
      return 500;
  }
}

// ─── Helper: verificar alias en db2 ──────────────────────────────────────────
async function verificarAlias(alias) {
  const database = initDb2();
  if (!database) throw new Error("No se pudo conectar a la base de datos.");

  const snap = await database
    .collection(USERS_COLLECTION)
    .doc(alias.toLowerCase())
    .get();

  if (!snap.exists)
    return {
      ok: false,
      status: 404,
      error: "Alias no encontrado. Contacta al administrador.",
    };

  const data = snap.data();

  if (data.suspended === true)
    return { ok: false, status: 403, error: "Cuenta suspendida." };

  const credits = data.credits ?? data.creditos ?? null;
  if (typeof credits === "number" && credits <= 0)
    return { ok: false, status: 402, error: "Sin créditos disponibles." };

  return { ok: true, credits, data };
}

// ─── Helper: descontar crédito en db2 ────────────────────────────────────────
async function descontarCredito(alias) {
  const database = initDb2();
  if (!database) return;

  const snap = await database.collection(USERS_COLLECTION).doc(alias).get();
  const data = snap.data() || {};
  const field = data.credits !== undefined ? "credits" : "creditos";

  await database
    .collection(USERS_COLLECTION)
    .doc(alias)
    .update({
      [field]: admin.firestore.FieldValue.increment(-1),
      lastQuery: admin.firestore.FieldValue.serverTimestamp(),
      totalQueries: admin.firestore.FieldValue.increment(1),
    });
}

// ─── CLOUD FUNCTION ───────────────────────────────────────────────────────────
const screenaiQuery = onRequest(
  { region: "us-central1", timeoutSeconds: 120, memory: "256MiB", cors: true },
  async (req, res) => {
    if (req.method === "OPTIONS") {
      res.status(204).send("");
      return;
    }

    // ── GET ?check=1&alias=xxx ────────────────────────────────────────────────
    if (req.method === "GET" && req.query.check === "1") {
      const alias = (req.query.alias || "").trim().toLowerCase();
      if (!alias) {
        res.status(400).json({ ok: false, error: "Alias requerido." });
        return;
      }

      try {
        const result = await verificarAlias(alias);
        if (!result.ok) {
          res.status(result.status).json({ ok: false, error: result.error });
          return;
        }
        res.status(200).json({ ok: true, credits: result.credits });
      } catch (e) {
        console.error("[ScreenAI] verificarAlias error:", e.message);
        res
          .status(500)
          .json({ ok: false, error: "Error al verificar alias: " + e.message });
      }
      return;
    }

    // ── GET ?config=1&alias=xxx — leer configuración del usuario ──────────────
    if (req.method === "GET" && req.query.config === "1") {
      const alias = (req.query.alias || "").trim().toLowerCase();
      if (!alias) {
        res.status(400).json({ ok: false, error: "Alias requerido." });
        return;
      }

      try {
        const database = initDb2();
        if (!database) {
          res.status(500).json({
            ok: false,
            error: "No se pudo conectar a la base de datos.",
          });
          return;
        }

        const snap = await database
          .collection(USERS_COLLECTION)
          .doc(alias)
          .get();
        if (!snap.exists) {
          res.status(404).json({ ok: false, error: "Alias no encontrado." });
          return;
        }

        const data = snap.data();
        res.status(200).json({
          ok: true,
          provider: data.provider || null,
          category: data.category || null,
          hotkeyToggle: data.hotkeyToggle || null,
          hotkeyQuery: data.hotkeyQuery || null,
          position: data.position || null,
          theme: data.theme || "solid",
          highlightColor: data.highlightColor || "#fad232",
          autoClick: data.autoClick ?? true,
        });
      } catch (e) {
        console.error("[ScreenAI] config GET error:", e.message);
        res
          .status(500)
          .json({ ok: false, error: "Error al leer config: " + e.message });
      }
      return;
    }

    // ── POST ?saveconfig=1 — guardar configuración del usuario ────────────────
    if (req.method === "POST" && req.query.saveconfig === "1") {
      console.log("[saveconfig] body:", JSON.stringify(req.body));
      const {
        alias,
        provider,
        category,
        hotkeyToggle,
        hotkeyQuery,
        position,
        theme,
        highlightColor,
        autoClick,
      } = req.body || {};

      if (!alias || !alias.trim()) {
        res.status(400).json({ ok: false, error: "Alias requerido." });
        return;
      }

      try {
        const database = initDb2();
        if (!database) {
          res.status(500).json({
            ok: false,
            error: "No se pudo conectar a la base de datos.",
          });
          return;
        }

        const updateData = {
          updatedAt: admin.firestore.FieldValue.serverTimestamp(),
        };
        if (provider !== undefined) updateData.provider = provider;
        if (category !== undefined) updateData.category = category;
        if (hotkeyToggle !== undefined) updateData.hotkeyToggle = hotkeyToggle;
        if (hotkeyQuery !== undefined) updateData.hotkeyQuery = hotkeyQuery;
        if (position !== undefined) updateData.position = position;
        if (theme !== undefined) updateData.theme = theme;
        if (highlightColor !== undefined)
          updateData.highlightColor = highlightColor;
        if (autoClick !== undefined) updateData.autoClick = autoClick;

        await database
          .collection(USERS_COLLECTION)
          .doc(alias.trim().toLowerCase())
          .update(updateData);

        res.status(200).json({ ok: true });
      } catch (e) {
        console.error("[ScreenAI] saveconfig error:", e.message);
        res
          .status(500)
          .json({ ok: false, error: "Error al guardar config: " + e.message });
      }
      return;
    }

    // ── POST → consulta IA ────────────────────────────────────────────────────
    if (req.method !== "POST") {
      res.status(405).json({ ok: false, error: "Método no permitido." });
      return;
    }

    const {
      alias: aliasRaw = "",
      mode = "text",
      provider = "gemini-flash",
      category = "general",
      text = "",
      imageBase64 = "",
      mimeType = "image/png",
      textHint = "",
    } = req.body || {};
    console.log("[ScreenAI] POST body:", aliasRaw);
    const alias = String(aliasRaw ?? "").trim();
    if (!alias) {
      res.status(401).json({ ok: false, error: "Alias requerido." });
      return;
    }
    if (mode === "text" && !text.trim()) {
      res.status(400).json({ ok: false, error: "Campo 'text' requerido." });
      return;
    }
    if (mode === "image" && !imageBase64.trim()) {
      res
        .status(400)
        .json({ ok: false, error: "Campo 'imageBase64' requerido." });
      return;
    }

    // Verificar alias
    let verification;
    try {
      verification = await verificarAlias(alias);
      if (!verification.ok) {
        res
          .status(verification.status)
          .json({ ok: false, error: verification.error });
        return;
      }
    } catch (e) {
      res
        .status(500)
        .json({ ok: false, error: "Error al verificar cuenta: " + e.message });
      return;
    }

    // Seleccionar modelo
    const modelInfo = MODEL_MAP[provider];
    if (!modelInfo) {
      res
        .status(400)
        .json({ ok: false, error: `Proveedor desconocido: ${provider}` });
      return;
    }

    // Keys desde env
    const GEMINI_KEY = process.env.PIRVATE_KEY_GEMINI_APITRABAJO;
    const OPENAI_KEY = process.env.PIRVATE_KEY_OPENIA_APITRABAJO;
    const CLAUDE_KEY =
      process.env.ANTHROPIC_API_KEY || process.env.CLAUDE_API_KEY || "";

    // Llamar IA
    let answer = "";
    try {
      const systemPrompt = SYSTEM_PROMPTS[category] || SYSTEM_PROMPTS.general;
      const tokens = maxTokens(category);

      if (mode === "text") {
        switch (modelInfo.family) {
          case "gemini":
            answer = await callGeminiText(
              text,
              GEMINI_KEY,
              modelInfo.endpoint,
              systemPrompt,
              tokens,
            );
            break;
          case "openai":
            answer = await callOpenAIText(
              text,
              OPENAI_KEY,
              modelInfo.model,
              systemPrompt,
              tokens,
            );
            break;
        }
      } else {
        switch (modelInfo.family) {
          case "gemini":
            answer = await callGeminiVision(
              imageBase64,
              mimeType,
              textHint,
              GEMINI_KEY,
              modelInfo.endpoint,
            );
            break;
          case "openai":
            answer = await callOpenAIVision(
              imageBase64,
              mimeType,
              textHint,
              OPENAI_KEY,
              modelInfo.model,
            );
            break;
        }
      }

      // Solo descuenta si la IA devolvió algo
      if (!answer || !answer.trim()) {
        res
          .status(502)
          .json({ ok: false, error: "La IA no devolvió respuesta." });
        return;
      }

      // DESPUÉS (correcto):
      try {
        const costo = await obtenerCostoDesdeDB(provider, mode);
        const costoCategoria = await obtenerCostoCategoria(category, mode);
        const costoTotal = costo + costoCategoria;

        const { antes, despues } = await descontarCreditoN(
          alias.toLowerCase(),
          costoTotal,
        );
        await guardarHistorial(
          alias.toLowerCase(),
          provider,
          category,
          mode,
          antes,
          despues,
          costoTotal,
        );
      } catch (e) {
        console.warn("[ScreenAI] No se pudo descontar crédito:", e.message);
      }

      res.status(200).json({ ok: true, answer });
    } catch (aiErr) {
      console.error("[ScreenAI] AI error:", aiErr.message);
      console.error(
        "[ScreenAI] AI response data:",
        JSON.stringify(aiErr.response?.data),
      );
      res.status(502).json({ ok: false, error: aiErr.message });
    }
  },
);

// ─── GEMINI TEXT ──────────────────────────────────────────────────────────────
async function callGeminiText(text, apiKey, endpoint, systemPrompt, tokens) {
  const isPro = endpoint.includes("2.5-pro");

  const userText = isPro
    ? `${text}\n\nIMPORTANTE: Si hay opciones (A, B, C, D), escribe SOLO el contenido de la opción correcta, NUNCA la letra sola.`
    : text;

  const { data } = await axios.post(
    `${endpoint}?key=${apiKey}`,
    {
      system_instruction: { parts: [{ text: systemPrompt }] },
      contents: [{ role: "user", parts: [{ text: userText }] }],
      generationConfig: {
        temperature: 0.0,
        maxOutputTokens: tokens,
        ...(isPro && { thinkingConfig: { thinkingBudget: 512 } }),
      },
    },
    {
      headers: { "Content-Type": "application/json" },
      timeout: isPro ? 60000 : 30000,
    },
  );
  return (
    data?.candidates?.[0]?.content?.parts
      ?.find((p) => p.text && !p.thought)
      ?.text?.trim() ?? ""
  );
}
async function callGeminiVision(
  imageBase64,
  mimeType,
  textHint,
  apiKey,
  endpoint,
) {
  const { data } = await axios.post(
    `${endpoint}?key=${apiKey}`,
    {
      system_instruction: { parts: [{ text: SYSTEM_PROMPT_VISION }] },
      contents: [
        {
          role: "user",
          parts: [
            { inline_data: { mime_type: mimeType, data: imageBase64 } },
            {
              text:
                "Responde TODAS las preguntas." +
                (textHint ? `\nContexto: ${textHint}` : ""),
            },
          ],
        },
      ],
      generationConfig: { temperature: 0.2, maxOutputTokens: 2048 },
    },
    { headers: { "Content-Type": "application/json" }, timeout: 45000 },
  );
  return (
    data?.candidates?.[0]?.content?.parts?.[0]?.text?.trim() ?? "Sin respuesta."
  );
}

// ─── OPENAI ───────────────────────────────────────────────────────────────────
async function callOpenAIText(text, apiKey, model, systemPrompt, tokens) {
  const { data } = await axios.post(
    OPENAI_URL,
    {
      model,
      messages: [
        { role: "system", content: systemPrompt },
        { role: "user", content: text },
      ],
      max_tokens: tokens,
      temperature: 0.0,
    },
    {
      headers: {
        "Content-Type": "application/json",
        Authorization: `Bearer ${apiKey}`,
      },
      timeout: 30000,
    },
  );
  return data?.choices?.[0]?.message?.content?.trim() ?? "";
}

async function callOpenAIVision(
  imageBase64,
  mimeType,
  textHint,
  apiKey,
  model,
) {
  const { data } = await axios.post(
    OPENAI_URL,
    {
      model,
      messages: [
        { role: "system", content: SYSTEM_PROMPT_VISION },
        {
          role: "user",
          content: [
            {
              type: "image_url",
              image_url: { url: `data:${mimeType};base64,${imageBase64}` },
            },
            {
              type: "text",
              text:
                "Responde todas las preguntas." +
                (textHint ? `\nContexto: ${textHint}` : ""),
            },
          ],
        },
      ],
      max_tokens: 2048,
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
  return data?.choices?.[0]?.message?.content?.trim() ?? "Sin respuesta.";
}

async function obtenerCostoCategoria(category, mode) {
  try {
    const ahora = Date.now();
    if (!preciosCache || ahora - preciosCacheTime > CACHE_TTL) {
      const snap = await db2
        .collection("precio_apartado")
        .doc("scag_site")
        .get();
      preciosCache = snap.exists ? snap.data() : {};
      preciosCacheTime = ahora;
    }

    const CATEGORY_KEY_MAP = {
      general: "General",
      matematicas: "Matemáticas",
      fisica: "Física",
      quimica: "Química",
      biologia: "Biología",
      medicina: "Medicina",
      programacion: "Programación",
      lectura: "Lectura",
      historia: "Historia",
      ingles: "Inglés",
    };

    const key = CATEGORY_KEY_MAP[category];
    if (!key) return 0;

    const costo = preciosCache?.categoria?.[key];
    return typeof costo === "number" ? costo : 0;
  } catch (e) {
    console.warn("[ScreenAI] No se pudo leer costo categoría:", e.message);
    return 0;
  }
}

async function descontarCreditoN(alias, n) {
  const database = initDb2();
  if (!database) return { antes: null, despues: null };

  const snap = await database.collection(USERS_COLLECTION).doc(alias).get();
  const data = snap.data() || {};
  const field = data.credits !== undefined ? "credits" : "creditos";
  const creditosAntes = data[field] ?? 0;
  const creditosDespues = creditosAntes - n;

  await database
    .collection(USERS_COLLECTION)
    .doc(alias)
    .update({
      [field]: admin.firestore.FieldValue.increment(-n),
      lastQuery: admin.firestore.FieldValue.serverTimestamp(),
      totalQueries: admin.firestore.FieldValue.increment(1),
    });

  return { antes: creditosAntes, despues: creditosDespues };
}

async function guardarHistorial(
  alias,
  provider,
  category,
  mode,
  creditosAntes,
  creditosDespues,
  costoTotal,
) {
  const database = initDb2();
  if (!database) return;

  try {
    await database
      .collection(USERS_COLLECTION)
      .doc(alias)
      .collection("historial")
      .add({
        modelo: provider,
        categoria: category,
        tipo: mode === "image" ? "Captura de pantalla" : "Selección de texto",
        creditosConsumidos: costoTotal,
        costoSoles: parseFloat((costoTotal * 0.1).toFixed(2)),
        creditosAntes,
        creditosRestantes: creditosDespues,
        fecha: admin.firestore.FieldValue.serverTimestamp(),
      });
  } catch (e) {
    console.warn("[ScreenAI] No se pudo guardar historial:", e.message);
  }
}

let preciosCache = null;
let preciosCacheTime = 0;
const CACHE_TTL = 5 * 60 * 1000; // 5 minutos

async function obtenerCostoDesdeDB(provider, mode) {
  try {
    const ahora = Date.now();
    if (!preciosCache || ahora - preciosCacheTime > CACHE_TTL) {
      const snap = await db2
        .collection("precio_apartado")
        .doc("scag_site")
        .get();
      preciosCache = snap.exists ? snap.data() : {};
      preciosCacheTime = ahora;
    }

    const modeKey = mode === "image" ? "captura" : "select";
    const PROVIDER_KEY_MAP = {
      "gemini-flash": { group: "gemini_google", key: `2.5flash_${modeKey}` },
      "gemini-pro": { group: "gemini_google", key: `2.5pro_${modeKey}` },
      "gpt-4o": { group: "gpt_openIA", key: `gpt4o_${modeKey}` },
      "gpt-4o-mini": { group: "gpt_openIA", key: `gpt4o_mini_${modeKey}` },
    };

    const map = PROVIDER_KEY_MAP[provider];
    if (!map) return 1;

    const costo = preciosCache?.[map.group]?.[map.key];
    return typeof costo === "number" ? costo : 1;
  } catch (e) {
    console.warn("[ScreenAI] No se pudo leer precio desde DB:", e.message);
    return 1;
  }
}

async function descontarCredito(alias) {
  const database = initDb2();
  if (!database) return { antes: null, despues: null };

  const snap = await database.collection(USERS_COLLECTION).doc(alias).get();
  const data = snap.data() || {};
  const field = data.credits !== undefined ? "credits" : "creditos";
  const creditosAntes = data[field] ?? 0;
  const creditosDespues = creditosAntes - 1;

  await database
    .collection(USERS_COLLECTION)
    .doc(alias)
    .update({
      [field]: admin.firestore.FieldValue.increment(-1),
      lastQuery: admin.firestore.FieldValue.serverTimestamp(),
      totalQueries: admin.firestore.FieldValue.increment(1),
    });

  return { antes: creditosAntes, despues: creditosDespues };
}

const suggestConfig = onRequest(
  { region: "us-central1", timeoutSeconds: 10, memory: "128MiB", cors: true },
  async (req, res) => {
    if (req.method === "OPTIONS") {
      res.status(204).send("");
      return;
    }
    if (req.method !== "POST") {
      res.status(405).json({ ok: false, error: "Método no permitido." });
      return;
    }

    const { prompt } = req.body || {};
    if (!prompt?.trim()) {
      res.status(400).json({ ok: false, error: "Prompt requerido." });
      return;
    }

    const GEMINI_KEY = process.env.PIRVATE_KEY_GEMINI_APITRABAJO;
    if (!GEMINI_KEY) {
      res.status(500).json({ ok: false, error: "API key no configurada." });
      return;
    }

    // ── Normalización ──
    const p = prompt
      .toLowerCase()
      .normalize("NFD")
      .replace(/[\u0300-\u036f]/g, "")
      .replace(/[¿¡]/g, "");

    // ── Textos explicativos por categoría ──
    const explanations = {
      fisica:
        "Seleccioné Gemini Pro porque tu tarea involucra física, ideal para razonamiento científico y cálculo. La categoría Física activa prompts especializados en fórmulas y teoremas.",
      matematicas:
        "Gemini Pro es el más preciso para matemáticas: maneja cálculo, álgebra y ecuaciones con alta exactitud. La categoría Matemáticas enfoca las respuestas en resolución paso a paso.",
      quimica:
        "Tu consulta es de química, donde Gemini Pro destaca en nomenclatura, balanceo y estequiometría. La categoría Química optimiza las respuestas con notación y fórmulas correctas.",
      programacion:
        "Gemini Pro es ideal para programación: entiende código, algoritmos y depuración en múltiples lenguajes. La categoría Programación prioriza respuestas con ejemplos de código.",
      biologia:
        "GPT-4o tiene mayor precisión en biología celular, genética y ecosistemas. La categoría Biología activa contexto especializado en ciencias de la vida.",
      medicina:
        "GPT-4o es riguroso para medicina clínica y farmacología. La categoría Medicina enfoca las respuestas con criterio clínico y terminología médica precisa.",
      lectura:
        "Gemini Flash es veloz y preciso para comprensión lectora, resúmenes y redacción. La categoría Lectura optimiza el análisis de textos y la coherencia argumentativa.",
      historia:
        "Gemini Flash responde rápido y con precisión en historia, geografía y ciencias sociales. La categoría Historia activa contexto cronológico y análisis de eventos.",
      ingles:
        "Gemini Flash es eficiente para inglés: gramática, traducción y comprensión lectora. La categoría Inglés enfoca las respuestas en corrección idiomática y vocabulario.",
      general:
        "Gemini Flash es el modelo más equilibrado para consultas generales. La categoría General permite respuestas versátiles sin restricción temática.",
    };

    const localMap = [
      {
        model: "gemini-pro",
        cat: "fisica",
        keys: [
          "fisica",
          "cuantica",
          "quantum",
          "relatividad",
          "termodinamica",
          "cinematica",
          "dinamica",
          "estatica",
          "electromagnetismo",
          "magnetismo",
          "electrostatica",
          "optica",
          "ondas",
          "calor",
          "energia",
          "trabajo mecanico",
          "momento lineal",
          "momento angular",
          "ley de newton",
          "caida libre",
          "movimiento parabolico",
          "mrua",
          "mru",
          "fuerza",
          "potencia",
          "presion",
          "fluidos",
          "viscosidad",
          "campo electrico",
          "campo magnetico",
          "circuito",
          "ley de ohm",
          "capacitor",
          "resistencia electrica",
          "fisica 1",
          "fisica 2",
          "fisica 3",
          "lab de fisica",
          "informe de fisica",
          "practica de fisica",
        ],
      },
      {
        model: "gemini-pro",
        cat: "matematicas",
        keys: [
          "matematica",
          "calculo",
          "integral",
          "derivada",
          "algebra",
          "estadistica",
          "probabilidad",
          "geometria",
          "trigonometria",
          "logaritmo",
          "ecuacion",
          "matriz",
          "determinante",
          "vectores",
          "limites",
          "series",
          "sucesiones",
          "funcion",
          "polinomio",
          "inecuacion",
          "conjunto",
          "combinatoria",
          "permutacion",
          "binomio",
          "calculo 1",
          "calculo 2",
          "algebra lineal",
          "matematica discreta",
          "matematica basica",
          "pre calculo",
          "raiz cuadrada",
          "regla de tres",
          "porcentaje",
          "fraccion",
          "numero complejo",
          "espacio vectorial",
          "transformada",
          "laplace",
          "fourier",
          "ecuacion diferencial",
          "variable aleatoria",
          "distribucion normal",
          "chi cuadrado",
          "lab de matematica",
          "ejercicio de mat",
          "practica de mat",
          "examen de mat",
        ],
      },
      {
        model: "gemini-pro",
        cat: "quimica",
        keys: [
          "quimica",
          "organica",
          "inorganica",
          "estequiometria",
          "tabla periodica",
          "enlace quimico",
          "mol ",
          "moles",
          "reaccion quimica",
          "acido",
          "base",
          "ph ",
          "oxidacion",
          "reduccion",
          "redox",
          "hidrocarburo",
          "alcohol",
          "cetona",
          "aldehido",
          "ester",
          "eter",
          "amina",
          "amida",
          "nomenclatura quimica",
          "formula quimica",
          "balancear",
          "concentracion",
          "molaridad",
          "molalidad",
          "gas ideal",
          "ley de boyle",
          "ley de charles",
          "termodinamica quimica",
          "entalpia",
          "entropia",
          "cinetica quimica",
          "equilibrio quimico",
          "quimica 1",
          "quimica 2",
          "lab de quimica",
          "informe de quimica",
          "practica de quimica",
        ],
      },
      {
        model: "gemini-pro",
        cat: "programacion",
        keys: [
          "programacion",
          "programar",
          "algoritmo",
          "codigo",
          "codificar",
          "debug",
          "depurar",
          "software",
          "desarrollo web",
          "desarrollo movil",
          "aplicacion",
          "app ",
          "base de datos",
          "java ",
          "javascript",
          "python ",
          "c++",
          "c# ",
          "php ",
          "ruby ",
          "swift ",
          "kotlin ",
          "typescript",
          "html ",
          "css ",
          "sql ",
          "mysql",
          "postgresql",
          "mongodb",
          "firebase",
          "django",
          "flask",
          "spring",
          "react ",
          "angular ",
          "vue ",
          "nodejs",
          "express ",
          "rest api",
          " api ",
          "json ",
          "xml ",
          "backend",
          "frontend",
          "fullstack",
          "poo",
          "programacion orientada",
          "clase ",
          "objeto ",
          "herencia",
          "polimorfismo",
          "encapsulamiento",
          "funcion ",
          "metodo ",
          "variable ",
          "arreglo ",
          "array ",
          "lista ",
          "pila ",
          "cola ",
          "grafo ",
          "arbol ",
          "recursion",
          "compilar",
          "interprete",
          "pseudocodigo",
          "diagrama de flujo",
          "uml",
          "casos de uso",
          "arquitectura de software",
          "patron de diseno",
          "mvc ",
          "microservicio",
          "docker",
          "git ",
          "github",
          "linux",
          "bash ",
          "practica de prog",
          "examen de prog",
          "proyecto de sistemas",
          "tesis de sistemas",
          "senati prog",
          "computacion",
          "informatica",
          "ing de sistemas",
          "ciencias de la computacion",
        ],
      },
      {
        model: "gpt-4o",
        cat: "biologia",
        keys: [
          "biologia",
          "genetica",
          "adn",
          "arn",
          "celula",
          "eucariota",
          "procariota",
          "mitosis",
          "meiosis",
          "evolucion",
          "ecosistema",
          "fotosintesis",
          "respiracion celular",
          "metabolismo",
          "proteina",
          "lipido",
          "carbohidrato",
          "enzima",
          "hormona",
          "sistema nervioso",
          "sistema inmune",
          "microbiologia",
          "bacterias",
          "virus",
          "parasito",
          "hongo",
          "biotecnologia",
          "clonacion",
          "secuenciacion",
          "bioquimica",
          "membrana celular",
          "organelo",
          "cloroplasto",
          "mitocondria",
          "ecologia",
          "cadena alimenticia",
          "bioma",
          "biologia 1",
          "biologia 2",
          "lab de biologia",
          "informe de biologia",
          "practica de biologia",
        ],
      },
      {
        model: "gpt-4o",
        cat: "medicina",
        keys: [
          "medicina",
          "diagnostico",
          "sintoma",
          "farmaco",
          "farmacologia",
          "patologia",
          "anatomia",
          "fisiologia",
          "histologia",
          "embriologia",
          "semiologia",
          "propedeutica",
          "clinica",
          "cirugia",
          "enfermedad",
          "tratamiento",
          "dosis",
          "medicamento",
          "antibiotico",
          "vacuna",
          "inmunologia",
          "cardiologia",
          "neurologia",
          "oncologia",
          "pediatria",
          "ginecologia",
          "obstetricia",
          "traumatologia",
          "ortopedia",
          "dermatologia",
          "oftalmologia",
          "otorrinolaringologia",
          "urologia",
          "nefrologia",
          "hepatologia",
          "gastroenterologia",
          "endocrinologia",
          "psiquiatria",
          "salud publica",
          "epidemiologia",
          "bioestadistica",
          "historia clinica",
          "anamnesis",
          "examen fisico",
          "diagnostico diferencial",
          "medicina 1",
          "medicina 2",
          "internado",
          "serums",
          "upch",
          "unmsm medicina",
          "san marcos medicina",
          "cayetano",
        ],
      },
      {
        model: "gemini-flash",
        cat: "lectura",
        keys: [
          "lectura",
          "literatura",
          "comprension lectora",
          "resumen de",
          "analiza este texto",
          "analiza el texto",
          "redaccion",
          "ortografia",
          "lenguaje",
          "comunicacion",
          "ensayo",
          "parrafo",
          "introduccion conclusion",
          "tesis de grado",
          "monografia",
          "informe escrito",
          "abstract",
          "marco teorico",
          "antecedentes",
          "justificacion",
          "hipotesis",
          "metodologia",
          "conclusion de",
          "obra literaria",
          "novela",
          "cuento",
          "poema",
          "autor",
          "personaje",
          "narrativa",
          "dramaturgia",
          "retorica",
          "argumentacion",
          "debate",
          "exposicion oral",
        ],
      },
      {
        model: "gemini-flash",
        cat: "historia",
        keys: [
          "historia",
          "guerra",
          "revolucion",
          "siglo xix",
          "siglo xx",
          "siglo xxi",
          "imperio",
          "civilizacion",
          "batalla",
          "independencia",
          "colonia",
          "virreinato",
          "republica",
          "conquista",
          "inca",
          "azteca",
          "maya",
          "grecia antigua",
          "roma antigua",
          "edad media",
          "renacimiento",
          "ilustracion",
          "primera guerra",
          "segunda guerra",
          "guerra fria",
          "holocausto",
          "nazismo",
          "fascismo",
          "comunismo",
          "capitalismo",
          "liberalismo",
          "socialismo",
          "geopolitica",
          "geografia",
          "peru historia",
          "historia del peru",
          "historia universal",
          "ciencias sociales",
          "filosofia",
          "logica",
          "etica",
          "epistemologia",
          "kant",
          "platon",
          "aristoteles",
          "socrates",
          "economia",
          "macroeconomia",
          "microeconomia",
          "pib",
          "inflacion",
          "mercado",
        ],
      },
      {
        model: "gemini-flash",
        cat: "ingles",
        keys: [
          "english",
          "translate",
          "grammar",
          "vocabulary",
          "writing in english",
          "essay in english",
          "reading comprehension",
          "listening",
          "speaking",
          "pronunciation",
          "toefl",
          "ielts",
          "past simple",
          "present perfect",
          "future tense",
          "conditional",
          "modal verbs",
          "ingles basico",
          "ingles intermedio",
          "ingles avanzado",
          "nivel b1",
          "nivel b2",
          "nivel c1",
          "phrasal verb",
          "idiom",
          "preposition",
          "article the",
          "passive voice",
          "reported speech",
          "traduci",
          "traduccion",
          "traducir",
        ],
      },
    ];

    // ── Score: gana la categoría con más keywords encontradas ──
    let bestMatch = null;
    let bestScore = 0;

    for (const { keys, model, cat } of localMap) {
      const score = keys.filter((k) => p.includes(k)).length;
      if (score > bestScore) {
        bestScore = score;
        bestMatch = { model, cat };
      }
    }

    if (bestMatch && bestScore >= 1) {
      return res.status(200).json({
        ok: true,
        model: bestMatch.model,
        category: bestMatch.cat,
        text: explanations[bestMatch.cat],
        source: "local",
      });
    }

    // ── Fallback: Gemini solo para casos ambiguos sin keywords ──
    const systemInstruction = `Clasifica la tarea del estudiante. Elige SIEMPRE el más cercano, nunca dejes sin respuesta.
Modelos: gemini-flash(general/lectura/historia/ingles) gemini-pro(matematicas/fisica/quimica/programacion) gpt-4o(biologia/medicina) gpt-4o-mini(simple)
Categorias: general matematicas fisica quimica biologia medicina programacion lectura historia ingles
JSON sin markdown: {"model":"<val>","category":"<val>","text":"<1 sola linea explicando la eleccion>"}`;

    let raw = "";
    try {
      const { data } = await axios.post(
        `${GEMINI_FLASH_URL}?key=${GEMINI_KEY}`,
        {
          system_instruction: { parts: [{ text: systemInstruction }] },
          contents: [{ role: "user", parts: [{ text: prompt.slice(0, 300) }] }],
          generationConfig: { temperature: 0.1, maxOutputTokens: 80 },
        },
        { headers: { "Content-Type": "application/json" }, timeout: 20000 },
      );

      raw = data?.candidates?.[0]?.content?.parts?.[0]?.text?.trim() ?? "";
      if (!raw) throw new Error("Vacío");

      const match = raw.match(/\{[\s\S]*?\}/);
      if (!match) throw new Error("Sin JSON: " + raw);

      const parsed = JSON.parse(match[0]);

      const validModels = [
        "gemini-flash",
        "gemini-pro",
        "gpt-4o",
        "gpt-4o-mini",
      ];
      const validCategories = [
        "general",
        "matematicas",
        "fisica",
        "quimica",
        "biologia",
        "medicina",
        "programacion",
        "lectura",
        "historia",
        "ingles",
      ];

      if (!validModels.includes(parsed.model)) parsed.model = "gemini-flash";
      if (!validCategories.includes(parsed.category))
        parsed.category = "general";
      if (!parsed.text) parsed.text = explanations["general"];

      console.log("[suggestConfig] Gemini clasificó:", {
        model: parsed.model,
        category: parsed.category,
      });

      res.status(200).json({
        ok: true,
        model: parsed.model,
        category: parsed.category,
        text: parsed.text.slice(0, 180),
        source: "ai",
      });
    } catch (e) {
      console.error("[suggestConfig] ERROR:", e.message, "| raw:", raw);
      res.status(200).json({
        ok: true,
        model: "gemini-flash",
        category: "general",
        text: explanations["general"],
        source: "fallback",
      });
    }
  },
);

const getUserData = onRequest(
  { region: "us-central1", timeoutSeconds: 20, memory: "256MiB", cors: true },
  async (req, res) => {
    if (req.method === "OPTIONS") {
      res.status(204).send("");
      return;
    }
    if (req.method !== "GET" && req.method !== "POST") {
      res.status(405).json({ ok: false, error: "Método no permitido." });
      return;
    }

    const alias = (
      req.method === "GET" ? req.query.alias : (req.body || {}).alias
    )
      ?.toString()
      .trim()
      .toLowerCase();

    if (!alias) {
      res.status(400).json({ ok: false, error: "Alias requerido." });
      return;
    }

    try {
      const database = initDb2();
      if (!database) {
        res.status(500).json({
          ok: false,
          error: "No se pudo conectar a la base de datos.",
        });
        return;
      }

      const snap = await database.collection(USERS_COLLECTION).doc(alias).get();
      if (!snap.exists) {
        res.status(404).json({ ok: false, error: "Alias no encontrado." });
        return;
      }

      const data = snap.data();
      const clean = {};
      for (const [key, value] of Object.entries(data)) {
        clean[key] = value?.toDate ? value.toDate().toISOString() : value;
      }

      res.status(200).json({ ok: true, alias, data: clean });
    } catch (e) {
      console.error("[getUserData] error:", e.message);
      res
        .status(500)
        .json({ ok: false, error: "Error al obtener datos: " + e.message });
    }
  },
);

module.exports = { screenaiQuery, getUserData, suggestConfig };
