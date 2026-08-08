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

// Ruta de ejemplo que compartiste, usada solo como default si no llega
// "ruta_negocio" en el body (útil para pruebas manuales del endpoint).
// DESPUÉS
const paths = require("../rutas_geinz_firebase/rutas.js"); // 👈 agregar el import arriba del archivo

const RUTA_NEGOCIO_DEFAULT = paths.tiendaPathStr("barranca", "tiendas", "TQmS5RKaSDdKmqPGMUXk");
/* =========================================================================
   RAMA NEGOCIO — info del negocio (horario, dirección, descripción, etc.)
   Modelo: gemini-2.5-flash (una sola llamada, contexto mínimo)

   👇 TOKENS: este archivo ya loggeaba los tokens de Gemini y los devolvía
   en un campo suelto "tokens" (prompt_tokens/respuesta_tokens/total_tokens
   o null si fallaba). Ahora se estandariza al mismo formato que usan todas
   las ramas — {detalle: [...], total: number} — usando token_utils.js, así
   el dispensador/webhook puede sumarlo con el resto sin casos especiales.

   👇 FIX (este archivo): el schema real de Firestore para un negocio es
   distinto al que este archivo asumía originalmente. Ajustado para leer:

     - horario_atencion.{dia}.bloques[]: cada bloque trae h_apertura,
       h_cierre y cerrado (por bloque, no por día). ANTES se leía
       horario_atencion.{dia}.apertura / .cierre directo, que no existe
       en los datos reales.
     - Además, cada día puede estar guardado con Y sin tilde a la vez
       (ej. "miercoles" con un objeto vacío/corrupto y "miércoles" con las
       horas reales; mismo caso con "sabado"/"sábado"). Como
       Intl.DateTimeFormat siempre devuelve el nombre CON tilde, si el
       negocio quedó guardado solo con la variante sin tilde (o con ambas y
       la de Gemini cae en la vacía) el bot leía mal el horario. Ahora se
       prueban ambas variantes y se usa la que sí tenga bloques con horas
       válidas.
     - imagen_bot -> está anidado en img_tienda.imagen_bot, NO en la raíz
       del documento.
     - direccion / referencia -> están anidados en ubicacion.dirección
       (con tilde) y ubicacion.referencia, NO en la raíz del documento.

   Documento en Firestore trae: alias_key, descripcion_seo, horario_atencion,
   img_tienda.imagen_bot, nombre_tienda, ubicacion.dirección, ubicacion.referencia.

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

   👇 LOGS: logging detallado en cada paso (igual que busqueda_algolia.js)
   para poder ver en Cloud Logging qué está pasando: caché usado o no,
   horario calculado, contexto armado, llamada a Gemini, tokens gastados,
   y resultado final.

   Devuelve: { mensaje, extra, imagen_bot, horario, tokens }
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

// 👇 Días cuyo nombre lleva tilde en español y que en Firestore pueden
// haber quedado guardados en cualquiera de las dos variantes (o en ambas,
// con una de ellas corrupta/vacía). Se prueban en este orden: primero la
// que tenga horas válidas, y si ninguna las tiene, la que exista.
const VARIANTES_DIA = {
  miercoles: ["miércoles", "miercoles"],
  sabado: ["sábado", "sabado"],
};

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
 * Busca el objeto de horario del día actual dentro de horario_atencion,
 * probando ambas variantes (con/sin tilde) cuando el día las tiene, y
 * quedándose con la que sí tenga al menos un bloque con h_apertura/h_cierre
 * válidos. Así no importa si el negocio quedó guardado como "miercoles" o
 * "miércoles" (o ambos) en Firestore.
 */
function obtenerHorarioDelDia(horarioAtencion, diaSemana) {
  const candidatos = VARIANTES_DIA[diaSemana] || [diaSemana];

  let mejorCandidato = null;
  for (const clave of candidatos) {
    const candidato = horarioAtencion?.[clave];
    if (!candidato) continue;

    const bloques = Array.isArray(candidato.bloques) ? candidato.bloques : [];
    const tieneHorasValidas = bloques.some(
      (b) => b && b.cerrado !== true && b.h_apertura && b.h_cierre,
    );

    if (tieneHorasValidas) {
      console.log(`[negocio] obtenerHorarioDelDia | usando clave "${clave}" (tiene horas válidas)`);
      return candidato;
    }
    if (!mejorCandidato) mejorCandidato = candidato;
  }

  if (mejorCandidato) {
    console.log("[negocio] obtenerHorarioDelDia | ninguna variante tenía horas válidas, usando la primera que existe (probablemente día cerrado real)");
  } else {
    console.log("[negocio] obtenerHorarioDelDia | no se encontró ninguna variante para el día:", diaSemana);
  }
  return mejorCandidato;
}

/**
 * Calcula si el negocio está abierto AHORA, usando los bloques del día de
 * hoy. Soporta horarios que cruzan medianoche (ej. 18:00 - 02:00) y varios
 * bloques por día (ej. horario partido mañana/tarde).
 *
 * @param {Object} horarioAtencion - Mapa por día, ej.
 *   { lunes: { bloques: [{h_apertura, h_cierre, cerrado, motivo}] }, ... }
 * @returns {{apertura: string|null, cierre: string|null, abierto: boolean, diaSemana: string}}
 */
function calcularEstadoHorario(horarioAtencion) {
  const { diaSemana, minutosActuales } = obtenerFechaLima();
  const horarioHoy = obtenerHorarioDelDia(horarioAtencion, diaSemana);

  console.log(
    "[negocio] → calcularEstadoHorario | diaSemana:", diaSemana,
    "| minutosActuales:", minutosActuales,
    "| horarioHoy:", JSON.stringify(horarioHoy),
  );

  const bloques = Array.isArray(horarioHoy?.bloques) ? horarioHoy.bloques : [];
  const bloquesValidos = bloques.filter(
    (b) => b && b.cerrado !== true && aMinutos(b.h_apertura) !== null && aMinutos(b.h_cierre) !== null,
  );

  if (!horarioHoy || bloquesValidos.length === 0) {
    console.log("[negocio] ← calcularEstadoHorario | sin bloques válidos hoy, abierto: false");
    return { apertura: null, cierre: null, abierto: false, diaSemana };
  }

  // Se recorren todos los bloques del día (por si hay horario partido) y
  // se usa el bloque en el que caiga la hora actual. Si ninguno está
  // activo ahora mismo, se muestra el primer bloque solo para el texto de
  // apertura/cierre (ej. "hoy abrimos de 12:00 a 23:00", aunque ahora esté
  // cerrado por estar fuera de ese rango).
  let abiertoAhora = false;
  let bloqueParaMostrar = bloquesValidos[0];

  for (const b of bloquesValidos) {
    const minApertura = aMinutos(b.h_apertura);
    const minCierre = aMinutos(b.h_cierre);

    let dentroDelRango;
    if (minCierre > minApertura) {
      dentroDelRango = minutosActuales >= minApertura && minutosActuales < minCierre;
    } else {
      // Horario que cruza medianoche (ej. 18:00 - 02:00).
      dentroDelRango = minutosActuales >= minApertura || minutosActuales < minCierre;
    }

    if (dentroDelRango) {
      abiertoAhora = true;
      bloqueParaMostrar = b;
      break;
    }
  }

  const apertura = bloqueParaMostrar.h_apertura;
  const cierre = bloqueParaMostrar.h_cierre;

  console.log(
    "[negocio] ← calcularEstadoHorario | apertura:", apertura,
    "| cierre:", cierre,
    "| abierto:", abiertoAhora,
  );

  return { apertura, cierre, abierto: abiertoAhora, diaSemana };
}

/** Cache en memoria muy simple para no golpear Firestore en cada mensaje. */
const CACHE_TTL_MS = 5 * 60 * 1000; // 5 minutos
const cacheNegocio = new Map(); // ruta -> { data, expira }

async function obtenerDatosNegocio(rutaNegocio) {
  const cacheado = cacheNegocio.get(rutaNegocio);
  if (cacheado && cacheado.expira > Date.now()) {
    console.log(
      "[negocio] obtenerDatosNegocio | usando CACHÉ | ruta:", rutaNegocio,
      "| expira en (ms):", cacheado.expira - Date.now(),
    );
    return cacheado.data;
  }

  console.log("[negocio] obtenerDatosNegocio | cache MISS, leyendo Firestore | ruta:", rutaNegocio);
  const snap = await db.doc(rutaNegocio).get();
  if (!snap.exists) {
    console.error("[negocio] ❌ obtenerDatosNegocio | documento no existe en:", rutaNegocio);
    throw new Error(`No existe el documento del negocio en: ${rutaNegocio}`);
  }

  const data = snap.data();
  cacheNegocio.set(rutaNegocio, { data, expira: Date.now() + CACHE_TTL_MS });
  console.log("[negocio] obtenerDatosNegocio | Firestore OK, guardado en caché por", CACHE_TTL_MS, "ms");
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
 * @returns {Promise<{mensaje: string, extra: string, imagen_bot: string|null, horario: Object, tokens: {detalle: Array, total: number}}>}
 */
async function responderNegocio({
  mensaje,
  nombre_usuario,
  extra_anterior,
  ruta_negocio = RUTA_NEGOCIO_DEFAULT,
}) {
  console.log(
    "[negocio] → responderNegocio | mensaje:", mensaje,
    "| nombre_usuario:", nombre_usuario,
    "| extra_anterior:", extra_anterior,
    "| ruta_negocio:", ruta_negocio,
  );

  const datos = await obtenerDatosNegocio(ruta_negocio);

  // alias_key jamás sale de esta función.
  const { descripcion_seo, horario_atencion, nombre_tienda } = datos;

  // 👇 FIX: estos 3 campos NO están en la raíz del documento, están
  // anidados. imagen_bot vive en img_tienda.imagen_bot; dirección y
  // referencia viven en ubicacion.dirección (con tilde) / ubicacion.referencia.
  // Se prueban ambas variantes de "direccion" (con/sin tilde) por seguridad,
  // igual que se hizo con los días de la semana.
  const imagen_bot = datos.img_tienda?.imagen_bot || null;
  const direccion =
    datos.ubicacion?.["dirección"] ?? datos.ubicacion?.direccion ?? null;
  const referencia = datos.ubicacion?.referencia ?? null;

  console.log(
    "[negocio] Datos del negocio cargados | nombre_tienda:", nombre_tienda,
    "| tiene_descripcion:", !!descripcion_seo,
    "| tiene_imagen_bot:", !!imagen_bot,
    "| tiene_direccion:", !!direccion,
    "| tiene_referencia:", !!referencia,
  );

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

  console.log("[negocio] Contexto armado para Gemini:\n", contexto);

  const promptUsuario = `Contexto:\n${contexto}\n\nMensaje del usuario:\n${mensaje}`;

  try {
    console.log("[negocio] Llamando a Gemini (gemini-2.5-flash) para redactar respuesta...");
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
      console.error("[negocio] ❌ Gemini respondió con error HTTP:", r.status, "| body:", body);
      throw new Error(`Gemini respondió ${r.status}: ${body}`);
    }

    const data = await r.json();

    // 👇 TOKENS: se estandariza al formato común {detalle, total}.
    const usageMeta = data?.usageMetadata;
    const tokens = armarTokens([tokenGemini(usageMeta, "gemini-2.5-flash")]);
    console.log(
      "[negocio] Tokens usados en Gemini (gemini-2.5-flash):",
      "prompt_tokens:", usageMeta?.promptTokenCount ?? null,
      "| respuesta_tokens:", usageMeta?.candidatesTokenCount ?? null,
      "| total_tokens:", usageMeta?.totalTokenCount ?? null,
    );

    const texto = data?.candidates?.[0]?.content?.parts?.[0]?.text;
    if (!texto) {
      console.error("[negocio] ❌ Gemini no devolvió texto. Respuesta cruda:", JSON.stringify(data));
      throw new Error("Gemini no devolvió texto en la respuesta.");
    }

    console.log("[negocio] Respuesta cruda de Gemini:", texto);

    const parsed = JSON.parse(texto);
    if (!parsed.mensaje || !parsed.extra) {
      console.error("[negocio] ❌ Respuesta de Gemini incompleta:", JSON.stringify(parsed));
      throw new Error("Respuesta de Gemini incompleta.");
    }

    console.log(
      "[negocio] ✅ Respuesta final | extra:", parsed.extra,
      "| abierto:", estadoHorario.abierto,
      "| total_tokens:", tokens.total,
    );

    return {
      mensaje: parsed.mensaje + construirLineaHorario(estadoHorario),
      extra: parsed.extra,
      imagen_bot: imagen_bot || null,
      horario: estadoHorario,
      tokens,
    };
  } catch (err) {
    console.error("[negocio] ❌ Error generando respuesta:", err.message, "| stack:", err.stack);
    return {
      mensaje:
        (nombre_tienda ? `${nombre_tienda}: ` : "") +
        "tuve un problema para redactar la respuesta, pero aquí tienes el horario." +
        construirLineaHorario(estadoHorario),
      extra: "error generando respuesta negocio",
      imagen_bot: imagen_bot || null,
      horario: estadoHorario,
      // No se pudo confirmar cuánto (o si algo) se gastó en Gemini antes
      // de fallar, así que se deja explícito en 0 en vez de omitirlo, para
      // que sumar tokens con otras ramas nunca rompa nada.
      tokens: armarTokens([]),
    };
  }
}

/* =========================================================================
   ENDPOINT HTTP — solo para pruebas manuales, igual que las otras ramas.
========================================================================= */
exports.negocio = onRequest(async (req, res) => {
  console.log("[negocio] === Nueva petición HTTP a /negocio ===");
  try {
    const { numero_usuario, nombre_usuario, mensaje, extra_anterior, ruta_negocio } =
      req.body || {};

    if (!numero_usuario || !mensaje) {
      console.warn("[negocio] Faltan numero_usuario o mensaje en el body:s", JSON.stringify(req.body));
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
    console.error("[negocio] ❌ Error en el endpoint:", err.message, "| stack:", err.stack);
    return res.status(500).json({ error: "Error interno de la rama negocio." });
  }
});

module.exports.responderNegocio = responderNegocio;
module.exports.calcularEstadoHorario = calcularEstadoHorario;