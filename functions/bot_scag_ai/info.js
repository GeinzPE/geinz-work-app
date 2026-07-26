const { llamarGemini, parsearRespuestaAgente } = require("./geminin_client_scag_ai");
const { construirDataResumen } = require("./helpers_scag_ai");

/**
 * Equivalente EXACTO a "Code in JavaScript3" + "info" (AI Agent).
 *
 * @param {object} params
 * @param {string} params.tipoMensaje - $('tipo_msje').item.json.tipo_mensaje
 * @param {string} params.nombreUser - $('tipo_msje').item.json.nombre_user
 * @param {object} params.data - $('validador_datos').first().json.data
 */
async function responderInfo({ tipoMensaje, nombreUser, data }) {
  // ---------- Igual que "Code in JavaScript3" ----------
  const dataResumen = construirDataResumen(data);

  // ---------- Igual que el nodo "info" (agent, gemini-2.5-flash, sin temperature custom) ----------
  const systemMessage = `Eres "Sebastián", asistente de SCAG AI peruano. Responde cálido, cercano, jerga peruana, máx 3-4 líneas, NUNCA saludando con "Hola", 2 emojis,EL USUARIO SE LLAMA:${nombreUser}.

Si pregunta algo específico de su cuenta, usa su configuración. Si pregunta algo general, responde sin mencionar la configuración. Nunca inventes datos — si no está, dilo con amabilidad. Termina con una pregunta corta y amigable, variando cada vez.

CONFIGURACIÓN: ${JSON.stringify(dataResumen)}

Responde SOLO este JSON, sin backticks ni texto extra:
{"reply":"...", "context":"INFO|qué pedía en 5 palabras|qué le diste en 5 palabras"}`;

  console.log("ℹ️ [info] tipo_mensaje:", tipoMensaje, "| nombreUser:", nombreUser);

  const textoCrudo = await llamarGemini({
    systemMessage,
    userText: tipoMensaje,
  });

  return parsearRespuestaAgente(textoCrudo);
}

module.exports = { responderInfo };