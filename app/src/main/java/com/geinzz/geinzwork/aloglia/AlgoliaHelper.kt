package com.geinzz.geinzwork.aloglia

import Item
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

    suspend fun search(queryText: String): List<Item> = withContext(Dispatchers.IO) {
        println("🔹 search() llamada con queryText: $queryText")

        try {
            val query = Query(queryText).apply {
                hitsPerPage = 50

                restrictSearchableAttributes =
                    listOf(
                        Attribute("nombre"),
                        Attribute("tag"),
                        Attribute("lugar"),
                        Attribute("categoria")
                    )
            }

            val response = index.search(query)
            println("🔹 Algolia raw hits: ${response.hits}")

            val items = response.hits.mapNotNull { hit ->
                val json = hit.json.jsonObject
                val nombre = json["nombre"]?.jsonPrimitive?.content ?: return@mapNotNull null
                val lugar = json["lugar"]?.jsonPrimitive?.content ?: return@mapNotNull null
                val categoria = json["categoria"]?.jsonPrimitive?.content ?: return@mapNotNull null
                val img = json["img"]?.jsonPrimitive?.content ?: return@mapNotNull null
                val tags = json["tag"]?.jsonArray?.mapNotNull { it.jsonPrimitive.contentOrNull }
                    ?: emptyList()
                Item(nombre, lugar, categoria, img, tags)
            }
            println("🔹 Items deserializados: $items")
            items
        } catch (e: Exception) {
            println("❌ Error en Algolia search: ${e.message}")
            emptyList()
        }
    }
}