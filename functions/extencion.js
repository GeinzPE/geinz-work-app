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
const MODEL_MAP = {
  "gemini-flash": { family: "gemini", endpoint: GEMINI_FLASH_URL },
  "gemini-pro": { family: "gemini", endpoint: GEMINI_PRO_URL },
  "gpt-4o": { family: "openai", model: "gpt-4o" },
  "gpt-4o-mini": { family: "openai", model: "gpt-4o-mini" },
  "gpt4o-mini": { family: "openai", model: "gpt-4o-mini" }, // ← alias
  gpt4o: { family: "openai", model: "gpt-4o" }, // ← alias
};

let db2 = null;
let preciosCache = null;
let preciosCacheTime = 0;
const CACHE_TTL = 5 * 60 * 1000;

const {
  SYSTEM_PROMPTS,
  SYSTEM_PROMPTS_DETALLADO, // 👈 nuevo set de prompts para modo detallado
  SYSTEM_PROMPT_VISION,
  maxTokens,
} = require("./modelo_promps_ia");

const {
  verificarAlias,
  esRespuestaValida,
  obtenerCostoDesdeDB,
  obtenerCostoCategoria,
  descontarCreditoN,
  guardarHistorial,
  callGeminiVision,
  callOpenAIText,
  callOpenAIVision,
  callGeminiText,
} = require("./functions_trabajo");

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


const screenaiQuery_extencion = onRequest(
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
          res.status(404).json({ ok: false, error: "Usuario no encontrado." });
          return;
        }

        const data = snap.data();
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
        if (hotkeyCapture !== undefined)
          updateData.hotkeyCapture = hotkeyCapture;
        if (position !== undefined) updateData.position = position;
        if (theme !== undefined) updateData.theme = theme;
        if (highlightColor !== undefined)
          updateData.highlightColor = highlightColor;
        if (solutionMode !== undefined) updateData.solutionMode = solutionMode;

        await database
          .collection(USERS_COLLECTION)
          .doc(alias.trim().toLowerCase())
          .set(updateData, { merge: true });

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
      solutionMode = "detallado",
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
      // ── Selección de set de prompts según el modo de solución ──────────────
      // solutionMode === "detallado" → usa SYSTEM_PROMPTS_DETALLADO (paso a paso)
      // solutionMode === "directo"   → usa SYSTEM_PROMPTS (respuesta simple, la que ya tenías)
      const promptSet =
        solutionMode === "detallado" ? SYSTEM_PROMPTS_DETALLADO : SYSTEM_PROMPTS;

      const systemPrompt =
        promptSet[category] || promptSet.general || SYSTEM_PROMPTS.general;

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

      if (!answer || !answer.trim()) {
        res
          .status(502)
          .json({ ok: false, error: "La IA no devolvió respuesta." });
        return;
      }

      // DESPUÉS (correcto):
      const valida = esRespuestaValida(answer, mode);

      if (valida) {
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
      } else {
        console.log(
          `[ScreenAI] Respuesta inválida/ilegible — NO se descuenta crédito. alias=${alias} mode=${mode}`,
        );
      }

      res.status(200).json({ ok: true, answer, charged: valida });
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

module.exports = { screenaiQuery_extencion };