package com.geinzz.geinzwork.model

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.geinzz.geinzwork.data.model.DatosDemograficosUsuario
import com.geinzz.geinzwork.data.model.EstadisticasPromo
import com.geinzz.geinzwork.data.model.data_class_promo_cerca_de_ti.dataclass_promociones_cerca_de_ti
import com.geinzz.geinzwork.data.model.data_class_promo_cerca_de_ti.estadisticas_publiccaciones
import com.geinzz.geinzwork.data.model.data_class_promo_cerca_de_ti.img_content
import com.geinzz.geinzwork.data.model.data_class_promo_cerca_de_ti.informacion_publcacion
import com.geinzz.geinzwork.data.model.data_class_promo_cerca_de_ti.obj_completo
import com.geinzz.geinzwork.data.model.data_class_promo_cerca_de_ti.tiendas_con_mas_de_una_promo
import com.geinzz.geinzwork.data.model.mensaje_predeterminado
import com.geinzz.geinzwork.data.model.msjes_predeteminados_generales
import com.google.firebase.Firebase
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.Period
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import kotlin.math.ceil
import kotlin.math.floor

class repo_promos_cercanas {
    val db = FirebaseFirestore.getInstance()

    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun obtener_promos(
        localidad: String
    ): List<obj_completo> {
        return try {
            val snapshot = db
                .collection("Tiendas")
                .document(localidad)
                .collection("promos_ofertas")
                .get()
                .await()


            // 🔹 Primero agrupamos todas las promos por idTienda
            val promosPorTienda = snapshot.documents.mapNotNull { doc ->
                val infoMap = doc.get("informacion") as? Map<*, *> ?: emptyMap<String, Any>()
                val imgMap = doc.get("img_container") as? Map<*, *> ?: emptyMap<String, Any>()

                val idTienda = infoMap["id_tienda"] as? String ?: return@mapNotNull null
                val nombreTienda = infoMap["nombre_tienda"] as? String ?: ""
                val logo = imgMap["logo_img"] as? String ?: ""

                Triple(idTienda, nombreTienda, logo)
            }.groupBy { it.first } // Agrupamos por idTienda

            // 🔹 Lista de tiendas con más de una promo
            val listaTiendasConMasDeUnaPromo = promosPorTienda.filter { it.value.size > 1 }
                .map { (idTienda, promos) ->
                    val primerElemento = promos.first()
                    tiendas_con_mas_de_una_promo(
                        id = idTienda,
                        nombre_tienda = primerElemento.second,
                        logo_img = primerElemento.third
                    )
                }

            // 🔹 Mapear cada promo
            snapshot.documents.mapNotNull { doc ->
                val estado = doc.getString("estado") ?: "expirado"
                if (estado != "activo") return@mapNotNull null
                val infoMap = doc.get("informacion") as? Map<*, *> ?: emptyMap<String, Any>()
                val imgMap = doc.get("img_container") as? Map<*, *> ?: emptyMap<String, Any>()
                val tipo_hora_dias = doc.get("tipo_hora_dias") as? String ?: ""
                val mensaje_predeterminado = doc.get("mensaje_predeterminado") as? Map<*, *> ?: emptyMap<String, Any>()
                val compartir= mensaje_predeterminado.get("compartir") as? Map<*, *> ?: emptyMap<String, Any>()
                val whatsapp = mensaje_predeterminado.get("whatsapp") as? Map<*, *> ?: emptyMap<String, Any>()
                val msje_compartir= compartir.get("msje_predermindo") as? String ?:""
                val msje_whatsapp = whatsapp.get("msje_predermindo") as? String ?:""


                val datos_hora_fecha = doc.get("datos_hora_fecha") as? Map<*, *> ?: emptyMap<String, Any>()
                val horas = datos_hora_fecha["horas"] as? Map<*, *> ?: emptyMap<String, Any>()
                val fechas = datos_hora_fecha["dias"] as? Map<*, *> ?: emptyMap<String, Any>()

                // 🔹 Obtenemos el timestamp final según tipo (horas o días)
                val timestampFin = when (tipo_hora_dias) {
                    "horas" -> (horas["timestamp_fin"] as? Number)?.toLong() ?: 0L
                    "dias"  -> (fechas["timestamp_fin"] as? Number)?.toLong() ?: 0L
                    else    -> 0L
                }

                // 🔹 Ajuste a milisegundos si estuviera en segundos
                val timestampFinMs = if (timestampFin < 1000000000000L) timestampFin * 1000 else timestampFin

                // 🔹 Logs para debug
                Log.d("DEBUG_PROMO", "PROMO ID=${doc.id} TIPO=$tipo_hora_dias")
                Log.d("DEBUG_PROMO", "timestampFin original = $timestampFin")
                Log.d("DEBUG_PROMO", "timestampFin ajustado (ms) = $timestampFinMs")
                val ahora = System.currentTimeMillis()
                Log.d("DEBUG_PROMO", "Ahora = $ahora")
                val diff = timestampFinMs - ahora
                Log.d("DEBUG_PROMO", "Diff = $diff")

                val tiempoRestanteString = tiempoRestante(timestampFinMs)
                Log.d("DEBUG_PROMO", "tiempoRestanteString = $tiempoRestanteString")

                val informacion = informacion_publcacion(
                    descripcion = infoMap["descripcion"] as? String ?: "",
                    numero = infoMap["numero"] as? String ?: "",
                    titulo = infoMap["titulo"] as? String ?: "",
                    nombre_tienda = infoMap["nombre_tienda"] as? String ?: "",
                    id_promocion = doc.id,
                    id_tienda = infoMap["id_tienda"] as? String ?: "",
                    categoria = infoMap["categoria"] as? String ?: "",
                    compartir = infoMap["compartir"] as? Boolean ?: false,
                    contactar = infoMap["contactar"] as? Boolean ?: false,
                    msjes_predeteminados_generales=msjes_predeteminados_generales(
                        compartir = mensaje_predeterminado(
                            msje_predermindo = msje_compartir,
                            activo_o_no = true
                        ),
                        whatsapp =mensaje_predeterminado(
                            msje_predermindo = msje_whatsapp,
                            activo_o_no = true
                        )
                    )
                )

                val img = img_content(
                    logo_img = imgMap["logo_img"] as? String ?: "",
                    lista_img = imgMap["lista_img"] as? List<String> ?: emptyList()
                )

                val promo = dataclass_promociones_cerca_de_ti(
                    informacion_publcacion = informacion,
                    img = img,
                    exclussivo = doc.getBoolean("exclusivo") ?: false,
                    dias_restantes = tiempoRestanteString,
                    estadisticas = estadisticas_publiccaciones(),
                    texto_msje_whatsapp=msjes_predeteminados_generales(
                        compartir = mensaje_predeterminado(
                            msje_predermindo = msje_compartir,
                            activo_o_no = true
                        ),
                        whatsapp =mensaje_predeterminado(
                            msje_predermindo = msje_whatsapp,
                            activo_o_no = true
                        )
                    )
                )

                val listaFiltrado = listOfNotNull(informacion.categoria)

                obj_completo(
                    dataclass_promociones_cerca_de_ti = promo,
                    lista_filtrado = listaFiltrado,
                    lista_tiendas_con_mas_promo = listaTiendasConMasDeUnaPromo
                )
            }.shuffled()

        } catch (e: Exception) {
            Log.e("ERROR_PROMO", "Error al obtener promociones: ${e.message}")
            emptyList()
        }
    }


    suspend fun obtener_estadisticas(
        localidad: String,
        id_promo: String
    ): EstadisticasPromo {

        val statsRef = db
            .collection("Tiendas")
            .document(localidad)
            .collection("promos_ofertas")
            .document(id_promo)
            .collection("estadisticas")

        val compartidos = statsRef.document("compartidos")
            .get().await()
            .getLong("total")?.toInt() ?: 0

        val whatsapp = statsRef.document("whatsapp")
            .get().await()
            .getLong("total")?.toInt() ?: 0

        return EstadisticasPromo(
            vistas = 0,
            compartidos = compartidos,
            whatsapp = whatsapp
        )
    }



    fun tiempoRestante(timestampFin: Long): String {
        val ahoraMs = System.currentTimeMillis()
        val diffMs = timestampFin - ahoraMs

        if (diffMs <= 0) return "Expirado"

        val totalHoras = diffMs.toDouble() / (1000 * 60 * 60)
        val totalMinutos = diffMs.toDouble() / (1000 * 60)

        return if (totalHoras >= 24) {
            // Mostrar días completos restantes
            val dias = ceil(totalHoras / 24).toLong()  // +1 implícito para el día actual
            "$dias ${if (dias == 1L) "día" else "días"} restantes"
        } else {
            // Mostrar horas y minutos restantes
            val horas = floor(totalHoras).toLong()
            val minutos = floor(totalMinutos % 60).toLong()
            when {
                horas > 0 && minutos > 0 -> "$horas ${if (horas == 1L) "hora" else "horas"} y $minutos ${if (minutos == 1L) "minuto" else "minutos"} restantes"
                horas > 0 -> "$horas ${if (horas == 1L) "hora" else "horas"} restantes"
                minutos > 0 -> "$minutos ${if (minutos == 1L) "minuto" else "minutos"} restantes"
                else -> "Menos de un minuto restante"
            }
        }
    }


    @RequiresApi(Build.VERSION_CODES.O)
    fun agregar_contador_estadisticas_publicacion(
        tipoEvento: String,
        id_promo: String,
        localidad: String,
        idUser: String
    ) {
        val db = FirebaseFirestore.getInstance()

        val estadisticaBase = db
            .collection("Tiendas").document(localidad)
            .collection("promos_ofertas").document(id_promo)
            .collection("estadisticas")
            .document(tipoEvento)

        val hoy = LocalDate.now().toString()

        // 1️⃣ TOTAL (todos los eventos)
        incrementar(estadisticaBase)

        // 2️⃣ POR DÍA (todos los eventos)
        incrementar(
            estadisticaBase
                .collection("por_dia")
                .document(hoy)
        )

        // 3️⃣ ALCANCE (solo vistas)
        if (tipoEvento == "vista") {
            registrarAlcanceUnico(
                estadisticaBase = estadisticaBase,
                idUser = idUser
            )
        }

        // 4️⃣ DEMOGRAFÍA (todo menos compartir)
        if (tipoEvento != "compartir") {
            obtenerDatosUsuario(idUser) { datos ->
                if (datos == null) return@obtenerDatosUsuario

                if (datos.localidad.isNotEmpty()) {
                    incrementar(
                        estadisticaBase
                            .collection("localidad")
                            .document(datos.localidad)
                    )
                }

                if (datos.genero.isNotEmpty()) {
                    incrementar(
                        estadisticaBase
                            .collection("genero")
                            .document(datos.genero.lowercase())
                    )
                }

                incrementar(
                    estadisticaBase
                        .collection("edad")
                        .document(obtenerRangoEdad(datos.edad))
                )
            }
        }
    }


    fun registrarAlcanceUnico(
        estadisticaBase: DocumentReference,
        idUser: String
    ) {
        val alcanceUserRef = estadisticaBase
            .collection("alcance_users")
            .document(idUser)

        alcanceUserRef.get().addOnSuccessListener { doc ->
            if (!doc.exists()) {
                alcanceUserRef.set(
                    mapOf("ts" to FieldValue.serverTimestamp())
                )

                incrementar(
                    estadisticaBase
                        .collection("alcance")
                        .document("total")
                )
            }
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
    fun incrementar(ref: DocumentReference) {
        ref.update("total", FieldValue.increment(1))
            .addOnFailureListener {
                ref.set(mapOf("total" to 1))
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

}