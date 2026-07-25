const { onRequest } = require("firebase-functions/v2/https");
const admin = require("firebase-admin");
const algoliasearch = require("algoliasearch");
const OpenAI = require("openai");

if (!admin.apps.length) {
  admin.initializeApp();
}

const db = admin.firestore();

const openai = new OpenAI({
  apiKey: process.env.API_KEYO_OPEN_IA,
});

const APP_ID = process.env.ALGOLIA_APP_ID || "";
const API_KEY = process.env.ALGOLIA_API_KEY || "";
const client = algoliasearch(APP_ID, API_KEY);
const index = client.initIndex("restaurante_menu_items");

const GEMINIKEY = process.env.PRIVATEKEY_GEMINI;
const GEMINI_URL =
  "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent";

/* =========================================================================
   RAMA BUSQUEDA_ALGOLIA — el usuario busca un producto o categoría puntual
   Modelos:
     1) gpt-5.4-nano   → clasificador rapidísimo: saca el término de búsqueda
     2) Algolia        → matchea ese término contra "restaurante_menu_items"
     3) gemini-2.5-flash → redacta la respuesta (nunca ve precio ni imagen)

   Cada registro de Algolia trae: categoria, nombre, precio, disponible, imagen(es).
   A Gemini SOLO le llega: id, nombre, categoria, disponible.
     - El precio NUNCA se le manda a Gemini. Se arma por código, aparte,
       y se pega al final del mensaje ya generado.
     - La imagen NUNCA se le manda a Gemini. Se devuelve aparte en
       "imagenes" para que quien llama a esta función la mande por WhatsApp.

   Si no hay match: se recomienda un pequeño puñado de productos (mejor
   esfuerzo) y se le pide a Gemini que además ofrezca mandar la carta visual.

   Devuelve: { mensaje, extra, imagenes, productos }
========================================================================= */

/**
 * Clasificador ultra liviano: extrae el término de búsqueda (nombre o
 * categoría) del mensaje del usuario, para pasárselo directo a Algolia.
 * Prompt mínimo a propósito — esto tiene que ser rápido.
 */
async function extraerTerminoBusqueda(mensajeUsuario) {
  try {
    const completion = await openai.chat.completions.create({
      model: "gpt-5.4-nano",
      messages: [
        {
          role: "system",
          content:
            "Extrae solo el nombre de producto o categoría que el usuario busca. Corto, sin relleno.",
        },
        { role: "user", content: mensajeUsuario },
      ],
      response_format: {
        type: "json_schema",
        json_schema: {
          name: "termino_busqueda",
          strict: true,
          schema: {
            type: "object",
            properties: { query: { type: "string" } },
            required: ["query"],
            additionalProperties: false,
          },
        },
      },
    });

    const parsed = JSON.parse(completion.choices[0].message.content);
    return (parsed.query || mensajeUsuario).trim();
  } catch (err) {
    console.error("[busqueda_algolia] Error extrayendo término, uso mensaje crudo:", err);
    return mensajeUsuario;
  }
}

/** Deja el producto listo para Gemini: nunca precio, nunca imagen. */
function sanitizarProducto(hit) {
  return {
    id: hit.objectID,
    nombre: hit.nombre,
    categoria: hit.categoria,
    disponible: !!hit.disponible,
  };
}

/** Junta las imágenes de los productos mostrados (soporta campo "imagen" o "imagenes"). */
function extraerImagenes(productos) {
  const imgs = [];
  productos.forEach((p) => {
    if (Array.isArray(p.imagenes)) imgs.push(...p.imagenes);
    else if (p.imagen) imgs.push(p.imagen);
  });
  return imgs.filter(Boolean);
}

/** Arma el bloque de precios que se pega al final del mensaje (por código, no por Gemini). */
function construirLineaPrecios(productos) {
  const lineas = productos
    .filter((p) => p.precio !== undefined && p.precio !== null)
    .map((p) => `• ${p.nombre}: S/ ${Number(p.precio).toFixed(2)}`);
  return lineas.length ? `\n\n${lineas.join("\n")}` : "";
}

const RESPONSE_SCHEMA = {
  type: "object",
  properties: {
    mensaje: { type: "string" },
    extra: { type: "string" },
  },
  required: ["mensaje", "extra"],
};

const SYSTEM_PROMPT = `Eres el asistente de un negocio por WhatsApp. El usuario buscó un producto o categoría.
Te paso una lista de productos (id, nombre, categoria, disponible) que hicieron match en el catálogo, o recomendaciones si no hubo match exacto (sin_match: true).

Reglas:
- NUNCA sabes el precio ni ves fotos — no los menciones ni los inventes, ni digas "cuesta" ni "aquí la foto". Eso se agrega después por fuera de tu respuesta.
- Si hay productos, preséntalos de forma breve y natural, indicando si están disponibles o agotados.
- Si sin_match es true, dilo con calidez, ofrece las recomendaciones que te di como alternativa, y pregunta si quiere que le mandes la carta completa para ver todo.
- Si tienes el nombre del usuario, puedes usarlo con naturalidad.
- Sé breve, como un mensaje real de WhatsApp.

Responde ÚNICAMENTE con un JSON:
- "mensaje": el texto para el usuario (sin precios).
- "extra": resumen de MÁXIMO 6 palabras de este turno (ej. "buscó chifa, mostré 3 platos disponibles").`;

/**
 * Flujo completo de la rama busqueda_algolia.
 *
 * @param {Object} params
 * @param {string} params.mensaje
 * @param {string} [params.nombre_usuario]
 * @param {string} [params.extra_anterior]
 * @returns {Promise<{mensaje: string, extra: string, imagenes: string[], productos: Array}>}
 */
async function responderBusquedaAlgolia({ mensaje, nombre_usuario, extra_anterior }) {
  const termino = await extraerTerminoBusqueda(mensaje);

  let hits = [];
  try {
    const resultado = await index.search(termino, { hitsPerPage: 5 });
    hits = resultado.hits || [];
  } catch (err) {
    console.error("[busqueda_algolia] Error buscando en Algolia:", err);
  }

  let sinMatch = false;
  let productosMostrados = hits;

  if (productosMostrados.length === 0) {
    sinMatch = true;
    try {
      const recomendados = await index.search("", { hitsPerPage: 3 });
      productosMostrados = recomendados.hits || [];
    } catch (err) {
      console.error("[busqueda_algolia] Error obteniendo recomendaciones:", err);
      productosMostrados = [];
    }
  }

  const productosSanitizados = productosMostrados.map(sanitizarProducto);

  const contexto = [
    nombre_usuario ? `nombre_usuario: ${nombre_usuario}` : null,
    extra_anterior ? `extra_anterior: ${extra_anterior}` : null,
    `termino_buscado: ${termino}`,
    `sin_match: ${sinMatch}`,
    `productos: ${JSON.stringify(productosSanitizados)}`,
  ]
    .filter(Boolean)
    .join("\n");

  const promptUsuario = `Contexto:\n${contexto}\n\nMensaje del usuario:\n${mensaje}`;

  try {
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
    if (!parsed.mensaje || !parsed.extra) {
      throw new Error("Respuesta de Gemini incompleta.");
    }

    return {
      mensaje: parsed.mensaje + construirLineaPrecios(productosMostrados),
      extra: parsed.extra,
      imagenes: extraerImagenes(productosMostrados),
      productos: productosSanitizados,
    };
  } catch (err) {
    console.error("[busqueda_algolia] Error generando respuesta:", err);
    return {
      mensaje: sinMatch
        ? "No encontré justo eso, pero puedo mostrarte la carta completa si quieres."
        : "Encontré algunas opciones, pero tuve un problema para redactarte los detalles. ¿Quieres que te mande la carta completa?",
      extra: "error generando respuesta busqueda",
      imagenes: extraerImagenes(productosMostrados),
      productos: productosSanitizados,
    };
  }
}

/* =========================================================================
   ENDPOINT HTTP — solo para pruebas manuales, igual que general y carta_visual.
========================================================================= */
exports.busquedaAlgolia = onRequest(async (req, res) => {
  try {
    const { numero_usuario, nombre_usuario, mensaje, extra_anterior } =
      req.body || {};

    if (!numero_usuario || !mensaje) {
      return res
        .status(400)
        .json({ error: "Faltan numero_usuario o mensaje." });
    }

    const respuesta = await responderBusquedaAlgolia({
      mensaje,
      nombre_usuario,
      extra_anterior,
    });

    return res.status(200).json(respuesta);
  } catch (err) {
    console.error("[busqueda_algolia] Error en el endpoint:", err);
    return res
      .status(500)
      .json({ error: "Error interno de la rama busqueda_algolia." });
  }
});

module.exports.responderBusquedaAlgolia = responderBusquedaAlgolia;