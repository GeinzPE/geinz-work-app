package com.example.geinzwork.fragmentos.img_completa

import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.result.ActivityResultLauncher
import androidx.core.content.ContextCompat
import com.geinzz.geinzwork.R
import com.yalantis.ucrop.UCrop
import java.io.File

object recortador_img {
    fun iniciarRecorte(cropImageLauncher:ActivityResultLauncher<Intent>,contex: Context,uri: Uri, horizontal: Boolean = false) {
        val destinationUri = Uri.fromFile(File(contex.cacheDir, "recortado_${System.currentTimeMillis()}.jpg"))

        val options = UCrop.Options().apply {
            setCompressionQuality(90)
            setFreeStyleCropEnabled(true) // libre si no es horizontal fijo
            setHideBottomControls(false) // mostrar controles como el botón ✓
            setToolbarTitle("Recorta tu imagen")
            setStatusBarColor(ContextCompat.getColor(contex, R.color.white))
            setToolbarColor(ContextCompat.getColor(contex, R.color.white))
            setActiveControlsWidgetColor(ContextCompat.getColor(contex, R.color.teal_700))
        }

        val cropIntent = UCrop.of(uri, destinationUri)
            .apply {
                if (horizontal) {
                    withAspectRatio(16f, 9f) // modo portada
                    withMaxResultSize(1280, 720) // HD horizontal

                } else {
                    withAspectRatio(1f, 1f) // cuadrado
                    withMaxResultSize(1080, 1080)
                }
            }
            .withOptions(options)
            .getIntent(contex)

        cropImageLauncher.launch(cropIntent)
    }


    fun iniciarRecorte_vertical_horizontal(cropImageLauncher: ActivityResultLauncher<Intent>, contex: Context, uri: Uri) {
        val destinationUri = Uri.fromFile(File(contex.cacheDir, "recortado_${System.currentTimeMillis()}.jpg"))

        val options = UCrop.Options().apply {
            setCompressionQuality(90)
            setFreeStyleCropEnabled(false) // Deshabilitar el recorte libre para forzar el aspecto
            setHideBottomControls(false) // Mostrar controles como el botón ✓
            setToolbarTitle("Recorta tu imagen")
            setStatusBarColor(ContextCompat.getColor(contex, R.color.white))
            setToolbarColor(ContextCompat.getColor(contex, R.color.white))
            setActiveControlsWidgetColor(ContextCompat.getColor(contex, R.color.teal_700))
        }

        val cropIntentBuilder = UCrop.of(uri, destinationUri)
            .withOptions(options)

        // Determinar la orientación de la imagen original
        val bitmapOptions = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        val inputStream = contex.contentResolver.openInputStream(uri)
        BitmapFactory.decodeStream(inputStream, null, bitmapOptions)
        inputStream?.close()

        val originalWidth = bitmapOptions.outWidth
        val originalHeight = bitmapOptions.outHeight

        // Si la imagen es horizontal (ancho > alto), aplíca 16:9
        if (originalWidth > originalHeight) {
            cropIntentBuilder
                .withAspectRatio(16f, 9f) // Modo portada horizontal
                .withMaxResultSize(1280, 720) // HD horizontal
        } else {
            // Si la imagen es vertical (alto >= ancho), aplica 4:5 (la más común en Instagram)
            // Opcionalmente, podrías usar 3:4 si encuentras que se adapta mejor a tus capturas.
            cropIntentBuilder
                .withAspectRatio(4f, 5f) // Relación de aspecto vertical de Instagram (la más común)
                // .withAspectRatio(3f, 4f) // Alternativa: 3:4, también válida y podría ser mejor para capturas de pantalla
                .withMaxResultSize(1080, 1350) // Tamaño recomendado para 4:5 (1080x1350)
            // .withMaxResultSize(1080, 1440) // Tamaño recomendado para 3:4 (1080x1440)
        }

        cropImageLauncher.launch(cropIntentBuilder.getIntent(contex))
    }
}