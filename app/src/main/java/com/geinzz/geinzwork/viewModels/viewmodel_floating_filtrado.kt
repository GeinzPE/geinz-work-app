package com.geinzz.geinzwork.viewModels

import android.content.Context
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.geinzz.geinzwork.data_store.data_store_localidad
import com.geinzz.geinzwork.data_store.data_store_localidad.guardar_hasgin_lat_lon_user
import com.geinzz.geinzwork.data_store.data_store_localidad.guardar_lat_log_user
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.constantes_lista_localidades.geohashing
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.constantes_lista_localidades.isGpsActivo
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.constantes_lista_localidades.obtenerUbicacionEnTiempoReal
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.constantes_lista_localidades.obtenerUbicacionReal
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.constantes_lista_localidades.obtenerZonaActual
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.ln

class viewmodel_floating_filtrado : ViewModel() {

    private val _carga_cordenadas_nuevas =
        MutableStateFlow<carga_cordenadas>(carga_cordenadas.inicial)
    val carga_cordenadas_nuevas = _carga_cordenadas_nuevas.asStateFlow()

    private val _gpsActivo = MutableStateFlow(false)
    val gpsActivo = _gpsActivo.asStateFlow()

    private val _cerca_de_ti_enable = MutableStateFlow(false)
    val cerca_de_ti_enable = _cerca_de_ti_enable.asStateFlow()


    fun gps_activo(enable_desable: Boolean){
        _gpsActivo.value=enable_desable
    }
    fun save_cerca_de_ti(valor: Boolean) {
        _cerca_de_ti_enable.value = valor
    }

    fun limpiar_valor_save_cerca_de_ti() {
        _cerca_de_ti_enable.value = false
    }


    private var jobVerificacionGPS: Job? = null

    fun iniciarVerificacionGPS(context: Context) {
        if (jobVerificacionGPS?.isActive == true) return

        jobVerificacionGPS = viewModelScope.launch {
            while (isActive) {
                _gpsActivo.value = isGpsActivo(context)
                delay(5000)
            }
        }
    }

    fun reiniciarEstadoCoordenadas() {
        _carga_cordenadas_nuevas.value = carga_cordenadas.inicial
    }


    fun detenerVerificacionGPS() {
        jobVerificacionGPS?.cancel()
        jobVerificacionGPS = null
    }
    fun colocar_gps_false(){
        _carga_cordenadas_nuevas.value =
            carga_cordenadas.error("El GPS está desactivado. Enciéndelo para continuar y Intenta nuevamente.")
    }

    fun obtener_nuevas_cordenadas(context: Context) {
        Log.d("qewqeqeqeeqewq", "obtener_nuevas_cordenadas")
        if (!_gpsActivo.value) {
            _carga_cordenadas_nuevas.value =
                carga_cordenadas.error("El GPS está desactivado. Enciéndelo para continuar y Intenta nuevamente.")
            return
        }
        _carga_cordenadas_nuevas.value = carga_cordenadas.loading
        viewModelScope.launch {
            try {
                obtenerUbicacionEnTiempoReal(_gpsActivo.value,context = context, { lat, lng ->
                    Log.d("asdasFSGIDNSHGUIB", "$lat ${lng}")
                    val geohasing = geohashing(lat, lng)
                    val hora = obtener_hora_actual_formato_12h()
                    viewModelScope.launch {
                        guardar_hasgin_lat_lon_user(context, geohasing, hora)
                        guardar_lat_log_user(context, lat, lng)
                    }
                    val zona_actual = obtenerZonaActual(lat, lng)
                    _carga_cordenadas_nuevas.value =
                        carga_cordenadas.succes(zona_actual, hora, geohasing)

                },{
                    _carga_cordenadas_nuevas.value =
                        carga_cordenadas.error("No se pudo obtener la ubicación. Intentalo nuevamente.")
                }
                )
            } catch (e: Exception) {
                _carga_cordenadas_nuevas.value =
                    carga_cordenadas.error("Error al obtener coordenadas: ${e.message}")
            }
        }
    }

    fun obtener_hora_actual_formato_12h(): String {
        val hora =
            SimpleDateFormat(
                "hh:mm a",
                Locale.getDefault()
            ).format(Date())
        return hora
    }

    sealed class carga_cordenadas() {
        data class succes(val localidad: String, val hora: String, val hashin_user: String) :
            carga_cordenadas()

        object loading : carga_cordenadas()
        data class error(val txt: String) : carga_cordenadas()
        object inicial : carga_cordenadas()
    }
}