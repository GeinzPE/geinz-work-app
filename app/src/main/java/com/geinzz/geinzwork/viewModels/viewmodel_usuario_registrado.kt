package com.geinzz.geinzwork.viewModels

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.geinzz.geinzwork.data.model.localizate_geinz.inicio_geinz.datos_principales_user
import com.geinzz.geinzwork.model.repo_principal_geinz_work
import com.geinzz.geinzwork.model.repo_usuario_registrado
import kotlinx.coroutines.launch

class viewmodel_usuario_registrado: ViewModel() {
    val instacia = repo_usuario_registrado()
    private val _userData = MutableLiveData<datos_principales_user>()
    val userData: LiveData<datos_principales_user> = _userData

    fun obtener_datos_user_registrado(id_user: String) {
        viewModelScope.launch {
            try {
                _userData.value = instacia.obtenerDatosUser(id_user)
                Log.d("impramos_vakoreas",_userData.value.toString())
            } catch (e: Exception) {
                _userData.value = datos_principales_user()
            }
        }
    }
}