const { onRequest, onCall } = require("firebase-functions/v2/https");
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

const axios = require("axios");

const CULQI_KEY = process.env.CULQI_KEY; // 🔹 v2: se usa env variable

const db = admin.firestore();
exports.crearOrdenCulqi = onCall({ region: "us-central1" }, async (req) => {
  const { monto, userId, monedas, nombre, email, localidad } = req.data;

  console.log("=== PARAMETROS RECIBIDOS ===");
  console.log("monto:", monto);
  console.log("userId:", userId);
  console.log("monedas:", monedas);
  console.log("nombre:", nombre);
  console.log("email:", email);
  console.log("localidad:", localidad);
  console.log("===========================");

 const montoInt = parseInt(monto) * 100;
  const orderId = "order_" + Date.now();

  try {
    const response = await axios.post(
      "https://api.culqi.com/v2/orders",
      {
        amount: montoInt,
        currency_code: "PEN",
        description: "Compra de monedas",
        order_number: orderId,
        expiration_date: Math.floor(Date.now() / 1000) + 24 * 60 * 60,
        client_details: {
          first_name: nombre || "Cliente",
          last_name: "Geinz",
          email: email || "cliente@geinz.com",
          phone_number: "999999999",
        },
      },
      {
        headers: {
          Authorization: `Bearer ${CULQI_KEY}`,
          "Content-Type": "application/json",
        },
      },
    );

    await db
      .collection("ordenes_pagos")
      .doc(orderId)
      .set({
        orderId: orderId,
        userId: userId,
        monedas: monedas,
        monto: parseInt(monto), // en soles (no en centavos)
        estado: "pendiente",
        localidad: localidad,
        culqi_order_id: response.data.id, // id interno de Culqi
        createdAt: admin.firestore.FieldValue.serverTimestamp(),

        paidAt: null,
      });

    console.log("Respuesta Culqi COMPLETA:", JSON.stringify(response.data));
    console.log("CULQUI KEY", CULQI_KEY);
    console.log("qr value:", response.data.qr);
    console.log("url_pe value:", response.data.url_pe);
    console.log("Todas las keys:", Object.keys(response.data));

    return {
      checkout_url: response.data.url_pe, // ✅ página de pago
      qr_url: response.data.qr, // ✅ imagen del QR (bonus)
      orderId,
    };
  } catch (error) {
    console.error(
      "Error crearOrdenCulqi:",
      error.response?.data || error.message,
    );
    throw new Error(JSON.stringify(error.response?.data || error.message));
  }
});

exports.confirmarPago = onCall(async (req) => {

  const { token, monto, email } = req.data;

  console.log("TOKEN:", token);
  console.log("MONTO:", monto);

  try {
    const response = await axios.post(
      "https://api.culqi.com/v2/charges",
      {
        amount: Math.round(monto * 100),
        currency_code: "PEN",
        email: email || "test@test.com",
        source_id: token
      },
      {
        headers: {
          Authorization: `Bearer ${CULQI_KEY}`,
          "Content-Type": "application/json"
        }
      }
    );

    console.log("CULQI RESPONSE:", response.data);

    return {
      ok: true,
      data: response.data
    };

  } catch (error) {
    console.error("ERROR CHARGE:", error.response?.data || error.message);

    throw new Error(JSON.stringify(error.response?.data || error.message));
  }
});
// ==================== Algolia ====================
const APP_ID = process.env.ALGOLIA_APP_ID || "";
const API_KEY = process.env.ALGOLIA_API_KEY || "";
const client = algoliasearch(APP_ID, API_KEY);
const index = client.initIndex("lugares");

// 🔀 Shuffle correcto (Fisher-Yates)
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

    // 🔥 match exacto completo
    if (nombreDB.includes(buscado)) score += 3;

    // 🔥 coincidencias por palabra
    let matches = 0;
    palabras.forEach((p) => {
      if (nombreDB.includes(p)) matches++;
    });
    score += matches;

    // 🔥 bonus si contiene TODAS
    if (palabras.every((p) => nombreDB.includes(p))) {
      score += 2;
    }

    // 🔥 similitud general
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

    // 🔥 DECISIÓN
    if (modoDifuso) {
      resultados = await buscarInteligente(ref, nombre_negocio);
    } else {
      resultados = await buscarRapido(ref, nombre_negocio);

      if (resultados.length === 0) {
        resultados = await buscarInteligente(ref, nombre_negocio);
      }
    }

    console.log("📊 RESULTADOS:", resultados.length);

    // 🧠 ordenar
    let ordenados = rankear(resultados, nombre_negocio);

    // 🕒 horario
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

    // 🟢 abiertos primero
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
    // 🔍 FILTRO NOMBRE
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
    // 🏷 FILTRO CATEGORIA (FLEXIBLE)
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
    // 🍕 FILTRO SUBCATEGORIA
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
    // 🕒 HORARIO PERÚ
    // =========================
    const now = new Date();
    const peru = new Date(
      now.toLocaleString("en-US", { timeZone: "America/Lima" }),
    );

    const dias = [
      "domingo",
      "lunes",
      "martes",
      "miércoles", // sin tilde 🔥
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
    // 🔥 PRIORIDAD: ABIERTOS
    // =========================
    let abiertos = response.filter((t) => t.open_state);

    console.log("🟢 ABIERTOS:", abiertos.length);

    // 🔥 fallback si no hay abiertos
    let baseFinal = abiertos.length > 0 ? abiertos : response;

    // =========================
    // 🎯 RANDOM + MAX 3
    // =========================
    const final = shuffle(baseFinal).slice(0, 3);

    return res.json({
      ok: true,
      total: final.length,
      data: final,
    });
  } catch (error) {
    console.error("❌ ERROR:", error);

    return res.status(500).json({
      ok: false,
      error: error.message,
    });
  }
});

exports.buscar_tienda_por_categorias_y_subcategoria = onRequest(
  async (req, res) => {
    try {
      const { localidad, categoria, subcategoria } = {
        localidad: limpiar(req.body.localidad),
        categoria: limpiar(req.body.categoria),
        subcategoria: req.body.subcategoria
          ? limpiar(req.body.subcategoria)
          : null,
      };
      if (!localidad || !categoria) {
        return res.status(400).json({
          ok: false,
          error: "localidad y categoria son obligatorias",
        });
      }

      let query = admin
        .firestore()
        .collection("Tiendas")
        .doc(localidad)
        .collection(localidad)
        .where("categoria_tienda", "==", categoria);

      // 👉 subcategoria opcional
      if (subcategoria) {
        query = query.where("subcategoria", "array-contains", subcategoria);
      }

      const snapshot = await query.get();

      let resultados = snapshot.docs.map((doc) => ({
        id: doc.id,
        ...doc.data(),
      }));

      console.log("📊 RESULTADOS:", resultados.length);

      // =========================
      // 🔥 MAP + HORARIO
      // =========================
      const response = resultados.map((tienda) => {
        const estado = verificar_apertura_tienda(tienda.horario_atencion);

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

      // =========================
      // 🟢 PRIORIDAD ABIERTOS
      // =========================
      const abiertos = response.filter((t) => t.open_state === true);

      const baseFinal = abiertos.length > 0 ? abiertos : response;

      // =========================
      // 🎯 RANDOM + LIMITE
      // =========================
      const final = baseFinal.sort(() => Math.random() - 0.5).slice(0, 3);

      return res.json({
        ok: true,
        hayAbiertos: abiertos.length > 0,
        total: final.length,
        data: final,
      });
    } catch (error) {
      console.error("❌ ERROR:", error);
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

    // 🔍 Validación básica
    if (!numero_user) {
      return res.status(400).json({
        ok: false,
        msg: "El número de usuario es obligatorio"
      });
    }

    // 📍 Ruta: /Trabajadores_Usuarios_Drivers/usuario_bot_geinz/usuario_bot_geinz/{numero_user}
    const ref = admin
      .firestore()
      .collection("Trabajadores_Usuarios_Drivers")
      .doc("usuario_bot_geinz")
      .collection("usuario_bot_geinz")
      .doc(numero_user);

    // 📦 Datos
    const data = {
      nombre_user: nombre_user || null,
      id_user: id_user || null,
      numero_user,
      from_user_id: from_user_id || null,
      fecha_registro: admin.firestore.FieldValue.serverTimestamp()
    };

    await ref.set(data, { merge: true });

    return res.json({
      ok: true,
      msg: "Usuario guardado correctamente"
    });

  } catch (error) {
    console.error(error);
    return res.status(500).json({
      ok: false,
      msg: "Error al guardar usuario"
    });
  }
});

function verificar_apertura_tienda(horario_atencion) {
  // 👉 si no hay datos
  if (!horario_atencion) return null;

  const now = new Date();

  // 🔥 hora Perú
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

  // 👉 si no hay horario ese día
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

    // 🔥 CASO NORMAL (ej: 9:00 - 18:00)
    if (apertura <= cierre) {
      if (minutosActual >= apertura && minutosActual <= cierre) {
        return true;
      }
    }
    // 🔥 CASO CRUZADO (ej: 20:00 - 02:00)
    else {
      if (minutosActual >= apertura || minutosActual <= cierre) {
        return true;
      }
    }
  }

  // 👉 no está dentro de ningún bloque
  return false;
}

exports.obtenerCategorias = onRequest(async (req, res) => {
  try {
    const snapshot = await admin
      .firestore()
      .collection("Tiendas")
      .doc("categorias")
      .collection("categorias")
      .select() // 👈 solo metadata ligera
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
      error: "Error interno"
    });
  }
});

exports.obtener_subcategoira_de_cat = onRequest(async (req, res) => {
  try {
    const categoria = (req.body?.categoria || "").trim().toLowerCase();

    if (!categoria) {
      return res.status(400).json({
        ok: false,
        error: "Categoría inválida"
      });
    }

    const snap = await admin
      .firestore()
      .doc(`Tiendas/categorias/categorias/${categoria}`)
      .get();

    if (!snap.exists) {
      return res.json({
        ok: true,
        data: []
      });
    }

    return res.json({
      ok: true,
      data: snap.get("subcategorias") ?? []
    });

  } catch (error) {
    return res.status(500).json({
      ok: false,
      error: "Error interno"
    });
  }
});

exports.obtener_lugares_seguros = onRequest(async (req, res) => {
  try {
    // 1. Recibimos localidad y la categoria_limpia del clasificador
    const { localidad, categoria } = req.body;

    let query = admin
      .firestore()
      .collection("Tiendas")
      .doc("salud_seguridad")
      .collection(localidad);

    // 2. Aplicamos el WHERE solo si la categoría no es "general"
    // Si es "general", traerá todos los contactos de la localidad
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

    // Cache de 5 minutos para ahorrar lecturas de Firestore
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
    // suma total de scores
    const totalScore = copia.reduce(
      (sum, item) => sum + (item.score > 0 ? item.score : 1),
      0,
    );
    let r = Math.random() * totalScore;

    for (let i = 0; i < copia.length; i++) {
      r -= copia[i].score || 1;

      if (r <= 0) {
        seleccionados.push(copia[i]);
        copia.splice(i, 1); // evitar repetidos
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
    // 🧠 FUNCIONES BASE
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
    // 🔍 FILTRO DINÁMICO
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
    // ⚠️ FALLBACK
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
    // 🔥 SCORE + RANDOM
    // =========================
    const LIMITE = 4;

    filtrados = filtrados.map((lugar) => ({
      ...lugar,
      score: scoreLugar(lugar, nombre_negocio, subcategoria), // 👈 usa tu función externa
    }));

    filtrados = seleccionarAleatorioPonderado(filtrados, LIMITE);

    // =========================
    // 🔥 RESPUESTA FINAL
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
    console.error("❌ ERROR:", error);
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

      console.log("🔥 WEBHOOK RECIBIDO:");
      console.log("Body:", JSON.stringify(event));

      if (!event || !event.type) {
        console.log("⚠️ Evento vacío o inválido");
        return res.status(200).send("ok");
      }

      if (event.type === "order.status_changed") {
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

    // 🔥 NUEVA RUTA CORRECTA
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
    // ============================
    //        PARÁMETROS
    // ============================
    const tipo = req.query.t || req.query.tipo; // SIEMPRE CORTO
    const id = req.query.id;
    const localidadRaw = req.query.l || req.query.localidad;
    const categoria = req.query.c || req.query.categoria;
    const indice = req.query.i || req.query.indice;
    const id_promo_compartida = req.query.pi;

    const mapa_id = {
      nvng: "nuevos_negocios",
      seyt: "servicios_y_tramites",
      lgtr: "lugares_turisticos",
      nemg: "numeros_servicios_publicos",
    };

    const coll_completa = tipo === "prms" ? "promos_ofertas" : "promo";
    // const coll_completa_datos_promos_intern= "prn"
    // ============================
    //        MAPA LOCALIDADES
    // ============================
    const MAPA_LOCALIDADES = {
      ba: "barranca",
      par: "paramonga",
      pat: "pativilca",
      su: "supe",
      pue: "puerto supe",
    };

    const localidad = MAPA_LOCALIDADES[localidadRaw] || localidadRaw;
    const mapa_ids_scren = mapa_id[id] || id;

    // ============================
    //        VALIDACIÓN BASE
    // ============================
    if (!tipo) {
      return res.status(400).send("Faltan parámetros obligatorios: tipo");
    }

    // ============================
    //   TIPOS QUE NO USAN LOCALIDAD
    // ============================
    const TIPOS_SIN_LOCALIDAD = [
      "rew",
      "rewc",
      "ru",
      "prf",
      "prn",
      "scr",
      "prms",
      "in",
    ];

    if (!TIPOS_SIN_LOCALIDAD.includes(tipo) && (!localidad || !categoria)) {
      return res.status(400).send("Faltan parámetros: localidad, categoria.");
    }

    let ref = null;
    let data = null;

    // ============================
    //     SELECCIÓN FIRESTORE
    // ============================
    if (tipo === "ti" || tipo === "p") {
      ref = admin
        .firestore()
        .collection("Tiendas")
        .doc(localidad)
        .collection(localidad)
        .doc(id);
    } else if (tipo === "tu") {
      ref = admin
        .firestore()
        .collection("Tiendas")
        .doc(localidad)
        .collection(categoria)
        .doc(id);
    } else if (tipo === "prms") {
      ref = admin
        .firestore()
        .collection("Tiendas")
        .doc(localidad)
        .collection(coll_completa)
        .doc(id_promo_compartida);
    } else if (tipo === "scr") {
      ref = admin.firestore().collection("share_screen").doc(mapa_ids_scren);
    } else if (tipo == "prn") {
      ref = admin
        .firestore()
        .collection("Tiendas")
        .doc(localidad)
        .collection(localidad)
        .doc(id)
        .collection("notificaciones_enviadas")
        .doc(id_promo_compartida);
    } else if (tipo == "in") {
      ref = admin
        .firestore()
        .collection("Tiendas")
        .doc(localidad)
        .collection("geinz_inmobiliaria")
        .doc(id);
    }
    // rew | rewc | ru | prf → NO FIRESTORE

    if (ref) {
      const snap = await ref.get();
      if (!snap.exists) {
        return res.status(404).send("No existe el documento solicitado.");
      }
      data = snap.data();
    }

    // ============================
    //            TÍTULO
    // ============================
    let titulo = "Geinz";

    if (data) {
      if (tipo === "ti" || tipo === "p") {
        titulo = capitalizeFirstLetter(data.nombre_tienda || "Tienda en Geinz");
      } else if (tipo === "tu") {
        titulo = capitalizeFirstLetter(data.nombre || "Lugar en Geinz");
      } else if (tipo === "prms") {
        titulo = capitalizeFirstLetter(
          data?.informacion?.titulo || "Mira esta promo en Geinz",
        );
      } else if (tipo === "scr") {
        titulo = capitalizeFirstLetter(data.titulo || "Geinz");
      } else if (tipo == "prn") {
        titulo = capitalizeFirstLetter(
          data.datos_de_notificacion.nombre_tienda || "Geinz",
        );
      } else if (tipo == "in") {
        titulo = capitalizeFirstLetter(data.nombre || "Geinz");
      }
    }

    // ============================
    //            IMAGEN
    // ============================
    let imagen = "https://geinzworkapp.web.app/default.jpg";

    if (data) {
      if (tipo === "ti" && data.img_tienda?.logo_tienda) {
        imagen = data.img_tienda.logo_tienda;
      } else if (tipo === "tu" && data.img?.principal) {
        imagen = data.img.principal;
      } else if (tipo === "p") {
        const promos = data.img_tienda?.lista_img?.promociones;
        const idImagen = req.query.i || req.query.indice; // string
        if (promos && idImagen) {
          imagen = promos[idImagen]; // obtiene la URL correcta
          if (!imagen && data.img_tienda?.logo_tienda) {
            imagen = data.img_tienda.logo_tienda; // fallback
          }
        } else if (data.img_tienda?.logo_tienda) {
          imagen = data.img_tienda.logo_tienda;
        }
      } else if (tipo === "prms") {
        const promos = data.img_container?.lista_img || [];
        if (promos.length > 0) {
          imagen = promos[0]; // toma siempre la primera imagen si existe
        } else if (data.img_container?.logo_img) {
          imagen = data.img_container.logo_img;
        } else {
          imagen = "https://geinzworkapp.web.app/default.jpg";
        }
      } else if (tipo === "scr") {
        if (data.img) {
          imagen = data.img;
        } else {
          imagen = "https://geinzworkapp.web.app/default.jpg";
        }
      } else if (tipo == "prn") {
        imagen =
          data?.datos_de_notificacion?.img_notificacion ||
          "https://geinzworkapp.web.app/default.jpg";
      } else if (tipo == "in") {
        const promos = data.listaImg || [];
        if (promos.length > 0) {
          imagen = promos[0]; // toma siempre la primera imagen si existe
        } else {
          imagen = "https://geinzworkapp.web.app/default.jpg";
        }
      }
    }

    // ============================
    //        URL DESTINO
    // ============================
    let destino = `https://geinzworkapp.web.app/${tipo}?id=${id}`;

    if (localidad) destino += `&localidad=${localidad}`;
    if (categoria) destino += `&categoria=${categoria}`;
    if (tipo === "p" && indice) {
      destino += `&indice=${indice}`;
    }

    // ============================
    //        HTML + META
    // ============================
    const html = `
      <html>
        <head>
          <meta property="og:title" content="${titulo}" />
          <meta property="og:image" content="${imagen}" />
          <meta property="og:image:width" content="1200" />
          <meta property="og:image:height" content="630" />
          <meta property="og:description" content="Encuéntralo en Geinz" />
          <meta property="og:type" content="website" />
        </head>
        <body>
          <script>
            window.location.href = "${destino}";
          </script>
        </body>
      </html>
    `;

    res.set("Content-Type", "text/html");
    res.status(200).send(html);
  } catch (e) {
    console.error("ERROR SHARE:", e);
    res.status(500).send("Error interno");
  }
});

function capitalizeFirstLetter(str) {
  if (!str) return "";
  return str.charAt(0).toUpperCase() + str.slice(1);
}

exports.eliminarPromocionesExpiradasCadaMinuto = onSchedule(
  {
    schedule: "0 0 * * *", // ⏱️ cada minuto
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
      console.log("ℹ️ No hay promociones activas");
      return;
    }

    let eliminadas = 0;

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

        // 1️⃣ Copiar promo
        await destinoRef.set({
          ...data,
          estado: "expirada",
          eliminada_en: admin.firestore.FieldValue.serverTimestamp(),
        });

        // 2️⃣ Copiar estadísticas
        await copiarSubcolecciones(doc.ref, destinoRef);

        // 🆕 3️⃣ BORRAR estadísticas originales
        await borrarSubcolecciones(doc.ref);

        // 4️⃣ Eliminar promo activa
        await doc.ref.delete();

        eliminadas++;
        console.log(`🗑️ Promo procesada: ${promoId}`);
      }
    }

    console.log(`✅ Total promociones procesadas: ${eliminadas}`);
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
              body: `¡Alcanzaste ${totalSeguidores} seguidores! Ya puedes enviar promociones 📢`,
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
              body: `Tu tienda tiene menos de 50 monedas. Mantén tu alcance y visibilidad activo recargando cuando puedas 💼✨`,
              link: "https://geinzworkapp.web.app/share?t=scr&id=rec",
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
              link: "https://geinzworkapp.web.app/share?t=scr&id=ads",
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
              title: "⏰ Tu panel vencio hoy 😣",
              body: "⚡ Renueva tu panel para seguir teniendo control de tu negocio 📈💼",
              link: "https://geinzworkapp.web.app/share?t=scr&id=pnl",
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
  link = "https://geinzworkapp.web.app/share?t=scr&id=ads",
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
    .normalize("NFD") // separa tildes
    .replace(/[\u0300-\u036f]/g, "") // elimina tildes
    .replace(/[^\w\s]/gi, "") // elimina puntos, comas, etc
    .trim();
}

/*

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
      image = "",
      idTienda = "",
      idAnuncio = "",
    } = req.body;

    const mensaje = {
      token: token,
      data: {
        title: String(title),
        body: String(body),
        link: String(link),
        image: String(image),
        idTienda: String(idTienda),
        idAnuncio: String(idAnuncio),
      },
      android: {
        priority: "high",
      },
    };

    const respuesta = await admin.messaging().send(mensaje);
    res.status(200).send("Notificación enviada: " + respuesta);
  } catch (error) {
    console.error(error);
    res.status(500).send(error.message);
  }
});
*/
