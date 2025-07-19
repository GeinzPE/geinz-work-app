package com.geinzz.geinzwork.utils.constantes.constantes

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.core.view.isVisible
import com.geinzz.geinzwork.databinding.BottomSheetHastagsFiltradosBinding
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.chip.Chip
import com.google.firebase.firestore.FirebaseFirestore

object constantes_hastags_generales {
    fun obtener_hastags_generales(
        contex: Context,
        hashtagsGenerales: MutableList<String>,
        dialog: BottomSheetDialog,
        Editex: EditText
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
            constantes_bottom_shet_trabaja.handler.postDelayed({
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
            agregarhastags_generales_editext(hashtagsGenerales, Editex)
            dialog.dismiss()
        }

        dialog.setContentView(view)
    }

    fun obtenerHastags_cada_cat(
        categoriasSeleccionadas: String,
        dialog: BottomSheetDialog,
        contex: Context,
        hashtagsCategoria: MutableList<String>,
        editText: EditText
    ) {
        Log.d("categori_pasda", categoriasSeleccionadas)
        val bindig_BottomSheet =
            BottomSheetHastagsFiltradosBinding.inflate(LayoutInflater.from(contex))
        val view = bindig_BottomSheet.root
        bindig_BottomSheet.cerrar.setOnClickListener { dialog.dismiss() }
        val db = FirebaseFirestore.getInstance()
        val subcategoriasRef =
            db.collection("subcategoriasTrabajos").document(categoriasSeleccionadas)

        val tiempoInicio = System.currentTimeMillis()

        subcategoriasRef.get().addOnSuccessListener { documentSnapshot ->
            val tiempoFin = System.currentTimeMillis()
            val tiempoTotal = tiempoFin - tiempoInicio
            constantes_bottom_shet_trabaja.handler.postDelayed({
                bindig_BottomSheet.netScroolViewHashtag.isVisible = true
                bindig_BottomSheet.cargandoHastag.isVisible = false
            }, tiempoTotal)

            if (documentSnapshot.exists()) {
                val hashtags = documentSnapshot.get("hashtags_array") as? List<String>
                if (hashtags != null) {
                    val chipGroup = bindig_BottomSheet.chipGrupHastagsP
                    chipGroup.removeAllViews()
                    hashtagsCategoria.clear()

                    for (hashtag in hashtags) {
                        val chip = Chip(contex).apply {
                            text = hashtag
                            isCheckable = true
                            isClickable = true

                            setOnCheckedChangeListener { buttonView, isChecked ->
                                if (isChecked) {
                                    if (hashtagsCategoria.size >= 5) {
                                        buttonView.isChecked = false // desmarcar el chip
                                        Toast.makeText(
                                            contex,
                                            "Solo puedes seleccionar hasta 5 hashtags",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    } else {
                                        hashtagsCategoria.add(text.toString())
                                    }

                                } else {
                                    hashtagsCategoria.remove(text.toString())
                                }
                                Log.d("HashtagCategoria", hashtagsCategoria.toString())
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
            Log.e(
                "Firestore", "Error al obtener documento: ${exception.message}"
            )
        }
        bindig_BottomSheet.agregarCampos.setOnClickListener {
            agregarhastags_generales_editext(hashtagsCategoria, editText)
            dialog.dismiss()
        }
        dialog.setContentView(view)
        dialog.show()
    }

    private fun agregarhastags_generales_editext(
        hashtagsGenerales: MutableList<String>,
        textView: TextView
    ) {
        val hashtagsTexto = hashtagsGenerales.joinToString(separator = ", ") { "$it" }
        textView.setText(hashtagsTexto)
    }


}