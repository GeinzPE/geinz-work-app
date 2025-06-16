package com.geinzz.geinzwork.adapterViewholder

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.geinzz.geinzwork.databinding.ItemProductosTiendasBinding
import com.geinzz.geinzwork.dataclass.dataclassArticulos

class adapterProductos(
    private val mostrarTodo:Boolean,
    private val listaArticulos: MutableList<dataclassArticulos>,
    private val vermas: (dataclassArticulos) -> Unit
) : RecyclerView.Adapter<adapterProductos.viewHolder>(){


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int):viewHolder {
        val binding=ItemProductosTiendasBinding.inflate(LayoutInflater.from(parent.context),parent,false)
      return viewHolder(binding)
    }

    override fun getItemCount(): Int {
        return if (mostrarTodo) listaArticulos.size else if (listaArticulos.size >= 5) 5 else listaArticulos.size
    }

    override fun onBindViewHolder(holder: viewHolder, position: Int) {
        val item=listaArticulos[position]
        holder.render(item,vermas)
    }

    inner class viewHolder (private val binding:ItemProductosTiendasBinding) : RecyclerView.ViewHolder(binding.root){
        val imgProducto=binding.imgProducto
        val tituloProducto=binding.tituloProducto
        val descripcionProducto=binding.descripcionProducto
//        val precioProducto=binding.precioProducto
        val Vermas=binding.Editar
        fun render(dataclassArticulos: dataclassArticulos,vermas: (dataclassArticulos) -> Unit){
            try {
                Glide.with(itemView.context)
                    .load(dataclassArticulos.imgArt)
                    .into(imgProducto)
            }catch (e:Exception){
                println("no se pudo setear la img")
            }
            Vermas.setOnClickListener {
                vermas(dataclassArticulos)
            }
            tituloProducto.text=dataclassArticulos.nombreART
            descripcionProducto.text=dataclassArticulos.descripcion
//            precioProducto.text=dataclassArticulos.precio
        }
    }
}