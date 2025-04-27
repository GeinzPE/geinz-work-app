package com.example.geinzwork.adapterViewholder

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.geinzwork.constantesGeneral.Variables
import com.example.geinzwork.constantesGeneral.constatnes_carga_imagenes_general
import com.example.geinzwork.dataclass.dataclass_seguidores_seguidos
import com.geinzz.geinzwork.R
import com.geinzz.geinzwork.databinding.ItemCargaSeguidoresSeguidosBinding
import com.geinzz.geinzwork.dataclass.dataClassTrabajosd
import de.hdodenhof.circleimageview.CircleImageView

class adapter_seguidores_seguidos(
    private val lista: MutableList<dataclass_seguidores_seguidos>,
    private val listener: (dataclass_seguidores_seguidos) -> Unit
) :
    RecyclerView.Adapter<adapter_seguidores_seguidos.viewholderSeguidores_Seguidos>() {


    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): viewholderSeguidores_Seguidos {
        val binding = ItemCargaSeguidoresSeguidosBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return viewholderSeguidores_Seguidos(binding)
    }

    override fun getItemCount(): Int = lista.size

    override fun onBindViewHolder(holder: viewholderSeguidores_Seguidos, position: Int) {
        val item = lista[position]
        holder.render(item)

    }

    inner class viewholderSeguidores_Seguidos(private val binding: ItemCargaSeguidoresSeguidosBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun render(item: dataclass_seguidores_seguidos) {
            binding.categoriaTrabajo.text = item.tipo_trabajado
            binding.nombreUser.text = item.nombre_trabajador
            binding.nacionalidadUser.text = item.nacionalidad
            if (item.verificado == true) {
                binding.verificadoIcono.isVisible = true
            } else {
                binding.verificadoIcono.isVisible = false
            }
            constatnes_carga_imagenes_general.changer_img(
                binding.progreesCarga,
                itemView.context,
                item.img_perfil.toString(),
                binding.imgPerfil,
                null,
                "perfil"
            ) { cargado ->
            }
            when (item.nacionalidad) {
                Variables.Peruano -> {
                    try {
                        Glide.with(itemView.context)
                            .load(R.drawable.bandera_peru)
                            .into(binding.banderaNacionalidad)
                    } catch (e: Exception) {
                        println(e)
                    }

                }

                Variables.Venezolano -> {
                    try {
                        Glide.with(itemView.context)
                            .load(R.drawable.bandera_venezolana)
                            .into(binding.banderaNacionalidad)
                    } catch (e: Exception) {
                        println(e)
                    }
                }
            }
            binding.listener.setOnClickListener {
                listener(item)
            }
        }


    }
}