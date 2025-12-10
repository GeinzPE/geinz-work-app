package com.geinzz.geinzwork.viewModels

import Item
import android.app.Application
import android.content.Context
import android.location.Location
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.geinzz.geinzwork.R
import com.geinzz.geinzwork.aloglia.AlgoliaHelper
import com.geinzz.geinzwork.data_store.data_store_localidad
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.constantes_lista_localidades
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.constantes_lista_localidades.formatDistancia
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.constantes_lista_localidades.formatDistancia_funcion
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay

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

    /** ---------- LISTA DE ALGOLIA GENERAL CATEGORIA ORIGINAL ---------- */
    private val lista_original_algolia1 = MutableStateFlow<List<Item>>(emptyList())

    /** ---------- LISTA COMPLETA LOCAL FILTRADA POR SUBCATEGORIA ---------- */
    private val lista_filtrada_subcategoria = MutableStateFlow<List<Item>>(emptyList())

    /** ---------- LISTA COMPLETA GEOHASING ---------- */
    private val lista_filtrada_geohasing = MutableStateFlow<List<Item>>(emptyList())

    /** ---------- LISTA COMPLETA ORIGINAL ---------- */
    private var listaOriginalCompleta: List<Item> = emptyList()

    /** ---------- LISTA COMPLETA PARA RECORDAR AL CAMBIAR DE PANTALLA ---------- */
    private var lista_original_sin_borrar_ni_editar = MutableStateFlow<List<Item>>(emptyList())
    private var lista_filtrada_sin_borrar_ni_edita = MutableStateFlow<List<Item>>(emptyList())

    /** ---------- FALG YA CARGADO DESDE ALGOLIA ---------- */
    private var algoliaCargado = false


    /** ---------- DATA CLASS PARA RECORDAR LOS CAMBIOS DE PANTALLA ---------- */
    data class cambio_pantalla(
        val cat: String,
        val sub: String,
        val localidad: String,
        val txt_filtrado: String,
    )

    /** ---------- DATOS DE RETORNO VARIABLES ---------- */
    private val _categoria_retorno = MutableStateFlow<String>  ("")
    val categoria_retorno : StateFlow<String> =_categoria_retorno

    private val _subcategoria = MutableStateFlow<String>  ("")
    val subcategoria : StateFlow<String> =_subcategoria


    private val _txt_filtrado_guardado =MutableStateFlow<String> ("")
    val txt_filtrado_guardado: StateFlow<String> =_txt_filtrado_guardado

    private val _localidad_fitlrado_guardado =MutableStateFlow<String>("")
    val localida_filtrado_guardado : StateFlow<String> = _localidad_fitlrado_guardado

    /** ---------- FUNCION PARA GUARDAR LOS DATOS ANTES DE CAMBIAR DE PANTALLA ---------- */
    fun guardar_datos_cambi_pantalla(i: cambio_pantalla) {
        Log.d("los_datos_al_cambiar_pantlla", "$i  lista completa= ${lista_original_sin_borrar_ni_editar.value.size} lista filtrada=${lista_filtrada_sin_borrar_ni_edita.value.size}")
        _categoria_retorno.value=i.cat
        _subcategoria.value=i.sub
        _txt_filtrado_guardado.value=i.txt_filtrado
        _localidad_fitlrado_guardado.value=i.localidad
    }

    fun guardar_localidad_select_viewmodel(localidad: String){
        _localidad_fitlrado_guardado.value=localidad
    }

    /** ---------- FILTRO PRINCIPAL (BÚSQUEDA + CATEGORÍA + SUBCATEGORÍA + GEOHASH) ---------- */
    fun buscarItems(
        radio: Float,
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
                // 🔹 1️⃣ Si hay categoría o subcategoría seleccionada, filtramos localmente
                if (!categoria.isNullOrBlank() || !subcategoria.isNullOrBlank()) {

                    // -----------------------------
                    // 🌍 Si el geohash está activo
                    // -----------------------------
                    if (geohashEnable && !hashUser.isNullOrBlank()) {

                        if (!subcategoria.isNullOrBlank()) {
                            Log.d("lista", lista_filtrada_geohasing.value.size.toString())
                            Log.d("asd123", "Geohash activado - filtrando por subcategoría")
                            val listaFiltrada = algoliaHelper.filtrar_por_nombre_local(
                                lista_filtrada_geohasing.value,
                                search
                            )
                            val categorias = listaFiltrada.map { it.categoria }.distinct()
                            Log.d("res_geohasing", listaFiltrada.size.toString())

                            _listaEncontrada.value = listaFiltrada
                            _state.value = if (listaFiltrada.isEmpty()) {
                                ListItemsResult.Empty("No se encontraron resultados a ${constantes_lista_localidades.formatDistancia(radio)} ")
                            } else {
                                ListItemsResult.Success(categorias, listaFiltrada)
                            }

                        } else if (!categoria.isNullOrBlank()) {
                            Log.d("asd123", "Geohash activado - filtrando por cateforia")
                            val listaFiltrada = algoliaHelper.filtrar_por_nombre_local(
                                lista_filtrada_geohasing.value,
                                search
                            )
                            val categorias = listaFiltrada.map { it.categoria }.distinct()

                            _listaEncontrada.value = listaFiltrada
                            _state.value = if (listaFiltrada.isEmpty()) {
                                ListItemsResult.Empty("No se encontraron resultados a ${constantes_lista_localidades.formatDistancia(radio)}")
                            } else {
                                ListItemsResult.Success(categorias, listaFiltrada)
                            }

                        }
                    } else {
                        // -----------------------------
                        // 🔍 Filtrado local sin geohash
                        // -----------------------------
                        if (!subcategoria.isNullOrBlank()) {
                            Log.d("asd123", "buscamos en texto por subcategoría")
                            val listaFiltrada = algoliaHelper.filtrar_por_nombre_local(
                                lista_filtrada_subcategoria.value,
                                search
                            )
                            val categorias = listaFiltrada.map { it.categoria }.distinct()

                            _listaEncontrada.value = listaFiltrada
                            _state.value = if (listaFiltrada.isEmpty()) {
                                ListItemsResult.Empty("No se encontraron resultados en la localidad")
                            } else {
                                ListItemsResult.Success(categorias, listaFiltrada)
                            }
                            if(listaFiltrada.isNotEmpty()){
                                lista_filtrada_sin_borrar_ni_edita.value=listaFiltrada
                            }
                        } else if (!categoria.isNullOrBlank()) {
                            Log.d("asd123", "buscamos en texto por categoria")
                            val listaFiltrada = algoliaHelper.filtrar_por_nombre_local(
                                lista_original_algolia1.value,
                                search
                            )
                            val categorias = listaFiltrada.map { it.categoria }.distinct()

                            _listaEncontrada.value = listaFiltrada
                            _state.value = if (listaFiltrada.isEmpty()) {
                                ListItemsResult.Empty("No se encontraron resultados en la localidad")
                            } else {
                                ListItemsResult.Success(categorias, listaFiltrada)
                            }
                            if(listaFiltrada.isNotEmpty()){
                                lista_filtrada_sin_borrar_ni_edita.value=listaFiltrada
                            }
                        }
                    }

                } else {
                    // -----------------------------
                    // 🔹 2️⃣ Sin categoría/subcategoría → Buscar directamente en Algolia
                    // -----------------------------

                    if (search.length < 3) {
                        clearResults()
                        _state.value = ListItemsResult.Empty("")
                        return@launch
                    }
                    Log.d("asd123", "Buscando directamente en Algolia")
                    val (listaFiltrada, categorias) = algoliaHelper.buscar_en_algolia(
                        localidad, categoria, subcategoria, search, seleccionado
                    )

                    _listaEncontrada.value = listaFiltrada
                    listaOriginalCompleta = listaFiltrada


                    _state.value = if (listaFiltrada.isEmpty() && categorias.isEmpty()) {
                        ListItemsResult.Empty("No se encontraron resultados algolia")
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
        lat:Double,lng:Double,
        radio: Float,
        context: Context,
        hashUser: String?,
        cercaDeTiEnable: Boolean,
        localidad: String,
        categoria: String?,
        subcategoria: String?
    ) {
        Log.d("isntqa_fun", "$lat $lng")
        Log.d("isntqa_fun1231313", "$cercaDeTiEnable")

        filterJob?.cancel()
        filterJob = viewModelScope.launch {
            try {
                _state.value = ListItemsResult.Loading

                // 🔹 1️⃣ Cargar desde Algolia solo si aún no hay datos
                if (!algoliaCargado) {
                    val resAlgoliaGeneral = algoliaHelper.filtrar_categoria_sub_algolia(
                        localidad,
                        categoria,
                        subcategoria
                    )
                    lista_original_algolia1.value = resAlgoliaGeneral
                    lista_original_sin_borrar_ni_editar.value=resAlgoliaGeneral
                    listaOriginalCompleta = resAlgoliaGeneral
                    algoliaCargado = true
                }


                // 🔹 2️⃣ Filtrar por categoría / subcategoría
                var listaFiltrada = listaOriginalCompleta.filter { item ->
                    (categoria.isNullOrBlank() || item.categoria.equals(
                        categoria,
                        ignoreCase = true
                    )) &&
                            (subcategoria.isNullOrBlank() || item.lista.any {
                                it.equals(subcategoria, ignoreCase = true)
                            })
                }

                // 🔹 3️⃣ Aplicar filtro por geohash solo si está habilitado
                if (cercaDeTiEnable && !hashUser.isNullOrBlank()) {
                    Log.d("isntqa_fun", "Aplicando filtro geohash interno")
                    listaFiltrada = filtrarPorRadioInterno(radio, lat,lng, listaFiltrada,categoria)
                    lista_filtrada_geohasing.value = listaFiltrada
                } else {
                    Log.d(
                        "isntqa_fun",
                        "❌ Filtro 'Cerca de ti' desactivado — mostrando lista original filtrada solo por categoría/subcategoría"
                    )
                    listaFiltrada = listaOriginalCompleta.filter { item ->
                        (categoria.isNullOrBlank() || item.categoria.equals(
                            categoria,
                            ignoreCase = true
                        )) &&
                                (subcategoria.isNullOrBlank() || item.lista.any {
                                    it.equals(subcategoria, ignoreCase = true)
                                })
                    }

                }

                // 🔹 4️⃣ Actualizar los estados reactivos
                lista_filtrada_subcategoria.value = listaFiltrada
                _listaEncontrada.value = listaFiltrada

                _state.value = if (listaFiltrada.isEmpty()) {
                    if (cercaDeTiEnable) {
                        ListItemsResult.Empty("No se encontraron resultados a ${formatDistancia(radio)}")
                    } else {
                        ListItemsResult.Empty("No se encontraron resultados ")
                    }
                } else {
                    val categoriasActuales = listaFiltrada.map { it.categoria }.distinct()
                    ListItemsResult.Success(categoriasActuales, listaFiltrada)
                }

                if (listaFiltrada.isNotEmpty()) {
                    lista_filtrada_sin_borrar_ni_edita.value = listaFiltrada
                }

            } catch (e: CancellationException) {
                Log.d("SearchViewModel", "Filtro cancelado por nueva solicitud")
            } catch (e: Exception) {
                Log.e("SearchViewModel", "Error al filtrarSubCat: ${e.message}")
                _state.value = ListItemsResult.Error("Ocurrió un error, vuelva a intentarlo")
            }
        }
    }


    /** ---------- FILTRAR POR RADIO INTERNO ---------- */
    fun filtrarPorRadioInterno(
        paso: Float,                // cada paso = 100 m
        latUser: Double,
        lngUser: Double,
        listaMaster: List<Item>,    // lista completa de la app
        categoriaSeleccionada: String? = null
    ): List<Item> {
        Log.d("lat_stras","$latUser $lngUser")
        return try {
            Log.d("isntqa_fun", "📌 Iniciando filtrarPorRadioInterno $latUser $lngUser")
            Log.d("isntqa_fun", "Paso recibido: $paso")
            Log.d("isntqa_fun", "Categoría seleccionada: $categoriaSeleccionada")

            val radioMetros = paso.toInt().coerceAtLeast(1) * 100.0
            Log.d("isntqa_fun", "Radio en metros: $radioMetros m")

            // --- Filtrado por categoría (sobre lista completa siempre) ---
            val listaPorCategoria = if (!categoriaSeleccionada.isNullOrEmpty()) {
                listaMaster.filter { it.categoria == categoriaSeleccionada }
            } else {
                listaMaster
            }
            Log.d("isntqa_fun", "Cantidad después de filtrar por categoría: ${listaPorCategoria.size}")

            // --- Si no hay lat/lng, devolver solo por categoría ---
            if (latUser == null || lngUser == null) return listaPorCategoria

            // --- Filtrado por distancia exacta ---
            val listaFinal = listaPorCategoria.filter { item ->
                val results = FloatArray(1)
                Location.distanceBetween(
                    latUser, lngUser,
                    item.latitud, item.longitud,
                    results
                )
                results[0].toDouble() <= radioMetros
            }

            Log.d("isntqa_fun", "Cantidad después de filtrar por distancia: ${listaFinal.size}")
            listaFinal

        } catch (e: Exception) {
            Log.e("isntqa_fun", "❌ Error al filtrar por radio interno: ${e.message}", e)
            listaMaster
        }
    }








    fun filtrar_por_radio(
        lat: Double,lng: Double,
        radio_filtrado: Float,
        context: Context,
        cat_select: String,
        sub_select: String,
        cerca_de_ti_enable: Boolean,
        hash_user: String?
    ) {

        Log.d("isntqa_fun", "filtrar_por_radio")
        Log.d("isntqa_fun1231312313", "$lat $lng")

        viewModelScope.launch {
            try {
                _state.value = ListItemsResult.Loading
//                val radioGuardado = data_store_localidad.radioUserFlow.value

//                val radioGuardado = data_store_localidad.get_radio_user(context).first()

                var listaFiltrable = listaOriginalCompleta
                if (cerca_de_ti_enable && !hash_user.isNullOrEmpty()) {
                    listaFiltrable =
                        filtrarPorRadioInterno(
                            radio_filtrado,
                            lat,lng,
                            listaOriginalCompleta,cat_select
                        )
                }

                if (cat_select.isNotBlank() || sub_select.isNotBlank()) {
                    listaFiltrable = listaFiltrable.filter { item ->
                        (cat_select.isBlank() || item.categoria == cat_select) &&
                                (sub_select.isBlank() || item.lista.contains(sub_select))
                    }
                }


                _listaEncontrada.value = listaFiltrable
                lista_filtrada_geohasing.value = listaFiltrable

                val categorias = listaFiltrable.map { it.categoria }.distinct()
                _state.value = if (listaFiltrable.isEmpty()) {
                    if (cerca_de_ti_enable) ListItemsResult.Empty("No se encontraron resultados en ${formatDistancia_funcion(radio_filtrado)} ")
                    else ListItemsResult.Empty("No se encontraron resultados")
                } else ListItemsResult.Success(categorias, listaFiltrable)

            } catch (e: Exception) {
                _state.value = ListItemsResult.Error("Ocurrió un error, vuelva a intentarlo")
            }
        }
    }

    /** ---------- LIMPIAR RESULTADOS ---------- */
    fun clearResults() {
        Log.d("llamaos", "clearResults")
        _state.value = ListItemsResult.Cleared
        _listaEncontrada.value = emptyList()
        lista_filtrada_subcategoria.value = emptyList()
        lista_original_algolia1.value = emptyList()
        listaOriginalCompleta = emptyList()
        algoliaCargado = false
    }


    fun restaurarListaOriginal(categoria: String, subcategoria: String) {
        Log.d("isntqa_fun", "🔄 Restaurando lista original...")

        viewModelScope.launch {
            try {
                // 🔹 1️⃣ Obtener la lista base original (de Algolia o cache)
                val listaBase = listaOriginalCompleta

                // 🔹 2️⃣ Filtrar según los parámetros actuales
                val listaRestaurada = listaBase.filter { item ->
                    (categoria.isBlank() || item.categoria.equals(categoria, ignoreCase = true)) &&
                            (subcategoria.isBlank() || item.lista.any {
                                it.equals(subcategoria, ignoreCase = true)
                            })
                }

                // 🔹 3️⃣ Actualizar los estados observados
                _listaEncontrada.value = listaRestaurada
                lista_filtrada_subcategoria.value = listaRestaurada

                // 🔹 4️⃣ Actualizar el estado general
                _state.value = if (listaRestaurada.isEmpty()) {
                    ListItemsResult.Empty("No se encontraron resultados en esta categoría")
                } else {
                    val categoriasActuales = listaRestaurada.map { it.categoria }.distinct()
                    ListItemsResult.Success(categoriasActuales, listaRestaurada)
                }

                Log.d(
                    "isntqa_fun",
                    "✅ Lista restaurada correctamente (${listaRestaurada.size} items)"
                )

            } catch (e: Exception) {
                Log.e("isntqa_fun", "Error al restaurar lista original: ${e.message}")
                _state.value = ListItemsResult.Error("Ocurrió un error al restaurar los resultados")
            }
        }
    }


    /** ---------- CLASE SELLADA PARA ESTADOS ---------- */
    sealed class ListItemsResult {
        object Loading : ListItemsResult()
        data class Success(val categorias: List<String>, val items: List<Item>) : ListItemsResult()
        data class Empty(
            val mensaje: String,
        ) : ListItemsResult()

        object Cleared : ListItemsResult()
        data class Error(val mensaje: String) : ListItemsResult()
    }
}