//package com.geinzz.geinzwork
//
//import android.app.Activity
//import android.content.ContentValues
//import android.content.Context
//import android.content.Intent
//import android.graphics.Bitmap
//import android.graphics.BitmapFactory
//import android.graphics.drawable.Drawable
//import android.net.Uri
//import android.os.Build
//import android.os.Bundle
//import android.util.Log
//import android.widget.ImageView
//import android.widget.ProgressBar
//import android.widget.TextView
//import android.widget.Toast
//import androidx.activity.enableEdgeToEdge
//import androidx.activity.result.ActivityResultLauncher
//import androidx.activity.result.contract.ActivityResultContracts
//import androidx.appcompat.app.AppCompatActivity
//import androidx.core.graphics.drawable.toBitmap
//import androidx.core.view.ViewCompat
//import androidx.core.view.WindowInsetsCompat
//import androidx.core.view.isVisible
//import com.bumptech.glide.Glide
//import com.example.geinzwork.constantesGeneral.Variables
//import com.geinzz.geinzwork.constantesGeneral.constantesImagenes
//import com.geinzz.geinzwork.databinding.ActivityAgregarImagenBinding
//import com.google.firebase.storage.FirebaseStorage
//import kotlinx.coroutines.Dispatchers
//import kotlinx.coroutines.GlobalScope
//import kotlinx.coroutines.launch
//import java.lang.Exception
//import java.io.ByteArrayOutputStream
//
//
//class panel_publicacion : AppCompatActivity() {
//    private lateinit var binding: ActivityPa
//    private var imguri1: Uri? = null
//    private var imguri2: Uri? = null
//    private var imguri3: Uri? = null
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        binding = ActivityAgregarImagenBinding.inflate(layoutInflater)
//        enableEdgeToEdge()
//        setContentView(binding.root)
//        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
//            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
//            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
//            insets
//        }
//
//        obtenerImagenes(Variables.imgane1, binding.imagen1, binding.progrsVarimg1,binding.textoImg1)
//        obtenerImagenes(Variables.imagen2, binding.imagen2, binding.progrsVarimg2,binding.textoImg2)
//        obtenerImagenes(Variables.imagen3, binding.imagen3, binding.progrsVarimg3,binding.textoImg3)
//
//        binding.imagen1.setOnClickListener {
//            verificacionAndorid(resultadoGaleria, permiso1, "concedemos el permiso")
//        }
//        binding.imagen2.setOnClickListener {
//            verificacionAndorid(resultadoGaleria2, permiso2, "concedemos el permiso")
//        }
//        binding.imagen3.setOnClickListener {
//            verificacionAndorid(resultadoGaleria3, permiso3, "concedemos el permiso")
//        }
//    }
//
//    val nombreimg1 = Variables.imgane1
//    val nombremimg2 = Variables.imagen2
//    val nombreimg3 = Variables.imagen3
//
//
//    private fun verificacionAndorid(
//        res: ActivityResultLauncher<Intent>,
//        permiso: ActivityResultLauncher<String>,
//        texto: String
//    ) {
//
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
//            obtenerimg(res)
//        } else {
//            permiso.launch(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
//        }
//
//    }
//
//    private fun obtenerimg(res: ActivityResultLauncher<Intent>) {
//        val intent = Intent(Intent.ACTION_PICK)
//        intent.type = "image/*"
//        res.launch(intent)
//    }
//
//    val permiso1 =
//        registerForActivityResult(ActivityResultContracts.RequestPermission()) { concedido ->
//            if (concedido) {
//                obtenerimg(resultadoGaleria)
//            } else {
//                println(getString(R.string.PERMISOdENEGADO))
//            }
//        }
//    val permiso2 =
//        registerForActivityResult(ActivityResultContracts.RequestPermission()) { concedido ->
//            if (concedido) {
//                obtenerimg(resultadoGaleria2)
//            } else {
//                println(getString(R.string.PERMISOdENEGADO))
//            }
//        }
//    val permiso3 =
//        registerForActivityResult(ActivityResultContracts.RequestPermission()) { concedido ->
//            if (concedido) {
//                obtenerimg(resultadoGaleria3)
//            } else {
//                println(getString(R.string.PERMISOdENEGADO))
//            }
//        }
//
//    private val resultadoGaleria =
//        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { res ->
//            if (res.resultCode == Activity.RESULT_OK) {
//                val data = res.data
//                imguri1 = data!!.data
//                try {
//                    Glide.with(this)
//                        .load(imguri1)
//                        .placeholder(R.drawable.img_perfil)
//                        .into(binding.imagen1)
//                } catch (e: Exception) {
//                    println("error $e")
//                }
//
//                imguri1?.let { uri ->
//                    val drawable = uri.toDrawable(this)
//                    val originalBitmap = drawable?.toBitmap()
//                    val compressedBitmap =
//                        originalBitmap?.let { constantesImagenes.reducesize(drawable) }
//
//                    (binding.imagen1.setImageBitmap(compressedBitmap))
//                    compressedBitmap?.let { bitmap ->
//                        subirImagenStorage(imguri1!!, nombreimg1)
//                    }
//                }
//
//            } else {
//                Toast.makeText(this, "cancelado", Toast.LENGTH_SHORT).show()
//            }
//        }
//
//    private val resultadoGaleria2 =
//        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { res ->
//            if (res.resultCode == Activity.RESULT_OK) {
//                val data = res.data
//                imguri2 = data!!.data
//                try {
//                    Glide.with(this)
//                        .load(imguri2)
//                        .placeholder(R.drawable.img_perfil)
//                        .into(binding.imagen2)
//                } catch (e: Exception) {
//                    println("error $e")
//
//                }
//                imguri2?.let { uri ->
//                    val drawable = uri.toDrawable(this)
//                    val originalBitmap = drawable?.toBitmap()
//                    val compressedBitmap = originalBitmap?.let { reducesize(drawable) }
//
//                    (binding.imagen2.setImageBitmap(compressedBitmap))
//                    compressedBitmap?.let { bitmap ->
//                        subirImagenStorage(imguri2!!, nombremimg2)
//                    }
//                }
//            } else {
//                Toast.makeText(this, "cancelado", Toast.LENGTH_SHORT).show()
//            }
//        }
//    private val resultadoGaleria3 =
//        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { res ->
//            if (res.resultCode == Activity.RESULT_OK) {
//                val data = res.data
//                imguri3 = data!!.data
//                try {
//                    Glide.with(this)
//                        .load(imguri3)
//                        .placeholder(R.drawable.img_perfil)
//                        .into(binding.imagen3)
//                } catch (e: Exception) {
//                    println("error $e")
//
//                }
//                imguri3?.let { uri ->
//                    val drawable = uri.toDrawable(this)
//                    val originalBitmap = drawable?.toBitmap()
//                    val compressedBitmap = originalBitmap?.let { reducesize(drawable) }
//
//                    (binding.imagen3.setImageBitmap(compressedBitmap))
//                    compressedBitmap?.let { bitmap ->
//                        subirImagenStorage(imguri3!!, nombreimg3)
//                    }
//                }
//            } else {
//                Toast.makeText(this, "cancelado", Toast.LENGTH_SHORT).show()
//            }
//        }
//
//
//    private fun subirImagenStorage(uri: Uri, nombreimg: String) {
//        val id = intent.getStringExtra(Variables.iduser)
//        val rutaimg1 = "${Variables.usuarios_db}/$id/$nombreimg"
//        val contentResolver = this.contentResolver
//        GlobalScope.launch(Dispatchers.Main) {
//            val imagenComprimida = constantesImagenes.resizeAndCompressImage(
//                contentResolver,
//                uri,
//                1920,
//                1080
//            ) // Ajustar el tamaño máximo según sea necesario
//            constantesImagenes.refereciaStorage(
//                rutaimg1,
//                imagenComprimida,
//                nombreimg,
//                this@panel_publicacion
//            )
//        }
//    }
//
//    fun Uri.toDrawable(context: Context): Drawable? {
//        val contentResolver = context.contentResolver
//        val inputStram = contentResolver.openInputStream(this)
//        return Drawable.createFromStream(inputStram, this.toString())
//    }
//
//    private fun reducesize(drawable: Drawable): Bitmap? {
//        val originalBitmap = drawable.toBitmap()
//        val compressedBitmap = Bitmap.createScaledBitmap(originalBitmap, 1920, 1080, true)
//
//        val outputStream = ByteArrayOutputStream()
//        compressedBitmap.compress(Bitmap.CompressFormat.WEBP, 100, outputStream)
//        val imageByte = outputStream.toByteArray()
//        val compressedWebPBitmap = BitmapFactory.decodeByteArray(imageByte, 0, imageByte.size)
//
//        Log.d(ContentValues.TAG, "Tamaño de la imagen comprimida en WebP: ${imageByte.size} bytes")
//
//        return compressedWebPBitmap
//    }
//
//    private fun obtenerImagenes(referencia: String, img: ImageView, progressBar: ProgressBar, texto: TextView) {
//        val id = intent.getStringExtra(Variables.iduser).toString()
//        val storage = FirebaseStorage.getInstance()
//            .getReference(Variables.usuarios_db)
//            .child(id)
//            .child(referencia)
//
//
//        // Mostrar el ProgressBar antes de iniciar la carga
//        progressBar.isVisible = true
//        texto.isVisible = false
//
//        storage.downloadUrl.addOnSuccessListener { uri ->
//            val urlImg = uri.toString()
//
//            // Validar si la URL no está vacía
//            if (urlImg.isNotEmpty()) {
//                try {
//                    Glide.with(this)
//                        .load(urlImg)
//                        .into(img)
//                    // Ocultar ProgressBar y TextView después de cargar la imagen
//                    progressBar.isVisible = false
//                    texto.isVisible = false
//                } catch (e: Exception) {
//                    println(e)
//                    // Manejar errores en Glide
//                    progressBar.isVisible = false
//                    texto.isVisible = true
//                }
//            } else {
//                // URL vacía: mostrar el mensaje
//                progressBar.isVisible = false
//                texto.isVisible = true
//            }
//        }.addOnFailureListener { e ->
//            Log.e("no_img", "Error al obtener la imagen: ${e.message}")
//            // Mostrar el mensaje en caso de error
//            progressBar.isVisible = false
//            texto.isVisible = true
//        }
//    }
//
//
//}
//
//
