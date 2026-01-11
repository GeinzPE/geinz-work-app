package com.geinzz.geinzwork.model

import android.util.Log
import com.geinzz.geinzwork.data.model.localizate_geinz.HorarioAtencion
import com.geinzz.geinzwork.data.model.localizate_geinz.HorarioDia
import com.geinzz.geinzwork.data.model.localizate_geinz.filtrado_tiendas.horario_tienda
import com.geinzz.geinzwork.data.model.localizate_geinz.inicio_geinz.favoritos_guardados
import com.geinzz.geinzwork.utils.constantes.constantes.constantes_bottomShet_fourdItem.obtenerHorarioDeHoy_BOX
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.constantes_horas.obtenerProximoDiaAbierto
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.constantes_lista_localidades.convertirABox
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.constantes_lista_localidades.estaAbiertoHoy
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.constantes_lista_localidades.toMetodoContacto
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.constantes_lista_localidades.to_horario_atencion_box_dia
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.constantes_lista_localidades.to_metodo_pago
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.constantes_lista_localidades.verificarSiEstaAbiertoHoy
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await
import java.util.Calendar
import kotlin.collections.mapOf

class repo_favoritos {
    val db = FirebaseFirestore.getInstance()
    val repo_filtrado = repo_filtrado_tiendas()
    fun obtener_favoritos_realtime(
        id_user: String,
        onUpdate: (Triple<List<favoritos_guardados>, List<String>, List<String>>) -> Unit
    ) {
        val ref = db.collection("Trabajadores_Usuarios_Drivers")
            .document("users")
            .collection("users")
            .document(id_user)
            .collection("favoritos")
        Log.d("obteniredos_id","$id_user")

        ref.addSnapshotListener { snapshot, error ->
            if (error != null) {
                Log.e("FAVORITOS_LISTENER", "Error al escuchar favoritos", error)
                return@addSnapshotListener
            }

            if (snapshot != null) {
                val listaFavoritos = mutableListOf<favoritos_guardados>()
                val listaCategorias = mutableListOf<String>()
                val lista_localidades = mutableListOf<String>()

                for (doc in snapshot.documents) {
                    val data = doc.data ?: continue

                    val horarioMap = data["horario"] as? Map<String, Any> ?: emptyMap()
                    val categoria = data["categoria"] as? String ?: ""
                    val localidad = data["localidad_lugar_tienda"] as? String ?: ""
                    val horario_map_box = horarioMap.to_horario_atencion_box_dia()


                    val horarioHoyBloques = obtenerHorarioDeHoy_BOX(horario_map_box)
                    val horarioHoyBox = convertirABox(horarioHoyBloques)
                    val estaAbierto = estaAbiertoHoy(horarioHoyBox)
                    Log.d("estaabeirtooo", estaAbierto.toString())

                    val favorito = favoritos_guardados(
                        img_tienda = data["img_tienda_lugar"] as? String ?: "",
                        id_tienda_lugar = data["id_tienda_lugar"] as? String ?: "",
                        nombre_lugar_tienda = data["nombre_lugar_tienda"] as? String ?: "",
                        categoria = categoria,
                        timesLap = data["timesLap_local"] as? String ?: "",
                        lat = (data["latitud"] as? Number)?.toDouble() ?: 0.0,
                        lng = (data["longitud"] as? Number)?.toDouble() ?: 0.0,
                        estaAbierto = estaAbierto,
                        localida_tienda = localidad,
                        horario_tienda_box = horario_map_box
                    )

                    listaFavoritos.add(favorito)
                    listaCategorias.add(categoria)
                    lista_localidades.add(localidad)
                }

                onUpdate(Triple(listaFavoritos, listaCategorias, lista_localidades))
            }
        }
    }


    fun obtener_timestamps_tiendas(
        idsFavoritos: List<Pair<String, String>>,
        onComplete: (Map<String, String>) -> Unit
    ) {
        if (idsFavoritos.isEmpty()) {
            onComplete(emptyMap())
            return
        }

        // Agrupar IDs por localidad
        val agrupadosPorLocalidad = idsFavoritos.groupBy { it.second } // segundo = localidad

        val resultadoFinal = mutableMapOf<String, String>()
        var consultasPendientes = 0

        for ((localidad, listaIds) in agrupadosPorLocalidad) {

            // Partir cada lista en grupos de máximo 10 IDs (por límite de Firestore)
            val chunks = listaIds.chunked(10)

            consultasPendientes += chunks.size

            for (chunk in chunks) {

                val ids = chunk.map { it.first } // primer elemento = id_tienda

                val ref = db.collection("Tiendas")
                    .document(localidad)
                    .collection(localidad)

                ref.whereIn(FieldPath.documentId(), ids)
                    .get()
                    .addOnSuccessListener { snapshot ->

                        for (doc in snapshot.documents) {
                            val timestampTienda = doc.getString("timeSlamp") ?: "0"
                            resultadoFinal[doc.id] = timestampTienda
                        }

                        consultasPendientes--

                        if (consultasPendientes == 0) {
                            onComplete(resultadoFinal)
                        }
                    }
                    .addOnFailureListener {
                        consultasPendientes--
                        if (consultasPendientes == 0) {
                            onComplete(resultadoFinal)
                        }
                    }
            }
        }
    }


    suspend fun obtener_nuevos_datos(localidad: String, id: String): favoritos_guardados {
        val ref = db.collection("Tiendas")
            .document(localidad)
            .collection(localidad)
            .document(id)
            .get()
            .await()

        if (!ref.exists()) {
            return favoritos_guardados(
                localida_tienda = TODO()
            )
        }

        val data = ref.data ?: emptyMap<String, Any>()

        val horario=data["horario_atencion"] as? Map<String, Any> ?: emptyMap()
        val hoarario_mapBox=horario.to_horario_atencion_box_dia()

        val lat_lng =data["ubicacion"] as? Map<String, Any> ?:emptyMap()
        val lat=lat_lng.get("latitud") as? Number ?: 0
        val lng=lat_lng.get("latitud") as? Number ?: 0
        val img_tienda =data["img_tienda"] as? Map<String, Any> ?: emptyMap()
        val logo=img_tienda.get("logo_tienda") as? String?:""

        return favoritos_guardados(
            id_tienda_lugar       = id,
            nombre_lugar_tienda   = data["nombre_tienda"] as? String ?: "",
            categoria             = data["categoria_tienda"] as? String ?: "",
            timesLap              = data["timeSlamp"] as? String ?: "",
            horario_tienda_box   = hoarario_mapBox,
            lat               = lat.toDouble(),
            lng              = lng.toDouble(),
            img_tienda      = logo,
            localida_tienda       = localidad
        )
    }


    fun actalizar_tienda(item:favoritos_guardados,id_user: String,id_tienda:String){
        val ref = db.collection("Trabajadores_Usuarios_Drivers")
            .document("users")
            .collection("users")
            .document(id_user)
            .collection("favoritos").document(id_tienda)

        val data = mapOf(
            "id_tienda_lugar" to item.id_tienda_lugar,
            "nombre_lugar_tienda" to item.nombre_lugar_tienda,
            "categoria" to item.categoria,
            "timesLap_local" to item.timesLap,
            "horario" to item.horario_tienda_box,
            "latitud" to item.lat,
            "longitud" to item.lng,
            "img_tienda_lugar" to item.img_tienda ,
            "localidad_lugar_tienda" to item.localida_tienda

        )
        ref.set(data, SetOptions.merge()).addOnSuccessListener {
            Log.d("tienda","actualziado")
        }.addOnFailureListener { e->
            Log.d("tienda","error al actualizar")

        }

    }



}