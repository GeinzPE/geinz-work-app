package com.geinzz.geinzwork.data.model.localizate_geinz

import org.apache.commons.logging.Log

data class dataclass_horarios_atencion_tiendas(
    val id_tienda: String?,
    val localidad_tienda: String?,
    val dia: String?,
    val h_apertura: String?,
    val h_cierre: String?,
    val categoria_tienda: String?,
    val subcategoria: String?
)

data class estadoTienda(
    val id_tienda: String?,
    val localidad_tienda: String?,
    val abierto_cerrado: Boolean?,
    val categoria: String?,
    val subcategoria: String?,
    val tiempo_cerrado: String?
)

data class encontradas_por_categoria(
    val cantidad_registradas: Int?,
    val activas: Int?,
    val categoria: String?,
    val subcateogiras: List<String>,
    val img_subcategorias: List<String>
)

data class horario_tienda(
    val id_tienda: String,
    val dia: String?,
    val h_apertura: String?,
    val h_cierre: String?
)

data class registradas_activas_cat_img(
    val cantidad_registradas: Int?,
    val catidad_activas: Int?,
    val subcategoria: String?,
    val img_subcategorias: List<String>
)

data class tienda_patrocinada(
    val categoria_tienda: String?,
    val id_tienda: String?,
    val img_tienda: String?,
    val nombre: String?,
    val latitud : Number?,
    val longitud: Number?,
    val direccion: String?,
    val referencia: String?
)
