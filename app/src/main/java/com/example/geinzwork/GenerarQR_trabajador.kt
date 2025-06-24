package com.geinzz.geinzwork

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.Menu
import android.view.View
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import com.example.geinzwork.constantesGeneral.PinShortcut_general
import com.example.geinzwork.constantesGeneral.Variables
import com.geinzz.geinzwork.databinding.ActivityGenerarQrTrabajadorBinding
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.dynamiclinks.androidParameters
import com.google.firebase.dynamiclinks.dynamicLinks
import com.google.firebase.dynamiclinks.googleAnalyticsParameters
import com.google.firebase.dynamiclinks.iosParameters
import com.google.firebase.dynamiclinks.itunesConnectAnalyticsParameters
import com.google.firebase.dynamiclinks.shortLinkAsync
import com.google.firebase.dynamiclinks.socialMetaTagParameters
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.storage.FirebaseStorage
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import com.google.zxing.common.BitMatrix
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import kotlin.math.max


class GenerarQR_trabajador : AppCompatActivity() {
    private lateinit var binding: ActivityGenerarQrTrabajadorBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private val REQUEST_WRITE_STORAGE_PERMISSION = 1

    @SuppressLint("SuspiciousIndentation")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGenerarQrTrabajadorBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        firebaseAuth = FirebaseAuth.getInstance()
        if (firebaseAuth.currentUser != null) {
            binding.linealLayoutPrincipal.isVisible = true
            Toast.makeText(this, "entramos a la actividad", Toast.LENGTH_SHORT).show()
            val info = intent.getStringExtra(Variables.info).toString()
            val idTrabajdor = intent.getStringExtra(Variables.idTrabajdor).toString()
            val db = FirebaseFirestore.getInstance()
                .collection(Variables.trabajadores_usuariosDB)
                .document(Variables.trabajadoresDB)
                .collection(Variables.trabajadoresDB)
                .document(firebaseAuth.uid.toString())

            val db2 = FirebaseFirestore.getInstance()
                .collection(Variables.trabajadores_usuariosDB)
                .document(Variables.trabajadoresDB)
                .collection(Variables.trabajadoresDB)
                .document(idTrabajdor)
            when (info) {

                Variables.info -> {
                    db2.get().addOnSuccessListener { res ->
                        if (res.exists()) {
                            val data = res.data
                            val nombre = data?.get(Variables.nombre) as? String
                            val apellido = data?.get(Variables.apellido) as? String
                            binding.nombreTrabajador.text = "${nombre} ${apellido}"

                            // Generar QR después de obtener los datos
                            val qrColor = ContextCompat.getColor(this, R.color.violetaQR)
                            val backgroundColor = ContextCompat.getColor(this, R.color.white)

                            val qrCode = generateQRCode(
                                text = idTrabajdor,
                                size = 900,
                                qrColor = qrColor,
                                backgroundColor = backgroundColor
                            )

                            qrCode?.let {
                                binding.imageView.setImageBitmap(it)
                            }

                            binding.linealImgQr.isVisible = true
                            binding.popup.isVisible=true
                            binding.nombreTrabajador.isVisible = true
                            binding.generarQR.isVisible = false
                            binding.linealCompartirDescargar.isVisible = true
                            binding.textoOculto.isVisible = false
                            binding.textoGeinz.isVisible = true
                            binding.loading.isVisible = false
                        }

                        binding.descargarQR.setOnClickListener {
                            val rootView = this.window.decorView.rootView
                            takeScreenshot(rootView)
                            saveToGallery()

                        }

                        binding.compartir.setOnClickListener {
                            binding.compartir.setOnClickListener {
                                createAndShareDynamicLink(idTrabajdor)
                            }
                        }
                    }

                }

                else -> {
                    db.get().addOnSuccessListener { res ->
                        if (res.exists()) {
                            val data = res.data
                            val nombre = data?.get(Variables.nombre) as? String
                            val apellido = data?.get(Variables.apellido) as? String
                            binding.nombreTrabajador.text = "${nombre} ${apellido}"

                            // Generar QR después de obtener los datos
                            val qrColor = ContextCompat.getColor(this, R.color.violetaQR)
                            val backgroundColor = ContextCompat.getColor(this, R.color.white)

                            val qrCode = generateQRCode(
                                text = firebaseAuth.uid.toString(),
                                size = 900,
                                qrColor = qrColor,
                                backgroundColor = backgroundColor
                            )

                            qrCode?.let {
                                binding.imageView.setImageBitmap(it)
                            }

                            // Mostrar y ocultar elementos de UI
                            binding.linealImgQr.isVisible = true
                            binding.popup.isVisible=true
                            binding.nombreTrabajador.isVisible = true
                            binding.generarQR.isVisible = false
                            binding.linealCompartirDescargar.isVisible = true
                            binding.textoOculto.isVisible = false
                            binding.textoGeinz.isVisible = true
                            binding.loading.isVisible = false
                        }

                        binding.descargarQR.setOnClickListener {
                            val rootView = this.window.decorView.rootView
                            takeScreenshot(rootView)
                            saveToGallery()

                        }

                        binding.compartir.setOnClickListener {
                            binding.compartir.setOnClickListener {
                                createAndShareDynamicLink(idTrabajdor)
                            }
                        }
                    }
                }
            }
            binding.sinRegistro.isVisible = false
        } else {
            binding.sinRegistro.isVisible = true
            binding.linealLayoutPrincipal.isVisible = false
            binding.loading.isVisible = false
            binding.iniciarSeccion.setOnClickListener {
                val vista = Intent(this, Login::class.java).apply {
                    putExtra("dato", "qr")
                }
                startActivity(vista)
            }
        }
        binding.popup.setOnClickListener {
            popup()
        }


    }

    override fun onResume() {
        super.onResume()
        if (firebaseAuth.currentUser != null) {
            binding.sinRegistro.isVisible = false
            binding.linealLayoutPrincipal.isVisible = true
            Toast.makeText(this, "entramos a la actividad", Toast.LENGTH_SHORT).show()
            val info = intent.getStringExtra(Variables.info).toString()
            val idTrabajdor = intent.getStringExtra(Variables.idTrabajdor).toString()
            val db = FirebaseFirestore.getInstance()
                .collection(Variables.trabajadores_usuariosDB)
                .document(Variables.trabajadoresDB)
                .collection(Variables.trabajadoresDB)
                .document(firebaseAuth.uid.toString())

            val db2 = FirebaseFirestore.getInstance()
                .collection(Variables.trabajadores_usuariosDB)
                .document(Variables.trabajadoresDB)
                .collection(Variables.trabajadoresDB)
                .document(idTrabajdor)
            when (info) {

                Variables.info -> {
                    db2.get().addOnSuccessListener { res ->
                        if (res.exists()) {
                            val data = res.data
                            val nombre = data?.get(Variables.nombre) as? String
                            val apellido = data?.get(Variables.apellido) as? String
                            binding.nombreTrabajador.text = "${nombre} ${apellido}"

                            // Generar QR después de obtener los datos
                            val qrColor = ContextCompat.getColor(this, R.color.violetaQR)
                            val backgroundColor = ContextCompat.getColor(this, R.color.white)

                            val qrCode = generateQRCode(
                                text = idTrabajdor,
                                size = 900,
                                qrColor = qrColor,
                                backgroundColor = backgroundColor
                            )

                            qrCode?.let {
                                binding.imageView.setImageBitmap(it)
                            }

                            binding.linealImgQr.isVisible = true
                            binding.popup.isVisible=true
                            binding.nombreTrabajador.isVisible = true
                            binding.generarQR.isVisible = false
                            binding.linealCompartirDescargar.isVisible = true
                            binding.textoOculto.isVisible = false
                            binding.textoGeinz.isVisible = true
                            binding.loading.isVisible = false
                        }

                        binding.descargarQR.setOnClickListener {
                            val rootView = this.window.decorView.rootView
                            takeScreenshot(rootView)
                            saveToGallery()

                        }

                        binding.compartir.setOnClickListener {
                            binding.compartir.setOnClickListener {
                                createAndShareDynamicLink(idTrabajdor)
                            }
                        }
                    }

                }

                else -> {
                    db.get().addOnSuccessListener { res ->
                        if (res.exists()) {
                            val data = res.data
                            val nombre = data?.get(Variables.nombre) as? String
                            val apellido = data?.get(Variables.apellido) as? String
                            binding.nombreTrabajador.text = "${nombre} ${apellido}"

                            // Generar QR después de obtener los datos
                            val qrColor = ContextCompat.getColor(this, R.color.violetaQR)
                            val backgroundColor = ContextCompat.getColor(this, R.color.white)

                            val qrCode = generateQRCode(
                                text = firebaseAuth.uid.toString(),
                                size = 900,
                                qrColor = qrColor,
                                backgroundColor = backgroundColor
                            )

                            qrCode?.let {
                                binding.imageView.setImageBitmap(it)
                            }

                            // Mostrar y ocultar elementos de UI
                            binding.linealImgQr.isVisible = true
                            binding.popup.isVisible=true
                            binding.nombreTrabajador.isVisible = true
                            binding.generarQR.isVisible = false
                            binding.linealCompartirDescargar.isVisible = true
                            binding.textoOculto.isVisible = false
                            binding.textoGeinz.isVisible = true
                            binding.loading.isVisible = false
                        }

                        binding.descargarQR.setOnClickListener {
                            val rootView = this.window.decorView.rootView
                            takeScreenshot(rootView)
                            saveToGallery()

                        }

                        binding.compartir.setOnClickListener {
                            binding.compartir.setOnClickListener {
                                createAndShareDynamicLink(idTrabajdor)
                            }
                        }
                    }
                }
            }
        } else {
            binding.sinRegistro.isVisible = true
            binding.linealLayoutPrincipal.isVisible = false
            binding.loading.isVisible = false
            binding.iniciarSeccion.setOnClickListener {
                val vista = Intent(this, Login::class.java).apply {
                    putExtra("dato", "qr")
                }
                startActivity(vista)
            }
        }
    }

    private fun popup() {
        val popup = PopupMenu(this, binding.popup)
        popup.menu.add(Menu.NONE, 1, 1, "Crear acceso directo")
        popup.show()
        popup.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                1 -> {
                    PinShortcut_general.qr_trabajador_accesoDirecto_panel(this)
                    true
                }

                else -> true
            }
        }
    }

    @SuppressLint("StringFormatInvalid")
    private fun createAndShareDynamicLink(
        idTrabajador: String,
    ) {
        val userCollections =
            FirebaseFirestore.getInstance().collection(Variables.trabajadores_usuariosDB)
                .document(Variables.trabajadoresDB).collection(Variables.trabajadoresDB)
                .document(idTrabajador)

        userCollections.get().addOnSuccessListener { res ->
            if (res.exists()) {
                val data = res.data
                val imgAnuncio = data?.get(Variables.imagenPerfil) as? String ?: ""
                val nombre = data?.get(Variables.nombre) as? String ?: ""
                val categoria = data?.get(Variables.categoriaTrabajo) as? String ?: ""

                if (imgAnuncio.isNotEmpty()) {
                    Firebase.dynamicLinks.shortLinkAsync {
                        link =
                            Uri.parse("https://geinzapp.page.link/?idTrabajadorGeinz=${idTrabajador}")
                        domainUriPrefix = "https://geinzapp.page.link"
                        androidParameters("com.geinzz.geinzwork") {
                            minimumVersion = 125
                        }
                        iosParameters("com.geinzz.ios") {
                            appStoreId = "123456789"
                            minimumVersion = "1.0.1"
                        }
                        googleAnalyticsParameters {
                            source = "orkut"
                            medium = "social"
                            campaign = "geinzz-promo"
                        }
                        itunesConnectAnalyticsParameters {
                            providerToken = "123456"
                            campaignToken = "geinzz-promo"
                        }
                        socialMetaTagParameters {
                            title = "!Mira este trabajad@r de Geinz Work $nombre"
                            description = "Categoria del trabajad@r : $categoria"
                            imageUrl = Uri.parse(imgAnuncio)
                        }
                    }.addOnSuccessListener { shortDynamicLink ->
                        val shortLink = shortDynamicLink.shortLink
                        val invitationLink = shortLink.toString()

                        val sendIntent: Intent = Intent().apply {
                            action = Intent.ACTION_SEND
                            putExtra(Intent.EXTRA_TEXT, invitationLink)
                            type = "text/plain"
                        }
                        startActivity(Intent.createChooser(sendIntent, null))
                    }.addOnFailureListener {
                        println("Hubo un error con los links dinámicos: $it")
                    }
                } else {
                    println("La URL de la imagen está vacía.")
                }
            } else {
                println("El anuncio no existe.")
            }
        }.addOnFailureListener { exception ->
            println("Error al obtener el anuncio: ${exception.message}")
        }
    }


    private fun uploadQRCodeToFirebase(imageView: ImageView) {
        // Capturar la imagen del ImageView
        imageView.isDrawingCacheEnabled = true
        imageView.buildDrawingCache()
        val bitmap = Bitmap.createBitmap(imageView.drawingCache)
        imageView.isDrawingCacheEnabled = false

        // Convertir el bitmap a un archivo
        val file = File(cacheDir, "qr_code.png")
        val fileOutputStream = FileOutputStream(file)
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, fileOutputStream)
        fileOutputStream.flush()
        fileOutputStream.close()

        // Subir el archivo a Firebase Storage en la ruta especificada
        val storageReference = FirebaseStorage.getInstance().getReference(Variables.usuarios_db)
            .child(firebaseAuth.uid.toString()).child("QR_trabajador.png")
        val uri = Uri.fromFile(file)

        storageReference.putFile(uri)
            .addOnSuccessListener { taskSnapshot ->
                // Obtener la URL de descarga
                taskSnapshot.storage.downloadUrl.addOnSuccessListener { downloadUrl ->
                    Log.d("FirebaseStorage", "URL de descarga: $downloadUrl")
                    val db = FirebaseFirestore.getInstance()
                        .collection(Variables.trabajadores_usuariosDB)
                        .document(Variables.trabajadoresDB)
                        .collection(Variables.trabajadoresDB)
                        .document(firebaseAuth.uid.toString())
                    val hasmap = hashMapOf<String, Any>(
                        "QR_trabajador" to downloadUrl.toString()
                    )
                    db.set(hasmap, SetOptions.merge()).addOnSuccessListener {
                        Toast.makeText(
                            this,
                            "QR subido cargado exitosamente ",
                            Toast.LENGTH_SHORT
                        ).show()
                    }.addOnFailureListener { e ->
                        Toast.makeText(
                            this,
                            "Error al subir QR a Firestore: ${e.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    Toast.makeText(this, "QR subido cargado exitosamente", Toast.LENGTH_SHORT)
                        .show()
                }
            }
            .addOnFailureListener { exception ->
                Log.e("FirebaseStorage", "Error al subir el QR", exception)
                Toast.makeText(this, "Error al subir el QR", Toast.LENGTH_SHORT).show()
            }
    }


    private fun generateQRCode(
        text: String,
        size: Int,
        qrColor: Int,
        backgroundColor: Int
    ): Bitmap? {
        return try {
            val bitMatrix = MultiFormatWriter().encode(text, BarcodeFormat.QR_CODE, size, size)
            val pixels = IntArray(size * size)

            for (y in 0 until size) {
                for (x in 0 until size) {
                    val color = if (bitMatrix[x, y]) qrColor else backgroundColor
                    pixels[y * size + x] = color
                }
            }

            Bitmap.createBitmap(pixels, size, size, Bitmap.Config.ARGB_8888)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }


    private fun takeScreenshot(view: View): Bitmap {
        val bitmap = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        view.draw(canvas)
        return bitmap
    }

    private fun saveToGallery() {
        // Obtener la vista raíz de la actividad o fragmento
        val rootView = window.decorView.rootView  // Ejemplo para una actividad

        // Tomar la captura de pantalla
        val screenshot = takeScreenshot(rootView)

        // Guardar la captura de pantalla en la galería
        val uri = saveImageToGallery(screenshot)

        // Notificar al usuario
        Toast.makeText(this, "QR guardado correctamente", Toast.LENGTH_LONG).show()
    }

    private fun saveImageToGallery(bitmap: Bitmap): Uri? {
        // Configurar los metadatos de la imagen
        val fileName = "screenshot_${System.currentTimeMillis()}.jpg"
        val contentValues = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, fileName)
            put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
            put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES)
        }

        // Insertar la imagen en MediaStore
        val resolver = applicationContext.contentResolver
        val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)

        // Escribir el bitmap en el OutputStream asociado con la URI
        try {
            uri?.let { uri ->
                resolver.openOutputStream(uri)?.use { outputStream ->
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
                }
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }

        return uri
    }


}