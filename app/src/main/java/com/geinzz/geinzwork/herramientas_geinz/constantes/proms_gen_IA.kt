package com.geinzz.geinzwork.herramientas_geinz.constantes

object proms_gen_IA {
    fun generarPromptPromoVenta(
        tituloUsuario: String,
        descripcionUsuario: String,
        nombreTienda: String,
        localidad: String
    ): String {
        return """
Mejora el título y la descripción de una promoción con ENFOQUE EN VENTA DIRECTA.
Usa SOLO la información proporcionada. NO inventes datos ni precios.
Si el usuario menciona un precio (ej: 120, cuesta 120, S/120), usa el símbolo S/.

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
Si el usuario menciona un precio, usa el símbolo S/.

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
Si el usuario menciona un precio, usa el símbolo S/.

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
