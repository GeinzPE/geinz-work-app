const { onCall, HttpsError } = require("firebase-functions/v2/https");
const admin = require("firebase-admin");

if (!admin.apps.length) admin.initializeApp();
const db = admin.firestore();

const { guardarMensajeHistorial } = require("./historial_chats/historial_geinz.js");

// ===== variables de entorno, ya las cargas desde tu .env =====
const WHATSAPP_TOKEN = process.env.ID_API_WHATSAPP;           // Bearer token (empieza con EAA...)
const WHATSAPP_PHONE_NUMBER_ID = process.env.ID_NUMBER_WHATSAPP; // el ID numérico
const TELEGRAM_TOKEN = process.env.TELEGRAM_BOT_TOKEN;        // "8874023011:AAHy..."
const WHATSAPP_API_VERSION = "v20.0";

// Mismo mapeo que historial_geinz.js: nada de colecciones planas,
// todo vive bajo historial_conversaciones/{docCanal}/{subcoleccion}/{numero_usuario}
const DESTINOS_GEINZ = {
  whatsapp: { docCanal: "whatsapp", subcoleccion: "whatsapp_geinz" },
  telegram: { docCanal: "telegram", subcoleccion: "geinz_telegram" },
};

/**
 * enviarMensajeManual
 * Se llama desde el front cuando el operador escribe un mensaje con la IA pausada.
 * input: { canal: 'whatsapp' | 'telegram', conversacionId: string, texto: string }
 */
exports.enviarMensajeManual = onCall(async (request) => {
  const { canal, conversacionId, texto } = request.data || {};

  if (!canal || !conversacionId || !texto || !texto.trim()) {
    throw new HttpsError("invalid-argument", "Faltan datos: canal, conversacionId o texto.");
  }
  if (!DESTINOS_GEINZ[canal]) {
    throw new HttpsError("invalid-argument", `Canal desconocido: ${canal}`);
  }

  // TODO: si quieres restringir quién puede enviar, valida aquí request.auth
  // if (!request.auth) throw new HttpsError("unauthenticated", "Debes iniciar sesión.");

  const { docCanal, subcoleccion } = DESTINOS_GEINZ[canal];
  const convRef = db
    .collection("historial_conversaciones")
    .doc(docCanal)
    .collection(subcoleccion)
    .doc(conversacionId);

  const convSnap = await convRef.get();
  if (!convSnap.exists) {
    throw new HttpsError("not-found", "La conversación no existe.");
  }
  const convData = convSnap.data();

  // 1) enviar el mensaje real por el canal correspondiente
  if (canal === "whatsapp") {
    const destino = convData.numero_usuario || conversacionId;
    await enviarWhatsapp(destino, texto);
  } else {
    // el chat_id de Telegram puede venir en 'numero_usuario' o, si no existe ese campo,
    // en el propio ID del documento con el prefijo 'tg_' (ej: 'tg_8786837495')
    const crudo = convData.numero_usuario || conversacionId;
    const chatId = String(crudo).replace(/^tg_/, "");
    await enviarTelegram(chatId, texto);
  }

  // 2) guardar el mensaje en la ruta nueva, con la MISMA función que usa
  //    todo el resto del bot (geinz_dispatcher.js / envios.js), para que
  //    quede exactamente igual de estructurado que cualquier otro mensaje.
  await guardarMensajeHistorial({
    canal,
    numero_usuario: conversacionId,
    remitente: "bot",
    tipo: "texto",
    contenido: texto,
  });

  return { ok: true };
});

async function enviarWhatsapp(numero, texto) {
  const url = `https://graph.facebook.com/${WHATSAPP_API_VERSION}/${WHATSAPP_PHONE_NUMBER_ID}/messages`;
  const res = await fetch(url, {
    method: "POST",
    headers: {
      Authorization: `Bearer ${WHATSAPP_TOKEN}`,
      "Content-Type": "application/json",
    },
    body: JSON.stringify({
      messaging_product: "whatsapp",
      to: numero,
      type: "text",
      text: { body: texto },
    }),
  });
  if (!res.ok) {
    const errBody = await res.text();
    console.error("Error WhatsApp API:", res.status, errBody);
    throw new HttpsError("internal", `Error de WhatsApp API: ${errBody}`);
  }
}

async function enviarTelegram(chatId, texto) {
  if (!TELEGRAM_TOKEN) {
    console.error("TELEGRAM_BOT_TOKEN no está definido en las variables de entorno.");
    throw new HttpsError("failed-precondition", "Falta configurar TELEGRAM_BOT_TOKEN.");
  }
  const url = `https://api.telegram.org/bot${TELEGRAM_TOKEN}/sendMessage`;
  console.log("Enviando a Telegram, chat_id:", chatId);
  const res = await fetch(url, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({
      chat_id: chatId,
      text: texto,
    }),
  });
  const data = await res.json().catch(() => null);
  if (!res.ok) {
    console.error("Error Telegram API:", res.status, data);
    throw new HttpsError("internal", `Error de Telegram API: ${data?.description || res.status}`);
  }
}