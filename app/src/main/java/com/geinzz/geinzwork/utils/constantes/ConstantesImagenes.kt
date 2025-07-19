package com.geinzz.geinzwork.utils.constantes.constantes

import android.content.ContentResolver
import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.Drawable
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.drawable.toBitmap
import com.bumptech.glide.Glide
import com.geinzz.geinzwork.R
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream

object constantesImagenes {

    fun bitmapToUri(bitmap: Bitmap?,contexto:Context): Uri? {
        val contentResolver = contexto.contentResolver
        bitmap ?: return null
        val bytes = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.WEBP, 100, bytes)
        val path = MediaStore.Images.Media.insertImage(contentResolver, bitmap, "Title", null)
        return Uri.parse(path)
    }

    fun reducesize(drawable: Drawable): Bitmap? {
        val originalBitmap = drawable.toBitmap()
        val compressedBitmap = Bitmap.createScaledBitmap(originalBitmap, 1200, 1200, true)

        val outputStream = ByteArrayOutputStream()
        compressedBitmap.compress(Bitmap.CompressFormat.WEBP, 100, outputStream)
        val imageByte = outputStream.toByteArray()
        val compressedWebPBitmap = BitmapFactory.decodeByteArray(imageByte, 0, imageByte.size)

        Log.d(ContentValues.TAG, "Tamaño de la imagen comprimida en WebP: ${imageByte.size} bytes")

        return compressedWebPBitmap
    }

    fun reortnarur(urlImagen: String): String {
        var urlimg = urlImagen
        return urlimg
    }
    suspend fun resizeAndCompressImage(
        contentResolver: ContentResolver,
        imageUri: Uri,
        maxWidth: Int,
        maxHeight: Int
    ): ByteArray {
        return withContext(Dispatchers.IO) {
            val originalBitmap = MediaStore.Images.Media.getBitmap(contentResolver, imageUri)

            // Paso 1: Recortar al centro y hacerlo cuadrado
            val dimension = minOf(originalBitmap.width, originalBitmap.height)
            val xOffset = (originalBitmap.width - dimension) / 2
            val yOffset = (originalBitmap.height - dimension) / 2

            val squareBitmap = Bitmap.createBitmap(originalBitmap, xOffset, yOffset, dimension, dimension)

            // Paso 2: Redimensionar al tamaño deseado (por ejemplo, 500x500)
            val resizedBitmap = Bitmap.createScaledBitmap(squareBitmap, maxWidth, maxHeight, true)

            // Paso 3: Comprimir
            val outputStream = ByteArrayOutputStream()
            resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream)

            outputStream.toByteArray()
        }
    }

    suspend fun procesarImagenPortada(
        contentResolver: ContentResolver,
        imageUri: Uri,
        targetWidth: Int = 1000,
        targetHeight: Int = 600
    ): ByteArray {
        return withContext(Dispatchers.IO) {
            val originalBitmap = MediaStore.Images.Media.getBitmap(contentResolver, imageUri)

            // Paso 1: Obtener proporciones deseadas
            val targetRatio = targetWidth.toFloat() / targetHeight

            val originalRatio = originalBitmap.width.toFloat() / originalBitmap.height

            val cropBitmap: Bitmap = if (originalRatio > targetRatio) {
                // Imagen más ancha que el objetivo, recortar bordes laterales
                val newWidth = (originalBitmap.height * targetRatio).toInt()
                val xOffset = (originalBitmap.width - newWidth) / 2
                Bitmap.createBitmap(originalBitmap, xOffset, 0, newWidth, originalBitmap.height)
            } else {
                // Imagen más alta que el objetivo, recortar parte superior e inferior
                val newHeight = (originalBitmap.width / targetRatio).toInt()
                val yOffset = (originalBitmap.height - newHeight) / 2
                Bitmap.createBitmap(originalBitmap, 0, yOffset, originalBitmap.width, newHeight)
            }

            // Paso 2: Redimensionar a 1000x600 (o lo que definas)
            val resizedBitmap = Bitmap.createScaledBitmap(cropBitmap, targetWidth, targetHeight, true)

            // Paso 3: Comprimir
            val outputStream = ByteArrayOutputStream()
            resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream)

            outputStream.toByteArray()
        }
    }
     fun refereciaStorage(rutaimgstorege: String, imagenComprimida: ByteArray, nombreimg: String, contexto: Context) {
        val ref = FirebaseStorage.getInstance().getReference(rutaimgstorege)
        ref.putBytes(imagenComprimida)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    ref.downloadUrl.addOnSuccessListener { res ->
                        val urlimg = res.toString()
                        Toast.makeText(
                            contexto,
                            "cargando $nombreimg",
                            Toast.LENGTH_SHORT
                        ).show()
                        var urlObtenida = reortnarur(urlimg)
                        println("obtenemos la url $urlObtenida")
                    }.addOnFailureListener { e ->
                        Toast.makeText(
                            contexto,
                            "Error al obtener la URL de la imagen: $e",
                            Toast.LENGTH_SHORT
                        ).show()
                        println("Error al obtener la URL de la imagen: $e")
                    }
                } else {
                    Toast.makeText(
                        contexto,
                        "Error al subir la imagen: ${task.exception}",
                        Toast.LENGTH_SHORT
                    ).show()
                    println("Error al subir la imagen: ${task.exception}")
                }
            }
    }

    fun obtenerURLDescarga(contexto: Context,yapeqr: ImageView, storageReference: String) {
        val storageReferences=FirebaseStorage.getInstance().getReference(storageReference)
        storageReferences.downloadUrl
            .addOnSuccessListener { uri ->
                val downloadUrl = uri.toString()
                try {
                    Glide.with(contexto)
                        .load(downloadUrl).placeholder(R.drawable.cargando_img_geinz_500).into(yapeqr)
                } catch (e: Exception) {
                    println("error al setear la img")
                }
                yapeqr.setOnClickListener {
                    val imageUrl = downloadUrl
                    val dialogFragment = ImageDialogFragmentURL.newInstance(imageUrl)
                    dialogFragment.show(
                        (contexto as AppCompatActivity).supportFragmentManager,
                        "image_dialog"
                    )
                }
            }
            .addOnFailureListener { exception ->
                println("error al obtener la img ")
            }

    }
}