package com.geinzz.geinzwork.vistaTiendas

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import android.view.LayoutInflater
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.LayoutManager
import com.bumptech.glide.Glide
import com.geinzz.geinzwork.utils.constantes.constantes.constantes_bottomShet_fourdItem
import com.geinzz.geinzwork.R
import com.geinzz.geinzwork.ui.adapters.adapterFiltradoTiendas
import com.geinzz.geinzwork.ui.adapters.adapterNewAnunciosTienda
import com.geinzz.geinzwork.ui.adapters.adapterProductos
import com.geinzz.geinzwork.ui.adapters.adapterServicios
import com.geinzz.geinzwork.ui.adapters.adapterTiendasGeneral
import com.geinzz.geinzwork.ui.adapters.noticias_oferta_vista_tienda
import com.geinzz.geinzwork.utils.constantes.constantes.constantes2
import com.geinzz.geinzwork.model.dataclassArticulos
import com.geinzz.geinzwork.model.dataclassGridNoticiasVistaTiendas
import com.geinzz.geinzwork.model.dataclassPromociones
import com.geinzz.geinzwork.model.dataclassServicios
import com.geinzz.geinzwork.model.dataclass_item_general_tienda
import com.geinzz.geinzwork.vistaTrabajador.promocionesvista
import com.geinzz.geinzwork.vistaTrabajador.ver_detalles_Promociones
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDragHandleView
import com.google.firebase.firestore.FirebaseFirestore
import de.hdodenhof.circleimageview.CircleImageView

object constantesVistaTiendas {
    private lateinit var bottomSheet: BottomSheetDragHandleView


    fun vermasProductosGenereal(
        dataclassArticulos: dataclassArticulos,
        idTienda: String,
        contenx: Context,
    ) {
        var vista = Intent(contenx, vistaProductosGeneralTiendas::class.java)
        vista.putExtra("idTienda", idTienda)
        vista.putExtra("idProductoClikado", dataclassArticulos.id)
        contenx.startActivity(vista)
    }


    @SuppressLint("WrongConstant")
    fun inicializarRecicleProductos(
        mostrarTodo: Boolean,
        listaArticulos: MutableList<dataclassArticulos>,
        recicleContainer: RecyclerView,
        context: Context,
        idTienda: String,
        orientation: Int = LinearLayoutManager.HORIZONTAL,
    ) {
        val recicle = recicleContainer
        recicle.layoutManager = LinearLayoutManager(context, orientation, false)
        recicle.adapter =
            adapterProductos(
                mostrarTodo,
                listaArticulos
            ) { dataclassArticulos ->
                vermasProductosGenereal(
                    dataclassArticulos,
                    idTienda,
                    context
                )
            }
    }

//    @RequiresApi(Build.VERSION_CODES.O)
//    fun inicializarRecile(
//        listaAnuncios: MutableList<dataclassPromociones>,
//        recicleNoticias: RecyclerView,
//        context: Context, idTienda: String,
//        mostrarTodo: Boolean,
//        layoutManager: LayoutManager,
//    ) {
//        val recicle = recicleNoticias
//        recicle.layoutManager = layoutManager
//        recicle.adapter = adapterNewAnunciosTienda(
//            listaAnuncios,
//            { dataclassPromociones -> AccederPromo(dataclassPromociones, context, idTienda) },
//            constantes2.firebaseAuth.uid.toString(), mostrarTodo
//        )
//    }

    fun AccederPromo(
        dataclassPromociones: dataclassPromociones,
        context: Context,
        idTienda: String,
    ) {
        var vista = Intent(context, vistaProductosGeneralTiendas::class.java)
        vista.putExtra("idTienda", idTienda)
        vista.putExtra("idProductoClikado", dataclassPromociones.id)
        context.startActivity(vista)
    }


    fun obtenerImgTiendaFirestore(
        id: String,
        imgPerfilTienda: ImageView,
        imgPortadaset: ImageView,
        context: Context,
    ) {
        val db = FirebaseFirestore.getInstance().collection("Tiendas").document(id)
        db.get()
            .addOnSuccessListener { res ->
                if (res.exists()) {
                    val data = res.data
                    val imgPerfil = data?.get("imgPerfil") as? String
                    val imgPortada = data?.get("imgPortada") as? String
                    try {
                        Glide.with(context)
                            .load(imgPerfil)
                            .placeholder(R.drawable.img_perfil)
                            .into(imgPerfilTienda)

                        Glide.with(context)
                            .load(imgPortada)
                            .placeholder(R.drawable.img_perfil)
                            .into(imgPortadaset)
                    } catch (e: Exception) {
                        println("ocurrio un error al setear las imagenes")
                    }

                }
            }
    }


    //filtrados vistaRecicleProductos
    fun mostrarCategorias(categorias: List<String>, autoCompleteCategory: AutoCompleteTextView) {
        val adapter = ArrayAdapter(
            autoCompleteCategory.context,
            android.R.layout.simple_dropdown_item_1line,
            categorias
        )
        autoCompleteCategory.setAdapter(adapter)
    }

    fun obtenerCategorias(idTienda: String, onCategoriasObtenidas: (List<String>) -> Unit) {
        val categorias = mutableSetOf<String>()
        val db = FirebaseFirestore.getInstance().collection("Tiendas").document(idTienda)
            .collection("articulos")
        db.get().addOnSuccessListener { res ->
            for (datos in res) {
                val data = datos.data
                val categoria = data?.get("categoriaProducto") as? String ?: ""
                categoria?.let {
                    if (it.isNotBlank()) {
                        categorias.add(it)
                    }
                }
            }
            onCategoriasObtenidas(categorias.toList())
        }.addOnFailureListener { e ->
            Log.e("Firebase", "Error al obtener categorías $e")
        }

    }


    fun filtradoBottom_shett(
        RecicleFiltrado: RecyclerView,
        dialog: BottomSheetDialog,
        context: Context,
        idTienda: String, listaArticulos: MutableList<dataclassArticulos>,
    ) {
        val view =
            LayoutInflater.from(context)
                .inflate(R.layout.bottom_sheet_recicle_productos, null, false)
        bottomSheet = view.findViewById(R.id.cerrar)

        val btnApply = view.findViewById<Button>(R.id.btnApply)
        val btnCancel = view.findViewById<Button>(R.id.btnCancel)
        val autoCompleteCategory = view.findViewById<AutoCompleteTextView>(R.id.categoria)
        obtenerCategorias(idTienda) { categorias ->
            mostrarCategorias(categorias, autoCompleteCategory)
        }
        bottomSheet.setOnClickListener {
            dialog.dismiss()

        }
        btnCancel.setOnClickListener {
            dialog.dismiss()
        }

        btnApply.setOnClickListener {
            val categoriaSeleccionada = autoCompleteCategory.text.toString()
            if (categoriaSeleccionada.isEmpty()) {
                Toast.makeText(context, "Seleccione un campo para el filtrado", Toast.LENGTH_SHORT)
                    .show()
            } else {
                listaArticulos.clear()
                val db = FirebaseFirestore.getInstance().collection("Tiendas").document(idTienda)
                    .collection("articulos")
                listaArticulos.clear()
                db.get().addOnSuccessListener { res ->
                    for (datos in res) {
                        val data = datos.data
                        val descripcion = data?.get("descripcion") as? String ?: ""
                        val imgArticulo = data?.get("imgArticulo") as? String ?: ""
                        val nombreArticulo = data?.get("nombreArticulo") as? String ?: ""
                        val precio = data?.get("precio") as? String ?: ""
                        val fecha = data?.get("descripcion") as? String ?: ""
                        val id = data?.get("id") as? String ?: ""
                        val categoria = data?.get("categoriaProducto") as? String ?: ""
                        val articulo = dataclassArticulos(
                            nombreArticulo,
                            imgArticulo,
                            precio,
                            descripcion,
                            fecha,
                            id,
                            categoria
                        )
                        if (categoria == categoriaSeleccionada) {
                            listaArticulos.add(articulo)
                        }
                    }
                    inicializarRecicleProductos(
                        true,
                        listaArticulos,
                        RecicleFiltrado,
                        context,
                        idTienda, LinearLayoutManager.VERTICAL
                    )
                    dialog.dismiss()
                }.addOnFailureListener { e ->
                    Log.e("Firebase", "Error al obtener categorías $e")
                }
            }
        }
        dialog.setContentView(view)
    }


    fun inicializarRecicle(
        recicle: RecyclerView,
        listatiendas: MutableList<dataclass_item_general_tienda>,
        layoutManager: RecyclerView.LayoutManager,
        context: Context,
    ) {
        recicle.layoutManager = layoutManager
        recicle.adapter = adapterTiendasGeneral(
            listatiendas,
            { dataclass_item_general_tienda ->
                constantes2.verMasInfoTiendaGeneral(
                    dataclass_item_general_tienda,
                    context
                )
            },
            context
        )
    }

    fun inicializarRecicleFiltados(
        recicle: RecyclerView,
        listatiendas: MutableList<dataclass_item_general_tienda>,
        layoutManager: RecyclerView.LayoutManager,
        context: Context,
    ) {
        recicle.layoutManager = layoutManager
        recicle.adapter = adapterFiltradoTiendas(
            listatiendas,
            { dataclass_item_general_tienda ->
                constantes2.verMasInfoTiendaGeneral(
                    dataclass_item_general_tienda,
                    context
                )
            },
            context
        )
    }

    fun obtenerTiendas(
        filtrado: String,
        listaTiendas: MutableList<dataclass_item_general_tienda>,
        contexto: Context,
        recyclerTiendas: RecyclerView,
        loadingView: LinearLayoutCompat,
        contenedor: LinearLayout,
        botonFiltrado: ImageButton, filtradore: (String) -> Unit,
    ) {
        listaTiendas.clear()
        val Firestore = FirebaseFirestore.getInstance().collection("Tiendas")
        loadingView.isVisible = true // Mostrar el loading
        contenedor.isVisible = false
        Firestore.get()
            .addOnSuccessListener { querySnapshot ->
                var remainingQueries = querySnapshot.size()
                for (document in querySnapshot.documents) {
                    val userData = document.data
                    val imgPerfil = userData?.get("imgPerfil") as? String
                    val imgPortada = userData?.get("imgPortada") as? String
                    val nombreTienda = userData?.get("nombre") as? String
                    val categoria = userData?.get("categoria") as? String
                    val calificacion = userData?.get("estrellas") as? String
                    val estado = userData?.get("estado") as? String
                    val zona = userData?.get("zona") as? String
                    val id = userData?.get("id") as? String
                    val ubicacion = userData?.get("ubicacion") as? String
                    val seguidores = userData?.get("seguidores") as? String
                    val localidad = userData?.get("localidad") as? String
                    val latitud = userData?.get("latitud") as? Double
                    val longitud = userData?.get("longitud") as? Double
                    val tipoTienda = userData?.get("tipoTienda") as? String
                    val subcategoria = userData?.get("subcategorias") as? String

                    val tiendaContenido = dataclass_item_general_tienda(
                        imgPerfil,
                        imgPortada,
                        categoria,
                        nombreTienda,
                        calificacion,
                        estado,
                        zona,
                        id,
                        ubicacion,
                        seguidores,
                        localidad,
                        latitud,
                        longitud,
                        tipoTienda,
                        subcategoria
                    )

                    if (filtrado == localidad || filtrado == "General") {
                        listaTiendas.add(tiendaContenido)
                    }
                    filtradore(filtrado)
                    remainingQueries--
                    if (remainingQueries == 0) {
                        inicializarRecicle(
                            recyclerTiendas,
                            listaTiendas,
                            LinearLayoutManager(contexto, LinearLayoutManager.HORIZONTAL, false),
                            contexto
                        )
                        actualizarVisibilidad(true, loadingView, contenedor, botonFiltrado)
                        loadingView.isVisible =
                            false // Ocultar loading cuando se completen todas las consultas
                    }
                }
            }
            .addOnFailureListener {
                loadingView.isVisible = false // Ocultar loading en caso de fallo
            }
    }


    fun actualizarVisibilidad(
        hayArticulos: Boolean,
        carga: LinearLayoutCompat,
        container: LinearLayout,
        botonFiltrado: ImageButton,
    ) {
        carga.isVisible = false
        if (hayArticulos) {
            container.isVisible = true
            carga.isVisible = false
            botonFiltrado.isVisible = true
        } else {
            carga.isVisible = true
            container.isVisible = false
            botonFiltrado.isVisible = false
        }
    }


    fun obtenerTiendaCategorias(
        filtrado: String,
        context: Context,
        RecicleSalud: RecyclerView,
        RecicleVeiculos: RecyclerView,
        RecicleBodegas: RecyclerView,
        RecicleComida: RecyclerView,
        loadingView: LinearLayoutCompat,
        container: LinearLayout,
        botonFiltrado: ImageButton,
    ) {
        val Comida = mutableListOf<dataclass_item_general_tienda>()
        val tiendasSalud = mutableListOf<dataclass_item_general_tienda>()
        val veiculos = mutableListOf<dataclass_item_general_tienda>()
        val bodegas = mutableListOf<dataclass_item_general_tienda>()

        val db = FirebaseFirestore.getInstance().collection("Tiendas")
        db.get()
            .addOnSuccessListener { querySnapshot ->
                loadingView.isVisible = true // Mostrar loading
                var remainingQueries = querySnapshot.size()
                for (document in querySnapshot.documents) {
                    val userData = document.data
                    val imgPerfil = userData?.get("imgPerfil") as? String
                    val imgPortada = userData?.get("imgPortada") as? String
                    val nombreTienda = userData?.get("nombre") as? String
                    val categoria = userData?.get("categoria") as? String
                    val calificacion = userData?.get("estrellas") as? String
                    val estado = userData?.get("estado") as? String
                    val zona = userData?.get("zona") as? String
                    val id = userData?.get("id") as? String
                    val ubicacion = userData?.get("ubicacion") as? String
                    val seguidores = userData?.get("seguidores") as? String
                    val localidad = userData?.get("localidad") as? String
                    val latitud = userData?.get("latitud") as? Double
                    val longitud = userData?.get("longitud") as? Double
                    val TipoTienda = userData?.get("tipoTienda") as? String
                    val subcategoria = userData?.get("subcategorias") as? String
                    val tiendaContenido = dataclass_item_general_tienda(
                        imgPerfil,
                        imgPortada,
                        categoria,
                        nombreTienda,
                        calificacion,
                        estado,
                        zona,
                        id,
                        ubicacion,
                        seguidores,
                        localidad,
                        latitud,
                        longitud,
                        TipoTienda,
                        subcategoria
                    )
                    if (filtrado == localidad) {
                        when (categoria) {
                            "salud y belleza" -> tiendasSalud.add(tiendaContenido)
                            "servicios" -> veiculos.add(tiendaContenido)
                            "bodegas" -> bodegas.add(tiendaContenido)
                            "comida" -> Comida.add(tiendaContenido)
                        }
                    } else if (filtrado == "General") {
                        when (categoria) {
                            "salud y belleza" -> tiendasSalud.add(tiendaContenido)
                            "servicios" -> veiculos.add(tiendaContenido)
                            "bodegas" -> bodegas.add(tiendaContenido)
                            "comida" -> Comida.add(tiendaContenido)
                        }
                    } else {
                        // Toast.makeText(mcontex,"no se encontro lugares en su localidad",Toast.LENGTH_SHORT).show()
                    }
                    remainingQueries--
                    if (remainingQueries == 0) {
                        inicializarRecicle(
                            RecicleSalud,
                            tiendasSalud,
                            LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false),
                            context
                        )
                        inicializarRecicle(
                            RecicleVeiculos,
                            veiculos,
                            LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false),
                            context
                        )
                        inicializarRecicle(
                            RecicleBodegas,
                            bodegas,
                            LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false),
                            context
                        )
                        inicializarRecicle(
                            RecicleComida,
                            Comida,
                            LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false),
                            context
                        )
                        actualizarVisibilidad(
                            true,
                            loadingView,
                            container, botonFiltrado
                        )
                    }
                    if (remainingQueries == 0) {
                        loadingView.isVisible = false // Ocultar loading si no hay documentos
                    }

                }

            }.addOnSuccessListener {
                loadingView.isVisible = false
            }

    }

    @SuppressLint("NewApi")
    fun setearDatosTienda(
        idTienda: String,
        context: Context,
        imgPerfilUser: CircleImageView,
        nombreTienda: TextView,
        sloganTienda: TextView,
        horarioTienda:TextView
    ) {
        val instancia = FirebaseFirestore.getInstance().collection("Tiendas")
        val info = instancia.document(idTienda)
        info.get()
            .addOnSuccessListener { res ->
                if (res.exists()) {
                    val data = res.data
                    val nombreTiendas = data?.get("nombre") as? String
                    val imgTienda = data?.get("imgPerfil") as? String
                    val slogan = data?.get("slogan") as? String
                    constantes_bottomShet_fourdItem.obtenerHorarioDeHoy(idTienda) { apertura, cierre, cerrado ->
                        if(cerrado){
                            horarioTienda.text="Fuera de servicio"
                        }else{
                            horarioTienda.text="$apertura AM a $cierre PM"
                        }
                    }
                    try {
                        Glide.with(context)
                            .load(imgTienda)
                            .into(imgPerfilUser)
                    } catch (e: Exception) {
                        println("error $e")
                    }

                    nombreTienda.text = nombreTiendas
                    sloganTienda.text = slogan
                }
            }
    }


    private fun inicializarRecicleGridTienda(
        listaAnuncios: MutableList<dataclassGridNoticiasVistaTiendas>,
        recicleNoticias: RecyclerView,
        context: Context,
    ) {
        val gridLayoutManager =
            LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        recicleNoticias.layoutManager = gridLayoutManager
        recicleNoticias.adapter =
            noticias_oferta_vista_tienda(listaAnuncios) { dataclassGridNoticiasVistaTiendas ->
                listenerNoticiass(dataclassGridNoticiasVistaTiendas, context)
            }
    }

    private fun listenerNoticiass(
        dataclassGridNoticiasVistaTiendas: dataclassGridNoticiasVistaTiendas,
        context: Context,
    ) {
        val intent = Intent(context, ver_detalles_Promociones::class.java)
        intent.putExtra("idAnuncio", dataclassGridNoticiasVistaTiendas.id)
        intent.putExtra("idTienda", dataclassGridNoticiasVistaTiendas.idTienda)
        intent.putExtra("entrada", "tiendas")

        context.startActivity(intent)
    }


    @RequiresApi(Build.VERSION_CODES.O)
    fun obtenerPromoSugeridas(
        with: String,
        listaArticulos: MutableList<dataclassPromociones>,
        idTienda: String,
        reciclePromo: RecyclerView,
        context: Context,
        layoutManager: LayoutManager,
        mostrar_todo: Boolean,
    ) {


        val db = FirebaseFirestore.getInstance().collection("Tiendas").document(idTienda)
            .collection("promociones")
        db.get().addOnSuccessListener { res ->
            for (datos in res) {
                val data = datos.data
                val Contenido = data?.get("Contenido") as? String
                val adquirir = data?.get("adquirir") as? Boolean ?: false
                val plin = data?.get("plin") as? Boolean ?: false
                val yape = data?.get("adquirir") as? Boolean ?: false
                val reserva = data?.get("reserva") as? Boolean ?: false
                val imgArticulo = data?.get("imagenUrl") as? String ?: ""
                val efectivo = data?.get("efectivo") as? Boolean ?: false
                val titulo = data?.get("titulo") as? String ?: ""
                val vencimiento = data?.get("vencimiento") as? String ?: ""
                val precio = data?.get("precio") as? String ?: ""
                val id = data?.get("id") as? String ?: ""
                val articulos = dataclassPromociones(
                    imgArticulo,
                    titulo,
                    vencimiento,
                    precio,
                    id,
                    adquirir,
                    reserva,
                    efectivo,
                    yape,
                    plin
                )
                listaArticulos.add(articulos)

            }
            inicializarRecilePromo(
                with,
                listaArticulos,
                reciclePromo,
                context,
                idTienda,
                mostrar_todo, layoutManager
            )
        }
    }


    fun inicializarRecilePromo(
        with: String,
        listaAnuncios: MutableList<dataclassPromociones>,
        recicleNoticias: RecyclerView,
        context: Context, idTienda: String,
        mostrarTodo: Boolean,
        layoutManager: LayoutManager,
    ) {
        val recicle = recicleNoticias
        recicle.layoutManager = layoutManager
        recicle.adapter = adapterNewAnunciosTienda(
            with,
            listaAnuncios,
            { dataclassPromociones -> AccederPromociones(dataclassPromociones, context, idTienda) },
            constantes2.firebaseAuth.uid.toString(), mostrarTodo
        )
    }

    fun AccederPromociones(
        dataclassPromociones: dataclassPromociones,
        context: Context,
        idTienda: String,
    ) {
        var vista = Intent(context, promocionesvista::class.java)
        vista.putExtra("idTienda", idTienda)
        vista.putExtra("idProductoClikado", dataclassPromociones.id)
        context.startActivity(vista)
    }


    fun obtenerimgNoticias(
        idTienda: String,
        listaimagenes: MutableList<dataclassGridNoticiasVistaTiendas>,
        recicleNoticiasYOfertas: RecyclerView,
        context: Context,

        ) {
        val db = FirebaseFirestore.getInstance().collection("Tiendas")
            .document(idTienda).collection("noticias")
        db.get()
            .addOnSuccessListener { res ->
                listaimagenes.clear()
                for (resultado in res) {
                    val data = resultado.data
                    val idTiendaProp = data?.get("idTiendaProp") as? String
                    val idnoticia = data?.get("id") as? String
                    val imagenUrl = data?.get("imagenUrl") as? String
                    val conexion = data?.get("conexion") as? String
                    val tipo = data?.get("tipoPromo") as? String
                    if (idTiendaProp == idTienda) {
                        val imagenes = dataclassGridNoticiasVistaTiendas(
                            idTienda,
                            imagenUrl,
                            idnoticia,
                            conexion,
                            tipo
                        )
                        listaimagenes.add(imagenes)
                    }
                }
                inicializarRecicleGridTienda(
                    listaimagenes,
                    recicleNoticiasYOfertas,
                    context
                )

            }
            .addOnFailureListener { e ->
                println("Error al obtener imágenes de noticias: $e")
            }
    }

    fun ArticulosPrimarios(
        idTienda: String,
        listaArticulos: MutableList<dataclassArticulos>,
        ProductosTienda: RecyclerView,
        context: Context,

        ) {
        val db = FirebaseFirestore.getInstance().collection("Tiendas").document(idTienda)
            .collection("articulos")
        db.get().addOnSuccessListener { res ->
            for (datos in res) {
                val data = datos.data

                val descripcion = data?.get("descripcion") as? String ?: ""
                val imgArticulo = data?.get("imgArticulo") as? String ?: ""
                val nombreArticulo = data?.get("nombreArticulo") as? String ?: ""
                val precio = data?.get("precio") as? String ?: ""
                val fecha = data?.get("fecha") as? String ?: ""
                val id = data?.get("id") as? String ?: ""
                val categoria = data?.get("categoria") as? String ?: ""
                val articulo = dataclassArticulos(
                    nombreArticulo, imgArticulo, precio, descripcion, fecha, id, categoria
                )
                listaArticulos.add(articulo)
            }
            inicializarRecicleProductos(
                false,
                listaArticulos,
                ProductosTienda,
                context,
                idTienda
            )
        }.addOnFailureListener {
            println("no se encontro productos")
        }
    }


    fun obtenerServicios(
        idTienda: String,
        recicleServicios: RecyclerView,
        context: Context,
    ) {
        val listaServicios =
            mutableListOf<dataclassServicios>()
        val db = FirebaseFirestore.getInstance().collection("Tiendas")
            .document(idTienda).collection("servicios")
        db.get().addOnSuccessListener { res ->
            listaServicios.clear()
            for (document in res.documents) {
                val nombre = document.getString("nombre")
                val urlimg = document.getString("UrlImg") ?: ""
                val precio = document.getString("precio") ?: ""
                val descripcion = document.getString("descripcion")
                val id = document.getString("id")
                val idTienda = document.getString("idTienda")
                val reserva = document.getBoolean("reserva") ?: false
                val plin = document.getBoolean("plin") ?: false
                val yape = document.getBoolean("yape") ?: false
                val efectivo = document.getBoolean("efectivo") ?: false
                val servicio = dataclassServicios(
                    urlimg,
                    nombre,
                    precio,
                    descripcion,
                    id,
                    idTienda,
                    efectivo,
                    yape,
                    plin,
                    reserva
                )
                listaServicios.add(servicio)
            }

            inicializarRecicleServicios(listaServicios, recicleServicios, context)
        }.addOnFailureListener { e ->
            println("Error al obtener servicios: $e")

        }
    }


    private fun inicializarRecicleServicios(
        listaServicios: MutableList<dataclassServicios>,
        recyclerContainer: RecyclerView, context: Context,
    ) {
        val adaptador = adapterServicios(
            listaServicios
        ) { dataclassServicios ->
            vistaverServicios(
                context,
                dataclassServicios
            )
        }

        recyclerContainer.apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            adapter = adaptador
        }
    }

    private fun vistaverServicios(context: Context, dataclassServicios: dataclassServicios) {
        var vista = Intent(context, verServicios::class.java).apply {
            putExtra("idServicio", dataclassServicios.idServicio)
            putExtra("idTienda", dataclassServicios.idTienda)
        }
        context.startActivity(vista)
    }

}