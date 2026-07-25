const { onRequest } = require("firebase-functions/v2/https");
const OpenAI = require("openai");
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
    _openai = new OpenAI({
      apiKey: process.env.OPENAI_API_KEY || process.env.API_KEYO_OPEN_IA,
    });
  }
  return _openai;
}

/* =========================================================================
   DISPENSADOR — clasificador de intención (rama única)
   Modelo: gpt-5.5-mini
   Reglas: siempre devuelve EXACTAMENTE una de las 6 ramas, nunca otra cosa.
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
- busqueda_algolia: busca un producto específico o pregunta si lo venden (ej. "¿tienen chifa?", "quiero un plato de...").
- pedidos_carrito: pregunta por su pedido/carrito actual: qué agregó, cuánto va, resumen antes de pagar.
- pagos_voucher: ya quiere pagar, va a enviar o envió un voucher, o pregunta cómo pagar.
- reclamos: tiene una queja, reclamo, o pide hablar con un asesor humano real.

Responde SOLO con el nombre exacto de una rama de la lista. Nunca expliques, nunca agregues texto, nunca inventes una rama nueva. Si el mensaje es ambiguo o no calza claramente en ninguna, usa "general".`;

/**
 * Clasifica el texto del usuario en una única rama del flujo.
 * Usa Structured Outputs (json_schema con enum) para que el modelo
 * SOLO pueda devolver uno de los 6 valores permitidos — sin errores de parseo.
 *
 * @param {string} textoUsuario - Mensaje que escribió el usuario.
 * @returns {Promise<string>} Una de: general | carta_visual | busqueda_algolia | pedidos_carrito | pagos_voucher | reclamos
 */
async function clasificarRama(textoUsuario) {
  try {
    const openai = obtenerClienteOpenAI();
    const completion = await openai.chat.completions.create({
      model: "gpt-5.5-mini",
      messages: [
        { role: "system", content: SYSTEM_PROMPT },
        { role: "user", content: textoUsuario },
      ],
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

    const contenido = completion.choices[0].message.content;
    const parsed = JSON.parse(contenido);

    if (!RAMAS.includes(parsed.rama)) {
      console.warn(
        "[dispensador] Rama fuera de catálogo, usando fallback:",
        parsed.rama,
      );
      return "general";
    }

    return parsed.rama;
  } catch (err) {
    console.error("[dispensador] Error clasificando rama:", err);
    return "general"; // fallback seguro: nunca dejamos al usuario sin respuesta
  }
}

/* =========================================================================
   ENDPOINT HTTP — recibe el mensaje, guarda historial y devuelve la rama
========================================================================= */
exports.dispensador = onRequest(async (req, res) => {
  try {
    const { numero_usuario, nombre_usuario, mensaje } = req.body || {};

    if (!numero_usuario || !mensaje) {
      return res
        .status(400)
        .json({ error: "Faltan numero_usuario o mensaje." });
    }

    const rama = await clasificarRama(mensaje);

    // Guarda el mensaje entrante en el historial (no bloquea la respuesta si falla)
    //  guardarMensajeHistorial({
    //    numero_usuario,
    //    nombre_usuario,
    //    contenido: mensaje,
    //    remitente: "usuario",
    //    tipo: "texto",
    //   }).catch((err) => console.error("[dispensador] Error guardando historial:", err));

    return res.status(200).json({ rama });
  } catch (err) {
    console.error("[dispensador] Error en el endpoint:", err);
    return res.status(500).json({ error: "Error interno del dispensador." });
  }
});

module.exports.clasificarRama = clasificarRama;
module.exports.RAMAS = RAMAS;