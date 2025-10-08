package com.geinzz.geinzwork.model.open_apps.fb_tk_ig

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.util.Log
import android.widget.Toast

object open_fb_tk_ig {
    private fun isPackageInstalled(context: Context, packageName: String): Boolean {
        return try {
            context.packageManager.getPackageInfo(packageName, 0)
            true
        } catch (e: PackageManager.NameNotFoundException) {
            false
        }
    }
    private fun openUrl(context: Context, url: String, packageName: String? = null) {
        val uri = Uri.parse(url)
        try {
            val intent = Intent(Intent.ACTION_VIEW, uri).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                if (!packageName.isNullOrEmpty()) `package` = packageName
            }
            context.startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            // Si falla (app no instalada), abre en navegador
            val browserIntent = Intent(Intent.ACTION_VIEW, uri).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(browserIntent)
        }
    }
    fun openInstagram(context: Context, url: String) {
        // Evita errores si está vacío
        if (url.isBlank()) {
            Toast.makeText(context, "Enlace de Instagram no disponible", Toast.LENGTH_SHORT).show()
            return
        }

        try {
            val uri = Uri.parse(url)
            val packageName = "com.instagram.android"

            // Si la app de Instagram está instalada, intenta abrirla
            if (isPackageInstalled(context, packageName)) {
                val intent = Intent(Intent.ACTION_VIEW, uri).apply {
                    setPackage(packageName)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(intent)
            } else {
                // Si no, abre en navegador
                val browserIntent = Intent(Intent.ACTION_VIEW, uri).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(browserIntent)
            }
        } catch (e: Exception) {
            Toast.makeText(context, "No se pudo abrir Instagram", Toast.LENGTH_SHORT).show()
            e.printStackTrace()
        }
    }


    fun openFacebook(context: Context, pageUrl: String) {
        val facebookPackage = "com.facebook.katana"
        val facebookLitePackage = "com.facebook.lite"

        val uriApp = Uri.parse(pageUrl)
        val uriWeb = Uri.parse(pageUrl)

        try {
            when {
                isPackageInstalled(context, facebookPackage) -> {
                    // Intento abrir con Facebook normal
                    val intent = Intent(Intent.ACTION_VIEW, uriApp).apply {
                        setPackage(facebookPackage)
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    context.startActivity(intent)
                }
                isPackageInstalled(context, facebookLitePackage) -> {
                    // Intento abrir con Facebook Lite
                    val intent = Intent(Intent.ACTION_VIEW, uriApp).apply {
                        setPackage(facebookLitePackage)
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    context.startActivity(intent)
                }
                else -> {
                    // Si no hay ninguna app, abre en navegador
                    val browserIntent = Intent(Intent.ACTION_VIEW, uriWeb).apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    context.startActivity(browserIntent)
                }
            }
        } catch (e: ActivityNotFoundException) {
            // fallback por seguridad
            val browserIntent = Intent(Intent.ACTION_VIEW, uriWeb).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(browserIntent)
        }
    }

    fun openWebLink(context: Context, url: String) {
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "No se pudo abrir el enlace", Toast.LENGTH_SHORT).show()
        }
    }


    fun openTiktok(context: Context, username: String) {
        Log.d("ingrsamoa",username)
        val webUri = "https://www.tiktok.com/@$username"
        if (isPackageInstalled(context, "com.zhiliaoapp.musically")) {
            openUrl(context, webUri, "com.zhiliaoapp.musically")
        } else {
            openUrl(context, webUri)
        }
    }

}