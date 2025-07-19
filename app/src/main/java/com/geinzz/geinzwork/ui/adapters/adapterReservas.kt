package com.geinzz.geinzwork.ui.adapters


import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.geinzz.geinzwork.utils.constantes.constantes.constantesCarrito
import com.geinzz.geinzwork.databinding.ItemReservaBinding
import com.geinzz.geinzwork.model.dataclassreservas
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase


class adapterReservas(
    private var listaReserva: MutableList<dataclassreservas>,
    private val generarQR: (dataclassreservas) -> Unit
) : RecyclerView.Adapter<adapterReservas.ViewHolder>() {
    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemReservaBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return listaReserva.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = listaReserva[position]
        holder.render(item, generarQR)
    }

    inner class ViewHolder(private val binding: ItemReservaBinding) : RecyclerView.ViewHolder(binding.root) {

        fun render(dataclassreservas: dataclassreservas, generarQR: (dataclassreservas) -> Unit) {
            firebaseAuth = FirebaseAuth.getInstance()
            when (dataclassreservas.metodoEntrega.toString()) {
                "Recojo en tienda" -> {
                    binding.direccionentregaLineal.isVisible = false
                    binding.linealdescripcion.isVisible = false
                }

                "Delivery" -> {
                    binding.direccionentregaLineal.isVisible = true
                    binding.linealdescripcion.isVisible = true
                }
            }
            binding.apply {
                descripcionus.text = dataclassreservas.descripcionEntrega
                direcionEntrega.text = dataclassreservas.direccionEntrega
                TipoTienda.text = dataclassreservas.tipoTienda
                localidaduser.text = dataclassreservas.localidaUser
                localidadTienda.text = dataclassreservas.localidaTienda
                codigoPedido.text = dataclassreservas.condigoP
                nombreTienda.text = dataclassreservas.nombreTienda
                nombreuser.text = dataclassreservas.nombre
                numerous.text = dataclassreservas.numero
                horaus.text = dataclassreservas.hora
                fechaus.text = dataclassreservas.fecha
                entregaMetodo.text = dataclassreservas.metodoEntrega
                fechaentrega.text = dataclassreservas.fechaEntrega
                horaaproximanda.text = dataclassreservas.horaEntrega
                estadous.text = dataclassreservas.estado
                tituloItem.text = dataclassreservas.tituloProducto
                precioItem.text = dataclassreservas.precio
                cantidad.text = dataclassreservas.cantidad
                total.text = dataclassreservas.total
                reserva.text = dataclassreservas.reserva
                TipoRerserva.text=dataclassreservas.TipoReserva
                containerListenr.setOnClickListener {
                    generarQR(dataclassreservas)
                }
                cancelar.setOnClickListener {
                    cancelarPedidofun(dataclassreservas)
                }
            }

            constantesCarrito.obtnerFotoPErfilTienda(
                itemView.context, dataclassreservas.idTienda.toString(), binding.imgTienda
            )
            constantesCarrito.obtenerIMGproducto(
                dataclassreservas.idTienda.toString(),
                dataclassreservas.idproducto.toString(),
                binding.imgItem,
                itemView.context
            )
        }

        private fun cancelarPedidofun(dataclassreservas: dataclassreservas) {
            val db = FirebaseDatabase.getInstance().getReference("PedidosUsuario")
                .child(firebaseAuth.uid.toString()).child("tiendas")
                .child(dataclassreservas.idTienda.toString()).child("reserva")
                .child(dataclassreservas.condigoP.toString())

            db.removeValue().addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(
                        itemView.context,
                        "Reserva cancelada correctamente.",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    Toast.makeText(
                        itemView.context,
                        "Error al cancelar el pedido: ${task.exception?.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }
}
