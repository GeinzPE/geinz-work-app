/**
 * historial_scag.js
 * ---------------------------------------------------------------
 * Guarda el historial de conversaciones del bot SCAG en Firestore,
 * con la MISMA estructura que ya usa tu monitor Geinz, pero en las
 * rutas propias de SCAG:
 *
 *   WhatsApp -> historial_conversaciones/whatsapp/whatsapp_scag/{numero_usuario}
 *   Telegram -> historial_conversaciones/telegram/scag_telegram/{numero_usuario}
 *
 * Cada conversación es un documento con:
 *   - nombre_usuario, numero_usuario
 *   - ultimo_mensaje: { remitente, contenido, tipo, timestamp }
 *   - ultima_actividad (serverTimestamp)
 *   - ia_activada (true por defecto, solo se setea al crear la conversación)
 *   - etiquetas, notas_internas, sonido_notificacion (para que el monitor
 *     los pueda editar sin romper nada)
 *
 * Y cada mensaje va en la subcolección 'mensajes':
 *   - remitente ('usuario' | 'bot'), contenido, tipo, timestamp, mensaje_id
 *
 * ---------------------------------------------------------------
 * USO (desde tu index.js):
 *
 *   const { guardarMensajeHistorial } = require('./historial_scag');
 *
 *   // cuando el usuario escribe:
 *   await guardarMensajeHistorial({
 *     canal: 'whatsapp',            // 'whatsapp' | 'telegram'
 *     numero_usuario,               // ej: '51987654321' o 'tg_123456'
 *     nombre_usuario: nombre_user,  // opcional, solo lo necesitas del lado usuario
 *     remitente: 'usuario',
 *     tipo,                         // 'texto' | 'audio' | 'foto' | etc
 *     contenido: mensajeFinal,
 *     mensaje_id: mensajeWa.id,     // opcional, se usa para no duplicar en reintentos
 *   });
 *
 *   // cuando le respondes al usuario:
 *   await guardarMensajeHistorial({
 *     canal: 'whatsapp',
 *     numero_usuario,
 *     remitente: 'bot',
 *     tipo: 'texto',
 *     contenido: final.reply,
 *   });
 * ---------------------------------------------------------------
 */

const admin = require("firebase-admin");
if (!admin.apps.length) admin.initializeApp();

const db = admin.firestore();
const FieldValue = admin.firestore.FieldValue;

// Mapa de canal -> (documento del canal, subcolección de SCAG)
const DESTINOS_SCAG = {
  whatsapp: { docCanal: "whatsapp", subcoleccion: "whatsapp_scag" },
  telegram: { docCanal: "telegram", subcoleccion: "scag_telegram" },
};

/**
 * Guarda un mensaje (de usuario o de bot) en el historial de SCAG.
 *
 * @param {Object} params
 * @param {'whatsapp'|'telegram'} params.canal
 * @param {string} params.numero_usuario  - id de la conversación (numero de WhatsApp o `tg_<chatId>`)
 * @param {string} [params.nombre_usuario] - nombre visible del contacto (opcional)
 * @param {'usuario'|'bot'} params.remitente
 * @param {string} [params.tipo='texto'] - 'texto' | 'audio' | 'foto' | 'sticker' | etc
 * @param {string} [params.contenido=''] - texto del mensaje (transcripción, texto plano, etc)
 * @param {string|null} [params.mensaje_id=null] - id del mensaje en el proveedor (WhatsApp/Telegram),
 *        se usa para no duplicar el mensaje si el webhook reintenta la entrega.
 * @returns {Promise<{ok:boolean, error?:string}>}
 */
async function guardarMensajeHistorial({
  canal,
  numero_usuario,
  nombre_usuario,
  remitente,
  tipo = "texto",
  contenido = "",
  mensaje_id = null,
}) {
  try {
    // ---------- Validaciones básicas ----------
    if (!canal || !DESTINOS_SCAG[canal]) {
      throw new Error(
        `Canal inválido: "${canal}". Debe ser "whatsapp" o "telegram".`,
      );
    }
    if (!numero_usuario) {
      throw new Error("Falta numero_usuario.");
    }
    if (remitente !== "usuario" && remitente !== "bot") {
      throw new Error(
        `remitente inválido: "${remitente}". Debe ser "usuario" o "bot".`,
      );
    }

    const { docCanal, subcoleccion } = DESTINOS_SCAG[canal];

    const conversacionRef = db
      .collection("historial_conversaciones")
      .doc(docCanal)
      .collection(subcoleccion)
      .doc(String(numero_usuario));

    const mensajesRef = conversacionRef.collection("mensajes");

    // ---------- 1) Guardar el mensaje en la subcolección 'mensajes' ----------
    // Si viene mensaje_id (de WhatsApp/Telegram), lo usamos como ID del doc
    // para que, si el webhook reintenta el mismo evento, no se duplique.
    const mensajeData = {
      remitente,
      tipo,
      contenido: contenido || "",
      mensaje_id: mensaje_id || null,
      timestamp: FieldValue.serverTimestamp(),
    };

    if (mensaje_id) {
      const msgDocId = `${remitente}_${mensaje_id}`; // evita choque si algún día coincide id usuario/bot
      await mensajesRef.doc(msgDocId).set(mensajeData, { merge: true });
    } else {
      await mensajesRef.add(mensajeData);
    }

    // ---------- 2) Verificar si la conversación ya existe ----------
    const conversacionSnap = await conversacionRef.get();
    const esConversacionNueva = !conversacionSnap.exists;

    // ---------- 3) Armar el update del documento principal de la conversación ----------
    const updateConversacion = {
      numero_usuario: String(numero_usuario),
      ultima_actividad: FieldValue.serverTimestamp(),
      ultimo_mensaje: {
        remitente,
        tipo,
        contenido: contenido || "",
        timestamp: FieldValue.serverTimestamp(),
      },
    };

    // El nombre solo se pisa si nos lo pasaron (para no borrar el nombre
    // guardado cuando el remitente es el bot y no tenemos ese dato a mano).
    if (nombre_usuario) {
      updateConversacion.nombre_usuario = nombre_usuario;
    }

    // Campos que solo se inicializan la primera vez que se crea la conversación,
    // para no pisar lo que el equipo ya haya configurado desde el monitor
    // (ia_activada, etiquetas, notas_internas, sonido_notificacion).
    if (esConversacionNueva) {
      updateConversacion.ia_activada = true;
      updateConversacion.etiquetas = [];
      updateConversacion.notas_internas = "";
      updateConversacion.sonido_notificacion = "default";
    }

    await conversacionRef.set(updateConversacion, { merge: true });

    return { ok: true };
  } catch (error) {
    console.error(
      `❌ [guardarMensajeHistorial][SCAG][${canal}] Error:`,
      error.message,
    );
    return { ok: false, error: error.message };
  }
}

module.exports = { guardarMensajeHistorial };