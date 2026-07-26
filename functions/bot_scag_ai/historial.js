const { llamarGemini, parsearRespuestaAgente } = require("./geminin_client_scag_ai");
const { historialn8n } = require("./extencion_reutilziable/historialn8n");
/**
 * Equivalente EXACTO a "historial" (antes HTTP Request) + "Code in JavaScript5"
 * + "historial2" (AI Agent).
 *
 * OJO: en n8n el nodo "historial" le pegaba por HTTP a
 * https://historialn8n-oixttik5rq-uc.a.run.app con { alias }. Como me
 * confirmaste que esa lógica vive en extencion.js dentro del mismo proyecto,
 * aquí llamo a `historialn8n` directo en vez de hacer un fetch.
 *
 * ASUNCIÓN (confírmame si es distinto): asumí que `historialn8n({ alias })`
 * devuelve un array de items, cada uno con un campo `.resumen` — igual que
 * "Code in JavaScript5" mapeaba `item.json.resumen` de cada item que
 * devolvía el HTTP Request.
 *
 * @param {object} params
 * @param {string} params.alias - $('validador_datos').item.json.alias
 * @param {string} params.tipoMensaje - $('tipo_msje').item.json.tipo_mensaje
 * @param {string} params.nombreUser - $('tipo_msje').item.json.nombre_user
 */
async function responderHistorial({ alias, tipoMensaje, nombreUser }) {
  // ---------- Igual que el nodo "historial" (ahora llamada directa, no HTTP) ----------
  const itemsHistorial = await historialn8n({ alias });

  // ---------- Igual que "Code in JavaScript5" ----------
  const listaItems = Array.isArray(itemsHistorial) ? itemsHistorial : [itemsHistorial];
  const resumenes = listaItems.map((item) => item?.resumen ?? item?.json?.resumen ?? null);

  const dataParaPrompt = { resumenes };

  console.log("🗂️ [historial] alias:", alias, "| resumenes obtenidos:", resumenes.length);

  // ---------- Igual que el nodo "historial2" (agent, gemini-2.5-flash, sin temperature custom) ----------
  const systemMessage = `Eres "Sebastián", asistente de SCAG AI.

Información del usuario:
${JSON.stringify(dataParaPrompt, null, 2)}

Reglas:
- EL USUARIO SE LLAMA:${nombreUser}
- NUNCA SALUDES
- Si pregunta por un dato específico (consultas, créditos, categorías, modelos, modos, tipos o período), responde solo con esa info.
- Si no es específico, ofrece un resumen breve de su actividad.
- Tono peruano, cercano, amable. Máx 4 líneas.
- No inventes información. Ignora valores \`null\`. No menciones el JSON ni su estructura.
- La info disponible es solo de los últimos 7 días.
- Si pide historial más detallado, rango mayor a 7 días, estadísticas completas o todo su historial, indícale https://scag.site/panel — solo si realmente aplica.

Responde SOLO este JSON, sin backticks ni texto extra:
{"reply":"...", "context":"HISTORIAL|qué pedía en 5 palabras|qué le diste en 5 palabras"}`;

  console.log("🗂️ [historial2] tipo_mensaje:", tipoMensaje);

  const textoCrudo = await llamarGemini({
    systemMessage,
    userText: tipoMensaje,
  });

  return parsearRespuestaAgente(textoCrudo);
}

module.exports = { responderHistorial };