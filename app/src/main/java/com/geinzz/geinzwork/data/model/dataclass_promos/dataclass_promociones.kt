package com.geinzz.geinzwork.data.model.dataclass_promos

import com.google.firebase.Timestamp

data class promociones_tiendas_negocios(
    val id_tienda: String = "",
    val nombre_tienda: String = "",
    val url_img: String = "",
    val numero_contacto_teinda: String = "",
    val img_logo_tienda: String = "",
    val localidad: String = "",
    val categoria: String = "",
    val tipo_promo_o_notificaccion: String = "",
    val id_promocion_clikeable: String = "",
    val id_promocion_parametro_link:String="",
    val msje_predetermindao_whatsapp:String="",
    val fecha_caducidad_promocion: Timestamp= Timestamp.now()
)

data class datos_para_promocieons_activas(
    val id_tienda: String = "",
    val lugar: String = "",
    val index: String = "",
    val id_promo: String
)


