package com.geinzz.geinzwork.data.model.dataclass_seguridad

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class dataclass_seguridad(
    val nombre_: String = "",
    val direccion: String = "",
    val numero_llamada: String,
    val numero_whatsapp: String,
    val latidud: Double,
    val longitud: Double,
    val img_ref:String="",
    val categoria:String=""
): Parcelable