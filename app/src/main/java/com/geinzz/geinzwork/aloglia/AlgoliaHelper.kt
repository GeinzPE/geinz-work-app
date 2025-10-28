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

//    suspend fun search(queryText: String, categoria: String, localidad: String): List<Item> =
//        withContext(Dispatchers.IO) {
//            Log.d(
//                "AlgoliaHelper",
//                "🔎 search() llamada con query='$queryText', categoria='$categoria', localidad='$localidad'"
//            )
//
//            try {
//                val query = Query(queryText.takeIf { it.isNotEmpty() } ?: "").apply {
//                    hitsPerPage = 50
//
//                    restrictSearchableAttributes = listOf(
//                        Attribute("nombre"),
//                        Attribute("tag"),
//                        Attribute("lugar"),
//                        Attribute("categoria")
//                    )
//
//                    val filtros = mutableListOf<List<String>>()
//
//                    if (categoria.isNotEmpty() && categoria != "Todos") {
//                        filtros.add(listOf("categoria:$categoria"))
//                        Log.d("AlgoliaHelper", "✅ Filtro aplicado: categoria=$categoria")
//                    }
//                    if (localidad.isNotEmpty()) {
//                        filtros.add(listOf("lugar:${localidad.lowercase()}"))
//                        Log.d("AlgoliaHelper", "✅ Filtro aplicado: localidad=$localidad")
//                    }
//
//                    if (filtros.isNotEmpty()) {
//                        facetFilters = filtros
//                        Log.d("AlgoliaHelper", "📌 facetFilters aplicados: $filtros")
//                    } else {
//                        Log.d("AlgoliaHelper", "⚠️ Sin filtros de categoría ni localidad.")
//                    }
//                }
//
//                val response = index.search(query)
//                Log.d("AlgoliaHelper", "📦 Algolia devolvió ${response.hits.size} hits")
//
//                val items = response.hits.mapNotNull { hit ->
//                    val json = hit.json.jsonObject
//                    val nombre = json["nombre"]?.jsonPrimitive?.content ?: return@mapNotNull null
//                    val lugar = json["lugar"]?.jsonPrimitive?.content ?: return@mapNotNull null
//                    val categoriaJson =
//                        json["categoria"]?.jsonPrimitive?.content ?: return@mapNotNull null
//                    val img = json["img"]?.jsonPrimitive?.content ?: return@mapNotNull null
//                    val id_tienda =
//                        json["id_tienda"]?.jsonPrimitive?.content ?: return@mapNotNull null
//                    val tags = json["tag"]?.jsonArray?.mapNotNull { it.jsonPrimitive.contentOrNull }
//                        ?: emptyList()
//                    val ubicacionJson = json["ubicacion"]?.jsonObject
//                    val lat = ubicacionJson?.get("latitud")?.jsonPrimitive?.doubleOrNull
//                    val lng = ubicacionJson?.get("longitud")?.jsonPrimitive?.doubleOrNull
//                    Item(nombre, lugar, id_tienda, categoriaJson, img, tags, lat ?: 0.0, lng ?: 0.0)
//                }
//
//                Log.d("AlgoliaHelper", "🎯 Items deserializados (${items.size}): $items")
//                items
//            } catch (e: Exception) {
//                Log.e("AlgoliaHelper", "❌ Error en Algolia search: ${e.message}", e)
//                emptyList()
//            }
//        }


//    suspend fun obtener_solo_categorias_subcategorias(
//        localidad_defaul: String,
//        texto: String
//    ): List<Resultado_sub_cat> {
//        return try {
//            Log.d("AlgoliaSearch", "Buscando en localidad='$localidad_defaul' con texto='$texto'")
//
//            val search = Query(texto.takeIf { it.isNotEmpty() } ?: "").apply {
//                restrictSearchableAttributes = listOf(
//                    Attribute("tag"),
//                    Attribute("categoria"),
//                    Attribute("nombre")
//                )
//                if (localidad_defaul.isNotEmpty()) {
//                    filters = "lugar:\"$localidad_defaul\""
//                }
//            }
//
//            val response = index.search(search)
//            Log.d("AlgoliaSearch", "Total hits obtenidos: ${response.hits.size}")
//
//            val resultados = response.hits.mapNotNull { hit ->
//                val json = hit.json.jsonObject
//                val categoriaJson = json["categoria"]?.jsonPrimitive?.content
//                val tags = json["tag"]?.jsonArray?.mapNotNull { it.jsonPrimitive.contentOrNull } ?: emptyList()
//                val nombre = json["nombre"]?.jsonPrimitive?.content.orEmpty()
//
//                Log.d("AlgoliaSearch", "Hit procesado: nombre='$nombre', categoria='$categoriaJson', tags=$tags")
//
//                when {
//                    // Coincidencia en la categoría
//                    categoriaJson?.contains(texto, ignoreCase = true) == true -> {
//                        Log.d("AlgoliaSearch", "Coincidencia en categoría: $categoriaJson")
//                        Resultado_sub_cat(categoria = categoriaJson)
//                    }
//
//                    // Coincidencia en tags
//                    tags.any { it.contains(texto, ignoreCase = true) } -> {
//                        Log.d("AlgoliaSearch", "Coincidencia en tag: $tags")
//                        Resultado_sub_cat(categoria = categoriaJson ?: "")
//                    }
//
//                    // Coincidencia en nombre
//                    nombre.contains(texto, ignoreCase = true) -> {
//                        Log.d("AlgoliaSearch", "Coincidencia en nombre: $nombre")
//                        Resultado_sub_cat(categoria = categoriaJson ?: "")
//                    }
//
//                    else -> null
//                }
//            }.distinctBy { it.categoria } // <- solo categoría, sin duplicados
//
//            Log.d("AlgoliaSearch", "Resultados filtrados y distintos: $resultados")
//            resultados
//
//        } catch (e: Exception) {
//            Log.e("AlgoliaSearch", "Error en búsqueda: ${e.message}", e)
//            emptyList()
//        }
//    }

    suspend fun obtener_solo_categorias_subcategorias(
        localidad_defaul: String,
        texto: String
    ): String? {
        return try {

            Log.d("AlgoliaSearch", "Buscando en localidad='$localidad_defaul' con texto='$texto'")

            if (texto.isEmpty()) return null
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

            // Tomamos solo el primer hit relevante
            val hit = response.hits.firstOrNull()
            val categoriaJson = hit?.json?.jsonObject?.get("categoria")?.jsonPrimitive?.content

            Log.d("AlgoliaSearch", "Categoría obtenida: $categoriaJson")
            categoriaJson

        } catch (e: Exception) {
            Log.e("AlgoliaSearch", "Error en búsqueda: ${e.message}", e)
            null
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
            Log.d("cat_sub_sleect", "${categoria} $subcategoria")
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
                val tags = json["tag"]?.jsonArray?.mapNotNull { it.jsonPrimitive.contentOrNull }
                    ?: emptyList()
                val ubicacionJson = json["ubicacion"]?.jsonObject
                val lat = ubicacionJson?.get("latitud")?.jsonPrimitive?.doubleOrNull ?: 0.0
                val lng = ubicacionJson?.get("longitud")?.jsonPrimitive?.doubleOrNull ?: 0.0
                val geohasing = json["geohash"]?.jsonPrimitive?.content.orEmpty()

                Item(nombre, lugar, id, categoriaJson, img, tags, lat, lng, geohasing)
            }

        } catch (e: Exception) {
            Log.e("AlgoliaQuery", "Error en búsqueda: ${e.message}", e)
            emptyList()
        }
    }

    suspend fun obtener_items_por_categoria_y_localidad(
        categoria: String,
        localidad: String
    ): List<Item> {
        return try {
            // 🔹 Creamos la query con categoría y localidad
            val filtros = """categoria:"$categoria" AND lugar:"$localidad""""
            val query = Query().apply {
                filters = filtros
            }

            Log.d(
                "AlgoliaQuery",
                "Buscando todos los items de la categoría='$categoria' en localidad='$localidad'"
            )

            // 🔹 Ejecutamos la búsqueda
            val response = index.search(query)
            Log.d("AlgoliaQuery", "Resultados encontrados: ${response.hits.size}")

            // 🔹 Mapeamos los resultados
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

                Item(nombre, lugar, id, categoriaJson, img, tags, lat, lng, geohasing)
            }

        } catch (e: Exception) {
            Log.e("AlgoliaQuery", "Error en búsqueda por categoría y localidad: ${e.message}", e)
            emptyList()
        }
    }

//
//    suspend fun retornar_items_categorias(
//        context: Context,
//        lista_base: List<Item>,          // lista completa de tiendas con geohash
//        geohasing_enable: Boolean,       // si usar geohash
//        hasing_user: String?,            // geohash del usuario
//        lista_guardada: List<Item>,      // lista cacheada / original
//        selecionado: Boolean,            // hay categoría/subcategoría seleccionada
//        localidad: String,
//        categoria: String? = null,
//        subcategoria: String? = null,
//        search: String
//    ): Pair<List<Item>, List<String>> {
//
//        Log.d("RetornarItems", "🚀 Iniciando retornar_items_categorias()")
//        Log.d(
//            "RetornarItems",
//            "Parámetros → geohash=$geohasing_enable, lista_base=${lista_base.size}, lista_guardada=${lista_guardada.size}, categoria=$categoria, subcategoria=$subcategoria, search='$search'"
//        )
//
//        // 🔹 Si no hay filtros ni búsqueda, devolver lista_guardada completa
//        if (!selecionado && categoria.isNullOrBlank() && subcategoria.isNullOrBlank() && search.isBlank()) {
//            Log.d("RetornarItems", "🔹 No hay filtros activos → devolviendo lista_guardada completa")
//            val categoriasUnicas = lista_guardada.map { it.categoria }.distinct()
//            return Pair(lista_guardada, categoriasUnicas)
//        }
//
//        // ===============================================
//        // 🔹 1️⃣ GEOHASHING
//        // ===============================================
//        if (geohasing_enable) {
//            Log.d("RetornarItems", "📍 Geohashing activo")
//
////            val radioGuardado = data_store_localidad.get_radio_user(context).first()
//            Log.d("RetornarItems", "📏 Radio guardado del usuario: $radioGuardado km")
//
//            var filtrados = if (!hasing_user.isNullOrEmpty()) {
//                filtrar_por_radio_interno(radioGuardado, hasing_user, lista_base)
//            } else emptyList()
//
//            Log.d("RetornarItems", "🔹 Tras filtro por radio → ${filtrados.size} items")
//
//            // Filtro por categoría/subcategoría
//            filtrados = filtrados.filter { item ->
//                (categoria.isNullOrBlank() || item.categoria.equals(
//                    categoria,
//                    ignoreCase = true
//                )) &&
//                        (subcategoria.isNullOrBlank() || item.lista.any {
//                            it.equals(
//                                subcategoria,
//                                ignoreCase = true
//                            )
//                        })
//            }
//            Log.d(
//                "RetornarItems",
//                "🔹 Tras filtro geohash categoría/subcategoría → ${filtrados.size}"
//            )
//
//            // Filtro por búsqueda
//            if (search.length >= 3) {
//                filtrados = filtrados.filter { it.nombre.contains(search, ignoreCase = true) }
//                Log.d("RetornarItems", "🔹 Tras filtro geohash búsqueda → ${filtrados.size}")
//            }
//
//            val categoriasUnicas = filtrados.map { it.categoria }.distinct()
//            Log.d("RetornarItems", "✅ Geohash final → ${filtrados.size} resultados")
//            return Pair(filtrados, categoriasUnicas)
//        }
//
//        // ===============================================
//        // 🔹 2️⃣ LISTA LOCAL
//        // ===============================================
//        if (lista_guardada.isNotEmpty()) {
//            Log.d("RetornarItems", "💾 Filtrando lista local (${lista_guardada.size})")
//
//            // Siempre usar lista_guardada como base, no sobrescribirla
//            var filtrados = lista_guardada
//
//            // Filtro categoría/subcategoría
//            if (!categoria.isNullOrBlank() || !subcategoria.isNullOrBlank()) {
//                filtrados = filtrados.filter { item ->
//                    (categoria.isNullOrBlank() || item.categoria.equals(
//                        categoria,
//                        ignoreCase = true
//                    )) &&
//                            (subcategoria.isNullOrBlank() || item.lista.any {
//                                it.equals(
//                                    subcategoria,
//                                    ignoreCase = true
//                                )
//                            })
//                }
//                Log.d(
//                    "RetornarItems",
//                    "🔹 Tras filtro local categoría/subcategoría → ${filtrados.size}"
//                )
//            }
//
//            // Filtro por búsqueda
//            if (search.length >= 3) {
//                filtrados = filtrados.filter { it.nombre.contains(search, ignoreCase = true) }
//                Log.d("RetornarItems", "🔹 Tras filtro local búsqueda → ${filtrados.size}")
//            }
//
//            val categoriasUnicas = filtrados.map { it.categoria }.distinct()
//            Log.d("RetornarItems", "✅ Filtrado local final → ${filtrados.size} resultados")
//            return Pair(filtrados, categoriasUnicas)
//        }
//
//        // ===============================================
//        // 🔹 3️⃣ CONSULTA ALGOLIA (último recurso)
//        // ===============================================
//        Log.d("RetornarItems", "🌐 Lista local vacía, consultando Algolia...")
//
//        val filtros = buildList {
//            add("""lugar:"$localidad"""")
//            if (selecionado) {
//                if (!categoria.isNullOrBlank()) add("""categoria:"$categoria"""")
//                if (!subcategoria.isNullOrBlank()) add("""tag:"$subcategoria"""")
//            }
//        }.joinToString(" AND ")
//
//        val query = if (search.isBlank()) {
//            Query().apply { if (filtros.isNotBlank()) this.filters = filtros }
//        } else {
//            Query(search).apply {
//                if (filtros.isNotBlank()) this.filters = filtros
//                if (selecionado) restrictSearchableAttributes = listOf(Attribute("nombre"))
//            }
//        }
//
//        val response = index.search(query)
//        Log.d("RetornarItems", "🔹 Algolia retornó ${response.hits.size} resultados")
//
//        val items = response.hits.mapNotNull { hit ->
//            val json = hit.json.jsonObject
//            val nombre = json["nombre"]?.jsonPrimitive?.content.orEmpty()
//            val lugar = json["lugar"]?.jsonPrimitive?.content.orEmpty()
//            val id = json["id_tienda"]?.jsonPrimitive?.content.orEmpty()
//            val categoriaJson = json["categoria"]?.jsonPrimitive?.content.orEmpty()
//            val img = json["img"]?.jsonPrimitive?.content.orEmpty()
//            val tags =
//                json["tag"]?.jsonArray?.mapNotNull { it.jsonPrimitive.contentOrNull } ?: emptyList()
//            val ubicacionJson = json["ubicacion"]?.jsonObject
//            val lat = ubicacionJson?.get("latitud")?.jsonPrimitive?.doubleOrNull ?: 0.0
//            val lng = ubicacionJson?.get("longitud")?.jsonPrimitive?.doubleOrNull ?: 0.0
//            val geohashing = json["geohash"]?.jsonPrimitive?.content.orEmpty()
//
//            Log.d(
//                "RetornarItems",
//                "📌 Hit procesado → nombre=$nombre, categoria=$categoriaJson, lat=$lat, lng=$lng"
//            )
//            Item(nombre, lugar, id, categoriaJson, img, tags, lat, lng, geohashing)
//        }
//
//        val categoriasUnicas = items.map { it.categoria }.distinct()
//        Log.d("RetornarItems", "✅ Algolia final → ${items.size} resultados")
//        return Pair(items, categoriasUnicas)
//    }


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

                Item(nombre, lugar, id, categoriaJson, img, tags, lat, lng, geohasing)
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
            Query().apply { if (filtros.isNotBlank()) this.filters = filtros }
        } else {
            Query(search).apply {
                if (filtros.isNotBlank()) this.filters = filtros
                if (selecionado) restrictSearchableAttributes = listOf(Attribute("nombre"))
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

            Log.d(
                "RetornarItems",
                "📌 Hit procesado → nombre=$nombre, categoria=$categoriaJson, lat=$lat, lng=$lng"
            )
            Item(nombre, lugar, id, categoriaJson, img, tags, lat, lng, geohashing)
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


    fun filtrar_categoria_local(
        localidad: String,
        lista_filtrada: List<Item>,
        categoria: String? = null,
        subcategoria: String? = null
    ): List<Item> {
        return try {
            lista_filtrada.filter { item ->

                val coincideCategoria =
                    categoria.isNullOrBlank() || item.categoria.equals(categoria, ignoreCase = true)
                val coincideSubcategoria = subcategoria.isNullOrBlank() || item.lista.any {
                    it.equals(
                        subcategoria,
                        ignoreCase = true
                    )
                }

                coincideCategoria && coincideSubcategoria
            }
        } catch (e: Exception) {
            Log.e("FILTRAR_LOCAL", "Error al filtrar localmente: ${e.message}")
            emptyList()
        }
    }

}