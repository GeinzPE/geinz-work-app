package com.geinzz.geinzwork.adapterViewholder

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.geinzwork.constantesGeneral.Variables
import com.geinzz.geinzwork.databinding.ItemNoticiasGuardadasBinding
import com.geinzz.geinzwork.dataclass.dataclassVerGuardados
import com.geinzz.geinzwork.vistaTrabajador.ver_detalles_Promociones


class adapterguardados(
    private val lista: MutableList<dataclassVerGuardados>,
    private val listener: (dataclassVerGuardados) -> Unit,
    private val long_listner: (dataclassVerGuardados) -> Unit
) :
    RecyclerView.Adapter<adapterguardados.viewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): viewHolder {
        val binding =
            ItemNoticiasGuardadasBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return viewHolder(binding)
    }

    override fun getItemCount(): Int {
        return lista.size
    }

    override fun onBindViewHolder(holder: viewHolder, position: Int) {
        val item = lista[position]
        holder.render(item, listener, long_listner)
    }

    class viewHolder(private val binsding: ItemNoticiasGuardadasBinding) :
        RecyclerView.ViewHolder(binsding.root) {
        fun render(
            dataclassVerGuardados: dataclassVerGuardados,
            listener: (dataclassVerGuardados) -> Unit,
            long_listner: (dataclassVerGuardados) -> Unit
        ) {
            try {
                Glide.with(itemView.context)
                    .load(dataclassVerGuardados.img)
                    .into(binsding.imgPrincipal)
            } catch (e: Exception) {
                println("error al setear la img")
            }
            binsding.linealListner.setOnClickListener {
                listener(dataclassVerGuardados)
            }
            binsding.linealListner.setOnLongClickListener {
                long_listner(dataclassVerGuardados)
                true
            }

            binsding.titulo.text = dataclassVerGuardados.nombre
            binsding.vencimineto.text = "Vence ${dataclassVerGuardados.fecha}"
        }

        private fun mandarDatos(dataclassVerGuardados: dataclassVerGuardados) {
            var vista = Intent(itemView.context, ver_detalles_Promociones::class.java).apply {
                putExtra(Variables.idAnuncio, dataclassVerGuardados.idNoticia)
                putExtra(Variables.entrada, Variables.tipoNoticia)
            }
            itemView.context.startActivity(vista)
        }


    }
}