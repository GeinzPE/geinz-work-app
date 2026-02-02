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
import java.time.temporal.ChronoUnit
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone
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

    @RequiresApi(Build.VERSION_CODES.O)
    fun calcularTiempoPromo(
        inicio: Timestamp,
        fin: Timestamp,
        tipo: String
    ): TiempoPromo {

        val zone = ZoneId.systemDefault()

        return if (tipo == "horas") {

            // 🔹 HORAS → por tiempo real
            val ahoraMs = Timestamp.now().toDate().time
            val inicioMs = inicio.toDate().time
            val finMs = fin.toDate().time

            val totalHoras = (finMs - inicioMs) / 3_600_000.0
            val horasPasadas =
                (minOf(ahoraMs, finMs) - inicioMs) / 3_600_000.0

            TiempoPromo(
                duracion = String.format("%.1f horas", totalHoras),
                transcurrido = String.format("%.1f horas", max(0.0, horasPasadas))
            )

        } else {

            // 🔹 DÍAS → CALENDARIO (ESTO ES LO CORRECTO)
            val inicioDate = inicio.toDate().toInstant().atZone(zone).toLocalDate()
            val finDate = fin.toDate().toInstant().atZone(zone).toLocalDate()
            val hoyDate = LocalDate.now(zone)

            val totalDias = ChronoUnit.DAYS.between(inicioDate, finDate)
            val diasPasados = ChronoUnit.DAYS.between(
                inicioDate,
                minOf(hoyDate, finDate)
            )

            TiempoPromo(
                duracion = "$totalDias días",
                transcurrido = "$diasPasados días"
            )
        }
    }


    @RequiresApi(Build.VERSION_CODES.O)
    fun calcularCostoPromo(
        inicio: Timestamp,
        fin: Timestamp,
        tipo: String
    ): CostoPromo {

        return if (tipo == "horas") {

            // 🔹 COBRO POR TIEMPO REAL
            val ahoraMs = Timestamp.now().toDate().time
            val inicioMs = inicio.toDate().time
            val finMs = fin.toDate().time

            val totalHoras = (finMs - inicioMs) / 3_600_000.0
            val horasConsumidas =
                (minOf(ahoraMs, finMs) - inicioMs) / 3_600_000.0

            CostoPromo(
                total = max(0.0, totalHoras) * 3.0,
                consumido = max(0.0, horasConsumidas) * 3.0
            )

        } else {

            // 🔹 COBRO POR DÍA CALENDARIO (NO por horas)
            val zone = ZoneId.systemDefault()

            val inicioDate = inicio.toDate().toInstant().atZone(zone).toLocalDate()
            val finDate = fin.toDate().toInstant().atZone(zone).toLocalDate()
            val hoyDate = LocalDate.now(zone)

            val totalDias = ChronoUnit.DAYS.between(inicioDate, finDate)
            val diasConsumidos = ChronoUnit.DAYS.between(
                inicioDate,
                minOf(hoyDate, finDate)
            ) + 1

            CostoPromo(
                total = max(0, totalDias) * 30.0,
                consumido = max(0, diasConsumidos) * 30.0
            )
        }
    }

    fun obtenerFechaFinDosDias(): Timestamp {
        val cal = Calendar.getInstance()
        cal.add(Calendar.DAY_OF_MONTH, 2)
        return Timestamp(cal.time)
    }


    fun timestampEn30Dias(): Timestamp {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, 30)
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