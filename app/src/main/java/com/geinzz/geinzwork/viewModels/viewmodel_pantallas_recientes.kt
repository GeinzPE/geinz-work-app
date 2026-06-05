package com.geinzz.geinzwork.viewModels

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.geinzz.geinzwork.data.model.notificaciones
import com.geinzz.geinzwork.data.model.obtener_datos_promociones
import com.geinzz.geinzwork.data.model.publicaciones_notificaciones_geinz
import com.geinzz.geinzwork.model.repo_pantalla_recientes
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.text.Normalizer

class viewmodel_pantallas_recientes : ViewModel() {

    val instarepo = repo_pantalla_recientes()

    private val _estadoPromoNoti = MutableStateFlow<EstadoPromoNoti>(EstadoPromoNoti.Vacío)
    val estadoPromoNoti: StateFlow<EstadoPromoNoti> = _estadoPromoNoti

    private val _estadoPromocion =
        MutableStateFlow<EstadoDatosPromocion>(EstadoDatosPromocion.Loading)
    val estadoPromocion: StateFlow<EstadoDatosPromocion> = _estadoPromocion



    private val _estado_notificacion=MutableStateFlow<EstadoDatosNotificacion>(
        EstadoDatosNotificacion.Loading)
    val estado_notificacion: StateFlow<EstadoDatosNotificacion> = _estado_notificacion


    private val _estado_promociones = MutableStateFlow<Map<String, String>>(emptyMap())
    val estado_promociones: StateFlow<Map<String, String>> = _estado_promociones




    fun obtner_noti_promo(id_tienda: String, localidad: String) {
        viewModelScope.launch {
            try {
                _estadoPromoNoti.value = EstadoPromoNoti.Cargando
                val lista = instarepo.obtener_notificacion_publicaciones(id_tienda, localidad)

                _estadoPromoNoti.value = if (lista.isEmpty()) {
                    EstadoPromoNoti.Vacío
                } else {

                    EstadoPromoNoti.Success(lista)
                }
            } catch (e: Exception) {
                _estadoPromoNoti.value = EstadoPromoNoti.Error(e.message ?: "Error desconocido")
            }
        }
    }

    fun obtener_estadotiempo_real_promociones(idTienda: String, localidad: String) {
        instarepo.escucharEstadosTodasPromociones(idTienda, localidad) { estadosTodos ->
            // Reemplazamos todo el mapa de estados
            _estado_promociones.value = estadosTodos
        }
    }




    fun cargarDatosPromocion( precio_hora:Int,precio_dias:Int,idTienda: String, localidad: String, idPromo: String) {
        viewModelScope.launch {
            _estadoPromocion.value = EstadoDatosPromocion.Loading
            try {
                val datos = instarepo.obtenerDatosPromocion(precio_hora,precio_dias,idTienda, localidad, idPromo)
                if (datos != null) {
                    _estadoPromocion.value = EstadoDatosPromocion.Success(datos)
                } else {
                    _estadoPromocion.value = EstadoDatosPromocion.Error("No se encontraron datos")
                }
            } catch (e: Exception) {
                _estadoPromocion.value =
                    EstadoDatosPromocion.Error(e.message ?: "Error desconocido")
            }
        }
    }


    fun cargar_datos_notificacion(idTienda: String, localidad: String, idPromo: String){
        viewModelScope.launch {
                _estado_notificacion.value= EstadoDatosNotificacion.Loading
            try {
                val datos=instarepo.obtenerNotificacionCompleta(localidad,idTienda,idPromo)
                if (datos != null) {
                _estado_notificacion.value=EstadoDatosNotificacion.Success(datos)
                }else {
                    _estado_notificacion.value = EstadoDatosNotificacion.Error("No se encontraron datos")
                }

            }catch (e:Exception){
                _estado_notificacion.value =
                    EstadoDatosNotificacion.Error(e.message ?: "Error desconocido")
            }
        }
    }


    fun cambiar_estado_promociones(
        id_tienda: String,
        localidad: String,
        id_promo: String,
        estado_cambiado: String
    ) {
        viewModelScope.launch {
            try {
                instarepo.cambiar_estado_publicacion(
                    id_tienda,
                    localidad,
                    id_promo,
                    estado_cambiado
                )
            } catch (e: Exception) {
                Log.d("error_cambiar", "error al cambiar estado de publicaion")
            }
        }
    }


    fun esVencido(vence: String): Boolean {
        return vence.equals("Expirado")
    }

    fun esPorVencer(vence: String): Boolean {
        val horas = convertirAVenceEnHoras(vence)
        return horas > 0 && horas <= 12
    }

    fun en_pausa(estado: String): Boolean {
        return estado.equals("pausado", ignoreCase = true)
    }

    fun esActivo(vence: String): Boolean {
        return !esVencido(vence) && !esPorVencer(vence) && !vence.equals("")
    }

    fun convertirAVenceEnHoras(vence: String): Double {
        val texto = Normalizer
            .normalize(vence.lowercase(), Normalizer.Form.NFD)
            .replace("\\p{Mn}+".toRegex(), "") // elimina tildes

        val partes = texto.split(" ")

        if (partes.size < 2) return Double.MAX_VALUE

        val valor = partes[0].toDoubleOrNull() ?: return Double.MAX_VALUE
        val unidad = partes[1]

        return when {
            unidad.startsWith("dia") -> valor * 24
            unidad.startsWith("hora") -> valor
            unidad.startsWith("minuto") -> valor / 60
            else -> Double.MAX_VALUE
        }
    }

    sealed class EstadoDatosPromocion {
        object Loading : EstadoDatosPromocion()
        data class Success(val datos: obtener_datos_promociones) : EstadoDatosPromocion()
        data class Error(val mensaje: String) : EstadoDatosPromocion()
    }


    sealed class EstadoDatosNotificacion {
        object Loading : EstadoDatosNotificacion()
        data class Success(val datos: notificaciones) : EstadoDatosNotificacion()
        data class Error(val mensaje: String) : EstadoDatosNotificacion()
    }



    sealed class EstadoPromoNoti {
        object Cargando : EstadoPromoNoti()
        data class Success(val lista: List<publicaciones_notificaciones_geinz>) : EstadoPromoNoti()
        data class Error(val mensaje: String) : EstadoPromoNoti()
        object Vacío : EstadoPromoNoti()
    }

}