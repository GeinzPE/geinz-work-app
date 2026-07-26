"use strict";

const admin = require("firebase-admin");

const BOT_SCAG_COLLECTION = "bot_scag";

// ⚠️ Debe ser la MISMA instancia de db2 que usa el resto de tu app.
// Si ya tienes initDb2() en functions_trabajo.js, lo más simple es
// importarlo desde ahí para no duplicar la conexión.
const { initDb2 } = require("../functions_trabajo");

// ============================================================
// Guardar contexto temporal (lógica pura, sin req/res)
// ============================================================
async function guardarContextoTemporal(numero, contexto_temporal) {
  const numeroLimpio = numero?.toString().trim();
  if (!numeroLimpio) {
    throw new Error("Número requerido.");
  }
  if (contexto_temporal === undefined) {
    throw new Error("contexto_temporal requerido.");
  }

  const database = initDb2();
  if (!database) {
    throw new Error("No se pudo conectar a la base de datos.");
  }

  const botRef = database.collection(BOT_SCAG_COLLECTION).doc(numeroLimpio);
  await botRef.set({ contexto_temporal }, { merge: true });

  return { ok: true, numero: numeroLimpio };
}

// ============================================================
// Leer contexto temporal (lógica pura, sin req/res)
// ============================================================
async function leerContextoTemporal(numero) {
  const numeroLimpio = numero?.toString().trim();
  if (!numeroLimpio) {
    throw new Error("Número requerido.");
  }

  const database = initDb2();
  if (!database) {
    throw new Error("No se pudo conectar a la base de datos.");
  }

  const botRef = database.collection(BOT_SCAG_COLLECTION).doc(numeroLimpio);
  const botSnap = await botRef.get();

  if (!botSnap.exists) {
    return { ok: true, contexto_temporal: null };
  }

  const contexto_temporal = botSnap.data()?.contexto_temporal ?? null;
  return { ok: true, contexto_temporal };
}

module.exports = {
  guardarContextoTemporal,
  leerContextoTemporal,
};