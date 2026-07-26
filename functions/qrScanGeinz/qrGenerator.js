// functions/qrGenerator.js
//
// Módulo independiente para generar QRs con diseño (gradientes, tarjetas, logo).
// En tu index.js solo necesitas:
//
//   const { qrApi } = require("./qrGenerator");
//   exports.qrApi = qrApi;
//
const { onRequest } = require("firebase-functions/v2/https");
const logger = require("firebase-functions/logger");
const path = require("path");

const puppeteer = require("puppeteer-core");
const chromium = require("@sparticuz/chromium");

const TEMPLATE_PATH = "file://" + path.join(__dirname, "generator.html");

const VALID_STYLES = [
  "style-neon", "style-minimal", "style-badge", "style-ticket",
  "style-sticker", "style-phone", "style-polaroid", "style-clip",
];
const VALID_DOT_SHAPES = [
  "rounded", "dots", "classy", "classy-rounded", "extra-rounded", "square",
];

// Reutilizamos el browser entre invocaciones "warm"
let browserPromise = null;
async function getBrowser() {
  if (!browserPromise) {
    browserPromise = puppeteer.launch({
      args: chromium.args,
      defaultViewport: { width: 900, height: 900, deviceScaleFactor: 3 },
      executablePath: await chromium.executablePath(),
      headless: chromium.headless,
    });
  }
  return browserPromise;
}

/**
 * Genera la imagen PNG del QR (tarjeta completa o solo el QR).
 * @param {object} config
 * @param {string} config.url            - Link al que apunta el QR (requerido)
 * @param {string} [config.style]        - style-neon | style-minimal | style-badge |
 *                                          style-ticket | style-sticker | style-phone |
 *                                          style-polaroid | style-clip
 * @param {string[]} [config.colors]     - [inicio, medio, fin] en hex, ej: ["#7b2ff7","#f107a3","#ff6ec7"]
 * @param {string} [config.dotShape]     - rounded | dots | classy | classy-rounded | extra-rounded | square
 * @param {string} [config.topText]      - Texto arriba de la tarjeta
 * @param {string} [config.subText]      - Texto abajo de la tarjeta
 * @param {string} [config.captionText]  - Texto bajo el QR (si no lo mandas, usa el link)
 * @param {string} [config.logo]         - Logo como data URL base64: "data:image/png;base64,...."
 * @param {boolean} [config.autoColor]   - Si mandas logo y NO mandas "colors", por defecto (true)
 *                                          se detecta el color dominante del logo (ignorando su
 *                                          whitespace/transparencia) y se usa para generar el
 *                                          degradado del QR y el aro alrededor del logo.
 *                                          Pasa false para mantener siempre la paleta por defecto.
 * @param {boolean} [config.onlyQr]      - true = solo el QR sin tarjeta; false = tarjeta completa
 * @returns {Promise<Buffer>} PNG buffer
 */
async function generateQrImage(config) {
  const {
    url,
    style = "style-neon",
    colors,
    dotShape = "rounded",
    topText = "",
    subText = "",
    captionText = "",
    logo = null,
    autoColor = true,
    onlyQr = false,
  } = config;

  if (!url) {
    const err = new Error('El campo "url" es requerido');
    err.status = 400;
    throw err;
  }
  if (style && !VALID_STYLES.includes(style)) {
    const err = new Error(`style inválido. Usa uno de: ${VALID_STYLES.join(", ")}`);
    err.status = 400;
    throw err;
  }
  if (dotShape && !VALID_DOT_SHAPES.includes(dotShape)) {
    const err = new Error(`dotShape inválido. Usa uno de: ${VALID_DOT_SHAPES.join(", ")}`);
    err.status = 400;
    throw err;
  }
  if (colors && (!Array.isArray(colors) || colors.length < 3)) {
    const err = new Error('"colors" debe ser un array de 3 hex: [inicio, medio, fin]');
    err.status = 400;
    throw err;
  }
  if (logo && !/^data:image\/(png|jpe?g|webp);base64,/.test(logo)) {
    const err = new Error('"logo" debe ser un data URL base64 válido, ej: "data:image/png;base64,...."');
    err.status = 400;
    throw err;
  }

  const browser = await getBrowser();
  const page = await browser.newPage();

  try {
    await page.goto(TEMPLATE_PATH, { waitUntil: "load" });

    await page.evaluate((cfg) => window.__applyApiConfig(cfg), {
      url, style, colors, dotShape, topText, subText, captionText, logo, autoColor,
    });

    await page.waitForFunction("window.__qrReady === true", { timeout: 15000 });

    const selector = onlyQr ? "#qrcode canvas" : "#exportCard";
    const el = await page.$(selector);
    if (!el) throw new Error("No se pudo encontrar el elemento a exportar");

    return await el.screenshot({ type: "png", omitBackground: !!onlyQr });
  } finally {
    await page.close();
  }
}

// Handler HTTP: acepta GET (query params) o POST (JSON body)
const qrApi = onRequest(
  { memory: "2GiB", timeoutSeconds: 60, region: "us-central1" },
  async (req, res) => {
    res.set("Access-Control-Allow-Origin", "*");
    res.set("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
    res.set("Access-Control-Allow-Headers", "Content-Type");

    if (req.method === "OPTIONS") {
      res.status(204).send("");
      return;
    }

    try {
      let config;
      if (req.method === "POST") {
        config = req.body || {};
      } else {
        const q = req.query;
        config = {
          url: q.url,
          style: q.style,
          dotShape: q.dotShape,
          topText: q.topText,
          subText: q.subText,
          captionText: q.captionText,
          onlyQr: q.onlyQr === "true" || q.onlyQr === "1",
          autoColor: q.autoColor === undefined ? undefined : (q.autoColor === "true" || q.autoColor === "1"),
          colors: q.colors ? q.colors.split(",") : undefined,
        };
      }

      const buffer = await generateQrImage(config);
      res.set("Content-Type", "image/png");
      res.send(buffer);
    } catch (err) {
      logger.error("qrApi error:", err);
      res.status(err.status || 500).json({ error: err.message });
    }
  },
);

module.exports = { qrApi, generateQrImage };