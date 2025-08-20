package com.geinzz.geinzwork.model

import com.geinzz.geinzwork.data.model.localizate_geinz.inicio_geinz.lugares_turisticos
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class repo_lugares_turisticos {
    val db = FirebaseFirestore.getInstance()

    suspend fun obtener_filtrado_lugares(): List<String> {
        val lista_filtrado = mutableListOf<String>()
        val lugares = db.collection("Tiendas")
            .document("categorias")
            .collection("categorias_lugares")
            .document("categorias_lugares_turisticos")
            .get()
            .await()

        if (lugares.exists()) {
            val categoria = lugares.get("categorias") as? List<String> ?: emptyList()
            lista_filtrado.addAll(categoria)
        }

        return lista_filtrado
    }

    suspend fun obtener_lugares_turisticos_filtrados(
        localidad: String,
        subcategoria: String
    ): List<lugares_turisticos> {
        val lugares_filtrados = db.collection("Tiendas")
            .document(localidad)
            .collection("lugares_turisticos")
            .whereArrayContains("categoria", subcategoria)
            .get()
            .await()

        return lugares_filtrados.mapNotNull { doc ->
            try {
                val id = doc.getString("id") ?: ""
                val titulo = doc.getString("titulo") ?: ""
                val descripcion = doc.getString("descripcion") ?: ""
                val imgReferencia = doc.getString("img") ?: ""
                val lista_categorias = doc?.get("categoria") as? List<String> ?: emptyList()


                val ubicacion = doc.get("ubicacion") as? Map<*, *> ?: emptyMap<String, Any>()
                val direccion = ubicacion["dirección"] as? String ?: ""
                val referencia = ubicacion["referencia"] as? String ?: ""
                val longitud = (ubicacion["longitud"] as? Number)?.toDouble() ?: 0.0
                val latitud = (ubicacion["latitud"] as? Number)?.toDouble() ?: 0.0

                lugares_turisticos(
                    id_lugar_turistico = id,
                    titulo = titulo,
                    descripcion = descripcion,
                    img_ref = imgReferencia,
                    direcccion = direccion,
                    referencia = referencia,
                    latitud = latitud,
                    longitud = longitud,
                    subcategoria_filtrado = lista_categorias
                )
            } catch (e: Exception) {
                null
            }
        }
    }


}