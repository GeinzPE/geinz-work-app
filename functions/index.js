const functions = require("firebase-functions");
const admin = require("firebase-admin");
admin.initializeApp();

exports.enviarNotificacion = functions.https.onRequest(async (req, res) => {
  // Permitir solicitudes desde cualquier origen (CORS, útil en desarrollo)
  res.set('Access-Control-Allow-Origin', '*');

  // Solo permitir método POST
  if (req.method !== "POST") {
    return res.status(405).send("Método no permitido");
  }

  try {
    const {
      token,
      title,
      body,
      image = "",         // Imagen opcional
      click_action = "",
      idAnuncio = "",
      idTienda = "",
      entrada = ""
    } = req.body;

    const mensaje = {
      notification: {
        title,
        body,
        image // Esto mostrará una imagen en la notificación (si el cliente lo soporta)
      },
      data: {
        click_action,
        idAnuncio,
        idTienda,
        entrada
      },
      token,
      android: {
        priority: 'high'
      },
      apns: {
        headers: {
          'apns-priority': '10'
        }
      }
    };

    const respuesta = await admin.messaging().send(mensaje);
    res.status(200).send("Notificación enviada: " + respuesta);
  } catch (error) {
    console.error("Error al enviar notificación:", error);
    res.status(500).send("Error: " + error.message);
  }
});
