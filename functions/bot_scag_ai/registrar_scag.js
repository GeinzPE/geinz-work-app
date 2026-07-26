"use strict";

const { llamarGemini } = require("./geminin_client_scag_ai");

// ============================================================
// Equivalente al agente "registrador a scag" + "limpiador_context1"
// de tu n8n. Este flujo es para USUARIOS NUEVOS/NO REGISTRADOS que
// mandan TEXTO (la contraparte de "rutaImagenSinRegistro", que ya
// tenías para foto).
//
// OJO: el formato de salida de este agente es {output, context},
// DISTINTO al {reply, context} que usan los demás (responderOcio,
// responderInfo, etc.). Por eso el parseo es propio y NO pasa por
// limpiarContextoRespuesta / enviarRespuestaFinal.
// ============================================================

function limpiarYParsear(raw) {
  let texto = raw;

  // 1. Si viene como string con ```json lo limpiamos
  if (typeof texto === "string") {
    texto = texto
      .replace(/```json/g, "")
      .replace(/```/g, "")
      .trim();

    let parsed;
    try {
      parsed = JSON.parse(texto);
    } catch (e) {
      // si falla el parseo, devolvemos fallback seguro
      return { output: texto, context: "parse error" };
    }
    texto = parsed;
  }

  // 2. Si viene envuelto en otro "output" anidado (a veces el modelo
  // devuelve el JSON como string dentro de la clave output)
  if (texto && typeof texto.output === "string") {
    let inner = texto.output.replace(/```json/g, "").replace(/```/g, "").trim();
    try {
      const parsedInner = JSON.parse(inner);
      return {
        output: parsedInner.output || "",
        context: parsedInner.context || "",
      };
    } catch (e) {
      return { output: texto.output, context: texto.context || "" };
    }
  }

  // 3. Caso ideal: ya viene limpio
  return {
    output: texto?.output || "",
    context: texto?.context || "",
  };
}

/**
 * @param {object} params
 * @param {string} params.mensajeUsuario - texto que mandó el usuario nuevo/no registrado
 * @param {string} params.nombreUsuario - nombre de perfil de WhatsApp
 * @param {string} [params.contextoTemporal] - contexto de turnos anteriores (de getContextoTemporal)
 * @returns {Promise<{output: string, context: string}>}
 */
async function responderRegistrador({ mensajeUsuario, nombreUsuario, contextoTemporal = "" }) {
  const systemMessage = `Eres Sebastián, asistente de SCAG AI (respaldado por Geinz tecnologia E.I.R.L , solo dilo si aplica a la conversación). Responde cercano, breve, natural, guiando al registro en https://scag.site/ para desbloquear el resto.

CONTEXTO ANTERIOR: ${contextoTemporal}
NOMBRE: ${nombreUsuario}

REGLAS:
- Nunca saludes con hola. Máx 2-4 líneas. Exactamente 2 emojis.
- SCAG AI ayuda con trabajos y consultas académicas. Sin registro, solo apoyas con cosas simples y básicas.
- Al registrarse, el usuario desbloquea acceso a distintas IAs y puede enviar imágenes para que las proceses.
- Solo respondes: conversación general, dudas simples, y matemáticas básicas (suma, resta, multiplicación, división de números simples).
- Cualquier otra cosa (álgebra, programación, análisis, tareas elaboradas, imagen o audio) NO la resuelves ni parcialmente: redirige a https://scag.site/.
- SI TE DICEN SI CUESTAS CUESTAS CENTAVOS DE SOL POR CONSULTA MENOS DE 10 CENTIMOS EN PRO
Responde SIEMPRE en este JSON, sin texto fuera de él:
{
  "output": "respuesta al usuario",
  "context": "máx 10 palabras para continuar contexto"
}`;

  console.log("🆕 [registrador] mensaje:", mensajeUsuario, "| nombre:", nombreUsuario);

  const textoCrudo = await llamarGemini({
    systemMessage,
    userText: mensajeUsuario,
  });

  const parsed = limpiarYParsear(textoCrudo);

  if (!parsed.output) {
    parsed.output = "Cuéntame de nuevo qué necesitas 🙌 (o regístrate en https://scag.site/ para más 🚀)";
  }

  return parsed;
}

module.exports = { responderRegistrador };