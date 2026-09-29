/**
 * functions/carritoShare.js
 * Sirve /carrito y /perfil/{alias}/carrito con meta Open Graph dinámicos
 * cuando la URL trae ?oferta=ID (para previews en WhatsApp, IG, etc).
 * Funciona con cualquier dominio/subdominio registrado en dominio_web_tiendas.
 */
const { onRequest } = require("firebase-functions/v2/https");
const { initializeApp, getApps } = require("firebase-admin/app");
const { getFirestore } = require("firebase-admin/firestore");

if (!getApps().length) initializeApp();
const db = getFirestore();

/* ═══════════ CONFIGURACIÓN (edita SOLO esto) ═══════════ */

// HTML base del carrito (es el MISMO archivo para todos los negocios y dominios).
// Usa tu dominio principal, sin ningún dominio de cliente.
const STATIC_CARRITO = "https://geinztech.com/carrito/carrito.html";

// Ruta real:
// /Tiendas/{pais}/departamento/{departamento}/provincia/{provincia}/distrito/{distrito}/tiendas/{id}
// Los campos vienen del doc de dominio_web_tiendas (o alias_tiendas).
// Si alguno falta, se usan estos valores por defecto.
const DEFAULT_PAIS = "peru";
const DEFAULT_DEPARTAMENTO = "lima";

const tiendaRef = (t) => {
  const localidad = String(t.localidad || "").trim().toLowerCase();
  const pais = String(t.pais || DEFAULT_PAIS).trim().toLowerCase();
  const departamento = String(t.departamento || DEFAULT_DEPARTAMENTO).trim().toLowerCase();
  const provincia = String(t.provincia || localidad).trim().toLowerCase();
  const distrito = String(t.distrito || localidad).trim().toLowerCase();
  return db.doc(
    `Tiendas/${pais}/departamento/${departamento}/provincia/${provincia}/distrito/${distrito}/tiendas/${t.id}`,
  );
};

/* ═══════════ HELPERS ═══════════ */

const HOSTS_PROPIOS = new Set(["geinztech.com", "www.geinztech.com"]);

const esc = (s) =>
  String(s ?? "").replace(
    /[&<>"']/g,
    (m) => ({ "&": "&amp;", "<": "&lt;", ">": "&gt;", '"': "&quot;", "'": "&#39;" })[m],
  );

function limpiarHost(h) {
  return String(h || "").split(",")[0].trim().toLowerCase().replace(/:\d+$/, "");
}

// Dominio real del visitante (funciona con Custom Hostnames de Cloudflare)
function hostOriginal(req) {
  const candidatos = [
    req.headers["x-original-host"], // lo puede poner tu Worker
    req.headers["x-forwarded-host"], // Firebase Hosting
    req.headers["host"],
  ];
  for (const c of candidatos) {
    const h = limpiarHost(c);
    if (
      h &&
      h !== "proxy.geinztech.com" &&
      !h.endsWith(".web.app") &&
      !h.endsWith(".firebaseapp.com") &&
      !h.includes("cloudfunctions.net") &&
      !h.includes("run.app")
    ) {
      return h;
    }
  }
  return "";
}

async function resolverTienda(host, path) {
  if (!host) return null;

  if (!HOSTS_PROPIOS.has(host)) {
    const sinWww = host.replace(/^www\./, "");
    for (const h of [host, sinWww, `www.${sinWww}`]) {
      const s = await db.doc(`dominio_web_tiendas/${h}`).get();
      if (s.exists) return s.data();
    }
    return null;
  }

  const m = path.match(/^\/perfil\/([^/]+)\/carrito/);
  if (!m) return null;
  const s = await db.doc(`alias_tiendas/${decodeURIComponent(m[1])}`).get();
  return s.exists ? s.data() : null;
}

async function cargarOferta(ref, biz, ofertaId) {
  // Oferta del momento (promociones_geinz)
  if (ofertaId.startsWith("activa_")) {
    const s = await ref
      .collection("promociones_geinz")
      .doc(ofertaId.slice(7))
      .get();
    if (!s.exists) return null;
    const d = s.data();
    const info = d.informacion || d;
    return {
      titulo: info.titulo || "Oferta",
      descripcion: info.descripcion || "",
      precio: Number(d.precio_publicacion) || 0,
      imagen: d.img_container?.lista_img?.[0] || d.img_container?.logo_img || "",
    };
  }

  // Banner
  if (ofertaId === "banner") {
    const b = biz.banner || {};
    return {
      titulo: b.descripcion || "Promoción",
      descripcion: b.descripcion || "",
      precio: Number(b.precio) || 0,
      imagen: b.imagen || "",
    };
  }

  // Promoción normal (img_tienda.lista_img.promociones)
  const p = biz.img_tienda?.lista_img?.promociones?.[ofertaId];
  if (!p || typeof p !== "object") return null;
  return {
    titulo: p.descripcion || "Promoción",
    descripcion: p.descripcion || "",
    precio: Number(p.precio) || 0,
    imagen: p.imagen || "",
  };
}

// Caché en memoria del HTML estático (5 min) para no pedirlo en cada request
let _htmlCache = { html: "", ts: 0 };
async function obtenerHtmlBase() {
  if (_htmlCache.html && Date.now() - _htmlCache.ts < 5 * 60 * 1000) {
    return _htmlCache.html;
  }
  const r = await fetch(STATIC_CARRITO);
  if (!r.ok) throw new Error(`No se pudo leer el carrito estático (${r.status})`);
  const html = await r.text();
  _htmlCache = { html, ts: Date.now() };
  return html;
}

function inyectarMeta(html, meta) {
  const sinTitle = html.replace(/<title>[\s\S]*?<\/title>/i, "");
  return /<head[^>]*>/i.test(sinTitle)
    ? sinTitle.replace(/<head[^>]*>/i, (m) => `${m}${meta}`)
    : `${meta}${sinTitle}`;
}

/* ═══════════ FUNCIÓN ═══════════ */

const carritoShare = onRequest(
  { region: "us-central1", memory: "256MiB", timeoutSeconds: 30 },
  async (req, res) => {
    let html;
    try {
      html = await obtenerHtmlBase();
    } catch (e) {
      console.error("carritoShare base:", e.message);
      return res.status(502).send("Carrito no disponible");
    }

    const ofertaId = req.query.oferta ? String(req.query.oferta) : null;

    if (ofertaId) {
      try {
        const host = hostOriginal(req);
        const t = await resolverTienda(host, req.path);

        if (t?.id && t?.localidad) {
          const ref = tiendaRef(t);
          const bizSnap = await ref.get();
          const biz = bizSnap.exists ? bizSnap.data() : {};
          const negocio = biz.nombre_tienda || biz.nombre || "Geinz";
          const o = await cargarOferta(ref, biz, ofertaId);

          const titulo = o
            ? `${o.titulo}${o.precio > 0 ? ` · S/ ${o.precio.toFixed(2)}` : ""} — ${negocio}`
            : `Ofertas de ${negocio}`;
          const desc = (o?.descripcion || `Mira las ofertas de ${negocio}`).slice(0, 180);
          const img = o?.imagen || biz.img_tienda?.logo_tienda || "";
          const url = `https://${host}${req.originalUrl}`;

          const meta = `
<title>${esc(titulo)}</title>
<meta name="description" content="${esc(desc)}">
<meta property="og:type" content="website">
<meta property="og:site_name" content="${esc(negocio)}">
<meta property="og:title" content="${esc(titulo)}">
<meta property="og:description" content="${esc(desc)}">
<meta property="og:url" content="${esc(url)}">
<meta property="og:locale" content="es_PE">
${img ? `<meta property="og:image" content="${esc(img)}">\n<meta name="twitter:image" content="${esc(img)}">` : ""}
<meta name="twitter:card" content="summary_large_image">
<meta name="twitter:title" content="${esc(titulo)}">
<meta name="twitter:description" content="${esc(desc)}">
`;
          html = inyectarMeta(html, meta);
        }
      } catch (e) {
        // Si algo falla, igual se sirve el carrito normal
        console.warn("carritoShare meta:", e.message);
      }
    }

    res
      .set("Content-Type", "text/html; charset=utf-8")
      .set("Cache-Control", "public, max-age=60, s-maxage=300")
      .set("Vary", "Host")
      .status(200)
      .send(html);
  },
);

module.exports = {
  carritoShare,
  // helpers compartidos con perfil_share.js
  helpers: {
    db,
    tiendaRef,
    esc,
    limpiarHost,
    hostOriginal,
    resolverTienda,
    inyectarMeta,
    HOSTS_PROPIOS,
  },
};