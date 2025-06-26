package com.example.geinzwork.constantesGeneral

import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.EditText
import android.widget.LinearLayout
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

import com.example.geinzwork.dataclass.CategoryWithSubcategories
import com.example.geinzwork.dataclass.dataclas_anidacion_productos_vr
import com.geinzz.geinzwork.databinding.BottomSheetCategoriasPrVrBinding
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.chip.Chip
import android.content.Context
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.TextView
import android.widget.Toast
import com.example.geinzwork.adapterViewholder.anidacion_categorias_productovrprivate
import com.example.geinzwork.constantesGeneral.constantes_bottom_shet_trabaja.handler
import com.geinzz.geinzwork.R
import com.geinzz.geinzwork.databinding.BottomSheetHastagsFiltradosBinding
import com.geinzz.geinzwork.databinding.BottomSheetPublicacionesParaBinding
import com.google.firebase.firestore.FirebaseFirestore

object constantes_productos_publicados {
    private lateinit var categoryAdapter: anidacion_categorias_productovrprivate
    fun agregarCategorias(
        dialog: BottomSheetDialog,
        Context: Context,
        layoutNombreMarca: LinearLayout,
        marcaProductoED: EditText,
        modeloProductoED: EditText,
        subcategoriaProducto: EditText,
        catSelcionado: TextView
    ) {
        val bindingBottomSheetCategorias =
            BottomSheetCategoriasPrVrBinding.inflate(LayoutInflater.from(Context))
        bindingBottomSheetCategorias.cerrar.setOnClickListener { dialog.dismiss() }
        val view = bindingBottomSheetCategorias.root

        val startTime = System.currentTimeMillis()

        val db = FirebaseFirestore.getInstance().collection("categoria_productos")
        db.get()
            .addOnSuccessListener { querySnapshot ->

                val categoriesWithSubcategoriesList = mutableListOf<CategoryWithSubcategories>()

                // Limpiar chips antes de añadir nuevos (hazlo una sola vez aquí)
                bindingBottomSheetCategorias.chipGroupCategorias.removeAllViews()

                // Variable para la categoría seleccionada, debe estar fuera del loop
                var categoriaSeleccionada: CategoryWithSubcategories? = null

                // Crear el chip "Todas"
                val chipTodas = Chip(Context).apply {
                    text = "Todas"
                    isCheckable = true
                    isClickable = true

                    setOnClickListener {

                        bindingBottomSheetCategorias.inputnombre.isVisible = false
                        bindingBottomSheetCategorias.search.setText("")

                        categoriaSeleccionada =
                            null // Ninguna categoría seleccionada porque mostramos todas

                        // Mostrar todas las categorías completas
                        setupCategoryRecyclerView(
                            Context,
                            bindingBottomSheetCategorias.idItemCategoriaVr.rvSubcategories,
                            categoriesWithSubcategoriesList,
                            layoutNombreMarca,
                            marcaProductoED,
                            modeloProductoED,
                            subcategoriaProducto,
                            catSelcionado
                        )
                    }
                }
                bindingBottomSheetCategorias.chipGroupCategorias.addView(chipTodas)

                for (document in querySnapshot) {
                    val categoryName = document.id.trim()
                    val subcategories =
                        document.get("subcategorias") as? List<String> ?: emptyList()

                    // Filtrar para no agregar categorías vacías
                    if (categoryName.isNotEmpty() && subcategories.isNotEmpty()) {
                        val category = dataclas_anidacion_productos_vr(categoryName, subcategories)

                        val categoryWithSub = CategoryWithSubcategories(category) { subcategory ->
                            handleSubcategoryClick(categoryName, subcategory)
                            dialog.dismiss()
                        }

                        categoriesWithSubcategoriesList.add(categoryWithSub)

                        // Crear chip para esta categoría
                        val chip = Chip(Context).apply {
                            text = categoryName
                            isCheckable = true
                            isClickable = true

                            setOnClickListener {
                                bindingBottomSheetCategorias.inputnombre.isVisible = true
                                bindingBottomSheetCategorias.search.setText("")

                                categoriaSeleccionada = categoryWithSub

                                setupCategoryRecyclerView(
                                    Context,
                                    bindingBottomSheetCategorias.idItemCategoriaVr.rvSubcategories,
                                    listOf(categoryWithSub),
                                    layoutNombreMarca,
                                    marcaProductoED,
                                    modeloProductoED,
                                    subcategoriaProducto,
                                    catSelcionado
                                )
                            }
                        }
                        bindingBottomSheetCategorias.chipGroupCategorias.addView(chip)
                    } else {
                        Log.w(
                            "categorias_vacias",
                            "Categoría vacía o sin subcategorías encontrada y omitida: $categoryName"
                        )
                    }
                }

                // Añadir el TextWatcher solo una vez, fuera del loop
                bindingBottomSheetCategorias.search.addTextChangedListener(object : TextWatcher {
                    override fun beforeTextChanged(
                        s: CharSequence?,
                        start: Int,
                        count: Int,
                        after: Int
                    ) {
                    }

                    override fun onTextChanged(
                        s: CharSequence?,
                        start: Int,
                        before: Int,
                        count: Int
                    ) {
                    }

                    override fun afterTextChanged(s: Editable?) {
                        val textoBusqueda = s.toString().trim()
                        val categoria = categoriaSeleccionada

                        if (categoria != null) {
                            val subcategoriasFiltradas = categoria.category.subcategories.filter {
                                it.contains(textoBusqueda, ignoreCase = true)
                            }

                            if (subcategoriasFiltradas.isEmpty()) {
                                // No hay subcategorías filtradas -> mostrar mensaje, ocultar RecyclerView
                                bindingBottomSheetCategorias.sinResultados.visibility = View.VISIBLE
                                bindingBottomSheetCategorias.idItemCategoriaVr.rvSubcategories.visibility =
                                    View.GONE
                            } else {
                                // Hay resultados -> mostrar RecyclerView, ocultar mensaje
                                bindingBottomSheetCategorias.sinResultados.visibility = View.GONE
                                bindingBottomSheetCategorias.idItemCategoriaVr.rvSubcategories.visibility =
                                    View.VISIBLE

                                val categoriaFiltrada = CategoryWithSubcategories(
                                    category = categoria.category.copy(subcategories = subcategoriasFiltradas),
                                    onSubcategoryClicked = categoria.onSubcategoryClicked
                                )

                                setupCategoryRecyclerView(
                                    Context,
                                    bindingBottomSheetCategorias.idItemCategoriaVr.rvSubcategories,
                                    listOf(categoriaFiltrada),
                                    layoutNombreMarca,
                                    marcaProductoED,
                                    modeloProductoED,
                                    subcategoriaProducto,
                                    catSelcionado
                                )
                            }
                        } else {
                            // No hay categoría seleccionada, filtramos todas las categorías y subcategorías
                            val categoriasFiltradas =
                                categoriesWithSubcategoriesList.map { catWithSub ->
                                    val subcatsFiltradas =
                                        catWithSub.category.subcategories.filter {
                                            it.contains(textoBusqueda, ignoreCase = true)
                                        }
                                    catWithSub.copy(
                                        category = catWithSub.category.copy(subcategories = subcatsFiltradas)
                                    )
                                }.filter { it.category.subcategories.isNotEmpty() }

                            if (categoriasFiltradas.isEmpty()) {
                                // No hay categorías filtradas -> mostrar mensaje, ocultar RecyclerView
                                bindingBottomSheetCategorias.sinResultados.visibility = View.VISIBLE
                                bindingBottomSheetCategorias.idItemCategoriaVr.rvSubcategories.visibility =
                                    View.GONE
                            } else {
                                // Hay resultados -> mostrar RecyclerView, ocultar mensaje
                                bindingBottomSheetCategorias.sinResultados.visibility = View.GONE
                                bindingBottomSheetCategorias.idItemCategoriaVr.rvSubcategories.visibility =
                                    View.VISIBLE

                                setupCategoryRecyclerView(
                                    Context,
                                    bindingBottomSheetCategorias.idItemCategoriaVr.rvSubcategories,
                                    categoriasFiltradas,
                                    layoutNombreMarca,
                                    marcaProductoED,
                                    modeloProductoED,
                                    subcategoriaProducto,
                                    catSelcionado
                                )
                            }
                        }
                    }
                })

                val endTime = System.currentTimeMillis()
                val elapsedTime = endTime - startTime
                Handler(Looper.getMainLooper()).postDelayed({
                    bindingBottomSheetCategorias.progrssCarga.isVisible = false
                    bindingBottomSheetCategorias.vistaCategoria.isVisible = true
                }, elapsedTime)

                // Mostrar todas las categorías inicialmente
                setupCategoryRecyclerView(
                    Context,
                    bindingBottomSheetCategorias.idItemCategoriaVr.rvSubcategories,
                    categoriesWithSubcategoriesList,
                    layoutNombreMarca,
                    marcaProductoED,
                    modeloProductoED,
                    subcategoriaProducto,
                    catSelcionado
                )
            }

            .addOnFailureListener { e ->
                println("Error al obtener categorías: $e")
            }

        dialog.setContentView(view)
    }

    private fun setupCategoryRecyclerView(
        context: Context,
        recyclerView: RecyclerView,
        categoriesWithSubcategoriesList: List<CategoryWithSubcategories>,
        layoutNombreMarca: LinearLayout,
        marcaProductoED: EditText,
        modeloProductoED: EditText,
        subcategoriaProducto: EditText,
        catSelcionado: TextView
    ) {

        val rvCategorias =
            recyclerView
        rvCategorias.layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
        categoryAdapter =
            anidacion_categorias_productovrprivate(categoriesWithSubcategoriesList) { categoria, subcategoria ->
                val categoriasSinMarcaNiModelo = listOf(
                    "Juguetes y juegos",
                    "Arte y antigüedades",
                    "Hobbies y actividades",
                    "Ropa, calzado y accesorios",
                    "Muebles",
                    "Hogar y jardín",
                    "Construcción y materiales"
                )
                val mostrarCamposMarca = categoria !in categoriasSinMarcaNiModelo
                layoutNombreMarca.isVisible = mostrarCamposMarca

                if (!mostrarCamposMarca) {
                    marcaProductoED.setText("")
                    modeloProductoED.setText("")
                }
                subcategoriaProducto.setText(subcategoria)
                catSelcionado.text = categoria

            }
        rvCategorias.adapter = categoryAdapter
    }

    private fun handleSubcategoryClick(categoryName: String, subcategoryName: String) {
        // Aquí implementa la lógica cuando se hace clic en una subcategoría
        println("Clic en la subcategoría '$subcategoryName' de la categoría '$categoryName'")
        // Por ejemplo, podrías filtrar una lista de productos y actualizar otra RecyclerView.
    }

    fun obtener_estados_productos(context: Context,condicionPrED: AutoCompleteTextView) {
        val db =
            FirebaseFirestore.getInstance().collection("estados_condiciones_productos_generales")
                .document("estados")
        db.get().addOnSuccessListener { res ->
            if (res.exists()) {
                val data = res.data
                val condicionesList = data?.get("estados") as? List<String> ?: emptyList()
                val adapter =
                    ArrayAdapter(context, android.R.layout.simple_dropdown_item_1line, condicionesList)
                condicionPrED.setAdapter(adapter)
            }
        }
    }

    fun mostrar_dialog_para(
        context: Context,
        dialog: BottomSheetDialog,
        selecionado: String,
        select: (String) -> Unit
    ) {
        val bindig_BottomSheet_dialog_para =
            BottomSheetPublicacionesParaBinding.inflate(LayoutInflater.from(context))
        val view = bindig_BottomSheet_dialog_para.root

        val radioGroup = bindig_BottomSheet_dialog_para.RadioGrupCaracterisiticas
        // Preseleccionar opción desde código
        when (selecionado.lowercase()) {
            "todos" -> radioGroup.check(R.id.todos)
            "seguidores" -> radioGroup.check(R.id.seguidores)
            "privado" -> radioGroup.check(R.id.privado)
        }

        // Detectar selección
        radioGroup.setOnCheckedChangeListener { _, checkedId ->
            val seleccion = when (checkedId) {
                R.id.todos -> "Todos"
                R.id.seguidores -> "Seguidores"
                R.id.privado -> "Privado"
                else -> ""
            }

            if (seleccion.isNotEmpty()) {
                select(seleccion)
                dialog.dismiss()
            }
        }

        // Cerrar diálogo manualmente
        bindig_BottomSheet_dialog_para.cerrar.setOnClickListener {
            dialog.dismiss()
        }

        dialog.setContentView(view)
        dialog.show()
    }

    fun obtener_hastags_generales(agregarHastagsED: TextView,
        contex: Context,
        hashtagsGenerales: MutableList<String>,
        dialog: BottomSheetDialog
    ) {
        val bindig_BottomSheet =
            BottomSheetHastagsFiltradosBinding.inflate(LayoutInflater.from(contex))
        val view = bindig_BottomSheet.root
        bindig_BottomSheet.cerrar.setOnClickListener { dialog.dismiss() }

        val tiempoInicio = System.currentTimeMillis()

        val db = FirebaseFirestore.getInstance().collection("hastags_generales")
            .document("hashtags_publicaciones")

        val chipGroup = bindig_BottomSheet.chipGrupHastagsP

        db.get().addOnSuccessListener { documentSnapshot ->
            val tiempoFin = System.currentTimeMillis()
            val tiempoTotal = tiempoFin - tiempoInicio
            handler.postDelayed({
                bindig_BottomSheet.netScroolViewHashtag.isVisible = true
                bindig_BottomSheet.cargandoHastag.isVisible = false
            }, tiempoTotal)

            if (documentSnapshot.exists()) {
                val hashtags = documentSnapshot.get("hashtags_publicaciones_array") as? List<String>
                if (hashtags != null) {
                    chipGroup.removeAllViews()
                    hashtagsGenerales.clear()

                    for (hashtag in hashtags) {
                        val chip = Chip(contex).apply {
                            text = hashtag
                            isCheckable = true
                            isClickable = true

                            setOnCheckedChangeListener { chipView, isChecked ->
                                if (isChecked) {
                                    if (hashtagsGenerales.size >= 5) {
                                        chipView.isChecked = false // desmarcar el chip
                                        Toast.makeText(
                                            contex,
                                            "Solo puedes seleccionar hasta 5 hashtags",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    } else {
                                        hashtagsGenerales.add(text.toString())
                                    }
                                } else {
                                    hashtagsGenerales.remove(text.toString())
                                }

                                Log.d("HashtagsSeleccionados", hashtagsGenerales.toString())
                            }
                        }
                        chipGroup.addView(chip)
                    }

                } else {
                    Log.e("Firestore", "El campo no es un array o está vacío.")
                }
            } else {
                Log.e("Firestore", "El documento no existe.")
            }
        }.addOnFailureListener { exception ->
            Log.e("Firestore", "Error al obtener documento: ${exception.message}")
        }

        // Botón para confirmar y agregar hashtags al EditText
        bindig_BottomSheet.agregarCampos.setOnClickListener {
            agregarhastags_generales_editext(hashtagsGenerales, agregarHastagsED)
            dialog.dismiss()
        }

        dialog.setContentView(view)
    }

    private fun agregarhastags_generales_editext(
        hashtagsGenerales: MutableList<String>,
        textView: TextView
    ) {
        val hashtagsTexto = hashtagsGenerales.joinToString(separator = ", ") { "$it" }
        textView.setText(hashtagsTexto)
    }


}