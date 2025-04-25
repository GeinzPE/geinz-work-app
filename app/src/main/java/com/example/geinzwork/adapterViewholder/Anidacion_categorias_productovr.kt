package com.example.geinzwork.adapterViewholder

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.geinzwork.dataclass.CategoryWithSubcategories
import com.geinzz.geinzwork.databinding.ItemCategoriaVrBinding
import com.google.android.material.bottomsheet.BottomSheetDragHandleView

class anidacion_categorias_productovrprivate(
    val categoriesWithSubcategories: List<CategoryWithSubcategories>,
    val selecionado: (String,String) -> Unit
) :
    RecyclerView.Adapter<anidacion_categorias_productovrprivate.CategoryViewHolder>() {
    inner class CategoryViewHolder(private val binding: ItemCategoriaVrBinding) :
        RecyclerView.ViewHolder(binding.root) {
        val tvCategoryTitle: TextView = binding.tvCategoryTitle
        val rvSubcategories: RecyclerView = binding.rvSubcategories
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        val binding =
            ItemCategoriaVrBinding.inflate(LayoutInflater.from(parent.context), parent, false)

        return CategoryViewHolder(binding)
    }

    override fun getItemCount(): Int = categoriesWithSubcategories.size

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        val categoryWithSubcategories = categoriesWithSubcategories[position]
        holder.tvCategoryTitle.text = categoryWithSubcategories.category.name

        val subcategoryAdapter =
            SubcategoryAdapter_vr(categoryWithSubcategories.category.subcategories) { subcategory ->
                categoryWithSubcategories.onSubcategoryClicked(subcategory)
                selecionado(categoryWithSubcategories.category.name,subcategory)
                println("Subcategoría clickeada: $subcategory")
            }

        holder.rvSubcategories.apply {
            layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
            adapter = subcategoryAdapter
            setRecycledViewPool(RecyclerView.RecycledViewPool())
        }
    }


}
