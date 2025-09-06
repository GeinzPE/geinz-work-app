const functions = require("firebase-functions");
const admin = require("firebase-admin");
const algoliasearch = require("algoliasearch");

admin.initializeApp();

// ==================== Algolia ====================
const APP_ID = functions.config().algolia.app_id;
const API_KEY = functions.config().algolia.api_key;
const client = algoliasearch(APP_ID, API_KEY);
const index = client.initIndex("lugares");

exports.syncLugarToAlgolia = functions.firestore
  .document("lugares/{lugarId}")
  .onWrite(async (change, context) => {
    const lugarId = context.params.lugarId;

    if (!change.after.exists) {
      await index.deleteObject(lugarId);
      console.log(`Documento ${lugarId} eliminado de Algolia`);
      return;
    }

    const data = change.after.data();
    data.objectID = lugarId;
    await index.saveObject(data);
    console.log(`Documento ${lugarId} agregado/actualizado en Algolia`);
  });

// ==================== Notificaciones ====================
exports.enviarNotificacion = functions.https.onRequest(async (req, res) => {
  res.set('Access-Control-Allow-Origin', '*');

  if (req.method !== "POST") {
    return res.status(405).send("Método no permitido");
  }

  try {
    const { token, title, body, image = "", click_action = "", idAnuncio = "", idTienda = "", entrada = "" } = req.body;

    const mensaje = {
      notification: { title, body, image },
      data: { click_action, idAnuncio, idTienda, entrada },
      token,
      android: { priority: 'high' },
      apns: { headers: { 'apns-priority': '10' } }
    };

    const respuesta = await admin.messaging().send(mensaje);
    res.status(200).send("Notificación enviada: " + respuesta);
  } catch (error) {
    console.error("Error al enviar notificación:", error);
    res.status(500).send("Error: " + error.message);
  }
});
