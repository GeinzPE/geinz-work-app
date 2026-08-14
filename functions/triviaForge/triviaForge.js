// triviaForge.js
// TODO EN UN SOLO ARCHIVO — exporta { triviaForge } para usar en tu index.js:
//   const { triviaForge } = require("./triviaForge");
//   exports.triviaForge = triviaForge;
//
// Dependencias en package.json: "sharp", "firebase-admin"
//
// FLUJO:
//   1) Toma una foto RANDOM de Storage en la carpeta SOURCE_FOLDER (imagen_fb/)
//   2) La difumina/edita/le pone la tarjeta de trivia + marca de agua
//   3) Sube la foto ya editada a Storage en OUTPUT_FOLDER (imagen_fb_editada/)
//   4) Responde con la trivia + la URL de la foto editada (o el PNG binario si lo pides)
//
// Funciona en español o inglés según el parámetro "lang": "es" | "en" (default "es").
//
// VARIABLES DE ENTORNO:
//   API_KEYO_OPEN_IA     -> tu API key de OpenAI
//   OPENAI_MODEL         -> default "gpt-4o-mini"
//   PRIVATEKEY_GEMINI    -> tu API key de Gemini
//   GEMINI_MODEL         -> default "gemini-2.0-flash"
//   DEFAULT_PROVIDER     -> "openai" | "gemini" (default "openai")
//
//   STORAGE_BUCKET       -> nombre del bucket (opcional, usa el bucket default si no lo pones)
//   SOURCE_FOLDER        -> carpeta de fotos originales (default "imagen_fb/")
//   OUTPUT_FOLDER        -> carpeta donde se guarda la foto editada (default "imagen_fb_editada/")
//
//   SAVE_TO_FIRESTORE, FIRESTORE_COLLECTION       -> opcional (log de cada post)
//   DEFAULT_BLUR_PX, DEFAULT_BRIGHTNESS, DEFAULT_OVERLAY_OPACITY, DEFAULT_RATIO -> opcional
//
// POST body (JSON):
// {
//   "lang": "es" | "en",                       // default "es"
//   "provider": "openai" | "gemini",
//   "model": "gpt-4o-mini",
//   "rubro": "Programación"|"Matemática"|"Programming"|"Math",
//   "dificultad": "Básico"|"Intermedio"|"Avanzado"|"Basic"|"Intermediate"|"Advanced",
//   "ratio": "9-16" | "1-1",
//   "titulo": "...", "codigo": "linea1\nlinea2", "pregunta": "...?",  // opcional: modo manual, salta IA
//   "imageUrl": "https://...",                 // opcional: si NO lo mandas, se toma una RANDOM de Storage
//   "zoom": 100, "posX": 50, "posY": 50, "brightness": 100, "blur": 8, "overlayOpacity": 60,
//   "watermarkUrl": "https://.../logo.png",
//   "watermarkPosition": "bottom-right", "watermarkSize": 64, "watermarkRadius": 12, "watermarkOpacity": 100,
//   "saveToFirestore": false,
//   "responseFormat": "json" | "png"            // default "json" -> { trivia, imageUrl }
// }

const sharp = require("sharp");
const admin = require("firebase-admin");
if (!admin.apps.length) admin.initializeApp();

/* ============================================================
   1) CONFIG
============================================================ */
const ENV = {
  DEFAULT_PROVIDER: process.env.DEFAULT_PROVIDER || "openai",

  OPENAI_API_KEY: process.env.API_KEYO_OPEN_IA || "",
  OPENAI_MODEL: process.env.OPENAI_MODEL || "gpt-4o-mini",

  GEMINI_API_KEY: process.env.PRIVATEKEY_GEMINI || "",
  GEMINI_MODEL: process.env.GEMINI_MODEL || "gemini-2.0-flash",

  STORAGE_BUCKET: process.env.STORAGE_BUCKET_MAIN || null,
  SOURCE_FOLDER: process.env.SOURCE_FOLDER || "imagen_fb/",
  OUTPUT_FOLDER: process.env.OUTPUT_FOLDER || "imagen_fb_editada/",

  SAVE_TO_FIRESTORE: (process.env.SAVE_TO_FIRESTORE || "false") === "true",
  FIRESTORE_COLLECTION: process.env.FIRESTORE_COLLECTION || "trivia_forge_posts",

  DEFAULT_BLUR_PX: Number(process.env.DEFAULT_BLUR_PX || 8),
  DEFAULT_BRIGHTNESS: Number(process.env.DEFAULT_BRIGHTNESS || 100),
  DEFAULT_OVERLAY_OPACITY: Number(process.env.DEFAULT_OVERLAY_OPACITY || 60),
  DEFAULT_RATIO: process.env.DEFAULT_RATIO || "9-16"
};

const clamp = (n, min, max) => Math.min(max, Math.max(min, Number(n)));

function normalizeRubro(input) {
  const v = (input || "").toString().toLowerCase();
  return v.startsWith("mat") ? "math" : "prog";
}
function normalizeDificultad(input) {
  const v = (input || "").toString().toLowerCase();
  if (v.startsWith("int")) return "intermediate";
  if (v.startsWith("adv") || v.startsWith("avan")) return "advanced";
  return "basic";
}

function parseParams(body) {
  body = body || {};
  return {
    lang: body.lang === "en" ? "en" : "es",
    provider: body.provider === "gemini" ? "gemini" : (body.provider === "openai" ? "openai" : ENV.DEFAULT_PROVIDER),
    model: body.model || null,

    titulo: body.titulo || null,
    codigo: body.codigo || null,
    pregunta: body.pregunta || null,
    rubroKey: normalizeRubro(body.rubro),
    dificultadKey: normalizeDificultad(body.dificultad),

    ratio: body.ratio === "1-1" ? "1-1" : ENV.DEFAULT_RATIO,

    imageUrl: body.imageUrl || body.imagenUrl || null, // si falta, se usa random de Storage

    zoom: clamp(body.zoom ?? 100, 100, 250),
    posX: clamp(body.posX ?? 50, 0, 100),
    posY: clamp(body.posY ?? 50, 0, 100),
    brightness: clamp(body.brightness ?? ENV.DEFAULT_BRIGHTNESS, 40, 160),
    blur: clamp(body.blur ?? ENV.DEFAULT_BLUR_PX, 2, 20),
    overlayOpacity: clamp(body.overlayOpacity ?? ENV.DEFAULT_OVERLAY_OPACITY, 30, 85),

    watermarkUrl: body.watermarkUrl || null,
    watermarkPosition: ["top-left", "top-right", "bottom-left", "bottom-right"].includes(body.watermarkPosition)
      ? body.watermarkPosition : "bottom-right",
    watermarkSize: clamp(body.watermarkSize ?? 72, 32, 140),
    watermarkRadius: clamp(body.watermarkRadius ?? 12, 0, 50),
    watermarkOpacity: clamp(body.watermarkOpacity ?? 100, 20, 100),

    saveToFirestore: body.saveToFirestore ?? ENV.SAVE_TO_FIRESTORE,
    responseFormat: body.responseFormat === "png" ? "png" : "json"
  };
}

/* ============================================================
   2) TRADUCCIONES (ES / EN)
============================================================ */
const I18N = {
  es: {
    rubroLabel: { prog: "💻 PROGRAMACIÓN", math: "🧮 MATEMÁTICA" },
    rubroName: { prog: "Programación", math: "Matemática" },
    dificultadLabel: { basic: "Básico", intermediate: "Intermedio", advanced: "Avanzado" },
    cta: "👇 Deja tu respuesta en los comentarios",
    defaultTitulo: "Reto rápido",
    defaultPregunta: "¿Cuál es la respuesta?"
  },
  en: {
    rubroLabel: { prog: "💻 PROGRAMMING", math: "🧮 MATH" },
    rubroName: { prog: "Programming", math: "Math" },
    dificultadLabel: { basic: "Basic", intermediate: "Intermediate", advanced: "Advanced" },
    cta: "👇 Leave your answer in the comments",
    defaultTitulo: "Quick challenge",
    defaultPregunta: "What's the answer?"
  }
};

/* ============================================================
   3) IA — OpenAI / Gemini (prompt bilingue)
============================================================ */
function buildSystemPrompt(lang, rubroName, dificultadLabel) {
  if (lang === "en") {
    return `Generate a VERY SHORT ${rubroName} trivia at ${dificultadLabel} level, for a small social-media card with a code-terminal aesthetic. ` +
      `Strict length rules: "titulo" max 5 words, no trailing period, no quotes. "codigo" is a code snippet or expression of 4 to 6 lines MAXIMUM, no comments, no explanations or text outside the code; use "\\n" to separate lines. "pregunta" max 10 words, always ends with a question mark, and does not repeat the title. ` +
      `No theoretical paragraphs, extra context, or explanations of any kind: only the code/expression snippet and the final question ` +
      `(e.g. 'What does this print?', 'Where is the bug?', 'What is the result?'). ` +
      `Respond ONLY in JSON with structure: {"titulo": "...", "codigo": "...", "pregunta": "..."}. All three fields must be written in English. ` +
      `No text outside the JSON, no markdown code fences (no \`\`\`).`;
  }
  return `Genera una trivia MUY CORTA de ${rubroName} en nivel ${dificultadLabel}, para una tarjeta pequeña de red social con estética de terminal de código. ` +
    `Reglas estrictas de longitud: "titulo" máximo 5 palabras, sin punto final, sin comillas. "codigo" es un fragmento de código o expresión de 4 a 6 líneas COMO MÁXIMO, sin comentarios, sin explicaciones ni texto fuera del código; usa "\\n" para separar líneas. "pregunta" máximo 10 palabras, siempre termina en signo de interrogación, y no repite el título. ` +
    `Prohibido incluir párrafos teóricos, contexto adicional o explicaciones de ningún tipo: solo el fragmento de código/expresión y la pregunta final ` +
    `(ej. '¿Qué imprime esto?', '¿Dónde está el error?', '¿Cuál es el resultado?'). ` +
    `Responde ÚNICAMENTE en formato JSON con la estructura: {"titulo": "...", "codigo": "...", "pregunta": "..."}. Los tres campos deben estar en español. ` +
    `No incluyas texto adicional fuera del JSON, ni bloques de código markdown (sin \`\`\`).`;
}

function extractJson(text) {
  if (!text) throw new Error("Respuesta vacía del modelo.");
  let cleaned = text.trim().replace(/^```json/i, "").replace(/^```/, "").replace(/```$/, "").trim();
  const start = cleaned.indexOf("{");
  const end = cleaned.lastIndexOf("}");
  if (start === -1 || end === -1) throw new Error("No se encontró un JSON en la respuesta del modelo.");
  return JSON.parse(cleaned.slice(start, end + 1));
}

async function callOpenAI(prompt, model) {
  if (!ENV.OPENAI_API_KEY) throw new Error("Falta API_KEYO_OPEN_IA en las variables de entorno.");
  const res = await fetch("https://api.openai.com/v1/chat/completions", {
    method: "POST",
    headers: { "Content-Type": "application/json", "Authorization": "Bearer " + ENV.OPENAI_API_KEY },
    body: JSON.stringify({
      model: model || ENV.OPENAI_MODEL,
      temperature: 0.9,
      messages: [
        { role: "system", content: prompt },
        { role: "user", content: "Genera la trivia ahora / Generate the trivia now." }
      ]
    })
  });
  if (!res.ok) {
    const err = await res.json().catch(() => ({}));
    throw new Error(err?.error?.message || `Error HTTP ${res.status} al llamar a OpenAI.`);
  }
  const data = await res.json();
  return extractJson(data?.choices?.[0]?.message?.content);
}

async function callGemini(prompt, model) {
  if (!ENV.GEMINI_API_KEY) throw new Error("Falta PRIVATEKEY_GEMINI en las variables de entorno.");
  const useModel = model || ENV.GEMINI_MODEL;
  const url = `https://generativelanguage.googleapis.com/v1beta/models/${encodeURIComponent(useModel)}:generateContent?key=${encodeURIComponent(ENV.GEMINI_API_KEY)}`;
  const res = await fetch(url, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({
      contents: [{ role: "user", parts: [{ text: prompt + "\n\nGenera la trivia ahora / Generate the trivia now." }] }],
      generationConfig: { temperature: 0.9, responseMimeType: "application/json" }
    })
  });
  if (!res.ok) {
    const err = await res.json().catch(() => ({}));
    throw new Error(err?.error?.message || `Error HTTP ${res.status} al llamar a Gemini.`);
  }
  const data = await res.json();
  return extractJson(data?.candidates?.[0]?.content?.parts?.[0]?.text);
}

function sanitizeTrivia(raw, lang) {
  const t = I18N[lang];
  let titulo = (raw.titulo || "").toString().trim() || t.defaultTitulo;
  let codigo = (raw.codigo || "").toString();
  let pregunta = (raw.pregunta || "").toString().trim();

  if (!pregunta) pregunta = t.defaultPregunta;
  if (!pregunta.includes("?")) pregunta += " " + t.defaultPregunta;

  let lines = codigo.split(/\r?\n/).map((l) => l.replace(/\s+$/, ""));
  if (lines.length > 6) lines = lines.slice(0, 6);
  codigo = lines.join("\n");

  if (titulo.length > 45) titulo = titulo.slice(0, 42).trim() + "...";
  if (pregunta.length > 110) pregunta = pregunta.slice(0, 107).trim() + "...";

  return { titulo, codigo, pregunta };
}

async function generateTrivia({ provider, model, lang, rubroKey, dificultadKey }) {
  const t = I18N[lang];
  const prompt = buildSystemPrompt(lang, t.rubroName[rubroKey], t.dificultadLabel[dificultadKey]);
  const raw = provider === "gemini" ? await callGemini(prompt, model) : await callOpenAI(prompt, model);
  return sanitizeTrivia(raw, lang);
}

/* ============================================================
   4) SVG — badges, tarjeta de código, pregunta, CTA (bilingue)
============================================================ */
function escapeXml(str) {
  return String(str)
    .replace(/&/g, "&amp;").replace(/</g, "&lt;").replace(/>/g, "&gt;")
    .replace(/"/g, "&quot;").replace(/'/g, "&apos;");
}

function wrapText(text, maxCharsPerLine) {
  const words = String(text).split(" ");
  const lines = [];
  let current = "";
  for (const w of words) {
    if ((current + " " + w).trim().length > maxCharsPerLine) {
      if (current) lines.push(current.trim());
      current = w;
    } else {
      current = (current + " " + w).trim();
    }
  }
  if (current) lines.push(current);
  return lines;
}

function buildOverlaySVG({ width, height, lang, rubroKey, dificultadKey, titulo, codigo, pregunta }) {
  const t = I18N[lang];
  const isProg = rubroKey === "prog";
  const accent = isProg ? "#22d3ee" : "#a78bfa";
  const rubroLabel = t.rubroLabel[rubroKey];
  const dificultadLabel = t.dificultadLabel[dificultadKey];
  const pad = Math.round(width * 0.045);

  const cardTop = pad + 40;
  const cardMaxHeight = Math.round(height * 0.42);
  const codeLines = String(codigo).split(/\r?\n/).slice(0, 6);
  const codeFontSize = Math.max(16, Math.round(width * 0.032));
  const codeLineHeight = Math.round(codeFontSize * 1.5);
  const codeHeaderH = Math.round(width * 0.09);
  const codeBodyH = Math.min(cardMaxHeight - codeHeaderH, codeLines.length * codeLineHeight + 24);
  const cardH = codeHeaderH + Math.max(codeBodyH, codeLineHeight + 24);
  const cardW = width - pad * 2;

  const codeTspans = codeLines.map((line, i) =>
    `<tspan x="${pad + 18}" dy="${i === 0 ? codeHeaderH + codeLineHeight * 0.9 : codeLineHeight}">${escapeXml(line)}</tspan>`
  ).join("");

  const preguntaTop = cardTop + cardH + Math.round(width * 0.06);
  const preguntaFontSize = Math.max(20, Math.round(width * 0.042));
  const preguntaLines = wrapText(pregunta, 30);
  const preguntaTspans = preguntaLines.map((line, i) =>
    `<tspan x="${width / 2}" dy="${i === 0 ? 0 : preguntaFontSize * 1.3}">${escapeXml(line)}</tspan>`
  ).join("");

  const tituloFontSize = Math.max(13, Math.round(width * 0.024));

  return `
<svg width="${width}" height="${height}" viewBox="0 0 ${width} ${height}" xmlns="http://www.w3.org/2000/svg">
  <defs>
    <style>
      .mono { font-family: 'JetBrains Mono', 'Courier New', monospace; }
      .disp { font-family: 'Space Grotesk', 'Segoe UI', sans-serif; }
    </style>
  </defs>
  <text x="${pad}" y="${pad + 18}" class="mono" font-size="${tituloFontSize + 2}" font-weight="700" fill="${accent}">${escapeXml(rubroLabel)}</text>
  <text x="${width - pad}" y="${pad + 18}" text-anchor="end" class="mono" font-size="${tituloFontSize + 2}" font-weight="600" fill="#ffffff">${escapeXml(dificultadLabel)}</text>
  <g>
    <rect x="${pad}" y="${cardTop}" width="${cardW}" height="${cardH}" rx="16" fill="#1e1e1e" stroke="rgba(255,255,255,0.1)" />
    <rect x="${pad}" y="${cardTop}" width="${cardW}" height="${codeHeaderH}" rx="16" fill="rgba(255,255,255,0.03)" />
    <rect x="${pad}" y="${cardTop + codeHeaderH - 16}" width="${cardW}" height="16" fill="rgba(255,255,255,0.03)" />
    <circle cx="${pad + 24}" cy="${cardTop + codeHeaderH / 2}" r="7" fill="#f87171" opacity="0.8" />
    <circle cx="${pad + 46}" cy="${cardTop + codeHeaderH / 2}" r="7" fill="#facc15" opacity="0.8" />
    <circle cx="${pad + 68}" cy="${cardTop + codeHeaderH / 2}" r="7" fill="#4ade80" opacity="0.8" />
    <text x="${pad + 90}" y="${cardTop + codeHeaderH / 2 + 5}" class="mono" font-size="${tituloFontSize}" font-weight="600" fill="#cbd5e1">${escapeXml(titulo)}</text>
    <text x="${pad}" y="${cardTop}" class="mono" font-size="${codeFontSize}" fill="#6ee7b7">${codeTspans}</text>
  </g>
  <text x="${width / 2}" y="${preguntaTop}" text-anchor="middle" class="disp" font-size="${preguntaFontSize}" font-weight="700" fill="#facc15">${preguntaTspans}</text>
  <text x="${width / 2}" y="${height - pad}" text-anchor="middle" class="disp" font-size="${Math.round(width * 0.028)}" font-weight="800" fill="#fbbf24">${escapeXml(t.cta)}</text>
</svg>`;
}

/* ============================================================
   5) STORAGE — foto random de entrada + subida de la foto editada
============================================================ */
function getBucket() {
  return ENV.STORAGE_BUCKET ? admin.storage().bucket(ENV.STORAGE_BUCKET) : admin.storage().bucket();
}

async function fetchBuffer(url) {
  const res = await fetch(url);
  if (!res.ok) throw new Error(`No se pudo descargar la imagen: ${url} (HTTP ${res.status})`);
  return Buffer.from(await res.arrayBuffer());
}

async function getWatermarkFromStorage(lang) {
  const bucket = getBucket();
  const folder = lang === "en" ? "problematicses/" : "problematics/";
  const filePath = `${folder}logo.webp`;
  const file = bucket.file(filePath);
  const [exists] = await file.exists();
  if (!exists) throw new Error(`No se encontró el logo en Storage: ${filePath}`);
  const [buffer] = await file.download();
  return buffer;
}


async function getRandomSourceImage() {
  const bucket = getBucket();
  const prefix = ENV.SOURCE_FOLDER;
  const [files] = await bucket.getFiles({ prefix });
  const imageFiles = files.filter((f) => /\.(jpe?g|png|webp)$/i.test(f.name));
  if (!imageFiles.length) throw new Error(`No se encontraron imágenes en el bucket, carpeta "${prefix}".`);
  const chosen = imageFiles[Math.floor(Math.random() * imageFiles.length)];
  const [buffer] = await chosen.download();
  return { buffer, sourceName: chosen.name };
}

async function uploadEditedImage(buffer) {
  const bucket = getBucket();
  const filename = `${ENV.OUTPUT_FOLDER}${Date.now()}-${Math.random().toString(36).slice(2, 8)}.png`;
  const file = bucket.file(filename);
  await file.save(buffer, { metadata: { contentType: "image/png" }, resumable: false });

  // Intenta URL pública; si el bucket no lo permite (uniform bucket-level access
  // sin acceso publico), cae a URL firmada por 7 dias.
  try {
    await file.makePublic();
    return { path: filename, url: `https://storage.googleapis.com/${bucket.name}/${filename}` };
  } catch (e) {
    const [signedUrl] = await file.getSignedUrl({
      action: "read",
      expires: Date.now() + 7 * 24 * 60 * 60 * 1000
    });
    return { path: filename, url: signedUrl };
  }
}

/* ============================================================
   6) IMAGE BUILDER — sharp: fondo difuminado + overlay + marca de agua
============================================================ */
const SIZES = { "9-16": { width: 1080, height: 1920 }, "1-1": { width: 1080, height: 1080 } };

async function prepareBackground(buffer, { width, height, zoom, posX, posY, brightness, blur }) {
  const scaledW = Math.round((width * zoom) / 100);
  const scaledH = Math.round((height * zoom) / 100);
  const resized = await sharp(buffer).resize(scaledW, scaledH, { fit: "cover" }).toBuffer();

  const left = Math.max(0, Math.min(scaledW - width, Math.round(((scaledW - width) * posX) / 100)));
  const top = Math.max(0, Math.min(scaledH - height, Math.round(((scaledH - height) * posY) / 100)));

  return sharp(resized)
    .extract({ left, top, width, height })
    .modulate({ brightness: brightness / 100 })
    .blur(Math.max(0.3, blur))
    .png()
    .toBuffer();
}

async function prepareWatermark(buffer, { size, radius, opacity }) {
  const rx = Math.round((size * radius) / 100);
  const maskSvg = `<svg width="${size}" height="${size}">
    <rect width="${size}" height="${size}" rx="${rx}" ry="${rx}" fill="white" opacity="${opacity / 100}" />
  </svg>`;
  return sharp(buffer)
    .resize(size, size, { fit: "cover" })
    .ensureAlpha()
    .composite([{ input: Buffer.from(maskSvg), blend: "dest-in" }])
    .png()
    .toBuffer();
}

function watermarkOffset(position, size, margin, width, height) {
  const top = position.startsWith("top") ? margin : height - size - margin;
  const left = position.endsWith("left") ? margin : width - size - margin;
  return { top, left };
}

async function buildImage(params, trivia, sourceBuffer) {
  const { width, height } = SIZES[params.ratio] || SIZES["9-16"];

  const bg = await prepareBackground(sourceBuffer, {
    width, height, zoom: params.zoom, posX: params.posX, posY: params.posY,
    brightness: params.brightness, blur: params.blur
  });

  const overlay = await sharp({
    create: { width, height, channels: 4, background: { r: 0, g: 0, b: 0, alpha: params.overlayOpacity / 100 } }
  }).png().toBuffer();

  const svgOverlay = buildOverlaySVG({
    width, height, lang: params.lang, rubroKey: params.rubroKey, dificultadKey: params.dificultadKey,
    titulo: trivia.titulo, codigo: trivia.codigo, pregunta: trivia.pregunta
  });

  const layers = [
    { input: overlay, top: 0, left: 0 },
    { input: Buffer.from(svgOverlay), top: 0, left: 0 }
  ];

  if (params.watermarkUrl) {
    const rawWm = params.watermarkUrl
      ? await fetchBuffer(params.watermarkUrl)
      : await getWatermarkFromStorage(params.lang);

    const wm = await prepareWatermark(rawWm, {
      size: params.watermarkSize,
      radius: 50, // siempre circular
      opacity: params.watermarkOpacity
    });
    const { top, left } = watermarkOffset(params.watermarkPosition, params.watermarkSize, 14, width, height);
    layers.push({ input: wm, top, left });
  }
  return sharp(bg).composite(layers).png().toBuffer();
}

/* ============================================================
   7) FIRESTORE (opcional)
============================================================ */
async function saveTriviaPost({ params, trivia, sourceName, outputPath, outputUrl }) {
  const db = admin.firestore();
  const ref = await db.collection(ENV.FIRESTORE_COLLECTION).add({
    lang: params.lang,
    provider: params.provider,
    rubro: params.rubroKey,
    dificultad: params.dificultadKey,
    ratio: params.ratio,
    titulo: trivia.titulo,
    codigo: trivia.codigo,
    pregunta: trivia.pregunta,
    sourceImage: sourceName || params.imageUrl || null,
    outputPath, outputUrl,
    createdAt: admin.firestore.FieldValue.serverTimestamp()
  });
  return ref.id;
}

/* ============================================================
   8) HANDLER — esto es lo único que usas en tu index.js
============================================================ */
async function triviaForge(req, res) {
  res.set("Access-Control-Allow-Origin", "*");
  if (req.method === "OPTIONS") {
    res.set("Access-Control-Allow-Methods", "POST");
    res.set("Access-Control-Allow-Headers", "Content-Type");
    return res.status(204).send("");
  }
  if (req.method !== "POST") {
    return res.status(405).json({ error: "Usa POST con un body JSON. / Use POST with a JSON body." });
  }

  const params = parseParams(req.body);

  try {
    const trivia = (params.titulo && params.codigo && params.pregunta)
      ? sanitizeTrivia(params, params.lang)
      : await generateTrivia(params);

    let sourceBuffer, sourceName = null;
    if (params.imageUrl) {
      sourceBuffer = await fetchBuffer(params.imageUrl);
    } else {
      const picked = await getRandomSourceImage();
      sourceBuffer = picked.buffer;
      sourceName = picked.sourceName;
    }

    const pngBuffer = await buildImage(params, trivia, sourceBuffer);

    const { path: outputPath, url: outputUrl } = await uploadEditedImage(pngBuffer);

    if (params.saveToFirestore) {
      await saveTriviaPost({ params, trivia, sourceName, outputPath, outputUrl });
    }

    if (params.responseFormat === "png") {
      res.set("Content-Type", "image/png");
      res.set("X-Image-Url", outputUrl);
      return res.status(200).send(pngBuffer);
    }

    return res.status(200).json({
      trivia,
      imageUrl: outputUrl,
      storagePath: outputPath,
      sourceImage: sourceName || params.imageUrl
    });

  } catch (err) {
    console.error(err);
    return res.status(500).json({ error: err.message || "Error inesperado. / Unexpected error." });
  }
}

module.exports = { triviaForge };