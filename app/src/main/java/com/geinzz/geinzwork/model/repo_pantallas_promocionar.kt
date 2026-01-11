package com.geinzz.geinzwork.model

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.viewModelScope
import com.geinzz.geinzwork.data.model.NotificacionIA
import com.geinzz.geinzwork.data.model.OpcionPromocionIA
import com.geinzz.geinzwork.herramientas_geinz.constantes.constantes_subir_img_panel_tienda.procesarImagenWebPSinRecorte
import com.geinzz.geinzwork.ui.adapters.ui.pantallas.socios.acortarDescripcionNotificacion

import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.GenerativeBackend
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.storage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.UUID

class repo_pantallas_promocionar {

    suspend fun generar_promociones_con_IA(
        tituloUsuario: String,
        descripcionUsuario: String,
        nombreTienda: String,
        localidad: String,
        diasRestantes: Int
    ): List<OpcionPromocionIA> {
        return try {
            val model = Firebase.ai(
                backend = GenerativeBackend.googleAI()
            ).generativeModel("gemini-2.5-flash")

            val prompt = generarPromptPromocionProduccion(
                tituloUsuario = tituloUsuario,
                descripcionUsuario = descripcionUsuario,
                nombreTienda = nombreTienda,
                localidad = localidad,
                diasRestantes = diasRestantes
            )
            val result = model.generateContent(prompt)
            val texto = result.text ?: return emptyList()

            parsearOpcionesIA(texto)

        } catch (e: Exception) {
            Log.e("IA", "Error IA promociones: ${e.message}")
            emptyList()
        }
    }


    suspend fun subirImgNotificacionTemporal(
        context: Context,
        uri: Uri,
        idTemporal: String,
        idTienda: String
    ): Result<String> {
        return try {
            val storageRef = Firebase.storage.reference
                .child("tiendas/$idTienda/imagenes/notificaciones/$idTemporal")

            // Comprimir imagen y obtener ByteArray
            val imgComprimida: ByteArray = procesarImagenWebPSinRecorte(context, uri)

            // Subir imagen desde ByteArray
            storageRef.putBytes(imgComprimida).await()

            // Obtener URL de descarga
            val downloadUrl = storageRef.downloadUrl.await()

            Result.success(downloadUrl.toString())
        } catch (e: Exception) {
            Log.e("FirebaseUpload", "Error subiendo imagen: ${e.message}")
            Result.failure(e)
        }
    }


    suspend fun eliminarImgTemporal(idTienda: String, idTemporal: String): Boolean {
        return try {
            // 1️⃣ Eliminar imagen de Storage
            val storageRef = Firebase.storage.reference
                .child("tiendas/$idTienda/imagenes/notificaciones/$idTemporal")
            storageRef.delete().await()

            // 2️⃣ Eliminar ID o URL de Firestore
            val docRef = FirebaseFirestore.getInstance()
                .collection("Tiendas")
                .document(idTienda)
                .collection("notificaciones_temporales")
                .document(idTemporal)
            docRef.delete().await()

            true // todo salió bien
        } catch (e: Exception) {
            Log.e("FirebaseDelete", "Error eliminando imagen temporal: ${e.message}")
            false // algo falló
        }
    }

    suspend fun crear_notificacion_conIA_corta(
        tituloPublicacion: String,
        descCorta: String,
        onResultado: (NotificacionIA) -> Unit
    ) {

        val model = Firebase.ai(
            backend = GenerativeBackend.googleAI()
        ).generativeModel("gemini-2.5-flash")
        val descripcion_acortada = acortarDescripcionNotificacion(descCorta)

        try {
            val prompt = generarPromptNotificacionSeleccionada(
                tituloPublicacion,
                descripcion_acortada,
            )

            val inicio = System.currentTimeMillis()
            val result = model.generateContent(prompt)
            val textoGenerado = result.text ?: ""
            val fin = System.currentTimeMillis()

            Log.d("Gemini", "Tiempo: ${fin - inicio} ms")
            Log.d("Gemini", "Resultado:\n$textoGenerado")

            // 🔥 PARSEAR RESPUESTA
            val notificacion = parsearRespuestaGemini(textoGenerado)

            // 🔁 RETORNAR RESULTADO
            onResultado(notificacion)

        } catch (e: Exception) {
            Log.e("Gemini", "Error IA: ${e.message}")
        }

    }


    suspend fun mejorar_texto_perzonalizado_whatsapp(
        titulo_publicacion: String,
        descripcion: String,
        onResultado: (String) -> Unit
    ) {
        val model = Firebase.ai(
            backend = GenerativeBackend.googleAI()
        ).generativeModel("gemini-2.5-flash")

        val descripcionAcortada = acortarDescripcionNotificacion(descripcion)

        try {
            val prompt = generarPromptWhatsAppContacto(
                titulo_publicacion,
                descripcionAcortada
            )

            val inicio = System.currentTimeMillis()
            val result = model.generateContent(prompt)
            val fin = System.currentTimeMillis()

            val textoGenerado = result.text?.trim().orEmpty()

            Log.d("Gemini", "Tiempo: ${fin - inicio} ms")
            Log.d("Gemini", "Resultado:\n$textoGenerado")

            // ✅ Retornar UNA SOLA RESPUESTA
            onResultado(textoGenerado)

        } catch (e: Exception) {
            Log.e("Gemini", "Error IA: ${e.message}")
            onResultado("")
        }
    }



    suspend fun mejorar_texto_perzonalizado_compartir(
        titulo_publicacion: String,
        descripcion: String,
        onResultado: (String) -> Unit
    ) {
        val model = Firebase.ai(
            backend = GenerativeBackend.googleAI()
        ).generativeModel("gemini-2.5-flash")

        val descripcionAcortada = acortarDescripcionNotificacion(descripcion)

        try {
            val prompt = generarPromptPromocion_text_compartir(
                titulo_publicacion,
                descripcionAcortada,
            )

            val inicio = System.currentTimeMillis()
            val result = model.generateContent(prompt)
            val fin = System.currentTimeMillis()

            val textoGenerado = result.text?.trim().orEmpty()

            Log.d("Gemini", "Tiempo: ${fin - inicio} ms")
            Log.d("Gemini", "Resultado:\n$textoGenerado")

            // ✅ Retorna UNA SOLA RESPUESTA
            if (textoGenerado.isNotBlank()) {
                onResultado(textoGenerado)
            } else {
                onResultado("")
            }

        } catch (e: Exception) {
            Log.e("Gemini", "Error IA: ${e.message}")
            onResultado("")
        }
    }



    fun crear_notificacion_conIA(
        scope: CoroutineScope,
        tituloPublicacion: String,
        descCorta: String,
        nombreTienda: String,
        localidad: String,
        diasRestantes: Int,
        onResultado: (NotificacionIA) -> Unit
    ) {
        scope.launch {

            val model = Firebase.ai(
                backend = GenerativeBackend.googleAI()
            ).generativeModel("gemini-2.5-flash")

            try {
                val prompt = generarPromptNotificacionOptimizado(
                    tituloPublicacion,
                    descCorta,
                    nombreTienda,
                    localidad,
                    diasRestantes
                )

                val inicio = System.currentTimeMillis()
                val result = model.generateContent(prompt)
                val textoGenerado = result.text ?: ""
                val fin = System.currentTimeMillis()

                Log.d("Gemini", "Tiempo: ${fin - inicio} ms")
                Log.d("Gemini", "Resultado:\n$textoGenerado")

                // 🔥 PARSEAR RESPUESTA
                val notificacion = parsearRespuestaGemini(textoGenerado)

                // 🔁 RETORNAR RESULTADO
                onResultado(notificacion)

            } catch (e: Exception) {
                Log.e("Gemini", "Error IA: ${e.message}")
            }
        }
    }


    fun parsearOpcionesIA(texto: String): List<OpcionPromocionIA> {

        val opciones = mutableListOf<OpcionPromocionIA>()

        val bloques = texto.split("Opcion")
            .map { it.trim() }
            .filter { it.startsWith("1") || it.startsWith("2") || it.startsWith("3") }

        for (bloque in bloques) {

            val titulo = Regex("T:\\s*(.*)")
                .find(bloque)
                ?.groupValues
                ?.get(1)
                ?.trim()
                ?: continue

            val descripcion = Regex("D:\\s*([\\s\\S]*)")
                .find(bloque)
                ?.groupValues
                ?.get(1)
                ?.trim()
                ?: continue

            opciones.add(
                OpcionPromocionIA(
                    titulo = titulo,
                    descripcion = descripcion
                )
            )
        }

        return opciones
    }

    fun parsearRespuestaGemini(texto: String): NotificacionIA {
        var titulo = ""
        var descripcion = ""

        texto.lines().forEach { linea ->
            when {
                linea.startsWith("T:") ->
                    titulo = linea.removePrefix("T:").trim()

                linea.startsWith("D:") ->
                    descripcion = linea.removePrefix("D:").trim()
            }
        }

        return NotificacionIA(
            titulo = titulo,
            descripcion = descripcion
        )
    }


    fun generarPromptNotificacionOptimizado(
        tituloPublicacion: String,
        descCorta: String, // ≤60 chars
        nombreTienda: String,
        localidad: String,
        diasRestantes: Int
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
        diasRestantes: Int
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
duracion:$diasRestantes dias

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
- Máx 18 palabras
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
        tituloPublicacion: String,
        descCorta: String
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


}