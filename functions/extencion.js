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
  "gemini-pro":   { family: "gemini", endpoint: GEMINI_PRO_URL },
  "gpt-4o":       { family: "openai", model: "gpt-4o" },
  "gpt-4o-mini":  { family: "openai", model: "gpt-4o-mini" },
  "gpt4o-mini":   { family: "openai", model: "gpt-4o-mini" },
  gpt4o:          { family: "openai", model: "gpt-4o" },
};

let db2 = null;
const CACHE_TTL = 5 * 60 * 1000;

// ── Importación de prompts y tokens ──────────────────────────────────────────
const {
  SYSTEM_PROMPTS,
  SYSTEM_PROMPTS_DETALLADO,
  SYSTEM_PROMPT_VISION,
  SYSTEM_PROMPT_VISION_DETALLADO,
  maxTokens,
  maxTokens_DETALLADO,
} = require("./modelo_promps_ia");

// ── Importación de funciones de negocio (sin las call* que ya están aquí) ────
const {
  verificarAlias,
  esRespuestaValida,
  obtenerCostoDesdeDB,
  obtenerCostoCategoria,
  descontarCreditoN,
  guardarHistorial,
} = require("./functions_trabajo");

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
            projectId:   process.env.PROJECT_ID_2,
            clientEmail: process.env.CLIENT_EMAIL_2,
            privateKey:  process.env.PRIVATE_KEY_2?.replace(/\\n/g, "\n"),
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
  const { data } = await axios.post(
    `${endpoint}?key=${apiKey}`,
    {
      system_instruction: { parts: [{ text: systemPrompt }] },
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
      generationConfig: {
        temperature: 0.2,
        maxOutputTokens: tokens,
      },
    },
    { headers: { "Content-Type": "application/json" }, timeout: 45000 },
  );

  return (
    data?.candidates?.[0]?.content?.parts?.[0]?.text?.trim() ?? "Sin respuesta."
  );
}

// ── OPENAI TEXT ───────────────────────────────────────────────────────────────
async function callOpenAIText(text, apiKey, model, systemPrompt, tokens) {
  const { data } = await axios.post(
    OPENAI_URL,
    {
      model,
      messages: [
        { role: "system", content: systemPrompt },
        { role: "user",   content: text },
      ],
      max_tokens:  tokens,
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
      max_tokens:  tokens,
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

// ── Cloud Function principal ──────────────────────────────────────────────────
const screenaiQuery_extencion = onRequest(
  { region: "us-central1", timeoutSeconds: 120, memory: "256MiB", cors: true },
  async (req, res) => {
    console.log(
      `[ScreenAI] Nueva petición recibida. Método: ${req.method}, Query:`,
      JSON.stringify(req.query),
    );

    // ── CORS Preflight ────────────────────────────────────────────────────────
    if (req.method === "OPTIONS") {
      console.log("[ScreenAI] Manejando petición OPTIONS (CORS Preflight)");
      res.status(204).send("");
      return;
    }

    // ── GET ?check=1&alias=xxx — verificar alias y créditos ───────────────────
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

    // ── GET ?config=1&alias=xxx — leer configuración del usuario ──────────────
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
          ok:             true,
          provider:       data.provider       || null,
          category:       data.category       || null,
          hotkeyToggle:   data.hotkeyToggle   || null,
          hotkeyQuery:    data.hotkeyQuery    || null,
          hotkeyCapture:  data.hotkeyCapture  || null,
          position:       data.position       || null,
          theme:          data.theme          || "solid",
          highlightColor: data.highlightColor || "#fad232",
          solutionMode:   data.solutionMode   || "detallado",
        });
      } catch (e) {
        console.error("[ScreenAI] config GET error:", e.message);
        res.status(500).json({ ok: false, error: "Error al leer config: " + e.message });
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
        if (provider       !== undefined) updateData.provider       = provider;
        if (category       !== undefined) updateData.category       = category;
        if (hotkeyToggle   !== undefined) updateData.hotkeyToggle   = hotkeyToggle;
        if (hotkeyQuery    !== undefined) updateData.hotkeyQuery    = hotkeyQuery;
        if (hotkeyCapture  !== undefined) updateData.hotkeyCapture  = hotkeyCapture;
        if (position       !== undefined) updateData.position       = position;
        if (theme          !== undefined) updateData.theme          = theme;
        if (highlightColor !== undefined) updateData.highlightColor = highlightColor;
        if (solutionMode   !== undefined) updateData.solutionMode   = solutionMode;

        const targetAlias = alias.trim().toLowerCase();
        console.log(`[ScreenAI] Guardando config para "${targetAlias}":`, JSON.stringify(updateData));

        await database
          .collection(USERS_COLLECTION)
          .doc(targetAlias)
          .set(updateData, { merge: true });

        console.log(`[ScreenAI] Config guardada correctamente para "${targetAlias}"`);
        res.status(200).json({ ok: true });
      } catch (e) {
        console.error("[ScreenAI] saveconfig error:", e.message);
        res.status(500).json({ ok: false, error: "Error al guardar config: " + e.message });
      }
      return;
    }

    // ── POST → consulta IA ────────────────────────────────────────────────────
    if (req.method !== "POST") {
      res.status(405).json({ ok: false, error: "Método no permitido." });
      return;
    }

    const {
      alias: aliasRaw       = "",
      mode                  = "text",
      provider              = "gemini-flash",
      category: categoryRaw = "general",
      text                  = "",
      imageBase64           = "",
      mimeType              = "image/png",
      textHint              = "",
      solutionMode          = "detallado", // "directo" | "detallado"
    } = req.body || {};

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

    // Para imágenes siempre forzamos esta categoría especial
    const category = mode === "image" ? "Vision_Procesamiento_Grafico" : categoryRaw;
    console.log(
      `[ScreenAI] Procesando IA. Alias: "${alias}", Modo: "${mode}", ` +
      `Proveedor: "${provider}", Categoría: "${category}", SolutionMode: "${solutionMode}"`,
    );

    // ── Verificar alias y créditos ────────────────────────────────────────────
    try {
      const verification = await verificarAlias(alias);
      if (!verification.ok) {
        console.warn(`[ScreenAI] Verificación fallida para "${alias}": ${verification.error}`);
        res.status(verification.status).json({ ok: false, error: verification.error });
        return;
      }
      console.log(`[ScreenAI] Cuenta verificada para IA. Alias: "${alias}"`);
    } catch (e) {
      console.error("[ScreenAI] Error crítico en verificación:", e.message);
      res.status(500).json({ ok: false, error: "Error al verificar cuenta: " + e.message });
      return;
    }

    // ── Seleccionar modelo ────────────────────────────────────────────────────
    const modelInfo = MODEL_MAP[provider];
    if (!modelInfo) {
      res.status(400).json({ ok: false, error: `Proveedor desconocido: ${provider}` });
      return;
    }

    // ── Keys desde variables de entorno ──────────────────────────────────────
    const GEMINI_KEY = process.env.PIRVATE_KEY_GEMINI_APITRABAJO;
    const OPENAI_KEY = process.env.PIRVATE_KEY_OPENIA_APITRABAJO;

    let answer = "";
    try {
      // ── Tokens según solutionMode ─────────────────────────────────────────
      const tokens = solutionMode === "detallado"
        ? maxTokens_DETALLADO(category)
        : maxTokens(category);

      // ── costoTotal ────────────────────────────────────────────────────────
      const costoTotal = tokens;

      // ── systemPrompt correcto según modo e imagen/texto ───────────────────
      let systemPrompt;
      if (mode === "image") {
        systemPrompt = solutionMode === "detallado"
          ? SYSTEM_PROMPT_VISION_DETALLADO
          : SYSTEM_PROMPT_VISION;
      } else {
        const promptSet = solutionMode === "detallado"
          ? SYSTEM_PROMPTS_DETALLADO
          : SYSTEM_PROMPTS;
        systemPrompt =
          promptSet[category] ||
          promptSet.general   ||
          SYSTEM_PROMPTS.general;
      }

      console.log(
        `[ScreenAI] Familia IA: "${modelInfo.family}", Tokens: ${tokens}, ` +
        `CostoTotal: ${costoTotal}, SolutionMode: "${solutionMode}"`,
      );

      // ── Llamada a la IA ───────────────────────────────────────────────────
      if (mode === "text") {
        switch (modelInfo.family) {
          case "gemini":
            console.log(`[ScreenAI] callGeminiText → endpoint: "${modelInfo.endpoint}"`);
            answer = await callGeminiText(
              text, GEMINI_KEY, modelInfo.endpoint, systemPrompt, tokens,
            );
            break;
          case "openai":
            console.log(`[ScreenAI] callOpenAIText → modelo: "${modelInfo.model}"`);
            answer = await callOpenAIText(
              text, OPENAI_KEY, modelInfo.model, systemPrompt, tokens,
            );
            break;
        }
      } else {
        // mode === "image"
        const finalHint = textHint || "Resuelve el examen de la imagen según las instrucciones del sistema.";
        switch (modelInfo.family) {
          case "gemini":
            console.log(`[ScreenAI] callGeminiVision → endpoint: "${modelInfo.endpoint}", hint: "${finalHint}"`);
            answer = await callGeminiVision(
              imageBase64, mimeType, finalHint,
              GEMINI_KEY, modelInfo.endpoint,
              systemPrompt, tokens,
            );
            break;
          case "openai":
            console.log(`[ScreenAI] callOpenAIVision → modelo: "${modelInfo.model}", hint: "${finalHint}"`);
            answer = await callOpenAIVision(
              imageBase64, mimeType, finalHint,
              OPENAI_KEY, modelInfo.model,
              systemPrompt, tokens,
            );
            break;
        }
      }

      console.log(`[ScreenAI] Respuesta de IA recibida. Caracteres: ${answer?.length ?? 0}`);

      if (!answer || !answer.trim()) {
        console.error("[ScreenAI] La respuesta de la IA llegó vacía.");
        res.status(502).json({ ok: false, error: "La IA no devolvió respuesta." });
        return;
      }

      // ── Validar y descontar créditos ──────────────────────────────────────
      const valida = esRespuestaValida(answer, mode);
      console.log(`[ScreenAI] esRespuestaValida: ${valida}`);

      if (valida) {
        try {
          const { antes, despues } = await descontarCreditoN(
            alias.toLowerCase(),
            costoTotal,
          );
          console.log(
            `[ScreenAI] Créditos descontados. Antes: ${antes}, Después: ${despues}, ` +
            `Costo cobrado: ${costoTotal}`,
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
      } else {
        console.log(
          `[ScreenAI] Respuesta inválida — NO se descuenta crédito. alias=${alias}, mode=${mode}`,
        );
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