package com.geinzz.geinzwork.model

import com.geinzz.geinzwork.data.model.dataclass_user.data_class_usuario_general
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class imfo_user_repo {
    val db = FirebaseFirestore.getInstance()
    private lateinit var firebaseAuth: FirebaseAuth

    val colecion_trabajadores = db.collection("Trabajadores_Usuarios_Drivers")
        .document("trabajadores").collection("trabajadores")

    val colecion_usuarios = db.collection("Trabajadores_Usuarios_Drivers")
        .document("usuarios").collection("usuarios")

    val collecion_verificados =
        db.collection("solicitudes_servicios").document("verificaciones").collection("activos")


    suspend fun obtener_perfil_user(): List<data_class_usuario_general> {
        val lista = mutableListOf<data_class_usuario_general>()
        firebaseAuth = FirebaseAuth.getInstance()
        val currentUser = firebaseAuth.currentUser ?: return lista

        val uid = currentUser.uid

        val trabajadorSnapshot = colecion_trabajadores.whereEqualTo("id", uid).get().await()
        val documentoEncontrado = trabajadorSnapshot.documents.firstOrNull()

        val data = documentoEncontrado?.data ?: colecion_usuarios
            .whereEqualTo("id", uid).get().await()
            .documents.firstOrNull()?.data

        data?.let {
            val datos = data_class_usuario_general(
                it["nombre"] as? String ?: "",
                it["apellido"] as? String ?: "",
                it["localidad"] as? String ?: "",
                it["TipoCuenta"] as? String ?: "",
                it["imagenPerfil"] as? String ?: "",
                it["categoriaTrabajo"] as? String ?: "",
                it["id"] as? String ?: "",
                it["nacionalidad"] as? String ?: "",

                )
            lista.add(datos)
        }

        return lista
    }

    suspend fun verificado_user(id_trabajador: String): Boolean {
        var verificado: Boolean = false
        val trabajadorSnapshot = collecion_verificados.document(id_trabajador).get().await()
        if (trabajadorSnapshot.exists()) {
            verificado = true
        } else {
            verificado = false
        }
        return verificado
    }
}