package com.geinzz.geinzwork.viewModels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.geinzz.geinzwork.data.model.NotificacionIA
import com.geinzz.geinzwork.data.model.NotificacionIA_dialog
import com.geinzz.geinzwork.data.model.datos_gen_IA_Tiendas
import com.geinzz.geinzwork.data.model.dialog_generaciones_IA_promo_noti
import com.geinzz.geinzwork.data.model.historial_descuento
import com.geinzz.geinzwork.model.repo_generaciones_IA
import com.geinzz.geinzwork.model.repo_pantallas_promocionar
import com.geinzz.geinzwork.utils.constantes.constantes.mostrarFechaDialog_horaDialog.obtenerFechaActual
import com.geinzz.geinzwork.utils.constantes.constantes.mostrarFechaDialog_horaDialog.obtenerHoraActual
import com.geinzz.geinzwork.utils.constantes.constantes_cobro_monedas
import com.geinzz.geinzwork.viewModels.viewmodel_pantallas_promocionar.EstadoIA
import com.geinzz.geinzwork.viewModels.viewmodel_pantallas_promocionar.EstadoIA_notifi_corta
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch

class viewmodel_generaciones_IA : ViewModel() {
    val insta_repo = repo_generaciones_IA()
    val viewmodel_recargas = viewmodel_recargas()


    private val _estado_generaciones_IA =
        MutableStateFlow<EstadoGeneracionesIA>(EstadoGeneracionesIA.Idle)

    val estado_generaciones_IA: StateFlow<EstadoGeneracionesIA> =
        _estado_generaciones_IA


    private val _estado_promociones_ia =
        MutableStateFlow<EstadoIA_dialog_centrado>(EstadoIA_dialog_centrado.Idle)

    val estado_promociones_ia: StateFlow<EstadoIA_dialog_centrado> =
        _estado_promociones_ia

    private val _estado_notificacion_con_ia_corta =
        MutableStateFlow<EstadoIA_dialog_centrado_notificaciones>(EstadoIA_dialog_centrado_notificaciones.Idle)

    val estado_notificaion_con_ia_corta: StateFlow<EstadoIA_dialog_centrado_notificaciones> =
        _estado_notificacion_con_ia_corta


    fun obtner_generaciones_IA(localida: String, id_tienda: String) {
        viewModelScope.launch {

            insta_repo
                .obtener_generaciones_IA_realtime(id_tienda, localida)
                .onStart {
                    _estado_generaciones_IA.value = EstadoGeneracionesIA.Loading
                }
                .catch {
                    _estado_generaciones_IA.value =
                        EstadoGeneracionesIA.Error("Error al obtener generaciones")
                }
                .collect { lista ->

                    _estado_generaciones_IA.value =
                        if (lista.isNotEmpty()) {
                            EstadoGeneracionesIA.Success(lista)
                        } else {
                            EstadoGeneracionesIA.Empty("No se encontraron generaciones")
                        }
                }
        }
    }

    fun agregar_nueva_generacion_remasterizada(
        titulo_anterior:String,
        descripcion_anteriro: String,
        id_tienda: String,
        localidad: String,
        titulo_nuevo: String,
        texto_nuevo: String,
        id_generacion: String
    ) {
        viewModelScope.launch {
            try {
                insta_repo.agregar_nuevas_generaciones(
                    titulo_anterior,descripcion_anteriro,
                    id_tienda,
                    localidad,
                    titulo_nuevo,
                    texto_nuevo,
                    id_generacion
                )
            } catch (
                e: Exception
            ) {
                Log.d("agregamos_campos", "$e")
            }
        }
    }

    fun mejorar_texto_con_promo_IA(
        id_promo_noti_gen:String,
        tipo_generacion: repo_pantallas_promocionar.TipoGeneracionIA,
        saldo_tienda: Int,
        localidad_tienda: String,
        id_tienda: String,
        nombre_tienda: String,
        tituloUsuario: String,
        descripcionUsuario: String,
        nombreTienda: String,
        localidad: String,
        total_cobrar: String,
        titulo_generacion_historial: String
    ) {
        viewModelScope.launch {

            _estado_promociones_ia.value = EstadoIA_dialog_centrado.Loading

            try {
                if (saldo_tienda < 30) {
                    _estado_promociones_ia.value =
                        EstadoIA_dialog_centrado.Error("Saldo insuficiente")
                    return@launch
                }

                val resultado = insta_repo.generar_promocion_con_IA(
                    id_promo_noti_gen = id_promo_noti_gen,
                    tipo_generacion = tipo_generacion,
                    tituloUsuario = tituloUsuario,
                    descripcionUsuario = descripcionUsuario,
                    nombreTienda = nombreTienda,
                    localidad = localidad
                )

                if (resultado != null) {

                    _estado_promociones_ia.value =
                        EstadoIA_dialog_centrado.Success(resultado)

                    val historial = historial_descuento(
                        tipo_transaccion = "descuento",
                        fecha = obtenerFechaActual(),
                        hora = obtenerHoraActual(),
                        id_recarga = constantes_cobro_monedas.generarIdRecarga(),
                        localidad_tienda = localidad_tienda,
                        id_tienda = id_tienda,
                        nombre_tienda = nombre_tienda,
                        monto_descuento = total_cobrar,
                        tipo = titulo_generacion_historial,
                        precio_soles = constantes_cobro_monedas
                            .calcular_precio_soles(total_cobrar)
                            .toString(),
                        estado = "Aceptado",
                        monto_restante = saldo_tienda - total_cobrar.toInt()
                    )

                    viewmodel_recargas.restar_puntos_recarga(
                        historial,
                        total_cobrar,
                        id_tienda,
                        localidad_tienda
                    )

                } else {
                    _estado_promociones_ia.value =
                        EstadoIA_dialog_centrado.Error("No se pudo generar contenido")
                }

            } catch (e: Exception) {
                _estado_promociones_ia.value =
                    EstadoIA_dialog_centrado.Error("Error al generar con IA")
            }
        }
    }

    fun limpiar_Estado_nueva_generacion(){
        _estado_promociones_ia.value=EstadoIA_dialog_centrado.Idle
    }


    fun mejorar_mejorar_notificacion_con_IA_corta(
        id_notificacion_promo:String,
        tipo_select_IA: String,
        tipoSeleccionado: repo_pantallas_promocionar.TipoGeneracionIA,
        saldo_tienda: Int, localidad_tienda: String, id_tienda: String,
        nombre_tienda: String,
        titulo_publicacion: String,
        descripcion: String
    ) {
        Log.d("titulo_publicacion", "$titulo_publicacion $descripcion")
        viewModelScope.launch {

            _estado_notificacion_con_ia_corta.value =
                EstadoIA_dialog_centrado_notificaciones.Loading

            try {
                if (saldo_tienda < 20) {
                    _estado_notificacion_con_ia_corta.value =
                        EstadoIA_dialog_centrado_notificaciones.Error("saldo insuficiente")
                    return@launch
                }
                insta_repo.crear_notificacion_conIA_corta(
                    id_notificacion_promo,
                    titulo_publicacion,
                    descripcion, tipoSeleccionado,
                ) { notificacionIA ->
                    _estado_notificacion_con_ia_corta.value =
                        EstadoIA_dialog_centrado_notificaciones.Success(notificacionIA)
                    if (notificacionIA.titulo.isNotEmpty() && notificacionIA.descripcion.isNotEmpty()) {
                        val historial_descuento = historial_descuento(
                            tipo_transaccion = "descuento",
                            fecha = obtenerFechaActual(),
                            hora = obtenerHoraActual(),
                            id_recarga = constantes_cobro_monedas.generarIdRecarga(),
                            localidad_tienda = localidad_tienda,
                            id_tienda = id_tienda,
                            nombre_tienda = nombre_tienda,
                            monto_descuento = "15",
                            tipo = tipo_select_IA,
                            precio_soles = constantes_cobro_monedas.calcular_precio_soles("15")
                                .toString(), estado = "Aceptado", monto_restante = saldo_tienda - 15
                        )
                        viewmodel_recargas.restar_puntos_recarga(
                            historial_descuento,
                            "15",
                            id_tienda,
                            localidad_tienda
                        )
                    }
                }

            } catch (e: Exception) {
                _estado_notificacion_con_ia_corta.value =
                    EstadoIA_dialog_centrado_notificaciones.Error(
                        e.message ?: "Error al generar la notificación con IA"
                    )
            }
        }
    }

    fun resetear_Estado_notificacion_enviadad() {
        _estado_notificacion_con_ia_corta.value = EstadoIA_dialog_centrado_notificaciones.Idle
    }

    sealed class EstadoIA_dialog_centrado {
        object Idle : EstadoIA_dialog_centrado()
        object Loading : EstadoIA_dialog_centrado()
        data class Success(val generacion: dialog_generaciones_IA_promo_noti) :
            EstadoIA_dialog_centrado()

        data class Error(val mensaje: String) : EstadoIA_dialog_centrado()
    }


    sealed class EstadoIA_dialog_centrado_notificaciones {
        object Idle : EstadoIA_dialog_centrado_notificaciones()
        object Loading : EstadoIA_dialog_centrado_notificaciones()
        data class Success(val txt_descripcion: NotificacionIA_dialog) : EstadoIA_dialog_centrado_notificaciones()
        data class Error(val mensaje: String) : EstadoIA_dialog_centrado_notificaciones()
    }



    sealed class EstadoGeneracionesIA {
        object Idle : EstadoGeneracionesIA()
        object Loading : EstadoGeneracionesIA()
        data class Success(
            val data: List<datos_gen_IA_Tiendas>
        ) : EstadoGeneracionesIA()

        data class Error(
            val message: String
        ) : EstadoGeneracionesIA()

        data class Empty(
            val message: String
        ) : EstadoGeneracionesIA()
    }


}