package com.geinzz.geinzwork.utils.constantes.hora

import android.app.Dialog
import android.app.TimePickerDialog
import android.content.Context
import android.os.Bundle
import android.widget.TimePicker
import androidx.fragment.app.DialogFragment
import java.util.Calendar

class timePickterFracment_normal(val listener: (String) -> Unit) : DialogFragment(),
    TimePickerDialog.OnTimeSetListener {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val calendar = Calendar.getInstance()
        val hora = calendar.get(Calendar.HOUR_OF_DAY)
        val minuto = calendar.get(Calendar.MINUTE)
        val dialog = TimePickerDialog(activity as Context, this, hora, minuto, false)
        return dialog
    }

    override fun onTimeSet(view: TimePicker?, hourOfDay: Int, minute: Int) {
        val amPm = if (hourOfDay < 12) "AM" else "PM"
        val formattedHour = if (hourOfDay == 0 || hourOfDay == 12) 12 else hourOfDay % 12
        val formattedMinute = String.format("%02d", minute)
        val formattedTime = String.format("%02d:%s %s", formattedHour, formattedMinute, amPm)

        listener(formattedTime)
    }
}