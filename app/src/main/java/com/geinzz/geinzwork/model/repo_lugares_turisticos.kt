package com.geinzz.geinzwork.model

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class repo_lugares_turisticos {
    val db = FirebaseFirestore.getInstance()

    suspend fun obtener_lugares_turisticos(localidad: String) {
        val lugares = db.collection("Tiendas").document(localidad).collection("lugares_turisticos").get().await()
        for(lugar in lugares){
        }
    }
}