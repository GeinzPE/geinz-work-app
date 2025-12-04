package com.geinzz.geinzwork.model.open_apps.fb_tk_ig

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import com.geinzz.geinzwork.model.repo_eres_socio
import java.net.URLEncoder

    @RequiresApi(Build.VERSION_CODES.O)
object open_fb_tk_ig {

    val repo_socios= repo_eres_socio()
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

    fun openInstagram(tienda:String,context: Context, url: String,id_tienda:String,localidad_tienda:String) {

        if (url.isBlank()) {
            Toast.makeText(context, "Enlace de Instagram no disponible", Toast.LENGTH_SHORT).show()
            return
        }

        try {
            val uri = Uri.parse(url)
            val packageName = "com.instagram.android"
            if(tienda=="Tienda"){
            repo_socios.agregar_contador("instagram",id_tienda,localidad_tienda)
            }
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


    fun openFacebook(tipo:String="Tienda",context: Context, pageUrl: String,id_tienda:String,localidad_tienda:String) {

        if (pageUrl.isBlank()) {
            Toast.makeText(context, "Enlace de Facebook no disponible", Toast.LENGTH_SHORT).show()
            return
        }

        val facebookPackage = "com.facebook.katana"
        val facebookLitePackage = "com.facebook.lite"

        val uri = Uri.parse(pageUrl)

        try {
            when {
                isPackageInstalled(context, facebookPackage) -> {
                    val intent = Intent(Intent.ACTION_VIEW, uri).apply {
                        setPackage(facebookPackage)
                    }
                    context.startActivity(intent)
                }
                isPackageInstalled(context, facebookLitePackage) -> {
                    val intent = Intent(Intent.ACTION_VIEW, uri).apply {
                        setPackage(facebookLitePackage)
                    }
                    context.startActivity(intent)
                }
                else -> {
                    val browserIntent = Intent(Intent.ACTION_VIEW, uri)
                    context.startActivity(browserIntent)
                    if(tipo=="Tienda"){
                    repo_socios.agregar_contador("facebook",id_tienda,localidad_tienda)
                    }
                }
            }
        } catch (e: ActivityNotFoundException) {
            val browserIntent = Intent(Intent.ACTION_VIEW, uri)
            context.startActivity(browserIntent)
        }
    }


    fun openWebLink(context: Context, url: String,id_tienda:String,localidad_tienda:String) {

        if (url.isBlank()) {
            Toast.makeText(context, "Enlace de sitio web no disponible", Toast.LENGTH_SHORT).show()
            return
        }
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            repo_socios.agregar_contador("sitio_web",id_tienda,localidad_tienda)
        } catch (e: Exception) {
            Toast.makeText(context, "No se pudo abrir el enlace", Toast.LENGTH_SHORT).show()
        }
    }


    fun openTiktok(tipo:String="Tienda",context: Context, username: String,id_tienda:String,localidad_tienda:String) {

        if (username.isBlank()) {
            Toast.makeText(context, "Enlace de sitio web no disponible", Toast.LENGTH_SHORT).show()
            return
        }

        val webUri = username
        if (isPackageInstalled(context, "com.ss.android.ugc.trill")) {
            openUrl(context, webUri, "com.ss.android.ugc.trill")
            if(tipo=="Tienda"){
            repo_socios.agregar_contador("tiktok",id_tienda,localidad_tienda)
            }
        } else {
            if(tipo=="Tienda"){
            repo_socios.agregar_contador("tiktok",id_tienda,localidad_tienda)
            }
            openUrl(context, webUri)
        }
    }


    fun abrir_whattsapp(
        tipo:String="tienda",id_tienda:String,localidad_tienda:String,
        context: Context,
        numero: String,
        mensajePredefinido: String = "¡Hola! Vengo de Geinz y me gustaría hacer una consulta. ¿Me pueden atender?"
    ) {

        val mensajeCodificado = URLEncoder.encode(mensajePredefinido, "UTF-8")
        val uri = Uri.parse(
            "https://api.whatsapp.com/send?phone=${"+51$numero"}&text=$mensajeCodificado"
        )

        val intent = Intent(Intent.ACTION_VIEW, uri)
        try {
            context.startActivity(intent)
            if(tipo == "tienda"){
            repo_socios.agregar_contador("whatsapp",id_tienda,localidad_tienda)
            }

        } catch (e: Exception) {
            Toast.makeText(
                context,
                "no se pudo abrir whatsapp",
                Toast.LENGTH_LONG
            )
                .show()
        }
    }

}