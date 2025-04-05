package com.geinzz.geinzwork

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
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
        val nuevaReseview = intent.getStringExtra(Variables.nuevaReseña).toString()
        val nuevaCantidadStart = intent.getStringExtra(Variables.cantidadStart).toString()
        val TipoEditado = intent.getStringExtra(Variables.TipoEditado).toString()
        val iduser = intent.getStringExtra(Variables.iduser).toString()
        seterReviewAnterior(iduser)
        binding.nuevaReview.setText(nuevaReseview)
        binding.EntradaCantidadStart.setText(nuevaCantidadStart)


        when (TipoEditado) {
            "CuentaTienda" -> {
                binding.ActulizarReview.setOnClickListener {
                    val idUserNewReview = intent.getStringExtra(Variables.iduser).toString()
                    actualizarReview(
                        idUserNewReview,
                        Variables.trabajadores_usuariosDB,
                    )
                    Log.d("pasmos_datos", "${idUserNewReview} ${Variables.trabajadores_usuariosDB}")
                }
            }

            Variables.CuentaFreelancer -> {
                binding.ActulizarReview.setOnClickListener {
                    val idUserNewReview = intent.getStringExtra(Variables.iduser).toString()
                    actualizarReview(
                        idUserNewReview,
                        Variables.trabajadores_usuariosDB,
                    )
                    Log.d("pasmos_datos", "${idUserNewReview} ${Variables.trabajadores_usuariosDB}")

                }
            }
        }
    }

    private fun seterReviewAnterior(idUser: String) {
        val cantidad = intent.getStringExtra(Variables.cantidad).toString()
        val imgPerfil = intent.getStringExtra(Variables.imgPerfil).toString()
        val nombre = intent.getStringExtra(Variables.nombre).toString()
        val review = intent.getStringExtra(Variables.review).toString()

        constantesCarrito.setearDatosUsuarioImgNombre(idUSer = idUser) { nombre, img, apellido ->

            val nombreCompleto = "$nombre $apellido"
            binding.nombre.text = nombreCompleto

            // Asumimos que changer_img tiene un callback con cargado = true cuando la imagen está lista
            constatnes_carga_imagenes_general.changer_img(
                binding.carga,
                this,
                img.toString(),
                binding.imgPerfilUser,
                null,
                "perfil",
                null
            ) { cargado ->
                // Validamos que nombre y apellido no estén vacíos
                val nombreValido = nombre.toString().isNotBlank() && apellido.toString().isNotBlank()
                val nombreCorrecto = binding.nombre.text.toString() == nombreCompleto

                if (nombreValido && nombreCorrecto && cargado) {
                    binding.linealCargaTextoImg.isVisible=true
                    binding.cargaIMGText.isVisible=false

                    // Aquí puedes ejecutar cualquier lógica que dependa de que todo esté listo
                } else {
                    binding.linealCargaTextoImg.isVisible=false
                    binding.cargaIMGText.isVisible=true

                    println("⚠️ Aún falta cargar algún dato")
                }
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
        try {
            Glide.with(this)
                .load(imgPerfil)
                .placeholder(R.drawable.img_perfil)
                .into(binding.imgPerfilUser)

        } catch (e: Exception) {
            println(e)
        }
    }


    private fun actualizarReview(idUser: String, collectionFirebase: String) {
        val idTrabajador = intent.getStringExtra(Variables.idTrabajdor).toString()
        val dbReview = FirebaseFirestore.getInstance()
            .collection("Trabajadores_Usuarios_Drivers")
            .document("trabajadores")
            .collection("trabajadores")
            .document(idTrabajador)
            .collection("review")
            .document(idUser)

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

        firestoreDocument.get().addOnSuccessListener { documento ->
            if (!documento.exists()) {
                mostrarError("El documento no existe")
                Log.e("ActualizarReview", "El documento del trabajador $idTrabajador no existe.")
                return@addOnSuccessListener
            }

            val data = documento.data
            val estrellasTotales = (data?.get(Variables.estrellas) as? String)?.toIntOrNull()

            if (estrellasTotales == null || estrellasAnteriores == null) {
                mostrarError("Datos inválidos para calcular estrellas")
                Log.e(
                    "ActualizarReview",
                    "Datos inválidos: estrellasTotales=$estrellasTotales, estrellasAnteriores=$estrellasAnteriores"
                )
                return@addOnSuccessListener
            }

            // Actualizar estrellas totales (restar las anteriores, agregar las nuevas)
            val estrellasActualizadas = (estrellasTotales - estrellasAnteriores) + nuevasEstrellas
            Log.d(
                "ActualizarReview",
                "Cálculo de estrellas: $estrellasTotales - $estrellasAnteriores + $nuevasEstrellas = $estrellasActualizadas"
            )

            // Validar el rango de estrellas actualizadas
            if (estrellasActualizadas !in 0..5 * 100) { // Ajusta el rango según la lógica de tu aplicación
                mostrarError("El total de estrellas debe ser válido")
                Log.e(
                    "ActualizarReview",
                    "El total de estrellas actualizadas no es válido: $estrellasActualizadas"
                )
                return@addOnSuccessListener
            }

            val updateMap = hashMapOf<String, Any>(
                Variables.estrellas to estrellasActualizadas.toString()
            )

            // Actualizar la colección "review"
            dbReview.set(hashMap, SetOptions.merge())
                .addOnSuccessListener {
                    Log.d("ActualizarReview", "Reseña actualizada exitosamente en dbReview.")
                }.addOnFailureListener { exception ->
                    Log.e(
                        "ActualizarReview",
                        "Error al actualizar la reseña: ${exception.message}",
                        exception
                    )
                }

            // Actualizar la colección principal
            firestoreDocument.update(updateMap)
                .addOnSuccessListener {
                    Toast.makeText(
                        this@EditarReview,
                        "Estrellas actualizadas correctamente",
                        Toast.LENGTH_SHORT
                    ).show()
                    Log.d(
                        "ActualizarReview",
                        "Estrellas actualizadas exitosamente en firestoreDocument."
                    )
                    finish()
                }.addOnFailureListener { exception ->
                    Log.e(
                        "ActualizarReview",
                        "Error al actualizar las estrellas: ${exception.message}",
                        exception
                    )
                }

        }.addOnFailureListener { exception ->
            Log.e(
                "ActualizarReview",
                "Error al obtener el documento: ${exception.message}",
                exception
            )
        }
    }


    // Método para mostrar errores como Toast o de otra manera
    private fun mostrarError(mensaje: String) {
        Toast.makeText(this@EditarReview, mensaje, Toast.LENGTH_SHORT).show()
    }

}



