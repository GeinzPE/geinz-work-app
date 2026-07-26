const OpenAI = require("openai");

const openai = new OpenAI({ apiKey: process.env.API_KEYO_OPEN_IA });

const CATEGORIAS_VALIDAS = [
  "TRABAJO",
  "RECARGA",
  "HISTORIAL",
  "CAMBIO",
  "INFO",
  "SCAG",
  "OCIO",
  "SOPORTE",
];

/**
 * Clasificador de mensajes — equivalente EXACTO al nodo "clasificador"
 * (AI Agent) de tu workflow de n8n. El systemMessage está copiado tal
 * cual del nodo, sin modificar ni una palabra.
 *
 * @param {string} texto - el mensaje del usuario (texto directo o audio
 *   ya transcrito por Whisper).
 * @param {string} contextoUser - equivalente a
 *   $('validador_datos').item.json.data.context_bot en n8n.
 * @returns {Promise<string>} una de las CATEGORIAS_VALIDAS.
 */
async function clasificarMensaje({ texto, contextoUser = "" }) {
  const systemMessage = `contexto_user: ${contextoUser}

Usa contexto_user SOLO si el mensaje actual continúa el mismo tema. Si no, ignóralo.
Clasifica el mensaje en UNA sola categoría.
SOPORTE: errores, fallas, problemas con SCAG o la cuenta, eliminar cuenta o datos.
TRABAJO: tareas, exámenes, ejercicios, problemas, programación, redacción, análisis o explicaciones paso a paso.
RECARGA: recargas, saldo móvil, datos, minutos, planes o pagos de recarga.
HISTORIAL: gastos, consumos o movimientos anteriores.
INFO: saldo, configuración o información de la cuenta.
CAMBIO: modificar datos o configuración de la cuenta.
SCAG: preguntas sobre SCAG, su empresa, creador, funcionamiento o identidad.
OCIO: cualquier otro mensaje.
REGLAS:
- Devuelve SOLO una de estas palabras:
TRABAJO
RECARGA
HISTORIAL
CAMBIO
INFO
SCAG
OCIO
SOPORTE
- Nunca inventes categorías.
- Si dudas entre dos categorías, elige OCIO.
- No escribas ninguna explicación.
- No uses comillas, puntos, emojis ni saltos de línea.
- La respuesta debe contener exactamente una palabra.`;

  const respuesta = await openai.chat.completions.create({
    model: "gpt-5.4-mini", // mismo modelo configurado en el nodo "OpenAI Chat Model" de n8n
    reasoning_effort: "high", // mismo valor que options.reasoningEffort en n8n
    messages: [
      { role: "system", content: systemMessage },
      { role: "user", content: texto },
    ],
  });

  const output = (respuesta.choices?.[0]?.message?.content || "").trim();

  console.log("🧭 [clasificador] texto recibido:", texto, "| output crudo del modelo:", output);

  if (!CATEGORIAS_VALIDAS.includes(output)) {
    console.warn(
      "⚠️ [clasificador] El modelo devolvió algo fuera de las categorías válidas, cae a OCIO por regla de negocio:",
      output,
    );
    return "OCIO";
  }

  return output;
}

module.exports = { clasificarMensaje, CATEGORIAS_VALIDAS };