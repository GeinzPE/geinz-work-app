package com.geinzz.geinzwork.viewModels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.geinzz.geinzwork.model.repo_lugares_turisticos
import kotlinx.coroutines.launch

class viewModel_lugares_turisticos : ViewModel() {
    private val repo_lugares = repo_lugares_turisticos()

    private val categorias_filtrado = MutableLiveData<List<String>>()
    val _categorias_filtrados: LiveData<List<String>> get() = categorias_filtrado


    fun obtener_categorias() {
        viewModelScope.launch {
            try {
                categorias_filtrado.value = repo_lugares.obtener_filtrado_lugares()
            } catch (e: Exception) {
                categorias_filtrado.value = emptyList()
            }
        }
    }
}