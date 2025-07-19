package com.geinzz.geinzwork.ui.adapters

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.geinzz.geinzwork.utils.constantes.constantes.constatnes_carga_imagenes_general
import com.geinzz.geinzwork.fragmentos.img_completa.FullscreenImageDialog
import com.geinzz.geinzwork.databinding.ItemCustomTrabajadoresProductosBinding
import com.geinzz.geinzwork.model.dataclassMostarImgProductosVendedor

class adapterInicializarRecycleimgProductosTrabajadores(private val lista: MutableList<dataclassMostarImgProductosVendedor>) :
    RecyclerView.Adapter<adapterInicializarRecycleimgProductosTrabajadores.viewHolderproductosImgTrabajadores>() {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): viewHolderproductosImgTrabajadores {
        val binding = ItemCustomTrabajadoresProductosBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false

        )
        return viewHolderproductosImgTrabajadores(binding)
    }

    override fun getItemCount(): Int = lista.size

    override fun onBindViewHolder(holder: viewHolderproductosImgTrabajadores, position: Int) {
        val item = lista[position]
        Log.d("IMG_ADAPTER", "Posición: $position, URL: ${item.imgProducto}")

        holder.render(item)
    }

    inner class viewHolderproductosImgTrabajadores(private val binding: ItemCustomTrabajadoresProductosBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun render(item: dataclassMostarImgProductosVendedor) {
            constatnes_carga_imagenes_general.changer_img(
                binding.progressCargaImagen,
                itemView.context,
                item.imgProducto.toString(),
                null,
                binding.imageView,
                "portada",
                null
            ) { complet -> }
            binding.imageView.setOnClickListener {
                val dialog = FullscreenImageDialog(item.imgProducto.toString()) //
                dialog.show((itemView.context as AppCompatActivity).supportFragmentManager, "fullscreenImage")
            }

        }


    }
}