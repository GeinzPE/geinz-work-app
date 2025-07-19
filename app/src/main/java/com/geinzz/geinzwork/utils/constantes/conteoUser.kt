package com.geinzz.geinzwork.utils.constantes.constantes

import com.google.firebase.firestore.FirebaseFirestore

object conteoUser {
    fun obtenerConteoUSer(USerRegistrados: (Int) -> Unit) {
        val firestore = FirebaseFirestore.getInstance()
        val dbTrabajadores = firestore.collection(Variables.trabajadores_usuariosDB)
            .document(Variables.trabajadoresDB)
            .collection(Variables.trabajadoresDB)
        val dbUsuarios = firestore.collection(Variables.trabajadores_usuariosDB)
            .document(Variables.usuarios_db)
            .collection(Variables.usuarios_db)

        var totalUsuarios = 0
        dbTrabajadores.get()
            .addOnSuccessListener { trabajadoresSnapshot ->
                val trabajadoresCount = trabajadoresSnapshot.size()
                totalUsuarios += trabajadoresCount
                dbUsuarios.get()
                    .addOnSuccessListener { usuariosSnapshot ->
                        val usuariosCount = usuariosSnapshot.size()

                        totalUsuarios += usuariosCount

                        USerRegistrados(totalUsuarios)
                    }
                    .addOnFailureListener { e ->
                        println("Error al obtener el conteo de usuarios: ${e.message}")
                    }
            }
            .addOnFailureListener { e ->
                println("Error al obtener el conteo de trabajadores: ${e.message}")
            }
    }

}