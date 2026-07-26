"use strict";

// ============================================================
// Equivalente a: Procesar Respuesta Boton + If Aceptado +
// If Origen Imagen (aceptado) + obtener_estados/obtener_estados1 +
// obtener respuesta img / RESOLVER POR TEXTO + RESPUESTA_FINAL +
// Mensaje Aleatorio No
// ============================================================

const URL_OBTENER_CONSULTA_PENDIENTE =
  "https://us-central1-geinzworkapp.cloudfunctions.net/obtenerConsultaPendiente";

// ⚠️ Esta es la URL que usa tu n8n para vision (Cloud Run, no Cloud Function).
// Verifica que siga siendo la correcta o cámbiala por la de tu función real.
const URL_RESOLVER_IMAGEN =
  "https://screenaiquery-vision-n8n-oixttik5rq-uc.a.run.app";

const URL_RESOLVER_TEXTO =
  "https://us-central1-geinzworkapp.cloudfunctions.net/screenaiQuery_texto_n8n";

// ============================================================
// 1) Parsear el botón presionado (Procesar Respuesta Boton)
// ============================================================
function parsearBotonRespuesta(mensajeWa) {
  const id =
    mensajeWa.interactive?.button_reply?.id ||
    mensajeWa.interactive?.list_reply?.id ||
    "";

  const aceptado = id.startsWith("aceptar_consulta_si");
  const origen = id.endsWith("_imagen") ? "imagen" : "texto";

  return { button_id: id, aceptado, origen };
}

// ============================================================
// 2) Mensaje aleatorio cuando el usuario dice "No" (Mensaje Aleatorio No)
// ============================================================
function obtenerMensajeAleatorioNo() {
  const mensajes = [
    "Listo, no se realizó ningún cargo 👍 Cuando quieras hacer otra consulta, aquí estoy.",
    "Sin problema, no se descontaron créditos. Escríbeme cuando tengas tu próxima consulta.",
    "Entendido, cancelado ✅ Quedo atento para tu siguiente pregunta.",
    "Perfecto, no se procesó nada. Cuéntame en qué más te puedo ayudar.",
    "Ok, consulta cancelada. Cuando quieras intentar de nuevo, aquí estaré.",
    "De acuerdo, no se gastaron créditos. Avísame si necesitas algo más.",
    "Cancelado sin costo 👌 Estoy listo para tu próxima consulta.",
    "Listo, no hubo cargo alguno. ¿En qué más te puedo ayudar?",
    "Sin cargos esta vez. Cuando gustes, hazme tu siguiente pregunta.",
    "Entendido, no se procesó la solicitud. Aquí sigo para lo que necesites.",
    "Ok, todo cancelado 👍 Escríbeme cuando tengas otra consulta.",
    "No hay problema, no se aplicó ningún descuento. Quedo atento.",
    "Cancelado correctamente. Avísame en qué más puedo apoyarte.",
    "Listo, sin costo. Cuando quieras seguimos con otra consulta.",
    "Perfecto, no se tocaron tus créditos. Aquí estoy para lo que sigue.",
    "Entendido, cancelado sin cargo. ¿Tienes otra consulta para mí?",
    "Ok, no se procesó nada esta vez. Dime en qué más te ayudo.",
    "Sin cargo, todo bien 👍 Cuando quieras, hazme tu próxima pregunta.",
    "Cancelado, créditos intactos. Quedo disponible para tu siguiente consulta.",
    "Listo, no se realizó el cobro. Aquí estaré para lo que necesites.",
  ];
  return mensajes[Math.floor(Math.random() * mensajes.length)];
}

// ============================================================
// 3) Obtener la consulta pendiente guardada (obtener_estados / obtener_estados1)
// ============================================================
async function obtenerConsultaPendienteHttp(aliasSinPrefijo) {
  const resp = await fetch(URL_OBTENER_CONSULTA_PENDIENTE, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ alias: aliasSinPrefijo }),
  });

  const data = await resp.json().catch(() => ({}));

  if (!resp.ok || !data.ok) {
    console.error(
      "❌ [obtenerConsultaPendienteHttp] Error:",
      resp.status,
      JSON.stringify(data),
    );
    throw new Error(
      data.error || `No se pudo obtener la consulta pendiente (${resp.status})`,
    );
  }

  return data.data; // { alias, provider, category, solutionMode, imageBase64, mimeType, textHint }
}

// ============================================================
// 4) Resolver la consulta pendiente vía IMAGEN (obtener respuesta img)
// ============================================================
async function resolverConsultaImagen(data) {
  const resp = await fetch(URL_RESOLVER_IMAGEN, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({
      alias: data.alias,
      provider: data.provider,
      category: data.category,
      solutionMode: data.solutionMode,
      imageBase64: data.imageBase64,
      mimeType: data.mimeType || "image/jpeg",
      textHint: "",
    }),
  });

  const respData = await resp.json().catch(() => ({}));

  if (!resp.ok || !respData.ok) {
    console.error(
      "❌ [resolverConsultaImagen] Error:",
      resp.status,
      JSON.stringify(respData),
    );
    throw new Error(respData.error || "Error resolviendo consulta de imagen");
  }

  return respData; // { ok, answer, usage, charged }
}

// ============================================================
// 5) Resolver la consulta pendiente vía TEXTO (RESOLVER POR TEXTO)
// ============================================================
async function resolverConsultaTexto(data) {
  const resp = await fetch(URL_RESOLVER_TEXTO, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({
      alias: data.alias,
      provider: data.provider,
      category: data.category,
      solutionMode: data.solutionMode,
      text: data.textHint,
    }),
  });

  const respData = await resp.json().catch(() => ({}));

  if (!resp.ok || !respData.ok) {
    console.error(
      "❌ [resolverConsultaTexto] Error:",
      resp.status,
      JSON.stringify(respData),
    );
    throw new Error(respData.error || "Error resolviendo consulta de texto");
  }

  return respData; // { ok, answer, usage, charged }
}

module.exports = {
  parsearBotonRespuesta,
  obtenerMensajeAleatorioNo,
  obtenerConsultaPendienteHttp,
  resolverConsultaImagen,
  resolverConsultaTexto,
};