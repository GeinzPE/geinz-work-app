// ============================================================
// carrito_store.js
// Persistencia ÚNICA del carrito. Reemplaza la colección suelta
// "carritos" que usaban pedido_carrito.js y pago_vaucher.js.
//
// Ahora el carrito vive DENTRO del documento de la tienda:
//   Tiendas/barranca/barranca/TQmS5RKaSDdKmqPGMUXk/carrito/{numero_usuario}
//
// Regla de oro: TODO guardado de carrito pasa por guardarCarrito() de
// aquí, y ESE es el único punto que dispara recalcularCartTotalYLead()
// (lead_scoring.js). Así, sin importar si el cambio vino de la IA
// (agregar/eliminar por texto o audio) o de un botón (callback_query, cero
// IA), el cart_total y el lead_status SIEMPRE quedan sincronizados.
//
// 👉 Reemplaza en pedido_carrito.js y pago_vaucher.js el uso directo de
// `db.collection("carritos").doc(numero_usuario)` por los métodos de este
// archivo (ver INTEGRACION_LEAD_SCORING.md para el diff exacto).
// ============================================================

const admin = require("firebase-admin");
const crypto = require("node:crypto");
const { carritoDocRef } = require("./tienda_paths.js");
const { recalcularCartTotalYLead } = require("./lead_scoring.js");

if (!admin.apps.length) {
  admin.initializeApp();
}
const db = admin.firestore();

async function obtenerCarrito(numero_usuario) {
  const doc = await carritoDocRef(db, numero_usuario).get();
  return doc.exists ? doc.data().items || [] : [];
}

/**
 * @param {string} numero_usuario
 * @param {Array} items
 * @param {Object} opciones
 * @param {"telegram"|"whatsapp"|"messenger"} opciones.canal - OBLIGATORIO para que
 *   el lead scoring se actualice. Si no lo pasas, se guarda el carrito
 *   igual pero se loggea un warning y NO se toca lead_status/cart_total.
 * @param {string} [opciones.nombre_usuario]
 */
async function guardarCarrito(numero_usuario, items, opciones = {}) {
  const { canal, nombre_usuario } = opciones;

  await carritoDocRef(db, numero_usuario).set(
    { items, actualizado: admin.firestore.FieldValue.serverTimestamp() },
    { merge: true },
  );

  if (canal) {
    await recalcularCartTotalYLead({ canal, numero_usuario, nombre_usuario, items });
  } else {
    console.warn(
      "[carrito_store] guardarCarrito sin 'canal': el carrito se guardó pero NO se actualizó lead_status/cart_total. Pasa siempre { canal } desde el webhook.",
    );
  }
}

async function eliminarItemCarritoPorId(numero_usuario, itemId, opciones = {}) {
  const actual = await obtenerCarrito(numero_usuario);
  const nuevo = actual.filter((i) => i.id !== itemId);
  await guardarCarrito(numero_usuario, nuevo, opciones);
  return nuevo;
}

async function vaciarCarrito(numero_usuario, opciones = {}) {
  await guardarCarrito(numero_usuario, [], opciones);
  return [];
}

async function obtenerOCrearTokenCarrito(numero_usuario) {
  const ref = carritoDocRef(db, numero_usuario);
  const snap = await ref.get();
  const dataActual = snap.exists ? snap.data() : {};
  if (dataActual.token) return dataActual.token;
  const token = crypto.randomUUID();
  await ref.set({ token }, { merge: true });
  return token;
}

module.exports = {
  obtenerCarrito,
  guardarCarrito,
  eliminarItemCarritoPorId,
  vaciarCarrito,
  obtenerOCrearTokenCarrito,
};