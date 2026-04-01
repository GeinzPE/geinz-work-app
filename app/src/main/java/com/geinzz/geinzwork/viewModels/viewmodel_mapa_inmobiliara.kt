package com.geinzz.geinzwork.viewModels

import androidx.lifecycle.ViewModel
import com.geinzz.geinzwork.data.model.datos_viewmodel_inmobiliara
import com.geinzz.geinzwork.model.repo_mapa_inmobiliara
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class viewmodel_mapa_inmobiliara : ViewModel() {

    private val instance_repo_mapa_inmobiliara = repo_mapa_inmobiliara()


    private val guardar_datos_inmuble = MutableStateFlow(datos_viewmodel_inmobiliara())

    val datosInmueble: StateFlow<datos_viewmodel_inmobiliara> = guardar_datos_inmuble.asStateFlow()


    fun agregar_datos_para_pasa_mapa(datos: datos_viewmodel_inmobiliara) {
        guardar_datos_inmuble.value = datos
    }


}