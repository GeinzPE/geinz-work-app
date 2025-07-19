package com.geinzz.geinzwork.servicios_geinz

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import com.bumptech.glide.Glide
import com.geinzz.geinzwork.utils.constantes.constantes.Variables
import com.geinzz.geinzwork.R
import com.geinzz.geinzwork.utils.constantes.constantes.constantes
import com.geinzz.geinzwork.databinding.ActivityNoticiasServiciosGeinzBinding
import com.geinzz.geinzwork.databinding.BottomSheetInformacionPublicidadNoticiasServiciosGeinzBinding
import com.geinzz.geinzwork.problemas_soporte_politicas.verificacion_cuenta_trabajador
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference

class noticias_servicios_geinz_activity : AppCompatActivity() {
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var binding: ActivityNoticiasServiciosGeinzBinding
    private var clikeable_Bool: Boolean? = null
    private var reserva: Boolean? = null
    private var compra: Boolean? = null
    private var descuentoBool: Boolean? = null
    private var tipo_tienda_Boll: Boolean? = null
    private var ubicacion_Bool: Boolean? = null
    private var precio_Bool: Boolean? = null
    private var efectivo: Boolean = false
    private var plin: Boolean = false
    private var yape: Boolean = false
    private var uri_yape: Uri? = null
    private var uri_pli: Uri? = null
    private lateinit var dialog: BottomSheetDialog
    private var metodCompra: Boolean? = null
    private var isCamposVisible = false
    private var isCampos2Visible = false
    private var isCampos3Visible = false
    private var isCampos4Visible = false
    val yape_pick = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) {
            uri_yape = uri
            binding.qrYape.setImageURI(uri)
        } else {
            println("Imagen no seleccionada")
        }
    }
    val plin_pick = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) {
            uri_pli = uri
            binding.qrPlin.setImageURI(uri)
        } else {
            println("Imagen no seleccionada")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNoticiasServiciosGeinzBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        firebaseAuth = FirebaseAuth.getInstance()
        confSwipe()
        obtenerNoticia(firebaseAuth.uid.toString())
        setearRadiobutons()
        val DialogMessage = verificacion_cuenta_trabajador()
        binding.yapeCheckbox.setOnCheckedChangeListener { _, _ -> actualizarVisibilidad() }
        binding.plinCheckbox.setOnCheckedChangeListener { _, _ -> actualizarVisibilidad() }
        binding.efectivoCheckbox.setOnCheckedChangeListener { _, _ -> actualizarVisibilidad() }
        binding.imgPublicidad.setOnClickListener {
            val vista =
                Intent(this, cambiar_imagenes_publicidad_noticias_servicios::class.java).apply {
                    putExtra("tipo_peticion", "noticia")
                    putExtra("idNoticia", binding.idPublicidad.text.toString())
                }
            startActivity(vista)
        }

        listaFiltrado(binding.localidadDisponible)
        obtenerCategorias(binding.elcionDeCateogoria)
        binding.qrYape.setOnClickListener {
            yape_pick.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }
        binding.qrPlin.setOnClickListener {
            plin_pick.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }

        binding.containerNoticiasCampo.setOnClickListener {
            if (isCamposVisible) {
                binding.campoNoticia.visibility = View.GONE
                binding.ocultarCampoNoticia.setImageResource(R.drawable.ocultar_arriva)
            } else {
                binding.campoNoticia.visibility = View.VISIBLE
                binding.ocultarCampoNoticia.setImageResource(R.drawable.ocultar_abajo)
            }
            isCamposVisible = !isCamposVisible
        }

        binding.campoPagosCompra.setOnClickListener {
            if (isCampos2Visible) {
                binding.campoCompraPagosEtc.visibility = View.GONE
                binding.mostrarOcularCompra.setImageResource(R.drawable.ocultar_arriva)

            } else {
                binding.campoCompraPagosEtc.visibility = View.VISIBLE
                binding.mostrarOcularCompra.setImageResource(R.drawable.ocultar_abajo)
            }
            isCampos2Visible = !isCampos2Visible
        }

        binding.campoContacto.setOnClickListener {
            if (isCampos3Visible) {
                binding.campoContactoLineal.visibility = View.GONE
                binding.mostrarOcularContacto.setImageResource(R.drawable.ocultar_arriva)

            } else {
                binding.campoContactoLineal.visibility = View.VISIBLE
                binding.mostrarOcularContacto.setImageResource(R.drawable.ocultar_abajo)
            }
            isCampos3Visible = !isCampos3Visible
        }
        binding.linealCampoRedes.setOnClickListener {
            if (isCampos4Visible) {
                binding.camposRedes.visibility = View.GONE
                binding.ocultarp3.setImageResource(R.drawable.ocultar_arriva)
            } else {
                binding.camposRedes.visibility = View.VISIBLE
                binding.ocultarp3.setImageResource(R.drawable.ocultar_abajo)
            }
            isCampos4Visible = !isCampos4Visible
        }

        binding.camposContacto.setOnClickListener {
            actualizarCamposContacto(firebaseAuth.uid.toString())
        }

        binding.camposNoticia.setOnClickListener {
            actualiazCamposNoticia(firebaseAuth.uid.toString())
        }

        binding.camposCompraPagos.setOnClickListener {
            actualizarCompra_pagos(firebaseAuth.uid.toString())
        }

        binding.guardarLinksRedes.setOnClickListener {
            actualizarCamposRedes(firebaseAuth.uid.toString())
        }

        binding.infoTikTok.setOnClickListener {
            DialogMessage.mostrarDialogo(
                this.getString(R.string.tiktokTitle),
                this.getString(R.string.tiktokText),
                this
            )
        }
        binding.infoPerfilFacebook.setOnClickListener {
            DialogMessage.mostrarDialogo(
                this.getString(R.string.facebookTitle),
                this.getString(R.string.facebookText),
                this
            )

        }
        binding.infoLinkIg.setOnClickListener {
            DialogMessage.mostrarDialogo(
                this.getString(R.string.instagramTitle),
                this.getString(R.string.instagramText),
                this
            )
        }
        binding.infoLinkSitioWeb.setOnClickListener {
            DialogMessage.mostrarDialogo(
                this.getString(R.string.sitioWebTitle),
                this.getString(R.string.sitioWebText),
                this
            )
        }
    }

    private fun confSwipe() {
        binding.swipe.setOnRefreshListener {
            binding.swipe.setColorSchemeResources(R.color.violeta)
            Handler(Looper.getMainLooper()).postDelayed({
                binding.swipe.isRefreshing = false
                obtenerNoticia(firebaseAuth.uid.toString())
            }, 2000)
        }
    }

    private fun obtenerNoticia(id: String) {
        val db = FirebaseFirestore.getInstance().collection(Variables.solicitudes_serviciosDB)
            .document(Variables.publicidadNoticiasDB).collection(Variables.activos).document(id)
        db.get().addOnSuccessListener { res ->
            if (res.exists()) {
                val data = res.data
                val documento1 = data?.get(Variables.documento1DB) as? String
                val documento2 = data?.get(Variables.documento2DB) as? String
                if (!documento1.isNullOrEmpty() && !documento2.isNullOrEmpty()) {
                    val db2 =
                        FirebaseFirestore.getInstance().collection(documento1!!)
                            .document(documento2!!)
                    db2.get().addOnSuccessListener { res ->
                        if (res.exists()) {
                            val data = res.data
                            val cateogiria = data?.get("Categoria") as? String
                            val Contenido = data?.get("Contenido") as? String
                            val fecha = data?.get("fecha_activacion") as? String ?: ""
                            val id = data?.get("id") as? String
                            val imagenUrl = data?.get("imagenUrl") as? String
                            val propietario = data?.get("propietario") as? String
                            val listener = data?.get("listener") as? Boolean
                            val localidad = data?.get("localidad") as? String
                            val lugaresDisponibles = data?.get("lugaresDisponibles") as? String
                            val metodo_compra_web_Geinz =
                                data?.get("metodo_compra_web_Geinz") as? Boolean
                            val numero = data?.get("numero") as? String
                            val tipo_T = data?.get("tipo_T") as? Boolean
                            val titulo = data?.get("titulo") as? String
                            val precioBoolean = data?.get("precio") as? Boolean
                            val preciosring = data?.get("price") as? String
                            val ubicacionBoolena = data?.get("ubicacionBoolena") as? Boolean
                            val ubicacion = data?.get("ubicacion") as? String
                            val whatsappmsj = data?.get("whatsappmsj") as? String
                            val link_compra = data?.get("link_compra") as? String
                            val qr_yape = data?.get("yape_img") as? String ?: ""
                            val qr_plin = data?.get("plin_img") as? String ?: ""
                            val plin = data?.get("plin") as? Boolean
                            val yape = data?.get("yape") as? Boolean
                            val efectivo = data?.get("efectivo") as? Boolean
                            val reserva = data?.get("reserva") as? Boolean
                            val compra = data?.get("compra") as? Boolean
                            val plan = data?.get("plan") as? String ?: ""
                            val fb = data?.get("fb") as? String ?: ""
                            val tk = data?.get("tk") as? String ?: ""
                            val web = data?.get("web") as? String ?: ""
                            val ig = data?.get("ig") as? String ?: ""
                            val plinVeri = plin ?: false
                            val yapeVeri = yape ?: false
                            val efectivoVeri = efectivo ?: false
                            val descuentoSting = data?.get("descuento") as? String ?: ""
                            val descuento = data?.get("descuento_boolean") as? Boolean ?: false
                            val fechaMap = data?.get("fechas") as? Map<String, Any>
                            val fechaActivacion = fechaMap?.get("fecha_activacion") as? String ?: ""
                            val fechaVencimiento =
                                fechaMap?.get("fecha_vencimiento") as? String ?: ""
                            configurarDialogo(plan, fechaActivacion, fechaVencimiento)

                            if (plan == "basico") {
                                binding.linealCampoRedes.isVisible = false
                            } else if (plan == "avanzado") {
                                binding.linealCampoRedes.isVisible = true
                            }

                            binding.idPublicidad.text = documento2
                            binding.tituloDeLaNoticia.setText(titulo)
                            binding.propietarioED.setText(propietario)
                            binding.contenidoED.setText(Contenido)
                            binding.elcionDeCateogoria.setText(cateogiria)
                            binding.localidadDisponible.setText(localidad)
                            binding.adquirira.text = fecha
                            binding.mensajeED.setText(whatsappmsj)
                            binding.numeroTiendaED.setText(numero)
                            binding.ubicacionTiendaED.setText(ubicacion)
                            binding.precioED.setText(preciosring)
                            binding.descuentoED.setText(descuentoSting)
                            binding.tiktokED.setText(tk)
                            binding.igED.setText(ig)
                            binding.fbED.setText(fb)
                            binding.websiteEd.setText(web)
                            binding.linkDeSitioWeb.setText(link_compra)
                            listaFiltrado(binding.localidadDisponible)
                            obtenerCategorias(binding.elcionDeCateogoria)
                            Glide.with(this)
                                .load(imagenUrl)
                                .placeholder(R.drawable.cargando_img_geinz_500)
                                .error(R.drawable.cargando_img_geinz_500)
                                .into(binding.imgPublicidad)

                            val grupoReserva = binding.grupoReserva
                            val reservaSi = binding.reservaSi
                            val reservaNo = binding.reservaNo
                            val grupoCompra = binding.grupoCompra
                            val compraSi = binding.reservaCompraSi
                            val compraNo = binding.reservaCompraNo
                            val radioListener = binding.grupoClikeable
                            val clikc_si = binding.clikeableSi
                            val clikc_no = binding.clikeableNo
                            val tipoTienda = binding.grupoTipoTienda
                            val virtual = binding.tiendaVirtual
                            val fisica = binding.tiendaFisica
                            val precioGRupo = binding.grupoPrecio
                            val precio_si = binding.precioSi
                            val precio_no = binding.precioNo
                            val radio_ubicacion = binding.grupoUbicacion
                            val ubi_si = binding.ubiSi
                            val ubi_no = binding.ubiNo
                            val tipoMetodoCompra = binding.grupoMetodoCompra
                            val compra_web = binding.compraWeb
                            val grupoDescuento = binding.grupoDescuento
                            val descuento_si = binding.descuentoSi
                            val descuento_no = binding.descuentoNo

                            val compra_Geinz = binding.compraGeinz
                            if (plinVeri && yapeVeri) {
                                binding.plinCheckbox.isChecked = true
                                obtnerImagenes_metodosPago_Principal(
                                    qr_yape,
                                    qr_plin,
                                    documento2,
                                    "plin"
                                )
                                binding.yapeCheckbox.isChecked = true
                                obtnerImagenes_metodosPago_Principal(
                                    qr_yape,
                                    qr_plin,
                                    documento2,
                                    "yape"
                                )
                            } else if (yapeVeri) {
                                binding.yapeCheckbox.isChecked = true
                                obtnerImagenes_metodosPago_Principal(
                                    qr_yape,
                                    qr_plin,
                                    documento2,
                                    "yape"
                                )
                            } else if (plinVeri) {
                                binding.plinCheckbox.isChecked = true
                                obtnerImagenes_metodosPago_Principal(
                                    qr_yape,
                                    qr_plin,
                                    documento2,
                                    "plin"
                                )
                            } else if (efectivoVeri) {
                                binding.efectivoCheckbox.isChecked = true
                            } else {
                                binding.plinCheckbox.isChecked = false
                                binding.efectivoCheckbox.isChecked = false
                                binding.efectivoCheckbox.isChecked = false
                            }
                            when (reserva) {
                                true -> grupoReserva.check(reservaSi.id)
                                false -> grupoReserva.check(reservaNo.id)
                                else -> grupoReserva.clearCheck()
                            }
                            when (compra) {
                                true -> grupoCompra.check(compraSi.id)
                                false -> grupoCompra.check(compraNo.id)
                                else -> grupoCompra.clearCheck()
                            }
                            when (listener) {
                                true -> {
                                    radioListener.check(clikc_si.id)
                                }

                                false -> {
                                    radioListener.check(clikc_no.id)
                                }

                                else -> radioListener.clearCheck()
                            }
                            when (tipo_T) {
                                true -> {
                                    tipoTienda.check(fisica.id)
                                }

                                false -> {
                                    tipoTienda.check(virtual.id)
                                }

                                else -> tipoTienda.clearCheck()
                            }
                            when (precioBoolean) {
                                true -> {
                                    precioGRupo.check(precio_si.id)
                                }

                                false -> {
                                    precioGRupo.check(precio_no.id)
                                }

                                else -> precioGRupo.clearCheck()
                            }
                            when (ubicacionBoolena) {
                                true -> {
                                    radio_ubicacion.check(ubi_si.id)
                                }

                                false -> {
                                    radio_ubicacion.check(ubi_no.id)
                                }

                                else -> radio_ubicacion.clearCheck()
                            }
                            grupoDescuento.post {
                                when (descuento) {
                                    true -> grupoDescuento.check(descuento_si.id)
                                    false -> grupoDescuento.check(descuento_no.id)
                                    else -> grupoDescuento.clearCheck()
                                }
                            }
                            when (metodo_compra_web_Geinz) {
                                true -> {
                                    tipoMetodoCompra.check(compra_web.id)
                                    binding.linkRef.isVisible = true
                                    binding.linealCompraReserva.isVisible = false
                                    binding.linealPrecio.isVisible = false
                                }

                                false -> {
                                    tipoMetodoCompra.check(compra_Geinz.id)
                                    binding.linkRef.isVisible = false
                                }

                                else -> tipoMetodoCompra.clearCheck()
                            }
                        }
                    }
                    constantes.carga(4000) { mostrarDatos(true) }
                } else {
                    constantes.carga(4000) { mostrarDatos(false) }
                    Log.e("Publicidad", "Documentos de anuncios nulos")
                }

            } else {
                constantes.carga(4000) { mostrarDatos(false) }
                Log.e("Publicidad", "Documentos de anuncios nulos")
            }
        }.addOnFailureListener { exception ->
            constantes.carga(4000) { mostrarDatos(false) }
            Log.e("Publicidad", "Error al obtener los datos de publicidad", exception)
        }
    }

    private fun mostrarDatos(servicio: Boolean) {
        if (servicio) {
            binding.scrollView.isVisible = true
            binding.LinealSinServicios.isVisible = false
            binding.loading.isVisible = false
        } else {
            binding.loading.isVisible = false
            binding.LinealSinServicios.isVisible = true
        }

    }

    private fun setearRadiobutons() {
        binding.grupoReserva.setOnCheckedChangeListener { grupo, checkId ->
            reserva = when (checkId) {
                R.id.reserva_si -> true
                R.id.reserva_no -> false
                else -> null
            }
        }

        binding.grupoCompra.setOnCheckedChangeListener { grupo, checkId ->
            compra = when (checkId) {
                R.id.reserva_compra_si -> true
                R.id.reserva_compra_no -> false
                else -> null
            }
        }

        binding.grupoClikeable.setOnCheckedChangeListener { grupo, chekId ->
            clikeable_Bool = when (chekId) {
                R.id.clikeable_si -> true
                R.id.clikeable_no -> false
                else -> null
            }
        }

        binding.grupoTipoTienda.setOnCheckedChangeListener { grupo, chekId ->
            tipo_tienda_Boll = when (chekId) {
                R.id.tienda_fisica -> true
                R.id.tienda_virtual -> false
                else -> null
            }
        }

        binding.grupoUbicacion.setOnCheckedChangeListener { grupo, chekId ->
            ubicacion_Bool = when (chekId) {
                R.id.ubi_si -> {
                    binding.ubicacionTienda.isVisible = true
                    true
                }

                R.id.ubi_no -> {
                    binding.ubicacionTienda.isVisible = false
                    binding.ubicacionTiendaED.setText("")
                    false
                }

                else -> null
            }
        }
        binding.grupoDescuento.setOnCheckedChangeListener { grupo, checkId ->
            descuentoBool = when (checkId) {
                R.id.descuento_si -> {
                    binding.linealdescuentoTexView.visibility = View.VISIBLE
                    true
                }

                R.id.descuento_no -> {
                    binding.linealdescuentoTexView.visibility = View.GONE
                    binding.descuentoED.setText("")
                    false
                }

                else -> null
            }
        }

        binding.grupoPrecio.setOnCheckedChangeListener { grupo, chekId ->
            precio_Bool = when (chekId) {
                R.id.precio_si -> {
                    binding.precio.isVisible = true
                    binding.LinealMetodoPago.isVisible = true
                    binding.linealDescuento.isVisible = true
                    true
                }

                R.id.precio_no -> {
                    binding.linealDescuento.isVisible = false
                    binding.linealPago.isVisible = false
                    binding.precio.isVisible = false
                    binding.LinealMetodoPago.isVisible = false
                    binding.yapeCheckbox.isChecked = false
                    binding.grupoDescuento.clearCheck()
                    binding.descuentoED.setText("")
                    binding.plinCheckbox.isChecked = false
                    binding.efectivoCheckbox.isChecked = false
                    binding.precioED.setText("")
                    false
                }

                else -> null
            }
        }

        binding.grupoMetodoCompra.setOnCheckedChangeListener { grupo, checkId ->
            metodCompra = when (checkId) {
                R.id.compra_web -> {
                    binding.linealPrecio.isVisible = false
                    binding.yapeCheckbox.isChecked = false
                    binding.efectivoCheckbox.isChecked = false
                    binding.plinCheckbox.isChecked = false
                    binding.grupoPrecio.check(R.id.precio_no)
                    binding.linealCompraReserva.isVisible = false
                    binding.grupoReserva.check(R.id.reserva_no)
                    binding.grupoCompra.check(R.id.reserva_compra_no)
                    binding.linkRef.isVisible = true
                    true
                }

                R.id.compra_Geinz -> {
                    binding.linkDeSitioWeb.setText("")
                    binding.linkRef.isVisible = false
                    binding.linealPrecio.isVisible = true
                    binding.linealCompraReserva.isVisible = true
                    false
                }

                else -> null
            }
        }
    }

    private fun listaFiltrado(autoCompleteTextView: AutoCompleteTextView) {
        val lista =
            listOf(Variables.Barranca, Variables.Supe, Variables.Paramonga, Variables.Paramonga)
        val adapter = ArrayAdapter(
            autoCompleteTextView.context,
            android.R.layout.simple_dropdown_item_1line,
            lista
        )
        autoCompleteTextView.setAdapter(adapter)
    }

    private fun obtenerCategorias(autoCompleteCategory: AutoCompleteTextView) {
        val categoriasTiendas = mutableListOf<String>()
        val db =
            FirebaseFirestore.getInstance().collection(Variables.categoriasDB)
                .document(Variables.categoriasTiendaDB)

        db.get()
            .addOnSuccessListener { res ->
                if (res.exists()) {
                    val categorias = res[Variables.categoriasDB] as? ArrayList<String>
                    categorias?.let {
                        categoriasTiendas.addAll(it)
                        val adapter = ArrayAdapter(
                            autoCompleteCategory.context,
                            android.R.layout.simple_dropdown_item_1line,
                            categoriasTiendas
                        )
                        autoCompleteCategory.setAdapter(adapter)
                    }
                } else {
                    println("El documento de categorías no existe.")
                }
            }
            .addOnFailureListener { exception ->
                println("Error al obtener categorías: ${exception.message}")
            }
    }

    private fun actualizarVisibilidad() {
        val yapeCheckbox = binding.yapeCheckbox
        val plinCheckbox = binding.plinCheckbox
        val checkBoxEfectivo = binding.efectivoCheckbox
        yape = false
        plin = false
        efectivo = false

        when {
            yapeCheckbox.isChecked && plinCheckbox.isChecked && checkBoxEfectivo.isChecked -> {
                binding.linealPago.isVisible = true
                binding.linealYape.isVisible = true
                binding.linealPlin.isVisible = true
                yape = true
                plin = true
                efectivo = true
            }

            yapeCheckbox.isChecked && plinCheckbox.isChecked -> {
                binding.linealPago.isVisible = true
                binding.linealYape.isVisible = true
                binding.linealPlin.isVisible = true
                yape = true
                plin = true
            }

            yapeCheckbox.isChecked && checkBoxEfectivo.isChecked -> {
                binding.linealPago.isVisible = true
                binding.linealYape.isVisible = true
                binding.linealPlin.isVisible = false
                yape = true
                efectivo = true
            }

            plinCheckbox.isChecked && checkBoxEfectivo.isChecked -> {
                binding.linealPago.isVisible = true
                binding.linealYape.isVisible = false
                binding.linealPlin.isVisible = true
                plin = true
                efectivo = true
            }

            yapeCheckbox.isChecked -> {
                binding.linealPago.isVisible = true
                binding.linealYape.isVisible = true
                binding.linealPlin.isVisible = false
                yape = true
            }

            plinCheckbox.isChecked -> {
                binding.linealPago.isVisible = true
                binding.linealYape.isVisible = false
                binding.linealPlin.isVisible = true
                plin = true
            }

            checkBoxEfectivo.isChecked -> {
                binding.linealPago.isVisible = false
                binding.linealYape.isVisible = false
                binding.linealPlin.isVisible = false
                efectivo = true
            }

            else -> {
                binding.linealPago.isVisible = false
                binding.linealYape.isVisible = false
                binding.linealPlin.isVisible = false
            }
        }

    }

    private fun configurarDialogo(plan: String, fecha: String, vencimiento: String) {
        val tipo = "Noticias Geinz"
        binding.vencimiento.text = vencimiento
        binding.botomShetPublicacion.setOnClickListener {
            dialog = BottomSheetDialog(this)
            BottomShet_info_publicacion(
                binding.idPublicidad.text.toString(),
                plan,
                fecha,
                vencimiento,
                tipo
            )
            dialog.show()
        }
    }

    private fun agragarImg_plin_yape_storage(id: String) {
        val db = FirebaseFirestore.getInstance().collection(Variables.solicitudes_serviciosDB)
            .document(Variables.publicidadNoticiasDB).collection(Variables.activos).document(id)
        db.get().addOnSuccessListener { res ->
            if (res.exists()) {
                val data = res.data
                val documento1 = data?.get(Variables.documento2DB) as? String
                val storageReference: StorageReference =
                    FirebaseStorage.getInstance().reference.child(Variables.noticiasImagenesDB)
                        .child(documento1!!).child(Variables.qrPAgos)

                val checkboxYape = binding.yapeCheckbox
                val checkboxPlin = binding.plinCheckbox
                if (checkboxYape.isChecked && uri_yape != null && checkboxPlin.isChecked && uri_pli != null) {
                    uploadBothImages(documento1, uri_yape!!, uri_pli!!)
                } else {
                    if (checkboxYape.isChecked && uri_yape != null) {
                        val yapeRef = storageReference.child(Variables.yapeQRDB)
                        uploadQrImage(
                            documento1,
                            yapeRef,
                            uri_yape!!,
                            "Yape",
                            "Error al subir la imagen de Yape"
                        ) { yapeUrl ->
                        }
                    }

                    if (checkboxPlin.isChecked && uri_pli != null) {
                        val plinRef = storageReference.child(Variables.plinQRDB)
                        uploadQrImage(
                            documento1,
                            plinRef,
                            uri_pli!!,
                            "Plin",
                            "Error al subir la imagen de Plin"
                        ) { plinUrl ->
                        }
                    }
                }
            }
        }
    }

    fun uploadBothImages(
        documento1: String,
        yapeImageUri: Uri,
        plinImageUri: Uri,
    ) {
        val storageReference: StorageReference =
            FirebaseStorage.getInstance().reference.child(Variables.noticiasImagenesDB)
                .child(documento1).child(Variables.qrPAgos)
        val yapeRef = storageReference.child(Variables.yapeQRDB)
        val plinRef = storageReference.child(Variables.plinQRDB)
        uploadQrImage(
            documento1,

            yapeRef,
            yapeImageUri,
            "Yape",
            "Error al subir la imagen de Yape"
        ) { yapeUrl ->
            uploadQrImage(
                documento1,
                plinRef,
                plinImageUri,
                "Plin",
                "Error al subir la imagen de Plin"
            ) { plinUrl ->
            }
        }
    }

    fun uploadQrImage(
        documento1: String,
        reference: StorageReference,
        imageUri: Uri,
        successMessage: String,
        failureMessage: String,
        onSuccess: (String) -> Unit,
    ) {
        val dbFirestore =
            FirebaseFirestore.getInstance().collection(Variables.noticiasDB).document(documento1)
        reference.putFile(imageUri)
            .addOnSuccessListener { taskSnapshot ->
                reference.downloadUrl.addOnSuccessListener { uri ->
                    val url = uri.toString()
                    println("$successMessage URL: $url")
                    when (successMessage) {
                        "Yape" -> {
                            val hashMap_yape = hashMapOf<String, Any>("yape_img" to url)
                            dbFirestore.set(hashMap_yape, SetOptions.merge())
                                .addOnSuccessListener {
                                    Toast.makeText(
                                        this,
                                        "Cambiamos correctamente la URL de metodo de pago Yape",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                        }

                        "Plin" -> {
                            val hashMap_plin = hashMapOf<String, Any>("plin_img" to url)
                            dbFirestore.set(hashMap_plin, SetOptions.merge())
                                .addOnSuccessListener {
                                    Toast.makeText(
                                        this,
                                        "Cambiamos correctamente la URL de metodo de pago Plin",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                        }
                    }
                    onSuccess(url)
                }
            }
            .addOnFailureListener { exception ->
                println("$failureMessage: ${exception.message}")
            }
    }

    private fun obtnerImagenes_metodosPago_Principal(
        yape_img: String,
        plin_img: String,
        id: String,
        encontrar: String,
    ) {

        when (encontrar) {
            "yape" -> {
                val imageViewYape = binding.qrYape
                if (yape_img != "") {
                    Glide.with(this).load(yape_img)
                        .placeholder(R.drawable.cargando_qr_geinz)
                        .error(R.drawable.agregar_img_geinz)
                        .into(imageViewYape)
                } else {
                    imageViewYape.setImageResource(R.drawable.agregar_img_geinz)
                    Toast.makeText(
                        this,
                        "No se encontro imagen de QR de yape",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            "plin" -> {
                val imageViewPlin = binding.qrPlin
                if (plin_img != "") {
                    Glide.with(this).load(yape_img)
                        .placeholder(R.drawable.cargando_qr_geinz)
                        .error(R.drawable.agregar_img_geinz)
                        .into(imageViewPlin)
                } else {
                    imageViewPlin.setImageResource(R.drawable.agregar_img_geinz)
                    Toast.makeText(
                        this,
                        "No se encontro imagen de QR de yape",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }

    }

    private fun BottomShet_info_publicacion(
        id_servicio: String,
        plan: String,
        fp: String,
        fv: String,
        tipo_servicio: String,
    ) {
        val bindingBottomShet =
            BottomSheetInformacionPublicidadNoticiasServiciosGeinzBinding.inflate(layoutInflater)
        val view = bindingBottomShet.root

        val idServicio = bindingBottomShet.idServicio
        val plan_servicio = bindingBottomShet.plan
        val publicado = bindingBottomShet.fechaPublicada
        val vencimiento = bindingBottomShet.finaliza
        val tipo_servicio_geinz = bindingBottomShet.tipoServicio
        val copy_id=bindingBottomShet.copyId
        copy_id.setOnClickListener {
            val textoACopiar = bindingBottomShet.idServicio.text.toString()

            val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("texto", textoACopiar)
            clipboard.setPrimaryClip(clip)

            Toast.makeText(this, "Texto copiado al portapapeles", Toast.LENGTH_SHORT).show()
        }
        if (id_servicio != "") {
            idServicio.text = id_servicio
        } else {
            bindingBottomShet.linealId.isVisible = false
        }
        idServicio.text = id_servicio
        plan_servicio.text = plan
        publicado.text = fp
        vencimiento.text = fv
        tipo_servicio_geinz.text = tipo_servicio

        dialog.setContentView(view)
    }

    private fun actualiazCamposNoticia(id: String) {
        val publicacionService = publicidad_servicios_geinz_activity()
        val titulo = binding.tituloDeLaNoticia.text.toString()
        val propietario = binding.propietarioED.text.toString()
        val contenido_noticia = binding.contenidoED.text.toString()
        val categoria_noticia = binding.elcionDeCateogoria.text.toString()
        val localidad_disponible = binding.localidadDisponible.text.toString()
        val ubicacion_tienda = binding.ubicacionTiendaED.text.toString()
        val db = FirebaseFirestore.getInstance().collection(Variables.solicitudes_serviciosDB)
            .document(Variables.publicidadNoticiasDB).collection(Variables.activos).document(id)
        db.get().addOnSuccessListener { res ->
            if (res.exists()) {
                val data = res.data
                val documento1 = data?.get(Variables.documento1DB) as? String
                val documento2 = data?.get(Variables.documento2DB) as? String
                val db2 =
                    FirebaseFirestore.getInstance().collection(documento1!!).document(documento2!!)
                db2.get().addOnSuccessListener {
                    val hasmap = hashMapOf<String, Any>(
                        "titulo" to titulo,
                        "propietario" to propietario,
                        "Categoria" to categoria_noticia,
                        "Contenido" to contenido_noticia,
                        "localidad" to localidad_disponible,
                        "lugaresDisponibles" to localidad_disponible,
                        "ubicacion" to ubicacion_tienda,
                        "tipo_T" to (tipo_tienda_Boll ?: false),
                        "ubicacionBoolena" to (ubicacion_Bool ?: false)
                    )
                    db2.set(hasmap, SetOptions.merge()).addOnSuccessListener {
                        publicacionService.ToastMessage(
                            "Campos de la noticia actualizados correctamente",
                            this
                        )
                    }.addOnFailureListener {
                        publicacionService.ToastMessage(
                            "Error al actualizar los campos de la noticia",
                            this
                        )
                    }
                }
            }
        }
    }

    private fun actualizarCompra_pagos(id: String) {
        val publicacionService = publicidad_servicios_geinz_activity()
        val precio_txt = binding.precioED.text.toString()
        val link_compra = binding.linkDeSitioWeb.text.toString()
        val descuentoTexto = binding.descuentoED.text.toString()
        val descunetosi = binding.descuentoSi.isChecked
        val precioInt = precio_txt.toIntOrNull()
        val descuento = descuentoTexto.toIntOrNull()

        if (precioInt == null || (descunetosi && descuento == null)) {
            binding.descuentoED.error = "Por favor, ingrese valores numéricos válidos"
            return
        }

        if (descunetosi && descuento!! >= precioInt) {
            binding.descuentoED.error = "El descuento no puede ser igual o mayor al precio original"
            return
        }
        val nuevoDescuento = if (descunetosi) descuentoTexto else ""
        val porcentajeDescuento = if (descunetosi) {
            ((precioInt - descuento!!).toDouble() / precioInt * 100).toInt()
        } else {
            0
        }


        val db = FirebaseFirestore.getInstance().collection(Variables.solicitudes_serviciosDB)
            .document(Variables.publicidadNoticiasDB).collection(Variables.activos).document(id)
        db.get().addOnSuccessListener { res ->
            if (res.exists()) {
                val data = res.data
                val documento1 = data?.get(Variables.documento1DB) as? String
                val documento2 = data?.get(Variables.documento2DB) as? String
                val db2 =
                    FirebaseFirestore.getInstance().collection(documento1!!).document(documento2!!)
                db2.get().addOnSuccessListener {
                    val hasmap = hashMapOf<String, Any>(
                        "precio" to (precio_Bool ?: false),
                        "price" to precio_txt,
                        "listener" to (clikeable_Bool ?: false),
                        "reserva" to (reserva ?: false),
                        "compra" to (compra ?: false),
                        "efectivo" to efectivo,
                        "yape" to yape,
                        "plin" to plin,
                        "descuento_boolean" to (descuentoBool ?: false),
                        "link_compra" to link_compra,
                        "metodo_compra_web_Geinz" to (metodCompra ?: false),
                        "descuento" to nuevoDescuento,
                        "porcentajeDescuento" to porcentajeDescuento
                    )
                    db2.set(hasmap, SetOptions.merge()).addOnSuccessListener {
                        agragarImg_plin_yape_storage(firebaseAuth.uid.toString())
                        publicacionService.ToastMessage(
                            "Campos de compra.pagos,etc actualizados correctamente",
                            this
                        )
                    }.addOnFailureListener {
                        publicacionService.ToastMessage(
                            "Error al actualizar los campos de compra.pagos,etc",
                            this
                        )
                    }
                }
            }
        }
    }

    private fun actualizarCamposContacto(id: String) {
        val publicacionService = publicidad_servicios_geinz_activity()
        val fecha_venciminento = binding.vencimiento.text.toString()
        val fecha_inicial = binding.adquirira.text.toString()
        val numero_whatsapp = binding.numeroTiendaED.text.toString()
        val msj_whatsapp = binding.mensajeED.text.toString()
        val db = FirebaseFirestore.getInstance().collection(Variables.solicitudes_serviciosDB)
            .document(Variables.publicidadNoticiasDB).collection(Variables.activos).document(id)
        db.get().addOnSuccessListener { res ->
            if (res.exists()) {
                val data = res.data
                val documento1 = data?.get(Variables.documento1DB) as? String
                val documento2 = data?.get(Variables.documento2DB) as? String
                val db2 =
                    FirebaseFirestore.getInstance().collection(documento1!!).document(documento2!!)
                db2.get().addOnSuccessListener {
                    val hashMap = hashMapOf<String, Any>(
//                        "fecha" to fecha_inicial,
                        "numero" to numero_whatsapp,
//                        "vencimineto" to fecha_venciminento,
                        "whatsappmsj" to msj_whatsapp,
                    )
                    db2.set(hashMap, SetOptions.merge()).addOnSuccessListener {
                        publicacionService.ToastMessage(
                            "Campos de contacto actualizados correctamente",
                            this
                        )
                    }.addOnFailureListener {
                        publicacionService.ToastMessage(
                            "Error al actualizar los campos de contacto",
                            this
                        )
                    }
                }
            }
        }
    }

    private fun actualizarCamposRedes(id: String) {
        val publicacionService = publicidad_servicios_geinz_activity()
        val fb = binding.fbED.text.toString()
        val tk = binding.tiktokED.text.toString()
        val web = binding.websiteEd.text.toString()
        val ig = binding.igED.text.toString()
        val db = FirebaseFirestore.getInstance().collection(Variables.solicitudes_serviciosDB)
            .document(Variables.publicidadNoticiasDB).collection(Variables.activos).document(id)
        db.get().addOnSuccessListener { res ->
            if (res.exists()) {
                val data = res.data
                val documento1 = data?.get("documento1") as? String
                val documento2 = data?.get("documento2") as? String

                if (!documento1.isNullOrEmpty() && !documento2.isNullOrEmpty()) {
                    val db2 =
                        FirebaseFirestore.getInstance().collection(documento1).document(documento2)
                    val hasmap = hashMapOf<String, Any>(
                        "fb" to fb,
                        "tk" to tk,
                        "web" to web,
                        "ig" to ig,
                    )
                    db2.set(hasmap, SetOptions.merge()).addOnSuccessListener {
                        publicacionService.ToastMessage(
                            "Campos de redes actualizados correctamente",
                            this
                        )
                    }
                        .addOnFailureListener {
                            publicacionService.ToastMessage(
                                "Error al actualizar los campos de redes",
                                this
                            )
                        }
                }
            }
        }
    }


}