package com.geinzz.geinzwork.utils.constantes.constantes

import android.R
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import com.google.firebase.firestore.FirebaseFirestore

object constanterAutcompleteTexviewGeneral {

    fun obtenerCategoriasPublicidades(autoCompleteCategory: AutoCompleteTextView) {
        val categoriasTiendas = mutableListOf<String>()
        val db = FirebaseFirestore.getInstance()
            .collection(Variables.categoriasDB)
            .document(Variables.categoriaPublicidad)

        db.get()
            .addOnSuccessListener { res ->
                if (res.exists()) {
                    val categorias = res[Variables.categoriasDB] as? ArrayList<String>
                    categorias?.let {
                        categoriasTiendas.clear()
                        categoriasTiendas.addAll(it)

                        val adapter = ArrayAdapter(
                            autoCompleteCategory.context,
                            R.layout.simple_dropdown_item_1line,
                            categoriasTiendas
                        )
                        autoCompleteCategory.setAdapter(adapter)
                    }
                } else {
                    println("El documento de categorías no existe.")
                }
            }
            .addOnFailureListener { exception ->
                println("Error al obtener categorías: ${exception.message}")
            }
    }

    fun obtenerSubcategoriasPublicidades(
        filtrado: String,
        autoCompleteCategory: AutoCompleteTextView
    ) {
        val categoriasTiendas = mutableListOf<String>()
        val db = FirebaseFirestore.getInstance().collection(Variables.subcategoriasPublicidad)
            .document(filtrado)
        db.get().addOnSuccessListener { res ->
            if (res.exists()) {
                val subCat = res[Variables.subcategoriasDB] as? ArrayList<String>
                subCat?.let { it ->
                    categoriasTiendas.addAll(it)
                    val adapter = ArrayAdapter(
                        autoCompleteCategory.context,
                        R.layout.simple_dropdown_item_1line,
                        categoriasTiendas
                    )
                    autoCompleteCategory.setAdapter(adapter)
                }
            } else {
                println("El documento de categorías no existe.")
            }
        }.addOnFailureListener { exception ->
            println("Error al obtener categorías: ${exception.message}")
        }

    }
}