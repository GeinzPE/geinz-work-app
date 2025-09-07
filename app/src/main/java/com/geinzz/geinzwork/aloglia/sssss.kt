import com.algolia.client.api.SearchClient
import com.algolia.client.model.search.SearchForHits
import com.algolia.client.model.search.SearchMethodParams
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable

@Serializable
data class Item(val nombre: String)

class AlgoliaHelper(
    appId: String,
    apiKey: String,
    private val indexName: String
) {

    private val client = SearchClient(appId = appId, apiKey = apiKey)

    suspend fun search(queryText: String): List<Item> = withContext(Dispatchers.IO) {
        val response = client.search(
            SearchMethodParams(
                requests = listOf(
                    SearchForHits(
                        indexName = indexName,
                        query = queryText,
                        hitsPerPage = 50
                    )
                )
            )
        )

        val items = mutableListOf<Item>()
        response.results.forEach { result ->
            // Cada result tiene "hits" como lista de Map<String, Any>
            val hits = result.hits
            hits?.forEach { hit ->
                val nombre = hit["nombre"]?.toString() ?: ""
                items.add(Item(nombre))
            }
        }

        items
    }
}
