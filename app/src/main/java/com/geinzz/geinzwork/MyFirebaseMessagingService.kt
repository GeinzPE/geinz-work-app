package com.geinzz.geinzwork

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.TaskStackBuilder
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresPermission
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import java.net.URL

class MyFirebaseMessagingService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d("FCM", "Nuevo token: $token")
    }


    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)

    override fun onMessageReceived(message: RemoteMessage) {

        val payload = message.data

        val link = payload["link"] ?: return
        val title = payload["title"] ?: "Geinz"
        val body = payload["body"] ?: ""

        val tipo = payload["tipo_notificacion"] ?: "normal"
        val logoUrl = payload["logo"] ?: ""
        val imageUrl = payload["image"] ?: ""

        val intent = Intent(this, MainActivity::class.java).apply {
            action = Intent.ACTION_VIEW
            data = Uri.parse(link)
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }

        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(this, "canal_geinz")
            .setSmallIcon(R.drawable.notification_ic)
            .setContentTitle(title)
            .setContentText(body)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_HIGH)

        // LOGO (para logo y premium)
        if ((tipo == "logo" || tipo == "premium") && logoUrl.isNotEmpty()) {
            cargarBitmapDesdeUrl(logoUrl)?.let { logoBitmap ->
                builder.setLargeIcon(logoBitmap)
            }
        }

        // IMAGEN GRANDE (imagen o premium)
        if ((tipo == "imagen" || tipo == "premium") && imageUrl.isNotEmpty()) {
            cargarBitmapDesdeUrl(imageUrl)?.let { imageBitmap ->
                builder.setStyle(
                    NotificationCompat.BigPictureStyle()
                        .bigPicture(imageBitmap)
                        .bigLargeIcon(null as Bitmap?) // evita duplicar el logo
                )
            }
        } else {
            // Si no hay imagen, usar BigTextStyle para expandir el texto
            builder.setStyle(NotificationCompat.BigTextStyle().bigText(body))
        }

        NotificationManagerCompat.from(this)
            .notify(System.currentTimeMillis().toInt(), builder.build())
    }



    fun cargarBitmapDesdeUrl(url: String): Bitmap? {
        return try {
            BitmapFactory.decodeStream(
                URL(url).openConnection().getInputStream()
            )
        } catch (e: Exception) {
            null
        }
    }



//    override fun onMessageReceived(message: RemoteMessage) {
//        val link = message.data["link"] ?: return
//        val title = message.data["title"] ?: "Geinz"
//        val body = message.data["body"] ?: ""
//        val imageUrl = message.data["image"] ?: ""
//
//        val intent = Intent(this, MainActivity::class.java).apply {
//            action = Intent.ACTION_VIEW
//            data = Uri.parse(link)
//            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
//        }
//
//
//        // ⚡ Back stack correcto
//        val pendingIntent = PendingIntent.getActivity(
//            this,
//            0,
//            intent,
//            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
//        )
//
//        val builder = NotificationCompat.Builder(this, "canal_geinz")
//            .setSmallIcon(R.drawable.notification_ic)
//            .setContentTitle(title)
//            .setContentText(body)
//            .setAutoCancel(true)
//            .setContentIntent(pendingIntent)
//            .setPriority(NotificationCompat.PRIORITY_HIGH)
//
//        // Imagen en notificación si existe
//        // Imagen en notificación si existe
//        if (imageUrl.isNotEmpty()) {
//            try {
//                val bitmap = BitmapFactory.decodeStream(
//                    URL(imageUrl).openConnection().getInputStream()
//                )
//
//                // Logo colapsado
//                builder.setLargeIcon(bitmap)
//
//                // Imagen expandida
//                builder.setStyle(
//                    NotificationCompat.BigPictureStyle()
//                        .bigPicture(bitmap)
//                        .bigLargeIcon(null as Bitmap?)
//                )
//
//            } catch (e: Exception) {
//                e.printStackTrace()
//            }
//        }
//
//
//        // Crear canal si no existe
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            val channel = NotificationChannel(
//                "canal_geinz",
//                "Geinz Notificaciones",
//                NotificationManager.IMPORTANCE_HIGH
//            )
//            channel.description = "Notificaciones generales de Geinz"
//            val manager = getSystemService(NotificationManager::class.java)
//            manager.createNotificationChannel(channel)
//        }
//
//        // ID fijo evita duplicados
//        NotificationManagerCompat.from(this).notify(1000, builder.build())
//    }


//    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
//    override fun onMessageReceived(message: RemoteMessage) {
//
//        val link = message.data["link"] ?: return
//        val title = message.data["title"] ?: "Geinz"
//        val body = message.data["body"] ?: ""
//        val imageUrl = message.data["image"] ?: "" // 👈 NO CAMBIADO
//
//        val intent = Intent(this, MainActivity::class.java).apply {
//            action = Intent.ACTION_VIEW
//            data = Uri.parse(link)
//            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
//        }
//
//        val pendingIntent = PendingIntent.getActivity(
//            this,
//            0,
//            intent,
//            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
//        )
//
//        val builder = NotificationCompat.Builder(this, "canal_geinz")
//            .setSmallIcon(R.drawable.notification_ic)
//            .setContentTitle(title)
//            .setContentText(body)
//            .setAutoCancel(true)
//            .setContentIntent(pendingIntent)
//            .setPriority(NotificationCompat.PRIORITY_HIGH)
//
//        // ✅ LOGO PEQUEÑO USANDO image (SIN BigPictureStyle)
//        if (imageUrl.isNotEmpty()) {
//            try {
//                val bitmap = BitmapFactory.decodeStream(
//                    URL(imageUrl).openConnection().getInputStream()
//                )
//                builder.setLargeIcon(bitmap) // 👈 CLAVE
//            } catch (e: Exception) {
//                e.printStackTrace()
//            }
//        }
//
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            val channel = NotificationChannel(
//                "canal_geinz",
//                "Geinz Notificaciones",
//                NotificationManager.IMPORTANCE_HIGH
//            )
//            channel.description = "Notificaciones generales de Geinz"
//            getSystemService(NotificationManager::class.java)
//                .createNotificationChannel(channel)
//        }
//
//        NotificationManagerCompat.from(this).notify(1000, builder.build())
//    }

}
