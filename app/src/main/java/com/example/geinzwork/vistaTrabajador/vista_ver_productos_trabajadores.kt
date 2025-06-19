package com.example.geinzwork.vistaTrabajador

import android.content.Context
import android.content.Intent
import android.graphics.Paint
import android.graphics.Typeface
import android.net.Uri
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Handler
import android.os.Looper
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.StyleSpan
import android.text.style.UnderlineSpan
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.geinzwork.adapterViewholder.adapterInicializarRecycleimgProductosTrabajadores
import com.example.geinzwork.constantesGeneral.Variables
import com.example.geinzwork.constantesGeneral.constantes_vistas_publicaciones_productos_verificados
import com.example.geinzwork.constantesGeneral.constatnes_carga_imagenes_general
import com.example.geinzwork.fragmentos.productosPublicadosVista.compras_productos_vendedor
import com.example.geinzwork.fragmentos.productosPublicadosVista.ver_mas_productos_publicados_trabajadores
import com.geinzz.geinzwork.R
import com.geinzz.geinzwork.constantesGeneral.constantesCarrito
import com.geinzz.geinzwork.constantesGeneral.constantesPublicidad
import com.geinzz.geinzwork.constantesGeneral.constantes_publicaciones_general_user_tiendas
import com.geinzz.geinzwork.constantesGeneral.constantes_publicaciones_general_user_tiendas.obtener_metodoEntrega
import com.geinzz.geinzwork.constantesGeneral.constantes_publicaciones_general_user_tiendas.obtener_metodosPaog
import com.geinzz.geinzwork.constantesGeneral.constantes_servicios
import com.geinzz.geinzwork.constantesGeneral.constantestextos_general
import com.geinzz.geinzwork.databinding.ActivityVistaVerProductosTrabajadoresBinding
import com.geinzz.geinzwork.databinding.BottomsheetProductosVendidosUserVerifiBinding
import com.geinzz.geinzwork.dataclass.dataclassMostarImgProductosVendedor
import com.google.android.material.progressindicator.CircularProgressIndicator
import com.google.firebase.Firebase
import com.google.firebase.dynamiclinks.androidParameters
import com.google.firebase.dynamiclinks.dynamicLinks
import com.google.firebase.dynamiclinks.googleAnalyticsParameters
import com.google.firebase.dynamiclinks.iosParameters
import com.google.firebase.dynamiclinks.itunesConnectAnalyticsParameters
import com.google.firebase.dynamiclinks.shortLinkAsync
import com.google.firebase.dynamiclinks.socialMetaTagParameters
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage

class vista_ver_productos_trabajadores : AppCompatActivity() {
    val listaImg = mutableListOf<dataclassMostarImgProductosVendedor>()
    private lateinit var binding: ActivityVistaVerProductosTrabajadoresBinding
    private val tiempoParaContarVista: Long = 20000
    private var vistaTimer: CountDownTimer? = null
    var isCamposVisible = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityVistaVerProductosTrabajadoresBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        val idTrabajador = intent.getStringExtra("id_trabajador").toString()
        val id_publicacion_clikeada = intent.getStringExtra("id_publicacion").toString()
        val tipo_ubicado = intent.getStringExtra("tipo_ubicado").toString()

        obtenerCampos_producto(idTrabajador, id_publicacion_clikeada, tipo_ubicado)

        binding.retroceder.setOnClickListener {
            onBackPressed()
        }
        constantesCarrito.setearDatosUsuarioImgNombre(idTrabajador) { nombre, img, apellido, nacionalidad, categoria, verificado, trabajador_user ->
            val db = FirebaseFirestore.getInstance()
                .collection("Trabajadores_Usuarios_Drivers").document("trabajadores")
                .collection("trabajadores").document(idTrabajador)
                .collection("productos_venta").document(tipo_ubicado)
                .collection(tipo_ubicado).document(id_publicacion_clikeada)
            binding.compartirIcon.setOnClickListener {
                constantesPublicidad.agregarCantidadClickAnuncios(
                    db,
                    "",
                    "compartir"
                )
                crear_dinamick_link(
                    this,
                    idTrabajador,
                    id_publicacion_clikeada,
                    "Mira este producto publicado por $nombre $apellido",
                    "${binding.nombreProducto.text}"
                )
            }
        }
        binding.ocultarCamposDePublicidad.setOnClickListener {
            if (isCamposVisible) {
                binding.camposProductosUserVerificados.linealVer.visibility =
                    View.GONE
                binding.ocultarP1.setImageResource(R.drawable.ocultar_arriva)

            } else {
                binding.camposProductosUserVerificados.linealVer.visibility =
                    View.VISIBLE
                binding.ocultarP1.setImageResource(R.drawable.ocultar_abajo)
            }
            isCamposVisible = !isCamposVisible
        }
        binding.ocultarCamposMasInformacion.setOnClickListener {
            if (isCamposVisible) {
                binding.masInfomacion.visibility =
                    View.GONE
                binding.ocultarP2.setImageResource(R.drawable.ocultar_arriva)

            } else {
                binding.masInfomacion.visibility =
                    View.VISIBLE
                binding.ocultarP2.setImageResource(R.drawable.ocultar_abajo)
            }
            isCamposVisible = !isCamposVisible
        }
        binding.ocultarCamposDescripcion.setOnClickListener {
            if (isCamposVisible) {
                binding.linealTextosDescripcion.visibility =
                    View.GONE
                binding.ocultarP2.setImageResource(R.drawable.ocultar_arriva)

            } else {
                binding.linealTextosDescripcion.visibility =
                    View.VISIBLE
                binding.ocultarP2.setImageResource(R.drawable.ocultar_abajo)
            }
            isCamposVisible = !isCamposVisible
        }
        constantes_vistas_publicaciones_productos_verificados.obtener_productosVenta(
            idTrabajador,
            this,
            binding.cargaProductosPromoTrabajos.masTrabajosRealiados
        ) { existe ->
            if (existe) {
                binding.cargaProductosPromoTrabajos.cargandoContenido.isVisible = false
                binding.cargaProductosPromoTrabajos.masTrabajosRealiados.isVisible = true
                binding.cargaProductosPromoTrabajos.linealNoSeEncontraron.isVisible = false

            } else {
                binding.cargaProductosPromoTrabajos.cargandoContenido.isVisible = false
                binding.cargaProductosPromoTrabajos.masTrabajosRealiados.isVisible = false
                binding.cargaProductosPromoTrabajos.linealNoSeEncontraron.isVisible = true
            }
        }
        binding.cargaProductosPromoTrabajos.cambiarTextoTrabajosRealziadosTrabajosRecientes.text =
            "Productos publicados"
        binding.cargaProductosPromoTrabajos.textoCambiarTrabajosOPublicaciones.text =
            "No se encontraron productos"
        constantes_vistas_publicaciones_productos_verificados.obtener_perfil_trabajador(
            idTrabajador,
            binding.perfiltrabajador,
            this
        )
        constantes_vistas_publicaciones_productos_verificados.ver_todos_productos_activity(
            binding.cargaProductosPromoTrabajos,
            this,
            idTrabajador
        )


    }

    private fun iniciarContadorVista(db: DocumentReference) {
        vistaTimer = object : CountDownTimer(tiempoParaContarVista, 1000) {
            override fun onTick(millisUntilFinished: Long) {}

            override fun onFinish() {
                constantesPublicidad.agregarCantidadClickAnuncios(db, "", Variables.vistas)
            }
        }.start()
    }

    private fun crear_dinamick_link(
        contex: Context,
        idTrabajador: String,
        id_publicacion: String,
        titulo_dinamick: String,
        texto_dinamick: String
    ) {
        val userCollections =
            FirebaseFirestore.getInstance().collection(Variables.trabajadores_usuariosDB)
                .document(Variables.trabajadoresDB).collection(Variables.trabajadoresDB)
                .document(idTrabajador).collection("productos_venta").document("publicados")
                .collection("publicados").document(id_publicacion)
        userCollections.get().addOnSuccessListener { res ->
            if (res.exists()) {
                val data = res.data
                val img_url = data?.get("img_url") as? String ?: ""
                val titulo = data?.get("titulo") as? String ?: ""
                Log.d("idpublicacones", "$id_publicacion ,$idTrabajador ,$img_url")
                if (img_url.isNotEmpty()) {
                    Firebase.dynamicLinks.shortLinkAsync {
                        link =
                            Uri.parse("https://geinzapp.page.link/?idTrabajadorVeriProducto=${idTrabajador}&idProducto=${id_publicacion}")
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
                            title = titulo_dinamick
                            description = texto_dinamick
                            imageUrl = Uri.parse(img_url)
                        }
                    }.addOnSuccessListener { shortDynamicLink ->
                        val shortLink = shortDynamicLink.shortLink
                        val invitationLink = shortLink.toString()

                        val sendIntent: Intent = Intent().apply {
                            action = Intent.ACTION_SEND
                            putExtra(Intent.EXTRA_TEXT, invitationLink)
                            type = "text/plain"
                        }
                        contex.startActivity(Intent.createChooser(sendIntent, null))
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

    private fun editar_setar_valores_campos(
        titulo: String,
        fuenteTextoTitulo: String,
        mayus_minus: String,
        texto_des: String,
        fuenteTextoTitulo_des: String,
        mayus_minus_des: String,
        listaFrases: List<String>
    ) {
        val tituloFormateado = when (mayus_minus) {
            "mayuscula" -> titulo.uppercase()
            "minuscula" -> titulo.lowercase()
            else -> titulo
        }
        binding.vistraPreviaDescripciontitulo.text = tituloFormateado
        when (fuenteTextoTitulo) {
            "Bold" -> {
                binding.vistraPreviaDescripciontitulo.setTypeface(
                    null,
                    Typeface.BOLD
                )
                binding.vistraPreviaDescripciontitulo.paintFlags =
                    binding.vistraPreviaDescripciontitulo.paintFlags and Paint.UNDERLINE_TEXT_FLAG.inv()
            }

            "Cursiva" -> {
                binding.vistraPreviaDescripciontitulo.setTypeface(
                    null,
                    Typeface.ITALIC
                )
                binding.vistraPreviaDescripciontitulo.paintFlags =
                    binding.vistraPreviaDescripciontitulo.paintFlags and Paint.UNDERLINE_TEXT_FLAG.inv()
            }

            "Subrayado" -> {
                binding.vistraPreviaDescripciontitulo.setTypeface(
                    null,
                    Typeface.NORMAL
                )
                binding.vistraPreviaDescripciontitulo.paintFlags =
                    binding.vistraPreviaDescripciontitulo.paintFlags or Paint.UNDERLINE_TEXT_FLAG
            }

            else -> {
                binding.vistraPreviaDescripciontitulo.setTypeface(
                    null,
                    Typeface.NORMAL
                )
                binding.vistraPreviaDescripciontitulo.paintFlags =
                    binding.vistraPreviaDescripciontitulo.paintFlags and Paint.UNDERLINE_TEXT_FLAG.inv()
            }
        }


        val spannable = SpannableStringBuilder(texto_des)

        for (fraseOriginal in listaFrases) {
            val textoOriginalLower = texto_des.lowercase()
            val fraseLower = fraseOriginal.lowercase()

            var startIndex = textoOriginalLower.indexOf(fraseLower)

            while (startIndex != -1) {
                val endIndex = startIndex + fraseLower.length

                // Aplicar estilo (Bold, Cursiva, Subrayado)
                when (fuenteTextoTitulo_des) {
                    "Bold" -> spannable.setSpan(
                        StyleSpan(Typeface.BOLD),
                        startIndex,
                        endIndex,
                        Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                    )

                    "Cursiva" -> spannable.setSpan(
                        StyleSpan(Typeface.ITALIC),
                        startIndex,
                        endIndex,
                        Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                    )

                    "Subrayado" -> spannable.setSpan(
                        UnderlineSpan(),
                        startIndex,
                        endIndex,
                        Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                    )
                }

                // Reemplazar visualmente la frase con mayúscula o minúscula (sin alterar el resto del texto)
                val nuevaFrase = when (mayus_minus_des) {
                    "mayuscula" -> texto_des.substring(startIndex, endIndex).uppercase()
                    "minuscula" -> texto_des.substring(startIndex, endIndex).lowercase()
                    else -> texto_des.substring(startIndex, endIndex)
                }

                spannable.replace(startIndex, endIndex, nuevaFrase)

                // Buscar siguiente ocurrencia
                val nuevoTexto = spannable.toString().lowercase()
                startIndex = nuevoTexto.indexOf(fraseLower, startIndex + nuevaFrase.length)
            }
        }

        binding.vistraPreviaDescripcion.text = spannable

    }

    private fun obtner_img_descripcion(id_trabajador: String, producto_id: String) {
        val storageRef = FirebaseStorage.getInstance().reference

        val fileName = "caracteristica_producto"

        val rutaImagen = storageRef
            .child("usuarios")
            .child(id_trabajador)
            .child("productos_publicados")
            .child(producto_id)
            .child(fileName)

        rutaImagen.downloadUrl
            .addOnSuccessListener { uri ->
                val urlImagen = uri.toString()
                Log.d("DownloadURL", "URL de la imagen: $urlImagen")
                if (urlImagen.isNotEmpty()) {
                    binding.relativeImgContainer.isVisible = true
                    constatnes_carga_imagenes_general.changer_img(
                        binding.progreesIndicator,
                        this,
                        urlImagen,
                        null,
                        binding.imgSubir,
                        "portada",
                        null
                    ) { completado ->

                    }
                } else {
                    binding.relativeImgContainer.isVisible = false
                }

            }
            .addOnFailureListener { e ->
                Log.e("DownloadURL", "Error al obtener la URL", e)
            }

    }


    private fun obtenerCampos_producto(
        idTrabajador: String,
        productoClikado: String,
        tipo_obtenido: String
    ) {
        val startTime = System.currentTimeMillis()
        binding.containerDatos.isVisible = false

        val db = FirebaseFirestore.getInstance().collection("Trabajadores_Usuarios_Drivers")
            .document("trabajadores").collection("trabajadores").document(idTrabajador)
            .collection("productos_venta").document(tipo_obtenido).collection(tipo_obtenido)
            .document(productoClikado)
        constantesPublicidad.agregarCantidadClickAnuncios(db, "", "click")
        obtner_img_descripcion(idTrabajador, productoClikado)
        iniciarContadorVista(db)

        db.get().addOnSuccessListener { res ->
            val endTime = System.currentTimeMillis()
            val totalTime = endTime - startTime
            val tiempoMinimoCarga = 800L
            val esperaExtra =
                if (totalTime < tiempoMinimoCarga) (tiempoMinimoCarga - totalTime) else 0L

            if (res.exists()) {
                val data = res.data ?: emptyMap()

                Handler(Looper.getMainLooper()).postDelayed({
                    try {
                        val cantidadPorcentajeDescuento =
                            data?.get("cantidad_porcentaje_descuento") as? Number ?: 0
                        val precio = data?.get("precio") as? Number ?: 0
                        val precioDescuento = data?.get("precio_descuento") as? Number ?: 0
                        val totalProducto = data?.get("total_producto") as? Number ?: 0

                        val categoria = data?.get("categoria_producto") as? String ?: ""
                        val condicionProducto = data?.get("condicion_producto") as? String ?: ""
                        val descripcion = data?.get("descripcion") as? String ?: ""
                        val modelo = data?.get("modelo") as? String ?: ""
                        val descuento = data?.get("descuento") as? Boolean ?: false
                        val verificado = data?.get("verificado") as? Boolean ?: false
                        val entrega_domicilio = data?.get("entrega_domicilio") as? Boolean ?: true
                        val garantia = data?.get("garantia") as? String ?: ""
                        val cantidad_porcentaje_descuento =
                            data?.get("cantidad_porcentaje_descuento") as? Number ?: 0
                        val id = data?.get("id") as? String ?: ""
                        val fechaPublicada = data?.get("fechaPublicada") as? String ?: ""
                        val lugarDeEntrega = data?.get("lugarEntrega") as? String ?: ""
                        val marca = data?.get("marca") as? String ?: ""
                        val nombre = data?.get("nombre") as? String ?: ""
                        val stok = data?.get("stok") as? String ?: ""
                        val plin = data?.get("plin") as? Boolean ?: false
                        val efectivo = data?.get("efectivo") as? Boolean ?: false
                        val yape = data?.get("yape") as? Boolean ?: false
                        val mas_informacio = data?.get("mas_informacio") as? String ?: ""
                        val metodoPago = data?.get("metodoPago") as? String ?: ""
                        val metodoEntrega = data?.get("metodoEntrega") as? String ?: ""
                        // Obtener valores del mapa de título
                        val descripcionTextoMap = data?.get("descripcion_texto") as? Map<*, *>
                        val descripcionTituloMap = data?.get("descripcion_titulo") as? Map<*, *>

                        // Arrays
                        val hashtagsGenerales = data?.get("hashtags_generales") as? List<*>
                        val listaTextoDescripcion = data?.get("descripcion_texto_lista") as? List<*>

                        // Convertir arrays a listas seguras de Strings
                        val listaHashtags =
                            hashtagsGenerales?.mapNotNull { it as? String } ?: emptyList()
                        val listaFrases =
                            listaTextoDescripcion?.mapNotNull { it as? String } ?: emptyList()
                        val titulo =
                            descripcionTituloMap?.get("titulo_descripcion") as? String ?: ""
                        val fuenteTextoTitulo =
                            descripcionTituloMap?.get("titulo_valor_style") as? String ?: ""
                        val mayusMinusTitulo =
                            descripcionTituloMap?.get("titulo_mayus") as? String ?: ""


                        // Obtener valores del mapa de descripción
                        val textoDescripcion =
                            descripcionTextoMap?.get("texto_descripcion") as? String ?: ""
                        val fuenteTextoDescripcion =
                            descripcionTextoMap?.get("texto_valor_style") as? String ?: ""


                        val mayusMinusDescripcion =
                            descripcionTextoMap?.get("texto_mayus") as? String ?: ""



                        obtener_metodosPaog(idTrabajador, metodoPago) { metodos_encontrados ->
                            binding.camposProductosUserVerificados.metodosPago.text =
                                metodos_encontrados
                        }

                        obtener_metodoEntrega(
                            idTrabajador, metodoEntrega,
                            callback = { metodo_entrega ->
                                binding.camposProductosUserVerificados.metodoEntrega.text =
                                    metodo_entrega
                            },
                            evio_gratis = { delivery_gratis ->
                                if (delivery_gratis) {
                                    binding.envioGratis.isVisible = true
                                } else {
                                    binding.envioGratis.isVisible = false
                                }
                            }
                        )

                        binding.masInfomacion.text = mas_informacio

                        if (descuento) {
                            constantestextos_general.marcarDescuentoTxt(binding.camposProductosUserVerificados.precioAntiguo)
                            constantestextos_general.setearPrecioDescuentoPrecioAntiguo(
                                precioDescuento,
                                binding.camposProductosUserVerificados.precioProducto,
                                precio,
                                binding.camposProductosUserVerificados.precioAntiguo,
                                cantidadPorcentajeDescuento,
                                binding.camposProductosUserVerificados.descuentoPorcentaje
                            )
                            binding.camposProductosUserVerificados.precioAntiguo.isVisible = true
                            binding.camposProductosUserVerificados.descuentoPorcentaje.isVisible =
                                true
                            constantestextos_general.setearPrecioDescuentoPrecioAntiguo(
                                precioDescuento,
                                binding.precioProducto,
                                precio,
                                binding.precioAntiguo,
                                cantidad_porcentaje_descuento,
                                binding.descuentoPorcentaje
                            )
                            constantestextos_general.marcarDescuentoTxt(binding.precioAntiguo)

                            constantestextos_general.marcarDescuentoTxt(binding.camposProductosUserVerificados.precioAntiguo)
                            constantestextos_general.setearPrecioDescuentoPrecioAntiguo(
                                precioDescuento,
                                binding.camposProductosUserVerificados.precioProducto,
                                precio,
                                binding.camposProductosUserVerificados.precioAntiguo,
                                cantidad_porcentaje_descuento,
                                binding.camposProductosUserVerificados.descuentoPorcentaje
                            )
                            binding.camposProductosUserVerificados.precioAntiguo.isVisible =
                                true
                            binding.camposProductosUserVerificados.descuentoPorcentaje.isVisible =
                                true
                            binding.precioAntiguo.isVisible =
                                true
                            binding.descuentoPorcentaje.isVisible =
                                true
                        } else {
                            binding.camposProductosUserVerificados.precioAntiguo.isVisible = false
                            binding.camposProductosUserVerificados.descuentoPorcentaje.isVisible =
                                false
                            constantestextos_general.setearPrecioDescuentoPrecioAntiguo(
                                precio,
                                binding.precioProducto
                            )
                            binding.camposProductosUserVerificados.precioAntiguo.isVisible =
                                false
                            binding.camposProductosUserVerificados.descuentoPorcentaje.isVisible =
                                false
                            binding.precioAntiguo.isVisible =
                                false
                            binding.descuentoPorcentaje.isVisible =
                                false
                            constantestextos_general.setearPrecioDescuentoPrecioAntiguo(
                                precio,
                                binding.camposProductosUserVerificados.precioProducto
                            )
                        }


                        editar_setar_valores_campos(
                            titulo,
                            fuenteTextoTitulo,
                            mayusMinusTitulo,
                            textoDescripcion,
                            fuenteTextoDescripcion,
                            mayusMinusDescripcion,
                            listaFrases
                        )


                        binding.nombreProducto.text = nombre
                        val db = FirebaseFirestore.getInstance()
                            .collection("Trabajadores_Usuarios_Drivers")
                            .document("trabajadores").collection("trabajadores")
                            .document(idTrabajador)
                        db.get().addOnSuccessListener { res ->
                            if (res.exists()) {
                                val data = res.data
                                val nombre_trabajador = data?.get("nombre") as? String ?: ""
                                constantes_servicios.verificarEstado_vericiacion(
                                    binding.iconoVerificado,
                                    idTrabajador
                                ) { v, plan ->
                                    when (plan) {
                                        Variables.plaA -> {
                                            binding.iconoVerificado.setImageResource(R.drawable.verificado_a)
                                            binding.nombreTrabajador.text =
                                                "Vendido por : $nombre_trabajador"

                                        }

                                        Variables.planB -> {
                                            binding.iconoVerificado.setImageResource(R.drawable.icon_verificado)
                                            binding.nombreTrabajador.text =
                                                "Vendido por : $nombre_trabajador"
                                        }

                                        Variables.PlanC -> {
                                            binding.iconoVerificado.setImageResource(R.drawable.verificado_c)
                                            binding.nombreTrabajador.text =
                                                "Vendido por : $nombre_trabajador"


                                        }
                                    }

                                }
                            }
                        }
                        binding.marcaProducto.text = marca

                        if (marca.isNotEmpty() && modelo.isNotEmpty()) {
                            binding.camposProductosUserVerificados.marca.text = marca
                            binding.camposProductosUserVerificados.modelo.text = modelo
                            binding.camposProductosUserVerificados.linealMarcaModelo.isVisible =
                                true
                        } else {
                            binding.camposProductosUserVerificados.linealMarcaModelo.isVisible =
                                false

                        }

                        binding.camposProductosUserVerificados.categoriaProducto.text = categoria


                        binding.camposProductosUserVerificados.stok.text = stok
                        binding.camposProductosUserVerificados.garantia.text = garantia
                        binding.camposProductosUserVerificados.Condicion.text = condicionProducto
                        binding.camposProductosUserVerificados.descripcion.text = descripcion
                        binding.camposProductosUserVerificados.fechaPublicado.text = fechaPublicada

                        constantestextos_general.extender_acortar_texto(
                            binding.camposProductosUserVerificados.descripcion,
                            binding.camposProductosUserVerificados.tvReadMore
                        )
                        inizializarImgProductosclikeado(this, listaImg, data)

                        binding.comprar.setOnClickListener {
                            val intent =
                                Intent(this, compras_productos_vendedor::class.java).apply {
                                    putExtra("idProducto", productoClikado)
                                    putExtra("idTrabajador", idTrabajador)
                                }
                            startActivity(intent)
                        }

                        binding.containerDatos.isVisible = true
                        binding.cargandoContenido.isVisible = false

                    } catch (e: Exception) {
                        Log.e("obtenerCampos_producto", "Error al procesar los datos: ${e.message}")
                    }

                }, esperaExtra)
            }
        }.addOnFailureListener {
            binding.cargandoContenido.isVisible = false
            Log.e("obtenerCampos_producto", "Error al obtener el documento: ${it.message}")
        }
    }


    fun inizializarImgProductosclikeado(
        context: Context,
        listaImg: MutableList<dataclassMostarImgProductosVendedor>,
        data: Map<String, Any>
    ) {
        // Limpiar la lista para evitar duplicados
        listaImg.clear()

        // Obtener las imágenes del mapa `data`
        val imgPrincipal = data["img_url"] as? String ?: ""
        val img_url2 = data["img_url2"] as? String ?: ""
        val img_url3 = data["img_url3"] as? String ?: ""
        val img_url4 = data["img_url4"] as? String ?: ""

        // Agregar las imágenes a la lista si no están vacías
        if (imgPrincipal.isNotEmpty()) listaImg.add(dataclassMostarImgProductosVendedor(imgPrincipal))
        if (img_url2.isNotEmpty()) listaImg.add(dataclassMostarImgProductosVendedor(img_url2))
        if (img_url3.isNotEmpty()) listaImg.add(dataclassMostarImgProductosVendedor(img_url3))
        if (img_url4.isNotEmpty()) listaImg.add(dataclassMostarImgProductosVendedor(img_url4))


        // Configurar RecyclerView solo una vez
        val recicle = binding.carrucelImgProductosVentaUser
        if (recicle.adapter == null) {
            recicle.layoutManager =
                LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            recicle.adapter = adapterInicializarRecycleimgProductosTrabajadores(listaImg)
        } else {
            recicle.adapter?.notifyDataSetChanged()
        }
    }


}