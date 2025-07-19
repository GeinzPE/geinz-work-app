package com.geinzz.geinzwork


import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.geinzz.geinzwork.problemas_soporte_politicas.reportes_users
import com.geinzz.geinzwork.servicios_geinz.serviciosGeinz
import com.geinzz.geinzwork.vistaTiendas.verServicios
import com.geinzz.geinzwork.vistaTiendas.vistaProductosGeneralTiendas
import com.geinzz.geinzwork.vistaTrabajador.promocionesvista
import com.geinzz.geinzwork.vistaTrabajador.ver_detalles_Promociones
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class MyFirebaseMessagingService : FirebaseMessagingService() {
    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        // Manejo de datos
        if (message.data.isNotEmpty()) {
            handleDataMessage(message.data)
        }
        message.notification?.let {
            sendNotification(it, message.data)
        }
    }

    private fun handleDataMessage(data: Map<String, String>) {
        val storyId = data["story_id"]

    }

    private fun sendNotification(
        notification: RemoteMessage.Notification,
        data: Map<String, String>,
    ) {
        val title = notification.title ?: "Título por defecto"
        val body = notification.body ?: "Cuerpo por defecto"
        val clickAction = data["click_action"]
        val idAnuncio = data["idProductoClikado"] ?: data["idAnuncio"] ?: data["idServicio"]
        val idTienda = data["idTienda"]
        val entrada = data["entrada"]

        val intent = Intent().apply {
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
            putExtra("idAnuncio", idAnuncio)
            putExtra("idTienda", idTienda)
            putExtra("entrada", entrada)

            Log.d(
                "notificaicones encontrada",
                "los valoes obtenidos son $clickAction , $idAnuncio,$idTienda,$entrada"
            )

            when (clickAction) {
                "SUBIDA_NOTICIAS" -> {
                    setClass(this@MyFirebaseMessagingService, ver_detalles_Promociones::class.java)
                    putExtra("idAnuncio", idAnuncio)
                    putExtra("idTienda", idTienda)
                    putExtra("entrada", entrada)
                }

                "SERVICIOS_TIENDAS" -> {
                    setClass(this@MyFirebaseMessagingService, verServicios::class.java)
                    putExtra("idServicio", idAnuncio)
                    putExtra("idTienda", idTienda)
                }

                "PROMO_SUGERIDAS" -> {
                    setClass(this@MyFirebaseMessagingService, promocionesvista::class.java)
                    putExtra("idProductoClikado", idAnuncio)
                    putExtra("idTienda", idTienda)
                }

                "PRODUCTOS_PRINCIPALES" -> {
                    setClass(this@MyFirebaseMessagingService, vistaProductosGeneralTiendas::class.java)
                    putExtra("idProductoClikado", idAnuncio)
                    putExtra("idTienda", idTienda)
                }

                "REPORTE" -> {
                    setClass(this@MyFirebaseMessagingService, reportes_users::class.java)
                }

                "SERVICIOS"->{
                    setClass(this@MyFirebaseMessagingService,serviciosGeinz::class.java)
                }

                else -> {
                    setClass(this@MyFirebaseMessagingService, MainActivity::class.java)
                }
            }
        }

        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
        )

        val channelId = getString(R.string.notificacion_chanel_id_defaul)
        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.logo_geinz_circular) // Reemplaza con tu icono
            .setContentTitle(title)
            .setContentText(body)
            .setAutoCancel(true)
            .setSound(defaultSoundUri)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_HIGH)

//        val TrackPendingIntent=PendingIntent.getActivity(this,System.currentTimeMillis().toInt(),intent,PendingIntent.FLAG_IMMUTABLE)
//        val action=NotificationCompat.Action.Builder(R.drawable.logo_geinz_circular,"Ver Ahora",TrackPendingIntent).build()
//        notificationBuilder.addAction(action)

        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                getString(R.string.notificacion_chanel_name_defaul),
                NotificationManager.IMPORTANCE_HIGH
            )
            notificationManager.createNotificationChannel(channel)
        }

        notificationManager.notify(0, notificationBuilder.build())
    }
}
