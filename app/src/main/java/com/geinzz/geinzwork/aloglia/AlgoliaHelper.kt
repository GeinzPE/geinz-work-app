package com.geinzz.geinzwork.aloglia

import Item
import android.content.Context
import android.util.Log
import com.algolia.search.client.ClientSearch
import com.algolia.search.model.APIKey
import com.algolia.search.model.ApplicationID
import com.algolia.search.model.Attribute
import com.algolia.search.model.IndexName
import com.algolia.search.model.search.Query
import com.geinzz.geinzwork.data_store.data_store_localidad
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.constantes_lista_localidades.filtrar_por_radio_interno
import kotlinx.coroutines.flow.first
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

    suspend fun filtrar_categoria_sub_algolia(
        localidad: String,
        categoria: String? = null,
        subcategoria: String? = null
    ): List<Item> {
        Log.d("entrmaos_solo","filtrar_categoria_sub_algolia")
        return try {
            // 🔹 Construimos los filtros según lo que venga
            val filtros = buildList {
                add("""lugar:"$localidad"""") // siempre filtramos por localidad
                if (!categoria.isNullOrBlank()) add("""categoria:"$categoria"""")
                if (!subcategoria.isNullOrBlank()) add("""tag:"$subcategoria"""")
            }.joinToString(" AND ")

            val query = Query().apply {
                if (filtros.isNotBlank()) this.filters = filtros
                hitsPerPage = 1000
            }

            val response = index.search(query)
            response.hits.mapNotNull { hit ->
                val json = hit.json.jsonObject
                val nombre = json["nombre"]?.jsonPrimitive?.content.orEmpty()
                val lugar = json["lugar"]?.jsonPrimitive?.content.orEmpty()
                val id = json["id_tienda"]?.jsonPrimitive?.content.orEmpty()
                val categoriaJson = json["categoria"]?.jsonPrimitive?.content.orEmpty()
                val img = json["img"]?.jsonPrimitive?.content.orEmpty()
                val tags = json["tag"]?.jsonArray?.mapNotNull { it.jsonPrimitive.contentOrNull }
                    ?: emptyList()
                val ubicacionJson = json["ubicacion"]?.jsonObject
                val lat = ubicacionJson?.get("latitud")?.jsonPrimitive?.doubleOrNull ?: 0.0
                val lng = ubicacionJson?.get("longitud")?.jsonPrimitive?.doubleOrNull ?: 0.0
                val geohasing = json["geohash"]?.jsonPrimitive?.content.orEmpty()
                val zona = json["zona"]?.jsonPrimitive?.content.orEmpty()
                Item(nombre, lugar, id, categoriaJson, img, tags, lat, lng, geohasing,zona)
            }

        } catch (e: Exception) {
            Log.e("AlgoliaQuery", "Error en búsqueda: ${e.message}", e)
            emptyList()
        }
//        return emptyList()
    }

    suspend fun buscar_en_algolia(
        localidad: String,
        categoria: String?,
        subcategoria: String?,
        search: String,
        selecionado: Boolean
    ): Pair<List<Item>, List<String>> {
        Log.d("entrmaos_solo","buscar_en_algolia")

         val filtros = buildList {
            add("""lugar:"$localidad"""")
            if (selecionado) {
                if (!categoria.isNullOrBlank()) add("""categoria:"$categoria"""")
                if (!subcategoria.isNullOrBlank()) add("""tag:"$subcategoria"""")
            }
        }.joinToString(" AND ")

        val query = if (search.isBlank()) {
            Query().apply { if (filtros.isNotBlank()) this.filters = filtros
                hitsPerPage = 1000}
        } else {
            Query(search).apply {
                if (filtros.isNotBlank()) this.filters = filtros
                if (selecionado) restrictSearchableAttributes = listOf(Attribute("nombre"))
                hitsPerPage = 1000
            }
        }

        val response = index.search(query)
        Log.d("RetornarItems", "🔹 Algolia retornó ${response.hits.size} resultados")

        val items = response.hits.mapNotNull { hit ->
            val json = hit.json.jsonObject
            val nombre = json["nombre"]?.jsonPrimitive?.content.orEmpty()
            val lugar = json["lugar"]?.jsonPrimitive?.content.orEmpty()
            val id = json["id_tienda"]?.jsonPrimitive?.content.orEmpty()
            val categoriaJson = json["categoria"]?.jsonPrimitive?.content.orEmpty()
            val img = json["img"]?.jsonPrimitive?.content.orEmpty()
            val tags =
                json["tag"]?.jsonArray?.mapNotNull { it.jsonPrimitive.contentOrNull } ?: emptyList()
            val ubicacionJson = json["ubicacion"]?.jsonObject
            val lat = ubicacionJson?.get("latitud")?.jsonPrimitive?.doubleOrNull ?: 0.0
            val lng = ubicacionJson?.get("longitud")?.jsonPrimitive?.doubleOrNull ?: 0.0
            val geohashing = json["geohash"]?.jsonPrimitive?.content.orEmpty()
            val zona = json["zona"]?.jsonPrimitive?.content.orEmpty()

            Log.d(
                "RetornarItems",
                "📌 Hit procesado → nombre=$nombre, categoria=$categoriaJson, lat=$lat, lng=$lng"
            )
            Item(nombre, lugar, id, categoriaJson, img, tags, lat, lng, geohashing,zona)
        }

        val categoriasUnicas = items.map { it.categoria }.distinct()
        Log.d("RetornarItems", "✅ Algolia final → ${items.size} resultados")
        return Pair(items, categoriasUnicas)
    }


    fun filtrar_por_nombre_local(
        lista_filtrada: List<Item>,
        nombre: String
    ): List<Item> {
        return try {
            if (nombre.isBlank()) return lista_filtrada

            lista_filtrada.filter { item ->
                item.nombre.contains(nombre, ignoreCase = true)
            }
        } catch (e: Exception) {
            Log.e("FILTRAR_NOMBRE", "Error al filtrar localmente por nombre: ${e.message}")
            emptyList()
        }
    }



}