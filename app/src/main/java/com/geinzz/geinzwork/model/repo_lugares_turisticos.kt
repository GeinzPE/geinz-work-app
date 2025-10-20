package com.geinzz.geinzwork.model

import Item
import android.util.Log
import com.firebase.geofire.GeoFireUtils
import com.firebase.geofire.GeoLocation
import com.geinzz.geinzwork.data.model.localizate_geinz.filtrado_tiendas.horario_tienda
import com.geinzz.geinzwork.data.model.localizate_geinz.filtrado_tiendas.lugares_cercanos
import com.geinzz.geinzwork.data.model.localizate_geinz.inicio_geinz.lugares_turisticos
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.constantes_horas.obtenerProximoDiaAbierto
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.constantes_lista_localidades.toMetodoContacto
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.constantes_lista_localidades.verificarSiEstaAbiertoHoy

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.util.Calendar

class repo_lugares_turisticos {
    val db = FirebaseFirestore.getInstance()

    suspend fun obtener_filtrado_lugares(): List<String> {
        val lista_filtrado = mutableListOf<String>()
        val lugares = db.collection("Tiendas")
            .document("categorias")
            .collection("categorias_lugares")
            .document("categorias_lugares_turisticos")
            .get()
            .await()

        if (lugares.exists()) {
            val categoria = lugares.get("categorias") as? List<String> ?: emptyList()
            lista_filtrado.addAll(categoria)
        }

        return lista_filtrado
    }

//    suspend fun obtener_lugares_turisticos_filtrados(
//        localidad: String,
//        subcategoria: String
//    ): List<lugares_turisticos> {
//        val lugares_filtrados = db.collection("Tiendas")
//            .document(localidad)
//            .collection("lugares_turisticos")
//            .whereArrayContains("categoria", subcategoria)
//            .get()
//            .await()
//
//        return lugares_filtrados.mapNotNull { doc ->
//            try {
//                val id = doc.getString("id") ?: ""
//                val titulo = doc.getString("titulo") ?: ""
//                val descripcion = doc.getString("descripcion") ?: ""
//                val imgReferencia = doc.getString("img") ?: ""
//                val lista_categorias = doc?.get("categoria") as? List<String> ?: emptyList()
//
//
//                val ubicacion = doc.get("ubicacion") as? Map<*, *> ?: emptyMap<String, Any>()
//                val direccion = ubicacion["dirección"] as? String ?: ""
//                val referencia = ubicacion["referencia"] as? String ?: ""
//                val longitud = (ubicacion["longitud"] as? Number)?.toDouble() ?: 0.0
//                val latitud = (ubicacion["latitud"] as? Number)?.toDouble() ?: 0.0
//
//                lugares_turisticos(
//                    id_lugar_turistico = id,
//                    titulo = titulo,
//                    descripcion = descripcion,
//                    img_ref = imgReferencia,
//                    direcccion = direccion,
//                    referencia = referencia,
//                    latitud = latitud,
//                    longitud = longitud,
//                    subcategoria_filtrado = lista_categorias
//                )
//            } catch (e: Exception) {
//                null
//            }
//        }
//    }


    fun obtenerTiendasCercanas(
        lat: Double,
        lon: Double,
        radioKm: Double,
        localidad: String,
        callback: (List<lugares_cercanos>) -> Unit
    ) {
        val db = FirebaseFirestore.getInstance()
        val center = GeoLocation(lat, lon)
        val radiusInM = radioKm * 1000
        val bounds = GeoFireUtils.getGeoHashQueryBounds(center, radiusInM)
        val tasks = mutableListOf<com.google.android.gms.tasks.Task<*>>()
        val tiendas = mutableListOf<lugares_cercanos>()

        Log.d(
            "geoquery",
            "📍 Buscando tiendas cercanas a ($lat, $lon) dentro de $radioKm km [${bounds.size} rangos]"
        )

        for ((index, b) in bounds.withIndex()) {
            Log.d("geoquery", "➡️ Rango $index: start=${b.startHash}, end=${b.endHash}")

            val q = db.collection("Tiendas")
                .document(localidad)
                .collection(localidad)
                .orderBy("geohash")
                .startAt(b.startHash)
                .endAt(b.endHash)

            Log.d(
                "geoquery",
                "🧭 Consulta Firestore: /Tiendas/$localidad/$localidad ordenando por geohash"
            )

            tasks.add(q.get().addOnSuccessListener { snapshot ->
                Log.d("geoquery", "📦 ${snapshot.size()} documentos devueltos para rango $index")

                for (doc in snapshot) {
                    Log.d(
                        "geoquery",
                        "🗂️ Documento: ${doc.id} → geohash=${doc.getString("geohash")}"
                    )

                    val geohash = doc.getString("geohash") ?: ""
                    val nombre = doc.getString("nombre_tienda") ?: ""
                    val idTienda = doc.id
                    val mapImg = doc.get("img_tienda") as? Map<String, Any> ?: emptyMap()
                    val logo = mapImg["logo_tienda"] as? String ?: ""
                    val tag = doc.get("subcategoria") as? List<String> ?: emptyList()
                    val ubicacion = doc.get("ubicacion") as? Map<String, Any> ?: emptyMap()
                    val pagado=doc.get("pagado") as? Boolean?:false
                    val latitud = ubicacion["latitud"] as? Number ?: 0
                    val longitud = ubicacion["longitud"] as? Number ?: 0
                    val categoria_tienda = doc.getString("categoria_tienda") ?: ""
                    val horario_dia=doc.get("horario_atencion") as? Map<String, Any>?:emptyMap()

                    val dias =
                        listOf("domingo", "lunes", "martes", "miércoles", "jueves", "viernes", "sábado")
                    val calendar = Calendar.getInstance()
                    val diaActual = dias[calendar.get(Calendar.DAY_OF_WEEK) - 1]

                    val horarioDia = horario_dia[diaActual] as? Map<String, Any> ?: emptyMap()
                    val cerrado = horarioDia["cerrado"] as? Boolean ?: false
                    val hApertura = horarioDia["h_apertura"] as? String ?: ""
                    val hCierre = horarioDia["h_cierre"] as? String ?: ""
                    val motivo = horarioDia["motivo"] as? String ?: ""
                    val metodos_contacto = doc.get("metodo_contacto") as? Map<String, Any> ?: emptyMap()
                    val contacto_obs = metodos_contacto.toMetodoContacto()
                    var datos_horario_actual = horario_tienda(hApertura, hCierre, cerrado, motivo)
                    val estaAbierto =
                        if (!cerrado) verificarSiEstaAbiertoHoy(datos_horario_actual) else false
                    if (!estaAbierto) {
                        val proximo = obtenerProximoDiaAbierto(horario_dia, diaActual)
                        if (proximo != null) {
                            val (diaProx, horarioProx) = proximo
                            datos_horario_actual = datos_horario_actual.copy(
                                dia_prox_apertura = diaProx,
                                hora_prox_apertura = horarioProx["h_apertura"] as? String ?: ""
                            )
                        }
                    }

                    val distancia = GeoFireUtils.getDistanceBetween(
                        center,
                        GeoLocation(latitud.toDouble(), longitud.toDouble())
                    )

                    if (distancia <= radiusInM  && pagado) {
                        Log.d("geoquery", "✅ ${doc.id} dentro del radio (${distancia.toInt()} m)")
                        tiendas.add(
                            lugares_cercanos(
                                nombre_tienda = nombre,
                                logo_tienda = logo,
                                categoria = categoria_tienda,
                                lista_subcategoiras = tag,
                                id_tienda = idTienda,
                                pagado = pagado,
                                horario_dia = datos_horario_actual,
                                latitud = latitud.toDouble(),
                                longitud = longitud.toDouble(),estaAbierto,contacto_obs
                            )
                        )
                    } else {
                        Log.d("geoquery", "❌ ${doc.id} fuera del radio (${distancia.toInt()} m)")
                    }
                }
            }.addOnFailureListener { e ->
                Log.e("geoquery", "⚠️ Error al obtener documentos: ${e.message}")
            })
        }

        com.google.android.gms.tasks.Tasks.whenAllComplete(tasks)
            .addOnSuccessListener {
                Log.d("geoquery", "🎯 Total tiendas encontradas: ${tiendas.size}")
                callback(tiendas)
            }
    }

//    suspend fun obtener_lugares_cercanos(id_tienda:String,localidad:String): lugares_cercanos {
//
//        return try {
//            val ref= FirebaseFirestore.getInstance().collection("Tiendas").document(localidad).collection(localidad).document(id_tienda).get().await()
//            if (ref.exists()) {
//       val data=ref.data
//            val
//            } else {
//                null
//            }
//        }catch (e: Exception) {
//            e.printStackTrace()
//            null
//        }
//
//
//    }

//    suspend fun obtener_lugares_cercanos(id_tienda: String, localidad: String) =
//        runCatching {
//            FirebaseFirestore.getInstance()
//                .collection("Tiendas").document(localidad)
//                .collection(localidad).document(id_tienda)
//                .get().await()
//                .takeIf { it.exists() }
//                ?.toObject(lugares_cercanos::class.java)
//                ?.copy(id_tienda = id_tienda)
//        }.getOrNull()


}