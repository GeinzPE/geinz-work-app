package com.geinzz.geinzwork.ui.adapters

import android.text.SpannableString
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.geinzz.geinzwork.model.dataclass_metodos_pagos
import com.geinzz.geinzwork.utils.constantes.constantes.constantestextos_general
import com.geinzz.geinzwork.databinding.MetodosPagosItemBinding

class adapter_metodos_pagos(
    private val lista: MutableList<dataclass_metodos_pagos>,
    private val eliminar: (dataclass_metodos_pagos) -> Unit,
    private val editar: (dataclass_metodos_pagos) -> Unit
) :
    RecyclerView.Adapter<adapter_metodos_pagos.viewHolderMetodosPagos>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): viewHolderMetodosPagos {
        val binding = MetodosPagosItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return viewHolderMetodosPagos(binding)
    }

    override fun getItemCount(): Int = lista.size

    override fun onBindViewHolder(holder: viewHolderMetodosPagos, position: Int) {
        val item = lista[position]
        holder.render(item)
    }

    inner class viewHolderMetodosPagos(private val binding: MetodosPagosItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun render(item: dataclass_metodos_pagos) {
            val metodosSeleccionados = listOfNotNull(
                if (item.yape == true) "yape" else null,
                if (item.efectivo == true) "efectivo" else null,
                if (item.plin == true) "plin" else null,
                if (item.trasnferencia == true) "transferencia" else null
            ).joinToString(", ")

            if (item.yape == true) {
                binding.logoYape.isVisible = true
            }
            if (item.efectivo == true) {
                binding.logoEfectivo.isVisible = true
            }
            if (item.plin == true) {
                binding.logoPlin.isVisible = true
            }
            if (item.trasnferencia == true) {
                binding.logoTransferencia.isVisible = true
            }
            binding.linealMetodos.setOnLongClickListener {
                eliminar(item)
                true
            }
            binding.linealMetodos.setOnClickListener {
                editar(item)
            }

            val spannableString = SpannableString("Nombre de metodo : ${item.nombre_coelccion}")
            constantestextos_general.setearInformacionboldDescripcion(
                "Nombre de metodo",
                spannableString, binding.tituloReferencia
            )

            val spannableStringpagoSelect =
                SpannableString("Pagos seleccionados : ${metodosSeleccionados}")
            constantestextos_general.setearInformacionboldDescripcion(
                "Pagos seleccionados",
                spannableStringpagoSelect, binding.metodosPagosSelecionadso
            )
        }

    }

}