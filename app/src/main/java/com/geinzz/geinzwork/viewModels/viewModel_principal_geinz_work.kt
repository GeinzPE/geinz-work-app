package com.geinzz.geinzwork.viewModels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.geinzz.geinzwork.data.model.localizate_geinz.inicio_geinz.dataclass_cat_sub
import com.geinzz.geinzwork.data.model.localizate_geinz.inicio_geinz.lugares_turisticos
import com.geinzz.geinzwork.model.repo_agregar_cat_sub_localizate
import com.geinzz.geinzwork.model.repo_principal_geinz_work
import kotlinx.coroutines.launch

class viewModel_principal_geinz_work : ViewModel() {

    val instacia = repo_principal_geinz_work()
    val instacia_repo_cat_sub = repo_agregar_cat_sub_localizate()

    private val lugares_turisiticos = MutableLiveData<List<lugares_turisticos>>()
    val _lugares_turisticos: LiveData<List<lugares_turisticos>> get() = lugares_turisiticos

    private val sub_cat_tiendas =
        MutableLiveData<List<com.geinzz.geinzwork.data.model.localizate_geinz.dataclass_cat_sub>>()
    val _sub_cat_tiendas: LiveData<List<com.geinzz.geinzwork.data.model.localizate_geinz.dataclass_cat_sub>> get() = sub_cat_tiendas

    fun lugares_turisticos(localidad: String) {
        viewModelScope.launch {
            try {
                lugares_turisiticos.value = instacia.obtener_lugares_turisticos(localidad)
            } catch (e: Exception) {
                lugares_turisiticos.value = emptyList()
            }
        }
    }

    fun obtener_subcategorias() {
        viewModelScope.launch {
            try {
                sub_cat_tiendas.value = instacia_repo_cat_sub.obtener_categorias_subcategorias()
            } catch (e: Exception) {
                sub_cat_tiendas.value = emptyList()

            }
        }
    }
}