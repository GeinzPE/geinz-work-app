/**
 * firestorePaths.js  (Admin SDK — Cloud Functions)
 * -------------------------------------------------------
 * Espejo backend de src/firebase/paths.js (cliente, modular v10).
 * Misma jerarquía, mismos nombres de función, para que el
 * criterio de rutas sea IDÉNTICO en frontend y backend.
 *
 * Jerarquía:
 *   Tiendas/{pais}/departamento/{departamento}/provincia/{provincia}
 *          /distrito/{localidad}/{...resto}
 *
 * NOTA: PAIS/DEPARTAMENTO/PROVINCIA siguen hardcodeados como en
 * tu archivo original. El día que sean dinámicos, se cambia SOLO
 * acá (o se agregan como parámetros — ver comentario al final).
 * -------------------------------------------------------
 */

const admin = require("firebase-admin");

const PAIS = "peru";
const DEPARTAMENTO = "lima"; // hardcodeado por ahora, dinámico más adelante
const PROVINCIA = "barranca"; // hardcodeado por ahora, dinámico más adelante

function db() {
  return admin.firestore();
}

function buildPath(localidad, ...resto) {
  return [
    "Tiendas",
    PAIS,
    "departamento",
    DEPARTAMENTO,
    "provincia",
    PROVINCIA,
    "distrito",
    localidad,
    ...resto,
  ].filter(Boolean);
}

/** doc genérico con cualquier cantidad de segmentos extra */
function tiendaDoc(localidad, ...resto) {
  //tiendaSubDoc
  return db().doc(buildPath(localidad, ...resto).join("/"));
}

/** collection genérica con cualquier cantidad de segmentos extra */
function tiendaCol(localidad, ...resto) {
  //tiendaSubCol
  return db().collection(buildPath(localidad, ...resto).join("/"));
}

function tiendaPathStr(localidad, ...resto) {
  return buildPath(localidad, ...resto).join("/");
}

function negocioDoc(localidad, idTienda) {
  return tiendaDoc(localidad, "tiendas", idTienda); // 👈 confirmar nombre subcolección
}

// Turismo → antes: Tiendas/{loc}/lugares_turisticos/{id}

// =========================================================
// 📤 EXPORTS
// =========================================================

module.exports = {
  db,
  buildPath,
  tiendaDoc,
  tiendaCol,
  tiendaPathStr,
  negocioDoc,
};
