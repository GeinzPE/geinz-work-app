package com.geinzz.geinzwork.model

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.geinzz.geinzwork.data.model.data_class_promo_cerca_de_ti.dataclass_promociones_cerca_de_ti
import com.geinzz.geinzwork.data.model.data_class_promo_cerca_de_ti.img_content
import com.geinzz.geinzwork.data.model.data_class_promo_cerca_de_ti.informacion_publcacion
import com.geinzz.geinzwork.data.model.data_class_promo_cerca_de_ti.obj_completo
import com.geinzz.geinzwork.data.model.data_class_promo_cerca_de_ti.tiendas_con_mas_de_una_promo
import com.google.firebase.Firebase
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

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
                val infoMap = doc.get("informacion") as? Map<*, *> ?: emptyMap<String, Any>()
                val imgMap = doc.get("img_container") as? Map<*, *> ?: emptyMap<String, Any>()
                val tipo_hora_dias = doc.get("tipo_hora_dias") as? String ?: ""

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
                )

                val img = img_content(
                    logo_img = imgMap["logo_img"] as? String ?: "",
                    lista_img = imgMap["lista_img"] as? List<String> ?: emptyList()
                )

                val promo = dataclass_promociones_cerca_de_ti(
                    informacion_publcacion = informacion,
                    img = img,
                    exclussivo = doc.getBoolean("exclusivo") ?: false,
                    dias_restantes = tiempoRestanteString
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


    fun tiempoRestante(timestampFin: Long): String {
        val ahora = System.currentTimeMillis()
        val diff = timestampFin - ahora

        if (diff <= 0) return "Expirado"

        val dias = diff / (1000 * 60 * 60 * 24)
        val horas = (diff / (1000 * 60 * 60)) % 24
        val minutos = (diff / (1000 * 60)) % 60

        return when {
            dias > 0 -> "$dias ${if (dias == 1L) "día" else "días"} restantes"
            horas > 0 && minutos > 0 -> "$horas ${if (horas == 1L) "hora" else "horas"} y $minutos ${if (minutos == 1L) "minuto" else "minutos"} restantes"
            horas > 0 -> "$horas ${if (horas == 1L) "hora" else "horas"} restantes"
            minutos > 0 -> "$minutos ${if (minutos == 1L) "minuto" else "minutos"} restantes"
            else -> "Menos de un minuto restante"
        }
    }


    fun agregar_contador_estadisticas_publicacion(tipo: String,id_promo: String,localidad: String){
        val db = FirebaseFirestore.getInstance()
            .collection("Tiendas").document(localidad)
            .collection("promos_ofertas").document(id_promo)
            .collection("estadisticas").document(tipo)

        db.update("total", FieldValue.increment(1))
            .addOnSuccessListener {
                Log.d("CONTADOR", "Contador actualizado correctamente")
            }
            .addOnFailureListener { e ->
                db.set(mapOf("total" to 1))
            }
    }

}