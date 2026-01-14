package com.geinzz.geinzwork.utils.constantes

import java.util.UUID

object constantes_cobro_monedas {
    fun generarIdRecarga(): String {
        return UUID.randomUUID().toString()
    }

    fun calcular_precio_soles(monedas_gasto: String): Double {
        val monedas = monedas_gasto.toDoubleOrNull() ?: 0.0
        return monedas / 100
    }
}