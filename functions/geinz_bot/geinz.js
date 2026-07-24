// ============================================================================
// ============================================================================
//   MÓDULO GEINZ — Asistente genérico "DANIEL"
//   (saludos, soporte, registro de negocio, mensajes sin categoría clara)
// ============================================================================
// ============================================================================

const GEMINIKEY = process.env.PRIVATEKEY_GEMINI;
const GEMINI_URL =
  "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent";

// PROMPT TAL CUAL — NO TOCAR
function construirPromptGeinz(mensaje, nombreUsuario) {
  return `Eres "DANIEL" el asistente oficial de Geinz (RUC 20615632580) para Barranca.
FUNCIONES: Recomendar negocios con horarios reales, turismo local, emergencias y promos de Barranca.

PERSONALIDAD:
- Pata peruano, canchero y natural, nada robótico ni corporativo
- Si te falta info, pide más detalle con frases distintas cada vez, nunca la misma dos veces seguidas
- Cierra siempre motivando a seguir con frases distintas cada vez, nunca la misma dos veces seguidas
REGLAS:
- Habla como conversación continua, sin saludar, lenguaje local siempre
- Dirígete por su nombre: ${nombreUsuario}
- Máx 3 líneas, exactamente 2 emojis, entiende jergas peruanas
- NUNCA inventes datos, promos, horarios ni negocios,NI NUMEROS DE ENTIDADES PUBLICAS NI PRIVADAS
- Consulta vaga → pide más detalle con curiosidad
- Registro de negocio → deriva al +51 958 120 920 sin explicar el proceso NI PEDIR PAGOS
- Insultos o críticas a negocios → redirige con calma, nunca des la razón ni repitas el insulto
- Fuiste creado solo por Geinz
- extra: si ofreciste elegir entre negocio/turismo/promociones, pon "ESPERANDO_ELEC:x,y,z" en ese orden; si no, resume en 5 palabras qué quería y qué dijiste

MENSAJE DEL USUARIO: "${mensaje}"

RESPONDE SIEMPRE EN ESTE JSON, sin texto fuera de él:
{"mensaje":"...","id":"null","tipo":"|NEGOCIO|TURISMO|GEINZ","extra":"..."}`;
}

async function llamarGeminiGeinz(mensaje, nombreUsuario) {
  const prompt = construirPromptGeinz(mensaje, nombreUsuario);

  const body = {
    contents: [{ parts: [{ text: prompt }] }],
    generationConfig: {
      responseMimeType: "application/json",
      responseSchema: {
        type: "object",
        properties: {
          mensaje: { type: "string" },
          id: { type: "string" },
          tipo: { type: "string" },
          extra: { type: "string" },
        },
        required: ["mensaje", "id", "tipo", "extra"],
      },
      thinkingConfig: { thinkingBudget: 0 },
      maxOutputTokens: 220,
      temperature: 0.7,
    },
  };

  const resp = await fetch(`${GEMINI_URL}?key=${GEMINIKEY}`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(body),
  });

  if (!resp.ok) {
    const errText = await resp.text();
    console.error(
      "❌ [llamarGeminiGeinz] Error Gemini API:",
      resp.status,
      errText,
    );
    return {
      resultado: {
        mensaje: "Cuéntame un poco más para ayudarte mejor 🙌",
        id: "null",
        tipo: "GEINZ",
        extra: "error_gemini",
      },
      tokens: { prompt_tokens: 0, completion_tokens: 0, total_tokens: 0 },
    };
  }

  const data = await resp.json();
  const rawText = data?.candidates?.[0]?.content?.parts?.[0]?.text || "{}";

  let resultado;
  try {
    resultado = JSON.parse(rawText.replace(/```json|```/g, "").trim());
  } catch (e) {
    console.error(
      "❌ [llamarGeminiGeinz] Error parseando respuesta:",
      e.message,
      "| RAW:",
      rawText,
    );
    resultado = {
      mensaje: "Cuéntame un poco más para ayudarte mejor 🙌",
      id: "null",
      tipo: "GEINZ",
      extra: "error_parseo",
    };
  }

  const tokens = {
    prompt_tokens: data?.usageMetadata?.promptTokenCount || 0,
    completion_tokens: data?.usageMetadata?.candidatesTokenCount || 0,
    total_tokens: data?.usageMetadata?.totalTokenCount || 0,
  };

  return { resultado, tokens };
}

exports.llamarGeminiGeinz = llamarGeminiGeinz;