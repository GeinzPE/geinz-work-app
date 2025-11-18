package com.geinzz.geinzwork.viewModels

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.geinzz.geinzwork.data_store.data_store_localidad.guardarUrlsCarga
import com.geinzz.geinzwork.data_store.data_store_localidad.guardarUrlsCarga_turismo
import com.geinzz.geinzwork.data_store.data_store_localidad.obtenerUrlsCarga
import com.geinzz.geinzwork.data_store.data_store_localidad.obtenerUrlsCarga_turismo
import com.geinzz.geinzwork.model.repo_carga_img_general
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class viewmodel_carga_img_general(
    private val context: Context,
) : ViewModel(){
    private val _urlsCarga = MutableStateFlow<List<String>>(emptyList())
    val urlsCarga = _urlsCarga.asStateFlow()

    private val _urlsCarga_turistico = MutableStateFlow<List<String>>(emptyList())
    val urlsCarga_turistico= _urlsCarga_turistico.asStateFlow()

    val repo= repo_carga_img_general()
    init {
        cargarUrls()
        cargar_url_lugares_turitsticos()
    }
    private fun cargarUrls() {
        viewModelScope.launch {
            try {
            val locales = obtenerUrlsCarga(context)

            if (locales.isNotEmpty()) {
                _urlsCarga.value = locales
            } else {
                val desdeFirebase = repo.obtenerUrlsCarga()

                if (desdeFirebase.isNotEmpty()) {
                    guardarUrlsCarga(context, desdeFirebase)
                    _urlsCarga.value = desdeFirebase
                }
            }
            }catch (e: Exception){
                Log.d("img_error","error al obtenr la img")
            }
        }
    }

    private fun cargar_url_lugares_turitsticos() {
        viewModelScope.launch {
            try {
                val locales = obtenerUrlsCarga_turismo(context)

                if (locales.isNotEmpty()) {
                    _urlsCarga_turistico.value = locales
                } else {
                    val desdeFirebase = repo.obtenerUrlsCarga_lugares_turisticos()

                    if (desdeFirebase.isNotEmpty()) {
                        guardarUrlsCarga_turismo(context, desdeFirebase)
                        _urlsCarga_turistico.value = desdeFirebase
                    }
                }
            }catch (e: Exception){
                Log.d("img_error","error al obtenr la img")
            }
        }
    }
}