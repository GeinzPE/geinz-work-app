package com.geinzz.geinzwork.model

import android.util.Log

import com.geinzz.geinzwork.data.model.localizate_geinz.filtrado_tiendas.filtrado_tiendas_cat_sub
import com.geinzz.geinzwork.data.model.localizate_geinz.filtrado_tiendas.tiendas_filtradas
import com.geinzz.geinzwork.data.model.localizate_geinz.filtrado_tiendas.tiendas_por_categoria
import com.geinzz.geinzwork.data.model.localizate_geinz.horario_tienda
import com.geinzz.geinzwork.data.model.localizate_geinz.modelo_tienda
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class repo_filtrado_tiendas {
    val db = FirebaseFirestore.getInstance()

    suspend fun obtener_subcategorias_tiendas(categorias: String): List<filtrado_tiendas_cat_sub> {
        val lista_cat_subcategoria = mutableListOf<filtrado_tiendas_cat_sub>()
        val subcategorias_ref =
            db.collection("Tiendas").document("categorias").collection("categorias")
                .document(categorias).get().await()


        if (subcategorias_ref.exists()) {
            val data = subcategorias_ref.data
            val subcategories = data?.get("subcategorias") as? List<String> ?: emptyList()
            lista_cat_subcategoria.add(filtrado_tiendas_cat_sub(categorias, subcategories))
        }
        return lista_cat_subcategoria
    }

    suspend fun obtenerTiendasFiltradas(
        localidad: String,
        categoria: String
    ): List<tiendas_por_categoria> {
        val lista_tiendas_filtradas = mutableListOf<tiendas_por_categoria>()
        try {
            val tiendas = db.collection("Tiendas")
                .document(localidad)
                .collection(localidad)
                .whereEqualTo("categoria_tienda", categoria)
                .get()
                .await()

            tiendas.forEach { i ->
                val subcategorias_list = i.get("subcategoria") as? List<String> ?: emptyList()
                val ubicacion = i.get("ubicacion") as? Map<String, Any>
                val direccion = ubicacion?.get("dirección") as? String ?: ""
                val referencia = ubicacion?.get("referencia") as? String ?: ""
                val descripcion = i.get("descripcion") as? String ?: ""
                val id_tienda = i.get("id_tienda") as? String ?: ""
                val map_img_tienda = i.get("img_tienda") as? Map<String, Any> ?: emptyMap()
                val logo_tienda = map_img_tienda.get("logo_tienda") as? String ?: ""

                lista_tiendas_filtradas.add(
                    tiendas_por_categoria(
                        nombre_tienda = i.get("nombre_tienda") as? String ?: "",
                        direccion = direccion,
                        referencia = referencia,
                        logo_tienda = logo_tienda,
                        lista_subcategoiras = subcategorias_list,
                        descripcion = descripcion,
                        id_tienda = id_tienda
                    )
                )

            }

        } catch (e: Exception) {
            Log.e("Firestore", "Error al obtener tiendas filtradas", e)
        }
        return lista_tiendas_filtradas
    }

    suspend fun obtenner_campos_tiendas_espesifica(
        localidad: String,
        id_tienda: String
    ): List<modelo_tienda> {
        val lista_modelo_tienda = mutableListOf<modelo_tienda>()
        val tienda =
            db.collection("Tiendas").document(localidad).collection(localidad).document(id_tienda)
                .get().await()
        if (tienda.exists()) {
            val data = tienda.data
            val map_img = data?.get("img_tienda") as? Map<String, Any> ?: emptyMap()
            val tiendaModelo = modelo_tienda(
                categoria_tienda = data?.get("categoria_tienda") as? String ?: "",
                descripcion = data?.get("descripcion") as? String ?: "",
                id_tienda = data?.get("id_tienda") as? String ?: "",
                img_perfil = map_img.get("logo_tienda") as? String ?: "",
                lista_img = map_img.get("lista_img") as? List<String> ?: emptyList(),
                localidad = data?.get("localidad") as? String ?: "",
                modelo_negocio = data?.get("modelo_negocio") as? Boolean ?: false,
                nombre_tienda = data?.get("nombre_tienda") as? String ?: "",
                subcategoria = data?.get("subcategoria") as? List<String> ?: emptyList(),
                ubicacion = data?.get("ubicacion") as? Map<String, Any> ?: emptyMap(),
                metodo_contacto = data?.get("metodo_contacto") as? Map<String, Any> ?: emptyMap()
            )
            lista_modelo_tienda.add(tiendaModelo)
        }
        return lista_modelo_tienda

    }

    suspend fun obtenerHorarioPorTienda(idTienda: String, localidad: String): List<horario_tienda> {
        val listaDias =
            listOf("lunes", "martes", "miércoles", "jueves", "viernes", "sábado", "domingo")
        val listaHorarios = mutableListOf<horario_tienda>()

        val tiendaSnapshot = db.collection("Tiendas")
            .document(localidad)
            .collection(localidad)
            .document(idTienda)
            .collection("horario_atencion")
            .document("horario_atencion")
            .get()
            .await()

        if (tiendaSnapshot.exists()) {
            val data = tiendaSnapshot.data ?: emptyMap()

            listaDias.forEach { dia ->
                val infoDia = data[dia] as? Map<*, *>
                val h_apertura = infoDia?.get("h_apertura") as? String ?: ""
                val h_cierre = infoDia?.get("h_cierre") as? String ?: ""
                listaHorarios.add(horario_tienda(idTienda, dia, h_apertura, h_cierre))
            }
        }
        return listaHorarios
    }


    suspend fun obtener_tiendas_por_subcateogira(
        subcategoria: String,
        localidad: String
    ): List<tiendas_por_categoria> {
        val lista_por_subcateogira = mutableListOf<tiendas_por_categoria>()
        val datos_tienda = db.collection("Tiendas").document(localidad).collection(localidad)
            .whereArrayContains("subcategoria", subcategoria).get().await()
        for (document in datos_tienda.documents) {
            val data = document.data
            val ubicacion = data?.get("ubicacion") as? Map<String, Any>
            val map_img_tienda = data?.get("img_tienda") as? Map<String, Any> ?: emptyMap()
            lista_por_subcateogira.add(
                tiendas_por_categoria(
                    nombre_tienda = data?.get("nombre_tienda") as? String ?: "",
                    direccion = ubicacion?.get("dirección") as? String ?: "",
                    referencia = ubicacion?.get("referencia") as? String ?: "",
                    logo_tienda = map_img_tienda.get("logo_tienda") as? String ?: "",
                    lista_subcategoiras = data?.get("subcategoria") as? List<String> ?: emptyList(),
                    descripcion = data?.get("descripcion") as? String ?: "",
                    id_tienda = data?.get("id_tienda") as? String ?: ""
                )
            )

        }
        return lista_por_subcateogira
    }
}