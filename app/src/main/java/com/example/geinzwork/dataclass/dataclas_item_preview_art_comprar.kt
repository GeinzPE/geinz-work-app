package com.example.geinzwork.dataclass

data class dataclas_item_preview_art_comprar(
    val img: String?,
    val titulo: String?,
    val precio: Double? = null,
    val cantidad: Int? = null,
    val descuentoBoolena: Boolean? = null,
    val descuentoCantidad: Number? = null
)