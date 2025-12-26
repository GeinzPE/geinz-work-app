package com.geinzz.geinzwork.viewModels

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.geinzz.geinzwork.data.model.dataclass_novedades.nuevas_teindas_dias
import com.geinzz.geinzwork.data.model.dataclass_seguridad.dataclass_seguridad
import com.geinzz.geinzwork.data.model.localizate_geinz.HorarioAtencion
import com.geinzz.geinzwork.data.model.localizate_geinz.HorarioAtencion_box
import com.geinzz.geinzwork.data.model.localizate_geinz.HorarioDia
import com.geinzz.geinzwork.data.model.localizate_geinz.HorarioTienda
import com.geinzz.geinzwork.data.model.localizate_geinz.dataclass_cat_sub
import com.geinzz.geinzwork.data.model.localizate_geinz.dataclass_cat_sub_lista_cat
import com.geinzz.geinzwork.data.model.localizate_geinz.filtrado_tiendas.HorarioDia_box
import com.geinzz.geinzwork.data.model.localizate_geinz.filtrado_tiendas.filtrado_tiendas_cat_sub
import com.geinzz.geinzwork.data.model.localizate_geinz.filtrado_tiendas.horario_tienda
import com.geinzz.geinzwork.data.model.localizate_geinz.filtrado_tiendas.nuevos_lugares_agregados
import com.geinzz.geinzwork.data.model.localizate_geinz.filtrado_tiendas.obtener_tiendas_lat_log_id
import com.geinzz.geinzwork.data.model.localizate_geinz.filtrado_tiendas.tiendas_por_categoria
import com.geinzz.geinzwork.data.model.localizate_geinz.inicio_geinz.datos_tienda_free
import com.geinzz.geinzwork.data.model.localizate_geinz.inicio_geinz.favoritos_guardados
import com.geinzz.geinzwork.data.model.localizate_geinz.inicio_geinz.lugares_turisticos
import com.geinzz.geinzwork.data.model.localizate_geinz.inicio_geinz.tiendas_mapa

import com.geinzz.geinzwork.data.model.localizate_geinz.modelo_tienda
import com.geinzz.geinzwork.model.repo_agregar_cat_sub_localizate
import com.geinzz.geinzwork.model.repo_eres_socio
import com.geinzz.geinzwork.model.repo_filtrado_tiendas
import com.geinzz.geinzwork.model.repo_lugares_turisticos
import com.geinzz.geinzwork.model.repo_seguridad_salud
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.constantes_horas.obtenerProximoDiaAbierto
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.constantes_lista_localidades
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.constantes_lista_localidades.verificarSiEstaAbiertoHoy
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Calendar

class viewModel_filtado_tiendas(private val savedStateHandle: SavedStateHandle) : ViewModel() {

    val repo_filtrado = repo_filtrado_tiendas()
    val repo_cat_sub = repo_agregar_cat_sub_localizate()

    @RequiresApi(Build.VERSION_CODES.O)
    val repo_erese_socio = repo_eres_socio()


    private val _categoria_filtrado = MutableStateFlow("Todos")
    val subcategoriaFiltrado: StateFlow<String> get() = _categoria_filtrado

    private val _txt_nombre_filtrado = MutableStateFlow("")
    val txtNombreFiltrado: StateFlow<String> get() = _txt_nombre_filtrado


    private val instancia_repo_lugar_turistico = repo_lugares_turisticos()

    private val _instance_lugar_turistico = MutableStateFlow(lugares_turisticos())
    val instance_lugar_turistico: StateFlow<lugares_turisticos> = _instance_lugar_turistico


    private val subcategorias = MutableLiveData<List<filtrado_tiendas_cat_sub>>()
    val _subcategoiraList: LiveData<List<filtrado_tiendas_cat_sub>> get() = subcategorias

    private val instacia_repo_salud = repo_seguridad_salud()

    private val _instance_salud_seguridad =
        MutableStateFlow(Triple(emptyList<String>(), emptyList<String>(), 0L))

    val instance_salud_seguridad: StateFlow<Triple<List<String>, List<String>, Long>> =
        _instance_salud_seguridad

    private val state_Tiendas_filtradas_por_categoria =
        MutableStateFlow<carga_tiendas>(carga_tiendas.loading)
    val _Tiendas_filtradas_por_categoria: StateFlow<carga_tiendas> =
        state_Tiendas_filtradas_por_categoria
    private val datos_tienda = MutableLiveData<List<modelo_tienda>>()
    val _datos_tienda: LiveData<List<modelo_tienda>> get() = datos_tienda

    private val _datos_nuevos_lugares =
        MutableStateFlow<List<nuevos_lugares_agregados>>(emptyList())
    val datos_nuevos_lugares: StateFlow<List<nuevos_lugares_agregados>> = _datos_nuevos_lugares


    private val datos_tiendas_sin_pago =
        MutableLiveData<carga_tiendas_sin_pago>(carga_tiendas_sin_pago.loading_tiendas_free)
    val _datos_tienda_sin_pago: LiveData<carga_tiendas_sin_pago> get() = datos_tiendas_sin_pago


    fun resetear_estado_sin_pago() {
        datos_tiendas_sin_pago.value = carga_tiendas_sin_pago.empty_tiendas_free
    }

    private val _listaTiendasGuardadas =
        MutableLiveData<List<tiendas_por_categoria>>(
            savedStateHandle["lista_tiendas_guardadas"] ?: emptyList()
        )

    val listaTiendasGuardadas: LiveData<List<tiendas_por_categoria>>
        get() = _listaTiendasGuardadas


    private val _tick = MutableStateFlow(System.currentTimeMillis())
    val tick: StateFlow<Long> = _tick


    private val _color_estado_tienda = MutableStateFlow(horario_tienda())
    val color_estado_tienda: StateFlow<horario_tienda> = _color_estado_tienda


    private val _color_estado_tienda_Box = MutableStateFlow(HorarioDia_box())
    val color_estado_tienda_box: StateFlow<HorarioDia_box> = _color_estado_tienda_Box


    private val _color_estado_tienda_flow = MutableStateFlow(Color.Gray)
    val color_estado_tienda_flow: StateFlow<Color> = _color_estado_tienda_flow


    private val _existe_favorito = MutableStateFlow(false)
    val existe_favorito: StateFlow<Boolean> = _existe_favorito


    fun setear_color(color: Color) {
        _color_estado_tienda_flow.value = color
    }

    private val _horariosTiendas_real = MutableStateFlow<Map<String, HorarioDia_box>>(emptyMap())
    val horariosTiendas_real = _horariosTiendas_real.asStateFlow()


    private val _horariosTiendas_real_compelto =
        MutableSharedFlow<HorarioAtencion_box>(replay = 1)
    val horariosTiendas_real_completo =
        _horariosTiendas_real_compelto.asSharedFlow()

    init {

        // Guarda las tiendas pagadas
        viewModelScope.launch {
            state_Tiendas_filtradas_por_categoria.collect { estado ->
                if (estado is carga_tiendas.succes) {
                    val tiendasPagadas = estado.items.filter { it.pagado }
                    _listaTiendasGuardadas.postValue(tiendasPagadas)
                    savedStateHandle["lista_tiendas_guardadas"] = tiendasPagadas
                }
            }
        }

        // Escucha los cambios de horarios en tiempo real
        viewModelScope.launch {
            repo_filtrado.cambiosHorarioTiendas.collect { update ->
                // Actualiza el horario de esa tienda
                val nuevoMapa = _horariosTiendas_real.value.toMutableMap()
                nuevoMapa[update.idTienda] = update.horario
                _horariosTiendas_real.value = nuevoMapa

                Log.d("VM-HORARIO", "Horario actualizado para ${update.idTienda}")
            }
        }


        viewModelScope.launch {
            repo_filtrado.cambiosHorariocompleto_tienda.collect { update ->
                Log.d("VM-HORARIO-COMPLETO", "🔥 EMITIENDO $update")
                _horariosTiendas_real_compelto.emit(update)
            }
        }

    }


    init {
        viewModelScope.launch {
            while (true) {
                delay(60_000) // cada minuto
                _tick.value = System.currentTimeMillis()
            }
        }
    }

    fun iniciarEscucha(localidad: String, categoria: String) {
        repo_filtrado.escucharHorariosEnTiempoReal(localidad, categoria)
    }

    private val _horarioTienda = MutableLiveData<HorarioTienda?>(null)

    private val subcategoria_filtrado = MutableLiveData<List<dataclass_cat_sub_lista_cat>>()
    val _subcategoria_filtrado: LiveData<List<dataclass_cat_sub_lista_cat>> get() = subcategoria_filtrado

    private val obtener_subcategoria = MutableLiveData<List<filtrado_tiendas_cat_sub>>()
    val _obtener_subacategoria: LiveData<List<filtrado_tiendas_cat_sub>> get() = obtener_subcategoria


    private val _estadoTiendas = MutableLiveData<Map<String, Boolean>>(emptyMap())
    val estadoTiendas: LiveData<Map<String, Boolean>> get() = _estadoTiendas
    private val _subcategorias_memory =
        MutableStateFlow<List<dataclass_cat_sub_lista_cat>>(emptyList())
    val subcategorias_memory: StateFlow<List<dataclass_cat_sub_lista_cat>> = _subcategorias_memory

    private val _lista_sub_lugares = MutableLiveData<List<String>>()
    val lista_sub_lugares: LiveData<List<String>> get() = _lista_sub_lugares


    private val datos_tiendas = MutableLiveData<List<tiendas_por_categoria>>()
    val _datos__tiendas: LiveData<List<tiendas_por_categoria>> get() = datos_tiendas


    private val subcategoria_lis = MutableStateFlow<List<String>>(emptyList())
    val _subcategoria_lis: StateFlow<List<String>> = subcategoria_lis

    var toda_las_tiendas = mutableListOf<tiendas_por_categoria>()
        private set


    val _lista_base_seguridad = MutableStateFlow<List<tiendas_por_categoria>>(emptyList())
    fun actualizarsubcategoria_filtrado(nuevaCategoria: String) {
        _categoria_filtrado.value = nuevaCategoria
    }

    fun actualizarNombre(nuevoNombre: String) {
        _txt_nombre_filtrado.value = nuevoNombre
    }

    fun limpiarFiltros() {
        _categoria_filtrado.value = "Todos"
        _txt_nombre_filtrado.value = ""
    }

    fun aplicarFiltrosAlRegresar() {
        Log.d("FiltroRegreso", "➡️ Detectamos regreso desde el mapa")

        val categoria = _categoria_filtrado.value
        val texto = _txt_nombre_filtrado.value
        val listaBase = _lista_base_seguridad.value

        Log.w("FiltroRegreso", "Tamaño de lista base: ${listaBase.size}")

        if (listaBase.isEmpty()) {
            Log.w("FiltroRegreso", "⚠️ No hay tiendas cargadas aún, no se aplican filtros.")
            return
        }

        when {
            texto.isNotEmpty() && categoria != "Todos" -> {
                Log.d("FiltroRegreso", "Filtrando por nombre '$texto' y categoría '$categoria'")
                obtener_filtrado_nombre(texto, categoria, listaBase)
            }

            texto.isNotEmpty() -> {
                Log.d("FiltroRegreso", "Filtrando solo por nombre: '$texto'")
                obtener_filtrado_nombre(texto, categoria, listaBase)
            }

            categoria != "Todos" -> {
                Log.d("FiltroRegreso", "Filtrando solo por categoría: '$categoria'")
                filtrar_por_subcategoria(categoria, listaBase)
            }

            else -> {
                Log.d("FiltroRegreso", "Sin filtros activos, mostrando lista completa.")
                state_Tiendas_filtradas_por_categoria.value = carga_tiendas.succes(listaBase)
            }
        }
    }


    fun obtener_categorias() {
        viewModelScope.launch {
            try {
                val lista = repo_cat_sub.obtener_subcategoiras()
                subcategoria_filtrado.value = lista
                _subcategorias_memory.value = lista
            } catch (e: Exception) {
                subcategoria_filtrado.value = emptyList()
                _subcategorias_memory.value = emptyList()
            }
        }
    }


    fun obtener_cat_lugares() {
        viewModelScope.launch {
            try {
                _lista_sub_lugares.value = repo_cat_sub.obtener_categorias_lugares()
            } catch (e: Exception) {
                _lista_sub_lugares.value = emptyList()
            }
        }
    }

    fun obtener_lista_sub(cat: String): List<String> {
        Log.d("filtadoddd", cat)
        val listaCat = subcategorias_memory.value
        val categoriaEncontrada =
            listaCat.firstOrNull { it.nombre_cat.equals(cat, ignoreCase = true) }
        Log.d("filtadoddd", categoriaEncontrada.toString())
        return categoriaEncontrada?.lista_subcategorias ?: emptyList()
    }

    fun obtener_subcategoiras(categoria: String) {
        viewModelScope.launch {
            try {
                obtener_subcategoria.value = repo_filtrado.obtener_subcategorias_tiendas(categoria)
            } catch (e: Exception) {
                obtener_subcategoria.value = emptyList()
            }
        }
    }


    fun get_subcategorias_sola(cat: String) {
        viewModelScope.launch {
            try {
                subcategoria_lis.value = repo_filtrado.obtenerSubcategorias(cat)
                Log.d("categoriacategoria", subcategoria_lis.value.toString())
            } catch (e: Exception) {
                subcategoria_lis.value = emptyList()
            }
        }
    }


    fun tiendas_iniciales(lista: List<tiendas_por_categoria>) {

        toda_las_tiendas.clear()
        toda_las_tiendas.addAll(lista)
    }

    fun obtener_tiendas_filtradas(localida: String, categoria: String) {
        Log.d("tiendas_eobtenidas", "${localida} $categoria")
        viewModelScope.launch {
            state_Tiendas_filtradas_por_categoria.value = carga_tiendas.loading
            try {
                val data = repo_filtrado.obtenerTiendasFiltradas(localida, categoria)
                if (data.isNotEmpty()) {
                    datos_tiendas.value = data
                    _lista_base_seguridad.value = data
                    state_Tiendas_filtradas_por_categoria.value = carga_tiendas.succes(data)
                } else {
                    datos_tiendas.value = emptyList()
                    state_Tiendas_filtradas_por_categoria.value =
                        carga_tiendas.empty("No se encontraron resultados")
                }

            } catch (e: Exception) {
                state_Tiendas_filtradas_por_categoria.value =
                    carga_tiendas.error("error al cargar las tiendas")
            }
        }
    }


    fun filtrar_por_subcategoria(subcategoria: String, lista: List<tiendas_por_categoria>) {
        Log.d("131231312313123", "$subcategoria")

        viewModelScope.launch {
            state_Tiendas_filtradas_por_categoria.value = carga_tiendas.loading
            val listaBase = lista

            val resultado = listaBase.filter { tienda ->
                tienda.lista_subcategoiras.any { it.equals(subcategoria, ignoreCase = true) }
            }

            state_Tiendas_filtradas_por_categoria.value = if (resultado.isNotEmpty()) {
                carga_tiendas.succes(resultado)
            } else {
                carga_tiendas.empty("No se encontraron resultados para $subcategoria")
            }
        }
    }


    fun lista_completa_inicial(subcategoria: String) {
        viewModelScope.launch {
            if (subcategoria == "Todos" && toda_las_tiendas.isNotEmpty()) {
                Log.d("toda_las_tiendas", "${toda_las_tiendas.size}")
                state_Tiendas_filtradas_por_categoria.value = carga_tiendas.succes(toda_las_tiendas)
                return@launch
            }
        }
    }

    fun obtener_filtrado_nombre(
        texto: String,
        categoria: String,
        lista: List<tiendas_por_categoria>
    ) {
        Log.d("131231312313123", "$texto $categoria ${lista.size}")
        viewModelScope.launch {
            try {
                state_Tiendas_filtradas_por_categoria.value = carga_tiendas.loading

                val res = lista.filter { tienda ->
                    val texto_coincide = tienda.nombre_tienda.contains(texto, ignoreCase = true)
                    val categoria_coincide =
                        categoria == "Todos" || tienda.lista_subcategoiras.any {
                            it.equals(categoria, ignoreCase = true)
                        }
                    texto_coincide && categoria_coincide
                }

                if (res.isNotEmpty()) {
                    Log.d("FILTRO_TIENDAS", "🟩 ${res.size} tiendas encontradas")
                    state_Tiendas_filtradas_por_categoria.value = carga_tiendas.succes(res)
                } else {
                    Log.d("FILTRO_TIENDAS", "🟥 Sin coincidencias para '$texto'")
                    state_Tiendas_filtradas_por_categoria.value =
                        carga_tiendas.empty("No se encontraron resultados")
                }

            } catch (e: Exception) {
                Log.e("FILTRO_TIENDAS", "❌ Error: ${e.message}", e)
                state_Tiendas_filtradas_por_categoria.value =
                    carga_tiendas.empty("Hubo un error al cargar los datos")
            }
        }
    }


    fun obtener_campos_tiendas_por_id(localida: String, id_tienda: String) {
        Log.d("tienda:", "$localida $id_tienda")
        viewModelScope.launch {
            try {
                val data = repo_filtrado.obtenner_campos_tiendas_espesifica(localida, id_tienda)
                datos_tienda.value = data
                Log.d("obtemos_tienda_Selecionda", "${datos_tienda.value}  $localida,$id_tienda")
            } catch (e: Exception) {
                datos_tienda.value = emptyList()
            }
        }
    }

    fun obtener_tienda_no_pagada(localida: String, id_tienda: String) {
        viewModelScope.launch {
            datos_tiendas_sin_pago.value = carga_tiendas_sin_pago.loading_tiendas_free
            try {
                val datos = repo_filtrado.obtener_campos_tienda_free(localida, id_tienda)

                if (datos.nombre_.isBlank()) {
                    datos_tiendas_sin_pago.value = carga_tiendas_sin_pago.empty_tiendas_free
                } else {
                    datos_tiendas_sin_pago.value = carga_tiendas_sin_pago.succes_tiendas_free(datos)
                }

            } catch (e: Exception) {
                datos_tiendas_sin_pago.value =
                    carga_tiendas_sin_pago.error_tiendas_free("No se cargaron los datos")
            }
        }
    }

//
//    fun obtenerHorarioPorTienda(localidad: String, idTienda: String) {
//        viewModelScope.launch {
//            try {
//                val data = repo_filtrado.obtenerHorarioPorTienda2(idTienda, localidad)
//                _horarioTienda.value = data
//            } catch (e: Exception) {
//                _horarioTienda.value = null
//            }
//        }
//    }

    fun obtener_numeros_seguridad_salud(localidad: String, idSelect: String) {
        viewModelScope.launch {
            try {
                val datosSaludSeguridad = instacia_repo_salud.get_numeros(localidad, idSelect)
                // Asegúrate de que el repo retorne Triple<List<String>, List<String>, Long>
                _instance_salud_seguridad.value = datosSaludSeguridad
            } catch (e: Exception) {
                Log.e("ViewModelError", "Error al obtener números: ${e.localizedMessage}", e)
                // Triple vacío si ocurre un error
                _instance_salud_seguridad.value = Triple(emptyList(), emptyList(), 0L)
            }
        }
    }

    fun obtener_datos_lugares_turisticos(id: String, localida: String) {

        viewModelScope.launch {
            try {
                _instance_lugar_turistico.value =
                    instancia_repo_lugar_turistico.get_lugar_turistico(localida, id)
                Log.d("id_tiendasdada123", "${_instance_lugar_turistico.value}")
            } catch (e: Exception) {
                _instance_lugar_turistico.value = lugares_turisticos()
            }
        }
    }


//    fun cast_horario_atencion_horario_tienda(horarioAtencion: HorarioAtencion) {
//        Log.d("cast_horario_atencion_horario_tienda", horarioAtencion.toString())
//        viewModelScope.launch {
//            try {
//                _color_estado_tienda.value =
//                    repo_filtrado.obtener_estado_horario_tienda(horarioAtencion)
//            } catch (e: Exception) {
//                _color_estado_tienda.value = horario_tienda()
//            }
//        }
//    }

    fun cast_horario_atencion_horario_tienda_box(horarioAtencion: HorarioAtencion_box) {
        Log.d("cast_horario_atencion_horario_tienda", horarioAtencion.toString())
        viewModelScope.launch {
            try {
                _color_estado_tienda_Box.value =
                    repo_filtrado.obtener_estado_horario_tienda_Box(horarioAtencion)
            } catch (e: Exception) {
                _color_estado_tienda_Box.value = HorarioDia_box()
            }
        }
    }

    val favoritos = MutableStateFlow<Map<String, Boolean>>(emptyMap())

    @RequiresApi(Build.VERSION_CODES.O)
    fun guardar_tienda_favorita(id_user: String, item_favoritos: favoritos_guardados) {
        viewModelScope.launch {
            try {
                repo_filtrado.guardar_tienda_favorito(id_user, item_favoritos)
                favoritos.update {
                    it.toMutableMap().apply { put(item_favoritos.id_tienda_lugar, true) }
                }
                repo_erese_socio.agregar_contador(
                    "guardados",
                    item_favoritos.id_tienda_lugar,
                    item_favoritos.localida_tienda
                )
            } catch (e: Exception) {
                Log.d("error", "error al guardar faboritos")
            }
        }
    }

    private val _horariosTiendas = MutableStateFlow<Map<String, HorarioDia_box>>(emptyMap())
    val horariosTiendas: StateFlow<Map<String, HorarioDia_box>> = _horariosTiendas

    fun calcularHorarioParaTienda(idTienda: String, horarioAtencion: HorarioAtencion_box) {
        Log.d("horario_atenicon_estado", horarioAtencion.toString())
        viewModelScope.launch {
            val result = try {
                repo_filtrado.obtener_estado_horario_tienda_Box(horarioAtencion)
            } catch (e: Exception) {
                HorarioDia_box()
            }
            _horariosTiendas.update { current ->
                current + (idTienda to result)
            }
        }
    }


    fun eliminar_tienda_favorita(id_user: String, id_tienda: String, localidad_tienda: String) {
        viewModelScope.launch {
            try {
                repo_filtrado.eliminar_tienda_favorito(id_user, id_tienda)
                repo_filtrado.eliminar_uer_tienda_fv(id_user, id_tienda, localidad_tienda)
                favoritos.update { it.toMutableMap().apply { put(id_tienda, false) } }
            } catch (e: Exception) {
                Log.d("error", "error al eliminar faboritos")
            }
        }
    }

    fun verificar_existe_favorito(id_user: String, id_tienda: String) {
        Log.d("varificar_fv", "$id_user $id_tienda")
        viewModelScope.launch {
            try {
                _existe_favorito.value = repo_filtrado.verificar_favorito(id_user, id_tienda)
                Log.d(
                    "exite",
                    "${repo_filtrado.verificar_favorito(id_user, id_tienda)} $id_user $id_tienda"
                )
            } catch (e: Exception) {
                _existe_favorito.value = false
                Log.d("error", "error al eliminar faboritos")
            }
        }
    }


    fun verificar_existe_favoritoMap(idUser: String, idTienda: String) {
        viewModelScope.launch {
            try {
                val existe = repo_filtrado.verificar_favorito(idUser, idTienda)
                favoritos.value += (idTienda to existe)
            } catch (e: Exception) {
                Log.d("error_entra", "error al encontrar el existente")
            }

        }
    }

    fun vincular_cuenta(id_user: String, id_tienda: String, localida: String) {
        viewModelScope.launch {
            try {
                repo_filtrado.vincular_cuenta(id_user, id_tienda, localida)
            } catch (e: Exception) {
                Log.d("error_entrar", "error al vincular $e")
            }
        }
    }


    fun eliminarvincualcion_cuenta_tienda(id_user: String, id_tienda: String, localida: String) {
        viewModelScope.launch {
            try {
                repo_filtrado.eliminar_vinculacion_cuenta(id_user, id_tienda, localida)
            } catch (e: Exception) {
                Log.d("error_entrar", "error al eliminar la vincualcion $e")
            }
        }
    }


    @RequiresApi(Build.VERSION_CODES.O)
    fun guardar_tienda_favorita_por_id(
        localidad_tienda: String,
        id_user: String,
        id_tienda: String
    ) {
        viewModelScope.launch {
            try {
                val datos = repo_filtrado.obtener_datos_tienda_id(localidad_tienda, id_tienda)
                repo_filtrado.guardar_tienda_favorito(id_user, datos)
                favoritos.update { it.toMutableMap().apply { put(id_tienda, true) } }
                repo_erese_socio.agregar_contador("guardados", id_tienda, localidad_tienda)

            } catch (e: Exception) {
                Log.d("error", "error al guardar faboritos")
            }
        }
    }
    private var ultimaLocalidad: String? = null


    fun obtener_lugaresnuevos(localidad: String) {

        if (localidad.isEmpty()) return

        // 🔒 si es la misma localidad, NO vuelve a consultar
        if (ultimaLocalidad == localidad && _datos_nuevos_lugares.value.isNotEmpty()) {
            return
        }

        ultimaLocalidad = localidad

        viewModelScope.launch {
            try {
                val nuevasTiendas =
                    repo_filtrado.obtener_lugaresnuevos_aleatorios(localidad)
                _datos_nuevos_lugares.value = nuevasTiendas
            } catch (e: Exception) {
                _datos_nuevos_lugares.value = emptyList()
            }
        }
    }

    sealed class carga_tiendas_sin_pago {
        object loading_tiendas_free : carga_tiendas_sin_pago()
        object empty_tiendas_free : carga_tiendas_sin_pago()
        data class succes_tiendas_free(val item: datos_tienda_free) : carga_tiendas_sin_pago()
        data class error_tiendas_free(val texto: String = "Error al cargar los datos") :
            carga_tiendas_sin_pago()
    }

    sealed class carga_subcategorias {
        object Loading : carga_subcategorias()
        object Empty : carga_subcategorias()
        data class loaded(val items: List<String>) : carga_subcategorias()
    }


    sealed class carga_tiendas {
        object loading : carga_tiendas()
        data class empty(val texto: String) : carga_tiendas()
        data class error(val texto: String) : carga_tiendas()
        data class succes(val items: List<tiendas_por_categoria>) : carga_tiendas()
    }

}