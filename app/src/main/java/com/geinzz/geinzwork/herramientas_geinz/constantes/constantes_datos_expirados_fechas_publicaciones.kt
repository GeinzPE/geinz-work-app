package com.geinzz.geinzwork.herramientas_geinz.constantes

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.geinzz.geinzwork.data.model.CostoPromo
import com.geinzz.geinzwork.data.model.TiempoPromo
import com.google.firebase.Timestamp
import java.util.concurrent.TimeUnit
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import java.util.UUID
import kotlin.math.max
import kotlin.math.min

object constantes_datos_expirados_fechas_publicaciones {

    fun tiempoRestante(timestampFin: Timestamp): String {

        val ahora = Timestamp.now()

        val diffSeconds = timestampFin.seconds - ahora.seconds

        // 🔍 LOG PRINCIPAL
        Log.d(
            "TIEMPO_RESTANTE",
            "ahora=${ahora.toDate()} | fin=${timestampFin.toDate()} | diffSeconds=$diffSeconds"
        )

        if (diffSeconds <= 0) {
            Log.d("TIEMPO_RESTANTE", "⛔ EXPIRADO")
            return "Expirado"
        }

        val dias = diffSeconds / 86400
        val horas = (diffSeconds % 86400) / 3600
        val minutos = (diffSeconds % 3600) / 60

        Log.d(
            "TIEMPO_RESTANTE",
            "dias=$dias | horas=$horas | minutos=$minutos"
        )

        return when {
            dias > 0 -> {
                val txt = "$dias ${if (dias == 1L) "día" else "días"} restantes"
                Log.d("TIEMPO_RESTANTE", "➡️ RESULTADO: $txt")
                txt
            }

            horas > 0 -> {
                val txt =
                    "$horas ${if (horas == 1L) "hora" else "horas"} y " +
                            "$minutos ${if (minutos == 1L) "minuto" else "minutos"} restantes"
                Log.d("TIEMPO_RESTANTE", "➡️ RESULTADO: $txt")
                txt
            }

            minutos > 0 -> {
                val txt = "$minutos ${if (minutos == 1L) "minuto" else "minutos"} restantes"
                Log.d("TIEMPO_RESTANTE", "➡️ RESULTADO: $txt")
                txt
            }

            else -> {
                Log.d("TIEMPO_RESTANTE", "➡️ RESULTADO: Menos de un minuto restante")
                "Menos de un minuto restante"
            }
        }
    }


    fun obtenerEstadoFinal(
        timestampFin: Timestamp,
        enPausa: Boolean
    ): String {

        return when {
            estaExpirada(timestampFin) -> "expirada"
            enPausa -> "pausado"
            else -> "activo"
        }
    }


    fun estaExpirada(timestampFin: Timestamp): Boolean {
        return Timestamp.now().seconds >= timestampFin.seconds
    }


    fun Timestamp.formatoFechaHora(): String {
        val sdf = SimpleDateFormat(
            "dd 'de' MMMM 'de' yyyy - hh:mm a",
            Locale("es", "PE")
        )
        sdf.timeZone = TimeZone.getTimeZone("America/Lima")
        return sdf.format(this.toDate())
    }


    fun calcularTiempoPromo(
        inicio: Timestamp,
        fin: Timestamp
    ): TiempoPromo {

        val diferenciaMs = fin.toDate().time - inicio.toDate().time

        val horas = diferenciaMs / 3_600_000.0

        return if (horas < 24) {

            TiempoPromo(
                duracion = "${max(0, horas.toInt())} horas",
                transcurrido = "En curso"
            )

        } else {

            val dias = (horas / 24.0).toInt()

            TiempoPromo(
                duracion = "$dias días",
                transcurrido = "En curso"
            )
        }
    }



    fun calcularCostoPromo(
        dia_hora: String,
        precio_por_publicacion_hora: Int,
        precio_publicacion_dias: Int,
        inicio: Timestamp,
        fin: Timestamp
    ): CostoPromo {

        val zone = ZoneId.systemDefault()
        val inicioZdt = inicio.toDate().toInstant().atZone(zone)
        val finZdt    = fin.toDate().toInstant().atZone(zone)
        val hoyZdt    = ZonedDateTime.now(zone)

        Log.d("calcularCostoPromo", """
        ┌─────────────────────────────────────
        │ modo        : $dia_hora
        │ inicio      : $inicioZdt
        │ fin         : $finZdt
        │ hoy         : $hoyZdt
        │ precio/hora : $precio_por_publicacion_hora
        │ precio/dia  : $precio_publicacion_dias
        └─────────────────────────────────────
    """.trimIndent())

        return if (dia_hora == "hora") {

            val totalHoras      = ChronoUnit.HOURS.between(inicioZdt, finZdt)
            val horasConsumidas = ChronoUnit.HOURS.between(inicioZdt, minOf(hoyZdt, finZdt))
            val total           = max(0, totalHoras).toDouble()      * precio_por_publicacion_hora
            val consumido       = max(0, horasConsumidas).toDouble() * precio_por_publicacion_hora

            Log.d("calcularCostoPromo", """
            ┌─────────────────────────────────────
            │ [HORA]
            │ totalHoras      : $totalHoras
            │ horasConsumidas : $horasConsumidas
            │ total           : $total
            │ consumido       : $consumido
            └─────────────────────────────────────
        """.trimIndent())

            CostoPromo(total = total, consumido = consumido)

        } else {

            val inicioDate     = inicioZdt.toLocalDate()
            val finDate        = finZdt.toLocalDate()
            val hoyDate        = hoyZdt.toLocalDate()
            val totalDias      = ChronoUnit.DAYS.between(inicioDate, finDate)
            val diasConsumidos = ChronoUnit.DAYS.between(inicioDate, minOf(hoyDate, finDate)) + 1
            val total          = max(0, totalDias).toDouble()      * precio_publicacion_dias
            val consumido      = max(0, diasConsumidos).toDouble() * precio_publicacion_dias

            Log.d("calcularCostoPromo", """
            ┌─────────────────────────────────────
            │ [DIA]
            │ totalDias      : $totalDias
            │ diasConsumidos : $diasConsumidos
            │ total          : $total
            │ consumido      : $consumido
            └─────────────────────────────────────
        """.trimIndent())

            CostoPromo(total = total, consumido = consumido)
        }
    }
    fun obtenerFechaFinDosDias(): Timestamp {
        val cal = Calendar.getInstance()
        cal.add(Calendar.DAY_OF_MONTH, 2)
        return Timestamp(cal.time)
    }


    fun timestampEn30Dias(dias:Int): Timestamp {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, dias)
        return Timestamp(calendar.time)
    }






    fun timestampAFechaLegible(timestamp: Timestamp): String {
        val locale = Locale("es", "ES")

        val date = timestamp.toDate() // 🔥 aquí la conversión correcta
        val formato = SimpleDateFormat("EEEE d 'de' MMMM 'del' yyyy", locale)


        val fecha = formato.format(date)
        return fecha.replaceFirstChar { it.uppercase() }
    }

    fun timestampToFechaHora(timestamp: Timestamp): String {
        val date = timestamp.toDate()
        val format = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        return format.format(date)
    }
}