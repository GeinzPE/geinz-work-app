// ============================================================
// telegram_dispensador_webhook.js
// Webhook de Telegram para el DISPENSADOR (proyecto actual, no Geinz).
// Reutiliza exactamente tu clasificador (dispensador.js) y tus 7 ramas:
//   general | negocio | carta_visual | busqueda_algolia |
//   pedidos_carrito | pagos_voucher | reclamos
//
// Diferencias clave frente a WhatsApp:
//   - No hay plantillas de Meta. Todo se manda como sendMessage / sendPhoto.
//   - No hay verificación GET tipo hub.challenge; Telegram solo hace POST.
//   - El audio (voice) se transcribe con Whisper igual que en WhatsApp.
//
// 👇 TOKENS (nuevo en este archivo):
//   clasificarRama() ahora devuelve {rama, tokens} en vez de solo el string
//   de la rama (ver dispensador.js). Y cada rama devuelve su propio
//   "tokens" (ver token_utils.js + cada archivo de rama).
//
//   Este webhook combina AMBOS con combinarTokens() para tener el gasto
//   TOTAL de la consulta completa (clasificación + redacción de la
//   respuesta), y:
//     1. Lo loggea claramente en Cloud Logging.
//     2. Lo guarda en el doc del usuario en Firestore (campo
//        tokens_ultima_consulta, y un acumulado histórico
//        tokens_totales_historicos).
//     3. Opcionalmente lo agrega al final del mensaje que se le manda al
//        usuario por Telegram (activar/desactivar con la env var
//        MOSTRAR_TOKENS_AL_USUARIO — ver más abajo).
//     4. Lo devuelve en la respuesta JSON del endpoint (campo
//        tokens_consulta), útil para debug/monitoreo aunque no se muestre
//        al usuario.
//
// 👇 BOTONES DE CARRITO (nuevo en este archivo):
//   Cuando la rama es "pedidos_carrito", el mensaje que se manda al
//   usuario ahora trae, además del texto, un teclado inline con un botón
//   "🗑️ <producto>" por cada item + "🧹 Vaciar carrito".
//
//   Cuando el usuario TOCA uno de esos botones, Telegram NO manda un
//   "message" normal — manda un update distinto: "callback_query". Ese
//   camino está separado del flujo de texto/audio de siempre:
//     1. Se detecta req.body.callback_query al inicio del webhook.
//     2. Se llama DIRECTO a eliminarItemCarritoPorId() / vaciarCarrito()
//        de pedido_carrito.js — sin pasar por dispensador.js, sin IA, sin
//        gastar tokens. El usuario borra su propio item, el código lo
//        ejecuta tal cual.
//     3. Se edita el mismo mensaje (editMessageText) con el carrito
//        actualizado y los botones recalculados, en vez de mandar un
//        mensaje nuevo — así el chat no se llena de carritos viejos.
//
// Pégalo junto a tu index.js y agrega:
//   const { dispensador_webhook_telegram } = require("./telegram_dispensador_webhook.js");
//   exports.dispensador_webhook_telegram = dispensador_webhook_telegram;
// ============================================================

const { onRequest } = require("firebase-functions/v2/https");
const admin = require("firebase-admin");
const OpenAI = require("openai");
const {
  combinarTokens,
  resumenPorProveedor,
  lineaTokens,
  armarTokens,
} = require("./token_utils.js");

if (!admin.apps.length) {
  admin.initializeApp();
}

const db = admin.firestore();

// 👇 AJUSTAR si tu key de Whisper vive en otra env var (en tus archivos
// vi tanto OPENAI_API_KEY como API_KEYO_OPEN_IA usados en distintos sitios).
const openai = new OpenAI({
  apiKey: process.env.OPENAI_API_KEY || process.env.API_KEYO_OPEN_IA,
});

// ---- Tus 7 ramas ya implementadas ----
const { clasificarRama } = require("./dispensador.js");
const { responderGeneral } = require("./general.js");
const { responderNegocio } = require("./negocio.js");
const { responderCartaVisual } = require("./carta_visual.js");
const { responderBusquedaAlgolia } = require("./busqueda_algolia.js");
const { responderReclamos } = require("./reclamos.js");
const {
  responderCarrito,
  eliminarItemCarritoPorId,
  vaciarCarrito,
  construirBotonesCarrito,
  formatearCarritoSinIA,
  obtenerOCrearTokenCarrito,
  agregarItemCarritoPorId,
} = require("./pedido_carrito.js");
const { responderPagos } = require("./pago_vaucher.js");

const { actualizarLeadStatusPorRama } = require("./lead_scoring.js");

// 👇 Ya no queda ninguna rama sin implementar. MENSAJES_RAMA_NO_IMPLEMENTADA
// se deja vacío (en vez de borrar el mecanismo entero) para que, si mañana
// agregas una rama nueva al catálogo de dispensador.js antes de tener el
// archivo listo, sea un solo objeto donde agregar el mensaje de espera.
const MENSAJES_RAMA_NO_IMPLEMENTADA = {};

// 👇 Si está en "true", se le agrega al usuario al final de cada mensaje
// una línea con los tokens gastados en esa consulta (ej. "🔢 Tokens:
// OpenAI 210 · Gemini 480 · Total 690"). Si no la quieres visible para el
// usuario final (por defecto no lo es), no toques nada — igual queda
// guardada en el historial y en los logs.
const MOSTRAR_TOKENS_AL_USUARIO =
  String(process.env.MOSTRAR_TOKENS_AL_USUARIO || "false").toLowerCase() ===
  "true";

// ============================================================
// CONFIG TELEGRAM
// ============================================================
const TELEGRAM_TOKEN = process.env.TELEGRAM_BOT_TOKEN_CHIFA_GEINZ;
const TG_API = `https://api.telegram.org/bot${TELEGRAM_TOKEN}`;
const TG_FILE_API = `https://api.telegram.org/file/bot${TELEGRAM_TOKEN}`;

const CONTEXTO_DEFAULT = { tipo: "general", extra: "null" };

// ============================================================
// ENVÍO — mensaje/imagen/botón/teclado, nunca plantillas (Telegram no las usa)
// ============================================================
async function enviarMensajeTelegram(chatId, texto) {
  console.log(
    "[telegram] → enviarMensajeTelegram | chatId:",
    chatId,
    "| texto:",
    texto,
  );
  const resp = await fetch(`${TG_API}/sendMessage`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ chat_id: chatId, text: texto }),
  });
  const json = await resp.json();
  if (!resp.ok) {
    console.error("[telegram] Error sendMessage:", JSON.stringify(json));
  } else {
    console.log("[telegram] ✅ sendMessage OK | chatId:", chatId);
  }
  return json;
}

/**
 * Manda un mensaje de texto con UN botón embebido (inline keyboard) que
 * abre una URL al tocarlo. Lo usa la rama pagos_voucher para el botón
 * "Pagar" → URL de pago, pero queda genérico por si otra rama lo necesita.
 *
 * @param {number|string} chatId
 * @param {string} texto
 * @param {{texto: string, url: string}} boton
 */
async function enviarMensajeConBotonTelegram(chatId, texto, boton) {
  console.log(
    "[telegram] → enviarMensajeConBotonTelegram | chatId:",
    chatId,
    "| boton:",
    JSON.stringify(boton),
  );
  const resp = await fetch(`${TG_API}/sendMessage`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({
      chat_id: chatId,
      text: texto,
      reply_markup: {
        inline_keyboard: [[{ text: boton.texto, url: boton.url }]],
      },
    }),
  });
  const json = await resp.json();
  if (!resp.ok) {
    console.error(
      "[telegram] Error sendMessage con botón:",
      JSON.stringify(json),
    );
  } else {
    console.log("[telegram] ✅ sendMessage con botón OK | chatId:", chatId);
  }
  return json;
}

/**
 * 👇 NUEVO — Manda un mensaje de texto con un teclado inline de VARIAS
 * filas (a diferencia de enviarMensajeConBotonTelegram, que es un solo
 * botón con URL). Lo usa la rama pedidos_carrito para mostrar un botón
 * "🗑️ <producto>" por cada item + "🧹 Vaciar carrito".
 *
 * @param {number|string} chatId
 * @param {string} texto
 * @param {Array<Array<{text:string, callback_data:string}>>} inline_keyboard
 */
async function enviarMensajeConTecladoTelegram(chatId, texto, inline_keyboard) {
  console.log(
    "[telegram] → enviarMensajeConTecladoTelegram | chatId:",
    chatId,
    "| filas de botones:",
    inline_keyboard.length,
  );
  const resp = await fetch(`${TG_API}/sendMessage`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({
      chat_id: chatId,
      text: texto,
      reply_markup: { inline_keyboard },
    }),
  });
  const json = await resp.json();
  if (!resp.ok) {
    console.error(
      "[telegram] Error sendMessage con teclado:",
      JSON.stringify(json),
    );
  } else {
    console.log(
      "[telegram] ✅ sendMessage con teclado OK | chatId:",
      chatId,
      "| message_id:",
      json.result?.message_id,
    );
  }
  return json;
}

/**
 * 👇 NUEVO — Edita un mensaje YA enviado (texto + teclado) en vez de mandar
 * uno nuevo. Se usa cuando el usuario toca un botón del carrito: en vez de
 * llenar el chat de carritos viejos, se actualiza el mismo mensaje.
 *
 * @param {number|string} chatId
 * @param {number} messageId
 * @param {string} texto
 * @param {Array<Array<{text:string, callback_data:string}>>} inline_keyboard
 */
async function editarMensajeConTecladoTelegram(
  chatId,
  messageId,
  texto,
  inline_keyboard,
) {
  console.log(
    "[telegram] → editarMensajeConTecladoTelegram | chatId:",
    chatId,
    "| messageId:",
    messageId,
  );
  const resp = await fetch(`${TG_API}/editMessageText`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({
      chat_id: chatId,
      message_id: messageId,
      text: texto,
      reply_markup:
        inline_keyboard && inline_keyboard.length
          ? { inline_keyboard }
          : { inline_keyboard: [] },
    }),
  });
  const json = await resp.json();
  if (!resp.ok) {
    console.error("[telegram] Error editMessageText:", JSON.stringify(json));
  } else {
    console.log(
      "[telegram] ✅ editMessageText OK | chatId:",
      chatId,
      "| messageId:",
      messageId,
    );
  }
  return json;
}

/**
 * 👇 NUEVO — Responde el "spinner" de carga que Telegram muestra en el
 * botón mientras procesa el tap. Es obligatorio llamarlo (o Telegram deja
 * el botón "cargando" hasta que expira solo). El "texto" opcional aparece
 * como un toast chiquito arriba del teclado del usuario.
 *
 * @param {string} callbackQueryId
 * @param {string} [texto]
 */
async function responderCallbackQueryTelegram(callbackQueryId, texto) {
  console.log(
    "[telegram] → responderCallbackQueryTelegram | callbackQueryId:",
    callbackQueryId,
    "| texto:",
    texto,
  );
  const resp = await fetch(`${TG_API}/answerCallbackQuery`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({
      callback_query_id: callbackQueryId,
      text: texto || "",
      show_alert: false,
    }),
  });
  const json = await resp.json();
  if (!resp.ok) {
    console.error(
      "[telegram] Error answerCallbackQuery:",
      JSON.stringify(json),
    );
  }
  return json;
}

async function enviarImagenTelegram(chatId, imagenUrl, caption) {
  console.log(
    "[telegram] → enviarImagenTelegram | chatId:",
    chatId,
    "| imagenUrl:",
    imagenUrl,
    "| caption:",
    caption,
  );
  const resp = await fetch(`${TG_API}/sendPhoto`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({
      chat_id: chatId,
      photo: imagenUrl,
      caption: caption || "",
    }),
  });
  const json = await resp.json();
  if (!resp.ok) {
    console.error("[telegram] Error sendPhoto:", JSON.stringify(json));
  } else {
    console.log("[telegram] ✅ sendPhoto OK | chatId:", chatId);
  }
  return json;
}

/**
 * Envía varias imágenes juntas como UN SOLO álbum (sendMediaGroup), en vez
 * de mandarlas una por una con sendPhoto. Telegram las agrupa visualmente
 * en un bloque. El texto (caption) va únicamente en la primera imagen del
 * grupo — así no se manda un mensaje de texto aparte y queda todo junto.
 *
 * Límite de Telegram: máximo 10 imágenes por álbum. Si llegara a haber más,
 * se recortan a las primeras 10 (caso raro para este bot, pero por seguridad).
 */
async function enviarGrupoImagenesTelegram(chatId, imagenes, caption) {
  const imagenesLimitadas = imagenes.slice(0, 10);
  console.log(
    "[telegram] → enviarGrupoImagenesTelegram | chatId:",
    chatId,
    "| cantidad:",
    imagenesLimitadas.length,
    "| caption:",
    caption,
  );

  const media = imagenesLimitadas.map((url, i) => ({
    type: "photo",
    media: url,
    // Solo la primera imagen lleva el texto; Telegram muestra ese caption
    // como el texto del álbum completo.
    ...(i === 0 && caption ? { caption } : {}),
  }));

  const resp = await fetch(`${TG_API}/sendMediaGroup`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ chat_id: chatId, media }),
  });
  const json = await resp.json();
  if (!resp.ok) {
    console.error("[telegram] Error sendMediaGroup:", JSON.stringify(json));
  } else {
    console.log(
      "[telegram] ✅ sendMediaGroup OK | chatId:",
      chatId,
      "| imágenes enviadas:",
      imagenesLimitadas.length,
    );
  }
  return json;
}

/**
 * Normaliza las distintas formas en que cada rama puede devolver imágenes
 * (imagenes: [], imagen: "", imagen_bot: "") a un solo array.
 */
function extraerImagenes(resultado) {
  console.log(
    "[telegram] → extraerImagenes | resultado:",
    JSON.stringify(resultado),
  );
  let out = [];
  if (!resultado) out = [];
  else if (Array.isArray(resultado.imagenes))
    out = resultado.imagenes.filter(Boolean);
  else if (resultado.imagen) out = [resultado.imagen];
  else if (resultado.imagen_bot) out = [resultado.imagen_bot];
  console.log(
    "[telegram] ← extraerImagenes | imágenes encontradas:",
    out.length,
  );
  return out;
}

/**
 * Envía la respuesta de cualquier rama:
 *   - Si trae "boton" (ej. pagos_voucher con { texto, url })  → sendMessage
 *     con inline keyboard de un solo botón con URL. Prioridad máxima.
 *   - Si trae "botones" (ej. pedidos_carrito con el teclado de 🗑️ por
 *     producto) → sendMessage con ese teclado completo. 👈 NUEVO
 *   - Si no hay botón/botones y hay imágenes:
 *       - 1 sola imagen  → sendPhoto con el texto como caption.
 *       - 2+ imágenes    → sendMediaGroup: todas juntas en un solo álbum,
 *                          con el texto como caption de la primera.
 *   - Si no hay nada de lo anterior → solo texto.
 * Así queda uniforme para las 7 ramas sin usar plantillas.
 *
 * @param {number|string} chatId
 * @param {Object} resultado - lo que devolvió la rama (con "mensaje" ya
 *   con la línea de tokens pegada si MOSTRAR_TOKENS_AL_USUARIO es true).
 */
async function enviarRespuestaRama(chatId, resultado) {
  console.log("[telegram] → enviarRespuestaRama | chatId:", chatId);
  const texto = (resultado && resultado.mensaje) || null;

  if (resultado?.boton) {
    console.log(
      "[telegram] enviarRespuestaRama: la rama devolvió botón, mandando inline keyboard",
    );
    try {
      await enviarMensajeConBotonTelegram(chatId, texto || "", resultado.boton);
    } catch (e) {
      console.error(
        "[telegram] Falló el mensaje con botón, texto de respaldo:",
        e.message,
      );
      if (texto) await enviarMensajeTelegram(chatId, texto);
    }
    console.log(
      "[telegram] ← enviarRespuestaRama: terminado | chatId:",
      chatId,
    );
    return;
  }

  // 👇 NUEVO — teclado con varios botones (carrito: uno por producto).
  if (
    resultado?.botones &&
    Array.isArray(resultado.botones) &&
    resultado.botones.length > 0
  ) {
    console.log(
      "[telegram] enviarRespuestaRama: la rama devolvió botones, mandando teclado inline",
    );
    try {
      await enviarMensajeConTecladoTelegram(
        chatId,
        texto || "",
        resultado.botones,
      );
    } catch (e) {
      console.error(
        "[telegram] Falló el mensaje con teclado, texto de respaldo:",
        e.message,
      );
      if (texto) await enviarMensajeTelegram(chatId, texto);
    }
    console.log(
      "[telegram] ← enviarRespuestaRama: terminado | chatId:",
      chatId,
    );
    return;
  }

  const imagenes = extraerImagenes(resultado);

  if (imagenes.length === 0) {
    console.log("[telegram] enviarRespuestaRama: sin imágenes, solo texto");
    if (texto) await enviarMensajeTelegram(chatId, texto);
    return;
  }

  if (imagenes.length === 1) {
    console.log(
      "[telegram] enviarRespuestaRama: una sola imagen, sendPhoto con caption",
    );
    try {
      await enviarImagenTelegram(chatId, imagenes[0], texto || "");
    } catch (e) {
      console.error("[telegram] Falló imagen, texto de respaldo:", e.message);
      if (texto) await enviarMensajeTelegram(chatId, texto);
    }
    console.log(
      "[telegram] ← enviarRespuestaRama: terminado | chatId:",
      chatId,
    );
    return;
  }

  // 2 o más imágenes: se mandan como un solo álbum agrupado.
  console.log(
    "[telegram] enviarRespuestaRama: múltiples imágenes, enviando como álbum (sendMediaGroup)",
  );
  try {
    await enviarGrupoImagenesTelegram(chatId, imagenes, texto || "");
  } catch (e) {
    console.error(
      "[telegram] Falló el álbum de imágenes, texto de respaldo:",
      e.message,
    );
    if (texto) await enviarMensajeTelegram(chatId, texto);
  }
  console.log("[telegram] ← enviarRespuestaRama: terminado | chatId:", chatId);
}

// ============================================================
// AUDIO (voice notes) → texto con Whisper
// ============================================================
async function obtenerUrlArchivoTelegram(fileId) {
  console.log("[telegram] → obtenerUrlArchivoTelegram | fileId:", fileId);
  const resp = await fetch(`${TG_API}/getFile?file_id=${fileId}`);
  const data = await resp.json();
  if (!data.ok) {
    console.error("[telegram] Error getFile:", JSON.stringify(data));
    throw new Error(`Telegram getFile error: ${JSON.stringify(data)}`);
  }
  const url = `${TG_FILE_API}/${data.result.file_path}`;
  console.log("[telegram] ← obtenerUrlArchivoTelegram | url:", url);
  return url;
}

async function descargarBinario(url) {
  console.log("[telegram] → descargarBinario | url:", url);
  const resp = await fetch(url);
  if (!resp.ok) {
    console.error(
      "[telegram] Error descargando archivo | status:",
      resp.status,
    );
    throw new Error(`Error descargando archivo: ${resp.status}`);
  }
  const buffer = Buffer.from(await resp.arrayBuffer());
  console.log("[telegram] ← descargarBinario | bytes:", buffer.length);
  return buffer;
}

async function transcribirAudio(bufferAudio, nombreArchivo = "audio.ogg") {
  console.log(
    "[telegram] → transcribirAudio | nombreArchivo:",
    nombreArchivo,
    "| bytes:",
    bufferAudio.length,
  );
  const file = new File([bufferAudio], nombreArchivo, { type: "audio/ogg" });
  const transcription = await openai.audio.transcriptions.create({
    file,
    model: "whisper-1",
    response_format: "verbose_json",
  });
  console.log("[telegram] ← transcribirAudio | texto:", transcription.text);
  return transcription.text || "";
}

async function procesarAudioTelegram(fileId) {
  console.log("[telegram] → procesarAudioTelegram | fileId:", fileId);
  const fileUrl = await obtenerUrlArchivoTelegram(fileId);
  const buffer = await descargarBinario(fileUrl);
  const texto = await transcribirAudio(buffer);
  console.log("[telegram] ← procesarAudioTelegram | texto final:", texto);
  return texto;
}

// ============================================================
// MENSAJES ENLATADOS para tipos no soportados
// ============================================================
function mensajeNoSoportado(tipo) {
  console.log("[telegram] → mensajeNoSoportado | tipo:", tipo);
  const mapa = {
    sticker:
      "😂 Buen sticker, aún no puedo verlos, pero cuéntame qué necesitas 🙌",
    photo:
      "📸 Recibí tu imagen, aún no puedo analizarla, pero dime qué buscas 😊",
    video:
      "🎥 Gracias por el video, aún no puedo verlo, pero cuéntame de qué trata 🙌",
    document:
      "📄 Documento recibido, aún no puedo abrirlo, pero explícame qué necesitas 😊",
    location:
      "📍 Ubicación recibida, aún no puedo procesarla, pero dime qué buscas 🙌",
    contact:
      "👤 Contacto recibido, aún no puedo guardarlo, pero dime en qué te ayudo 😊",
  };
  const texto =
    mapa[tipo] ||
    "Gracias por tu mensaje 🙌 aún no puedo procesar ese tipo de contenido.";
  console.log("[telegram] ← mensajeNoSoportado | texto:", texto);
  return texto;
}

function detectarTipoNoSoportado(mensaje) {
  console.log("[telegram] → detectarTipoNoSoportado");
  let tipo = null;
  if (mensaje.sticker) tipo = "sticker";
  else if (mensaje.photo) tipo = "photo";
  else if (mensaje.video) tipo = "video";
  else if (mensaje.document) tipo = "document";
  else if (mensaje.location) tipo = "location";
  else if (mensaje.contact) tipo = "contact";
  console.log("[telegram] ← detectarTipoNoSoportado | tipo:", tipo);
  return tipo;
}

// ============================================================
// USUARIO / CONTEXTO — colección propia para Telegram, separada de
// cualquier colección que ya tengas para WhatsApp.
// 👈 AJUSTAR el nombre de la colección si ya usas otra en tu proyecto.
// ============================================================
async function obtenerOCrearUsuarioTelegram(chatId, nombreTg) {
  console.log(
    "[telegram] → obtenerOCrearUsuarioTelegram | chatId:",
    chatId,
    "| nombreTg:",
    nombreTg,
  );
  const numero_usuario = `tg_${chatId}`;
  const ref = db
    .collection("usuarios_telegram_dispensador")
    .doc(numero_usuario);
  const snap = await ref.get();

  if (!snap.exists) {
    console.log(
      "[telegram] obtenerOCrearUsuarioTelegram: usuario nuevo, creando:",
      numero_usuario,
    );
    const nuevo = {
      nombre_usuario: nombreTg || "Usuario",
      chat_id: chatId,
      contexto: CONTEXTO_DEFAULT,
      // 👇 Acumulado histórico de tokens gastados por este usuario, para
      // poder ver de un vistazo cuánto ha costado atenderlo en total.
      tokens_totales_historicos: { openai: 0, gemini: 0, total: 0 },
      createdAt: admin.firestore.FieldValue.serverTimestamp(),
      updated_at: admin.firestore.FieldValue.serverTimestamp(),
    };
    await ref.set(nuevo);
    console.log(
      "[telegram] ← obtenerOCrearUsuarioTelegram: usuario creado:",
      numero_usuario,
    );
    return { numero_usuario, ...nuevo, contexto: CONTEXTO_DEFAULT };
  }

  console.log(
    "[telegram] ← obtenerOCrearUsuarioTelegram: usuario existente:",
    numero_usuario,
    "| data:",
    JSON.stringify(snap.data()),
  );
  return { numero_usuario, ...snap.data() };
}

/**
 * Guarda el contexto de la vuelta (tipo/extra para el próximo mensaje) Y
 * los tokens gastados en esta consulta:
 *   - tokens_ultima_consulta: {openai, gemini, total} — SOLO esta consulta.
 *   - tokens_totales_historicos: acumulado de todas las consultas del
 *     usuario desde que existe, para tener el gasto total en OpenAI/Gemini
 *     por usuario a lo largo del tiempo.
 *
 * @param {string} numero_usuario
 * @param {Object} nuevoContexto - { tipo, extra }
 * @param {{openai:number, gemini:number, total:number}} tokensConsulta
 * @param {{openai:number, gemini:number, total:number}} acumuladoPrevio
 */
async function actualizarContextoUsuarioTelegram(
  numero_usuario,
  nuevoContexto,
  tokensConsulta,
  acumuladoPrevio,
) {
  console.log(
    "[telegram] → actualizarContextoUsuarioTelegram | numero_usuario:",
    numero_usuario,
    "| nuevoContexto:",
    JSON.stringify(nuevoContexto),
    "| tokensConsulta:",
    JSON.stringify(tokensConsulta),
  );

  const acumuladoNuevo = {
    openai: (acumuladoPrevio?.openai || 0) + tokensConsulta.openai,
    gemini: (acumuladoPrevio?.gemini || 0) + tokensConsulta.gemini,
    total: (acumuladoPrevio?.total || 0) + tokensConsulta.total,
  };

  const ref = db
    .collection("usuarios_telegram_dispensador")
    .doc(numero_usuario);
  await ref.set(
    {
      contexto: nuevoContexto,
      tokens_ultima_consulta: tokensConsulta,
      tokens_totales_historicos: acumuladoNuevo,
      updated_at: admin.firestore.FieldValue.serverTimestamp(),
    },
    { merge: true },
  );
  console.log(
    "[telegram] ← actualizarContextoUsuarioTelegram: guardado OK | numero_usuario:",
    numero_usuario,
    "| acumulado histórico:",
    JSON.stringify(acumuladoNuevo),
  );
}

// ============================================================
// DESPACHO POR RAMA — llama a la función de cada archivo con la
// misma forma de parámetros ({ mensaje, nombre_usuario, extra_anterior,
// numero_usuario }) y devuelve lo que sea que esa rama entregue
// ({ mensaje, extra, tokens, ... }).
// ============================================================
async function despacharRama(
  rama,
  { mensaje, nombre_usuario, extra_anterior, numero_usuario },
) {
  console.log(
    "[telegram] → despacharRama | rama:",
    rama,
    "| nombre_usuario:",
    nombre_usuario,
    "| extra_anterior:",
    extra_anterior,
    "| mensaje:",
    mensaje,
  );
  const params = { mensaje, nombre_usuario, extra_anterior, numero_usuario };

  // Ramas aún no implementadas: mensaje fijo, sin tocar ninguna IA, sin
  // gasto de tokens.
  if (MENSAJES_RAMA_NO_IMPLEMENTADA[rama]) {
    console.log(
      "[telegram] despacharRama: rama no implementada, usando mensaje fijo:",
      rama,
    );
    return {
      mensaje: MENSAJES_RAMA_NO_IMPLEMENTADA[rama],
      extra: `rama ${rama} aun no implementada`,
      tokens: armarTokens([]),
    };
  }

  let resultado;
  switch (rama) {
    case "general":
      console.log("[telegram] despacharRama: llamando responderGeneral");
      resultado = await responderGeneral(params);
      break;
    case "negocio":
      console.log("[telegram] despacharRama: llamando responderNegocio");
      resultado = await responderNegocio(params);
      break;
    case "carta_visual":
      console.log("[telegram] despacharRama: llamando responderCartaVisual");
      resultado = await responderCartaVisual(params);
      break;
    case "busqueda_algolia":
      console.log(
        "[telegram] despacharRama: llamando responderBusquedaAlgolia",
      );
      resultado = await responderBusquedaAlgolia(params);
      break;
    case "pedidos_carrito":
      console.log("[telegram] despacharRama: llamando responderCarrito");
      resultado = await responderCarrito(params);
      break;
    case "pagos_voucher":
      console.log("[telegram] despacharRama: llamando responderPagos");
      resultado = await responderPagos(params);
      break;
    case "reclamos":
      console.log("[telegram] despacharRama: llamando responderReclamos");
      resultado = await responderReclamos(params);
      break;
    default:
      console.warn("[telegram] despacharRama: rama desconocida:", rama);
      resultado = {
        mensaje: "No entendí bien eso 😅 ¿me lo explicas de otra forma?",
        extra: "rama_desconocida",
        tokens: armarTokens([]),
      };
  }
  console.log(
    "[telegram] ← despacharRama | rama:",
    rama,
    "| tokens de la rama:",
    resultado?.tokens?.total ?? 0,
  );
  return resultado;
}

// ============================================================
// 👇 NUEVO — MANEJO DE callback_query (taps de botón del carrito)
// No pasa por dispensador.js, no pasa por ninguna IA. Va directo a
// Firestore y edita el mensaje del carrito con el resultado.
// ============================================================
async function manejarCallbackCarrito(req, res) {
  const callback = req.body.callback_query;
  const chatId = callback.message?.chat?.id;
  const messageId = callback.message?.message_id;
  const data = callback.data || "";
  const nombreTg = callback.from?.first_name || "Usuario";
  const numero_usuario = `tg_${chatId}`;

  console.log(
    "[telegram] === Callback recibido === | chatId:",
    chatId,
    "| messageId:",
    messageId,
    "| data:",
    data,
    "| de:",
    nombreTg,
  );

  if (!chatId || !messageId) {
    console.warn("[telegram] Callback sin chatId/messageId, ignorando");
    return res
      .status(200)
      .json({ ok: true, info: "callback sin chat/message id" });
  }

  try {
    if (data.startsWith("bus_add:")) {
      const itemId = data.slice("bus_add:".length);
      console.log(
        "[telegram] Callback: agregando producto desde búsqueda | numero_usuario:",
        numero_usuario,
        "| itemId:",
        itemId,
      );
      const carritoActualizado = await agregarItemCarritoPorId(
        numero_usuario,
        itemId,
        "telegram",
      );
      await responderCallbackQueryTelegram(
        callback.id,
        "Agregado a tu carrito 🛒",
      );
      console.log(
        "[telegram] === Callback completado (bus_add) === | chatId:",
        chatId,
        "| items en carrito:",
        carritoActualizado.length,
      );
      return res.status(200).json({ ok: true, carrito: carritoActualizado });
    }

    let carritoActualizado;
    let textoToast = "";

    if (data === "car_vaciar") {
      console.log(
        "[telegram] Callback: vaciando carrito completo | numero_usuario:",
        numero_usuario,
      );
      carritoActualizado = await vaciarCarrito(numero_usuario, {
        canal: "telegram",
      });
      textoToast = "Carrito vaciado 🧹";
    } else if (data.startsWith("car_del:")) {
      const itemId = data.slice("car_del:".length);
      console.log(
        "[telegram] Callback: eliminando item | numero_usuario:",
        numero_usuario,
        "| itemId:",
        itemId,
      );
      carritoActualizado = await eliminarItemCarritoPorId(
        numero_usuario,
        itemId,
        { canal: "telegram" },
      );
      textoToast = "Producto eliminado 🗑️";
    } else {
      console.warn("[telegram] Callback con data no reconocida:", data);
      await responderCallbackQueryTelegram(callback.id, "");
      return res
        .status(200)
        .json({ ok: true, info: "callback_data no reconocido" });
    }

    // Responder el toast de Telegram (obligatorio, si no el botón queda
    // "cargando" en el celular del usuario).
    await responderCallbackQueryTelegram(callback.id, textoToast);

    // Texto y teclado recalculados en base al carrito YA actualizado.
    // Sin IA — 100% código, directo desde Firestore. El token es el mismo
    // de siempre para este usuario (no se regenera en cada tap).
    const token = await obtenerOCrearTokenCarrito(numero_usuario);
    const nuevoTexto = formatearCarritoSinIA(carritoActualizado, nombreTg);
    const nuevosBotones = construirBotonesCarrito(carritoActualizado, {
      token,
    });

    await editarMensajeConTecladoTelegram(
      chatId,
      messageId,
      nuevoTexto,
      nuevosBotones,
    );

    console.log(
      "[telegram] === Callback completado === | chatId:",
      chatId,
      "| items restantes:",
      carritoActualizado.length,
    );

    return res.status(200).json({ ok: true, carrito: carritoActualizado });
  } catch (err) {
    console.error(
      "[telegram] ❌ Error en callback de carrito:",
      err.message,
      "| stack:",
      err.stack,
    );
    await responderCallbackQueryTelegram(
      callback.id,
      "Hubo un problema, intenta de nuevo 🙏",
    ).catch(() => {});
    return res.status(200).json({ ok: false, error: err.message });
  }
}

// ============================================================
// WEBHOOK PRINCIPAL
// ============================================================
exports.dispensador_webhook_telegram = onRequest(
  { concurrency: 20, cpu: 1 },
  async (req, res) => {
    console.log("[telegram] === Nueva petición ===", "| method:", req.method);

    // Telegram no hace handshake GET como WhatsApp; solo responde ok
    // para health checks manuales.
    if (req.method === "GET") {
      console.log("[telegram] GET recibido (health check) → respondiendo ok");
      return res.status(200).send("ok");
    }

    // 👇 NUEVO — los taps de botón llegan como "callback_query", no como
    // "message". Se manejan aparte, ANTES de la lógica normal de
    // texto/audio, y nunca tocan dispensador.js ni ninguna IA.
    if (req.body?.callback_query) {
      console.log(
        "[telegram] Update es un callback_query, desviando a manejarCallbackCarrito",
      );
      return await manejarCallbackCarrito(req, res);
    }

    const inicio = Date.now();
    let chatIdParaError = null; // se setea apenas se conoce, para poder avisar al usuario si algo falla

    try {
      const mensaje = req.body?.message;
      console.log("[telegram] Body recibido:", JSON.stringify(req.body));

      if (!mensaje) {
        console.log("[telegram] Update sin mensaje procesable, saliendo");
        return res
          .status(200)
          .json({ ok: true, info: "Update sin mensaje procesable" });
      }

      const chatId = mensaje.chat.id;
      chatIdParaError = chatId;
      const nombreTg = mensaje.from?.first_name || "Usuario";
      console.log("[telegram] chatId:", chatId, "| nombreTg:", nombreTg);

      // ---- Tipos no soportados (foto, sticker, video, doc, ubicación, contacto) ----
      const tipoNoSoportado = detectarTipoNoSoportado(mensaje);
      if (tipoNoSoportado) {
        console.log(
          "[telegram] Tipo no soportado detectado:",
          tipoNoSoportado,
          "| respondiendo mensaje enlatado",
        );
        await enviarMensajeTelegram(
          chatId,
          mensajeNoSoportado(tipoNoSoportado),
        );
        return res
          .status(200)
          .json({ ok: true, tipo_mensaje: tipoNoSoportado });
      }

      // ---- Resolver texto: audio (voice) o texto plano ----
      let mensajeFinal = "";
      if (mensaje.voice) {
        console.log("[telegram] Mensaje de voz detectado, transcribiendo...");
        mensajeFinal = await procesarAudioTelegram(mensaje.voice.file_id);
      } else if (mensaje.text) {
        console.log("[telegram] Mensaje de texto detectado:", mensaje.text);
        mensajeFinal = mensaje.text;
      } else {
        console.log("[telegram] Tipo de mensaje no manejado, saliendo");
        return res
          .status(200)
          .json({ ok: true, info: "Tipo de mensaje no manejado" });
      }

      if (!mensajeFinal.trim()) {
        console.log("[telegram] Mensaje vacío tras resolución, saliendo");
        return res
          .status(200)
          .json({ ok: true, info: "Mensaje vacío tras resolución" });
      }

      // ---- Usuario + contexto previo (para extra_anterior) ----
      const usuarioInfo = await obtenerOCrearUsuarioTelegram(chatId, nombreTg);
      const contextoUsuario = usuarioInfo.contexto || CONTEXTO_DEFAULT;
      console.log(
        "[telegram] Contexto usuario actual:",
        JSON.stringify(contextoUsuario),
      );

      // ---- Clasificar con el MISMO dispensador que usas en WhatsApp ----
      // 👇 clasificarRama() ahora devuelve {rama, tokens}.
      console.log(
        "[telegram] Clasificando mensaje con clasificarRama... | contexto previo:",
        JSON.stringify(contextoUsuario),
      );
      const { rama, tokens: tokensClasificacion } = await clasificarRama(
        mensajeFinal,
        contextoUsuario,
      );
      console.log(
        "[telegram] Rama clasificada:",
        rama,
        "| tokens clasificación:",
        tokensClasificacion.total,
      );

      // ---- Ejecutar la rama correspondiente ----
      const resultado = await despacharRama(rama, {
        mensaje: mensajeFinal,
        nombre_usuario: nombreTg,
        extra_anterior: contextoUsuario.extra,
        numero_usuario: usuarioInfo.numero_usuario,
      });

      // ---- Lead scoring: se actualiza DESPUÉS de tener el resultado real ----
      await actualizarLeadStatusPorRama({
        canal: "telegram",
        numero_usuario: usuarioInfo.numero_usuario,
        nombre_usuario: nombreTg,
        rama,
        resultadoRama: resultado,
      });

      // ---- TOKENS: sumar clasificación + rama = gasto TOTAL de esta consulta ----
      const tokensConsulta = combinarTokens(
        tokensClasificacion,
        resultado?.tokens,
      );
      const resumenConsulta = resumenPorProveedor(tokensConsulta); // {openai, gemini, total}
      console.log(
        "[telegram] 🔢 TOKENS TOTALES de la consulta | chatId:",
        chatId,
        "| OpenAI:",
        resumenConsulta.openai,
        "| Gemini:",
        resumenConsulta.gemini,
        "| TOTAL:",
        resumenConsulta.total,
      );

      // Si se activó MOSTRAR_TOKENS_AL_USUARIO, se le pega la línea al
      // mensaje ANTES de enviarlo por Telegram (no afecta a los botones ni
      // a las imágenes, solo al texto/caption).
      if (MOSTRAR_TOKENS_AL_USUARIO && resultado?.mensaje) {
        resultado.mensaje = resultado.mensaje + lineaTokens(tokensConsulta);
      }

      // ---- Responder por Telegram: mensaje + imagen/botón/botones si aplica ----
      await enviarRespuestaRama(chatId, resultado);

      // 👇 reclamos.js puede devolver humano:true cuando el caso ya necesita
      // que un asesor lo tome. Por ahora solo se deja registrado; si más
      // adelante quieres notificar a un chat de Telegram interno o a WhatsApp,
      // aquí es el punto exacto para hacerlo.
      if (rama === "reclamos" && resultado?.humano === true) {
        console.warn(
          "⚠️ [reclamos] Caso requiere asesor humano | chatId:",
          chatId,
        );
      }

      // ---- Guardar contexto + tokens para la próxima vuelta / historial ----
      await actualizarContextoUsuarioTelegram(
        usuarioInfo.numero_usuario,
        { tipo: rama, extra: resultado?.extra || "null" },
        resumenConsulta,
        usuarioInfo.tokens_totales_historicos,
      );

      const tiempoTotal = Date.now() - inicio;
      console.log(
        "[telegram] === Petición completada === | chatId:",
        chatId,
        "| rama:",
        rama,
        "| tiempo_ms:",
        tiempoTotal,
      );

      return res.status(200).json({
        ok: true,
        rama,
        mensaje_usuario: mensajeFinal,
        numero_usuario: usuarioInfo.numero_usuario,
        tiempo_ms: tiempoTotal,
        // 👇 Gasto total (OpenAI + Gemini) de ESTA consulta puntual.
        tokens_consulta: resumenConsulta,
      });
    } catch (error) {
      console.error(
        "❌ Error dispensador_webhook_telegram:",
        error.message,
        "| stack:",
        error.stack,
      );

      // Cada rama ya tiene su propio try/catch con mensaje de respaldo, así
      // que llegar aquí significa algo no controlado por ninguna (ej. el
      // doc de negocio.js no existe en Firestore, un require roto, etc).
      // Aun así, nunca dejamos al usuario sin respuesta.
      if (chatIdParaError) {
        console.log(
          "[telegram] Enviando mensaje de error de respaldo | chatId:",
          chatIdParaError,
        );
        enviarMensajeTelegram(
          chatIdParaError,
          "Perdón, tuve un problema respondiéndote. ¿Puedes intentar de nuevo en un momento? 🙏",
        ).catch(() => {});
      }

      return res.status(500).json({ ok: false, error: error.message });
    }
  },
);
