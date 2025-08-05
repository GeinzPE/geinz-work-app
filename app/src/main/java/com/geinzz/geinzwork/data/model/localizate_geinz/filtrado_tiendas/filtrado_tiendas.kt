package com.geinzz.geinzwork.data.model.localizate_geinz.filtrado_tiendas

data class filtrado_tiendas_cat_sub(val categoria: String, val subcategorias: List<String>)

data class tiendas_filtradas(
    val img_tiendas: String = "",
    val nombre_tienda: String = "",
    val direccion: String = "",
    val referencia: String = "",
    val latitud: Double = 0.0,
    val longitud: Double = 0.0,
    val lista_subcategoiras: List<String> = emptyList(),
    val descripcion: String = "",
    val id_tienda: String = "",
)
