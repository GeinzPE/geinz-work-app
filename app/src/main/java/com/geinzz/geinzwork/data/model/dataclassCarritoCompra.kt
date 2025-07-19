package com.geinzz.geinzwork.model

data class dataclassCarritoCompra(
    //nuevos cambios
    val idPedido: String?,
    val idTienda: String?,
    val idUser: String?,
    val idRef_user: String? = null,
    val metodoEntrega: String?,
    val metodoPago: String?,
    val precioDelivery: Double?,
    val productos: String?,
    val tipoRealizado: String?,
    val totalCancelar: Double?,
    val totalDriver: Double?,
    val totalProductos: Double?,
    val estado: String?,
    val estadoPedido: String?,
    val fecha: String?,
    val hora: String?,
    val idDriver: String? = null
)