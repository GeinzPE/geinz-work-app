package com.geinzz.geinzwork.utils.constantes.constantes


import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import android.view.LayoutInflater
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.RadioButton
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.geinzz.geinzwork.NotificacionRS
import com.geinzz.geinzwork.R
import com.geinzz.geinzwork.data.model.localizate_geinz.HorarioAtencion_box
import com.geinzz.geinzwork.data.model.localizate_geinz.HorarioDia_bloques
import com.geinzz.geinzwork.data.model.localizate_geinz.filtrado_tiendas.HorarioDia_box
import com.geinzz.geinzwork.ui.adapters.adapter_radioButton_envios
import com.geinzz.geinzwork.databinding.BottomSheetReservaProductosBinding
import com.geinzz.geinzwork.model.dataclasscompra_reserva_promociones
import com.geinzz.geinzwork.model.dataclassradiobtn
import com.geinzz.geinzwork.utils.constantes.constantes.constantes_publicaciones_general_user_tiendas.obtenerDiaActualEnEspañol
import com.geinzz.geinzwork.vistaTiendas.direccion_entrega_lat_log
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.imageview.ShapeableImageView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.time.DayOfWeek
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.Date
import java.util.Locale

object constantes_bottomShet_fourdItem {
    private lateinit var firebaseAuth: FirebaseAuth

    @SuppressLint("SetTextI18n")
    @RequiresApi(Build.VERSION_CODES.O)
    fun bottomSheetReservaProducto(
        titulo_dialog: String,
        tipo: String,
        dialog: BottomSheetDialog,
        idTienda: String,
        idProductoClikado: String,
        context: Context,
        nombreTienda: TextView,
        idUSer: String,
        lista: MutableList<dataclassradiobtn>,
    ) {
        val bindingBottomShjet =
            BottomSheetReservaProductosBinding.inflate(LayoutInflater.from(context))
        val titulo = bindingBottomShjet.tvTitle
        val view = bindingBottomShjet.root
        val horaActual = bindingBottomShjet.horaActual
        val fechaActual = bindingBottomShjet.FechaActual
        val tipoTienda = bindingBottomShjet.TipoTienda
        val localidaTienda = bindingBottomShjet.localidadTienda
        val nombreUser = bindingBottomShjet.camposPrincipales.nombreUser
        val apellidouser = bindingBottomShjet.camposPrincipales.apellidouser
        val dniUser = bindingBottomShjet.camposPrincipales.dniUser
        val numeroContacto = bindingBottomShjet.camposPrincipales.numeroContacto
        val horareserva = bindingBottomShjet.camposPrincipales.horareserva
        val horaint = bindingBottomShjet.camposPrincipales.horaint
        val localidadUser = bindingBottomShjet.camposPrincipales.localidadUser
        val RadioMetodoPago = bindingBottomShjet.metodoPagos.RadioMetodoPago
        val tituloItem = bindingBottomShjet.itemPreviewProductos.tituloItem
        val stokART = bindingBottomShjet.itemPreviewProductos.Stok
        val precioItem = bindingBottomShjet.itemPreviewProductos.precioItem
        val cantidad = bindingBottomShjet.itemPreviewProductos.cantidad
        val mas = bindingBottomShjet.itemPreviewProductos.mas
        val menos = bindingBottomShjet.itemPreviewProductos.menos
        val imgItem = bindingBottomShjet.itemPreviewProductos.imgItem
        val aumentarCantidadLineal = bindingBottomShjet.itemPreviewProductos.aumentarCantidadLineal
        val textoDriverPago = bindingBottomShjet.totalPagar
        val pagodeldriver = bindingBottomShjet.totalPagar.drivetxt
        val productosTotal = bindingBottomShjet.totalPagar.pagoProductos
        val totalPagar = bindingBottomShjet.totalPagar.totalPagar
        val totalLinealPagar = bindingBottomShjet.totalPagar.linealTotalPagar
        val linealDriver = bindingBottomShjet.totalPagar.linealDriver
        val layoutContainer = bindingBottomShjet.direccionEnvios.layoutContainer
        val delivery_precio = bindingBottomShjet.direccionEnvios.precioDelivery
        val longitudUser = bindingBottomShjet.direccionEnvios.longituduser
        val latitudUser = bindingBottomShjet.direccionEnvios.latitudUSer
        val direccionEntrega = bindingBottomShjet.direccionEnvios.direccionED
        val referenciaEntrega = bindingBottomShjet.direccionEnvios.referenciaED
        val reccileRadioBtn = bindingBottomShjet.direccionEnvios.reccileRadioBtn
        val creaDireccion = bindingBottomShjet.direccionEnvios.creaDireccion
        val metodoDelivery = bindingBottomShjet.metodoEntrega.delivery
        val idDireccionEvios = bindingBottomShjet.direccionEnvios.idSelecionado
        val radioGrupMetodoEntrega = bindingBottomShjet.metodoEntrega.RadiomodoEntrega
        val btnApply = bindingBottomShjet.btnApply
        var METODPAGO: String = ""
        var metodoEntrega = ""
        var precioITemSTR: String = ""
        var StokART_PROMO: String = ""

        obtnerDireciones(
            idDireccionEvios,
            idUSer,
            lista,
            reccileRadioBtn,
            creaDireccion,
            context,
            direccionEntrega,
            referenciaEntrega,
            latitudUser,
            longitudUser,
            delivery_precio,
            pagodeldriver,
            totalPagar
        )
        obtenerHorarioDeHoy(idTienda) { apertura, cierre, boolena ->
            bindingBottomShjet.horarioAtencion.text = "$apertura AM a $cierre PM"
        }
        mas.setOnClickListener {
            incrementarCantidad(
                cantidad,
                1,
                stokART,
                context,
                mas,
                productosTotal,
                precioItem,
                pagodeldriver,
                totalPagar
            )
        }
        menos.setOnClickListener {
            incrementarCantidad(
                cantidad,
                -1,
                stokART,
                context,
                menos,
                productosTotal,
                precioItem,
                pagodeldriver,
                totalPagar
            )
        }
        titulo.text = titulo_dialog
        creaDireccion.setOnClickListener {
            context.startActivity(Intent(context, direccion_entrega_lat_log::class.java))
            dialog.dismiss()
        }
        constantesCarrito.obtnerfechaHora(horaActual, fechaActual)
        constantesDatosUsuarioTienda.obtnerLocalidades(localidadUser)
        constantesCarrito.setearDatosUsuario { nombre, numero, localidad, apellido ->
            if (nombre == null || numero == null || localidad == null || apellido == null) {
                Log.e("user", "Error: Algunos datos son null")
            } else {
                nombreUser.setText(nombre)
                numeroContacto.setText(numero)
                localidadUser.setText(localidad)
                apellidouser.setText(apellido)
                constantesDatosUsuarioTienda.obtnerLocalidades(localidadUser)
            }
        }
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
            constantes_cotizacion_driver.obtenerCostoDelivery(longitudUser,
                latitudUser,
                idTienda,
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
        horareserva.setOnClickListener {
            mostrarFechaDialog_horaDialog.showTimePiker(context as AppCompatActivity, horareserva)
        }
        constantesDatosUsuarioTienda.obtenerDetallesTienda(idTienda) { localidad, tipo ->
            localidaTienda.text = localidad
            tipoTienda.text = tipo
            if (tipo == "virtual") {
                metodoDelivery.isVisible = true
                direccionEntrega.isVisible = true
                referenciaEntrega.isVisible = true
                layoutContainer.isVisible = true
                linealDriver.isVisible = true
                totalLinealPagar.isVisible = false
                metodoEntrega = "Delivery"

            } else {
                radioGrupMetodoEntrega.isVisible = true
                linealDriver.isVisible = false
                direccionEntrega.isVisible = false
                referenciaEntrega.isVisible = false
                layoutContainer.isVisible = false
                totalLinealPagar.isVisible = false
            }
        }
        RadioMetodoPago.setOnCheckedChangeListener { group, checkedId ->
            when (checkedId) {
                R.id.Efectivo -> {
                    METODPAGO = "EFECTIVO"
                    bindingBottomShjet.metodoPagos.layoutYape.isVisible = false
                    bindingBottomShjet.metodoPagos.pagoEfectivo.isVisible = true
                    bindingBottomShjet.metodoPagos.imagenYape.isVisible = false
                }

                R.id.Yape -> {
                    METODPAGO = "YAPE"
                    bindingBottomShjet.metodoPagos.imagenYape.isVisible = true
                    bindingBottomShjet.metodoPagos.layoutYape.isVisible = true
                    bindingBottomShjet.metodoPagos.pagoEfectivo.isVisible = false
                    bindingBottomShjet.metodoPagos.imagenPlin.isVisible = false
                }

                R.id.Plin -> {
                    METODPAGO = "PLIN"
                    bindingBottomShjet.metodoPagos.imagenYape.isVisible = false
                    bindingBottomShjet.metodoPagos.layoutYape.isVisible = true
                    bindingBottomShjet.metodoPagos.imagenPlin.isVisible = true
                    bindingBottomShjet.metodoPagos.pagoEfectivo.isVisible = false
                }

                else -> {
                    METODPAGO = ""
                }
            }

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
                    pagodeldriver.text = "0.0"
                    totalPagar.text = productosTotal.text
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

        when (tipo) {
            "reservaPromociones" -> {
                firebaseAuth = FirebaseAuth.getInstance()
                horaint.isVisible = true
                get_obtenerPromoSugeridas(
                    tituloItem,
                    precioItem,
                    productosTotal,
                    imgItem,
                    context,
                    idProductoClikado,
                    idTienda,
                    bindingBottomShjet,
                ) { precio, stok ->
                    precioITemSTR = precio
                    StokART_PROMO = stok
                    stokART.text = StokART_PROMO
                }
                btnApply.setOnClickListener {
                    val cantidad = cantidad.text.toString().trim()
                    val horaActual = horaActual.text.toString().trim()
                    val fechaActual = fechaActual.text.toString().trim()
                    val pagodeldriver = pagodeldriver.text.toString().trim().toDouble()
                    val totalPagar = totalPagar.text.toString().trim().toDouble()

                    convertirJSON(
                        idProductoClikado, precioItem.text.toString().toDouble(), 1
                    ) { json ->
                        val dataclas = dataclasscompra_reserva_promociones(

                            idPedido = constantesCarrito.generarCodigoPedido(),
                            idTienda = idTienda,
                            idUser = firebaseAuth.uid.toString(),
                            idRef_user = idDireccionEvios.text.toString().takeIf { it.isNotBlank() }
                                ?: "",

                            metodoEntrega = metodoEntrega,
                            metodoPago = METODPAGO,
                            precioDelivery = pagodeldriver,
                            productos = json, // Asegúrate de que este campo sea un String válido

                            tipoRealizado = "reserva_promociones",
                            totalCancelar = totalPagar,
                            totalDriver = pagodeldriver,
                            totalProductos = totalPagar,

                            estado = "pendiente",
                            estadoPedido = "pendiente",

                            fecha = fechaActual,
                            hora = horaActual,
                            hora_entrega_llegada = horareserva.text.toString(),
                            fecha_entrega_llegada = null,

                            idDriver = "", // Se deja vacío si no hay ID asignado
                        )
                        manejarClick(
                            "reserva_promo",
                            bindingBottomShjet,
                            context,
                            metodoEntrega,
                            idTienda,
                            localidaTienda,
                            localidadUser,
                            metodoDelivery,
                            METODPAGO
                        ) {
                            notificarCompraOReserva(
                                "idReservaPedido",
                                "reserva_promociones",
                                idTienda,
                                context,
                                constantesCarrito.generarCodigoPedido(),
                                dataclas, "reserva",
                                "Reserva de promocione",
                            ) { enviado ->
                                if (enviado) {
                                    dialog.dismiss()
                                }
                            }
                        }
                    }

                }
            }

            "compraPromociones" -> {
                firebaseAuth = FirebaseAuth.getInstance()
                horaint.isVisible = false
                get_obtenerPromoSugeridas(
                    tituloItem,
                    precioItem,
                    productosTotal,
                    imgItem,
                    context,
                    idProductoClikado,
                    idTienda,
                    bindingBottomShjet,

                    ) { precio, stok ->
                    precioITemSTR = precio
                    StokART_PROMO = stok
                    stokART.text = StokART_PROMO
                }

                btnApply.setOnClickListener {

                    val cantidad = cantidad.text.toString().trim().trim()
                    val horaActual = horaActual.text.toString().trim()
                    val fechaActual = fechaActual.text.toString().trim()
                    val pagodeldriver = pagodeldriver.text.toString().trim().toDouble()
                    val totalPagar = totalPagar.text.toString().toDouble()
                    convertirJSON(
                        idProductoClikado, precioItem.text.toString().toDouble(), 1
                    ) { json ->
                        val dataclas = dataclasscompra_reserva_promociones(

                            idPedido = constantesCarrito.generarCodigoPedido(),
                            idTienda = idTienda,
                            idUser = firebaseAuth.uid.toString(),
                            idRef_user = idDireccionEvios.text.toString().takeIf { it.isNotBlank() }
                                ?: "",

                            metodoEntrega = metodoEntrega,
                            metodoPago = METODPAGO,
                            precioDelivery = pagodeldriver,
                            productos = json, // Asegúrate de que este campo sea un String válido

                            tipoRealizado = "compra_promociones",
                            totalCancelar = totalPagar,
                            totalDriver = pagodeldriver,
                            totalProductos = totalPagar,

                            estado = "pendiente",
                            estadoPedido = "pendiente",

                            fecha = fechaActual,
                            hora = horaActual,
                            hora_entrega_llegada = null,
                            fecha_entrega_llegada = null,
                            idDriver = "", // Se deja vacío si no hay ID asignado
                        )
                        manejarClick(
                            "compra_promo",
                            bindingBottomShjet,
                            context,
                            metodoEntrega,
                            idTienda,
                            localidaTienda,
                            localidadUser,
                            metodoDelivery,
                            METODPAGO
                        ) {
                            notificarCompraOReserva(
                                "productos",
                                "compra_promociones",
                                idTienda,
                                context,
                                constantesCarrito.generarCodigoPedido(),
                                dataclas,
                                "compra",
                                "Compra de promocion"
                            ) { enviado ->
                                if (enviado) {
                                    dialog.dismiss()
                                }
                            }
                        }
                    }


                }
            }

            "reservaarticulos" -> {
                firebaseAuth = FirebaseAuth.getInstance()
                aumentarCantidadLineal.isVisible = true
                get_obtenerProductos(
                    tituloItem,
                    precioItem,
                    productosTotal,
                    imgItem,
                    context,
                    idProductoClikado,
                    idTienda,
                    bindingBottomShjet,
                ) { precio, stok ->
                    precioITemSTR = precio
                    StokART_PROMO = stok
                    stokART.text = StokART_PROMO
                }
                btnApply.setOnClickListener {
                    val cantidad = cantidad.text.toString().trim()
                    val horaActual = horaActual.text.toString().trim()
                    val fechaActual = fechaActual.text.toString().trim()
                    val pagodeldriver = pagodeldriver.text.toString().trim().toDouble()
                    val totalPagar = totalPagar.text.toString().trim().toDouble()
                    convertirJSON(
                        idProductoClikado, precioItem.text.toString().toDouble(), cantidad.toInt()
                    ) { json ->
                        val dataclas = dataclasscompra_reserva_promociones(

                            idPedido = constantesCarrito.generarCodigoPedido(),
                            idTienda = idTienda,
                            idUser = firebaseAuth.uid.toString(),
                            idRef_user = idDireccionEvios.text.toString().takeIf { it.isNotBlank() }
                                ?: "",

                            metodoEntrega = metodoEntrega,
                            metodoPago = METODPAGO,
                            precioDelivery = pagodeldriver,
                            productos = json, // Asegúrate de que este campo sea un String válido

                            tipoRealizado = "reserva_articulos",
                            totalCancelar = totalPagar,
                            totalDriver = pagodeldriver,
                            totalProductos = totalPagar,

                            estado = "pendiente",
                            estadoPedido = "pendiente",

                            fecha = fechaActual,
                            hora = horaActual,
                            hora_entrega_llegada = horareserva.text.toString(),
                            fecha_entrega_llegada = null,

                            idDriver = "", // Se deja vacío si no hay ID asignado

                        )
                        manejarClick(
                            "reserva_promo",
                            bindingBottomShjet,
                            context,
                            metodoEntrega,
                            idTienda,
                            localidaTienda,
                            localidadUser,
                            metodoDelivery,
                            METODPAGO
                        ) {
                            notificarCompraOReserva(
                                "idReservaPedido",
                                "reserva_articulos",
                                idTienda,
                                context,
                                constantesCarrito.generarCodigoPedido(),
                                dataclas, "reserva",
                                "Reserva de articulos",

                                ) { enviado ->
                                if (enviado) {
                                    dialog.dismiss()
                                }
                            }
                        }
                    }


                }
            }
        }

        dialog.setContentView(view)
    }

    fun convertirJSON(
        idClikado: String,
        PrecioItem: Double,
        cantidad: Int? = null,
        json: (String) -> Unit
    ) {
        val productoJson = JSONObject().apply {
            put("cantidad", cantidad)
            put("precio", PrecioItem)
        }
        val productosJson = JSONObject().apply {
            put(idClikado, productoJson)
        }
        json(productosJson.toString())

    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun validarCamposYHora(
        idTienda: String,
        metodoEntrega: String,
        binding: BottomSheetReservaProductosBinding,
        context: Context,
        callback: (Boolean) -> Unit,
    ) {
        compararHoraEntregaLLegada(
            binding.camposPrincipales.horareserva.text.toString(),
            binding.camposPrincipales.horareserva,
            idTienda
        ) { isWithinHours ->
            if (!isWithinHours) {
                Toast.makeText(
                    context, "Ingrese una hora dentro del horario de atención", Toast.LENGTH_SHORT
                ).show()
                callback(false)
                return@compararHoraEntregaLLegada
            }

            val verificacionCampos = verificarDeliveryCmapos(
                binding.direccionEnvios.direccionED,
                binding.direccionEnvios.referenciaED,
                binding.direccionEnvios.precioDelivery
            )
            if (metodoEntrega.equals("")) {
                Toast.makeText(context, "Seleccione su metodo de entrega", Toast.LENGTH_SHORT)
                    .show()
                callback(false)
                return@compararHoraEntregaLLegada
            }

            if (metodoEntrega == "Delivery" && !verificacionCampos) {
                Toast.makeText(context, "Formulario incompleto", Toast.LENGTH_SHORT).show()
                callback(false)
                return@compararHoraEntregaLLegada
            }

            if (!verificarCamposReservaCompra(context, binding, true)) {
                Toast.makeText(context, "Formulario incompleto", Toast.LENGTH_SHORT).show()
                callback(false)
                return@compararHoraEntregaLLegada
            }

            callback(true) // Si todas las validaciones pasan, llamar al callback con true
        }
    }


    private fun get_obtenerPromoSugeridas(
        tituloItem: TextView,
        precioItem: TextView,
        productosTotal: TextView,
        imgItem: ShapeableImageView,
        context: Context,
        idProductoClikado: String,
        idTienda: String,
        bindingBottomShjet: BottomSheetReservaProductosBinding,
        precioPROMO: (String, String) -> Unit,
    ) {
        val db = FirebaseFirestore.getInstance().collection("Tiendas").document(idTienda)
            .collection("promociones").document(idProductoClikado)
        db.get().addOnSuccessListener { res ->
            if (res.exists()) {
                val datos = res.data
                val imgArticulo = datos?.get("imagenUrl") as? String ?: ""
                val stok = datos?.get("stok") as? String ?: ""
                val nombreArticulo = datos?.get("titulo") as? String ?: ""
                val precio = datos?.get("precio") as? String ?: ""
                val plin = datos?.get("plin") as? Boolean ?: false
                val yape = datos?.get("yape") as? Boolean ?: false
                val efectivo = datos?.get("efectivo") as? Boolean ?: false
                val yapeRuta = "Tiendas/$idTienda/QR_pagos/Yape.jpg"
                val plinrRuta = "Tiendas/$idTienda/QR_pagos/Plin.jpg"
                precioPROMO(precio, stok)
                Log.d("precio_stok", "obtenemos precio y stok $precio $stok")
                constantesSetadas(
                    yapeRuta, plinrRuta, context, bindingBottomShjet, yape, efectivo, plin
                )
                verificarViivilidad_Pagos(
                    yape,
                    efectivo,
                    plin,
                    bindingBottomShjet.metodoPagos.Yape,
                    bindingBottomShjet.metodoPagos.Efectivo,
                    bindingBottomShjet.metodoPagos.Plin
                )
                setearIMGART(
                    tituloItem,
                    nombreArticulo,
                    precioItem,
                    precio,
                    productosTotal,
                    context,
                    imgArticulo,
                    imgItem
                )

            } else {
                Log.e("getErrorArt", "error")
                precioPROMO("", "")
            }
        }.addOnSuccessListener { res ->
            Log.e("getErrorArt", "error al obtener el articulo de la db")
        }
    }

    private fun get_obtenerProductos(
        tituloItem: TextView,
        precioItem: TextView,
        productosTotal: TextView,
        imgItem: ShapeableImageView,
        context: Context,
        idProductoClikado: String,
        idTienda: String,
        bindingBottomShjet: BottomSheetReservaProductosBinding,
        precioITEM: (String, String) -> Unit,
    ) {
        val db = FirebaseFirestore.getInstance().collection("Tiendas").document(idTienda)
            .collection("articulos").document(idProductoClikado)
        db.get().addOnSuccessListener { res ->
            if (res.exists()) {
                val datos = res.data
                val imgArticulo = datos?.get("imgArticulo") as? String ?: ""
                val nombreArticulo = datos?.get("nombreArticulo") as? String ?: ""
                val precio = datos?.get("precio") as? String ?: ""
                val plin = datos?.get("plin") as? Boolean ?: false
                val yape = datos?.get("yape") as? Boolean ?: false
                val cantidad = datos?.get("cantidad") as? String ?: ""
                val efectivo = datos?.get("efectivo") as? Boolean ?: false
                val yapeRuta = "Tiendas/$idTienda/QR_pagos/Yape.jpg"
                val plinrRuta = "Tiendas/$idTienda/QR_pagos/Plin.jpg"
                precioITEM(precio, cantidad)
                constantesSetadas(
                    yapeRuta, plinrRuta, context, bindingBottomShjet, yape, efectivo, plin
                )
                verificarViivilidad_Pagos(
                    yape,
                    efectivo,
                    plin,
                    bindingBottomShjet.metodoPagos.Yape,
                    bindingBottomShjet.metodoPagos.Efectivo,
                    bindingBottomShjet.metodoPagos.Plin
                )
                setearIMGART(
                    tituloItem,
                    nombreArticulo,
                    precioItem,
                    precio,
                    productosTotal,
                    context,
                    imgArticulo,
                    imgItem
                )

            } else {
                Log.e("getErrorArt", "error")
                precioITEM("", "")
            }
        }.addOnSuccessListener { res ->
            Log.e("getErrorArt", "error al obtener el articulo de la db")
        }
    }


    fun dialogo_solo_envios_localidadTienda(context: Context) {
        val builder = AlertDialog.Builder(context)
        builder.setTitle("ATENCIÓN")
            .setMessage("Los envíos por delivery solo están disponibles para localidades iguales a las tiendas negocios o locales.")
            .setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }.create().show()
    }

    private fun validacionCamposUsuario(
        bindingBottomShjet: BottomSheetReservaProductosBinding,
        context: Context,
        METODPAGO: String,
    ): Boolean {
        val nombre = bindingBottomShjet.camposPrincipales.nombreUser.text.toString().trim()
        val apellido = bindingBottomShjet.camposPrincipales.apellidouser.text.toString().trim()
        val numero = bindingBottomShjet.camposPrincipales.numeroContacto.text.toString().trim()
        val dni = bindingBottomShjet.camposPrincipales.dniUser.text.toString().trim()
        return when {
            nombre.isEmpty() -> {
                bindingBottomShjet.camposPrincipales.nombreUser.error = "El campo es requerido"
                Toast.makeText(context, "Campo nombre vacio", Toast.LENGTH_SHORT).show()
                false
            }

            apellido.isEmpty() -> {
                bindingBottomShjet.camposPrincipales.apellidouser.error = "El campo es requerido"
                Toast.makeText(context, "Campo apellido vacio", Toast.LENGTH_SHORT).show()
                false
            }

            numero.isEmpty() -> {
                bindingBottomShjet.camposPrincipales.numeroContacto.error = "El campo es requerido"
                Toast.makeText(context, "Campo numero vacio", Toast.LENGTH_SHORT).show()
                false
            }

            dni.isEmpty() -> {
                bindingBottomShjet.camposPrincipales.dniUser.error = "El campo es requerido"
                Toast.makeText(context, "Campo DNI vacio", Toast.LENGTH_SHORT).show()
                false
            }

            METODPAGO.isEmpty() -> {
                Toast.makeText(context, "Seleccione un método de pago", Toast.LENGTH_SHORT).show()
                false
            }

            else -> true
        }
    }


    fun constantesSetadas(
        yapeRuta: String,
        plinrRuta: String,
        context: Context,
        bindingBottomShjet: BottomSheetReservaProductosBinding,
        yape: Boolean,
        efectivo: Boolean,
        plin: Boolean,
    ) {
        constantesImagenes.obtenerURLDescarga(
            context, bindingBottomShjet.metodoPagos.imagenYape, yapeRuta
        )
        constantesImagenes.obtenerURLDescarga(
            context, bindingBottomShjet.metodoPagos.imagenPlin, plinrRuta
        )

        verificarViivilidad_Pagos(
            yape,
            efectivo,
            plin,
            bindingBottomShjet.metodoPagos.Yape,
            bindingBottomShjet.metodoPagos.Efectivo,
            bindingBottomShjet.metodoPagos.Plin
        )

    }

    private fun setearIMGART(
        tituloItem: TextView,
        nombreArticulo: String,
        precioItem: TextView,
        precio: String,
        productosTotal: TextView,
        context: Context,
        imgArticulo: String,
        imgItem: ShapeableImageView,
    ) {
        tituloItem.text = nombreArticulo
        precioItem.text = precio
        productosTotal.text = precio
        Glide.with(context).load(imgArticulo).into(imgItem)
    }

    private fun incrementarCantidad(
        cantidad: TextView,
        incremento: Int,
        stock: TextView,
        context: Context,
        btnApply: ImageButton,
        precioAumentado: TextView,
        precioUnitario: TextView,
        precioDElivery: TextView,
        totalAPAGARGENERAL: TextView,
    ) {
        val cantidadSTR = cantidad.text.toString()

        val precioUnitarioDouble = precioUnitario.text.toString().toDoubleOrNull() ?: 0.0
        val precioDriver = precioDElivery.text.toString().toDoubleOrNull() ?: 0.0
        val stockINT = stock.text.toString().toDoubleOrNull() ?: 0.0
        val cantidadINT = if (cantidadSTR.isNotBlank()) cantidadSTR.toInt() else 0
        val nuevaCantidad = cantidadINT + incremento


        if (nuevaCantidad > stockINT) {
            Toast.makeText(
                context, "La cantidad no puede superar al stock disponible", Toast.LENGTH_SHORT
            ).show()
            btnApply.isEnabled = true
        } else if (nuevaCantidad < 1) {
            Toast.makeText(context, "La cantidad no puede ser menor que 1", Toast.LENGTH_SHORT)
                .show()
            btnApply.isEnabled = true
        } else {
            cantidad.text = nuevaCantidad.toString()
            btnApply.isEnabled = true

            val precioTotal = nuevaCantidad * precioUnitarioDouble
            precioAumentado.text = String.format("%.2f", precioTotal)
            totalAPAGARGENERAL.text = (precioTotal + precioDriver).toString()
        }
    }


    fun obtnerDireciones(
        TexviewIDRefDire: TextView,
        idUSer: String,
        lista: MutableList<dataclassradiobtn>,
        RecyclerView: RecyclerView,
        btnCrearDirecion: Button,
        context: Context,
        direccion: EditText,
        refererencia: EditText,
        latUSser: TextView,
        logUSer: TextView,
        precioDElivery: AutoCompleteTextView,
        pagoDriver: TextView, total_Pagar: TextView,
    ) {
        val dbTrabajadores =
            FirebaseFirestore.getInstance().collection("Trabajadores_Usuarios_Drivers")
                .document("trabajadores").collection("trabajadores").document(idUSer)
                .collection("ubicacion")

        val dbUsuarios = FirebaseFirestore.getInstance().collection("Trabajadores_Usuarios_Drivers")
            .document("usuarios").collection("usuarios").document(idUSer).collection("ubicacion")
        lista.clear()
        dbTrabajadores.get().addOnSuccessListener { res ->
            for (datos in res) {
                val data = datos.data
                val lat = data?.get("lat") as? String ?: ""
                val log = data?.get("log") as? String ?: ""
                val direccion = data?.get("direccion") as? String ?: ""
                val id = data?.get("id") as? String ?: ""
                val referencia = data?.get("referencia") as? String ?: ""
                val nombre = data?.get("nombre") as? String ?: ""


                val dataclass = dataclassradiobtn(id, lat, log, referencia, direccion, nombre)
                lista.add(dataclass)
            }
            if (lista.isEmpty()) {
                dbUsuarios.get().addOnSuccessListener { resUsuarios ->
                    for (datos in resUsuarios) {
                        val data = datos.data
                        val lat = data?.get("lat") as? String ?: ""
                        val log = data?.get("log") as? String ?: ""
                        val direccion = data?.get("direccion") as? String ?: ""
                        val id = data?.get("id") as? String ?: ""
                        val referencia = data?.get("referencia") as? String ?: ""
                        val nombre = data?.get("nombre") as? String ?: ""
                        val dataclass =
                            dataclassradiobtn(id, lat, log, referencia, direccion, nombre)
                        lista.add(dataclass)
                    }
                    if (lista.isEmpty()) {
                        RecyclerView.isVisible = false
                        btnCrearDirecion.isVisible = true
                    } else {
                        RecyclerView.isVisible = true
                        btnCrearDirecion.isVisible = false
                        inicializarRecicle(
                            TexviewIDRefDire,
                            RecyclerView,
                            context,
                            lista,
                            direccion,
                            refererencia,
                            latUSser,
                            logUSer,
                            precioDElivery,
                            pagoDriver,
                            total_Pagar
                        )
                    }
                }.addOnFailureListener { e ->
                    Toast.makeText(
                        context, "Error al buscar en usuarios: ${e.message}", Toast.LENGTH_SHORT
                    ).show()
                }
            } else {
                RecyclerView.isVisible = true
                btnCrearDirecion.isVisible = false
                inicializarRecicle(
                    TexviewIDRefDire,
                    RecyclerView,
                    context,
                    lista,
                    direccion,
                    refererencia,
                    latUSser,
                    logUSer,
                    precioDElivery,
                    pagoDriver,
                    total_Pagar
                )
            }
        }.addOnFailureListener { e ->
            Toast.makeText(
                context, "Error al buscar en trabajadores: ${e.message}", Toast.LENGTH_SHORT
            ).show()
        }
    }

    fun setearTotal(
        total_Pagar: TextView,
        totalDelivery: TextView,
        precioTotalProductos: TextView,
        delivery: Int,
    ) {
        val estandar_precio = (delivery.toString()).toDouble()
        val total_ART = (precioTotalProductos.text.toString()).toDouble()
        val totalPagar = estandar_precio + total_ART
        totalDelivery.text =
            constantes_cotizacion_driver.formatearNumeros(estandar_precio.toString())
        total_Pagar.text = constantes_cotizacion_driver.formatearNumeros(totalPagar.toString())
    }

    private fun inicializarRecicle(
        TexviewIDRefDire: TextView,
        recycle: RecyclerView,
        context: Context,
        items: MutableList<dataclassradiobtn>,
        direccion: EditText,
        refererencia: EditText,
        latUSser: TextView,
        logUSer: TextView,
        precioDElivery: AutoCompleteTextView,
        pagoDriver: TextView,
        totalPAgar: TextView,
    ) {

        val adapter = adapter_radioButton_envios(
            items,
            onItemClick = { ubicacionSeleccionada ->
                direccion.setText(ubicacionSeleccionada.direccion)
                refererencia.setText(ubicacionSeleccionada.referencia)
                latUSser.text = ubicacionSeleccionada.lat
                logUSer.text = ubicacionSeleccionada.log
                TexviewIDRefDire.text = ubicacionSeleccionada.id
                precioDElivery.setText("")
                constantes_cotizacion_driver.setearDelivery(precioDElivery, context)
                pagoDriver.text = "***"
                totalPAgar.text = "***"
            },
            onCrearDireccionClick = {
                // Acción cuando se presiona el botón "Crear dirección"
                println("Se hizo clic en 'Crear dirección'")
            }
        )


        recycle.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        recycle.adapter = adapter
    }

    fun verificarViivilidad_Pagos(
        yape: Boolean,
        efectivo: Boolean,
        plin: Boolean,
        Yape: RadioButton,
        Efectivo: RadioButton,
        Plin: RadioButton,
    ) {
        if (yape == true && efectivo == true && plin) {
            Yape.isVisible = true
            Efectivo.isVisible = true
            Yape.isVisible = true
            Plin.isVisible = true
        } else if (yape == true && efectivo == true) {
            Yape.isVisible = true
            Efectivo.isVisible = true
            Yape.isVisible = true
            Plin.isVisible = false
        } else if (yape == true && plin) {
            Yape.isVisible = true
            Efectivo.isVisible = false
            Yape.isVisible = true
            Plin.isVisible = true
        } else if (yape == true) {
            Yape.isVisible = true
            Efectivo.isVisible = false
            Yape.isVisible = true
            Plin.isVisible = false
        } else if (efectivo == true) {
            Yape.isVisible = false
            Efectivo.isVisible = true
            Yape.isVisible = false
            Plin.isVisible = false
        } else if (plin) {
            Plin.isVisible = true
            Yape.isVisible = false
            Efectivo.isVisible = false
            Yape.isVisible = false
        } else {
            Yape.isVisible = false
            Efectivo.isVisible = false
            Yape.isVisible = false
            Plin.isVisible = false
        }
    }

    private fun verificarDeliveryCmapos(
        direcion: EditText,
        refererencia: EditText,
        precio_driver: EditText,
    ): Boolean {
        var isValid = true

        if (direcion.text.isEmpty()) {
            direcion.error = "Seleccione una de sus direcciones de entrega"
            isValid = false
        }

        if (refererencia.text.isEmpty()) {
            refererencia.error = "Seleccione una de sus direcciones de entrega"
            isValid = false
        }

        if (precio_driver.text.isEmpty()) {
            precio_driver.error = "Seleccione su precio de driver"

            isValid = false
        }

        return isValid
    }

    private fun verificarCamposReservaCompra(
        context: Context,
        bindingBottomShjet: BottomSheetReservaProductosBinding,
        verificarHora: Boolean,
    ): Boolean {
        var todosLosCamposValidos = true

        if (bindingBottomShjet.camposPrincipales.nombreUser.text.isEmpty()) {
            bindingBottomShjet.camposPrincipales.nombreUser.error = "El campo de nombre está vacío"
            Toast.makeText(context, "el campo de nombre esta vacio", Toast.LENGTH_SHORT).show()
            todosLosCamposValidos = false
        }
        if (bindingBottomShjet.camposPrincipales.apellidouser.text.isEmpty()) {
            bindingBottomShjet.camposPrincipales.apellidouser.error =
                "El campo de apellido está vacío"
            Toast.makeText(context, "el campo de apellido esta vacio", Toast.LENGTH_SHORT).show()
            todosLosCamposValidos = false
        }
        if (bindingBottomShjet.camposPrincipales.dniUser.text.isEmpty()) {
            bindingBottomShjet.camposPrincipales.dniUser.error = "El campo de DNI está vacío"
            Toast.makeText(context, "el campo de DNI esta vacio", Toast.LENGTH_SHORT).show()
            todosLosCamposValidos = false
        }
        if (bindingBottomShjet.camposPrincipales.numeroContacto.text.isEmpty()) {
            bindingBottomShjet.camposPrincipales.numeroContacto.error =
                "El campo de número está vacío"
            Toast.makeText(context, "el campo de número esta vacio", Toast.LENGTH_SHORT).show()
            todosLosCamposValidos = false
        }
        if (bindingBottomShjet.camposPrincipales.localidadUser.text.isEmpty()) {
            bindingBottomShjet.camposPrincipales.localidadUser.error =
                "El campo de localidad está vacío"
            Toast.makeText(context, "el campo de localidad esta vacio", Toast.LENGTH_SHORT).show()
            todosLosCamposValidos = false
        }

        if (verificarHora && bindingBottomShjet.camposPrincipales.horareserva.text.isEmpty()) {
            bindingBottomShjet.camposPrincipales.horareserva.error = "El campo de hora está vacío"
            Toast.makeText(context, "el campo de hora esta vacio", Toast.LENGTH_SHORT).show()
            todosLosCamposValidos = false
        }

        return todosLosCamposValidos
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun compararHoraEntregaLLegada(
        horaSeleccionada: String,
        editextHora: EditText,
        idTienda: String,
        callback: (Boolean) -> Unit,
    ) {
        val formatoHora = SimpleDateFormat("HH:mm", Locale.getDefault())

        if (editextHora.text.isEmpty()) {
            editextHora.error = "Ingrese una hora"
            callback(false)
            return
        }
        obtenerHorarioDeHoy(idTienda) { abierto, cerrado, boolean ->
            Log.d(
                "hora_comparada",
                "Las horas a comparar son $abierto AM tienda, $cerrado PM tienda, $horaSeleccionada hora seleccionada por el usuario"
            )
            val horaSeleccionadaDate: Date? = formatoHora.parse(horaSeleccionada)
            val aperturaDate: Date? = formatoHora.parse(abierto.toString())
            val cierreDate: Date? = formatoHora.parse(cerrado.toString())
            if (horaSeleccionadaDate == null || aperturaDate == null || cierreDate == null) {
                Log.e("hora_comparada", "Error al analizar las horas.")
                callback(false)
                return@obtenerHorarioDeHoy
            }
            val isWithinHours = if (cierreDate.before(aperturaDate)) {
                !(horaSeleccionadaDate.before(aperturaDate) && horaSeleccionadaDate.after(cierreDate))
            } else {
                !(horaSeleccionadaDate.before(aperturaDate) || horaSeleccionadaDate.after(cierreDate))
            }

            callback(isWithinHours) // Llamar al callback con el resultado de la comparación
        }
    }
    fun obtenerDiaActualClaveFirestore(): String {
        return obtenerDiaActualEnEspañol().lowercase()
    }

    fun obtenerHorarioDeHoy_BOX(horario: HorarioAtencion_box): HorarioDia_bloques {
        return when (obtenerDiaActualClaveFirestore()) {
            "lunes" -> horario.lunes
            "martes" -> horario.martes
            "miércoles" -> horario.miércoles
            "jueves" -> horario.jueves
            "viernes" -> horario.viernes
            "sábado" -> horario.sábado
            "domingo" -> horario.domingo
            else -> HorarioDia_bloques()
        }
    }



    @RequiresApi(Build.VERSION_CODES.O)
    fun obtenerHorarioDeHoy(
        storeId: String,
        callback: (LocalTime, LocalTime, cerrado: Boolean) -> Unit,
    ) {
        val db = FirebaseFirestore.getInstance()
        val hoy = DayOfWeek.from(
            Calendar.getInstance().time.toInstant().atZone(ZoneId.systemDefault()).toLocalDate()
        ).name.lowercase(
            Locale.ROOT
        )

        val diasEnInglesAEs = when (hoy) {
            "monday" -> "lunes"
            "tuesday" -> "martes"
            "wednesday" -> "miercoles"
            "thursday" -> "jueves"
            "friday" -> "viernes"
            "saturday" -> "sabado"
            "sunday" -> "domingo"
            else -> {
                "lunes"
            }
        }
        Log.d("hoy", "el dia de hoy es $diasEnInglesAEs")


        val horarioRef = db.collection("Tiendas").document(storeId).collection("horario")
            .document(diasEnInglesAEs.toString())

        horarioRef.get().addOnSuccessListener { document ->
            if (document != null && document.exists()) {
                val aperturaStr = document.getString("h_apertura") ?: "00:00"
                val cierreStr = document.getString("h_cierre") ?: "00:00"
                val cerrado = document.getBoolean("cerrado") ?: false
                val formatter = DateTimeFormatter.ofPattern("HH:mm")
                val apertura = LocalTime.parse(aperturaStr, formatter)
                val cierre = LocalTime.parse(cierreStr, formatter)

                callback(apertura, cierre, cerrado)
                Log.d("hoy", "la apertura es $apertura y el cierre es $cierre")
            } else {
                println("No se encontró el documento para hoy")
            }
        }.addOnFailureListener { exception ->
            exception.printStackTrace()
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun manejarClick(
        tipo: String,
        bindingBottomShjet: BottomSheetReservaProductosBinding,
        context: Context,
        metodoEntrega: String,
        idTienda: String,
        localidaTienda: TextView,
        localidadUser: EditText,
        metodoDelivery: TextView,
        METODPAGO: String,
        funcionEspecifica: () -> Unit,
    ) {
        val localidadUserTxt = localidadUser.text.toString()
        val deliveryText = metodoDelivery.text.toString()

        when (tipo) {
            "compra_promo" -> {
                if (!validacionCamposUsuario(bindingBottomShjet, context, METODPAGO)) {
                    return
                }

                if (metodoEntrega.isEmpty()) {
                    Toast.makeText(context, "Seleccione su método de entrega", Toast.LENGTH_SHORT)
                        .show()
                    return
                }

                if (metodoEntrega.equals("Recojo en tienda")) {
                    funcionEspecifica()
                    return
                }

                if (metodoEntrega == "Delivery" || deliveryText == "delivery") {
                    val verificacionCampos = verificarDeliveryCmapos(
                        bindingBottomShjet.direccionEnvios.direccionED,
                        bindingBottomShjet.direccionEnvios.referenciaED,
                        bindingBottomShjet.direccionEnvios.precioDelivery
                    )
                    if (!verificacionCampos) {
                        return
                    }

                    if (localidadUserTxt != localidaTienda.text.toString()) {
                        dialogo_solo_envios_localidadTienda(context)
                        return
                    }
                }

                funcionEspecifica()
            }


            "reserva_promo" -> {
                validarCamposYHora(
                    idTienda,
                    metodoEntrega,
                    bindingBottomShjet,
                    context,
                ) { isValid ->
                    if (isValid) {
                        if (!validacionCamposUsuario(bindingBottomShjet, context, METODPAGO)) {
                            return@validarCamposYHora
                        } else {
                            if ((metodoEntrega == "Delivery" || deliveryText == "delivery") && localidadUserTxt != localidaTienda.text.toString()) {
                                dialogo_solo_envios_localidadTienda(context)
                            } else {
                                funcionEspecifica()
                            }
                        }
                    } else {
                        Toast.makeText(
                            context, "error al realizar el envio", Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }

            "reserva_articulo" -> {}
        }

    }

    private fun notificarCompraOReserva(
        tipoProdutoOreser: String,
        child: String,
        idTienda: String,
        context: Context,
        idART: String,
        dataclasscompraReservaPromociones: dataclasscompra_reserva_promociones,
        tipoChil1: String,
        tipoOperacion: String,
        enviado: (Boolean) -> Unit,

        ) {
        firebaseAuth = FirebaseAuth.getInstance()
        val realtimeInstance = FirebaseDatabase.getInstance()
        val CompraTienda = realtimeInstance.getReference("CompraTienda").child(idTienda)
            .child(firebaseAuth.uid.toString()).child(tipoChil1).child(idART)
        val PedidosUsuario =
            realtimeInstance.getReference("PedidosUsuario").child(firebaseAuth.uid.toString())
                .child("tiendas").child(idTienda).child(tipoChil1).child(idART)

        val hashMap = hashMapOf<String, Any>(
            "idPedido" to idART,
            "idTienda" to idTienda,
            "idUser" to firebaseAuth.uid.toString(),
            "idRef_user" to (dataclasscompraReservaPromociones.idRef_user ?: ""), // Se deja en null ya que es opcional

            "metodoEntrega" to (dataclasscompraReservaPromociones.metodoEntrega.takeIf {
                it?.isNotBlank() ?: false
            } ?: "Delivery"),

            "metodoPago" to dataclasscompraReservaPromociones.metodoPago.toString(),
            "precioDelivery" to dataclasscompraReservaPromociones.totalDriver!!,  // Usamos Total_driver como precioDelivery

            "productos" to dataclasscompraReservaPromociones.productos!!, // Aquí deberías pasar el JSON de los productos si es necesario

            "tipoRealizado" to dataclasscompraReservaPromociones.tipoRealizado!!,
            "totalCancelar" to dataclasscompraReservaPromociones.totalCancelar!!,
            "totalDriver" to dataclasscompraReservaPromociones.totalDriver!!,
            "totalProductos" to dataclasscompraReservaPromociones.totalProductos!!,

            "estado" to "pendiente",
            "estadoPedido" to "pendiente",

            "fecha" to dataclasscompraReservaPromociones.fecha!!,
            "hora" to dataclasscompraReservaPromociones.hora!!,
            "hora_entrega_llegada" to (dataclasscompraReservaPromociones.hora_entrega_llegada?: ""),
            "fecha_entrega_llegada" to (dataclasscompraReservaPromociones.fecha_entrega_llegada ?: ""),



            "idDriver" to "" // Se deja vacío si no hay ID asignado
        )


        PedidosUsuario.setValue(hashMap).addOnSuccessListener {
            Toast.makeText(context, "Operación realizada correctamente", Toast.LENGTH_SHORT)
                .show()
            constantes.obtnerTokenTienda(idTienda) { token ->
                GlobalScope.launch {
                    val enviar_notificaciones = NotificacionRS()
                    enviar_notificaciones.sendNotification_con_parametros(
                        "IDTienda",
                        idTienda,
                        "INICIO",
                        context,
                        token,
                        "Operación de $tipoOperacion: ${dataclasscompraReservaPromociones.idPedido}",
                        "Tienes una nueva operación de $tipoOperacion. Haz clic para aceptar o rechazar la operación"
                    )
                }
                enviado(true)
            }
        }.addOnFailureListener {
            enviado(false)
            Toast.makeText(context, "Error al realizar la operación", Toast.LENGTH_SHORT).show()
        }

        CompraTienda.setValue(hashMap).addOnSuccessListener {
            enviado(true)
            Log.d("operacion_$tipoOperacion", "Operación realizada correctamente")
        }.addOnFailureListener {
            enviado(false)
            Log.d("operacion_$tipoOperacion", "Error al realizar la operación")
        }
    }


}