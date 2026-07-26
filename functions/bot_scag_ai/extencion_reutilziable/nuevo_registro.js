"use strict";

const { onRequest } = require("firebase-functions/v2/https");
const { axios } = require("./_shared");

// ⚠️ NOTA: en el archivo original, PHONE_ID y WHATSAPP_TOKEN se usan pero
// nunca se declaran/importan en ese mismo archivo (probablemente vienen de
// variables globales de otro módulo). Se dejan tal cual para no alterar el
// comportamiento; considera moverlos a variables de entorno, por ejemplo:
const PHONE_ID = process.env.PHONE_ID;
const WHATSAPP_TOKEN = process.env.WHATSAPP_TOKEN;

const nuevo_registro = onRequest(
  {
    region: "us-central1",
    timeoutSeconds: 120,
    memory: "256MiB",
    cors: true,
  },
  async (req, res) => {
    console.log("📲 [enviarWhatsApp_registro] Body recibido:", req.body);

    try {
      const { telefono, nombre_user } = req.body;

      if (!telefono) {
        console.error(
          "⚠️ [enviarWhatsApp_registro] telefono vacío o undefined, usando default",
        );
      }
      if (!nombre_user) {
        console.error(
          "⚠️ [enviarWhatsApp_registro] nombre_user vacío o undefined",
        );
      }

      const telefonoFinal = `51${telefono || "937659216"}`;

      const payload = {
        messaging_product: "whatsapp",
        to: telefonoFinal,
        type: "template",
        template: {
          name: "registro",
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
          ],
        },
      };

      console.log("========== WHATSAPP TEMPLATE ==========");
      console.log("PHONE_ID:", PHONE_ID);
      console.log("telefono:", telefonoFinal);
      console.log("nombre_user:", nombre_user);
      console.log("payload:");
      console.log(JSON.stringify(payload, null, 2));
      console.log("======================================");

      const respuesta = await axios.post(
        `https://graph.facebook.com/v19.0/${PHONE_ID}/messages`,
        payload,
        {
          headers: {
            Authorization: `Bearer ${WHATSAPP_TOKEN}`,
            "Content-Type": "application/json",
          },
        },
      );

      console.log(
        "✅ WhatsApp enviado correctamente. Status:",
        respuesta.status,
      );
      console.log(
        "✅ Respuesta completa:",
        JSON.stringify(respuesta.data, null, 2),
      );

      return res.status(200).json({
        success: true,
        message: "WhatsApp enviado correctamente",
        data: respuesta.data,
      });
    } catch (error) {
      console.error("❌ ERROR WHATSAPP - status:", error.response?.status);
      console.error(
        "❌ ERROR WHATSAPP - data:",
        JSON.stringify(error.response?.data, null, 2) || error.message,
      );
      console.error("❌ ERROR WHATSAPP - message:", error.message);

      return res.status(500).json({
        success: false,
        message: "Error al enviar WhatsApp",
        error: error.response?.data || error.message,
      });
    }
  },
);

module.exports = { nuevo_registro };
