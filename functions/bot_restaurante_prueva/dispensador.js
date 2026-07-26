const { onRequest } = require("firebase-functions/v2/https");
const OpenAI = require("openai");
const { tokenOpenAI, armarTokens } = require("./token_utils.js");
//const { guardarMensajeHistorial } = require("../historial_whatsapp.js");

/* =========================================================================
   Cliente OpenAI — inicialización PEREZOSA.
   Antes se hacía `new OpenAI(...)` al cargar el archivo, lo cual revienta
   el `firebase deploy` completo (analiza TODO el código, aunque despliegues
   una sola función) si la env var no está disponible en ese momento.
   Ahora el cliente solo se crea la primera vez que se necesita de verdad.

   También se acepta OPENAI_API_KEY o API_KEYO_OPEN_IA — el proyecto usa
   ambos nombres en distintos archivos (busqueda_algolia.js usa
   API_KEYO_OPEN_IA), así que con esto funciona sin importar cuál tengas
   puesta en tu .env.
========================================================================= */
let _openai = null;
function obtenerClienteOpenAI() {
  if (!_openai) {
    console.log("[dispensador] Inicializando cliente OpenAI (primera vez)...");
    _openai = new OpenAI({
      apiKey: process.env.OPENAI_API_KEY || process.env.API_KEYO_OPEN_IA,
    });
  }
  return _openai;
}

/* =========================================================================
   DISPENSADOR — clasificador de intención (rama única)
   Modelo: gpt-5.4-mini
   Reglas: siempre devuelve EXACTAMENTE una de las 6 ramas, nunca otra cosa.

   👇 TOKENS: clasificarRama() ahora devuelve también cuánto gastó ESTA
   llamada a OpenAI, en el mismo formato {detalle, total} que usan todas
   las ramas (ver token_utils.js). Antes esta función devolvía solo el
   string de la rama y los tokens de esta llamada se perdían — nunca se
   sumaban al total de la consulta.

   Quien llama a clasificarRama() (el webhook) debe sumar este tokens con
   el tokens que devuelva la rama despachada, para tener el gasto TOTAL de
   la consulta completa (clasificación + redacción de la respuesta).
========================================================================= */

const RAMAS = [
  "general",
  "negocio",
  "carta_visual",
  "busqueda_algolia",
  "pedidos_carrito",
  "pagos_voucher",
  "reclamos",
];

const SYSTEM_PROMPT = `Eres el clasificador de intención de un bot de ventas por WhatsApp.
Lee el ÚLTIMO mensaje del usuario y decide a cuál de estas 6 ramas pertenece:

- general: preguntas sin contexto 
- negocio:pide información del negocio (horarios, ubicación, quiénes son, promociones, dudas generales).
- carta_visual: quiere VER la carta, el menú o fotos de los productos.
- busqueda_algolia: busca un producto específico o pregunta si lo venden o precio de producto.
- pedidos_carrito: pregunta por su pedido/carrito actual: qué agregó, cuánto va, resumen antes de pagar.
- pagos_voucher: ya quiere pagar, va a enviar o envió un voucher, o pregunta cómo pagar.
- reclamos: tiene una queja, reclamo, o pide hablar con un asesor humano real.

Vas a recibir también, en un mensaje aparte, el CONTEXTO de la conversación anterior (un resumen corto de qué se venía hablando, incluyendo la rama en la que se quedó). Úsalo así:
- Si el último mensaje del usuario es corto, ambiguo, una confirmación ("sí", "ok", "dale", "eso"), una aclaración, o claramente sigue el mismo tema del contexto anterior, MANTÉN la misma rama que indica el contexto en vez de mandarlo a "general".
- Solo cambia de rama si el mensaje muestra una intención NUEVA y distinta a la del contexto (ej. venía en "negocio" preguntando horarios y ahora pide ver la carta → pasa a "carta_visual").
- Si no hay contexto (primera interacción o contexto vacío/null), clasifica solo con el mensaje, como siempre.

Responde SOLO con el nombre exacto de una rama de la lista. Nunca expliques, nunca agregues texto, nunca inventes una rama nueva. Si el mensaje es ambiguo y NO hay contexto que lo aclare, usa "general".`;

/**
 * Clasifica el texto del usuario en una única rama del flujo.
 * Usa Structured Outputs (json_schema con enum) para que el modelo
 * SOLO pueda devolver uno de los 6 valores permitidos — sin errores de parseo.
 *
 * @param {string} textoUsuario - Mensaje que escribió el usuario.
 * @param {object} [contextoAnterior] - Contexto de la vuelta anterior guardado en Firestore.
 * @param {string} [contextoAnterior.tipo] - Rama en la que se quedó la conversación (ej. "negocio").
 * @param {string} [contextoAnterior.extra] - Resumen corto de qué se venía hablando.
 * @returns {Promise<{rama: string, tokens: {detalle: Array, total: number}}>}
 *   rama: una de general | carta_visual | busqueda_algolia | pedidos_carrito | pagos_voucher | reclamos
 *   tokens: lo gastado en OpenAI SOLO en esta llamada de clasificación.
 */
async function clasificarRama(textoUsuario, contextoAnterior) {
  console.log("[dispensador] → clasificarRama | mensaje del usuario:", textoUsuario, "| contextoAnterior:", JSON.stringify(contextoAnterior));
  try {
    const openai = obtenerClienteOpenAI();

    // Armamos los mensajes a enviar al modelo. Si hay contexto útil (no "null",
    // no vacío), lo mandamos como un mensaje adicional para que el modelo lo
    // tenga en cuenta al decidir si mantener la rama o cambiarla.
    const mensajes = [{ role: "system", content: SYSTEM_PROMPT }];

    const tipoAnterior = contextoAnterior?.tipo;
    const extraAnterior = contextoAnterior?.extra;
    const hayContextoUtil =
      extraAnterior && extraAnterior !== "null" && extraAnterior.trim() !== "";

    if (hayContextoUtil) {
      const textoContexto = `CONTEXTO de la conversación anterior:\n- Rama anterior: ${tipoAnterior || "desconocida"}\n- Resumen: ${extraAnterior}`;
      console.log("[dispensador] Contexto útil encontrado, se envía al modelo:", textoContexto);
      mensajes.push({ role: "system", content: textoContexto });
    } else {
      console.log("[dispensador] Sin contexto útil (primera interacción o extra vacío/null), clasificando solo con el mensaje");
    }

    mensajes.push({ role: "user", content: textoUsuario });

    console.log("[dispensador] Llamando a OpenAI (gpt-5.4-mini) para clasificar...");
    const completion = await openai.chat.completions.create({
      model: "gpt-5.4-mini",
      messages: mensajes,
      response_format: {
        type: "json_schema",
        json_schema: {
          name: "clasificacion_rama",
          strict: true,
          schema: {
            type: "object",
            properties: {
              rama: { type: "string", enum: RAMAS },
            },
            required: ["rama"],
            additionalProperties: false,
          },
        },
      },
    });

    // 👇 TOKENS: antes esto no se loggeaba ni se devolvía en ningún lado.
    const tokens = armarTokens([tokenOpenAI(completion.usage, "gpt-5.4-mini")]);
    console.log(
      "[dispensador] Tokens usados en clasificarRama (gpt-5.4-mini):",
      "prompt_tokens:", completion.usage?.prompt_tokens,
      "| completion_tokens:", completion.usage?.completion_tokens,
      "| total_tokens:", completion.usage?.total_tokens,
    );

    const contenido = completion.choices[0].message.content;
    console.log("[dispensador] Respuesta cruda del modelo:", contenido);

    const parsed = JSON.parse(contenido);
    console.log("[dispensador] Rama devuelta por el modelo:", parsed.rama);

    if (!RAMAS.includes(parsed.rama)) {
      console.warn(
        "[dispensador] ⚠️ Rama fuera de catálogo, usando fallback 'general':",
        parsed.rama,
      );
      return { rama: "general", tokens };
    }

    console.log("[dispensador] ✅ RAMA FINAL:", parsed.rama, "| mensaje:", textoUsuario);
    return { rama: parsed.rama, tokens };
  } catch (err) {
    console.error("[dispensador] ❌ Error clasificando rama:", err.message, "| mensaje:", textoUsuario, "| contextoAnterior:", JSON.stringify(contextoAnterior));
    console.log("[dispensador] ✅ RAMA FINAL (fallback por error): general");
    // fallback seguro: nunca dejamos al usuario sin respuesta. Como falló
    // antes o durante la llamada, no hay forma confiable de saber cuánto se
    // gastó, así que se reporta en 0 (no null, para que sumarlo con otros
    // tokens no rompa nada).
    return { rama: "general", tokens: armarTokens([]) };
  }
}

/* =========================================================================
   ENDPOINT HTTP — recibe el mensaje, guarda historial y devuelve la rama
========================================================================= */
exports.dispensador = onRequest(async (req, res) => {
  console.log("[dispensador] === Nueva petición HTTP a /dispensador ===");
  try {
    // contexto (opcional): { tipo, extra } de la vuelta anterior, si el
    // caller (ej. WhatsApp) lo tiene guardado y quiere pasarlo.
    const { numero_usuario, nombre_usuario, mensaje, contexto } = req.body || {};
    console.log("[dispensador] Body recibido:", JSON.stringify(req.body));

    if (!numero_usuario || !mensaje) {
      console.warn("[dispensador] Faltan numero_usuario o mensaje, respondiendo 400");
      return res
        .status(400)
        .json({ error: "Faltan numero_usuario o mensaje." });
    }

    console.log("[dispensador] numero_usuario:", numero_usuario, "| nombre_usuario:", nombre_usuario, "| mensaje:", mensaje, "| contexto:", JSON.stringify(contexto));

    const { rama, tokens } = await clasificarRama(mensaje, contexto);
    console.log("[dispensador] Endpoint /dispensador devolverá rama:", rama, "| para numero_usuario:", numero_usuario, "| tokens clasificación:", tokens.total);

    // Guarda el mensaje entrante en el historial (no bloquea la respuesta si falla)
    //  guardarMensajeHistorial({
    //    numero_usuario,
    //    nombre_usuario,
    //    contenido: mensaje,
    //    remitente: "usuario",
    //    tipo: "texto",
    //   }).catch((err) => console.error("[dispensador] Error guardando historial:", err));

    return res.status(200).json({ rama, tokens });
  } catch (err) {
    console.error("[dispensador] ❌ Error en el endpoint:", err.message, "| stack:", err.stack);
    return res.status(500).json({ error: "Error interno del dispensador." });
  }
});

module.exports.clasificarRama = clasificarRama;
module.exports.RAMAS = RAMAS;