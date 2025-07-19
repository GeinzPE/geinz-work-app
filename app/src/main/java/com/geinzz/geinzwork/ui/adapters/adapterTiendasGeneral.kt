package com.geinzz.geinzwork.ui.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.geinzz.geinzwork.databinding.ItemGeneralTiendaBinding
import com.geinzz.geinzwork.model.dataclass_item_general_tienda

class adapterTiendasGeneral(
    private val listaTiendas: MutableList<dataclass_item_general_tienda>,
    private val ver_mas: (dataclass_item_general_tienda) -> Unit,
    private val context: Context
) : RecyclerView.Adapter<adapterTiendasGeneral.viewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): viewHolder {
        val binding=ItemGeneralTiendaBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return viewHolder(binding)
    }

    override fun getItemCount(): Int {
        return if (listaTiendas.size >= 6) 6 else listaTiendas.size
    }

    override fun onBindViewHolder(holder: viewHolder, position: Int) {
        val item = listaTiendas[position]
        holder.render(item, ver_mas,context)
    }

    inner class viewHolder(private val binding:ItemGeneralTiendaBinding) : RecyclerView.ViewHolder(binding.root) {
        val imgPerfil = binding.imgPerfilUser
        val imgPortada = binding.imgPortadaTienda
        val nombreTienda = binding.nombreTienda
        val categoriaTienda = binding.categoriaTienda
        val calificacionTienda = binding.calificacionTienda
        val estadoTienda = binding.estadoTienda
        val zonaTienda = binding.zonaTienda
        val container = binding.listenerPadre
        val localidadTienda = binding.localidadTienda

        fun render(dataclassItemGeneralTienda:dataclass_item_general_tienda, verMas: (dataclass_item_general_tienda) -> Unit,context: Context) {
            container.setOnClickListener {
                verMas(dataclassItemGeneralTienda)
            }
            nombreTienda.text=dataclassItemGeneralTienda.nombreTienda
            calificacionTienda.text=dataclassItemGeneralTienda.estrellas
            categoriaTienda.text=dataclassItemGeneralTienda.categoria
            estadoTienda.text=dataclassItemGeneralTienda.estado
            zonaTienda.text=dataclassItemGeneralTienda.zona
            localidadTienda.text = dataclassItemGeneralTienda.localidad
            try {
                Glide.with(context)
                    .load(dataclassItemGeneralTienda.imgPerfil)
                    .into(imgPerfil)

                Glide.with(context)
                    .load(dataclassItemGeneralTienda.imgPortada)
                    .into(imgPortada)
            } catch (e: Exception) {
                println("error al setear la img")
            }
        }
    }

}