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
import com.geinzz.geinzwork.ui.adapters.ui.pantallas.servicios_basicos.carta_servicio_tramites
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.constantes_lista_localidades.isInternetAvailable
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
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

    init {
        viewModelScope.launch {
            state_servicios.value = carga_servicios.loading
        }
    }

    fun obtener_lugares(context: Context, localida: String) {
        viewModelScope.launch {
            try {

                state_servicios.value = carga_servicios.loading
                if (!isInternetAvailable(context)) {
                    state_servicios.value = carga_servicios.error("Sin conexión a internet 😕")
                    return@launch
                }
                val res = isnta.obtenerServiciosTramites(localida)
                _lugares.value = res
                if (res.isNotEmpty()) {
                    state_servicios.value = carga_servicios.succes(res)
                } else {
                    // Espera un poco antes de mostrar el "vacío"
                    delay(300)
                    state_servicios.value =
                        carga_servicios.emoty("No hay datos registrados de $localida")
                }

            } catch (e: Exception) {
                _lugares.value = emptyList()
                state_servicios.value =
                    carga_servicios.error("Ocurrió un error al cargar los datos")
            }
        }
    }


    fun todos(lista: List<dataclass_lugares_db>) {

        todo_lugares = lista
//        state_servicios.value = carga_servicios.succes(lista)
//        _listaFiltrada.value = lista
        Log.d("todo_lugares_agregardos", lista.toString())
    }

    fun filtrar_por_categoria(context: Context, categorias: String) {
        Log.d("filtraoms",categorias)
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
                val resultado= todo_lugares.filter {
                    it.categoria.toString().lowercase().contains(categorias.lowercase())
                }
//                }

                if (resultado.isNotEmpty()) {
                    state_servicios.value = carga_servicios.succes(resultado)
                } else {
                    state_servicios.value =
                        carga_servicios.emoty("No hay datos en la categoría $categorias")
                }

            } catch (e: Exception) {
                state_servicios.value =
                    carga_servicios.error("Error inesperado: ${e.localizedMessage}")
            }
        }
    }

    fun mostar_lista_completa(categorias: String){
        viewModelScope.launch {
            if (categorias == "Todos" && todo_lugares.isNotEmpty()) {
                state_servicios.value = carga_servicios.succes(todo_lugares)
                return@launch
            }
        }

    }

    //    fun filtrar_nombre_categoria(
//        nombre: String,
//        categoria: String,
//        lista: List<dataclass_lugares_db>
//    ): List<dataclass_lugares_db> {
//        return lista.filter { item ->
//            val conicide_TXT =
//                nombre.isBlank() || item.lugar_nombre.contains(nombre, ignoreCase = true)
//            val coincidenciaExacta = categoria == "Todos" || item.categoria.any { i ->
//                i.contains(
//                    categoria,
//                    ignoreCase = true
//                )
//            }
//            conicide_TXT && coincidenciaExacta
//        }
//
//    }

    fun filtrar_nombre_categoria(
        nombre: String,
        categoria: String,
        lista: List<dataclass_lugares_db>
    ) {
        Log.d("bucamos_por","${nombre} $categoria $lista")

        viewModelScope.launch {
            try {
                // 🔹 Si hay búsqueda válida, mostramos el "loading"
                state_servicios.value = carga_servicios.loading

                val resultado = lista.filter { item ->
                    val coincideTexto = item.lugar_nombre.contains(nombre, ignoreCase = true)
                    val coincideCategoria =
                        categoria == "Todos" || item.categoria.any {
                            it.contains(categoria, ignoreCase = true)
                        }
                    coincideTexto && coincideCategoria
                }

                if (resultado.isNotEmpty()) {
                    state_servicios.value = carga_servicios.succes(resultado)
                } else {
                    state_servicios.value = carga_servicios.emoty("No se encontraron resultados")
                }

            } catch (e: Exception) {
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