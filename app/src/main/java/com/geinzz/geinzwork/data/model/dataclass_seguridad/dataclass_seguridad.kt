package com.geinzz.geinzwork.data.model.dataclass_seguridad

data class dataclass_seguridad(
    val nombre_: String = "",
    val direccion: String = "",
    val numero_llamada: String,
    val numero_whatsapp: String,
    val latidud: Number,
    val longitud: Number,
    val img_ref:String=""
)
