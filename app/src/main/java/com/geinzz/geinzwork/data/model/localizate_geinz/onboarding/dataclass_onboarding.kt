package com.geinzz.geinzwork.data.model.localizate_geinz.onboarding

data class dataclass_onboarding(
    val img: Int,
    val nombre_localidad: String,
    val nombre_lugar: String
)

data class dataclass_pantalla1(
    val titulo:String="",
    val texto: String="",
    val img: Int=0
)