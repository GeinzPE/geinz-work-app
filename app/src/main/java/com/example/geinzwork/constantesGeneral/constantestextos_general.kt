package com.geinzz.geinzwork.constantesGeneral

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.graphics.Paint
import android.graphics.Typeface
import android.text.Spannable
import android.text.SpannableString
import android.text.style.StyleSpan
import android.text.style.UnderlineSpan
import android.view.ViewTreeObserver
import android.widget.TextView
import android.widget.Toast
import androidx.core.view.isVisible
import com.geinzz.geinzwork.dataclass.daclassReview

object constantestextos_general {

    fun subrallarTexto(fullText: String, textoAsubrallar: TextView) {
        val fullText = fullText
        val spannableString = SpannableString(fullText)

        val startIndex = fullText.indexOf(fullText)
        val endIndex = startIndex + fullText.length


        spannableString.setSpan(UnderlineSpan(), startIndex, endIndex, 0)
        textoAsubrallar.text = spannableString
    }

    fun textoPrimarioBold(daclassReview: daclassReview, review: TextView) {
        val spannableString =
            SpannableString("Reseña:  ${daclassReview.review}")


        val boldSpan = StyleSpan(Typeface.BOLD)
        val startIndex = 0
        val endIndex = "Reseña".length ?: 0
        spannableString.setSpan(
            boldSpan,
            startIndex,
            endIndex,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        review.text = spannableString
    }

    fun setearInformacionboldDescripcion(
        endIndex: String,
        SpannableString: SpannableString,
        texview: TextView
    ) {
        val boldSpan = StyleSpan(Typeface.BOLD)
        val starIndex = 0
        val endIndex = "$endIndex".length ?: 0
        SpannableString.setSpan(
            boldSpan, starIndex, endIndex, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        texview.text = SpannableString

    }

    fun textoPrimarioBold2(daclassReview: daclassReview, review: TextView) {
        if (daclassReview.TipoTrabajo.isNullOrEmpty()) {
            review.isVisible = false
        } else {
            val spannableString =
                SpannableString("Tipo de Trabajo:  ${daclassReview.TipoTrabajo}")

            val boldSpan = StyleSpan(Typeface.BOLD)
            val startIndex = 0
            val endIndex = "Tipo de Trabajo:".length ?: 0
            spannableString.setSpan(
                boldSpan,
                startIndex,
                endIndex,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )

            review.text = spannableString
        }

    }


    fun extender_acortar_texto(descripcion: TextView, tvReadMore: TextView) {
        descripcion.viewTreeObserver.addOnGlobalLayoutListener(object :
            ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                descripcion.viewTreeObserver.removeOnGlobalLayoutListener(this)
                if (descripcion.lineCount >= 2) {
                    tvReadMore.isVisible = true
                    println("el texo es lagor $descripcion")
                } else {
                    tvReadMore.isVisible = false
                }
            }
        })

        tvReadMore.setOnClickListener {
            if (tvReadMore.text == "Leer más") {
                descripcion.maxLines = Integer.MAX_VALUE
                tvReadMore.text = "Leer menos"
            } else {
                descripcion.maxLines = 3
                tvReadMore.text = "Leer más"
            }
        }
    }

    fun extender_acortar_texto2(descripcion: TextView, tvReadMore: TextView) {
        descripcion.viewTreeObserver.addOnGlobalLayoutListener(object :
            ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                descripcion.viewTreeObserver.removeOnGlobalLayoutListener(this)
                if (descripcion.lineCount >= 2) {
                    tvReadMore.isVisible = true
                    println("el texo es lagor $descripcion")
                } else {
                    tvReadMore.isVisible = false
                }
            }
        })

        tvReadMore.setOnClickListener {
            if (tvReadMore.text == "Leer más") {
                descripcion.maxLines = Integer.MAX_VALUE
                tvReadMore.text = "Leer menos"
            } else {
                descripcion.maxLines = 2
                tvReadMore.text = "Leer más"
            }
        }
    }


    fun marcarDescuentoTxt(
        textViewPriceBefore: TextView,
    ) {
        textViewPriceBefore.paintFlags =
            textViewPriceBefore.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
        textViewPriceBefore.textSize = 12f

    }

    fun setearPrecioDescuentoPrecioAntiguo(
        precio: Number? = null,
        precioTXT: TextView? = null,
        antiguo_precio: Number? = null,
        antiguo_precioTXT: TextView? = null,
        descuentoPocentaje: Number? = null,
        descuentoPocentajeTXT: TextView? = null,
    ) {

        println("$precio,$antiguo_precio,$descuentoPocentaje")
        // Formatear y asignar precio actual con "S/"
        precio?.let {
            val precioFormateado = if (it.toDouble() % 1.0 == 0.0) {
                "S/ %.2f".format(it.toDouble())
            } else {
                "S/ $it"
            }
            precioTXT?.text = precioFormateado
        }

        // Formatear y asignar precio antiguo con "S/"
        antiguo_precio?.let {
            val antiguoPrecioFormateado = if (it.toDouble() % 1.0 == 0.0) {
                "S/ %.2f".format(it.toDouble())
            } else {
                "S/ $it"
            }
            antiguo_precioTXT?.text = antiguoPrecioFormateado
        }
        descuentoPocentaje?.let {
            val descuentoFormateado = if (it.toDouble() % 1.0 == 0.0) {
                "-${it.toInt()}%"  // Si es entero, muestra sin decimales
            } else {
                "-i$it%"  // Si tiene decimales, lo muestra tal cual
            }
            descuentoPocentajeTXT?.text = descuentoFormateado
        }


    }

    fun copiarTexto_portapapeles(textoTexview:TextView,context: Context){
        val textoACopiar = textoTexview.text.toString()

        val clipboard =
            context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("texto", textoACopiar)
        clipboard.setPrimaryClip(clip)

        Toast.makeText(
            context,
            "Texto copiado al portapapeles",
            Toast.LENGTH_SHORT
        ).show()
    }


}