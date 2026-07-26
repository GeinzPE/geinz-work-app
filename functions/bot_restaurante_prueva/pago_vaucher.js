const { onRequest } = require("firebase-functions/v2/https");
const admin = require("firebase-admin");
const { tokenGemini, armarTokens } = require("./token_utils.js");

if (!admin.apps.length) {
  admin.initializeApp();
}

const db = admin.firestore();

const GEMINIKEY = process.env.PRIVATEKEY_GEMINI;
const GEMINI_URL =
  "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent";
const { obtenerCarrito } = require("./carrito_store.js");
/* =========================================================================
   RAMA PAGOS_VOUCHER — el usuario ya quiere pagar su pedido.
   Flujo:
     1) Firestore  → lee el carrito actual del usuario.
     2) Código     → calcula el TOTAL sumando precio × cantidad.
     3) gemini-2.5-flash → redacta SOLO un texto corto invitando a pagar
                      seguro con Culqi.
     4) Código     → arma el bloque de precios + total y lo pega al final
                      del mensaje de Gemini.
     5) Código     → genera la URL de pago (Culqi) y arma el "botón" de
                      salida: { texto: "Pagar", url }.

   👇 TOKENS: este archivo ya loggeaba usageMetadata de Gemini, pero no lo
   devolvía. Ahora se estandariza con token_utils.js y se devuelve en
   "tokens" para que el dispensador/webhook lo pueda sumar con el resto.

   Devuelve: { mensaje, extra, boton: { texto, url } | null, tokens }
========================================================================= */

/** Lee el carrito actual del usuario. Mismo schema que pedido_carrito.js. */

/** Suma precio × cantidad de todo el carrito. SIEMPRE por código, nunca por IA. */
function calcularTotal(items) {
  return items.reduce((acc, i) => acc + Number(i.precio || 0) * (i.cantidad || 1), 0);
}

/** Arma el bloque de precios + total que se pega al final del mensaje de Gemini. */
function construirLineaPreciosYTotal(items, total) {
  const lineas = items.map(
    (i) => `• ${i.nombre}${i.cantidad > 1 ? ` x${i.cantidad}` : ""}: S/ ${Number(i.precio).toFixed(2)}`,
  );
  return `\n\n${lineas.join("\n")}\n\nTotal a pagar: S/ ${total.toFixed(2)}`;
}

/**
 * Genera la URL de pago para el pedido actual.
 *
 * 👇 PLACEHOLDER — acá va la integración real con Culqi.
 */
function generarUrlPago(numeroUsuario, total) {
  const base = process.env.CULQI_CHECKOUT_BASE_URL || "https://pago.tu-dominio.com/checkout";
  const url = `${base}?usuario=${encodeURIComponent(numeroUsuario)}&monto=${total.toFixed(2)}`;
  console.log("[pagos_voucher] URL de pago generada (placeholder Culqi):", url);
  return url;
}

const RESPONSE_SCHEMA = {
  type: "object",
  properties: {
    mensaje: { type: "string" },
    extra: { type: "string" },
  },
  required: ["mensaje", "extra"],
};

const SYSTEM_PROMPT = `Eres el asistente de un negocio por WhatsApp/Telegram. El usuario ya quiere pagar su pedido.
Te paso la lista de productos que lleva (nombre y precio de cada uno, solo como contexto de tono).

Reglas MUY importantes:
- NO listes los productos uno por uno, NO menciones precios individuales, NO calcules ni menciones el total. Todo eso se agrega después por fuera de tu respuesta.
- NO menciones ningún link, URL ni botón — eso también se agrega aparte.
- Tu única tarea es escribir un texto CORTO, cálido y con confianza, invitando al usuario a pagar de forma segura con Culqi (ej. mencionar que es un pago seguro y rápido).
- Si tienes el nombre del usuario, puedes usarlo con naturalidad.
- Sé breve, como un mensaje real de WhatsApp/Telegram.

Responde ÚNICAMENTE con un JSON:
- "mensaje": el texto corto para el usuario (sin precios, sin total, sin links).
- "extra": resumen de MÁXIMO 6 palabras de este turno (ej. "usuario listo para pagar, envié botón").`;

/**
 * Le pide a Gemini el texto corto de invitación a pagar.
 *
 * @returns {Promise<{mensaje:string, extra:string, tokens: {detalle: Array, total: number}}>}
 */
async function redactarMensajePago(items, nombreUsuario) {
  const itemsParaGemini = items.map((i) => ({ nombre: i.nombre, precio: i.precio }));

  const contexto = [
    nombreUsuario ? `nombre_usuario: ${nombreUsuario}` : null,
    `productos: ${JSON.stringify(itemsParaGemini)}`,
  ]
    .filter(Boolean)
    .join("\n");

  console.log("[pagos_voucher] Llamando a Gemini (gemini-2.5-flash) para redactar mensaje de pago...");
  const r = await fetch(`${GEMINI_URL}?key=${GEMINIKEY}`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({
      contents: [{ role: "user", parts: [{ text: contexto }] }],
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

  // 👇 TOKENS: se estandariza al formato común {detalle, total}.
  const tokens = armarTokens([tokenGemini(data?.usageMetadata, "gemini-2.5-flash")]);
  console.log(
    "[pagos_voucher] Tokens usados en Gemini (gemini-2.5-flash):",
    "prompt_tokens:", data?.usageMetadata?.promptTokenCount,
    "| respuesta_tokens:", data?.usageMetadata?.candidatesTokenCount,
    "| total_tokens:", data?.usageMetadata?.totalTokenCount,
  );

  const texto = data?.candidates?.[0]?.content?.parts?.[0]?.text;
  if (!texto) throw new Error("Gemini no devolvió texto en la respuesta.");

  const parsed = JSON.parse(texto);
  if (!parsed.mensaje || !parsed.extra) {
    throw new Error("Respuesta de Gemini incompleta.");
  }
  return { ...parsed, tokens };
}

/**
 * Flujo completo de la rama pagos_voucher.
 *
 * @param {Object} params
 * @param {string} params.mensaje
 * @param {string} params.numero_usuario
 * @param {string} [params.nombre_usuario]
 * @param {string} [params.extra_anterior]
 * @returns {Promise<{mensaje: string, extra: string, boton: {texto:string,url:string}|null, tokens: {detalle: Array, total: number}}>}
 */
async function responderPagos({ mensaje, numero_usuario, nombre_usuario, extra_anterior }) {
  console.log("[pagos_voucher] → responderPagos | mensaje:", mensaje, "| numero_usuario:", numero_usuario, "| extra_anterior:", extra_anterior);

  const items = await obtenerCarrito(numero_usuario);

  if (items.length === 0) {
    console.log("[pagos_voucher] Carrito vacío, no hay nada que pagar");
    return {
      mensaje: "Todavía no tienes nada en tu carrito para pagar 🙌 cuéntame qué quieres pedir y lo agregamos.",
      extra: "quiso pagar con carrito vacio",
      boton: null,
      tokens: armarTokens([]),
    };
  }

  const total = calcularTotal(items);
  const urlPago = generarUrlPago(numero_usuario, total);

  try {
    const { mensaje: textoIA, extra, tokens } = await redactarMensajePago(items, nombre_usuario);
    const mensajeFinal = textoIA + construirLineaPreciosYTotal(items, total);

    console.log("[pagos_voucher] ✅ Respuesta final | extra:", extra, "| total:", total.toFixed(2), "| total_tokens:", tokens.total);

    return {
      mensaje: mensajeFinal,
      extra,
      boton: { texto: "Pagar", url: urlPago },
      tokens,
    };
  } catch (err) {
    console.error("[pagos_voucher] ❌ Error generando mensaje de pago:", err.message);
    return {
      mensaje: `Tu total es de S/ ${total.toFixed(2)}. Puedes pagar de forma segura con Culqi tocando el botón de abajo.`,
      extra: "error generando mensaje pago",
      boton: { texto: "Pagar", url: urlPago },
      tokens: armarTokens([]),
    };
  }
}

/* =========================================================================
   ENDPOINT HTTP — solo para pruebas manuales, igual que las otras ramas.
========================================================================= */
exports.pagosVoucher = onRequest(async (req, res) => {
  console.log("[pagos_voucher] === Nueva petición HTTP a /pagosVoucher ===");
  try {
    const { numero_usuario, nombre_usuario, mensaje, extra_anterior } =
      req.body || {};

    if (!numero_usuario || !mensaje) {
      return res
        .status(400)
        .json({ error: "Faltan numero_usuario o mensaje." });
    }

    const respuesta = await responderPagos({
      mensaje,
      numero_usuario,
      nombre_usuario,
      extra_anterior,
    });

    return res.status(200).json(respuesta);
  } catch (err) {
    console.error("[pagos_voucher] ❌ Error en el endpoint:", err.message, "| stack:", err.stack);
    return res
      .status(500)
      .json({ error: "Error interno de la rama pagos_voucher." });
  }
});

module.exports.responderPagos = responderPagos;