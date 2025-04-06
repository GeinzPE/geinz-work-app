package com.example.geinzwork.fragmentos.productosPublicadosVista

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.geinzwork.adapterViewholder.adapterCategoriasPromocionesFiltrado
import com.example.geinzwork.adapterViewholder.adapter_trabajos_realizados_trabajador
import com.example.geinzwork.adapterViewholder.adapter_ver_mas_productos_publicados
import com.example.geinzwork.classcustom.classcustomscrool
import com.example.geinzwork.dataclass.dataclasCaterogirasFiltrado
import com.example.geinzwork.dataclass.dataclass_ver_mas_productos_trabajador
import com.geinzz.geinzwork.R
import com.geinzz.geinzwork.adapterViewholder.adapter
import com.geinzz.geinzwork.constantesGeneral.constantes_publicaciones_general_user_tiendas
import com.geinzz.geinzwork.databinding.ActivityVerMasProductosPublicadosTrabajadoresBinding
import com.geinzz.geinzwork.databinding.BottomsheetProductosVendidosUserVerifiBinding
import com.geinzz.geinzwork.dataclass.dataclassMostarImgProductosVendedor
import com.geinzz.geinzwork.fragmentos.info
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.firestore.FirebaseFirestore

class ver_mas_productos_publicados_trabajadores : AppCompatActivity() {
    private var listaVer_mas_productos = mutableListOf<dataclass_ver_mas_productos_trabajador>()
    private lateinit var adapterCategorias: adapterCategoriasPromocionesFiltrado
    private val listaCategoriasDescuento = mutableListOf<dataclasCaterogirasFiltrado>()
    private val listaCategorias = mutableListOf<dataclasCaterogirasFiltrado>()
    val listaImg = mutableListOf<dataclassMostarImgProductosVendedor>()

    private lateinit var dialog: BottomSheetDialog

    private lateinit var binding: ActivityVerMasProductosPublicadosTrabajadoresBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityVerMasProductosPublicadosTrabajadoresBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val idTrabajdor = intent.getStringExtra("idTrabajador").toString()
        obtenerProductosGenerales(idTrabajdor)
        obtener_categoriasDescuentos(idTrabajdor)

    }

    private fun filtrar_por_categorias(idTrabajador: String, categoriaFiltrado: String) {
        val db = FirebaseFirestore.getInstance().collection("Trabajadores_Usuarios_Drivers")
            .document("trabajadores").collection("trabajadores").document(idTrabajador)
            .collection("productos_venta")
        listaVer_mas_productos.clear()
        binding.prograsvar.isVisible=true
        binding.recycleViewProductosFiltrados.isVisible=false
        db.get().addOnSuccessListener { res ->
            for (datos in res) {
                val data = datos.data
                val imgPrincipal = data.get("img_principal") as? String ?: ""
                val id = data.get("id") as? String ?: ""
                val descripcionProducto = data.get("descripcion") as? String ?: ""
                val cantidad_porcentaje_descuento = data.get("cantidad_porcentaje_descuento") as? Number ?: 0
                val precioAntiguoProducto = data.get("precio_descuento") as? Number ?: 0
                val precioProducto = data.get("precio") as? Number ?: 0
                val descuentoProducto = data.get("descuento") as? Boolean ?: false
                val envioGratisProducto = data.get("envio_gratis") as? Boolean ?: false
                val categoria = data.get("categoria") as? String ?: ""
                val dataclass = dataclass_ver_mas_productos_trabajador(
                    imgPrincipal,
                    descripcionProducto,
                    precioAntiguoProducto,
                    precioProducto,
                    descuentoProducto,
                    envioGratisProducto, id, cantidad_porcentaje_descuento
                )
                if (categoriaFiltrado == categoria) {
                    listaVer_mas_productos.add(dataclass)
                    if (listaVer_mas_productos.isNotEmpty()) {
                        inicializarRecycleProductos(idTrabajador,listaVer_mas_productos)
                        binding.recycleViewProductosFiltrados.isVisible=true
                        binding.textoNoEncontrado.isVisible=false
                        binding.prograsvar.isVisible=false
                    } else {
                        println("no se econtraron datos")
                        binding.textoNoEncontrado.isVisible=true
                        binding.prograsvar.isVisible=false
                    }

                } else {
                    println("no se econtraron datos")
                }

            }
        }
    }

    private fun filtar_por_categorias_y_descuentos(idTrabajador: String, categoriaFiltrado: String){
        val db = FirebaseFirestore.getInstance().collection("Trabajadores_Usuarios_Drivers")
            .document("trabajadores").collection("trabajadores").document(idTrabajador)
            .collection("productos_venta")
        listaVer_mas_productos.clear()
        binding.prograsvar.isVisible=true
        binding.recycleViewProductosFiltrados.isVisible=false
        db.get().addOnSuccessListener { res ->
            for (datos in res) {
                val data = datos.data
                val imgPrincipal = data.get("img_principal") as? String ?: ""
                val id = data.get("id") as? String ?: ""
                val descripcionProducto = data.get("descripcion") as? String ?: ""
                val cantidad_porcentaje_descuento = data.get("cantidad_porcentaje_descuento") as? Number ?: 0
                val precioAntiguoProducto = data.get("precio_descuento") as? Number ?: 0
                val precioProducto = data.get("precio") as? Number ?: 0
                val descuentoProducto = data.get("descuento") as? Boolean ?: false
                val envioGratisProducto = data.get("envio_gratis") as? Boolean ?: false
                val categoria = data.get("categoria") as? String ?:""
                val dataclass = dataclass_ver_mas_productos_trabajador(
                    imgPrincipal,
                    descripcionProducto,
                    precioAntiguoProducto,
                    precioProducto,
                    descuentoProducto,
                    envioGratisProducto, id, cantidad_porcentaje_descuento
                )
                if (categoriaFiltrado == categoria && descuentoProducto) {
                    listaVer_mas_productos.add(dataclass)
                    if (listaVer_mas_productos.isNotEmpty()) {
                        inicializarRecycleProductos(idTrabajador,listaVer_mas_productos)
                        binding.recycleViewProductosFiltrados.isVisible=true
                        binding.textoNoEncontrado.isVisible=false
                        binding.prograsvar.isVisible=false
                    } else {
                        println("no se econtraron datos")
                        binding.textoNoEncontrado.isVisible=true
                        binding.prograsvar.isVisible=false
                    }

                } else {
                    println("no se econtraron datos")
                }

            }
        }
    }

    private fun obtener_categoriasDescuentos(idTrabajador: String) {
        val db = FirebaseFirestore.getInstance().collection("Trabajadores_Usuarios_Drivers")
            .document("trabajadores").collection("trabajadores").document(idTrabajador)
            .collection("productos_venta")
        binding.prograsvar.isVisible=true
        binding.recycleViewProductosFiltrados.isVisible=false
        listaCategorias.clear()
        listaCategoriasDescuento.clear()
        db.get().addOnSuccessListener { res ->
            val categoriasSet = mutableSetOf<String>() // Evitar duplicados
            val categoriaDescuento = mutableSetOf<String>()

            for (datos in res) {
                val categoria = datos.getString("categoria") ?: ""
                val descuento = datos.getBoolean("descuento") ?: false
                if (categoria.isNotEmpty()) {
                    categoriasSet.add(categoria) // Agregar solo categorías válidas
                }
                if (descuento) {
                    categoriaDescuento.add(categoria)
                }
            }

            listaCategorias.add(dataclasCaterogirasFiltrado("Todos")) // Agregar "Todos" solo una vez
            categoriasSet.shuffled().forEach { categoria ->
                listaCategorias.add(dataclasCaterogirasFiltrado(categoria))
            }
            listaCategoriasDescuento.add(dataclasCaterogirasFiltrado("Sin seleccion")) // Agregar "Todos" solo una vez
            categoriaDescuento.shuffled().forEach { descuentoCat ->
                listaCategoriasDescuento.add(dataclasCaterogirasFiltrado(descuentoCat))
            }

            if (listaCategorias.isNotEmpty()) {
                inicializarCategoriasFiltrados(idTrabajador)
                binding.recycleViewProductosFiltrados.isVisible=true
                binding.textoNoEncontrado.isVisible=false
                binding.prograsvar.isVisible=false
            } else {
                println("no se econtraron datos")
                binding.textoNoEncontrado.isVisible=true
                binding.prograsvar.isVisible=false
            }

            if (listaCategoriasDescuento.isNotEmpty()) {
                inicializarCategoriasFiltradosDescuento(idTrabajador)
                binding.recycleViewProductosFiltrados.isVisible=true
                binding.textoNoEncontrado.isVisible=false
                binding.prograsvar.isVisible=false
            } else {
                println("no se econtraron datos")
                binding.textoNoEncontrado.isVisible=true
                binding.prograsvar.isVisible=false
            }
        }.addOnFailureListener { e ->
            println("No se encontraron categorías: $e")
        }
    }


    private fun obtenerProductosGenerales(idTrabajador: String) {
        val db = FirebaseFirestore.getInstance().collection("Trabajadores_Usuarios_Drivers")
            .document("trabajadores").collection("trabajadores").document(idTrabajador)
            .collection("productos_venta")
        binding.prograsvar.isVisible=true
        db.get().addOnSuccessListener { res ->
            for (datos in res) {
                val data = datos.data
                val imgPrincipal = data.get("img_principal") as? String ?: ""
                val id = data.get("id") as? String ?: ""
                val descripcionProducto = data.get("descripcion") as? String ?: ""
                val cantidad_porcentaje_descuento =
                    data.get("cantidad_porcentaje_descuento") as? Number ?: 0
                val precioAntiguoProducto = data.get("precio_descuento") as? Number ?: 0
                val precioProducto = data.get("precio") as? Number ?: 0
                val descuentoProducto = data.get("descuento") as? Boolean ?: false
                val envioGratisProducto = data.get("envio_gratis") as? Boolean ?: false
                val dataclass = dataclass_ver_mas_productos_trabajador(
                    imgPrincipal,
                    descripcionProducto,
                    precioAntiguoProducto,
                    precioProducto,
                    descuentoProducto,
                    envioGratisProducto, id, cantidad_porcentaje_descuento
                )
                listaVer_mas_productos.add(dataclass)
                if (listaVer_mas_productos.isNotEmpty()) {
                    inicializarRecycleProductos(idTrabajador,listaVer_mas_productos)
                    binding.recycleViewProductosFiltrados.isVisible=true
                    binding.textoNoEncontrado.isVisible=false
                    binding.prograsvar.isVisible=false
                } else {
                    println("no se econtraron datos")
                    binding.textoNoEncontrado.isVisible=true
                    binding.prograsvar.isVisible=false
                }

            }
        }.addOnFailureListener { e ->
            println("no se encontraron datos $e")
            binding.textoNoEncontrado.isVisible=true
            binding.prograsvar.isVisible=false
        }
    }

    private fun inicializarRecycleProductos(idTrabajador: String,listaProductos: MutableList<dataclass_ver_mas_productos_trabajador>) {
        val recicle = binding.recycleViewProductosFiltrados
        val layoutManager = GridLayoutManager(this, 2)
        recicle.layoutManager = layoutManager
        recicle.adapter = adapter_ver_mas_productos_publicados(
            listaProductos
        ){item->
            dialog = BottomSheetDialog(this)
            ShowBottomSheetDialogProductosTrabajadores(idTrabajador,item.id.toString())
            dialog.show()
        }

        layoutManager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
            override fun getSpanSize(position: Int): Int {
                return if (position < 2) {
                    1
                } else {
                    1
                }
            }
        }
    }

    private fun ShowBottomSheetDialogProductosTrabajadores(
        idTrabajador: String,
        productoClikado: String
    ) {
        val vistaInfo=info()
        val bindingProductosTrabajadores =
            BottomsheetProductosVendidosUserVerifiBinding.inflate(LayoutInflater.from(this))
        dialog.setContentView(bindingProductosTrabajadores.root)
        bindingProductosTrabajadores.cerrar.setOnClickListener {
            dialog.dismiss()
        }
        bindingProductosTrabajadores.linealTXTmasProductos.isVisible=false
        bindingProductosTrabajadores.carrucelMasProductosPublicados.isVisible=false

        bindingProductosTrabajadores.comprar.setOnClickListener {
            val intent=Intent(this,compras_productos_vendedor::class.java).apply {
                putExtra("idProducto",productoClikado)
                putExtra("idTrabajador",idTrabajador)
            }
            startActivity(intent)
            dialog.dismiss()
        }
        val recicle = bindingProductosTrabajadores.carrucelImgProductosVentaUser
        val customLayoutManager = classcustomscrool(this, LinearLayoutManager.HORIZONTAL, false)
        recicle.layoutManager = customLayoutManager

        val db = FirebaseFirestore.getInstance().collection("Trabajadores_Usuarios_Drivers")
            .document("trabajadores").collection("trabajadores").document(idTrabajador)
            .collection("productos_venta").document(productoClikado)

        db.get().addOnSuccessListener { res ->
            if (res.exists()) {
                bindingProductosTrabajadores.progressCarga.isVisible=true
                bindingProductosTrabajadores.nettScrollView.isVisible=false
                val data = res.data ?: emptyMap()
                constantes_publicaciones_general_user_tiendas.setearDatosdialogProductos(this,data, bindingProductosTrabajadores) { completado ->
                    bindingProductosTrabajadores.progressCarga.isVisible=false
                    bindingProductosTrabajadores.nettScrollView.isVisible=true
                }
                constantes_publicaciones_general_user_tiendas.inizializarImgProductos(this,listaImg,bindingProductosTrabajadores,data)
            } else {
                println("no se encontraron datos del producto")
            }
        }.addOnFailureListener { e ->
            println("no se encontro ningun dato de producto $e")
        }

    }

    private fun inicializarCategoriasFiltrados(idTrabajador: String) {
        val recicle = binding.RecicleCategoria
        adapterCategorias = adapterCategoriasPromocionesFiltrado(listaCategorias) { item ->
            println("el que selciontaste fue ${item.nombreCategoria}")
            if (!item.nombreCategoria.toString().equals("Todos")) {
                binding.lineaCategoriasDescuento.isVisible = false
            } else {
                binding.lineaCategoriasDescuento.isVisible = true
                obtenerProductosGenerales(idTrabajador)
            }
            filtrar_por_categorias(idTrabajador, item.nombreCategoria.toString())

        }
        recicle.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        recicle.adapter = adapterCategorias

    }

    private fun inicializarCategoriasFiltradosDescuento(idTrabajador: String) {
        val recicle = binding.RecicleCategoriaDescuento
        adapterCategorias = adapterCategoriasPromocionesFiltrado(listaCategoriasDescuento) { item ->
            println("el que selciontaste fue ${item.nombreCategoria}")
            if (!item.nombreCategoria.toString().equals("Sin seleccion")) {
                binding.RecicleCategoria.isVisible = false

            } else {
                binding.RecicleCategoria.isVisible = true
                obtenerProductosGenerales(idTrabajador)
            }
            filtar_por_categorias_y_descuentos(idTrabajador, item.nombreCategoria.toString())
        }
        recicle.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        recicle.adapter = adapterCategorias

    }
}