"use strict";

const { onRequest } = require("firebase-functions/v2/https");
const { initDb2 } = require("./_shared");

const obtenerCategorias_datas = onRequest(
  {
    region: "us-central1",
    timeoutSeconds: 120,
    memory: "256MiB",
    cors: true,
  },
  async (req, res) => {
    res.set("Access-Control-Allow-Origin", "*");
    res.set("Access-Control-Allow-Methods", "GET, OPTIONS");
    res.set("Access-Control-Allow-Headers", "Content-Type");

    if (req.method === "OPTIONS") {
      return res.status(204).send("");
    }

    console.log("[obtenerCategorias] ── Inicio de request ──");

    try {
      const db = initDb2();
      console.log(
        "[obtenerCategorias] initDb2() devolvió:",
        db ? "OK (instancia válida)" : "NULL",
      );

      if (!db) {
        console.error("[obtenerCategorias] db es null, abortando.");
        return res.status(500).json({
          ok: false,
          error: "No se pudo conectar a la base de datos.",
        });
      }

      console.log("[obtenerCategorias] Consultando: precio_apartado/scag_site");
      const docRef = db.collection("precio_apartado").doc("scag_site");
      const snap = await docRef.get();

      console.log("[obtenerCategorias] snap.exists:", snap.exists);
      console.log("[obtenerCategorias] snap.id:", snap.id);
      console.log("[obtenerCategorias] snap.ref.path:", snap.ref.path);

      if (!snap.exists) {
        console.warn(
          "[obtenerCategorias] El documento no existe en esta ruta.",
        );
        return res.status(404).json({
          ok: false,
          error: "No se encontró el documento de categorías.",
        });
      }

      const docData = snap.data() || {};
      const categorias = docData.categoria || {};
      console.log("[obtenerCategorias] campos del doc:", Object.keys(docData));
      console.log(
        "[obtenerCategorias] categorias:",
        JSON.stringify(categorias),
      );
      console.log(
        "[obtenerCategorias] cantidad de categorias:",
        Object.keys(categorias).length,
      );

      return res.status(200).json({
        ok: true,
        categorias,
      });
    } catch (err) {
      console.error("[obtenerCategorias] Error:", err.message);
      console.error("[obtenerCategorias] Stack:", err.stack);
      return res.status(500).json({
        ok: false,
        error: err.message || "Error interno del servidor.",
      });
    }
  },
);

module.exports = { obtenerCategorias_datas };
