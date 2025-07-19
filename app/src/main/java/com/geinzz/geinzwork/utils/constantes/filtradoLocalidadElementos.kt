package com.geinzz.geinzwork.utils.constantes.constantes

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.view.isVisible
import com.geinzz.geinzwork.R
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDragHandleView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

object filtradoLocalidadElementos {

    private lateinit var bottomSheet: BottomSheetDragHandleView

    @SuppressLint("CommitPrefEdits")
    fun filtradoNacionalidades( Tilte:String,context: Context, dialog: BottomSheetDialog, callback: (String?) -> Unit) {
        val view =
            LayoutInflater.from(context)
                .inflate(R.layout.bottom_sheet_recicle_productos, null, false)
        bottomSheet = view.findViewById(R.id.cerrar)


        val btnApply = view.findViewById<Button>(R.id.btnApply)
        val btnCancel = view.findViewById<Button>(R.id.btnCancel)
        val Title = view.findViewById<TextView>(R.id.tvTitle)
        val deGeinz=view.findViewById<Button>(R.id.geinz)
        val general =view.findViewById<Button>(R.id.general)
        val autoCompleteCategory = view.findViewById<AutoCompleteTextView>(R.id.categoria)
        val linealBotomoes=view.findViewById<LinearLayout>(R.id.lineal_filtrado_btn)

        linealBotomoes.isVisible=true
        general.isVisible=false
        deGeinz.isVisible=false


        obtenerLocalidadUser { localidad ->
            autoCompleteCategory.setText(localidad)
            listaFiltrado(autoCompleteCategory)
        }

        Title.text = Tilte

        bottomSheet.setOnClickListener {
            dialog.dismiss()
        }

        btnCancel.setOnClickListener {
            dialog.dismiss()
        }

        btnApply.setOnClickListener {
            val seleccion = autoCompleteCategory.text.toString()
            Toast.makeText(
                context,
                "Buscando trabajadores de $seleccion",
                Toast.LENGTH_SHORT
            ).show()

            callback(seleccion)
            dialog.dismiss()

        }

        dialog.setContentView(view)
    }

    fun listaFiltrado(autoCompleteTextView: AutoCompleteTextView) {
        val lista = listOf(Variables.Barranca, Variables.Supe, Variables.Paramonga, Variables.Pativilca,Variables.General)

        val adapter = ArrayAdapter(
            autoCompleteTextView.context,
            android.R.layout.simple_dropdown_item_1line,
            lista
        )

        autoCompleteTextView.setAdapter(adapter)
    }

    fun obtenerLocalidadUser(callback: (String?) -> Unit) {
        val firebaseAuth = FirebaseAuth.getInstance()
        val uid = firebaseAuth.uid

        if (uid != null) {
            val firestore = FirebaseFirestore.getInstance()
            val usuarioDoc = firestore.collection(Variables.trabajadores_usuariosDB).document(Variables.trabajadoresDB).collection(Variables.trabajadoresDB).document(uid)

            usuarioDoc.get()
                .addOnSuccessListener { res ->
                    if (res.exists()) {
                        val localidad = res.getString(Variables.localidad)
                        callback(localidad)
                    } else {
                        val usuarioCuentaNormalDoc = firestore.collection(Variables.trabajadores_usuariosDB).document(Variables.usuarios_db).collection(Variables.usuarios_db).document(uid)

                        usuarioCuentaNormalDoc.get()
                            .addOnSuccessListener { res2 ->
                                if (res2.exists()) {
                                    val localidad = res2.getString(Variables.localidad)
                                    callback(localidad)
                                } else {
                                    callback("No se encontró la localidad del cliente en ninguna colección")
                                }
                            }
                            .addOnFailureListener { exception ->
                                callback("Error al obtener el documento: ${exception.message}")
                            }
                    }
                }
                .addOnFailureListener { exception ->
                    callback("Error al obtener el documento: ${exception.message}")
                }
        } else {
            callback(Variables.General)
        }
    }

}