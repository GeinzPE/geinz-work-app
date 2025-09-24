package com.geinzz.geinzwork.viewModels

import Item
import Resultado_sub_cat
import android.app.Application
import android.util.Log
import androidx.collection.emptyIntList
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.geinzz.geinzwork.R
import com.geinzz.geinzwork.aloglia.AlgoliaHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class SearchViewModel(application: Application) : AndroidViewModel(application) {

    private val algoliaHelper = AlgoliaHelper(
        appId = application.getString(R.string.APPID_ALGOLIA),
        apiKey = application.getString(R.string.APIKEY_ALGOLIA_SEARCH),
        indexName = application.getString(R.string.IDEX_NAME_ALGOLIA)
    )

    private val _results = MutableStateFlow<List<Item>>(emptyList())
    val results: StateFlow<List<Item>> = _results

    private val _resultado_categorias = MutableStateFlow<List<Resultado_sub_cat>>(emptyList())

    val resultado_categorias: StateFlow<List<Resultado_sub_cat>> = _resultado_categorias

    fun search(query: String, subcategoria_selecionada: String, localidad: String = "") {
        viewModelScope.launch {
            try {
                val hits = algoliaHelper.search(query, subcategoria_selecionada, localidad)
                _results.value = hits
            } catch (e: Exception) {
                _results.value = emptyList()
            }
        }
    }

    fun search_subcategorias(query: String) {
        viewModelScope.launch {
            try {
                val resultado = algoliaHelper.obtener_solo_categorias_subcategorias(query)
                _resultado_categorias.value = resultado
            } catch (e: Exception) {
                _resultado_categorias.value = emptyList()
            }
        }
    }

    fun clearResults() {
        _resultado_categorias.value=emptyList()
        _results.value = emptyList()
        Log.d("valor_resul", _results.value.toString())
    }
}