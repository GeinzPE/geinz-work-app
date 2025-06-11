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
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import com.example.geinzwork.constantesGeneral.Variables

import com.geinzz.geinzwork.constantesGeneral.mostrarFechaDialog_horaDialog
import com.geinzz.geinzwork.databinding.ActivityCrearTrabajosRealizadosBinding

import com.geinzz.geinzwork.publicaciones_trabajadores.voleta_estado_verificacion
import com.google.android.material.imageview.ShapeableImageView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.io.ByteArrayOutputStream

class crear_trabajos_realizados : AppCompatActivity() {
    private lateinit var binding: ActivityCrearTrabajosRealizadosBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private val imageViews by lazy {
        listOf(binding.img1, binding.img2, binding.img3, binding.img4, binding.img5)
    }
    private var currentImageIndex = 0
    private val pickImage =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let {
                imageViews[currentImageIndex].setImageURI(it)

                if (currentImageIndex + 1 < imageViews.size) {
                    imageViews[currentImageIndex + 1].visibility = View.VISIBLE
                }
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
//        constantesCarrito.obtnerfechaHora(binding.hora, binding.fecha)
        binding.popup.setOnClickListener {
            popup()
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

    override fun onBackPressed() {

        val hayContenido = binding.tituloPublicacionED.text.isNotBlank() ||
                binding.descripcionServiciosED.text.isNotBlank() ||
                obtenerImagenesValidas().isNotEmpty()

        if (hayContenido) {
            AlertDialog.Builder(this)
                .setTitle("¿Cancelar publicación?")
                .setMessage("Si sales ahora, se perderá lo que hayas escrito. ¿Deseas continuar?")
                .setPositiveButton("Sí") { dialog, _ ->
                    dialog.dismiss()
                    super.onBackPressed()
                }
                .setNegativeButton("No") { dialog, _ ->
                    dialog.dismiss()
                }
                .show()
        } else {
            super.onBackPressed()
        }
    }

    private fun resetearImagenes() {
        val drawablePorDefecto = ContextCompat.getDrawable(this, R.drawable.agregar_imagen)

        imageViews.forEachIndexed { index, imageView ->
            imageView.setImageDrawable(drawablePorDefecto)
            imageView.visibility = if (index == 0) View.VISIBLE else View.GONE
        }

        currentImageIndex = 0
    }


    private fun crearPublicacion(id: String) {
        val titulo = binding.tituloPublicacionED
        val descripcion = binding.descripcionServiciosED
        val db = FirebaseFirestore.getInstance().collection(Variables.trabajadores_usuariosDB)
            .document(Variables.trabajadoresDB).collection(Variables.trabajadoresDB).document(id)
            .collection(Variables.trabajos_realizados)
        if (titulo.text.isNullOrEmpty()) {
            titulo.error = "Ingresa un titulo"
            titulo.requestFocus()
        } else if (descripcion.text.isNullOrEmpty()) {
            descripcion.error = "Ingresa una descripcion"
            descripcion.requestFocus()
        } else if (obtenerImagenesValidas().isEmpty()) {
            Toast.makeText(
                this,
                "Debe agregar al menos una imagen",
                Toast.LENGTH_SHORT
            ).show()
            return
        } else if (!binding.Acepto.isChecked) {
            Toast.makeText(
                this,
                "Acepta que realizas la publicación.",
                Toast.LENGTH_SHORT
            ).show()

        } else {
            binding.scroll.isVisible = false
            binding.progressBarContainer.isVisible = true

            val newDocRef = db.document()

            val hasmap = hashMapOf<String, Any>(
                Variables.titulo to titulo.text.toString(),
                Variables.descripcion to descripcion.text.toString(),
                Variables.hora to mostrarFechaDialog_horaDialog.obtenerHoraActual(),
                Variables.fecha to mostrarFechaDialog_horaDialog.obtenerFechaActual(),
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
                    resetearImagenes()
                    binding.tituloPublicacionED.setText("")
                    binding.descripcionServiciosED.setText("")
                    binding.Acepto.isChecked = false
                }.addOnFailureListener {
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
        popup.menu.add(Menu.NONE, 2, 2, "Trabajos publicados")
        popup.menu.add(Menu.NONE, 3, 3, "Trabajos Archivados")
        popup.menu.add(Menu.NONE, 3, 3, "Trabajos Eliminados")

        popup.show()
        popup.setOnMenuItemClickListener { item ->
            val itemID = item.itemId
            if (itemID == 1) {
                startActivity(Intent(this, voleta_estado_verificacion::class.java))
            } else if (itemID == 2) {
                startActivity(Intent(this, ver_publicaciones::class.java))
            }
            return@setOnMenuItemClickListener true
        }
    }

    private fun obtenerImagenesValidas(): List<ShapeableImageView> {
        val placeholder = ContextCompat.getDrawable(this, R.drawable.agregar_imagen)
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