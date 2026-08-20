// telegramBot.js
// Bot de Telegram para Carousel Studio (educación). Exporta { telegramWebhook }
// para tu index.js:
//
//   const { telegramWebhook } = require("./telegram-carousel-bot/telegramBot");
//   exports.telegramWebhook = onRequest({ timeoutSeconds: 540, memory: "2GiB" }, telegramWebhook);
//
// Nota de recursos: renderizar diapositivas (Puppeteer) y armar video (ffmpeg)
// consume bastante memoria/tiempo. En Cloud Functions usa como mínimo 2GiB y
// timeout largo, o mejor, despliega esto en Cloud Run.
//
// FLUJO POR BOTONES (nada de "escribe la config"; todo son taps):
//   /start -> "🎓 Generar carrusel"
//   -> elige un temario preset (o "✍️ Lista manual" o "🧠 Modo libre")
//   -> [si manual] escribe los puntos, uno por línea
//   -> [si libre]  escribe el tema libremente
//   -> elige alcance: Todo el temario / Un solo punto (si aplica, elige cuál)
//   -> confirma o escribe el nombre de la tecnología
//   -> [opcional] envía el logo como foto, o "Omitir"
//   -> [opcional] escribe marca de agua, o "Omitir"
//   -> elige motor de IA (solo los que tengan API key en el entorno)
//   -> [si ElevenLabs está configurado] ¿narrar con voz IA? Sí/No
//   -> 🚀 Generar -> manda cada diapositiva como foto + caption
//   -> botones: 📦 ZIP completo / 🎬 Generar video / 🔁 Generar otro
//   -> [video] pregunta si quieres música de fondo (envía audio u "Omitir")
//
// VARIABLES DE ENTORNO: ver .env.example (tokens, API keys de IA, ElevenLabs,
// bucket de Storage). Nada de esto se pide ni se guarda por chat.

const admin = require("firebase-admin");
if (!admin.apps.length) admin.initializeApp();

const archiver = require("archiver");
const forge = require("./carouselForge");
const renderer = require("./slideRenderer");
const videoBuilder = require("./videoBuilder");
const storage = require("./storage");

const TELEGRAM_TOKEN = process.env.TELEGRAM_BOT_TOKEN_GENERATOR_CODE || "";
const TELEGRAM_API = `https://api.telegram.org/bot${TELEGRAM_TOKEN}`;
const TELEGRAM_FILE_API = `https://api.telegram.org/file/bot${TELEGRAM_TOKEN}`;
const WEBHOOK_SECRET = process.env.TELEGRAM_BOT_TOKEN_GENERATOR_CODE_WEBHOOK_SECRET || null;
const ALLOWED_CHAT_IDS = (process.env.TELEGRAM_ALLOWED_CHAT_IDS || "")
  .split(",").map((s) => s.trim()).filter(Boolean);

/* ============================================================
   1) HELPERS DE TELEGRAM
============================================================ */
async function tg(method, payload) {
  const res = await fetch(`${TELEGRAM_API}/${method}`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(payload),
  });
  const data = await res.json().catch(() => ({}));
  if (!data.ok) console.error(`Telegram API error (${method}):`, data);
  return data;
}
const sendMessage = (chatId, text, extra = {}) =>
  tg("sendMessage", { chat_id: chatId, text, parse_mode: "HTML", ...extra });
const answerCallback = (id, text = "") =>
  tg("answerCallbackQuery", { callback_query_id: id, text, show_alert: false });
const sendPhoto = (chatId, url, caption) =>
  tg("sendPhoto", { chat_id: chatId, photo: url, caption, parse_mode: "HTML" });
const sendDocument = (chatId, url, caption) =>
  tg("sendDocument", { chat_id: chatId, document: url, caption, parse_mode: "HTML" });
const sendVideo = (chatId, url, caption) =>
  tg("sendVideo", { chat_id: chatId, video: url, caption, parse_mode: "HTML" });
const sendChatAction = (chatId, action) => tg("sendChatAction", { chat_id: chatId, action });

function isAllowed(chatId) {
  return !ALLOWED_CHAT_IDS.length || ALLOWED_CHAT_IDS.includes(String(chatId));
}

async function downloadTelegramFile(fileId) {
  const info = await tg("getFile", { file_id: fileId });
  const filePath = info?.result?.file_path;
  if (!filePath) throw new Error("No se pudo obtener el archivo desde Telegram.");
  const res = await fetch(`${TELEGRAM_FILE_API}/${filePath}`);
  if (!res.ok) throw new Error(`No se pudo descargar el archivo de Telegram (HTTP ${res.status}).`);
  return Buffer.from(await res.arrayBuffer());
}

async function fetchBuffer(url) {
  const res = await fetch(url);
  if (!res.ok) throw new Error(`No se pudo descargar ${url} (HTTP ${res.status}).`);
  return Buffer.from(await res.arrayBuffer());
}

/* ============================================================
   2) SESIÓN (Firestore) — igual patrón que triviaForge
============================================================ */
function sessionRef(chatId) {
  return admin.firestore().collection("carousel_sessions").doc(String(chatId));
}
async function setSession(chatId, data) {
  await sessionRef(chatId).set(
    { ...data, updatedAt: admin.firestore.FieldValue.serverTimestamp() },
    { merge: true },
  );
}
async function getSession(chatId) {
  const snap = await sessionRef(chatId).get();
  return snap.exists ? snap.data() : {};
}
async function resetSession(chatId) {
  await sessionRef(chatId).set({ state: "idle", updatedAt: admin.firestore.FieldValue.serverTimestamp() });
}

/* ============================================================
   3) TECLADOS
============================================================ */
function kbMainMenu() {
  return { inline_keyboard: [[{ text: "🎓 Generar carrusel educativo", callback_data: "menu:start" }]] };
}

function kbTemarioPresets() {
  const rows = [];
  const keys = forge.PRESET_ORDER;
  for (let i = 0; i < keys.length; i += 2) {
    const row = [keys[i], keys[i + 1]].filter(Boolean).map((k) => ({
      text: forge.PRESETS[k].label,
      callback_data: `preset:${k}`,
    }));
    rows.push(row);
  }
  rows.push([{ text: "✍️ Lista manual", callback_data: "preset:custom" }]);
  rows.push([{ text: "🧠 Modo libre (la IA decide)", callback_data: "preset:libre" }]);
  return { inline_keyboard: rows };
}

function kbScope(items) {
  return {
    inline_keyboard: [
      [{ text: "📚 Todo el temario", callback_data: "scope:all" }, { text: "🎯 Un solo punto", callback_data: "scope:single" }],
    ],
  };
}

function kbSingleItems(items) {
  const rows = items.map((it, i) => [{ text: `${i + 1}. ${it.slice(0, 50)}`, callback_data: `single:${i}` }]);
  return { inline_keyboard: rows };
}

function kbTechConfirm(defaultTech) {
  return {
    inline_keyboard: [
      [{ text: `✅ Usar "${defaultTech}"`, callback_data: "tech:default" }],
      [{ text: "✏️ Escribir otro nombre", callback_data: "tech:custom" }],
    ],
  };
}

function kbLogoChoice() {
  return { inline_keyboard: [[{ text: "🚫 Omitir logo", callback_data: "logo:skip" }]] };
}

function kbWatermarkChoice() {
  return {
    inline_keyboard: [
      [{ text: "✏️ Escribir marca de agua", callback_data: "wm:custom" }],
      [{ text: "🚫 Sin marca de agua", callback_data: "wm:skip" }],
    ],
  };
}

function kbProvider() {
  const providers = forge.availableProviders();
  const labels = { gemini: "✨ Gemini 2.5 Flash", openai: "🤖 GPT-4o mini" };
  return { inline_keyboard: [providers.map((p) => ({ text: labels[p], callback_data: `provider:${p}` }))] };
}

function kbNarration() {
  return {
    inline_keyboard: [[
      { text: "🔊 Sí, narrar con voz IA", callback_data: "narrate:yes" },
      { text: "🔇 No narrar", callback_data: "narrate:no" },
    ]],
  };
}

function kbPostGenerate() {
  return {
    inline_keyboard: [
      [{ text: "📦 Descargar ZIP", callback_data: "post:zip" }, { text: "🎬 Generar video", callback_data: "post:video" }],
      [{ text: "🔁 Generar otro carrusel", callback_data: "post:again" }],
    ],
  };
}

function kbMusicChoice() {
  return { inline_keyboard: [[{ text: "🚫 Sin música de fondo", callback_data: "music:skip" }]] };
}

/* ============================================================
   4) PIPELINE DE GENERACIÓN
============================================================ */
async function runGeneration(chatId, session) {
  await sendChatAction(chatId, "typing");
  await sendMessage(chatId, "⚙️ Consultando a la IA, un momento…");

  const items = session.items || [];
  const params = {
    tech: session.tech,
    temarioMode: session.temarioMode,
    items,
    scope: session.scope,
    singleIndex: session.singleIndex || 0,
    libreTopic: session.libreTopic || "",
  };

  let json;
  try {
    const result = await forge.generateCarouselContent(session.provider, params);
    json = result.json;
  } catch (err) {
    console.error(err);
    await sendMessage(chatId, "❌ No se pudo generar el contenido: " + err.message);
    await setSession(chatId, { state: "idle" });
    return;
  }

  // Si el temario era "modo libre", la IA decidió los puntos: los adoptamos
  // para que el checklist de cada diapositiva quede completo.
  let finalItems = items;
  if (session.temarioMode === "libre") {
    finalItems = Array.isArray(json.content_slides)
      ? json.content_slides.map((s) => s.titulo || "Punto")
      : ["Contenido"];
  }

  const slides = forge.buildSlides(json, {
    tech: session.tech,
    items: finalItems,
    scope: session.temarioMode === "libre" ? "all" : session.scope,
    singleIndex: session.singleIndex || 0,
  });

  // Narración (opcional)
  if (session.narrate) {
    await sendMessage(chatId, "🎙️ Generando narración con voz IA…");
    try {
      await forge.narrateSlides(slides, async (i, total) => {
        if (i % 2 === 0 || i === total) await sendChatAction(chatId, "record_voice");
      });
    } catch (err) {
      console.error(err);
      await sendMessage(chatId, "⚠️ No se pudo generar la narración, sigo sin audio: " + err.message);
    }
  }

  // Logo (si el usuario mandó uno)
  let logoBuffer = null;
  if (session.logoStoragePath) {
    try {
      logoBuffer = await fetchBuffer(session.logoUrl);
    } catch (e) { console.warn("No se pudo recuperar el logo", e); }
  }
  let colors = [];
  let logoDataUrl = null;
  let logoBlurredDataUrl = null;
  if (logoBuffer) {
    try {
      colors = await renderer.extractPalette(logoBuffer);
      logoDataUrl = `data:image/png;base64,${logoBuffer.toString("base64")}`;
      logoBlurredDataUrl = await renderer.generateBlurredLogoDataUrl(logoBuffer);
    } catch (e) { console.warn("No se pudo procesar el logo", e); }
  }

  await sendMessage(chatId, `🖼️ Renderizando ${slides.length} diapositivas…`);
  const ctx = {
    tech: session.tech,
    items: finalItems,
    logoDataUrl,
    logoBlurredDataUrl,
    colors,
    watermarkText: session.watermarkText || "",
    watermarkPos: session.watermarkPos || { xPct: 0.82, yPct: 0.078 },
  };

  const sessionSlides = [];
  for (let i = 0; i < slides.length; i++) {
    await sendChatAction(chatId, "upload_photo");
    const png = await renderer.renderSlideToPng(slides[i], ctx);
    const imgPath = `carousel_tmp/${chatId}/${storage.randomId()}_slide_${i}.png`;
    const imageUrl = await storage.uploadBuffer(png, imgPath, "image/png");

    let audioUrl = null, audioPath = null;
    if (slides[i].audioBuffer) {
      audioPath = `carousel_tmp/${chatId}/${storage.randomId()}_audio_${i}.mp3`;
      audioUrl = await storage.uploadBuffer(slides[i].audioBuffer, audioPath, "audio/mpeg");
    }

    sessionSlides.push({
      numero: slides[i].numero,
      titulo: slides[i].titulo,
      sub: slides[i].sub,
      dialogo: slides[i].dialogo,
      imagePath: imgPath, imageUrl,
      audioPath, audioUrl,
    });

    const caption = i === 0 ? (json.caption || "") : "";
    await sendPhoto(chatId, imageUrl, caption ? escapeHtmlCaption(caption) : undefined);
  }

  await setSession(chatId, {
    state: "post_generate",
    slides: sessionSlides,
    caption: json.caption || "",
    tech: session.tech,
  });
await renderer.closeBrowser();
  await sendMessage(chatId, "✅ ¡Carrusel generado! ¿Qué quieres hacer ahora?", { reply_markup: kbPostGenerate() });
}

function escapeHtmlCaption(str) {
  return String(str).replace(/[&<>]/g, (c) => ({ "&": "&amp;", "<": "&lt;", ">": "&gt;" }[c]));
}

async function buildAndSendZip(chatId, session) {
  await sendMessage(chatId, "📦 Armando el ZIP…");
  const slides = session.slides || [];
  if (!slides.length) { await sendMessage(chatId, "No hay diapositivas generadas todavía."); return; }

  const archive = archiver("zip", { zlib: { level: 9 } });
  const chunks = [];
  archive.on("data", (d) => chunks.push(d));
  const done = new Promise((resolve, reject) => {
    archive.on("end", resolve);
    archive.on("error", reject);
  });

  for (let i = 0; i < slides.length; i++) {
    const buf = await fetchBuffer(slides[i].imageUrl);
    archive.append(buf, { name: `slide-${String(i + 1).padStart(2, "0")}.png` });
  }
  archive.finalize();
  await done;

  const zipBuffer = Buffer.concat(chunks);
  const zipPath = `carousel_tmp/${chatId}/${storage.randomId()}_carrusel.zip`;
  const zipUrl = await storage.uploadBuffer(zipBuffer, zipPath, "application/zip");
  const techSlug = (session.tech || "carrusel").toLowerCase().replace(/[^a-z0-9]+/g, "-");
  await sendDocument(chatId, zipUrl, `${techSlug}-carrusel.zip`);
}

async function buildAndSendVideo(chatId, session, musicBuffer) {
  await sendMessage(chatId, "🎬 Generando video, esto puede tardar un poco…");
  const sessionSlides = session.slides || [];
  if (!sessionSlides.length) { await sendMessage(chatId, "No hay diapositivas generadas todavía."); return; }

  try {
    const slidesForVideo = [];
    for (const s of sessionSlides) {
      let pngBuffer = await fetchBuffer(s.imageUrl);
      let audioBuffer = null;
      if (s.audioUrl) audioBuffer = await fetchBuffer(s.audioUrl);
      if (s.dialogo) {
        const subtitlePngBuffer = await renderer.renderSubtitlePng(s.dialogo);
        pngBuffer = await renderer.compositeSubtitle(pngBuffer, subtitlePngBuffer);
      }
      slidesForVideo.push({ pngBuffer, audioBuffer, dialogo: s.dialogo, sub: s.sub });
    }

    await renderer.closeBrowser(); // <- nuevo: Chromium ya no se necesita, libéralo antes de ffmpeg

    const { buffer, durationSeconds } = await videoBuilder.buildVideo(slidesForVideo, {
      musicBuffer,
      musicVolume: 0.15,
      narrationVolume: 1,
      subtitlesEnabled: false, // <- ya están quemados en el PNG, ffmpeg no debe montar la capa aparte
      subtitlePos: { xPct: 0.5, yPct: 0.86 },
    });

    const videoPath = `carousel_tmp/${chatId}/${storage.randomId()}_video.mp4`;
    const videoUrl = await storage.uploadBuffer(buffer, videoPath, "video/mp4");
    await sendVideo(chatId, videoUrl, `Video generado (${durationSeconds.toFixed(1)}s)`);
  } catch (err) {
    console.error(err);
    await sendMessage(chatId, "❌ No se pudo generar el video: " + err.message);
  }
}
/* ============================================================
   5) ENRUTADOR DE UPDATES
============================================================ */
async function handleCallback(cq) {
  const chatId = cq.message.chat.id;
  const data = cq.data || "";
  await answerCallback(cq.id);
  if (!isAllowed(chatId)) return;
  const session = await getSession(chatId);

  if (data === "menu:start") {
    await setSession(chatId, {
      state: "await_preset", tech: null, items: [], temarioMode: null, scope: null,
      singleIndex: 0, libreTopic: "", logoUrl: null, logoStoragePath: null,
      watermarkText: "", provider: null, narrate: false, slides: [],
    });
    await sendMessage(chatId, "Elige un temario:", { reply_markup: kbTemarioPresets() });
    return;
  }

  if (data.startsWith("preset:")) {
    const key = data.split(":")[1];
    if (key === "custom") {
      await setSession(chatId, { state: "await_custom_items", temarioMode: "custom" });
      await sendMessage(chatId, "Escribe los puntos del temario, uno por línea. Ej:\nSentencia SELECT\nFiltro WHERE\nOrdenamiento ORDER BY");
      return;
    }
    if (key === "libre") {
      await setSession(chatId, { state: "await_libre_topic", temarioMode: "libre" });
      await sendMessage(chatId, "Describe libremente el tema (la IA decide cuántas diapositivas necesita). Ej:\n\"Explica un ataque de inyección SQL y cómo prevenirlo\"");
      return;
    }
    const preset = forge.PRESETS[key];
    if (!preset) return;
    await setSession(chatId, { state: "await_scope", temarioMode: key, items: preset.items, tech: preset.tech });
    await sendMessage(chatId, `Temario: <b>${preset.label}</b>\n\n¿Cubro todo el temario o solo un punto?`, { reply_markup: kbScope() });
    return;
  }

  if (data.startsWith("scope:")) {
    const scope = data.split(":")[1];
    if (scope === "single") {
      const items = session.items || [];
      await setSession(chatId, { state: "await_single_index", scope });
      await sendMessage(chatId, "¿Qué punto quieres cubrir?", { reply_markup: kbSingleItems(items) });
      return;
    }
    await setSession(chatId, { state: "await_tech_confirm", scope: "all" });
    await sendMessage(chatId, `Nombre de la tecnología para el carrusel:`, { reply_markup: kbTechConfirm(session.tech) });
    return;
  }

  if (data.startsWith("single:")) {
    const idx = Number(data.split(":")[1]);
    await setSession(chatId, { state: "await_tech_confirm", scope: "single", singleIndex: idx });
    await sendMessage(chatId, `Nombre de la tecnología para el carrusel:`, { reply_markup: kbTechConfirm(session.tech) });
    return;
  }

  if (data === "tech:default") {
    await setSession(chatId, { state: "await_logo" });
    await sendMessage(chatId, "Envíame el logo como foto (define el color del carrusel), o toca Omitir.", { reply_markup: kbLogoChoice() });
    return;
  }
  if (data === "tech:custom") {
    await setSession(chatId, { state: "await_tech_custom" });
    await sendMessage(chatId, "Escribe el nombre de la tecnología:");
    return;
  }

  if (data === "logo:skip") {
    await setSession(chatId, { state: "await_watermark" });
    await sendMessage(chatId, "¿Marca de agua?", { reply_markup: kbWatermarkChoice() });
    return;
  }

  if (data === "wm:skip") {
    await goToProviderOrNarration(chatId, { ...session, watermarkText: "" });
    return;
  }
  if (data === "wm:custom") {
    await setSession(chatId, { state: "await_watermark_text" });
    await sendMessage(chatId, "Escribe el texto de la marca de agua (ej. @tuusuario):");
    return;
  }

  if (data.startsWith("provider:")) {
    const provider = data.split(":")[1];
    await setSession(chatId, { provider });
    await goToNarrationOrGenerate(chatId, { ...session, provider });
    return;
  }

  if (data.startsWith("narrate:")) {
    const narrate = data.split(":")[1] === "yes";
    await setSession(chatId, { narrate, state: "generating" });
    const finalSession = await getSession(chatId);
    await runGeneration(chatId, finalSession);
    return;
  }

  if (data === "post:zip") {
    await buildAndSendZip(chatId, session);
    return;
  }
  if (data === "post:video") {
    await setSession(chatId, { state: "await_music" });
    await sendMessage(chatId, "¿Música de fondo para el video? Envíame un audio, o toca Omitir.", { reply_markup: kbMusicChoice() });
    return;
  }
  if (data === "music:skip") {
    await setSession(chatId, { state: "post_generate" });
    const finalSession = await getSession(chatId);
    await buildAndSendVideo(chatId, finalSession, null);
    return;
  }
  if (data === "post:again") {
    await handleCallback({ ...cq, data: "menu:start" });
    return;
  }
}

async function goToProviderOrNarration(chatId, session) {
  const providers = forge.availableProviders();
  if (providers.length === 0) {
    await sendMessage(chatId, "❌ No hay ningún motor de IA configurado (falta GEMINI_API_KEY u OPENAI_API_KEY en el entorno).");
    await setSession(chatId, { state: "idle" });
    return;
  }
  if (providers.length === 1) {
    await setSession(chatId, { provider: providers[0] });
    await goToNarrationOrGenerate(chatId, { ...session, provider: providers[0] });
    return;
  }
  await setSession(chatId, { state: "await_provider" });
  await sendMessage(chatId, "¿Qué motor de IA usamos?", { reply_markup: kbProvider() });
}

async function goToNarrationOrGenerate(chatId, session) {
  if (forge.narrationEnabled()) {
    await setSession(chatId, { state: "await_narration" });
    await sendMessage(chatId, "¿Narro el carrusel con voz IA (ElevenLabs)?", { reply_markup: kbNarration() });
    return;
  }
  await setSession(chatId, { narrate: false, state: "generating" });
  const finalSession = await getSession(chatId);
  await runGeneration(chatId, finalSession);
}

async function handleMessage(msg) {
  const chatId = msg.chat.id;
  if (!isAllowed(chatId)) return;
  const session = await getSession(chatId);
  const text = (msg.text || "").trim();

  // Foto: puede ser el logo o la música... la música va como audio/documento,
  // así que una foto siempre corresponde al paso de logo.
  if (Array.isArray(msg.photo) && msg.photo.length && session.state === "await_logo") {
    const best = msg.photo[msg.photo.length - 1];
    try {
      const buffer = await downloadTelegramFile(best.file_id);
      const logoPath = `carousel_tmp/${chatId}/${storage.randomId()}_logo.png`;
      const logoUrl = await storage.uploadBuffer(buffer, logoPath, "image/png");
      await setSession(chatId, { logoUrl, logoStoragePath: logoPath, state: "await_watermark" });
      await sendMessage(chatId, "✅ Logo recibido.\n\n¿Marca de agua?", { reply_markup: kbWatermarkChoice() });
    } catch (err) {
      console.error(err);
      await sendMessage(chatId, "❌ No se pudo procesar el logo: " + err.message);
    }
    return;
  }

  // Audio/documento: corresponde a la música de fondo del video.
  if ((msg.audio || msg.document) && session.state === "await_music") {
    const fileId = msg.audio ? msg.audio.file_id : msg.document.file_id;
    try {
      const buffer = await downloadTelegramFile(fileId);
      await setSession(chatId, { state: "post_generate" });
      const finalSession = await getSession(chatId);
      await buildAndSendVideo(chatId, finalSession, buffer);
    } catch (err) {
      console.error(err);
      await sendMessage(chatId, "❌ No se pudo procesar el audio: " + err.message);
    }
    return;
  }

  if (session.state === "await_custom_items") {
    const lines = text.split("\n").map((l) => l.trim()).filter(Boolean);
    if (!lines.length) { await sendMessage(chatId, "Escribe al menos un punto, uno por línea."); return; }
    await setSession(chatId, { items: lines, state: "await_tech_custom_first" });
    await sendMessage(chatId, "Escribe el nombre de la tecnología para este temario (ej. Kotlin):");
    return;
  }

  if (session.state === "await_tech_custom_first") {
    await setSession(chatId, { tech: text, state: "await_scope" });
    await sendMessage(chatId, "¿Cubro todo el temario o solo un punto?", { reply_markup: kbScope() });
    return;
  }

  if (session.state === "await_libre_topic") {
    await setSession(chatId, { libreTopic: text, state: "await_tech_custom_first" });
    await sendMessage(chatId, "Escribe el nombre de la tecnología o tema principal (ej. Seguridad Web):");
    return;
  }

  if (session.state === "await_tech_custom") {
    await setSession(chatId, { tech: text, state: "await_logo" });
    await sendMessage(chatId, "Envíame el logo como foto, o toca Omitir.", { reply_markup: kbLogoChoice() });
    return;
  }

  if (session.state === "await_watermark_text") {
    await setSession(chatId, { watermarkText: text });
    const finalSession = await getSession(chatId);
    await goToProviderOrNarration(chatId, finalSession);
    return;
  }

  // Cualquier otro mensaje -> menú principal
  await resetSession(chatId);
  await sendMessage(chatId, "👋 ¡Hola! Soy el generador de carruseles educativos.", { reply_markup: kbMainMenu() });
}

async function handleUpdate(update) {
  if (update.callback_query) return handleCallback(update.callback_query);
  if (update.message) return handleMessage(update.message);
}

/* ============================================================
   6) HANDLER HTTP
============================================================ */
async function telegramWebhook(req, res) {
  if (WEBHOOK_SECRET) {
    const incoming = req.get("X-Telegram-Bot-Api-Secret-Token");
    if (incoming !== WEBHOOK_SECRET) return res.status(401).send("unauthorized");
  }
  try {
    await handleUpdate(req.body || {});
  } catch (err) {
    console.error(err);
  }
  return res.status(200).send("ok");
}

module.exports = { telegramWebhook };
