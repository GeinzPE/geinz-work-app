const { setGlobalOptions } = require("firebase-functions");
const functions = require("firebase-functions");
const admin = require("firebase-admin");
admin.initializeApp();

setGlobalOptions({ maxInstances: 10 });

exports.enviarNotificacion = functions.https.onRequest(async (req, res) => {
  const {
    token,
    title,
    body,
    click_action = "",     // ejemplo: "SERVICIOS_TIENDAS"
    idAnuncio = "",
    idTienda = "",
    entrada = ""
  } = req.body;

  const mensaje = {
    notification: {
      title,
      body
    },
    data: {
      click_action,
      idAnuncio,
      idTienda,
      entrada
    },
    token
  };

  try {
    const respuesta = await admin.messaging().send(mensaje);
    res.status(200).send("Notificación enviada: " + respuesta);
  } catch (error) {
    console.error("Error al enviar notificación:", error);
    res.status(500).send("Error: " + error.message);
  }
});
