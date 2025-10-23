package com.geinzz.geinzwork.viewModels

import Item
import android.app.Application
import android.content.Context
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.geinzz.geinzwork.R
import com.geinzz.geinzwork.aloglia.AlgoliaHelper
import com.geinzz.geinzwork.data_store.data_store_localidad
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class SearchViewModel(application: Application) : AndroidViewModel(application) {

    private val algoliaHelper = AlgoliaHelper(
        appId = application.getString(R.string.APPID_ALGOLIA),
        apiKey = application.getString(R.string.APIKEY_ALGOLIA_SEARCH),
        indexName = application.getString(R.string.IDEX_NAME_ALGOLIA)
    )

    private var searchJob: Job? = null
    private var filterJob: Job? = null

    private val _state = MutableStateFlow<ListItemsResult>(ListItemsResult.Empty(""))
    val state: StateFlow<ListItemsResult> = _state

    private val _listaEncontrada = MutableStateFlow<List<Item>>(emptyList())
    val listaEncontrada: StateFlow<List<Item>> = _listaEncontrada

    private var listaOriginalCompleta: List<Item> = emptyList()
    private var listaGeohashCompleta: List<Item> = emptyList()

    fun setListaOriginal(lista: List<Item>) {
        listaOriginalCompleta = lista
    }

    /** ---------- FILTRO PRINCIPAL (BÚSQUEDA + CATEGORÍA + SUBCATEGORÍA + GEOHASH) ---------- */
    fun buscarItems(
        context: Context,
        geohashEnable: Boolean,
        hashUser: String?,
        seleccionado: Boolean,
        localidad: String,
        categoria: String?,
        subcategoria: String?,
        search: String
    ) {
        Log.d("isntqa_fun", "buscarItems")
        searchJob?.cancel()

        searchJob = viewModelScope.launch {
            _state.value = ListItemsResult.Loading
            try {
                // -------------------------
                // 🔹 1️⃣ Filtrar localmente si hay categoría/subcategoría
                // -------------------------
                if (!categoria.isNullOrBlank() || !subcategoria.isNullOrBlank()) {
                    if(geohashEnable)Log.d("asd123","con geohasing")
                    Log.d("asd123", "Filtraremos solo por nombre local")

                    // Filtrar lista original por nombre (search)
                    val listaFiltrada = algoliaHelper.filtrar_por_nombre_local(listaOriginalCompleta, search)
                    val categorias = listaFiltrada.map { it.categoria }.distinct()

                    _listaEncontrada.value = listaFiltrada
                    _state.value = if (listaFiltrada.isEmpty()) {
                        ListItemsResult.Empty("No se encontraron resultados en la localidad")
                    } else {
                        ListItemsResult.Success(categorias, listaFiltrada)
                    }

                } else {
                    // -------------------------
                    // 🔹 2️⃣ Buscar en Algolia si no hay cat/subcat seleccionada
                    // -------------------------
                    Log.d("asd123", "Buscando en Algolia")

                    // Búsqueda con texto y filtros en Algolia
                    val (listaFiltrada, categorias) = algoliaHelper.buscar_en_algolia(
                        localidad,
                        categoria,
                        subcategoria,
                        search,
                        seleccionado
                    )

                    _listaEncontrada.value = listaFiltrada
                    _state.value = if (listaFiltrada.isEmpty() && categorias.isEmpty()) {
//                        if (geohashEnable) {
//                            val radioGuardado = data_store_localidad.get_radio_user(context).first()
//                            ListItemsResult.Empty("No se encontraron resultados en ${radioGuardado.toInt()} Km")
//                        } else {
                            ListItemsResult.Empty("No se encontraron resultados")
//                        }
                    } else {
                        ListItemsResult.Success(categorias, listaFiltrada)
                    }
                }

            } catch (e: Exception) {
                Log.e("SearchViewModel", "Error buscarItems: ${e.message}")
                _state.value = ListItemsResult.Error("Ocurrió un error, vuelva a intentarlo")
            }
        }
    }


    /** ---------- FILTRAR POR SUBCATEGORÍA Y CATEGORÍA ---------- */
    fun filtrarSubCat(
        context: Context,
        hashUser: String?,
        cercaDeTiEnable: Boolean,
        localidad: String,
        categoria: String?,
        subcategoria: String?
    ) {
        Log.d("isntqa_fun", "filtrarSubCat")
        filterJob?.cancel()
        filterJob = viewModelScope.launch {
            try {
                _state.value = ListItemsResult.Loading

                if (!categoria.isNullOrBlank() || !subcategoria.isNullOrBlank()) {
                    val algolia = algoliaHelper.filtrar_categoria_sub_algolia(
                        localidad,
                        categoria,
                        subcategoria
                    )

                    _listaEncontrada.value = algolia

                    _state.value = if (algolia.isEmpty()) {
                        ListItemsResult.Empty("No se encontraron resultados")
                    } else {
                        val categoriasActuales = algolia.map { it.categoria }.distinct()
                        listaOriginalCompleta = algolia
                        ListItemsResult.Success(categoriasActuales, algolia)

                    }
                }

            } catch (e: CancellationException) {
                Log.d("SearchViewModel", "Filtro cancelado por nueva solicitud")
            } catch (e: Exception) {
                _state.value = ListItemsResult.Error("Ocurrió un error, vuelva a intentarlo")
            }
        }
    }


    /** ---------- RESTAURAR LISTA ORIGINAL ---------- */
    fun restaurarListaOriginal() {
        val base =
            if (listaGeohashCompleta.isNotEmpty()) listaGeohashCompleta else listaOriginalCompleta
        _listaEncontrada.value = base
        val categorias = base.map { it.categoria }.distinct()
        _state.value = if (base.isEmpty()) ListItemsResult.Empty("No se encontraron resultados")
        else ListItemsResult.Success(categorias, base)
    }

    /** ---------- FILTRAR POR RADIO INTERNO ---------- */
    fun filtrarPorRadioInterno(radio: Float, hashUser: String, listaBase: List<Item>): List<Item> {
        Log.d("isntqa_fun", "filtrarPorRadioInterno")

        val precision = when {
            radio <= 0.1 -> 8
            radio <= 0.3 -> 7
            radio <= 1 -> 6
            radio <= 5 -> 5
            else -> 4
        }
        val prefijo = hashUser.take(precision)
        return listaBase.filter { it.geohasing.startsWith(prefijo) }
    }

    fun filtrar_por_radio(
        context: Context,
        cat_select: String,
        sub_select: String,
        cerca_de_ti_enable: Boolean,
        hash_user: String?
    ) {
        Log.d("isntqa_fun", "filtrar_por_radio")

        viewModelScope.launch {
            try {
                _state.value = ListItemsResult.Loading

                val radioGuardado = data_store_localidad.get_radio_user(context).first()

                var listaFiltrable = listaOriginalCompleta
                if (cerca_de_ti_enable && !hash_user.isNullOrEmpty()) {
                    listaFiltrable =
                        filtrarPorRadioInterno(radioGuardado, hash_user, listaOriginalCompleta)
                }

                if (cat_select.isNotBlank() || sub_select.isNotBlank()) {
                    listaFiltrable = listaFiltrable.filter { item ->
                        (cat_select.isBlank() || item.categoria == cat_select) &&
                                (sub_select.isBlank() || item.lista.contains(sub_select))
                    }
                }


                _listaEncontrada.value = listaFiltrable

                val categorias = listaFiltrable.map { it.categoria }.distinct()
                _state.value = if (listaFiltrable.isEmpty()) {
                    if (cerca_de_ti_enable) ListItemsResult.Empty("No se encontraron resultados en ${radioGuardado.toInt()} Km")
                    else ListItemsResult.Empty("No se encontraron resultados")
                } else ListItemsResult.Success(categorias, listaFiltrable)

            } catch (e: Exception) {
                _state.value = ListItemsResult.Error("Ocurrió un error, vuelva a intentarlo")
            }
        }
    }

    /** ---------- LIMPIAR RESULTADOS ---------- */
    fun clearResults() {
        _state.value = ListItemsResult.Cleared
        _listaEncontrada.value = emptyList()
        listaGeohashCompleta = emptyList()
    }

    /** ---------- CLASE SELLADA PARA ESTADOS ---------- */
    sealed class ListItemsResult {
        object Loading : ListItemsResult()
        data class Success(val categorias: List<String>, val items: List<Item>) : ListItemsResult()
        data class Empty(val mensaje: String) : ListItemsResult()
        object Cleared : ListItemsResult()
        data class Error(val mensaje: String) : ListItemsResult()
    }
}
