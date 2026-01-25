package com.geinzz.geinzwork.model

import android.content.Context
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.lifecycle.viewModelScope
import com.geinzz.geinzwork.data.model.DatosDemograficosUsuario
import com.geinzz.geinzwork.data.model.NotificacionIA
import com.geinzz.geinzwork.data.model.OpcionPromocionIA
import com.geinzz.geinzwork.herramientas_geinz.constantes.constantes_subir_img_panel_tienda.procesarImagenWebPSinRecorte
import com.geinzz.geinzwork.herramientas_geinz.constantes.proms_gen_IA.generarPromptNotificacionOptimizado
import com.geinzz.geinzwork.herramientas_geinz.constantes.proms_gen_IA.generarPromptNotificacionSeleccionada
import com.geinzz.geinzwork.herramientas_geinz.constantes.proms_gen_IA.generarPromptPromocion_text_compartir
import com.geinzz.geinzwork.herramientas_geinz.constantes.proms_gen_IA.generarPromptWhatsAppContacto
import com.geinzz.geinzwork.herramientas_geinz.constantes.proms_gen_IA.promptNotificacionAtencion
import com.geinzz.geinzwork.herramientas_geinz.constantes.proms_gen_IA.promptNotificacionCita
import com.geinzz.geinzwork.herramientas_geinz.constantes.proms_gen_IA.promptNotificacionNovedad
import com.geinzz.geinzwork.herramientas_geinz.constantes.proms_gen_IA.promptNotificacionOperativa
import com.geinzz.geinzwork.herramientas_geinz.constantes.proms_gen_IA.promptNotificacionReposicion
import com.geinzz.geinzwork.herramientas_geinz.constantes.proms_gen_IA.promptNotificacionServicios
import com.geinzz.geinzwork.herramientas_geinz.constantes.proms_gen_IA.promptNotificacionUrgencia
import com.geinzz.geinzwork.herramientas_geinz.constantes.proms_gen_IA.promptNotificacionVenta
import com.geinzz.geinzwork.herramientas_geinz.constantes.proms_gen_IA.generarPromptPromoAtencion
import com.geinzz.geinzwork.herramientas_geinz.constantes.proms_gen_IA.generarPromptPromoInformativo
import com.geinzz.geinzwork.herramientas_geinz.constantes.proms_gen_IA.generarPromptPromoVenta
import com.geinzz.geinzwork.herramientas_geinz.constantes.proms_gen_IA.promptNotificacionVenta
import com.geinzz.geinzwork.ui.adapters.ui.pantallas.socios.acortarDescripcionNotificacion

import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.GenerativeBackend
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.storage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.time.LocalDate
import java.time.Period
import java.time.format.DateTimeFormatter
import java.util.UUID

class repo_pantallas_promocionar {

    val db = FirebaseFirestore.getInstance()

    enum class TipoGeneracionIA(
        val tituloUI: String,
        val icono: String
    ) {
        VENTA(
            tituloUI = "Venta",
            icono = "🛒"
        ),
        ATENCION(
            tituloUI = "Llamado de atención",
            icono = "✨"
        ),
        INFORMATIVO(
            tituloUI = "Informativo",
            icono = "🏢"
        ),

        URGENCIA(
            tituloUI = "Urgencia",
            icono = "⏰"
        ),

        NOVEDAD(
            tituloUI = "Novedad",
            icono = "🆕"
        ),

        OPERATIVA(
            tituloUI = "Operativa",
            icono = "⚠️" // Cambios de última hora, cierres inesperados
        ),
        REPOSICION(
            tituloUI = "Reposición",
            icono = "📦" // Nuevos productos o reposición de stock
        ),
        CITAS(
            tituloUI = "Citas",
            icono = "📅" // Recordatorio de citas o reservas
        ),
        SERVICIOS(
            tituloUI = "Servicios",
            icono = "🛠️" // Información sobre servicios, cambios o novedades
        ),


    }


    fun generarPromptSegunTipo(
        tipo: TipoGeneracionIA,
        tituloUsuario: String,
        descripcionUsuario: String,
        nombreTienda: String,
        localidad: String
    ): String {
        return when (tipo) {
            TipoGeneracionIA.VENTA -> generarPromptPromoVenta(
                tituloUsuario, descripcionUsuario, nombreTienda, localidad
            )

            TipoGeneracionIA.ATENCION -> generarPromptPromoAtencion(
                tituloUsuario, descripcionUsuario, nombreTienda, localidad
            )

            TipoGeneracionIA.INFORMATIVO -> generarPromptPromoInformativo(
                tituloUsuario, descripcionUsuario, nombreTienda, localidad
            )

            TipoGeneracionIA.URGENCIA -> {
                generarPromptPromoInformativo(
                    tituloUsuario, descripcionUsuario, nombreTienda, localidad
                )

            }

            TipoGeneracionIA.NOVEDAD -> {
                generarPromptPromoInformativo(
                    tituloUsuario, descripcionUsuario, nombreTienda, localidad
                )

            }

            TipoGeneracionIA.OPERATIVA -> {
                generarPromptPromoInformativo(
                    tituloUsuario, descripcionUsuario, nombreTienda, localidad
                )

            }

            TipoGeneracionIA.REPOSICION -> {
                generarPromptPromoInformativo(
                    tituloUsuario, descripcionUsuario, nombreTienda, localidad
                )

            }

            TipoGeneracionIA.CITAS -> {
                generarPromptPromoInformativo(
                    tituloUsuario, descripcionUsuario, nombreTienda, localidad
                )

            }

            TipoGeneracionIA.SERVICIOS -> {
                generarPromptPromoInformativo(
                    tituloUsuario, descripcionUsuario, nombreTienda, localidad
                )

            }
        }
    }


    suspend fun generar_promociones_con_IA(
        tipo_generacion: TipoGeneracionIA,
        tituloUsuario: String,
        descripcionUsuario: String,
        nombreTienda: String,
        localidad: String,
    ): List<OpcionPromocionIA> {

        return try {
            val model = Firebase.ai(
                backend = GenerativeBackend.googleAI()
            ).generativeModel("gemini-2.5-flash")

            val prompt = generarPromptSegunTipo(
                tipo = tipo_generacion,
                tituloUsuario = tituloUsuario,
                descripcionUsuario = descripcionUsuario,
                nombreTienda = nombreTienda,
                localidad = localidad
            )

            val result = model.generateContent(prompt)
            val texto = result.text ?: return emptyList()

            parsearOpcionesIA(tipo_generacion, texto)

        } catch (e: Exception) {
            Log.e("IA", "Error IA promociones: ${e.message}")
            emptyList()
        }
    }


    suspend fun subirImgNotificacionTemporal(
        context: Context, uri: Uri, idTemporal: String, idTienda: String
    ): Result<String> {
        return try {
            val storageRef =
                Firebase.storage.reference.child("tiendas/$idTienda/imagenes/notificaciones/$idTemporal")

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
            val storageRef =
                Firebase.storage.reference.child("tiendas/$idTienda/imagenes/notificaciones/$idTemporal")
            storageRef.delete().await()

            // 2️⃣ Eliminar ID o URL de Firestore
            val docRef = FirebaseFirestore.getInstance().collection("Tiendas").document(idTienda)
                .collection("notificaciones_temporales").document(idTemporal)
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
        tipoGeneracion: TipoGeneracionIA,  // <-- nuevo
        onResultado: (NotificacionIA) -> Unit
    ) {

        val model = Firebase.ai(
            backend = GenerativeBackend.googleAI()
        ).generativeModel("gemini-2.5-flash")

        val descripcion_acortada = acortarDescripcionNotificacion(descCorta)

        try {
            // Elegimos el prompt según el tipo
            val prompt = when (tipoGeneracion) {
                TipoGeneracionIA.VENTA -> promptNotificacionVenta(
                    tituloPublicacion,
                    descripcion_acortada
                )

                TipoGeneracionIA.ATENCION -> promptNotificacionAtencion(
                    tituloPublicacion,
                    descripcion_acortada
                )

                TipoGeneracionIA.URGENCIA -> promptNotificacionUrgencia(
                    tituloPublicacion,
                    descripcion_acortada
                )

                TipoGeneracionIA.NOVEDAD -> promptNotificacionNovedad(
                    tituloPublicacion,
                    descripcion_acortada
                )

                TipoGeneracionIA.INFORMATIVO -> promptNotificacionAtencion(
                    tituloPublicacion,
                    descripcion_acortada
                )

                TipoGeneracionIA.OPERATIVA -> promptNotificacionOperativa(
                    tituloPublicacion,
                    descripcion_acortada
                )

                TipoGeneracionIA.REPOSICION -> promptNotificacionReposicion(
                    tituloPublicacion,
                    descripcion_acortada
                )

                TipoGeneracionIA.CITAS -> promptNotificacionCita(
                    tituloPublicacion,
                    descripcion_acortada
                )

                TipoGeneracionIA.SERVICIOS -> promptNotificacionServicios(
                    tituloPublicacion,
                    descripcion_acortada
                )
            }

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
        titulo_publicacion: String, descripcion: String, onResultado: (String) -> Unit
    ) {
        val model = Firebase.ai(
            backend = GenerativeBackend.googleAI()
        ).generativeModel("gemini-2.5-flash")

        val descripcionAcortada = acortarDescripcionNotificacion(descripcion)

        try {
            val prompt = generarPromptWhatsAppContacto(
                titulo_publicacion, descripcionAcortada
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
        titulo_publicacion: String, descripcion: String, onResultado: (String) -> Unit
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
                    tituloPublicacion, descCorta, nombreTienda, localidad, diasRestantes
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


    fun parsearOpcionesIA(
        tipo: TipoGeneracionIA,
        texto: String
    ): List<OpcionPromocionIA> {

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
                    tipoIA = tipo,          // 👈 AQUÍ está la magia
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
                linea.startsWith("T:") -> titulo = linea.removePrefix("T:").trim()

                linea.startsWith("D:") -> descripcion = linea.removePrefix("D:").trim()
            }
        }

        return NotificacionIA(
            titulo = titulo, descripcion = descripcion
        )
    }


    enum class EventoNotificacion(val key: String) {
        VISTA("vista"),
        CLICK("click"),
        CLICK_PERFIL("click_perfil"),
        CLICK_ANUNCIO("click_anuncio"),
        CLICK_WHATSAPP("click_whatsapp"),
        TIEMPO_ANUNCIO("tiempo_anuncio"),
        CERRAR_ANUNCIO("cerrar_anuncio")
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun registrarEventoNotificacion(
        localidadTienda: String,
        idTienda: String,
        idPromo: String,
        idUser: String,
        evento: EventoNotificacion,
        valor: Long? = null
    ) {
        val db = FirebaseFirestore.getInstance()

        // Documento base de la promo
        val promoRef = db.collection("Tiendas")
            .document(localidadTienda)
            .collection(localidadTienda)
            .document(idTienda)
            .collection("notificaciones_enviadas")
            .document(idPromo)

        val eventoDoc = promoRef.collection("eventos").document(evento.key)
        val hoy = LocalDate.now().toString()

        // 1️⃣ Guardar tiempo individual por usuario
        if (valor != null) {
            val usuarioRef = eventoDoc.collection("usuarios").document(idUser)
                .collection("por_dia").document(hoy)

            usuarioRef.update("totalSegundos", FieldValue.increment(valor))
                .addOnFailureListener {
                    usuarioRef.set(mapOf("totalSegundos" to valor))
                }
        }

        // 2️⃣ TOTAL GLOBAL
        incrementar(eventoDoc) // sigue sumando eventos globales

        // 3️⃣ POR DÍA GLOBAL
        incrementar(eventoDoc.collection("por_dia").document(hoy))

        // 4️⃣ Alcance único
        if (evento == EventoNotificacion.VISTA) {
            registrarAlcanceUnico(eventoDoc, idUser)
        }

        // 5️⃣ Demografía
        if (evento != EventoNotificacion.CERRAR_ANUNCIO) {
            registrarDemografiaEvento(eventoDoc, idUser)
        }
    }


    fun registrarDemografiaEvento(
        eventoDoc: DocumentReference,
        idUser: String
    ) {
        obtenerDatosUsuario(idUser) { datos ->
            if (datos == null) return@obtenerDatosUsuario

            // 📍 LOCALIDAD
            if (datos.localidad.isNotEmpty()) {
                incrementar(
                    eventoDoc.collection("localidad")
                        .document(datos.localidad)
                )
            }

            // 👤 GÉNERO
            if (datos.genero.isNotEmpty()) {
                incrementar(
                    eventoDoc.collection("genero")
                        .document(datos.genero.lowercase())
                )
            }

            // 🎂 EDAD
            incrementar(
                eventoDoc.collection("edad")
                    .document(obtenerRangoEdad(datos.edad))
            )
        }
    }


    fun incrementar(ref: DocumentReference) {
        ref.update("total", FieldValue.increment(1))
            .addOnFailureListener {
                ref.set(mapOf("total" to 1))
            }
    }


    fun registrarAlcanceUnico(
        eventoDoc: DocumentReference,
        idUser: String
    ) {
        val ref = eventoDoc
            .collection("alcance_users")
            .document(idUser)

        ref.get().addOnSuccessListener { doc ->
            if (!doc.exists()) {
                ref.set(mapOf("ts" to FieldValue.serverTimestamp()))
                incrementar(
                    eventoDoc.collection("alcance")
                        .document("total")
                )
            }
        }
    }

    object MedidorTiempoAnuncio {
        private var inicio: Long = 0L
        private var cerrado = false

        fun iniciar() {
            inicio = System.currentTimeMillis()
            cerrado = false
        }

        fun finalizarUnaVez(): Long? {
            if (cerrado) return null
            cerrado = true
            return ((System.currentTimeMillis() - inicio) / 1000).coerceAtLeast(1)
        }
    }


    fun obtenerRangoEdad(edad: Int): String {
        return when (edad) {
            in 18..25 -> "18-25"
            in 26..35 -> "26-35"
            in 36..45 -> "36-45"
            else -> "otro"
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun obtenerDatosUsuario(
        idUser: String,
        onResult: (DatosDemograficosUsuario?) -> Unit
    ) {
        db.collection("Trabajadores_Usuarios_Drivers")
            .document("users")
            .collection("users")
            .document(idUser)
            .get()
            .addOnSuccessListener { snapshot ->

                if (!snapshot.exists()) {
                    onResult(null)
                    return@addOnSuccessListener
                }

                val data = snapshot.data ?: run {
                    onResult(null)
                    return@addOnSuccessListener
                }

                val fechaNacString = data["fecha_nac"] as? String

                val edad = try {
                    fechaNacString?.let {
                        val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
                        val fechaNac = LocalDate.parse(it, formatter)
                        Period.between(fechaNac, LocalDate.now()).years
                    } ?: 0
                } catch (e: Exception) {
                    0 // 🔒 nunca crashea
                }

                onResult(
                    DatosDemograficosUsuario(
                        localidad = data["localida"] as? String ?: "",
                        nacionalidad = data["nacionalidad_nacimiento"] as? String ?: "",
                        genero = data["genero"] as? String ?: "",
                        edad = edad
                    )
                )
            }
            .addOnFailureListener {
                onResult(null)
            }
    }

}



