"use strict";

const { onRequest } = require("firebase-functions/v2/https");
const { initDb2 } = require("./_shared");

const guardarConsultaPendiente = onRequest(
  {
    region: "us-central1",
    timeoutSeconds: 120,
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

      const {
        alias,
        provider,
        category,
        solutionMode,
        imageBase64,
        mimeType,
        textHint,
      } = req.body;

      if (!alias) {
        return res.status(400).json({
          ok: false,
          error: "Falta el parámetro 'alias'.",
        });
      }

      const data = {
        alias: alias,
        provider: provider || null,
        category: category || null,
        solutionMode: solutionMode || null,
        imageBase64: imageBase64 || null,
        mimeType: mimeType || null,
        textHint: textHint || null,
        fecha: new Date().toISOString(),
      };

      await db
        .collection("trabajos_ia")
        .doc(alias)
        .collection("consultas")
        .doc("pendiente")
        .set(data);

      return res.status(200).json({
        ok: true,
        message: "Consulta pendiente guardada correctamente.",
        data,
      });
    } catch (error) {
      console.error("Error guardando consulta pendiente:", error);
      return res.status(500).json({
        ok: false,
        error: "Error interno al guardar la consulta pendiente.",
      });
    }
  },
);

module.exports = { guardarConsultaPendiente };
