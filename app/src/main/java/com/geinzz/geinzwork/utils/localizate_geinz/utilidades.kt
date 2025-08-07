package com.geinzz.geinzwork.utils.localizate_geinz

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.location.LocationManager
import android.net.Uri
import android.widget.Toast
import java.text.Normalizer

fun normalizarTexto(texto: String): String {
    val textoSinTildes = Normalizer.normalize(texto, Normalizer.Form.NFD)
        .replace("\\p{InCombiningDiacriticalMarks}+".toRegex(), "")
    return textoSinTildes.lowercase().trim()
}

fun abrirRutaEnGoogleMaps(context: Context, latitud: Double, longitud: Double) {
    val uri = Uri.parse("google.navigation:q=$latitud,$longitud&mode=d")
    val intent = Intent(Intent.ACTION_VIEW, uri).apply {
        setPackage("com.google.android.apps.maps")
    }

    try {
        context.startActivity(intent)
    } catch (e: ActivityNotFoundException) {
        Toast.makeText(
            context,
            "Google Maps no está instalado. Por favor, instálalo desde Play Store.",
            Toast.LENGTH_LONG
        ).show()
    }
}

fun verificarUbiActiva(context: Context): Boolean {
    val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
}
