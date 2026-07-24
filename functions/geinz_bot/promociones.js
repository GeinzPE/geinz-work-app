const { onRequest } = require("firebase-functions/v2/https");
const admin = require("firebase-admin");
const OpenAI = require("openai");
const algoliasearch = require("algoliasearch");
const { FieldValue } = require("firebase-admin/firestore");

const { obtener_creditos_tienda_fn } = require("../test_db2");

const similarity = require("string-similarity-js");
const openai = new OpenAI({
  apiKey: process.env.API_KEYO_OPEN_IA,
});

const db = admin.firestore();

const APP_ID = process.env.ALGOLIA_APP_ID || "";
const API_KEY = process.env.ALGOLIA_API_KEY || "";
const client = algoliasearch(APP_ID, API_KEY);
const index = client.initIndex("lugares");
const index_Algolia_promos = client.initIndex("promociones_filtrado_index");

const GEMINIKEY = process.env.PRIVATEKEY_GEMINI;
const GEMINI_URL =
  "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent";

const CACHE_TTL_MS = 1000 * 60 * 30;
const { guardarMensajeHistorial } = require("../historial_whatsapp.js");


function pick(arr) {
  if (!Array.isArray(arr) || !arr.length) return "";
  return arr[Math.floor(Math.random() * arr.length)];
}

function obtenerHoraPeru() {
  const horaStr = new Date().toLocaleString("en-US", {
    timeZone: "America/Lima",
    hour: "2-digit",
    hour12: false,
  });
  return parseInt(horaStr, 10);
}

// ============================================================================
// ============================================================================
//   6. MÓDULO PROMOCIONES
// ============================================================================
// ============================================================================
const MAX_ITEMS = 10;

const STICKERS_PROMO = [
  "https://firebasestorage.googleapis.com/v0/b/geinzworkapp.appspot.com/o/STIKER%2Fmagnific__the-character-is-leaning-back-arms-crossed-with-a__44769-Photoroom.webp?alt=media&token=6a29a532-58f0-46c2-ba26-72893472bb10",
  "https://firebasestorage.googleapis.com/v0/b/geinzworkapp.appspot.com/o/STIKER%2Fmagnific__con-fondo-blanco-con-el-personaje-mismo-cambiale-l__50004%20(1)%20(1)-convertido-de-png.webp?alt=media&token=b3bcf074-61d6-4ce4-a038-305296dc2b27",
  "https://firebasestorage.googleapis.com/v0/b/geinzworkapp.appspot.com/o/STIKER%2Fmagnific__-the-character-is-giving-a-thumbs-up-with-one-hand__44763-Photoroom.webp?alt=media&token=16b2df96-b727-4c6f-b9f2-fb0841d04ff0",
];

function construirSystemPromptExtractorPromos(contextoUsuario) {
  return `# CONTEXTO PREVIO
contexto: ${JSON.stringify(contextoUsuario || {})}

# TAREA
Extrae datos del mensaje del usuario. Responde SOLO con JSON válido, sin texto adicional.

# USO DEL CONTEXTO
+ Si contexto.tipo es "TURISMO", ignora el nombre/id/categoria heredados del contexto (no son una tienda), pero sigue extrayendo normalmente cualquier dato nuevo que el usuario mencione en el mensaje actual (tienda, productos, etc.).
- Usa contexto previo solo si el mensaje actual lo complementa o busca continuidad.
- Ignora el contexto si el usuario cambia de tema o lo contradice.
- "tienda" del contexto: úsala SOLO si el usuario menciona explícitamente esa tienda o pide algo de ella. Si pide un producto genérico, tienda = null.

# CAMPOS
{
  "tipo": "bot" | "geinz",
  "tienda": string | null,
  "productos": string[],
  "precio_max": int | null,
  "metodos_pago": string[],
  "comodidades": string[],
  "traer_promos": boolean
}

# REGLAS
- tienda: si el usuario menciona CUALQUIER nombre propio (de persona, marca o negocio) sin importar si "suena" a nombre de tienda o no, captúralo tal cual en "tienda" (texto limpio, sin diminutivos). NO necesitas confirmar que existe, solo extrae lo que el usuario dijo.
- productos: corregir ortografía, sin diminutivos, no inventar, no duplicar.
- comodidades: detectar menciones implícitas (ej: "con wifi" → "wifi").
- Si tipo es "bot" y hay productos → buscar promociones relevantes.
- Sin campo vacío: usa null o [] según corresponda.
- metodos_pago: solo de ["yape","plin","efectivo","agora","visa","mastercard"]
- comodidades: solo de ["aire_acondicionado","camaras_de_seguridad","enchufe","estacionamiento","ingreso_mascotas","mesa_para_ninos","sala_de_espera","sala_juegos","servicios_higienicos","wifi","zona_expandida"]
- traer_promos: true SOLO si pide promos/ofertas/descuentos de forma general (cualquier sinónimo), sin dar tienda, producto, precio, pago ni comodidad. Si da algo específico → false.
# EJEMPLOS CRÍTICOS (patrones que suelen fallar)
Mensaje: "dame las promos de marita" → {"tienda":"marita","traer_promos":false,...}
Mensaje: "que promos tiene chifa central" → {"tienda":"chifa central","traer_promos":false,...}
Mensaje: "dame promos" → {"tienda":null,"traer_promos":true,...}
Mensaje: "tienes descuentos?" → {"tienda":null,"traer_promos":true,...}
REGLA CLAVE: si después de "promos/ofertas/descuentos" aparece "de [algo]", "en [algo]" o "de la tienda X", SIEMPRE captura ese algo como "tienda" y traer_promos=false, sin importar que la palabra "promos" aparezca en la oración.`;
}

function normalizarTextoPromo(valor) {
  if (Array.isArray(valor)) {
    const encontrado = valor.find((v) => v != null && String(v).trim() !== "");
    return encontrado ? String(encontrado).trim() : "";
  }
  return valor != null ? String(valor).trim() : "";
}

function normalizarFiltrosPromocion(parsed) {
  const tienda = normalizarTextoPromo(parsed.tienda ?? parsed.Tienda);
  const tipo_ = parsed.tipo || null;
  const productos = Array.isArray(parsed.productos) ? parsed.productos : [];
  const precio_max =
    typeof parsed.precio_max === "number" ? parsed.precio_max : null;
  const metodos_pago = Array.isArray(parsed.metodos_pago)
    ? parsed.metodos_pago
    : [];
  const comodidades = Array.isArray(parsed.comodidades)
    ? parsed.comodidades
    : [];

  const traer_promos_raw = parsed.traer_promos;
  const traer_promos =
    traer_promos_raw === true ||
    String(traer_promos_raw).trim().toLowerCase() === "true";

  const todoVacio =
    !tienda &&
    productos.length === 0 &&
    precio_max === null &&
    metodos_pago.length === 0 &&
    comodidades.length === 0;

  const traerPromosFinal = traer_promos && todoVacio;

  return {
    tipo: tipo_,
    tienda,
    productos,
    precio_max,
    metodos_pago,
    comodidades,
    traer_promos: traerPromosFinal,
    preguntar_mejor: todoVacio && !traerPromosFinal,
  };
}

async function extraerFiltrosPromocion(mensajeUsuario, contextoUsuario) {
  console.log(
    "🧾 [extraerFiltrosPromocion] Contexto recibido:",
    JSON.stringify(contextoUsuario),
    "| Mensaje:",
    mensajeUsuario,
  );
  const systemMessage = construirSystemPromptExtractorPromos(contextoUsuario);
  const completion = await openai.chat.completions.create({
    model: "gpt-5.4-nano",
    messages: [
      { role: "system", content: systemMessage },
      { role: "user", content: mensajeUsuario },
    ],
    response_format: { type: "json_object" },
  });

  const raw = completion.choices[0]?.message?.content || "{}";
  const tokens = {
    prompt_tokens: completion.usage?.prompt_tokens || 0,
    completion_tokens: completion.usage?.completion_tokens || 0,
    total_tokens: completion.usage?.total_tokens || 0,
  };

  let parsed;
  try {
    parsed = JSON.parse(raw);
  } catch (e) {
    console.error(
      "❌ [extraerFiltrosPromocion] Error parseando:",
      e.message,
      "| RAW:",
      raw,
    );
    parsed = {};
  }

  return { filtros: normalizarFiltrosPromocion(parsed), tokens };
}

async function buscarPromosEnAlgolia(filtros) {
  const nombreTienda = (filtros?.tienda || "")
    .toLowerCase()
    .trim()
    .slice(0, 100);

  // Hora Perú
  const horaPeru = obtenerHoraPeru();
  let horarioActual = "noche";
  if (horaPeru >= 6 && horaPeru < 12) horarioActual = "manana";
  else if (horaPeru >= 12 && horaPeru < 18) horarioActual = "tarde";

  const rawPrecio = filtros?.precio_max;
  const precioInput =
    rawPrecio != null && rawPrecio !== "" ? Number(rawPrecio) : null;
  const precioValido =
    Number.isFinite(precioInput) && precioInput >= 0 && precioInput <= 99999;

  const pagosQuery = Array.isArray(filtros?.metodos_pago)
    ? filtros.metodos_pago
        .slice(0, MAX_ITEMS)
        .map((p) => String(p).toLowerCase().trim())
        .filter(Boolean)
    : [];
  const comodidadesQuery = Array.isArray(filtros?.comodidades)
    ? filtros.comodidades
        .slice(0, MAX_ITEMS)
        .map((c) => String(c).toLowerCase().trim())
        .filter(Boolean)
    : [];
  const productosQuery = Array.isArray(filtros?.productos)
    ? filtros.productos
        .slice(0, MAX_ITEMS)
        .map((p) => String(p).toLowerCase().trim())
        .filter(Boolean)
    : [];

  const query = (nombreTienda || productosQuery.join(" ")).slice(0, 200);

  // ── Filtro clave de vigencia: solo promos cuyo timestamp_fin sea futuro
  //    EN ESTE INSTANTE. Las vencidas ni siquiera llegan como hits.
  const timestampFiltro = Date.now();
  const finalFilters = [
    `(horario_publicacion:${horarioActual} OR horario_publicacion:todo_dia)`,
    `timestamp_fin > ${timestampFiltro}`,
  ].join(" AND ");

  console.log("🧩 Filtros:", finalFilters, "| 🔎 Query:", query);

  let response;
  try {
    response = await Promise.race([
      index_Algolia_promos.search(query, {
        filters: finalFilters,
        hitsPerPage: 20,
        getRankingInfo: true,
        optionalWords: query,
        removeWordsIfNoResults: "allOptional",
      }),
      new Promise((_, reject) =>
        setTimeout(() => reject(new Error("Algolia timeout")), 6000),
      ),
    ]);
  } catch (algoliaError) {
    console.error("❌ Error Algolia:", algoliaError.message);
    return {
      momento_dia: horarioActual,
      pago_exacto_encontrado: null,
      precio_exacto_encontrado: null,
      comodidad_exacta_encontrada: null,
      resultados: [],
      resultados_alternativos: [],
      total: 0,
      hitsMap: new Map(),
      aviso: "sin_resultados",
    };
  }

  console.log("📦 Hits encontrados:", response.hits.length);

  // Mapa de hits originales (con timestamp_fin) para O(1) lookup y para
  // la revalidación final de vigencia.
  const hitsMap = new Map(response.hits.map((h) => [h.objectID, h]));

  const calcularScore = (h) => {
    const textScore = h._rankingInfo?.nbTypos === 0 ? 40 : 20;

    const terminosDB = (h.terminos_clave || []).map((t) =>
      t.toLowerCase().trim(),
    );
    const matchCount = productosQuery.filter((p) =>
      terminosDB.some((t) => t.includes(p) || p.includes(t)),
    ).length;
    const matchScore =
      productosQuery.length > 0
        ? Math.round((matchCount / productosQuery.length) * 20)
        : 0;

    let precioScore = 0;
    let tienePrecioExacto = false;

    if (precioValido) {
      const dentroRango =
        precioInput >= h.precioMin && precioInput <= h.precioMax;
      if (dentroRango) {
        precioScore = 20;
        tienePrecioExacto = true;
      } else {
        const diff = Math.min(
          Math.abs(precioInput - (h.precioMin || 0)),
          Math.abs(precioInput - (h.precioMax || 0)),
        );
        precioScore = Math.max(0, 20 - diff * 1.5);
      }
    }

    const pagosDB = (h.pagos || []).map((p) => p.toLowerCase());
    const pagoMatch = pagosQuery.filter((p) => pagosDB.includes(p)).length;
    const pagoScore =
      pagosQuery.length > 0
        ? Math.round((pagoMatch / pagosQuery.length) * 10)
        : 0;
    const tienePagoExacto =
      pagosQuery.length > 0 && pagoMatch === pagosQuery.length;

    const comodDB = (h.comodidades || []).map((c) => c.toLowerCase());
    const comodMatch = comodidadesQuery.filter((c) =>
      comodDB.includes(c),
    ).length;
    const comodScore =
      comodidadesQuery.length > 0
        ? Math.round((comodMatch / comodidadesQuery.length) * 10)
        : 0;
    const tieneComodidadExacta =
      comodidadesQuery.length > 0 && comodMatch === comodidadesQuery.length;

    const totalScore = Math.min(
      100,
      Math.round(textScore + matchScore + precioScore + pagoScore + comodScore),
    );

    return {
      matchCount,
      totalScore,
      tienePagoExacto,
      pagosDB,
      tienePrecioExacto,
      precio: h.precio ?? null,
      tieneComodidadExacta,
    };
  };

  let resultados = response.hits.map((h) => {
    const s = calcularScore(h);
    return {
      id: h.objectID,
      score: s.totalScore,
      matchCount: s.matchCount,
      tienePagoExacto: s.tienePagoExacto,
      pagos_disponibles: s.pagosDB,
      tienePrecioExacto: s.tienePrecioExacto,
      precio: s.precio,
      tieneComodidadExacta: s.tieneComodidadExacta,
      descripcion: (h.descripcion || "").slice(0, 120),
      name_tienda: h.nombre_tienda || "",
      id_tienda: h.id_tienda || "", // FIX: antes nunca se propagaba y siempre salía ""
      img: h.imagen_promo || "",
    };
  });

  resultados.sort((a, b) => b.score - a.score);

  let pool = resultados;
  if (productosQuery.length > 1) {
    const usados = new Set();
    const porProducto = [];
    for (const producto of productosQuery) {
      const mejor = pool.find((r) => {
        if (usados.has(r.id)) return false;
        const hit = hitsMap.get(r.id);
        const terminos = (hit?.terminos_clave || []).map((t) =>
          t.toLowerCase().trim(),
        );
        return terminos.some(
          (t) => t.includes(producto) || producto.includes(t),
        );
      });
      if (mejor) {
        usados.add(mejor.id);
        porProducto.push(mejor);
        console.log(`✅ Producto "${producto}" → hit ${mejor.id}`);
      } else {
        console.log(`⚠️ Producto "${producto}" → sin match`);
      }
    }
    pool = porProducto;
  } else {
    pool = pool.slice(0, 3);
  }

  const clasificar = (r) =>
    (pagosQuery.length === 0 || r.tienePagoExacto) &&
    (precioInput === null || r.tienePrecioExacto) &&
    (comodidadesQuery.length === 0 || r.tieneComodidadExacta);

  const exactos = pool.filter((r) => clasificar(r));
  const alternativos = pool.filter((r) => !clasificar(r));
  const hayExactos = exactos.length > 0;

  const pago_exacto_encontrado =
    pagosQuery.length > 0 ? pool.some((r) => r.tienePagoExacto) : null;
  const precio_exacto_encontrado = precioValido
    ? pool.some((r) => r.tienePrecioExacto)
    : null;
  const comodidad_exacta_encontrada =
    comodidadesQuery.length > 0
      ? pool.some((r) => r.tieneComodidadExacta)
      : null;

  // FIX: antes esta función descartaba tienePagoExacto/tienePrecioExacto
  // por completo, así que nunca llegaban a Gemini como p_ok/pr_ok reales
  // (siempre salían vacíos). Ahora se conservan renombrados.
  const limpiar = ({
    tieneComodidadExacta,
    matchCount,
    tienePagoExacto,
    tienePrecioExacto,
    ...rest
  }) => ({
    ...rest,
    p_ok: tienePagoExacto,
    pr_ok: tienePrecioExacto,
  });

  return {
    momento_dia: horarioActual,
    pago_exacto_encontrado,
    precio_exacto_encontrado,
    comodidad_exacta_encontrada,
    resultados: hayExactos ? exactos.map(limpiar) : alternativos.map(limpiar),
    resultados_alternativos: hayExactos ? alternativos.map(limpiar) : [],
    total: hayExactos ? exactos.length : alternativos.length,
    hitsMap,
  };
}
async function buscarPromosRandomVigentes({ cantidad = 3 } = {}) {
  const horaPeru = obtenerHoraPeru();
  let horarioActual = "noche";
  if (horaPeru >= 6 && horaPeru < 12) horarioActual = "manana";
  else if (horaPeru >= 12 && horaPeru < 18) horarioActual = "tarde";

  const timestampFiltro = Date.now();
  const finalFilters = [
    `(horario_publicacion:${horarioActual} OR horario_publicacion:todo_dia)`,
    `timestamp_fin > ${timestampFiltro}`,
  ].join(" AND ");

  console.log("🎲 [buscarPromosRandomVigentes] Filtros:", finalFilters);

  let response;
  try {
    response = await Promise.race([
      index_Algolia_promos.search("", {
        filters: finalFilters,
        hitsPerPage: 100,
      }),
      new Promise((_, reject) =>
        setTimeout(() => reject(new Error("Algolia timeout")), 6000),
      ),
    ]);
  } catch (algoliaError) {
    console.error(
      "❌ [buscarPromosRandomVigentes] Error Algolia:",
      algoliaError.message,
    );
    return { momento_dia: horarioActual, resultados: [], hitsMap: new Map() };
  }

  const hitsMap = new Map(response.hits.map((h) => [h.objectID, h]));

  const seleccionados = [...response.hits]
    .sort(() => Math.random() - 0.5)
    .slice(0, cantidad);

  const resultados = seleccionados.map((h) => ({
    id: h.objectID,
    score: 100,
    matchCount: 0,
    tienePagoExacto: false,
    pagos_disponibles: (h.pagos || []).map((p) => p.toLowerCase()),
    tienePrecioExacto: false,
    precio: h.precio ?? null,
    tieneComodidadExacta: false,
    descripcion: (h.descripcion || "").slice(0, 120),
    name_tienda: h.nombre_tienda || "",
    id_tienda: h.id_tienda || "",
    img: h.imagen_promo || "",
  }));

  console.log(
    `🎲 [buscarPromosRandomVigentes] ${response.hits.length} vigentes → ${resultados.length} elegidas`,
  );

  return { momento_dia: horarioActual, resultados, hitsMap };
}

function comprimirResultadoPromo(r) {
  const obj = {
    id: r.id,
    sc: r.score,
    t: r.name_tienda,
    desc: r.descripcion,
    pagos: r.pagos_disponibles,
    p_ok: r.p_ok,
    pr_ok: r.pr_ok,
  };
  if (r.precio != null && r.precio !== "") obj.precio = r.precio;
  return obj;
}

function limpiarResultadoPromos(raw) {
  return {
    momento: raw.momento_dia,
    p_ok: raw.pago_exacto_encontrado,
    pr_ok: raw.precio_exacto_encontrado,
    co_ok: raw.comodidad_exacta_encontrada,
    resultados: (raw.resultados || []).map(comprimirResultadoPromo),
    alt: (raw.resultados_alternativos || []).map(comprimirResultadoPromo),
    total: raw.total,
  };
}

function normalizarPagos(pagos) {
  if (Array.isArray(pagos)) return [...pagos].sort().join(",");
  return pagos || "";
}

function prepararResultados(lista, maxItems = 8, maxDescLen = 120) {
  if (!Array.isArray(lista)) return [];
  return lista.slice(0, maxItems).map((r) => ({
    id: r.id ?? "",
    sc: r.sc ?? "",
    t: r.t ?? "",
    desc: (r.desc || "").toString().slice(0, maxDescLen),
    pagos: normalizarPagos(r.pagos),
    precio: r.precio ?? "",
    como: r.como ?? "",
    p_ok: r.p_ok === true ? "true" : r.p_ok === false ? "false" : "",
    pr_ok: r.pr_ok === true ? "true" : r.pr_ok === false ? "false" : "",
  }));
}

function agruparPorTiendaYPago(lista) {
  const grupos = {};
  for (const r of lista) {
    const key = `${r.t}||${r.pagos}`;
    if (!grupos[key]) grupos[key] = { t: r.t, pagos: r.pagos, items: [] };
    grupos[key].items.push({
      id: r.id,
      sc: r.sc,
      desc: r.desc,
      precio: r.precio,
      como: r.como,
      p_ok: r.p_ok,
      pr_ok: r.pr_ok,
    });
  }
  return Object.values(grupos);
}

function compactar(lista) {
  if (!lista.length) return "ninguna";
  const grupos = agruparPorTiendaYPago(lista);
  return grupos
    .map((g) => {
      const header = `TIENDA:${g.t}|PAGOS:${g.pagos || "no_especificado"}`;
      const items = g.items
        .map(
          (it) =>
            `  ${it.id}~${it.sc}~${it.desc}~${it.precio}~${it.como}~${it.p_ok}~${it.pr_ok}`,
        )
        .join("\n");
      return `${header}\n${items}`;
    })
    .join("\n\n");
}

function construirPromptPromo(
  momento,
  nombreUsuario,
  resultados,
  alt,
  mensajeUsuario,
) {
  return `Informador peruano. Elige la mejor promo.

FORMATO: TIENDA:nombre|PAGOS:métodos
  id~sc~desc~precio~como~p_ok~pr_ok
(sc=score,mayor mejor | precio=soles | p_ok/pr_ok=match pago/precio pedido,true/false/vacío | vacío=no filtró eso)

Momento:${momento} | Usuario:${nombreUsuario}
Pidió: "${mensajeUsuario || "nada, usa datos pre-filtrados"}"

PROMOS:
${compactar(resultados)}

ALT:
${compactar(alt)}

REGLAS: usa solo estos datos, no inventes. Misma tienda=compara y elige mejor calce. NUNCA digas frases como "mensaje predeterminado", "mensaje genérico", "esto es automático" ni nada que describa la naturaleza de tu propia respuesta. p_ok=false avisa y ofrece alt con pago real...
DECISIÓN: 2+ relevantes→varios=true+ids | 1→varios=false+id | 0→varios=false,id="none"`;
}

async function elegirMejorPromoConGemini({
  momento,
  nombreUsuario,
  mensajeUsuario,
  resultados,
  alt,
}) {
  const inicioTiempo = Date.now();
  const MAX_MENSAJE_CHARS = 220;

  const resultadosFiltrados = prepararResultados(resultados, 8, 120);
  const altFiltrada = prepararResultados(alt, 4, 120);

  const prompt = construirPromptPromo(
    momento || "",
    nombreUsuario || "👋",
    resultadosFiltrados,
    altFiltrada,
    mensajeUsuario || "",
  );

  const body = {
    contents: [{ parts: [{ text: prompt }] }],
    generationConfig: {
      responseMimeType: "application/json",
      responseSchema: {
        type: "object",
        properties: {
          tipo: { type: "string" },
          varios: { type: "boolean" },
          id: { type: "string" },
          ids: { type: "array", items: { type: "string" } },
          mensaje: { type: "string" },
        },
        required: ["tipo", "varios", "mensaje"],
      },
      thinkingConfig: { thinkingBudget: 0 },
      maxOutputTokens: 220,
      temperature: 0.7,
    },
  };

  const fallback = {
    data: {
      tipo: "bot",
      varios: false,
      id: "none",
      mensaje: "No pude procesar las promociones en este momento.",
    },
    _debug: null,
  };

  let geminiRes;
  try {
    geminiRes = await fetch(`${GEMINI_URL}?key=${GEMINIKEY}`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(body),
    });
  } catch (err) {
    console.error("❌ Error de red llamando a Gemini:", err.message);
    return fallback;
  }

  if (!geminiRes.ok) {
    const errText = await geminiRes.text();
    console.error("❌ Error Gemini API:", geminiRes.status, errText);
    return fallback;
  }

  const geminiData = await geminiRes.json();
  const usage = geminiData?.usageMetadata || {};
  const promptTokens = usage.promptTokenCount ?? 0;
  const respuestaTokens = usage.candidatesTokenCount ?? 0;
  const pensamientoTokens = usage.thoughtsTokenCount ?? 0;
  const totalTokens = usage.totalTokenCount ?? 0;
  const tiempoMs = Date.now() - inicioTiempo;

  console.log(
    `📊 TOKENS | prompt: ${promptTokens} | respuesta: ${respuestaTokens} | thinking: ${pensamientoTokens} | TOTAL: ${totalTokens} | tiempo: ${tiempoMs}ms | usuario: ${nombreUsuario} | promos_enviadas: ${resultadosFiltrados.length} | alt_enviadas: ${altFiltrada.length}`,
  );

  if (respuestaTokens >= body.generationConfig.maxOutputTokens - 10) {
    console.warn(
      "⚠️ Respuesta posiblemente truncada por maxOutputTokens, revisar prompt o subir el tope",
    );
  }

  const rawText =
    geminiData?.candidates?.[0]?.content?.parts?.[0]?.text || "{}";

  let resultado;
  try {
    resultado = JSON.parse(rawText);
  } catch (e) {
    console.error(
      "❌ Error parseando respuesta de Gemini:",
      e.message,
      "| RAW:",
      rawText,
    );
    return fallback;
  }

  if (
    typeof resultado.mensaje === "string" &&
    resultado.mensaje.length > MAX_MENSAJE_CHARS
  ) {
    console.warn(
      `✂️ Mensaje recortado: ${resultado.mensaje.length} chars → ${MAX_MENSAJE_CHARS}`,
    );
    resultado.mensaje =
      resultado.mensaje.slice(0, MAX_MENSAJE_CHARS).trim() + "...";
  }

  return {
    data: resultado,
    _debug: {
      tokens: {
        prompt: promptTokens,
        respuesta: respuestaTokens,
        thinking: pensamientoTokens,
        total: totalTokens,
      },
      tiempoMs,
    },
  };
}

function construirRespuestaUnica({ id, mensaje, promosArray, siker }) {
  const promoEncontrada = promosArray.find((p) => String(p?.id) === String(id));
  const imagen = promoEncontrada?.img || "";
  const mensaje_safe = mensaje
    ? `${mensaje} Encuéntralo aquí: https://geinztech.com/api/share?t=prms&l=ba&pi=${id}`
    : "Sin mensaje";

  return {
    varios: false,
    id,
    imagen,
    mensaje: mensaje || "",
    mensaje_safe,
    siker,
    data: {
      tipo: "PROMOCIONES",
      nombre: promoEncontrada?.name_tienda || "",
      categoria: null,
      extra: "le mande una promo al usuario",
      id: promoEncontrada?.id_tienda || "",
      ids_promos: [id],
    },
  };
}

function respuestaVaciaPromo(siker, motivo) {
  return {
    varios: false,
    id: "",
    imagen: "",
    mensaje: "",
    mensaje_safe: "",
    siker,
    data: {
      tipo: "PROMOCIONES",
      nombre: "",
      categoria: null,
      extra: motivo,
      id: "",
      ids_promos: [],
    },
  };
}

function construirResultadoFinalPromo({ respuestaIA, promosArray, hitsMap }) {
  const response = respuestaIA?.data || {};
  const siker = pick(STICKERS_PROMO);
  const ahora = Date.now();

  const esVigente = (id) => {
    const hit = hitsMap?.get(String(id));
    return !!hit && Number(hit.timestamp_fin) > ahora;
  };

  if (response.varios === true) {
    const idsUnicos = [
      ...new Set((Array.isArray(response.ids) ? response.ids : []).map(String)),
    ];

    const idsValidos = idsUnicos.filter((id) => {
      const ok = esVigente(id);
      if (!ok)
        console.warn(
          `⏳ Promo ${id} descartada en el check final (vencida o inexistente)`,
        );
      return ok;
    });

    if (idsValidos.length === 0) {
      return respuestaVaciaPromo(
        siker,
        "todas las promociones elegidas vencieron o ya no existen",
      );
    }

    if (idsValidos.length === 1) {
      return construirRespuestaUnica({
        id: idsValidos[0],
        mensaje: response.mensaje,
        promosArray,
        siker,
      });
    }

    const promosEncontradas = idsValidos
      .map((id) => promosArray.find((p) => String(p?.id) === id))
      .filter(Boolean);
    const imagen = pick(promosEncontradas)?.img || ""; // 👈 cambio: random en vez de [0]
    const mensaje_safe = response.mensaje
      ? `${response.mensaje} Encuéntralo aquí: https://geinztech.com/api/share?t=pmspls&l=ba&p=${idsValidos.join(",")}`
      : "Sin mensaje";

    return {
      varios: true,
      ids: idsValidos,
      imagen,
      mensaje: response.mensaje || "",
      mensaje_safe,
      siker,
      data: {
        tipo: "PROMOCIONES",
        nombre: promosEncontradas[0]?.name_tienda || "",
        categoria: null,
        extra: "le mande el usuario varias promociones",
        id: promosEncontradas[0]?.id_tienda || "",
        ids_promos: idsValidos,
      },
    };
  }

  // Caso simple (una sola promo)
  if (!response.id || response.id === "none" || !esVigente(response.id)) {
    if (response.id && response.id !== "none") {
      console.warn(
        `⏳ Promo ${response.id} descartada en el check final (vencida o inexistente)`,
      );
    }
    return respuestaVaciaPromo(
      siker,
      "no se encontró una promoción vigente para ofrecer",
    );
  }

  return construirRespuestaUnica({
    id: response.id,
    mensaje: response.mensaje,
    promosArray,
    siker,
  });
}

async function procesarPromociones({
  mensaje,
  contexto_previo,
  nombre_usuario,
}) {
  console.log(
    "🎁 [procesarPromociones] INICIO | mensaje:",
    mensaje,
    "| contexto_previo:",
    JSON.stringify(contexto_previo),
  );
  const { filtros, tokens: tokensExtractor } = await extraerFiltrosPromocion(
    mensaje,
    contexto_previo,
  );

  console.log(
    "🧪 [procesarPromociones] FILTROS EXTRAIDOS:",
    JSON.stringify(filtros),
  ); // 👈 nuevo

  if (filtros.tipo === "geinz") {
    return {
      preguntar_mejor: true,
      tokens_usados: { extractor: tokensExtractor },
    };
  }

  // 👇 NUEVO: pidió promos/ofertas de forma general → trae 3 random vigentes
  if (filtros.traer_promos === true) {
    const promosRandomRaw = await buscarPromosRandomVigentes({ cantidad: 3 });

    if (promosRandomRaw.resultados.length === 0) {
      return {
        preguntar_mejor: false,
        sin_resultados: true,
        referencia: "promociones",
        tipo_referencia: "categoria",
        tokens_usados: { extractor: tokensExtractor },
      };
    }

    const promosLimpiasRandom = limpiarResultadoPromos({
      momento_dia: promosRandomRaw.momento_dia,
      pago_exacto_encontrado: null,
      precio_exacto_encontrado: null,
      comodidad_exacta_encontrada: null,
      resultados: promosRandomRaw.resultados,
      resultados_alternativos: [],
      total: promosRandomRaw.resultados.length,
    });

    const respuestaIA = await elegirMejorPromoConGemini({
      momento: promosLimpiasRandom.momento,
      nombreUsuario: nombre_usuario,
      mensajeUsuario: mensaje,
      resultados: promosLimpiasRandom.resultados,
      alt: [],
    });

    const resultadoFinal = construirResultadoFinalPromo({
      respuestaIA,
      promosArray: promosRandomRaw.resultados,
      hitsMap: promosRandomRaw.hitsMap,
    });

    return {
      ...resultadoFinal,
      preguntar_mejor: false,
      sin_resultados: false,
      tokens_usados: {
        extractor: tokensExtractor,
        elegir_promo: respuestaIA?._debug?.tokens || null,
      },
    };
  }

  if (filtros.preguntar_mejor) {
    return {
      preguntar_mejor: true,
      tokens_usados: { extractor: tokensExtractor },
    };
  }

  const promosRaw = await buscarPromosEnAlgolia(filtros);
  const promosLimpias = limpiarResultadoPromos(promosRaw);

  if (promosLimpias.resultados.length === 0) {
    const tieneTienda = !!filtros.tienda;
    const tieneProductos =
      Array.isArray(filtros.productos) && filtros.productos.length > 0;

    if (tieneTienda) {
      return {
        preguntar_mejor: false,
        sin_resultados: true,
        referencia: filtros.tienda,
        tipo_referencia: "tienda",
        tokens_usados: { extractor: tokensExtractor },
      };
    }

    if (tieneProductos) {
      return {
        preguntar_mejor: false,
        sin_resultados: true,
        referencia: filtros.productos.join(", "),
        tipo_referencia: "categoria",
        tokens_usados: { extractor: tokensExtractor },
      };
    }

    return {
      preguntar_mejor: true,
      tokens_usados: { extractor: tokensExtractor },
    };
  }
  const respuestaIA = await elegirMejorPromoConGemini({
    momento: promosLimpias.momento,
    nombreUsuario: nombre_usuario,
    mensajeUsuario: mensaje,
    resultados: promosLimpias.resultados,
    alt: promosLimpias.alt,
  });

  const promosArray = [
    ...promosRaw.resultados,
    ...promosRaw.resultados_alternativos,
  ];

  const resultadoFinal = construirResultadoFinalPromo({
    respuestaIA,
    promosArray,
    hitsMap: promosRaw.hitsMap,
  });

  return {
    ...resultadoFinal,
    preguntar_mejor: false,
    sin_resultados: false,
    tokens_usados: {
      extractor: tokensExtractor,
      elegir_promo: respuestaIA?._debug?.tokens || null,
    },
  };
}

exports.procesarPromociones = procesarPromociones;

// ============================================================================
// Endpoint HTTP independiente: elegir_mejor_promo
// (usa las MISMAS funciones de arriba: prepararResultados, agruparPorTiendaYPago,
// compactar, construirPromptPromo — en tu archivo original estaban duplicadas
// aquí abajo con el mismo nombre; las quité para que esto compile como
// módulo aparte sin choque de identificadores)
// ============================================================================

exports.elegir_mejor_promo = onRequest(async (req, res) => {
  const inicioTiempo = Date.now();

  try {
    const { momento, resultados, alt, nombre_usuario, mensaje_usuario } =
      req.body;

    if (!Array.isArray(resultados)) {
      return res.status(400).json({
        ok: false,
        error: "El campo 'resultados' es requerido y debe ser un array",
      });
    }

    const resultadosFiltrados = prepararResultados(resultados, 8, 120);
    const altFiltrada = prepararResultados(alt, 4, 120);

    const nombreUsuario = nombre_usuario || "👋";
    const prompt = construirPromptPromo(
      momento || "",
      nombreUsuario,
      resultadosFiltrados,
      altFiltrada,
      mensaje_usuario || "",
    );

    const MAX_MENSAJE_CHARS = 220;

    const body = {
      contents: [{ parts: [{ text: prompt }] }],
      generationConfig: {
        responseMimeType: "application/json",
        responseSchema: {
          type: "object",
          properties: {
            tipo: { type: "string" },
            varios: { type: "boolean" },
            id: { type: "string" },
            ids: { type: "array", items: { type: "string" } },
            mensaje: { type: "string" },
          },
          required: ["tipo", "varios", "mensaje"],
        },
        thinkingConfig: { thinkingBudget: 0 },
        maxOutputTokens: 220,
        temperature: 0.7,
      },
    };

    const geminiRes = await fetch(`${GEMINI_URL}?key=${GEMINIKEY}`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(body),
    });

    if (!geminiRes.ok) {
      const errText = await geminiRes.text();
      console.error("❌ Error Gemini API:", geminiRes.status, errText);
      return res
        .status(502)
        .json({ ok: false, error: "Error al consultar Gemini" });
    }

    const geminiData = await geminiRes.json();

    const usage = geminiData?.usageMetadata || {};
    const promptTokens = usage.promptTokenCount ?? 0;
    const respuestaTokens = usage.candidatesTokenCount ?? 0;
    const pensamientoTokens = usage.thoughtsTokenCount ?? 0;
    const totalTokens = usage.totalTokenCount ?? 0;
    const tiempoMs = Date.now() - inicioTiempo;

    console.log(
      `📊 TOKENS | prompt: ${promptTokens} | respuesta: ${respuestaTokens} | thinking: ${pensamientoTokens} | TOTAL: ${totalTokens} | tiempo: ${tiempoMs}ms | usuario: ${nombreUsuario} | promos_enviadas: ${resultadosFiltrados.length} | alt_enviadas: ${altFiltrada.length}`,
    );

    if (respuestaTokens >= body.generationConfig.maxOutputTokens - 10) {
      console.warn(
        "⚠️ Respuesta posiblemente truncada por maxOutputTokens, revisar prompt o subir el tope",
      );
    }

    const rawText =
      geminiData?.candidates?.[0]?.content?.parts?.[0]?.text || "{}";

    let resultado;
    try {
      resultado = JSON.parse(rawText);
    } catch (e) {
      console.error(
        "❌ Error parseando respuesta de Gemini:",
        e.message,
        "| RAW:",
        rawText,
      );
      resultado = {
        tipo: "bot",
        varios: false,
        id: "none",
        mensaje: "No pude procesar las promociones en este momento.",
      };
    }

    if (
      typeof resultado.mensaje === "string" &&
      resultado.mensaje.length > MAX_MENSAJE_CHARS
    ) {
      console.warn(
        `✂️ Mensaje recortado: ${resultado.mensaje.length} chars → ${MAX_MENSAJE_CHARS}`,
      );
      resultado.mensaje =
        resultado.mensaje.slice(0, MAX_MENSAJE_CHARS).trim() + "...";
    }

    return res.status(200).json({
      ok: true,
      data: resultado,
      _debug: {
        tokens: {
          prompt: promptTokens,
          respuesta: respuestaTokens,
          thinking: pensamientoTokens,
          total: totalTokens,
        },
        tiempoMs,
      },
    });
  } catch (error) {
    console.error("❌ Error elegir_mejor_promo:", error.message);
    return res.status(500).json({ ok: false, error: error.message });
  }
});
