package com.geinzz.geinzwork.viewModels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.geinzz.geinzwork.data.model.localizate_geinz.inicio_geinz.lugares_turisticos
import com.geinzz.geinzwork.model.repo_principal_geinz_work
import kotlinx.coroutines.launch

class viewModel_principal_geinz_work : ViewModel() {

    val instacia = repo_principal_geinz_work()

    val lugares_turisiticos = MutableLiveData<List<lugares_turisticos>>()
    val _lugares_turisticos: LiveData<List<lugares_turisticos>> get() = lugares_turisiticos

    fun lugares_turisticos(localidad: String) {
        viewModelScope.launch {
            try {
                lugares_turisiticos.value = instacia.obtener_lugares_turisticos(localidad)
            } catch (e: Exception) {
                lugares_turisiticos.value = emptyList()
            }
        }
    }
}