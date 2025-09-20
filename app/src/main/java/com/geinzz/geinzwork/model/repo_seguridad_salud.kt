package com.geinzz.geinzwork.model

import androidx.compose.runtime.mutableStateOf
import com.geinzz.geinzwork.data.model.dataclass_seguridad.dataclass_seguridad
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class repo_seguridad_salud {
    val db = FirebaseFirestore.getInstance()

    suspend fun obtener_servicios_salud(localdad: String): List<dataclass_seguridad> {
        val lista = mutableListOf<dataclass_seguridad>()
        val ref =
            db.collection("Tiendas").document("salud_seguridad").collection(localdad).get().await()

        for (datos in ref) {
            val data = datos.data
            val ubicacion = data?.get("ubicacion") as? Map<String, Any> ?: emptyMap()
            val numero_contacto = data?.get("numeros_contactos") as? Map<String, Any> ?: emptyMap()
            val latitud = (ubicacion["latitud"] as? Number ?: 0).toDouble()
            val longitud = (ubicacion["longitud"] as? Number ?: 0).toDouble()
            val categoria = data?.get("categoria") as? String ?: ""
            val servicios = dataclass_seguridad(
                nombre_ = data.get("nombre") as? String ?: "",
                direccion = ubicacion["direccion"] as? String ?: "",
                numero_llamada = "",
                numero_whatsapp = "",
                img_ref = data.get("img_ref") as? String ?: "",
                latidud = latitud,
                longitud = longitud,
                categoria = categoria
            )
            lista.add(servicios)

        }
        return lista

    }
}