"use strict";

const WHATSAPP_TOKEN = process.env.SCAG_AI_WHATSAP_KEY;
const WHATSAPP_PHONE_NUMBER_ID = process.env.SCAG_WHATSAP_ID;
const WHATSAPP_API_VERSION = "v20.0";

const URL_GUARDAR_CONSULTA_PENDIENTE =
  "https://us-central1-geinzworkapp.cloudfunctions.net/guardarConsultaPendiente";

// ============================================================
// 1) Sanitizar el texto (equivalente a "limpiar_consulta")
// ============================================================
function sanitizeForJSON(text) {
  if (!text) return "";
  return text
    .replace(/\\/g, "") // elimina backslashes de LaTeX (\sec, \tan, etc.)
    .replace(/[\r\n\t]+/g, " ") // saltos de línea/tabs -> espacio
    .replace(/[\u2061\u200B\u200C\u200D\uFEFF]/g, "") // caracteres invisibles
    .replace(/\s+/g, " ") // espacios múltiples -> uno
    .trim();
}

// ============================================================
// 2) Guardar consulta pendiente de TEXTO (equivalente a "guardar en db")
// ============================================================
async function guardarConsultaPendienteTexto({
  alias,
  provider,
  category,
  solutionMode,
  textoLimpio,
}) {
  const resp = await fetch(URL_GUARDAR_CONSULTA_PENDIENTE, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({
      alias,
      provider,
      category,
      solutionMode,
      imageBase64: "",
      mimeType: "",
      textHint: textoLimpio,
    }),
  });

  const data = await resp.json().catch(() => ({}));
  if (!resp.ok) {
    console.error(
      "❌ [guardarConsultaPendienteTexto] Error:",
      resp.status,
      JSON.stringify(data),
    );
    throw new Error("No se pudo guardar la consulta pendiente de texto");
  }
  return data;
}

// ============================================================
// 3) Mensaje aleatorio de confirmación (equivalente a "msje_predeterminados")
// ============================================================
function construirMensajeConfirmacionTexto({
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
    `🤖 ${nombreUser}, detecté una consulta de *texto*. Con tu configuración actual de *${modeloLabel}*, categoría *${categoriaLabel}* y respuesta *${solucionLabel}*, se te gastarán *${creditos_costo} créditos* ¿deseas aceptar?`,
    `💳 ${nombreUser}, esta consulta de *texto* tiene un costo de *${creditos_costo} créditos*, usando *${modeloLabel}* en modo *${solucionLabel}* (categoría: *${categoriaLabel}*). ¿Confirmas que quieres continuar?`,
    `⚙️ Oe ${nombreUser}, procesar este *texto* con *${modeloLabel}* + *${solucionLabel}* te costará *${creditos_costo} créditos* (categoría *${categoriaLabel}*). ¿Le damos play?`,
    `✨ ${nombreUser}, vas a usar *${creditos_costo} créditos* para esta consulta de *texto* (${categoriaLabel}), configurada en *${modeloLabel}* modo *${solucionLabel}*. ¿Lo confirmamos?`,
    `🔎 ${nombreUser}, detecté tu consulta de *texto* — con *${modeloLabel}* y respuesta *${solucionLabel}*, el costo es de *${creditos_costo} créditos*. ¿Avanzamos?`,
    `📌 ${nombreUser}, tu consulta de *texto* con *${modeloLabel}*, categoría *${categoriaLabel}* y respuesta *${solucionLabel}* te costará *${creditos_costo} créditos*. ¿Continuamos?`,
    `🧩 ${nombreUser}, procesaré tu *texto* con *${modeloLabel}* en modo *${solucionLabel}* (categoría: *${categoriaLabel}*), el costo es de *${creditos_costo} créditos*. ¿Aceptas?`,
    `💡 ${nombreUser}, tu panel está configurado con *${modeloLabel}* + *${solucionLabel}*. Esta consulta de *texto* (${categoriaLabel}) costará *${creditos_costo} créditos*. ¿Seguimos?`,
  ];

  return msgs[Math.floor(Math.random() * msgs.length)];
}

// ============================================================
// 4) Enviar botones Sí/No de TEXTO (equivalente a "Enviar Botones Confirmacion Creditos2")
//    OJO: ids SIN sufijo "_imagen" -> tu parsearBotonRespuesta ya
//    interpreta esto como origen: "texto"
// ============================================================
async function enviarBotonesConfirmacionTexto(numero_usuario, mensaje) {
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
          { type: "reply", reply: { id: "aceptar_consulta_si", title: "Sí" } },
          { type: "reply", reply: { id: "aceptar_consulta_no", title: "No" } },
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
        "❌ [enviarBotonesConfirmacionTexto] Error Meta:",
        resp.status,
        JSON.stringify(data),
      );
      return { ok: false, status: resp.status, error: data };
    }
    return { ok: true, data };
  } catch (e) {
    console.error("❌ [enviarBotonesConfirmacionTexto] Excepción:", e.message);
    return { ok: false, error: e.message };
  }
}

module.exports = {
  sanitizeForJSON,
  guardarConsultaPendienteTexto,
  construirMensajeConfirmacionTexto,
  enviarBotonesConfirmacionTexto,
};