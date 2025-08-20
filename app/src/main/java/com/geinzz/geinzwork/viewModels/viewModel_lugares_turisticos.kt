package com.geinzz.geinzwork.viewModels

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.geinzz.geinzwork.data.model.localizate_geinz.filtrado_tiendas.tiendas_por_categoria
import com.geinzz.geinzwork.data.model.localizate_geinz.inicio_geinz.lugares_turisticos
import com.geinzz.geinzwork.model.repo_lugares_turisticos
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class viewModel_lugares_turisticos : ViewModel() {
    private val repo_lugares = repo_lugares_turisticos()

    private val categorias_filtrado = MutableLiveData<List<String>>()
    val _categorias_filtrados: LiveData<List<String>> get() = categorias_filtrado

//    private val lugares_turisiticos_filtrados = MutableLiveData<List<lugares_turisticos>>()
//    val _lugares_turisticos_filtrados: LiveData<List<lugares_turisticos>> get() = lugares_turisiticos_filtrados

    private val _listaFiltrada = MutableStateFlow<List<lugares_turisticos>>(emptyList())
    val listaFiltrada: StateFlow<List<lugares_turisticos>> = _listaFiltrada

    var lugares_turisticos = mutableListOf<lugares_turisticos>()
        private set

//    fun todos_lugares(lista: List<lugares_turisticos>) {
//        lugares_turisticos.clear()
//        lugares_turisticos.addAll(lista)
//    }


    fun obtener_categorias() {
        viewModelScope.launch {
            try {
                categorias_filtrado.value = repo_lugares.obtener_filtrado_lugares()
            } catch (e: Exception) {
                categorias_filtrado.value = emptyList()
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
        _listaFiltrada.value = lista
    }

    fun filtrar_por_subcategoria(subcategoria: String) {
        _listaFiltrada.value = if (subcategoria == "Todos") {
            todosLosLugares
        } else {
            todosLosLugares.filter { it.subcategoria_filtrado.contains(subcategoria) }
        }

        Log.d(
            "ViewModel",
            "Lista filtrada por '$subcategoria': ${_listaFiltrada.value.map { it.titulo }}"
        )
    }

}