const { onRequest } = require("firebase-functions/v2/https");
const admin = require("firebase-admin");
const OpenAI = require("openai");
const algoliasearch = require("algoliasearch");
const { FieldValue } = require("firebase-admin/firestore");
const similarity = require("string-similarity-js");
const paths = require("../rutas_geinz_firebase/rutas");
const { obtener_creditos_tienda_fn } = require("../test_db2");

const openai = new OpenAI({
  apiKey: process.env.API_KEYO_OPEN_IA,
});

const db = admin.firestore();

const APP_ID = process.env.ALGOLIA_APP_ID || "";
const API_KEY = process.env.ALGOLIA_API_KEY || "";
const client = algoliasearch(APP_ID, API_KEY);
const index = client.initIndex("lugares");

const GEMINIKEY = process.env.PRIVATEKEY_GEMINI;
const GEMINI_URL =
  "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent";

const CACHE_TTL_MS = 1000 * 60 * 30;

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

const {
  buscarServiciosBasicosAmplio,
  construirListaLigeraServicios,
  matchLocalServicioBasico,
} = require("./servicios_basicos");
let categoriasCache = null;
let categoriasCacheTimestamp = 0;


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

function generarToken() {
  return `${Date.now().toString(36)}${Math.random().toString(36).substring(2, 12)}`;
}

async function obtenerDatosPorIds(localidad, ids) {
  const ref =paths.tiendaCol(localidad,"tiendas");
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
    // ==========================================================================
    if (
      !resultadosBusqueda.length &&
      (trace.tipo_busqueda === "nombre" || trace.tipo_busqueda === "categoria")
    ) {
      try {
        const terminoBusqueda = nombre || mensaje;
        const hitsServicios = await buscarServiciosBasicosAmplio({ localidad });
        if (hitsServicios.length) {
          const listaLigeraServicios =
            construirListaLigeraServicios(hitsServicios);
          const idServicioMatch = matchLocalServicioBasico(
            terminoBusqueda,
            listaLigeraServicios,
          );
          if (idServicioMatch) {
            console.log(
              "↪️ [procesarBusquedaTienda] Match local con servicio básico, redirigiendo | id:",
              idServicioMatch,
            );
            trace.tipo_busqueda = "redirigido_servicios_basicos";
            return {
              id: "sin_id",
              redirigir_a_servicios: true,
              data: "null",
              subcategoria: null,
              debug_trace: trace,
            };
          }
        }
      } catch (e) {
        console.error(
          "❌ [procesarBusquedaTienda] Error en fallback a servicios básicos:",
          e.message,
        );
      }
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


exports.procesarBusquedaTienda = procesarBusquedaTienda;
exports.resolverInfoNegocio = resolverInfoNegocio;