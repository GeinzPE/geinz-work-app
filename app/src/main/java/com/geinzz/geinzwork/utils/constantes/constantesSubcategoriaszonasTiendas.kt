package com.geinzz.geinzwork.utils.constantes.constantes

import android.R
import android.content.Context
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.core.view.isVisible
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage

object constantesSubcategoriaszonasTiendas {

    fun obtenerySetearCat(
        lista: MutableList<String>, categoria: String
    ) {
        val db = FirebaseFirestore.getInstance()
        val docRef = db.collection("subcategoriasTiendas").document(categoria)

        docRef.get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val subcategorias = document.get("subcategorias") as? List<*>
                    if (subcategorias != null) {
                        for (subcategoria in subcategorias) {
                            if (subcategoria is String) {
                                lista.add(subcategoria) // Agregar cada subcategoría individualmente a la lista
                            } else {
                                // Manejar caso donde subcategoria no es String si es necesario
                            }
                        }
                    }
                } else {
                    println("No se encontró el documento para la categoría $categoria")
                }
            }
            .addOnFailureListener { exception ->
                println(exception)
            }
    }

    fun obtenerSubcategorias(
        rutaColletion:String,
        categorias: String,
        onSuccess: (String) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val db = FirebaseFirestore.getInstance()
        val docRef = db.collection(rutaColletion).document(categorias)

        docRef.get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val subcategorias = document.get("subcategorias") as? List<*>
                    if (subcategorias != null) {
                        val listasubcategorias = mutableListOf<String>()
                        for (subcategoria in subcategorias) {
                            if (subcategoria is String) {
                                listasubcategorias.add(subcategoria)
                            } else {
                                // Manejar caso donde subcategoria no es String si es necesario
                            }
                        }
                        val subcategoriasString = listasubcategorias.joinToString(",")
                        onSuccess(subcategoriasString) // Llamar onSuccess con la cadena de subcategorías
                    } else {
                        onFailure(Exception("No se encontraron subcategorías para la categoría $categorias"))
                    }
                } else {
                    onFailure(Exception("No se encontró el documento para la categoría $categorias"))
                }
            }
            .addOnFailureListener { exception ->
                onFailure(exception)
            }
    }

    fun obtenerImagenesCategorias(
        ruta1: String, ruta2: String,
        categoria: String,
        onSuccess: (String?) -> Unit,
        onFailure: (Exception) -> Unit,
    ) {
        // Construye la referencia al archivo en Firebase Storage
        val referenciaImagen = FirebaseStorage.getInstance().reference
            .child(ruta1)
            .child(ruta2)
            .child("${categoria}.png") // Cambia la extensión según el tipo de imagen que tengas

        // Obtiene la URL de descarga de la imagen
        referenciaImagen.downloadUrl
            .addOnSuccessListener { uri ->
                val urlImagen = uri.toString()
                onSuccess(urlImagen) // Llama al callback onSuccess con la URL de la imagen
            }
            .addOnFailureListener { exception ->
                onFailure(exception) // Llama al callback onFailure si ocurre un error
            }
    }

    fun agregarZonas(
        autoCompleteTextView: AutoCompleteTextView,
        context: Context,
        masCercamosAti: LinearLayout
    ) {
        val zonas = listOf("centrica", "salida", "entrada", "Cercanos a ti (BETA)")
        val adapter = ArrayAdapter(
            autoCompleteTextView.context,
            R.layout.simple_dropdown_item_1line,
            zonas
        )
        autoCompleteTextView.setAdapter(adapter)
        autoCompleteTextView.onItemClickListener =
            AdapterView.OnItemClickListener { parent, view, position, id ->
                val selectedItem = parent.getItemAtPosition(position).toString()
                if (selectedItem.equals("Cercanos a ti (BETA)")) {
                    masCercamosAti.isVisible = true
                } else {
                    masCercamosAti.isVisible = false
                }
                Toast.makeText(context, "Elemento seleccionado: $selectedItem", Toast.LENGTH_SHORT)
                    .show()
            }
    }

}