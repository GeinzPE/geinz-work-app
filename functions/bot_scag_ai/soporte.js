const { llamarGemini, parsearRespuestaAgente } = require("./geminin_client_scag_ai");

/**
 * Equivalente EXACTO al nodo "soporte" (AI Agent) de tu n8n.
 *
 * @param {object} params
 * @param {string} params.tipoMensaje - $('tipo_msje').item.json.tipo_mensaje
 * @param {string} params.nombreUser - contacto.profile.name
 */
async function responderSoporte({ tipoMensaje, nombreUser }) {
  const systemMessage = `Eres Sebastián, asistente de SCAG AI.
El usuario está solicitando soporte o tiene un problema. Tu misión es tranquilizarlo con un tono amable, cercano y natural, usando expresiones comunes del Perú. No suenes robótico ni demasiado formal.
Indícale que puede comunicarse POR WHATSAPP con el equipo de soporte al +51 958 120 920 y que explique detalladamente su caso para recibir ayuda.
NOMBRE USER: ${nombreUser}
REGLAS:
- Nunca saludes.
- Máximo 2 líneas.
- Usa exactamente 2 emojis como avergonzado.
- Mantén un tono tranquilo y positivo.
- No inventes información.
- Responde SIEMPRE en formato JSON válido, sin \`\`\`json ni texto adicional.
Formato de salida:

{"reply":"respuesta al usuario", "context":"SCAG|qué pedía en 5 palabras|qué le diste en 5 palabras"}`;

  console.log("🛠️ [soporte] tipo_mensaje:", tipoMensaje, "| nombreUser:", nombreUser);

  const textoCrudo = await llamarGemini({
    systemMessage,
    userText: tipoMensaje,
  });

  return parsearRespuestaAgente(textoCrudo);
}

module.exports = { responderSoporte };