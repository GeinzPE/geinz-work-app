// functions/publicarEnFacebookOrganico.js
//
// Versión v2 de firebase-functions, en CommonJS puro (require/module.exports)
// — compatible con tu index.js actual, sin import/export de ES Modules.

const { onRequest } = require("firebase-functions/v2/https");
const logger = require("firebase-functions/logger");
const admin = require("firebase-admin");
const axios = require("axios");

if (!admin.apps.length) admin.initializeApp();

const GRAPH_API_VERSION = "v19.0";
const GRAPH_BASE = `https://graph.facebook.com/${GRAPH_API_VERSION}`;

const publicarEnFacebookOrganico = onRequest(
  {
    region: "us-central1", // ajusta si tus otras funciones usan otra región
    cors: true, // v2 maneja CORS automáticamente, no hace falta setear headers a mano
  },
  async (req, res) => {
    try {
      // 1) Validar el Bearer token del usuario
      const authHeader = req.headers.authorization || "";
      const idToken = authHeader.replace("Bearer ", "");
      if (!idToken) {
        res.status(401).json({ error: { message: "Falta token de autenticación" } });
        return;
      }
      await admin.auth().verifyIdToken(idToken); // lanza si es inválido

      const payload = (req.body && req.body.data) || {};
      const { id_tienda, localidad, image_url, caption } = payload;

      if (!id_tienda || !localidad || !image_url || !caption) {
        res.status(400).json({ error: { message: "Payload incompleto" } });
        return;
      }

      // 2) Leer page_access_token / page_id desde Firestore
      const db = admin.firestore();
      const tiendaSnap = await db
        .doc(`Tiendas/${localidad}/${localidad}/${id_tienda}`)
        .get();

      const facebookConfig = (tiendaSnap.data() || {}).facebook_page;
      const pageAccessToken = facebookConfig && facebookConfig.page_access_token;
      const pageId = facebookConfig && facebookConfig.page_id;

      if (!pageAccessToken || !pageId) {
        res.status(400).json({
          result: {
            ok: false,
            error_code: "FANPAGE_NO_CONECTADA",
            mensaje: "Esta tienda no tiene una Fanpage de Facebook conectada.",
          },
        });
        return;
      }

      // 3) Publicar
      const resultado = await publicarFoto({
        pageAccessToken,
        pageId,
        imageUrl: image_url,
        caption,
      });

      res.status(200).json({ result: resultado });
    } catch (err) {
      logger.error("❌ Error en publicarEnFacebookOrganico:", err);
      res.status(500).json({
        error: { message: (err && err.message) || "Error interno" },
      });
    }
  },
);

async function publicarFoto(opts) {
  try {
    const { data } = await axios.post(`${GRAPH_BASE}/${opts.pageId}/photos`, null, {
      params: {
        url: opts.imageUrl,
        caption: opts.caption,
        access_token: opts.pageAccessToken,
      },
    });

    const postId = data.post_id || data.id;
    if (!postId) {
      return { ok: false, error_code: "ERROR_DESCONOCIDO", mensaje: "Facebook no devolvió post_id." };
    }
    return { ok: true, post_id: postId };
  } catch (err) {
    const fbError = err.response && err.response.data && err.response.data.error;

    if (fbError && fbError.code === 190) {
      return {
        ok: false,
        error_code: "TOKEN_INVALIDO_O_EXPIRADO",
        mensaje: "El token de la página expiró. El usuario debe reconectar su Fanpage.",
      };
    }
    return {
      ok: false,
      error_code: "ERROR_DESCONOCIDO",
      mensaje: (fbError && fbError.message) || err.message,
    };
  }
}

module.exports = { publicarEnFacebookOrganico };