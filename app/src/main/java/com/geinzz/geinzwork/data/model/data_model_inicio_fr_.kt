package com.geinzz.geinzwork.data.model

data class data_model_inicio_fr_(
    val titulo: String?,
    val descripcion: String?,
    val img: String?,
    val id: String?
)

data class data_model_trabajador_scanner(
    val id_trabajador: String="",
    val nombre: String?,
    val nacionalidad: String?,
    val categoria: String?,
    val img: String?
)