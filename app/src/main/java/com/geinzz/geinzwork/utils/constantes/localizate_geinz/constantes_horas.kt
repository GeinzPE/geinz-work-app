package com.geinzz.geinzwork.utils.constantes.localizate_geinz

import android.location.Location


object constantes_horas {
    fun obtenerProximoDiaAbierto(
        horario: Map<String, Any>,
        diaActual: String
    ): Pair<String, Map<String, Any>>? {
        val dias = listOf("domingo", "lunes", "martes", "miércoles", "jueves", "viernes", "sábado")
        val indiceActual = dias.indexOf(diaActual)

        // Recorremos desde el siguiente día hasta completar la semana
        for (i in 1..7) {
            val indice = (indiceActual + i) % 7
            val dia = dias[indice]
            val horarioDia = horario[dia] as? Map<String, Any> ?: continue

            val cerrado = horarioDia["cerrado"] as? Boolean ?: true
            if (!cerrado) {
                // Día abierto encontrado ✅
                return Pair(dia, horarioDia)
            }
        }

        // Si no encuentra ningún día abierto
        return null
    }

    fun calcularDistanciaKm(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val resultados = FloatArray(1)
        Location.distanceBetween(lat1, lon1, lat2, lon2, resultados)
        return resultados[0] / 1000.0 // Pasa de metros a kilómetros
    }
}