package com.geinzz.geinzwork.utils.constantes.constantes

import android.animation.ValueAnimator
import android.content.Context
import android.content.res.ColorStateList
import androidx.core.content.ContextCompat
import com.geinzz.geinzwork.R
import com.google.android.material.button.MaterialButton

object animacionesCambio_color_btnFiltrado_historial {
    fun styleBtn(context: Context, listaBtnFiltrado: List<MaterialButton>, btn: MaterialButton) {
        listaBtnFiltrado.forEach { button ->
            if (button == btn) {
                // Aplica el estilo clicado con animación
                animateBackgroundColor(
                    button,
                    R.color.textoColorBTNFiltrado,
                    R.color.colorBTNFiltrado,
                    context
                )
                button.setTextAppearance(R.style.RoundedButtonFiltradoHistorialCLikado)
            } else {
                // Aplica el estilo por defecto sin animación
                animateBackgroundColor(
                    button,
                    R.color.colorBTNFiltrado,
                    R.color.textoColorBTNFiltrado,
                    context
                )
                button.setTextAppearance(R.style.RoundedButtonFiltradoHistorial)
            }
        }
    }

    private fun animateBackgroundColor(
        button: MaterialButton,
        startColorRes: Int,
        endColorRes: Int,
        context: Context
    ) {
        val startColor = ContextCompat.getColor(context, startColorRes)
        val endColor = ContextCompat.getColor(context, endColorRes)

        val colorAnimator = ValueAnimator.ofArgb(startColor, endColor)
        colorAnimator.addUpdateListener { animator ->
            button.backgroundTintList = ColorStateList.valueOf(animator.animatedValue as Int)
        }
        colorAnimator.duration = 300
        colorAnimator.start()
    }
}