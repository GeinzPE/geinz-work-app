package com.geinzz.geinzwork.utils.constantes.constantes

import android.util.Log
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

object constantes_agregar_estadisticas_publicaiones {

    fun agregarCantidadClikAnuncios(documento: String, update: String) {
        val db = FirebaseFirestore.getInstance().collection("noticias").document(documento)

        db.update(update, FieldValue.increment(1))
            .addOnSuccessListener {
                Log.d("Firestore", "Click count updated successfully")
            }
            .addOnFailureListener { e ->
                Log.w("Firestore", "Error updating click count", e)

            }
    }
    fun decrementarClick(documento: String, update: String) {
        val db = FirebaseFirestore.getInstance().collection("noticias").document(documento)

        db.update(update, FieldValue.increment(-1))
            .addOnSuccessListener {
                Log.d("Firestore", "Click count updated successfully")
            }
            .addOnFailureListener { e ->
                Log.w("Firestore", "Error updating click count", e)

            }
    }
}