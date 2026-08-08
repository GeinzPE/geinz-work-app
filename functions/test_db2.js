// test_db2.js
const { onRequest } = require("firebase-functions/v2/https");
const admin = require("firebase-admin");
const paths = require("./rutas_geinz_firebase/rutas.js");
let db2 = null;

const initDb2 = () => {
  if (db2) return db2;

  try {
    const appExistente = admin.apps.find((app) => app.name === "app2");

    if (appExistente) {
      db2 = appExistente.firestore();
    } else {
      const app2 = admin.initializeApp(
        {
          credential: admin.credential.cert({
            projectId: process.env.PROJECT_ID_2,
            clientEmail: process.env.CLIENT_EMAIL_2,
            privateKey: process.env.PRIVATE_KEY_2?.replace(/\\n/g, "\n"),
          }),
        },
        "app2",
      );
      db2 = app2.firestore();
    }
  } catch (e) {
    console.error("❌ Error inicializando app2:", e.message);
    db2 = null;
  }

  return db2;
};

/* ═══════════════════════════════════════════════════════
   MAESTRA: leer puntos_tienda de Geinz
   Devuelve { saldo, nombre_tienda, localidad }
═══════════════════════════════════════════════════════ */
async function leer_maestra(id_tienda) {
  const tiendaSnap = await paths
    .tiendaCol("barranca", "tiendas", id_tienda)
    .get();
  if (!tiendaSnap.exists) {
    throw new Error(`Tienda no encontrada en maestra: ${id_tienda}`);
  }

  const data = tiendaSnap.data();
  return {
    snap: tiendaSnap,
    saldo: Math.max(Number(data.puntos_tienda || 0), 0),
    nombre_tienda: data.nombre_tienda || "Sin nombre",
    localidad: data.localidad || "barranca",
  };
}

/* ═══════════════════════════════════════════════════════
   COPIA: setear creditos en DB2 = saldo_restante
═══════════════════════════════════════════════════════ */
async function sincronizar_copia(id_tienda, saldo_restante) {
  const database = initDb2();
  if (!database) throw new Error("DB2 no inicializada");

  await database.collection("creditos_tienda").doc(id_tienda).update({
    creditos: saldo_restante,
    updatedAt: admin.firestore.FieldValue.serverTimestamp(),
  });

  console.log("✅ copia DB2 sincronizada →", saldo_restante);
}
exports.sincronizar_copia = sincronizar_copia;

/* ═══════════════════════════════════════════════════════
   actualizar_creditos_tienda
   Recibe saldo_actual ya calculado (viene de la maestra)
   Solo actualiza la COPIA en DB2
═══════════════════════════════════════════════════════ */
async function actualizar_creditos_tienda(id_tienda, saldo_actual) {
  try {
    if (!id_tienda) throw new Error("id_tienda requerido");
    await sincronizar_copia(id_tienda, saldo_actual);
    return { ok: true, mensaje: "Créditos actualizados" };
  } catch (error) {
    console.error("❌ Error actualizando créditos:", error);
    return { ok: false, error: error.message };
  }
}
exports.actualizar_creditos_tienda = actualizar_creditos_tienda;

/* ═══════════════════════════════════════════════════════
   obtener_creditos_tienda_fn
   Lee SIEMPRE de la maestra (puntos_tienda)
═══════════════════════════════════════════════════════ */
const obtener_creditos_tienda_fn = async (id) => {
  if (!id) return { ok: false, creditos: 0 };

  try {
    // ✅ Lee de la maestra, no de la copia
    const { saldo } = await leer_maestra(id);
    return { ok: true, creditos: saldo };
  } catch (e) {
    console.error("❌ Error obtener_creditos_tienda_fn:", e.message);
    return { ok: false, creditos: 0 };
  }
};
exports.obtener_creditos_tienda_fn = obtener_creditos_tienda_fn;

/* ═══════════════════════════════════════════════════════
   obtener_creditos_tienda — HTTP endpoint
═══════════════════════════════════════════════════════ */
exports.obtener_creditos_tienda = onRequest(
  { cors: true, region: "us-central1", memory: "256MIB" },
  async (req, res) => {
    try {
      if (req.method !== "POST") return res.status(405).json({ ok: false });

      const id = req.body?.id;
      if (!id) return res.status(400).json({ ok: false });

      const database = initDb2();
      if (!database)
        return res
          .status(500)
          .json({ ok: false, error: "DB2 no inicializada" });

      const preciosSnap = await database
        .collection("precio_apartado")
        .doc("bot_daniel")
        .get();

      if (!preciosSnap.exists) {
        return res
          .status(500)
          .json({
            ok: false,
            error: "No se encontró precio_apartado/bot_daniel",
          });
      }

      const montoMinimo = Number(
        preciosSnap.get("monto_minimo_plantilla") || 0,
      );
      if (!montoMinimo) {
        return res
          .status(500)
          .json({
            ok: false,
            error: "monto_minimo_plantilla inválido o en cero",
          });
      }

      console.log("💲 monto_minimo_plantilla:", montoMinimo);

      // ✅ Lee saldo desde la MAESTRA
      const result = await obtener_creditos_tienda_fn(id);
      if (!result.ok) return res.status(404).json({ ok: false, existe: false });

      const mayor_a_minimo = result.creditos >= montoMinimo;
      console.log(
        `💰 Créditos: ${result.creditos} | Mínimo: ${montoMinimo} | Suficiente: ${mayor_a_minimo}`,
      );

      return res
        .status(200)
        .json({ ok: true, existe: true, mayor_a_100: mayor_a_minimo });
    } catch (e) {
      console.error("❌ Error endpoint obtener_creditos_tienda:", e.message);
      return res.status(500).json({ ok: false });
    }
  },
);

/* ═══════════════════════════════════════════════════════
   guardar_historial_para_tienda
═══════════════════════════════════════════════════════ */
async function guardar_historial_para_tienda(
  id_tienda,
  localidad,
  nombre_tienda,
  monto_descontado,
  monto_restante,
  tipo_paquete,
) {
  const now = new Date();
  const opcionesZona = { timeZone: "America/Lima" };

  const fecha = now.toLocaleDateString("es-PE", {
    ...opcionesZona,
    day: "2-digit",
    month: "2-digit",
    year: "numeric",
  });

  const hora = now.toLocaleTimeString("es-PE", {
    ...opcionesZona,
    hour: "2-digit",
    minute: "2-digit",
    hour12: false,
  });

  const precioSnap = await initDb2()
    .collection("precio_apartado")
    .doc("app")
    .get();
  const costo_por_moneda = Number(precioSnap.get("costo_por_moneda") || 0);
  const precio_soles = Number(monto_descontado * costo_por_moneda).toFixed(2);

  const id_transaccion = crypto.randomUUID();

  await paths
    .tiendaCol(
      localidad,
      "tiendas",
      id_tienda,
      "historial_financiero",
      id_transaccion,
    )
    .set({
      datos_recarga: {
        estado: "Aceptado",
        monto_descontado,
        monto_restante,
        precio_soles,
        costo_por_moneda,
        tipo_paquete,
      },
      datos_tienda: {
        id_tienda,
        localidad_tienda: localidad,
        nombre_tienda,
      },
      hora_fecha: { fecha, hora },
      id_transaccion,
      timestamp: admin.firestore.Timestamp.fromDate(now),
      tipo_transacción: "descuento",
    });

  return id_transaccion;
}

/* ═══════════════════════════════════════════════════════
   descontar_creditos_tiendas
   1. Lee puntos_tienda de la MAESTRA
   2. Resta ahí
   3. Copia a DB2
   4. Guarda historial
═══════════════════════════════════════════════════════ */
async function descontar_creditos_tiendas(
  id_tienda,
  cantidad,
  tipo_paquete = "Envio de plantillas (asistente Whatsapp)",
) {
  // 1️⃣ Leer MAESTRA
  const {
    saldo: saldo_actual,
    nombre_tienda,
    localidad,
  } = await leer_maestra(id_tienda);

  console.log("💎 saldo maestra →", saldo_actual);

  if (saldo_actual < cantidad) {
    throw new Error(
      `Créditos insuficientes. Saldo: ${saldo_actual}, requerido: ${cantidad}`,
    );
  }

  // 2️⃣ Calcular saldo restante
  const saldo_restante = saldo_actual - cantidad;

  // 3️⃣ Actualizar MAESTRA
  await paths
    .tiendaCol(localidad, "tiendas", id_tienda)

    .update({ puntos_tienda: saldo_restante });

  console.log("✅ maestra actualizada →", saldo_restante);

  // 4️⃣ Sincronizar COPIA en DB2
  await sincronizar_copia(id_tienda, saldo_restante);

  // 5️⃣ Guardar historial
  const id_transaccion = await guardar_historial_para_tienda(
    id_tienda,
    localidad,
    nombre_tienda,
    cantidad,
    saldo_restante,
    tipo_paquete,
  );

  return {
    saldo_anterior: saldo_actual,
    saldo_restante,
    id_transaccion,
    nombre_tienda,
    localidad,
  };
}

/* ═══════════════════════════════════════════════════════
   descontar_creditos_tienda — HTTP endpoint
   ✅ Un solo descuento: primero maestra, luego copia
═══════════════════════════════════════════════════════ */
// ============================================================
// 👇 FUNCIÓN PELADA — reusable desde otro archivo (webhook, etc.)
//    Mismos parámetros, misma lógica, mismo resultado que el endpoint.
// ============================================================
async function descontarCreditosTienda({ id, token_id, tipo }) {
  console.log("🚀 [descontarCreditosTienda] PARAMS:", { id, token_id, tipo });

  if (!id) throw new Error("ID requerido");
  if (!token_id) throw new Error("token_id requerido");
  if (!tipo) throw new Error("tipo requerido (plantilla | whatsapp)");

  const database = initDb2();
  if (!database) throw new Error("DB2 no inicializada");

  // Leer precios desde DB2
  const preciosSnap = await database
    .collection("precio_apartado")
    .doc("bot_daniel")
    .get();

  if (!preciosSnap.exists) {
    throw new Error("No se encontró precio_apartado/bot_daniel");
  }

  const COSTO_WHATSAPP = Number(preciosSnap.get("contacto_directo") || 0);
  const COSTO_PLANTILLA = Number(preciosSnap.get("plantillas") || 0);

  if (!COSTO_WHATSAPP || !COSTO_PLANTILLA) {
    throw new Error("Precios inválidos en precio_apartado/bot_daniel");
  }

  const descuento = tipo === "whatsapp" ? COSTO_WHATSAPP : COSTO_PLANTILLA;
  console.log(
    "💲 [descontarCreditosTienda] descuento →",
    descuento,
    "| tipo →",
    tipo,
  );

  /* ══════════════════════════════════════
     1️⃣ Descontar desde la MAESTRA
        (puntos_tienda Geinz → copia DB2 → historial)
  ══════════════════════════════════════ */
  const resultado = await descontar_creditos_tiendas(
    id,
    descuento,
    tipo === "whatsapp"
      ? "Contacto directo (WhatsApp)"
      : "Envio de plantillas (asistente Whatsapp)",
  );

  console.log("✅ [descontarCreditosTienda] Descuento completado:", resultado);

  /* ══════════════════════════════════════
     2️⃣ Crear token interacción en DB2
  ══════════════════════════════════════ */
  const now = admin.firestore.Timestamp.now();
  const fechaId = new Date().toISOString().split("T")[0];
  const tiendaRef = database.collection("creditos_tienda").doc(id);
  const tokenRef = tiendaRef
    .collection("interaccion_directa_bot")
    .doc(token_id);
  const historialBotRef = tiendaRef.collection("historial_bot_envios").doc();
  const estadisticaRef = tiendaRef.collection("estadisticas").doc(fechaId);
  const fin = admin.firestore.Timestamp.fromMillis(
    now.toMillis() + 24 * 60 * 60 * 1000,
  );

  await Promise.all([
    tokenRef.set({
      inicio: now,
      fin,
      usado: false,
      estado: "enviado",
      createdAt: now,
      historial_id: historialBotRef.id,
      monedas: descuento,
    }),
    historialBotRef.set({
      timestamp: now,
      monedas_descontadas: descuento,
      saldo_antes: resultado.saldo_anterior,
      saldo_despues: resultado.saldo_restante,
      tipo: "recomendacion_asistente",
      token_id,
    }),
    estadisticaRef.set(
      {
        enviados: admin.firestore.FieldValue.increment(1),
        monedasGastadas: admin.firestore.FieldValue.increment(descuento),
        updatedAt: now,
      },
      { merge: true },
    ),
  ]);

  console.log("🎟️ [descontarCreditosTienda] Token creado:", token_id);

  return {
    ok: true,
    tienda_id: id,
    token_id,
    historial_id: historialBotRef.id,
    saldo_anterior: resultado.saldo_anterior,
    descontado: descuento,
    saldo_actual: resultado.saldo_restante,
    id_transaccion_financiero: resultado.id_transaccion,
  };
}

// ============================================================
// 👇 onRequest — wrapper delgado, solo para llamadas HTTP externas
// ============================================================
exports.descontar_creditos_tienda = onRequest(
  { cors: true, region: "us-central1", memory: "512MiB" },
  async (req, res) => {
    if (req.method !== "POST") {
      return res.status(405).json({ ok: false, error: "Método no permitido" });
    }

    try {
      const { id, token_id, tipo } = req.body;

      console.log("🚀 BODY:", req.body);

      const resultado = await descontarCreditosTienda({ id, token_id, tipo });

      return res.status(200).json(resultado);
    } catch (e) {
      console.error("❌ Error en descontar_creditos_tienda:", e);
      return res.status(500).json({ ok: false, error: e.message });
    }
  },
);

// 👇 CLAVE: se exporta también pelada, para poder importarla desde otro archivo
exports.descontarCreditosTienda = descontarCreditosTienda;

/* ═══════════════════════════════════════════════════════
   eliminar_deuda_actual
═══════════════════════════════════════════════════════ */
async function eliminar_deuda_actual(user_id) {
  try {
    if (!user_id) throw new Error("user_id requerido");

    const database = initDb2();
    if (!database) throw new Error("DB2 no inicializada");

    const creditosRef = database.collection("creditos_tienda").doc(user_id);

    const result = await database.runTransaction(async (tx) => {
      const snap = await tx.get(creditosRef);

      if (!snap.exists)
        throw new Error("La tienda no existe en creditos_tienda");

      const data = snap.data() || {};
      const deuda_actual = Number(data.deuda_pendiente || 0);
      const creditos_actuales = Number(data.creditos || 0);

      console.log("💳 deuda_actual:", deuda_actual);
      console.log("🪙 creditos_actuales:", creditos_actuales);

      if (deuda_actual <= 0) {
        return {
          ok: true,
          tenia_deuda: false,
          deuda_eliminada: 0,
          creditos_actuales,
        };
      }

      tx.update(creditosRef, {
        deuda_pendiente: 0,
        updatedAt: admin.firestore.FieldValue.serverTimestamp(),
      });

      console.log("✅ deuda eliminada");

      return {
        ok: true,
        tenia_deuda: true,
        deuda_eliminada: deuda_actual,
        creditos_actuales,
      };
    });

    return result;
  } catch (e) {
    console.error("❌ Error eliminar_deuda_actual:", e.message);
    return { ok: false, error: e.message };
  }
}
exports.eliminar_deuda_actual = eliminar_deuda_actual;
