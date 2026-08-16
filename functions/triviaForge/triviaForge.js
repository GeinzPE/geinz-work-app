// triviaForge.js
// TODO EN UN SOLO ARCHIVO — exporta { triviaForge, ...helpers } para usar en tu index.js
// y en telegramBot.js (que reutiliza estas funciones directamente, sin pasar por HTTP).
//
//   const { triviaForge } = require("./triviaForge");
//   exports.triviaForge = triviaForge;
//
// Dependencias en package.json: "sharp", "firebase-admin"
//
// FLUJO:
//   1) Toma una foto RANDOM de Storage de la carpeta que el usuario elija:
//      problematics_es/ | problematics_en/ | motivaciones_es/ | motivaciones_en/
//   2) La difumina/edita/le pone la tarjeta de trivia + marca de agua
//   3) Sube la foto ya editada a Storage en OUTPUT_FOLDER (imagen_fb_editada/)
//   4) Responde con la trivia + la URL de la foto editada (o el PNG binario si lo pides)
//
// Funciona en español o inglés según el parámetro "lang": "es" | "en" (default "es").
//
// CARPETAS DE IMÁGENES FUENTE (nuevo):
//   Hay 2 carpetas nada más en Storage, una por categoría (NO se separan por idioma,
//   las mismas imágenes sirven para trivia en ES y EN):
//     problematics/
//     motivaciones/
//   Cuando subes una imagen nueva (uploadSourceImage), se convierte a WEBP y se
//   recorta siempre a formato 9:16 (1080x1920, "reel"), sin importar el ratio
//   final que se use al momento de generar la trivia.
//
// VARIABLES DE ENTORNO:
//   API_KEYO_OPEN_IA     -> tu API key de OpenAI
//   OPENAI_MODEL         -> default "gpt-4o-mini"
//   PRIVATEKEY_GEMINI    -> tu API key de Gemini
//   GEMINI_MODEL         -> default "gemini-2.0-flash"
//   DEFAULT_PROVIDER     -> "openai" | "gemini" (default "openai")
//
//   STORAGE_BUCKET_MAIN  -> nombre del bucket (opcional, usa el bucket default si no lo pones)
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
//   "sourceCategory": "problematics" | "motivaciones",   // NUEVO: de qué carpeta sacar la imagen
//   "titulo": "...", "codigo": "linea1\nlinea2", "pregunta": "...?",  // opcional: modo manual, salta IA
//   "imageUrl": "https://...",                 // opcional: si NO lo mandas, se toma una RANDOM de Storage
//   "zoom": 100, "posX": 50, "posY": 50, "brightness": 100, "blur": 8, "overlayOpacity": 60,
//   "watermarkUrl": "https://.../logo.png",    // opcional: URL directa de logo
//   "useLogo": true,                           // opcional: usa el logo guardado en Storage (problematics/logo.webp)
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

/* ============================================================
   1.1) CATEGORÍAS DE IMÁGENES FUENTE (NUEVO)
============================================================ */
const IMAGE_CATEGORIES = {
  problematics: { es: "Problemáticas", en: "Problematics" },
  motivaciones: { es: "Motivación", en: "Motivation" }
};
const IMAGE_CATEGORY_KEYS = Object.keys(IMAGE_CATEGORIES); // ["problematics", "motivaciones"]

function normalizeSourceCategory(input) {
  const v = (input || "").toString().toLowerCase();
  return IMAGE_CATEGORY_KEYS.includes(v) ? v : "problematics";
}

// Carpeta resultante en Storage para una categoría (UNA sola carpeta por categoría,
// no se separa por idioma: las mismas imágenes sirven para ES y EN), ej: "problematics/"
function sourceFolder(category) {
  const cat = normalizeSourceCategory(category);
  return `${cat}/`;
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

    ratio: body.ratio === "1-1" ? "1-1" : (body.ratio === "9-16" ? "9-16" : ENV.DEFAULT_RATIO),

    sourceCategory: normalizeSourceCategory(body.sourceCategory),

    imageUrl: body.imageUrl || body.imagenUrl || null, // si falta, se usa random de Storage

    zoom: clamp(body.zoom ?? 100, 100, 250),
    posX: clamp(body.posX ?? 50, 0, 100),
    posY: clamp(body.posY ?? 50, 0, 100),
    brightness: clamp(body.brightness ?? ENV.DEFAULT_BRIGHTNESS, 40, 160),
    blur: clamp(body.blur ?? ENV.DEFAULT_BLUR_PX, 2, 20),
    overlayOpacity: clamp(body.overlayOpacity ?? ENV.DEFAULT_OVERLAY_OPACITY, 30, 85),

    watermarkUrl: body.watermarkUrl || null,
    useLogo: body.useLogo === true || body.useLogo === "true",
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
    rubroLabel: { prog: "</> PROGRAMACIÓN", math: "∑ MATEMÁTICA" },
    rubroName: { prog: "Programación", math: "Matemática" },
    dificultadLabel: { basic: "Básico", intermediate: "Intermedio", advanced: "Avanzado" },
    cta: "▼ Deja tu respuesta en los comentarios",
    defaultTitulo: "Reto rápido",
    defaultPregunta: "¿Cuál es la respuesta?"
  },
  en: {
    rubroLabel: { prog: "</> PROGRAMMING", math: "∑ MATH" },
    rubroName: { prog: "Programming", math: "Math" },
    dificultadLabel: { basic: "Basic", intermediate: "Intermediate", advanced: "Advanced" },
    cta: "▼ Leave your answer in the comments",
    defaultTitulo: "Quick challenge",
    defaultPregunta: "What's the answer?"
  }
};

const HASHTAGS = {
  es: {
    prog: "#programacion #python #codigo #desarrolladores #tech",
    math: "#matematicas #reto #logica #aprende #tech"
  },
  en: {
    prog: "#coding #python #programming #developers #tech",
    math: "#math #challenge #logic #learn #tech"
  }
};

function buildHashtags(lang, rubroKey) {
  const l = lang === "en" ? "en" : "es";
  const r = rubroKey === "math" ? "math" : "prog";
  return HASHTAGS[l][r];
}
/* ============================================================
   3) IA — OpenAI / Gemini (prompt bilingue)
============================================================ */
function buildSystemPrompt(lang, rubroKey, rubroName, dificultadLabel) {
  const isMath = rubroKey === "math";

  if (lang === "en") {
    if (isMath) {
      return `Generate a VERY SHORT ${rubroName} trivia at ${dificultadLabel} level, for a small social-media card. ` +
        `Strict length rules: "titulo" max 5 words, no trailing period, no quotes. "codigo" must contain 2 to 4 lines of PURE MATH ONLY: numbers and arithmetic/algebra/geometry expressions or equations using symbols like + - × ÷ = √ ² ³ π, fractions, or short word problems. ` +
        `ABSOLUTELY FORBIDDEN in "codigo": any programming syntax — no print(), no function calls, no semicolons, no variable assignments with code style, no quotes, no brackets/parentheses used as code, no words like "print", "console", "return", "function". It must read like something written on a chalkboard, not a script. Use "\\n" to separate lines. ` +
        `"pregunta" max 10 words, always ends with a question mark, and does not repeat the title. ` +
        `No theoretical paragraphs or explanations of any kind: only the math expression/equation and the final question (e.g. 'What is the result?', 'Solve for x'). ` +
        `Respond ONLY in JSON with structure: {"titulo": "...", "codigo": "...", "pregunta": "..."}. All three fields must be written in English. ` +
        `No text outside the JSON, no markdown code fences (no \`\`\`).`;
    }
    return `Generate a VERY SHORT ${rubroName} trivia at ${dificultadLabel} level, for a small social-media card with a code-terminal aesthetic. ` +
      `Strict length rules: "titulo" max 5 words, no trailing period, no quotes. "codigo" is a code snippet or expression of 4 to 6 lines MAXIMUM, no comments, no explanations or text outside the code; use "\\n" to separate lines. "pregunta" max 10 words, always ends with a question mark, and does not repeat the title. ` +
      `No theoretical paragraphs, extra context, or explanations of any kind: only the code/expression snippet and the final question ` +
      `(e.g. 'What does this print?', 'Where is the bug?', 'What is the result?'). ` +
      `Respond ONLY in JSON with structure: {"titulo": "...", "codigo": "...", "pregunta": "..."}. All three fields must be written in English. ` +
      `No text outside the JSON, no markdown code fences (no \`\`\`).`;
  }

  if (isMath) {
    return `Genera una trivia MUY CORTA de ${rubroName} en nivel ${dificultadLabel}, para una tarjeta pequeña de red social. ` +
      `Reglas estrictas de longitud: "titulo" máximo 5 palabras, sin punto final, sin comillas. "codigo" debe tener de 2 a 4 líneas de MATEMÁTICA PURA ÚNICAMENTE: números y expresiones o ecuaciones de aritmética/álgebra/geometría usando símbolos como + - × ÷ = √ ² ³ π, fracciones, o un problema corto en palabras. ` +
      `PROHIBIDO TOTALMENTE en "codigo": cualquier sintaxis de programación — nada de print(), nada de llamadas a funciones, sin punto y coma, sin asignaciones de variables al estilo código, sin comillas, sin paréntesis usados como código, sin palabras como "print", "console", "return", "function". Debe leerse como algo escrito en una pizarra, no como un script. Usa "\\n" para separar líneas. ` +
      `"pregunta" máximo 10 palabras, siempre termina en signo de interrogación, y no repite el título. ` +
      `Prohibido incluir párrafos teóricos o explicaciones de ningún tipo: solo la expresión/ecuación matemática y la pregunta final (ej. '¿Cuál es el resultado?', 'Despeja x'). ` +
      `Responde ÚNICAMENTE en formato JSON con la estructura: {"titulo": "...", "codigo": "...", "pregunta": "..."}. Los tres campos deben estar en español. ` +
      `No incluyas texto adicional fuera del JSON, ni bloques de código markdown (sin \`\`\`).`;
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

// Si el modelo se equivoca y mete sintaxis de código en una trivia de matemática
// (ej. print(), function, ;, etc.), la limpiamos como red de seguridad extra.
function stripProgrammingSyntax(codigo) {
  return String(codigo)
    .split(/\r?\n/)
    .map((line) =>
      line
        .replace(/\b(print|console\.log|return|function|def|let|const|var)\s*\(/gi, "")
        .replace(/[;{}]/g, "")
        .replace(/\)\s*$/g, "")
        .trim()
    )
    .filter(Boolean)
    .join("\n");
}

function sanitizeTrivia(raw, lang, rubroKey) {
  const t = I18N[lang];
  let titulo = (raw.titulo || "").toString().trim() || t.defaultTitulo;
  let codigo = (raw.codigo || "").toString();
  let pregunta = (raw.pregunta || "").toString().trim();

  if (rubroKey === "math" && /\b(print|console\.log|function|def)\s*\(/i.test(codigo)) {
    codigo = stripProgrammingSyntax(codigo);
  }

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
  const prompt = buildSystemPrompt(lang, rubroKey, t.rubroName[rubroKey], t.dificultadLabel[dificultadKey]);
  const raw = provider === "gemini" ? await callGemini(prompt, model) : await callOpenAI(prompt, model);
  return sanitizeTrivia(raw, lang, rubroKey);
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
  const accent = isProg ? "#22d3ee" : "#c084fc";
  const accentDark = isProg ? "#0e7490" : "#7e22ce";
  const rubroLabel = t.rubroLabel[rubroKey];
  const dificultadLabel = t.dificultadLabel[dificultadKey];
  const pad = Math.round(width * 0.05);

  // --- Badges tipo "pill" arriba ---
  const badgeY = pad;
  const badgeH = Math.round(width * 0.055);
  const rubroFontSize = Math.max(14, Math.round(width * 0.026));
  const rubroPillW = Math.round(rubroLabel.length * rubroFontSize * 0.62 + 36);
  const dificPillW = Math.round(dificultadLabel.length * rubroFontSize * 0.62 + 36);

  // --- Tarjeta central ---
  const cardTop = badgeY + badgeH + Math.round(width * 0.06);
  const cardW = width - pad * 2;
  const codeLines = String(codigo).split(/\r?\n/).slice(0, 6);
  const codeFontSize = isProg
    ? Math.max(16, Math.round(width * 0.032))
    : Math.max(20, Math.round(width * 0.04));
  const codeLineHeight = Math.round(codeFontSize * 1.65);
  const headerH = isProg ? Math.round(width * 0.09) : Math.round(width * 0.075);
  const innerPadTop = isProg ? headerH + codeLineHeight * 0.9 : headerH + codeLineHeight * 0.55;
  const bodyH = codeLines.length * codeLineHeight + Math.round(width * 0.05);
  const cardH = headerH + bodyH;

  const codeAlignX = isProg ? pad + 22 : width / 2;
  const codeAnchor = isProg ? "start" : "middle";
  const codeTspans = codeLines.map((line, i) =>
    `<tspan x="${codeAlignX}" dy="${i === 0 ? innerPadTop : codeLineHeight}">${escapeXml(line)}</tspan>`
  ).join("");

  const preguntaTop = cardTop + cardH + Math.round(width * 0.09);
  const preguntaFontSize = Math.max(22, Math.round(width * 0.046));
  const preguntaLines = wrapText(pregunta, 28);
  const preguntaTspans = preguntaLines.map((line, i) =>
    `<tspan x="${width / 2}" dy="${i === 0 ? 0 : preguntaFontSize * 1.3}">${escapeXml(line)}</tspan>`
  ).join("");

  const tituloFontSize = Math.max(14, Math.round(width * 0.025));

  // --- CTA como botón/pill al final ---
  const ctaText = t.cta;
  const ctaFontSize = Math.round(width * 0.03);
  const ctaPillW = Math.min(cardW, Math.round(ctaText.length * ctaFontSize * 0.56 + 60));
  const ctaPillH = Math.round(ctaFontSize * 2.4);
  const ctaY = height - pad - ctaPillH;

  // Header de la tarjeta: estilo "terminal" para programación, estilo "pizarra" para matemática
  const headerContent = isProg
    ? `
    <circle cx="${pad + 26}" cy="${cardTop + headerH / 2}" r="7" fill="#f87171" opacity="0.85" />
    <circle cx="${pad + 48}" cy="${cardTop + headerH / 2}" r="7" fill="#facc15" opacity="0.85" />
    <circle cx="${pad + 70}" cy="${cardTop + headerH / 2}" r="7" fill="#4ade80" opacity="0.85" />
    <text x="${pad + 92}" y="${cardTop + headerH / 2 + 5}" class="mono" font-size="${tituloFontSize}" font-weight="600" fill="#cbd5e1">${escapeXml(titulo)}</text>`
    : `
    <text x="${width / 2}" y="${cardTop + headerH / 2 + 5}" text-anchor="middle" class="disp" font-size="${tituloFontSize + 2}" font-weight="700" letter-spacing="1" fill="${accent}">${escapeXml(titulo.toUpperCase())}</text>`;

  const codeFill = isProg ? "#6ee7b7" : "#f8fafc";
  const codeFontFamily = isProg ? "mono" : "disp";
  const codeFontWeight = isProg ? "400" : "700";

  return `
<svg width="${width}" height="${height}" viewBox="0 0 ${width} ${height}" xmlns="http://www.w3.org/2000/svg">
  <defs>
    <style>
      .mono { font-family: 'JetBrains Mono', 'Courier New', monospace; }
      .disp { font-family: 'Space Grotesk', 'Segoe UI', sans-serif; }
    </style>
    <linearGradient id="cardGrad" x1="0" y1="0" x2="0" y2="1">
      <stop offset="0%" stop-color="#1f2430" />
      <stop offset="100%" stop-color="#14161d" />
    </linearGradient>
    <linearGradient id="ctaGrad" x1="0" y1="0" x2="1" y2="0">
      <stop offset="0%" stop-color="${accent}" />
      <stop offset="100%" stop-color="${accentDark}" />
    </linearGradient>
    <filter id="softShadow" x="-20%" y="-20%" width="140%" height="140%">
      <feDropShadow dx="0" dy="8" stdDeviation="14" flood-color="#000000" flood-opacity="0.45" />
    </filter>
  </defs>

  <!-- Badges -->
  <rect x="${pad}" y="${badgeY}" width="${rubroPillW}" height="${badgeH}" rx="${badgeH / 2}" fill="${accent}" />
  <text x="${pad + rubroPillW / 2}" y="${badgeY + badgeH / 2 + rubroFontSize * 0.35}" text-anchor="middle" class="disp" font-size="${rubroFontSize}" font-weight="700" fill="#0b0f14">${escapeXml(rubroLabel)}</text>

  <rect x="${width - pad - dificPillW}" y="${badgeY}" width="${dificPillW}" height="${badgeH}" rx="${badgeH / 2}" fill="rgba(255,255,255,0.12)" stroke="rgba(255,255,255,0.25)" />
  <text x="${width - pad - dificPillW / 2}" y="${badgeY + badgeH / 2 + rubroFontSize * 0.35}" text-anchor="middle" class="disp" font-size="${rubroFontSize}" font-weight="600" fill="#ffffff">${escapeXml(dificultadLabel)}</text>

  <!-- Tarjeta -->
  <g filter="url(#softShadow)">
    <rect x="${pad}" y="${cardTop}" width="${cardW}" height="${cardH}" rx="22" fill="url(#cardGrad)" stroke="${accent}" stroke-opacity="0.35" stroke-width="1.5" />
    <rect x="${pad}" y="${cardTop}" width="${cardW}" height="${headerH}" rx="22" fill="rgba(255,255,255,0.04)" />
    <rect x="${pad}" y="${cardTop + headerH - 22}" width="${cardW}" height="22" fill="rgba(255,255,255,0.04)" />
    <rect x="${pad}" y="${cardTop + headerH - 2}" width="${cardW}" height="2" fill="${accent}" opacity="0.4" />
    ${headerContent}
    <text x="${codeAlignX}" y="${cardTop}" text-anchor="${codeAnchor}" class="${codeFontFamily}" font-size="${codeFontSize}" font-weight="${codeFontWeight}" fill="${codeFill}">${codeTspans}</text>
  </g>

  <!-- Pregunta -->
  <text x="${width / 2}" y="${preguntaTop}" text-anchor="middle" class="disp" font-size="${preguntaFontSize}" font-weight="800" fill="#facc15">${preguntaTspans}</text>

  <!-- CTA como botón -->
  <rect x="${(width - ctaPillW) / 2}" y="${ctaY}" width="${ctaPillW}" height="${ctaPillH}" rx="${ctaPillH / 2}" fill="url(#ctaGrad)" filter="url(#softShadow)" />
  <text x="${width / 2}" y="${ctaY + ctaPillH / 2 + ctaFontSize * 0.35}" text-anchor="middle" class="disp" font-size="${ctaFontSize}" font-weight="800" fill="#0b0f14">${escapeXml(ctaText)}</text>
</svg>`;
}

/* ============================================================
   5) STORAGE — imágenes fuente por categoría, subida,
      subida de la foto editada, estadísticas, y listados para enviar
============================================================ */
function getBucket() {
  return ENV.STORAGE_BUCKET ? admin.storage().bucket(ENV.STORAGE_BUCKET) : admin.storage().bucket();
}

async function fetchBuffer(url) {
  const res = await fetch(url);
  if (!res.ok) throw new Error(`No se pudo descargar la imagen: ${url} (HTTP ${res.status})`);
  return Buffer.from(await res.arrayBuffer());
}

// URL firmada de corta duración, suficiente para que Telegram la descargue al enviarla
async function getSignedUrl(file, minutes = 30) {
  const [url] = await file.getSignedUrl({ action: "read", expires: Date.now() + minutes * 60 * 1000 });
  return url;
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

// Toma una imagen random de la carpeta de la categoría (ej: "problematics/")
// Excluye "logo.webp" por si esa carpeta coincide con la carpeta del logo/marca de agua.
async function getRandomSourceImage(category = "problematics") {
  const bucket = getBucket();
  const prefix = sourceFolder(category);
  const [files] = await bucket.getFiles({ prefix });
  const imageFiles = files.filter((f) => /\.(jpe?g|png|webp)$/i.test(f.name) && !/\/logo\.webp$/i.test(f.name));
  if (!imageFiles.length) throw new Error(`No se encontraron imágenes en la carpeta "${prefix}".`);
  const chosen = imageFiles[Math.floor(Math.random() * imageFiles.length)];
  const [buffer] = await chosen.download();
  return { buffer, sourceName: chosen.name };
}

async function deleteSourceImage(sourceName) {
  if (!sourceName) return;
  const bucket = getBucket();
  await bucket.file(sourceName).delete().catch((e) => {
    console.error(`No se pudo borrar la imagen origen "${sourceName}":`, e.message);
  });
}

// Sube una imagen nueva (ej: la que el usuario mandó por Telegram) a la carpeta
// de la categoría elegida (sin separar por idioma). Siempre se convierte a WEBP
// y se recorta a 9:16 (1080x1920, formato "reel"), sin importar el ratio final
// de generación.
async function uploadSourceImage(buffer, category) {
  const folder = sourceFolder(category);
  const optimized = await sharp(buffer)
    .resize(1080, 1920, { fit: "cover" })
    .webp({ quality: 82 })
    .toBuffer();

  const filename = `${folder}${Date.now()}-${Math.random().toString(36).slice(2, 8)}.webp`;
  const bucket = getBucket();
  const file = bucket.file(filename);
  await file.save(optimized, { metadata: { contentType: "image/webp" }, resumable: false });

  return { path: filename, sizeBytes: optimized.length, category: normalizeSourceCategory(category) };
}

// Cuenta cuántas imágenes hay y cuánto pesan, por cada carpeta de categoría
async function getStorageStats() {
  const bucket = getBucket();
  const stats = [];
  let totalFiles = 0;
  let totalBytes = 0;

  for (const category of IMAGE_CATEGORY_KEYS) {
    const prefix = sourceFolder(category);
    const [files] = await bucket.getFiles({ prefix });
    const imageFiles = files.filter((f) => /\.(jpe?g|png|webp)$/i.test(f.name) && !/\/logo\.webp$/i.test(f.name));
    const bytes = imageFiles.reduce((sum, f) => sum + Number(f.metadata?.size || 0), 0);
    stats.push({ category, count: imageFiles.length, bytes });
    totalFiles += imageFiles.length;
    totalBytes += bytes;
  }
  return { stats, totalFiles, totalBytes };
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

// Lista imágenes de una carpeta de categoría (para mandárselas al usuario por Telegram).
// random=true -> orden aleatorio (útil para "mándame 10 cualquiera").
async function listCategoryImages(category, { limit = 10, random = true } = {}) {
  const bucket = getBucket();
  const prefix = sourceFolder(category);
  const [files] = await bucket.getFiles({ prefix });
  let imageFiles = files.filter((f) => /\.(jpe?g|png|webp)$/i.test(f.name) && !/\/logo\.webp$/i.test(f.name));
  const total = imageFiles.length;

  if (random) {
    imageFiles = imageFiles.sort(() => Math.random() - 0.5);
  } else {
    imageFiles = imageFiles.sort((a, b) => new Date(b.metadata?.timeCreated || 0) - new Date(a.metadata?.timeCreated || 0));
  }
  const picked = imageFiles.slice(0, limit);
  const items = await Promise.all(picked.map(async (f) => ({ name: f.name, url: await getSignedUrl(f) })));
  return { total, items };
}

// Lista las publicaciones ya generadas (más recientes primero), para mandárselas al usuario
async function listOutputImages({ limit = 10 } = {}) {
  const bucket = getBucket();
  const [files] = await bucket.getFiles({ prefix: ENV.OUTPUT_FOLDER });
  let imageFiles = files.filter((f) => /\.(png|jpe?g|webp)$/i.test(f.name));
  const total = imageFiles.length;
  imageFiles = imageFiles.sort((a, b) => new Date(b.metadata?.timeCreated || 0) - new Date(a.metadata?.timeCreated || 0));
  const picked = imageFiles.slice(0, limit);
  const items = await Promise.all(picked.map(async (f) => ({ name: f.name, url: await getSignedUrl(f) })));
  return { total, items };
}

// Cuenta y pesa las publicaciones YA GENERADAS (carpeta de salida, imagen_fb_editada/
// por default). Cada trivia generada se guarda ahí automáticamente, así que esto
// siempre refleja lo que ya tienes creado, sin necesidad de Firestore.
async function getOutputStats() {
  const bucket = getBucket();
  const [files] = await bucket.getFiles({ prefix: ENV.OUTPUT_FOLDER });
  const imageFiles = files.filter((f) => /\.(png|jpe?g|webp)$/i.test(f.name));
  const bytes = imageFiles.reduce((sum, f) => sum + Number(f.metadata?.size || 0), 0);
  const lastFiles = imageFiles
    .slice()
    .sort((a, b) => new Date(b.metadata?.timeCreated || 0) - new Date(a.metadata?.timeCreated || 0))
    .slice(0, 5)
    .map((f) => ({ name: f.name, createdAt: f.metadata?.timeCreated || null }));
  return { count: imageFiles.length, bytes, lastFiles };
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

  // Marca de agua: se activa con una URL directa (watermarkUrl) O pidiendo el logo
  // guardado en Storage (useLogo). Antes, la rama de Storage nunca se ejecutaba
  // porque estaba anidada dentro de un "if (watermarkUrl)" — ya corregido aquí.
  if (params.watermarkUrl || params.useLogo) {
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
    sourceCategory: params.sourceCategory,
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
   8) HANDLER — esto es lo único que usas en tu index.js para el endpoint HTTP
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
      ? sanitizeTrivia(params, params.lang, params.rubroKey)
      : await generateTrivia(params);

    let sourceBuffer, sourceName = null;
    if (params.imageUrl) {
      sourceBuffer = await fetchBuffer(params.imageUrl);
    } else {
      const picked = await getRandomSourceImage(params.sourceCategory);
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

module.exports = {
  triviaForge,
  // Reutilizables por telegramBot.js (o cualquier otro consumidor interno)
  parseParams,
  generateTrivia,
  sanitizeTrivia,
  getRandomSourceImage,
  uploadSourceImage,
  getStorageStats,
  getOutputStats,
  listCategoryImages,
  listOutputImages,
  deleteSourceImage,
  buildImage,
  uploadEditedImage,
  I18N,
  IMAGE_CATEGORIES,
  IMAGE_CATEGORY_KEYS,
  normalizeSourceCategory,
  sourceFolder,
    buildHashtags,
};