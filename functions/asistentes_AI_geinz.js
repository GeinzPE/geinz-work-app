const { onRequest } = require("firebase-functions/v2/https");
const admin = require("firebase-admin");
const OpenAI = require("openai");

const openai = new OpenAI({
  apiKey: process.env.API_KEYO_OPEN_IA,
});

const { obtener_creditos_tienda_fn } = require("./test_db2");

const CACHE_TTL_MS = 1000 * 60 * 30; // 30 minutos

const APP_ID = process.env.ALGOLIA_APP_ID || "";
const API_KEY = process.env.ALGOLIA_API_KEY || "";
const algoliasearch = require("algoliasearch");
const { FieldValue } = require("firebase-admin/firestore");

const db = admin.firestore();
const client = algoliasearch(APP_ID, API_KEY);
const index_Algolia_promos = client.initIndex("promociones_filtrado_index");
const index = client.initIndex("lugares");
const GEMINIKEY = process.env.PRIVATEKEY_GEMINI;
const GEMINI_URL =
  "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent";
// ============================================================
// 🔵 CLASIFICADOR: NEGOCIOS / TIENDAS
// ============================================================

// ✅ Cache en memoria de categorías de negocios
let categoriasCache = null;
let categoriasCacheTimestamp = 0;

async function obtenerCategorias() {
  const ahora = Date.now();
  if (categoriasCache && ahora - categoriasCacheTimestamp < CACHE_TTL_MS) {
    console.log("♻️ Usando categorías (negocios) desde cache");
    return categoriasCache;
  }

  console.log("🔄 Refrescando categorías (negocios) desde Firestore");
  const snapshot = await admin
    .firestore()
    .collection("Tiendas")
    .doc("categorias")
    .collection("categorias")
    .select()
    .get();

  const categorias = [];
  snapshot.forEach((doc) => {
    const id = doc.id.toLowerCase();
    if (id !== "turismo") {
      categorias.push(doc.id);
    }
  });

  categoriasCache = categorias;
  categoriasCacheTimestamp = ahora;
  return categorias;
}

function construirPromptNegocios(mensaje, contextoPrevio, categorias) {
  const contextoStr = JSON.stringify(
    contextoPrevio || {
      tipo: "GEINZ",
      categoria: "null",
      extra: "null",
      id: null,
      nombre: "null",
    },
  );

  return `CONTEXTO PREVIO: ${contextoStr}
MENSAJE DEL USUARIO: "${mensaje}"

Responde SOLO con JSON válido. Sin explicaciones, sin markdown.
{"nombre":string|null,"categoria":string|null,"subcategoria":null,"tipo":"tiendas","search":boolean,"excluir_id":string|null,"pregunta":boolean,"registro":boolean}

CATEGORIAS: ${categorias.join(",")}

REGLAS (en orden de prioridad):
0. Si el usuario quiere registrar su negocio, tienda, empresa, local o emprendimiento → registro=true; en cualquier otro caso → registro=false.
1. EMERGENCIA: serenazgo, policia, bomberos, samu, hospital, posta, robo, auxilio, fuego, choque → categoria="emergencia"
2. NOMBRE PROPIO detectado en mensaje → nombre=texto limpio, categoria=null, ignorar contexto
3. NEGACION (no quiero/sin/excepto/evitar + nombre) → nombre=null, inferir categoria
4. SIN NOMBRE → nombre=null, inferir categoria de CATEGORIAS
5. Sin relacion con categorias → categoria="geinz"
6. search=true solo si pide oferta/descuento de nombre propio, sino false
7. excluir_id: null en cualquier caso que no sea regla 4
8. pregunta=true SOLO si el usuario pide más información sobre un lugar YA mencionado en el contexto (ej: "tiene estacionamiento?", "a qué hora cierra?"). Si es una búsqueda nueva, pregunta=false.
LIMPIEZA: minusculas, sin tildes, sin simbolos`;
}

exports.clasificador_geinz_categorias_negocios = onRequest(async (req, res) => {
  try {
    const { mensaje, contexto_previo } = req.body;

    if (!mensaje || typeof mensaje !== "string" || !mensaje.trim()) {
      return res.status(400).json({
        ok: false,
        error: "El campo 'mensaje' es requerido",
      });
    }

    const categorias = await obtenerCategorias();
    const prompt = construirPromptNegocios(
      mensaje,
      contexto_previo,
      categorias,
    );

    const completion = await openai.chat.completions.create({
      model: "gpt-5-nano",
      messages: [{ role: "user", content: prompt }],
      response_format: { type: "json_object" },
    });

    const raw = completion.choices[0]?.message?.content || "{}";

    let resultado;
    try {
      resultado = JSON.parse(raw);
    } catch (e) {
      console.error(
        "❌ Error parseando respuesta de OpenAI (negocios):",
        e.message,
        "| RAW:",
        raw,
      );
      resultado = {
        nombre: null,
        categoria: null,
        subcategoria: null,
        tipo: "tiendas",
        search: false,
        excluir_id: null,
        pregunta: false,
        registro: false,
      };
    }

    return res.status(200).json({
      ok: true,
      data: resultado,
    });
  } catch (error) {
    console.error(
      "❌ Error clasificador_geinz_categorias_negocios:",
      error.message,
    );
    return res.status(500).json({
      ok: false,
      error: error.message,
    });
  }
});

// ============================================================
// 🟢 CLASIFICADOR: TURISMO
// ============================================================

// ✅ Cache en memoria de subcategorías de turismo
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
  const snap = await admin
    .firestore()
    .doc("Tiendas/categorias/categorias/turismo")
    .get();

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
      return res.status(400).json({
        ok: false,
        error: "El campo 'mensaje' es requerido",
      });
    }

    const categorias = await obtenerSubcategoriasTurismo();
    const prompt = construirPromptTurismo(mensaje, contexto_previo, categorias);

    const completion = await openai.chat.completions.create({
      model: "gpt-5-nano",
      messages: [{ role: "user", content: prompt }],
      response_format: { type: "json_object" },
    });

    const raw = completion.choices[0]?.message?.content || "{}";

    let resultado;
    try {
      resultado = JSON.parse(raw);
    } catch (e) {
      console.error(
        "❌ Error parseando respuesta de OpenAI (turismo):",
        e.message,
        "| RAW:",
        raw,
      );
      resultado = {
        tipo: "turismo",
        nombre: null,
        categoria: null,
        excluir_id: null,
      };
    }

    return res.status(200).json({
      ok: true,
      data: resultado,
    });
  } catch (error) {
    console.error("❌ Error clasificador_geinz_turismo:", error.message);
    return res.status(500).json({
      ok: false,
      error: error.message,
    });
  }
});

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
  const db = admin.firestore();

  const ref = db.collection("Tiendas").doc(localidad).collection(localidad);

  const resultados = {};

  const size = 10;
  const chunks = [];

  for (let i = 0; i < ids.length; i += size) {
    chunks.push(ids.slice(i, i + size));
  }

  // 🚀 ejecutar TODO en paralelo
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

  if (!horario || horario.cerrado === true) {
    return false;
  }

  const bloques = horario.bloques || [];

  for (const bloque of bloques) {
    if (!bloque || !bloque.h_apertura || !bloque.h_cierre) continue;

    const [ha, ma] = bloque.h_apertura.split(":").map(Number);
    const [hc, mc] = bloque.h_cierre.split(":").map(Number);

    const apertura = ha * 60 + ma;
    const cierre = hc * 60 + mc;

    if (apertura <= cierre) {
      if (minutosActual >= apertura && minutosActual <= cierre) {
        return true;
      }
    } else {
      if (minutosActual >= apertura || minutosActual <= cierre) {
        return true;
      }
    }
  }

  return false;
}

exports.buscar_por_categoria_subcateogira_Actualizado = onRequest(
  async (req, res) => {
    try {
      const { localidad, categoria, mensaje, contexto_previo, excluir_id } =
        req.body;

      const categoriaLimpia = (categoria || "").trim().toLowerCase();

      // ============================================================
      // 1️⃣ Obtener subcategorías de la categoría (igual que obtener_subcategoira_de_cat)
      // ============================================================
      let listaSubcategorias = [];
      if (categoriaLimpia) {
        const snap = await admin
          .firestore()
          .doc(`Tiendas/categorias/categorias/${categoriaLimpia}`)
          .get();
        listaSubcategorias = snap.exists
          ? (snap.get("subcategorias") ?? [])
          : [];
      }

      // ============================================================
      // 2️⃣ Clasificar subcategoría con IA (igual que "calsificador de subcateorias para tiendas")
      // ============================================================
      const contextoStr = JSON.stringify(contexto_previo || {});

      const promptSubcategoria = `CONTEXTO DEL USUARIO:
${contextoStr}

LISTA DE SUBCATEGORÍAS:
${listaSubcategorias}

TAREA:
Selecciona UNA ÚNICA subcategoría de la LISTA que mejor corresponda a la intención del usuario.

REGLAS:
- Si el mensaje es de rechazo o continuación ("no quiero ese", "otro", "otro sitio", "muéstrame más", "no me gustó", "dame otro") → heredar EXACTAMENTE la misma sub del CONTEXTO, sin cambiar nada
- Usa el CONTEXTO para reforzar la elección si el mensaje actual es ambiguo
- Si el contexto tiene subcategoría activa y el mensaje es continuación → heredar esa subcategoría sino escoje uno de la LISTA y evita el contexto
- Responde ÚNICAMENTE 1 valor exacto de la LISTA, sin cambios ni mayúsculas extra
- No inventar valores fuera de la LISTA
- Si dudas → elegir la más cercana semánticamente
- Ignorar errores ortográficos, interpretar semánticamente
- Si el mensaje contiene nombre de negocio o marca → responde: NEGOCIO: [nombre normalizado]
- Respuesta: una sola línea, sin comas, sin listas

MENSAJE DEL USUARIO: "${mensaje}"`;

      const completion = await openai.chat.completions.create({
        model: "gpt-5-nano",
        messages: [{ role: "user", content: promptSubcategoria }],
      });

      const subcategoria = (
        completion.choices[0]?.message?.content || ""
      ).trim();

      // ============================================================
      // 3️⃣ Búsqueda en Algolia (idéntico a tu función original)
      // ============================================================
      const query = "";
      let filters = [];
      const momento_dia = obtenerMomentoDia();

      if (localidad) filters.push(`lugar:"${localidad.toLowerCase().trim()}"`);
      if (categoria)
        filters.push(`categoria:"${categoria.toLowerCase().trim()}"`);
      if (subcategoria)
        filters.push(`tag:"${subcategoria.toLowerCase().trim()}"`);

      if (categoria) {
        const refCat = db.collection("estadisticas").doc(categoria);
        refCat
          .set({ categoria }, { merge: true })
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
      if (excluir_id) {
        filters.push(`NOT objectID:"${excluir_id}"`);
      }

      const { hits } = await index.search(query, {
        filters: filters.join(" AND "),
        hitsPerPage: 20,
        typoTolerance: true,
        ignorePlurals: true,
        removeStopWords: true,
        attributesToRetrieve: [
          "objectID",
          "nombre",
          "descripcion",
          "lugar",
          "categoria",
          "imagen_bot",
          "plantilla",
          "msje_whatsapp",
          "alias",
        ],
      });

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

      const topFlag = conFlagValidos.slice(0, 3);
      const topNormal = sinFlag.slice(0, 2);

      const mezclados = [];
      const maxLen = Math.max(topFlag.length, topNormal.length);
      for (let i = 0; i < maxLen; i++) {
        if (i < topFlag.length) mezclados.push(topFlag[i]);
        if (i < topNormal.length) mezclados.push(topNormal[i]);
      }

      const dataFinal = mezclados.slice(0, 5);

      return res.status(200).json({
        ok: true,
        momento_dia,
        total: dataFinal.length,
        data: dataFinal,
      });
    } catch (error) {
      console.error("Error búsqueda algolia turismo:", error);
      return res.status(500).json({ ok: false, error: error.message });
    }
  },
);

// ============================================================
// 🔴 CLASIFICADOR + BÚSQUEDA: EMERGENCIAS (SALUD / SEGURIDAD)
// ============================================================
// ============================================================
// 🔴 CLASIFICADOR + BÚSQUEDA: EMERGENCIAS (SALUD / SEGURIDAD)
// ============================================================

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
      return res.status(400).json({
        ok: false,
        error: "El campo 'mensaje' es requerido",
      });
    }

    // ============================================================
    // 1️⃣ Clasificar SALUD o SEGURIDAD con IA
    // ============================================================
    const promptEmergencia = construirPromptEmergencia(mensaje);

    const completion = await openai.chat.completions.create({
      model: "gpt-5-nano",
      messages: [{ role: "user", content: promptEmergencia }],
    });

    const clasificacionRaw = (completion.choices[0]?.message?.content || "")
      .toUpperCase()
      .normalize("NFD")
      .replace(/[\u0300-\u036f]/g, ""); // quita tildes por si acaso

    console.log("🧠 RAW IA:", completion.choices[0]?.message?.content);

    const categoria = clasificacionRaw.includes("SALUD")
      ? "salud"
      : clasificacionRaw.includes("SEGURIDAD")
        ? "seguridad"
        : "general";

    console.log("🚨 CLASIFICACION EMERGENCIA:", categoria);

    // ============================================================
    // 2️⃣ Búsqueda en Algolia (mismo índice "lugares")
    // ============================================================
    let filtersArray = [];

    if (localidad) {
      filtersArray.push(`lugar:"${localidad}"`);
    }

    if (categoria && categoria !== "general") {
      filtersArray.push(`categoria:"${categoria}"`);
    }

    const filters =
      filtersArray.length > 0 ? filtersArray.join(" AND ") : undefined;

    const result = await index.search("", {
      filters,
      hitsPerPage: 20,
    });

    const data = result.hits.map((d) => {
      let ubicacion = null;

      if (
        d.ubicacion &&
        d.ubicacion.latitud != null &&
        d.ubicacion.longitud != null
      ) {
        ubicacion = {
          lat: d.ubicacion.latitud,
          lng: d.ubicacion.longitud,
        };
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

    return res.status(200).json({
      ok: true,
      categoria,
      total: data.length,
      data,
    });
  } catch (error) {
    console.error("ERROR obtener_lugares_emergencia:", error);

    return res.status(500).json({
      ok: false,
      mensaje: "Error interno al buscar lugares",
    });
  }
});

// --- Normaliza array de pagos a string estable (para agrupar bien) ---
function normalizarPagos(pagos) {
  if (Array.isArray(pagos)) return [...pagos].sort().join(",");
  return pagos || "";
}

// --- Normaliza y recorta los resultados antes de mandarlos al prompt ---
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

// --- Agrupa promos que comparten tienda + método de pago ---
function agruparPorTiendaYPago(lista) {
  const grupos = {};
  for (const r of lista) {
    const key = `${r.t}||${r.pagos}`;
    if (!grupos[key]) {
      grupos[key] = { t: r.t, pagos: r.pagos, items: [] };
    }
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

// --- Formato compacto AGRUPADO: tienda/pagos una sola vez, desc por promo ---
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
  return `Eres un informador peruano. Elige la mejor promo para el usuario.

FORMATO DE DATOS (agrupado por tienda y pagos para ahorrar tokens; cada línea debajo del header es una promo distinta de esa misma tienda):
TIENDA:nombre|PAGOS:métodos
  id~sc~desc~precio~como~p_ok~pr_ok

GLOSARIO:
sc=score relevancia(mayor=mejor) | desc=descripción | precio=rango en soles(vacío=no especificado) | como=comodidades | p_ok=true/false/vacío(match de pago pedido) | pr_ok=true/false/vacío(match de precio pedido) | vacío=usuario no filtró eso

CONTEXTO
Momento: ${momento}
Usuario: ${nombreUsuario}
Lo que pidió el usuario: "${mensajeUsuario || "(no especificó nada en texto, usa solo los datos pre-filtrados)"}"

PROMOS (ya pre-filtradas, trabaja solo con estas):
${compactar(resultados)}

ALTERNATIVAS:
${compactar(alt)}

REGLAS
- Usa SOLO estos datos, nunca inventes
- Si varias promos son de la misma tienda (mismo header TIENDA), compáralas por desc/precio/como y elige la(s) que mejor calce con lo que pidió el usuario
- Ten en cuenta lo que pidió el usuario en su mensaje para elegir mejor y ajustar el tono de la respuesta
- Si p_ok=false, avisa que no hay con ese pago y ofrece la alternativa con sus pagos reales
- Si todo viene vacío, el usuario no filtró nada: recomienda directo
- Prioriza mayor sc
- Tono peruano natural, sin saludar, como conversación continua
- Nunca menciones "score" ni datos internos
- Usa el momento del día de forma natural
- Máximo 2 líneas y 2 emojis
- Si no hay promos ni alternativas, dilo directo
- NUNCA SALUDES con hola ni que buena ni que tal o similares

DECISIÓN
- 2+ promos relevantes → varios=true, incluye sus ids
- 1 promo → varios=false, incluye su id
- 0 promos → varios=false, id="none"`;
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
        thinkingConfig: {
          thinkingBudget: 0, // 👈 apaga "thinking", el mayor ahorro
        },
        maxOutputTokens: 200, // 👈 tope de seguridad, es solo 2 líneas de mensaje
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
      return res.status(502).json({
        ok: false,
        error: "Error al consultar Gemini",
      });
    }

    const geminiData = await geminiRes.json();

    // =========================
    // 📊 LOG DETALLADO DE TOKENS
    // =========================
    const usage = geminiData?.usageMetadata || {};
    const promptTokens = usage.promptTokenCount ?? 0;
    const respuestaTokens = usage.candidatesTokenCount ?? 0;
    const pensamientoTokens = usage.thoughtsTokenCount ?? 0;
    const totalTokens = usage.totalTokenCount ?? 0;
    const tiempoMs = Date.now() - inicioTiempo;

    console.log(
      `📊 TOKENS | prompt: ${promptTokens} | respuesta: ${respuestaTokens} | thinking: ${pensamientoTokens} | TOTAL: ${totalTokens} | tiempo: ${tiempoMs}ms | usuario: ${nombreUsuario} | promos_enviadas: ${resultadosFiltrados.length} | alt_enviadas: ${altFiltrada.length}`,
    );

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
    return res.status(500).json({
      ok: false,
      error: error.message,
    });
  }
});