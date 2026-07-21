const { onRequest } = require("firebase-functions/v2/https");
const admin = require("firebase-admin");
const OpenAI = require("openai");
const algoliasearch = require("algoliasearch");
const { FieldValue } = require("firebase-admin/firestore");

const { obtener_creditos_tienda_fn } = require("./test_db2");

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
const { guardarMensajeHistorial } = require("./historial_whatsapp.js");
const WHATSAPP_TOKEN = process.env.ID_API_WHATSAPP;
const WHATSAPP_PHONE_NUMBER_ID = process.env.ID_NUMBER_WHATSAPP;
const WHATSAPP_API_VERSION = "v20.0";
let categoriasCache = null;
let categoriasCacheTimestamp = 0;
const REGLA_NO_SALUDAR = `- NUNCA saludes de ninguna forma (prohibido: "hola", "buenas", "buenos días", "buenas tardes", "buenas noches", "qué tal", "qué más"), habla como si la conversación ya estuviera en curso, directo al grano`;
function obtenerHoraPeru() {
  const horaStr = new Date().toLocaleString("en-US", {
    timeZone: "America/Lima",
    hour: "2-digit",
    hour12: false,
  });
  return parseInt(horaStr, 10);
}
function obtenerFechaHoraPeru() {
  const formatter = new Intl.DateTimeFormat("es-PE", {
    timeZone: "America/Lima",
    weekday: "long",
    hour: "2-digit",
    minute: "2-digit",
    hour12: false,
  });

  const partes = formatter.formatToParts(new Date());
  const obj = {};
  for (const p of partes) obj[p.type] = p.value;

  let hora = Number(obj.hour);
  if (hora === 24) hora = 0; // por si formatea medianoche como "24"

  return {
    diaActual: obj.weekday.toLowerCase(), // "domingo","lunes",...,"miércoles","sábado"
    minutosActual: hora * 60 + Number(obj.minute),
  };
}

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

const MENSAJES_PEDIR_ACLARACION_GEINZ = [
  (n) =>
    `Claro ${n} 😊 cuéntame un poco más, ¿buscas algún negocio, un lugar turístico o promociones?`,
  (n) =>
    `${n}, para ayudarte mejor dime qué necesitas: ¿negocio, turismo o promociones? 🙌`,
  (n) =>
    `A ver ${n} 🤔 ¿qué es lo que buscas? ¿algún negocio, lugar turístico o alguna promoción?`,
  (n) =>
    `Dime ${n} 👀 ¿te interesa algún negocio, un sitio turístico o promos?`,
];
const MENSAJES_INVITACION_TIENDA = [
  "Oye, si tienes tu propio espacio por ahí o conoces uno que debería estar en Geinz, escríbenos al 958 120 920 y lo sumamos 🚀",
  "Aprovecha: si manejas algo piola por acá o sabes de un sitio que debería aparecer aquí, mándanos un mensajito al 958 120 920 😎",
  "Psst, si tienes un espacio tuyo o conoces uno bacán, escribe al 958 120 920 y lo metemos a la app 📲",
  "Si conoces un lugar que la gente debería encontrar por aquí, cuéntanos al 958 120 920 y lo agregamos ✨",
  "¿Tienes o conoces un sitio que merece estar en Geinz? Escríbenos al 958 120 920 y lo sumamos al toque 🔥",
];
const HITS_PER_PAGE_CATEGORIA_AMPLIA = 60;

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

  const { diaActual, minutosActual } = obtenerFechaHoraPeru();
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
    console.error(
      "❌ Error parseando clasificación:",
      e.message,
      "| RAW:",
      rawContent.slice(0, 200),
    );
    return defaults;
  }

  const limpiarNull = (v) => {
    if (typeof v === "string" && v.trim().toLowerCase() === "null") return null;
    return v;
  };

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

  console.log(
    "🔄 Refrescando categorías+subcategorias (negocios) desde Firestore",
  );
  const snapshot = await db
    .collection("Tiendas")
    .doc("categorias")
    .collection("categorias")
    .get();

  const lista = [];
  const mapaSub = {};

  snapshot.forEach((doc) => {
    const id = doc.id.toLowerCase();
    if (id === "turismo") return;
    lista.push(doc.id);
    mapaSub[id] = doc.get("subcategorias") || [];
  });

  categoriasCache = { lista, mapaSub };
  categoriasCacheTimestamp = ahora;
  return categoriasCache;
}

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
- Responder ÚNICAMENTE 1 solo valor de la LISTA, o la palabra NINGUNA
- PROHIBIDO responder con comas, listas, ni múltiples valores
- Texto EXACTO como aparece en la LISTA (sin cambios, sin mayúsculas extra)
- No inventar valores fuera de la LISTA
- IMPORTANTE: solo elige "la más cercana semánticamente" cuando sea el MISMO tipo de negocio/servicio dicho con otras palabras o jerga (ej: "gym" = "gimnasio"). Si el usuario pide un tipo de negocio/servicio DISTINTO al de todas las opciones de la LISTA (ej: pide "cancha de futbol" y la LISTA solo tiene "gimnasio", "yoga", "crossfit") → responde EXACTAMENTE: NINGUNA
- Ignorar errores ortográficos e interpretar semánticamente, pero NUNCA fuerces un match falso solo para no responder NINGUNA
- Si el mensaje contiene nombre de negocio o marca → responde EXACTAMENTE: NEGOCIO: [nombre normalizado]
- Tu respuesta debe ser una sola línea, sin comas, sin explicaciones

MENSAJE DEL USUARIO: "${mensaje}"`;
}

async function buscarPorNombreTienda({ localidad, nombre, search }) {
  console.log("🔍 [buscar_tienda] Parámetros recibidos:", {
    localidad,
    nombre,
    search,
  });

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
  console.log(
    `✅ [buscar_tienda] Algolia retornó ${hits.length} hits normales`,
  );

  if (hits.length > 0) {
    hits = hits.map((h) => ({ ...h, similarity: 1, match_keyword: query }));
    console.log(
      "📦 [buscar_tienda] Hits normales mapeados:",
      hits.map((h) => ({ id: h.objectID, nombre: h.nombre })),
    );
  }

  if (hits.length === 0) {
    console.log(
      "🔄 [buscar_tienda] Sin hits normales, iniciando fallback inteligente...",
    );

    const { hits: hitsFallback } = await index.search("", {
      filters: filters.join(" AND "),
      hitsPerPage: 150,
      attributesToRetrieve: ATTRS_NOMBRE,
    });
    console.log(
      `📥 [buscar_tienda] Fallback: ${hitsFallback.length} candidatos para comparar`,
    );

    const UMBRAL_MINIMO = 0.55;
    const RATIO_LONGITUD_MINIMO = 0.6;

    hits = hitsFallback
      .map((h) => {
        let bestScore = 0;
        let bestKeyword = null;

        if (typeof h.nombre === "string") {
          const value = h.nombre.toLowerCase();
          const minLen = Math.min(query.length, value.length);
          const maxLen = Math.max(query.length, value.length);
          const lengthOk =
            maxLen > 0 ? minLen / maxLen >= RATIO_LONGITUD_MINIMO : false;
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
            const lengthOk =
              maxLen > 0 ? minLen / maxLen >= RATIO_LONGITUD_MINIMO : false;
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
            const lengthOk =
              maxLen > 0 ? minLen / maxLen >= RATIO_LONGITUD_MINIMO : false;
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
          const lengthOk =
            maxLen > 0 ? minLen / maxLen >= RATIO_LONGITUD_MINIMO : false;
          if (lengthOk) {
            let score = similarity.stringSimilarity(query, value);
            if (value.includes(query)) score += 0.1;
            if (score > bestScore) {
              bestScore = score;
              bestKeyword = h.categoria;
            }
          }
        }

        if (bestScore >= UMBRAL_MINIMO) {
          console.log(
            `✅ [fallback] "${h.nombre}" pasó filtro → score: ${bestScore.toFixed(2)}, keyword: ${bestKeyword}`,
          );
          return { ...h, similarity: bestScore, match_keyword: bestKeyword };
        }

        console.log(
          `❌ [fallback] "${h.nombre}" descartado → score: ${bestScore.toFixed(2)}`,
        );
        return null;
      })
      .filter(Boolean)
      .sort((a, b) => b.similarity - a.similarity)
      .slice(0, 10);

    console.log(
      `📊 [buscar_tienda] Fallback final: ${hits.length} hits seleccionados`,
    );
  }

  const ids = hits.map((h) => h.objectID);
  console.log("🆔 [buscar_tienda] IDs a consultar en Firestore:", ids);

  const idsConFlag = hits
    .filter((h) => h.plantilla === true)
    .map((h) => h.objectID);
  const idsSinFlag = hits
    .filter((h) => h.plantilla !== true)
    .map((h) => h.objectID);
  console.log("🏷️ [buscar_tienda] IDs con plantilla:", idsConFlag);
  console.log("🏷️ [buscar_tienda] IDs sin plantilla:", idsSinFlag);

  console.log(
    "⚡ [buscar_tienda] Consultando extraData y créditos en paralelo...",
  );
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
                console.error(
                  `❌ [creditos] Error obteniendo créditos para ${id}:`,
                  e.message,
                );
                return { id, mayor_a_100: false };
              }),
          ),
        )
      : Promise.resolve([]),
  ]);
  console.log(
    "✅ [buscar_tienda] extraData obtenida para IDs:",
    Object.keys(extraData),
  );

  const creditosMap = Object.fromEntries(
    creditosResults.map(({ id, mayor_a_100 }) => [id, mayor_a_100]),
  );
  console.log("💳 [buscar_tienda] creditosMap:", creditosMap);

  const data = hits.map((hit) => {
    const extra = extraData[hit.objectID] || {};
    const tienePlan =
      hit.plantilla === true && creditosMap[hit.objectID] === true;
    const eraPlantillaSinCreditos =
      hit.plantilla === true && creditosMap[hit.objectID] !== true;

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

async function buscarCategoriaAmplia({ localidad, categoria, excluir_id }) {
  const categoriaLimpia = (categoria || "").trim().toLowerCase();

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
    "tag", // necesario para poder filtrar por subcategoria en memoria luego
  ];

  let filters = [];
  if (localidad) filters.push(`lugar:"${localidad.toLowerCase().trim()}"`);
  if (categoriaLimpia) filters.push(`categoria:"${categoriaLimpia}"`);
  if (excluir_id) filters.push(`NOT objectID:"${excluir_id}"`);

  if (categoriaLimpia) {
    const refCat = db.collection("estadisticas").doc(categoriaLimpia);
    refCat
      .set({ categoria: categoriaLimpia }, { merge: true })
      .catch((e) => console.error("Stats init:", e));
    refCat
      .collection("busquedas_categoria")
      .add({
        timestamp: FieldValue.serverTimestamp(),
        localidad: localidad || null,
      })
      .catch((e) => console.error("Stats cat:", e));
  }

  console.log(
    `🚀 [buscar_categoria_amplia] Buscando "${categoriaLimpia}" en paralelo con IA de subcategoria (hitsPerPage: ${HITS_PER_PAGE_CATEGORIA_AMPLIA})`,
  );

  const { hits } = await index.search("", {
    filters: filters.join(" AND "),
    hitsPerPage: HITS_PER_PAGE_CATEGORIA_AMPLIA,
    typoTolerance: true,
    ignorePlurals: true,
    removeStopWords: true,
    attributesToRetrieve: ATTRS_CATEGORIA,
  });

  console.log(
    `✅ [buscar_categoria_amplia] ${hits.length} hits amplios de "${categoriaLimpia}"`,
  );

  return hits;
}

async function buscarPorCategoria({
  localidad,
  categoria,
  subcategoria,
  excluir_id,
  preHits,
}) {
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

  let hits;

  if (Array.isArray(preHits)) {
    // 👇 Ya tenemos los hits (vinieron de la búsqueda amplia en paralelo).
    // Solo falta filtrar por subcategoria en memoria y registrar la
    // estadística de subcategoria (la de categoria ya se registró en
    // buscarCategoriaAmplia).
    console.log(
      `♻️ [buscar_categoria] Usando ${preHits.length} hits precargados (paralelo), filtrando por subcategoria en memoria`,
    );

    if (categoriaLimpia && subcategoria) {
      const refCat = db.collection("estadisticas").doc(categoriaLimpia);
      refCat
        .collection("busquedas_subcategoria")
        .add({
          subcategoria,
          timestamp: FieldValue.serverTimestamp(),
          localidad: localidad || null,
        })
        .catch((e) => console.error("Stats sub:", e));
    }

    const subLimpia = subcategoria ? subcategoria.toLowerCase().trim() : null;

    hits = subLimpia
      ? preHits.filter(
          (h) =>
            Array.isArray(h.tag) &&
            h.tag.some((t) => (t || "").toLowerCase().trim() === subLimpia),
        )
      : preHits;

    console.log(
      `🔎 [buscar_categoria] ${hits.length} hits tras filtrar por subcategoria "${subcategoria || "(ninguna)"}"`,
    );
  } else {
    // Camino original: sin búsqueda paralela previa (ej: heredar_contexto,
    // o categorías sin subcategorías disponibles). Se comporta exactamente
    // igual que antes: Algolia filtra por categoria + subcategoria (tag).
    let filters = [];
    if (localidad) filters.push(`lugar:"${localidad.toLowerCase().trim()}"`);
    if (categoriaLimpia) filters.push(`categoria:"${categoriaLimpia}"`);
    if (subcategoria)
      filters.push(`tag:"${subcategoria.toLowerCase().trim()}"`);
    if (excluir_id) filters.push(`NOT objectID:"${excluir_id}"`);

    if (categoriaLimpia) {
      const refCat = db.collection("estadisticas").doc(categoriaLimpia);
      refCat
        .set({ categoria: categoriaLimpia }, { merge: true })
        .catch((e) => console.error("Stats init:", e));
      refCat
        .collection("busquedas_categoria")
        .add({
          timestamp: FieldValue.serverTimestamp(),
          localidad: localidad || null,
        })
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

    const resultadoAlgolia = await index.search("", {
      filters: filters.join(" AND "),
      hitsPerPage: 20,
      typoTolerance: true,
      ignorePlurals: true,
      removeStopWords: true,
      attributesToRetrieve: ATTRS_CATEGORIA,
    });

    hits = resultadoAlgolia.hits;
  }

  const ids = hits.map((h) => h.objectID);
  const idsConFlag = hits
    .filter((h) => h.plantilla === true)
    .map((h) => h.objectID);
  const idsSinFlag = hits
    .filter((h) => h.plantilla !== true)
    .map((h) => h.objectID);

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
      const tienePlan =
        hit.plantilla === true && creditosMap[hit.objectID] === true;
      const eraPlantillaSinCreditos =
        hit.plantilla === true && creditosMap[hit.objectID] !== true;

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
    (d) =>
      idsSinFlagSet.has(d.id) ||
      (idsConFlagSet.has(d.id) && creditosMap[d.id] !== true),
  );

  // 👇 si hay 3+ con plantilla toma 3, si hay menos toma las que haya (incluso 0)
  // 👇 lo mismo para sin plantilla: hasta 2, o las que existan
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

async function procesarBusquedaTienda({
  mensaje,
  contexto_previo,
  localidad,
  excluir_id,
  nombre_usuario,
}) {
  const tiempoInicioTotal = Date.now();
  console.log(
    "🏪 [procesarBusquedaTienda] INICIO | mensaje:",
    mensaje,
    "| contexto_previo:",
    JSON.stringify(contexto_previo),
    "| excluir_id:",
    excluir_id,
  );
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

  const trace = {
    tipo_busqueda: null,
    clasificacion_raw: null,
    prompt_clasificacion: null,
    subcategoria_usada: null,
    prompt_subcategoria: null,
    reenrutado_a_nombre: false,
    heredo_contexto: false,
    prompt_respuesta_gemini: null,
    respuesta_gemini_raw: null,
  };

  try {
    if (!mensaje || typeof mensaje !== "string" || !mensaje.trim()) {
      throw new Error("El campo 'mensaje' es requerido");
    }

    const momento_dia = obtenerMomentoDia();

    // ==========================================================================
    // 1a) PROMPT 1 — CLASIFICADOR
    // ==========================================================================
    const { lista, mapaSub } = await obtenerCategoriasConSub();
    const promptClasificacion = construirPromptClasificacion(
      mensaje,
      contexto_previo,
      lista,
    );
    trace.prompt_clasificacion = promptClasificacion;

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

    const clasificacion = parsearClasificacionIA(
      completionClasificacion.choices[0]?.message?.content,
    );

    trace.clasificacion_raw = clasificacion;

    let { nombre, categoria, search } = clasificacion;
    const { heredar_contexto } = clasificacion;
    trace.heredo_contexto = !!heredar_contexto;
    const categoriaLimpia = (categoria || "").toLowerCase().trim();

    if (!nombre && (categoriaLimpia === "geinz" || categoriaLimpia === "")) {
      trace.tipo_busqueda = "pedir_aclaracion";
      const mensajeAclaracion = pick(MENSAJES_PEDIR_ACLARACION_GEINZ)(
        nombre_usuario || "amigo",
      );
      return {
        id: "sin_id",
        mensaje: mensajeAclaracion,
        mensaje_safe: mensajeAclaracion,
        pedir_aclaracion: true,
        data: "null",
        subcategoria: null,
        debug_trace: trace,
      };
    }
    // 👇 mismo valor que antes se calculaba inline en la llamada a
    // buscarPorCategoria; lo adelantamos aquí porque ya está disponible
    // (excluir_id y clasificacion.excluir_id ya se conocen en este punto)
    // y lo necesitamos para poder lanzar la búsqueda amplia en paralelo.
    const excluirIdParaBusqueda = excluir_id || clasificacion.excluir_id;

    // ==========================================================================
    // 1b) PROMPT 2 — SELECTOR DE SUBCATEGORIA
    // ==========================================================================
    let subcategoria = null;
    let hitsAmpliosCategoria = null; // 👈 NUEVO: hits de la búsqueda paralela (si se lanzó)
    let subcategoriaSinMatch = false;
    if (nombre) {
      subcategoria = null;
    } else if (heredar_contexto) {
      const ctxUsuario =
        contexto_previo?.contexto_usuario ?? contexto_previo ?? {};
      subcategoria = ctxUsuario?.subcategoria || null;
    } else if (categoria) {
      const subsDisponibles = mapaSub[categoria.toLowerCase()] || [];

      if (subsDisponibles.length > 0) {
        const promptSubcategoria = construirPromptSubcategoria(
          mensaje,
          contexto_previo,
          subsDisponibles,
        );
        trace.prompt_subcategoria = promptSubcategoria;

        const [completionSubcategoria, hitsAmplios] = await Promise.all([
          openai.chat.completions.create({
            model: "gpt-5.4-nano",
            messages: [{ role: "user", content: promptSubcategoria }],
            reasoning_effort: "none",
            max_completion_tokens: 60,
          }),
          buscarCategoriaAmplia({
            localidad,
            categoria,
            excluir_id: excluirIdParaBusqueda,
          }),
        ]);

        hitsAmpliosCategoria = hitsAmplios;

        if (completionSubcategoria?.usage) {
          tokensOpenAI.prompt_tokens +=
            completionSubcategoria.usage.prompt_tokens || 0;
          tokensOpenAI.completion_tokens +=
            completionSubcategoria.usage.completion_tokens || 0;
          tokensOpenAI.total_tokens +=
            completionSubcategoria.usage.total_tokens || 0;
        }

        const rawSub = (
          completionSubcategoria.choices[0]?.message?.content || ""
        ).trim();

        const matchNegocio = rawSub.match(/^NEGOCIO\s*:\s*(.+)$/i);
        if (matchNegocio) {
          nombre = matchNegocio[1].trim();
          categoria = null;
          subcategoria = null;
          trace.reenrutado_a_nombre = true;
          hitsAmpliosCategoria = null;
        } else if (rawSub.toUpperCase() === "NINGUNA") {
          // 👇 el negocio/servicio pedido no corresponde a ninguna
          // subcategoria real de esta categoria → no hay que devolver
          // "lo más parecido", hay que decir que no hay resultados.
          subcategoria = null;
          subcategoriaSinMatch = true;
          trace.subcategoria_sin_match = true;
          hitsAmpliosCategoria = null; // ya no sirven, no se van a usar
        } else {
          subcategoria = rawSub || null;
        }
      }
    }
    trace.subcategoria_usada = subcategoria;
    // ==========================================================================
    // 2) BUSCAR — por nombre O por categoria+subcategoria, nunca ambos
    // ==========================================================================
    let resultadosBusqueda = [];

    if (nombre) {
      trace.tipo_busqueda = "nombre";
      resultadosBusqueda = await buscarPorNombreTienda({
        localidad,
        nombre,
        search,
      });
    } else if (subcategoriaSinMatch) {
      // 👇 NUEVO: cortamos aquí, sin tocar Algolia de nuevo ni devolver
      // resultados de la categoria completa sin relación con lo pedido
      trace.tipo_busqueda = "sin_resultado";
      resultadosBusqueda = [];
    } else if (categoria) {
      trace.tipo_busqueda = "categoria";
      const resultado = await buscarPorCategoria({
        localidad,
        categoria,
        subcategoria,
        excluir_id: excluirIdParaBusqueda,
        preHits: hitsAmpliosCategoria,
      });
      resultadosBusqueda = resultado.data;
    } else {
      trace.tipo_busqueda = "sin_criterio";
    }

    // 3) 2da IA (GEMINI) -> elige 1 negocio y redacta el mensaje
    let response;
    const MENSAJES_SIN_RESULTADO = [
      "Uy 😕 no encontré nada con esa búsqueda. Puedes intentar con otra.",
      "Mmm, por ahora no me sale ningún resultado con eso 👀.",
      "Nada por aquí 😅 Intenta hacer otra búsqueda.",
      "No encontré resultados esta vez 🤔.",
      "Parece que no hay nada que coincida con esa búsqueda 😕.",
      "No me apareció ningún resultado 🔍.",
      "Esta vez no encontré nada 😅. Si quieres, prueba con otra búsqueda.",
      "No hubo suerte con esa búsqueda 😕.",
      "Por ahora no tengo resultados para eso 👀.",
      "No encontré nada con esa búsqueda 🚫. Puedes intentar con otra.",
    ];
    if (!resultadosBusqueda.length) {
      trace.tipo_busqueda =
        trace.tipo_busqueda === "sin_criterio"
          ? "sin_criterio"
          : "sin_resultado";
      response = {
        id: "sin_id",
        mensaje: pick(MENSAJES_SIN_RESULTADO),
        intencion: "SIN_DATOS",
      };
    } else {
      // ==========================================================================
      // 👇 FIX: el contexto previo SOLO es relevante si el clasificador
      // detectó heredar_contexto=true (ej: "otro", "otra opción", "dame otro
      // similar"). En cualquier otra búsqueda (nombre nuevo o categoria nueva)
      // el contexto previo es ruido que puede confundir a Gemini y hacer que
      // devuelva el negocio viejo en vez de uno de los resultados nuevos.
      // ==========================================================================
      const contextoEsRelevante = heredar_contexto === true;
      const contextoStr = contextoEsRelevante
        ? JSON.stringify(
            contexto_previo?.contexto_usuario ?? contexto_previo ?? {},
          )
        : "null (no aplica, esta es una búsqueda nueva, ignora cualquier negocio anterior)";

      // 👇 SOLO estos campos van a Gemini — nada de img, wha, pla,
      // msje_pla_wa, similarity, loc, cat, tipo, etc.
      const datosParaPrompt = resultadosBusqueda.map((d) => ({
        id: d.id,
        name: d.name || d.tienda || "",
        desc: d.desc || "",
        open_closed: d.open_state === true ? "abierto" : "cerrado",
      }));

      const promptRespuesta = `Responde en JSON válido.
DATOS:
${JSON.stringify(datosParaPrompt)}
CONTEXTO PREVIO (negocio ya consultado antes, SOLO úsalo si el CONTEXTO PREVIO no es null):
${contextoStr}
REGLAS:
- El id que devuelvas DEBE existir siempre dentro de DATOS, nunca inventes ni regreses un id que no esté ahí, incluso si el CONTEXTO PREVIO menciona otro negocio
- Si el CONTEXTO PREVIO no es null Y el usuario pregunta algo sobre ese mismo negocio → responde sobre ese negocio usando los DATOS disponibles
- Nunca SALUDES con buenos o hola, habla como si fuera conversación continua, como si ya se conocieran, LENGUAJE LOCAL SIEMPRE MUY AMIGABLE nada robótico ni corporativo, habla como un pata de Barranca peruano, canchero, ALGO INFORMATIVO SIN VENDER TANTO
- Elegir SOLO 1 negocio según lo que pide el usuario
- mensaje: 1 línea, máximo 2 frases
- incluir SOLO si existen: estado siempre(🟢 Abierto / 🔴 Cerrado DE open_closed DE DATOS), descripción siempre informativa, métodos de pago (máx 2), comodidades (máx 2) solo si existen, si no existen → NO mencionarlos
- El usuario se llama: ${nombre_usuario || ""} úsalo siempre
- si falta info → mencionar app Geinz
- sin comillas dentro del mensaje
- USA EL MOMENTO DEL DIA SIEMPRE QUE ES: ${momento_dia}
- Priorizar negocios con open_closed: "abierto" al elegir
- NUNCA digas frases como "mensaje predeterminado", "mensaje genérico", "esto es automático" ni nada que describa la naturaleza de tu propia respuesta
- Si el usuario menciona un nombre exacto de negocio → elegir ese sin importar si está abierto o cerrado
FORMATO OBLIGATORIO:
{"id":"{id}","mensaje":"...","intencion":"NEGOCIO"}`;

      trace.prompt_respuesta_gemini = promptRespuesta;

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
        console.error(
          "❌ Error Gemini API (tienda):",
          geminiRes.status,
          errText,
        );
        response = {
          id: "sin_id",
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
        trace.respuesta_gemini_raw = rawText;
        response = parsearRespuestaIA(rawText);
        if (!response || !Object.keys(response).length) {
          response = {
            id: "sin_id",
            mensaje: rawText || "Sin respuesta",
            intencion: "ERROR_FORMATO_IA",
          };
        }
      }

      // ==========================================================================
      // 👇 FIX: log de validación — si Gemini devuelve un id que NO está en
      // datosParaPrompt, es una señal clara de que se contaminó con el
      // contexto previo (o alucinó). Esto NO corrige el id (eso ya lo hace
      // el match más abajo, que simplemente no encuentra nada y queda
      // vacío), pero te deja rastro exacto en los logs de cuándo pasa.
      // ==========================================================================
      const idDevuelto = response?.id;
      if (
        idDevuelto &&
        idDevuelto !== "sin_id" &&
        !datosParaPrompt.some((d) => d.id === idDevuelto)
      ) {
        console.warn(
          "⚠️ [procesarBusquedaTienda] Gemini devolvió un id fuera de DATOS (posible contaminación de contexto) | id_devuelto:",
          idDevuelto,
          "| ids_validos:",
          datosParaPrompt.map((d) => d.id),
          "| contexto_previo_id:",
          contexto_previo?.contexto_usuario?.id ?? contexto_previo?.id,
          "| heredo_contexto:",
          heredar_contexto,
        );
      }
    }

    // 4) ARMAR SALIDA — usa apiData completa (con img, wha, alias, pla, etc.),
    //    esto NUNCA se le mandó a Gemini, solo se usa aquí para el match final
    const apiData = resultadosBusqueda;

    const idFinal = response?.id || "sin_id";
    const mensajeFinal = String(response?.mensaje || "").trim();

    const match = apiData.find(
      (d) =>
        String(d?.id || "")
          .trim()
          .replace(/"/g, "") === String(idFinal).trim().replace(/"/g, ""),
    );

    const imagenFinal = match?.img || "";
    const keywords = response?.keywords || response?.palabras_clave || [];
    const kwArr = Array.isArray(keywords) ? keywords : [];
    const subcategoria_tienda = subcategoria || "";

    const nombre_negocio =
      match?.nombre ||
      match?.name ||
      match?.titulo ||
      match?.negocio ||
      match?.tienda ||
      "";
    const categoria_para_data = match?.cat || match?.category || "";
    const sub = subcategoria_tienda
      ? `${categoria_para_data} sub:${subcategoria_tienda}`
      : categoria_para_data;
    const extra = kwArr.length ? kwArr.join(",") : "null";

    const data = ["TIENDA", nombre_negocio, sub, extra, idFinal].join("|");

    let categoria_match = match?.cat || match?.category || "general";
    const whatsappFinal = match?.wha || "";
    const msje_pla_wa = match?.msje_pla_wa || "";
    const usa_plantilla = match?.pla || false;
    const era_plantilla_pero_misio = match?.era_plantilla || false;
    const alias_tienda = match?.alias || "";

    const categoriaFinal = encodeURIComponent(categoria_match).replace(
      /%20/g,
      "+",
    );

    const link_construido = alias_tienda
      ? `https://geinztech.com/perfil/${alias_tienda}`
      : "";

    let mensaje_safe = usa_plantilla
      ? mensajeFinal
      : mensajeFinal
        ? link_construido
          ? `${mensajeFinal}  ${pick(CTAS)}: ${link_construido}`
          : mensajeFinal
        : link_construido
          ? `${pick(CTAS)}: ${link_construido}`
          : "";

    // 👇 Solo cuando SÍ se hizo una búsqueda real y no hubo resultados
    // (no aplica si fue "sin_criterio", que es un caso distinto)
    if (trace.tipo_busqueda === "sin_resultado") {
      mensaje_safe =
        `${mensaje_safe} ${pick(MENSAJES_INVITACION_TIENDA)}`.trim();
    }
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
      total_tokens_combinado:
        tokensOpenAI.total_tokens + tokensGemini.totalTokenCount,
    };

    console.log(
      "🧭 [procesarBusquedaTienda] TRACE:",
      JSON.stringify({
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
      }),
    );

    return {
      ...response,
      id: idFinal,
      imagen: esTiendaSinPlantilla ? "" : imagenFinal,
      mensaje_safe,
      data,
      subcategoria: subcategoria || null,
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
    };
  } catch (error) {
    console.error("❌ [procesarBusquedaTienda] Error:", error.message);
    console.error("❌ [procesarBusquedaTienda] Stack:", error.stack);
    console.error(
      "❌ [procesarBusquedaTienda] Trace hasta el fallo:",
      JSON.stringify(trace),
    );

    error.trace = trace;
    error.tokens_usados = {
      openai: tokensOpenAI,
      gemini: tokensGemini,
      total_tokens_combinado:
        tokensOpenAI.total_tokens + tokensGemini.totalTokenCount,
    };
    error.tiempo_total_ms = Date.now() - tiempoInicioTotal;

    throw error;
  }
}

exports.geinz_buscar_unificado = onRequest(async (req, res) => {
  const tiempoInicioTotal = Date.now();

  try {
    const { mensaje, contexto_previo, localidad, excluir_id, nombre_usuario } =
      req.body;

    if (!mensaje || typeof mensaje !== "string" || !mensaje.trim()) {
      return res
        .status(400)
        .json({ ok: false, error: "El campo 'mensaje' es requerido" });
    }

    const resultado = await procesarBusquedaTienda({
      mensaje,
      contexto_previo,
      localidad,
      excluir_id,
      nombre_usuario,
    });

    return res.status(200).json(resultado);
  } catch (error) {
    console.error("❌ [geinz_buscar_unificado] Error:", error.message);

    const tiempoTotalMs = Date.now() - tiempoInicioTotal;

    return res.status(500).json({
      ok: false,
      error: error.message,
      debug_trace: error.trace || null,
      tokens_usados: error.tokens_usados || null,
      tiempo_total_ms: tiempoTotalMs,
      tiempo_total_seg: Number((tiempoTotalMs / 1000).toFixed(2)),
    });
  }
});

exports.procesarBusquedaTienda = procesarBusquedaTienda;

// ============================================================================
// ============================================================================
//   4. MÓDULO TURISMO
// ============================================================================
// ============================================================================
let subcategoriasTurismoCache = null;
let subcategoriasTurismoCacheTimestamp = 0;

const HITS_PER_PAGE_TURISMO_AMPLIO = 100;

function obtenerMomentoDia() {
  const hora = Number(
    new Intl.DateTimeFormat("en-US", {
      hour: "numeric",
      hour12: false,
      timeZone: "America/Lima",
    }).format(new Date()),
  );

  if (hora >= 5 && hora < 12) return "mañana";
  if (hora >= 12 && hora < 19) return "tarde";
  return "noche";
}

function pick(arr) {
  return !Array.isArray(arr) || !arr.length
    ? ""
    : arr[Math.floor(Math.random() * arr.length)];
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

exports.procesarBusquedaTurismo = procesarBusquedaTurismo;

// ============================================================================
// ============================================================================
//   5. MÓDULO EMERGENCIAS (SALUD / SEGURIDAD)
const HITS_PER_PAGE_EMERGENCIA_AMPLIO = 60;

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
  console.log(
    "🟢 [clasificarMensaje] FIN | categoria:",
    categoria,
    "| tiempo_ms:",
    tiempoMs,
  );

  return { categoria, usage: { ...usage, intentos: intento } };
}

async function buscarLugaresAmplio(localidad) {
  const t0 = Date.now();

  let filtersArray = [];
  if (localidad) filtersArray.push(`lugar:"${localidad}"`);

  const filters =
    filtersArray.length > 0 ? filtersArray.join(" AND ") : undefined;

  console.log(
    `🚀 [buscarLugaresAmplio] Buscando "${localidad || ""}" en paralelo con IA clasificadora (hitsPerPage: ${HITS_PER_PAGE_EMERGENCIA_AMPLIO})`,
  );

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
  console.log(
    "🟡 [buscarLugares] INICIO | localidad:",
    localidad,
    "| categoria:",
    categoria,
  );

  const t0 = Date.now();

  let hitsCrudos;

  if (Array.isArray(preHits)) {
    // 👇 Ya tenemos los hits (vinieron de la búsqueda amplia en paralelo).
    // Solo falta filtrar por categoria en memoria, sin tocar Algolia de nuevo.
    console.log(
      `♻️ [buscarLugares] Usando ${preHits.length} hits precargados (paralelo), filtrando por categoria en memoria`,
    );

    const categoriaLimpia =
      categoria && categoria !== "general"
        ? categoria.toLowerCase().trim()
        : null;

    hitsCrudos = categoriaLimpia
      ? preHits.filter(
          (h) => (h.categoria || "").toLowerCase().trim() === categoriaLimpia,
        )
      : preHits;

    console.log(
      `🔎 [buscarLugares] ${hitsCrudos.length} hits tras filtrar por categoria "${categoria || "(ninguna)"}"`,
    );
  } else {
    // Camino original: sin búsqueda paralela previa — Algolia filtra
    // directo por localidad + categoria, tal como antes.
    let filtersArray = [];
    if (localidad) filtersArray.push(`lugar:"${localidad}"`);
    if (categoria && categoria !== "general")
      filtersArray.push(`categoria:"${categoria}"`);

    const filters =
      filtersArray.length > 0 ? filtersArray.join(" AND ") : undefined;

    console.log(
      "🔎 [buscarLugares] filtros Algolia:",
      filters || "(sin filtro)",
    );

    const result = await index.search("", { filters, hitsPerPage: 20 });
    hitsCrudos = result.hits;
  }

  console.log("📦 [buscarLugares] TOTAL HITS CRUDOS:", hitsCrudos.length);

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
  console.log("⏱️ Algolia:", { tiempo_ms: tiempoMs, resultados: data.length });

  return { data, usage: { tiempo_ms: tiempoMs } };
}

async function seleccionarContacto(
  entidadesLigeras,
  mensajeUsuario,
  nombreUsuario,
) {
  console.log(
    "🟡 [seleccionarContacto] INICIO | entidades disponibles:",
    entidadesLigeras.length,
  );
  console.log(
    "📤 [seleccionarContacto] ENTIDADES ENVIADAS A GEMINI (solo id+n):",
    JSON.stringify(entidadesLigeras),
  );

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
  console.log("🤖 RAW Gemini:", rawText);

  let outputIA;
  try {
    outputIA = JSON.parse(rawText.replace(/```json|```/g, "").trim());
  } catch (e) {
    console.log("❌ [seleccionarContacto] ERROR parseando Gemini:", e.message);
    throw new Error("No se pudo parsear la respuesta de Gemini: " + rawText);
  }

  const usage = {
    tiempo_ms: tiempoMs,
    prompt_tokens: json.usageMetadata?.promptTokenCount ?? null,
    completion_tokens: json.usageMetadata?.candidatesTokenCount ?? null,
    total_tokens: json.usageMetadata?.totalTokenCount ?? null,
  };

  console.log("⏱️ Gemini:", usage);
  console.log(
    "🟢 [seleccionarContacto] FIN | outputIA:",
    JSON.stringify(outputIA),
  );

  return { outputIA, usage };
}

function construirRespuestaFinal(outputIA, data) {
  console.log(
    "🟡 [construirRespuestaFinal] INICIO | buscando id:",
    outputIA.id,
    "entre",
    data.length,
    "contactos",
  );

  const contacto = data.find((c) => c.id === outputIA.id);

  if (!contacto) {
    console.log(
      "❌ [construirRespuestaFinal] NO SE ENCONTRÓ la entidad con id:",
      outputIA.id,
    );
    return { error: "No se encontró la entidad", tiene_link: false };
  }

  console.log(
    "✅ [construirRespuestaFinal] CONTACTO ENCONTRADO:",
    JSON.stringify(contacto),
  );

  const listaLlamadas = contacto.num?.llamada || [];
  const listaWhatsapp = contacto.num?.whatsapp?.[0]
    ? contacto.num.whatsapp
    : [];

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

  const resultadoFinal = tiene_link
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

  console.log(
    "🟢 [construirRespuestaFinal] FIN | resultado:",
    JSON.stringify(resultadoFinal),
  );

  return resultadoFinal;
}

async function enviarPlantillaEmergencia(recipientPhoneNumber, resultado) {
  const telefonosLine = [
    resultado.telefonos?.length
      ? `📞 Llámalos al: ${resultado.telefonos[0]}`
      : "",
    resultado.whatsapp?.length
      ? ` 💬 Escríbeles al: ${resultado.whatsapp[0]}`
      : "",
  ]
    .join(" o ")
    .trim();

  const url = `https://graph.facebook.com/${WHATSAPP_API_VERSION}/${WHATSAPP_PHONE_NUMBER_ID}/messages`;

  const body = {
    messaging_product: "whatsapp",
    to: recipientPhoneNumber,
    type: "template",
    template: {
      name: "emergencia_user",
      language: { code: "es" },
      components: [
        {
          type: "header",
          parameters: [{ type: "text", text: "MANTEN LA CALMA" }],
        },
        {
          type: "body",
          parameters: [
            { type: "text", text: resultado.mensaje_texto },
            { type: "text", text: telefonosLine },
          ],
        },
        {
          type: "button",
          sub_type: "url",
          index: "0",
          parameters: [
            { type: "text", text: `${resultado.lat},${resultado.lng}` },
          ],
        },
      ],
    },
  };

  console.log("📤 [enviarPlantillaEmergencia] BODY:", JSON.stringify(body));

  const resp = await fetch(url, {
    method: "POST",
    headers: {
      Authorization: `Bearer ${WHATSAPP_TOKEN}`,
      "Content-Type": "application/json",
    },
    body: JSON.stringify(body),
  });

  if (!resp.ok) {
    const errText = await resp.text();
    console.log("❌ [enviarPlantillaEmergencia] ERROR:", resp.status, errText);
    throw new Error(
      `Error enviando plantilla emergencia: ${resp.status} ${errText}`,
    );
  }

  console.log("✅ [enviarPlantillaEmergencia] Plantilla enviada OK");
  guardarMensajeHistorial({
    numero_usuario: recipientPhoneNumber,
    remitente: "bot",
    tipo: "plantilla",
    contenido: resultado.mensaje_texto || "",
    extra: { plantilla: "emergencia_user" },
  }).catch(() => {});

  return resp.json();
}

async function enviarMensajeTextoEmergencia(recipientPhoneNumber, resultado) {
  const url = `https://graph.facebook.com/${WHATSAPP_API_VERSION}/${WHATSAPP_PHONE_NUMBER_ID}/messages`;

  const body = {
    messaging_product: "whatsapp",
    to: recipientPhoneNumber,
    type: "text",
    text: { body: resultado.mensaje_safe },
  };

  console.log("📤 [enviarMensajeTextoEmergencia] BODY:", JSON.stringify(body));

  const resp = await fetch(url, {
    method: "POST",
    headers: {
      Authorization: `Bearer ${WHATSAPP_TOKEN}`,
      "Content-Type": "application/json",
    },
    body: JSON.stringify(body),
  });

  if (!resp.ok) {
    const errText = await resp.text();
    console.log(
      "❌ [enviarMensajeTextoEmergencia] ERROR:",
      resp.status,
      errText,
    );
    throw new Error(
      `Error enviando mensaje texto emergencia: ${resp.status} ${errText}`,
    );
  }

  console.log("✅ [enviarMensajeTextoEmergencia] Mensaje enviado OK");
  guardarMensajeHistorial({
    numero_usuario: recipientPhoneNumber,
    remitente: "bot",
    tipo: "texto",
    contenido: resultado.mensaje_safe || "",
  }).catch(() => {});
  return resp.json();
}

async function enviarRespuestaEmergencia(recipientPhoneNumber, resultado) {
  if (resultado.tiene_link) {
    console.log("🔀 [enviarRespuestaEmergencia] Rama CON LINK → plantilla");
    return enviarPlantillaEmergencia(recipientPhoneNumber, resultado);
  } else {
    console.log("🔀 [enviarRespuestaEmergencia] Rama SIN LINK → texto plano");
    return enviarMensajeTextoEmergencia(recipientPhoneNumber, resultado);
  }
}

exports.obtener_lugares_emergencia_Actualizado = onRequest(async (req, res) => {
  const tInicio = Date.now();
  try {
    const { localidad, mensaje, nombreUsuario, numero_usuario } = req.body;

    console.log(
      "🚀 [obtener_lugares_emergencia] REQUEST BODY:",
      JSON.stringify(req.body),
    );

    if (!mensaje || typeof mensaje !== "string" || !mensaje.trim()) {
      console.log("❌ falta el campo 'mensaje'");
      return res
        .status(400)
        .json({ ok: false, error: "El campo 'mensaje' es requerido" });
    }

    if (!numero_usuario) {
      console.log("❌ falta el campo 'numero_usuario'");
      return res.status(400).json({
        ok: false,
        error:
          "El campo 'numero_usuario' es requerido para enviar la respuesta por WhatsApp",
      });
    }

    // ============================================================
    // 1) PARALELIZACIÓN: la IA clasificadora (SALUD/SEGURIDAD) y la
    // búsqueda amplia en Algolia (solo por localidad) corren AL MISMO
    // TIEMPO con Promise.all, en vez de esperar a que la IA termine para
    // recién ahí tocar Algolia. La búsqueda amplia solo depende de
    // "localidad" (ya se conoce desde el inicio), así que no hay ningún
    // dato inventado ni adelantado que dependa de la respuesta de la IA.
    // ============================================================
    const [
      { categoria, usage: usageOpenAI },
      { hits: hitsAmplios, usage: usageAlgoliaAmplio },
    ] = await Promise.all([
      clasificarMensaje(mensaje),
      buscarLugaresAmplio(localidad),
    ]);

    // 2) Filtrado en memoria por categoria (sin nueva consulta a Algolia)
    const { data, usage: usageAlgoliaFiltrado } = await buscarLugares(
      localidad,
      categoria,
      hitsAmplios,
    );

    console.log("📊 DATA FILTRADA POR CATEGORIA:", data.length, "resultados");

    // 3) A Gemini SOLO le llega { id, n } de cada entidad
    const entidadesLigeras = construirEntidadesLigeras(data);
    const { outputIA, usage: usageGemini } = await seleccionarContacto(
      entidadesLigeras,
      mensaje,
      nombreUsuario || "usuario",
    );

    // 4) Armar la respuesta final (con o sin link)
    const resultado = construirRespuestaFinal(outputIA, data);

    // 5) Enviar DIRECTO por WhatsApp (antes esto lo hacía n8n con Switch1 + template_emergencia)
    if (!resultado.error) {
      await enviarRespuestaEmergencia(numero_usuario, resultado);
    } else {
      console.log(
        "⚠️ No se envía WhatsApp porque hubo error armando el resultado:",
        resultado.error,
      );
    }

    const tiempoTotalMs = Date.now() - tInicio;

    const debugInfo = {
      tiempo_total_ms: tiempoTotalMs,
      categoria_detectada: categoria,
      openai: usageOpenAI,
      algolia: usageAlgoliaAmplio,
      algolia_filtrado_local: usageAlgoliaFiltrado,
      gemini: usageGemini,
    };

    console.log("📊 RESUMEN TIEMPOS/TOKENS:", JSON.stringify(debugInfo));

    res.set("Cache-Control", "public, max-age=300");
    return res.status(200).json({ ...resultado, _debug: debugInfo });
  } catch (error) {
    console.error("❌ ERROR obtener_lugares_emergencia:", error.message);
    return res
      .status(500)
      .json({ ok: false, mensaje: "Error interno al buscar lugares" });
  }
});

async function procesarEmergencia({
  localidad,
  mensaje,
  nombreUsuario,
  numero_usuario,
}) {
  const tInicio = Date.now();

  console.log(
    "🚀 [procesarEmergencia] INICIO | mensaje:",
    mensaje,
    "| numero_usuario:",
    numero_usuario,
  );

  // ============================================================
  // 1) PARALELIZACIÓN: mismo cambio que en el endpoint HTTP — la IA
  // clasificadora y la búsqueda amplia por localidad corren juntas.
  // ============================================================
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

  console.log(
    "📊 [procesarEmergencia] DATA FILTRADA POR CATEGORIA:",
    data.length,
    "resultados",
  );

  const entidadesLigeras = construirEntidadesLigeras(data);
  const { outputIA, usage: usageGemini } = await seleccionarContacto(
    entidadesLigeras,
    mensaje,
    nombreUsuario || "usuario",
  );

  const resultado = construirRespuestaFinal(outputIA, data);

  if (!resultado.error) {
    await enviarRespuestaEmergencia(numero_usuario, resultado);
  } else {
    console.log(
      "⚠️ [procesarEmergencia] No se envía WhatsApp, hubo error:",
      resultado.error,
    );
  }

  const tiempoTotalMs = Date.now() - tInicio;

  const debugInfo = {
    tiempo_total_ms: tiempoTotalMs,
    categoria_detectada: categoria,
    openai: usageOpenAI,
    algolia: usageAlgoliaAmplio,
    algolia_filtrado_local: usageAlgoliaFiltrado,
    gemini: usageGemini,
  };

  console.log("📊 [procesarEmergencia] RESUMEN:", JSON.stringify(debugInfo));

  return { ...resultado, _debug: debugInfo };
}
exports.procesarEmergencia = procesarEmergencia;

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
- EVITA SI TIPO ES TURISMO
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
- productos: corregir ortografía, sin diminutivos, no inventar, no duplicar.
- comodidades: detectar menciones implícitas (ej: "con wifi" → "wifi").
- Si tipo es "bot" y hay productos → buscar promociones relevantes.
- Sin campo vacío: usa null o [] según corresponda.
- metodos_pago: solo de ["yape","plin","efectivo","agora","visa","mastercard"]
- comodidades: solo de ["aire_acondicionado","camaras_de_seguridad","enchufe","estacionamiento","ingreso_mascotas","mesa_para_ninos","sala_de_espera","sala_juegos","servicios_higienicos","wifi","zona_expandida"]
- traer_promos: true SOLO si pide promos/ofertas/descuentos de forma general (cualquier sinónimo), sin dar tienda, producto, precio, pago ni comodidad. Si da algo específico → false.`;
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
    model: "gpt-5-mini",
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
// ============================================================================
//   6. MÓDULO PROMOCIONES
// ============================================================================
// ============================================================================

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

REGLAS: usa solo estos datos, no inventes. Misma tienda=compara y elige mejor calce. p_ok=false avisa y ofrece alt con pago real. Todo vacío=recomienda directo. Prioriza mayor sc. Tono peruano natural sin saludar. No menciones "score". Usa el momento del día natural. MENSAJE MÁX 200 CARACTERES, sin listar sabores/variantes si son 2+ promos, solo tienda+frase gancho corta c/u. Máx 2 emojis. Sin promos ni alt: dilo directo. Nunca saludes con buenos o hola(hola/qué tal/qué buena).

DECISIÓN: 2+ relevantes→varios=true+ids | 1→varios=false+id | 0→varios=false,id="none"`;
}

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
      console.error(
        "❌ [info_negocio] No se encontró objeto por id:",
        id,
        e.message,
      );
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

async function resolverInfoNegocio({
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

  const hit = await obtenerNegocioPorIdONombre({ id, nombre, localidad });

  if (!hit) {
    return {
      id: "sin_id",
      mensaje: "No encontré ese negocio, prueba con otro nombre",
      mensaje_safe: "No encontré ese negocio, prueba con otro nombre",
      intencion: "SIN_DATOS",
      tokens_usados: { gemini: tokensGemini },
      tiempo_total_ms: Date.now() - tiempoInicioTotal,
    };
  }

  const extraData = await obtenerDatosPorIds(localidad, [hit.objectID]);
  const extra = extraData[hit.objectID] || {};
  const open_state = verificar_apertura_tienda(extra.horario);

  const datoParaPrompt = {
    id: hit.objectID,
    tienda: hit.nombre || "",
    desc: (hit.descripcion || "").substring(0, 150),
    loc: hit.lugar || "",
    cat: hit.categoria || "",
    tipo: "tienda",
    open_closed: open_state === true ? "abierto" : "cerrado",
  };

  const promptRespuesta = `Responde en JSON válido.
DATOS DEL NEGOCIO:
${JSON.stringify(datoParaPrompt)}
El usuario se llama: ${nombre_usuario || ""} úsalo siempre
MENSAJE/PREGUNTA DEL USUARIO: "${mensaje || ""}"
REGLAS:
- Responde AL GRANO exactamente lo que el usuario pregunta sobre ESTE negocio (id:${hit.objectID}, nombre:${hit.nombre}), basándote SOLO en los DATOS
- Nunca SALUDES con buenos o hola, habla como conversación continua, como si ya se conocieran, LENGUAJE LOCAL SIEMPRE MUY AMIGABLE nada robótico ni corporativo, habla como un pata de Barranca peruano, canchero, ALGO INFORMATIVO SIN VENDER TANTO
- mensaje: máximo 2 líneas (máximo 2 frases)
- incluir SOLO si existen: estado (🟢 Abierto / 🔴 Cerrado según open_closed), descripción siempre informativa
- sin comillas dentro del mensaje
- USA EL MOMENTO DEL DIA SIEMPRE QUE ES: ${momento_dia}
- NUNCA digas frases como "mensaje predeterminado", "mensaje genérico", "esto es automático" ni nada que describa la naturaleza de tu propia respuesta
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
    console.error(
      "❌ [resolverInfoNegocio] Error Gemini API:",
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
  const imagenFinal = hit.imagen_bot || "";
  const nombre_negocio = hit.nombre || "";
  const categoria_match = hit.categoria || "general";
  const categoriaFinal = encodeURIComponent(categoria_match).replace(
    /%20/g,
    "+",
  );
  const alias_tienda = hit.alias || "";

  const link_construido = alias_tienda
    ? `https://geinztech.com/perfil/${alias_tienda}`
    : "";

  const mensaje_safe = link_construido
    ? `${mensajeFinal} 📲 Si quieres más info, chécalo en Geinz: ${link_construido}`
    : mensajeFinal;

  const imagen_stiker = pick(STIKER_TIENDA);

  const data = [
    "TIENDA",
    nombre_negocio,
    categoria_match,
    "null",
    idFinal,
  ].join("|");

  const tiempoTotalMs = Date.now() - tiempoInicioTotal;

  return {
    ...response,
    id: idFinal,
    imagen: imagenFinal,
    mensaje_safe,
    data,
    siker: imagen_stiker,
    cat_detectada: categoriaFinal,
    nombre_negocio,
    alias_tienda,
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

// El endpoint HTTP ahora solo delega
exports.geinz_info_negocio = onRequest(async (req, res) => {
  try {
    const { id, nombre, mensaje, localidad, nombre_usuario } = req.body;
    if (!id && (!nombre || !nombre.trim())) {
      return res
        .status(400)
        .json({ ok: false, error: "Debes enviar 'id' o 'nombre' del negocio" });
    }
    const resultado = await resolverInfoNegocio({
      id,
      nombre,
      mensaje,
      localidad,
      nombre_usuario,
    });
    console.log(
      "🧭 [geinz_info_negocio] TRACE:",
      JSON.stringify({
        id_solicitado: id || null,
        nombre_solicitado: nombre || null,
        resultado_id: resultado.id,
        tiempo_total_ms: resultado.tiempo_total_ms,
      }),
    );
    return res.status(200).json(resultado);
  } catch (error) {
    console.error("❌ [geinz_info_negocio] Error:", error.message);
    return res.status(500).json({ ok: false, error: error.message });
  }
});

exports.resolverInfoNegocio = resolverInfoNegocio;

// ============================================================
// DISPERSADOR IA — Clasificador de intención (antes nodo n8n "dispersador IA2")
//    Motor: OpenAI gpt-5.4-mini, reasoning_effort: low
//    Prompt tal cual el original — NO TOCAR
// ============================================================

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

// PROMPT TAL CUAL — NO TOCAR
function construirSystemMessageDispersador(contextoUsuario) {
  return `Eres un clasificador. Responde SOLO con una palabra.
CONTEXTO: ${JSON.stringify(contextoUsuario || {})}
PASOS (seguir en orden):
0. Si CONTEXTO.extra contiene "ESPERANDO_NOMBRE_PROMO" Y el mensaje NO tiene señales de cambio de intención (no dice "no", "olvida", "mejor otra cosa", "ya no", ni pide algo claramente distinto como emergencia/peligro) → responde PROMOCIONES. Para.
1. VERIFICA EL EXTRA PARA QUE TENGAS MAYOR CONTEXTO Y CLASIFIQUES SEGUN LA CONVERSACION
2. Si el mensaje tiene "otro/otra/otros" → responde NEGOCIO o TURISMO según el contexto. Para.
3. Si el mensaje menciona un nombre, negocio o lugar → ignora el contexto y clasifica solo.
4. Si hay contexto previo y el mensaje menciona ofertas, promos, descuentos, precios o falta de dinero sin nombrar nada nuevo → responde PROMOCIONES. Para.
5. CONTINUIDAD_INFO solo si: hay contexto previo, no hay nombre nuevo, y el mensaje pregunta algo concreto del mismo negocio y el mismo "tipo" sino obiar esto.
6. Si dudas entre CONTINUIDAD_INFO y otra → elige NEGOCIO o TURISMO.
CATEGORÍAS:
- EMERGENCIA: peligro de vida real ahora mismo, o pide número de SAMU/policía/serenazgo/salud.
- PELIGRO: amenaza, extorsión o delito real. No expresiones de enojo.
- CONTINUIDAD_INFO: pregunta concreta sobre el mismo negocio del contexto y el mismo "tipo" .
- PROMOCIONES: busca descuentos, ofertas, precios bajos o dice que no tiene dinero.
- NEGOCIO: busca tienda, producto, servicio, o quiere comer/tomar/consumir algo nombre de tienda o negocio.
- TURISMO: busca lugares para visitar. No incluye querer comer o consumir.
- GEINZ: saludo, soporte, registrar su negocio, mensaje sin sentido claro.
PRIORIDAD: EMERGENCIA > PELIGRO > paso 0 (ESPERANDO) > CONTINUIDAD_INFO > PROMOCIONES > NEGOCIO > TURISMO > GEINZ
Responde solo: EMERGENCIA | PELIGRO | CONTINUIDAD_INFO | PROMOCIONES | NEGOCIO | TURISMO | GEINZ`;
}

async function llamarOpenAIDispersador(mensajeUsuario, contextoUsuario) {
  const systemMessage = construirSystemMessageDispersador(contextoUsuario);

  const completion = await openai.chat.completions.create({
    model: "gpt-5.4-mini",
    messages: [
      { role: "system", content: systemMessage },
      { role: "user", content: mensajeUsuario },
    ],
    reasoning_effort: "low",
  });

  const texto = (completion.choices[0]?.message?.content || "").trim();

  const tokens = {
    prompt_tokens: completion.usage?.prompt_tokens || 0,
    completion_tokens: completion.usage?.completion_tokens || 0,
    thoughts_tokens:
      completion.usage?.completion_tokens_details?.reasoning_tokens || 0,
    total_tokens: completion.usage?.total_tokens || 0,
  };

  return { texto, tokens, raw: completion };
}

const CATEGORIAS_VALIDAS = [
  "EMERGENCIA",
  "PELIGRO",
  "CONTINUIDAD_INFO",
  "PROMOCIONES",
  "NEGOCIO",
  "TURISMO",
  "GEINZ",
];

function limpiarCategoria(raw) {
  const limpio = (raw || "").trim().toUpperCase();
  return CATEGORIAS_VALIDAS.includes(limpio) ? limpio : "GEINZ"; // fallback seguro
}

// ============================================================
// CLOUD FUNCTION PRINCIPAL
// ============================================================

exports.dispersador_geinz = onRequest(async (req, res) => {
  const inicio = Date.now();
  const tokensOpenAI = tokensVacios();

  try {
    const { mensaje, contexto_usuario } = req.body;

    if (!mensaje || typeof mensaje !== "string" || !mensaje.trim()) {
      return res
        .status(400)
        .json({ ok: false, error: "El campo 'mensaje' es requerido" });
    }

    const { texto: categoriaRaw, tokens } = await llamarOpenAIDispersador(
      mensaje,
      contexto_usuario,
    );
    sumarTokens(tokensOpenAI, tokens);

    const categoria = limpiarCategoria(categoriaRaw);

    const tiempo_ms = Date.now() - inicio;

    console.log(
      "🧭 CATEGORIA:",
      categoria,
      "| RAW:",
      categoriaRaw,
      "| TOKENS:",
      JSON.stringify(tokensOpenAI),
    );

    return res.status(200).json({
      ok: true,
      categoria,
      tokens_usados: tokensOpenAI,
      tiempo_ms,
    });
  } catch (error) {
    console.error("❌ Error dispersador_geinz:", error.message);
    const tiempo_ms = Date.now() - inicio;
    return res.status(500).json({ ok: false, error: error.message, tiempo_ms });
  }
});

function construirPromptGeinz(mensaje, nombreUsuario) {
  return `Eres "DANIEL" el asistente oficial de Geinz (RUC 20615632580) para Barranca.
FUNCIONES: Recomendar negocios con horarios reales, turismo local, emergencias y promos de Barranca.

PERSONALIDAD:
- Pata peruano, canchero y natural, nada robótico ni corporativo
- Si te falta info, pide más detalle con frases distintas cada vez, nunca la misma dos veces seguidas
- Cierra siempre motivando a seguir con frases distintas cada vez, nunca la misma dos veces seguidas
REGLAS:
- Habla como conversación continua, sin saludar, lenguaje local siempre
- Dirígete por su nombre: ${nombreUsuario}
- Máx 3 líneas, exactamente 2 emojis, entiende jergas peruanas
- NUNCA inventes datos, promos, horarios ni negocios,NI NUMEROS DE ENTIDADES PUBLICAS NI PRIVADAS
- Consulta vaga → pide más detalle con curiosidad
- Registro de negocio → deriva al +51 958 120 920 sin explicar el proceso NI PEDIR PAGOS
- Insultos o críticas a negocios → redirige con calma, nunca des la razón ni repitas el insulto
- Fuiste creado solo por Geinz
- extra: si ofreciste elegir entre negocio/turismo/promociones, pon "ESPERANDO_ELEC:x,y,z" en ese orden; si no, resume en 5 palabras qué quería y qué dijiste

MENSAJE DEL USUARIO: "${mensaje}"

RESPONDE SIEMPRE EN ESTE JSON, sin texto fuera de él:
{"mensaje":"...","id":"null","tipo":"|NEGOCIO|TURISMO|GEINZ","extra":"..."}`;
}

async function llamarGeminiGeinz(mensaje, nombreUsuario) {
  const prompt = construirPromptGeinz(mensaje, nombreUsuario);

  const body = {
    contents: [{ parts: [{ text: prompt }] }],
    generationConfig: {
      responseMimeType: "application/json",
      responseSchema: {
        type: "object",
        properties: {
          mensaje: { type: "string" },
          id: { type: "string" },
          tipo: { type: "string" },
          extra: { type: "string" },
        },
        required: ["mensaje", "id", "tipo", "extra"],
      },
      thinkingConfig: { thinkingBudget: 0 },
      maxOutputTokens: 220,
      temperature: 0.7,
    },
  };

  const resp = await fetch(`${GEMINI_URL}?key=${GEMINIKEY}`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(body),
  });

  if (!resp.ok) {
    const errText = await resp.text();
    console.error(
      "❌ [llamarGeminiGeinz] Error Gemini API:",
      resp.status,
      errText,
    );
    return {
      resultado: {
        mensaje: "Cuéntame un poco más para ayudarte mejor 🙌",
        id: "null",
        tipo: "GEINZ",
        extra: "error_gemini",
      },
      tokens: { prompt_tokens: 0, completion_tokens: 0, total_tokens: 0 },
    };
  }

  const data = await resp.json();
  const rawText = data?.candidates?.[0]?.content?.parts?.[0]?.text || "{}";

  let resultado;
  try {
    resultado = JSON.parse(rawText.replace(/```json|```/g, "").trim());
  } catch (e) {
    console.error(
      "❌ [llamarGeminiGeinz] Error parseando respuesta:",
      e.message,
      "| RAW:",
      rawText,
    );
    resultado = {
      mensaje: "Cuéntame un poco más para ayudarte mejor 🙌",
      id: "null",
      tipo: "GEINZ",
      extra: "error_parseo",
    };
  }

  const tokens = {
    prompt_tokens: data?.usageMetadata?.promptTokenCount || 0,
    completion_tokens: data?.usageMetadata?.candidatesTokenCount || 0,
    total_tokens: data?.usageMetadata?.totalTokenCount || 0,
  };

  return { resultado, tokens };
}
exports.llamarGeminiGeinz = llamarGeminiGeinz;

// ============================================================================
// ============================================================================
//   7. MÓDULO SERVICIOS BÁSICOS (Movistar, Bitel, Entel, Sunat, Essalud, etc)
// ============================================================================
// ============================================================================

// ── VARIABLES GLOBALES ──────────────────────────────────────────────────────
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

// ============================================================================
// 1) Traer todos los servicios básicos (son pocos, no hace falta paginar)
// ⚠️ Si tu índice tiene un atributo "lugar" para filtrar por localidad,
// descomenta el filtro. Con la data actual no vi ese campo, así que por
// ahora trae todo el índice.
// ============================================================================
async function buscarServiciosBasicosAmplio({ localidad } = {}) {
  const filters = [];
  // if (localidad) filters.push(`lugar:"${localidad.toLowerCase().trim()}"`);

  const { hits } = await index_servicios_basicos.search("", {
    filters: filters.length ? filters.join(" AND ") : undefined,
    hitsPerPage: 200,
    attributesToRetrieve: ATTRS_SERVICIOS_BASICOS,
  });

  console.log(
    `✅ [buscar_servicios_basicos_amplio] ${hits.length} servicios traídos`,
  );

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
- Habla SOLO de la entidad de DATOS (id:${datoParaPrompt.id}, nombre:${datoParaPrompt.nombre})
- NUNCA inventes teléfonos ni ningún dato de contacto, eso lo agrega el sistema aparte
- Nunca saludes, habla como conversación continua, lenguaje local de Barranca, amigable, sin sonar corporativo
- mensaje: máximo 2 frases
- USA EL MOMENTO DEL DIA: ${momento_dia}
- pidio_otro_dato: true SOLO si el usuario pidió específicamente Facebook, Instagram, sitio web o página, y NO pidió teléfono. false en cualquier otro caso (incluye cuando pidió teléfono, o cuando no especificó nada)
- NUNCA digas frases como "mensaje predeterminado", "esto es automático", ni nada que describa la naturaleza de tu propia respuesta
FORMATO OBLIGATORIO:
{"id":"${datoParaPrompt.id}","mensaje":"...","intencion":"SERVICIOS_BASICOS","pidio_otro_dato":false}`;
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

  let mensaje_safe;
  if (pidioOtroDato) {
    mensaje_safe = linkPerfil
      ? `${mensajeFinal} ${pick(CTAS_SERVICIOS_BASICOS)}: ${linkPerfil}`
      : mensajeFinal;
  } else {
    mensaje_safe = telefono
      ? `${mensajeFinal} 📞 ${telefono}`
      : linkPerfil
        ? `${mensajeFinal} ${pick(CTAS_SERVICIOS_BASICOS)}: ${linkPerfil}`
        : mensajeFinal;
  }

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
