package com.geinzz.geinzwork.ui.adapters

import android.text.SpannableString
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.geinzz.geinzwork.model.dataclass_metodos_entrega
import com.geinzz.geinzwork.utils.constantes.constantes.constantestextos_general
import com.geinzz.geinzwork.databinding.LayoutMetodoEntregaBinding

class adapter_metodos_entrega(
    private val lista: MutableList<dataclass_metodos_entrega>,
    private val eliminar: (dataclass_metodos_entrega) -> Unit,
    private val editar: (dataclass_metodos_entrega) -> Unit
) : RecyclerView.Adapter<adapter_metodos_entrega.viewHolderMetodoEntrega>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): viewHolderMetodoEntrega {
        val binding =
            LayoutMetodoEntregaBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return viewHolderMetodoEntrega(binding)
    }

    override fun getItemCount(): Int = lista.size

    override fun onBindViewHolder(holder: viewHolderMetodoEntrega, position: Int) {
        val item = lista[position]
        holder.render(item, eliminar, editar)
    }

    class viewHolderMetodoEntrega(private val binding: LayoutMetodoEntregaBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun render(
            item: dataclass_metodos_entrega,
            eliminar: (dataclass_metodos_entrega) -> Unit,
            editar: (dataclass_metodos_entrega) -> Unit
        ) {


            val estados = mapOf(
                "Delivery" to item.delivery,
                "Coordinar" to item.coordinar,
                "Lugares de Entrega" to item.lugaresEntrega,
                "Retiro en Tienda" to item.retiroTienda,
                "Envio Courier" to item.envioCourier,
                "Entrega Programada" to item.entregaProgramada
            )
            binding.listener.setOnLongClickListener {
                eliminar(item)
                true
            }
            binding.listener.setOnClickListener {
                editar(item)
            }
            val metodosActivos = estados.filter { it.value == true }.keys

            val textoMetodos = if (metodosActivos.isNotEmpty()) {
                metodosActivos.joinToString(", ")
            } else {
                "sin métodos activos"
            }


            val nombre_metodo = SpannableString("Nombre de metodo : ${item.nombre_metodo}")
            constantestextos_general.setearInformacionboldDescripcion(
                "Nombre de metodo",
                nombre_metodo, binding.nombreMetodo
            )

            val entregametodo =
                SpannableString("Metodos de entrega seleccionados : ${textoMetodos}")
            constantestextos_general.setearInformacionboldDescripcion(
                "Metodos de entrega seleccionados",
                entregametodo, binding.metodoEncontrado
            )
        }
    }

}

