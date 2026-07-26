"use strict";

const { onRequest } = require("firebase-functions/v2/https");
const { initDb2 } = require("./_shared");

const guardar_contacto_user = onRequest(
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

      const { uid, facturacionEmail, facturacionTelefono } = req.body || {};

      // Validamos que venga el identificador del usuario
      if (!uid || typeof uid !== "string") {
        return res.status(400).json({
          ok: false,
          error: "El parámetro 'uid' es requerido y debe ser un string.",
        });
      }

      // Validamos que, si vienen, facturacionEmail y facturacionTelefono sean strings (o null)
      const emailValido =
        facturacionEmail === null ||
        facturacionEmail === undefined ||
        typeof facturacionEmail === "string";

      const telefonoValido =
        facturacionTelefono === null ||
        facturacionTelefono === undefined ||
        typeof facturacionTelefono === "string";

      if (!emailValido || !telefonoValido) {
        return res.status(400).json({
          ok: false,
          error:
            "facturacionEmail y facturacionTelefono deben ser string o null.",
        });
      }

      // Si ambos son null/undefined, no hay nada que guardar
      if (
        (facturacionEmail === null || facturacionEmail === undefined) &&
        (facturacionTelefono === null || facturacionTelefono === undefined)
      ) {
        return res.status(400).json({
          ok: false,
          error:
            "Debes enviar al menos uno de los dos: facturacionEmail o facturacionTelefono.",
        });
      }

      // Armamos el objeto a actualizar solo con los campos que llegaron definidos
      const datosActualizar = {};

      if (facturacionEmail !== undefined) {
        datosActualizar.facturacionEmail =
          facturacionEmail === null ? null : facturacionEmail.trim();
      }

      if (facturacionTelefono !== undefined) {
        datosActualizar.facturacionTelefono =
          facturacionTelefono === null ? null : facturacionTelefono.trim();
      }

      // Guardamos en la base de datos (merge para no pisar otros campos del documento)
      await db
        .collection("trabajos_ia")
        .doc(uid)
        .set(datosActualizar, { merge: true });

      return res.status(200).json({
        ok: true,
        mensaje: "Datos de facturación guardados correctamente.",
        data: datosActualizar,
      });
    } catch (error) {
      console.error("Error en guardar_contacto_user:", error);
      return res.status(500).json({
        ok: false,
        error: "Ocurrió un error al guardar los datos de facturación.",
      });
    }
  },
);

module.exports = { guardar_contacto_user };
