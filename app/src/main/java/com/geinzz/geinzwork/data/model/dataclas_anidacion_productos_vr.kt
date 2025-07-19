package com.geinzz.geinzwork.model

data class dataclas_anidacion_productos_vr(val name: String, val subcategories: List<String>)
data class CategoryWithSubcategories(
    val category: dataclas_anidacion_productos_vr,
    val onSubcategoryClicked: (String) -> Unit
)
