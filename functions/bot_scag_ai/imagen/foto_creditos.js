"use strict";

const WHATSAPP_TOKEN = process.env.SCAG_AI_WHATSAP_KEY;
const WHATSAPP_PHONE_NUMBER_ID = process.env.SCAG_WHATSAP_ID;
const WHATSAPP_API_VERSION = "v20.0";
const GEMINI_API_KEY = process.env.PIRVATE_KEY_GEMINI_APITRABAJO; // ⚠️ ajusta el nombre real de tu env var

// ============================================================
// 1) Descargar imagen de WhatsApp (Download media5 + imagen_whatsap1)
// ============================================================
async function obtenerUrlMediaImagen(mediaId) {
  const url = `https://graph.facebook.com/${WHATSAPP_API_VERSION}/${mediaId}`;
  const resp = await fetch(url, {
    headers: { Authorization: `Bearer ${WHATSAPP_TOKEN}` },
  });
  if (!resp.ok) {
    const body = await resp.text();
    throw new Error(
      `No se pudo obtener URL del media ${mediaId}: ${resp.status} ${body}`,
    );
  }
  const data = await resp.json();
  return { url: data.url, mimeType: data.mime_type || "image/jpeg" };
}

async function descargarImagenBinaria(mediaUrl) {
  const resp = await fetch(mediaUrl, {
    headers: { Authorization: `Bearer ${WHATSAPP_TOKEN}` },
  });
  if (!resp.ok) {
    throw new Error(`No se pudo descargar la imagen: ${resp.status}`);
  }
  const arrayBuffer = await resp.arrayBuffer();
  return Buffer.from(arrayBuffer);
}

// ============================================================
// 2) Analizar con Gemini Vision (equivalente a "Analyze an image")
// ============================================================
async function analizarImagenGemini({ imageBase64, mimeType }) {
  const prompt = `Analiza la imagen. Responde SOLO este JSON, sin markdown:
{"esAcademica": "SI", "descripcion": "ejercicio de derivadas con integrales"}

- "SI": solo si el enunciado/ejercicio/problema es el sujeto PRINCIPAL de la foto (hoja de ejercicios, examen, captura de pregunta, pizarra).
- "NO": todo lo demás (personas, comida, apps, memes, publicidad, mascotas, etc). Si hay código/texto/fórmulas solo de FONDO o decoración y no es lo que se pide resolver, sigue siendo "NO".
- Si hay duda, responde "NO".
- descripcion: máx 5 palabras de lo que ves.`;

  const url = `https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=${GEMINI_API_KEY}`;

  const body = {
    contents: [
      {
        parts: [
          { text: prompt },
          { inline_data: { mime_type: mimeType, data: imageBase64 } },
        ],
      },
    ],
  };

  const resp = await fetch(url, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(body),
  });

  const data = await resp.json();
  if (!resp.ok) {
    console.error(
      "❌ [analizarImagenGemini] Error Gemini:",
      resp.status,
      JSON.stringify(data),
    );
    throw new Error("Error llamando a Gemini Vision");
  }

  return data?.candidates?.[0]?.content?.parts?.[0]?.text || "";
}

// ============================================================
// 3) Parsear respuesta de Gemini (equivalente a "limpiado_respuesta")
// ============================================================
function parsearRespuestaVision(raw) {
  let esAcademica = false;
  let descripcion = "";
  let parseOk = true;

  try {
    let texto = (raw || "").trim();
    texto = texto
      .replace(/^```json\s*/i, "")
      .replace(/^```\s*/i, "")
      .replace(/```\s*$/i, "")
      .trim();

    const inicio = texto.indexOf("{");
    const fin = texto.lastIndexOf("}");
    if (inicio === -1 || fin === -1) throw new Error("No se encontró JSON");
    texto = texto.substring(inicio, fin + 1);

    // arreglar saltos de línea dentro de strings
    let arreglado = "";
    let enString = false;
    let anterior = "";
    for (let i = 0; i < texto.length; i++) {
      const c = texto[i];
      if (c === '"' && anterior !== "\\") enString = !enString;
      arreglado += enString && (c === "\n" || c === "\r") ? "\\n" : c;
      anterior = c;
    }
    arreglado = arreglado.replace(/,(\s*[}\]])/g, "$1");

    const parsed = JSON.parse(arreglado);
    esAcademica = parsed.esAcademica?.toString().toUpperCase() === "SI";
    descripcion =
      typeof parsed.descripcion === "string" ? parsed.descripcion.trim() : "";
  } catch (e) {
    parseOk = false;
    console.error(
      "❌ [parsearRespuestaVision] Error parseando:",
      e.message,
      "| raw:",
      raw,
    );
  }

  return { esAcademica, descripcion, parseOk };
}

// ============================================================
// 4) Mensaje aleatorio de confirmación (msje_predeterminado_visual)
// ============================================================
function construirMensajeConfirmacionCreditos({
  nombre,
  creditos_costo,
  categoria,
  modelo,
  solucion,
}) {
  const MODEL_LABELS = {
    "gemini-flash": "Gemini 2.5 Flash",
    "gemini-pro": "Gemini 2.5 Pro",
    "gpt-4o": "GPT-4o",
    "gpt-4o-mini": "GPT-4o Mini",
  };
  const SOLUTION_LABELS = {
    detallado: "Detallada 🧠",
    directo: "Directa ⚡",
    super_detallado: "Super Detallada 🚀",
  };

  const modeloLabel = MODEL_LABELS[modelo] || modelo;
  const solucionLabel = SOLUTION_LABELS[solucion] || solucion;
  const categoriaLabel = categoria || "General";
  const nombreUser = nombre || "crack";

  const msgs = [
    `🤖 ${nombreUser}, detecté una consulta de *imagen*. Con tu configuración actual de *${modeloLabel}*, categoría *${categoriaLabel}* y respuesta *${solucionLabel}*, se te gastarán *${creditos_costo} créditos* ¿deseas aceptar?`,
    `💳 ${nombreUser}, esta consulta de *imagen* tiene un costo de *${creditos_costo} créditos*, usando *${modeloLabel}* en modo *${solucionLabel}* (categoría: *${categoriaLabel}*). ¿Confirmas que quieres continuar?`,
    `⚙️ Oe ${nombreUser}, procesar esta *imagen* con *${modeloLabel}* + *${solucionLabel}* te costará *${creditos_costo} créditos* (categoría *${categoriaLabel}*). ¿Le damos play?`,
    `✨ ${nombreUser}, vas a usar *${creditos_costo} créditos* para esta consulta de *imagen* (${categoriaLabel}), configurada en *${modeloLabel}* modo *${solucionLabel}*. ¿Lo confirmamos?`,
    `🔎 ${nombreUser}, detecté tu consulta de *imagen* — con *${modeloLabel}* y respuesta *${solucionLabel}*, el costo es de *${creditos_costo} créditos*. ¿Avanzamos?`,
    `📌 ${nombreUser}, tu consulta de *imagen* con *${modeloLabel}*, categoría *${categoriaLabel}* y respuesta *${solucionLabel}* te costará *${creditos_costo} créditos*. ¿Continuamos?`,
    `🧩 ${nombreUser}, procesaré tu *imagen* con *${modeloLabel}* en modo *${solucionLabel}* (categoría: *${categoriaLabel}*), el costo es de *${creditos_costo} créditos*. ¿Aceptas?`,
    `💡 ${nombreUser}, tu panel está configurado con *${modeloLabel}* + *${solucionLabel}*. Esta consulta de *imagen* (${categoriaLabel}) costará *${creditos_costo} créditos*. ¿Seguimos?`,
  ];

  return msgs[Math.floor(Math.random() * msgs.length)];
}

// ============================================================
// 5) Enviar botones Sí/No (Enviar Botones Confirmacion Creditos Imagen1)
// ============================================================
async function enviarBotonesConfirmacionImagen(numero_usuario, mensaje) {
  const url = `https://graph.facebook.com/${WHATSAPP_API_VERSION}/${WHATSAPP_PHONE_NUMBER_ID}/messages`;

  const body = {
    messaging_product: "whatsapp",
    to: numero_usuario,
    type: "interactive",
    interactive: {
      type: "button",
      body: { text: mensaje },
      action: {
        buttons: [
          { type: "reply", reply: { id: "aceptar_consulta_si_imagen", title: "Sí" } },
          { type: "reply", reply: { id: "aceptar_consulta_no_imagen", title: "No" } },
        ],
      },
    },
  };

  try {
    const resp = await fetch(url, {
      method: "POST",
      headers: {
        Authorization: `Bearer ${WHATSAPP_TOKEN}`,
        "Content-Type": "application/json",
      },
      body: JSON.stringify(body),
    });
    const data = await resp.json();
    if (!resp.ok) {
      console.error(
        "❌ [enviarBotonesConfirmacionImagen] Error Meta:",
        resp.status,
        JSON.stringify(data),
      );
      return { ok: false, status: resp.status, error: data };
    }
    return { ok: true, data };
  } catch (e) {
    console.error(
      "❌ [enviarBotonesConfirmacionImagen] Excepción:",
      e.message,
    );
    return { ok: false, error: e.message };
  }
}

// ============================================================
// 6) Guardar consulta pendiente (guardar en db3)
//    Llama al mismo endpoint que ya usas en n8n.
// ============================================================
async function guardarConsultaPendiente({
  alias,
  provider,
  category,
  solutionMode,
  imageBase64,
  mimeType,
}) {
  const url =
    "https://us-central1-geinzworkapp.cloudfunctions.net/guardarConsultaPendiente";
  const resp = await fetch(url, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({
      alias,
      provider,
      category,
      solutionMode,
      imageBase64,
      mimeType,
      textHint: "",
    }),
  });
  const data = await resp.json().catch(() => ({}));
  if (!resp.ok) {
    console.error(
      "❌ [guardarConsultaPendiente] Error:",
      resp.status,
      JSON.stringify(data),
    );
    throw new Error("No se pudo guardar la consulta pendiente");
  }
  return data;
}

module.exports = {
  obtenerUrlMediaImagen,
  descargarImagenBinaria,
  analizarImagenGemini,
  parsearRespuestaVision,
  construirMensajeConfirmacionCreditos,
  enviarBotonesConfirmacionImagen,
  guardarConsultaPendiente,
};