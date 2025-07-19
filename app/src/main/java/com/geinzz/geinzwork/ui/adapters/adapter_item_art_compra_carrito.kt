package com.geinzz.geinzwork.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.geinzz.geinzwork.model.dataclas_item_preview_art_comprar
import com.geinzz.geinzwork.databinding.ItemPreviewArtComprarCarritoBinding

class adapter_item_art_compra_carrito(private var lista: MutableList<dataclas_item_preview_art_comprar>) :
    RecyclerView.Adapter<adapter_item_art_compra_carrito.vieHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): vieHolder {
        val binding = ItemPreviewArtComprarCarritoBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return vieHolder(binding)
    }

    override fun getItemCount(): Int {
        return lista.size
    }

    override fun onBindViewHolder(holder: vieHolder, position: Int) {
        val item = lista[position]
        holder.render(item)
    }

    inner class vieHolder(private val binding: ItemPreviewArtComprarCarritoBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun render(item: dataclas_item_preview_art_comprar) {
            binding.tituloItem.text=item.titulo
            binding.precioItem.text=item.precio.toString()
            binding.cantidad.text=item.cantidad.toString()
            try {
                Glide.with(itemView.context)
                    .load(item.img)
                    .into(binding.imgItem)
            }catch (e:Exception){
                println("error al setear la img $e")
            }
            try {
                val precio = item.precio ?: 0.0
                val cantidad = item.cantidad ?: 0.0

//                val totalProducto = precio * cantidad
//                binding.totalProducto.text=totalProducto.toString()

            } catch (e: NumberFormatException) {
                println("Error al convertir el precio o la cantidad: ${e.message}")
            }
        }

    }
}