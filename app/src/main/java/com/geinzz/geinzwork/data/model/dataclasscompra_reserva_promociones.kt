package com.geinzz.geinzwork.model

data class dataclasscompra_reserva_promociones(
    //camposUSer
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
    val hora_entrega_llegada: String? = null,
    val fecha_entrega_llegada: String? = null,
    val idDriver: String? = null


)
