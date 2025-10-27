package com.geinzz.geinzwork.viewModels

import android.util.Log
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.geinzz.geinzwork.data.model.dataclass_seguridad.dataclass_seguridad
import com.geinzz.geinzwork.data.model.localizate_geinz.HorarioTienda
import com.geinzz.geinzwork.data.model.localizate_geinz.dataclass_cat_sub
import com.geinzz.geinzwork.data.model.localizate_geinz.dataclass_cat_sub_lista_cat
import com.geinzz.geinzwork.data.model.localizate_geinz.filtrado_tiendas.filtrado_tiendas_cat_sub
import com.geinzz.geinzwork.data.model.localizate_geinz.filtrado_tiendas.obtener_tiendas_lat_log_id
import com.geinzz.geinzwork.data.model.localizate_geinz.filtrado_tiendas.tiendas_por_categoria
import com.geinzz.geinzwork.data.model.localizate_geinz.inicio_geinz.datos_tienda_free
import com.geinzz.geinzwork.data.model.localizate_geinz.inicio_geinz.lugares_turisticos
import com.geinzz.geinzwork.data.model.localizate_geinz.inicio_geinz.tiendas_mapa

import com.geinzz.geinzwork.data.model.localizate_geinz.modelo_tienda
import com.geinzz.geinzwork.model.repo_agregar_cat_sub_localizate
import com.geinzz.geinzwork.model.repo_filtrado_tiendas
import com.geinzz.geinzwork.model.repo_lugares_turisticos
import com.geinzz.geinzwork.model.repo_seguridad_salud
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.constantes_lista_localidades
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class viewModel_filtado_tiendas (   private val savedStateHandle: SavedStateHandle): ViewModel() {

    val repo_filtrado = repo_filtrado_tiendas()
    val repo_cat_sub = repo_agregar_cat_sub_localizate()

    private val instancia_repo_lugar_turistico= repo_lugares_turisticos()

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


    private val datos_tiendas_sin_pago = MutableLiveData<carga_tiendas_sin_pago>(carga_tiendas_sin_pago.loading_tiendas_free)
    val _datos_tienda_sin_pago: LiveData<carga_tiendas_sin_pago> get() = datos_tiendas_sin_pago


    private val _listaTiendasGuardadas =
        MutableLiveData<List<tiendas_por_categoria>>(
            savedStateHandle["lista_tiendas_guardadas"] ?: emptyList()
        )

    val listaTiendasGuardadas: LiveData<List<tiendas_por_categoria>>
        get() = _listaTiendasGuardadas


    private val _tick = MutableStateFlow(System.currentTimeMillis())
    val tick: StateFlow<Long> = _tick


    init {
        viewModelScope.launch {
            state_Tiendas_filtradas_por_categoria.collect { estado ->
                if (estado is carga_tiendas.succes) {
                    val tiendasPagadas = estado.items.filter { it.pagado }
                    _listaTiendasGuardadas.postValue(tiendasPagadas)
                    savedStateHandle["lista_tiendas_guardadas"] = tiendasPagadas

                }
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


    private val subcategoria_lis=MutableStateFlow<List<String>>(emptyList())
    val _subcategoria_lis: StateFlow<List<String>> = subcategoria_lis

    var toda_las_tiendas = mutableListOf<tiendas_por_categoria>()
        private set

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


    fun get_subcategorias_sola(cat: String){
        viewModelScope.launch {
            try {
                subcategoria_lis.value=repo_filtrado.obtenerSubcategorias(cat)
                Log.d("categoriacategoria",   subcategoria_lis.value.toString())
            }catch (e: Exception){
                subcategoria_lis.value=emptyList()
            }
        }
    }



    fun tiendas_iniciales(lista: List<tiendas_por_categoria>) {
        toda_las_tiendas.clear()
        toda_las_tiendas.addAll(lista)
        Log.d("Tiendas_inicalaes","${lista.size}")
    }

    fun obtener_subcategorias(categoria_selecionada: String) {
        viewModelScope.launch {
            try {
                val data = repo_filtrado.obtener_subcategorias_tiendas(categoria_selecionada)
                subcategorias.value = data
                Log.d(
                    "obtenemos_datos",
                    " $categoria_selecionada ${subcategorias.value.toString()}"
                )

            } catch (e: Exception) {
                subcategorias.value = emptyList()
            }
        }
    }


    fun obtener_tiendas_filtradas(localida: String, categoria: String) {
        Log.d("tiendas_eobtenidas", "${localida} $categoria")
        viewModelScope.launch {
            state_Tiendas_filtradas_por_categoria.value = carga_tiendas.loading
            try {
                val data = repo_filtrado.obtenerTiendasFiltradas(localida, categoria)
                if (data.isNotEmpty()) {
                    datos_tiendas.value = data
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

    fun filtrar_por_subcategoria(subcategoria: String,lista: List<tiendas_por_categoria>) {
        Log.d("filtrado_sub", "Filtrando por: $subcategoria")

        viewModelScope.launch {
            state_Tiendas_filtradas_por_categoria.value = carga_tiendas.loading
            val listaBase =lista

            val resultado=listaBase.filter { tienda ->
                tienda.lista_subcategoiras.any { it.equals(subcategoria, ignoreCase = true) }
            }

            state_Tiendas_filtradas_por_categoria.value = if (resultado.isNotEmpty()) {
                carga_tiendas.succes(resultado)
            } else {
                carga_tiendas.empty("No se encontraron resultados para $subcategoria")
            }
        }
    }

    fun lista_completa_inicial(subcategoria: String){
        viewModelScope.launch {
            if(subcategoria=="Todos" && toda_las_tiendas.isNotEmpty()){
                Log.d("toda_las_tiendas","${toda_las_tiendas.size}")
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


    fun obtenerHorarioPorTienda_activa(localidad: String, idTienda: String) {
        Log.d("id_registrado", idTienda)
        viewModelScope.launch {
            try {
                val data = repo_filtrado.obtenerHorarioPorTienda(idTienda, localidad)
                data?.let { horarioTienda ->
//                    Log.d("datos_obtnidos", data.toString())
//                    val estaAbierto =
//                        constantes_lista_localidades.verificarSiEstaAbiertoHoy(horarioTienda)
//                    val nuevoMapa = _estadoTiendas.value.orEmpty().toMutableMap()
//                    nuevoMapa[idTienda] = estaAbierto
//                    _estadoTiendas.postValue(nuevoMapa)
                }
                Log.d("obtenos_dataios_teindas", _estadoTiendas.value.toString())
            } catch (e: Exception) {
                Log.d("obtenos_dataios_teindas", "no se econtroa datos")
            }
        }
    }

    fun obtenerHorarioPorTienda(localidad: String, idTienda: String) {
        viewModelScope.launch {
            try {
                val data = repo_filtrado.obtenerHorarioPorTienda2(idTienda, localidad)
                _horarioTienda.value = data
            } catch (e: Exception) {
                _horarioTienda.value = null
            }
        }
    }

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

    fun obtener_datos_lugares_turisticos(id: String,localida: String){
        viewModelScope.launch {
            try {
                _instance_lugar_turistico.value=instancia_repo_lugar_turistico.get_lugar_turistico(localida,id)
            }catch (e: Exception){
                _instance_lugar_turistico.value=lugares_turisticos()
            }
        }

    }

    sealed class carga_tiendas_sin_pago {
        object loading_tiendas_free : carga_tiendas_sin_pago()
        object empty_tiendas_free : carga_tiendas_sin_pago()
        data class succes_tiendas_free(val item: datos_tienda_free): carga_tiendas_sin_pago()
        data class error_tiendas_free(val texto: String = "Error al cargar los datos"): carga_tiendas_sin_pago()
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