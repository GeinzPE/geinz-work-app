#!/usr/bin/env node
/**
 * validar_rutas_v2.js
 * -------------------------------------------------------
 * Versión mejorada de validar_rutas.js.
 *
 * Ademas de contar segmentos par/impar, ahora MIRA EL CONTEXTO
 * de cada llamada (lo que se hace con el resultado) para decirte
 * si el bug es:
 *
 *   A) "NOMBRE_MAL"      -> el código usa métodos de documento
 *                           (.data(), .exists, .set(, .update(, .delete()
 *                           pero llamaste a tiendaCol.
 *                           SOLUCION: cambia tiendaCol -> tiendaDoc
 *                           (o viceversa). Es un simple swap de nombre.
 *
 *   B) "FALTA_ARGUMENTO" -> el código usa métodos de colección
 *                           (.forEach(, .docs, .where(, .add(, .size, .empty)
 *                           pero el conteo de segmentos da par (documento).
 *                           SOLUCION: probablemente falta un segmento
 *                           (una subcolección) en los argumentos.
 *
 *   C) "AMBIGUO"         -> no se detectaron señales claras de uso.
 *                           Hay que revisar el código a mano.
 *
 * Sigue reportando también los .set(/.update() encadenados directo
 * sobre tiendaCol(...) como error garantizado (CollectionReference
 * no tiene esos métodos).
 *
 * USO:
 *   node validar_rutas_v2.js [carpeta]
 * -------------------------------------------------------
 */

const fs = require("fs");
const path = require("path");

const ROOT = process.argv[2] || ".";
const SEGMENTOS_FIJOS = 8; // Tiendas/peru/departamento/lima/provincia/barranca/distrito/{localidad}
const IGNORAR_CARPETAS = new Set(["node_modules", ".git", "dist", "build"]);
const IGNORAR_ARCHIVOS = new Set(["validar_rutas.js", "validar_rutas_v2.js"]);

// cuántas líneas hacia adelante buscamos uso de la variable resultante
const LINEAS_CONTEXTO_ADELANTE = 40;

// señales de que el resultado se está tratando como DOCUMENTO
const SIGNALS_DOC = [
  /\.data\s*\(/,
  /\.exists\b/,
  /\.set\s*\(/,
  /\.update\s*\(/,
  /\.delete\s*\(\s*\)/, // delete() sin args = borrar doc
];

// señales de que el resultado se está tratando como COLECCION
const SIGNALS_COL = [
  /\.forEach\s*\(/,
  /\.docs\b/,
  /\.where\s*\(/,
  /\.add\s*\(/,
  /\.orderBy\s*\(/,
  /\.limit\s*\(/,
  /\.size\b/,
  /\.empty\b/,
];

// ────────────────────────────────────────────────
function listarArchivosJS(dir) {
  let resultados = [];
  const entradas = fs.readdirSync(dir, { withFileTypes: true });
  for (const entrada of entradas) {
    if (IGNORAR_CARPETAS.has(entrada.name)) continue;
    const rutaCompleta = path.join(dir, entrada.name);
    if (entrada.isDirectory()) {
      resultados = resultados.concat(listarArchivosJS(rutaCompleta));
    } else if (
      entrada.isFile() &&
      entrada.name.endsWith(".js") &&
      !IGNORAR_ARCHIVOS.has(entrada.name)
    ) {
      resultados.push(rutaCompleta);
    }
  }
  return resultados;
}

function detectarImports(contenido) {
  const imports = [];
  const regexAlias =
    /const\s+(\w+)\s*=\s*require\(\s*["']([^"']*(?:rutas|paths|firestorePaths)[^"']*)["']\s*\)/gi;
  let m;
  while ((m = regexAlias.exec(contenido)) !== null) {
    imports.push({ tipo: "alias", nombre: m[1], modulo: m[2] });
  }
  const regexDestructure =
    /const\s*\{\s*([^}]*)\}\s*=\s*require\(\s*["']([^"']*(?:rutas|paths|firestorePaths)[^"']*)["']\s*\)/gi;
  while ((m = regexDestructure.exec(contenido)) !== null) {
    const nombres = m[1].split(",").map((s) => s.trim()).filter(Boolean);
    imports.push({ tipo: "destructure", nombres, modulo: m[2] });
  }
  return imports;
}

function splitArgsTopLevel(text) {
  const args = [];
  let depth = 0;
  let current = "";
  let quoteChar = null;
  for (let i = 0; i < text.length; i++) {
    const ch = text[i];
    if (quoteChar) {
      current += ch;
      if (ch === quoteChar && text[i - 1] !== "\\") quoteChar = null;
      continue;
    }
    if (ch === '"' || ch === "'" || ch === "`") {
      quoteChar = ch;
      current += ch;
      continue;
    }
    if ("([{".includes(ch)) depth++;
    if (")]}".includes(ch)) depth--;
    if (ch === "," && depth === 0) {
      args.push(current.trim());
      current = "";
      continue;
    }
    current += ch;
  }
  if (current.trim().length > 0) args.push(current.trim());
  return args;
}

function encontrarCierre(contenido, idxAperturaParen) {
  let depth = 0;
  let quoteChar = null;
  for (let i = idxAperturaParen; i < contenido.length; i++) {
    const ch = contenido[i];
    if (quoteChar) {
      if (ch === quoteChar && contenido[i - 1] !== "\\") quoteChar = null;
      continue;
    }
    if (ch === '"' || ch === "'" || ch === "`") {
      quoteChar = ch;
      continue;
    }
    if (ch === "(") depth++;
    if (ch === ")") {
      depth--;
      if (depth === 0) return i;
    }
  }
  return -1;
}

// Intenta encontrar el nombre de variable a la que se asigna la llamada,
// buscando hacia atrás desde el inicio de la línea: "const X =" / "let X =" / "X ="
function detectarVariableAsignada(contenido, idxInicioLinea, idxMatch) {
  const textoAntes = contenido.slice(idxInicioLinea, idxMatch);
  const m = textoAntes.match(/(?:const|let|var)?\s*(\w+)\s*=\s*(?:await\s+)?$/);
  return m ? m[1] : null;
}

function buscarLlamadas(contenido, nombreFuncion) {
  const llamadas = [];
  const regex = new RegExp(
    "(^|[^.\\w])(" + nombreFuncion.replace(".", "\\.") + ")\\s*\\(",
    "g",
  );
  let m;
  while ((m = regex.exec(contenido)) !== null) {
    const idxApertura = m.index + m[0].length - 1;
    const idxCierre = encontrarCierre(contenido, idxApertura);
    if (idxCierre === -1) continue;

    const argsTexto = contenido.slice(idxApertura + 1, idxCierre);
    const args = splitArgsTopLevel(argsTexto);

    const resto = contenido.slice(idxCierre + 1, idxCierre + 40).trim();
    const encadenaSetOUpdate = /^\.\s*(set|update)\s*\(/.test(resto);

    const idxInicioLinea = contenido.lastIndexOf("\n", m.index) + 1;
    const lineaNum = contenido.slice(0, m.index).split("\n").length;
    const variable = detectarVariableAsignada(contenido, idxInicioLinea, m.index);

    llamadas.push({
      linea: lineaNum,
      idxCierre,
      args,
      argsCount: args.length,
      encadenaSetOUpdate,
      variable,
      textoCompleto: contenido.slice(m.index, Math.min(idxCierre + 1, m.index + 160)),
    });
  }
  return llamadas;
}

// Busca señales de uso tipo documento/colección cerca de la llamada:
// 1) en lo que sigue inmediatamente encadenado (.get().data(), etc)
// 2) en las próximas N líneas, sobre la variable asignada (si existe)
function analizarUso(contenido, llamada) {
  const idxDesde = llamada.idxCierre;

  // Ventana de contexto: nos quedamos DENTRO del mismo bloque { } donde
  // ocurrió la llamada. Si nos salimos del bloque (depth de llaves < 0)
  // paramos ahí, para no arrastrar variables con el mismo nombre de otra
  // función/caso más abajo en el archivo. LINEAS_CONTEXTO_ADELANTE actúa
  // como techo máximo por si el bloque es gigante.
  let idxHasta = idxDesde;
  let saltos = 0;
  let depth = 0;
  let quoteChar = null;
  while (idxHasta < contenido.length && saltos < LINEAS_CONTEXTO_ADELANTE) {
    const ch = contenido[idxHasta];
    if (quoteChar) {
      if (ch === quoteChar && contenido[idxHasta - 1] !== "\\") quoteChar = null;
    } else if (ch === '"' || ch === "'" || ch === "`") {
      quoteChar = ch;
    } else if (ch === "{") {
      depth++;
    } else if (ch === "}") {
      depth--;
      if (depth < 0) break; // salimos del bloque que contenía la llamada
    } else if (ch === "\n") {
      saltos++;
    }
    idxHasta++;
  }
  const ventanaCompleta = contenido.slice(idxDesde, idxHasta);

  // si hay variable, seguimos la cadena de alias:
  // const ref = tiendaCol(...); const snap = await ref.get(); snap.exists...
  let ventanaRelevante = ventanaCompleta;
  if (llamada.variable) {
    const lineasVentana = ventanaCompleta.split("\n");
    const alias = new Set([llamada.variable]);

    // hasta 3 pasadas para encontrar cadenas de reasignación tipo ref -> snap -> data
    for (let pasada = 0; pasada < 3; pasada++) {
      let agrego = false;
      for (const linea of lineasVentana) {
        for (const nombreAlias of Array.from(alias)) {
          const reAsign = new RegExp(
            "(?:const|let|var)\\s+(\\w+)\\s*=\\s*(?:await\\s+)?" + nombreAlias + "\\b",
          );
          const mAsign = linea.match(reAsign);
          if (mAsign && !alias.has(mAsign[1])) {
            alias.add(mAsign[1]);
            agrego = true;
          }
        }
      }
      if (!agrego) break;
    }

    const patronAlias = new RegExp("\\b(" + Array.from(alias).join("|") + ")\\b");
    const lineasConVar = lineasVentana.filter((l) => patronAlias.test(l));
    if (lineasConVar.length > 0) {
      ventanaRelevante = lineasConVar.join("\n");
    }
  }

  const hitDoc = SIGNALS_DOC.find((re) => re.test(ventanaRelevante));
  const hitCol = SIGNALS_COL.find((re) => re.test(ventanaRelevante));

  return {
    pareceDocumento: !!hitDoc && !hitCol,
    pareceColeccion: !!hitCol && !hitDoc,
    ambiguo: (!!hitDoc && !!hitCol) || (!hitDoc && !hitCol),
    señalDoc: hitDoc ? hitDoc.toString() : null,
    señalCol: hitCol ? hitCol.toString() : null,
  };
}

function evaluarLlamada(tipoFn, argsCount) {
  const argsExtra = Math.max(argsCount - 1, 0);
  const totalSegmentos = SEGMENTOS_FIJOS + argsExtra;
  const esImpar = totalSegmentos % 2 !== 0;
  let ok;
  if (tipoFn === "tiendaCol") ok = esImpar;
  else ok = !esImpar;
  return { totalSegmentos, esImpar, ok };
}

function main() {
  if (!fs.existsSync(ROOT)) {
    console.error(`❌ No existe la carpeta: ${ROOT}`);
    process.exit(1);
  }

  const archivos = listarArchivosJS(ROOT);
  console.log(`\n🔍 Escaneando ${archivos.length} archivos .js en "${ROOT}"...\n`);
  console.log("=".repeat(70));

  let totalLlamadas = 0;
  let totalErrores = 0;
  let totalNombreMal = 0;
  let totalFaltaArg = 0;
  let totalAmbiguo = 0;
  const resumenPorArchivo = [];

  for (const archivo of archivos) {
    const contenido = fs.readFileSync(archivo, "utf8");
    const imports = detectarImports(contenido);
    if (imports.length === 0) continue;

    const nombresFuncionesAUsar = [];
    for (const imp of imports) {
      if (imp.tipo === "alias") {
        nombresFuncionesAUsar.push(`${imp.nombre}.tiendaCol`);
        nombresFuncionesAUsar.push(`${imp.nombre}.tiendaDoc`);
      } else if (imp.tipo === "destructure") {
        if (imp.nombres.includes("tiendaCol")) nombresFuncionesAUsar.push("tiendaCol");
        if (imp.nombres.includes("tiendaDoc")) nombresFuncionesAUsar.push("tiendaDoc");
      }
    }
    if (nombresFuncionesAUsar.length === 0) continue;

    const hallazgos = [];

    for (const nombreFn of new Set(nombresFuncionesAUsar)) {
      const tipoFn = nombreFn.includes("tiendaDoc") ? "tiendaDoc" : "tiendaCol";
      const llamadas = buscarLlamadas(contenido, nombreFn);

      for (const llamada of llamadas) {
        totalLlamadas++;
        const evalRes = evaluarLlamada(tipoFn, llamada.argsCount);

        const problemas = [];
        let diagnostico = null;

        if (!evalRes.ok) {
          const uso = analizarUso(contenido, llamada);

          if (uso.pareceDocumento) {
            diagnostico = "NOMBRE_MAL";
            totalNombreMal++;
            problemas.push(
              `NOMBRE_MAL: llamaste a ${tipoFn} pero el código usa el resultado como DOCUMENTO ` +
                `(detecté "${uso.señalDoc}"). Solución: cambia ${tipoFn} por ${
                  tipoFn === "tiendaCol" ? "tiendaDoc" : "tiendaCol"
                }.`,
            );
          } else if (uso.pareceColeccion) {
            diagnostico = "FALTA_ARGUMENTO";
            totalFaltaArg++;
            problemas.push(
              `FALTA_ARGUMENTO: llamaste a ${tipoFn} y el código usa el resultado como COLECCIÓN ` +
                `(detecté "${uso.señalCol}"), pero el conteo de segmentos (${evalRes.totalSegmentos}) da par. ` +
                `Probablemente falta un argumento (subcolección). Revisa los args, no cambies el nombre de la función.`,
            );
          } else {
            diagnostico = "AMBIGUO";
            totalAmbiguo++;
            problemas.push(
              `AMBIGUO: ${evalRes.totalSegmentos} segmentos no calzan con ${tipoFn}, pero no encontré ` +
                `uso claro (.data/.exists/.set/.update vs .forEach/.docs/.where/.add) cerca. Revisar a mano.`,
            );
          }
        }

        if (tipoFn === "tiendaCol" && llamada.encadenaSetOUpdate) {
          problemas.push(
            `⚠️ ERROR GARANTIZADO: encadenaste .set()/.update() directo sobre tiendaCol(...) — ` +
              `CollectionReference NO tiene esos métodos.`,
          );
        }

        if (problemas.length > 0) totalErrores++;

        hallazgos.push({
          linea: llamada.linea,
          fn: nombreFn,
          args: llamada.args,
          totalSegmentos: evalRes.totalSegmentos,
          ok: problemas.length === 0,
          diagnostico,
          problemas,
          snippet: llamada.textoCompleto.replace(/\s+/g, " "),
        });
      }
    }

    if (hallazgos.length > 0) {
      resumenPorArchivo.push({ archivo, imports, hallazgos });
    }
  }

  for (const { archivo, imports, hallazgos } of resumenPorArchivo) {
    console.log(`\n📄 ${archivo}`);
    for (const imp of imports) {
      if (imp.tipo === "alias") {
        console.log(`   import: const ${imp.nombre} = require("${imp.modulo}")`);
      } else {
        console.log(`   import: const { ${imp.nombres.join(", ")} } = require("${imp.modulo}")`);
      }
    }
    for (const h of hallazgos) {
      const icono = h.ok ? "✅" : h.diagnostico === "NOMBRE_MAL" ? "🔴" : h.diagnostico === "FALTA_ARGUMENTO" ? "🟠" : "🟡";
      console.log(
        `   ${icono} línea ${h.linea} → ${h.fn}(${h.args.join(", ")})  [${h.totalSegmentos} segmentos]${h.diagnostico ? "  [" + h.diagnostico + "]" : ""}`,
      );
      if (!h.ok) {
        for (const p of h.problemas) console.log(`        ↳ ${p}`);
        console.log(`        ↳ código: ${h.snippet}...`);
      }
    }
  }

  console.log("\n" + "=".repeat(70));
  console.log(`\n📊 RESUMEN`);
  console.log(`   Archivos que usan el módulo de rutas: ${resumenPorArchivo.length}`);
  console.log(`   Llamadas totales a tiendaCol/tiendaDoc: ${totalLlamadas}`);
  console.log(`   Llamadas con posible error: ${totalErrores}`);
  console.log(`   🔴 Solo cambiar nombre (NOMBRE_MAL): ${totalNombreMal}`);
  console.log(`   🟠 Falta argumento (FALTA_ARGUMENTO): ${totalFaltaArg}`);
  console.log(`   🟡 Ambiguo, revisar a mano: ${totalAmbiguo}`);
  console.log("");

  if (totalErrores > 0) {
    console.log("⚠️  Revisa las líneas marcadas arriba antes de hacer deploy.\n");
    process.exit(1);
  } else {
    console.log("✅ No se detectaron inconsistencias.\n");
    process.exit(0);
  }
}

main();