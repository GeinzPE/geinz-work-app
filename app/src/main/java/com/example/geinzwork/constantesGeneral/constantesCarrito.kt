package com.geinzz.geinzwork.constantesGeneral


import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.geinzwork.NotificacionRS
import com.example.geinzwork.constantesGeneral.constantes_bottomShet_fourdItem
import com.geinzz.geinzwork.R
import com.geinzz.geinzwork.adapterViewholder.adapterHistorial
import com.geinzz.geinzwork.adapterViewholder.adapterReservaSevicios
import com.geinzz.geinzwork.adapterViewholder.adapterReservas
import com.geinzz.geinzwork.dataclass.dataclassCarritoCompra
import com.geinzz.geinzwork.dataclass.dataclassHistorial
import com.geinzz.geinzwork.dataclass.dataclassServiciosHistorial
import com.geinzz.geinzwork.dataclass.dataclassreservas
import com.geinzz.geinzwork.vistaTiendas.generarQRPedidoArticulos
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.TaskCompletionSource
import com.google.android.material.imageview.ShapeableImageView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.firestore.FirebaseFirestore
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

object constantesCarrito {
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var historialAdapter: adapterHistorial
    fun agregarCompra(
        dataclassCarritoCompra: dataclassCarritoCompra,
        context: Context,
    ): Task<Boolean> {
        val taskCompletionSource = TaskCompletionSource<Boolean>()
        firebaseAuth = FirebaseAuth.getInstance()

        val reaaltime =
            FirebaseDatabase.getInstance().getReference("PedidosUsuario")
                .child(firebaseAuth.uid.toString()).child("tiendas")
                .child(dataclassCarritoCompra.idTienda.toString()).child("compra")
                .child(dataclassCarritoCompra.idPedido.toString())

        val realtimeTienda =
            FirebaseDatabase.getInstance().getReference("CompraTienda")
                .child(dataclassCarritoCompra.idTienda.toString())
                .child(firebaseAuth.uid.toString()).child("compra")
                .child(dataclassCarritoCompra.idPedido.toString())

        val hashMapCompra = hashMapOf<String, Any>(
            // Información del pedido y driver
            "idDriver" to dataclassCarritoCompra.idDriver.toString(),
            "idPedido" to dataclassCarritoCompra.idPedido.toString(),
            "estado" to dataclassCarritoCompra.estado.toString(),
            "metodoPago" to dataclassCarritoCompra.metodoPago.toString(),
            "productos" to dataclassCarritoCompra.productos.toString(),
            "estadoPedido" to dataclassCarritoCompra.estadoPedido.toString(),
            "metodoEntrega" to dataclassCarritoCompra.metodoEntrega.toString(),
            "tipoRealizado" to dataclassCarritoCompra.tipoRealizado.toString(),
            "idRef_user" to dataclassCarritoCompra.idRef_user.toString(),

            // Totales para cancelar
            "precioDelivery" to dataclassCarritoCompra.precioDelivery.toString().toDouble(),
            "totalCancelar" to dataclassCarritoCompra.totalCancelar.toString().toDouble(),
            "totalDriver" to dataclassCarritoCompra.totalDriver.toString().toDouble(),
            "totalProductos" to dataclassCarritoCompra.totalProductos.toString().toDouble(),
            "fecha_entrega_llegada" to "",
            "hora_entrega_llegada" to "",
            // Fecha y hora
            "fecha" to dataclassCarritoCompra.fecha.toString(),
            "hora" to dataclassCarritoCompra.hora.toString(),

            // Información de la tienda
            "idTienda" to dataclassCarritoCompra.idTienda.toString(),

            // Información del usuario
            "idUser" to dataclassCarritoCompra.idUser.toString(),
            "metodoEntrega" to (dataclassCarritoCompra.metodoEntrega.takeIf {
                it?.isNotBlank() ?: false
            } ?: "Delivery"),
        )

        reaaltime.setValue(hashMapCompra)
            .addOnSuccessListener {
                realtimeTienda.setValue(hashMapCompra)
                    .addOnSuccessListener {
                        Toast.makeText(
                            context,
                            "Compra realizada correctamente",
                            Toast.LENGTH_SHORT
                        ).show()
                        taskCompletionSource.setResult(true)
                        constantes.obtnerTokenTienda(dataclassCarritoCompra.idTienda.toString()) { token ->
                            GlobalScope.launch {
                                val enviar_notificaciones = NotificacionRS()
                                enviar_notificaciones.sendNotification_con_parametros(
                                    "IDTienda",
                                    dataclassCarritoCompra.idTienda.toString(),
                                    "INICIO",
                                    context,
                                    token,
                                    "Nuevo pedido ${dataclassCarritoCompra.idPedido.toString()}",
                                    "Tienes un nuevo pedido clik para confirmarlo o rechazarlo"
                                )
                            }
                        }
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(
                            context,
                            "Error al insertar en CompraTienda",
                            Toast.LENGTH_SHORT
                        ).show()
                        taskCompletionSource.setResult(false)  // Error en la segunda inserción
                    }
            }
            .addOnFailureListener { e ->
                Toast.makeText(context, "Error al insertar en PedidosUsuario", Toast.LENGTH_SHORT)
                    .show()
                taskCompletionSource.setResult(false)  // Error en la primera inserción
            }

        return taskCompletionSource.task
    }


    fun agregarReservaServicios(
        nombreUser: String,
        apellido: String,
        dni: String,
        codigoReserva: String,
        fecha: String,
        hora: String,
        metodopago: String,
        tipoSerivicio: String,
        idServico: String,
        idTienda: String,
        numero: String,
        total: String,
        precioServicio: String,
        fechaLlegada: String,
        horallegada: String,
        context: Context,
    ) {
        firebaseAuth = FirebaseAuth.getInstance()
        val reaaltime =
            FirebaseDatabase.getInstance().getReference("PedidosUsuario")
                .child(firebaseAuth.uid.toString()).child("tiendas").child(idTienda)
                .child("reserva").child(codigoReserva)
        val realtimeTienda =
            FirebaseDatabase.getInstance().getReference("CompraTienda").child(idTienda).child(
                firebaseAuth.uid.toString()
            ).child("reserva").child(codigoReserva)
        constantes_bottomShet_fourdItem.convertirJSON(
            idServico,
            precioServicio.toDouble(),
            1
        ) { json ->

            val hashMapCompra = hashMapOf<String, Any>(
                "estado" to "pendiente",
                "estadoPedido" to "pendiente",
                "fecha" to fecha,
                "fecha_entrega_llegada" to fechaLlegada,
                "hora" to hora,
                "hora_entrega_llegada" to horallegada,
                "idDriver" to "",
                "idPedido" to codigoReserva,
                "idRef_user" to "",
                "idTienda" to idTienda,
                "idUser" to firebaseAuth.uid.toString(),
                "metodoEntrega" to "",
                "metodoPago" to metodopago,
                "precioDelivery" to 0,
                "productos" to json,
                "tipoRealizado" to "reserva_servicios",
                "totalCancelar" to total.toDouble(),
                "totalDriver" to 0,
                "totalProductos" to total.toDouble(),
            )
            reaaltime.setValue(hashMapCompra).addOnSuccessListener {
                Toast.makeText(context, "reserva exitosamente ", Toast.LENGTH_SHORT).show()
                realtimeTienda.setValue(hashMapCompra).addOnSuccessListener {
                    Toast.makeText(context, "reserva exitosamente ", Toast.LENGTH_SHORT).show()
                }.addOnFailureListener {
                    Toast.makeText(context, "error al reservar el servicio", Toast.LENGTH_SHORT)
                        .show()
                }
            }.addOnFailureListener {
                Toast.makeText(context, "error al reservar el servicio", Toast.LENGTH_SHORT).show()
            }
        }

    }

    fun obtenerReservaServicios(
        filtrado: String,
        recicle: RecyclerView,
        context: Context,
        lista: MutableList<dataclassServiciosHistorial>,
    ) {
        firebaseAuth = FirebaseAuth.getInstance()
        val userId = firebaseAuth.currentUser?.uid

        if (userId != null) {
            val database = FirebaseDatabase.getInstance()
            val usuarioRef = database.getReference("PedidosUsuario/$userId/tiendas")
            usuarioRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    lista.clear()
                    for (tiendaSnapshot in dataSnapshot.children) {
                        val idTienda = tiendaSnapshot.key
                        // Obtener compras de la tienda actual
                        val comprasRef =
                            database.getReference("PedidosUsuario/$userId/tiendas/$idTienda/reserva")
                        comprasRef.addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onDataChange(comprasSnapshot: DataSnapshot) {
                                for (compraSnapshot in comprasSnapshot.children) {
                                    val historialServicio = dataclassServiciosHistorial(
                                        // Datos del usuario
                                        idCompra = compraSnapshot.key, // idCompra es equivalente a idPedido en el primer bloque
                                        adelanto = compraSnapshot.child("adelanto")
                                            .getValue(String::class.java),
                                        apellido = compraSnapshot.child("apellido")
                                            .getValue(String::class.java),
                                        dni = compraSnapshot.child("dni")
                                            .getValue(String::class.java),
                                        nombre = compraSnapshot.child("nombre")
                                            .getValue(String::class.java),
                                        numero = compraSnapshot.child("numero")
                                            .getValue(String::class.java),

                                        // Datos de la tienda
                                        idTienda = compraSnapshot.child("idTienda")
                                            .getValue(String::class.java),
                                        nombreServicio = compraSnapshot.child("nombre_servicio")
                                            .getValue(String::class.java),
                                        idServicio = compraSnapshot.child("idReservaPedido")
                                            .getValue(String::class.java),
                                        precioServicio = compraSnapshot.child("precioServicio")
                                            .getValue(String::class.java),
                                        metodoPago = compraSnapshot.child("metodoPago")
                                            .getValue(String::class.java),
                                        metodoEntrega = compraSnapshot.child("metodoEntrega")
                                            .getValue(String::class.java),
                                        precioART = compraSnapshot.child("precioServicio")
                                            .getValue(String::class.java), // Usando el mismo valor de precioServicio
                                        precioDriver = compraSnapshot.child("totalDriver")
                                            .getValue(String::class.java),

                                        // Datos de la reserva
                                        codeReserva = compraSnapshot.child("idPedido")
                                            .getValue(String::class.java), // codeReserva es equivalente a idPedido
                                        fecha = compraSnapshot.child("fecha")
                                            .getValue(String::class.java),
                                        hora = compraSnapshot.child("hora")
                                            .getValue(String::class.java),
                                        estado = compraSnapshot.child("estado")
                                            .getValue(String::class.java),
                                        tipoReserva = compraSnapshot.child("tipoReserva")
                                            .getValue(String::class.java),
                                        fechaLlegada = compraSnapshot.child("fecha_llegada")
                                            .getValue(String::class.java),
                                        horaLlegada = compraSnapshot.child("hora_llegada")
                                            .getValue(String::class.java),

                                        // Otros datos
                                        totaPAgar = compraSnapshot.child("totalCancelar")
                                            .getValue(String::class.java),
                                        cantidad = compraSnapshot.child("Total_item_selecionado")
                                            .getValue(String::class.java),
                                    )

                                    if (shouldAddToservicios(filtrado, historialServicio)) {
                                        lista.add(historialServicio)
                                    }
                                }
                                inicializarServiciosHistorial(lista, recicle, context)


                            }

                            override fun onCancelled(databaseError: DatabaseError) {
                                Log.e(
                                    "TAG",
                                    "Error al obtener las compras de la tienda $idTienda: ${databaseError.message}"
                                )
                            }
                        })
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    Log.e(
                        "TAG",
                        "Error al obtener las tiendas del usuario: ${databaseError.message}"
                    )
                }
            })
        }
    }

    private fun shouldAddToservicios(
        filtro: String,
        historial: dataclassServiciosHistorial,
    ): Boolean {
        return when (filtro) {
            "todos" -> true
            "promo" -> historial.tipoReserva.equals("reserva promo", ignoreCase = true)
            "articulo" -> historial.tipoReserva.equals("reserva articulos", ignoreCase = true)
            "servicios" -> historial.tipoReserva.equals("Reserva Servicios", ignoreCase = true)
            else -> false
        }
    }

    private fun inicializarServiciosHistorial(
        lista: MutableList<dataclassServiciosHistorial>,
        recile: RecyclerView,
        context: Context,
    ) {
        val recicle = recile
        recicle.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        recicle.adapter =
            adapterReservaSevicios(
                lista
            )
    }


    fun generarCodigoPedido(): String {
        val codigoPedido = (100000000..999999999).random()
        return codigoPedido.toString()
    }

    private fun inicializarLista(
        lista: List<dataclassHistorial>,
        recicle: RecyclerView,
        context: Context
    ) {
        // Inicializamos el adaptador y lo asignamos a la variable global
        historialAdapter = adapterHistorial(lista) { dataclassHistorial ->
        }

        // Configuramos el RecyclerView con el adaptador
        recicle.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        recicle.adapter = historialAdapter
    }

    fun actualizarHistorial(newList: List<dataclassHistorial>) {
        historialAdapter.updateList(newList)
    }

    fun obtenerComprasReservas(
        filtro: String,
        listaHistorial: List<dataclassHistorial>,
        recile: RecyclerView,
        context: Context,
        nombreTienda: (List<String>) -> Unit,
        totalEncontrado: (String) -> Unit
    ) {
        val firebaseAuth = FirebaseAuth.getInstance()
        val userId = firebaseAuth.currentUser?.uid

        if (userId != null) {
            val database = FirebaseDatabase.getInstance()
            val usuarioRef = database.getReference("PedidosUsuario/$userId/tiendas")

            usuarioRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    var nuevaListaHistorial =
                        listaHistorial.toMutableList()  // Convertir a lista mutable
                    val nombresTiendas = mutableSetOf<String>()  // Usar un Set para nombres únicos

                    dataSnapshot.children.forEach { tiendaSnapshot ->
                        val idTienda = tiendaSnapshot.key ?: return@forEach
                        val comprasRef =
                            database.getReference("PedidosUsuario/$userId/tiendas/$idTienda/compra")

                        comprasRef.addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onDataChange(comprasSnapshot: DataSnapshot) {
                                comprasSnapshot.children.forEach { compraSnapshot ->
                                    val historial = dataclassHistorial(
                                        latitud = 1231, // Reemplaza con datos reales si están disponibles
                                        longitud = 123, // Reemplaza con datos reales si están disponibles
                                        direccion = compraSnapshot.child("direccion")
                                            .getValue(String::class.java),
                                        estadp = compraSnapshot.child("estado")
                                            .getValue(String::class.java),
                                        fecha = compraSnapshot.child("fecha")
                                            .getValue(String::class.java),
                                        hora = compraSnapshot.child("hora")
                                            .getValue(String::class.java),
                                        idPedido = compraSnapshot.child("idPedido")
                                            .getValue(String::class.java),
                                        idTienda = compraSnapshot.child("idTienda")
                                            .getValue(String::class.java),
                                        iduser = compraSnapshot.child("idUSer")
                                            .getValue(String::class.java),
                                        metodoentrega = compraSnapshot.child("metodoEntrega")
                                            .getValue(String::class.java),
                                        metodoPago = compraSnapshot.child("metodoPago")
                                            .getValue(String::class.java),
                                        nombre = compraSnapshot.child("nombre")
                                            .getValue(String::class.java),
                                        productos = compraSnapshot.child("productos")
                                            .getValue(String::class.java),
                                        numero = compraSnapshot.child("numero")
                                            .getValue(String::class.java),
                                        nombreTienda = compraSnapshot.child("nombreTienda")
                                            .getValue(String::class.java),
                                        localidadTienda = compraSnapshot.child("localidadTienda")
                                            .getValue(String::class.java),
                                        localidadUSer = compraSnapshot.child("localidad")
                                            .getValue(String::class.java),
                                        descipcion = compraSnapshot.child("descipcion")
                                            .getValue(String::class.java),
                                        tipoTienda = compraSnapshot.child("tipoTienda")
                                            .getValue(String::class.java),
                                        estadoPedido = compraSnapshot.child("estadoPedido")
                                            .getValue(String::class.java),
                                        idDriver = compraSnapshot.child("idDriver")
                                            .getValue(String::class.java),
                                        total = compraSnapshot.child("totalCancelar")
                                            .getValue(String::class.java),
                                        precioDelivery = compraSnapshot.child("precioDelivery")
                                            .getValue(String::class.java),
                                        referencia = compraSnapshot.child("referencia")
                                            .getValue(String::class.java),
                                        totalDriver = compraSnapshot.child("totalDriver")
                                            .getValue(String::class.java),
                                        totalProductos = compraSnapshot.child("totalProductos")
                                            .getValue(String::class.java),
                                        tipoRealizado = compraSnapshot.child("tipoRealizado")
                                            .getValue(String::class.java),
                                        totalItems = compraSnapshot.child("Total_item_selecionado")
                                            .getValue(String::class.java),
                                    )

                                    // Filtrar y agregar a lista y set
                                    if (shouldAddToHistorial(filtro, historial)) {
                                        nuevaListaHistorial.add(historial)
                                        when (filtro) {
                                            "todos" -> {
                                                historial.nombreTienda?.let { nombresTiendas.add(it) }
                                            }

                                            "carrito" -> {
                                                if (historial.tipoRealizado == "compra carrito") {
                                                    historial.nombreTienda?.let {
                                                        nombresTiendas.add(
                                                            it
                                                        )
                                                    }
                                                }
                                            }

                                            "promociones" -> {
                                                if (historial.tipoRealizado == "compra de promociones") {
                                                    historial.nombreTienda?.let {
                                                        nombresTiendas.add(
                                                            it
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }

                                // Actualizar la lista en el RecyclerView
                                inicializarLista(nuevaListaHistorial, recile, context)
                                totalEncontrado(nuevaListaHistorial.size.toString())
                                nombreTienda(nombresTiendas.toList())
                            }

                            override fun onCancelled(databaseError: DatabaseError) {
                                Log.e(
                                    "TAG",
                                    "Error al obtener las compras de la tienda $idTienda: ${databaseError.message}"
                                )
                                totalEncontrado("0")
                            }
                        })
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    Log.e(
                        "TAG",
                        "Error al obtener las tiendas del usuario: ${databaseError.message}"
                    )
                    totalEncontrado("0")
                }
            })
        } else {
            Log.e("TAG", "El usuario actual no está autenticado.")
            totalEncontrado("0")
        }
    }


    fun obtenerComprasReservasFiltrado(
        filtro: String,
        tienda: String,
        precio: String,
        fecha: String,
        estado: String,
        listaHistorial: List<dataclassHistorial>,  // List (inmutable)
        recile: RecyclerView,
        context: Context,
        encontrado: (String) -> Unit
    ) {
        val firebaseAuth = FirebaseAuth.getInstance()
        val userId = firebaseAuth.currentUser?.uid

        if (userId != null) {
            val database = FirebaseDatabase.getInstance()
            val usuarioRef = database.getReference("PedidosUsuario/$userId/tiendas")

            usuarioRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    // Lista temporal para almacenar los nuevos elementos
                    var nuevaListaHistorial = listOf<dataclassHistorial>()  // Lista vacía inmutable

                    dataSnapshot.children.forEach { tiendaSnapshot ->
                        val comprasSnapshot = tiendaSnapshot.child("compra")

                        comprasSnapshot.children.forEach { compraSnapshot ->
                            val tipoRealizado =
                                compraSnapshot.child("tipoRealizado").getValue(String::class.java)
                            val esValido = when (filtro) {
                                "todos" -> true
                                "carrito" -> tipoRealizado == "compra carrito"
                                "promociones" -> tipoRealizado == "compra de promociones"
                                else -> false
                            }

                            if (esValido) {
                                val historial = dataclassHistorial(
                                    latitud = 1231,
                                    longitud = 123,
                                    direccion = compraSnapshot.child("direccion")
                                        .getValue(String::class.java),
                                    estadp = compraSnapshot.child("estado")
                                        .getValue(String::class.java),
                                    fecha = compraSnapshot.child("fecha")
                                        .getValue(String::class.java),
                                    hora = compraSnapshot.child("hora")
                                        .getValue(String::class.java),
                                    idPedido = compraSnapshot.child("idPedido")
                                        .getValue(String::class.java),
                                    idTienda = compraSnapshot.child("idTienda")
                                        .getValue(String::class.java),
                                    iduser = compraSnapshot.child("idUSer")
                                        .getValue(String::class.java),
                                    metodoentrega = compraSnapshot.child("metodoEntrega")
                                        .getValue(String::class.java),
                                    metodoPago = compraSnapshot.child("metodoPago")
                                        .getValue(String::class.java),
                                    nombre = compraSnapshot.child("nombre")
                                        .getValue(String::class.java),
                                    productos = compraSnapshot.child("productos")
                                        .getValue(String::class.java),
                                    numero = compraSnapshot.child("numero")
                                        .getValue(String::class.java),
                                    nombreTienda = compraSnapshot.child("nombreTienda")
                                        .getValue(String::class.java),
                                    localidadTienda = compraSnapshot.child("localidadTienda")
                                        .getValue(String::class.java),
                                    localidadUSer = compraSnapshot.child("localidad")
                                        .getValue(String::class.java),
                                    descipcion = compraSnapshot.child("descipcion")
                                        .getValue(String::class.java),
                                    tipoTienda = compraSnapshot.child("tipoTienda")
                                        .getValue(String::class.java),
                                    estadoPedido = compraSnapshot.child("estado")
                                        .getValue(String::class.java),
                                    idDriver = compraSnapshot.child("idDriver")
                                        .getValue(String::class.java),
                                    total = compraSnapshot.child("totalCancelar")
                                        .getValue(String::class.java),
                                    precioDelivery = compraSnapshot.child("precioDelivery")
                                        .getValue(String::class.java),
                                    referencia = compraSnapshot.child("referencia")
                                        .getValue(String::class.java),
                                    totalDriver = compraSnapshot.child("totalDriver")
                                        .getValue(String::class.java),
                                    totalProductos = compraSnapshot.child("totalProductos")
                                        .getValue(String::class.java),
                                    tipoRealizado = tipoRealizado,
                                    totalItems = compraSnapshot.child("Total_item_selecionado")
                                        .getValue(String::class.java),
                                )

                                // Agregar el historial a la nueva lista usando `plus`
                                nuevaListaHistorial = nuevaListaHistorial.plus(historial)
                            }
                        }
                    }

                    // Filtrar la lista
                    val listaFiltrada = nuevaListaHistorial.filter { historial ->
                        pasaFiltroHistorial(historial, tienda, precio, fecha, estado)
                    }

                    // Inicializar el RecyclerView con la lista filtrada
                    inicializarLista(listaFiltrada, recile, context)

                    Log.d(
                        "FiltradoHistorial",
                        "Total de elementos filtrados: ${listaFiltrada.size}"
                    )
                    encontrado(listaFiltrada.size.toString())
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    Log.e(
                        "TAG",
                        "Error al obtener las tiendas del usuario: ${databaseError.message}"
                    )
                    encontrado("0")
                }
            })
        } else {
            Log.e("TAG", "El usuario actual no está autenticado.")
        }
    }


    fun pasaFiltroHistorial(
        historial: dataclassHistorial,
        tienda: String,
        precio: String,
        fecha: String,
        estado: String,
    ): Boolean {
        val precioTotal = historial.total?.toDoubleOrNull() ?: 0.0

        val filtroTienda = if (tienda.isNotEmpty()) {
            historial.nombreTienda?.contains(tienda, ignoreCase = true) == true
        } else {
            true
        }

        val filtroEstado = if (estado.isNotEmpty()) {
            historial.estadoPedido?.contains(estado, ignoreCase = true) == true
        } else {
            true
        }

        val filtradoFecha = if (fecha.isNotEmpty()) {
            historial.fecha?.contains(fecha, ignoreCase = true) == true
        } else {
            true
        }

        val filtroPrecio = if (precio.isNotEmpty()) {
            val precioMaximo = precio.toDoubleOrNull() ?: Double.MAX_VALUE
            precioTotal <= precioMaximo
        } else {
            true
        }

        return filtroTienda && filtroEstado && filtradoFecha && filtroPrecio
    }


    private fun shouldAddToHistorial(filtro: String, historial: dataclassHistorial): Boolean {
        return when (filtro) {
            "todos" -> true
            "carrito" -> historial.tipoRealizado.equals("compra carrito", ignoreCase = true)
            "promociones" -> historial.tipoRealizado.equals(
                "compra de promociones",
                ignoreCase = true
            )

            else -> false
        }
    }


    private fun inicalizarListareservas(
        lista: MutableList<dataclassreservas>,
        recile: RecyclerView,
        context: Context,
    ) {
        val recicle = recile
        recicle.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        recicle.adapter =
            adapterReservas(
                lista,
                {})
    }


    fun obtenerReservas(
        listareserva: MutableList<dataclassreservas>,
        recile: RecyclerView,
        context: Context,
    ) {
        val firebaseAuth = FirebaseAuth.getInstance()
        val userId = firebaseAuth.currentUser?.uid

        if (userId != null) {
            val database = FirebaseDatabase.getInstance()
            val usuarioRef = database.getReference("PedidosUsuario/$userId/tiendas")

            usuarioRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    listareserva.clear()
                    for (tiendaSnapshot in dataSnapshot.children) {
                        val idTienda = tiendaSnapshot.key

                        // Obtener compras de la tienda actual
                        val comprasRef =
                            database.getReference("PedidosUsuario/$userId/tiendas/$idTienda/reserva")
                        comprasRef.addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onDataChange(comprasSnapshot: DataSnapshot) {
                                for (compraSnapshot in comprasSnapshot.children) {
                                    val apellido =
                                        compraSnapshot.child("apellido")
                                            .getValue(String::class.java)
                                    val cantidad =
                                        compraSnapshot.child("cantidad")
                                            .getValue(String::class.java)
                                    val codigoPedido =
                                        compraSnapshot.child("codigoPedido")
                                            .getValue(String::class.java)
                                    val dni =
                                        compraSnapshot.child("dni").getValue(String::class.java)
                                    val fecha =
                                        compraSnapshot.child("fecha").getValue(String::class.java)
                                    val fechamaximaEntrega =
                                        compraSnapshot.child("fechamaximaEntrega")
                                            .getValue(String::class.java)
                                    val hora =
                                        compraSnapshot.child("hora").getValue(String::class.java)
                                    val horaMaximaEntrega =
                                        compraSnapshot.child("horaMaximaEntrega")
                                            .getValue(String::class.java)
                                    val idProducto =
                                        compraSnapshot.child("idProducto")
                                            .getValue(String::class.java)
                                    val metodoEntrega =
                                        compraSnapshot.child("metodoEntrega")
                                            .getValue(String::class.java)
                                    val nombre =
                                        compraSnapshot.child("nombre").getValue(String::class.java)
                                    val precio =
                                        compraSnapshot.child("precio").getValue(String::class.java)
                                    val estado =
                                        compraSnapshot.child("estado").getValue(String::class.java)
                                    val tituloProducto =
                                        compraSnapshot.child("tituloProducto")
                                            .getValue(String::class.java)
                                    val totalApagar =
                                        compraSnapshot.child("totalApagar")
                                            .getValue(String::class.java)
                                    val idTienda = compraSnapshot.child("idTienda")
                                        .getValue(String::class.java)
                                    val numerous =
                                        compraSnapshot.child("numero").getValue(String::class.java)
                                    val nombreTienda = compraSnapshot.child("nombreTienda")
                                        .getValue(String::class.java)
                                    val localidaus = compraSnapshot.child("localidaduser")
                                        .getValue(String::class.java)
                                    val localidaTienda = compraSnapshot.child("localidaTienda")
                                        .getValue(String::class.java)
                                    val descripcionEntrega =
                                        compraSnapshot.child("descripcionEntrega")
                                            .getValue(String::class.java)
                                    val direccionEntrega = compraSnapshot.child("direccionEntrega")
                                        .getValue(String::class.java)
                                    val tipoTienda = compraSnapshot.child("tipoTienda")
                                        .getValue(String::class.java)
                                    val reserva = compraSnapshot.child("reserva")
                                        .getValue(String::class.java)
                                    val tipoReserva = compraSnapshot.child("tipoReserva")
                                        .getValue(String::class.java)


                                    val datos = dataclassreservas(
                                        numerous,
                                        estado,
                                        nombreTienda,
                                        apellido,
                                        cantidad,
                                        codigoPedido,
                                        dni,
                                        fecha,
                                        fechamaximaEntrega,
                                        hora,
                                        horaMaximaEntrega,
                                        idProducto,
                                        metodoEntrega,
                                        nombre,
                                        precio,
                                        tituloProducto,
                                        totalApagar,
                                        reserva,
                                        idTienda,
                                        localidaus,
                                        localidaTienda,
                                        descripcionEntrega,
                                        direccionEntrega,
                                        tipoTienda, tipoReserva
                                    )
                                    listareserva.add(datos)
                                }
                                inicalizarListareservas(listareserva, recile, context)
                            }

                            override fun onCancelled(databaseError: DatabaseError) {
                                Log.e(
                                    "TAG",
                                    "Error al obtener las compras de la tienda $idTienda: ${databaseError.message}"
                                )
                            }
                        })
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    Log.e(
                        "TAG",
                        "Error al obtener las tiendas del usuario: ${databaseError.message}"
                    )
                }
            })
        } else {
            Log.e("TAG", "El usuario actual no está autenticado.")
        }
    }


    private fun generarQR(dataclassHistorial: dataclassHistorial, context: Context) {
        val vista = Intent(context, generarQRPedidoArticulos::class.java).apply {
            putExtra("codigoOrden", dataclassHistorial.idPedido)
            putExtra("nombreTiena", dataclassHistorial.nombreTienda)
            putExtra("user", dataclassHistorial.nombre)
            putExtra("numerouser", dataclassHistorial.numero)
            putExtra("hora", dataclassHistorial.hora)
            putExtra("fecha", dataclassHistorial.fecha)
            putExtra("estado", dataclassHistorial.estadp)
            putExtra("metodoentrega", dataclassHistorial.metodoentrega)
            putExtra("pagometodo", dataclassHistorial.metodoPago)
            putExtra("total", dataclassHistorial.total)
            putExtra("iduser", dataclassHistorial.iduser)
            putExtra("idTienda", dataclassHistorial.idTienda)
            putExtra("direccion", dataclassHistorial.direccion)
            putExtra("localidadUSER", dataclassHistorial.localidadUSer)
            putExtra("localidadTienda", dataclassHistorial.localidadTienda)
            putExtra("descripcion", dataclassHistorial.descipcion)

        }
        context.startActivity(vista)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun obtnerfechaHora(hora: TextView, fecha: TextView) {
        val currentTime = LocalTime.now()
        val formatter = DateTimeFormatter.ofPattern("hh:mm:ss a")
        val formattedTime = currentTime.format(formatter)
        val currentDate = LocalDate.now()
        val formatterfecha = DateTimeFormatter.ofPattern("dd/MM/yyyy")
        val formattedDate = currentDate.format(formatterfecha)

        fecha.text = formattedDate
        hora.text = formattedTime
    }


    fun obtnerFotoPErfilTienda(
        context: Context,
        idTienda: String,
        imgPerfilTienda: CircleImageView,
    ) {
        val db = FirebaseFirestore.getInstance().collection("Tiendas").document(idTienda)
        db.get().addOnSuccessListener { res ->
            if (res.exists()) {
                val data = res.data
                val imPerfil = data?.get("imgPerfil") as? String
                try {
                    Glide.with(context)
                        .load(imPerfil)
                        .into(imgPerfilTienda)
                } catch (e: Exception) {
                    println("error al setear la img")
                }
            }
        }
    }

    fun obtenerEstadoPedidoReservaCompra(
        estado: TextView,
        context: Context,
        tienda: RelativeLayout,
        delivery: RelativeLayout,
        camindo: RelativeLayout,
        entregado: RelativeLayout,
        dataclassHistorial: dataclassHistorial,
        nombreDriver: TextView,
        generoDriver: TextView,
        nacionalidadDriver: TextView,
        imgDriver: CircleImageView,
        buttonWhatsapp: ImageView,
        llamar: ImageView, linealDriver: LinearLayout,
    ) {
        when (estado.text.toString()) {
            "Aceptado" -> {
                estado.setTextColor(ContextCompat.getColor(context, R.color.verde))
                tienda.setBackgroundResource(R.drawable.round_icono_pedidos_estado)
                linealDriver.isVisible = true
                obtenerDatosDriver(
                    dataclassHistorial,
                    nombreDriver,
                    generoDriver,
                    nacionalidadDriver,
                    imgDriver,
                    buttonWhatsapp,
                    llamar, context
                )
            }

            "Rechazado" -> {
                estado.setTextColor(ContextCompat.getColor(context, R.color.rojo))
            }

            "Driver" -> {
                estado.setTextColor(
                    ContextCompat.getColor(
                        context,
                        R.color.amarillo
                    )
                )
                delivery.setBackgroundResource(R.drawable.round_icono_pedidos_estado)
                linealDriver.isVisible = true
                obtenerDatosDriver(
                    dataclassHistorial,
                    nombreDriver,
                    generoDriver,
                    nacionalidadDriver,
                    imgDriver,
                    buttonWhatsapp,
                    llamar, context
                )
            }

            "Camino" -> {
                estado.setTextColor(
                    ContextCompat.getColor(
                        context,
                        R.color.violeta
                    )
                )
                camindo.setBackgroundResource(R.drawable.round_icono_pedidos_estado)
                linealDriver.isVisible = true
                obtenerDatosDriver(
                    dataclassHistorial,
                    nombreDriver,
                    generoDriver,
                    nacionalidadDriver,
                    imgDriver,
                    buttonWhatsapp,
                    llamar, context
                )
            }

            "Entregado" -> {
                estado.setTextColor(
                    ContextCompat.getColor(
                        context,
                        R.color.selected_background
                    )
                )
                entregado.setBackgroundResource(R.drawable.round_icono_pedidos_estado)
                linealDriver.isVisible = true
                obtenerDatosDriver(
                    dataclassHistorial,
                    nombreDriver,
                    generoDriver,
                    nacionalidadDriver,
                    imgDriver,
                    buttonWhatsapp,
                    llamar, context
                )
            }
        }
    }

    private fun obtenerDatosDriver(
        dataclassHistorial: dataclassHistorial,
        nombreDriver: TextView,
        generoDriver: TextView,
        nacionalidadDriver: TextView,
        imgDriver: CircleImageView,
        buttonWhatsapp: ImageView,
        llamar: ImageView,
        context: Context,
    ) {
        val firestore = FirebaseFirestore.getInstance().collection("driver")
            .document(dataclassHistorial.idDriver.toString())
        firestore.get().addOnSuccessListener { res ->
            if (res.exists()) {
                // Obtener los datos del documento
                val data = res.data
                val nombre = data?.get("nombre") as? String
                val imgPerfilDriver = data?.get("imagen_perfil") as? String
                val numeroDriver = data?.get("telefono") as? String
                val disponible = data?.get("disponible") as? Boolean
                val genero = data?.get("genero") as? String
                val nacionalidad = data?.get("nacionalidad") as? String

                // Setear los valores en los TextViews y la imagen
                nombreDriver.text = nombre ?: "N/A"
                nacionalidadDriver.text = nacionalidad ?: "N/A"
                generoDriver.text = genero ?: "N/A"

                try {
                    Glide.with(context)
                        .load(imgPerfilDriver)
                        .into(imgDriver)
                } catch (e: Exception) {
                    println("Error al setear la imagen: ${e.message}")
                }

                // Configurar el botón de WhatsApp
                buttonWhatsapp.setOnClickListener {
                    constantes.contactarWhatsapp(
                        numeroDriver.toString(),
                        "Hola, estoy esperando mi pedido ${dataclassHistorial.idPedido}",
                        context
                    )
                }

                // Configurar el botón de llamada
                llamar.setOnClickListener {
                    constantes.llamarCliente(
                        context,
                        numeroDriver.toString(),
                        REQUEST_CALL_PHONE = 1
                    )
                }

            } else {
                println("El documento del driver no existe.")
            }
        }.addOnFailureListener { exception ->
            println("Error al obtener los datos del driver: ${exception.message}")
        }
    }

    fun obtenerIMGproducto(
        idTienda: String,
        idporducto: String,
        imgArticulo: ShapeableImageView,
        context: Context,
    ) {
        val realtime = FirebaseDatabase.getInstance().getReference("Articulos").child(idTienda)
            .child(idporducto)
        realtime.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val imgPerfil = snapshot.child("imgArticulo").getValue(String::class.java)
                    try {
                        Glide.with(context)
                            .load(imgPerfil)
                            .into(imgArticulo)
                    } catch (E: Exception) {
                        println("no se pudo setear la imagen del articulo")
                    }
                } else {
                    println("no se econtro imagen en el articulo")
                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })
    }

    fun subirScroll(scrollView: ScrollView, fab: ImageButton) {
        scrollView.viewTreeObserver.addOnScrollChangedListener {
            val view = scrollView.getChildAt(scrollView.childCount - 1) as View
            val diff = view.bottom - (scrollView.height + scrollView.scrollY)

            if (diff < 100) {
                fab.visibility = View.VISIBLE
            } else {
                fab.visibility = View.GONE
            }
        }

        fab.setOnClickListener {
            scrollView.smoothScrollTo(0, 0)
        }
    }

    fun setearDatosUsuario(callback: (String?, String?, String?, String?) -> Unit) {
        val idPAsado = FirebaseAuth.getInstance().uid.toString()
        val firestore = FirebaseFirestore.getInstance()
        val usuariosRef = firestore.collection("Trabajadores_Usuarios_Drivers")
            .document("trabajadores").collection("trabajadores")
        val usuariosCuentaNormalRef = firestore.collection("Trabajadores_Usuarios_Drivers")
            .document("usuarios").collection("usuarios")

        usuariosRef.whereEqualTo("id", idPAsado).get()
            .addOnSuccessListener { trabajadoresResult ->
                if (!trabajadoresResult.isEmpty) {

                    val datos = trabajadoresResult.documents.first()
                    val nombre = datos.getString("nombre")
                    val numero = datos.getString("numero")
                    val localidad = datos.getString("localidad")
                    val apellido = datos.getString("apellido")
                    callback(nombre, numero, localidad, apellido)
                } else {
                    // Si no se encuentra en "trabajadores", buscar en "usuarios"
                    usuariosCuentaNormalRef.whereEqualTo("id", idPAsado).get()
                        .addOnSuccessListener { usuariosResult ->
                            if (!usuariosResult.isEmpty) {
                                // Si se encuentra en "usuarios", llama al callback con los datos
                                val datos = usuariosResult.documents.first()
                                val nombre = datos.getString("nombre")
                                val numero = datos.getString("numero")
                                val localidad = datos.getString("localidad")
                                val apellido = datos.getString("apellido")
                                callback(nombre, numero, localidad, apellido)
                            } else {
                                // Si no se encuentra en ninguna de las colecciones
                                callback(null, null, null, null)
                            }
                        }
                        .addOnFailureListener { exception ->
                            Log.e(
                                "FirestoreError",
                                "Error al obtener documentos de usuarios",
                                exception
                            )
                            callback(null, null, null, null)
                        }
                }
            }
            .addOnFailureListener { exception ->
                Log.e(
                    "FirestoreError",
                    "Error al obtener documentos de trabajadores",
                    exception
                )
                callback(null, null, null, null)
            }
    }

    fun setearDatosUsuarioImgNombre(idUSer: String, callback: (String?, String?, String?) -> Unit) {

        val firestore = FirebaseFirestore.getInstance()
        val usuariosRef = firestore.collection("Trabajadores_Usuarios_Drivers")
            .document("trabajadores").collection("trabajadores")
        val usuariosCuentaNormalRef = firestore.collection("Trabajadores_Usuarios_Drivers")
            .document("usuarios").collection("usuarios")

        usuariosRef.whereEqualTo("id", idUSer).get()
            .addOnSuccessListener { trabajadoresResult ->
                if (!trabajadoresResult.isEmpty) {

                    val datos = trabajadoresResult.documents.first()
                    val nombre = datos.getString("nombre")
                    val imagenPerfil = datos.getString("imagenPerfil")
                    val apellido = datos.getString("apellido")
                    callback(nombre, imagenPerfil, apellido)
                } else {
                    // Si no se encuentra en "trabajadores", buscar en "usuarios"
                    usuariosCuentaNormalRef.whereEqualTo("id", idUSer).get()
                        .addOnSuccessListener { usuariosResult ->
                            if (!usuariosResult.isEmpty) {
                                // Si se encuentra en "usuarios", llama al callback con los datos
                                val datos = usuariosResult.documents.first()
                                val nombre = datos.getString("nombre")
                                val imagenPerfil = datos.getString("imagenPerfil")
                                val apellido = datos.getString("apellido")
                                callback(nombre, imagenPerfil, apellido)
                            } else {
                                // Si no se encuentra en ninguna de las colecciones
                                callback(null, null, null)
                            }
                        }
                        .addOnFailureListener { exception ->
                            Log.e(
                                "FirestoreError",
                                "Error al obtener documentos de usuarios",
                                exception
                            )
                            callback(null, null, null)
                        }
                }
            }
            .addOnFailureListener { exception ->
                Log.e(
                    "FirestoreError",
                    "Error al obtener documentos de trabajadores",
                    exception
                )
                callback(null, null, null)
            }
    }


    fun retornarIDTienda(callback: (List<String>) -> Unit) {
        firebaseAuth = FirebaseAuth.getInstance()
        val userId = firebaseAuth.currentUser?.uid
        val realtime = FirebaseDatabase.getInstance().getReference("carritoGeneral")
            .child(userId.toString())
        realtime.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val idTiendas = mutableListOf<String>()
                for (tiendaSnapshot in snapshot.children) {
                    val idTienda = tiendaSnapshot.key
                    if (idTienda != null) {
                        idTiendas.add(idTienda)
                        println("id Tienda id $idTiendas")
                    }
                }
                callback(idTiendas) // Llama al callback con la lista de ids de tiendas
            }

            override fun onCancelled(error: DatabaseError) {
                // Manejar error aquí
                callback(emptyList()) // En caso de error, retorna una lista vacía
            }
        })
    }

    fun retornarnumeroTienda(idTienda: String, callback: (String?) -> Unit) {
        val db = FirebaseFirestore.getInstance().collection("Tiendas").document(idTienda)
        db.get().addOnSuccessListener { res ->
            if (res.exists()) {
                val data = res.data
                val numero = data?.get("numero") as String
                callback(numero)
            } else {
                callback(null)
            }
        }.addOnFailureListener {
            callback(null)
        }
    }


}