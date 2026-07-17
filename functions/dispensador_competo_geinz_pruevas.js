const { onRequest } = require("firebase-functions/v2/https");
const OpenAI = require("openai");
const admin = require("firebase-admin");

if (!admin.apps.length) {
  admin.initializeApp();
}

const db = admin.firestore();

// ============================================================
// CONFIG
// ============================================================
const WHATSAPP_TOKEN = process.env.ID_API_WHATSAPP; // Bearer token (empieza con EAA...)
const WHATSAPP_PHONE_NUMBER_ID = process.env.ID_NUMBER_WHATSAPP; // el ID numérico
const WHATSAPP_API_VERSION = "v20.0";

const openai = new OpenAI({ apiKey: process.env.API_KEYO_OPEN_IA });

const PRECIO_WHISPER_USD_POR_MINUTO = 0.006;
const TIPO_CAMBIO_USD_PEN = 3.75;

const MAX_TOKENS_RATE = 10;
const MS_POR_TOKEN = 2000;
const DURACION_BLOQUEO = 15000;

const CONTEXTO_DEFAULT = {
  tipo: "GEINZ",
  categoria: null,
  extra: "null",
  id: null,
  nombre: null,
};
const {
  procesarEmergencia,
  procesarBusquedaTienda,
  llamarGeminiGeinz,
  procesarBusquedaTurismo,
  resolverInfoNegocio,
  procesarPromociones,
} = require("./asistentes_AI_geinz.js");

const { descontarCreditosTienda } = require("./test_db2.js");

const { programarTareaDebounce } = require("./tasks.js");

// ============================================================
// ¿Hay actividad reciente del usuario? (buffer pendiente, bot
// procesando una respuesta, o audio/texto recién llegado)
// ============================================================
async function marcarActividadReciente(numero_usuario) {
  const ref = db.collection("buffer_mensajes_geinz").doc(numero_usuario);
  await ref.set({ ultima_actividad: Date.now() }, { merge: true });
}

// 👇 NUEVO: marca/desmarca que el bot está activamente generando
//    una respuesta (llamando IA, buscando tienda/turismo, etc).
//    Se usa con try/finally para garantizar que SIEMPRE se limpie,
//    incluso si algo falla a mitad de camino.
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

  // 👇 El bot está en este momento llamando a la IA / armando la
  //    respuesta de un mensaje anterior — la ventana sigue abierta
  //    aunque el buffer de mensajes ya se haya vaciado.
  //    Con expiración de seguridad: si "procesando" lleva más de
  //    PROCESANDO_MAX_MS sin limpiarse (ej. la función murió sin
  //    llegar al finally, timeout duro de Cloud Run, etc), se trata
  //    como vencido y NO bloquea al usuario para siempre.
  const PROCESANDO_MAX_MS = 60000;
  if (data.procesando === true) {
    const desde = data.procesando_desde || 0;
    if (ahora - desde < PROCESANDO_MAX_MS) return true;
    console.warn(
      "⚠️ [hayActividadReciente] Flag 'procesando' vencido (>60s), se ignora | 👤:",
      numero_usuario,
    );
  }

  // Hay mensajes bufferizados esperando el debounce
  if (Array.isArray(data.mensajes) && data.mensajes.length > 0) return true;

  // Hubo actividad reciente (ej. un audio que aún se está transcribiendo)
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

  // 👇 Si este mensaje vino por audio, marcamos el buffer completo como
  //    "origen_audio". Se queda en true aunque después lleguen más
  //    mensajes de texto dentro del mismo debounce, porque el usuario
  //    sigue "en modo audio" para efectos de la respuesta.
  if (porAudio) {
    payload.origen_audio = true;
  }

  await ref.set(payload, { merge: true });
}
// ============================================================
// UTILIDADES
// ============================================================
function pick(arr) {
  return arr[Math.floor(Math.random() * arr.length)];
}

function detectarTipoMensajeNoSoportado(mensajeWa) {
  if (!mensajeWa) return null;

  // 0. POLL WHATSAP → WhatsApp manda las encuestas como type "unsupported"
  if (mensajeWa.type === "unsupported") return "pool";

  // 1. CONTACTO
  if (mensajeWa.type === "contacts") return "contacto";

  // 2. LOCATION
  if (mensajeWa.location) return "location";

  // 3. STIKER
  if (mensajeWa.type === "sticker") return "sticker";

  // 4. VIDEO
  if (mensajeWa.type === "video") return "video";

  // 5. IMAGEN
  if (mensajeWa.type === "image") return "imagen";

  // 6. DOCUMENTO
  if (mensajeWa.document) return "documento";

  // 7. CONTIENE_URL — solo aplica si hay texto con un link adentro
  if (mensajeWa.text?.body && /https?:\/\/[^\s]+/.test(mensajeWa.text.body)) {
    return "url";
  }

  // 8. AUDIO_TEXTO → null = sigue el flujo normal del bot (texto o audio)
  return null;
}

// ============================================================
// MENSAJES ENLATADOS para tipos no soportados (antes "Code in JavaScript")
// ============================================================
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

  // fallback — cualquier otro caso no contemplado
  return "Gracias por tu mensaje 🙌. Aún no puedo procesar ese tipo de contenido, pero si me explicas qué necesitas, con gusto te ayudo 😊";
}


// ============================================================
// PASO 1 — VALIDAR / RATE LIMIT USUARIO
// ============================================================
async function validarUsuario({ numero_usuario, id_user }) {
  const ahora = Date.now();

  const ref = db
    .collection("Trabajadores_Usuarios_Drivers")
    .doc("usuario_bot_geinz")
    .collection("usuario_bot_geinz")
    .doc(numero_usuario);

  return db.runTransaction(async (tx) => {
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
}

// ============================================================
// Actualizar el contexto del usuario en Firestore tras clasificar
// ============================================================
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

// ============================================================
// PASO 2 — PROCESAMIENTO DE AUDIO (Whisper)
// ============================================================
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

async function enviarMensajeWhatsapp(recipientPhoneNumber, textBody) {
  const url = `https://graph.facebook.com/${WHATSAPP_API_VERSION}/${WHATSAPP_PHONE_NUMBER_ID}/messages`;
  const resp = await fetch(url, {
    method: "POST",
    headers: {
      Authorization: `Bearer ${WHATSAPP_TOKEN}`,
      "Content-Type": "application/json",
    },
    body: JSON.stringify({
      messaging_product: "whatsapp",
      to: recipientPhoneNumber,
      type: "text",
      text: { body: textBody },
    }),
  });
  if (!resp.ok)
    throw new Error(
      `Error enviando mensaje WhatsApp: ${resp.status} ${await resp.text()}`,
    );
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
  const url = `https://graph.facebook.com/${WHATSAPP_API_VERSION}/${WHATSAPP_PHONE_NUMBER_ID}/messages`;

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
          parameters: [
            {
              type: "image",
              image: { link: imagen },
            },
          ],
        },
        {
          type: "body",
          parameters: [
            {
              type: "text",
              text: mensaje_safe,
            },
          ],
        },
        {
          type: "button",
          sub_type: "url",
          index: "0",
          parameters: [
            {
              type: "text",
              text: alias_tienda,
            },
          ],
        },
        {
          type: "button",
          sub_type: "url",
          index: "1",
          parameters: [
            {
              type: "text",
              text: `?id_tienda=${id}&contacto=${token_wsap}`,
            },
          ],
        },
      ],
    },
  };

  const resp = await fetch(url, {
    method: "POST",
    headers: {
      Authorization: `Bearer ${WHATSAPP_TOKEN}`,
      "Content-Type": "application/json",
    },
    body: JSON.stringify(body),
  });

  if (!resp.ok) {
    throw new Error(
      `Error enviando plantilla WhatsApp: ${resp.status} ${await resp.text()}`,
    );
  }

  const resultadoEnvio = await resp.json();

  try {
    const resultadoDescuento = await descontarCreditosTienda({
      id,
      token_id: token_wsap,
      tipo: "plantilla",
    });
    console.log(
      "💳 [enviarPlantillaWhatsapp_para_tiendas] Créditos descontados:",
      resultadoDescuento,
    );
  } catch (e) {
    console.error(
      "❌ [enviarPlantillaWhatsapp_para_tiendas] Falló el descuento de créditos:",
      e.message,
    );
  }

  return resultadoEnvio;
}

async function enviarImagenWhatsapp(recipientPhoneNumber, imagenUrl, caption) {
  const url = `https://graph.facebook.com/${WHATSAPP_API_VERSION}/${WHATSAPP_PHONE_NUMBER_ID}/messages`;
  const resp = await fetch(url, {
    method: "POST",
    headers: {
      Authorization: `Bearer ${WHATSAPP_TOKEN}`,
      "Content-Type": "application/json",
    },
    body: JSON.stringify({
      messaging_product: "whatsapp",
      to: recipientPhoneNumber,
      type: "image",
      image: {
        link: imagenUrl,
        caption: caption || "",
      },
    }),
  });
  if (!resp.ok)
    throw new Error(
      `Error enviando imagen WhatsApp: ${resp.status} ${await resp.text()}`,
    );
  return resp.json();
}

async function enviarStickerWhatsapp(recipientPhoneNumber, stickerUrl) {
  const url = `https://graph.facebook.com/${WHATSAPP_API_VERSION}/${WHATSAPP_PHONE_NUMBER_ID}/messages`;
  const resp = await fetch(url, {
    method: "POST",
    headers: {
      Authorization: `Bearer ${WHATSAPP_TOKEN}`,
      "Content-Type": "application/json",
    },
    body: JSON.stringify({
      messaging_product: "whatsapp",
      to: recipientPhoneNumber,
      type: "sticker",
      sticker: {
        link: stickerUrl,
      },
    }),
  });
  if (!resp.ok)
    throw new Error(
      `Error enviando sticker WhatsApp: ${resp.status} ${await resp.text()}`,
    );
  return resp.json();
}

async function obtenerUrlMedia(mediaId) {
  const url = `https://graph.facebook.com/${WHATSAPP_API_VERSION}/${mediaId}`;
  const resp = await fetch(url, {
    headers: { Authorization: `Bearer ${WHATSAPP_TOKEN}` },
  });
  if (!resp.ok)
    throw new Error(
      `Error obteniendo URL de media: ${resp.status} ${await resp.text()}`,
    );
  const data = await resp.json();
  return data.url;
}

async function descargarAudioBinario(mediaUrl) {
  const resp = await fetch(mediaUrl, {
    headers: { Authorization: `Bearer ${WHATSAPP_TOKEN}` },
  });
  if (!resp.ok)
    throw new Error(
      `Error descargando audio: ${resp.status} ${await resp.text()}`,
    );
  return Buffer.from(await resp.arrayBuffer());
}

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

async function procesarAudioWhatsapp({
  mediaId,
  recipientPhoneNumber,
  nombreUsuario,
}) {
  const mensajeEscucha = construirMensajeEscucha(nombreUsuario);
  const [, mediaUrl] = await Promise.all([
    enviarMensajeWhatsapp(recipientPhoneNumber, mensajeEscucha),
    obtenerUrlMedia(mediaId),
  ]);
  const audioBuffer = await descargarAudioBinario(mediaUrl);
  const resultado = await transcribirAudio(audioBuffer);
  return { mensajefinal: resultado.texto, whisper: resultado };
}

// ============================================================
// PASO 3 — DISPERSADOR (clasificador de intención) — OpenAI gpt-5.4-mini
// ============================================================
function construirSystemMessageDispersador(contextoUsuario) {
  return `Eres un clasificador. Responde SOLO con una palabra.
CONTEXTO: ${JSON.stringify(contextoUsuario || {})}
PASOS (seguir en orden):
0. Si CONTEXTO.extra contiene "ESPERANDO_NOMBRE_PROMO" Y el mensaje NO detectas señales de 
cambio de intención o cambia de tema  ( o dice "no", "olvida", "mejor otra cosa", "ya no" ,etc
claramente obia "CONTINUIDAD_INFO"
1. VERIFICA EL EXTRA PARA QUE TENGAS MAYOR CONTEXTO Y CLASIFIQUES SEGUN LA CONVERSACION
2. Si el mensaje tiene "otro/otra/otros" → responde NEGOCIO o TURISMO según el contexto.
3. Si el mensaje menciona un nombre, negocio o lugar → ignora el contexto y clasifica solo.
4.si detetas intencion que busca ofertas promociones o sinonimos similares → responde PROMOCIONES.
5. CONTINUIDAD_INFO solo si: hay contexto previo, no hay nombre nuevo, y el mensaje pregunta algo concreto del mismo negocio y el mismo "tipo" sino obiar esto.
6. Si dudas entre CONTINUIDAD_INFO y otra → elige NEGOCIO o TURISMO.
CATEGORÍAS:
- EMERGENCIA: peligro de vida real ahora mismo, o pide número de SAMU/policía/serenazgo.
- PELIGRO: amenaza, extorsión o delito real. No expresiones de enojo del usuario no emergencia real.
- CONTINUIDAD_INFO: pregunta concreta sobre el mismo negocio del contexto y el mismo "tipo" .
- PROMOCIONES: busca descuentos, ofertas, precios bajos o dice que no tiene dinero.
- NEGOCIO: busca tienda, producto, servicio, o quiere comer/tomar/consumir algo nombre de tienda o negocio.
- TURISMO: busca lugares turisticos playas plazas no incluye cuidades. No incluye querer comer o consumir.
- GEINZ: saludo, soporte, registrar su negocio, mensaje sin sentido claro.
PRIORIDAD: EMERGENCIA > PELIGRO > paso 0 (ESPERANDO) > CONTINUIDAD_INFO > PROMOCIONES > NEGOCIO > TURISMO > GEINZ
Responde solo: EMERGENCIA | PELIGRO | CONTINUIDAD_INFO | PROMOCIONES | NEGOCIO | TURISMO | GEINZ`;
}

const CATEGORIAS_VALIDAS = [
  "EMERGENCIA",
  "PELIGRO",
  "CONTINUIDAD_INFO",
  "PROMOCIONES",
  "NEGOCIO",
  "TURISMO",
  "GEINZ",
];
function limpiarCategoria(raw) {
  const limpio = (raw || "").trim().toUpperCase();
  return CATEGORIAS_VALIDAS.includes(limpio) ? limpio : "GEINZ";
}

async function clasificarIntencion(mensajeUsuario, contextoUsuario) {
  const systemMessage = construirSystemMessageDispersador(contextoUsuario);
  const completion = await openai.chat.completions.create({
    model: "gpt-5.4-mini",
    messages: [
      { role: "system", content: systemMessage },
      { role: "user", content: mensajeUsuario },
    ],
    reasoning_effort: "low",
  });

  const raw = (completion.choices[0]?.message?.content || "").trim();
  const tokens = {
    prompt_tokens: completion.usage?.prompt_tokens || 0,
    completion_tokens: completion.usage?.completion_tokens || 0,
    thoughts_tokens:
      completion.usage?.completion_tokens_details?.reasoning_tokens || 0,
    total_tokens: completion.usage?.total_tokens || 0,
  };

  return { categoria: limpiarCategoria(raw), tokens };
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
  return null;
}

// ---- Mensaje de cuenta bloqueada ----
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

// ============================================================
// CLOUD FUNCTION PRINCIPAL
// ============================================================

exports.geinz_webhook_principal = onRequest(async (req, res) => {
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
      // 👇 Caso especial: si es sticker y hay un task vivo (el usuario ya
      // mandó texto y se está procesando), lo ignoramos por completo.
      if (tipoNoSoportado === "sticker" || tipoNoSoportado === "pool") {
        const activo = await hayActividadReciente(numero_usuario);

        if (activo) {
          console.log(
            "🙈 [geinz_webhook_principal] Sticker ignorado, hay task viva | 👤:",
            numero_usuario,
          );
          return res.status(200).json({
            ok: true,
            info: "Sticker ignorado porque hay un mensaje en proceso",
            numero_usuario,
          });
        }
      }

      const mensajeEnlatado = construirMensajeNoSoportado(tipoNoSoportado);
      await enviarMensajeWhatsapp(numero_usuario, mensajeEnlatado);

      console.log(
        "📦 [geinz_webhook_principal] Tipo no soportado:",
        tipoNoSoportado,
        "| 👤 NUMERO:",
        numero_usuario,
      );

      return res.status(200).json({
        ok: true,
        tipo_mensaje: tipoNoSoportado,
        mensaje_enviado: mensajeEnlatado,
        numero_usuario,
      });
    }

    const usuarioInfo = await validarUsuario({
      numero_usuario,
      id_user,
    });
    const nombre_user = usuarioInfo.nombre_user || "Usuario";

    if (usuarioInfo.is_spam) {
      await enviarMensajeWhatsapp(numero_usuario, usuarioInfo.mensaje_spam);
      return res
        .status(200)
        .json({ ok: true, bloqueado: true, motivo: usuarioInfo.mensaje_spam });
    }

    // 2) Si está baneado
    if (usuarioInfo.fecha_bloqueo && usuarioInfo.motivo_bloqueo) {
      const mensajeBan = construirMensajeBaneado(
        usuarioInfo.fecha_bloqueo,
        usuarioInfo.motivo_bloqueo,
      );
      await enviarMensajeWhatsapp(numero_usuario, mensajeBan);
      return res
        .status(200)
        .json({ ok: true, baneado: true, mensaje: mensajeBan });
    }

    // 3) Resolver el mensaje según tipo (texto o audio)
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

    if (!mensajeFinal.trim()) {
      return res
        .status(200)
        .json({ ok: true, info: "Mensaje vacío tras resolución" });
    }

    // 4) Guardar en buffer y programar la task de debounce
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

    console.log(
      "🕒 [geinz_webhook_principal] Mensaje bufferizado, task programada | 👤:",
      numero_usuario,
      "| 📨 mensajeId:",
      mensajeId,
    );

    return res.status(200).json({ ok: true, buffered: true, mensajeId });
  } catch (error) {
    console.error("❌ Error geinz_webhook_principal:", error.message);
    const tiempo_ms = Date.now() - inicio;
    return res.status(500).json({ ok: false, error: error.message, tiempo_ms });
  }
});
// ============================================================
// LEER Y VALIDAR BUFFER
// ============================================================
// ============================================================
// LEER Y VALIDAR BUFFER
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
  const vinoDeAudio = data.origen_audio === true; // 👈 lo capturamos ANTES de borrarlo

  await ref.update({
    mensajes: admin.firestore.FieldValue.delete(),
    last_message_id: admin.firestore.FieldValue.delete(),
    origen_audio: admin.firestore.FieldValue.delete(), // 👈 se limpia para el próximo mensaje
    procesando: true,
    procesando_desde: Date.now(),
  });

  return {
    valido: true,
    textoConcatenado,
    cantidad: ordenados.length,
    vinoDeAudio,
  };
}

// ============================================================
// CLOUD FUNCTION — PROCESAR BUFFER (disparada por Cloud Tasks)
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
        console.warn(
          "⚠️ geinz_procesar_buffer: faltan numero_usuario o mensajeId",
        );
        return res
          .status(200)
          .json({ ok: true, info: "Payload incompleto, ignorado" });
      }

      // 👇 Lectura del usuario en paralelo con la validación del buffer.
      //    Ambas dependen solo de numero_usuario, no una de la otra.
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
        console.log(
          "🗑️ [geinz_procesar_buffer] Task descartada:",
          resultadoBuffer.motivo,
          "| 👤:",
          numero_usuario,
          "| mensajeId:",
          mensajeId,
        );
        return res
          .status(200)
          .json({ ok: true, descartado: true, motivo: resultadoBuffer.motivo });
      }

      // 👇 A partir de aquí "procesando: true" ya quedó seteado dentro de
      //    leerYValidarBuffer. Todo lo que sigue va en try/finally para
      //    GARANTIZAR que se limpie el flag pase lo que pase (éxito, error,
      //    cualquier branch de categoría).
      try {
        const mensajeFinal = resultadoBuffer.textoConcatenado;
        const vinoDeAudio = resultadoBuffer.vinoDeAudio === true;
        console.log(
          "📦 [geinz_procesar_buffer] Procesando buffer combinado | 👤:",
          numero_usuario,
          "| 🧩 mensajes combinados:",
          resultadoBuffer.cantidad,
          "| 💬 texto:",
          mensajeFinal,
        );

        // 👇 usuarioSnap ya se leyó en paralelo arriba — no se vuelve a pedir.
        const usuarioData = usuarioSnap.exists ? usuarioSnap.data() : {};
        const nombre_user = usuarioData.nombre_user || "Usuario";
        const contextoUsuario = usuarioData.contexto || CONTEXTO_DEFAULT;

        const { categoria, tokens: tokensClasificador } =
          await clasificarIntencion(mensajeFinal, contextoUsuario);

        const mensajeEsperaInicial = construirMensajeEspera(
          categoria,
          nombre_user,
        );
        if (mensajeEsperaInicial) {
          enviarMensajeWhatsapp(numero_usuario, mensajeEsperaInicial).catch(
            (e) => console.error("❌ Falló mensaje de espera:", e.message),
          );
        }

        if (categoria === "EMERGENCIA") {
          const contextoActualizadoEmergencia = {
            ...limpiarCamposPromoDelContexto(contextoUsuario),
            tipo: "EMERGENCIA",
          };
          const promesaContexto = actualizarContextoUsuario(
            numero_usuario,
            contextoActualizadoEmergencia,
          );

          const resultadoEmergencia = await procesarEmergencia({
            localidad: "barranca",
            mensaje: mensajeFinal,
            nombreUsuario: nombre_user,
            numero_usuario,
          });

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
              emergencia: resultadoEmergencia?.tokens_usados || null,
              total_tokens_combinado:
                tokensClasificador.total_tokens +
                (resultadoEmergencia?.tokens_usados?.total_tokens_combinado ||
                  0),
            },
            tiempo_ms,
          });
        }

        if (categoria === "NEGOCIO") {
          const resultadoTienda = await procesarBusquedaTienda({
            mensaje: mensajeFinal,
            contexto_previo: contextoUsuario,
            localidad: "barranca",
            excluir_id: contextoUsuario?.id || null,
            nombre_usuario: nombre_user,
          });

          const contextoActualizadoNegocio = {
            ...limpiarCamposPromoDelContexto(contextoUsuario),
            tipo: "NEGOCIO",
            categoria: resultadoTienda.cat_detectada || null,
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
              console.error(
                "❌ [NEGOCIO] Falló plantilla, mando texto de respaldo:",
                e.message,
              );
              if (resultadoTienda.mensaje_safe) {
                await enviarMensajeWhatsapp(
                  numero_usuario,
                  resultadoTienda.mensaje_safe,
                );
              }
            }
          } else if (resultadoTienda.mensaje_safe) {
            await enviarMensajeWhatsapp(
              numero_usuario,
              resultadoTienda.mensaje_safe,
            );
            if (resultadoTienda.era_plantilla_pero_misio === true) {
              enviarNotificacionSinSaldo({
                id_tienda: resultadoTienda.id,
                localidad: "barranca",
                nombre_negocio: resultadoTienda.nombre_negocio,
              }).catch((e) =>
                console.error(
                  "❌ [NEGOCIO] Falló notificar tienda sin saldo:",
                  e.message,
                ),
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
              total_tokens_combinado:
                tokensClasificador.total_tokens +
                (resultadoTienda.tokens_usados?.total_tokens_combinado || 0),
            },
            tiempo_ms,
          });
        }

        if (categoria === "GEINZ") {
          const { resultado: respuestaGeinz, tokens: tokensGeinz } =
            await llamarGeminiGeinz(mensajeFinal, nombre_user);

          const contextoActualizadoGeinz = {
            ...limpiarCamposPromoDelContexto(contextoUsuario),
            tipo: "GEINZ",
            extra: respuestaGeinz.extra || "null",
          };
          const promesaContexto = actualizarContextoUsuario(
            numero_usuario,
            contextoActualizadoGeinz,
          );

          if (respuestaGeinz.mensaje) {
            // 👇 Se decide ANTES de mandar nada: si toca audio, se intenta audio
            //    y el texto queda como respaldo SOLO si el audio falla.
            const debeIntentarAudio =
              vinoDeAudio &&
              !contieneNumeroOLink(respuestaGeinz.mensaje) &&
              Math.random() < PROBABILIDAD_AUDIO;

            let audioEnviado = false;

            if (debeIntentarAudio) {
              // Se AWAITEA a propósito: en Cloud Functions gen2, si retornas la
              // respuesta HTTP antes de tiempo, el contenedor puede congelarse y
              // el fetch "en segundo plano" nunca termina de ejecutarse.
              audioEnviado = await intentarResponderConAudio({
                recipientPhoneNumber: numero_usuario,
                texto: respuestaGeinz.mensaje,
              });
            }

            // Solo se manda texto si NO tocaba audio, o si tocaba pero falló.
            if (!audioEnviado) {
              await enviarMensajeWhatsapp(
                numero_usuario,
                respuestaGeinz.mensaje,
              );
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
              total_tokens_combinado:
                tokensClasificador.total_tokens + tokensGeinz.total_tokens,
            },
            tiempo_ms,
          });
        }

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
              console.error(
                "❌ [TURISMO] Falló imagen, texto de respaldo:",
                e.message,
              );
              if (resultadoTurismo.mensaje_safe) {
                try {
                  await enviarMensajeWhatsapp(
                    numero_usuario,
                    resultadoTurismo.mensaje_safe,
                  );
                  mensajeResultadoEnviado = true;
                } catch (e2) {
                  console.error(
                    "❌ [TURISMO] Falló también texto de respaldo:",
                    e2.message,
                  );
                }
              }
            }
          } else if (resultadoTurismo.mensaje_safe) {
            try {
              await enviarMensajeWhatsapp(
                numero_usuario,
                resultadoTurismo.mensaje_safe,
              );
              mensajeResultadoEnviado = true;
            } catch (e) {
              console.error(
                "❌ [TURISMO] Falló texto de resultado:",
                e.message,
              );
            }
          }

          if (resultadoTurismo.siker && mensajeResultadoEnviado) {
            try {
              await enviarStickerWhatsapp(
                numero_usuario,
                resultadoTurismo.siker,
              );
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
              total_tokens_combinado:
                tokensClasificador.total_tokens +
                (resultadoTurismo.tokens_usados?.total || 0),
            },
            tiempo_ms,
          });
        }

        if (categoria === "CONTINUIDAD_INFO") {
          const contextoConContinuidad =
            prepararContextoContinuidad(contextoUsuario);

          if (!contextoConContinuidad.id && !contextoConContinuidad.nombre) {
            console.warn("⚠️ CONTINUIDAD_INFO sin id/nombre en contexto");
          }

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
              resultadoContinuidad.cat_detectada ||
              contextoConContinuidad.categoria ||
              null,
            id: resultadoContinuidad.id || contextoConContinuidad.id || null,
            nombre:
              resultadoContinuidad.nombre_negocio ||
              contextoConContinuidad.nombre ||
              null,
            extra: resultadoContinuidad.data || "null",
          };
          const promesaContexto = actualizarContextoUsuario(
            numero_usuario,
            contextoActualizadoContinuidad,
          );

          if (resultadoContinuidad.mensaje_safe) {
            await enviarMensajeWhatsapp(
              numero_usuario,
              resultadoContinuidad.mensaje_safe,
            );
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
              total_tokens_combinado:
                tokensClasificador.total_tokens +
                (resultadoContinuidad.tokens_usados?.gemini?.total_tokens || 0),
            },
            tiempo_ms,
          });
        }

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
            console.error(
              "❌ [PELIGRO] Falló mensaje de moderación:",
              e.message,
            );
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
              total_tokens_combinado: tokensClasificador.total_tokens,
            },
            tiempo_ms,
          });
        }

        if (categoria === "PROMOCIONES") {
          const resultadoPromo = await procesarPromociones({
            mensaje: mensajeFinal,
            contexto_previo: contextoUsuario,
            nombre_usuario: nombre_user,
          });

          // Caso 1: falta info → pedir tienda/categoría y marcar ESPERANDO_NOMBRE_PROMO
          if (resultadoPromo.preguntar_mejor) {
            const mensajePreguntar =
              construirMensajePreguntarPromo(nombre_user);
            const contextoActualizadoPromo = {
              ...limpiarCamposPromoDelContexto(contextoUsuario),
              tipo: "PROMOCIONES",
              categoria: null,
              id: null,
              nombre: null, // 👈 limpiar explícitamente lo heredado
              extra:
                "ESPERANDO_NOMBRE_PROMO: se le pidió al usuario un nombre de negocio o categoría para buscar promociones",
            };
            await actualizarContextoUsuario(
              numero_usuario,
              contextoActualizadoPromo,
            );
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

          // Caso 2: pidió tienda puntual y esa tienda no tiene promos
          if (resultadoPromo.sin_resultados) {
            const mensajeSinPromo = construirMensajeSinPromo(
              resultadoPromo.referencia,
              nombre_user,
              resultadoPromo.tipo_referencia,
            );
            // Caso 2 — sin_resultados
            const contextoActualizadoPromo = {
              ...limpiarCamposPromoDelContexto(contextoUsuario),
              tipo: "PROMOCIONES",
              categoria: null,
              id: null,
              nombre: null,
              extra:
                "pedi al usuario otro nombre o categoria para darle las promociones",
            };
            await actualizarContextoUsuario(
              numero_usuario,
              contextoActualizadoPromo,
            );
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

          // Caso 3: hay promo(s) de verdad → imagen+sticker (1 promo) o plantilla (2+)
          const contextoActualizadoPromo = {
            ...contextoUsuario,
            ...resultadoPromo.data,
          };
          const promesaContexto = actualizarContextoUsuario(
            numero_usuario,
            contextoActualizadoPromo,
          );

          const usarImagenYSticker =
            resultadoPromo.imagen &&
            (resultadoPromo.data?.ids_promos?.length || 0) < 2;

          if (usarImagenYSticker) {
            try {
              await enviarImagenWhatsapp(
                numero_usuario,
                resultadoPromo.imagen,
                resultadoPromo.mensaje_safe,
              );
            } catch (e) {
              console.error("❌ [PROMOCIONES] Falló imagen:", e.message);
            }
            if (resultadoPromo.siker) {
              try {
                await enviarStickerWhatsapp(
                  numero_usuario,
                  resultadoPromo.siker,
                );
              } catch (e) {
                console.error("❌ [PROMOCIONES] Falló sticker:", e.message);
              }
            }
          } else {
            try {
              await enviarPlantillaWhatsapp_promociones({
                recipientPhoneNumber: numero_usuario,
                imagen: resultadoPromo.imagen,
                mensaje: resultadoPromo.mensaje,
                ids: resultadoPromo.data?.ids_promos || [],
              });
            } catch (e) {
              console.error(
                "❌ [PROMOCIONES] Falló plantilla, mando texto de respaldo:",
                e.message,
              );
              if (resultadoPromo.mensaje_safe) {
                await enviarMensajeWhatsapp(
                  numero_usuario,
                  resultadoPromo.mensaje_safe,
                );
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

        // Categorías restantes (ej. PROMOCIONES) sin rama específica todavía
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
        // 👇 SIEMPRE se ejecuta, sin importar qué branch retornó o si
        //    algo lanzó una excepción — la ventana de "procesando" se
        //    cierra recién cuando el bot terminó de responder.
        await marcarProcesando(numero_usuario, false);
      }
    } catch (error) {
      console.error("❌ Error geinz_procesar_buffer:", error.message);
      if (numero_usuario_actual) {
        await marcarProcesando(numero_usuario_actual, false);
      }
      const tiempo_ms = Date.now() - inicio;
      return res
        .status(500)
        .json({ ok: false, error: error.message, tiempo_ms });
    }
  },
);

const CAMPOS_EXCLUSIVOS_PROMOCIONES = ["ids_promos", "mas_de_uno"];

function limpiarCamposPromoDelContexto(contexto) {
  const limpio = { ...(contexto || {}) };
  for (const campo of CAMPOS_EXCLUSIVOS_PROMOCIONES) {
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

async function enviarPlantillaBaneoWhatsapp({
  recipientPhoneNumber,
  nombre_user,
  mensajeFinal,
}) {
  const url = `https://graph.facebook.com/${WHATSAPP_API_VERSION}/${WHATSAPP_PHONE_NUMBER_ID}/messages`;

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

  const resp = await fetch(url, {
    method: "POST",
    headers: {
      Authorization: `Bearer ${WHATSAPP_TOKEN}`,
      "Content-Type": "application/json",
    },
    body: JSON.stringify(body),
  });

  if (!resp.ok) {
    throw new Error(
      `Error enviando plantilla de baneo WhatsApp: ${resp.status} ${await resp.text()}`,
    );
  }

  return resp.json();
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

// ---- Mensajes cuando falta info para buscar promos ----
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

// ---- Mensajes cuando la tienda pedida no tiene promos ----
function construirMensajeSinPromo(
  referencia,
  nombreUsuario,
  tipoReferencia = "tienda",
) {
  const n = nombreUsuario || "amigo";
  const otra =
    tipoReferencia === "categoria" ? "otra categoría" : "otra tienda";
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

// ---- Plantilla de WhatsApp para promociones (2+ promos) ----
async function enviarPlantillaWhatsapp_promociones({
  recipientPhoneNumber,
  imagen,
  mensaje,
  ids,
}) {
  const url = `https://graph.facebook.com/${WHATSAPP_API_VERSION}/${WHATSAPP_PHONE_NUMBER_ID}/messages`;

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
        {
          type: "body",
          parameters: [{ type: "text", text: mensaje }],
        },
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

  const resp = await fetch(url, {
    method: "POST",
    headers: {
      Authorization: `Bearer ${WHATSAPP_TOKEN}`,
      "Content-Type": "application/json",
    },
    body: JSON.stringify(body),
  });

  if (!resp.ok) {
    throw new Error(
      `Error enviando plantilla de promociones: ${resp.status} ${await resp.text()}`,
    );
  }

  return resp.json();
}

// ============================================================
// AUDIO DE RESPUESTA (TTS condicional)
// ============================================================
const TTS_ELEVENLABS_URL =
  "https://us-central1-geinzworkapp.cloudfunctions.net/textoAVozn8n_elevenlabs_2";
const TTS_VOICE_ID_DEFAULT = "KFBj2OnpjcE1zKB9CGb8";
const PROBABILIDAD_AUDIO = 0.8;

// Detecta números de celular/teléfono o links dentro de un texto.
// Si detecta cualquiera de los dos, NUNCA se debe mandar audio.
function contieneNumeroOLink(texto) {
  if (!texto) return false;

  // Links
  if (/https?:\/\/[^\s]+/i.test(texto)) return true;
  if (/\bwww\.[^\s]+/i.test(texto)) return true;

  // Celular peruano: +51 9XX XXX XXX, 9XXXXXXXX, con o sin separadores
  if (/(\+?51[\s-]?)?9\d{2}[\s.-]?\d{3}[\s.-]?\d{3}\b/.test(texto)) return true;

  // Cualquier secuencia larga de dígitos (fijos, otros formatos, etc.)
  if (/\d[\d\s.-]{6,}\d/.test(texto)) return true;

  return false;
}

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

async function subirAudioWhatsapp(
  bufferAudio,
  mimeType = "audio/ogg; codecs=opus",
) {
  const url = `https://graph.facebook.com/${WHATSAPP_API_VERSION}/${WHATSAPP_PHONE_NUMBER_ID}/media`;

  const form = new FormData();
  form.append("messaging_product", "whatsapp");
  form.append("file", new Blob([bufferAudio], { type: mimeType }), "audio.ogg");

  const resp = await fetch(url, {
    method: "POST",
    headers: { Authorization: `Bearer ${WHATSAPP_TOKEN}` }, // ojo: SIN Content-Type, lo pone fetch solo con FormData
    body: form,
  });

  if (!resp.ok) {
    throw new Error(
      `Error subiendo audio a WhatsApp: ${resp.status} ${await resp.text()}`,
    );
  }

  const data = await resp.json();
  return data.id; // media_id
}

async function enviarAudioWhatsapp(recipientPhoneNumber, mediaId) {
  const url = `https://graph.facebook.com/${WHATSAPP_API_VERSION}/${WHATSAPP_PHONE_NUMBER_ID}/messages`;

  const resp = await fetch(url, {
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

  if (!resp.ok) {
    throw new Error(
      `Error enviando audio WhatsApp: ${resp.status} ${await resp.text()}`,
    );
  }

  return resp.json();
}

async function intentarResponderConAudio({ recipientPhoneNumber, texto }) {
  try {
    const audioBuffer = await generarAudioTTS(texto);
    const mediaId = await subirAudioWhatsapp(audioBuffer);
    await enviarAudioWhatsapp(recipientPhoneNumber, mediaId);
    console.log(
      "🔊 [TTS] Audio enviado correctamente | 👤:",
      recipientPhoneNumber,
    );
    return true;
  } catch (e) {
    console.error(
      "❌ [TTS] Falló el envío de audio (se mantiene solo texto):",
      e.message,
    );
    return false;
  }
}

async function enviarNotificacionSinSaldo({
  id_tienda,
  localidad,
  nombre_negocio,
}) {
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

  if (!tiendaSnap.exists) {
    throw new Error("Tienda no encontrada.");
  }

  const propietario_ids = tiendaSnap.data().propietario_id || [];
  if (propietario_ids.length === 0) {
    throw new Error("La tienda no tiene propietarios registrados.");
  }

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

  if (todosLosTokens.length === 0) {
    throw new Error("No se encontraron tokens para los propietarios.");
  }

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

async function enviarNotificacionFCM_tienda({
  token,
  title,
  body,
  link = "https://geinztech.com/share?t=scr&id=ads",
  logo = "https://firebasestorage.googleapis.com/v0/b/geinzworkapp.appspot.com/o/logo_geinz_webp.webp?alt=media&token=aa1ef1df-1bcd-48f2-9cad-a85929c3a8d0",
  image = "",
  idTienda,
  idAnuncio = "", // ✅ agregar
  tipo_notificacion,
  prioridad = "high",
}) {
  try {
    const mensaje = {
      token: token,
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
    const respuesta = await admin.messaging().send(mensaje);
    console.log("Notificación enviada al token:", token);
    return respuesta;
  } catch (error) {
    console.error("ERROR enviarNotificacionFCM:", error);
    if (error.code === "messaging/registration-token-not-registered") {
      console.log("Token inválido, debería eliminarlo de Firestore:", token);
    }
  }
}

async function enviarMensajeTextoWhatsApp(recipientPhoneNumber, mensajeTexto) {
  const url = `https://graph.facebook.com/${WHATSAPP_API_VERSION}/${WHATSAPP_PHONE_NUMBER_ID}/messages`;

  const body = {
    messaging_product: "whatsapp",
    to: recipientPhoneNumber,
    type: "text",
    text: { body: mensajeTexto, preview_url: false },
  };

  const resp = await fetch(url, {
    method: "POST",
    headers: {
      Authorization: `Bearer ${WHATSAPP_TOKEN}`,
      "Content-Type": "application/json",
    },
    body: JSON.stringify(body),
  });

  if (!resp.ok) {
    const errText = await resp.text();
    throw new Error(`Error enviando WhatsApp: ${resp.status} ${errText}`);
  }

  return resp.json();
}
