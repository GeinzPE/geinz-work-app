package com.geinzz.geinzwork.model

import android.util.Log
import com.geinzz.geinzwork.data.model.dataclass_review.data_class_resultado_tienda_lugar
import com.geinzz.geinzwork.data.model.dataclass_review.data_class_review
import com.geinzz.geinzwork.data.model.dataclass_review.datos_review
import com.geinzz.geinzwork.data.model.dataclass_review.datos_review_existenet
import com.geinzz.geinzwork.data.model.localizate_geinz.filtrado_tiendas.horario_tienda
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.constantes_horas.obtenerProximoDiaAbierto
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.constantes_lista_localidades.verificarSiEstaAbiertoHoy
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await
import java.util.Calendar

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
                val horario = data?.get("horario_atencion") as? Map<String, Any> ?: emptyMap()
                val dias =
                    listOf("domingo", "lunes", "martes", "miércoles", "jueves", "viernes", "sábado")
                val calendar = Calendar.getInstance()
                val diaActual = dias[calendar.get(Calendar.DAY_OF_WEEK) - 1]
                val horarioDia = horario[diaActual] as? Map<String, Any> ?: emptyMap()
                val cerrado = horarioDia["cerrado"] as? Boolean ?: false
                val hApertura = horarioDia["h_apertura"] as? String ?: ""
                val hCierre = horarioDia["h_cierre"] as? String ?: ""
                val motivo = horarioDia["motivo"] as? String ?: ""
                var datos_horario_actual = horario_tienda(hApertura, hCierre, cerrado, motivo)
                val estaAbierto =
                    if (!cerrado) verificarSiEstaAbiertoHoy(datos_horario_actual) else false
                if (!estaAbierto) {
                    val proximo = obtenerProximoDiaAbierto(horario, diaActual)
                    if (proximo != null) {
                        val (diaProx, horarioProx) = proximo
                        datos_horario_actual = datos_horario_actual.copy(
                            dia_prox_apertura = diaProx,
                            hora_prox_apertura = horarioProx["h_apertura"] as? String ?: ""
                        )
                    }
                }
                data_class_resultado_tienda_lugar(
                    id = data_class_review.id_tienda_lugar,
                    nombre = nombre_tienda_lugar,
                    imagen = img_LT,
                    localidad = data_class_review.localida_lugar,estaAbierto,datos_horario_actual
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
    ): datos_review_existenet? {
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
                val calificacion = (data?.get("calificacion") as? Number)?.toInt() ?: 0
                val descripcion = data?.get("descripcion") as? String ?: ""
                val fechaRealizada = data?.get("fecha_realizada") as? String ?: ""

                // Retorna el objeto con los datos
                datos_review_existenet(
                    calificacion = calificacion,
                    descripcion = descripcion,
                    fecha_realizada = fechaRealizada
                )
            } else {
                datos_review_existenet()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            datos_review_existenet()
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