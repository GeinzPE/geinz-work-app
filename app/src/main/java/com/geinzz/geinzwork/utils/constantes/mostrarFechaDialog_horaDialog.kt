package com.geinzz.geinzwork.utils.constantes.constantes

import android.app.TimePickerDialog
import android.content.Context
import android.icu.text.SimpleDateFormat
import android.os.Build
import android.text.format.DateFormat
import android.widget.EditText
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.geinzz.geinzwork.utils.constantes.hora.timePiker24hours
import com.geinzz.geinzwork.utils.constantes.hora.datePickterFracment
import com.google.firebase.Timestamp
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Calendar
import java.util.Date
import java.util.Locale

object mostrarFechaDialog_horaDialog {
     fun mostrarFechaDialogVista(fecha:EditText,activity: AppCompatActivity) {
        val datePicker = datePickterFracment { dia, mes, year ->
            CalendarioSelecionado(fecha,dia, mes, year)
        }
         datePicker.show(activity.supportFragmentManager, "datePicker")
    }

    fun CalendarioSelecionado(fecha:EditText,day: Int, month: String, year: Int) {
        fecha.setText("$day/$month/$year")
    }

    fun showTimePicker(context: Context, textView: EditText) {
        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)

        val timePickerDialog = TimePickerDialog(
            context,
            { _, hourOfDay, minute ->
                val selectedTime = formatTime(hourOfDay, minute)
                textView.setText(selectedTime)
            },
            hour,
            minute,
            DateFormat.is24HourFormat(context)
        )

        timePickerDialog.show()
    }

    private fun formatTime(hourOfDay: Int, minute: Int): String {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
        calendar.set(Calendar.MINUTE, minute)

        val amPm = if (calendar.get(Calendar.AM_PM) == Calendar.AM) "a. m." else "p. m."
        val hour = if (hourOfDay % 12 == 0) 12 else hourOfDay % 12
        val simpleDateFormat = SimpleDateFormat("hh:mm", Locale.getDefault())

        return "${simpleDateFormat.format(calendar.time)} $amPm"
    }

    fun showTimePiker(activity: AppCompatActivity,Editext:EditText){
        val timePiker=timePiker24hours{time->ontimeSelect24Hours(time,Editext)}
        timePiker.show(activity.supportFragmentManager,"time")
    }

    private fun ontimeSelect24Hours(time:String,Editext:EditText){
        Editext.setText(time)
    }

    fun calcularDiasRestantes(fechaVencimientoStr: String): Long {
        try {
            val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
            val fechaVencimiento = LocalDate.parse(fechaVencimientoStr, formatter)
            val fechaActual = LocalDate.now()
            return ChronoUnit.DAYS.between(fechaActual, fechaVencimiento)
        } catch (e: Exception) {
            e.printStackTrace()
            return -1
        }
    }

    fun obtenerFechaActual(): String {
        val formato = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val fechaActual = Date()
        return formato.format(fechaActual)
    }

    fun obtenerFechaConDias(fechaBase: String, dias: Int): String {
        val formato = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val date = formato.parse(fechaBase) ?: return ""

        val calendar = Calendar.getInstance()
        calendar.time = date
        calendar.add(Calendar.DAY_OF_YEAR, dias)

        return formato.format(calendar.time)
    }


    fun obtenerHoraActual(): String {
        val formato = SimpleDateFormat("HH:mm", Locale.getDefault())
        val horaActual = Date()
        return formato.format(horaActual)
    }


    fun obtenerHoraFin(horas: Int): String {
        val horaActual = LocalTime.now()          // Hora actual
        val horaFin = horaActual.plusHours(horas.toLong()) // Sumar horas
        val formatter = DateTimeFormatter.ofPattern("HH:mm")
        return horaFin.format(formatter)          // Devolver solo hora y minutos
    }
//
//
//    fun obtenerTimestampHoraFin(horas: Int): Timestamp {
//        val ahora = LocalDateTime.now()
//        val fin = ahora.plusHours(horas.toLong())
//        val instant = fin
//            .atZone(ZoneId.systemDefault())
//            .toInstant()
//
//        return Timestamp(instant.epochSecond, instant.nano)
//    }


    fun obtenerTimestampFin(valor: Int, tipo: String): Timestamp {
        val instant = when (tipo) {
            "dias" -> LocalDateTime.now().plusDays(valor.toLong())
            else   -> LocalDateTime.now().plusHours(valor.toLong())
        }
            .atZone(ZoneId.systemDefault())
            .toInstant()

        return Timestamp(instant.epochSecond, instant.nano)
    }


    fun obtenerTimestampHoraInicio(): Timestamp {
        val ahora = LocalDateTime.now()

        val instant = ahora
            .atZone(ZoneId.systemDefault())
            .toInstant()

        return Timestamp(instant.epochSecond, instant.nano)
    }



    fun obtenerTimestampInicio(): Timestamp {
        val instant = LocalDateTime.now()
            .atZone(ZoneId.systemDefault())
            .toInstant()

        return Timestamp(instant.epochSecond, instant.nano)
    }


    fun obtenerTimestampFinDias(dias: Int): Timestamp {
        val instant = LocalDateTime.now()
            .plusDays(dias.toLong())
            .atZone(ZoneId.systemDefault())
            .toInstant()

        return Timestamp(instant.epochSecond, instant.nano)
    }


    fun obtenerTimestampFecha(fecha: String): Timestamp {
        val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
        val localDate = LocalDate.parse(fecha, formatter)

        val instant = localDate
            .atStartOfDay(ZoneId.systemDefault()) // ⬅ zona local (importante)
            .toInstant()

        return Timestamp(instant.epochSecond, instant.nano)
    }



}