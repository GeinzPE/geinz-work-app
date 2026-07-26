"use strict";

const { onRequest } = require("firebase-functions/v2/https");
const { initDb2 } = require("./_shared"); // ajusta la ruta si tu initDb2 vive en otro archivo

// ============================================================
// FUNCIÓN REUTILIZABLE (esta es la que importas en otros archivos)
// No es un endpoint HTTP, es una función normal de JS: la puedes
// llamar con await desde cualquier parte de tu backend.
// ============================================================
async function obtenerPreciosPlanes() {
  const db = initDb2();
  const col = db.collection("precios_planes_scag");

  const [avanzado, basico, medio, pro, proultra] = await Promise.all([
    col.doc("avanzado").get(),
    col.doc("basico").get(),
    col.doc("medio").get(),
    col.doc("pro").get(),
    col.doc("proultra").get(),
  ]);

  const extract = (snap) => {
    if (!snap.exists) return { creditos: null, precio: null, nombre: null };
    const { creditos, precio, nombre } = snap.data();
    return { creditos, precio, nombre };
  };

  return {
    avanzado: extract(avanzado),
    basico: extract(basico),
    medio: extract(medio),
    pro: extract(pro),
    proultra: extract(proultra),
  };
}

// ============================================================
// ENDPOINT HTTP (para n8n u otros consumidores externos)
// Es solo una envoltura delgada sobre obtenerPreciosPlanes().
// Si nada externo lo necesita, puedes borrar este bloque y
// dejar solo la función de arriba.
// ============================================================
const getPreciosPlanes = onRequest(
  { region: "us-central1", cors: true },
  async (req, res) => {
    try {
      const data = await obtenerPreciosPlanes();
      return res.status(200).json({ success: true, data });
    } catch (error) {
      console.error("getPreciosPlanes error:", error);
      return res.status(500).json({ success: false, error: error.message });
    }
  },
);

module.exports = {
  obtenerPreciosPlanes, // ← úsala dentro de tu código (rutaRecarga, etc.)
  getPreciosPlanes, // ← este sigue siendo el endpoint HTTP, por si n8n lo sigue llamando
};