package com.geinzz.geinzwork.viewModels

import android.R
import android.content.Context
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.geinzz.geinzwork.data.model.datos_tienda
import com.geinzz.geinzwork.data_store.data_store_localidad.set_id_socio
import com.geinzz.geinzwork.model.repo_eres_socio
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

@RequiresApi(Build.VERSION_CODES.O)
class viewmodel_eres_socio : ViewModel() {

    val instace_repo = repo_eres_socio()

    private val _state_eres_socio = MutableStateFlow<carga_acces_socio>(carga_acces_socio.loading)
    val state_eres_socio: StateFlow<carga_acces_socio> = _state_eres_socio


    private val _state_cerrar = MutableStateFlow<Boolean>(false)
    val state_cerrar: StateFlow<Boolean> = _state_cerrar

    private val _state_abierto = MutableStateFlow<Boolean>(false)
    val state_abierto: StateFlow<Boolean> = _state_abierto

    private var listenerDatosTienda: ListenerRegistration? = null

    fun verificar_seccion(context: Context, id_tienda: String,localidad_tienda:String) {

        listenerDatosTienda?.remove()   // si ya había uno activo, lo eliminamos

        _state_eres_socio.value = carga_acces_socio.loading

        listenerDatosTienda = instace_repo.escuchar_datos_tienda(
            localidad_tienda,
            id_tienda,
            resultado = { datos ->
                if (datos.nombre.isNotEmpty()) {
                    _state_eres_socio.value = carga_acces_socio.succes(datos)
                    viewModelScope.launch {
                    set_id_socio(context, datos.id_tienda)
                    }
                } else {
                    _state_eres_socio.value = carga_acces_socio.error("No se encontraron datos")
                }
            },
            error = { e ->
                _state_eres_socio.value = carga_acces_socio.error("Error: ${e.message}")
            }
        )
    }

    fun cambiar_cerrado(
        id_tienda: String,
        dia: String,
        motivo: String,
        bloques: List<Map<String, String>>
    ) {
        viewModelScope.launch {
            try {
                instace_repo.guardar_horario_cerrado(id_tienda, dia, motivo, bloques)
                _state_cerrar.value = true
            } catch (e: Exception) {
                _state_cerrar.value = false
                Log.d("error", "$e")

            }
        }

    }

    fun cambiar_abierto(
        id_tienda: String,
        dia: String,
        bloques: List<Map<String, String>>
    ) {
        viewModelScope.launch {
            try {
                instace_repo.guardar_horario_atencion_abierto(id_tienda, dia, bloques)
                _state_abierto.value = true
            } catch (e: Exception) {
                _state_abierto.value = false
                Log.d("error", "$e")

            }

        }
    }


    sealed class carga_acces_socio {
        data class succes(val datos: datos_tienda) : carga_acces_socio()
        data class error(val txt: String) : carga_acces_socio()
        object loading : carga_acces_socio()
    }

}