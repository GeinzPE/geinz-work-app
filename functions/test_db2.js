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

// ✅ lógica pura — para llamar internamente
const obtener_creditos_tienda_fn = async (id) => {
  if (!id) return { ok: false, mayor_a_100: false };

  try {
    const database = initDb2();
    if (!database) return { ok: false, mayor_a_100: false };

    const snap = await database.collection("creditos_tienda").doc(id).get();

    if (!snap.exists) return { ok: false, mayor_a_100: false };

    const creditos = Number(snap.get("creditos") || 0);

    return {
      ok: true,
      mayor_a_100: creditos > 100,
    };
  } catch (e) {
    console.error("❌ Error obtener_creditos_tienda_fn:", e.message);
    return { ok: false, mayor_a_100: false };
  }
};

exports.obtener_creditos_tienda_fn = obtener_creditos_tienda_fn;

// ✅ HTTP endpoint
exports.obtener_creditos_tienda = onRequest(
  {
    cors: true,
    region: "us-central1",
    memory: "128MiB",
  },
  async (req, res) => {
    try {
      if (req.method !== "POST") {
        return res.status(405).json({ ok: false });
      }

      const id = req.body?.id;
      if (!id) return res.status(400).json({ ok: false });

      const result = await obtener_creditos_tienda_fn(id);

      if (!result.ok) {
        return res.status(404).json({ ok: false, existe: false });
      }

      return res.status(200).json({
        ok: true,
        existe: true,
        mayor_a_100: result.mayor_a_100,
      });
    } catch (e) {
      console.error("❌ Error endpoint obtener_creditos_tienda:", e.message);
      return res.status(500).json({ ok: false });
    }
  },
);

exports.descontar_creditos_tienda = onRequest(
  {
    cors: true,
    region: "us-central1",
    memory: "512MiB",
  },
  async (req, res) => {

    // =========================
    // 🔒 SOLO POST
    // =========================
    if (req.method !== "POST") {
      return res.status(405).json({
        ok: false,
        error: "Método no permitido",
      });
    }

    try {

      // =========================
      // 📦 BODY
      // =========================
      const { id, monedas } = req.body;

      console.log("🚀 BODY:", req.body);

      // =========================
      // 🔍 VALIDACIONES
      // =========================
      if (!id) {
        return res.status(400).json({
          ok: false,
          error: "ID requerido",
        });
      }

      const descuento = Number(monedas);

      if (isNaN(descuento) || descuento <= 0) {
        return res.status(400).json({
          ok: false,
          error: "Cantidad inválida",
        });
      }

      // =========================
      // 🔥 INIT DB2
      // =========================
      const database = initDb2();

      if (!database) {
        return res.status(500).json({
          ok: false,
          error: "DB2 no inicializada",
        });
      }

      // =========================
      // 📄 REF PRINCIPAL
      // =========================
      const ref = database
        .collection("creditos_tienda")
        .doc(id);

      // =========================
      // 🔥 TRANSACTION
      // =========================
      const result = await database.runTransaction(async (tx) => {

        // =========================
        // 📥 GET DOC
        // =========================
        const snap = await tx.get(ref);

        if (!snap.exists) {

          console.log("❌ Tienda no existe:", id);

          return {
            ok: false,
            error: "La tienda no existe",
          };
        }

        // =========================
        // 💳 CREDITOS ACTUALES
        // =========================
        const creditosActuales = Number(
          snap.get("creditos") || 0
        );

        console.log("💰 Créditos actuales:", creditosActuales);

        // =========================
        // ❌ CREDITOS INSUFICIENTES
        // =========================
        if (creditosActuales < descuento) {

          console.log("⚠️ Créditos insuficientes");

          return {
            ok: false,
            error: "Créditos insuficientes",
            creditos_actuales: creditosActuales,
          };
        }

        // =========================
        // ➖ NUEVO SALDO
        // =========================
        const nuevosCreditos =
          creditosActuales - descuento;

        // =========================
        // ⏱️ TIMESTAMP
        // =========================
        const now =
          admin.firestore.Timestamp.now();

        // =========================
        // 🔥 UPDATE CREDITOS
        // =========================
        tx.update(ref, {
          creditos: nuevosCreditos,
          updatedAt: now,
        });

        // =========================
        // 🔥 NUEVO HISTORIAL
        // 👇 SIEMPRE CREA UNO NUEVO
        // =========================
        const historialRef = ref
          .collection("historial_hot")
          .doc();

        tx.set(historialRef, {
          timestamp: now,
          monedas_descontadas: descuento,
          creditos_antes: creditosActuales,
          creditos_despues: nuevosCreditos,
          tipo: "recomendacion_asistente",
        });

        console.log("🔥 Historial creado:", historialRef.id);

        // =========================
        // ✅ RETURN
        // =========================
        return {
          ok: true,
          id,
          historial_id: historialRef.id,
          creditos_anteriores: creditosActuales,
          descontado: descuento,
          creditos_actuales: nuevosCreditos,
        };
      });

      // =========================
      // ❌ ERROR RESULT
      // =========================
      if (!result.ok) {
        return res.status(400).json(result);
      }

      // =========================
      // ✅ SUCCESS
      // =========================
      console.log("✅ Descuento completado:", result);

      return res.status(200).json(result);

    } catch (e) {

      console.error(
        "❌ Error en descontar_creditos_tienda:",
        e
      );

      return res.status(500).json({
        ok: false,
        error: e.message,
      });
    }
  },
);