"use strict";

const { onSchedule } = require("firebase-functions/v2/scheduler");
const { onRequest } = require("firebase-functions/v2/https");
const admin = require("firebase-admin");

const db = admin.firestore();

const BOT_TOKEN = process.env.TELEGRAM_BOT_TOKEN_APRENDE_CODE;
const API_BASE = `https://api.telegram.org/bot${BOT_TOKEN}`;

const SCHEDULED_COLLECTION = "scheduled_publications";
const GENERATIONS_COLLECTION = "carousel_generations";
const SESSIONS_COLLECTION = "bot_sessions"; // estado conversacional por chat (ej: esperando nueva fecha)
const PAGE_SIZE = 5;
const GENERATIONS_FETCH_CAP = 300; // suficiente para filtrar/paginar en memoria sin necesitar índices compuestos
const { getTotalCredits, classifyKeys } = require("../granjabots/granjabots_elevenlabs");

// Lima (Perú) no tiene horario de verano: UTC-5 todo el año.
const LIMA_UTC_OFFSET_MS = 5 * 60 * 60 * 1000;

function buildKeysChartUrl(details) {
  const labels = details.map((d) => `Key ${d.index}`);
  const data = details.map((d) => (d.ok ? d.remaining : 0));
  const colors = details.map((d) => {
    if (!d.ok) return "#e74c3c"; // rojo: falló / inválida
    if (d.remaining <= 0) return "#f39c12"; // naranja: sin créditos
    return "#2ecc71"; // verde: sana
  });

  const chartConfig = {
    type: "bar",
    data: { labels, datasets: [{ label: "Créditos restantes", data, backgroundColor: colors }] },
    options: {
      title: { display: true, text: "Estado de API keys — ElevenLabs" },
      legend: { display: false },
    },
  };

  const encoded = encodeURIComponent(JSON.stringify(chartConfig));
  return `https://quickchart.io/chart?c=${encoded}&width=800&height=400&backgroundColor=white`;
}

/* ============================================================
   HELPERS DE RED / TELEGRAM
   ============================================================ */

function formatCreditsMessage(stats) {
  const pct = stats.totalLimit > 0 ? ((stats.totalUsed / stats.totalLimit) * 100).toFixed(1) : "0.0";
  let text =
    `🔊 <b>Créditos de ElevenLabs</b>\n\n` +
    `🧮 Total disponible: <b>${stats.totalRemaining.toLocaleString("es-PE")}</b> caracteres\n` +
    `📦 Límite total del pool: ${stats.totalLimit.toLocaleString("es-PE")} caracteres\n` +
    `📈 Usado: ${stats.totalUsed.toLocaleString("es-PE")} caracteres (${pct}%)\n` +
    `🔑 Keys activas: ${stats.workingKeys}/${stats.poolSize}\n`;

  text += `\n<b>Detalle por key:</b>\n`;
  stats.details.forEach((d) => {
    if (d.ok) {
      text += `#${d.index}: ${d.remaining.toLocaleString("es-PE")}/${d.characterLimit.toLocaleString("es-PE")}\n`;
    } else {
      text += `#${d.index}: ⚠️ falló (${d.statusCode})\n`;
    }
  });

  if (stats.failedKeys > 0) {
    text += `\n⚠️ ${stats.failedKeys} key(s) no respondieron (revisa logs).`;
  }
  return text;
}

async function sendCreditsInfo(chatId, messageId) {
  await sendChatAction(chatId, "typing");
  try {
    const stats = await getTotalCredits();
    const { toDelete, healthy } = classifyKeys(stats.details);

    const text = formatCreditsMessage(stats);
    const keyboard = {
      inline_keyboard: [
        [{ text: "🔄 Actualizar", callback_data: "check_credits" }],
        [{ text: "🏠 Menú", callback_data: "genpage:all:0" }],
      ],
    };

    if (messageId) await editMessageText(chatId, messageId, text, keyboard);
    else await sendMessage(chatId, text, keyboard);

    await sendChatAction(chatId, "upload_photo");
    const chartUrl = buildKeysChartUrl(stats.details);
    await sendPhoto(chatId, chartUrl, "📊 Créditos restantes por key");

    if (toDelete.length > 0) {
      const deleteList = toDelete.map((d) => `#${d.index} → ${d.reason}`).join("\n");
      await sendMessage(
        chatId,
        `🗑️ <b>Keys que puedes eliminar (${toDelete.length}):</b>\n${deleteList}\n\n` +
          `✅ Keys sanas: ${healthy.length}/${stats.poolSize}`,
      );
    } else {
      await sendMessage(chatId, `✅ Todas las keys están sanas (${healthy.length}/${stats.poolSize}). No hay ninguna para eliminar.`);
    }
  } catch (err) {
    console.error("Error consultando créditos de ElevenLabs:", err);
    await sendMessage(chatId, "⚠️ No se pudo consultar los créditos en este momento. Intenta de nuevo más tarde.");
  }
}

async function tgCall(method, payload, { retries = 2, timeoutMs = 20000 } = {}) {
  let lastError;
  for (let attempt = 0; attempt <= retries; attempt++) {
    const controller = new AbortController();
    const timer = setTimeout(() => controller.abort(), timeoutMs);
    try {
      const res = await fetch(`${API_BASE}/${method}`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(payload),
        signal: controller.signal,
      });
      clearTimeout(timer);
      const data = await res.json();
      if (!data.ok) {
        console.error(`Telegram API error en ${method}:`, data);
        return data;
      }
      return data;
    } catch (err) {
      clearTimeout(timer);
      lastError = err;
      console.warn(`Fallo de red en ${method} (intento ${attempt + 1}/${retries + 1}):`, err.message || err);
      if (attempt < retries) {
        await sleep(500 * (attempt + 1));
      }
    }
  }
  return { ok: false, error: String(lastError?.message || lastError || "Error de red desconocido") };
}

function sleep(ms) {
  return new Promise((resolve) => setTimeout(resolve, ms));
}

function sendMessage(chatId, text, replyMarkup) {
  return tgCall("sendMessage", {
    chat_id: chatId,
    text,
    parse_mode: "HTML",
    disable_web_page_preview: true,
    reply_markup: replyMarkup || undefined,
  });
}

function sendVideo(chatId, videoUrl, caption, replyMarkup) {
  return tgCall(
    "sendVideo",
    {
      chat_id: chatId,
      video: videoUrl,
      caption: (caption || "").slice(0, 1024),
      parse_mode: "HTML",
      supports_streaming: true,
      reply_markup: replyMarkup || undefined,
    },
    { retries: 1, timeoutMs: 45000 },
  );
}

function sendDocument(chatId, fileUrl, caption, replyMarkup) {
  return tgCall(
    "sendDocument",
    {
      chat_id: chatId,
      document: fileUrl,
      caption: (caption || "").slice(0, 1024),
      parse_mode: "HTML",
      reply_markup: replyMarkup || undefined,
    },
    { retries: 1, timeoutMs: 45000 },
  );
}

function sendMediaGroupChunk(chatId, photoUrls, captionForFirst) {
  const media = photoUrls.map((url, i) => ({
    type: "photo",
    media: url,
    ...(i === 0 && captionForFirst ? { caption: captionForFirst.slice(0, 1024), parse_mode: "HTML" } : {}),
  }));
  return tgCall("sendMediaGroup", { chat_id: chatId, media }, { retries: 1, timeoutMs: 30000 });
}

function sendPhoto(chatId, photoUrl, caption) {
  return tgCall("sendPhoto", {
    chat_id: chatId,
    photo: photoUrl,
    caption: (caption || "").slice(0, 1024),
    parse_mode: "HTML",
  });
}

async function sendPhotoAlbum(chatId, photoUrls, captionForFirst) {
  if (!photoUrls.length) return { ok: false, reason: "sin_fotos" };
  await sendChatAction(chatId, "upload_photo");
  const chunks = [];
  for (let i = 0; i < photoUrls.length; i += 10) chunks.push(photoUrls.slice(i, i + 10));

  let allOk = true;
  let firstError = null;
  for (let i = 0; i < chunks.length; i++) {
    const caption = i === 0 ? captionForFirst : undefined;
    const result = await sendMediaGroupChunk(chatId, chunks[i], caption);
    if (!result.ok) {
      allOk = false;
      firstError = firstError || result.description || result.error;
      console.error("Fallo enviando álbum de diapositivas:", result);
    }
  }
  return { ok: allOk, error: firstError };
}

function extractSlideUrls(doc) {
  const raw = doc.slides || doc.slideUrls || doc.images || [];
  if (!Array.isArray(raw)) return [];
  return raw
    .map((item) => {
      if (typeof item === "string") return item;
      if (item && typeof item === "object") return item.url || item.imageUrl || item.src || item.downloadUrl || null;
      return null;
    })
    .filter(Boolean);
}

function editMessageText(chatId, messageId, text, replyMarkup) {
  return tgCall("editMessageText", {
    chat_id: chatId,
    message_id: messageId,
    text,
    parse_mode: "HTML",
    disable_web_page_preview: true,
    reply_markup: replyMarkup || undefined,
  });
}

function editMessageCaption(chatId, messageId, caption, replyMarkup) {
  return tgCall("editMessageCaption", {
    chat_id: chatId,
    message_id: messageId,
    caption: (caption || "").slice(0, 1024),
    parse_mode: "HTML",
    reply_markup: replyMarkup || undefined,
  });
}

function editMessageReplyMarkup(chatId, messageId, replyMarkup) {
  return tgCall("editMessageReplyMarkup", {
    chat_id: chatId,
    message_id: messageId,
    reply_markup: replyMarkup || { inline_keyboard: [] },
  });
}

function answerCallbackQuery(callbackQueryId, text, showAlert) {
  return tgCall(
    "answerCallbackQuery",
    { callback_query_id: callbackQueryId, text: text || undefined, show_alert: !!showAlert },
    { retries: 0, timeoutMs: 8000 },
  );
}

function deleteMessage(chatId, messageId) {
  return tgCall("deleteMessage", { chat_id: chatId, message_id: messageId }, { retries: 0 });
}

function sendChatAction(chatId, action) {
  return tgCall("sendChatAction", { chat_id: chatId, action }, { retries: 0 });
}

function escapeHtml(str) {
  return String(str || "").replace(
    /[&<>"']/g,
    (c) => ({ "&": "&amp;", "<": "&lt;", ">": "&gt;", '"': "&quot;", "'": "&#39;" })[c],
  );
}

async function sendVideoWithFeedback(chatId, videoUrl, caption, keyboard, retryCallbackData) {
  if (!videoUrl) {
    await sendMessage(chatId, caption + "\n\n<i>⚠️ Esta publicación no tiene un video asociado.</i>", keyboard);
    return { ok: false, reason: "no_video_url" };
  }

  await sendChatAction(chatId, "upload_video");
  const waitMsg = await sendMessage(chatId, "⏳ Preparando tu video, un momento...");

  let result = await sendVideo(chatId, videoUrl, caption, keyboard);

  if (waitMsg.ok && waitMsg.result) {
    await deleteMessage(chatId, waitMsg.result.message_id).catch(() => {});
  }

  if (result.ok) {
    return { ok: true, result };
  }

  const videoErrorReason = result.description || result.error || "motivo desconocido";
  console.warn(`sendVideo falló para ${chatId} (${videoErrorReason}), probando como documento...`);

  result = await sendDocument(chatId, videoUrl, caption, keyboard);
  if (result.ok) {
    return { ok: true, result, sentAsDocument: true };
  }

  const docErrorReason = result.description || result.error || "motivo desconocido";
  console.error(`Tampoco se pudo enviar como documento a ${chatId}:`, docErrorReason);

  const fallbackKeyboard = {
    inline_keyboard: [
      ...(retryCallbackData
        ? [[{ text: "🔁 Reintentar envío de video", callback_data: retryCallbackData }]]
        : []),
      ...(keyboard?.inline_keyboard || []),
    ],
  };

  const isFormatIssue = /wrong type|unsupported|codec/i.test(videoErrorReason);
  const hint = isFormatIssue
    ? "\n\n💡 Este video parece estar en un formato que Telegram no reproduce bien (ej. .webm). Exporta en .mp4 (H.264) para que se envíe sin problemas."
    : "";

  await sendMessage(
    chatId,
    caption +
      `\n\n⚠️ <b>No se pudo enviar el video</b> (${escapeHtml(videoErrorReason)}).${hint}\n` +
      `🔗 <a href="${escapeHtml(videoUrl)}">Abrir video directamente</a>`,
    fallbackKeyboard,
  );

  return { ok: false, reason: docErrorReason };
}

/* ============================================================
   FORMATO / ESTADOS
   ============================================================ */

const STATUS_META = {
  pending: { emoji: "🕓", label: "Pendiente" },
  sending: { emoji: "📤", label: "Enviando..." },
  notified: { emoji: "🔔", label: "Notificado" },
  sent: { emoji: "✅", label: "Publicado" },
  cancelled: { emoji: "❌", label: "Cancelado" },
  error: { emoji: "⚠️", label: "Error" },
};

function statusBadge(status) {
  const meta = STATUS_META[status] || { emoji: "•", label: status || "Desconocido" };
  return `${meta.emoji} ${meta.label}`;
}

function formatDate(iso) {
  if (!iso) return "—";
  try {
    return new Date(iso).toLocaleString("es-PE", {
      dateStyle: "full",
      timeStyle: "short",
      timeZone: "America/Lima",
    });
  } catch (e) {
    return iso;
  }
}

/* ============================================================
   FECHAS: parsing de reprogramación / programación manual escrita por el usuario
   ============================================================ */

function parseRescheduleInput(text) {
  const trimmed = (text || "").trim();

  const relativeMatch = trimmed.match(/^\+(\d+)\s*(m|min|h|hora|hrs|d|dia|día)s?$/i);
  if (relativeMatch) {
    const amount = Number(relativeMatch[1]);
    const unit = relativeMatch[2].toLowerCase();
    let ms = 0;
    if (unit.startsWith("m")) ms = amount * 60 * 1000;
    else if (unit.startsWith("h")) ms = amount * 60 * 60 * 1000;
    else ms = amount * 24 * 60 * 60 * 1000;
    return new Date(Date.now() + ms).toISOString();
  }

  const absoluteMatch = trimmed.match(/^(\d{1,2})[\/\-](\d{1,2})[\/\-](\d{4})\s+(\d{1,2}):(\d{2})$/);
  if (absoluteMatch) {
    const [, dayStr, monthStr, yearStr, hourStr, minStr] = absoluteMatch;
    const day = Number(dayStr);
    const month = Number(monthStr);
    const year = Number(yearStr);
    const hour = Number(hourStr);
    const min = Number(minStr);
    if (month < 1 || month > 12 || day < 1 || day > 31 || hour > 23 || min > 59) return null;
    const utcMillis = Date.UTC(year, month - 1, day, hour, min) + LIMA_UTC_OFFSET_MS;
    const date = new Date(utcMillis);
    if (Number.isNaN(date.getTime())) return null;
    return date.toISOString();
  }

  return null;
}

/* ============================================================
   SESIONES: estado conversacional simple por chat (para reprogramar / programar)
   ============================================================ */

async function setSession(chatId, data) {
  await db.collection(SESSIONS_COLLECTION).doc(String(chatId)).set({ ...data, updatedAt: new Date().toISOString() });
}

async function getSession(chatId) {
  const snap = await db.collection(SESSIONS_COLLECTION).doc(String(chatId)).get();
  return snap.exists ? snap.data() : null;
}

async function clearSession(chatId) {
  await db.collection(SESSIONS_COLLECTION).doc(String(chatId)).delete().catch(() => {});
}

/* ============================================================
   1) PROGRAMADA: revisa cada 5 min publicaciones pendientes
   ============================================================ */

function scheduledAtToMillis(scheduledAt) {
  if (!scheduledAt) return null;
  if (typeof scheduledAt.toMillis === "function") return scheduledAt.toMillis();
  if (typeof scheduledAt === "string") {
    const ms = Date.parse(scheduledAt);
    return Number.isNaN(ms) ? null : ms;
  }
  if (typeof scheduledAt === "number") return scheduledAt;
  return null;
}

const checkScheduledPublications = onSchedule("every 5 minutes", async () => {
  const nowMs = Date.now();

  try {
    const snap = await db.collection(SCHEDULED_COLLECTION).where("status", "==", "pending").get();

    if (snap.empty) {
      console.log("No hay publicaciones pendientes por avisar.");
      return;
    }

    const due = snap.docs.filter((docSnap) => {
      const ms = scheduledAtToMillis(docSnap.data().scheduledAt);
      return ms !== null && ms <= nowMs;
    });

    if (due.length === 0) {
      console.log(`Hay ${snap.size} pendiente(s), ninguna vencida todavía.`);
      return;
    }

    for (const docSnap of due) {
      await processScheduledDoc(docSnap.ref);
    }
  } catch (err) {
    console.error("Error en checkScheduledPublications:", err);
    if (process.env.TELEGRAM_BOT_TOKEN_GENERATOR_CODE_WEBHOOK_SECRET_APRENDE_CODE) {
      await sendMessage(
        process.env.TELEGRAM_BOT_TOKEN_GENERATOR_CODE_WEBHOOK_SECRET_APRENDE_CODE,
        `⚠️ <b>checkScheduledPublications falló</b>\n${escapeHtml(String(err.message || err))}`,
      ).catch(() => {});
    }
  }
});

async function processScheduledDoc(docRef) {
  let pub;
  try {
    pub = await db.runTransaction(async (tx) => {
      const fresh = await tx.get(docRef);
      if (!fresh.exists) return null;
      const data = fresh.data();
      if (data.status !== "pending") return null;
      tx.update(docRef, { status: "sending" });
      return data;
    });
  } catch (err) {
    console.error(`Error reservando publicación ${docRef.id}:`, err);
    return;
  }

  if (!pub) return;

  const chatId = pub.telegramChatId;
  if (!chatId) {
    console.warn(`Publicación ${docRef.id} no tiene telegramChatId, se omite.`);
    await docRef.update({ status: "error", errorMessage: "Sin telegramChatId" }).catch(() => {});
    return;
  }

  try {
    const caption = buildScheduledCaption(pub, "notified");
    const keyboard = scheduledActionsKeyboard(docRef.id, "notified");

    const sendResult = await sendVideoWithFeedback(chatId, pub.videoUrl, caption, keyboard, `retry_video:${docRef.id}`);

    await docRef.update({
      status: "notified",
      notifiedAt: new Date().toISOString(),
      telegramMessageId: sendResult.result?.result?.message_id || null,
      lastVideoError: sendResult.ok ? null : sendResult.reason || null,
    });
  } catch (err) {
    console.error(`Error notificando publicación ${docRef.id}:`, err);
    await docRef.update({ status: "error", errorMessage: String(err.message || err) }).catch(() => {});
    await sendMessage(
      chatId,
      `⚠️ No se pudo procesar el envío de <b>${escapeHtml(pub.tech || "una publicación")}</b>. ` +
        `Puedes reintentarlo desde /programadas.`,
    ).catch(() => {});
  }
}

function buildScheduledCaption(pub, status) {
  return (
    `🔔 <b>¡Hora de publicar!</b>\n\n` +
    `<b>${escapeHtml(pub.tech || "Contenido")}</b>\n` +
    `${statusBadge(status)}\n\n` +
    `${escapeHtml(pub.caption || "")}`
  );
}

function scheduledActionsKeyboard(schedId, status) {
  if (status === "pending" || status === "notified") {
    return {
      inline_keyboard: [
        [
          { text: "✅ Marcar como publicado", callback_data: `mark_sent:${schedId}` },
          { text: "⏰ +1h", callback_data: `snooze:${schedId}` },
        ],
        [{ text: "🗓️ Reprogramar...", callback_data: `ask_reschedule:${schedId}` }],
        [{ text: "❌ Cancelar", callback_data: `ask_cancel:${schedId}` }],
      ],
    };
  }
  if (status === "error") {
    return {
      inline_keyboard: [
        [{ text: "🔁 Reintentar envío", callback_data: `retry_pub:${schedId}` }],
        [{ text: "🗓️ Reprogramar...", callback_data: `ask_reschedule:${schedId}` }],
        [{ text: "🗑️ Eliminar", callback_data: `ask_delete_sched:${schedId}` }],
      ],
    };
  }
  return {
    inline_keyboard: [[{ text: "🗑️ Eliminar", callback_data: `ask_delete_sched:${schedId}` }]],
  };
}

/* ============================================================
   2) WEBHOOK: recibe comandos y clics de botones desde Telegram
   ============================================================ */

const telegramWebhook_aprende_code = onRequest(async (req, res) => {
  if (req.method !== "POST") {
    res.status(200).send("OK");
    return;
  }
  try {
    const update = req.body || {};
    if (update.message) await handleMessage(update.message);
    else if (update.callback_query) await handleCallbackQuery(update.callback_query);
    res.status(200).send("OK");
  } catch (err) {
    console.error("Error procesando update de Telegram:", err);
    res.status(200).send("OK");
  }
});

/* ---------------- mensajes / comandos ---------------- */

async function handleMessage(message) {
  const chatId = message.chat.id;
  const text = (message.text || "").trim();

  try {
    const session = await getSession(chatId);

    if (session?.mode === "awaiting_reschedule" && !text.startsWith("/")) {
      await handleRescheduleInput(chatId, session, text);
      return;
    }
    if (session?.mode === "awaiting_schedule_date" && !text.startsWith("/")) {
      await handleScheduleDateInput(chatId, session, text);
      return;
    }

    if (text === "/start" || text === "/menu") {
      await clearSession(chatId);
      await sendMessage(
        chatId,
        "👋 <b>Carousel Studio Bot</b>\n\nGestiona tus generaciones y publicaciones programadas directamente desde aquí. Elige una opción:",
        mainMenuKeyboard(),
      );
      return;
    }
    if (text === "/generaciones") {
      await sendChatAction(chatId, "typing");
      await sendGenerationsPage(chatId, null, 0, "all");
      return;
    }
    if (text === "/creditos" || text === "/credits") {
      await sendChatAction(chatId, "typing");
      await sendCreditsInfo(chatId, null);
      return;
    }
    if (text === "/programadas") {
      await sendChatAction(chatId, "typing");
      await sendScheduledPage(chatId, null, 0);
      return;
    }
    if (text === "/programar") {
      await clearSession(chatId);
      await sendChatAction(chatId, "typing");
      await sendGenerationsPage(chatId, null, 0, "all");
      return;
    }
    if (text === "/cancelar") {
      await clearSession(chatId);
      await sendMessage(chatId, "Operación cancelada. Usa /menu para ver las opciones.");
      return;
    }
    if (text === "/ayuda" || text === "/help") {
      await sendMessage(
        chatId,
        "<b>Comandos disponibles</b>\n\n" +
          "/menu — Menú principal\n" +
          "/generaciones — Ver tus generaciones guardadas (con filtros)\n" +
          "/programar — Elegir una generación para programar su publicación\n" +
          "/programadas — Ver publicaciones programadas\n" +
          "/cancelar — Cancela una acción en curso (ej. reprogramar/programar)\n" +
          "/ayuda — Este mensaje",
      );
      return;
    }

    await sendMessage(chatId, "No entendí ese comando. Usa /menu para ver las opciones disponibles.");
  } catch (err) {
    console.error("Error en handleMessage:", err);
    await sendMessage(chatId, "⚠️ Ocurrió un error procesando tu mensaje. Intenta de nuevo.").catch(() => {});
  }
}

async function handleRescheduleInput(chatId, session, text) {
  const schedId = session.schedId;
  const newIso = parseRescheduleInput(text);

  if (!newIso) {
    await sendMessage(
      chatId,
      "No entendí esa fecha 🤔\n\n" +
        "Usa el formato <code>dd/mm/aaaa hh:mm</code> (hora de Lima), por ejemplo:\n" +
        "<code>25/12/2026 15:30</code>\n\n" +
        "O algo relativo: <code>+30m</code>, <code>+2h</code>, <code>+1d</code>.\n\n" +
        "Envía /cancelar para salir sin reprogramar.",
    );
    return;
  }

  const docRef = db.collection(SCHEDULED_COLLECTION).doc(schedId);
  const docSnap = await docRef.get();
  if (!docSnap.exists) {
    await clearSession(chatId);
    await sendMessage(chatId, "Esa publicación ya no existe.");
    return;
  }

  await docRef.update({ status: "pending", scheduledAt: newIso, errorMessage: null });
  await clearSession(chatId);

  await sendMessage(chatId, `✅ Reprogramado para <b>${escapeHtml(formatDate(newIso))}</b>.`, {
    inline_keyboard: [[{ text: "📅 Ver publicaciones", callback_data: "menu:scheduled:0" }]],
  });
}

// Crea una nueva publicación programada en scheduled_publications a partir
// de una generación existente, usando la fecha que el usuario escribió
// mientras estaba en modo "awaiting_schedule_date".
async function handleScheduleDateInput(chatId, session, text) {
  const { genId, filter, page } = session;
  const newIso = parseRescheduleInput(text);

  if (!newIso) {
    await sendMessage(
      chatId,
      "No entendí esa fecha 🤔\n\n" +
        "Usa el formato <code>dd/mm/aaaa hh:mm</code> (hora de Lima), por ejemplo:\n" +
        "<code>25/12/2026 15:30</code>\n\n" +
        "O algo relativo: <code>+30m</code>, <code>+2h</code>, <code>+1d</code>.\n\n" +
        "Envía /cancelar para salir sin programar.",
    );
    return;
  }

  const genSnap = await db.collection(GENERATIONS_COLLECTION).doc(genId).get();
  if (!genSnap.exists) {
    await clearSession(chatId);
    await sendMessage(chatId, "Esa generación ya no existe.");
    return;
  }
  const g = genSnap.data();

  const newDocRef = await db.collection(SCHEDULED_COLLECTION).add({
    generationId: genId,
    tech: g.tech || "",
    caption: g.caption || "",
    videoUrl: g.videoUrl || null,
    slides: g.slides || g.slideUrls || g.images || [],
    telegramChatId: chatId,
    status: "pending",
    scheduledAt: newIso,
    createdAt: new Date().toISOString(),
  });

  await clearSession(chatId);

  await sendMessage(
    chatId,
    `✅ Publicación programada para <b>${escapeHtml(formatDate(newIso))}</b>.`,
    {
      inline_keyboard: [
        [{ text: "📅 Ver programadas", callback_data: "menu:scheduled:0" }],
        [{ text: "⬅️ Volver a la generación", callback_data: `view_gen:${genId}:${filter || "all"}:${page || 0}` }],
      ],
    },
  );

  console.log(`Nueva publicación programada ${newDocRef.id} para generación ${genId} en ${newIso}`);
}

function mainMenuKeyboard() {
  return {
    inline_keyboard: [
      [{ text: "🎬 Mis generaciones", callback_data: "genpage:all:0" }],
      [{ text: "📅 Publicaciones programadas", callback_data: "menu:scheduled:0" }],
      [{ text: "💳 Créditos ElevenLabs", callback_data: "check_credits" }],
    ],
  };
}

/* ============================================================
   FILTROS DE GENERACIONES (Todas / Publicadas / Archivadas / Programadas)
   ============================================================ */

const GENERATION_FILTERS = ["all", "published", "archived", "scheduled"];

function generationFilterLabel(filter) {
  return { all: "Todas", published: "✅ Publicadas", archived: "📦 Archivadas", scheduled: "📅 Programadas" }[filter] || "Todas";
}

// Fila de chips de filtro; marca con "• " el filtro activo.
function generationFilterRow(activeFilter) {
  return GENERATION_FILTERS.map((f) => ({
    text: `${f === activeFilter ? "• " : ""}${generationFilterLabel(f)}`,
    callback_data: `genpage:${f}:0`,
  }));
}

// Trae los ids de generaciones que tienen una publicación pendiente/notificada
// (equivalente al chip "Programadas" de la app web).
async function getScheduledGenerationIdsSet() {
  try {
    const snap = await db.collection(SCHEDULED_COLLECTION).where("status", "in", ["pending", "notified"]).get();
    return new Set(snap.docs.map((d) => d.data().generationId).filter(Boolean));
  } catch (err) {
    console.error("Error obteniendo generaciones programadas:", err);
    return new Set();
  }
}

/* ---------------- botones (callback_query) ---------------- */

async function handleCallbackQuery(cbq) {
  const chatId = cbq.message.chat.id;
  const messageId = cbq.message.message_id;
  const [action, ...rest] = (cbq.data || "").split(":");

  try {
    switch (action) {
      case "menu": {
        const [type, pageStr] = rest;
        await answerCallbackQuery(cbq.id);
        if (type === "generations") await sendGenerationsPage(chatId, messageId, Number(pageStr) || 0, "all");
        else await sendScheduledPage(chatId, messageId, Number(pageStr) || 0);
        break;
      }

      case "check_credits":
        await answerCallbackQuery(cbq.id, "Consultando créditos...");
        await sendCreditsInfo(chatId, messageId);
        break;

      // Lista de generaciones con filtro + paginación: genpage:<filter>:<page>
      case "genpage": {
        const [filter, pageStr] = rest;
        await answerCallbackQuery(cbq.id);
        await sendGenerationsPage(chatId, messageId, Number(pageStr) || 0, filter || "all");
        break;
      }

      case "sched_page":
        await answerCallbackQuery(cbq.id);
        await sendScheduledPage(chatId, messageId, Number(rest[0]) || 0);
        break;

      // Detalle de una generación: view_gen:<id>:<filter>:<page>  (filter/page para el botón "Volver")
      case "view_gen": {
        const [genId, filter, pageStr] = rest;
        await answerCallbackQuery(cbq.id);
        await sendGenerationDetail(chatId, genId, filter || "all", Number(pageStr) || 0);
        break;
      }

      case "view_sched":
        await answerCallbackQuery(cbq.id);
        await sendScheduledDetail(chatId, rest[0]);
        break;

      case "view_slides": {
        const [collection, docId] = rest;
        const colRef = collection === "sched" ? SCHEDULED_COLLECTION : GENERATIONS_COLLECTION;
        const docSnap = await db.collection(colRef).doc(docId).get();
        if (!docSnap.exists) {
          await answerCallbackQuery(cbq.id, "Ya no existe.", true);
          break;
        }
        const slideUrls = extractSlideUrls(docSnap.data());
        if (!slideUrls.length) {
          await answerCallbackQuery(cbq.id, "Esta publicación no tiene diapositivas guardadas.", true);
          break;
        }
        await answerCallbackQuery(cbq.id, `Enviando ${slideUrls.length} diapositiva(s)...`);
        const albumResult = await sendPhotoAlbum(
          chatId,
          slideUrls,
          `🖼️ <b>${escapeHtml(docSnap.data().tech || "Diapositivas")}</b> (${slideUrls.length})`,
        );
        if (!albumResult.ok) {
          await sendMessage(
            chatId,
            `⚠️ No se pudieron enviar todas las diapositivas (${escapeHtml(String(albumResult.error || ""))}).\n` +
              slideUrls.map((u, i) => `${i + 1}. ${u}`).join("\n"),
          );
        }
        break;
      }

      // Marca/desmarca una generación como publicada: toggle_published:<genId>:<filter>:<page>
      case "toggle_published": {
        const [genId, filter, pageStr] = rest;
        const docRef = db.collection(GENERATIONS_COLLECTION).doc(genId);
        const docSnap = await docRef.get();
        if (!docSnap.exists) {
          await answerCallbackQuery(cbq.id, "Esta generación ya no existe.", true);
          break;
        }
        const newValue = !docSnap.data().published;
        await docRef.update({ published: newValue });
        await answerCallbackQuery(cbq.id, newValue ? "Marcada como publicada ✅" : "Desmarcada ↩️");
        await sendGenerationDetail(chatId, genId, filter || "all", Number(pageStr) || 0);
        break;
      }

      // Archiva/desarchiva una generación: toggle_archived:<genId>:<filter>:<page>
      case "toggle_archived": {
        const [genId, filter, pageStr] = rest;
        const docRef = db.collection(GENERATIONS_COLLECTION).doc(genId);
        const docSnap = await docRef.get();
        if (!docSnap.exists) {
          await answerCallbackQuery(cbq.id, "Esta generación ya no existe.", true);
          break;
        }
        const newValue = !docSnap.data().archived;
        await docRef.update({ archived: newValue, archivedAt: newValue ? new Date().toISOString() : null });
        await answerCallbackQuery(cbq.id, newValue ? "Generación archivada 📦" : "Desarchivada 📤");
        await sendGenerationDetail(chatId, genId, filter || "all", Number(pageStr) || 0);
        break;
      }

      // Pide la fecha para programar la publicación de una generación: ask_schedule:<genId>:<filter>:<page>
      case "ask_schedule": {
        const [genId, filter, pageStr] = rest;
        const docSnap = await db.collection(GENERATIONS_COLLECTION).doc(genId).get();
        if (!docSnap.exists) {
          await answerCallbackQuery(cbq.id, "Esta generación ya no existe.", true);
          break;
        }
        await answerCallbackQuery(cbq.id);
        await setSession(chatId, {
          mode: "awaiting_schedule_date",
          genId,
          filter: filter || "all",
          page: Number(pageStr) || 0,
        });
        await sendMessage(
          chatId,
          `📅 ¿Para cuándo quieres programar <b>${escapeHtml(docSnap.data().tech || "esta generación")}</b>?\n\n` +
            "Escribe la fecha como <code>dd/mm/aaaa hh:mm</code> (hora de Lima), por ejemplo:\n" +
            "<code>25/12/2026 15:30</code>\n\n" +
            "O algo relativo: <code>+30m</code>, <code>+2h</code>, <code>+1d</code>.\n\n" +
            "Envía /cancelar para salir sin programar.",
        );
        break;
      }

      case "mark_sent": {
        const schedId = rest[0];
        const docRef = db.collection(SCHEDULED_COLLECTION).doc(schedId);
        const docSnap = await docRef.get();
        if (!docSnap.exists) {
          await answerCallbackQuery(cbq.id, "Esta publicación ya no existe.", true);
          break;
        }
        await docRef.update({ status: "sent", sentAt: new Date().toISOString() });
        await answerCallbackQuery(cbq.id, "Marcado como publicado ✅");
        await refreshMessageAfterStatusChange(chatId, messageId, schedId, docSnap.data(), "sent");
        break;
      }

      case "snooze": {
        const schedId = rest[0];
        const docRef = db.collection(SCHEDULED_COLLECTION).doc(schedId);
        const docSnap = await docRef.get();
        if (!docSnap.exists) {
          await answerCallbackQuery(cbq.id, "Esta publicación ya no existe.", true);
          break;
        }
        const newTime = new Date(Date.now() + 60 * 60 * 1000).toISOString();
        await docRef.update({ status: "pending", scheduledAt: newTime });
        await answerCallbackQuery(cbq.id, "Pospuesto 1 hora ⏰");
        await refreshMessageAfterStatusChange(chatId, messageId, schedId, { ...docSnap.data(), scheduledAt: newTime }, "pending");
        break;
      }

      case "ask_reschedule": {
        const schedId = rest[0];
        const docSnap = await db.collection(SCHEDULED_COLLECTION).doc(schedId).get();
        if (!docSnap.exists) {
          await answerCallbackQuery(cbq.id, "Esta publicación ya no existe.", true);
          break;
        }
        await answerCallbackQuery(cbq.id);
        await setSession(chatId, { mode: "awaiting_reschedule", schedId });
        await sendMessage(
          chatId,
          `🗓️ ¿Para cuándo quieres reprogramar <b>${escapeHtml(docSnap.data().tech || "esta publicación")}</b>?\n\n` +
            "Escribe la fecha como <code>dd/mm/aaaa hh:mm</code> (hora de Lima), por ejemplo:\n" +
            "<code>25/12/2026 15:30</code>\n\n" +
            "O algo relativo: <code>+30m</code>, <code>+2h</code>, <code>+1d</code>.\n\n" +
            "Envía /cancelar para salir sin cambios.",
        );
        break;
      }

      case "retry_video":
      case "retry_pub": {
        const schedId = rest[0];
        const docRef = db.collection(SCHEDULED_COLLECTION).doc(schedId);
        const docSnap = await docRef.get();
        if (!docSnap.exists) {
          await answerCallbackQuery(cbq.id, "Esta publicación ya no existe.", true);
          break;
        }
        await answerCallbackQuery(cbq.id, "Reintentando envío...");
        await docRef.update({ status: "pending", errorMessage: null });
        await processScheduledDoc(docRef);
        break;
      }

      case "ask_cancel": {
        const schedId = rest[0];
        await answerCallbackQuery(cbq.id);
        await editMessageReplyMarkup(chatId, messageId, {
          inline_keyboard: [
            [
              { text: "⚠️ Sí, cancelar", callback_data: `cancel:${schedId}` },
              { text: "↩️ Volver", callback_data: `undo_cancel:${schedId}` },
            ],
          ],
        });
        break;
      }

      case "undo_cancel": {
        const schedId = rest[0];
        const docSnap = await db.collection(SCHEDULED_COLLECTION).doc(schedId).get();
        await answerCallbackQuery(cbq.id);
        if (docSnap.exists) {
          await refreshMessageAfterStatusChange(chatId, messageId, schedId, docSnap.data(), docSnap.data().status);
        } else {
          await editMessageReplyMarkup(chatId, messageId, { inline_keyboard: [] });
        }
        break;
      }

      case "cancel": {
        const schedId = rest[0];
        const docRef = db.collection(SCHEDULED_COLLECTION).doc(schedId);
        const docSnap = await docRef.get();
        if (!docSnap.exists) {
          await answerCallbackQuery(cbq.id, "Esta publicación ya no existe.", true);
          break;
        }
        await docRef.update({ status: "cancelled" });
        await answerCallbackQuery(cbq.id, "Publicación cancelada ❌");
        await refreshMessageAfterStatusChange(chatId, messageId, schedId, docSnap.data(), "cancelled");
        break;
      }

      case "ask_delete_sched": {
        const schedId = rest[0];
        await answerCallbackQuery(cbq.id);
        await editMessageReplyMarkup(chatId, messageId, {
          inline_keyboard: [
            [
              { text: "🗑️ Sí, eliminar", callback_data: `delete_sched:${schedId}` },
              { text: "↩️ Volver", callback_data: `view_sched:${schedId}` },
            ],
          ],
        });
        break;
      }

      case "delete_sched": {
        const schedId = rest[0];
        await db.collection(SCHEDULED_COLLECTION).doc(schedId).delete();
        await answerCallbackQuery(cbq.id, "Eliminado 🗑️");
        await deleteMessage(chatId, messageId).catch(() => {});
        await sendScheduledPage(chatId, null, 0);
        break;
      }

      default:
        await answerCallbackQuery(cbq.id, "Esta opción ya no está disponible.");
    }
  } catch (err) {
    console.error(`Error en handleCallbackQuery (${action}):`, err);
    await answerCallbackQuery(cbq.id, "⚠️ Ocurrió un error. Intenta de nuevo.", true).catch(() => {});
  }
}

async function refreshMessageAfterStatusChange(chatId, messageId, schedId, pub, newStatus) {
  const caption = buildScheduledCaption({ ...pub, status: newStatus }, newStatus);
  const keyboard = scheduledActionsKeyboard(schedId, newStatus);

  const result = await editMessageCaption(chatId, messageId, caption, keyboard);
  if (!result.ok) {
    await editMessageText(chatId, messageId, caption, keyboard).catch(() => {});
  }
}

/* ---------------- listados paginados ---------------- */

// Trae hasta GENERATIONS_FETCH_CAP generaciones ordenadas por fecha, filtra
// en memoria según el chip elegido (evita índices compuestos en Firestore
// para published/archived) y pagina el resultado ya filtrado.
async function sendGenerationsPage(chatId, messageId, page, filter = "all") {
  const snap = await db.collection(GENERATIONS_COLLECTION).orderBy("createdAt", "desc").limit(GENERATIONS_FETCH_CAP).get();

  let docs = snap.docs;

  if (filter === "scheduled") {
    const scheduledIds = await getScheduledGenerationIdsSet();
    docs = docs.filter((d) => scheduledIds.has(d.id));
  } else if (filter === "published") {
    docs = docs.filter((d) => !!d.data().published);
  } else if (filter === "archived") {
    docs = docs.filter((d) => !!d.data().archived);
  } else {
    docs = docs.filter((d) => !d.data().archived); // "Todas" oculta archivadas, igual que en la app web
  }

  const filterRow = generationFilterRow(filter);

  if (!docs.length) {
    const emptyMsgs = {
      all: "📭 Aún no tienes generaciones guardadas.\n\nCréalas desde Carousel Studio y aparecerán aquí automáticamente.",
      published: "📭 No tienes generaciones marcadas como publicadas.",
      archived: "📭 No tienes generaciones archivadas.",
      scheduled: "📭 No tienes generaciones con una publicación programada.",
    };
    const keyboard = { inline_keyboard: [filterRow, [{ text: "📅 Ver programadas", callback_data: "menu:scheduled:0" }]] };
    const text = emptyMsgs[filter] || emptyMsgs.all;
    return messageId ? editMessageText(chatId, messageId, text, keyboard) : sendMessage(chatId, text, keyboard);
  }

  const totalPages = Math.max(1, Math.ceil(docs.length / PAGE_SIZE));
  const safePage = Math.min(Math.max(page, 0), totalPages - 1);
  const pageDocs = docs.slice(safePage * PAGE_SIZE, safePage * PAGE_SIZE + PAGE_SIZE);

  const rows = pageDocs.map((d) => {
    const g = d.data();
    const statusIcon = g.status === "ready" ? "✅" : g.status === "error" ? "⚠️" : "⏳";
    const videoIcon = g.videoUrl ? " 🎥" : "";
    const publishedIcon = g.published ? " ⭐" : "";
    const archivedIcon = g.archived ? " 📦" : "";
    return [
      {
        text: `${statusIcon} ${g.tech || "Sin nombre"}${videoIcon}${publishedIcon}${archivedIcon}`,
        callback_data: `view_gen:${d.id}:${filter}:${safePage}`,
      },
    ];
  });

  const navRow = [];
  if (safePage > 0) navRow.push({ text: "⬅️ Anterior", callback_data: `genpage:${filter}:${safePage - 1}` });
  if (safePage < totalPages - 1) navRow.push({ text: "Siguiente ➡️", callback_data: `genpage:${filter}:${safePage + 1}` });

  const rowsFinal = [filterRow, ...rows];
  if (navRow.length) rowsFinal.push(navRow);
  rowsFinal.push([{ text: "🔄 Actualizar", callback_data: `genpage:${filter}:${safePage}` }]);
  rowsFinal.push([{ text: "📅 Ver programadas", callback_data: "menu:scheduled:0" }]);

  const text =
    `🎬 <b>Tus generaciones</b> — ${generationFilterLabel(filter)} (página ${safePage + 1}/${totalPages})\n` +
    `<i>✅ listo · ⏳ procesando · ⚠️ error · 🎥 con video · ⭐ publicado · 📦 archivado</i>`;
  const keyboard = { inline_keyboard: rowsFinal };
  return messageId ? editMessageText(chatId, messageId, text, keyboard) : sendMessage(chatId, text, keyboard);
}

async function sendScheduledPage(chatId, messageId, page) {
  const snap = await db
    .collection(SCHEDULED_COLLECTION)
    .orderBy("scheduledAt", "asc")
    .offset(page * PAGE_SIZE)
    .limit(PAGE_SIZE)
    .get();

  if (snap.empty && page === 0) {
    const text = "📭 No tienes publicaciones programadas.\n\nUsa /programar para elegir una generación y programarla, o hazlo desde Carousel Studio.";
    return messageId
      ? editMessageText(chatId, messageId, text, mainMenuKeyboard())
      : sendMessage(chatId, text, mainMenuKeyboard());
  }

  const rows = snap.docs.map((d) => {
    const s = d.data();
    const meta = STATUS_META[s.status] || { emoji: "•", label: s.status };
    const date = s.scheduledAt
      ? new Date(s.scheduledAt).toLocaleString("es-PE", { dateStyle: "short", timeStyle: "short", timeZone: "America/Lima" })
      : "—";
    return [{ text: `${meta.emoji} ${s.tech || "Contenido"} · ${date}`, callback_data: `view_sched:${d.id}` }];
  });

  const navRow = [];
  if (page > 0) navRow.push({ text: "⬅️ Anterior", callback_data: `sched_page:${page - 1}` });
  if (snap.size === PAGE_SIZE) navRow.push({ text: "Siguiente ➡️", callback_data: `sched_page:${page + 1}` });
  if (navRow.length) rows.push(navRow);
  rows.push([{ text: "🔄 Actualizar", callback_data: `sched_page:${page}` }]);
  rows.push([{ text: "🎬 Ver generaciones", callback_data: "genpage:all:0" }]);
  rows.push([{ text: "➕ Programar nueva", callback_data: "genpage:all:0" }]);

  const text = `📅 <b>Publicaciones programadas</b> (página ${page + 1})\n<i>🕓 pendiente · 🔔 notificado · ✅ publicado · ❌ cancelado · ⚠️ error</i>`;
  const keyboard = { inline_keyboard: rows };
  return messageId ? editMessageText(chatId, messageId, text, keyboard) : sendMessage(chatId, text, keyboard);
}

/* ---------------- detalle individual ---------------- */

async function sendGenerationDetail(chatId, genId, filter = "all", page = 0) {
  const docSnap = await db.collection(GENERATIONS_COLLECTION).doc(genId).get();
  if (!docSnap.exists) {
    await sendMessage(chatId, "Esa generación ya no existe. Puede haber sido eliminada.", {
      inline_keyboard: [[{ text: "⬅️ Volver", callback_data: `genpage:${filter}:${page}` }]],
    });
    return;
  }
  const g = docSnap.data();
  const statusLabel = g.status === "ready" ? "✅ Lista" : g.status === "error" ? "⚠️ Error al guardar" : "⏳ Procesando";
  const slideUrls = extractSlideUrls(g);
  const badges = [g.published ? "⭐ Publicada" : null, g.archived ? "📦 Archivada" : null].filter(Boolean).join(" · ");
  const caption =
    `<b>${escapeHtml(g.tech || "Contenido")}</b>\n` +
    `${slideUrls.length || g.slidesCount || 0} diapositivas · ${statusLabel}${badges ? "\n" + badges : ""}\n\n` +
    `${escapeHtml(g.caption || "")}`;

  const rows = [];
  if (slideUrls.length) rows.push([{ text: "🖼️ Ver diapositivas", callback_data: `view_slides:gen:${genId}` }]);
  rows.push([
    {
      text: g.published ? "↩️ Desmarcar publicada" : "✅ Marcar publicada",
      callback_data: `toggle_published:${genId}:${filter}:${page}`,
    },
    {
      text: g.archived ? "📤 Desarchivar" : "📦 Archivar",
      callback_data: `toggle_archived:${genId}:${filter}:${page}`,
    },
  ]);
  rows.push([{ text: "📅 Programar publicación", callback_data: `ask_schedule:${genId}:${filter}:${page}` }]);
  rows.push([{ text: "⬅️ Volver", callback_data: `genpage:${filter}:${page}` }]);
  const keyboard = { inline_keyboard: rows };

  if (g.videoUrl) {
    await sendVideoWithFeedback(chatId, g.videoUrl, caption, keyboard, `view_gen:${genId}:${filter}:${page}`);
  } else {
    await sendMessage(chatId, caption + "\n\n<i>Aún no tiene video generado.</i>", keyboard);
  }
}

async function sendScheduledDetail(chatId, schedId) {
  const docSnap = await db.collection(SCHEDULED_COLLECTION).doc(schedId).get();
  if (!docSnap.exists) {
    await sendMessage(chatId, "Esa publicación ya no existe. Puede haber sido eliminada.", {
      inline_keyboard: [[{ text: "⬅️ Volver", callback_data: "menu:scheduled:0" }]],
    });
    return;
  }
  const s = docSnap.data();
  const slideUrls = extractSlideUrls(s);
  const caption =
    `<b>${escapeHtml(s.tech || "Contenido")}</b>\n` +
    `🕓 Programado para: ${formatDate(s.scheduledAt)}\n` +
    `Estado: ${statusBadge(s.status)}\n\n` +
    `${escapeHtml(s.caption || "")}`;

  const keyboard = scheduledActionsKeyboard(schedId, s.status);
  if (slideUrls.length) {
    keyboard.inline_keyboard.push([{ text: "🖼️ Ver diapositivas", callback_data: `view_slides:sched:${schedId}` }]);
  }
  keyboard.inline_keyboard.push([{ text: "⬅️ Volver", callback_data: "menu:scheduled:0" }]);

  if (s.videoUrl) {
    await sendVideoWithFeedback(chatId, s.videoUrl, caption, keyboard, `view_sched:${schedId}`);
  } else {
    await sendMessage(chatId, caption + "\n\n<i>Sin video adjunto.</i>", keyboard);
  }
}

module.exports = {
  checkScheduledPublications,
  telegramWebhook_aprende_code,
};