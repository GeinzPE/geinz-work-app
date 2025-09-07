const { onRequest } = require("firebase-functions/v2/https");
const { onDocumentWritten } = require("firebase-functions/v2/firestore");
const logger = require("firebase-functions/logger");

const admin = require("firebase-admin");
const algoliasearch = require("algoliasearch");

admin.initializeApp();

// ==================== Algolia ====================
const APP_ID = process.env.ALGOLIA_APP_ID || "";
const API_KEY = process.env.ALGOLIA_API_KEY || "";
const client = algoliasearch(APP_ID, API_KEY);
const index = client.initIndex("lugares");

// 🔹 Función Firestore con región especificada
exports.syncLugarToAlgolia = onDocumentWritten(
  {
    document: "lugares/{lugarId}",
    region: "us-central1" // 👈 aquí especificas la región de tu Firestore
  },
  async (event) => {
    const lugarId = event.params.lugarId;

    if (!event.data.after.exists) {
      // 🔹 Documento eliminado
      await index.deleteObject(lugarId);
      logger.info(`Documento ${lugarId} eliminado de Algolia`);
      return;
    }

    // 🔹 Documento creado o actualizado
    const data = event.data.after.data();
    data.objectID = lugarId;
    await index.saveObject(data);
    logger.info(`Documento ${lugarId} agregado/actualizado en Algolia`);
  }
);

// ==================== Notificaciones ====================
exports.enviarNotificacion = onRequest(async (req, res) => {
  res.set("Access-Control-Allow-Origin", "*");

  if (req.method !== "POST") {
    return res.status(405).send("Método no permitido");
  }

  try {
    const { token, title, body, image = "", click_action = "", idAnuncio = "", idTienda = "", entrada = "" } = req.body;

    const mensaje = {
      notification: { title, body, image },
      data: { click_action, idAnuncio, idTienda, entrada },
      token,
      android: { priority: "high" },
      apns: { headers: { "apns-priority": "10" } }
    };

    const respuesta = await admin.messaging().send(mensaje);
    res.status(200).send("Notificación enviada: " + respuesta);
  } catch (error) {
    logger.error("Error al enviar notificación:", error);
    res.status(500).send("Error: " + error.message);
  }
});
