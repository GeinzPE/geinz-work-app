const { onRequest } = require("firebase-functions/v2/https");

// const { guardarMensajeHistorial } = require("../historial_whatsapp.js");
// TODO: activar el guardado en historial cuando esté listo (ver bloque comentado más abajo)

const GEMINIKEY = process.env.PRIVATEKEY_GEMINI;
const GEMINI_URL =
  "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent";

/* =========================================================================
   RAMA GENERAL — preguntas sin contexto específico / info del negocio
   Modelo: gemini-2.5-flash (vía REST, sin SDK)
   Siempre devuelve un JSON: { mensaje, extra }
     - mensaje: la respuesta que se le manda al usuario por WhatsApp.
     - extra:   resumen de 5 palabras del tema tratado en este turno, para
                que la IA tenga contexto de la conversación anterior en el
                siguiente mensaje (se guarda junto al mensaje, igual que el
                campo "extra" del historial).
========================================================================= */

const RESPONSE_SCHEMA = {
  type: "object",
  properties: {
    mensaje: { type: "string" },
    extra: { type: "string" },
  },
  required: ["mensaje", "extra"],
};

const SYSTEM_PROMPT = `Eres el asistente conversacional de un negocio por WhatsApp.
Respondes preguntas generales o sin contexto claro: quiénes son, horarios, ubicación, promociones, dudas sueltas, saludos, etc.

Reglas:
- Sé breve, cálido y directo, como un mensaje real de WhatsApp (no un correo formal).
- Si tienes el nombre del usuario, úsalo con naturalidad, sin abusar.
- Si recibes contexto de la conversación anterior (extra_anterior), tenlo en cuenta para no repetirte ni perder el hilo.
- Si no tienes información suficiente para responder algo puntual del negocio, sé honesto y ofrece ayuda alternativa (no inventes datos como precios, horarios o direcciones que no te dieron).

Debes responder ÚNICAMENTE con un JSON con dos campos:
- "mensaje": tu respuesta para el usuario.
- "extra": un resumen de EXACTAMENTE 5 palabras sobre el tema de este turno (para dar contexto al siguiente mensaje). Ejemplo: "usuario pregunta horario de atención".`;

/**
 * Genera la respuesta de la rama general usando Gemini 2.5 Flash.
 *
 * @param {Object} params
 * @param {string} params.mensaje - Mensaje actual del usuario.
 * @param {string} [params.nombre_usuario] - Nombre del usuario, si se conoce.
 * @param {string} [params.extra_anterior] - Contexto (5 palabras) del turno anterior.
 * @returns {Promise<{mensaje: string, extra: string}>}
 */
async function responderGeneral({ mensaje, nombre_usuario, extra_anterior }) {
  try {
    const contexto = [
      nombre_usuario ? `nombre_usuario: ${nombre_usuario}` : null,
      extra_anterior ? `extra_anterior: ${extra_anterior}` : null,
    ]
      .filter(Boolean)
      .join("\n");

    const promptUsuario = contexto
      ? `Contexto:\n${contexto}\n\nMensaje del usuario:\n${mensaje}`
      : `Mensaje del usuario:\n${mensaje}`;

    const respuestaFetch = await fetch(`${GEMINI_URL}?key=${GEMINIKEY}`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({
        contents: [{ role: "user", parts: [{ text: promptUsuario }] }],
        systemInstruction: { role: "system", parts: [{ text: SYSTEM_PROMPT }] },
        generationConfig: {
          responseMimeType: "application/json",
          responseSchema: RESPONSE_SCHEMA,
        },
      }),
    });

    if (!respuestaFetch.ok) {
      const errorBody = await respuestaFetch.text();
      throw new Error(
        `Gemini respondió ${respuestaFetch.status}: ${errorBody}`,
      );
    }

    const data = await respuestaFetch.json();
    const texto = data?.candidates?.[0]?.content?.parts?.[0]?.text;

    if (!texto) {
      throw new Error("Gemini no devolvió texto en la respuesta.");
    }

    const parsed = JSON.parse(texto);

    if (!parsed.mensaje || !parsed.extra) {
      throw new Error("Respuesta de Gemini incompleta.");
    }

    return { mensaje: parsed.mensaje, extra: parsed.extra };
  } catch (err) {
    console.error("[general] Error generando respuesta:", err);
    return {
      mensaje:
        "Perdona, tuve un problema para responderte. ¿Puedes repetir tu pregunta?",
      extra: "error al responder general",
    };
  }
}

/* =========================================================================
   ENDPOINT HTTP — recibe el mensaje y devuelve { mensaje, extra }
========================================================================= */
exports.general = onRequest(async (req, res) => {
  try {
    const { numero_usuario, nombre_usuario, mensaje, extra_anterior } =
      req.body || {};

    if (!numero_usuario || !mensaje) {
      return res
        .status(400)
        .json({ error: "Faltan numero_usuario o mensaje." });
    }

    const respuesta = await responderGeneral({
      mensaje,
      nombre_usuario,
      extra_anterior,
    });

    // Guarda la respuesta del bot en el historial (comentado por ahora)
    // guardarMensajeHistorial({
    //   numero_usuario,
    //   nombre_usuario,
    //   contenido: respuesta.mensaje,
    //   extra: respuesta.extra,
    //   remitente: "bot",
    //   tipo: "texto",
    // }).catch((err) => console.error("[general] Error guardando historial:", err));

    return res.status(200).json(respuesta);
  } catch (err) {
    console.error("[general] Error en el endpoint:", err);
    return res.status(500).json({ error: "Error interno de la rama general." });
  }
});

module.exports.responderGeneral = responderGeneral;