package com.geinzz.geinzwork.viewModels

import Item
import Resultado_sub_cat
import android.app.Application
import android.icu.text.StringSearch
import android.util.Log
import androidx.collection.emptyIntList
import androidx.compose.ui.graphics.Paint
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.geinzz.geinzwork.R
import com.geinzz.geinzwork.aloglia.AlgoliaHelper
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SearchViewModel(application: Application) : AndroidViewModel(application) {

    private val algoliaHelper = AlgoliaHelper(
        appId = application.getString(R.string.APPID_ALGOLIA),
        apiKey = application.getString(R.string.APIKEY_ALGOLIA_SEARCH),
        indexName = application.getString(R.string.IDEX_NAME_ALGOLIA)
    )
    private var searchJob: Job? = null

//    private val _results = MutableStateFlow<List<Item>>(emptyList())
//    val results: StateFlow<List<Item>> = _results

//    private val _resultado_categorias = MutableStateFlow<String>("")
//
//    val resultado_categorias: StateFlow<String> = _resultado_categorias

//
//    val _resultado_solo_nombre = MutableStateFlow<List<Item>>(emptyList())
//    val resultado_solo_nombre: StateFlow<List<Item>> = _resultado_solo_nombre


    private val _ls_items_ls_cat =
        MutableStateFlow<Pair<List<Item>, List<String>>>(Pair(emptyList(), emptyList()))
    val ls_items_ls_cat: StateFlow<Pair<List<Item>, List<String>>> = _ls_items_ls_cat

//    val resultadosCombinados: StateFlow<List<Item>> =
//        combine(resultado_categorias, resultado_solo_nombre) { categorias, tiendas ->
//            val deCategorias: List<Item> = categorias.flatMap { it.listaItems }
//            val deTiendas: List<Item> = tiendas
//
//            (deCategorias + deTiendas)
//                .distinctBy { it.id_tienda } // 🔹 elimina duplicados por id
//        }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())


//    fun search(query: String, subcategoria_selecionada: String, localidad: String = "") {
//        viewModelScope.launch {
//            try {
//                val hits = algoliaHelper.search(query, subcategoria_selecionada, localidad)
//                _results.value = hits
//            } catch (e: Exception) {
//                _results.value = emptyList()
//            }
//        }
//    }


    fun ls_items_ls_cat_fun(
        selecionado: Boolean,
        localidad: String,
        categoria: String?,
        subcategoria: String?,
        search: String
    ) {
        Log.d("LS_ITEMS", "➡️ INICIO FUNCIÓN")
        Log.d("LS_ITEMS", "Parámetros recibidos:")
        Log.d("LS_ITEMS", "   selecionado = $selecionado")
        Log.d("LS_ITEMS", "   localidad   = $localidad")
        Log.d("LS_ITEMS", "   categoria   = $categoria")
        Log.d("LS_ITEMS", "   subcategoria= $subcategoria")
        Log.d("LS_ITEMS", "   search      = $search")

        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            delay(500)
            try {
                Log.d("LS_ITEMS", "⏳ Consultando Algolia...")
                val res = algoliaHelper.retornar_items_categorias(
                    selecionado,
                    localidad,
                    categoria,
                    subcategoria,
                    search
                )
                Log.d("LS_ITEMS", "✅ Resultados recibidos:")
                Log.d("LS_ITEMS", "   Categorías encontradas = ${res.first.size}")
                Log.d("LS_ITEMS", "   Items encontrados      = ${res.second.size}")
                Log.d("LS_ITEMS", "   Detalle categorías     = ${res.first}")
                Log.d("LS_ITEMS", "   Detalle items          = ${res.second}")

                _ls_items_ls_cat.value = res
            } catch (e: Exception) {
                Log.e("LS_ITEMS", "❌ ERROR en consulta", e)
                _ls_items_ls_cat.value = Pair(emptyList(), emptyList())
            }
        }
    }

//    fun search_subcategorias(localidad_defaul: String, query: String) {
//        Log.d("resultado_cateogira", "Buscando en localidad=$localidad_defaul con query=$query")
//        viewModelScope.launch {
//            delay(300L)
//            try {
//                val resultado =
//                    algoliaHelper.obtener_solo_categorias_subcategorias(localidad_defaul, query)
//                _resultado_categorias.value = resultado ?: ""
//                Log.d("resresareasdare", _resultado_categorias.value.toString())
//            } catch (e: Exception) {
//                _resultado_categorias.value = ""
//
//            }
//
//        }
//
//    }
//
//    fun search_solo_nombre(
//        selecionado: Boolean,
//        localidad: String,
//        categoria: String?,
//        subcategoria: String?,
//        search: String
//    ) {
//        viewModelScope.launch {
//            delay(300L)
//            try {
//                val res = algoliaHelper.obtener_lugares_tiendas_nombre(
//                    selecionado,
//                    localidad,
//                    categoria,
//                    subcategoria,
//                    search
//                )
//
//                _resultado_solo_nombre.value = res
//                Log.d("search_solo_nombrebool", selecionado.toString())
//                Log.d("search_solo_nombre", "Resultados encontrados: ${res.size}")
//                res.forEachIndexed { index, item ->
//                    Log.d("search_solo_nombre", "[$index] → $item")
//                }
//
//            } catch (e: Exception) {
//                _resultado_solo_nombre.value = emptyList()
//                Log.e("search_solo_nombre", "Error en búsqueda", e)
//            }
//        }
//
//    }


    fun filtar_sub_cat(localidad: String, cat: String?, sub: String?) {
        Log.d("filtramos_cat_sub", "$localidad $cat $sub")
        viewModelScope.launch {
            try {
                val res = algoliaHelper.filtrar_categoria_sub_algolia(localidad, cat, sub)
                val categoriasActuales = _ls_items_ls_cat.value.second


                _ls_items_ls_cat.value = Pair(res, categoriasActuales)

            } catch (e: Exception) {
                val categoriasActuales = _ls_items_ls_cat.value.second
                _ls_items_ls_cat.value = Pair(emptyList(), categoriasActuales)
            }
        }
    }



    fun clearResults() {
        _ls_items_ls_cat.value =  Pair(emptyList(), emptyList())
    }
}