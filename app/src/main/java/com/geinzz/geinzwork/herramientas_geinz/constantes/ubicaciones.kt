package com.geinzz.geinzwork.herramientas_geinz.constantes


import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.core.content.ContextCompat
import com.geinzz.geinzwork.model.repo_seguridad_salud
import com.google.android.gms.location.FusedLocationProviderClient
import kotlinx.coroutines.delay
data class ResultadoUbicacion(
    val lat: Double,
    val lng: Double,
    val exito: Boolean,
    val mensajeError: String = ""
)
object ubicaciones {
    /**
     * Obtiene la ubicación de forma robusta con:
     * - Verificación de permiso antes de intentar
     * - Timeout por intento (timeoutMs)
     * - Reintentos automáticos (maxIntentos)
     * - Resultado claro: éxito o fallo con mensaje
     */
    suspend fun obtenerUbicacionRobusta(
        fusedLocationClient: FusedLocationProviderClient,
        repo: repo_seguridad_salud,
        maxIntentos: Int = 3,
        timeoutMs: Long = 8_000L
    ): ResultadoUbicacion {
        repeat(maxIntentos) { intento ->
            try {
                // Pequeño backoff entre reintentos
                if (intento > 0) delay(1_000L * intento)

                val datosConCallback = repo.obtenerUbicacionUsuarioCancelable(fusedLocationClient)
                val latLng = datosConCallback.latLng
                repo.cancelarUbicacion(fusedLocationClient, datosConCallback.callback)

                if (latLng.latitude != 0.0 || latLng.longitude != 0.0) {
                    return ResultadoUbicacion(
                        lat = latLng.latitude,
                        lng = latLng.longitude,
                        exito = true
                    )
                }
                // lat/lng son 0,0 → coordenada inválida, reintentamos
            } catch (e: Exception) {
                // Excepción en este intento → continuamos al siguiente
            }
        }
        return ResultadoUbicacion(
            lat = 0.0, lng = 0.0,
            exito = false,
            mensajeError = "No se pudo obtener tu ubicación tras $maxIntentos intentos"
        )
    }

    /**
     * Verifica si la app tiene permiso de ubicación en este momento.
     * No lanza ni pide nada — solo consulta.
     */
    fun tienePermisoUbicacion(context: Context): Boolean =
        ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
}