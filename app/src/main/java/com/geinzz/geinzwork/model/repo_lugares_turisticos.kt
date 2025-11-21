package com.geinzz.geinzwork.model

import android.util.Log
import com.firebase.geofire.GeoFireUtils
import com.firebase.geofire.GeoLocation
import com.geinzz.geinzwork.data.model.localizate_geinz.filtrado_tiendas.horario_tienda
import com.geinzz.geinzwork.data.model.localizate_geinz.filtrado_tiendas.lugares_cercanos
import com.geinzz.geinzwork.data.model.localizate_geinz.inicio_geinz.lugares_turisticos
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.constantes_horas.obtenerProximoDiaAbierto
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.constantes_lista_localidades.toMetodoContacto
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.constantes_lista_localidades.to_horario_atencion_box_dia
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.constantes_lista_localidades.to_metodo_pago
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.constantes_lista_localidades.verificarSiEstaAbiertoHoy
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.util.Calendar

class repo_lugares_turisticos {
    val db = FirebaseFirestore.getInstance()


    suspend fun obtener_lugares_turisticos(localidad: String): List<lugares_turisticos> {
        Log.d("localida_pasada", localidad)
        val lista_lugares = mutableListOf<lugares_turisticos>()
        val lugares_turisticos =
            db.collection("Tiendas").document(localidad.lowercase())
                .collection("lugares_turisticos")
                .get().await()
        for (datos in lugares_turisticos) {
            val data = datos.data
            val id = data?.get("id") as? String ?: ""
            val titulo = data?.get("titulo") as? String ?: ""
            val descripcion = data?.get("descripcion") as? String ?: ""
            val img_refencia = data?.get("img") as? Map<String, Any> ?: emptyMap()
            val lista_img_ref = img_refencia?.get("lista_img") as? List<String> ?: emptyList()
            val img_principal = img_refencia?.get("principal") as? String ?: ""
            val ubicacion = data?.get("ubicacion") as? Map<String, Any> ?: emptyMap()
            val dirección = ubicacion?.get("dirección") as? String ?: ""
            val referencia = ubicacion?.get("referencia") as? String ?: ""
            val longitud = ubicacion?.get("longitud") as? Number ?: 0
            val latitud = ubicacion?.get("latitud") as? Number ?: 0
            val lista_categorias = data?.get("categoria") as? List<String> ?: emptyList()

            val lista = lugares_turisticos(
                id,
                titulo,
                descripcion,
                lista_img_ref, img_principal,
                dirección,
                referencia,
                latitud.toDouble(),
                longitud.toDouble(), lista_categorias
            )
            lista_lugares.add(lista)
        }

        return lista_lugares
    }

    suspend fun get_lugar_turistico(localidad: String, id: String): lugares_turisticos {
        val docSnapshot = db.collection("Tiendas")
            .document(localidad.lowercase())
            .collection("lugares_turisticos")
            .document(id)
            .get()
            .await()

        if (!docSnapshot.exists()) lugares_turisticos()

        val data = docSnapshot.data
        val id = data?.get("id") as? String ?: ""
        val titulo = data?.get("titulo") as? String ?: ""
        val descripcion = data?.get("descripcion") as? String ?: ""
        val img_refencia = data?.get("img") as? Map<String, Any> ?: emptyMap()
        val lista_img_ref = img_refencia?.get("lista_img") as? List<String> ?: emptyList()
        val img_principal = img_refencia?.get("principal") as? String ?: ""
        val ubicacion = data?.get("ubicacion") as? Map<String, Any> ?: emptyMap()
        val dirección = ubicacion?.get("dirección") as? String ?: ""
        val referencia = ubicacion?.get("referencia") as? String ?: ""
        val longitud = ubicacion?.get("longitud") as? Number ?: 0
        val latitud = ubicacion?.get("latitud") as? Number ?: 0
        val lista_categorias = data?.get("categoria") as? List<String> ?: emptyList()


        // ✅ Retornamos el objeto completo
        return lugares_turisticos(
            id_lugar_turistico = id,
            titulo = titulo,
            descripcion = descripcion,
            lista_img = lista_img_ref,
            img_principal = img_principal,
            direcccion = dirección,
            referencia = referencia,
            latitud = latitud.toDouble(),
            longitud = longitud.toDouble(),
            subcategoria_filtrado = lista_categorias
        )
    }


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


    fun obtenerTiendasCercanas(
        lat: Double,
        lon: Double,
        radioKm: Double,
        localidad: String,
        callback: (List<lugares_cercanos>, categorias: List<String>) -> Unit
    ) {
        val db = FirebaseFirestore.getInstance()
        val center = GeoLocation(lat, lon)
        val radiusInM = radioKm * 1000
        val bounds = GeoFireUtils.getGeoHashQueryBounds(center, radiusInM)
        val tasks = mutableListOf<Task<*>>()
        val tiendas = mutableListOf<lugares_cercanos>()
        val categoriasSet = mutableSetOf<String>()
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
                    val pagado = doc.get("pagado") as? Boolean ?: false
                    val latitud = ubicacion["latitud"] as? Number ?: 0
                    val longitud = ubicacion["longitud"] as? Number ?: 0
                    val direccion = ubicacion["dirección"] as? String?:""
                    val referencia = ubicacion["referencia"] as? String?:""
                    val descripcion = doc["descripcion"] as? String?:""

                    val categoria_tienda = doc.getString("categoria_tienda") ?: ""
                    val horario_dia = doc.get("horario_atencion") as? Map<String, Any> ?: emptyMap()
                    if (categoria_tienda.isNotEmpty()) {
                        categoriasSet.add(categoria_tienda) // 🔹 Guardamos la categoría
                    }

                    val dias =
                        listOf(
                            "domingo",
                            "lunes",
                            "martes",
                            "miércoles",
                            "jueves",
                            "viernes",
                            "sábado"
                        )
                    val calendar = Calendar.getInstance()
                    val diaActual = dias[calendar.get(Calendar.DAY_OF_WEEK) - 1]

                    val horarioDia = horario_dia[diaActual] as? Map<String, Any> ?: emptyMap()
                    val cerrado = horarioDia["cerrado"] as? Boolean ?: false
                    val hApertura = horarioDia["h_apertura"] as? String ?: ""
                    val hCierre = horarioDia["h_cierre"] as? String ?: ""
                    val motivo = horarioDia["motivo"] as? String ?: ""
                    val metodos_contacto =
                        doc.get("metodo_contacto") as? Map<String, Any> ?: emptyMap()
                    val contacto_obs = metodos_contacto.toMetodoContacto()
                    val metodo_pago = doc.get("metodos_pago") as? Map<String, Any> ?: emptyMap()
                    val metodo_pago_separado = metodo_pago.to_metodo_pago()
                    val horario_box_mapeo=horario_dia.to_horario_atencion_box_dia()
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

                    if (distancia <= radiusInM && pagado) {
                        Log.d("geoquery", "✅ ${doc.id} dentro del radio (${distancia.toInt()} m)")
                        // ✅ Filtrar por categoría seleccionada
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
                                longitud = longitud.toDouble(),
                                esta_abierto = estaAbierto,
                                contacto_tienda = contacto_obs,
                                has_tienda = geohash,
                                direccion = direccion, referencia = referencia,
                                descripcion = descripcion,
                                metodos_pago_tienda = metodo_pago_separado,horario_box_mapeo
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

        Tasks.whenAllComplete(tasks)
            .addOnSuccessListener {
                Log.d("geoquery", "🎯 Total tiendas encontradas: ${tiendas.size}")
                callback(tiendas, categoriasSet.toList())
            }
    }


}