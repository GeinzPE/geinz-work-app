package com.geinzz.geinzwork.servicios_geinz

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import android.os.Handler
import android.os.Looper
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import com.bumptech.glide.Glide
import com.example.geinzwork.constantesGeneral.Variables
import com.geinzz.geinzwork.R
import com.geinzz.geinzwork.databinding.ActivityCambiarImagenesPublicidadNoticiasServiciosBinding
import com.google.android.material.imageview.ShapeableImageView
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference

class cambiar_imagenes_publicidad_noticias_servicios : AppCompatActivity() {
    private lateinit var binding: ActivityCambiarImagenesPublicidadNoticiasServiciosBinding
    private var img_pincipal: Uri? = null
    private var img1_uir1: Uri? = null
    private var img2_uir2: Uri? = null
    private var img3_uir3: Uri? = null
    private var img4_uir4: Uri? = null
    private var img5_uir5: Uri? = null
    val storage = FirebaseStorage.getInstance().reference
    val baner = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) {
            img_pincipal = uri
            binding.imgPublicidad.setImageURI(uri)
        } else {
            println(getString(R.string.ImgNoSeleccionada))
        }
    }
    val img1 = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) {
            img1_uir1 = uri
            binding.img1.setImageURI(uri)
        } else {
            println(getString(R.string.ImgNoSeleccionada))
        }
    }
    val img2 = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) {
            img2_uir2 = uri
            binding.img2.setImageURI(uri)
        } else {
            println(getString(R.string.ImgNoSeleccionada))
        }
    }
    val img3 = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) {
            img3_uir3 = uri
            binding.img3.setImageURI(uri)
        } else {
            println(getString(R.string.ImgNoSeleccionada))
        }
    }
    val img4 = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) {
            img4_uir4 = uri
            binding.img4.setImageURI(uri)
        } else {
            println(getString(R.string.ImgNoSeleccionada))
        }
    }
    val img5 = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) {
            img5_uir5 = uri
            binding.img5.setImageURI(uri)
        } else {
            println(getString(R.string.ImgNoSeleccionada))
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCambiarImagenesPublicidadNoticiasServiciosBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val tipo = intent.getStringExtra(Variables.tipo_peticion)
        binding.imgPublicidad.setOnClickListener {
            baner.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }
        binding.img1.setOnClickListener {
            img1.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }
        binding.img2.setOnClickListener {
            img2.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }
        binding.img3.setOnClickListener {
            img3.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }
        binding.img4.setOnClickListener {
            img4.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }
        binding.img5.setOnClickListener {
            img5.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }
        when (tipo) {
            Variables.tipoNoticia -> {
                val idNoticia = intent.getStringExtra(Variables.idNoticia).toString()
                binding.img1.isVisible = true
                binding.img2.isVisible = true
                binding.imgPublicidad.isVisible = true
                binding.agregar.setOnClickListener {
                    subir_noticias(idNoticia)
                    binding.scrollimg.isVisible = false
                    binding.linealCargandoImg.isVisible = true
                }
                obtner_img_firestore_noticias(idNoticia)
                binding.texto.text = getString(R.string.textoNoticia)
            }

            Variables.tipoPublicidad -> {
                binding.texto.text = getString(R.string.textoPublicidad)
                cambiarAltura(binding.imgPublicidad, 200, this)
                val plan = intent.getStringExtra(Variables.plan)
                val documento1 = intent.getStringExtra(Variables.documento1IMG)
                val docuemnto2 = intent.getStringExtra(Variables.documento2IMG)
                when (plan) {
                    Variables.basico -> {
                        mostrarImagenes(1)
                        binding.agregar.setOnClickListener {
                            subir_img_publicaciones(documento1!!, docuemnto2!!, Variables.basico)
                            binding.scrollimg.isVisible = false
                            binding.linealCargandoImg.isVisible = true
                        }
                        obtener_imagen_publicaciones(documento1!!, docuemnto2!!, 0)
                    }

                    Variables.avanzado -> {
                        mostrarImagenes(4)
                        binding.agregar.setOnClickListener {
                            subir_img_publicaciones(documento1!!, docuemnto2!!, Variables.avanzado)
                            binding.scrollimg.isVisible = false
                            binding.linealCargandoImg.isVisible = true
                        }
                        obtener_imagen_publicaciones(documento1!!, docuemnto2!!, 4)
                    }

                    Variables.premiun -> {
                        mostrarImagenes(6)
                        binding.agregar.setOnClickListener {
                            subir_img_publicaciones(documento1!!, docuemnto2!!, Variables.premiun)
                            binding.scrollimg.isVisible = false
                            binding.linealCargandoImg.isVisible = true
                        }
                        obtener_imagen_publicaciones(documento1!!, docuemnto2!!, 6)

                    }
                }
            }
        }

    }

    private fun subir_img_publicaciones(docuemnto1: String, docuemnto2: String, plan: String) {
        val tiempoInicio = System.currentTimeMillis() // ← Tiempo de inicio

        val refFirestore = FirebaseFirestore.getInstance()
            .collection(Variables.anuncios)
            .document(docuemnto1)
            .collection(Variables.anuncios)
            .document(docuemnto2)

        val storage = FirebaseStorage.getInstance().reference
            .child(Variables.anuncios)
            .child(docuemnto1)
            .child(docuemnto2)

        val storageImgPublicitarias = storage.child(Variables.Imagenes_publicitarias)

        val scrollImg = binding.scrollimg
        val linealCargandoImg = binding.linealCargandoImg

        scrollImg.isVisible = false
        linealCargandoImg.isVisible = true

        var tareasFinalizadas = 0
        var totalTareas = 1 // Imagen principal al menos

        img_pincipal?.let { uri ->
            refFirestore.get().addOnSuccessListener { res ->
                if (res.exists()) {
                    val imagenRef = storage.child(Variables.Foto_publicidadJPG)
                    imagenRef.putFile(uri)
                        .addOnSuccessListener {
                            imagenRef.downloadUrl.addOnSuccessListener { url ->
                                val hashMap = hashMapOf<String, Any>(
                                    Variables.imagenUrl to url.toString()
                                )
                                refFirestore.set(hashMap, SetOptions.merge())
                                    .addOnSuccessListener {
                                        Toast.makeText(
                                            this,
                                            "imagen cambiada correctamente",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                        tareasFinalizadas++
                                        verificarFinal(tareasFinalizadas, totalTareas, tiempoInicio)
                                    }
                                    .addOnFailureListener {
                                        Toast.makeText(
                                            this,
                                            "Error al cambiar la imagen",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                        tareasFinalizadas++
                                        verificarFinal(tareasFinalizadas, totalTareas, tiempoInicio)
                                    }
                            }.addOnFailureListener {
                                Toast.makeText(
                                    this,
                                    "Error al obtener la imagen de descarga",
                                    Toast.LENGTH_SHORT
                                ).show()
                                tareasFinalizadas++
                                verificarFinal(tareasFinalizadas, totalTareas, tiempoInicio)
                            }
                        }.addOnFailureListener {
                            Log.e("misma_img", "La imagen principal no se subió")
                            tareasFinalizadas++
                            verificarFinal(tareasFinalizadas, totalTareas, tiempoInicio)
                        }
                } else {
                    Toast.makeText(this, "El documento no existe", Toast.LENGTH_SHORT).show()
                    tareasFinalizadas++
                    verificarFinal(tareasFinalizadas, totalTareas, tiempoInicio)
                }
            }.addOnFailureListener {
                Log.e("error_doc_img", "Error al obtener el documento de Firestore")
                tareasFinalizadas++
                verificarFinal(tareasFinalizadas, totalTareas, tiempoInicio)
            }
        } ?: run {
            Toast.makeText(this, "No se proporcionó una imagen principal", Toast.LENGTH_SHORT)
                .show()
            tareasFinalizadas++
            verificarFinal(tareasFinalizadas, totalTareas, tiempoInicio)
        }

        val imagenes = when (plan) {
            Variables.avanzado -> listOf(
                Pair(Variables.img1JPG, img1_uir1),
                Pair(Variables.img2JPG, img2_uir2),
                Pair(Variables.img3JPG, img3_uir3)
            )

            Variables.premiun -> listOf(
                Pair(Variables.img1JPG, img1_uir1),
                Pair(Variables.img2JPG, img2_uir2),
                Pair(Variables.img3JPG, img3_uir3),
                Pair(Variables.img4JPG, img4_uir4),
                Pair(Variables.img5JPG, img5_uir5)
            )

            else -> emptyList()
        }

        totalTareas += imagenes.size

        imagenes.forEach { (nombreArchivo, uri) ->
            uri?.let {
                val imagenRef = storageImgPublicitarias.child(nombreArchivo)
                imagenRef.putFile(it)
                    .addOnSuccessListener {
                        obtener_imagen_publicaciones(docuemnto1, docuemnto2, 0)
                        Toast.makeText(
                            this,
                            "Imagen $nombreArchivo subida correctamente",
                            Toast.LENGTH_SHORT
                        ).show()
                        tareasFinalizadas++
                        verificarFinal(tareasFinalizadas, totalTareas, tiempoInicio)
                    }
                    .addOnFailureListener {
                        Toast.makeText(this, "Error al subir $nombreArchivo", Toast.LENGTH_SHORT)
                            .show()
                        tareasFinalizadas++
                        verificarFinal(tareasFinalizadas, totalTareas, tiempoInicio)
                    }
            } ?: run {
                Log.d("misma_img", "No se proporcionó imagen para $nombreArchivo")
                tareasFinalizadas++
                verificarFinal(tareasFinalizadas, totalTareas, tiempoInicio)
            }
        }
    }

    private fun verificarFinal(completadas: Int, total: Int, inicio: Long) {
        if (completadas == total) {
            val fin = System.currentTimeMillis()
            val segundos = (fin - inicio) / 1000
            Handler(Looper.getMainLooper()).postDelayed({
                binding.scrollimg.isVisible = true
                binding.linealCargandoImg.isVisible = false
            }, segundos)
            Log.d("tiempo_subida", "Subida total en $segundos segundos")
        }
    }


    private fun obtener_imagen_publicaciones(
        docuemnto1: String,
        docuemnto2: String,
        numImagenes: Int,
    ) {
        val storage =
            FirebaseStorage.getInstance().getReference(Variables.anuncios).child(docuemnto1)
                .child(docuemnto2)
        val storag2 =
            FirebaseStorage.getInstance().getReference(Variables.anuncios).child(docuemnto1)
                .child(docuemnto2).child(Variables.Imagenes_publicitarias)

        cargarImagen(storage.child(Variables.Foto_publicidadJPG), binding.imgPublicidad)

        if (numImagenes >= 1) cargarImagen(storag2.child(Variables.img1JPG), binding.img1)
        if (numImagenes >= 2) cargarImagen(storag2.child(Variables.img2JPG), binding.img2)
        if (numImagenes >= 3) cargarImagen(storag2.child(Variables.img3JPG), binding.img3)
        if (numImagenes >= 4) cargarImagen(storag2.child(Variables.img4JPG), binding.img4)
        if (numImagenes >= 5) cargarImagen(storag2.child(Variables.img5JPG), binding.img5)
    }

    private fun subir_noticias(chilOtros: String) {
        val storage = FirebaseStorage.getInstance().reference
        val storageOtros = storage.child(Variables.noticiasImagenesDB).child(chilOtros)
        val storagePrincipal =
            storage.child(Variables.noticiasImagenesDB).child(chilOtros).child(Variables.principal)
        val refFirestore =
            FirebaseFirestore.getInstance().collection(Variables.noticiasDB).document(chilOtros)

        img_pincipal?.let { uri ->
            val tiempoInicioPrincipal = System.currentTimeMillis()
            refFirestore.get().addOnSuccessListener { res ->
                if (res.exists()) {
                    val imagenRef = storagePrincipal.child(Variables.PrincipalJPG)
                    imagenRef.putFile(uri).addOnSuccessListener {
                        val tiempoFinPrincipal = System.currentTimeMillis()

                        val tipoSubido = tiempoFinPrincipal - tiempoInicioPrincipal

                        Log.d(
                            "tiempo_subida",
                            "Principal: ${(tiempoFinPrincipal - tiempoInicioPrincipal)} ms"
                        )
                        Handler(Looper.getMainLooper()).postDelayed({
                            binding.scrollimg.isVisible = true
                            binding.linealCargandoImg.isVisible = false
                        }, tipoSubido)
                        imagenRef.downloadUrl.addOnSuccessListener { url ->
                            val hashMap = hashMapOf<String, Any>(
                                Variables.imagenUrl to url.toString()
                            )
                            refFirestore.set(hashMap, SetOptions.merge()).addOnSuccessListener {
                                Toast.makeText(
                                    this,
                                    "imagen cambiada correctamente",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }.addOnFailureListener {
                                Toast.makeText(
                                    this,
                                    "Error al cambiar la imagen",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }.addOnFailureListener {
                            Toast.makeText(
                                this,
                                "Error al obtener la imagen de descarga",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }.addOnFailureListener {
                        binding.scrollimg.isVisible = true
                        binding.linealCargandoImg.isVisible = false
                        Toast.makeText(
                            this,
                            "La imagen principal no se subió o se dejó la misma imagen",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                } else {
                    Log.e("error_doc_img", "Error al obtener el documento de Firestore")
                }
            }.addOnFailureListener {
                Log.e("error_doc_img", "Error al obtener el documento de Firestore")
            }
        } ?: run {
            Log.d("misma_img", "No se proporcionó una imagen nueva por lo que sigue igual.")
        }

        val imagenes = listOf(
            Pair(Variables.img1JPG, img1_uir1),
            Pair(Variables.img2JPG, img2_uir2),
        )

        imagenes.forEach { (nombreArchivo, imagenUri) ->
            imagenUri?.let { uri ->
                val tiempoInicio = System.currentTimeMillis()
                val imagenRef = storageOtros.child(nombreArchivo)
                imagenRef.putFile(uri)
                    .addOnSuccessListener {
                        val tiempoFin = System.currentTimeMillis()
                        val tipo_subido = (tiempoFin - tiempoInicio)
                        Log.d("tiempo_subida", "$nombreArchivo: ${(tiempoFin - tiempoInicio)} ms")

                        Handler(Looper.getMainLooper()).postDelayed({
                            binding.scrollimg.isVisible = true
                            binding.linealCargandoImg.isVisible = false
                            Toast.makeText(
                                this,
                                "Imagen subida correctamente",
                                Toast.LENGTH_SHORT
                            ).show()
                        }, tipo_subido)
                        obtner_img_firestore_noticias(chilOtros)
                    }
                    .addOnFailureListener {
                        binding.scrollimg.isVisible = true
                        binding.linealCargandoImg.isVisible = false
                        Toast.makeText(
                            this,
                            "La imagen $nombreArchivo no se subió o se dejó la misma imagen",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
            } ?: run {
                Log.d(
                    "misma_img",
                    "No se proporcionó una imagen nueva para $nombreArchivo, por lo que sigue igual."
                )
            }
        }
    }


    private fun obtner_img_firestore_noticias(chilOtros: String) {
        val storageOtros = storage.child(Variables.noticiasImagenesDB).child(chilOtros)
        val storagePrincipal = storageOtros.child(Variables.principal)

        cargarImagen(storageOtros.child(Variables.img1JPG), binding.img1)
        cargarImagen(storageOtros.child(Variables.img2JPG), binding.img2)
        cargarImagen(storagePrincipal.child(Variables.PrincipalJPG), binding.imgPublicidad)
    }

    private fun cargarImagen(storageRef: StorageReference, imageView: ShapeableImageView) {
        storageRef.downloadUrl.addOnSuccessListener { url ->
            Glide.with(this)
                .load(url)
                .placeholder(R.drawable.cargando_img_geinz_500)
                .into(imageView)
        }.addOnFailureListener {
            Log.d("img_not_found", "No se encontro imagen en: ${storageRef.name}")
        }
    }


    fun cambiarAltura(imageView: ShapeableImageView, alturaDp: Int, context: Context) {
        val layoutParams = imageView.layoutParams
        layoutParams.height = dpToPx(alturaDp, context)
        imageView.layoutParams = layoutParams
    }

    fun dpToPx(dp: Int, context: Context): Int {
        return (dp * context.resources.displayMetrics.density).toInt()
    }

    fun mostrarImagenes(cantidad: Int) {
        val imageViews = listOf(
            binding.imgPublicidad,
            binding.img1,
            binding.img2,
            binding.img3,
            binding.img4,
            binding.img5
        )
        for (i in 0 until cantidad) {
            imageViews[i].isVisible = true
        }
    }


}