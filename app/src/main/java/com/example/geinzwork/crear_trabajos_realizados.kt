package com.geinzz.geinzwork

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.Menu
import android.widget.PopupMenu
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import com.example.geinzwork.constantesGeneral.Variables
import com.geinzz.geinzwork.constantesGeneral.constantesCarrito
import com.geinzz.geinzwork.databinding.ActivityCrearTrabajosRealizadosBinding
import com.geinzz.geinzwork.hora.ImageDialogFragmentURI
import com.geinzz.geinzwork.publicaciones_trabajadores.agregar_redes
import com.geinzz.geinzwork.publicaciones_trabajadores.voleta_estado_verificacion
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage

class crear_trabajos_realizados : AppCompatActivity() {
    private lateinit var binding: ActivityCrearTrabajosRealizadosBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private var Imagen_Trabajo: Uri? = null
    private var uriPasada: Uri? = null
    private val pciMEdia =
        registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
            if (uri != null) {
                Imagen_Trabajo = uri
                binding.imagenTrabajo.setImageURI(uri)
                uriPasada = Imagen_Trabajo
            } else {
                println("Imagen no seleccionada")
            }
        }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCrearTrabajosRealizadosBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        firebaseAuth = FirebaseAuth.getInstance()
        constantesCarrito.obtnerfechaHora(binding.hora, binding.fecha)
        binding.popup.setOnClickListener {
            popup()
        }
        val plan = intent.getStringExtra(Variables.plan).toString()
        val db = FirebaseFirestore.getInstance().collection(Variables.trabajadores_usuariosDB)
            .document(Variables.trabajadoresDB).collection(Variables.trabajadoresDB)
            .document(firebaseAuth.uid.toString())
            .collection(Variables.trabajos_realizados)

        db.get().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val count = task.result?.size() ?: 0
                if (count >= 1) {
                    binding.PublicacionesRealizadas.text = count.toString()
                    val total = binding.TotalPublicaciones.text.toString()
                    val restante = total.toInt() - count
                    binding.PublicacionesDisponibles.text = restante.toString()
                } else {
                    println("Error al obtener la cantidad de publicaciones")
                }

            } else {
                println("Error al obtener la cantidad de publicaciones")
            }
        }
        when (plan) {
            "B" -> {
                binding.plan.text = "B"
                binding.TotalPublicaciones.text = "10"
            }

            "C" -> {
                binding.plan.text = "C"
                binding.TotalPublicaciones.text = "20"
            }
        }
        binding.publicar.setOnClickListener {
            crearPublicacion(firebaseAuth.uid.toString())
        }
        binding.imagenTrabajo.setOnClickListener {
            pciMEdia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }
        binding.imagenTrabajo.setOnLongClickListener {
            if (uriPasada == null) {
                Toast.makeText(this, "Seleccione una imagen", Toast.LENGTH_SHORT).show()
                false
            } else {
                uriPasada?.let { uri ->
                    val dialogFragment = ImageDialogFragmentURI.newInstance(uri)
                    dialogFragment.show(supportFragmentManager, "image_dialog")
                    true
                } ?: run {
                    Toast.makeText(this, "No URI available", Toast.LENGTH_SHORT).show()
                    false
                }
            }
        }

    }

    private fun crearPublicacion(id: String) {
        val titulo = binding.tituloPublicacionED
        val descripcion = binding.descripcionServiciosED
        val hora = binding.hora
        val fecha = binding.fecha
        val totalPublicaciones = binding.TotalPublicaciones.text.toString()
        val publicaionesRealizadas = binding.PublicacionesRealizadas.text.toString()
        val db = FirebaseFirestore.getInstance().collection(Variables.trabajadores_usuariosDB)
            .document(Variables.trabajadoresDB).collection(Variables.trabajadoresDB).document(id)
            .collection(Variables.trabajos_realizados)
        if (!binding.Acepto.isChecked) {
            Toast.makeText(
                this,
                "Acepta que realizas la publicación.",
                Toast.LENGTH_SHORT
            ).show()
        } else if (totalPublicaciones.toInt() == publicaionesRealizadas.toInt()) {
            Toast.makeText(
                this,
                "Alcanzaste tu máximo de publicaciones mensuales. Adquiere un nuevo plan o contáctate con Geinz.",
                Toast.LENGTH_SHORT
            ).show()
        } else if (Imagen_Trabajo == null) {
            Toast.makeText(this, "Seleccione una imagen", Toast.LENGTH_SHORT).show()
        } else if (titulo.text.isNullOrEmpty()) {
            Toast.makeText(this, "La publicacion necesita un titulo", Toast.LENGTH_SHORT).show()
        } else if (descripcion.text.isNullOrEmpty()) {
            Toast.makeText(this, "La publicacion necesita una descripcion", Toast.LENGTH_SHORT)
                .show()
        } else {
            binding.scroll.isVisible = false
            binding.progressBarContainer.isVisible = true
            val newDocRef = db.document()

            val hasmap = hashMapOf<String, Any>(
                Variables.titulo to titulo.text.toString(),
                Variables.descripcion to descripcion.text.toString(),
                Variables.hora to hora.text.toString(),
                Variables.fecha to fecha.text.toString(),
                Variables.id to newDocRef.id
            )

            Imagen_Trabajo?.let { imageUri ->
                val storageRef = FirebaseStorage.getInstance()
                    .getReference("usuarios/$id/IMG_trabajos/${System.currentTimeMillis()}.jpg")

                storageRef.putFile(imageUri)
                    .addOnSuccessListener { taskSnapshot ->
                        // Obtener la URL de descarga
                        storageRef.downloadUrl.addOnSuccessListener { uri ->
                            val imageUrl = uri.toString()
                            hasmap[Variables.imageUrl] = imageUrl

                            // Ahora agregar los datos al documento previamente creado
                            newDocRef.set(hasmap)
                                .addOnSuccessListener {
                                    Toast.makeText(
                                        this,
                                        "Publicacion creada exitosamente",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    binding.progressBarContainer.isVisible = false
                                    binding.scroll.isVisible = true
                                    onBackPressed()
                                }
                                .addOnFailureListener {
                                    Toast.makeText(
                                        this,
                                        "Error al crear la publicacion",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                        }.addOnFailureListener {
                            Toast.makeText(
                                this,
                                "Error al obtener la URL de la imagen",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                    .addOnFailureListener {
                        Toast.makeText(this, "Error al subir la imagen", Toast.LENGTH_SHORT)
                            .show()
                    }
            } ?: run {
                Toast.makeText(this, "No se seleccionó ninguna imagen", Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }

    fun popup() {
        val popup = PopupMenu(this, binding.popup)
        popup.menu.add(Menu.NONE, 1, 1, "Estado de pago y verificacion")
        popup.menu.add(Menu.NONE, 2, 2, "Ver Publicaciones")
        popup.menu.add(Menu.NONE, 3, 3, "Agregar redes sociales")
        popup.show()
        popup.setOnMenuItemClickListener { item ->
            val itemID = item.itemId
            if (itemID == 1) {
                startActivity(Intent(this, voleta_estado_verificacion::class.java))
            } else if (itemID == 2) {
                startActivity(Intent(this, ver_publicaciones::class.java))
            } else if (itemID == 3) {
                startActivity(Intent(this, agregar_redes::class.java))
            }
            return@setOnMenuItemClickListener true
        }
    }


}