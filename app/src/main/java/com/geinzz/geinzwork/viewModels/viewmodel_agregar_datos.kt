package com.geinzz.geinzwork.viewModels

import android.content.Context
import android.location.Geocoder
import android.util.Log
import androidx.lifecycle.ViewModel
import com.geinzz.geinzwork.data.model.localizate_geinz.HorarioAtencion_box
import com.geinzz.geinzwork.data.model.localizate_geinz.HorarioBloque
import com.geinzz.geinzwork.data.model.localizate_geinz.HorarioDia_bloques
import com.geinzz.geinzwork.ui.adapters.ui.pantallas.HorasDia
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Locale

class viewmodel_agregar_datos : ViewModel() {
    val dias = listOf(
        "Lunes", "Martes", "Miércoles",
        "Jueves", "Viernes", "Sábado", "Domingo"
    )

    val mapaHoras = dias.associateWith { HorasDia() }.toMutableMap()

    fun obtenerHorarioAtencion(): HorarioAtencion_box {

        fun crearBloques(h: HorasDia): List<HorarioBloque> {

            val bloques = mutableListOf<HorarioBloque>()

            // Bloque AM
            if (h.h1AM.value.isNotBlank() && h.h2AM.value.isNotBlank()) {
                bloques.add(
                    HorarioBloque(
                        h_apertura = h.h1AM.value,
                        h_cierre = h.h2AM.value
                    )
                )
            }

            // Bloque PM
            if (h.h1PM.value.isNotBlank() && h.h2PM.value.isNotBlank()) {
                bloques.add(
                    HorarioBloque(
                        h_apertura = h.h1PM.value,
                        h_cierre = h.h2PM.value
                    )
                )
            }

            return bloques
        }

        return HorarioAtencion_box(
            lunes = HorarioDia_bloques(
                bloques = crearBloques(mapaHoras["Lunes"]!!),
                cerrado = !mapaHoras["Lunes"]!!.cerrado.value,
                motivo = "" // si tienes motivo, lo pones aquí
            ),
            martes = HorarioDia_bloques(
                bloques = crearBloques(mapaHoras["Martes"]!!),
                cerrado = !mapaHoras["Martes"]!!.cerrado.value,
                motivo = ""
            ),
            miércoles = HorarioDia_bloques(
                bloques = crearBloques(mapaHoras["Miércoles"]!!),
                cerrado = !mapaHoras["Miércoles"]!!.cerrado.value,
                motivo = ""
            ),
            jueves = HorarioDia_bloques(
                bloques = crearBloques(mapaHoras["Jueves"]!!),
                cerrado = !mapaHoras["Jueves"]!!.cerrado.value,
                motivo = ""
            ),
            viernes = HorarioDia_bloques(
                bloques = crearBloques(mapaHoras["Viernes"]!!),
                cerrado = !mapaHoras["Viernes"]!!.cerrado.value,
                motivo = ""
            ),
            sábado = HorarioDia_bloques(
                bloques = crearBloques(mapaHoras["Sábado"]!!),
                cerrado = !mapaHoras["Sábado"]!!.cerrado.value,
                motivo = ""
            ),
            domingo = HorarioDia_bloques(
                bloques = crearBloques(mapaHoras["Domingo"]!!),
                cerrado = !mapaHoras["Domingo"]!!.cerrado.value,
                motivo = ""
            )
        )
    }

    suspend fun obtenerDireccion(lat: Double, lon: Double, context: Context): String? {
        return withContext(Dispatchers.IO) {
            try {
                val geocoder = Geocoder(context, Locale.getDefault())
                val direcciones = geocoder.getFromLocation(lat, lon, 1)

                if (!direcciones.isNullOrEmpty()) {
                    val addressLine = direcciones[0].getAddressLine(0)

                    Log.d("GeocoderFunc", "AddressLine(0): $addressLine")

                    addressLine
                } else {
                    Log.w("GeocoderFunc", "No se encontró AddressLine(0)")
                    null
                }
            } catch (e: Exception) {
                Log.e("GeocoderFunc", "Error: ${e.message}")
                null
            }
        }
    }


}