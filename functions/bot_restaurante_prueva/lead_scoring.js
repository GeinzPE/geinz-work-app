// ============================================================
// lead_scoring.js
// Calificación de Leads 100% DETERMINISTA — cero llamadas a IA, cero
// tokens gastados. Todo se decide por código en base a:
//   1) la RAMA que devolvió el Router (clasificarRama() en dispensador.js)
//   2) el cart_total real, calculado sumando precio × cantidad del carrito
//
// Se integra en 2 puntos de tu arquitectura:
//   - actualizarLeadStatusPorRama()   → se llama 1 vez por mensaje, justo
//     después de despacharRama() en el webhook (telegram/whatsapp/messenger).
//   - recalcularCartTotalYLead()      → se llama cada vez que el carrito
//     cambia de verdad (agregar/quitar/vaciar), ya sea por IA (texto/audio)
//     o por un tap de botón (callback_query, sin IA). Ver carrito_store.js.
//
// El objeto User/Chat vive en:
//   Tiendas/.../historial_chats/{canal}/usuarios/{numero_usuario}
// y ahora trae, además de lo que ya guardabas (contexto, tokens_totales_
// historicos), estos 4 campos nuevos:
//   - lead_status              (string, ver LEAD_STATUS)
//   - cart_total                (number, soles)
//   - last_interaction_at       (Timestamp)
//   - abandoned_cart_notified   (boolean)
// ============================================================

const admin = require("firebase-admin");
const {
  usuarioDocRef,
  monitorLeadsRef,
  RUTA_TIENDA_STR,
} = require("./tienda_paths.js");

if (!admin.apps.length) {
  admin.initializeApp();
}
const db = admin.firestore();

const LEAD_STATUS = Object.freeze({
  FRIO: "LEAD_FRIO",
  TIBIO: "LEAD_TIBIO",
  CALIENTE: "LEAD_CALIENTE",
  PENDIENTE_PAGO: "PENDIENTE_PAGO",
  CARRITO_ABANDONADO: "CARRITO_ABANDONADO",
  RECLAMO: "RECLAMO",
  ATENCION_HUMANA: "ATENCION_HUMANA",
});

// 👇 Rama (tal cual la devuelve TU clasificarRama() real en dispensador.js,
// que usa nombres en minúscula/snake_case) -> lead_status por defecto.
// Mapeo pedido: NEGOCIO/CARTA_VISUAL/GENERAL -> FRIO, BUSQUEDA -> TIBIO,
// PEDIDOS_CARRITO -> CALIENTE, PAGO_VOUCHER -> PENDIENTE_PAGO, RECLAMOS -> RECLAMO.
const MAPEO_RAMA_LEAD_STATUS = Object.freeze({
  general: LEAD_STATUS.FRIO,
  negocio: LEAD_STATUS.FRIO,
  carta_visual: LEAD_STATUS.FRIO,
  busqueda_algolia: LEAD_STATUS.TIBIO,
  pedidos_carrito: LEAD_STATUS.CALIENTE,
  pagos_voucher: LEAD_STATUS.PENDIENTE_PAGO,
  reclamos: LEAD_STATUS.RECLAMO,
});

// Estados que un simple cambio de rama o una edición de carrito NUNCA
// deben pisar. Solo salen de aquí por una acción explícita (ej. el asesor
// resuelve el reclamo y lo cambia a mano, o Gemini en reclamos.js decide
// humano:false en un turno posterior — ver la excepción más abajo).
const STATUSES_PROTEGIDOS = [LEAD_STATUS.RECLAMO, LEAD_STATUS.ATENCION_HUMANA];

const EMOJI_POR_STATUS = {
  [LEAD_STATUS.FRIO]: "❄️",
  [LEAD_STATUS.TIBIO]: "🌤️",
  [LEAD_STATUS.CALIENTE]: "🔥",
  [LEAD_STATUS.PENDIENTE_PAGO]: "💳",
  [LEAD_STATUS.CARRITO_ABANDONADO]: "⏰🛒",
  [LEAD_STATUS.RECLAMO]: "⚠️",
  [LEAD_STATUS.ATENCION_HUMANA]: "🆘",
};

const NOMBRE_LEGIBLE_STATUS = {
  [LEAD_STATUS.FRIO]: "LEAD FRÍO",
  [LEAD_STATUS.TIBIO]: "LEAD TIBIO",
  [LEAD_STATUS.CALIENTE]: "LEAD CALIENTE",
  [LEAD_STATUS.PENDIENTE_PAGO]: "PENDIENTE DE PAGO",
  [LEAD_STATUS.CARRITO_ABANDONADO]: "CARRITO ABANDONADO",
  [LEAD_STATUS.RECLAMO]: "RECLAMO",
  [LEAD_STATUS.ATENCION_HUMANA]: "ATENCIÓN HUMANA",
};

// Solo estos estados justifican avisarle al GEINZ Monitor en tiempo real
// (no tiene sentido spamear el monitor en cada mensaje "frío").
const STATUSES_QUE_EMITEN_MONITOR = [
  LEAD_STATUS.CALIENTE,
  LEAD_STATUS.PENDIENTE_PAGO,
  LEAD_STATUS.CARRITO_ABANDONADO,
  LEAD_STATUS.RECLAMO,
  LEAD_STATUS.ATENCION_HUMANA,
];

function construirDisplayTag(lead_status, cart_total) {
  const emoji = EMOJI_POR_STATUS[lead_status] || "•";
  const nombre = NOMBRE_LEGIBLE_STATUS[lead_status] || lead_status;
  const montoTexto =
    typeof cart_total === "number" && cart_total > 0
      ? ` - S/ ${cart_total.toFixed(2)}`
      : "";
  return `${emoji} ${nombre}${montoTexto}`;
}

/**
 * Escribe/actualiza el doc que escucha el GEINZ Monitor. Un onSnapshot en
 * monitor_leads/{numero_usuario} desde el front funciona igual que un
 * WebSocket: el monitor recibe el cambio al instante, sin polling.
 *
 * Si además tienes tu propio servidor de WebSocket, configura la env var
 * GEINZ_MONITOR_WEBHOOK_URL y el mismo payload se reenvía por POST.
 */
async function emitirEventoMonitor({
  canal,
  numero_usuario,
  nombre_usuario,
  lead_status,
  cart_total,
}) {
  const payloadBase = {
    user_id: numero_usuario,
    canal,
    name: nombre_usuario || "Usuario",
    lead_status,
    cart_total: Number(cart_total || 0),
    display_tag: construirDisplayTag(lead_status, cart_total),
  };

  console.log(
    "[lead_scoring] 📡 Emitiendo evento al GEINZ Monitor:",
    JSON.stringify(payloadBase),
  );

  try {
    await monitorLeadsRef(db)
      .doc(numero_usuario)
      .set(
        { ...payloadBase, updated_at: admin.firestore.FieldValue.serverTimestamp() },
        { merge: true },
      );
  } catch (err) {
    console.error("[lead_scoring] ❌ Error escribiendo en monitor_leads:", err.message);
  }

  const webhookUrl = process.env.GEINZ_MONITOR_WEBHOOK_URL;
  if (webhookUrl) {
    try {
      await fetch(webhookUrl, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ ...payloadBase, updated_at: new Date().toISOString() }),
      });
    } catch (err) {
      console.error("[lead_scoring] ❌ Error notificando webhook del monitor:", err.message);
    }
  }
}

/** Suma precio × cantidad de un carrito. Determinista, cero IA. */
function calcularCartTotal(items) {
  if (!Array.isArray(items)) return 0;
  return items.reduce(
    (acc, i) => acc + Number(i.precio || 0) * Number(i.cantidad || 1),
    0,
  );
}

/**
 * PUNTO 2 del pedido — se llama UNA vez por mensaje, justo después de que
 * el Router clasificó la rama y esa rama ya respondió. Actualiza
 * lead_status según el mapeo fijo, con las excepciones de negocio:
 *   - reclamos con humano:true (lo decide reclamos.js)  -> ATENCION_HUMANA
 *   - cart_total > 0 tocando carrito/catálogo (punto 3) -> se fuerza CALIENTE
 *   - nunca pisa RECLAMO / ATENCION_HUMANA con un cambio de rama normal
 *
 * @param {Object} params
 * @param {"telegram"|"whatsapp"|"messenger"} params.canal
 * @param {string} params.numero_usuario
 * @param {string} [params.nombre_usuario]
 * @param {string} params.rama - rama devuelta por clasificarRama()
 * @param {Object} [params.resultadoRama] - lo que devolvió despacharRama() (para leer "humano" en reclamos)
 * @param {number} [params.cart_total_actual] - pásalo si ya lo tienes a mano; si no, se lee del doc de usuario
 * @returns {Promise<{lead_status: string, cart_total: number}>}
 */
async function actualizarLeadStatusPorRama({
  canal,
  numero_usuario,
  nombre_usuario,
  rama,
  resultadoRama,
  cart_total_actual,
}) {
  const ref = usuarioDocRef(db, canal, numero_usuario);
  const snap = await ref.get();
  const dataActual = snap.exists ? snap.data() : {};
  const statusActual = dataActual.lead_status || null;

  const cart_total =
    typeof cart_total_actual === "number"
      ? cart_total_actual
      : Number(dataActual.cart_total || 0);

  let nuevoStatus = MAPEO_RAMA_LEAD_STATUS[rama] || LEAD_STATUS.FRIO;

  if (rama === "reclamos" && resultadoRama?.humano === true) {
    nuevoStatus = LEAD_STATUS.ATENCION_HUMANA;
  }

  const ramasQueCalientanConCarritoLleno = ["busqueda_algolia", "pedidos_carrito"];
  if (cart_total > 0 && ramasQueCalientanConCarritoLleno.includes(rama)) {
    nuevoStatus = LEAD_STATUS.CALIENTE;
  }

  if (
    STATUSES_PROTEGIDOS.includes(statusActual) &&
    nuevoStatus !== LEAD_STATUS.ATENCION_HUMANA
  ) {
    console.log(
      "[lead_scoring] Status protegido (", statusActual, ") no se pisa por rama:", rama,
    );
    nuevoStatus = statusActual;
  }

  const huboCambio = nuevoStatus !== statusActual;

  await ref.set(
    {
      numero_usuario,
      canal,
      nombre_usuario: nombre_usuario || dataActual.nombre_usuario || "Usuario",
      lead_status: nuevoStatus,
      cart_total,
      last_interaction_at: admin.firestore.FieldValue.serverTimestamp(),
      abandoned_cart_notified: false,
      ruta_tienda: RUTA_TIENDA_STR,
      updated_at: admin.firestore.FieldValue.serverTimestamp(),
    },
    { merge: true },
  );

  console.log(
    "[lead_scoring] lead_status |", numero_usuario,
    "| rama:", rama,
    "| antes:", statusActual, "-> ahora:", nuevoStatus,
    "| cart_total:", cart_total,
  );

  if (huboCambio && STATUSES_QUE_EMITEN_MONITOR.includes(nuevoStatus)) {
    await emitirEventoMonitor({ canal, numero_usuario, nombre_usuario, lead_status: nuevoStatus, cart_total });
  }

  return { lead_status: nuevoStatus, cart_total };
}

/**
 * PUNTO 3 del pedido — se llama CADA VEZ que el carrito cambia de verdad:
 * se agrega, se quita, o se vacía un producto (por IA en texto/audio, o
 * por un tap de botón sin IA). Recalcula cart_total 100% por código y
 * fuerza LEAD_CALIENTE si quedó plata en el carrito.
 *
 * @param {Object} params
 * @param {"telegram"|"whatsapp"|"messenger"} params.canal
 * @param {string} params.numero_usuario
 * @param {string} [params.nombre_usuario]
 * @param {Array<{precio:number, cantidad:number}>} params.items - carrito YA actualizado
 * @returns {Promise<{lead_status: string, cart_total: number}>}
 */
async function recalcularCartTotalYLead({ canal, numero_usuario, nombre_usuario, items }) {
  const cart_total = calcularCartTotal(items);
  const ref = usuarioDocRef(db, canal, numero_usuario);
  const snap = await ref.get();
  const dataActual = snap.exists ? snap.data() : {};
  const statusActual = dataActual.lead_status || null;

  let nuevoStatus = statusActual || LEAD_STATUS.TIBIO;

  if (cart_total > 0 && !STATUSES_PROTEGIDOS.includes(statusActual)) {
    nuevoStatus = LEAD_STATUS.CALIENTE;
  }
  // Si el carrito quedó en 0 (se vació), NO se toca el lead_status aquí a
  // propósito: se queda como estaba hasta que llegue el próximo mensaje y
  // actualizarLeadStatusPorRama() lo re-evalúe según la rama nueva.

  console.log(
    "[lead_scoring] recalcularCartTotalYLead |", numero_usuario,
    "| cart_total:", cart_total.toFixed(2),
    "| status:", statusActual, "->", nuevoStatus,
  );

  await ref.set(
    {
      numero_usuario,
      canal,
      nombre_usuario: nombre_usuario || dataActual.nombre_usuario || "Usuario",
      cart_total,
      lead_status: nuevoStatus,
      last_interaction_at: admin.firestore.FieldValue.serverTimestamp(),
      abandoned_cart_notified: false,
      ruta_tienda: RUTA_TIENDA_STR,
      updated_at: admin.firestore.FieldValue.serverTimestamp(),
    },
    { merge: true },
  );

  if (STATUSES_QUE_EMITEN_MONITOR.includes(nuevoStatus)) {
    await emitirEventoMonitor({ canal, numero_usuario, nombre_usuario, lead_status: nuevoStatus, cart_total });
  }

  return { lead_status: nuevoStatus, cart_total };
}

module.exports = {
  LEAD_STATUS,
  MAPEO_RAMA_LEAD_STATUS,
  calcularCartTotal,
  actualizarLeadStatusPorRama,
  recalcularCartTotalYLead,
  emitirEventoMonitor,
  construirDisplayTag,
};