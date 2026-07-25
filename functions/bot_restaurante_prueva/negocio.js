const { onRequest } = require("firebase-functions/v2/https");
const admin = require("firebase-admin");

if (!admin.apps.length) {
  admin.initializeApp();
}

const db = admin.firestore();

const GEMINIKEY = process.env.PRIVATEKEY_GEMINI;
const GEMINI_URL =
  "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent";

// Ruta de ejemplo que compartiste, usada solo como default si no llega
// "ruta_negocio" en el body (útil para pruebas manuales del endpoint).
const RUTA_NEGOCIO_DEFAULT = "Tiendas/barranca/barranca/TQmS5RKaSDdKmqPGMUXk";

/* =========================================================================
   RAMA NEGOCIO — info del negocio (horario, dirección, descripción, etc.)
   Modelo: gemini-2.5-flash (una sola llamada, contexto mínimo)

   Documento en Firestore trae: alias_key, descripcion_seo, horario_atencion,
   imagen_bot, nombre_tienda, direccion, referencia.

   Reglas de qué SÍ y qué NO ve Gemini:
     - alias_key   -> NUNCA se manda a Gemini (uso interno).
     - imagen_bot  -> NUNCA se manda a Gemini (se devuelve aparte para WhatsApp).
     - direccion / referencia -> SÍ se incluyen en el contexto, pero el
       system prompt le ordena a Gemini mencionarlas solo si el usuario las
       pidió explícitamente (ubicación, cómo llegar, dirección, etc.).
     - horario (apertura, cierre, abierto/cerrado) -> se CALCULA por código
       con la hora actual de Lima, no lo decide Gemini. Se le pasa ya
       resuelto para que redacte de forma consistente, y además se arma un
       indicador 🟢/🔴 aparte, igual que el precio en busqueda_algolia.
     - descripcion_seo -> sí se manda, es el contexto de qué vende / métodos
       de pago / comodidades.

   Devuelve: { mensaje, extra, imagen_bot, horario }
========================================================================= */

const DIAS_SEMANA = [
  "domingo",
  "lunes",
  "martes",
  "miercoles",
  "jueves",
  "viernes",
  "sabado",
];

/**
 * Devuelve el día de la semana (en español, sin tildes) y la hora actual
 * en minutos desde medianoche, ambos en horario de Lima, Perú.
 */
function obtenerFechaLima() {
  const ahora = new Date();

  const partes = new Intl.DateTimeFormat("en-US", {
    timeZone: "America/Lima",
    weekday: "long",
    hour: "2-digit",
    minute: "2-digit",
    hourCycle: "h23",
  }).formatToParts(ahora);

  const mapa = {};
  partes.forEach((p) => (mapa[p.type] = p.value));

  const diasEnIngles = {
    Sunday: "domingo",
    Monday: "lunes",
    Tuesday: "martes",
    Wednesday: "miercoles",
    Thursday: "jueves",
    Friday: "viernes",
    Saturday: "sabado",
  };

  const diaSemana = diasEnIngles[mapa.weekday] || DIAS_SEMANA[ahora.getUTCDay()];
  const horaActual = `${mapa.hour}:${mapa.minute}`;
  const minutosActuales = parseInt(mapa.hour, 10) * 60 + parseInt(mapa.minute, 10);

  return { diaSemana, horaActual, minutosActuales };
}

/** Convierte "HH:mm" a minutos desde medianoche. Devuelve null si es inválido. */
function aMinutos(horaStr) {
  if (typeof horaStr !== "string" || !/^\d{1,2}:\d{2}$/.test(horaStr)) return null;
  const [h, m] = horaStr.split(":").map(Number);
  if (Number.isNaN(h) || Number.isNaN(m)) return null;
  return h * 60 + m;
}

/**
 * Calcula si el negocio está abierto AHORA, usando el horario de hoy.
 * Soporta horarios que cruzan medianoche (ej. 18:00 - 02:00).
 *
 * @param {Object} horarioAtencion - Mapa por día, ej. { lunes: {apertura, cierre}, ... }
 * @returns {{apertura: string|null, cierre: string|null, abierto: boolean, diaSemana: string}}
 */
function calcularEstadoHorario(horarioAtencion) {
  const { diaSemana, minutosActuales } = obtenerFechaLima();
  const horarioHoy = (horarioAtencion && horarioAtencion[diaSemana]) || null;

  if (!horarioHoy || horarioHoy.cerrado) {
    return { apertura: null, cierre: null, abierto: false, diaSemana };
  }

  const apertura = horarioHoy.apertura || null;
  const cierre = horarioHoy.cierre || null;
  const minApertura = aMinutos(apertura);
  const minCierre = aMinutos(cierre);

  if (minApertura === null || minCierre === null) {
    return { apertura, cierre, abierto: false, diaSemana };
  }

  let abierto;
  if (minCierre > minApertura) {
    // Horario normal dentro del mismo día.
    abierto = minutosActuales >= minApertura && minutosActuales < minCierre;
  } else {
    // Horario que cruza medianoche (ej. 18:00 - 02:00).
    abierto = minutosActuales >= minApertura || minutosActuales < minCierre;
  }

  return { apertura, cierre, abierto, diaSemana };
}

/** Cache en memoria muy simple para no golpear Firestore en cada mensaje. */
const CACHE_TTL_MS = 5 * 60 * 1000; // 5 minutos
const cacheNegocio = new Map(); // ruta -> { data, expira }

async function obtenerDatosNegocio(rutaNegocio) {
  const cacheado = cacheNegocio.get(rutaNegocio);
  if (cacheado && cacheado.expira > Date.now()) {
    return cacheado.data;
  }

  const snap = await db.doc(rutaNegocio).get();
  if (!snap.exists) {
    throw new Error(`No existe el documento del negocio en: ${rutaNegocio}`);
  }

  const data = snap.data();
  cacheNegocio.set(rutaNegocio, { data, expira: Date.now() + CACHE_TTL_MS });
  return data;
}

const RESPONSE_SCHEMA = {
  type: "object",
  properties: {
    mensaje: { type: "string" },
    extra: { type: "string" },
  },
  required: ["mensaje", "extra"],
};

const SYSTEM_PROMPT = `Eres el asistente de un negocio por WhatsApp. El usuario pregunta algo sobre el negocio (horario, ubicación, qué venden, promociones, quiénes son, etc.).

Te paso datos del negocio: nombre, descripción (qué vende, métodos de pago, comodidades), y su horario ya calculado para HOY (apertura, cierre, si está abierto ahora).

Reglas:
- El horario ya viene resuelto (apertura, cierre, abierto). No lo inventes ni lo recalcules, solo úsalo.
- NO menciones la dirección ni la referencia del local a menos que el usuario las haya pedido explícitamente (preguntó por ubicación, dirección, cómo llegar, dónde queda, etc.). Si no las pidió, ignóralas aunque las tengas en el contexto.
- Si tienes el nombre del usuario, puedes usarlo con naturalidad.
- Sé breve y cálido, como un mensaje real de WhatsApp. No agregues el bloque de horario formateado (apertura/cierre/🟢🔴), eso se agrega después por fuera de tu respuesta.

Responde ÚNICAMENTE con un JSON:
- "mensaje": el texto para el usuario.
- "extra": resumen de MÁXIMO 6 palabras de este turno (ej. "preguntó horario, le dije que abrimos").`;

/** Arma la línea de horario con indicador visual, por código (no por Gemini). */
function construirLineaHorario(estado) {
  if (!estado.apertura || !estado.cierre) {
    return "\n\n🔴 Hoy no atendemos.";
  }
  const punto = estado.abierto ? "🟢" : "🔴";
  const texto = estado.abierto ? "Abierto ahora" : "Cerrado ahora";
  return `\n\n${punto} ${texto} · Hoy: ${estado.apertura} - ${estado.cierre}`;
}

/**
 * Flujo completo de la rama negocio.
 *
 * @param {Object} params
 * @param {string} params.mensaje
 * @param {string} [params.nombre_usuario]
 * @param {string} [params.extra_anterior]
 * @param {string} [params.ruta_negocio] - Path del doc en Firestore.
 * @returns {Promise<{mensaje: string, extra: string, imagen_bot: string|null, horario: Object}>}
 */
async function responderNegocio({
  mensaje,
  nombre_usuario,
  extra_anterior,
  ruta_negocio = RUTA_NEGOCIO_DEFAULT,
}) {
  const datos = await obtenerDatosNegocio(ruta_negocio);

  // alias_key jamás sale de esta función.
  const {
    descripcion_seo,
    horario_atencion,
    imagen_bot,
    nombre_tienda,
    direccion,
    referencia,
  } = datos;

  const estadoHorario = calcularEstadoHorario(horario_atencion || {});

  const contexto = [
    nombre_usuario ? `nombre_usuario: ${nombre_usuario}` : null,
    extra_anterior ? `extra_anterior: ${extra_anterior}` : null,
    nombre_tienda ? `nombre_tienda: ${nombre_tienda}` : null,
    descripcion_seo ? `descripcion_negocio: ${descripcion_seo}` : null,
    `horario_hoy: apertura=${estadoHorario.apertura ?? "N/A"}, cierre=${
      estadoHorario.cierre ?? "N/A"
    }, abierto=${estadoHorario.abierto}`,
    direccion ? `direccion: ${direccion}` : null,
    referencia ? `referencia: ${referencia}` : null,
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
      mensaje: parsed.mensaje + construirLineaHorario(estadoHorario),
      extra: parsed.extra,
      imagen_bot: imagen_bot || null,
      horario: estadoHorario,
    };
  } catch (err) {
    console.error("[negocio] Error generando respuesta:", err);
    return {
      mensaje:
        (nombre_tienda ? `${nombre_tienda}: ` : "") +
        "tuve un problema para redactar la respuesta, pero aquí tienes el horario." +
        construirLineaHorario(estadoHorario),
      extra: "error generando respuesta negocio",
      imagen_bot: imagen_bot || null,
      horario: estadoHorario,
    };
  }
}

/* =========================================================================
   ENDPOINT HTTP — solo para pruebas manuales, igual que las otras ramas.
========================================================================= */
exports.negocio = onRequest(async (req, res) => {
  try {
    const { numero_usuario, nombre_usuario, mensaje, extra_anterior, ruta_negocio } =
      req.body || {};

    if (!numero_usuario || !mensaje) {
      return res
        .status(400)
        .json({ error: "Faltan numero_usuario o mensaje." });
    }

    const respuesta = await responderNegocio({
      mensaje,
      nombre_usuario,
      extra_anterior,
      ruta_negocio,
    });

    return res.status(200).json(respuesta);
  } catch (err) {
    console.error("[negocio] Error en el endpoint:", err);
    return res.status(500).json({ error: "Error interno de la rama negocio." });
  }
});

module.exports.responderNegocio = responderNegocio;
module.exports.calcularEstadoHorario = calcularEstadoHorario;