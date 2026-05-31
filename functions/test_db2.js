// test_db2.js
const { onRequest } = require("firebase-functions/v2/https");
const admin = require("firebase-admin");

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

// =====================================================
// ✅ obtener_creditos_tienda_fn — lógica pura interna
// =====================================================

async function actualizar_creditos_tienda(id_tienda, saldo_actual) {

  try {

    const db = initDb2();

    if (!db) {
      throw new Error("Firestore DB2 no inicializado");
    }

    if (!id_tienda) {
      throw new Error("id_tienda requerido");
    }

    await db
      .collection("creditos_tienda")
      .doc(id_tienda)
      .update({
        creditos: saldo_actual
      });

    return {
      ok: true,
      mensaje: "Créditos actualizados"
    };

  } catch (error) {

    console.error("❌ Error actualizando créditos:", error);

    return {
      ok: false,
      error: error.message
    };
  }
}
exports.actualizar_creditos_tienda = actualizar_creditos_tienda;

const obtener_creditos_tienda_fn = async (id) => {
  if (!id) return { ok: false, creditos: 0 };

  try {
    const database = initDb2();
    if (!database) return { ok: false, creditos: 0 };

    const snap = await database.collection("creditos_tienda").doc(id).get();

    if (!snap.exists) return { ok: false, creditos: 0 };

    const creditos = Number(snap.get("creditos") || 0);

    return { ok: true, creditos };
  } catch (e) {
    console.error("❌ Error obtener_creditos_tienda_fn:", e.message);
    return { ok: false, creditos: 0 };
  }
};
exports.obtener_creditos_tienda_fn = obtener_creditos_tienda_fn;
exports.eliminar_deuda_actual = eliminar_deuda_actual;
// =====================================================
// ✅ obtener_creditos_tienda — HTTP endpoint
// =====================================================
exports.obtener_creditos_tienda = onRequest(
  { cors: true, region: "us-central1", memory: "128MiB" },
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

      // ── Precios y monto mínimo desde precio_apartado/bot_daniel ──
      const preciosSnap = await database
        .collection("precio_apartado")
        .doc("bot_daniel")
        .get();

      if (!preciosSnap.exists) {
        console.error("❌ No se encontró precio_apartado/bot_daniel");
        return res.status(500).json({
          ok: false,
          error: "No se encontró precio_apartado/bot_daniel",
        });
      }

      const montoMinimo = Number(
        preciosSnap.get("monto_minimo_plantilla") || 0,
      );

      if (!montoMinimo) {
        console.error("❌ monto_minimo_plantilla inválido o en cero");
        return res.status(500).json({
          ok: false,
          error: "monto_minimo_plantilla inválido o en cero",
        });
      }

      console.log("💲 monto_minimo_plantilla:", montoMinimo);

      // ── Créditos de la tienda ─────────────────────────
      const result = await obtener_creditos_tienda_fn(id);

      if (!result.ok) return res.status(404).json({ ok: false, existe: false });

      const mayor_a_minimo = result.creditos >= montoMinimo;

      console.log(
        `💰 Créditos: ${result.creditos} | Mínimo: ${montoMinimo} | Suficiente: ${mayor_a_minimo}`,
      );

      return res.status(200).json({
        ok: true,
        existe: true,
        mayor_a_100: mayor_a_minimo,
      });
    } catch (e) {
      console.error("❌ Error endpoint obtener_creditos_tienda:", e.message);
      return res.status(500).json({ ok: false });
    }
  },
);
// =====================================================
// 📜 guardar_historial_para_tienda
// =====================================================
async function guardar_historial_para_tienda(
  id_tienda,
  localidad,
  nombre_tienda,
  monto_descontado,
  monto_restante,
  tipo_paquete = "Envio de plantillas (asistente Whatsapp)",
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

  const precio_soles = (monto_descontado * 0.01).toFixed(2);
  const id_transaccion = crypto.randomUUID();

  // ── db = Firebase principal (Tiendas está en la DB principal) ──
  const historialRef = admin
    .firestore()
    .collection("Tiendas")
    .doc(localidad)
    .collection(localidad)
    .doc(id_tienda)
    .collection("historial_financiero")
    .doc(id_transaccion);

  await historialRef.set({
    datos_recarga: {
      estado: "Aceptado",
      monto_descontado: monto_descontado,
      monto_restante: monto_restante,
      precio_soles: precio_soles,
      tipo_paquete: tipo_paquete,
    },
    datos_tienda: {
      id_tienda: id_tienda,
      localidad_tienda: localidad,
      nombre_tienda: nombre_tienda,
    },
    hora_fecha: {
      fecha: fecha,
      hora: hora,
    },
    id_transaccion: id_transaccion,
    timestamp: admin.firestore.Timestamp.fromDate(now),
    tipo_transacción: "descuento",
  });

  console.log("📜 Historial financiero guardado:", id_transaccion);

  return id_transaccion;
}

// =====================================================
// ➖ descontar_creditos_tiendas
//    Lee datos tienda → descuenta puntos → guarda historial
// =====================================================
async function descontar_creditos_tiendas(
  id_tienda,
  cantidad,
  tipo_paquete = "Envio de plantillas (asistente Whatsapp)",
) {
  // ── Lee nombre, localidad y saldo desde la DB principal ──
  const tiendaSnap = await admin
    .firestore()
    .collection("Tiendas")
    .doc("barranca")
    .collection("barranca")
    .doc(id_tienda)
    .get();

  if (!tiendaSnap.exists) {
    throw new Error(`Tienda no encontrada en Tiendas: ${id_tienda}`);
  }

  const tiendaData = tiendaSnap.data();
  const nombre_tienda = tiendaData.nombre_tienda || "Sin nombre";
  const localidad = tiendaData.localidad || "barranca";
  const saldo_actual = Number(tiendaData.puntos_tienda || 0);
  const monto_restante = saldo_actual - cantidad;

  console.log(
    `💰 Saldo: ${saldo_actual} | Descuento: ${cantidad} | Restante: ${monto_restante}`,
  );

  if (monto_restante < 0) {
    throw new Error(
      `Créditos insuficientes en Tiendas. Saldo: ${saldo_actual}, requerido: ${cantidad}`,
    );
  }

  // ── Descuenta puntos_tienda ──────────────────────────
  await admin
    .firestore()
    .collection("Tiendas")
    .doc(localidad)
    .collection(localidad)
    .doc(id_tienda)
    .update({
      puntos_tienda: admin.firestore.FieldValue.increment(-cantidad),
    });

  console.log(`✅ Descontado ${cantidad} puntos a ${nombre_tienda}`);

  // ── Guarda historial financiero ──────────────────────
  const id_transaccion = await guardar_historial_para_tienda(
    id_tienda,
    localidad,
    nombre_tienda,
    cantidad,
    monto_restante,
    tipo_paquete,
  );

  return {
    saldo_anterior: saldo_actual,
    monto_restante,
    id_transaccion,
    nombre_tienda,
    localidad,
  };
}

// =====================================================
// ✅ descontar_creditos_tienda — HTTP endpoint
// =====================================================
exports.descontar_creditos_tienda = onRequest(
  { cors: true, region: "us-central1", memory: "512MiB" },
  async (req, res) => {
    if (req.method !== "POST") {
      return res.status(405).json({ ok: false, error: "Método no permitido" });
    }

    try {
      const { id, monedas, token_id, tipo } = req.body;

      console.log("🚀 BODY:", req.body);

      if (!id)
        return res.status(400).json({ ok: false, error: "ID requerido" });
      if (!token_id)
        return res.status(400).json({ ok: false, error: "token_id requerido" });
      if (!tipo)
        return res
          .status(400)
          .json({ ok: false, error: "tipo requerido (plantilla | whatsapp)" });

      // ── DB2 ──────────────────────────────────────────
      const database = initDb2();
      if (!database)
        return res
          .status(500)
          .json({ ok: false, error: "DB2 no inicializada" });

      // ── Jalar precios desde /precio_apartado/bot_daniel ──
      const preciosSnap = await database
        .collection("precio_apartado")
        .doc("bot_daniel")
        .get();

      if (!preciosSnap.exists) {
        return res.status(500).json({
          ok: false,
          error:
            "No se encontró el documento de precios (precio_apartado/bot_daniel)",
        });
      }

      const COSTO_WHATSAPP = Number(preciosSnap.get("contacto_directo") || 0);
      const COSTO_PLANTILLA = Number(preciosSnap.get("plantillas") || 0);

      if (!COSTO_WHATSAPP || !COSTO_PLANTILLA) {
        return res.status(500).json({
          ok: false,
          error: "Precios inválidos o en cero en precio_apartado/bot_daniel",
        });
      }

      console.log(
        "💲 Precios cargados — contacto_directo:",
        COSTO_WHATSAPP,
        "| plantillas:",
        COSTO_PLANTILLA,
      );

      const descuento = tipo === "whatsapp" ? COSTO_WHATSAPP : COSTO_PLANTILLA;

      const tiendaRef = database.collection("creditos_tienda").doc(id);

      // ── Transaction en DB2 ────────────────────────────
      const result = await database.runTransaction(async (tx) => {
        const tiendaSnap = await tx.get(tiendaRef);

        if (!tiendaSnap.exists) {
          console.log("❌ Tienda no existe en creditos_tienda:", id);
          return { ok: false, error: "La tienda no existe" };
        }

        const now = admin.firestore.Timestamp.now();
        const creditosActuales = Number(tiendaSnap.get("creditos") || 0);

        console.log("💰 Créditos DB2:", creditosActuales);

        if (creditosActuales < descuento) {
          console.log("⚠️ Créditos insuficientes en DB2");
          return {
            ok: false,
            error: "Créditos insuficientes",
            creditos_actuales: creditosActuales,
          };
        }

        const nuevosCreditos = creditosActuales - descuento;
        const fechaId = new Date().toISOString().split("T")[0];

        // 1️⃣ Actualiza créditos
        tx.update(tiendaRef, { creditos: nuevosCreditos, updatedAt: now });

        // 2️⃣ Historial bot envíos
        const historialRef = tiendaRef.collection("historial_bot_envios").doc();
        tx.set(historialRef, {
          timestamp: now,
          monedas_descontadas: descuento,
          creditos_antes: creditosActuales,
          creditos_despues: nuevosCreditos,
          tipo: "recomendacion_asistente",
          token_id,
        });
        console.log("🧾 Historial bot creado:", historialRef.id);

        // 3️⃣ Estadísticas
        const estadisticaRef = tiendaRef
          .collection("estadisticas")
          .doc(fechaId);
        tx.set(
          estadisticaRef,
          {
            enviados: admin.firestore.FieldValue.increment(1),
            monedasGastadas: admin.firestore.FieldValue.increment(descuento),
            updatedAt: now,
          },
          { merge: true },
        );
        console.log("📊 Estadísticas actualizadas:", fechaId);

        // 4️⃣ Token interacción directa
        const tokenRef = tiendaRef
          .collection("interaccion_directa_bot")
          .doc(token_id);
        const fin = admin.firestore.Timestamp.fromMillis(
          now.toMillis() + 24 * 60 * 60 * 1000,
        );
        tx.set(tokenRef, {
          inicio: now,
          fin,
          usado: false,
          estado: "enviado",
          createdAt: now,
          historial_id: historialRef.id,
          monedas: descuento,
        });
        console.log("🎟️ Token creado:", token_id);

        return {
          ok: true,
          tienda_id: id,
          token_id,
          historial_id: historialRef.id,
          creditos_anteriores: creditosActuales,
          descontado: descuento,
          creditos_actuales: nuevosCreditos,
          estadistica_fecha: fechaId,
        };
      });

      // ── Si la transaction falló, no seguimos ──────────
      if (!result.ok) {
        return res.status(400).json(result);
      }

      // ── Descontamos puntos_tienda + historial financiero en DB principal ──
      try {
        const historialFinanciero = await descontar_creditos_tiendas(
          id,
          descuento,
          "Envio de plantillas (asistente Whatsapp)",
        );

        console.log(
          "📜 Historial financiero OK:",
          historialFinanciero.id_transaccion,
        );

        result.id_transaccion_financiero = historialFinanciero.id_transaccion;
      } catch (e) {
        console.warn(
          "⚠️ Historial financiero no guardado (no crítico):",
          e.message,
        );
      }

      console.log("✅ Descuento completado:", result);
      return res.status(200).json(result);
    } catch (e) {
      console.error("❌ Error en descontar_creditos_tienda:", e);
      return res.status(500).json({ ok: false, error: e.message });
    }
  },
);

async function eliminar_deuda_actual(user_id) {
  try {
    if (!user_id) {
      throw new Error("user_id requerido");
    }

    // ─────────────────────────────────────────
    // DB2
    // ─────────────────────────────────────────
    const database = initDb2();

    if (!database) {
      throw new Error("DB2 no inicializada");
    }

    const creditosRef = database
      .collection("creditos_tienda")
      .doc(user_id);

    const result = await database.runTransaction(async (tx) => {
      const snap = await tx.get(creditosRef);

      if (!snap.exists) {
        throw new Error("La tienda no existe en creditos_tienda");
      }

      const data = snap.data() || {};

      const deuda_actual = Number(data.deuda_pendiente || 0);
      const creditos_actuales = Number(data.creditos || 0);

      console.log("💳 deuda_actual:", deuda_actual);
      console.log("🪙 creditos_actuales:", creditos_actuales);

      // ✅ No hay deuda
      if (deuda_actual <= 0) {
        return {
          ok: true,
          tenia_deuda: false,
          deuda_eliminada: 0,
          creditos_actuales,
        };
      }

      // ─────────────────────────────────────────
      // ELIMINAR DEUDA
      // ─────────────────────────────────────────
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

    return {
      ok: false,
      error: e.message,
    };
  }
};
