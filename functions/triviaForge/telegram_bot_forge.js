// telegramBot.js
// Bot de Telegram para triviaForge. Exporta { telegramWebhook } para tu index.js:
//
//   const { telegramWebhook } = require("./triviaForge/telegramBot");
//   exports.telegramWebhook = onRequest({ timeoutSeconds: 60, memory: "512MiB" }, telegramWebhook);
//
// FLUJO DE GENERACIÓN:
//   Usuario escribe algo -> botón "🎨 Generar imagen"
//   -> elige categoría de trivia (Matemática/Programación, ES/EN)
//   -> elige formato (9:16 o 1:1)
//   -> elige de qué carpeta sacar la imagen fuente (Problemáticas / Motivación)
//   -> le preguntamos si quiere usar el logo/marca de agua
//   -> se genera la imagen (reutilizando triviaForge.js directamente, sin HTTP)
//   -> se envía la foto + la trivia (título, código, pregunta) como caption
//
// FLUJO DE SUBIDA DE IMÁGENES:
//   Usuario manda una FOTO directo al bot
//   -> el bot pregunta si quiere guardarla en Storage
//   -> si dice que sí, pregunta en qué carpeta (Problemáticas o Motivación — solo
//      2 carpetas, NO se separan por idioma, las mismas imágenes sirven para ES y EN)
//   -> la imagen se convierte a WEBP y se recorta a 9:16, y se sube a esa carpeta
//
// ESTADÍSTICAS Y ENVÍO:
//   "cuántas imágenes tengo" / "how many images" -> cantidad y peso (MB) de cada
//   una de las 2 carpetas fuente (problematics/, motivaciones/) y el total.
//   Después pregunta si quieres que te las envíe: eliges carpeta y cuántas
//   (botones 5/10/20/Todas, o escribes el número que quieras).
//
//   "cuántas publicaciones tengo" / "how many posts" -> cantidad y peso (MB) de
//   las trivias YA GENERADAS (se guardan solas en Storage cada vez que generas una).
//   También pregunta si quieres que te las envíe, mismo mecanismo de cantidad.
//
// Si escribe "programar", por ahora el bot avisa que esa parte (Cloud Scheduler,
// frecuencia, niveles, borrado de fotos usadas) viene después.
//
// VARIABLES DE ENTORNO:
//   TELEGRAM_BOT_TOKEN_GENERATOR -> token del bot (BotFather). ¡Nunca lo hardcodees ni lo pegues en chats!
//   TELEGRAM_WEBHOOK_SECRET      -> (recomendado) string secreta para validar que el request viene de Telegram
//   TELEGRAM_ALLOWED_CHAT_IDS    -> (opcional) lista separada por comas de chat_id permitidos, ej "12345,67890"
//
// CONFIGURAR EL WEBHOOK (una sola vez, tras desplegar la función):
//   curl -X POST "https://api.telegram.org/bot<TOKEN>/setWebhook" \
//     -H "Content-Type: application/json" \
//     -d '{"url":"https://<tu-url-de-cloud-function>","secret_token":"<TELEGRAM_WEBHOOK_SECRET>"}'

const admin = require("firebase-admin");
if (!admin.apps.length) admin.initializeApp();

const {
  parseParams,
  generateTrivia,
  getRandomSourceImage,
  uploadSourceImage,
  getStorageStats,
  getOutputStats,
  listCategoryImages,
  listOutputImages,
  buildImage,
  uploadEditedImage
} = require("./triviaForge");

const TELEGRAM_TOKEN = process.env.TELEGRAM_BOT_TOKEN_GENERATOR || "";
const TELEGRAM_API = `https://api.telegram.org/bot${TELEGRAM_TOKEN}`;
const TELEGRAM_FILE_API = `https://api.telegram.org/file/bot${TELEGRAM_TOKEN}`;
const WEBHOOK_SECRET = process.env.TELEGRAM_WEBHOOK_SECRET || null;
const ALLOWED_CHAT_IDS = (process.env.TELEGRAM_ALLOWED_CHAT_IDS || "")
  .split(",").map((s) => s.trim()).filter(Boolean);

/* ============================================================
   1) HELPERS DE TELEGRAM
============================================================ */
async function tg(method, payload) {
  const res = await fetch(`${TELEGRAM_API}/${method}`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(payload)
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

// Manda varias fotos en lotes de hasta 10 (límite de Telegram para sendMediaGroup)
const MAX_GROUP = 10;
async function sendPhotosBatch(chatId, items) {
  for (let i = 0; i < items.length; i += MAX_GROUP) {
    const batch = items.slice(i, i + MAX_GROUP);
    if (batch.length === 1) {
      await sendPhoto(chatId, batch[0].url);
    } else {
      await tg("sendMediaGroup", {
        chat_id: chatId,
        media: batch.map((it) => ({ type: "photo", media: it.url }))
      });
    }
  }
}

function isAllowed(chatId) {
  return !ALLOWED_CHAT_IDS.length || ALLOWED_CHAT_IDS.includes(String(chatId));
}

// Descarga un archivo de Telegram (por file_id) y devuelve el Buffer
async function downloadTelegramFile(fileId) {
  const info = await tg("getFile", { file_id: fileId });
  const filePath = info?.result?.file_path;
  if (!filePath) throw new Error("No se pudo obtener el archivo desde Telegram.");
  const res = await fetch(`${TELEGRAM_FILE_API}/${filePath}`);
  if (!res.ok) throw new Error(`No se pudo descargar el archivo de Telegram (HTTP ${res.status}).`);
  return Buffer.from(await res.arrayBuffer());
}

// Normaliza texto quitando acentos, para comparar frases del usuario
function normalizeText(s) {
  return String(s || "").toLowerCase().normalize("NFD").replace(/[\u0300-\u036f]/g, "").trim();
}

function isStatsQuery(text) {
  const n = normalizeText(text);
  return (
    n.includes("cuantas imagenes") ||
    n.includes("cuantas fotos") ||
    n.includes("how many images") ||
    n.includes("how many photos")
  );
}

function isPostsQuery(text) {
  const n = normalizeText(text);
  return (
    n.includes("cuantas publicaciones") ||
    n.includes("cuantos posts") ||
    n.includes("how many posts") ||
    n.includes("how many publications")
  );
}

/* ============================================================
   2) TECLADOS INLINE
============================================================ */
function kbGenerate() {
  return { inline_keyboard: [[{ text: "🎨 Generar imagen", callback_data: "menu:generate" }]] };
}

function kbCategories() {
  return {
    inline_keyboard: [
      [
        { text: "🧮 Matemática (ES)", callback_data: "cat:math:es" },
        { text: "🧮 Math (EN)", callback_data: "cat:math:en" }
      ],
      [
        { text: "💻 Programación (ES)", callback_data: "cat:prog:es" },
        { text: "💻 Programming (EN)", callback_data: "cat:prog:en" }
      ]
    ]
  };
}

function kbRatio(lang) {
  const square = lang === "en" ? "◻️ 1:1 (Square)" : "◻️ 1:1 (Cuadrado)";
  return {
    inline_keyboard: [[
      { text: "📱 9:16 (Reel)", callback_data: "ratio:9-16" },
      { text: square, callback_data: "ratio:1-1" }
    ]]
  };
}

function kbSourceCategory(lang) {
  const t1 = lang === "en" ? "Problematics" : "Problemáticas";
  const t2 = lang === "en" ? "Motivation" : "Motivación";
  // Solo 2 carpetas, sin separar por idioma: las mismas imágenes sirven para ES y EN.
  return {
    inline_keyboard: [[
      { text: `🗂️ ${t1}`, callback_data: "src:problematics" },
      { text: `🌟 ${t2}`, callback_data: "src:motivaciones" }
    ]]
  };
}

function kbWatermark(lang) {
  const yes = lang === "en" ? "✅ Yes, use logo" : "✅ Sí, usar logo";
  const no = lang === "en" ? "🚫 No watermark" : "🚫 Sin marca de agua";
  return { inline_keyboard: [[{ text: yes, callback_data: "wm:yes" }, { text: no, callback_data: "wm:no" }]] };
}

function kbSaveConfirm() {
  return {
    inline_keyboard: [[
      { text: "✅ Sí, guardar / Yes, save", callback_data: "save:yes" },
      { text: "🚫 No", callback_data: "save:no" }
    ]]
  };
}

function kbSaveCategory() {
  // Solo 2 carpetas, sin separar por idioma.
  return {
    inline_keyboard: [[
      { text: "🗂️ Problemáticas / Problematics", callback_data: "savecat:problematics" },
      { text: "🌟 Motivación / Motivation", callback_data: "savecat:motivaciones" }
    ]]
  };
}

function kbYesNo(yesData, noData) {
  return {
    inline_keyboard: [[
      { text: "✅ Sí / Yes", callback_data: yesData },
      { text: "🚫 No", callback_data: noData }
    ]]
  };
}

function kbSendCategory() {
  return {
    inline_keyboard: [
      [
        { text: "🗂️ Problemáticas", callback_data: "sendcat:problematics" },
        { text: "🌟 Motivación", callback_data: "sendcat:motivaciones" }
      ],
      [{ text: "📦 Ambas carpetas", callback_data: "sendcat:ambas" }]
    ]
  };
}

function kbSendQty() {
  return {
    inline_keyboard: [
      [
        { text: "5", callback_data: "sendqty:5" },
        { text: "10", callback_data: "sendqty:10" },
        { text: "20", callback_data: "sendqty:20" }
      ],
      [{ text: "📦 Todas (máx 30)", callback_data: "sendqty:all" }]
    ]
  };
}

/* ============================================================
   3) SESIÓN (Firestore) — Cloud Functions no mantiene estado
============================================================ */
function sessionRef(chatId) {
  return admin.firestore().collection("telegram_sessions").doc(String(chatId));
}

async function setSession(chatId, data) {
  await sessionRef(chatId).set(
    { ...data, updatedAt: admin.firestore.FieldValue.serverTimestamp() },
    { merge: true }
  );
}

async function getSession(chatId) {
  const snap = await sessionRef(chatId).get();
  return snap.exists ? snap.data() : null;
}

// --- Cola de fotos pendientes (para cuando mandan varias imágenes seguidas) ---
function getPendingPhotos(session) {
  return Array.isArray(session?.pendingPhotos) ? session.pendingPhotos : [];
}

async function pushPendingPhoto(chatId, fileId) {
  const session = await getSession(chatId);
  const queue = getPendingPhotos(session);
  queue.push(fileId);
  await setSession(chatId, { pendingPhotos: queue });
  return { queue, wasEmpty: queue.length === 1 };
}

async function shiftPendingPhoto(chatId) {
  const session = await getSession(chatId);
  const queue = getPendingPhotos(session);
  queue.shift();
  await setSession(chatId, { pendingPhotos: queue });
  return queue;
}

/* ============================================================
   4) FLUJO DE GENERACIÓN — reutiliza triviaForge.js directamente
============================================================ */
async function runGeneration(chatId, { rubroKey, lang, ratio, sourceCategory, useLogo }) {
  const waitMsg = lang === "en" ? "⚙️ Generating your trivia, one moment…" : "⚙️ Generando tu trivia, un momento…";
  await sendMessage(chatId, waitMsg);

  const params = parseParams({
    lang,
    rubro: rubroKey === "math" ? "Matemática" : "Programación",
    dificultad: "basic",
    ratio,
    sourceCategory,
    useLogo
  });

  const trivia = await generateTrivia(params);
  const { buffer: sourceBuffer } = await getRandomSourceImage(sourceCategory);
  const pngBuffer = await buildImage(params, trivia, sourceBuffer);
  const { url: outputUrl } = await uploadEditedImage(pngBuffer);

  const caption = `<b>${trivia.titulo}</b>\n\n<code>${trivia.codigo}</code>\n\n${trivia.pregunta}`;
  await sendPhoto(chatId, outputUrl, caption);
  await sendMessage(chatId, lang === "en" ? "Want another one?" : "¿Quieres otra?", { reply_markup: kbGenerate() });
}

/* ============================================================
   5) ESTADÍSTICAS DE STORAGE
============================================================ */
async function handleStatsQuery(chatId) {
  try {
    const { stats, totalFiles, totalBytes } = await getStorageStats();
    const lines = stats.map((s) => {
      const mb = (s.bytes / 1024 / 1024).toFixed(2);
      return `• <code>${s.category}</code>: ${s.count} imágenes — ${mb} MB`;
    });
    const totalMb = (totalBytes / 1024 / 1024).toFixed(2);
    const text =
      `📊 <b>Imágenes en Storage</b>\n\n${lines.join("\n")}\n\n` +
      `<b>Total:</b> ${totalFiles} imágenes — ${totalMb} MB`;
    await sendMessage(chatId, text);
  } catch (err) {
    console.error(err);
    await sendMessage(chatId, "❌ Error al consultar Storage: " + err.message);
  }
}

// Cuenta las trivias/publicaciones YA GENERADAS (se guardan solas en Storage
// cada vez que corres una generación, no hace falta nada extra para "guardarlas").
async function handlePostsQuery(chatId) {
  try {
    const { count, bytes } = await getOutputStats();
    const mb = (bytes / 1024 / 1024).toFixed(2);
    const text =
      count > 0
        ? `🖼️ <b>Publicaciones generadas</b>\n\nTienes <b>${count}</b> imágenes ya generadas, pesando en total <b>${mb} MB</b> en Storage.`
        : `🖼️ Todavía no tienes publicaciones generadas. Usa "🎨 Generar imagen" para crear la primera.`;
    await sendMessage(chatId, text);
  } catch (err) {
    console.error(err);
    await sendMessage(chatId, "❌ Error al consultar tus publicaciones: " + err.message);
  }
}

// Envía imágenes al usuario según lo que haya elegido: fuente (con categoría) o
// publicaciones ya generadas. Se usa tanto si eligió un botón de cantidad como
// si escribió el número directamente.
async function performSend(chatId, session, limit) {
  await sendMessage(chatId, "📤 Preparando el envío…");
  try {
    if (session.state === "await_send_qty_source") {
      const category = session.sendCategory;
      let items = [];
      if (category === "ambas") {
        const half = Math.ceil(limit / 2);
        const a = await listCategoryImages("problematics", { limit: half });
        const b = await listCategoryImages("motivaciones", { limit: limit - a.items.length });
        items = [...a.items, ...b.items].slice(0, limit);
      } else {
        const r = await listCategoryImages(category, { limit });
        items = r.items;
      }
      if (!items.length) {
        await sendMessage(chatId, "No encontré imágenes en esa carpeta.");
      } else {
        await sendPhotosBatch(chatId, items);
        await sendMessage(chatId, `✅ Te envié ${items.length} imagen(es).`);
      }
    } else if (session.state === "await_send_qty_posts") {
      const { items } = await listOutputImages({ limit });
      if (!items.length) {
        await sendMessage(chatId, "No tienes publicaciones generadas todavía.");
      } else {
        await sendPhotosBatch(chatId, items);
        await sendMessage(chatId, `✅ Te envié ${items.length} publicación(es).`);
      }
    }
  } catch (err) {
    console.error(err);
    await sendMessage(chatId, "❌ Error al enviar: " + err.message);
  }
  await setSession(chatId, { state: "idle" });
}

async function askSaveConfirmForFront(chatId, queue) {
  const extra = queue.length > 1 ? ` (1 de ${queue.length})` : "";
  await setSession(chatId, { state: "await_save_confirm" });
  await sendMessage(
    chatId,
    `📸 Recibí tu imagen${extra}. ¿Quieres guardarla en Firebase Storage? / Do you want to save it to Firebase Storage?`,
    { reply_markup: kbSaveConfirm() }
  );
}

/* ============================================================
   6) ENRUTADOR DE UPDATES
============================================================ */
async function handleUpdate(update) {
  // --- Botones (callback_query) ---
  if (update.callback_query) {
    const cq = update.callback_query;
    const chatId = cq.message.chat.id;
    const data = cq.data || "";
    await answerCallback(cq.id);

    if (!isAllowed(chatId)) return;

    if (data === "menu:generate") {
      await setSession(chatId, { state: "await_category" });
      await sendMessage(chatId, "Elige una categoría / Choose a category:", { reply_markup: kbCategories() });
      return;
    }

    if (data.startsWith("cat:")) {
      const [, rubroKey, lang] = data.split(":");
      await setSession(chatId, { state: "await_ratio", rubroKey, lang });
      const q = lang === "en" ? "What format do you want?" : "¿Qué formato quieres?";
      await sendMessage(chatId, q, { reply_markup: kbRatio(lang) });
      return;
    }

    if (data.startsWith("ratio:")) {
      const [, ratio] = data.split(":");
      const session = await getSession(chatId);
      if (!session || session.state !== "await_ratio") {
        await sendMessage(chatId, "Sesión expirada, escribe algo para empezar de nuevo. / Session expired, send any message to start over.");
        return;
      }
      await setSession(chatId, { state: "await_source", ratio });
      const q = session.lang === "en" ? "Which image folder should I use?" : "¿De qué carpeta saco la imagen?";
      await sendMessage(chatId, q, { reply_markup: kbSourceCategory(session.lang) });
      return;
    }

    if (data.startsWith("src:")) {
      const [, sourceCategory] = data.split(":");
      const session = await getSession(chatId);
      if (!session || session.state !== "await_source") {
        await sendMessage(chatId, "Sesión expirada, escribe algo para empezar de nuevo. / Session expired, send any message to start over.");
        return;
      }
      await setSession(chatId, { state: "await_watermark", sourceCategory });
      const q = session.lang === "en" ? "Do you want to use the logo / watermark?" : "¿Quieres usar el logo / marca de agua?";
      await sendMessage(chatId, q, { reply_markup: kbWatermark(session.lang) });
      return;
    }

    if (data.startsWith("wm:")) {
      const session = await getSession(chatId);
      if (!session || session.state !== "await_watermark") {
        await sendMessage(chatId, "Sesión expirada, escribe algo para empezar de nuevo. / Session expired, send any message to start over.");
        return;
      }
      const useLogo = data === "wm:yes";
      await setSession(chatId, { state: "generating", useLogo });
      try {
        await runGeneration(chatId, {
          rubroKey: session.rubroKey,
          lang: session.lang,
          ratio: session.ratio,
          sourceCategory: session.sourceCategory,
          useLogo
        });
        await setSession(chatId, { state: "idle" });
      } catch (err) {
        console.error(err);
        await sendMessage(chatId, "❌ Error: " + err.message);
        await setSession(chatId, { state: "idle" });
      }
      return;
    }

    // --- Confirmación de guardado de foto(s) subida(s) por el usuario ---
    if (data === "save:no") {
      const queue = await shiftPendingPhoto(chatId);
      if (queue.length) {
        await askSaveConfirmForFront(chatId, queue);
      } else {
        await setSession(chatId, { state: "idle" });
        await sendMessage(chatId, "Ok, no la guardé. 👍 / Ok, not saved. 👍");
      }
      return;
    }

    if (data === "save:yes") {
      const session = await getSession(chatId);
      const queue = getPendingPhotos(session);
      if (!queue.length) {
        await sendMessage(chatId, "No encontré la imagen, mándala de nuevo por favor. / I couldn't find the image, please send it again.");
        return;
      }
      await setSession(chatId, { state: "await_save_category" });
      await sendMessage(chatId, "¿En qué carpeta la guardo? / Which folder should I save it in?", { reply_markup: kbSaveCategory() });
      return;
    }

    if (data.startsWith("savecat:")) {
      const [, category] = data.split(":");
      const session = await getSession(chatId);
      const queueBefore = getPendingPhotos(session);
      if (!queueBefore.length || session.state !== "await_save_category") {
        await sendMessage(chatId, "Sesión expirada, manda la foto de nuevo. / Session expired, send the photo again.");
        return;
      }
      const fileId = queueBefore[0];
      try {
        const buffer = await downloadTelegramFile(fileId);
        const { path, sizeBytes } = await uploadSourceImage(buffer, category);
        const kb = (sizeBytes / 1024).toFixed(0);
        await sendMessage(chatId, `✅ Guardada en <code>${path}</code> (${kb} KB, WebP 9:16)`);
      } catch (err) {
        console.error(err);
        await sendMessage(chatId, "❌ Error al guardar la imagen: " + err.message);
      }
      const queueAfter = await shiftPendingPhoto(chatId);
      if (queueAfter.length) {
        await askSaveConfirmForFront(chatId, queueAfter);
      } else {
        await setSession(chatId, { state: "idle" });
      }
      return;
    }

    // --- Flujo de "quieres que te envíe las imágenes / publicaciones" ---
    if (data === "sendimgs:no" || data === "sendposts:no") {
      await setSession(chatId, { state: "idle" });
      await sendMessage(chatId, "Ok 👍");
      return;
    }

    if (data === "sendimgs:yes") {
      const session = await getSession(chatId);
      if (!session || session.state !== "await_send_confirm_source") {
        await sendMessage(chatId, "Sesión expirada, escribe algo para empezar de nuevo. / Session expired, send any message to start over.");
        return;
      }
      await setSession(chatId, { state: "await_send_category" });
      await sendMessage(chatId, "¿De qué carpeta? / Which folder?", { reply_markup: kbSendCategory() });
      return;
    }

    if (data.startsWith("sendcat:")) {
      const [, category] = data.split(":");
      const session = await getSession(chatId);
      if (!session || session.state !== "await_send_category") {
        await sendMessage(chatId, "Sesión expirada, escribe algo para empezar de nuevo. / Session expired, send any message to start over.");
        return;
      }
      await setSession(chatId, { state: "await_send_qty_source", sendCategory: category });
      await sendMessage(
        chatId,
        "¿Cuántas quieres que te envíe? Elige una opción o escribe un número (ej. 15). / How many? Pick an option or type a number.",
        { reply_markup: kbSendQty() }
      );
      return;
    }

    if (data === "sendposts:yes") {
      const session = await getSession(chatId);
      if (!session || session.state !== "await_send_confirm_posts") {
        await sendMessage(chatId, "Sesión expirada, escribe algo para empezar de nuevo. / Session expired, send any message to start over.");
        return;
      }
      await setSession(chatId, { state: "await_send_qty_posts" });
      await sendMessage(
        chatId,
        "¿Cuántas quieres que te envíe? Elige una opción o escribe un número (ej. 15). / How many? Pick an option or type a number.",
        { reply_markup: kbSendQty() }
      );
      return;
    }

    if (data.startsWith("sendqty:")) {
      const [, qtyRaw] = data.split(":");
      const session = await getSession(chatId);
      if (!session || (session.state !== "await_send_qty_source" && session.state !== "await_send_qty_posts")) {
        await sendMessage(chatId, "Sesión expirada, escribe algo para empezar de nuevo. / Session expired, send any message to start over.");
        return;
      }
      const limit = qtyRaw === "all" ? 30 : Number(qtyRaw);
      await performSend(chatId, session, limit);
      return;
    }

    return;
  }

  // --- Mensajes normales ---
  if (update.message) {
    const chatId = update.message.chat.id;
    if (!isAllowed(chatId)) return;

    // Foto enviada directo al bot (soporta varias seguidas / álbumes: se encolan)
    if (Array.isArray(update.message.photo) && update.message.photo.length) {
      const sizes = update.message.photo;
      const best = sizes[sizes.length - 1]; // Telegram manda de menor a mayor resolución
      const { queue, wasEmpty } = await pushPendingPhoto(chatId, best.file_id);
      // Si ya había una pregunta activa esperando respuesta sobre otra foto,
      // no mandamos otro mensaje: esta se procesa automáticamente cuando le
      // toque el turno. Si la cola estaba vacía, preguntamos de una vez.
      const session = await getSession(chatId);
      const alreadyAsking = session?.state === "await_save_confirm" || session?.state === "await_save_category";
      if (wasEmpty || !alreadyAsking) {
        await askSaveConfirmForFront(chatId, queue);
      }
      return;
    }

    const text = (update.message.text || "").trim();

    // Si está esperando una cantidad para enviar y el usuario escribió un número
    // directamente (en vez de tocar un botón), lo usamos igual.
    const activeSession = await getSession(chatId);
    if (
      activeSession &&
      (activeSession.state === "await_send_qty_source" || activeSession.state === "await_send_qty_posts") &&
      /^\d+$/.test(text)
    ) {
      const limit = Math.min(Math.max(Number(text), 1), 50);
      await performSend(chatId, activeSession, limit);
      return;
    }

    if (isStatsQuery(text)) {
      await handleStatsQuery(chatId);
      await setSession(chatId, { state: "await_send_confirm_source" });
      await sendMessage(chatId, "📤 ¿Quieres que te las envíe?", { reply_markup: kbYesNo("sendimgs:yes", "sendimgs:no") });
      return;
    }

    if (isPostsQuery(text)) {
      await handlePostsQuery(chatId);
      await setSession(chatId, { state: "await_send_confirm_posts" });
      await sendMessage(chatId, "📤 ¿Quieres que te envíe algunas?", { reply_markup: kbYesNo("sendposts:yes", "sendposts:no") });
      return;
    }

    if (normalizeText(text) === "programar") {
      await sendMessage(
        chatId,
        "⏳ La programación automática (frecuencia, niveles, carpeta fuente y borrado de fotos usadas) todavía no está lista — la agrego en el siguiente paso."
      );
      return;
    }

    await setSession(chatId, { state: "idle" });
    await sendMessage(chatId, "👋 ¡Hola! ¿Qué quieres hacer?", { reply_markup: kbGenerate() });
  }
}

/* ============================================================
   7) HANDLER HTTP — esto es lo que exportas en index.js
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
  // Siempre 200: si no respondes 200 rápido, Telegram reintenta el mismo update.
  return res.status(200).send("ok");
}

module.exports = { telegramWebhook };