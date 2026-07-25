const admin = require("firebase-admin");
const db = admin.firestore();

const COLECCIONES_POR_CANAL = {
  whatsapp: "historial_whatsapp",
  telegram: "historial_telegram",
};

/**
 * Guarda un mensaje (del usuario o del bot) en el historial de conversación.
 * Nunca lanza error hacia arriba: si falla el guardado, solo lo loguea,
 * para que jamás rompa el flujo real del bot.
 *
 * @param {Object} params
 * @param {"whatsapp"|"telegram"} params.canal - canal de origen (obligatorio)
 * @param {string} params.numero_usuario   - wa_id o chat_id del usuario (obligatorio)
 * @param {string} [params.nombre_usuario] - nombre del usuario si se conoce
 * @param {"usuario"|"bot"} params.remitente
 * @param {string} params.tipo             - "texto"|"audio"|"imagen"|"sticker"|
 *                                            "documento"|"video"|"ubicacion"|
 *                                            "contacto"|"url"|"plantilla"
 * @param {string} [params.contenido]      - texto del mensaje / caption / descripción
 * @param {string} [params.mensaje_id]     - id del mensaje (si aplica)
 * @param {string} [params.categoria]      - categoría clasificada por la IA (si aplica)
 * @param {Object|string} [params.extra]   - cualquier dato extra útil para depurar
 */
async function guardarMensajeHistorial({
  canal,
  numero_usuario,
  nombre_usuario,
  remitente,
  tipo,
  contenido = "",
  mensaje_id = null,
  categoria = null,
  extra = null,
}) {
  const coleccion = COLECCIONES_POR_CANAL[canal];
  if (!coleccion) {
    console.error("❌ [guardarMensajeHistorial] canal inválido o faltante:", canal);
    return;
  }
  if (!numero_usuario) {
    console.error("❌ [guardarMensajeHistorial] Falta numero_usuario");
    return;
  }
  if (remitente !== "usuario" && remitente !== "bot") {
    console.error(
      "❌ [guardarMensajeHistorial] remitente inválido:",
      remitente,
    );
    return;
  }

  try {
    const refUsuario = db.collection(coleccion).doc(numero_usuario);
    const refMensaje = refUsuario.collection("mensajes").doc();

    const contenidoRecortado = (contenido || "").toString().slice(0, 4000);

    const batch = db.batch();

    batch.set(
      refUsuario,
      {
        numero_usuario,
        ...(nombre_usuario ? { nombre_usuario } : {}),
        ultima_actividad: admin.firestore.FieldValue.serverTimestamp(),
        ultimo_mensaje: {
          remitente,
          tipo,
          contenido: contenidoRecortado.slice(0, 300),
        },
      },
      { merge: true },
    );

    batch.set(refMensaje, {
      remitente,
      tipo,
      contenido: contenidoRecortado,
      mensaje_id: mensaje_id || null,
      categoria: categoria || null,
      extra: extra ?? null,
      timestamp: admin.firestore.FieldValue.serverTimestamp(),
    });

    await batch.commit();
  } catch (e) {
    console.error(
      "❌ [guardarMensajeHistorial] Error guardando historial:",
      e.message,
      "| canal:", canal,
      "| 👤:",
      numero_usuario,
      "| tipo:",
      tipo,
    );
  }
}

/**
 * Trae los últimos N mensajes de un usuario, ordenados cronológicamente
 * (más antiguo primero), listo para pintar en una app de conversación.
 */
async function obtenerHistorialUsuario(canal, numero_usuario, limite = 100) {
  const coleccion = COLECCIONES_POR_CANAL[canal];
  if (!coleccion) {
    console.error("❌ [obtenerHistorialUsuario] canal inválido:", canal);
    return [];
  }
  const snap = await db
    .collection(coleccion)
    .doc(numero_usuario)
    .collection("mensajes")
    .orderBy("timestamp", "desc")
    .limit(limite)
    .get();

  return snap.docs.map((d) => ({ id: d.id, ...d.data() })).reverse();
}

module.exports = { guardarMensajeHistorial, obtenerHistorialUsuario };