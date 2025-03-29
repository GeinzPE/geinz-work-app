package com.example.geinzwork.adapterViewholder

import android.graphics.Paint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.example.geinzwork.constantesGeneral.constatnes_carga_imagenes_general
import com.example.geinzwork.dataclass.dataclass_ver_mas_productos_trabajador
import com.geinzz.geinzwork.databinding.ItemProductosTrabajadorRecycleviewBinding

class adapter_ver_mas_productos_publicados(
    private val lista: MutableList<dataclass_ver_mas_productos_trabajador>,
    private val listener: (dataclass_ver_mas_productos_trabajador) -> Unit
) :
    RecyclerView.Adapter<adapter_ver_mas_productos_publicados.viewHolderPorductosPublicados>() {


    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): viewHolderPorductosPublicados {
        val binding = ItemProductosTrabajadorRecycleviewBinding.inflate(
            LayoutInflater.from(parent.context),
            parent, false
        )
        return viewHolderPorductosPublicados(binding)
    }

    override fun getItemCount(): Int = lista.size

    override fun onBindViewHolder(holder: viewHolderPorductosPublicados, position: Int) {
        val item = lista[position]
        holder.render(item)
    }

    inner class viewHolderPorductosPublicados(private val binding: ItemProductosTrabajadorRecycleviewBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun render(item: dataclass_ver_mas_productos_trabajador) {
            binding.btnVermas.setOnClickListener { listener(item) }
            constatnes_carga_imagenes_general.changer_img(
                binding.progressCargaImagen,
                itemView.context,
                item.img ?: "", // Evitar problemas de nulos
                null,
                binding.imgTrabajo,
                "portada",
                null
            ) {}

            binding.descripcionProducto.text = item.descripcionPRD ?: ""

            if (item.descuentoProducto == true) {
                binding.descuentoPorcentaje.isVisible = true
                binding.descuentoProducto.apply {
                    paintFlags = paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
                    textSize = 12f
                    text = "S/${item.precioPRD}.00"
                }
                binding.precioProducto.text = "S/${item.precioDescuento}.00"
                binding.descuentoPorcentaje.text = "-${item.descuentoTotalNumber}%"
            } else {
                binding.precioProducto.text = "S/${item.precioPRD}.00"
                binding.descuentoPorcentaje.isVisible = false
            }

            binding.envioGratis.isVisible = item.envioGRT == true
        }

    }
}