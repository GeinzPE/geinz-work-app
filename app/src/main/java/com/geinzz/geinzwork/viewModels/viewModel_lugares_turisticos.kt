package com.geinzz.geinzwork.viewModels

import android.content.Context
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.firebase.geofire.GeoFireUtils
import com.firebase.geofire.GeoLocation
import com.geinzz.geinzwork.data.model.localizate_geinz.filtrado_tiendas.TiendasCercanasFiltrada
import com.geinzz.geinzwork.data.model.localizate_geinz.filtrado_tiendas.lugares_cercanos
import com.geinzz.geinzwork.data.model.localizate_geinz.filtrado_tiendas.tiendas_por_categoria
import com.geinzz.geinzwork.data.model.localizate_geinz.inicio_geinz.lugares_turisticos
import com.geinzz.geinzwork.model.repo_lugares_turisticos
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.constantes_lista_localidades.isInternetAvailable
import com.geinzz.geinzwork.viewModels.viewModel_filtado_tiendas.carga_tiendas
import com.geinzz.geinzwork.viewModels.viewmode_servicios_tramite.carga_servicios
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.IOException
import kotlin.collections.emptyList

class viewModel_lugares_turisticos(private val savedStateHandle: SavedStateHandle) : ViewModel() {
    private val repo_lugares = repo_lugares_turisticos()

//    private val categorias_filtrado = MutableLiveData<List<String>>()
//    val _categorias_filtrados: LiveData<List<String>> get() = categorias_filtrado

//    private val lugares_turisiticos_filtrados = MutableLiveData<List<lugares_turisticos>>()
//    val _lugares_turisticos_filtrados: LiveData<List<lugares_turisticos>> get() = lugares_turisiticos_filtrados

//    private val _listaFiltrada = MutableStateFlow<List<lugares_turisticos>>(emptyList())
//    val listaFiltrada: StateFlow<List<lugares_turisticos>> = _listaFiltrada

    private val _state_carga_tiendas_cercanas =
        MutableStateFlow<carga_tienda_cercanos>(carga_tienda_cercanos.loading)
    val state_carga_tiendas_cercanas: StateFlow<carga_tienda_cercanos> =
        _state_carga_tiendas_cercanas


    var lugares_turisticos = mutableListOf<lugares_turisticos>()
        private set

    private val lugares_turisiticos = MutableLiveData<List<lugares_turisticos>>()
    val _lugares_turisticos: LiveData<List<lugares_turisticos>> get() = lugares_turisiticos

    val _lista_obtenida = MutableStateFlow<List<lugares_cercanos>>(emptyList())


    private val _stata_lugares_turisticos =
        MutableStateFlow<carga_lugares_turisticos>(carga_lugares_turisticos.loading)
    val stata_lugares_turisticos: StateFlow<carga_lugares_turisticos> = _stata_lugares_turisticos

    private val lista_completa_lugares_turisticos =
        MutableStateFlow<List<lugares_turisticos>>(emptyList())

    val _lista_completa_lugares_turisticos: StateFlow<List<lugares_turisticos>> =
        lista_completa_lugares_turisticos
    private val lista_completa_categorias_fitlrado =
        MutableStateFlow<List<String>>(emptyList())

    private var lista_general_completa = MutableStateFlow<List<lugares_cercanos>>(emptyList())
    val _lista_general_completa: StateFlow<List<lugares_cercanos>> = lista_general_completa
    private var lista_categoiras_encontrada = MutableStateFlow<List<String>>(emptyList())

    private var _lista_filtrada_turistica = MutableStateFlow<List<lugares_cercanos>>(emptyList())
    var lista_filtrada_turistica: StateFlow<List<lugares_cercanos>> = _lista_filtrada_turistica

    private val _listaTiendasGuardadasFlow = MutableSharedFlow<List<lugares_cercanos>>(replay = 1)
    private val _lista_categoria_filtrada = MutableSharedFlow<List<String>>(replay = 1)

    private val _listaTiendascompeltaFlow = MutableSharedFlow<List<lugares_cercanos>>(replay = 1)

    private val _catFiltrada = MutableStateFlow<String?>(null)

    // Lista de categorías disponibles (chips mostrados en UI)
    private val _listaCatFiltrado = MutableStateFlow<List<String>>(emptyList())

    // Radio de búsqueda en metros o km (según tu implementación)
    private val _radioFiltrado = MutableStateFlow<Double>(1000.0) // por defecto 1 km


    private val _lat_lugar = MutableStateFlow<Double>(0.0) // por defecto 1 km
    private val _lng_lugar = MutableStateFlow<Double>(0.0) // por defecto 1 km


    private val _estadoFiltrado = MutableStateFlow(TiendasCercanasFiltrada())
    val estadoFiltrado = _estadoFiltrado.asStateFlow()


    private val _estado_categoria_filtrada = MutableStateFlow("Todos")
    val estado_categoria_filtrada = _estado_categoria_filtrada.asStateFlow()

    private val _estado_radio_filtrada = MutableStateFlow(10.0)
    val estado_radio_filtrada = _estado_radio_filtrada.asStateFlow()


    fun actualizarCategoria(nuevaCategoria: String) {
        _estadoFiltrado.update { it.copy(categoriaFiltrada = nuevaCategoria) }
        _estado_categoria_filtrada.value=nuevaCategoria
    }

    fun actualizarRadio(nuevoRadio: Double) {
        val radioFinal = if (nuevoRadio == 0.0) 1.0 else nuevoRadio
        _estadoFiltrado.update { it.copy(radioFiltrado = radioFinal) }
        _estado_radio_filtrada.value=nuevoRadio
    }


    fun actualizarCategorias(lista: List<String>) {
        _estadoFiltrado.update { it.copy(listaCategorias = lista) }
    }

    fun actualizarListaCompleta(lista: List<lugares_cercanos>) {
        _estadoFiltrado.update { it.copy(listaCompleta = lista) }
    }

    fun actualizar_lat_lugar(lat:Double){
        _estadoFiltrado.update { it.copy(lugar_lat=lat) }
    }


    fun actualizar_lng_lugar(lng:Double){
        _estadoFiltrado.update { it.copy(lugar_lng =lng) }
    }
    val listaTiendasGuardadas = _listaTiendasGuardadasFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = emptyList()
        )

    val lista_categoira_filtradas=_lista_categoria_filtrada
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = emptyList()
        )

    val listaTiendasGuardadas_completa = _listaTiendascompeltaFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = emptyList()
        )


    init {
        viewModelScope.launch {
            _state_carga_tiendas_cercanas.collect { estado ->

                when (estado) {

                    is carga_tienda_cercanos.succes -> {
                        val tiendasPagadas = estado.lista_lugares
                        val categorias_filtadas =estado.lista_categorias
                        Log.d("tiendasPagadas", "SUCCESS → ${categorias_filtadas.size}")

                        _listaTiendasGuardadasFlow.emit(tiendasPagadas)
                        _lista_categoria_filtrada.emit(categorias_filtadas)
                        _listaTiendascompeltaFlow.emit(estado.lista_completa_lugares)
                    }

                    is carga_tienda_cercanos.empty -> {
                        Log.d("tiendasPagadas", "EMPTY → 0 tiendas")

                        // 👇 Aquí manejas la lista vacía como tú quieras
                        _listaTiendasGuardadasFlow.emit(emptyList())
                        _listaTiendascompeltaFlow.emit(emptyList())
                        _lista_categoria_filtrada.emit(emptyList())

                        // O si quieres mostrar la lista base en vez de vacío:
                        // _listaTiendasGuardadasFlow.emit(estado.lista_backup)
                    }

                    else -> {}
                }
            }
        }
    }



//    fun obtener_tiendas_cercanas(lat: Double, long: Double, radio: Double, localida: String) {
//        viewModelScope.launch {
//            delay(250)
//            try {
//                repo_lugares.obtenerTiendasCercanas(
//                    lat,
//                    long,
//                    10.0,
//                    localida
//                ) { it, lista_categoria ->
//                    lista_general_completa.value = it
//                    lista_categoiras_encontrada.value = lista_categoria
//                    _lista_filtrada_turistica.value=it
//                }
//            } catch (e: Exception) {
////                _state_carga_tiendas_cercanas.value =
////                    carga_tienda_cercanos.empty("Error al encontrar negocios")
//            }
//        }
//    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun obtener_tiendas_cercanas(
        lat: Double,
        long: Double,
        radio: Double,
        localidad: String
    ) {
        Log.d("falstapasdrdatosrelevats", "$lat $long $radio $localidad")
        viewModelScope.launch {
            delay(250)
            try {
                repo_lugares.obtenerTiendasCercanas(
                    lat,
                    long,
                    1.0,
                    localidad
                ) { tiendas_10km, lista_categoria ->
                    // Guardamos toda la data base completa
                    lista_general_completa.value = tiendas_10km

                    // 🔹 Filtrar las tiendas dentro del radio actual
                    val tiendas_en_radio = tiendas_10km.filter { tienda ->
                        val distanciaKm = GeoFireUtils.getDistanceBetween(
                            GeoLocation(lat, long),
                            GeoLocation(tienda.latitud, tienda.longitud)
                        ) / 1000.0
                        distanciaKm <= radio
                    }

                    // 🔹 Subcategorías disponibles dentro del radio actual
                    val subcategorias_en_radio = tiendas_en_radio.map { it.categoria }.distinct()

                    // 🔹 Actualizamos estados visibles
                    lista_categoiras_encontrada.value = subcategorias_en_radio
                    _lista_filtrada_turistica.value = tiendas_en_radio

                    Log.d("obtener_tiendas_cercanas", "✅ ${tiendas_en_radio.size} tiendas dentro de ${radio}")
                    Log.d("obtener_tiendas_cercanas", "📂 Subcategorías visibles: $subcategorias_en_radio")
                }
            } catch (e: Exception) {
                Log.e("obtener_tiendas_cercanas", "❌ Error: ${e.message}")
//            _state_carga_tiendas_cercanas.value =
//                carga_tienda_cercanos.empty("Error al encontrar negocios")
            }
        }
    }


    fun limpiar_tiendas_cercanas() {
        _state_carga_tiendas_cercanas.value = carga_tienda_cercanos.loading
        lista_general_completa.value = emptyList()
        lista_categoiras_encontrada.value = emptyList()
    }



    fun filtrar_por_subcategoria(
        lista_subcategorias: List<String>,
        subcategoria: String,
        latUser: Double,
        lonUser: Double,
        paso: Float
    ) {
        Log.d("FILTRO12313", "🔎 Iniciando filtro →$lista_subcategorias")

        viewModelScope.launch {

            val lista_base = lista_general_completa.value
            Log.d("FILTRO", "📦 Lista base recibida: ${lista_base.size} items")

            // Convertimos el float del slider a metros (entero)
            val pasoInt = paso.toInt().coerceAtLeast(1)
            val radioFinal = pasoInt * 100 // metros
            Log.d("FILTRO", "📏 Radio final: $radioFinal metros (${radioFinal / 1000} km)")

            _state_carga_tiendas_cercanas.value = carga_tienda_cercanos.loading

            try {
                // --- 1️⃣ Filtrar tiendas por distancia ---
                val tiendas_en_radio = lista_base.filter { tienda ->

                    val distanciaMetros = GeoFireUtils.getDistanceBetween(
                        GeoLocation(latUser, lonUser),
                        GeoLocation(tienda.latitud, tienda.longitud)
                    )

                    Log.d(
                        "DISTANCIA",
                        "📍 ${tienda.nombre_tienda}: ${"%.2f".format(distanciaMetros)} m (<= $radioFinal m → ${distanciaMetros <= radioFinal})"
                    )

                    distanciaMetros <= radioFinal
                }

                Log.d("FILTRO", "📌 Tiendas dentro del radio: ${tiendas_en_radio.size}")

                // --- 2️⃣ Obtener subcategorías disponibles en el radio ---
                val subcategorias_en_radio = tiendas_en_radio.map { it.categoria }.distinct()
                Log.d("FILTRO", "🏷️ Subcategorías en este radio: ${subcategorias_en_radio.size}")

                // --- 3️⃣ Filtrar por subcategoría ---
                val listaFiltradaFinal =
                    if (subcategoria == "Todos") {
                        Log.d("FILTRO", "🔄 Mostrando TODAS las subcategorías dentro del radio")
                        tiendas_en_radio
                    } else {
                        val filtradas = tiendas_en_radio.filter {
                            it.categoria.equals(subcategoria, ignoreCase = true)
                        }
                        Log.d(
                            "FILTRO",
                            "🔍 Tiendas filtradas por '$subcategoria': ${filtradas.size}"
                        )
                        filtradas
                    }

                // --- 4️⃣ Resultado final ---
                if (listaFiltradaFinal.isNotEmpty()) {
                    Log.d("FILTRO", "✅ Resultado final: ${listaFiltradaFinal.size} tiendas")

                    _state_carga_tiendas_cercanas.value = carga_tienda_cercanos.succes(
                        listaFiltradaFinal,
                        subcategorias_en_radio,
                        lista_base
                    )
                    _lista_filtrada_turistica.value = listaFiltradaFinal
                } else {
                    Log.d("FILTRO", "⚠️ Sin tiendas de '$subcategoria' dentro del radio")

                    _state_carga_tiendas_cercanas.value =
                        carga_tienda_cercanos.empty(
                            "No hay tiendas de la categoría '$subcategoria' en este radio"
                        )
                }

            } catch (e: Exception) {
                Log.e("FILTRO", "❌ Error inesperado: ${e.message}", e)
                _state_carga_tiendas_cercanas.value =
                    carga_tienda_cercanos.error("Error al obtener los resultados")
            }
        }
    }


    private val _mostrar_carga_turistico = MutableStateFlow(false)
    val mostrar_carga_turistico = _mostrar_carga_turistico.asStateFlow()


    fun mostrar_listas_completas(
        lat_lugar: Double,
        lon_lugar: Double
    ) {
        val lista_base = lista_general_completa.value
        val lista_base_categorias = lista_categoiras_encontrada.value
        val radioInicialKm = 1.0  // 🔹 Siempre 1 km por defecto

        viewModelScope.launch {
            try {
                _state_carga_tiendas_cercanas.value = carga_tienda_cercanos.loading

                if (lista_base.isNotEmpty() && lista_base_categorias.isNotEmpty()) {
                    // 🔸 Filtramos solo las tiendas dentro de 1 km
                    val listaFiltrada = lista_base.filter { tienda ->
                        val distanciaKm = GeoFireUtils.getDistanceBetween(
                            GeoLocation(lat_lugar, lon_lugar),
                            GeoLocation(tienda.latitud, tienda.longitud)
                        ) / 1000.0  // convertir a kilómetros
                        distanciaKm <= radioInicialKm
                    }

                    if (listaFiltrada.isNotEmpty()) {
                        _state_carga_tiendas_cercanas.value =
                            carga_tienda_cercanos.succes(listaFiltrada, lista_base_categorias, lista_general_completa.value)

                    } else {
                        _state_carga_tiendas_cercanas.value =
                            carga_tienda_cercanos.empty("No se encontraron negocios dentro de 1 km")
                    }
                } else {
                    _state_carga_tiendas_cercanas.value =
                        carga_tienda_cercanos.empty("No se encontraron negocios cerca")
                }
            } catch (e: Exception) {
                _state_carga_tiendas_cercanas.value =
                    carga_tienda_cercanos.error("No se encontraron negocios cerca")
            }
        }
    }


    fun lugares_turisticos(localidad: String,context: Context) {
        viewModelScope.launch {
            _mostrar_carga_turistico.value=true
            _stata_lugares_turisticos.value = carga_lugares_turisticos.loading
            delay(2000)
            try {
                if (!isInternetAvailable(context)) {
                    _mostrar_carga_turistico.value=false
                    _stata_lugares_turisticos.value = carga_lugares_turisticos.error("Sin conexión a internet 😕")
                    return@launch
                }
                val lugares_turisticos = repo_lugares.obtener_lugares_turisticos(localidad)
                val categoria_filtrado = repo_lugares.obtener_filtrado_lugares()
                if (lugares_turisticos.isNotEmpty() && categoria_filtrado.isNotEmpty()) {
                    _mostrar_carga_turistico.value=false
                    _stata_lugares_turisticos.value =
                        carga_lugares_turisticos.succes(categoria_filtrado, lugares_turisticos)
                    lista_completa_lugares_turisticos.value = lugares_turisticos
                    lista_completa_categorias_fitlrado.value = categoria_filtrado
                } else {
                    _mostrar_carga_turistico.value=false
                    _stata_lugares_turisticos.value =
                        carga_lugares_turisticos.empty("No se encontraron lugares en $localidad")
                }
            } catch (e: Exception) {
                _mostrar_carga_turistico.value=false
                _stata_lugares_turisticos.value =
                    carga_lugares_turisticos.error("Ocurrió un error inesperado al cargar los lugares de $localidad.")
            }catch (e: IOException){
                _stata_lugares_turisticos.value =
                    carga_lugares_turisticos.error("No se pudo conectar. Verifica tu conexión a internet.")
            }
        }
    }

    fun resetearEstado() {
        _stata_lugares_turisticos.value = carga_lugares_turisticos.loading
    }

    fun filtrar_lugares_turisticos(categoria: String) {
        viewModelScope.launch {

            _stata_lugares_turisticos.value = carga_lugares_turisticos.loading
            try {
                val lista_original = lista_completa_lugares_turisticos.value
                val lista_filtrada = if (categoria == "Todos") {
                    lista_original
                } else {
                    lista_original.filter { lugar ->
                        lugar.subcategoria_filtrado.any {
                            it.equals(categoria, ignoreCase = true)
                        }
                    }
                }

                if (lista_filtrada.isNotEmpty()) {

                    _stata_lugares_turisticos.value =
                        carga_lugares_turisticos.succes(
                            lista_completa_categorias_fitlrado.value,
                            lista_filtrada
                        )
                } else {
                    _stata_lugares_turisticos.value =
                        carga_lugares_turisticos.empty("No hay lugares disponibles en la categoría $categoria")
                }

            } catch (e: Exception) {
                _stata_lugares_turisticos.value =
                    carga_lugares_turisticos.error("Error al filtrar los lugares de $categoria")
            }
        }
    }


    private var todosLosLugares = emptyList<lugares_turisticos>()

    fun todos_lugares(lista: List<lugares_turisticos>) {
        todosLosLugares = lista
    }

    /** ---------- SEALED CLASS PARA LUGARES TURISTICOS ---------- */
    sealed class carga_lugares_turisticos {
        object idle : carga_lugares_turisticos()
        data class succes(
            val lista_categoria: List<String>,
            val lista_lugares: List<lugares_turisticos>
        ) : carga_lugares_turisticos()

        object loading : carga_lugares_turisticos()
        data class error(val txt: String) : carga_lugares_turisticos()
        data class empty(val txt: String) : carga_lugares_turisticos()
    }

    /** ---------- SEALED CLASS PARA CARGAS DE TIENDAS CERCANAS ---------- */
    sealed class carga_tienda_cercanos {
        data class succes(
            val lista_lugares: List<lugares_cercanos>,
            val lista_categorias: List<String>,val lista_completa_lugares: List<lugares_cercanos>
        ) : carga_tienda_cercanos()

        object loading : carga_tienda_cercanos()
        data class error(val texto: String) : carga_tienda_cercanos()
        data class empty(val txt: String) : carga_tienda_cercanos()
    }


}