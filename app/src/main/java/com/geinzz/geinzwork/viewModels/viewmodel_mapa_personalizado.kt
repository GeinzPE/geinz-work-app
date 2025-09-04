package com.geinzz.geinzwork.viewModels

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.geinzz.geinzwork.data.model.localizate_geinz.inicio_geinz.lugares_turisticos
import com.geinzz.geinzwork.model.repo_lugares_turisticos
import com.geinzz.geinzwork.model.repo_principal_geinz_work
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class viewmodel_mapa_personalizado: ViewModel() {
    private val repo_principal_geinz = repo_principal_geinz_work()
    private val repo_lugares = repo_lugares_turisticos()

    private val lugares_turisiticos = MutableLiveData<List<lugares_turisticos>>()
    val _lugares_turisticos: LiveData<List<lugares_turisticos>> get() = lugares_turisiticos

    private var todosLosLugares = emptyList<lugares_turisticos>()
    private val _listaFiltrada = MutableStateFlow<List<lugares_turisticos>>(emptyList())
    val listaFiltrada: StateFlow<List<lugares_turisticos>> = _listaFiltrada


    fun lugares_turisticos(localidad: String) {
        viewModelScope.launch {
            try {
                lugares_turisiticos.value = repo_principal_geinz.obtener_lugares_turisticos(localidad)
            } catch (e: Exception) {
                lugares_turisiticos.value = emptyList()
            }
        }
    }

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

    }

}