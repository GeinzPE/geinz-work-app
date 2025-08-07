package com.geinzz.geinzwork.data.model.localizate_geinz.filtrado_tiendas

data class filtrado_tiendas_cat_sub(val categoria: String, val subcategorias: List<String>)

data class tiendas_filtradas(
    val logo_tienda: String = "",
    val img_tienda: List<String> = emptyList(),
    val nombre_tienda: String = "",
    val direccion: String = "",
    val referencia: String = "",
    val latitud: Double = 0.0,
    val longitud: Double = 0.0,
    val lista_subcategoiras: List<String> = emptyList(),
    val descripcion: String = "",
    val id_tienda: String = "",
)

data class EstadoFiltrosUi(
    val subcategorias: List<filtrado_tiendas_cat_sub> = emptyList(),
    val tiendasFiltradas: List<tiendas_por_categoria> = emptyList(),
)

data class tiendas_por_categoria(
    val nombre_tienda: String = "",
    val direccion: String = "",
    val referencia: String = "",
    val logo_tienda: String = "",
    val lista_subcategoiras: List<String> = emptyList(),
    val descripcion: String = "",
    val id_tienda: String = "",
)

