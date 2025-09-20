package com.geinzz.geinzwork.viewModels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.geinzz.geinzwork.data.model.dataclass_seguridad.dataclass_seguridad
import com.geinzz.geinzwork.model.repo_seguridad_salud
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

    val coordenadasSeleccionadas: LiveData<Pair<Double, Double>?> = _coordenadasSeleccionadas

    var todos_lugares = mutableListOf<dataclass_seguridad>()
        private set

    fun obtener_servicios(localidad: String) {
        viewModelScope.launch {
            try {
                datos_lugares.value = instancia.obtener_servicios_salud(localidad)
            } catch (e: Exception) {
                datos_lugares.value = emptyList()
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

    fun mostar_lugar_por_nombre(
        nombre: String,
        lista: List<dataclass_seguridad>
    ): List<dataclass_seguridad> {
        return lista.filter { it.nombre_.contains(nombre, ignoreCase = true) }
    }

    fun filtar_por_categorias(categoria: String,lista: List<dataclass_seguridad>): List<dataclass_seguridad>{
        return lista.filter { it.categoria.contains(categoria, ignoreCase = true) }
    }


    fun filtrar_lugares(
        nombre: String,
        categoria: String,
        lista: List<dataclass_seguridad>
    ): List<dataclass_seguridad> {
        return lista.filter { item ->
            val coincideTexto = nombre.isBlank() || item.nombre_.contains(nombre, ignoreCase = true)
            val coincideCategoria = categoria == "Todos" || item.categoria.contains(categoria, ignoreCase = true)
            coincideTexto && coincideCategoria
        }
    }



    fun actualizar_lista_filtrada(nuevaLista: List<dataclass_seguridad>) {
        _listaFiltrada.value=nuevaLista
    }

}