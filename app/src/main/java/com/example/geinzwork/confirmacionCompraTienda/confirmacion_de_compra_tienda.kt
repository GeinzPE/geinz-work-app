package com.geinzz.geinzwork.confirmacionCompraTienda

import android.content.Context
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.geinzwork.adapterViewholder.adapter_item_art_compra_carrito
import com.example.geinzwork.dataclass.dataclas_item_preview_art_comprar
import com.geinzz.geinzwork.R
import com.geinzz.geinzwork.adapterViewholder.adapter_item_mas_promo_servicios_art
import com.geinzz.geinzwork.constantesGeneral.constantes
import com.geinzz.geinzwork.constantesGeneral.constantesCarrito
import com.geinzz.geinzwork.databinding.ActivityConfirmacionDeCompraTiendaBinding
import com.geinzz.geinzwork.databinding.BottomSheetCompraProductosTiendasBinding
import com.geinzz.geinzwork.databinding.BottomSheetMostrarProductosBinding
import com.geinzz.geinzwork.dataclass.dataclassCarritoCompra
import com.geinzz.geinzwork.dataclass.dataclassCarritoCompras
import com.geinzz.geinzwork.dataclass.dataclass_mas_art_promo_servicio
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDragHandleView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import org.json.JSONException
import org.json.JSONObject

class confirmacion_de_compra_tienda : AppCompatActivity() {
    private lateinit var binding: ActivityConfirmacionDeCompraTiendaBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var bottomSheet: BottomSheetDragHandleView
    private lateinit var dialog: BottomSheetDialog
    private lateinit var bindingbottomShet: BottomSheetMostrarProductosBinding
    private val lista = mutableListOf<dataclas_item_preview_art_comprar>()

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityConfirmacionDeCompraTiendaBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        firebaseAuth = FirebaseAuth.getInstance()
        setearDatos()
        val tipoTienda = intent.getStringExtra("tipoTienda").toString()
        val localidad = intent.getStringExtra("localidad_USER").toString()
        val localidadTienda = intent.getStringExtra("localidadTiendaa").toString()
        val nombreUSer = intent.getStringExtra("nombreUser").toString()
        val numeroUser = intent.getStringExtra("numeroUser").toString()
        val nombreTienda = intent.getStringExtra("nombreTienda").toString()
        val idReferenciaEnvio = intent.getStringExtra("idReferenciaEnvio").toString()
        val TotalPagar = intent.getStringExtra("total").toString()
        val metodoEntrega = intent.getStringExtra("metodoEntrega").toString()
        val metodoPago = intent.getStringExtra("metodoPago").toString()
        val idTienda = intent.getStringExtra("idTienda").toString()
        val listaProductoss = intent.getStringExtra("listaProductos").toString()
        val descripcion = intent.getStringExtra("descripcion").toString()
        val codigoPedido = intent.getStringExtra("codigoPedigo").toString()
        val totalProductos = intent.getStringExtra("totalProductos").toString()
        val precio_delivery = intent.getStringExtra("precio_delivery").toString()
        val precioDriverVeri = if (precio_delivery == "********") {
            0.0
        } else {
            precio_delivery.toDoubleOrNull() ?: 0.0
        }

        val referencia_USER = intent.getStringExtra("referencia_USER").toString()
        val hora = binding.hora.text.toString()
        val fecha = binding.fecha.text.toString()
        val total = intent.getStringExtra("total").toString()

        binding.productos.setOnClickListener {
            dialog = BottomSheetDialog(this)
            obtenerProductos(
                totalProductos,
                precio_delivery,
                total,
                idTienda.toString(),
                listaProductoss
            )
            dialog.show()
        }



        binding.confirmar.setOnClickListener {
            val builder = AlertDialog.Builder(this)
            builder.setTitle("Antes de continuar")
            builder.setMessage("Al confirmar este pedido, su tiempo máximo de cancelación es de 3 minuto. Pasados 3 minuto, el pedido no podrá ser cancelado.")
            builder.setPositiveButton("Aceptar") { dialog, which ->
                val carritoDataclass = dataclassCarritoCompra(
                    idPedido = codigoPedido,
                    idTienda = idTienda,
                    idUser = firebaseAuth.uid.toString(),
                    idRef_user = idReferenciaEnvio.takeIf { it.isNotBlank() }
                        ?: "", // Como en la data class es opcional, lo dejamos nulo

                    metodoEntrega = metodoEntrega,
                    metodoPago = metodoPago,
                    precioDelivery = precioDriverVeri,
                    productos = listaProductoss, // Asegúrate de que este campo sea un `String` si es necesario

                    tipoRealizado = "compra_carrito",
                    totalCancelar = TotalPagar.toDouble(),
                    totalDriver = precioDriverVeri,
                    totalProductos = totalProductos.toDouble(),

                    estado = "pendiente",
                    estadoPedido = "pendiente",

                    fecha = fecha,
                    hora = hora,
                    idDriver = "" // Se mantiene vacío si no hay un ID asignado
                )


                constantesCarrito.agregarCompra(
                    carritoDataclass,
                    this
                ).addOnCompleteListener { task ->

                }
                constantesCarrito.retornarnumeroTienda(idTienda!!) { numero ->
                    if (numero != null) {
                        constantes.contactarWhatsapp(numero, "Número de orden: $codigoPedido", this)
                    } else {
                        println("No se pudo obtener el número de la tienda.")
                    }
                }

                dialog.dismiss()
            }
            builder.setNegativeButton("Cancelar") { dialog, which ->
                dialog.dismiss()
            }

            val dialog = builder.create()
            dialog.show()
        }


    }


    @RequiresApi(Build.VERSION_CODES.O)
    private fun setearDatos() {
        val nombreUser = intent.getStringExtra("nombreUser").toString()
        val totalProductos = intent.getStringExtra("totalProductos").toString()
        val numeroUser = intent.getStringExtra("numeroUser").toString()
        val referencia_USER = intent.getStringExtra("referencia_USER").toString()
        val direccion_USER = intent.getStringExtra("direccion_USER").toString()
        val localidad_USER = intent.getStringExtra("localidad_USER").toString()
        val tipoTienda = intent.getStringExtra("tipoTienda").toString()
        val localidadTiendaa = intent.getStringExtra("localidadTiendaa").toString()
        val nombreTienda = intent.getStringExtra("nombreTienda").toString()
        val metodoEntrega = intent.getStringExtra("metodoEntrega").toString()
        val metodoPago = intent.getStringExtra("metodoPago").toString()
        val idTienda = intent.getStringExtra("idTienda").toString()
        val codigoPedigo = intent.getStringExtra("codigoPedigo").toString()
        val Delivery_escojido = intent.getStringExtra("Delivery_escojido").toString()
        val precio_delivery = intent.getStringExtra("precio_delivery").toString()
        val total = intent.getStringExtra("total").toString()


        if (direccion_USER.isEmpty() && referencia_USER.isEmpty()) {
            binding.layoutDescripcion.isVisible = false
            binding.layoutDireccion.isVisible = false
        }
        if (metodoEntrega.isEmpty()) {
            binding.metodoEntrega.text = "Delivery"
        } else {
            binding.metodoEntrega.text = metodoEntrega
        }
        if (Delivery_escojido.equals("") || precio_delivery.equals(" ***")) {
            binding.linealDriverPrecio.isVisible = false
        } else {
            binding.linealDriverPrecio.isVisible = true
            binding.deliveryEscojido.text = Delivery_escojido
            binding.precioDriver.text = precio_delivery
        }
        constantesCarrito.obtnerfechaHora(binding.hora, binding.fecha)
        binding.codigoPedido.text = codigoPedigo
        binding.nombreUser.text = nombreUser
        binding.numero.text = numeroUser
        binding.nombreTienda.text = nombreTienda
        binding.direccion.text = direccion_USER
        binding.totalPagar.text = total
        binding.tipop.text = metodoPago
        binding.tipoTienda.text = tipoTienda
        binding.localidadTienda.text = localidadTiendaa
        binding.localidaUSer.text = localidad_USER
        binding.referenciaEntrega.text = referencia_USER
        binding.totalProductos.text = totalProductos
    }

    private fun generarOrden(codigoPedio: String): String {
        constantes.contactarWhatsapp("937659216", "numero de orden $codigoPedio", this)
        return codigoPedio
    }

    private fun obtenerProductos(
        total_productos: String,
        delivery: String,
        total: String,
        idTienda: String,
        listaProductoss: String?,
    ) {
        lista.clear()
        bindingbottomShet = BottomSheetMostrarProductosBinding.inflate(LayoutInflater.from(this))
        val view = bindingbottomShet.root
        bottomSheet = view.findViewById(R.id.cerrar)
        val recicleView = bindingbottomShet.recicleProductos
        if (delivery.equals("********")) {
            bindingbottomShet.linealDelivery.isVisible = false
            bindingbottomShet.totalCompra.text = total
        }
        bindingbottomShet.totalCompra.text = total
        bindingbottomShet.precioDrivers.text = delivery
        bindingbottomShet.totalProductos.text = total_productos

        if (!listaProductoss.isNullOrEmpty()) {
            try {
                val jsonObject = JSONObject(listaProductoss)
                val productKeys = jsonObject.keys()

                while (productKeys.hasNext()) {
                    val productId = productKeys.next()
                    val db = FirebaseFirestore.getInstance()
                        .collection("Tiendas")
                        .document(idTienda)
                        .collection("articulos")
                        .document(productId)

                    db.get().addOnSuccessListener { res ->
                        if (res.exists()) {
                            val data = res.data
                            val img = data?.get("imgArticulo") as? String ?: ""
                            val id = data?.get("id") as? String ?: ""
                            val nombreArticulo = data?.get("nombreArticulo") as? String ?: ""
                            val productDetails = jsonObject.getJSONObject(productId)
                            val cantidad = productDetails.getInt("cantidad")
                            val precio = productDetails.getDouble("precio")
                            val datos_dataclas = dataclas_item_preview_art_comprar(
                                id,
                                img,
                                nombreArticulo,
                                precio,
                                cantidad
                            )
                            lista.add(datos_dataclas)
                            activarRecicle(lista, recicleView, this)
                        } else {
                            println("Producto con ID $productId no existe en Firestore")
                        }
                    }.addOnFailureListener { exception ->
                        println("Error al obtener el producto con ID $productId: ${exception.message}")
                    }
                }
            } catch (e: JSONException) {
                e.printStackTrace()
                println("Error al parsear el JSON: ${e.message}")
            }
        } else {
            println("El JSON de la lista de productos está vacío o es nulo")
        }
        dialog.setContentView(view)
    }

    private fun activarRecicle(
        listaServicios: MutableList<dataclas_item_preview_art_comprar>,
        recyclerContainer: RecyclerView, context: Context,
    ) {
        val adaptador = adapter_item_art_compra_carrito(
            listaServicios
        )
        recyclerContainer.apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
            adapter = adaptador
        }
    }


}