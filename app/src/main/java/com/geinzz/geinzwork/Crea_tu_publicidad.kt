package com.geinzz.geinzwork

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.viewbinding.ViewBinding
import com.geinzz.geinzwork.databinding.ActivityCreaTuPublicidadBinding
import com.geinzz.geinzwork.databinding.ItemCustomFixedSizeLayout2Binding
import com.geinzz.geinzwork.servicios_geinz.planes_noticias_servicios_geinz
import com.geinzz.geinzwork.vistasPubliciadesGeinz.crear_cuenta_Tienda
import org.imaginativeworld.whynotimagecarousel.listener.CarouselListener
import org.imaginativeworld.whynotimagecarousel.model.CarouselItem
import org.imaginativeworld.whynotimagecarousel.utils.setImage

class Crea_tu_publicidad : AppCompatActivity() {
    private lateinit var binding:ActivityCreaTuPublicidadBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding=ActivityCreaTuPublicidadBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        binding.verPlanes.setOnClickListener {
            startActivity(Intent(this,planes::class.java))
        }

        binding.carruselPublicidadBaner.registerLifecycle(lifecycle)

        val listaUrls = listOf(
            "https://firebasestorage.googleapis.com/v0/b/geinzwork.appspot.com/o/IMG_CategoriasGeneral%2Fservicios_geinz_img%2FCrea-tu-Publicidad-en-Geinz-wokr.webp?alt=media&token=e4a094dd-07b3-41c2-9d87-aa97eb5812c0",
            "https://firebasestorage.googleapis.com/v0/b/geinzwork.appspot.com/o/IMG_CategoriasGeneral%2Fservicios_geinz_img%2FCrea-tu-Publicidad-en-Geinz-wokr.webp?alt=media&token=e4a094dd-07b3-41c2-9d87-aa97eb5812c0",
            "https://firebasestorage.googleapis.com/v0/b/geinzwork.appspot.com/o/IMG_CategoriasGeneral%2Fservicios_geinz_img%2FCrea-tu-Publicidad-en-Geinz-wokr.webp?alt=media&token=e4a094dd-07b3-41c2-9d87-aa97eb5812c0"
        )

        val listaCarouselItems = listaUrls.map { url ->
            CarouselItem(imageUrl = url)
        }

        binding.carruselPublicidadBaner.carouselListener = object : CarouselListener {
            override fun onCreateViewHolder(
                layoutInflater: LayoutInflater,
                parent: ViewGroup,
            ): ViewBinding? {
                return ItemCustomFixedSizeLayout2Binding.inflate(
                    layoutInflater,
                    parent,
                    false
                )
            }

            override fun onBindViewHolder(
                binding: ViewBinding,
                item: CarouselItem,
                position: Int,
            ) {
                val currentBinding = binding as ItemCustomFixedSizeLayout2Binding
                currentBinding.imageView.apply {
                    scaleType = ImageView.ScaleType.CENTER_CROP
                    setImage(item, R.drawable.ic_wb_cloudy_with_padding)
                }
            }
        }

        binding.carruselPublicidadBaner.setData(listaCarouselItems)
    }
}