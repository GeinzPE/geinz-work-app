const { onRequest } = require("firebase-functions/v2/https");
const admin = require("firebase-admin");

// ── INIT SOLO SI NO EXISTE YA ──
if (!admin.apps.find((app) => app.name === "app2")) {
  admin.initializeApp(
    {
      credential: admin.credential.cert({
        projectId: process.env.PROJECT_ID_2,
        clientEmail: process.env.CLIENT_EMAIL_2,
        privateKey: process.env.PRIVATE_KEY_2?.replace(/\\n/g, "\n"),
      }),
    },
    "app2",
  );
}

const db2 = admin.app("app2").firestore();

exports.obtener_creditos_tienda = onRequest(
  {
    cors: true,
    region: "us-central1",
    memory: "128MiB",
  },

  async (req, res) => {

    try {

      // ✅ solo POST
      if (req.method !== "POST") {

        return res.status(405).json({
          ok: false,
        });
      }

      const id = req.body?.id;

      // ✅ validar rápido
      if (!id) {

        return res.status(400).json({
          ok: false,
        });
      }

      // ✅ lectura directa
      const snap = await db2
        .collection("creditos_tienda")
        .doc(id)
        .get();

      // ✅ no existe
      if (!snap.exists) {

        return res.status(404).json({
          ok: false,
          existe: false,
        });
      }

      // ✅ solo obtener creditos
      const creditos = Number(
        snap.get("creditos") || 0
      );

      return res.status(200).json({
        ok: true,
        existe: true,
        creditos,
        mayor_a_100: creditos > 100,
      });

    } catch (e) {

      return res.status(500).json({
        ok: false,
      });
    }
  }
);
