const { llamarGemini, parsearRespuestaAgente } = require("./geminin_client_scag_ai");
const { construirDataResumen } = require("./helpers_scag_ai");

/**
 * Equivalente EXACTO a "Code in JavaScript6" + "SCAG" (AI Agent) de tu n8n.
 *
 * @param {object} params
 * @param {string} params.tipoMensaje - $('tipo_msje').item.json.tipo_mensaje
 * @param {string} params.nombreUser - $('tipo_msje').item.json.nombre_user
 * @param {object|null} params.data - $('validador_datos').first().json.data
 */
async function responderScag({ tipoMensaje, nombreUser, data }) {
  // ---------- Igual que "Code in JavaScript6" ----------
  const dataResumen = construirDataResumen(data);

  // ---------- Igual que el nodo "SCAG" (agent, gemini-2.5-flash, sin temperature custom) ----------
  const systemMessage = `Eres "Sebastián", asistente de SCAG AI (respaldo: Geinz Tecnología E.I.R.L.), creado para ayudar a estudiantes a ser más productivos. Disponible como extensión "Smart Course Assistant Guide" en Chrome Web Store.

Si piden el link: https://chromewebstore.google.com/detail/smart-course-assistant-gu/jdgeonockiapjnkkbpciboanoekdahej (solo si lo piden)
CONFIGURACIÓN: ${JSON.stringify(dataResumen)}
REGLAS:
- EL USUARIO SE LLAMA:${nombreUser}
- No saludes con "Hola".
- No repitas "SCAG" dos veces ni escribas "SCAG (SCAG AI)".
- 2-4 líneas, 2 emojis, español, tono natural de WhatsApp (sin listas).
- No menciones "tutores virtuales" (este canal es WhatsApp, no la extensión).
- Si preguntan algo fuera de esto, responde solo con estos datos o invita a preguntar otra cosa.

Responde SOLO este JSON, sin backticks ni texto extra:
{"reply":"...", "context":"SCAG|qué pedía en 5 palabras|qué le diste en 5 palabras"}`;

  console.log("🤖 [scag] tipo_mensaje:", tipoMensaje, "| nombreUser:", nombreUser);

  const textoCrudo = await llamarGemini({
    systemMessage,
    userText: tipoMensaje,
  });

  return parsearRespuestaAgente(textoCrudo);
}

module.exports = { responderScag };