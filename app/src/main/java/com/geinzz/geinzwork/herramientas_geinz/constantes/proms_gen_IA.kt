package com.geinzz.geinzwork.herramientas_geinz.constantes

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



