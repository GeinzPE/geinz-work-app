const { onRequest } = require("firebase-functions/v2/https");
const admin = require("firebase-admin");

if (!admin.apps.length) {
  admin.initializeApp();
}

const db = admin.firestore();

const GEMINIKEY = process.env.PRIVATEKEY_GEMINI;
const GEMINI_URL =
  "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent";

/* =========================================================================
   RAMA CARTA_VISUAL — el usuario quiere ver la carta / el menú
   Modelo: gemini-2.5-flash (REST, sin SDK)

   Flujo:
     1. Lee TODAS las cartas de:
        /Tiendas/barranca/barranca/TQmS5RKaSDdKmqPGMUXk/carta
     2. Un clasificador (Gemini, solo texto) elige UNA carta:
        - si el usuario pidió una específica ("la carta del chifa"), esa.
        - si no especificó ninguna, una al azar entre las disponibles.
        Gemini JAMÁS recibe las imágenes en este paso, solo los nombres.
     3. Un segundo llamado a Gemini redacta el mensaje de acompañamiento
        (tampoco ve las imágenes) y menciona qué otras cartas hay.
     4. Las imágenes de la carta elegida se adjuntan DESPUÉS, por código,
        no por la IA.

   Devuelve: { mensaje, extra, carta, imagenes }
     - imagenes: las URLs reales que hay que mandar por WhatsApp.
     - carta:    nombre limpio de la carta elegida (sin guiones bajos).
========================================================================= */

const RUTA_CARTA = [
  "Tiendas", "barranca",
  "barranca", "TQmS5RKaSDdKmqPGMUXk",
  "carta",
];

function cartaCollectionRef() {
  const [c1, d1, c2, d2, c3] = RUTA_CARTA;
  return db.collection(c1).doc(d1).collection(c2).doc(d2).collection(c3);
}

/** Limpia el nombre de una carta: quita guiones bajos y el prefijo "carta_" del id si hiciera falta. */
function nombreLimpio(doc) {
  const data = doc.data() || {};
  let nombre = data.nombre;

  if (!nombre) {
    // fallback: derivar el nombre desde el id del documento (ej. "carta_chifa")
    nombre = doc.id.replace(/^carta_?/i, "");
  }

  // por si el nombre trae guion bajo en vez de espacio (ej. "carta_chifa")
  nombre = String(nombre).replace(/_/g, " ").trim();
  // colapsa dobles espacios que puedan quedar
  nombre = nombre.replace(/\s+/g, " ");

  return nombre;
}

async function obtenerCartas() {
  const snap = await cartaCollectionRef().get();
  return snap.docs.map((d) => ({
    id: d.id,
    nombre: nombreLimpio(d),
    imagenes: d.data().imagenes || [],
  }));
}

/**
 * Clasificador: decide cuál carta mandar.
 * Si el usuario no pidió una en particular, Gemini elige una al azar
 * entre las disponibles (así igual queda un solo llamado, sin lógica random en código).
 */
async function elegirCarta(mensajeUsuario, cartas) {
  if (cartas.length === 1) return cartas[0];

  const listado = cartas.map((c) => `- ${c.id}: ${c.nombre}`).join("\n");

  const prompt = `El usuario escribió: "${mensajeUsuario}"

Cartas disponibles (id: nombre):
${listado}

Si el usuario menciona claramente una carta específica, elige su id.
Si NO menciona ninguna carta en particular, elige un id al azar de la lista (cualquiera).
Responde solo con el id exacto de una de la lista, nada más.`;

  const schema = {
    type: "object",
    properties: {
      carta_id: { type: "string", enum: cartas.map((c) => c.id) },
    },
    required: ["carta_id"],
  };

  try {
    const r = await fetch(`${GEMINI_URL}?key=${GEMINIKEY}`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({
        contents: [{ role: "user", parts: [{ text: prompt }] }],
        generationConfig: {
          responseMimeType: "application/json",
          responseSchema: schema,
        },
      }),
    });

    if (!r.ok) throw new Error(`Gemini (clasificador) respondió ${r.status}`);

    const data = await r.json();
    const texto = data?.candidates?.[0]?.content?.parts?.[0]?.text;
    if (!texto) throw new Error("Gemini (clasificador) no devolvió texto.");

    const parsed = JSON.parse(texto);
    const encontrada = cartas.find((c) => c.id === parsed.carta_id);
    return encontrada || cartas[Math.floor(Math.random() * cartas.length)];
  } catch (err) {
    console.error("[carta_visual] Error eligiendo carta, uso random:", err);
    return cartas[Math.floor(Math.random() * cartas.length)];
  }
}

const RESPONSE_SCHEMA = {
  type: "object",
  properties: {
    mensaje: { type: "string" },
    extra: { type: "string" },
  },
  required: ["mensaje", "extra"],
};

const SYSTEM_PROMPT = `Eres el asistente de un negocio por WhatsApp.
El usuario pidió ver la carta/el menú. Por fuera de tu respuesta (por código, no por ti) ya se le va a enviar la carta elegida junto con sus fotos — tú NUNCA ves esas imágenes, así que no las describas ni digas cómo se ven.

Tu única tarea es escribir un mensaje corto y natural de WhatsApp que acompañe el envío de esa carta, y si hay otras cartas disponibles, mencionarlas brevemente al final como alternativa (por su nombre, tal como te las den).

Reglas:
- No inventes platos, precios ni descripciones de la carta.
- Si tienes el nombre del usuario, puedes usarlo con naturalidad, sin abusar.
- Sé breve, como un mensaje real de WhatsApp.

Responde ÚNICAMENTE con un JSON:
- "mensaje": el texto para el usuario.
- "extra": resumen de MÁXIMO 6 palabras de lo que pasó en este turno (ej. "mostró carta chifa, ofreció otras cartas").`;

/**
 * Arma la respuesta completa de la rama carta_visual.
 *
 * @param {Object} params
 * @param {string} params.mensaje - Mensaje actual del usuario.
 * @param {string} [params.nombre_usuario]
 * @param {string} [params.extra_anterior]
 * @returns {Promise<{mensaje: string, extra: string, carta: string|null, imagenes: string[]}>}
 */
async function responderCartaVisual({ mensaje, nombre_usuario, extra_anterior }) {
  const cartas = await obtenerCartas();

  if (cartas.length === 0) {
    return {
      mensaje: "Por ahora no tengo cartas cargadas para mostrarte, pero cuéntame qué buscas y te ayudo igual.",
      extra: "sin cartas disponibles",
      carta: null,
      imagenes: [],
    };
  }

  const elegida = await elegirCarta(mensaje, cartas);
  const otras = cartas.filter((c) => c.id !== elegida.id).map((c) => c.nombre);

  const contexto = [
    nombre_usuario ? `nombre_usuario: ${nombre_usuario}` : null,
    extra_anterior ? `extra_anterior: ${extra_anterior}` : null,
    `carta_elegida: ${elegida.nombre}`,
    `otras_cartas_disponibles: ${otras.length ? otras.join(", ") : "ninguna"}`,
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
      mensaje: parsed.mensaje,
      extra: parsed.extra,
      carta: elegida.nombre,
      imagenes: elegida.imagenes,
    };
  } catch (err) {
    console.error("[carta_visual] Error generando respuesta:", err);
    return {
      mensaje: `Aquí tienes la ${elegida.nombre}. Avísame si quieres ver otra.`,
      extra: `envié carta ${elegida.nombre}`.slice(0, 60),
      carta: elegida.nombre,
      imagenes: elegida.imagenes,
    };
  }
}

/* =========================================================================
   ENDPOINT HTTP — solo para pruebas manuales. El envío real por WhatsApp
   se hace desde donde estamos armando el flujo (index / dispensador),
   igual que con la rama general.
========================================================================= */
exports.cartaVisual = onRequest(async (req, res) => {
  try {
    const { numero_usuario, nombre_usuario, mensaje, extra_anterior } =
      req.body || {};

    if (!numero_usuario || !mensaje) {
      return res
        .status(400)
        .json({ error: "Faltan numero_usuario o mensaje." });
    }

    const respuesta = await responderCartaVisual({
      mensaje,
      nombre_usuario,
      extra_anterior,
    });

    return res.status(200).json(respuesta);
  } catch (err) {
    console.error("[carta_visual] Error en el endpoint:", err);
    return res
      .status(500)
      .json({ error: "Error interno de la rama carta_visual." });
  }
});

module.exports.responderCartaVisual = responderCartaVisual;
module.exports.obtenerCartas = obtenerCartas;