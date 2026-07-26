"use strict";

const { onRequest } = require("firebase-functions/v2/https");

const {
  MODEL_MAP,
  PRECIO_USD_POR_MILLON,
  TIPO_CAMBIO_USD_PEN,
  calcularCostoRealUSD,
  callGeminiText,
  callOpenAIText,
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

const { salidafinal, resolverCategoryKey, thinkingBudgetPorNivel } = require(
  "../promps_scag_ai",
);

const screenaiQuery_texto_n8n = onRequest(
  { region: "us-central1", timeoutSeconds: 90, memory: "256MiB", cors: true },
  async (req, res) => {
    if (req.method === "OPTIONS") {
      res.status(204).send("");
      return;
    }
    if (req.method !== "POST") {
      res.status(405).json({ ok: false, error: "Método no permitido." });
      return;
    }

    const {
      alias: aliasRaw = "",
      provider = "gemini-flash",
      category: categoryRaw = "general",
      solutionMode = "detallado",
      text = "",
    } = req.body || {};

    const alias = String(aliasRaw ?? "")
      .trim()
      .toLowerCase();

    // ── VALIDACIONES BÁSICAS ──────────────────────────────────────────────────
    if (!alias) {
      res.status(401).json({ ok: false, error: "Alias requerido." });
      return;
    }
    if (!text || !text.trim()) {
      res.status(400).json({ ok: false, error: "Campo 'text' requerido." });
      return;
    }

    const MIN_TEXT_CHARS = 3;
    if (text.trim().length < MIN_TEXT_CHARS) {
      console.warn(
        `[texto-n8n] Texto rechazado por tamaño mínimo. alias="${alias}", chars=${text.trim().length}`,
      );
      res.status(400).json({
        ok: false,
        code: "TEXT_TOO_SMALL",
        error: "El texto enviado es demasiado corto o inválido.",
      });
      return;
    }

    const modelInfo = MODEL_MAP[provider];
    if (!modelInfo) {
      res
        .status(400)
        .json({ ok: false, error: `Proveedor desconocido: ${provider}` });
      return;
    }

    console.log(
      "📦 [texto-n8n] alias:",
      alias,
      "| provider:",
      provider,
      "| category:",
      categoryRaw,
      "| solutionMode:",
      solutionMode,
      "| text length:",
      text.length,
    );

    // ── VERIFICAR ALIAS Y CRÉDITOS ────────────────────────────────────────────
    let creditosDisponibles = 0;
    try {
      const verification = await verificarAlias(alias);
      if (!verification.ok) {
        console.warn(
          `[texto-n8n] Alias inválido: "${alias}" → ${verification.error}`,
        );
        res
          .status(verification.status)
          .json({ ok: false, error: verification.error });
        return;
      }
      creditosDisponibles = Number(verification.credits) || 0;
      console.log(
        `[texto-n8n] Alias OK. Créditos disponibles: ${creditosDisponibles}`,
      );
    } catch (e) {
      console.error("[texto-n8n] Error verificando alias:", e.message);
      res
        .status(500)
        .json({ ok: false, error: "Error al verificar cuenta: " + e.message });
      return;
    }

    // ── CALCULAR COSTO ANTES DE LLAMAR A LA IA ────────────────────────────────
    let costoTotal = 0;
    let costoPorMoneda = 1;
    try {
      const costoModelo = await obtenerCostoDesdeDB(provider, "text");
      const { costo: costoCategoria, costoPorMoneda: cpM } =
        await obtenerCostoCategoria(categoryRaw, "text");
      const costoSolucion = await obtenerCostoSolucion(solutionMode);
      costoPorMoneda = cpM;
      costoTotal = costoModelo + costoCategoria + costoSolucion;
      console.log(
        `[texto-n8n] Costo calculado: ${costoTotal} (modelo:${costoModelo} + cat:${costoCategoria} + sol:${costoSolucion})`,
      );
    } catch (e) {
      console.error("[texto-n8n] Error calculando costo:", e.message);
      res.status(500).json({ ok: false, error: "Error al calcular costo." });
      return;
    }

    if (creditosDisponibles < costoTotal) {
      console.warn(
        `[texto-n8n] Créditos insuficientes. Disponibles: ${creditosDisponibles}, Necesita: ${costoTotal}`,
      );
      res.status(402).json({
        ok: false,
        code: "INSUFFICIENT_CREDITS",
        error: "No tienes créditos suficientes para esta consulta.",
      });
      return;
    }

    // ── PROMPT Y TOKENS ───────────────────────────────────────────────────────
    const categoryKey = resolverCategoryKey(categoryRaw);
    const baseSeleccionada =
      solutionMode === "super_detallado"
        ? "super"
        : solutionMode === "detallado"
          ? "detallado"
          : "directo";

    const { systemPrompt, maxTokens: tokens } = salidafinal(
      categoryKey,
      "texto",
      baseSeleccionada,
      provider,
    );
    const thinkingBudget = thinkingBudgetPorNivel(baseSeleccionada, provider);

    const GEMINI_KEY = process.env.PIRVATE_KEY_GEMINI_APITRABAJO;
    const OPENAI_KEY = process.env.PIRVATE_KEY_OPENIA_APITRABAJO;

    // ── LLAMADA A LA IA ───────────────────────────────────────────────────────
    let answer = "";
    let usage = { tokensInput: 0, tokensOutput: 0, tokensTotal: 0 };

    try {
      let result;
      if (modelInfo.family === "gemini") {
        result = await callGeminiText(
          text,
          GEMINI_KEY,
          modelInfo.endpoint,
          systemPrompt,
          tokens,
          thinkingBudget,
        );
      } else if (modelInfo.family === "openai") {
        result = await callOpenAIText(
          text,
          OPENAI_KEY,
          modelInfo.model,
          systemPrompt,
          tokens,
        );
      } else {
        res.status(400).json({
          ok: false,
          error: `Familia no soportada: ${modelInfo.family}`,
        });
        return;
      }

      answer = limpiarRespuesta(result.answer);
      usage = result.usage;

      console.log(
        `[texto-n8n] Respuesta IA. chars: ${answer?.length ?? 0} | ` +
          `tokens → input: ${usage.tokensInput}, output: ${usage.tokensOutput}`,
      );
    } catch (err) {
      console.error("[texto-n8n] Error llamando IA:", err.message);
      res.status(502).json({ ok: false, error: err.message });
      return;
    }

    if (!answer || !answer.trim()) {
      console.error(
        "[texto-n8n] IA devolvió respuesta vacía. NO se descuenta crédito.",
      );
      res
        .status(502)
        .json({ ok: false, error: "La IA no devolvió respuesta." });
      return;
    }

    const valida = esRespuestaValida(answer, "text");

    if (valida) {
      try {
        const costoRealUSD = calcularCostoRealUSD(
          provider,
          usage.tokensInput,
          usage.tokensOutput,
        );
        const costoRealSoles = parseFloat(
          (costoRealUSD * TIPO_CAMBIO_USD_PEN).toFixed(6),
        );
        const costoEnSoles = parseFloat(
          (costoTotal * costoPorMoneda).toFixed(4),
        );
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

        console.log(
          `[texto-n8n] Intentando descontar crédito. alias=${alias}, costoTotal=${costoTotal}`,
        );
        const { antes, despues } = await descontarCreditoN(alias, costoTotal);
        console.log(
          `[texto-n8n] Crédito descontado OK. antes=${antes}, despues=${despues}`,
        );

        console.log(`[texto-n8n] Intentando guardar historial...`);
        await guardarHistorial(
          alias,
          provider,
          categoryRaw,
          "text",
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
            costoRealInputUSD:
              (usage.tokensInput / 1_000_000) *
              (PRECIO_USD_POR_MILLON[provider]?.input ?? 0),
            costoRealOutputUSD:
              (usage.tokensOutput / 1_000_000) *
              (PRECIO_USD_POR_MILLON[provider]?.output ?? 0),
            fuente: "n8n",
          },
          answer,
        );
        console.log(`[texto-n8n] Historial guardado OK.`);
      } catch (e) {
        console.error(
          "[texto-n8n] ❌ ERROR en descuento/historial:",
          e.message,
        );
        console.error("[texto-n8n] ❌ Stack:", e.stack);
      }
    } else {
      console.log(
        `[texto-n8n] Respuesta inválida — NO se descuenta. alias=${alias}, answer preview: "${answer?.substring(0, 80)}"`,
      );
    }

    res.status(200).json({ ok: true, answer, usage, charged: valida });
  },
);

module.exports = { screenaiQuery_texto_n8n };
