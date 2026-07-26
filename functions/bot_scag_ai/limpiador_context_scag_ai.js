// ============================================================
// Equivalente EXACTO al nodo "limpiador_context" (Code) de n8n.
// Toma el texto crudo que devolvió el modelo (item.json.output en n8n) y
// produce { reply, context, audio, parseOk, errorMsg }.
// ============================================================

function limpiarYParsear(textoRaw) {
  if (!textoRaw || typeof textoRaw !== "string") {
    throw new Error("Respuesta vacía o no es texto.");
  }
  let texto = textoRaw.trim();
  texto = texto.replace(/^```json\s*/i, "").replace(/^```\s*/i, "");
  texto = texto.replace(/```\s*$/i, "");
  texto = texto.trim();

  const primerLlave = texto.indexOf("{");
  const ultimaLlave = texto.lastIndexOf("}");
  if (primerLlave === -1 || ultimaLlave === -1 || ultimaLlave < primerLlave) {
    throw new Error("No se encontró un objeto JSON en la respuesta.");
  }
  texto = texto.substring(primerLlave, ultimaLlave + 1);

  try {
    return JSON.parse(texto);
  } catch (e) {}

  // Reparación 1: escapa saltos de línea sueltos dentro de strings
  let textoArreglado = "";
  let dentroDeString = false;
  let anterior = "";
  for (let i = 0; i < texto.length; i++) {
    const c = texto[i];
    if (c === '"' && anterior !== "\\") {
      dentroDeString = !dentroDeString;
    }
    if (dentroDeString && (c === "\n" || c === "\r")) {
      textoArreglado += "\\n";
    } else {
      textoArreglado += c;
    }
    anterior = c;
  }
  try {
    return JSON.parse(textoArreglado);
  } catch (e) {}

  // Reparación 2: quita comas colgantes antes de } o ]
  let textoSinComas = textoArreglado.replace(/,(\s*[}\]])/g, "$1");
  try {
    return JSON.parse(textoSinComas);
  } catch (e) {
    throw new Error("No se pudo parsear el JSON tras limpieza: " + e.message);
  }
}

function limpiarEmojis(texto) {
  if (!texto || typeof texto !== "string") return texto;
  return texto
    .replace(
      /[\u{1F300}-\u{1FAFF}\u{2600}-\u{27BF}\u{1F1E6}-\u{1F1FF}\u{2190}-\u{21FF}\u{2B00}-\u{2BFF}\u{FE0F}\u{200D}]/gu,
      "",
    )
    .replace(/\s{2,}/g, " ")
    .trim();
}

/**
 * @param {object} params
 * @param {string} params.raw - texto crudo del modelo (item.json.output/text/content/message en n8n)
 * @param {string} params.tipoMsjeUsuario - "audio" | "texto" | "foto" — equivalente a
 *   $('tipo_msje').first().json.tipo_mjse (así, con el typo que tiene en tu n8n)
 * @param {*} [params.valorDirecto] - equivalente a $input.first().json.valor (normalmente no aplica en tu flujo)
 * @param {*} [params.contextDirecto] - equivalente a $input.first().json.context (normalmente no aplica)
 */
function limpiarContextoRespuesta({
  raw,
  tipoMsjeUsuario = "",
  valorDirecto,
  contextDirecto,
}) {
  const existeValor =
    valorDirecto !== undefined && valorDirecto !== null && valorDirecto !== "";
  const existeContextInput =
    contextDirecto !== undefined && contextDirecto !== null && contextDirecto !== "";

  const usuarioMandoAudio =
    (tipoMsjeUsuario || "").toString().trim().toLowerCase() === "audio";

  let parsed;
  let parseOk = true;
  let errorMsg = null;
  try {
    parsed = limpiarYParsear(raw);
  } catch (e) {
    parseOk = false;
    errorMsg = e.message;
    parsed = null;
  }

  let reply = "";
  let context = "categoria: OCIO | user:  | respondiste: ";

  if (parseOk && parsed && typeof parsed === "object") {
    reply = typeof parsed.reply === "string" ? parsed.reply.trim() : "";

    if (existeValor && existeContextInput) {
      context = contextDirecto;
    } else {
      const rawContext = typeof parsed.context === "string" ? parsed.context.trim() : "";
      const partes = rawContext.split("|");
      context = `categoria: ${partes[0]?.trim() || "OCIO"} | user: ${
        partes[1]?.trim() || ""
      } | respondiste: ${partes[2]?.trim() || ""}`;
    }
  }

  if (!reply) {
    reply = "Dale, cuéntame de nuevo qué necesitas 🙌";
    parseOk = false;
    errorMsg = errorMsg || "reply vacío tras parseo";
  }

  // ── Determinar si se envía audio: solo posible si la categoría es OCIO ──
  const categoriaMatch = context.match(/categoria:\s*([^|]+)/i);
  const categoria = categoriaMatch ? categoriaMatch[1].trim().toUpperCase() : "";
  const esOcio = categoria === "OCIO";

  // Si el usuario mandó audio y la categoría es OCIO, respondemos siempre en audio.
  // Si no, se mantiene el comportamiento random de siempre (50%).
  const audio = esOcio ? (usuarioMandoAudio ? true : Math.random() < 0.5) : false;

  // ── Si se enviará audio, limpiamos emojis del reply para el TTS ──
  if (audio) {
    reply = limpiarEmojis(reply);
  }

  return {
    reply,
    context,
    audio,
    parseOk,
    errorMsg,
    rawOriginal: parseOk ? undefined : raw,
  };
}

module.exports = { limpiarContextoRespuesta };