require("dotenv").config();
const { onRequest, onCall } = require("firebase-functions/v2/https");
const {
  onDocumentCreated,
  onDocumentWritten,
} = require("firebase-functions/v2/firestore");

const logger = require("firebase-functions/logger");
const paths = require("./rutas_geinz_firebase/rutas");
const admin = require("firebase-admin");
const algoliasearch = require("algoliasearch");
const { onSchedule } = require("firebase-functions/v2/scheduler");

const speech = require("@google-cloud/speech");

const client_specth = new speech.SpeechClient();

const textToSpeech = require("@google-cloud/text-to-speech");
const ttsClient = new textToSpeech.TextToSpeechClient();
const geofire = require("geofire-common");

admin.initializeApp();

const axios = require("axios");

const CULQI_KEY = process.env.CULQI_KEY; // 🔹 v2: se usa env variable
const PHONE_ID = process.env.ID_NUMBER_WHATSAPP;
const WHATSAPP_TOKEN = process.env.ID_API_WHATSAPP;
const PDFDocument = require("pdfkit");
const fs = require("fs");
const path = require("path");

const db = admin.firestore();

async function generarPDF({ userId, monedas, chargeId }) {
  const doc = new PDFDocument();

  const fileName = `boleta-${Date.now()}.pdf`;
  const filePath = path.join("/tmp", fileName);

  doc.pipe(fs.createWriteStream(filePath));

  doc.fontSize(20).text("GEINZ - BOLETA ELECTRÓNICA", { align: "center" });
  doc.moveDown();

  doc.fontSize(12).text(`Usuario: ${userId}`);
  doc.text(`Monedas: ${monedas}`);
  doc.text(`ID Pago: ${chargeId}`);
  doc.text(`Estado: APROBADO`);

  doc.moveDown();
  doc.text("Gracias por su compra 🙌");

  doc.end();

  return filePath;
}

async function subirPDF(filePath, fileName) {
  const bucket = admin.storage().bucket();
  const file = bucket.file(`boletas/${fileName}`);

  await bucket.upload(filePath, {
    destination: `boletas/${fileName}`,
    metadata: { contentType: "application/pdf" },
  });

  const file = bucket.file(`boletas/${fileName}`);

  const [url] = await file.getSignedUrl({
    action: "read",
    expires: Date.now() + 24 * 60 * 60 * 1000, // 24h
  });

  return url;
}

async function enviarPDFWhatsApp(numero, pdfUrl) {
  await axios.post(
    `https://graph.facebook.com/v19.0/${PHONE_ID}/messages`,
    {
      messaging_product: "whatsapp",
      to: `51${numero}`,
      type: "document",
      document: {
        link: pdfUrl,
        filename: "boleta_geinz.pdf",
      },
    },
    {
      headers: {
        Authorization: `Bearer ${WHATSAPP_TOKEN}`,
        "Content-Type": "application/json",
      },
    },
  );
}

async function enviarWhatsApp(numero, mensaje) {
  const telefono = `51${numero}`;

  await axios.post(
    `https://graph.facebook.com/v19.0/${PHONE_ID}/messages`,
    {
      messaging_product: "whatsapp",
      to: `51${numero}`,
      type: "text",
      text: { body: mensaje },
    },
    {
      headers: {
        Authorization: `Bearer ${WHATSAPP_TOKEN}`,
        "Content-Type": "application/json",
      },
    },
  );
}

async function sumarSaldo(userId, monedas) {
  console.log("🟡 [sumarSaldo] INICIO");
  console.log("userId:", userId);
  console.log("monedas:", monedas);

  const ref = paths.tiendaDoc("barranca", "tiendas", userId);
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

exports.confirmarPago = onCall(async (req) => {
  const { token, monto, email, userId, monedas } = req.data;

  try {
    const response = await axios.post(
      "https://api.culqi.com/v2/charges",
      {
        amount: Math.round(monto * 100),
        currency_code: "PEN",
        email: email || "test@test.com",
        source_id: token,
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

    // 🚨 VALIDACIÓN REAL DE PAGO
    if (charge.outcome?.code !== "AUT0000") {
      throw new Error("Pago no aprobado");
    }

    const numero = await sumarSaldo(userId, monedas);

    if (typeof numero === "string" && numero.length >= 9) {
      const filePath = await generarPDF({
        userId,
        monedas,
        chargeId: charge.id,
      });

      const pdfUrl = await subirPDF(filePath, `boleta-${charge.id}.pdf`);

      await enviarPDFWhatsApp(numero, pdfUrl);
    }
    return {
      ok: true,
      chargeId: charge.id,
    };
  } catch (error) {
    console.error("ERROR CHARGE:", error.response?.data || error.message);

    throw new Error(JSON.stringify(error.response?.data || error.message));
  }
});
module.exports = { confirmarPago };
