package com.geinzz.geinzwork.viewModels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.geinzz.geinzwork.data.model.dataclass_user.data_class_usuario_general
import com.geinzz.geinzwork.model.repo_info_user
import com.google.firebase.firestore.CollectionReference
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class viewModel_usuarios_general : ViewModel() {
    val repo_usauri = repo_info_user()
    private val _verificado = MutableLiveData<Boolean>()
    private val _datos_usarios = MutableLiveData<List<data_class_usuario_general>>()
    private val _encontrar_user = MutableLiveData<Pair<Boolean, CollectionReference?>>()

    val verificados_Bool: LiveData<Boolean> get() = _verificado
    val datos_user: LiveData<List<data_class_usuario_general>> get() = _datos_usarios
    val encontrar_user: LiveData<Pair<Boolean, CollectionReference?>> get() = _encontrar_user


    val nombre_localidad_user = MutableLiveData<Pair<String, String>>()
    val _nombre_localidad_user: LiveData<Pair<String, String>> get() = nombre_localidad_user

    val _accesoPermitido = MutableStateFlow<Boolean?>(null)
    val accesoPermitido: StateFlow<Boolean?> = _accesoPermitido

    val usuarioName = MutableLiveData<String>()
    val usuarioFiltrado = MutableLiveData<String>()


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


    fun encontra_user(id_android: String) {
        viewModelScope.launch {
            try {
                val acceso = repo_usauri.encontrar_user(id_android)
                _encontrar_user.value = acceso
            } catch (e: Exception) {
                _encontrar_user.value = Pair(false, null)  // <- corregido el orden
            }
        }
    }

    fun iniciarVerificacion(androidId: String) {
        viewModelScope.launch {
            try {
                repo_usauri.verificarAccesoTiempoRealFlow(androidId).collect { acceso ->
                    _accesoPermitido.value = acceso
                }
            } catch (e: Exception) {
                _accesoPermitido.value = false
            }

        }
    }


    fun obtener_localida_nombre_user(id: String) {
        viewModelScope.launch {
            try {
                nombre_localidad_user.value = repo_usauri.nombra_localidad_user(id)
            } catch (e: Exception) {
                nombre_localidad_user.value= Pair("","")

            }
        }
    }


}