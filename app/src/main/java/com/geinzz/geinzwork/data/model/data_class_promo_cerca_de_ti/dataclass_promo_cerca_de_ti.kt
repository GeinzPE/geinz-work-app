package com.geinzz.geinzwork.data.model.data_class_promo_cerca_de_ti

import android.R

data class obj_completo(
    val dataclass_promociones_cerca_de_ti: dataclass_promociones_cerca_de_ti,
    val lista_filtrado: List<String>,
    val lista_tiendas_con_mas_promo: List<tiendas_con_mas_de_una_promo>
)

data class tiendas_con_mas_de_una_promo(
    val id: String,
    val logo_img: String,
    val nombre_tienda: String
)

data class dataclass_promociones_cerca_de_ti(
    val informacion_publcacion: informacion_publcacion,
    val img: img_content,
    val exclussivo: Boolean,
    val dias_restantes: Int,
)

data class img_content(
    val logo_img: String,
    val lista_img: List<String>,
)

data class informacion_publcacion(
    val descripcion: String,
    val numero: String,
    val titulo: String,
    val nombre_tienda: String,
    val id_promocion: String,
    val id_tienda: String,
    val categoria: String,
    val compartir: Boolean,
    val contactar: Boolean,
)

data class ubicacion(
    val direccion: String,
    val lat: Number,
    val lng: Number,
    val ref: String
)

