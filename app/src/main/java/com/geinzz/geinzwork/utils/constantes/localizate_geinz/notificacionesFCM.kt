package com.geinzz.geinzwork.utils.constantes.localizate_geinz

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.geinzz.geinzwork.NotificacionRS
import com.geinzz.geinzwork.model.repo_eres_socio
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.URLEncoder

object notificacionesFCM {
    data class ResultadoEnvioNotificacion(
        val enviadosCorrectos: Int,
        val enviadosFallidos: Int
    )

    val insta_repo_socio= repo_eres_socio()


    suspend fun enviar_notificacion_lista_dispo(
        id_promo:String,
        id_tienda: String,
        localidad: String,
        categora_tienda: String,
        tipo_notificacion_params: String,
        id_users: List<String>,
        titulo: String,
        txt: String,
        logo_tienda: String,
        tipo_notificacion: String,
        url_img: String,
        prioridad: String = "high",
    ): ResultadoEnvioNotificacion = withContext(Dispatchers.IO) {

        val tokensExitosos = mutableListOf<String>()
        val tokensFallidos = mutableListOf<String>()

        val link = when (tipo_notificacion_params) {
            "informativas" ->
                "https://geinzworkapp.web.app/share?t=to&id=$id_tienda&l=$localidad&c=${
                    URLEncoder.encode(categora_tienda, "UTF-8")
                }&pi=$id_promo"

            "promociones y ofertas" ->
                "https://geinzworkapp.web.app/share?t=prn&id=$id_tienda&l=$localidad&c=${
                    URLEncoder.encode(categora_tienda, "UTF-8")
                }&pi=$id_promo"

            "screen"-> {
                   "https://geinzworkapp.web.app/share?t=scr&id=$categora_tienda"
            }

            else -> {
                ""
            }
        }

        val tipo_notificacion_string = when (tipo_notificacion) {
            "Basico" -> "logo"
            "Avanzado" -> "imagen"
            "Premium" -> "premium"
            else -> "basico"
        }

        val notificacion = NotificacionRS()

        id_users.forEach { id_user ->
            val lista_tokens = insta_repo_socio.obtener_lista_tokens(id_user)

            lista_tokens.forEach { (dispositivo, token) ->

                val exito = notificacion.enviarNotificacionFCM_LINK_SUSPEND(
                    id_user = id_user,
                    token = token,
                    titulo = titulo,
                    cuerpo = txt,
                    link = link,
                    tipoNotificacion = tipo_notificacion_string,
                    urlLogo = logo_tienda,
                    urlImagen = url_img,
                    prioridad = prioridad
                )

                if (exito) {
                    tokensExitosos.add(token)
                } else {
                    tokensFallidos.add(dispositivo)
                }
            }

            if (tokensFallidos.isNotEmpty()) {
                notificacion.eliminar_tokens_usuario(id_user, tokensFallidos)
            }
        }

        ResultadoEnvioNotificacion(
            enviadosCorrectos = tokensExitosos.size,
            enviadosFallidos = tokensFallidos.size
        )
    }

}