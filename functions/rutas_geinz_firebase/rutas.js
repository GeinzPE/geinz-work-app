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
const DEPARTAMENTO = "lima";  // hardcodeado por ahora, dinámico más adelante
const PROVINCIA = "barranca"; // hardcodeado por ahora, dinámico más adelante

function db() {
  return admin.firestore();
}

/**
 * localidad = distrito (barranca, supe, paramonga, pativilca, puerto-supe)
 */
function buildPath(localidad, ...resto) {
  return [
    "Tiendas", PAIS,
    "departamento", DEPARTAMENTO,
    "provincia", PROVINCIA,
    "distrito", localidad,
    ...resto,
  ].filter(Boolean);
}

/** doc: .../distrito/{localidad}/{subcoleccion}/{id} */
function tiendaDoc(localidad, subcoleccion, id) {
  return db().doc(buildPath(localidad, subcoleccion, id).join("/"));
}

/** collection: .../distrito/{localidad}/{subcoleccion} */
function tiendaCol(localidad, subcoleccion) {
  return db().collection(buildPath(localidad, subcoleccion).join("/"));
}

/** doc genérico con cualquier cantidad de segmentos extra */
function tiendaSubDoc(localidad, ...resto) {
  return db().doc(buildPath(localidad, ...resto).join("/"));
}

/** collection genérica con cualquier cantidad de segmentos extra */
function tiendaSubCol(localidad, ...resto) {
  return db().collection(buildPath(localidad, ...resto).join("/"));
}

function tiendaPathStr(localidad, ...resto) {
  return buildPath(localidad, ...resto).join("/");
}

// =========================================================
// ⚠️ ATAJOS — mapeo de las colecciones viejas a la nueva jerarquía
// Ajusta el nombre de "subcoleccion" si en tu frontend nuevo le
// pusiste otro nombre (ej: "negocios" en vez de "tiendas").
// =========================================================

// Negocio/tienda individual → antes: Tiendas/{loc}/{loc}/{id}
function negocioDoc(localidad, idTienda) {
  return tiendaDoc(localidad, "tiendas", idTienda); // 👈 confirmar nombre subcolección
}
function negociosCol(localidad) {
  return tiendaCol(localidad, "tiendas"); // 👈 confirmar nombre subcolección
}

// Promos → antes: Tiendas/{loc}/promos_ofertas/{id}
function promoOfertaDoc(localidad, promoId) {
  return tiendaDoc(localidad, "promos_ofertas", promoId);
}
function promosOfertasCol(localidad) {
  return tiendaCol(localidad, "promos_ofertas");
}

// Cache filtrado → antes: Tiendas/{loc}/cache_filtrado/filtrado
function cacheFiltradoDoc(localidad) {
  return tiendaSubDoc(localidad, "cache_filtrado", "filtrado");
}

// Turismo → antes: Tiendas/{loc}/lugares_turisticos/{id}
function lugarTuristicoDoc(localidad, id) {
  return tiendaDoc(localidad, "lugares_turisticos", id);
}
function lugaresTuristicosCol(localidad) {
  return tiendaCol(localidad, "lugares_turisticos");
}

// Salud/seguridad → antes: Tiendas/salud_seguridad/{loc}/{id}
// (OJO: antes NO colgaba de {loc} como distrito, sino de un doc fijo
// "salud_seguridad". Si la migras a la jerarquía nueva, decide si va
// dentro del distrito o se queda aparte).
function lugarSeguroDoc(localidad, id) {
  return tiendaDoc(localidad, "salud_seguridad", id);
}
function lugaresSegurosCol(localidad) {
  return tiendaCol(localidad, "salud_seguridad");
}

// Inmobiliaria → antes: Tiendas/{loc}/geinz_inmobiliaria/{id}
function inmobiliariaDoc(localidad, id) {
  return tiendaDoc(localidad, "geinz_inmobiliaria", id);
}
function inmobiliariaCol(localidad) {
  return tiendaCol(localidad, "geinz_inmobiliaria");
}

// Pagos de tienda → antes: Tiendas/{loc}/pagos_tiendas/{idPago}
function pagoTiendaDoc(localidad, idPago) {
  return tiendaDoc(localidad, "pagos_tiendas", idPago);
}
function pagosTiendaCol(localidad) {
  return tiendaCol(localidad, "pagos_tiendas");
}

// =========================================================
// 🌍 GLOBALES — NO dependen de distrito/localidad, quedan igual
// =========================================================

function categoriasCollection() {
  return db().collection("Tiendas").doc("categorias").collection("categorias");
}
function categoriaDoc(categoria) {
  return categoriasCollection().doc(categoria);
}

function usuariosBotCollection() {
  return db()
    .collection("Trabajadores_Usuarios_Drivers")
    .doc("usuario_bot_geinz")
    .collection("usuario_bot_geinz");
}
function usuarioBotDoc(numeroUser) {
  return usuariosBotCollection().doc(numeroUser);
}

function tokensCollection() {
  return db().collection("Trabajadores_Usuarios_Drivers").doc("users").collection("tokens");
}
function tokenDoc(uid) {
  return tokensCollection().doc(uid);
}

function aliasTiendaDoc(alias) {
  return db().collection("alias_tiendas").doc(alias);
}
function aliasTurismoDoc(alias) {
  return db().collection("alias_turismo").doc(alias);
}

function ordenPagoDoc(orderId) {
  return db().collection("ordenes_pagos").doc(orderId);
}

// =========================================================
// 📤 EXPORTS
// =========================================================

module.exports = {
  db,
  buildPath,
  // genéricos (idénticos al cliente)
  tiendaDoc,
  tiendaCol,
  tiendaSubDoc,
  tiendaSubCol,
  tiendaPathStr,
  // atajos migrados
  negocioDoc,
  negociosCol,
  promoOfertaDoc,
  promosOfertasCol,
  cacheFiltradoDoc,
  lugarTuristicoDoc,
  lugaresTuristicosCol,
  lugarSeguroDoc,
  lugaresSegurosCol,
  inmobiliariaDoc,
  inmobiliariaCol,
  pagoTiendaDoc,
  pagosTiendaCol,
  // globales sin cambios
  categoriasCollection,
  categoriaDoc,
  usuariosBotCollection,
  usuarioBotDoc,
  tokensCollection,
  tokenDoc,
  aliasTiendaDoc,
  aliasTurismoDoc,
  ordenPagoDoc,
};