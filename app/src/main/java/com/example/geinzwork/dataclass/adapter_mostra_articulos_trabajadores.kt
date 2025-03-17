package com.example.geinzwork.dataclass



import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.example.geinzwork.constantesGeneral.constatnes_carga_imagenes_general
import com.example.geinzwork.dataclass.dataclas_item_preview_art_comprar
import com.geinzz.geinzwork.databinding.ItemProductosUsuariosVerificadosBinding

class adapter_mostra_articulos_trabajadores(private val lista: MutableList<dataclas_item_preview_art_comprar>) :
    RecyclerView.Adapter<adapter_mostra_articulos_trabajadores.viewholderMostrarArticulos>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): viewholderMostrarArticulos {
        val binding = ItemProductosUsuariosVerificadosBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return viewholderMostrarArticulos(binding)
    }

    override fun getItemCount(): Int {
        return if (lista.size >= 5) 5 else lista.size
    }

    override fun onBindViewHolder(holder: viewholderMostrarArticulos, position: Int) {
        val item = lista[position]
        holder.render(item)
    }

    inner class viewholderMostrarArticulos(private val binding: ItemProductosUsuariosVerificadosBinding) :
        RecyclerView.ViewHolder(binding.root) {
        val imgARticulo = binding.imgTrabajo
        val progressVar = binding.progressCargaImagen
        val descuentoPorcentaje = binding.descuentoPorcentaje
        fun render(item: dataclas_item_preview_art_comprar) {
            constatnes_carga_imagenes_general.changer_img(
                progressVar,
                itemView.context,
                item.img.toString(),
                null,
                imgARticulo,
                "portada", null
            ) { completado ->

            }
            if (item.descuentoBoolena == true) {
                descuentoPorcentaje.isVisible = true
                descuentoPorcentaje.text = "-${item.descuentoCantidad.toString()}%"
            } else {
                descuentoPorcentaje.isVisible = false
            }
        }


    }
}