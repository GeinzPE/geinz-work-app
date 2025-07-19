package com.geinzz.geinzwork.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.geinzz.geinzwork.model.dataclasCaterogirasFiltrado
import com.geinzz.geinzwork.R
import com.geinzz.geinzwork.databinding.ItemFiltradoPromocionesBinding

class adapterCategoriasPromocionesFiltrado(
    private val lista: List<dataclasCaterogirasFiltrado>,
    private val listener: (dataclasCaterogirasFiltrado) -> Unit
) : RecyclerView.Adapter<adapterCategoriasPromocionesFiltrado.viewHolderFiltrado>() {

    private var selectedPosition: Int = 0

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): viewHolderFiltrado {
        val binding = ItemFiltradoPromocionesBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return viewHolderFiltrado(binding)
    }

    override fun getItemCount(): Int = lista.size

    override fun onBindViewHolder(holder: viewHolderFiltrado, position: Int) {
        val item = lista[position]
        holder.render(item, position)
    }

    inner class viewHolderFiltrado(private val binding: ItemFiltradoPromocionesBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun render(item: dataclasCaterogirasFiltrado, position: Int) {
            binding.nombreCategoria.text = item.nombreCategoria

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