package com.geinzz.geinzwork.utils.constantes.constantes

import android.content.Context
import android.graphics.drawable.Drawable
import androidx.core.content.ContextCompat
import com.geinzz.geinzwork.R

object constantes_valores {
    fun getDrawableMiIcono(context: Context): Drawable? {
        return ContextCompat.getDrawable(context, R.drawable.img_perfil)
    }}