package com.geinzz.geinzwork.model

import com.geinzz.geinzwork.data.model.contacto_lugares_gratis
import com.geinzz.geinzwork.data.model.dataclass_lugares_db
import com.geinzz.geinzwork.data.model.direccion_lugar
import com.geinzz.geinzwork.data.model.obtener_servicios_lugares
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class repo_servicios_tramites {
    val db = FirebaseFirestore.getInstance()

    suspend fun obtenerServiciosTramites(localidad: String): List<obtener_servicios_lugares> {
        val snapshot = db.collection("Tiendas")
            .document("servicios_basicos")
            .collection(localidad)
            .get()
            .await()

        return snapshot.documents.mapNotNull { doc ->
            val data = doc.data ?: return@mapNotNull null
//            val direccion = data["direccion"] as? Map<String, Any> ?: emptyMap()
//            val contacto = data["contacto"] as? Map<String, Any> ?: emptyMap()

//            val datos_direcion = direccion_lugar(
//                lat = (direccion["lat"] as? Number)?.toDouble() ?: 0.0,
//                log = (direccion["log"] as? Number)?.toDouble() ?: 0.0,
//                direccion = direccion["direccion"] as? String ?: "",
//                refencia = direccion["referencia"] as? String ?: ""
//            )
//            val datos_contacto = contacto_lugares_gratis(
//                facebook = (contacto["facebook"] as? String ?: ""),
//                ig = (contacto["ig"] as? String ?: ""),
//                sitio_web = (contacto["sitio_web"] as? String ?: ""),
//                telefono = (contacto["telefono"] as? List<String> ?: emptyList()),
//                tk = (contacto["tk"] as? String ?: ""),
//                whatsapp = (contacto["whatsapp"] as? List<String> ?: emptyList()),
//            )

            obtener_servicios_lugares(
                categoria = data["categoria"] as? List<String> ?: emptyList(),
                id = data["id"] as? String ?: "",
                localidad_params=localidad,
                img = data["img_logo"] as? String ?: "",
                lugar_nombre=data["lugar_nombre"] as? String ?: "",
                pagado = data["pagado"] as? Boolean ?: false
            )
        }
    }

    suspend fun obtener_datos_servicios_tramites(
        localidad: String,
        id: String
    ): dataclass_lugares_db {

        val snapshot = db.collection("Tiendas")
            .document("servicios_basicos")
            .collection(localidad)
            .document(id)
            .get()
            .await()

        if (!snapshot.exists()) {
            throw Exception("El documento no existe")
        }

        val data = snapshot.data!!

        val direccion = data["direccion"] as? Map<String, Any> ?: emptyMap()
        val contacto = data["contacto"] as? Map<String, Any> ?: emptyMap()

        val datos_direccion = direccion_lugar(
            lat = (direccion["lat"] as? Number)?.toDouble() ?: 0.0,
            log = (direccion["log"] as? Number)?.toDouble() ?: 0.0,
            direccion = direccion["direccion"] as? String ?: "",
            refencia = direccion["referencia"] as? String ?: ""
        )

        val datos_contacto = contacto_lugares_gratis(
            facebook = contacto["facebook"] as? String ?: "",
            ig = contacto["ig"] as? String ?: "",
            sitio_web = contacto["sitio_web"] as? String ?: "",
            telefono = contacto["telefono"] as? List<String> ?: emptyList(),
            tk = contacto["tk"] as? String ?: "",
            whatsapp = contacto["whatsapp"] as? List<String> ?: emptyList()
        )

        return dataclass_lugares_db(
            descripcion = data["descripcion"] as? String ?: "",
            categoria = data["categoria"] as? List<String> ?: emptyList(),
            direccion = datos_direccion,
            id = data["id"] as? String ?: "",
            lugar_nombre = data["lugar_nombre"] as? String ?: "",
            logo_img = data["img_logo"] as? String ?: "",
            contacto = datos_contacto,
            pagado = data["pagado"] as? Boolean ?: false
        )
    }

}