const { onRequest } = require("firebase-functions/v2/https");
const admin = require("firebase-admin");
const algoliasearch = require("algoliasearch");
const OpenAI = require("openai");
const { tokenOpenAI, tokenGemini, armarTokens, combinarTokens } = require("./token_utils.js");

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

// Máximo de productos que se le mandan a Gemini y que se muestran al
// usuario, ya sea porque matchearon o porque son recomendaciones.
const MAX_PRODUCTOS_MOSTRADOS = 5;

/* =========================================================================
   RAMA BUSQUEDA_ALGOLIA — el usuario busca uno o varios productos/categorías
   Modelos:
     1) gpt-5.4-nano      → clasificador rápido: saca UNA LISTA de términos
                            de búsqueda.
     2) Algolia          → matchea CADA término contra "restaurante_menu_items"
                            y se juntan los resultados (sin duplicados).
     3) gemini-2.5-flash → redacta la respuesta (nunca ve precio ni imagen)

   👇 TOKENS: extraerTerminosBusqueda() (OpenAI) y la llamada final a Gemini
   ya loggeaban sus tokens en consola, pero ninguna los devolvía — se
   perdían. Ahora extraerTerminosBusqueda() devuelve {queries, tokens}, y
   responderBusquedaAlgolia() combina esos tokens con los de la llamada a
   Gemini usando combinarTokens(), y los expone en el campo final "tokens".

   Cada registro de Algolia trae: categoria, nombre, precio, disponible, imagen(es).
   A Gemini SOLO le llega: id, nombre, categoria, disponible — y NUNCA más de
   MAX_PRODUCTOS_MOSTRADOS productos, para mantener el prompt chico y barato.
     - El precio NUNCA se le manda a Gemini. Se arma por código, aparte,
       y se pega al final del mensaje ya generado.
     - La imagen NUNCA se le manda a Gemini. Se devuelve aparte en
       "imagenes" para que quien llama a esta función la mande por WhatsApp/Telegram.

   👇 NUEVO — PREGUNTA PENDIENTE GUARDADA EN "extra":
   Cuando Gemini cierra el mensaje con una pregunta al usuario (ej. "¿deseas
   agregar alguno a tu carrito?"), esa pregunta textual se guarda en el
   campo "pregunta" de su respuesta JSON, y se pega al final de "extra" —
   así el próximo turno (el clasificador de dispensador.js, o cualquier
   rama que lea extra_anterior) sabe exactamente qué se le preguntó al
   usuario, no solo un resumen genérico.

   👇 NUEVO — BOTONES "➕ AGREGAR AL CARRITO" (sin IA):
   Cada producto disponible que se muestra (máximo MAX_PRODUCTOS_MOSTRADOS)
   viene con un botón "➕ <nombre>" (callback_data: "bus_add:<id>"). El
   webhook de Telegram maneja ese tap directo — agrega 1 unidad al carrito
   en Firestore sin pasar por ningún modelo, igual que los botones 🗑️ del
   carrito.

   Devuelve: { mensaje, extra, imagenes, productos, tokens, botones }
========================================================================= */

/**
 * Clasificador ultra liviano: extrae la LISTA de productos o categorías que
 * el usuario busca (puede ser uno o varios en el mismo mensaje).
 *
 * @param {string} mensajeUsuario - Mensaje actual del usuario.
 * @param {string} [extraAnterior] - Resumen del turno anterior.
 * @returns {Promise<{queries: string[], tokens: {detalle: Array, total: number}}>}
 */
async function extraerTerminosBusqueda(mensajeUsuario, extraAnterior) {
  console.log("[busqueda_algolia] → extraerTerminosBusqueda | mensaje:", mensajeUsuario, "| extraAnterior:", extraAnterior);
  try {
    const hayContextoUtil =
      extraAnterior && extraAnterior !== "null" && String(extraAnterior).trim() !== "";

    let systemContent =
      "Extrae los nombres de producto o categoría que el usuario busca. " +
      "El usuario puede mencionar UNO o VARIOS productos en el mismo mensaje " +
      '(ej. "¿me recomiendas un chaufa de pollo o un seco de res?" → dos términos). ' +
      "Devuelve cada uno como un término corto y limpio, sin relleno ni palabras de la pregunta " +
      '(nada de "me recomiendas", "tienen", "quiero", etc. — solo el nombre del producto o categoría). ' +
      "Si solo hay uno, devuelve una lista de un solo elemento.";

    if (hayContextoUtil) {
      systemContent +=
        "\n\nTambién vas a recibir el CONTEXTO de lo que se venía hablando (productos ya " +
        "mencionados y, si aplica, una pregunta que el asistente le hizo al usuario). " +
        "Si el mensaje del usuario es una confirmación corta o ambigua (ej. \"claro\", \"sí\", " +
        "\"ok\", \"dale\", \"los dos\", \"ambos\", \"el primero\", \"cualquiera\") y NO menciona " +
        "un producto nuevo, usa los MISMOS productos que aparecen en el contexto como términos " +
        "de búsqueda — NUNCA busques la palabra de confirmación en sí (ej. no busques \"claro\"). " +
        "Si el mensaje sí menciona un producto nuevo y distinto al del contexto, usa ese en su lugar.";
    }

    const messages = [{ role: "system", content: systemContent }];
    if (hayContextoUtil) {
      messages.push({
        role: "system",
        content: `CONTEXTO del turno anterior: ${extraAnterior}`,
      });
      console.log("[busqueda_algolia] Contexto útil enviado al extractor:", extraAnterior);
    } else {
      console.log("[busqueda_algolia] Sin contexto útil, extrayendo solo con el mensaje");
    }
    messages.push({ role: "user", content: mensajeUsuario });

    const completion = await openai.chat.completions.create({
      model: "gpt-5.4-nano",
      messages,
      response_format: {
        type: "json_schema",
        json_schema: {
          name: "terminos_busqueda",
          strict: true,
          schema: {
            type: "object",
            properties: {
              queries: {
                type: "array",
                items: { type: "string" },
              },
            },
            required: ["queries"],
            additionalProperties: false,
          },
        },
      },
    });

    // 👇 TOKENS: ahora sí se devuelven, antes solo se loggeaban.
    const tokens = armarTokens([tokenOpenAI(completion.usage, "gpt-5.4-nano")]);
    console.log(
      "[busqueda_algolia] Tokens usados en extraerTerminosBusqueda (gpt-5.4-nano):",
      "prompt_tokens:", completion.usage?.prompt_tokens,
      "| completion_tokens:", completion.usage?.completion_tokens,
      "| total_tokens:", completion.usage?.total_tokens,
    );

    const contenido = completion.choices[0].message.content;
    console.log("[busqueda_algolia] Respuesta cruda del extractor:", contenido);

    const parsed = JSON.parse(contenido);
    const queries = Array.isArray(parsed.queries)
      ? parsed.queries.map((q) => String(q).trim()).filter(Boolean)
      : [];

    if (queries.length === 0) {
      console.warn("[busqueda_algolia] Extractor no devolvió términos, uso mensaje crudo como fallback");
      return { queries: [mensajeUsuario], tokens };
    }

    console.log("[busqueda_algolia] ← Términos extraídos:", JSON.stringify(queries));
    return { queries, tokens };
  } catch (err) {
    console.error("[busqueda_algolia] ❌ Error extrayendo términos, uso mensaje crudo:", err.message);
    return { queries: [mensajeUsuario], tokens: armarTokens([]) };
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

/**
 * 👇 NUEVO — Arma botones "➕ <producto>" para agregar directo al carrito,
 * uno por cada producto DISPONIBLE que se está mostrando (los agotados no
 * llevan botón, no tiene sentido dejar agregar algo que no hay).
 *
 * callback_data: "bus_add:<objectID>" — el webhook de Telegram lo agarra
 * directo, sin pasar por ningún modelo, y suma 1 unidad al carrito.
 *
 * @param {Array<{objectID:string, nombre:string, disponible?:boolean}>} productos - hits crudos de Algolia
 * @returns {Array<Array<{text:string, callback_data:string}>>}
 */
function construirBotonesAgregarCarrito(productos) {
  const disponibles = (productos || []).filter((p) => p.disponible !== false);
  if (disponibles.length === 0) return [];

  return disponibles.map((p) => [
    {
      text: `➕ ${String(p.nombre).slice(0, 40)}`,
      callback_data: `bus_add:${p.objectID}`,
    },
  ]);
}

/**
 * Busca en Algolia UNA LISTA de términos y junta todos los resultados en
 * un solo array, sin duplicados (por objectID).
 */
async function buscarEnAlgoliaMultiple(terminos) {
  console.log("[busqueda_algolia] → buscarEnAlgoliaMultiple | términos:", JSON.stringify(terminos));
  const vistos = new Set();
  const combinados = [];

  for (const termino of terminos) {
    try {
      const resultado = await index.search(termino, { hitsPerPage: 5 });
      const hits = resultado.hits || [];
      console.log(`[busqueda_algolia] Algolia | término: "${termino}" | resultados:`, hits.length);

      for (const hit of hits) {
        if (!vistos.has(hit.objectID)) {
          vistos.add(hit.objectID);
          combinados.push(hit);
        }
      }
    } catch (err) {
      console.error(`[busqueda_algolia] ❌ Error buscando en Algolia el término "${termino}":`, err.message);
    }
  }

  console.log("[busqueda_algolia] ← buscarEnAlgoliaMultiple | total combinado sin duplicados:", combinados.length);
  return combinados;
}

const RESPONSE_SCHEMA = {
  type: "object",
  properties: {
    mensaje: { type: "string" },
    extra: { type: "string" },
    // 👇 NUEVO — si "mensaje" termina en una pregunta dirigida al usuario,
    // aquí va esa pregunta TEXTUAL. Si no hay pregunta, va "" (vacío).
    pregunta: { type: "string" },
  },
  required: ["mensaje", "extra", "pregunta"],
};

const SYSTEM_PROMPT = `Eres el asistente de un negocio por WhatsApp/Telegram. El usuario buscó uno o varios productos o categorías.
Te paso una lista de productos (id, nombre, categoria, disponible) que hicieron match en el catálogo, o recomendaciones si no hubo match exacto (sin_match: true). Nunca son más de ${MAX_PRODUCTOS_MOSTRADOS} productos.

Reglas:
- NUNCA sabes el precio ni ves fotos — no los menciones ni los inventes, ni digas "cuesta" ni "aquí la foto". Eso se agrega después por fuera de tu respuesta.
- Si hay productos, preséntalos de forma breve y natural, indicando si están disponibles o agotados.
- Si el usuario preguntó por varios productos y solo algunos matchearon, dilo con naturalidad (ej. "el chaufa de pollo sí lo tenemos, el seco de res no lo encontré").
- Si sin_match es true, dilo con calidez, ofrece las recomendaciones que te di como alternativa, y pregunta si quiere que le mandes la carta completa para ver todo.
- Si hay al menos un producto disponible, cierra tu mensaje preguntando si desea agregar alguno a su carrito (los productos disponibles van a aparecer con un botón "➕" debajo, así que puedes mencionar brevemente que puede tocarlo para agregarlo).
- Si tienes el nombre del usuario, puedes usarlo con naturalidad.
- Sé breve, como un mensaje real de chat.

Responde ÚNICAMENTE con un JSON:
- "mensaje": el texto para el usuario (sin precios).
- "extra": resumen de MÁXIMO 6 palabras de este turno (ej. "buscó chifa, mostré 3 platos disponibles").
- "pregunta": si "mensaje" termina con una pregunta dirigida al usuario, escribe esa pregunta TAL CUAL (ej. "¿deseas agregar alguno a tu carrito?"). Si no terminaste con una pregunta, deja "pregunta" como string vacío "".`;

/** Mezcla un array in-place con el algoritmo Fisher-Yates. */
function mezclarAleatorio(array) {
  const copia = [...array];
  for (let i = copia.length - 1; i > 0; i--) {
    const j = Math.floor(Math.random() * (i + 1));
    [copia[i], copia[j]] = [copia[j], copia[i]];
  }
  return copia;
}

/**
 * Devuelve `cantidad` productos elegidos AL AZAR de verdad, para usar como
 * recomendación cuando no hubo match de búsqueda.
 */
async function obtenerRecomendacionesAleatorias(cantidad = MAX_PRODUCTOS_MOSTRADOS) {
  const cantidadFinal = Math.min(cantidad, MAX_PRODUCTOS_MOSTRADOS);
  console.log("[busqueda_algolia] → obtenerRecomendacionesAleatorias | cantidad:", cantidadFinal);
  try {
    const resultado = await index.search("", { hitsPerPage: 15 });
    let pool = resultado.hits || [];
    console.log("[busqueda_algolia] Pool de productos disponible para recomendar:", pool.length);

    const poolFiltrado = pool.filter((p) => p.disponible !== false);
    if (poolFiltrado.length > 0) pool = poolFiltrado;

    const mezclado = mezclarAleatorio(pool);
    const elegidos = mezclado.slice(0, cantidadFinal);
    console.log(
      "[busqueda_algolia] ← Recomendaciones aleatorias elegidas:",
      elegidos.map((p) => p.nombre),
    );
    return elegidos;
  } catch (err) {
    console.error("[busqueda_algolia] ❌ Error obteniendo recomendaciones aleatorias:", err.message);
    return [];
  }
}

/**
 * Flujo completo de la rama busqueda_algolia.
 *
 * @param {Object} params
 * @param {string} params.mensaje
 * @param {string} [params.nombre_usuario]
 * @param {string} [params.extra_anterior]
 * @returns {Promise<{mensaje: string, extra: string, imagenes: string[], productos: Array, tokens: {detalle: Array, total: number}, botones: Array}>}
 */
async function responderBusquedaAlgolia({ mensaje, nombre_usuario, extra_anterior }) {
  console.log("[busqueda_algolia] → responderBusquedaAlgolia | mensaje:", mensaje, "| nombre_usuario:", nombre_usuario, "| extra_anterior:", extra_anterior);

  const { queries: terminos, tokens: tokensExtraccion } = await extraerTerminosBusqueda(mensaje, extra_anterior);
  let hits = await buscarEnAlgoliaMultiple(terminos);

  let sinMatch = false;
  let productosMostrados = hits;

  if (productosMostrados.length === 0) {
    sinMatch = true;
    console.log("[busqueda_algolia] Sin match para ningún término, buscando recomendaciones aleatorias");
    productosMostrados = await obtenerRecomendacionesAleatorias(MAX_PRODUCTOS_MOSTRADOS);
  }

  if (productosMostrados.length > MAX_PRODUCTOS_MOSTRADOS) {
    console.log(
      `[busqueda_algolia] Recortando productos mostrados de ${productosMostrados.length} a ${MAX_PRODUCTOS_MOSTRADOS}`,
    );
    productosMostrados = productosMostrados.slice(0, MAX_PRODUCTOS_MOSTRADOS);
  }

  const productosSanitizados = productosMostrados.map(sanitizarProducto);
  // 👇 NUEVO — botones "➕" listos independientemente de si Gemini responde
  // bien o falla (así el catch de abajo también puede devolverlos).
  const botones = construirBotonesAgregarCarrito(productosMostrados);

  const contexto = [
    nombre_usuario ? `nombre_usuario: ${nombre_usuario}` : null,
    extra_anterior ? `extra_anterior: ${extra_anterior}` : null,
    `sin_match: ${sinMatch}`,
    `productos: ${JSON.stringify(productosSanitizados)}`,
  ]
    .filter(Boolean)
    .join("\n");

  const promptUsuario = `Contexto:\n${contexto}\n\nMensaje del usuario:\n${mensaje}`;

  try {
    console.log("[busqueda_algolia] Llamando a Gemini (gemini-2.5-flash) para redactar respuesta...");
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

    // 👇 TOKENS: se combina lo gastado en extraerTerminosBusqueda (OpenAI)
    // con esta llamada a Gemini para tener el total de la rama completa.
    const tokensRedaccion = armarTokens([tokenGemini(data?.usageMetadata, "gemini-2.5-flash")]);
    const tokens = combinarTokens(tokensExtraccion, tokensRedaccion);
    console.log(
      "[busqueda_algolia] Tokens | extracción (OpenAI):", tokensExtraccion.total,
      "| redacción (Gemini):", tokensRedaccion.total,
      "| total rama:", tokens.total,
    );

    const texto = data?.candidates?.[0]?.content?.parts?.[0]?.text;
    if (!texto) throw new Error("Gemini no devolvió texto en la respuesta.");

    const parsed = JSON.parse(texto);
    if (!parsed.mensaje || !parsed.extra) {
      throw new Error("Respuesta de Gemini incompleta.");
    }

    // 👇 NUEVO — si Gemini cerró con una pregunta, se pega textual al
    // "extra" para que el próximo turno (dispensador.js, u otra rama que
    // lea extra_anterior) sepa EXACTAMENTE qué se le preguntó al usuario,
    // no solo un resumen genérico de 6 palabras.
    const preguntaPendiente = parsed.pregunta && parsed.pregunta.trim() ? parsed.pregunta.trim() : null;
    const extraFinal = preguntaPendiente
      ? `${parsed.extra} | pregunta pendiente: "${preguntaPendiente}"`
      : parsed.extra;

    console.log(
      "[busqueda_algolia] ✅ Respuesta final | extra:", extraFinal,
      "| pregunta pendiente:", preguntaPendiente || "(ninguna)",
      "| productos mostrados:", productosSanitizados.length,
      "| botones agregar:", botones.length,
    );

    return {
      mensaje: parsed.mensaje + construirLineaPrecios(productosMostrados),
      extra: extraFinal,
      imagenes: extraerImagenes(productosMostrados),
      productos: productosSanitizados,
      tokens,
      botones,
    };
  } catch (err) {
    console.error("[busqueda_algolia] ❌ Error generando respuesta:", err.message);
    return {
      mensaje: sinMatch
        ? "No encontré justo eso, pero puedo mostrarte la carta completa si quieres."
        : "Encontré algunas opciones, pero tuve un problema para redactarte los detalles. ¿Quieres que te mande la carta completa?",
      extra: "error generando respuesta busqueda",
      imagenes: extraerImagenes(productosMostrados),
      productos: productosSanitizados,
      // Al menos la extracción de términos sí se pudo confirmar.
      tokens: tokensExtraccion,
      botones,
    };
  }
}

/* =========================================================================
   ENDPOINT HTTP — solo para pruebas manuales, igual que general y carta_visual.
========================================================================= */
exports.busquedaAlgolia = onRequest(async (req, res) => {
  console.log("[busqueda_algolia] === Nueva petición HTTP a /busquedaAlgolia ===");
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
    console.error("[busqueda_algolia] ❌ Error en el endpoint:", err.message, "| stack:", err.stack);
    return res
      .status(500)
      .json({ error: "Error interno de la rama busqueda_algolia." });
  }
});

module.exports.responderBusquedaAlgolia = responderBusquedaAlgolia;