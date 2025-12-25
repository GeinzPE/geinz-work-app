package com.geinzz.geinzwork.utils.constantes.localizate_geinz

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import androidx.activity.result.ActivityResultLauncher
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.yalantis.ucrop.UCrop
import java.io.File

object constantes_carga_ucrop_img {
    var croppedUri by mutableStateOf<Uri?>(null)

    fun launchCrop(
        context: Context,
        sourceUri: Uri,
        launcher: ActivityResultLauncher<Intent>
    ) {
        val destinationUri = Uri.fromFile(
            File(context.cacheDir, "crop_${System.currentTimeMillis()}.jpg")
        )

        val options = UCrop.Options().apply {

            setToolbarTitle("Editar imagen")
            setToolbarColor(Color.BLACK)
            setStatusBarColor(Color.BLACK)
            setToolbarWidgetColor(Color.WHITE)

            setActiveControlsWidgetColor(Color.WHITE)
            setDimmedLayerColor(Color.parseColor("#66000000"))

            setCropGridColor(Color.WHITE)
            setCropFrameColor(Color.WHITE)

            setFreeStyleCropEnabled(true)
            setHideBottomControls(true) // 👈 CLAVE

            setCompressionQuality(85)
            withMaxResultSize(1080, 1080)
        }


        val intent = UCrop.of(sourceUri, destinationUri)
            .withOptions(options)
            .withMaxResultSize(1080, 1080)
            .getIntent(context)

        launcher.launch(intent)
    }

}