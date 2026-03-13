const { onRequest } = require("firebase-functions/v2/https");
const {
  onDocumentCreated,
  onDocumentWritten,
} = require("firebase-functions/v2/firestore");
const logger = require("firebase-functions/logger");

const admin = require("firebase-admin");
const algoliasearch = require("algoliasearch");
const { onSchedule } = require("firebase-functions/v2/scheduler");

const speech = require("@google-cloud/speech");

const client_specth = new speech.SpeechClient();

const textToSpeech = require("@google-cloud/text-to-speech");
const ttsClient = new textToSpeech.TextToSpeechClient();

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
  },
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
      prioridad.toLowerCase(),
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

    const coll_completa = tipo === "prms" ? "promos_ofertas" : "promo";
    // const coll_completa_datos_promos_intern= "prn"
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
    if (!tipo) {
      return res.status(400).send("Faltan parámetros obligatorios: tipo");
    }

    // ============================
    //   TIPOS QUE NO USAN LOCALIDAD
    // ============================
    const TIPOS_SIN_LOCALIDAD = [
      "rew",
      "rewc",
      "ru",
      "prf",
      "prn",
      "scr",
      "prms","in"
    ];

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
    } else if (tipo === "prms") {
      ref = admin
        .firestore()
        .collection("Tiendas")
        .doc(localidad)
        .collection(coll_completa)
        .doc(id_promo_compartida);
    } else if (tipo === "scr") {
      ref = admin.firestore().collection("share_screen").doc(mapa_ids_scren);
    } else if (tipo == "prn") {
      ref = admin
        .firestore()
        .collection("Tiendas")
        .doc(localidad)
        .collection(localidad)
        .doc(id)
        .collection("notificaciones_enviadas")
        .doc(id_promo_compartida);
    } else if (tipo == "in") {
      ref = admin
        .firestore()
        .collection("Tiendas")
        .doc(localidad)
        .collection("geinz_inmobiliaria")
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
      } else if (tipo === "prms") {
        titulo = capitalizeFirstLetter(
          data?.informacion?.titulo || "Mira esta promo en Geinz",
        );
      } else if (tipo === "scr") {
        titulo = capitalizeFirstLetter(data.titulo || "Geinz");
      } else if (tipo == "prn") {
        titulo = capitalizeFirstLetter(
          data.datos_de_notificacion.nombre_tienda || "Geinz",
        );
      } else if (tipo == "in") {
        titulo = capitalizeFirstLetter(data.nombre || "Geinz");
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
      } else if (tipo === "prms") {
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
      } else if (tipo == "prn") {
        imagen =
          data?.datos_de_notificacion?.img_notificacion ||
          "https://geinzworkapp.web.app/default.jpg";
      }else if(tipo=="in"){
         const promos = data.listaImg || [];
        if (promos.length > 0) {
          imagen = promos[0]; // toma siempre la primera imagen si existe
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

exports.eliminarPromocionesExpiradasCadaMinuto = onSchedule(
  {
    schedule: "0 0 * * *", // ⏱️ cada minuto
    timeZone: "America/Lima",
    region: "us-central1",
  },
  async () => {
    const db = admin.firestore();
    const ahora = admin.firestore.Timestamp.now();

    console.log("🗑️ Revisando promociones expiradas...");

    const snapshot = await db
      .collection("Tiendas")
      .doc("barranca")
      .collection("promos_ofertas")
      .get();

    if (snapshot.empty) {
      console.log("ℹ️ No hay promociones activas");
      return;
    }

    let eliminadas = 0;

    for (const doc of snapshot.docs) {
      const data = doc.data();
      const tipo = data?.tipo_hora_dias;
      const fechas = data?.datos_hora_fecha;

      let timestampFin = null;

      if (tipo === "dias") {
        timestampFin = fechas?.dias?.timestamp_fin;
      } else if (tipo === "horas") {
        timestampFin = fechas?.horas?.timestamp_fin;
      }

      if (!timestampFin) continue;

      if (timestampFin.toMillis() <= ahora.toMillis()) {
        const promoId = doc.id;
        const idTienda = data?.informacion?.id_tienda;

        if (!idTienda) continue;

        const destinoRef = db
          .collection("Tiendas")
          .doc("barranca")
          .collection("barranca")
          .doc(idTienda)
          .collection("promociones_geinz")
          .doc(promoId);

        // 1️⃣ Copiar promo
        await destinoRef.set({
          ...data,
          estado: "expirada",
          eliminada_en: admin.firestore.FieldValue.serverTimestamp(),
        });

        // 2️⃣ Copiar estadísticas
        await copiarSubcolecciones(doc.ref, destinoRef);

        // 🆕 3️⃣ BORRAR estadísticas originales
        await borrarSubcolecciones(doc.ref);

        // 4️⃣ Eliminar promo activa
        await doc.ref.delete();

        eliminadas++;
        console.log(`🗑️ Promo procesada: ${promoId}`);
      }
    }

    console.log(`✅ Total promociones procesadas: ${eliminadas}`);
  },
);

async function copiarSubcolecciones(origenRef, destinoRef) {
  const colecciones = await origenRef.listCollections();

  for (const col of colecciones) {
    const snap = await col.get();

    for (const doc of snap.docs) {
      const nuevoDocRef = destinoRef.collection(col.id).doc(doc.id);
      await nuevoDocRef.set(doc.data());

      // 🔁 copiar sub-subcolecciones
      await copiarSubcolecciones(doc.ref, nuevoDocRef);
    }
  }
}

async function borrarSubcolecciones(docRef) {
  const colecciones = await docRef.listCollections();

  for (const col of colecciones) {
    const snap = await col.get();

    for (const doc of snap.docs) {
      // 🔁 borrar sub-subcolecciones primero
      await borrarSubcolecciones(doc.ref);

      // ❌ borrar documento
      await doc.ref.delete();
    }
  }
}

exports.verificarMinimoSeguidores = onDocumentCreated(
  "Tiendas/{localidad}/{localidad}/{idTienda}/seguidores/{idUsuario}",
  async (event) => {
    const { localidad, idTienda } = event.params;
    const db = admin.firestore();

    const tiendaRef = db
      .collection("Tiendas")
      .doc(localidad)
      .collection(localidad)
      .doc(idTienda);

    try {
      await db.runTransaction(async (tx) => {
        const tiendaDoc = await tx.get(tiendaRef);
        if (!tiendaDoc.exists) return;

        const tiendaData = tiendaDoc.data();

        // 🔕 YA DESBLOQUEADO → SALIR
        if (tiendaData?.notificacionDesbloqueoEnviada === true) {
          console.log("🔕 Notificación ya enviada anteriormente");
          return;
        }

        // 🔢 CONTAR SEGUIDORES
        const seguidoresSnap = await tiendaRef.collection("seguidores").get();

        const totalSeguidores = seguidoresSnap.size;

        console.log(`👥 Seguidores actuales: ${totalSeguidores}`);

        if (totalSeguidores < 10) return;

        // 🔒 MARCAMOS PRIMERO (clave)
        tx.update(tiendaRef, {
          notificacionDesbloqueoEnviada: true,
        });

        const nombre_tienda = tiendaData?.nombre_tienda || "Tu tienda 🎉";

        const propietarios = tiendaData?.propietario_id || [];
        if (propietarios.length === 0) return;

        // 🔔 ENVIAR NOTIFICACIONES
        for (const propietarioId of propietarios) {
          const tokenDoc = await db
            .collection("Trabajadores_Usuarios_Drivers")
            .doc("users")
            .collection("tokens")
            .doc(propietarioId)
            .get();

          if (!tokenDoc.exists) continue;

          const tokens = Object.values(tokenDoc.data()?.tokens || {});
          for (const token of tokens) {
            await enviarNotificacionFCM_tienda({
              token,
              title: `🎉 ¡Felicidades ${nombre_tienda}!`,
              body: `¡Alcanzaste ${totalSeguidores} seguidores! Ya puedes enviar promociones 📢`,
              idTienda,
              tipo_notificacion: "logo",
              prioridad: "high",
            });
          }
        }
      });

      return null;
    } catch (error) {
      console.error("❌ ERROR verificarMinimoSeguidores:", error);
      return null;
    }
  },
);

exports.alertaSaldoBajo = onDocumentWritten(
  "Tiendas/{localidad}/{localidad}/{id_tienda}",
  async (event) => {
    try {
      console.log("===== TRIGGER alertaSaldoBajo =====");
      console.log("Params recibidos:", event.params);

      const beforeData = event.data?.before?.data() || {};
      const afterData = event.data?.after?.data() || {};
      console.log("Datos ANTES del cambio:", beforeData);
      console.log("Datos DESPUÉS del cambio:", afterData);

      const saldoAntes = Number(beforeData.puntos_tienda || 0);
      const saldoDespues = Number(afterData.puntos_tienda || 0);
      const notiEnviada = afterData.ultima_notificacion_enviada || false;
      const propietarios = afterData.propietario_id || [];
      const idTienda = event.params.id_tienda;

      console.log("Saldo antes:", saldoAntes);
      console.log("Saldo después:", saldoDespues);
      console.log("ultima_notificacion_enviada:", notiEnviada);
      console.log("Propietarios:", propietarios);

      // 🔹 Reiniciar flag si el saldo sube por encima del umbral
      if (saldoDespues >= 50 && notiEnviada) {
        await event.data.after.ref.update({
          ultima_notificacion_enviada: false,
        });
        console.log(
          "✅ Flag de notificación reiniciado porque saldo subió >=50",
        );
      }

      // 🔹 Enviar notificación solo si el saldo baja de 50 y antes estaba >=50
      if (saldoDespues < 50 && saldoAntes >= 50 && !notiEnviada) {
        console.log("⚠️ Saldo bajo detectado, enviando notificaciones...");

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
              `No se encontró doc de tokens para propietario ${propietarioId}`,
            );
            continue;
          }

          const tokensMap = tokenDoc.data()?.tokens || {};
          const tokens = Object.values(tokensMap);

          if (tokens.length === 0) continue;

          for (const token of tokens) {
            await enviarNotificacionFCM_tienda({
              token,
              title: `⚠️ ¡Tu saldo está bajo!`,
              body: `Tu tienda tiene menos de 50 monedas. Mantén tu alcance y visibilidad activo recargando cuando puedas 💼✨`,
              link: "https://geinzworkapp.web.app/share?t=scr&id=rec",
              logo: "https://firebasestorage.googleapis.com/v0/b/geinzworkapp.appspot.com/o/logo_geinz_webp.webp?alt=media&token=aa1ef1df-1bcd-48f2-9cad-a85929c3a8d0",
              idTienda,
              idAnuncio: "",
              tipo_notificacion: "logo",
              prioridad: "high",
            });
          }
        }

        // 🔹 Marcar que ya se envió la notificación
        await event.data.after.ref.update({
          ultima_notificacion_enviada: true,
        });
        console.log("✅ Notificación enviada y flag actualizado");
      } else {
        console.log(
          "No se cumple condición de saldo bajo o ya se envió notificación",
        );
      }
    } catch (error) {
      console.error("ERROR alertaSaldoBajo:", error);
    }
  },
);

exports.resetearEstadoNotificacionesYPanel = onSchedule(
  {
    schedule: "0 0 * * *", // Cada minuto
    timeZone: "America/Lima",
  },
  async () => {
    try {
      const ahora = admin.firestore.Timestamp.now();
      logger.info("⏱ Ejecutando resetearEstadoNotificacionesYPanel", {
        ahora: ahora.toDate().toISOString(),
      });

      const snapshot = await admin
        .firestore()
        .collectionGroup("tiendas_servicios_geinz_activos")
        .get();

      if (snapshot.empty) {
        logger.info("✅ No hay documentos para verificar");
        return;
      }

      const batch = admin.firestore().batch();
      const propietariosSet = new Map(); // propietarioId => Set("notificaciones"|"panel")
      let huboReseteo = false;

      snapshot.docs.forEach((doc) => {
        const data = doc.data();
        const propietarios = data.propietario_id || [];

        // --- NOTIFICACIONES (resetear si vencidas) ---
        const noti = data.notificaciones;
        if (
          noti?.timestamp_fin &&
          noti.timestamp_fin.toMillis() <= ahora.toMillis()
        ) {
          batch.update(doc.ref, {
            "notificaciones.contador": 0,
            "notificaciones.fecha_inicio": "",
            "notificaciones.fecha_fin": "",
            "notificaciones.promocion_nueva": false,
            "notificaciones.ultima_notificacion_enviada": false,
            "notificaciones.timestamp_fin": admin.firestore.FieldValue.delete(),
          });
          propietarios.forEach((p) => {
            if (!propietariosSet.has(p)) propietariosSet.set(p, new Set());
            propietariosSet.get(p).add("notificaciones");
          });
          huboReseteo = true;
        }

        // --- PANEL ADMIN (solo notificar si vencido) ---
        const panel = data.panel_admin;
        if (
          panel?.timestamp_fin &&
          panel.timestamp_fin.toMillis() <= ahora.toMillis()
        ) {
          propietarios.forEach((p) => {
            if (!propietariosSet.has(p)) propietariosSet.set(p, new Set());
            propietariosSet.get(p).add("panel");
          });
          huboReseteo = true; // marcar que hay algo que notificar
        }
      });

      if (!huboReseteo) return;
      await batch.commit();
      logger.info("♻️ Reseteo de notificaciones completado y panel revisado");

      // --- Enviar notificaciones ---
      for (const [propietarioId, tipos] of propietariosSet) {
        const tokenDoc = await admin
          .firestore()
          .collection("Trabajadores_Usuarios_Drivers")
          .doc("users")
          .collection("tokens")
          .doc(propietarioId)
          .get();

        if (!tokenDoc.exists) continue;
        const tokensMap = tokenDoc.data()?.tokens || {};
        const tokens = Object.values(tokensMap);
        if (tokens.length === 0) continue;

        for (const token of tokens) {
          if (tipos.has("notificaciones")) {
            await enviarNotificacionFCM_tienda({
              token,
              title: "♻️ ¡Tus notificaciones fueron renovadas!",
              body: "✨ Ya puedes enviar nuevas notificaciones y mantener a tus clientes al día 🔔🚀",
              link: "https://geinzworkapp.web.app/share?t=scr&id=ads",
              logo: "https://firebasestorage.googleapis.com/v0/b/geinzworkapp.appspot.com/o/logo_geinz_webp.webp?alt=media&token=aa1ef1df-1bcd-48f2-9cad-a85929c3a8d0",
              idTienda: "",
              idAnuncio: "",
              tipo_notificacion: "logo",
              prioridad: "high",
            });
          }

          if (tipos.has("panel")) {
            await enviarNotificacionFCM_tienda({
              token,
              title: "⏰ Tu panel vencio hoy 😣",
              body: "⚡ Renueva tu panel para seguir teniendo control de tu negocio 📈💼",
              link: "https://geinzworkapp.web.app/share?t=scr&id=pnl",
              logo: "https://firebasestorage.googleapis.com/v0/b/geinzworkapp.appspot.com/o/logo_geinz_webp.webp?alt=media&token=aa1ef1df-1bcd-48f2-9cad-a85929c3a8d0",
              idTienda: "",
              idAnuncio: "",
              tipo_notificacion: "logo",
              prioridad: "high",
            });
          }
        }
      }
    } catch (error) {
      logger.error("❌ Error en resetearEstadoNotificacionesYPanel", error);
    }
  },
);

async function enviarNotificacionFCM_tienda({
  token,
  title,
  body,
  link = "https://geinzworkapp.web.app/share?t=scr&id=ads",
  logo = "https://firebasestorage.googleapis.com/v0/b/geinzworkapp.appspot.com/o/logo_geinz_webp.webp?alt=media&token=aa1ef1df-1bcd-48f2-9cad-a85929c3a8d0",
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
      android: { priority: prioridad },
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

exports.recognizeSpeech = onRequest(
  { region: "us-central1" },
  async (req, res) => {
    if (req.method !== "POST") {
      return res.status(405).send("Método no permitido xd");
    }

    try {
      const audioBytes = req.body.audio; // base64
      if (!audioBytes) return res.status(400).send("Falta el audio");

      const audio = { content: audioBytes };

      const config = {
        encoding: "LINEAR16",
        sampleRateHertz: 16000,
        languageCode: "es-PE",
        useEnhanced: true,
        model: "default",
        speechContexts: [
          { phrases: ["creatina", "chifa", "pollo a la brasa"] },
        ],
      };

      const request = { audio, config };
      const [response] = await client_specth.recognize(request);

      const transcription = response.results
        .map((result) => result.alternatives[0].transcript)
        .join(" ");

      res.json({ text: transcription });
    } catch (err) {
      console.error("Error recognizeSpeech:", err);
      res.status(500).send(err.message);
    }
  },
);

exports.textToSpeechIA = onRequest(
  { region: "us-central1" },
  async (req, res) => {
    try {
      if (req.method !== "POST") {
        return res.status(405).send("Método no permitido");
      }

      const { texto } = req.body;
      if (!texto) return res.status(400).send("Falta texto");

      // 🔹 Limpiar texto (quitar emojis y símbolos raros)
      const textoLimpio = String(texto)
        .replace(/[\u{1F600}-\u{1F6FF}]/gu, "")
        .replace(/[\u{2700}-\u{27BF}]/gu, "");

      if (textoLimpio.length === 0)
        return res.status(400).send("Texto inválido");

      // 🔹 Dividir texto en trozos de 5000 caracteres (limite API)
      const CHUNK_SIZE = 4500; // un poco menos de 5000 para seguridad
      const chunks = [];
      for (let i = 0; i < textoLimpio.length; i += CHUNK_SIZE) {
        chunks.push(textoLimpio.slice(i, i + CHUNK_SIZE));
      }

      // 🔹 Generar audio para cada trozo y concatenar
      let audioFinal = Buffer.alloc(0);

      for (const chunk of chunks) {
        const request = {
          input: { text: chunk },
          voice: {
            languageCode: "es-US",
            name: "es-US-Standard-B", // rápido y natural
          },
          audioConfig: {
            audioEncoding: "MP3",
            speakingRate: 1.0,
          },
        };

        const [response] = await ttsClient.synthesizeSpeech(request);
        audioFinal = Buffer.concat([audioFinal, response.audioContent]);
      }

      res.set("Content-Type", "audio/mpeg");
      res.send(audioFinal);
    } catch (error) {
      console.error("❌ Error TTS:", error.code, error.message);
      res.status(500).send(error.message);
    }
  },
);

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
