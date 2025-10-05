package com.geinzz.geinzwork.data.model.dataclass_seguridad

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class dataclass_seguridad(
    val nombre_: String = "",
    val direccion: String = "",
    val numero_llamada: List<String>,
    val numero_whatsapp:  List<String>,
    val latidud: Double=0.0,
    val longitud: Double=0.0,
    val img_ref:String="",
    val categoria:String=""
): Parcelable