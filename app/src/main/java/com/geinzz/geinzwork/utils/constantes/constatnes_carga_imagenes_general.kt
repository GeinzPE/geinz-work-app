package com.geinzz.geinzwork.utils.constantes.constantes

import android.content.Context
import android.graphics.drawable.Drawable
import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import de.hdodenhof.circleimageview.CircleImageView
import com.bumptech.glide.request.target.Target // Importa Target de Glide

object constatnes_carga_imagenes_general {
    fun changer_img(
        progressBar: ProgressBar,
        context: Context,
        url: String,
        circle_img: CircleImageView? = null,
        imageView: ImageView? = null, // 🔹 Ahora acepta ShapeableImageView y PhotoView
        type: String,
        placeholder: Drawable? = null, // Placeholder opcional
        onImageLoaded: (Boolean) -> Unit // Callback de éxito o fallo
    ) {
        try {
            progressBar.visibility = View.VISIBLE

            val glideRequest = Glide.with(context)
                .load(url)
                .diskCacheStrategy(DiskCacheStrategy.ALL)

            placeholder?.let { glideRequest.placeholder(it) } // Establece el placeholder si no es nulo

            glideRequest.listener(object : RequestListener<Drawable> {
                override fun onResourceReady(
                    resource: Drawable,
                    model: Any,
                    target: Target<Drawable>?,
                    dataSource: DataSource,
                    isFirstResource: Boolean
                ): Boolean {
                    progressBar.visibility = View.GONE
                    onImageLoaded(true) // Notifica que la imagen se cargó correctamente
                    return false
                }

                override fun onLoadFailed(
                    e: GlideException?,
                    model: Any?,
                    target: Target<Drawable>,
                    isFirstResource: Boolean
                ): Boolean {
                    progressBar.visibility = View.GONE
                    onImageLoaded(false) // Notifica que la imagen falló
                    return false
                }
            })
                .apply {
                    when (type) {
                        "perfil" -> circle_img?.let { into(it) }
                        "portada", "zoom" -> imageView?.let { into(it) } // 🔹 Puede ser ShapeableImageView o PhotoView
                    }
                }
        } catch (e: Exception) {
            println("Problema al setear la imagen: $e")
            onImageLoaded(false) // En caso de error, notifica que la imagen no se cargó
        }
    }


}