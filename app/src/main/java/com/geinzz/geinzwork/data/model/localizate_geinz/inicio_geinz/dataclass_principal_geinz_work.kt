package com.geinzz.geinzwork.data.model.localizate_geinz.inicio_geinz

data class dataclass_principal_geinz_work_lugares_turistico(
    val nombre_lugar: String,
    val descripcion_lugar: String
)

data class recomedado(
    val nombre_recomendado: String,
    val texto_recomendao: String,
    val estrellas_recomendad: Int,
    val latitud_: Double,
    val longitud: Double,
    val ref: String,
    val direccion: String
)

data class dataclass_cat_sub(
    val nombre: String?,
    val lista_subcategorias: List<String>?,
    val lista_img: String = ""
)

data class lugares_turisticos(
    val titulo: String?,
    val descripcion: String?,
    val img_ref: String?,
    val direcccion: String?,
    val referencia: String?,
    val longitud: Double = 0.0,
    val latitud: Double = 0.0
)
