package com.geinzz.geinzwork.aloglia

import Item
import Resultado_sub_cat
import android.util.Log
import com.algolia.search.client.ClientSearch
import com.algolia.search.model.APIKey
import com.algolia.search.model.ApplicationID
import com.algolia.search.model.Attribute
import com.algolia.search.model.IndexName
import com.algolia.search.model.search.Query
import com.geinzz.geinzwork.model.CategoryWithSubcategories
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.doubleOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

class AlgoliaHelper(
    appId: String,
    apiKey: String,
    indexName: String
) {
    private val client = ClientSearch(ApplicationID(appId), APIKey(apiKey))
    private val index = client.initIndex(IndexName(indexName))

    suspend fun search(queryText: String, categoria: String, localidad: String): List<Item> =
        withContext(Dispatchers.IO) {
            Log.d(
                "AlgoliaHelper",
                "🔎 search() llamada con query='$queryText', categoria='$categoria', localidad='$localidad'"
            )

            try {
                val query = Query(queryText.takeIf { it.isNotEmpty() } ?: "").apply {
                    hitsPerPage = 50

                    restrictSearchableAttributes = listOf(
                        Attribute("nombre"),
                        Attribute("tag"),
                        Attribute("lugar"),
                        Attribute("categoria")
                    )

                    val filtros = mutableListOf<List<String>>()

                    if (categoria.isNotEmpty() && categoria != "Todos") {
                        filtros.add(listOf("categoria:$categoria"))
                        Log.d("AlgoliaHelper", "✅ Filtro aplicado: categoria=$categoria")
                    }
                    if (localidad.isNotEmpty()) {
                        filtros.add(listOf("lugar:${localidad.lowercase()}"))
                        Log.d("AlgoliaHelper", "✅ Filtro aplicado: localidad=$localidad")
                    }

                    if (filtros.isNotEmpty()) {
                        facetFilters = filtros
                        Log.d("AlgoliaHelper", "📌 facetFilters aplicados: $filtros")
                    } else {
                        Log.d("AlgoliaHelper", "⚠️ Sin filtros de categoría ni localidad.")
                    }
                }

                val response = index.search(query)
                Log.d("AlgoliaHelper", "📦 Algolia devolvió ${response.hits.size} hits")

                val items = response.hits.mapNotNull { hit ->
                    val json = hit.json.jsonObject
                    val nombre = json["nombre"]?.jsonPrimitive?.content ?: return@mapNotNull null
                    val lugar = json["lugar"]?.jsonPrimitive?.content ?: return@mapNotNull null
                    val categoriaJson =
                        json["categoria"]?.jsonPrimitive?.content ?: return@mapNotNull null
                    val img = json["img"]?.jsonPrimitive?.content ?: return@mapNotNull null
                    val id_tienda =
                        json["id_tienda"]?.jsonPrimitive?.content ?: return@mapNotNull null
                    val tags = json["tag"]?.jsonArray?.mapNotNull { it.jsonPrimitive.contentOrNull }
                        ?: emptyList()
                    val ubicacionJson = json["ubicacion"]?.jsonObject
                    val lat = ubicacionJson?.get("latitud")?.jsonPrimitive?.doubleOrNull
                    val lng = ubicacionJson?.get("longitud")?.jsonPrimitive?.doubleOrNull
                    Item(nombre, lugar, id_tienda, categoriaJson, img, tags, lat ?: 0.0, lng ?: 0.0)
                }

                Log.d("AlgoliaHelper", "🎯 Items deserializados (${items.size}): $items")
                items
            } catch (e: Exception) {
                Log.e("AlgoliaHelper", "❌ Error en Algolia search: ${e.message}", e)
                emptyList()
            }
        }

    suspend fun obtener_solo_categorias_subcategorias(
        localidad_defaul: String,
        texto: String
    ): List<Resultado_sub_cat> {
        return try {
            Log.d("AlgoliaSearch", "Buscando en localidad='$localidad_defaul' con texto='$texto'")

            val search = Query(texto.takeIf { it.isNotEmpty() } ?: "").apply {
                restrictSearchableAttributes = listOf(
                    Attribute("tag"),
                    Attribute("categoria"),
                    Attribute("nombre")
                )
                if (localidad_defaul.isNotEmpty()) {
                    filters = "lugar:\"$localidad_defaul\""
                }
            }

            val response = index.search(search)
            Log.d("AlgoliaSearch", "Total hits obtenidos: ${response.hits.size}")

            val resultados = response.hits.mapNotNull { hit ->
                val json = hit.json.jsonObject

                val categoriaJson = json["categoria"]?.jsonPrimitive?.content
                val tags = json["tag"]?.jsonArray?.mapNotNull { it.jsonPrimitive.contentOrNull } ?: emptyList()
                val nombre = json["nombre"]?.jsonPrimitive?.content.orEmpty()

                Log.d("AlgoliaSearch", "Hit procesado: nombre='$nombre', categoria='$categoriaJson', tags=$tags")

                when {
                    // Coincidencia en la categoría
                    categoriaJson?.contains(texto, ignoreCase = true) == true -> {
                        Log.d("AlgoliaSearch", "Coincidencia en categoría: $categoriaJson")
                        Resultado_sub_cat(
                            categoria = categoriaJson,
                            subcategoria = null
                        )
                    }

                    // Coincidencia en alguna subcategoría (tag)
                    tags.any { it.contains(texto, ignoreCase = true) } -> {
                        val match = tags.firstOrNull { it.contains(texto, ignoreCase = true) }
                        Log.d("AlgoliaSearch", "Coincidencia en subcategoría: $match")
                        Resultado_sub_cat(
                            categoria = categoriaJson ?: "",
                            subcategoria = match
                        )
                    }

                    // Coincidencia en el nombre
                    nombre.contains(texto, ignoreCase = true) -> {
                        val subMatch = tags.firstOrNull { nombre.contains(it, ignoreCase = true) }
                        Log.d("AlgoliaSearch", "Coincidencia en nombre: $nombre, subMatch=$subMatch")
                        Resultado_sub_cat(
                            categoria = categoriaJson ?: "",
                            subcategoria = subMatch
                        )
                    }

                    else -> null
                }
            }.distinctBy { it.categoria to it.subcategoria }

            Log.d("AlgoliaSearch", "Resultados filtrados y distintos: $resultados")

            resultados

        } catch (e: Exception) {
            Log.e("AlgoliaSearch", "Error en búsqueda: ${e.message}", e)
            emptyList()
        }
    }



    suspend fun obtener_lugares_tiendas_nombre(
        selecionado: Boolean,
        localidad: String,
        categoria: String?,
        subcategoria: String?,
        search: String
    ): List<Item> {
        return try {
            val filtros = buildList {
                add("""lugar:"$localidad"""")
                if (selecionado) {
                    // 🔹 Cuando ya hay selección, filtramos por categoria y subcategoria si existen
                    if (!categoria.isNullOrBlank()) add("""categoria:"$categoria"""")
                    if (!subcategoria.isNullOrBlank()) add("""tag:"$subcategoria"""")
                }
                // Si no hay selección, no agregamos filtros extra, búsqueda será más abierta
            }.joinToString(" AND ")


            val query = if (search.isBlank()) {
                Query().apply { if (filtros.isNotBlank()) this.filters = filtros }
            } else {
                if (selecionado) {
                    // Solo buscar por nombre dentro de los filtros de categoría/subcategoría
                    Query(search).apply {
                        if (filtros.isNotBlank()) this.filters = filtros
                        restrictSearchableAttributes = listOf(Attribute("nombre"))
                    }
                } else {
                    // Buscar por todo (nombre, categoría, subcategoría)
                    Query(search).apply {
                        if (filtros.isNotBlank()) this.filters = filtros
                        // no limitamos atributos, Algolia buscará en todos los campos indexados
                    }
                }
            }
Log.d("cat_sub_sleect","${categoria} $subcategoria")
            Log.d("AlgoliaQuery", "seleccionado=$selecionado, search='$search', filtros='$filtros'")

            val response = index.search(query)
            Log.d("AlgoliaQuery", "Total resultados: ${response.hits.size}")

            response.hits.mapNotNull { hit ->
                val json = hit.json.jsonObject
                val nombre = json["nombre"]?.jsonPrimitive?.content.orEmpty()
                val lugar = json["lugar"]?.jsonPrimitive?.content.orEmpty()
                val id = json["id_tienda"]?.jsonPrimitive?.content.orEmpty()
                val categoriaJson = json["categoria"]?.jsonPrimitive?.content.orEmpty()
                val img = json["img"]?.jsonPrimitive?.content.orEmpty()
                val tags = json["tag"]?.jsonArray?.mapNotNull { it.jsonPrimitive.contentOrNull } ?: emptyList()
                val ubicacionJson = json["ubicacion"]?.jsonObject
                val lat = ubicacionJson?.get("latitud")?.jsonPrimitive?.doubleOrNull ?: 0.0
                val lng = ubicacionJson?.get("longitud")?.jsonPrimitive?.doubleOrNull ?: 0.0

                Item(nombre, lugar, id, categoriaJson, img, tags, lat, lng)
            }

        } catch (e: Exception) {
            Log.e("AlgoliaQuery", "Error en búsqueda: ${e.message}", e)
            emptyList()
        }
    }


    suspend fun filtrar_categoria_sub_algolia(
        localidad: String,
        categoria: String? = null,
        subcategoria: String? = null
    ): List<Item> {
        return try {
            // 🔹 Construimos los filtros según lo que venga
            val filtros = buildList {
                add("""lugar:"$localidad"""") // siempre filtramos por localidad
                if (!categoria.isNullOrBlank()) add("""categoria:"$categoria"""")
                if (!subcategoria.isNullOrBlank()) add("""tag:"$subcategoria"""")
            }.joinToString(" AND ")

            val query = Query().apply {
                if (filtros.isNotBlank()) this.filters = filtros
            }

            val response = index.search(query)
            response.hits.mapNotNull { hit ->
                val json = hit.json.jsonObject
                val nombre = json["nombre"]?.jsonPrimitive?.content.orEmpty()
                val lugar = json["lugar"]?.jsonPrimitive?.content.orEmpty()
                val id = json["id_tienda"]?.jsonPrimitive?.content.orEmpty()
                val categoriaJson = json["categoria"]?.jsonPrimitive?.content.orEmpty()
                val img = json["img"]?.jsonPrimitive?.content.orEmpty()
                val tags = json["tag"]?.jsonArray?.mapNotNull { it.jsonPrimitive.contentOrNull } ?: emptyList()
                val ubicacionJson = json["ubicacion"]?.jsonObject
                val lat = ubicacionJson?.get("latitud")?.jsonPrimitive?.doubleOrNull ?: 0.0
                val lng = ubicacionJson?.get("longitud")?.jsonPrimitive?.doubleOrNull ?: 0.0

                Item(nombre, lugar, id, categoriaJson, img, tags, lat, lng)
            }

        } catch (e: Exception) {
            Log.e("AlgoliaQuery", "Error en búsqueda: ${e.message}", e)
            emptyList()
        }
    }


}
