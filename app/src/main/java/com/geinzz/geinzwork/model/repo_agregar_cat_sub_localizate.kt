package com.geinzz.geinzwork.model

import android.util.Log
import com.geinzz.geinzwork.data.model.localizate_geinz.dataclass_cat_sub
import com.geinzz.geinzwork.data.model.localizate_geinz.dataclass_cat_sub_lista_cat
import com.geinzz.geinzwork.data.model.localizate_geinz.encontradas_por_categoria
import com.geinzz.geinzwork.data.model.localizate_geinz.filtrado_tiendas.tiendas_filtradas
import com.geinzz.geinzwork.data.model.localizate_geinz.horario_Dia
import com.geinzz.geinzwork.data.model.localizate_geinz.inicio_geinz.localidades_filtrado
import com.geinzz.geinzwork.data.model.localizate_geinz.tiendas_patrocinadas
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.constantes_lista_localidades
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.tasks.await

class repo_agregar_cat_sub_localizate {

    val db = FirebaseFirestore.getInstance()

    suspend fun obtener_tiendas_categorias_activas_registradas(filtrado_localidad: String): List<encontradas_por_categoria> {
        val lista_activos_registrados_categoria = mutableListOf<encontradas_por_categoria>()
        val lista = obtener_categorias_subcategorias(false)
        lista.forEach { i ->
            val nombre_categoria = i.nombre.toString()
            val activos_por_localidad = obtenerTiendas_registradas_activas_por_categoria(
                filtrado_localidad,
                nombre_categoria, i.lista_subcategorias, i.lista_img
            )
            activos_por_localidad.forEach { i ->
                val datos =
                    encontradas_por_categoria(
                        i.cantidad_registradas,
                        i.activas,
                        i.categoria,
                        i.subcateogiras, i.img_subcategorias
                    )
                lista_activos_registrados_categoria.add(datos)
            }
        }
        return lista_activos_registrados_categoria
    }

    suspend fun obtener_categorias_subcategorias(solo5: Boolean = true): List<dataclass_cat_sub> {
        val lista = mutableListOf<dataclass_cat_sub>()
        val categoriasRef = db.collection("Tiendas")
            .document("categorias")
            .collection("categorias")

        val snapshot = categoriasRef.get().await()
        lista.clear()

        for (cate in snapshot.documents) {
            val subcategoriasDoc = categoriasRef.document(cate.id).get().await()
            if (subcategoriasDoc.exists()) {
                val data = subcategoriasDoc.data
                val subcategorias = data?.get("subcategorias") as? List<String> ?: emptyList()
                val img_data = data?.get("img_categoria") as? String ?: ""
                val datos = dataclass_cat_sub(cate.id.lowercase(), subcategorias, img_data)
                lista.add(datos)
            }
        }

        // Buscar si existe "comida y restaurantes"
        val comida = lista.firstOrNull { it.nombre.equals("comida y restaurantes", ignoreCase = true) }

        // Resto de categorías sin la de comida
        val resto = lista.filterNot { it.nombre.equals("comida y restaurantes", ignoreCase = true) }
            .shuffled()

        val resultado = mutableListOf<dataclass_cat_sub>()
        comida?.let { resultado.add(it) }  // primero comida
        resultado.addAll(resto)            // después el resto

        return if (solo5) resultado.take(5) else resultado
    }

    suspend fun obtener_subcategoiras(): List<dataclass_cat_sub_lista_cat> = coroutineScope {
        val categoriasRef = db.collection("Tiendas")
            .document("categorias")
            .collection("categorias")

        val snapshot = categoriasRef.get().await()

        val listaDeferred = snapshot.documents.map { doc ->
            async {
                val data = doc.data
                val subcategorias = data?.get("subcategorias") as? List<String> ?: emptyList()
                dataclass_cat_sub_lista_cat(doc.id.lowercase(), subcategorias)
            }
        }

        listaDeferred.awaitAll().also { lista ->
            lista.forEach { Log.d("obtnermosdaotssss", it.toString()) }
        }
    }





    suspend fun obtenerTiendas_registradas_activas_por_categoria(
        categoria_filtrada_localidad: String,
        categoria_filtrada: String,
        listaSubcategorias: List<String>?,
        listaImg: String
    ): List<encontradas_por_categoria> {
        val lista_encotrado = mutableListOf<encontradas_por_categoria>()
        val categoria = categoria_filtrada_localidad.lowercase()
        val collectionTiendas = db
            .collection("Tiendas")
            .document(categoria)
            .collection(categoria)

        val snapshot = collectionTiendas.get().await()

        val coincidenciasCategoria = snapshot.filter { doc ->
            doc.getString("categoria_tienda") == categoria_filtrada
        }
        val cantidadRegistradas = coincidenciasCategoria.size
        var cantidadActivas = 0

        for (datos in coincidenciasCategoria) {
            val id_tienda = datos.getString("id_tienda") ?: continue
            val horarioSnapshot = collectionTiendas.document(id_tienda)
                .collection("horario_atencion")
                .document("horario_atencion")
                .get()
                .await()
            val dias_sema = constantes_lista_localidades.dias_sema
            val lista_horario_por_tienda = mutableListOf<horario_Dia>()

            for (dias in dias_sema) {

                val diaMap = horarioSnapshot.get(dias) as? Map<*, *>
                val h_apertura = diaMap?.get("h_apertura") as? String ?: "Sin horario"
                val h_cierre = diaMap?.get("h_cierre") as? String ?: "Sin horario"
                horario_Dia(dias, h_apertura, h_cierre)
                val datos = horario_Dia(dias, h_apertura, h_cierre)
                lista_horario_por_tienda.add(datos)
            }

            Log.d("temonos_teindas", lista_horario_por_tienda.toString())
            val tienda_activa =
                constantes_lista_localidades.verificarSiEstaAbierto(lista_horario_por_tienda)
            if (tienda_activa) {
                cantidadActivas++
            }

        }
        // Agregar solo una vez por categoría
        val resultado = encontradas_por_categoria(
            cantidad_registradas = cantidadRegistradas,
            activas = cantidadActivas,
            categoria = categoria_filtrada, listaSubcategorias!!, listaImg
        )
        lista_encotrado.add(resultado)


        return lista_encotrado
    }


    suspend fun obtenerTiendasPatrocinadas(
        localidadSeleccionada: String,
        categoriaSeleccionada: String
    ): List<tiendas_patrocinadas> {
        return try {
            val snapshot = db.collection("Tiendas")
                .document(localidadSeleccionada)
                .collection("patrocinadas")
                .whereEqualTo("categoria", categoriaSeleccionada)
                .get()
                .await()

            snapshot.mapNotNull { doc ->
                val categoria = doc.getString("categoria") ?: return@mapNotNull null
                val idTienda = doc.getString("id_tienda") ?: return@mapNotNull null
                tiendas_patrocinadas(
                    categoria_tienda = categoria,
                    id_tienda = idTienda,
                )
            }

        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    suspend fun obtener_datos_tiendas_patrocindas(
        localidadSeleccionada: String,
        categoriaSeleccionada: String
    ): List<tiendas_filtradas> = coroutineScope {
        val trabajos =
            obtenerTiendasPatrocinadas(localidadSeleccionada, categoriaSeleccionada).map { tienda ->
                async {
                    try {
                        val doc = db.collection("Tiendas")
                            .document(localidadSeleccionada)
                            .collection(localidadSeleccionada)
                            .document(tienda.id_tienda ?: "")
                            .get().await()

                        val ubicacion = doc.get("ubicacion") as? Map<String, Any>
                        val direccion = ubicacion?.get("dirección") as? String ?: ""
                        val latitud = ubicacion?.get("latitud") as? Number ?: 0
                        val longitud = ubicacion?.get("longitud") as? Number ?: 0
                        val referencia = ubicacion?.get("referencia") as? String ?: ""
                        val map_img_tienda =
                            doc.get("img_tienda") as? Map<String, Any> ?: emptyMap()
                        val logo_tienda = map_img_tienda.get("logo_tienda") as? String ?: ""
                        val lista_img_tienda =
                            map_img_tienda.get("lista_img") as? List<String> ?: emptyList()


                        val mapMetodoContacto =
                            doc.get("metodo_contacto") as? Map<String, Any> ?: emptyMap()

                        val (estadoFb, nombreFb) = constantes_lista_localidades.obtenerMetodoContacto(
                            "facebook",
                            mapMetodoContacto
                        )
                        val (estadoIg, nombreIg) = constantes_lista_localidades.obtenerMetodoContacto(
                            "instagram",
                            mapMetodoContacto
                        )
                        val (estadoTk, nombreTk) = constantes_lista_localidades.obtenerMetodoContacto(
                            "tiktok",
                            mapMetodoContacto
                        )
                        val (estadoWa, numeroWa) = constantes_lista_localidades.obtenerMetodoContacto(
                            "whatsapp",
                            mapMetodoContacto
                        )
                        val (estadoWeb, urlWeb) = constantes_lista_localidades.obtenerMetodoContacto(
                            "sitio_web",
                            mapMetodoContacto
                        )



                        tiendas_filtradas(
                            logo_tienda,
                            lista_img_tienda,
                            doc.getString("nombre_tienda") ?: "",
                            direccion,
                            referencia,
                            latitud.toDouble(),
                            longitud.toDouble(),
                            doc.get("subcategoria") as? List<String> ?: emptyList(),
                            doc.getString("descripcion") ?: "",
                            doc.getString("id_tienda") ?: "",
                            estadoWa,
                            numeroWa,
                            estadoTk,
                            nombreTk,
                            estadoWeb,
                            urlWeb, estadoIg, nombreIg, estadoFb, nombreFb
                        )
                    } catch (e: Exception) {
                        null
                    }
                }
            }

        trabajos.awaitAll().filterNotNull()
    }




}