package com.geinzz.geinzwork.aloglia

import Item
import android.util.Log
import com.algolia.search.client.ClientSearch
import com.algolia.search.model.APIKey
import com.algolia.search.model.ApplicationID
import com.algolia.search.model.Attribute
import com.algolia.search.model.IndexName
import com.algolia.search.model.search.Query
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.contentOrNull
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

    suspend fun search(queryText: String, categoria: String, localidad: String): List<Item> = withContext(Dispatchers.IO) {
        Log.d("AlgoliaHelper", "🔎 search() llamada con query='$queryText', categoria='$categoria', localidad='$localidad'")

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
                val categoriaJson = json["categoria"]?.jsonPrimitive?.content ?: return@mapNotNull null
                val img = json["img"]?.jsonPrimitive?.content ?: return@mapNotNull null
                val id_tienda = json["id_tienda"]?.jsonPrimitive?.content ?: return@mapNotNull null
                val tags = json["tag"]?.jsonArray?.mapNotNull { it.jsonPrimitive.contentOrNull } ?: emptyList()

                Item(nombre, lugar, id_tienda, categoriaJson, img, tags)
            }

            Log.d("AlgoliaHelper", "🎯 Items deserializados (${items.size}): $items")
            items
        } catch (e: Exception) {
            Log.e("AlgoliaHelper", "❌ Error en Algolia search: ${e.message}", e)
            emptyList()
        }
    }
}
