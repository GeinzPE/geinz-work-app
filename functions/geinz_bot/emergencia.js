const { onRequest } = require("firebase-functions/v2/https");
const admin = require("firebase-admin");
const algoliasearch = require("algoliasearch");
const OpenAI = require("openai");

const { guardarMensajeHistorial } = require("../historial_whatsapp.js");

if (!admin.apps.length) {
  admin.initializeApp();
}

const db = admin.firestore();

const openai = new OpenAI({
  apiKey: process.env.API_KEYO_OPEN_IA,
});

const APP_ID = process.env.ALGOLIA_APP_ID || "";
const API_KEY = process.env.ALGOLIA_API_KEY || "";
const client = algoliasearch(APP_ID, API_KEY);
const index = client.initIndex("lugares");

const GEMINIKEY = process.env.PRIVATEKEY_GEMINI;
const GEMINI_URL =
  "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent";

const WHATSAPP_TOKEN = process.env.ID_API_WHATSAPP;
const WHATSAPP_PHONE_NUMBER_ID = process.env.ID_NUMBER_WHATSAPP;
const WHATSAPP_API_VERSION = "v20.0";

// ============================================================
// 👇 NUEVO: config de Telegram (mismo patrón que WhatsApp arriba)
// ============================================================
const TELEGRAM_TOKEN = process.env.TELEGRAM_BOT_TOKEN;
const TELEGRAM_API_URL = `https://api.telegram.org/bot${TELEGRAM_TOKEN}`;

const HITS_PER_PAGE_EMERGENCIA_AMPLIO = 60;

// ============================================================================
// ============================================================================

function construirPromptEmergencia(mensaje) {
  return `Clasifica el mensaje en una sola palabra: SALUD o SEGURIDAD.

SALUD: hospital, clínica, ambulancia, accidente, herido, enfermedad, dolor, sangre, parto, desmayo, intoxicación, farmacia, emergencia médica.
SEGURIDAD: robo, asalto, delincuencia, policía, comisaría, bomberos, incendio, disparo, pelea, amenaza, extorsión, serenazgo, ladrón.

Si el mensaje menciona directamente una palabra de la lista SALUD (por ejemplo "hospital"), responde SALUD sin dudar, sin importar el resto del contexto.
Si el mensaje menciona directamente una palabra de la lista SEGURIDAD, responde SEGURIDAD sin dudar.

Responde ÚNICAMENTE con esa palabra, sin explicaciones, sin puntos, sin comillas, sin markdown.

MENSAJE: "${mensaje}"`;
}

function construirSystemPromptSelector(entidadesLigeras, nombreUsuario) {
  return `Eres el selector de contactos de Geinz. Tu única función es encontrar la entidad que el usuario solicita o la que mejor pueda ayudarlo.

ENTIDADES DISPONIBLES (ID y Nombre):

${JSON.stringify(entidadesLigeras)}

INSTRUCCIONES DE SELECCIÓN:
1. Si el usuario menciona un nombre que está en la Data (ej: "Divpol", "Serenazgo", "Hospital"), selecciona ESE ID sin dudar.
2. Si el usuario describe una situación (ej: "me robaron"), selecciona la entidad de seguridad más cercana.
3. Bajo ninguna circunstancia respondas con un ID vacío si hay datos disponibles.

REGLAS DE RESPUESTA:
- NUNCA DECIR "Te conectaremos con ellos"
- Tono: Calmado y directo para ${nombreUsuario}.
- Longitud: Máximo 2 líneas.
- Formato: JSON ESTRICTO.
- NUNCA digas frases como "mensaje predeterminado", "mensaje genérico", "esto es automático" ni nada que describa la naturaleza de tu propia respuesta


{
  "id": "ID_DEL_CONTACTO_ELEGIDO siempre de la lista de entidades sin inventar ni acrotar nada",
  "mensaje": "Texto de apoyo con el nombre de la entidad seleccionada y el nombre",
  "intencion": "EMERGENCIA",
  "estado": "AYUDA_EMERGENCIA"
}`;
}

function construirEntidadesLigeras(data) {
  return data.map((d) => ({ id: d.id, n: d.n }));
}

function validarClasificacion(textoRaw) {
  const limpio = (textoRaw || "")
    .toUpperCase()
    .normalize("NFD")
    .replace(/[\u0300-\u036f]/g, "")
    .trim();

  if (limpio.includes("SALUD")) return "salud";
  if (limpio.includes("SEGURIDAD")) return "seguridad";

  return null;
}

async function clasificarMensaje(mensaje, intento = 1) {
  const MAX_INTENTOS = 2;

  console.log(
    `🟡 [clasificarMensaje] INICIO (intento ${intento}) | mensaje:`,
    mensaje,
  );

  const t0 = Date.now();
  const promptEmergencia = construirPromptEmergencia(mensaje);

  const completion = await openai.chat.completions.create({
    model: "gpt-5.4-nano",
    messages: [{ role: "user", content: promptEmergencia }],
    max_completion_tokens: 10,
  });
  const tiempoMs = Date.now() - t0;

  const rawTexto = completion.choices[0]?.message?.content || "";
  console.log("🧠 RAW IA:", rawTexto);

  const categoriaValidada = validarClasificacion(rawTexto);

  const usage = {
    tiempo_ms: tiempoMs,
    prompt_tokens: completion.usage?.prompt_tokens ?? null,
    completion_tokens: completion.usage?.completion_tokens ?? null,
    total_tokens: completion.usage?.total_tokens ?? null,
  };

  console.log("⏱️ OpenAI:", usage);

  if (categoriaValidada === null && intento < MAX_INTENTOS) {
    console.log(
      `⚠️ [clasificarMensaje] Respuesta no reconocida ("${rawTexto}"), reintentando...`,
    );
    const reintento = await clasificarMensaje(mensaje, intento + 1);
    return {
      categoria: reintento.categoria,
      usage: {
        tiempo_ms: usage.tiempo_ms + reintento.usage.tiempo_ms,
        prompt_tokens:
          (usage.prompt_tokens || 0) + (reintento.usage.prompt_tokens || 0),
        completion_tokens:
          (usage.completion_tokens || 0) +
          (reintento.usage.completion_tokens || 0),
        total_tokens:
          (usage.total_tokens || 0) + (reintento.usage.total_tokens || 0),
        intentos: (reintento.usage.intentos || 1) + 1,
      },
    };
  }

  const categoria = categoriaValidada || "general";

  console.log(
    "🚨 CLASIFICACION EMERGENCIA:",
    categoria,
    "| intentos usados:",
    intento,
  );
  console.log(
    "🟢 [clasificarMensaje] FIN | categoria:",
    categoria,
    "| tiempo_ms:",
    tiempoMs,
  );

  return { categoria, usage: { ...usage, intentos: intento } };
}

async function buscarLugaresAmplio(localidad) {
  const t0 = Date.now();

  let filtersArray = [];
  if (localidad) filtersArray.push(`lugar:"${localidad}"`);

  const filters =
    filtersArray.length > 0 ? filtersArray.join(" AND ") : undefined;

  console.log(
    `🚀 [buscarLugaresAmplio] Buscando "${localidad || ""}" en paralelo con IA clasificadora (hitsPerPage: ${HITS_PER_PAGE_EMERGENCIA_AMPLIO})`,
  );

  const result = await index.search("", {
    filters,
    hitsPerPage: HITS_PER_PAGE_EMERGENCIA_AMPLIO,
  });

  const tiempoMs = Date.now() - t0;
  console.log(
    `✅ [buscarLugaresAmplio] ${result.hits.length} hits amplios | tiempo_ms:`,
    tiempoMs,
  );

  return { hits: result.hits, usage: { tiempo_ms: tiempoMs } };
}

async function buscarLugares(localidad, categoria, preHits) {
  console.log(
    "🟡 [buscarLugares] INICIO | localidad:",
    localidad,
    "| categoria:",
    categoria,
  );

  const t0 = Date.now();

  let hitsCrudos;

  if (Array.isArray(preHits)) {
    // 👇 Ya tenemos los hits (vinieron de la búsqueda amplia en paralelo).
    // Solo falta filtrar por categoria en memoria, sin tocar Algolia de nuevo.
    console.log(
      `♻️ [buscarLugares] Usando ${preHits.length} hits precargados (paralelo), filtrando por categoria en memoria`,
    );

    const categoriaLimpia =
      categoria && categoria !== "general"
        ? categoria.toLowerCase().trim()
        : null;

    hitsCrudos = categoriaLimpia
      ? preHits.filter(
          (h) => (h.categoria || "").toLowerCase().trim() === categoriaLimpia,
        )
      : preHits;

    console.log(
      `🔎 [buscarLugares] ${hitsCrudos.length} hits tras filtrar por categoria "${categoria || "(ninguna)"}"`,
    );
  } else {
    // Camino original: sin búsqueda paralela previa — Algolia filtra
    // directo por localidad + categoria, tal como antes.
    let filtersArray = [];
    if (localidad) filtersArray.push(`lugar:"${localidad}"`);
    if (categoria && categoria !== "general")
      filtersArray.push(`categoria:"${categoria}"`);

    const filters =
      filtersArray.length > 0 ? filtersArray.join(" AND ") : undefined;

    console.log(
      "🔎 [buscarLugares] filtros Algolia:",
      filters || "(sin filtro)",
    );

    const result = await index.search("", { filters, hitsPerPage: 20 });
    hitsCrudos = result.hits;
  }

  console.log("📦 [buscarLugares] TOTAL HITS CRUDOS:", hitsCrudos.length);

  const data = hitsCrudos.map((d) => {
    let ubicacion = null;
    if (
      d.ubicacion &&
      d.ubicacion.latitud != null &&
      d.ubicacion.longitud != null
    ) {
      ubicacion = { lat: d.ubicacion.latitud, lng: d.ubicacion.longitud };
    }

    return {
      id: d.id ?? d.objectID,
      c: d.categoria ?? null,
      n: d.nombre ?? null,
      num: {
        llamada: d.llamada ? [d.llamada] : [],
        whatsapp: d.whatsapp ? [d.whatsapp] : [],
      },
      dir: d.dir ?? null,
      ref: d.ref ?? null,
      ...(ubicacion && { ub: ubicacion }),
    };
  });

  const tiempoMs = Date.now() - t0;
  console.log("⏱️ Algolia:", { tiempo_ms: tiempoMs, resultados: data.length });

  return { data, usage: { tiempo_ms: tiempoMs } };
}

async function seleccionarContacto(
  entidadesLigeras,
  mensajeUsuario,
  nombreUsuario,
) {
  console.log(
    "🟡 [seleccionarContacto] INICIO | entidades disponibles:",
    entidadesLigeras.length,
  );
  console.log(
    "📤 [seleccionarContacto] ENTIDADES ENVIADAS A GEMINI (solo id+n):",
    JSON.stringify(entidadesLigeras),
  );

  const t0 = Date.now();
  const systemMessage = construirSystemPromptSelector(
    entidadesLigeras,
    nombreUsuario,
  );

  const body = {
    contents: [{ role: "user", parts: [{ text: mensajeUsuario }] }],
    systemInstruction: { parts: [{ text: systemMessage }] },
    generationConfig: {
      temperature: 0.2,
      maxOutputTokens: 200,
      thinkingConfig: { thinkingBudget: 0 },
    },
  };

  const resp = await fetch(`${GEMINI_URL}?key=${GEMINIKEY}`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(body),
  });

  const json = await resp.json();
  const tiempoMs = Date.now() - t0;

  const rawText = json.candidates?.[0]?.content?.parts?.[0]?.text || "";
  console.log("🤖 RAW Gemini:", rawText);

  let outputIA;
  try {
    outputIA = JSON.parse(rawText.replace(/```json|```/g, "").trim());
  } catch (e) {
    console.log("❌ [seleccionarContacto] ERROR parseando Gemini:", e.message);
    throw new Error("No se pudo parsear la respuesta de Gemini: " + rawText);
  }

  const usage = {
    tiempo_ms: tiempoMs,
    prompt_tokens: json.usageMetadata?.promptTokenCount ?? null,
    completion_tokens: json.usageMetadata?.candidatesTokenCount ?? null,
    total_tokens: json.usageMetadata?.totalTokenCount ?? null,
  };

  console.log("⏱️ Gemini:", usage);
  console.log(
    "🟢 [seleccionarContacto] FIN | outputIA:",
    JSON.stringify(outputIA),
  );

  return { outputIA, usage };
}

function construirRespuestaFinal(outputIA, data) {
  console.log(
    "🟡 [construirRespuestaFinal] INICIO | buscando id:",
    outputIA.id,
    "entre",
    data.length,
    "contactos",
  );

  const contacto = data.find((c) => c.id === outputIA.id);

  if (!contacto) {
    console.log(
      "❌ [construirRespuestaFinal] NO SE ENCONTRÓ la entidad con id:",
      outputIA.id,
    );
    return { error: "No se encontró la entidad", tiene_link: false };
  }

  console.log(
    "✅ [construirRespuestaFinal] CONTACTO ENCONTRADO:",
    JSON.stringify(contacto),
  );

  const listaLlamadas = contacto.num?.llamada || [];
  const listaWhatsapp = contacto.num?.whatsapp?.[0]
    ? contacto.num.whatsapp
    : [];

  const bloqueContactos =
    listaLlamadas.length && listaWhatsapp.length
      ? `📞 Contáctalos al: ${listaLlamadas.join(" - ")} o 💬 Escríbeles al: ${listaWhatsapp.join(" - ")}`
      : listaLlamadas.length
        ? `📞 Contáctalos al: ${listaLlamadas.join(" - ")}`
        : listaWhatsapp.length
          ? `💬 Escríbeles al: ${listaWhatsapp.join(" - ")}`
          : "";

  const lat = contacto.ub?.lat || null;
  const lng = contacto.ub?.lng || null;
  const tiene_link = !!(lat && lng);
  const direccion = contacto.dir || "";
  const referencia = contacto.ref || "";
  const mensajeBase = String(outputIA.mensaje).trim();
  const base = {
    id: outputIA.id,
    intencion: outputIA.intencion,
    estado: outputIA.estado,
    tiene_link,
    telefonos: listaLlamadas,
    whatsapp: listaWhatsapp,
  };

  const resultadoFinal = tiene_link
    ? {
        ...base,
        mensaje_texto:
          `${mensajeBase} ubicalos en *${direccion}* ,con referencia *${referencia ? `(${referencia})* , ` : ""}`.trim(),
        lat,
        lng,
      }
    : {
        ...base,
        mensaje_safe:
          `${mensajeBase} ${bloqueContactos} 🏠 ${direccion} ${referencia ? `💡 ${referencia}` : ""} ✅`.trim(),
      };

  console.log(
    "🟢 [construirRespuestaFinal] FIN | resultado:",
    JSON.stringify(resultadoFinal),
  );

  return resultadoFinal;
}

// ============================================================
// ENVÍO — WHATSAPP (sin cambios respecto al original)
// ============================================================

async function enviarPlantillaEmergencia(recipientPhoneNumber, resultado) {
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

  const url = `https://graph.facebook.com/${WHATSAPP_API_VERSION}/${WHATSAPP_PHONE_NUMBER_ID}/messages`;

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

  console.log("📤 [enviarPlantillaEmergencia] BODY:", JSON.stringify(body));

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
    console.log("❌ [enviarPlantillaEmergencia] ERROR:", resp.status, errText);
    throw new Error(
      `Error enviando plantilla emergencia: ${resp.status} ${errText}`,
    );
  }

  console.log("✅ [enviarPlantillaEmergencia] Plantilla enviada OK");
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

async function enviarMensajeTextoEmergencia(recipientPhoneNumber, resultado) {
  const url = `https://graph.facebook.com/${WHATSAPP_API_VERSION}/${WHATSAPP_PHONE_NUMBER_ID}/messages`;

  const body = {
    messaging_product: "whatsapp",
    to: recipientPhoneNumber,
    type: "text",
    text: { body: resultado.mensaje_safe },
  };

  console.log("📤 [enviarMensajeTextoEmergencia] BODY:", JSON.stringify(body));

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
    console.log(
      "❌ [enviarMensajeTextoEmergencia] ERROR:",
      resp.status,
      errText,
    );
    throw new Error(
      `Error enviando mensaje texto emergencia: ${resp.status} ${errText}`,
    );
  }

  console.log("✅ [enviarMensajeTextoEmergencia] Mensaje enviado OK");
  guardarMensajeHistorial({
    canal: "whatsapp",
    numero_usuario: recipientPhoneNumber,
    remitente: "bot",
    tipo: "texto",
    contenido: resultado.mensaje_safe || "",
  }).catch(() => {});
  return resp.json();
}

// ============================================================
// 👇 NUEVO — ENVÍO POR TELEGRAM
// Telegram no exige plantillas pre-aprobadas ni ventana de 24h, así
// que aquí es más simple: texto libre con sendMessage, y ubicación
// nativa con sendLocation (Telegram no permite caption en
// sendLocation, por eso el texto de apoyo va como mensaje aparte).
// ============================================================

// ============================================================
// 👇 MODIFICADO — ahora acepta un teclado inline opcional (botones)
// ============================================================
async function enviarMensajeTextoTelegram(chat_id, texto, replyMarkup = null) {
  const url = `${TELEGRAM_API_URL}/sendMessage`;

  const chatIdReal = String(chat_id).startsWith("tg_")
    ? String(chat_id).replace(/^tg_/, "")
    : chat_id;

  const body = {
    chat_id: chatIdReal,
    text: texto,
    parse_mode: "HTML",
  };

  if (replyMarkup) {
    body.reply_markup = replyMarkup;
  }

  const resp = await fetch(url, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(body),
  });

  if (!resp.ok) {
    const errText = await resp.text();
    console.log("❌ [enviarMensajeTextoTelegram] ERROR:", resp.status, errText);
    throw new Error(
      `Error enviando mensaje texto Telegram: ${resp.status} ${errText}`,
    );
  }

  console.log("✅ [enviarMensajeTextoTelegram] Mensaje enviado OK");
  guardarMensajeHistorial({
    canal: "telegram",
    numero_usuario: String(chat_id),
    remitente: "bot",
    tipo: "texto",
    contenido: texto || "",
    extra: { canal: "telegram" },
  }).catch(() => {});

  return resp.json();
}

async function enviarUbicacionTelegram(chat_id, lat, lng, caption) {
  const urlLoc = `${TELEGRAM_API_URL}/sendLocation`;

  // Mismo fix: limpiar el prefijo "tg_" antes de llamar a la API de Telegram.
  const chatIdReal = String(chat_id).startsWith("tg_")
    ? String(chat_id).replace(/^tg_/, "")
    : chat_id;

  console.log("📤 [enviarUbicacionTelegram] lat:", lat, "| lng:", lng);

  const respLoc = await fetch(urlLoc, {
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
    console.log("❌ [enviarUbicacionTelegram] ERROR:", respLoc.status, errText);
    throw new Error(
      `Error enviando ubicación Telegram: ${respLoc.status} ${errText}`,
    );
  }

  console.log("✅ [enviarUbicacionTelegram] Pin de ubicación enviado OK");

  // El texto de apoyo (mensaje_texto) va como mensaje de texto aparte,
  // ya que sendLocation no acepta caption.
  if (caption) {
    await enviarMensajeTextoTelegram(chat_id, caption);
  }

  guardarMensajeHistorial({
    numero_usuario: String(chat_id),
    remitente: "bot",
    tipo: "ubicacion",
    contenido: caption || "",
    extra: { canal: "telegram", lat, lng },
  }).catch(() => {});

  return { ok: true };
}
// ============================================================
// SWITCH CENTRAL — decide WhatsApp vs Telegram, y dentro de cada
// canal decide con-ubicación vs sin-ubicación (mismo criterio de
// siempre: resultado.tiene_link)
// ============================================================

function construirMensajeEmergenciaTelegram(resultado) {
  let mensaje = resultado.mensaje_texto || resultado.mensaje_safe || "";

  if (resultado.telefonos?.length) {
    mensaje += `\n📞 Llama al: ${resultado.telefonos.join(" / ")}`;
  }

  if (resultado.whatsapp?.length) {
    mensaje += `\n💬 WhatsApp: ${resultado.whatsapp.join(" / ")}`;
  }

  return mensaje;
}

// ============================================================
// 👇 MODIFICADO — si hay ubicación, arma el botón "Crear ruta"
// apuntando a Google Maps y lo manda junto al texto
// ============================================================
async function enviarRespuestaEmergencia(
  recipientId,
  resultado,
  canal = "whatsapp",
) {
  if (canal === "telegram") {
    console.log("🔀 [enviarRespuestaEmergencia] Telegram + texto con botón");
    const texto = construirMensajeEmergenciaTelegram(resultado);

    let replyMarkup = null;
    if (resultado.tiene_link && resultado.lat && resultado.lng) {
      const mapsUrl = `https://www.google.com/maps?q=${resultado.lat},${resultado.lng}`;
      replyMarkup = {
        inline_keyboard: [[{ text: "🗺️ Crear ruta", url: mapsUrl }]],
      };
    }

    return enviarMensajeTextoTelegram(recipientId, texto, replyMarkup);
  }

  // ---- Rama WhatsApp (sin cambios) ----
  if (resultado.tiene_link) {
    console.log("🔀 [enviarRespuestaEmergencia] WhatsApp + plantilla");
    return enviarPlantillaEmergencia(recipientId, resultado);
  } else {
    console.log("🔀 [enviarRespuestaEmergencia] WhatsApp + texto plano");
    return enviarMensajeTextoEmergencia(recipientId, resultado);
  }
}
exports.obtener_lugares_emergencia_Actualizado = onRequest(async (req, res) => {
  const tInicio = Date.now();
  try {
    const { localidad, mensaje, nombreUsuario, numero_usuario, canal } =
      req.body; // 👈 se agregó "canal" al destructuring

    console.log(
      "🚀 [obtener_lugares_emergencia] REQUEST BODY:",
      JSON.stringify(req.body),
    );

    if (!mensaje || typeof mensaje !== "string" || !mensaje.trim()) {
      console.log("❌ falta el campo 'mensaje'");
      return res
        .status(400)
        .json({ ok: false, error: "El campo 'mensaje' es requerido" });
    }

    if (!numero_usuario) {
      console.log("❌ falta el campo 'numero_usuario'");
      return res.status(400).json({
        ok: false,
        error:
          "El campo 'numero_usuario' es requerido para enviar la respuesta",
      });
    }

    // ============================================================
    // 1) PARALELIZACIÓN: la IA clasificadora (SALUD/SEGURIDAD) y la
    // búsqueda amplia en Algolia (solo por localidad) corren AL MISMO
    // TIEMPO con Promise.all, en vez de esperar a que la IA termine para
    // recién ahí tocar Algolia. La búsqueda amplia solo depende de
    // "localidad" (ya se conoce desde el inicio), así que no hay ningún
    // dato inventado ni adelantado que dependa de la respuesta de la IA.
    // ============================================================
    const [
      { categoria, usage: usageOpenAI },
      { hits: hitsAmplios, usage: usageAlgoliaAmplio },
    ] = await Promise.all([
      clasificarMensaje(mensaje),
      buscarLugaresAmplio(localidad),
    ]);

    // 2) Filtrado en memoria por categoria (sin nueva consulta a Algolia)
    const { data, usage: usageAlgoliaFiltrado } = await buscarLugares(
      localidad,
      categoria,
      hitsAmplios,
    );

    console.log("📊 DATA FILTRADA POR CATEGORIA:", data.length, "resultados");

    // 3) A Gemini SOLO le llega { id, n } de cada entidad
    const entidadesLigeras = construirEntidadesLigeras(data);
    const { outputIA, usage: usageGemini } = await seleccionarContacto(
      entidadesLigeras,
      mensaje,
      nombreUsuario || "usuario",
    );

    // 4) Armar la respuesta final (con o sin link)
    const resultado = construirRespuestaFinal(outputIA, data);

    // 5) Enviar por el canal correspondiente (WhatsApp o Telegram)
    if (!resultado.error) {
      await enviarRespuestaEmergencia(
        numero_usuario,
        resultado,
        canal || "whatsapp", // 👈 default a whatsapp si no viene el campo
      );
    } else {
      console.log(
        "⚠️ No se envía el mensaje porque hubo error armando el resultado:",
        resultado.error,
      );
    }

    const tiempoTotalMs = Date.now() - tInicio;

    const debugInfo = {
      tiempo_total_ms: tiempoTotalMs,
      categoria_detectada: categoria,
      canal: canal || "whatsapp",
      openai: usageOpenAI,
      algolia: usageAlgoliaAmplio,
      algolia_filtrado_local: usageAlgoliaFiltrado,
      gemini: usageGemini,
    };

    console.log("📊 RESUMEN TIEMPOS/TOKENS:", JSON.stringify(debugInfo));

    res.set("Cache-Control", "public, max-age=300");
    return res.status(200).json({ ...resultado, _debug: debugInfo });
  } catch (error) {
    console.error("❌ ERROR obtener_lugares_emergencia:", error.message);
    return res
      .status(500)
      .json({ ok: false, mensaje: "Error interno al buscar lugares" });
  }
});

async function procesarEmergencia({
  localidad,
  mensaje,
  nombreUsuario,
  numero_usuario,
  canal = "whatsapp", // 👈 NUEVO — "whatsapp" | "telegram", con default a whatsapp
  // para no romper las llamadas existentes desde geinz_webhook_principal.js
}) {
  const tInicio = Date.now();

  console.log(
    "🚀 [procesarEmergencia] INICIO | mensaje:",
    mensaje,
    "| numero_usuario:",
    numero_usuario,
    "| canal:",
    canal,
  );

  // ============================================================
  // 1) PARALELIZACIÓN: mismo cambio que en el endpoint HTTP — la IA
  // clasificadora y la búsqueda amplia por localidad corren juntas.
  // ============================================================
  const [
    { categoria, usage: usageOpenAI },
    { hits: hitsAmplios, usage: usageAlgoliaAmplio },
  ] = await Promise.all([
    clasificarMensaje(mensaje),
    buscarLugaresAmplio(localidad),
  ]);

  const { data, usage: usageAlgoliaFiltrado } = await buscarLugares(
    localidad,
    categoria,
    hitsAmplios,
  );

  console.log(
    "📊 [procesarEmergencia] DATA FILTRADA POR CATEGORIA:",
    data.length,
    "resultados",
  );

  const entidadesLigeras = construirEntidadesLigeras(data);
  const { outputIA, usage: usageGemini } = await seleccionarContacto(
    entidadesLigeras,
    mensaje,
    nombreUsuario || "usuario",
  );

  const resultado = construirRespuestaFinal(outputIA, data);

  if (!resultado.error) {
    await enviarRespuestaEmergencia(numero_usuario, resultado, canal);
  } else {
    console.log(
      "⚠️ [procesarEmergencia] No se envía el mensaje, hubo error:",
      resultado.error,
    );
  }

  const tiempoTotalMs = Date.now() - tInicio;

  const debugInfo = {
    tiempo_total_ms: tiempoTotalMs,
    categoria_detectada: categoria,
    canal,
    openai: usageOpenAI,
    algolia: usageAlgoliaAmplio,
    algolia_filtrado_local: usageAlgoliaFiltrado,
    gemini: usageGemini,
  };

  console.log("📊 [procesarEmergencia] RESUMEN:", JSON.stringify(debugInfo));

  return { ...resultado, _debug: debugInfo };
}
exports.procesarEmergencia = procesarEmergencia;
