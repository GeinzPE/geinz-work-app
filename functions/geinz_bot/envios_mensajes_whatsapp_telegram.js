// ============================================================
// envios.js
// ============================================================
// TODAS las funciones que efectivamente mandan algo hacia afuera
// (WhatsApp y Telegram) viven aquí: mensajes de texto, imágenes,
// stickers, plantillas, audio (TTS) y notificaciones push.
//
// Regla de oro: ningún otro archivo (geinz_dispatcher.js,
// emergencia.js, negocio.js, turismo.js, promociones.js,
// servicios_basicos.js, geinz.js, etc.) vuelve a hacer un fetch
// directo a la Graph API de WhatsApp o a la API de Telegram.
// Todo pasa por aquí.
// ============================================================

const admin = require("firebase-admin");
if (!admin.apps.length) {
  admin.initializeApp();
}
const db = admin.firestore();

const { guardarMensajeHistorial } = require("../historial_chats/historial_geinz");

// ============================================================
// CONFIG
// ============================================================
const WHATSAPP_TOKEN = process.env.ID_API_WHATSAPP; // Bearer token (empieza con EAA...)
const WHATSAPP_PHONE_NUMBER_ID = process.env.ID_NUMBER_WHATSAPP;
const WHATSAPP_API_VERSION = "v20.0";
const WHATSAPP_URL_MESSAGES = `https://graph.facebook.com/${WHATSAPP_API_VERSION}/${WHATSAPP_PHONE_NUMBER_ID}/messages`;
const WHATSAPP_URL_MEDIA = `https://graph.facebook.com/${WHATSAPP_API_VERSION}/${WHATSAPP_PHONE_NUMBER_ID}/media`;

const TELEGRAM_TOKEN = process.env.TELEGRAM_BOT_TOKEN;
const TELEGRAM_API_URL = `https://api.telegram.org/bot${TELEGRAM_TOKEN}`;

const TTS_ELEVENLABS_URL =
  "https://us-central1-geinzworkapp.cloudfunctions.net/textoAVozn8n_elevenlabs_2";
const TTS_VOICE_ID_DEFAULT = "KFBj2OnpjcE1zKB9CGb8";

// ============================================================
// UTILIDADES COMPARTIDAS
// ============================================================

// Detecta números de celular/teléfono o links dentro de un texto.
// Si detecta cualquiera de los dos, NUNCA se debe mandar audio.
function contieneNumeroOLink(texto) {
  if (!texto) return false;
  if (/https?:\/\/[^\s]+/i.test(texto)) return true;
  if (/\bwww\.[^\s]+/i.test(texto)) return true;
  if (/(\+?51[\s-]?)?9\d{2}[\s.-]?\d{3}[\s.-]?\d{3}\b/.test(texto)) return true;
  if (/\d[\d\s.-]{6,}\d/.test(texto)) return true;
  return false;
}

// Los usuarios de Telegram se guardan en Firestore como "tg_<chatId>"
// para no chocar con números de WhatsApp. La API de Telegram necesita
// el chatId "pelado" (sin el prefijo).
function normalizarChatIdTelegram(chatId) {
  return String(chatId).startsWith("tg_")
    ? String(chatId).replace(/^tg_/, "")
    : String(chatId);
}

// ============================================================
// WHATSAPP — ENVÍOS
// ============================================================

async function enviarMensajeWhatsapp(recipientPhoneNumber, textBody) {
  const payload = {
    messaging_product: "whatsapp",
    to: recipientPhoneNumber,
    type: "text",
    text: { body: textBody },
  };

  const resp = await fetch(WHATSAPP_URL_MESSAGES, {
    method: "POST",
    headers: {
      Authorization: `Bearer ${WHATSAPP_TOKEN}`,
      "Content-Type": "application/json",
    },
    body: JSON.stringify(payload),
  });

  const responseText = await resp.text();

  if (!resp.ok) {
    console.error("❌ [enviarMensajeWhatsapp] Error:", resp.status, responseText);
    throw new Error(
      `Error enviando mensaje WhatsApp: ${resp.status}\n${responseText}`,
    );
  }

  let json = {};
  try {
    json = JSON.parse(responseText);
  } catch (e) {
    console.warn("⚠️ [enviarMensajeWhatsapp] La respuesta no es un JSON válido.");
  }

  guardarMensajeHistorial({
    canal: "whatsapp",
    numero_usuario: recipientPhoneNumber,
    remitente: "bot",
    tipo: "texto",
    contenido: textBody,
  }).catch((err) => console.warn("⚠️ No se pudo guardar el historial.", err));

  return json;
}

async function enviarImagenWhatsapp(recipientPhoneNumber, imagenUrl, caption) {
  const resp = await fetch(WHATSAPP_URL_MESSAGES, {
    method: "POST",
    headers: {
      Authorization: `Bearer ${WHATSAPP_TOKEN}`,
      "Content-Type": "application/json",
    },
    body: JSON.stringify({
      messaging_product: "whatsapp",
      to: recipientPhoneNumber,
      type: "image",
      image: { link: imagenUrl, caption: caption || "" },
    }),
  });

  if (!resp.ok)
    throw new Error(
      `Error enviando imagen WhatsApp: ${resp.status} ${await resp.text()}`,
    );

  guardarMensajeHistorial({
    canal: "whatsapp",
    numero_usuario: recipientPhoneNumber,
    remitente: "bot",
    tipo: "imagen",
    contenido: caption || "",
    extra: { imagen: imagenUrl },
  }).catch(() => {});

  return resp.json();
}

async function enviarStickerWhatsapp(recipientPhoneNumber, stickerUrl) {
  const resp = await fetch(WHATSAPP_URL_MESSAGES, {
    method: "POST",
    headers: {
      Authorization: `Bearer ${WHATSAPP_TOKEN}`,
      "Content-Type": "application/json",
    },
    body: JSON.stringify({
      messaging_product: "whatsapp",
      to: recipientPhoneNumber,
      type: "sticker",
      sticker: { link: stickerUrl },
    }),
  });

  if (!resp.ok)
    throw new Error(
      `Error enviando sticker WhatsApp: ${resp.status} ${await resp.text()}`,
    );

  guardarMensajeHistorial({
    canal: "whatsapp",
    numero_usuario: recipientPhoneNumber,
    remitente: "bot",
    tipo: "sticker",
    contenido: "",
    extra: { sticker: stickerUrl },
  }).catch(() => {});

  return resp.json();
}

async function enviarPlantillaWhatsapp_para_tiendas({
  recipientPhoneNumber,
  imagen,
  mensaje_safe,
  alias_tienda,
  id,
  token_wsap,
}) {
  const body = {
    messaging_product: "whatsapp",
    to: recipientPhoneNumber,
    type: "template",
    template: {
      name: "entidades_data",
      language: { code: "en" },
      components: [
        {
          type: "header",
          parameters: [{ type: "image", image: { link: imagen } }],
        },
        {
          type: "body",
          parameters: [{ type: "text", text: mensaje_safe }],
        },
        {
          type: "button",
          sub_type: "url",
          index: "0",
          parameters: [{ type: "text", text: alias_tienda }],
        },
        {
          type: "button",
          sub_type: "url",
          index: "1",
          parameters: [
            { type: "text", text: `?id_tienda=${id}&contacto=${token_wsap}` },
          ],
        },
      ],
    },
  };

  const resp = await fetch(WHATSAPP_URL_MESSAGES, {
    method: "POST",
    headers: {
      Authorization: `Bearer ${WHATSAPP_TOKEN}`,
      "Content-Type": "application/json",
    },
    body: JSON.stringify(body),
  });

  const responseText = await resp.text();
  if (!resp.ok)
    throw new Error(
      `Error enviando plantilla WhatsApp: ${resp.status}\n${responseText}`,
    );

  let json = null;
  try {
    json = JSON.parse(responseText);
  } catch (e) {
    /* no era JSON, no pasa nada */
  }

  guardarMensajeHistorial({
    canal: "whatsapp",
    numero_usuario: recipientPhoneNumber,
    remitente: "bot",
    tipo: "plantilla",
    contenido: mensaje_safe || "",
    extra: { plantilla: "entidades_data" },
  }).catch(() => {});

  return json;
}

async function enviarPlantillaWhatsapp_promociones({
  recipientPhoneNumber,
  imagen,
  mensaje,
  ids,
}) {
  const body = {
    messaging_product: "whatsapp",
    to: recipientPhoneNumber,
    type: "template",
    template: {
      name: "detalles_establecimiento_standard",
      language: { code: "es" },
      components: [
        {
          type: "header",
          parameters: [{ type: "image", image: { link: imagen } }],
        },
        { type: "body", parameters: [{ type: "text", text: mensaje }] },
        {
          type: "button",
          sub_type: "url",
          index: "0",
          parameters: [
            {
              type: "text",
              text: `api/share?t=pmspls&l=ba&p=${ids[0] || ""},${ids[1] || ""}`,
            },
          ],
        },
      ],
    },
  };

  const resp = await fetch(WHATSAPP_URL_MESSAGES, {
    method: "POST",
    headers: {
      Authorization: `Bearer ${WHATSAPP_TOKEN}`,
      "Content-Type": "application/json",
    },
    body: JSON.stringify(body),
  });

  if (!resp.ok)
    throw new Error(
      `Error enviando plantilla de promociones: ${resp.status} ${await resp.text()}`,
    );

  guardarMensajeHistorial({
    canal: "whatsapp",
    numero_usuario: recipientPhoneNumber,
    remitente: "bot",
    tipo: "plantilla",
    contenido: mensaje || "",
    extra: { plantilla: "detalles_establecimiento_standard", ids },
  }).catch(() => {});

  return resp.json();
}

async function enviarPlantillaBaneoWhatsapp({
  recipientPhoneNumber,
  nombre_user,
  mensajeFinal,
}) {
  const body = {
    messaging_product: "whatsapp",
    to: 51937659216,
    type: "template",
    template: {
      name: "baneo_usr",
      language: { code: "es" },
      components: [
        {
          type: "body",
          parameters: [
            { type: "text", text: recipientPhoneNumber },
            { type: "text", text: nombre_user },
            { type: "text", text: mensajeFinal },
          ],
        },
        {
          type: "button",
          sub_type: "url",
          index: "0",
          parameters: [{ type: "text", text: recipientPhoneNumber }],
        },
      ],
    },
  };

  const resp = await fetch(WHATSAPP_URL_MESSAGES, {
    method: "POST",
    headers: {
      Authorization: `Bearer ${WHATSAPP_TOKEN}`,
      "Content-Type": "application/json",
    },
    body: JSON.stringify(body),
  });

  if (!resp.ok)
    throw new Error(
      `Error enviando plantilla de baneo WhatsApp: ${resp.status} ${await resp.text()}`,
    );

  guardarMensajeHistorial({
    canal: "whatsapp",
    numero_usuario: recipientPhoneNumber,
    remitente: "bot",
    tipo: "plantilla",
    contenido: mensajeFinal || "",
    extra: { plantilla: "baneo_usr" },
  }).catch(() => {});

  return resp.json();
}

// ---- Plantilla de EMERGENCIA por WhatsApp (con ubicación) ----
async function enviarPlantillaEmergenciaWhatsapp(recipientPhoneNumber, resultado) {
  const telefonosLine = [
    resultado.telefonos?.length
      ? `📞 Llámalos al: ${resultado.telefonos[0]}`
      : "",
    resultado.whatsapp?.length
      ? ` 💬 Escríbeles al: ${resultado.whatsapp[0]}`
      : "",
  ]
    .join(" o ")
    .trim();

  const body = {
    messaging_product: "whatsapp",
    to: recipientPhoneNumber,
    type: "template",
    template: {
      name: "emergencia_user",
      language: { code: "es" },
      components: [
        {
          type: "header",
          parameters: [{ type: "text", text: "MANTEN LA CALMA" }],
        },
        {
          type: "body",
          parameters: [
            { type: "text", text: resultado.mensaje_texto },
            { type: "text", text: telefonosLine },
          ],
        },
        {
          type: "button",
          sub_type: "url",
          index: "0",
          parameters: [
            { type: "text", text: `${resultado.lat},${resultado.lng}` },
          ],
        },
      ],
    },
  };

  const resp = await fetch(WHATSAPP_URL_MESSAGES, {
    method: "POST",
    headers: {
      Authorization: `Bearer ${WHATSAPP_TOKEN}`,
      "Content-Type": "application/json",
    },
    body: JSON.stringify(body),
  });

  if (!resp.ok) {
    const errText = await resp.text();
    throw new Error(
      `Error enviando plantilla emergencia: ${resp.status} ${errText}`,
    );
  }

  guardarMensajeHistorial({
    canal: "whatsapp",
    numero_usuario: recipientPhoneNumber,
    remitente: "bot",
    tipo: "plantilla",
    contenido: resultado.mensaje_texto || "",
    extra: { plantilla: "emergencia_user" },
  }).catch(() => {});

  return resp.json();
}

// ============================================================
// TELEGRAM — ENVÍOS
// ============================================================

// opts: { replyMarkup, parseMode }
async function enviarMensajeTelegram(chatId, texto, opts = {}) {
  const { replyMarkup = null, parseMode = null } = opts;
  const chatIdReal = normalizarChatIdTelegram(chatId);

  const body = { chat_id: chatIdReal, text: texto };
  if (parseMode) body.parse_mode = parseMode;
  if (replyMarkup) body.reply_markup = replyMarkup;

  const resp = await fetch(`${TELEGRAM_API_URL}/sendMessage`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(body),
  });

  const json = await resp.json();
  if (!resp.ok)
    throw new Error(`Telegram sendMessage error: ${JSON.stringify(json)}`);

  guardarMensajeHistorial({
    canal: "telegram",
    numero_usuario: `tg_${chatIdReal}`,
    remitente: "bot",
    tipo: "texto",
    contenido: texto,
    extra: replyMarkup ? { botones: replyMarkup } : undefined,
  }).catch(() => {});

  return json;
}

async function enviarImagenTelegram(chatId, imagenUrl, caption, replyMarkup = null) {
  const chatIdReal = normalizarChatIdTelegram(chatId);

  const body = { chat_id: chatIdReal, photo: imagenUrl, caption: caption || "" };
  if (replyMarkup) body.reply_markup = replyMarkup;

  const resp = await fetch(`${TELEGRAM_API_URL}/sendPhoto`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(body),
  });

  const json = await resp.json();
  if (!resp.ok)
    throw new Error(`Telegram sendPhoto error: ${JSON.stringify(json)}`);

  guardarMensajeHistorial({
    canal: "telegram",
    numero_usuario: `tg_${chatIdReal}`,
    remitente: "bot",
    tipo: "imagen",
    contenido: caption || "",
    extra: { imagen: imagenUrl, botones: replyMarkup || undefined },
  }).catch(() => {});

  return json;
}

async function enviarStickerTelegram(chatId, stickerUrl) {
  const chatIdReal = normalizarChatIdTelegram(chatId);
  const resp = await fetch(`${TELEGRAM_API_URL}/sendSticker`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ chat_id: chatIdReal, sticker: stickerUrl }),
  });
  return resp.json();
}

async function enviarUbicacionTelegram(chatId, lat, lng, caption) {
  const chatIdReal = normalizarChatIdTelegram(chatId);

  const respLoc = await fetch(`${TELEGRAM_API_URL}/sendLocation`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({
      chat_id: chatIdReal,
      latitude: lat,
      longitude: lng,
    }),
  });

  if (!respLoc.ok) {
    const errText = await respLoc.text();
    throw new Error(
      `Error enviando ubicación Telegram: ${respLoc.status} ${errText}`,
    );
  }

  // sendLocation no acepta caption, el texto de apoyo va como mensaje aparte.
  if (caption) {
    await enviarMensajeTelegram(chatId, caption);
  }

  guardarMensajeHistorial({
    canal: "telegram",
    numero_usuario: `tg_${chatIdReal}`,
    remitente: "bot",
    tipo: "ubicacion",
    contenido: caption || "",
    extra: { lat, lng },
  }).catch(() => {});

  return { ok: true };
}

async function enviarNotaDeVozTelegram(chatId, bufferAudio) {
  const chatIdReal = normalizarChatIdTelegram(chatId);

  const form = new FormData();
  form.append("chat_id", chatIdReal);
  form.append(
    "voice",
    new Blob([bufferAudio], { type: "audio/ogg" }),
    "audio.ogg",
  );

  const resp = await fetch(`${TELEGRAM_API_URL}/sendVoice`, {
    method: "POST",
    body: form,
  });

  const json = await resp.json();
  if (!resp.ok)
    throw new Error(`Telegram sendVoice error: ${JSON.stringify(json)}`);

  guardarMensajeHistorial({
    canal: "telegram",
    numero_usuario: `tg_${chatIdReal}`,
    remitente: "bot",
    tipo: "audio",
    contenido: "",
  }).catch(() => {});

  return json;
}

// ============================================================
// TTS COMPARTIDO — WhatsApp y Telegram usan el mismo ElevenLabs
// ============================================================

async function generarAudioTTS(texto, voiceId = TTS_VOICE_ID_DEFAULT) {
  const resp = await fetch(TTS_ELEVENLABS_URL, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ text: texto, voiceId }),
  });

  if (!resp.ok) {
    throw new Error(`Error generando TTS: ${resp.status} ${await resp.text()}`);
  }

  const data = await resp.json();
  if (!data.audioContent) throw new Error("TTS no devolvió audioContent");

  return Buffer.from(data.audioContent, "base64");
}

async function subirAudioWhatsapp(bufferAudio, mimeType = "audio/ogg; codecs=opus") {
  const form = new FormData();
  form.append("messaging_product", "whatsapp");
  form.append("file", new Blob([bufferAudio], { type: mimeType }), "audio.ogg");

  const resp = await fetch(WHATSAPP_URL_MEDIA, {
    method: "POST",
    headers: { Authorization: `Bearer ${WHATSAPP_TOKEN}` }, // sin Content-Type, lo pone fetch con FormData
    body: form,
  });

  if (!resp.ok)
    throw new Error(
      `Error subiendo audio a WhatsApp: ${resp.status} ${await resp.text()}`,
    );

  const data = await resp.json();
  return data.id; // media_id
}

async function enviarAudioWhatsapp(recipientPhoneNumber, mediaId) {
  const resp = await fetch(WHATSAPP_URL_MESSAGES, {
    method: "POST",
    headers: {
      Authorization: `Bearer ${WHATSAPP_TOKEN}`,
      "Content-Type": "application/json",
    },
    body: JSON.stringify({
      messaging_product: "whatsapp",
      to: recipientPhoneNumber,
      type: "audio",
      audio: { id: mediaId, voice: true },
    }),
  });

  if (!resp.ok)
    throw new Error(
      `Error enviando audio WhatsApp: ${resp.status} ${await resp.text()}`,
    );

  return resp.json();
}

async function intentarResponderConAudioWhatsapp({ recipientPhoneNumber, texto }) {
  try {
    const audioBuffer = await generarAudioTTS(texto);
    const mediaId = await subirAudioWhatsapp(audioBuffer);
    await enviarAudioWhatsapp(recipientPhoneNumber, mediaId);

    guardarMensajeHistorial({
      canal: "whatsapp",
      numero_usuario: recipientPhoneNumber,
      remitente: "bot",
      tipo: "audio",
      contenido: texto,
    }).catch(() => {});

    return true;
  } catch (e) {
    console.error(
      "❌ [TTS WhatsApp] Falló el envío de audio (se mantiene solo texto):",
      e.message,
    );
    return false;
  }
}

async function intentarResponderConAudioTelegram({ chatId, texto }) {
  try {
    const audioBuffer = await generarAudioTTS(texto);
    await enviarNotaDeVozTelegram(chatId, audioBuffer);
    console.log("🔊 [TTS Telegram] Audio enviado correctamente | chatId:", chatId);
    return true;
  } catch (e) {
    console.error(
      "❌ [TTS Telegram] Falló el envío de audio (se mantiene solo texto):",
      e.message,
    );
    return false;
  }
}

// ============================================================
// NOTIFICACIONES PUSH (FCM) — tienda recomendada sin saldo activo
// ============================================================

async function enviarNotificacionFCM_tienda({
  token,
  title,
  body,
  link = "https://geinztech.com/share?t=scr&id=ads",
  logo = "https://firebasestorage.googleapis.com/v0/b/geinzworkapp.appspot.com/o/logo_geinz_webp.webp?alt=media&token=aa1ef1df-1bcd-48f2-9cad-a85929c3a8d0",
  image = "",
  idTienda,
  idAnuncio = "",
  tipo_notificacion,
  prioridad = "high",
}) {
  try {
    const mensaje = {
      token,
      data: {
        title: String(title),
        body: String(body),
        link: String(link),
        logo: String(logo),
        image: String(image),
        idTienda: String(idTienda),
        idAnuncio: String(idAnuncio),
        tipo_notificacion: String(tipo_notificacion),
      },
      android: { priority: prioridad },
    };
    return await admin.messaging().send(mensaje);
  } catch (error) {
    console.error("ERROR enviarNotificacionFCM:", error);
    if (error.code === "messaging/registration-token-not-registered") {
      console.log("Token inválido, debería eliminarlo de Firestore:", token);
    }
  }
}

async function enviarNotificacionSinSaldo({ id_tienda, localidad, nombre_negocio }) {
  if (!id_tienda || !localidad) {
    throw new Error("Faltan parámetros: id_tienda y localidad son requeridos.");
  }

  const localidadLower = localidad.toLowerCase().trim();

  const tiendaSnap = await db
    .collection("Tiendas")
    .doc(localidadLower)
    .collection(localidadLower)
    .doc(id_tienda)
    .get();

  if (!tiendaSnap.exists) throw new Error("Tienda no encontrada.");

  const propietario_ids = tiendaSnap.data().propietario_id || [];
  if (propietario_ids.length === 0)
    throw new Error("La tienda no tiene propietarios registrados.");

  const tokensSnaps = await Promise.all(
    propietario_ids.map((uid) =>
      db
        .collection("Trabajadores_Usuarios_Drivers")
        .doc("users")
        .collection("tokens")
        .doc(uid)
        .get()
        .catch(() => null),
    ),
  );

  const todosLosTokens = tokensSnaps.flatMap((snap) => {
    if (!snap?.exists) return [];
    return Object.values(snap.data()?.tokens || {}).filter(Boolean);
  });

  if (todosLosTokens.length === 0)
    throw new Error("No se encontraron tokens para los propietarios.");

  await Promise.all(
    todosLosTokens.map((token) =>
      enviarNotificacionFCM_tienda({
        token,
        title: `📢 ${nombre_negocio}, te están buscando`,
        body: `El asistente Daniel 🤖 recomendó a ${nombre_negocio} a un usuario interesado. pero debido a que tu plantilla premium no tenía saldo activo, no se mostró el acceso directo a tu WhatsApp 📲 y se compartió tu perfil de Geinz 🏪. Verifica tu saldo 💳 para seguir conectando con clientes potenciales directo desde whatsapp🚀`,
        link: "https://geinztech.com/share?t=scr&id=rec",
        logo: "https://firebasestorage.googleapis.com/v0/b/geinzworkapp.appspot.com/o/logo_geinz_webp.webp?alt=media&token=aa1ef1df-1bcd-48f2-9cad-a85929c3a8d0",
        idTienda: id_tienda,
        idAnuncio: "",
        tipo_notificacion: "logo",
        prioridad: "high",
      }),
    ),
  );

  return { ok: true, total_tokens: todosLosTokens.length };
}

// ============================================================
// EMERGENCIA — despacho unificado multi-canal
// ============================================================
// emergencia.js SOLO calcula el resultado (a quién contactar, si hay
// ubicación, etc). Estas 2 funciones son las ÚNICAS que efectivamente
// mandan el mensaje/plantilla de emergencia, sea por WhatsApp o
// Telegram. Las llama el dispensador general (geinz_dispatcher.js)
// después de recibir el resultado de procesarEmergencia().
// ============================================================

function construirMensajeEmergenciaTelegram(resultado) {
  let mensaje = resultado.mensaje_texto || resultado.mensaje_safe || "";
  if (resultado.telefonos?.length)
    mensaje += `\n📞 Llama al: ${resultado.telefonos.join(" / ")}`;
  if (resultado.whatsapp?.length)
    mensaje += `\n💬 WhatsApp: ${resultado.whatsapp.join(" / ")}`;
  return mensaje;
}

async function enviarRespuestaEmergencia(recipientId, resultado, canal = "whatsapp") {
  if (canal === "telegram") {
    const texto = construirMensajeEmergenciaTelegram(resultado);

    let replyMarkup = null;
    if (resultado.tiene_link && resultado.lat && resultado.lng) {
      const mapsUrl = `https://www.google.com/maps?q=${resultado.lat},${resultado.lng}`;
      replyMarkup = {
        inline_keyboard: [[{ text: "🗺️ Crear ruta", url: mapsUrl }]],
      };
    }

    return enviarMensajeTelegram(recipientId, texto, { replyMarkup });
  }

  // WhatsApp
  if (resultado.tiene_link) {
    return enviarPlantillaEmergenciaWhatsapp(recipientId, resultado);
  }
  return enviarMensajeWhatsapp(recipientId, resultado.mensaje_safe);
}

module.exports = {
  // utilidades
  contieneNumeroOLink,
  normalizarChatIdTelegram,
  // WhatsApp
  enviarMensajeWhatsapp,
  enviarImagenWhatsapp,
  enviarStickerWhatsapp,
  enviarPlantillaWhatsapp_para_tiendas,
  enviarPlantillaWhatsapp_promociones,
  enviarPlantillaBaneoWhatsapp,
  enviarPlantillaEmergenciaWhatsapp,
  intentarResponderConAudioWhatsapp,
  enviarNotificacionSinSaldo,
  enviarNotificacionFCM_tienda,
  // Telegram
  enviarMensajeTelegram,
  enviarImagenTelegram,
  enviarStickerTelegram,
  enviarUbicacionTelegram,
  enviarNotaDeVozTelegram,
  intentarResponderConAudioTelegram,
  // TTS genérico
  generarAudioTTS,
  // Emergencia multi-canal
  construirMensajeEmergenciaTelegram,
  enviarRespuestaEmergencia,
};