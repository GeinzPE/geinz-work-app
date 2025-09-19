package com.geinzz.geinzwork.viewModels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.geinzz.geinzwork.data.model.dataclass_seguridad.dataclass_seguridad
import com.geinzz.geinzwork.model.repo_seguridad_salud
import kotlinx.coroutines.launch

class viewmode_seguridad_salud : ViewModel() {
    private val datos_lugares = MutableLiveData<List<dataclass_seguridad>>()
    val _datos_lugares: LiveData<List<dataclass_seguridad>> get() = datos_lugares

    val instancia = repo_seguridad_salud()

    fun obtener_servicios(localidad: String) {
        viewModelScope.launch {
            try {
                datos_lugares.value = instancia.obtener_servicios_salud(localidad)
            } catch (e: Exception) {
                datos_lugares.value = emptyList()
            }
        }
    }
}