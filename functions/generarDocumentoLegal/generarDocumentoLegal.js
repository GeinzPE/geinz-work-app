/**

 * generarDocumentoLegal.js

 * ---------------------------------------------------------

 * Cloud Function (Firebase Functions v2) que genera un documento

 * legal (términos y condiciones, política de privacidad, etc.)

 * para un negocio en Perú, usando Gemini.

 *

 * Uso en tu index.js:

 * const { generarDocumentoLegal } = require("./generarDocumentoLegal");

 * exports.generarDocumentoLegal = generarDocumentoLegal;

 *

 * Variable de entorno requerida (ya existente en el proyecto):

 * PRIVATEKEY_GEMINI

 *

 * Dependencia requerida:

 * npm install @google/generative-ai --save

 * (si el proyecto ya usa otro SDK de Gemini, reemplazar el bloque

 *  "LLAMADA A GEMINI" de más abajo por el SDK existente)

 * ---------------------------------------------------------

 */



const { onCall, HttpsError } = require("firebase-functions/v2/https");

const { GoogleGenerativeAI } = require("@google/generative-ai");



// ------------------------------------------------------------------

// CONFIGURACIÓN

// ------------------------------------------------------------------



const TIPOS_PERMITIDOS = [

 "terminos_condiciones",

 "politica_privacidad",

 "politica_cookies",

 "libro_reclamaciones",

 "procedimiento_reclamos",

];



const TITULOS = {

 terminos_condiciones: "Términos y Condiciones",

 politica_privacidad: "Política de Privacidad",

 politica_cookies: "Política de Cookies",

 libro_reclamaciones: "Libro de Reclamaciones",

 procedimiento_reclamos: "Procedimiento de Reclamos",

};



// Campos permitidos desde datos_negocio (whitelist explícita).

// Cualquier otro campo enviado por el cliente es descartado.

const CAMPOS_NEGOCIO_PERMITIDOS = [

 "nombre_comercial",

 "razon_social",

 "ruc",

 "telefono",

 "direccion",

 "pais",

 "categoria",

 "subcategoria",

 "descripcion",

 "descripcion_seo",

 "sitio_web",

 "redes_sociales",

 "metodos_contacto",

 "metodos_pago",

 "horario",

];



// Campos permitidos desde respuestas_negocio (whitelist explícita).

const CAMPOS_RESPUESTAS_PERMITIDOS = [

 "descripcion_operacion",

 "cambios_devoluciones",

 "entrega",

 "datos_personales",

 "uso_datos",

 "marketing",

 "terceros",

 "informacion_adicional",

];



// Límites de tamaño para evitar solicitudes excesivas / costos altos.

const MAX_TIPO_DOCUMENTO_LEN = 60;

const MAX_CAMPO_VALOR_LEN = 2000; // por campo individual

const MAX_TOTAL_PAYLOAD_LEN = 12000; // tamaño total aproximado enviado a Gemini



// ------------------------------------------------------------------

// UTILIDADES INTERNAS

// ------------------------------------------------------------------



/**

 * Filtra un objeto dejando solo las claves permitidas, con valores

 * de tipo string/number/boolean (o arrays/objetos simples), recortados

 * a un tamaño máximo. Nunca deja pasar tokens, ids internos, secretos, etc.

 * porque esas claves ni siquiera están en la whitelist.

 */

function filtrarObjeto(obj, camposPermitidos) {

 const resultado = {};

 if (!obj || typeof obj !== "object") return resultado;



 for (const campo of camposPermitidos) {

  const valor = obj[campo];

  if (valor === undefined || valor === null || valor === "") continue;



  let valorSeguro;

  if (typeof valor === "string") {

   valorSeguro = valor.slice(0, MAX_CAMPO_VALOR_LEN);

  } else if (typeof valor === "number" || typeof valor === "boolean") {

   valorSeguro = valor;

  } else if (Array.isArray(valor)) {

   valorSeguro = valor

    .map((v) => (typeof v === "string" ? v.slice(0, 300) : v))

    .slice(0, 20);

  } else if (typeof valor === "object") {

   // objetos simples (ej. redes_sociales: {instagram, facebook})

   const plano = {};

   for (const [k, v] of Object.entries(valor)) {

    if (typeof v === "string") plano[k] = v.slice(0, 300);

    else if (typeof v === "number" || typeof v === "boolean") plano[k] = v;

   }

   valorSeguro = plano;

  } else {

   continue;

  }



  resultado[campo] = valorSeguro;

 }



 return resultado;

}



/**

 * Limpia bloques de Markdown tipo ```json ... ``` que Gemini

 * suele devolver, y extrae el primer objeto JSON válido del texto.

 */

function limpiarYParsearJSON(textoBruto) {

 if (!textoBruto || typeof textoBruto !== "string") {

  throw new Error("Respuesta vacía del modelo");

 }



 let texto = textoBruto.trim();



 // Quitar cercas de código ```json ... ``` o ``` ... ```

 texto = texto.replace(/^```json\s*/i, "").replace(/^```\s*/, "");

 texto = texto.replace(/```$/i, "").trim();



 // Intento 1: parseo directo

 try {

  return JSON.parse(texto);

 } catch (_) {

  // continúa al fallback

 }



 // Extraer el primer bloque { ... } balanceado (por si hay texto extra alrededor)

 const inicio = texto.indexOf("{");

 const fin = texto.lastIndexOf("}");

 const bloque = inicio !== -1 && fin !== -1 && fin > inicio

  ? texto.slice(inicio, fin + 1)

  : texto;



 // Intento 2: parseo directo del bloque recortado

 try {

  return JSON.parse(bloque);

 } catch (_) {

  // continúa al fallback

 }



 // Intento 3: Gemini a veces mete saltos de línea / tabs reales (sin escapar)

 // dentro de los valores de string, lo cual rompe JSON.parse. Reparamos

 // escapando los caracteres de control que caen DENTRO de comillas.

 try {

  const reparado = escaparControlesDentroDeStrings(bloque);

  return JSON.parse(reparado);

 } catch (_) {

  // continúa al error final

 }



 throw new Error("No se pudo extraer un JSON válido de la respuesta");

}



/**

 * Recorre el texto carácter por carácter llevando control de si estamos

 * dentro de un string JSON (entre comillas dobles no escapadas). Dentro de

 * un string, reemplaza saltos de línea/retornos/tabs reales por sus

 * versiones escapadas (\n, \r, \t), que es lo que exige el estándar JSON.

 */

function escaparControlesDentroDeStrings(texto) {

 let resultado = "";

 let dentroDeString = false;

 let escapando = false;



 for (let i = 0; i < texto.length; i++) {

  const ch = texto[i];



  if (dentroDeString) {

   if (escapando) {

    resultado += ch;

    escapando = false;

    continue;

   }

   if (ch === "\\") {

    resultado += ch;

    escapando = true;

    continue;

   }

   if (ch === '"') {

    resultado += ch;

    dentroDeString = false;

    continue;

   }

   if (ch === "\n") {

    resultado += "\\n";

    continue;

   }

   if (ch === "\r") {

    resultado += "\\r";

    continue;

   }

   if (ch === "\t") {

    resultado += "\\t";

    continue;

   }

   resultado += ch;

   continue;

  }



  // Fuera de un string

  if (ch === '"') {

   dentroDeString = true;

  }

  resultado += ch;

 }



 return resultado;

}



/**

 * Construye el prompt final para Gemini, siempre pidiendo

 * únicamente JSON como salida.

 */

function construirPrompt(tipoDocumento, datosFiltrados, respuestasFiltradas) {

 const instruccionesFormato = `

Devuelve ÚNICAMENTE un JSON válido (sin bloques de markdown, sin texto adicional) con esta forma exacta:



{

 "titulo": "string",

 "contenido": "string (documento completo en texto/markdown simple)",

 "preguntas_pendientes": [

  { "campo": "string", "pregunta": "string" }

 ]

}



"preguntas_pendientes" debe estar vacío [] casi siempre. Redacta SIEMPRE el documento completo con lo disponible.

`.trim();



 return `

Genera un documento legal para un negocio en Perú utilizando exclusivamente los datos proporcionados.



Tipo de documento: ${tipoDocumento}



Datos del negocio:

${JSON.stringify(datosFiltrados)}



Información proporcionada por el negocio:

${JSON.stringify(respuestasFiltradas)}



Reglas:

- No inventes datos específicos del negocio (montos, plazos exactos, proveedores, políticas concretas) que no te hayan dado.

- Cuando falte un dato no esencial, redacta esa parte con lenguaje genérico y estándar del rubro/tipo de documento en Perú (frases suaves tipo "el cliente puede solicitar cambios y devoluciones conforme a la normativa vigente" o "los datos personales se usan únicamente para brindar el servicio"), en vez de dejarlo en blanco o preguntar.

- Usa "preguntas_pendientes" ÚNICAMENTE si es imposible generar el documento sin ese dato (por ejemplo, no tienes ni el nombre ni el rubro del negocio). Para este tipo de documentos eso casi nunca ocurre: casi siempre se puede redactar una versión completa y razonable con lo disponible.

- Prioriza siempre entregar un documento completo, aunque sea genérico en las partes sin información específica, sobre hacer preguntas.

- Redacta en español, con lenguaje profesional, claro y comprensible.

- El documento debe estar personalizado con los datos reales del negocio que sí tengas (nombre, rubro, contacto, etc.) y listo para ser revisado y publicado.



${instruccionesFormato}

`.trim();

}



// ------------------------------------------------------------------

// CLOUD FUNCTION

// ------------------------------------------------------------------



const generarDocumentoLegal = onCall(

 {

  // Ajusta región/memoria según el resto de tus funciones si es necesario.

  region: "us-central1",

  memory: "256MiB",

  timeoutSeconds: 60,

 },

 async (request) => {

  try {

   const data = request.data || {};



   const tipo_documento = data.tipo_documento;

   const datos_negocio = data.datos_negocio;

   const respuestas_negocio = data.respuestas_negocio;



   // ---- VALIDACIONES ----



   if (typeof tipo_documento !== "string" || !tipo_documento.trim()) {

    return {

     success: false,

     error: {

      code: "TIPO_DOCUMENTO_REQUERIDO",

      message: "El campo 'tipo_documento' es obligatorio.",

     },

    };

   }



   if (tipo_documento.length > MAX_TIPO_DOCUMENTO_LEN) {

    return {

     success: false,

     error: {

      code: "TIPO_DOCUMENTO_INVALIDO",

      message: "El campo 'tipo_documento' excede el tamaño permitido.",

     },

    };

   }



   if (!TIPOS_PERMITIDOS.includes(tipo_documento)) {

    return {

     success: false,

     error: {

      code: "TIPO_DOCUMENTO_NO_SOPORTADO",

      message: `Tipo de documento no soportado. Tipos permitidos: ${TIPOS_PERMITIDOS.join(", ")}.`,

     },

    };

   }



   if (

    datos_negocio !== undefined &&

    (typeof datos_negocio !== "object" || Array.isArray(datos_negocio))

   ) {

    return {

     success: false,

     error: {

      code: "DATOS_NEGOCIO_INVALIDOS",

      message: "El campo 'datos_negocio' debe ser un objeto.",

     },

    };

   }



   if (

    respuestas_negocio !== undefined &&

    (typeof respuestas_negocio !== "object" || Array.isArray(respuestas_negocio))

   ) {

    return {

     success: false,

     error: {

      code: "RESPUESTAS_NEGOCIO_INVALIDAS",

      message: "El campo 'respuestas_negocio' debe ser un objeto.",

     },

    };

   }



   // Bloquear intentos de sobrescribir configuración de Gemini desde el request

   if (

    data.apiKey ||

    data.api_key ||

    data.geminiConfig ||

    data.model ||

    data.modelo

   ) {

    return {

     success: false,

     error: {

      code: "PARAMETRO_NO_PERMITIDO",

      message:

       "No está permitido enviar configuración de Gemini (apiKey, model, etc.) en el request.",

     },

    };

   }



   // ---- FILTRADO DE DATOS (solo lo relevante y no sensible) ----



   const datosFiltrados = filtrarObjeto(

    datos_negocio,

    CAMPOS_NEGOCIO_PERMITIDOS

   );

   const respuestasFiltradas = filtrarObjeto(

    respuestas_negocio,

    CAMPOS_RESPUESTAS_PERMITIDOS

   );



   const tamanioAproximado =

    JSON.stringify(datosFiltrados).length +

    JSON.stringify(respuestasFiltradas).length;



   if (tamanioAproximado > MAX_TOTAL_PAYLOAD_LEN) {

    return {

     success: false,

     error: {

      code: "PAYLOAD_DEMASIADO_GRANDE",

      message: "La información proporcionada excede el tamaño permitido.",

     },

    };

   }



   // ---- API KEY DE GEMINI (ya existente en el proyecto) ----



   const apiKey = process.env.PRIVATEKEY_GEMINI;

   if (!apiKey) {

    return {

     success: false,

     error: {

      code: "GEMINI_NO_CONFIGURADO",

      message:

       "No se encontró la variable de entorno PRIVATEKEY_GEMINI configurada en el servidor.",

     },

    };

   }



   // ---- LLAMADA A GEMINI (una sola llamada por generación) ----



   const genAI = new GoogleGenerativeAI(apiKey);

   const model = genAI.getGenerativeModel({

    model: "gemini-1.5-flash",

    generationConfig: {

     // Fuerza a Gemini a devolver JSON válido de forma nativa,

     // en vez de confiar solo en la instrucción del prompt.

     responseMimeType: "application/json",

     maxOutputTokens: 8192,

    },

   });



   const prompt = construirPrompt(

    tipo_documento,

    datosFiltrados,

    respuestasFiltradas

   );



   let textoRespuesta;

   try {

    const resultado = await model.generateContent(prompt);

    textoRespuesta = resultado.response.text();

   } catch (errGemini) {

    console.error("Error real al llamar a Gemini:", errGemini);

    return {

     success: false,

     error: {

      code: "ERROR_GEMINI",

      message: "Ocurrió un error al generar el documento con Gemini.",

     },

    };

   }



   // ---- PARSEO DE LA RESPUESTA ----



   let parseado;

   try {

    parseado = limpiarYParsearJSON(textoRespuesta);

   } catch (errParse) {

    console.error("No se pudo parsear la respuesta de Gemini. Texto crudo:", textoRespuesta);

    console.error("Detalle del error de parseo:", errParse);

    return {

     success: false,

     error: {

      code: "RESPUESTA_INVALIDA",

      message: "No se pudo interpretar la respuesta generada. Intenta nuevamente.",

     },

    };

   }



   const titulo =

    (parseado && parseado.titulo) || TITULOS[tipo_documento] || "";

   const contenido = (parseado && parseado.contenido) || "";

   const preguntasPendientes =

    parseado && Array.isArray(parseado.preguntas_pendientes)

     ? parseado.preguntas_pendientes

     : [];



   return {

    success: true,

    tipo_documento,

    titulo,

    contenido,

    preguntas_pendientes: preguntasPendientes,

   };

  } catch (errGeneral) {

   return {

    success: false,

    error: {

     code: "ERROR_INTERNO",

     message: "Ocurrió un error inesperado al procesar la solicitud.",

    },

   };

  }

 }

);



module.exports = { generarDocumentoLegal };