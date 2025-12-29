const { onRequest } = require("firebase-functions/v2/https");
const { onDocumentWritten } = require("firebase-functions/v2/firestore");
const logger = require("firebase-functions/logger");

const admin = require("firebase-admin");
const algoliasearch = require("algoliasearch");
const { onSchedule } = require("firebase-functions/v2/scheduler");

admin.initializeApp();

// ==================== Algolia ====================
const APP_ID = process.env.ALGOLIA_APP_ID || "";
const API_KEY = process.env.ALGOLIA_API_KEY || "";
const client = algoliasearch(APP_ID, API_KEY);
const index = client.initIndex("lugares");

exports.syncLugarToAlgolia = onDocumentWritten(
  {
    document: "lugares/{lugarId}",
    region: "us-central1",
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
      idAnuncio = "",
    } = req.body;

    const mensaje = {
      token: token,
      data: {
        title: String(title),
        body: String(body),
        link: String(link),
        image: String(image),
        idTienda: String(idTienda),
        idAnuncio: String(idAnuncio),
      },
      android: {
        priority: "high",
      },
    };

    const respuesta = await admin.messaging().send(mensaje);
    res.status(200).send("Notificación enviada: " + respuesta);
  } catch (error) {
    console.error(error);
    res.status(500).send(error.message);
  }
});

// ==================== SHARE (Tienda + Turismo) ====================
// ==================== SHARE (Tienda + Turismo + Otros) ====================
exports.share = onRequest(async (req, res) => {
  try {
    // ============================
    //        PARÁMETROS
    // ============================
    const tipo = req.query.t || req.query.tipo; // SIEMPRE CORTO
    const id = req.query.id;
    const localidadRaw = req.query.l || req.query.localidad;
    const categoria = req.query.c || req.query.categoria;
    const indice = parseInt(req.query.i || req.query.indice);
    const cl = req.query.cl;

    const coll_completa = cl === "pro" ? "promos_ofertas" : "promo";
    // ============================
    //        MAPA LOCALIDADES
    // ============================
    const MAPA_LOCALIDADES = {
      ba: "barranca",
      par: "paramonga",
      pat: "pativilca",
      su: "supe",
      pue: "puerto supe",
    };

    const localidad = MAPA_LOCALIDADES[localidadRaw] || localidadRaw;

    // ============================
    //        VALIDACIÓN BASE
    // ============================
    if (!tipo || !id) {
      return res.status(400).send("Faltan parámetros obligatorios: tipo, id.");
    }

    // ============================
    //   TIPOS QUE NO USAN LOCALIDAD
    // ============================
    const TIPOS_SIN_LOCALIDAD = ["rew", "rewc", "ru", "prf", "prof"];

    if (!TIPOS_SIN_LOCALIDAD.includes(tipo) && (!localidad || !categoria)) {
      return res.status(400).send("Faltan parámetros: localidad, categoria.");
    }

    let ref = null;
    let data = null;

    // ============================
    //     SELECCIÓN FIRESTORE
    // ============================
    if (tipo === "ti" || tipo === "p") {
      ref = admin
        .firestore()
        .collection("Tiendas")
        .doc(localidad)
        .collection(localidad)
        .doc(id);
    } else if (tipo === "tu") {
      ref = admin
        .firestore()
        .collection("Tiendas")
        .doc(localidad)
        .collection(categoria)
        .doc(id);
    } else if (tipo === "prof") {
      ref = admin
        .firestore()
        .collection("Tiendas")
        .doc(localidad)
        .collection(coll_completa)
        .doc(id);
    }
    // rew | rewc | ru | prf → NO FIRESTORE

    if (ref) {
      const snap = await ref.get();
      if (!snap.exists) {
        return res.status(404).send("No existe el documento solicitado.");
      }
      data = snap.data();
    }

    // ============================
    //            TÍTULO
    // ============================
    let titulo = "Geinz";

    if (data) {
      if (tipo === "ti" || tipo === "p") {
        titulo = capitalizeFirstLetter(data.nombre_tienda || "Tienda en Geinz");
      } else if (tipo === "tu") {
        titulo = capitalizeFirstLetter(data.nombre || "Lugar en Geinz");
      } else if (tipo === "prof") {
        titulo = capitalizeFirstLetter(
          data?.informacion?.titulo || "Mira esta promo en Geinz"
        );
      }
    }

    // ============================
    //            IMAGEN
    // ============================
    let imagen = "https://geinzworkapp.web.app/default.jpg";

    if (data) {
      if (tipo === "ti" && data.img_tienda?.logo_tienda) {
        imagen = data.img_tienda.logo_tienda;
      } else if (tipo === "tu" && data.img?.principal) {
        imagen = data.img.principal;
      } else if (tipo === "p") {
        const promos = data.img_tienda?.lista_img?.promociones;
        if (
          promos &&
          Array.isArray(promos) &&
          !isNaN(indice) &&
          indice >= 0 &&
          indice < promos.length
        ) {
          imagen = promos[indice];
        } else if (data.img_tienda?.logo_tienda) {
          imagen = data.img_tienda.logo_tienda;
        }
      } else if (tipo === "prof") {
        const promos = data.img_container?.lista_img || [];
        if (promos.length > 0) {
          imagen = promos[0]; // toma siempre la primera imagen si existe
        } else if (data.img_container?.logo_img) {
          imagen = data.img_container.logo_img;
        } else {
          imagen = "https://geinzworkapp.web.app/default.jpg";
        }
      }
    }

    // ============================
    //        URL DESTINO
    // ============================
    let destino = `https://geinzworkapp.web.app/${tipo}?id=${id}`;

    if (localidad) destino += `&localidad=${localidad}`;
    if (categoria) destino += `&categoria=${categoria}`;
    if (tipo === "p" && !isNaN(indice)) {
      destino += `&indice=${indice}`;
    }

    // ============================
    //        HTML + META
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

exports.desactivarPromocionesExpiradas = onSchedule(
  {
    schedule: "every 24 hours",
    timeZone: "America/Lima",
    region: "us-central1",
  },
  async () => {
    const db = admin.firestore();
    const hoy = new Date();

    console.log("🔄 Revisando promociones expiradas...");

    const snapshot = await db
      .collection("Tiendas")
      .doc("barranca") // luego puedes hacerlo dinámico
      .collection("promos_ofertas")
      .where("fechas.activo", "==", true)
      .get();

    if (snapshot.empty) {
      console.log("No hay promociones activas");
      return;
    }

    const batch = db.batch();
    let contador = 0;

    snapshot.forEach((doc) => {
      const data = doc.data();
      const fechaFinStr = data?.fechas?.fin; // "dd/MM/yyyy"

      if (!fechaFinStr) return;

      const [dia, mes, anio] = fechaFinStr.split("/").map(Number);
      const fechaFinDate = new Date(anio, mes - 1, dia);

      if (fechaFinDate < hoy) {
        batch.update(doc.ref, {
          "fechas.activo": false,
        });
        contador++;
      }
    });

    if (contador > 0) {
      await batch.commit();
    }

    console.log(`✅ Promociones desactivadas: ${contador}`);
  }
);
