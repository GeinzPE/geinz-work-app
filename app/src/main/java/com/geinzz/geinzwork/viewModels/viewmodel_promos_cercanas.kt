package com.geinzz.geinzwork.viewModels

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.lazy.items

import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.SharingStarted
import androidx.lifecycle.viewModelScope

import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.geinzz.geinzwork.data.model.EstadisticasPromo
import com.geinzz.geinzwork.data.model.data_class_promo_cerca_de_ti.RespuestaGemini
import com.geinzz.geinzwork.data.model.data_class_promo_cerca_de_ti.dataclass_promociones_cerca_de_ti
import com.geinzz.geinzwork.data.model.data_class_promo_cerca_de_ti.obj_completo
import com.geinzz.geinzwork.model.repo_promos_cercanas
import com.google.firebase.firestore.FirebaseFirestore
import com.google.gson.Gson
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlin.String
import kotlin.collections.Set

class viewmodel_promos_cercanas : ViewModel() {

    private val repo = repo_promos_cercanas()


    private val _promosCargadas =
        MutableStateFlow<List<dataclass_promociones_cerca_de_ti>>(emptyList())
    val promosCargadas: StateFlow<List<dataclass_promociones_cerca_de_ti>> = _promosCargadas


    private val _respuesta_gemini = MutableStateFlow<RespuestaGemini?>(null)
    val respuesta_gemini: StateFlow<RespuestaGemini?> = _respuesta_gemini


    private var paginaActual = 0
    private val bloque = 5
    private var cargando = false


    private val _comodidadesSeleccionadas =
        MutableStateFlow<Set<String>>(emptySet())

    val comodidadesSeleccionadas: StateFlow<Set<String>> =
        _comodidadesSeleccionadas

    fun setComodidadesDesdeLista(lista: List<String>) {
        _comodidadesSeleccionadas.value = lista.toSet()
    }


    fun togleRango_select(metodo: String) {
        val actuiales_metodos_pago = _comodidadesSeleccionadas.value
        _comodidadesSeleccionadas.value =
            if (actuiales_metodos_pago.contains(metodo)) {
                actuiales_metodos_pago - metodo
            } else {
                actuiales_metodos_pago + metodo
            }
    }

    fun limpiar_comodidad() {
        _comodidadesSeleccionadas.value = emptySet()
    }

    private val _metodosPagoSeleccionados =
        MutableStateFlow<Set<String>>(emptySet())

    val metodosPagoSeleccionados: StateFlow<Set<String>> =
        _metodosPagoSeleccionados


    fun setPagosDesdeLista(lista: List<String>) {
        _metodosPagoSeleccionados.value = lista.toSet()
    }

    fun toggleMetodoPago(metodo: String) {
        val actuales = _metodosPagoSeleccionados.value

        _metodosPagoSeleccionados.value =
            if (actuales.contains(metodo)) {
                actuales - metodo
            } else {
                actuales + metodo
            }
    }



    fun limpiarMetodosPago() {
        _metodosPagoSeleccionados.value = emptySet()
    }


    private val _rangoPrecioSeleccionado = MutableStateFlow<String?>(null)
    val rangoPrecioSeleccionado: StateFlow<String?> = _rangoPrecioSeleccionado

    fun setearRangoPrecio(rango: String) {
        _rangoPrecioSeleccionado.value =
            if (_rangoPrecioSeleccionado.value == rango) {
                null   // si lo vuelve a tocar, se deselecciona
            } else {
                rango
            }
    }


    @RequiresApi(Build.VERSION_CODES.O)
    fun cargarSiguienteBloque(
        localidad: String,
        categoria_filtrado: String,
        tiendaSeleccionada1: String?,

        ) {
        if (cargando) return
        cargando = true

        viewModelScope.launch {
            try {
                val todasLasPromos =
                    repo.obtener_promos(categoria_filtrado, localidad, tiendaSeleccionada1)
//                Log.d("ViewModelPromos", "Total promos obtenidas de DB: ${todasLasPromos.size}")

                // 🔹 eliminar duplicados globalmente por id_promocion
                val todasFiltradas = todasLasPromos
                    .map { it.dataclass_promociones_cerca_de_ti }
                    .distinctBy { it.informacion_publcacion.id_promocion }

//                Log.d("ViewModelPromos", "Promos únicas tras distinctBy: ${todasFiltradas.size} -> IDs: ${todasFiltradas.map { it.informacion_publcacion.id_promocion }}")

                // 🔹 Filtrar las promos que ya se cargaron
                val existentesIds =
                    _promosCargadas.value.map { it.informacion_publcacion.id_promocion }.toSet()
                val nuevasDisponibles =
                    todasFiltradas.filter { it.informacion_publcacion.id_promocion !in existentesIds }
                        .shuffled() // 🔹 orden aleatorio real cada vez

//                Log.d("ViewModelPromos", "Promos disponibles tras filtrar existentes: ${nuevasDisponibles.map { it.informacion_publcacion.id_promocion }}")

                // 🔹 Tomar solo hasta "bloque" elementos
                val nuevasFiltradas = nuevasDisponibles.take(bloque)

                if (nuevasFiltradas.isNotEmpty()) {
                    _promosCargadas.value = _promosCargadas.value + nuevasFiltradas
//                    Log.d("ViewModelPromos", "Total promos cargadas en StateFlow: ${_promosCargadas.value.size} -> IDs: ${_promosCargadas.value.map { it.informacion_publcacion.id_promocion }}")
                } else {
                    Log.d("ViewModelPromos", "No hay más promos nuevas para cargar.")
                }

            } catch (e: Exception) {
                Log.e("ViewModelPromos", "Error cargando bloque: ${e.message}")
            } finally {
                cargando = false
            }
        }
    }


    fun resetPromos() {
        _promosCargadas.value = emptyList()
        paginaActual = 0
    }


    private val listaCompleta =
        MutableStateFlow<List<obj_completo>>(emptyList())

    private val listaFiltrada =
        MutableStateFlow<List<obj_completo>>(emptyList())

    private val categoriasDisponibles =
        MutableStateFlow<List<String>>(emptyList())

    val _categoriasDisponibles: StateFlow<List<String>> =
        categoriasDisponibles.asStateFlow()

    private val _estadoPromos =
        MutableStateFlow<estado_carga_promociones>(
            estado_carga_promociones.loading
        )
    val estadoPromos: StateFlow<estado_carga_promociones> =
        _estadoPromos.asStateFlow()

    @RequiresApi(Build.VERSION_CODES.O)
    fun agregar_estadisticas_publicacion(
        tipo: String,
        id_promo: String,
        localidad: String,
        iduser: String
    ) {
        viewModelScope.launch {
            try {
                repo.agregar_contador_estadisticas_publicacion(tipo, id_promo, localidad, iduser)
            } catch (e: Exception) {
                Log.d("error", "$e")
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun obtener_promociones(
        localidad: String,
        tipo_filtrado: String,
    ) {
        viewModelScope.launch {
            _estadoPromos.value = estado_carga_promociones.loading

            try {
                val resultado = repo.obtener_promos(tipo_filtrado, localidad, null)

                if (resultado.isEmpty()) {
                    _estadoPromos.value =
                        estado_carga_promociones.empty("No hay promociones cerca de ti")
                    return@launch
                }

                // 🔥 LISTA BASE (NO SE TOCA)
                listaCompleta.value = resultado

                // 🔥 LISTA VISIBLE
                listaFiltrada.value = resultado

                // 🔥 CATEGORÍAS (DESDE LISTA COMPLETA)
                categoriasDisponibles.value =
                    resultado.flatMap {
                        it.dataclass_promociones_cerca_de_ti
                            .informacion_publcacion
                            .categoria
                            .split(",")
                    }
                        .map { it.trim() }
                        .distinct()

                _estadoPromos.value =
                    estado_carga_promociones.succes(listaFiltrada.value)

            } catch (e: Exception) {
                _estadoPromos.value =
                    estado_carga_promociones.error("Error al cargar promociones")
            }
        }
    }

    fun filtrarPromociones(categoria: String) {

        val base = listaCompleta.value

        val metodoPago = metodosPagoSeleccionados.value
        val comodidades = comodidadesSeleccionadas.value
        val rangoPrecio = rangoPrecioSeleccionado.value

        Log.d("FILTRO_DEBUG", "========== FILTRANDO ==========")
        Log.d("FILTRO_DEBUG", "Categoria: $categoria")
        Log.d("FILTRO_DEBUG", "Metodos: $metodoPago")
        Log.d("FILTRO_DEBUG", "Comodidades: $comodidades")
        Log.d("FILTRO_DEBUG", "Rango precio: $rangoPrecio")
        Log.d("FILTRO_DEBUG", "Total base: ${base.size}")

        val filtrada = base.filter { obj ->

            val data = obj.dataclass_promociones_cerca_de_ti

            // ✅ 1. CATEGORÍA
            val cumpleCategoria = if (categoria == "Todos") {
                true
            } else {
                data.informacion_publcacion.categoria
                    .split(",")
                    .any { it.trim().equals(categoria, ignoreCase = true) }
            }

            // ✅ 2. MÉTODOS DE PAGO (OR interno)
            val cumpleMetodoPago = if (metodoPago.isEmpty()) {
                true
            } else {
                metodoPago.any { metodo ->
                    data.pagos[metodo] == true
                }
            }

            // ✅ 3. COMODIDADES (OR interno)
            val cumpleComodidades = if (comodidades.isEmpty()) {
                true
            } else {
                comodidades.any { comod ->
                    data.comodidades[comod] == true
                }
            }

            // ✅ 4. PRECIO
            val cumplePrecio = if (rangoPrecio.isNullOrEmpty()) {
                true
            } else {
                val precio = data.precio.toDoubleOrNull() ?: return@filter false

                when (rangoPrecio) {
                    "0 - 10" -> precio in 0.0..10.0
                    "10 - 20" -> precio in 10.0..20.0
                    "20 - 30" -> precio in 20.0..30.0
                    "30 - 50" -> precio in 30.0..50.0
                    "50 - 80" -> precio in 50.0..80.0
                    "80 - 120" -> precio in 80.0..120.0
                    "120 - 200" -> precio in 120.0..200.0
                    "200 - 350" -> precio in 200.0..350.0
                    "350 - 500" -> precio in 350.0..500.0
                    "500 - 1000" -> precio in 500.0..1000.0
                    "1000 - 2500" -> precio in 1000.0..2500.0
                    "2500 - 5000" -> precio in 2500.0..5000.0
                    "Mayor a 5000" -> precio > 5000.0
                    else -> true
                }

            }

            // ✅ ENTRE GRUPOS ES AND
            cumpleCategoria &&
                    cumpleMetodoPago &&
                    cumpleComodidades &&
                    cumplePrecio
        }

        listaFiltrada.value = filtrada

        _estadoPromos.value =
//            if (filtrada.isEmpty()) {
//             estado_carga_promociones.empty("No hay promociones con esos filtros")
//            } else {
            estado_carga_promociones.succes(filtrada)
//            }

        Log.d("FILTRO_DEBUG", "Total filtrados: ${filtrada.size}")
    }


    fun filtrar_promociones_por_id(id: String) {
        val base = listaCompleta.value

        listaFiltrada.value = base.filter { obj ->
            obj.dataclass_promociones_cerca_de_ti
                .informacion_publcacion
                .id_tienda == id
        }

        _estadoPromos.value =
            if (listaFiltrada.value.isEmpty()) {
                estado_carga_promociones.empty("Esta tienda no tiene promociones activas")
            } else {
                estado_carga_promociones.succes(listaFiltrada.value)
            }
    }

    fun mostrarTodasLasPromociones() {
        val base = listaCompleta.value

        listaFiltrada.value = base

        _estadoPromos.value =
            if (base.isEmpty()) {
                estado_carga_promociones.empty("No hay promociones disponibles")
            } else {
                estado_carga_promociones.succes(base)
            }
    }


    private val _statsCache =
        mutableStateMapOf<String, EstadisticasPromo>()

    val statsCache: Map<String, EstadisticasPromo> = _statsCache

    fun cargarStats(localidad: String, idPromo: String) {
        if (_statsCache.containsKey(idPromo)) return

        viewModelScope.launch {
            _statsCache[idPromo] =
                repo.obtener_estadisticas(localidad, idPromo)
        }
    }

    fun procesar_NLP(texto: String) {
        viewModelScope.launch {
            try {
                val respuesta_NLP = repo.extraer_con_gemini(texto)

                if (!respuesta_NLP.isNullOrEmpty()) {

                    // Extrae solo el JSON válido
                    val jsonRegex = "\\{.*\\}".toRegex(RegexOption.DOT_MATCHES_ALL)
                    val match = jsonRegex.find(respuesta_NLP)
                    val limpio = match?.value ?: ""

                    if (limpio.isNotEmpty()) {
                        val gson = Gson()
                        val objeto = gson.fromJson(limpio, RespuestaGemini::class.java)
                        Log.d("NLP_OBJETO", objeto.toString())

                        _respuesta_gemini.value = objeto

                    } else {
                        _respuesta_gemini.value = RespuestaGemini(principal = "no entendí nada")
                    }

                } else {
                    _respuesta_gemini.value = RespuestaGemini(principal = "no entendí nada")
                }

            } catch (e: Exception) {
                Log.e("NLP_ERROR", "Error parseando JSON", e)
                _respuesta_gemini.value = RespuestaGemini(principal = "error procesando")
            }
        }
    }




    sealed class estado_carga_promociones {
        object loading : estado_carga_promociones()
        data class empty(val txt: String) : estado_carga_promociones()
        data class succes(val items: List<obj_completo>) : estado_carga_promociones()
        data class error(val txt: String) : estado_carga_promociones()
    }
}
