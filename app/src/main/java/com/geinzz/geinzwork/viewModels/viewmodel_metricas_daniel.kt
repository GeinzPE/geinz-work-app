package com.geinzz.geinzwork.viewModels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.geinzz.geinzwork.data.model.daniel_metricas.MetricasResumen
import com.geinzz.geinzwork.model.repo_metricas_daniel_wsap

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch


class viewmodel_metricas_daniel : ViewModel() {
    private val repository = repo_metricas_daniel_wsap()
    sealed class EstadoMetricas {
        object Idle    : EstadoMetricas()
        object Loading : EstadoMetricas()
        data class Success(val data: MetricasResumen) : EstadoMetricas()
        data class Error(val mensaje: String)         : EstadoMetricas()
    }

    private val _estado = MutableStateFlow<EstadoMetricas>(EstadoMetricas.Idle)
    val estado: StateFlow<EstadoMetricas> = _estado.asStateFlow()

    fun cargarMetricas(id_tienda: String) {
        if (_estado.value is EstadoMetricas.Loading) return
        _estado.value = EstadoMetricas.Loading
        viewModelScope.launch {
            val result = repository.obtenerResumenMetricas(id_tienda)
            _estado.value = if (result.isSuccess) {
                EstadoMetricas.Success(result.getOrNull()!!)
            } else {
                EstadoMetricas.Error(result.exceptionOrNull()?.message ?: "Error desconocido")
            }
        }
    }

    fun recargar(id_tienda: String) {
        _estado.value = EstadoMetricas.Idle
        cargarMetricas(id_tienda)
    }

}