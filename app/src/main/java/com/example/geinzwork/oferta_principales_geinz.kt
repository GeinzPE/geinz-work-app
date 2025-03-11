package com.example.geinzwork

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.Menu
import android.widget.PopupMenu
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.core.view.iterator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.example.geinzwork.adapterViewholder.adapter_promociones_principales_geinz
import com.example.geinzwork.constantesGeneral.Variables
import com.example.geinzwork.dataclass.dataclass_adapter_promociones
import com.geinzz.geinzwork.Crea_tu_publicidad
import com.geinzz.geinzwork.R
import com.geinzz.geinzwork.databinding.ActivityOfertaPrincipalesGeinzBinding
import com.geinzz.geinzwork.databinding.ItemCustomFixedSizeLayout3Binding
import com.geinzz.geinzwork.dataclass.dataclass_mas_art_promo_servicio
import com.geinzz.geinzwork.vistaTiendas.VistaTienda
import com.geinzz.geinzwork.vistaTiendas.vistaProductosGeneralTiendas
import com.google.firebase.Firebase
import com.google.firebase.dynamiclinks.androidParameters
import com.google.firebase.dynamiclinks.dynamicLinks
import com.google.firebase.dynamiclinks.googleAnalyticsParameters
import com.google.firebase.dynamiclinks.iosParameters
import com.google.firebase.dynamiclinks.itunesConnectAnalyticsParameters
import com.google.firebase.dynamiclinks.shortLinkAsync
import com.google.firebase.dynamiclinks.socialMetaTagParameters
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import org.imaginativeworld.whynotimagecarousel.ImageCarousel
import org.imaginativeworld.whynotimagecarousel.listener.CarouselOnScrollListener
import org.imaginativeworld.whynotimagecarousel.model.CarouselItem

class oferta_principales_geinz : AppCompatActivity() {
    private lateinit var binding: ActivityOfertaPrincipalesGeinzBinding
    private val listaArticulos = mutableListOf<dataclass_mas_art_promo_servicio>()
    private val listaMas_promo = mutableListOf<dataclass_adapter_promociones>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOfertaPrincipalesGeinzBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }


        val idAnuncio = intent.getStringExtra(Variables.idPublicidad).toString()
        binding.bakPresert.setOnClickListener { onBackPressed() }
        binding.popup.setOnClickListener { popup(idAnuncio) }

        cargarDatos()

        obtenerImgPrincipales()
    }

    private fun popup(idAnuncio: String) {
        val popup = PopupMenu(this, binding.popup)
        popup.menu.add(Menu.NONE, 1, 1, "Compartir")
        popup.menu.add(Menu.NONE, 2, 2, "Crea tu oferta con Geinz")
        popup.show()
        popup.setOnMenuItemClickListener { item ->
            val itemID = item.itemId
            if (itemID == 1) {
                createAndShareDynamicLink(idAnuncio, this)
            } else if (itemID == 2) {
                startActivity(Intent(this, Crea_tu_publicidad::class.java))
            }
            return@setOnMenuItemClickListener true
        }
    }

    @SuppressLint("StringFormatInvalid")
    fun createAndShareDynamicLink(
        idAnuncio: String,
        context: Context,
    ) {
        val firestore = FirebaseFirestore.getInstance()
            .collection(Variables.anuncios)
            .document(Variables.anunciosprimarios_cortos)
            .collection(Variables.anuncios)
            .document(idAnuncio)

        firestore.get().addOnSuccessListener { res ->
            if (res.exists()) {
                val data = res.data
                val imgAnuncio = data?.get(Variables.URLimg) as? String ?: ""
                val tipo = data?.get(Variables.tipo) as? String ?: ""

                if (imgAnuncio.isNotEmpty()) {
                    Firebase.dynamicLinks.shortLinkAsync {
                        link =
                            Uri.parse("https://geinzapp.page.link/?idAnuncioPrimarioGeinz=${idAnuncio}")
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
                            when (tipo) {
                                "oferta" -> {
                                    val mensaje = getString(
                                        R.string.oferta,
                                        binding.includeLayour.nombreTienda.text.toString()
                                    )
                                    title = "¡No te pierdas esta oferta exclusiva! 🎉"
                                    description = mensaje
                                    imageUrl = Uri.parse(imgAnuncio)
                                }

                                "atencion" -> {
                                    val mensaje = getString(
                                        R.string.atencion,
                                        binding.includeLayour.nombreTienda.text.toString()
                                    )
                                    title = "✨ ¡Echa un vistazo!"
                                    description = mensaje
                                    imageUrl = Uri.parse(imgAnuncio)
                                }

                                "convenio" -> {
                                    val mensaje = getString(
                                        R.string.convenio,
                                        binding.includeLayour.nombreTienda.text.toString()
                                    )
                                    title = "\uD83C\uDF1F ¡Grandes noticias! \uD83C\uDF1F"
                                    description = mensaje
                                    imageUrl = Uri.parse(imgAnuncio)
                                }

                                else -> {
                                    title = "¡Mira esta promoción de Geinz!"
                                    description = "Aprovecha ofertas y descuentos."
                                    imageUrl = Uri.parse(imgAnuncio)
                                }
                            }
                        }
                    }.addOnSuccessListener { shortDynamicLink ->
                        val shortLink = shortDynamicLink.shortLink
                        val invitationLink = shortLink.toString()

                        val sendIntent: Intent = Intent().apply {
                            action = Intent.ACTION_SEND
                            putExtra(Intent.EXTRA_TEXT, invitationLink)
                            type = "text/plain"
                        }
                        context.startActivity(Intent.createChooser(sendIntent, null))
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

    private fun cargarDatos() {
          binding.loading.isVisible = true
        binding.scrollPrincipal.isVisible = false

        CoroutineScope(Dispatchers.Main).launch {
            try {
                val idAnuncio = intent.getStringExtra(Variables.idPublicidad).toString()


                val idTienda = obtenerDatosPublicidad()
                obtenerDatosTienda(idTienda)

                // Listener para redirigir a la vista de tienda después de cargar los datos
                binding.includeLayour.listenerTienda.setOnClickListener {
                    val vista =
                        Intent(this@oferta_principales_geinz, VistaTienda::class.java).apply {
                            putExtra("idTienda", idTienda)
                        }
                    startActivity(vista)
                }

                obtenr_masPromociones(idAnuncio)

                binding.loading.isVisible = false
                binding.scrollPrincipal.isVisible = true

            } catch (e: Exception) {
                Log.e("Error", "Error al cargar los datos: $e")
            }
        }
    }


    private suspend fun obtenerDatosTienda(idTienda: String) {
        val db = FirebaseFirestore.getInstance().collection(Variables.collection_Tiendas).document(idTienda)
        db.get().addOnSuccessListener { res ->
            if (res.exists()) {
                val data = res.data
                val imgTienda = data?.get(Variables.imgPerfil) as? String ?: ""
                val nombreTienda = data?.get(Variables.nombre) as? String ?: ""
                val localidad = data?.get(Variables.ubicacion) as? String ?: ""
                val tipoTeinda = data?.get(Variables.tipoTienda) as? String ?: ""
                val estrella = data?.get(Variables.estrellas) as? String ?: ""
                val imagenTeindaPerfil = data?.get(Variables.imgPerfil) as? String ?: ""
                binding.includeLayour.nombreTienda.text = nombreTienda
                binding.includeLayour.estrellas.text = estrella
                binding.includeLayour.localidad.text = localidad
                try {
                    Glide.with(this@oferta_principales_geinz)
                        .load(imgTienda)
                        .into(binding.includeLayour.containerImgTienda)
                    Glide.with(this@oferta_principales_geinz)
                        .load(imagenTeindaPerfil)
                        .into(binding.includeLayour.perfilFotoTienda)
                } catch (e: Exception) {
                    println("Error al cargar la imagen: $e")
                }
            }
        }
    }

    private suspend fun obtenerDatosPublicidad(): String {
        val idAnuncio = intent.getStringExtra(Variables.idPublicidad).toString()
        val db = FirebaseFirestore.getInstance().collection(Variables.anuncios)
            .document(Variables.anunciosprimarios_cortos).collection(Variables.anuncios).document(idAnuncio)

        val res = db.get().await() // Esperar la respuesta de manera suspendida

        return if (res.exists()) {
            val data = res.data
            val URLimg = data?.get(Variables.URLimg) as? String ?: ""
            val descripcion = data?.get(Variables.descripcion) as? String ?: ""
            val idTienda = res.getString(Variables.idTienda) ?: ""
            val inicio = res.getString(Variables.inicio) ?: ""
            val titulo = data?.get(Variables.titulo) as? String ?: ""
            val vence = res.getString(Variables.vence) ?: ""

            val vistaPRoductos = vistaProductosGeneralTiendas()
            vistaPRoductos.obtenerMasServicios(
                idTienda,
                binding.recicleProductosPrincipales,
                listaArticulos,
                "finish"
            )

            binding.vencimineto.text = vence
            binding.fechaPublicada.text = inicio

            try {
                Glide.with(this@oferta_principales_geinz)
                    .load(URLimg)
                    .into(binding.imgContainer)
            } catch (e: Exception) {
                println("Error al cargar la imagen: $e")
            }

            binding.tituloContenido.text = titulo
            binding.descripcionContenido.text = descripcion

            idTienda // Devuelve el idTienda

        } else {
            Log.e("error_obtenerAnuncios", "El documento no existe")
            ""
        }
    }

    private fun obtenerImgPrincipales() {
        val db = FirebaseFirestore.getInstance().collection(Variables.anuncios)
            .document(Variables.anunciosprimarios_cortos).collection(Variables.anuncios)
        val lista = mutableListOf<CarouselItem>()

        db.get().addOnSuccessListener { res ->
            for (anuncios in res.documents) {
                val AnunciosData = anuncios.data
                val urlimg = AnunciosData?.get(Variables.URLimg) as? String

                if (urlimg != null) {
                    lista.add(CarouselItem(urlimg))
                }
            }

            if (lista.isNotEmpty()) {
                // Ajustar dinámicamente la altura del carrusel según la primera imagen
                val firstImageUrl = lista[0].imageUrl
                if (firstImageUrl != null) {
                    Glide.with(binding.root.context)
                        .asBitmap()
                        .load(firstImageUrl)
                        .into(object : CustomTarget<Bitmap>() {
                            override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                                val layoutParams = binding.carrucelImgContainer.layoutParams
                                layoutParams.height = resource.height
                                binding.carrucelImgContainer.layoutParams = layoutParams
                            }

                            override fun onLoadCleared(placeholder: Drawable?) {}
                        })
                }

                // Inicializar el TextView con el formato "1 / total"
                binding.cantidadImg.text = "1/${lista.size}"

                // Agregar las imágenes al carrusel
                binding.carrucelImgContainer.addData(lista)

                // Actualizar el contador de imágenes cuando cambie la imagen del carrusel
                binding.carrucelImgContainer.onScrollListener = object : CarouselOnScrollListener {
                    override fun onScrollStateChanged(
                        recyclerView: RecyclerView,
                        newState: Int,
                        position: Int,
                        carouselItem: CarouselItem?
                    ) {
                        if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                            // Actualizar el contador de imágenes "actual/total"
                            val currentIndex = position + 1 // Para que el conteo empiece desde 1
                            binding.cantidadImg.text = "$currentIndex/${lista.size}"
                        }
                    }
                }
            }
        }
    }








    private suspend fun obtenr_masPromociones(idAnuncio: String) {
        val db = FirebaseFirestore.getInstance().collection(Variables.anuncios)
            .document(Variables.anunciosprimarios_cortos).collection(Variables.anuncios)
        db.get().addOnSuccessListener { res ->
            for (datos in res) {
                val data = datos.data
                val img = data?.get(Variables.URLimg) as? String ?: ""
                val titulo = data?.get(Variables.titulo) as? String ?: ""
                val texto = data?.get(Variables.descripcion) as? String ?: ""
                val id = data.get(Variables.id) as? String ?: ""
                val fecha = data?.get("fecha_rec") as? String ?: ""
                val hora = data?.get("hora_rec") as? String ?: ""
                if (idAnuncio != id) {
                    val dataClass = dataclass_adapter_promociones(img, titulo, texto, id,fecha,hora)
                    listaMas_promo.add(dataClass)
                }
            }
            if (listaMas_promo.isNotEmpty()) {
                initRecycleView()
            } else {
                Log.d("error obtenerDAtos", "No hay datos para mostrar")
            }
        }.addOnFailureListener { e ->
            Log.d("error obtenerDAtos", "error al obtener los datos $e")
        }
    }

    private fun initRecycleView() {
        val recicle = binding.reciclePromocionesGeinz
        recicle.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        recicle.adapter = adapter_promociones_principales_geinz(
            listaMas_promo
        )
    }
}