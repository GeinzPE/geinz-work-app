const { llamarGemini, parsearRespuestaAgente } = require("./geminin_client_scag_ai");

/**
 * Equivalente EXACTO a los nodos "limpiar_texto" (Code) + "ocio" (AI Agent).
 *
 * @param {object} params
 * @param {string} params.nombreUsuario - contacto.profile.name (fallback "crack", igual que en n8n)
 * @param {boolean|string|undefined} params.esAcademico - equivalente a
 *   $('var si problemas').first()?.json?.es_academico (puede no existir)
 * @param {string} params.tipoMensajeTexto - tipo_mensaje cuando SÍ hay texto
 *   (equivalente a $('tipo_msje').first()?.json?.tipo_mensaje)
 * @param {string} params.descripcionVision - $input.first()?.json?.descripcion
 * @param {string} params.respuestaVision - $input.first()?.json?.respuesta_vision
 * @param {boolean} params.tieneImagen - equivalente a $('tipo_msje').isExecuted
 *   (true = vino texto/audio, false = vino una foto)
 */
async function responderOcio({
  nombreUsuario,
  esAcademico,
  tipoMensajeTexto = "",
  descripcionVision = "",
  respuestaVision = "",
  tieneImagen = true,
}) {
  // ---------- Igual que "limpiar_texto" ----------
  const respuesta_vision = respuestaVision || descripcionVision;
  const tipo_mensaje = tieneImagen ? tipoMensajeTexto || "" : `${respuesta_vision}`;
  const nombre = nombreUsuario || "crack";

  const formatoJson = `FORMATO DE SALIDA — responde ÚNICAMENTE con este JSON, sin texto antes ni después, sin backticks, sin markdown:
{"reply": "el mensaje para el usuario siguiendo las reglas de estilo", "context": "OCIO|qué pedía el usuario en máximo 10 palabras|qué le respondiste en máximo 10 palabras"}`;

  const reglas = `Reglas de estilo:
- EL USUARIO SE LLAMA: ${nombre}
- NUNCA SALUDES CON HOLA, PROHIBIDO DECIR "qué buena"
- Habla como pata cercano, sin formalismos ni tecnicismos
- Máximo 3 líneas, máximo 2 emojis obligatorio
- Si no sabes algo no mientas
- Nunca expliques que eres una IA o cómo funciona el sistema`;

  const promptFotoNoAcademica = `Eres "Sebastián", el pata de SCAG AI en WhatsApp que ayuda en estudios.
El usuario mandó una foto que NO es un problema académico, es de: "${respuesta_vision}". Redirígelo con calidez hacia lo que importa.
${reglas}
${formatoJson}`;

  const promptNormal = `Eres "Sebastián", el pata de SCAG AI en WhatsApp que ayuda en estudios.
Si el usuario se desvía del tema (saluda de más, pregunta otra cosa, manda algo que no es tarea), redirígelo con calidez, como amigo, nunca como robot.
${reglas}
${formatoJson}`;

  // Normaliza esAcademico a booleano real (venga como boolean, string, undefined/null)
  const esAcademicoBool =
    esAcademico === false || esAcademico === "false"
      ? false
      : esAcademico === true || esAcademico === "true"
      ? true
      : esAcademico;

  const usarNoAcademico = esAcademicoBool === false;
  const prompt = usarNoAcademico ? promptFotoNoAcademica : promptNormal;

  console.log(
    "🎈 [ocio] tipo_mensaje:",
    tipo_mensaje,
    "| promptUsado:",
    usarNoAcademico ? "promptFotoNoAcademica" : "promptNormal",
    "| esAcademicoBool:",
    esAcademicoBool,
  );

  // ---------- Igual que el nodo "ocio" (agent, gemini-2.5-flash, temperature 0.2) ----------
  const textoCrudo = await llamarGemini({
    systemMessage: prompt,
    userText: tipo_mensaje,
    temperature: 0.2,
  });

  return parsearRespuestaAgente(textoCrudo);
}

module.exports = { responderOcio };