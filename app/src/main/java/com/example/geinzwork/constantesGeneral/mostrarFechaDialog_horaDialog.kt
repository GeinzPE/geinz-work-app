package com.geinzz.geinzwork.constantesGeneral

import android.app.TimePickerDialog
import android.content.Context
import android.icu.text.SimpleDateFormat
import android.os.Build
import android.text.format.DateFormat
import android.widget.EditText
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.example.geinzwork.hora.timePiker24hours
import com.geinzz.geinzwork.hora.datePickterFracment
import java.time.LocalDate
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
    @RequiresApi(Build.VERSION_CODES.O)
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


}