// ============================================================
// lead_scoring_cron.js
// PUNTO 4 del pedido — Cloud Scheduler (Cloud Function programada) que
// revisa TODOS los chats de esta tienda (cualquier canal: telegram,
// whatsapp, messenger) que estén en LEAD_TIBIO o LEAD_CALIENTE con
// cart_total > 0, y no hayan escrito en más de 15 minutos. A esos los pasa
// a CARRITO_ABANDONADO y avisa al GEINZ Monitor.
//
// Corre cada 5 minutos (suficiente resolución para un umbral de 15 min).
//
// 👉 Regístralo en tu index.js:
//   const { revisarCarritosAbandonados } = require("./lead_scoring_cron.js");
//   exports.revisarCarritosAbandonados = revisarCarritosAbandonados;
//
// 👉 Requiere un índice compuesto (Firestore te va a dar el link exacto la
// primera vez que corra si falta) para esta collectionGroup query sobre
// "usuarios": ruta_tienda (==) + lead_status (in) + cart_total (>).
// ============================================================

const { onSchedule } = require("firebase-functions/v2/scheduler");
const admin = require("firebase-admin");
const { RUTA_TIENDA_STR } = require("./tienda_paths.js");
const { LEAD_STATUS, emitirEventoMonitor } = require("./lead_scoring.js");

if (!admin.apps.length) {
  admin.initializeApp();
}
const db = admin.firestore();

const MINUTOS_PARA_ABANDONO = 15;

exports.revisarCarritosAbandonados = onSchedule(
  { schedule: "every 5 minutes", timeZone: "America/Lima" },
  async () => {
    const ahora = Date.now();
    const limiteMs = MINUTOS_PARA_ABANDONO * 60 * 1000;

    console.log("[lead_scoring_cron] === Revisando carritos abandonados ===");

    // collectionGroup("usuarios") porque "usuarios" se repite bajo cada
    // canal (historial_chats/telegram/usuarios, historial_chats/whatsapp/usuarios,
    // etc.) — ruta_tienda filtra para que solo mire esta tienda.
    const snap = await db
      .collectionGroup("usuarios")
      .where("ruta_tienda", "==", RUTA_TIENDA_STR)
      .where("lead_status", "in", [LEAD_STATUS.TIBIO, LEAD_STATUS.CALIENTE])
      .where("cart_total", ">", 0)
      .get();

    console.log("[lead_scoring_cron] Candidatos a revisar:", snap.size);

    let marcados = 0;

    for (const doc of snap.docs) {
      const data = doc.data();
      const lastInteraction = data.last_interaction_at?.toDate
        ? data.last_interaction_at.toDate().getTime()
        : 0;

      const inactivoPorMs = ahora - lastInteraction;
      if (inactivoPorMs < limiteMs) continue;

      console.log(
        "[lead_scoring_cron] ⏰ Marcando abandonado:", data.numero_usuario,
        "| canal:", data.canal,
        "| inactivo por (min):", Math.round(inactivoPorMs / 60000),
        "| cart_total:", data.cart_total,
      );

      await doc.ref.set(
        {
          lead_status: LEAD_STATUS.CARRITO_ABANDONADO,
          abandoned_cart_notified: true,
          updated_at: admin.firestore.FieldValue.serverTimestamp(),
        },
        { merge: true },
      );

      await emitirEventoMonitor({
        canal: data.canal,
        numero_usuario: data.numero_usuario,
        nombre_usuario: data.nombre_usuario,
        lead_status: LEAD_STATUS.CARRITO_ABANDONADO,
        cart_total: data.cart_total,
      });

      marcados++;
    }

    console.log("[lead_scoring_cron] === Terminado | marcados:", marcados, "===");
  },
);