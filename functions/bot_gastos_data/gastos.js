/**
 * 🤖 Bot de Telegram — Control de Gastos
 * Firebase Cloud Functions (2ª generación) — UN SOLO ARCHIVO, listo para deploy.
 * Conecta directamente con Firestore. Gráficos vía QuickChart.io (gratis, sin API key).
 *
 * COMANDOS
 *  /gasto monto categoria descripcion    -> registra un gasto
 *  /ingreso monto descripcion            -> registra un ingreso (resta del total)
 *  "50 comida almuerzo" (sin comando)    -> atajo rápido: monto + categoria + descripción
 *  /deshacer                             -> elimina el último movimiento
 *  /borrar id                            -> elimina un movimiento por id
 *  /hoy                                  -> total gastado hoy
 *  /semana                               -> reporte últimos 7 días + gráfico de barras
 *  /mes                                  -> reporte del mes actual + gráfico de torta
 *  /categorias                           -> gráfico de torta por categoría (mes actual)
 *  /promedio                             -> promedio diario de gasto (mes actual)
 *  /presupuesto monto                    -> define presupuesto mensual (activa alertas)
 *  /resumen                              -> resumen general (hoy + mes + presupuesto)
 *  /recordatorio dia texto               -> recordatorio mensual recurrente (ej: /recordatorio 15 Pagar internet)
 *  /recordatorios                        -> lista recordatorios activos
 *  /borrarrecordatorio id                -> elimina un recordatorio
 *  /deudas                               -> lista deudas pendientes (dinero que te deben)
 *  /ayuda o /start                       -> muestra esta ayuda
 *
 * MENÚ CON BOTONES — "✏️ Editar / Borrar"
 *  Elige un período (hoy/semana/mes/año/todos) -> ve la lista de movimientos de ese
 *  período (gastos e ingresos) -> toca uno -> edita el monto, la categoría, la
 *  descripción, o elimínalo con confirmación.
 *
 * MENÚ CON BOTONES — "🤝 Me deben"
 *  Ver deudas pendientes -> tocar una para ver detalle, marcarla pagada o eliminarla.
 *  "➕ Nueva deuda" -> flujo conversacional: nombre -> monto -> fecha/hora de aviso ->
 *  descripción. Al crearla se descuenta el monto de tu saldo (como un gasto) y se
 *  guarda la fecha en la que Telegram te va a recordar la deuda. Al marcarla pagada,
 *  se vuelve a sumar a tu saldo (como un ingreso).
 *
 * TAREAS PROGRAMADAS (zona horaria America/Lima)
 *  dailyCheck        -> todos los días 21:00, resumen del día + alertas de presupuesto + recordatorios de hoy
 *  weeklyReport      -> lunes 08:00, reporte de los últimos 7 días con gráfico
 *  monthlyReport     -> día 1 de cada mes 08:00, reporte del mes anterior con gráfico + reinicio de alertas
 *  debtReminderCheck -> cada 15 minutos, revisa si alguna deuda ya llegó a su fecha/hora de aviso
 *
 * VARIABLES DE ENTORNO (.env en la carpeta functions/)
 *  TOKEN_TELEGRAM_API_BOT_GASTOS     -> token del bot, dado por @BotFather
 *  TELEGRAM_CHAT_ID_BOT_GASTOS       -> tu chat id numérico de Telegram (único usuario autorizado)
 *  TELEGRAM_WEBHOOK_SECRET_BOT_GASTOS-> string aleatorio para validar que el webhook viene de Telegram
 */

// Forzamos que el proceso de Node corra en UTC. Esto es lo que ya hace por
// defecto Cloud Functions, pero lo dejamos explícito para que TODA la
// aritmética de fechas de este archivo (getDate, getDay, getHours, setHours,
// etc., que usan la zona horaria "local" del proceso) sea 100% predecible sin
// depender de configuración externa. A partir de aquí, "local" == "UTC".
process.env.TZ = "UTC";

const { onRequest } = require("firebase-functions/v2/https");
const { onSchedule } = require("firebase-functions/v2/scheduler");
const admin = require("firebase-admin");
const logger = require("firebase-functions/logger");

// No se llama admin.initializeApp() aquí: se asume que tu index.js principal
// ya inicializó la app (admin.initializeApp()) antes de requerir este archivo.
if (!admin.apps.length) {
  admin.initializeApp();
}
const db = admin.firestore();

// Todas las credenciales se leen desde el archivo .env (carpeta functions/).
// Firebase Functions v2 carga el .env automáticamente, tanto en el emulador
// como en el deploy real — no hace falta el paquete "dotenv".
const TELEGRAM_BOT_TOKEN = process.env.TOKEN_TELEGRAM_API_BOT_GASTOS;
const TELEGRAM_CHAT_ID = process.env.TELEGRAM_CHAT_ID_BOT_GASTOS;
const TELEGRAM_WEBHOOK_SECRET = process.env.TELEGRAM_WEBHOOK_SECRET_BOT_GASTOS;

if (!TELEGRAM_BOT_TOKEN || !TELEGRAM_CHAT_ID) {
  logger.warn(
    "⚠️ Faltan variables en tu .env: TOKEN_TELEGRAM_API_BOT_GASTOS y/o TELEGRAM_CHAT_ID_BOT_GASTOS. El bot no funcionará hasta que las definas."
  );
}

const TZ = "America/Lima";
const CURRENCY = "S/";
const DIAS = ["Dom", "Lun", "Mar", "Mié", "Jue", "Vie", "Sáb"];

// Perú vive siempre en UTC-5, todo el año (no tiene horario de verano).
// Esta constante es la clave para convertir correctamente entre "hora de
// pared en Lima" y el instante UTC real que se guarda en Firestore.
const LIMA_OFFSET_HOURS = 5;
const LIMA_OFFSET_MS = LIMA_OFFSET_HOURS * 60 * 60 * 1000;

// Consejos de ahorro por categoría (se muestran según en qué gastas más)
const CONSEJOS_POR_CATEGORIA = {
  comida: "🍔 *Comida*: cocina en casa y planea el menú semanal — puede bajar el gasto hasta 30%. Evita el delivery más de 2 veces por semana y lleva lonchera si trabajas fuera.",
  transporte: "🚗 *Transporte*: compara rutas, usa transporte público cuando puedas, o comparte viajes. Revisa el mantenimiento preventivo del auto para evitar gastos grandes.",
  ocio: "🎉 *Ocio*: define un tope mensual para salidas y entretenimiento. Revisa tus suscripciones (streaming, apps) y cancela las que no uses.",
  casa: "🏠 *Casa*: revisa tus gastos fijos (luz, agua, internet) y compara proveedores una vez al año. Apaga equipos que no uses para bajar el recibo de luz.",
  salud: "💊 *Salud*: aprovecha seguros, campañas de salud gratuitas y genéricos en farmacia. La medicina preventiva sale más barata que tratar algo avanzado.",
  otros: "📦 *Otros*: esta categoría suele esconder compras impulsivas. Antes de comprar algo de aquí, espera 24 horas y pregúntate si realmente lo necesitas.",
  ingreso: "",
};

// Guía de categorías sugeridas, para que siempre uses las mismas y no se mezclen
const GUIA_CATEGORIAS = {
  comida: "Supermercado, restaurantes, delivery, snacks, café",
  transporte: "Taxi/apps, gasolina, pasajes, mantenimiento del auto, peajes",
  ocio: "Cine, salidas, streaming, hobbies, viajes cortos",
  casa: "Alquiler, luz, agua, internet, muebles, artículos de limpieza",
  salud: "Farmacia, consultas médicas, seguros, gimnasio",
  otros: "Todo lo que no encaje claramente en las anteriores",
};

// ============================================================
// Utilidades de fecha / formato
// ============================================================

function fmt(n) {
  return `${CURRENCY} ${Number(n).toFixed(2)}`;
}

// Devuelve un objeto Date "de pared en Lima": sus campos (getFullYear,
// getMonth, getDate, getHours, getDay, ...) leídos con los getters LOCALES
// (que aquí son UTC, ver process.env.TZ arriba) coinciden exactamente con la
// fecha/hora que marcaría un reloj en Lima en este instante.
//
// OJO: el valor interno (epoch) de este objeto NO es el instante UTC real —
// está corrido. Por eso nunca se debe usar directamente para comparar con un
// Timestamp de Firestore. Para eso existe limaWallTimeToUTC() más abajo.
function todayInTZ() {
  return limaWallClockDate(new Date());
}

// Convierte un instante real (Date en UTC real) a su representación de
// "hora de pared en Lima" (ver comentario de todayInTZ). Usa Intl para leer
// los componentes de fecha/hora en la zona horaria de Lima de forma robusta,
// sin depender de parseos de strings ambiguos.
function limaWallClockDate(realDate) {
  const parts = new Intl.DateTimeFormat("en-US", {
    timeZone: TZ,
    year: "numeric",
    month: "2-digit",
    day: "2-digit",
    hour: "2-digit",
    minute: "2-digit",
    second: "2-digit",
    hour12: false,
  }).formatToParts(realDate);

  const get = (type) => parts.find((p) => p.type === type).value;
  let hour = parseInt(get("hour"), 10);
  if (hour === 24) hour = 0; // Intl a veces devuelve "24" para la medianoche

  return new Date(
    Date.UTC(
      parseInt(get("year"), 10),
      parseInt(get("month"), 10) - 1,
      parseInt(get("day"), 10),
      hour,
      parseInt(get("minute"), 10),
      parseInt(get("second"), 10)
    )
  );
}

// Inversa de limaWallTimeToUTC: a partir de un Date real (UTC verdadero),
// devuelve su representación "de pared en Lima" (mismo formato que
// todayInTZ()/limaWallClockDate). Se usa para agrupar movimientos reales por
// día calendario de Lima (ej. en el reporte semanal).
function utcToLimaWallTime(realDate) {
  return new Date(realDate.getTime() - LIMA_OFFSET_MS);
}

// Convierte una fecha "de pared en Lima" (la que devuelven todayInTZ(),
// startOfDay(), addDays(), startOfMonth(), etc.) al instante UTC REAL
// correspondiente. Este es el paso que faltaba antes de mandar cualquier
// fecha a Firestore (Timestamp.fromDate) — sin esto, las consultas de rango
// quedaban corridas por las 5 horas de diferencia Lima/UTC, y por eso los
// movimientos de la noche (aprox. 7pm a 12am hora Lima) se colaban al día
// siguiente en los reportes y en editar/borrar.
function limaWallTimeToUTC(fakeLocalDate) {
  return new Date(fakeLocalDate.getTime() + LIMA_OFFSET_MS);
}

function startOfDay(d) {
  const x = new Date(d);
  x.setHours(0, 0, 0, 0);
  return x;
}

function addDays(d, days) {
  const x = new Date(d);
  x.setDate(x.getDate() + days);
  return x;
}

function startOfMonth(d) {
  return new Date(d.getFullYear(), d.getMonth(), 1);
}

function startOfNextMonth(d) {
  return new Date(d.getFullYear(), d.getMonth() + 1, 1);
}

function startOfPrevMonth(d) {
  return new Date(d.getFullYear(), d.getMonth() - 1, 1);
}

function startOfYear(d) {
  return new Date(d.getFullYear(), 0, 1);
}

function startOfNextYear(d) {
  return new Date(d.getFullYear() + 1, 0, 1);
}

// Formatea una fecha corta (DD/MM) o larga, siempre en hora de Lima, a partir
// de un Date real (por ejemplo el que sale de un Timestamp.toDate()).
function fechaCortaLima(realDate) {
  return realDate.toLocaleDateString("es-PE", { day: "2-digit", month: "2-digit", timeZone: TZ });
}

function fechaLargaLima(realDate) {
  return realDate.toLocaleDateString("es-PE", { day: "2-digit", month: "2-digit", year: "numeric", timeZone: TZ });
}

function fechaHoraLima(realDate) {
  return realDate.toLocaleString("es-PE", { dateStyle: "medium", timeStyle: "short", timeZone: TZ });
}

// ============================================================
// Telegram helpers
// ============================================================

async function tgCall(token, method, payload) {
  const res = await fetch(`https://api.telegram.org/bot${token}/${method}`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(payload),
  });
  return res.json();
}

async function tgSend(token, chatId, text) {
  return tgCall(token, "sendMessage", { chat_id: chatId, text, parse_mode: "Markdown" });
}

async function tgSendPhoto(token, chatId, photoUrl, caption) {
  return tgCall(token, "sendPhoto", { chat_id: chatId, photo: photoUrl, caption, parse_mode: "Markdown" });
}

async function tgSendKeyboard(token, chatId, text, keyboard) {
  return tgCall(token, "sendMessage", { chat_id: chatId, text, parse_mode: "Markdown", reply_markup: keyboard });
}

async function tgAnswerCallback(token, callbackQueryId, text) {
  return tgCall(token, "answerCallbackQuery", { callback_query_id: callbackQueryId, text });
}

// ============================================================
// Botones (teclados inline de Telegram)
// ============================================================

function mainMenuKeyboard() {
  return {
    inline_keyboard: [
      [{ text: "➕ Agregar gasto", callback_data: "menu_gasto" }, { text: "✏️ Editar / Borrar", callback_data: "menu_borrar_gasto" }],
      [{ text: "📅 Hoy", callback_data: "menu_hoy" }, { text: "📊 Semana", callback_data: "menu_semana" }, { text: "📆 Mes", callback_data: "menu_mes" }],
      [{ text: "🥧 Categorías", callback_data: "menu_categorias" }, { text: "📈 Promedio", callback_data: "menu_promedio" }],
      [{ text: "💰 Presupuesto", callback_data: "menu_presupuesto" }, { text: "🔔 Recordatorios", callback_data: "menu_recordatorios" }],
      [{ text: "🤝 Me deben", callback_data: "menu_deudas" }],
      [{ text: "📋 Resumen", callback_data: "menu_resumen" }, { text: "❓ Ayuda", callback_data: "menu_ayuda" }],
    ],
  };
}

function categoriasKeyboard() {
  return {
    inline_keyboard: [
      [{ text: "🍔 Comida", callback_data: "cat_comida" }, { text: "🚗 Transporte", callback_data: "cat_transporte" }],
      [{ text: "🎉 Ocio", callback_data: "cat_ocio" }, { text: "🏠 Casa", callback_data: "cat_casa" }],
      [{ text: "💊 Salud", callback_data: "cat_salud" }, { text: "📦 Otros", callback_data: "cat_otros" }],
      [{ text: "✏️ Escribir categoría", callback_data: "cat_custom" }],
      [{ text: "❌ Cancelar", callback_data: "cancelar" }],
    ],
  };
}

// Teclado de categorías usado SOLO al editar un movimiento existente.
// Codifica el id del movimiento en el callback_data (medit_catset_<id>_<categoria>).
function categoriasEditKeyboard(id) {
  return {
    inline_keyboard: [
      [{ text: "🍔 Comida", callback_data: `medit_catset_${id}_comida` }, { text: "🚗 Transporte", callback_data: `medit_catset_${id}_transporte` }],
      [{ text: "🎉 Ocio", callback_data: `medit_catset_${id}_ocio` }, { text: "🏠 Casa", callback_data: `medit_catset_${id}_casa` }],
      [{ text: "💊 Salud", callback_data: `medit_catset_${id}_salud` }, { text: "📦 Otros", callback_data: `medit_catset_${id}_otros` }],
      [{ text: "✏️ Escribir categoría", callback_data: `medit_catcustom_${id}` }],
      [{ text: "⬅️ Cancelar", callback_data: `movsel_${id}` }],
    ],
  };
}

// Selector de período para el flujo de editar/borrar movimientos.
function periodoBorrarKeyboard() {
  return {
    inline_keyboard: [
      [{ text: "📅 Hoy", callback_data: "mbp_hoy" }, { text: "🗓️ Esta semana", callback_data: "mbp_semana" }],
      [{ text: "📆 Este mes", callback_data: "mbp_mes" }, { text: "📈 Este año", callback_data: "mbp_anio" }],
      [{ text: "🗂️ Todos (últimos 20)", callback_data: "mbp_todos" }],
      [{ text: "❌ Cancelar", callback_data: "cancelar" }],
    ],
  };
}

// Lista de movimientos de un período, uno por botón, para elegir cuál editar/borrar.
function movimientosListKeyboard(movs) {
  const kb = { inline_keyboard: [] };
  movs.slice(0, 20).forEach((m) => {
    const signo = m.tipo === "ingreso" ? "➕" : "➖";
    const fechaCorta = fechaCortaLima(m.fecha.toDate());
    const etiqueta = `${signo} ${fmt(m.monto)} · ${m.categoria} · ${fechaCorta}${m.descripcion ? " · " + m.descripcion : ""}`;
    kb.inline_keyboard.push([{ text: etiqueta.slice(0, 60), callback_data: `movsel_${m.id}` }]);
  });
  kb.inline_keyboard.push([{ text: "⬅️ Cambiar período", callback_data: "menu_borrar_gasto" }]);
  kb.inline_keyboard.push([{ text: "🏠 Menú", callback_data: "menu" }]);
  return kb;
}

// Acciones disponibles para un movimiento ya seleccionado.
function movimientoDetalleKeyboard(id) {
  return {
    inline_keyboard: [
      [{ text: "✏️ Editar monto", callback_data: `medit_monto_${id}` }],
      [{ text: "🏷️ Editar categoría", callback_data: `medit_cat_${id}` }],
      [{ text: "📝 Editar descripción", callback_data: `medit_desc_${id}` }],
      [{ text: "🗑️ Eliminar", callback_data: `medit_delete_${id}` }],
      [{ text: "⬅️ Volver a la lista", callback_data: "menu_borrar_gasto" }],
    ],
  };
}

function confirmKeyboard(yesData) {
  return { inline_keyboard: [[{ text: "✅ Sí, eliminar", callback_data: yesData }, { text: "❌ No", callback_data: "cancelar" }]] };
}

// Lista de deudas pendientes, una por botón, para elegir cuál ver/editar/pagar.
function deudasKeyboard(deudas) {
  const kb = { inline_keyboard: [] };
  deudas.slice(0, 20).forEach((d) => {
    const fechaCorta = fechaCortaLima(d.fechaRecordatorio.toDate());
    const etiqueta = `${d.persona} · ${fmt(d.monto)} · aviso ${fechaCorta}`;
    kb.inline_keyboard.push([{ text: etiqueta.slice(0, 60), callback_data: `deudasel_${d.id}` }]);
  });
  kb.inline_keyboard.push([{ text: "➕ Nueva deuda", callback_data: "menu_add_deuda" }]);
  kb.inline_keyboard.push([{ text: "🏠 Menú", callback_data: "menu" }]);
  return kb;
}

// Acciones disponibles para una deuda ya seleccionada.
function deudaDetalleKeyboard(id) {
  return {
    inline_keyboard: [
      [{ text: "✅ Marcar como pagada", callback_data: `deuda_pagar_${id}` }],
      [{ text: "🗑️ Eliminar", callback_data: `deuda_del_${id}` }],
      [{ text: "⬅️ Volver a la lista", callback_data: "menu_deudas" }],
    ],
  };
}

function textoAyuda() {
  return (
    "🤖 *Bot de Control de Gastos*\n\n" +
    "Puedes usar el botón *Menú* (o escribe /menu) para todo con botones, o estos comandos:\n\n" +
    "*/gasto* `monto categoria descripcion` — registra un gasto\n" +
    "*/ingreso* `monto descripcion` — registra un ingreso\n" +
    "`monto categoria descripcion` — atajo rápido, sin comando\n" +
    "*/deshacer* — elimina el último movimiento\n" +
    "*/borrar* `id` — elimina un movimiento\n" +
    "*/hoy* — total de hoy\n" +
    "*/semana* — reporte últimos 7 días + gráfico\n" +
    "*/mes* — reporte del mes + gráfico\n" +
    "*/categorias* — gráfico por categoría\n" +
    "*/promedio* — promedio diario del mes\n" +
    "*/presupuesto* `monto` — define presupuesto mensual\n" +
    "*/resumen* — resumen general\n" +
    "*/recordatorio* `dia texto` — recordatorio mensual (ej: /recordatorio 15 Pagar internet)\n" +
    "*/recordatorios* — lista recordatorios\n" +
    "*/borrarrecordatorio* `id` — elimina recordatorio\n" +
    "*/deudas* — lista deudas pendientes (lo que te deben)\n" +
    "*/menu* — muestra el menú con botones\n\n" +
    "✏️ *Editar / Borrar* (desde el menú): elige un período (hoy, semana, mes, año o todos), toca el movimiento y podrás cambiar el monto, la categoría, la descripción, o eliminarlo.\n\n" +
    "🤝 *Me deben* (desde el menú): registra dinero que te deben. Se descuenta de tu saldo al crearla y te llega un recordatorio en la fecha/hora que elijas."
  );
}

// ============================================================
// QuickChart.io (gráficos gratis vía URL, sin API key)
// ============================================================

function quickChartUrl(config) {
  const encoded = encodeURIComponent(JSON.stringify(config));
  return `https://quickchart.io/chart?c=${encoded}&backgroundColor=white&width=600&height=350`;
}

function barChart(title, labels, data) {
  return quickChartUrl({
    type: "bar",
    data: { labels, datasets: [{ label: "Gastos", data, backgroundColor: "#4e79a7" }] },
    options: { plugins: { title: { display: true, text: title }, legend: { display: false } } },
  });
}

function pieChart(title, labelsObj) {
  return quickChartUrl({
    type: "pie",
    data: { labels: Object.keys(labelsObj), datasets: [{ data: Object.values(labelsObj) }] },
    options: { plugins: { title: { display: true, text: title } } },
  });
}

// ============================================================
// Firestore: movimientos, presupuesto, recordatorios, deudas
// ============================================================

const movsRef = db.collection("movimientos");
const configRef = db.collection("config").doc("presupuesto");
const limiteDiarioRef = db.collection("config").doc("limiteDiario");
const alertaSaldoRef = db.collection("config").doc("alertaSaldo");
const remindersRef = db.collection("recordatorios");
const estadoRef = db.collection("estado").doc("pendiente");
const deudasRef = db.collection("deudas");

// Estado de conversación: cuando un botón pide "envía el monto" o "envía el texto",
// se guarda aquí qué está esperando el bot para el próximo mensaje de texto.
async function getEstado() {
  const snap = await estadoRef.get();
  return snap.exists ? snap.data() : null;
}

async function setEstado(obj) {
  await estadoRef.set(obj);
}

async function clearEstado() {
  await estadoRef.delete().catch(() => {});
}

async function addMovimiento({ tipo, monto, categoria, descripcion }) {
  const doc = await movsRef.add({
    tipo, // "gasto" | "ingreso"
    monto: Math.abs(monto),
    categoria: (categoria || "otros").toLowerCase(),
    descripcion: descripcion || "",
    fecha: admin.firestore.Timestamp.now(),
  });
  return doc.id;
}

// desde/hasta deben ser fechas "de pared en Lima" (las que produce
// todayInTZ()/startOfDay()/addDays()/startOfMonth()/etc.). Aquí, y solo aquí,
// se convierten al instante UTC real antes de consultar Firestore — este es
// el punto donde antes se perdían las 5 horas de diferencia y por eso los
// movimientos de la noche se colaban al día siguiente.
async function getMovimientos(desde, hasta) {
  const desdeUTC = limaWallTimeToUTC(desde);
  const hastaUTC = limaWallTimeToUTC(hasta);
  const snap = await movsRef
    .where("fecha", ">=", admin.firestore.Timestamp.fromDate(desdeUTC))
    .where("fecha", "<", admin.firestore.Timestamp.fromDate(hastaUTC))
    .orderBy("fecha", "desc")
    .get();
  return snap.docs.map((d) => ({ id: d.id, ...d.data() }));
}

// Trae los movimientos (gastos e ingresos) de un período dado, para el flujo de
// editar/borrar. IMPORTANTE: a propósito NO se filtra por "tipo" dentro de la
// consulta a Firestore (eso exigiría un índice compuesto tipo+fecha que no
// tienes creado, y era la causa de que el botón "no hiciera nada"). En vez de
// eso, se filtra por fecha (rango sobre un único campo, que Firestore indexa
// solo) y el filtrado por tipo/categoría se hace en memoria si hace falta.
async function getMovimientosPorPeriodo(periodo, now) {
  if (periodo === "todos") {
    // Sin filtro de fecha: traemos directamente los más recientes.
    const snap = await movsRef.orderBy("fecha", "desc").limit(20).get();
    return snap.docs.map((d) => ({ id: d.id, ...d.data() }));
  }

  let desde, hasta;
  switch (periodo) {
    case "hoy":
      desde = startOfDay(now);
      hasta = addDays(desde, 1);
      break;
    case "semana":
      hasta = addDays(startOfDay(now), 1);
      desde = addDays(hasta, -7);
      break;
    case "mes":
      desde = startOfMonth(now);
      hasta = startOfNextMonth(now);
      break;
    case "anio":
      desde = startOfYear(now);
      hasta = startOfNextYear(now);
      break;
    default:
      desde = startOfDay(now);
      hasta = addDays(desde, 1);
  }
  return getMovimientos(desde, hasta);
}

function netTotal(movs) {
  return movs.reduce((acc, m) => acc + (m.tipo === "ingreso" ? -m.monto : m.monto), 0);
}

async function getBudget() {
  const snap = await configRef.get();
  return snap.exists ? snap.data() : { monto: 0, alertados: [] };
}

async function setBudget(monto) {
  await configRef.set({ monto, alertados: [] }, { merge: true });
}

async function checkBudgetAlert(token, chatId) {
  const budget = await getBudget();
  if (!budget.monto) return;
  const now = todayInTZ();
  const movs = await getMovimientos(startOfMonth(now), startOfNextMonth(now));
  const total = netTotal(movs);
  const pct = total / budget.monto;
  const alertados = budget.alertados || [];
  const hitos = [0.5, 0.8, 1.0, 1.2];
  let cambiado = false;
  for (const hito of hitos) {
    if (pct >= hito && !alertados.includes(hito)) {
      const emoji = hito >= 1 ? "🚨" : "⚠️";
      await tgSend(
        token,
        chatId,
        `${emoji} Has usado el *${Math.round(hito * 100)}%* de tu presupuesto mensual (${fmt(total)} de ${fmt(budget.monto)}).`
      );
      alertados.push(hito);
      cambiado = true;
    }
  }
  if (cambiado) await configRef.set({ alertados }, { merge: true });
}

// ---------- Límite diario de gasto ----------

async function getLimiteDiario() {
  const snap = await limiteDiarioRef.get();
  return snap.exists ? snap.data() : { monto: 0, alertadoFecha: null };
}

async function setLimiteDiario(monto) {
  await limiteDiarioRef.set({ monto, alertadoFecha: null }, { merge: true });
}

async function checkLimiteDiarioAlert(token, chatId) {
  const config = await getLimiteDiario();
  if (!config.monto) return;
  const now = todayInTZ();
  const fechaHoy = startOfDay(now).toDateString();
  if (config.alertadoFecha === fechaHoy) return; // ya se avisó hoy

  const movs = await getMovimientos(startOfDay(now), addDays(startOfDay(now), 1));
  const totalHoy = movs.filter((m) => m.tipo === "gasto").reduce((a, m) => a + m.monto, 0);

  if (totalHoy >= config.monto) {
    await tgSend(
      token,
      chatId,
      `⚡ *Límite diario alcanzado.* Hoy llevas gastado ${fmt(totalHoy)} de tu límite de ${fmt(config.monto)}.`
    );
    await limiteDiarioRef.set({ alertadoFecha: fechaHoy }, { merge: true });
  }
}

// ---------- Saldo y alerta de saldo bajo ----------

async function getSaldoActual() {
  const snap = await movsRef.get();
  return snap.docs.reduce((acc, d) => {
    const m = d.data();
    return acc + (m.tipo === "ingreso" ? m.monto : -m.monto);
  }, 0);
}

async function getAlertaSaldo() {
  const snap = await alertaSaldoRef.get();
  return snap.exists ? snap.data() : { monto: 0, alertado: false };
}

async function setAlertaSaldo(monto) {
  await alertaSaldoRef.set({ monto, alertado: false }, { merge: true });
}

async function checkSaldoBajoAlert(token, chatId) {
  const config = await getAlertaSaldo();
  if (!config.monto) return;
  const saldo = await getSaldoActual();

  if (saldo <= config.monto && !config.alertado) {
    await tgSend(
      token,
      chatId,
      `🚨 *Saldo bajo.* Tu saldo actual es ${fmt(saldo)}, por debajo de tu límite de ${fmt(config.monto)}. Considera frenar gastos no esenciales.`
    );
    await alertaSaldoRef.set({ alertado: true }, { merge: true });
  } else if (saldo > config.monto * 1.2 && config.alertado) {
    // el saldo se recuperó lo suficiente: se puede volver a avisar si baja de nuevo
    await alertaSaldoRef.set({ alertado: false }, { merge: true });
  }
}

// ---------- Combinación de todas las alertas tras registrar un gasto ----------

async function runAlertasGasto(token, chatId) {
  await checkBudgetAlert(token, chatId);
  await checkLimiteDiarioAlert(token, chatId);
  await checkSaldoBajoAlert(token, chatId);
}

// ---------- Consejos de ahorro ----------

async function reporteConsejos(token, chatId, now) {
  const movs = await getMovimientos(startOfMonth(now), startOfNextMonth(now));
  const porCategoria = {};
  movs.forEach((m) => {
    if (m.tipo === "ingreso") return;
    porCategoria[m.categoria] = (porCategoria[m.categoria] || 0) + m.monto;
  });

  const ordenado = Object.entries(porCategoria).sort((a, b) => b[1] - a[1]);
  let txt = "💡 *Consejos de ahorro*\n\n";

  if (ordenado.length === 0) {
    txt += "Aún no tienes gastos este mes. ¡Vas muy bien! Registra tus gastos para recibir consejos personalizados.";
    await tgSend(token, chatId, txt);
    return;
  }

  const [topCategoria, topMonto] = ordenado[0];
  txt += `Tu categoría con más gasto este mes es *${topCategoria}* (${fmt(topMonto)}).\n\n`;
  txt += CONSEJOS_POR_CATEGORIA[topCategoria] || CONSEJOS_POR_CATEGORIA.otros;

  if (ordenado.length > 1) {
    const [segCategoria] = ordenado[1];
    txt += `\n\n${CONSEJOS_POR_CATEGORIA[segCategoria] || ""}`;
  }

  const budget = await getBudget();
  if (budget.monto) {
    const total = netTotal(movs);
    const pct = (total / budget.monto) * 100;
    if (pct >= 80) {
      txt += `\n\n⚠️ Ya usaste el ${pct.toFixed(0)}% de tu presupuesto mensual — revisa tus próximos gastos.`;
    }
  }

  await tgSend(token, chatId, txt);
}

function textoGuiaCategorias() {
  let txt = "📖 *Guía de categorías*\nÚsalas siempre así para que tus reportes salgan ordenados:\n\n";
  Object.entries(GUIA_CATEGORIAS).forEach(([cat, desc]) => {
    txt += `*${cat}*: ${desc}\n`;
  });
  return txt;
}

// ============================================================
// Reportes
// ============================================================

async function reporteHoy(now) {
  const desde = startOfDay(now);
  const hasta = addDays(desde, 1);
  const movs = await getMovimientos(desde, hasta);
  const total = netTotal(movs);
  let txt = `📅 *Hoy* — ${fmt(total)}\n`;
  if (movs.length === 0) {
    txt += "_Sin movimientos aún._";
  } else {
    movs.forEach((m) => {
      const signo = m.tipo === "ingreso" ? "➕" : "➖";
      txt += `${signo} ${fmt(m.monto)} · ${m.categoria}${m.descripcion ? " · " + m.descripcion : ""} _(${m.id.slice(0, 6)})_\n`;
    });
  }
  return txt;
}

async function reporteSemana(token, chatId, now) {
  const hasta = addDays(startOfDay(now), 1);
  const desde = addDays(hasta, -7);
  const movs = await getMovimientos(desde, hasta);

  const porDia = {};
  for (let i = 0; i < 7; i++) {
    const d = addDays(desde, i);
    porDia[d.toDateString()] = 0;
  }
  movs.forEach((m) => {
    // m.fecha es un Timestamp real (UTC). Lo pasamos a "hora de pared en
    // Lima" para que la clave coincida con las claves de porDia (que también
    // están en ese mismo espacio "de pared en Lima"). Antes se comparaba
    // día-calendario-UTC contra día-calendario-Lima y el gráfico agrupaba mal
    // los movimientos cercanos a la medianoche.
    const key = utcToLimaWallTime(m.fecha.toDate()).toDateString();
    const val = m.tipo === "ingreso" ? -m.monto : m.monto;
    if (key in porDia) porDia[key] += val;
  });

  const labels = Object.keys(porDia).map((k) => {
    const d = new Date(k);
    return `${DIAS[d.getDay()]} ${d.getDate()}`;
  });
  const data = Object.values(porDia).map((v) => Number(v.toFixed(2)));
  const total = data.reduce((a, b) => a + b, 0);
  const promedio = total / 7;

  const porCategoria = {};
  movs.forEach((m) => {
    if (m.tipo === "ingreso") return;
    porCategoria[m.categoria] = (porCategoria[m.categoria] || 0) + m.monto;
  });
  const topCat = Object.entries(porCategoria).sort((a, b) => b[1] - a[1])[0];

  let txt = `📊 *Últimos 7 días*\nTotal: ${fmt(total)}\nPromedio diario: ${fmt(promedio)}\n`;
  if (topCat) txt += `Mayor categoría: *${topCat[0]}* (${fmt(topCat[1])})\n`;

  await tgSend(token, chatId, txt);
  await tgSendPhoto(token, chatId, barChart("Gastos por día (últimos 7 días)", labels, data), "Gastos últimos 7 días");
}

async function reporteMesGenerico(token, chatId, desde, hasta, titulo, diasPromedio) {
  const movs = await getMovimientos(desde, hasta);
  const total = netTotal(movs);
  const promedio = total / diasPromedio;

  const budget = await getBudget();
  let presupuestoTxt = "";
  if (budget.monto) {
    const restante = budget.monto - total;
    const pct = ((total / budget.monto) * 100).toFixed(1);
    presupuestoTxt = `\nPresupuesto: ${fmt(budget.monto)} (usado ${pct}%)\nRestante: ${fmt(restante)}`;
  }

  const porCategoria = {};
  movs.forEach((m) => {
    if (m.tipo === "ingreso") return;
    porCategoria[m.categoria] = (porCategoria[m.categoria] || 0) + m.monto;
  });

  const txt = `📆 *${titulo}*\nTotal gastado: ${fmt(total)}\nPromedio diario: ${fmt(promedio)}${presupuestoTxt}`;
  await tgSend(token, chatId, txt);
  if (Object.keys(porCategoria).length > 0) {
    await tgSendPhoto(token, chatId, pieChart(`Gastos por categoría — ${titulo}`, porCategoria), "Distribución por categoría");
  }
}

async function reporteCategorias(token, chatId, now) {
  const desde = startOfMonth(now);
  const hasta = startOfNextMonth(now);
  const movs = await getMovimientos(desde, hasta);
  const porCategoria = {};
  movs.forEach((m) => {
    if (m.tipo === "ingreso") return;
    porCategoria[m.categoria] = (porCategoria[m.categoria] || 0) + m.monto;
  });
  if (Object.keys(porCategoria).length === 0) {
    await tgSend(token, chatId, "No hay gastos este mes todavía.");
    return;
  }
  await tgSendPhoto(token, chatId, pieChart("Gastos por categoría (mes actual)", porCategoria), "Distribución por categoría");
}

// ============================================================
// Recordatorios
// ============================================================

async function addReminder(dia, descripcion) {
  return remindersRef.add({ dia, descripcion, creado: admin.firestore.Timestamp.now() });
}

async function listReminders() {
  const snap = await remindersRef.orderBy("dia").get();
  return snap.docs.map((d) => ({ id: d.id, ...d.data() }));
}

async function checkTodayReminders(token, chatId, now) {
  const dia = now.getDate();
  const snap = await remindersRef.where("dia", "==", dia).get();
  for (const doc of snap.docs) {
    const r = doc.data();
    await tgSend(token, chatId, `🔔 *Recordatorio de hoy:* ${r.descripcion}`);
  }
}

// ============================================================
// Deudas (dinero que te deben)
// ============================================================

async function addDeuda({ persona, monto, descripcion, fechaRecordatorio }) {
  // Registra un "gasto" para descontar el dinero prestado de tu saldo actual.
  const movId = await addMovimiento({
    tipo: "gasto",
    monto,
    categoria: "prestamo",
    descripcion: `Préstamo a ${persona}${descripcion ? " — " + descripcion : ""}`,
  });
  const doc = await deudasRef.add({
    persona,
    monto: Math.abs(monto),
    descripcion: descripcion || "",
    fechaRecordatorio: admin.firestore.Timestamp.fromDate(fechaRecordatorio),
    recordatorioEnviado: false,
    pagada: false,
    movimientoId: movId,
    creado: admin.firestore.Timestamp.now(),
  });
  return doc.id;
}

async function listDeudas(soloPendientes = true) {
  const snap = await deudasRef.orderBy("fechaRecordatorio", "asc").get();
  let list = snap.docs.map((d) => ({ id: d.id, ...d.data() }));
  if (soloPendientes) list = list.filter((d) => !d.pagada);
  return list;
}

async function marcarDeudaPagada(id) {
  const doc = await deudasRef.doc(id).get();
  if (!doc.exists) return null;
  const d = doc.data();
  // Al pagarse, se devuelve el dinero a tu saldo como ingreso.
  await addMovimiento({
    tipo: "ingreso",
    monto: d.monto,
    categoria: "prestamo",
    descripcion: `Pago de deuda de ${d.persona}`,
  });
  await deudasRef.doc(id).update({ pagada: true });
  return d;
}

async function deleteDeuda(id) {
  await deudasRef.doc(id).delete().catch(() => {});
}

async function checkRecordatoriosDeuda(token, chatId) {
  const now = admin.firestore.Timestamp.now();
  const snap = await deudasRef
    .where("pagada", "==", false)
    .where("recordatorioEnviado", "==", false)
    .where("fechaRecordatorio", "<=", now)
    .get();
  for (const doc of snap.docs) {
    const d = doc.data();
    await tgSendKeyboard(
      token,
      chatId,
      `💸 *Recordatorio de deuda*\n${d.persona} te debe ${fmt(d.monto)}${d.descripcion ? " — " + d.descripcion : ""}`,
      { inline_keyboard: [[{ text: "✅ Marcar como pagada", callback_data: `deuda_pagar_${doc.id}` }]] }
    );
    await doc.ref.update({ recordatorioEnviado: true });
  }
}

// ============================================================
// Router de comandos
// ============================================================

function parseMontoCategoria(text) {
  const m = text.trim().match(/^(-?\d+(?:\.\d+)?)\s+(\S+)\s*(.*)$/);
  if (!m) return null;
  return { monto: parseFloat(m[1]), categoria: m[2].toLowerCase(), descripcion: m[3] || "" };
}

async function handleCommand(token, chatId, rawText) {
  const now = todayInTZ();
  const text = rawText.trim();

  // Atajo rápido sin "/": "50 comida almuerzo", "-30 reembolso", "50"
  if (!text.startsWith("/")) {
    const m = text.match(/^(-?\d+(?:\.\d+)?)(?:\s+(\S+))?(?:\s+(.*))?$/);
    if (m) {
      const monto = parseFloat(m[1]);
      const categoria = (m[2] || "otros").toLowerCase();
      const descripcion = m[3] || "";
      const tipo = monto < 0 ? "ingreso" : "gasto";
      const id = await addMovimiento({ tipo, monto, categoria, descripcion });
      await tgSend(
        token,
        chatId,
        `✅ ${tipo === "gasto" ? "Gasto" : "Ingreso"} registrado: ${fmt(Math.abs(monto))} · ${categoria} _(${id.slice(0, 6)})_`
      );
      if (tipo === "gasto") await runAlertasGasto(token, chatId);
      return;
    }
    await tgSend(token, chatId, "No entendí. Escribe /ayuda para ver los comandos.");
    return;
  }

  const [cmdRaw] = text.split(/\s+/);
  const cmd = cmdRaw.toLowerCase();
  const rest = text.slice(cmdRaw.length).trim();

  switch (cmd) {
    case "/start":
      await tgSendKeyboard(token, chatId, "👋 ¡Hola! Soy tu bot de gastos. Toca una opción:", mainMenuKeyboard());
      break;

    case "/menu":
      await tgSendKeyboard(token, chatId, "🏠 *Menú principal* — toca una opción:", mainMenuKeyboard());
      break;

    case "/ayuda":
      await tgSend(token, chatId, textoAyuda());
      break;

    case "/gasto": {
      const parsed = parseMontoCategoria(rest);
      if (!parsed) {
        await tgSend(token, chatId, "Uso: /gasto monto categoria descripcion");
        break;
      }
      const id = await addMovimiento({ tipo: "gasto", monto: parsed.monto, categoria: parsed.categoria, descripcion: parsed.descripcion });
      await tgSend(token, chatId, `✅ Gasto registrado: ${fmt(parsed.monto)} · ${parsed.categoria} _(${id.slice(0, 6)})_`);
      await runAlertasGasto(token, chatId);
      break;
    }

    case "/ingreso": {
      const m = rest.match(/^(\d+(?:\.\d+)?)\s*(.*)$/);
      if (!m) {
        await tgSend(token, chatId, "Uso: /ingreso monto descripcion");
        break;
      }
      const monto = parseFloat(m[1]);
      const id = await addMovimiento({ tipo: "ingreso", monto, categoria: "ingreso", descripcion: m[2] || "" });
      await tgSend(token, chatId, `✅ Ingreso registrado: ${fmt(monto)} _(${id.slice(0, 6)})_`);
      break;
    }

    case "/deshacer": {
      const snap = await movsRef.orderBy("fecha", "desc").limit(1).get();
      if (snap.empty) {
        await tgSend(token, chatId, "No hay movimientos para deshacer.");
        break;
      }
      const doc = snap.docs[0];
      const data = doc.data();
      await doc.ref.delete();
      await tgSend(token, chatId, `🗑️ Eliminado: ${fmt(data.monto)} · ${data.categoria}`);
      break;
    }

    case "/borrar": {
      const idFrag = rest.trim();
      if (!idFrag) {
        await tgSend(token, chatId, "Uso: /borrar id");
        break;
      }
      const snap = await movsRef.get();
      const match = snap.docs.find((d) => d.id.startsWith(idFrag));
      if (!match) {
        await tgSend(token, chatId, "No se encontró ese movimiento.");
        break;
      }
      await match.ref.delete();
      await tgSend(token, chatId, "🗑️ Movimiento eliminado.");
      break;
    }

    case "/hoy":
      await tgSend(token, chatId, await reporteHoy(now));
      break;

    case "/semana":
      await reporteSemana(token, chatId, now);
      break;

    case "/mes":
      await reporteMesGenerico(token, chatId, startOfMonth(now), startOfNextMonth(now), "Mes actual", now.getDate());
      break;

    case "/categorias":
      await reporteCategorias(token, chatId, now);
      break;

    case "/promedio": {
      const movs = await getMovimientos(startOfMonth(now), startOfNextMonth(now));
      const total = netTotal(movs);
      const promedio = total / now.getDate();
      await tgSend(token, chatId, `📈 Promedio diario del mes: ${fmt(promedio)}`);
      break;
    }

    case "/presupuesto": {
      const monto = parseFloat(rest);
      if (!monto || monto <= 0) {
        await tgSend(token, chatId, "Uso: /presupuesto monto");
        break;
      }
      await setBudget(monto);
      await tgSend(token, chatId, `✅ Presupuesto mensual establecido en ${fmt(monto)}`);
      break;
    }

    case "/limitediario": {
      const monto = parseFloat(rest);
      if (!monto || monto <= 0) {
        await tgSend(token, chatId, "Uso: /limitediario monto");
        break;
      }
      await setLimiteDiario(monto);
      await tgSend(token, chatId, `✅ Límite diario de gasto establecido en ${fmt(monto)}`);
      break;
    }

    case "/saldo": {
      const saldo = await getSaldoActual();
      const alerta = await getAlertaSaldo();
      let txt = `💳 *Saldo actual:* ${fmt(saldo)}`;
      txt += alerta.monto ? `\nAlerta configurada en: ${fmt(alerta.monto)}` : "\nNo tienes alerta de saldo bajo configurada (usa /alertasaldo monto)";
      await tgSend(token, chatId, txt);
      break;
    }

    case "/alertasaldo": {
      const monto = parseFloat(rest);
      if (!monto || monto <= 0) {
        await tgSend(token, chatId, "Uso: /alertasaldo monto");
        break;
      }
      await setAlertaSaldo(monto);
      await tgSend(token, chatId, `✅ Te avisaré cuando tu saldo baje de ${fmt(monto)}`);
      break;
    }

    case "/consejos":
      await reporteConsejos(token, chatId, now);
      break;

    case "/guiacategorias":
      await tgSend(token, chatId, textoGuiaCategorias());
      break;

    case "/resumen": {
      const hoyTxt = await reporteHoy(now);
      const movsMes = await getMovimientos(startOfMonth(now), startOfNextMonth(now));
      const totalMes = netTotal(movsMes);
      const budget = await getBudget();
      const presu = budget.monto
        ? `\nPresupuesto: ${fmt(budget.monto)} (usado ${((totalMes / budget.monto) * 100).toFixed(1)}%)`
        : "\nNo has definido un presupuesto (usa /presupuesto monto)";
      await tgSend(token, chatId, `${hoyTxt}\n📆 Total del mes: ${fmt(totalMes)}${presu}`);
      break;
    }

    case "/recordatorio": {
      const m = rest.match(/^(\d{1,2})\s+(.+)$/);
      if (!m) {
        await tgSend(token, chatId, "Uso: /recordatorio dia_del_mes texto  (ej: /recordatorio 15 Pagar internet)");
        break;
      }
      const dia = parseInt(m[1], 10);
      const ref = await addReminder(dia, m[2]);
      await tgSend(token, chatId, `🔔 Recordatorio creado para el día ${dia} de cada mes _(${ref.id.slice(0, 6)})_`);
      break;
    }

    case "/recordatorios": {
      const list = await listReminders();
      if (list.length === 0) {
        await tgSend(token, chatId, "No tienes recordatorios.");
        break;
      }
      let txt = "🔔 *Recordatorios:*\n";
      list.forEach((r) => (txt += `Día ${r.dia}: ${r.descripcion} _(${r.id.slice(0, 6)})_\n`));
      await tgSend(token, chatId, txt);
      break;
    }

    case "/borrarrecordatorio": {
      const idFrag = rest.trim();
      const snap = await remindersRef.get();
      const match = snap.docs.find((d) => d.id.startsWith(idFrag));
      if (!match) {
        await tgSend(token, chatId, "No se encontró ese recordatorio.");
        break;
      }
      await match.ref.delete();
      await tgSend(token, chatId, "🗑️ Recordatorio eliminado.");
      break;
    }

    case "/deudas": {
      const list = await listDeudas(true);
      if (!list.length) {
        await tgSend(token, chatId, "No tienes deudas pendientes.");
        break;
      }
      let txt = "🤝 *Deudas pendientes:*\n";
      list.forEach((d) => {
        const f = fechaLargaLima(d.fechaRecordatorio.toDate());
        txt += `${d.persona} · ${fmt(d.monto)} · aviso ${f} _(${d.id.slice(0, 6)})_\n`;
      });
      await tgSend(token, chatId, txt);
      break;
    }

    default:
      await tgSend(token, chatId, "No entendí ese comando. Escribe /ayuda o /menu para ver las opciones.");
  }
}

// ============================================================
// Manejo de texto entrante (revisa si el bot está esperando
// una respuesta de un botón antes de tratarlo como comando)
// ============================================================

async function processIncomingText(token, chatId, text) {
  const estado = await getEstado();

  if (estado) {
    if (estado.accion === "esperando_monto") {
      const monto = parseFloat(text.trim().replace(",", "."));
      if (isNaN(monto) || monto <= 0) {
        await tgSend(token, chatId, "Eso no es un número válido. Envía solo el monto, ej: 35.50");
        return;
      }
      const id = await addMovimiento({ tipo: "gasto", monto, categoria: estado.categoria, descripcion: "" });
      await clearEstado();
      await tgSendKeyboard(
        token,
        chatId,
        `✅ Gasto registrado: ${fmt(monto)} · ${estado.categoria} _(${id.slice(0, 6)})_`,
        mainMenuKeyboard()
      );
      await runAlertasGasto(token, chatId);
      return;
    }

    // ---------- Edición de un movimiento ya existente ----------

    if (estado.accion === "esperando_edicion_monto") {
      const monto = parseFloat(text.trim().replace(",", "."));
      if (isNaN(monto) || monto <= 0) {
        await tgSend(token, chatId, "Eso no es un número válido. Envía solo el nuevo monto, ej: 45.90");
        return;
      }
      const ref = movsRef.doc(estado.id);
      const doc = await ref.get();
      if (!doc.exists) {
        await clearEstado();
        await tgSendKeyboard(token, chatId, "Ese movimiento ya no existe (puede que lo hayas eliminado antes).", mainMenuKeyboard());
        return;
      }
      await ref.update({ monto: Math.abs(monto) });
      await clearEstado();
      await tgSendKeyboard(token, chatId, `✅ Monto actualizado a ${fmt(monto)}`, mainMenuKeyboard());
      return;
    }

    if (estado.accion === "esperando_edicion_descripcion") {
      const ref = movsRef.doc(estado.id);
      const doc = await ref.get();
      if (!doc.exists) {
        await clearEstado();
        await tgSendKeyboard(token, chatId, "Ese movimiento ya no existe.", mainMenuKeyboard());
        return;
      }
      await ref.update({ descripcion: text.trim() });
      await clearEstado();
      await tgSendKeyboard(token, chatId, "✅ Descripción actualizada.", mainMenuKeyboard());
      return;
    }

    if (estado.accion === "esperando_edicion_categoria") {
      const categoria = text.trim().toLowerCase();
      if (!categoria) {
        await tgSend(token, chatId, "Envía una categoría válida (una sola palabra, ej: mascota).");
        return;
      }
      const ref = movsRef.doc(estado.id);
      const doc = await ref.get();
      if (!doc.exists) {
        await clearEstado();
        await tgSendKeyboard(token, chatId, "Ese movimiento ya no existe.", mainMenuKeyboard());
        return;
      }
      await ref.update({ categoria });
      await clearEstado();
      await tgSendKeyboard(token, chatId, `✅ Categoría actualizada a *${categoria}*`, mainMenuKeyboard());
      return;
    }

    if (estado.accion === "esperando_limite_diario") {
      const monto = parseFloat(text.trim().replace(",", "."));
      if (!monto || monto <= 0) {
        await tgSend(token, chatId, "Envía solo el número del límite diario, ej: 50");
        return;
      }
      await setLimiteDiario(monto);
      await clearEstado();
      await tgSendKeyboard(token, chatId, `✅ Límite diario de gasto establecido en ${fmt(monto)}`, mainMenuKeyboard());
      return;
    }

    if (estado.accion === "esperando_alerta_saldo") {
      const monto = parseFloat(text.trim().replace(",", "."));
      if (!monto || monto <= 0) {
        await tgSend(token, chatId, "Envía solo el número del saldo mínimo, ej: 100");
        return;
      }
      await setAlertaSaldo(monto);
      await clearEstado();
      await tgSendKeyboard(token, chatId, `✅ Te avisaré cuando tu saldo baje de ${fmt(monto)}`, mainMenuKeyboard());
      return;
    }

    if (estado.accion === "esperando_categoria_personalizada") {
      await clearEstado();
      await handleCommand(token, chatId, text); // reutiliza el atajo rápido normal
      return;
    }

    if (estado.accion === "esperando_presupuesto") {
      const monto = parseFloat(text.trim().replace(",", "."));
      if (!monto || monto <= 0) {
        await tgSend(token, chatId, "Envía solo el número del presupuesto, ej: 1500");
        return;
      }
      await setBudget(monto);
      await clearEstado();
      await tgSendKeyboard(token, chatId, `✅ Presupuesto mensual establecido en ${fmt(monto)}`, mainMenuKeyboard());
      return;
    }

    if (estado.accion === "esperando_recordatorio") {
      const m = text.trim().match(/^(\d{1,2})\s+(.+)$/);
      if (!m) {
        await tgSend(token, chatId, "Formato: dia texto  (ej: 15 Pagar internet)");
        return;
      }
      await addReminder(parseInt(m[1], 10), m[2]);
      await clearEstado();
      await tgSendKeyboard(token, chatId, `🔔 Recordatorio creado para el día ${m[1]} de cada mes.`, mainMenuKeyboard());
      return;
    }

    // ---------- Flujo de creación de deuda (lo que te deben) ----------

    if (estado.accion === "esperando_deuda_persona") {
      const persona = text.trim();
      if (!persona) {
        await tgSend(token, chatId, "Escribe el nombre de la persona.");
        return;
      }
      await setEstado({ accion: "esperando_deuda_monto", persona });
      await tgSend(token, chatId, `¿Cuánto te debe *${persona}*? (ej: 100)`);
      return;
    }

    if (estado.accion === "esperando_deuda_monto") {
      const monto = parseFloat(text.trim().replace(",", "."));
      if (!monto || monto <= 0) {
        await tgSend(token, chatId, "Envía solo el monto, ej: 100");
        return;
      }
      await setEstado({ accion: "esperando_deuda_fecha", persona: estado.persona, monto });
      await tgSend(token, chatId, "¿Cuándo te lo recuerdo? Formato: `DD/MM/AAAA HH:MM` (ej: 30/09/2026 18:00)");
      return;
    }

    if (estado.accion === "esperando_deuda_fecha") {
      const m = text.trim().match(/^(\d{1,2})\/(\d{1,2})\/(\d{4})\s+(\d{1,2}):(\d{2})$/);
      if (!m) {
        await tgSend(token, chatId, "Formato inválido. Usa: DD/MM/AAAA HH:MM (ej: 30/09/2026 18:00)");
        return;
      }
      const dd = +m[1], mm = +m[2], yyyy = +m[3], hh = +m[4], min = +m[5];
      // El usuario escribe la fecha/hora pensando en hora de Lima. Armamos
      // primero esos mismos números como una fecha "de pared en Lima" (con
      // Date.UTC, para no depender de la zona horaria del proceso) y luego
      // la convertimos al instante UTC real con limaWallTimeToUTC. Antes se
      // usaba "new Date(yyyy, mm-1, dd, hh, min)", que interpreta hh:min en
      // la zona horaria del servidor (UTC) en vez de la de Lima, y el
      // recordatorio terminaba disparándose 5 horas antes de lo debido.
      const fechaComoPared = new Date(Date.UTC(yyyy, mm - 1, dd, hh, min));
      const fecha = limaWallTimeToUTC(fechaComoPared);
      if (isNaN(fecha.getTime()) || fecha < new Date()) {
        await tgSend(token, chatId, "Esa fecha no es válida o ya pasó. Intenta de nuevo.");
        return;
      }
      await setEstado({
        accion: "esperando_deuda_descripcion",
        persona: estado.persona,
        monto: estado.monto,
        fecha: fecha.toISOString(),
      });
      await tgSend(token, chatId, "¿Descripción? (o envía \"-\" para dejarla vacía)");
      return;
    }

    if (estado.accion === "esperando_deuda_descripcion") {
      const descripcion = text.trim() === "-" ? "" : text.trim();
      const id = await addDeuda({
        persona: estado.persona,
        monto: estado.monto,
        descripcion,
        fechaRecordatorio: new Date(estado.fecha),
      });
      await clearEstado();
      await tgSendKeyboard(
        token,
        chatId,
        `✅ Deuda registrada: *${estado.persona}* te debe ${fmt(estado.monto)}. Se descontó de tu saldo _(${id.slice(0, 6)})_`,
        mainMenuKeyboard()
      );
      return;
    }

    await clearEstado();
  }

  await handleCommand(token, chatId, text);
}

// ============================================================
// Manejo de botones (callback_query)
// ============================================================

async function handleCallback(token, chatId, cq) {
  const data = cq.data || "";
  await tgAnswerCallback(token, cq.id);
  const now = todayInTZ();

  if (data === "menu") {
    await tgSendKeyboard(token, chatId, "🏠 *Menú principal*", mainMenuKeyboard());
    return;
  }

  if (data === "cancelar") {
    await clearEstado();
    await tgSendKeyboard(token, chatId, "Operación cancelada.", mainMenuKeyboard());
    return;
  }

  if (data === "menu_gasto") {
    await tgSendKeyboard(token, chatId, "Elige una categoría para el gasto:", categoriasKeyboard());
    return;
  }

  if (data.startsWith("cat_")) {
    const categoria = data.replace("cat_", "");
    if (categoria === "custom") {
      await setEstado({ accion: "esperando_categoria_personalizada" });
      await tgSend(token, chatId, "Escribe: `monto categoria descripcion`\nEj: 35.50 mascota veterinario");
      return;
    }
    await setEstado({ accion: "esperando_monto", categoria });
    await tgSend(token, chatId, `Envía el *monto* para *${categoria}* (ej: 35.50)`);
    return;
  }

  // ---------- Flujo "Editar / Borrar" ----------

  if (data === "menu_borrar_gasto") {
    await tgSendKeyboard(token, chatId, "¿De qué período quieres ver tus movimientos?", periodoBorrarKeyboard());
    return;
  }

  if (data.startsWith("mbp_")) {
    const periodo = data.replace("mbp_", "");
    const movs = await getMovimientosPorPeriodo(periodo, now);
    if (!movs.length) {
      await tgSendKeyboard(token, chatId, "No hay movimientos en ese período. Elige otro:", periodoBorrarKeyboard());
      return;
    }
    const etiquetas = { hoy: "de hoy", semana: "de esta semana", mes: "de este mes", anio: "de este año", todos: "más recientes" };
    await tgSendKeyboard(
      token,
      chatId,
      `Movimientos ${etiquetas[periodo] || ""} — toca uno para editarlo o eliminarlo:`,
      movimientosListKeyboard(movs)
    );
    return;
  }

  if (data.startsWith("movsel_")) {
    const id = data.replace("movsel_", "");
    const doc = await movsRef.doc(id).get();
    if (!doc.exists) {
      await tgSendKeyboard(token, chatId, "Ese movimiento ya no existe.", mainMenuKeyboard());
      return;
    }
    const m = doc.data();
    const signo = m.tipo === "ingreso" ? "➕ Ingreso" : "➖ Gasto";
    const fechaTxt = fechaHoraLima(m.fecha.toDate());
    const txt =
      `${signo}: ${fmt(m.monto)}\n` +
      `Categoría: ${m.categoria}\n` +
      `Descripción: ${m.descripcion || "—"}\n` +
      `Fecha: ${fechaTxt}\n\n¿Qué quieres hacer?`;
    await tgSendKeyboard(token, chatId, txt, movimientoDetalleKeyboard(id));
    return;
  }

  if (data.startsWith("medit_monto_")) {
    const id = data.replace("medit_monto_", "");
    await setEstado({ accion: "esperando_edicion_monto", id });
    await tgSend(token, chatId, "Envía el nuevo monto (ej: 45.90)");
    return;
  }

  if (data.startsWith("medit_desc_")) {
    const id = data.replace("medit_desc_", "");
    await setEstado({ accion: "esperando_edicion_descripcion", id });
    await tgSend(token, chatId, "Envía la nueva descripción");
    return;
  }

  // Nota: se revisan primero los prefijos más específicos (catset_/catcustom_)
  // antes que el genérico "medit_cat_", aunque no colisionan entre sí.
  if (data.startsWith("medit_catset_")) {
    const resto = data.replace("medit_catset_", "");
    const ultimoGuion = resto.lastIndexOf("_");
    const id = resto.slice(0, ultimoGuion);
    const categoria = resto.slice(ultimoGuion + 1);
    const doc = await movsRef.doc(id).get();
    if (!doc.exists) {
      await tgSendKeyboard(token, chatId, "Ese movimiento ya no existe.", mainMenuKeyboard());
      return;
    }
    await movsRef.doc(id).update({ categoria });
    await tgSendKeyboard(token, chatId, `✅ Categoría actualizada a *${categoria}*`, mainMenuKeyboard());
    return;
  }

  if (data.startsWith("medit_catcustom_")) {
    const id = data.replace("medit_catcustom_", "");
    await setEstado({ accion: "esperando_edicion_categoria", id });
    await tgSend(token, chatId, "Escribe la nueva categoría (una sola palabra, ej: mascota)");
    return;
  }

  if (data.startsWith("medit_cat_")) {
    const id = data.replace("medit_cat_", "");
    await tgSendKeyboard(token, chatId, "Elige la nueva categoría:", categoriasEditKeyboard(id));
    return;
  }

  if (data.startsWith("medit_delete_")) {
    const id = data.replace("medit_delete_", "");
    const doc = await movsRef.doc(id).get();
    if (!doc.exists) {
      await tgSendKeyboard(token, chatId, "Ese movimiento ya no existe.", mainMenuKeyboard());
      return;
    }
    const m = doc.data();
    const signo = m.tipo === "ingreso" ? "ingreso" : "gasto";
    await tgSendKeyboard(
      token,
      chatId,
      `¿Confirmas eliminar este ${signo} de ${fmt(m.monto)} · ${m.categoria}${m.descripcion ? " · " + m.descripcion : ""}?`,
      confirmKeyboard(`medit_confirmdel_${id}`)
    );
    return;
  }

  if (data.startsWith("medit_confirmdel_")) {
    const id = data.replace("medit_confirmdel_", "");
    const doc = await movsRef.doc(id).get();
    if (doc.exists) {
      await doc.ref.delete();
      await tgSendKeyboard(token, chatId, "🗑️ Movimiento eliminado.", mainMenuKeyboard());
    } else {
      await tgSendKeyboard(token, chatId, "Ese movimiento ya no existe.", mainMenuKeyboard());
    }
    return;
  }

  // ---------- Flujo de deudas (lo que te deben) ----------

  if (data === "menu_deudas") {
    const deudas = await listDeudas(true);
    await tgSendKeyboard(
      token,
      chatId,
      deudas.length ? "🤝 *Deudas pendientes* — toca una:" : "No tienes deudas pendientes.",
      deudasKeyboard(deudas)
    );
    return;
  }

  if (data === "menu_add_deuda") {
    await setEstado({ accion: "esperando_deuda_persona" });
    await tgSend(token, chatId, "¿Quién te debe? Escribe el nombre.");
    return;
  }

  if (data.startsWith("deudasel_")) {
    const id = data.replace("deudasel_", "");
    const doc = await deudasRef.doc(id).get();
    if (!doc.exists) {
      await tgSendKeyboard(token, chatId, "Esa deuda ya no existe.", mainMenuKeyboard());
      return;
    }
    const d = doc.data();
    const fechaTxt = fechaHoraLima(d.fechaRecordatorio.toDate());
    const txt =
      `🤝 *${d.persona}* te debe ${fmt(d.monto)}\nDescripción: ${d.descripcion || "—"}\n` +
      `Recordatorio: ${fechaTxt}\nEstado: ${d.pagada ? "✅ Pagada" : "⏳ Pendiente"}`;
    await tgSendKeyboard(token, chatId, txt, deudaDetalleKeyboard(id));
    return;
  }

  if (data.startsWith("deuda_pagar_")) {
    const id = data.replace("deuda_pagar_", "");
    const d = await marcarDeudaPagada(id);
    if (!d) {
      await tgSendKeyboard(token, chatId, "Esa deuda ya no existe.", mainMenuKeyboard());
      return;
    }
    await tgSendKeyboard(token, chatId, `✅ Pagada. Se registró un ingreso de ${fmt(d.monto)}.`, mainMenuKeyboard());
    return;
  }

  if (data.startsWith("deuda_del_")) {
    const id = data.replace("deuda_del_", "");
    await tgSendKeyboard(token, chatId, "¿Eliminar esta deuda? (no borra el gasto ya registrado)", confirmKeyboard(`deuda_confirmdel_${id}`));
    return;
  }

  if (data.startsWith("deuda_confirmdel_")) {
    const id = data.replace("deuda_confirmdel_", "");
    await deleteDeuda(id);
    await tgSendKeyboard(token, chatId, "🗑️ Deuda eliminada.", mainMenuKeyboard());
    return;
  }

  // ---------- Resto del menú principal ----------

  if (data === "menu_hoy") {
    await tgSendKeyboard(token, chatId, await reporteHoy(now), mainMenuKeyboard());
    return;
  }

  if (data === "menu_semana") {
    await reporteSemana(token, chatId, now);
    await tgSendKeyboard(token, chatId, "🏠 Menú", mainMenuKeyboard());
    return;
  }

  if (data === "menu_mes") {
    await reporteMesGenerico(token, chatId, startOfMonth(now), startOfNextMonth(now), "Mes actual", now.getDate());
    await tgSendKeyboard(token, chatId, "🏠 Menú", mainMenuKeyboard());
    return;
  }

  if (data === "menu_categorias") {
    await reporteCategorias(token, chatId, now);
    await tgSendKeyboard(token, chatId, "🏠 Menú", mainMenuKeyboard());
    return;
  }

  if (data === "menu_promedio") {
    const movs = await getMovimientos(startOfMonth(now), startOfNextMonth(now));
    const total = netTotal(movs);
    const promedio = total / now.getDate();
    await tgSendKeyboard(token, chatId, `📈 Promedio diario del mes: ${fmt(promedio)}`, mainMenuKeyboard());
    return;
  }

  if (data === "menu_presupuesto") {
    const budget = await getBudget();
    const txt = budget.monto ? `💰 Presupuesto actual: ${fmt(budget.monto)}` : "No has definido un presupuesto todavía.";
    const kb = {
      inline_keyboard: [
        [{ text: "✏️ Cambiar presupuesto", callback_data: "menu_cambiar_presupuesto" }],
        [{ text: "🏠 Menú", callback_data: "menu" }],
      ],
    };
    await tgSendKeyboard(token, chatId, txt, kb);
    return;
  }

  if (data === "menu_cambiar_presupuesto") {
    await setEstado({ accion: "esperando_presupuesto" });
    await tgSend(token, chatId, "Envía el nuevo monto del presupuesto mensual (ej: 1500)");
    return;
  }

  if (data === "menu_recordatorios") {
    const list = await listReminders();
    const kb = { inline_keyboard: [] };
    list.forEach((r) => kb.inline_keyboard.push([{ text: `Día ${r.dia}: ${r.descripcion}`, callback_data: `delrec_${r.id}` }]));
    kb.inline_keyboard.push([{ text: "➕ Agregar recordatorio", callback_data: "menu_add_recordatorio" }]);
    kb.inline_keyboard.push([{ text: "🏠 Menú", callback_data: "menu" }]);
    await tgSendKeyboard(token, chatId, list.length ? "🔔 Toca uno para eliminarlo:" : "No tienes recordatorios todavía.", kb);
    return;
  }

  if (data === "menu_add_recordatorio") {
    await setEstado({ accion: "esperando_recordatorio" });
    await tgSend(token, chatId, "Envía: `dia texto`\nEj: 15 Pagar internet");
    return;
  }

  if (data.startsWith("delrec_")) {
    const id = data.replace("delrec_", "");
    await tgSendKeyboard(token, chatId, "¿Eliminar este recordatorio?", confirmKeyboard(`confirmdelrec_${id}`));
    return;
  }

  if (data.startsWith("confirmdelrec_")) {
    const id = data.replace("confirmdelrec_", "");
    await remindersRef.doc(id).delete().catch(() => {});
    await tgSendKeyboard(token, chatId, "🗑️ Recordatorio eliminado.", mainMenuKeyboard());
    return;
  }

  if (data === "menu_resumen") {
    const hoyTxt = await reporteHoy(now);
    const movsMes = await getMovimientos(startOfMonth(now), startOfNextMonth(now));
    const totalMes = netTotal(movsMes);
    const budget = await getBudget();
    const presu = budget.monto
      ? `\nPresupuesto: ${fmt(budget.monto)} (usado ${((totalMes / budget.monto) * 100).toFixed(1)}%)`
      : "\nNo has definido un presupuesto.";
    await tgSendKeyboard(token, chatId, `${hoyTxt}\n📆 Total del mes: ${fmt(totalMes)}${presu}`, mainMenuKeyboard());
    return;
  }

  if (data === "menu_ayuda") {
    await tgSendKeyboard(token, chatId, textoAyuda(), mainMenuKeyboard());
    return;
  }
}

// ============================================================
// Webhook principal (HTTP)
// ============================================================

const telegramWebhook_gastos_geinz_bot = onRequest(
  { region: "us-central1" },
  async (req, res) => {
    try {
      const expectedSecret = TELEGRAM_WEBHOOK_SECRET;
      const secretHeader = req.get("X-Telegram-Bot-Api-Secret-Token");
      if (expectedSecret && secretHeader !== expectedSecret) {
        res.status(401).send("unauthorized");
        return;
      }

      const update = req.body;
      const message = update && update.message;
      const callbackQuery = update && update.callback_query;
      const token = TELEGRAM_BOT_TOKEN;
      const authorizedId = TELEGRAM_CHAT_ID;

      // --- Botones presionados ---
      if (callbackQuery) {
        const chatId = callbackQuery.from.id.toString();
        if (chatId !== authorizedId) {
          await tgAnswerCallback(token, callbackQuery.id, "No autorizado");
          res.status(200).send("ok");
          return;
        }
        try {
          await handleCallback(token, chatId, callbackQuery);
        } catch (err) {
          logger.error("Error manejando callback_query", err);
          await tgSend(token, chatId, "⚠️ Ocurrió un error procesando esa acción. Intenta de nuevo o escribe /menu.");
        }
        res.status(200).send("ok");
        return;
      }

      // --- Mensajes de texto ---
      if (!message || !message.text) {
        res.status(200).send("ok");
        return;
      }

      const chatId = message.chat.id.toString();

      if (chatId !== authorizedId) {
        await tgSend(token, chatId, "🚫 No autorizado para usar este bot.");
        res.status(200).send("ok");
        return;
      }

      try {
        await processIncomingText(token, chatId, message.text);
      } catch (err) {
        logger.error("Error procesando mensaje de texto", err);
        await tgSend(token, chatId, "⚠️ Ocurrió un error procesando tu mensaje. Intenta de nuevo o escribe /menu.");
      }
      res.status(200).send("ok");
    } catch (err) {
      logger.error("Error en telegramWebhook_gastos_geinz_bot", err);
      res.status(200).send("ok"); // 200 para que Telegram no reintente indefinidamente
    }
  }
);

// ============================================================
// Tareas programadas (America/Lima)
// ============================================================

const dailyCheck = onSchedule(
  { schedule: "0 21 * * *", timeZone: TZ },
  async () => {
    const token = TELEGRAM_BOT_TOKEN;
    const chatId = TELEGRAM_CHAT_ID;
    const now = todayInTZ();
    const txt = await reporteHoy(now);
    await tgSend(token, chatId, `🌙 *Resumen del día*\n${txt}`);
    await checkBudgetAlert(token, chatId);
    await checkTodayReminders(token, chatId, now);
  }
);

const weeklyReport = onSchedule(
  { schedule: "0 8 * * 1", timeZone: TZ },
  async () => {
    const token = TELEGRAM_BOT_TOKEN;
    const chatId = TELEGRAM_CHAT_ID;
    const now = todayInTZ();
    await tgSend(token, chatId, "📊 *Reporte semanal automático*");
    await reporteSemana(token, chatId, now);
  }
);

const monthlyReport = onSchedule(
  { schedule: "0 8 1 * *", timeZone: TZ },
  async () => {
    const token = TELEGRAM_BOT_TOKEN;
    const chatId = TELEGRAM_CHAT_ID;
    const now = todayInTZ();
    const desde = startOfPrevMonth(now);
    const hasta = startOfMonth(now);
    const diasMesAnterior = Math.round((hasta - desde) / 86400000);
    await tgSend(token, chatId, "📆 *Reporte mensual automático*");
    await reporteMesGenerico(token, chatId, desde, hasta, "Mes anterior", diasMesAnterior);
    // Reinicia las alertas de presupuesto para el nuevo mes
    await configRef.set({ alertados: [] }, { merge: true });
  }
);

// Revisa cada 15 minutos si alguna deuda ya llegó a su fecha/hora de aviso.
const debtReminderCheck = onSchedule(
  { schedule: "*/15 * * * *", timeZone: TZ },
  async () => {
    await checkRecordatoriosDeuda(TELEGRAM_BOT_TOKEN, TELEGRAM_CHAT_ID);
  }
);

// ============================================================
// Exports del módulo — para importar en tu index.js principal
// ============================================================

module.exports = {
  telegramWebhook_gastos_geinz_bot,
  dailyCheck,
  weeklyReport,
  monthlyReport,
  debtReminderCheck,
};