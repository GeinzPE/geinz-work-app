const { onRequest } = require("firebase-functions/v2/https");
const { tokenGemini, armarTokens } = require("./token_utils.js");

// const { guardarMensajeHistorial } = require("../historial_whatsapp.js");
// TODO: activar el guardado en historial cuando esté listo (ver bloque comentado más abajo)
const WHATSAPP_TOKEN = process.env.ID_API_WHATSAPP; // Bearer token (empieza con EAA...)
const WHATSAPP_PHONE_NUMBER_ID = process.env.ID_NUMBER_WHATSAPP; // el ID numérico
const WHATSAPP_API_VERSION = "v20.0";
const NUMERO_ALERTA_RECLAMOS =
  process.env.NUMERO_ALERTA_RECLAMOS || "51937659216";
const GEMINIKEY = process.env.PRIVATEKEY_GEMINI;
const GEMINI_URL =
  "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent";

/* =========================================================================
   RAMA RECLAMOS — quejas, reclamos, o pedido de hablar con un asesor real
   Modelo: gemini-2.5-flash (REST, sin SDK). No usa Algolia ni GPT.

   Objetivo único: tranquilizar al usuario, con tono calmado y empático,
   validando lo que siente, y avisarle que un asesor humano va a tomar su
   caso (entre 5 y 10 minutos).

   👇 TOKENS: antes este archivo no leía `data.usageMetadata` en absoluto.
   Ahora se lee, se loggea y se devuelve en "tokens" con el mismo formato
   {detalle, total} que usan las demás ramas.

   Siempre devuelve un JSON: { mensaje, extra, humano, tokens }
     - mensaje: la respuesta calmante para el usuario.
     - extra:   resumen de máximo 6 palabras del turno.
     - humano:  booleano (true/false), SIEMPRE presente.
     - tokens:  lo gastado en Gemini en esta llamada.
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
 * @returns {Promise<{mensaje: string, extra: string, humano: boolean, tokens: {detalle: Array, total: number}}>}
 */
async function responderReclamos({
  mensaje,
  nombre_usuario,
  extra_anterior,
  numero_usuario,
}) {
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

    // 👇 TOKENS: antes no se leía en absoluto en este archivo.
    const usageMeta = data?.usageMetadata;
    const tokens = armarTokens([tokenGemini(usageMeta, "gemini-2.5-flash")]);
    console.log(
      "[reclamos] Tokens usados en Gemini (gemini-2.5-flash):",
      "prompt_tokens:",
      usageMeta?.promptTokenCount ?? null,
      "| respuesta_tokens:",
      usageMeta?.candidatesTokenCount ?? null,
      "| total_tokens:",
      usageMeta?.totalTokenCount ?? null,
    );

    const texto = data?.candidates?.[0]?.content?.parts?.[0]?.text;
    if (!texto) throw new Error("Gemini no devolvió texto en la respuesta.");

    const parsed = JSON.parse(texto);

    if (
      !parsed.mensaje ||
      !parsed.extra ||
      typeof parsed.humano !== "boolean"
    ) {
      throw new Error("Respuesta de Gemini incompleta o mal tipada.");
    }

    // 👇 NUEVO — si el reclamo ya necesita un asesor humano, se avisa por
    // WhatsApp al número de atención. No se espera (await) para no
    // demorar la respuesta al usuario; la función tiene su propio
    // try/catch interno así que un fallo ahí no rompe nada más.
    if (parsed.humano === true) {
      enviarAlertaWhatsAppReclamo({
        numero_usuario,
        nombre_usuario,
        mensajeUsuario: mensaje,
      });
    }

    return {
      mensaje: parsed.mensaje,
      extra: parsed.extra,
      humano: parsed.humano,
      tokens,
    };
  } catch (err) {
    console.error("[reclamos] Error generando respuesta:", err);
    // Ante cualquier falla, priorizamos no dejar a un usuario molesto sin
    // respuesta: mensaje calmante genérico + escalar a humano por seguridad.

    // 👇 NUEVO — aquí también se escala a humano, así que también se avisa.
    enviarAlertaWhatsAppReclamo({
      numero_usuario,
      nombre_usuario,
      mensajeUsuario: mensaje,
    });

    return {
      mensaje:
        "Entiendo tu molestia y lamento el inconveniente. Ya voy a pasar tu caso con un asesor humano, que te va a contactar en unos 5 a 10 minutos.",
      extra: "reclamo, escalado por error técnico",
      humano: true,
      tokens: armarTokens([]),
    };
  }
}

/**
 * 👇 NUEVO — Manda un WhatsApp simple (texto plano, sin plantilla) al
 * número de alerta interno, avisando que un usuario de Telegram necesita
 * atención humana YA. Se dispara solo cuando Gemini devolvió humano:true.
 *
 * No lanza excepción hacia arriba si falla — un fallo en la alerta interna
 * NUNCA debe romper la respuesta que se le da al usuario que reclama.
 *
 * @param {Object} params
 * @param {string} [params.numero_usuario] - ej. "tg_8786837495"
 * @param {string} [params.nombre_usuario]
 * @param {string} params.mensajeUsuario - lo que escribió el usuario
 */
async function enviarAlertaWhatsAppReclamo({
  numero_usuario,
  nombre_usuario,
  mensajeUsuario,
}) {
  console.log(
    "[reclamos] → enviarAlertaWhatsAppReclamo | numero_usuario:",
    numero_usuario,
    "| nombre_usuario:",
    nombre_usuario,
  );

  const textoAlerta =
    `⚠️ Atención: este usuario requiere atención ahora.\n\n` +
    `🆔 ID Telegram: ${numero_usuario || "desconocido"}\n` +
    `👤 Nombre: ${nombre_usuario || "Usuario"}\n\n` +
    `💬 Mensaje:\n${mensajeUsuario}`;

  try {
    const resp = await fetch(
      `https://graph.facebook.com/${WHATSAPP_API_VERSION}/${WHATSAPP_PHONE_NUMBER_ID}/messages`,
      {
        method: "POST",
        headers: {
          Authorization: `Bearer ${WHATSAPP_TOKEN}`,
          "Content-Type": "application/json",
        },
        body: JSON.stringify({
          messaging_product: "whatsapp",
          to: NUMERO_ALERTA_RECLAMOS,
          type: "text",
          text: { body: textoAlerta },
        }),
      },
    );

    const json = await resp.json();
    if (!resp.ok) {
      console.error(
        "[reclamos] ❌ Error mandando alerta WhatsApp:",
        JSON.stringify(json),
      );
    } else {
      console.log(
        "[reclamos] ✅ Alerta WhatsApp enviada OK | message_id:",
        json.messages?.[0]?.id,
      );
    }
  } catch (err) {
    console.error(
      "[reclamos] ❌ Excepción mandando alerta WhatsApp:",
      err.message,
    );
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

    return res.status(200).json(respuesta);
  } catch (err) {
    console.error("[reclamos] Error en el endpoint:", err);
    return res
      .status(500)
      .json({ error: "Error interno de la rama reclamos." });
  }
});

module.exports.responderReclamos = responderReclamos;
