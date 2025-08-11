package com.geinzz.geinzwork.viewModels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.geinzz.geinzwork.model.dataclass_dispo_vinculados
import com.geinzz.geinzwork.model.repo_dispo_vinculados
import com.geinzz.geinzwork.model.repo_info_user
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class viewModel_dispo_vincualdos : ViewModel() {
    val instance_repo_vincualdos = repo_dispo_vinculados()
    val _obtener_dispo_vinculados = MutableStateFlow<List<dataclass_dispo_vinculados?>>(emptyList())
    val obtener_dispo_vinculados: StateFlow<List<dataclass_dispo_vinculados?>> = _obtener_dispo_vinculados
    val encontra_user_colection = repo_info_user()

    val _buscar_primario = MutableLiveData<String>()
    val buscar_primario: LiveData<String> get() = _buscar_primario

    fun obtener_vinculados() {
        viewModelScope.launch {
            try {
                instance_repo_vincualdos.obtener_dispo_vinculados().collect { lista ->
                    _obtener_dispo_vinculados.value = lista
                }
            } catch (e: Exception) {
                _obtener_dispo_vinculados.value = emptyList()

            }
        }
    }


    fun puedeCerrarSesionDispositivo(
        id_user: String,
    ) {
        viewModelScope.launch {
            try {
                val collection_user = encontra_user_colection.encontrar_user(id_user)
                val (encontrado, collection) = collection_user
                if (encontrado && collection != null) {
                    val buscarPrimario = instance_repo_vincualdos.buscar_primario(collection)
                    _buscar_primario.value = buscarPrimario
                }

            } catch (e: Exception) {
                _buscar_primario.value = ""
            }
        }


    }

}