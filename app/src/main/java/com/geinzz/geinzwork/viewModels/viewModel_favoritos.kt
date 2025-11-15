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
    private val lista_categoria_filtrad = MutableStateFlow<List<String>>(emptyList())
    private val _lista_fv = MutableStateFlow<state_fv>(state_fv.loading)
    val lista_fv: StateFlow<state_fv> get() = _lista_fv


    fun obtener_favoritos(id_user: String) {
        viewModelScope.launch {
            _lista_fv.value = state_fv.loading
            try {
                repo_fv.obtener_favoritos_realtime(id_user) { pair ->
                    val (favoritos, categorias) = pair
                    val categoriasSinRepetir = categorias.distinct()
                    lista_categoria_filtrad.value = categoriasSinRepetir

                    _lista_fv.value = when {
                        favoritos.isNotEmpty() -> state_fv.succes(favoritos, categoriasSinRepetir)
                        else -> state_fv.empty
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _lista_fv.value = state_fv.error("Ocurrió un error, inténtalo nuevamente")
            }
        }
    }


    sealed class state_fv {
        object loading : state_fv()
        data class succes(val item: List<favoritos_guardados>,val lista_categoria: List<String>) : state_fv()
        object empty : state_fv()
        data class error(val txt: String) : state_fv()
    }


}