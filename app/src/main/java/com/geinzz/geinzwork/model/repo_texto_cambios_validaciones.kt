package com.geinzz.geinzwork.model

import android.util.Log


class repo_texto_cambios_validaciones {
    fun verificar_dispostivo_iguales(
        nombre1_dispo: String,
        nombre2_dispo: String
    ): Boolean {
        val nombreDispoNormalizado = nombre1_dispo.normalizar()
        val dispositivoNormalizado = nombre2_dispo.normalizar()
        Log.d("verificamos_igauldad","$nombreDispoNormalizado == $dispositivoNormalizado")
        return nombreDispoNormalizado == dispositivoNormalizado
    }

    private fun String.normalizar(): String {
        return this.replace("-", "")
            .replace("_", "")
            .replace(" ", "")
            .replace(".", "")
            .lowercase()
    }
}