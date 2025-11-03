package com.geinzz.geinzwork.viewModels

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.geinzz.geinzwork.data.model.dataclass_lugares_db
import com.geinzz.geinzwork.data.model.localizate_geinz.inicio_geinz.lugares_turisticos
import com.geinzz.geinzwork.model.repo_servicios_tramites
import com.geinzz.geinzwork.ui.adapters.adapterCategorias
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.constantes_lista_localidades.isInternetAvailable
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class viewmode_servicios_tramite : ViewModel() {
    private val isnta = repo_servicios_tramites()
    private val _lugares = MutableLiveData<List<dataclass_lugares_db>>()
    val lugares: LiveData<List<dataclass_lugares_db>> get() = _lugares
//    private val _listaFiltrada = MutableStateFlow<List<dataclass_lugares_db>>(emptyList())
//    val listaFiltrada: StateFlow<List<dataclass_lugares_db>> = _listaFiltrada

    var lugares_turisticos = mutableListOf<dataclass_lugares_db>()
        private set

    private var todo_lugares = emptyList<dataclass_lugares_db>()


    private val state_servicios = MutableStateFlow<carga_servicios>(carga_servicios.loading)
    val _state_servicios: StateFlow<carga_servicios> = state_servicios


    private val _mostrar_carga_turistico = MutableStateFlow(false)
    val mostrar_carga_turistico = _mostrar_carga_turistico.asStateFlow()

    init {
        viewModelScope.launch {
            state_servicios.value = carga_servicios.loading
        }
    }

    fun obtener_lugares(context: Context, localida: String) {
        viewModelScope.launch {
            try {
                _mostrar_carga_turistico.value=true
                delay(2000)
                state_servicios.value = carga_servicios.loading
                if (!isInternetAvailable(context)) {
                    _mostrar_carga_turistico.value=false
                    state_servicios.value = carga_servicios.error("Sin conexión a internet 😕")
                    return@launch
                }
                val res = isnta.obtenerServiciosTramites(localida)
                _lugares.value = res
                if (res.isNotEmpty()) {
                    _mostrar_carga_turistico.value=false
                    state_servicios.value = carga_servicios.succes(res)
                } else {
                    // Espera un poco antes de mostrar el "vacío"
                    _mostrar_carga_turistico.value=false
                    delay(300)
                    state_servicios.value =
                        carga_servicios.emoty("No hay datos registrados de $localida")
                }

            } catch (e: Exception) {
                _mostrar_carga_turistico.value=false
                _lugares.value = emptyList()
                state_servicios.value =
                    carga_servicios.error("Ocurrió un error al cargar los datos")
            }
        }
    }


    fun todos(lista: List<dataclass_lugares_db>) {
        todo_lugares = lista
        Log.d("todo_lugares_agregardos", lista.toString())
    }

    fun filtrar_por_categoria(context: Context, categorias: String) {
        Log.d("filtraoms", categorias)
        viewModelScope.launch {
//            if (categorias == "Todos" && todo_lugares.isNotEmpty()) {
//                state_servicios.value = carga_servicios.succes(todo_lugares)
//                return@launch
//            }

            state_servicios.value = carga_servicios.loading


            if (!isInternetAvailable(context)) {
                state_servicios.value = carga_servicios.error("Sin conexión a internet 😕")
                return@launch
            }

            try {
//                val resultado = if (categorias == "Todos") {
//                    todo_lugares
//                } else {
                val resultado = todo_lugares.filter {
                    it.categoria.toString().lowercase().contains(categorias.lowercase())
                }
//                }

                if (resultado.isNotEmpty()) {
                    state_servicios.value = carga_servicios.succes(resultado)
                } else {
                    if (categorias == "Todos") {
                        mostar_lista_completa(categorias)
                    } else {
                        state_servicios.value =
                            carga_servicios.emoty("No hay datos en la categoría $categorias")
                    }

                }

            } catch (e: Exception) {
                state_servicios.value =
                    carga_servicios.error("Error inesperado: ${e.localizedMessage}")
            }
        }
    }

    fun mostar_lista_completa(categorias: String) {
        viewModelScope.launch {
            if (categorias == "Todos" && todo_lugares.isNotEmpty()) {
                state_servicios.value = carga_servicios.succes(todo_lugares)
                return@launch
            }
        }

    }


    fun filtrar_nombre_categoria(
        nombre: String,
        categoria: String,
        lista: List<dataclass_lugares_db>
    ) {
        viewModelScope.launch {
            try {
                state_servicios.value = carga_servicios.loading

                val resultado = if (categoria == "Todos") {
                    // 🔹 Si la categoría es "Todos", busca por nombre o por categoría
                    lista.filter { item ->
                        val coincideNombre = item.lugar_nombre.contains(nombre, ignoreCase = true)
                        val coincideCategoria = item.categoria.any { it.contains(nombre, ignoreCase = true) }
                        coincideNombre || coincideCategoria
                    }
                } else {
                    // 🔹 Si hay una categoría específica, primero filtra por esa categoría
                    val listaFiltradaPorCategoria = lista.filter { item ->
                        item.categoria.any { it.contains(categoria, ignoreCase = true) }
                    }

                    // 🔹 Luego busca por nombre dentro de esa categoría filtrada
                    listaFiltradaPorCategoria.filter { item ->
                        item.lugar_nombre.contains(nombre, ignoreCase = true)
                    }
                }

                Log.d("buscamos_por", "Resultado filtrado: $resultado")

                state_servicios.value = if (resultado.isNotEmpty()) {
                    carga_servicios.succes(resultado)
                } else {
                    carga_servicios.emoty("No se encontraron resultados")
                }

            } catch (e: Exception) {
                Log.e("buscamos_por", "Error al filtrar datos", e)
                state_servicios.value = carga_servicios.error("Error al filtrar datos")
            }
        }
    }



    sealed class carga_servicios {
        object loading : carga_servicios()
        data class emoty(val texto: String) : carga_servicios()
        data class succes(val items: List<dataclass_lugares_db>) : carga_servicios()
        data class error(val texto: String) : carga_servicios()
    }
}