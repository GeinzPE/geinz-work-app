package com.example.geinzwork.herramientas_geinz

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.bumptech.glide.Glide
import com.example.geinzwork.dataclass.HerramientaGeinz
import com.example.geinzwork.vistaTrabajador.ver_promociones
import com.geinzz.geinzwork.R
import com.geinzz.geinzwork.constantesGeneral.constantesPublicidad.obtenerCaptino
import com.geinzz.geinzwork.databinding.ActivityHerramientasGeinzBinding
import com.geinzz.geinzwork.databinding.ItemCustomFixedHerrameintasGeinzBinding
import com.geinzz.geinzwork.databinding.ItemCustomFixedSizeLayout3Binding
import com.google.firebase.firestore.FirebaseFirestore
import org.imaginativeworld.whynotimagecarousel.ImageCarousel
import org.imaginativeworld.whynotimagecarousel.listener.CarouselListener
import org.imaginativeworld.whynotimagecarousel.listener.CarouselOnScrollListener
import org.imaginativeworld.whynotimagecarousel.model.CarouselItem

class herramientas_geinz : AppCompatActivity() {
    private lateinit var binding: ActivityHerramientasGeinzBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHerramientasGeinzBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        obtenerHerramientasGeinzLocales(
            binding.carruse2,
            this,
            binding.textViewID
        )
    }

    fun obtenerHerramientasGeinzLocales(
        carrucel: ImageCarousel,
        contexto: Context,
        textViewID: TextView // <- ID se mostrará aquí
    ) {
        // 1. Lista local de herramientas
        val herramientas = listOf(
            HerramientaGeinz(
                "herramienta_2",
                R.drawable.herramienta_tecnicos,
                "Reservas inteligentes",
                "Herrameinta para tecnicos",
                "Accede a información detallada sobre compatibilidades de pantallas, zócalos, carcasas, micas y otros componentes esenciales para técnicos de celulares."

            ),
            HerramientaGeinz(
                "herramienta_3",
                R.drawable.ofertas_geinz,
                "Seguimiento de pedidos", titulo = "Exploración de ofertas cercanas",
                descripcion = "Descubre ofertas publicadas por personas o tiendas cercanas a tu ubicación. Encuentra promociones relevantes en tu localidad directamente desde Geinz Work."
            )
            // Agrega más herramientas aquí...
        )

        // 2. Crear lista para el carrusel
        val listaCarousel = herramientas.map {
            CarouselItem(imageDrawable = it.imagenRes, caption = it.caption)
        }

        carrucel.setData(listaCarousel)

        // 3. Personalización del ViewHolder del carrusel
        carrucel.carouselListener = object : CarouselListener {
            override fun onCreateViewHolder(
                layoutInflater: LayoutInflater,
                parent: ViewGroup,
            ): ViewBinding {
                return ItemCustomFixedHerrameintasGeinzBinding.inflate(
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
                val currentBinding = binding as ItemCustomFixedHerrameintasGeinzBinding

                currentBinding.imageView.apply {
                    scaleType = ImageView.ScaleType.CENTER_CROP
                    Glide.with(contexto)
                        .load(item.imageDrawable)
                        .placeholder(R.drawable.cargando_img)
                        .into(this)
                }

                // Evento de clic
                currentBinding.root.setOnClickListener {
                    val herramientaSeleccionada = herramientas.getOrNull(position)
                    herramientaSeleccionada?.let {
                        abrirActividadSegunID(contexto, it.id)
                    }
                }
            }

        }

        // 4. Mostrar ID en TextView cuando cambia el carrusel
        carrucel.onScrollListener = object : CarouselOnScrollListener {
            override fun onScrollStateChanged(
                recyclerView: RecyclerView,
                newState: Int,
                position: Int,
                carouselItem: CarouselItem?,
            ) {
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    val herramientaActual = herramientas.getOrNull(position)
                    herramientaActual?.let {
                        textViewID.text = "ID: ${it.id}"
                        binding.ituloCarrucel.text = "${it.titulo}"
                        binding.descripcionHerramienta.text = "${it.descripcion}"
                    }
                }
            }
        }
    }

    fun abrirActividadSegunID(context: Context, id: String) {
        when (id) {

            "herramienta_2" -> context.startActivity(
                Intent(
                    context,
                    inicio_geinz_work::class.java
                )
            )

            "herramienta_3" -> context.startActivity(
                Intent(
                    context,
                    ver_promociones::class.java
                )
            )
            // Agrega más casos según tus herramientas
            else -> Toast.makeText(context, "Herramienta no disponible", Toast.LENGTH_SHORT).show()
        }
    }

}