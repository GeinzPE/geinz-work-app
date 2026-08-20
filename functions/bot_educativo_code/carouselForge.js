// carouselForge.js
// Lógica "de negocio" de Carousel Studio, portada 1:1 desde el HTML original:
// presets de temario, construcción de prompts, llamadas a Gemini/OpenAI,
// y armado del arreglo de diapositivas (portada + contenido + reto).
//
// Todas las claves de API se leen SIEMPRE de variables de entorno — nunca
// se piden ni se guardan por chat.

/* ===================== PRESETS (idénticos al HTML) ===================== */
const PRESETS = {
  python_basico: { label: "Python Básico", tech: "Python", items: ["Variables y tipos de datos", "Operadores", "Condicionales if/else", "Bucles for y while"] },
  python_intermedio: { label: "Python Intermedio", tech: "Python", items: ["Funciones y parámetros", "Listas y comprensión de listas", "Diccionarios y sets", "Manejo de excepciones"] },
  python_avanzado: { label: "Python Avanzado", tech: "Python", items: ["Decoradores", "Generadores y yield", "Programación asíncrona (async/await)", "Context managers (with)"] },
  sql: { label: "SQL Consultas", tech: "SQL", items: ["Sentencia SELECT", "Filtro WHERE", "Ordenamiento ORDER BY", "Agrupación GROUP BY"] },
  js: { label: "JavaScript Moderno", tech: "JavaScript", items: ["Let y Const", "Arrow Functions", "Template Literals", "Desestructuración"] },
  git: { label: "Git & GitHub", tech: "Git", items: ["git init / status", "git add / commit", "git branch / checkout", "git push / pull"] },
  html_css: { label: "HTML & CSS", tech: "HTML & CSS", items: ["Estructura semántica", "Selectores CSS", "Flexbox", "Grid Layout"] },
  react: { label: "React Básico", tech: "React", items: ["Componentes y JSX", "Props", "useState", "useEffect"] },
  ts: { label: "TypeScript", tech: "TypeScript", items: ["Tipos básicos", "Interfaces", "Types y Union", "Genéricos"] },
  node: { label: "Node.js & APIs", tech: "Node.js", items: ["Módulos y npm", "Servidor con Express", "Rutas y middlewares", "Peticiones async"] },
  mongodb: { label: "MongoDB / NoSQL", tech: "MongoDB", items: ["Documentos y colecciones", "Operaciones CRUD", "Filtros de consulta", "Agregaciones"] },
  poo: { label: "Programación Orientada a Objetos", tech: "POO", items: ["Clases y objetos", "Herencia", "Encapsulamiento", "Polimorfismo"] },
  docker: { label: "Docker Esencial", tech: "Docker", items: ["Imágenes vs contenedores", "Dockerfile básico", "docker build / run", "Volúmenes y redes"] },
  estructuras: { label: "Estructuras de Datos", tech: "Estructuras de Datos", items: ["Arrays y Listas", "Pilas (Stacks)", "Colas (Queues)", "Árboles binarios"] },
  bash: { label: "Bash & Terminal", tech: "Bash", items: ["Navegación de directorios", "Permisos de archivos", "Pipes y redirección", "Scripts básicos"] },
  regex: { label: "Expresiones Regulares", tech: "Regex", items: ["Caracteres literales y especiales", "Cuantificadores", "Grupos y capturas", "Lookahead / Lookbehind"] },
  seguridad_web: { label: "Seguridad Web Básica", tech: "Seguridad Web", items: ["Inyección SQL", "Cross-Site Scripting (XSS)", "Autenticación segura", "HTTPS y certificados"] },
};

// Orden en el que se muestran los botones del temario
const PRESET_ORDER = Object.keys(PRESETS);

/* ===================== ENV =====================
   Nombres tal cual existen ya en tu proyecto (no genéricos), para no tener
   que duplicar variables de entorno. */
const GEMINI_API_KEY = process.env.PRIVATEKEY_GEMINI || "";
const OPENAI_API_KEY = process.env.API_KEYO_OPEN_IA || "";
const AI_DEFAULT_PROVIDER = (process.env.AI_DEFAULT_PROVIDER || "gemini").toLowerCase();
const ELEVENLABS_API_KEY = process.env.CLAVE_API_ELEVENLABS_BOT_CREADOR_PROGRAMACION || "";
const ELEVENLABS_VOICE_ID = process.env.VOICE_ID_TELEGRAM || "";

function availableProviders() {
  const list = [];
  if (GEMINI_API_KEY) list.push("gemini");
  if (OPENAI_API_KEY) list.push("openai");
  return list;
}

function narrationEnabled() {
  return Boolean(ELEVENLABS_API_KEY && ELEVENLABS_VOICE_ID);
}

/* ===================== UTIL ===================== */
function trimText(str, maxLen) {
  const s = String(str ?? "").trim();
  return s.length > maxLen ? s.slice(0, maxLen - 1).trim() + "…" : s;
}
function trimCode(str, maxLines = 3, maxCharsPerLine = 30) {
  const lines = String(str ?? "").split("\n").slice(0, maxLines);
  return lines.map((l) => trimText(l, maxCharsPerLine)).join("\n");
}
function escapeHtml(str) {
  return String(str ?? "").replace(/[&<>"']/g, (c) => ({
    "&": "&amp;", "<": "&lt;", ">": "&gt;", '"': "&quot;", "'": "&#39;",
  }[c]));
}
function guessExt(tech) {
  const t = String(tech || "").toLowerCase();
  if (t.includes("python")) return "py";
  if (t.includes("sql")) return "sql";
  if (t.includes("javascript") || t.includes("js")) return "js";
  if (t.includes("git")) return "sh";
  return "txt";
}

/* ===================== PROMPTS (portados 1:1 del HTML) ===================== */
function buildSystemPrompt() {
  return `Eres un creador de contenido técnico para redes sociales (TikTok/Reels/Shorts), no un generador de fichas de referencia. Tu prioridad número uno es la RETENCIÓN: los primeros 1-2 segundos deciden si la persona se queda o se va, así que "intro_dialogo" y "caption" SIEMPRE deben empezar con un gancho — una pregunta directa, un error común, o una promesa concreta de lo que se va a aprender — nunca con saludos genéricos tipo "Hola" o "En este video". Explicas conceptos técnicos como lo haría un buen instructor: defines el concepto, muestras un ejemplo concreto y das un tip práctico para recordarlo, todo en tono cercano y directo, sin relleno. Respondes ÚNICAMENTE con un objeto JSON válido, compacto y sin texto adicional, sin markdown, sin backticks.`;
}

function buildJsonRulesBlock(tech) {
  return `Devuelve SOLO este objeto JSON (nada de texto fuera de él):
{
  "caption": "texto corto que EMPIECE con un gancho (pregunta, error común, o promesa de resultado), sin saludos, seguido de 2-3 hashtags relevantes, máximo 220 caracteres",
  "intro_dialogo": "guion de EXACTAMENTE 2 frases cortas para narrar la portada. La primera frase es el gancho (nunca un saludo). La segunda dice qué se va a aprender. Máximo 30 palabras en total.",
  "content_slides": [
    { "titulo": "título corto del punto", "explicacion": "explicación breve en una frase (máx 80 caracteres)", "codigo": "snippet de código real relacionado con \\"${tech}\\", MÁXIMO 3 líneas, cada línea máximo 28 caracteres, usa \\n para saltos de línea", "dialogo": "guion de EXACTAMENTE 3 a 4 frases cortas, tono de instructor cercano: (1) qué es este concepto en términos simples, (2) para qué sirve o cuándo se usa, con un ejemplo concreto de la vida real de programación, (3) un tip práctico para recordarlo. Máximo 55 palabras en total, sin relleno." }
  ],
  "reto": { "titulo": "título corto del reto", "enunciado": "consigna breve del ejercicio (máx 100 caracteres)", "codigo": "código base del reto, MÁXIMO 3 líneas, cada línea máximo 28 caracteres", "dialogo": "guion de EXACTAMENTE 2 a 3 frases: recuerda el concepto clave en una frase, dice qué debe hacer la persona, y la anima a pausar e intentarlo. Máximo 40 palabras en total." }
}

Reglas estrictas:
- GANCHO OBLIGATORIO en "intro_dialogo" y "caption": pregunta directa, error común, o promesa concreta. Prohibido abrir con saludos o "En este carrusel vamos a ver...".
- LÍMITES DE PALABRAS EN "dialogo": son estrictos porque el audio generado debe caber en el tiempo de lectura de la diapositiva sin sentirse atropellado ni alargarse de más. No los excedas.
- Cada "codigo" debe ser código real y útil relacionado con "${tech}", MÁXIMO 3 líneas y 28 caracteres por línea. Si no cabe, simplifica nombres y quita comentarios.
- Los campos visibles ("titulo", "explicacion", "codigo", "enunciado") deben ser extremadamente concisos.
- Los campos "dialogo" e "intro_dialogo" son guiones para ser LEÍDOS EN VOZ ALTA. Escribe SIEMPRE los números en palabras (ej. "tres" en vez de "3"), nunca dígitos ni símbolos raros ni abreviaturas, porque la voz IA los pronuncia mal.
- No repitas el nombre "${tech}" dentro de cada título.
- JSON compacto, sin espacios innecesarios, sin comentarios.`;
}

function buildUserPrompt({ tech, temarioMode, items, scope, singleIndex, libreTopic }) {
  if (temarioMode === "libre") {
    const topic = libreTopic || tech;
    return `Genera el contenido para un carrusel educativo sobre "${tech}", a partir de esta petición libre escrita por el usuario (puede ser un tema, una pregunta o un caso concreto):
"${topic}"

Interpreta la intención real del usuario y organiza el contenido en el mismo formato que usa esta app para temarios estructurados (una portada, varios puntos de contenido y un reto final). TÚ decides cuántos puntos de contenido son necesarios para explicar bien el tema: usa entre 3 y 7 elementos en "content_slides", ni más ni menos de lo que el tema realmente requiera. Cada punto debe cubrir un aspecto claro, distinto y en un orden lógico de aprendizaje.

${buildJsonRulesBlock(tech)}
- "content_slides" debe tener entre 3 y 7 elementos, decididos por ti según la complejidad del tema, cada uno con un "titulo" corto que sirva como punto de temario.`;
  }

  const scoped = scope === "single" ? [items[Math.min(singleIndex, items.length - 1)]] : items;
  return `Genera el contenido para un carrusel educativo sobre "${tech}".
El temario a cubrir en esta generación, en este orden exacto:
${scoped.map((it, i) => `${i + 1}. ${it}`).join("\n")}

${buildJsonRulesBlock(tech)}
- "content_slides" debe tener EXACTAMENTE ${scoped.length} elementos, uno por cada punto listado arriba, en el mismo orden.`;
}

/* ===================== LLAMADAS A LAS APIS DE IA ===================== */
async function callGemini(systemPrompt, userPrompt) {
  if (!GEMINI_API_KEY) throw new Error("Falta GEMINI_API_KEY en el entorno.");
  const url = `https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=${encodeURIComponent(GEMINI_API_KEY)}`;
  const body = {
    contents: [{ role: "user", parts: [{ text: userPrompt }] }],
    systemInstruction: { parts: [{ text: systemPrompt }] },
    generationConfig: { responseMimeType: "application/json", temperature: 0.6 },
  };
  const res = await fetch(url, { method: "POST", headers: { "Content-Type": "application/json" }, body: JSON.stringify(body) });
  if (!res.ok) {
    const errText = await res.text().catch(() => "");
    throw new Error(`Gemini API error (${res.status}): ${errText.slice(0, 200)}`);
  }
  const data = await res.json();
  const text = data?.candidates?.[0]?.content?.parts?.map((p) => p.text || "").join("") || "";
  if (!text) throw new Error("Gemini no devolvió contenido. Verifica la API Key o la cuota.");
  return { text, usage: data?.usageMetadata || null };
}

async function callOpenAI(systemPrompt, userPrompt) {
  if (!OPENAI_API_KEY) throw new Error("Falta OPENAI_API_KEY en el entorno.");
  const url = "https://api.openai.com/v1/chat/completions";
  const body = {
    model: "gpt-4o-mini",
    messages: [{ role: "system", content: systemPrompt }, { role: "user", content: userPrompt }],
    response_format: { type: "json_object" },
    temperature: 0.6,
  };
  const res = await fetch(url, {
    method: "POST",
    headers: { "Content-Type": "application/json", Authorization: `Bearer ${OPENAI_API_KEY}` },
    body: JSON.stringify(body),
  });
  if (!res.ok) {
    const errText = await res.text().catch(() => "");
    throw new Error(`OpenAI API error (${res.status}): ${errText.slice(0, 200)}`);
  }
  const data = await res.json();
  const text = data?.choices?.[0]?.message?.content || "";
  if (!text) throw new Error("OpenAI no devolvió contenido. Verifica la API Key o la cuota.");
  return { text, usage: data?.usage || null };
}

function safeParseJson(raw) {
  let cleaned = raw.trim();
  cleaned = cleaned.replace(/^```(json)?/i, "").replace(/```$/, "").trim();
  const firstBrace = cleaned.indexOf("{");
  const lastBrace = cleaned.lastIndexOf("}");
  if (firstBrace !== -1 && lastBrace !== -1) cleaned = cleaned.slice(firstBrace, lastBrace + 1);
  return JSON.parse(cleaned);
}

// provider: "gemini" | "openai"
async function generateCarouselContent(provider, params) {
  const systemPrompt = buildSystemPrompt();
  const userPrompt = buildUserPrompt(params);
  const result = provider === "openai" ? await callOpenAI(systemPrompt, userPrompt) : await callGemini(systemPrompt, userPrompt);
  const json = safeParseJson(result.text);
  return { json, usage: result.usage };
}

/* ===================== ARMADO DE DIAPOSITIVAS (idéntico a buildSlides) ===================== */
function buildSlides(json, { tech, items, scope, singleIndex }) {
  const isSingle = scope === "single";
  const idx = isSingle ? Math.min(singleIndex, items.length - 1) : null;
  const scopedItems = isSingle ? [items[idx]] : items;
  const contentSlides = Array.isArray(json.content_slides) ? json.content_slides : [];
  const reto = json.reto || {};

  const slides = [];

  slides.push({
    tipo: "portada",
    numero: 1,
    badge: isSingle ? "Punto destacado" : "Introducción",
    titulo: tech || "Tecnología",
    sub: isSingle ? trimText(scopedItems[0] || "", 60) : `Guía rápida · ${items.length} conceptos clave`,
    codigo: "",
    checkedCount: 0,
    dialogo: json.intro_dialogo || "",
  });

  scopedItems.forEach((item, i) => {
    const src = contentSlides[i] || {};
    const posInFull = isSingle ? idx : i;
    slides.push({
      tipo: "contenido",
      numero: i + 2,
      badge: isSingle ? `Punto ${posInFull + 1} de ${items.length}` : `Parte ${i + 1} de ${items.length}`,
      titulo: trimText(src.titulo || item, 46),
      sub: trimText(src.explicacion || "", 100),
      codigo: trimCode(src.codigo || ""),
      checkedCount: posInFull + 1,
      dialogo: src.dialogo || "",
    });
  });

  slides.push({
    tipo: "reto",
    numero: scopedItems.length + 2,
    badge: "Reto / Ejercicio",
    titulo: trimText(reto.titulo || "Ponlo en práctica", 46),
    sub: trimText(reto.enunciado || "", 120),
    codigo: trimCode(reto.codigo || ""),
    checkedCount: isSingle ? idx + 1 : items.length,
    dialogo: reto.dialogo || "",
  });

  return slides;
}

/* ===================== ELEVENLABS TTS ===================== */
async function callElevenLabsTTS(text) {
  if (!narrationEnabled()) throw new Error("ElevenLabs no está configurado en el entorno.");
  const res = await fetch(`https://api.elevenlabs.io/v1/text-to-speech/${ELEVENLABS_VOICE_ID}/with-timestamps`, {
    method: "POST",
    headers: { "Content-Type": "application/json", "xi-api-key": ELEVENLABS_API_KEY, Accept: "application/json" },
    body: JSON.stringify({
      text,
      model_id: "eleven_multilingual_v2",
      voice_settings: { stability: 0.5, similarity_boost: 0.75 },
    }),
  });
  if (!res.ok) {
    const errText = await res.text().catch(() => "");
    throw new Error(`ElevenLabs error (${res.status}): ${errText.slice(0, 200)}`);
  }
  const data = await res.json();
  if (!data.audio_base64) throw new Error("ElevenLabs no devolvió audio.");
  const buffer = Buffer.from(data.audio_base64, "base64");
  return { buffer, alignment: data.alignment || null };
}

// Genera narración para cada diapositiva que tenga "dialogo". Devuelve el
// mismo arreglo de slides con audioBuffer/audioAlignment agregados.
async function narrateSlides(slides, onProgress) {
  let totalChars = 0;
  for (let i = 0; i < slides.length; i++) {
    const slide = slides[i];
    if (!slide.dialogo) continue;
    if (onProgress) await onProgress(i + 1, slides.length);
    const { buffer, alignment } = await callElevenLabsTTS(slide.dialogo);
    slide.audioBuffer = buffer;
    slide.audioAlignment = alignment;
    totalChars += slide.dialogo.length;
  }
  return { slides, totalChars };
}

module.exports = {
  PRESETS,
  PRESET_ORDER,
  availableProviders,
  narrationEnabled,
  AI_DEFAULT_PROVIDER,
  trimText,
  trimCode,
  escapeHtml,
  guessExt,
  generateCarouselContent,
  buildSlides,
  narrateSlides,
  callElevenLabsTTS,
};
