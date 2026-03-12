package com.geinzz.geinzwork.viewModels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.geinzz.geinzwork.data.model.completeta_info_inmuebles
import com.geinzz.geinzwork.data.model.dataclass_geinz_inmobiliaria_principal
import com.geinzz.geinzwork.model.repo_inmobiliaria
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class viewmodel_inmobiliaria : ViewModel() {
    val instarepo = repo_inmobiliaria()

    private val _estado_carga_inmubles_principales =
        MutableStateFlow<estado_carga_principal_immubles>(estado_carga_principal_immubles.idle)

    val estado_carga_inmuebles_principales: StateFlow<estado_carga_principal_immubles> =
        _estado_carga_inmubles_principales


    private val _estado_carga_info_inmuebles =
        MutableStateFlow<etado_carga_info_inmuebles>(etado_carga_info_inmuebles.idle)

    val estado_carga_info_inmuebles: StateFlow<etado_carga_info_inmuebles> =
        _estado_carga_info_inmuebles

    private var lastDocument: DocumentSnapshot? = null

    // Agregar en el ViewModel
    fun limpiar_estado_info() {
        _estado_carga_info_inmuebles.value = etado_carga_info_inmuebles.idle
    }

    fun obtener_inmubles_dados(localidad_select: String, cargarMas: Boolean = false) {

        if (!cargarMas &&
            _estado_carga_inmubles_principales.value is estado_carga_principal_immubles.succes
        ) {
            return
        }

        viewModelScope.launch {

            if (!cargarMas) {
                _estado_carga_inmubles_principales.value =
                    estado_carga_principal_immubles.loading
            }

            try {

                val resultado = instarepo.obtener_inmuebles(localidad_select, lastDocument)

                val lista = resultado.first
                lastDocument = resultado.second

                if (lista.isNotEmpty()) {

                    if (cargarMas &&
                        _estado_carga_inmubles_principales.value is estado_carga_principal_immubles.succes
                    ) {

                        val actual =
                            (_estado_carga_inmubles_principales.value as estado_carga_principal_immubles.succes).lista_inmuebles

                        _estado_carga_inmubles_principales.value =
                            estado_carga_principal_immubles.succes(actual + lista)

                    } else {

                        _estado_carga_inmubles_principales.value =
                            estado_carga_principal_immubles.succes(lista)

                    }

                } else {

                    if (!cargarMas) {
                        _estado_carga_inmubles_principales.value =
                            estado_carga_principal_immubles.empty("No se encontraron datos")
                    }

                }

            } catch (e: Exception) {
                Log.d("error_inmubles", "error al obtener los inmuebles $e")
            }
        }
    }

    fun cargarDatos(id: String, localidad: String) {

        viewModelScope.launch {

            try {

                val datos = instarepo.obtner_datos_completos_del_inmueble(
                    id,
                    localidad
                )

                _estado_carga_info_inmuebles.value =
                    etado_carga_info_inmuebles.succes(datos)

            } catch (e: Exception) {

                _estado_carga_info_inmuebles.value =
                    etado_carga_info_inmuebles.error(e.message ?: "error")

            }

        }

    }


//    fun agregar_geo(){
//        viewModelScope.launch {
//            try {
//                instarepo.agregar_geohasgin_turistico()
//
//            }catch (e: Exception){
//
//            }
//        }
//    }



    sealed class etado_carga_info_inmuebles {
        data class succes(val datos: completeta_info_inmuebles) : etado_carga_info_inmuebles()
        data class error(val txt: String = "error") : etado_carga_info_inmuebles()
        object idle : etado_carga_info_inmuebles()
    }


    sealed class estado_carga_principal_immubles {
        data class succes(val lista_inmuebles: List<dataclass_geinz_inmobiliaria_principal>) :
            estado_carga_principal_immubles()

        data class empty(val texto: String) : estado_carga_principal_immubles()
        data class error(val texto: String) : estado_carga_principal_immubles()
        object idle : estado_carga_principal_immubles()
        object loading : estado_carga_principal_immubles()
    }

}