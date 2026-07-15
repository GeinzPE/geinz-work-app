package com.geinzz.geinzwork.viewModels

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.getValue

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
import com.geinzz.geinzwork.data.model.data_class_promo_cerca_de_ti.DatosResponse
import com.geinzz.geinzwork.data.model.data_class_promo_cerca_de_ti.IdScore
import com.geinzz.geinzwork.data.model.data_class_promo_cerca_de_ti.PromoConMatch
import com.geinzz.geinzwork.data.model.data_class_promo_cerca_de_ti.ResAlgoliaFiltrado
import com.geinzz.geinzwork.data.model.data_class_promo_cerca_de_ti.RespuestaGemini
import com.geinzz.geinzwork.data.model.data_class_promo_cerca_de_ti.dataclass_promociones_cerca_de_ti
import com.geinzz.geinzwork.data.model.data_class_promo_cerca_de_ti.datos_para_filtrado_manual
import com.geinzz.geinzwork.data.model.data_class_promo_cerca_de_ti.filtrado_feed_promociones
import com.geinzz.geinzwork.data.model.data_class_promo_cerca_de_ti.obj_completo
import com.geinzz.geinzwork.data.model.data_class_promo_cerca_de_ti.tiendas_con_mas_de_una_promo
import com.geinzz.geinzwork.model.repo_promos_cercanas
import com.geinzz.geinzwork.retrofit.objet_retrofit
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import kotlin.String
import kotlin.collections.Set
import kotlin.text.get

class viewmodel_promos_cercanas : ViewModel() {

    private val repo = repo_promos_cercanas()


    private val _promosCargadas =
        MutableStateFlow<List<dataclass_promociones_cerca_de_ti>>(emptyList())
    val promosCargadas: StateFlow<List<dataclass_promociones_cerca_de_ti>> = _promosCargadas


    private val _tiendas_con_mas_de_una_promo=MutableStateFlow<List<tiendas_con_mas_de_una_promo>> (emptyList())
    val tiendas_con_mas_de_una_promo: StateFlow<List<tiendas_con_mas_de_una_promo>> = _tiendas_con_mas_de_una_promo


    private val _respuesta_gemini = MutableStateFlow<estado_Carga_respuesta_gemini?>(null)
    val respuesta_gemini: StateFlow<estado_Carga_respuesta_gemini?> = _respuesta_gemini

    private val _listaResultados = MutableStateFlow<List<String>>(emptyList())
    val listaResultados: StateFlow<List<String>> = _listaResultados

    val texto_usser_buscado = MutableStateFlow("")

    fun guardar_texto_user_buscado(txt: String) {
        texto_usser_buscado.value = txt
    }

    private val _obtener_categorias = MutableStateFlow<List<filtrado_feed_promociones>>(emptyList())
    val obtener_categorias: StateFlow<List<filtrado_feed_promociones>> = _obtener_categorias

    init {
        Log.d("VM_INIT", "🔥 ViewModel creado - cargando categorias")
        obtener_filtrado_Categorias()
    }

    fun limpiarSubcategorias() {
        _subcategoria_seleccionada.value = emptyList()
    }

    fun eliminarItem(item: String) {
        _listaResultados.value = _listaResultados.value - item
    }

    private var paginaActual = 0
    private val bloque = 5
    private var cargando = false


    fun resetear_respuesta_de_gemini() {
        _respuesta_gemini.value = estado_Carga_respuesta_gemini.idle
    }

    fun resetear_Estado_respuesta_IA(){

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

    var resultado by mutableStateOf<DatosResponse?>(null)
        private set

    var resultado_encontrado_algolia by mutableStateOf<ResAlgoliaFiltrado?>(null)
        private set

    var loading by mutableStateOf(false)
        private set


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

//    private val _rangoPrecioSeleccionado_por_tienda = MutableStateFlow<String?>(null)
//    val _rangoPrecioSeleccionado_por_tienda: StateFlow<String?> = _rangoPrecioSeleccionado_por_tienda
//
//
//    fun setearRangoPrecioDesdeNLP_por_tienda(rango: String?) {
//        _rangoPrecioSeleccionado_por_tienda.value =
//            if (_rangoPrecioSeleccionado_por_tienda.value == rango) {
//                null
//            } else {
//                rango
//            }
//    }

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
    fun limpiarCategoria() {
        _categoria_seleccionada.value = ""
    }
    fun limpiarRangoPrecio() {
        _rangoPrecioSeleccionado.value = null
    }

    private val _subcategoria_seleccionada =
        MutableStateFlow<List<String>>(emptyList())

    val subcategoria_seleccionada: StateFlow<List<String>> =
        _subcategoria_seleccionada

    fun toggle_subcategoria(item: String) {
        val actuales = _subcategoria_seleccionada.value.toMutableList()

        _subcategoria_seleccionada.value =
            if (actuales.contains(item)) {
                actuales - item
            } else {
                actuales + item
            }
    }
    private val _categoria_seleccionada = MutableStateFlow("")
    val categoria_seleccionada: StateFlow<String> = _categoria_seleccionada

    fun toggleCategoria(cat: String) {
        val nueva = if (_categoria_seleccionada.value == cat) "" else cat
        _categoria_seleccionada.value = nueva
        if (nueva.isEmpty()) limpiarSubcategorias()
    }
    private var ultimaTiendaCargada: String = ""

    fun cargarSiguienteBloque(
        localidad: String,
        categoria_filtrado: String,
        tiendaSeleccionada1: String,
    ) {
        // 🔹 Si cambió la tienda, limpiar sin importar si estaba cargando
        if (ultimaTiendaCargada != tiendaSeleccionada1) {
            _promosCargadas.value = emptyList()
            ultimaTiendaCargada = tiendaSeleccionada1
            cargando = false
        }

        if (cargando) return
        cargando = true

        viewModelScope.launch {
            try {
                val todasLasPromos = repo.obtener_promos(categoria_filtrado, localidad, tiendaSeleccionada1)

                val todasFiltradas = todasLasPromos
                    .map { it.dataclass_promociones_cerca_de_ti }
                    .distinctBy { it.informacion_publcacion.id_promocion }

                val existentesIds = _promosCargadas.value
                    .map { it.informacion_publcacion.id_promocion }
                    .toSet()

                val nuevasFiltradas = todasFiltradas
                    .filter { it.informacion_publcacion.id_promocion !in existentesIds }
                    .shuffled()
                    .take(bloque)

                if (nuevasFiltradas.isNotEmpty()) {
                    _promosCargadas.value = _promosCargadas.value + nuevasFiltradas
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

    private val _estadoMicrofono = MutableStateFlow<EstadoMicrofono>(EstadoMicrofono.Idle)
    val estadoMicrofono: StateFlow<EstadoMicrofono> = _estadoMicrofono
    fun resetear_microfono() {
        _estadoMicrofono.value = EstadoMicrofono.Idle
    }

    fun iniciar_grabacion() {
        _estadoMicrofono.value = EstadoMicrofono.Grabando
    }
    fun enviar_audio_a_whisper(file: File, onTextoListo: (String) -> Unit) {
        viewModelScope.launch {
            try {
                _estadoMicrofono.value = EstadoMicrofono.Procesando

                val requestBody = file.asRequestBody("audio/3gpp".toMediaType())
                val part = MultipartBody.Part.createFormData("file", file.name, requestBody)

                val response = objet_retrofit.api.transcribirAudio(part)

                withContext(Dispatchers.Main) {
                    onTextoListo(response.texto)
                }

                _estadoMicrofono.value = EstadoMicrofono.Idle

            } catch (e: Exception) {
                Log.e("WHISPER", "Error: ${e.message}")
                _estadoMicrofono.value = EstadoMicrofono.Error("Error: ${e.message}")
            }
        }
    }

    fun filtrar_terminos_nlp_seleccionados() {
        val seleccionados = _terminos_nlp_seleccionados.value
        Log.d("BUG_NLP_FILTRO", "🎯 filtrar_terminos_nlp_seleccionados() — seleccionados=$seleccionados")

        if (seleccionados.isEmpty()) {
            Log.d("BUG_NLP_FILTRO", "⚠️ seleccionados vacío — abortando")
            _estadoPromos.value = estado_carga_promociones.empty("Selecciona al menos un término")
            return
        }

        val base = listaCompleta.value
        Log.d("BUG_NLP_FILTRO", "📦 listaCompleta.value tiene ${base.size} promos ANTES de filtrar")

        base.forEachIndexed { index, obj ->
            val data = obj.dataclass_promociones_cerca_de_ti
            Log.d("BUG_NLP_FILTRO", "   [$index] titulo='${data.informacion_publcacion.titulo}' | terminos_clave=${data.terminos_clave} | categoria='${data.informacion_publcacion.categoria}'")
        }

        val filtradas = base.filter { obj ->
            val data = obj.dataclass_promociones_cerca_de_ti
            val textoCompleto = buildString {
                append(data.terminos_clave.joinToString(" "))
                append(" ")
                append(data.informacion_publcacion.titulo)
                append(" ")
                append(data.informacion_publcacion.categoria)
            }.lowercase()

            val matchea = seleccionados.any { termino -> textoCompleto.contains(termino.lowercase()) }
            Log.d("BUG_NLP_FILTRO", "   🔍 comparando textoCompleto='$textoCompleto' contra seleccionados=$seleccionados -> matchea=$matchea")
            matchea
        }

        Log.d("BUG_NLP_FILTRO", "✅ Resultado filtro: ${filtradas.size} promos matchearon de ${base.size} totales")

        listaFiltrada.value = filtradas
        _promosAcumuladas.value = filtradas

        _estadoPromos.value = if (filtradas.isEmpty())
            estado_carga_promociones.empty("No hay resultados para los términos seleccionados")
        else
            estado_carga_promociones.succes(filtradas)
    }
    fun resetPromos() {
        _promosCargadas.value = emptyList()
        paginaActual = 0
        cargando = false
        ultimaTiendaCargada = "" // 🔹 esto faltaba

    }


    private val listaCompleta =
        MutableStateFlow<List<obj_completo>>(emptyList())

    private val listaFiltrada =
        MutableStateFlow<List<obj_completo>>(emptyList())

    private val categoriasDisponibles =
        MutableStateFlow<List<String>>(emptyList())



    private val _estadoPromos =
        MutableStateFlow<estado_carga_promociones>(
            estado_carga_promociones.loading
        )
    val estadoPromos: StateFlow<estado_carga_promociones> =
        _estadoPromos.asStateFlow()



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

    fun obtener_filtrado_Categorias() {
        viewModelScope.launch {
            try {
                val datos = repo.obtener_categorias_firebase()
                if (datos.isNotEmpty()) {
                    _obtener_categorias.value = datos
                } else {
                    _obtener_categorias.value = emptyList()
                }
            } catch (e: Exception) {
                Log.d("error_obtenr_cat", "$e")
                _obtener_categorias.value = emptyList()
            }
        }
    }



    fun retornar_lista_nuevamente() {
        _estadoPromos.value = estado_carga_promociones.succes(listaCompleta.value)
    }




    private val PAGINA_SIZE = 5
    private var ultimoDocumento: DocumentSnapshot? = null

    private var listaIds: List<String> = emptyList()
    private var paginaActual_ = 0
    private val PAGE_SIZE_IDS = 5


    private val _hayMasPaginas = MutableStateFlow(true)
    val hayMasPaginas: StateFlow<Boolean> = _hayMasPaginas

    private val _cargandoPagina = MutableStateFlow(false)
    val cargandoPagina: StateFlow<Boolean> = _cargandoPagina

    private val _promosAcumuladas = MutableStateFlow<List<obj_completo>>(emptyList())

    private val _esPrimeraCarga = MutableStateFlow(true)
    val esPrimeraCarga: StateFlow<Boolean> = _esPrimeraCarga
    private var listaIdsConScore: List<IdScore> = emptyList()

    private val _estado_Carga_tienda_select =
        MutableStateFlow<estado_carga_tienda_Seleccionada>(estado_carga_tienda_Seleccionada.idle)
    val estado_Carga_tienda_select: StateFlow<estado_carga_tienda_Seleccionada> =
        _estado_Carga_tienda_select

    var modoBusquedaIA by mutableStateOf(false)
    // 🔧 Backup del estado de búsqueda NLP, para restaurarlo si el usuario cancela
// la selección de una tienda (evita perder los resultados del NLP).
    private var backupNlp_modoBusquedaIA: Boolean = false
    private var backupNlp_listaIdsConScore: List<IdScore> = emptyList()
    private var backupNlp_listaCompleta: List<obj_completo> = emptyList()
    private var backupNlp_promosAcumuladas: List<obj_completo> = emptyList()
    private var backupNlp_paginaActual_: Int = 0
    private var backupNlp_hayMasPaginas: Boolean = true
    private var hayBackupNlp: Boolean = false
    private var backupNlp_terminos: List<String> = emptyList()
    private var backupNlp_terminosSeleccionados: List<String> = emptyList()
    private var backupNlp_resultado: DatosResponse? = null
    private var backupNlp_valorBuscado: String = ""

    private val _filtro_tienda_sin_resultados = MutableStateFlow(false)
    val filtro_tienda_sin_resultados: StateFlow<Boolean> = _filtro_tienda_sin_resultados

    fun resetear_filtro_sin_resultados() {
        _filtro_tienda_sin_resultados.value = false
    }

    private val _terminos_nlp = MutableStateFlow<List<String>>(emptyList())
    val terminos_nlp: StateFlow<List<String>> = _terminos_nlp

    private val _terminos_nlp_seleccionados = MutableStateFlow<List<String>>(emptyList())
    val terminos_nlp_seleccionados: StateFlow<List<String>> = _terminos_nlp_seleccionados

    fun toggleTerminoNlp(termino: String) {
        val actuales = _terminos_nlp_seleccionados.value.toMutableList()
        _terminos_nlp_seleccionados.value =
            if (actuales.contains(termino)) actuales - termino
            else actuales + termino
    }

    fun limpiarTerminosNlp() {
        _terminos_nlp.value = emptyList()
        _terminos_nlp_seleccionados.value = emptyList()
    }


    fun restaurar_busqueda_nlp_si_existe(): String {
        if (!hayBackupNlp) return ""
        modoBusquedaIA = backupNlp_modoBusquedaIA
        listaIdsConScore = backupNlp_listaIdsConScore
        listaCompleta.value = backupNlp_listaCompleta
        listaFiltrada.value = backupNlp_listaCompleta
        _promosAcumuladas.value = backupNlp_promosAcumuladas
        paginaActual_ = backupNlp_paginaActual_
        _hayMasPaginas.value = backupNlp_hayMasPaginas
        _terminos_nlp.value = backupNlp_terminos
        _terminos_nlp_seleccionados.value = backupNlp_terminosSeleccionados
        resultado = backupNlp_resultado
        _estadoPromos.value = estado_carga_promociones.succes(backupNlp_promosAcumuladas)
        val textoRestaurado = backupNlp_valorBuscado
        hayBackupNlp = false
        return textoRestaurado
    }
    private val _totalPromosTienda = MutableStateFlow(0)
    private var jobPromociones: Job? = null
    fun obtener_promociones_2da(localidad: String, tipo_filtrado: String, tienda_seleccionada: String?,  textoBuscadoActual: String = "" ) {
        jobPromociones?.cancel()   // 👈 AGREGA ESTA LÍNEA: cancela la petición anterior si sigue en curso
        if (tienda_seleccionada != null && modoBusquedaIA) {
            backupNlp_modoBusquedaIA = modoBusquedaIA
            backupNlp_listaIdsConScore = listaIdsConScore
            backupNlp_listaCompleta = listaCompleta.value
            backupNlp_promosAcumuladas = _promosAcumuladas.value
            backupNlp_paginaActual_ = paginaActual_
            backupNlp_hayMasPaginas = _hayMasPaginas.value
            backupNlp_terminos = _terminos_nlp.value                     // 👈 AGREGAR
            backupNlp_terminosSeleccionados = _terminos_nlp_seleccionados.value  // 👈 AGREGAR
            backupNlp_resultado = resultado                              // 👈 AGREGAR
            backupNlp_valorBuscado = textoBuscadoActual                  // 👈 AGREGAR
            hayBackupNlp = true
        }
        ultimoDocumento = null
        _hayMasPaginas.value = true
        _promosAcumuladas.value = emptyList()
        listaCompleta.value = emptyList()
        modoBusquedaIA = false  // 👈 resetear modo IA
        listaIdsConScore = emptyList() // 👈 limpiar ids anteriores
        paginaActual_ = 0 // 👈 resetear página

        jobPromociones = viewModelScope.launch{

        if (tienda_seleccionada != null) {
                // ── MODO TIENDA: trae TODO sin paginación ──────────────────
                _estado_Carga_tienda_select.value = estado_carga_tienda_Seleccionada.loading

                try {
                    val (todasLasPromos, pagos, comodidades) = repo.obtener_todas_promos_de_tienda(
                        tienda_seleccionada, localidad
                    )


                    if (todasLasPromos.isEmpty()) {
                        _estado_Carga_tienda_select.value = estado_carga_tienda_Seleccionada.empty(
                            "No hay promos para esta tienda"
                        )
                        return@launch
                    }

                    _totalPromosTienda.value = todasLasPromos.size
                    _promosAcumuladas.value = todasLasPromos
                    listaCompleta.value = todasLasPromos
                    listaFiltrada.value = todasLasPromos
                    _hayMasPaginas.value = false

                    _estado_Carga_tienda_select.value = estado_carga_tienda_Seleccionada.succes(
                        items = todasLasPromos,
                        total = todasLasPromos.size,
                        pagos = pagos,          // 🔥
                        comodidades = comodidades // 🔥
                    )
                } catch (e: Exception) {
                    _estado_Carga_tienda_select.value = estado_carga_tienda_Seleccionada.idle
                }

            } else {
                // ── MODO GENERAL: paginación normal ────────────────────────
                _estado_Carga_tienda_select.value = estado_carga_tienda_Seleccionada.idle
                if (_esPrimeraCarga.value) {
                    _estadoPromos.value = estado_carga_promociones.loading
                }

                try {
                    val (nueva, nuevoCursor) = repo.obtener_promos_paginado2(
                        esPrimeraCarga.value,
                        null,
                        localidad,
                        ultimoDocumento,
                        PAGINA_SIZE
                    )

                    if (nueva.isEmpty()) {
                        _estadoPromos.value = estado_carga_promociones.empty("No hay promociones cerca de ti")
                        _hayMasPaginas.value = false
                        _esPrimeraCarga.value = false
                        return@launch
                    }

                    ultimoDocumento = nuevoCursor
                    _hayMasPaginas.value = nuevoCursor != null

                    _promosAcumuladas.value = nueva
                    listaCompleta.value = nueva
                    listaFiltrada.value = nueva

                    if (esPrimeraCarga.value || _tiendas_con_mas_de_una_promo.value.isEmpty()) {   // 👈 cambio
                        val tiendas = nueva
                            .map { it.lista_tiendas_con_mas_promo }
                            .firstOrNull { it.isNotEmpty() }
                            ?: emptyList()
                        _tiendas_con_mas_de_una_promo.value = tiendas
                    }

                    _estadoPromos.value = estado_carga_promociones.succes(
                        nueva.distinctBy {
                            it.dataclass_promociones_cerca_de_ti.informacion_publcacion.id_promocion
                        }
                    )
                    Log.d("BUG_TIENDA", "🧬 _estadoPromos.value asignado — total=${nueva.size}")

                    _esPrimeraCarga.value = false

                } catch (e: Exception) {
                    _estadoPromos.value = estado_carga_promociones.error("Error al cargar promociones")
                    _esPrimeraCarga.value = false
                }
            }
        }
    }
    fun cargarSiguientePagina(localidad: String, tipo_filtrado: String, id_tienda_select: String?) {
        if (_cargandoPagina.value || !_hayMasPaginas.value) return

        viewModelScope.launch {
            _cargandoPagina.value = true

            try {
                val (nuevas, nuevoCursor) = repo.obtener_promos_paginado2(
                    esPrimeraCarga.value, id_tienda_select, localidad, ultimoDocumento, PAGINA_SIZE
                )

                if (nuevas.isEmpty()) {
                    _hayMasPaginas.value = false
                    return@launch
                }

                ultimoDocumento = nuevoCursor
                _hayMasPaginas.value = nuevoCursor != null

                val idsExistentes = _promosAcumuladas.value
                    .map { it.dataclass_promociones_cerca_de_ti.informacion_publcacion.id_promocion }
                    .toSet()

                val sinDuplicados = nuevas.filter {
                    it.dataclass_promociones_cerca_de_ti.informacion_publcacion.id_promocion !in idsExistentes
                }

                // 🔒 distinctBy como red de seguridad final
                val listaActualizada = (_promosAcumuladas.value + sinDuplicados)
                    .distinctBy { it.dataclass_promociones_cerca_de_ti.informacion_publcacion.id_promocion }

                _promosAcumuladas.value = listaActualizada
                listaCompleta.value = listaActualizada
                listaFiltrada.value = listaActualizada

                _estadoPromos.value = estado_carga_promociones.succes(listaActualizada)

            } catch (e: Exception) {
                Log.e("PAGINACION", "Error cargando siguiente página", e)
            } finally {
                _cargandoPagina.value = false
            }
        }
    }

    fun filtrar_promos_de_tienda(id_tienda: String) {
        val base = listaCompleta.value
            .filter {
                it.dataclass_promociones_cerca_de_ti
                    .informacion_publcacion.id_tienda == id_tienda
            }

        val subcats = _subcategoria_seleccionada.value
        val rango = _rangoPrecioSeleccionado.value

        val filtrada = base.filter { obj ->
            val data = obj.dataclass_promociones_cerca_de_ti

            val cumpleCategoria = if (subcats.isEmpty()) true
            else subcats.any { cat ->
                data.terminos_clave.any { it.equals(cat, ignoreCase = true) } ||
                        data.informacion_publcacion.categoria.equals(cat, ignoreCase = true)
            }

            val cumpleRango = if (rango.isNullOrEmpty()) true
            else data.rango == rango

            cumpleCategoria && cumpleRango
        }

        // 🔥 Si no hay resultados → NO cambiar el estado, solo notificar con callback
        if (filtrada.isEmpty()) {
            _filtro_tienda_sin_resultados.value = true  // ← signal para Toast en UI
            return  // ← NO tocar estadoPromos
        }

        _filtro_tienda_sin_resultados.value = false
        listaFiltrada.value = filtrada
        _estadoPromos.value = estado_carga_promociones.succes(filtrada)
    }

    suspend fun obtenerPrimeraPaginaDesdeIds(
        lista: List<IdScore>
    ): List<obj_completo> {

        val ordenados = lista.sortedByDescending { it.score }

        val sub = ordenados.take(PAGE_SIZE_IDS)
        val subIds = sub.map { it.id }

        val nuevas = repo.obtenerPromosPorIdsProcesadas(
            ids = subIds,
            limite = PAGE_SIZE_IDS
        )

        val mapa = nuevas.associateBy {
            it.dataclass_promociones_cerca_de_ti.informacion_publcacion.id_promocion
        }

        // 🔥 respetar orden de Algolia
        return sub.mapNotNull { mapa[it.id] }
    }


    fun cargarSiguientePaginaPorIds() {

        if (_cargandoPagina.value || !_hayMasPaginas.value) {
            Log.d("IDS_DEBUG", "⛔ No carga: cargando=${_cargandoPagina.value}, hayMas=${_hayMasPaginas.value}")
            return
        }

        viewModelScope.launch {
            _cargandoPagina.value = true

            try {
                val desde = paginaActual_ * PAGE_SIZE_IDS
                val hasta = minOf(desde + PAGE_SIZE_IDS, listaIdsConScore.size)

                Log.d("IDS_DEBUG", "📄 Página: $paginaActual_")
                Log.d("IDS_DEBUG", "📌 Rango: $desde -> $hasta")
                Log.d("IDS_DEBUG", "📊 Total IDs: ${listaIdsConScore.size}")

                if (desde >= listaIdsConScore.size) {
                    Log.d("IDS_DEBUG", "🚫 No hay más datos")
                    _hayMasPaginas.value = false
                    return@launch
                }

                val sub = listaIdsConScore.subList(desde, hasta)
                val subIds = sub.map { it.id }

                Log.d("IDS_DEBUG", "🧩 IDs solicitados: $subIds")

                // 🔥 consulta
                val nuevas = repo.obtenerPromosPorIdsProcesadas(
                    ids = subIds,
                    limite = PAGE_SIZE_IDS
                )

                Log.d("IDS_DEBUG", "📦 Promos recibidas: ${nuevas.size}")
                Log.d("IDS_DEBUG", "📦 IDs recibidos: ${
                    nuevas.map { it.dataclass_promociones_cerca_de_ti.informacion_publcacion.id_promocion }
                }")

                // 🧠 ordenar según Algolia
                val mapa = nuevas.associateBy {
                    it.dataclass_promociones_cerca_de_ti.informacion_publcacion.id_promocion
                }

                val ordenadas = sub.mapNotNull { mapa[it.id] }

                Log.d("IDS_DEBUG", "✅ Ordenadas: ${
                    ordenadas.map { it.dataclass_promociones_cerca_de_ti.informacion_publcacion.id_promocion }
                }")


// DESPUÉS
                val idsExistentes = _promosAcumuladas.value
                    .map { it.dataclass_promociones_cerca_de_ti.informacion_publcacion.id_promocion }
                    .toSet()

                val sinDuplicados = ordenadas.filter {
                    it.dataclass_promociones_cerca_de_ti.informacion_publcacion.id_promocion !in idsExistentes
                }

                val listaActualizada = _promosAcumuladas.value + sinDuplicados

                Log.d("IDS_DEBUG", "📚 Total acumulado: ${listaActualizada.size}")

                _promosAcumuladas.value = listaActualizada
                listaCompleta.value = listaActualizada
                listaFiltrada.value = listaActualizada

                paginaActual_++ // 🔥 corregido

                _hayMasPaginas.value = hasta < listaIdsConScore.size // 🔥 corregido

                Log.d("IDS_DEBUG", "➡️ Hay más páginas: ${_hayMasPaginas.value}")

                _estadoPromos.value =
                    estado_carga_promociones.succes(listaActualizada)

            } catch (e: Exception) {
                Log.e("IDS_DEBUG", "❌ Error cargando página", e)
            } finally {
                _cargandoPagina.value = false
            }
        }
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

//    fun filtrarPromociones(
//        categoria: String,
//        terminoNLP: String?,
//        atributosNLP: List<String?>
//    ) {
//
//        val base = listaCompleta.value
//
//        val metodoPago = metodosPagoSeleccionados.value
//        val comodidades = comodidadesSeleccionadas.value
//        val rangoPrecio = rangoPrecioSeleccionado.value
//
//        val listaUsuario = buildList {
//            terminoNLP?.let { termino ->
//                normalizar(termino)
//                    .split(" ")
//                    .filter { it.length > 2 }
//                    .forEach { add(it) }
//            }
//
//            atributosNLP.filterNotNull().forEach {
//                add(normalizar(it))
//            }
//        }
//
//        val filtradaConScore = base.mapNotNull { obj ->
//
//            val data = obj.dataclass_promociones_cerca_de_ti
//
//            // ✅ 1. CATEGORÍA
//            val cumpleCategoria = if (categoria == "Todos") {
//                true
//            } else {
//                data.informacion_publcacion.categoria
//                    .split(",")
//                    .any { it.trim().equals(categoria, ignoreCase = true) }
//            }
//
//            // ✅ 2. MÉTODOS DE PAGO
//            val cumpleMetodoPago = if (metodoPago.isEmpty()) {
//                true
//            } else {
//                metodoPago.any { metodo ->
//                    data.pagos[metodo] == true
//                }
//            }
//
//            // ✅ 3. COMODIDADES
//            val cumpleComodidades = if (comodidades.isEmpty()) {
//                true
//            } else {
//                comodidades.any { comod ->
//                    data.comodidades[comod] == true
//                }
//            }
//
//            // ✅ 4. PRECIO
//            val cumplePrecio = if (rangoPrecio.isNullOrEmpty()) {
//                true
//            } else {
//                val precio = data.precio.toDoubleOrNull()
//
//                if (precio == null) {
//                    false
//                } else {
//                    when (rangoPrecio) {
//                        "0 - 10" -> precio in 0.0..10.0
//                        "10 - 20" -> precio in 10.0..20.0
//                        "20 - 30" -> precio in 20.0..30.0
//                        "30 - 50" -> precio in 30.0..50.0
//                        "50 - 80" -> precio in 50.0..80.0
//                        "80 - 120" -> precio in 80.0..120.0
//                        "120 - 200" -> precio in 120.0..200.0
//                        "200 - 350" -> precio in 200.0..350.0
//                        "350 - 500" -> precio in 350.0..500.0
//                        "500 - 1000" -> precio in 500.0..1000.0
//                        "1000 - 2500" -> precio in 1000.0..2500.0
//                        "2500 - 5000" -> precio in 2500.0..5000.0
//                        "Mayor a 5000" -> precio > 5000.0
//                        else -> true
//                    }
//                }
//            }
//
//            // ✅ 5. NLP + SCORE
//            // ✅ 5. NLP + SCORE
//
//            val scoreNLP = if (listaUsuario.isEmpty()) {
//                1.0
//            } else {
//                calcularCoincidencia(listaUsuario, data.terminos_clave)
//            }
//
//            val cumpleNLP = listaUsuario.isEmpty() || scoreNLP >= 0.4
//
//            if (
//                cumpleCategoria &&
//                cumpleMetodoPago &&
//                cumpleComodidades &&
//                cumplePrecio &&
//                cumpleNLP
//            ) {
//
//                val porcentaje = if (listaUsuario.isEmpty()) {
//                    100
//                } else {
//                    (scoreNLP * 100).toInt()
//                }
//
//                // 🔥 LOG COMPLETO DEBUG
//                Log.d("DEBUG_MATCH", "----------------------------")
//                Log.d("DEBUG_MATCH", "Promo: ${data.informacion_publcacion.titulo}")
//                Log.d("DEBUG_MATCH", "Usuario términos: $listaUsuario")
//                Log.d("DEBUG_MATCH", "Promo términos: ${data.terminos_clave}")
//                Log.d("DEBUG_MATCH", "Score decimal: $scoreNLP")
//                Log.d("DEBUG_MATCH", "Porcentaje final: $porcentaje%")
//                Log.d("DEBUG_MATCH", "----------------------------")
//
//                Pair(obj, porcentaje)
//
//            } else {
//                null
//            }
//
//
//        }.sortedByDescending { it.second }
//
//        // 🔥 Construimos lista limpia + mapa %
//        val mapaPorcentajes = mutableMapOf<String, Int>()
//
//        val soloPromos = filtradaConScore.map { pair ->
//
//            val promo = pair.first
//            val porcentaje = pair.second
//
//            val id = promo.dataclass_promociones_cerca_de_ti
//                .informacion_publcacion.id_promocion
//
//            mapaPorcentajes[id] = porcentaje
//
//            // 🔥 LOG DEL %
//            Log.d(
//                "MATCH_NLP",
//                "Promo: ${promo.dataclass_promociones_cerca_de_ti.informacion_publcacion.titulo} -> $porcentaje%"
//            )
//
//            promo
//        }
//
//        // ✅ Actualizamos estados
//        listaFiltrada.value = soloPromos
//        _porcentajesMatch.value = mapaPorcentajes
//        _estadoPromos.value =
//            estado_carga_promociones.succes(soloPromos)
//    }




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


    fun resetearModoBusquedaIA() {
        modoBusquedaIA = false
        listaIdsConScore = emptyList()
        paginaActual_ = 0
    }
    fun busqueda_manual_filtrado(data: datos_para_filtrado_manual) {
        viewModelScope.launch {
            try {
                loading = true
                _respuesta_gemini.value = estado_Carga_respuesta_gemini.loading

                resultado_encontrado_algolia = withContext(Dispatchers.IO) {
                    repo.send_params_filter_manual(data)
                }

                val resultadosOrdenados = resultado_encontrado_algolia?.resultados
                    ?.sortedByDescending { it.score }
                    ?.map { IdScore(it.id, it.score) }

                if (resultadosOrdenados.isNullOrEmpty()) {
                    _respuesta_gemini.value =
                        estado_Carga_respuesta_gemini.empty("No encontré resultados")
                    modoBusquedaIA = false
                    limpiarTerminosNlp()
                    return@launch
                }

                // ✅ sin launch anidado — todo en la misma corrutina
                val primerasPromos = obtenerPrimeraPaginaDesdeIds(resultadosOrdenados)

                _promosAcumuladas.value = primerasPromos
                listaCompleta.value = primerasPromos
                listaFiltrada.value = primerasPromos

                // ✅ asignar listaIdsConScore ANTES de activar modoBusquedaIA
                listaIdsConScore = resultadosOrdenados.sortedByDescending { it.score }
                paginaActual_ = 1
                _hayMasPaginas.value = listaIdsConScore.size > PAGE_SIZE_IDS
                modoBusquedaIA = true  // ← al último, cuando todo está listo

                _respuesta_gemini.value = estado_Carga_respuesta_gemini.succes(
                    cantidad = resultadosOrdenados.size,
                    items = primerasPromos
                )

            } catch (e: Exception) {
                Log.d("e", "error_eviar $e")
                modoBusquedaIA = false
                _respuesta_gemini.value = estado_Carga_respuesta_gemini.error("se produjo un error")
            } finally {
                loading = false
            }
        }
    }

    fun procesar_nlp_open_ia(texto: String) {
        viewModelScope.launch {
            try {
                loading = true
                _respuesta_gemini.value = estado_Carga_respuesta_gemini.loading
                Log.d("NLP", "▶ Iniciando búsqueda con texto: \"$texto\"")

                val resultadoLocal = repo.obtener_respuesta_open_ia(texto)
                Log.d("NLP", "📦 resultadoLocal: $resultadoLocal")

                if (resultadoLocal != null) {
                    resultado = resultadoLocal
                    _terminos_nlp.value = resultadoLocal.productos
                    _terminos_nlp_seleccionados.value = resultadoLocal.productos
                    Log.d("NLP", "🏷 términos NLP: ${resultadoLocal.productos}")

                    resultado_encontrado_algolia = withContext(Dispatchers.IO) {
                        repo.send_get_resul_algoalia(resultadoLocal)
                    }
                    Log.d("NLP", "🔍 resultados Algolia: ${resultado_encontrado_algolia?.resultados?.size} items")
                    resultado_encontrado_algolia?.resultados?.forEach {
                        Log.d("NLP", "   id=${it.id} score=${it.score}")
                    }

                    val resultadosOrdenados = resultado_encontrado_algolia?.resultados
                        ?.sortedByDescending { it.score }
                        ?.map { IdScore(it.id, it.score) }
                    Log.d("NLP", "📊 ordenados: ${resultadosOrdenados?.size} items")

                    if (resultadosOrdenados.isNullOrEmpty()) {
                        Log.d("NLP", "❌ Sin resultados → empty")
                        _respuesta_gemini.value =
                            estado_Carga_respuesta_gemini.empty("No encontré resultados")
                        modoBusquedaIA = false
                        limpiarTerminosNlp()
                        return@launch
                    }

                    val primerasPromos = obtenerPrimeraPaginaDesdeIds(resultadosOrdenados)
                    Log.d("NLP", "✅ primerasPromos: ${primerasPromos.size} promos cargadas")

                    _promosAcumuladas.value = primerasPromos
                    listaCompleta.value = primerasPromos
                    listaFiltrada.value = primerasPromos

                    listaIdsConScore = resultadosOrdenados.sortedByDescending { it.score }
                    paginaActual_ = 1
                    _hayMasPaginas.value = listaIdsConScore.size > PAGE_SIZE_IDS
                    modoBusquedaIA = true
                    Log.d("NLP", "📄 hayMasPaginas: ${listaIdsConScore.size > PAGE_SIZE_IDS}")

                    _respuesta_gemini.value = estado_Carga_respuesta_gemini.succes(
                        cantidad = resultadosOrdenados.size,
                        items = primerasPromos
                    )
                    Log.d("NLP", "🎉 succes emitido — cantidad=${resultadosOrdenados.size}")

                } else {
                    Log.d("NLP", "⚠️ resultadoLocal es null")
                }

            } catch (e: Exception) {
                Log.e("NLP", "💥 Exception: ${e.message}", e)
                modoBusquedaIA = false
                limpiarTerminosNlp()
                _respuesta_gemini.value = estado_Carga_respuesta_gemini.error("se produjo un error")
            } finally {
                loading = false
                Log.d("NLP", "⏹ finally — loading = false")
            }
        }
    }
    fun obtener_promos_por_ids_directos(ids: List<String>, localidad: String) {
        ultimoDocumento = null
        _hayMasPaginas.value = false
        _promosAcumuladas.value = emptyList()
        listaCompleta.value = emptyList()
        modoBusquedaIA = false
        listaIdsConScore = emptyList()
        paginaActual_ = 0
        _esPrimeraCarga.value = true
        _estadoPromos.value = estado_carga_promociones.loading

        viewModelScope.launch {
            try {
                val promos = repo.obtenerPromosPorIdsProcesadas_123(
                    ids = ids,
                    limite = ids.size,
                    localidad = localidad
                )

                if (promos.isEmpty()) {
                    _estadoPromos.value = estado_carga_promociones.empty("No hay promociones disponibles")
                    _esPrimeraCarga.value = false
                    return@launch
                }


                _promosAcumuladas.value = promos
                listaCompleta.value = promos
                listaFiltrada.value = promos
                _estadoPromos.value = estado_carga_promociones.succes(promos)
                _esPrimeraCarga.value = false

            } catch (e: Exception) {
                _estadoPromos.value = estado_carga_promociones.error("Error al cargar promociones")
                _esPrimeraCarga.value = false
            }
        }
    }

    fun limpiar_estado_promos_completo() {
        ultimoDocumento = null
        _hayMasPaginas.value = false
        _promosAcumuladas.value = emptyList()
        listaCompleta.value = emptyList()
        listaFiltrada.value = emptyList()
        modoBusquedaIA = false
        listaIdsConScore = emptyList()
        paginaActual_ = 0
        _esPrimeraCarga.value = true
        _estadoPromos.value = estado_carga_promociones.loading

        // 🔥 lo que faltaba: limpiar también tiendas y estado relacionado
        _tiendas_con_mas_de_una_promo.value = emptyList()
        _estado_Carga_tienda_select.value = estado_carga_tienda_Seleccionada.idle
        _totalPromosTienda.value = 0
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
        data class succes (val cantidad:Int,val items: List<obj_completo>): estado_Carga_respuesta_gemini()
        data class error(val texto_error: String) : estado_Carga_respuesta_gemini()
        data class empty(val text_vacio: String) : estado_Carga_respuesta_gemini()
        object idle : estado_Carga_respuesta_gemini()
    }


    sealed class estado_carga_tienda_Seleccionada {
        object loading : estado_carga_tienda_Seleccionada()
        data class succes(
            val items: List<obj_completo>,
            val total: Int = 0,
            val pagos: List<String> = emptyList(),        // 🔥
            val comodidades: List<String> = emptyList()   // 🔥
        ) : estado_carga_tienda_Seleccionada()
        data class error(val texto_error: String) : estado_carga_tienda_Seleccionada()
        data class empty(val text_vacio: String) : estado_carga_tienda_Seleccionada()
        object idle : estado_carga_tienda_Seleccionada()
    }

    sealed class estado_carga_promociones {
        object loading : estado_carga_promociones()
        data class empty(val txt: String) : estado_carga_promociones()
        data class succes(
            val items: List<obj_completo>,
            val requestId: Long = System.nanoTime()   // 👈 AGREGAR: fuerza que cada emisión sea única
        ) : estado_carga_promociones()
        data class error(val txt: String) : estado_carga_promociones()
    }

    sealed class EstadoMicrofono {
        object Idle : EstadoMicrofono()
        object Grabando : EstadoMicrofono()
        object Procesando : EstadoMicrofono()
        data class Error(val msg: String) : EstadoMicrofono()
    }
}