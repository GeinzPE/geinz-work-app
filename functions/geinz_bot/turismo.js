const { onRequest } = require("firebase-functions/v2/https");
const admin = require("firebase-admin");
const OpenAI = require("openai");
const algoliasearch = require("algoliasearch");

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
const index = client.initIndex("lugares");

const GEMINIKEY = process.env.PRIVATEKEY_GEMINI;
const GEMINI_URL =
  "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent";

const CACHE_TTL_MS = 1000 * 60 * 30;

const HITS_PER_PAGE_TURISMO_AMPLIO = 100;

let subcategoriasTurismoCache = null;
let subcategoriasTurismoCacheTimestamp = 0;


const stiker_turismo = [
  "https://firebasestorage.googleapis.com/v0/b/geinzworkapp.appspot.com/o/STIKER%2Fdanierl_playa_11zon.webp?alt=media&token=814c832b-d9cb-4062-83f6-a4b560a183f2",
  "https://firebasestorage.googleapis.com/v0/b/geinzworkapp.appspot.com/o/STIKER%2Fdaniel_turista2_11zon.webp?alt=media&token=af13f197-597e-4c77-9f3e-5d565bce518a",
  "https://firebasestorage.googleapis.com/v0/b/geinzworkapp.appspot.com/o/STIKER%2Fdaniel_relajado_11zon.webp?alt=media&token=14bd62ee-79e2-4408-8b89-efe773da1872",
  "https://firebasestorage.googleapis.com/v0/b/geinzworkapp.appspot.com/o/STIKER%2Fdanierl_turista_11zon.webp?alt=media&token=cb0a245e-336f-4afe-82b4-c5571771653b",
  "https://firebasestorage.googleapis.com/v0/b/geinzworkapp.appspot.com/o/STIKER%2FDise%C3%B1o%20sin%20t%C3%ADtulo%20(21)-Photoroom-convertido-de-png.webp?alt=media&token=54454bc2-d962-4312-98c5-53681e7b29b3",
];

const MENSAJES_SIN_RESULTADO_TURISMO = [
  "Uy, no encontré ningún sitio así por acá 😕. Prueba con otra búsqueda.",
  "Mmm, no me sale nada con eso por ahora 👀.",
  "No hay nada que calce con esa búsqueda por aquí 😅.",
  "Por ahora no tengo un lugar así registrado 🤔.",
  "No encontré resultados esta vez 🔍.",
];

const MENSAJES_INVITACION_TURISMO = [
  "Oye, si conoces un lugar bacán para pasear que no está en la app, cuéntanos al 958 120 920 y lo sumamos 🗺️",
  "Si sabes de un sitio piola para visitar por acá, escríbenos al 958 120 920 y lo agregamos al mapa 😎",
  "¿Conoces un lugar que la gente debería visitar? Mándanos un mensaje al 958 120 920 y lo metemos a Geinz ✨",
  "Cuéntanos de algún lugar chévere que conozcas al 958 120 920 y lo sumamos por acá 🔥",
];


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

function tokensVacios() {
  return {
    prompt_tokens: 0,
    completion_tokens: 0,
    thoughts_tokens: 0,
    total_tokens: 0,
  };
}


function sumarTokens(total, extra) {
  total.prompt_tokens += extra?.prompt_tokens || 0;
  total.completion_tokens += extra?.completion_tokens || 0;
  total.thoughts_tokens += extra?.thoughts_tokens || 0;
  total.total_tokens += extra?.total_tokens || 0;
}

async function llamarGemini(
  prompt,
  {
    jsonMode = true,
    systemMessage = null,
    maxOutputTokens = 300,
    schema = null,
  } = {},
) {
  const contents = [];
  if (systemMessage) {
    contents.push({ role: "user", parts: [{ text: systemMessage }] });
    contents.push({ role: "model", parts: [{ text: "Entendido." }] });
  }
  contents.push({ role: "user", parts: [{ text: prompt }] });

  const body = {
    contents,
    generationConfig: {
      ...(jsonMode ? { responseMimeType: "application/json" } : {}),
      ...(schema ? { responseSchema: schema } : {}),
      maxOutputTokens,
      thinkingConfig: { thinkingBudget: 0 },
    },
  };

  const resp = await fetch(`${GEMINI_URL}?key=${GEMINIKEY}`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(body),
  });

  const data = await resp.json();

  const texto =
    data?.candidates?.[0]?.content?.parts?.map((p) => p.text).join("") || "{}";

  const tokens = {
    prompt_tokens: data?.usageMetadata?.promptTokenCount || 0,
    completion_tokens: data?.usageMetadata?.candidatesTokenCount || 0,
    thoughts_tokens: data?.usageMetadata?.thoughtsTokenCount || 0,
    total_tokens: data?.usageMetadata?.totalTokenCount || 0,
  };

  return { texto, tokens, raw: data };
}

async function llamarOpenAIClasificador(prompt) {
  const completion = await openai.chat.completions.create({
    model: "gpt-5.4-nano",
    messages: [{ role: "user", content: prompt }],
    response_format: { type: "json_object" },
    reasoning_effort: "none",
  });

  const texto = completion.choices[0]?.message?.content || "{}";

  const tokens = {
    prompt_tokens: completion.usage?.prompt_tokens || 0,
    completion_tokens: completion.usage?.completion_tokens || 0,
    thoughts_tokens:
      completion.usage?.completion_tokens_details?.reasoning_tokens || 0,
    total_tokens: completion.usage?.total_tokens || 0,
  };

  return { texto, tokens, raw: completion };
}

async function obtenerSubcategoriasTurismo() {
  const ahora = Date.now();
  if (
    subcategoriasTurismoCache &&
    ahora - subcategoriasTurismoCacheTimestamp < CACHE_TTL_MS
  ) {
    console.log("♻️ Usando subcategorías de turismo desde cache");
    return subcategoriasTurismoCache;
  }

  console.log("🔄 Refrescando subcategorías de turismo desde Firestore");
  const snap = await db.doc("Tiendas/categorias/categorias/turismo").get();
  const subcategorias = snap.exists ? (snap.get("subcategorias") ?? []) : [];

  subcategoriasTurismoCache = subcategorias;
  subcategoriasTurismoCacheTimestamp = ahora;
  return subcategorias;
}

function construirPromptTurismo(mensaje, contextoPrevio, categorias) {
  const contextoStr = JSON.stringify(
    contextoPrevio || {
      tipo: "GEINZ",
      categoria: "null",
      extra: "null",
      id: null,
      nombre: "null",
    },
  );

  return `Responde SOLO JSON válido. Sin explicaciones, sin markdown.
CATEGORIAS: ${categorias.join(",")}
CONTEXTO PREVIO: ${contextoStr}
MENSAJE DEL USUARIO: "${mensaje}"

MAPEO RAPIDO:
agua|rio|nadar|refrescar|mojar|laguna|cascada|fresquito|bañarse → "playa" o "rio"
historia|ruinas|antiguo|colonial|inca|prehispanico → "historico" o "arqueologico"
vista|cerro|panorama|alto|paisaje → "mirador"
naturaleza|caminar|verde|aire libre|tranquilo → "parque" o "recreativo"
fe|misa|rezar|iglesia|virgen → "iglesia"

DETECCION:

1. NOMBRE PROPIO: si el user menciona un nombre específico de lugar ("se llama X", "existe X", "hay un X", "conoces X")
   → nombre: [nombre limpio] | categoria: null | excluir_id: null — PRIORIDAD MAXIMA

2. SOLO ZONA (barranca, paramonga, pativilca, supe) sin detalle
   → nombre: null | categoria: [elegir de CATEGORIAS]

3. SIN LUGAR ESPECIFICO
   → nombre: null | categoria: [elegir de CATEGORIAS segun intencion]

4. USER DICE "dame otro", "más", "otro similar", "otra opción"
   → nombre: null | categoria: [heredar categoria del CONTEXTO PREVIO]

5. USER RECHAZA o pide algo diferente
   → nombre: null | categoria: [re-evaluar segun nueva intencion]

LIMPIEZA: minusculas, sin tildes, sin inventar.

SALIDA: {"tipo":"turismo","nombre":"...","categoria":"...","excluir_id":"..."}

- Si el user dice "otro", "no quiero ese", "muéstrame más" → excluir_id: [id del CONTEXTO PREVIO]
- En cualquier otro caso → excluir_id: null`;
}

async function clasificarTurismo(mensaje, contexto_previo) {
  const categorias = await obtenerSubcategoriasTurismo();
  const prompt = construirPromptTurismo(mensaje, contexto_previo, categorias);

  const { texto, tokens } = await llamarOpenAIClasificador(prompt);

  let resultado;
  try {
    resultado = JSON.parse(texto);
  } catch (e) {
    console.error(
      "❌ Error parseando respuesta de OpenAI (turismo):",
      e.message,
      "| RAW:",
      texto,
    );
    resultado = {
      tipo: "turismo",
      nombre: null,
      categoria: null,
      excluir_id: null,
    };
  }

  return { resultado, tokens };
}

async function buscarTurismoAmplio({ localidad }) {
  const filters = [`categoria:"turismo"`];
  if (localidad) filters.push(`lugar:"${localidad}"`);

  console.log(
    `🚀 [buscar_turismo_amplio] Buscando turismo "${localidad || ""}" en paralelo con IA clasificadora (hitsPerPage: ${HITS_PER_PAGE_TURISMO_AMPLIO})`,
  );

  const { hits } = await index.search("", {
    filters: filters.join(" AND "),
    hitsPerPage: HITS_PER_PAGE_TURISMO_AMPLIO,
    typoTolerance: true,
    ignorePlurals: true,
    removeStopWords: true,
  });

  console.log(
    `✅ [buscar_turismo_amplio] ${hits.length} hits amplios de turismo`,
  );

  return hits;
}

async function obtenerLugaresTuristicos(
  localidad,
  nombre,
  subcategoria,
  excluir_id,
  preHits, // 👈 NUEVO (opcional): hits ya traídos por buscarTurismoAmplio en paralelo
) {
  const excluirIds = Array.isArray(excluir_id)
    ? excluir_id.filter((id) => id !== null && id !== undefined && id !== "")
    : excluir_id
      ? [excluir_id]
      : [];

  let hits;

  if (!nombre && Array.isArray(preHits)) {
    // 👇 Búsqueda por categoria (o sin criterio): ya tenemos los hits de la
    // búsqueda amplia en paralelo. Solo falta filtrar en memoria por
    // subcategoria (tag) y por excluir_id — nada de esto necesita una
    // nueva consulta a Algolia.
    console.log(
      `♻️ [lugares_turisticos] Usando ${preHits.length} hits precargados (paralelo), filtrando en memoria`,
    );

    const excluirSet = new Set(excluirIds.map((id) => String(id)));
    const subLimpia = subcategoria ? subcategoria.toLowerCase().trim() : null;

    hits = preHits.filter((h) => {
      if (excluirSet.has(String(h.objectID))) return false;
      if (!subLimpia) return true;
      return (
        Array.isArray(h.tag) &&
        h.tag.some((t) => (t || "").toLowerCase().trim() === subLimpia)
      );
    });

    console.log(
      `🔎 [lugares_turisticos] ${hits.length} hits tras filtrar por subcategoria "${subcategoria || "(ninguna)"}" y excluir_id`,
    );
  } else {
    // Camino original: búsqueda por nombre propio (necesita el matching de
    // texto real de Algolia — typoTolerance, relevancia, etc. — que no se
    // puede replicar filtrando en memoria) o fallback sin búsqueda paralela.
    let filters = [`categoria:"turismo"`];
    if (localidad) filters.push(`lugar:"${localidad}"`);
    if (subcategoria) filters.push(`tag:"${subcategoria}"`);

    if (excluirIds.length > 0) {
      const excluirFilters = excluirIds
        .map((id) => `NOT objectID:"${id}"`)
        .join(" AND ");
      filters.push(excluirFilters);
    }

    const query = nombre || "";

    const resultadoAlgolia = await index.search(query, {
      filters: filters.join(" AND "),
      hitsPerPage: 20,
      typoTolerance: true,
      ignorePlurals: true,
      removeStopWords: true,
    });

    hits = resultadoAlgolia.hits;
  }

  const LIMITE = 5;

  const data = hits
    .sort(() => Math.random() - 0.5)
    .slice(0, LIMITE)
    .map((hit) => ({
      id: hit.objectID,
      titulo: hit.nombre || "",
      descripcion: (hit.descripcion || "").substring(0, 150),
      img: hit.img || "",
      tipo: "turismo",
      tag: hit.tag || [],
      alias: hit.alias || "",
    }));

  return {
    ok: true,
    total: data.length,
    momento_dia: obtenerMomentoDia(),
    tipo: "turismo",
    data,
  };
}

function construirDataLigeraParaAgente(lugaresData) {
  return (lugaresData || []).map((l) => ({
    id: l.id,
    nombre: l.titulo,
    tag: Array.isArray(l.tag) ? l.tag.join(",") : "",
    resumen: (l.descripcion || "").substring(0, 80),
  }));
}

function construirSystemMessageAgente({ data, usuario, momento_dia }) {
  return `DATA:
${JSON.stringify(data)}

INSTRUCCION CRITICA: Responde UNICAMENTE con el JSON, sin texto antes ni después, sin markdown, sin explicaciones.

REGLAS:
- Usar SOLO la DATA
- PROHIBIDO saludar, PROHIBIDO decir Hola, PROHIBIDO empezar con el nombre, habla como si ya estuvieran en medio de una conversación, lenguaje local siempre
- Elegir el lugar más relevante SEGÚN la intención del USUARIO
- El id DEBE ser exactamente el id del lugar escogido de la DATA, OBLIGATORIO
- mensaje: 1-2 líneas, tono amigable con clase
- usar exactamente 2 emojis
- sin comillas dobles dentro del mensaje
- usuario: ${usuario}
- USA EL MOMENTO DEL DIA SIEMPRE: ${momento_dia}
- NUNCA digas frases como "mensaje predeterminado", "mensaje genérico", "esto es automático" ni nada que describa la naturaleza de tu propia respuesta
- no inventar

FORMATO OBLIGATORIO, EXACTAMENTE ASI:
{"id":"AQUI_EL_ID","mensaje":"AQUI_EL_MENSAJE"}`;
}

async function agenteFinalTurismo(
  mensajeUsuario,
  lugaresData,
  usuario,
  momento_dia,
) {
  const dataLigera = construirDataLigeraParaAgente(lugaresData);

  const systemMessage = construirSystemMessageAgente({
    data: dataLigera,
    usuario,
    momento_dia,
  });

  const { texto, tokens } = await llamarGemini(mensajeUsuario, {
    jsonMode: true,
    systemMessage,
    maxOutputTokens: 250,
    schema: {
      // 👈 NUEVO
      type: "object",
      properties: {
        id: { type: "string" },
        mensaje: { type: "string" },
      },
      required: ["id", "mensaje"],
    },
  });

  return { texto, tokens };
}

function parseIA(raw) {
  if (!raw || typeof raw !== "string") return {};
  try {
    raw = raw
      .replace(/```json|```/gi, "")
      .replace(/\n/g, " ")
      .trim();
    if (raw.startsWith('"') || raw.startsWith("'")) {
      try {
        raw = JSON.parse(raw);
      } catch (e) {}
    }
    raw = raw.replace(/([,{]\s*)([a-zA-Z_]\w*)\s*:/g, '$1"$2":');
    const match = raw.match(/\{.*\}/s);
    if (!match) return {};
    return JSON.parse(match[0]);
  } catch (e) {
    console.log("❌ ERROR PARSE IA:", e.message, "| RAW:", raw.slice(0, 120));
    return {};
  }
}

function agregarImagenesTurismo(responseRaw, apiResponse) {
  let link_construido = "";

  let response = parseIA(responseRaw);
  if (response?.mensaje && typeof response.mensaje === "string") {
    const inner = parseIA(response.mensaje);
    if (inner?.id && inner?.intencion) {
      console.log("🔁 DOUBLE PARSE: JSON encontrado dentro de 'mensaje'");
      response = { ...inner };
    }
  }
  if (!response || !Object.keys(response).length) {
    console.error(
      "⚠️ [agregarImagenesTurismo] No se pudo parsear respuesta de Gemini, usando mensaje genérico en vez de mandar el JSON crudo | raw:",
      (responseRaw || "").slice(0, 300),
    );
    response = {
      mensaje: pick([
        "Uy, se me cruzaron los cables un toque 😅 ¿me repites qué buscabas?",
        "Perdona causa, me trabé ahí 🙈 ¿puedes preguntarme de nuevo?",
        "Se me fue la onda un segundo 😂 vuelve a decirme qué necesitas",
      ]),
      id: null,
      intencion: "ERROR_FORMATO_IA",
      estado: "FALLBACK",
    };
  }
  console.log("📦 RESPONSE:", response);

  const apiData = Array.isArray(apiResponse.data)
    ? apiResponse.data
    : Object.values(apiResponse.data || {});
  const responseId = String(response?.id || "")
    .trim()
    .replace(/"/g, "");
  const match = apiData.find(
    (d) =>
      String(d?.id || "")
        .trim()
        .replace(/"/g, "") === responseId,
  );

  const idFinal = response?.id || match?.id || "sin_id";
  const mensajeFinal = String(response?.mensaje || "").trim();
  const imagenFinal = match?.img || "";
  const keywords = response?.keywords || response?.palabras_clave || [];
  const kwArr = Array.isArray(keywords) ? keywords : [];

  const nombre =
    match?.nombre || match?.name || match?.titulo || match?.lugar || "";
  const tags = Array.isArray(match?.tag) ? match.tag.join(",") : "turismo";
  const extra = kwArr.length ? kwArr.join(",") : "null";
  const data = ["TURISMO", nombre, tags, extra, idFinal].join("|");

  const alias_turismo = match?.alias || "";
  link_construido = alias_turismo
    ? `https://geinztech.com/turismo/${alias_turismo}`
    : "";

  const ctas = [
    "👉 Mira su perfil aqui",
    "🔥 Descúbrelo en Geinz",
    "📍 Mira todos los detalles",
    "🚀 Explóralo ahora",
    "😎 Dale un vistazo aquí",
    "✨ Mira en Geinz",
    "👀 Chequéalo aquí",
    "📲 Ábrelo en la app",
  ];

  const mensaje_safe = mensajeFinal
    ? link_construido
      ? `${mensajeFinal}  ${pick(ctas)}: ${link_construido}`
      : mensajeFinal
    : link_construido
      ? `${pick(ctas)}: ${link_construido}`
      : "";

  const imagen_stiker = pick(stiker_turismo);

  console.log("MATCH:", JSON.stringify(match));
  console.log("DATA TURISMO:", data);
  console.log("IMAGEN:", imagenFinal);

  // Estructura idéntica a tu código original (rama turismo). Los campos
  // exclusivos de tienda quedan igual que salían para tipo="turismo": vacíos/false.
  return {
    ...response,
    id: idFinal,
    imagen: imagenFinal,
    mensaje_safe,
    data,
    siker: imagen_stiker,
    msje_pla_wa: "",
    plantilla: false,
    wha: "",
    cat_detectada: "",
    era_plantilla_pero_misio: false,
    nombre_negocio: "",
    token_wsap: "",
    alias_tienda: "",
  };
}

async function procesarBusquedaTurismo({
  mensaje,
  contexto_previo,
  localidad,
  usuario,
}) {
  const inicio = Date.now();
  const tokensOpenAI = tokensVacios();
  const tokensGemini = tokensVacios();
  const pasos = [];

  if (!mensaje || typeof mensaje !== "string" || !mensaje.trim()) {
    const err = new Error("El campo 'mensaje' es requerido");
    err.status = 400;
    throw err;
  }

  // ============================================================
  // 1) PARALELIZACIÓN: la IA clasificadora y la búsqueda amplia de
  // turismo en Algolia corren AL MISMO TIEMPO con Promise.all, en vez de
  // esperar a que la IA termine para recién ahí tocar Algolia.
  // La búsqueda amplia solo depende de "localidad" (ya se conoce desde el
  // inicio) y de categoria:"turismo" (fija), así que no hay ningún dato
  // inventado ni adelantado que dependa de la respuesta de la IA.
  // ============================================================
  const tParalelo = Date.now();
  const [resultadoClasificador, hitsAmplios] = await Promise.all([
    clasificarTurismo(mensaje, contexto_previo).then((r) => ({
      ...r,
      _tiempo_ms: Date.now() - tParalelo,
    })),
    buscarTurismoAmplio({ localidad }).then((hits) => ({
      hits,
      _tiempo_ms: Date.now() - tParalelo,
    })),
  ]);

  const { resultado: clasificacion, tokens: tokensClasificador } =
    resultadoClasificador;
  sumarTokens(tokensOpenAI, tokensClasificador);
  pasos.push({
    paso: "clasificador",
    motor: "openai",
    tiempo_ms: resultadoClasificador._tiempo_ms,
    tokens: tokensClasificador,
    resultado: clasificacion,
  });
  pasos.push({
    paso: "busqueda_algolia_amplia",
    motor: "algolia",
    tiempo_ms: hitsAmplios._tiempo_ms,
    tokens: null,
    resultado: { total_amplio: hitsAmplios.hits.length },
  });

  // 2) Filtrado / búsqueda final de lugares (usa los hits precargados si
  // aplica, o hace la consulta real a Algolia si la IA detectó un nombre
  // propio — ver comentario dentro de obtenerLugaresTuristicos)
  const t2 = Date.now();
  const lugares = await obtenerLugaresTuristicos(
    localidad,
    clasificacion?.nombre,
    clasificacion?.categoria,
    clasificacion?.excluir_id,
    hitsAmplios.hits,
  );
  pasos.push({
    paso: "filtrado_lugares",
    motor: clasificacion?.nombre ? "algolia" : "local",
    tiempo_ms: Date.now() - t2,
    tokens: null,
    resultado: { total: lugares.total, ids: lugares.data.map((d) => d.id) },
  });

  if (!lugares.total) {
    const mensajeBase = pick(MENSAJES_SIN_RESULTADO_TURISMO);
    const mensaje_safe =
      `${mensajeBase} ${pick(MENSAJES_INVITACION_TURISMO)}`.trim();

    const tiempo_ms = Date.now() - inicio;
    const tokens_usados = {
      gemini: tokensGemini,
      openai: tokensOpenAI,
      total: tokensGemini.total_tokens + tokensOpenAI.total_tokens,
      pasos,
    };

    console.log(
      "🧭 [procesarBusquedaTurismo] Sin resultados turístico | mensaje_safe:",
      mensaje_safe,
    );

    return {
      id: "sin_id",
      mensaje: mensajeBase,
      imagen: "",
      mensaje_safe,
      data: ["TURISMO", "", "", "null", "sin_id"].join("|"),
      siker: pick(stiker_turismo),
      msje_pla_wa: "",
      plantilla: false,
      wha: "",
      cat_detectada: "",
      era_plantilla_pero_misio: false,
      nombre_negocio: "",
      token_wsap: "",
      alias_tienda: "",
      tokens_usados,
      tiempo_ms,
    };
  }
  // 3) Agente final — Gemini
  const t3 = Date.now();
  const usuarioNombre = usuario || "amigo";
  const { texto: respuestaAgenteRaw, tokens: tokensAgente } =
    await agenteFinalTurismo(
      mensaje,
      lugares.data,
      usuarioNombre,
      lugares.momento_dia,
    );
  sumarTokens(tokensGemini, tokensAgente);
  pasos.push({
    paso: "agente_final",
    motor: "gemini",
    tiempo_ms: Date.now() - t3,
    tokens: tokensAgente,
    resultado: respuestaAgenteRaw,
  });

  // 4) Agregador de imágenes — arma la salida final
  const t4 = Date.now();
  const salidaFinal = agregarImagenesTurismo(respuestaAgenteRaw, lugares);
  pasos.push({
    paso: "agregador_imagenes",
    motor: "local",
    tiempo_ms: Date.now() - t4,
    tokens: null,
    resultado: { id: salidaFinal.id, imagen: !!salidaFinal.imagen },
  });

  const tiempo_ms = Date.now() - inicio;
  const tokens_usados = {
    gemini: tokensGemini,
    openai: tokensOpenAI,
    total: tokensGemini.total_tokens + tokensOpenAI.total_tokens,
    pasos,
  };

  console.log(
    "💰 [procesarBusquedaTurismo] TOKENS:",
    JSON.stringify({ gemini: tokensGemini, openai: tokensOpenAI }),
    "| ⏱️ TIEMPO_MS:",
    tiempo_ms,
  );

  return {
    ...salidaFinal,
    tokens_usados,
    tiempo_ms,
  };
}

exports.clasificador_geinz_turismo = onRequest(async (req, res) => {
  try {
    const { mensaje, contexto_previo, localidad, usuario } = req.body;
    const resultado = await procesarBusquedaTurismo({
      mensaje,
      contexto_previo,
      localidad,
      usuario,
    });
    return res.status(200).json(resultado);
  } catch (error) {
    console.error("❌ Error clasificador_geinz_turismo:", error.message);
    return res
      .status(error.status || 500)
      .json({ ok: false, error: error.message });
  }
});



async function obtenerTurismoPorIdONombre({ id, nombre, localidad }) {
  const ATTRS = [
    "objectID",
    "nombre",
    "descripcion",
    "lugar",
    "categoria",
    "img",
    "tag",
    "alias",
  ];

  let hit = null;

  if (id) {
    try {
      hit = await index.getObject(id, { attributesToRetrieve: ATTRS });
    } catch (e) {
      console.error(
        "❌ [info_turismo] No se encontró objeto por id:",
        id,
        e.message,
      );
      hit = null;
    }
  }

  if (!hit && nombre) {
    const query = nombre.toLowerCase().trim();
    const filters = [`categoria:"turismo"`];
    if (localidad) filters.push(`lugar:"${localidad}"`);

    const { hits } = await index.search(query, {
      filters: filters.join(" AND "),
      hitsPerPage: 1,
      typoTolerance: true,
      ignorePlurals: true,
      removeStopWords: true,
      attributesToRetrieve: ATTRS,
    });

    hit = hits?.[0] || null;
  }

  return hit;
}

async function resolverInfoTurismo({
  id,
  nombre,
  mensaje,
  localidad,
  nombre_usuario,
}) {
  const tiempoInicioTotal = Date.now();
  let tokensGemini = {
    promptTokenCount: 0,
    candidatesTokenCount: 0,
    totalTokenCount: 0,
  };

  const momento_dia = obtenerMomentoDia();

  const hit = await obtenerTurismoPorIdONombre({ id, nombre, localidad });

  if (!hit) {
    return {
      id: "sin_id",
      mensaje: "No encontré ese lugar, prueba con otro nombre",
      mensaje_safe: "No encontré ese lugar, prueba con otro nombre",
      intencion: "SIN_DATOS",
      tokens_usados: { gemini: tokensGemini },
      tiempo_total_ms: Date.now() - tiempoInicioTotal,
    };
  }

  const datoParaPrompt = {
    id: hit.objectID,
    lugar: hit.nombre || "",
    desc: (hit.descripcion || "").substring(0, 150),
    tag: Array.isArray(hit.tag) ? hit.tag.join(",") : "",
  };

  const promptRespuesta = `Responde en JSON válido.
DATOS DEL LUGAR TURÍSTICO:
${JSON.stringify(datoParaPrompt)}
El usuario se llama: ${nombre_usuario || ""} úsalo siempre
MENSAJE/PREGUNTA DEL USUARIO: "${mensaje || ""}"
REGLAS:
- Responde AL GRANO exactamente lo que el usuario pregunta sobre ESTE lugar (id:${hit.objectID}, nombre:${hit.nombre}), basándote SOLO en los DATOS
- Si el usuario pide un dato que NO está en los DATOS (ej: horario exacto, precio de entrada) → dilo con naturalidad, sin inventar
- Nunca SALUDES con buenos o hola, habla como conversación continua, como si ya se conocieran, LENGUAJE LOCAL SIEMPRE MUY AMIGABLE nada robótico ni corporativo, habla como un pata de Barranca peruano, canchero
- mensaje: máximo 2 líneas (máximo 2 frases)
- sin comillas dentro del mensaje
- USA EL MOMENTO DEL DIA SIEMPRE QUE ES: ${momento_dia}
- NUNCA digas frases como "mensaje predeterminado", "mensaje genérico", "esto es automático" ni nada que describa la naturaleza de tu propia respuesta
- NO menciones links, perfiles, ni la app Geinz, eso lo agrega el sistema aparte
FORMATO OBLIGATORIO:
{"id":"${hit.objectID}","mensaje":"...","intencion":"TURISMO"}`;

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
        },
        required: ["id", "mensaje", "intencion"],
      },
      thinkingConfig: { thinkingBudget: 0 },
      maxOutputTokens: 220,
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
      "❌ [resolverInfoTurismo] Error Gemini API:",
      geminiRes.status,
      errText,
    );
    response = {
      id: hit.objectID,
      mensaje:
        "Tuve un problema consultando la info, intenta de nuevo en un momento",
      intencion: "ERROR_GEMINI",
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
        id: hit.objectID,
        mensaje: rawText || "Sin respuesta",
        intencion: "ERROR_FORMATO_IA",
      };
    }
  }

  const idFinal = response?.id || hit.objectID;
  const mensajeFinal = String(response?.mensaje || "").trim();
  const imagenFinal = hit.img || "";
  const nombreLugar = hit.nombre || "";
  const tags = Array.isArray(hit.tag) ? hit.tag.join(",") : "turismo";
  const alias = hit.alias || "";

  const link_construido = alias
    ? `https://geinztech.com/turismo/${alias}`
    : "";

  const mensaje_safe = link_construido
    ? `${mensajeFinal} 📲 Mira más detalles en Geinz: ${link_construido}`
    : mensajeFinal;

  const imagen_stiker = pick(stiker_turismo);

  const data = ["TURISMO", nombreLugar, tags, "null", idFinal].join("|");

  const tiempoTotalMs = Date.now() - tiempoInicioTotal;

  return {
    ...response,
    id: idFinal,
    imagen: imagenFinal,
    mensaje_safe,
    data,
    siker: imagen_stiker,
    nombre_lugar: nombreLugar,
    alias_turismo: alias,
    tokens_usados: {
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
      "❌ Error parseando respuesta IA (turismo info):",
      e.message,
      "| RAW:",
      raw.slice(0, 200),
    );
    return {};
  }
}

exports.resolverInfoTurismo = resolverInfoTurismo;
exports.procesarBusquedaTurismo = procesarBusquedaTurismo;

