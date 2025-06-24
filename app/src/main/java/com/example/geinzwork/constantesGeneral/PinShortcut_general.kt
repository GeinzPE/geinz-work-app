package com.example.geinzwork.constantesGeneral

import android.app.PendingIntent
import android.content.Intent
import android.content.pm.ShortcutInfo
import android.content.pm.ShortcutManager
import android.graphics.drawable.Icon
import android.net.Uri
import android.os.Build
import android.widget.Toast
import com.example.geinzwork.fragmentos.panel_publicacion_trabajador
import com.geinzz.geinzwork.R
import android.content.Context
import com.example.geinzwork.activity_dispositivos_vinculados
import com.geinzz.geinzwork.GenerarQR_trabajador

object PinShortcut_general {

    fun panel_publicacion_trabajador_accesoDirecto_panel(context: Context) {
        val shortcutId = "geinzwork_main_app_pinned_shortcut_panel"
        val shortLabel = "Panel Publicaciones"
        val longLabel = "Panel de Publicaciones GeinzWork"

        val targetActivity = panel_publicacion_trabajador::class.java
        val iconResId = R.drawable.ic_pinned_shortcut_prueva

        requestPinAppShortcut(context, shortcutId, shortLabel, longLabel, targetActivity, iconResId)
    }

    fun qr_trabajador_accesoDirecto_panel(context: Context) {
        val shortcutId = "geinzwork_pinned_shortcut_qr_trabajador"
        val shortLabel = "Qr trabajador"
        val longLabel = "Qr Trabajador"

        val targetActivity = GenerarQR_trabajador::class.java
        val iconResId = R.drawable.ic_pinned_shorcut_qr_trabajador


        requestPinAppShortcut(context, shortcutId, shortLabel, longLabel, targetActivity, iconResId)
    }

    fun vinculados_accesoDirecto_panel(context: Context) {
        val shortcutId = "geinzwork_pinned_shortcut_dispositvos"
        val shortLabel = "Disposivos vinculados"
        val longLabel = "Disposivos vinculados"

        val targetActivity = activity_dispositivos_vinculados::class.java
        val iconResId = R.drawable.ic_pinned_shorcut_dispositivos_v


        requestPinAppShortcut(context, shortcutId, shortLabel, longLabel, targetActivity, iconResId)
    }


    fun handleIncomingIntent(intent: Intent?,content: Context) {
        intent?.let {
            // Ejemplo: Si el Intent lleva un "shortcut_action"
            val shortcutAction = it.getStringExtra("shortcut_action")
            when (shortcutAction) {
                "open_feature_X" -> {
                    Toast.makeText(content, "Abriendo Característica X desde acceso directo", Toast.LENGTH_SHORT).show()
                    // Aquí podrías navegar a un Fragment, o iniciar una nueva Activity
                    // Por ejemplo: supportFragmentManager.beginTransaction().replace(R.id.fragment_container, FeatureXFragment()).commit()
                }
                // Añade más casos según los datos que envíes en tus Intents de atajo
            }
            // Si usas Deeplinks con Uri:
            val data: Uri? = it.data
            data?.let { uri ->
                if (uri.host == "geinzapp.page.link" && uri.pathSegments.contains("chat")) {
                    val chatId = uri.lastPathSegment
                    Toast.makeText(content, "Abriendo chat con ID: $chatId", Toast.LENGTH_SHORT).show()
                    // Aquí podrías abrir una pantalla de chat con el chatId
                }
            }
        }
    }

    fun requestPinAppShortcut(
        context: Context,
        shortcutId: String,
        shortLabel: String,
        longLabel: String,
        targetActivityClass: Class<*>,
        iconResId: Int
    ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val shortcutManager = context.getSystemService(ShortcutManager::class.java)

            // 1. Verificar si el launcher soporta fijar accesos directos
            if (shortcutManager?.isRequestPinShortcutSupported == true) {

                // 2. Obtener los accesos directos fijados por tu aplicación
                val pinnedShortcuts = shortcutManager.getPinnedShortcuts()

                // 3. Verificar si ya existe un acceso directo con el mismo shortcutId
                val existingShortcut = pinnedShortcuts.find { it.id == shortcutId }

                if (existingShortcut != null) {
                    // El acceso directo con este ID ya existe
                    Toast.makeText(
                        context,
                        "El acceso directo '${shortLabel}' ya está en tu pantalla de inicio.",
                        Toast.LENGTH_SHORT
                    ).show()

                    // Opcional: Si quieres, puedes actualizarlo en lugar de simplemente no hacer nada.
                    // Si el contenido o la etiqueta pudieran cambiar, esto sería útil.
                    // updateExistingPinnedShortcut(context, existingShortcut, shortLabel, longLabel, iconResId, targetActivityClass)

                    return // Salir de la función, no creamos un nuevo acceso directo
                }

                // Si llegamos aquí, significa que el acceso directo NO existe, podemos crearlo

                val pinnedShortcutIntent = Intent(context, targetActivityClass).apply {
                    action = Intent.ACTION_VIEW
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    // Aquí podrías añadir extras si este acceso directo abre a una sección específica
                    putExtra("shortcut_action", "open_panel_publicaciones") // Ejemplo de extra
                }

                val pinShortcutInfo = ShortcutInfo.Builder(context, shortcutId)
                    .setShortLabel(shortLabel)
                    .setLongLabel(longLabel)
                    .setIcon(Icon.createWithResource(context, iconResId))
                    .setIntent(pinnedShortcutIntent)
                    .build()

                val pinnedShortcutCallbackIntent = shortcutManager.createShortcutResultIntent(pinShortcutInfo)
                val successCallback = PendingIntent.getBroadcast(
                    context,
                    0, // requestCode, si tienes múltiples PendingIntents, usa un valor único
                    pinnedShortcutCallbackIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )

                // 4. Solicitar el fijado del nuevo acceso directo
                shortcutManager.requestPinShortcut(pinShortcutInfo, successCallback.intentSender)

                Toast.makeText(
                    context,
                    context.getString(R.string.request_pin_shortcut_sent, shortLabel),
                    Toast.LENGTH_LONG
                ).show()

            } else {
                Toast.makeText(
                    context,
                    context.getString(R.string.launcher_not_support_pin_shortcut),
                    Toast.LENGTH_LONG
                ).show()
            }
        } else {
            Toast.makeText(
                context,
                context.getString(R.string.android_version_not_support_pin_shortcut),
                Toast.LENGTH_LONG
            ).show()
        }
    }
}