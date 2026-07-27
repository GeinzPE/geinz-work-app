// functions/conectarFacebookPage.js
//
// Recibe el token de usuario (corto, obtenido del FB.login() en el front)
// + el page_id que el usuario eligió, y hace:
//   1) Intercambia el token corto de usuario por uno de LARGA duración (~60 días)
//   2) Con ese token largo, pide la lista de páginas que administra (/me/accounts)
//      — Meta devuelve ahí el page_access_token de cada página, YA de larga
//      duración, derivado del token largo de usuario.
//   3) Busca la página que el usuario seleccionó y guarda su token en Firestore.
//
// Variables de entorno necesarias (agrégalas a tu functions/.env, mismo
// archivo que ya usas — vi en tu log de deploy "injected env (22) from .env"):
//   FACEBOOK_APP_ID=tu_app_id
//   FACEBOOK_APP_SECRET=tu_app_secret
//
// Las consigues en developers.facebook.com → tu App → Configuración básica.
// FACEBOOK_APP_SECRET NUNCA debe estar en el frontend ni en git.

const { onRequest } = require("firebase-functions/v2/https");
const logger = require("firebase-functions/logger");
const admin = require("firebase-admin");
const axios = require("axios");

if (!admin.apps.length) admin.initializeApp();

const GRAPH_API_VERSION = "v19.0";
const GRAPH_BASE = `https://graph.facebook.com/${GRAPH_API_VERSION}`;

const conectarFacebookPage = onRequest(
  { region: "us-central1", cors: true },
  async (req, res) => {
    try {
      // 1) Validar sesión del usuario en tu plataforma
      const authHeader = req.headers.authorization || "";
      const idToken = authHeader.replace("Bearer ", "");
      if (!idToken) {
        res.status(401).json({ error: { message: "Falta token de autenticación" } });
        return;
      }
      await admin.auth().verifyIdToken(idToken);

      const payload = (req.body && req.body.data) || {};
      const { id_tienda, localidad, page_id, user_access_token } = payload;

      if (!id_tienda || !localidad || !page_id || !user_access_token) {
        res.status(400).json({ error: { message: "Payload incompleto" } });
        return;
      }

      const appId = process.env.FACEBOOK_APP_ID;
      const appSecret = process.env.FACEBOOK_APP_SECRET;
      if (!appId || !appSecret) {
        logger.error("Faltan FACEBOOK_APP_ID / FACEBOOK_APP_SECRET en el entorno");
        res.status(500).json({ error: { message: "Configuración de servidor incompleta" } });
        return;
      }

      // 2) Intercambiar el token corto de usuario por uno de larga duración
      const exchangeResp = await axios.get(`${GRAPH_BASE}/oauth/access_token`, {
        params: {
          grant_type: "fb_exchange_token",
          client_id: appId,
          client_secret: appSecret,
          fb_exchange_token: user_access_token,
        },
      });
      const longLivedUserToken = exchangeResp.data.access_token;

      // 3) Pedir la lista de páginas que administra (con sus page tokens largos)
      const paginasResp = await axios.get(`${GRAPH_BASE}/me/accounts`, {
        params: { access_token: longLivedUserToken },
      });
      const paginas = paginasResp.data.data || [];

      const paginaElegida = paginas.find((p) => p.id === page_id);
      if (!paginaElegida) {
        res.status(400).json({
          result: {
            ok: false,
            error_code: "PAGINA_NO_ENCONTRADA",
            mensaje:
              "No se encontró esa página entre las que administra el usuario. Puede que haya perdido el permiso o elegido una incorrecta.",
          },
        });
        return;
      }

      // 4) Guardar en Firestore
      const db = admin.firestore();
      await db.doc(`Tiendas/${localidad}/${localidad}/${id_tienda}`).set(
        {
          facebook_page: {
            page_id: paginaElegida.id,
            page_name: paginaElegida.name,
            page_access_token: paginaElegida.access_token,
            conectado_en: admin.firestore.FieldValue.serverTimestamp(),
          },
        },
        { merge: true },
      );

      res.status(200).json({
        result: {
          ok: true,
          page_id: paginaElegida.id,
          page_name: paginaElegida.name,
          mensaje: `Fanpage "${paginaElegida.name}" conectada con éxito.`,
        },
      });
    } catch (err) {
      const fbError = err.response && err.response.data && err.response.data.error;
      logger.error("❌ Error en conectarFacebookPage:", fbError || err.message);
      res.status(500).json({
        error: { message: (fbError && fbError.message) || err.message || "Error interno" },
      });
    }
  },
);

module.exports = { conectarFacebookPage };