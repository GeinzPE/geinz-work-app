package com.geinzz.geinzwork.viewModels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.geinzz.geinzwork.data.model.dataclass_user.data_class_usuario_general
import com.geinzz.geinzwork.model.imfo_user_repo
import kotlinx.coroutines.launch

class viewModel_usuarios_general : ViewModel() {
    val repo_usauri = imfo_user_repo()
    private val _verificado = MutableLiveData<Boolean>()
    private val _datos_usarios = MutableLiveData<List<data_class_usuario_general>>()


    val verificados_Bool: LiveData<Boolean> get() = _verificado
    val datos_user: LiveData<List<data_class_usuario_general>> get() = _datos_usarios
    fun ver_verificaro(id: String) {
        viewModelScope.launch {
            try {
                val trabajador_verificado = repo_usauri.verificado_user(id)
                _verificado.value = trabajador_verificado
            } catch (e: Exception) {
                _verificado.value = false

            }
        }
    }

    fun obtener_datos_trabajajdor() {
        viewModelScope.launch {
            try {
                val datos_user = repo_usauri.obtener_perfil_user()
                _datos_usarios.value = datos_user
            } catch (e: Exception) {
                _datos_usarios.value = emptyList()
            }
        }
    }
}