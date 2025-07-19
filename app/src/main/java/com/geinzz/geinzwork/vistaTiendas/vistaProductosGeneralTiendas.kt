package com.geinzz.geinzwork.vistaTiendas

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.ViewTreeObserver
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.PopupMenu
import android.widget.RadioGroup
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.geinzz.geinzwork.utils.constantes.constantes.constantes_bottomShet_fourdItem
import com.geinzz.geinzwork.R
import com.geinzz.geinzwork.ui.adapters.adapter_item_mas_promo_servicios_art
import com.geinzz.geinzwork.utils.constantes.constantes.constantesCarrito
import com.geinzz.geinzwork.utils.constantes.constantes.constantesDatosUsuarioTienda
import com.geinzz.geinzwork.utils.constantes.constantes.constantesPublicidad
import com.geinzz.geinzwork.utils.constantes.constantes.mostrarFechaDialog_horaDialog
import com.geinzz.geinzwork.databinding.ActivityVistaProductosGeneralTiendasBinding
import com.geinzz.geinzwork.model.dataclass_mas_art_promo_servicio
import com.geinzz.geinzwork.model.dataclassradiobtn
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDragHandleView
import com.google.android.material.imageview.ShapeableImageView
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.dynamiclinks.androidParameters
import com.google.firebase.dynamiclinks.dynamicLinks
import com.google.firebase.dynamiclinks.googleAnalyticsParameters
import com.google.firebase.dynamiclinks.iosParameters
import com.google.firebase.dynamiclinks.itunesConnectAnalyticsParameters
import com.google.firebase.dynamiclinks.shortLinkAsync
import com.google.firebase.dynamiclinks.socialMetaTagParameters
import com.google.firebase.firestore.FirebaseFirestore
import org.imaginativeworld.whynotimagecarousel.model.CarouselItem

class vistaProductosGeneralTiendas : AppCompatActivity() {
    private lateinit var binding: ActivityVistaProductosGeneralTiendasBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var bottomSheet: BottomSheetDragHandleView
    private lateinit var dialog: BottomSheetDialog
    private val lista = mutableListOf<dataclassradiobtn>()
    private val listaServicios = mutableListOf<dataclass_mas_art_promo_servicio>()

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityVistaProductosGeneralTiendasBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        firebaseAuth = FirebaseAuth.getInstance()

        var idProductoClikado = intent.getStringExtra("idProductoClikado").toString()
        var idTienda = intent.getStringExtra("idTienda").toString()
        setearcampos(idProductoClikado, idTienda)
        constantesVistaTiendas.setearDatosTienda(
            idTienda,
            this,
            binding.layoutPerfilTienda.imgPerfilUser,
            binding.layoutPerfilTienda.nombreTienda,
            binding.layoutPerfilTienda.sloganTienda,
            binding.layoutPerfilTienda.horarioAtencion
        )
        binding.bakPresert.setOnClickListener {
            onBackPressed()
        }

        binding.popup.setOnClickListener {
            popup(idTienda, idProductoClikado)
        }

        binding.layoutPerfilTienda.InfoTienda.setOnClickListener {
            val vista = Intent(this, VistaTienda::class.java)
            vista.putExtra("idTienda", idTienda)
            startActivity(vista)
            finish()
        }

        binding.descripcionPromos.comprar.setOnClickListener {
            if (firebaseAuth.currentUser == null) {
                dialog = BottomSheetDialog(this)
                constantesPublicidad.CreacionCuentaBottom_shett(
                    this,
                    dialog
                )
                dialog.show()
                Toast.makeText(
                    this,
                    "Necesitas registrarte para agregar este producto a tu carrito",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                val realtime = FirebaseDatabase.getInstance().getReference("carritoGeneral")
                    .child(firebaseAuth.uid.toString())
                    .child(idTienda)

                realtime.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        var productoExiste = false
                        if (snapshot.exists()) {
                            for (res in snapshot.children) {
                                val id = res.key
                                if (id == idProductoClikado) {
                                    productoExiste = true
                                    break
                                }
                            }
                        }

                        if (productoExiste) {
                            Toast.makeText(
                                this@vistaProductosGeneralTiendas,
                                "El producto ya esta agregado al carrito",
                                Toast.LENGTH_SHORT
                            ).show()
                        } else {
                            agregarCarrito(
                                this@vistaProductosGeneralTiendas,
                                firebaseAuth.uid.toString(),
                                idTienda,
                                idProductoClikado
                            )
                            Toast.makeText(
                                this@vistaProductosGeneralTiendas,
                                "Producto agregado al carrito",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Log.e("Firebase", "Error al verificar el carrito", error.toException())
                    }
                })
            }
        }

        binding.descripcionPromos.reservar.setOnClickListener {
            if (firebaseAuth.currentUser == null) {
                dialog = BottomSheetDialog(this)
                constantesPublicidad.CreacionCuentaBottom_shett(
                    this,
                    dialog
                )
                dialog.show()
                Toast.makeText(
                    this,
                    "Necesitas registrarte para reservar este producto",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                dialog = BottomSheetDialog(this)
                constantes_bottomShet_fourdItem.bottomSheetReservaProducto(
                    "Reserva de articulos",
                    "reservaarticulos",
                    dialog,
                    idTienda,
                    idProductoClikado,
                    this,
                    binding.layoutPerfilTienda.nombreTienda,
                    firebaseAuth.uid.toString(), lista
                )
                dialog.show()
            }

        }
        binding.carritoCompras.setOnClickListener {
            val vista = Intent(this, carrito_compras::class.java)
            startActivity(vista)
        }
        obtenerMasServicios(idTienda,binding.recicleItemMas,listaServicios,"vistaProductosGeneralTiendas")
    }

    @SuppressLint("SuspiciousIndentation")
    private fun setearcampos(idProductoClikado: String, idTienda: String) {
        val db = FirebaseFirestore.getInstance().collection("Tiendas")
            .document(idTienda).collection("articulos").document(idProductoClikado)
        db.get().addOnSuccessListener { res ->
            if (res.exists()) {
                val data = res.data
                val contenido = data?.get("descripcion") as? String ?: ""
                val imgArticulo = data?.get("imgArticulo") as? String ?: ""
                val nombreArticulo = data?.get("nombreArticulo") as? String ?: ""
                val precio = data?.get("precio") as? String ?: ""
                val id = data?.get("id") as? String ?: ""
                val reserva = data?.get("reserva") as? Boolean ?: false
                val cantidad = data?.get("cantidad") as? String ?: ""
                val descripcionCorta = data?.get("descripcion_corta") as? String ?: ""
                val lista = mutableListOf<CarouselItem>()
                lista.add(CarouselItem(imgArticulo))
                binding.imgPrincipal.addData(lista)
                binding.descripcionPromos.contenido.text = contenido
                binding.descripcionPromos.titulo.text = nombreArticulo
                binding.descripcionPromos.precio.text = precio
                binding.descripcionPromos.cantidadDisponible.text = cantidad
                binding.descripcionPromos.contenidoCorto.text = descripcionCorta
                if (reserva) {
                    binding.descripcionPromos.reservar.isVisible = true
                }

                binding.descripcionPromos.contenido.viewTreeObserver.addOnGlobalLayoutListener(
                    object :
                        ViewTreeObserver.OnGlobalLayoutListener {
                        override fun onGlobalLayout() {
                            binding.descripcionPromos.contenido.viewTreeObserver.removeOnGlobalLayoutListener(
                                this
                            )
                            if (binding.descripcionPromos.contenido.lineCount > 2) {
                                binding.descripcionPromos.tvReadMore.isVisible = true
                                println("el texo es lagor")
                            }
                        }
                    })

                binding.descripcionPromos.tvReadMore.setOnClickListener {
                    if (binding.descripcionPromos.tvReadMore.text == "Leer más") {
                        binding.descripcionPromos.contenido.maxLines = Integer.MAX_VALUE
                        binding.descripcionPromos.tvReadMore.text = "Leer menos"
                    } else {
                        binding.descripcionPromos.contenido.maxLines = 3
                        binding.descripcionPromos.tvReadMore.text = "Leer más"
                    }
                }
            }
        }
    }


    private fun popup(idTienda: String, idProductoClikado: String) {
        val popup = PopupMenu(this, binding.popup)
        popup.menu.add(Menu.NONE, 1, 1, "Compartir")
        popup.show()

        popup.setOnMenuItemClickListener { item ->
            val itemID = item.itemId
            if (itemID == 1) {

                createAndShareDynamicLink(idTienda, idProductoClikado, this)

            }
            return@setOnMenuItemClickListener true
        }
    }

    fun createAndShareDynamicLink(
        idTienda: String,
        idProductoClikado: String,
        context: Context,
    ) {
        val Realtime = FirebaseDatabase.getInstance().getReference("Articulos").child(idTienda)
            .child(idProductoClikado)
        Realtime.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val contenido = "${snapshot.child("descripcion").value}"
                    val imgArticulo = "${snapshot.child("imgArticulo").value}"
                    val nombreArticulo = "${snapshot.child("nombreArticulo").value}"
                    Firebase.dynamicLinks.shortLinkAsync {
                        link =
                            Uri.parse("https:/geinzapp.page.link/?idArticulo=${idProductoClikado}&idTiendaSeleccionada=${idTienda}")
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
                            title = nombreArticulo
                            description = contenido
                            imageUrl = Uri.parse(imgArticulo)
                        }
                    }.addOnSuccessListener { shortDynamicLink ->
                        val shortLink = shortDynamicLink.shortLink
                        val invitationLink = shortLink.toString()

                        // Comparte el enlace usando una intención
                        val sendIntent: Intent = Intent().apply {
                            action = Intent.ACTION_SEND
                            putExtra(Intent.EXTRA_TEXT, invitationLink)
                            type = "text/plain"
                        }
                        context.startActivity(Intent.createChooser(sendIntent, null))
                    }.addOnFailureListener {
                        println("Hubo un error con los links dinámicos: $it")
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })
    }

    private fun agregarCarrito(
        context: Context,
        idUser: String,
        idTienda: String,
        idProducto: String,
    ) {
        val realtime = FirebaseDatabase.getInstance().getReference("carritoGeneral")
            .child(idUser)
            .child(idTienda)
            .child(idProducto)

        val productoData = mapOf(
            "idProducto" to idProducto,
            "nombre" to binding.descripcionPromos.titulo.text.toString(),
            "precio" to binding.descripcionPromos.precio.text.toString(),
            "cantidad" to "1",
            "descripcion" to binding.descripcionPromos.contenidoCorto.text.toString()
        )

        realtime.setValue(productoData)
            .addOnSuccessListener {
                Toast.makeText(context, "Producto agregado exitosamente", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { exception ->
                Toast.makeText(context, "Error al agregar el producto", Toast.LENGTH_SHORT).show()
            }
    }


    @RequiresApi(Build.VERSION_CODES.O)
    @SuppressLint("MissingInflatedId")
    private fun bottomSheetReservaProducto(
        idTienda: String,
        idProductoClikado: String,
        context: Context,
    ) {
        val view = LayoutInflater.from(context)
            .inflate(R.layout.bottom_sheet_reserva_productos, null, false)

        val scrollview = view.findViewById<ScrollView>(R.id.scrollview)
        val imageviewSubir = view.findViewById<ImageButton>(R.id.imageviewSubir)
        val horaActual = view.findViewById<TextView>(R.id.horaActual)
        val FechaActual = view.findViewById<TextView>(R.id.FechaActual)
        val tipoTienda = view.findViewById<TextView>(R.id.TipoTienda)
        val localidaTienda = view.findViewById<TextView>(R.id.localidadTienda)
        val nombreUser = view.findViewById<EditText>(R.id.nombreUser)
        val apellidouser = view.findViewById<EditText>(R.id.apellidouser)
        val dniUser = view.findViewById<EditText>(R.id.dniUser)
        val numeroContacto = view.findViewById<EditText>(R.id.numeroContacto)
        val metodoDelivery = view.findViewById<TextView>(R.id.delivery)
        val radioGrupMetodoEntrega = view.findViewById<RadioGroup>(R.id.RadiomodoEntrega)
//        val direccionEntrega = view.findViewById<TextInputLayout>(R.id.direccionEntrega)
//        val descripcionEntrega = view.findViewById<TextInputLayout>(R.id.descripcionEntrega)
        val fechareserva = view.findViewById<EditText>(R.id.fechareserva)
        val horareserva = view.findViewById<EditText>(R.id.horareserva)
        val localidadUser = view.findViewById<AutoCompleteTextView>(R.id.localidadUser)
//        val descripcionentregaEditext = view.findViewById<EditText>(R.id.descripcionEntregaED)
//        val direccionEntregaEditext = view.findViewById<EditText>(R.id.DireccionEntregaED)
        val tituloItem = view.findViewById<TextView>(R.id.tituloItem)
        val precioItem = view.findViewById<TextView>(R.id.precioItem)
        val cantidad = view.findViewById<TextView>(R.id.cantidad)
        val mas = view.findViewById<ImageButton>(R.id.mas)
        val menos = view.findViewById<ImageButton>(R.id.menos)
        val img_item = view.findViewById<ShapeableImageView>(R.id.img_item)
        val btnApply = view.findViewById<Button>(R.id.btnApply)

        fechareserva.setOnClickListener {
            mostrarFechaDialog_horaDialog.mostrarFechaDialogVista(fechareserva, this)
        }
        horareserva.setOnClickListener {
            mostrarFechaDialog_horaDialog.showTimePicker(this, horareserva)
        }

        constantesCarrito.obtnerfechaHora(horaActual, FechaActual)
        constantesCarrito.subirScroll(scrollview, imageviewSubir)
        constantesDatosUsuarioTienda.obtnerLocalidades(localidadUser)
        constantesDatosUsuarioTienda.obtenerDetallesTienda(idTienda) { localidad, tipo ->
            localidaTienda.text = localidad
            tipoTienda.text = tipo
            if (tipo == "virtual") {
                metodoDelivery.isVisible = true
            } else {
                radioGrupMetodoEntrega.isVisible = true
            }
        }

        var metodoEntrega = ""
        radioGrupMetodoEntrega.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.recojoEnTienda -> {
//                    direccionEntrega.isVisible = false
//                    descripcionEntrega.isVisible = false
                    metodoEntrega = "Recojo en tienda"
                }

                R.id.Delivery -> {
//                    direccionEntrega.isVisible = true
//                    descripcionEntrega.isVisible = true
                    metodoEntrega = "Delivery"
                }

                else -> {
//                    direccionEntrega.isVisible = false
//                    descripcionEntrega.isVisible = false
                    metodoEntrega = metodoDelivery.text.toString().trim()
                }
            }
        }

        val Realtime = FirebaseDatabase.getInstance().getReference("Articulos")
            .child(idTienda).child(idProductoClikado)

        Realtime.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val imgArticulo = "${snapshot.child("imgArticulo").value}"
                    val nombreArticulo = "${snapshot.child("nombreArticulo").value}"
                    val precio = "${snapshot.child("precio").value}"
                    val id = "${snapshot.child("id").value}"

                    mas.setOnClickListener { incrementarCantidad(cantidad, 1) }
                    menos.setOnClickListener { incrementarCantidad(cantidad, -1) }
                    btnApply.setOnClickListener {
                        val nombre = nombreUser.text.toString().trim()
                        val apellido = apellidouser.text.toString().trim()
                        val numero = numeroContacto.text.toString().trim()
                        val dni = dniUser.text.toString().trim()
                        val fecha = fechareserva.text.toString().trim()
                        val hora = horareserva.text.toString().trim()
                        val cantidadString = cantidad.text.toString()
                        val precioInt = precioItem.text.toString().toInt()
                        val localidadUserTxt = localidadUser.text.toString()
                        val deliveryText = metodoDelivery.text.toString()

                        if (nombre.isEmpty() || apellido.isEmpty() || numero.isEmpty() || dni.isEmpty() || fecha.isEmpty() || hora.isEmpty()) {
                            Toast.makeText(
                                context,
                                "Por favor, complete todos los campos.",
                                Toast.LENGTH_SHORT
                            ).show()
                        } else if ((metodoEntrega == "Delivery" || deliveryText == "delivery") && localidadUserTxt != localidaTienda.text.toString()) {
                            val builder = AlertDialog.Builder(context)
                            builder.setTitle("ATENCIÓN")
                                .setMessage("Los envíos por delivery solo están disponibles para localidades iguales a las tiendas negocios o locales.")
                                .setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }
                                .create().show()
                        } else {
                            val totalReserva = cantidadString.toInt() * precioInt * 0.5
                            val totalApagar = cantidadString.toInt() * precioInt
                            val builder = AlertDialog.Builder(context)
                            builder.setTitle("ATENCION")
                                .setMessage(
                                    """
                            En Geinz, nuestras políticas de reserva requieren que los pagos se realicen por el 50% del total.
                                Una vez confirmado el pago, la tienda o negocio aceptará tu reserva.
                                -----------------------------------------------
                                Total de la reserva: PEN $totalReserva por adelantado
                            """.trimIndent()
                                )
                                .setPositiveButton("ok") { dialogs, _ ->
//                                    constantesCarrito.agregarReserva(
//                                        binding.nombreTienda.text.toString(),
//                                        idTienda,
//                                        constantesCarrito.generarCodigoPedido(),
//                                        FechaActual.text.toString(),
//                                        horaActual.text.toString(),
//                                        nombre,
//                                        apellido,
//                                        dni,
//                                        numero,
//                                        fecha,
//                                        hora,
//                                        metodoEntrega,
//                                        tituloItem.text.toString(),
//                                        precioItem.text.toString(),
//                                        cantidadString,
//                                        id,
//                                        totalReserva.toString(),
//                                        totalApagar.toString(),
//                                        context,
//                                        localidadUserTxt,
//                                        localidaTienda.text.toString(),
//                                        descripcionentregaEditext.text.toString(),
//                                        direccionEntregaEditext.text.toString(),
//                                        tipoTienda.text.toString()
//                                    )
                                    dialogs.dismiss()
                                    dialog.dismiss()
                                }
                                .setNegativeButton("ver politicas") { dialog, _ -> dialog.dismiss() }
                                .create().show()
                        }

                    }

                    tituloItem.text = nombreArticulo
                    precioItem.text = precio
                    Glide.with(context).load(imgArticulo).into(img_item)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                println("No se pudo obtener los datos de Realtime")
            }
        })

        dialog.setContentView(view)
    }

    fun incrementarCantidad(cantidad: TextView, incremento: Int) {

        val cantidadSTR = cantidad.text.toString()
        val cantidadINT = if (cantidadSTR.isNotBlank()) cantidadSTR.toInt() else 0

        val nuevaCantidad = cantidadINT + incremento

        if (nuevaCantidad >= 0) {
            cantidad.setText(nuevaCantidad.toString())
        }

    }
    fun obtenerMasServicios(idTienda: String ,recicleItemMas:RecyclerView,listaServicios: MutableList<dataclass_mas_art_promo_servicio> ,categoria:String) {
        listaServicios.clear()
        val db = FirebaseFirestore.getInstance().collection("Tiendas").document(idTienda)
            .collection("articulos")

        db.get().addOnSuccessListener { res ->
            val tempList = mutableListOf<dataclass_mas_art_promo_servicio>()

            for (datos in res) {
                val data = datos.data
                val img = data?.get("imgArticulo") as? String ?: ""
                val titulo = data?.get("nombreArticulo") as? String ?: ""
                val descripcion = data?.get("descripcion_corta") as? String ?: ""
                val id = data?.get("id") as? String ?: ""
                val precio = data?.get("precio") as? String ?: ""

                val dataclass = dataclass_mas_art_promo_servicio(id, idTienda, img, titulo, descripcion, precio)
                tempList.add(dataclass)
            }

            tempList.shuffle()

            val cantidadAMostrar = 5
            listaServicios.addAll(tempList.take(cantidadAMostrar))

            activarRecicle(listaServicios, recicleItemMas, this,categoria)
        }
            .addOnFailureListener { e ->
                android.util.Log.d("error", "error al obtener los datos: ${e.message}")
            }
    }


    private fun activarRecicle(
        listaServicios: MutableList<dataclass_mas_art_promo_servicio>,
        recyclerContainer: RecyclerView, context: Context,
        categoria:String
    ) {
        val adaptador = adapter_item_mas_promo_servicios_art(
            listaServicios, categoria
        )
        recyclerContainer.apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            adapter = adaptador
        }
    }


}