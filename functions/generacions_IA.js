const { onCall, HttpsError } = require("firebase-functions/v2/https");
const axios = require("axios");
const admin = require("firebase-admin"); // ✅ solo require, NO initializeApp()

const db = admin.firestore();
const GEMINIKEY = process.env.PRIVATEKEY_GEMINI;
const GEMINI_URL =
  "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent";
const TIMEOUT = 30000;
const { v4: uuidv4 } = require("uuid");
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
  const { imageBase64, mimeType, tipo } = request.data;

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

    let json;
    try {
      json = parsearJSON(texto);
    } catch {
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

    return {
      ok: true,
      titulo: json.titulo || "",
      descripcion: json.descripcion || "",
    };
  } catch (error) {
    if (error instanceof HttpsError) throw error;
    console.error("ERROR generar_titulo_descripcion_IA:", error);
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
  const { tipo, tituloUsuario, descripcionUsuario } = request.data;

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

    return { ok: true, respuesta };
  } catch (error) {
    if (error instanceof HttpsError) throw error;
    console.error("ERROR generar_texto_ia:", error);
    throw new HttpsError(
      "internal",
      error.message || "Error generando texto IA",
    );
  }
});

// ─── 3. Generar mensaje para compartir ──────────────────────────────────────

exports.generar_texto_compartir_ia = onCall(async (request) => {
  const { tituloUsuario, descripcionUsuario } = request.data;

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

    return { ok: true, mensaje };
  } catch (error) {
    if (error instanceof HttpsError) throw error;
    console.error("ERROR generar_texto_compartir_ia:", error);
    throw new HttpsError(
      "internal",
      error.message || "Error generando mensaje para compartir",
    );
  }
});

// ─── 4. Generar mensaje de contacto por WhatsApp ─────────────────────────────

exports.generar_whatsapp_contacto_ia = onCall(async (request) => {
  const { titulo, descripcion } = request.data;

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

    return { ok: true, mensaje };
  } catch (error) {
    if (error instanceof HttpsError) throw error;
    console.error("ERROR generar_whatsapp_contacto_ia:", error);
    throw new HttpsError(
      "internal",
      error.message || "Error generando mensaje de WhatsApp",
    );
  }
});

// ─── 5. Generar terminos_calves_filtrado ─────────────────────────────

exports.extraerTerminosClaveIA = onCall(async (request) => {
  const { textoUsuario, categoria } = request.data;

  if (!textoUsuario || !textoUsuario.trim()) {
    throw new HttpsError(
      "invalid-argument",
      "El campo textoUsuario es requerido",
    );
  }

  if (!categoria || !categoria.trim()) {
    throw new HttpsError("invalid-argument", "El campo categoria es requerido");
  }

  const prompt = `
Extrae términos clave de búsqueda para una promoción. Categoría: ${categoria}

Extrae SOLO lo que un usuario escribiría en un buscador para encontrar esto:
- Lugares, destinos o direcciones si los hay
- Productos o servicios específicos (no genéricos)
- Especialidades o rubros del negocio

Descarta: precios, descuentos, cantidades, adjetivos, palabras genéricas 
(como "oferta", "promo", "compra", "viaje", "boleto", "servicio", "producto")

Reglas: minúsculas, sin tildes, singular, sin duplicados, máximo 6 términos.

Responde SOLO el array JSON. Texto: "${textoUsuario}"
    `.trim();

  try {
    const respuesta = await llamarGemini([{ text: prompt }]);

    // Limpiar la respuesta para obtener solo el JSON
    let jsonStr = respuesta.trim();
    if (jsonStr.startsWith("```json")) {
      jsonStr = jsonStr.replace(/```json\n?/g, "").replace(/```\n?/g, "");
    }
    if (jsonStr.startsWith("```")) {
      jsonStr = jsonStr.replace(/```\n?/g, "");
    }

    const terminos = JSON.parse(jsonStr);

    return {
      ok: true,
      terminos: Array.isArray(terminos) ? terminos : [],
    };
  } catch (error) {
    if (error instanceof HttpsError) throw error;
    console.error("ERROR extraerTerminosClaveIA:", error);
    throw new HttpsError(
      "internal",
      error.message || "Error extrayendo términos clave",
    );
  }
});

// ─── 6. Generar descripcionSEOIA ─────────────────────────────

exports.generar_descripcion_whatsapp_ia = onCall(async (request) => {
  const { texto } = request.data;

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

    return {
      ok: true,
      descripcion,
    };
  } catch (error) {
    if (error instanceof HttpsError) throw error;

    console.error("ERROR generar_descripcion_whatsapp_ia:", error);

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
      urls_imagenes = [], // ✅ nuevo — URLs ya subidas desde el front
      img_bot: img_bot_param = "", // ✅ nuevo — bot URL desde el front
      logo_url = "",
    } = data;

    // ── Validaciones ──────────────────────────────────────
    if (!id_tienda || !id_promocion || !localidad) {
      throw new HttpsError("invalid-argument", "Faltan campos requeridos");
    }

    // ✅ Acepta imágenes desde front (urls_imagenes) O base64
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
      // ✅ El front ya subió las imágenes — usar URLs directamente
      urls = urls_imagenes;
      img_bot = img_bot_param || urls[0] || null;
      console.log("✅ Usando URLs ya subidas desde el front:", urls);
    } else {
      // 🔄 Fallback: subir desde base64 (flujo antiguo)
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

    // ── PASO 2: img_container en Firestore ────────────────
    const imgContainer = {
      lista_img: urls,
      logo_img: logo_url,
    };

    const ref1 = db
      .collection("Tiendas")
      .doc(localidad.toLowerCase())
      .collection("promos_ofertas")
      .doc(id_promocion);

    await ref1.set({ img_container: imgContainer }, { merge: true });

    // ── PASO 3: Crear promoción completa ──────────────────
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

    const precioNum = parseInt(precio) || 0;
    const precioMin = Math.floor(precioNum * 0.8);
    const precioMax = Math.floor(precioNum * 1.2);
    const rangoCalculado = precioNum > 0 ? `${precioMin}-${precioMax}` : "";

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

    const algoliaData = {
      activo: true,
      categoria: categoria || "",
      comodidades: comodidadesArray,
      descripcion: descripcion || "",
      horario_publicacion: horario_seleccion,
      id_promocion,
      id_tienda,
      imagen_promo: img_bot || "", // ✅ usa la bot URL correcta
      localidad: localidad.toLowerCase(),
      nombre_tienda: nombre_tienda || "",
      objectID: id_promocion,
      pagos: pagosArray,
      precio: precioNum,
      precioMax,
      precioMin,
      terminos_clave: terminosClave,
      timestamp_fin: tsFin.seconds * 1000,
      timestamp_inicio: tsInicio.seconds * 1000,
    };

    const ref3 = db
      .collection("promociones_filtrado_algolia")
      .doc(id_promocion);

    await Promise.all([
      ref1.set(promocionData, { merge: true }),
      ref3.set(algoliaData, { merge: true }),
    ]);

    return {
      success: true,
      id_promocion,
      id_tienda,
      localidad,
      mensaje: "Promoción guardada exitosamente",
    };
  } catch (error) {
    console.error("Error crearPromocion:", error);
    throw new HttpsError("internal", error.message);
  }
});

exports.pagar_plan__usuario = onCall(async (request) => {
  console.log("🚀 [pagar_plan] Iniciando función...");
  console.log("📥 [pagar_plan] Datos recibidos:", JSON.stringify(request.data));

  try {
    const { id_tienda, localidad, dias_extra, monedas_costo } = request.data;

    // ─────────────────────────────
    // VALIDACIONES
    // ─────────────────────────────
    console.log(
      "🔍 [validaciones] id_tienda:",
      id_tienda,
      "| localidad:",
      localidad,
      "| dias_extra:",
      dias_extra,
      "| monedas_costo:",
      monedas_costo,
    );

    if (!id_tienda || !localidad) {
      console.error("❌ [validaciones] Faltan id_tienda o localidad");
      throw new HttpsError(
        "invalid-argument",
        "Faltan parámetros: id_tienda o localidad",
      );
    }
    if (!dias_extra || typeof dias_extra !== "number") {
      console.error("❌ [validaciones] dias_extra inválido:", dias_extra);
      throw new HttpsError("invalid-argument", "dias_extra debe ser un número");
    }
    if (!monedas_costo || typeof monedas_costo !== "number") {
      console.error("❌ [validaciones] monedas_costo inválido:", monedas_costo);
      throw new HttpsError(
        "invalid-argument",
        "monedas_costo debe ser un número",
      );
    }
    console.log("✅ [validaciones] OK");

    // ─────────────────────────────
    // REFERENCIAS FIRESTORE
    // ─────────────────────────────
    const refServicio = db
      .collection("Tiendas")
      .doc(localidad)
      .collection("tiendas_servicios_geinz_activos")
      .doc(id_tienda);

    const refTienda = db
      .collection("Tiendas")
      .doc(localidad)
      .collection(localidad)
      .doc(id_tienda);

    console.log("📂 [firestore] refServicio:", refServicio.path);
    console.log("📂 [firestore] refTienda:", refTienda.path);

    const doc = await refServicio.get();
    console.log("📄 [firestore] doc existe:", doc.exists);

    if (!doc.exists) {
      console.error(
        "❌ [firestore] Tienda no encontrada en:",
        refServicio.path,
      );
      throw new HttpsError("not-found", "Tienda no encontrada");
    }

    const data = doc.data();
    console.log(
      "📄 [firestore] panel_admin actual:",
      JSON.stringify(data?.panel_admin),
    );

    // ─────────────────────────────
    // FECHA ACTUAL DEL PLAN
    // ─────────────────────────────
    let fechaActual = new Date();
    console.log("📅 [fecha] fecha base (hoy):", fechaActual.toISOString());

    if (data?.panel_admin?.timestamp_fin?.toDate) {
      const actual = data.panel_admin.timestamp_fin.toDate();
      console.log("📅 [fecha] timestamp_fin actual:", actual.toISOString());
      if (actual > fechaActual) {
        fechaActual = actual;
        console.log(
          "📅 [fecha] Plan aún vigente → extendiendo desde:",
          actual.toISOString(),
        );
      } else {
        console.log("📅 [fecha] Plan vencido → extendiendo desde hoy");
      }
    } else {
      console.warn("⚠️ [fecha] No hay timestamp_fin → usando fecha de hoy");
    }

    // ─────────────────────────────
    // CALCULAR NUEVA FECHA
    // ─────────────────────────────
    const nuevaFecha = new Date(fechaActual);
    nuevaFecha.setDate(nuevaFecha.getDate() + dias_extra);

    const dd = String(nuevaFecha.getDate()).padStart(2, "0");
    const mm = String(nuevaFecha.getMonth() + 1).padStart(2, "0");
    const yyyy = nuevaFecha.getFullYear();
    const fecha_fin = `${dd}/${mm}/${yyyy}`;

    console.log(
      "📅 [fecha] dias_extra:",
      dias_extra,
      "| nueva fecha_fin:",
      fecha_fin,
      "| ISO:",
      nuevaFecha.toISOString(),
    );

    // ─────────────────────────────
    // DATOS HORA / FECHA LIMA
    // ─────────────────────────────
    const ahora = new Date();
    const horaStr = ahora.toLocaleTimeString("es-PE", {
      hour: "2-digit",
      minute: "2-digit",
      hour12: false,
      timeZone: "America/Lima",
    });
    const fechaStr = ahora.toLocaleDateString("es-PE", {
      day: "2-digit",
      month: "2-digit",
      year: "numeric",
      timeZone: "America/Lima",
    });

    console.log(
      "🕐 [historial] hora Lima:",
      horaStr,
      "| fecha Lima:",
      fechaStr,
    );

    const id_transaccion = uuidv4();
    console.log("🔑 [historial] id_transaccion generado:", id_transaccion);

    // ─────────────────────────────
    // LEER PUNTOS Y NOMBRE (PRIMERO)
    // ─────────────────────────────
    console.log("💰 [puntos] Leyendo puntos actuales de:", refTienda.path);
    const tiendaDoc = await refTienda.get();
    console.log("💰 [puntos] tiendaDoc existe:", tiendaDoc.exists);

    const tiendaData = tiendaDoc.exists ? tiendaDoc.data() : {};
    const puntosActuales = tiendaData?.puntos_tienda ?? 0;
    const nombreTienda = tiendaData?.nombre_tienda ?? "";
    const monto_restante = puntosActuales - monedas_costo;

    console.log(
      "💰 [puntos] actuales:",
      puntosActuales,
      "| a descontar:",
      monedas_costo,
      "| restante:",
      monto_restante,
    );

    // ─────────────────────────────
    // ARMAR DOCUMENTO HISTORIAL
    // ─────────────────────────────
    const docHistorial = {
      id_transaccion,
      tipo_transacción: "descuento",
      hora_fecha: {
        fecha: fechaStr,
        hora: horaStr,
      },
      datos_tienda: {
        nombre_tienda: nombreTienda,
        id_tienda,
        localidad_tienda: localidad,
      },
      datos_recarga: {
        tipo_paquete: `Panel activo por ${dias_extra === 30 ? "1 mes" : dias_extra + " días"}`,
        monto_descontado: monedas_costo,
        precio_soles: (monedas_costo / 100).toFixed(2),
        estado: "Aceptado",
        monto_restante,
      },
      timestamp: admin.firestore.FieldValue.serverTimestamp(),
    };

    console.log("📦 [historial] docHistorial:", JSON.stringify(docHistorial));

    // ─────────────────────────────
    // 3 ESCRITURAS EN PARALELO
    // ─────────────────────────────
    console.log("✍️ [escrituras] Iniciando Promise.all con 3 operaciones...");

    const rutaHistorial = `${refTienda.path}/historial_financiero/${id_transaccion}`;
    console.log("✍️ [escrituras] 1 → panel_admin en:", refServicio.path);
    console.log("✍️ [escrituras] 2 → historial en:", rutaHistorial);
    console.log("✍️ [escrituras] 3 → puntos_tienda en:", refTienda.path);

    await Promise.all([
      // 1. Actualizar fecha del panel en tiendas_servicios_geinz_activos
      refServicio.update({
        "panel_admin.fecha_fin": fecha_fin,
        "panel_admin.timestamp_fin":
          admin.firestore.Timestamp.fromDate(nuevaFecha),
      }),

      // 2. Guardar historial en historial_financiero
      refTienda
        .collection("historial_financiero")
        .doc(id_transaccion)
        .set(docHistorial),

      // 3. Descontar puntos en tienda principal
      refTienda.update({
        puntos_tienda: admin.firestore.FieldValue.increment(-monedas_costo),
      }),
    ]);

    console.log("✅ [escrituras] Las 3 operaciones completadas exitosamente");

    // ─────────────────────────────
    // RESPUESTA
    // ─────────────────────────────
    const respuesta = {
      success: true,
      message: "Plan actualizado correctamente",
      fecha_fin,
      timestamp_fin: nuevaFecha.getTime(),
      id_transaccion,
      puntos_restantes: monto_restante,
    };

    console.log("📤 [respuesta] Enviando:", JSON.stringify(respuesta));
    return respuesta;
  } catch (error) {
    console.error("❌ [pagar_plan] ERROR CAPTURADO:");
    console.error("   mensaje:", error.message);
    console.error("   código:", error.code);
    console.error("   stack:", error.stack);

    if (error instanceof HttpsError) throw error;
    throw new HttpsError("internal", "Error interno al procesar pago");
  }
});
