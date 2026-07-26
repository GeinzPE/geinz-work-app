"use strict";

const { onRequest } = require("firebase-functions/v2/https");
const { initDb2 } = require("./_shared");

const obtener_contacto_user = onRequest(
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

      // Aceptamos el uid tanto por query (?uid=...) como por body (para GET o POST)
      const uid = (req.query && req.query.uid) || (req.body && req.body.uid);

      if (!uid || typeof uid !== "string") {
        return res.status(400).json({
          ok: false,
          error: "El parámetro 'uid' es requerido y debe ser un string.",
        });
      }

      const docRef = db.collection("trabajos_ia").doc(uid);
      const docSnap = await docRef.get();

      if (!docSnap.exists) {
        return res.status(404).json({
          ok: false,
          error: "No se encontró el documento para el uid proporcionado.",
        });
      }

      const data = docSnap.data() || {};

      return res.status(200).json({
        ok: true,
        data: {
          facturacionEmail:
            data.facturacionEmail !== undefined ? data.facturacionEmail : null,
          facturacionTelefono:
            data.facturacionTelefono !== undefined
              ? data.facturacionTelefono
              : null,
        },
      });
    } catch (error) {
      console.error("Error en obtener_contacto_user:", error);
      return res.status(500).json({
        ok: false,
        error: "Ocurrió un error al obtener los datos de facturación.",
      });
    }
  },
);

module.exports = { obtener_contacto_user };
