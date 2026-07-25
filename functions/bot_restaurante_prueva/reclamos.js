const { onRequest } = require("firebase-functions/v2/https");

// const { guardarMensajeHistorial } = require("../historial_whatsapp.js");
// TODO: activar el guardado en historial cuando esté listo (ver bloque comentado más abajo)

const GEMINIKEY = process.env.PRIVATEKEY_GEMINI;
const GEMINI_URL =
  "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent";

/* =========================================================================
   RAMA RECLAMOS — quejas, reclamos, o pedido de hablar con un asesor real
   Modelo: gemini-2.5-flash (REST, sin SDK). No usa Algolia ni GPT.

   Objetivo único: tranquilizar al usuario, con tono calmado y empático,
   validando lo que siente, y avisarle que un asesor humano va a tomar su
   caso (entre 5 y 10 minutos).

   Siempre devuelve un JSON: { mensaje, extra, humano }
     - mensaje: la respuesta calmante para el usuario.
     - extra:   resumen de máximo 6 palabras del turno.
     - humano:  booleano (true/false), SIEMPRE presente. true cuando el
                usuario necesita que un asesor humano tome el caso ahora
                mismo; false cuando todavía es una queja inicial que solo
                necesita ser escuchada antes de escalar.
========================================================================= */

const RESPONSE_SCHEMA = {
  type: "object",
  properties: {
    mensaje: { type: "string" },
    extra: { type: "string" },
    humano: { type: "boolean" },
  },
  required: ["mensaje", "extra", "humano"],
};

const SYSTEM_PROMPT = `Eres el asistente de un negocio por WhatsApp encargado de atender reclamos y quejas.

Tu prioridad SIEMPRE es tranquilizar al usuario: tono calmado, empático y pacífico. Dale la razón en la medida de lo posible y valida cómo se siente, sin ponerte a discutir ni a justificar al negocio.

No intentas resolver tú el reclamo. Tu trabajo es contener al usuario y avisarle que vas a pasar su caso con un asesor humano, que puede tardar entre 5 y 10 minutos en contestarle. No prometas nada que no sea eso.

Además del mensaje, debes decidir "humano":
- true  → el usuario ya quiere o necesita que un asesor humano tome el caso ahora.
- false → todavía es una queja inicial que primero necesita ser escuchada/calmada.
"humano" es SIEMPRE un booleano (true o false), nunca texto ni null.

Si tienes el nombre del usuario, puedes usarlo con naturalidad, sin abusar.
Sé breve, como un mensaje real de WhatsApp.

Responde ÚNICAMENTE con un JSON: { "mensaje": "...", "extra": "...", "humano": true|false }`;

/**
 * Genera la respuesta de la rama reclamos usando Gemini 2.5 Flash.
 *
 * @param {Object} params
 * @param {string} params.mensaje - Mensaje actual del usuario.
 * @param {string} [params.nombre_usuario]
 * @param {string} [params.extra_anterior]
 * @returns {Promise<{mensaje: string, extra: string, humano: boolean}>}
 */
async function responderReclamos({ mensaje, nombre_usuario, extra_anterior }) {
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

    const r = await fetch(`${GEMINI_URL}?key=${GEMINIKEY}`, {
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

    if (!r.ok) {
      const body = await r.text();
      throw new Error(`Gemini respondió ${r.status}: ${body}`);
    }

    const data = await r.json();
    const texto = data?.candidates?.[0]?.content?.parts?.[0]?.text;
    if (!texto) throw new Error("Gemini no devolvió texto en la respuesta.");

    const parsed = JSON.parse(texto);

    if (!parsed.mensaje || !parsed.extra || typeof parsed.humano !== "boolean") {
      throw new Error("Respuesta de Gemini incompleta o mal tipada.");
    }

    return { mensaje: parsed.mensaje, extra: parsed.extra, humano: parsed.humano };
  } catch (err) {
    console.error("[reclamos] Error generando respuesta:", err);
    // Ante cualquier falla, priorizamos no dejar a un usuario molesto sin
    // respuesta: mensaje calmante genérico + escalar a humano por seguridad.
    return {
      mensaje:
        "Entiendo tu molestia y lamento el inconveniente. Ya voy a pasar tu caso con un asesor humano, que te va a contactar en unos 5 a 10 minutos.",
      extra: "reclamo, escalado por error técnico",
      humano: true,
    };
  }
}

/* =========================================================================
   ENDPOINT HTTP — solo para pruebas manuales, igual que las otras ramas.
========================================================================= */
exports.reclamos = onRequest(async (req, res) => {
  try {
    const { numero_usuario, nombre_usuario, mensaje, extra_anterior } =
      req.body || {};

    if (!numero_usuario || !mensaje) {
      return res
        .status(400)
        .json({ error: "Faltan numero_usuario o mensaje." });
    }

    const respuesta = await responderReclamos({
      mensaje,
      nombre_usuario,
      extra_anterior,
    });

    // Guarda la respuesta del bot en el historial (comentado por ahora,
    // igual que en la rama general)
    // guardarMensajeHistorial({
    //   numero_usuario,
    //   nombre_usuario,
    //   contenido: respuesta.mensaje,
    //   extra: respuesta.extra,
    //   remitente: "bot",
    //   tipo: "texto",
    // }).catch((err) => console.error("[reclamos] Error guardando historial:", err));

    return res.status(200).json(respuesta);
  } catch (err) {
    console.error("[reclamos] Error en el endpoint:", err);
    return res.status(500).json({ error: "Error interno de la rama reclamos." });
  }
});

module.exports.responderReclamos = responderReclamos;