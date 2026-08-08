#!/usr/bin/env node
/**
 * detectar_funciones_cambiadas.js
 * -------------------------------------------------------
 * Detecta qué Cloud Functions cambiaron desde el último deploy exitoso,
 * para no tener que desplegar TODO cada vez.
 *
 * CÓMO FUNCIONA:
 *   1. Lee el commit del último deploy exitoso desde el archivo
 *      ".last-deploy-commit" (si no existe, usa el primer commit del repo,
 *      o sea la primera vez marca TODO como cambiado — es lo seguro).
 *   2. Corre `git diff --name-only <ultimoDeploy>..HEAD` para saber
 *      qué archivos .js cambiaron.
 *   3. Escanea todos los .js del proyecto para mapear:
 *        - qué función (exports.NOMBRE = ...) está definida en qué archivo
 *        - qué archivos requiere cada archivo (require("./algo"))
 *   4. Marca una función como CAMBIADA si:
 *        a) el archivo donde está definida cambió, o
 *        b) cualquier archivo que ese archivo requiere (directa o
 *           indirectamente, ej. rutas.js) cambió.
 *   5. Imprime un JSON con el resultado, y opcionalmente filtra una
 *      lista de funciones que le pases (para usar desde el .ps1).
 *
 * USO:
 *   node detectar_funciones_cambiadas.js
 *     -> imprime TODAS las funciones detectadas y si cambiaron o no
 *
 *   node detectar_funciones_cambiadas.js --solo funcA,funcB,funcC
 *     -> imprime SOLO cuáles de esas cambiaron (una por línea),
 *        listas para pegar en --only functions:...
 *
 *   node detectar_funciones_cambiadas.js --marcar-deploy
 *     -> guarda el commit actual (HEAD) como "último deploy exitoso"
 *        Uso esto DESPUÉS de que el deploy real haya terminado bien.
 * -------------------------------------------------------
 */

const fs = require("fs");
const path = require("path");
const { execSync } = require("child_process");

const ROOT = process.cwd();
const ARCHIVO_ULTIMO_DEPLOY = path.join(ROOT, ".last-deploy-commit");
const IGNORAR_CARPETAS = new Set(["node_modules", ".git", "dist", "build"]);
const IGNORAR_ARCHIVOS = new Set(["detectar_funciones_cambiadas.js"]);

// ────────────────────────────────────────────────
function ejecutarGit(cmd) {
  try {
    return execSync(cmd, { cwd: ROOT, encoding: "utf8" }).trim();
  } catch (err) {
    return null;
  }
}

function obtenerCommitUltimoDeploy() {
  if (fs.existsSync(ARCHIVO_ULTIMO_DEPLOY)) {
    const commit = fs.readFileSync(ARCHIVO_ULTIMO_DEPLOY, "utf8").trim();
    // validar que el commit todavía existe en el historial
    const valido = ejecutarGit(`git cat-file -e ${commit}`);
    if (valido !== null) return commit;
  }
  // primera vez: usamos el primer commit del repo (root), así TODO
  // se marca como "cambiado" la primera vez que corres esto — es lo
  // seguro, ya que no sabemos qué se desplegó antes.
  const primerCommit = ejecutarGit("git rev-list --max-parents=0 HEAD");
  return primerCommit;
}

function marcarDeployActual() {
  const head = ejecutarGit("git rev-parse HEAD");
  if (!head) {
    console.error("❌ No se pudo obtener el commit actual (¿estás en un repo git?)");
    process.exit(1);
  }
  fs.writeFileSync(ARCHIVO_ULTIMO_DEPLOY, head + "\n", "utf8");
  console.log(`✅ Marcado como último deploy exitoso: ${head}`);
}

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

// exports.nombreFuncion = ...   ó   exports.nombreFuncion=onCall(...)
function detectarExports(contenido) {
  const nombres = [];
  const regex = /exports\.(\w+)\s*=/g;
  let m;
  while ((m = regex.exec(contenido)) !== null) {
    nombres.push(m[1]);
  }
  return nombres;
}

// require("./algo") o require("../algo") -> solo relativos (no node_modules)
function detectarRequiresRelativos(archivo, contenido) {
  const dirActual = path.dirname(archivo);
  const resultado = [];
  const regex = /require\(\s*["'](\.[^"']+)["']\s*\)/g;
  let m;
  while ((m = regex.exec(contenido)) !== null) {
    let resuelto = path.resolve(dirActual, m[1]);
    resultado.push(resuelto);
  }
  return resultado;
}

// intenta resolver una ruta de require a un archivo .js real en disco
function resolverArchivo(rutaBase) {
  const candidatos = [
    rutaBase,
    rutaBase + ".js",
    path.join(rutaBase, "index.js"),
  ];
  for (const c of candidatos) {
    if (fs.existsSync(c) && fs.statSync(c).isFile()) return path.normalize(c);
  }
  return null;
}

// ────────────────────────────────────────────────
function main() {
  const args = process.argv.slice(2);

  if (args.includes("--marcar-deploy")) {
    marcarDeployActual();
    return;
  }

  const idxSolo = args.indexOf("--solo");
  const funcionesFiltro =
    idxSolo !== -1 && args[idxSolo + 1]
      ? args[idxSolo + 1].split(",").map((s) => s.trim()).filter(Boolean)
      : null;

  const commitBase = obtenerCommitUltimoDeploy();
  if (!commitBase) {
    console.error("❌ No se pudo determinar un commit base. ¿Es un repo git válido?");
    process.exit(1);
  }

  const diffRaw = ejecutarGit(`git diff --name-only ${commitBase}..HEAD -- "*.js"`);
  // también incluir cambios sin commitear (working tree) para no llevarse sorpresas
  const diffSinCommitear = ejecutarGit(`git diff --name-only -- "*.js"`);
  const diffStaged = ejecutarGit(`git diff --name-only --cached -- "*.js"`);

  const archivosCambiadosSet = new Set(
    [diffRaw, diffSinCommitear, diffStaged]
      .filter(Boolean)
      .flatMap((s) => s.split("\n"))
      .filter(Boolean)
      .map((rel) => path.normalize(path.join(ROOT, rel))),
  );

  if (archivosCambiadosSet.size === 0) {
    console.log("ℹ️  No hay archivos .js cambiados desde el último deploy marcado.");
  }

  // Escanear todo el proyecto: función -> archivo, archivo -> requires
  const archivos = listarArchivosJS(ROOT);
  const funcionAArchivo = new Map(); // nombreFuncion -> archivo absoluto
  const archivoARequires = new Map(); // archivo absoluto -> [archivos requeridos]

  for (const archivo of archivos) {
    const contenido = fs.readFileSync(archivo, "utf8");

    const exportsEncontrados = detectarExports(contenido);
    for (const nombre of exportsEncontrados) {
      funcionAArchivo.set(nombre, archivo);
    }

    const requiresRelativos = detectarRequiresRelativos(archivo, contenido)
      .map(resolverArchivo)
      .filter(Boolean);
    archivoARequires.set(archivo, requiresRelativos);
  }

  // Para cada archivo, calculamos su cierre transitivo de dependencias
  // (a qué archivos requiere, directa o indirectamente)
  function cierreTransitivo(archivoInicial) {
    const visitados = new Set();
    const pila = [archivoInicial];
    while (pila.length > 0) {
      const actual = pila.pop();
      const requeridos = archivoARequires.get(actual) || [];
      for (const req of requeridos) {
        if (!visitados.has(req)) {
          visitados.add(req);
          pila.push(req);
        }
      }
    }
    return visitados;
  }

  // Determinar, para cada función, si cambió
  const resultado = {};
  for (const [nombreFuncion, archivoDefinicion] of funcionAArchivo.entries()) {
    if (funcionesFiltro && !funcionesFiltro.includes(nombreFuncion)) continue;

    let cambio = false;
    let razon = null;

    if (archivosCambiadosSet.has(path.normalize(archivoDefinicion))) {
      cambio = true;
      razon = `archivo propio cambió: ${path.relative(ROOT, archivoDefinicion)}`;
    } else {
      const dependencias = cierreTransitivo(archivoDefinicion);
      for (const dep of dependencias) {
        if (archivosCambiadosSet.has(path.normalize(dep))) {
          cambio = true;
          razon = `dependencia cambió: ${path.relative(ROOT, dep)}`;
          break;
        }
      }
    }

    resultado[nombreFuncion] = {
      archivo: path.relative(ROOT, archivoDefinicion),
      cambio,
      razon,
    };
  }

  // ── Salida ──
  if (funcionesFiltro) {
    // modo "--solo": imprime nombres separados por coma, listos para
    // pegar en --only functions:X,Y  (una función por línea a stderr
    // para debug, y la lista final en stdout)
    const cambiadas = funcionesFiltro.filter((f) => resultado[f] && resultado[f].cambio);
    const noEncontradas = funcionesFiltro.filter((f) => !resultado[f]);

    for (const f of funcionesFiltro) {
      if (!resultado[f]) {
        console.error(`   ⚠️  ${f}: no se encontró "exports.${f}" en ningún archivo`);
      } else if (resultado[f].cambio) {
        console.error(`   🔄 ${f}: CAMBIÓ (${resultado[f].razon})`);
      } else {
        console.error(`   ⏸️  ${f}: sin cambios`);
      }
    }

    // si alguna función del grupo no se pudo encontrar, la incluimos
    // igual como "cambiada" para no arriesgarnos a dejarla afuera
    const salida = Array.from(new Set([...cambiadas, ...noEncontradas]));
    console.log(salida.join(","));
  } else {
    console.log(JSON.stringify(resultado, null, 2));
  }
}

main();