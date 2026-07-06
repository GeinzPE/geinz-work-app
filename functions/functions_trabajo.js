"use strict";

const { onRequest } = require("firebase-functions/v2/https");
const admin = require("firebase-admin");
const axios = require("axios");

const GEMINI_FLASH_URL =
  "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent";
const GEMINI_PRO_URL =
  "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-pro:generateContent";
const OPENAI_URL = "https://api.openai.com/v1/chat/completions";

const USERS_COLLECTION = "trabajos_ia";
const MODEL_MAP = {
  "gemini-flash": { family: "gemini", endpoint: GEMINI_FLASH_URL },
  "gemini-pro": { family: "gemini", endpoint: GEMINI_PRO_URL },
  "gpt-4o": { family: "openai", model: "gpt-4o" },
  "gpt-4o-mini": { family: "openai", model: "gpt-4o-mini" },
  "gpt4o-mini": { family: "openai", model: "gpt-4o-mini" }, // ← alias
  gpt4o: { family: "openai", model: "gpt-4o" }, // ← alias
};

const {
  salidafinal,
  ESPECIALIDADES,
  CATEGORY_LABELS,
  TOKEN_LIMITS,
  MODELOS_IA,
  maxTokens,
  maxTokens_DETALLADO,
  maxTokens_SUPER_DETALLADO,
  maxTokensConBuffer,
} = require("./promps_scag_ai");

// ─── COMPATIBILIDAD CON EL SISTEMA VIEJO DE PROMPTS ──────────────────────────
// Antes existían SYSTEM_PROMPTS / SYSTEM_PROMPTS_DETALLADO /
// SYSTEM_PROMPTS_SUPER_DETALLADO / SYSTEM_PROMPT_VISION* como objetos ya
// armados con las keys camelCase exactas ("estructurasDatosAlgoritmos", etc).
// Ahora esas keys viven en ESPECIALIDADES (promps_scag_ai.js) y el texto que
// realmente llega desde la DB / el body suele venir "bonito", con espacios
// y tildes (ej: "Estructuras de Datos y Algoritmos", "Química General").
//
// En vez de tocar cada lugar del código que hace SYSTEM_PROMPTS[categoria],
// se recrean esas mismas variables como objetos "proxy": al leer
// SYSTEM_PROMPTS[cualquierTexto] se normaliza el texto, se resuelve la key
// correcta de ESPECIALIDADES y se arma el prompt con salidafinal(). Así el
// resto del archivo no necesita cambiar.

// Quita tildes, pasa a minúsculas y elimina espacios/guiones/guiones bajos
// para poder comparar "Estructuras de Datos y Algoritmos",
// "Estructuras_de_Datos_y_Algoritmos" y "estructurasDatosAlgoritmos" como si
// fueran el mismo texto.
function normalizarCategoria(str) {
  return (str || "")
    .toString()
    .normalize("NFD")
    .replace(/[\u0300-\u036f]/g, "") // quita tildes/acentos
    .toLowerCase()
    .replace(/[^a-z0-9]+/g, ""); // quita espacios, guiones, guiones bajos, etc.
}

// Mapa: texto normalizado -> key camelCase real de ESPECIALIDADES.
// Se arma una sola vez al cargar el módulo, a partir de:
//  - las keys camelCase (ej: "programacion", "estructurasDatosAlgoritmos")
//  - los labels bonitos con guion bajo (ej: "Estructuras_de_Datos_y_Algoritmos")
//  - los mismos labels pero con espacio en vez de guion bajo (como suelen
//    llegar desde la DB / el front)
const CATEGORIA_KEY_MAP = {};
for (const key of Object.keys(ESPECIALIDADES)) {
  CATEGORIA_KEY_MAP[normalizarCategoria(key)] = key;
}
for (const [key, label] of Object.entries(CATEGORY_LABELS)) {
  CATEGORIA_KEY_MAP[normalizarCategoria(label)] = key;
  CATEGORIA_KEY_MAP[normalizarCategoria(label.replace(/_/g, " "))] = key;
}

// Dado cualquier texto de categoría (con tildes, espacios, mayúsculas,
// guiones bajos, o ya en camelCase), devuelve la key real de ESPECIALIDADES.
// Si no encuentra coincidencia, cae a "general" (usa PROFESOR_GENERAL).
function resolverCategoria(categoriaInput) {
  const norm = normalizarCategoria(categoriaInput);
  if (!norm || norm === "general") return "general";
  return CATEGORIA_KEY_MAP[norm] || "general";
}

// Fábrica de diccionarios "proxy" para texto: SYSTEM_PROMPTS[categoria],
// SYSTEM_PROMPTS_DETALLADO[categoria], SYSTEM_PROMPTS_SUPER_DETALLADO[categoria]
function crearDiccionarioPrompts(nivel) {
  return new Proxy(
    {},
    {
      get(_target, prop) {
        if (typeof prop !== "string") return undefined;
        const catKey = resolverCategoria(prop);
        return salidafinal(catKey, "texto", nivel).systemPrompt;
      },
    },
  );
}

const SYSTEM_PROMPTS = crearDiccionarioPrompts("directo");
const SYSTEM_PROMPTS_DETALLADO = crearDiccionarioPrompts("detallado");
const SYSTEM_PROMPTS_SUPER_DETALLADO = crearDiccionarioPrompts("super");

// Los prompts de visión no dependen de la categoría (son solo instrucciones
// de formato), así que se calculan una única vez al cargar el módulo.
const SYSTEM_PROMPT_VISION = salidafinal("general", "vision", "directo").systemPrompt;
const SYSTEM_PROMPT_VISION_DETALLADO = salidafinal(
  "general",
  "vision",
  "detallado",
).systemPrompt;
const SYSTEM_PROMPT_VISION_SUPER_DETALLADO = salidafinal(
  "general",
  "vision",
  "super",
).systemPrompt;

// ─── DB2 (segunda app Firebase) ───────────────────────────────────────────────
let db2 = null;
let preciosCache = null;
let preciosCacheTime = 0;
const CACHE_TTL = 5 * 60 * 1000;

const initDb2 = () => {
  if (db2) return db2;
  try {
    const appExistente = admin.apps.find((app) => app.name === "app2");
    if (appExistente) {
      db2 = appExistente.firestore();
    } else {
      const app2 = admin.initializeApp(
        {
          credential: admin.credential.cert({
            projectId: process.env.PROJECT_ID_2,
            clientEmail: process.env.CLIENT_EMAIL_2,
            privateKey: process.env.PRIVATE_KEY_2?.replace(/\\n/g, "\n"),
          }),
        },
        "app2",
      );
      db2 = app2.firestore();
    }
  } catch (e) {
    console.error("❌ Error inicializando app2:", e.message);
    db2 = null;
  }
  return db2;
};

// ─── Helper: verificar alias en db2 ──────────────────────────────────────────
async function verificarAlias(alias) {
  const database = initDb2();
  if (!database) throw new Error("No se pudo conectar a la base de datos.");

  const snap = await database
    .collection(USERS_COLLECTION)
    .doc(alias.toLowerCase())
    .get();

  if (!snap.exists)
    return {
      ok: false,
      status: 404,
      error: "Usuario no encontrado.",
    };

  const data = snap.data();

  if (data.suspended === true)
    return { ok: false, status: 403, error: "Cuenta suspendida." };

  const credits = data.credits ?? data.creditos ?? null;

  return { ok: true, credits, data };
}

// ─── OBTENER COSTOS POR CATEGORIA ────────
function categoriaToDbKey(category) {
  if (!category || category === "general" || category === "General") {
    return "general";
  }
  return category.replace(/_/g, " ");
}

async function obtenerCostoCategoria(category) {
  try {
    const database = initDb2();
    if (!database) {
      console.warn("[obtenerCostoCategoria] ❌ No se pudo inicializar DB.");
      return { costo: 0, costoPorMoneda: 0, tipoCambio: 1 };
    }

    const ahora = Date.now();
    const cacheVencido = !preciosCache || ahora - preciosCacheTime > CACHE_TTL;
    console.log(
      `[obtenerCostoCategoria] Cache vencido: ${cacheVencido}, category: "${category}"`,
    );

    if (cacheVencido) {
      console.log("[obtenerCostoCategoria] Leyendo precios desde Firestore...");
      const snap = await database
        .collection("precio_apartado")
        .doc("scag_site")
        .get();
      preciosCache = snap.exists ? snap.data() : {};
      preciosCacheTime = ahora;
      console.log(
        "[obtenerCostoCategoria] preciosCache cargado:",
        JSON.stringify(preciosCache),
      );
    } else {
      console.log("[obtenerCostoCategoria] Usando cache existente.");
    }

    const key = categoriaToDbKey(category);
    const costo = preciosCache?.categoria?.[key];
    const costoPorMoneda = preciosCache?.costo_por_moneda ?? 0;
    const tipoCambio = preciosCache?.tipo_cambio ?? 1;

    console.log(
      `[obtenerCostoCategoria] key: "${key}", costo: ${costo}, costoPorMoneda: ${costoPorMoneda}, tipoCambio: ${tipoCambio}`,
    );
    console.log(
      `[obtenerCostoCategoria] categorias disponibles en cache:`,
      JSON.stringify(preciosCache?.categoria ?? {}),
    );

    return {
      costo: typeof costo === "number" ? costo : 0,
      costoPorMoneda,
      tipoCambio,
    };
  } catch (e) {
    console.error("[obtenerCostoCategoria] ❌ ERROR:", e.message, e.stack);
    return { costo: 0, costoPorMoneda: 0, tipoCambio: 1 };
  }
}
// ─── Helper: detectar si la respuesta de la IA es válida para cobrar ────────
function esRespuestaValida(answer, mode) {
  if (!answer || !answer.trim()) return false;

  const a = answer.trim().toLowerCase();

  // Si TODA la respuesta es solo "ilegible" o equivalentes, no cobrar
  const soloIlegible = /^(\d+\)\s*\[?ilegible\]?\s*\n?)+$/i.test(answer.trim());
  if (soloIlegible) return false;

  // Si la respuesta contiene una sola línea y es genérica de "sin respuesta"
  const patronesInvalidos = [
    /^sin respuesta\.?$/,
    /^no se pudo (leer|procesar|identificar)/,
    /^\[ilegible\]$/,
    /^no hay preguntas/,
    /^no veo (ninguna|texto|preguntas)/,
    /^sin_contenido$/i,
  ];
  if (patronesInvalidos.some((re) => re.test(a))) return false;

  // Si es modo imagen: contar cuántas líneas son "[ilegible]" vs líneas totales
  if (mode === "image") {
    const lineas = answer
      .trim()
      .split("\n")
      .map((l) => l.trim())
      .filter(Boolean);
    if (lineas.length === 0) return false;

    const ilegibles = lineas.filter((l) => /\[ilegible\]/i.test(l)).length;
    // Si TODAS las líneas son ilegibles → no cobrar
    if (ilegibles === lineas.length) return false;
  }

  return true;
}

// ─── CLOUD FUNCTION ───────────────────────────────────────────────────────────
const screenaiQuery = onRequest(
  { region: "us-central1", timeoutSeconds: 120, memory: "256MiB", cors: true },
  async (req, res) => {
    if (req.method === "OPTIONS") {
      res.status(204).send("");
      return;
    }

    // ── GET ?check=1&alias=xxx ────────────────────────────────────────────────
    if (req.method === "GET" && req.query.check === "1") {
      const alias = (req.query.alias || "").trim().toLowerCase();
      if (!alias) {
        res.status(400).json({ ok: false, error: "Alias requerido." });
        return;
      }

      try {
        const result = await verificarAlias(alias);
        if (!result.ok) {
          res.status(result.status).json({ ok: false, error: result.error });
          return;
        }
        res.status(200).json({ ok: true, credits: result.credits });
      } catch (e) {
        console.error("[ScreenAI] verificarAlias error:", e.message);
        res
          .status(500)
          .json({ ok: false, error: "Error al verificar alias: " + e.message });
      }
      return;
    }

    // ── GET ?config=1&alias=xxx — leer configuración del usuario ──────────────
    if (req.method === "GET" && req.query.config === "1") {
      const alias = (req.query.alias || "").trim().toLowerCase();
      if (!alias) {
        res.status(400).json({ ok: false, error: "Alias requerido." });
        return;
      }

      try {
        const database = initDb2();
        if (!database) {
          res.status(500).json({
            ok: false,
            error: "No se pudo conectar a la base de datos.",
          });
          return;
        }

        const snap = await database
          .collection(USERS_COLLECTION)
          .doc(alias)
          .get();
        if (!snap.exists) {
          res.status(404).json({ ok: false, error: "Usuario no encontrado." });
          return;
        }

        const data = snap.data();
        res.status(200).json({
          ok: true,
          provider: data.provider || null,
          category: data.category || null,
          hotkeyToggle: data.hotkeyToggle || null,
          hotkeyQuery: data.hotkeyQuery || null,
          hotkeyCapture: data.hotkeyCapture || null,
          position: data.position || null,
          theme: data.theme || "solid",
          highlightColor: data.highlightColor || "#fad232",
          autoClick: data.autoClick ?? true,
          solutionMode: data.solutionMode || "detallado",
        });
      } catch (e) {
        console.error("[ScreenAI] config GET error:", e.message);
        res
          .status(500)
          .json({ ok: false, error: "Error al leer config: " + e.message });
      }
      return;
    }

    // ── POST ?saveconfig=1 — guardar configuración del usuario ────────────────
    if (req.method === "POST" && req.query.saveconfig === "1") {
      console.log("[saveconfig] body:", JSON.stringify(req.body));
      const {
        alias,
        provider,
        category,
        hotkeyToggle,
        hotkeyQuery,
        hotkeyCapture,
        position,
        theme,
        highlightColor,
        autoClick,
        solutionMode, // 👈 agregar aquí
      } = req.body || {};

      if (!alias || !alias.trim()) {
        res.status(400).json({ ok: false, error: "Alias requerido." });
        return;
      }

      try {
        const database = initDb2();
        if (!database) {
          res.status(500).json({
            ok: false,
            error: "No se pudo conectar a la base de datos.",
          });
          return;
        }

        const updateData = {
          updatedAt: admin.firestore.FieldValue.serverTimestamp(),
        };
        if (provider !== undefined) updateData.provider = provider;
        if (category !== undefined) updateData.category = category;
        if (hotkeyToggle !== undefined) updateData.hotkeyToggle = hotkeyToggle;
        if (hotkeyQuery !== undefined) updateData.hotkeyQuery = hotkeyQuery;
        if (hotkeyCapture !== undefined)
          updateData.hotkeyCapture = hotkeyCapture;
        if (position !== undefined) updateData.position = position;
        if (theme !== undefined) updateData.theme = theme;
        if (highlightColor !== undefined)
          updateData.highlightColor = highlightColor;
        if (autoClick !== undefined) updateData.autoClick = autoClick;
        if (solutionMode !== undefined) updateData.solutionMode = solutionMode; // 👈 agregar aquí

        await database
          .collection(USERS_COLLECTION)
          .doc(alias.trim().toLowerCase())
          .set(updateData, { merge: true });

        res.status(200).json({ ok: true });
      } catch (e) {
        console.error("[ScreenAI] saveconfig error:", e.message);
        res
          .status(500)
          .json({ ok: false, error: "Error al guardar config: " + e.message });
      }
      return;
    }

    // ── POST → consulta IA ────────────────────────────────────────────────────
    if (req.method !== "POST") {
      res.status(405).json({ ok: false, error: "Método no permitido." });
      return;
    }

    const {
      alias: aliasRaw = "",
      mode = "text",
      provider = "gemini-flash",
      category = "general",
      text = "",
      imageBase64 = "",
      mimeType = "image/png",
      textHint = "",
    } = req.body || {};
    console.log("[ScreenAI] POST body:", aliasRaw);
    const alias = String(aliasRaw ?? "").trim();
    if (!alias) {
      res.status(401).json({ ok: false, error: "Alias requerido." });
      return;
    }
    if (mode === "text" && !text.trim()) {
      res.status(400).json({ ok: false, error: "Campo 'text' requerido." });
      return;
    }
    if (mode === "image" && !imageBase64.trim()) {
      res
        .status(400)
        .json({ ok: false, error: "Campo 'imageBase64' requerido." });
      return;
    }

    // Verificar alias
    let verification;
    try {
      verification = await verificarAlias(alias);
      if (!verification.ok) {
        res
          .status(verification.status)
          .json({ ok: false, error: verification.error });
        return;
      }
    } catch (e) {
      res
        .status(500)
        .json({ ok: false, error: "Error al verificar cuenta: " + e.message });
      return;
    }

    // Seleccionar modelo
    const modelInfo = MODEL_MAP[provider];
    if (!modelInfo) {
      res
        .status(400)
        .json({ ok: false, error: `Proveedor desconocido: ${provider}` });
      return;
    }

    // Keys desde env
    const GEMINI_KEY = process.env.PIRVATE_KEY_GEMINI_APITRABAJO;
    const OPENAI_KEY = process.env.PIRVATE_KEY_OPENIA_APITRABAJO;
    const CLAUDE_KEY =
      process.env.ANTHROPIC_API_KEY || process.env.CLAUDE_API_KEY || "";

    // Llamar IA
    let answer = "";
    try {
      const systemPrompt = SYSTEM_PROMPTS[category] || SYSTEM_PROMPTS.general;
      // 👇 antes faltaba pasar "provider": maxTokens ahora distingue entre
      // modelos económicos (gemini-flash / gpt-4o-mini) y pesados
      // (gemini-pro / gpt-4o), así que sin el provider siempre calculaba el
      // tope de los modelos "pesados". Se corrige para que respete el tope
      // real del modelo elegido.
      const tokens = maxTokens(category, provider);

      if (mode === "text") {
        switch (modelInfo.family) {
          case "gemini":
            answer = await callGeminiText(
              text,
              GEMINI_KEY,
              modelInfo.endpoint,
              systemPrompt,
              tokens,
            );
            break;
          case "openai":
            answer = await callOpenAIText(
              text,
              OPENAI_KEY,
              modelInfo.model,
              systemPrompt,
              tokens,
            );
            break;
        }
      } else {
        switch (modelInfo.family) {
          case "gemini":
            answer = await callGeminiVision(
              imageBase64,
              mimeType,
              textHint,
              GEMINI_KEY,
              modelInfo.endpoint,
            );
            break;
          case "openai":
            answer = await callOpenAIVision(
              imageBase64,
              mimeType,
              textHint,
              OPENAI_KEY,
              modelInfo.model,
            );
            break;
        }
      }

      if (!answer || !answer.trim()) {
        res
          .status(502)
          .json({ ok: false, error: "La IA no devolvió respuesta." });
        return;
      }

      // DESPUÉS (correcto):
      const valida = esRespuestaValida(answer, mode);
      // Reemplaza el bloque "if (valida)" actual por esto:
      if (valida) {
        try {
          // Leer solutionMode del perfil del usuario
          const database = initDb2();
          let solutionMode = "directo";
          if (database) {
            const perfilSnap = await database
              .collection(USERS_COLLECTION)
              .doc(alias.toLowerCase())
              .get();
            solutionMode = perfilSnap.data()?.solutionMode || "directo";
          }

          // Costos en monedas desde DB
          const costoModelo = await obtenerCostoDesdeDB(provider, mode);
          const { costo: costoCategoria, costoPorMoneda } =
            await obtenerCostoCategoria(category);
          const costoSolucion = await obtenerCostoSolucion(solutionMode);

          // Total en monedas y en soles
          const costoTotalMonedas =
            costoModelo + costoCategoria + costoSolucion;
          const costoEnSoles = parseFloat(
            (costoTotalMonedas * costoPorMoneda).toFixed(4),
          );

          const { antes, despues } = await descontarCreditoN(
            alias.toLowerCase(),
            costoTotalMonedas,
          );

          await guardarHistorial(
            alias.toLowerCase(),
            provider,
            category,
            mode,
            antes,
            despues,
            costoTotalMonedas, // creditosConsumidos
            costoEnSoles, // costoSoles
            costoPorMoneda, // costo_por_moneda
            solutionMode, // modo solución
          );
        } catch (e) {
          console.warn("[ScreenAI] No se pudo descontar crédito:", e.message);
        }
      } else {
        console.log(
          `[ScreenAI] Respuesta inválida/ilegible — NO se descuenta crédito. alias=${alias} mode=${mode}`,
        );
      }

      res.status(200).json({ ok: true, answer, charged: valida });
    } catch (aiErr) {
      console.error("[ScreenAI] AI error:", aiErr.message);
      console.error(
        "[ScreenAI] AI response data:",
        JSON.stringify(aiErr.response?.data),
      );
      res.status(502).json({ ok: false, error: aiErr.message });
    }
  },
);

// ─── GEMINI TEXT ──────────────────────────────────────────────────────────────
async function callGeminiText(text, apiKey, endpoint, systemPrompt, tokens) {
  const isPro = endpoint.includes("2.5-pro");

  const userText = isPro
    ? `${text}\n\nIMPORTANTE: Si hay opciones (A, B, C, D), escribe SOLO el contenido de la opción correcta, NUNCA la letra sola.`
    : text;

  const { data } = await axios.post(
    `${endpoint}?key=${apiKey}`,
    {
      system_instruction: { parts: [{ text: systemPrompt }] },
      contents: [{ role: "user", parts: [{ text: userText }] }],
      generationConfig: {
        temperature: 0.0,
        maxOutputTokens: tokens,
        ...(isPro && { thinkingConfig: { thinkingBudget: 512 } }),
      },
    },
    {
      headers: { "Content-Type": "application/json" },
      timeout: isPro ? 60000 : 30000,
    },
  );
  return (
    data?.candidates?.[0]?.content?.parts
      ?.find((p) => p.text && !p.thought)
      ?.text?.trim() ?? ""
  );
}

async function callGeminiVision(
  imageBase64,
  mimeType,
  textHint,
  apiKey,
  endpoint,
) {
  const { data } = await axios.post(
    `${endpoint}?key=${apiKey}`,
    {
      system_instruction: { parts: [{ text: SYSTEM_PROMPT_VISION }] },
      contents: [
        {
          role: "user",
          parts: [
            { inline_data: { mime_type: mimeType, data: imageBase64 } },
            {
              text:
                "Responde TODAS las preguntas." +
                (textHint ? `\nContexto: ${textHint}` : ""),
            },
          ],
        },
      ],
      generationConfig: { temperature: 0.2, maxOutputTokens: 2048 },
    },
    { headers: { "Content-Type": "application/json" }, timeout: 45000 },
  );
  return (
    data?.candidates?.[0]?.content?.parts?.[0]?.text?.trim() ?? "Sin respuesta."
  );
}

// ─── OPENAI ───────────────────────────────────────────────────────────────────
async function callOpenAIText(text, apiKey, model, systemPrompt, tokens) {
  const { data } = await axios.post(
    OPENAI_URL,
    {
      model,
      messages: [
        { role: "system", content: systemPrompt },
        { role: "user", content: text },
      ],
      max_tokens: tokens,
      temperature: 0.0,
    },
    {
      headers: {
        "Content-Type": "application/json",
        Authorization: `Bearer ${apiKey}`,
      },
      timeout: 30000,
    },
  );
  return data?.choices?.[0]?.message?.content?.trim() ?? "";
}

async function callOpenAIVision(
  imageBase64,
  mimeType,
  textHint,
  apiKey,
  model,
) {
  const { data } = await axios.post(
    OPENAI_URL,
    {
      model,
      messages: [
        { role: "system", content: SYSTEM_PROMPT_VISION },
        {
          role: "user",
          content: [
            {
              type: "image_url",
              image_url: { url: `data:${mimeType};base64,${imageBase64}` },
            },
            {
              type: "text",
              text:
                "Responde todas las preguntas." +
                (textHint ? `\nContexto: ${textHint}` : ""),
            },
          ],
        },
      ],
      max_tokens: 2048,
      temperature: 0.2,
    },
    {
      headers: {
        "Content-Type": "application/json",
        Authorization: `Bearer ${apiKey}`,
      },
      timeout: 45000,
    },
  );
  return data?.choices?.[0]?.message?.content?.trim() ?? "Sin respuesta.";
}

async function descontarCreditoN(alias, n) {
  const database = initDb2();
  if (!database) return { antes: null, despues: null };

  const snap = await database.collection(USERS_COLLECTION).doc(alias).get();
  const data = snap.data() || {};
  const field = data.credits !== undefined ? "credits" : "creditos";
  const creditosAntes = data[field] ?? 0;
  const creditosDespues = creditosAntes - n;

  await database
    .collection(USERS_COLLECTION)
    .doc(alias)
    .update({
      [field]: admin.firestore.FieldValue.increment(-n),
      lastQuery: admin.firestore.FieldValue.serverTimestamp(),
      totalQueries: admin.firestore.FieldValue.increment(1),
    });

  return { antes: creditosAntes, despues: creditosDespues };
}

async function guardarHistorial(
  alias,
  provider,
  category,
  mode,
  creditosAntes,
  creditosDespues,
  creditosConsumidos,
  costoSoles,
  costoPorMoneda,
  solutionMode,
  apiMeta = {},
  respuestaIA = "",
) {
  const database = initDb2();
  if (!database) return;
  try {
    const MAX_LEN_RESPUESTA = 50000;
    const respuestaGuardada =
      typeof respuestaIA === "string" && respuestaIA.length > MAX_LEN_RESPUESTA
        ? respuestaIA.slice(0, MAX_LEN_RESPUESTA) + "…(truncado)"
        : respuestaIA;

    await database
      .collection(USERS_COLLECTION)
      .doc(alias)
      .collection("historial")
      .add({
        // ── Identificación ─────────────────────────────────────────
        modelo: provider,
        categoria: category,
        tipo: mode === "image" ? "Captura de pantalla" : "Selección de texto",
        solutionMode: solutionMode || "directo",
        fuente: apiMeta.fuente ?? "extension", // ← extensión o n8n

        // ── Créditos del usuario ───────────────────────────────────
        creditosAntes,
        creditosRestantes: creditosDespues,
        creditosConsumidos,

        // ── Lo que cobras tú al usuario ────────────────────────────
        costoSoles,
        costoPorMoneda,

        // ── Lo que te cobró la API a ti (costo real) ───────────────
        costoRealUSD: apiMeta.costoRealUSD ?? 0,
        costoRealSoles: apiMeta.costoRealSoles ?? 0,
        tipoCambioUSD: apiMeta.tipoCambioUSD ?? 0,

        // ── Tokens ────────────────────────────────────────────────
        tokensInput: apiMeta.tokensInput ?? 0,
        tokensOutput: apiMeta.tokensOutput ?? 0,
        tokensTotal: apiMeta.tokensTotal ?? 0,
        precioInputPorMillon: apiMeta.precioInputPorMillon ?? 0,
        precioOutputPorMillon: apiMeta.precioOutputPorMillon ?? 0,
        costoRealInputUSD: apiMeta.costoRealInputUSD ?? 0,
        costoRealOutputUSD: apiMeta.costoRealOutputUSD ?? 0,

        // ── Ganancia ──────────────────────────────────────────────
        margenGananciaSoles: apiMeta.margenGananciaSoles ?? 0,
        margenGananciaPorcentaje: apiMeta.margenGananciaPorcentaje ?? 0,
        multiplicadorGanancia: apiMeta.multiplicadorGanancia ?? 0,

        // ── Respuesta IA ───────────────────────────────────────────
        respuestaIA: respuestaGuardada,
        respuestaTruncada:
          typeof respuestaIA === "string" &&
          respuestaIA.length > MAX_LEN_RESPUESTA,
        respuestaLongitudOriginal:
          typeof respuestaIA === "string" ? respuestaIA.length : 0,

        // ── Timestamp ─────────────────────────────────────────────
        fecha: admin.firestore.FieldValue.serverTimestamp(),
      });
  } catch (e) {
    console.warn("[ScreenAI] No se pudo guardar historial:", e.message);
  }
}

async function obtenerCostoDesdeDB(provider, mode) {
  try {
    const ahora = Date.now();
    if (!preciosCache || ahora - preciosCacheTime > CACHE_TTL) {
      const snap = await db2
        .collection("precio_apartado")
        .doc("scag_site")
        .get();
      preciosCache = snap.exists ? snap.data() : {};
      preciosCacheTime = ahora;
    }

    const modeKey = mode === "image" ? "captura" : "select";
    const PROVIDER_KEY_MAP = {
      "gemini-flash": { group: "gemini_google", key: `2.5flash_${modeKey}` },
      "gemini-pro": { group: "gemini_google", key: `2.5pro_${modeKey}` },
      "gpt-4o": { group: "gpt_openIA", key: `gpt4o_${modeKey}` },
      "gpt-4o-mini": { group: "gpt_openIA", key: `gpt4o_mini_${modeKey}` },
    };

    const map = PROVIDER_KEY_MAP[provider];
    if (!map) return 1;

    const costo = preciosCache?.[map.group]?.[map.key];
    return typeof costo === "number" ? costo : 1;
  } catch (e) {
    console.warn("[ScreenAI] No se pudo leer precio desde DB:", e.message);
    return 1;
  }
}

// ─── OBTENER COSTO POR MODO DE SOLUCIÓN (directo / detallado) ───────────────
async function obtenerCostoSolucion(solutionMode) {
  try {
    const database = initDb2();
    if (!database) return 0;

    const ahora = Date.now();
    if (!preciosCache || ahora - preciosCacheTime > CACHE_TTL) {
      const snap = await database
        .collection("precio_apartado")
        .doc("scag_site")
        .get();
      preciosCache = snap.exists ? snap.data() : {};
      preciosCacheTime = ahora;
    }

    const costo = preciosCache?.solucion?.[solutionMode];
    return typeof costo === "number" ? costo : 0;
  } catch (e) {
    console.warn("[ScreenAI] No se pudo leer costo solución:", e.message);
    return 0;
  }
}

function extractJSON(raw) {
  const cleaned = raw.replace(/```[\w]*\n?/g, "").trim();
  try {
    return JSON.parse(cleaned);
  } catch (_) {}
  const s = cleaned.indexOf("{"),
    e = cleaned.lastIndexOf("}");
  if (s !== -1 && e > s) {
    try {
      return JSON.parse(cleaned.slice(s, e + 1));
    } catch (_) {}
  }
  return null;
}

const suggestConfig = onRequest(
  { region: "us-central1", timeoutSeconds: 15, memory: "256MiB", cors: true },
  async (req, res) => {
    if (req.method === "OPTIONS") return res.status(204).send("");
    if (req.method !== "POST")
      return res.status(405).json({ ok: false, error: "Método no permitido." });

    try {
      const { prompt } = req.body || {};
      if (!prompt?.trim())
        return res.status(400).json({ ok: false, error: "Prompt requerido." });

      const GEMINI_KEY =
        process.env.PRIVATE_KEY_GEMINI_APITRABAJO ||
        process.env.PIRVATE_KEY_GEMINI_APITRABAJO;
      if (!GEMINI_KEY)
        return res
          .status(500)
          .json({ ok: false, error: "API key no configurada." });

      const systemInstruction = `Eres un clasificador de consultas académicas. Responde SOLO con un JSON, sin markdown:
{"model":"X","category":"Y","text":"una frase corta diciendo por qué escogiste esa categoría"}
 
Modelos y categorías válidas:
- "gemini-pro": "Modelado y Simulación", "Análisis Estadístico y Datos", "Fórmulas y Glosarios Técnicos", "Código y Lógica de Software"
- "gpt-4o": "Análisis Técnico y Ambiental", "Informes y Terminología Científica"
- "gemini-flash": "Comprensión y Análisis Corporativo", "Documentación e Investigación", "Traducción y Redacción Global", "General"`;

      const { data } = await axios.post(
        `${GEMINI_FLASH_URL}?key=${GEMINI_KEY}`,
        {
          system_instruction: { parts: [{ text: systemInstruction }] },
          contents: [
            { role: "user", parts: [{ text: prompt.trim().slice(0, 800) }] },
          ],
          generationConfig: { temperature: 0, maxOutputTokens: 200 },
        },
        { headers: { "Content-Type": "application/json" }, timeout: 10000 },
      );

      const rawText =
        data?.candidates?.[0]?.content?.parts?.[0]?.text?.trim() || "";
      const finishReason = data?.candidates?.[0]?.finishReason;
      console.log(
        "[suggestConfig] finishReason:",
        finishReason,
        "| raw:",
        rawText,
      );

      if (!rawText)
        throw new Error("Texto vacío. finishReason: " + finishReason);

      const parsed = extractJSON(rawText);
      if (!parsed) throw new Error("JSON inválido: " + rawText);

      const validModels = ["gemini-flash", "gemini-pro", "gpt-4o"];
      const validCategories = [
        "General",
        "Modelado y Simulación",
        "Análisis Estadístico y Datos",
        "Fórmulas y Glosarios Técnicos",
        "Código y Lógica de Software",
        "Análisis Técnico y Ambiental",
        "Informes y Terminología Científica",
        "Comprensión y Análisis Corporativo",
        "Documentación e Investigación",
        "Traducción y Redacción Global",
      ];

      const model = validModels.includes(parsed.model)
        ? parsed.model
        : "gemini-flash";
      const category = validCategories.includes(parsed.category)
        ? parsed.category
        : "General";
      const text =
        typeof parsed.text === "string" && parsed.text.trim()
          ? parsed.text.trim()
          : "";

      return res
        .status(200)
        .json({ ok: true, model, category, text, source: "ai" });
    } catch (e) {
      console.error("[suggestConfig Fallback]:", e.response?.data || e.message);
      return res.status(200).json({
        ok: true,
        model: "gemini-flash",
        category: "General",
        text: "No se pudo clasificar la consulta.",
        source: "fallback",
      });
    }
  },
);

const BOT_SCAG_COLLECTION = "bot_scag";
const TRABAJOS_IA_COLLECTION = "trabajos_ia";

const getUserData = onRequest(
  { region: "us-central1", timeoutSeconds: 20, memory: "256MiB", cors: true },
  async (req, res) => {
    if (req.method === "OPTIONS") {
      res.status(204).send("");
      return;
    }
    if (req.method !== "GET" && req.method !== "POST") {
      res.status(405).json({ ok: false, error: "Método no permitido." });
      return;
    }

    const body = req.method === "GET" ? req.query : req.body || {};
    const numero = body.numero?.toString().trim();
    const nombre = body.nombre?.toString().trim() || "";

    if (!numero) {
      res.status(400).json({ ok: false, error: "Número requerido." });
      return;
    }

    try {
      const database = initDb2();
      if (!database) {
        res.status(500).json({
          ok: false,
          error: "No se pudo conectar a la base de datos.",
        });
        return;
      }

      // ── 1. Buscar el registro en bot_scag ──
      const botRef = database.collection(BOT_SCAG_COLLECTION).doc(numero);
      const botSnap = await botRef.get();

      // ── 2. Si NO existe el documento, lo creamos (solo numero y nombre, sin alias) ──
      if (!botSnap.exists) {
        console.log(`[getUserData] Número nuevo, creando: ${numero}`);
        await botRef.set({
          numero,
          nombre,
          creadoEn: admin.firestore.Timestamp.now(),
        });

        res.status(200).json({
          ok: true,
          esNuevo: true, // no tiene alias -> es nuevo
          alias: null,
          data: null,
        });
        return;
      }

      // ── 3. El documento ya existía, revisamos si tiene alias ──
      const botData = botSnap.data();
      const alias = botData?.alias;

      // Sin alias configurado -> lo tratamos como "nuevo" también
      if (!alias) {
        res.status(200).json({
          ok: true,
          esNuevo: true, // sin alias = todavía no vinculó cuenta = nuevo
          alias: null,
          data: null,
        });
        return;
      }

      // ── 4. Con el alias, buscamos en trabajos_ia (mismo flujo de siempre) ──
      const snap = await database
        .collection(TRABAJOS_IA_COLLECTION)
        .doc(alias)
        .get();

      if (!snap.exists) {
        res.status(404).json({ ok: false, error: "Usuario no encontrado." });
        return;
      }

      const data = snap.data();
      const clean = {};
      for (const [key, value] of Object.entries(data)) {
        clean[key] = value?.toDate ? value.toDate().toISOString() : value;
      }

      res.status(200).json({ ok: true, esNuevo: false, alias, data: clean });
    } catch (e) {
      console.error("[getUserData] error:", e.message);
      res
        .status(500)
        .json({ ok: false, error: "Error al obtener datos: " + e.message });
    }
  },
);

const getUserData_Extencion = onRequest(
  { region: "us-central1", timeoutSeconds: 20, memory: "256MiB", cors: true },
  async (req, res) => {
    if (req.method === "OPTIONS") {
      res.status(204).send("");
      return;
    }
    if (req.method !== "GET" && req.method !== "POST") {
      console.log(`[getUserData_Extencion] Método no permitido: ${req.method}`);
      res.status(405).json({ ok: false, error: "Método no permitido." });
      return;
    }

    const body = req.method === "GET" ? req.query : req.body || {};
    const alias = body.alias?.toString().trim();

    console.log(
      `[getUserData_Extencion] Método: ${req.method} | Query/Body recibido:`,
      body,
    );
    console.log(`[getUserData_Extencion] Alias extraído: "${alias}"`);

    if (!alias) {
      console.log(
        `[getUserData_Extencion] Alias vacío o no llegó, devolviendo 400.`,
      );
      res.status(400).json({ ok: false, error: "Alias requerido." });
      return;
    }

    try {
      const database = initDb2();
      if (!database) {
        console.log(
          `[getUserData_Extencion] No se pudo conectar a la base de datos.`,
        );
        res.status(500).json({
          ok: false,
          error: "No se pudo conectar a la base de datos.",
        });
        return;
      }

      console.log(
        `[getUserData_Extencion] Buscando en colección "${TRABAJOS_IA_COLLECTION}" el documento con ID: "${alias}"`,
      );

      // Buscamos directo en trabajos_ia por el alias
      const snap = await database
        .collection(TRABAJOS_IA_COLLECTION)
        .doc(alias)
        .get();

      console.log(`[getUserData_Extencion] ¿Documento existe?: ${snap.exists}`);

      if (!snap.exists) {
        console.log(
          `[getUserData_Extencion] No se encontró "${alias}" en "${TRABAJOS_IA_COLLECTION}". Devolviendo 404.`,
        );
        res.status(404).json({ ok: false, error: "Usuario no encontrado." });
        return;
      }

      const data = snap.data();
      console.log(`[getUserData_Extencion] Datos crudos encontrados:`, data);

      const clean = {};
      for (const [key, value] of Object.entries(data)) {
        clean[key] = value?.toDate ? value.toDate().toISOString() : value;
      }

      console.log(`[getUserData_Extencion] Datos limpios a devolver:`, clean);

      res.status(200).json({ ok: true, esNuevo: false, alias, data: clean });
    } catch (e) {
      console.error("[getUserData_Extencion] error:", e.message);
      res
        .status(500)
        .json({ ok: false, error: "Error al obtener datos: " + e.message });
    }
  },
);

const getPreciosPlanes = onRequest(
  { region: "us-central1", cors: true },
  async (req, res) => {
    try {
      const db = initDb2();
      const col = db.collection("precios_planes_scag");

      const [avanzado, basico, medio, pro, proultra] = await Promise.all([
        col.doc("avanzado").get(),
        col.doc("basico").get(),
        col.doc("medio").get(),
        col.doc("pro").get(),
        col.doc("proultra").get(),
      ]);

      const extract = (snap) => {
        if (!snap.exists) return { creditos: null, precio: null, nombre: null };
        const { creditos, precio, nombre } = snap.data();
        return { creditos, precio, nombre };
      };

      return res.status(200).json({
        success: true,
        data: {
          avanzado: extract(avanzado),
          basico: extract(basico),
          medio: extract(medio),
          pro: extract(pro),
          proultra: extract(proultra),
        },
      });
    } catch (error) {
      console.error("getPreciosPlanes error:", error);
      return res.status(500).json({ success: false, error: error.message });
    }
  },
);

// ── HELPER: elimina saltos de línea del prompt ────────────────
const flatPrompt = (str) =>
  str
    .replace(/\n+/g, " ")
    .replace(/\s{2,}/g, " ")
    .trim();

// ── FUNCIÓN 1: texto ──────────────────────────────────────────
const obtener_prompt = onRequest(
  { region: "us-central1", cors: true },
  async (req, res) => {
    const { modo, tipo, categoria, provider } = req.body;

    if (!modo || !tipo || !provider) {
      return res
        .status(400)
        .json({ error: "Faltan parámetros: modo, tipo, provider" });
    }

    const cat = categoria || "general";
    let prompt = null;
    let promptTexto = null;
    let tokens = 0;

    if (tipo === "vision") {
      switch (modo) {
        case "directo":
          prompt = flatPrompt(SYSTEM_PROMPT_VISION);
          promptTexto = flatPrompt(
            SYSTEM_PROMPTS[cat] || SYSTEM_PROMPTS["general"],
          );
          tokens = maxTokens("Vision_Procesamiento_Grafico", provider);
          break;
        case "detallado":
          prompt = flatPrompt(SYSTEM_PROMPT_VISION_DETALLADO);
          promptTexto = flatPrompt(
            SYSTEM_PROMPTS_DETALLADO[cat] ||
              SYSTEM_PROMPTS_DETALLADO["general"],
          );
          tokens = maxTokens_DETALLADO(
            "Vision_Procesamiento_Grafico",
            provider,
          );
          break;
        case "super_detallado":
          prompt = flatPrompt(SYSTEM_PROMPT_VISION_SUPER_DETALLADO);
          promptTexto = flatPrompt(
            SYSTEM_PROMPTS_SUPER_DETALLADO[cat] ||
              SYSTEM_PROMPTS_SUPER_DETALLADO["general"],
          );
          tokens = maxTokens_SUPER_DETALLADO(
            "Vision_Procesamiento_Grafico",
            provider,
          );
          break;
        default:
          return res.status(400).json({
            error: "modo inválido. Usa: directo | detallado | super_detallado",
          });
      }
      return res.status(200).json({
        prompt_vision: prompt,
        prompt_texto: promptTexto,
        max_tokens: tokens,
        categoria: cat,
        modo,
        tipo,
        provider,
      });
    } else if (tipo === "texto") {
      switch (modo) {
        case "directo":
          prompt = flatPrompt(SYSTEM_PROMPTS[cat] || SYSTEM_PROMPTS["general"]);
          tokens = maxTokens(cat, provider);
          break;
        case "detallado":
          prompt = flatPrompt(
            SYSTEM_PROMPTS_DETALLADO[cat] ||
              SYSTEM_PROMPTS_DETALLADO["general"],
          );
          tokens = maxTokens_DETALLADO(cat, provider);
          break;
        case "super_detallado":
          prompt = flatPrompt(
            SYSTEM_PROMPTS_SUPER_DETALLADO[cat] ||
              SYSTEM_PROMPTS_SUPER_DETALLADO["general"],
          );
          tokens = maxTokens_SUPER_DETALLADO(cat, provider);
          break;
        default:
          return res.status(400).json({
            error: "modo inválido. Usa: directo | detallado | super_detallado",
          });
      }
      return res.status(200).json({
        prompt_texto: prompt,
        max_tokens: tokens,
        categoria: cat,
        modo,
        tipo,
        provider,
      });
    } else {
      return res
        .status(400)
        .json({ error: "tipo inválido. Usa: vision | texto" });
    }
  },
);

// ── FUNCIÓN 2: solo vision ────────────────────────────────────
const obtener_prompt_vision = onRequest(
  { region: "us-central1", cors: true },
  async (req, res) => {
    const { modo, categoria, provider } = req.body;

    if (!modo || !provider) {
      return res
        .status(400)
        .json({ error: "Faltan parámetros: modo, provider" });
    }

    const cat = categoria || "general";
    let prompt_vision = null;
    let prompt_categoria = null;
    let tokens_vision = 0;

    switch (modo) {
      case "directo":
        prompt_vision = flatPrompt(SYSTEM_PROMPT_VISION);
        prompt_categoria = flatPrompt(
          SYSTEM_PROMPTS[cat] || SYSTEM_PROMPTS["general"],
        );
        tokens_vision = maxTokens("Vision_Procesamiento_Grafico", provider);
        break;
      case "detallado":
        prompt_vision = flatPrompt(SYSTEM_PROMPT_VISION_DETALLADO);
        prompt_categoria = flatPrompt(
          SYSTEM_PROMPTS_DETALLADO[cat] || SYSTEM_PROMPTS_DETALLADO["general"],
        );
        tokens_vision = maxTokens_DETALLADO(
          "Vision_Procesamiento_Grafico",
          provider,
        );
        break;
      case "super_detallado":
        prompt_vision = flatPrompt(SYSTEM_PROMPT_VISION_SUPER_DETALLADO);
        prompt_categoria = flatPrompt(
          SYSTEM_PROMPTS_SUPER_DETALLADO[cat] ||
            SYSTEM_PROMPTS_SUPER_DETALLADO["general"],
        );
        tokens_vision = maxTokens_SUPER_DETALLADO(
          "Vision_Procesamiento_Grafico",
          provider,
        );
        break;
      default:
        return res.status(400).json({
          error: "modo inválido. Usa: directo | detallado | super_detallado",
        });
    }

    return res.status(200).json({
      prompt_vision,
      prompt_categoria,
      tokens_vision,
      categoria: cat,
      modo,
      provider,
    });
  },
);

const setContextoTemporal = onRequest(
  { region: "us-central1", timeoutSeconds: 20, memory: "256MiB", cors: true },
  async (req, res) => {
    if (req.method === "OPTIONS") {
      res.status(204).send("");
      return;
    }
    if (req.method !== "POST") {
      res.status(405).json({ ok: false, error: "Método no permitido." });
      return;
    }

    const { numero, contexto_temporal } = req.body || {};
    const numeroLimpio = numero?.toString().trim();

    if (!numeroLimpio) {
      res.status(400).json({ ok: false, error: "Número requerido." });
      return;
    }
    if (contexto_temporal === undefined) {
      res
        .status(400)
        .json({ ok: false, error: "contexto_temporal requerido." });
      return;
    }

    try {
      const database = initDb2();
      if (!database) {
        res.status(500).json({
          ok: false,
          error: "No se pudo conectar a la base de datos.",
        });
        return;
      }

      const botRef = database.collection(BOT_SCAG_COLLECTION).doc(numeroLimpio);

      await botRef.set({ contexto_temporal }, { merge: true });

      res.status(200).json({ ok: true, numero: numeroLimpio });
    } catch (e) {
      console.error("[setContextoTemporal] error:", e.message);
      res
        .status(500)
        .json({ ok: false, error: "Error al guardar contexto: " + e.message });
    }
  },
);

// ── Obtiene solo el contexto temporal del usuario ──
const getContextoTemporal = onRequest(
  { region: "us-central1", timeoutSeconds: 20, memory: "256MiB", cors: true },
  async (req, res) => {
    if (req.method === "OPTIONS") {
      res.status(204).send("");
      return;
    }
    if (req.method !== "GET" && req.method !== "POST") {
      res.status(405).json({ ok: false, error: "Método no permitido." });
      return;
    }

    const body = req.method === "GET" ? req.query : req.body || {};
    const numeroLimpio = body.numero?.toString().trim();

    if (!numeroLimpio) {
      res.status(400).json({ ok: false, error: "Número requerido." });
      return;
    }

    try {
      const database = initDb2();
      if (!database) {
        res.status(500).json({
          ok: false,
          error: "No se pudo conectar a la base de datos.",
        });
        return;
      }

      const botRef = database.collection(BOT_SCAG_COLLECTION).doc(numeroLimpio);
      const botSnap = await botRef.get();

      if (!botSnap.exists) {
        res
          .status(404)
          .json({ ok: false, error: "Número no encontrado en bot_scag." });
        return;
      }

      const contexto_temporal = botSnap.data()?.contexto_temporal ?? null;

      res.status(200).json({ ok: true, contexto_temporal });
    } catch (e) {
      console.error("[getContextoTemporal] error:", e.message);
      res
        .status(500)
        .json({ ok: false, error: "Error al obtener contexto: " + e.message });
    }
  },
);

module.exports = {
  setContextoTemporal,
  getContextoTemporal,
  obtener_prompt_vision,
  obtener_prompt,
  getPreciosPlanes,
  callOpenAIText,
  callOpenAIVision,
  callGeminiText,
  callGeminiVision,
  screenaiQuery,
  getUserData,
  getUserData_Extencion,
  suggestConfig,
  verificarAlias,
  esRespuestaValida,
  obtenerCostoDesdeDB,
  obtenerCostoCategoria,
  obtenerCostoSolucion,
  descontarCreditoN,
  guardarHistorial,
  MODEL_MAP,
  SYSTEM_PROMPTS,
  maxTokens,
  resolverCategoria,
};