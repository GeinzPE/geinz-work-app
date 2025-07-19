package com.geinzz.geinzwork.ui.adapters


import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.geinzz.geinzwork.utils.constantes.constantes.Variables
import com.geinzz.geinzwork.utils.constantes.constantes.constatnes_carga_imagenes_general
import com.geinzz.geinzwork.R
import com.geinzz.geinzwork.model.dataclass_mas_art_promo_servicio
import com.geinzz.geinzwork.databinding.ItemMasArtPromoServiciosBinding

class adapter_item_mas_promo_servicios_art(
    private var lista: MutableList<dataclass_mas_art_promo_servicio>,
    private val categoria: String,
) : RecyclerView.Adapter<adapter_item_mas_promo_servicios_art.viewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): viewHolder {
        val binding = ItemMasArtPromoServiciosBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return viewHolder(binding)
    }

    override fun getItemCount(): Int {
        return if (lista.size >= 4) 4 else lista.size
    }

    override fun onBindViewHolder(holder: viewHolder, position: Int) {
        val item = lista[position]
        holder.render(item, categoria)
    }


    inner class viewHolder(private val binding: ItemMasArtPromoServiciosBinding) :
        RecyclerView.ViewHolder(binding.root) {
        val imagen = binding.imgTrabajo
        val titulo = binding.titulo
        val descripcion = binding.descripcion
        val precio = binding.precioDisponible
        val listener = binding.layoutListener
        fun render(
            dataclassMasArtPromoServicio: dataclass_mas_art_promo_servicio,
            categoria: String,
        ) {
            constatnes_carga_imagenes_general.changer_img(
                binding.progressCargaImagenFondo,
                itemView.context,
                dataclassMasArtPromoServicio.imgPrincipal.toString(),
                null,
                imagen,
                "portada",
                null
            ){}
            titulo.text = dataclassMasArtPromoServicio.titulo
            descripcion.text = dataclassMasArtPromoServicio.descripcion
            val precioTexto = dataclassMasArtPromoServicio.precio
            precio.text = if (precioTexto.equals("GRATIS", ignoreCase = true)) {
                "GRATIS"
            } else {
                "S/$precioTexto.00"
            }
            listener.setOnClickListener {
                Toast.makeText(
                    itemView.context,
                    itemView.context.getString(R.string.Disponible_version_supe),
                    Toast.LENGTH_SHORT
                ).show()
            }


        }
    }

    private fun <T : AppCompatActivity> pasarVista(
        activityClass: Class<T>,
        context: Context,
        idtienda: String,
        idListener: String,
        finish: Boolean
    ) {

        val intent = Intent(context, activityClass)
        intent.putExtra(Variables.idTienda, idtienda)
        intent.putExtra(Variables.idServicio, idListener)
        intent.putExtra(Variables.idProductoClikado, idListener)
        context.startActivity(intent)
        if (finish && context is AppCompatActivity) {
            context.finish()
        }
    }

}