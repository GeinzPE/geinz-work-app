package com.geinzz.geinzwork.model

data class dataclassPedidoCompraVenta (
    val cantidad_adquirida: Number = 0,
    val codigo_compra: String = "",
//    val datos_delivery: R.string? = null,
    val estado_pedido: String = "",
    val fecha_pedido: String = "",
    val hora_pedido: String = "",
    val id_producto: String = "",
    val id_usuario: String = "",
    val id_vendedor: String = "",
    val id_venta: String = "",
    val metodo_entrega: String = "",
    val metodo_pago: String = "",
    val observaciones: String = "",
    val pagado: Boolean = false,
    val total_cancelar: Number = 0
)