package com.geinzz.geinzwork.model

import com.geinzz.geinzwork.data.model.dataclass_novedades.dataclass_novedades_geinz
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.constantes_lista_localidades.to_horario_atencion_box_dia
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class repo_novedades_tiendas_geinz {

    private val db = FirebaseFirestore.getInstance()
    private val PAGE_SIZE = 6L

    suspend fun obtener_tiendas_paginadas(
        localidad: String,
        ultimoDocumento: DocumentSnapshot? = null
    ): Pair<List<dataclass_novedades_geinz>, DocumentSnapshot?> {

        val baseQuery = db.collection("Tiendas")
            .document(localidad)
            .collection("nuevos_lugares")
            .limit(PAGE_SIZE)

        val query = if (ultimoDocumento != null) {
            baseQuery.startAfter(ultimoDocumento)
        } else {
            baseQuery
        }

        val ref = query.get().await()
        val formato = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val hoy = Date()

        val listaTiendas = mutableListOf<dataclass_novedades_geinz>()

        for (datos in ref.documents) {
            val data = datos.data ?: continue

            val fechaMap = data["fecha"] as? Map<String, String> ?: emptyMap()
            val fechaFinString = fechaMap["fecha_fin"]

            // Filtrar tiendas caducadas (no las borramos aquí)
            if (fechaFinString != null) {
                val fechaFin = try { formato.parse(fechaFinString) } catch (e: Exception) { null }
                if (fechaFin != null && hoy.after(fechaFin)) {
                    continue // simplemente ignoramos esta tienda
                }
            }

            val horarioMap = data["horario_atencion"] as? Map<String, Any> ?: emptyMap()
            val horario_atencion_box = horarioMap.to_horario_atencion_box_dia()

            listaTiendas.add(
                dataclass_novedades_geinz(
                    categoria        = data["categoria"]         as? String       ?: "",
                    direccion        = data["direccion"]         as? String       ?: "",
                    horario_atencion = horario_atencion_box,
                    id_tienda        = data["id_tienda"]         as? String       ?: "",
                    logo_img         = data["logo_img"]          as? String       ?: "",
                    nombre_tienda    = data["nombre_tienda"]     as? String       ?: "",
                    lista_subcateogira = data["lista_subcateogira"] as? List<String> ?: emptyList(),
                    descripcion      = data["descripcion"]       as? String       ?: "",
                    localidad_tienda = data["localidad_tienda"]  as? String       ?: "",
                    fecha            = fechaMap
                )
            )
        }

        val nuevoCursor = if (ref.documents.size >= PAGE_SIZE.toInt()) {
            ref.documents.last()
        } else {
            null
        }

        return Pair(listaTiendas, nuevoCursor)
    }
}