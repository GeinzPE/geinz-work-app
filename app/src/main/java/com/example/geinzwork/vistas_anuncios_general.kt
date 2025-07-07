package com.geinzz.geinzwork

import android.content.Context
import android.content.Intent
import android.graphics.Paint
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.ViewGroup
import android.widget.AutoCompleteTextView
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.viewbinding.ViewBinding
import com.bumptech.glide.Glide
import com.example.geinzwork.constantesGeneral.Variables
import com.example.geinzwork.fragmentos.img_completa.FullscreenImageDialog
import com.example.geinzwork.vistaTrabajador.ver_promociones
import com.geinzz.geinzwork.constantesGeneral.constantes
import com.geinzz.geinzwork.constantesGeneral.constantesCarrito
import com.geinzz.geinzwork.constantesGeneral.constantesPublicidad
import com.geinzz.geinzwork.constantesGeneral.constantesPublicidad.agregarCantidadClickAnuncios
import com.geinzz.geinzwork.constantesGeneral.constantes_redes
import com.geinzz.geinzwork.constantesGeneral.filtradoLocalidadElementos
import com.geinzz.geinzwork.constantesGeneral.mostrarFechaDialog_horaDialog
import com.geinzz.geinzwork.databinding.ActivityVistasAnunciosGeneralBinding
import com.geinzz.geinzwork.databinding.BottomSheetRedesBinding
import com.geinzz.geinzwork.databinding.BottomSheetReservasBinding
import com.geinzz.geinzwork.databinding.ItemMasPublicidadesGeinzBinding
import com.geinzz.geinzwork.vistaTiendas.VistaTienda
import com.geinzz.geinzwork.vistaTiendas.constantesVistaTiendas
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDragHandleView
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.dynamiclinks.androidParameters
import com.google.firebase.dynamiclinks.dynamicLinks
import com.google.firebase.dynamiclinks.googleAnalyticsParameters
import com.google.firebase.dynamiclinks.iosParameters
import com.google.firebase.dynamiclinks.itunesConnectAnalyticsParameters
import com.google.firebase.dynamiclinks.shortLinkAsync
import com.google.firebase.dynamiclinks.socialMetaTagParameters
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import org.imaginativeworld.whynotimagecarousel.listener.CarouselListener
import org.imaginativeworld.whynotimagecarousel.model.CarouselItem

class vistas_anuncios_general : AppCompatActivity() {
    private lateinit var binding: ActivityVistasAnunciosGeneralBinding
    private lateinit var bottomSheet: BottomSheetDragHandleView
    private lateinit var bindingBottomShet: BottomSheetReservasBinding
    private lateinit var bindingRedes: BottomSheetRedesBinding
    private lateinit var dialog: BottomSheetDialog
    private lateinit var firebaseAuth: FirebaseAuth
    private val lista = mutableListOf<CarouselItem>()
    private var vistaTimer: CountDownTimer? = null
    private val tiempoParaContarVista: Long = 20000

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityVistasAnunciosGeneralBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        firebaseAuth = FirebaseAuth.getInstance()
        val textViewPriceBefore = binding.includeLinealPrecios.textViewPriceBefore
        textViewPriceBefore.paintFlags =
            textViewPriceBefore.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
        textViewPriceBefore.textSize = 12f
        val documento = intent.getStringExtra(Variables.documentoAdapterPromo).toString()
        val anuncio = intent.getStringExtra(Variables.anuncio).toString()


        constantes.carga(4000, { mostrarDatos() })
        binding.retroceder.setOnClickListener {
            onBackPressed()
        }
        binding.verMasPromociones.setOnClickListener {
            startActivity(Intent(this, ver_promociones::class.java))
        }
        obtenerImagenesFirestorage(documento, anuncio)
        obtenerDatosPublicidad(documento, anuncio)
        if (documento.equals(Variables.anunciosSegundarios) || documento.equals(Variables.anunciosTerceros)) {
            binding.realtive.isVisible = true
            binding.imgAnuncio.isVisible = false
            binding.redes.isVisible = true
            binding.containerBasicoGeinz.isVisible = false
            obtenerDatos_Avanzado_Premiun(documento, anuncio)
        } else {
            binding.redes.isVisible = false
            binding.realtive.isVisible = false
            binding.imgAnuncio.isVisible = true
            binding.containerBasicoGeinz.isVisible = true
            binding.compartir.isVisible = true
            retornarubiTienda(documento,anuncio,binding.ubicacion)
        }

        binding.popup.setOnClickListener {
            popup()
        }
        binding.btnPLanes.setOnClickListener {
            startActivity(Intent(this, Crea_tu_publicidad::class.java))
        }

        binding.ubicacion.setOnClickListener {

        }
        binding.compartir.setOnClickListener {
            createAndShareDynamicLink(this)
        }
        val db = FirebaseFirestore.getInstance().collection(Variables.anuncios).document(documento)
            .collection(Variables.anuncios).document(anuncio)
        constantesPublicidad.obtenerLocalidaGeneroTipoCuenta(db, Variables.tipoPublicidad)


    }

    private fun retornarubiTienda(docuemnto: String, anuncio: String,ubiBTN:ImageView) {
        val db = FirebaseFirestore.getInstance().collection(Variables.anuncios).document(docuemnto)
            .collection(Variables.anuncios).document(anuncio)
        db.get().addOnSuccessListener { res ->
            if (res.exists()) {
                val data = res.data
                val ubicaciontxt = data?.get("ubicacion_tienda") as? String ?: ""
                val ubicacionBoolena = data?.get("ubicacion") as? Boolean ?: false

                if(ubicacionBoolena){
                    ubiBTN.isVisible=true
                    ubiBTN.setOnClickListener {
                        constantes.abrirGoogleMaps(this, ubicaciontxt)
                    }

                }else{
                    ubiBTN.isVisible=false
                }
            } else {
                println("error al obtener la ubicacion")
            }
        }.addOnFailureListener { e->
            println("error al obtener la ubicacion $e")
        }

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
        val documento = intent.getStringExtra(Variables.documentoAdapterPromo).toString()
        val anuncio = intent.getStringExtra(Variables.anuncio).toString()
        vistaTimer = object : CountDownTimer(tiempoParaContarVista, 1000) {
            override fun onTick(millisUntilFinished: Long) {}

            override fun onFinish() {
                val db = FirebaseFirestore.getInstance().collection(Variables.anuncios)
                    .document(documento)
                    .collection(Variables.anuncios).document(anuncio)

                constantesPublicidad.agregarCantidadClickAnuncios(db, anuncio, Variables.vistas)
            }
        }.start()
    }

    private fun cancelarContadorVista() {
        vistaTimer?.cancel()
    }

    private fun popup() {
        val popup = PopupMenu(this, binding.popup)
        popup.menu.add(Menu.NONE, 1, 1, "Compartir")
        popup.menu.add(Menu.NONE, 2, 2, "Crea tu publicidad")
        popup.show()
        popup.setOnMenuItemClickListener { item ->
            val itemID = item.itemId
            if (itemID == 1) {
                createAndShareDynamicLink(this)
            } else if (itemID == 2) {
                startActivity(Intent(this, Crea_tu_publicidad::class.java))
            }
            return@setOnMenuItemClickListener true
        }
    }


    @RequiresApi(Build.VERSION_CODES.O)
    private fun bottomshetGeneral(
        titulo: String,
        reserva_Adrquiri: String,
        tipoServicioProducto: String,
        nombreServicio: String,
        numeroTienda: String,
        efectivo: Boolean,
        yape: Boolean,
        yape_img: String,
        plin: Boolean,
        plin_img: String,
        precio: String,
        mensaje: String
    ) {
        bindingBottomShet = BottomSheetReservasBinding.inflate(LayoutInflater.from(this))
        val view = bindingBottomShet.root
        bottomSheet = bindingBottomShet.cerrar
        bindingBottomShet.btnCancel.setOnClickListener {
            dialog.dismiss()
        }
        constantesCarrito.obtnerfechaHora(
            bindingBottomShet.horaActual,
            bindingBottomShet.FechaActual
        )
        val nombre = bindingBottomShet.nombreUser
        val apellidop = bindingBottomShet.apellidouser
        val dni = bindingBottomShet.dniUser
        val number = bindingBottomShet.numeroContacto
        val btnApply = bindingBottomShet.btnApply
        val localidadUser = bindingBottomShet.localida
        val fechaActual = bindingBottomShet.FechaActual
        val metodoPago = bindingBottomShet.metodoPago
        val horaActual = bindingBottomShet.horaActual
        bindingBottomShet.nombreSevicio.text = nombreServicio
        bindingBottomShet.fechaimt.isVisible = false
        bindingBottomShet.horaint.isVisible = false
        bindingBottomShet.dniint.isVisible = false
        bindingBottomShet.servicioProducto.text = tipoServicioProducto
        bindingBottomShet.montoTotal.text = "$precio"
        bindingBottomShet.tvTitle.text = titulo
        bindingBottomShet.Yape.isVisible = false
        bindingBottomShet.Efectivo.isVisible = false
        bindingBottomShet.plin.isVisible = false

        verificarVisibilidad(efectivo, yape, plin)
        if (!yape_img.isNullOrEmpty()) {
            try {
                Glide.with(this)
                    .load(yape_img)
                    .placeholder(R.drawable.cargando_img_geinz_500)
                    .error(R.drawable.cargando_img_geinz_500)
                    .into(bindingBottomShet.imagenYape)
            } catch (e: Exception) {
                println("error al setear la img")
            }

        }
        if (!plin_img.isNullOrEmpty()) {
            try {
                Glide.with(this)
                    .load(plin_img)
                    .placeholder(R.drawable.cargando_img_geinz_500)
                    .error(R.drawable.agregar_img_geinz)
                    .into(bindingBottomShet.imagenPlin)
            } catch (e: Exception) {
                println("error al setear la img")
            }
        }
        bindingBottomShet.imagenYape.setOnClickListener { listenerImg(yape_img) }
        bindingBottomShet.imagenPlin.setOnClickListener { listenerImg(plin_img) }
        bindingBottomShet.horareserva.setOnClickListener {
            mostrarFechaDialog_horaDialog.showTimePicker(
                this,
                bindingBottomShet.horareserva
            )
        }

        constantesCarrito.setearDatosUsuario { nombreus, numerous, localidad, apellido ->
            if (nombreus != null && numerous != null) {
                nombre.setText(nombreus)
                number.setText(numerous)
                apellidop.setText(apellido)
                localidadUser.setText(localidad)
                filtradoLocalidadElementos.listaFiltrado(bindingBottomShet.localida)
            } else {
                println("no se pudo encontrar su nombre")
            }
        }
        filtradoLocalidadElementos.listaFiltrado(bindingBottomShet.localida)
        metodoPago.setOnCheckedChangeListener { group, checkedId ->
            when (checkedId) {
                R.id.Efectivo -> {
                    bindingBottomShet.layoutYape.isVisible = false
                    bindingBottomShet.pagoEfectivo.isVisible = true
                }

                R.id.Yape -> {
                    bindingBottomShet.layoutYape.isVisible = true
                    bindingBottomShet.pagoEfectivo.isVisible = false
                    bindingBottomShet.imagenPlin.isVisible = false
                    bindingBottomShet.imagenYape.isVisible = true
                }

                R.id.plin -> {
                    bindingBottomShet.imagenPlin.isVisible = true
                    bindingBottomShet.imagenYape.isVisible = false
                    bindingBottomShet.layoutYape.isVisible = true
                    bindingBottomShet.pagoEfectivo.isVisible = false
                }
            }
        }
        btnApply.setOnClickListener {
            if (nombre.text.isNullOrEmpty() && apellidop.text.isNullOrEmpty() && dni.text.isNullOrEmpty() && number.text.isNullOrEmpty()) {
                Toast.makeText(
                    this,
                    "Todo los campos son requeridos",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                val metodoPagoSelect = when (metodoPago.checkedRadioButtonId) {
                    R.id.Efectivo -> "Efectivo"
                    R.id.Yape -> "Yape"
                    R.id.plin -> "Plin"
                    else -> ""
                }
                when (reserva_Adrquiri) {
                    "reserva" -> {
                        reservaMSJ(
                            this,
                            numeroTienda,
                            "RESERVA GEINZ",
                            nombre,
                            apellidop,
                            number,
                            horaActual,
                            localidadUser,
                            fechaActual,
                            metodoPagoSelect,
                            binding.TiendaPropietaria,
                            nombreServicio,
                            mensaje
                        )
                    }

                    "adquirir" -> {
                        reservaMSJ(
                            this,
                            numeroTienda,
                            "COMPRA GEINZ",
                            nombre,
                            apellidop,
                            number,
                            horaActual,
                            localidadUser,
                            fechaActual,
                            metodoPagoSelect,
                            binding.TiendaPropietaria,
                            nombreServicio,
                            mensaje
                        )
                    }

                    else -> ""
                }
            }
        }
        dialog.setContentView(view)
    }


    private fun reservaMSJ(
        context: Context,
        numeroTienda: String,
        titulo: String,
        nombre: TextView,
        apellido: TextView,
        numero: TextView,
        horaActual: TextView,
        locidad: AutoCompleteTextView,
        FechaActual: TextView,
        metodoPago: String, nombreTienda: TextView, producto: String, mensaje: String,
    ) {
        var mensajeProducto = " ------------------------------------------\n" +
                "|          $titulo           |\n" +
                "----------------------------------\n" +
                "| Nombre: ${nombre.text}  \n" +
                "| Apellido: ${apellido.text}\n" +
                "| Número de usuario: ${numero.text} \n" +
                "| Hora enviada: ${horaActual.text} \n" +
                "| Fecha enviada: ${FechaActual.text} \n" +
                "| Localidad: ${locidad.text} \n" +
                "| Metodo de pago: ${metodoPago}    \n" +
                "| Propietario : *${nombreTienda.text}*    \n" +
                "| Producto: *$producto* \n" +
                "------------------------------------------\n" +
                "$mensaje\n"
        constantes.contactarWhatsapp("+51 $numeroTienda", mensajeProducto, context)
    }

    private fun listenerImg(img: String) {
        val dialogFragment = ImageDialogFragmentURL.newInstance(img)
        dialogFragment.show((this as AppCompatActivity).supportFragmentManager, "image_dialog")
    }

    private fun verificarVisibilidad(efectivo: Boolean, yape: Boolean, plin: Boolean) {
        if (efectivo && yape && plin) {
            bindingBottomShet.Yape.isVisible = true
            bindingBottomShet.Efectivo.isVisible = true
            bindingBottomShet.plin.isVisible = true
        } else if (efectivo && yape) {
            bindingBottomShet.Yape.isVisible = true
            bindingBottomShet.Efectivo.isVisible = true
        } else if (efectivo && plin) {
            bindingBottomShet.Efectivo.isVisible = true
            bindingBottomShet.plin.isVisible = true
        } else if (yape && plin) {
            bindingBottomShet.Yape.isVisible = true
            bindingBottomShet.plin.isVisible = true
        } else if (efectivo) {
            bindingBottomShet.Efectivo.isVisible = true
        } else if (yape) {
            bindingBottomShet.Yape.isVisible = true
            bindingBottomShet.plin.isVisible = false
        } else if (plin) {
            bindingBottomShet.plin.isVisible = true
            bindingBottomShet.Yape.isVisible = false
        }
    }

    fun ocultarContenedorSiEstaVacio(
        url: String,
        contenedor: LinearLayout,
    ) {
        contenedor.isVisible = url.isNotEmpty()
    }

    fun configurarListenerRedSocial(
        dialog: BottomSheetDialog,
        context: Context,
        boton: ImageView,
        url: String,
        packageName: String,
        esWeb: Boolean = false,
    ) {
        boton.setOnClickListener {
            if (esWeb) {
                openWebsite(this, url)
            } else {
                constantes_redes.openApps(context, url, packageName)
            }
            dialog.dismiss()
        }
    }


    private fun bottom_shetRedes(
        web: String,
        fb: String,
        ig: String,
        tk: String,
        modalidadTienda: Boolean,
        publicadada: String,
        finaliza: String,
        ubi_icon_boolena: Boolean,
        ubi: String,
    ) {
        bindingRedes = BottomSheetRedesBinding.inflate(LayoutInflater.from(this))
        val view = bindingRedes.root
        bottomSheet = bindingRedes.cerrar

        ocultarContenedorSiEstaVacio(ig, bindingRedes.containerIG)
        ocultarContenedorSiEstaVacio(fb, bindingRedes.containerFB)
        ocultarContenedorSiEstaVacio(tk, bindingRedes.containerTK)
        ocultarContenedorSiEstaVacio(web, bindingRedes.containerWEB)

        configurarListenerRedSocial(dialog, this, bindingRedes.ig, ig, "com.instagram.android")
        configurarListenerRedSocial(dialog, this, bindingRedes.fb, fb, "com.facebook.katana")
        configurarListenerRedSocial(dialog, this, bindingRedes.tk, tk, "com.zhiliaoapp.musically")
        configurarListenerRedSocial(dialog, this, bindingRedes.web, web, "", esWeb = true)

        bindingRedes.modalidaNegocio.text = if (modalidadTienda == true) "Fisica" else "Virtual"
        bindingRedes.fechaPublicada.text = publicadada
        bindingRedes.finaliza.text = finaliza
        if (ubi.isNotEmpty()) {
            bindingRedes.ubicacion.text = ubi
            bindingRedes.mapa.setOnClickListener {
                constantes.abrirGoogleMaps(this, ubi)
            }
        } else {
            bindingRedes.linealUbi.isVisible = false
        }
        if (ubi_icon_boolena == false) {
            bindingRedes.mapa.isVisible = false
        } else {
            bindingRedes.mapa.isVisible = true
        }


        dialog.setContentView(view)

    }


    @RequiresApi(Build.VERSION_CODES.O)
    private fun obtenerDatosPublicidad(docuemnto: String, anuncio: String) {
        val db = FirebaseFirestore.getInstance().collection(Variables.anuncios).document(docuemnto)
            .collection(Variables.anuncios).document(anuncio)
        db.get().addOnSuccessListener { res ->
            if (res.exists()) {
                val data = res.data
                val nombreTienda = data?.get("nombreTienda") as? String
                val descripcionTeinda = data?.get("descripcion") as? String
                val fechas = data?.get("fechas") as? Map<String, String>
                val fechaVencimiento = fechas?.get("fecha_vencimiento") ?: "Fecha no disponible"
                val fecha_activacion = fechas?.get("fecha_activacion") ?: "Fecha no disponible"
                val reserva = data?.get("reserva") as? Boolean
                val compra = data?.get("compra") as? Boolean
                val precioBoolean = data?.get("precio_boolean") as? Boolean
                val ubicacionBoolena = data?.get("ubicacion") as? Boolean ?: false
                val tipoTienda = data?.get("tipoTienda") as? Boolean ?: false
                val ubicaciontxt = data?.get("ubicacion_tienda") as? String ?: ""
                val imagenUrl = data?.get(Variables.imagenUrl) as? String
                val efectivo = data?.get("efectivo") as? Boolean
                val yape = data?.get("yape") as? Boolean
                val idPropietario = data?.get("idPropietario") as? String ?: ""
                val yape_img = data?.get("yape_img") as? String
                val plin = data?.get("plin") as? Boolean
                val plin_img = data?.get("plin_img") as? String
                val numero = data?.get("numero") as? String
                val precio = data?.get("precio") as? String
                val titulo = data?.get("titulo") as? String
                val metodo_compra_web_Geinz = data?.get("metodo_compra_web_Geinz") as? Boolean
                val link_compra = data?.get("link_compra") as? String ?: ""
                val reservaVar = reserva ?: false
                val compraVar = compra ?: false
                val metodoCompraGeinz = metodo_compra_web_Geinz ?: false
                val mensaje = data?.get("mensaje") as? String ?: ""
                val descuento = data?.get("descuento") as? String ?: ""
                val descuento_boolean = data?.get("descuento_boolean") as? Boolean ?: false
                val porcentajeDescuento = data?.get("porcentajeDescuento") as? Number ?: 0


                val web = data?.get("web") as? String ?: ""
                val tk = data?.get("tk") as? String ?: ""
                val ig = data?.get("ig") as? String ?: ""
                val fb = data?.get("fb") as? String ?: ""

                if (precioBoolean == false) {
                    binding.includeLinealPrecios.linealPrecio.isVisible = false
                } else {
                    binding.includeLinealPrecios.linealPrecio.isVisible = true
                }

                binding.adquirirWeb.setOnClickListener {
                    val link = link_compra
                    openWebsite(this, link)
                }

                binding.adquirir.setOnClickListener {
                    if (firebaseAuth.currentUser == null) {
                        dialog = BottomSheetDialog(this)
                        constantesPublicidad.CreacionCuentaBottom_shett(
                            this,
                            dialog
                        )
                        dialog.show()
                    } else {
                        dialog = BottomSheetDialog(this)
                        val title = titulo ?: ""
                        val number = numero ?: ""
                        val isEfectivo = efectivo ?: false
                        val isYape = yape ?: false
                        val yapeImage = yape_img ?: ""
                        val isPlin = plin ?: false
                        val price = binding.includeLinealPrecios.precio.text.toString()?: ""
                        val plinImage = plin_img ?: ""
                        bottomshetGeneral(
                            "Adquirir",
                            "adquirir",
                            "Titulo de la publicidad : ",
                            title,
                            number,
                            isEfectivo,
                            isYape,
                            yapeImage,
                            isPlin,
                            plinImage,
                            price,
                            mensaje
                        )
                        dialog.show()
                    }
                }

                binding.reservar.setOnClickListener {
                    if (firebaseAuth.currentUser == null) {
                        dialog = BottomSheetDialog(this)
                        constantesPublicidad.CreacionCuentaBottom_shett(
                            this,
                            dialog
                        )
                        dialog.show()
                    } else {
                        dialog = BottomSheetDialog(this)
                        val title = titulo ?: ""
                        val number = numero ?: ""
                        val isEfectivo = efectivo ?: false
                        val isYape = yape ?: false
                        val yapeImage = yape_img ?: ""
                        val isPlin = plin ?: false
                        val plinImage = plin_img ?: ""
                        val price = precio ?: ""


                        bottomshetGeneral(
                            "Reserva",
                            "reserva",
                            "Título de la publicidad:",
                            title,
                            number,
                            isEfectivo,
                            isYape,
                            yapeImage,
                            isPlin,
                            plinImage,
                            binding.includeLinealPrecios.precio.text.toString(), mensaje
                        )

                        dialog.show()
                    }
                }
                binding.titulo.text = titulo
                binding.descripcionPublicidad.text = descripcionTeinda
                binding.TiendaPropietaria.text = nombreTienda

                if (descuento_boolean) {
                    binding.includeLinealPrecios.textViewPriceBefore.isVisible = true
                    binding.includeLinealPrecios.descuentoPorcentaje.isVisible = true
                    binding.includeLinealPrecios.textViewPriceBefore.text = "S/ $precio.00"
                    binding.includeLinealPrecios.precio.text = "S/ $descuento.00"
                    binding.includeLinealPrecios.descuentoPorcentaje.text =
                        "- ${porcentajeDescuento}%"
                } else {
                    binding.includeLinealPrecios.precio.text = "S/ $precio.00"
                    binding.includeLinealPrecios.textViewPriceBefore.isVisible = false
                    binding.includeLinealPrecios.descuentoPorcentaje.isVisible = false
                }

                if (reservaVar && compraVar) {
                    binding.adquirir.isVisible = true
                    binding.reservar.isVisible = true
                } else if (reservaVar) {
                    binding.reservar.isVisible = true
                } else if (compraVar) {
                    binding.adquirir.isVisible = true
                } else if (metodoCompraGeinz) {
                    binding.adquirirWeb.isVisible = true
                }
                try {
                    Glide.with(this)
                        .load(imagenUrl)
                        .centerCrop()
                        .into(binding.imgAnuncio)
                    binding.imgAnuncio.setOnClickListener {
                        val dialog = FullscreenImageDialog(imagenUrl.toString())
                        dialog.show(this.supportFragmentManager, "fullscreenImage")
                    }
                } catch (e: Exception) {
                    println(e)
                }

                binding.redes.setOnClickListener {
                    dialog = BottomSheetDialog(this)
                    bottom_shetRedes(
                        web,
                        fb,
                        ig,
                        tk,
                        tipoTienda,
                        fecha_activacion,
                        fechaVencimiento,
                        ubicacionBoolena,
                        ubicaciontxt
                    )
                    dialog.show()
                }
            }
        }
    }

    fun openWebsite(context: Context, url: String) {
        val uri = Uri.parse(url)
        val intent = Intent(Intent.ACTION_VIEW, uri)
        context.startActivity(intent)
    }

    private fun obtenerDatos_Avanzado_Premiun(docuemnto1: String, documento2: String) {
        val refefencia = FirebaseStorage.getInstance()
            .getReference(Variables.anuncios)
            .child(docuemnto1)
            .child(documento2)
            .child(Variables.Imagenes_publicitarias)

        val lista = mutableListOf<CarouselItem>()

        refefencia.listAll().addOnSuccessListener { listResult ->
            val items = listResult.items
            var cargadas = 0

            items.forEach { item ->
                item.downloadUrl.addOnSuccessListener { uri ->
                    lista.add(CarouselItem(uri.toString()))
                    cargadas++

                    // Solo setea el carrusel una vez, cuando todas las imágenes estén listas
                    if (cargadas == items.size) {
                        binding.carrusel.setData(lista)

                        binding.carrusel.carouselListener = object : CarouselListener {
                            override fun onClick(position: Int, carouselItem: CarouselItem) {
                                val imageUrl = carouselItem.imageUrl ?: return
                                val dialog = FullscreenImageDialog(imageUrl)
                                dialog.show(this@vistas_anuncios_general.supportFragmentManager, "fullscreenImage")
                            }
                        }
                    }
                }.addOnFailureListener { it.printStackTrace() }
            }
        }.addOnFailureListener { it.printStackTrace() }
    }


    private fun mostrarDatos() {
        binding.loading.isVisible = false
        binding.scrollContainer.isVisible = true
    }

    fun createAndShareDynamicLink(
        context: Context,
    ) {
        val documento = intent.getStringExtra(Variables.documentoAdapterPromo).toString()
        val anuncio = intent.getStringExtra(Variables.anuncio).toString()
        val db = FirebaseFirestore.getInstance().collection(Variables.anuncios).document(documento)
            .collection(Variables.anuncios)
            .document(anuncio)


        db.get().addOnSuccessListener { res ->
            if (res.exists()) {
                val data = res.data
                val imagenUrl = data?.get(Variables.imagenUrl) as? String
                Firebase.dynamicLinks.shortLinkAsync {
                    link =
                        Uri.parse("https://geinzapp.page.link/?idDocumento=${documento}&idAnuncio=${anuncio}")
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
                        title = binding.TiendaPropietaria.text.toString()
                        description = binding.titulo.text.toString()
                        imageUrl = Uri.parse(imagenUrl)
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
                    constantesPublicidad.agregarCantidadClickAnuncios(
                        db,
                        anuncio,
                        "compartidas"
                    )
                }.addOnFailureListener {
                    println("Hubo un error con los links dinámicos: $it")
                }
            }


        }
    }

    private fun obtenerImagenesFirestorage(documento: String, anuncio: String) {
        val db = FirebaseFirestore.getInstance().collection(Variables.anuncios).document(documento)
            .collection(Variables.anuncios)
        val lista = mutableListOf<CarouselItem>()
        val listaIds = mutableListOf<String>()
        val listaDisponibles = mutableListOf<Boolean>()

        db.get().addOnSuccessListener { res ->
            for (datos in res) {
                val data = datos.data
                val URLimg = data?.get(Variables.imagenUrl) as? String ?: ""
                val idPubli = data?.get("id") as? String ?: ""
                val disponibleAN = data?.get("disponibleAN") as? Boolean ?: false

                if (idPubli != anuncio) {
                    val carouselItem = CarouselItem(URLimg)
                    lista.add(carouselItem) // Agrega el item a la lista del carrusel
                    listaIds.add(idPubli)   // Agrega el id a la lista separada de ids
                    listaDisponibles.add(disponibleAN) // Agrega el estado de disponibilidad
                }
            }

            binding.masPromocionesGeinz.registerLifecycle(lifecycle)
            binding.masPromocionesGeinz.carouselListener = object : CarouselListener {
                override fun onCreateViewHolder(
                    layoutInflater: LayoutInflater,
                    parent: ViewGroup,
                ): ViewBinding? {
                    return ItemMasPublicidadesGeinzBinding.inflate(
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
                    val currentBinding = binding as ItemMasPublicidadesGeinzBinding
                    val currentItem = lista[position]
                    val currentDisponible = listaDisponibles[position] // Obtén el valor dinámico

                    Glide.with(this@vistas_anuncios_general)
                        .load(currentItem.imageUrl)
                        .into(currentBinding.imgPublicidad)

                    currentBinding.realtiveClikc.setOnClickListener {
                        val vista = when (currentDisponible) {
                            true -> {
                                Intent(this@vistas_anuncios_general, Crea_tu_publicidad::class.java)
                            }

                            false -> {
                                agregarCantidadClickAnuncios(
                                    db.document(listaIds[position]),
                                    anuncio,
                                    "click"
                                )
                                Intent(
                                    this@vistas_anuncios_general,
                                    vistas_anuncios_general::class.java
                                ).apply {
                                    putExtra(Variables.documentoAdapterPromo, documento)
                                    putExtra(Variables.anuncio, listaIds[position])
                                }
                            }
                        }

                        vista?.let {
                            startActivity(it)
                            finish()
                        }
                    }
                }
            }

            binding.masPromocionesGeinz.setData(lista)

        }.addOnFailureListener { e ->
            println("Error al obtener los datos: $e")
        }
    }


}