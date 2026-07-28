const { onRequest } = require("firebase-functions/v2/https");
const admin = require("firebase-admin");
const algoliasearch = require("algoliasearch");
const OpenAI = require("openai");

// 👇 ÚNICO import de envío que se permite en este archivo: la función
// que decide WhatsApp/Telegram y con/sin ubicación. Ya NO hay fetch
// directo a Meta ni a Telegram aquí — todo vive en envios.js.
const { enviarRespuestaEmergencia } = require("./envios_mensajes_whatsapp_telegram.js");

if (!admin.apps.length) {
  admin.initializeApp();
}

const openai = new OpenAI({
  apiKey: process.env.API_KEYO_OPEN_IA,
});

const APP_ID = process.env.ALGOLIA_APP_ID || "";
const API_KEY = process.env.ALGOLIA_API_KEY || "";
const client = algoliasearch(APP_ID, API_KEY);
const index = client.initIndex("lugares");

const GEMINIKEY = process.env.PRIVATEKEY_GEMINI;
const GEMINI_URL =
  "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent";

const HITS_PER_PAGE_EMERGENCIA_AMPLIO = 60;

// ============================================================================
// CLASIFICACIÓN SALUD / SEGURIDAD (sin cambios)
// ============================================================================

function construirPromptEmergencia(mensaje) {
  return `Clasifica el mensaje en una sola palabra: SALUD o SEGURIDAD.

SALUD: hospital, clínica, ambulancia, accidente, herido, enfermedad, dolor, sangre, parto, desmayo, intoxicación, farmacia, emergencia médica.
SEGURIDAD: robo, asalto, delincuencia, policía, comisaría, bomberos, incendio, disparo, pelea, amenaza, extorsión, serenazgo, ladrón.

Si el mensaje menciona directamente una palabra de la lista SALUD (por ejemplo "hospital"), responde SALUD sin dudar, sin importar el resto del contexto.
Si el mensaje menciona directamente una palabra de la lista SEGURIDAD, responde SEGURIDAD sin dudar.

Responde ÚNICAMENTE con esa palabra, sin explicaciones, sin puntos, sin comillas, sin markdown.

MENSAJE: "${mensaje}"`;
}

function construirSystemPromptSelector(entidadesLigeras, nombreUsuario) {
  return `Eres el selector de contactos de Geinz. Tu única función es encontrar la entidad que el usuario solicita o la que mejor pueda ayudarlo.

ENTIDADES DISPONIBLES (ID y Nombre):

${JSON.stringify(entidadesLigeras)}

INSTRUCCIONES DE SELECCIÓN:
1. Si el usuario menciona un nombre que está en la Data (ej: "Divpol", "Serenazgo", "Hospital"), selecciona ESE ID sin dudar.
2. Si el usuario describe una situación (ej: "me robaron"), selecciona la entidad de seguridad más cercana.
3. Bajo ninguna circunstancia respondas con un ID vacío si hay datos disponibles.

REGLAS DE RESPUESTA:
- NUNCA DECIR "Te conectaremos con ellos"
- Tono: Calmado y directo para ${nombreUsuario}.
- Longitud: Máximo 2 líneas.
- Formato: JSON ESTRICTO.
- NUNCA digas frases como "mensaje predeterminado", "mensaje genérico", "esto es automático" ni nada que describa la naturaleza de tu propia respuesta


{
  "id": "ID_DEL_CONTACTO_ELEGIDO siempre de la lista de entidades sin inventar ni acrotar nada",
  "mensaje": "Texto de apoyo con el nombre de la entidad seleccionada y el nombre",
  "intencion": "EMERGENCIA",
  "estado": "AYUDA_EMERGENCIA"
}`;
}

function construirEntidadesLigeras(data) {
  return data.map((d) => ({ id: d.id, n: d.n }));
}

function validarClasificacion(textoRaw) {
  const limpio = (textoRaw || "")
    .toUpperCase()
    .normalize("NFD")
    .replace(/[\u0300-\u036f]/g, "")
    .trim();

  if (limpio.includes("SALUD")) return "salud";
  if (limpio.includes("SEGURIDAD")) return "seguridad";

  return null;
}

async function clasificarMensaje(mensaje, intento = 1) {
  const MAX_INTENTOS = 2;

  console.log(
    `🟡 [clasificarMensaje] INICIO (intento ${intento}) | mensaje:`,
    mensaje,
  );

  const t0 = Date.now();
  const promptEmergencia = construirPromptEmergencia(mensaje);

  const completion = await openai.chat.completions.create({
    model: "gpt-5.4-nano",
    messages: [{ role: "user", content: promptEmergencia }],
    max_completion_tokens: 10,
  });
  const tiempoMs = Date.now() - t0;

  const rawTexto = completion.choices[0]?.message?.content || "";
  console.log("🧠 RAW IA:", rawTexto);

  const categoriaValidada = validarClasificacion(rawTexto);

  const usage = {
    tiempo_ms: tiempoMs,
    prompt_tokens: completion.usage?.prompt_tokens ?? null,
    completion_tokens: completion.usage?.completion_tokens ?? null,
    total_tokens: completion.usage?.total_tokens ?? null,
  };

  console.log("⏱️ OpenAI:", usage);

  if (categoriaValidada === null && intento < MAX_INTENTOS) {
    console.log(
      `⚠️ [clasificarMensaje] Respuesta no reconocida ("${rawTexto}"), reintentando...`,
    );
    const reintento = await clasificarMensaje(mensaje, intento + 1);
    return {
      categoria: reintento.categoria,
      usage: {
        tiempo_ms: usage.tiempo_ms + reintento.usage.tiempo_ms,
        prompt_tokens:
          (usage.prompt_tokens || 0) + (reintento.usage.prompt_tokens || 0),
        completion_tokens:
          (usage.completion_tokens || 0) +
          (reintento.usage.completion_tokens || 0),
        total_tokens:
          (usage.total_tokens || 0) + (reintento.usage.total_tokens || 0),
        intentos: (reintento.usage.intentos || 1) + 1,
      },
    };
  }

  const categoria = categoriaValidada || "general";

  console.log(
    "🚨 CLASIFICACION EMERGENCIA:",
    categoria,
    "| intentos usados:",
    intento,
  );

  return { categoria, usage: { ...usage, intentos: intento } };
}

async function buscarLugaresAmplio(localidad) {
  const t0 = Date.now();

  let filtersArray = [];
  if (localidad) filtersArray.push(`lugar:"${localidad}"`);

  const filters =
    filtersArray.length > 0 ? filtersArray.join(" AND ") : undefined;

  const result = await index.search("", {
    filters,
    hitsPerPage: HITS_PER_PAGE_EMERGENCIA_AMPLIO,
  });

  const tiempoMs = Date.now() - t0;
  console.log(
    `✅ [buscarLugaresAmplio] ${result.hits.length} hits amplios | tiempo_ms:`,
    tiempoMs,
  );

  return { hits: result.hits, usage: { tiempo_ms: tiempoMs } };
}

async function buscarLugares(localidad, categoria, preHits) {
  const t0 = Date.now();

  let hitsCrudos;

  if (Array.isArray(preHits)) {
    const categoriaLimpia =
      categoria && categoria !== "general"
        ? categoria.toLowerCase().trim()
        : null;

    hitsCrudos = categoriaLimpia
      ? preHits.filter(
          (h) => (h.categoria || "").toLowerCase().trim() === categoriaLimpia,
        )
      : preHits;
  } else {
    let filtersArray = [];
    if (localidad) filtersArray.push(`lugar:"${localidad}"`);
    if (categoria && categoria !== "general")
      filtersArray.push(`categoria:"${categoria}"`);

    const filters =
      filtersArray.length > 0 ? filtersArray.join(" AND ") : undefined;

    const result = await index.search("", { filters, hitsPerPage: 20 });
    hitsCrudos = result.hits;
  }

  const data = hitsCrudos.map((d) => {
    let ubicacion = null;
    if (
      d.ubicacion &&
      d.ubicacion.latitud != null &&
      d.ubicacion.longitud != null
    ) {
      ubicacion = { lat: d.ubicacion.latitud, lng: d.ubicacion.longitud };
    }

    return {
      id: d.id ?? d.objectID,
      c: d.categoria ?? null,
      n: d.nombre ?? null,
      num: {
        llamada: d.llamada ? [d.llamada] : [],
        whatsapp: d.whatsapp ? [d.whatsapp] : [],
      },
      dir: d.dir ?? null,
      ref: d.ref ?? null,
      ...(ubicacion && { ub: ubicacion }),
    };
  });

  const tiempoMs = Date.now() - t0;
  return { data, usage: { tiempo_ms: tiempoMs } };
}

async function seleccionarContacto(entidadesLigeras, mensajeUsuario, nombreUsuario) {
  const t0 = Date.now();
  const systemMessage = construirSystemPromptSelector(
    entidadesLigeras,
    nombreUsuario,
  );

  const body = {
    contents: [{ role: "user", parts: [{ text: mensajeUsuario }] }],
    systemInstruction: { parts: [{ text: systemMessage }] },
    generationConfig: {
      temperature: 0.2,
      maxOutputTokens: 200,
      thinkingConfig: { thinkingBudget: 0 },
    },
  };

  const resp = await fetch(`${GEMINI_URL}?key=${GEMINIKEY}`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(body),
  });

  const json = await resp.json();
  const tiempoMs = Date.now() - t0;

  const rawText = json.candidates?.[0]?.content?.parts?.[0]?.text || "";

  let outputIA;
  try {
    outputIA = JSON.parse(rawText.replace(/```json|```/g, "").trim());
  } catch (e) {
    throw new Error("No se pudo parsear la respuesta de Gemini: " + rawText);
  }

  const usage = {
    tiempo_ms: tiempoMs,
    prompt_tokens: json.usageMetadata?.promptTokenCount ?? null,
    completion_tokens: json.usageMetadata?.candidatesTokenCount ?? null,
    total_tokens: json.usageMetadata?.totalTokenCount ?? null,
  };

  return { outputIA, usage };
}

function construirRespuestaFinal(outputIA, data) {
  const contacto = data.find((c) => c.id === outputIA.id);

  if (!contacto) {
    return { error: "No se encontró la entidad", tiene_link: false };
  }

  const listaLlamadas = contacto.num?.llamada || [];
  const listaWhatsapp = contacto.num?.whatsapp?.[0] ? contacto.num.whatsapp : [];

  const bloqueContactos =
    listaLlamadas.length && listaWhatsapp.length
      ? `📞 Contáctalos al: ${listaLlamadas.join(" - ")} o 💬 Escríbeles al: ${listaWhatsapp.join(" - ")}`
      : listaLlamadas.length
        ? `📞 Contáctalos al: ${listaLlamadas.join(" - ")}`
        : listaWhatsapp.length
          ? `💬 Escríbeles al: ${listaWhatsapp.join(" - ")}`
          : "";

  const lat = contacto.ub?.lat || null;
  const lng = contacto.ub?.lng || null;
  const tiene_link = !!(lat && lng);
  const direccion = contacto.dir || "";
  const referencia = contacto.ref || "";
  const mensajeBase = String(outputIA.mensaje).trim();

  const base = {
    id: outputIA.id,
    intencion: outputIA.intencion,
    estado: outputIA.estado,
    tiene_link,
    telefonos: listaLlamadas,
    whatsapp: listaWhatsapp,
  };

  return tiene_link
    ? {
        ...base,
        mensaje_texto:
          `${mensajeBase} ubicalos en *${direccion}* ,con referencia *${referencia ? `(${referencia})* , ` : ""}`.trim(),
        lat,
        lng,
      }
    : {
        ...base,
        mensaje_safe:
          `${mensajeBase} ${bloqueContactos} 🏠 ${direccion} ${referencia ? `💡 ${referencia}` : ""} ✅`.trim(),
      };
}

// ============================================================
// NÚCLEO — calcula el resultado de la emergencia. NO MANDA NADA.
// ============================================================
async function resolverEmergencia({ localidad, mensaje, nombreUsuario }) {
  const tInicio = Date.now();

  // Clasificador IA + búsqueda amplia en Algolia corren en paralelo
  // (la búsqueda amplia solo depende de "localidad", no de la
  // clasificación, así que no hay nada adelantado/inventado).
  const [
    { categoria, usage: usageOpenAI },
    { hits: hitsAmplios, usage: usageAlgoliaAmplio },
  ] = await Promise.all([
    clasificarMensaje(mensaje),
    buscarLugaresAmplio(localidad),
  ]);

  const { data, usage: usageAlgoliaFiltrado } = await buscarLugares(
    localidad,
    categoria,
    hitsAmplios,
  );

  const entidadesLigeras = construirEntidadesLigeras(data);
  const { outputIA, usage: usageGemini } = await seleccionarContacto(
    entidadesLigeras,
    mensaje,
    nombreUsuario || "usuario",
  );

  const resultado = construirRespuestaFinal(outputIA, data);

  const debugInfo = {
    tiempo_total_ms: Date.now() - tInicio,
    categoria_detectada: categoria,
    openai: usageOpenAI,
    algolia: usageAlgoliaAmplio,
    algolia_filtrado_local: usageAlgoliaFiltrado,
    gemini: usageGemini,
  };

  return { resultado, debugInfo };
}

// ============================================================
// procesarEmergencia — usada por el dispensador general
// (geinz_dispatcher.js). YA NO ENVÍA NADA: solo calcula y devuelve
// el resultado + info de debug. El dispensador es quien decide,
// según el canal (whatsapp/telegram) de la conversación, llamar a
// enviarRespuestaEmergencia (importada de envios.js) para mandar el
// mensaje o la plantilla de verdad.
// ============================================================
async function procesarEmergencia({ localidad, mensaje, nombreUsuario }) {
  console.log(
    "🚀 [procesarEmergencia] INICIO (solo cálculo, sin envío) | mensaje:",
    mensaje,
  );

  const { resultado, debugInfo } = await resolverEmergencia({
    localidad,
    mensaje,
    nombreUsuario,
  });

  if (resultado.error) {
    console.log(
      "⚠️ [procesarEmergencia] No se pudo armar un resultado enviable:",
      resultado.error,
    );
  }

  console.log("📊 [procesarEmergencia] RESUMEN:", JSON.stringify(debugInfo));

  return { ...resultado, _debug: debugInfo };
}

// ============================================================
// Endpoint HTTP directo (uso manual / integraciones externas que no
// pasan por geinz_webhook_principal, geinz_procesar_buffer o
// geinz_webhook_telegram). Al ser su propia puerta de entrada HTTP,
// aquí sí se dispara el envío — pero SIEMPRE a través de la misma
// función compartida enviarRespuestaEmergencia de envios.js. No hay
// ningún fetch directo a Meta/Telegram en este archivo.
// ============================================================
exports.obtener_lugares_emergencia_Actualizado = onRequest(async (req, res) => {
  const tInicio = Date.now();
  try {
    const { localidad, mensaje, nombreUsuario, numero_usuario, canal } = req.body;

    if (!mensaje || typeof mensaje !== "string" || !mensaje.trim()) {
      return res
        .status(400)
        .json({ ok: false, error: "El campo 'mensaje' es requerido" });
    }

    if (!numero_usuario) {
      return res.status(400).json({
        ok: false,
        error:
          "El campo 'numero_usuario' es requerido para enviar la respuesta",
      });
    }

    const { resultado, debugInfo } = await resolverEmergencia({
      localidad,
      mensaje,
      nombreUsuario,
    });

    if (!resultado.error) {
      await enviarRespuestaEmergencia(
        numero_usuario,
        resultado,
        canal || "whatsapp",
      );
    } else {
      console.log(
        "⚠️ No se envía el mensaje porque hubo error armando el resultado:",
        resultado.error,
      );
    }

    res.set("Cache-Control", "public, max-age=300");
    return res.status(200).json({
      ...resultado,
      _debug: { ...debugInfo, canal: canal || "whatsapp", tiempo_total_ms: Date.now() - tInicio },
    });
  } catch (error) {
    console.error("❌ ERROR obtener_lugares_emergencia:", error.message);
    return res
      .status(500)
      .json({ ok: false, mensaje: "Error interno al buscar lugares" });
  }
});

exports.procesarEmergencia = procesarEmergencia;