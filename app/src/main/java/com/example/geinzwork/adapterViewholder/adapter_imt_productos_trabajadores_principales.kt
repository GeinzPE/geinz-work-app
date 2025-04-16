package com.example.geinzwork.adapterViewholder

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.example.geinzwork.constantesGeneral.constatnes_carga_imagenes_general
import com.example.geinzwork.dataclass.dataclass_productos_info_trabajadores_principal
import com.geinzz.geinzwork.databinding.ItemProductsTrabajadoresPrincipalBinding

class adapter_imt_productos_trabajadores_principales(private val lista: MutableList<dataclass_productos_info_trabajadores_principal>) :
    RecyclerView.Adapter<adapter_imt_productos_trabajadores_principales.viewHolder_productos>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): viewHolder_productos {
        val binding = ItemProductsTrabajadoresPrincipalBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return viewHolder_productos(binding)
    }

    override fun getItemCount(): Int {
        return lista.size
    }

    override fun onBindViewHolder(holder: viewHolder_productos, position: Int) {
        val item = lista[position]
        holder.render(item)
    }

    inner class viewHolder_productos(private val binding: ItemProductsTrabajadoresPrincipalBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun render(item: dataclass_productos_info_trabajadores_principal) {
            val imgProducto = binding.imgProducto
            val precio_producto = binding.precioProducto
            val precio_descuento = binding.precioDescuento
            val envio_gratis = binding.envioGratis
            val titulo = binding.tituloProducto
            val descripcion = binding.descripcionProducto
            val progresVar = binding.cargarContenido

            constatnes_carga_imagenes_general.changer_img(
                binding.cargaImg,
                itemView.context,
                item.img_productos.toString(),
                null,
                imgProducto as ImageView,
                "portada",
                null
            ) { c ->

            }
            titulo.text = item.nombre.toString()
            descripcion.text = item.descripcion_producto.toString()
            binding.imgProducto.isVisible = true
            binding.linealProductosPublicados.isVisible=true
        }

    }
}