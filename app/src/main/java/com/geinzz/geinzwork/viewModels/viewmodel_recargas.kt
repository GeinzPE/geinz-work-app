package com.geinzz.geinzwork.viewModels

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.geinzz.geinzwork.data.model.EstadoNotificaciones
import com.geinzz.geinzwork.data.model.historial_descuento
import com.geinzz.geinzwork.data.model.historial_financiero
import com.geinzz.geinzwork.data.model.historial_recargas
import com.geinzz.geinzwork.data.model.recargar_monedas_tienda
import com.geinzz.geinzwork.model.repo_recargas
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.notificacionesFCM.enviar_notificacion_lista_dispo
import com.google.firebase.functions.FirebaseFunctions
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.WeekFields
import java.util.Locale
import java.util.UUID

class viewmodel_recargas : ViewModel() {
    val insta_repo = repo_recargas()

    private val _stateHistorial =
        MutableStateFlow<state_historial_financiero>(
            state_historial_financiero.Idle
        )

    val stateHistorial: StateFlow<state_historial_financiero> =
        _stateHistorial.asStateFlow()

    @RequiresApi(Build.VERSION_CODES.O)
    private val formatterFecha = DateTimeFormatter.ofPattern("dd/MM/yyyy")

    private val _saldo = MutableStateFlow(0)
    val saldo: StateFlow<Int> = _saldo

    private val _estadoNotificaciones =
        MutableStateFlow(EstadoNotificaciones(restantes = 3))

    val estadoNotificaciones: StateFlow<EstadoNotificaciones> =
        _estadoNotificaciones
    private val _enviar_webhook_culqui = MutableStateFlow("")
    // Inmutable para exponer
    val enviar_webhook_culqui: StateFlow<String> = _enviar_webhook_culqui


    fun crear_cargo__compra_paquete(
        localidad:String,
        id_cliente: String,
        cantidad: String,
        monto: String,
    ) {

        viewModelScope.launch {
            try {
                insta_repo.crear_cargo_compras_paquetes(localidad,id_cliente, cantidad, monto, { url ->
                    _enviar_webhook_culqui.value = url
                })
            } catch (e: Exception) {
                _enviar_webhook_culqui.value = "$e"
            }
        }
    }




    @RequiresApi(Build.VERSION_CODES.O)
    fun historialFechaToLocalDate(fecha: String): LocalDate? {
        return runCatching {
            LocalDate.parse(fecha, formatterFecha)
        }.getOrNull()
    }


    @RequiresApi(Build.VERSION_CODES.O)
    fun recargar_puntos(i: historial_recargas, id_user: String) {
        viewModelScope.launch {
            try {
                val datos_recarga =
                    recargar_monedas_tienda(i.id_tienda, i.localidad_tienda, i.monto)
                val recargado = insta_repo.recargar_monedas(datos_recarga)
                if (recargado) {
                    insta_repo.guardarHistorial(i)
                    enviar_notificacion_lista_dispo(
                        id_promo = "",
                        id_tienda = "", localidad = "", categora_tienda = "",
                        tipo_notificacion_params = "screen",
                        id_users = listOf(id_user),
                        titulo = "¡Recarga completada! \uD83C\uDF89",
                        txt = "\uD83D\uDC4B Hola ${i.nombre_tienda}  Tu recarga de ${i.monto}\uD83E\uDE99 fue procesada correctamente.",
                        logo_tienda = "https://firebasestorage.googleapis.com/v0/b/geinzworkapp.appspot.com/o/logo_geinz_webp.webp?alt=media&token=aa1ef1df-1bcd-48f2-9cad-a85929c3a8d0",
                        tipo_notificacion = "Basico",
                        url_img = "",
                        prioridad = "high"
                    )
                }
            } catch (e: Exception) {
                Log.e("error", "Error al procesar la recarga: ${e.message}")
            }
        }
    }

    fun agregarPagoTienda(
        idTienda: String,
        nombreUser: String,
        planSelect: String,
        localdiad: String,
        saldoTienda: Int,
        categoriaTienda: String,
        logoTienda: String,
        nombrePlan: String,
        precio_soles:Int,
        onResult: (String?, Boolean) -> Unit
    ) {
        val functions = FirebaseFunctions.getInstance()
        val data = hashMapOf(
            "id_tienda" to idTienda,
            "nombre_user" to nombreUser,
            "plan_select" to planSelect,
            "localdiad" to localdiad,
            "saldo_tienda" to saldoTienda,
            "categoira_tienda" to categoriaTienda,
            "logo_tienda" to logoTienda,
            "nombre_plan" to nombrePlan,
            "monto_pagar_de_plan" to precio_soles
        )

        functions
            .getHttpsCallable("agregar_pago_para_el_usuario_tienda")
            .call(data)
            .addOnSuccessListener { result ->

                val dataResult = result.data as Map<*, *>

                val idPago = dataResult["id_pago"]?.toString()
                val reutilizado = dataResult["reutilizado"] == true

                println("✅ ID PAGO: $idPago")
                println("♻️ Reutilizado: $reutilizado")

                onResult(idPago, reutilizado)
            }
            .addOnFailureListener { e ->
                println("❌ ERROR AL CREAR PAGO: ${e.message}")
                onResult(null, false)
            }
    }
    fun obtner_saldo_actual_reactivo(id_tienda: String,localidad: String){
        viewModelScope.launch {
            try {
                insta_repo.obtner_saldo_reactivo(id_tienda, localidad)
                    .collect { nuevoSaldo ->
                        _saldo.value = nuevoSaldo
                    }
            }catch (e: Exception){
                Log.e("error", "Error al obtner el saldo: ${e.message}")
            }
        }
    }

    fun obtner_estado_notificaciones(id_tienda: String, localidad: String) {
        viewModelScope.launch {
            try {
                insta_repo
                    .obtner_estado_notificaciones_reactivos(id_tienda, localidad)
                    .collect { estadoNoti ->
                        _estadoNotificaciones.value = estadoNoti
                    }
            } catch (e: Exception) {
                Log.e(
                    "error",
                    "Error al obtener el estado de notificaciones: ${e.message}"
                )
            }
        }
    }

    fun restar_puntos_recarga(
        i: historial_descuento,
        monto_descontar: String,
        id_tienda: String,
        localidad: String
    ) {
        viewModelScope.launch {
            try {
                val restar_puntos =
                    insta_repo.descontar_puntos_uso(monto_descontar, id_tienda, localidad)
                if (restar_puntos) {
                    insta_repo.guardar_historial_descuento(i)
                }
            } catch (e: Exception) {
                Log.e("error", "Error al procesar la el descuento: ${e.message}")
            }
        }
    }

    fun obtner_historial_pagos_tienda(
        id_tienda: String,
        localidad: String
    ) {
        viewModelScope.launch {
            _stateHistorial.value = state_historial_financiero.Loading

            try {
                val resultado =
                    insta_repo.obtner_historial(id_tienda, localidad)

                _stateHistorial.value =
                    state_historial_financiero.Success(resultado)

            } catch (e: Exception) {
                _stateHistorial.value =
                    state_historial_financiero.Error(
                        e.message ?: "Error al obtener historial"
                    )
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun filtrarHistorial(
        lista: List<historial_financiero>,
        filtro: String
    ): List<historial_financiero> {

        val hoy = LocalDate.now()
        val weekFields = WeekFields.of(Locale.getDefault())

        val listaFiltrada = when (filtro) {

            "Todos" -> lista

            "Hoy" -> lista.filter {
                historialFechaToLocalDate(it.fecha) == hoy
            }

            "Esta semana" -> lista.filter {
                val fecha = historialFechaToLocalDate(it.fecha) ?: return@filter false

                val lunes = hoy.with(weekFields.dayOfWeek(), 1L)
                val domingo = hoy.with(weekFields.dayOfWeek(), 7L)

                fecha in lunes..domingo
            }

            "Este mes" -> lista.filter {
                val fecha = historialFechaToLocalDate(it.fecha) ?: return@filter false
                fecha.month == hoy.month && fecha.year == hoy.year
            }

            "Generacion con IA" -> lista.filter {
                it.tipo_transaccion.contains("GEN IA", ignoreCase = true)
            }

            "Notificaciones" -> lista.filter {
                it.tipo_transaccion.contains("envio de notificaciones", ignoreCase = true)
            }

            "Publicaciones" -> lista.filter {
                it.tipo_transaccion.contains("PUBLIC", ignoreCase = true)
            }

            "Recargas" -> lista.filter {
                it.tipo_transaccion.contains("PAQUETE", ignoreCase = true)
            }

            else -> lista
        }

        // 🔥 ORDENAR SIEMPRE: más reciente primero
        return listaFiltrada.sortedByDescending { it.dateTime }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun agruparHistorialPorFecha(
        lista: List<historial_financiero>
    ): Map<String, List<historial_financiero>> {

        return lista
            .groupBy { it.fecha }
            .toSortedMap(compareByDescending { fechaStr ->
                // Convertimos string a LocalDate para ordenar correctamente
                historialFechaToLocalDate(fechaStr) ?: LocalDate.MIN
            })
    }





    sealed class state_historial_financiero {
        object Idle : state_historial_financiero()
        object Loading : state_historial_financiero()
        data class Success(
            val lista: List<historial_financiero>
        ) : state_historial_financiero()

        data class Error(
            val mensaje: String
        ) : state_historial_financiero()
    }


}