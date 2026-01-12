const { onRequest } = require("firebase-functions/v2/https");
const {
  onDocumentCreated,
  onDocumentWritten,
} = require("firebase-functions/v2/firestore");
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
      logo = "", // 👈 NUEVO (opcional)
      image = "", // 👈 YA EXISTE
      idTienda = "",
      idAnuncio = "",
      tipo_notificacion = "logo", // logo | imagen | premium,
      prioridad = "high",
    } = req.body;

    // 🔹 Validación mínima del tipo
    const tiposPermitidos = ["logo", "imagen", "premium"];
    if (!tiposPermitidos.includes(tipo_notificacion)) {
      return res.status(400).send("Tipo de notificación inválido");
    }

    const prioridadesPermitidas = ["high", "normal", "low"];
    const prioridadFinal = prioridadesPermitidas.includes(
      prioridad.toLowerCase()
    )
      ? prioridad.toLowerCase()
      : "high";

    const mensaje = {
      token: token,
      data: {
        title: String(title),
        body: String(body),
        link: String(link),
        logo: String(logo), // 👈 SE ENVÍA
        image: String(image),
        idTienda: String(idTienda),
        idAnuncio: String(idAnuncio),
        tipo_notificacion: String(tipo_notificacion),
      },
      android: {
        priority: prioridadFinal,
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
    const indice = req.query.i || req.query.indice;
    const id_promo_compartida = req.query.pi;

    const mapa_id = {
      nvng: "nuevos_negocios",
      seyt: "servicios_y_tramites",
      lgtr: "lugares_turisticos",
      nemg: "numeros_servicios_publicos",
    };

    const coll_completa = tipo === "prn" ? "promos_ofertas" : "promo";
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
    const mapa_ids_scren = mapa_id[id] || id;

    // ============================
    //        VALIDACIÓN BASE
    // ============================
    if (!tipo || !id) {
      return res.status(400).send("Faltan parámetros obligatorios: tipo, id.");
    }

    // ============================
    //   TIPOS QUE NO USAN LOCALIDAD
    // ============================
    const TIPOS_SIN_LOCALIDAD = ["rew", "rewc", "ru", "prf", "prn", "scr"];

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
    } else if (tipo === "prn") {
      ref = admin
        .firestore()
        .collection("Tiendas")
        .doc(localidad)
        .collection(coll_completa)
        .doc(id_promo_compartida);
    } else if (tipo === "scr") {
      ref = admin.firestore().collection("share_screen").doc(mapa_ids_scren);
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
      } else if (tipo === "prn") {
        titulo = capitalizeFirstLetter(
          data?.informacion?.titulo || "Mira esta promo en Geinz"
        );
      } else if (tipo === "scr") {
        titulo = capitalizeFirstLetter(data.titulo || "Geinz");
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
        const idImagen = req.query.i || req.query.indice; // string
        if (promos && idImagen) {
          imagen = promos[idImagen]; // obtiene la URL correcta
          if (!imagen && data.img_tienda?.logo_tienda) {
            imagen = data.img_tienda.logo_tienda; // fallback
          }
        } else if (data.img_tienda?.logo_tienda) {
          imagen = data.img_tienda.logo_tienda;
        }
      } else if (tipo === "prn") {
        const promos = data.img_container?.lista_img || [];
        if (promos.length > 0) {
          imagen = promos[0]; // toma siempre la primera imagen si existe
        } else if (data.img_container?.logo_img) {
          imagen = data.img_container.logo_img;
        } else {
          imagen = "https://geinzworkapp.web.app/default.jpg";
        }
      } else if (tipo === "scr") {
        if (data.img) {
          imagen = data.img;
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
    if (tipo === "p" && indice) {
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
    schedule: "0 0 * * *",
    timeZone: "America/Lima",
    region: "us-central1",
  },
  async () => {
    const db = admin.firestore();

    console.log("🔄 Revisando promociones expiradas...");

    // 🕛 HOY sin hora (00:00) en America/Lima
    const hoy = new Date();
    hoy.setHours(0, 0, 0, 0);

    const snapshot = await db
      .collection("Tiendas")
      .doc("barranca") // luego lo puedes hacer dinámico
      .collection("promos_ofertas")
      .where("fechas.activo", "==", true)
      .get();

    if (snapshot.empty) {
      console.log("ℹ️ No hay promociones activas");
      return;
    }

    const batch = db.batch();
    let contador = 0;

    snapshot.forEach((doc) => {
      const data = doc.data();
      const fechaFinStr = data?.fechas?.fin; // "dd/MM/yyyy"

      if (!fechaFinStr) return;

      const [dia, mes, anio] = fechaFinStr.split("/").map(Number);

      // 📅 Fecha fin sin hora
      const fechaFinDate = new Date(anio, mes - 1, dia);
      fechaFinDate.setHours(0, 0, 0, 0);

      // ❌ Si la promo ya venció
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

exports.verificarMinimoSeguidores = onDocumentCreated(
  "Tiendas/{localidad}/{localidad}/{idTienda}/seguidores/{idUsuario}",
  async (event) => {
    try {
      // 🔹 Obtenemos parámetros del trigger
      const { localidad, idTienda } = event.params;

      // 🔹 Referencia a la tienda
      const tiendaRef = admin
        .firestore()
        .collection("Tiendas")
        .doc(localidad)
        .collection(localidad)
        .doc(idTienda);

      const tiendaDoc = await tiendaRef.get();
      if (!tiendaDoc.exists) return null;

      const tiendaData = tiendaDoc.data();
      const yaDesbloqueado = tiendaData?.notificacionDesbloqueoEnviada || false;
      const nombre_tienda = tiendaData?.nombre_tienda || "🥳🥳"; // Valor por defecto

      const propietarios = tiendaData?.propietario_id || [];
      if (propietarios.length === 0) return null;

      // 🔹 Contamos los seguidores
      const totalSeguidores = (await tiendaRef.collection("seguidores").get())
        .size;
      console.log(
        `Tienda ${idTienda} ahora tiene ${totalSeguidores} seguidores`
      );

      if (totalSeguidores < 10) return null;

      console.log(`Propietarios a notificar: ${propietarios}`);

      // 🔹 Recorremos todos los propietarios
      for (const propietarioId of propietarios) {
        const tokenDoc = await admin
          .firestore()
          .collection("Trabajadores_Usuarios_Drivers")
          .doc("users")
          .collection("tokens")
          .doc(propietarioId)
          .get();

        if (!tokenDoc.exists) {
          console.log(
            `No existe doc de tokens para propietario ${propietarioId}`
          );
          continue;
        }

        const tokensMap = tokenDoc?.data()?.tokens || {};
        const tokens = Object.values(tokensMap);

        if (tokens.length === 0) {
          console.log(
            `No hay tokens dentro del map para propietario ${propietarioId}`
          );
          continue;
        }

        console.log(
          `Enviando notificación a ${tokens.length} token(s) de propietario ${propietarioId}`
        );

        // 🔹 Enviamos la notificación a cada token usando la función reutilizable
        for (const token of tokens) {
          await enviarNotificacionFCM_tienda({
            token,
            title: `🎉¡Felicidades ${nombre_tienda}! 🥳`,
            body: `¡Ya tienes tus primeros ${totalSeguidores} seguidores 👨🏻‍👩🏻‍👦‍👦! Ahora has desbloqueado la opción de enviar notificaciones sobre tus promociones y ofertas a tus seguidores. 📢`,
            idTienda,
            idAnuncio: "", // opcional si tu función tiene parámetro idAnuncio=""
            tipo_notificacion: "logo",
            prioridad: "high",
          });
        }
      }

      // 🔹 Marcamos que ya se envió la notificación
      await tiendaRef.update({ notificacionDesbloqueoEnviada: true });
      console.log("Marcado notificacionDesbloqueoEnviada: true");

      return null;
    } catch (error) {
      console.error("ERROR verificarMinimoSeguidores:", error);
      return null;
    }
  }
);

exports.alertaSaldoBajo = onDocumentWritten(
  'Tiendas/{localidad}/{localidad}/{id_tienda}',
  async (event) => {
    try {
      console.log("===== TRIGGER alertaSaldoBajo =====");
      console.log("Params recibidos:", event.params);

      const beforeData = event.data?.before?.data() || {};
      const afterData = event.data?.after?.data() || {};
      console.log("Datos ANTES del cambio:", beforeData);
      console.log("Datos DESPUÉS del cambio:", afterData);

      const saldoAntes = beforeData.puntos_tienda || 0;
      const saldoDespues = afterData.puntos_tienda || 0;
      const notiEnviada = afterData.ultima_notificacion_enviada || false;
      const propietarios = afterData.propietario_id || [];
      const idTienda = event.params.id_tienda;

      console.log("Saldo antes:", saldoAntes);
      console.log("Saldo después:", saldoDespues);
      console.log("ultima_notificacion_enviada:", notiEnviada);
      console.log("Propietarios:", propietarios);

      // 🔹 Reiniciar flag si el saldo sube por encima del umbral
      if (saldoDespues >= 50 && notiEnviada) {
        await event.data.after.ref.update({ ultima_notificacion_enviada: false });
      }

      // 🔹 Comprobamos si debemos enviar notificación
      if (saldoDespues < 50 && !notiEnviada) {
     
        for (const propietarioId of propietarios) {

          const tokenDoc = await admin
            .firestore()
            .collection("Trabajadores_Usuarios_Drivers")
            .doc("users")
            .collection("tokens")
            .doc(propietarioId)
            .get();

          if (!tokenDoc.exists) {
            console.log(`No se encontró doc de tokens para propietario ${propietarioId}`);
            continue;
          }

          const tokensMap = tokenDoc.data()?.tokens || {};
          const tokens = Object.values(tokensMap);


          if (tokens.length === 0) {
            continue;
          }

          for (const token of tokens) {
       
            await enviarNotificacionFCM_tienda({
              token,
              title: `🎯 ¡A punto de quedarte sin monedas!`,
              body: `Te quedan pocas monedas. ¡No dejes que tu alcance se detenga! 🔔✨`,
              link: "https://geinzworkapp.web.app/share?t=scr&id=rec",
              idTienda,
              idAnuncio: "",
              tipo_notificacion: "logo",
              prioridad: "high",
            });
          }
        }


        await event.data.after.ref.update({ ultima_notificacion_enviada: true });
      } else {
      }

  

    } catch (error) {
      console.error("ERROR alertaSaldoBajo:", error);
    }
  }
);

async function enviarNotificacionFCM_tienda({
  token,
  title,
  body,
  link = "https://geinzworkapp.web.app/share?t=scr&id=ads",
  logo = "",
  image = "",
  idTienda,
  idAnuncio = "", // ✅ agregar
  tipo_notificacion,
  prioridad = "high",
}) {
  try {
const mensaje = {
  token: token,
  data: {
    title: String(title),
    body: String(body),
    link: String(link),
    logo: String(logo),
    image: String(image),
    idTienda: String(idTienda),
    idAnuncio: String(idAnuncio),
    tipo_notificacion: String(tipo_notificacion),
  },
  android: { priority: prioridad }
};
    const respuesta = await admin.messaging().send(mensaje);
    console.log("Notificación enviada al token:", token);
    return respuesta;
  } catch (error) {
    console.error("ERROR enviarNotificacionFCM:", error);
    if (error.code === "messaging/registration-token-not-registered") {
      console.log("Token inválido, debería eliminarlo de Firestore:", token);
    }
  }
}

/*

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
*/
