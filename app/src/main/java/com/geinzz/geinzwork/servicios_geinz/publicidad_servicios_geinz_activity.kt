package com.geinzz.geinzwork.servicios_geinz

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import com.bumptech.glide.Glide
import com.geinzz.geinzwork.utils.constantes.constantes.constanterAutcompleteTexviewGeneral
import com.geinzz.geinzwork.R
import com.geinzz.geinzwork.utils.constantes.constantes.constantes
import com.geinzz.geinzwork.databinding.ActivityPublicidadServiciosGeinzBinding
import com.geinzz.geinzwork.databinding.BottomSheetInformacionPublicidadNoticiasServiciosGeinzBinding
import com.geinzz.geinzwork.problemas_soporte_politicas.verificacion_cuenta_trabajador
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.util.Timer

class publicidad_servicios_geinz_activity : AppCompatActivity() {
    private lateinit var binding: ActivityPublicidadServiciosGeinzBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private var reserva: Boolean? = null
    private var descuentoBool: Boolean? = null
    private var compra: Boolean? = null
    private var ubi: Boolean? = null
    private var precioBoolen: Boolean? = null
    private var tipoTienda: Boolean? = null
    private var efectivo: Boolean = false
    private var plin: Boolean = false
    private var yape: Boolean = false
    private var metodCompra: Boolean? = null
    private var yape_qr_img: Uri? = null
    private var plin_qr_img: Uri? = null
    private var isCamposVisible = false
    private var isCampos2Visible = false
    private var isCampos3Visible = false
    private var isCampos4Visible = false
    private lateinit var dialog: BottomSheetDialog
    private var timer: Timer? = null
    val pciMEdiaYape =
        registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
            if (uri != null) {
                yape_qr_img = uri
                binding.qrYape.setImageURI(uri)
            } else {
                println("Imagen no seleccionada")
            }
        }
    val pciMEdiaPLin =
        registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
            if (uri != null) {
                plin_qr_img = uri
                binding.qrPlin.setImageURI(uri)
            } else {
                println("Imagen no seleccionada")
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPublicidadServiciosGeinzBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        firebaseAuth = FirebaseAuth.getInstance()
        obtenerDatosPublicidad(firebaseAuth.uid.toString())
        confSwipe()
        val DialogMessage = verificacion_cuenta_trabajador()
        binding.imgPublicidad.setOnClickListener {
            val vista =
                Intent(this, cambiar_imagenes_publicidad_noticias_servicios::class.java).apply {
                    putExtra("tipo_peticion", "publicidad")
                    putExtra("plan", binding.plan.text.toString())
                    putExtra("documento1", binding.documento1.text.toString())
                    putExtra("documento2", binding.documento2.text.toString())
                }
            startActivity(vista)
        }
        binding.yapeCheckbox.setOnCheckedChangeListener { _, _ -> actualizarVisibilidad() }
        binding.plinCheckbox.setOnCheckedChangeListener { _, _ -> actualizarVisibilidad() }
        binding.efectivoCheckbox.setOnCheckedChangeListener { _, _ -> actualizarVisibilidad() }
        setupRadioButtons()

        binding.qrYape.setOnClickListener {
            pciMEdiaYape.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }
        binding.qrPlin.setOnClickListener {
            pciMEdiaPLin.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }
        binding.ocultarCamposDePublicidad.setOnClickListener {
            if (isCamposVisible) {
                binding.camposOculatar.visibility = View.GONE
                binding.ocultarP1.setImageResource(R.drawable.ocultar_arriva)

            } else {
                binding.camposOculatar.visibility = View.VISIBLE
                binding.ocultarP1.setImageResource(R.drawable.ocultar_abajo)
            }
            isCamposVisible = !isCamposVisible
        }
        binding.ocultarCamposPagos.setOnClickListener {
            if (isCampos2Visible) {

                binding.camposPagos.visibility = View.GONE
                binding.ocultarp2.setImageResource(R.drawable.ocultar_arriva)

            } else {

                binding.camposPagos.visibility = View.VISIBLE
                binding.ocultarp2.setImageResource(R.drawable.ocultar_abajo)
            }

            isCampos2Visible = !isCampos2Visible
        }


        binding.categoriaED.setOnClickListener {
            constanterAutcompleteTexviewGeneral.obtenerCategoriasPublicidades(binding.categoriaED)
        }

        binding.categoriaED.setOnItemClickListener { parent, _, position, _ ->
            val selectedCategory = parent.getItemAtPosition(position).toString()
            binding.subcategoriED.setText("")
            constanterAutcompleteTexviewGeneral.obtenerSubcategoriasPublicidades(
                selectedCategory,
                binding.subcategoriED
            )
        }

        binding.linealCampoRedes.setOnClickListener {
            if (isCampos3Visible) {
                binding.camposRedes.visibility = View.GONE
                binding.ocultarp3.setImageResource(R.drawable.ocultar_arriva)

            } else {

                binding.camposRedes.visibility = View.VISIBLE
                binding.ocultarp3.setImageResource(R.drawable.ocultar_abajo)
            }
            // Actualiza el estado
            isCampos3Visible = !isCampos3Visible
        }
        binding.campoContacto.setOnClickListener {
            if (isCampos4Visible) {
                binding.campoContactoLineal.visibility = View.GONE
                binding.mostrarOcularContacto.setImageResource(R.drawable.ocultar_arriva)

            } else {
                binding.campoContactoLineal.visibility = View.VISIBLE
                binding.mostrarOcularContacto.setImageResource(R.drawable.ocultar_abajo)
            }
            isCampos4Visible = !isCampos4Visible
        }
        binding.guardarCamposPublicidad.setOnClickListener {
            actualizarCamposPublicidad(firebaseAuth.uid.toString())
        }
        binding.guardarMetodoPago.setOnClickListener {
            actualizarCamposCompra_pago(firebaseAuth.uid.toString())
        }
        binding.guardarContacto.setOnClickListener {
            actualizarCamposContacto(firebaseAuth.uid.toString())
        }
        binding.guardarLinksRedes.setOnClickListener {
            actualizarCamposRedes(firebaseAuth.uid.toString())
        }

        binding.nombreTiendaImgaView.setOnClickListener {
            DialogMessage.mostrarDialogo(
                this.getString(R.string.nombretiendaTitle),
                this.getString(R.string.nombreTiendaText),
                this
            )
        }
        binding.infoTituloPublicacion.setOnClickListener {
            DialogMessage.mostrarDialogo(
                this.getString(R.string.tituloPublicacionTitle),
                this.getString(R.string.tituloPublicacionText),
                this
            )
        }
        binding.infoDescripcionPublicidad.setOnClickListener {
            DialogMessage.mostrarDialogo(
                this.getString(R.string.descripcionPublicidadTitle),
                this.getString(R.string.descripcionPublicidadText),
                this
            )
        }
        binding.ubicacionTiendaImageview.setOnClickListener {
            DialogMessage.mostrarDialogo(
                this.getString(R.string.ubicacionTiendaTitle),
                this.getString(R.string.ubicacionTiendaText),
                this
            )
        }
        binding.infoDescripcionServicios.setOnClickListener {
            DialogMessage.mostrarDialogo(
                this.getString(R.string.precioArticuloTitle),
                this.getString(R.string.precioArticuloText),
                this
            )
        }

        binding.infoDescripcionServiciosDescuento.setOnClickListener {
            DialogMessage.mostrarDialogo(
                this.getString(R.string.descuentoArticuloTitle),
                this.getString(R.string.descuentoArticuloText),
                this
            )
        }
        binding.numeroTelfTienda.setOnClickListener {
            DialogMessage.mostrarDialogo(
                this.getString(R.string.contactoTiendaTitle),
                this.getString(R.string.contactoTiendaText),
                this
            )
        }
        binding.mensajewhatsappInfo.setOnClickListener {
            DialogMessage.mostrarDialogo(
                this.getString(R.string.mensajeWhatsappTitle),
                this.getString(R.string.mensajeWhatsappText),
                this
            )
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
                obtenerDatosPublicidad(firebaseAuth.uid.toString())
            }, 2000)
        }
    }

    private fun obtenerDatosPublicidad(id: String) {
        val db = FirebaseFirestore.getInstance()
            .collection("solicitudes_servicios")
            .document("publicidad_baner")
            .collection("activos")
            .document(id)

        db.get().addOnSuccessListener { res ->
            if (res.exists()) {
                val data = res.data
                val documento1 = data?.get("documento1") as? String
                val documento2 = data?.get("documento2") as? String
                val planActivos = data?.get("plan") as? String
                binding.plan.text = planActivos
                binding.documento1.text = documento1
                binding.documento2.text = documento2

                if (!documento1.isNullOrEmpty() && !documento2.isNullOrEmpty() && !planActivos.isNullOrEmpty()) {

                    when (planActivos) {
                        "basico" -> {
                            binding.linealCampoRedes.isVisible = false
                        }

                        "avanzado" -> {
                            binding.linealCampoRedes.isVisible = true
                        }

                        "premiun" -> {
                            binding.linealCampoRedes.isVisible = true
                        }
                    }
                    val dbAnuncios = FirebaseFirestore.getInstance()
                        .collection("anuncios")
                        .document(documento1)
                        .collection("anuncios")
                        .document(documento2)

                    dbAnuncios.get().addOnSuccessListener { resAnuncios ->
                        if (resAnuncios.exists()) {
                            val dataAnuncios = resAnuncios.data
                            val compra = dataAnuncios?.get("compra") as? Boolean ?: false
                            val efectivo = dataAnuncios?.get("efectivo") as? Boolean ?: false
                            val reserva = dataAnuncios?.get("reserva") as? Boolean ?: false
                            val yape = dataAnuncios?.get("yape") as? Boolean ?: false
                            val plin = dataAnuncios?.get("plin") as? Boolean ?: false
                            val ubicacion = dataAnuncios?.get("ubicacion") as? Boolean ?: false
                            val precio_boolean =
                                dataAnuncios?.get("precio_boolean") as? Boolean ?: false
                            val descuento =
                                dataAnuncios?.get("descuento_boolean") as? Boolean ?: false
                            val descuentoSting = dataAnuncios?.get("descuento") as? String ?: ""
                            val tipoTiendaBoolean =
                                dataAnuncios?.get("tipoTienda") as? Boolean ?: false
                            val metodo_compra_web_Geinz =
                                dataAnuncios?.get("metodo_compra_web_Geinz") as? Boolean ?: false
                            val ubitxt = dataAnuncios?.get("ubicacion_tienda") as? String ?: ""
                            val nombreTienda = dataAnuncios?.get("nombreTienda") as? String ?: ""
                            val titulo = dataAnuncios?.get("titulo") as? String ?: ""
                            val descripcion = dataAnuncios?.get("descripcion") as? String ?: ""
                            val numero = dataAnuncios?.get("numero") as? String ?: ""
                            val mensaje = dataAnuncios?.get("mensaje") as? String ?: ""
                            val fb = dataAnuncios?.get("fb") as? String ?: ""
                            val tk = dataAnuncios?.get("tk") as? String ?: ""
                            val web = dataAnuncios?.get("web") as? String ?: ""
                            val ig = dataAnuncios?.get("ig") as? String ?: ""
                            val precio = dataAnuncios?.get("precio") as? String ?: ""
                            val yape_img = dataAnuncios?.get("yape_img") as? String ?: ""
                            val plin_img = dataAnuncios?.get("plin_img") as? String ?: ""
                            val imagenUrl = dataAnuncios?.get("imagenUrl") as? String ?: ""
                            val link_compra = dataAnuncios?.get("link_compra") as? String ?: ""
                            val plan = dataAnuncios?.get("plan") as? String ?: ""
                            val fechaMap = dataAnuncios?.get("fechas") as? Map<String, Any>
                            val fechaActivacion = fechaMap?.get("fecha_activacion") as? String ?: ""
                            val categoria = dataAnuncios?.get("categoria") as? String ?: ""
                            val subcategoria = dataAnuncios?.get("subcategoria") as? String ?: ""
                            val fechaVencimiento =
                                fechaMap?.get("fecha_vencimiento") as? String ?: ""
                            val tipo = "Publicidad Geinz"

                            bottomShetDialog(fechaActivacion, fechaVencimiento, tipo)


                            val grupoReserva = binding.grupoReserva
                            val reservaSi = binding.reservaSi
                            val reservaNo = binding.reservaNo

                            val grupoCompra = binding.grupoCompra
                            val compraSi = binding.reservaCompraSi
                            val compraNo = binding.reservaCompraNo

                            val grupoUbicacion = binding.grupoUbicacion
                            val ubi_si = binding.ubiSi
                            val ubi_no = binding.ubiNo

                            val grupoprecio = binding.grupoPrecio
                            val precio_si = binding.precioSi
                            val precio_no = binding.precioNo

                            val grupoDescuento = binding.grupoDescuento
                            val descuento_si = binding.descuentoSi
                            val descuento_no = binding.descuentoNo

                            val tipoTienda = binding.grupoTipoTienda
                            val fisica = binding.tiendaFisica
                            val virtual = binding.tiendaVirtual

                            val tipoMetodoCompra = binding.grupoMetodoCompra
                            val compra_web = binding.compraWeb
                            val compra_Geinz = binding.compraGeinz

                            val yapeCheckbox = binding.yapeCheckbox
                            val plinCheckbox = binding.plinCheckbox
                            val efectivoCheckbox = binding.efectivoCheckbox

                            yapeCheckbox.isChecked = yape == true
                            efectivoCheckbox.isChecked = efectivo == true
                            plinCheckbox.isChecked = plin == true
                            if (!yape_img.isNullOrEmpty() || !plin_img.isNullOrEmpty()) {
                                try {
                                    if (!yape_img.isNullOrEmpty()) {
                                        Glide.with(this)
                                            .load(yape_img)
                                            .placeholder(R.drawable.cargando_img_geinz_500)
                                            .error(R.drawable.sin_foto_portada_con_marca)
                                            .into(binding.qrYape)
                                    }


                                    if (!plin_img.isNullOrEmpty()) {
                                        Glide.with(this)
                                            .load(plin_img)
                                            .placeholder(R.drawable.cargando_img_geinz_500)
                                            .error(R.drawable.sin_foto_portada_con_marca)
                                            .into(binding.qrPlin)
                                    }
                                } catch (e: Exception) {
                                    println("Error al setear la imagen: $e")
                                }
                            } else {
                                println("No se encontraron imágenes de métodos de pago")
                            }

                            when {
                                yapeCheckbox.isChecked && plinCheckbox.isChecked -> {
                                    binding.linealPago.isVisible = true
                                    binding.linealYape.isVisible = true
                                    binding.linealPlin.isVisible = true
                                }

                                yapeCheckbox.isChecked -> {
                                    binding.linealPago.isVisible = true
                                    binding.linealYape.isVisible = true
                                    binding.linealPlin.isVisible = false
                                }

                                plinCheckbox.isChecked -> {
                                    binding.linealPago.isVisible = true
                                    binding.linealYape.isVisible = false
                                    binding.linealPlin.isVisible = true
                                }

                                else -> {
                                    binding.linealPago.isVisible = false
                                    binding.linealYape.isVisible = false
                                    binding.linealPlin.isVisible = false
                                }

                            }
                            when (compra) {
                                true -> grupoCompra.check(compraSi.id)
                                false -> grupoCompra.check(compraNo.id)
                                else -> grupoCompra.clearCheck()
                            }
                            when (reserva) {
                                true -> grupoReserva.check(reservaSi.id)
                                false -> grupoReserva.check(reservaNo.id)
                                else -> grupoReserva.clearCheck()
                            }
                            grupoUbicacion.post {
                                when (ubicacion) {
                                    true -> grupoUbicacion.check(ubi_si.id)
                                    false -> grupoUbicacion.check(ubi_no.id)
                                    else -> grupoUbicacion.clearCheck()
                                }
                            }
                            grupoprecio.post {
                                when (precio_boolean) {
                                    true -> grupoprecio.check(precio_si.id)
                                    false -> grupoprecio.check(precio_no.id)
                                    else -> grupoprecio.clearCheck()
                                }
                            }
                            grupoDescuento.post {
                                when (descuento) {
                                    true -> grupoDescuento.check(descuento_si.id)
                                    false -> grupoDescuento.check(descuento_no.id)
                                    else -> grupoDescuento.clearCheck()
                                }
                            }

                            when (precio_boolean) {
                                true -> grupoprecio.check(precio_si.id)
                                false -> grupoUbicacion.check(precio_no.id)
                                else -> grupoUbicacion.clearCheck()
                            }
                            when (tipoTiendaBoolean) {
                                true -> tipoTienda.check(fisica.id)
                                false -> tipoTienda.check(virtual.id)
                                else -> grupoUbicacion.clearCheck()
                            }
                            when (metodo_compra_web_Geinz) {
                                true -> {
                                    tipoMetodoCompra.check(compra_web.id)
                                    binding.campoContacto.isVisible = false
                                }

                                false -> {
                                    tipoMetodoCompra.check(compra_Geinz.id)
                                    binding.campoContacto.isVisible = true
                                    binding.linealPrecio.isVisible = true
                                    binding.linealCompraReserva.isVisible = true
                                }

                                else -> tipoMetodoCompra.clearCheck()
                            }
                            binding.categoriaED.setText(
                                categoria,
                                false
                            )
                            binding.subcategoriED.setText(
                                subcategoria,
                                false
                            )

                            binding.categoriaED.setText(categoria)
                            binding.subcategoriED.setText(subcategoria)
                            binding.tiktokED.setText(tk)
                            binding.precioED.setText(precio)
                            binding.igED.setText(ig)
                            binding.fbED.setText(fb)
                            binding.websiteEd.setText(web)
                            binding.descripcionServiciosED.setText(descripcion)
                            binding.tituloPublicacionED.setText(titulo)
                            binding.numeroTiendaED.setText(numero)
                            binding.mensajeED.setText(mensaje)
                            binding.ubicacionTienda.setText(ubitxt)
                            binding.linkDeSitioWeb.setText(link_compra)
                            binding.descuentoED.setText(descuentoSting)

                            Glide.with(this)
                                .load(imagenUrl)
                                .placeholder(R.drawable.portada_agregar_geinz)
                                .error(R.drawable.portada_agregar_geinz)
                                .into(binding.imgPublicidad)

                            constantes.carga(4000) { mostrarDatos(true) }
                            binding.nombreTiendaED.setText(nombreTienda)
                        } else {
                            constantes.carga(4000) { mostrarDatos(false) }
                            Log.e("Publicidad", "El documento de anuncios no existe")
                        }
                    }.addOnFailureListener { exception ->
                        constantes.carga(4000) { mostrarDatos(false) }
                        Log.e("Publicidad", "Error al obtener los datos de anuncios", exception)
                    }
                } else {
                    constantes.carga(4000) { mostrarDatos(false) }
                    Log.e("Publicidad", "Documentos de anuncios nulos")
                }

            } else {
                constantes.carga(4000) { mostrarDatos(false) }
                Log.e("Publicidad", "El documento de solicitud de publicidad no existe")
            }
        }.addOnFailureListener { exception ->
            constantes.carga(4000) { mostrarDatos(false) }
            Log.e("Publicidad", "Error al obtener los datos de publicidad", exception)
        }
    }

    private fun mostrarDatos(servicio: Boolean) {
        if (servicio == true) {
            binding.scrollView.isVisible = true
            binding.LinealSinServicios.isVisible = false
            binding.loading.isVisible = false
        } else {
            binding.loading.isVisible = false
            binding.LinealSinServicios.isVisible = true
        }

    }

    private fun bottomShetDialog(fecha_text: String, vecimiento_text: String, tipo: String) {
        binding.botomShetPublicacion.setOnClickListener {
            dialog = BottomSheetDialog(this)
            BottomShet_info_publicacion(
                "",
                binding.plan.text.toString(),
                fecha_text,
                vecimiento_text,
                tipo
            )
            dialog.show()
        }
    }

    private fun agregar_img_qr(id: String) {
        val db = FirebaseFirestore.getInstance().collection("solicitudes_servicios")
            .document("publicidad_baner").collection("activos").document(id)
        db.get().addOnSuccessListener { res ->
            if (res.exists()) {
                val data = res.data
                val documento1 = data?.get("documento1") as? String
                val documento2 = data?.get("documento2") as? String
                val storageReference: StorageReference =
                    FirebaseStorage.getInstance().reference.child("anuncios")
                        .child(documento1!!).child(documento2!!).child("qr_pagos")

                // Obtener los checkboxes
                val checkboxYape = binding.yapeCheckbox
                val checkboxPlin = binding.plinCheckbox

                if (checkboxYape.isChecked && yape_qr_img != null && checkboxPlin.isChecked && plin_qr_img != null) {
                    uploadBothImages(documento1, documento2, yape_qr_img!!, plin_qr_img!!)
                } else {
                    if (checkboxYape.isChecked && yape_qr_img != null) {
                        val yapeRef = storageReference.child("yape_qr.jpg")
                        uploadQrImage(
                            documento1,
                            documento2,
                            yapeRef,
                            yape_qr_img!!,
                            "Yape",
                            "Error al subir la imagen de Yape"
                        ) { yapeUrl ->
                        }
                    }

                    if (checkboxPlin.isChecked && plin_qr_img != null) {
                        val plinRef = storageReference.child("plin_qr.jpg")
                        uploadQrImage(
                            documento1,
                            documento2,
                            plinRef,
                            plin_qr_img!!,
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
        documento2: String,
        yapeImageUri: Uri,
        plinImageUri: Uri,
    ) {
        val storageReference: StorageReference =
            FirebaseStorage.getInstance().reference.child("anuncios")
                .child(documento1).child(documento2).child("qr_pagos")
        val yapeRef = storageReference.child("yape_qr.jpg")
        val plinRef = storageReference.child("plin_qr.jpg")
        uploadQrImage(
            documento1,
            documento2,
            yapeRef,
            yapeImageUri,
            "Yape",
            "Error al subir la imagen de Yape"
        ) { yapeUrl ->
            uploadQrImage(
                documento1,
                documento2,
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
        documento2: String,
        reference: StorageReference,
        imageUri: Uri,
        successMessage: String,
        failureMessage: String,
        onSuccess: (String) -> Unit,
    ) {
        val dbFirestore =
            FirebaseFirestore.getInstance().collection("anuncios").document(documento1)
                .collection("anuncios").document(documento2)
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

    private fun setupRadioButtons() {
        binding.grupoReserva.setOnCheckedChangeListener { grupo, checkId ->
            reserva = when (checkId) {
                R.id.reserva_si -> true
                R.id.reserva_no -> false
                else -> null
            }
        }

        binding.grupoDescuento.setOnCheckedChangeListener { grupo, checkId ->
            descuentoBool = when (checkId) {
                R.id.descuento_si -> true
                R.id.descuento_no -> false
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
        binding.grupoUbicacion.setOnCheckedChangeListener { grupo, checkId ->
            ubi = when (checkId) {
                R.id.ubi_si -> {
                    binding.linealUbicacion.visibility = View.VISIBLE
                    true
                }

                R.id.ubi_no -> {
                    binding.linealUbicacion.visibility = View.GONE
                    binding.ubicacionTienda.setText("")
                    false
                }

                else -> null
            }
        }

        binding.grupoPrecio.setOnCheckedChangeListener { grupo, checkId ->
            precioBoolen = when (checkId) {
                R.id.precio_si -> {
                    binding.linealPrecioTexview.visibility = View.VISIBLE
                    binding.LinealMetodoPago.isVisible = true
                    binding.linealDescuento.isVisible = true
                    true
                }

                R.id.precio_no -> {
                    binding.linealDescuento.isVisible = false
                    binding.linealPrecioTexview.visibility = View.GONE
                    binding.LinealMetodoPago.isVisible = false
                    binding.precioED.setText("")
                    binding.yapeCheckbox.isChecked = false
                    binding.efectivoCheckbox.isChecked = false
                    binding.plinCheckbox.isChecked = false
                    binding.grupoDescuento.clearCheck()
                    binding.descuentoED.setText("")
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

        binding.grupoTipoTienda.setOnCheckedChangeListener { grupo, checkId ->
            tipoTienda = when (checkId) {
                R.id.tienda_fisica -> true
                R.id.tienda_virtual -> false
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
                    binding.campoContacto.isVisible = false
                    true
                }

                R.id.compra_Geinz -> {
                    binding.campoContacto.isVisible = true
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

    private fun actualizarCamposPublicidad(id: String) {
        val nombreTienda = binding.nombreTiendaED.text.toString()
        val titulo = binding.tituloPublicacionED.text.toString()
        val descripcion = binding.descripcionServiciosED.text.toString()
        val ubicaciontxt = binding.ubicacionTienda.text.toString()
        val categoriaAnuncio = binding.categoriaED.text.toString()
        val subcategoria = binding.subcategoriED.text.toString()


        // Validación de los campos
        if (nombreTienda.isEmpty() || titulo.isEmpty() || descripcion.isEmpty()) {
            ToastMessage("Todos los campos deben estar llenos", this)
            return
        }


        // Acceso a Firestore
        val db = FirebaseFirestore.getInstance()
            .collection("solicitudes_servicios")
            .document("publicidad_baner")
            .collection("activos")
            .document(id)

        db.get().addOnSuccessListener { res ->
            if (res.exists()) {
                val data = res.data
                val documento1 = data?.get("documento1") as? String
                val documento2 = data?.get("documento2") as? String

                if (!documento1.isNullOrEmpty() && !documento2.isNullOrEmpty()) {
                    val dbAnuncios = FirebaseFirestore.getInstance()
                        .collection("anuncios")
                        .document(documento1)
                        .collection("anuncios")
                        .document(documento2)

                    val hashmap = hashMapOf<String, Any>(
                        "nombreTienda" to nombreTienda,
                        "titulo" to titulo,
                        "descripcion" to descripcion,
                        "ubicacion" to (ubi ?: false),
                        "tipoTienda" to (tipoTienda ?: false),
                        "ubicacion_tienda" to ubicaciontxt,
                        "categoria" to categoriaAnuncio,
                        "subcategoria" to subcategoria,

                        )

                    dbAnuncios.set(hashmap, SetOptions.merge())
                        .addOnSuccessListener {
                            ToastMessage("Campos actualizados correctamente", this)
                        }
                        .addOnFailureListener { error ->
                            ToastMessage("Error al actualizar: ${error.message}", this)
                        }
                } else {
                    ToastMessage("Documentos relacionados no encontrados", this)
                }
            } else {
                ToastMessage("Documento no encontrado en 'activos'", this)
            }
        }.addOnFailureListener { error ->
            ToastMessage("Error al obtener el documento: ${error.message}", this)
        }
    }


    private fun actualizarCamposCompra_pago(id: String) {
        val precio = binding.precioED.text.toString()
        val link_compra = binding.linkDeSitioWeb.text.toString()
        val precioTexto = binding.precioED.text.toString()
        val descuentoTexto = binding.descuentoED.text.toString()
        val descunetosi = binding.descuentoSi.isChecked
        val precioInt = precioTexto.toIntOrNull()
        val descuento = descuentoTexto.toIntOrNull()


        if (precioInt == null || (descunetosi && descuento == null)) {
            binding.descuentoED.error = "Por favor, ingrese valores numéricos válidos"
            return
        }

        if (descunetosi && descuento!! >= precioInt) {
            binding.descuentoED.error = "El descuento no puede ser o igual mayor al precio original"
            return
        }

        val nuevoDescuento = if (descunetosi) descuentoTexto else ""
        val porcentajeDescuento = if (descunetosi) {
            ((precioInt - descuento!!).toDouble() / precioInt * 100).toInt()
        } else {
            0
        }
        val db = FirebaseFirestore.getInstance()
            .collection("solicitudes_servicios")
            .document("publicidad_baner")
            .collection("activos")
            .document(id)
        db.get().addOnSuccessListener { res ->
            if (res.exists()) {
                val data = res.data
                val documento1 = data?.get("documento1") as? String
                val documento2 = data?.get("documento2") as? String
                if (!documento1.isNullOrEmpty() && !documento2.isNullOrEmpty()) {
                    val dbAnuncios = FirebaseFirestore.getInstance()
                        .collection("anuncios")
                        .document(documento1)
                        .collection("anuncios")
                        .document(documento2)
                    val hasmap = hashMapOf<String, Any>(
                        "precio" to precio,
                        "precio_boolean" to (precioBoolen ?: false),
                        "efectivo" to efectivo,
                        "reserva" to (reserva ?: false),
                        "compra" to (compra ?: false),
                        "descuento_boolean" to (descuentoBool ?: false),
                        "plin" to plin,
                        "yape" to yape,
                        "precio" to precio,
                        "metodo_compra_web_Geinz" to (metodCompra ?: false),
                        "link_compra" to link_compra,
                        "descuento" to nuevoDescuento,
                        "porcentajeDescuento" to porcentajeDescuento
                    )
                    dbAnuncios.set(hasmap, SetOptions.merge()).addOnSuccessListener {
                        agregar_img_qr(firebaseAuth.uid.toString())
                        ToastMessage(
                            "Campos de metodo de pago y compra actualizados correctamente",
                            this
                        )
                    }.addOnFailureListener {
                        ToastMessage(
                            "Error al actualizar los campos ",
                            this
                        )
                    }
                }
            }
        }

    }

    private fun actualizarCamposContacto(id: String) {
        val numeroTienda = binding.numeroTiendaED.text.toString()
        val mensajeWhatsapp = binding.mensajeED.text.toString()
        val db = FirebaseFirestore.getInstance()
            .collection("solicitudes_servicios")
            .document("publicidad_baner")
            .collection("activos")
            .document(id)
        db.get().addOnSuccessListener { res ->
            if (res.exists()) {
                val data = res.data
                val documento1 = data?.get("documento1") as? String
                val documento2 = data?.get("documento2") as? String

                if (!documento1.isNullOrEmpty() && !documento2.isNullOrEmpty()) {
                    val dbAnuncios = FirebaseFirestore.getInstance()
                        .collection("anuncios")
                        .document(documento1)
                        .collection("anuncios")
                        .document(documento2)
                    val hasmap = hashMapOf<String, Any>(
                        "numero" to numeroTienda,
                        "mensaje" to mensajeWhatsapp
                    )

                    dbAnuncios.set(hasmap, SetOptions.merge()).addOnSuccessListener {
                        ToastMessage("Campos de contacto actualizados correctamente", this)
                    }.addOnFailureListener {
                        ToastMessage("Error al actualiazr los campos de contacto", this)
                    }

                }

            }
        }
    }

    private fun actualizarCamposRedes(id: String) {
        val fb = binding.fbED.text.toString()
        val tk = binding.tiktokED.text.toString()
        val web = binding.websiteEd.text.toString()
        val ig = binding.igED.text.toString()
        val db = FirebaseFirestore.getInstance()
            .collection("solicitudes_servicios")
            .document("publicidad_baner")
            .collection("activos")
            .document(id)
        db.get().addOnSuccessListener { res ->
            if (res.exists()) {
                val data = res.data
                val documento1 = data?.get("documento1") as? String
                val documento2 = data?.get("documento2") as? String

                if (!documento1.isNullOrEmpty() && !documento2.isNullOrEmpty()) {
                    val dbAnuncios = FirebaseFirestore.getInstance()
                        .collection("anuncios")
                        .document(documento1)
                        .collection("anuncios")
                        .document(documento2)
                    val hasmap = hashMapOf<String, Any>(
                        "fb" to fb,
                        "tk" to tk,
                        "web" to web,
                        "ig" to ig,
                    )
                    dbAnuncios.set(hasmap, SetOptions.merge()).addOnSuccessListener {
                        ToastMessage("Campos de redes actualizados correctamente", this)
                    }
                        .addOnFailureListener {
                            ToastMessage(
                                "Error al actualizar los campos de redes",
                                this
                            )
                        }
                }
            }
        }
    }


    override fun onDestroy() {
        super.onDestroy()
        timer?.cancel()
    }

    fun ToastMessage(msj: String, context: Context) {
        Toast.makeText(
            context,
            msj,
            Toast.LENGTH_SHORT
        ).show()
    }
}