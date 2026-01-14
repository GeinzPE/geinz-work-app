package com.geinzz.geinzwork.model

import android.os.Build
import androidx.annotation.RequiresApi
import com.geinzz.geinzwork.data.model.datos_tienda
import com.geinzz.geinzwork.data.model.widget_tienda
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.constantes_horas.DiaHoy
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.constantes_lista_localidades.to_horario_atencion_box_dia
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.storage.FirebaseStorage
import io.ktor.client.plugins.cache.storage.FileStorage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class repo_carga_img_general {
    val db = FirebaseFirestore.getInstance()
    val storage = FirebaseStorage.getInstance()

    @RequiresApi(Build.VERSION_CODES.O)
    val repo_eres_socio= repo_eres_socio()


    suspend fun obtenerUrlsCarga(): List<String> = suspendCoroutine { continuation ->
        val folderRef = storage.reference.child("walpaper_geinz/emergencia")

        folderRef.listAll()
            .addOnSuccessListener { listResult ->
                val items = listResult.items
                val total = items.size
                val urls = mutableListOf<String>()

                if (total == 0) {
                    continuation.resume(emptyList())
                    return@addOnSuccessListener
                }

                items.forEach { item ->
                    item.downloadUrl.addOnSuccessListener { uri ->
                        urls.add(uri.toString())

                        if (urls.size == total) {
                            continuation.resume(urls)
                        }
                    }.addOnFailureListener {
                        // si falla una, igual seguimos
                        urls.add("ERROR")
                        if (urls.size == total) {
                            continuation.resume(urls)
                        }
                    }
                }
            }
            .addOnFailureListener {
                continuation.resume(emptyList())
            }
    }

    suspend fun obtenerUrlsCarga_lugares_turisticos(): List<String> =
        suspendCoroutine { continuation ->
            val folderRef = storage.reference.child("walpaper_geinz/fondos_carga")

            folderRef.listAll()
                .addOnSuccessListener { listResult ->
                    val items = listResult.items
                    val total = items.size
                    val urls = mutableListOf<String>()

                    if (total == 0) {
                        continuation.resume(emptyList())
                        return@addOnSuccessListener
                    }

                    items.forEach { item ->
                        item.downloadUrl.addOnSuccessListener { uri ->
                            urls.add(uri.toString())

                            if (urls.size == total) {
                                continuation.resume(urls)
                            }
                        }.addOnFailureListener {
                            // si falla una, igual seguimos
                            urls.add("ERROR")
                            if (urls.size == total) {
                                continuation.resume(urls)
                            }
                        }
                    }
                }
                .addOnFailureListener {
                    continuation.resume(emptyList())
                }
        }

    @RequiresApi(Build.VERSION_CODES.O)
    val horario=DiaHoy()
    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun obtener_datos_tienda(
        id_tienda: String,
        localidad: String,
        resultado: (widget_tienda) -> Unit,
        error: (Exception) -> Unit,
    ): ListenerRegistration {

        // StateFlow que se actualizará en tiempo real desde fechaFinTiendaPanel
        val fechaFinFlow = MutableStateFlow("")

        val listenerFecha = repo_eres_socio.fechaFinTiendaPanel(id_tienda, localidad) { fecha ->
            fechaFinFlow.value = fecha ?: ""
        }

        val ref = db.collection("Tiendas")
            .document(localidad)
            .collection(localidad)
            .document(id_tienda)

        // Listener de Firestore para los datos de la tienda
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
                val total_puntos = data["puntos_tienda"] as? Number ?: 0
                val categoria_tienda = data["categoria_tienda"] as? String ?: ""
                val localidad_tienda = data["localidad"] as? String ?: ""
                val horarioMap = horario_atencion.to_horario_atencion_box_dia()

                // Emitimos el widget con el StateFlow
                resultado(
                    widget_tienda(
                        total_puntos = total_puntos.toString(),
                        dia_hoy = horario,
                        id_tienda = id_tienda,
                        nombre_tienda = nombre_tienda,
                        img_tienda = logo,
                        horario_tiendaMap = horarioMap,
                        localidad_tienda,
                        categoria_tienda,
                        fecha_fin_panel = fechaFinFlow // <-- pasamos el StateFlow
                    )
                )
            }
        }
    }
}