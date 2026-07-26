"use strict";

const { onRequest } = require("firebase-functions/v2/https");
const { initDb2 } = require("./_shared");

const obtenerConsultaPendiente = onRequest(
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

      const alias = req.query.alias || req.body.alias;

      if (!alias) {
        return res.status(400).json({
          ok: false,
          error: "Falta el parámetro 'alias'.",
        });
      }

      // 1. Buscar el documento en bot_scag usando el alias recibido como ID
      const buscarAliasRef = db.collection("bot_scag").doc(alias);
      const buscarAliasSnap = await buscarAliasRef.get();

      if (!buscarAliasSnap.exists) {
        return res.status(404).json({
          ok: false,
          error: "No se encontró el alias en bot_scag.",
        });
      }

      const aliasEncontrado = buscarAliasSnap.data().alias;

      if (!aliasEncontrado) {
        return res.status(404).json({
          ok: false,
          error: "El documento en bot_scag no tiene el campo 'alias'.",
        });
      }

      // 2. Usar ese alias encontrado para buscar la consulta pendiente
      const docRef = db
        .collection("trabajos_ia")
        .doc(aliasEncontrado)
        .collection("consultas")
        .doc("pendiente");

      const docSnap = await docRef.get();

      if (!docSnap.exists) {
        return res.status(404).json({
          ok: false,
          error: "No se encontró una consulta pendiente para ese alias.",
        });
      }

      return res.status(200).json({
        ok: true,
        message: "Consulta pendiente obtenida correctamente.",
        data: docSnap.data(),
      });
    } catch (error) {
      console.error("Error obteniendo consulta pendiente:", error);
      return res.status(500).json({
        ok: false,
        error: "Error interno al obtener la consulta pendiente.",
      });
    }
  },
);

module.exports = { obtenerConsultaPendiente };
