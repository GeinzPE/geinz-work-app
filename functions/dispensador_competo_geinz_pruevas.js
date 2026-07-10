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

const MAX_TOKENS_RATE = 3;
const MS_POR_TOKEN = 2000;
const DURACION_BLOQUEO = 15000;

const CONTEXTO_DEFAULT = { tipo: "GEINZ", categoria: null, extra: "null", id: null, nombre: null };

// ============================================================
// UTILIDADES
// ============================================================
function pick(arr) {
  return arr[Math.floor(Math.random() * arr.length)];
}

function tokensVacios() {
  return { prompt_tokens: 0, completion_tokens: 0, thoughts_tokens: 0, total_tokens: 0 };
}

function sumarTokens(total, extra) {
  total.prompt_tokens += extra?.prompt_tokens || 0;
  total.completion_tokens += extra?.completion_tokens || 0;
  total.thoughts_tokens += extra?.thoughts_tokens || 0;
  total.total_tokens += extra?.total_tokens || 0;
}

// ============================================================
// PASO 1 — VALIDAR / RATE LIMIT USUARIO
// ============================================================
async function validarUsuario({ numero_usuario, nombre_user, id_user }) {
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
        nombre_user: nombre_user || "Usuario",
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
      return { exists: false, is_spam: false, rate_limit: false, ...nuevoUsuario };
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

    const tokensRecuperados = Math.floor((ahora - ultimoMensaje) / MS_POR_TOKEN);
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
    headers: { Authorization: `Bearer ${WHATSAPP_TOKEN}`, "Content-Type": "application/json" },
    body: JSON.stringify({
      messaging_product: "whatsapp",
      to: recipientPhoneNumber,
      type: "text",
      text: { body: textBody },
    }),
  });
  if (!resp.ok) throw new Error(`Error enviando mensaje WhatsApp: ${resp.status} ${await resp.text()}`);
  return resp.json();
}

async function obtenerUrlMedia(mediaId) {
  const url = `https://graph.facebook.com/${WHATSAPP_API_VERSION}/${mediaId}`;
  const resp = await fetch(url, { headers: { Authorization: `Bearer ${WHATSAPP_TOKEN}` } });
  if (!resp.ok) throw new Error(`Error obteniendo URL de media: ${resp.status} ${await resp.text()}`);
  const data = await resp.json();
  return data.url;
}

async function descargarAudioBinario(mediaUrl) {
  const resp = await fetch(mediaUrl, { headers: { Authorization: `Bearer ${WHATSAPP_TOKEN}` } });
  if (!resp.ok) throw new Error(`Error descargando audio: ${resp.status} ${await resp.text()}`);
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

async function procesarAudioWhatsapp({ mediaId, recipientPhoneNumber, nombreUsuario }) {
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
0. Si CONTEXTO.extra contiene "ESPERANDO_NOMBRE_PROMO" Y el mensaje NO tiene señales de cambio de intención (no dice "no", "olvida", "mejor otra cosa", "ya no", ni pide algo claramente distinto como emergencia/peligro) → responde PROMOCIONES. Para.
1. VERIFICA EL EXTRA PARA QUE TENGAS MAYOR CONTEXTO Y CLASIFIQUES SEGUN LA CONVERSACION
2. Si el mensaje tiene "otro/otra/otros" → responde NEGOCIO o TURISMO según el contexto. Para.
3. Si el mensaje menciona un nombre, negocio o lugar → ignora el contexto y clasifica solo.
4. Si hay contexto previo y el mensaje menciona ofertas, promos, descuentos, precios o falta de dinero sin nombrar nada nuevo → responde PROMOCIONES. Para.
5. CONTINUIDAD_INFO solo si: hay contexto previo, no hay nombre nuevo, y el mensaje pregunta algo concreto del mismo negocio y el mismo "tipo" sino obiar esto.
6. Si dudas entre CONTINUIDAD_INFO y otra → elige NEGOCIO o TURISMO.
CATEGORÍAS:
- EMERGENCIA: peligro de vida real ahora mismo, o pide número de SAMU/policía/serenazgo/salud.
- PELIGRO: amenaza, extorsión o delito real. No expresiones de enojo.
- CONTINUIDAD_INFO: pregunta concreta sobre el mismo negocio del contexto y el mismo "tipo" .
- PROMOCIONES: busca descuentos, ofertas, precios bajos o dice que no tiene dinero.
- NEGOCIO: busca tienda, producto, servicio, o quiere comer/tomar/consumir algo nombre de tienda o negocio.
- TURISMO: busca lugares para visitar. No incluye querer comer o consumir.
- GEINZ: saludo, soporte, registrar su negocio, mensaje sin sentido claro.
PRIORIDAD: EMERGENCIA > PELIGRO > paso 0 (ESPERANDO) > CONTINUIDAD_INFO > PROMOCIONES > NEGOCIO > TURISMO > GEINZ
Responde solo: EMERGENCIA | PELIGRO | CONTINUIDAD_INFO | PROMOCIONES | NEGOCIO | TURISMO | GEINZ`;
}

const CATEGORIAS_VALIDAS = ["EMERGENCIA","PELIGRO","CONTINUIDAD_INFO","PROMOCIONES","NEGOCIO","TURISMO","GEINZ"];
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
    thoughts_tokens: completion.usage?.completion_tokens_details?.reasoning_tokens || 0,
    total_tokens: completion.usage?.total_tokens || 0,
  };

  return { categoria: limpiarCategoria(raw), tokens };
}

// ---- Mensajes de "espera" según categoría ----
function construirMensajeEspera(categoria, nombreUsuario) {
  const n = nombreUsuario || "amigo";

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
    try { fecha = new Date(JSON.parse(fechaRaw)._seconds * 1000); } catch (e) { return "fecha inválida"; }
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
  return fecha.toLocaleString("es-PE", { day: "2-digit", month: "long", year: "numeric", hour: "2-digit", minute: "2-digit" });
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
  const inicio = Date.now();
  const tokensOpenAI = tokensVacios();

  try {
    const entry = req.body?.entry?.[0];
    const value = entry?.changes?.[0]?.value;
    const mensajeWa = value?.messages?.[0];
    const contacto = value?.contacts?.[0];

    if (!mensajeWa || !contacto) {
      return res.status(200).json({ ok: true, info: "Evento sin mensaje procesable" });
    }

    const numero_usuario = contacto.wa_id;
    const nombre_user = contacto.profile?.name || "amigo";
    const id_user = value.metadata?.phone_number_id || "";

    // 1) Validar usuario / rate limit
    const usuarioInfo = await validarUsuario({ numero_usuario, nombre_user, id_user });

    if (usuarioInfo.is_spam) {
      await enviarMensajeWhatsapp(numero_usuario, usuarioInfo.mensaje_spam);
      return res.status(200).json({ ok: true, bloqueado: true, motivo: usuarioInfo.mensaje_spam });
    }

    // 2) Si está baneado
    if (usuarioInfo.fecha_bloqueo && usuarioInfo.motivo_bloqueo) {
      const mensajeBan = construirMensajeBaneado(usuarioInfo.fecha_bloqueo, usuarioInfo.motivo_bloqueo);
      await enviarMensajeWhatsapp(numero_usuario, mensajeBan);
      return res.status(200).json({ ok: true, baneado: true, mensaje: mensajeBan });
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
      return res.status(200).json({ ok: true, info: `Tipo de mensaje no soportado: ${mensajeWa.type}` });
    }

    if (!mensajeFinal.trim()) {
      return res.status(200).json({ ok: true, info: "Mensaje vacío tras resolución" });
    }

    // 4) Clasificar intención — el contexto viene del campo "contexto" en Firestore
    const contextoUsuario = usuarioInfo.contexto || CONTEXTO_DEFAULT;
    const { categoria, tokens: tokensClasificador } = await clasificarIntencion(mensajeFinal, contextoUsuario);
    sumarTokens(tokensOpenAI, tokensClasificador);

    // 5) Mensaje de "espera" según categoría
    const mensajeEspera = construirMensajeEspera(categoria, nombre_user);
    if (mensajeEspera) {
      await enviarMensajeWhatsapp(numero_usuario, mensajeEspera);
    }

    // 6) Actualizar el contexto en Firestore con la nueva categoría detectada
    //    (el "nombre"/"id" específico del negocio/lugar se actualiza después,
    //    en la función que hace la búsqueda real — turismo/negocio)
    const contextoActualizado = {
      ...contextoUsuario,
      tipo: categoria,
    };
    await actualizarContextoUsuario(numero_usuario, contextoActualizado);

    const tiempo_ms = Date.now() - inicio;

    console.log(
      "🧭 CATEGORIA:", categoria,
      "| 👤 USUARIO:", nombre_user,
      "| 💬 MENSAJE:", mensajeFinal,
      "| 💰 TOKENS OPENAI:", JSON.stringify(tokensOpenAI),
      whisperInfo ? `| 🎙️ WHISPER: ${JSON.stringify(whisperInfo)}` : "",
    );

    return res.status(200).json({
      ok: true,
      categoria,
      mensaje_usuario: mensajeFinal,
      nombre_usuario: nombre_user,
      numero_usuario,
      contexto_usuario: contextoActualizado,
      tokens_usados: {
        openai: tokensOpenAI,
        whisper: whisperInfo,
      },
      tiempo_ms,
    });
  } catch (error) {
    console.error("❌ Error geinz_webhook_principal:", error.message);
    const tiempo_ms = Date.now() - inicio;
    return res.status(500).json({ ok: false, error: error.message, tiempo_ms });
  }
});