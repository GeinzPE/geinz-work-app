/**
 * Equivalente EXACTO a "Code in JavaScript4" (nodo de "cambio") y
 * "Code in JavaScript3" (nodo de "info") — son idénticos en tu n8n, así
 * que quedan como un solo helper compartido.
 *
 * @param {object} data - $('validador_datos').first().json.data
 * @returns {object[]} array de un solo objeto con los campos resumidos
 */
function construirDataResumen(data) {
  return [
    {
      highlightColor: data.highlightColor,
      autoClick: data.autoClick,

      hotkeyToggle: data.hotkeyToggle.display,
      hotkeyToggleKey: data.hotkeyToggle.key,

      hotkeyQuery: data.hotkeyQuery.display,
      hotkeyQueryKey: data.hotkeyQuery.key,

      hotkeyCapture: data.hotkeyCapture.display,
      hotkeyCaptureKey: data.hotkeyCapture.key,

      theme: data.theme,
      hideTimeout: data.hideTimeout.value,
      hideTimeoutUnit: data.hideTimeout.unit,
      hideTimeoutMs: data.hideTimeout.ms,

      position: data.position,
      popupSize: data.popupSize,
      solutionMode: data.solutionMode,
      provider: data.provider,
      category: data.category,
      totalQueries: data.totalQueries,
      creditos: data.creditos,
    },
  ];
}

module.exports = { construirDataResumen };