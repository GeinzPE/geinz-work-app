package com.geinzz.geinzwork.ui.adapters

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.geinzz.geinzwork.databinding.ItemTrabajosMostradosBinding
import com.geinzz.geinzwork.model.dataClassCategoriasTiendas

class adapterCategoriasTiendas(
    private val lista: MutableList<dataClassCategoriasTiendas>,
    private val vermas: (dataClassCategoriasTiendas) -> Unit
) : RecyclerView.Adapter<adapterCategoriasTiendas.viewHoldeCategoriasTrabajos>() {

    @SuppressLint("SuspiciousIndentation")
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): viewHoldeCategoriasTrabajos {
        val binding =
            ItemTrabajosMostradosBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return viewHoldeCategoriasTrabajos(binding)
    }

    override fun getItemCount(): Int {
        return lista.size
    }

    @SuppressLint("SuspiciousIndentation")
    override fun onBindViewHolder(holder: viewHoldeCategoriasTrabajos, position: Int) {
        val item = lista[position]
        holder.render(item, vermas)
    }

    inner class viewHoldeCategoriasTrabajos(private val binding: ItemTrabajosMostradosBinding) :
        RecyclerView.ViewHolder(binding.root) {
        val img = binding.imgTrabajo
        val nombre = binding.titulo
        val tipoT = binding.tiposT
        val layaout = binding.LinealVermas
        fun render(
            dataClassCategoriasTiendas: dataClassCategoriasTiendas,
            vermas: (dataClassCategoriasTiendas) -> Unit
        ) {
            layaout.setOnClickListener {
                vermas(dataClassCategoriasTiendas)
            }

            tipoT.text = dataClassCategoriasTiendas.tipoT
            try {
                Glide.with(itemView.context)
                    .load(dataClassCategoriasTiendas.imgResId)
                    .into(img)
            } catch (e: Exception) {
                println("error al setear la img")
            }
            nombre.text = dataClassCategoriasTiendas.categorias
        }
    }
}