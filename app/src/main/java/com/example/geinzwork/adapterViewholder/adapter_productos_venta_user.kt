package com.example.geinzwork.adapterViewholder

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.example.geinzwork.constantesGeneral.constatnes_carga_imagenes_general
import com.example.geinzwork.dataclass.dataclassPorductosVerntaUser
import com.geinzz.geinzwork.databinding.ItemCustomTrabajadoresProductosBinding

class adapter_productos_venta_user(
    private val lista: MutableList<dataclassPorductosVerntaUser>,
    private val listener: (dataclassPorductosVerntaUser) -> Unit
) : RecyclerView.Adapter<adapter_productos_venta_user.viewHolderVentaProductos>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): viewHolderVentaProductos {
        val binding = ItemCustomTrabajadoresProductosBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false

        )
        return viewHolderVentaProductos(binding)
    }

    override fun getItemCount(): Int {
        return lista.size.coerceAtMost(6) // Muestra máximo 6 elementos
    }


    override fun onBindViewHolder(holder: viewHolderVentaProductos, position: Int) {
        val item = lista[position]
        holder.render(item)
    }


    inner class viewHolderVentaProductos(private val binding: ItemCustomTrabajadoresProductosBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun render(item: dataclassPorductosVerntaUser) {
            Log.d("RenderDebug", "Producto: ${item.id}, Descuento: ${item.descuento}, Descuento Número: ${item.descuentoNumero}")

            if (item.descuento == true) {
                binding.descuentoPorcentaje.text = "-${item.descuentoNumero.toString()}%"
                binding.descuentoPorcentaje.isVisible = true
            } else {
                binding.descuentoPorcentaje.isVisible = false
            }

            binding.imageView.setOnClickListener {
                listener(item)
                Log.d("RenderDebug", "Click en: ${item.id}, Descuento: ${item.descuento}, Descuento Número: ${item.descuentoNumero}")

                if (item.descuento == true) {
                    binding.descuentoPorcentaje.text = "-${item.descuentoNumero.toString()}%"
                    binding.descuentoPorcentaje.isVisible = true
                } else {
                    binding.descuentoPorcentaje.isVisible = false
                }
            }

            constatnes_carga_imagenes_general.changer_img(
                binding.progressCargaImagen,
                itemView.context,
                item.img.toString(),
                null,
                binding.imageView,
                "portada",
                null
            ) { complet -> }
        }

    }

}