// slideRenderer.js
// Dibuja cada diapositiva EXACTAMENTE con el mismo lenguaje visual del HTML
// original (gradiente de marca, editor de código falso, checklist, marca de
// agua) pero del lado del servidor: arma un HTML/CSS equivalente y lo
// "fotografía" con Puppeteer (headless Chrome) en vez de usar html2canvas.
//
// También porta la extracción de paleta de color del logo (antes hecha con
// <canvas> en el navegador) usando `sharp` para leer los píxeles.

const sharp = require("sharp");
const { escapeHtml, guessExt } = require("./carouselForge");

const WIDTH = 1080;
const HEIGHT = 1920;

/* ===================== PALETA DE COLOR DEL LOGO ===================== */
// Mismo algoritmo de "bins de matiz" del HTML: agrupa por tono (hue) para
// que un color minoritario del logo (ej. el amarillo de Python) compita
// como grupo aparte del azul, en vez de perderse contra el conteo bruto.

function rgbToHsl(r, g, b) {
  r /= 255; g /= 255; b /= 255;
  const max = Math.max(r, g, b), min = Math.min(r, g, b);
  let h = 0, s = 0;
  const l = (max + min) / 2;
  const d = max - min;
  if (d !== 0) {
    s = d / (1 - Math.abs(2 * l - 1));
    switch (max) {
      case r: h = 60 * (((g - b) / d) % 6); break;
      case g: h = 60 * ((b - r) / d + 2); break;
      case b: h = 60 * ((r - g) / d + 4); break;
    }
    if (h < 0) h += 360;
  }
  return { h, s, l };
}

async function extractPalette(logoBuffer) {
  const size = 100;
  const { data, info } = await sharp(logoBuffer)
    .resize(size, size, { fit: "contain", background: { r: 0, g: 0, b: 0, alpha: 0 } })
    .ensureAlpha()
    .raw()
    .toBuffer({ resolveWithObject: true });

  const HUE_BINS = 24;
  const bins = new Map();
  const channels = info.channels; // 4 (RGBA)

  for (let i = 0; i < data.length; i += channels) {
    const r = data[i], g = data[i + 1], b = data[i + 2], a = data[i + 3];
    if (a < 100) continue;
    const { h: hue, s, l } = rgbToHsl(r, g, b);
    if (l > 0.94 || l < 0.06 || s < 0.15) continue;

    const binIndex = Math.floor(hue / (360 / HUE_BINS)) % HUE_BINS;
    const quantKey = [Math.round(r / 16) * 16, Math.round(g / 16) * 16, Math.round(b / 16) * 16].join(",");
    if (!bins.has(binIndex)) bins.set(binIndex, { count: 0, colors: new Map() });
    const bin = bins.get(binIndex);
    bin.count++;
    bin.colors.set(quantKey, (bin.colors.get(quantKey) || 0) + 1);
  }

  if (bins.size === 0) return ["#22D3EE", "#8B5CF6", "#A78BFA"];

  const sortedBins = [...bins.entries()].sort((a, b) => b[1].count - a[1].count).slice(0, 5);
  return sortedBins.map(([, bin]) => {
    const [topKey] = [...bin.colors.entries()].sort((a, b) => b[1] - a[1])[0];
    const [r, g, b] = topKey.split(",").map(Number);
    return "#" + [r, g, b].map((v) => v.toString(16).padStart(2, "0")).join("");
  });
}

// Versión "glow" borrosa del logo para el fondo (equivalente a
// generateBlurredLogo del HTML, que usaba filter: blur() en <canvas>).
async function generateBlurredLogoDataUrl(logoBuffer) {
  const size = 480;
  const out = await sharp(logoBuffer)
    .resize(Math.round(size * 0.62), Math.round(size * 0.62), { fit: "inside", background: { r: 0, g: 0, b: 0, alpha: 0 } })
    .blur(18)
    .modulate({ saturation: 1.6, brightness: 1.1 })
    .extend({
      top: Math.round(size * 0.19), bottom: Math.round(size * 0.19),
      left: Math.round(size * 0.19), right: Math.round(size * 0.19),
      background: { r: 0, g: 0, b: 0, alpha: 0 },
    })
    .png()
    .toBuffer();
  return `data:image/png;base64,${out.toString("base64")}`;
}

function hexToRgba(hex, alpha) {
  const h = hex.replace("#", "");
  const r = parseInt(h.substring(0, 2), 16) || 0;
  const g = parseInt(h.substring(2, 4), 16) || 0;
  const b = parseInt(h.substring(4, 6), 16) || 0;
  return `rgba(${r},${g},${b},${alpha})`;
}

function buildGradientCss(colors) {
  const base = colors && colors.length ? colors : ["#22D3EE", "#8B5CF6", "#0A0C10"];
  const stops = base.slice(0, 3);
  while (stops.length < 3) stops.push("#0A0C10");
  return `
    radial-gradient(circle at 12% 8%, ${hexToRgba(stops[0], 0.34)} 0%, ${hexToRgba(stops[0], 0.12)} 30%, transparent 55%),
    radial-gradient(circle at 92% 18%, ${hexToRgba(stops[1], 0.26)} 0%, ${hexToRgba(stops[1], 0.09)} 28%, transparent 52%),
    radial-gradient(circle at 45% 105%, ${hexToRgba(stops[2], 0.3)} 0%, ${hexToRgba(stops[2], 0.1)} 30%, transparent 60%),
    linear-gradient(165deg, #0A0C10 0%, #0D1017 100%)`;
}

/* ===================== TAMAÑOS DINÁMICOS DE TEXTO (idénticos al HTML) ===================== */
function codeFontSizeForLines(n) {
  if (n <= 3) return { code: 26, gutter: 23, lh: 1.6 };
  if (n <= 4) return { code: 22, gutter: 20, lh: 1.5 };
  return { code: 19, gutter: 18, lh: 1.4 };
}
function fontSizeForCount(n) {
  if (n <= 4) return { text: 22, ring: 28, gap: 16, pad: 8 };
  if (n <= 6) return { text: 21, ring: 26, gap: 12, pad: 4 };
  return { text: 19, ring: 24, gap: 8, pad: 4 };
}
function computeTextSizing(slide) {
  const subLen = (slide.sub || "").length;
  const codeLines = slide.codigo ? slide.codigo.split("\n").length : 0;
  const isPortada = slide.tipo === "portada";
  const veryHeavy = subLen > 130 || codeLines >= 4;
  const heavy = subLen > 100 || codeLines >= 3;
  if (veryHeavy) return { title: isPortada ? 46 : 34, sub: 23 };
  if (heavy) return { title: isPortada ? 48 : 36, sub: 24 };
  return { title: isPortada ? 50 : 38, sub: 25 };
}

/* ===================== HTML DE LA DIAPOSITIVA ===================== */
const NOISE_SVG =
  "data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='90' height='90'%3E%3Cfilter id='n'%3E%3CfeTurbulence type='fractalNoise' baseFrequency='.85' numOctaves='2' stitchTiles='stitch'/%3E%3C/filter%3E%3Crect width='100%25' height='100%25' filter='url(%23n)'/%3E%3C/svg%3E";

function buildSlideHtml(slide, { tech, items, logoDataUrl, logoBlurredDataUrl, colors, watermarkText, watermarkPos }) {
  const gradient = buildGradientCss(colors);
  const cfs = codeFontSizeForLines(slide.codigo ? slide.codigo.split("\n").length : 0);
  const ts = computeTextSizing(slide);
  const fs = fontSizeForCount(items.length);

  const logoHtml = logoDataUrl
    ? `<div class="logo-box"><img src="${logoDataUrl}" style="width:100%;height:100%;object-fit:contain;padding:6px;box-sizing:border-box;" /></div>`
    : `<div class="logo-box logo-fallback">${escapeHtml((tech || "?").charAt(0))}</div>`;

  const wm = watermarkPos || { xPct: 0.82, yPct: 0.078 };
  const watermarkHtml = watermarkText
    ? `<div class="watermark" style="left:${(wm.xPct * 100).toFixed(2)}%; top:${(wm.yPct * 100).toFixed(2)}%;"><span>${escapeHtml(watermarkText)}</span></div>`
    : "";

  const codeBlock = slide.codigo
    ? `<div class="editor-window">
         <div class="editor-titlebar">
           <span class="dot" style="background:#ff5f57"></span>
           <span class="dot" style="background:#febc2e"></span>
           <span class="dot" style="background:#28c840"></span>
           <span class="filename">${escapeHtml(tech.toLowerCase().replace(/\s+/g, "-"))}${slide.tipo === "reto" ? "-challenge" : ""}.${guessExt(tech)}</span>
         </div>
         <div class="editor-body">
           <div class="gutter" style="font-size:${cfs.gutter}px; line-height:${cfs.lh}">
             ${slide.codigo.split("\n").map((_, i) => `<div>${i + 1}</div>`).join("")}
           </div>
           <pre class="code" style="font-size:${cfs.code}px; line-height:${cfs.lh}">${escapeHtml(slide.codigo)}</pre>
         </div>
       </div>`
    : "";

  const checklistHtml = items.length
    ? `<div class="checklist">
         <p class="checklist-title">Temario</p>
         <ul>
           ${items.map((it, i) => {
             const done = i < slide.checkedCount;
             return `<li style="gap:${fs.gap}px; padding:${fs.pad}px 0;">
               <span class="ring ${done ? "done" : ""}" style="width:${fs.ring}px;height:${fs.ring}px">
                 ${done ? `<svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="#34D399" stroke-width="3.5"><path d="M20 6L9 17l-5-5"/></svg>` : ""}
               </span>
               <span class="item-text" style="font-size:${fs.text}px; color:${done ? "rgba(255,255,255,.88)" : "rgba(255,255,255,.38)"}">${escapeHtml(it)}</span>
             </li>`;
           }).join("")}
         </ul>
       </div>`
    : "";

  const badgeIsReto = slide.tipo === "reto";

  return `<!DOCTYPE html>
<html><head><meta charset="UTF-8" />
<link rel="preconnect" href="https://fonts.googleapis.com">
<link href="https://fonts.googleapis.com/css2?family=Space+Grotesk:wght@600;700&family=Inter:wght@400;500;600;700&family=JetBrains+Mono:wght@400;500;600;700&display=swap" rel="stylesheet">
<style>
  * { box-sizing: border-box; margin:0; padding:0; }
  html,body { width:${WIDTH}px; height:${HEIGHT}px; overflow:hidden; background:#0A0C10; }
  .font-display { font-family:'Space Grotesk',sans-serif; }
  .font-mono { font-family:'JetBrains Mono',monospace; }
  #stage { position:relative; width:${WIDTH}px; height:${HEIGHT}px; overflow:hidden; font-family:'Inter',sans-serif; }
  .bg { position:absolute; inset:0; z-index:0; background:${gradient}; }
  .logo-glow { position:absolute; inset:-18%; z-index:0; background-position:center; background-repeat:no-repeat; background-size:60%; opacity:.65;
    ${logoBlurredDataUrl ? `background-image:url(${logoBlurredDataUrl});` : ""} }
  .dark-overlay { position:absolute; inset:0; z-index:0;
    background: linear-gradient(180deg, rgba(6,7,10,.34) 0%, rgba(6,7,10,.18) 30%, rgba(6,7,10,.34) 68%, rgba(6,7,10,.62) 100%),
                radial-gradient(circle at 50% 45%, transparent 0%, transparent 42%, rgba(4,5,8,.25) 100%); }
  .grain { position:absolute; inset:0; z-index:1; opacity:.04; mix-blend-mode:overlay; background-image:url("${NOISE_SVG}"); }
  .vignette { position:absolute; inset:0; z-index:1; background:linear-gradient(180deg, rgba(0,0,0,.25) 0%, transparent 16%, transparent 76%, rgba(0,0,0,.42) 100%); }
  .content { position:relative; z-index:10; width:100%; height:100%; display:flex; flex-direction:column; }
  .topbar { display:flex; align-items:center; padding:44px 44px 0; }
  .logo-box { width:76px; height:76px; border-radius:20px; background:rgba(255,255,255,.08); border:1px solid rgba(255,255,255,.1); display:flex; align-items:center; justify-content:center; overflow:hidden; flex-shrink:0; }
  .logo-fallback { font-family:'Space Grotesk',sans-serif; font-weight:700; font-size:30px; color:rgba(255,255,255,.8); }
  .watermark { position:absolute; transform:translate(-50%,-50%); z-index:12; }
  .watermark span { font-family:'Space Grotesk',sans-serif; font-size:24px; font-weight:500; color:rgba(255,255,255,.55); letter-spacing:.02em; }
  .main { flex:1; display:flex; flex-direction:column; justify-content:center; padding:32px 44px 44px; min-height:0; }
  .title { font-family:'Space Grotesk',sans-serif; font-weight:700; color:#fff; line-height:1.18; letter-spacing:-.01em; font-size:${ts.title}px; }
  .sub { color:rgba(255,255,255,.62); line-height:1.5; margin-top:16px; font-size:${ts.sub}px; }
  .editor-window { margin-top:20px; width:100%; background:#0B0D12; border:1px solid rgba(255,255,255,.08); border-radius:20px; overflow:hidden; box-shadow:0 18px 40px -18px rgba(0,0,0,.6); }
  .editor-titlebar { display:flex; align-items:center; gap:8px; padding:20px 26px; border-bottom:1px solid rgba(255,255,255,.06); }
  .editor-titlebar .dot { width:14px; height:14px; border-radius:50%; }
  .editor-titlebar .filename { margin-left:12px; font-family:'JetBrains Mono',monospace; font-size:15px; color:rgba(255,255,255,.3); letter-spacing:.03em; }
  .editor-body { padding:20px 26px; display:flex; gap:20px; }
  .editor-body .gutter { color:#333a4a; text-align:right; font-family:'JetBrains Mono',monospace; flex-shrink:0; }
  .editor-body .code { color:rgba(110,231,183,.95); font-family:'JetBrains Mono',monospace; white-space:pre-wrap; word-break:break-word; flex:1; }
  .checklist { margin-top:24px; width:100%; background:#161A23; border:1px solid #1A1F29; border-radius:20px; padding:22px 26px; }
  .checklist-title { font-family:'JetBrains Mono',monospace; font-size:15px; text-transform:uppercase; letter-spacing:.18em; color:rgba(255,255,255,.3); margin-bottom:14px; }
  .checklist ul { list-style:none; }
  .checklist li { display:flex; align-items:center; }
  .ring { border-radius:50%; border:2px solid rgba(255,255,255,.2); display:flex; align-items:center; justify-content:center; flex-shrink:0; }
  .ring.done { border-color:rgba(52,211,153,.9); background:rgba(52,211,153,.14); }
  .item-text { white-space:nowrap; overflow:hidden; text-overflow:ellipsis; font-family:'Inter',sans-serif; }
</style></head>
<body>
  <div id="stage">
    <div class="bg"></div>
    <div class="logo-glow"></div>
    <div class="dark-overlay"></div>
    <div class="grain"></div>
    <div class="vignette"></div>
    <div class="content">
      <div class="topbar">${logoHtml}</div>
      ${watermarkHtml}
      <div class="main">
        <div class="title">${escapeHtml(slide.titulo)}</div>
        ${slide.sub ? `<div class="sub">${escapeHtml(slide.sub)}</div>` : ""}
        ${codeBlock}
        ${checklistHtml}
      </div>
    </div>
  </div>
</body></html>`;
}

/* ===================== PUPPETEER (chromium sin cabeza) ===================== */
let _browserPromise = null;
async function getBrowser() {
  if (_browserPromise) return _browserPromise;
  const puppeteer = require("puppeteer-core");
  _browserPromise = (async () => {
    let executablePath = process.env.PUPPETEER_EXECUTABLE_PATH || null;
    let args = ["--no-sandbox", "--disable-setuid-sandbox"];
    if (!executablePath) {
      // Entorno serverless (Cloud Functions / Cloud Run): usa Chromium empaquetado.
      const chromium = require("@sparticuz/chromium");
      executablePath = await chromium.executablePath();
      args = chromium.args;
    }
    return puppeteer.launch({ executablePath, args, headless: true, defaultViewport: { width: WIDTH, height: HEIGHT } });
  })();
  return _browserPromise;
}

// Renderiza una diapositiva a un Buffer PNG de 1080x1920.
async function renderSlideToPng(slide, ctx) {
  const html = buildSlideHtml(slide, ctx);
  const browser = await getBrowser();
  const page = await browser.newPage();
  try {
    await page.setViewport({ width: WIDTH, height: HEIGHT, deviceScaleFactor: 1 });
    await page.setContent(html, { waitUntil: "networkidle0" });
    // Pequeño margen para que las fuentes de Google Fonts terminen de aplicarse.
    await page.evaluate(() => document.fonts.ready);
    const el = await page.$("#stage");
    const buffer = await el.screenshot({ type: "png" });
    return buffer;
  } finally {
    await page.close();
  }
}

async function closeBrowser() {
  if (_browserPromise) {
    const browser = await _browserPromise;
    await browser.close();
    _browserPromise = null;
  }
}
function buildSubtitleHtml(text) {
  const safe = escapeHtml(text || "");
  return `<!DOCTYPE html>
<html><head><meta charset="UTF-8" />
<link href="https://fonts.googleapis.com/css2?family=Inter:wght@600;700&display=swap" rel="stylesheet">
<style>
  * { box-sizing:border-box; margin:0; padding:0; }
  html,body { width:${WIDTH}px; height:${HEIGHT}px; background:transparent; }
  .wrap { position:relative; width:${WIDTH}px; height:${HEIGHT}px; }
  .sub {
    position:absolute; left:50%; top:86%; transform:translate(-50%,-50%);
    max-width:920px; text-align:center; font-family:'Inter',sans-serif;
    font-weight:600; font-size:42px; line-height:1.35; color:#fff;
    -webkit-text-stroke:3px black; paint-order:stroke fill;
    background:rgba(0,0,0,.45); padding:18px 26px; border-radius:14px;
  }
</style></head>
<body><div class="wrap"><div class="sub">${safe}</div></div></body></html>`;
}

// Devuelve un PNG transparente 1080x1920 con solo el texto posicionado.
async function renderSubtitlePng(text) {
  const browser = await getBrowser();
  const page = await browser.newPage();
  try {
    await page.setViewport({ width: WIDTH, height: HEIGHT, deviceScaleFactor: 1 });
    await page.setContent(buildSubtitleHtml(text), { waitUntil: "networkidle0" });
    await page.evaluate(() => document.fonts.ready);
    return await page.screenshot({ type: "png", omitBackground: true });
  } finally {
    await page.close();
  }
}

// Quema el subtítulo directamente sobre el PNG de la diapositiva (composición
// en Node con sharp), para no tener que duplicar streams en el filtro de ffmpeg.
async function compositeSubtitle(slidePngBuffer, subtitlePngBuffer) {
  return sharp(slidePngBuffer)
    .composite([{ input: subtitlePngBuffer, top: 0, left: 0 }])
    .png()
    .toBuffer();
}

module.exports = {
  WIDTH,
  HEIGHT,
  extractPalette,
  generateBlurredLogoDataUrl,
  renderSlideToPng,
  closeBrowser,
  renderSubtitlePng,
  compositeSubtitle,
};