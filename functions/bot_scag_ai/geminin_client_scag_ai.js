const GEMINI_FLASH_URL =
  "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent";

// TODO: confirma el nombre real de tu variable de entorno con la API key de Gemini
const GOOGLE_GEMINI_API_KEY = process.env.PIRVATE_KEY_GEMINI_APITRABAJO;

/**
 * Llama a Gemini igual que los nodos "AI Agent" de n8n, pero directo
 * contra la API REST (mismo patrón que enviarMensajeWhatsapp: fetch crudo).
 * - systemMessage -> systemInstruction
 * - userText -> el campo "text" del agente (equivalente a $json.tipo_mensaje)
 * - temperature -> generationConfig.temperature (si no se pasa, se deja el
 *   default de la API, igual que cuando en n8n el nodo tenía options: {}
 *   sin temperature)
 */
async function llamarGemini({ systemMessage, userText, temperature }) {
  if (!GOOGLE_GEMINI_API_KEY) {
    throw new Error(
      "Falta la variable de entorno GOOGLE_GEMINI_API_KEY. No se puede llamar a Gemini.",
    );
  }

  const body = {
    contents: [{ parts: [{ text: userText }] }],
  };

  if (systemMessage) {
    body.systemInstruction = { parts: [{ text: systemMessage }] };
  }

  if (typeof temperature === "number") {
    body.generationConfig = { temperature };
  }

  const url = `${GEMINI_FLASH_URL}?key=${GOOGLE_GEMINI_API_KEY}`;

  const respuesta = await fetch(url, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(body),
  });

  const data = await respuesta.json();

  if (!respuesta.ok) {
    console.error(
      "❌ [llamarGemini] Gemini respondió con error:",
      respuesta.status,
      JSON.stringify(data),
    );
    throw new Error(
      `Gemini API error ${respuesta.status}: ${data?.error?.message || "desconocido"}`,
    );
  }

  const texto = data.candidates?.[0]?.content?.parts?.[0]?.text || "";

  if (!texto) {
    console.warn(
      "⚠️ [llamarGemini] Respuesta sin texto utilizable:",
      JSON.stringify(data),
    );
  }

  return texto;
}

/**
 * Los agentes de n8n están instruidos para devolver SOLO un JSON
 * ({"reply":..., "context":...}), pero el modelo a veces lo envuelve en
 * ```json ... ``` o mete espacios. Esto lo deja seguro.
 */
function parsearRespuestaAgente(textoCrudo) {
  let limpio = (textoCrudo || "").trim();
  limpio = limpio
    .replace(/^```json/i, "")
    .replace(/^```/, "")
    .replace(/```$/, "")
    .trim();

  try {
    const parsed = JSON.parse(limpio);
    return { ...parsed, _raw: limpio };
  } catch (error) {
    console.error(
      "❌ [parsearRespuestaAgente] No se pudo parsear el JSON del modelo:",
      error.message,
      "| texto crudo:",
      textoCrudo,
    );
    return { reply: limpio, context: null, _parse_error: true, _raw: limpio };
  }
}

module.exports = { llamarGemini, parsearRespuestaAgente };