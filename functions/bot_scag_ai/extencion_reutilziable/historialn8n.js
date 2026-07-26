"use strict";

const { onRequest } = require("firebase-functions/v2/https");
const { Timestamp } = require("firebase-admin/firestore");
const { initDb2 } = require("./_shared");

const historialn8n = onRequest(
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

      // ── Alias del usuario (body o query) ──────────────────────────────────
      const alias = req.body?.alias || req.query?.alias;
      if (!alias) {
        return res
          .status(400)
          .json({ ok: false, error: "Falta el parámetro 'alias'" });
      }

      // ── Rango: últimos 7 días ──────────────────────────────────────────────
      const ahora = new Date();
      const hace7Dias = new Date(ahora);
      hace7Dias.setDate(ahora.getDate() - 7);
      const desde = Timestamp.fromDate(hace7Dias);

      // ── Leer colección historial ───────────────────────────────────────────
      const snap = await db
        .collection("trabajos_ia")
        .doc(alias)
        .collection("historial")
        .where("fecha", ">=", desde)
        .orderBy("fecha", "desc")
        .get();

      if (snap.empty) {
        return res.status(200).json({
          ok: true,
          mensaje: "Sin registros en los últimos 7 días",
          resumen: null,
        });
      }

      // ── Acumuladores ───────────────────────────────────────────────────────
      let totalDocs = 0;
      let creditosConsumidosTotal = 0;
      let creditosAntesInicial = null;
      let creditosRestantesActual = null;

      const categorias = {};
      const modelos = {};
      const modos = {};
      const tipos = {};

      snap.forEach((doc, idx) => {
        const d = doc.data();
        totalDocs++;

        if (idx === 0) creditosRestantesActual = d.creditosRestantes ?? null;
        creditosAntesInicial = d.creditosAntes ?? null;
        creditosConsumidosTotal += d.creditosConsumidos ?? 0;

        if (d.categoria)
          categorias[d.categoria] = (categorias[d.categoria] ?? 0) + 1;
        if (d.modelo) modelos[d.modelo] = (modelos[d.modelo] ?? 0) + 1;
        if (d.solutionMode)
          modos[d.solutionMode] = (modos[d.solutionMode] ?? 0) + 1;
        if (d.tipo) tipos[d.tipo] = (tipos[d.tipo] ?? 0) + 1;
      });

      const ordenarDesc = (obj) =>
        Object.fromEntries(Object.entries(obj).sort(([, a], [, b]) => b - a));

      const resumen = {
        periodo: {
          desde: hace7Dias.toISOString(),
          hasta: ahora.toISOString(),
        },
        totalConsultas: totalDocs,
        creditos: {
          creditosAntesDelPeriodo: creditosAntesInicial,
          creditosRestantesActuales: creditosRestantesActual,
          creditosConsumidosTotales: creditosConsumidosTotal,
        },
        categorias: ordenarDesc(categorias),
        modelos: ordenarDesc(modelos),
        modos: ordenarDesc(modos),
        tipos: ordenarDesc(tipos),
      };

      return res.status(200).json({ ok: true, resumen });
    } catch (err) {
      console.error("historialn8n error:", err);
      return res.status(500).json({ ok: false, error: err.message });
    }
  },
);

module.exports = { historialn8n };
