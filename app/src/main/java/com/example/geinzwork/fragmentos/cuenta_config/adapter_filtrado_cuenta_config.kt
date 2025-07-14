package com.example.geinzwork.fragmentos.cuenta_config

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.geinzz.geinzwork.R
import com.geinzz.geinzwork.databinding.ItemCuentaConfigBinding


class adapter_filtrado_cuenta_config(
    private val lista: MutableList<dataclass_cuenta_config_filtrado>,
    private val onItemClick: (dataclass_cuenta_config_filtrado) -> Unit
) : RecyclerView.Adapter<adapter_filtrado_cuenta_config.viewholder_filtrado>() {

    fun actualizarLista(nuevaLista: List<dataclass_cuenta_config_filtrado>) {
        lista.clear()
        lista.addAll(nuevaLista)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): viewholder_filtrado {
        val binding = ItemCuentaConfigBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return viewholder_filtrado(binding)
    }

    override fun onBindViewHolder(holder: viewholder_filtrado, position: Int) {
        val item = lista[position]
        holder.render(item)
    }

    override fun getItemCount(): Int = lista.size

    inner class viewholder_filtrado(private val binding: ItemCuentaConfigBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun render(item: dataclass_cuenta_config_filtrado) {
            Glide.with(itemView.context)
                .load(item.img)
                .into(binding.imgIcono)
            binding.textoContenido.text = item.titulo_texto
            binding.linealPadre.setOnClickListener {
                onItemClick(item)
            }
            var visible = false
            binding.lineaApartado.isVisible=true

            binding.ocultarP1.setOnClickListener {
                visible = !visible
                binding.textoOcultar.visibility = if (visible) View.VISIBLE else View.GONE
                binding.ocultarP1.setImageResource(
                    if (visible) R.drawable.ocultar_abajo else R.drawable.ocultar_arriva
                )
            }

            binding.textoOcultar.text=item.texto_descripcion
        }
    }
}

