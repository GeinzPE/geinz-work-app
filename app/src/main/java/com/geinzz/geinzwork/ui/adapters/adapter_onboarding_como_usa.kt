package com.geinzz.geinzwork.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.geinzz.geinzwork.model.dataclass_onboarding_comousar
import com.geinzz.geinzwork.databinding.ItemOnboardingBaseBindingBinding


class adapter_onboarding_como_usa(
    private val lista: List<dataclass_onboarding_comousar>
) : RecyclerView.Adapter<adapter_onboarding_como_usa.OnboardingComoUsar>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OnboardingComoUsar {
        val binding = ItemOnboardingBaseBindingBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return OnboardingComoUsar(binding)
    }

    override fun getItemViewType(position: Int): Int {
        // Devuelve el layout a inflar para ese item
        return lista[position].layoutResId
    }

    override fun onBindViewHolder(holder: OnboardingComoUsar, position: Int) {
        val context = holder.binding.root.context
        val layoutToInflate = getItemViewType(position)
        val childView = LayoutInflater.from(context).inflate(layoutToInflate, holder.binding.containerLayout, false)

        // Limpiar el contenedor primero
        holder.binding.containerLayout.removeAllViews()
        // Agregar el nuevo layout
        holder.binding.containerLayout.addView(childView)
    }

    override fun getItemCount(): Int = lista.size

    inner class OnboardingComoUsar(val binding: ItemOnboardingBaseBindingBinding) :
        RecyclerView.ViewHolder(binding.root)
}