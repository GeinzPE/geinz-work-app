package com.geinzz.geinzwork.model

import com.geinzz.geinzwork.data.model.localizate_geinz.inicio_geinz.datos_principales_user
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class repo_usuario_registrado {
    val db = FirebaseFirestore.getInstance()
    suspend fun obtenerDatosUser(idUser: String): datos_principales_user? {
        val ref = db.collection("Trabajadores_Usuarios_Drivers")
            .document("users")
            .collection("users")
            .document(idUser)
            .get()
            .await()

        return if (ref.exists()) {
            ref.toObject(datos_principales_user::class.java)
        } else {
            null
        }
    }

}