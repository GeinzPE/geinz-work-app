package com.geinzz.geinzwork.ui.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.geinzz.geinzwork.utils.constantes.constantes.constantes2
import com.geinzz.geinzwork.databinding.ItemTiendasBinding
import com.geinzz.geinzwork.model.dataclassTiendas

class adapterTiendas(
    private val listaTiendas: MutableList<dataclassTiendas>,
    private val seguir: (dataclassTiendas) -> Unit,
    private val dejarDeSeguir: (dataclassTiendas) -> Unit,
    private val ver_mas: (dataclassTiendas) -> Unit,
    private val contexto: Context,
    private val idUser: String


) : RecyclerView.Adapter<adapterTiendas.viewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): viewHolder {
        val binding = ItemTiendasBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return viewHolder(binding)
    }

    override fun getItemCount(): Int {
        return if (listaTiendas.size >= 6) 6 else listaTiendas.size
    }

    override fun onBindViewHolder(holder: viewHolder, position: Int) {
        val item = listaTiendas[position]
        holder.render(item, seguir, dejarDeSeguir, ver_mas, contexto, idUser)
    }

    inner class viewHolder(private val binding: ItemTiendasBinding) :
        RecyclerView.ViewHolder(binding.root) {
        val imgPerfil = binding.imgPerfil
        val imagenProtada = binding.imgPortada
        val nombre = binding.nombreTienda
        val estellasResultado = binding.resultadoEstrellas
        val categorias = binding.categoriaTienda
        val slogan = binding.slogan
        val seguirbtn = binding.seguir
        val layoutListener = binding.linealListener

        fun render(
            dataclassTiendas: dataclassTiendas,
            seguir: (dataclassTiendas) -> Unit,
            dejarDeSeguir: (dataclassTiendas) -> Unit,
            ver_mas: (dataclassTiendas) -> Unit,
            contexto: Context,
            idUser: String
        ) {
            constantes2.verificarSiSige(dataclassTiendas, contexto, seguirbtn)
            seguirbtn.setOnClickListener {
                if (seguirbtn.text.equals("seguir")) {
                    seguir(dataclassTiendas)
                    seguirbtn.text = "Siguiendo"
                } else if (seguirbtn.text.equals("Siguiendo")) {
                    dejarDeSeguir(dataclassTiendas)
                    seguirbtn.text = "seguir"
                }
            }


            slogan.text = dataclassTiendas.slogan
            nombre.text = dataclassTiendas.nombreTienda
            estellasResultado.text = dataclassTiendas.estrellas
            categorias.text = dataclassTiendas.categoria

            try {
                Glide.with(itemView.context)
                    .load(dataclassTiendas.imgPerfil)
                    .into(imgPerfil)

                Glide.with(itemView.context)
                    .load(dataclassTiendas.imgPortada)
                    .into(imagenProtada)
            } catch (e: Exception) {
                println("ocurrio un errro al setear la imagen")
            }

            layoutListener.setOnClickListener {
                ver_mas(dataclassTiendas)
            }

        }
    }

}