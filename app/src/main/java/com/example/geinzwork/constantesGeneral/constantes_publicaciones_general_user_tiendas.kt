package com.geinzz.geinzwork.constantesGeneral

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.widget.RelativeLayout

import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.geinzwork.adapterViewholder.adapterInicializarRecycleimgProductosTrabajadores
import com.example.geinzwork.adapterViewholder.adapter_mostra_articulos_trabajadores
import com.example.geinzwork.adapterViewholder.adapter_productos_venta_user
import com.example.geinzwork.classcustom.classcustomscrool
import com.example.geinzwork.constantesGeneral.Variables
import com.example.geinzwork.dataclass.dataclas_item_preview_art_comprar
import com.example.geinzwork.dataclass.dataclassPorductosVerntaUser
import com.example.geinzwork.fragmentos.productosPublicadosVista.compras_productos_vendedor
import com.example.geinzwork.fragmentos.productosPublicadosVista.ver_mas_productos_publicados_trabajadores
import com.geinzz.geinzwork.databinding.BottomsheetProductosVendidosUserVerifiBinding
import com.geinzz.geinzwork.databinding.FragmentInfoBinding
import com.geinzz.geinzwork.dataclass.dataclas_trabajos_ralizados
import com.geinzz.geinzwork.dataclass.dataclassMostarImgProductosVendedor
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.play.integrity.internal.b
import com.google.firebase.firestore.FirebaseFirestore

object constantes_publicaciones_general_user_tiendas {
    private val listaAdapterProductosTRabajdores =
        mutableListOf<dataclas_item_preview_art_comprar>()
    val listaImg = mutableListOf<dataclassMostarImgProductosVendedor>()
    private val listaProductosUSer = mutableListOf<dataclassPorductosVerntaUser>()
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

                val item = dataclas_item_preview_art_comprar(
                    id, imgProducto, "", null, null, descuentoActivo, cantidadDescuento
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
                binding.linealNoSeEncontraron.isVisible=false
                binding.productosDestacados.isVisible=true

            } else {
                binding.linealNoSeEncontraron.isVisible=true
                binding.productosDestacados.isVisible=false
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

    private fun ShowBottomSheetDialogProductosTrabajadores(
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
        bindingProductosTrabajadores.vermasProductos.setOnClickListener {
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
        db.get().addOnSuccessListener { res ->
            listaProductosUSer.clear()
            for (datos in res) {
                val data = datos.data
                val id = data?.get("id") as? String ?: ""
                val imgpricipal = data?.get("img_principal") as? String ?: ""
                val descuento = data?.get("cantidad_porcentaje_descuento") as? Number ?: 0
                val porcentajeDescuentoBool = data?.get("descuento") as? Boolean ?: false
                val descripcion = data?.get("") as? String ?: ""
                if (id != idProducto) {
                    val dataClass = dataclassPorductosVerntaUser(
                        id, imgpricipal, descuento, porcentajeDescuentoBool
                    )
                    listaProductosUSer.add(dataClass)
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
            } else {
                Log.d("error obtenerDAtos", "No hay datos para mostrar")
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
                bindingProductosTrabajadores.descripcion,
                bindingProductosTrabajadores.tvReadMore
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
                bindingProductosTrabajadores.entregaDomicilio.text = "si"
            } else {
                bindingProductosTrabajadores.entregaDomicilio.text = "no"
            }
            if (descuento) {
                constantestextos_general.marcarDescuentoTxt(bindingProductosTrabajadores.precioAntiguo)


                constantestextos_general.setearPrecioDescuentoPrecioAntiguo(
                    precioDescuento,
                    bindingProductosTrabajadores.precioProducto,
                    precio,
                    bindingProductosTrabajadores.precioAntiguo,
                    cantidad_porcentaje_descuento,
                    bindingProductosTrabajadores.descuentoPorcentaje
                )

                bindingProductosTrabajadores.precioAntiguo.isVisible = true
                bindingProductosTrabajadores.descuentoPorcentaje.isVisible = true
            } else {
                bindingProductosTrabajadores.precioAntiguo.isVisible = false
                bindingProductosTrabajadores.descuentoPorcentaje.isVisible = false
                constantestextos_general.setearPrecioDescuentoPrecioAntiguo(
                    precio,
                    bindingProductosTrabajadores.precioProducto
                )

            }

            bindingProductosTrabajadores.categoriaProducto.text = categoria
            bindingProductosTrabajadores.nombreProducto.text = nombre
            bindingProductosTrabajadores.marca.text = marca
            bindingProductosTrabajadores.modelo.text = modelo
            bindingProductosTrabajadores.stok.text = stok
            bindingProductosTrabajadores.garantia.text = garantia
            bindingProductosTrabajadores.Condicion.text = condicionProducto
            bindingProductosTrabajadores.descripcion.text = descripcion
            bindingProductosTrabajadores.fechaPublicado.text = fechaPublicada
            inizializarImgProductos(context, listaImg, bindingProductosTrabajadores, data)
            constantestextos_general.marcarDescuentoTxt(bindingProductosTrabajadores.precioAntiguo)


            onComplete(true)
        } catch (e: Exception) {
            onComplete(false)
        }
    }

    private fun inizializarCarruceBindig(
        context: Context,
        idTrabajador: String,
        lista_productosVentaUSer: MutableList<dataclassPorductosVerntaUser>,
        bindingProductosTrabajadores: BottomsheetProductosVendidosUserVerifiBinding
    ) {
        val recicle = bindingProductosTrabajadores.carrucelMasProductosPublicados
        recicle.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)

        val adapter = adapter_productos_venta_user(lista_productosVentaUSer) { item ->
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

        Log.d(
            "IMG_DEBUG",
            "Contenido de listaImg después de limpiar: ${listaImg.joinToString { it.imgProducto.toString() }}"
        )

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


}