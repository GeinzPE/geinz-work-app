package com.example.geinzwork.constantesGeneral

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore

object obtenertokenIdAdmin {
    fun obtenertokenAdmin(callbackk: (String, String) -> Unit) {
        val db = FirebaseFirestore.getInstance().collection(Variables.trabajadores_usuariosDB)
            .document(Variables.adminDB).collection(Variables.adminDB)
        db.get().addOnSuccessListener { res ->
            for (dat in res) {
                val datos = dat.data
                val token = datos[Variables.token] as? String ?: ""
                val id = datos[Variables.id] as? String ?: ""
                callbackk(token, id)
            }

        }.addOnFailureListener { e ->
            callbackk("", "")
            Log.e("error_token", "error al obtenr los datos del usuario")
        }
    }

     fun obtenerTokensDispositivos_trabajador(
        id_registrado: String,
        onSuccess: (Map<String, String>) -> Unit,
        onError: (Exception) -> Unit
    ) {
        val db = FirebaseFirestore.getInstance()
            .collection("Trabajadores_Usuarios_Drivers")
            .document("tokens")
            .collection(id_registrado)
            .document("dispositivos")

        db.get()
            .addOnSuccessListener { documentSnapshot ->
                if (documentSnapshot.exists()) {
                    // ✅ Aquí sí puede leer como mapa correctamente ahora
                    val tokensMap = documentSnapshot.get("tokens") as? Map<String, String>
                    if (tokensMap != null) {
                        onSuccess(tokensMap)
                    } else {
                        onSuccess(emptyMap())
                    }
                } else {
                    onSuccess(emptyMap())
                }
            }
            .addOnFailureListener { exception ->
                onError(exception)
            }
    }

}