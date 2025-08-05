package com.geinzz.geinzwork.model

import android.util.Log
import com.geinzz.geinzwork.data.model.localizate_geinz.dataclass_cat_sub
import com.geinzz.geinzwork.data.model.localizate_geinz.dataclass_horarios_atencion_tiendas
import com.geinzz.geinzwork.data.model.localizate_geinz.filtrado_tiendas.filtrado_tiendas_cat_sub
import com.geinzz.geinzwork.data.model.localizate_geinz.filtrado_tiendas.tiendas_filtradas
import com.geinzz.geinzwork.data.model.localizate_geinz.horario_tienda
import com.geinzz.geinzwork.data.model.localizate_geinz.modelo_tienda
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
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
    ): List<tiendas_filtradas> {
        val lista_tiendas_filtradas = mutableListOf<tiendas_filtradas>()
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
                val latitud = ubicacion?.get("latitud") as? Number ?: 0
                val longitud = ubicacion?.get("longitud") as? Number ?: 0
                val referencia = ubicacion?.get("referencia") as? String ?: ""
                val descripcion = i.get("descripcion") as? String ?: ""
                val id_tienda = i.get("id_tienda") as? String ?: ""


                lista_tiendas_filtradas.add(
                    tiendas_filtradas(
                        i.get("img_perfil") as? String ?: "",
                        i.get("nombre_tienda") as? String ?: "",
                        direccion,
                        referencia,
                        latitud.toDouble(),
                        longitud.toDouble(), subcategorias_list, descripcion, id_tienda
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
            val tiendaModelo = modelo_tienda(
                categoria_tienda = data?.get("categoria_tienda") as? String ?: "",
                descripcion = data?.get("descripcion") as? String ?: "",
                id_tienda = data?.get("id_tienda") as? String ?: "",
                img_perfil = data?.get("img_perfil") as? String ?: "",
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
        val listaDias = listOf("lunes", "martes", "miercoles", "jueves", "viernes", "sabado", "domingo")
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
                listaHorarios.add(horario_tienda(idTienda,dia, h_apertura, h_cierre))
            }
        }
        return listaHorarios
    }


}