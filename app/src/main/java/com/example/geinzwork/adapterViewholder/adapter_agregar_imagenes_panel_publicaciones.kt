package com.example.geinzwork.adapterViewholder

import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.geinzz.geinzwork.R
import com.geinzz.geinzwork.databinding.ItemAgregarImagenesPanelPublicacionesBinding

class adapter_agregar_imagenes_panel_publicaciones(
    private val imagenes: MutableList<Uri>,
    private val onAgregarClick: () -> Unit
) : RecyclerView.Adapter<adapter_agregar_imagenes_panel_publicaciones.ImageViewHolder>() {

    companion object {
        private const val VIEW_TYPE_IMAGE = 1
        private const val VIEW_TYPE_ADD = 2
    }

    override fun getItemViewType(position: Int): Int {
        return if (position < imagenes.size) VIEW_TYPE_IMAGE else VIEW_TYPE_ADD
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
        val binding = ItemAgregarImagenesPanelPublicacionesBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ImageViewHolder(binding)
    }

    override fun getItemCount(): Int {
        // Siempre mostramos el botón de agregar si hay menos de 5 imágenes
        return if (imagenes.size < 5) imagenes.size + 1 else 5
    }

    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
        val binding = holder.binding

        if (getItemViewType(position) == VIEW_TYPE_ADD && imagenes.size < 5) {
            // Recuadro de agregar imagen
            binding.imgTrabajo.setImageResource(R.drawable.cargando_img) // Imagen de "agregar"
            binding.iconoAgregar.visibility = View.VISIBLE
            binding.imgTrabajo.setOnClickListener { onAgregarClick() } // Llamar al callback
            binding.imgTrabajo.setImageURI(null) // Limpiar cualquier imagen anterior
        } else {
            // Mostrar imagen cargada
            binding.iconoAgregar.visibility = View.GONE
            binding.imgTrabajo.setImageURI(imagenes[position])
            binding.imgTrabajo.setOnClickListener(null) // Deshabilitar el clic en imágenes cargadas (opcional)
        }
    }

    inner class ImageViewHolder(val binding: ItemAgregarImagenesPanelPublicacionesBinding) :
        RecyclerView.ViewHolder(binding.root)
}