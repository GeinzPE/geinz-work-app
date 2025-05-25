package com.example.geinzwork.adapterViewholder

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.geinzwork.dataclass.dataclass_dispo_vinculados
import com.geinzz.geinzwork.databinding.LayoutDispoVinculadoBinding

class adapter_dispo_vinculados(private val lista_dispo: MutableList<dataclass_dispo_vinculados>) :
    RecyclerView.Adapter<adapter_dispo_vinculados.viewholderDispo>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): viewholderDispo {
        val binding =
            LayoutDispoVinculadoBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return viewholderDispo(binding)

    }

    override fun getItemCount(): Int = lista_dispo.size

    override fun onBindViewHolder(holder: viewholderDispo, position: Int) {
        val item = lista_dispo[position]
        holder.render(item)
    }

    class viewholderDispo(private val binding: LayoutDispoVinculadoBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun render(item: dataclass_dispo_vinculados) {
            binding.dispositivo.text = item.nombre_dispo
            binding.hora.text = item.hora
            binding.fecha.text = item.fecha
        }
    }
}