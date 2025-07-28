package com.geinzz.geinzwork.viewModels

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.geinzz.geinzwork.model.repo_texto_cambios_validaciones
import kotlinx.coroutines.launch

class viewModel_text_validaciones : ViewModel() {
    val repo_textos_validator = repo_texto_cambios_validaciones()


    fun modelos_celulares_iguales_directo(nombre1: String, nombre2: String): Boolean {
        return repo_textos_validator.verificar_dispostivo_iguales(nombre1, nombre2)
    }

}