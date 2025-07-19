package com.geinzz.geinzwork.vistas_principales.onboarding_activitys

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.geinzz.geinzwork.vistas_principales.dataclass_onboarding.onboarding_verificado
import com.geinzz.geinzwork.databinding.ItemOnboardingVerificadoBinding

class onboardingAdapterVerificado(
    private val lista: List<onboarding_verificado>,
    ) :
    RecyclerView.Adapter<onboardingAdapterVerificado.onboarding_verificadoHolder>() {
    inner class onboarding_verificadoHolder(private val binding: ItemOnboardingVerificadoBinding) :
        RecyclerView.ViewHolder(binding.root) {
        val titulo = binding.titleTextView
        val img = binding.insgniasVerificados
        val animation = binding.animatior
        val texto = binding.descriptionTextView
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): onboarding_verificadoHolder {
        val view = ItemOnboardingVerificadoBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return onboarding_verificadoHolder(view)
    }

    override fun getItemCount(): Int {
        return lista.size
    }

    override fun onBindViewHolder(holder: onboarding_verificadoHolder, position: Int) {
        val item = lista[position]
        holder.titulo.text = item.titulo
        holder.texto.text = item.texto
        if (item.imgLotte != null) {
            holder.animation.setAnimation(item.imgLotte)
            holder.animation.isVisible=true
            holder.img.isVisible=false
        } else if (item.imgDrawable != null) {
            holder.img.setImageResource(item.imgDrawable)
            holder.animation.isVisible=false
            holder.img.isVisible=true

        }


    }
}