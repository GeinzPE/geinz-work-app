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

class adapter_publicaciones_venta(
    private val lista_publicaciones_venta: MutableList<dataclas_trabajos_ralizados_verificados>,
    private val editar_eliminar_estadi_archivar: (dataclas_trabajos_ralizados_verificados) -> Unit
) : RecyclerView.Adapter<adapter_publicaciones_venta.ViewHolder_publi_venta>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder_publi_venta {
        val binding =
            ItemProductosTiendasBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder_publi_venta(binding)
    }

    override fun getItemCount(): Int {
        return lista_publicaciones_venta.size
    }

    override fun onBindViewHolder(holder: ViewHolder_publi_venta, position: Int) {
        val item = lista_publicaciones_venta[position]
        holder.render(item, editar_eliminar_estadi_archivar)
    }

    inner class ViewHolder_publi_venta(private val binding: ItemProductosTiendasBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun render(
            item: dataclas_trabajos_ralizados_verificados,
            editarEliminarEstadiArchivar: (dataclas_trabajos_ralizados_verificados) -> Unit
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

            binding.listenerPadre.setOnLongClickListener {
                editar_eliminar_estadi_archivar(item)
                true
            }

//            binding.linealvistaCompartidasPublicacion.isVisible = true
//            binding.cantidadView.text = item.vista.toString()
//            binding.cantidadCompartida.text = item.compartidas.toString()
//            binding.cantidadClicks.text = item.cliks.toString()
            TODO("Not yet implemented")
        }

    }
}