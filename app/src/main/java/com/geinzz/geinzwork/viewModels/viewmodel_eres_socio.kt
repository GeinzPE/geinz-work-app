package com.geinzz.geinzwork.viewModels

import android.R
import android.content.Context
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.runtime.State
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.geinzz.geinzwork.data.model.datos_tienda
import com.geinzz.geinzwork.data_store.data_store_localidad
import com.geinzz.geinzwork.data_store.data_store_localidad.set_id_socio
import com.geinzz.geinzwork.model.repo_eres_socio
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

@RequiresApi(Build.VERSION_CODES.O)
class viewmodel_eres_socio : ViewModel() {

    val instace_repo = repo_eres_socio()

    private val _state_eres_socio = MutableStateFlow<carga_acces_socio>(carga_acces_socio.idle)
    val state_eres_socio: StateFlow<carga_acces_socio> = _state_eres_socio


    private val _state_cerrar = MutableStateFlow<Boolean>(false)
    val state_cerrar: StateFlow<Boolean> = _state_cerrar

    private val _state_abierto = MutableStateFlow<Boolean>(false)
    val state_abierto: StateFlow<Boolean> = _state_abierto

    private var listenerDatosTienda: ListenerRegistration? = null

    private val _verificar_seccion_tienda =
        MutableStateFlow<Triple<Boolean, String, String?>>(Triple(false, "", null))
    val verificarSeccion = _verificar_seccion_tienda.asStateFlow()


    private val _idSocio = MutableStateFlow("")
    val idSocio = _idSocio.asStateFlow()

    private val _cargandoIdSocio = MutableStateFlow(true)
    val cargandoIdSocio = _cargandoIdSocio.asStateFlow()

    fun cargarIdSocio(context: Context) {
        _cargandoIdSocio.value = true
        Log.d("CargarIdSocio", "Inicio de la función. _cargandoIdSocio = true")

        viewModelScope.launch {
            val inicio = System.currentTimeMillis()
            Log.d("CargarIdSocio", "Marca de tiempo de inicio: $inicio")
            viewModelScope.launch {
                combine(
                    data_store_localidad.get_localidad_tienda_socio(context),
                    data_store_localidad.get_id_socio(context)
                ) { localidad, idSocio -> Pair(localidad, idSocio) }
                    .collect { (localidad, idSocio) ->

                        Log.d("CargarIdSocio", "Valor recibido de DataStore: $idSocio")
                        _idSocio.value = idSocio

                        if (idSocio.isNotEmpty()) {
                            Log.d("CargarIdSocio", "Valor no vacío, se llama a verificar_seccion")
                            verificar_seccion(context, idSocio, localidad)
                        } else {
                            Log.d("CargarIdSocio", "Valor vacío, no se verifica sección")
                        }

                        val tiempoTranscurrido = System.currentTimeMillis() - inicio
                        val faltante = 4000 - tiempoTranscurrido

                        if (faltante > 0) {
                            delay(faltante)
                        }

                        _cargandoIdSocio.value = false
                    }
            }
        }
    }

    /**
     * Para resetear el estado de la pantalla antes de logear
     */
//    fun resetearEstado() {
//        _cargandoIdSocio.value = false
//    }


    fun descontar_puntos(localidad_tienda: String, id_tienda: String, puntos_descuento: Int,meses_agregados:String) {
        viewModelScope.launch {
            try {
                instace_repo.restar_puntos(localidad_tienda, id_tienda, puntos_descuento,meses_agregados)

            } catch (e: Exception) {
                Log.d("Error_canjear", "error al cambiar el cange")
            }
        }
    }

    fun verificar_existencia_tienda(
        id_user: String,
        ingresa_correo: Boolean,
        correo_tienda: String,
        id_tienda: String,
        localidad_tienda: String
    ) {
        viewModelScope.launch {
            try {
                instace_repo.verificar_existencia_tienda(
                    id_user,
                    ingresa_correo,
                    correo_tienda,
                    id_tienda,
                    localidad_tienda
                ) { existe, msje, idConfirmado ->
                    _verificar_seccion_tienda.value = Triple(existe, msje, idConfirmado)
                }
            } catch (e: Exception) {
                _verificar_seccion_tienda.value = Triple(false, "Error al verificar tu id", null)
            }
        }
    }

    fun cambiar_estado_Seccion() {
        _verificar_seccion_tienda.value = Triple(false, "", null)
    }

    fun verificar_seccion(context: Context, id_tienda: String, localidad_tienda: String) {

        listenerDatosTienda?.remove()

        _state_eres_socio.value = carga_acces_socio.loading

        listenerDatosTienda = instace_repo.escuchar_datos_tienda(
            localidad_tienda,
            id_tienda,
            resultado = { datos ->
                viewModelScope.launch {
                    if (datos.nombre.isNotEmpty()) {
                        _state_eres_socio.value = carga_acces_socio.succes(datos)
                    } else {
                        _state_eres_socio.value = carga_acces_socio.error("No se encontraron datos")
                    }
                }
            },
            error = { e ->
                viewModelScope.launch {

                    _state_eres_socio.value =
                        carga_acces_socio.error("Error: ${e.message}")
                }
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
        object idle : carga_acces_socio()
        data class succes(val datos: datos_tienda) : carga_acces_socio()
        data class error(val txt: String) : carga_acces_socio()
        object loading : carga_acces_socio()
    }

}