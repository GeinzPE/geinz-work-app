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
// ==================== Notificaciones ====================
exports.enviarNotificacion = onRequest(async (req, res) => {
  res.set("Access-Control-Allow-Origin", "*");

  if (req.method !== "POST") {
    return res.status(405).send("Método no permitido");
  }

  try {
    const {
      token,
      title,
      body,
      link = "",
      image = "",
      idTienda = "",
      idAnuncio = ""
    } = req.body;

    const mensaje = {
      token: token,
      data: {
        title: String(title),
        body: String(body),
        link: String(link),
        image: String(image),       // 🔥 URL de la imagen
        idTienda: String(idTienda),
        idAnuncio: String(idAnuncio)
      },
      android: {
        priority: "high"
      }
    };

    const respuesta = await admin.messaging().send(mensaje);
    res.status(200).send("Notificación enviada: " + respuesta);

  } catch (error) {
    console.error(error);
    res.status(500).send(error.message);
  }
});



// ==================== SHARE (Tienda + Turismo) ====================
exports.share = onRequest(async (req, res) => {
  try {
    // ============================
    //   PARÁMETROS (cortos / largos)
    // ============================
    const tipoRaw =
      req.query.t ||
      req.query.tipo; // ti | p | tu | tienda | promo | turismo

    const id = req.query.id;

    const localidadRaw =
      req.query.l ||
      req.query.localidad;

    const categoria =
      req.query.c ||
      req.query.categoria;

    const indice = parseInt(
      req.query.i ||
      req.query.indice
    );

    // ============================
    //   NORMALIZAR TIPO
    // ============================
    let tipo = tipoRaw;

    if (tipoRaw === "ti") tipo = "tienda";
    else if (tipoRaw === "p") tipo = "promo";
    else if (tipoRaw === "tu") tipo = "turismo";

    // ============================
    //   MAPA LOCALIDADES
    // ============================
    const MAPA_LOCALIDADES = {
      ba: "barranca",
      par: "paramonga",
      pat: "pativilca",
      su: "supe",
      pue: "puerto supe"
    };

    const localidad =
      MAPA_LOCALIDADES[localidadRaw] || localidadRaw;

    // ============================
    //      VALIDACIÓN BÁSICA
    // ============================
    if (!tipo || !id || !localidad || !categoria) {
      return res
        .status(400)
        .send("Faltan parámetros: tipo, id, localidad, categoria.");
    }

    let ref;

    // ============================
    //   SELECCIÓN FIRESTORE
    // ============================
    if (tipo === "tienda" || tipo === "promo") {
      ref = admin.firestore()
        .collection("Tiendas")
        .doc(localidad)
        .collection(localidad)
        .doc(id);
    }
    else if (tipo === "turismo") {
      ref = admin.firestore()
        .collection("Tiendas")
        .doc(localidad)
        .collection(categoria)
        .doc(id);
    }
    else {
      return res.status(400).send("Tipo inválido.");
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
      tipo === "tienda" || tipo === "promo"
        ? capitalizeFirstLetter(data.nombre_tienda || "Tienda en Geinz")
        : capitalizeFirstLetter(data.nombre || "Lugar turístico en Geinz");

    // ============================
    //          IMAGEN
    // (NO SE TOCA TU ESTRUCTURA)
    // ============================
    let imagen = "https://geinzworkapp.web.app/default.jpg";

    if (tipo === "tienda") {
      if (data.img_tienda && data.img_tienda.logo_tienda) {
        imagen = data.img_tienda.logo_tienda;
      }
    }
    else if (tipo === "turismo") {
      if (data.img && data.img.principal) {
        imagen = data.img.principal;
      }
    }
    else if (tipo === "promo") {
      const promos =
        data.img_tienda &&
        data.img_tienda.lista_img &&
        data.img_tienda.lista_img.promociones;

      if (
        promos &&
        Array.isArray(promos) &&
        indice >= 0 &&
        indice < promos.length
      ) {
        imagen = promos[indice];
      }
      else if (data.img_tienda && data.img_tienda.logo_tienda) {
        imagen = data.img_tienda.logo_tienda;
      }
    }

    // ============================
    //       URL DESTINO WEB
    // ============================
    let destino =
      `https://geinzworkapp.web.app/${tipo}` +
      `?id=${id}&localidad=${localidad}&categoria=${categoria}`;

    if (tipo === "promo" && !isNaN(indice)) {
      destino += `&indice=${indice}`;
    }

    // ============================
    //        HTML + META TAGS
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

