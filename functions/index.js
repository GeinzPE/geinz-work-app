require("dotenv").config();
const {
  onRequest,
  onCall,
  HttpsError,
} = require("firebase-functions/v2/https");
const {
  onDocumentCreated,
  onDocumentWritten,
} = require("firebase-functions/v2/firestore");

const logger = require("firebase-functions/logger");

const admin = require("firebase-admin");
const algoliasearch = require("algoliasearch");
const { onSchedule } = require("firebase-functions/v2/scheduler");

const speech = require("@google-cloud/speech");

const client_specth = new speech.SpeechClient();

const textToSpeech = require("@google-cloud/text-to-speech");
const ttsClient = new textToSpeech.TextToSpeechClient();
const geofire = require("geofire-common");
admin.initializeApp();
const {
  obtener_creditos_tienda,
  obtener_creditos_tienda_fn,
  descontar_creditos_tienda,
  eliminar_deuda_actual,
} = require("./test_db2");

const {
  generar_texto_ia,
  generar_texto_compartir_ia,
  generar_whatsapp_contacto_ia,
  generar_titulo_descripcion_IA,
  crearPromocion,
  extraerTerminosClaveIA,
  generar_descripcion_whatsapp_ia,
  pagar_plan__usuario,
} = require("./generacions_IA");
exports.obtener_creditos_tienda = obtener_creditos_tienda;
exports.descontar_creditos_tienda = descontar_creditos_tienda;
exports.generar_texto_ia = generar_texto_ia;
exports.generar_texto_compartir_ia = generar_texto_compartir_ia;
exports.generar_whatsapp_contacto_ia = generar_whatsapp_contacto_ia;
exports.generar_titulo_descripcion_IA = generar_titulo_descripcion_IA;
exports.crearPromocion = crearPromocion;
exports.extraerTerminosClaveIA = extraerTerminosClaveIA;
exports.generar_descripcion_whatsapp_ia = generar_descripcion_whatsapp_ia;
exports.pagar_plan__usuario = pagar_plan__usuario;
const axios = require("axios");

const CULQI_KEY = process.env.CULQI_KEY;
const PHONE_ID = process.env.ID_NUMBER_WHATSAPP;
const WHATSAPP_TOKEN = process.env.ID_API_WHATSAPP;
const PDFDocument = require("pdfkit");
const fs = require("fs");
const path = require("path");

const db = admin.firestore();
const FieldValue = admin.firestore.FieldValue; // 👈 solo aquí
const OpenAI = require("openai");
const { ref } = require("process");
const APP_ID = process.env.ALGOLIA_APP_ID || "";
const API_KEY = process.env.ALGOLIA_API_KEY || "";

const client = algoliasearch(APP_ID, API_KEY);
const index_Algolia_promos = client.initIndex("promociones_filtrado_index");
const index = client.initIndex("lugares");
const openai = new OpenAI({
  apiKey: process.env.API_KEYO_OPEN_IA,
});
const similarity = require("string-similarity-js");

// ==================== clasificador_IA ====================
exports.extraerDatos = onRequest(async (req, res) => {
  try {
    // 🔒 Solo POST
    if (req.method !== "POST") {
      return res.status(405).json({ error: "Método no permitido" });
    }

    const { texto } = req.body;

    if (!texto) {
      return res.status(400).json({ error: "Falta 'texto'" });
    }

    const prompt = `Extrae del texto y responde SOLO en JSON válido.
-"tipo":"app",
-"nombre":null,
- "productos":array (producto/servicio/lugar) → corregir ortografía, minúscula, 
sin tildes, sin personas, sin palabras vacías, no inventar, 
no duplicar ,sin diminutivo,forma canónica 
- "precio_max": número entero o null
- "metodos_pago": solo de ["yape","plin","efectivo","agora","visa","mastercard"], no duplicar  
- "comodidades": array solo de ["aire_acondicionado","camaras_de_seguridad","enchufe","estacionamiento",
"ingreso_mascotas","mesa_para_ninos","sala_de_espera","sala_juegos",
"servicios_higienicos","wifi","zona_expandida"], 
detectar implícito, no inventar, no duplicar  

Texto: "${texto}"`;
    // 🚀 OpenAI
    const response = await openai.chat.completions.create({
      model: "gpt-5-nano",
      messages: [
        {
          role: "system",
          content:
            "Eres un extractor de datos. Respondes SOLO con JSON válido, sin texto adicional.",
        },
        { role: "user", content: prompt },
      ],
    });

    const content = response.choices?.[0]?.message?.content;

    if (!content) {
      return res.status(500).json({
        error: "Respuesta vacía del modelo",
      });
    }

    // 🛡️ Parseo seguro
    let resultado;

    try {
      resultado = JSON.parse(content);
    } catch (err) {
      logger.error("Error parseando JSON:", content);

      return res.status(500).json({
        error: "Respuesta inválida del modelo",
        raw: content,
      });
    }

    return res.status(200).json(resultado);
  } catch (error) {
    logger.error("Error general:", error);

    return res.status(500).json({
      error: "Error interno",
      detail: error.message,
    });
  }
});

exports.filtrar_por_datos = onRequest(async (req, res) => {
  try {
    console.log("📥 REQUEST BODY:", JSON.stringify(req.body, null, 2));

    const resultado = req.body.resultado || req.body;

    const tipo = resultado?.tipo || "app";
    const nombreTienda = resultado?.nombre?.toLowerCase().trim() || "";

    let filters = [];

    // 🕐 Hora Perú
    const ahora = new Date();

    const ahoraPeru = new Date(
      ahora.toLocaleString("en-US", { timeZone: "America/Lima" }),
    );

    const horaPeru = ahoraPeru.getHours();

    console.log("🕒 Hora Perú (Date):", ahoraPeru.toString());
    console.log("🧮 Date.now():", Date.now());

    let horarioActual = "";
    if (horaPeru >= 6 && horaPeru < 12) horarioActual = "manana";
    else if (horaPeru < 18) horarioActual = "tarde";
    else horarioActual = "noche";

    console.log("⏰ Horario:", horarioActual);

    // 🔹 PRECIO
    const rawPrecio = resultado?.precio ?? resultado?.precio_max;
    const precioInput =
      rawPrecio != null && rawPrecio !== "" ? Number(rawPrecio) : null;

    if (Number.isFinite(precioInput)) {
      filters.push(`precioMin <= ${precioInput}`);
      filters.push(`precioMax >= ${precioInput}`);
    }

    // 🔹 HORARIO
    filters.push(
      `(horario_publicacion:${horarioActual} OR horario_publicacion:todo_dia)`,
    );

    // 🔹 EXPIRACIÓN
    const timestampFiltro = Date.now();
    console.log("🧪 Filtro timestamp_fin >", timestampFiltro);

    filters.push(`timestamp_fin > ${timestampFiltro}`);

    const productosQuery = (resultado?.productos || [])
      .map((p) => p.toLowerCase().trim())
      .filter((p) => p.length > 0);

    const query = nombreTienda || productosQuery.join(" ");

    const finalFilters = filters.join(" AND ");

    console.log("🧩 Filtros:", finalFilters);
    console.log("🔎 Query:", query);

    // 🔍 BÚSQUEDA
    const response = await index_Algolia_promos.search(query, {
      filters: finalFilters,
      hitsPerPage: 20,
      getRankingInfo: true,
      optionalWords: query,
      removeWordsIfNoResults: "allOptional",
    });

    console.log("📦 Hits:", response.hits.length);

    // 🔥 LOG CLAVE: timestamps de resultados
    response.hits.forEach((h, i) => {
      console.log(`📊 Hit ${i}:`, {
        id: h.objectID,
        timestamp_fin: h.timestamp_fin,
        vigente: h.timestamp_fin > timestampFiltro,
      });
    });

    const pagosQuery = resultado?.metodos_pago || [];
    const comodidadesQuery = resultado?.comodidades || [];

    let resultados = response.hits.map((h) => {
      let score = 0;

      const textScore = h._rankingInfo?.nbTypos === 0 ? 40 : 20;

      const terminosDB = (h.terminos_clave || []).map((t) =>
        t.toLowerCase().trim(),
      );

      const matchCount = productosQuery.filter((p) =>
        terminosDB.some((t) => t.includes(p) || p.includes(t)),
      ).length;

      let matchScore = 0;
      if (productosQuery.length > 0) {
        const matchRatio = matchCount / productosQuery.length;
        matchScore = Math.round(matchRatio * 20);
      }

      let precioScore = 0;
      if (precioInput != null) {
        const dentroRango =
          precioInput >= h.precioMin && precioInput <= h.precioMax;

        if (dentroRango) {
          precioScore = 20;
        } else {
          const diff = Math.min(
            Math.abs(precioInput - h.precioMin),
            Math.abs(precioInput - h.precioMax),
          );
          precioScore = Math.max(0, 20 - diff * 1.5);
        }
      }

      let pagoScore = 0;
      if (pagosQuery.length > 0) {
        const pagosDB = (h.pagos || []).map((p) => p.toLowerCase());
        const pagoMatch = pagosQuery.filter((p) =>
          pagosDB.includes(p.toLowerCase()),
        ).length;
        pagoScore = Math.round((pagoMatch / pagosQuery.length) * 10);
      }

      let comodidadScore = 0;
      if (comodidadesQuery.length > 0) {
        const comodDB = (h.comodidades || []).map((c) => c.toLowerCase());
        const comodMatch = comodidadesQuery.filter((c) =>
          comodDB.includes(c.toLowerCase()),
        ).length;
        comodidadScore = Math.round(
          (comodMatch / comodidadesQuery.length) * 10,
        );
      }

      score = textScore + matchScore + precioScore + pagoScore + comodidadScore;

      if (tipo === "app") {
        return {
          id: h.objectID,
          score: Math.min(100, Math.round(score)),
          matchCount,
          precio: h.precio,
        };
      }

      return {
        id: h.objectID,
        score: Math.min(100, Math.round(score)),
        descripcion: h.descripcion || "",
        name_tienda: h.nombre_tienda || "",
        img: h.imagen_promo || "",
      };
    });

    if (tipo !== "bot" && productosQuery.length > 0) {
      resultados = resultados.filter((r) => r.matchCount > 0);
    }

    resultados.sort((a, b) => b.score - a.score);

    if (tipo === "bot") {
      resultados = resultados.slice(0, 3);
    }
    const data = {
      tipo,
      total: resultados.length,
      resultados,
    };

    if (tipo === "bot") {
      data.momento_dia = horarioActual;
    }
    console.log("🏆 TOP RESULTADOS:", resultados);
    return res.status(200).json(data);
  } catch (error) {
    console.error("❌ ERROR:", error);
    return res.status(500).json({ error: error.message });
  }
});

// ==================== BUSQUEDA_ALGOLIA_BOT_GEINZ ====================

exports.buscarNegocios_para_solucionar = onRequest(
  { cors: true },
  async (req, res) => {
    if (req.method !== "POST") {
      return res.status(405).json({ error: "Método no permitido" });
    }

    const { termino } = req.body;

    if (!termino || !termino.trim()) {
      return res.status(400).json({ error: "Término requerido" });
    }

    try {
      const { hits } = await index.search(termino.trim(), {
        restrictSearchableAttributes: ["nombre"],
        typoTolerance: true,
        minWordSizefor1Typo: 4,
        minWordSizefor2Typos: 8,
        hitsPerPage: 10,
        attributesToRetrieve: ["nombre", "img", "id_tienda"],
        attributesToHighlight: [],
      });

      const resultados = hits.map((hit) => ({
        nombre: hit.nombre || "",
        logo: hit.img || "",
        id_tienda: hit.id_tienda || hit.objectID || "",
      }));

      return res.status(200).json({ ok: true, resultados });
    } catch (err) {
      logger.error("buscarNegocios error:", err);
      return res.status(500).json({ error: "Error al buscar" });
    }
  },
);

exports.buscar_por_nombre__tienda = onRequest(async (req, res) => {
  try {
    const { localidad, nombre, search } = req.body;

    // =========================================================
    // 🔥 FILTROS
    // =========================================================

    const filters = [];

    if (localidad) {
      filters.push(`lugar:"${localidad}"`);
    }

    // =========================================================
    // 🔥 QUERY
    // =========================================================

    const query = (nombre || "").toLowerCase().trim();

    if (!query) {
      return res.status(200).json({
        ok: true,
        total: 0,
        data: [],
      });
    }

    // =========================================================
    // 🔥 BÚSQUEDA NORMAL ALGOLIA
    // =========================================================

    let { hits } = await index.search(query, {
      filters: filters.join(" AND "),

      hitsPerPage: 10,

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
        "parecidas",
        "tag",
        "plantilla",
        "msje_whatsapp",
      ],
    });

    // =========================================================
    // 🔥 RESULTADOS NORMALES
    // =========================================================

    if (hits.length > 0) {
      hits = hits.map((h) => ({
        ...h,
        similarity: 1,
        match_keyword: query,
      }));
    }

    // =========================================================
    // 🔥 FALLBACK INTELIGENTE
    // =========================================================

    if (hits.length === 0) {
      const { hits: hitsFallback } = await index.search("", {
        filters: filters.join(" AND "),

        hitsPerPage: 150,

        attributesToRetrieve: [
          "objectID",
          "nombre",
          "descripcion",
          "lugar",
          "categoria",
          "imagen_bot",
          "parecidas",
          "tag",
        ],
      });

      hits = hitsFallback
        .map((h) => {
          let bestScore = 0;
          let bestKeyword = null;

          // =====================================================
          // 🔥 NOMBRE
          // =====================================================

          if (typeof h.nombre === "string") {
            const value = h.nombre.toLowerCase();

            let score = similarity.stringSimilarity(query, value);

            if (value.includes(query)) {
              score += 0.2;
            }

            if (score > bestScore) {
              bestScore = score;
              bestKeyword = h.nombre;
            }
          }

          // =====================================================
          // 🔥 PARECIDAS
          // =====================================================

          if (Array.isArray(h.parecidas)) {
            for (const p of h.parecidas) {
              if (typeof p !== "string") continue;

              const value = p.toLowerCase();

              let score = similarity.stringSimilarity(query, value);

              if (value.includes(query)) {
                score += 0.2;
              }

              if (score > bestScore) {
                bestScore = score;
                bestKeyword = p;
              }
            }
          }

          // =====================================================
          // 🔥 TAGS
          // =====================================================

          if (Array.isArray(h.tag)) {
            for (const t of h.tag) {
              if (typeof t !== "string") continue;

              const value = t.toLowerCase();

              let score = similarity.stringSimilarity(query, value);

              if (value.includes(query)) {
                score += 0.15;
              }

              if (score > bestScore) {
                bestScore = score;
                bestKeyword = t;
              }
            }
          }

          // =====================================================
          // 🔥 CATEGORÍA
          // =====================================================

          if (typeof h.categoria === "string") {
            const value = h.categoria.toLowerCase();

            let score = similarity.stringSimilarity(query, value);

            if (value.includes(query)) {
              score += 0.1;
            }

            if (score > bestScore) {
              bestScore = score;
              bestKeyword = h.categoria;
            }
          }

          // =====================================================
          // 🔥 FILTRO FINAL
          // =====================================================

          if (bestScore >= 0.35) {
            return {
              ...h,
              similarity: bestScore,
              match_keyword: bestKeyword,
            };
          }

          return null;
        })

        .filter(Boolean)

        // 🔥 MEJORES RESULTADOS PRIMERO
        .sort((a, b) => b.similarity - a.similarity)

        // 🔥 LIMITAR
        .slice(0, 10);
    }

    // =========================================================
    // 🔥 IDS
    // =========================================================

    const ids = hits.map((h) => h.objectID);

    // =========================================================
    // 🔥 IDS CON Y SIN FLAG
    // =========================================================

    const idsConFlag = hits
      .filter((h) => h.plantilla === true)
      .map((h) => h.objectID);

    const idsSinFlag = hits
      .filter((h) => h.plantilla !== true)
      .map((h) => h.objectID);

    // =========================================================
    // 🔥 EXTRA DATA + CRÉDITOS EN PARALELO
    // =========================================================

    const [extraData, creditosResults] = await Promise.all([
      obtenerDatosPorIds(localidad, ids),
      idsConFlag.length > 0
        ? Promise.all(
            idsConFlag.map((id) =>
              obtener_creditos_tienda_fn(id)
                .then((r) => ({ id, mayor_a_100: r?.mayor_a_100 === true }))
                .catch(() => ({ id, mayor_a_100: false })),
            ),
          )
        : Promise.resolve([]),
    ]);

    const creditosMap = Object.fromEntries(
      creditosResults.map(({ id, mayor_a_100 }) => [id, mayor_a_100]),
    );

    // =========================================================
    // 🔥 RESPUESTA FINAL
    // =========================================================

    const data = hits.map((hit) => {
      const extra = extraData[hit.objectID] || {};

      // ✅ pla true solo si tiene plantilla Y más de 100 créditos
      const tienePlan =
        hit.plantilla === true && creditosMap[hit.objectID] === true;

      // ✅ era plantilla pero sin créditos suficientes
      const eraPlantillaSinCreditos =
        hit.plantilla === true && creditosMap[hit.objectID] !== true;

      const base = {
        id: hit.objectID,

        tienda: hit.nombre || "",

        open_state: verificar_apertura_tienda(extra.horario),

        match_keyword: hit.match_keyword || null,

        similarity: Number((hit.similarity || 0).toFixed(2)),
      };

      // =====================================================
      // 🔥 RESPUESTA SIMPLE
      // =====================================================

      if (search === true) {
        return base;
      }

      // =====================================================
      // 🔥 RESPUESTA COMPLETA
      // =====================================================

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

        tipo: "tienda",
      };
    });

    // =========================================================
    // 🔥 RESPONSE
    // =========================================================

    return res.status(200).json({
      ok: true,

      total: data.length,

      data,
    });
  } catch (error) {
    console.error("❌ Error búsqueda tienda:", error);

    return res.status(500).json({
      ok: false,
      error: error.message,
    });
  }
});

exports.buscar_por_categoria_subcateogira = onRequest(async (req, res) => {
  try {
    const { localidad, categoria, subcategoria } = req.body;

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

    const { hits } = await index.search(query, {
      filters: filters.join(" AND "),
      hitsPerPage: 20,
      typoTolerance: true,
      ignorePlurals: true,
      removeStopWords: true,
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
                .then((r) => ({ id, mayor_a_100: r?.mayor_a_100 === true }))
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

        // ✅ pla true solo si tiene plantilla Y más de 100 créditos
        const tienePlan =
          hit.plantilla === true && creditosMap[hit.objectID] === true;

        // ✅ era plantilla pero sin créditos suficientes
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
          tipo: "tienda",
        };
      });

    const idsConFlagSet = new Set(idsConFlag);
    const idsSinFlagSet = new Set(idsSinFlag);

    // ✅ solo los que tienen plantilla Y créditos > 100
    const conFlagValidos = data.filter(
      (d) => idsConFlagSet.has(d.id) && creditosMap[d.id] === true,
    );

    // ✅ sin flag + los que tienen plantilla pero sin saldo → grupo normal
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
});
exports.agregar_error_firebase_bot = onRequest(async (req, res) => {
  try {
    // =========================
    // VALIDAR MÉTODO
    // =========================
    if (req.method !== "POST") {
      return res.status(405).json({
        ok: false,
        message: "Método no permitido",
      });
    }

    // =========================
    // BODY
    // =========================
    const {
      usuario = "",
      numero = "",
      busqueda = "",
      rama = "",
      tipo = "",
      nombre_detectado = null,
      categoria = null,
      subcategoria = null,
    } = req.body;

    // =========================
    // VALIDACIÓN
    // =========================
    if (!usuario.trim()) {
      return res.status(400).json({
        ok: false,
        message: "Usuario requerido",
      });
    }

    // =========================
    // REF FIREBASE
    // =========================
    const ref = db.collection("error_bot").doc();

    // =========================
    // PAYLOAD
    // =========================
    const payload = {
      id: ref.id,

      usuario,
      numero,
      busqueda,
      rama,
      estado: false,
      data: {
        tipo,
        nombre_detectado,
        categoria,
        subcategoria,
      },

      createdAt: admin.firestore.FieldValue.serverTimestamp(),
    };

    // =========================
    // GUARDAR
    // =========================
    await ref.set(payload);

    // =========================
    // RESPUESTA
    // =========================
    return res.status(201).json({
      ok: true,
      message: "Error guardado correctamente",
      id: ref.id,
    });
  } catch (error) {
    console.error("Error Firebase:", error);

    return res.status(500).json({
      ok: false,
      message: "Error interno",
      error: error.message,
    });
  }
});

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

exports.obtener_lugares_emergencia = onRequest(async (req, res) => {
  try {
    const { localidad, categoria } = req.body;

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

exports.obtener_cat_sub_promos_algolia = onRequest(async (req, res) => {
  try {
    const index = index_Algolia_promos;

    // 1. Obtener categorías
    const categoriasRes = await index.search("", {
      facets: ["categoria"],
      hitsPerPage: 0,
    });

    const categorias = Object.keys(categoriasRes.facets?.categoria || {});

    // Si no hay categorías
    if (!categorias.length) {
      return res.json({});
    }

    // 2. Hacer queries en paralelo 🚀
    const promesas = categorias.map((cat) => {
      // Escapar comillas por seguridad
      const catSafe = cat.replace(/"/g, '\\"');

      return index
        .search("", {
          filters: `categoria:"${catSafe}"`,
          facets: ["terminos_clave"],
          hitsPerPage: 0,
        })
        .then((resCat) => ({
          categoria: cat,
          tags: Object.keys(resCat.facets?.terminos_clave || {}),
        }));
    });

    const resultados = await Promise.all(promesas);

    // 3. Construir objeto final limpio
    const resultadoFinal = {};

    resultados.forEach(({ categoria, tags }) => {
      resultadoFinal[categoria] = tags
        .map((t) => t.trim().toLowerCase())
        .filter((t) => t.length > 0);
    });

    res.json(resultadoFinal);
  } catch (error) {
    console.error(error);
    res.status(500).json({ error: "Error interno" });
  }
});

exports.obtener_filtrado_manual_alogolia = onRequest(async (req, res) => {
  try {
    const index = index_Algolia_promos;

    const {
      categoria,
      subcategorias,
      rango_precio,
      pagos,
      comodidades,
      localidad,
    } = req.body;

    let filters = [];

    // 🔥 categoría
    if (categoria) {
      filters.push(`categoria:"${categoria}"`);
    }

    // 🔥 subcategorías
    if (subcategorias?.length > 0) {
      const subFilter = subcategorias
        .map((s) => `terminos_clave:"${s}"`)
        .join(" OR ");
      filters.push(`(${subFilter})`);
    }

    // 🔥 rango precio
    if (rango_precio) {
      if (rango_precio === "0 - 10")
        filters.push("precio >= 0 AND precio <= 10");
      if (rango_precio === "10 - 20")
        filters.push("precio >= 10 AND precio <= 20");
      if (rango_precio === "20 - 30")
        filters.push("precio >= 20 AND precio <= 30");
      if (rango_precio === "30 - 50")
        filters.push("precio >= 30 AND precio <= 50");
      if (rango_precio === "50 - 80")
        filters.push("precio >= 50 AND precio <= 80");
      if (rango_precio === "Mayor a 5000") filters.push("precio > 5000");
    }

    // 🔥 pagos
    if (pagos?.length > 0) {
      const pagosFilter = pagos.map((p) => `pagos:"${p}"`).join(" OR ");
      filters.push(`(${pagosFilter})`);
    }

    // 🔥 comodidades
    if (comodidades?.length > 0) {
      const comodFilter = comodidades
        .map((c) => `comodidades:"${c}"`)
        .join(" OR ");
      filters.push(`(${comodFilter})`);
    }

    // 🔥 localidad
    if (localidad) {
      filters.push(`localidad:"${localidad}"`);
    }

    const finalFilters = filters.join(" AND ");

    console.log("FILTERS:", finalFilters);

    const response = await index.search("", {
      filters: finalFilters,
      hitsPerPage: 50,
    });

    // 🔥 NORMALIZACIÓN (IMPORTANTE)
    const resultados = response.hits.map((h) => ({
      id: h.objectID || "",
      score: 0, // puedes calcular si quieres
      precio: h.precio ?? 0,
      precioMin: h.precioMin ?? 0,
      precioMax: h.precioMax ?? 0,
      rango: h.rango ?? "",
    }));

    return res.status(200).json({
      ok: true,
      total: resultados.length,
      resultados,
    });
  } catch (error) {
    console.error(error);
    return res.status(500).json({ error: error.message });
  }
});

exports.agregar_historial_usuario = onRequest(async (req, res) => {
  try {
    const { numeroUser, contexto } = req.body;

    // 🔍 Validación
    if (!numeroUser) {
      return res.status(400).json({ ok: false, error: "Falta numeroUser" });
    }

    // 🔥 Parse seguro del contexto
    let contextoObj = null;

    if (contexto) {
      try {
        contextoObj =
          typeof contexto === "string" ? JSON.parse(contexto) : contexto;
      } catch (e) {
        console.error("❌ Error parseando contexto:", e);
      }
    }

    // 📍 Referencia
    const ref = admin
      .firestore()
      .doc(
        `Trabajadores_Usuarios_Drivers/usuario_bot_geinz/usuario_bot_geinz/${numeroUser}`,
      );

    // 💾 Guardado con MERGE (NO sobreescribe todo)
    await ref.set(
      {
        contexto: contextoObj,
        updated_at: admin.firestore.FieldValue.serverTimestamp(),
      },
      { merge: true }, // 🔥 CLAVE
    );

    return res.status(200).json({
      ok: true,
      mensaje: "Historial guardado correctamente",
    });
  } catch (error) {
    console.error("🔥 ERROR GENERAL:", error);
    return res.status(500).json({
      ok: false,
      error: "Error interno",
    });
  }
});

// ==================== culqui ====================
// ─── BACKEND: crearOrdenCulqi ───
exports.crearOrdenCulqi = onCall(async (req) => {
  const { monto, userId, nombre, email, orderId } = req.data;

  // ─── LOG AQUÍ ───
  console.log("📥 Datos recibidos:");
  console.log("  monto:", monto);
  console.log("  tipo monto:", typeof monto);
  console.log("  amount calculado:", Math.round(monto * 100));
  console.log("  userId:", userId);
  // ────────────────

  const orderNumber = `ORD-${userId.slice(0, 8)}-${Date.now().toString().slice(-8)}`;

  const response = await axios.post(
    "https://api.culqi.com/v2/orders",
    {
      amount: Math.round(monto * 100),
      currency_code: "PEN",
      description: `Monedas Geinz - ${nombre}`,
      order_number: orderNumber,
      client_details: {
        first_name: nombre || "Cliente",
        last_name: "Geinz",
        email: email || "cliente@geinz.com",
        phone_number: "999999999",
      },
      expiration_date: Math.floor(Date.now() / 1000) + 900,
      confirm: false,
    },
    {
      headers: {
        Authorization: `Bearer ${CULQI_KEY}`,
        "Content-Type": "application/json",
      },
    },
  );

  // ─── LOG AQUÍ ───
  console.log("✅ Respuesta Culqi:");
  console.log("  amount:", response.data.amount);
  console.log("  state:", response.data.state);
  console.log("  order_id:", response.data.id);
  // ────────────────

  const culqi_order_id = response.data.id;

  if (orderId) {
    await db
      .collection("Tiendas")
      .doc("barranca")
      .collection("pagos_tiendas")
      .doc(orderId)
      .set(
        { order_number_culqi: orderNumber, culqi_order_id },
        { merge: true },
      );
  }

  return { culqi_order_id };
});
exports.culqiWebhook = onRequest(async (req, res) => {
  try {
    const event = req.body;
    console.log("Webhook Culqi:", JSON.stringify(event));

    if (event.type !== "order.status.changed") return res.sendStatus(200);
    if (event.data?.object?.state !== "paid") return res.sendStatus(200);

    const order = event.data.object;
    const orderNumber = order.order_number; // "ORD-{8chars}-{8nums}"

    console.log("Order number recibido:", orderNumber);

    // Buscar en pagos_tiendas por order_number
    const pagosRef = db
      .collection("Tiendas")
      .doc("barranca")
      .collection("pagos_tiendas");

    const query = await pagosRef
      .where("order_number_culqi", "==", orderNumber)
      .limit(1)
      .get();

    if (query.empty) {
      console.log("❌ No se encontró orden:", orderNumber);
      return res.sendStatus(200);
    }

    const pagoDoc = query.docs[0];
    const datos = pagoDoc.data();
    const userId = datos.id_tienda;

    if (datos.estado === "pagado") {
      console.log("⚠️ Ya estaba pagado");
      return res.sendStatus(200);
    }

    await sumarSaldo(userId, datos.monedas_a_recargar || datos.monedas);

    await agregar_historial_de_pagos_tienda({
      id_transaccion: pagoDoc.id,
      tipo_transaccion: "recarga",
      metodo_pago: "billetera_movil",
      nombre_tienda: datos.nombre_user,
      id_tienda: userId,
      localidad_tienda: datos.localdiad,
      tipo_paquete: datos.plan_select,
      monto_aumentado: datos.monedas_a_recargar || datos.monedas,
      precio_soles: (order.amount / 100).toString(),
      estado: "Aceptado",
      monto_anterior: datos.saldo_tienda || 0,
    });

    await enviarWhatsApp(
      "937659216",
      `✅ *Pago Billetera exitoso*\n🏪 ${datos.nombre_user}\n💰 S/ ${order.amount / 100}\n🪙 ${datos.monedas_a_recargar || datos.monedas} monedas`,
    );

    return res.sendStatus(200);
  } catch (err) {
    console.error("Webhook error:", err);
    return res.sendStatus(500);
  }
});

async function enviarPDFWhatsApp(numero, pdfUrl) {
  try {
    const telefono = `51${numero}`;

    console.log("📄 Enviando PDF a WhatsApp:", {
      telefono,
      pdfUrl,
    });

    const res = await axios.post(
      `https://graph.facebook.com/v19.0/${PHONE_ID}/messages`,
      {
        messaging_product: "whatsapp",
        to: telefono,
        type: "document",
        document: {
          link: pdfUrl,
          filename: "boleta_geinz.pdf",
        },
      },
      {
        headers: {
          Authorization: `Bearer ${WHATSAPP_TOKEN}`,
          "Content-Type": "application/json",
        },
      },
    );

    console.log("✅ PDF enviado correctamente:", res.data);

    return true;
  } catch (error) {
    console.error(
      "❌ ERROR ENVIANDO PDF WHATSAPP:",
      error.response?.data || error.message,
    );

    return false;
  }
}

async function emitirBoletaNubefact({
  userId,
  monedas,
  chargeId,
  monto,
  email,
  nombre,
}) {
  // 1. Definir y calcular variables necesarias (Evita el error 'not defined')
  const montoNum = Number(monto);
  const tasaIgv = 0.18;
  const valorUnitario = montoNum / (1 + tasaIgv); // Monto sin IGV
  const igvTotal = montoNum - valorUnitario;

  const response = await axios.post(
    "https://api.nubefact.com/api/v1/02bb7d82-0b0c-4006-82a5-74b7437bea0b",
    {
      operacion: "generar_comprobante", //
      tipo_de_comprobante: 2, // 2 = BOLETA
      serie: "BBB1", //
      numero: 0, // NubeFacT asigna el siguiente correlativo[cite: 1]
      sunat_transaction: 1, // Venta interna[cite: 1]

      // Para boletas menores a S/ 700.00 se usa "-" y "0"[cite: 1]
      cliente_tipo_de_documento: "-", //[cite: 1]
      cliente_numero_de_documento: "0", //[cite: 1]
      cliente_denominacion: nombre || "Consumidor final", //[cite: 1]
      cliente_direccion: "", // OBLIGATORIO aunque sea String vacío[cite: 1]
      cliente_email: email || "",

      fecha_de_emision: new Date().toLocaleDateString("en-CA", {
        timeZone: "America/Lima",
      }), // Formato DD-MM-YYYY[cite: 1]
      moneda: 1, // 1 = SOLES[cite: 1]
      porcentaje_de_igv: 18.0, //[cite: 1]

      // Totales del comprobante (Numeric con 2 decimales)[cite: 1]
      total_gravada: valorUnitario.toFixed(2), //[cite: 1]
      total_igv: igvTotal.toFixed(2), //[cite: 1]
      total: montoNum.toFixed(2), //[cite: 1]

      enviar_automaticamente_a_la_sunat: true, //[cite: 1]
      enviar_automaticamente_al_cliente: !!email, //[cite: 1]
      codigo_unico: chargeId, // Para evitar duplicidad[cite: 1]

      items: [
        {
          unidad_de_medida: "ZZ", // ZZ = SERVICIO[cite: 1]
          codigo: "MON001",
          descripcion: `Paquete de ${monedas} monedas Geinz`, //[cite: 1]
          cantidad: 1, //[cite: 1]
          valor_unitario: valorUnitario.toFixed(2), // Precio sin IGV[cite: 1]
          precio_unitario: montoNum.toFixed(2), // Precio con IGV[cite: 1]
          subtotal: valorUnitario.toFixed(2), //[cite: 1]
          tipo_de_igv: 1, // 1 = Gravado - Operación Onerosa[cite: 1]
          igv: igvTotal.toFixed(2), //[cite: 1]
          total: montoNum.toFixed(2), //[cite: 1]
          anticipo_regularizacion: false, //[cite: 1]
        },
      ],
    },
    {
      headers: {
        // Formato correcto de autenticación[cite: 1]
        Authorization: `Token token="8eee1a640fd7485cbc1da29427f59792b196deb29b954a6eb131bdb8562492fa"`,
        "Content-Type": "application/json",
      },
    },
  );

  return response.data.enlace_del_pdf; // Retorna el enlace generado[cite: 1]
}

/**
 * Emite un comprobante electrónico (Boleta o Factura) vía NubeFacT.
 *
 * @param {Object} params - Datos del comprobante.
 * @param {number} params.tipoComprobante - 1 para FACTURA, 2 para BOLETA.
 * @param {string} params.documento - RUC (11 dígitos) para factura o DNI/S.N. para boleta.
 * @param {string} params.nombre - Razón Social o Nombre del cliente.
 * @param {string} params.direccion - Dirección (obligatorio para Factura, opcional para Boleta).
 * @param {number} params.monto - Monto total de la venta (incluido IGV).
 * @param {string} params.email - Correo del cliente para envío automático.
 * @param {string} params.chargeId - ID único de transacción para evitar duplicados.[cite: 1]
 */
async function emitirComprobanteGeinz({
  tipoComprobante,
  documento,
  nombre,
  direccion,
  monto,
  email,
  chargeId,
  monedas,
}) {
  // LOGS DE ENTRADA: Para verificar qué argumentos recibe la función
  console.log("=== INICIANDO EMISIÓN NUBEFACT ===");
  console.log("Argumentos recibidos:", {
    tipoComprobante, // 1 para Factura, 2 para Boleta[cite: 1]
    documento,
    nombre,
    direccion,
    monto,
    email,
    chargeId,
    monedas,
  });

  const fechaPeru = new Date().toLocaleDateString("en-CA", {
    timeZone: "America/Lima",
  });
  try {
    const montoNum = Number(monto);
    const valorUnitario = montoNum / 1.18;
    const igvTotal = montoNum - valorUnitario;

    const esFactura = tipoComprobante === 1;

    // Mapeo dinámico del tipo de documento del cliente
    let tipoDocCliente = "-";
    if (esFactura) {
      tipoDocCliente = 6; // RUC[cite: 1]
    } else if (documento && documento.length === 8) {
      tipoDocCliente = 1; // DNI[cite: 1]
    }

    const payload = {
      operacion: "generar_comprobante",
      tipo_de_comprobante: tipoComprobante,
      serie: esFactura ? "FFF1" : "BBB1",
      numero: 0,
      sunat_transaction: 1,

      cliente_tipo_de_documento: tipoDocCliente,
      cliente_numero_de_documento: documento || "0",
      cliente_denominacion: nombre || "Consumidor Final",
      cliente_direccion: direccion || "",
      cliente_email: email || "",

      fecha_de_emision: new Date().toLocaleDateString("en-CA", {
        timeZone: "America/Lima",
      }), // Formato AAAA-MM-DD
      moneda: 1,
      porcentaje_de_igv: 18.0,

      total_gravada: valorUnitario.toFixed(2),
      total_igv: igvTotal.toFixed(2),
      total: montoNum.toFixed(2),

      items: [
        {
          unidad_de_medida: "ZZ",
          codigo: "MON001",
          descripcion: `Compra de ${monedas} monedas Geinz`,
          cantidad: 1,
          valor_unitario: valorUnitario.toFixed(2),
          precio_unitario: montoNum.toFixed(2),
          subtotal: valorUnitario.toFixed(2),
          tipo_de_igv: 1,
          igv: igvTotal.toFixed(2),
          total: montoNum.toFixed(2),
          anticipo_regularizacion: false,
        },
      ],
      enviar_automaticamente_a_la_sunat: true,
      enviar_automaticamente_al_cliente: !!email,
      codigo_unico: chargeId,
    };

    console.log(
      "Payload final a enviar a NubeFacT:",
      JSON.stringify(payload, null, 2),
    );

    const response = await axios.post(
      "https://api.nubefact.com/api/v1/02bb7d82-0b0c-4006-82a5-74b7437bea0b",
      payload,
      {
        headers: {
          Authorization: `Token token="8eee1a640fd7485cbc1da29427f59792b196deb29b954a6eb131bdb8562492fa"`,
          "Content-Type": "application/json",
        },
      },
    );

    console.log("✅ Respuesta exitosa de NubeFacT:", response.data);
    return response.data.enlace_del_pdf;
  } catch (error) {
    // LOG DE ERROR: Captura errores de validación de NubeFacT (como RUC inválido)
    console.error("❌ ERROR EN NUBEFACT:");
    if (error.response) {
      console.error("Data del error:", error.response.data);
      console.error("Status:", error.response.status);
    } else {
      console.error("Mensaje de error:", error.message);
    }
    throw error;
  }
}

async function enviarWhatsApp(numero, mensaje) {
  try {
    const telefono = `51${numero}`;

    console.log("📲 Enviando WhatsApp a:", telefono);

    const res = await axios.post(
      `https://graph.facebook.com/v19.0/${PHONE_ID}/messages`,
      {
        messaging_product: "whatsapp",
        to: telefono,
        type: "text",
        text: { body: mensaje },
      },
      {
        headers: {
          Authorization: `Bearer ${WHATSAPP_TOKEN}`,
          "Content-Type": "application/json",
        },
      },
    );

    console.log("✅ WhatsApp enviado:", res.data);

    return true;
  } catch (error) {
    console.error("❌ ERROR WHATSAPP:", error.response?.data || error.message);

    return false; // 🔥 no rompas todo tu flujo
  }
}

async function sumarSaldo(userId, monedas) {
  console.log("🟡 [sumarSaldo] INICIO");
  console.log("userId:", userId);
  console.log("monedas:", monedas);

  const ref = db
    .collection("Tiendas")
    .doc("barranca")
    .collection("barranca")
    .doc(userId);

  console.log("📄 Referencia doc creada");

  await ref.set(
    {
      puntos_tienda: admin.firestore.FieldValue.increment(monedas),
    },
    { merge: true },
  );

  console.log("✅ Saldo actualizado en Firestore");

  const snap = await ref.get();

  if (!snap.exists) {
    console.log("❌ Documento no existe");
    return null;
  }

  const data = snap.data();
  console.log("📦 DATA COMPLETA USUARIO:", JSON.stringify(data, null, 2));

  const numero = data?.metodo_contacto?.whatsapp?.numero || null;

  console.log("📲 Número WhatsApp extraído:", numero);

  return numero;
}

async function agregar_historial_de_pagos_tienda({
  id_transaccion,
  tipo_transaccion,
  metodo_pago,
  nombre_tienda,
  id_tienda,
  localidad_tienda,
  tipo_paquete,
  monto_aumentado,
  precio_soles,
  estado,
  monto_anterior,
}) {
  console.log("🚀 INICIANDO PROCESO PAGO COMPLETO");

  try {
    // HISTORIAL PRIMERO (más importante)
    const historialRef = db
      .collection("Tiendas")
      .doc(localidad_tienda)
      .collection(localidad_tienda)
      .doc(id_tienda)
      .collection("historial_financiero")
      .doc(id_transaccion);

    const data = {
      id_transaccion: id_transaccion,
      tipo_transacción: tipo_transaccion,

      hora_fecha: {
        fecha: new Date().toISOString(),
        hora: new Date().toLocaleTimeString("es-PE"),
      },

      metodo_pago: {
        yape: metodo_pago === "yape",
        plin: metodo_pago === "plin",
      },

      datos_tienda: {
        nombre_tienda,
        id_tienda,
        localidad_tienda,
      },

      datos_recarga: {
        tipo_paquete,
        monto_aumentado,
        precio_soles,
        estado,
        monto_anterior,
      },

      timestamp: admin.firestore.FieldValue.serverTimestamp(),
    };

    await historialRef.set(data);
    console.log("✅ HISTORIAL GUARDADO");

    //  LUEGO ACTUALIZAS PAGO
    const pagoRef = db
      .collection("Tiendas")
      .doc(localidad_tienda)
      .collection("pagos_tiendas")
      .doc(id_transaccion);

    await pagoRef.set(
      {
        estado: "pagado",
        actualizado_en: admin.firestore.FieldValue.serverTimestamp(),
      },
      { merge: true },
    );

    console.log("💰 PAGO MARCADO COMO PAGADO");

    await db
      .collection("Tiendas")
      .doc(localidad_tienda)
      .collection(localidad_tienda)
      .doc(id_tienda)
      .update({
        pago_actual_id: admin.firestore.FieldValue.delete(),
      });

    //  OBTENER PROPIETARIOS
    const tiendaDoc = await db
      .collection("Tiendas")
      .doc(localidad_tienda)
      .collection(localidad_tienda)
      .doc(id_tienda)
      .get();

    const propietarios = tiendaDoc.data()?.propietario_id || [];
    const mensajesRandom = [
      "🚀 Mira tus beneficios y sácales provecho.",
      "📈 Disfruta tu recarga y haz crecer tu negocio.",
      "💡 Aprovecha al máximo tus créditos disponibles.",
      "🔥 Es momento de impulsar tu tienda.",
      "✨ Dale más visibilidad a tu negocio ahora.",
      "🎯 Usa tus créditos estratégicamente y destaca.",
      "🛍️ Atrae más clientes con tus nuevas opciones.",
      "📊 Haz que tu tienda crezca con esta recarga.",
    ];
    // ENVIAR NOTIFICACIONES
    for (const propietarioId of propietarios) {
      const tokenDoc = await db
        .collection("Trabajadores_Usuarios_Drivers")
        .doc("users")
        .collection("tokens")
        .doc(propietarioId)
        .get();

      const tokens = Object.values(tokenDoc.data()?.tokens || {});

      for (const token of tokens) {
        const mensajeExtra =
          mensajesRandom[Math.floor(Math.random() * mensajesRandom.length)];

        await enviarNotificacionFCM_tienda({
          token,
          title: "¡Recarga Exitosa! 🎉",
          body: `👋 Hola ${nombre_tienda} Tu recarga de ${monto_aumentado} creditos fue procesada correctamente. ${mensajeExtra}`,
          idTienda: id_tienda,
          tipo_notificacion: "pago",
          prioridad: "high",
        });
      }
    }

    console.log("🧹 campo pago_actual_id eliminado");
  } catch (error) {
    console.error("❌ ERROR EN PROCESO DE PAGO COMPLETO:");
    console.error(error);
    throw error;
  }
}

exports.confirmarPago = onCall(async (req) => {
  console.log("=====================");
  const {
    tipo_comprobante,
    ruc,
    direccion_negocio,
    token,
    monto,
    email,
    userId,
    monedas,
    monedas_originales,
    deuda_pendiente,
    tiene_deuda,
    nombre_tienda,
    localidad,
    nombre_paquete,
    monto_anterior,
    id_select_boleta_pago,
  } = req.data;

  const tieneDeudaBool = tiene_deuda === true || tiene_deuda === "true";
  const deudaPendienteNum = Number(deuda_pendiente || 0);
  console.log("Tipo Comprobante (1:Fact, 2:Bol):", tipo_comprobante);
  console.log("🔍 Deuda check:", {
    tiene_deuda,
    deuda_pendiente,
    tipo_tiene_deuda: typeof tiene_deuda,
    tipo_deuda_pendiente: typeof deuda_pendiente,
  });
  console.log("Datos Cliente:", {
    ruc_dni: ruc,
    nombre: nombre_tienda,
    direccion: direccion_negocio,
    email: email,
  });
  console.log("Datos Transacción:", {
    id_transaccion_geinz: id_select_boleta_pago,
    monto_soles: monto,
    monedas_a_recargar: monedas,
    paquete: nombre_paquete,
    culqi_token: token,
  });
  console.log("Contexto Usuario:", {
    userId: userId,
    saldo_previo: monto_anterior,
    ubicacion: localidad,
  });
  try {
    const response = await axios.post(
      "https://api.culqi.com/v2/charges",
      {
        amount: Math.round(monto * 100),
        currency_code: "PEN",
        email: email || "cliente@geinz.com",
        source_id: token,
        capture: true,
        description: "Compra de monedas Geinz",
        antifraud_details: {
          address: "Barranca",
          address_city: "Barranca",
          country_code: "PE",
          first_name: nombre_tienda || "Cliente",
          last_name: "Geinz",
          phone: "999999999",
        },
      },
      {
        headers: {
          Authorization: `Bearer ${CULQI_KEY}`,
          "Content-Type": "application/json",
        },
      },
    );

    const charge = response.data;

    console.log("CULQI RESPONSE:", charge);

    await agregar_historial_de_pagos_tienda({
      id_transaccion: id_select_boleta_pago,
      tipo_transaccion: "recarga",
      metodo_pago: "yape",
      nombre_tienda: nombre_tienda,
      id_tienda: userId,
      localidad_tienda: localidad,
      tipo_paquete: nombre_paquete,
      monto_aumentado: monedas,
      precio_soles: monto.toString(),
      estado: "Aceptado",
      monto_anterior: monto_anterior,
    });

    const numero = await sumarSaldo(userId, monedas);

    if (tieneDeudaBool && deudaPendienteNum > 0) {
      try {
        console.log("💳 Eliminando deuda pendiente:", deuda_pendiente);

        const resultadoDeuda = await eliminar_deuda_actual(userId);

        if (!resultadoDeuda.ok) {
          console.error(
            "❌ No se pudo eliminar la deuda:",
            resultadoDeuda.error,
          );
        } else {
          console.log("✅ deuda eliminada:", resultadoDeuda);
        }

        console.log("✅ deuda eliminada");

        /* ═══════════════════════════════════════
       HISTORIAL DESCUENTO DEUDA
    ════════════════════════════════════════ */

        const deuda_soles = (Number(deuda_pendiente || 0) / 100).toFixed(2);

        await agregar_historial_de_pagos_tienda({
          id_transaccion: `${id_select_boleta_pago}_deuda`,

          tipo_transaccion: "descuento_deuda",

          metodo_pago: "saldo_automatico",

          nombre_tienda: nombre_tienda,

          id_tienda: userId,

          localidad_tienda: localidad,

          tipo_paquete: "Débito automático Geinz",

          monto_aumentado: Number(deuda_pendiente || 0),

          precio_soles: deuda_soles,

          estado: "Aceptado",

          monto_anterior: 0,
        });

        console.log("🧾 historial deuda guardado");

        // 🔔 Notificación deuda cancelada
        try {
          const tiendaDoc = await db
            .collection("Tiendas")
            .doc(localidad)
            .collection(localidad)
            .doc(userId)
            .get();

          const propietarios = tiendaDoc.data()?.propietario_id || [];

          for (const propietarioId of propietarios) {
            const tokenDoc = await db
              .collection("Trabajadores_Usuarios_Drivers")
              .doc("users")
              .collection("tokens")
              .doc(propietarioId)
              .get();

            const tokens = Object.values(tokenDoc.data()?.tokens || {});

            for (const token of tokens) {
              await enviarNotificacionFCM_tienda({
                token,
                title: "✅ ¡Deuda cancelada exitosamente!",
                body: `Tu deuda de ${deudaPendienteNum} créditos fue saldada automáticamente con tu recarga. Ya estás al día 🎉`,
                link: "https://geinzworkapp.web.app/api/api/share?t=scr&id=rec",
                logo: "https://firebasestorage.googleapis.com/v0/b/geinzworkapp.appspot.com/o/logo_geinz_webp.webp?alt=media&token=aa1ef1df-1bcd-48f2-9cad-a85929c3a8d0",
                idTienda: userId,
                idAnuncio: "",
                tipo_notificacion: "logo",
                prioridad: "high",
              });
            }
          }
        } catch (notiDeudaErr) {
          console.error(
            "⚠️ Error enviando notificación de deuda cancelada:",
            notiDeudaErr,
          );
        }
      } catch (deudaErr) {
        console.error("❌ Error eliminando deuda:", deudaErr);
      }
    }

    try {
      const pdfUrl = await emitirComprobanteGeinz({
        tipoComprobante: tipo_comprobante,
        documento: ruc,
        nombre: nombre_tienda,
        direccion: direccion_negocio,
        monedas,
        chargeId: charge.id,
        monto,
        email: "cliente@geinz.com",
      });
      if (typeof numero === "string" && numero.length >= 9) {
        await enviarPDFWhatsApp(numero, pdfUrl);
        enviarWhatsApp(
          937659216,
          `✅ *Pago exitoso en Geinz*\n` +
            `🏪 *Negocio:* ${nombre_tienda}\n` +
            `🆔 *ID Negocio:* ${userId}\n` +
            `📦 *Paquete:* ${nombre_paquete}\n` +
            `💰 *Monto pagado:* S/ ${monto}\n` +
            `🪙 *Monedas acreditadas:* ${monedas}\n` +
            `🧾 *ID Transacción:* ${id_select_boleta_pago}\n` +
            `💳 *ID Cargo Culqi:* ${charge.id}\n` +
            `📅 *Fecha:* ${new Date().toLocaleString("es-PE", { timeZone: "America/Lima" })}`,
        );
      }
    } catch (nubefactErr) {
      console.error(
        "⚠️ Nubefact falló:",
        nubefactErr.response?.data || nubefactErr.message,
      );
    }

    return {
      ok: true,
      chargeId: charge.id,
    };
  } catch (error) {
    const culqiError = error.response?.data;

    console.error("ERROR CHARGE:", culqiError || error.message);

    const motivo = culqiError?.user_message || "Error en el pago";

    await enviarWhatsApp(
      "937659216",
      `❌ *Pago rechazado en Geinz*\n\n` +
        `🏪 *Negocio:* ${nombre_tienda}\n` +
        `🆔 *ID:* ${userId}\n` +
        `💰 *Monto:* S/ ${monto}\n` +
        `⚠️ *Motivo:* ${motivo}\n` +
        `📅 *Fecha:* ${new Date().toLocaleString("es-PE", { timeZone: "America/Lima" })}`,
    );

    throw new HttpsError("failed-precondition", motivo);
  }
});

// ==================== verificar_usuario_asistente ====================
exports.verificar_usuario_asistente = onRequest(async (req, res) => {
  try {
    const { numero_usuario, nombre_user, id_user } = req.body;

    if (!numero_usuario) {
      return res.status(400).json({ error: "numero_usuario requerido" });
    }

    const ref = db
      .collection("Trabajadores_Usuarios_Drivers")
      .doc("usuario_bot_geinz")
      .collection("usuario_bot_geinz")
      .doc(numero_usuario);

    const doc = await ref.get();

    // SI EXISTE
    if (doc.exists) {
      const data = doc.data();

      return res.json({
        exists: true,
        nombre_cliente: data.nombre_user || "",
        numero_cliente: numero_usuario,
        estado_cuenta: data.status || "activo",
        fecha_bloqueo: data.fecha_bloqueo || null,
        motivo_bloqueo: data.motivo_bloqueo || "",
        contexto: data.contexto || null,
      });
    }

    // SI NO EXISTE → CREAR
    const nuevoUsuario = {
      nombre_user: nombre_user || "Usuario",
      numero_user: numero_usuario,
      id_user: id_user || "",
      status: "activo",
      createdAt: admin.firestore.FieldValue.serverTimestamp(),
    };

    await ref.set(nuevoUsuario);

    return res.json({
      exists: false,
      nombre_cliente: nuevoUsuario.nombre_user,
      numero_cliente: numero_usuario,
      estado_cuenta: "activo",
      fecha_bloqueo: null,
      motivo_bloqueo: "",
      contexto: data.contexto || null,
    });
  } catch (error) {
    console.error(error);
    return res.status(500).json({ error: "Error interno" });
  }
});

// ==================== agregar_pago_del_usuario ====================
exports.agregar_pago_para_el_usuario_tienda = onCall(async (req) => {
  const {
    id_tienda,
    nombre_user,
    plan_select,
    localdiad,
    saldo_tienda,
    categoira_tienda,
    logo_tienda,
    nombre_plan,
    monto_pagar_de_plan,
  } = req.data;

  if (!id_tienda || !nombre_user || !plan_select || !localdiad) {
    console.error("Faltan datos obligatorios:", {
      id_tienda,
      nombre_user,
      plan_select,
      localdiad,
    });
    throw new Error("Faltan datos obligatorios");
  }

  const tiendaRef = db
    .collection("Tiendas")
    .doc(localdiad)
    .collection(localdiad)
    .doc(id_tienda);

  const pagosRef = db
    .collection("Tiendas")
    .doc(localdiad)
    .collection("pagos_tiendas");

  const result = await db.runTransaction(async (t) => {
    const tiendaSnap = await t.get(tiendaRef);
    const tiendaData = tiendaSnap.data() || {};

    let pagoRef;

    // SI YA EXISTE PAGO PENDIENTE → REUTILIZAR
    if (tiendaData.pago_actual_id) {
      pagoRef = pagosRef.doc(tiendaData.pago_actual_id);

      t.set(
        pagoRef,
        {
          nombre_user,
          plan_select,
          saldo_tienda,
          categoira_tienda,
          logo_tienda,
          localdiad,
          monto_pagar_de_plan,
          actualizado_en: admin.firestore.FieldValue.serverTimestamp(),
          nombre_plan: nombre_plan || "",
        },
        { merge: true },
      );

      return {
        id_pago: pagoRef.id,
        reutilizado: true,
      };
    }

    //  SI NO EXISTE → CREAR NUEVO
    pagoRef = pagosRef.doc();

    const data = {
      id_pago: pagoRef.id,
      id_tienda,
      nombre_user,
      plan_select,
      saldo_tienda,
      categoira_tienda,
      logo_tienda,
      localdiad,
      monto_pagar_de_plan,
      nombre_plan: nombre_plan || "",
      fecha_pago: admin.firestore.FieldValue.serverTimestamp(),
      estado: "pendiente",
    };

    t.set(pagoRef, data);

    t.set(
      tiendaRef,
      {
        pago_actual_id: pagoRef.id,
        estado_pago: "pendiente",
      },
      { merge: true },
    );

    return {
      id_pago: pagoRef.id,
      reutilizado: false,
    };
  });

  return {
    ok: true,
    id_pago: result.id_pago,
    reutilizado: result.reutilizado,
  };
});

// ==================== Algolia ====================

function shuffle(array) {
  for (let i = array.length - 1; i > 0; i--) {
    const j = Math.floor(Math.random() * (i + 1));
    [array[i], array[j]] = [array[j], array[i]];
  }
  return array;
}

function similitud(a, b) {
  a = normalizar(a);
  b = normalizar(b);

  if (!a || !b) return 0;

  if (a.includes(b) || b.includes(a)) return 1;

  const palabrasA = a.split(" ");
  const palabrasB = b.split(" ");

  let matches = 0;

  palabrasB.forEach((palabra) => {
    if (palabrasA.some((p) => p.includes(palabra))) {
      matches++;
    }
  });

  return matches / palabrasB.length;
}

function esBusquedaDifusa(texto) {
  const limpio = normalizar(texto);
  const palabras = limpio.split(" ").filter((p) => p.length > 2);

  if (limpio.length < 4) return true;

  const sinVocales = palabras.some((p) => !/[aeiou]/.test(p));
  const muchasCortas = palabras.filter((p) => p.length <= 3).length >= 2;

  return sinVocales || muchasCortas;
}

async function buscarRapido(ref, nombre) {
  const palabras = normalizar(nombre).split(" ");
  const palabraClave = palabras.find((p) => p.length > 3) || palabras[0];

  const snap = await ref
    .where("nombre_keywords", "array-contains", palabraClave)
    .limit(50)
    .get();

  return snap.docs.map((doc) => ({
    id: doc.id,
    ...doc.data(),
  }));
}

async function buscarInteligente(ref, nombre) {
  const palabras = normalizar(nombre)
    .split(" ")
    .filter((p) => p.length > 2);

  const keywords = palabras.slice(0, 2);

  const queries = keywords.map((k) =>
    ref.where("nombre_keywords", "array-contains", k).limit(30).get(),
  );

  const snaps = await Promise.all(queries);

  let mapa = new Map();

  snaps.forEach((snap) => {
    snap.docs.forEach((doc) => {
      mapa.set(doc.id, {
        id: doc.id,
        ...doc.data(),
      });
    });
  });

  return Array.from(mapa.values());
}

function rankear(resultados, nombreBuscado) {
  const buscado = normalizar(nombreBuscado);
  const palabras = buscado.split(" ");

  let conScore = resultados.map((tienda) => {
    const nombreDB = normalizar(
      tienda.nombre_lower || tienda.nombre_tienda || "",
    );

    let score = 0;

    // match exacto completo
    if (nombreDB.includes(buscado)) score += 3;

    //coincidencias por palabra
    let matches = 0;
    palabras.forEach((p) => {
      if (nombreDB.includes(p)) matches++;
    });
    score += matches;

    //  bonus si contiene TODAS
    if (palabras.every((p) => nombreDB.includes(p))) {
      score += 2;
    }

    // similitud general
    score += similitud(nombreDB, buscado);

    return { tienda, score };
  });

  conScore = conScore
    .filter((t) => t.score > 1)
    .sort((a, b) => b.score - a.score);

  return conScore.map((t) => t.tienda);
}

function limpiar(texto) {
  return (texto || "")
    .toLowerCase()
    .normalize("NFD")
    .replace(/[\u0300-\u036f]/g, "")
    .replace(/\b(el|la|los|las|de|del)\b/g, "")
    .replace(/\s+/g, " ")
    .trim();
}

exports.buscarTiendasSmart = onRequest(async (req, res) => {
  try {
    const { localidad, nombre_negocio } = req.body;

    if (!localidad || !nombre_negocio) {
      return res.status(400).json({
        ok: false,
        error: "faltan datos",
      });
    }

    const ref = admin
      .firestore()
      .collection("Tiendas")
      .doc(localidad)
      .collection(localidad);

    let resultados = [];

    const modoDifuso = esBusquedaDifusa(nombre_negocio);

    console.log("🧠 MODO:", modoDifuso ? "INTELIGENTE" : "RAPIDO");

    //  DECISIÓN
    if (modoDifuso) {
      resultados = await buscarInteligente(ref, nombre_negocio);
    } else {
      resultados = await buscarRapido(ref, nombre_negocio);

      if (resultados.length === 0) {
        resultados = await buscarInteligente(ref, nombre_negocio);
      }
    }

    console.log("📊 RESULTADOS:", resultados.length);

    //  ordenar
    let ordenados = rankear(resultados, nombre_negocio);

    //  horario
    const response = ordenados.map((tienda) => ({
      id: tienda.id,
      name: tienda.nombre_tienda,
      desc: (tienda.descripcion_seo || "").substring(0, 120),
      ref: tienda.ubicacion?.referencia || "",
      wha: tienda.metodo_contacto?.whatsapp?.numero || "",
      loc: tienda.localidad,
      cat: tienda.categoria_tienda,
      img: tienda.img_tienda ? tienda.img_tienda.imagen_bot || "" : "",
      open_state: verificar_apertura_tienda(tienda.horario_atencion),
      tipo: "tienda",
    }));

    // abiertos primero
    const abiertos = response.filter((t) => t.open_state === true);
    const baseFinal = abiertos.length > 0 ? abiertos : response;

    const final = baseFinal.slice(0, 3);

    return res.json({
      ok: true,
      modo: modoDifuso ? "inteligente" : "rapido",
      total: final.length,
      data: final,
    });
  } catch (error) {
    console.error(error);
    return res.status(500).json({ ok: false });
  }
});

exports.buscarTiendas = onRequest(async (req, res) => {
  try {
    const { localidad, nombre_negocio, categoria, subcategoria } = req.body;

    console.log("📥 BODY:", req.body);

    if (!localidad) {
      return res.status(400).json({
        ok: false,
        error: "localidad es obligatoria",
      });
    }

    const ref = admin
      .firestore()
      .collection("Tiendas")
      .doc(localidad)
      .collection(localidad);

    const snapshot = await ref.get();

    let resultados = snapshot.docs.map((doc) => ({
      id: doc.id,
      ...doc.data(),
    }));

    console.log("📊 TOTAL INICIAL:", resultados.length);

    // =========================
    // FILTRO NOMBRE
    // =========================
    if (nombre_negocio) {
      const nombre = normalizar(nombre_negocio);

      resultados = resultados.filter((tienda) => {
        const target = normalizar(
          tienda.nombre_lower || tienda.nombre_tienda || "",
        );
        return target.includes(nombre);
      });

      console.log("🔍 NOMBRE:", resultados.length);
    }

    // =========================
    // FILTRO CATEGORIA (FLEXIBLE)
    // =========================
    if (categoria) {
      const cat = normalizar(categoria);

      resultados = resultados.filter((tienda) => {
        const categoriaDB = normalizar(tienda.categoria_tienda || "");
        return categoriaDB.includes(cat) || cat.includes(categoriaDB);
      });

      console.log("🏷 CATEGORIA:", resultados.length);
    }

    // =========================
    //  FILTRO SUBCATEGORIA
    // =========================
    if (subcategoria) {
      const palabrasBusqueda = normalizar(subcategoria).split(" ");

      resultados = resultados.filter((tienda) => {
        if (!Array.isArray(tienda.subcategoria)) return false;

        return tienda.subcategoria.some((sc) => {
          const value = normalizar(sc);

          return palabrasBusqueda.some((palabra) => value.includes(palabra));
        });
      });

      console.log("🍕 SUBCATEGORIA:", resultados.length);
    }
    // =========================
    // HORARIO PERÚ
    // =========================
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

    console.log("📅 DIA:", diaActual);

    // =========================
    // 🔥 MAP
    // =========================
    const response = resultados.map((tienda) => {
      const horario = tienda.horario_atencion?.[diaActual];

      let abierto = false;

      if (horario && horario.cerrado !== true) {
        const bloques = horario.bloques || [];

        for (const bloque of bloques) {
          if (!bloque?.h_apertura || !bloque?.h_cierre) continue;

          const [ha, ma] = bloque.h_apertura.split(":").map(Number);
          const [hc, mc] = bloque.h_cierre.split(":").map(Number);

          const apertura = ha * 60 + ma;
          const cierre = hc * 60 + mc;

          if (apertura <= cierre) {
            if (minutosActual >= apertura && minutosActual <= cierre) {
              abierto = true;
              break;
            }
          } else {
            if (minutosActual >= apertura || minutosActual <= cierre) {
              abierto = true;
              break;
            }
          }
        }
      }

      return {
        id: tienda.id,
        name: tienda.nombre_tienda,
        desc: (tienda.descripcion_seo || "").substring(0, 150),
        ref: tienda.ubicacion?.referencia || "",
        wha: tienda.metodo_contacto?.whatsapp?.numero || "",
        loc: tienda.localidad,
        cat: tienda.categoria_tienda,
        img: tienda.img_tienda ? tienda.img_tienda.imagen_bot || "" : "",
        open_state: abierto,
        tipo: "tienda",
      };
    });

    // =========================
    // PRIORIDAD: ABIERTOS
    // =========================
    let abiertos = response.filter((t) => t.open_state);

    console.log("🟢 ABIERTOS:", abiertos.length);

    // fallback si no hay abiertos
    let baseFinal = abiertos.length > 0 ? abiertos : response;

    // =========================
    // RANDOM + MAX 3
    // =========================
    const final = shuffle(baseFinal).slice(0, 3);

    return res.json({
      ok: true,
      total: final.length,
      data: final,
    });
  } catch (error) {
    console.error("ERROR:", error);

    return res.status(500).json({
      ok: false,
      error: error.message,
    });
  }
});

exports.buscar_tienda_por_categorias_y_subcategoria = onRequest(
  async (req, res) => {
    console.log("======================================");
    console.log("🚀 INICIO buscar_tienda_por_categorias_y_subcategoria");

    try {
      console.log("📥 BODY RECIBIDO:", req.body);

      const { localidad, categoria, subcategoria } = {
        localidad: limpiar(req.body.localidad),
        categoria: limpiar(req.body.categoria),
        subcategoria: req.body.subcategoria || null,
      };

      console.log("🧹 PARAMS LIMPIOS:");
      console.log("➡️ localidad:", localidad);
      console.log("➡️ categoria:", categoria);
      console.log("➡️ subcategoria:", subcategoria);

      if (!localidad || !categoria) {
        console.log("❌ FALTAN PARAMETROS OBLIGATORIOS");
        return res.status(400).json({
          ok: false,
          error: "localidad y categoria son obligatorias",
        });
      }

      console.log("📡 CONSTRUYENDO QUERY FIRESTORE...");

      let query = admin
        .firestore()
        .collection("Tiendas")
        .doc(localidad)
        .collection(localidad)
        .where("categoria_tienda", "==", categoria);

      console.log("🔎 Query base aplicada (categoria)");

      // subcategoria opcional
      if (subcategoria) {
        console.log("🔎 Agregando filtro subcategoria:", subcategoria);
        query = query.where("subcategoria", "array-contains", subcategoria);
      } else {
        console.log("⚠️ No se aplicó filtro de subcategoria");
      }

      console.log("⏳ Ejecutando query...");
      const snapshot = await query.get();

      console.log("📊 DOCUMENTOS OBTENIDOS:", snapshot.size);

      let resultados = snapshot.docs.map((doc) => {
        const data = doc.data();
        console.log("📄 DOC:", doc.id, data);

        return {
          id: doc.id,
          ...data,
        };
      });

      console.log("📊 TOTAL RESULTADOS:", resultados.length);

      // =========================
      // MAP + HORARIO
      // =========================
      console.log("🧠 PROCESANDO HORARIOS...");

      const response = resultados.map((tienda) => {
        const estado = verificar_apertura_tienda(tienda.horario_atencion);

        console.log("🏪 TIENDA:", tienda.nombre_tienda);
        console.log("⏰ horario:", tienda.horario_atencion);
        console.log("🟢 estado abierto:", estado);

        return {
          id: tienda.id,
          name: tienda.nombre_tienda,
          desc: (tienda.descripcion_seo || "").substring(0, 150),
          ref: tienda.ubicacion?.referencia || "",
          wha: tienda.metodo_contacto?.whatsapp?.numero || "",
          loc: tienda.localidad,
          cat: tienda.categoria_tienda,
          img: tienda.img_tienda ? tienda.img_tienda.imagen_bot || "" : "",
          open_state: estado, // true | false | null
          tipo: "tienda",
        };
      });

      console.log("📦 RESPONSE GENERADO:", response);

      // =========================
      // PRIORIDAD ABIERTOS
      // =========================
      const abiertos = response.filter((t) => t.open_state === true);

      console.log("🟢 TIENDAS ABIERTAS:", abiertos.length);

      const baseFinal = abiertos.length > 0 ? abiertos : response;

      if (abiertos.length > 0) {
        console.log("✅ Se priorizan tiendas abiertas");
      } else {
        console.log("⚠️ No hay abiertas, se usan todas");
      }

      // =========================
      // RANDOM + LIMITE
      // =========================
      console.log("🎲 Aplicando random + límite 3");

      const final = baseFinal.sort(() => Math.random() - 0.5).slice(0, 3);

      console.log("🏁 RESULTADO FINAL:", final);

      return res.json({
        ok: true,
        hayAbiertos: abiertos.length > 0,
        total: final.length,
        data: final,
      });
    } catch (error) {
      console.error("💥 ERROR GENERAL:", error);

      return res.status(500).json({
        ok: false,
        error: "Error interno",
      });
    }
  },
);

exports.agregar_usuario_de_geinz_bot = onRequest(async (req, res) => {
  try {
    const { nombre_user, id_user, numero_user, from_user_id } = req.body;

    if (!numero_user) {
      return res.status(400).json({
        ok: false,
        msg: "El número de usuario es obligatorio",
      });
    }

    const ref = admin
      .firestore()
      .collection("Trabajadores_Usuarios_Drivers")
      .doc("usuario_bot_geinz")
      .collection("usuario_bot_geinz")
      .doc(numero_user);

    const data = {
      nombre_user: nombre_user || null,
      id_user: id_user || null,
      numero_user,
      from_user_id: from_user_id || null,
      fecha_registro: admin.firestore.FieldValue.serverTimestamp(),
    };

    await ref.set(data, { merge: true });

    return res.json({
      ok: true,
      msg: "Usuario guardado correctamente",
    });
  } catch (error) {
    console.error(error);
    return res.status(500).json({
      ok: false,
      msg: "Error al guardar usuario",
    });
  }
});

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

exports.obtenerCategorias = onRequest(async (req, res) => {
  try {
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

    return res.json({
      ok: true,
      categorias,
    });
  } catch (error) {
    return res.status(500).json({
      ok: false,
      error: "Error interno",
    });
  }
});

exports.obtener_subcategoira_de_cat = onRequest(async (req, res) => {
  try {
    const categoria = (req.body?.categoria || "").trim().toLowerCase();

    if (!categoria) {
      return res.status(400).json({
        ok: false,
        error: "Categoría inválida",
      });
    }

    const snap = await admin
      .firestore()
      .doc(`Tiendas/categorias/categorias/${categoria}`)
      .get();

    if (!snap.exists) {
      return res.json({
        ok: true,
        data: [],
      });
    }

    return res.json({
      ok: true,
      data: snap.get("subcategorias") ?? [],
    });
  } catch (error) {
    return res.status(500).json({
      ok: false,
      error: "Error interno",
    });
  }
});

exports.obtener_lugares_seguros = onRequest(async (req, res) => {
  try {
    const { localidad, categoria } = req.body;

    let query = admin
      .firestore()
      .collection("Tiendas")
      .doc("salud_seguridad")
      .collection(localidad);

    if (categoria && categoria !== "general") {
      query = query.where("categoria", "==", categoria);
    }

    const snapshot = await query
      .select("id", "categoria", "nombre", "numeros_contactos", "ubicacion")
      .get();

    if (snapshot.empty) {
      return res.status(200).json({ ok: true, total: 0, data: [] });
    }

    const data = snapshot.docs.map((doc) => {
      const d = doc.data();
      return {
        id: d.id ?? doc.id,
        c: d.categoria,
        n: d.nombre,
        num: d.numeros_contactos ?? { llamada: [], whatsapp: [] },
        ub: d.ubicacion ?? null,
      };
    });

    res.set("Cache-Control", "public, max-age=300");

    return res.status(200).json({
      ok: true,
      total: data.length,
      data,
    });
  } catch (error) {
    console.error("Error en obtener_lugares_seguros:", error);
    return res.status(500).json({
      ok: false,
      mensaje: "Error interno al filtrar por categoría",
    });
  }
});

function scoreLugar(lugar, nombre_negocio, subcategoria) {
  let score = 0;

  const nombre = lugar.nombre_lower || lugar.titulo || "";

  if (nombre_negocio) {
    const palabras = limpiar(nombre_negocio).split(" ");
    const texto = limpiar(nombre);

    palabras.forEach((p) => {
      if (texto.includes(p)) score += 2;
    });
  }

  if (subcategoria && Array.isArray(lugar.categoria)) {
    const sub = limpiar(subcategoria);

    if (lugar.categoria.some((c) => limpiar(c).includes(sub))) {
      score += 1;
    }
  }

  return score;
}

function seleccionarAleatorioPonderado(lista, limite) {
  const seleccionados = [];
  const copia = [...lista];

  while (seleccionados.length < limite && copia.length > 0) {
    const totalScore = copia.reduce(
      (sum, item) => sum + (item.score > 0 ? item.score : 1),
      0,
    );
    let r = Math.random() * totalScore;

    for (let i = 0; i < copia.length; i++) {
      r -= copia[i].score || 1;

      if (r <= 0) {
        seleccionados.push(copia[i]);
        copia.splice(i, 1);
        break;
      }
    }
  }

  return seleccionados;
}
exports.obtener_lugares_turisticos_directos = onRequest(async (req, res) => {
  try {
    const { localidad, nombre_negocio, subcategoria } = req.body;

    console.log("📥 BODY:", req.body);

    if (!localidad) {
      return res.status(400).json({
        ok: false,
        error: "localidad es obligatoria",
      });
    }

    const ref = admin
      .firestore()
      .collection("Tiendas")
      .doc(localidad)
      .collection("lugares_turisticos");

    const snapshot = await ref.get();

    let resultados = snapshot.docs.map((doc) => ({
      id: doc.id,
      ...doc.data(),
    }));

    console.log("📊 TOTAL INICIAL:", resultados.length);

    // =========================
    // FUNCIONES BASE
    // =========================

    function matchNombre(target, busqueda) {
      if (!busqueda) return true;

      const palabras = limpiar(busqueda).split(" ");
      const texto = limpiar(target);

      return palabras.every((p) => texto.includes(p));
    }

    function matchCategoria(categorias, sub) {
      if (!sub) return true;
      if (!Array.isArray(categorias)) return false;

      const subLimpio = limpiar(sub);

      return categorias.some((c) => limpiar(c).includes(subLimpio));
    }

    // =========================
    // FILTRO DINÁMICO
    // =========================
    let filtrados = resultados.filter((lugar) => {
      const nombreOK = matchNombre(
        lugar.nombre_lower || lugar.titulo,
        nombre_negocio,
      );

      const categoriaOK = matchCategoria(lugar.categoria, subcategoria);

      return nombreOK && categoriaOK;
    });

    console.log("🔎 FILTRO INICIAL:", filtrados.length);

    // =========================
    //FALLBACK
    // =========================
    if (filtrados.length === 0 && nombre_negocio && subcategoria) {
      console.log("⚠️ fallback → solo nombre");

      filtrados = resultados.filter((lugar) =>
        matchNombre(lugar.nombre_lower || lugar.titulo, nombre_negocio),
      );
    }

    if (filtrados.length === 0 && subcategoria) {
      console.log("⚠️ fallback → solo categoría");

      filtrados = resultados.filter((lugar) =>
        matchCategoria(lugar.categoria, subcategoria),
      );
    }

    if (filtrados.length === 0) {
      console.log("⚠️ fallback → general");

      filtrados = resultados;
    }

    // =========================
    // SCORE + RANDOM
    // =========================
    const LIMITE = 4;

    filtrados = filtrados.map((lugar) => ({
      ...lugar,
      score: scoreLugar(lugar, nombre_negocio, subcategoria), // 👈 usa tu función externa
    }));

    filtrados = seleccionarAleatorioPonderado(filtrados, LIMITE);

    // =========================
    //RESPUESTA FINAL
    // =========================
    const data = filtrados.map((lugar) => ({
      id: lugar.id,
      titulo: lugar.titulo || "",
      descripcion: (lugar.descripcion || "").substring(0, 150),
      img: lugar.img?.principal || "",
      tipo: "turismo",
    }));

    return res.json({
      ok: true,
      total: data.length,
      data,
    });
  } catch (error) {
    console.error("ERROR:", error);
    return res.status(500).json({
      ok: false,
      error: error.message,
    });
  }
});

exports.syncLugarToAlgolia = onDocumentWritten(
  {
    document: "lugares/{lugarId}",
    region: "us-central1",
  },
  async (event) => {
    const lugarId = event.params.lugarId;

    if (!event.data.after.exists) {
      await index.deleteObject(lugarId);
      logger.info(`Documento ${lugarId} eliminado de Algolia`);
      return;
    }

    const data = event.data.after.data();
    data.objectID = lugarId;
    await index.saveObject(data);
    logger.info(`Documento ${lugarId} agregado/actualizado en Algolia`);
  },
);

exports.webhookCulqi = onRequest(
  { region: "us-central1" },
  async (req, res) => {
    try {
      const body = req.rawBody ? JSON.parse(req.rawBody.toString()) : req.body;

      const event = body;

      console.log("WEBHOOK RECIBIDO:");
      console.log("Body:", JSON.stringify(event));

      if (!event || !event.type) {
        console.log("⚠️ Evento vacío o inválido");
        return res.status(200).send("ok");
      }

      if (event.type === "order.status.changed") {
        const order = event.data;

        console.log("Estado:", order?.payment_status);

        if (order?.payment_status === "paid") {
          console.log("✅ PAGO CONFIRMADO");

          const orderId = order.order_number;

          const db = admin.firestore();
          const docRef = db.collection("ordenes_pagos").doc(orderId);
          const doc = await docRef.get();

          if (!doc.exists) {
            console.log("❌ Orden no encontrada:", orderId);
            return res.status(200).send("ok");
          }

          const data = doc.data();

          if (data.estado === "pagado") {
            console.log("⚠️ Ya estaba pagado");
            return res.status(200).send("ok");
          }

          await db
            .collection("Tiendas")
            .doc(data.localidad)
            .collection(data.localidad)
            .doc(data.userId)
            .set(
              {
                puntos_tienda: admin.firestore.FieldValue.increment(
                  data.monedas,
                ),
              },
              { merge: true },
            );

          await docRef.update({
            estado: "pagado",
            paidAt: admin.firestore.FieldValue.serverTimestamp(),
          });

          console.log("💰 Monedas entregadas");
        }
      }

      res.status(200).send("ok");
    } catch (error) {
      console.error("❌ Error webhook:", error);
      res.status(500).send("error");
    }
  },
);
// ==================== Notificaciones ====================
// ==================== Notificaciones ====================

exports.enviar_notificacion_con_solo_id = onRequest(async (req, res) => {
  res.set("Access-Control-Allow-Origin", "*");

  if (req.method !== "POST") {
    return res.status(405).send("Método no permitido");
  }

  try {
    const { id_tienda, localidad, nombre_negocio } = req.body;

    if (!id_tienda || !localidad) {
      return res.status(400).json({
        ok: false,
        error: "Faltan parámetros: id_tienda y localidad son requeridos.",
      });
    }

    const localidadLower = localidad.toLowerCase().trim();

    // 1️⃣ Obtener documento de la tienda
    const tiendaSnap = await db
      .collection("Tiendas")
      .doc(localidadLower)
      .collection(localidadLower)
      .doc(id_tienda)
      .get();

    if (!tiendaSnap.exists) {
      return res.status(404).json({
        ok: false,
        error: "Tienda no encontrada.",
      });
    }

    const propietario_ids = tiendaSnap.data().propietario_id || [];

    if (propietario_ids.length === 0) {
      return res.status(404).json({
        ok: false,
        error: "La tienda no tiene propietarios registrados.",
      });
    }

    // 2️⃣ Obtener tokens de cada propietario
    const tokensSnaps = await Promise.all(
      propietario_ids.map((uid) =>
        db
          .collection("Trabajadores_Usuarios_Drivers")
          .doc("users")
          .collection("tokens")
          .doc(uid)
          .get()
          .catch(() => null),
      ),
    );

    // 3️⃣ Extraer tokens válidos
    const todosLosTokens = tokensSnaps.flatMap((snap) => {
      if (!snap?.exists) return [];
      return Object.values(snap.data()?.tokens || {}).filter(Boolean);
    });

    if (todosLosTokens.length === 0) {
      return res.status(404).json({
        ok: false,
        error: "No se encontraron tokens para los propietarios.",
      });
    }

    // 4️⃣ Enviar notificación a cada token
    await Promise.all(
      todosLosTokens.map((token) =>
        enviarNotificacionFCM_tienda({
          token,
          title: `📢 ${nombre_negocio}, te están buscando`,
          body: `El asistente Daniel 🤖 recomendó a ${nombre_negocio} a un usuario interesado. pero debido a que tu plantilla premium no tenía saldo activo, no se mostró el acceso directo a tu WhatsApp 📲 y se compartió tu perfil de Geinz 🏪. Verifica tu saldo 💳 para seguir conectando con clientes potenciales directo desde whatsapp🚀`,
          link: "https://geinzworkapp.web.app/api/api/share?t=scr&id=rec",
          logo: "https://firebasestorage.googleapis.com/v0/b/geinzworkapp.appspot.com/o/logo_geinz_webp.webp?alt=media&token=aa1ef1df-1bcd-48f2-9cad-a85929c3a8d0",
          idTienda: id_tienda,
          idAnuncio: "",
          tipo_notificacion: "logo",
          prioridad: "high",
        }),
      ),
    );

    return res.status(200).json({
      ok: true,
      total_tokens: todosLosTokens.length,
    });
  } catch (error) {
    console.error("🔥 Error en enviar_notificacion_con_solo_id:", error);

    return res.status(500).json({
      ok: false,
      error: error.message,
    });
  }
});

exports.enviar_notificacion_deuda_acumulada = onRequest(async (req, res) => {
  res.set("Access-Control-Allow-Origin", "*");
  res.set("Access-Control-Allow-Methods", "POST, OPTIONS");
  res.set("Access-Control-Allow-Headers", "Content-Type");

  // ✅ PREFLIGHT
  if (req.method === "OPTIONS") {
    return res.status(204).send("");
  }

  if (req.method !== "POST") {
    return res.status(405).send("Método no permitido");
  }

  try {
    const {
      id_tienda,
      localidad,
      nombre_negocio,
      deuda,

      // ✅ DINÁMICOS
      titulo,
      mensaje,
      link,
    } = req.body;

    if (!id_tienda || !localidad || deuda === undefined) {
      return res.status(400).json({
        ok: false,
        error:
          "Faltan parámetros: id_tienda, localidad y deuda son requeridos.",
      });
    }

    const localidadLower = localidad.toLowerCase().trim();

    /* ═══════════════════════════════
         OBTENER TIENDA
      ═══════════════════════════════ */

    const tiendaSnap = await db
      .collection("Tiendas")
      .doc(localidadLower)
      .collection(localidadLower)
      .doc(id_tienda)
      .get();

    if (!tiendaSnap.exists) {
      return res.status(404).json({
        ok: false,
        error: "Tienda no encontrada.",
      });
    }

    const propietario_ids = tiendaSnap.data().propietario_id || [];

    if (propietario_ids.length === 0) {
      return res.status(404).json({
        ok: false,
        error: "La tienda no tiene propietarios registrados.",
      });
    }

    /* ═══════════════════════════════
         TOKENS
      ═══════════════════════════════ */

    const tokensSnaps = await Promise.all(
      propietario_ids.map((uid) =>
        db
          .collection("Trabajadores_Usuarios_Drivers")
          .doc("users")
          .collection("tokens")
          .doc(uid)
          .get()
          .catch(() => null),
      ),
    );

    const todosLosTokens = tokensSnaps.flatMap((snap) => {
      if (!snap?.exists) return [];

      return Object.values(snap.data()?.tokens || {}).filter(Boolean);
    });

    if (todosLosTokens.length === 0) {
      return res.status(404).json({
        ok: false,
        error: "No se encontraron tokens para los propietarios.",
      });
    }

    /* ═══════════════════════════════
         ENVIAR NOTIFICACIONES
      ═══════════════════════════════ */

    await Promise.all(
      todosLosTokens.map((token) =>
        enviarNotificacionFCM_tienda({
          token,

          // ✅ DINÁMICOS
          title: titulo || `⚠️ ${nombre_negocio}, tienes una deuda acumulada`,

          body:
            mensaje ||
            `🚨 Tu negocio tiene una deuda acumulada de ${deuda} créditos 💳
📲 Tu WhatsApp sigue recibiendo clientes y clicks directos gracias a tu plantilla premium 🚀
Recarga tu saldo para seguir recibiendo pedidos sin interrupciones 🔥
⚠️ Si la deuda supera los 300 créditos, tu cuenta pasará automáticamente al plan gratis y el saldo pendiente se descontará en tu próxima recarga.`,

          link: link || "https://geinzworkapp.web.app/api/api/share?t=scr&id=rec",

          // ✅ LO DEMÁS IGUAL
          logo: "https://firebasestorage.googleapis.com/v0/b/geinzworkapp.appspot.com/o/logo_geinz_webp.webp?alt=media&token=aa1ef1df-1bcd-48f2-9cad-a85929c3a8d0",

          idTienda: id_tienda,

          idAnuncio: "",

          tipo_notificacion: "logo",

          prioridad: "high",
        }),
      ),
    );

    return res.status(200).json({
      ok: true,
      total_tokens: todosLosTokens.length,
    });
  } catch (error) {
    console.error("🔥 Error en enviar_notificacion_deuda_acumulada:", error);

    return res.status(500).json({
      ok: false,
      error: error.message,
    });
  }
});

exports.enviarNotificacion = onRequest(async (req, res) => {
  res.set("Access-Control-Allow-Origin", "*");

  if (req.method !== "POST") {
    return res.status(405).send("Método no permitido");
  }

  try {
    const {
      token,
      title,
      body,
      link = "",
      logo = "", // 👈 NUEVO (opcional)
      image = "", // 👈 YA EXISTE
      idTienda = "",
      idAnuncio = "",
      tipo_notificacion = "logo", // logo | imagen | premium,
      prioridad = "high",
    } = req.body;

    // 🔹 Validación mínima del tipo
    const tiposPermitidos = ["logo", "imagen", "premium"];
    if (!tiposPermitidos.includes(tipo_notificacion)) {
      return res.status(400).send("Tipo de notificación inválido");
    }

    const prioridadesPermitidas = ["high", "normal", "low"];
    const prioridadFinal = prioridadesPermitidas.includes(
      prioridad.toLowerCase(),
    )
      ? prioridad.toLowerCase()
      : "high";

    const mensaje = {
      token: token,
      data: {
        title: String(title),
        body: String(body),
        link: String(link),
        logo: String(logo), // 👈 SE ENVÍA
        image: String(image),
        idTienda: String(idTienda),
        idAnuncio: String(idAnuncio),
        tipo_notificacion: String(tipo_notificacion),
      },
      android: {
        priority: prioridadFinal,
      },
    };

    const respuesta = await admin.messaging().send(mensaje);
    res.status(200).send("Notificación enviada: " + respuesta);
  } catch (error) {
    console.error(error);
    res.status(500).send(error.message);
  }
});

// ==================== Baneo Bot geinz====================
exports.banUser = onRequest(async (req, res) => {
  const userId = req.query.id;

  if (!userId) {
    return res
      .status(400)
      .send("<h1>Error: ID de usuario no proporcionado.</h1>");
  }

  try {
    const db = admin.firestore();

    //NUEVA RUTA CORRECTA
    const userRef = db
      .collection("Trabajadores_Usuarios_Drivers")
      .doc("usuario_bot_geinz")
      .collection("usuario_bot_geinz")
      .doc(userId);

    const doc = await userRef.get();

    if (!doc.exists) {
      return res
        .status(404)
        .send(`<h1>Error: El usuario ${userId} no existe.</h1>`);
    }

    await userRef.update({
      status: "deshabilitado",
      fecha_bloqueo: admin.firestore.FieldValue.serverTimestamp(),
      motivo_bloqueo: "Detección de amenaza - Administrador",
    });

    res.send(`
      <div style="font-family: sans-serif; text-align: center; padding: 40px; background-color: #fce4e4;">
        <h1 style="color: #c62828;">🚫 Geinz: Bloqueo Exitoso</h1>
        <p style="font-size: 1.2em;">El usuario <strong>${userId}</strong> ha sido deshabilitado.</p>
        <p>El Bot ya no responderá a sus mensajes.</p>
        <hr style="border: 1px solid #c62828; width: 50%;">
        <small>Geinz Tecnología E.I.R.L.</small>
      </div>
    `);
  } catch (error) {
    console.error("Error en banUser:", error);
    res.status(500).send("<h1>Error interno al procesar el baneo.</h1>");
  }
});

// ==================== SHARE (Tienda + Turismo + Otros) ====================
exports.share = onRequest(async (req, res) => {
  try {
    const tipo = req.query.t || req.query.tipo;
    const id = req.query.id;
    const localidadRaw = req.query.l || req.query.localidad;
    const categoria = req.query.c || req.query.categoria;
    const indice = req.query.i || req.query.indice;
    const pi = req.query.pi;

    if (!tipo) return res.status(400).send("Falta tipo");

    const MAPA_LOCALIDADES = {
      ba: "barranca",
      par: "paramonga",
      pat: "pativilca",
      su: "supe",
      pue: "puerto supe",
    };

    const mapa_id = {
      nvng: "nuevos_negocios",
      seyt: "servicios_y_tramites",
      lgtr: "lugares_turisticos",
      nemg: "numeros_servicios_publicos",
    };

    const localidad = MAPA_LOCALIDADES[localidadRaw] || localidadRaw;
    const mapa_ids_scren = mapa_id[id] || id;

    const TIPOS_SIN_LOCALIDAD = ["rew", "rewc", "ru", "prf", "prn", "scr", "prms", "in"];
    if (!TIPOS_SIN_LOCALIDAD.includes(tipo) && (!localidad || !categoria)) {
      return res.status(400).send("Faltan parámetros: localidad, categoria.");
    }

    let ref = null;
    if (tipo === "ti" || tipo === "p") {
      ref = admin.firestore().collection("Tiendas").doc(localidad).collection(localidad).doc(id);
    } else if (tipo === "tu") {
      ref = admin.firestore().collection("Tiendas").doc(localidad).collection(categoria).doc(id);
    } else if (tipo === "prms") {
      ref = admin.firestore().collection("Tiendas").doc(localidad).collection("promos_ofertas").doc(pi);
    } else if (tipo === "scr") {
      ref = admin.firestore().collection("share_screen").doc(mapa_ids_scren);
    } else if (tipo === "prn") {
      ref = admin.firestore().collection("Tiendas").doc(localidad).collection(localidad).doc(id).collection("notificaciones_enviadas").doc(pi);
    } else if (tipo === "in") {
      ref = admin.firestore().collection("Tiendas").doc(localidad).collection("geinz_inmobiliaria").doc(id);
    }

    let data = null;
    if (ref) {
      const snap = await ref.get();
      if (snap.exists) data = snap.data();
    }

    let titulo = "Geinz";
    if (data) {
      if (tipo === "ti" || tipo === "p")
        titulo = capitalizeFirstLetter(data.nombre_tienda || "Tienda en Geinz");
      else if (tipo === "tu")
        titulo = capitalizeFirstLetter(data.nombre || "Lugar en Geinz");
      else if (tipo === "prms")
        titulo = capitalizeFirstLetter(data?.informacion?.titulo || "Mira esta promo en Geinz");
      else if (tipo === "scr")
        titulo = capitalizeFirstLetter(data.titulo || "Geinz");
      else if (tipo === "prn")
        titulo = capitalizeFirstLetter(data.datos_de_notificacion?.nombre_tienda || "Geinz");
      else if (tipo === "in")
        titulo = capitalizeFirstLetter(data.nombre || "Geinz");
    }

    let imagen = "https://geinzworkapp.web.app/api/default.jpg";
    if (data) {
      if (tipo === "ti" && data.img_tienda?.logo_tienda) {
        imagen = data.img_tienda.logo_tienda;
      } else if (tipo === "tu" && data.img?.principal) {
        imagen = data.img.principal;
      } else if (tipo === "p") {
        const promos = data.img_tienda?.lista_img?.promociones;
        if (promos && indice && promos[indice]) imagen = promos[indice];
        else if (data.img_tienda?.logo_tienda) imagen = data.img_tienda.logo_tienda;
      } else if (tipo === "prms") {
        const lista = data.img_container?.lista_img || [];
        imagen = lista.length > 0 ? lista[0] : (data.img_container?.logo_img || imagen);
      } else if (tipo === "scr") {
        imagen = data.img || imagen;
      } else if (tipo === "prn") {
        imagen = data?.datos_de_notificacion?.img_notificacion || imagen;
      } else if (tipo === "in") {
        const imgs = data.listaImg || [];
        imagen = imgs.length > 0 ? imgs[0] : imagen;
      }
    }

    const tituloSafe = titulo.replace(/"/g, "&quot;").replace(/</g, "&lt;");
    const imagenSafe = imagen.replace(/"/g, "%22");

    // 🔥 DESTINO FINAL (tu redirect real)
    let destino = `https://geinzworkapp.web.app/redirect/index.html?t=${tipo}`;

    if (id) destino += `&id=${encodeURIComponent(id)}`;
    if (localidad) destino += `&localidad=${encodeURIComponent(localidad)}`;
    if (categoria) destino += `&c=${encodeURIComponent(categoria)}`;
    if (indice) destino += `&i=${encodeURIComponent(indice)}`;
    if (pi) destino += `&pi=${encodeURIComponent(pi)}`;

    const html = `
<!DOCTYPE html>
<html lang="es">
<head>
<meta charset="UTF-8" />
<meta name="viewport" content="width=device-width, initial-scale=1.0">

<title>${tituloSafe}</title>

<meta property="og:title" content="${tituloSafe}" />
<meta property="og:description" content="Encuéntralo en Geinz" />
<meta property="og:image" content="${imagenSafe}" />
<meta property="og:type" content="website" />

<link href="https://fonts.googleapis.com/css2?family=Plus+Jakarta+Sans:wght@400;600;700&display=swap" rel="stylesheet">

<style>
:root {
  --bg:#030008;
  --p:#a855f7;
  --m:#ec4899;
  --text:#f8fafc;
  --muted:#94a3b8;
}

*{margin:0;padding:0;box-sizing:border-box;font-family:'Plus Jakarta Sans',sans-serif;}

body{
  height:100vh;
  display:flex;
  align-items:center;
  justify-content:center;
  background: radial-gradient(circle at 20% 20%, rgba(168,85,247,.15), transparent 40%),
              radial-gradient(circle at 80% 80%, rgba(236,72,153,.15), transparent 40%),
              var(--bg);
  color:var(--text);
}

.card{
  text-align:center;
  padding:40px;
  border-radius:24px;
  background:rgba(15,10,25,.6);
  backdrop-filter:blur(20px);
  border:1px solid rgba(168,85,247,.2);
  box-shadow:0 20px 60px rgba(0,0,0,.6);
}

.loader{
  width:70px;
  height:70px;
  margin:0 auto 20px;
  border-radius:50%;
  border:3px solid rgba(255,255,255,.1);
  border-top:3px solid var(--p);
  animation:spin 1s linear infinite;
}

@keyframes spin{to{transform:rotate(360deg)}}

h1{
  font-size:20px;
  margin-bottom:8px;
}

p{
  color:var(--muted);
  font-size:14px;
}
</style>
</head>

<body>

<div class="card">
  <div class="loader"></div>
  <h1>Abriendo Geinz</h1>
  <p>Redireccionando de forma segura...</p>
</div>

<script>
  setTimeout(() => {
    window.location.replace("${destino}");
  }, 600);
</script>

</body>
</html>
`;

    res.set("Content-Type", "text/html; charset=utf-8");
    res.set("Cache-Control", "no-cache");

    return res.status(200).send(html);

  } catch (e) {
    console.error(e);
    return res.status(500).send("Error interno");
  }
});


function capitalizeFirstLetter(str) {
  if (!str) return "";
  return str.charAt(0).toUpperCase() + str.slice(1);
}

async function recalcularCategoria(db, ciudad, categoria) {
  const snapshot = await db
    .collection("Tiendas")
    .doc(ciudad)
    .collection("promos_ofertas")
    .where("informacion.categoria", "==", categoria)
    .get();

  const tags = new Set();

  snapshot.forEach((doc) => {
    const data = doc.data();

    (data.terminos_clave || [])
      .filter((t) => t)
      .map((t) => t.toLowerCase().trim())
      .forEach((t) => tags.add(t));
  });

  const tagsArray = Array.from(tags);

  const ref = db
    .collection("Tiendas")
    .doc(ciudad)
    .collection("cache_filtrado")
    .doc("filtrado");

  if (tagsArray.length === 0) {
    // 🔥 BORRAR categoría si ya no existe
    await ref.update({
      [categoria]: admin.firestore.FieldValue.delete(),
    });

    console.log(`🗑️ Categoría eliminada del cache: ${categoria}`);
  } else {
    // 🔥 ACTUALIZAR normal
    await ref.set(
      {
        [categoria]: tagsArray,
      },
      { merge: true },
    );

    console.log(`✅ Cache actualizado [${categoria}]:`, tagsArray);
  }
}

exports.onPromocionChange = onDocumentWritten(
  {
    document: "Tiendas/{localidad}/promos_ofertas/{promoId}",
    region: "us-central1",
  },
  async (event) => {
    console.log("🔥 CAMBIO EN PROMO");

    const after = event.data.after?.data();

    if (!after) {
      console.log("🗑️ Eliminado");
      return;
    }

    const categoria = after?.informacion?.categoria;
    const localidad = event.params.localidad;

    if (!categoria) {
      console.log("⏳ Aún no está lista la promo");
      return;
    }

    // 🔥 SOLO recalcular si ya tiene términos clave
    if (!after.terminos_clave || after.terminos_clave.length === 0) {
      console.log("⏳ Aún no hay términos clave");
      return;
    }

    const db = admin.firestore();

    console.log("✅ Recalculando categoría:", categoria);

    await recalcularCategoria(db, localidad, categoria);
  },
);

exports.eliminarPromocionesExpiradasCadaMinuto = onSchedule(
  {
    schedule: "0 0 * * *",
    timeZone: "America/Lima",
    region: "us-central1",
  },
  async () => {
    const db = admin.firestore();
    const ahora = admin.firestore.Timestamp.now();

    console.log("🗑️ Revisando promociones expiradas...");

    const snapshot = await db
      .collection("Tiendas")
      .doc("barranca")
      .collection("promos_ofertas")
      .get();

    if (snapshot.empty) {
      console.log("No hay promociones activas");
      return;
    }

    let eliminadas = 0;
    const categoriasAfectadas = new Set(); // acumula sin duplicados

    for (const doc of snapshot.docs) {
      const data = doc.data();
      const tipo = data?.tipo_hora_dias;
      const fechas = data?.datos_hora_fecha;

      let timestampFin = null;

      if (tipo === "dias") {
        timestampFin = fechas?.dias?.timestamp_fin;
      } else if (tipo === "horas") {
        timestampFin = fechas?.horas?.timestamp_fin;
      }

      if (!timestampFin) continue;

      if (timestampFin.toMillis() <= ahora.toMillis()) {
        const promoId = doc.id;
        const idTienda = data?.informacion?.id_tienda;

        if (!idTienda) continue;

        const destinoRef = db
          .collection("Tiendas")
          .doc("barranca")
          .collection("barranca")
          .doc(idTienda)
          .collection("promociones_geinz")
          .doc(promoId);

        // 1️⃣ mover a historial
        await destinoRef.set({
          ...data,
          estado: "expirada",
          eliminada_en: admin.firestore.FieldValue.serverTimestamp(),
        });

        // 2️⃣ copiar stats
        await copiarSubcolecciones(doc.ref, destinoRef);

        // 3️⃣ borrar stats
        await borrarSubcolecciones(doc.ref);

        // 4️⃣ borrar Firestore activo
        await doc.ref.delete();

        // 5️⃣ borrar en Algolia
        await index_Algolia_promos.deleteObject(promoId);

        // 6️⃣ acumular categoría (NO recalcular aquí todavía)
        const categoria = data?.informacion?.categoria;
        if (categoria) {
          categoriasAfectadas.add(categoria);
        }

        eliminadas++;
        console.log(`🗑️ Promo eliminada: ${promoId}`);
      }
    }

    // 7️⃣ DESPUÉS del loop — recalcular UNA VEZ por categoría única
    if (categoriasAfectadas.size > 0) {
      console.log(`🔄 Recalculando ${categoriasAfectadas.size} categorías...`);

      await Promise.all(
        Array.from(categoriasAfectadas).map((cat) =>
          recalcularCategoria(db, "barranca", cat),
        ),
      );
    }

    console.log(
      `✅ Eliminadas: ${eliminadas} | Categorías recalculadas: ${categoriasAfectadas.size}`,
    );
  },
);

async function copiarSubcolecciones(origenRef, destinoRef) {
  const colecciones = await origenRef.listCollections();

  for (const col of colecciones) {
    const snap = await col.get();

    for (const doc of snap.docs) {
      const nuevoDocRef = destinoRef.collection(col.id).doc(doc.id);
      await nuevoDocRef.set(doc.data());

      // 🔁 copiar sub-subcolecciones
      await copiarSubcolecciones(doc.ref, nuevoDocRef);
    }
  }
}

async function borrarSubcolecciones(docRef) {
  const colecciones = await docRef.listCollections();

  for (const col of colecciones) {
    const snap = await col.get();

    for (const doc of snap.docs) {
      // 🔁 borrar sub-subcolecciones primero
      await borrarSubcolecciones(doc.ref);

      // ❌ borrar documento
      await doc.ref.delete();
    }
  }
}

exports.verificarMinimoSeguidores = onDocumentCreated(
  "Tiendas/{localidad}/{localidad}/{idTienda}/seguidores/{idUsuario}",
  async (event) => {
    const { localidad, idTienda } = event.params;
    const db = admin.firestore();

    const tiendaRef = db
      .collection("Tiendas")
      .doc(localidad)
      .collection(localidad)
      .doc(idTienda);

    try {
      await db.runTransaction(async (tx) => {
        const tiendaDoc = await tx.get(tiendaRef);
        if (!tiendaDoc.exists) return;

        const tiendaData = tiendaDoc.data();

        // 🔕 YA DESBLOQUEADO → SALIR
        if (tiendaData?.notificacionDesbloqueoEnviada === true) {
          console.log("🔕 Notificación ya enviada anteriormente");
          return;
        }

        // 🔢 CONTAR SEGUIDORES
        const seguidoresSnap = await tiendaRef.collection("seguidores").get();

        const totalSeguidores = seguidoresSnap.size;

        console.log(`👥 Seguidores actuales: ${totalSeguidores}`);

        if (totalSeguidores < 10) return;

        // 🔒 MARCAMOS PRIMERO (clave)
        tx.update(tiendaRef, {
          notificacionDesbloqueoEnviada: true,
        });

        const nombre_tienda = tiendaData?.nombre_tienda || "Tu tienda 🎉";

        const propietarios = tiendaData?.propietario_id || [];
        if (propietarios.length === 0) return;

        // 🔔 ENVIAR NOTIFICACIONES
        for (const propietarioId of propietarios) {
          const tokenDoc = await db
            .collection("Trabajadores_Usuarios_Drivers")
            .doc("users")
            .collection("tokens")
            .doc(propietarioId)
            .get();

          if (!tokenDoc.exists) continue;

          const tokens = Object.values(tokenDoc.data()?.tokens || {});
          for (const token of tokens) {
            await enviarNotificacionFCM_tienda({
              token,
              title: `🎉 ¡Felicidades ${nombre_tienda}!`,
              body: `¡Alcanzaste ${totalSeguidores} seguidores! Ya puedes enviar notificaciones a tus seguidores 📢`,
              idTienda,
              tipo_notificacion: "logo",
              prioridad: "high",
            });
          }
        }
      });

      return null;
    } catch (error) {
      console.error("❌ ERROR verificarMinimoSeguidores:", error);
      return null;
    }
  },
);

exports.alertaSaldoBajo = onDocumentWritten(
  "Tiendas/{localidad}/{localidad}/{id_tienda}",
  async (event) => {
    try {
      console.log("===== TRIGGER alertaSaldoBajo =====");
      console.log("Params recibidos:", event.params);

      const beforeData = event.data?.before?.data() || {};
      const afterData = event.data?.after?.data() || {};
      console.log("Datos ANTES del cambio:", beforeData);
      console.log("Datos DESPUÉS del cambio:", afterData);

      const saldoAntes = Number(beforeData.puntos_tienda || 0);
      const saldoDespues = Number(afterData.puntos_tienda || 0);
      const notiEnviada = afterData.ultima_notificacion_enviada || false;
      const propietarios = afterData.propietario_id || [];
      const idTienda = event.params.id_tienda;

      console.log("Saldo antes:", saldoAntes);
      console.log("Saldo después:", saldoDespues);
      console.log("ultima_notificacion_enviada:", notiEnviada);
      console.log("Propietarios:", propietarios);

      // 🔹 Reiniciar flag si el saldo sube por encima del umbral
      if (saldoDespues >= 50 && notiEnviada) {
        await event.data.after.ref.update({
          ultima_notificacion_enviada: false,
        });
        console.log(
          "✅ Flag de notificación reiniciado porque saldo subió >=50",
        );
      }

      // 🔹 Enviar notificación solo si el saldo baja de 50 y antes estaba >=50
      if (saldoDespues < 50 && saldoAntes >= 50 && !notiEnviada) {
        console.log("⚠️ Saldo bajo detectado, enviando notificaciones...");

        for (const propietarioId of propietarios) {
          const tokenDoc = await admin
            .firestore()
            .collection("Trabajadores_Usuarios_Drivers")
            .doc("users")
            .collection("tokens")
            .doc(propietarioId)
            .get();

          if (!tokenDoc.exists) {
            console.log(
              `No se encontró doc de tokens para propietario ${propietarioId}`,
            );
            continue;
          }

          const tokensMap = tokenDoc.data()?.tokens || {};
          const tokens = Object.values(tokensMap);

          if (tokens.length === 0) continue;

          for (const token of tokens) {
            await enviarNotificacionFCM_tienda({
              token,
              title: `⚠️ ¡Tu saldo está bajo!`,
              body: `Tu tienda tiene menos de 50 creaditos. Mantén tu alcance y visibilidad activo recargando cuando puedas 💼✨`,
              link: "https://geinzworkapp.web.app/api/api/share?t=scr&id=rec",
              logo: "https://firebasestorage.googleapis.com/v0/b/geinzworkapp.appspot.com/o/logo_geinz_webp.webp?alt=media&token=aa1ef1df-1bcd-48f2-9cad-a85929c3a8d0",
              idTienda,
              idAnuncio: "",
              tipo_notificacion: "logo",
              prioridad: "high",
            });
          }
        }

        // 🔹 Marcar que ya se envió la notificación
        await event.data.after.ref.update({
          ultima_notificacion_enviada: true,
        });
        console.log("✅ Notificación enviada y flag actualizado");
      } else {
        console.log(
          "No se cumple condición de saldo bajo o ya se envió notificación",
        );
      }
    } catch (error) {
      console.error("ERROR alertaSaldoBajo:", error);
    }
  },
);

exports.resetearEstadoNotificacionesYPanel = onSchedule(
  {
    schedule: "0 0 * * *", // Cada minuto
    timeZone: "America/Lima",
  },
  async () => {
    try {
      const ahora = admin.firestore.Timestamp.now();
      logger.info("⏱ Ejecutando resetearEstadoNotificacionesYPanel", {
        ahora: ahora.toDate().toISOString(),
      });

      const snapshot = await admin
        .firestore()
        .collectionGroup("tiendas_servicios_geinz_activos")
        .get();

      if (snapshot.empty) {
        logger.info("✅ No hay documentos para verificar");
        return;
      }

      const batch = admin.firestore().batch();
      const propietariosSet = new Map(); // propietarioId => Set("notificaciones"|"panel")
      let huboReseteo = false;

      snapshot.docs.forEach((doc) => {
        const data = doc.data();
        const propietarios = data.propietario_id || [];

        // --- NOTIFICACIONES (resetear si vencidas) ---
        const noti = data.notificaciones;
        if (
          noti?.timestamp_fin &&
          noti.timestamp_fin.toMillis() <= ahora.toMillis()
        ) {
          batch.update(doc.ref, {
            "notificaciones.contador": 0,
            "notificaciones.fecha_inicio": "",
            "notificaciones.fecha_fin": "",
            "notificaciones.promocion_nueva": false,
            "notificaciones.ultima_notificacion_enviada": false,
            "notificaciones.timestamp_fin": admin.firestore.FieldValue.delete(),
          });
          propietarios.forEach((p) => {
            if (!propietariosSet.has(p)) propietariosSet.set(p, new Set());
            propietariosSet.get(p).add("notificaciones");
          });
          huboReseteo = true;
        }

        // --- PANEL ADMIN (solo notificar si vencido) ---
        const panel = data.panel_admin;
        if (
          panel?.timestamp_fin &&
          panel.timestamp_fin.toMillis() <= ahora.toMillis()
        ) {
          propietarios.forEach((p) => {
            if (!propietariosSet.has(p)) propietariosSet.set(p, new Set());
            propietariosSet.get(p).add("panel");
          });
          huboReseteo = true; // marcar que hay algo que notificar
        }
      });

      if (!huboReseteo) return;
      await batch.commit();
      logger.info("♻️ Reseteo de notificaciones completado y panel revisado");

      // --- Enviar notificaciones ---
      for (const [propietarioId, tipos] of propietariosSet) {
        const tokenDoc = await admin
          .firestore()
          .collection("Trabajadores_Usuarios_Drivers")
          .doc("users")
          .collection("tokens")
          .doc(propietarioId)
          .get();

        if (!tokenDoc.exists) continue;
        const tokensMap = tokenDoc.data()?.tokens || {};
        const tokens = Object.values(tokensMap);
        if (tokens.length === 0) continue;

        for (const token of tokens) {
          if (tipos.has("notificaciones")) {
            await enviarNotificacionFCM_tienda({
              token,
              title: "♻️ ¡Tus notificaciones fueron renovadas!",
              body: "✨ Ya puedes enviar nuevas notificaciones y mantener a tus clientes al día 🔔🚀",
              link: "https://geinzworkapp.web.app/api/api/share?t=scr&id=ads",
              logo: "https://firebasestorage.googleapis.com/v0/b/geinzworkapp.appspot.com/o/logo_geinz_webp.webp?alt=media&token=aa1ef1df-1bcd-48f2-9cad-a85929c3a8d0",
              idTienda: "",
              idAnuncio: "",
              tipo_notificacion: "logo",
              prioridad: "high",
            });
          }

          if (tipos.has("panel")) {
            await enviarNotificacionFCM_tienda({
              token,
              title: "⏰ Tu panel de Geinz ya vencio  😣",
              body: "⚡ Renueva tu panel para seguir teniendo control de tu negocio en tiempo real desde Geinz📈💼",
              link: "https://geinzworkapp.web.app/api/api/share?t=scr&id=pnl",
              logo: "https://firebasestorage.googleapis.com/v0/b/geinzworkapp.appspot.com/o/logo_geinz_webp.webp?alt=media&token=aa1ef1df-1bcd-48f2-9cad-a85929c3a8d0",
              idTienda: "",
              idAnuncio: "",
              tipo_notificacion: "logo",
              prioridad: "high",
            });
          }
        }
      }
    } catch (error) {
      logger.error("❌ Error en resetearEstadoNotificacionesYPanel", error);
    }
  },
);

async function enviarNotificacionFCM_tienda({
  token,
  title,
  body,
  link = "https://geinzworkapp.web.app/api/api/share?t=scr&id=ads",
  logo = "https://firebasestorage.googleapis.com/v0/b/geinzworkapp.appspot.com/o/logo_geinz_webp.webp?alt=media&token=aa1ef1df-1bcd-48f2-9cad-a85929c3a8d0",
  image = "",
  idTienda,
  idAnuncio = "", // ✅ agregar
  tipo_notificacion,
  prioridad = "high",
}) {
  try {
    const mensaje = {
      token: token,
      data: {
        title: String(title),
        body: String(body),
        link: String(link),
        logo: String(logo),
        image: String(image),
        idTienda: String(idTienda),
        idAnuncio: String(idAnuncio),
        tipo_notificacion: String(tipo_notificacion),
      },
      android: { priority: prioridad },
    };
    const respuesta = await admin.messaging().send(mensaje);
    console.log("Notificación enviada al token:", token);
    return respuesta;
  } catch (error) {
    console.error("ERROR enviarNotificacionFCM:", error);
    if (error.code === "messaging/registration-token-not-registered") {
      console.log("Token inválido, debería eliminarlo de Firestore:", token);
    }
  }
}

exports.recognizeSpeech = onRequest(
  { region: "us-central1" },
  async (req, res) => {
    if (req.method !== "POST") {
      return res.status(405).send("Método no permitido xd");
    }

    try {
      const audioBytes = req.body.audio; // base64
      if (!audioBytes) return res.status(400).send("Falta el audio");

      const audio = { content: audioBytes };

      const config = {
        encoding: "LINEAR16",
        sampleRateHertz: 16000,
        languageCode: "es-PE",
        useEnhanced: true,
        model: "default",
        speechContexts: [
          { phrases: ["creatina", "chifa", "pollo a la brasa"] },
        ],
      };

      const request = { audio, config };
      const [response] = await client_specth.recognize(request);

      const transcription = response.results
        .map((result) => result.alternatives[0].transcript)
        .join(" ");

      res.json({ text: transcription });
    } catch (err) {
      console.error("Error recognizeSpeech:", err);
      res.status(500).send(err.message);
    }
  },
);

exports.textToSpeechIA = onRequest(
  { region: "us-central1" },
  async (req, res) => {
    try {
      if (req.method !== "POST") {
        return res.status(405).send("Método no permitido");
      }

      const { texto } = req.body;
      if (!texto) return res.status(400).send("Falta texto");

      // 🔹 Limpiar texto (quitar emojis y símbolos raros)
      const textoLimpio = String(texto)
        .replace(/[\u{1F600}-\u{1F6FF}]/gu, "")
        .replace(/[\u{2700}-\u{27BF}]/gu, "");

      if (textoLimpio.length === 0)
        return res.status(400).send("Texto inválido");

      // 🔹 Dividir texto en trozos de 5000 caracteres (limite API)
      const CHUNK_SIZE = 4500; // un poco menos de 5000 para seguridad
      const chunks = [];
      for (let i = 0; i < textoLimpio.length; i += CHUNK_SIZE) {
        chunks.push(textoLimpio.slice(i, i + CHUNK_SIZE));
      }

      // 🔹 Generar audio para cada trozo y concatenar
      let audioFinal = Buffer.alloc(0);

      for (const chunk of chunks) {
        const request = {
          input: { text: chunk },
          voice: {
            languageCode: "es-US",
            name: "es-US-Standard-B", // rápido y natural
          },
          audioConfig: {
            audioEncoding: "MP3",
            speakingRate: 1.05,
          },
        };

        const [response] = await ttsClient.synthesizeSpeech(request);
        audioFinal = Buffer.concat([audioFinal, response.audioContent]);
      }

      res.set("Content-Type", "audio/mpeg");
      res.send(audioFinal);
    } catch (error) {
      console.error("❌ Error TTS:", error.code, error.message);
      res.status(500).send(error.message);
    }
  },
);

exports.textToSpeechIA_con_params = onRequest(
  { region: "us-central1" },
  async (req, res) => {
    try {
      if (req.method !== "POST") {
        return res.status(405).send("Método no permitido");
      }

      const { texto, voz } = req.body;

      if (!texto) return res.status(400).send("Falta texto");

      // voz por defecto si no envían nada
      const voiceName = voz || "es-US-News-F";

      const textoLimpio = String(texto)
        .replace(/[\u{1F600}-\u{1F6FF}]/gu, "")
        .replace(/[\u{2700}-\u{27BF}]/gu, "");

      if (textoLimpio.length === 0)
        return res.status(400).send("Texto inválido");

      const request = {
        input: { text: textoLimpio },
        voice: {
          languageCode: "es-US",
          name: voiceName,
        },
        audioConfig: {
          audioEncoding: "MP3",
          speakingRate: 1.05,
        },
      };

      const [response] = await ttsClient.synthesizeSpeech(request);

      res.set("Content-Type", "audio/mpeg");
      res.send(response.audioContent);
    } catch (error) {
      console.error("❌ Error TTS:", error);
      res.status(500).send(error.message);
    }
  },
);

exports.tiendasGeo = onRequest(async (req, res) => {
  try {
    const lat = parseFloat(req.query.lat);
    const lng = parseFloat(req.query.lng);
    const radioKm = 0.5;

    if (isNaN(lat) || isNaN(lng)) {
      return res.status(400).json({ error: "Lat/Lng inválidos" });
    }

    const db = admin.firestore();
    const center = [lat, lng];
    const bounds = geofire.geohashQueryBounds(center, radioKm * 1000);

    // Definimos las 3 colecciones que queremos consultar
    const configuracion = {
      turismo: db
        .collection("Tiendas")
        .doc("barranca")
        .collection("lugares_turisticos"),
      seguridad: db
        .collection("Tiendas")
        .doc("salud_seguridad")
        .collection("barranca"),
      cercanos: db.collection("Tiendas").doc("barranca").collection("barranca"),
    };

    const respuestaFinal = {};

    // Ejecutamos la búsqueda para cada categoría
    for (const [categoria, collectionRef] of Object.entries(configuracion)) {
      const promises = bounds.map((b) => {
        return collectionRef
          .orderBy("geohash")
          .startAt(b[0])
          .endAt(b[1])
          .limit(10)
          .get();
      });

      const snapshots = await Promise.all(promises);
      const resultados = [];
      const vistos = new Set();

      for (const snap of snapshots) {
        for (const doc of snap.docs) {
          if (vistos.has(doc.id)) continue;
          vistos.add(doc.id);

          const data = doc.data();
          const latTienda = data?.ubicacion?.latitud;
          const lngTienda = data?.ubicacion?.longitud;

          if (!latTienda || !lngTienda) continue;

          const distancia = geofire.distanceBetween(
            [lat, lng],
            [latTienda, lngTienda],
          );
          if (distancia > radioKm) continue;

          resultados.push({
            nombre: data.nombre || data.nombre_tienda || data.titulo,
          });
        }
      }

      // Si hay resultados en esta categoría, los agregamos
      if (resultados.length > 0) {
        // Mezclamos y limitamos a 4 por categoría para no saturar el prompt
        respuestaFinal[categoria] = resultados
          .sort(() => 0.5 - Math.random())
          .slice(0, 4);
      }
    }

    return res.json({
      coordenadas_busqueda: { lat, lng },
      datos_entorno: respuestaFinal,
    });
  } catch (error) {
    console.error("💥 ERROR GEO UNIFICADO:", error);
    return res.status(500).json({ error: "Error en consulta unificada" });
  }
});

function normalizar(texto) {
  return texto
    .toLowerCase()
    .normalize("NFD")
    .replace(/[\u0300-\u036f]/g, "")
    .replace(/[^\w\s]/gi, "")
    .trim();
}
