const { onCall, HttpsError } = require("firebase-functions/v2/https");
const { onDocumentCreated } = require("firebase-functions/v2/firestore");
const logger = require("firebase-functions/logger");
const admin = require("firebase-admin");
const { randomUUID } = require("crypto");

if (!admin.apps.length) {
  admin.initializeApp();
}

const db = admin.firestore();
const bucket = admin.storage().bucket();

// ============================================================
// CONFIG
// ============================================================
const WHATSAPP_TOKEN = process.env.ID_API_WHATSAPP;
const WHATSAPP_PHONE_NUMBER_ID = process.env.ID_NUMBER_WHATSAPP;
const WHATSAPP_API_VERSION = "v20.0";

// Mismo patrón que NUMERO_AVISO_INTERNO en dispensador_competo_geinz_pruevas.js
// 👉 Cámbialo aquí (o define NUMERO_AVISO_REGISTRO en tus env vars) si quieres
//    que el aviso llegue a otro número.
const NUMERO_AVISO_REGISTRO = process.env.NUMERO_AVISO_REGISTRO || "51937659216";

const COLECCION_NEGOCIOS_POR_VISITAR = "negocios_por_visitar";

// ============================================================
// HELPERS
// ============================================================

// Sube el logo (dataURL base64) a Storage y devuelve su URL pública.
// Si algo falla, devuelve null (el registro se guarda igual, sin logo).
async function subirLogoAStorage(dataUrl, docId) {
  const match = /^data:(image\/[\w+.-]+);base64,(.+)$/.exec(dataUrl || "");
  if (!match) throw new Error("Formato de imagen no válido (se esperaba un data URL base64).");

  const mimeType = match[1];
  const base64Data = match[2];
  const buffer = Buffer.from(base64Data, "base64");

  const extension = mimeType.split("/")[1]?.replace("+xml", "") || "jpg";
  const filePath = `${COLECCION_NEGOCIOS_POR_VISITAR}/${docId}/logo.${extension}`;
  const file = bucket.file(filePath);
  const downloadToken = randomUUID();

  await file.save(buffer, {
    contentType: mimeType,
    metadata: {
      contentType: mimeType,
      metadata: { firebaseStorageDownloadTokens: downloadToken },
    },
  });

  const encodedPath = encodeURIComponent(filePath);
  return `https://firebasestorage.googleapis.com/v0/b/${bucket.name}/o/${encodedPath}?alt=media&token=${downloadToken}`;
}

// Envía un mensaje de texto simple por WhatsApp al número interno.
async function enviarMensajeWhatsappInterno(textBody) {
  const url = `https://graph.facebook.com/${WHATSAPP_API_VERSION}/${WHATSAPP_PHONE_NUMBER_ID}/messages`;

  const resp = await fetch(url, {
    method: "POST",
    headers: {
      Authorization: `Bearer ${WHATSAPP_TOKEN}`,
      "Content-Type": "application/json",
    },
    body: JSON.stringify({
      messaging_product: "whatsapp",
      to: NUMERO_AVISO_REGISTRO,
      type: "text",
      text: { body: textBody },
    }),
  });

  if (!resp.ok) {
    throw new Error(
      `Error enviando WhatsApp interno: ${resp.status} ${await resp.text()}`,
    );
  }

  return resp.json();
}

// Convierte "YYYY-MM-DD" + "HH:mm" en un texto legible tipo
// "lunes 27 de julio a las 08:00 am"
function formatearFechaHoraVisita(fechaISO, hora) {
  try {
    const [horaNum, minNum] = (hora || "00:00").split(":").map(Number);
    const fecha = new Date(`${fechaISO}T00:00:00-05:00`);
    fecha.setHours(horaNum, minNum, 0, 0);

    const fechaTexto = fecha.toLocaleDateString("es-PE", {
      weekday: "long",
      day: "2-digit",
      month: "long",
      timeZone: "America/Lima",
    });
    const horaTexto = fecha.toLocaleTimeString("es-PE", {
      hour: "2-digit",
      minute: "2-digit",
      timeZone: "America/Lima",
    });

    return `${fechaTexto} a las ${horaTexto}`;
  } catch (e) {
    return `${fechaISO} ${hora}`;
  }
}

function construirMensajeNuevoRegistro(data, registroId) {
  const fechaHoraBonita = formatearFechaHoraVisita(
    data.fecha_verificacion,
    data.hora_verificacion,
  );

  return (
    `🆕 *Nuevo negocio registrado en GEINZ*\n\n` +
    `🏬 Negocio: ${data.nombre_negocio}\n` +
    `📍 Ubicación: ${data.ubicacion}\n` +
    `📞 Teléfono: ${data.telefono}\n` +
    `🗓️ Verificación agendada: ${fechaHoraBonita}\n` +
    `🖼️ Logo: ${data.logo_url ? data.logo_url : "No adjuntó imagen"}\n` +
    `🌐 Origen: ${data.origen || "landing_geinztech"}\n\n` +
    `Estado: pendiente de verificación presencial (máx. 10 min).\n` +
    `🆔 ${registroId}`
  );
}

// ============================================================
// CLOUD FUNCTION 1 — Guardar el registro (llamada desde el formulario)
// ============================================================
exports.registrarNegocioGeinz = onCall(
  { region: "us-central1" },
  async (request) => {
    const data = request.data || {};

    const nombre_negocio = (data.nombre_negocio || "").trim();
    const ubicacion = (data.ubicacion || "").trim();
    const telefono = (data.telefono || "").trim();
    const fecha_verificacion = data.fecha_verificacion || "";
    const hora_verificacion = data.hora_verificacion || "";
    const terminos_aceptados = data.terminos_aceptados === true;
    const lat = typeof data.lat === "number" ? data.lat : null;
    const lng = typeof data.lng === "number" ? data.lng : null;
    const origen = data.origen || "landing_geinztech";
    const logo_base64 =
      typeof data.logo_base64 === "string" ? data.logo_base64 : null;

    // ---- Validaciones ----
    if (!nombre_negocio) {
      throw new HttpsError("invalid-argument", "Falta el nombre del negocio.");
    }
    if (!ubicacion) {
      throw new HttpsError("invalid-argument", "Falta la ubicación del negocio.");
    }
    if (!telefono) {
      throw new HttpsError("invalid-argument", "Falta el teléfono del negocio.");
    }
    if (!fecha_verificacion || !hora_verificacion) {
      throw new HttpsError(
        "invalid-argument",
        "Falta la fecha o la hora de verificación.",
      );
    }
    if (!terminos_aceptados) {
      throw new HttpsError(
        "failed-precondition",
        "Debes aceptar los Términos y Condiciones para registrarte.",
      );
    }

    const docRef = db.collection(COLECCION_NEGOCIOS_POR_VISITAR).doc();

    // ---- Logo (si vino) ----
    let logo_url = null;
    if (logo_base64) {
      try {
        logo_url = await subirLogoAStorage(logo_base64, docRef.id);
      } catch (e) {
        logger.error(
          "❌ [registrarNegocioGeinz] Falló la subida del logo, se guarda sin logo:",
          e.message,
        );
      }
    }

    const registro = {
      nombre_negocio,
      ubicacion,
      lat,
      lng,
      telefono,
      logo_url,
      fecha_verificacion, // "YYYY-MM-DD"
      hora_verificacion, // "HH:mm"
      estado: "pendiente_verificacion",
      terminos_aceptados: true,
      origen,
      createdAt: admin.firestore.FieldValue.serverTimestamp(),
    };

    await docRef.set(registro);

    logger.log(
      "✅ [registrarNegocioGeinz] Nuevo registro guardado:",
      docRef.id,
      "|",
      nombre_negocio,
    );

    return { ok: true, id: docRef.id };
  },
);

// ============================================================
// CLOUD FUNCTION 2 — Avisar por WhatsApp cuando se crea un registro
// ============================================================
exports.notificarNuevoRegistroNegocio = onDocumentCreated(
  {
    document: `${COLECCION_NEGOCIOS_POR_VISITAR}/{registroId}`,
    region: "us-central1",
  },
  async (event) => {
    const snap = event.data;
    if (!snap) {
      logger.warn("⚠️ [notificarNuevoRegistroNegocio] Evento sin datos, ignorado.");
      return;
    }

    const data = snap.data();
    const registroId = event.params.registroId;

    try {
      const mensaje = construirMensajeNuevoRegistro(data, registroId);
      await enviarMensajeWhatsappInterno(mensaje);
      logger.log(
        "📲 [notificarNuevoRegistroNegocio] Aviso enviado por WhatsApp | registroId:",
        registroId,
      );
    } catch (e) {
      logger.error(
        "❌ [notificarNuevoRegistroNegocio] Falló el envío del aviso por WhatsApp:",
        e.message,
      );
    }
  },
);