// ============================================================
// tienda_paths.js
// Helpers centralizados para construir las referencias de Firestore de
// ESTA tienda (barranca). Si mañana manejas más de una tienda, este es el
// único archivo que hay que tocar (o convertir RUTA_TIENDA en parámetro
// de función en vez de constante).
//
// Estructura real en Firestore:
//   Tiendas/barranca/barranca/TQmS5RKaSDdKmqPGMUXk
//     ├── carta/                              (ya existía, ver carta_visual.js)
//     ├── carrito/{numero_usuario}             👈 NUEVO
//     │     antes vivía suelto en la colección "carritos" (pedido_carrito.js
//     │     y pago_vaucher.js). Ahora cuelga del doc de la tienda.
//     ├── historial_chats/{canal}/usuarios/{numero_usuario}/mensajes/{msgId}
//     │     canal = "telegram" | "whatsapp" | "messenger"
//     │     el doc "usuarios/{numero_usuario}" ES el objeto User/Chat:
//     │     trae contexto, tokens_totales_historicos, lead_status,
//     │     cart_total, last_interaction_at, abandoned_cart_notified, etc.
//     │     (reemplaza a la colección suelta "usuarios_telegram_dispensador")
//     └── monitor_leads/{numero_usuario}       👈 NUEVO
//           el GEINZ Monitor escucha este documento en tiempo real
//           (onSnapshot funciona como un WebSocket para el front).
// ============================================================

const RUTA_TIENDA = ["Tiendas", "barranca", "barranca", "TQmS5RKaSDdKmqPGMUXk"];

// String plano — se guarda en cada doc de usuario para poder filtrar con
// collectionGroup queries en el cron de carritos abandonados (ver
// lead_scoring_cron.js), ya que "usuarios" se repite bajo cada canal.
const RUTA_TIENDA_STR = RUTA_TIENDA.join("/");

function tiendaDocRef(db) {
  const [c1, d1, c2, d2] = RUTA_TIENDA;
  return db.collection(c1).doc(d1).collection(c2).doc(d2);
}

// ---- Carrito ----
function carritoCollectionRef(db) {
  return tiendaDocRef(db).collection("carrito");
}
function carritoDocRef(db, numero_usuario) {
  return carritoCollectionRef(db).doc(numero_usuario);
}

// ---- Historial / usuario por canal ----
const CANALES_VALIDOS = ["telegram", "whatsapp", "messenger"];

function normalizarCanal(canal) {
  const c = String(canal || "").toLowerCase();
  return CANALES_VALIDOS.includes(c) ? c : "desconocido";
}

/** Colección de usuarios/chats de UN canal específico. */
function usuariosCanalRef(db, canal) {
  return tiendaDocRef(db)
    .collection("historial_chats")
    .doc(normalizarCanal(canal))
    .collection("usuarios");
}

/** El doc de usuario/chat (User object): lead_status, cart_total, contexto, tokens, etc. */
function usuarioDocRef(db, canal, numero_usuario) {
  return usuariosCanalRef(db, canal).doc(numero_usuario);
}

/** Subcolección de mensajes reales de ese usuario. */
function mensajesUsuarioRef(db, canal, numero_usuario) {
  return usuarioDocRef(db, canal, numero_usuario).collection("mensajes");
}

// ---- Monitor GEINZ ----
function monitorLeadsRef(db) {
  return tiendaDocRef(db).collection("monitor_leads");
}

module.exports = {
  RUTA_TIENDA,
  RUTA_TIENDA_STR,
  CANALES_VALIDOS,
  normalizarCanal,
  tiendaDocRef,
  carritoCollectionRef,
  carritoDocRef,
  usuariosCanalRef,
  usuarioDocRef,
  mensajesUsuarioRef,
  monitorLeadsRef,
};