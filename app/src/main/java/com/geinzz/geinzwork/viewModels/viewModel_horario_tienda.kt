package com.geinzz.geinzwork.viewModels

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.geinzz.geinzwork.data.model.localizate_geinz.HorarioTienda
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class viewModel_horario_tienda: ViewModel() {
    var mostrandoCarga by mutableStateOf(true)
    var existeHorario by mutableStateOf(false)

    fun cargarHorario(horarioTienda: HorarioTienda?) {
        viewModelScope.launch {
            mostrandoCarga = true
            delay(2000)
            existeHorario = horarioTienda != null
            mostrandoCarga = false
        }
    }
    fun resetEstado() {
        mostrandoCarga = true
        existeHorario = true
    }

    private val tiendasCargadas = mutableSetOf<String>()

    fun horarioYaCargado(idTienda: String): Boolean {
        return tiendasCargadas.contains(idTienda)
    }

    fun cargarHorario(idTienda: String) {
        // Lógica de carga de datos
        tiendasCargadas.add(idTienda)
    }

}