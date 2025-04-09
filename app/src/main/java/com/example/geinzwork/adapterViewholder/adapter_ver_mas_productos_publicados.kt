package com.example.geinzwork.adapterViewholder

import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.example.geinzwork.constantesGeneral.constatnes_carga_imagenes_general
import com.example.geinzwork.dataclass.dataclass_ver_mas_productos_trabajador
import com.geinzz.geinzwork.constantesGeneral.constantestextos_general
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

        // Controlar el alto total y la altura de la imagen
        val totalHeight = if (position % 2 == 0) 950 else 850
        val imgHeight = (totalHeight * 0.65).toInt() // 65% para la imagen
        val contentHeight = totalHeight - imgHeight

        // Ajustar la altura de las vistas a través de holder.getBinding()
        holder.itemView.layoutParams.height = totalHeight
        holder.getBinding().imgTrabajo.layoutParams.height = imgHeight
        holder.getBinding().linealCargandoDatos.layoutParams.height = contentHeight

        holder.render(item)
    }


    inner class viewHolderPorductosPublicados(private val binding: ItemProductosTrabajadorRecycleviewBinding) :
        RecyclerView.ViewHolder(binding.root) {

        // Método para acceder al binding
        fun getBinding() = binding

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

            Handler(Looper.getMainLooper()).postDelayed({
                binding.btnVermas.isVisible = true
                binding.cargadoContenido.isVisible = false
            }, 2000)
            binding.nombreProducto.text = item.nombreProducto
            if (item.descuentoProducto == true) {
                binding.descuentoPorcentaje.isVisible = true
                constantestextos_general.setearPrecioDescuentoPrecioAntiguo(
                    item.precioPRD,
                    binding.precioProducto,
                    item.precioDescuento,
                    binding.precioDescuento,
                    item.descuentoTotalNumber,
                    binding.descuentoPorcentaje
                )
                binding.precioDescuento.isVisible = true
                constantestextos_general.marcarDescuentoTxt(binding.precioDescuento)
            } else {
                constantestextos_general.setearPrecioDescuentoPrecioAntiguo(
                    item.precioPRD,
                    binding.precioProducto
                )
                binding.precioDescuento.isVisible = true
                binding.descuentoPorcentaje.isVisible = false
            }

            binding.envioGratis.isVisible = item.envioGRT == true
        }
    }

}