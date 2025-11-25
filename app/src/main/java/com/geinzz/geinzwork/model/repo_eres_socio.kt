package com.geinzz.geinzwork.model

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.geinzz.geinzwork.data.model.datos_tienda
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.constantes_horas.timeStampNumero
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.constantes_lista_localidades.to_horario_atencion_box_dia
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.tasks.await
@RequiresApi(Build.VERSION_CODES.O)

class repo_eres_socio {
    private val db = FirebaseFirestore.getInstance()

    fun escuchar_datos_tienda(
        id_tienda: String,
        resultado: (datos_tienda) -> Unit,
        error: (Exception) -> Unit
    ): ListenerRegistration {

        val ref = db.collection("Tiendas")
            .document("barranca")
            .collection("barranca")
            .document(id_tienda)

        return ref.addSnapshotListener { snapshot, e ->
            if (e != null) {
                error(e)
                return@addSnapshotListener
            }

            if (snapshot != null && snapshot.exists()) {

                val data = snapshot.data ?: emptyMap<String, Any>()

                val nombre_tienda = data["nombre_tienda"] as? String ?: ""
                val img_tienda = data["img_tienda"] as? Map<String, Any> ?: emptyMap()
                val logo = img_tienda["logo_tienda"] as? String ?: ""

                val horario_atencion = data["horario_atencion"] as? Map<String, Any> ?: emptyMap()
                val horarioMap = horario_atencion.to_horario_atencion_box_dia()

                // ---- COLECCIÓN DE ESTADÍSTICAS ----
                val estadRef = ref.collection("estadisticas")

                estadRef.get().addOnSuccessListener { col ->

                    fun obtenerTotal(nombreDoc: String): Int {
                        val d = col.documents.find { it.id == nombreDoc }
                        return (d?.get("total") as? Number)?.toInt() ?: 0
                    }

                    val totalVistas = obtenerTotal("vistas")
                    val totalGuardados = obtenerTotal("guardados")
                    val totalClic = obtenerTotal("clic")

                    val fb = obtenerTotal("facebook")
                    val ig = obtenerTotal("instagram")
                    val tk = obtenerTotal("tiktok")
                    val stweb = obtenerTotal("sitio_web")
                    val wsap = obtenerTotal("whatsapp")
                    val llamada = obtenerTotal("llamada")
                    val ruta = obtenerTotal("ruta")

                    resultado(
                        datos_tienda(
                            id_tienda = id_tienda,
                            nombre = nombre_tienda,
                            img_tienda = logo,
                            horario_tiendaMap = horarioMap,

                            total_vista = totalVistas,
                            total_guardados = totalGuardados,
                            clic = totalClic,

                            fb = fb,
                            ig = ig,
                            tk = tk,
                            stweb = stweb,
                            wsap = wsap,
                            llamada = llamada,
                            ruta = ruta
                        )
                    )
                }.addOnFailureListener {
                    error(it)
                }
            }
        }
    }



    suspend fun guardar_horario_cerrado(
        id_tienda: String,
        dia: String,
        motivo: String,
        bloques: List<Map<String, String>>
    ) {
        val db = FirebaseFirestore.getInstance()
            .collection("Tiendas").document("barranca")
            .collection("barranca").document(id_tienda)

        val dataDia = mapOf(
            "bloques" to bloques,
            "cerrado" to true,
            "motivo" to motivo
        )

        val updates = mapOf(
            "timeSlamp" to timeStampNumero(),
            "horario_atencion.${dia.lowercase()}" to dataDia
        )

        try {
            db.update(updates).await()
            Log.d("DB", "Horario de $dia actualizado correctamente (CERRADO).")
        } catch (e: Exception) {
            Log.e("DB", "Error al actualizar horario cerrado", e)
        }
    }


    suspend fun guardar_horario_atencion_abierto(
        id_tienda: String, dia: String, bloques: List<Map<String, String>>
    ) {
        val db = FirebaseFirestore.getInstance()
            .collection("Tiendas").document("barranca")
            .collection("barranca").document(id_tienda)

        val dataDia = mapOf(
            "bloques" to bloques, "cerrado" to false, "motivo" to ""
        )

        val updates = mapOf(
            "timeSlamp" to timeStampNumero(), "horario_atencion.${dia.lowercase()}" to dataDia
        )

        try {
            db.update(updates).await()
            Log.d("DB", "Horario de $dia actualizado correctamente (CERRADO).")
        } catch (e: Exception) {
            Log.e("DB", "Error al actualizar horario cerrado", e)
        }

    }



}