package com.geinzz.geinzwork.constantesGeneral

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Paint
import android.graphics.Typeface
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.StyleSpan
import android.text.style.UnderlineSpan
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.lifecycle.Lifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.example.geinzwork.adapterViewholder.adapterInicializarRecycleimgProductosTrabajadores
import com.example.geinzwork.adapterViewholder.adapter_mostra_articulos_trabajadores
import com.example.geinzwork.adapterViewholder.adapter_trabajos_realizados_trabajador
import com.example.geinzwork.classcustom.classcustomscrool
import com.example.geinzwork.constantesGeneral.Variables
import com.example.geinzwork.constantesGeneral.constatnes_carga_imagenes_general
import com.example.geinzwork.dataclass.dataclas_item_preview_art_comprar
import com.example.geinzwork.dataclass.dataclass_adapter_promociones
import com.example.geinzwork.fragmentos.productosPublicadosVista.compras_productos_vendedor
import com.example.geinzwork.fragmentos.productosPublicadosVista.ver_mas_productos_publicados_trabajadores
import com.example.geinzwork.publicaciones_trabajadores.mostrarTodosTrabajos
import com.geinzz.geinzwork.R
import com.geinzz.geinzwork.databinding.BottomSheetMostarTrabajosRecientesBinding
import com.geinzz.geinzwork.databinding.BottomsheetProductosVendidosUserVerifiBinding
import com.geinzz.geinzwork.databinding.FragmentInfoBinding
import com.geinzz.geinzwork.databinding.ItemCustomFixedSizeLayout2Binding
import com.geinzz.geinzwork.dataclass.dataclas_trabajos_ralizados
import com.geinzz.geinzwork.dataclass.dataclassMostarImgProductosVendedor
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.Firebase
import com.google.firebase.dynamiclinks.androidParameters
import com.google.firebase.dynamiclinks.dynamicLinks
import com.google.firebase.dynamiclinks.googleAnalyticsParameters
import com.google.firebase.dynamiclinks.iosParameters
import com.google.firebase.dynamiclinks.itunesConnectAnalyticsParameters
import com.google.firebase.dynamiclinks.shortLinkAsync
import com.google.firebase.dynamiclinks.socialMetaTagParameters
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import org.imaginativeworld.whynotimagecarousel.listener.CarouselListener
import org.imaginativeworld.whynotimagecarousel.model.CarouselItem
import org.imaginativeworld.whynotimagecarousel.utils.setImage

object constantes_publicaciones_general_user_tiendas {
    private val listaAdapterProductosTRabajdores =
        mutableListOf<dataclas_item_preview_art_comprar>()
    val listaImg = mutableListOf<dataclassMostarImgProductosVendedor>()
    private val listaProductosUSer = mutableListOf<dataclas_item_preview_art_comprar>()
    private lateinit var dialog: BottomSheetDialog

    @SuppressLint("SuspiciousIndentation")
    fun obtenerPublicaciones(
        plan: String,
        id: String,
        lista: MutableList<dataclas_trabajos_ralizados>,
        context: Context,
        adapter: RecyclerView.Adapter<*>,
        binding: FragmentInfoBinding,
    ) {


        val db = FirebaseFirestore.getInstance().collection(Variables.trabajadores_usuariosDB)
            .document(Variables.trabajadoresDB).collection(Variables.trabajadoresDB)
            .document(id)
            .collection(Variables.trabajos_realizados).document("publicados")
            .collection("publicados")
        binding.linealProductosPublicados.isVisible = true
        obtenerARticulosComprasVerificado(binding, context, id)
        lista.clear()

        db.get().addOnSuccessListener { res ->
            val listaTemporal = mutableListOf<dataclas_trabajos_ralizados>()
            for (datos in res) {
                val data = datos.data
                val trabajoRealizado = dataclas_trabajos_ralizados(
                    data?.get("img_url") as? String ?: "",
                    data?.get(Variables.titulo) as? String ?: "",
                    data?.get(Variables.descripcion) as? String ?: "",
                    data?.get(Variables.fecha) as? String ?: "",
                    data?.get(Variables.hora) as? String ?: "",
                    data?.get(Variables.id) as? String ?: ""
                )
                listaTemporal.add(trabajoRealizado)
            }

            // Mezclar los elementos de la lista de manera aleatoria
            listaTemporal.shuffle()
            // Tomar hasta 5 elementos aleatorios
            lista.addAll(listaTemporal.take(5))

            if (lista.isEmpty()) {
                binding.noSeEncontroPublicaciones.isVisible = true
            } else {
                binding.noSeEncontroPublicaciones.isVisible = false
                inicializarRecicle(binding.TrabajosRealizados, adapter, context)
                adapter.notifyDataSetChanged()
            }
        }

    }

    private fun obtenerARticulosComprasVerificado(
        binding: FragmentInfoBinding,
        context: Context,
        idTrabajador: String
    ) {
        val db = FirebaseFirestore.getInstance().collection("Trabajadores_Usuarios_Drivers")
            .document("trabajadores").collection("trabajadores").document(idTrabajador)
            .collection("productos_venta").document("publicados").collection("publicados")

        db.get().addOnSuccessListener { res ->
            val listaTemporal = mutableListOf<dataclas_item_preview_art_comprar>()

            for (datos in res) {
                val data = datos.data
                val imgProducto = data["img_url"] as? String ?: ""
                val descuentoActivo = data["descuento"] as? Boolean ?: false
                val id = data["id"] as? String ?: ""
                val cantidadDescuento = data["cantidad_porcentaje_descuento"] as? Number ?: 0
                val precio_descuento = data["precio_descuento"] as? Number ?: 0
                val precio = data["precio"] as? Number ?: 0
                val nombre = data["nombre"] as? String ?: ""
                val item = dataclas_item_preview_art_comprar(
                    id,
                    imgProducto,
                    nombre,
                    precio,
                    precio_descuento,
                    descuentoActivo,
                    cantidadDescuento
                )
                listaTemporal.add(item)
            }

            listaTemporal.shuffle()

            listaAdapterProductosTRabajdores.clear()
            listaAdapterProductosTRabajdores.addAll(listaTemporal)

            // Inicializar RecyclerView una sola vez
            if (listaAdapterProductosTRabajdores.isNotEmpty()) {
                inicializarRecicleProductosVentasTrabajdores(
                    binding, context,
                    listaAdapterProductosTRabajdores,
                    idTrabajador
                )
                binding.linealNoSeEncontraron.isVisible = false
                binding.productosDestacados.isVisible = true

            } else {
                binding.linealNoSeEncontraron.isVisible = true
                binding.productosDestacados.isVisible = false
            }
        }.addOnFailureListener { e ->
            println("No se encontraron datos: ${e.message}")
        }
    }

    private fun inicializarRecicleProductosVentasTrabajdores(
        binding: FragmentInfoBinding,
        context: Context,
        listaAdapterProductosTRabajdores: MutableList<dataclas_item_preview_art_comprar>,
        idTrabajador: String
    ) {
        val recicle = binding.productosDestacados
        recicle.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        recicle.adapter = adapter_mostra_articulos_trabajadores(
            listaAdapterProductosTRabajdores
        ) { item ->
            dialog = BottomSheetDialog(context)
            ShowBottomSheetDialogProductosTrabajadores(
                context,
                idTrabajador,
                item.id.toString(), dialog
            )
            dialog.show()
        }
    }

    fun ShowBottomSheetDialogProductosTrabajadores(
        context: Context,
        idTrabajador: String,
        productoClikado: String,
        dialog: BottomSheetDialog
    ) {
        val bindingProductosTrabajadores =
            BottomsheetProductosVendidosUserVerifiBinding.inflate(LayoutInflater.from(context))
        dialog.setContentView(bindingProductosTrabajadores.root)
        bindingProductosTrabajadores.cerrar.setOnClickListener {
            dialog.dismiss()
        }

        val tiempoApertura = System.currentTimeMillis()

        bindingProductosTrabajadores.cargaProductosPromoTrabajos.verTodosTrabajos.setOnClickListener {
            val intent =
                Intent(context, ver_mas_productos_publicados_trabajadores::class.java).apply {
                    putExtra("idTrabajador", idTrabajador)
                }
            context.startActivity(intent)
            dialog.dismiss()
        }
        bindingProductosTrabajadores.comprar.setOnClickListener {
            val intent = Intent(context, compras_productos_vendedor::class.java).apply {
                putExtra("idProducto", productoClikado)
                putExtra("idTrabajador", idTrabajador)
            }
            context.startActivity(intent)
            dialog.dismiss()
        }
        constantesCarrito.setearDatosUsuarioImgNombre(idTrabajador) { nombre, img, apellido, nacionalidad, categoria, verificado, trabajador_user ->
            val db = FirebaseFirestore.getInstance()
                .collection("Trabajadores_Usuarios_Drivers").document("trabajadores")
                .collection("trabajadores").document(idTrabajador)
                .collection("productos_venta").document("publicados").collection("publicados")
                .document(productoClikado)
            dialog.setOnDismissListener {
                val tiempoCierre = System.currentTimeMillis()
                val tiempoAbiertoSegundos = (tiempoCierre - tiempoApertura) / 1000
                if (tiempoAbiertoSegundos > 20) {
                    constantesPublicidad.agregarCantidadClickAnuncios(db, "", Variables.vistas)
                }
                Toast.makeText(context, "se cerro en $tiempoAbiertoSegundos", Toast.LENGTH_SHORT)
                    .show()
            }
            constantesPublicidad.agregarCantidadClickAnuncios(db, "", "click")
            bindingProductosTrabajadores.compartirIcon.setOnClickListener {
                constantesPublicidad.agregarCantidadClickAnuncios(
                    db,
                    "",
                    "compartir"
                )
                crear_dinamick_link(
                    context,
                    idTrabajador,
                    productoClikado,
                    "Mira este producto publicado por $nombre $apellido",
                    "${bindingProductosTrabajadores.nombreProducto.text}"
                )
            }
        }

        val recicle = bindingProductosTrabajadores.carrucelImgProductosVentaUser
        val customLayoutManager = classcustomscrool(context, LinearLayoutManager.HORIZONTAL, false)
        recicle.layoutManager = customLayoutManager

        val db = FirebaseFirestore.getInstance().collection("Trabajadores_Usuarios_Drivers")
            .document("trabajadores").collection("trabajadores").document(idTrabajador)
            .collection("productos_venta").document("publicados").collection("publicados")
            .document(productoClikado)

        db.get().addOnSuccessListener { res ->
            if (res.exists()) {
                val data = res.data ?: emptyMap()
                bindingProductosTrabajadores.progressCarga.isVisible = true
                bindingProductosTrabajadores.nettScrollView.isVisible = false
                setearDatosdialogProductos(
                    idTrabajador,
                    context,
                    data,
                    bindingProductosTrabajadores
                ) { cargado ->
                    bindingProductosTrabajadores.progressCarga.isVisible = false
                    bindingProductosTrabajadores.nettScrollView.isVisible = true
                }
                obtenerMasProductosVenta(
                    context,
                    idTrabajador,
                    productoClikado,
                    bindingProductosTrabajadores, dialog
                )

            } else {
                println("no se encontraron datos del producto")
            }
        }.addOnFailureListener { e ->
            println("no se encontro ningun dato de producto $e")
        }

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
                .collection("publicados")
                .document(id_publicacion)
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

    fun obtenerMasProductosVenta(
        context: Context,
        idTrabajador: String,
        idProducto: String,
        bindingProductosVencidodos: BottomsheetProductosVendidosUserVerifiBinding,
        dialog_p: BottomSheetDialog
    ) {
        val tiempoApertura = System.currentTimeMillis()
//        Toast.makeText(context, "obtemos el productio obtneido$idProducto", Toast.LENGTH_SHORT)
//            .show()
        val db = FirebaseFirestore.getInstance()
            .collection("Trabajadores_Usuarios_Drivers").document("trabajadores")
            .collection("trabajadores").document(idTrabajador).collection("productos_venta")
            .document("publicados").collection("publicados")
        bindingProductosVencidodos.cargaProductosPromoTrabajos.cargandoContenido.isVisible = true
        bindingProductosVencidodos.cargaProductosPromoTrabajos.cambiarTextoTrabajosRealziadosTrabajosRecientes.text =
            "Productos"
        db.get().addOnSuccessListener { res ->

            listaProductosUSer.clear()
            for (datos in res) {
                val imgProducto = datos["img_url"] as? String ?: ""
                val descuentoActivo = datos["descuento"] as? Boolean ?: false
                val id = datos["id"] as? String ?: ""
                val cantidadDescuento = datos["cantidad_porcentaje_descuento"] as? Number ?: 0
                val precio_descuento = datos["precio_descuento"] as? Number ?: 0
                val precio = datos["precio"] as? Number ?: 0
                val nombre = datos["nombre"] as? String ?: ""
                if (id != idProducto) {
                    val item = dataclas_item_preview_art_comprar(
                        id,
                        imgProducto,
                        nombre,
                        precio,
                        precio_descuento,
                        descuentoActivo,
                        cantidadDescuento
                    )
                    listaProductosUSer.add(item)
                }
            }
            if (listaProductosUSer.isNotEmpty()) {
                listaProductosUSer.shuffle()
                inizializarCarruceBindig(
                    context,
                    idTrabajador,
                    listaProductosUSer,
                    bindingProductosVencidodos, dialog_p
                )
                bindingProductosVencidodos.cargaProductosPromoTrabajos.cargandoContenido.isVisible =
                    false
                bindingProductosVencidodos.cargaProductosPromoTrabajos.masTrabajosRealiados.isVisible =
                    true
                bindingProductosVencidodos.cargaProductosPromoTrabajos.linealNoSeEncontraron.isVisible =
                    false
            } else {
                Log.d("error obtenerDAtos", "No hay datos para mostrar")
                bindingProductosVencidodos.cargaProductosPromoTrabajos.cargandoContenido.isVisible =
                    false
                bindingProductosVencidodos.cargaProductosPromoTrabajos.masTrabajosRealiados.isVisible =
                    false
                bindingProductosVencidodos.cargaProductosPromoTrabajos.linealNoSeEncontraron.isVisible =
                    true
                bindingProductosVencidodos.cargaProductosPromoTrabajos.textoCambiarTrabajosOPublicaciones.text =
                    "No se encontro mas productos"
            }


        }.addOnFailureListener { e ->
            println("no se econtro productos publicados $e")
        }

    }


    fun setearDatosdialogProductos(
        idTrabajador: String,
        context: Context,
        data: Map<String, Any>,
        bindingProductosTrabajadores: BottomsheetProductosVendidosUserVerifiBinding,
        onComplete: (Boolean) -> Unit

    ) {
        var isCamposVisible = false
        try {
            val db = FirebaseFirestore.getInstance().collection("Trabajadores_Usuarios_Drivers")
                .document("trabajadores").collection("trabajadores").document(idTrabajador)
            db.get().addOnSuccessListener { res ->
                if (res.exists()) {
                    val data = res.data
                    val nombre_trabajador = data?.get("nombre") as? String ?: ""
                    val verificado = data?.get("verificado") as? Boolean ?: false
                    constantes_servicios.verificarEstado_vericiacion(
                        bindingProductosTrabajadores.iconoVerificado,
                        idTrabajador
                    ) { v, plan ->
                        when (plan) {
                            Variables.plaA -> {
                                bindingProductosTrabajadores.iconoVerificado.setImageResource(R.drawable.verificado_a)
                                bindingProductosTrabajadores.nombreTrabajador.text =
                                    "Vendido por : $nombre_trabajador"

                            }

                            Variables.planB -> {
                                bindingProductosTrabajadores.iconoVerificado.setImageResource(R.drawable.icon_verificado)
                                bindingProductosTrabajadores.nombreTrabajador.text =
                                    "Vendido por : $nombre_trabajador"
                            }

                            Variables.PlanC -> {
                                bindingProductosTrabajadores.iconoVerificado.setImageResource(R.drawable.verificado_c)
                                bindingProductosTrabajadores.nombreTrabajador.text =
                                    "Vendido por : $nombre_trabajador"


                            }
                        }

                    }
                }
            }
            val cantidadPorcentajeDescuento = data["cantidad_porcentaje_descuento"] as? Number ?: 0
            val precio = data["precio"] as? Number ?: 0
            val precioDescuento = data["precio_descuento"] as? Number ?: 0
            val totalProducto = data["total_producto"] as? Number ?: 0
            val categoria = data["categoria_producto"] as? String ?: ""
            val condicionProducto = data["condicion_producto"] as? String ?: ""
            val descripcion = data["descripcion"] as? String ?: ""
            val modelo = data["modelo"] as? String ?: ""
            val descuento = data["descuento"] as? Boolean ?: false
            val garantia = data["garantia"] as? String ?: ""
            val id = data["id"] as? String ?: ""
            val fechaPublicada = data["fechaPublicada"] as? String ?: ""
            val marca = data["marca"] as? String ?: ""
            val nombre = data["nombre"] as? String ?: ""
            val mas_informacio = data["mas_informacio"] as? String ?: ""
            val stok = data["stok"] as? String ?: ""
            val cantidad_porcentaje_descuento =
                data["cantidad_porcentaje_descuento"] as? Number ?: 0
            val metodoPago = data?.get("metodoPago") as? String ?: ""
            val metodoEntrega = data?.get("metodoEntrega") as? String ?: ""

            constantestextos_general.extender_acortar_texto(
                bindingProductosTrabajadores.camposProductosUserVerificados.descripcion,
                bindingProductosTrabajadores.camposProductosUserVerificados.tvReadMore
            )
            obtner_img_descripcion(context, idTrabajador, id, bindingProductosTrabajadores)
            obtener_metodosPaog(idTrabajador, metodoPago) { metodos_encontrados ->
                bindingProductosTrabajadores.camposProductosUserVerificados.metodosPago.text =
                    metodos_encontrados
            }
            obtener_metodoEntrega(
                idTrabajador, metodoEntrega,
                callback = { metodo_entrega ->
                    bindingProductosTrabajadores.camposProductosUserVerificados.metodoEntrega.text =
                        metodo_entrega
                },
                evio_gratis = { delivery_gratis ->
                    if (delivery_gratis) {
                        bindingProductosTrabajadores.envioGratis.isVisible = true
                    } else {
                        bindingProductosTrabajadores.envioGratis.isVisible = false
                    }
                }
            )


            bindingProductosTrabajadores.marcaProducto.text = marca

            if (marca.isNotEmpty() && modelo.isNotEmpty()) {
                bindingProductosTrabajadores.camposProductosUserVerificados.marca.text = marca
                bindingProductosTrabajadores.camposProductosUserVerificados.modelo.text = modelo
                bindingProductosTrabajadores.camposProductosUserVerificados.linealMarcaModelo.isVisible =
                    true
            } else {
                bindingProductosTrabajadores.camposProductosUserVerificados.linealMarcaModelo.isVisible =
                    false

            }
////            if (entrega_domicilio) {
////                bindingProductosTrabajadores.camposProductosUserVerificados.entregaDomicilio.text =
////                    "si"
//            } else {
//                bindingProductosTrabajadores.camposProductosUserVerificados.entregaDomicilio.text =
//                    "no"
//            }
            bindingProductosTrabajadores.masInfomacion.text = mas_informacio

            if (descuento) {
                constantestextos_general.setearPrecioDescuentoPrecioAntiguo(
                    precioDescuento,
                    bindingProductosTrabajadores.precioProducto,
                    precio,
                    bindingProductosTrabajadores.precioAntiguo,
                    cantidad_porcentaje_descuento,
                    bindingProductosTrabajadores.descuentoPorcentaje
                )
                constantestextos_general.marcarDescuentoTxt(bindingProductosTrabajadores.precioAntiguo)

                constantestextos_general.marcarDescuentoTxt(bindingProductosTrabajadores.camposProductosUserVerificados.precioAntiguo)
                constantestextos_general.setearPrecioDescuentoPrecioAntiguo(
                    precioDescuento,
                    bindingProductosTrabajadores.camposProductosUserVerificados.precioProducto,
                    precio,
                    bindingProductosTrabajadores.camposProductosUserVerificados.precioAntiguo,
                    cantidad_porcentaje_descuento,
                    bindingProductosTrabajadores.camposProductosUserVerificados.descuentoPorcentaje
                )

                bindingProductosTrabajadores.camposProductosUserVerificados.precioAntiguo.isVisible =
                    true
                bindingProductosTrabajadores.camposProductosUserVerificados.descuentoPorcentaje.isVisible =
                    true
                bindingProductosTrabajadores.precioAntiguo.isVisible =
                    true
                bindingProductosTrabajadores.descuentoPorcentaje.isVisible =
                    true
            } else {
                bindingProductosTrabajadores.camposProductosUserVerificados.precioAntiguo.isVisible =
                    false
                bindingProductosTrabajadores.camposProductosUserVerificados.descuentoPorcentaje.isVisible =
                    false
                bindingProductosTrabajadores.precioAntiguo.isVisible =
                    false
                bindingProductosTrabajadores.descuentoPorcentaje.isVisible =
                    false
                constantestextos_general.setearPrecioDescuentoPrecioAntiguo(
                    precio,
                    bindingProductosTrabajadores.camposProductosUserVerificados.precioProducto
                )

                constantestextos_general.setearPrecioDescuentoPrecioAntiguo(
                    precio,
                    bindingProductosTrabajadores.precioProducto
                )

            }


            obtener_textos_stylos(idTrabajador, id, bindingProductosTrabajadores)
            bindingProductosTrabajadores.camposProductosUserVerificados.categoriaProducto.text =
                categoria
            bindingProductosTrabajadores.nombreProducto.text = nombre
            bindingProductosTrabajadores.camposProductosUserVerificados.marca.text = marca
            bindingProductosTrabajadores.camposProductosUserVerificados.modelo.text = modelo
            bindingProductosTrabajadores.camposProductosUserVerificados.stok.text = "$stok UND"
            bindingProductosTrabajadores.camposProductosUserVerificados.garantia.text = garantia
            bindingProductosTrabajadores.camposProductosUserVerificados.Condicion.text =
                condicionProducto
            bindingProductosTrabajadores.camposProductosUserVerificados.descripcion.text =
                descripcion
            bindingProductosTrabajadores.camposProductosUserVerificados.fechaPublicado.text =
                fechaPublicada
            inizializarImgProductos(context, listaImg, bindingProductosTrabajadores, data)
            constantestextos_general.marcarDescuentoTxt(bindingProductosTrabajadores.camposProductosUserVerificados.precioAntiguo)
            bindingProductosTrabajadores.ocultarCamposDePublicidad.setOnClickListener {
                if (isCamposVisible) {
                    bindingProductosTrabajadores.camposProductosUserVerificados.linealVer.visibility =
                        View.GONE
                    bindingProductosTrabajadores.ocultarP1.setImageResource(R.drawable.ocultar_arriva)

                } else {
                    bindingProductosTrabajadores.camposProductosUserVerificados.linealVer.visibility =
                        View.VISIBLE
                    bindingProductosTrabajadores.ocultarP1.setImageResource(R.drawable.ocultar_abajo)
                }
                isCamposVisible = !isCamposVisible
            }

            bindingProductosTrabajadores.ocultarCamposDescripcion.setOnClickListener {
                if (isCamposVisible) {
                    bindingProductosTrabajadores.linealTextosDescripcion.visibility =
                        View.GONE
                    bindingProductosTrabajadores.ocultarP2.setImageResource(R.drawable.ocultar_arriva)

                } else {
                    bindingProductosTrabajadores.linealTextosDescripcion.visibility =
                        View.VISIBLE
                    bindingProductosTrabajadores.ocultarP2.setImageResource(R.drawable.ocultar_abajo)
                }
                isCamposVisible = !isCamposVisible
            }

            bindingProductosTrabajadores.ocultarCamposMasInformacion.setOnClickListener {
                if (isCamposVisible) {
                    bindingProductosTrabajadores.masInfomacion.visibility =
                        View.GONE
                    bindingProductosTrabajadores.ocultarP2.setImageResource(R.drawable.ocultar_arriva)

                } else {
                    bindingProductosTrabajadores.masInfomacion.visibility =
                        View.VISIBLE
                    bindingProductosTrabajadores.ocultarP2.setImageResource(R.drawable.ocultar_abajo)
                }
                isCamposVisible = !isCamposVisible
            }

            onComplete(true)
        } catch (e: Exception) {
            onComplete(false)
        }
    }

    fun obtener_metodoEntrega(
        idTrabajador: String,
        metodoEntrega: String,
        callback: (String) -> Unit,
        evio_gratis: (Boolean) -> Unit
    ) {
        val db = FirebaseFirestore.getInstance().collection("Trabajadores_Usuarios_Drivers")
            .document("trabajadores").collection("trabajadores").document(idTrabajador)
            .collection("metodos_entrega").document(metodoEntrega)

        db.get().addOnSuccessListener { res ->
            if (res.exists()) {
                val data = res.data
                val metodosDisponibles = mutableListOf<String>()

                val delivery = data?.get("delivery") as? Boolean ?: false
                val entregaProgramada = data?.get("entregaProgramada") as? Boolean ?: false
                val envioCourier = data?.get("envioCourier") as? Boolean ?: false
                val lugaresEntrega = data?.get("lugaresEntrega") as? Boolean ?: false
                val retiroTienda = data?.get("retiroTienda") as? Boolean ?: false
                val coordinar = data?.get("coordinar") as? Boolean ?: false

                if (delivery) metodosDisponibles.add("Delivery")
                if (entregaProgramada) metodosDisponibles.add("Entrega Programada")
                if (envioCourier) metodosDisponibles.add("Envío Courier")
                if (lugaresEntrega) metodosDisponibles.add("Lugares de Entrega")
                if (retiroTienda) metodosDisponibles.add("Retiro en Tienda")
                if (coordinar) metodosDisponibles.add("Coordinar")

                val resultadoTexto = metodosDisponibles.joinToString(", ")

                if (delivery) {
                    val datosDelivery = data?.get("datos_delivery") as? Map<*, *>
                    val esGratis = datosDelivery?.get("gratis") as? Boolean ?: false
                    evio_gratis(esGratis)
                }

                callback(resultadoTexto)
            } else {
                callback("") // Documento no existe
            }
        }.addOnFailureListener {
            callback("") // Error al obtener datos
        }
    }


    fun obtener_metodos_entrega_campos(
        linearLayout: LinearLayout,
        progressBar: ProgressBar,
        idTrabajador: String,
        metodoEntrega: String,
        datosExtra: (
            descripcionLugar: String,
            localidadLugar: String,
            nombreTienda: String,
            referenciaTienda: String,
            localidadTienda: String,

            ) -> Unit
    ) {
        progressBar.isVisible = true
        linearLayout.isVisible = false
        val tiempoInicio = System.currentTimeMillis()

        val db = FirebaseFirestore.getInstance().collection("Trabajadores_Usuarios_Drivers")
            .document("trabajadores").collection("trabajadores").document(idTrabajador)
            .collection("metodos_entrega").document(metodoEntrega)

        db.get().addOnSuccessListener { res ->
            val tiempoFin = System.currentTimeMillis()
            val tiempoTotal = tiempoFin - tiempoInicio
            progressBar.isVisible = false
            linearLayout.isVisible = true
            if (res.exists()) {
                val data = res.data

                val datosLugares = data?.get("datos_lugares_entrega") as? Map<*, *>
                val descripcionLugar = datosLugares?.get("descripcion") as? String ?: ""
                val localidadLugar = datosLugares?.get("localidad") as? String ?: ""

                val datosTienda = data?.get("datos_retiro_tienda") as? Map<*, *>
                val nombreTienda = datosTienda?.get("nombre_tienda") as? String ?: ""
                val referenciaTienda = datosTienda?.get("referencia") as? String ?: ""
                val localidadTienda = datosTienda?.get("localidad") as? String ?: ""

                datosExtra(
                    descripcionLugar,
                    localidadLugar,
                    nombreTienda,
                    referenciaTienda,
                    localidadTienda
                )
            } else {
                datosExtra("", "", "", "", "")
            }
        }.addOnFailureListener {
            val tiempoFin = System.currentTimeMillis()
            val tiempoTotal = tiempoFin - tiempoInicio
            progressBar.isVisible = false
            linearLayout.isVisible = false
            datosExtra("", "", "", "", "")
        }
    }


    fun obtener_metodosPaog(id_trabajador: String, id_metodo: String, callback: (String) -> Unit) {
        val db = FirebaseFirestore.getInstance().collection("Trabajadores_Usuarios_Drivers")
            .document("trabajadores").collection("trabajadores").document(id_trabajador)
            .collection("metodos_pago").document(id_metodo)

        db.get().addOnSuccessListener { res ->
            if (res.exists()) {
                val data = res.data

                val metodosDisponibles = mutableListOf<String>()

                if (data?.get("efectivo") as? Boolean == true) metodosDisponibles.add("Efectivo")
                if (data?.get("plin") as? Boolean == true) metodosDisponibles.add("Plin")
                if (data?.get("yape") as? Boolean == true) metodosDisponibles.add("Yape")
                if (data?.get("transferenia") as? Boolean == true) metodosDisponibles.add("Transferencia")
                if (data?.get("nombre_metodo") as? Boolean == true) metodosDisponibles.add("Otro")

                val resultadoTexto = metodosDisponibles.joinToString(", ")
                callback(resultadoTexto)
            } else {
                callback("") // Documento no existe
            }
        }.addOnFailureListener {
            callback("") // Error al obtener datos
        }
    }


    private fun obtner_img_descripcion(
        context: Context,
        id_trabajador: String,
        producto_id: String,
        bindingProductosTrabajadores: BottomsheetProductosVendidosUserVerifiBinding
    ) {
        val storageRef = FirebaseStorage.getInstance().reference

        val fileName = "imagen_caracteristica.jpg"

        val rutaImagen = storageRef
            .child("usuarios")
            .child(id_trabajador)
            .child("productos_venta")
            .child(producto_id)
            .child(fileName)

        rutaImagen.downloadUrl
            .addOnSuccessListener { uri ->
                val urlImagen = uri.toString()
                Log.d("DownloadURL", "URL de la imagen: $urlImagen")
                if (urlImagen.isNotEmpty()) {
                    bindingProductosTrabajadores.relativeImgContainer.isVisible = true
                    constatnes_carga_imagenes_general.changer_img(
                        bindingProductosTrabajadores.progreesIndicator,
                        context,
                        urlImagen,
                        null,
                        bindingProductosTrabajadores.imgSubir,
                        "portada",
                        null
                    ) { completado ->

                    }
                } else {
                    bindingProductosTrabajadores.relativeImgContainer.isVisible = false
                }

            }
            .addOnFailureListener { e ->
                Log.e("DownloadURL", "Error al obtener la URL", e)
            }

    }

    private fun inizializarCarruceBindig(
        context: Context,
        idTrabajador: String,
        lista_productosVentaUSer: MutableList<dataclas_item_preview_art_comprar>,
        bindingProductosTrabajadores: BottomsheetProductosVendidosUserVerifiBinding,
        dialog: BottomSheetDialog
    ) {
        val recicle = bindingProductosTrabajadores.cargaProductosPromoTrabajos.masTrabajosRealiados
        recicle.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)

        val adapter = adapter_mostra_articulos_trabajadores(lista_productosVentaUSer) { item ->
            cargarDatosNuevaMente(
                context,
                idTrabajador,
                item.id.toString(),
                bindingProductosTrabajadores, dialog
            )
        }
        recicle.adapter = adapter

    }

    private fun cargarDatosNuevaMente(
        context: Context,
        idTrabajador: String,
        productoClikado: String,
        bindingProductosVencidodos: BottomsheetProductosVendidosUserVerifiBinding,
        dialog_p: BottomSheetDialog,
    ) {
        val tiempoApertura = System.currentTimeMillis()
        Toast.makeText(context, "$productoClikado", Toast.LENGTH_SHORT).show()
        bindingProductosVencidodos.progressCarga.isVisible = true
        bindingProductosVencidodos.netScrollView.isVisible = false
        val db = FirebaseFirestore.getInstance().collection("Trabajadores_Usuarios_Drivers")
            .document("trabajadores").collection("trabajadores").document(idTrabajador)
            .collection("productos_venta").document("publicados").collection("publicados")
            .document(productoClikado)
        listaProductosUSer.clear()
        db.get().addOnSuccessListener { res ->
            dialog_p.setOnDismissListener {
                val tiempoCierre = System.currentTimeMillis()
                val tiempoAbiertoSegundos = (tiempoCierre - tiempoApertura) / 1000
                if (tiempoAbiertoSegundos > 20) {
                    constantesPublicidad.agregarCantidadClickAnuncios(db, "", Variables.vistas)
                }
                Toast.makeText(context, "se cerro en $tiempoAbiertoSegundos", Toast.LENGTH_SHORT)
                    .show()
            }
            constantesCarrito.setearDatosUsuarioImgNombre(idTrabajador) { nombre, img, apellido, nacionalidad, categoria, verificado, trabajador_user ->
                constantesPublicidad.agregarCantidadClickAnuncios(db, "", "click")
                bindingProductosVencidodos.compartirIcon.setOnClickListener {
                    constantesPublicidad.agregarCantidadClickAnuncios(
                        db,
                        "",
                        "compartir"
                    )
                    crear_dinamick_link(
                        context,
                        idTrabajador,
                        productoClikado,
                        "Mira este producto publicado por $nombre $apellido",
                        "${bindingProductosVencidodos.nombreProducto.text}"
                    )
                }
            }

            if (res.exists()) {
                val data = res.data ?: emptyMap()
                bindingProductosVencidodos.progressCarga.isVisible = true
                bindingProductosVencidodos.netScrollView.isVisible = false
                setearDatosdialogProductos(
                    idTrabajador,
                    context,
                    data,
                    bindingProductosVencidodos
                ) { completado ->
                    if (completado) {
                        bindingProductosVencidodos.progressCarga.isVisible = false
                        bindingProductosVencidodos.netScrollView.isVisible = true
                    } else {
                        bindingProductosVencidodos.progressCarga.isVisible = true
                        bindingProductosVencidodos.netScrollView.isVisible = false
                    }

                }
                obtenerMasProductosVenta(
                    context,
                    idTrabajador,
                    productoClikado,
                    bindingProductosVencidodos, dialog_p
                )
            } else {

            }
        }.addOnFailureListener { e ->

            println("no se econtro datos del producto")
        }

    }


    fun inizializarImgProductos(
        context: Context,
        listaImg: MutableList<dataclassMostarImgProductosVendedor>,
        bindingProductosTrabajadores: BottomsheetProductosVendidosUserVerifiBinding,
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
        val recicle = bindingProductosTrabajadores.carrucelImgProductosVentaUser
        if (recicle.adapter == null) {
            recicle.layoutManager =
                LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            recicle.adapter = adapterInicializarRecycleimgProductosTrabajadores(listaImg)
        } else {
            recicle.adapter?.notifyDataSetChanged()
        }
    }


    private fun inicializarRecicle(
        recycle: RecyclerView,
        adapter: RecyclerView.Adapter<*>,
        context: Context
    ) {
        recycle.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        recycle.adapter = adapter
    }


    fun showBottomShetDialogAnuncios(
        idTrabajador: String,
        trabajo: CollectionReference,
        context: Context,
        Nombre_trabajador: String,
        listaMas_promo: MutableList<dataclass_adapter_promociones>,
        lifecycle: Lifecycle,
        item: dataclass_adapter_promociones, dialog: BottomSheetDialog
    ) {
        val tiempoApertura = System.currentTimeMillis()
        val tiempoInicio = System.currentTimeMillis()
        val bindingMostrar =
            BottomSheetMostarTrabajosRecientesBinding.inflate(LayoutInflater.from(context))
        dialog.setContentView(bindingMostrar.root)
        bindingMostrar.cargarConteindo.isVisible = true
        bindingMostrar.linealGeneralLinea.isVisible = false
        bindingMostrar.cerrar.setOnClickListener {
            dialog.dismiss()
        }
        trabajo.document(item.id.toString()).get().addOnSuccessListener { res ->
            val tiempoFin = System.currentTimeMillis()
            val tiempoTotalMs = tiempoFin - tiempoInicio
            val tiempoEnSegundos = tiempoTotalMs / 1000.0
            if (res.exists()) {
                val data = res.data
                // Obtener valores del mapa, asegurando que se conviertan a String si es necesario
                val titulo = data?.get("titulo") as? String ?: "Sin título"
                val contenido = data?.get("contenido") as? String ?: "Sin contenido"
                val idSelecionado = data?.get("id") as? String ?: ""
                val fecha_rec = data?.get("fecha_rec") as? String ?: ""
                val hora_rec = data?.get("hora_rec") as? String ?: ""
                val img_url = data?.get("img_url") as? String ?: ""
                val img_url2 = data?.get("img_url2") as? String ?: ""
                val img_url3 = data?.get("img_url3") as? String ?: ""
                val img_url4 = data?.get("img_url4") as? String ?: ""
                val listaImg =
                    listOf(img_url, img_url2, img_url3, img_url4).filter { it.isNotEmpty() }
                bindingMostrar.tituloNombreTrabajador.text =
                    "Trabajos realizados por $Nombre_trabajador"
                // Configurar botón para ver todos los trabajos
                bindingMostrar.cargaProductosPromoTrabajos.verTodosTrabajos.setOnClickListener {
                    val intent = Intent(context, mostrarTodosTrabajos::class.java).apply {
                        putExtra("idTrabajador", idTrabajador)
                        putExtra("fecha_rec", fecha_rec)
                        putExtra("hora_rec", hora_rec)
                    }
                    context.startActivity(intent)
                    dialog.dismiss()
                }
                // Ocultar el texto después del mismo tiempo que tomó cargar los datos
                Handler(Looper.getMainLooper()).postDelayed({
                    bindingMostrar.cargarConteindo.isVisible = false
                    bindingMostrar.linealGeneralLinea.isVisible = true
                }, tiempoTotalMs)

                // Configurar el texto expandible
                constantestextos_general.extender_acortar_texto(
                    bindingMostrar.textoTrabajosRealzados,
                    bindingMostrar.tvReadMore
                )

                bindingMostrar.textoTrabajosRealzados.text = contenido
                bindingMostrar.tituloTrabajosRealizados.text = titulo
                constantesCarrito.setearDatosUsuarioImgNombre(idTrabajador) { nombre, img, apellido, nacionalidad, categoria, verificado, trabajador_user ->
                    dialog.setOnDismissListener {
                        val tiempoCierre = System.currentTimeMillis()
                        val tiempoAbiertoSegundos = (tiempoCierre - tiempoApertura) / 1000
                        if (tiempoAbiertoSegundos > 20) {
                            constantesPublicidad.agregarCantidadClickAnuncios( trabajo.document(item.id.toString()), "", Variables.vistas)
                        }
                        Toast.makeText(context, "se cerro en $tiempoAbiertoSegundos", Toast.LENGTH_SHORT)
                            .show()
                    }
                    constantesPublicidad.agregarCantidadClickAnuncios( trabajo.document(item.id.toString()), "", "click")
                    bindingMostrar.compartirIcon.setOnClickListener {
                        constantesPublicidad.agregarCantidadClickAnuncios(
                            trabajo.document(item.id.toString()),
                            "",
                            "compartir"
                        )
                        crear_dinamick_link_prblicaciones_trabajador(
                            context,
                            idTrabajador,
                            item.id.toString(),
                            "Mira esta publicacion relizada por $nombre $apellido",
                            "${item.titulo_promo}"
                        )
                    }
                }

                // Configurar el carrusel de imágenes si hay imágenes disponibles
                if (listaImg.isNotEmpty()) {
                    val carouselItems = listaImg.map { CarouselItem(it) }
                    bindingMostrar.carruselImgTrabajos.registerLifecycle(lifecycle)
                    bindingMostrar.carruselImgTrabajos.carouselListener =
                        object : CarouselListener {
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
                                    setImage(item, R.drawable.ic_wb_cloudy_with_padding)
                                    minimumScale = 1f
                                    maximumScale = 10f
                                    mediumScale = 5f
                                }
                            }

                        }
                    bindingMostrar.carruselImgTrabajos.setData(carouselItems)


                }

                // Obtener más trabajos relacionados
                obtenerMasTrabajosRealiazdos(
                    context,
                    idTrabajador,
                    bindingMostrar,
                    idSelecionado,
                    listaMas_promo,
                    lifecycle,dialog
                )
            }
        }


    }

    fun crear_dinamick_link_prblicaciones_trabajador(
        contex: Context,
        idTrabajador: String,
        id_publicacion: String,
        titulo_dinamick: String,
        texto_dinamick: String
    ) {
        val userCollections =
            FirebaseFirestore.getInstance().collection(Variables.trabajadores_usuariosDB)
                .document(Variables.trabajadoresDB).collection(Variables.trabajadoresDB)
                .document(idTrabajador).collection("publicaciones_trabajos").document("publicados")
                .collection("publicados")
                .document(id_publicacion)
        userCollections.get().addOnSuccessListener { res ->
            if (res.exists()) {
                val data = res.data
                val img_url = data?.get("img_url") as? String ?: ""
                val titulo = data?.get("titulo") as? String ?: ""
                Log.d("idpublicacones", "$id_publicacion ,$idTrabajador ,$img_url")
                Firebase.dynamicLinks.shortLinkAsync {
                    link =
                        Uri.parse("https://geinzapp.page.link/?idTrabajadorVeri=${idTrabajador}&idpublicacion=${id_publicacion}")
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
                println("El anuncio no existe.")
            }
        }.addOnFailureListener { exception ->
            println("Error al obtener el anuncio: ${exception.message}")
        }
    }

    fun obtenerMasTrabajosRealiazdos(
        context: Context,
        idTrabajador: String,
        bindingMostrarTRabajos: BottomSheetMostarTrabajosRecientesBinding,
        idSelecionado: String,
        listaMas_promo: MutableList<dataclass_adapter_promociones>, lifecycle: Lifecycle,dialog_p: BottomSheetDialog
    ) {
        val db = FirebaseFirestore.getInstance()
            .collection("Trabajadores_Usuarios_Drivers").document("trabajadores")
            .collection("trabajadores").document(idTrabajador).collection("publicaciones_trabajos")
            .document("publicados").collection("publicados")
        bindingMostrarTRabajos.cargaProductosPromoTrabajos.cargandoContenido.isVisible = true
        bindingMostrarTRabajos.cargaProductosPromoTrabajos.cambiarTextoTrabajosRealziadosTrabajosRecientes.text =
            "Trabajos recientes"
        db.get().addOnSuccessListener { res ->
            listaMas_promo.clear()

            for (datos in res) {
                val data = datos.data
                val img_url = data?.get("img_url") as? String ?: ""
                val titulo = data?.get("titulo") as? String ?: ""
                val contenido = data?.get("contenido") as? String ?: ""
                val id = data?.get("id") as? String ?: ""
                val fecha = data?.get("fecha_rec") as? String ?: ""
                val hora = data?.get("hora_rec") as? String ?: ""
                val img_url2 = data?.get("img_url2") as? String ?: ""
                val img_url3 = data?.get("img_url3") as? String ?: ""
                val img_url4 = data?.get("img_url4") as? String ?: ""


                // Filtrar para que no se agregue el idSeleccionado
                if (id != idSelecionado) {
                    val dataClass =
                        dataclass_adapter_promociones(
                            img_url,
                            img_url2,
                            img_url3,
                            img_url4,
                            titulo,
                            contenido,
                            id,
                            fecha,
                            hora
                        )
                    listaMas_promo.add(dataClass)
                }
            }

            if (listaMas_promo.isNotEmpty()) {
                listaMas_promo.shuffle() // Mezclar los datos antes de mostrarlos
                inicializarTrabajosRealizados(
                    idTrabajador,
                    bindingMostrarTRabajos,
                    context,
                    listaMas_promo,
                    lifecycle,dialog_p
                )
                bindingMostrarTRabajos.cargaProductosPromoTrabajos.linealNoSeEncontraron.isVisible =
                    false
                bindingMostrarTRabajos.cargaProductosPromoTrabajos.masTrabajosRealiados.isVisible =
                    true
                bindingMostrarTRabajos.cargaProductosPromoTrabajos.cargandoContenido.isVisible =
                    false
            } else {
                Log.d("error obtenerDAtos", "No hay datos para mostrar")
                bindingMostrarTRabajos.cargaProductosPromoTrabajos.linealNoSeEncontraron.isVisible =
                    true
                bindingMostrarTRabajos.cargaProductosPromoTrabajos.masTrabajosRealiados.isVisible =
                    false
                bindingMostrarTRabajos.cargaProductosPromoTrabajos.cargandoContenido.isVisible =
                    false
                bindingMostrarTRabajos.cargaProductosPromoTrabajos.textoCambiarTrabajosOPublicaciones.text =
                    "No se encontro mas trabajos recientes"
            }
        }.addOnFailureListener { e ->
            println("error al encontrar $e")
        }
    }

    private fun obtener_textos_stylos(
        idTrabajador: String,
        idProducto: String,
        bindingProductosTrabajadores: BottomsheetProductosVendidosUserVerifiBinding
    ) {
        val db = FirebaseFirestore.getInstance().collection("Trabajadores_Usuarios_Drivers")
            .document("trabajadores").collection("trabajadores").document(idTrabajador)
            .collection("productos_venta").document("publicados").collection("publicados")
            .document(idProducto)

        db.get().addOnSuccessListener { res ->
            if (res.exists()) {
                val data = res.data

                // Mapas
                val descripcionTextoMap = data?.get("descripcion_texto") as? Map<*, *>
                val descripcionTituloMap = data?.get("descripcion_titulo") as? Map<*, *>

                // Arrays
                val hashtagsGenerales = data?.get("hashtags_generales") as? List<*>
                val listaTextoDescripcion = data?.get("descripcion_texto_lista") as? List<*>

                // Convertir arrays a listas seguras de Strings
                val listaHashtags = hashtagsGenerales?.mapNotNull { it as? String } ?: emptyList()
                val listaFrases = listaTextoDescripcion?.mapNotNull { it as? String } ?: emptyList()

                // Obtener valores del mapa de título
                val titulo = descripcionTituloMap?.get("titulo_descripcion") as? String ?: ""
                val fuenteTextoTitulo =
                    descripcionTituloMap?.get("titulo_valor_style") as? String ?: ""
                val mayusMinusTitulo = descripcionTituloMap?.get("titulo_mayus") as? String ?: ""

                // Obtener valores del mapa de descripción
                val textoDescripcion =
                    descripcionTextoMap?.get("texto_descripcion") as? String ?: ""
                val fuenteTextoDescripcion =
                    descripcionTextoMap?.get("texto_valor_style") as? String ?: ""
                val mayusMinusDescripcion = descripcionTextoMap?.get("texto_mayus") as? String ?: ""

                // Llamar a la función con todos los datos necesarios
                editar_setar_valores_campos(
                    bindingProductosTrabajadores,
                    titulo,
                    fuenteTextoTitulo,
                    mayusMinusTitulo,
                    textoDescripcion,
                    fuenteTextoDescripcion,
                    mayusMinusDescripcion,
                    listaFrases
                )
            }
        }
    }


    private fun editar_setar_valores_campos(
        bindingProductosTrabajadores: BottomsheetProductosVendidosUserVerifiBinding,
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
        bindingProductosTrabajadores.vistraPreviaDescripciontitulo.text = tituloFormateado
        when (fuenteTextoTitulo) {
            "Bold" -> {
                bindingProductosTrabajadores.vistraPreviaDescripciontitulo.setTypeface(
                    null,
                    Typeface.BOLD
                )
                bindingProductosTrabajadores.vistraPreviaDescripciontitulo.paintFlags =
                    bindingProductosTrabajadores.vistraPreviaDescripciontitulo.paintFlags and Paint.UNDERLINE_TEXT_FLAG.inv()
            }

            "Cursiva" -> {
                bindingProductosTrabajadores.vistraPreviaDescripciontitulo.setTypeface(
                    null,
                    Typeface.ITALIC
                )
                bindingProductosTrabajadores.vistraPreviaDescripciontitulo.paintFlags =
                    bindingProductosTrabajadores.vistraPreviaDescripciontitulo.paintFlags and Paint.UNDERLINE_TEXT_FLAG.inv()
            }

            "Subrayado" -> {
                bindingProductosTrabajadores.vistraPreviaDescripciontitulo.setTypeface(
                    null,
                    Typeface.NORMAL
                )
                bindingProductosTrabajadores.vistraPreviaDescripciontitulo.paintFlags =
                    bindingProductosTrabajadores.vistraPreviaDescripciontitulo.paintFlags or Paint.UNDERLINE_TEXT_FLAG
            }

            else -> {
                bindingProductosTrabajadores.vistraPreviaDescripciontitulo.setTypeface(
                    null,
                    Typeface.NORMAL
                )
                bindingProductosTrabajadores.vistraPreviaDescripciontitulo.paintFlags =
                    bindingProductosTrabajadores.vistraPreviaDescripciontitulo.paintFlags and Paint.UNDERLINE_TEXT_FLAG.inv()
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

        bindingProductosTrabajadores.vistraPreviaDescripcion.text = spannable

    }

    private fun inicializarTrabajosRealizados(
        idTrabajador: String,
        bindingMostrarTRabajos: BottomSheetMostarTrabajosRecientesBinding,
        context: Context,
        listaMas_promo: MutableList<dataclass_adapter_promociones>,
        lifecycle: Lifecycle,dialog_p: BottomSheetDialog
    ) {
        val recicle = bindingMostrarTRabajos.cargaProductosPromoTrabajos.masTrabajosRealiados
        recicle.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        recicle.adapter = adapter_trabajos_realizados_trabajador(
            false,
            listaMas_promo
        ) { item ->
            cagrarDatosNuevamente(
                idTrabajador, context,
                item,
                bindingMostrarTRabajos,
                listaMas_promo,
                lifecycle,dialog_p
            )
        }

    }

    fun cagrarDatosNuevamente(
        idTrabajador: String,
        context: Context,
        item: dataclass_adapter_promociones,
        bindingMostrarTRabajos: BottomSheetMostarTrabajosRecientesBinding,
        listaMas_promo: MutableList<dataclass_adapter_promociones>,
        lifecycle: Lifecycle,dialog_p: BottomSheetDialog
    ) {
        val tiempoApertura = System.currentTimeMillis()
        bindingMostrarTRabajos.cargarConteindo.isVisible = true // Mostrar el ProgressBar
        bindingMostrarTRabajos.scollView.isVisible = false

        constantestextos_general.extender_acortar_texto(
            bindingMostrarTRabajos.textoTrabajosRealzados,
            bindingMostrarTRabajos.tvReadMore
        )
        bindingMostrarTRabajos.textoTrabajosRealzados.text = item.texto_promo
        bindingMostrarTRabajos.tituloTrabajosRealizados.text = item.titulo_promo
        println("El item seleccionado fue el ${item.id}")

        val db = FirebaseFirestore.getInstance()
            .collection("Trabajadores_Usuarios_Drivers").document("trabajadores")
            .collection("trabajadores").document(idTrabajador)
            .collection("publicaciones_trabajos").document("publicados")
            .collection("publicados").document(item.id.toString())

        constantesCarrito.setearDatosUsuarioImgNombre(idTrabajador) { nombre, img, apellido, nacionalidad, categoria, verificado, trabajador_user ->
            dialog_p.setOnDismissListener {
                val tiempoCierre = System.currentTimeMillis()
                val tiempoAbiertoSegundos = (tiempoCierre - tiempoApertura) / 1000
                if (tiempoAbiertoSegundos > 20) {
                    constantesPublicidad.agregarCantidadClickAnuncios(db, "", Variables.vistas)
                }
                Toast.makeText(context, "se cerro en $tiempoAbiertoSegundos", Toast.LENGTH_SHORT)
                    .show()
            }
            constantesPublicidad.agregarCantidadClickAnuncios(db, "", "click")
            bindingMostrarTRabajos.compartirIcon.setOnClickListener {
                constantesPublicidad.agregarCantidadClickAnuncios(
                    db,
                    "",
                    "compartir"
                )
                crear_dinamick_link_prblicaciones_trabajador(
                    context,
                    idTrabajador,
                    item.id.toString(),
                    "Mira esta publicacion relizada por $nombre $apellido",
                    "${item.titulo_promo}"
                )
            }
        }

        // Filtrar la lista excluyendo el item seleccionado
        val nuevaLista = listaMas_promo.filter { it.id != item.id }.toMutableList()

        // Mezclar la nueva lista para que el orden siga siendo aleatorio
        nuevaLista.shuffle()

        // Inicializar RecyclerView con la lista actualizada
        bindingMostrarTRabajos.cargaProductosPromoTrabajos.masTrabajosRealiados.adapter =
            adapter_trabajos_realizados_trabajador(
                false,
                nuevaLista
            ) { nuevoItem ->
                cagrarDatosNuevamente(
                    idTrabajador,
                    context,
                    nuevoItem,
                    bindingMostrarTRabajos,
                    listaMas_promo,
                    lifecycle,dialog_p
                )
            }
        val listaImg =
            listOf(item.img, item.img2, item.img3, item.img4)
        // Configurar el carrusel de imágenes si hay imágenes disponibles
        if (listaImg.isNotEmpty()) {
            Handler(Looper.getMainLooper()).postDelayed({
                val carouselItems = listaImg.map { CarouselItem(it) }
                bindingMostrarTRabajos.carruselImgTrabajos.apply {
                    registerLifecycle(lifecycle)
                    setData(carouselItems)
                }
                bindingMostrarTRabajos.cargarConteindo.isVisible = false
                bindingMostrarTRabajos.scollView.isVisible = true
                constantestextos_general.extender_acortar_texto(
                    bindingMostrarTRabajos.textoTrabajosRealzados,
                    bindingMostrarTRabajos.tvReadMore
                )
            }, 2000)

        } else {
            bindingMostrarTRabajos.cargaProductosPromoTrabajos.linealNoSeEncontraron.isVisible =
                true
            bindingMostrarTRabajos.cargaProductosPromoTrabajos.masTrabajosRealiados.isVisible =
                false
        }

    }


}