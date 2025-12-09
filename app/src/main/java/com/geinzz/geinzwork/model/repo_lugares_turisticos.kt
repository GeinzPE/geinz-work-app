package com.geinzz.geinzwork.model

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
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


@RequiresApi(Build.VERSION_CODES.O)
    fun obtenerTiendasCercanas(
        lat: Double,
        lon: Double,
        radioKm: Double,
        localidad: String,
        callback: (List<lugares_cercanos>, categorias: List<String>) -> Unit
    ) {
        val db = FirebaseFirestore.getInstance()
        val center = GeoLocation(lat, lon)
        val radiusInM = (radioKm * 1000)
        val bounds = GeoFireUtils.getGeoHashQueryBounds(center, radiusInM)
        val tasks = mutableListOf<Task<*>>()
        val tiendas = mutableListOf<lugares_cercanos>()
        val categoriasSet = mutableSetOf<String>()

        Log.d("geoquery", "-----------------------------------------------")
        Log.d("geoquery", "📍 BUSCANDO TIENDAS CERCA")
        Log.d("geoquery", "Lat: $lat  Lon: $lon")
        Log.d("geoquery", "Radio: $radioKm km ($radiusInM metros)")
        Log.d("geoquery", "Localidad: $localidad")
        Log.d("geoquery", "Rangos geohash: ${bounds.size}")
        Log.d("geoquery", "-----------------------------------------------")

        for ((index, b) in bounds.withIndex()) {

            Log.d("geoquery", "➡️ RANGO $index")
            Log.d("geoquery", "   startHash=${b.startHash}")
            Log.d("geoquery", "   endHash=${b.endHash}")

            val q = db.collection("Tiendas")
                .document(localidad)
                .collection(localidad)
                .orderBy("geohash")
                .startAt(b.startHash)
                .endAt(b.endHash)

            tasks.add(
                q.get()
                    .addOnSuccessListener { snapshot ->

                        Log.d("geoquery", "📦 Documentos devueltos en rango $index: ${snapshot.size()}")

                        for (doc in snapshot) {

                            val geohash = doc.getString("geohash") ?: ""
                            val nombre = doc.getString("nombre_tienda") ?: ""
                            val idTienda = doc.id

                            val ubicacion = doc.get("ubicacion") as? Map<String, Any> ?: emptyMap()
                            val latitud = ubicacion["latitud"] as? Number ?: 0
                            val longitud = ubicacion["longitud"] as? Number ?: 0
                            val pagado = doc.get("pagado") as? Boolean ?: false

                            val categoria_tienda = doc.getString("categoria_tienda") ?: ""

                            // 🔥 DISTANCIA REAL
                            val distancia = GeoFireUtils.getDistanceBetween(
                                center,
                                GeoLocation(latitud.toDouble(), longitud.toDouble())
                            )

                            Log.d("geoquery", "-------------------------------")
                            Log.d("geoquery", "📝 TIENDA: ${doc.id}")
                            Log.d("geoquery", "Nombre: $nombre")
                            Log.d("geoquery", "Geohash: $geohash")
                            Log.d("geoquery", "Lat: $latitud  Lon: $longitud")
                            Log.d("geoquery", "Distancia: ${distancia.toInt()} m")
                            Log.d("geoquery", "Pagado: $pagado")
                            Log.d("geoquery", "Categoría: $categoria_tienda")

                            if (!pagado) {
                                Log.d("geoquery", "❌ Ignorada (NO PAGADO)")
                                continue
                            }

                            if (distancia <= radiusInM) {

                                Log.d("geoquery", "✅ DENTRO DEL RADIO")

                                if (categoria_tienda.isNotEmpty()) {
                                    categoriasSet.add(categoria_tienda)
                                    Log.d("geoquery", "   ➕ Categoría añadida: $categoria_tienda")
                                }

                                // Añadir a la lista (resto de campos completos)
                                val mapImg = doc.get("img_tienda") as? Map<String, Any> ?: emptyMap()
                                val logo = mapImg["logo_tienda"] as? String ?: ""
                                val tag = doc.get("subcategoria") as? List<String> ?: emptyList()
                                val direccion = ubicacion["dirección"] as? String ?: ""
                                val referencia = ubicacion["referencia"] as? String ?: ""
                                val descripcion = doc["descripcion"] as? String ?: ""
                                val horario_dia = doc.get("horario_atencion") as? Map<String, Any> ?: emptyMap()
                                val metodos_contacto = doc.get("metodo_contacto") as? Map<String, Any> ?: emptyMap()
                                val metodo_pago = doc.get("metodos_pago") as? Map<String, Any> ?: emptyMap()
                                val lcalidad_tienda = doc.get("localidad") as? String ?: ""

                                val contacto_obs = metodos_contacto.toMetodoContacto()
                                val metodo_pago_separado = metodo_pago.to_metodo_pago()
                                val horario_box_mapeo = horario_dia.to_horario_atencion_box_dia()

                                tiendas.add(
                                    lugares_cercanos(
                                        nombre_tienda = nombre,
                                        logo_tienda = logo,
                                        categoria = categoria_tienda,
                                        lista_subcategoiras = tag,
                                        id_tienda = idTienda,
                                        pagado = pagado,
                                        latitud = latitud.toDouble(),
                                        longitud = longitud.toDouble(),
                                        contacto_tienda = contacto_obs,
                                        has_tienda = geohash,
                                        direccion = direccion,
                                        referencia = referencia,
                                        descripcion = descripcion,
                                        metodos_pago_tienda = metodo_pago_separado,
                                        horario_box = horario_box_mapeo,
                                        localidad_tienda = lcalidad_tienda
                                    )
                                )
                            } else {
                                Log.d("geoquery", "❌ FUERA DEL RADIO")
                            }
                        }
                    }
                    .addOnFailureListener { e ->
                        Log.e("geoquery", "⚠️ Error en rango $index → ${e.message}")
                    }
            )
        }

        Tasks.whenAllComplete(tasks).addOnSuccessListener {
            Log.d("geoquery", "=======================================")
            Log.d("geoquery", "🎯 PROCESO COMPLETADO")
            Log.d("geoquery", "Tiendas encontradas DENTRO del radio: ${tiendas.size}")
            Log.d("geoquery", "Categorías finales: $categoriasSet")
            Log.d("geoquery", "=======================================")

            callback(tiendas, categoriasSet.toList())
        }
    }




}