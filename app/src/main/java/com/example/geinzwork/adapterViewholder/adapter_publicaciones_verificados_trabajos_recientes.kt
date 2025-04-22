package com.example.geinzwork.adapterViewholder

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.example.geinzwork.constantesGeneral.constatnes_carga_imagenes_general
import com.example.geinzwork.dataclass.dataclas_trabajos_ralizados_verificados
import com.geinzz.geinzwork.R
import com.geinzz.geinzwork.databinding.ItemProductosTiendasBinding

class adapter_publicaciones_verificados_trabajos_recientes(
    private val lista_publicaciones_vista_trabajos_recientes: MutableList<dataclas_trabajos_ralizados_verificados>,
    private val eliminarItem: (dataclas_trabajos_ralizados_verificados) -> Unit,
    private val editar_item: (dataclas_trabajos_ralizados_verificados) -> Unit
) : RecyclerView.Adapter<adapter_publicaciones_verificados_trabajos_recientes.adapter_publicaciones_verificados_trabajos_recientesHolder>() {


    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): adapter_publicaciones_verificados_trabajos_recientesHolder {
        val binding =
            ItemProductosTiendasBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return adapter_publicaciones_verificados_trabajos_recientesHolder(binding)
    }

    override fun getItemCount(): Int {
        return lista_publicaciones_vista_trabajos_recientes.size
    }

    override fun onBindViewHolder(
        holder: adapter_publicaciones_verificados_trabajos_recientesHolder,
        position: Int
    ) {
        val item = lista_publicaciones_vista_trabajos_recientes[position]
        holder.render(item, eliminarItem, editar_item)
    }

    inner class adapter_publicaciones_verificados_trabajos_recientesHolder(private val binding: ItemProductosTiendasBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun render(
            item: dataclas_trabajos_ralizados_verificados,
            eliminarItem: (dataclas_trabajos_ralizados_verificados) -> Unit,
            editarItem: (dataclas_trabajos_ralizados_verificados) -> Unit,
        ) {
            binding.precioProducto.isVisible = false
            binding.tituloProducto.text = item.titulo
            binding.descripcionProducto.text = item.contenido
            binding.pen.isVisible = false

            val placeholderperfil =
                ContextCompat.getDrawable(itemView.context, R.drawable.cargando_img_geinz_500)
            constatnes_carga_imagenes_general.changer_img(
                binding.progressCargaImagen,
                itemView.context,
                item.img.toString(),
                null,
                binding.imgProducto,
                "portada", placeholderperfil
            ) {}

            binding.linealPublicacionesEditDelete.isVisible = true
            binding.borrarItem.setOnClickListener {
                eliminarItem(item)
            }
            binding.editar.setOnClickListener {
                editarItem(item)
            }
            binding.linealvistaCompartidasPublicacion.isVisible = true
            binding.cantidadView.text = item.vista.toString()
            binding.cantidadCompartida.text = item.compartidas.toString()
            binding.cantidadClicks.text = item.cliks.toString()
        }

    }

}