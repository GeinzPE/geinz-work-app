package com.geinzz.geinzwork.model

import com.geinzz.geinzwork.data.model.datamode_notificaciones.data_class_notificaciones
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class repo_notificaciones {
    val db = FirebaseFirestore.getInstance()
    private val info_general = repo_info_user()
    private val validacion_texto = repo_texto_cambios_validaciones()

    suspend fun notificar_tokes(id_user: String): List<data_class_notificaciones> {
        val listatokes_nombre = mutableListOf<data_class_notificaciones>()
        val encontra_user = info_general.encontrar_user(id_user)
        val (encontrado, collection) = encontra_user
        if (encontrado && collection != null) {
            val admin = collection.document(id_user).collection("vinculados").get().await()
            for (Snapshot in admin) {
                val data = Snapshot.data
                val primario = data?.get("primario") as? Boolean ?: false
                val dispositivo_name_admin = data?.get("dispositivo") as? String ?: ""
                if (primario) {
                    val docSnapshot = db.collection("Trabajadores_Usuarios_Drivers")
                        .document("tokens")
                        .collection(id_user)
                        .document("dispositivos")
                        .get()
                        .await()
                    val tokensMap = docSnapshot.get("tokens") as? Map<*, *> ?: return emptyList()
                    tokensMap.mapNotNull { (dispositivo, token) ->
                        val key = dispositivo?.toString()
                        val value = token?.toString()
                        if (validacion_texto.verificar_dispostivo_iguales(
                                dispositivo_name_admin,
                                dispositivo.toString()
                            )
                        ) {
                            val tokens_nombre = data_class_notificaciones(value, key)
                            listatokes_nombre.add(tokens_nombre)
                        }

                    }
                }
            }
        }


        return listatokes_nombre
    }

    suspend fun notificar_cerrado_Seccion(
        id_user: String,
        dispo_cerrando_seccion: String
    ): List<data_class_notificaciones> {
        val listatokes_nombre = mutableListOf<data_class_notificaciones>()
        val encontra_user = info_general.encontrar_user(id_user)
        val (encontrado, collection) = encontra_user
        if (encontrado && collection != null) {
            val docSnapshot = db.collection("Trabajadores_Usuarios_Drivers")
                .document("tokens")
                .collection(id_user)
                .document("dispositivos")
                .get()
                .await()
            val tokensMap = docSnapshot.get("tokens") as? Map<*, *> ?: return emptyList()
            tokensMap.mapNotNull { (dispositivo, token) ->
                val key = dispositivo?.toString()
                val value = token?.toString()
                if (validacion_texto.verificar_dispostivo_iguales(
                        dispo_cerrando_seccion, dispositivo.toString()
                    )
                ) {
                    val tokens_nombre = data_class_notificaciones(value, key)
                    listatokes_nombre.add(tokens_nombre)
                }

            }

        }
        return listatokes_nombre
    }

}