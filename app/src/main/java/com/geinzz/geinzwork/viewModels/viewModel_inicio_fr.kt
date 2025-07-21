package com.geinzz.geinzwork.viewModels


import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.geinzz.geinzwork.data.model.data_model_inicio_fr_
import com.geinzz.geinzwork.data.model.data_model_trabajador_scanner
import com.geinzz.geinzwork.model.dataClassCategoriasInicio
import com.geinzz.geinzwork.model.dataClassTrabajosd
import com.geinzz.geinzwork.model.repo_info

import kotlinx.coroutines.launch

class viewModel_inicio_fr : ViewModel() {

    //live_data
    private val _categorias = MutableLiveData<List<dataClassCategoriasInicio>>()
    private val _img_firestore = MutableLiveData<List<data_model_inicio_fr_>>()
    private val _scanner_trabajador = MutableLiveData<List<data_model_trabajador_scanner>>()
    private val _mejores_Trabajdores = MutableLiveData<List<dataClassTrabajosd>>()
    private val _trabajadores_por_cat = MutableLiveData<Map<String, List<dataClassTrabajosd>>>()

    private val _servicios_cat = MutableLiveData<List<dataClassTrabajosd>>()
    private val _construcion_hogar = MutableLiveData<List<dataClassTrabajosd>>()
    private val _reparto_coductor = MutableLiveData<List<dataClassTrabajosd>>()
    private val _tecnicos = MutableLiveData<List<dataClassTrabajosd>>()
    private val _mecanicos = MutableLiveData<List<dataClassTrabajosd>>()


    //escuchadores
    val cat: LiveData<List<dataClassCategoriasInicio>> get() = _categorias
    val img_firestore: LiveData<List<data_model_inicio_fr_>> get() = _img_firestore
    val scaner: LiveData<List<data_model_trabajador_scanner>> get() = _scanner_trabajador
    val trabajadores: LiveData<List<dataClassTrabajosd>> get() = _mejores_Trabajdores
    val categorias_trabajadores: LiveData<Map<String, List<dataClassTrabajosd>>> get() = _trabajadores_por_cat

    val escucha_servicio: LiveData<List<dataClassTrabajosd>> get() = _servicios_cat
    val escucha_constructor: LiveData<List<dataClassTrabajosd>> get() = _construcion_hogar
    val escucha_reparto: LiveData<List<dataClassTrabajosd>> get() = _reparto_coductor
    val escucha_tecnicos: LiveData<List<dataClassTrabajosd>> get() = _tecnicos
    val escucha_mecanicos: LiveData<List<dataClassTrabajosd>> get() = _mecanicos


    private val _cargando = MutableLiveData<Boolean>()
    val cargando: LiveData<Boolean> get() = _cargando


    //instacia del repo_info
    private val repo = repo_info()

    fun cargar_categorias() {
        viewModelScope.launch {
            try {
                val categoriasObtenidas = repo.obtenerTrabajosCat()
                _categorias.value = categoriasObtenidas
            } catch (e: Exception) {
                _categorias.value = emptyList()
            }
        }
    }

    fun cargar_img_fire() {
        viewModelScope.launch {
            try {
                val img_obtenidas = repo.obtener_img_firestore()
                _img_firestore.value = img_obtenidas
            } catch (e: Exception) {
                _img_firestore.value = emptyList()

            }
        }
    }

    fun obtenerScannerTrabajador(result: String) {
        viewModelScope.launch {
            try {
                val campos_obtenidos = repo.obtener_res_scanner(result)
                _scanner_trabajador.value = campos_obtenidos
            } catch (e: Exception) {
                _scanner_trabajador.value = emptyList()
            }
        }
    }

    fun obtener_mejores_trabajadores(filtrado_shader: String, tiempoCallback: (Long) -> Unit) {
        viewModelScope.launch {
            try {
                _cargando.value = true
                val mejores = repo.obtener_mejores_trabajadores(filtrado_shader, tiempoCallback)
                _mejores_Trabajdores.value = mejores
            } catch (e: Exception) {
                _mejores_Trabajdores.value = emptyList()
            } finally {
                _cargando.value = false
            }
        }
    }


    fun obtener_servicios(
        filtrado_localida: String,
        categoria: String,
        tiempoCallback: (Long) -> Unit
    ) {
        viewModelScope.launch {
            try {
                val lista = repo.obtner_trabajadores_por_categorias(
                    filtrado_localida,
                    categoria,
                    tiempoCallback
                )
                _servicios_cat.value = lista
            } catch (e: Exception) {
                _servicios_cat.value = emptyList()
            }
        }


    }

    fun obtener_construcion_hogar(
        filtrado_localida: String,
        categoria: String,
        tiempoCallback: (Long) -> Unit
    ) {
        viewModelScope.launch {
            try {
                val lista = repo.obtner_trabajadores_por_categorias(
                    filtrado_localida,
                    categoria,
                    tiempoCallback
                )
                _construcion_hogar.value = lista
            } catch (e: Exception) {
                _construcion_hogar.value = emptyList()
            }
        }
    }

    fun conductor_reparto(
        filtrado_localida: String,
        categoria: String,
        tiempoCallback: (Long) -> Unit
    ) {
        viewModelScope.launch {
            try {
                val lista = repo.obtner_trabajadores_por_categorias(
                    filtrado_localida,
                    categoria,
                    tiempoCallback
                )
                _reparto_coductor.value = lista
            } catch (e: Exception) {
                _reparto_coductor.value = emptyList()
            }
        }
    }

    fun tecnicos(filtrado_localida: String, categoria: String, tiempoCallback: (Long) -> Unit) {
        viewModelScope.launch {
            try {
                val lista = repo.obtner_trabajadores_por_categorias(
                    filtrado_localida,
                    categoria,
                    tiempoCallback
                )
                _tecnicos.value = lista
            } catch (e: Exception) {
                _tecnicos.value = emptyList()
            }
        }

    }

    fun mecanicos(filtrado_localida: String, categoria: String, tiempoCallback: (Long) -> Unit) {
        viewModelScope.launch {
            try {
                val lista = repo.obtner_trabajadores_por_categorias(
                    filtrado_localida,
                    categoria,
                    tiempoCallback
                )
                _mecanicos.value = lista
            } catch (e: Exception) {
                _mecanicos.value = emptyList()
            }
        }

    }

}