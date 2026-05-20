package com.geinzz.geinzwork.model

import android.content.Context
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.runtime.snapshots.SnapshotStateList
import com.geinzz.geinzwork.data.model.dataclass_review.ImagenReview
import com.geinzz.geinzwork.data.model.dataclass_review.data_class_resultado_tienda_lugar
import com.geinzz.geinzwork.data.model.dataclass_review.data_class_review
import com.geinzz.geinzwork.data.model.dataclass_review.datos_review
import com.geinzz.geinzwork.data.model.dataclass_review.datos_review_existenet
import com.geinzz.geinzwork.data.model.localizate_geinz.filtrado_tiendas.horario_tienda
import com.geinzz.geinzwork.herramientas_geinz.constantes.constantes_subir_img_panel_tienda
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.constantes_horas.obtenerProximoDiaAbierto
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.constantes_lista_localidades.verificarSiEstaAbiertoHoy
import com.google.firebase.Firebase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.storage.storage
import kotlinx.coroutines.tasks.await
import java.util.Calendar
import java.util.UUID

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
                    localidad = data_class_review.localida_lugar, estaAbierto, datos_horario_actual
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
                val lista_img_subidas = data?.get("lista_img_url") as? List<String> ?: emptyList()


                // Retorna el objeto con los datos
                datos_review_existenet(
                    calificacion = calificacion,
                    descripcion = descripcion,
                    fecha_realizada = fechaRealizada, lista_img_subidas
                )
            } else {
                datos_review_existenet()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            datos_review_existenet()
        }
    }


    suspend fun agregar_review(
        datos_review: datos_review,
        context: Context,
        list: List<ImagenReview>
    ): Boolean {
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
            Log.d("envaiomodatos", "$hasmap")
            ref.set(hasmap, SetOptions.merge()).await()
            agregar_review_storage(
                context,
                datos_review.id_tienda_lugar,
                datos_review.id_usuario,
                list,
                datos_review.localidad_tienda
            )
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun agregar_review_storage(
        context: Context,
        id_tienda: String,
        id_review: String,
        imagenes: List<ImagenReview>,
        localidad: String,
    ) {
        val storage = Firebase.storage
        val reviewRef = storage.reference
            .child("tiendas/$id_tienda/review/$id_review")

        // 1️⃣ URLs antiguas (ANTES de tocar nada)
        val urlsAntiguas = obtener_urls_antiguas(id_tienda, id_review, localidad)

        // 2️⃣ Mantener imágenes existentes
        val downloadUrls = imagenes
            .filter { it.uri == null && it.url != null }
            .map { it.url!! }
            .toMutableList()

        // 3️⃣ Subir nuevas imágenes
        val nuevasImagenes = imagenes.filter { it.uri != null }

        nuevasImagenes.forEach { imagen ->
            try {
                val bytes = constantes_subir_img_panel_tienda
                    .procesarImagenWebPSinRecorte(context, imagen.uri!!)

                val nombreImagen = UUID.randomUUID().toString()
                val ref = reviewRef.child(nombreImagen)

                ref.putBytes(bytes).await()
                val downloadUrl = ref.downloadUrl.await()

                downloadUrls.add(downloadUrl.toString())

            } catch (e: Exception) {
                Log.e("FirebaseStorage", "❌ Error subiendo imagen", e)
            }
        }

        // 4️⃣ Detectar imágenes eliminadas
        val urlsEliminadas = urlsAntiguas - downloadUrls

        // 5️⃣ Eliminar del Storage
        eliminar_imagenes_storage(urlsEliminadas)

        // 6️⃣ Actualizar Firestore (UNA SOLA VEZ)
        agregar_img_firestore_review(
            id_tienda,
            id_review,
            localidad,
            downloadUrls
        )
    }



    suspend fun agregar_img_firestore_review(
        id_tienda: String,
        id_review: String,
        localidad: String,
        lista: List<String>
    ) {
        val firestore = FirebaseFirestore.getInstance()
            .collection("Tiendas")
            .document(localidad)
            .collection(localidad)
            .document(id_tienda)
            .collection("review")
            .document(id_review)

        val hashMap = hashMapOf<String, Any>(
            "lista_img_url" to lista
        )

        try {

            firestore.update(hashMap).await()

            println("✅ URLs de imágenes actualizadas correctamente en Firestore")
        } catch (e: Exception) {

            println("❌ Error al actualizar Firestore: ${e.message}")
        }
    }

    suspend fun eliminar_imagenes_storage(urls: List<String>) {
        val storage = Firebase.storage

        urls.forEach { url ->
            try {
                val ref = storage.getReferenceFromUrl(url)
                ref.delete().await()
                Log.d("Storage", "🗑 Imagen eliminada: $url")
            } catch (e: Exception) {
                Log.e("Storage", "❌ Error eliminando imagen", e)
            }
        }
    }

    suspend fun obtener_urls_antiguas(
        id_tienda: String,
        id_review: String,
        localidad: String
    ): List<String> {
        val doc = FirebaseFirestore.getInstance()
            .collection("Tiendas")
            .document(localidad)
            .collection(localidad)
            .document(id_tienda)
            .collection("review")
            .document(id_review)
            .get()
            .await()

        return doc.get("lista_img_url") as? List<String> ?: emptyList()
    }


}