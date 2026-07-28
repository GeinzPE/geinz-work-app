// ============================================================
// geinz_dispatcher.js
// ============================================================
// DISPENSADOR GENERAL. Aquí viven las 3 Cloud Functions que reciben
// mensajes de los usuarios y deciden qué hacer con ellos:
//   - geinz_webhook_principal   (entrada de WhatsApp)
//   - geinz_procesar_buffer     (debounce/IA de WhatsApp)
//   - geinz_webhook_telegram    (entrada + IA de Telegram)
//
// Regla de oro: TODO envío real (mensajes, imágenes, stickers,
// plantillas, audio, emergencias) se hace llamando a funciones de
// envios.js. Este archivo y los módulos de negocio (negocio.js,
// turismo.js, promociones.js, servicios_basicos.js, geinz.js,
// emergencia.js) nunca hacen un fetch directo a Meta ni a Telegram.
// ============================================================

const { onRequest } = require("firebase-functions/v2/https");
const admin = require("firebase-admin");

if (!admin.apps.length) {
  admin.initializeApp();
}
const db = admin.firestore();

// ---- Módulos de negocio (sin cambios) ----
const {
  procesarBusquedaServiciosBasicos,
} = require("./servicios_basicos.js");
const {
  procesarBusquedaTienda,
  resolverInfoNegocio,
} = require("./negocio.js");
const { procesarPromociones } = require("./promociones.js");
const { procesarEmergencia } = require("./emergencia.js"); // 👈 ya NO envía nada, solo calcula
const { llamarGeminiGeinz } = require("./geinz.js");
const {
  resolverInfoTurismo,
  procesarBusquedaTurismo,
} = require("./turismo.js");
const { clasificarIntencion } = require("./clasificador.js");

const { guardarMensajeHistorial } = require("../historial_chats/historial_geinz.js");
const { descontarCreditosTienda } = require("../test_db2.js");
const { programarTareaDebounce } = require("../tasks.js");

// ---- TODOS los envíos (WhatsApp + Telegram) vienen de aquí ----
const {
  contieneNumeroOLink,
  // WhatsApp
  enviarMensajeWhatsapp,
  enviarImagenWhatsapp,
  enviarStickerWhatsapp,
  enviarPlantillaWhatsapp_para_tiendas,
  enviarPlantillaWhatsapp_promociones,
  enviarPlantillaBaneoWhatsapp,
  intentarResponderConAudioWhatsapp,
  enviarNotificacionSinSaldo,
  // Telegram
  enviarMensajeTelegram,
  enviarImagenTelegram,
  enviarStickerTelegram,
  intentarResponderConAudioTelegram,
  // Emergencia multi-canal
  enviarRespuestaEmergencia,
} = require("./envios_mensajes_whatsapp_telegram.js");

const OpenAI = require("openai");
const openai = new OpenAI({ apiKey: process.env.API_KEYO_OPEN_IA });

// ============================================================
// CONFIG GENERAL
// ============================================================
const MAX_TOKENS_RATE = 10;
const MS_POR_TOKEN = 2000;
const DURACION_BLOQUEO = 15000;
const PROBABILIDAD_AUDIO = 0.8;
const NUMERO_AVISO_INTERNO = "51937659216";

const CONTEXTO_DEFAULT = {
  tipo: "GEINZ",
  categoria: null,
  extra: "null",
  id: null,
  nombre: null,
};

function pick(arr) {
  return arr[Math.floor(Math.random() * arr.length)];
}

async function enviarAvisoInterno(mensajeTexto) {
  try {
    await enviarMensajeWhatsapp(NUMERO_AVISO_INTERNO, mensajeTexto);
  } catch (e) {
    console.error("❌ [enviarAvisoInterno] Falló el envío:", e.message);
  }
}

// ============================================================================
// ============================================================================
// SECCIÓN 1 — WHATSAPP
// ============================================================================
// ============================================================================

// ---- Buffer / actividad reciente (debounce) ----
async function marcarActividadReciente(numero_usuario) {
  const ref = db.collection("buffer_mensajes_geinz").doc(numero_usuario);
  await ref.set({ ultima_actividad: Date.now() }, { merge: true });
}

async function marcarProcesando(numero_usuario, activo) {
  const ref = db.collection("buffer_mensajes_geinz").doc(numero_usuario);
  try {
    if (activo) {
      await ref.set(
        { procesando: true, procesando_desde: Date.now() },
        { merge: true },
      );
    } else {
      await ref.set(
        {
          procesando: false,
          procesando_desde: admin.firestore.FieldValue.delete(),
        },
        { merge: true },
      );
    }
  } catch (e) {
    console.error(
      "❌ [marcarProcesando] Falló actualizando flag 'procesando':",
      e.message,
      "| 👤:",
      numero_usuario,
      "| activo:",
      activo,
    );
  }
}

async function hayActividadReciente(numero_usuario, ventanaMs = 8000) {
  const ref = db.collection("buffer_mensajes_geinz").doc(numero_usuario);
  const snap = await ref.get();
  if (!snap.exists) return false;

  const data = snap.data();
  const ahora = Date.now();

  const PROCESANDO_MAX_MS = 60000;
  if (data.procesando === true) {
    const desde = data.procesando_desde || 0;
    if (ahora - desde < PROCESANDO_MAX_MS) return true;
    console.warn(
      "⚠️ [hayActividadReciente] Flag 'procesando' vencido (>60s), se ignora | 👤:",
      numero_usuario,
    );
  }

  if (Array.isArray(data.mensajes) && data.mensajes.length > 0) return true;

  if (data.ultima_actividad && ahora - data.ultima_actividad < ventanaMs) {
    return true;
  }

  return false;
}

async function agregarMensajeABuffer({
  numero_usuario,
  mensajeId,
  texto,
  porAudio = false,
}) {
  const ref = db.collection("buffer_mensajes_geinz").doc(numero_usuario);
  const ahora = Date.now();

  const payload = {
    mensajes: admin.firestore.FieldValue.arrayUnion({
      id: mensajeId,
      texto,
      ts: ahora,
    }),
    last_message_id: mensajeId,
    updated_at: admin.firestore.FieldValue.serverTimestamp(),
  };

  if (porAudio) {
    payload.origen_audio = true;
  }

  await ref.set(payload, { merge: true });
}

// ---- Tipos de mensaje no soportados ----
function detectarTipoMensajeNoSoportado(mensajeWa) {
  if (!mensajeWa) return null;
  if (mensajeWa.type === "unsupported") return "pool";
  if (mensajeWa.type === "contacts") return "contacto";
  if (mensajeWa.location) return "location";
  if (mensajeWa.type === "sticker") return "sticker";
  if (mensajeWa.type === "video") return "video";
  if (mensajeWa.type === "image") return "imagen";
  if (mensajeWa.document) return "documento";
  if (mensajeWa.text?.body && /https?:\/\/[^\s]+/.test(mensajeWa.text.body)) {
    return "url";
  }
  return null;
}

function construirMensajeNoSoportado(tipo_mensaje) {
  if (tipo_mensaje === "sticker") {
    return pick([
      "😂🔥 Ese sticker seguro está buenazo. Por ahora no puedo verlo, pero igual estoy aquí para ayudarte en lo que necesites 🙌",
      "Jajaja 😎😂 me imagino que ese sticker está top. Aún no puedo abrirlos, pero dime qué necesitas y te doy una mano 💪",
      "😂 Me dejaste con la curiosidad con ese sticker 😅. Todavía no puedo verlos, pero puedo ayudarte con cualquier otra cosa 🙌",
      "Buen sticker 😂🔥. Aunque no puedo verlo por ahora, sigo aquí listo para ayudarte 💬",
    ]);
  }
  if (tipo_mensaje === "video") {
    return pick([
      "🎥✨ ¡Gracias por el video! Aún no puedo reproducirlo, pero si me cuentas de qué trata, con gusto te ayudo 🙌",
      "Recibí tu video 👀🎬. Todavía no puedo verlo directamente, pero dime qué necesitas y te apoyo 😊",
      "Buen video 🎥🔥. Aunque no puedo analizarlo aún, estoy aquí para ayudarte en lo que necesites 💬",
      "🎬 Ya me llegó tu video, gracias 🙌. Si quieres info o ayuda sobre algo relacionado, dime sin problema 😊",
    ]);
  }
  if (tipo_mensaje === "imagen") {
    return pick([
      "🖼️✨ ¡Gracias por la imagen! Aunque no puedo verla aún, si me explicas un poco, te ayudo con gusto 🙌",
      "Recibí tu imagen 👀📸. Todavía no puedo analizarla, pero dime qué necesitas y vemos cómo te ayudo 😊",
      "📸 Buen aporte. Aunque no puedo ver la imagen directamente, puedo ayudarte con lo que estés buscando 💬",
      "Imagen recibida 🖼️🙌. Si quieres información o ayuda relacionada, dime y te apoyo 😊",
    ]);
  }
  if (tipo_mensaje === "documento") {
    return pick([
      "📄✨ Documento recibido, gracias 🙌. Aún no puedo abrir archivos, pero si me explicas qué necesitas, te ayudo 😊",
      "Gracias por el documento 📄🙌. Por ahora no puedo revisarlo, pero dime de qué trata y te doy una mano 💪",
      "📄 Recibí tu archivo 👀. Aunque no puedo procesarlo todavía, puedo ayudarte si me das más contexto 💬",
      "Perfecto, documento recibido 📄✅. Si necesitas ayuda con algo relacionado, aquí estoy 🙌",
    ]);
  }
  if (tipo_mensaje === "url") {
    return pick([
      "🔗 Gracias por el enlace 🙌. Aún no puedo abrir páginas externas, pero si me cuentas qué buscas, te ayudo encantado 😊",
      "Recibí tu link 🌐. No puedo entrar directamente, pero dime qué necesitas y lo vemos juntos 💬",
      "Buen aporte con la URL 🔗. Aunque no puedo revisarla, puedo darte información si me explicas qué buscas 🙌",
    ]);
  }
  if (tipo_mensaje === "pool") {
    return pick([
      "📊 ¡Gracias por la encuesta! 🙌 Aún no puedo votar, pero si necesitas ayuda con algo, dime 😊",
      "Recibí tu formulario 📝. Todavía no puedo interactuar con encuestas, pero estoy aquí para ayudarte 💬",
      "Veo que es una encuesta 📊. No puedo participar aún, pero puedes preguntarme lo que necesites 🙌",
    ]);
  }
  if (tipo_mensaje === "contacto") {
    return pick([
      "👤 Gracias por compartir el contacto 🙌. Aún no puedo guardarlo directamente, pero dime qué necesitas y te ayudo 😊",
      "Recibí el contacto que enviaste 📲. Si necesitas algo relacionado, aquí estoy para ayudarte 💬",
      "Contacto recibido 👤✅. Aunque no puedo gestionarlo aún, puedo apoyarte en lo que necesites 🙌",
    ]);
  }
  if (tipo_mensaje === "location") {
    return pick([
      "📍 Uy, ubicación recibida 👀 por ahora no puedo leer coordenadas todavía... pero spoiler 🤫 muy pronto Geinz te va a mostrar negocios y lugares cercanos a ti solo con mandar tu ubicación 🔥",
      "📍 Ya vi que mandaste tu ubicación 👀 aún no puedo procesarla, pero spoiler 🤫 Geinz está trabajando en algo que te va a encantar... muy pronto podrás encontrar todo cerca de ti 🗺️✨",
      "Ooo ubicación detectada 📍😄 todavía no puedo hacer nada con ella por ahora... pero te cuento algo 🤫 muy pronto Geinz lanzará una actualización donde podrás ver todo lo que hay cerca de ti solo mandando tu ubicación 🚀",
      "📍 Recibí tu ubicación 🙌 aún no la puedo leer, pero spoiler 🤫 viene una actualización de Geinz donde con solo mandar tu ubicación vas a encontrar los mejores lugares y negocios cerca de ti 🔥🗺️",
    ]);
  }
  return "Gracias por tu mensaje 🙌. Aún no puedo procesar ese tipo de contenido, pero si me explicas qué necesitas, con gusto te ayudo 😊";
}

// ---- Usuario / rate limit (WhatsApp) ----
async function validarUsuario({ numero_usuario, id_user }) {
  const ahora = Date.now();

  const ref = db
    .collection("Trabajadores_Usuarios_Drivers")
    .doc("usuario_bot_geinz")
    .collection("usuario_bot_geinz")
    .doc(numero_usuario);

  const resultado = await db.runTransaction(async (tx) => {
    const doc = await tx.get(ref);

    if (!doc.exists) {
      const nuevoUsuario = {
        nombre_user: "Usuario",
        numero_user: numero_usuario,
        id_user: id_user || "",
        status: "activo",
        spam: false,
        spam_count: 0,
        ultimo_mensaje: ahora,
        rate_limit_tokens: MAX_TOKENS_RATE - 1,
        rate_limit_bloqueado_hasta: null,
        contexto: CONTEXTO_DEFAULT,
        createdAt: admin.firestore.FieldValue.serverTimestamp(),
        updated_at: admin.firestore.FieldValue.serverTimestamp(),
      };
      tx.set(ref, nuevoUsuario);
      return {
        exists: false,
        is_spam: false,
        rate_limit: false,
        ...nuevoUsuario,
      };
    }

    const data = doc.data();
    let tokens = data.rate_limit_tokens ?? MAX_TOKENS_RATE;
    let ultimoMensaje = data.ultimo_mensaje ?? ahora;
    const bloqueadoHasta = data.rate_limit_bloqueado_hasta ?? 0;

    if (bloqueadoHasta && ahora < bloqueadoHasta) {
      return {
        exists: true,
        is_spam: true,
        rate_limit: true,
        ...data,
        mensaje_spam: `🛑 Espera ${Math.ceil((bloqueadoHasta - ahora) / 1000)}s antes de escribir nuevamente.`,
      };
    }

    if (bloqueadoHasta && ahora >= bloqueadoHasta) {
      tokens = MAX_TOKENS_RATE;
      tx.update(ref, {
        spam: false,
        rate_limit_tokens: MAX_TOKENS_RATE,
        rate_limit_bloqueado_hasta: null,
        updated_at: admin.firestore.FieldValue.serverTimestamp(),
      });
      ultimoMensaje = ahora;
    }

    const tokensRecuperados = Math.floor(
      (ahora - ultimoMensaje) / MS_POR_TOKEN,
    );
    tokens = Math.min(MAX_TOKENS_RATE, tokens + tokensRecuperados);

    if (tokens <= 0) {
      const bloqueadoHastaNuevo = ahora + DURACION_BLOQUEO;
      tx.update(ref, {
        spam: true,
        spam_count: admin.firestore.FieldValue.increment(1),
        ultimo_mensaje: ahora,
        rate_limit_tokens: 0,
        rate_limit_bloqueado_hasta: bloqueadoHastaNuevo,
        updated_at: admin.firestore.FieldValue.serverTimestamp(),
      });
      return {
        exists: true,
        is_spam: true,
        rate_limit: true,
        ...data,
        spam: true,
        rate_limit_tokens: 0,
        rate_limit_bloqueado_hasta: bloqueadoHastaNuevo,
        mensaje_spam: `🚫 Demasiados mensajes. Bloqueado ${DURACION_BLOQUEO / 1000}s.`,
      };
    }

    tx.update(ref, {
      spam: false,
      ultimo_mensaje: ahora,
      rate_limit_tokens: tokens - 1,
      rate_limit_bloqueado_hasta: null,
      updated_at: admin.firestore.FieldValue.serverTimestamp(),
    });

    return {
      exists: true,
      is_spam: false,
      rate_limit: false,
      ...data,
      spam: false,
      rate_limit_tokens: tokens - 1,
      rate_limit_bloqueado_hasta: null,
    };
  });

  if (resultado.exists === false) {
    enviarAvisoInterno("Usuario nuevo escribió a Daniel");
  }

  return resultado;
}

async function actualizarContextoUsuario(numero_usuario, nuevoContexto) {
  const ref = db
    .collection("Trabajadores_Usuarios_Drivers")
    .doc("usuario_bot_geinz")
    .collection("usuario_bot_geinz")
    .doc(numero_usuario);

  await ref.update({
    contexto: nuevoContexto,
    updated_at: admin.firestore.FieldValue.serverTimestamp(),
  });
}

// ---- Audio WhatsApp (Whisper) ----
function construirMensajeEscucha(nombreUsuario) {
  const n = nombreUsuario || "amigo";
  return pick([
    `${n} ya te escuché 👂 dame un momento...`,
    `captado ${n} 🎙️ ya voy...`,
    `oki ${n}, en un seg te respondo 👌`,
    `ya te escuché ${n}, espérame un momento ⏳`,
    `recibido ${n} 📨 ya estoy en eso...`,
    `sí sí ${n}, ya escuché todo 😄 un momento...`,
    `ok ${n} ya voy 🏃`,
    `escuchado ${n}, dame un seg 🙌`,
  ]);
}

async function obtenerUrlMediaWhatsapp(mediaId) {
  const url = `https://graph.facebook.com/v20.0/${mediaId}`;
  const resp = await fetch(url, {
    headers: { Authorization: `Bearer ${process.env.ID_API_WHATSAPP}` },
  });
  if (!resp.ok)
    throw new Error(
      `Error obteniendo URL de media: ${resp.status} ${await resp.text()}`,
    );
  const data = await resp.json();
  return data.url;
}

async function descargarAudioBinarioWhatsapp(mediaUrl) {
  const resp = await fetch(mediaUrl, {
    headers: { Authorization: `Bearer ${process.env.ID_API_WHATSAPP}` },
  });
  if (!resp.ok)
    throw new Error(
      `Error descargando audio: ${resp.status} ${await resp.text()}`,
    );
  return Buffer.from(await resp.arrayBuffer());
}

const PRECIO_WHISPER_USD_POR_MINUTO = 0.006;
const TIPO_CAMBIO_USD_PEN = 3.75;

async function transcribirAudio(bufferAudio, nombreArchivo = "audio.ogg") {
  const file = new File([bufferAudio], nombreArchivo, { type: "audio/ogg" });
  const transcription = await openai.audio.transcriptions.create({
    file,
    model: "whisper-1",
    response_format: "verbose_json",
  });

  const duracionSegundos = transcription.duration || 0;
  const duracionMinutos = duracionSegundos / 60;
  const costoUsd = duracionMinutos * PRECIO_WHISPER_USD_POR_MINUTO;
  const costoSoles = costoUsd * TIPO_CAMBIO_USD_PEN;

  return {
    texto: transcription.text || "",
    duracion_segundos: Number(duracionSegundos.toFixed(2)),
    costo_usd: Number(costoUsd.toFixed(6)),
    costo_soles: Number(costoSoles.toFixed(6)),
  };
}

async function procesarAudioWhatsapp({ mediaId, recipientPhoneNumber, nombreUsuario }) {
  const mensajeEscucha = construirMensajeEscucha(nombreUsuario);
  const [, mediaUrl] = await Promise.all([
    enviarMensajeWhatsapp(recipientPhoneNumber, mensajeEscucha),
    obtenerUrlMediaWhatsapp(mediaId),
  ]);
  const audioBuffer = await descargarAudioBinarioWhatsapp(mediaUrl);
  const resultado = await transcribirAudio(audioBuffer);
  return { mensajefinal: resultado.texto, whisper: resultado };
}

// ---- Mensajes de "espera" según categoría ----
function construirMensajeEspera(categoria, nombreUsuario) {
  const n = nombreUsuario || "amigo";

  if (categoria === "EMERGENCIA") {
    return pick([
      `${n} tranquilo, ya estoy viendo esto contigo, dame un segundo 🚨`,
      `${n} entendido, dame un momento que estoy sacando la info de emergencia 🚨`,
      `ok ${n}, ya estoy en eso, un segundo por favor 🚨`,
    ]);
  }
  if (categoria === "PROMOCIONES") {
    return pick([
      `a tus órdenes ${n}, voy a buscar lo mejor en promos para ti 🔥`,
      `por supuesto ${n}, buscaré lo mejor para ti en segundos ⚡`,
      `buscando las mejores ofertas ahora mismo ${n}... 🔍`,
      `un momento ${n}, rastreando los mejores precios para ti 💸`,
      `espera un segundo ${n}, estoy buscando las promos más top 🎯`,
      `ya voy ${n}, déjame encontrar lo más barato para ti 👀`,
      `buscando... no te muevas ${n} que ya regreso con algo bueno 😏`,
      `escaneando todas las ofertas disponibles para ti ${n} 📡`,
      `cargando las mejores promos del momento ${n}... ⏳`,
      `dale ${n}, ya estoy buscando lo que necesitas 💪`,
    ]);
  }
  if (categoria === "NEGOCIO") {
    return pick([
      `🔎 A ver ${n} déjame buscar bien, quiero mostrarte opciones que te sirvan...`,
      `📍 Espérame un momento ${n} que estoy revisando qué hay disponible...`,
      `✨ Dame un seg ${n} que estoy viendo las opciones para elegir las mejores...`,
      `🔍 Aguanta ${n} que estoy filtrando bien antes de mandarte algo...`,
      `⚡ Un momentito ${n} que estoy mirando qué hay por ahí...`,
      `🛍️ Oe ${n} déjame revisar bien, no te mando la primera que aparece...`,
      `🔎 Ya voy ${n}, estoy buscando algo que realmente te sirva...`,
      `✨ Espérame ${n} que estoy filtrando lo mejor, al toque te cuento...`,
      `⚡ A ver ${n}, déjame ver bien qué opciones hay antes de mandarte algo...`,
      `🔍 Un seg ${n} que estoy mirando bien, quiero mandarte algo que valga...`,
    ]);
  }
  if (categoria === "TURISMO") {
    return pick([
      `🌍 A ver ${n} déjame buscar bien, quiero mostrarte opciones que valgan la pena...`,
      `✨ Espérame un momento ${n} que estoy revisando qué lugares pueden gustarte...`,
      `🔎 Dame un seg ${n} que estoy viendo las opciones, quiero que encuentres algo bueno...`,
      `📍 Aguanta ${n} que estoy buscando las mejores opciones para ti...`,
      `🍀 Un momentito ${n} que estoy mirando qué hay por ahí...`,
      `🌊 Oe ${n} déjame revisar bien, hay varias opciones y quiero mandarte la mejor...`,
      `🗺️ Ya voy ${n}, estoy filtrando lo más bacán para que no pierdas el tiempo...`,
      `✨ Espérame ${n} que estoy mirando bien, no te mando cualquier cosa...`,
      `🔍 A ver ${n}, déjame buscar algo que realmente te sirva...`,
      `⚡ Un seg ${n} que estoy viendo qué lugares valen la visita...`,
    ]);
  }
  if (categoria === "SERVICIOS_BASICOS") {
    return pick([
      `📋 Ya voy ${n}, dame un segundo que te paso esa info...`,
      `🔎 Un momento ${n}, estoy sacando esos datos para ti...`,
      `📞 Espérame ${n}, ya te consigo esa información...`,
      `✨ Dame un seg ${n}, ya te traigo lo que necesitas...`,
      `📲 Aguanta ${n}, estoy buscando ese dato...`,
      `⚡ Ya voy ${n}, en un momento te paso todo eso...`,
    ]);
  }
  return null;
}

function formatearFecha(fechaRaw) {
  if (!fechaRaw) return "fecha no disponible";
  let fecha;
  if (typeof fechaRaw === "string" && fechaRaw.includes("_seconds")) {
    try {
      fecha = new Date(JSON.parse(fechaRaw)._seconds * 1000);
    } catch (e) {
      return "fecha inválida";
    }
  } else if (typeof fechaRaw === "object" && fechaRaw?._seconds) {
    fecha = new Date(fechaRaw._seconds * 1000);
  } else if (typeof fechaRaw === "number") {
    fecha = new Date(fechaRaw * 1000);
  } else if (!isNaN(fechaRaw)) {
    fecha = new Date(Number(fechaRaw) * 1000);
  } else {
    fecha = new Date(fechaRaw);
  }
  if (isNaN(fecha)) return "fecha inválida";
  return fecha.toLocaleString("es-PE", {
    day: "2-digit",
    month: "long",
    year: "numeric",
    hour: "2-digit",
    minute: "2-digit",
  });
}

function construirMensajeBaneado(fechaBloqueo, motivoBloqueo) {
  const fechaBonita = formatearFecha(fechaBloqueo);
  const motivo = motivoBloqueo || "No especificado";
  return pick([
    `🚫 Lo sentimos. Tu cuenta fue bloqueada el ${fechaBonita} debido a: "${motivo}". Geinz prioriza la seguridad de la plataforma. comunicate al 958120920 para ver el estado de tu cuenta`,
    `⚠️ Acceso restringido. Cuenta deshabilitada el ${fechaBonita}. Motivo: "${motivo} comunicate al 958120920 para ver el estado de tu cuenta".`,
    `🚫 Cuenta bloqueada el ${fechaBonita}. Razón: "${motivo}".`,
    `⚠️ Cuenta deshabilitada desde el ${fechaBonita}. Motivo: "${motivo} comunicate al 958120920 para ver el estado de tu cuenta".`,
  ]);
}

function construirMensajeModeracion(nombreUsuario) {
  const n = nombreUsuario || "causa";
  return pick([
    `⚠️ ${n}, tu mensaje ha sido detectado como contenido que incumple las normas de uso de Geinz. Te pedimos mantener una comunicación respetuosa. De continuar, tu número podría ser restringido permanentemente.`,
    `🚫 ${n}, hemos identificado contenido inapropiado según las políticas de Geinz. Por favor evita este tipo de lenguaje. El uso reiterado puede llevar a la suspensión definitiva de tu acceso.`,
    `⚠️ ${n}, tu mensaje no cumple con las normas de la plataforma Geinz. Te recomendamos moderar tu contenido. El incumplimiento continuo podría resultar en el bloqueo permanente de tu número.`,
    `🚨 ${n}, se detectó un mensaje que infringe nuestras políticas. En Geinz priorizamos el respeto y la seguridad. Si esto persiste, tu número podría ser bloqueado permanentemente.`,
    `⚠️ ${n}, detectamos contenido que va contra las normas de Geinz. Por favor mantén un lenguaje adecuado. El incumplimiento reiterado puede ocasionar la suspensión definitiva de tu cuenta.`,
    `🚫 ${n}, tu mensaje ha sido marcado por incumplir nuestras políticas. Te pedimos mayor cuidado al comunicarte. De persistir, podrías perder el acceso a Geinz de forma permanente.`,
  ]);
}

function construirMensajePreguntarPromo(nombreUsuario) {
  const n = nombreUsuario || "amigo";
  return pick([
    `Mira ${n} 😊 para darte las promos que necesitas, ¿me mandas algún nombre de tienda o categoría?`,
    `${n}, cuéntame qué tienda o categoría te interesa y te busco las promos 🛍️`,
    `Para ayudarte mejor ${n} 🙌 ¿me dices el nombre de una tienda o qué tipo de producto buscas?`,
    `${n} 👋 dame un nombre de tienda o categoría y te tiro las promos al toque`,
    `Necesito un poco más de info ${n} 😅 ¿qué tienda o categoría se te antoja?`,
    `${n}, ¿tienes en mente alguna tienda o categoría? Así te busco algo bueno 🔥`,
    `Oye ${n} 🤗 dime una tienda o categoría y vemos qué promos hay disponibles`,
    `${n} 💭 ¿qué tienda o tipo de producto te interesa? Así afino la búsqueda`,
    `Para acertarle mejor ${n} 🎯 ¿me pasas el nombre de una tienda o categoría?`,
    `${n}, dame una pista 😄 ¿tienda específica o categoría que te llame la atención?`,
  ]);
}

function construirMensajeSinPromo(referencia, nombreUsuario, tipoReferencia = "tienda") {
  const n = nombreUsuario || "amigo";
  const otra = tipoReferencia === "categoria" ? "otra categoría" : "otra tienda";
  return pick([
    `Oye ${n} 😅 no encontré promociones de ${referencia} ni nada parecido.\n¿Tienes ${otra} en mente?`,
    `${n}, busqué y busqué pero no hay promociones de ${referencia} 🔍\n¿Se te ocurre ${otra} que probemos?`,
    `Uy ${n} 😬 de ${referencia} no salió ninguna promoción.\n¿Quieres que revise ${otra}?`,
    `${n} 👀 no hallé promociones de ${referencia} ni algo similar.\n¿Tienes ${otra} en la cabeza?`,
    `Nada de nada con las promociones de ${referencia}, ${n} 🙈\n¿Probamos con ${otra}?`,
    `${n}, revisé todo y ${referencia} no tiene promociones activas 😕\n¿Hay ${otra} que te interese?`,
    `Che ${n} 😅 ${referencia} no tiene promociones por ahora.\n¿Se te viene ${otra} a la mente?`,
    `${n} 🤔 no aparecieron promociones de ${referencia}.\n¿Quieres que busque en ${otra}?`,
  ]);
}

const CAMPOS_TRANSITORIOS = ["ids_promos", "mas_de_uno", "continuidad"];

function limpiarCamposPromoDelContexto(contexto) {
  const limpio = { ...(contexto || {}) };
  for (const campo of CAMPOS_TRANSITORIOS) {
    delete limpio[campo];
  }
  return limpio;
}

function prepararContextoContinuidad(contextoUsuario) {
  let obj;
  try {
    obj =
      typeof contextoUsuario === "string"
        ? JSON.parse(contextoUsuario)
        : { ...(contextoUsuario || {}) };
  } catch (e) {
    obj = {};
  }

  obj.continuidad = true;

  const idsPromos = Array.isArray(obj.ids_promos)
    ? obj.ids_promos.filter((id) => id != null && String(id).trim() !== "")
    : [];
  obj.mas_de_uno = idsPromos.length > 1;

  return obj;
}

// ============================================================
// geinz_webhook_principal — entrada de WhatsApp (Meta webhook)
// ============================================================
exports.geinz_webhook_principal = onRequest(
  { concurrency: 20, cpu: 1 },
  async (req, res) => {
    if (req.method === "GET") {
      const VERIFY_TOKEN = process.env.WHATSAPP_VERIFY_TOKEN;
      const mode = req.query["hub.mode"];
      const token = req.query["hub.verify_token"];
      const challenge = req.query["hub.challenge"];

      if (mode === "subscribe" && token === VERIFY_TOKEN) {
        return res.status(200).send(challenge);
      }
      return res.sendStatus(403);
    }
    const inicio = Date.now();

    try {
      const entry = req.body?.entry?.[0];
      const value = entry?.changes?.[0]?.value;
      const mensajeWa = value?.messages?.[0];
      const contacto = value?.contacts?.[0];

      if (!mensajeWa || !contacto) {
        return res
          .status(200)
          .json({ ok: true, info: "Evento sin mensaje procesable" });
      }

      const numero_usuario = contacto.wa_id;
      const id_user = value.metadata?.phone_number_id || "";
      if (mensajeWa.type === "audio" || mensajeWa.type === "text") {
        await marcarActividadReciente(numero_usuario);
      }

      const tipoNoSoportado = detectarTipoMensajeNoSoportado(mensajeWa);
      if (tipoNoSoportado) {
        if (tipoNoSoportado === "sticker" || tipoNoSoportado === "pool") {
          const activo = await hayActividadReciente(numero_usuario);
          if (activo) {
            return res.status(200).json({
              ok: true,
              info: "Sticker ignorado porque hay un mensaje en proceso",
              numero_usuario,
            });
          }
        }

        const mensajeEnlatado = construirMensajeNoSoportado(tipoNoSoportado);
        guardarMensajeHistorial({
          canal: "whatsapp",
          numero_usuario,
          remitente: "usuario",
          tipo: tipoNoSoportado,
          contenido: "",
          mensaje_id: mensajeWa.id,
        }).catch(() => {});
        await enviarMensajeWhatsapp(numero_usuario, mensajeEnlatado);

        return res.status(200).json({
          ok: true,
          tipo_mensaje: tipoNoSoportado,
          mensaje_enviado: mensajeEnlatado,
          numero_usuario,
        });
      }

      const usuarioInfo = await validarUsuario({ numero_usuario, id_user });
      const nombre_user = usuarioInfo.nombre_user || "Usuario";

      if (usuarioInfo.is_spam) {
        await enviarMensajeWhatsapp(numero_usuario, usuarioInfo.mensaje_spam);
        return res.status(200).json({
          ok: true,
          bloqueado: true,
          motivo: usuarioInfo.mensaje_spam,
        });
      }

      if (usuarioInfo.fecha_bloqueo && usuarioInfo.motivo_bloqueo) {
        const mensajeBan = construirMensajeBaneado(
          usuarioInfo.fecha_bloqueo,
          usuarioInfo.motivo_bloqueo,
        );
        await enviarMensajeWhatsapp(numero_usuario, mensajeBan);
        return res.status(200).json({ ok: true, baneado: true, mensaje: mensajeBan });
      }

      let mensajeFinal = "";
      let whisperInfo = null;

      if (mensajeWa.type === "audio") {
        const resultadoAudio = await procesarAudioWhatsapp({
          mediaId: mensajeWa.audio.id,
          recipientPhoneNumber: numero_usuario,
          nombreUsuario: nombre_user,
        });
        mensajeFinal = resultadoAudio.mensajefinal;
        whisperInfo = resultadoAudio.whisper;
      } else if (mensajeWa.type === "text") {
        mensajeFinal = mensajeWa.text?.body || "";
      } else {
        return res.status(200).json({
          ok: true,
          info: `Tipo de mensaje no soportado: ${mensajeWa.type}`,
        });
      }

      guardarMensajeHistorial({
        canal: "whatsapp",
        numero_usuario,
        nombre_usuario: nombre_user,
        remitente: "usuario",
        tipo: mensajeWa.type === "audio" ? "audio" : "texto",
        contenido: mensajeFinal,
        mensaje_id: mensajeWa.id,
      }).catch(() => {});

      if (!mensajeFinal.trim()) {
        return res.status(200).json({ ok: true, info: "Mensaje vacío tras resolución" });
      }

      const mensajeId = mensajeWa.id;

      await Promise.all([
        agregarMensajeABuffer({
          numero_usuario,
          mensajeId,
          texto: mensajeFinal,
          porAudio: mensajeWa.type === "audio",
        }),
        programarTareaDebounce({ numero_usuario, mensajeId }),
      ]);

      return res.status(200).json({ ok: true, buffered: true, mensajeId });
    } catch (error) {
      console.error("❌ Error geinz_webhook_principal:", error.message);
      const tiempo_ms = Date.now() - inicio;
      return res.status(500).json({ ok: false, error: error.message, tiempo_ms });
    }
  },
);

// ============================================================
// leerYValidarBuffer — helper de geinz_procesar_buffer
// ============================================================
async function leerYValidarBuffer({ numero_usuario, mensajeId }) {
  const ref = db.collection("buffer_mensajes_geinz").doc(numero_usuario);
  const snap = await ref.get();

  if (!snap.exists) {
    return { valido: false, motivo: "buffer_no_existe" };
  }

  const data = snap.data();

  if (data.last_message_id !== mensajeId) {
    return {
      valido: false,
      motivo: "task_obsoleta",
      last_message_id: data.last_message_id,
    };
  }

  const mensajes = Array.isArray(data.mensajes) ? data.mensajes : [];
  const ordenados = [...mensajes].sort((a, b) => (a.ts || 0) - (b.ts || 0));
  const textoConcatenado = ordenados.map((m) => m.texto).join(" ");
  const vinoDeAudio = data.origen_audio === true;

  ref
    .update({
      mensajes: admin.firestore.FieldValue.delete(),
      last_message_id: admin.firestore.FieldValue.delete(),
      origen_audio: admin.firestore.FieldValue.delete(),
      procesando: true,
      procesando_desde: Date.now(),
    })
    .catch((e) =>
      console.error("❌ [leerYValidarBuffer] Falló marcar procesando:", e.message),
    );

  return {
    valido: true,
    textoConcatenado,
    cantidad: ordenados.length,
    vinoDeAudio,
  };
}

// ============================================================
// geinz_procesar_buffer — clasifica y responde (WhatsApp)
// ============================================================
exports.geinz_procesar_buffer = onRequest(
  {
    region: "us-central1",
    invoker: "geinz-tasks-invoker@geinzworkapp.iam.gserviceaccount.com",
  },
  async (req, res) => {
    const inicio = Date.now();
    let numero_usuario_actual = null;

    try {
      const { numero_usuario, mensajeId } = req.body || {};
      numero_usuario_actual = numero_usuario;

      if (!numero_usuario || !mensajeId) {
        return res.status(200).json({ ok: true, info: "Payload incompleto, ignorado" });
      }

      const refUsuario = db
        .collection("Trabajadores_Usuarios_Drivers")
        .doc("usuario_bot_geinz")
        .collection("usuario_bot_geinz")
        .doc(numero_usuario);

      const [resultadoBuffer, usuarioSnap] = await Promise.all([
        leerYValidarBuffer({ numero_usuario, mensajeId }),
        refUsuario.get(),
      ]);

      if (!resultadoBuffer.valido) {
        return res
          .status(200)
          .json({ ok: true, descartado: true, motivo: resultadoBuffer.motivo });
      }

      try {
        const mensajeFinal = resultadoBuffer.textoConcatenado;
        const vinoDeAudio = resultadoBuffer.vinoDeAudio === true;

        const usuarioData = usuarioSnap.exists ? usuarioSnap.data() : {};
        const nombre_user = usuarioData.nombre_user || "Usuario";
        const contextoUsuario = usuarioData.contexto || CONTEXTO_DEFAULT;

        const { categoria, tokens: tokensClasificador } = await clasificarIntencion(
          mensajeFinal,
          contextoUsuario,
        );

        const mensajeEsperaInicial = construirMensajeEspera(categoria, nombre_user);
        if (mensajeEsperaInicial) {
          enviarMensajeWhatsapp(numero_usuario, mensajeEsperaInicial).catch((e) =>
            console.error("❌ Falló mensaje de espera:", e.message),
          );
        }

        // ---------------- EMERGENCIA ----------------
        if (categoria === "EMERGENCIA") {
          const contextoActualizadoEmergencia = {
            ...limpiarCamposPromoDelContexto(contextoUsuario),
            tipo: "EMERGENCIA",
          };
          const promesaContexto = actualizarContextoUsuario(
            numero_usuario,
            contextoActualizadoEmergencia,
          );

          // 👇 procesarEmergencia YA NO ENVÍA NADA. Solo calcula. El
          // ÚNICO que dispara el envío real es el dispensador, aquí
          // mismo, llamando a enviarRespuestaEmergencia (envios.js).
          const resultadoEmergencia = await procesarEmergencia({
            localidad: "barranca",
            mensaje: mensajeFinal,
            nombreUsuario: nombre_user,
          });

          if (!resultadoEmergencia.error) {
            await enviarRespuestaEmergencia(
              numero_usuario,
              resultadoEmergencia,
              "whatsapp",
            );
          }

          await promesaContexto;

          const tiempo_ms = Date.now() - inicio;
          return res.status(200).json({
            ok: true,
            categoria: "EMERGENCIA",
            mensaje_usuario: mensajeFinal,
            nombre_usuario: nombre_user,
            numero_usuario,
            contexto_usuario: contextoActualizadoEmergencia,
            resultado_emergencia: resultadoEmergencia,
            tokens_usados: {
              clasificador: { modelo: "gpt-5.4-mini", ...tokensClasificador },
              emergencia: resultadoEmergencia?._debug || null,
            },
            tiempo_ms,
          });
        }

        // ---------------- NEGOCIO ----------------
        if (categoria === "NEGOCIO") {
          const resultadoTienda = await procesarBusquedaTienda({
            mensaje: mensajeFinal,
            contexto_previo: contextoUsuario,
            localidad: "barranca",
            excluir_id: contextoUsuario?.id || null,
            nombre_usuario: nombre_user,
          });

          if (resultadoTienda.redirigir_a_servicios === true) {
            const resultadoServicio = await procesarBusquedaServiciosBasicos({
              mensaje: mensajeFinal,
              contexto_previo: contextoUsuario,
              localidad: "barranca",
              nombre_usuario: nombre_user,
            });

            const contextoActualizadoServicio = {
              ...limpiarCamposPromoDelContexto(contextoUsuario),
              tipo: "SERVICIOS_BASICOS",
              categoria: null,
              id: resultadoServicio.id || null,
              nombre: resultadoServicio.nombre_servicio || null,
              extra: resultadoServicio.data || "null",
            };
            const promesaContextoServicio = actualizarContextoUsuario(
              numero_usuario,
              contextoActualizadoServicio,
            );

            if (resultadoServicio.imagen) {
              try {
                await enviarImagenWhatsapp(
                  numero_usuario,
                  resultadoServicio.imagen,
                  resultadoServicio.mensaje_safe,
                );
              } catch (e) {
                console.error(
                  "❌ [NEGOCIO→SERVICIOS_BASICOS] Falló imagen, texto de respaldo:",
                  e.message,
                );
                if (resultadoServicio.mensaje_safe) {
                  await enviarMensajeWhatsapp(numero_usuario, resultadoServicio.mensaje_safe);
                }
              }
            } else if (resultadoServicio.mensaje_safe) {
              await enviarMensajeWhatsapp(numero_usuario, resultadoServicio.mensaje_safe);
            }

            await promesaContextoServicio;

            const tiempo_ms_redirigido = Date.now() - inicio;
            return res.status(200).json({
              ok: true,
              categoria: "SERVICIOS_BASICOS",
              subcaso: "redirigido_desde_negocio",
              mensaje_usuario: mensajeFinal,
              nombre_usuario: nombre_user,
              numero_usuario,
              contexto_usuario: contextoActualizadoServicio,
              resultado_servicio: resultadoServicio,
              tokens_usados: {
                clasificador: { modelo: "gpt-5.4-mini", ...tokensClasificador },
                servicios_basicos: resultadoServicio.tokens_usados,
              },
              tiempo_ms: tiempo_ms_redirigido,
            });
          }

          if (resultadoTienda.pedir_aclaracion === true) {
            const contextoActualizadoAclaracion = {
              ...limpiarCamposPromoDelContexto(contextoUsuario),
              tipo: "GEINZ",
              categoria: null,
              id: null,
              nombre: null,
              subcategoria: null,
              extra: "ESPERANDO_ELECCION:negocio,turismo,promociones",
            };
            const promesaContextoAclaracion = actualizarContextoUsuario(
              numero_usuario,
              contextoActualizadoAclaracion,
            );

            if (resultadoTienda.mensaje_safe) {
              await enviarMensajeWhatsapp(numero_usuario, resultadoTienda.mensaje_safe);
            }

            await promesaContextoAclaracion;

            const tiempo_ms_aclaracion = Date.now() - inicio;
            return res.status(200).json({
              ok: true,
              categoria: "NEGOCIO",
              subcaso: "pedir_aclaracion",
              mensaje_usuario: mensajeFinal,
              nombre_usuario: nombre_user,
              numero_usuario,
              contexto_usuario: contextoActualizadoAclaracion,
              tokens_usados: {
                clasificador: { modelo: "gpt-5.4-mini", ...tokensClasificador },
                negocio: resultadoTienda.tokens_usados,
              },
              tiempo_ms: tiempo_ms_aclaracion,
            });
          }

          const contextoActualizadoNegocio = {
            ...limpiarCamposPromoDelContexto(contextoUsuario),
            tipo: "NEGOCIO",
            categoria: resultadoTienda.cat_detectada || null,
            subcategoria: resultadoTienda.subcategoria || null,
            id: resultadoTienda.id || null,
            nombre: resultadoTienda.nombre_negocio || null,
            extra: resultadoTienda.data || "null",
          };
          const promesaContexto = actualizarContextoUsuario(
            numero_usuario,
            contextoActualizadoNegocio,
          );

          if (resultadoTienda.plantilla === true) {
            try {
              await enviarPlantillaWhatsapp_para_tiendas({
                recipientPhoneNumber: numero_usuario,
                imagen: resultadoTienda.imagen,
                mensaje_safe: resultadoTienda.mensaje_safe,
                alias_tienda: resultadoTienda.alias_tienda,
                id: resultadoTienda.id,
                token_wsap: resultadoTienda.token_wsap,
              });
            } catch (e) {
              console.error("❌ [NEGOCIO] Falló plantilla, mando texto de respaldo:", e.message);
              if (resultadoTienda.mensaje_safe) {
                await enviarMensajeWhatsapp(numero_usuario, resultadoTienda.mensaje_safe);
              }
            }
          } else if (resultadoTienda.mensaje_safe) {
            await enviarMensajeWhatsapp(numero_usuario, resultadoTienda.mensaje_safe);
            if (resultadoTienda.era_plantilla_pero_misio === true) {
              enviarNotificacionSinSaldo({
                id_tienda: resultadoTienda.id,
                localidad: "barranca",
                nombre_negocio: resultadoTienda.nombre_negocio,
              }).catch((e) =>
                console.error("❌ [NEGOCIO] Falló notificar tienda sin saldo:", e.message),
              );
            }
          }

          await promesaContexto;

          const tiempo_ms = Date.now() - inicio;
          return res.status(200).json({
            ok: true,
            categoria: "NEGOCIO",
            mensaje_usuario: mensajeFinal,
            nombre_usuario: nombre_user,
            numero_usuario,
            contexto_usuario: contextoActualizadoNegocio,
            resultado_negocio: resultadoTienda,
            tokens_usados: {
              clasificador: { modelo: "gpt-5.4-mini", ...tokensClasificador },
              negocio: resultadoTienda.tokens_usados,
            },
            tiempo_ms,
          });
        }

        // ---------------- GEINZ ----------------
        if (categoria === "GEINZ") {
          const { resultado: respuestaGeinz, tokens: tokensGeinz } =
            await llamarGeminiGeinz(mensajeFinal, nombre_user);

          const contextoActualizadoGeinz = {
            ...limpiarCamposPromoDelContexto(contextoUsuario),
            tipo: "GEINZ",
            categoria: null,
            id: null,
            nombre: null,
            extra: respuestaGeinz.extra || "null",
          };
          const promesaContexto = actualizarContextoUsuario(
            numero_usuario,
            contextoActualizadoGeinz,
          );

          if (respuestaGeinz.mensaje) {
            const debeIntentarAudio =
              vinoDeAudio &&
              !contieneNumeroOLink(respuestaGeinz.mensaje) &&
              Math.random() < PROBABILIDAD_AUDIO;

            let audioEnviado = false;
            if (debeIntentarAudio) {
              audioEnviado = await intentarResponderConAudioWhatsapp({
                recipientPhoneNumber: numero_usuario,
                texto: respuestaGeinz.mensaje,
              });
            }

            if (!audioEnviado) {
              await enviarMensajeWhatsapp(numero_usuario, respuestaGeinz.mensaje);
            }
          }

          await promesaContexto;

          const tiempo_ms = Date.now() - inicio;
          return res.status(200).json({
            ok: true,
            categoria: "GEINZ",
            mensaje_usuario: mensajeFinal,
            nombre_usuario: nombre_user,
            numero_usuario,
            contexto_usuario: contextoActualizadoGeinz,
            resultado_geinz: respuestaGeinz,
            tokens_usados: {
              clasificador: { modelo: "gpt-5.4-mini", ...tokensClasificador },
              geinz: { modelo: "gemini-2.5-flash", ...tokensGeinz },
            },
            tiempo_ms,
          });
        }

        // ---------------- TURISMO ----------------
        if (categoria === "TURISMO") {
          const resultadoTurismo = await procesarBusquedaTurismo({
            mensaje: mensajeFinal,
            contexto_previo: contextoUsuario,
            localidad: "barranca",
            usuario: nombre_user,
          });

          const contextoActualizadoTurismo = {
            ...limpiarCamposPromoDelContexto(contextoUsuario),
            tipo: "TURISMO",
            categoria: resultadoTurismo.categoria || null,
            id: resultadoTurismo.id || null,
            nombre: resultadoTurismo.nombre || null,
            extra: resultadoTurismo.data || "null",
          };
          const promesaContexto = actualizarContextoUsuario(
            numero_usuario,
            contextoActualizadoTurismo,
          );

          let mensajeResultadoEnviado = false;

          if (resultadoTurismo.imagen) {
            try {
              await enviarImagenWhatsapp(
                numero_usuario,
                resultadoTurismo.imagen,
                resultadoTurismo.mensaje_safe,
              );
              mensajeResultadoEnviado = true;
            } catch (e) {
              console.error("❌ [TURISMO] Falló imagen, texto de respaldo:", e.message);
              if (resultadoTurismo.mensaje_safe) {
                try {
                  await enviarMensajeWhatsapp(numero_usuario, resultadoTurismo.mensaje_safe);
                  mensajeResultadoEnviado = true;
                } catch (e2) {
                  console.error("❌ [TURISMO] Falló también texto de respaldo:", e2.message);
                }
              }
            }
          } else if (resultadoTurismo.mensaje_safe) {
            try {
              await enviarMensajeWhatsapp(numero_usuario, resultadoTurismo.mensaje_safe);
              mensajeResultadoEnviado = true;
            } catch (e) {
              console.error("❌ [TURISMO] Falló texto de resultado:", e.message);
            }
          }

          if (resultadoTurismo.siker && mensajeResultadoEnviado) {
            try {
              await enviarStickerWhatsapp(numero_usuario, resultadoTurismo.siker);
            } catch (e) {
              console.error("❌ [TURISMO] Falló sticker:", e.message);
            }
          }

          await promesaContexto;

          const tiempo_ms = Date.now() - inicio;
          return res.status(200).json({
            ok: true,
            categoria: "TURISMO",
            mensaje_usuario: mensajeFinal,
            nombre_usuario: nombre_user,
            numero_usuario,
            contexto_usuario: contextoActualizadoTurismo,
            resultado_turismo: resultadoTurismo,
            tokens_usados: {
              clasificador: { modelo: "gpt-5.4-mini", ...tokensClasificador },
              turismo: resultadoTurismo.tokens_usados,
            },
            tiempo_ms,
          });
        }

        // ---------------- CONTINUIDAD_INFO ----------------
        if (categoria === "CONTINUIDAD_INFO") {
          const tiposValidosParaContinuidad = ["NEGOCIO", "PROMOCIONES", "TURISMO"];
          const tieneContextoValido =
            tiposValidosParaContinuidad.includes(contextoUsuario?.tipo) &&
            (contextoUsuario?.id || contextoUsuario?.nombre);

          if (!tieneContextoValido) {
            const resultadoTiendaFallback = await procesarBusquedaTienda({
              mensaje: mensajeFinal,
              contexto_previo: contextoUsuario,
              localidad: "barranca",
              excluir_id: null,
              nombre_usuario: nombre_user,
            });

            const contextoFallback = {
              ...limpiarCamposPromoDelContexto(contextoUsuario),
              tipo: "NEGOCIO",
              categoria: resultadoTiendaFallback.cat_detectada || null,
              subcategoria: resultadoTiendaFallback.subcategoria || null,
              id: resultadoTiendaFallback.id || null,
              nombre: resultadoTiendaFallback.nombre_negocio || null,
              extra: resultadoTiendaFallback.data || "null",
            };
            const promesaContextoFallback = actualizarContextoUsuario(
              numero_usuario,
              contextoFallback,
            );

            if (resultadoTiendaFallback.mensaje_safe) {
              await enviarMensajeWhatsapp(numero_usuario, resultadoTiendaFallback.mensaje_safe);
            }

            await promesaContextoFallback;

            const tiempo_ms_fallback = Date.now() - inicio;
            return res.status(200).json({
              ok: true,
              categoria: "NEGOCIO",
              subcaso: "fallback_desde_continuidad_invalida",
              mensaje_usuario: mensajeFinal,
              nombre_usuario: nombre_user,
              numero_usuario,
              contexto_usuario: contextoFallback,
              resultado_negocio: resultadoTiendaFallback,
              tokens_usados: {
                clasificador: { modelo: "gpt-5.4-mini", ...tokensClasificador },
                negocio: resultadoTiendaFallback.tokens_usados,
              },
              tiempo_ms: tiempo_ms_fallback,
            });
          }

          if (contextoUsuario.tipo === "TURISMO") {
            const contextoConContinuidadTurismo = prepararContextoContinuidad(contextoUsuario);

            const resultadoContinuidadTurismo = await resolverInfoTurismo({
              id: contextoConContinuidadTurismo.id,
              nombre: contextoConContinuidadTurismo.nombre,
              mensaje: mensajeFinal,
              localidad: "barranca",
              nombre_usuario: nombre_user,
            });

            const contextoActualizadoTurismo = {
              ...limpiarCamposPromoDelContexto(contextoConContinuidadTurismo),
              tipo: "TURISMO",
              categoria: contextoConContinuidadTurismo.categoria || null,
              id: resultadoContinuidadTurismo.id || contextoConContinuidadTurismo.id || null,
              nombre:
                resultadoContinuidadTurismo.nombre_lugar ||
                contextoConContinuidadTurismo.nombre ||
                null,
              extra: resultadoContinuidadTurismo.data || "null",
            };
            const promesaContextoTurismo = actualizarContextoUsuario(
              numero_usuario,
              contextoActualizadoTurismo,
            );

            if (resultadoContinuidadTurismo.imagen) {
              try {
                await enviarImagenWhatsapp(
                  numero_usuario,
                  resultadoContinuidadTurismo.imagen,
                  resultadoContinuidadTurismo.mensaje_safe,
                );
              } catch (e) {
                console.error("❌ [CONTINUIDAD_INFO turismo] Falló imagen, texto de respaldo:", e.message);
                if (resultadoContinuidadTurismo.mensaje_safe) {
                  await enviarMensajeWhatsapp(numero_usuario, resultadoContinuidadTurismo.mensaje_safe);
                }
              }
            } else if (resultadoContinuidadTurismo.mensaje_safe) {
              await enviarMensajeWhatsapp(numero_usuario, resultadoContinuidadTurismo.mensaje_safe);
            }

            await promesaContextoTurismo;

            const tiempo_ms_turismo = Date.now() - inicio;
            return res.status(200).json({
              ok: true,
              categoria: "CONTINUIDAD_INFO",
              subcaso: "turismo",
              mensaje_usuario: mensajeFinal,
              nombre_usuario: nombre_user,
              numero_usuario,
              contexto_usuario: contextoActualizadoTurismo,
              resultado_continuidad: resultadoContinuidadTurismo,
              tokens_usados: {
                clasificador: { modelo: "gpt-5.4-mini", ...tokensClasificador },
                continuidad: resultadoContinuidadTurismo.tokens_usados,
              },
              tiempo_ms: tiempo_ms_turismo,
            });
          }

          const contextoConContinuidad = prepararContextoContinuidad(contextoUsuario);

          const resultadoContinuidad = await resolverInfoNegocio({
            id: contextoConContinuidad.id,
            nombre: contextoConContinuidad.nombre,
            mensaje: mensajeFinal,
            localidad: "barranca",
            nombre_usuario: nombre_user,
          });

          const contextoActualizadoContinuidad = {
            ...limpiarCamposPromoDelContexto(contextoConContinuidad),
            tipo: "NEGOCIO",
            categoria:
              resultadoContinuidad.cat_detectada || contextoConContinuidad.categoria || null,
            subcategoria: contextoConContinuidad.subcategoria || null,
            id: resultadoContinuidad.id || contextoConContinuidad.id || null,
            nombre: resultadoContinuidad.nombre_negocio || contextoConContinuidad.nombre || null,
            extra: resultadoContinuidad.data || "null",
          };
          const promesaContexto = actualizarContextoUsuario(
            numero_usuario,
            contextoActualizadoContinuidad,
          );

          if (resultadoContinuidad.mensaje_safe) {
            await enviarMensajeWhatsapp(numero_usuario, resultadoContinuidad.mensaje_safe);
          }

          await promesaContexto;

          const tiempo_ms = Date.now() - inicio;
          return res.status(200).json({
            ok: true,
            categoria: "CONTINUIDAD_INFO",
            mensaje_usuario: mensajeFinal,
            nombre_usuario: nombre_user,
            numero_usuario,
            contexto_usuario: contextoActualizadoContinuidad,
            resultado_continuidad: resultadoContinuidad,
            tokens_usados: {
              clasificador: { modelo: "gpt-5.4-mini", ...tokensClasificador },
              continuidad: resultadoContinuidad.tokens_usados,
            },
            tiempo_ms,
          });
        }

        // ---------------- PELIGRO ----------------
        if (categoria === "PELIGRO") {
          const contextoActualizadoPeligro = {
            ...limpiarCamposPromoDelContexto(contextoUsuario),
            tipo: "PELIGRO",
          };
          const promesaContexto = actualizarContextoUsuario(
            numero_usuario,
            contextoActualizadoPeligro,
          );

          try {
            await enviarPlantillaBaneoWhatsapp({
              recipientPhoneNumber: numero_usuario,
              nombre_user,
              mensajeFinal,
            });
          } catch (e) {
            console.error("❌ [PELIGRO] Falló plantilla baneo_usr:", e.message);
          }

          const mensajeModeracion = construirMensajeModeracion(nombre_user);
          try {
            await enviarMensajeWhatsapp(numero_usuario, mensajeModeracion);
          } catch (e) {
            console.error("❌ [PELIGRO] Falló mensaje de moderación:", e.message);
          }

          await promesaContexto;

          const tiempo_ms = Date.now() - inicio;
          return res.status(200).json({
            ok: true,
            categoria: "PELIGRO",
            mensaje_usuario: mensajeFinal,
            nombre_usuario: nombre_user,
            numero_usuario,
            contexto_usuario: contextoActualizadoPeligro,
            mensaje_moderacion: mensajeModeracion,
            tokens_usados: {
              clasificador: { modelo: "gpt-5.4-mini", ...tokensClasificador },
            },
            tiempo_ms,
          });
        }

        // ---------------- PROMOCIONES ----------------
        if (categoria === "PROMOCIONES") {
          const resultadoPromo = await procesarPromociones({
            mensaje: mensajeFinal,
            contexto_previo: contextoUsuario,
            nombre_usuario: nombre_user,
          });

          if (resultadoPromo.preguntar_mejor) {
            const mensajePreguntar = construirMensajePreguntarPromo(nombre_user);
            const contextoActualizadoPromo = {
              ...limpiarCamposPromoDelContexto(contextoUsuario),
              tipo: "PROMOCIONES",
              categoria: null,
              id: null,
              nombre: null,
              subcategoria: null,
              extra: "ESPERANDO_NOMBRE_PROMO: se le pidió al usuario un nombre de negocio o categoría para buscar promociones",
            };
            await actualizarContextoUsuario(numero_usuario, contextoActualizadoPromo);
            await enviarMensajeWhatsapp(numero_usuario, mensajePreguntar);

            const tiempo_ms = Date.now() - inicio;
            return res.status(200).json({
              ok: true,
              categoria: "PROMOCIONES",
              subcaso: "preguntar_mejor",
              mensaje_usuario: mensajeFinal,
              nombre_usuario: nombre_user,
              numero_usuario,
              contexto_usuario: contextoActualizadoPromo,
              tokens_usados: {
                clasificador: { modelo: "gpt-5.4-mini", ...tokensClasificador },
                promociones: resultadoPromo.tokens_usados,
              },
              tiempo_ms,
            });
          }

          if (resultadoPromo.sin_resultados) {
            const mensajeSinPromo = construirMensajeSinPromo(
              resultadoPromo.referencia,
              nombre_user,
              resultadoPromo.tipo_referencia,
            );
            const contextoActualizadoPromo = {
              ...limpiarCamposPromoDelContexto(contextoUsuario),
              tipo: "PROMOCIONES",
              categoria: null,
              id: null,
              nombre: null,
              subcategoria: null,
              extra: "pedi al usuario otro nombre o categoria para darle las promociones",
            };
            await actualizarContextoUsuario(numero_usuario, contextoActualizadoPromo);
            await enviarMensajeWhatsapp(numero_usuario, mensajeSinPromo);

            const tiempo_ms = Date.now() - inicio;
            return res.status(200).json({
              ok: true,
              categoria: "PROMOCIONES",
              subcaso: "sin_resultados",
              mensaje_usuario: mensajeFinal,
              nombre_usuario: nombre_user,
              numero_usuario,
              contexto_usuario: contextoActualizadoPromo,
              tokens_usados: {
                clasificador: { modelo: "gpt-5.4-mini", ...tokensClasificador },
                promociones: resultadoPromo.tokens_usados,
              },
              tiempo_ms,
            });
          }

          const contextoActualizadoPromo = { ...contextoUsuario, ...resultadoPromo.data };
          const promesaContexto = actualizarContextoUsuario(numero_usuario, contextoActualizadoPromo);

          const tieneImagen = !!resultadoPromo.imagen;
          const esUnaSola = (resultadoPromo.data?.ids_promos?.length || 0) < 2;

          if (tieneImagen && esUnaSola) {
            try {
              await enviarImagenWhatsapp(numero_usuario, resultadoPromo.imagen, resultadoPromo.mensaje_safe);
            } catch (e) {
              console.error("❌ [PROMOCIONES] Falló imagen:", e.message);
              if (resultadoPromo.mensaje_safe) {
                await enviarMensajeWhatsapp(numero_usuario, resultadoPromo.mensaje_safe);
              }
            }
            if (resultadoPromo.siker) {
              try {
                await enviarStickerWhatsapp(numero_usuario, resultadoPromo.siker);
              } catch (e) {
                console.error("❌ [PROMOCIONES] Falló sticker:", e.message);
              }
            }
          } else if (tieneImagen && !esUnaSola) {
            try {
              await enviarPlantillaWhatsapp_promociones({
                recipientPhoneNumber: numero_usuario,
                imagen: resultadoPromo.imagen,
                mensaje: resultadoPromo.mensaje,
                ids: resultadoPromo.data?.ids_promos || [],
              });
            } catch (e) {
              console.error("❌ [PROMOCIONES] Falló plantilla, mando texto de respaldo:", e.message);
              if (resultadoPromo.mensaje_safe) {
                await enviarMensajeWhatsapp(numero_usuario, resultadoPromo.mensaje_safe);
              }
            }
          } else {
            if (resultadoPromo.mensaje_safe) {
              try {
                await enviarMensajeWhatsapp(numero_usuario, resultadoPromo.mensaje_safe);
              } catch (e) {
                console.error("❌ [PROMOCIONES] Falló texto (sin imagen):", e.message);
              }
            }
          }

          await promesaContexto;

          const tiempo_ms = Date.now() - inicio;
          return res.status(200).json({
            ok: true,
            categoria: "PROMOCIONES",
            mensaje_usuario: mensajeFinal,
            nombre_usuario: nombre_user,
            numero_usuario,
            contexto_usuario: contextoActualizadoPromo,
            resultado_promociones: resultadoPromo,
            tokens_usados: {
              clasificador: { modelo: "gpt-5.4-mini", ...tokensClasificador },
              promociones: resultadoPromo.tokens_usados,
            },
            tiempo_ms,
          });
        }

        // ---------------- SERVICIOS_BASICOS ----------------
        if (categoria === "SERVICIOS_BASICOS") {
          const resultadoServicio = await procesarBusquedaServiciosBasicos({
            mensaje: mensajeFinal,
            contexto_previo: contextoUsuario,
            localidad: "barranca",
            nombre_usuario: nombre_user,
          });

          const contextoActualizadoServicio = {
            ...limpiarCamposPromoDelContexto(contextoUsuario),
            tipo: "SERVICIOS_BASICOS",
            categoria: null,
            id: resultadoServicio.id || null,
            nombre: resultadoServicio.nombre_servicio || null,
            extra: resultadoServicio.data || "null",
          };
          const promesaContexto = actualizarContextoUsuario(numero_usuario, contextoActualizadoServicio);

          if (resultadoServicio.imagen) {
            try {
              await enviarImagenWhatsapp(numero_usuario, resultadoServicio.imagen, resultadoServicio.mensaje_safe);
            } catch (e) {
              console.error("❌ [SERVICIOS_BASICOS] Falló imagen, texto de respaldo:", e.message);
              if (resultadoServicio.mensaje_safe) {
                try {
                  await enviarMensajeWhatsapp(numero_usuario, resultadoServicio.mensaje_safe);
                } catch (e2) {
                  console.error("❌ [SERVICIOS_BASICOS] Falló también texto de respaldo:", e2.message);
                }
              }
            }
          } else if (resultadoServicio.mensaje_safe) {
            try {
              await enviarMensajeWhatsapp(numero_usuario, resultadoServicio.mensaje_safe);
            } catch (e) {
              console.error("❌ [SERVICIOS_BASICOS] Falló texto de resultado:", e.message);
            }
          }

          await promesaContexto;

          const tiempo_ms = Date.now() - inicio;
          return res.status(200).json({
            ok: true,
            categoria: "SERVICIOS_BASICOS",
            mensaje_usuario: mensajeFinal,
            nombre_usuario: nombre_user,
            numero_usuario,
            contexto_usuario: contextoActualizadoServicio,
            resultado_servicio: resultadoServicio,
            tokens_usados: {
              clasificador: { modelo: "gpt-5.4-mini", ...tokensClasificador },
              servicios_basicos: resultadoServicio.tokens_usados,
            },
            tiempo_ms,
          });
        }

        // ---------------- Fallback ----------------
        const contextoActualizado = {
          ...limpiarCamposPromoDelContexto(contextoUsuario),
          tipo: categoria,
        };
        await actualizarContextoUsuario(numero_usuario, contextoActualizado);

        const tiempo_ms = Date.now() - inicio;
        return res.status(200).json({
          ok: true,
          categoria,
          mensaje_usuario: mensajeFinal,
          nombre_usuario: nombre_user,
          numero_usuario,
          contexto_usuario: contextoActualizado,
          tokens_usados: { clasificador: tokensClasificador },
          tiempo_ms,
        });
      } finally {
        await marcarProcesando(numero_usuario, false);
      }
    } catch (error) {
      console.error("❌ Error geinz_procesar_buffer:", error.message);
      if (numero_usuario_actual) {
        await marcarProcesando(numero_usuario_actual, false);
      }
      const tiempo_ms = Date.now() - inicio;
      return res.status(500).json({ ok: false, error: error.message, tiempo_ms });
    }
  },
);

exports.geinz_aviso_qr_escaneado = onRequest(async (req, res) => {
  try {
    await enviarAvisoInterno("Usuario nuevo escaneó el QR");
    return res.status(200).json({ ok: true });
  } catch (error) {
    console.error("❌ Error geinz_aviso_qr_escaneado:", error.message);
    return res.status(500).json({ ok: false, error: error.message });
  }
});

// ============================================================================
// ============================================================================
// SECCIÓN 2 — TELEGRAM
// ============================================================================
// ============================================================================

const TELEGRAM_TOKEN = process.env.TELEGRAM_BOT_TOKEN;
const TG_API = `https://api.telegram.org/bot${TELEGRAM_TOKEN}`;
const TG_FILE_API = `https://api.telegram.org/file/bot${TELEGRAM_TOKEN}`;

// ---- Descarga/transcripción de audio de Telegram (input, no envío) ----
async function obtenerUrlArchivoTelegram(fileId) {
  const resp = await fetch(`${TG_API}/getFile?file_id=${fileId}`);
  const data = await resp.json();
  if (!data.ok) throw new Error(`Telegram getFile error: ${JSON.stringify(data)}`);
  return `${TG_FILE_API}/${data.result.file_path}`;
}

async function descargarBinarioTelegram(url) {
  const resp = await fetch(url);
  if (!resp.ok) throw new Error(`Error descargando archivo: ${resp.status}`);
  return Buffer.from(await resp.arrayBuffer());
}

async function transcribirAudioTelegram(bufferAudio, nombreArchivo = "audio.ogg") {
  const file = new File([bufferAudio], nombreArchivo, { type: "audio/ogg" });
  const transcription = await openai.audio.transcriptions.create({
    file,
    model: "whisper-1",
    response_format: "verbose_json",
  });
  return { texto: transcription.text || "" };
}

async function procesarAudioTelegram({ fileId }) {
  const fileUrl = await obtenerUrlArchivoTelegram(fileId);
  const buffer = await descargarBinarioTelegram(fileUrl);
  const resultado = await transcribirAudioTelegram(buffer);
  return resultado.texto;
}

// ---- Usuario / contexto (Telegram) ----
async function obtenerOCrearUsuarioTelegram(chatId, nombreTg) {
  const numero_usuario = `tg_${chatId}`;
  const ref = db
    .collection("Trabajadores_Usuarios_Drivers")
    .doc("usuario_bot_geinz")
    .collection("usuario_bot_geinz")
    .doc(numero_usuario);

  const snap = await ref.get();
  if (!snap.exists) {
    const nuevo = {
      nombre_user: nombreTg || "Usuario",
      numero_user: numero_usuario,
      plataforma: "telegram",
      chat_id: chatId,
      status: "activo",
      contexto: CONTEXTO_DEFAULT,
      createdAt: admin.firestore.FieldValue.serverTimestamp(),
      updated_at: admin.firestore.FieldValue.serverTimestamp(),
    };
    await ref.set(nuevo);
    return { numero_usuario, ...nuevo };
  }
  return { numero_usuario, ...snap.data() };
}

async function actualizarContextoUsuarioTelegram(numero_usuario, nuevoContexto) {
  const ref = db
    .collection("Trabajadores_Usuarios_Drivers")
    .doc("usuario_bot_geinz")
    .collection("usuario_bot_geinz")
    .doc(numero_usuario);
  await ref.update({
    contexto: nuevoContexto,
    updated_at: admin.firestore.FieldValue.serverTimestamp(),
  });
}

const BASE_URL_PERFIL_TIENDA = "https://geinztech.com/";
const BASE_URL_CONTACTO_TIENDA = "https://geinztech.com/";
const BASE_URL_PROMOCIONES = "https://geinztech.com/";

// ============================================================
// geinz_webhook_telegram — entrada + IA de Telegram
// ============================================================
exports.geinz_webhook_telegram = onRequest(
  { concurrency: 20, cpu: 1 },
  async (req, res) => {
    if (req.method === "GET") {
      return res.status(200).send("ok");
    }

    const inicio = Date.now();
    try {
      const update = req.body;
      const mensaje = update?.message;

      if (!mensaje) {
        return res.status(200).json({ ok: true, info: "Update sin mensaje procesable" });
      }

      const chatId = mensaje.chat.id;
      const nombreTg = mensaje.from?.first_name || "Usuario";

      let mensajeFinal = "";

      if (mensaje.voice) {
        mensajeFinal = await procesarAudioTelegram({ fileId: mensaje.voice.file_id });
      } else if (mensaje.text) {
        mensajeFinal = mensaje.text;
      } else if (mensaje.sticker) {
        await enviarMensajeTelegram(
          chatId,
          "😂 Buen sticker, aún no puedo verlos pero cuéntame qué necesitas 🙌",
        );
        return res.status(200).json({ ok: true, info: "sticker" });
      } else if (mensaje.photo) {
        await enviarMensajeTelegram(
          chatId,
          "📸 Recibí tu imagen, aún no puedo analizarla, pero dime qué necesitas 😊",
        );
        return res.status(200).json({ ok: true, info: "imagen" });
      } else {
        return res.status(200).json({
          ok: true,
          info: `Tipo no soportado: ${Object.keys(mensaje)}`,
        });
      }

      if (!mensajeFinal.trim()) {
        return res.status(200).json({ ok: true, info: "Mensaje vacío" });
      }

      guardarMensajeHistorial({
        canal: "telegram",
        numero_usuario: `tg_${chatId}`,
        nombre_usuario: nombreTg,
        remitente: "usuario",
        tipo: mensaje.voice ? "audio" : "texto",
        contenido: mensajeFinal,
        mensaje_id: mensaje.message_id,
      }).catch(() => {});

      const usuarioInfo = await obtenerOCrearUsuarioTelegram(chatId, nombreTg);
      const contextoUsuario = usuarioInfo.contexto || CONTEXTO_DEFAULT;

      const { categoria } = await clasificarIntencion(mensajeFinal, contextoUsuario);

      // ---------------- NEGOCIO ----------------
      if (categoria === "NEGOCIO") {
        const resultadoTienda = await procesarBusquedaTienda({
          mensaje: mensajeFinal,
          contexto_previo: contextoUsuario,
          localidad: "barranca",
          excluir_id: contextoUsuario?.id || null,
          nombre_usuario: nombreTg,
        });

        const contextoActualizado = {
          tipo: "NEGOCIO",
          categoria: resultadoTienda.cat_detectada || null,
          id: resultadoTienda.id || null,
          nombre: resultadoTienda.nombre_negocio || null,
          extra: resultadoTienda.data || "null",
        };
        await actualizarContextoUsuarioTelegram(usuarioInfo.numero_usuario, contextoActualizado);

        if (resultadoTienda.plantilla === true) {
          const botones = [
            {
              text: resultadoTienda.alias_tienda || "Ver perfil",
              url: `${BASE_URL_PERFIL_TIENDA}${resultadoTienda.alias_tienda}`,
            },
            {
              text: "💬 Contactar",
              url: `${BASE_URL_CONTACTO_TIENDA}?id_tienda=${resultadoTienda.id}&contacto=${resultadoTienda.token_wsap}`,
            },
          ];
          const replyMarkup = { inline_keyboard: [botones] };

          try {
            if (resultadoTienda.imagen) {
              await enviarImagenTelegram(
                chatId,
                resultadoTienda.imagen,
                resultadoTienda.mensaje_safe,
                replyMarkup,
              );
            } else {
              await enviarMensajeTelegram(chatId, resultadoTienda.mensaje_safe, { replyMarkup });
            }
          } catch (e) {
            console.error("❌ [NEGOCIO Telegram] Falló botones, texto de respaldo:", e.message);
            if (resultadoTienda.mensaje_safe) {
              await enviarMensajeTelegram(chatId, resultadoTienda.mensaje_safe);
            }
          }
        } else if (resultadoTienda.imagen) {
          try {
            await enviarImagenTelegram(chatId, resultadoTienda.imagen, resultadoTienda.mensaje_safe);
          } catch (e) {
            console.error("❌ [NEGOCIO Telegram] Falló imagen, texto de respaldo:", e.message);
            if (resultadoTienda.mensaje_safe) {
              await enviarMensajeTelegram(chatId, resultadoTienda.mensaje_safe);
            }
          }
        } else if (resultadoTienda.mensaje_safe) {
          await enviarMensajeTelegram(chatId, resultadoTienda.mensaje_safe);
        }

        return res.status(200).json({ ok: true, categoria: "NEGOCIO", tiempo_ms: Date.now() - inicio });
      }

      // ---------------- GEINZ ----------------
      if (categoria === "GEINZ") {
        const { resultado: respuestaGeinz } = await llamarGeminiGeinz(mensajeFinal, nombreTg);
        await actualizarContextoUsuarioTelegram(usuarioInfo.numero_usuario, {
          tipo: "GEINZ",
          extra: respuestaGeinz.extra || "null",
        });

        if (respuestaGeinz.mensaje) {
          const debeIntentarAudio =
            !!mensaje.voice &&
            !contieneNumeroOLink(respuestaGeinz.mensaje) &&
            Math.random() < PROBABILIDAD_AUDIO;

          let audioEnviado = false;
          if (debeIntentarAudio) {
            audioEnviado = await intentarResponderConAudioTelegram({
              chatId,
              texto: respuestaGeinz.mensaje,
            });
          }

          if (!audioEnviado) {
            await enviarMensajeTelegram(chatId, respuestaGeinz.mensaje);
          }
        }

        return res.status(200).json({ ok: true, categoria: "GEINZ", tiempo_ms: Date.now() - inicio });
      }

      // ---------------- TURISMO ----------------
      if (categoria === "TURISMO") {
        const resultadoTurismo = await procesarBusquedaTurismo({
          mensaje: mensajeFinal,
          contexto_previo: contextoUsuario,
          localidad: "barranca",
          usuario: nombreTg,
        });

        const contextoActualizado = {
          tipo: "TURISMO",
          categoria: resultadoTurismo.categoria || null,
          id: resultadoTurismo.id || null,
          nombre: resultadoTurismo.nombre || null,
          extra: resultadoTurismo.data || "null",
        };
        await actualizarContextoUsuarioTelegram(usuarioInfo.numero_usuario, contextoActualizado);

        if (resultadoTurismo.imagen) {
          try {
            await enviarImagenTelegram(chatId, resultadoTurismo.imagen, resultadoTurismo.mensaje_safe);
          } catch (e) {
            console.error("❌ [TURISMO] Falló imagen, texto de respaldo:", e.message);
            if (resultadoTurismo.mensaje_safe)
              await enviarMensajeTelegram(chatId, resultadoTurismo.mensaje_safe);
          }
        } else if (resultadoTurismo.mensaje_safe) {
          await enviarMensajeTelegram(chatId, resultadoTurismo.mensaje_safe);
        }

        return res.status(200).json({ ok: true, categoria: "TURISMO", tiempo_ms: Date.now() - inicio });
      }

      // ---------------- SERVICIOS_BASICOS ----------------
      if (categoria === "SERVICIOS_BASICOS") {
        const resultadoServicio = await procesarBusquedaServiciosBasicos({
          mensaje: mensajeFinal,
          contexto_previo: contextoUsuario,
          localidad: "barranca",
          nombre_usuario: nombreTg,
        });

        const contextoActualizado = {
          tipo: "SERVICIOS_BASICOS",
          categoria: null,
          id: resultadoServicio.id || null,
          nombre: resultadoServicio.nombre_servicio || null,
          extra: resultadoServicio.data || "null",
        };
        await actualizarContextoUsuarioTelegram(usuarioInfo.numero_usuario, contextoActualizado);

        if (resultadoServicio.imagen) {
          try {
            await enviarImagenTelegram(chatId, resultadoServicio.imagen, resultadoServicio.mensaje_safe);
          } catch (e) {
            console.error("❌ [SERVICIOS_BASICOS] Falló imagen, texto de respaldo:", e.message);
            if (resultadoServicio.mensaje_safe)
              await enviarMensajeTelegram(chatId, resultadoServicio.mensaje_safe);
          }
        } else if (resultadoServicio.mensaje_safe) {
          await enviarMensajeTelegram(chatId, resultadoServicio.mensaje_safe);
        }

        return res.status(200).json({ ok: true, categoria: "SERVICIOS_BASICOS", tiempo_ms: Date.now() - inicio });
      }

      // ---------------- PROMOCIONES ----------------
      if (categoria === "PROMOCIONES") {
        const resultadoPromo = await procesarPromociones({
          mensaje: mensajeFinal,
          contexto_previo: contextoUsuario,
          nombre_usuario: nombreTg,
        });

        if (resultadoPromo.preguntar_mejor) {
          const contextoActualizado = {
            tipo: "PROMOCIONES",
            categoria: null,
            id: null,
            nombre: null,
            extra: "ESPERANDO_NOMBRE_PROMO: se le pidió al usuario un nombre de negocio o categoría para buscar promociones",
          };
          await actualizarContextoUsuarioTelegram(usuarioInfo.numero_usuario, contextoActualizado);
          await enviarMensajeTelegram(
            chatId,
            `${nombreTg}, cuéntame qué tienda o categoría te interesa y te busco las promos 🛍️`,
          );
          return res.status(200).json({
            ok: true,
            categoria: "PROMOCIONES",
            subcaso: "preguntar_mejor",
            tiempo_ms: Date.now() - inicio,
          });
        }

        if (resultadoPromo.sin_resultados) {
          const contextoActualizado = {
            tipo: "PROMOCIONES",
            categoria: null,
            id: null,
            nombre: null,
            extra: "pedi al usuario otro nombre o categoria para darle las promociones",
          };
          await actualizarContextoUsuarioTelegram(usuarioInfo.numero_usuario, contextoActualizado);
          await enviarMensajeTelegram(
            chatId,
            `No encontré promociones de ${resultadoPromo.referencia} 😅 ¿tienes otra tienda o categoría en mente?`,
          );
          return res.status(200).json({
            ok: true,
            categoria: "PROMOCIONES",
            subcaso: "sin_resultados",
            tiempo_ms: Date.now() - inicio,
          });
        }

        const contextoActualizado = { ...contextoUsuario, ...resultadoPromo.data };
        await actualizarContextoUsuarioTelegram(usuarioInfo.numero_usuario, contextoActualizado);

        const tieneImagen = !!resultadoPromo.imagen;
        const esUnaSola = (resultadoPromo.data?.ids_promos?.length || 0) < 2;

        if (tieneImagen && !esUnaSola) {
          const ids = resultadoPromo.data?.ids_promos || [];
          const replyMarkup = {
            inline_keyboard: [
              [
                {
                  text: "🔥 Ver promociones",
                  url: `${BASE_URL_PROMOCIONES}api/share?t=pmspls&l=ba&p=${ids[0] || ""},${ids[1] || ""}`,
                },
              ],
            ],
          };
          try {
            await enviarImagenTelegram(
              chatId,
              resultadoPromo.imagen,
              resultadoPromo.mensaje || resultadoPromo.mensaje_safe,
              replyMarkup,
            );
          } catch (e) {
            console.error("❌ [PROMOCIONES Telegram] Falló botón, texto de respaldo:", e.message);
            if (resultadoPromo.mensaje_safe) {
              await enviarMensajeTelegram(chatId, resultadoPromo.mensaje_safe);
            }
          }
        } else if (tieneImagen && esUnaSola) {
          try {
            await enviarImagenTelegram(chatId, resultadoPromo.imagen, resultadoPromo.mensaje_safe);
          } catch (e) {
            console.error("❌ [PROMOCIONES Telegram] Falló imagen:", e.message);
            if (resultadoPromo.mensaje_safe) {
              await enviarMensajeTelegram(chatId, resultadoPromo.mensaje_safe);
            }
          }
        } else if (resultadoPromo.mensaje_safe) {
          await enviarMensajeTelegram(chatId, resultadoPromo.mensaje_safe);
        }

        return res.status(200).json({ ok: true, categoria: "PROMOCIONES", tiempo_ms: Date.now() - inicio });
      }

      // ---------------- CONTINUIDAD_INFO ----------------
      if (categoria === "CONTINUIDAD_INFO") {
        const tiposValidos = ["NEGOCIO", "PROMOCIONES", "TURISMO"];
        const tieneContextoValido =
          tiposValidos.includes(contextoUsuario?.tipo) &&
          (contextoUsuario?.id || contextoUsuario?.nombre);

        if (!tieneContextoValido) {
          const resultadoFallback = await procesarBusquedaTienda({
            mensaje: mensajeFinal,
            contexto_previo: contextoUsuario,
            localidad: "barranca",
            excluir_id: null,
            nombre_usuario: nombreTg,
          });

          const contextoActualizado = {
            tipo: "NEGOCIO",
            categoria: resultadoFallback.cat_detectada || null,
            id: resultadoFallback.id || null,
            nombre: resultadoFallback.nombre_negocio || null,
            extra: resultadoFallback.data || "null",
          };
          await actualizarContextoUsuarioTelegram(usuarioInfo.numero_usuario, contextoActualizado);

          if (resultadoFallback.mensaje_safe) {
            await enviarMensajeTelegram(chatId, resultadoFallback.mensaje_safe);
          }

          return res.status(200).json({
            ok: true,
            categoria: "NEGOCIO",
            subcaso: "fallback_desde_continuidad_invalida",
            tiempo_ms: Date.now() - inicio,
          });
        }

        if (contextoUsuario.tipo === "TURISMO") {
          const resultadoContinuidad = await resolverInfoTurismo({
            id: contextoUsuario.id,
            nombre: contextoUsuario.nombre,
            mensaje: mensajeFinal,
            localidad: "barranca",
            nombre_usuario: nombreTg,
          });

          const contextoActualizado = {
            tipo: "TURISMO",
            categoria: contextoUsuario.categoria || null,
            id: resultadoContinuidad.id || contextoUsuario.id || null,
            nombre: resultadoContinuidad.nombre_lugar || contextoUsuario.nombre || null,
            extra: resultadoContinuidad.data || "null",
          };
          await actualizarContextoUsuarioTelegram(usuarioInfo.numero_usuario, contextoActualizado);

          if (resultadoContinuidad.imagen) {
            try {
              await enviarImagenTelegram(chatId, resultadoContinuidad.imagen, resultadoContinuidad.mensaje_safe);
            } catch (e) {
              console.error("❌ [CONTINUIDAD turismo] Falló imagen, texto de respaldo:", e.message);
              if (resultadoContinuidad.mensaje_safe)
                await enviarMensajeTelegram(chatId, resultadoContinuidad.mensaje_safe);
            }
          } else if (resultadoContinuidad.mensaje_safe) {
            await enviarMensajeTelegram(chatId, resultadoContinuidad.mensaje_safe);
          }

          return res.status(200).json({
            ok: true,
            categoria: "CONTINUIDAD_INFO",
            subcaso: "turismo",
            tiempo_ms: Date.now() - inicio,
          });
        }

        const resultadoContinuidad = await resolverInfoNegocio({
          id: contextoUsuario.id,
          nombre: contextoUsuario.nombre,
          mensaje: mensajeFinal,
          localidad: "barranca",
          nombre_usuario: nombreTg,
        });

        const contextoActualizado = {
          tipo: "NEGOCIO",
          categoria: resultadoContinuidad.cat_detectada || contextoUsuario.categoria || null,
          id: resultadoContinuidad.id || contextoUsuario.id || null,
          nombre: resultadoContinuidad.nombre_negocio || contextoUsuario.nombre || null,
          extra: resultadoContinuidad.data || "null",
        };
        await actualizarContextoUsuarioTelegram(usuarioInfo.numero_usuario, contextoActualizado);

        if (resultadoContinuidad.mensaje_safe) {
          await enviarMensajeTelegram(chatId, resultadoContinuidad.mensaje_safe);
        }

        return res.status(200).json({ ok: true, categoria: "CONTINUIDAD_INFO", tiempo_ms: Date.now() - inicio });
      }

      // ---------------- EMERGENCIA ----------------
      if (categoria === "EMERGENCIA") {
        await actualizarContextoUsuarioTelegram(usuarioInfo.numero_usuario, { tipo: "EMERGENCIA" });

        // 👇 Igual que en WhatsApp: procesarEmergencia solo calcula, y
        // el ÚNICO que dispara el envío real es este dispensador.
        const resultadoEmergencia = await procesarEmergencia({
          localidad: "barranca",
          mensaje: mensajeFinal,
          nombreUsuario: nombreTg,
        });

        if (!resultadoEmergencia.error) {
          await enviarRespuestaEmergencia(`tg_${chatId}`, resultadoEmergencia, "telegram");
        }

        return res.status(200).json({ ok: true, categoria: "EMERGENCIA", tiempo_ms: Date.now() - inicio });
      }

      // ---------------- PELIGRO ----------------
      if (categoria === "PELIGRO") {
        await actualizarContextoUsuarioTelegram(usuarioInfo.numero_usuario, { tipo: "PELIGRO" });

        await enviarMensajeTelegram(
          chatId,
          `⚠️ ${nombreTg}, tu mensaje ha sido detectado como contenido que incumple las normas de uso de Geinz. Te pedimos mantener una comunicación respetuosa.`,
        );

        return res.status(200).json({ ok: true, categoria: "PELIGRO", tiempo_ms: Date.now() - inicio });
      }

      // ---------------- Fallback ----------------
      await actualizarContextoUsuarioTelegram(usuarioInfo.numero_usuario, { tipo: categoria });
      await enviarMensajeTelegram(
        chatId,
        `Perdón ${nombreTg}, no entendí bien eso 😅 ¿me lo puedes explicar de otra forma?`,
      );

      return res.status(200).json({ ok: true, categoria, tiempo_ms: Date.now() - inicio });
    } catch (error) {
      console.error("❌ Error geinz_webhook_telegram:", error.message);
      return res.status(500).json({ ok: false, error: error.message });
    }
  },
);