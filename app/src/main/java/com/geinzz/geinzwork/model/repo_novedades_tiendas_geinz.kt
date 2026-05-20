package com.geinzz.geinzwork.model

import com.geinzz.geinzwork.data.model.dataclass_novedades.dataclass_novedades_geinz
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.constantes_lista_localidades.to_horario_atencion_box_dia
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.constantes_lista_localidades.to_metodo_pago
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale

class repo_novedades_tiendas_geinz {

    val db = FirebaseFirestore.getInstance()

    suspend fun obtener_tiendas_real_time(
        localidad: String
    ): List<dataclass_novedades_geinz> {

        val ref = db.collection("Tiendas")
            .document(localidad)
            .collection("nuevos_lugares")
            .get()
            .await()

        val listaTiendas = mutableListOf<dataclass_novedades_geinz>()

        val formato = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val hoy = Date()

        for (datos in ref) {

            val data = datos.data

            val fechaMap = data["fecha"] as? Map<String, String> ?: emptyMap()
            val fechaFinString = fechaMap["fecha_fin"]

            if (fechaFinString != null) {

                val fechaFin = formato.parse(fechaFinString)

                if (fechaFin != null && hoy.after(fechaFin)) {

                    db.collection("Tiendas")
                        .document(localidad)
                        .collection("nuevos_lugares")
                        .document(data["id_tienda"] as String)
                        .delete()

                    continue
                }
            }

            val horarioMap =
                data["horario_atencion"] as? Map<String, Any> ?: emptyMap()

            val horario_atencion_box =
                horarioMap.to_horario_atencion_box_dia()

            listaTiendas.add(
                dataclass_novedades_geinz(
                    categoria = data["categoria"] as? String ?: "",
                    direccion = data["direccion"] as? String ?: "",
                    horario_atencion = horario_atencion_box,
                    id_tienda = data["id_tienda"] as? String ?: "",
                    logo_img = data["logo_img"] as? String ?: "",
                    nombre_tienda = data["nombre_tienda"] as? String ?: "",
                    lista_subcateogira = data["lista_subcateogira"] as? List<String>
                        ?: emptyList(),
                    descripcion = data["descripcion"] as? String ?: "",
                    localidad_tienda = data["localidad_tienda"] as? String ?: "",
                    fecha = fechaMap
                )
            )
        }

        return listaTiendas
    }
}