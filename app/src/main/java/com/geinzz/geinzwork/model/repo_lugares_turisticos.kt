package com.geinzz.geinzwork.model

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class repo_lugares_turisticos {
    val db = FirebaseFirestore.getInstance()

    suspend fun obtener_filtrado_lugares(): List<String> {
        val lista_filtrado = mutableListOf<String>()
        val lugares = db.collection("Tiendas")
            .document("categorias")
            .collection("categorias_lugares")
            .document("categorias_lugares_turisticos")
            .get()
            .await()

        if (lugares.exists()) {
            val categoria = lugares.get("categorias") as? List<String> ?: emptyList()
            lista_filtrado.addAll(categoria)
        }

        return lista_filtrado
    }

}