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
import com.geinzz.geinzwork.data.model.data_class_promo_cerca_de_ti.PromoConMatch
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


    private val _respuesta_gemini = MutableStateFlow<estado_Carga_respuesta_gemini?>(null)
    val respuesta_gemini: StateFlow<estado_Carga_respuesta_gemini?> = _respuesta_gemini

    private val _listaResultados = MutableStateFlow<List<String>>(emptyList())
    val listaResultados: StateFlow<List<String>> = _listaResultados

    val texto_usser_buscado =MutableStateFlow("")

    fun guardar_texto_user_buscado (txt:String){
        texto_usser_buscado.value=txt
    }



    fun eliminarItem(item: String) {
        _listaResultados.value = _listaResultados.value - item
    }

    private var paginaActual = 0
    private val bloque = 5
    private var cargando = false


    fun resetear_respuesta_de_gemini() {
        _respuesta_gemini.value = null
    }

    private val _porcentajesMatch =
        MutableStateFlow<Map<String, Int>>(emptyMap())

    val porcentajesMatch: StateFlow<Map<String, Int>> =
        _porcentajesMatch


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

    fun setearRangoPrecioDesdeNLP(rango: String?) {
        _rangoPrecioSeleccionado.value =
            if (_rangoPrecioSeleccionado.value == rango) {
                null
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

    fun retornar_lista_nuevamente(){
        _estadoPromos.value = estado_carga_promociones.succes( listaCompleta.value)
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

    fun filtrarPromociones(
        categoria: String,
        terminoNLP: String?,
        atributosNLP: List<String?>
    ) {

        val base = listaCompleta.value

        val metodoPago = metodosPagoSeleccionados.value
        val comodidades = comodidadesSeleccionadas.value
        val rangoPrecio = rangoPrecioSeleccionado.value

        val listaUsuario = buildList {
            terminoNLP?.let { termino ->
                normalizar(termino)
                    .split(" ")
                    .filter { it.length > 2 }
                    .forEach { add(it) }
            }

            atributosNLP.filterNotNull().forEach {
                add(normalizar(it))
            }
        }

        val filtradaConScore = base.mapNotNull { obj ->

            val data = obj.dataclass_promociones_cerca_de_ti

            // ✅ 1. CATEGORÍA
            val cumpleCategoria = if (categoria == "Todos") {
                true
            } else {
                data.informacion_publcacion.categoria
                    .split(",")
                    .any { it.trim().equals(categoria, ignoreCase = true) }
            }

            // ✅ 2. MÉTODOS DE PAGO
            val cumpleMetodoPago = if (metodoPago.isEmpty()) {
                true
            } else {
                metodoPago.any { metodo ->
                    data.pagos[metodo] == true
                }
            }

            // ✅ 3. COMODIDADES
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
                val precio = data.precio.toDoubleOrNull()

                if (precio == null) {
                    false
                } else {
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
            }

            // ✅ 5. NLP + SCORE
            // ✅ 5. NLP + SCORE

            val scoreNLP = if (listaUsuario.isEmpty()) {
                1.0
            } else {
                calcularCoincidencia(listaUsuario, data.terminos_clave)
            }

            val cumpleNLP = listaUsuario.isEmpty() || scoreNLP >= 0.4

            if (
                cumpleCategoria &&
                cumpleMetodoPago &&
                cumpleComodidades &&
                cumplePrecio &&
                cumpleNLP
            ) {

                val porcentaje = if (listaUsuario.isEmpty()) {
                    100
                } else {
                    (scoreNLP * 100).toInt()
                }

                // 🔥 LOG COMPLETO DEBUG
                Log.d("DEBUG_MATCH", "----------------------------")
                Log.d("DEBUG_MATCH", "Promo: ${data.informacion_publcacion.titulo}")
                Log.d("DEBUG_MATCH", "Usuario términos: $listaUsuario")
                Log.d("DEBUG_MATCH", "Promo términos: ${data.terminos_clave}")
                Log.d("DEBUG_MATCH", "Score decimal: $scoreNLP")
                Log.d("DEBUG_MATCH", "Porcentaje final: $porcentaje%")
                Log.d("DEBUG_MATCH", "----------------------------")

                Pair(obj, porcentaje)

            } else {
                null
            }


        }.sortedByDescending { it.second }

        // 🔥 Construimos lista limpia + mapa %
        val mapaPorcentajes = mutableMapOf<String, Int>()

        val soloPromos = filtradaConScore.map { pair ->

            val promo = pair.first
            val porcentaje = pair.second

            val id = promo.dataclass_promociones_cerca_de_ti
                .informacion_publcacion.id_promocion

            mapaPorcentajes[id] = porcentaje

            // 🔥 LOG DEL %
            Log.d(
                "MATCH_NLP",
                "Promo: ${promo.dataclass_promociones_cerca_de_ti.informacion_publcacion.titulo} -> $porcentaje%"
            )

            promo
        }

        // ✅ Actualizamos estados
        listaFiltrada.value = soloPromos
        _porcentajesMatch.value = mapaPorcentajes
        _estadoPromos.value =
            estado_carga_promociones.succes(soloPromos)
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


    fun procesar_NLP(texto: String, categoria: String) {
        viewModelScope.launch {
            _respuesta_gemini.value = estado_Carga_respuesta_gemini.loading
            try {
                val respuesta_NLP = repo.extraer_con_gemini(texto, categoria)

                if (!respuesta_NLP.isNullOrEmpty()) {

                    // Extrae solo el JSON válido
                    val jsonRegex = "\\{.*\\}".toRegex(RegexOption.DOT_MATCHES_ALL)
                    val match = jsonRegex.find(respuesta_NLP)
                    val limpio = match?.value ?: ""

                    if (limpio.isNotEmpty()) {
                        val gson = Gson()
                        val objeto = gson.fromJson(limpio, RespuestaGemini::class.java)
                        Log.d("NLP_OBJETO", objeto.toString())
                        _respuesta_gemini.value = estado_Carga_respuesta_gemini.succes(objeto)

                        _respuesta_gemini.value?.let { estado ->
                            if (estado is estado_Carga_respuesta_gemini.succes) {
                                _listaResultados.value = buildList {
                                    estado.items?.principal?.let { add(it) }
                                    estado.items?.atributos?.let { addAll(it) }
                                }
                            }
                        }


                    } else {
                        _respuesta_gemini.value =
                            estado_Carga_respuesta_gemini.empty("no entendí nada")
                    }

                } else {
                    _respuesta_gemini.value = estado_Carga_respuesta_gemini.empty("no entendí nada")
                }

            } catch (e: Exception) {
                Log.e("NLP_ERROR", "Error parseando JSON", e)
                _respuesta_gemini.value = estado_Carga_respuesta_gemini.error("se produjo un error")
            }
        }
    }

    fun normalizar(texto: String): String {
        return texto
            .lowercase()
            .trim()
    }


//    fun fiiltrar_por_termino_y_atributos(
//        termino: String?,
//        atributo: List<String?>
//    ) {
//
//        viewModelScope.launch {
//
//            val listaUsuario = buildList {
//                termino?.let { add(normalizar(it)) }
//                atributo.filterNotNull().forEach {
//                    add(normalizar(it))
//                }
//            }
//
//            val resultado = listaCompleta.value
//                .map { promo ->
//                    val score = calcularCoincidencia(
//                        listaUsuario,
//                        promo.dataclass_promociones_cerca_de_ti.terminos_clave
//                    )
//                    promo to score
//                }
//                .filter { it.second >= 0.4 } // mínimo 40%
//                .sortedByDescending { it.second }
//                .map { it.first }
//
//            // Aquí actualizas tu StateFlow de resultados
//            // _listaFiltrada.value = resultado
//        }
//    }


    fun calcularCoincidencia(
        usuario: List<String>,
        promo: List<String>
    ): Double {

        if (usuario.isEmpty()) return 0.0

        val promoNormalizada = promo.map { normalizar(it) }

        val coincidencias = usuario.count { terminoUser ->
            promoNormalizada.any { terminoPromo ->
                terminoPromo.contains(terminoUser)
            }
        }

        return coincidencias.toDouble() / usuario.size.toDouble()
    }


    sealed class estado_Carga_respuesta_gemini {
        object loading : estado_Carga_respuesta_gemini()
        data class succes(val items: RespuestaGemini?) : estado_Carga_respuesta_gemini()
        data class error(val texto_error: String) : estado_Carga_respuesta_gemini()
        data class empty(val text_vacio: String) : estado_Carga_respuesta_gemini()
        object idle : estado_Carga_respuesta_gemini()
    }


    sealed class estado_carga_promociones {
        object loading : estado_carga_promociones()
        data class empty(val txt: String) : estado_carga_promociones()
        data class succes(val items: List<obj_completo>) : estado_carga_promociones()
        data class error(val txt: String) : estado_carga_promociones()
    }
}