package com.geinzz.geinzwork.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.geinzz.geinzwork.model.dataclasFiltradoTrabaadores
import com.geinzz.geinzwork.R
import com.geinzz.geinzwork.databinding.ItemFiltradoPromocionesBinding

class adapterFiltradoTrabajadoresVerMAs(
    private val lista: MutableList<dataclasFiltradoTrabaadores>,
    private val listener: (dataclasFiltradoTrabaadores) -> Unit
) :
    RecyclerView.Adapter<adapterFiltradoTrabajadoresVerMAs.viewHolder>() {
    private var selectedPosition: Int = 0


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): viewHolder {
        val bindig =
            ItemFiltradoPromocionesBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        return viewHolder(bindig)
    }

    override fun getItemCount(): Int = lista.size

    override fun onBindViewHolder(holder: viewHolder, position: Int) {
        val item = lista[position]
        holder.render(item, position)
    }

    inner class viewHolder(private val binding: ItemFiltradoPromocionesBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun render(item: dataclasFiltradoTrabaadores, position: Int) {

            binding.nombreCategoria.text = item.nombreCat

            if (position == selectedPosition) {
                binding.nombreCategoria.setBackgroundResource(R.drawable.item_roun_carpetas_oscuro) // Fondo seleccionado
            } else {
                binding.nombreCategoria.setBackgroundResource(R.drawable.roun_item_carpetas) // Fondo normal
            }


            binding.nombreCategoria.setOnClickListener {
                if (selectedPosition != position) {
                    val previousPosition = selectedPosition
                    selectedPosition = position
                    if (previousPosition != RecyclerView.NO_POSITION) {
                        notifyItemChanged(previousPosition)
                    }
                    notifyItemChanged(selectedPosition)
                }


                listener(item)
            }

        }

    }
}