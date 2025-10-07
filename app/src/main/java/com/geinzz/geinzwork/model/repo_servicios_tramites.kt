package com.geinzz.geinzwork.model

import com.geinzz.geinzwork.data.model.dataclass_lugares_db
import com.geinzz.geinzwork.data.model.direccion_lugar
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class repo_servicios_tramites {
    val db = FirebaseFirestore.getInstance()

    suspend fun obtenerServiciosTramites(localidad: String): List<dataclass_lugares_db> {
        val snapshot = db.collection("Tiendas")
            .document("servicios_basicos")
            .collection(localidad)
            .get()
            .await()

        return snapshot.documents.mapNotNull { doc ->
            val data = doc.data ?: return@mapNotNull null
            val direccion = data["direccion"] as? Map<String, Any> ?: emptyMap()
            val datos_direcion = direccion_lugar(
                lat = (direccion["lat"] as? Number)?.toDouble() ?: 0.0,
                log = (direccion["log"] as? Number)?.toDouble() ?: 0.0,
                direccion = direccion["direccion"] as? String ?: "",
                refencia = direccion["referencia"] as? String ?: ""
            )

            dataclass_lugares_db(
                categoria = data["categoria"] as? List<String> ?: emptyList(),
                direccion = datos_direcion,
                id = data["id"] as? String ?: "",
                lugar_nombre = data["lugar_nombre"] as? String ?: "",
                logo_img = data["img_logo"] as? String ?: "",
            )
        }
    }

}