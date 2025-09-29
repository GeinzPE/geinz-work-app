package com.geinzz.geinzwork.model

import android.R
import android.util.Log
import com.geinzz.geinzwork.data.model.localizate_geinz.inicio_geinz.datos_principales_user
import com.geinzz.geinzwork.data.model.localizate_geinz.inicio_geinz.localidades_filtrado
import com.geinzz.geinzwork.data.model.localizate_geinz.inicio_geinz.lugares_turisticos
import com.geinzz.geinzwork.data.model.localizate_geinz.inicio_geinz.seguridad_salud_publica
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await

class repo_principal_geinz_work {
    val db = FirebaseFirestore.getInstance()

    suspend fun obtener_lugares_turisticos(localidad: String): List<lugares_turisticos> {
        Log.d("localida_pasada",localidad)
        val lista_lugares = mutableListOf<lugares_turisticos>()
        val lugares_turisticos =
            db.collection("Tiendas").document(localidad.lowercase()).collection("lugares_turisticos")
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
                longitud.toDouble(), lista_categorias
            )
            lista_lugares.add(lista)
        }

        return lista_lugares
    }


    suspend fun obtenerLocalidadesFiltrados(): List<localidades_filtrado> {
        val localidadesSnapshot = db.collection("Tiendas")
            .document("categorias")
            .collection("localidades")
            .get()
            .await()

        return localidadesSnapshot.documents.mapNotNull { doc ->
            val nombre = doc.getString("nombre") ?: return@mapNotNull null
            val listaImg = doc.get("img") as? List<String> ?: emptyList()
            val aniversario = doc.get("aniversario") as? Map<String, Any> ?: emptyMap()
            val dia = aniversario.get("dia") as? Number ?: 0
            val mes = aniversario.get("mes") as? Number ?: 0
            val imgPrincipal = listaImg.randomOrNull() ?: ""
            localidades_filtrado(nombre, listOf(imgPrincipal),dia,mes)
        }
    }




//    suspend fun obtenerDatosUser(idUser: String): datos_principales_user? {
//        val ref = db.collection("Trabajadores_Usuarios_Drivers")
//            .document("users")
//            .collection("users")
//            .document(idUser)
//            .get()
//            .await()
//
//        return if (ref.exists()) {
//            ref.toObject(datos_principales_user::class.java)
//        } else {
//            null
//        }
//    }

//    suspend fun subir_lugares(lista: List<seguridad_salud_publica>) {
//        lista.forEach { i ->
//            val ref = db.collection("Tiendas")
//                .document("salud_seguridad")
//                .collection(i.localidad)
//                .document()
//            val generatedId = ref.id
//
//            val hasmap_normal = hashMapOf<String, Any>(
//                "nombre" to i.nombre,
//                "lugar" to i.localidad,
//                "img" to i.img,
//                "categoria" to i.tipo,
//                "ubicacion" to i.datos_ubi,
//                "numeros_contactos" to i.numero_contacto,
//                "id" to generatedId
//            )
//
//            try {
//                ref.set(hasmap_normal, SetOptions.merge()).await()
//                Log.d("Firestore", "Documento subido con ID: $generatedId")
//
//                val ref2 = db.collection("lugares").document(generatedId)
//                val hashMap_algolia = hashMapOf<String, Any>(
//                    "nombre" to i.nombre,
//                    "lugar" to i.localidad,
//                    "img" to i.img,
//                    "categoria" to i.tipo,
//                    "id_tienda" to generatedId
//                )
//                ref2.set(hashMap_algolia, SetOptions.merge()).await()
//                Log.d("Firestore", "Documento en 'lugares' creado con ID: $generatedId")
//            } catch (e: Exception) {
//                Log.e("Firestore", "Error subiendo documento: ", e)
//            }
//        }
//    }


}