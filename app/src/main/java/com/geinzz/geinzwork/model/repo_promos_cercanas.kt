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

            // 🔹 Creamos lista de tiendas con más de una promo
            val listaTiendasConMasDeUnaPromo = promosPorTienda.filter { it.value.size > 1 }
                .map { (idTienda, promos) ->
                    val primerElemento = promos.first()
                    tiendas_con_mas_de_una_promo(
                        id = idTienda,
                        nombre_tienda = primerElemento.second,
                        logo_img = primerElemento.third
                    )
                }

            // 🔹 Ahora mapeamos cada promo como antes, pero agregamos la lista de tiendas con más de una promo
            snapshot.documents.mapNotNull { doc ->
                val infoMap = doc.get("informacion") as? Map<*, *> ?: emptyMap<String, Any>()
                val imgMap = doc.get("img_container") as? Map<*, *> ?: emptyMap<String, Any>()
                val fechas = doc.get("fechas") as? Map<*, *> ?: emptyMap<String, Any>()
                val fecha_fin = fechas["fin"] as? String ?: ""

                val informacion = informacion_publcacion(
                    descripcion = infoMap["descripcion"] as? String ?: "",
                    numero = infoMap["numero"] as? String ?: "",
                    titulo = infoMap["titulo"] as? String ?: "",
                    nombre_tienda = infoMap["nombre_tienda"] as? String ?: "",
                    id_promocion = doc.id,
                    id_tienda = infoMap["id_tienda"] as? String ?: "",
                    categoria = infoMap["categoria"] as? String ?: "",
                    compartir=infoMap["compartir"] as? Boolean ?: false,
                    contactar=infoMap["contactar"] as? Boolean ?: false,
                )

                val img = img_content(
                    logo_img = imgMap["logo_img"] as? String ?: "",
                    lista_img = imgMap["lista_img"] as? List<String> ?: emptyList()
                )

                val promo = dataclass_promociones_cerca_de_ti(
                    informacion_publcacion = informacion,
                    img = img,
                    exclussivo = doc.getBoolean("exclusivo") ?: false,
                    dias_restantes = diasRestantes(fecha_fin)
                )

                val listaFiltrado = listOfNotNull(informacion.categoria)

                obj_completo(
                    dataclass_promociones_cerca_de_ti = promo,
                    lista_filtrado = listaFiltrado,
                    lista_tiendas_con_mas_promo = listaTiendasConMasDeUnaPromo
                )
            }.shuffled()

        } catch (e: Exception) {
            emptyList()
        }
    }


    @RequiresApi(Build.VERSION_CODES.O)
    fun diasRestantes(fechaFin: String): Int {
        return try {
            if (fechaFin.isBlank()) return 0

            val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
            val hoy = LocalDate.now()
            val fin = LocalDate.parse(fechaFin, formatter)

            ChronoUnit.DAYS.between(hoy, fin).coerceAtLeast(0L).toInt()
        } catch (e: Exception) {
            0
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