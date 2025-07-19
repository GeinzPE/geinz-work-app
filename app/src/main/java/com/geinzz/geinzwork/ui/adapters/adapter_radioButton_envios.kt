package com.geinzz.geinzwork.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.geinzz.geinzwork.databinding.RadioBtnDirecionEnviosBinding
import com.geinzz.geinzwork.model.dataclassradiobtn

class adapter_radioButton_envios(
    private val lista: MutableList<dataclassradiobtn>,
    private val onItemClick: (dataclassradiobtn) -> Unit,
    private val onCrearDireccionClick: () -> Unit // Nueva función para el botón "Crear dirección"
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {


    companion object {
        private const val TIPO_ITEM_NORMAL = 0
        private const val TIPO_BOTON_CREAR = 1
    }

    override fun getItemViewType(position: Int): Int {
        return if (position == lista.size) TIPO_BOTON_CREAR else TIPO_ITEM_NORMAL
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return if (viewType == TIPO_ITEM_NORMAL) {
            val binding = RadioBtnDirecionEnviosBinding.inflate(inflater, parent, false)
            ViewHolderItem(binding)
        } else {
            val binding = RadioBtnDirecionEnviosBinding.inflate(inflater, parent, false)
            ViewHolderBoton(binding) // Usamos el mismo layout para "Crear dirección"
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is ViewHolderItem) {
            val item = lista[position]
            holder.render(item)
        } else if (holder is ViewHolderBoton) {
            holder.bind(onCrearDireccionClick)
        }
    }

    override fun getItemCount(): Int {
        return lista.size + 1 // Se suma 1 para el botón final
    }

    inner class ViewHolderItem(private val binding: RadioBtnDirecionEnviosBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun render(dataclassradiobtn: dataclassradiobtn) {
            binding.docuemnto.text = dataclassradiobtn.nombreRef
            binding.docuemnto.setOnClickListener {
                onItemClick(dataclassradiobtn)
            }
        }
    }

    inner class ViewHolderBoton(private val binding: RadioBtnDirecionEnviosBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(onClick: () -> Unit) {
            binding.docuemnto.text = "Crear nueva dirección" // Cambia el texto del botón
            binding.docuemnto.setOnClickListener { onClick() }
        }
    }
}