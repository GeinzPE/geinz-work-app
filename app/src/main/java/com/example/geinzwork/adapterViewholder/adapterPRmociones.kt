package com.example.geinzwork.adapterViewholder

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.geinzwork.DiffUtilClass.difiultilsFiltrado
import com.example.geinzwork.constantesGeneral.Variables
import com.example.geinzwork.constantesGeneral.constatnes_carga_imagenes_general
import com.example.geinzwork.dataclass.dataclasEncontrados
import com.geinzz.geinzwork.constantesGeneral.constantesPublicidad.agregarCantidadClickAnuncios
import com.geinzz.geinzwork.databinding.ItemPromocionesEncontradasBinding
import com.geinzz.geinzwork.vistas_anuncios_general
import com.google.firebase.firestore.FirebaseFirestore

class adapterPRmociones(private var lista: List<dataclasEncontrados>) :
    RecyclerView.Adapter<adapterPRmociones.vieHolderPromociones>() {

    fun actulizarLista(newLista: List<dataclasEncontrados>) {
        val difiutl = difiultilsFiltrado(lista, newLista)
        val result = DiffUtil.calculateDiff(difiutl)
        lista = newLista
        result.dispatchUpdatesTo(this)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): vieHolderPromociones {
        val binding = ItemPromocionesEncontradasBinding.inflate(
            LayoutInflater.from(parent.context),
            parent, false
        )
        return vieHolderPromociones(binding)
    }

    override fun getItemCount(): Int = lista.size

    override fun onBindViewHolder(holder: vieHolderPromociones, position: Int) {
        val item = lista[position]
        holder.render(item)
    }

    class vieHolderPromociones(private val binding: ItemPromocionesEncontradasBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun render(item: dataclasEncontrados) {


            binding.nombeTienda.text = item.nombreTienda
            binding.fechaVencimineto.text = "Finaliza: ${item.FechaVencimineto}"

            if (item.oferta == true) {
                binding.descuentoPorcentaje.isVisible = true
                binding.descuentoPorcentaje.text = "- ${item.descuentoOferta}% OFF"
            } else {
                binding.descuentoPorcentaje.isVisible = false
            }
            constatnes_carga_imagenes_general.changer_img(
                binding.progressCargaImagen,
                itemView.context,
                item.img.toString(),
                null,
                binding.imgTrabajo,
                "portada",
                null
            ){}
            binding.imgTrabajo.setOnClickListener {
                val db = FirebaseFirestore.getInstance().collection(Variables.anuncios)
                    .document(item.documento.toString())
                    .collection(Variables.anuncios).document(item.anuncioid.toString())
                agregarCantidadClickAnuncios(db, item.anuncioid.toString(), Variables.click)
                var vista = Intent(itemView.context, vistas_anuncios_general::class.java).apply {
                    putExtra(Variables.anuncio, item.anuncioid)
                    putExtra(Variables.documentoAdapterPromo, item.documento)
//
                }
                itemView.context.startActivity(vista)
            }
        }
    }


}
