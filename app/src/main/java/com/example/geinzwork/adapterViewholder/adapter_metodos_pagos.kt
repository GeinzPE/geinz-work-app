package com.example.geinzwork.adapterViewholder

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.geinzwork.dataclass.dataclass_metodos_pagos
import com.geinzz.geinzwork.databinding.MetodosPagosItemBinding

class adapter_metodos_pagos(private val lista: MutableList<dataclass_metodos_pagos>,private val listaner:(dataclass_metodos_pagos)->Unit) :
    RecyclerView.Adapter<adapter_metodos_pagos.viewHolderMetodosPagos>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): viewHolderMetodosPagos {
        val binding = MetodosPagosItemBinding.inflate(LayoutInflater.from(parent.context))
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

            binding.linealMetodos.setOnClickListener {
                listaner(item)
            }
            binding.metodosPagosSelecionadso.text = metodosSeleccionados

            binding.tituloReferencia.text = item.nombre_coelccion
        }

    }

}