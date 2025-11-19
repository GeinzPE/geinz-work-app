package com.geinzz.geinzwork.utils.constantes.constantes

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast

object constantes_redes {

    fun openApps(context: Context, url: String, packageName: String) {
        try {
            val uri = Uri.parse(url)
            val intent = Intent(Intent.ACTION_VIEW, uri)

            val packageManager = context.packageManager
            val isAppInstalled = packageManager.getLaunchIntentForPackage(packageName) != null

            if (isAppInstalled) {
                intent.setPackage(packageName)
                context.startActivity(intent)
            } else {
                Toast.makeText(context, "Aplicación no instalada. Abriendo en navegador.", Toast.LENGTH_SHORT).show()
                context.startActivity(Intent(Intent.ACTION_VIEW, uri))
            }
        } catch (e: Exception) {
            Toast.makeText(context, "Error al abrir el enlace.", Toast.LENGTH_SHORT).show()
            e.printStackTrace()
        }
    }

    fun openPlayStore(context: Context, appPackage: String) {
        try {
            val playStoreUri = Uri.parse("market://details?id=$appPackage")
            val intent = Intent(Intent.ACTION_VIEW, playStoreUri)

            val packageManager = context.packageManager
            val isPlayStoreInstalled =
                packageManager.getLaunchIntentForPackage("com.android.vending") != null

            if (isPlayStoreInstalled) {
                // Abrir directamente la Play Store
                intent.setPackage("com.android.vending")
                context.startActivity(intent)
            } else {
                // Abrir en navegador si Play Store no está instalada
                val webUri = Uri.parse("https://play.google.com/store/apps/details?id=$appPackage")
                Toast.makeText(context, "Play Store no instalada. Abriendo en navegador.", Toast.LENGTH_SHORT).show()
                context.startActivity(Intent(Intent.ACTION_VIEW, webUri))
            }

        } catch (e: Exception) {
            Toast.makeText(context, "Error al abrir Play Store", Toast.LENGTH_SHORT).show()
            e.printStackTrace()
        }
    }

}