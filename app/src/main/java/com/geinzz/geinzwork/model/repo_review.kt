package com.geinzz.geinzwork.model

import android.util.Log
import com.geinzz.geinzwork.data.model.dataclass_review.data_class_resultado_tienda_lugar
import com.geinzz.geinzwork.data.model.dataclass_review.data_class_review
import com.geinzz.geinzwork.data.model.dataclass_review.datos_review
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await

class repo_review {
    val db = FirebaseFirestore.getInstance()


    suspend fun obtener_datos_tienda(
        data_class_review: data_class_review
    ): data_class_resultado_tienda_lugar? {
        Log.d("data_class_review", data_class_review.toString())
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
                val img_LT = img_nombre_lugar.get("logo_tienda") as? String ?: ""

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

    suspend fun verificar_review_exsitente(
        id_user: String,
        data_class_review: data_class_review
    ): Pair<Int, String>? {
        return try {
            val ref = db.collection("Tiendas")
                .document(data_class_review.localida_lugar)
                .collection(data_class_review.localida_lugar)
                .document(data_class_review.id_tienda_lugar)
                .collection("review")
                .document(id_user)
                .get()
                .await()

            if (ref.exists()) {
                val data = ref.data
                val calificacion = data?.get("calificacion") as? Number ?: 0
                val descripcion = data?.get("descripcion") as? String ?: ""
                Pair(calificacion.toInt(), descripcion)
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }



    suspend fun agregar_review(datos_review: datos_review): Boolean {
        return try {
            val ref = db.collection("Tiendas").document(datos_review.localidad_tienda)
                .collection(datos_review.localidad_tienda).document(datos_review.id_tienda_lugar)
                .collection("review").document(datos_review.id_usuario)
            val idCreado = ref.id

            val hasmap = hashMapOf<String, Any>(
                "id_review" to idCreado,
                "id_user" to datos_review.id_usuario,
                "verificado_presencial" to datos_review.verificado_presencial,
                "calificacion" to datos_review.cantidad_Strar,
                "descripcion" to datos_review.descripcion_review,
                "hora_realizada" to datos_review.hora,
                "fecha_realizada" to datos_review.fecha
            )
            ref.set(hasmap, SetOptions.merge()).await()
            true
        } catch (e: Exception) {
            false
        }
    }


}