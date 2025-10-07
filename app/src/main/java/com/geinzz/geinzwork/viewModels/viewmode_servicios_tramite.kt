package com.geinzz.geinzwork.viewModels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.geinzz.geinzwork.data.model.dataclass_lugares_db
import com.geinzz.geinzwork.model.repo_servicios_tramites
import kotlinx.coroutines.launch

class viewmode_servicios_tramite : ViewModel(){
    private val isnta= repo_servicios_tramites()
     private val _lugares= MutableLiveData<List<dataclass_lugares_db>>()
    val lugares : LiveData<List<dataclass_lugares_db>> get() =_lugares

    fun obtener_lugares(localida:String){
        viewModelScope.launch {
            try {
                _lugares.value=isnta.obtenerServiciosTramites(localida)
            }catch (e: Exception){
                _lugares.value=emptyList()
            }
        }
    }
}