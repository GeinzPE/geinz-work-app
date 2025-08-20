package com.geinzz.geinzwork.model

import com.geinzz.geinzwork.data.model.localizate_geinz.inicio_geinz.lugares_turisticos
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class repo_principal_geinz_work {
    val db = FirebaseFirestore.getInstance()

    suspend fun obtener_lugares_turisticos(localidad: String): List<lugares_turisticos> {
        val lista_lugares = mutableListOf<lugares_turisticos>()
        val lugares_turisticos =
            db.collection("Tiendas").document(localidad).collection("lugares_turisticos")
                .get().await()
        for (datos in lugares_turisticos) {
            val data = datos.data
            val id = data?.get("id") as? String ?: ""
            val titulo = data?.get("titulo") as? String ?: ""
            val descripcion = data?.get("descripcion") as? String ?: ""
            val img_refencia = data?.get("img") as? String ?: ""
            val ubicacion = data?.get("ubicacion") as? Map<String, Any> ?: emptyMap()
            val dirección = ubicacion?.get("dirección") as? String ?: ""
            val referencia = ubicacion?.get("referencia") as? String ?: ""
            val longitud = ubicacion?.get("longitud") as? Number ?: 0
            val latitud = ubicacion?.get("latitud") as? Number ?: 0
            val lista_categorias = data?.get("categoria") as? List<String> ?: emptyList()

            val lista = lugares_turisticos(
                id,
                titulo,
                descripcion,
                img_refencia,
                dirección,
                referencia,
                latitud.toDouble(),
                longitud.toDouble(),lista_categorias
            )
            lista_lugares.add(lista)
        }

        return lista_lugares
    }


}