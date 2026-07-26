/* =========================================================================
   token_utils.js — helpers para trackear y sumar los tokens gastados en
   OpenAI y Gemini a lo largo de UNA MISMA consulta del usuario (que puede
   implicar varias llamadas: ej. dispensador clasifica con OpenAI, y luego
   la rama llama a OpenAI de nuevo y/o a Gemini para redactar).

   Cada rama arma su propio "detalle" (una entrada por cada llamada que
   hizo) usando tokenOpenAI()/tokenGemini() + armarTokens(), y lo devuelve
   en su respuesta como campo "tokens": { detalle: [...], total: number }.

   El dispensador / webhook después junta el tokens de la clasificación con
   el tokens de la rama usando combinarTokens(), y arma el resumen final
   (por proveedor) con resumenPorProveedor().
========================================================================= */

/** Arma una entrada de detalle para una llamada a OpenAI (usa completion.usage). */
function tokenOpenAI(usage, modelo) {
  return {
    proveedor: "openai",
    modelo,
    prompt_tokens: usage?.prompt_tokens ?? null,
    completion_tokens: usage?.completion_tokens ?? null,
    total_tokens: usage?.total_tokens ?? null,
  };
}

/** Arma una entrada de detalle para una llamada a Gemini (usa data.usageMetadata). */
function tokenGemini(usageMetadata, modelo) {
  return {
    proveedor: "gemini",
    modelo,
    prompt_tokens: usageMetadata?.promptTokenCount ?? null,
    completion_tokens: usageMetadata?.candidatesTokenCount ?? null,
    total_tokens: usageMetadata?.totalTokenCount ?? null,
  };
}

/** Suma total_tokens de una lista de entradas de detalle (ignora nulls). */
function sumarTotal(detalle) {
  return (detalle || []).reduce((acc, t) => acc + (t?.total_tokens || 0), 0);
}

/** Arma el objeto {detalle, total} que cada rama devuelve en "tokens". */
function armarTokens(detalle) {
  const limpio = (detalle || []).filter(Boolean);
  return { detalle: limpio, total: sumarTotal(limpio) };
}

/**
 * Junta el "tokens" de varias fuentes (ej. clasificación del dispensador +
 * la rama que respondió) en un solo resumen de la consulta completa.
 */
function combinarTokens(...listaDeTokens) {
  const detalleCombinado = listaDeTokens
    .filter(Boolean)
    .flatMap((t) => t.detalle || []);
  return armarTokens(detalleCombinado);
}

/** Separa el total combinado en subtotal OpenAI / Gemini + total general. */
function resumenPorProveedor(tokensCombinados) {
  const detalle = tokensCombinados?.detalle || [];
  const openai = detalle
    .filter((t) => t.proveedor === "openai")
    .reduce((acc, t) => acc + (t.total_tokens || 0), 0);
  const gemini = detalle
    .filter((t) => t.proveedor === "gemini")
    .reduce((acc, t) => acc + (t.total_tokens || 0), 0);
  return { openai, gemini, total: openai + gemini };
}

/** Línea corta y humana, útil para pegarla al final de un mensaje o loguearla. */
function lineaTokens(tokensCombinados) {
  const r = resumenPorProveedor(tokensCombinados);
  return `\n\n🔢 Tokens: OpenAI ${r.openai} · Gemini ${r.gemini} · Total ${r.total}`;
}

module.exports = {
  tokenOpenAI,
  tokenGemini,
  armarTokens,
  combinarTokens,
  resumenPorProveedor,
  lineaTokens,
};