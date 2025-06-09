package com.geinzz.geinzwork

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.View
import android.widget.PopupMenu
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import com.example.geinzwork.constantesGeneral.Variables
import com.example.geinzwork.dataclass.MiViewModel
import com.geinzz.geinzwork.constantesGeneral.constantesCarrito
import com.geinzz.geinzwork.databinding.ActivityCrearTrabajosRealizadosBinding
import com.geinzz.geinzwork.hora.ImageDialogFragmentURI
import com.geinzz.geinzwork.publicaciones_trabajadores.agregar_redes
import com.geinzz.geinzwork.publicaciones_trabajadores.voleta_estado_verificacion
import com.google.android.material.imageview.ShapeableImageView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.io.ByteArrayOutputStream

class crear_trabajos_realizados : AppCompatActivity() {
    private lateinit var binding: ActivityCrearTrabajosRealizadosBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private var Imagen_Trabajo: Uri? = null
    private var uriPasada: Uri? = null
    private val imageViews by lazy {
        listOf(binding.img1, binding.img2, binding.img3, binding.img4, binding.img5)
    }
    private val viewModel: MiViewModel by viewModels()
    private var currentImageIndex = 0
    private val pickImage =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let {
                // Establece la imagen seleccionada
                imageViews[currentImageIndex].setImageURI(it)

                // Si hay otra ImageView disponible, la hacemos visible
                if (currentImageIndex + 1 < imageViews.size) {
                    imageViews[currentImageIndex + 1].visibility = View.VISIBLE
                }

                // Hacer scroll automático al final
                binding.horizontalScrollView.post {
                    binding.horizontalScrollView.fullScroll(View.FOCUS_RIGHT)
                }
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

        binding.horizontalScrollView.post {
            binding.horizontalScrollView.fullScroll(View.FOCUS_RIGHT)
        }
        imageViews.forEach { it.visibility = View.GONE }

        imageViews[0].visibility = View.VISIBLE

        imageViews.forEachIndexed { index, imageView ->
            imageView.setOnClickListener {
                currentImageIndex = index
                pickImage.launch("image/*")
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

            newDocRef.set(hasmap)
                .addOnSuccessListener {
                    Toast.makeText(
                        this,
                        "Publicacion creada exitosamente",
                        Toast.LENGTH_SHORT
                    ).show()
                    binding.progressBarContainer.isVisible = false
                    binding.scroll.isVisible = true
                    guardar_img_storage(id)
                    onBackPressed()
                }    .addOnFailureListener {
                    Toast.makeText(
                        this,
                        "Error al crear la publicacion",
                        Toast.LENGTH_SHORT
                    ).show()
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

    private fun obtenerImagenesValidas(): List<ShapeableImageView> {
        val placeholder = ContextCompat.getDrawable(this, R.drawable.img_perfil)
        return imageViews.filter { imageView ->
            imageView.drawable != null &&
                    imageView.drawable.constantState != placeholder?.constantState
        }
    }

    private fun guardar_img_storage(id_publicacion: String) {
        val imagenesValidas = obtenerImagenesValidas()

        imagenesValidas.forEachIndexed { index, imageView ->
            val bitmap = (imageView.drawable as BitmapDrawable).bitmap
            val baos = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
            val data = baos.toByteArray()

            val nombreArchivo = "imagen_$index.jpg"
            val storageRef = FirebaseStorage.getInstance().reference
                .child("usuarios/${firebaseAuth.uid.toString()}/trabajos_realizados/$id_publicacion/$nombreArchivo")

            storageRef.putBytes(data)
                .addOnSuccessListener {
                    Log.d("Storage", "Imagen subida con éxito: $nombreArchivo")
                }
                .addOnFailureListener { e ->
                    Log.e("Storage", "Error al subir imagen $nombreArchivo: ${e.message}")
                }
        }

    }


}