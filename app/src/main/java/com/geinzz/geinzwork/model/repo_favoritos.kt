package com.geinzz.geinzwork.model

import com.geinzz.geinzwork.data.model.localizate_geinz.HorarioAtencion
import com.geinzz.geinzwork.data.model.localizate_geinz.HorarioDia
import com.geinzz.geinzwork.data.model.localizate_geinz.inicio_geinz.favoritos_guardados
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.constantes_lista_localidades.toMetodoContacto
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.constantes_lista_localidades.to_metodo_pago
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class repo_favoritos {
    val db = FirebaseFirestore.getInstance()

    suspend fun obtener_favoritos(id_user: String): List<favoritos_guardados> {
        val listaFavoritos = mutableListOf<favoritos_guardados>()
        val ref =
            db.collection("Trabajadores_Usuarios_Drivers").document("users").collection("users")
                .document(id_user).collection("favoritos").get().await()
        for (doc in ref) {
            val data = doc.data ?: continue
            fun mapearDia(diaMap: Map<String, Any>?): HorarioDia {
                if (diaMap == null) return HorarioDia()
                return HorarioDia(
                    cerrado = diaMap["cerrado"] as? Boolean ?: false,
                    h_apertura = diaMap["h_apertura"] as? String ?: "",
                    h_cierre = diaMap["h_cierre"] as? String ?: "",
                    motivo = diaMap["motivo"] as? String ?: ""
                )
            }

            val horarioMap = data?.get("horario_atencion") as? Map<String, Any> ?: emptyMap()
            val metodo_pago = data?.get("metodos_pago") as? Map<String, Any> ?: emptyMap()

            val metodo_pago_tienda = metodo_pago.to_metodo_pago()

            val horarioTienda = HorarioAtencion(
                lunes = mapearDia(horarioMap["lunes"] as? Map<String, Any>),
                martes = mapearDia(horarioMap["martes"] as? Map<String, Any>),
                miercoles = mapearDia(horarioMap["miércoles"] as? Map<String, Any>),
                jueves = mapearDia(horarioMap["jueves"] as? Map<String, Any>),
                viernes = mapearDia(horarioMap["viernes"] as? Map<String, Any>),
                sabado = mapearDia(horarioMap["sábado"] as? Map<String, Any>),
                domingo = mapearDia(horarioMap["domingo"] as? Map<String, Any>)
            )
            val favorito = favoritos_guardados(
                id_tienda_lugar = data["id_tienda_lugar"] as? String ?: "",
                nombre_lugar_tienda = data["nombre_lugar_tienda"] as? String ?: "",
                tag_sub = data["tag_sub"] as? List<String> ?: emptyList(),
                categoria = data["categoria"] as? String ?: "",
                timesLap = data["timesLap"] as? String ?: "",
                horario = horarioTienda,
                metodos_pago = metodo_pago_tienda,
                lat = (data["lat"] as? Number)?.toDouble() ?: 0.0,
                lng = (data["lng"] as? Number)?.toDouble() ?: 0.0
            )

            listaFavoritos.add(favorito)
        }
        return listaFavoritos
    }
}