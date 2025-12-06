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

exports.syncLugarToAlgolia = onDocumentWritten(
  {
    document: "lugares/{lugarId}",
    region: "us-central1"
  },
  async (event) => {
    const lugarId = event.params.lugarId;

    if (!event.data.after.exists) {
      await index.deleteObject(lugarId);
      logger.info(`Documento ${lugarId} eliminado de Algolia`);
      return;
    }

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

// ==================== SHARE (Tienda + Turismo) ====================
exports.share = onRequest(async (req, res) => {
  try {
    const tipo = req.query.tipo;     // tienda | turismo
    const id = req.query.id;
    const localidad = req.query.localidad;
    const categoria = req.query.categoria;

    if (!tipo || !id || !localidad || !categoria) {
      return res
        .status(400)
        .send("Faltan parámetros: tipo, id, localidad, categoria.");
    }

    let ref;

    // Selección de ruta Firestore
    if (tipo === "tienda") {
      ref = admin.firestore()
        .collection("Tiendas")
        .doc(localidad)
        .collection(localidad)
        .doc(id);
    }
    else if (tipo === "turismo") {
      ref = admin.firestore()
        .collection("Tiendas") // 👈 turismo está dentro de Tiendas
        .doc(localidad)
        .collection(categoria)
        .doc(id);
    }
    else {
      return res.status(400).send("Tipo inválido. Usa 'tienda' o 'turismo'");
    }

    const snap = await ref.get();
    if (!snap.exists) {
      return res.status(404).send("No existe el documento solicitado.");
    }

    const data = snap.data();

    // ============================
    //          TÍTULO
    // ============================
const titulo =
  tipo === "tienda"
    ? capitalizeFirstLetter(data.nombre_tienda || "Tienda en Geinz")
    : capitalizeFirstLetter(data.nombre || "Lugar turístico en Geinz");

    // ============================
    //          IMAGEN
    // ============================
    let imagen = "https://geinzworkapp.web.app/default.jpg";

    if (tipo === "tienda") {
      // 🔥 FIJO: tu Firestore usa img_tienda.logo_tienda
      if (data.img_tienda?.logo_tienda) {
        imagen = data.img_tienda.logo_tienda;
      }
    } else if (tipo === "turismo") {
      if (data.img?.principal) {
        imagen = data.img.principal;
      }
    }

    // ============================
    //       URL DESTINO WEB
    // ============================
    const destino = `https://geinzworkapp.web.app/${tipo}?id=${id}&localidad=${localidad}&categoria=${categoria}`;

    // ============================
    //        HTML + META TAG
    // ============================
   const html = `
      <html>
        <head>
          <meta property="og:title" content="${titulo}" />
          <meta property="og:image" content="${imagen}" />
          <meta property="og:image:width" content="1200" />
          <meta property="og:image:height" content="630" />
          <meta property="og:description" content="Encuéntralo en Geinz" />
          <meta property="og:type" content="website" />
        </head>
        <body>
          <script>
            window.location.href = "${destino}";
          </script>
        </body>
      </html>
    `;

    res.set("Content-Type", "text/html");
    res.status(200).send(html);

  } catch (e) {
    console.error("ERROR SHARE:", e);
    res.status(500).send("Error interno");
  }
});

function capitalizeFirstLetter(str) {
  if (!str) return "";
  return str.charAt(0).toUpperCase() + str.slice(1);
}

