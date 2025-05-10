package com.example.geinzwork.constantesGeneral

import android.util.Log
import com.geinzz.geinzwork.constantesGeneral.constantesCarrito
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions

object constantes_nombre_usuarios {

    fun agregar_firestoreNombre_usuario(
        id_registrado: String,
        nombre_user: String,
        tipo_cuento: String
    ) {
        val db = FirebaseFirestore.getInstance()
            .collection("Trabajadores_Usuarios_Drivers")
            .document("nombres_user")
            .collection("nombres_user").document(id_registrado)

        val hashMap = hashMapOf<String, Any>(
            "nombres_user" to "@$nombre_user",
            "id_registrado" to id_registrado,
            "cuenta" to tipo_cuento
        )
        db.set(hashMap, SetOptions.merge()).addOnSuccessListener {
            Log.d("user_agregado", "user agregado correctament")
        }.addOnFailureListener { e ->
            Log.d("error_user", "error al agregar el user")

        }
    }

    fun verificar_existencia_nombre_usuario(
        rawNombreUsuario: String,
        callback: (Boolean) -> Unit
    ) {

        val db = FirebaseFirestore.getInstance()
            .collection("Trabajadores_Usuarios_Drivers")
            .document("nombres_user")
            .collection("nombres_user")

        db.whereEqualTo("nombres_user", rawNombreUsuario)
            .get()
            .addOnSuccessListener { res ->
                callback(!res.isEmpty) // true si existe, false si no
            }
            .addOnFailureListener { e ->
                println("Error al verificar nombre de usuario: $e")
                callback(false)
            }
    }

    fun actualizar_nombre_usuario(
        id_user: String,
        rawNombreUsuario: String,
    ) {
        val db = FirebaseFirestore.getInstance()
            .collection("Trabajadores_Usuarios_Drivers")
            .document("nombres_user")
            .collection("nombres_user").document(id_user)
        val hashMap = hashMapOf<String, Any>(
            "nombres_user" to rawNombreUsuario,
            "editado" to true,
           "fecha_edicion" to constantesCarrito.obtenerFechaActual()
        )
        db.set(hashMap, SetOptions.merge()).addOnSuccessListener { res ->
            Log.d("nombre_user","Nombre de usuario cambiado correctamente")
        }.addOnFailureListener { e->
            Log.d("nombre_user","Ocurio un errro al cambiar el nobreUSer")
        }
    }
}