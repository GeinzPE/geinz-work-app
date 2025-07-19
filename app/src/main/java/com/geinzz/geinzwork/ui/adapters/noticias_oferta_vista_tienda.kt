package com.geinzz.geinzwork.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.geinzz.geinzwork.R
import com.geinzz.geinzwork.model.dataclassGridNoticiasVistaTiendas
import com.google.android.material.imageview.ShapeableImageView

class noticias_oferta_vista_tienda(
    private val lista: MutableList<dataclassGridNoticiasVistaTiendas>,
    private val vermas: (dataclassGridNoticiasVistaTiendas) -> Unit
) : RecyclerView.Adapter<noticias_oferta_vista_tienda.viewHolderOfertaVistaTienda>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): viewHolderOfertaVistaTienda {
        val layoutInflater = LayoutInflater.from(parent.context)
        return viewHolderOfertaVistaTienda(
            layoutInflater.inflate(R.layout.item_grid_noticias_ofertas_vista_tiendas, parent, false)
        )
    }

    override fun getItemCount(): Int {
        return if (lista.size > 8) 8 else lista.size
    }

    override fun onBindViewHolder(holder: viewHolderOfertaVistaTienda, position: Int) {
        val item = lista[position]
        holder.render(item, vermas)
    }

    inner class viewHolderOfertaVistaTienda(view: View) : RecyclerView.ViewHolder(view) {
        val img = view.findViewById<ShapeableImageView>(R.id.img_trabajo)
        val Tipo = view.findViewById<TextView>(R.id.Tipo)
        fun render(
            dataclassGridNoticiasVistaTiendas: dataclassGridNoticiasVistaTiendas,
            vermas: (dataclassGridNoticiasVistaTiendas) -> Unit
        ) {
            Tipo.text = dataclassGridNoticiasVistaTiendas.tipo
            try {
                Glide.with(itemView.context)
                    .load(dataclassGridNoticiasVistaTiendas.img)
                    .into(img)
            } catch (e: Exception) {
                println("error al setear ma img")
            }
            img.setOnClickListener {
                vermas(dataclassGridNoticiasVistaTiendas)
            }
        }
    }
}