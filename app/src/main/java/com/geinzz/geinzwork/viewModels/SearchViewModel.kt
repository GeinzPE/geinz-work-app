package com.geinzz.geinzwork.viewModels

import Item
import Resultado_sub_cat
import android.app.Application
import android.icu.text.StringSearch
import android.util.Log
import androidx.collection.emptyIntList
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

    private val _resultado_categorias = MutableStateFlow<List<Resultado_sub_cat>>(emptyList())

    val resultado_categorias: StateFlow<List<Resultado_sub_cat>> = _resultado_categorias


    val _resultado_solo_nombre = MutableStateFlow<List<Item>>(emptyList())
    val resultado_solo_nombre: StateFlow<List<Item>> = _resultado_solo_nombre

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


    fun search_subcategorias(localidad_defaul: String, query: String) {
        Log.d("resultado_cateogira", "Buscando en localidad=$localidad_defaul con query=$query")
        viewModelScope.launch {
            delay(300L)
            try {
                val resultado =
                    algoliaHelper.obtener_solo_categorias_subcategorias(localidad_defaul, query)
                _resultado_categorias.value = resultado

                Log.d("resultado_cateogira", "Resultados encontrados: ${resultado.size}")
                resultado.forEach {
                    Log.d("resultado_cateogira", "→ $it")
                }
            } catch (e: Exception) {
                _resultado_categorias.value = emptyList()
                Log.e("resultado_cateogira", "Error en búsqueda", e)
            }

        }

    }

    fun search_solo_nombre(
        selecionado: Boolean,
        localidad: String,
        categoria: String?,
        subcategoria: String?,
        search: String
    ) {
        viewModelScope.launch {
            delay(300L)
            try {
                val res = algoliaHelper.obtener_lugares_tiendas_nombre(
                    selecionado,
                    localidad,
                    categoria,
                    subcategoria,
                    search
                )

                _resultado_solo_nombre.value = res
                Log.d("search_solo_nombrebool", selecionado.toString())
                Log.d("search_solo_nombre", "Resultados encontrados: ${res.size}")
                res.forEachIndexed { index, item ->
                    Log.d("search_solo_nombre", "[$index] → $item")
                }

            } catch (e: Exception) {
                _resultado_solo_nombre.value = emptyList()
                Log.e("search_solo_nombre", "Error en búsqueda", e)
            }
        }

    }


    fun filtar_sub_cat(localidad: String, cat: String?, sub: String?) {
        Log.d("filtramos_cat_sub", "$localidad $cat $sub")
        viewModelScope.launch {
            try {
                val res = algoliaHelper.filtrar_categoria_sub_algolia(localidad, cat, sub)
                _resultado_solo_nombre.value = res
            } catch (e: Exception) {
                _resultado_solo_nombre.value = emptyList()
            }
        }
    }


//    fun clearResults() {
//        _resultado_categorias.value = emptyList()
//        _results.value = emptyList()
//        Log.d("valor_resul", _results.value.toString())
//    }
}