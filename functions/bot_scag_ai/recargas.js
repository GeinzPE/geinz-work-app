const { llamarGemini, parsearRespuestaAgente } = require("./geminin_client_scag_ai");

// ── Imágenes promocionales, igual que el nodo "IMG_RAMDOM" de tu n8n ──
const IMAGENES_RECARGA = [
  "https://firebasestorage.googleapis.com/v0/b/geinzworkapp.appspot.com/o/scag_ia%2FChatGPT%20Image%2029%20jun%202026%2C%2021_23_33.png?alt=media&token=55e1f0be-138b-4c63-aa44-78175f3d78f7",
  "https://firebasestorage.googleapis.com/v0/b/geinzworkapp.appspot.com/o/scag_ia%2FChatGPT%20Image%2029%20jun%202026%2C%2021_24_47.png?alt=media&token=976b823f-1ab5-47d3-8a50-a091be311dae",
  "https://firebasestorage.googleapis.com/v0/b/geinzworkapp.appspot.com/o/scag_ia%2FChatGPT%20Image%2029%20jun%202026%2C%2021_25_10.png?alt=media&token=bbf1c3bc-c0cd-4f5e-af61-1eed414dddfc",
  "https://firebasestorage.googleapis.com/v0/b/geinzworkapp.appspot.com/o/scag_ia%2Frecargas_scag.jpg?alt=media&token=0dc0984e-94f6-4a1b-920c-eb756f63da9c",
  "https://firebasestorage.googleapis.com/v0/b/geinzworkapp.appspot.com/o/scag_ia%2Fsacg1.png?alt=media&token=e3aa9854-18f5-41f7-8406-e8cda02480ba",
  "https://firebasestorage.googleapis.com/v0/b/geinzworkapp.appspot.com/o/scag_ia%2Fscag2.png?alt=media&token=fe0ed873-016a-4650-8749-d7aed9ee0721",
];

const PROBABILIDAD_IMAGEN = 0.5;

/**
 * Equivalente EXACTO a "CONVERTIR_PRECIOS" + "recargas" (AI Agent) + "IMG_RAMDOM"
 * de tu n8n. NOTA: la rama "sin_creditos_user" del n8n original viene de un
 * trigger automático distinto (cuando la extensión se queda sin créditos a
 * medio uso) — no aplica al flujo de WhatsApp, donde el usuario siempre
 * pregunta manualmente por texto. Por eso aquí solo se usa "promptNormal".
 *
 * @param {object} params
 * @param {string} params.tipoMensaje - mensaje del usuario
 * @param {string} params.nombreUser - nombre del usuario
 * @param {object} params.planes - { avanzado, basico, medio, pro, proultra }, cada uno { nombre, creditos, precio }
 * @returns {Promise<{reply:string, context:string, parseOk:boolean, tieneImagen:boolean, imagen:string|null}>}
 */
async function responderRecarga({ tipoMensaje, nombreUser, planes }) {
  // ---------- Igual que "CONVERTIR_PRECIOS" ----------
  const tienePlanes = planes && typeof planes === "object" && Object.keys(planes).length > 0;
  const textoplanes = tienePlanes
    ? Object.values(planes)
        .filter((plan) => plan && plan.nombre)
        .map((plan) => `${plan.nombre}: ${plan.creditos} créditos - S/. ${plan.precio}`)
        .join(", ")
    : "";

  const formatoJson = `Responde SOLO este JSON, sin backticks ni texto extra:
{"reply": "...", "context": "RECARGA|qué pedía en 5 palabras|qué le diste en 5 palabras"}`;

  const systemMessage = `Eres "SEBASTIAN" de SCAG AI, ayudas con recargas. Amigo, relajado, nada formal.
USUARIO: ${nombreUser}
PLANES: ${textoplanes}
Plan específico → solo ese plan. General → todos los planes. ¿Cuál conviene? → mejor créditos/precio. ¿Cómo pagar? → Culqi(yape,tarjeta,plin) en https://scag.site/ con su ID.
Nunca saludes con "Hola". Máximo 4 líneas, 2 emojis. Sin frases formales.
${formatoJson}`;

  console.log("💳 [recarga] tipo_mensaje:", tipoMensaje, "| planes disponibles:", tienePlanes);

  const textoCrudo = await llamarGemini({
    systemMessage,
    userText: tipoMensaje,
  });

  const parsed = parsearRespuestaAgente(textoCrudo);

  // ---------- Igual que "IMG_RAMDOM": limpia saltos de línea y decide imagen al azar ----------
  const replyLimpio = String(parsed.reply || "")
    .replace(/\r/g, "")
    .replace(/\n/g, " ")
    .replace(/\t/g, " ")
    .trim();

  const tieneImagen = Math.random() < PROBABILIDAD_IMAGEN;
  const imagen = tieneImagen
    ? IMAGENES_RECARGA[Math.floor(Math.random() * IMAGENES_RECARGA.length)]
    : null;

  return {
    ...parsed,
    reply: replyLimpio,
    tieneImagen,
    imagen,
  };
}

module.exports = { responderRecarga };