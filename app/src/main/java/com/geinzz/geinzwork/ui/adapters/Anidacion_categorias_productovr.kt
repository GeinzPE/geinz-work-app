package com.geinzz.geinzwork.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.geinzz.geinzwork.model.CategoryWithSubcategories
import com.geinzz.geinzwork.databinding.ItemCategoriaVrBinding

class anidacion_categorias_productovrprivate(
    categoriesWithSubcategories: List<CategoryWithSubcategories>,
    val selecionado: (String, String) -> Unit
) : RecyclerView.Adapter<anidacion_categorias_productovrprivate.CategoryViewHolder>() {

    // Filtramos categorías con nombre no vacío para no mostrar categorías vacías
    private val filteredCategories = categoriesWithSubcategories.filter { it.category.name.isNotBlank() }

    inner class CategoryViewHolder(private val binding: ItemCategoriaVrBinding) :
        RecyclerView.ViewHolder(binding.root) {
        val tvCategoryTitle: TextView = binding.tvCategoryTitle
        val rvSubcategories: RecyclerView = binding.rvSubcategories
    }

    override fun getItemCount(): Int = filteredCategories.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        val binding = ItemCategoriaVrBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CategoryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        val categoryWithSubcategories = filteredCategories[position]
        holder.tvCategoryTitle.isVisible=true
        holder.tvCategoryTitle.text = categoryWithSubcategories.category.name

        val subcategoryAdapter = SubcategoryAdapter_vr(categoryWithSubcategories.category.subcategories) { subcategory ->
            categoryWithSubcategories.onSubcategoryClicked(subcategory)
            selecionado(categoryWithSubcategories.category.name, subcategory)
            println("Subcategoría clickeada: ${categoryWithSubcategories.category.name} $subcategory")
        }

        holder.rvSubcategories.apply {
            layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
            adapter = subcategoryAdapter
            setRecycledViewPool(RecyclerView.RecycledViewPool())
        }
    }
}
