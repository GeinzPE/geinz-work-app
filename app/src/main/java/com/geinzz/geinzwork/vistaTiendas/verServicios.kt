package com.geinzz.geinzwork.vistaTiendas

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
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
import com.geinzz.geinzwork.NotificacionRS
import com.geinzz.geinzwork.utils.constantes.constantes.Variables
import com.geinzz.geinzwork.R
import com.geinzz.geinzwork.ui.adapters.adapter_item_mas_promo_servicios_art
import com.geinzz.geinzwork.utils.constantes.constantes.constantes
import com.geinzz.geinzwork.utils.constantes.constantes.constantesCarrito
import com.geinzz.geinzwork.utils.constantes.constantes.constantesImagenes
import com.geinzz.geinzwork.utils.constantes.constantes.constantesPublicidad
import com.geinzz.geinzwork.utils.constantes.constantes.constantesTrabajadoresTiendasInicioFragmet
import com.geinzz.geinzwork.utils.constantes.constantes.filtradoLocalidadElementos
import com.geinzz.geinzwork.utils.constantes.constantes.mostrarFechaDialog_horaDialog
import com.geinzz.geinzwork.databinding.ActivityVerServiciosBinding
import com.geinzz.geinzwork.databinding.BottomSheetReservasBinding
import com.geinzz.geinzwork.model.dataclass_mas_art_promo_servicio
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDragHandleView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.imaginativeworld.whynotimagecarousel.model.CarouselItem

class verServicios : AppCompatActivity() {
    private lateinit var binding: ActivityVerServiciosBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var dialog: BottomSheetDialog
    private lateinit var bottomSheet: BottomSheetDragHandleView
    private lateinit var bindingBottomShet: BottomSheetReservasBinding
    private val lista = mutableListOf<CarouselItem>()
    private val listaServicios = mutableListOf<dataclass_mas_art_promo_servicio>()

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityVerServiciosBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        firebaseAuth = FirebaseAuth.getInstance()

        var id = intent.getStringExtra("idServicio").toString()
        var idTienda = intent.getStringExtra("idTienda").toString()
        binding.bakPresert.setOnClickListener {
            onBackPressed()
        }
        obtenerDatosServicios(id, idTienda)
        constantesVistaTiendas.setearDatosTienda(
            idTienda,
            this,
            binding.layoutPerfilTienda.imgPerfilUser,
            binding.layoutPerfilTienda.nombreTienda,
            binding.layoutPerfilTienda.sloganTienda,
            binding.layoutPerfilTienda.horarioAtencion
        )
        binding.layoutPerfilTienda.InfoTienda.setOnClickListener {
            val vista = Intent(this, VistaTienda::class.java)
            vista.putExtra("idTienda", idTienda)
            startActivity(vista)
            finish()
        }
        obtenerMasServicios(idTienda)

    }

    private fun masInfoServicios(idTienda: String, nombreServicio: String) {
        constantesTrabajadoresTiendasInicioFragmet.obtenerNumeroTiendas(idTienda) { numero ->
            val nombreServicioBold =
                "*$nombreServicio*"
            constantes.contactarWhatsapp(
                "+51 $numero",
                "Hola, deseo más información sobre este servicio que brindan: $nombreServicioBold",
                this
            )
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun reservar(
        nombreServicio: String,
        monto: String,
        idServicio: String,
        idTienda: String,
    ) {
        bindingBottomShet = BottomSheetReservasBinding.inflate(LayoutInflater.from(this))
        val view = bindingBottomShet.root
        bottomSheet = bindingBottomShet.cerrar
        val nombre = bindingBottomShet.nombreUser
        val apellidop = bindingBottomShet.apellidouser
        val dni = bindingBottomShet.dniUser
        val yapeBTN = bindingBottomShet.Yape
        val efectivoBTN = bindingBottomShet.Efectivo
        val yapeRadio = bindingBottomShet.Yape
        val plin_radio = bindingBottomShet.plin
        val number = bindingBottomShet.numeroContacto
        val btnApply = bindingBottomShet.btnApply
        val metodoPago = bindingBottomShet.metodoPago
        val localidadUser = bindingBottomShet.localida

        bindingBottomShet.fechareserva.setOnClickListener {
            mostrarFechaDialog_horaDialog.mostrarFechaDialogVista(
                bindingBottomShet.fechareserva,
                this
            )
        }
        bindingBottomShet.horareserva.setOnClickListener {
            mostrarFechaDialog_horaDialog.showTimePicker(this, bindingBottomShet.horareserva)
        }


        val codigoPedido = constantesCarrito.generarCodigoPedido()
        constantesCarrito.obtnerfechaHora(
            bindingBottomShet.horaActual,
            bindingBottomShet.FechaActual
        )
        constantesCarrito.setearDatosUsuario { nombreus, numerous, localidad, apellido ->
            if (nombreus != null && numerous != null) {
                nombre.setText(nombreus)
                number.setText(numerous)
                apellidop.setText(apellido)
                localidadUser.setText(localidad)
                filtradoLocalidadElementos.listaFiltrado(bindingBottomShet.localida)
            } else {
                println("no se pudo encontrar su nombre")
            }
            filtradoLocalidadElementos.listaFiltrado(bindingBottomShet.localida)
        }
        bindingBottomShet.metodoPago.setOnCheckedChangeListener { group, checkedId ->
            when (checkedId) {
                R.id.Efectivo -> {
                    bindingBottomShet.layoutYape.isVisible = false
                    bindingBottomShet.pagoEfectivo.isVisible = true
                    bindingBottomShet.imagenYape.isVisible = false
                }

                R.id.Yape -> {
                    bindingBottomShet.imagenYape.isVisible = true
                    bindingBottomShet.layoutYape.isVisible = true
                    bindingBottomShet.pagoEfectivo.isVisible = false
                    bindingBottomShet.imagenPlin.isVisible = false
                }

                R.id.plin -> {
                    bindingBottomShet.imagenYape.isVisible = false
                    bindingBottomShet.layoutYape.isVisible = true
                    bindingBottomShet.imagenPlin.isVisible = true
                    bindingBottomShet.pagoEfectivo.isVisible = false
                }
            }

        }
        val yapeRuta = "Tiendas/$idTienda/QR_pagos/Yape.jpg"
        val plinrRuta = "Tiendas/$idTienda/QR_pagos/Plin.jpg"
        constantesImagenes.obtenerURLDescarga(this, bindingBottomShet.imagenYape, yapeRuta)
        constantesImagenes.obtenerURLDescarga(this, bindingBottomShet.imagenPlin, plinrRuta)

        val db = FirebaseFirestore.getInstance().collection("Tiendas").document(idTienda)
            .collection("servicios").document(idServicio)
        db.get().addOnSuccessListener { res ->
            if (res.exists()) {
                val data = res.data
                val yape = data?.get("yape") as? Boolean ?: false
                val plin = data?.get("plin") as? Boolean ?: false
                val efectivo = data?.get("efectivo") as? Boolean ?: false
                val nombre = data?.get("nombre") as? String ?: ""
                val precio = data?.get("precio") as? String ?: ""
                bindingBottomShet.nombreSevicio.text = nombre
                bindingBottomShet.montoTotal.text = precio
                if (yape == true && efectivo == true && plin) {
                    yapeBTN.isVisible = true
                    efectivoBTN.isVisible = true
                    yapeRadio.isVisible = true
                    plin_radio.isVisible = true
                } else if (yape == true && efectivo == true) {
                    yapeBTN.isVisible = true
                    efectivoBTN.isVisible = true
                    yapeRadio.isVisible = true
                    plin_radio.isVisible = false
                } else if (yape == true && plin) {
                    yapeBTN.isVisible = true
                    efectivoBTN.isVisible = false
                    yapeRadio.isVisible = true
                    plin_radio.isVisible = true
                } else if (yape == true) {
                    yapeBTN.isVisible = true
                    efectivoBTN.isVisible = false
                    yapeRadio.isVisible = true
                    plin_radio.isVisible = false
                } else if (efectivo == true) {
                    yapeBTN.isVisible = false
                    efectivoBTN.isVisible = true
                    yapeRadio.isVisible = false
                    plin_radio.isVisible = false
                } else if (plin) {
                    plin_radio.isVisible = true
                    yapeBTN.isVisible = false
                    efectivoBTN.isVisible = false
                    yapeRadio.isVisible = false
                } else {
                    bindingBottomShet.textoMetodoPago.isVisible = false
                    yapeBTN.isVisible = false
                    efectivoBTN.isVisible = false
                    yapeRadio.isVisible = false
                    plin_radio.isVisible = false
                }

            }
        }


        btnApply.setOnClickListener {
            if (nombre.text.isNullOrEmpty() && apellidop.text.isNullOrEmpty() && dni.text.isNullOrEmpty() && number.text.isNullOrEmpty()) {
                Toast.makeText(
                    this,
                    "Llene todo los campos para realizar la reserva",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                val metodoPagoSelect = when (metodoPago.checkedRadioButtonId) {
                    R.id.Efectivo -> "Efectivo"
                    R.id.Yape -> "Yape"
                    else -> ""
                }

                when (metodoPagoSelect) {
                    "Efectivo" -> {
                        Toast.makeText(
                            this,
                            "Acércate a la tienda para cancelar y confirmar tu reserva.",
                            Toast.LENGTH_LONG
                        ).show()
                    }

                    "Yape" -> {
                        val dialogBuilder = AlertDialog.Builder(this)
                        dialogBuilder
                            .setTitle("Modalidad Yape")
                            .setMessage(
                                "Para confirmar tu reserva, realiza el pago del 50% del total por Yape. La tienda confirmará tu reserva después del pago.\n-----------------------------------------------\n" +
                                        "Monto a cancelar por adelantado: PEN ${monto.toDouble() / 2}"
                            )
                            .setPositiveButton("Entendido") { dialogs, _ ->
                                dialogs.dismiss()
                                var mensaje = "----------------------------------\n" +
                                        "|           RESERVA DE SERVICIO           |\n" +
                                        "----------------------------------\n" +
                                        "| Nombre: ${nombre.text}  \n" +
                                        "| Apellido: ${apellidop.text}\n" +
                                        "| DNI: ${dni.text}           \n" +
                                        "| Número de reserva: ${codigoPedido} \n" +
                                        "| Número de usuario: ${number.text} \n" +
                                        "| Fecha de reserva: ${bindingBottomShet.FechaActual.text}    \n" +
                                        "| Metodo de pago: ${metodoPagoSelect}    \n" +
                                        "| Hora de reserva: ${bindingBottomShet.horaActual.text}      \n" +
                                        "| Tipo de servicio reservado: *$nombreServicio* \n" +
                                        "----------------------------------\n"
                                constantesTrabajadoresTiendasInicioFragmet.obtenerNumeroTiendas(
                                    idTienda
                                ) { numero ->
                                    constantes.contactarWhatsapp("+51 $numero", mensaje, this)
                                }
                                constantesCarrito.agregarReservaServicios(
                                    nombre.text.toString(),
                                    apellidop.text.toString(),
                                    dni.text.toString(),
                                    codigoPedido,
                                    bindingBottomShet.FechaActual.text.toString(),
                                    bindingBottomShet.horaActual.text.toString(),
                                    metodoPagoSelect,
                                    nombreServicio,
                                    idServicio,
                                    idTienda,
                                    number.text.toString(),
                                    monto,
                                    monto,
                                    bindingBottomShet.fechareserva.text.toString(),
                                    bindingBottomShet.horareserva.text.toString(), this
                                )

                                constantes.obtnerTokenTienda(idTienda) { token ->
                                    GlobalScope.launch {
                                        val enviar_notificaciones = NotificacionRS()
                                        enviar_notificaciones.sendNotification_con_parametros(
                                            "IDTienda",
                                            idTienda,
                                            "INICIO",
                                            this@verServicios,
                                            token,
                                            "Solicitud de Servicio $nombreServicio",
                                            "Un cliente ha solicitado el servicio $nombreServicio. Por favor, revisa y confirma la solicitud. "
                                        )
                                    }
                                }
                                Toast.makeText(this, "enviamos notificacione", Toast.LENGTH_SHORT)
                                    .show()
                                dialog.dismiss()
                            }
                            .show()
                    }

                    else -> {
                        Toast.makeText(
                            this,
                            "Selecciona un método de pago válido.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

            }
        }
        dialog.setContentView(view)

    }


    @RequiresApi(Build.VERSION_CODES.O)
    private fun obtenerDatosServicios(idServicio: String, idTienda: String) {
        val db = FirebaseFirestore.getInstance().collection(Variables.collection_Tiendas)
            .document(idTienda)
            .collection(Variables.collection_servicios_tiendas).document(idServicio)

        db.get().addOnSuccessListener { res ->
            if (res.exists()) {
                val data = res.data
                val titulo = data?.get("nombre") as? String ?: ""
                val precio = data?.get("precio") as? String ?: ""
                val descripcion = data?.get("descripcion") as? String ?: ""
                val UrlImg = data?.get("UrlImg") as? String ?: ""
                val reserva = data?.get("reserva") as? Boolean ?: false

                binding.titulo.text = titulo
                binding.precio.text = precio.toString()
                binding.contenido.text = descripcion
                lista.add(CarouselItem(UrlImg))
                binding.imgPrincipal.addData(lista)
                if (reserva) {
                    binding.reservar.isVisible = true
                } else {
                    binding.reservar.isVisible = false
                }
                binding.reservar.setOnClickListener {
                    if (firebaseAuth.currentUser == null) {
                        dialog = BottomSheetDialog(this)
                        constantesPublicidad.CreacionCuentaBottom_shett(
                            this,
                            dialog
                        )
                        dialog.show()
                        Toast.makeText(
                            this,
                            "Necesitas registrarte para reservar este servicio",
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        dialog = BottomSheetDialog(this)
                        reservar(titulo, precio, idServicio, idTienda)
                        dialog.show()
                    }
                }

                binding.masInformacion.setOnClickListener {
                    masInfoServicios(idTienda, titulo)
                }
            }


        }
    }

    private fun obtenerMasServicios(idTienda: String) {
        listaServicios.clear()
        val db = FirebaseFirestore.getInstance().collection("Tiendas").document(idTienda)
            .collection("servicios")

        db.get().addOnSuccessListener { res ->
            val tempList = mutableListOf<dataclass_mas_art_promo_servicio>()

            for (datos in res) {
                val data = datos.data
                val img = data?.get("UrlImg") as? String ?: ""
                val titulo = data?.get("nombre") as? String ?: ""
                val descripcion = data?.get("descripcion") as? String ?: ""
                val id = data?.get("id") as? String ?: ""
                val precio = data?.get("precio") as? String ?: ""

                val dataclass = dataclass_mas_art_promo_servicio(id, idTienda, img, titulo, descripcion, precio)
                tempList.add(dataclass)
            }

            // Barajar la lista para obtener un orden aleatorio
            tempList.shuffle()

            // Si solo quieres mostrar una cantidad específica, por ejemplo, 5 elementos
            val cantidadAMostrar = 5
            listaServicios.addAll(tempList.take(cantidadAMostrar))

            // Llamar a la función para activar el RecyclerView
            activarRecicle(listaServicios, binding.recicleItemMas, this)
        }
            .addOnFailureListener { e ->
                android.util.Log.d("error", "error al obtener los datos: ${e.message}")
            }
    }


    private fun activarRecicle(
        listaServicios: MutableList<dataclass_mas_art_promo_servicio>,
        recyclerContainer: RecyclerView, context: Context,
    ) {
        val adaptador = adapter_item_mas_promo_servicios_art(
            listaServicios, "servicios"
        )
        recyclerContainer.apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            adapter = adaptador
        }
    }
}