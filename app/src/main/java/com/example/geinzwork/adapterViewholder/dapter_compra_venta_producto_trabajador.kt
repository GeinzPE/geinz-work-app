package com.example.geinzwork.adapterViewholder

import android.R
import android.text.SpannableString
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.example.geinzwork.NotificacionRS
import com.example.geinzwork.constantesGeneral.constatnes_carga_imagenes_general
import com.example.geinzwork.constantesGeneral.obtenertokenIdAdmin
import com.example.geinzwork.dataclass.dataclassPedidoCompraVenta
import com.geinzz.geinzwork.constantesGeneral.constantesCarrito
import com.geinzz.geinzwork.constantesGeneral.constantestextos_general
import com.geinzz.geinzwork.databinding.ItemCompraProductoTrabajadorBinding
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.model.Document
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.collections.component1
import kotlin.collections.component2
import kotlin.collections.iterator

class dapter_compra_venta_producto_trabajador(
    private val lista: MutableList<dataclassPedidoCompraVenta>,
    private val apartado: String
) :
    RecyclerView.Adapter<dapter_compra_venta_producto_trabajador.viewHolder_compra_venta>() {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): viewHolder_compra_venta {
        val binding = ItemCompraProductoTrabajadorBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return viewHolder_compra_venta(binding)
    }

    override fun onBindViewHolder(
        holder: viewHolder_compra_venta,
        position: Int
    ) {
        val item = lista[position]
        holder.render(item)
    }

    override fun getItemCount(): Int = lista.size

    inner class viewHolder_compra_venta(private val binding: ItemCompraProductoTrabajadorBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun render(item: dataclassPedidoCompraVenta) {
            when (apartado) {
                "compra" -> {
                    binding.aceptarVenta.isVisible = false
                    binding.rechazarVenta.isVisible = false
                    constantesCarrito.obtenerDatosVendedor_trabajadores(item.id_vendedor) { nombre, apellido, imgPerfil, localidad ->
                        constatnes_carga_imagenes_general.changer_img(
                            binding.progrsCargaImg,
                            itemView.context,
                            imgPerfil,
                            binding.imgCompradoVendedor,
                            null,
                            "perfil",
                            null
                        ) { cargado -> }
                        setear_datos_compra(
                            "Vendido por ",
                            "$nombre $apellido",
                            binding.compradoPor
                        )
                        binding.cantidadRestante.isVisible = false

                    }

                }

                "venta" -> {
                    binding.aceptarVenta.isVisible = true
                    binding.rechazarVenta.isVisible = true
                    constantesCarrito.obtenerDatosVendedor_trabajadores(item.id_usuario) { nombre, apellido, imgPerfil, localidad ->
                        constatnes_carga_imagenes_general.changer_img(
                            binding.progrsCargaImg,
                            itemView.context,
                            imgPerfil,
                            binding.imgCompradoVendedor,
                            null,
                            "perfil",
                            null
                        ) { cargado -> }
                        setear_datos_compra(
                            "Comprado por ",
                            "$nombre $apellido",
                            binding.compradoPor
                        )

                        binding.aceptarVenta.setOnClickListener {
                            aceptar_rechazar_compra(
                                item.id_usuario.toString(),
                                item,
                                "Tu compra fue aceptada 🎉",
                                "Hola $nombre \uD83D\uDC4B, tu pedido Nº ${item.id_venta} fue confirmado. Toca aquí para ver el estado de tu pedido."
                            )
                        }
                        binding.rechazarVenta.setOnClickListener {
                            aceptar_rechazar_compra(
                                item.id_usuario.toString(),
                                item,
                                "Tu compra fue rechazada 😞",
                                "Hola $nombre, lamentamos informarte que el vendedor rechazó tu pedido Nº ${item.id_venta}. Toca aquí para ver el motivo del rechazo. Gracias por usar Geinz Work."
                            )
                        }
                    }

                }
            }
            setear_datos_compra("ID Compra", item.id_venta, binding.idComprovante)
            constantesCarrito.obtener_datos_productos(
                item.id_vendedor,
                item.id_producto
            ) { nombre_prodcuto, precio_producto, stok_disponible ->
                setear_datos_compra(
                    "Cantidad comprada",
                    "${item.cantidad_adquirida}",
                    binding.cantidaComprada
                )
                setear_datos_compra(
                    "Nombre_producto",
                    "$nombre_prodcuto",
                    binding.tituloProducto
                )
                val cantidad_restante = calcular_stok_disponible(
                    item.cantidad_adquirida.toInt(),
                    stok_disponible?.toInt() ?: 0
                )
                setear_datos_compra(
                    "Stok disponible",
                    cantidad_restante.toString(),
                    binding.cantidadRestante
                )

            }

            setear_datos_compra(
                "Metodo de entrega selecionado",
                item.metodo_entrega,
                binding.metodoEntregaSelecionado
            )
            setear_datos_compra(
                "Metodo de pago selecionado",
                item.metodo_pago,
                binding.metodoPagoSelecionado
            )
            setear_datos_compra("Pagado", "${item.pagado}", binding.pagado)
            setear_datos_compra("Estado del pedido", item.estado_pedido, binding.estadoPedido)
            setear_datos_compra("Fecha ordenada", item.fecha_pedido, binding.fechaPedida)
            setear_datos_compra("Hora ordenada", item.hora_pedido, binding.horaPedida)


        }

        private fun aceptar_rechazar_compra(
            id_comprdor: String,
            item: dataclassPedidoCompraVenta,
            titulo: String, cuerpo: String
        ) {
            obtenertokenIdAdmin.obtenerTokensDispositivos_trabajador(
                id_comprdor,
                onSuccess = { tokensMap ->
                    val notificacionRS = NotificacionRS()
                    CoroutineScope(Dispatchers.IO).launch {
                        for ((_, token) in tokensMap) {
                            notificacionRS.enviarNotificacionFCM(
                                token,
                                "idAdmin",
                                "codigoCompra",
                                "nuevaVenta",
                                "entrada",
                                titulo,
                                cuerpo

                            )
                        }
                    }
                    cambiar_estado_aceptado_rechazado("aceptado", item)
                },
                onError = { error -> Log.e("FCM", "Error al obtener tokens: ${error.message}") }
            )

        }

        private fun cambiar_estado_aceptado_rechazado(
            aceptado_rechazado: String,
            item: dataclassPedidoCompraVenta
        ) {
            val dbVenta_compra_general = FirebaseFirestore.getInstance()
                .collection("Trabajadores_Usuarios_Drivers")
                .document("trabajadores").collection("compra_venta_trabajadores")
                .document(item.id_venta)
            val hasmap = hashMapOf<String, Any>(
                "estado_pedido" to aceptado_rechazado
            )
            dbVenta_compra_general.set(hasmap, SetOptions.merge()).addOnSuccessListener { res->
                Log.d("estado_cambiado","estado cambiado correctaemnte")
            }.addOnFailureListener { e->
                Log.e("estado_cambiado","Error al cambiar el estado")

            }
        }

        private fun calcular_stok_disponible(
            cantidad_comprada: Int,
            cantidad_Stok: Int
        ): Int {
            return cantidad_Stok - cantidad_comprada
        }


        private fun setear_datos_compra(
            texto_principal: String,
            dato_seteado: String,
            textView: TextView
        ) {
            val spannableString =
                SpannableString("$texto_principal : $dato_seteado")
            constantestextos_general.setearInformacionboldDescripcion(
                texto_principal,
                spannableString, textView
            )
        }
    }


}
