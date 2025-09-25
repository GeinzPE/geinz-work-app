package com.geinzz.geinzwork.data.model.localizate_geinz

data class dataclass_cat_sub(
    val nombre: String = "",
    val lista_subcategorias: List<String> = emptyList(),
    val lista_img: String = ""
)


data class dataclass_cat_sub_lista_cat(
    val nombre_cat: String = "",
    val lista_subcategorias: List<String> = emptyList(),

)
