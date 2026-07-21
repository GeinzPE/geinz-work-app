const {
  onCall,
  onRequest,
  HttpsError,
} = require("firebase-functions/v2/https");
const axios = require("axios");
const admin = require("firebase-admin");

if (!admin.apps.length) {
  admin.initializeApp();
}

const db = admin.firestore();

const CULQI_KEY = process.env.CULQI_KEY;
const PHONE_ID = process.env.ID_NUMBER_WHATSAPP;
const WHATSAPP_TOKEN = process.env.ID_API_WHATSAPP;

// NOTA: este archivo es un fragmento — asume que arriba en tu index.js ya
// tienes los requires/inicializaciones de: admin, db, axios, onCall, onRequest,
// HttpsError, CULQI_KEY, WHATSAPP_TOKEN, PHONE_ID, enviarNotificacionFCM_tienda,
// eliminar_deuda_actual.

// ============================================================
// EXPORTS (funciones invocables)
// ============================================================

async function enviarNotificacionFCM_tienda({
  token,
  title,
  body,
  link = "https://geinztech.com/share?t=scr&id=ads",
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
exports.crearOrdenCulqi = onCall(async (req) => {
  const { monto, userId, nombre, email, orderId } = req.data;

  console.log("📥 Datos recibidos:");
  console.log("  monto:", monto);
  console.log("  tipo monto:", typeof monto);
  console.log("  amount calculado:", Math.round(monto * 100));
  console.log("  userId:", userId);

  const orderNumber = `ORD-${userId.slice(0, 8)}-${Date.now().toString().slice(-8)}`;

  const response = await axios.post(
    "https://api.culqi.com/v2/orders",
    {
      amount: Math.round(monto * 100),
      currency_code: "PEN",
      description: `Monedas Geinz - ${nombre}`,
      order_number: orderNumber,
      client_details: {
        first_name: nombre || "Cliente",
        last_name: "Geinz",
        email: email || "cliente@geinz.com",
        phone_number: "999999999",
      },
      expiration_date: Math.floor(Date.now() / 1000) + 900,
      confirm: false,
    },
    {
      headers: {
        Authorization: `Bearer ${CULQI_KEY}`,
        "Content-Type": "application/json",
      },
    },
  );

  console.log("✅ Respuesta Culqi:");
  console.log("  amount:", response.data.amount);
  console.log("  state:", response.data.state);
  console.log("  order_id:", response.data.id);

  const culqi_order_id = response.data.id;

  if (orderId) {
    await db
      .collection("Tiendas")
      .doc("barranca")
      .collection("pagos_tiendas")
      .doc(orderId)
      .set(
        { order_number_culqi: orderNumber, culqi_order_id },
        { merge: true },
      );
  }

  return { culqi_order_id };
});

exports.culqiWebhook = onRequest({ cors: true }, async (req, res) => {
  try {
    const evento = req.body || {};
    console.log(
      "📩 Webhook Culqi (Geinz) recibido:",
      JSON.stringify(evento, null, 2),
    );

    if (evento.type !== "order.status.changed") {
      console.log("ℹ️ Evento ignorado, type:", evento.type);
      return res.sendStatus(200);
    }

    // Culqi documenta "data" como string, aunque a veces llega ya como objeto.
    let dataEvento = evento.data;
    if (typeof dataEvento === "string") {
      try {
        dataEvento = JSON.parse(dataEvento);
      } catch (e) {
        console.error("⚠️ No se pudo parsear evento.data:", e.message);
        return res.sendStatus(200);
      }
    }

    // Soporta tanto data.id directo como data.object.id anidado
    const ordenObj = dataEvento?.object || dataEvento;
    const ordenId = ordenObj?.id;

    if (!ordenId) {
      console.error(
        "⚠️ No se encontró id de orden en el evento:",
        JSON.stringify(dataEvento),
      );
      return res.sendStatus(200);
    }

    console.log("🆔 Orden ID detectada:", ordenId);

    // 🔒 No confiar en el body del webhook: reconsultar el estado real a Culqi
    const ordenReal = await axios.get(
      `https://api.culqi.com/v2/orders/${ordenId}`,
      { headers: { Authorization: `Bearer ${CULQI_KEY}` } }, // 👈 misma key de Geinz
    );

    console.log("📦 Estado real de la orden en Culqi:", ordenReal.data.state);

    if (ordenReal.data.state !== "paid") {
      return res.sendStatus(200);
    }

    const pagosRef = db
      .collection("Tiendas")
      .doc("barranca")
      .collection("pagos_tiendas");

    // 🔑 Igual que SCAG: buscar por culqi_order_id, no por order_number
    const query = await pagosRef
      .where("culqi_order_id", "==", ordenId)
      .limit(1)
      .get();

    if (query.empty) {
      console.log(
        "❌ No se encontró pago pendiente para culqi_order_id:",
        ordenId,
      );
      return res.sendStatus(200);
    }

    const pagoDoc = query.docs[0];
    const datos = pagoDoc.data();
    const userId = datos.id_tienda;

    if (datos.estado === "pagado") {
      console.log("⚠️ Ya estaba pagado");
      return res.sendStatus(200);
    }

    const monedas = datos.monedas_a_recargar || datos.monedas;

    const numero = await sumarSaldo(userId, monedas);

    await agregar_historial_de_pagos_tienda({
      id_transaccion: pagoDoc.id,
      tipo_transaccion: "recarga",
      metodo_pago: "billetera_movil",
      nombre_tienda: datos.nombre_user,
      id_tienda: userId,
      localidad_tienda: datos.localdiad,
      tipo_paquete: datos.plan_select,
      monto_aumentado: monedas,
      precio_soles: (ordenReal.data.amount / 100).toString(),
      estado: "Aceptado",
      monto_anterior: datos.saldo_tienda || 0,
    });

    if (typeof numero === "string" && numero.length >= 9) {
      await enviarPlantillaWhatsApp({
        numero,
        nombreTienda: datos.nombre_user,
        monedas,
        idTransaccion: pagoDoc.id,
      });
    }

    await enviarWhatsApp(
      937659216,
      `Billetera exitoso`,
      `🏪 ${datos.nombre_user} realizo una recarga de ${monedas}`,
      String(ordenReal.data.amount / 100),
      String(monedas),
    );

    return res.sendStatus(200);
  } catch (err) {
    console.error("Webhook error (Geinz):", err.response?.data || err.message);
    return res.sendStatus(200); // 👈 evitar reintentos agresivos de Culqi
  }
});

exports.confirmarPago = onCall(async (req) => {
  console.log("=====================");
  const {
    tipo_comprobante,
    ruc,
    direccion_negocio,
    token,
    monto,
    email,
    userId,
    monedas,
    monedas_originales,
    deuda_pendiente,
    tiene_deuda,
    nombre_tienda,
    localidad,
    nombre_paquete,
    monto_anterior,
    id_select_boleta_pago,
  } = req.data;

  const ahora = new Date();
  const mes = String(ahora.getMonth() + 1).padStart(2, "0");
  const anio = ahora.getFullYear();
  const idConFecha = id_select_boleta_pago;
  const tieneDeudaBool = tiene_deuda === true || tiene_deuda === "true";
  const deudaPendienteNum = Number(deuda_pendiente || 0);

  console.log("Tipo Comprobante (1:Fact, 2:Bol):", tipo_comprobante);
  console.log("🔍 Deuda check:", {
    tiene_deuda,
    deuda_pendiente,
    tipo_tiene_deuda: typeof tiene_deuda,
    tipo_deuda_pendiente: typeof deuda_pendiente,
  });
  console.log("Datos Cliente:", {
    ruc_dni: ruc,
    nombre: nombre_tienda,
    direccion: direccion_negocio,
    email: email,
  });
  console.log("Datos Transacción:", {
    id_transaccion_geinz: id_select_boleta_pago,
    monto_soles: monto,
    monedas_a_recargar: monedas,
    paquete: nombre_paquete,
    culqi_token: token,
  });
  console.log("Contexto Usuario:", {
    userId: userId,
    saldo_previo: monto_anterior,
    ubicacion: localidad,
  });

  try {
    const response = await axios.post(
      "https://api.culqi.com/v2/charges",
      {
        amount: Math.round(monto * 100),
        currency_code: "PEN",
        email: email || "cliente@geinz.com",
        source_id: token,
        capture: true,
        description: "Compra de monedas Geinz",
        antifraud_details: {
          address: "Barranca",
          address_city: "Barranca",
          country_code: "PE",
          first_name: nombre_tienda || "Cliente",
          last_name: "Geinz",
          phone: "999999999",
        },
      },
      {
        headers: {
          Authorization: `Bearer ${CULQI_KEY}`,
          "Content-Type": "application/json",
        },
      },
    );

    const charge = response.data;
    console.log("CULQI RESPONSE:", charge);

    // ── 1. Historial recarga principal (CON notificación "Recarga exitosa") ──
    await agregar_historial_de_pagos_tienda({
      id_transaccion: idConFecha,
      tipo_transaccion: "recarga",
      metodo_pago: "yape",
      nombre_tienda: nombre_tienda,
      id_tienda: userId,
      localidad_tienda: localidad,
      tipo_paquete: nombre_paquete,
      monto_aumentado: monedas,
      precio_soles: monto.toString(),
      estado: "Aceptado",
      monto_anterior: monto_anterior,
      enviarNotificacion: true, // ← notifica "Recarga exitosa"
    });

    const numero = await sumarSaldo(userId, monedas);

    // ── 2. Bloque de deuda ──
    if (tieneDeudaBool && deudaPendienteNum > 0) {
      try {
        console.log("💳 Eliminando deuda pendiente:", deuda_pendiente);

        const resultadoDeuda = await eliminar_deuda_actual(userId);

        if (!resultadoDeuda.ok) {
          console.error(
            "❌ No se pudo eliminar la deuda:",
            resultadoDeuda.error,
          );
        } else {
          console.log("✅ deuda eliminada:", resultadoDeuda);
        }

        const deuda_soles = (Number(deuda_pendiente || 0) / 100).toFixed(2);

        // Historial descuento deuda SIN notificación (para no duplicar)
        await agregar_historial_de_pagos_tienda({
          id_transaccion: `${id_select_boleta_pago}_deuda`,
          tipo_transaccion: "descuento_deuda",
          metodo_pago: "saldo_automatico",
          nombre_tienda: nombre_tienda,
          id_tienda: userId,
          localidad_tienda: localidad,
          tipo_paquete: "Débito automático Geinz",
          monto_aumentado: Number(deuda_pendiente || 0),
          precio_soles: deuda_soles,
          estado: "Aceptado",
          monto_anterior: 0,
          enviarNotificacion: false, // ← NO notifica, evita el duplicado
        });

        console.log("🧾 historial deuda guardado");

        // Notificación "Deuda cancelada" — única, enviada aquí
        try {
          const tiendaDoc = await db
            .collection("Tiendas")
            .doc(localidad)
            .collection(localidad)
            .doc(userId)
            .get();

          const propietarios = tiendaDoc.data()?.propietario_id || [];

          for (const propietarioId of propietarios) {
            const tokenDoc = await db
              .collection("Trabajadores_Usuarios_Drivers")
              .doc("users")
              .collection("tokens")
              .doc(propietarioId)
              .get();

            const tokens = Object.values(tokenDoc.data()?.tokens || {});

            for (const token of tokens) {
              await enviarNotificacionFCM_tienda({
                token,
                title: "✅ ¡Deuda cancelada exitosamente!",
                body: `Tu deuda de ${deudaPendienteNum} créditos fue saldada automáticamente con tu recarga. Ya estás al día 🎉`,
                link: "https://geinztech.com/share?t=scr&id=rec",
                logo: "https://firebasestorage.googleapis.com/v0/b/geinzworkapp.appspot.com/o/logo_geinz_webp.webp?alt=media&token=aa1ef1df-1bcd-48f2-9cad-a85929c3a8d0",
                idTienda: userId,
                idAnuncio: "",
                tipo_notificacion: "logo",
                prioridad: "high",
              });
            }
          }
        } catch (notiDeudaErr) {
          console.error(
            "⚠️ Error enviando notificación de deuda cancelada:",
            notiDeudaErr,
          );
        }
      } catch (deudaErr) {
        console.error("❌ Error eliminando deuda:", deudaErr);
      }
    }

    // ── 3. Comprobante NubeFact ──
    try {
      const urlNubefact = await emitirComprobanteGeinz({
        tipoComprobante: tipo_comprobante,
        documento: ruc,
        nombre: nombre_tienda,
        direccion: direccion_negocio,
        monedas,
        chargeId: charge.id,
        monto,
        email: "cliente@geinz.com", // ⚠️ revisa esto: ignora el `email` real recibido arriba
      });

      const urlPDFStorage = await guardarPDFEnStorage(
        urlNubefact,
        idConFecha,
        userId,
      );

      await guardarURLComprobanteFirestore({
        idTransaccion: idConFecha,
        idTienda: userId,
        localidad,
        urlPDF: urlPDFStorage,
      });

      if (typeof numero === "string" && numero.length >= 9) {
        await enviarPlantillaWhatsApp({
          numero,
          nombreTienda: nombre_tienda,
          monedas,
          idTransaccion: idConFecha,
        });
      }

      await enviarWhatsApp(
        937659216,
        `exitoso en Geinz`,
        `Pago exitoso en Geinz 🏪 Negocio: ${nombre_tienda} 🧾 Comprobante: ${urlPDFStorage}`,
        String(monto),
        String(monedas),
      );
    } catch (nubefactErr) {
      console.error(
        "⚠️ Nubefact/Storage falló:",
        nubefactErr.response?.data || nubefactErr.message,
      );
    }

    return {
      ok: true,
      chargeId: charge.id,
    };
  } catch (error) {
    const numero = await obtenerNumeroWhatsApp(userId);
    const culqiError = error.response?.data;
    console.error("ERROR CHARGE:", culqiError || error.message);

    const motivo = culqiError?.user_message || "Error en el pago";

    await enviarWhatsAppRechazo(
      937659216, // numero
      "rechazado", // estadoPago
      nombre_tienda, // nombreUsuario
      `${motivo}`, // motivoRechazo
      String(monedas), // creditos
      String(monto), // montoSoles
      numero, // idUsuario
    );

    throw new HttpsError("failed-precondition", motivo);
  }
});

// ============================================================
// HELPERS - saldo / historial / deuda
// ============================================================

async function sumarSaldo(userId, monedas) {
  console.log("🟡 [sumarSaldo] INICIO");
  console.log("userId:", userId);
  console.log("monedas:", monedas);

  const ref = db
    .collection("Tiendas")
    .doc("barranca")
    .collection("barranca")
    .doc(userId);

  console.log("📄 Referencia doc creada");

  await ref.set(
    {
      puntos_tienda: admin.firestore.FieldValue.increment(monedas),
    },
    { merge: true },
  );

  console.log("✅ Saldo actualizado en Firestore");

  const snap = await ref.get();

  if (!snap.exists) {
    console.log("❌ Documento no existe");
    return null;
  }

  const data = snap.data();
  console.log("📦 DATA COMPLETA USUARIO:", JSON.stringify(data, null, 2));

  const numero = data?.metodo_contacto?.whatsapp?.numero || null;

  console.log("📲 Número WhatsApp extraído:", numero);

  return numero;
}

async function obtenerNumeroWhatsApp(userId) {
  const snap = await db
    .collection("Tiendas")
    .doc("barranca")
    .collection("barranca")
    .doc(userId)
    .get();

  if (!snap.exists) {
    return null;
  }

  return snap.data()?.metodo_contacto?.whatsapp?.numero || null;
}

async function agregar_historial_de_pagos_tienda({
  id_transaccion,
  tipo_transaccion,
  metodo_pago,
  nombre_tienda,
  id_tienda,
  localidad_tienda,
  tipo_paquete,
  monto_aumentado,
  precio_soles,
  estado,
  monto_anterior,
  enviarNotificacion = true, // ← por defecto true, pasar false para historial de deuda
}) {
  console.log("🚀 INICIANDO PROCESO PAGO COMPLETO");

  try {
    // HISTORIAL PRIMERO (más importante)
    const historialRef = db
      .collection("Tiendas")
      .doc(localidad_tienda)
      .collection(localidad_tienda)
      .doc(id_tienda)
      .collection("historial_financiero")
      .doc(id_transaccion);

    const data = {
      id_transaccion: id_transaccion,
      tipo_transacción: tipo_transaccion,

      hora_fecha: {
        fecha: new Date().toISOString(),
        hora: new Date().toLocaleTimeString("es-PE"),
      },

      metodo_pago: {
        yape: metodo_pago === "yape",
        plin: metodo_pago === "plin",
      },

      datos_tienda: {
        nombre_tienda,
        id_tienda,
        localidad_tienda,
      },

      datos_recarga: {
        tipo_paquete,
        monto_aumentado,
        precio_soles,
        estado,
        monto_anterior,
      },

      timestamp: admin.firestore.FieldValue.serverTimestamp(),
    };

    await historialRef.set(data);
    console.log("✅ HISTORIAL GUARDADO");

    // LUEGO ACTUALIZAS PAGO
    const pagoRef = db
      .collection("Tiendas")
      .doc(localidad_tienda)
      .collection("pagos_tiendas")
      .doc(id_transaccion);

    await pagoRef.set(
      {
        estado: "pagado",
        actualizado_en: admin.firestore.FieldValue.serverTimestamp(),
      },
      { merge: true },
    );

    console.log("💰 PAGO MARCADO COMO PAGADO");

    await db
      .collection("Tiendas")
      .doc(localidad_tienda)
      .collection(localidad_tienda)
      .doc(id_tienda)
      .update({
        pago_actual_id: admin.firestore.FieldValue.delete(),
      });

    // ── NOTIFICACIONES (solo si está habilitado) ──
    if (enviarNotificacion) {
      const tiendaDoc = await db
        .collection("Tiendas")
        .doc(localidad_tienda)
        .collection(localidad_tienda)
        .doc(id_tienda)
        .get();

      const propietarios = tiendaDoc.data()?.propietario_id || [];
      const mensajesRandom = [
        "🚀 Mira tus beneficios y sácales provecho.",
        "📈 Disfruta tu recarga y haz crecer tu negocio.",
        "💡 Aprovecha al máximo tus créditos disponibles.",
        "🔥 Es momento de impulsar tu tienda.",
        "✨ Dale más visibilidad a tu negocio ahora.",
        "🎯 Usa tus créditos estratégicamente y destaca.",
        "🛍️ Atrae más clientes con tus nuevas opciones.",
        "📊 Haz que tu tienda crezca con esta recarga.",
      ];

      for (const propietarioId of propietarios) {
        const tokenDoc = await db
          .collection("Trabajadores_Usuarios_Drivers")
          .doc("users")
          .collection("tokens")
          .doc(propietarioId)
          .get();

        const tokens = Object.values(tokenDoc.data()?.tokens || {});

        for (const token of tokens) {
          const mensajeExtra =
            mensajesRandom[Math.floor(Math.random() * mensajesRandom.length)];

          await enviarNotificacionFCM_tienda({
            token,
            title: "¡Recarga Exitosa! 🎉",
            body: `👋 Hola ${nombre_tienda} Tu recarga de ${monto_aumentado} creditos fue procesada correctamente. ${mensajeExtra}`,
            idTienda: id_tienda,
            tipo_notificacion: "pago",
            prioridad: "high",
          });
        }
      }
    } else {
      console.log("🔕 Notificación de recarga omitida (registro de deuda)");
    }

    console.log("🧹 campo pago_actual_id eliminado");
  } catch (error) {
    console.error("❌ ERROR EN PROCESO DE PAGO COMPLETO:");
    console.error(error);
    throw error;
  }
}

// ============================================================
// HELPERS - comprobante NubeFacT / Storage
// ============================================================

/**
 * Emite un comprobante electrónico (Boleta o Factura) vía NubeFacT.
 *
 * @param {Object} params - Datos del comprobante.
 * @param {number} params.tipoComprobante - 1 para FACTURA, 2 para BOLETA.
 * @param {string} params.documento - RUC (11 dígitos) para factura o DNI/S.N. para boleta.
 * @param {string} params.nombre - Razón Social o Nombre del cliente.
 * @param {string} params.direccion - Dirección (obligatorio para Factura, opcional para Boleta).
 * @param {number} params.monto - Monto total de la venta (incluido IGV).
 * @param {string} params.email - Correo del cliente para envío automático.
 * @param {string} params.chargeId - ID único de transacción para evitar duplicados.
 */
async function emitirComprobanteGeinz({
  tipoComprobante,
  documento,
  nombre,
  direccion,
  monto,
  email,
  chargeId,
  monedas,
}) {
  console.log("=== INICIANDO EMISIÓN NUBEFACT ===");
  console.log("Argumentos recibidos:", {
    tipoComprobante,
    documento,
    nombre,
    direccion,
    monto,
    email,
    chargeId,
    monedas,
  });

  try {
    const montoNum = Number(monto);
    const valorUnitario = montoNum / 1.18;
    const igvTotal = montoNum - valorUnitario;

    const esFactura = tipoComprobante === 1;

    // Mapeo dinámico del tipo de documento del cliente
    let tipoDocCliente = "-";
    if (esFactura) {
      tipoDocCliente = 6; // RUC
    } else if (documento && documento.length === 8) {
      tipoDocCliente = 1; // DNI
    }

    const payload = {
      operacion: "generar_comprobante",
      tipo_de_comprobante: tipoComprobante,
      serie: esFactura ? "FFF1" : "BBB1",
      numero: 0,
      sunat_transaction: 1,

      cliente_tipo_de_documento: tipoDocCliente,
      cliente_numero_de_documento: documento || "0",
      cliente_denominacion: nombre || "Consumidor Final",
      cliente_direccion: direccion || "",
      cliente_email: email || "",

      fecha_de_emision: new Date().toLocaleDateString("en-CA", {
        timeZone: "America/Lima",
      }), // Formato AAAA-MM-DD
      moneda: 1,
      porcentaje_de_igv: 18.0,

      total_gravada: valorUnitario.toFixed(2),
      total_igv: igvTotal.toFixed(2),
      total: montoNum.toFixed(2),

      items: [
        {
          unidad_de_medida: "ZZ",
          codigo: "MON001",
          descripcion: `Compra de ${monedas} créditos Geinz`,
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
      "Payload final a enviar a NubeFacT:",
      JSON.stringify(payload, null, 2),
    );

    const response = await axios.post(
      "https://api.nubefact.com/api/v1/02bb7d82-0b0c-4006-82a5-74b7437bea0b",
      payload,
      {
        headers: {
          Authorization: `Token token="8eee1a640fd7485cbc1da29427f59792b196deb29b954a6eb131bdb8562492fa"`,
          "Content-Type": "application/json",
        },
      },
    );

    console.log("✅ Respuesta exitosa de NubeFacT:", response.data);
    return response.data.enlace_del_pdf;
  } catch (error) {
    console.error("❌ ERROR EN NUBEFACT:");
    if (error.response) {
      console.error("Data del error:", error.response.data);
      console.error("Status:", error.response.status);
    } else {
      console.error("Mensaje de error:", error.message);
    }
    throw error;
  }
}

// Función helper para guardar PDF en Storage
async function guardarPDFEnStorage(pdfUrl, idTransaccion, idTienda) {
  try {
    const response = await axios.get(pdfUrl, { responseType: "arraybuffer" });
    const pdfBuffer = Buffer.from(response.data);

    const bucket = admin.storage().bucket();
    const filePath = `comprobantes/${idTienda}/${idTransaccion}.pdf`;
    const file = bucket.file(filePath);

    await file.save(pdfBuffer, {
      metadata: { contentType: "application/pdf" },
      public: true, // 👈 acceso público permanente
    });

    // URL pública directa de Storage (no expira)
    const urlPublica = `https://storage.googleapis.com/${bucket.name}/${filePath}`;

    console.log("✅ PDF subido a Storage:", urlPublica);
    return urlPublica;
  } catch (error) {
    console.error("❌ Error guardando PDF en Storage:", error);
    throw error;
  }
}

// Función helper para guardar URL en Firestore
async function guardarURLComprobanteFirestore({
  idTransaccion,
  idTienda,
  localidad,
  urlPDF,
}) {
  // En el historial financiero (merge para no pisar datos)
  await db
    .collection("Tiendas")
    .doc(localidad)
    .collection(localidad)
    .doc(idTienda)
    .collection("historial_financiero")
    .doc(idTransaccion)
    .set(
      {
        comprobante: {
          url_pdf: urlPDF,
          generado_en: admin.firestore.FieldValue.serverTimestamp(),
        },
      },
      { merge: true },
    );

  // También en pagos_tiendas para acceso rápido
  await db
    .collection("Tiendas")
    .doc(localidad)
    .collection("pagos_tiendas")
    .doc(idTransaccion)
    .set({ url_comprobante: urlPDF }, { merge: true });

  console.log("✅ URL guardada en Firestore");
}

// ============================================================
// HELPERS - WhatsApp
// ============================================================

async function enviarWhatsApp(
  numero,
  estadoPago,
  detallePago,
  montoSoles,
  creditos,
) {
  try {
    const telefono = `51${numero}`;

    const payload = {
      messaging_product: "whatsapp",
      to: telefono,
      type: "template",
      template: {
        name: "confirmacion_benjamin",
        language: {
          code: "en",
        },
        components: [
          {
            type: "header",
            parameters: [
              {
                type: "text",
                text: estadoPago,
              },
            ],
          },
          {
            type: "body",
            parameters: [
              {
                type: "text",
                text: detallePago,
              },
              {
                type: "text",
                text: montoSoles,
              },
              {
                type: "text",
                text: creditos,
              },
            ],
          },
        ],
      },
    };

    console.log("========== WHATSAPP TEMPLATE ==========");
    console.log("numero:", numero);
    console.log("telefono:", telefono);
    console.log("estadoPago:", estadoPago);
    console.log("detallePago:", detallePago);
    console.log("montoSoles:", montoSoles);
    console.log("creditos:", creditos);
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
    console.error(
      "❌ ERROR WHATSAPP:",
      JSON.stringify(error.response?.data, null, 2) || error.message,
    );

    return false;
  }
}

async function enviarWhatsAppRechazo(
  numero,
  estadoPago,
  nombreUsuario,
  motivoRechazo,
  creditos,
  montoSoles,
  idUsuario,
) {
  try {
    const telefono = `51${numero}`;

    const payload = {
      messaging_product: "whatsapp",
      to: telefono,
      type: "template",
      template: {
        name: "pago_rechazado",
        language: {
          code: "es",
        },
        components: [
          {
            type: "header",
            parameters: [
              {
                type: "text",
                text: estadoPago,
              },
            ],
          },
          {
            type: "body",
            parameters: [
              {
                type: "text",
                text: nombreUsuario,
              },
              {
                type: "text",
                text: motivoRechazo,
              },
              {
                type: "text",
                text: creditos,
              },
              {
                type: "text",
                text: montoSoles,
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
                text: idUsuario,
              },
            ],
          },
        ],
      },
    };

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

    console.log("✅ WhatsApp rechazo enviado:", res.data);
    return true;
  } catch (error) {
    console.error(
      "❌ ERROR WHATSAPP:",
      JSON.stringify(error.response?.data, null, 2) || error.message,
    );
    return false;
  }
}

async function enviarPlantillaWhatsApp({
  numero,
  nombreTienda,
  monedas,
  idTransaccion,
}) {
  try {
    const telefono = `51${numero}`;

    const res = await axios.post(
      `https://graph.facebook.com/v19.0/${PHONE_ID}/messages`,
      {
        messaging_product: "whatsapp",
        to: telefono,
        type: "template",
        template: {
          // 👇 El nombre exacto de tu plantilla en Meta
          name: "recarga",
          language: { code: "es" },
          components: [
            {
              // Header: {{1}} = emojis o texto del título
              type: "header",
              parameters: [{ type: "text", text: "🎉" }],
            },
            {
              // Body: {{1}} = nombre, {{2}} = monedas
              type: "body",
              parameters: [
                { type: "text", text: nombreTienda },
                { type: "text", text: `${monedas} créditos en Geinz` },
              ],
            },
            {
              // Botón URL dinámico "ver comprobante"
              type: "button",
              sub_type: "url",
              index: "0",
              parameters: [
                { type: "text", text: `data/comprobantes?id=${idTransaccion}` }, // 👈 solo el ID
              ],
            },
          ],
        },
      },
      {
        headers: {
          Authorization: `Bearer ${WHATSAPP_TOKEN}`,
          "Content-Type": "application/json",
        },
      },
    );

    console.log("✅ Plantilla WhatsApp enviada:", res.data);
    return true;
  } catch (error) {
    console.error(
      "❌ Error enviando plantilla:",
      error.response?.data || error.message,
    );
    return false;
  }
}
