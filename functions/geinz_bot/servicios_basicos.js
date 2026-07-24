const algoliasearch = require("algoliasearch");
const similarity = require("string-similarity-js");
const OpenAI = require("openai");

const APP_ID = process.env.ALGOLIA_APP_ID || "";
const API_KEY = process.env.ALGOLIA_API_KEY || "";
const client = algoliasearch(APP_ID, API_KEY);

const openai = new OpenAI({
  apiKey: process.env.API_KEYO_OPEN_IA,
});

const GEMINIKEY = process.env.PRIVATEKEY_GEMINI;
const GEMINI_URL =
  "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent";

const CACHE_TTL_MS = 1000 * 60 * 30;
const index_servicios_basicos = client.initIndex("servicios_basicos_generales");

const ATTRS_SERVICIOS_BASICOS = [
  "objectID",
  "id",
  "nombre",
  "descripcion",
  "telefono",
  "img_logo",
  "alias",
];

const CTAS_SERVICIOS_BASICOS = [
  "Toda esa info la tienes completa aquí en geinz",
  "Ahí te dejo todo lo que necesitas",
  "Mira, ahí está todo detallado",
  "Encuentra eso y más por acá",
  "Échale un vistazo aquí, está todo",
];

const MENSAJES_SIN_RESULTADO_SERVICIOS = [
  "Uy, no tengo registrado ese servicio por aquí 😕",
  "No encontré nada con eso por ahora 👀.",
  "No me sale ese servicio en la lista 😅.",
  "Por ahora no tengo ese dato registrado 🤔.",
];

const UMBRAL_MATCH_LOCAL_SERVICIOS = 0.72;

const CAMPOS_MAP_SERVICIOS = {
  t: { label: "📞", campo: "telefono" },
  w: { label: "💬 WhatsApp:", campo: "whatsapp" },
  s: { label: "🌐", campo: "sitio_web" },
  fb: { label: "📘 Facebook:", campo: "fb" },
  ig: { label: "📸 Instagram:", campo: "ig" },
};

let serviciosBasicosCache = null;
let serviciosBasicosCacheTimestamp = 0;

async function buscarServiciosBasicosAmplio({ localidad } = {}) {
  const ahora = Date.now();
  if (
    serviciosBasicosCache &&
    ahora - serviciosBasicosCacheTimestamp < CACHE_TTL_MS
  ) {
    console.log("♻️ Usando servicios básicos desde cache");
    return serviciosBasicosCache;
  }

  console.log("🔄 Refrescando servicios básicos desde Algolia");

  const filters = [];
  // if (localidad) filters.push(`lugar:"${localidad.toLowerCase().trim()}"`);

  const { hits } = await index_servicios_basicos.search("", {
    filters: filters.length ? filters.join(" AND ") : undefined,
    hitsPerPage: 200,
    attributesToRetrieve: ATTRS_SERVICIOS_BASICOS,
  });

  console.log(
    `✅ [buscar_servicios_basicos_amplio] ${hits.length} servicios traídos (fresh)`,
  );

  serviciosBasicosCache = hits;
  serviciosBasicosCacheTimestamp = ahora;
  return hits;
}

function construirListaLigeraServicios(hits) {
  return hits.map((h) => ({
    id: h.objectID || h.id,
    n: h.nombre || "",
    d: (h.descripcion || "").substring(0, 40),
  }));
}

// ============================================================================
// 2) MATCH LOCAL (gratis, sin IA) — resuelve la mayoría de casos por nombre
// ============================================================================
function matchLocalServicioBasico(mensaje, listaLigera) {
  const query = (mensaje || "").toLowerCase().trim();
  if (!query) return null;

  let mejor = null;
  let mejorScore = 0;

  for (const item of listaLigera) {
    const nombre = (item.n || "").toLowerCase().trim();
    if (!nombre) continue;

    let score = similarity.stringSimilarity(query, nombre);
    if (query.includes(nombre)) score = Math.max(score, 0.9);

    if (score > mejorScore) {
      mejorScore = score;
      mejor = item;
    }
  }

  if (mejor && mejorScore >= UMBRAL_MATCH_LOCAL_SERVICIOS) {
    console.log(
      `✅ [matchLocalServicioBasico] Match local sin IA: "${mejor.n}" (score: ${mejorScore.toFixed(2)})`,
    );
    return mejor.id;
  }

  return null;
}

// ============================================================================
// 3) PROMPT 1 — CLASIFICADOR IA (GPT-5.4-nano) — solo si no hubo match local
// ============================================================================
function construirPromptClasificadorServicios(
  mensaje,
  contextoPrevio,
  listaLigera,
) {
  const contextoRaw = contextoPrevio?.contexto_usuario ?? contextoPrevio;
  const contextoStr =
    contextoRaw === undefined || contextoRaw === null
      ? "null"
      : typeof contextoRaw === "string"
        ? contextoRaw
        : JSON.stringify(contextoRaw);

  return `Elige el servicio básico más parecido a lo que pide el usuario.
CONTEXTO PREVIO: ${contextoStr}
LISTA (id,nombre,desc):
${JSON.stringify(listaLigera)}
MENSAJE: "${mensaje}"
REGLAS:
- Responde SOLO JSON: {"id":"..."}
- El id DEBE existir EXACTO en la LISTA, nunca inventes
- Si el mensaje es continuación/pregunta sobre el mismo servicio del CONTEXTO PREVIO → responde ese mismo id
- Si ninguno de la LISTA coincide claramente con lo que pide → {"id":"NINGUNO"}
- Ignora tildes, mayúsculas y errores de tipeo al comparar`;
}

async function clasificarServicioBasicoIA(
  mensaje,
  contexto_previo,
  listaLigera,
) {
  const prompt = construirPromptClasificadorServicios(
    mensaje,
    contexto_previo,
    listaLigera,
  );

  const completion = await openai.chat.completions.create({
    model: "gpt-5.4-nano",
    messages: [{ role: "user", content: prompt }],
    response_format: { type: "json_object" },
    reasoning_effort: "none",
    max_completion_tokens: 60,
  });

  const tokens = {
    prompt_tokens: completion.usage?.prompt_tokens || 0,
    completion_tokens: completion.usage?.completion_tokens || 0,
    total_tokens: completion.usage?.total_tokens || 0,
  };

  let idElegido = null;
  try {
    const parsed = JSON.parse(completion.choices[0]?.message?.content || "{}");
    const raw = (parsed.id || "").toString().trim();
    idElegido = raw && raw.toUpperCase() !== "NINGUNO" ? raw : null;
  } catch (e) {
    console.error(
      "❌ [clasificarServicioBasicoIA] Error parseando:",
      e.message,
    );
  }

  return { idElegido, tokens };
}

// ============================================================================
// 4) PROMPT 2 — GEMINI REDACTA (solo con id+nombre+desc, sin contacto)
// Gemini decide qué campos de contacto ("campos") pidió el usuario, y el
// código arma el bloque real de contacto a partir de "match", nunca de lo
// que Gemini redactó.
// ============================================================================
function construirPromptRespuestaServicio({
  match,
  mensaje,
  nombre_usuario,
  momento_dia,
}) {
  const datoParaPrompt = {
    id: match.objectID || match.id,
    nombre: match.nombre || "",
    desc: (match.descripcion || "").substring(0, 150),
  };

  return `Responde en JSON válido.
DATOS:
${JSON.stringify(datoParaPrompt)}
El usuario se llama: ${nombre_usuario || ""} úsalo siempre
MENSAJE DEL USUARIO: "${mensaje || ""}"
REGLAS:
- Usa SOLO lo que hay en DATOS (id:${datoParaPrompt.id}, nombre:${datoParaPrompt.nombre}), no inventes nada extra
- NUNCA inventes teléfonos ni ningún dato de contacto, eso lo agrega el sistema aparte
- Nunca SALUDES con buenos o hola, habla como si la conversación ya estuviera en curso, directo al grano
- LENGUAJE LOCAL SIEMPRE, habla como un pata de Barranca peruano, canchero, nada robótico ni corporativo
- DIRECTO SIN FLORO: nada de rodeos, nada de vender el servicio, solo suelta el dato tal cual lo pidió
- mensaje: máximo 2 frases
- USA EL MOMENTO DEL DIA: ${momento_dia}
- pidio_otro_dato: true SOLO si el usuario pidió específicamente Facebook, Instagram, sitio web o página, y NO pidió teléfono. false en cualquier otro caso (incluye cuando pidió teléfono, o cuando no especificó nada)
- NUNCA digas frases como "mensaje predeterminado", "esto es automático", ni nada que describa la naturaleza de tu propia respuesta
FORMATO OBLIGATORIO:
{"id":"${datoParaPrompt.id}","mensaje":"...","intencion":"SERVICIOS_BASICOS","pidio_otro_dato":false}`;
}

function obtenerMomentoDia() {
  const hora = Number(
    new Intl.DateTimeFormat("en-US", {
      hour: "numeric",
      hour12: false,
      timeZone: "America/Lima",
    }).format(new Date()),
  );

  if (hora < 12) return hora >= 6 ? "manana" : "noche";
  return hora < 18 ? "tarde" : "noche";
}

function pick(arr) {
  if (!Array.isArray(arr) || !arr.length) return "";
  return arr[Math.floor(Math.random() * arr.length)];
}

function parsearRespuestaIA(raw) {
  if (!raw || typeof raw !== "string") return {};
  try {
    let limpio = raw
      .replace(/```json|```/gi, "")
      .replace(/\n/g, " ")
      .trim();
    if (limpio.startsWith('"') || limpio.startsWith("'")) {
      try {
        limpio = JSON.parse(limpio);
      } catch (e) {}
    }
    limpio = limpio.replace(/([,{]\s*)([a-zA-Z_]\w*)\s*:/g, '$1"$2":');
    const match = limpio.match(/\{.*\}/s);
    if (!match) return {};
    return JSON.parse(match[0]);
  } catch (e) {
    console.error(
      "❌ Error parseando respuesta IA (tienda):",
      e.message,
      "| RAW:",
      raw.slice(0, 200),
    );
    return {};
  }
}
// ============================================================================
// 5) FUNCIÓN PRINCIPAL
// ============================================================================
async function procesarBusquedaServiciosBasicos({
  mensaje,
  contexto_previo,
  localidad,
  nombre_usuario,
}) {
  const tiempoInicioTotal = Date.now();
  const momento_dia = obtenerMomentoDia();

  let tokensOpenAI = {
    prompt_tokens: 0,
    completion_tokens: 0,
    total_tokens: 0,
  };
  let tokensGemini = {
    promptTokenCount: 0,
    candidatesTokenCount: 0,
    totalTokenCount: 0,
  };

  if (!mensaje || typeof mensaje !== "string" || !mensaje.trim()) {
    throw new Error("El campo 'mensaje' es requerido");
  }

  const hitsAmplios = await buscarServiciosBasicosAmplio({ localidad });
console.log(
  "🔍 [DEBUG servicios_basicos] Muestra de hits:",
  JSON.stringify(hitsAmplios.slice(0, 3).map((h) => ({
    id: h.objectID || h.id,
    nombre: h.nombre,
    img_logo: h.img_logo,
    keys: Object.keys(h),
  }))),
);
  if (!hitsAmplios.length) {
    return {
      id: "sin_id",
      mensaje: "Por ahora no tengo servicios básicos registrados por aquí 😕",
      mensaje_safe:
        "Por ahora no tengo servicios básicos registrados por aquí 😕",
      intencion: "SIN_DATOS",
      tokens_usados: { openai: tokensOpenAI, gemini: tokensGemini },
      tiempo_total_ms: Date.now() - tiempoInicioTotal,
    };
  }

  const listaLigera = construirListaLigeraServicios(hitsAmplios);

  const hayContextoRelevante = !!(
    contexto_previo?.contexto_usuario || contexto_previo
  );

  let idElegido = null;
  if (!hayContextoRelevante) {
    idElegido = matchLocalServicioBasico(mensaje, listaLigera);
  }

  if (!idElegido) {
    const { idElegido: idIA, tokens: tokensClasificador } =
      await clasificarServicioBasicoIA(mensaje, contexto_previo, listaLigera);
    idElegido = idIA;
    tokensOpenAI.prompt_tokens += tokensClasificador.prompt_tokens;
    tokensOpenAI.completion_tokens += tokensClasificador.completion_tokens;
    tokensOpenAI.total_tokens += tokensClasificador.total_tokens;
  }

  if (!idElegido) {
    return {
      id: "sin_id",
      mensaje: pick(MENSAJES_SIN_RESULTADO_SERVICIOS),
      mensaje_safe: pick(MENSAJES_SIN_RESULTADO_SERVICIOS),
      intencion: "SIN_DATOS",
      tokens_usados: { openai: tokensOpenAI, gemini: tokensGemini },
      tiempo_total_ms: Date.now() - tiempoInicioTotal,
    };
  }

  const match = hitsAmplios.find(
    (h) => String(h.objectID || h.id) === String(idElegido),
  );

  if (!match) {
    console.warn(
      "⚠️ [procesarBusquedaServiciosBasicos] id elegido no está en la lista:",
      idElegido,
    );
    return {
      id: "sin_id",
      mensaje: pick(MENSAJES_SIN_RESULTADO_SERVICIOS),
      mensaje_safe: pick(MENSAJES_SIN_RESULTADO_SERVICIOS),
      intencion: "SIN_DATOS",
      tokens_usados: { openai: tokensOpenAI, gemini: tokensGemini },
      tiempo_total_ms: Date.now() - tiempoInicioTotal,
    };
  }

  const promptRespuesta = construirPromptRespuestaServicio({
    match,
    mensaje,
    nombre_usuario,
    momento_dia,
  });

  const bodyGemini = {
    contents: [{ parts: [{ text: promptRespuesta }] }],
    generationConfig: {
      responseMimeType: "application/json",
      responseSchema: {
        type: "object",
        properties: {
          id: { type: "string" },
          mensaje: { type: "string" },
          intencion: { type: "string" },
          pidio_otro_dato: { type: "boolean" },
        },
        required: ["id", "mensaje", "intencion", "pidio_otro_dato"],
      },
      thinkingConfig: { thinkingBudget: 0 },
      maxOutputTokens: 180,
      temperature: 0.7,
    },
  };

  let response;
  const geminiRes = await fetch(`${GEMINI_URL}?key=${GEMINIKEY}`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(bodyGemini),
  });

  if (!geminiRes.ok) {
    const errText = await geminiRes.text();
    console.error(
      "❌ [procesarBusquedaServiciosBasicos] Error Gemini:",
      geminiRes.status,
      errText,
    );
    response = {
      id: match.objectID || match.id,
      mensaje:
        "Tuve un problema consultando la info, intenta de nuevo en un momento",
      intencion: "ERROR_GEMINI",
      pidio_otro_dato: false,
    };
  } else {
    const geminiData = await geminiRes.json();
    if (geminiData?.usageMetadata) {
      tokensGemini = {
        promptTokenCount: geminiData.usageMetadata.promptTokenCount || 0,
        candidatesTokenCount:
          geminiData.usageMetadata.candidatesTokenCount || 0,
        totalTokenCount: geminiData.usageMetadata.totalTokenCount || 0,
      };
    }
    const rawText =
      geminiData?.candidates?.[0]?.content?.parts?.[0]?.text || "{}";
    response = parsearRespuestaIA(rawText);
    if (!response || !Object.keys(response).length) {
      response = {
        id: match.objectID || match.id,
        mensaje: rawText || "Sin respuesta",
        intencion: "ERROR_FORMATO_IA",
        pidio_otro_dato: false,
      };
    }
  }

  const idFinal = response?.id || match.objectID || match.id;
  const mensajeFinal = String(response?.mensaje || "").trim();
  const nombreServicio = match.nombre || "";
  const telefono = match.telefono || "";
  const alias = match.alias || "";

  // 👇 SOLO exponemos teléfono directo. Cualquier otro dato pedido
  // (fb, ig, sitio_web) redirige al perfil dentro de la app usando el alias.
  const pidioOtroDato = response?.pidio_otro_dato === true;

  const linkPerfil = alias
    ? `https://geinztech.com/redirect/serviciosHogar/${alias}`
    : "";

  const partesMensaje = [mensajeFinal];
  if (!pidioOtroDato && telefono) {
    partesMensaje.push(`📞 ${telefono}`);
  }
  if (linkPerfil) {
    partesMensaje.push(`${pick(CTAS_SERVICIOS_BASICOS)}: ${linkPerfil}`);
  }
  const mensaje_safe = partesMensaje.filter(Boolean).join(" ");

  const imagenFinal = match.img_logo || "";

  const data = [
    "SERVICIOS_BASICOS",
    nombreServicio,
    "servicios_basicos",
    "null",
    idFinal,
  ].join("|");

  const tiempoTotalMs = Date.now() - tiempoInicioTotal;

  console.log(
    "🧭 [procesarBusquedaServiciosBasicos] TRACE:",
    JSON.stringify({
      mensaje,
      id_elegido: idFinal,
      alias,
      pidio_otro_dato: pidioOtroDato,
      total_disponibles: hitsAmplios.length,
      tiempo_total_ms: tiempoTotalMs,
    }),
  );
  return {
    ...response,
    id: idFinal,
    imagen: imagenFinal,
    mensaje_safe,
    data,
    telefono,
    alias,
    nombre_servicio: nombreServicio,
    tokens_usados: {
      openai: tokensOpenAI,
      gemini: {
        prompt_tokens: tokensGemini.promptTokenCount,
        completion_tokens: tokensGemini.candidatesTokenCount,
        total_tokens: tokensGemini.totalTokenCount,
      },
    },
    tiempo_total_ms: tiempoTotalMs,
    tiempo_total_seg: Number((tiempoTotalMs / 1000).toFixed(2)),
  };
}
// 👇 Esta es la que llamas desde el dispersador cuando categoria === "SERVICIOS_BASICOS"
exports.procesarBusquedaServiciosBasicos = procesarBusquedaServiciosBasicos;
