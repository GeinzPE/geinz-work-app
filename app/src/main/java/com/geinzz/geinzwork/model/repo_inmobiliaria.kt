package com.geinzz.geinzwork.model

import android.util.Log
import com.firebase.geofire.GeoFireUtils
import com.firebase.geofire.GeoLocation
import com.firebase.geofire.core.GeoHash
import com.geinzz.geinzwork.data.model.completeta_info_inmuebles
import com.geinzz.geinzwork.data.model.dataclass_geinz_inmobiliaria_principal
import com.geinzz.geinzwork.data.model.ia_inmobiliara_tts
import com.geinzz.geinzwork.data.model.lugares_cercanos_
import com.geinzz.geinzwork.herramientas_geinz.constantes.construir_prompt_NLP_para_busqueda
import com.geinzz.geinzwork.herramientas_geinz.constantes.contruir_promp_ia_datos_inmobiliara
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.constantes_horas.calcularDistanciaKm
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.constantes_lista_localidades
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.GenerativeBackend
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.tasks.await
import kotlin.collections.emptyList
import kotlin.math.cos
import kotlin.math.sqrt
import kotlin.math.atan2
import kotlin.math.sin
import kotlin.math.pow

class repo_inmobiliaria {
    private val db = FirebaseFirestore.getInstance()

    suspend fun obtener_inmuebles(
        localidad: String,
        lastDoc: DocumentSnapshot? = null
    ): Pair<List<dataclass_geinz_inmobiliaria_principal>, DocumentSnapshot?> {

        Log.d("INMUEBLES", "Buscando inmuebles en localidad: $localidad")

        var query = db.collection("Tiendas")
            .document(localidad)
            .collection("geinz_inmobiliaria")
            .limit(20)

        if (lastDoc != null) {
            Log.d("INMUEBLES", "Usando paginación desde: ${lastDoc.id}")
            query = query.startAfter(lastDoc)
        }

        val snapshot = query.get().await()

        Log.d("INMUEBLES", "Documentos obtenidos: ${snapshot.size()}")

        val lista = snapshot.documents.map { datos ->

            Log.d("INMUEBLES_DOC", "DocID: ${datos.id} -> ${datos.data}")

            val inmueble = dataclass_geinz_inmobiliaria_principal(
                id = datos.getString("id") ?: "",
                lista_img = datos.get("listaImg") as? List<String> ?: emptyList(),
                nombre_inmobiliara = datos.getString("nombre") ?: "",
                descripcion = datos.getString("descripcion") ?: "",
                precio_String = datos.getDouble("precioid") ?: 0.0,
                localidad = datos.getString("ciudad") ?: "",
                tipo_propieda = datos.getString("tipoPropiedad") ?: "",
                cantidad_banos = datos.getString("banos") ?: "0",
                metros_cuadrados = datos.getDouble("metros") ?: 0.0,
                cantidad_dormitrios = datos.getString("habitaciones") ?: "0",
                cantidad_cochera = datos.getString("estacionamientos") ?: "0",
                trato = datos.getString("tipoOperacion") ?: "",
            )

            Log.d(
                "INMUEBLE_MAP",
                "ID:${inmueble.id} | baños:${inmueble.cantidad_banos} | dorm:${inmueble.cantidad_dormitrios} | cochera:${inmueble.cantidad_cochera} | metros:${inmueble.metros_cuadrados}"
            )

            inmueble
        }

        val lastVisible = snapshot.documents.lastOrNull()

        Log.d("INMUEBLES", "Último documento para paginación: ${lastVisible?.id}")

        return Pair(lista, lastVisible)
    }

    suspend fun obtner_datos_completos_del_inmueble(
        id: String,
        localidad: String
    ): completeta_info_inmuebles = coroutineScope {

        val doc = db.collection("Tiendas")
            .document(localidad)
            .collection("geinz_inmobiliaria")
            .document(id)
            .get()
            .await()

        val inmueble = doc.toObject(completeta_info_inmuebles::class.java)
            ?: return@coroutineScope completeta_info_inmuebles()

        val lat = inmueble.lat
        val lng = inmueble.lng

        val lugaresCercanosDeferred = async {
            obtener_cantidad_lugares_cercanos(lat, lng, localidad)
        }

        val lugaresSegurosDeferred = async {
            obtner_lugares_seguros_cerca(lat, lng, localidad)
        }

        val turismoDeferred = async {
            obtner_lugares_seguros_cerca_turismo(lat, lng, localidad)
        }

        val sitios_cercanos = async {
            obtener_servicios_esenciales(lat, lng, localidad)
        }

        inmueble.copy(
            listalugares_cercanos = lugaresCercanosDeferred.await(),
            cantidad_lugares_seguros = lugaresSegurosDeferred.await(),
            llissa_lugareS_turistos = turismoDeferred.await(),
            lista_servicios_sercanos = sitios_cercanos.await()
        )
    }


    suspend fun obtener_cantidad_lugares_cercanos(
        lat: Double,
        lng: Double,
        localidad: String,
        radiusKm: Double = 1.0
    ): List<lugares_cercanos_> {

        val bounds = GeoFireUtils.getGeoHashQueryBounds(GeoLocation(lat, lng), radiusKm * 1000)

        val lista = bounds.flatMap { bound ->
            db.collection("Tiendas")
                .document(localidad)
                .collection(localidad)
                .orderBy("geohash")
                .startAt(bound.startHash)
                .endAt(bound.endHash)
                .get()
                .await()
                .documents
        }.distinctBy { it.id }
        return lista
            .filter { doc ->
                val ubicacion = doc.get("ubicacion") as? Map<*, *>
                val docLat = ubicacion?.get("latitud") as? Double ?: return@filter false
                val docLng = ubicacion?.get("longitud") as? Double ?: return@filter false
                calcularDistanciaKm_directo(lat, lng, docLat, docLng) <= radiusKm
            }
            .map { doc ->
                val listaImg = doc.get("img_tienda.logo_tienda") as? String ?: ""
                val subcategorias = doc.get("subcategoria") as? List<*>
                val ubicacion = doc.get("ubicacion") as? Map<*, *>
                val docLat = ubicacion?.get("latitud") as? Double ?: 0.0
                val docLng = ubicacion?.get("longitud") as? Double ?: 0.0
                val distancia = calcularDistanciaKm_directo(lat, lng, docLat, docLng)
                lugares_cercanos_(
                    img_String = listaImg,
                    nombre = doc.getString("nombre_tienda") ?: "",
                    categoira = doc.getString("categoria_tienda") ?: "",
                    subcategoria = subcategorias?.firstOrNull()?.toString() ?: "",
                    distancia
                )
            }
    }

    suspend fun obtner_lugares_seguros_cerca(
        lat: Double,
        lng: Double,
        localidad: String,
        radiusKm: Double = 1.0
    ): List<lugares_cercanos_> {

        val bounds = GeoFireUtils.getGeoHashQueryBounds(
            GeoLocation(lat, lng),
            radiusKm * 1000
        )

        val docs = bounds.flatMap { bound ->
            db.collection("Tiendas")
                .document("salud_seguridad")
                .collection(localidad)
                .orderBy("geohash")
                .startAt(bound.startHash)
                .endAt(bound.endHash)
                .get()
                .await()
                .documents
        }.distinctBy { it.id } // evita duplicados

        return docs
            .filter { doc ->
                val ubicacion = doc.get("ubicacion") as? Map<*, *>
                val docLat = ubicacion?.get("latitud") as? Double ?: return@filter false
                val docLng = ubicacion?.get("longitud") as? Double ?: return@filter false

                val distancia = calcularDistanciaKm_directo(lat, lng, docLat, docLng)

                Log.d("lugares_seguros", "doc: ${doc.id} | distancia: $distancia km")

                distancia <= radiusKm
            }
            .map { doc ->

                val img = doc.getString("img") ?: ""
                val nombre = doc.getString("nombre") ?: ""
                val categoria = doc.getString("categoria") ?: ""
                val ubicacion = doc.get("ubicacion") as? Map<*, *>
                val docLat = ubicacion?.get("latitud") as? Double ?: 0.0
                val docLng = ubicacion?.get("longitud") as? Double ?: 0.0
                val distancia = calcularDistanciaKm_directo(lat, lng, docLat, docLng) // 👈

                lugares_cercanos_(
                    img_String = img,
                    nombre = nombre,
                    categoira = categoria,
                    subcategoria = "seguridad",
                    distanciaKm = distancia
                )
            }
    }

    suspend fun obtner_lugares_seguros_cerca_turismo(
        lat: Double,
        lng: Double,
        localidad: String,
        radiusKm: Double = 1.0
    ): List<lugares_cercanos_> {

        val bounds = GeoFireUtils.getGeoHashQueryBounds(
            GeoLocation(lat, lng),
            radiusKm * 1000
        )

        val docs = bounds.flatMap { bound ->
            db.collection("Tiendas")
                .document(localidad)
                .collection("lugares_turisticos")
                .orderBy("geohash")
                .startAt(bound.startHash)
                .endAt(bound.endHash)
                .get()
                .await()
                .documents
        }.distinctBy { it.id } // evita duplicados

        return docs
            .filter { doc ->
                val ubicacion = doc.get("ubicacion") as? Map<*, *>
                val docLat = ubicacion?.get("latitud") as? Double ?: return@filter false
                val docLng = ubicacion?.get("longitud") as? Double ?: return@filter false

                val distancia = calcularDistanciaKm_directo(lat, lng, docLat, docLng)

                Log.d("lugares_seguros", "doc: ${doc.id} | distancia: $distancia km")

                distancia <= radiusKm
            }
            .map { doc ->
                val ubicacion = doc.get("ubicacion") as? Map<*, *>
                val docLat = ubicacion?.get("latitud") as? Double ?: 0.0
                val docLng = ubicacion?.get("longitud") as? Double ?: 0.0
                val img = doc.get("img.principal") as? String ?: ""
                val nombre = doc.getString("titulo") ?: ""
                val categoria = ""
                val distancia = calcularDistanciaKm_directo(lat, lng, docLat, docLng)
                lugares_cercanos_(
                    img_String = img,
                    nombre = nombre,
                    categoira = categoria,
                    subcategoria = "",
                    distancia
                )
            }
    }


    suspend fun generacion_texto_por_IA(
        i: ia_inmobiliara_tts,
        perfil_selet: String
    ): String {

        val model = Firebase.ai(
            backend = GenerativeBackend.googleAI()
        ).generativeModel("gemini-2.5-flash")

        val prompt = contruir_promp_ia_datos_inmobiliara(i, perfil_selet)

        return try {

            val result = model.generateContent(prompt)
            val raw = result.text?.trim().orEmpty()

            if (raw.isBlank()) {
                Log.d("NLP_FLOW", "Respuesta vacía")
                ""
            } else {
                Log.d("NLP_FLOW", "Respuesta IA: $raw")
                raw
            }

        } catch (e: Exception) {

            Log.e("NLP_FLOW", "Error en generación IA", e)
            ""
        }
    }

    suspend fun obtener_servicios_esenciales(
        lat: Double,
        lng: Double,
        localidad: String,
        radiusKm: Double = 1.0
    ): List<lugares_cercanos_> {

        val bounds = GeoFireUtils.getGeoHashQueryBounds(
            GeoLocation(lat, lng),
            radiusKm * 1000
        )

        val docs = bounds.flatMap { bound ->
            db.collection("Tiendas")
                .document("servicios_basicos")
                .collection(localidad)
                .orderBy("geohash")
                .startAt(bound.startHash)
                .endAt(bound.endHash)
                .get()
                .await()
                .documents
        }.distinctBy { it.id }

        return docs
            .filter { doc ->
                val ubicacion = doc.get("direccion") as? Map<*, *>
                val docLat = ubicacion?.get("lat") as? Double ?: return@filter false
                val docLng = ubicacion?.get("log") as? Double ?: return@filter false

                val distancia = calcularDistanciaKm_directo(lat, lng, docLat, docLng)

                Log.d("lugares_seguros", "doc: ${doc.id} | distancia: $distancia km")

                distancia <= radiusKm
            }
            .map { doc ->
                val ubicacion = doc.get("direccion") as? Map<*, *>
                val docLat = ubicacion?.get("lat") as? Double ?: 0.0
                val docLng = ubicacion?.get("log") as? Double ?: 0.0
                val img = doc.getString("img_logo") ?: ""
                val nombre = doc.getString("lugar_nombre") ?: ""
                val distancia = calcularDistanciaKm_directo(lat, lng, docLat, docLng)

                lugares_cercanos_(
                    img_String = img,
                    nombre = nombre,
                    categoira = "",
                    subcategoria = "seguridad",
                    distancia
                )
            }
    }


    suspend fun buscarPorGeohash(
        lat: Double,
        lng: Double,
        radiusKm: Double,
        query: Query
    ): List<DocumentSnapshot> {

        val bounds = GeoFireUtils.getGeoHashQueryBounds(
            GeoLocation(lat, lng),
            radiusKm * 1000
        )

        return bounds.flatMap { bound ->
            query
                .startAt(bound.startHash)
                .endAt(bound.endHash)
                .get()
                .await()
                .documents
        }.distinctBy { it.id }
    }

//    suspend fun agregar_geohasgin_turistico() {
//
//        val ref = FirebaseFirestore.getInstance()
//            .collection("Tiendas")
//            .document("servicios_basicos")
//            .collection("barranca")
//
//        val snapshot = ref.get().await()
//
//        for (doc in snapshot.documents) {
//
//            val data = doc.data ?: continue
//
//            val mapUbicacion = data["direccion"] as? Map<String, Any> ?: continue
//
//            val lat = (mapUbicacion["lat"] as? Number)?.toDouble() ?: continue
//            val lng = (mapUbicacion["log"] as? Number)?.toDouble() ?: continue
//
//            val geohash = constantes_lista_localidades.geohashing(lat, lng)
//
//            ref.document(doc.id)
//                .set(
//                    mapOf("geohash" to geohash),
//                    SetOptions.merge() // 👈 NO borra otros campos
//                )
//                .await()
//        }
//    }

}


private fun calcularDistanciaKm_directo(
    lat1: Double, lng1: Double,
    lat2: Double, lng2: Double
): Double {
    val R = 6371.0
    val dLat = Math.toRadians(lat2 - lat1)
    val dLng = Math.toRadians(lng2 - lng1)
    val a = sin(dLat / 2).pow(2) +
            cos(Math.toRadians(lat1)) *
            cos(Math.toRadians(lat2)) *
            sin(dLng / 2).pow(2)
    val c = 2 * atan2(sqrt(a), sqrt(1 - a))
    return R * c
}