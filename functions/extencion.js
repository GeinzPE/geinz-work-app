"use strict";

const { onRequest } = require("firebase-functions/v2/https");
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

function calcularCostoRealUSD(provider, tokensInput, tokensOutput) {
  const precio = PRECIO_USD_POR_MILLON[provider];
  if (!precio) return 0;
  const costoInput = ((Number(tokensInput) || 0) / 1_000_000) * precio.input;
  const costoOutput = ((Number(tokensOutput) || 0) / 1_000_000) * precio.output;
  return costoInput + costoOutput;
}
let db2 = null;
const CACHE_TTL = 5 * 60 * 1000;

// ── Importación de prompts y tokens ──────────────────────────────────────────

const {
  verificarAlias,
  esRespuestaValida,
  obtenerCostoDesdeDB,
  obtenerCostoCategoria,
  obtenerCostoSolucion,
  descontarCreditoN,
  guardarHistorial,
} = require("./functions_trabajo");
// ── Importación de funciones de negocio (sin las call* que ya están aquí) ────
const {
  SYSTEM_PROMPTS,
  SYSTEM_PROMPTS_DETALLADO,
  SYSTEM_PROMPTS_SUPER_DETALLADO,       // ← agregar
  SYSTEM_PROMPT_VISION,
  SYSTEM_PROMPT_VISION_DETALLADO,
  SYSTEM_PROMPT_VISION_SUPER_DETALLADO, // ← agregar
  maxTokens,
  maxTokens_DETALLADO,
  maxTokens_SUPER_DETALLADO,            // ← agregar
} = require("./modelo_promps_ia");

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
async function callGeminiText(text, apiKey, endpoint, systemPrompt, tokens) {
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
          ...(isPro && { thinkingConfig: { thinkingBudget: 512 } }),
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
) {
  const isPro = endpoint.includes("2.5-pro");
  console.log("=== DEBUG callGeminiVision ===");
  console.log("mimeType:", mimeType);
  console.log("textHint:", textHint);
  console.log("tokens:", tokens);

  // 🛠️ FIX 1: Limpieza automática del prefijo Base64 si el frontend lo envía mal
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
    // 🛠️ FIX 2: Ajuste del prompt del usuario para no contradecir los SYSTEM_PROMPTS
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
          ...(isPro && { thinkingConfig: { thinkingBudget: 512 } }),
        },
      },
      { headers: { "Content-Type": "application/json" }, timeout: 45000 },
    );

    console.log("=== RESPUESTA GEMINI ===");
    const finishReason = data?.candidates?.[0]?.finishReason;
    console.log("finish_reason:", finishReason);

    let answer = data?.candidates?.[0]?.content?.parts?.[0]?.text?.trim();

    // 🛠️ FIX 3: Control de respuestas inválidas o vacías por bloqueos/errores
    if (!answer || answer === "" || answer === "SIN_CONTENIDO") {
      console.warn(
        "⚠️ Gemini retornó una estructura vacía o restrictiva. Activando fallback.",
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
    // Retornamos la estructura limpia para que tu backend no se caiga (crash)
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
// ── Cloud Function principal ──────────────────────────────────────────────────
const screenaiQuery_extencion = onRequest(
  { region: "us-central1", timeoutSeconds: 120, memory: "256MiB", cors: true },
  async (req, res) => {
    console.log(
      `[ScreenAI] Nueva petición recibida. Método: ${req.method}, Query:`,
      JSON.stringify(req.query),
    );

    if (req.method === "OPTIONS") {
      console.log("[ScreenAI] Manejando petición OPTIONS (CORS Preflight)");
      res.status(204).send("");
      return;
    }

    if (req.method === "GET" && req.query.check === "1") {
      const alias = (req.query.alias || "").trim().toLowerCase();
      console.log(`[ScreenAI] Flujo GET check=1 iniciado para alias: "${alias}"`);

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
        console.log(`[ScreenAI] Alias "${alias}" verificado. Créditos: ${result.credits}`);
        res.status(200).json({ ok: true, credits: result.credits });
      } catch (e) {
        console.error("[ScreenAI] verificarAlias error:", e.message);
        res.status(500).json({ ok: false, error: "Error al verificar alias: " + e.message });
      }
      return;
    }

    if (req.method === "GET" && req.query.config === "1") {
      const alias = (req.query.alias || "").trim().toLowerCase();
      console.log(`[ScreenAI] Flujo GET config=1 iniciado para alias: "${alias}"`);

      if (!alias) {
        res.status(400).json({ ok: false, error: "Alias requerido." });
        return;
      }

      try {
        const database = initDb2();
        if (!database) {
          res.status(500).json({ ok: false, error: "No se pudo conectar a la base de datos." });
          return;
        }

        const snap = await database.collection(USERS_COLLECTION).doc(alias).get();
        if (!snap.exists) {
          res.status(404).json({ ok: false, error: "Usuario no encontrado." });
          return;
        }

        const data = snap.data();
        console.log(`[ScreenAI] Config recuperada para "${alias}":`, JSON.stringify(data));
        res.status(200).json({
          ok: true,
          provider: data.provider || null,
          category: data.category || null,
          hotkeyToggle: data.hotkeyToggle || null,
          hotkeyQuery: data.hotkeyQuery || null,
          hotkeyCapture: data.hotkeyCapture || null,
          position: data.position || null,
          theme: data.theme || "solid",
          highlightColor: data.highlightColor || "#fad232",
          solutionMode: data.solutionMode || "detallado",
        });
      } catch (e) {
        console.error("[ScreenAI] config GET error:", e.message);
        res.status(500).json({ ok: false, error: "Error al leer config: " + e.message });
      }
      return;
    }

    if (req.method === "POST" && req.query.saveconfig === "1") {
      console.log("[saveconfig] body:", JSON.stringify(req.body));
      const {
        alias,
        provider,
        category,
        hotkeyToggle,
        hotkeyQuery,
        hotkeyCapture,
        position,
        theme,
        highlightColor,
        solutionMode,
      } = req.body || {};

      if (!alias || !alias.trim()) {
        res.status(400).json({ ok: false, error: "Alias requerido." });
        return;
      }

      try {
        const database = initDb2();
        if (!database) {
          res.status(500).json({ ok: false, error: "No se pudo conectar a la base de datos." });
          return;
        }

        const updateData = {
          updatedAt: admin.firestore.FieldValue.serverTimestamp(),
        };
        if (provider !== undefined) updateData.provider = provider;
        if (category !== undefined) updateData.category = category;
        if (hotkeyToggle !== undefined) updateData.hotkeyToggle = hotkeyToggle;
        if (hotkeyQuery !== undefined) updateData.hotkeyQuery = hotkeyQuery;
        if (hotkeyCapture !== undefined) updateData.hotkeyCapture = hotkeyCapture;
        if (position !== undefined) updateData.position = position;
        if (theme !== undefined) updateData.theme = theme;
        if (highlightColor !== undefined) updateData.highlightColor = highlightColor;
        if (solutionMode !== undefined) updateData.solutionMode = solutionMode;

        const targetAlias = alias.trim().toLowerCase();
        console.log(`[ScreenAI] Guardando config para "${targetAlias}":`, JSON.stringify(updateData));

        await database.collection(USERS_COLLECTION).doc(targetAlias).set(updateData, { merge: true });

        console.log(`[ScreenAI] Config guardada correctamente para "${targetAlias}"`);
        res.status(200).json({ ok: true });
      } catch (e) {
        console.error("[ScreenAI] saveconfig error:", e.message);
        res.status(500).json({ ok: false, error: "Error al guardar config: " + e.message });
      }
      return;
    }

    if (req.method !== "POST") {
      res.status(405).json({ ok: false, error: "Método no permitido." });
      return;
    }

    const {
      alias: aliasRaw = "",
      mode = "text",
      provider = "gemini-flash",
      category: categoryRaw = "general",
      text = "",
      imageBase64 = "",
      mimeType = "image/png",
      textHint = "",
      solutionMode = "detallado",
    } = req.body || {};

    // 📦 LOG DETALLADO DEL BODY RECIBIDO
    console.log("📦 ============ BODY RECIBIDO ============");
    console.log("👤 alias:", JSON.stringify(aliasRaw));
    console.log("🎯 mode:", JSON.stringify(mode));
    console.log("🤖 provider:", JSON.stringify(provider));
    console.log("📂 category (raw):", JSON.stringify(categoryRaw));
    console.log("🔑 solutionMode:", JSON.stringify(solutionMode));
    console.log("📝 text (primeros 100 chars):", JSON.stringify(text?.substring(0, 100)));
    console.log("🖼️  imageBase64 presente:", imageBase64 ? `✅ SÍ (${imageBase64.length} chars)` : "❌ NO");
    console.log("💬 textHint:", JSON.stringify(textHint));
    console.log("📦 ========================================");

    console.log("[ScreenAI] POST body de alias:", aliasRaw);
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
      res.status(400).json({ ok: false, error: "Campo 'imageBase64' requerido." });
      return;
    }

    const MAX_CHARS_SELECCION = 5000;
    if (mode === "text" && text.trim().length > MAX_CHARS_SELECCION) {
      console.warn(
        `[ScreenAI] Selección rechazada por longitud. Alias: "${alias}", Caracteres: ${text.trim().length}`,
      );
      res.status(413).json({
        ok: false,
        code: "TEXT_TOO_LONG",
        error: `Tu selección tiene ${text.trim().length} caracteres. El máximo es ${MAX_CHARS_SELECCION}. Selecciona solo el enunciado de la pregunta e inténtalo de nuevo.`,
      });
      return;
    }

    const category = categoryRaw;
    const categoryKey = category.replace(/ /g, "_");

    // 🧩 LOG DE SELECCIÓN DE PROMPT
    console.log("🧩 ============ SELECCIÓN DE PROMPT ============");
    console.log("🔤 categoryKey generado:", JSON.stringify(categoryKey));
    console.log("🗂️  solutionMode activo:", JSON.stringify(solutionMode));
    const promptSet =
      solutionMode === "super_detallado"
        ? SYSTEM_PROMPTS_SUPER_DETALLADO
        : solutionMode === "detallado"
          ? SYSTEM_PROMPTS_DETALLADO
          : SYSTEM_PROMPTS;
    console.log("📋 promptSet usado:",
      solutionMode === "super_detallado" ? "SYSTEM_PROMPTS_SUPER_DETALLADO ✅" :
        solutionMode === "detallado" ? "SYSTEM_PROMPTS_DETALLADO ✅" :
          "SYSTEM_PROMPTS (directo) ⚠️");
    console.log("🔍 prompt encontrado para categoryKey:", promptSet[categoryKey] ? "✅ SÍ" : "❌ NO — cayó a general");
    console.log("📄 prompt preview (80 chars):", JSON.stringify(promptSet[categoryKey]?.substring(0, 80) ?? promptSet.general?.substring(0, 80)));
    console.log("🧩 ===============================================");

    console.log(
      `[ScreenAI] Procesando IA. Alias: "${alias}", Modo: "${mode}", ` +
      `Proveedor: "${provider}", Categoría: "${category}", SolutionMode: "${solutionMode}"`,
    );

    let creditosDisponibles = 0;
    try {
      const verification = await verificarAlias(alias);
      if (!verification.ok) {
        console.warn(`[ScreenAI] Verificación fallida para "${alias}": ${verification.error}`);
        res.status(verification.status).json({ ok: false, error: verification.error });
        return;
      }
      creditosDisponibles = Number(verification.credits) || 0;
      console.log(`[ScreenAI] Cuenta verificada para IA. Alias: "${alias}", Créditos: ${creditosDisponibles}`);
    } catch (e) {
      console.error("[ScreenAI] Error crítico en verificación:", e.message);
      res.status(500).json({ ok: false, error: "Error al verificar cuenta: " + e.message });
      return;
    }

    const modelInfo = MODEL_MAP[provider];
    if (!modelInfo) {
      res.status(400).json({ ok: false, error: `Proveedor desconocido: ${provider}` });
      return;
    }

    const GEMINI_KEY = process.env.PIRVATE_KEY_GEMINI_APITRABAJO;
    const OPENAI_KEY = process.env.PIRVATE_KEY_OPENIA_APITRABAJO;

    let answer = "";
    let usage = { tokensInput: 0, tokensOutput: 0, tokensTotal: 0 };

    try {
      const tokens =
        solutionMode === "super_detallado"
          ? maxTokens_SUPER_DETALLADO(categoryKey, provider)
          : solutionMode === "detallado"
            ? maxTokens_DETALLADO(categoryKey, provider)
            : maxTokens(categoryKey, provider);

      const costoModelo = await obtenerCostoDesdeDB(provider, mode);
      const { costo: costoCategoria, costoPorMoneda, tipoCambio } = await obtenerCostoCategoria(category, mode);
      const costoSolucion = await obtenerCostoSolucion(solutionMode);
      const costoTotal = costoModelo + costoCategoria + costoSolucion;

      if (creditosDisponibles < costoTotal) {
        console.warn(`[ScreenAI] Créditos insuficientes, Disponibles: ${creditosDisponibles}, Costo: ${costoTotal}`);
        res.status(402).json({
          ok: false,
          code: "INSUFFICIENT_CREDITS",
          error: "No tienes créditos suficientes para esta consulta.",
        });
        return;
      }

      let systemPrompt;
      if (mode === "image") {
        systemPrompt =
          solutionMode === "super_detallado"
            ? SYSTEM_PROMPT_VISION_SUPER_DETALLADO
            : solutionMode === "detallado"
              ? SYSTEM_PROMPT_VISION_DETALLADO
              : SYSTEM_PROMPT_VISION;
      } else {
        systemPrompt = promptSet[categoryKey] || promptSet.general || SYSTEM_PROMPTS.general;
        console.log(
          `[ScreenAI] categoryKey: "${categoryKey}", promptFound: ${!!promptSet[categoryKey]}, promptPreview: "${systemPrompt?.substring(0, 80)}"`,
        );
      }

      // 🚀 LOG ANTES DE LLAMAR LA IA
      console.log("🚀 ============ LLAMADA A IA ============");
      console.log("🏷️  modelo endpoint/model:", JSON.stringify(modelInfo.endpoint || modelInfo.model));
      console.log("🪙 tokens asignados:", tokens);
      console.log("💰 costoTotal:", costoTotal);
      console.log("📐 maxTokens_DETALLADO hubiera dado:", maxTokens_DETALLADO(categoryKey, provider));
      console.log("📐 maxTokens_DIRECTO hubiera dado:", maxTokens(categoryKey, provider));
      console.log("🗣️  systemPrompt preview (120 chars):", JSON.stringify(systemPrompt?.substring(0, 120)));
      console.log("🚀 ========================================");

      if (mode === "text") {
        switch (modelInfo.family) {
          case "gemini": {
            console.log(`[ScreenAI] callGeminiText → endpoint: "${modelInfo.endpoint}"`);
            const result = await callGeminiText(text, GEMINI_KEY, modelInfo.endpoint, systemPrompt, tokens);
            answer = result.answer;
            usage = result.usage;
            break;
          }
          case "openai": {
            console.log(`[ScreenAI] callOpenAIText → modelo: "${modelInfo.model}"`);
            const result = await callOpenAIText(text, OPENAI_KEY, modelInfo.model, systemPrompt, tokens);
            answer = result.answer;
            usage = result.usage;
            break;
          }
        }
      } else {
        const finalHint = textHint || "Resuelve el examen de la imagen según las instrucciones del sistema.";
        switch (modelInfo.family) {
          case "gemini": {
            console.log(`[ScreenAI] callGeminiVision → endpoint: "${modelInfo.endpoint}", hint: "${finalHint}"`);
            const result = await callGeminiVision(imageBase64, mimeType, finalHint, GEMINI_KEY, modelInfo.endpoint, systemPrompt, tokens);
            answer = result.answer;
            usage = result.usage;
            break;
          }
          case "openai": {
            console.log(`[ScreenAI] callOpenAIVision → modelo: "${modelInfo.model}", hint: "${finalHint}"`);
            const result = await callOpenAIVision(imageBase64, mimeType, finalHint, OPENAI_KEY, modelInfo.model, systemPrompt, tokens);
            answer = result.answer;
            usage = result.usage;
            break;
          }
        }
      }

      console.log(
        `[ScreenAI] Respuesta de IA recibida. Caracteres: ${answer?.length ?? 0}. ` +
        `Tokens → input: ${usage.tokensInput}, output: ${usage.tokensOutput}, total: ${usage.tokensTotal}`,
      );

      if (!answer || !answer.trim()) {
        console.error("[ScreenAI] La respuesta de la IA llegó vacía.");
        res.status(502).json({ ok: false, error: "La IA no devolvió respuesta." });
        return;
      }

      const costoRealUSD = calcularCostoRealUSD(provider, usage.tokensInput, usage.tokensOutput);
      const TIPO_CAMBIO_USD_PEN = 3.75;
      const costoRealSoles = parseFloat((costoRealUSD * TIPO_CAMBIO_USD_PEN).toFixed(6));
      const costoEnSoles = parseFloat((costoTotal * costoPorMoneda).toFixed(4));
      const margenGananciaSoles = parseFloat((costoEnSoles - costoRealSoles).toFixed(6));
      const margenGananciaPorcentaje =
        costoRealSoles > 0
          ? parseFloat((((costoEnSoles - costoRealSoles) / costoRealSoles) * 100).toFixed(2))
          : 0;
      const multiplicadorGanancia =
        costoRealSoles > 0
          ? parseFloat((costoEnSoles / costoRealSoles).toFixed(2))
          : 0;

      const valida = esRespuestaValida(answer, mode);

      if (valida) {
        try {
          const { antes, despues } = await descontarCreditoN(alias.toLowerCase(), costoTotal);
          await guardarHistorial(
            alias.toLowerCase(),
            provider,
            category,
            mode,
            antes,
            despues,
            costoTotal,
            costoEnSoles,
            costoPorMoneda,
            solutionMode,
            {
              tokensInput: usage.tokensInput,
              tokensOutput: usage.tokensOutput,
              tokensTotal: usage.tokensTotal,
              costoRealUSD: parseFloat(costoRealUSD.toFixed(6)),
              costoRealSoles,
              margenGananciaSoles,
              margenGananciaPorcentaje,
              multiplicadorGanancia,
              tipoCambioUSD: TIPO_CAMBIO_USD_PEN,
              precioInputPorMillon: PRECIO_USD_POR_MILLON[provider]?.input ?? 0,
              precioOutputPorMillon: PRECIO_USD_POR_MILLON[provider]?.output ?? 0,
              costoRealInputUSD: (usage.tokensInput / 1_000_000) * (PRECIO_USD_POR_MILLON[provider]?.input ?? 0),
              costoRealOutputUSD: (usage.tokensOutput / 1_000_000) * (PRECIO_USD_POR_MILLON[provider]?.output ?? 0),
            },
            answer,
          );
        } catch (e) {
          console.warn("[ScreenAI] No se pudo descontar crédito:", e.message);
        }
      } else {
        console.log(`[ScreenAI] Respuesta inválida — NO se descuenta crédito. alias=${alias}, mode=${mode}`);
      }

      console.log(`[ScreenAI] Petición finalizada OK para "${alias}". HTTP 200.`);
      res.status(200).json({ ok: true, answer, charged: valida });
    } catch (aiErr) {
      console.error("[ScreenAI] Error en llamada a IA:", aiErr.message);
      console.error("[ScreenAI] AI response data:", JSON.stringify(aiErr.response?.data));
      res.status(502).json({ ok: false, error: aiErr.message });
    }
  },
);

module.exports = { screenaiQuery_extencion };
