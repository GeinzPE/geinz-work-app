package com.example.geinzwork.fragmentos.productosPublicadosVista

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.geinzz.geinzwork.R
import com.geinzz.geinzwork.adapterViewholder.adapter_radioButton_envios
import com.geinzz.geinzwork.constantesGeneral.constantesCarrito
import com.geinzz.geinzwork.constantesGeneral.constantesPublicidad
import com.geinzz.geinzwork.constantesGeneral.constantestextos_general
import com.geinzz.geinzwork.databinding.ActivityComprasProductosVendedorBinding
import com.geinzz.geinzwork.dataclass.dataclassradiobtn
import com.geinzz.geinzwork.vistaTiendas.direccion_entrega_lat_log
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class compras_productos_vendedor : AppCompatActivity() {
    private lateinit var binding: ActivityComprasProductosVendedorBinding
    private val listaLugaresEntrega = mutableListOf<dataclassradiobtn>()
    private lateinit var dialog: BottomSheetDialog
    private lateinit var firebaseAuth: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityComprasProductosVendedorBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        firebaseAuth = FirebaseAuth.getInstance()
        val idTrabajdor = intent.getStringExtra("idTrabajador").toString()
        val idProducto = intent.getStringExtra("idProducto").toString()
        println("datos eobnieods del trabajODR  $idProducto,$idTrabajdor")
        binding.categoriaProductos.textoNumero1.text = "Categoria del producto : "
        binding.marcaProducto.textoNumero1.text = "Marca :"
        binding.modeloProducto.textoNumero1.text = "Modelo :"
        binding.StokDiponible.textoNumero1.text = "Stok disponible :"
        binding.garantiaProducto.textoNumero1.text = "Garantia :"
        binding.condicionProducto.textoNumero1.text = "Condicion del producto :"
        binding.MetodoEntregaProducto.textoNumero1.text = "Metodo de entrega :"
        binding.precioproducto.textoNumero1.text = "Precio :"
        binding.precioDelivery.textoNumero1.text = "Precio del delivery :"
        binding.totalcancelar.textoNumero1.text = "Total a cancelar :"
        binding.MetodoEntregaProducto.textoNumero1.text = "Metodo de entrega :"
        binding.codigoGeneradoCompra.text = constantesCarrito.generarCodigoPedido()
        obtnerDireciones(
            firebaseAuth.uid.toString(),
            listaLugaresEntrega,
            binding.verLugaresEntrega, binding.creaDireccion, this
        ) { cargado ->
//            if (cargado) {
//                binding.linealCargandoRef.isVisible = false
//                binding.verLugaresEntrega.isVisible = true
//            } else {
//                binding.linealCargandoRef.isVisible = true
//                binding.verLugaresEntrega.isVisible = false
//            }
        }


        obtnerDatosProducto(idProducto, idTrabajdor) { cargado ->
            if (cargado) {
                incrementarCantidad()
                binding.linealCarga.isVisible = false
                binding.netScroolView.isVisible = true
            } else {
                binding.linealCarga.isVisible = true
                binding.netScroolView.isVisible = false
            }
        }
        println("el idtrabajodr $idTrabajdor y el $idProducto")
        verificarRegistroUSer(firebaseAuth)
    }

    override fun onBackPressed() {
        AlertDialog.Builder(this)
            .setTitle("Confirmación")
            .setMessage("¿Desea abandonar la compra o seguir comprando?")
            .setPositiveButton("Sí, abandonar") { _, _ ->
                super.onBackPressed() // Cierra la actividad
            }
            .setNegativeButton("Seguir comprando") { dialog, _ ->
                dialog.dismiss() // Solo cierra el diálogo
            }
            .show()
    }


    private fun verificarRegistroUSer(firebaseAuth: FirebaseAuth) {
        binding.linealRecicleCargandoRerf.isVisible = firebaseAuth.currentUser != null

        // Método para mostrar el BottomSheetDialog si no está autenticado
        val mostrarDialogCreacionCuenta: () -> Unit = {
            if (firebaseAuth.currentUser == null) {
                dialog = BottomSheetDialog(this)
                constantesPublicidad.CreacionCuentaBottom_shett(this, dialog)
                dialog.show()
            }
        }

        binding.creaDireccion.setOnClickListener {
            mostrarDialogCreacionCuenta()
            if (firebaseAuth.currentUser != null) {
                startActivity(Intent(this, direccion_entrega_lat_log::class.java))
                finish()
            }
        }

        binding.comfirmarCompra.setOnClickListener {
            mostrarDialogCreacionCuenta()
            if (firebaseAuth.currentUser != null) {
                verificarCamposCompletos()
            }
        }

        binding.cancelarCompra.setOnClickListener {
            AlertDialog.Builder(it.context)
                .setTitle("Confirmación")
                .setMessage("¿Desea abandonar la compra o seguir comprando?")
                .setPositiveButton("Sí, abandonar") { _, _ -> finish() }
                .setNegativeButton("Seguir comprando") { dialog, _ -> dialog.dismiss() }
                .show()
        }
    }

    private fun verificarCamposCompletos() {
        var camposValidos = true // Variable para verificar si todo está correcto

        val nombreCompletoVacio = binding.nombresCompletosED.text.toString().trim().isEmpty()
        val dniVacio = binding.dniED.text.toString().trim()
            .isEmpty() // Corregí el error de repetir nombresCompletosED
        val numeroContactoVacio = binding.NumeroDeContactoED.text.toString().trim().isEmpty()

        if (nombreCompletoVacio) {
            binding.nombresCompletosED.error = "Ingrese su nombre y apellido"
            camposValidos = false
        }
        if (dniVacio) {
            binding.dniED.error = "Ingrese su DNI"
            camposValidos = false
        }
        if (numeroContactoVacio) {
            binding.NumeroDeContactoED.error = "Ingrese su número de contacto"
            camposValidos = false
        }

        if (binding.MetodoEntregaProducto.textoNumero2.text.toString()
                .equals("delivery", ignoreCase = true)
        ) {
            val direccionVacia = binding.direccionEntregaED.text.toString().trim().isEmpty()
            val referenciaVacia = binding.ReferenciaEntregaED.text.toString().trim().isEmpty()

            if (direccionVacia) {
                binding.direccionEntregaED.error = "Seleccione una dirección o cree una nueva"
                camposValidos = false
            }

            if (referenciaVacia) {
                binding.ReferenciaEntregaED.error = "Ingrese una referencia válida"
                camposValidos = false
            }
        }

        // Verificación del método de pago
        if (!binding.metodoYape.isChecked && !binding.metodoPlin.isChecked && !binding.metodoEfectivo.isChecked) {
            Toast.makeText(binding.root.context, "Seleccione un método de pago", Toast.LENGTH_SHORT)
                .show()
            camposValidos = false
        }

        // Si todos los campos están correctos, mostrar mensaje de confirmación
        if (camposValidos) {
            Toast.makeText(
                binding.root.context,
                "¡Todo correcto! Continúe con su proceso",
                Toast.LENGTH_SHORT
            ).show()
        }
    }


    fun obtnerDireciones(
        idUSer: String,
        lista: MutableList<dataclassradiobtn>,
        RecyclerView: RecyclerView,
        btnCrearDirecion: Button,
        context: Context,
        cargado: (Boolean) -> Unit
    ) {
        binding.linealCargandoRef.isVisible = true
        binding.verLugaresEntrega.isVisible = false
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
                inicializarRecycle(lista)
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
                        inicializarRecycle(lista)
                    }
                    if (lista.isEmpty()) {
                        RecyclerView.isVisible = false
                        btnCrearDirecion.isVisible = true
                        binding.linealCargandoRef.isVisible=false
                        cargado(false)

                    } else {
                        RecyclerView.isVisible = true
                        binding.linealCargandoRef.isVisible=false
                        btnCrearDirecion.isVisible = false
                        cargado(true)
                    }
                }.addOnFailureListener { e ->
                    Toast.makeText(
                        context, "Error al buscar en usuarios: ${e.message}", Toast.LENGTH_SHORT
                    ).show()
                    cargado(false)
                    binding.linealCargandoRef.isVisible=false
                }
            } else {
                RecyclerView.isVisible = true
                btnCrearDirecion.isVisible = false
                binding.linealCargandoRef.isVisible=false
                cargado(true)
            }
        }.addOnFailureListener { e ->
            Toast.makeText(
                context, "Error al buscar en trabajadores: ${e.message}", Toast.LENGTH_SHORT
            ).show()
            cargado(false)
            binding.linealCargandoRef.isVisible=false
        }
    }

    private fun inicializarRecycle(items: MutableList<dataclassradiobtn>) {
        val adapter = adapter_radioButton_envios(
            items,
            onItemClick = { ubicacionSeleccionada ->
                binding.direccionEntregaED.setText(ubicacionSeleccionada.direccion)
                binding.ReferenciaEntregaED.setText(ubicacionSeleccionada.referencia)
                binding.lat.text = ubicacionSeleccionada.lat
                binding.log.text = ubicacionSeleccionada.log
                binding.TexviewIDRefDire.text = ubicacionSeleccionada.id
            },
            onCrearDireccionClick = {
                    if (firebaseAuth.currentUser == null) {
                        dialog = BottomSheetDialog(this)
                        constantesPublicidad.CreacionCuentaBottom_shett(this, dialog)
                        dialog.show()
                    }else{
                        startActivity(Intent(this, direccion_entrega_lat_log::class.java))
                        finish()
                    }

                println("Se hizo clic en 'Crear dirección'")
            }
        )

        binding.verLugaresEntrega.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        binding.verLugaresEntrega.adapter = adapter
    }

    private fun obtnerDatosProducto(
        idProducto: String,
        idTrabajador: String,
        cargado: (Boolean) -> Unit
    ) {
        val db = FirebaseFirestore.getInstance().collection("Trabajadores_Usuarios_Drivers")
            .document("trabajadores").collection("trabajadores").document(idTrabajador)
            .collection("productos_venta").document(idProducto)
        db.get().addOnSuccessListener { res ->
            if (res.exists()) {
                Log.d(
                    "FirestoreResponse",
                    "Datos obtenidos: ${res.data}"
                )  // 🔍 Ver qué trae la base de datos

                val data = res.data ?: return@addOnSuccessListener
                val cantidadPorcentajeDescuento =
                    data?.get("cantidad_porcentaje_descuento") as? Number ?: 0
                val precio = data?.get("precio") as? Number ?: 0
                val precioDescuento = data?.get("precio_descuento") as? Number ?: 0
                val totalProducto = data?.get("total_producto") as? Number ?: 0

                val categoria = data?.get("categoria") as? String ?: ""
                val condicionProducto = data?.get("condicion_producto") as? String ?: ""
                val descripcion = data?.get("descripcion") as? String ?: ""


                val modelo = data?.get("modelo") as? String ?: ""
                val descuento = data?.get("descuento") as? Boolean ?: false
                val efectivo = data?.get("efectivo") as? Boolean ?: false
                val entrega_domicilio = data?.get("entrega_domicilio") as? Boolean ?: true

                val garantia = data?.get("garantia") as? String ?: ""
                val id = data?.get("id") as? String ?: ""
                val fechaPublicada = data?.get("fechaPublicada") as? String ?: ""
                val metodoEntrega = data?.get("metodoEntrega") as? String ?: ""


                val lugarDeEntrega = data?.get("lugarEntrega") as? String ?: ""
                val marca = data?.get("marca") as? String ?: ""
                val nombre = data?.get("nombre") as? String ?: ""
                val plin = data?.get("plin") as? Boolean ?: false
                val stok = data?.get("stok") as? String ?: ""
                val yape = data?.get("yape") as? Boolean ?: false
                val envio_gratis = data?.get("envio_gratis") as? Boolean ?: false
                val precioDelivery = data?.get("precioDelivery") as? Number ?: 0
                val cantidad_porcentaje_descuento =
                    data?.get("cantidad_porcentaje_descuento") as? Number ?: 0
                Log.d(
                    "DatosProducto",
                    "Categoría: $categoria, Marca: $marca, Modelo: $modelo, Stock: $stok, Garantía: $garantia"
                )
                binding.precioDelivery.textoNumero2.text
                // Manejo del precio de delivery
                if (envio_gratis) {
                    binding.precioDelivery.textoNumero2.text = "Envío Gratis"
                    binding.precioDelivery.textoNumero2.setTextColor(
                        ContextCompat.getColor(
                            this,
                            R.color.verde
                        )
                    )
                    val precioFinal = if (descuento) precioDescuento else precio
                    constantestextos_general.setearPrecioDescuentoPrecioAntiguo(
                        precioFinal,
                        binding.totalcancelar.textoNumero2
                    )
                } else {
                    val precioBase = if (descuento) precioDescuento else precio
                    val precioFinalDelivery = precioBase.toDouble() + precioDelivery.toDouble()

                    constantestextos_general.setearPrecioDescuentoPrecioAntiguo(
                        precioDelivery,
                        binding.precioDelivery.textoNumero2
                    )

                    constantestextos_general.setearPrecioDescuentoPrecioAntiguo(
                        precioFinalDelivery,
                        binding.totalcancelar.textoNumero2
                    )
                }
                if (metodoEntrega == "delivery") {
                    binding.linealMetodoEntrega.isVisible = true
                } else {
                    binding.linealMetodoEntrega.isVisible = false
                }

                if (descuento) {
                    binding.precioproducto.AntiguoPrecio.isVisible = true
                    binding.precioproducto.descuentoPorcentaje.isVisible = true
                    constantestextos_general.marcarDescuentoTxt(binding.precioproducto.AntiguoPrecio)
                    constantestextos_general.setearPrecioDescuentoPrecioAntiguo(
                        precioDescuento,
                        binding.precioproducto.textoNumero2,
                        precio,
                        binding.precioproducto.AntiguoPrecio,
                        cantidad_porcentaje_descuento,
                        binding.precioproducto.descuentoPorcentaje
                    )
                } else {
                    constantestextos_general.setearPrecioDescuentoPrecioAntiguo(
                        precio,
                        binding.precioproducto.textoNumero2
                    )
                }



                binding.categoriaProductos.textoNumero2.text = categoria
                binding.marcaProducto.textoNumero2.text = marca
                binding.modeloProducto.textoNumero2.text = modelo
                binding.StokDiponible.textoNumero2.text = "$stok UND"
                binding.garantiaProducto.textoNumero2.text = garantia
                binding.condicionProducto.textoNumero2.text = condicionProducto
                binding.MetodoEntregaProducto.textoNumero2.text = metodoEntrega
                binding.descripcionProducto.text = descripcion
                constantestextos_general.extender_acortar_texto(
                    binding.descripcionProducto,
                    binding.tvReadMore
                )
                constantesCarrito.setearDatosUsuario { nombre, numero, localidad, apellido ->
                    val nombreCompleto = "${nombre ?: ""} ${apellido ?: ""}".trim()
                    binding.nombresCompletosED.setText(nombreCompleto)
                    binding.NumeroDeContactoED.setText(numero ?: "")
                }


                val cantidadTrue = listOf(yape, plin, efectivo).count { it }

                when (cantidadTrue) {
                    3 -> {
                        binding.metodoYape.isVisible = true
                        binding.metodoPlin.isVisible = true
                        binding.metodoEfectivo.isVisible = true
                    }

                    2 -> {
                        when {
                            yape && plin -> {
                                binding.metodoYape.isVisible = true
                                binding.metodoPlin.isVisible = true
                            }

                            yape && efectivo -> {
                                binding.metodoYape.isVisible = true
                                binding.metodoEfectivo.isVisible = true
                            }

                            plin && efectivo -> {
                                binding.metodoPlin.isVisible = true
                                binding.metodoEfectivo.isVisible = true
                            }
                        }
                    }

                    1 -> {
                        when {
                            yape -> {
                                binding.metodoYape.isVisible = true
                            }

                            plin -> {
                                binding.metodoPlin.isVisible = true
                            }

                            efectivo -> {
                                binding.metodoEfectivo.isVisible = true
                            }
                        }
                    }

                    0 -> println("Ningún método de pago está activo")
                }
                cargado(true)
            } else {
                cargado(false)
                Log.e("FirestoreResponse", "Documento no encontrado")
            }
        }.addOnFailureListener { e ->
            Log.e("FirestoreError", "Error obteniendo datos: ", e)
            cargado(false)
        }

    }

    private fun incrementarCantidad() {
        var numeroCantidad = binding.cantidad.text.toString().toInt()
        val mas = binding.mas
        val menos = binding.menos

        val totalPAga = binding.totalcancelar.textoNumero2.text.toString()
            .replace("S/", "", ignoreCase = true)
            .trim()
            .toDouble()

        val intCantidadStok = binding.StokDiponible.textoNumero2.text.toString()
            .replace("UND", "", ignoreCase = true)
            .trim()
            .toInt()

        mas.setOnClickListener {
            if (numeroCantidad < intCantidadStok) {
                numeroCantidad++
                binding.cantidad.text = numeroCantidad.toString()
                val totalPAgarPorCantidad = numeroCantidad * totalPAga
                binding.totalcancelar.textoNumero2.text = "S/ %.2f".format(totalPAgarPorCantidad)
            } else {
                Toast.makeText(binding.root.context, "No puede exceder el stock disponible", Toast.LENGTH_SHORT).show()
            }
        }

        menos.setOnClickListener {
            if (numeroCantidad > 1) {
                numeroCantidad--
                binding.cantidad.text = numeroCantidad.toString()
                val totalPAgarPorCantidad = numeroCantidad * totalPAga
                binding.totalcancelar.textoNumero2.text = "S/ %.2f".format(totalPAgarPorCantidad)
            }
        }
    }


}