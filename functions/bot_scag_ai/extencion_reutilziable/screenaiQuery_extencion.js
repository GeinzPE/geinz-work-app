"use strict";

const { onRequest } = require("firebase-functions/v2/https");
const admin = require("firebase-admin");

const {
  MODEL_MAP,
  PRECIO_USD_POR_MILLON,
  TIPO_CAMBIO_USD_PEN,
  USERS_COLLECTION,
  calcularCostoRealUSD,
  initDb2,
  callGeminiText,
  callGeminiVision,
  callOpenAIText,
  callOpenAIVision,
  limpiarRespuesta,
} = require("./_shared");

const {
  verificarAlias,
  esRespuestaValida,
  obtenerCostoDesdeDB,
  obtenerCostoCategoria,
  obtenerCostoSolucion,
  descontarCreditoN,
  guardarHistorial,
} = require("../functions_trabajo");

const {
  salidafinal,
  ESPECIALIDADES,
  resolverCategoryKey,
  maxTokens,
  maxTokens_DETALLADO,
  maxTokens_SUPER_DETALLADO,
  thinkingBudgetPorNivel,
} = require("../promps_scag_ai");

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
      console.log(
        `[ScreenAI] Flujo GET check=1 iniciado para alias: "${alias}"`,
      );

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
        console.log(
          `[ScreenAI] Alias "${alias}" verificado. Créditos: ${result.credits}`,
        );
        res.status(200).json({ ok: true, credits: result.credits });
      } catch (e) {
        console.error("[ScreenAI] verificarAlias error:", e.message);
        res
          .status(500)
          .json({ ok: false, error: "Error al verificar alias: " + e.message });
      }
      return;
    }

    if (req.method === "GET" && req.query.config === "1") {
      const alias = (req.query.alias || "").trim().toLowerCase();
      console.log(
        `[ScreenAI] Flujo GET config=1 iniciado para alias: "${alias}"`,
      );

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
        console.log(
          `[ScreenAI] Config recuperada para "${alias}":`,
          JSON.stringify(data),
        );
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

        const targetAlias = alias.trim().toLowerCase();
        console.log(
          `[ScreenAI] Guardando config para "${targetAlias}":`,
          JSON.stringify(updateData),
        );

        await database
          .collection(USERS_COLLECTION)
          .doc(targetAlias)
          .set(updateData, { merge: true });

        console.log(
          `[ScreenAI] Config guardada correctamente para "${targetAlias}"`,
        );
        res.status(200).json({ ok: true });
      } catch (e) {
        console.error("[ScreenAI] saveconfig error:", e.message);
        res
          .status(500)
          .json({ ok: false, error: "Error al guardar config: " + e.message });
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
    console.log(
      "📝 text (primeros 100 chars):",
      JSON.stringify(text?.substring(0, 100)),
    );
    console.log(
      "🖼️  imageBase64 presente:",
      imageBase64 ? `✅ SÍ (${imageBase64.length} chars)` : "❌ NO",
    );
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
      res
        .status(400)
        .json({ ok: false, error: "Campo 'imageBase64' requerido." });
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
    const categoryKey = resolverCategoryKey(category);

    // 🧩 LOG DE SELECCIÓN DE PROMPT
    console.log("🧩 ============ SELECCIÓN DE PROMPT ============");
    console.log("🔤 categoryKey generado:", JSON.stringify(categoryKey));
    console.log("🗂️  solutionMode activo:", JSON.stringify(solutionMode));

    // ── FIX: ya no existen SYSTEM_PROMPTS / SYSTEM_PROMPTS_DETALLADO / etc.
    // Ahora todo pasa por salidafinal(category, tipo, base, provider).
    const baseSeleccionada =
      solutionMode === "super_detallado"
        ? "super"
        : solutionMode === "detallado"
          ? "detallado"
          : "directo";

    console.log("📋 base usada:", JSON.stringify(baseSeleccionada));
    console.log(
      "🔍 especialidad encontrada para categoryKey:",
      ESPECIALIDADES[categoryKey] ? "✅ SÍ" : "❌ NO — cayó a general",
    );
    console.log("🧩 ===============================================");

    console.log(
      `[ScreenAI] Procesando IA. Alias: "${alias}", Modo: "${mode}", ` +
        `Proveedor: "${provider}", Categoría: "${category}", SolutionMode: "${solutionMode}"`,
    );

    let creditosDisponibles = 0;
    try {
      const verification = await verificarAlias(alias);
      if (!verification.ok) {
        console.warn(
          `[ScreenAI] Verificación fallida para "${alias}": ${verification.error}`,
        );
        res
          .status(verification.status)
          .json({ ok: false, error: verification.error });
        return;
      }
      creditosDisponibles = Number(verification.credits) || 0;
      console.log(
        `[ScreenAI] Cuenta verificada para IA. Alias: "${alias}", Créditos: ${creditosDisponibles}`,
      );
    } catch (e) {
      console.error("[ScreenAI] Error crítico en verificación:", e.message);
      res
        .status(500)
        .json({ ok: false, error: "Error al verificar cuenta: " + e.message });
      return;
    }

    const modelInfo = MODEL_MAP[provider];
    if (!modelInfo) {
      res
        .status(400)
        .json({ ok: false, error: `Proveedor desconocido: ${provider}` });
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

      const thinkingBudget = thinkingBudgetPorNivel(baseSeleccionada, provider);

      const costoModelo = await obtenerCostoDesdeDB(provider, mode);
      const {
        costo: costoCategoria,
        costoPorMoneda,
        tipoCambio,
      } = await obtenerCostoCategoria(category, mode);
      const costoSolucion = await obtenerCostoSolucion(solutionMode);
      const costoTotal = costoModelo + costoCategoria + costoSolucion;

      if (creditosDisponibles < costoTotal) {
        console.warn(
          `[ScreenAI] Créditos insuficientes, Disponibles: ${creditosDisponibles}, Costo: ${costoTotal}`,
        );
        res.status(402).json({
          ok: false,
          code: "INSUFFICIENT_CREDITS",
          error: "No tienes créditos suficientes para esta consulta.",
        });
        return;
      }

      // ── FIX: selección de systemPrompt vía salidafinal en vez de
      // SYSTEM_PROMPTS_VISION_* / SYSTEM_PROMPTS_* que ya no existen.
      let systemPrompt;
      if (mode === "image") {
        const resultadoPrompt = salidafinal(
          categoryKey,
          "vision",
          baseSeleccionada,
          provider,
        );
        systemPrompt = resultadoPrompt.systemPrompt;
        console.log(
          `[ScreenAI] VISION categoryKey: "${categoryKey}", promptFound: ${!!ESPECIALIDADES[categoryKey]}, solutionMode: "${solutionMode}"`,
        );
      } else {
        const resultadoPrompt = salidafinal(
          categoryKey,
          "texto",
          baseSeleccionada,
          provider,
        );
        systemPrompt = resultadoPrompt.systemPrompt;
        console.log(
          `[ScreenAI] categoryKey: "${categoryKey}", promptFound: ${!!ESPECIALIDADES[categoryKey]}, promptPreview: "${systemPrompt?.substring(0, 80)}"`,
        );
      }
      // 🚀 LOG ANTES DE LLAMAR LA IA
      console.log("🚀 ============ LLAMADA A IA ============");
      console.log(
        "🏷️  modelo endpoint/model:",
        JSON.stringify(modelInfo.endpoint || modelInfo.model),
      );
      console.log("🪙 tokens asignados:", tokens);
      console.log("💰 costoTotal:", costoTotal);
      console.log(
        "📐 maxTokens_DETALLADO hubiera dado:",
        maxTokens_DETALLADO(categoryKey, provider),
      );
      console.log(
        "📐 maxTokens_DIRECTO hubiera dado:",
        maxTokens(categoryKey, provider),
      );
      console.log(
        "🗣️  systemPrompt preview (120 chars):",
        JSON.stringify(systemPrompt?.substring(0, 120)),
      );
      console.log("🚀 ========================================");

      if (mode === "text") {
        switch (modelInfo.family) {
          case "gemini": {
            console.log(
              `[ScreenAI] callGeminiText → endpoint: "${modelInfo.endpoint}"`,
            );
            const result = await callGeminiText(
              text,
              GEMINI_KEY,
              modelInfo.endpoint,
              systemPrompt,
              tokens,
              thinkingBudget,
            );
            answer = limpiarRespuesta(result.answer);
            usage = result.usage;
            break;
          }
          case "openai": {
            console.log(
              `[ScreenAI] callOpenAIText → modelo: "${modelInfo.model}"`,
            );
            const result = await callOpenAIText(
              text,
              OPENAI_KEY,
              modelInfo.model,
              systemPrompt,
              tokens,
            );
            answer = limpiarRespuesta(result.answer);
            usage = result.usage;
            break;
          }
        }
      } else {
        const finalHint =
          textHint ||
          "Resuelve el examen de la imagen según las instrucciones del sistema.";
        switch (modelInfo.family) {
          case "gemini": {
            console.log(
              `[ScreenAI] callGeminiVision → endpoint: "${modelInfo.endpoint}", hint: "${finalHint}"`,
            );
            const result = await callGeminiVision(
              imageBase64,
              mimeType,
              finalHint,
              GEMINI_KEY,
              modelInfo.endpoint,
              systemPrompt,
              tokens,
              thinkingBudget,
            );
            answer = limpiarRespuesta(result.answer);
            usage = result.usage;
            break;
          }
          case "openai": {
            console.log(
              `[ScreenAI] callOpenAIVision → modelo: "${modelInfo.model}", hint: "${finalHint}"`,
            );
            const result = await callOpenAIVision(
              imageBase64,
              mimeType,
              finalHint,
              OPENAI_KEY,
              modelInfo.model,
              systemPrompt,
              tokens,
            );
            answer = limpiarRespuesta(result.answer);
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
        res
          .status(502)
          .json({ ok: false, error: "La IA no devolvió respuesta." });
        return;
      }

      const costoRealUSD = calcularCostoRealUSD(
        provider,
        usage.tokensInput,
        usage.tokensOutput,
      );

      const costoRealSoles = parseFloat(
        (costoRealUSD * TIPO_CAMBIO_USD_PEN).toFixed(6),
      );
      const costoEnSoles = parseFloat((costoTotal * costoPorMoneda).toFixed(4));
      const margenGananciaSoles = parseFloat(
        (costoEnSoles - costoRealSoles).toFixed(6),
      );
      const margenGananciaPorcentaje =
        costoRealSoles > 0
          ? parseFloat(
              (
                ((costoEnSoles - costoRealSoles) / costoRealSoles) *
                100
              ).toFixed(2),
            )
          : 0;
      const multiplicadorGanancia =
        costoRealSoles > 0
          ? parseFloat((costoEnSoles / costoRealSoles).toFixed(2))
          : 0;

      const valida = esRespuestaValida(answer, mode);

      if (valida) {
        try {
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
              precioOutputPorMillon:
                PRECIO_USD_POR_MILLON[provider]?.output ?? 0,
              costoRealInputUSD:
                (usage.tokensInput / 1_000_000) *
                (PRECIO_USD_POR_MILLON[provider]?.input ?? 0),
              costoRealOutputUSD:
                (usage.tokensOutput / 1_000_000) *
                (PRECIO_USD_POR_MILLON[provider]?.output ?? 0),
            },
            answer,
          );
        } catch (e) {
          console.warn("[ScreenAI] No se pudo descontar crédito:", e.message);
        }
      } else {
        console.log(
          `[ScreenAI] Respuesta inválida — NO se descuenta crédito. alias=${alias}, mode=${mode}`,
        );
      }

      console.log(
        `[ScreenAI] Petición finalizada OK para "${alias}". HTTP 200.`,
      );
      res.status(200).json({ ok: true, answer, charged: valida });
    } catch (aiErr) {
      console.error("[ScreenAI] Error en llamada a IA:", aiErr.message);
      console.error(
        "[ScreenAI] AI response data:",
        JSON.stringify(aiErr.response?.data),
      );
      res.status(502).json({ ok: false, error: aiErr.message });
    }
  },
);

module.exports = { screenaiQuery_extencion };
