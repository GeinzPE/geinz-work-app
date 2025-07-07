package com.example.geinzwork.fragmentos.img_completa

import android.content.Context
import android.content.Intent
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
}