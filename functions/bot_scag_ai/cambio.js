const { llamarGemini, parsearRespuestaAgente } = require("./geminin_client_scag_ai");
const { construirDataResumen } = require("./helpers_scag_ai");

/**
 * Equivalente EXACTO a "Code in JavaScript4" + "cambio" (AI Agent).
 *
 * @param {object} params
 * @param {string} params.tipoMensaje - $('tipo_msje').item.json.tipo_mensaje
 * @param {string} params.nombreUser - $('tipo_msje').item.json.nombre_user
 * @param {object} params.data - $('validador_datos').first().json.data (config completa del usuario)
 */
async function responderCambio({ tipoMensaje, nombreUser, data }) {
  // ---------- Igual que "Code in JavaScript4" ----------
  const dataResumen = construirDataResumen(data);

  // ---------- Igual que el nodo "cambio" (agent, gemini-2.5-flash, temperature 0.2) ----------
  const systemMessage = `Eres "Sebastián", asistente de SCAG AI peruano. Cuando el usuario quiera cambiar, modificar o personalizar algo de su cuenta, respóndele cálido y cercano, jerga peruana, sin saludar con "Hola", máx 3 líneas, 1-2 emojis, EL USUARIO SE LLAMA:${nombreUser}.

CONFIGURACION DEL USUARIO: ${JSON.stringify(dataResumen)}
Indícale que puede hacerlo en https://scag.site/login con su clave personal, donde cambia en tiempo real su modelo, categoría, atajos, estilo de panel, estilo de respuesta y más.

Termina con una pregunta corta y amigable invitándolo a seguir consultando.

Responde SOLO este JSON, sin backticks ni texto extra:
{"reply":"...", "context":"CAMBIO|qué quería cambiar en 5 palabras|qué le indicaste en 5 palabras"}`;

  console.log("🔧 [cambio] tipo_mensaje:", tipoMensaje, "| nombreUser:", nombreUser);

  const textoCrudo = await llamarGemini({
    systemMessage,
    userText: tipoMensaje,
    temperature: 0.2,
  });

  return parsearRespuestaAgente(textoCrudo);
}

module.exports = { responderCambio };