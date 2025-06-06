package com.geinzz.geinzwork

import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import com.bumptech.glide.Glide
import com.example.geinzwork.constantesGeneral.Variables
import com.example.geinzwork.constantesGeneral.constatnes_carga_imagenes_general
import com.geinzz.geinzwork.constantesGeneral.constantes
import com.geinzz.geinzwork.constantesGeneral.constantesCarrito
import com.geinzz.geinzwork.databinding.ActivityEditarReviewBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions

class EditarReview : AppCompatActivity() {
    private lateinit var binding: ActivityEditarReviewBinding
    private lateinit var firebaseAuth: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditarReviewBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        firebaseAuth = FirebaseAuth.getInstance()

        val TipoEditado = intent.getStringExtra(Variables.TipoEditado).toString()
        val iduser = intent.getStringExtra(Variables.iduser).toString()
        val review_id = intent.getStringExtra("id_review").toString()
        seterReviewAnterior(iduser, review_id)
        val nuevaReseview = intent.getStringExtra(Variables.nuevaReseña)
        val editado_adaptador = intent.getBooleanExtra("editado_adaptador", false)
        if (!nuevaReseview.isNullOrEmpty()) {
            binding.nuevaReview.setText(nuevaReseview)
        }

        val nuevaCantidadStart = intent.getStringExtra(Variables.cantidadStart) ?: ""
        binding.EntradaCantidadStart.setText(nuevaCantidadStart)



        when (TipoEditado) {
            "CuentaTienda" -> {
                binding.ActulizarReview.setOnClickListener {
                    val idUserNewReview = intent.getStringExtra(Variables.iduser).toString()
                    actualizarReview(
                        idUserNewReview,
                        Variables.trabajadores_usuariosDB,review_id
                    )
                    Log.d("pasmos_datos", "${idUserNewReview} ${Variables.trabajadores_usuariosDB}")
                }
            }

            Variables.CuentaFreelancer -> {
                if (editado_adaptador == false) {
                    binding.reseAtxt.isVisible = false
                } else {
                    binding.reseAtxt.isVisible = true

                }
                binding.ActulizarReview.setOnClickListener {
                    val idUserNewReview = intent.getStringExtra(Variables.iduser).toString()
                    actualizarReview(
                        idUserNewReview,
                        Variables.trabajadores_usuariosDB,review_id
                    )
                    Log.d("pasmos_datos", "${review_id} ${Variables.trabajadores_usuariosDB}")

                }
            }
        }
    }

    private fun seterReviewAnterior(idUser: String, id_review: String) {
        val cantidad = intent.getStringExtra(Variables.cantidad).toString()
        val nombre = intent.getStringExtra(Variables.nombre).toString()
        val review = intent.getStringExtra(Variables.review).toString()

        constantesCarrito.setearDatosUsuarioImgNombre(idUSer = idUser) { nombre, img, apellido, nacionalidad, categoria, verificado, trabajador_user ->

            val nombreCompleto = "$nombre $apellido"
            binding.nombre.text = nombreCompleto

            val placeholder: Drawable? =
                ContextCompat.getDrawable(this, R.drawable.img_perfil)
            constatnes_carga_imagenes_general.changer_img(
                binding.carga,
                this,
                img.toString(),
                binding.imgPerfilUser,
                null,
                "perfil",
                placeholder
            ) { cargado ->

            }
            // Validamos que nombre y apellido no estén vacíos
            val nombreValido = nombre.toString().isNotBlank() && apellido.toString().isNotBlank()
            val nombreCorrecto = binding.nombre.text.toString() == nombreCompleto

            if (nombreValido && nombreCorrecto) {
                binding.linealCargaTextoImg.isVisible = true
                binding.cargaIMGText.isVisible = false


            } else {
                binding.linealCargaTextoImg.isVisible = false
                binding.cargaIMGText.isVisible = true

                println("⚠️ Aún falta cargar algún dato")
            }
        }

        binding.nombre.text = nombre
        binding.review.text = review
        try {
            val drawableId = when (cantidad) {
                "1" -> R.drawable.start_one
                "2" -> R.drawable.start_two
                "3" -> R.drawable.start_tree
                "4" -> R.drawable.start_four
                "5" -> R.drawable.start_five
                else -> R.drawable.start_one
            }

            Glide.with(this)
                .load(drawableId)
                .into(binding.cantidadStart)
        } catch (e: Exception) {
            println(e)
        }

    }


    private fun actualizarReview(idUser: String, collectionFirebase: String, id_review: String) {
        val idTrabajador = intent.getStringExtra(Variables.idTrabajdor).toString()
        val dbReview = FirebaseFirestore.getInstance()
            .collection("Trabajadores_Usuarios_Drivers")
            .document("trabajadores")
            .collection("trabajadores")
            .document(idTrabajador)
            .collection("review")
            .document(id_review)

        val firestoreDocument = FirebaseFirestore.getInstance()
            .collection("Trabajadores_Usuarios_Drivers")
            .document("trabajadores")
            .collection("trabajadores")
            .document(idTrabajador)

        val nuevaReview = binding.nuevaReview.text.toString()
        val nuevasEstrellas = binding.EntradaCantidadStart.text.toString().toIntOrNull()
        val estrellasAnteriores = intent.getStringExtra(Variables.cantidad)?.toIntOrNull()


        // Validar entrada de estrellas antes de procesar
        if (nuevasEstrellas == null || nuevasEstrellas !in 0..5) {
            mostrarError("Las estrellas ingresadas deben estar entre 0 y 5")
            Log.e("ActualizarReview", "Las estrellas ingresadas no son válidas: $nuevasEstrellas")
            return
        }
        val hashMap = hashMapOf<String, Any>(
            Variables.editado to true,
            Variables.reseña to nuevaReview,
            Variables.cantidad to nuevasEstrellas.toString()
        )
        dbReview.set(hashMap, SetOptions.merge()).addOnSuccessListener { res ->
            Toast.makeText(
                this,
                "Estrellas actulizadas correctamente actualiza para ver los nuevos cambios",
                Toast.LENGTH_SHORT
            ).show()
            finish()
        }.addOnFailureListener { e ->
            Log.d("error_actualizar", "error al actualizar las estrellas")
        }

        firestoreDocument.get().addOnSuccessListener { res ->
            if (res.exists()) {
                val data = res.data
                val estrellas = data?.get("estrellas") as? String ?: ""

                val estrellasInt = estrellas.toInt()
                val resta = (estrellasInt - estrellasAnteriores!!) + nuevasEstrellas
                Log.d(
                    "entrellasEncontradas",
                    "$estrellasInt.toString() y el total de estrellas es $resta"
                )
                val updateMap = hashMapOf<String, Any>(
                    Variables.estrellas to resta.toString()
                )
                firestoreDocument.update(updateMap).addOnSuccessListener { res ->
                    Log.d("estrellas_Actualizadas", "estrellas actulizadas correctamente")
                }.addOnFailureListener {
                    Log.e("estrellas_Actualizadas", "error al subir las estrellas")

                }
            }
        }

    }


    // Método para mostrar errores como Toast o de otra manera
    private fun mostrarError(mensaje: String) {
        Toast.makeText(this@EditarReview, mensaje, Toast.LENGTH_SHORT).show()
    }

}



