/**
 * functions/perfilShare.js
 * Sirve el perfil del negocio con meta Open Graph propios (nombre, descripción, logo).
 * Funciona igual para:
 *   - Dominio propio:  https://nuestrahistoriajuntos.com/
 *   - Perfil de Geinz: https://geinztech.com/perfil/alonsopenarestobar1621
 * Ambos apuntan al mismo negocio (dominio_web_tiendas / alias_tiendas).
 */
const { onRequest } = require("firebase-functions/v2/https");
const {
  tiendaRef,
  esc,
  limpiarHost,
  hostOriginal,
  resolverTienda,
  inyectarMeta,
  HOSTS_PROPIOS,
  db,
} = require("./carrito_share").helpers;

/* ═══════════ CONFIGURACIÓN ═══════════ */

// HTML base del perfil (mismo archivo para todos los negocios).
// Según tu canonical es public/perfil_negocio.html (con cleanUrls va sin .html).
const STATIC_PERFIL = "https://geinztech.com/perfil.html";
const STATIC_APP_SHELL = "https://geinztech.com/app-shell.html";
/* ═══════════ HELPERS ═══════════ */

const _cache = new Map(); // url -> { html, ts }
async function obtenerHtml(url) {
  const c = _cache.get(url);
  if (c && Date.now() - c.ts < 5 * 60 * 1000) return c.html;
  const r = await fetch(url);
  if (!r.ok) throw new Error(`No se pudo leer ${url} (${r.status})`);
  const html = await r.text();
  _cache.set(url, { html, ts: Date.now() });
  return html;
}

// Geinz: /perfil/{alias}[-carta | -mesa-token]   |   Dominio propio: dominio_web_tiendas
async function tiendaDePerfil(host, path) {
  if (!HOSTS_PROPIOS.has(host)) return resolverTienda(host, path);

  let alias = decodeURIComponent(
    path.replace(/^\/perfil\//, "").split("/")[0] || "",
  ).trim();
  alias = alias.replace(/-carta$/, "").replace(/-mesa-[a-zA-Z0-9]+$/, "");
  if (!alias) return null;

  const s = await db.doc(`alias_tiendas/${alias}`).get();
  return s.exists ? { ...s.data(), alias } : null;
}

function limpiarMetaViejos(html) {
  return html
    .replace(/<meta\s+(?:property|name)="(?:og:[^"]*|twitter:[^"]*|description)"[^>]*>\s*/gi, "")
    .replace(/<link\s+rel="canonical"[^>]*>\s*/gi, "");
}

/* ═══════════ FUNCIÓN ═══════════ */

const perfilShare = onRequest(
  { region: "us-central1", memory: "256MiB", timeoutSeconds: 30 },
  async (req, res) => {
    // 1) Los dominios de Firebase Hosting siempre caen en el oficial
    const rawHost = limpiarHost(req.headers["x-forwarded-host"] || req.headers["host"]);
    if (rawHost.endsWith(".web.app") || rawHost.endsWith(".firebaseapp.com")) {
      return res.redirect(301, `https://geinztech.com${req.originalUrl}`);
    }

    const host = hostOriginal(req);
    const esCore = !host || HOSTS_PROPIOS.has(host);

    // 2) Home de geinztech.com → app shell
    if (esCore && req.path === "/") {
      try {
        const shell = await obtenerHtml(STATIC_APP_SHELL);
        return res
          .set("Content-Type", "text/html; charset=utf-8")
          .set("Cache-Control", "public, max-age=60, s-maxage=300")
          .status(200)
          .send(shell);
      } catch (e) {
        console.error("perfilShare app-shell:", e.message);
        return res.status(500).send("Error interno");
      }
    }

    // 3) Perfil (dominio propio "/" o geinztech.com/perfil/alias)
    let html;
    try {
      html = await obtenerHtml(STATIC_PERFIL);
    } catch (e) {
      console.error("perfilShare base:", e.message);
      return res.status(502).send("Perfil no disponible");
    }

    let status = 200;
    try {
      const t = await tiendaDePerfil(host, req.path);

      if (!t?.id || !t?.localidad) {
        status = 404; // el JS del perfil muestra su pantalla "Perfil no encontrado"
      } else {
        const bizSnap = await tiendaRef(t).get();
        const biz = bizSnap.exists ? bizSnap.data() : {};

        const nombre = biz.nombre_tienda || biz.nombre || "Geinz";
        const categoria = biz.categoria_tienda || "";
        let titulo = categoria ? `${nombre} — ${categoria}` : nombre;
        let desc = String(biz.descripcion || "").trim() ||
          `Conoce ${nombre}: horarios, ubicación, promociones y más.`;
        let img = biz.img_tienda?.logo_tienda || "";

        // Si se comparte una promo del perfil (?p=ID) usa su foto y precio
        const promoId = req.query.p ? String(req.query.p) : null;
        const promo = promoId ? biz.img_tienda?.lista_img?.promociones?.[promoId] : null;
        if (promo && typeof promo === "object" && promo.imagen) {
          const precio = Number(promo.precio) || 0;
          titulo = `${promo.descripcion || "Promoción"}${precio > 0 ? ` · S/ ${precio.toFixed(2)}` : ""} — ${nombre}`;
          desc = String(promo.descripcion || desc);
          img = promo.imagen;
        }

        desc = desc.replace(/\s+/g, " ").slice(0, 200);

        const urlPublica = HOSTS_PROPIOS.has(host)
          ? `https://geinztech.com/perfil/${encodeURIComponent(t.alias || "")}`
          : `https://${host}/`;
        const url = promoId ? `${urlPublica}${urlPublica.includes("?") ? "&" : "?"}p=${encodeURIComponent(promoId)}` : urlPublica;

        const meta = `
<title>${esc(titulo)}</title>
<meta name="description" content="${esc(desc)}">
<link rel="canonical" href="${esc(urlPublica)}">
<meta property="og:type" content="website">
<meta property="og:site_name" content="${esc(nombre)}">
<meta property="og:title" content="${esc(titulo)}">
<meta property="og:description" content="${esc(desc)}">
<meta property="og:url" content="${esc(url)}">
<meta property="og:locale" content="es_PE">
${img ? `<meta property="og:image" content="${esc(img)}">\n<meta name="twitter:image" content="${esc(img)}">` : ""}
<meta name="twitter:card" content="summary_large_image">
<meta name="twitter:title" content="${esc(titulo)}">
<meta name="twitter:description" content="${esc(desc)}">
`;
        // <base href="/"> hace que ./style, ./js, ../img funcionen desde /perfil/alias
        const base = /<base\s/i.test(html) ? "" : '<base href="/">\n';
        html = inyectarMeta(limpiarMetaViejos(html), base + meta, true);
      }
    } catch (e) {
      console.warn("perfilShare meta:", e.message); // igual se sirve el perfil normal
    }

    res
      .set("Content-Type", "text/html; charset=utf-8")
      .set("Cache-Control", "public, max-age=60, s-maxage=120")
      .set("Vary", "Host")
      .status(status)
      .send(html);
  },
);

module.exports = { perfilShare };