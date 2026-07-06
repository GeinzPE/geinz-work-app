const { onRequest } = require("firebase-functions/v2/https");
const axios = require("axios");
const admin = require("firebase-admin");

if (!admin.apps.length) {
  admin.initializeApp();
}

const CULQI_KEY_SCAG = process.env.CULQI_KEY_SCAG;
const PHONE_ID = process.env.SCAG_WHATSAP_ID;
const WHATSAPP_TOKEN = process.env.SCAG_AI_WHATSAP_KEY;
let db2 = null;

const initDb2 = () => {
  if (db2) return db2;
  try {
    const appExistente = admin.apps.find((app) => app.name === "app2");
    if (appExistente) {
      db2 = appExistente.firestore();
    } else {
      const app2 = admin.initializeApp(
        {
          credential: admin.credential.cert({
            projectId: process.env.PROJECT_ID_2,
            clientEmail: process.env.CLIENT_EMAIL_2,
            privateKey: process.env.PRIVATE_KEY_2?.replace(/\\n/g, "\n"),
          }),
          storageBucket: `${process.env.PROJECT_ID_2}.appspot.com`,
        },
        "app2",
      );
      db2 = app2.firestore();
    }
  } catch (e) {
    console.error("❌ Error inicializando app2:", e.message);
    db2 = null;
  }
  return db2;
};

const initBucket2 = () => {
  if (bucket2) return bucket2;
  try {
    const appExistente = admin.apps.find((app) => app.name === "app2");
    if (appExistente) {
      bucket2 = appExistente.storage().bucket();
    }
  } catch (e) {
    console.error("❌ Error inicializando bucket app2:", e.message);
    bucket2 = null;
  }
  return bucket2;
};

async function emitirBoletaNubefact({
  userId,
  monedas,
  chargeId,
  monto,
  email,
  nombre,
  documento,
  direccion,
  tipoDocCliente,
}) {
  console.log("=== INICIANDO EMISIÓN BOLETA NUBEFACT (SCAG AI) ===");
  console.log("Argumentos recibidos:", {
    userId,
    monedas,
    chargeId,
    monto,
    email,
    nombre,
    documento,
    direccion,
    tipoDocCliente,
  });

  const montoNum = Number(monto);
  const tasaIgv = 0.18;
  const valorUnitario = montoNum / (1 + tasaIgv);
  const igvTotal = montoNum - valorUnitario;

  console.log("Cálculo IGV SCAG AI:", {
    montoNum,
    valorUnitario: valorUnitario.toFixed(2),
    igvTotal: igvTotal.toFixed(2),
  });

  const payload = {
    operacion: "generar_comprobante",
    tipo_de_comprobante: 2,
    serie: "BBB2", // 🚀 Serie única para SCAG AI (Local 0002)
    numero: 0,
    sunat_transaction: 1,

    cliente_tipo_de_documento: tipoDocCliente || "-",
    cliente_numero_de_documento: documento || "0",
    cliente_denominacion: nombre || "Consumidor Final",
    cliente_direccion: direccion || "",
    cliente_email: email || "",

    fecha_de_emision: new Date().toLocaleDateString("en-CA", {
      timeZone: "America/Lima",
    }),
    moneda: 1,
    porcentaje_de_igv: 18.0,

    total_gravada: valorUnitario.toFixed(2),
    total_igv: igvTotal.toFixed(2),
    total: montoNum.toFixed(2),

    items: [
      {
        unidad_de_medida: "ZZ",
        codigo: "MON001",
        // 📝 Descripción adaptada a tu extensión
        descripcion: `SERVICIO DIGITAL - PROCESAMIENTO DE ${monedas} CONSULTAS EN LA NUBE`,
        cantidad: 1,
        valor_unitario: valorUnitario.toFixed(2),
        precio_unitario: montoNum.toFixed(2),
        subtotal: valorUnitario.toFixed(2),
        tipo_de_igv: 1,
        igv: igvTotal.toFixed(2),
        total: montoNum.toFixed(2),
        anticipo_regularizacion: false,
      },
    ],
    enviar_automaticamente_a_la_sunat: true,
    enviar_automaticamente_al_cliente: !!email,
    codigo_unico: chargeId,
  };

  console.log(
    "Payload a enviar a NubeFact (SCAG AI):",
    JSON.stringify(payload, null, 2),
  );

  try {
    const response = await axios.post(
      "https://api.nubefact.com/api/v1/02bb7d82-0b0c-4006-82a5-74b7437bea0b",
      payload,
      {
        headers: {
          Authorization: `Token token="b8b2a35495954bceaefe0716de425bbcb605a4094396474db278bd8a292121f6"`,
          "Content-Type": "application/json",
        },
      },
    );

    console.log(
      "✅ Respuesta completa NubeFact SCAG AI:",
      JSON.stringify(response.data, null, 2),
    );
    console.log("🔗 enlace_del_pdf obtenido:", response.data.enlace_del_pdf);

    if (!response.data.enlace_del_pdf) {
      console.error(
        "⚠️ enlace_del_pdf es null/undefined. Respuesta completa:",
        response.data,
      );
    }

    return response.data.enlace_del_pdf;
  } catch (error) {
    console.error("❌ ERROR EN NUBEFACT SCAG AI:");
    if (error.response) {
      console.error("Status HTTP:", error.response.status);
      console.error(
        "Data del error:",
        JSON.stringify(error.response.data, null, 2),
      );
    } else {
      console.error("Mensaje:", error.message);
    }
    throw error;
  }
}

async function guardarPDFEnStorage(urlPdf, idTransaccion, alias) {
  const bucketName = process.env.STORAGE_BUCKET_MAIN;
  const bucket = admin.storage().bucket(bucketName);

  console.log("📦 Subiendo PDF al bucket:", bucket.name);

  const pdfResponse = await axios.get(urlPdf, { responseType: "arraybuffer" });
  const buffer = Buffer.from(pdfResponse.data);

  const rutaArchivo = `boletas_scag/${alias}/${idTransaccion}.pdf`;
  const file = bucket.file(rutaArchivo);

  await file.save(buffer, {
    metadata: { contentType: "application/pdf" },
  });

  await file.makePublic();

  return `https://storage.googleapis.com/${bucket.name}/${rutaArchivo}`;
}

async function agregarHistorialUsuario(dbPlanes, alias, datos) {
  const historialRef = dbPlanes
    .collection("trabajos_ia")
    .doc(alias)
    .collection("historial")
    .doc();

  const reciboRef = dbPlanes.collection("RECIBOS_SCAG").doc(historialRef.id);

  const datosConAlias = { ...datos, alias };

  await Promise.all([
    historialRef.set(datosConAlias),
    reciboRef.set(datosConAlias),
  ]);

  return historialRef.id;
}

exports.crearOrdenCulqiPlan = onRequest({ cors: true }, async (req, res) => {
  try {
    const { alias, plan_select, precio_soles, creditos, email, telefono } =
      req.body || {};

    if (!alias || !plan_select || !precio_soles) {
      res.status(400).json({
        error:
          "Faltan parámetros: alias, plan_select y precio_soles son obligatorios.",
      });
      return;
    }

    const dbPlanes = initDb2();
    if (!dbPlanes) {
      res.status(500).json({ error: "Error de conexión a la base de datos." });
      return;
    }

    const monto = Number(precio_soles);

    const perfilRef = dbPlanes.collection("trabajos_ia").doc(alias);
    const perfilSnap = await perfilRef.get();
    const idPagoExistente = perfilSnap.exists
      ? perfilSnap.data()?.id_pago
      : null;

    const orderNumber = `PLAN-${alias.slice(0, 8)}-${Date.now().toString().slice(-8)}`;

    const response = await axios.post(
      "https://api.culqi.com/v2/orders",
      {
        amount: Math.round(monto * 100),
        currency_code: "PEN",
        description: `Plan ${plan_select}`,
        order_number: orderNumber,
        client_details: {
          first_name: "Cliente",
          last_name: "Scag AI",
          email: email || `soybenjadesing@gmail.com`,
          phone_number: telefono || "937659216",
        },
        expiration_date: Math.floor(Date.now() / 1000) + 900,
        confirm: false,
      },
      {
        headers: {
          Authorization: `Bearer ${CULQI_KEY_SCAG}`,
          "Content-Type": "application/json",
        },
      },
    );

    const culqi_order_id = response.data.id;
    const fecha = admin.firestore.Timestamp.now();

    const datosPago = {
      alias,
      plan_select,
      precio_soles: monto,
      creditos: Number(creditos || 0),
      fecha,
      estado: "pendiente",
      order_number_culqi: orderNumber,
      culqi_order_id,
      email: email || null,
      telefono: telefono || null,
    };

    let idPago;

    if (idPagoExistente) {
      idPago = idPagoExistente;
      await dbPlanes
        .collection("pagos_scag")
        .doc(idPago)
        .set(datosPago, { merge: true });
    } else {
      const nuevoDocRef = dbPlanes.collection("pagos_scag").doc();
      idPago = nuevoDocRef.id;
      await nuevoDocRef.set(datosPago);
      await perfilRef.set({ id_pago: idPago }, { merge: true });
    }

    res.status(200).json({
      culqi_order_id,
      order_number_culqi: orderNumber,
      id_pago: idPago,
      monto,
      creditos: Number(creditos || 0),
    });
  } catch (error) {
    const culqiError = error.response?.data;
    console.error("ERROR ORDEN CULQI:", culqiError || error.message);
    res.status(400).json({
      error: culqiError?.user_message || "Error creando la orden de pago",
    });
  }
});

exports.confirmarPagoPlan = onRequest({ cors: true }, async (req, res) => {
  const {
    alias,
    plan_select,
    precio_soles,
    creditos,
    token,
    email,
    telefono,
    id_pago,
  } = req.body || {};

  if (!alias || !plan_select || !precio_soles || !token || !id_pago) {
    res.status(400).json({
      error:
        "Faltan parámetros: alias, plan_select, precio_soles, token e id_pago son obligatorios.",
    });
    return;
  }

  const dbPlanes = initDb2();
  if (!dbPlanes) {
    res.status(500).json({ error: "Error de conexión a la base de datos." });
    return;
  }

  const monto = Number(precio_soles);
  const creditosComprados = Number(creditos || 0);

  try {
    const response = await axios.post(
      "https://api.culqi.com/v2/charges",
      {
        amount: Math.round(monto * 100),
        currency_code: "PEN",
        email: email || `soybenjadesing@gmail.com`,
        source_id: token,
        capture: true,
        description: `Compra de plan ${plan_select} - Scag AI`,
        antifraud_details: {
          address: "Barranca",
          address_city: "Barranca",
          country_code: "PE",
          first_name: "Cliente",
          last_name: "Scag AI",
          phone: telefono || "937659216",
        },
      },
      {
        headers: {
          Authorization: `Bearer ${CULQI_KEY_SCAG}`,
          "Content-Type": "application/json",
        },
      },
    );

    const charge = response.data;

    const perfilRef = dbPlanes.collection("trabajos_ia").doc(alias);
    let creditosAntes = 0;
    let creditosRestantes = 0;

    await dbPlanes.runTransaction(async (tx) => {
      const perfilSnap = await tx.get(perfilRef);
      creditosAntes = perfilSnap.exists
        ? Number(perfilSnap.data()?.creditos || 0)
        : 0;
      creditosRestantes = creditosAntes + creditosComprados;

      tx.set(
        perfilRef,
        {
          creditos: creditosRestantes,
          id_pago: admin.firestore.FieldValue.delete(),
        },
        { merge: true },
      );
    });

    await dbPlanes.collection("pagos_scag").doc(id_pago).delete();

    let urlBoletaStorage = null;
    try {
      const urlNubefact = await emitirBoletaNubefact({
        userId: alias,
        monedas: creditosComprados,
        chargeId: charge.id,
        monto,
        email: email || "soybenjadesing@gmail.com",
        nombre: "Consumidor final",
        telefono: telefono || "",
      });

      urlBoletaStorage = await guardarPDFEnStorage(
        urlNubefact,
        charge.id,
        alias,
      );

      await perfilRef.set({ urlBoleta: urlBoletaStorage }, { merge: true });
    } catch (nubefactErr) {
      console.error(
        "⚠️ Error emitiendo/guardando boleta NubeFact:",
        nubefactErr.response?.data || nubefactErr.message,
      );
    }

    // ── 5. Registrar en el historial del usuario (trabajos_ia/{alias}/historial) ──
    let idHistorial = null;
    try {
      idHistorial = await agregarHistorialUsuario(dbPlanes, alias, {
        categoria: "",
        costoSoles: Number(precio_soles),
        creditosAntes,
        creditosConsumidos: 0,
        creditosRestantes,
        fecha: admin.firestore.Timestamp.now(),
        modelo: "",
        tipo: "Recarga",
        urlComprobante: urlBoletaStorage,
      });
    } catch (e) {
      console.error("⚠️ Error historial webhook:", e.message);
    }

    try {
      await enviarWhatsApp_pago_exitoso(
        telefono,
        alias,
        idHistorial ?? pagoDoc.id,
      );
    } catch (e) {
      console.error("⚠️ Error WhatsApp pago exitoso:", e.message);
    }
    res.status(200).json({
      ok: true,
      chargeId: charge.id,
      urlBoleta: urlBoletaStorage,
    });
  } catch (error) {
    const culqiError = error.response?.data;
    console.error("ERROR CHARGE:", culqiError || error.message);
    const motivo = culqiError?.user_message || "Error en el pago";

    // 📲 Enviar WhatsApp de pago rechazado
    try {
      await enviarWhatsAppRechazado(telefono, alias, motivo);
    } catch (waErr) {
      console.error("⚠️ Error WhatsApp pago rechazado:", waErr.message);
    }

    res.status(400).json({ error: motivo });
  }
});

exports.webhookCulqiOrder_scagAI = onRequest(
  { cors: true },
  async (req, res) => {
    try {
      const evento = req.body || {};

      console.log(
        "📩 Webhook Culqi recibido:",
        JSON.stringify(evento, null, 2),
      );

      if (evento.type !== "order.status.changed") {
        console.log("ℹ️ Evento ignorado, type:", evento.type);
        res.sendStatus(200);
        return;
      }

      // 🔧 Parseo defensivo: Culqi documenta "data" como string,
      // aunque a veces llega ya como objeto según el proveedor/integración.
      let dataEvento = evento.data;
      if (typeof dataEvento === "string") {
        try {
          dataEvento = JSON.parse(dataEvento);
        } catch (e) {
          console.error("⚠️ No se pudo parsear evento.data:", e.message);
          res.sendStatus(200);
          return;
        }
      }

      // 🔑 CAMBIO CLAVE: Culqi puede mandar el objeto de la orden
      // directamente en "data" o anidado en "data.object", según el
      // formato del evento. Soportamos ambos casos.
      const ordenObj = dataEvento;
      const ordenId = ordenObj?.id;

      if (!ordenId) {
        console.error(
          "⚠️ No se encontró id de orden en el evento. dataEvento:",
          JSON.stringify(dataEvento),
        );
        res.sendStatus(200);
        return;
      }

      console.log("🆔 Orden ID detectada:", ordenId);

      // 🔒 Importante: no confíes ciegamente en el body del webhook,
      // vuelve a consultar la orden a la API de Culqi para verificar su estado real.
      const ordenReal = await axios.get(
        `https://api.culqi.com/v2/orders/${ordenId}`,
        { headers: { Authorization: `Bearer ${CULQI_KEY_SCAG}` } },
      );

      console.log("📦 Estado real de la orden en Culqi:", ordenReal.data.state);

      if (ordenReal.data.state !== "paid") {
        res.sendStatus(200);
        return;
      }

      const dbPlanes = initDb2();

      // Buscamos el pago pendiente asociado a esa orden
      const snap = await dbPlanes
        .collection("pagos_scag")
        .where("culqi_order_id", "==", ordenId)
        .limit(1)
        .get();

      if (snap.empty) {
        console.log(
          "ℹ️ No se encontró pago pendiente para culqi_order_id:",
          ordenId,
          "(ya procesado antes o no existe)",
        );
        res.sendStatus(200);
        return;
      }

      const pagoDoc = snap.docs[0];
      const { alias, creditos, precio_soles, plan_select, email, telefono } =
        pagoDoc.data(); // AGREGADO: telefono
      const creditosComprados = Number(creditos || 0);
      const perfilRef = dbPlanes.collection("trabajos_ia").doc(alias);

      let creditosAntes = 0;
      let creditosRestantes = 0;

      await dbPlanes.runTransaction(async (tx) => {
        const perfilSnap = await tx.get(perfilRef);
        creditosAntes = perfilSnap.exists
          ? Number(perfilSnap.data()?.creditos || 0)
          : 0;
        creditosRestantes = creditosAntes + creditosComprados;

        tx.set(
          perfilRef,
          {
            creditos: creditosRestantes,
            id_pago: admin.firestore.FieldValue.delete(),
          },
          { merge: true },
        );
      });

      await pagoDoc.ref.delete();

      console.log(
        `✅ Créditos acreditados para alias "${alias}": ${creditosAntes} → ${creditosRestantes}`,
      );

      // (Opcional pero recomendado) boleta + historial, igual que en confirmarPagoPlan
      let urlBoletaStorage = null;
      try {
        const urlNubefact = await emitirBoletaNubefact({
          userId: alias,
          monedas: creditosComprados,
          chargeId: ordenId,
          monto: Number(precio_soles),
          email: email || "",
          nombre: "Consumidor final",
          telefono: telefono || "", // AGREGADO: solo si emitirBoletaNubefact acepta este campo
        });
        urlBoletaStorage = await guardarPDFEnStorage(
          urlNubefact,
          ordenId,
          alias,
        );
        await perfilRef.set({ urlBoleta: urlBoletaStorage }, { merge: true });
      } catch (e) {
        console.error(
          "⚠️ Error boleta webhook:",
          e.response?.data || e.message,
        );
      }

      // ── Guardamos el historial y capturamos el ID real del documento ──
      let idHistorial = null;
      try {
        idHistorial = await agregarHistorialUsuario(dbPlanes, alias, {
          categoria: "",
          costoSoles: Number(precio_soles),
          creditosAntes,
          creditosConsumidos: 0,
          creditosRestantes,
          fecha: admin.firestore.Timestamp.now(),
          modelo: "",
          tipo: "Recarga",
          urlComprobante: urlBoletaStorage,
        });
      } catch (e) {
        console.error("⚠️ Error historial webhook:", e.message);
      }

      // 📲 Usamos el ID del historial (no el de pagos_scag) para el botón de WhatsApp
      try {
        await enviarWhatsApp_pago_exitoso(
          telefono,
          alias,
          idHistorial ?? pagoDoc.id,
        );
      } catch (e) {
        console.error("⚠️ Error WhatsApp pago exitoso:", e.message);
      }

      res.sendStatus(200);
    } catch (error) {
      console.error(
        "ERROR WEBHOOK CULQI:",
        error.response?.data || error.message,
      );
      res.sendStatus(200);
    }
  },
);

async function enviarWhatsApp_pago_exitoso(telefono, nombre_user, idPago) {
  console.log("📲 [enviarWhatsApp_pago_exitoso] Iniciando con params:", {
    telefono,
    nombre_user,
    idPago,
  });

  try {
    if (!telefono) {
      console.error(
        "⚠️ [enviarWhatsApp_pago_exitoso] telefono vacío o undefined, usando default",
      );
    }
    if (!nombre_user) {
      console.error(
        "⚠️ [enviarWhatsApp_pago_exitoso] nombre_user vacío o undefined",
      );
    }
    if (!idPago) {
      console.error(
        "⚠️ [enviarWhatsApp_pago_exitoso] idPago vacío o undefined",
      );
    }

    const telefonoFinal = `51${telefono || "937659216"}`;
    const rutaBoton = `comprobante/?id=${idPago}`;

    const payload = {
      messaging_product: "whatsapp",
      to: telefonoFinal,
      type: "template",
      template: {
        name: "pago_exitoso",
        language: {
          code: "es",
        },
        components: [
          {
            type: "body",
            parameters: [
              {
                type: "text",
                text: nombre_user,
              },
            ],
          },
          {
            type: "button",
            sub_type: "url",
            index: "0",
            parameters: [
              {
                type: "text",
                text: rutaBoton,
              },
            ],
          },
        ],
      },
    };

    console.log("========== WHATSAPP TEMPLATE ==========");
    console.log("PHONE_ID:", PHONE_ID);
    console.log("telefono:", telefonoFinal);
    console.log("nombre_user:", nombre_user);
    console.log("rutaBoton:", rutaBoton);
    console.log("payload:");
    console.log(JSON.stringify(payload, null, 2));
    console.log("======================================");

    const res = await axios.post(
      `https://graph.facebook.com/v19.0/${PHONE_ID}/messages`,
      payload,
      {
        headers: {
          Authorization: `Bearer ${WHATSAPP_TOKEN}`,
          "Content-Type": "application/json",
        },
      },
    );

    console.log("✅ WhatsApp enviado correctamente. Status:", res.status);
    console.log("✅ Respuesta completa:", JSON.stringify(res.data, null, 2));
    return true;
  } catch (error) {
    console.error("❌ ERROR WHATSAPP - status:", error.response?.status);
    console.error(
      "❌ ERROR WHATSAPP - data:",
      JSON.stringify(error.response?.data, null, 2) || error.message,
    );
    console.error("❌ ERROR WHATSAPP - message:", error.message);
    return false;
  }
}

async function enviarWhatsAppRechazado(telefono, nombre_user, motivo) {
  try {
    if (!telefono) {
      console.error(
        "⚠️ [enviarWhatsAppRechazado] telefono vacío o undefined, usando default",
      );
    }

    const telefonoFinal = `51${telefono || "937659216"}`;

    const payload = {
      messaging_product: "whatsapp",
      to: telefonoFinal,
      type: "template",
      template: {
        name: "pago_rechazado",
        language: {
          code: "es",
        },
        components: [
          {
            type: "body",
            parameters: [
              {
                type: "text",
                text: nombre_user,
              },
              {
                type: "text",
                text: motivo,
              },
            ],
          },
        ],
      },
    };

    console.log("========== WHATSAPP TEMPLATE ==========");
    console.log("telefono:", telefonoFinal);
    console.log("nombre_user:", nombre_user);
    console.log("motivo:", motivo);
    console.log("payload:");
    console.log(JSON.stringify(payload, null, 2));
    console.log("======================================");

    const res = await axios.post(
      `https://graph.facebook.com/v19.0/${PHONE_ID}/messages`,
      payload,
      {
        headers: {
          Authorization: `Bearer ${WHATSAPP_TOKEN}`,
          "Content-Type": "application/json",
        },
      },
    );

    console.log("✅ WhatsApp enviado:", res.data);
    return true;
  } catch (error) {
    console.error("❌ ERROR WHATSAPP - status:", error.response?.status);
    console.error(
      "❌ ERROR WHATSAPP - data:",
      JSON.stringify(error.response?.data, null, 2) || error.message,
    );
    return false;
  }
}
