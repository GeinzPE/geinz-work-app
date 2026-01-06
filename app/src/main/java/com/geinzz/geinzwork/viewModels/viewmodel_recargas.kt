package com.geinzz.geinzwork.viewModels

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.geinzz.geinzwork.data.model.historial_descuento
import com.geinzz.geinzwork.data.model.historial_recargas
import com.geinzz.geinzwork.data.model.recargar_monedas_tienda
import com.geinzz.geinzwork.model.repo_recargas
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.notificacionesFCM.enviar_notificacion_lista_dispo
import kotlinx.coroutines.launch
import java.util.UUID

class viewmodel_recargas : ViewModel() {
    val insta_repo = repo_recargas()

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


    fun generarIdRecarga(): String {
        return UUID.randomUUID().toString()
    }

    fun calcular_precio_soles(monedas_gasto: String): Double {
        val monedas = monedas_gasto.toDoubleOrNull() ?: 0.0
        return monedas / 100
    }


}