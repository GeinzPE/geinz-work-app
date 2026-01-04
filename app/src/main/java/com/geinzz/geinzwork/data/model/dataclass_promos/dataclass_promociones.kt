package com.geinzz.geinzwork.data.model.dataclass_promos

data class promociones_tiendas_negocios(
    val id_tienda: String = "",
    val nombre_tienda: String = "",
    val url_img: String = "",
    val numero_contacto_teinda: String = "",
    val img_logo_tienda: String = "",
    val localidad: String = "",
    val categoria: String = "",
)

data class datos_para_promocieons_activas(
    val id_tienda: String = "",
    val lugar: String = "",
    val index: String = "",
    val id_promo:String
)
