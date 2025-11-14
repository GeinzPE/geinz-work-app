package com.geinzz.geinzwork.viewModels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.geinzz.geinzwork.data.model.localizate_geinz.inicio_geinz.favoritos_guardados
import com.geinzz.geinzwork.model.repo_favoritos
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class viewModel_favoritos : ViewModel() {
    private val repo_fv = repo_favoritos()
    private val _lista_fv = MutableStateFlow<state_fv>(state_fv.loading)
    val lista_fv: StateFlow<state_fv> get() = _lista_fv


    fun obtener_favoritos(id_user: String) {
        viewModelScope.launch {
            _lista_fv.value = state_fv.loading
            try {
                val lista_retorno = repo_fv.obtener_favoritos(id_user = id_user)
                if (lista_retorno.isNotEmpty()) {
                    _lista_fv.value = state_fv.succes(lista_retorno)
                } else {
                    _lista_fv.value = state_fv.empty
                }
            } catch (e: Exception) {
                _lista_fv.value = state_fv.error("Ocurrio un error intentalo nuevamente")
            }
        }
    }

    sealed class state_fv {
        object loading : state_fv()
        data class succes(val item: List<favoritos_guardados>) : state_fv()
        object empty : state_fv()
        data class error(val txt: String) : state_fv()
    }


}