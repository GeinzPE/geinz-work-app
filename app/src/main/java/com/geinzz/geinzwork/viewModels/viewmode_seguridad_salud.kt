package com.geinzz.geinzwork.viewModels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.geinzz.geinzwork.data.model.dataclass_seguridad.dataclass_seguridad
import com.geinzz.geinzwork.model.repo_seguridad_salud
import com.geinzz.geinzwork.ui.adapters.ui.pantallas.salud_seguridad.carta_salud_cuidad
import com.geinzz.geinzwork.viewModels.viewmode_servicios_tramite.carga_servicios
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class viewmode_seguridad_salud : ViewModel() {
    val instancia = repo_seguridad_salud()
    private val datos_lugares = MutableLiveData<List<dataclass_seguridad>>()
    val _datos_lugares: LiveData<List<dataclass_seguridad>> get() = datos_lugares

    private val _coordenadasSeleccionadas = MutableLiveData<Pair<Double, Double>?>()

    private val _listaFiltrada = MutableStateFlow<List<dataclass_seguridad>>(emptyList())
    val lista_filtrada: StateFlow<List<dataclass_seguridad>> = _listaFiltrada

    private val _state_lista_filtrada = MutableStateFlow<carga_seguidad>(carga_seguidad.loading)
    val state_lista_filtradad: StateFlow<carga_seguidad> = _state_lista_filtrada

    val coordenadasSeleccionadas: LiveData<Pair<Double, Double>?> = _coordenadasSeleccionadas


    var todos_lugares = mutableListOf<dataclass_seguridad>()
        private set

    fun obtener_servicios(localidad: String) {
        viewModelScope.launch {
            _state_lista_filtrada.value= carga_seguidad.loading
            try {
                val respuesta=instancia.obtener_servicios_salud(localidad)
                datos_lugares.value = respuesta
                if(respuesta.isNotEmpty()){
                    _state_lista_filtrada.value=carga_seguidad.succes(respuesta)
                }else{
                    delay(300)
                    _state_lista_filtrada.value=carga_seguidad.empity("No se encontraron resultados en $localidad")
                }
            } catch (e: Exception) {
                datos_lugares.value = emptyList()
                _state_lista_filtrada.value=carga_seguidad.error("Error al cargar los datos")

            }
        }
    }

    fun setCoordenadas(lat: Double, lon: Double) {
        _coordenadasSeleccionadas.value = lat to lon
    }

    fun lugares_iniciales(lista: List<dataclass_seguridad>) {
        todos_lugares.clear()
        todos_lugares.addAll(lista)
    }

    fun horario_atencion(nombre: String): String {
        return instancia.atencion_24h(nombre)
    }

//    fun mostar_lugar_por_nombre(
//        nombre: String,
//        lista: List<dataclass_seguridad>
//    ): List<dataclass_seguridad> {
//        return lista.filter { it.nombre_.contains(nombre, ignoreCase = true) }
//    }
//
//    fun filtar_por_categorias(categoria: String,lista: List<dataclass_seguridad>): List<dataclass_seguridad>{
//        return lista.filter { it.categoria.contains(categoria, ignoreCase = true) }
//    }


    //    fun filtrar_lugares(
//        nombre: String,
//        categoria: String,
//        lista: List<dataclass_seguridad>
//    ): List<dataclass_seguridad> {
//
//
//        return lista.filter { item ->
//            val coincideTexto = nombre.isBlank() || item.nombre_.contains(nombre, ignoreCase = true)
//            val coincideCategoria =
//                categoria == "Todos" || item.categoria.contains(categoria, ignoreCase = true)
//            coincideTexto && coincideCategoria
//        }
//    }
    fun filtrar_lugares(
        categoria: String,
    ){
        viewModelScope.launch {
            _state_lista_filtrada.value=carga_seguidad.loading
            try {
                val resultado=if(categoria=="Todos"){
                    todos_lugares
                }else{
                    todos_lugares.filter {
                        it.categoria.lowercase().contains(categoria.lowercase())
                    }
                }

                if(resultado.isNotEmpty()){
                    _state_lista_filtrada.value= carga_seguidad.succes(resultado)
                }else{
                    _state_lista_filtrada.value= carga_seguidad.empity("No se encontraron resutlados")
                }
            }catch (e: Exception){
                _state_lista_filtrada.value= carga_seguidad.empity("No se encontraron resutlados")

            }
        }
    }

    fun lista_base_completa(categorias: String){
        viewModelScope.launch {
            if (categorias == "Todos" && todos_lugares.isNotEmpty()) {
                _state_lista_filtrada.value= carga_seguidad.succes(todos_lugares)
                return@launch
            }
        }
    }

    fun filtrar_nombre_categoria(
        nombre: String,
        categoria: String,
        lista: List<dataclass_seguridad>
    ){
        viewModelScope.launch {
            try {
                _state_lista_filtrada.value= carga_seguidad.loading
                val res = lista.filter { item ->
                    val textoCoincide = item.nombre_.contains(nombre, ignoreCase = true)
                    val categoriaCoincide = categoria == "Todos" || item.categoria == categoria
                    textoCoincide && categoriaCoincide
                }
                if (res.isNotEmpty()) {
                    _state_lista_filtrada.value = carga_seguidad.succes(res)
                } else {
                    _state_lista_filtrada.value = carga_seguidad.empity("No se encontraron resultados")

                }
            }catch (e: Exception){
                _state_lista_filtrada.value = carga_seguidad.error("Error al filtrar datos")

            }
        }
    }

    sealed class carga_seguidad {
        data class empity(val texto: String) : carga_seguidad()
        data class succes(val list: List<dataclass_seguridad>) : carga_seguidad()
        data class error(val texto: String) : carga_seguidad()
        object loading : carga_seguidad()
    }


}