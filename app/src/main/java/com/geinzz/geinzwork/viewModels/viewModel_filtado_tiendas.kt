package com.geinzz.geinzwork.viewModels

import android.util.Log
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.constantes_lista_localidades
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class viewModel_filtado_tiendas : ViewModel() {

    val repo_filtrado = repo_filtrado_tiendas()
    val repo_cat_sub = repo_agregar_cat_sub_localizate()


    private val subcategorias = MutableLiveData<List<filtrado_tiendas_cat_sub>>()
    val _subcategoiraList: LiveData<List<filtrado_tiendas_cat_sub>> get() = subcategorias

    private val tiendas_filtradas_por_categoria = MutableLiveData<List<tiendas_por_categoria>>()
    val _tiendas_filtradas_por_categoria: LiveData<List<tiendas_por_categoria>> get() = tiendas_filtradas_por_categoria


    private val state_Tiendas_filtradas_por_categoria =
        MutableStateFlow<carga_tiendas>(carga_tiendas.loading)
    val _Tiendas_filtradas_por_categoria: StateFlow<carga_tiendas> =
        state_Tiendas_filtradas_por_categoria
    private val datos_tienda = MutableLiveData<List<modelo_tienda>>()
    val _datos_tienda: LiveData<List<modelo_tienda>> get() = datos_tienda


    private val datos_tiendas_sin_pago = MutableLiveData<datos_tienda_free>()
    val _datos_tienda_sin_pago: LiveData<datos_tienda_free> get() = datos_tiendas_sin_pago

    private val _listaFiltrada = MutableStateFlow<List<tiendas_por_categoria>>(emptyList())
    val listaFiltrada: StateFlow<List<tiendas_por_categoria>> = _listaFiltrada

    private val _horarioTienda = MutableLiveData<HorarioTienda?>(null)

    val horarioTienda: LiveData<HorarioTienda?> get() = _horarioTienda


    private val subcategoria_filtrado = MutableLiveData<List<dataclass_cat_sub_lista_cat>>()
    val _subcategoria_filtrado: LiveData<List<dataclass_cat_sub_lista_cat>> get() = subcategoria_filtrado


//    private val cat_sub_filtados=

//    private val tiendas_por_subcategoria = MutableLiveData<List<tiendas_por_categoria>>()
//    val _tiendas_por_subcategoria: LiveData<List<tiendas_por_categoria>> get() = tiendas_por_subcategori

    private val obtener_subcategoria = MutableLiveData<List<filtrado_tiendas_cat_sub>>()
    val _obtener_subacategoria: LiveData<List<filtrado_tiendas_cat_sub>> get() = obtener_subcategoria
    fun actualizarListaFiltrada(nuevaLista: List<tiendas_por_categoria>) {
        Log.d("nuevalsita", nuevaLista.toString())
        _listaFiltrada.value = nuevaLista
    }

    private val _estadoTiendas = MutableLiveData<Map<String, Boolean>>(emptyMap())
    val estadoTiendas: LiveData<Map<String, Boolean>> get() = _estadoTiendas

//    private val obtener_tiendas_filtradas = MutableLiveData<List<obtener_tiendas_lat_log_id>>()
//    val _obtener_datos_tienda: LiveData<List<obtener_tiendas_lat_log_id>> get() = obtener_tiendas_filtradas

    private val _subcategorias_memory =
        MutableStateFlow<List<dataclass_cat_sub_lista_cat>>(emptyList())
    val subcategorias_memory: StateFlow<List<dataclass_cat_sub_lista_cat>> = _subcategorias_memory

    private val _lista_sub_lugares = MutableLiveData<List<String>>()
    val lista_sub_lugares: LiveData<List<String>> get() = _lista_sub_lugares


    private val _todas_tiendas = MutableStateFlow<List<tiendas_por_categoria>>(emptyList())
    val todas_tiendas = _todas_tiendas.asStateFlow()


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

    fun tiendas_iniciales(lista: List<tiendas_por_categoria>) {
        if (toda_las_tiendas.isEmpty()) {
            toda_las_tiendas.clear()
            toda_las_tiendas.addAll(lista)
            _todas_tiendas.value = lista // la guardas también en StateFlow si la quieres observar
            Log.d("tiendas_iniciales", "Lista base guardada con ${lista.size} tiendas")
        }
    }


//    fun resetearTiendasFiltradas() {
//        tiendas_filtradas_por_categoria.value = emptyList()
//    }

//    fun filtrar_por_subcategoria(subcategoria: String): List<tiendas_por_categoria> {
//        return todas_tiendas.value.filter { it.lista_subcategoiras.contains(subcategoria) }
//    }

//    fun filtrar_por_nombre_en_lista(
//        nombre: String,
//        lista: List<tiendas_por_categoria>
//    ): List<tiendas_por_categoria> {
//        return lista.filter { it.nombre_tienda.contains(nombre, ignoreCase = true) }
//    }

//    fun filtrar_por_nombre(
//        nombre: String,
//        list: List<tiendas_por_categoria>
//    ) {
//        viewModelScope.launch {
//            Tiendas_filtradas_por_categoria.value = carga_tiendas.loading
//            delay(400)
//            try {
//                val texto_igual =
//                    list.filter { it.nombre_tienda.contains(nombre, ignoreCase = true) }
//
//                if (texto_igual.isNotEmpty()) {
//                    Tiendas_filtradas_por_categoria.value = carga_tiendas.succes(texto_igual)
//                } else {
//                    Tiendas_filtradas_por_categoria.value =
//                        carga_tiendas.empty("No se encontraron resultados")
//                }
//            } catch (e: Exception) {
//                Tiendas_filtradas_por_categoria.value =
//                    carga_tiendas.error("Error al cargar los datos")
//            }
//        }
//
//    }


    fun fraces_loadin(localida: String, nombre_user: String, categoria: String): List<String> {
        return listOf(
            "Qué bueno verte por aquí en $localida ...",
            "Buscando tiendas para ti, $nombre_user ...",
            "Buscando tiendas de $categoria ..."
        )
    }

    fun fraces_cargando_filtradas(subcategoria: String, nombre_user: String): List<String> {
        return listOf(
            "Cargandos todas los negocios de $subcategoria ...",
            "Espera un momento $nombre_user ..."
        )
    }

    fun fraces_cargando(nombre_user: String): List<String> {
        return listOf(
            "Estamos cargando todas las tiendas ...",
            "Espera un momento $nombre_user ...",
            "Gracias por la espera ..."
        )
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
                    tiendas_filtradas_por_categoria.value = data
                    state_Tiendas_filtradas_por_categoria.value = carga_tiendas.succes(data)
                } else {
                    state_Tiendas_filtradas_por_categoria.value =
                        carga_tiendas.empty("No se encontraron resultados")
                    tiendas_filtradas_por_categoria.value = emptyList()
                }
//                tiendas_filtradas_por_categoria.value = data
//                Log.d(
//                    "obtenosm_teindas_fitlkradas",
//                    tiendas_filtradas_por_categoria.value.toString()
//                )
            } catch (e: Exception) {
//                tiendas_filtradas_por_categoria.value = emptyList()
                state_Tiendas_filtradas_por_categoria.value =
                    carga_tiendas.error("error al cargar las tiendas")
            }
        }
    }

    fun filtrar_por_subcategoria(subcategoria: String) {
        Log.d("filtrado_sub", "Filtrando por: $subcategoria")

        viewModelScope.launch {
            state_Tiendas_filtradas_por_categoria.value = carga_tiendas.loading

            val listaBase = toda_las_tiendas

            val resultado = if (subcategoria.equals("Todos", ignoreCase = true)) {
                listaBase
            } else {
                listaBase.filter { tienda ->
                    tienda.lista_subcategoiras.any { it.equals(subcategoria, ignoreCase = true) }
                }
            }

            state_Tiendas_filtradas_por_categoria.value = if (resultado.isNotEmpty()) {
                carga_tiendas.succes(resultado)
            } else {
                carga_tiendas.empty("No se encontraron resultados para $subcategoria")
            }
        }
    }
    private var listaBasePorCategoria: List<tiendas_por_categoria> = emptyList()

    fun actualizarListaPorCategoria(nuevaLista: List<tiendas_por_categoria>) {
        listaBasePorCategoria = nuevaLista
    }

    fun obtener_filtrado_nombre(
        texto: String,
        categoria: String
    ) {
        viewModelScope.launch {
            try {
                Log.d("FILTRO_TIENDAS", "🟢 Iniciando filtrado -> texto='$texto', categoria='$categoria'")
                Log.d("FILTRO_TIENDAS", "Lista base actual (${listaBasePorCategoria.size} tiendas)")

                if (texto.isBlank()) {
                    Log.d("FILTRO_TIENDAS", "⚪ Texto vacío → devolviendo lista base completa de la categoría")
                    state_Tiendas_filtradas_por_categoria.value =
                        carga_tiendas.succes(listaBasePorCategoria)
                    return@launch
                }

                state_Tiendas_filtradas_por_categoria.value = carga_tiendas.loading
                delay(400)

                val res = listaBasePorCategoria.filter { tienda ->
                    val texto_coincide = tienda.nombre_tienda.contains(texto, ignoreCase = true)
                    val categoria_coincide = categoria == "Todos" || tienda.lista_subcategoiras.any {
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
            try {
                val datos = repo_filtrado.obtener_campos_tienda_free(localida, id_tienda)
                datos_tiendas_sin_pago.value = datos
            } catch (e: Exception) {
                datos_tiendas_sin_pago.value = datos_tienda_free()
            }
        }
    }

    fun obtenerHorarioPorTienda_activa(localidad: String, idTienda: String) {
        Log.d("id_registrado", idTienda)
        viewModelScope.launch {
            try {
                val data = repo_filtrado.obtenerHorarioPorTienda(idTienda, localidad)
                data?.let { horarioTienda ->
                    Log.d("datos_obtnidos", data.toString())
                    val estaAbierto =
                        constantes_lista_localidades.verificarSiEstaAbiertoHoy(horarioTienda)
                    val nuevoMapa = _estadoTiendas.value.orEmpty().toMutableMap()
                    nuevoMapa[idTienda] = estaAbierto
                    _estadoTiendas.postValue(nuevoMapa)
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


//    fun obtener_tiendas_registradas(localidad: String) {
//        viewModelScope.launch {
//            try {
//                val datos = repo_filtrado.obtener_tienas_filtradas(localidad)
//                state_Tiendas_filtradas_por_categoria.value = datos
//            } catch (e: Exception) {
//                state_Tiendas_filtradas_por_categoria.value = emptyList()
//            }
//        }
//
//    }


    sealed class carga_subcategorias {
        object Loading : carga_subcategorias()
        object Empty : carga_subcategorias()
        data class loaded(val items: List<String>) : carga_subcategorias()
    }
//    fun obtener_tiendas_por_subcategoria(subcategoria: String, localida: String) {
//        viewModelScope.launch {
//            try {
//                val data = repo_filtrado.obtener_tiendas_por_subcateogira(subcategoria, localida)
//                tiendas_por_subcategoria.value = data
//            } catch (e: Exception) {
//                tiendas_por_subcategoria.value = emptyList()
//            }
//        }
//    }

    sealed class carga_tiendas {
        object loading : carga_tiendas()
        data class empty(val texto: String) : carga_tiendas()
        data class error(val texto: String) : carga_tiendas()
        data class succes(val items: List<tiendas_por_categoria>) : carga_tiendas()
    }

}