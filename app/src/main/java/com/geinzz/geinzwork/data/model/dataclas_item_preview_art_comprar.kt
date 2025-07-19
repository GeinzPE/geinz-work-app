package com.geinzz.geinzwork.model

data class dataclas_item_preview_art_comprar(
    val id_trabajador: String,
    val metodo_entrega: String,
    val id:String?,
    val img: String?,
    val titulo: String?,
    val precio: Number? = null,
    val cantidad: Number? = null,
    val descuentoBoolena: Boolean? = null,
    val descuentoCantidad: Number? = null,
)