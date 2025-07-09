package com.example.geinzwork.adapterViewholder

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.example.geinzwork.constantesGeneral.constatnes_carga_imagenes_general
import com.example.geinzwork.dataclass.dataclass_ver_mas_productos_trabajador
import com.geinzz.geinzwork.constantesGeneral.constantes_publicaciones_general_user_tiendas.obtener_metodoEntrega
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

    fun dpToPx(context: Context, dp: Float): Int {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP, dp, context.resources.displayMetrics
        ).toInt()
    }


    override fun onBindViewHolder(holder: viewHolderPorductosPublicados, position: Int) {
        val item = lista[position]

        // Si quieres variar el tamaño de la imagen según la posición, puedes cambiar este valor
        val context = holder.itemView.context
        val maxImgHeightDp = if (position % 2 == 0) 200f else 150f
        val maxImgHeightPx = dpToPx(context, maxImgHeightDp)

        // Aplicar la altura máxima (opcional, en caso de que uses wrap_content + maxHeight)
        holder.getBinding().imgTrabajo.apply {
            layoutParams.height = maxImgHeightPx
            requestLayout() // Asegura que se redibuje
        }

        // Renderizar los datos
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
            obtener_metodoEntrega(
                item.id_trabajador.toString(), item.metodo_envio.toString(),
                callback = { metodo_entrega ->
                },
                evio_gratis = { delivery_gratis ->
                    if (delivery_gratis) {
                        binding.envioGratis.isVisible = true
                    } else {
                        binding.envioGratis.isVisible = false
                    }
                }
            )

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
                binding.precioDescuento.isVisible = false
                binding.descuentoPorcentaje.isVisible = false
            }

        }
    }

}