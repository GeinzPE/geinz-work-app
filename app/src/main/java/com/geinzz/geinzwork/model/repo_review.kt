package com.geinzz.geinzwork.model

import android.util.Log
import com.geinzz.geinzwork.data.model.dataclass_review.data_class_resultado_tienda_lugar
import com.geinzz.geinzwork.data.model.dataclass_review.data_class_review
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class repo_review {
    val db = FirebaseFirestore.getInstance()


    suspend fun obtener_datos_tienda(
        data_class_review: data_class_review
    ): data_class_resultado_tienda_lugar? {
        Log.d("data_class_review",data_class_review.toString())
        return try {
            val ref = db.collection("Tiendas")
                .document(data_class_review.localida_lugar)
                .collection(data_class_review.localida_lugar)
                .document(data_class_review.id_tienda_lugar)
                .get()
                .await()

            if (ref.exists()) {
                val data = ref.data
                val nombre_tienda_lugar = data?.get("nombre_tienda") as? String ?: ""
                val img_nombre_lugar = data?.get("img_tienda") as? Map<String, Any> ?: emptyMap()
                val img_LT=img_nombre_lugar.get("logo_tienda")as? String?:""

                data_class_resultado_tienda_lugar(
                    id = data_class_review.id_tienda_lugar,
                    nombre = nombre_tienda_lugar,
                    imagen = img_LT,
                    localidad = data_class_review.localida_lugar
                )
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }

}