package com.geinzz.geinzwork.viewModels

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.geinzz.geinzwork.data.model.datos_tienda
import com.geinzz.geinzwork.data_store.data_store_localidad.set_id_socio
import com.geinzz.geinzwork.model.repo_eres_socio
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class viewmodel_eres_socio : ViewModel() {

    val instace_repo = repo_eres_socio()

    private val _state_eres_socio = MutableStateFlow<carga_acces_socio>(carga_acces_socio.loading)
    val state_eres_socio: StateFlow<carga_acces_socio> = _state_eres_socio


    fun verificar_seccion(context: Context, id_tienda: String) {
        viewModelScope.launch {
            try {
                _state_eres_socio.value = carga_acces_socio.loading

                val datos = instace_repo.obtener_datos_tienda(id_tienda)

                if (datos.nombre.isNotEmpty()) {
                    Log.d("estadosocio", datos.toString())
                    _state_eres_socio.value = carga_acces_socio.succes(datos)
                    set_id_socio(context, datos.id_tienda)
                } else {
                    Log.d("estadosocio", "No se encontraron datos sobre ese id")
                    _state_eres_socio.value =
                        carga_acces_socio.error("No se encontraron datos sobre ese id")
                }

            } catch (e: Exception) {
                _state_eres_socio.value = carga_acces_socio.error("Error: ${e.message}")
            }
        }
    }


    sealed class carga_acces_socio {
        data class succes(val datos: datos_tienda) : carga_acces_socio()
        data class error(val txt: String) : carga_acces_socio()
        object loading : carga_acces_socio()
    }

}