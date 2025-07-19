package com.geinzz.geinzwork.utils.constantes.constantes

import android.R
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import com.google.firebase.firestore.FirebaseFirestore

object constantesDatosUsuarioTienda {

    fun obtenerDetallesTienda(idTienda: String, callback: (String?, String?) -> Unit) {
        val db = FirebaseFirestore.getInstance().collection("Tiendas").document(idTienda)
        db.get()
            .addOnSuccessListener { res ->
                if (res.exists()) {
                    val data = res.data
                    val localidad = data?.get("localidad") as? String
                    val tipoTienda = data?.get("tipoTienda") as? String
                    callback(localidad, tipoTienda)
                } else {
                    callback(null, null)
                }
            }
            .addOnFailureListener {
                println("No se pudo obtener los detalles de la tienda")
                callback(null, null)
            }
    }


     fun obtnerLocalidades(autoCompleteTextView: AutoCompleteTextView) {

        val categorias = listOf("Barranca", "Supe", "Paramonga", "Pativilca")

        val adapter = ArrayAdapter(
            autoCompleteTextView.context,
            R.layout.simple_dropdown_item_1line,
            categorias
        )
        autoCompleteTextView.setAdapter(adapter)

    }
}