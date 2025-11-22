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
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.util.Calendar

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

//                    fun mapearDia(diaMap: Map<String, Any>?): HorarioDia {
//                        if (diaMap == null) return HorarioDia()
//                        return HorarioDia(
//                            cerrado = diaMap["cerrado"] as? Boolean ?: false,
//                            h_apertura = diaMap["h_apertura"] as? String ?: "",
//                            h_cierre = diaMap["h_cierre"] as? String ?: "",
//                            motivo = diaMap["motivo"] as? String ?: ""
//                        )
//                    }

                    val horarioMap = data["horario"] as? Map<String, Any> ?: emptyMap()
//                    val metodo_pago = data["metodos_pago"] as? Map<String, Any> ?: emptyMap()
                    val categoria = data["categoria"] as? String ?: ""
                    val localidad = data["localidad_lugar_tienda"] as? String ?: ""
                    val horario_map_box = horarioMap.to_horario_atencion_box_dia()
//                    val metodo_pago_tienda = metodo_pago.to_metodo_pago()
                    val horarioHoyBloques = obtenerHorarioDeHoy_BOX(horario_map_box)
                    val horarioHoyBox = convertirABox(horarioHoyBloques)
                    val estaAbierto = estaAbiertoHoy(horarioHoyBox)
                    Log.d("estaabeirtooo",estaAbierto.toString())

//                    val horarioTienda = HorarioAtencion(
//                        lunes = mapearDia(horarioMap["lunes"] as? Map<String, Any>),
//                        martes = mapearDia(horarioMap["martes"] as? Map<String, Any>),
//                        miercoles = mapearDia(horarioMap["miercoles"] as? Map<String, Any>),
//                        jueves = mapearDia(horarioMap["jueves"] as? Map<String, Any>),
//                        viernes = mapearDia(horarioMap["viernes"] as? Map<String, Any>),
//                        sabado = mapearDia(horarioMap["sabado"] as? Map<String, Any>),
//                        domingo = mapearDia(horarioMap["domingo"] as? Map<String, Any>)
//                    )

//                    val cast_horario = repo_filtrado.obtener_estado_horario_tienda(horarioTienda)
//                    Log.d("3fafgSDGSDAFTGSDF", cast_horario.toString())

                    val favorito = favoritos_guardados(
                        img_tienda = data["img_tienda_lugar"] as? String ?: "",
                        id_tienda_lugar = data["id_tienda_lugar"] as? String ?: "",
                        nombre_lugar_tienda = data["nombre_lugar_tienda"] as? String ?: "",
//                        tag_sub = data["tag_sub"] as? List<String> ?: emptyList(),
                        categoria = categoria,
                        timesLap = data["timesLap"] as? String ?: "",
//                        horario = cast_horario,
//                        metodos_pago = metodo_pago_tienda,
                        lat = (data["latitud"] as? Number)?.toDouble() ?: 0.0,
                        lng = (data["longitud"] as? Number)?.toDouble() ?: 0.0,estaAbierto=estaAbierto,
                        localida_tienda = localidad, horario_tienda_box = horario_map_box
                    )

                    listaFavoritos.add(favorito)
                    listaCategorias.add(categoria)
                    lista_localidades.add(localidad)
                }

                onUpdate(Triple(listaFavoritos, listaCategorias, lista_localidades))
            }
        }
    }


}