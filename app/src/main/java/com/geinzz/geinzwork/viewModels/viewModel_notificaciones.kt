package com.geinzz.geinzwork.viewModels

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.geinzz.geinzwork.data.model.datamode_notificaciones.data_class_notificaciones
import com.geinzz.geinzwork.model.repo_notificaciones
import kotlinx.coroutines.launch

class viewModel_notificaciones : ViewModel() {
    val _notificar_vincualdos = MutableLiveData<List<data_class_notificaciones>>()

    val notificar_vincualdos: LiveData<List<data_class_notificaciones>> get() = _notificar_vincualdos

    val _notifica_cerrado_seccion = MutableLiveData<List<data_class_notificaciones>>()

    val notifica_cerrado_seccion: LiveData<List<data_class_notificaciones>> get() = _notifica_cerrado_seccion

    val instace = repo_notificaciones()

    fun notificar_vincualdos(id_user: String) {
        viewModelScope.launch {
            try {
                val lista_not = instace.notificar_tokes(id_user)
                _notificar_vincualdos.value = lista_not
            } catch (e: Exception) {
                _notificar_vincualdos.value = emptyList()
            }
        }
    }

    fun notificar_cerrado_seccion_vinculado(id_user: String, dispo_encontrado: String) {
        viewModelScope.launch {
            try {
                val lista_cerrado = instace.notificar_cerrado_Seccion(id_user, dispo_encontrado)
                _notifica_cerrado_seccion.value = lista_cerrado
            } catch (e: Exception) {
                _notifica_cerrado_seccion.value = emptyList()
            }
        }
    }

}