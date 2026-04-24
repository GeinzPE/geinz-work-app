package com.geinzz.geinzwork.herramientas_geinz.constantes

import android.content.Context
import android.net.Uri

object constante_abrir_navegador {
    fun openCustomTab(context: Context, url: String) {

        val builder = androidx.browser.customtabs.CustomTabsIntent.Builder()

        builder.setShowTitle(true)
        builder.setInstantAppsEnabled(true)

        val customTabsIntent = builder.build()
        customTabsIntent.launchUrl(context, Uri.parse(url))
    }
}