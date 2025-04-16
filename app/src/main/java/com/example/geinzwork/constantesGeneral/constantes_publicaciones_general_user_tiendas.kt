package com.geinzz.geinzwork.constantesGeneral

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
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
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
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
        if (plan == Variables.plaA) {
            binding.noSeEncontroPublicaciones.isVisible = false
            binding.linealProductosPublicados.isVisible = false
            return
        }
        if (plan == Variables.planB || plan == Variables.PlanC) {
            val db = FirebaseFirestore.getInstance().collection(Variables.trabajadores_usuariosDB)
                .document(Variables.trabajadoresDB).collection(Variables.trabajadoresDB)
                .document(id)
                .collection(Variables.trabajos_realizados)
            binding.linealProductosPublicados.isVisible = true
            obtenerARticulosComprasVerificado(binding, context, id)
            lista.clear()

            db.get().addOnSuccessListener { res ->
                val listaTemporal = mutableListOf<dataclas_trabajos_ralizados>()
                for (datos in res) {
                    val data = datos.data
                    val trabajoRealizado = dataclas_trabajos_ralizados(
                        data?.get(Variables.imageUrl) as? String ?: "",
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
                    adapter.notifyDataSetChanged() // Notificar cambios en la lista
                }
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
            .collection("productos_venta")

        db.get().addOnSuccessListener { res ->
            val listaTemporal = mutableListOf<dataclas_item_preview_art_comprar>()

            for (datos in res) {
                val data = datos.data
                val imgProducto = data["img_principal"] as? String ?: ""
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
        bindingProductosTrabajadores.cargaProductosPromoTrabajos.verTodosTrabajos.setOnClickListener {
            val intent =
                Intent(context, ver_mas_productos_publicados_trabajadores::class.java).apply {
                    putExtra("idTrabajador", idTrabajador)
                }
            context.startActivity(intent)
            dialog.dismiss()
        }
        bindingProductosTrabajadores.camposProductosUserVerificados.comprar.setOnClickListener {
            val intent = Intent(context, compras_productos_vendedor::class.java).apply {
                putExtra("idProducto", productoClikado)
                putExtra("idTrabajador", idTrabajador)
            }
            context.startActivity(intent)
            dialog.dismiss()
        }
        val recicle = bindingProductosTrabajadores.carrucelImgProductosVentaUser
        val customLayoutManager = classcustomscrool(context, LinearLayoutManager.HORIZONTAL, false)
        recicle.layoutManager = customLayoutManager

        val db = FirebaseFirestore.getInstance().collection("Trabajadores_Usuarios_Drivers")
            .document("trabajadores").collection("trabajadores").document(idTrabajador)
            .collection("productos_venta").document(productoClikado)

        db.get().addOnSuccessListener { res ->
            if (res.exists()) {
                val data = res.data ?: emptyMap()
                bindingProductosTrabajadores.progressCarga.isVisible = true
                bindingProductosTrabajadores.nettScrollView.isVisible = false
                setearDatosdialogProductos(context, data, bindingProductosTrabajadores) { cargado ->
                    bindingProductosTrabajadores.progressCarga.isVisible = false
                    bindingProductosTrabajadores.nettScrollView.isVisible = true
                }
                obtenerMasProductosVenta(
                    context,
                    idTrabajador,
                    productoClikado,
                    bindingProductosTrabajadores
                )

            } else {
                println("no se encontraron datos del producto")
            }
        }.addOnFailureListener { e ->
            println("no se encontro ningun dato de producto $e")
        }

    }

    fun obtenerMasProductosVenta(
        context: Context,
        idTrabajador: String,
        idProducto: String,
        bindingProductosVencidodos: BottomsheetProductosVendidosUserVerifiBinding
    ) {
        val db = FirebaseFirestore.getInstance()
            .collection("Trabajadores_Usuarios_Drivers").document("trabajadores")
            .collection("trabajadores").document(idTrabajador).collection("productos_venta")
        bindingProductosVencidodos.cargaProductosPromoTrabajos.cargandoContenido.isVisible = true
        bindingProductosVencidodos.cargaProductosPromoTrabajos.cambiarTextoTrabajosRealziadosTrabajosRecientes.text =
            "Productos"
        db.get().addOnSuccessListener { res ->
            listaProductosUSer.clear()
            for (datos in res) {
                val imgProducto = datos["img_principal"] as? String ?: ""
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
                    bindingProductosVencidodos
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
        context: Context,
        data: Map<String, Any>,
        bindingProductosTrabajadores: BottomsheetProductosVendidosUserVerifiBinding,
        onComplete: (Boolean) -> Unit

    ) {
        try {
            val cantidadPorcentajeDescuento = data["cantidad_porcentaje_descuento"] as? Number ?: 0
            val precio = data["precio"] as? Number ?: 0
            val precioDescuento = data["precio_descuento"] as? Number ?: 0
            val totalProducto = data["total_producto"] as? Number ?: 0

            constantestextos_general.extender_acortar_texto(
                bindingProductosTrabajadores.camposProductosUserVerificados.descripcion,
                bindingProductosTrabajadores.camposProductosUserVerificados.tvReadMore
            )

            val categoria = data["categoria"] as? String ?: ""
            val condicionProducto = data["condicion_producto"] as? String ?: ""
            val descripcion = data["descripcion"] as? String ?: ""


            val modelo = data["modelo"] as? String ?: ""
            val descuento = data["descuento"] as? Boolean ?: false
            val efectivo = data["efectivo"] as? Boolean ?: false
            val entrega_domicilio = data["entrega_domicilio"] as? Boolean ?: true

            val garantia = data["garantia"] as? String ?: ""
            val id = data["id"] as? String ?: ""
            val fechaPublicada = data["fechaPublicada"] as? String ?: ""

            val lugarDeEntrega = data["lugarEntrega"] as? String ?: ""
            val marca = data["marca"] as? String ?: ""
            val nombre = data["nombre"] as? String ?: ""
            val plin = data["plin"] as? Boolean ?: false
            val stok = data["stok"] as? String ?: ""
            val yape = data["yape"] as? Boolean ?: false
            val cantidad_porcentaje_descuento =
                data["cantidad_porcentaje_descuento"] as? Number ?: 0

            if (entrega_domicilio) {
                bindingProductosTrabajadores.camposProductosUserVerificados.entregaDomicilio.text = "si"
            } else {
                bindingProductosTrabajadores.camposProductosUserVerificados.entregaDomicilio.text = "no"
            }
            if (descuento) {
                constantestextos_general.marcarDescuentoTxt(bindingProductosTrabajadores.camposProductosUserVerificados.precioAntiguo)


                constantestextos_general.setearPrecioDescuentoPrecioAntiguo(
                    precioDescuento,
                    bindingProductosTrabajadores.camposProductosUserVerificados.precioProducto,
                    precio,
                    bindingProductosTrabajadores.camposProductosUserVerificados.precioAntiguo,
                    cantidad_porcentaje_descuento,
                    bindingProductosTrabajadores.camposProductosUserVerificados.descuentoPorcentaje
                )

                bindingProductosTrabajadores.camposProductosUserVerificados.precioAntiguo.isVisible = true
                bindingProductosTrabajadores.camposProductosUserVerificados.descuentoPorcentaje.isVisible = true
            } else {
                bindingProductosTrabajadores.camposProductosUserVerificados.precioAntiguo.isVisible = false
                bindingProductosTrabajadores.camposProductosUserVerificados.descuentoPorcentaje.isVisible = false
                constantestextos_general.setearPrecioDescuentoPrecioAntiguo(
                    precio,
                    bindingProductosTrabajadores.camposProductosUserVerificados.precioProducto
                )

            }

            bindingProductosTrabajadores.camposProductosUserVerificados.categoriaProducto.text = categoria
            bindingProductosTrabajadores.nombreProducto.text = nombre
            bindingProductosTrabajadores.camposProductosUserVerificados.marca.text = marca
            bindingProductosTrabajadores.camposProductosUserVerificados.modelo.text = modelo
            bindingProductosTrabajadores.camposProductosUserVerificados.stok.text = stok
            bindingProductosTrabajadores.camposProductosUserVerificados.garantia.text = garantia
            bindingProductosTrabajadores.camposProductosUserVerificados.Condicion.text = condicionProducto
            bindingProductosTrabajadores.camposProductosUserVerificados.descripcion.text = descripcion
            bindingProductosTrabajadores.camposProductosUserVerificados.fechaPublicado.text = fechaPublicada
            inizializarImgProductos(context, listaImg, bindingProductosTrabajadores, data)
            constantestextos_general.marcarDescuentoTxt(bindingProductosTrabajadores.camposProductosUserVerificados.precioAntiguo)


            onComplete(true)
        } catch (e: Exception) {
            onComplete(false)
        }
    }

    private fun inizializarCarruceBindig(
        context: Context,
        idTrabajador: String,
        lista_productosVentaUSer: MutableList<dataclas_item_preview_art_comprar>,
        bindingProductosTrabajadores: BottomsheetProductosVendidosUserVerifiBinding
    ) {
        val recicle = bindingProductosTrabajadores.cargaProductosPromoTrabajos.masTrabajosRealiados
        recicle.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)

        val adapter = adapter_mostra_articulos_trabajadores(lista_productosVentaUSer) { item ->
            cargarDatosNuevaMente(
                context,
                idTrabajador,
                item.id.toString(),
                bindingProductosTrabajadores
            )
        }
        recicle.adapter = adapter

    }

    private fun cargarDatosNuevaMente(
        context: Context,
        idTrabajador: String,
        productoClikado: String,
        bindingProductosVencidodos: BottomsheetProductosVendidosUserVerifiBinding
    ) {
        bindingProductosVencidodos.progressCarga.isVisible = true
        bindingProductosVencidodos.netScrollView.isVisible = false
        val db = FirebaseFirestore.getInstance().collection("Trabajadores_Usuarios_Drivers")
            .document("trabajadores").collection("trabajadores").document(idTrabajador)
            .collection("productos_venta").document(productoClikado)
        listaProductosUSer.clear()
        db.get().addOnSuccessListener { res ->
            if (res.exists()) {
                val data = res.data ?: emptyMap()
                bindingProductosVencidodos.progressCarga.isVisible = true
                bindingProductosVencidodos.netScrollView.isVisible = false
                setearDatosdialogProductos(
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
                    bindingProductosVencidodos
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
        val imgPrincipal = data["img_principal"] as? String ?: ""
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
        item: dataclass_adapter_promociones,dialog: BottomSheetDialog
    ) {
        val bindingMostrar =
            BottomSheetMostarTrabajosRecientesBinding.inflate(LayoutInflater.from(context))
        dialog.setContentView(bindingMostrar.root)

        bindingMostrar.cerrar.setOnClickListener {
            dialog.dismiss()
        }
        trabajo.document(item.id.toString()).get().addOnSuccessListener { res->
            if(res.exists()){
                val data=res.data
                // Obtener valores del mapa, asegurando que se conviertan a String si es necesario
                val titulo = data?.get("titulo") as? String ?: "Sin título"
                val contenido = data?.get("contenido") as? String ?: "Sin contenido"
                val idSelecionado = data?.get("id") as? String ?: ""
                val fecha_rec = data?.get("fecha_rec") as? String ?: ""
                val hora_rec = data?.get("hora_rec")as? String ?: ""
                val img_url = data?.get("img_url") as? String ?: ""
                val img_url2 = data?.get("img_url2") as? String ?: ""
                val img_url3 = data?.get("img_url3") as? String ?: ""
                val img_url4 = data?.get("img_url4") as? String ?: ""
                val listaImg =
                    listOf(img_url, img_url2, img_url3, img_url4).filter { it.isNotEmpty() }
                bindingMostrar.tituloNombreTrabajador.text = "Trabajos realizados por $Nombre_trabajador"
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

                // Configurar el texto expandible
                constantestextos_general.extender_acortar_texto(
                    bindingMostrar.textoTrabajosRealzados,
                    bindingMostrar.tvReadMore
                )

                bindingMostrar.textoTrabajosRealzados.text = contenido
                bindingMostrar.tituloTrabajosRealizados.text = titulo

                // Configurar el carrusel de imágenes si hay imágenes disponibles
                if (listaImg.isNotEmpty()) {
                    val carouselItems = listaImg.map { CarouselItem(it) }
                    bindingMostrar.carruselImgTrabajos.registerLifecycle(lifecycle)
                    bindingMostrar.carruselImgTrabajos.carouselListener = object : CarouselListener {
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
                    lifecycle
                )
            }
        }



    }

    fun obtenerMasTrabajosRealiazdos(
        context: Context,
        idTrabajador: String,
        bindingMostrarTRabajos: BottomSheetMostarTrabajosRecientesBinding,
        idSelecionado: String,
        listaMas_promo: MutableList<dataclass_adapter_promociones>, lifecycle: Lifecycle
    ) {
        val db = FirebaseFirestore.getInstance()
            .collection("Trabajadores_Usuarios_Drivers").document("trabajadores")
            .collection("trabajadores").document(idTrabajador).collection("publicaciones_trabajos")
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
                    bindingMostrarTRabajos,
                    context,
                    listaMas_promo,
                    lifecycle
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

    private fun inicializarTrabajosRealizados(
        bindingMostrarTRabajos: BottomSheetMostarTrabajosRecientesBinding,
        context: Context,
        listaMas_promo: MutableList<dataclass_adapter_promociones>,
        lifecycle: Lifecycle
    ) {
        val recicle = bindingMostrarTRabajos.cargaProductosPromoTrabajos.masTrabajosRealiados
        recicle.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        recicle.adapter = adapter_trabajos_realizados_trabajador(
            false,
            listaMas_promo
        ) { item ->
            cagrarDatosNuevamente(item, bindingMostrarTRabajos, listaMas_promo, lifecycle)
        }

    }

    fun cagrarDatosNuevamente(
        item: dataclass_adapter_promociones,
        bindingMostrarTRabajos: BottomSheetMostarTrabajosRecientesBinding,
        listaMas_promo: MutableList<dataclass_adapter_promociones>,
        lifecycle: Lifecycle
    ) {
        bindingMostrarTRabajos.cargarConteindo.isVisible = true // Mostrar el ProgressBar
        bindingMostrarTRabajos.scollView.isVisible = false

        constantestextos_general.extender_acortar_texto(
            bindingMostrarTRabajos.textoTrabajosRealzados,
            bindingMostrarTRabajos.tvReadMore
        )
        bindingMostrarTRabajos.textoTrabajosRealzados.text = item.texto_promo
        bindingMostrarTRabajos.tituloTrabajosRealizados.text = item.titulo_promo
        println("El item seleccionado fue el ${item.id}")

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
                cagrarDatosNuevamente(nuevoItem, bindingMostrarTRabajos, listaMas_promo, lifecycle)
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