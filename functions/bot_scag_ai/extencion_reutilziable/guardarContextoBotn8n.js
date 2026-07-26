"use strict";

const { onRequest } = require("firebase-functions/v2/https");
const { initDb2 } = require("./_shared");

const guardarContextoBotn8n = onRequest(
  {
    region: "us-central1",
    timeoutSeconds: 60,
    memory: "256MiB",
    cors: true,
  },
  async (req, res) => {
    try {
      const db = initDb2();
      if (!db) {
        return res.status(500).json({
          ok: false,
          error: "No se pudo conectar a la base de datos.",
        });
      }

      const { alias, context } = req.body;

      if (!alias) {
        return res.status(400).json({
          ok: false,
          error: "Falta el parámetro 'alias'.",
        });
      }

      if (!context || typeof context !== "string") {
        return res.status(400).json({
          ok: false,
          error: "Falta el parámetro 'context' o no es un string válido.",
        });
      }

      const aliasLimpio = String(alias).trim().toLowerCase();

      await db.collection("trabajos_ia").doc(aliasLimpio).set(
        {
          context_bot: context,
          context_bot_fecha: new Date().toISOString(),
        },
        { merge: true },
      );

      return res.status(200).json({
        ok: true,
        message: "Contexto guardado correctamente.",
        alias: aliasLimpio,
        context,
      });
    } catch (error) {
      console.error("Error guardando contexto del bot:", error);
      return res.status(500).json({
        ok: false,
        error: "Error interno al guardar el contexto.",
      });
    }
  },
);

module.exports = { guardarContextoBotn8n };
