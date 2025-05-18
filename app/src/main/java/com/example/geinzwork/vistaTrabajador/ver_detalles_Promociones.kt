package com.geinzz.geinzwork.vistaTrabajador

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Paint
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.view.LayoutInflater
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.geinzwork.constantesGeneral.Variables
import com.geinzz.geinzwork.R
import com.geinzz.geinzwork.adapterViewholder.adaptadorAnuncios
import com.geinzz.geinzwork.constantesGeneral.constantes
import com.geinzz.geinzwork.constantesGeneral.constantesCarrito
import com.geinzz.geinzwork.constantesGeneral.constantesImagenes
import com.geinzz.geinzwork.constantesGeneral.constantesNoticias.createAndShareDynamicLink
import com.geinzz.geinzwork.constantesGeneral.constantesPublicidad
import com.geinzz.geinzwork.constantesGeneral.filtradoLocalidadElementos
import com.geinzz.geinzwork.constantesGeneral.mostrarFechaDialog_horaDialog
import com.geinzz.geinzwork.databinding.ActivityVerDetallesPromocionesBinding
import com.geinzz.geinzwork.databinding.BottomSheetInfoPublicidadBinding
import com.geinzz.geinzwork.databinding.BottomSheetReservasBinding
import com.geinzz.geinzwork.dataclass.dataClassAnuncios
import com.geinzz.geinzwork.vistas_anuncios_general
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDragHandleView
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ListResult
import com.google.firebase.storage.StorageReference
import org.imaginativeworld.whynotimagecarousel.model.CarouselItem


class ver_detalles_Promociones : AppCompatActivity() {
    private lateinit var binding: ActivityVerDetallesPromocionesBinding
    private lateinit var bottomSheet: BottomSheetDragHandleView
    private lateinit var dialog: BottomSheetDialog
    private lateinit var bindingBottomShet: BottomSheetReservasBinding
    private val lista = mutableListOf<dataClassAnuncios>()
    private var vistaTimer: CountDownTimer? = null
    private val tiempoParaContarVista: Long = 20000
    private lateinit var zoonIn: Animation
    private lateinit var zoomout: Animation


    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityVerDetallesPromocionesBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        constantes.carga(4000, { mostrarDatos() })
        binding.retroceder.setOnClickListener {
            onBackPressed()
        }
        val textViewPriceBefore = binding.includeLinealPrecios.textViewPriceBefore
        textViewPriceBefore.paintFlags =
            textViewPriceBefore.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
        textViewPriceBefore.textSize = 12f
        zoonIn = AnimationUtils.loadAnimation(this, R.anim.zoom_in)
        zoomout = AnimationUtils.loadAnimation(this, R.anim.zoom_uot)
        val entrada = intent.getStringExtra(Variables.entrada).toString()
        if (entrada.equals(Variables.tiendas)) {
            val idTienda = intent.getStringExtra(Variables.idTienda).toString()
            val idAnuncios = intent.getStringExtra(Variables.idAnuncio).toString()

            binding.reserva.setOnClickListener {
                dialog = BottomSheetDialog(this)
                bottomSheetDialog(
                    Variables.reserva,
                    this,
                    idAnuncios,
                    binding.titulo.text.toString(),
                    Variables.tiendas, idTienda
                )
                dialog.show()
            }
            binding.comprar.setOnClickListener {
                dialog = BottomSheetDialog(this)
                bottomSheetDialog(
                    Variables.compra,
                    this,
                    idAnuncios,
                    binding.titulo.text.toString(),
                    Variables.tiendas, idTienda
                )
                dialog.show()
            }
        } else if (entrada.equals(Variables.tipoNoticia)) {
            val idAnuncio = intent.getStringExtra(Variables.idAnuncio).toString()
            obtenerDatosPromocion(idAnuncio)
            obtenerNoticiasPorCategoria(idAnuncio)
            val db =
                FirebaseFirestore.getInstance().collection(Variables.noticiasDB).document(idAnuncio)
            constantesPublicidad.obtenerLocalidaGeneroTipoCuenta(db, Variables.tipoNoticia)
            binding.reserva.setOnClickListener {
                dialog = BottomSheetDialog(this)
                bottomSheetDialog(
                    Variables.reserva,
                    this,
                    idAnuncio,
                    binding.titulo.text.toString(),
                    Variables.noticiasDB, ""
                )
                dialog.show()
            }
            binding.comprar.setOnClickListener {
                dialog = BottomSheetDialog(this)
                bottomSheetDialog(
                    Variables.compra,
                    this,
                    idAnuncio,
                    binding.titulo.text.toString(),
                    Variables.noticiasDB, ""
                )
                dialog.show()
            }
        }

    }

    private fun obtenerNoticiasTiendas() {
        val idAnuncio = intent.getStringExtra(Variables.idAnuncio).toString()
        val idTienda = intent.getStringExtra(Variables.idTienda).toString()
        val db = FirebaseFirestore.getInstance().collection(Variables.tiendas).document(idTienda)
            .collection(Variables.noticiasDB).document(idAnuncio)

        db.get().addOnSuccessListener { res ->
            if (res.exists()) {
                val datos = res.data
                val titulo = datos?.get(Variables.titulo) as? String ?: ""
                val tipo = datos?.get(Variables.tipoPromo) as? String ?: ""
                val propietario = datos?.get(Variables.propietario) as? String ?: ""
                val contenido = datos?.get(Variables.contenido) as? String ?: ""
                val precio = datos?.get(Variables.price) as? String ?: ""
                val reserva = datos?.get(Variables.reserva) as? Boolean ?: false
                val compra = datos?.get(Variables.compra) as? Boolean ?: false

                binding.titulo.setText(titulo)
                binding.propietario.setText(propietario)
                binding.contenido.setText(contenido)
                binding.servicioONoticia.setText(tipo)
                if (reserva && compra) {
                    binding.comprar.isVisible = true
                    binding.reserva.isVisible = true
                } else if (reserva) {
                    binding.reserva.isVisible = true
                } else if (compra) {
                    binding.comprar.isVisible = true
                }
                if (precio.isNotEmpty()) {
                    binding.includeLinealPrecios.precio.text = "S/ ${precio}.00"
                } else {
                    binding.includeLinealPrecios.linealPrecio.isVisible = false
                }

            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun obtenerNoticiasPorCategoria(id: String) {
        fun obtenerPorCategoria(categoriaP: String, id: String) {
            // Mostrar el ProgressBar al iniciar la carga
            binding.ProgrsVar.isVisible = true
            binding.recicleViewAnuncios.isVisible = false
            binding.noEnctrado.isVisible = false

            val db = FirebaseFirestore.getInstance().collection(Variables.noticiasDB)

            db.whereEqualTo(Variables.categoria, categoriaP)
                .get()
                .addOnSuccessListener { res ->

                    for (datos in res) {
                        val data = datos.data
                        val documentId = datos.id // ID del documento actual

                        if (documentId != id) { // Verificar que el ID sea diferente del pasado
                            val titulo = data[Variables.titulo] as? String ?: ""
                            val contenido = data[Variables.Contenido] as? String ?: ""
                            val fecha = data[Variables.fecha] as? String ?: ""
                            val imagen = data[Variables.imagenUrl] as? String ?: ""
                            val estado = data[Variables.estado] as? String ?: ""
                            val numeroTelf = data[Variables.numero] as? String ?: ""
                            val mensaje = data[Variables.whatsappmsj] as? String ?: ""
                            val mapFechas = data[Variables.fechas] as? Map<String, Any>
                            val fechaVencimientomap =
                                mapFechas?.get(Variables.fecha_vencimiento) as? String ?: ""
                            val estadoVencimineto = data[Variables.estado] as? String ?: ""
                            val categoria = data[Variables.categoria] as? String ?: ""
                            val tipoPromo = data[Variables.tipoPromo] as? String ?: ""
                            val idTiendaProp = data[Variables.idTiendaProp] as? String ?: ""
                            val localidad = data[Variables.localidad] as? String ?: ""
                            val propietario = data[Variables.propietario] as? String ?: ""
                            val efectivo = data[Variables.efectivo] as? Boolean ?: false
                            val yape = data[Variables.yape] as? Boolean ?: false
                            val tipT = data[Variables.tipo_T] as? Boolean ?: false
                            val listener = data[Variables.listener] as? Boolean ?: false
                            val descuento_boolean =
                                data?.get(Variables.descuento_boolean) as? Boolean ?: false
                            val descuento = data?.get(Variables.descuento) as? String ?: ""
                            val porcentajeDescuento =
                                data?.get(Variables.porcentajeDescuento) as? Number ?: 0

                            val anuncio = dataClassAnuncios(
                                propietario,
                                contenido,
                                fecha,
                                titulo,
                                imagen,
                                estado,
                                documentId, // Usamos el ID del documento
                                numeroTelf,
                                mensaje,
                                fechaVencimientomap,
                                estadoVencimineto,
                                categoria,
                                tipoPromo,
                                idTiendaProp,
                                localidad,
                                efectivo,
                                yape,
                                tipT,
                                listener, descuento_boolean, descuento, porcentajeDescuento
                            )

                            lista.add(anuncio)
                        }
                    }

                    // Verificar el tamaño de la lista y mostrar los elementos correspondientes
                    if (lista.isEmpty()) {
                        binding.noEnctrado.isVisible = true
                        binding.recicleViewAnuncios.isVisible = false
                    } else {
                        binding.noEnctrado.isVisible = false
                        binding.recicleViewAnuncios.isVisible = true
                        inicializarReciles(
                            Variables.tipoNoticia,
                            lista,
                            binding.recicleViewAnuncios,
                            this,
                            zoomout,
                            zoonIn
                        )
                    }

                    // Ocultar el ProgressBar después de procesar los datos
                    binding.ProgrsVar.isVisible = false
                }
                .addOnFailureListener { e ->
                    // Ocultar el ProgressBar y mostrar mensaje de error
                    binding.ProgrsVar.isVisible = false
                    binding.recicleViewAnuncios.isVisible = false
                    binding.noEnctrado.isVisible = true
                    Log.e("FirestoreError", "Error obteniendo noticias: ${e.message}")
                }
        }


        val dbNoticia =
            FirebaseFirestore.getInstance().collection(Variables.noticiasDB).document(id)
        dbNoticia.get().addOnSuccessListener { res ->
            if (res.exists()) {
                val data = res.data
                val categoria = data?.get(Variables.categoria) as? String ?: ""
                obtenerPorCategoria(categoria, id)
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun inicializarReciles(
        tipo: String,
        listaAnuncios: MutableList<dataClassAnuncios>,
        recicleNoticias: RecyclerView,
        context: Context, zoomout: Animation?, zoomouts: Animation?,
    ) {
        val recicle = recicleNoticias
        recicle.layoutManager = LinearLayoutManager(context)
        recicle.adapter = adaptadorAnuncios(
            true,
            tipo,
            zoomout,
            zoomouts,
            listaAnuncios,
            context,
            { dataClassAnuncios -> createAndShareDynamicLink(dataClassAnuncios, context) }
        )
    }


    @SuppressLint("MissingInflatedId")
    private fun verinfoPublicidad(
        web: String? = null,
        fb: String? = null,
        ig: String? = null,
        tk: String? = null,
        idNoticia: String? = null,
        ubicacions: String,
        venciminetoPromo: String,
        fechaPublicadaPromo: String,
        estadoVencimineto: String,
        tipo_T: Boolean,
        contexto: Context,
    ) {
        // Usa ViewBinding para inflar la vista
        val binding = BottomSheetInfoPublicidadBinding.inflate(LayoutInflater.from(contexto))
        val view = binding.root

        bottomSheet = binding.cerrar
        val vistaAnuncios = vistas_anuncios_general()

        // Asignación directa con ViewBinding
        val ubicacionIMG = binding.mapa
        val estado = binding.estado
        val fechaPublicada = binding.fechaPublicada
        val vencimiento = binding.finaliza
        val ubicacionTienda = binding.ubicacion
        val tipo_tienda = binding.modalidaNegocio
        val linealUbicacion = binding.linealUbicacion

        if (idNoticia != null) {
            val db =
                FirebaseFirestore.getInstance().collection(Variables.noticiasDB).document(idNoticia)
            db.get().addOnSuccessListener { res ->
                if (res.exists()) {
                    val datos = res.data
                    val fechas = datos?.get(Variables.fechas) as? Map<String, String>
                    val ubi = datos?.get(Variables.ubicacion) as? String ?: ""

                    if (ubi.isNullOrEmpty()) {
                        linealUbicacion.isVisible = false
                    } else {
                        linealUbicacion.isVisible = true
                        ubicacionTienda.text = ubi
                    }

                    if (fechas != null) {
                        val fechaActivacion = fechas["fecha_activacion"]
                        val fechaVencimiento = fechas["fecha_vencimiento"]

                        vencimiento.text = fechaVencimiento
                        fechaPublicada.text = fechaActivacion
                    } else {
                        println("No se encontró el campo 'fechas' en el documento.")
                    }
                    vistaAnuncios.ocultarContenedorSiEstaVacio(ig ?: "", binding.containerIG)
                    vistaAnuncios.ocultarContenedorSiEstaVacio(fb ?: "", binding.containerFB)
                    vistaAnuncios.ocultarContenedorSiEstaVacio(tk ?: "", binding.containerTK)
                    vistaAnuncios.ocultarContenedorSiEstaVacio(web ?: "", binding.containerWEB)
                    vistaAnuncios.configurarListenerRedSocial(
                        dialog,
                        this,
                        binding.ig,
                        ig ?: "",
                        "com.instagram.android"
                    )
                    vistaAnuncios.configurarListenerRedSocial(
                        dialog,
                        this,
                        binding.fb,
                        fb ?: "",
                        "com.facebook.katana"
                    )
                    vistaAnuncios.configurarListenerRedSocial(
                        dialog,
                        this,
                        binding.tk,
                        tk ?: "",
                        "com.zhiliaoapp.musically"
                    )
                    vistaAnuncios.configurarListenerRedSocial(
                        dialog,
                        this,
                        binding.web,
                        web ?: "",
                        "",
                        esWeb = true
                    )
                } else {
                    println("El documento con ID $idNoticia no existe.")
                }
            }.addOnFailureListener { exception ->
                println("Error al obtener el documento: ${exception.message}")
            }
        } else {
            println("El ID de la noticia es nulo. No se puede realizar la operación.")
        }

        bottomSheet.setOnClickListener {
            dialog.dismiss()
        }

        if (tipo_T) {
            tipo_tienda.text = "Fisica"
        } else {
            tipo_tienda.text = "Virtual"
        }

        estado.text = estadoVencimineto
        fechaPublicada.text = fechaPublicadaPromo
        vencimiento.text = venciminetoPromo
        ubicacionTienda.text = ubicacions

        ubicacionIMG.setOnClickListener {
            val direccion = ubicacions
            constantes.abrirGoogleMaps(this, direccion)
            finish()
        }

        dialog.setContentView(view)
    }


    private fun obtenerDatosPromocion(
        idPromo: String,
    ) {
        val db = FirebaseFirestore.getInstance().collection(Variables.noticiasDB).document(idPromo)
        db.get()
            .addOnSuccessListener { resultado ->
                if (resultado.exists()) {
                    val data = resultado.data
                    val ubicacion = data?.get(Variables.ubicacion) as? String ?: ""
                    val venciminetoPromo = data?.get(Variables.vencimineto) as? String ?: ""
                    val fechaPublicadaPromo = data?.get(Variables.fecha) as? String ?: ""
                    val estadoVencimineto = data?.get(Variables.estado) as? String ?: ""
                    val tipo_T = data?.get(Variables.tipo_T) as? Boolean ?: false
                    val categoriaPromo = data?.get(Variables.categoria) as? String
                    val contenidoPromo = data?.get(Variables.Contenido) as? String
                    val tituloPromo = data?.get(Variables.titulo) as? String
                    val propietario = data?.get(Variables.propietario) as? String
                    val tipo = data?.get(Variables.tipo) as? String
                    val precioBoolean = data?.get(Variables.precio) as? Boolean ?: ""
                    val price = data?.get(Variables.price) as? String ?: ""
                    val reserva = data?.get(Variables.reserva) as? Boolean ?: false
                    val compra = data?.get(Variables.compra) as? Boolean ?: false
                    val metodo_compra_web_Geinz =
                        data?.get(Variables.metodo_compra_web_Geinz) as? Boolean ?: false
                    val link_compra = data?.get(Variables.link_compra) as? String ?: ""
                    val descuento = data?.get(Variables.descuento) as? String ?: ""
                    val descuento_boolean =
                        data?.get(Variables.descuento_boolean) as? Boolean ?: false
                    val porcentajeDescuento =
                        data?.get(Variables.porcentajeDescuento) as? Number ?: 0
                    val web = data?.get(Variables.web) as? String ?: ""
                    val tk = data?.get(Variables.tk) as? String ?: ""
                    val ig = data?.get(Variables.ig) as? String ?: ""
                    val fb = data?.get(Variables.fb) as? String ?: ""
                    if (reserva && compra) {
                        binding.comprar.isVisible = true
                        binding.reserva.isVisible = true
                    } else if (reserva) {
                        binding.reserva.isVisible = true
                    } else if (compra) {
                        binding.comprar.isVisible = true
                    } else if (metodo_compra_web_Geinz) {
                        binding.comprarWeb.isVisible = true
                    }
                    if (price.isNotEmpty()) {
                        binding.includeLinealPrecios.precio.text = "S/${price}"
                    } else {
                        binding.includeLinealPrecios.linealPrecio.isVisible = false
                    }
                    if (precioBoolean == false) {
                        binding.includeLinealPrecios.linealPrecio.isVisible = false
                    } else {
                        binding.includeLinealPrecios.linealPrecio.isVisible = true
                    }
                    when (tipo) {
                        Variables.tipoNoticia -> {
                            binding.servicioONoticia.text = Variables.tipoNoticia
                        }

                        "oferta" -> {
                            binding.servicioONoticia.text = "oferta"
                        }
                    }
                    if (descuento_boolean) {
                        binding.includeLinealPrecios.textViewPriceBefore.isVisible = true
                        binding.includeLinealPrecios.descuentoPorcentaje.isVisible = true
                        binding.includeLinealPrecios.textViewPriceBefore.text = "S/ $price.00"
                        binding.includeLinealPrecios.precio.text = "S/ $descuento.00"
                        binding.includeLinealPrecios.descuentoPorcentaje.text =
                            "- ${porcentajeDescuento}%"
                    } else {
                        binding.includeLinealPrecios.precio.text = "S/ $price.00"
                        binding.includeLinealPrecios.textViewPriceBefore.isVisible = false
                        binding.includeLinealPrecios.descuentoPorcentaje.isVisible = false
                    }
                    binding.contenido.text = contenidoPromo
                    binding.titulo.text = tituloPromo
                    binding.propietario.text = propietario


                    binding.comprarWeb.setOnClickListener {
                        val link = link_compra

                        if (link.isNotEmpty()) {
                            try {

                                val chromeIntent =
                                    Intent(Intent.ACTION_VIEW, Uri.parse(link)).apply {
                                        setPackage("com.android.chrome")
                                    }
                                if (chromeIntent.resolveActivity(packageManager) != null) {
                                    startActivity(chromeIntent)
                                } else {
                                    val fallbackIntent = Intent(Intent.ACTION_VIEW, Uri.parse(link))
                                    if (fallbackIntent.resolveActivity(packageManager) != null) {
                                        startActivity(fallbackIntent)
                                    } else {
                                        Toast.makeText(
                                            this,
                                            "No hay aplicaciones disponibles para abrir el enlace",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                }
                            } catch (e: Exception) {
                                Toast.makeText(this, "Error al abrir el enlace", Toast.LENGTH_SHORT)
                                    .show()
                            }
                        } else {
                            Toast.makeText(this, "Enlace no disponible", Toast.LENGTH_SHORT).show()
                        }
                    }

                    binding.informacionPublicidad.setOnClickListener {
                        dialog = BottomSheetDialog(this)
                        verinfoPublicidad(
                            web, fb, ig, tk,
                            idPromo,
                            ubicacion,
                            venciminetoPromo,
                            fechaPublicadaPromo,
                            estadoVencimineto,
                            tipo_T,
                            this
                        )
                        dialog.show()
                    }
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "error al obtener la imagen", Toast.LENGTH_SHORT).show()
            }

        val FireStorage = FirebaseStorage.getInstance().getReference("noticias_imagenes/$idPromo")
        fetchAllImageUrls(FireStorage)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    @SuppressLint("MissingInflatedId")
    private fun bottomSheetDialog(
        tipo_res_compra: String,
        contexto: Context,
        idAnuncio: String,
        nombre_servicio: String,
        tipo: String,
        idTienda: String,
    ) {
        bindingBottomShet = BottomSheetReservasBinding.inflate(LayoutInflater.from(contexto))
        val view = bindingBottomShet.root
        bottomSheet = bindingBottomShet.cerrar
        val nombreus = bindingBottomShet.nombreUser
        val numeroUser = bindingBottomShet.numeroContacto
        val apellidoUser = bindingBottomShet.apellidouser
        val localidadUser = bindingBottomShet.localida
        val Title = bindingBottomShet.tvTitle
        val reservar = bindingBottomShet.btnApply
        val img_plin = bindingBottomShet.imagenPlin
        val dniUser = bindingBottomShet.dniUser
        val cancelar = bindingBottomShet.btnCancel
        val yapeBTN = bindingBottomShet.imagenYape
        val nombreSevicio = bindingBottomShet.nombreSevicio
        val vechaReserva = bindingBottomShet.fechareserva
        val hora_reserva = bindingBottomShet.horareserva
        val metodoPagoGrupo = bindingBottomShet.metodoPago
        val yapeRadio = bindingBottomShet.Yape
        val efectivoBTN = bindingBottomShet.Efectivo
        val plin_radio = bindingBottomShet.plin

        bindingBottomShet.fechareserva.setOnClickListener {
            mostrarFechaDialog_horaDialog.mostrarFechaDialogVista(
                bindingBottomShet.fechareserva,
                this
            )
        }
        bindingBottomShet.horareserva.setOnClickListener {
            mostrarFechaDialog_horaDialog.showTimePicker(this, bindingBottomShet.horareserva)
        }

        when (tipo_res_compra) {
            Variables.reserva -> {
                bindingBottomShet.fechaimt.isVisible = true
                bindingBottomShet.horaint.isVisible = true
            }

            Variables.compra -> {
                bindingBottomShet.fechaimt.isVisible = false
                bindingBottomShet.horaint.isVisible = false
            }

            else -> ""
        }

        fun obtenerDatosFirestore(referencia: DocumentReference) {
            referencia.get().addOnSuccessListener { res ->
                if (res.exists()) {
                    val data = res.data
                    val qr_yape = data?.get(Variables.yape_img) as? String
                    val qrPlin = data?.get(Variables.plin_img) as? String
                    val efectivo = data?.get(Variables.efectivo) as? Boolean
                    val yape = data?.get(Variables.yape) as? Boolean
                    val plin = data?.get(Variables.plin) as? Boolean ?: false
                    val descuento = data?.get(Variables.descuento_boolean) as? Boolean ?: false
                    val descuentoNumber = data?.get(Variables.descuento) as? String ?: ""
                    val price = data?.get(Variables.price) as? String
                    val numeroTienda = data?.get(Variables.numero) as? String

                    Glide.with(contexto)
                        .load(qr_yape)
                        .placeholder(R.drawable.cargando_img_geinz_500)
                        .error(R.drawable.agregar_img_geinz)
                        .into(bindingBottomShet.imagenYape)
                    Glide.with(contexto)
                        .load(qrPlin)
                        .placeholder(R.drawable.cargando_img_geinz_500)
                        .error(R.drawable.agregar_img_geinz)
                        .into(img_plin)

                    bindingBottomShet.imagenYape.setOnClickListener {
                        val imageUrl = qr_yape
                        val dialogFragment = ImageDialogFragmentURL.newInstance(imageUrl)
                        dialogFragment.show(
                            (this as AppCompatActivity).supportFragmentManager,
                            "image_dialog"
                        )
                    }
                    bindingBottomShet.imagenPlin.setOnClickListener {
                        val imageUrl = qrPlin
                        val dialogFragment = ImageDialogFragmentURL.newInstance(imageUrl)
                        dialogFragment.show(
                            (this as AppCompatActivity).supportFragmentManager,
                            "image_dialog"
                        )
                    }
                    if (descuento) {
                        bindingBottomShet.montoTotal.text = "S/${descuentoNumber}"
                    } else {
                        bindingBottomShet.montoTotal.text = "S/${price}"
                    }

                    if (yape == true && efectivo == true && plin) {
                        yapeBTN.isVisible = true
                        efectivoBTN.isVisible = true
                        yapeRadio.isVisible = true
                        plin_radio.isVisible = true
                    } else if (yape == true && efectivo == true) {
                        yapeBTN.isVisible = true
                        efectivoBTN.isVisible = true
                        yapeRadio.isVisible = true
                        plin_radio.isVisible = false
                    } else if (yape == true && plin) {
                        yapeBTN.isVisible = true
                        efectivoBTN.isVisible = false
                        yapeRadio.isVisible = true
                        plin_radio.isVisible = true
                    } else if (yape == true) {
                        yapeBTN.isVisible = true
                        efectivoBTN.isVisible = false
                        yapeRadio.isVisible = true
                        plin_radio.isVisible = false
                    } else if (efectivo == true) {
                        yapeBTN.isVisible = false
                        efectivoBTN.isVisible = true
                        yapeRadio.isVisible = false
                        plin_radio.isVisible = false
                    } else if (plin) {
                        plin_radio.isVisible = true
                        yapeBTN.isVisible = false
                        efectivoBTN.isVisible = false
                        yapeRadio.isVisible = false
                    } else {
                        bindingBottomShet.textoMetodoPago.isVisible = false
                        yapeBTN.isVisible = false
                        efectivoBTN.isVisible = false
                        yapeRadio.isVisible = false
                        plin_radio.isVisible = false
                    }
                    constantesCarrito.setearDatosUsuario { nombre, numero, localidad, apellido ->
                        nombreus.setText(nombre)
                        numeroUser.setText(numero)
                        apellidoUser.setText(apellido)
                        localidadUser.setText(localidad)
                        filtradoLocalidadElementos.listaFiltrado(bindingBottomShet.localida)
                    }
                    filtradoLocalidadElementos.listaFiltrado(bindingBottomShet.localida)
                    nombreSevicio.text = nombre_servicio
                    constantesCarrito.obtnerfechaHora(
                        bindingBottomShet.horaActual,
                        bindingBottomShet.FechaActual
                    )

                    bindingBottomShet.pagoEfectivo.isVisible = false

                    bindingBottomShet.metodoPago.setOnCheckedChangeListener { group, checkedId ->
                        when (checkedId) {
                            R.id.Efectivo -> {
                                bindingBottomShet.layoutYape.isVisible = false
                                bindingBottomShet.pagoEfectivo.isVisible = true
                                bindingBottomShet.imagenYape.isVisible = false
                            }

                            R.id.Yape -> {
                                bindingBottomShet.imagenYape.isVisible = true
                                bindingBottomShet.layoutYape.isVisible = true
                                bindingBottomShet.pagoEfectivo.isVisible = false
                                bindingBottomShet.imagenPlin.isVisible = false
                            }

                            R.id.plin -> {
                                bindingBottomShet.imagenYape.isVisible = false
                                bindingBottomShet.layoutYape.isVisible = true
                                bindingBottomShet.imagenPlin.isVisible = true
                                bindingBottomShet.pagoEfectivo.isVisible = false
                            }
                        }

                    }
                    reservar.setOnClickListener {
                        if (nombreus.text.toString().isNotEmpty() &&
                            apellidoUser.text.toString().isNotEmpty() &&
                            localidadUser.text.toString().isNotEmpty() &&
                            dniUser.text.toString().isNotEmpty() &&
                            numeroUser.text.toString().isNotEmpty() &&
                            vechaReserva.text.toString().isNotEmpty() &&
                            hora_reserva.text.toString().isNotEmpty()
                        ) {

                            if (yapeRadio.isChecked || efectivoBTN.isChecked || plin_radio.isChecked) {
                                numeroTienda?.let { numero ->
                                    //constantes.contactarWhatsapp(numero, "", this)
                                }
                            } else {
                                Toast.makeText(
                                    this,
                                    "Seleccione un método de pago",
                                    Toast.LENGTH_SHORT
                                )
                                    .show()
                            }
                        } else {
                            Toast.makeText(
                                this,
                                "Rellene todos los campos necesarios",
                                Toast.LENGTH_SHORT
                            ).show()
                        }

                    }
                }

            }
        }

        val db = when (tipo) {
            Variables.tiendas -> {
                val yapeRuta = "Tiendas/$idTienda/QR_pagos/Yape.jpg"
                val plinrRuta = "Tiendas/$idTienda/QR_pagos/Plin.jpg"
                constantesImagenes.obtenerURLDescarga(this, bindingBottomShet.imagenYape, yapeRuta)
                constantesImagenes.obtenerURLDescarga(this, bindingBottomShet.imagenPlin, plinrRuta)
                if (idTienda.isNotBlank()) {
                    FirebaseFirestore.getInstance().collection(Variables.tiendas).document(idTienda)
                        .collection(Variables.noticiasDB).document(idAnuncio)
                } else {
                    // Manejar el caso en que idTienda esté vacío
                    Log.e("Error", "El idTienda está vacío para el tipo tiendas.")
                    return
                }

            }

            Variables.noticiasDB -> {
                FirebaseFirestore.getInstance().collection(Variables.noticiasDB).document(idAnuncio)
            }

            else -> {
                Log.e("Error", "Tipo desconocido: $tipo")
                return
            }
        }

        obtenerDatosFirestore(db)

        cancelar.setOnClickListener {
            dialog.dismiss()
        }
        bottomSheet.setOnClickListener {
            dialog.dismiss()
        }

        dialog.setContentView(view)

    }

    private fun fetchAllImageUrls(storageRef: StorageReference) {
        val lista = mutableListOf<CarouselItem>()
        storageRef.listAll()
            .addOnSuccessListener { listResult: ListResult ->
                val items = listResult.items
                for (item in items) {
                    item.downloadUrl.addOnSuccessListener { uri ->
                        lista.add(CarouselItem(uri.toString()))
                        binding.carrusel.setData(lista)
                    }.addOnFailureListener { exception ->
                        exception.printStackTrace()
                    }
                }
            }
            .addOnFailureListener { exception ->
                exception.printStackTrace()
            }
    }

    private fun mostrarDatos() {
        binding.scroll.isVisible = true
        binding.loading.isVisible = false
    }

    override fun onStart() {
        super.onStart()
        iniciarContadorVista()
    }

    override fun onStop() {
        super.onStop()
        cancelarContadorVista()
    }

    private fun iniciarContadorVista() {
        val idAnuncio = intent.getStringExtra(Variables.idAnuncio).toString()
        val db =
            FirebaseFirestore.getInstance().collection(Variables.noticiasDB).document(idAnuncio)
        vistaTimer = object : CountDownTimer(tiempoParaContarVista, 1000) {
            override fun onTick(millisUntilFinished: Long) {}

            override fun onFinish() {
                constantesPublicidad.agregarCantidadClickAnuncios(db, "", "vistas")
            }
        }.start()
    }

    private fun cancelarContadorVista() {
        vistaTimer?.cancel()
    }


}

