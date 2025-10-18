package com.geinzz.geinzwork.viewModels

import Item
import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.geinzz.geinzwork.R
import com.geinzz.geinzwork.aloglia.AlgoliaHelper
import com.geinzz.geinzwork.model.repo_seguridad_salud
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
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


//    private val _ls_items_ls_cat =
//         MutableStateFlow<Pair<List<Item>, List<String>>>(Pair(emptyList(), emptyList()))
//    val ls_items_ls_cat: StateFlow<Pair<List<Item>, List<String>>> = _ls_items_ls_cat

    private val _state = MutableStateFlow<List_items_result>(List_items_result.Empty)
    val state: StateFlow<List_items_result> = _state

    private val _lista_encontrada = MutableStateFlow<List<Item>>(emptyList())
    val lista_encontrada: StateFlow<List<Item>> = _lista_encontrada
    private var ultimaLocalidad: String? = null

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
        search: String,
        it: String
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
            _state.value = List_items_result.Loading

            val start = System.currentTimeMillis()
            try {
                Log.d("LS_ITEMS", "⏳ Consultando Algolia...")

                val res = algoliaHelper.retornar_items_categorias(
                    _lista_encontrada.value,
                    selecionado,
                    localidad,
                    categoria,
                    subcategoria,
                    search, // usa solo el valor actual
                    search  // quita el parámetro 'it'
                )

                coroutineContext.ensureActive()
                val elapsed = System.currentTimeMillis() - start

                // 🔹 Mantener mínimo 400ms de carga para mejor UX
                if (elapsed < 400) delay(400 - elapsed)

                Log.d("LS_ITEMS", "✅ Resultados recibidos:")
                Log.d("LS_ITEMS", "   Items encontrados      = ${res.first.size}")
                Log.d("LS_ITEMS", "   Categorías encontradas = ${res.second.size}")

                _state.value = if (res.first.isEmpty() && res.second.isEmpty()) {
                    List_items_result.Empty
                } else {
                    List_items_result.succes(res.second, res.first)
                }

                if (res.first.isNotEmpty() && res.first.size > 1) {
                    Log.d("enviamos_valor_anulado", res.first.toString())
                    _lista_encontrada.value = res.first
                    Log.d("hay_select", "${res.first.size}")
                }

            } catch (e: Exception) {
                Log.e("LS_ITEMS", "${e.message.toString()}")
                _state.value = List_items_result.error("Ocurrió un error, vuelva a intentarlo")
            }
        }
    }


    fun filtar_sub_cat(localidad: String, cat: String?, sub: String?) {
        //activador donde tenemos que pasar la lista_ya filtrada

        Log.d("buscamosen", "valor_lista_base ${cat}")
//        val localidadCambio = ultimaLocalidad != localidad
//
//        if (localidadCambio) {
//            clearResults()
//        }

        viewModelScope.launch {
            val a1 = algoliaHelper.obtener_items_por_categoria_y_localidad(cat ?: "", localidad)
            val lista_base_busqueda = a1
            Log.d("a1", "${a1.toString()}")

            _state.value = List_items_result.Loading
            delay(500)
            try {
                val res: List<Item>

                if (lista_base_busqueda.isEmpty()) {
                    Log.d("buscamosen", "algolia")
                    res = algoliaHelper.filtrar_categoria_sub_algolia(localidad, cat, sub)
                    _lista_encontrada.value = res
                } else {
                    Log.d("buscamosen", "local")
                    res = algoliaHelper.filtrar_categoria_local(
                        localidad,
                        lista_base_busqueda,
                        cat,
                        sub
                    )
                }

                val categoriasActuales = when (val currentState = _state.value) {
                    is List_items_result.succes -> currentState.categoira
                    else -> emptyList()
                }
                _state.value = if (res.isEmpty() && categoriasActuales.isEmpty()) {
                    List_items_result.Empty
                } else {
                    List_items_result.succes(categoriasActuales, res)
                }

            } catch (e: Exception) {

                _state.value = List_items_result.error("Ocurrio un error vuelvalo a intentar")
            }
        }
    }




    fun clearResults() {
        Log.d("limpiamos_valor","valor_limiado")
        _state.value = List_items_result.Cleared
        _lista_encontrada.value = emptyList()
    }


    sealed class List_items_result {
        object Loading : List_items_result()
        data class succes(
            val categoira: List<String>,
            val items: List<Item>,
        ) : List_items_result()

        object Empty : List_items_result()
        object Cleared : List_items_result()
        data class error(val msje: String) : List_items_result()
    }
}