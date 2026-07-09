// ============================================================================
// ============================================================================
//   1. IMPORTS Y CONFIGURACIÓN GLOBAL
// ============================================================================
// ============================================================================

const { onRequest } = require("firebase-functions/v2/https");
const admin = require("firebase-admin");
const OpenAI = require("openai");
const algoliasearch = require("algoliasearch");
const { FieldValue } = require("firebase-admin/firestore");

const { obtener_creditos_tienda_fn } = require("./test_db2");

// ⚠️ OJO: en tu código original se usa `similarity.stringSimilarity(...)`
// dentro del fallback de búsqueda por nombre, pero `similarity` nunca se
// importa en este archivo. Si esa rama del fallback llega a ejecutarse,
// va a tirar "similarity is not defined". Falta algo como:
//   const similarity = require("./similarity"); // o el paquete que uses
// Avísame cuál usas y te lo dejo importado correctamente.
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

const CACHE_TTL_MS = 1000 * 60 * 30; // 30 minutos

const ATTRS_TIENDA = [
  "objectID",
  "nombre",
  "descripcion",
  "lugar",
  "categoria",
  "imagen_bot",
  "parecidas",
  "tag",
  "plantilla",
  "msje_whatsapp",
  "alias",
];

// ============================================================================
// ============================================================================
//   2. UTILIDADES GENERALES (usadas por varios módulos)
// ============================================================================
// ============================================================================

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

async function obtenerDatosPorIds(localidad, ids) {
  const ref = db.collection("Tiendas").doc(localidad).collection(localidad);
  const resultados = {};

  const size = 10;
  const chunks = [];
  for (let i = 0; i < ids.length; i += size) {
    chunks.push(ids.slice(i, i + size));
  }

  const promises = chunks.map((chunk) =>
    ref.where(admin.firestore.FieldPath.documentId(), "in", chunk).get(),
  );

  const snapshots = await Promise.all(promises);

  snapshots.forEach((snapshot) => {
    snapshot.forEach((doc) => {
      const data = doc.data();
      resultados[doc.id] = {
        horario: data.horario_atencion || null,
        whatsapp: data.metodo_contacto?.whatsapp?.numero || "",
      };
    });
  });

  return resultados;
}

function verificar_apertura_tienda(horario_atencion) {
  if (!horario_atencion) return null;

  const now = new Date();
  const peru = new Date(
    now.toLocaleString("en-US", { timeZone: "America/Lima" }),
  );

  const dias = [
    "domingo",
    "lunes",
    "martes",
    "miércoles",
    "jueves",
    "viernes",
    "sábado",
  ];

  const diaActual = dias[peru.getDay()];
  const minutosActual = peru.getHours() * 60 + peru.getMinutes();
  const horario = horario_atencion[diaActual];

  if (!horario || horario.cerrado === true) return false;

  const bloques = horario.bloques || [];

  for (const bloque of bloques) {
    if (!bloque || !bloque.h_apertura || !bloque.h_cierre) continue;

    const [ha, ma] = bloque.h_apertura.split(":").map(Number);
    const [hc, mc] = bloque.h_cierre.split(":").map(Number);
    const apertura = ha * 60 + ma;
    const cierre = hc * 60 + mc;

    if (apertura <= cierre) {
      if (minutosActual >= apertura && minutosActual <= cierre) return true;
    } else {
      if (minutosActual >= apertura || minutosActual <= cierre) return true;
    }
  }

  return false;
}

// ============================================================================
// ============================================================================
//   3. MÓDULO TIENDAS / NEGOCIOS — FLUJO COMPLETO EN UN SOLO ENDPOINT
//      Reemplaza: clasificador_geinz_categorias_negocios + buscar_por_nombre__tienda
//      + buscar_por_categoria_subcateogira_Actualizado + limpiador_json +
//      dispersador_IA2 + code node de armado final.
//      En n8n: WhatsApp Trigger -> HTTP Request (geinz_tienda_completo) -> listo.
// ============================================================================
// ============================================================================

// ----------------------------------------------------------------------------
// 3.1 Caché de categorías + subcategorías de negocios
// ----------------------------------------------------------------------------
// ----------------------------------------------------------------------------
// 3.1 Caché de categorías + subcategorías de negocios
// ----------------------------------------------------------------------------
// ----------------------------------------------------------------------------
// 3.1 Caché de categorías + subcategorías de negocios
// ----------------------------------------------------------------------------
let categoriasCache = null;
let categoriasCacheTimestamp = 0;

function parsearClasificacionIA(rawContent) {
  const defaults = {
    nombre: null,
    categoria: null,
    subcategoria: null,
    tipo: "tiendas",
    search: false,
    heredar_contexto: false,
    pregunta: false,
    registro: false,
    excluir_id: null,
  };

  if (!rawContent || typeof rawContent !== "string") return defaults;

  let parsed;
  try {
    parsed = JSON.parse(rawContent);
  } catch (e) {
    console.error("❌ Error parseando clasificación:", e.message, "| RAW:", rawContent.slice(0, 200));
    return defaults;
  }

  // 🔧 Normaliza campos: "null" string → null real
  const limpiarNull = (v) => {
    if (typeof v === "string" && v.trim().toLowerCase() === "null") return null;
    return v;
  };

  // 🔧 Normaliza booleanos que a veces llegan como string "true"/"false"
  const limpiarBool = (v) => {
    if (typeof v === "boolean") return v;
    if (typeof v === "string") return v.trim().toLowerCase() === "true";
    return false;
  };

  return {
    nombre: limpiarNull(parsed.nombre) || null,
    categoria: limpiarNull(parsed.categoria) || null,
    subcategoria: limpiarNull(parsed.subcategoria) || null,
    tipo: parsed.tipo || "tiendas",
    search: limpiarBool(parsed.search),
    heredar_contexto: limpiarBool(parsed.heredar_contexto),
    pregunta: limpiarBool(parsed.pregunta),
    registro: limpiarBool(parsed.registro),
    excluir_id: limpiarNull(parsed.excluir_id) || null,
  };
}
async function obtenerCategoriasConSub() {
  const ahora = Date.now();
  if (categoriasCache && ahora - categoriasCacheTimestamp < CACHE_TTL_MS) {
    console.log("♻️ Usando categorías+subcategorias (negocios) desde cache");
    return categoriasCache;
  }

  console.log("🔄 Refrescando categorías+subcategorias (negocios) desde Firestore");
  const snapshot = await db
    .collection("Tiendas")
    .doc("categorias")
    .collection("categorias")
    .get();

  const lista = [];
  const mapaSub = {};

  snapshot.forEach((doc) => {
    const id = doc.id.toLowerCase();
    if (id === "turismo") return; // turismo se maneja aparte
    lista.push(doc.id);
    mapaSub[id] = doc.get("subcategorias") || [];
  });

  categoriasCache = { lista, mapaSub };
  categoriasCacheTimestamp = ahora;
  return categoriasCache;
}

// ----------------------------------------------------------------------------
// 3.2a Prompt 1: CLASIFICADOR (solo nombres de categorías, SIN subcategorías)
// Decide: nombre propio vs categoria. NO decide subcategoria todavía.
// ----------------------------------------------------------------------------
function construirPromptClasificacion(mensaje, contextoPrevio, lista) {
  const contextoRaw = contextoPrevio?.contexto_usuario ?? contextoPrevio;

  const contextoStr =
    contextoRaw === undefined || contextoRaw === null
      ? "null"
      : typeof contextoRaw === "string"
        ? contextoRaw
        : JSON.stringify(contextoRaw);

  const categoriasStr = lista.join(",");

  return `contexto anterior del usuario es: ${contextoStr}
Responde SOLO con JSON válido:
{"nombre":string|null,"categoria":string|null,"subcategoria":null,"tipo":"tiendas","search":boolean,"heredar_contexto":boolean,"pregunta":boolean,"registro":boolean,"excluir_id":string|null}
IMPORTANTE: cuando un campo no aplique, usa el valor JSON null (sin comillas). NUNCA escribas el texto "null" entre comillas como si fuera un string.
CATEGORIAS: ${categoriasStr}
REGLAS DE SEGURIDAD (Prioridad Máxima):
1. Si detectas entidades de auxilio públicas (serenazgo, policia, comisaria, bomberos, samu, hospital, posta, ambulancia) o palabras de crisis (robo, auxilio, fuego, choque) → categoria="emergencia"
REGLAS:
- Detecta palabras de jerga peruana
- Si el mensaje no tiene relación con ninguna categoría → categoria="geinz"
- Negación (no/menos/excepto/evitar + nombre) → ignorar ese nombre
- Nombre propio (marca/negocio/persona) → nombre=texto limpio, categoria=null
- Sin nombre → inferir categoría más cercana de la lista
- search=true solo si pide oferta/descuento de un nombre propio, sino false
- Usar contexto anterior solo si es relevante al mensaje actual
- Si el usuario cambia de tema → ignorar contexto completamente
- pregunta=true SOLO si pide más info de un lugar YA mencionado en el contexto (ej: "tiene estacionamiento?"). Si es búsqueda nueva, pregunta=false
- registro=true SOLO si el usuario quiere registrar su negocio/tienda/empresa/local/emprendimiento, sino false
- Si el mensaje es de rechazo o continuación ("no quiero ese", "otro", "otro sitio", "muéstrame más", "no me gustó", "dame otro") → heredar_contexto=true y categoria=EXACTAMENTE la misma categoria del contexto anterior. En cualquier otro caso heredar_contexto=false
- excluir_id: null salvo que aplique la regla de rechazo/continuación
- Normalizar: minúsculas, sin tildes, sin símbolos

MENSAJE DEL USUARIO: "${mensaje}"`;
}
// ----------------------------------------------------------------------------
// 3.2b Prompt 2: SELECTOR DE SUBCATEGORIA
// Solo se llama si hay categoria, NO hay nombre y NO se hereda del contexto.
// Respuesta en TEXTO PLANO (no JSON), una sola línea.
// Incluye fallback: si detecta que en realidad es un nombre de negocio,
// responde "NEGOCIO: [nombre normalizado]" y el endpoint reenruta a búsqueda por nombre.
// ----------------------------------------------------------------------------
function construirPromptSubcategoria(mensaje, contextoPrevio, subcategorias) {
  const contextoRaw = contextoPrevio?.contexto_usuario ?? contextoPrevio;

  const contextoStr =
    contextoRaw === undefined || contextoRaw === null
      ? "null"
      : typeof contextoRaw === "string"
        ? contextoRaw
        : JSON.stringify(contextoRaw);

  return `CONTEXTO DEL USUARIO:
${contextoStr}

LISTA DE SUBCATEGORÍAS:
${subcategorias.join(", ")}

TAREA:
Selecciona UNA ÚNICA subcategoría de la LISTA que mejor corresponda a la intención del usuario.

REGLAS (en orden de prioridad):
- Si el mensaje es de rechazo o continuación ("no quiero ese", "otro", "otro sitio", "muéstrame más", "no me gustó", "dame otro") → heredar EXACTAMENTE la misma sub del CONTEXTO, sin cambiar nada
- Usa el CONTEXTO para reforzar la elección si el mensaje actual es ambiguo
- Si el contexto tiene subcategoría activa y el mensaje es continuación → heredar esa subcategoría, sino escoge una de la LISTA y evita el contexto
REGLAS ESTRICTAS:
- Responder ÚNICAMENTE 1 solo valor de la LISTA
- PROHIBIDO responder con comas, listas, ni múltiples valores
- Texto EXACTO como aparece en la LISTA (sin cambios, sin mayúsculas extra)
- No inventar valores fuera de la LISTA
- Si dudas → elegir LA MÁS CERCANA semánticamente
- Ignorar errores ortográficos e interpretar semánticamente
- Si el mensaje contiene nombre de negocio o marca → responde EXACTAMENTE: NEGOCIO: [nombre normalizado]
- Tu respuesta debe ser una sola línea, sin comas, sin explicaciones

MENSAJE DEL USUARIO: "${mensaje}"`;
}

// ----------------------------------------------------------------------------
// 3.3 Búsqueda por NOMBRE de tienda (función interna, basada en buscar_por_nombre__tienda)
// ----------------------------------------------------------------------------
async function buscarPorNombreTienda({ localidad, nombre, search }) {
  console.log("🔍 [buscar_tienda] Parámetros recibidos:", { localidad, nombre, search });
 
  const ATTRS_NOMBRE = [
    "objectID",
    "nombre",
    "descripcion",
    "lugar",
    "categoria",
    "imagen_bot",
    "parecidas",
    "tag",
    "plantilla",
    "msje_whatsapp",
    "alias",
  ];
 
  const filters = [];
  if (localidad) filters.push(`lugar:"${localidad}"`);
  filters.push(`NOT categoria:"turismo"`);
  filters.push(`NOT categoria:"salud"`);
  console.log("🔧 [buscar_tienda] Filtros aplicados:", filters);
 
  const query = (nombre || "").toLowerCase().trim();
  console.log("🔎 [buscar_tienda] Query normalizado:", query);
 
  if (!query) {
    console.warn("⚠️ [buscar_tienda] Query vacío, retornando lista vacía");
    return [];
  }
 
  console.log("🚀 [buscar_tienda] Iniciando búsqueda en Algolia...");
  let { hits } = await index.search(query, {
    filters: filters.join(" AND "),
    hitsPerPage: 10,
    typoTolerance: true,
    ignorePlurals: true,
    removeStopWords: true,
    attributesToRetrieve: ATTRS_NOMBRE,
  });
  console.log(`✅ [buscar_tienda] Algolia retornó ${hits.length} hits normales`);
 
  if (hits.length > 0) {
    hits = hits.map((h) => ({ ...h, similarity: 1, match_keyword: query }));
    console.log(
      "📦 [buscar_tienda] Hits normales mapeados:",
      hits.map((h) => ({ id: h.objectID, nombre: h.nombre })),
    );
  }
 
  if (hits.length === 0) {
    console.log("🔄 [buscar_tienda] Sin hits normales, iniciando fallback inteligente...");
 
    const { hits: hitsFallback } = await index.search("", {
      filters: filters.join(" AND "),
      hitsPerPage: 150,
      attributesToRetrieve: ATTRS_NOMBRE,
    });
    console.log(`📥 [buscar_tienda] Fallback: ${hitsFallback.length} candidatos para comparar`);
 
    // 🔧 CAMBIO 2: nuevo umbral (antes 0.35) + validación de longitud
    const UMBRAL_MINIMO = 0.55;
    const RATIO_LONGITUD_MINIMO = 0.6;
 
    hits = hitsFallback
      .map((h) => {
        let bestScore = 0;
        let bestKeyword = null;
 
        if (typeof h.nombre === "string") {
          const value = h.nombre.toLowerCase();
          // 🔧 CAMBIO 2: chequeo de longitud antes de calcular similarity
          const minLen = Math.min(query.length, value.length);
          const maxLen = Math.max(query.length, value.length);
          const lengthOk = maxLen > 0 ? minLen / maxLen >= RATIO_LONGITUD_MINIMO : false;
          if (lengthOk) {
            let score = similarity.stringSimilarity(query, value);
            if (value.includes(query)) score += 0.2;
            if (score > bestScore) {
              bestScore = score;
              bestKeyword = h.nombre;
            }
          }
        }
 
        if (Array.isArray(h.parecidas)) {
          for (const p of h.parecidas) {
            if (typeof p !== "string") continue;
            const value = p.toLowerCase();
            const minLen = Math.min(query.length, value.length);
            const maxLen = Math.max(query.length, value.length);
            const lengthOk = maxLen > 0 ? minLen / maxLen >= RATIO_LONGITUD_MINIMO : false;
            if (!lengthOk) continue;
            let score = similarity.stringSimilarity(query, value);
            if (value.includes(query)) score += 0.2;
            if (score > bestScore) {
              bestScore = score;
              bestKeyword = p;
            }
          }
        }
 
        if (Array.isArray(h.tag)) {
          for (const t of h.tag) {
            if (typeof t !== "string") continue;
            const value = t.toLowerCase();
            const minLen = Math.min(query.length, value.length);
            const maxLen = Math.max(query.length, value.length);
            const lengthOk = maxLen > 0 ? minLen / maxLen >= RATIO_LONGITUD_MINIMO : false;
            if (!lengthOk) continue;
            let score = similarity.stringSimilarity(query, value);
            if (value.includes(query)) score += 0.15;
            if (score > bestScore) {
              bestScore = score;
              bestKeyword = t;
            }
          }
        }
 
        if (typeof h.categoria === "string") {
          const value = h.categoria.toLowerCase();
          const minLen = Math.min(query.length, value.length);
          const maxLen = Math.max(query.length, value.length);
          const lengthOk = maxLen > 0 ? minLen / maxLen >= RATIO_LONGITUD_MINIMO : false;
          if (lengthOk) {
            let score = similarity.stringSimilarity(query, value);
            if (value.includes(query)) score += 0.1;
            if (score > bestScore) {
              bestScore = score;
              bestKeyword = h.categoria;
            }
          }
        }
 
        // 🔧 CAMBIO 2: 0.55 en vez de 0.35
        if (bestScore >= UMBRAL_MINIMO) {
          console.log(
            `✅ [fallback] "${h.nombre}" pasó filtro → score: ${bestScore.toFixed(2)}, keyword: ${bestKeyword}`,
          );
          return { ...h, similarity: bestScore, match_keyword: bestKeyword };
        }
 
        console.log(`❌ [fallback] "${h.nombre}" descartado → score: ${bestScore.toFixed(2)}`);
        return null;
      })
      .filter(Boolean)
      .sort((a, b) => b.similarity - a.similarity)
      .slice(0, 10);
 
    console.log(`📊 [buscar_tienda] Fallback final: ${hits.length} hits seleccionados`);
  }
 
  const ids = hits.map((h) => h.objectID);
  console.log("🆔 [buscar_tienda] IDs a consultar en Firestore:", ids);
 
  const idsConFlag = hits.filter((h) => h.plantilla === true).map((h) => h.objectID);
  const idsSinFlag = hits.filter((h) => h.plantilla !== true).map((h) => h.objectID);
  console.log("🏷️ [buscar_tienda] IDs con plantilla:", idsConFlag);
  console.log("🏷️ [buscar_tienda] IDs sin plantilla:", idsSinFlag);
 
  console.log("⚡ [buscar_tienda] Consultando extraData y créditos en paralelo...");
  const [extraData, creditosResults] = await Promise.all([
    obtenerDatosPorIds(localidad, ids),
    idsConFlag.length > 0
      ? Promise.all(
        idsConFlag.map((id) =>
          obtener_creditos_tienda_fn(id)
            .then((r) => {
              const mayor_a_100 = r?.creditos > 100;
              console.log(
                `💰 [creditos] ${id} → creditos: ${r?.creditos} | mayor_a_100: ${mayor_a_100}`,
              );
              return { id, mayor_a_100 };
            })
            .catch((e) => {
              console.error(`❌ [creditos] Error obteniendo créditos para ${id}:`, e.message);
              return { id, mayor_a_100: false };
            }),
        ),
      )
      : Promise.resolve([]),
  ]);
  console.log("✅ [buscar_tienda] extraData obtenida para IDs:", Object.keys(extraData));
 
  const creditosMap = Object.fromEntries(
    creditosResults.map(({ id, mayor_a_100 }) => [id, mayor_a_100]),
  );
  console.log("💳 [buscar_tienda] creditosMap:", creditosMap);
 
  const data = hits.map((hit) => {
    const extra = extraData[hit.objectID] || {};
    const tienePlan = hit.plantilla === true && creditosMap[hit.objectID] === true;
    const eraPlantillaSinCreditos = hit.plantilla === true && creditosMap[hit.objectID] !== true;
 
    console.log(
      `🏪 [buscar_tienda] Mapeando tienda: ${hit.nombre} | plantilla: ${hit.plantilla} | tienePlan: ${tienePlan}`,
    );
 
    const base = {
      id: hit.objectID,
      tienda: hit.nombre || "",
      open_state: verificar_apertura_tienda(extra.horario),
      match_keyword: hit.match_keyword || null,
      similarity: Number((hit.similarity || 0).toFixed(2)),
    };
 
    if (search === true) return base;
 
    return {
      ...base,
      desc: (hit.descripcion || "").substring(0, 150),
      loc: hit.lugar || "",
      cat: hit.categoria || "",
      img: hit.imagen_bot || "",
      wha: extra.whatsapp || "",
      pla: tienePlan,
      ...(eraPlantillaSinCreditos && { era_plantilla: true }),
      msje_pla_wa: hit.msje_whatsapp || "",
      alias: hit.alias || "",
      tipo: "tienda",
    };
  });
 
  console.log(`🎯 [buscar_tienda] Respuesta final: ${data.length} tiendas`);
  return data;
}

// ----------------------------------------------------------------------------
// 3.4 Búsqueda por CATEGORIA + SUBCATEGORIA (función interna, basada en
// buscar_por_categoria_subcateogira_Actualizado). La subcategoria ya viene
// resuelta desde afuera (prompt 2), aquí solo se hace la búsqueda en Algolia.
// ----------------------------------------------------------------------------
async function buscarPorCategoria({ localidad, categoria, subcategoria, excluir_id }) {
  const categoriaLimpia = (categoria || "").trim().toLowerCase();
  const momento_dia = obtenerMomentoDia();

  const ATTRS_CATEGORIA = [
    "objectID",
    "nombre",
    "descripcion",
    "lugar",
    "categoria",
    "imagen_bot",
    "plantilla",
    "msje_whatsapp",
    "alias",
  ];

  let filters = [];
  if (localidad) filters.push(`lugar:"${localidad.toLowerCase().trim()}"`);
  if (categoriaLimpia) filters.push(`categoria:"${categoriaLimpia}"`);
  if (subcategoria) filters.push(`tag:"${subcategoria.toLowerCase().trim()}"`);
  if (excluir_id) filters.push(`NOT objectID:"${excluir_id}"`);

  if (categoriaLimpia) {
    const refCat = db.collection("estadisticas").doc(categoriaLimpia);
    refCat.set({ categoria: categoriaLimpia }, { merge: true }).catch((e) => console.error("Stats init:", e));
    refCat
      .collection("busquedas_categoria")
      .add({ timestamp: FieldValue.serverTimestamp(), localidad: localidad || null })
      .catch((e) => console.error("Stats cat:", e));

    if (subcategoria) {
      refCat
        .collection("busquedas_subcategoria")
        .add({
          subcategoria,
          timestamp: FieldValue.serverTimestamp(),
          localidad: localidad || null,
        })
        .catch((e) => console.error("Stats sub:", e));
    }
  }

  const { hits } = await index.search("", {
    filters: filters.join(" AND "),
    hitsPerPage: 20,
    typoTolerance: true,
    ignorePlurals: true,
    removeStopWords: true,
    attributesToRetrieve: ATTRS_CATEGORIA,
  });

  const ids = hits.map((h) => h.objectID);
  const idsConFlag = hits.filter((h) => h.plantilla === true).map((h) => h.objectID);
  const idsSinFlag = hits.filter((h) => h.plantilla !== true).map((h) => h.objectID);

  const [extraData, creditosResults] = await Promise.all([
    obtenerDatosPorIds(localidad, ids),
    idsConFlag.length > 0
      ? Promise.all(
        idsConFlag.map((id) =>
          obtener_creditos_tienda_fn(id)
            .then((r) => ({ id, mayor_a_100: r?.creditos > 100 }))
            .catch(() => ({ id, mayor_a_100: false })),
        ),
      )
      : Promise.resolve([]),
  ]);

  const creditosMap = Object.fromEntries(
    creditosResults.map(({ id, mayor_a_100 }) => [id, mayor_a_100]),
  );

  const data = hits
    .sort(() => Math.random() - 0.5)
    .map((hit) => {
      const extra = extraData[hit.objectID] || {};
      const tienePlan = hit.plantilla === true && creditosMap[hit.objectID] === true;
      const eraPlantillaSinCreditos = hit.plantilla === true && creditosMap[hit.objectID] !== true;

      return {
        id: hit.objectID,
        name: hit.nombre || "",
        desc: (hit.descripcion || "").substring(0, 150),
        loc: hit.lugar || "",
        cat: hit.categoria || "",
        img: hit.imagen_bot || "",
        wha: extra.whatsapp || "",
        open_state: verificar_apertura_tienda(extra.horario),
        pla: tienePlan,
        ...(eraPlantillaSinCreditos && { era_plantilla: true }),
        msje_pla_wa: hit.msje_whatsapp || "",
        alias: hit.alias || "",
        tipo: "tienda",
      };
    });

  const idsConFlagSet = new Set(idsConFlag);
  const idsSinFlagSet = new Set(idsSinFlag);

  const conFlagValidos = data.filter(
    (d) => idsConFlagSet.has(d.id) && creditosMap[d.id] === true,
  );
  const sinFlag = data.filter(
    (d) => idsSinFlagSet.has(d.id) || (idsConFlagSet.has(d.id) && creditosMap[d.id] !== true),
  );

  const topFlag = conFlagValidos.slice(0, 3);
  const topNormal = sinFlag.slice(0, 2);

  const mezclados = [];
  const maxLen = Math.max(topFlag.length, topNormal.length);
  for (let i = 0; i < maxLen; i++) {
    if (i < topFlag.length) mezclados.push(topFlag[i]);
    if (i < topNormal.length) mezclados.push(topNormal[i]);
  }

  const dataFinal = mezclados.slice(0, 5);
  return { momento_dia, total: dataFinal.length, data: dataFinal };
}

// ----------------------------------------------------------------------------
// Helpers de presentación (mismos que tenías en tu code node / elegir_mejor_promo)
// ----------------------------------------------------------------------------
const CTAS = [
  "👉 Mira su perfil aqui",
  "🔥 Descúbrelo en Geinz",
  "📍 Mira todos los detalles",
  "🚀 Explóralo ahora",
  "😎 Dale un vistazo aquí",
  "✨ Mira en Geinz",
  "👀 Chequéalo aquí",
  "📲 Ábrelo en la app",
];

const STIKER_TIENDA = [
  "https://firebasestorage.googleapis.com/v0/b/geinzworkapp.appspot.com/o/STIKER%2FDise%C3%B1o%20sin%20t%C3%ADtulo%20(20)-convertido-de-png.webp?alt=media&token=8368964e-7782-4872-8b8b-4f454b09cea5",
  "https://firebasestorage.googleapis.com/v0/b/geinzworkapp.appspot.com/o/STIKER%2FDise%C3%B1o%20sin%20t%C3%ADtulo%20(22)-Photoroom-convertido-de-png.webp?alt=media&token=c3e538f5-18fc-4f3b-936f-6acad775ab15",
  "https://firebasestorage.googleapis.com/v0/b/geinzworkapp.appspot.com/o/STIKER%2FDise%C3%B1o%20sin%20t%C3%ADtulo%20(23)-Photoroom-convertido-de-png.webp?alt=media&token=a9f2cfd5-9918-43ac-a312-b7746a1afe06",
];

function pick(arr) {
  if (!Array.isArray(arr) || !arr.length) return "";
  return arr[Math.floor(Math.random() * arr.length)];
}

function generarToken() {
  return `${Date.now().toString(36)}${Math.random().toString(36).substring(2, 12)}`;
}

function parsearRespuestaIA(raw) {
  if (!raw || typeof raw !== "string") return {};
  try {
    let limpio = raw.replace(/```json|```/gi, "").replace(/\n/g, " ").trim();
    if (limpio.startsWith('"') || limpio.startsWith("'")) {
      try {
        limpio = JSON.parse(limpio);
      } catch (e) { }
    }
    limpio = limpio.replace(/([,{]\s*)([a-zA-Z_]\w*)\s*:/g, '$1"$2":');
    const match = limpio.match(/\{.*\}/s);
    if (!match) return {};
    return JSON.parse(match[0]);
  } catch (e) {
    console.error("❌ Error parseando respuesta IA (tienda):", e.message, "| RAW:", raw.slice(0, 200));
    return {};
  }
}

// ----------------------------------------------------------------------------
// 3.5 🚀 ENDPOINT COMPLETO: clasifica -> (subcategoria si aplica) -> busca -> Gemini elige y redacta -> arma salida EXACTA
// ----------------------------------------------------------------------------
exports.geinz_buscar_unificado = onRequest(async (req, res) => {
  const tiempoInicioTotal = Date.now();

  let tokensOpenAI = { prompt_tokens: 0, completion_tokens: 0, total_tokens: 0 };
  let tokensGemini = { promptTokenCount: 0, candidatesTokenCount: 0, totalTokenCount: 0 };

  // 🪵 Acumulador de trazas/debug para la respuesta final
  const trace = {
    tipo_busqueda: null, // "nombre" | "categoria" | "sin_resultado"
    clasificacion_raw: null,
    prompt_clasificacion: null,
    subcategoria_usada: null,
    prompt_subcategoria: null,
    reenrutado_a_nombre: false, // true si prompt2 detectó NEGOCIO cuando prompt1 dijo categoria
    heredo_contexto: false,
    prompt_respuesta_gemini: null,
    respuesta_gemini_raw: null,
  };

  try {
    const { mensaje, contexto_previo, localidad, excluir_id, nombre_usuario } = req.body;

    if (!mensaje || typeof mensaje !== "string" || !mensaje.trim()) {
      return res.status(400).json({ ok: false, error: "El campo 'mensaje' es requerido" });
    }

    const momento_dia = obtenerMomentoDia();

    // ==========================================================================
    // 1a) PROMPT 1 — CLASIFICADOR
    // ==========================================================================
    const { lista, mapaSub } = await obtenerCategoriasConSub();
    const promptClasificacion = construirPromptClasificacion(mensaje, contexto_previo, lista);
    trace.prompt_clasificacion = promptClasificacion; // 🪵 guardamos el prompt exacto usado

    const completionClasificacion = await openai.chat.completions.create({
      model: "gpt-5.4-nano",
      messages: [{ role: "user", content: promptClasificacion }],
      response_format: { type: "json_object" },
      reasoning_effort: "none",
      max_completion_tokens: 300,
    });

    if (completionClasificacion?.usage) {
      tokensOpenAI = {
        prompt_tokens: completionClasificacion.usage.prompt_tokens || 0,
        completion_tokens: completionClasificacion.usage.completion_tokens || 0,
        total_tokens: completionClasificacion.usage.total_tokens || 0,
      };
    }

    const clasificacion = parsearClasificacionIA(completionClasificacion.choices[0]?.message?.content);

    trace.clasificacion_raw = clasificacion; // 🪵 guardamos el JSON crudo devuelto por el modelo

    let { nombre, categoria, search } = clasificacion;
    const { heredar_contexto } = clasificacion;
    trace.heredo_contexto = !!heredar_contexto;

    // ==========================================================================
    // 1b) PROMPT 2 — SELECTOR DE SUBCATEGORIA
    // ==========================================================================
    let subcategoria = null;

    if (nombre) {
      subcategoria = null;
    } else if (heredar_contexto) {
      const ctxUsuario = contexto_previo?.contexto_usuario ?? contexto_previo ?? {};
      subcategoria = ctxUsuario?.subcategoria || null;
    } else if (categoria) {
      const subsDisponibles = mapaSub[categoria.toLowerCase()] || [];

      if (subsDisponibles.length > 0) {
        const promptSubcategoria = construirPromptSubcategoria(mensaje, contexto_previo, subsDisponibles);
        trace.prompt_subcategoria = promptSubcategoria; // 🪵 guardamos el prompt exacto usado

        const completionSubcategoria = await openai.chat.completions.create({
          model: "gpt-5.4-nano",
          messages: [{ role: "user", content: promptSubcategoria }],
          reasoning_effort: "none",
          max_completion_tokens: 60,
        });

        if (completionSubcategoria?.usage) {
          tokensOpenAI.prompt_tokens += completionSubcategoria.usage.prompt_tokens || 0;
          tokensOpenAI.completion_tokens += completionSubcategoria.usage.completion_tokens || 0;
          tokensOpenAI.total_tokens += completionSubcategoria.usage.total_tokens || 0;
        }

        const rawSub = (completionSubcategoria.choices[0]?.message?.content || "").trim();

        const matchNegocio = rawSub.match(/^NEGOCIO\s*:\s*(.+)$/i);
        if (matchNegocio) {
          nombre = matchNegocio[1].trim();
          categoria = null;
          subcategoria = null;
          trace.reenrutado_a_nombre = true; // 🪵 el prompt 2 detectó que era un negocio, no una categoría
        } else {
          subcategoria = rawSub || null;
        }
      }
    }
    trace.subcategoria_usada = subcategoria; // 🪵 subcategoría final (heredada, elegida por IA, o null)

    // ==========================================================================
    // 2) BUSCAR — por nombre O por categoria+subcategoria, nunca ambos
    // ==========================================================================
    let resultadosBusqueda = [];

    if (nombre) {
      trace.tipo_busqueda = "nombre"; // 🪵
      resultadosBusqueda = await buscarPorNombreTienda({ localidad, nombre, search });
    } else if (categoria) {
      trace.tipo_busqueda = "categoria"; // 🪵
      const resultado = await buscarPorCategoria({
        localidad,
        categoria,
        subcategoria,
        excluir_id: excluir_id || clasificacion.excluir_id,
      });
      resultadosBusqueda = resultado.data;
    } else {
      trace.tipo_busqueda = "sin_criterio"; // 🪵 ni nombre ni categoria (caso raro, ej geinz/emergencia)
    }

    // 3) 2da IA (GEMINI) -> elige 1 negocio y redacta el mensaje
    let response;

    if (!resultadosBusqueda.length) {
      trace.tipo_busqueda = trace.tipo_busqueda === "sin_criterio" ? "sin_criterio" : "sin_resultado"; // 🪵
      response = {
        id: "sin_id",
        mensaje: "No encontré nada por ahora, cuéntame qué buscas o prueba con otro nombre",
        intencion: "SIN_DATOS",
      };
    } else {
      const contextoStr = JSON.stringify(contexto_previo?.contexto_usuario ?? contexto_previo ?? {});

      const datosParaPrompt = resultadosBusqueda.map((d) => {
        const { open_state, ...resto } = d;
        return { ...resto, open_closed: open_state === true ? "abierto" : "cerrado" };
      });

      const promptRespuesta = `Responde en JSON válido.
DATOS:
${JSON.stringify(datosParaPrompt)}
CONTEXTO PREVIO (negocio ya consultado antes):
${contextoStr}
REGLAS:
- Si el usuario pregunta algo sobre el negocio del CONTEXTO PREVIO → responde sobre ese negocio usando los DATOS disponibles
- Nunca SALUDES, habla como si fuera conversación continua, como si ya se conocieran, LENGUAJE LOCAL SIEMPRE MUY AMIGABLE nada robótico ni corporativo, habla como un pata de Barranca peruano, canchero, ALGO INFORMATIVO SIN VENDER TANTO
- Elegir SOLO 1 negocio según lo que pide el usuario
- mensaje: 1 línea, máximo 2 frases
- incluir SOLO si existen: estado (🟢 Abierto / 🔴 Cerrado DE open_closed DE DATOS), descripción siempre informativa, métodos de pago (máx 2), comodidades (máx 2) solo si existen, si no existen → NO mencionarlos
- El usuario se llama: ${nombre_usuario || ""} úsalo siempre
- si falta info → mencionar app Geinz
- sin comillas dentro del mensaje
- USA EL MOMENTO DEL DIA SIEMPRE QUE ES: ${momento_dia}
- Priorizar negocios con open_closed: "abierto" al elegir
- Si el usuario menciona un nombre exacto de negocio → elegir ese sin importar si está abierto o cerrado
FORMATO OBLIGATORIO:
{"id":"{id}","mensaje":"...","intencion":"NEGOCIO"}`;

      trace.prompt_respuesta_gemini = promptRespuesta; // 🪵 guardamos el prompt exacto enviado a Gemini

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

      const geminiRes = await fetch(`${GEMINI_URL}?key=${GEMINIKEY}`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(bodyGemini),
      });

      if (!geminiRes.ok) {
        const errText = await geminiRes.text();
        console.error("❌ Error Gemini API (tienda):", geminiRes.status, errText);
        response = {
          id: "sin_id",
          mensaje: "Tuve un problema consultando la info, intenta de nuevo en un momento",
          intencion: "ERROR_GEMINI",
        };
      } else {
        const geminiData = await geminiRes.json();

        if (geminiData?.usageMetadata) {
          tokensGemini = {
            promptTokenCount: geminiData.usageMetadata.promptTokenCount || 0,
            candidatesTokenCount: geminiData.usageMetadata.candidatesTokenCount || 0,
            totalTokenCount: geminiData.usageMetadata.totalTokenCount || 0,
          };
        }

        const rawText = geminiData?.candidates?.[0]?.content?.parts?.[0]?.text || "{}";
        trace.respuesta_gemini_raw = rawText; // 🪵 guardamos la respuesta cruda de Gemini
        response = parsearRespuestaIA(rawText);
        if (!response || !Object.keys(response).length) {
          response = { id: "sin_id", mensaje: rawText || "Sin respuesta", intencion: "ERROR_FORMATO_IA" };
        }
      }
    }

    // 4) ARMAR SALIDA — mismos nombres, mismo shape que tu code node original
    const apiData = resultadosBusqueda;

    const idFinal = response?.id || "sin_id";
    const mensajeFinal = String(response?.mensaje || "").trim();

    const match = apiData.find(
      (d) => String(d?.id || "").trim().replace(/"/g, "") === String(idFinal).trim().replace(/"/g, ""),
    );

    const imagenFinal = match?.img || "";
    const keywords = response?.keywords || response?.palabras_clave || [];
    const kwArr = Array.isArray(keywords) ? keywords : [];
    const subcategoria_tienda = subcategoria || "";

    const nombre_negocio =
      match?.nombre || match?.name || match?.titulo || match?.negocio || match?.tienda || "";
    const categoria_para_data = match?.cat || match?.category || "";
    const sub = subcategoria_tienda ? `${categoria_para_data} sub:${subcategoria_tienda}` : categoria_para_data;
    const extra = kwArr.length ? kwArr.join(",") : "null";

    const data = ["TIENDA", nombre_negocio, sub, extra, idFinal].join("|");

    let categoria_match = match?.cat || match?.category || "general";
    const whatsappFinal = match?.wha || "";
    const msje_pla_wa = match?.msje_pla_wa || "";
    const usa_plantilla = match?.pla || false;
    const era_plantilla_pero_misio = match?.era_plantilla || false;
    const alias_tienda = match?.alias || "";

    const categoriaFinal = encodeURIComponent(categoria_match).replace(/%20/g, "+");

    const link_construido = alias_tienda ? `https://geinzworkapp.web.app/perfil/${alias_tienda}` : "";

    const mensaje_safe = usa_plantilla
      ? mensajeFinal
      : mensajeFinal
        ? link_construido
          ? `${mensajeFinal}  ${pick(CTAS)}: ${link_construido}`
          : mensajeFinal
        : link_construido
          ? `${pick(CTAS)}: ${link_construido}`
          : "";

    const imagen_stiker = pick(STIKER_TIENDA);
    const esTiendaSinPlantilla = !usa_plantilla;
    const token_wsap = usa_plantilla ? generarToken() : "";

    const tiempoTotalMs = Date.now() - tiempoInicioTotal;

    const tokens_usados = {
      openai: {
        modelo: "gpt-5.4-nano",
        nota: "suma de 1 o 2 llamadas: clasificador (solo categorias) + selector de subcategoria (si aplico)",
        prompt_tokens: tokensOpenAI.prompt_tokens,
        completion_tokens: tokensOpenAI.completion_tokens,
        total_tokens: tokensOpenAI.total_tokens,
      },
      gemini: {
        prompt_tokens: tokensGemini.promptTokenCount,
        completion_tokens: tokensGemini.candidatesTokenCount,
        total_tokens: tokensGemini.totalTokenCount,
      },
      total_tokens_combinado: tokensOpenAI.total_tokens + tokensGemini.totalTokenCount,
    };

    // 🪵 Log en consola (para Cloud Logging) de la traza completa del flujo
    console.log("🧭 [geinz_tienda_completo] TRACE:", JSON.stringify({
      mensaje,
      tipo_busqueda: trace.tipo_busqueda,
      heredo_contexto: trace.heredo_contexto,
      reenrutado_a_nombre: trace.reenrutado_a_nombre,
      clasificacion_raw: trace.clasificacion_raw,
      subcategoria_usada: trace.subcategoria_usada,
      total_resultados: resultadosBusqueda.length,
      id_elegido: idFinal,
      tokens_usados,
      tiempo_total_ms: tiempoTotalMs,
    }));

    return res.status(200).json({
      ...response,
      id: idFinal,
      imagen: esTiendaSinPlantilla ? "" : imagenFinal,
      mensaje_safe,
      data,
      siker: imagen_stiker,
      msje_pla_wa,
      plantilla: usa_plantilla,
      wha: usa_plantilla ? whatsappFinal : "",
      cat_detectada: categoriaFinal,
      era_plantilla_pero_misio,
      nombre_negocio,
      token_wsap,
      alias_tienda,
      tokens_usados,
      tiempo_total_ms: tiempoTotalMs,
      tiempo_total_seg: Number((tiempoTotalMs / 1000).toFixed(2)),
      // 👇 NUEVO: traza completa del flujo (qué camino tomó, qué prompts usó)
      debug_trace: {
        tipo_busqueda: trace.tipo_busqueda,
        heredo_contexto: trace.heredo_contexto,
        reenrutado_a_nombre: trace.reenrutado_a_nombre,
        clasificacion_raw: trace.clasificacion_raw,
        subcategoria_usada: trace.subcategoria_usada,
        total_resultados: resultadosBusqueda.length,
        prompts_usados: {
          clasificacion: trace.prompt_clasificacion,
          subcategoria: trace.prompt_subcategoria,
          respuesta_gemini: trace.prompt_respuesta_gemini,
        },
        respuesta_gemini_raw: trace.respuesta_gemini_raw,
      },
    });
  } catch (error) {
    console.error("❌ [geinz_tienda_completo] Error:", error.message);
    console.error("❌ [geinz_tienda_completo] Stack:", error.stack);
    console.error("❌ [geinz_tienda_completo] Trace hasta el fallo:", JSON.stringify(trace));

    const tiempoTotalMs = Date.now() - tiempoInicioTotal;

    return res.status(500).json({
      ok: false,
      error: error.message,
      debug_trace: trace, // 🪵 aunque falle, mandamos lo que se alcanzó a registrar
      tokens_usados: {
        openai: tokensOpenAI,
        gemini: tokensGemini,
        total_tokens_combinado: tokensOpenAI.total_tokens + tokensGemini.totalTokenCount,
      },
      tiempo_total_ms: tiempoTotalMs,
      tiempo_total_seg: Number((tiempoTotalMs / 1000).toFixed(2)),
    });
  }
});
// ============================================================================
// ============================================================================
//   4. MÓDULO TURISMO
// ============================================================================
// ============================================================================

let subcategoriasTurismoCache = null;
let subcategoriasTurismoCacheTimestamp = 0;

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

exports.clasificador_geinz_turismo = onRequest(async (req, res) => {
  try {
    const { mensaje, contexto_previo } = req.body;

    if (!mensaje || typeof mensaje !== "string" || !mensaje.trim()) {
      return res.status(400).json({ ok: false, error: "El campo 'mensaje' es requerido" });
    }

    const categorias = await obtenerSubcategoriasTurismo();
    const prompt = construirPromptTurismo(mensaje, contexto_previo, categorias);

    const completion = await openai.chat.completions.create({
      model: "gpt-5.4-nano",
      messages: [{ role: "user", content: prompt }],
      response_format: { type: "json_object" },
    });

    const raw = completion.choices[0]?.message?.content || "{}";

    let resultado;
    try {
      resultado = JSON.parse(raw);
    } catch (e) {
      console.error("❌ Error parseando respuesta de OpenAI (turismo):", e.message, "| RAW:", raw);
      resultado = { tipo: "turismo", nombre: null, categoria: null, excluir_id: null };
    }

    return res.status(200).json({ ok: true, data: resultado });
  } catch (error) {
    console.error("❌ Error clasificador_geinz_turismo:", error.message);
    return res.status(500).json({ ok: false, error: error.message });
  }
});

// ============================================================================
// ============================================================================
//   5. MÓDULO EMERGENCIAS (SALUD / SEGURIDAD)
// ============================================================================
// ============================================================================

function construirPromptEmergencia(mensaje) {
  return `Clasifica el mensaje en una sola palabra: SALUD o SEGURIDAD.
SIEMPRE TIENES QUE VER CUAL ES MEJOR PARA LA SITUACION SIN COMETER ERRORES.

Responde ÚNICAMENTE con esa palabra, sin explicaciones, sin puntos, sin comillas, sin markdown.

MENSAJE: "${mensaje}"`;
}

exports.obtener_lugares_emergencia_Actualizado = onRequest(async (req, res) => {
  try {
    const { localidad, mensaje } = req.body;

    if (!mensaje || typeof mensaje !== "string" || !mensaje.trim()) {
      return res.status(400).json({ ok: false, error: "El campo 'mensaje' es requerido" });
    }

    const promptEmergencia = construirPromptEmergencia(mensaje);

    const completion = await openai.chat.completions.create({
      model: "gpt-5.4-nano",
      messages: [{ role: "user", content: promptEmergencia }],
    });

    const clasificacionRaw = (completion.choices[0]?.message?.content || "")
      .toUpperCase()
      .normalize("NFD")
      .replace(/[\u0300-\u036f]/g, "");

    console.log("🧠 RAW IA:", completion.choices[0]?.message?.content);

    const categoria = clasificacionRaw.includes("SALUD")
      ? "salud"
      : clasificacionRaw.includes("SEGURIDAD")
        ? "seguridad"
        : "general";

    console.log("🚨 CLASIFICACION EMERGENCIA:", categoria);

    let filtersArray = [];
    if (localidad) filtersArray.push(`lugar:"${localidad}"`);
    if (categoria && categoria !== "general") filtersArray.push(`categoria:"${categoria}"`);

    const filters = filtersArray.length > 0 ? filtersArray.join(" AND ") : undefined;

    const result = await index.search("", { filters, hitsPerPage: 20 });

    const data = result.hits.map((d) => {
      let ubicacion = null;
      if (d.ubicacion && d.ubicacion.latitud != null && d.ubicacion.longitud != null) {
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

    res.set("Cache-Control", "public, max-age=300");

    return res.status(200).json({ ok: true, categoria, total: data.length, data });
  } catch (error) {
    console.error("ERROR obtener_lugares_emergencia:", error);
    return res.status(500).json({ ok: false, mensaje: "Error interno al buscar lugares" });
  }
});

// ============================================================================
// ============================================================================
//   6. MÓDULO PROMOCIONES
// ============================================================================
// ============================================================================

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
        .map((it) => `  ${it.id}~${it.sc}~${it.desc}~${it.precio}~${it.como}~${it.p_ok}~${it.pr_ok}`)
        .join("\n");
      return `${header}\n${items}`;
    })
    .join("\n\n");
}

function construirPromptPromo(momento, nombreUsuario, resultados, alt, mensajeUsuario) {
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

REGLAS: usa solo estos datos, no inventes. Misma tienda=compara y elige mejor calce. p_ok=false avisa y ofrece alt con pago real. Todo vacío=recomienda directo. Prioriza mayor sc. Tono peruano natural sin saludar. No menciones "score". Usa el momento del día natural. MENSAJE MÁX 200 CARACTERES, sin listar sabores/variantes si son 2+ promos, solo tienda+frase gancho corta c/u. Máx 2 emojis. Sin promos ni alt: dilo directo. Nunca saludes (hola/qué tal/qué buena).

DECISIÓN: 2+ relevantes→varios=true+ids | 1→varios=false+id | 0→varios=false,id="none"`;
}

exports.elegir_mejor_promo = onRequest(async (req, res) => {
  const inicioTiempo = Date.now();

  try {
    const { momento, resultados, alt, nombre_usuario, mensaje_usuario } = req.body;

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
      return res.status(502).json({ ok: false, error: "Error al consultar Gemini" });
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
      console.warn("⚠️ Respuesta posiblemente truncada por maxOutputTokens, revisar prompt o subir el tope");
    }

    const rawText = geminiData?.candidates?.[0]?.content?.parts?.[0]?.text || "{}";

    let resultado;
    try {
      resultado = JSON.parse(rawText);
    } catch (e) {
      console.error("❌ Error parseando respuesta de Gemini:", e.message, "| RAW:", rawText);
      resultado = {
        tipo: "bot",
        varios: false,
        id: "none",
        mensaje: "No pude procesar las promociones en este momento.",
      };
    }

    if (typeof resultado.mensaje === "string" && resultado.mensaje.length > MAX_MENSAJE_CHARS) {
      console.warn(`✂️ Mensaje recortado: ${resultado.mensaje.length} chars → ${MAX_MENSAJE_CHARS}`);
      resultado.mensaje = resultado.mensaje.slice(0, MAX_MENSAJE_CHARS).trim() + "...";
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


// ============================================================================
// 📌 DEPENDENCIAS QUE YA TIENES DEFINIDAS EN TU ARCHIVO (no las repito aquí):
// db, index (Algolia), onRequest, fetch, GEMINI_URL, GEMINIKEY,
// obtenerDatosPorIds, verificar_apertura_tienda, obtener_creditos_tienda_fn,
// obtenerMomentoDia, pick, CTAS, STIKER_TIENDA, parsearRespuestaIA
// ============================================================================

// ----------------------------------------------------------------------------
// 🔎 Trae UN negocio puntual: por ID directo (index.getObject) o, si no hay id,
// por NOMBRE (búsqueda simple en Algolia, se queda con el mejor hit).
// ----------------------------------------------------------------------------
// ----------------------------------------------------------------------------
// 🔎 Trae UN negocio puntual: por ID directo (index.getObject) o, si no hay id,
// por NOMBRE (búsqueda simple en Algolia, se queda con el mejor hit).
// ----------------------------------------------------------------------------
async function obtenerNegocioPorIdONombre({ id, nombre, localidad }) {
  const ATTRS = [
    "objectID",
    "nombre",
    "descripcion",
    "lugar",
    "categoria",
    "imagen_bot",
    "alias",
  ];

  let hit = null;

  if (id) {
    try {
      hit = await index.getObject(id, { attributesToRetrieve: ATTRS });
    } catch (e) {
      console.error("❌ [info_negocio] No se encontró objeto por id:", id, e.message);
      hit = null;
    }
  }

  if (!hit && nombre) {
    const query = nombre.toLowerCase().trim();
    const filters = [];
    if (localidad) filters.push(`lugar:"${localidad}"`);
    filters.push(`NOT categoria:"turismo"`);
    filters.push(`NOT categoria:"salud"`);

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

// ----------------------------------------------------------------------------
// 🚀 ENDPOINT: info directa de un negocio (por id o nombre) -> Gemini SOLO
// redacta el mensaje. El link con alias lo arma el código, nunca Gemini.
// ----------------------------------------------------------------------------
exports.geinz_info_negocio = onRequest(async (req, res) => {
  const tiempoInicioTotal = Date.now();
  let tokensGemini = { promptTokenCount: 0, candidatesTokenCount: 0, totalTokenCount: 0 };

  try {
    const { id, nombre, mensaje, localidad, nombre_usuario } = req.body;

    if (!id && (!nombre || !nombre.trim())) {
      return res.status(400).json({ ok: false, error: "Debes enviar 'id' o 'nombre' del negocio" });
    }

    const momento_dia = obtenerMomentoDia();

    // 1) Traer el negocio puntual
    const hit = await obtenerNegocioPorIdONombre({ id, nombre, localidad });

    if (!hit) {
      return res.status(200).json({
        id: "sin_id",
        mensaje: "No encontré ese negocio, prueba con otro nombre",
        mensaje_safe: "No encontré ese negocio, prueba con otro nombre",
        intencion: "SIN_DATOS",
        tokens_usados: { gemini: tokensGemini },
        tiempo_total_ms: Date.now() - tiempoInicioTotal,
      });
    }

    // 2) Extra data (solo horario, para saber si está abierto/cerrado)
    const extraData = await obtenerDatosPorIds(localidad, [hit.objectID]);
    const extra = extraData[hit.objectID] || {};
    const open_state = verificar_apertura_tienda(extra.horario);

    // 3) Data que se le pasa a Gemini — sin plantilla, sin créditos, sin
    //    whatsapp. Solo lo necesario para que redacte el mensaje.
    const datoParaPrompt = {
      id: hit.objectID,
      tienda: hit.nombre || "",
      desc: (hit.descripcion || "").substring(0, 150),
      loc: hit.lugar || "",
      cat: hit.categoria || "",
      tipo: "tienda",
      open_closed: open_state === true ? "abierto" : "cerrado",
    };

    // 4) PROMPT GEMINI — SOLO redacta el mensaje. El link/alias lo arma el
    //    código después, Gemini no debe mencionarlo ni construirlo.
    const promptRespuesta = `Responde en JSON válido.
DATOS DEL NEGOCIO:
${JSON.stringify(datoParaPrompt)}
El usuario se llama: ${nombre_usuario || ""} úsalo siempre
MENSAJE/PREGUNTA DEL USUARIO: "${mensaje || ""}"
REGLAS:
- Responde AL GRANO exactamente lo que el usuario pregunta sobre ESTE negocio (id:${hit.objectID}, nombre:${hit.nombre}), basándote SOLO en los DATOS
- Nunca SALUDES, habla como conversación continua, como si ya se conocieran, LENGUAJE LOCAL SIEMPRE MUY AMIGABLE nada robótico ni corporativo, habla como un pata de Barranca peruano, canchero, ALGO INFORMATIVO SIN VENDER TANTO
- mensaje: máximo 2 líneas (máximo 2 frases)
- incluir SOLO si existen: estado (🟢 Abierto / 🔴 Cerrado según open_closed), descripción siempre informativa
- sin comillas dentro del mensaje
- USA EL MOMENTO DEL DIA SIEMPRE QUE ES: ${momento_dia}
- NO menciones links, perfiles, ni la app Geinz, eso lo agrega el sistema aparte
FORMATO OBLIGATORIO:
{"id":"${hit.objectID}","mensaje":"...","intencion":"NEGOCIO"}`;

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
      console.error("❌ [info_negocio] Error Gemini API:", geminiRes.status, errText);
      response = {
        id: hit.objectID,
        mensaje: "Tuve un problema consultando la info, intenta de nuevo en un momento",
        intencion: "ERROR_GEMINI",
      };
    } else {
      const geminiData = await geminiRes.json();

      if (geminiData?.usageMetadata) {
        tokensGemini = {
          promptTokenCount: geminiData.usageMetadata.promptTokenCount || 0,
          candidatesTokenCount: geminiData.usageMetadata.candidatesTokenCount || 0,
          totalTokenCount: geminiData.usageMetadata.totalTokenCount || 0,
        };
      }

      const rawText = geminiData?.candidates?.[0]?.content?.parts?.[0]?.text || "{}";
      response = parsearRespuestaIA(rawText);
      if (!response || !Object.keys(response).length) {
        response = { id: hit.objectID, mensaje: rawText || "Sin respuesta", intencion: "ERROR_FORMATO_IA" };
      }
    }

    // 5) ARMAR SALIDA — sin plantilla, sin créditos. El link con alias lo
    //    arma el CÓDIGO directamente (nunca Gemini/el prompt).
    const idFinal = response?.id || hit.objectID;
    const mensajeFinal = String(response?.mensaje || "").trim();
    const imagenFinal = hit.imagen_bot || "";
    const nombre_negocio = hit.nombre || "";
    const categoria_match = hit.categoria || "general";
    const categoriaFinal = encodeURIComponent(categoria_match).replace(/%20/g, "+");
    const alias_tienda = hit.alias || "";

    // 🔧 Link + mensaje predeterminado armados por CÓDIGO, no por Gemini.
    // Siempre que exista alias, se agrega al final del mensaje.
    const link_construido = alias_tienda ? `https://geinzworkapp.web.app/perfil/${alias_tienda}` : "";

    const mensaje_safe = link_construido
      ? `${mensajeFinal} 📲 Si quieres más info, chécalo en Geinz: ${link_construido}`
      : mensajeFinal;

    const imagen_stiker = pick(STIKER_TIENDA);

    const data = ["TIENDA", nombre_negocio, categoria_match, "null", idFinal].join("|");

    const tiempoTotalMs = Date.now() - tiempoInicioTotal;

    const tokens_usados = {
      gemini: {
        prompt_tokens: tokensGemini.promptTokenCount,
        completion_tokens: tokensGemini.candidatesTokenCount,
        total_tokens: tokensGemini.totalTokenCount,
      },
    };

    // 🪵 Log de la traza (para Cloud Logging)
    console.log("🧭 [geinz_info_negocio] TRACE:", JSON.stringify({
      id_solicitado: id || null,
      nombre_solicitado: nombre || null,
      id_encontrado: hit.objectID,
      nombre_negocio,
      tokens_usados,
      tiempo_total_ms: tiempoTotalMs,
    }));

    return res.status(200).json({
      ...response,
      id: idFinal,
      imagen: imagenFinal,
      mensaje_safe,
      data,
      siker: imagen_stiker,
      cat_detectada: categoriaFinal,
      nombre_negocio,
      alias_tienda,
      tokens_usados,
      tiempo_total_ms: tiempoTotalMs,
      tiempo_total_seg: Number((tiempoTotalMs / 1000).toFixed(2)),
    });
  } catch (error) {
    console.error("❌ [geinz_info_negocio] Error:", error.message);
    console.error("❌ [geinz_info_negocio] Stack:", error.stack);

    const tiempoTotalMs = Date.now() - tiempoInicioTotal;

    return res.status(500).json({
      ok: false,
      error: error.message,
      tokens_usados: { gemini: tokensGemini },
      tiempo_total_ms: tiempoTotalMs,
      tiempo_total_seg: Number((tiempoTotalMs / 1000).toFixed(2)),
    });
  }
});