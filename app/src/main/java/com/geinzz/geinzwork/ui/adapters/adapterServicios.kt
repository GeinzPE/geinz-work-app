package com.geinzz.geinzwork.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.geinzz.geinzwork.databinding.ItemServiciosBinding
import com.geinzz.geinzwork.model.dataclassServicios

class adapterServicios(
    private var listaTrabajos: MutableList<dataclassServicios>,
    private val vermas: (dataclassServicios) -> Unit,
) : RecyclerView.Adapter<adapterServicios.viewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): viewHolder {
        val binding =
            ItemServiciosBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return viewHolder(binding)
    }

    override fun getItemCount(): Int {
        return listaTrabajos.size
    }

    override fun onBindViewHolder(holder: viewHolder, position: Int) {
        val item = listaTrabajos[position]
        holder.render(item, vermas)
    }

    inner class viewHolder(private val binding: ItemServiciosBinding) :
        RecyclerView.ViewHolder(binding.root) {
        val imgServicio = binding.imgTrabajo
        val nombreServicio = binding.nombreServicio
        val precio = binding.precio
        val listner=binding.listener

        fun render(dataclassServicios: dataclassServicios, vermas: (dataclassServicios) -> Unit) {
            try {
                Glide.with(itemView.context)
                    .load(dataclassServicios.imgServicio)
                    .into(imgServicio)
            } catch (e: Exception) {
                println("no se pudo setear la img")
            }
            precio.text = dataclassServicios.precio.toString()
            nombreServicio.text = dataclassServicios.titulo
            listner.setOnClickListener { vermas(dataclassServicios) }
        }
    }

}