package com.geinzz.geinzwork.vistaTiendas


import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import com.geinzz.geinzwork.utils.constantes.constantes.constantes_bottomShet_fourdItem
import com.geinzz.geinzwork.utils.constantes.constantes.constantes_bottomShet_fourdItem.obtnerDireciones
import com.geinzz.geinzwork.utils.constantes.constantes.constantes_bottomShet_fourdItem.setearTotal
import com.geinzz.geinzwork.utils.constantes.constantes.constantes_cotizacion_driver
import com.geinzz.geinzwork.R
import com.geinzz.geinzwork.ui.adapters.adapterCarritoCompras
import com.geinzz.geinzwork.confirmacionCompraTienda.confirmacion_de_compra_tienda
import com.geinzz.geinzwork.utils.constantes.constantes.constantesCarrito
import com.geinzz.geinzwork.utils.constantes.constantes.constantesDatosUsuarioTienda
import com.geinzz.geinzwork.utils.constantes.constantes.constantesImagenes
import com.geinzz.geinzwork.databinding.ActivityCarritoComprasBinding
import com.geinzz.geinzwork.databinding.BottomSheetCompraProductosTiendasBinding
import com.geinzz.geinzwork.model.dataclassCarritoCompras
import com.geinzz.geinzwork.model.dataclassradiobtn
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDragHandleView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.firestore.FirebaseFirestore
import org.json.JSONObject


class carrito_compras : AppCompatActivity() {
    private lateinit var binding: ActivityCarritoComprasBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var bottomSheet: BottomSheetDragHandleView
    private lateinit var dialog: BottomSheetDialog
    private lateinit var direccion: EditText
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private val LOCATION_PERMISSION_REQUEST_CODE = 1
    private val listaArticulos = mutableListOf<dataclassCarritoCompras>()
    private var guardarPedidoEnBD: String = "String"
    private lateinit var bindingbottomShet: BottomSheetCompraProductosTiendasBinding
    private val lista = mutableListOf<dataclassradiobtn>()
    private var metodoEntrega = ""

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCarritoComprasBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        firebaseAuth = FirebaseAuth.getInstance()

        binding.cancelar.setOnClickListener {
            dialog = BottomSheetDialog(this)
            cancelarBottomShet(this)
            dialog.show()
        }
        binding.opciones.setOnClickListener {
            startActivity(Intent(this, historialCompras::class.java))
        }
        obtenerArticulos(firebaseAuth.uid.toString())

    }

    private fun obtenerArticulos(idUser: String) {
        val realtime = FirebaseDatabase.getInstance().getReference("carritoGeneral").child(idUser)
        realtime.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val totalArticulos = snapshot.children.flatMap { it.children }.size
                var articulosProcesados = 0

                for (tiendaSnapshot in snapshot.children) {
                    val idTienda = tiendaSnapshot.key
                    for (productoSnapshot in tiendaSnapshot.children) {
                        val idProducto = productoSnapshot.key
                        val nombre = productoSnapshot.child("nombre").getValue(String::class.java)
                        val precio = productoSnapshot.child("precio").getValue(String::class.java)
                        val cantidad =
                            productoSnapshot.child("cantidad").getValue(String::class.java)
                        val descripcion =
                            productoSnapshot.child("descripcion").getValue(String::class.java)

                        val firestore = FirebaseFirestore.getInstance().collection("Tiendas")
                            .document(idTienda!!)
                        firestore.get().addOnSuccessListener { res ->
                            if (res.exists()) {
                                val data = res.data
                                val nombreTienda = data?.get("nombre") as? String
                                val db = FirebaseFirestore.getInstance().collection("Tiendas")
                                    .document(idTienda).collection("articulos")
                                    .document(idProducto!!)
                                db.get().addOnSuccessListener { res ->
                                    if (res.exists()) {
                                        val datos = res.data
                                        val imgArticulo = datos?.get("imgArticulo") as? String
                                        val articulo = dataclassCarritoCompras(
                                            idProducto,
                                            nombre,
                                            precio,
                                            imgArticulo,
                                            nombreTienda,
                                            cantidad,
                                            descripcion,
                                            idTienda
                                        )
                                        listaArticulos.add(articulo)
                                    }
                                    articulosProcesados++
                                    // Verificar si todos los artículos han sido procesados
                                    if (articulosProcesados == totalArticulos) {
                                        iniciarRecicle(listaArticulos)
                                    }
                                }.addOnFailureListener { e ->
                                    println("No se pudo obtener la imagen del artículo: $e")
                                    articulosProcesados++
                                    if (articulosProcesados == totalArticulos) {
                                        iniciarRecicle(listaArticulos)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                println("Error al obtener los datos del carrito: ${error.message}")
            }
        })
    }

    fun iniciarRecicle(listaArticulos: MutableList<dataclassCarritoCompras>) {
        val recicle = binding.recicleProductosItem
        recicle.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        recicle.adapter = adapterCarritoCompras(
            listaArticulos
        ) { dataclassCarritoCompras -> pasarArticuloVista(dataclassCarritoCompras) }
    }

    private fun pasarArticuloVista(dataclassCarritoCompras: dataclassCarritoCompras) {
        var vista = Intent(this, vistaProductosGeneralTiendas::class.java).apply {
            putExtra("idTienda", dataclassCarritoCompras.idTienda)
            putExtra("idProductoClikado", dataclassCarritoCompras.id)
        }
        startActivity(vista)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    @SuppressLint("MissingInflatedId")
    private fun cancelarBottomShet(context: Context) {
        bindingbottomShet =
            BottomSheetCompraProductosTiendasBinding.inflate(LayoutInflater.from(context))
        val view = bindingbottomShet.root

        bottomSheet = view.findViewById(R.id.cerrar)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        val nombre = bindingbottomShet.nombreUser
        val numeroContacto = bindingbottomShet.numeroContacto
        val NombreTienda = bindingbottomShet.nombreTienda
        val btnApply = bindingbottomShet.btnApply
        val cancelar = bindingbottomShet.btnCancel
        val nombreTiendaCanelar = bindingbottomShet.tiendaCancelar
        val idTienda = bindingbottomShet.idTienda
        val localidadTienda = bindingbottomShet.localidadTienda
        val localidadUser = bindingbottomShet.localidadUser
        val tipoTienda = bindingbottomShet.TipoTienda
        val deliveryTEXT = bindingbottomShet.delivery
        val radioGrupMetodoEntrega = bindingbottomShet.metodoEntrega.RadiomodoEntrega
        val totalLinealPagar = bindingbottomShet.totalPagar.linealTotalPagar
        val direccionEntrega = bindingbottomShet.direccionEnvios.direccionED
        val referenciaEntrega = bindingbottomShet.direccionEnvios.referenciaED
        val layoutContainer = bindingbottomShet.direccionEnvios.layoutContainer
        val linealDriver = bindingbottomShet.totalPagar.linealDriver
        val delivery_precio = bindingbottomShet.direccionEnvios.precioDelivery
        val totalPagar = bindingbottomShet.totalPagar.totalPagar
        val reccileRadioBtn = bindingbottomShet.direccionEnvios.reccileRadioBtn
        val creaDireccion = bindingbottomShet.direccionEnvios.creaDireccion
        val pagodeldriver = bindingbottomShet.totalPagar.drivetxt
        val longitudUser = bindingbottomShet.direccionEnvios.longituduser
        val latitudUser = bindingbottomShet.direccionEnvios.latitudUSer
        val RadioMetodoPago = bindingbottomShet.metodoPagos.RadioMetodoPago
        val productosTotal = bindingbottomShet.totalPagar.pagoProductos
        val textoDriverPago = bindingbottomShet.totalPagar
        val idDireccionEvios=bindingbottomShet.direccionEnvios.idSelecionado
        var metodoEntrega: String = ""

        var METODPAGO: String = ""

        obtnerDireciones(
            idDireccionEvios,
            firebaseAuth.uid.toString(),
            lista,
            reccileRadioBtn,
            creaDireccion,
            context,
            direccionEntrega,
            referenciaEntrega,
            latitudUser,
            longitudUser, delivery_precio, pagodeldriver, totalPagar
        )
        delivery_precio.setOnClickListener {
            if (direccionEntrega.text.isNullOrEmpty()) {
                Toast.makeText(context, "Primero devemos obtener su direccion", Toast.LENGTH_SHORT)
                    .show()
                delivery_precio.dismissDropDown()
            } else {
                delivery_precio.showDropDown()
            }
        }

        constantes_cotizacion_driver.setearDelivery(delivery_precio, context)
        delivery_precio.setOnItemClickListener { parent, view, position, id ->
            val selectedService = parent.getItemAtPosition(position).toString()
            constantes_cotizacion_driver.obtenerCostoDelivery(
                longitudUser,
                latitudUser,
                idTienda.text.toString(),
                { estandar ->
                    if (selectedService == "Delivery estandar(GEINZ)") {
                        textoDriverPago.drivetxt.text = "(estandar) "
                        setearTotal(totalPagar, pagodeldriver, productosTotal, estandar)
                    }
                },
                { express ->
                    if (selectedService == "Delivery express(GEINZ)") {
                        textoDriverPago.drivetxt.text = "(express) "
                        setearTotal(totalPagar, pagodeldriver, productosTotal, express)
                    }
                })
        }

        radioGrupMetodoEntrega.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.recojoEnTienda -> {
                    totalLinealPagar.isVisible = true
                    direccionEntrega.isVisible = false
                    referenciaEntrega.isVisible = false
                    layoutContainer.isVisible = false
                    linealDriver.isVisible = false
                    metodoEntrega = "Recojo en tienda"
                    direccionEntrega.setText("")
                    referenciaEntrega.setText("")
                    delivery_precio.setText("")
                    pagodeldriver.text="0.0"
                    totalPagar.text = bindingbottomShet.totalPagar.pagoProductos.text.toString()
                }

                R.id.Delivery -> {
                    totalLinealPagar.isVisible = true
                    linealDriver.isVisible = true
                    layoutContainer.isVisible = true
                    direccionEntrega.isVisible = true
                    referenciaEntrega.isVisible = true
                    metodoEntrega = "Delivery"
                    pagodeldriver.text = "***"
                    totalPagar.text = "***"

                }

                else -> {
                    direccionEntrega.isVisible = false
                    referenciaEntrega.isVisible = false
                    metodoEntrega = ""
                    totalLinealPagar.isVisible = false
                }
            }
        }

        RadioMetodoPago.setOnCheckedChangeListener { group, checkedId ->
            when (checkedId) {
                R.id.Efectivo -> {
                    METODPAGO = "EFECTIVO"
                    bindingbottomShet.metodoPagos.layoutYape.isVisible = false
                    bindingbottomShet.metodoPagos.pagoEfectivo.isVisible = true
                    bindingbottomShet.metodoPagos.imagenYape.isVisible = false
                }

                R.id.Yape -> {
                    METODPAGO = "YAPE"
                    bindingbottomShet.metodoPagos.imagenYape.isVisible = true
                    bindingbottomShet.metodoPagos.layoutYape.isVisible = true
                    bindingbottomShet.metodoPagos.pagoEfectivo.isVisible = false
                    bindingbottomShet.metodoPagos.imagenPlin.isVisible = false
                }

                R.id.Plin -> {
                    METODPAGO = "PLIN"
                    bindingbottomShet.metodoPagos.imagenYape.isVisible = false
                    bindingbottomShet.metodoPagos.layoutYape.isVisible = true
                    bindingbottomShet.metodoPagos.imagenPlin.isVisible = true
                    bindingbottomShet.metodoPagos.pagoEfectivo.isVisible = false
                }

                else -> {
                    METODPAGO = ""
                }
            }

        }

        setearDatosDialogCompra()
        nombreTiendaCanelar.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                bindingbottomShet.infoTiendaSelect.isVisible = true
                NombreTienda.text = s.toString()

            }

            override fun afterTextChanged(s: Editable?) {}
        })

        constantesCarrito.retornarIDTienda { idTiendas ->
            llenarAutocompletTexView(idTiendas, nombreTiendaCanelar) { idSeleccionado ->
                NombreTienda.text = nombreTiendaCanelar.text.toString()
                obtenerTotalTienda(
                    firebaseAuth.uid.toString(),
                    idSeleccionado
                ) { res ->
                    guardarPedidoEnBD = res
                    Log.d("pedidos", res)
                }

                idTienda.text = idSeleccionado
                val yapeRuta = "Tiendas/$idSeleccionado/QR_pagos/Yape.jpg"
                val plinrRuta = "Tiendas/$idSeleccionado/QR_pagos/Plin.jpg"
                obtenerMetodosPago(yapeRuta, plinrRuta, idSeleccionado)
                constantesDatosUsuarioTienda.obtenerDetallesTienda(idSeleccionado) { localidad, tipo ->
                    tipoTienda.text = tipo
                    localidadTienda.text = localidad
                    when (tipo) {
                        "virtual" -> {
                            deliveryTEXT.text = "Delivery"
                            deliveryTEXT.isVisible = true
                            metodoEntrega = "Delivery"
                            bindingbottomShet.direccionEnvios.layoutContainer.isVisible = true
                        }

                        "fisica" -> {
                            radioGrupMetodoEntrega.isVisible = true
                        }

                        else -> {
                        }
                    }

                }
            }
        }


        bottomSheet.setOnClickListener { dialog.dismiss() }
        cancelar.setOnClickListener { dialog.dismiss() }

        btnApply.setOnClickListener {
            if (!verificarDatosUser(localidadTienda.text.toString(), metodoEntrega, METODPAGO)) {
                Toast.makeText(this, "error al enviar el formulario", Toast.LENGTH_SHORT).show()
            } else {
                val builder = AlertDialog.Builder(this)
                builder.setTitle("Confirmar Datos")
                builder.setMessage("¿Los datos que enviará son correctos?")
                builder.setPositiveButton("Sí") { dialogs, _ ->
                    val vista = Intent(this, confirmacion_de_compra_tienda::class.java).apply {
                        putExtra("nombreUser", nombre.text.toString())
                        putExtra("numeroUser", numeroContacto.text.toString())
                        putExtra("referencia_USER", referenciaEntrega.text.toString())
                        putExtra("direccion_USER", direccionEntrega.text.toString())
                        putExtra("localidad_USER", localidadUser.text.toString())
                        putExtra("idReferenciaEnvio",idDireccionEvios.text.toString())

                        putExtra("tipoTienda", tipoTienda.text.toString())
                        putExtra("localidadTiendaa", localidadTienda.text.toString())

                        putExtra("nombreTienda", nombreTiendaCanelar.text.toString())
                        putExtra("metodoEntrega", metodoEntrega)
                        putExtra("metodoPago", METODPAGO)
                        putExtra("idTienda", idTienda.text.toString())
                        putExtra("listaProductos", guardarPedidoEnBD)
                        putExtra("codigoPedigo", constantesCarrito.generarCodigoPedido())

                        putExtra("Delivery_escojido", delivery_precio.text.toString())
                        putExtra("precio_delivery", pagodeldriver.text.toString())
                        putExtra("totalProductos", productosTotal.text.toString())
                        putExtra("total", totalPagar.text.toString())
                        Log.d("delivery","${pagodeldriver.text.toString()}  ${delivery_precio.text.toString()}")
                    }
                    startActivity(vista)
                    dialogs.dismiss()
                    dialog.dismiss()
                }
                builder.setNegativeButton("No") { dialog, _ ->
                    dialog.dismiss()
                }
                builder.create().show()
            }
        }

        dialog.setContentView(view)
    }

    private fun obtenerTotalTienda(
        idUser: String,
        idTiendaEspecifica: String,
        callback: (String) -> Unit,
    ) {
        val productos = mutableListOf<Triple<String, Double, Double>>()
        val realtime = FirebaseDatabase.getInstance().getReference("carritoGeneral").child(idUser)
        realtime.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                var totalGlobal = 0.0
                val tiendaSnapshot = snapshot.child(idTiendaEspecifica)
                if (tiendaSnapshot.exists()) {
                    for (productoSnapshot in tiendaSnapshot.children) {
                        val idProducto =
                            productoSnapshot.child("idProducto").getValue(String::class.java)
                        val precioString =
                            productoSnapshot.child("precio").getValue(String::class.java)
                        val cantidadString =
                            productoSnapshot.child("cantidad").getValue(String::class.java)
                        val precio = precioString?.toDoubleOrNull() ?: 0.0
                        val cantidad = cantidadString?.toDoubleOrNull() ?: 0.0

                        if (idProducto != null) {
                            productos.add(Triple(idProducto, cantidad, precio))
                        }

                        totalGlobal += precio * cantidad
                    }
                    bindingbottomShet.totalPagar.pagoProductos.text = totalGlobal.toString()
                    constantes_cotizacion_driver.formatearNumeros(bindingbottomShet.totalPagar.totalPagar.text.toString())

                    val productosJson = JSONObject()
                    for (producto in productos) {
                        val productoJson = JSONObject()
                        productoJson.put("cantidad", producto.second)
                        productoJson.put("precio", producto.third)
                        productosJson.put(producto.first, productoJson)
                    }
                    val resultado = productosJson.toString()

                    callback(resultado)
                } else {
                    bindingbottomShet.totalPagar.totalPagar.text = "0.0"
                    callback("{}")
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("FirebaseDatabase", "Error obteniendo los datos", error.toException())
                callback("{}") // En caso de error, llama al callback con un JSON vacío
            }
        })
    }


    private fun llenarAutocompletTexView(
        idTiendas: List<String>,
        autoCompleteTextView: AutoCompleteTextView,
        onStoreSelected: (String) -> Unit,
    ) {
        val firestore = FirebaseFirestore.getInstance().collection("Tiendas")
        firestore.get()
            .addOnSuccessListener { res ->
                val nombresTiendas = mutableListOf<String>()
                val tiendasMap = mutableMapOf<String, String>() // Mapa para asociar nombres e IDs
                for (datos in res) {
                    val id = datos.getString("id")
                    if (id in idTiendas) {
                        val nombreTienda = datos.getString("nombre")
                        if (nombreTienda != null) {
                            nombresTiendas.add(nombreTienda)
                            tiendasMap[nombreTienda] = id!! // Asociar nombre con ID
                        }
                    }
                }

                val adapter = ArrayAdapter(
                    autoCompleteTextView.context,
                    android.R.layout.simple_dropdown_item_1line,
                    nombresTiendas
                )
                autoCompleteTextView.setAdapter(adapter)

                autoCompleteTextView.setOnItemClickListener { parent, view, position, id ->
                    val nombreSeleccionado = parent.getItemAtPosition(position).toString()
                    val idSeleccionado = tiendasMap[nombreSeleccionado]
                    if (idSeleccionado != null) {
                        onStoreSelected(idSeleccionado)
                    }
                    Log.d("SelectedTiendaID", "ID de la tienda seleccionada: $idSeleccionado")
                }
            }
            .addOnFailureListener { exception ->
                Log.e("FirestoreError", "Error al obtener documentos", exception)
            }
    }


    private fun setearDatosDialogCompra() {
        val localidadUser = bindingbottomShet.localidadUser
        val nombre = bindingbottomShet.nombreUser
        val numeroContacto = bindingbottomShet.numeroContacto
        constantesDatosUsuarioTienda.obtnerLocalidades(localidadUser)
        constantesCarrito.setearDatosUsuario { nombreus, numerous, localidad, apellido ->
            if (nombreus != null && numerous != null) {
                nombre.setText("$nombreus $apellido")
                numeroContacto.setText(numerous)
                localidadUser.setText(localidad)
                constantesDatosUsuarioTienda.obtnerLocalidades(localidadUser)

            } else {
                println("no se pudo encontrar su nombre")
            }
        }
    }


    private fun verificarDatosUser(
        localidTienda: String,
        metodoEntrega: String,
        metodoPagoSeleccionado: String,
    ): Boolean {
        val nombreUser = bindingbottomShet.nombreUser.text.toString()
        val tiendaCancelar = bindingbottomShet.tiendaCancelar.text.toString()
        val numeroContacto = bindingbottomShet.numeroContacto.text.toString()
        val localidadUser = bindingbottomShet.localidadUser.text.toString()

        if (nombreUser.isEmpty()) {
            mostrarError(bindingbottomShet.nombreUser, "Ingrese un nombre")
            return false
        }

        if (tiendaCancelar.isEmpty()) {
            mostrarError(bindingbottomShet.tiendaCancelar, "Seleccione una tienda a cancelar")
            return false
        }

        if (numeroContacto.isEmpty()) {
            mostrarError(bindingbottomShet.numeroContacto, "Ingrese un número de contacto")
            return false
        }

        if (localidadUser.isEmpty()) {
            mostrarError(bindingbottomShet.localidadUser, "Seleccione su localidad")
            return false
        }

        if (metodoEntrega.isEmpty()) {
            Toast.makeText(this, "Seleccione un método de entrega", Toast.LENGTH_SHORT).show()
            return false
        } else if (metodoEntrega == "Delivery") {
            if (localidadUser != localidTienda) {
                constantes_bottomShet_fourdItem.dialogo_solo_envios_localidadTienda(this)
                return false
            }
            if (bindingbottomShet.direccionEnvios.direccionED.text.isEmpty()) {
                mostrarError(
                    bindingbottomShet.direccionEnvios.direccionED,
                    "Seleccione una dirección de envío"
                )
                return false
            } else if (bindingbottomShet.direccionEnvios.referenciaED.text.isEmpty()) {
                mostrarError(
                    bindingbottomShet.direccionEnvios.referenciaED,
                    "Seleccione una referencia de envío"
                )
                return false
            } else if (bindingbottomShet.direccionEnvios.precioDelivery.text.isEmpty()) {
                mostrarError(
                    bindingbottomShet.direccionEnvios.precioDelivery,
                    "Seleccione un método de delivery"
                )
                return false
            }
        }

        if (metodoPagoSeleccionado.isEmpty()) {
            Toast.makeText(this, "Seleccione un método de pago", Toast.LENGTH_SHORT).show()
            return false
        }

        return true
    }

    private fun mostrarError(campo: EditText, mensaje: String) {
        Toast.makeText(this, mensaje, Toast.LENGTH_SHORT).show()
        campo.error = mensaje
    }

    private fun obtenerMetodosPago(yapeRuta: String, plinrRuta: String, idTienda: String) {
        Log.d("obtenismos", "obtenismos $idTienda y $plinrRuta y $yapeRuta")
        val db = FirebaseFirestore.getInstance().collection("Tiendas").document(idTienda)
        db.get().addOnSuccessListener { res ->
            if (res.exists()) {
                val datos = res.data
                val yape = datos?.get("yape") as? Boolean ?: false
                val plin = datos?.get("plin") as? Boolean ?: false
                val efectivo = datos?.get("efectivo") as? Boolean ?: false

                constantesImagenes.obtenerURLDescarga(
                    this,
                    bindingbottomShet.metodoPagos.imagenYape,
                    yapeRuta
                )
                constantesImagenes.obtenerURLDescarga(
                    this,
                    bindingbottomShet.metodoPagos.imagenPlin,
                    plinrRuta
                )
                constantes_bottomShet_fourdItem.verificarViivilidad_Pagos(
                    yape,
                    efectivo,
                    plin,
                    bindingbottomShet.metodoPagos.Yape,
                    bindingbottomShet.metodoPagos.Efectivo,
                    bindingbottomShet.metodoPagos.Plin
                )
            }
        }.addOnFailureListener { e ->
            Log.e("error obtener ruta img", "error al obtener las imagenes de pago ")
        }
    }
}
