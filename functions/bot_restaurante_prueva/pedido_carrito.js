const { onRequest } = require("firebase-functions/v2/https");
const admin = require("firebase-admin");
const algoliasearch = require("algoliasearch");
const OpenAI = require("openai");
const crypto = require("node:crypto");
const {
  tokenOpenAI,
  tokenGemini,
  armarTokens,
  combinarTokens,
} = require("./token_utils.js");

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
const {
  obtenerCarrito,
  guardarCarrito,
  eliminarItemCarritoPorId,
  vaciarCarrito,
  obtenerOCrearTokenCarrito,
} = require("./carrito_store.js");
// 👇 NUEVO — dominio de la página del carrito (la construyes tú después).
// Se arma como: `${URL_BASE_CARRITO}?token=<token_del_usuario>`
// 👈 AJUSTAR cuando tengas la página real, vía env var URL_BASE_CARRITO.
const URL_BASE_CARRITO =
  process.env.URL_BASE_CARRITO || "https://TU-DOMINIO-AQUI.com/carrito";

// Máximo de productos que se muestran en el chat antes de mandar al link.
const MAX_ITEMS_VISIBLES_CHAT = 3;

/* =========================================================================
   RAMA CARRITO — el usuario pide guardar o quitar un producto de su lista
   Modelos:
     1) gpt-5.4-nano      → clasificador rápido: saca la ACCIÓN (agregar /
                            eliminar / consultar / otro) y la LISTA de
                            productos que menciona.
     2) Algolia           → matchea cada producto mencionado contra
                            "restaurante_menu_items".
     3) Firestore         → colección "carritos", doc id = numero_usuario.
     4) gemini-2.5-flash  → redacta el resumen del carrito.

   👇 TOKENS: extraerIntencionCarrito() (OpenAI) y redactarResumenCarrito()
   (Gemini) ya loggeaban tokens en consola, pero ninguna los devolvía. Ahora
   ambas devuelven {..., tokens} y responderCarrito() combina ambos con
   combinarTokens() en el "tokens" final de la rama.

   👇 NUEVO — BOTONES DE ELIMINACIÓN (sin IA):
   Además del flujo normal (usuario escribe / manda audio y la IA
   interpreta), ahora cada vez que se muestra el carrito se arma un teclado
   inline con un botón "🗑️ <producto>" por item + un botón "🧹 Vaciar
   carrito". Cuando el usuario toca uno de esos botones, Telegram manda un
   callback_query (lo maneja el webhook, NO este archivo) que llama
   directo a eliminarItemCarritoPorId() / vaciarCarrito() — pega directo a
   Firestore, sin pasar por ningún modelo. Esto es intencional: es más
   rápido, gratis en tokens, y 100% determinístico (no depende de que la
   IA "entienda" qué quiso decir el usuario).

   Devuelve: { mensaje, extra, carrito, tokens, botones }
   - "botones" es el inline_keyboard (array de filas) listo para mandarle
     a Telegram tal cual, ver construirBotonesCarrito() más abajo.
========================================================================= */

/**
 * Clasificador ultra liviano: saca la acción (agregar/eliminar/consultar/otro)
 * y la lista de productos CON CANTIDAD que el usuario menciona.
 *
 * 👇 FIX IMPORTANTE: antes esta función no recibía el carrito actual, así
 * que no podía distinguir "elimina la limonada" (quitar el producto entero)
 * de "elimina una limonada, solo quiero 1" (restar 1 unidad y dejar 1). Le
 * pasamos el carrito actual como contexto para que el propio modelo calcule
 * cuántas unidades hay que restar/sumar, en vez de que el código asuma
 * "eliminar = borrar todo el producto" a ciegas.
 *
 * Contrato de "cantidad" en cada producto de la respuesta:
 *   - accion "agregar": cantidad = unidades NUEVAS a sumar (1 si no especifica).
 *   - accion "eliminar":
 *       - cantidad = número → restar esa cantidad de lo que ya tiene.
 *       - cantidad = null   → quitar el producto POR COMPLETO (el usuario no
 *         dio ninguna pista de cantidad, ej. "ya no quiero pollo").
 *
 * @param {string} mensajeUsuario
 * @param {string} [extraAnterior]
 * @param {Array<{nombre:string, cantidad:number}>} [carritoActual]
 * @returns {Promise<{accion: "agregar"|"eliminar"|"consultar"|"otro", productos: Array<{nombre:string, cantidad: number|null}>, tokens: {detalle: Array, total: number}}>}
 */
async function extraerIntencionCarrito(
  mensajeUsuario,
  extraAnterior,
  carritoActual,
) {
  console.log(
    "[carrito] → extraerIntencionCarrito | mensaje:",
    mensajeUsuario,
    "| extraAnterior:",
    extraAnterior,
  );
  try {
    const hayContextoUtil =
      extraAnterior &&
      extraAnterior !== "null" &&
      String(extraAnterior).trim() !== "";

    const resumenCarrito =
      carritoActual && carritoActual.length
        ? carritoActual
            .map((i) => `${i.nombre} (tiene ${i.cantidad || 1})`)
            .join(", ")
        : "vacío";

    let systemContent =
      "Eres un clasificador de intención para un carrito de pedidos por WhatsApp/Telegram. " +
      "Del mensaje del usuario, saca:\n" +
      '1) "accion": una de "agregar" (quiere sumar un producto a su lista), ' +
      '"eliminar" (quiere quitar o reducir un producto de su lista), "consultar" (quiere ' +
      'ver su lista/total actual) u "otro" (nada de lo anterior).\n' +
      '2) "productos": lista de objetos {"nombre": string, "cantidad": number|null}, uno por ' +
      'cada producto mencionado, con el nombre corto y limpio (sin relleno como "quiero", ' +
      '"agrégame", "ya no quiero").\n\n' +
      `El carrito ACTUAL del usuario es: ${resumenCarrito}.\n\n` +
      'REGLAS PARA "cantidad" según la acción:\n' +
      '- Si accion es "agregar": cantidad = cuántas unidades NUEVAS quiere sumar (usa 1 si no da número).\n' +
      '- Si accion es "eliminar" y el usuario da una pista de CUÁNTAS unidades quitar o CUÁNTAS quiere que ' +
      'queden (ej. "elimina una", "solo quiero 1", "que se quede en 2", "quita 3"), CALCULA TÚ MISMO usando ' +
      'la cantidad actual del carrito cuántas unidades hay que restar, y pon ese número en "cantidad" ' +
      '(ejemplo: si tiene 2 y el usuario dice "solo quiero 1", cantidad = 1, porque hay que restar 1).\n' +
      '- Si accion es "eliminar" y el usuario NO da ninguna pista de cantidad (ej. "elimina la limonada", ' +
      '"ya no quiero pollo"), pon cantidad = null, que significa quitar el producto POR COMPLETO.\n' +
      '- Si accion es "consultar" u "otro", productos puede ir vacío.';

    if (hayContextoUtil) {
      systemContent +=
        "\n\nTambién recibes el CONTEXTO del turno anterior. Si el mensaje actual es una " +
        'confirmación corta o ambigua (ej. "sí", "ese", "quítalo", "el mismo") y no nombra ' +
        "un producto nuevo, usa el producto del contexto para resolver a qué se refiere.";
    }

    const messages = [{ role: "system", content: systemContent }];
    if (hayContextoUtil) {
      messages.push({
        role: "system",
        content: `CONTEXTO del turno anterior: ${extraAnterior}`,
      });
      console.log(
        "[carrito] Contexto útil enviado al extractor:",
        extraAnterior,
      );
    } else {
      console.log(
        "[carrito] Sin contexto útil, extrayendo solo con el mensaje",
      );
    }
    messages.push({ role: "user", content: mensajeUsuario });

    const completion = await openai.chat.completions.create({
      model: "gpt-5.4-nano",
      messages,
      response_format: {
        type: "json_schema",
        json_schema: {
          name: "intencion_carrito",
          strict: true,
          schema: {
            type: "object",
            properties: {
              accion: {
                type: "string",
                enum: ["agregar", "eliminar", "consultar", "otro"],
              },
              productos: {
                type: "array",
                items: {
                  type: "object",
                  properties: {
                    nombre: { type: "string" },
                    cantidad: { type: ["integer", "null"] },
                  },
                  required: ["nombre", "cantidad"],
                  additionalProperties: false,
                },
              },
            },
            required: ["accion", "productos"],
            additionalProperties: false,
          },
        },
      },
    });

    // 👇 TOKENS: ahora sí se devuelven, antes solo se loggeaban.
    const tokens = armarTokens([tokenOpenAI(completion.usage, "gpt-5.4-nano")]);
    console.log(
      "[carrito] Tokens usados en extraerIntencionCarrito (gpt-5.4-nano):",
      "prompt_tokens:",
      completion.usage?.prompt_tokens,
      "| completion_tokens:",
      completion.usage?.completion_tokens,
      "| total_tokens:",
      completion.usage?.total_tokens,
    );

    const contenido = completion.choices[0].message.content;
    console.log("[carrito] Respuesta cruda del extractor:", contenido);

    const parsed = JSON.parse(contenido);
    const productos = Array.isArray(parsed.productos)
      ? parsed.productos
          .map((p) => ({
            nombre: String(p?.nombre || "").trim(),
            cantidad:
              p?.cantidad === null || p?.cantidad === undefined
                ? null
                : Number(p.cantidad),
          }))
          .filter((p) => p.nombre)
      : [];

    const resultado = { accion: parsed.accion || "otro", productos, tokens };
    console.log(
      "[carrito] ← Intención extraída:",
      JSON.stringify({
        accion: resultado.accion,
        productos: resultado.productos,
      }),
    );
    return resultado;
  } catch (err) {
    console.error(
      "[carrito] ❌ Error extrayendo intención, uso 'otro' como fallback:",
      err.message,
    );
    return { accion: "otro", productos: [], tokens: armarTokens([]) };
  }
}

/**
 * Matchea cada producto mencionado (con su cantidad) contra Algolia y
 * devuelve el mejor hit de cada uno junto con la cantidad que venía del
 * clasificador, o nada si no matcheó.
 *
 * @param {Array<{nombre:string, cantidad:number|null}>} productos
 * @returns {Promise<Array<{hit:Object, cantidad:number|null}>>}
 */
async function matchearProductosAlgolia(productos) {
  console.log(
    "[carrito] → matchearProductosAlgolia | productos:",
    JSON.stringify(productos),
  );
  const vistos = new Set();
  const matcheados = [];

  for (const producto of productos) {
    try {
      const resultado = await index.search(producto.nombre, { hitsPerPage: 1 });
      const hit = (resultado.hits || [])[0];
      console.log(
        `[carrito] Algolia | término: "${producto.nombre}" | match:`,
        hit ? hit.nombre : "sin match",
      );

      if (hit && !vistos.has(hit.objectID)) {
        vistos.add(hit.objectID);
        matcheados.push({ hit, cantidad: producto.cantidad });
      }
    } catch (err) {
      console.error(
        `[carrito] ❌ Error buscando en Algolia el término "${producto.nombre}":`,
        err.message,
      );
    }
  }

  console.log(
    "[carrito] ← matchearProductosAlgolia | total matcheado:",
    matcheados.length,
  );
  return matcheados;
}

/* -------------------------------------------------------------------------
   Persistencia del carrito en Firestore.
   Colección "carritos", doc id = numero_usuario, campo "items".
------------------------------------------------------------------------- */

/**
 * Agrega productos matcheados al carrito actual, sumando la "cantidad"
 * indicada (por defecto 1) a lo que ya existe.
 *
 * @param {Array} carritoActual
 * @param {Array<{hit:Object, cantidad:number|null}>} matcheados
 */
function agregarItemsACarrito(carritoActual, matcheados) {
  const items = [...carritoActual];
  for (const { hit, cantidad } of matcheados) {
    const aSumar = cantidad && cantidad > 0 ? cantidad : 1;
    const existente = items.find((i) => i.id === hit.objectID);
    if (existente) {
      existente.cantidad = (existente.cantidad || 1) + aSumar;
    } else {
      items.push({
        id: hit.objectID,
        nombre: hit.nombre,
        precio: hit.precio,
        cantidad: aSumar,
      });
    }
  }
  return items;
}

/**
 * 👇 FIX: antes esto borraba el item ENTERO del carrito sin importar la
 * cantidad ("elimina una limonada, solo quiero 1" con 2 en el carrito
 * vaciaba el producto en vez de dejar 1). Ahora:
 *   - cantidad === null → se interpreta como "quitar el producto por
 *     completo" (el usuario no dio ninguna pista de cantidad).
 *   - cantidad === número → se resta esa cantidad de lo que ya tenía; si
 *     el resultado queda en 0 o menos, recién ahí se elimina el item.
 *
 * @param {Array} carritoActual
 * @param {Array<{hit:Object, cantidad:number|null}>} matcheados
 */
function quitarItemsDeCarrito(carritoActual, matcheados) {
  let items = [...carritoActual];

  for (const { hit, cantidad } of matcheados) {
    const existente = items.find((i) => i.id === hit.objectID);
    if (!existente) continue;

    if (cantidad === null || cantidad === undefined) {
      // Sin pista de cantidad → se quita el producto completo.
      items = items.filter((i) => i.id !== hit.objectID);
      continue;
    }

    const cantidadActual = existente.cantidad || 1;
    const cantidadNueva = cantidadActual - cantidad;

    if (cantidadNueva <= 0) {
      items = items.filter((i) => i.id !== hit.objectID);
    } else {
      existente.cantidad = cantidadNueva;
    }
  }

  return items;
}

/* =========================================================================
   👇 NUEVO — ELIMINACIÓN DIRECTA POR BOTÓN (sin IA, sin Algolia)
   Estas 3 funciones son las que llama el webhook cuando llega un
   callback_query de Telegram (el usuario tocó un botón). Van directo a
   Firestore usando el "id" que ya viene guardado en el item — no hace
   falta reinterpretar nada.
========================================================================= */

/** Arma la URL completa del carrito para un token dado. */
function armarLinkCarrito(token) {
  return `${URL_BASE_CARRITO}?token=${token}`;
}

/**
 * Arma el teclado inline para mostrar junto al carrito:
 *   - Máximo MAX_ITEMS_VISIBLES_CHAT botones "🗑️ <nombre>"
 *     (callback_data: "car_del:<id>") — si el carrito tiene más productos
 *     que ese límite, los restantes NO se listan aquí, se ven en el link.
 *   - Un botón "🧹 Vaciar carrito" (callback_data: "car_vaciar").
 *   - Si se pasa un "token", un botón de URL "🛒 Ver / editar pedido
 *     completo" que abre la página del carrito (la que construyas tú).
 *
 * @param {Array<{id:string,nombre:string}>} carrito
 * @param {{token?: string}} [opciones]
 * @returns {Array<Array<{text:string, callback_data?:string, url?:string}>>}
 */
function construirBotonesCarrito(carrito, { token } = {}) {
  if (!carrito || carrito.length === 0) return [];

  const visibles = carrito.slice(0, MAX_ITEMS_VISIBLES_CHAT);

  const inline_keyboard = visibles.map((item) => [
    {
      text: `🗑️ ${String(item.nombre).slice(0, 40)}`,
      callback_data: `car_del:${item.id}`,
    },
  ]);

  inline_keyboard.push([
    { text: "🧹 Vaciar carrito", callback_data: "car_vaciar" },
  ]);

  if (token) {
    inline_keyboard.push([
      { text: "🛒 Ver / editar pedido completo", url: armarLinkCarrito(token) },
    ]);
  }

  return inline_keyboard;
}

/**
 * Arma el texto plano del carrito SIN llamar a ninguna IA. Se usa en el
 * camino rápido de "el usuario tocó un botón" — ahí no hace falta
 * redacción con IA, solo mostrar el estado actualizado al toque.
 *
 * Muestra máximo MAX_ITEMS_VISIBLES_CHAT productos; si hay más, lo indica
 * y remite al botón de link. El TOTAL siempre se calcula sobre TODOS los
 * productos, no solo los visibles.
 *
 * @param {Array<{nombre:string,precio:number,cantidad:number}>} carrito
 * @param {string} [nombreUsuario]
 * @returns {string}
 */
function formatearCarritoSinIA(carrito, nombreUsuario) {
  if (!carrito || carrito.length === 0) {
    return `${nombreUsuario ? nombreUsuario + ", tu" : "Tu"} carrito quedó vacío 🛒\n¿Quieres pedir algo más?`;
  }

  let total = 0;
  carrito.forEach((item) => {
    total += (item.precio || 0) * (item.cantidad || 1);
  });

  const visibles = carrito.slice(0, MAX_ITEMS_VISIBLES_CHAT);
  const lineas = visibles.map((item) => {
    const cantidad = item.cantidad || 1;
    const subtotal = (item.precio || 0) * cantidad;
    return `• ${cantidad}x ${item.nombre} — S/${subtotal.toFixed(2)}`;
  });

  const restantes = carrito.length - visibles.length;
  const notaRestantes =
    restantes > 0
      ? `\n…y ${restantes} producto${restantes > 1 ? "s" : ""} más.`
      : "";

  return (
    `🛒 Tu carrito (${carrito.length} producto${carrito.length > 1 ? "s" : ""}):\n\n` +
    `${lineas.join("\n")}${notaRestantes}\n\n` +
    `💰 Total: S/${total.toFixed(2)}\n\n` +
    `Toca 🗑️ para quitar uno, o usa el botón de abajo para ver/editar todo tu pedido.`
  );
}

const RESPONSE_SCHEMA = {
  type: "object",
  properties: {
    mensaje: { type: "string" },
    extra: { type: "string" },
  },
  required: ["mensaje", "extra"],
};

const SYSTEM_PROMPT = `Eres el asistente de un negocio por WhatsApp/Telegram. El usuario está armando su pedido (carrito).
Te paso hasta ${MAX_ITEMS_VISIBLES_CHAT} productos para MOSTRAR en el mensaje (aunque el carrito real tenga más), la cantidad TOTAL de productos distintos que tiene, el TOTAL A PAGAR de absolutamente todo el carrito (no solo lo que te muestro), y qué acción se acaba de hacer (agregar, eliminar, consultar, o ninguna si no matcheó nada).

Reglas:
- Redacta un mensaje breve y natural confirmando la acción (si hubo) y mostrando SOLO los productos que te pasé, con sus precios.
- Muestra el TOTAL A PAGAR que te doy (ya viene calculado sobre todo el carrito, no lo recalcules).
- Si "cantidad_total_de_productos_distintos" es mayor a la cantidad de productos que te mostré, menciona brevemente que hay más productos y que puede verlos/editarlos todos con el botón de abajo (no inventes cuáles son).
- Si el carrito quedó vacío, dilo con naturalidad e invita a pedir algo.
- Si se pidió agregar o eliminar un producto que no matcheó ningún producto real, dilo con calidez y no lo inventes.
- Los métodos de pago todavía no están disponibles — si el usuario pregunta por eso, dile que en un momento le confirman las opciones de pago.
- Si tienes el nombre del usuario, puedes usarlo con naturalidad.
- Sé breve, como un mensaje real de chat.
- No expliques cómo funcionan los botones ni el link — eso ya se lo mostramos aparte, solo menciona que existen si aplica.

Responde ÚNICAMENTE con un JSON:
- "mensaje": el texto para el usuario.
- "extra": resumen de MÁXIMO 6 palabras de este turno (ej. "agregó chaufa, carrito en S/25").`;

/**
 * Le pide a Gemini que redacte el resumen del carrito (lista + total).
 * Solo le pasa hasta MAX_ITEMS_VISIBLES_CHAT productos para mostrar, pero
 * el total y la cantidad de productos distintos se calculan sobre TODO
 * el carrito real.
 *
 * @param {Array<{id:string,nombre:string,precio:number,cantidad:number}>} items
 * @param {string} accion
 * @param {string} [nombreUsuario]
 * @returns {Promise<{mensaje:string, extra:string, tokens: {detalle: Array, total: number}}>}
 */
async function redactarResumenCarrito(items, accion, nombreUsuario) {
  const itemsVisibles = items.slice(0, MAX_ITEMS_VISIBLES_CHAT).map((i) => ({
    nombre: i.nombre,
    precio: i.precio,
    cantidad: i.cantidad || 1,
  }));

  let totalGeneral = 0;
  items.forEach((i) => {
    totalGeneral += (i.precio || 0) * (i.cantidad || 1);
  });

  const contexto = [
    nombreUsuario ? `nombre_usuario: ${nombreUsuario}` : null,
    `accion: ${accion}`,
    `productos_para_mostrar: ${JSON.stringify(itemsVisibles)}`,
    `cantidad_total_de_productos_distintos: ${items.length}`,
    `total_a_pagar_de_TODO_el_carrito: ${totalGeneral.toFixed(2)}`,
  ]
    .filter(Boolean)
    .join("\n");

  console.log(
    "[carrito] Llamando a Gemini (gemini-2.5-flash) para redactar resumen del carrito...",
  );
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

  // 👇 TOKENS: ahora sí se devuelven, antes solo se loggeaban.
  const tokens = armarTokens([
    tokenGemini(data?.usageMetadata, "gemini-2.5-flash"),
  ]);
  console.log(
    "[carrito] Tokens usados en Gemini (gemini-2.5-flash):",
    "prompt_tokens:",
    data?.usageMetadata?.promptTokenCount,
    "| respuesta_tokens:",
    data?.usageMetadata?.candidatesTokenCount,
    "| total_tokens:",
    data?.usageMetadata?.totalTokenCount,
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
 * Flujo completo de la rama carrito (cuando el usuario escribe o manda
 * audio — NO cuando toca un botón, eso lo maneja el webhook directo con
 * eliminarItemCarritoPorId / vaciarCarrito).
 *
 * @param {Object} params
 * @param {string} params.mensaje
 * @param {string} params.numero_usuario
 * @param {string} [params.nombre_usuario]
 * @param {string} [params.extra_anterior]
 * @returns {Promise<{mensaje: string, extra: string, carrito: Array, tokens: {detalle: Array, total: number}, botones: Array}>}
 */
async function responderCarrito({
  mensaje,
  numero_usuario,
  nombre_usuario,
  extra_anterior,
}) {
  console.log(
    "[carrito] → responderCarrito | mensaje:",
    mensaje,
    "| numero_usuario:",
    numero_usuario,
    "| extra_anterior:",
    extra_anterior,
  );

  // 👇 FIX: se lee el carrito ANTES de clasificar, y se le pasa al
  // extractor como contexto — así el modelo puede calcular cuántas
  // unidades restar/sumar en vez de solo decir "eliminar sí/no".
  let carritoActual = await obtenerCarrito(numero_usuario);
  const intencion = await extraerIntencionCarrito(
    mensaje,
    extra_anterior,
    carritoActual,
  );

  if (intencion.accion === "agregar" && intencion.productos.length > 0) {
    const matcheados = await matchearProductosAlgolia(intencion.productos);
    carritoActual = agregarItemsACarrito(carritoActual, matcheados);
    await guardarCarrito(numero_usuario, carritoActual, {
      canal: "telegram",
      nombre_usuario,
    });
  } else if (
    intencion.accion === "eliminar" &&
    intencion.productos.length > 0
  ) {
    const matcheados = await matchearProductosAlgolia(intencion.productos);
    carritoActual = quitarItemsDeCarrito(carritoActual, matcheados);
    await guardarCarrito(numero_usuario, carritoActual, {
      canal: "telegram",
      nombre_usuario,
    });
  }
  // "consultar" y "otro" no tocan el carrito, solo se redacta el estado actual.

  // 👇 Botones se arman siempre en base al carrito YA actualizado, sin IA.
  // El token es el mismo siempre para ese usuario (se genera una sola vez).
  const token = await obtenerOCrearTokenCarrito(numero_usuario);
  const botones = construirBotonesCarrito(carritoActual, { token });

  try {
    const {
      mensaje: textoFinal,
      extra,
      tokens: tokensRedaccion,
    } = await redactarResumenCarrito(
      carritoActual,
      intencion.accion,
      nombre_usuario,
    );

    // 👇 TOKENS: se combina lo gastado en el clasificador (OpenAI) con la
    // redacción del resumen (Gemini) para tener el total de la rama.
    const tokens = combinarTokens(intencion.tokens, tokensRedaccion);
    console.log(
      "[carrito] Tokens | clasificación (OpenAI):",
      intencion.tokens.total,
      "| redacción (Gemini):",
      tokensRedaccion.total,
      "| total rama:",
      tokens.total,
    );

    console.log(
      "[carrito] ✅ Respuesta final | extra:",
      extra,
      "| items en carrito:",
      carritoActual.length,
    );

    return {
      mensaje: textoFinal,
      extra,
      carrito: carritoActual,
      tokens,
      botones,
    };
  } catch (err) {
    console.error("[carrito] ❌ Error generando respuesta:", err.message);
    return {
      mensaje: formatearCarritoSinIA(carritoActual, nombre_usuario),
      extra: "error generando resumen carrito",
      carrito: carritoActual,
      // Al menos la clasificación sí se pudo confirmar.
      tokens: intencion.tokens,
      botones,
    };
  }
}

/* =========================================================================
   ENDPOINT HTTP — solo para pruebas manuales, igual que busqueda_algolia.js.
========================================================================= */
exports.carrito = onRequest(async (req, res) => {
  console.log("[carrito] === Nueva petición HTTP a /carrito ===");
  try {
    const { numero_usuario, nombre_usuario, mensaje, extra_anterior } =
      req.body || {};

    if (!numero_usuario || !mensaje) {
      return res
        .status(400)
        .json({ error: "Faltan numero_usuario o mensaje." });
    }

    const respuesta = await responderCarrito({
      mensaje,
      numero_usuario,
      nombre_usuario,
      extra_anterior,
    });

    return res.status(200).json(respuesta);
  } catch (err) {
    console.error(
      "[carrito] ❌ Error en el endpoint:",
      err.message,
      "| stack:",
      err.stack,
    );
    return res.status(500).json({ error: "Error interno de la rama carrito." });
  }
});

/**
 * 👇 NUEVO — Agrega UN producto al carrito por su id de Algolia (objectID),
 * sumando 1 unidad. Pensado para el botón "➕ <producto>" que aparece en
 * los resultados de busqueda_algolia.js.
 *
 * @param {string} numeroUsuario
 * @param {string} itemId - objectID de Algolia
 * @returns {Promise<Array>} el carrito ya actualizado
 */
async function agregarItemCarritoPorId(
  numeroUsuario,
  itemId,
  canal = "telegram",
) {
  console.log(
    "[carrito] → agregarItemCarritoPorId | numero_usuario:",
    numeroUsuario,
    "| itemId:",
    itemId,
  );

  let hit;
  try {
    hit = await index.getObject(itemId);
  } catch (err) {
    console.error(
      "[carrito] ❌ No se pudo obtener el producto de Algolia:",
      err.message,
    );
    return await obtenerCarrito(numeroUsuario);
  }

  const carritoActual = await obtenerCarrito(numeroUsuario);
  const nuevoCarrito = agregarItemsACarrito(carritoActual, [
    { hit, cantidad: 1 },
  ]);
  await guardarCarrito(numeroUsuario, nuevoCarrito, { canal });

  console.log(
    "[carrito] ← agregarItemCarritoPorId | items antes:",
    carritoActual.length,
    "| items después:",
    nuevoCarrito.length,
  );
  return nuevoCarrito;
}

module.exports.responderCarrito = responderCarrito;
module.exports.agregarItemCarritoPorId = agregarItemCarritoPorId;
// 👇 NUEVO — exportado para que el webhook de Telegram pueda manejar los
// callback_query de los botones directo, sin pasar por responderCarrito.
module.exports.eliminarItemCarritoPorId = eliminarItemCarritoPorId;
module.exports.vaciarCarrito = vaciarCarrito;
module.exports.obtenerCarrito = obtenerCarrito;
module.exports.construirBotonesCarrito = construirBotonesCarrito;
module.exports.formatearCarritoSinIA = formatearCarritoSinIA;
module.exports.obtenerOCrearTokenCarrito = obtenerOCrearTokenCarrito;
module.exports.armarLinkCarrito = armarLinkCarrito;
