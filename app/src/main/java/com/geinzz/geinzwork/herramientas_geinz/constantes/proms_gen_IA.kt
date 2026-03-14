package com.geinzz.geinzwork.herramientas_geinz.constantes

import com.geinzz.geinzwork.data.model.dataclass_seguridad.EntidadNLP
import com.geinzz.geinzwork.data.model.ia_inmobiliara_tts
import android.util.Log

object proms_gen_IA {

    fun generarPromptPromoVenta_solo_una_generacion(
        tituloUsuario: String,
        descripcionUsuario: String,
        nombreTienda: String,
        localidad: String
    ): String {
        return """
Mejora el título y la descripción de una promoción con ENFOQUE EN VENTA DIRECTA.
Usa SOLO la información proporcionada. NO inventes datos ni precios.
Si el usuario menciona un precio (ej: 120, cuesta 120, S/120), usa el símbolo s/

Reglas:
- Genera UNA SOLA opción
- Título ≤60 caracteres
- Descripción 30–50 palabras
- Español comercial y persuasivo
- Llamados a la acción claros (ej: Aprovecha, Compra hoy, No te lo pierdas)
- Sin emojis
- No exageres beneficios irreales

Datos reales:
titulo:$tituloUsuario
descripcion:$descripcionUsuario
tienda:$nombreTienda
localidad:$localidad

Salida EXACTA:
T:
D:
""".trimIndent()
    }


    fun generarPromptPromoAtencion_solo_una_generacion(
        tituloUsuario: String,
        descripcionUsuario: String,
        nombreTienda: String,
        localidad: String
    ): String {
        return """
Mejora el título y la descripción de una promoción con ENFOQUE EN LLAMAR LA ATENCIÓN.
Usa SOLO la información proporcionada. NO inventes datos ni precios.
Si el usuario menciona un precio, usa el símbolo s/

Reglas:
- Genera UNA SOLA opción
- Título ≤60 caracteres
- Descripción 30–50 palabras
- Español claro y atractivo
- Usa preguntas, ganchos creativos o beneficios impactantes
- Sin emojis
- No uses promesas falsas

Datos reales:
titulo:$tituloUsuario
descripcion:$descripcionUsuario
tienda:$nombreTienda
localidad:$localidad

Salida EXACTA:
T:
D:
""".trimIndent()
    }


    fun generarPromptPromoInformativo_solo_una_generacion(
        tituloUsuario: String,
        descripcionUsuario: String,
        nombreTienda: String,
        localidad: String
    ): String {
        return """
Mejora el título y la descripción de una promoción con ENFOQUE PROFESIONAL E INFORMATIVO.
Usa SOLO la información proporcionada. NO inventes datos ni precios.
Si el usuario menciona un precio, usa el símbolo s/

Reglas:
- Genera UNA SOLA opción
- Título ≤60 caracteres
- Descripción 30–50 palabras
- Español profesional, claro y confiable
- Explica el valor del producto o servicio sin exageraciones
- Sin emojis
- Tono serio y elegante

Datos reales:
titulo:$tituloUsuario
descripcion:$descripcionUsuario
tienda:$nombreTienda
localidad:$localidad

Salida EXACTA:
T:
D:
""".trimIndent()
    }


    fun generarPromptPromoVenta(
        tituloUsuario: String,
        descripcionUsuario: String,
        nombreTienda: String,
        localidad: String
    ): String {
        return """
Mejora el título y la descripción de una promoción con ENFOQUE EN VENTA DIRECTA.
Usa SOLO la información proporcionada. NO inventes datos ni precios.
Si el usuario menciona un precio (ej: 120, cuesta 120, S/120), usa el símbolo s/

Reglas:
- Genera EXACTAMENTE 3 opciones distintas
- Título ≤60 caracteres
- Descripción 30–50 palabras
- Español comercial y persuasivo
- Llamados a la acción claros (ej: Aprovecha, Compra hoy, No te lo pierdas)
- Sin emojis
- No exageres beneficios irreales

Datos reales:
titulo:$tituloUsuario
descripcion:$descripcionUsuario
tienda:$nombreTienda
localidad:$localidad

Salida EXACTA:
Opcion 1:
T:
D:

Opcion 2:
T:
D:

Opcion 3:
T:
D:
""".trimIndent()
    }

    fun generarPromptPromoAtencion(
        tituloUsuario: String,
        descripcionUsuario: String,
        nombreTienda: String,
        localidad: String
    ): String {
        return """
Mejora el título y la descripción de una promoción con ENFOQUE EN LLAMAR LA ATENCIÓN.
Usa SOLO la información proporcionada. NO inventes datos ni precios.
Si el usuario menciona un precio, usa el símbolo s/

Reglas:
- Genera EXACTAMENTE 3 opciones distintas
- Título ≤60 caracteres
- Descripción 30–50 palabras
- Español claro y atractivo
- Usa preguntas, ganchos creativos o beneficios impactantes
- Sin emojis
- No uses promesas falsas

Datos reales:
titulo:$tituloUsuario
descripcion:$descripcionUsuario
tienda:$nombreTienda
localidad:$localidad

Salida EXACTA:
Opcion 1:
T:
D:

Opcion 2:
T:
D:

Opcion 3:
T:
D:
""".trimIndent()
    }

    fun generarPromptPromoInformativo(
        tituloUsuario: String,
        descripcionUsuario: String,
        nombreTienda: String,
        localidad: String
    ): String {
        return """
Mejora el título y la descripción de una promoción con ENFOQUE PROFESIONAL E INFORMATIVO.
Usa SOLO la información proporcionada. NO inventes datos ni precios.
Si el usuario menciona un precio, usa el símbolo s/

Reglas:
- Genera EXACTAMENTE 3 opciones distintas
- Título ≤60 caracteres
- Descripción 30–50 palabras
- Español profesional, claro y confiable
- Explica el valor del producto o servicio sin exageraciones
- Sin emojis
- Tono serio y elegante

Datos reales:
titulo:$tituloUsuario
descripcion:$descripcionUsuario
tienda:$nombreTienda
localidad:$localidad

Salida EXACTA:
Opcion 1:
T:
D:

Opcion 2:
T:
D:

Opcion 3:
T:
D:
""".trimIndent()
    }


    fun generarPromptNotificacionOptimizado(
        tituloPublicacion: String, descCorta: String, // ≤60 chars
        nombreTienda: String, localidad: String, diasRestantes: Int
    ): String {

        return """
Genera un título (≤40) y una descripción (≤90) para notificación.
No inventes datos. Español neutro.
Usa MÁXIMO 1 emoji SOLO en el título. Sin emojis en la descripción.
Texto claro, directo y comercial. Incluye CTA breve.

Datos:
t:$tituloPublicacion
d:$descCorta
n:$nombreTienda
l:$localidad
r:$diasRestantes

Urgencia:
r=1 -> "Último día"
r<=3 -> "Últimos días"
r>3 -> "Por tiempo limitado"

Salida EXACTA:
T: texto
D: texto
""".trimIndent()
    }

    fun generarPromptPromocionProduccion(
        tituloUsuario: String,
        descripcionUsuario: String,
        nombreTienda: String,
        localidad: String,
    ): String {

        return """
Mejora el título y la descripción de una promoción usando SOLO la información dada.
No inventes datos ni precios.
Genera EXACTAMENTE 3 opciones distintas.

Reglas:
- Título ≤60 caracteres
- Descripción 30–50 palabras
- Español claro y comercial
- Sin emojis
- Texto profesional y directo

Datos reales:
titulo:$tituloUsuario
descripcion:$descripcionUsuario
tienda:$nombreTienda
localidad:$localidad


Salida EXACTA:
Opcion 1:
T:
D:

Opcion 2:
T:
D:

Opcion 3:
T:
D:
""".trimIndent()
    }


    fun generarPromptPromocion_text_compartir(
        tituloUsuario: String,
        descripcionUsuario: String,
    ): String {

        return """
Crea un mensaje muy corto para compartir y provocar clic inmediato.

Reglas:
- Máx 80 caracteres
- Español
- Usa información concreta del título
- Inicio fuerte y directo
- EXACTAMENTE 2 emojis
- Sin preguntas
- Sin relleno
- Devuelve SOLO el texto

Datos:
$tituloUsuario
$descripcionUsuario
""".trimIndent()
    }


    fun generarPromptWhatsAppContacto(
        titulo: String,
        descripcion: String,
    ): String {

        return """
Actúa como un cliente interesado que contacta por WhatsApp.

Reglas:
- Mensaje de WhatsApp
- Máx 60 caracteres
- Español
- Natural y respetuoso
- Estructura: saludo + interés en el título + pregunta de disponibilidad
- Incluye EXACTAMENTE 1 emoji
- No inventes datos
- Devuelve SOLO el mensaje

Datos:
Título: $titulo
Descripción: $descripcion
""".trimIndent()
    }


    fun generarPromptNotificacionSeleccionada(
        tituloPublicacion: String, descCorta: String
    ): String {

        return """
Crea una notificación PUSH comercial.
Reescribe desde cero, pero CONSERVA los datos clave del contexto.
No inventes información.

Reglas:
- Mantén producto, precio y lugar si existen
- Cambia redacción y enfoque (beneficio / urgencia / acción)
- Título y descripción deben ser nuevos
- Título ≤40, descripción ≤90
- Español neutro, CTA corto
- 1 emoji SOLO en título

Base (contexto):
T:$tituloPublicacion
D:$descCorta

Salida EXACTA:
T:
D:
""".trimIndent()
    }

    fun promptNotificacionVenta(tituloPublicacion: String, descCorta: String): String {
        return """
Adapta y mejora una notificación PUSH de tipo: Venta 🛒.
NO crees información nueva.
Optimiza el texto del usuario para que sea persuasivo y orientado a conversión.

Objetivo:
Impulsar clics o compras manteniendo los datos reales (producto, precio, lugar si existen).

Reglas:
- Mantén el significado original
- Título ≤40 caracteres, descripción ≤90
- Lenguaje claro, directo y comercial
- CTA corto y concreto
- 1 emoji SOLO en el título

Texto original:
T:$tituloPublicacion
D:$descCorta

Salida EXACTA:
T:
D:
""".trimIndent()
    }


    fun promptNotificacionAtencion(tituloPublicacion: String, descCorta: String): String {
        return """
Adapta y mejora una notificación PUSH de tipo: Llamado de atención ✨.
NO inventes información.

Objetivo:
Captar interés y aumentar la apertura de la notificación.

Reglas:
- Respeta el contenido original
- NO elimines precios si existen
- NO inventes precios
- Si hay precios, formátalos como: s/
- Título ≤40 caracteres, descripción ≤90
- Usa ganchos claros, no engañosos
- CTA opcional y corto
- 1 emoji SOLO en el título

Texto original:
T:$tituloPublicacion
D:$descCorta

Salida EXACTA:
T:
D:
""".trimIndent()
    }


    fun promptNotificacionUrgencia(tituloPublicacion: String, descCorta: String): String {
        return """
Adapta y mejora una notificación PUSH de tipo: Urgencia ⏰.
NO agregues escasez falsa ni inventes tiempos o precios.

Objetivo:
Motivar acción rápida cuando el contexto lo justifica.

Reglas:
- Mantén los datos originales
- NO borres precios
- NO inventes precios
- Precios siempre en formato: s/
- Título ≤40 caracteres, descripción ≤90
- Urgencia clara pero real
- CTA corto y directo
- 1 emoji SOLO en el título

Texto original:
T:$tituloPublicacion
D:$descCorta

Salida EXACTA:
T:
D:
""".trimIndent()
    }


    fun promptNotificacionNovedad(tituloPublicacion: String, descCorta: String): String {
        return """
Adapta y mejora una notificación PUSH de tipo: Novedad 🆕.
NO inventes lanzamientos, exclusividades ni precios.

Objetivo:
Informar y generar interés en algo reciente o actualizado.

Reglas:
- Respeta el contenido original
- NO elimines precios si están presentes
- NO inventes precios
- Usa el formato s/ si hay precios
- Título ≤40 caracteres, descripción ≤90
- Lenguaje informativo y atractivo
- CTA corto opcional
- 1 emoji SOLO en el título

Texto original:
T:$tituloPublicacion
D:$descCorta

Salida EXACTA:
T:
D:
""".trimIndent()
    }


    fun promptNotificacionServicios(titulo: String, descCorta: String): String {
        return """
Adapta y mejora una notificación PUSH de tipo: Servicio 🛠️.
NO crees información nueva.
Optimiza el texto escrito por el usuario para que sea claro, profesional y útil.

Objetivo:
Informar sobre cambios en locales, métodos de pago, servicios o novedades importantes.

Reglas:
- Mantén el significado original
- Título ≤40 caracteres, descripción ≤90
- Lenguaje claro y directo
- CTA opcional
- 1 emoji SOLO en el título

Texto original:
T:$titulo
D:$descCorta

Salida EXACTA:
T:
D:
""".trimIndent()
    }


    fun promptNotificacionCita(titulo: String, descCorta: String): String {
        return """
Adapta y mejora una notificación PUSH de tipo: Cita / Reserva 📅.
NO inventes datos.
Convierte el texto del usuario en un recordatorio claro y accionable.

Objetivo:
Recordar citas, reservas o servicios agendados.

Reglas:
- Respeta fecha, hora y contexto
- Título ≤40 caracteres, descripción ≤90
- CTA corto si aplica (confirmar, asistir, ver)
- 1 emoji SOLO en el título

Texto original:
T:$titulo
D:$descCorta

Salida EXACTA:
T:
D:
""".trimIndent()
    }


    fun promptNotificacionReposicion(titulo: String, descCorta: String): String {
        return """
Adapta y mejora una notificación PUSH de tipo: Reposición 🛒.
NO agregues productos ni datos nuevos.
Optimiza el texto para que informe claramente la llegada o reposición.

Objetivo:
Avisar disponibilidad de nuevos productos o stock renovado.

Reglas:
- Mantén la información original
- Título ≤40 caracteres, descripción ≤90
- CTA corto invitando a revisar
- 1 emoji SOLO en el título

Texto original:
T:$titulo
D:$descCorta

Salida EXACTA:
T:
D:
""".trimIndent()
    }


    fun promptNotificacionOperativa(titulo: String, descCorta: String): String {
        return """
Adapta y mejora una notificación PUSH de tipo: Operativa ⚠️.
NO crees ni exageres información.
Haz el mensaje más claro y entendible para el usuario.

Objetivo:
Informar cambios de última hora, cierres inesperados o ajustes operativos.

Reglas:
- Mantén el mensaje original
- Título ≤40 caracteres, descripción ≤90
- Claridad y urgencia leve
- CTA opcional
- 1 emoji SOLO en el título

Texto original:
T:$titulo
D:$descCorta

Salida EXACTA:
T:
D:
""".trimIndent()
    }

}


fun generarPromptNombreGeneracionIA(
    titulo: String,
    descripcion: String
): String {
    return """
Devuelve SOLO el nombre final.
NO incluyas etiquetas como "Nombre:", ni comillas, ni explicaciones.
Máximo 40 caracteres.
Incluye exactamente 1 emoji relevante.
Resume sin inventar.

Título: $titulo
Descripción: $descripcion
""".trimIndent()
}


//val GEINZ_SEARCH_PARSER_PROMPT = """
//Eres Geinz Search Parser, un motor de procesamiento de lenguaje natural de alta precisión.
//
//Tu única función es convertir el texto del usuario
//en un objeto JSON estructurado para filtrar el historial de generaciones de IA.
//
//Reglas estrictas de análisis
//
//1. Limpieza absoluta
//Elimina palabras de intención o relleno. Ignora expresiones como:
//"búscame", "muéstrame", "quiero ver", "encuéntrame", "dónde está",
//"todas las", "que tengan", "a ver", "enséñame".
//
//2. Términos detectados
//Extrae TODOS los conceptos o temas relevantes mencionados por el usuario.
//Devuélvelos en singular y en minúsculas.
//No combines términos.
//No inventes términos.
//
//Ejemplo:
//"generaciones de creatina proteína y colágeno"
//→ ["creatina", "proteína", "colágeno"]
//
//3. Precio (si aplica)
//Si se menciona un monto (ej. "20 soles", "50"), extrae solo el número entero.
//Si no se menciona, usa null.
//
//4. Tiempo
//Detecta referencias temporales como:
//"hoy", "ayer", "esta semana", "este mes", "este año",
//o fechas explícitas como "07/06/2016", "febrero 2017".
//
//Devuélvelas como texto literal.
//Si no hay referencia temporal, usa null.
//
//5. Prioridad fija
//Si el usuario menciona que algo es "fijo", "guardado", "permanente" o equivalente,
//establece prioridad_fija en true. Caso contrario, false.
//
//Formato de salida obligatorio
//
//Devuelve SIEMPRE este objeto JSON:
//
//{
//  "terminos": [],
//  "precio": null,
//  "tiempo": null,
//  "prioridad_fija": false
//}
//
//Reglas finales
//- Responde únicamente con JSON válido
//- No saludes
//- No expliques
//- No agregues texto adicional
//- No uses Markdown ni bloques de código
//""".trimIndent()

fun String?.clean(): String? =
    this?.takeIf { it.isNotBlank() && it.lowercase() != "null" }


fun procesaro_por_vos(
    nombreDelNegocio: String,
    cantidad: Int,
    terminos: List<String>,
    tiempo: String?,
    precio: String?,
    prioridad: String?,
    tipo: String?
): String {

    // --- Construcción flexible de la descripción ---
    val partesBusqueda = mutableListOf<String>()

    if (terminos.isNotEmpty()) {
        partesBusqueda.add(terminos.joinToString(", "))
    }

    tiempo.clean()?.let {
        partesBusqueda.add(it)
    }

    precio.clean()?.let {
        partesBusqueda.add("con un precio $it")
    }

    val descripcionBusqueda =
        if (partesBusqueda.isEmpty()) ""
        else partesBusqueda.joinToString(" ")

    val textoTipo = tipo.clean() ?: "resultados"

    val textoPrioridad = when (prioridad.clean()?.lowercase()) {
        "guardado", "permanente" -> " que tienes guardado"
        else -> ""
    }

    val ayudasFinales = if (cantidad == 0) {
        listOf(
            "Puedo intentar la búsqueda con otros criterios.",
            "La búsqueda se puede ajustar en cualquier momento.",
            "Hay otras formas de encontrar lo que buscas.",
            "Puedo seguir ayudándote con otra opción."
        )
    } else {
        listOf(
            "Puedo ayudarte a ajustar los resultados.",
            "Los filtros se pueden modificar si lo necesitas.",
            "Hay más opciones disponibles para explorar.",
            "Puedo seguir asistiendo con esta búsqueda."
        )
    }.random()


    // --- Respuesta principal ---
    val respuestaPrincipal = when {
        cantidad == 0 -> listOf(
            "No encontré $textoTipo $descripcionBusqueda en $nombreDelNegocio.",
            "No hay resultados disponibles con esos criterios en $nombreDelNegocio.",
            "No obtuve coincidencias esta vez en $nombreDelNegocio.",
            "No se encontraron resultados para esa búsqueda."
        ).random()

        cantidad in 1..20 -> listOf(
            "Encontré $cantidad $textoTipo $descripcionBusqueda en $nombreDelNegocio.",
            "Tengo $cantidad $textoTipo listos para mostrar.",
            "Estos son los $cantidad $textoTipo disponibles.",
            "Ya preparé los $cantidad $textoTipo para que los revises."
        ).random()

        cantidad > 20 -> listOf(
            "Encontré varios $textoTipo $descripcionBusqueda en $nombreDelNegocio.",
            "Hay una cantidad considerable de $textoTipo disponibles.",
            "Tengo muchos resultados listos para mostrar.",
            "Encontré múltiples opciones para esta búsqueda."
        ).random()

        else -> listOf(
            "Aquí tienes los $textoTipo $descripcionBusqueda en $nombreDelNegocio.",
            "$nombreDelNegocio, estos son los $textoTipo que encontré para ti.",
            "Ya dejé listos los resultados para que los revises con calma."
        ).random()
    }

    // --- Resultado final (respuesta + ayuda) ---
    return "$respuestaPrincipal $ayudasFinales"
}

//fun procesarBusquedaConIA_promp(textoUsuario: String): String {
//
//    return """
//Eres Geinz Search Parser, un motor de procesamiento de lenguaje natural de alta precisión.
//
//Tu única función es convertir el texto del usuario
//en un objeto JSON estructurado para filtrar el historial de generaciones de IA.
//
//Reglas estrictas de análisis
//
//1. Limpieza absoluta
//Elimina palabras de intención, verbos de acción y relleno. Ignora expresiones como:
//"búscame", "muéstrame", "quiero ver", "encuéntrame", "dónde está",
//"todas las", "que tengan", "a ver", "enséñame", "resultados", "mostrar", "realicé".
//
//2. Términos detectados
//Extrae SOLO conceptos o temas relevantes para un historial de IA.
//- Devuélvelos en singular y en minúsculas.
//- No combines términos.
//- No inventes términos.
//- No incluyas números, fechas o palabras genéricas.
//
//Ejemplo:
//"generaciones de creatina proteína y colágeno"
//→ ["creatina", "proteína", "colágeno"]
//
//3. Precio
//Si se menciona un monto (ej. "20 soles", "50"), extrae solo el número entero como texto.
//Si no se menciona, devuelve el valor JSON nulo literal: null (NO "null")
//
//4. Tiempo
//Detecta referencias temporales explícitas:
//- Relativas: "hoy", "ayer", "esta semana", "este mes", "este año", etc.
//- Absolutas: "dd/mm/yyyy", "yyyy-mm-dd", meses y años escritos.
//Devuelve el valor JSON nulo literal: null si no se detecta nada.
//
//5. Prioridad
//Si el usuario menciona que algo es "guardado", "fijo", "permanente" o equivalente,
//devuelve ese texto literal en el campo prioridad.
//Si no se menciona, devuelve null literal.
//
//6. Tipo de historial
//Detecta si el usuario se refiere explícitamente a alguno de estos tipos:
//- "publicacion"
//- "notificacion"
//- "generacion_publicacion_sin_pulicar"
//- "notificacion_sin_publicar"
//
//Devuelve SOLO uno de esos valores como texto.
//Si el usuario no especifica el tipo, devuelve null literal.
//
//Formato de salida obligatorio
//
//Devuelve SIEMPRE este objeto JSON:
//
//{
//  "terminos": [],
//  "precio": null,
//  "tiempo": null,
//  "prioridad": null,
//  "tipo": null
//}
//
//Reglas finales
//- Responde únicamente con JSON válido
//- No saludes
//- No expliques
//- No agregues texto adicional
//- No uses comillas para null
//- Usa null solo cuando el usuario NO haya mencionado explícitamente ese dato
//- No completes campos por suposición
//
//Texto del usuario:
//"$textoUsuario"
//""".trimIndent()
//}
fun procesarBusquedaConIA_promp(textoUsuario: String): String {

    return """
Eres Geinz Search Parser, un motor de NLP.

Convierte el texto del usuario en un JSON para filtrar historial de IA:

1. Limpieza
Elimina verbos de acción y palabras de relleno como: "búscame", "muéstrame", "quiero ver", "encuéntrame", "a ver", "enséñame", "mostrar", "realicé".

2. Términos
Extrae SOLO conceptos relevantes, en singular y minúscula. No combines términos, no inventes, no incluyas números, fechas o palabras genéricas.
Ejemplo: "generaciones de creatina proteína" → ["creatina","proteína"] 

3. Días restantes
Detecta menciones futuras de días que quedan: "quedan 5 días", "hasta 3 días", "menos de 5 días", "más de 5 días".  
- Devuelve **solo el número entero**, sin comparadores ni palabras.  
- Ejemplo: "quedan 5 días" → "5", "más de 10 días" → "10".  
- Ignora tiempo pasado: "hace 2 días", "5 días atrás". Devuelve null si no hay.

4. Tiempo
Detecta referencias temporales pasadas o relativas: "hoy", "ayer", "esta semana", fechas. Devuelve null si no hay.

5. Prioridad
Si menciona "guardado", "fijo", "permanente", devuelve el texto. Si no, null.

6. Tipo
Detecta uno de: "publicacion", "notificacion", "generacion_publicacion_sin_pulicar", "notificacion_sin_publicar". Si no, null.

JSON obligatorio:

{
  "terminos": [],
  "dias_restantes": null,
  "tiempo": null,
  "prioridad": null,
  "tipo": null
}

Reglas finales
- Responde solo con JSON válido, sin explicaciones ni saludos.
- Usa null literal si no hay dato.
- No completes campos por suposición.

Texto del usuario:
"$textoUsuario"
""".trimIndent()
}


fun extraer_terminos_para_GenIA(textos: String): String {
    return """
        Eres un extractor de términos clave para búsquedas y filtrado. 
        Analiza el siguiente texto y devuelve únicamente las palabras más importantes, únicas y representativas en formato JSON:

        Texto:
        "$textos"
    """.trimIndent()
}

//fun construirPromptNLP(textoUsuario: String): String {
//    return """
//Eres un extractor NLP.
//Extrae la intención y el término clave del texto.
//Acciones posibles:
//- llamar
//- buscar
//- whatsapp
//- ruta
//- info
//- distancia
//Reglas:
//- No expliques nada
//- No inventes
//- Devuelve SOLO JSON válido
//- Si no es claro usa "desconocido"
//- Normaliza el término clave eliminando artículos y mayúsculas
//Formato:
//{"a":"","t":"","c":"a|m|b"}
//Texto:
//$textoUsuario
//""".trim()
//}
//fun construirPromptNLP(textoUsuario: String): String {
//    return """
//Eres un extractor NLP de emergencias.
//Devuelve SOLO un objeto JSON válido.
//
//Campos obligatorios:
//"a": llamar, whatsapp, dar_numero, buscar, ruta, info, distancia, desconocido
//"t": término clave en minúsculas
//"g": salud, seguridad u otro
//"c": a (alta), m (media), b (baja)
//
//Reglas:
//- Si no hay acción clara, usa "desconocido"
//- No agregues texto fuera del JSON
//- No expliques nada
//
//Ejemplos:
//"Quiero ir a la comisaría central"
//{"a":"ruta","t":"comisaría","g":"seguridad","c":"a"}
//
//"Dime el teléfono del hospital regional"
//{"a":"dar_numero","t":"hospital","g":"salud","c":"a"}
//
//"Hola qué tal"
//{"a":"desconocido","t":"","g":"otro","c":"b"}
//
//Texto:
//"$textoUsuario"
//
//JSON:
//""".trimIndent()
//}

fun construirPromptNL2P(textoUsuario: String): String {
    return """
Eres un extractor NLP de emergencias.
Responde SOLO JSON válido.

Formato:
{"a":"","t":"","g":"","c":""}

Campos:
"a": ruta, dar_numero, llamar, whatsapp, info, distancia, buscar, desconocido
"t": término clave del texto normalizado en minúsculas sin artículos sin diminutivo ni cambios bruscos
"g": salud, seguridad, otro
"c": a (alta), m (media), b (baja)

Reglas:
- Si solo reporta un hecho → "a":"desconocido"
- Si es saludo o no se entiende → {"a":"desconocido","t":"","g":"otro","c":"b"}
- No expliques nada.
- No agregues texto fuera del JSON.

Ejemplo:
-"Me robaron el celular"
{"a":"desconocido","t":"robo","g":"seguridad","c":"a"}
-"quiero ver los poli"
{"a":"buscar","t":"polica","g":"seguridad","c":"m"}

Texto: "$textoUsuario"
""".trimIndent()
}

fun construirPromptNLP(textoUsuario: String): String {
    return """
Extractor NLP de emergencias.
Responde SOLO JSON:
{"a":"","t":"","g":"","c":""}

a: ruta, dar_numero, llamar, whatsapp, info, distancia, buscar, desconocido
t: palabra clave en minúsculas sin artículos ni plural
g: salud, seguridad, otro
c: a, m, b

Reglas:
1) Corrige errores ortográficos leves.
2) Si reporta incidente o síntoma → a:"desconocido".
3) Si pide mostrar, ver o explorar entidades → a:"buscar".
4) Si pide horario, direccion, telefono, numero u otro dato específico → a:"info".
5) numero explícito → dar_numero | llamar explícitamente → llamar | distancia → distancia | como llegar → ruta.
6) Delito → seguridad | Síntoma → salud.
7) Emergencia grave → c:"a" | Incidente no crítico → c:"m" | Navegación → c:"b".
8) Si no se entiende → {"a":"desconocido","t":"","g":"otro","c":"b"}.

Texto: "$textoUsuario"
""".trimIndent()
}


fun construir_promp_NLP_depromo_y_oferta(textoUsuario: String, categoira_selec: String): String {
    return """
    Eres un extractor de datos categoría $categoira_selec.
    Extrae producto principal (sin cantidades), atributos, precio, método de pago y comodidades.
    No inventes datos. Si no aparece:
    - precio: null
    - listas: []

    Diminutivos en forma normal.

    Método_pago solo puede ser:
    yape (si dice "llave"), plin (si dice "link"), efectivo, agora, visa, mastercard.

    Comodidades solo si aparecen o se parecen:
    wifi, zona_expandida, servicios_higienicos, camaras_de_seguridad, sala_de_espera,sala_juegos, mesa_para_ninos, estacionamiento, enchufe, aire_acondicionado, ingreso_mascotas.

    Responde solo JSON:
    {"principal":"string","atributos":[],"precio":number|null,"metodo_pago":[],"comodidades":[]}

Texto: "$textoUsuario"
""".trimIndent()
}

fun construir_prompt_NLP_para_busqueda(textoUsuario: String, categoria: String): String {
    return """
    Extrae términos de búsqueda del siguiente texto en categoría $categoria.
    
    Reglas:
    - Solo productos, cantidades, marcas y precios que aparezcan literalmente en el texto.
    - No inventes términos.
    - No agregues explicaciones.
    - No agregues texto adicional.
    - No uses markdown.
    - No nombre del negocio 
    - No ubicacion
    - Todo en minuscula y evita diminutivos
    
    La respuesta debe ser únicamente un arreglo de strings.
    Debe empezar con [ y terminar con ] NO AGREGES MAS TEXTO SOLO EL ARRAY.
    
    Texto: "$textoUsuario"
    """.trimIndent()
}



fun contruir_promp_ia_datos_inmobiliara(
    i: ia_inmobiliara_tts,
    perfil_selet: String
): String {

    Log.d("PROMPT_IA", "Perfil seleccionado: $perfil_selet")
    Log.d("PROMPT_IA", "Usuario: ${i.nombre_user}")
    Log.d("PROMPT_IA", "Tipo propiedad: ${i.tipo}")
    Log.d("PROMPT_IA", "Metros cuadrados: ${i.metros_cuadrados}")
    Log.d("PROMPT_IA", "Ubicación: ${i.calle_ubicada}")

    val lugares = (i.lista_lugares_cercanos + i.lista_lugares_seguros + i.lista_lugares_turisticos)
        .take(6)
        .joinToString(", ")

    Log.d("PROMPT_IA", "Lugares usados en prompt: $lugares")

    val enfoque = when (perfil_selet.lowercase().trim()) {
        "inversionista" -> "enfócate en plusvalía, rentabilidad por metro cuadrado y el potencial de desarrollo del ${i.tipo}."
        "familiar"      -> "enfócate en la amplitud de los ${i.metros_cuadrados}m², la seguridad y el espacio ideal para ver crecer a los hijos."
        "solitario"     -> "enfócate en la independencia, el manejo eficiente del espacio de ${i.metros_cuadrados}m² y la practicidad."
        else            -> "enfócate en la oportunidad única que representan estos ${i.metros_cuadrados}m² en una ubicación estratégica."
    }

    // construye solo las líneas de entorno que tienen datos reales
    val lineas_entorno = buildString {
        if (i.cantidad_lugares_encontrado > 0 && i.lista_lugares_cercanos.isNotEmpty()) {
            appendLine("- Lugares de interés cercanos: ${i.cantidad_lugares_encontrado} (ej: ${i.lista_lugares_cercanos.take(3).joinToString(", ")})")
        }
        if (i.cantidad_lugares_seguros > 0 && i.lista_lugares_seguros.isNotEmpty()) {
            appendLine("- Zonas seguras verificadas: ${i.cantidad_lugares_seguros} (ej: ${i.lista_lugares_seguros.take(2).joinToString(", ")})")
        }
        if (i.cantidad_lugares_turisticos > 0 && i.lista_lugares_turisticos.isNotEmpty()) {
            appendLine("- Atractivos turísticos: ${i.cantidad_lugares_turisticos} (ej: ${i.lista_lugares_turisticos.take(2).joinToString(", ")})")
        }
    }.trim()

    // instrucción de entorno adaptada según qué datos existen
    val instruccion_entorno = if (lineas_entorno.isEmpty()) {
        "No se encontraron datos de entorno cercano, así que enfócate solo en las características del inmueble y su ubicación estratégica."
    } else {
        "PODER DE LOS DATOS: Usa los datos de entorno para dar seguridad y credibilidad al cliente. Menciona las cantidades reales."
    }

    val seccion_entorno = if (lineas_entorno.isEmpty()) {
        "No hay datos de entorno disponibles para esta búsqueda."
    } else {
        "DATOS DE ENTORNO (menos de 500 metros):\n$lineas_entorno"
    }

    val prompt = """
        Eres un cerrador de ventas inmobiliarias de élite en Barranca.
        Cliente: ${i.nombre_user}.
        Producto: ${i.tipo} de ${i.metros_cuadrados}m² en ${i.estado}.
        Ubicación: ${i.calle_ubicada}.
        Perfil: $perfil_selet.
        
        $seccion_entorno
        
        TAREA: Convence al cliente en 3 párrafos muy breves.
        REGLAS CRÍTICAS:
        1. $instruccion_entorno
        2. MENCIONA EL TAMAÑO: Integra los ${i.metros_cuadrados} metros cuadrados con naturalidad.
        3. CONCORDANCIA Y FORMATO: Usa "esta/este" correctamente. NO uses asteriscos (**), ni negritas. Solo texto plano.
        4. DEBES mencionar el nombre de la inmobiliaria "House Capital Group".
        5. PERSUASIÓN: $enfoque.
        
        Estilo directo, profesional y sin errores ortográficos.
    """.trimIndent()

    Log.d("PROMPT_IA_FINAL", prompt)

    return prompt
}
