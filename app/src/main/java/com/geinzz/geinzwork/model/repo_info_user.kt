package com.geinzz.geinzwork.model

import android.util.Log
import androidx.compose.ui.graphics.Paint
import com.geinzz.geinzwork.data.model.dataclass_user.data_class_usuario_general
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await


class repo_info_user {
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
        var verificado = false
        val trabajadorSnapshot = collecion_verificados.document(id_trabajador).get().await()
        if (trabajadorSnapshot.exists()) {
            verificado = true
        } else {
            verificado = false
        }
        return verificado
    }

    suspend fun encontrar_user(id: String): Pair<Boolean, CollectionReference?> {
        val posiblesColecciones = listOf("usuarios", "trabajadores")
        for (nombreCol in posiblesColecciones) {
            val docSnapshot = db.collection("Trabajadores_Usuarios_Drivers").document(nombreCol)
                .collection(nombreCol).document(id).get().await()
            if (docSnapshot.exists()) {
                return Pair(
                    true,
                    db.collection("Trabajadores_Usuarios_Drivers").document(nombreCol)
                        .collection(nombreCol)
                )
            }
        }
        return Pair(false, null)
    }


    suspend fun verificar_acceso(
        androidId: String
    ): Boolean {
        val id_user = FirebaseAuth.getInstance().uid ?: return false
        val (encontrado_boolean, collection) = encontrar_user(id_user) ?: return false
        Log.d("obtenemos_datos", "fue encontra en $encontrado_boolean y $collection")
        if (!encontrado_boolean || collection == null) return false

        val ref_vinculados = collection.document(id_user)
            .collection("vinculados")
            .get()
            .await()

        for (datos in ref_vinculados) {
            val id = datos.getString("id_dispositivo")
            if (androidId == id) {
                return true
            }
        }
        FirebaseAuth.getInstance().signOut()
        return false
    }

    fun verificarAccesoTiempoRealFlow(androidId: String): Flow<Boolean> = callbackFlow {
        val idUser = FirebaseAuth.getInstance().uid

        if (idUser == null) {
            trySend(false)
            close()
            return@callbackFlow
        }

        val (encontradoBoolean, collection) = encontrar_user(idUser) ?: run {
            trySend(false)
            close()
            return@callbackFlow
        }

        if (!encontradoBoolean || collection == null) {
            trySend(false)
            close()
            return@callbackFlow
        }

        val listenerRegistration = collection.document(idUser)
            .collection("vinculados")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(false)
                    return@addSnapshotListener
                }

                val tieneAcceso = snapshot?.any { doc ->
                    doc.getString("id_dispositivo") == androidId
                } ?: false

                if (!tieneAcceso) {
                    FirebaseAuth.getInstance().signOut()
                }

                trySend(tieneAcceso)
            }


        awaitClose {
            listenerRegistration.remove()
        }
    }

    suspend fun nombra_localidad_user(id: String): Pair<String, String> {
        val posiblesColecciones = listOf("usuarios", "trabajadores")
        for (nombreCol in posiblesColecciones) {
            val docSnapshot = db.collection("Trabajadores_Usuarios_Drivers").document(nombreCol)
                .collection(nombreCol).document(id).get().await()
            if (docSnapshot.exists()) {
                val nombre = docSnapshot.get("nombre") as? String ?: ""
                val localidad = docSnapshot.get("localidad") as? String ?: ""
                return Pair(
                    nombre, localidad
                )
            }
        }
        return Pair("", "")
    }

}