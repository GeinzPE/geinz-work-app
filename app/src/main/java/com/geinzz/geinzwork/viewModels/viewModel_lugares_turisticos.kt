package com.geinzz.geinzwork.viewModels

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.firebase.geofire.GeoFireUtils
import com.firebase.geofire.GeoLocation
import com.geinzz.geinzwork.data.model.localizate_geinz.filtrado_tiendas.lugares_cercanos
import com.geinzz.geinzwork.data.model.localizate_geinz.inicio_geinz.lugares_turisticos
import com.geinzz.geinzwork.model.repo_lugares_turisticos
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class viewModel_lugares_turisticos : ViewModel() {
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

    fun llenarlista_completa(lista: List<lugares_cercanos>) {
        Log.d("lista_encontrada", lista.size.toString())
        _lista_obtenida.value = lista
    }

    private var lista_general_completa= MutableStateFlow<List<lugares_cercanos>> (emptyList())



//    fun obtener_categorias() {
//        viewModelScope.launch {
//            try {
//                categorias_filtrado.value = repo_lugares.obtener_filtrado_lugares()
//            } catch (e: Exception) {
//                categorias_filtrado.value = emptyList()
//            }
//        }
//    }

    fun obtener_tiendas_cercanas(lista_subcategorias: List<String>,categoria:String,lat: Double, long: Double, radio: Double, localida: String) {
        Log.d("FltrmosPOR"," $categoria $lista_subcategorias")
        viewModelScope.launch {
            _state_carga_tiendas_cercanas.value = carga_tienda_cercanos.loading
            delay(250)
            try {

                repo_lugares.obtenerTiendasCercanas(
                    categoria,
                    lat,
                    long,
                    radio,
                    localida
                ) { it, lista_categoria ->
                    if (it.isNotEmpty() && lista_categoria.isNotEmpty()) {
                        Log.d("encontramos_cal", lista_categoria.toString())
                        llenarlista_completa(it)

                        _state_carga_tiendas_cercanas.value =
                            carga_tienda_cercanos.succes(it, lista_categoria)
                        lista_general_completa.value=it
                    } else {
                        _state_carga_tiendas_cercanas.value =
                            carga_tienda_cercanos.empty("No se encontraron tiendas cercanas en el radio de $radio Km")
                    }
                }
            } catch (e: Exception) {
                _state_carga_tiendas_cercanas.value =
                    carga_tienda_cercanos.empty("Error al encontrar tiendas")
            }
        }
    }





    fun limpiar_tiendas_cercanas() {
        _state_carga_tiendas_cercanas.value = carga_tienda_cercanos.loading
    }

    fun filtrar_por_subcategoria(
        lista_subcategorias: List<String>,
        subcategoria: String
    ) {
        viewModelScope.launch {
            val lista_base = _lista_obtenida.value
            _state_carga_tiendas_cercanas.value = carga_tienda_cercanos.loading
            try {
                val result = if (subcategoria == "Todos") {
                    lista_base
                } else {
                    lista_base.filter { i ->
                        i.categoria.lowercase().contains(subcategoria.lowercase())
                    }
                }

                _state_carga_tiendas_cercanas.value = carga_tienda_cercanos.succes(result, lista_subcategorias)

            } catch (e: Exception) {
                _state_carga_tiendas_cercanas.value =
                    carga_tienda_cercanos.error("error al obtener los resultados")
            }
        }
    }


    fun lugares_turisticos(localidad: String) {
        viewModelScope.launch {
            _stata_lugares_turisticos.value = carga_lugares_turisticos.loading
            try {
                val lugares_turisticos = repo_lugares.obtener_lugares_turisticos(localidad)
                val categoria_filtrado = repo_lugares.obtener_filtrado_lugares()
                if (lugares_turisticos.isNotEmpty() && categoria_filtrado.isNotEmpty()) {
                    _stata_lugares_turisticos.value =
                        carga_lugares_turisticos.succes(categoria_filtrado, lugares_turisticos)
                } else {
                    _stata_lugares_turisticos.value =
                        carga_lugares_turisticos.empty("No se encontraron lugares en $localidad")
                }
            } catch (e: Exception) {
                _stata_lugares_turisticos.value =
                    carga_lugares_turisticos.error("Error al cargar los lugares de $localidad")
            }
        }
    }


    //    fun obtener_Lugares_filtrado(localidad: String, subcategoria: String) {
//        viewModelScope.launch {
//            try {
//                lugares_turisiticos_filtrados.value =
//                    repo_lugares.obtener_lugares_turisticos_filtrados(localidad, subcategoria)
//            } catch (e: Exception) {
//                lugares_turisiticos_filtrados.value = emptyList()
//            }
//        }
//
//    }
    private var todosLosLugares = emptyList<lugares_turisticos>()

    fun todos_lugares(lista: List<lugares_turisticos>) {
        todosLosLugares = lista
    }

//    fun filtrar_por_subcategoria(subcategoria: String) {
//        _listaFiltrada.value = if (subcategoria == "Todos") {
//            todosLosLugares
//        } else {
//            todosLosLugares.filter { it.subcategoria_filtrado.contains(subcategoria) }
//        }
//
//        Log.d(
//            "ViewModel",
//            "Lista filtrada por '$subcategoria': ${_listaFiltrada.value.map { it.titulo }}"
//        )
//    }
//

    sealed class carga_lugares_turisticos {
        data class succes(
            val lista_categoria: List<String>,
            val lista_lugares: List<lugares_turisticos>
        ) : carga_lugares_turisticos()

        object loading : carga_lugares_turisticos()
        data class error(val txt: String) : carga_lugares_turisticos()
        data class empty(val txt: String) : carga_lugares_turisticos()
    }

    sealed class carga_tienda_cercanos {
        data class succes(
            val lista_lugares: List<lugares_cercanos>,
            val lista_categorias: List<String>
        ) : carga_tienda_cercanos()

        object loading : carga_tienda_cercanos()
        data class error(val texto: String) : carga_tienda_cercanos()
        data class empty(val txt: String) : carga_tienda_cercanos()
    }


}