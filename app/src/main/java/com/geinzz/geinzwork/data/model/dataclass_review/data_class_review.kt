package com.geinzz.geinzwork.data.model.dataclass_review

data class data_class_review(val id_tienda_lugar: String = "", val localida_lugar: String = "")

data class data_class_resultado_tienda_lugar(
    val id: String = "",
    val nombre: String = "",
    val imagen: String = "",
    val localidad: String = ""
)

data class datos_review(
    val id_usuario: String="",
    val cantidad_Strar: Int=0,
    val descripcion_review: String="",
    val verificado_presencial: Boolean=false,
    val id_tienda_lugar: String="",
    val localidad_tienda: String="",
    val hora:String="",
    val fecha: String="",
    )

