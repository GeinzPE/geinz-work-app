package com.geinzz.geinzwork.model

data class dataclassPorductosVerntaUser(
    var id: String?,
    val img: String?,
    val descuentoNumero: Number?,
    val descuento: Boolean?,
    val precio :Number?=null,
    val precioDescuento:Number?=null,
    val tituloProducto:String?=null

)
