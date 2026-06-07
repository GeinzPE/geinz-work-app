const { onCall, HttpsError } = require("firebase-functions/v2/https");
const axios = require("axios");
const admin = require("firebase-admin"); 
const db = admin.firestore();
const GEMINIKEY = process.env.PRIVATEKEY_GEMINI;
const GEMINI_URL =
  "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent";
const TIMEOUT = 30000;
const { actualizar_creditos_tienda } = require("./test_db2");
// ─── Helper: llamada a Gemini ────────────────────────────────────────────────

async function llamarGemini(parts) {
  if (!GEMINIKEY) {
    throw new HttpsError(
      "failed-precondition",
      "API key de Gemini no configurada",
    );
  }

  const response = await axios.post(
    `${GEMINI_URL}?key=${GEMINIKEY}`,
    { contents: [{ parts }] },
    { headers: { "Content-Type": "application/json" }, timeout: TIMEOUT },
  );

  const texto =
    response.data?.candidates?.[0]?.content?.parts?.[0]?.text?.trim() || "";

  if (!texto) {
    throw new HttpsError("internal", "Gemini no devolvió contenido");
  }

  return texto;
}

// ─── Helper: limpiar y parsear JSON de Gemini ────────────────────────────────

function parsearJSON(texto) {
  // Elimina bloques de markdown ```json ... ``` si Gemini los incluye
  const limpio = texto
    .replace(/^```(?:json)?\s*/i, "")
    .replace(/\s*```$/, "")
    .trim();

  return JSON.parse(limpio);
}

// ─── 1. Generar título y descripción desde imagen ────────────────────────────

exports.generar_titulo_descripcion_IA = onCall(async (request) => {
  console.log("🚀 generar_titulo_descripcion_IA iniciado");

  const {
    imageBase64,
    mimeType,
    tipo,
    saldo_actual,
    saldo_descuento,
    id_tienda,
    precio_por_moneda,
    localidad,
    nombre_tienda,
    tipo_paquete,
  } = request.data;

  console.log("📥 Datos recibidos:", {
    tipo,
    saldo_actual,
    saldo_descuento,
    id_tienda,
    precio_por_moneda,
    localidad,
    nombre_tienda,
    mimeType,
    imageBase64_length: imageBase64 ? imageBase64.length : 0,
  });

  if (!imageBase64) {
    throw new HttpsError(
      "invalid-argument",
      "El campo imageBase64 es requerido",
    );
  }

  const tipoTexto = tipo && tipo.trim() ? tipo.trim() : "publicación de venta";

  const prompt = `Analiza la imagen y genera contenido para una publicación de tipo: ${tipoTexto}.

Responde SOLO con JSON válido, sin texto adicional, sin markdown, sin bloques de código:

{
  "titulo": "máximo 15 palabras, orientado a venta, incluye precio en soles si aparece en la imagen",
  "descripcion": "máximo 3 líneas, estilo persuasivo de venta"
}

Reglas estrictas:
- Sin markdown
- Sin asteriscos
- Sin texto fuera del JSON
- No inventes información que no esté en la imagen`;

  try {
    const texto = await llamarGemini([
      { text: prompt },
      {
        inline_data: {
          mime_type: mimeType && mimeType.trim() ? mimeType : "image/jpeg",
          data: imageBase64,
        },
      },
    ]);

    console.log("🤖 Respuesta RAW Gemini:", texto);

    let json;
    try {
      json = parsearJSON(texto);
      console.log("✅ JSON parseado:", json);
    } catch (e) {
      throw new HttpsError(
        "internal",
        "Gemini devolvió JSON inválido: " + texto.slice(0, 100),
      );
    }

    if (!json.titulo && !json.descripcion) {
      throw new HttpsError(
        "internal",
        "La respuesta no contiene título ni descripción",
      );
    }

    // ── Cálculo financiero ──────────────────────────────────────────
    const monto_descontado = saldo_descuento;
    const monto_restante = saldo_actual - monto_descontado;
    const precio_por_moneda_num = parseFloat(precio_por_moneda);

const precio_soles = (!isNaN(precio_por_moneda_num))
  ? (monto_descontado * precio_por_moneda_num).toFixed(2)
  : "0.00";
console.log("🔍 precio_por_moneda tipo:", typeof precio_por_moneda, "valor:", precio_por_moneda);

    console.log("💰 Datos financieros:", {
      monto_descontado,
      monto_restante,
      precio_soles,
    });

    // ── ID transacción ──────────────────────────────────────────────
    const id_transaccion = uuidv4();
    console.log("🆔 ID transacción:", id_transaccion);

    // ── Fecha Lima UTC-5 ────────────────────────────────────────────
    const ahora = new Date();
    const offset = -5 * 60;
    const lima = new Date(
      ahora.getTime() + (offset - ahora.getTimezoneOffset()) * 60000,
    );
    const fecha = `${String(lima.getMonth() + 1).padStart(2, "0")}/${String(lima.getDate()).padStart(2, "0")}/${lima.getFullYear()}`;
    const hora = `${String(lima.getHours()).padStart(2, "0")}:${String(lima.getMinutes()).padStart(2, "0")}`;

    console.log("📅 Fecha/Hora Lima:", { fecha, hora });

    // ── 1. Descontar puntos_tienda ──────────────────────────────────
    const tiendaRef = db
      .collection("Tiendas")
      .doc(localidad)
      .collection(localidad)
      .doc(id_tienda);

    await tiendaRef.update({
      puntos_tienda: admin.firestore.FieldValue.increment(-monto_descontado),
    });

    console.log("✅ puntos_tienda descontado");

    // ── 2. Actualizar creditos_tienda (si tiene bot_plan_pro) ───────
    const tiendaSnap = await tiendaRef.get();
    const tieneBotPlanPro =
      tiendaSnap.exists && tiendaSnap.data()?.bot_plan_pro != null;

    if (tieneBotPlanPro) {
      const creditosResult = await actualizar_creditos_tienda(
        id_tienda,
        monto_restante,
      );
      console.log("✅ creditos_tienda actualizado:", creditosResult);
    } else {
      console.log("ℹ️ Sin bot_plan_pro — creditos_tienda no actualizado");
    }

    // ── 3. Guardar historial financiero ─────────────────────────────
    await db
      .collection("Tiendas")
      .doc(localidad)
      .collection(localidad)
      .doc(id_tienda)
      .collection("historial_financiero")
      .doc(id_transaccion)
      .set({
        datos_recarga: {
          estado: "Aceptado",
          monto_descontado: monto_descontado,
          monto_restante: monto_restante,
          precio_soles: precio_soles,
          tipo_paquete: tipo_paquete || "Gen IA",
        },
        datos_tienda: {
          id_tienda: id_tienda,
          localidad_tienda: localidad,
          nombre_tienda: nombre_tienda,
        },
        hora_fecha: { fecha, hora },
        id_transaccion: id_transaccion,
        timestamp: admin.firestore.FieldValue.serverTimestamp(),
        tipo_transacción: "descuento",
      });

    console.log("✅ Historial financiero guardado");

    // ── Retorno al cliente ──────────────────────────────────────────
    return {
      ok: true,
      titulo: json.titulo || "",
      descripcion: json.descripcion || "",
    };
  } catch (error) {
    console.error("💥 ERROR GENERAL generar_titulo_descripcion_IA:", {
      message: error?.message,
      stack: error?.stack,
    });
    if (error instanceof HttpsError) throw error;
    throw new HttpsError(
      "internal",
      error.message || "Error generando contenido desde imagen",
    );
  }
});
// ─── 2. Mejorar texto (título + descripción) con enfoque ────────────────────

const TIPOS_VALIDOS = ["VENTA", "ATENCION", "INFORMATIVO"];

function generarPromptSegunTipo(tipo, tituloUsuario, descripcionUsuario) {
  const enfoques = {
    VENTA: {
      nombre: "VENTA DIRECTA",
      instrucciones: `- Llamados a la acción claros (ej: Aprovecha, Compra hoy, No te lo pierdas)
- Tono comercial y persuasivo
- Sin emojis
- No exageres beneficios irreales`,
    },
    ATENCION: {
      nombre: "LLAMAR LA ATENCIÓN",
      instrucciones: `- Usa preguntas, ganchos creativos o beneficios impactantes
- Tono claro y atractivo
- Sin emojis
- Sin promesas falsas`,
    },
    INFORMATIVO: {
      nombre: "PROFESIONAL E INFORMATIVO",
      instrucciones: `- Explica el valor del producto o servicio sin exageraciones
- Tono serio, elegante y confiable
- Sin emojis`,
    },
  };

  const cfg = enfoques[tipo] || enfoques["VENTA"];

  return `Mejora el título y la descripción de una promoción con ENFOQUE EN ${cfg.nombre}.
Usa SOLO la información proporcionada. NO inventes datos ni precios.
Si el usuario menciona un precio (ej: 120, cuesta 120, S/120), usa el símbolo s/

Reglas generales:
- Genera EXACTAMENTE 3 opciones distintas
- Título ≤60 caracteres
- Descripción entre 30 y 50 palabras
- Español
${cfg.instrucciones}

Datos del usuario:
titulo: ${tituloUsuario}
descripcion: ${descripcionUsuario}

Responde con este formato EXACTO (sin texto adicional):
Opcion 1:
T:
D:

Opcion 2:
T:
D:

Opcion 3:
T:
D:`.trim();
}

exports.generar_texto_ia = onCall(async (request) => {
  console.log("🚀 generar_texto_ia iniciado");

  const {
    tipo,
    tituloUsuario,
    descripcionUsuario,
    saldo_actual,
    saldo_descuento,
    id_tienda,
    precio_por_moneda,
    localidad,
    nombre_tienda,
    tipo_paquete,
  } = request.data;

  console.log("📥 Datos recibidos:", {
    tipo,
    tituloUsuario,
    descripcionUsuario,
    saldo_actual,
    saldo_descuento,
    id_tienda,
    precio_por_moneda,
    localidad,
    nombre_tienda,
  });

  if (!tituloUsuario || !tituloUsuario.trim()) {
    throw new HttpsError(
      "invalid-argument",
      "El campo tituloUsuario es requerido",
    );
  }

  if (!descripcionUsuario || !descripcionUsuario.trim()) {
    throw new HttpsError(
      "invalid-argument",
      "El campo descripcionUsuario es requerido",
    );
  }

  const tipoFinal =
    tipo && TIPOS_VALIDOS.includes(tipo.toUpperCase())
      ? tipo.toUpperCase()
      : "VENTA";

  const prompt = generarPromptSegunTipo(
    tipoFinal,
    tituloUsuario.trim(),
    descripcionUsuario.trim(),
  );

  try {
    const respuesta = await llamarGemini([{ text: prompt }]);

    console.log("🤖 Respuesta Gemini:", respuesta);

    // ── Cálculo financiero ──────────────────────────────────────────
    const monto_descontado = saldo_descuento;
    const monto_restante = saldo_actual - monto_descontado;
    const precio_soles = (monto_descontado * precio_por_moneda).toFixed(2);

    console.log("💰 Datos financieros:", {
      monto_descontado,
      monto_restante,
      precio_soles,
    });

    // ── ID transacción ──────────────────────────────────────────────
    const id_transaccion = uuidv4();
    console.log("🆔 ID transacción:", id_transaccion);

    // ── Fecha Lima UTC-5 ────────────────────────────────────────────
    const ahora = new Date();
    const offset = -5 * 60;
    const lima = new Date(
      ahora.getTime() + (offset - ahora.getTimezoneOffset()) * 60000,
    );
    const fecha = `${String(lima.getMonth() + 1).padStart(2, "0")}/${String(lima.getDate()).padStart(2, "0")}/${lima.getFullYear()}`;
    const hora = `${String(lima.getHours()).padStart(2, "0")}:${String(lima.getMinutes()).padStart(2, "0")}`;

    console.log("📅 Fecha/Hora Lima:", { fecha, hora });

    // ── 1. Descontar puntos_tienda ──────────────────────────────────
    const tiendaRef = db
      .collection("Tiendas")
      .doc(localidad)
      .collection(localidad)
      .doc(id_tienda);

    await tiendaRef.update({
      puntos_tienda: admin.firestore.FieldValue.increment(-monto_descontado),
    });

    console.log("✅ puntos_tienda descontado");

    // ── 2. Actualizar creditos_tienda (si tiene bot_plan_pro) ───────
    const tiendaSnap = await tiendaRef.get();
    const tieneBotPlanPro =
      tiendaSnap.exists && tiendaSnap.data()?.bot_plan_pro != null;

    if (tieneBotPlanPro) {
      const creditosResult = await actualizar_creditos_tienda(
        id_tienda,
        monto_restante,
      );
      console.log("✅ creditos_tienda actualizado:", creditosResult);
    } else {
      console.log("ℹ️ Sin bot_plan_pro — creditos_tienda no actualizado");
    }

    // ── 3. Guardar historial financiero ─────────────────────────────
    await db
      .collection("Tiendas")
      .doc(localidad)
      .collection(localidad)
      .doc(id_tienda)
      .collection("historial_financiero")
      .doc(id_transaccion)
      .set({
        datos_recarga: {
          estado: "Aceptado",
          monto_descontado: monto_descontado,
          monto_restante: monto_restante,
          precio_soles: precio_soles,
          tipo_paquete: tipo_paquete || "Gen IA",
        },
        datos_tienda: {
          id_tienda: id_tienda,
          localidad_tienda: localidad,
          nombre_tienda: nombre_tienda,
        },
        hora_fecha: { fecha, hora },
        id_transaccion: id_transaccion,
        timestamp: admin.firestore.FieldValue.serverTimestamp(),
        tipo_transacción: "descuento",
      });

    console.log("✅ Historial financiero guardado");

    // ── Retorno al cliente ──────────────────────────────────────────
    return { ok: true, respuesta };
  } catch (error) {
    console.error("💥 ERROR GENERAL generar_texto_ia:", {
      message: error?.message,
      stack: error?.stack,
    });
    if (error instanceof HttpsError) throw error;
    throw new HttpsError(
      "internal",
      error.message || "Error generando texto IA",
    );
  }
});

// ─── 3. Generar mensaje para compartir ──────────────────────────────────────

exports.generar_texto_compartir_ia = onCall(async (request) => {
  console.log("🚀 generar_texto_compartir_ia iniciado");

  const {
    tituloUsuario,
    descripcionUsuario,
    saldo_actual,
    saldo_descuento,
    id_tienda,
    precio_por_moneda,
    localidad,
    nombre_tienda,
    tipo_paquete,
  } = request.data;

  console.log("📥 Datos recibidos:", {
    tituloUsuario,
    saldo_actual,
    saldo_descuento,
    id_tienda,
    precio_por_moneda,
    localidad,
    nombre_tienda,
    tipo_paquete,
  });

  if (!tituloUsuario || !tituloUsuario.trim()) {
    throw new HttpsError(
      "invalid-argument",
      "El campo tituloUsuario es requerido",
    );
  }

  const descTexto =
    descripcionUsuario && descripcionUsuario.trim()
      ? descripcionUsuario.trim()
      : "";

  const prompt =
    `Crea un mensaje muy corto para compartir en redes o WhatsApp que provoque clic inmediato.

Reglas estrictas:
- Máximo 80 caracteres
- Español
- Usa información concreta del título
- Inicio fuerte y directo
- EXACTAMENTE 2 emojis
- Sin preguntas
- Sin relleno
- Devuelve SOLO el texto, sin comillas, sin explicaciones

Datos:
Título: ${tituloUsuario.trim()}
Descripción: ${descTexto}`.trim();

  try {
    const mensaje = await llamarGemini([{ text: prompt }]);

    console.log("🤖 Respuesta Gemini:", mensaje);

    // ── Cálculo financiero ──────────────────────────────────────────
    const monto_descontado = saldo_descuento;
    const monto_restante = saldo_actual - monto_descontado;
    const precio_soles = (monto_descontado * precio_por_moneda).toFixed(2);

    console.log("💰 Datos financieros:", {
      monto_descontado,
      monto_restante,
      precio_soles,
    });

    // ── ID transacción ──────────────────────────────────────────────
    const id_transaccion = uuidv4();
    console.log("🆔 ID transacción:", id_transaccion);

    // ── Fecha Lima UTC-5 ────────────────────────────────────────────
    const ahora = new Date();
    const offset = -5 * 60;
    const lima = new Date(
      ahora.getTime() + (offset - ahora.getTimezoneOffset()) * 60000,
    );
    const fecha = `${String(lima.getMonth() + 1).padStart(2, "0")}/${String(lima.getDate()).padStart(2, "0")}/${lima.getFullYear()}`;
    const hora = `${String(lima.getHours()).padStart(2, "0")}:${String(lima.getMinutes()).padStart(2, "0")}`;

    console.log("📅 Fecha/Hora Lima:", { fecha, hora });

    // ── 1. Descontar puntos_tienda ──────────────────────────────────
    const tiendaRef = db
      .collection("Tiendas")
      .doc(localidad)
      .collection(localidad)
      .doc(id_tienda);

    await tiendaRef.update({
      puntos_tienda: admin.firestore.FieldValue.increment(-monto_descontado),
    });

    console.log("✅ puntos_tienda descontado");

    // ── 2. Actualizar creditos_tienda (si tiene bot_plan_pro) ───────
    const tiendaSnap = await tiendaRef.get();
    const tieneBotPlanPro =
      tiendaSnap.exists && tiendaSnap.data()?.bot_plan_pro != null;

    if (tieneBotPlanPro) {
      const creditosResult = await actualizar_creditos_tienda(
        id_tienda,
        monto_restante,
      );
      console.log("✅ creditos_tienda actualizado:", creditosResult);
    } else {
      console.log("ℹ️ Sin bot_plan_pro — creditos_tienda no actualizado");
    }

    // ── 3. Guardar historial financiero ─────────────────────────────
    await db
      .collection("Tiendas")
      .doc(localidad)
      .collection(localidad)
      .doc(id_tienda)
      .collection("historial_financiero")
      .doc(id_transaccion)
      .set({
        datos_recarga: {
          estado: "Aceptado",
          monto_descontado: monto_descontado,
          monto_restante: monto_restante,
          precio_soles: precio_soles,
          tipo_paquete: tipo_paquete || "Gen IA",
        },
        datos_tienda: {
          id_tienda: id_tienda,
          localidad_tienda: localidad,
          nombre_tienda: nombre_tienda,
        },
        hora_fecha: { fecha, hora },
        id_transaccion: id_transaccion,
        timestamp: admin.firestore.FieldValue.serverTimestamp(),
        tipo_transacción: "descuento",
      });

    console.log("✅ Historial financiero guardado");

    // ── Retorno al cliente ──────────────────────────────────────────
    return { ok: true, mensaje };
  } catch (error) {
    console.error("💥 ERROR GENERAL generar_texto_compartir_ia:", {
      message: error?.message,
      stack: error?.stack,
    });
    if (error instanceof HttpsError) throw error;
    throw new HttpsError(
      "internal",
      error.message || "Error generando mensaje para compartir",
    );
  }
});
// ─── 4. Generar mensaje de contacto por WhatsApp ─────────────────────────────

exports.generar_whatsapp_contacto_ia = onCall(async (request) => {
  console.log("🚀 generar_whatsapp_contacto_ia iniciado");

  const {
    titulo,
    descripcion,
    saldo_actual,
    saldo_descuento,
    id_tienda,
    precio_por_moneda,
    localidad,
    nombre_tienda,
    tipo_paquete,
  } = request.data;

  console.log("📥 Datos recibidos:", {
    titulo,
    saldo_actual,
    saldo_descuento,
    id_tienda,
    precio_por_moneda,
    localidad,
    nombre_tienda,
    tipo_paquete,
  });

  if (!titulo || !titulo.trim()) {
    throw new HttpsError("invalid-argument", "El campo titulo es requerido");
  }

  const descTexto = descripcion && descripcion.trim() ? descripcion.trim() : "";

  const prompt =
    `Actúa como un cliente interesado que va a enviar un mensaje por WhatsApp al vendedor.

Reglas estrictas:
- Máximo 60 caracteres
- Español natural y respetuoso
- Estructura: saludo breve + interés en el producto + pregunta de disponibilidad
- EXACTAMENTE 1 emoji
- No inventes datos ni precios
- Devuelve SOLO el mensaje, sin comillas, sin explicaciones

Datos del producto:
Título: ${titulo.trim()}
Descripción: ${descTexto}`.trim();

  try {
    const mensaje = await llamarGemini([{ text: prompt }]);

    console.log("🤖 Respuesta Gemini:", mensaje);

    // ── Cálculo financiero ──────────────────────────────────────────
    const monto_descontado = saldo_descuento;
    const monto_restante = saldo_actual - monto_descontado;
    const precio_soles = (monto_descontado * precio_por_moneda).toFixed(2);

    console.log("💰 Datos financieros:", {
      monto_descontado,
      monto_restante,
      precio_soles,
    });

    // ── ID transacción ──────────────────────────────────────────────
    const id_transaccion = uuidv4();
    console.log("🆔 ID transacción:", id_transaccion);

    // ── Fecha Lima UTC-5 ────────────────────────────────────────────
    const ahora = new Date();
    const offset = -5 * 60;
    const lima = new Date(
      ahora.getTime() + (offset - ahora.getTimezoneOffset()) * 60000,
    );
    const fecha = `${String(lima.getMonth() + 1).padStart(2, "0")}/${String(lima.getDate()).padStart(2, "0")}/${lima.getFullYear()}`;
    const hora = `${String(lima.getHours()).padStart(2, "0")}:${String(lima.getMinutes()).padStart(2, "0")}`;

    console.log("📅 Fecha/Hora Lima:", { fecha, hora });

    // ── 1. Descontar puntos_tienda ──────────────────────────────────
    const tiendaRef = db
      .collection("Tiendas")
      .doc(localidad)
      .collection(localidad)
      .doc(id_tienda);

    await tiendaRef.update({
      puntos_tienda: admin.firestore.FieldValue.increment(-monto_descontado),
    });

    console.log("✅ puntos_tienda descontado");

    // ── 2. Actualizar creditos_tienda (si tiene bot_plan_pro) ───────
    const tiendaSnap = await tiendaRef.get();
    const tieneBotPlanPro =
      tiendaSnap.exists && tiendaSnap.data()?.bot_plan_pro != null;

    if (tieneBotPlanPro) {
      const creditosResult = await actualizar_creditos_tienda(
        id_tienda,
        monto_restante,
      );
      console.log("✅ creditos_tienda actualizado:", creditosResult);
    } else {
      console.log("ℹ️ Sin bot_plan_pro — creditos_tienda no actualizado");
    }

    // ── 3. Guardar historial financiero ─────────────────────────────
    await db
      .collection("Tiendas")
      .doc(localidad)
      .collection(localidad)
      .doc(id_tienda)
      .collection("historial_financiero")
      .doc(id_transaccion)
      .set({
        datos_recarga: {
          estado: "Aceptado",
          monto_descontado: monto_descontado,
          monto_restante: monto_restante,
          precio_soles: precio_soles,
          tipo_paquete: tipo_paquete || "Gen IA",
        },
        datos_tienda: {
          id_tienda: id_tienda,
          localidad_tienda: localidad,
          nombre_tienda: nombre_tienda,
        },
        hora_fecha: { fecha, hora },
        id_transaccion: id_transaccion,
        timestamp: admin.firestore.FieldValue.serverTimestamp(),
        tipo_transacción: "descuento",
      });

    console.log("✅ Historial financiero guardado");

    // ── Retorno al cliente ──────────────────────────────────────────
    return { ok: true, mensaje };
  } catch (error) {
    console.error("💥 ERROR GENERAL generar_whatsapp_contacto_ia:", {
      message: error?.message,
      stack: error?.stack,
    });
    if (error instanceof HttpsError) throw error;
    throw new HttpsError(
      "internal",
      error.message || "Error generando mensaje de WhatsApp",
    );
  }
});

// ─── 5. Generar terminos_calves_filtrado ─────────────────────────────

exports.extraerTerminosClaveIA = onCall(async (request) => {
  const { textoUsuario, categoria, nombreNegocio } = request.data;

  console.log("[extraerTerminosClaveIA] INICIO — data recibida:", {
    textoUsuario: textoUsuario?.slice(0, 80),
    categoria,
    nombreNegocio,
  });

  if (!textoUsuario?.trim())
    throw new HttpsError("invalid-argument", "El campo textoUsuario es requerido");
  if (!categoria?.trim())
    throw new HttpsError("invalid-argument", "El campo categoria es requerido");
  if (!nombreNegocio?.trim())
    throw new HttpsError("invalid-argument", "El campo nombreNegocio es requerido");

  const palabrasProhibidas = [
    ...categoria.toLowerCase().split(/\s+/),
    ...nombreNegocio.toLowerCase().split(/\s+/),
  ].filter((p) => p.length > 2);

  console.log("[extraerTerminosClaveIA] palabrasProhibidas:", palabrasProhibidas);

  const quitarTildes = (str) =>
    str.normalize("NFD").replace(/[\u0300-\u036f]/g, "");

const prompt = `
[INPUT]
Texto: "${textoUsuario}"
Categoria del negocio: "${categoria}"
Filtros prohibidos: "${nombreNegocioFinal}"

[INSTRUCCIONES]
Extrae de 'Texto' un array JSON de strings con máximo 6 términos clave para motores de búsqueda.
1. Prioriza ÚNICAMENTE: nombres propios, lugares, marcas, modelos, productos o servicios MUY específicos mencionados en el texto.
2. PROHIBIDO: 
   - Palabras genéricas que describan la categoría "${categoria}" (ej: si es transporte, excluir "viaje","pasaje","bus","ruta")
   - Adjetivos, verbos, precios, palabras de marketing ("oferta","promo","descuento","oportunidad")
   - Cualquier palabra o fragmento de 'Filtros prohibidos'
3. Solo incluir términos que por sí solos sirvan como búsqueda específica en Google.
4. Formato: minúsculas, singular, sin tildes, sin duplicados.
5. Si no hay términos específicos válidos, devuelve [].

[OUTPUT]
Contesta ÚNICAMENTE con el array JSON. Ejemplo: ["tag1", "tag2"]
`.trim();
  try {
    console.log("[extraerTerminosClaveIA] Llamando a Gemini...");
    const respuesta = await llamarGemini([{ text: prompt }]);
    console.log("[extraerTerminosClaveIA] Respuesta raw Gemini:", respuesta);

    const jsonStr = respuesta
      .trim()
      .replace(/```json\n?/g, "")
      .replace(/```\n?/g, "");

    console.log("[extraerTerminosClaveIA] jsonStr limpio:", jsonStr);

    const terminos = JSON.parse(jsonStr);
    console.log("[extraerTerminosClaveIA] terminos parseados:", terminos);

    const terminosNormalizados = Array.isArray(terminos)
      ? terminos
          .map((item) =>
            typeof item === "string" ? item :
            typeof item === "object" ? (item.term ?? item.value ?? Object.values(item)[0] ?? "") :
            String(item)
          )
          .map((t) => quitarTildes(t.toLowerCase().trim()))
          .filter((t) => t.length > 2)
          .filter((t) => !palabrasProhibidas.some((p) => t.includes(p)))
          .filter((t, i, arr) => arr.indexOf(t) === i)
          .slice(0, 6)
      : [];

    console.log("[extraerTerminosClaveIA] RESULTADO FINAL:", terminosNormalizados);

    return { ok: true, terminos: terminosNormalizados };

  } catch (error) {
    if (error instanceof HttpsError) throw error;
    console.error("[extraerTerminosClaveIA] ERROR:", {
      message: error.message,
      stack: error.stack,
    });
    throw new HttpsError("internal", error.message || "Error extrayendo términos clave");
  }
});

// ─── 6. Generar descripcionSEOIA ─────────────────────────────

exports.generar_descripcion_whatsapp_ia = onCall(async (request) => {
  console.log("🚀 generar_descripcion_whatsapp_ia iniciado");

  const {
    texto,
    saldo_actual,
    saldo_descuento,
    id_tienda,
    precio_por_moneda,
    localidad,
    nombre_tienda,
    tipo_paquete,
  } = request.data;

  console.log("📥 Datos recibidos:", {
    saldo_actual,
    saldo_descuento,
    id_tienda,
    precio_por_moneda,
    localidad,
    nombre_tienda,
    tipo_paquete,
  });

  if (!texto || !texto.trim()) {
    throw new HttpsError("invalid-argument", "El campo texto es requerido");
  }

  const prompt = `
Eres un optimizador SEO local.
Tarea: Crear descripción de negocio para WhatsApp.

Restricciones:
1. El texto debe tener más de 120 y menos de 150 caracteres.
2. Texto plano, sin saludos, sin emojis, sin introducciones.
3. No repitas el nombre del negocio.
4. Prioriza beneficios y palabras clave para SEO.
5. Español neutro.
6. Devuelve SOLO el texto final en una sola línea.
7. No uses comillas.
8. No expliques nada.

Input:
${texto.trim()}
`.trim();

  try {
    const descripcion = await llamarGemini([{ text: prompt }]);

    console.log("🤖 Respuesta Gemini:", descripcion);

    // ── Cálculo financiero ──────────────────────────────────────────
    const monto_descontado = saldo_descuento;
    const monto_restante = saldo_actual - monto_descontado;
    const precio_soles = (monto_descontado * precio_por_moneda).toFixed(2);

    console.log("💰 Datos financieros:", {
      monto_descontado,
      monto_restante,
      precio_soles,
    });

    // ── ID transacción ──────────────────────────────────────────────
    const id_transaccion = uuidv4();
    console.log("🆔 ID transacción:", id_transaccion);

    // ── Fecha Lima UTC-5 ────────────────────────────────────────────
    const ahora = new Date();
    const offset = -5 * 60;
    const lima = new Date(
      ahora.getTime() + (offset - ahora.getTimezoneOffset()) * 60000,
    );
    const fecha = `${String(lima.getMonth() + 1).padStart(2, "0")}/${String(lima.getDate()).padStart(2, "0")}/${lima.getFullYear()}`;
    const hora = `${String(lima.getHours()).padStart(2, "0")}:${String(lima.getMinutes()).padStart(2, "0")}`;

    console.log("📅 Fecha/Hora Lima:", { fecha, hora });

    // ── 1. Descontar puntos_tienda ──────────────────────────────────
    const tiendaRef = db
      .collection("Tiendas")
      .doc(localidad)
      .collection(localidad)
      .doc(id_tienda);

    await tiendaRef.update({
      puntos_tienda: admin.firestore.FieldValue.increment(-monto_descontado),
    });

    console.log("✅ puntos_tienda descontado");

    // ── 2. Actualizar creditos_tienda (si tiene bot_plan_pro) ───────
    const tiendaSnap = await tiendaRef.get();
    const tieneBotPlanPro =
      tiendaSnap.exists && tiendaSnap.data()?.bot_plan_pro != null;

    if (tieneBotPlanPro) {
      const creditosResult = await actualizar_creditos_tienda(
        id_tienda,
        monto_restante,
      );
      console.log("✅ creditos_tienda actualizado:", creditosResult);
    } else {
      console.log("ℹ️ Sin bot_plan_pro — creditos_tienda no actualizado");
    }

    // ── 3. Guardar historial financiero ─────────────────────────────
    await db
      .collection("Tiendas")
      .doc(localidad)
      .collection(localidad)
      .doc(id_tienda)
      .collection("historial_financiero")
      .doc(id_transaccion)
      .set({
        datos_recarga: {
          estado: "Aceptado",
          monto_descontado: monto_descontado,
          monto_restante: monto_restante,
          precio_soles: precio_soles,
          tipo_paquete: tipo_paquete || "Gen IA",
        },
        datos_tienda: {
          id_tienda: id_tienda,
          localidad_tienda: localidad,
          nombre_tienda: nombre_tienda,
        },
        hora_fecha: { fecha, hora },
        id_transaccion: id_transaccion,
        timestamp: admin.firestore.FieldValue.serverTimestamp(),
        tipo_transacción: "descuento",
      });

    console.log("✅ Historial financiero guardado");

    // ── Retorno al cliente ──────────────────────────────────────────
    return { ok: true, descripcion };
  } catch (error) {
    console.error("💥 ERROR GENERAL generar_descripcion_whatsapp_ia:", {
      message: error?.message,
      stack: error?.stack,
    });
    if (error instanceof HttpsError) throw error;
    throw new HttpsError(
      "internal",
      error.message || "Error generando descripción para WhatsApp",
    );
  }
});

// ============================================
// CREAR PROMOCIÓN (SOLO FIRESTORE)
// ============================================
exports.crearPromocion = onCall(async (request) => {
  try {
    const data = request.data.data || request.data;

    const {
      id_tienda,
      id_promocion,
      localidad,
      estado = "activo",
      exclusivo = false,
      formato_fecha_hora = "dias",
      categoria,
      descripcion,
      titulo,
      numero,
      compartir = true,
      contactar = true,
      nombre_tienda,
      direccion,
      lat,
      long,
      referencia,
      fecha_inicio,
      fecha_fin,
      hora_inicio,
      hora_fin,
      timestamp_inicio,
      timestamp_fin,
      mensaje_compartir,
      mensaje_whatsapp,
      activo_mensaje_compartir = true,
      activo_mensaje_whatsapp = true,
      precio,
      horario_seleccion = "todo_dia",
      yape = false,
      plin = false,
      agora = false,
      efectivo = false,
      visa = false,
      mastercard = false,
      servicios_comodidades = {},
      terminos_clave_ia,
      imagenes_base64 = [],
      urls_imagenes = [],
      img_bot: img_bot_param = "",
      logo_url = "",
      // ── Financiero ──────────────────────────
      saldo_actual,
      saldo_descuento,
      precio_por_moneda,
      tipo_paquete,
    } = data;

    // ── Validaciones ──────────────────────────────────────
    if (!id_tienda || !id_promocion || !localidad) {
      throw new HttpsError("invalid-argument", "Faltan campos requeridos");
    }

    if (imagenes_base64.length === 0 && urls_imagenes.length === 0) {
      throw new HttpsError(
        "invalid-argument",
        "Debes subir al menos una imagen",
      );
    }

    console.log("📌 terminos_clave_ia:", terminos_clave_ia);
    console.log("📌 urls_imagenes recibidas:", urls_imagenes);

    // ── PASO 1: Obtener URLs ──────────────────────────────
    let urls = [];
    let img_bot = null;

    if (urls_imagenes.length > 0) {
      urls = urls_imagenes;
      img_bot = img_bot_param || urls[0] || null;
      console.log("✅ Usando URLs ya subidas desde el front:", urls);
    } else {
      const subirImagenes = async () => {
        const bucket = admin.storage().bucket();
        const resultUrls = [];
        for (let idx = 0; idx < imagenes_base64.length; idx++) {
          const {
            base64,
            nombre,
            mimeType = "image/jpeg",
          } = imagenes_base64[idx];
          const buffer = Buffer.from(base64, "base64");
          const path = `promociones/${localidad}/${id_tienda}/${id_promocion}/${nombre || `img_${idx}.jpg`}`;
          const file = bucket.file(path);
          await file.save(buffer, { metadata: { contentType: mimeType } });
          await file.makePublic();
          resultUrls.push(file.publicUrl());
        }
        return resultUrls;
      };

      const subirConReintento = async (intentos = 3) => {
        for (let i = 0; i < intentos - 1; i++) {
          try {
            return await subirImagenes();
          } catch (e) {
            console.warn(`Intento ${i + 1} fallido:`, e.message);
          }
        }
        return await subirImagenes();
      };

      urls = await subirConReintento(3);
      if (urls.length !== imagenes_base64.length) {
        throw new HttpsError(
          "internal",
          "No se pudieron subir todas las imágenes",
        );
      }
      img_bot = urls[0] ?? null;
    }

    // ── PASO 2: img_container ─────────────────────────────
    const imgContainer = {
      lista_img: urls,
      logo_img: logo_url,
    };

    // ── PASO 3: Preparar datos ────────────────────────────
    const terminosClave =
      Array.isArray(terminos_clave_ia) && terminos_clave_ia.length > 0
        ? terminos_clave_ia
        : [];

    const comodidadesArray = [];
    const comodidadMap = {
      zonaExpandida: "zona_expandida",
      wifi: "wifi",
      serviciosHigienicos: "servicios_higienicos",
      camarasSeguridad: "camaras_seguridad",
      salaEspera: "sala_espera",
      salaJuegos: "sala_juegos",
      mesaParaNinos: "mesa_para_ninos",
      ingresoConMascotas: "ingreso_con_mascotas",
      estacionamiento: "estacionamiento",
      enchufe: "enchufe",
      aireAcondicionado: "aire_acondicionado",
    };
    for (const [key, value] of Object.entries(servicios_comodidades)) {
      if (value === true && comodidadMap[key])
        comodidadesArray.push(comodidadMap[key]);
    }

    const pagosArray = [];
    if (yape) pagosArray.push("yape");
    if (plin) pagosArray.push("plin");
    if (agora) pagosArray.push("agora");
    if (efectivo) pagosArray.push("efectivo");
    if (visa) pagosArray.push("visa");
    if (mastercard) pagosArray.push("mastercard");

    const nowTimestamp = admin.firestore.Timestamp.now();
    const tsInicio = timestamp_inicio
      ? new admin.firestore.Timestamp(
          timestamp_inicio.seconds,
          timestamp_inicio.nanoseconds,
        )
      : nowTimestamp;
    const tsFin = timestamp_fin
      ? new admin.firestore.Timestamp(
          timestamp_fin.seconds,
          timestamp_fin.nanoseconds,
        )
      : nowTimestamp;

    // ── Rango de precio fijo ──────────────────────────────
    const precioNum = parseInt(precio) || 0;

    const obtenerRangoPrecio = (p) => {
      if (p <= 0)    return "";
      if (p <= 10)   return "0 - 10";
      if (p <= 20)   return "10 - 20";
      if (p <= 30)   return "20 - 30";
      if (p <= 50)   return "30 - 50";
      if (p <= 80)   return "50 - 80";
      if (p <= 120)  return "80 - 120";
      if (p <= 200)  return "120 - 200";
      if (p <= 350)  return "200 - 350";
      if (p <= 500)  return "350 - 500";
      if (p <= 1000) return "500 - 1000";
      if (p <= 2500) return "1000 - 2500";
      if (p <= 5000) return "2500 - 5000";
      return "Mayor a 5000";
    };

    const rangoCalculado = obtenerRangoPrecio(precioNum);

    const localidadLower = localidad.toLowerCase();

    // ── PASO 4: Construir documento promoción ─────────────
    const promocionData = {
      comodidades: comodidadesArray,
      datos_hora_fecha: {
        activo: true,
        fecha_fin: fecha_fin || "",
        fecha_inicio: fecha_inicio || "",
        hora_fin: hora_fin || "",
        hora_inicio: hora_inicio || "",
        timestamp_fin: tsFin,
        timestamp_inicio: tsInicio,
      },
      estado,
      exclusivo,
      horario_publicacion: horario_seleccion,
      img_container: imgContainer,
      informacion: {
        categoria: categoria || "",
        compartir,
        contactar,
        descripcion: descripcion || "",
        id_promocion,
        id_tienda,
        nombre_tienda: nombre_tienda || "",
        numero: numero || "",
        titulo: titulo || "",
      },
      mensaje_predeterminado: {
        compartir: {
          activo_o_no: activo_mensaje_compartir,
          msje_predermindo: mensaje_compartir || "Mira esta promo en Geinz ❤️‍🔥",
        },
        whatsapp: {
          activo_o_no: activo_mensaje_whatsapp,
          msje_predermindo:
            mensaje_whatsapp || "Hola, quiero esta oferta que vi Geinz:",
        },
      },
      pagos: pagosArray,
      precio_publicacion: precio || "",
      rango_establecido: rangoCalculado,
      random: Math.random(),
      terminos_clave: terminosClave,
      tipo_hora_dias: formato_fecha_hora,
      ubicacion: {
        direccion: direccion || "",
        lat: lat || 0.0,
        long: long || 0.0,
        referencia: referencia || "",
      },
    };

    // ── PASO 5: Referencias Firestore ─────────────────────
    const ref1 = db
      .collection("Tiendas")
      .doc(localidadLower)
      .collection("promos_ofertas")
      .doc(id_promocion);

    const ref2 = db
      .collection("Tiendas")
      .doc(localidadLower)
      .collection(localidadLower)
      .doc(id_tienda)
      .collection("promociones_geinz")
      .doc(id_promocion);

    const ref3 = db
      .collection("promociones_filtrado_algolia")
      .doc(id_promocion);

    const algoliaData = {
      activo: true,
      categoria: categoria || "",
      comodidades: comodidadesArray,
      descripcion: descripcion || "",
      horario_publicacion: horario_seleccion,
      id_promocion,
      id_tienda,
      imagen_promo: img_bot || "",
      localidad: localidadLower,
      nombre_tienda: nombre_tienda || "",
      objectID: id_promocion,
      pagos: pagosArray,
      precio: precioNum,
      rango_precio: rangoCalculado,
      terminos_clave: terminosClave,
      timestamp_fin: tsFin.seconds * 1000,
      timestamp_inicio: tsInicio.seconds * 1000,
    };

    // ── PASO 6: Escribir promoción en las 3 referencias ───
    await Promise.all([
      ref1.set(promocionData, { merge: true }),
      ref2.set(promocionData, { merge: true }),
      ref3.set(algoliaData,   { merge: true }),
    ]);

    console.log(`✅ Promoción guardada en 3 rutas:
  - Tiendas/${localidadLower}/promos_ofertas/${id_promocion}
  - Tiendas/${localidadLower}/${localidadLower}/${id_tienda}/promociones_geinz/${id_promocion}
  - promociones_filtrado_algolia/${id_promocion}`);

    // ── PASO 7: Descuento de puntos y historial financiero ─
    if (saldo_descuento && precio_por_moneda) {
      const monto_descontado = saldo_descuento;
      const monto_restante   = (saldo_actual || 0) - monto_descontado;
      const precio_soles     = (monto_descontado * parseFloat(precio_por_moneda)).toFixed(2);
      const id_transaccion   = uuidv4();

      // Fecha Lima UTC-5
      const ahora  = new Date();
      const offset = -5 * 60;
      const lima   = new Date(ahora.getTime() + (offset - ahora.getTimezoneOffset()) * 60000);
      const fecha  = `${String(lima.getMonth() + 1).padStart(2, "0")}/${String(lima.getDate()).padStart(2, "0")}/${lima.getFullYear()}`;
      const hora   = `${String(lima.getHours()).padStart(2, "0")}:${String(lima.getMinutes()).padStart(2, "0")}`;

      const tiendaRef = db
        .collection("Tiendas")
        .doc(localidadLower)
        .collection(localidadLower)
        .doc(id_tienda);

      // Descontar puntos_tienda
      await tiendaRef.update({
        puntos_tienda: admin.firestore.FieldValue.increment(-monto_descontado),
      });
      console.log("✅ puntos_tienda descontado:", -monto_descontado);

      // Guardar historial financiero
      await tiendaRef
        .collection("historial_financiero")
        .doc(id_transaccion)
        .set({
          datos_recarga: {
            estado:           "Aceptado",
            monto_descontado: monto_descontado,
            monto_restante:   monto_restante,
            precio_soles:     precio_soles,
            tipo_paquete:     tipo_paquete || "Crear Promoción",
          },
          datos_tienda: {
            id_tienda:        id_tienda,
            localidad_tienda: localidad,
            nombre_tienda:    nombre_tienda || "",
          },
          hora_fecha: { fecha, hora },
          id_transaccion,
          timestamp:        admin.firestore.FieldValue.serverTimestamp(),
          tipo_transacción: "descuento",
        });

      console.log("✅ Historial financiero guardado:", id_transaccion);
      console.log("💰 Datos financieros:", { monto_descontado, monto_restante, precio_soles });
    } else {
      console.log("ℹ️ Sin datos financieros — historial no guardado");
    }

    // ── Retorno ───────────────────────────────────────────
    return {
      success:     true,
      id_promocion,
      id_tienda,
      localidad,
      mensaje:     "Promoción guardada exitosamente",
    };
  } catch (error) {
    console.error("Error crearPromocion:", error);
    throw new HttpsError("internal", error.message);
  }
});

const { v4: uuidv4 } = require("uuid");
exports.pagar_plan__usuario = onCall(async (request) => {
  console.log("🚀 [pagar_plan] Iniciando función...");

  try {
    const { precio_por_moneda, id_tienda, localidad,
            dias_extra, monedas_costo } = request.data;

    // Validaciones básicas
    if (!id_tienda || !localidad)
      throw new HttpsError("invalid-argument", "Faltan parámetros");
    if (typeof dias_extra !== "number" || dias_extra <= 0)
      throw new HttpsError("invalid-argument", "dias_extra inválido");
    if (typeof monedas_costo !== "number" || monedas_costo <= 0)
      throw new HttpsError("invalid-argument", "monedas_costo inválido");

    const refServicio = db.collection("Tiendas")
      .doc(localidad)
      .collection("tiendas_servicios_geinz_activos")
      .doc(id_tienda);

    const refTienda = db.collection("Tiendas")
      .doc(localidad)
      .collection(localidad)
      .doc(id_tienda);


    // ✅ CAMBIO 1: leer ambos en paralelo, sin lanzar error si servicios no existe


    const [servicioDoc, tiendaDoc] = await Promise.all([


      refServicio.get(),


      refTienda.get(),


    ]);



    // Solo la tienda principal es obligatoria


    if (!tiendaDoc.exists)


      throw new HttpsError("not-found", "No se encontró la tienda");



    // Si servicios no existe, usar objeto vacío — se creará en el batch


    const dataServicio = servicioDoc.exists ? servicioDoc.data() : {};

    const dataTienda = tiendaDoc.data();

    const puntosActuales = Number(dataTienda?.puntos_tienda || 0);

    if (puntosActuales < monedas_costo)
      throw new HttpsError("failed-precondition",
        "La tienda no tiene suficientes monedas");

    const monto_restante = puntosActuales - monedas_costo;

    // Calcular fecha base
    let fechaBase = new Date();
    if (dataServicio?.panel_admin?.timestamp_fin?.toDate) {
      const fechaActualPlan = dataServicio.panel_admin.timestamp_fin.toDate();
      if (fechaActualPlan > fechaBase) fechaBase = fechaActualPlan;
    }

    const nuevaFecha = new Date(fechaBase);
    nuevaFecha.setDate(nuevaFecha.getDate() + dias_extra);

    const dd   = String(nuevaFecha.getDate()).padStart(2, "0");
    const mm   = String(nuevaFecha.getMonth() + 1).padStart(2, "0");
    const yyyy = nuevaFecha.getFullYear();
    const fecha_fin = `${dd}/${mm}/${yyyy}`;

    // Historial
    const ahora     = new Date();
    const fechaStr  = ahora.toLocaleDateString("es-PE",
      { day:"2-digit", month:"2-digit", year:"numeric", timeZone:"America/Lima" });
    const horaStr   = ahora.toLocaleTimeString("es-PE",
      { hour:"2-digit", minute:"2-digit", hour12:false, timeZone:"America/Lima" });
    const id_transaccion = uuidv4();
    const nombreTienda   = dataTienda?.nombre_tienda || "";

    const historialData = {
      datos_recarga: {
        estado: "Aceptado",
        monto_descontado: monedas_costo,
        monto_restante,
        precio_soles: (Number(monedas_costo) * Number(precio_por_moneda)).toFixed(2),
        tipo_paquete: `Panel activo por ${dias_extra} días`,
      },
      datos_tienda: { id_tienda, localidad_tienda: localidad, nombre_tienda: nombreTienda },
      hora_fecha:   { fecha: fechaStr, hora: horaStr },
      id_transaccion,
      timestamp: admin.firestore.Timestamp.now(),
      tipo_transacción: "descuento",
    };

    const batch = db.batch();


    // ✅ CAMBIO 2: set con merge en vez de update — crea el doc si no existe


    batch.set(refServicio, {


      panel_admin: {


        fecha_fin,


        timestamp_fin: admin.firestore.Timestamp.fromDate(nuevaFecha),


      },


    }, { merge: true });  // ← merge conserva otros campos si ya existía


    batch.set(
      refTienda.collection("historial_financiero").doc(id_transaccion),
      historialData,
    );

    batch.update(refTienda, {
      puntos_tienda: admin.firestore.FieldValue.increment(-monedas_costo),
    });

    await batch.commit();

    return {
      success: true,
      message: "Plan actualizado correctamente",
      fecha_fin,
      timestamp_fin: nuevaFecha.getTime(),
      id_transaccion,
      puntos_restantes: monto_restante,
    };

  } catch (error) {
    console.error("❌ ERROR pagar_plan__usuario:", error);
    if (error instanceof HttpsError) throw error;
    throw new HttpsError("internal", error.message || "Error interno");
  }
});
