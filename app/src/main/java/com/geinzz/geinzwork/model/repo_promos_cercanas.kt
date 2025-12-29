package com.geinzz.geinzwork.model

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.geinzz.geinzwork.data.model.data_class_promo_cerca_de_ti.dataclass_promociones_cerca_de_ti
import com.geinzz.geinzwork.data.model.data_class_promo_cerca_de_ti.img_content
import com.geinzz.geinzwork.data.model.data_class_promo_cerca_de_ti.informacion_publcacion
import com.geinzz.geinzwork.data.model.data_class_promo_cerca_de_ti.obj_completo
import com.google.firebase.Firebase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

class repo_promos_cercanas {
    val db = FirebaseFirestore.getInstance()

    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun obtener_promos(
        localidad: String
    ): List<obj_completo> {

        return try {


            Log.d("PROMOS_REPO", "📍 Buscando promos en localidad: $localidad")

            val snapshot = db
                .collection("Tiendas")
                .document(localidad)
                .collection("promos_ofertas")
                .get()
                .await()

            Log.d("PROMOS_REPO", "📦 Documentos encontrados: ${snapshot.size()}")
            snapshot.documents.mapNotNull { doc ->

                // ---------- MAPS ----------
                val infoMap = doc.get("informacion") as? Map<*, *> ?: emptyMap<String, Any>()
                val imgMap = doc.get("img_container") as? Map<*, *> ?: emptyMap<String, Any>()
                val fechas = doc.get("fechas") as? Map<*, *> ?: emptyMap<String, Any>()
                val fecha_fin = fechas["fin"] as? String ?: ""
                // ---------- INFORMACIÓN ----------
                val informacion = informacion_publcacion(
                    descripcion = infoMap["descripcion"] as? String ?: "",
                    numero = infoMap["numero"] as? String ?: "",
                    titulo = infoMap["titulo"] as? String ?: "",
                    nombre_tienda = infoMap["nombre_tienda"] as? String ?: "",
                    id_promocion = doc.id,
                    id_tienda = infoMap["id_tienda"] as? String ?: "",
                    categoria = infoMap["categoria"] as? String?: ""
                )

                // ---------- IMÁGENES ----------
                val img = img_content(
                    logo_img = imgMap["logo_img"] as? String ?: "",
                    lista_img = imgMap["lista_img"] as? List<String> ?: emptyList()
                )

                val promo = dataclass_promociones_cerca_de_ti(
                    informacion_publcacion = informacion,
                    img = img,
                    exclussivo = doc.getBoolean("exclusivo") ?: false,
                    dias_restantes = diasRestantes(fecha_fin)
                )

                // ---------- LISTA DE FILTRO ----------
                val listaFiltrado = listOfNotNull(
                    informacion.categoria,
                )
                Log.d("listreadosat","${ informacion.id_tienda}")
                // ---------- OBJETO FINAL ----------

                obj_completo(
                    dataclass_promociones_cerca_de_ti = promo,
                    lista_filtrado = listaFiltrado
                )

            }

        } catch (e: Exception) {
            emptyList()
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun diasRestantes(fechaFin: String): Int {
        return try {
            if (fechaFin.isBlank()) return 0

            val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
            val hoy = LocalDate.now()
            val fin = LocalDate.parse(fechaFin, formatter)

            ChronoUnit.DAYS.between(hoy, fin).coerceAtLeast(0L).toInt()
        } catch (e: Exception) {
            0
        }
    }


}