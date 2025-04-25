package com.example.geinzwork.adapterViewholder

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.geinzz.geinzwork.databinding.ItemSubcategoriaButtonVrBinding
import com.google.android.material.chip.Chip

class SubcategoryAdapter_vr(
    private val subcategories: List<String>,
    private val onSubcategoryClicked: (String) -> Unit
) : RecyclerView.Adapter<SubcategoryAdapter_vr.SubcategoryViewHolder>() {
    class SubcategoryViewHolder(
        private val binding: ItemSubcategoriaButtonVrBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        val chip: Chip = binding.chipSubcategoryButton // Asegúrate de que el ID en item_subcategory_button_vr.xml sea 'chipSubcategoryButton'
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SubcategoryViewHolder {
        val binding = ItemSubcategoriaButtonVrBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return SubcategoryViewHolder(binding)
    }

    override fun getItemCount(): Int = subcategories.size

    override fun onBindViewHolder(holder: SubcategoryViewHolder, position: Int) {
        val subcategory = subcategories[position]
        holder.chip.text = subcategory
        holder.chip.setOnClickListener {
            onSubcategoryClicked(subcategory)
        }
    }
}