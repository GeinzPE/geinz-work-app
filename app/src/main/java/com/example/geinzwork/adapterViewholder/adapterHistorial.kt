package com.geinzz.geinzwork.adapterViewholder

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.geinzwork.adapterViewholder.adapter_item_art_compra_carrito
import com.example.geinzwork.dataclass.dataclas_item_preview_art_comprar
import com.geinzz.geinzwork.DiffUtilClass.DiffUtilHistorial
import com.geinzz.geinzwork.R
import com.geinzz.geinzwork.constantesGeneral.constantes
import com.geinzz.geinzwork.constantesGeneral.constantesCarrito
import com.geinzz.geinzwork.databinding.BottomSheetMostrarProductosBinding
import com.geinzz.geinzwork.databinding.ItemHistorialBinding
import com.geinzz.geinzwork.dataclass.dataclassHistorial
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDragHandleView
import com.google.android.material.imageview.ShapeableImageView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.firestore.FirebaseFirestore

import de.hdodenhof.circleimageview.CircleImageView
import org.json.JSONException
import org.json.JSONObject
import java.time.Duration
import java.time.LocalTime
import java.time.format.DateTimeFormatter


class adapterHistorial(
    private var listaHistorial: List<dataclassHistorial>,
    private val generarQR: (dataclassHistorial) -> Unit,
) : RecyclerView.Adapter<adapterHistorial.viewHolder>() {

    fun updateList(newList: List<dataclassHistorial>) {
        val historail = DiffUtilHistorial(listaHistorial, newList)
        val resukt = DiffUtil.calculateDiff(historail)
        listaHistorial = newList
        resukt.dispatchUpdatesTo(this)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): viewHolder {
        val binding =
            ItemHistorialBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return viewHolder(binding)
    }

    override fun getItemCount(): Int {
        return listaHistorial.size
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onBindViewHolder(holder: viewHolder, position: Int) {
        val item = listaHistorial[position]
        holder.render(item, generarQR)
    }

    class viewHolder(private val binding: ItemHistorialBinding) :
        RecyclerView.ViewHolder(binding.root) {
        private val lista = mutableListOf<dataclas_item_preview_art_comprar>()
        private lateinit var dialog: BottomSheetDialog
        private lateinit var bindingbottomShet: BottomSheetMostrarProductosBinding
        private lateinit var bottomSheet: BottomSheetDragHandleView
        val codigoPedido = binding.codigoPedido
        val nombreTienda = binding.nombreTienda
        val nombreuser = binding.nombreuser
        val numerouser = binding.numerous
        val TipoTienda = binding.TipoTienda
        val horaus = binding.horaus
        val fechaus = binding.fechaus
        val estadouser = binding.estadous
        val entregaMetodo = binding.entregaMetodo
        val pagoMetodo = binding.pagometodo
        val Totaluser = binding.Totalus
        val imgPerfilTienda = binding.imgTienda
        val containerListenr = binding.containerListenr
        val cancelarPedido = binding.cancelar

        val localidaduser = binding.localidaduser
        val localidadTienda = binding.localidadTienda


        val relativeTienda = binding.relativeTienda
        val relativedriver = binding.relativedriver
        val relativecamino = binding.relativecamino
        val relativeentrega = binding.relativeentrega

        val linealDriver = binding.containerDriver
        val nombreDriver = binding.nombreDriver
        val nacionalidad_driver = binding.nacionalidadDriver
        val imgDriver = binding.imgDriver
        val whatsappDriver = binding.whatsappDriver
        val LlamadaDriver = binding.LlamadaDriver
        val generoDriver = binding.generoDriver
        val btn_verProductos = binding.verProductos
        val precioDelivery = binding.precioDelivery
        val direccion = binding.direccion
        val referencia = binding.referencia
        val totalDriver = binding.totalDriver
        val totalCancelar = binding.totalCancelar
        val tipoCompra = binding.tipoCompra

        @RequiresApi(Build.VERSION_CODES.O)
        @SuppressLint("ResourceAsColor")
        fun render(
            dataclassHistorial: dataclassHistorial,
            generarQR: (dataclassHistorial) -> Unit,
        ) {
            binding.layoutPrevizualisacion.textoPreview.isVisible = false
            binding.layoutPrevizualisacion.LinealStok.isVisible = false

            if (dataclassHistorial.tipoRealizado == "compra carrito") {
                binding.lineaLayout.isVisible = false
            } else {
                binding.verProductos.isVisible = false
                binding.lineaLayout.isVisible = true
                obtnerDatosPromociones(
                    dataclassHistorial.productos.toString(),
                    dataclassHistorial.idTienda.toString(),
                    binding.layoutPrevizualisacion.imgItem,
                    binding.layoutPrevizualisacion.tituloItem,
                    binding.layoutPrevizualisacion.precioItem,
                    dataclassHistorial.totalItems.toString(),
                    binding.layoutPrevizualisacion.cantidad
                )
            }

            btn_verProductos.setOnClickListener {
                dialog = BottomSheetDialog(itemView.context)
                obtenerProductos(
                    dataclassHistorial.totalProductos.toString(),
                    dataclassHistorial.totalDriver.toString(),
                    dataclassHistorial.total.toString(),
                    dataclassHistorial.idTienda.toString(),
                    dataclassHistorial.productos.toString()
                )
                dialog.show()
            }

            TipoTienda.text = dataclassHistorial.tipoTienda
            codigoPedido.text = dataclassHistorial.idPedido
            nombreTienda.text = dataclassHistorial.nombreTienda
            nombreuser.text = dataclassHistorial.nombre
            numerouser.text = dataclassHistorial.numero
            horaus.text = dataclassHistorial.hora
            fechaus.text = dataclassHistorial.fecha
            estadouser.text = dataclassHistorial.estadp
            referencia.text = dataclassHistorial.referencia
            direccion.text = dataclassHistorial.direccion
            entregaMetodo.text = dataclassHistorial.metodoentrega
            pagoMetodo.text = dataclassHistorial.metodoPago
            localidaduser.text = dataclassHistorial.localidadUSer
            localidadTienda.text = dataclassHistorial.localidadTienda
            precioDelivery.text = dataclassHistorial.precioDelivery
            totalDriver.text = dataclassHistorial.totalDriver
            totalCancelar.text = dataclassHistorial.total
            Totaluser.text = dataclassHistorial.totalProductos
            tipoCompra.text = dataclassHistorial.tipoRealizado

            if(dataclassHistorial.metodoentrega.equals("Delivery")){
                linealDriver.isVisible=true
            }else{
                linealDriver.isVisible=false
            }

            when (dataclassHistorial.estadp.toString()) {
                "Aceptado"->{
                    relativeTienda.setBackgroundResource(R.drawable.round_icono_pedidos_estado)
                }
                "driver"->{
                    relativeTienda.setBackgroundResource(R.drawable.round_icono_pedidos_estado)
                    relativedriver.setBackgroundResource(R.drawable.round_icono_pedidos_estado)
                }
                "camino"->{
                    relativeTienda.setBackgroundResource(R.drawable.round_icono_pedidos_estado)
                    relativedriver.setBackgroundResource(R.drawable.round_icono_pedidos_estado)
                    relativecamino.setBackgroundResource(R.drawable.round_icono_pedidos_estado)
                }
                "entregado"->{
                    relativeTienda.setBackgroundResource(R.drawable.round_icono_pedidos_estado)
                    relativedriver.setBackgroundResource(R.drawable.round_icono_pedidos_estado)
                    relativecamino.setBackgroundResource(R.drawable.round_icono_pedidos_estado)
                    relativeentrega.setBackgroundResource(R.drawable.round_icono_pedidos_estado)
                }
            }



            if (entregaMetodo.text.toString() == "Recojo en tienda") {
                relativedriver.isVisible = false
                relativecamino.isVisible = false
                binding.linealDireccion.isVisible = false
                binding.linealReferencia.isVisible = false
                binding.precioDriverLineal.isVisible = false
                binding.TotalDriverLineal.isVisible = false

            }
            ocultarBotonPedido(dataclassHistorial, cancelarPedido)

            cancelarPedido.setOnClickListener {
                cancelarPedidofun(dataclassHistorial)
            }

           obtenerDatosDriver(binding,dataclassHistorial)



            constantesCarrito.obtnerFotoPErfilTienda(
                itemView.context,
                dataclassHistorial.idTienda.toString(),
                imgPerfilTienda
            )


            containerListenr.setOnClickListener {
                generarQR(dataclassHistorial)
            }
        }

        private fun obtenerDatosDriver(
            binding:ItemHistorialBinding,
            dataclassHistorial: dataclassHistorial,
        ) {
            val db = FirebaseFirestore.getInstance().collection("Trabajadores_Usuarios_Drivers")
                .document("drivers").collection("drivers")
                .document(dataclassHistorial.idDriver.toString())
            db.get().addOnSuccessListener { res ->
                if (res.exists()) {
                    // Obtener los datos del documento
                    val data = res.data
                    val nombre = data?.get("nombre") as? String
                    val imgPerfilDriver = data?.get("imagen_perfil") as? String
                    val numeroDriver = data?.get("telefono") as? String
                    val disponible = data?.get("disponible") as? Boolean?:false
                    val genero = data?.get("genero") as? String
                    val nacionalidad = data?.get("nacionalidad") as? String

                    binding.nombreDriver.text = nombre ?: "N/A"
                    binding.nacionalidadDriver.text = nacionalidad ?: "N/A"
                    binding.generoDriver.text = genero ?: "N/A"

                    try {
                        Glide.with(itemView.context)
                            .load(imgPerfilDriver)
                            .into(binding.imgDriver)
                    } catch (e: Exception) {
                        println("Error al setear la imagen: ${e.message}")
                    }

                    binding.whatsappDriver.setOnClickListener {
                        constantes.contactarWhatsapp(
                            numeroDriver.toString(),
                            "Hola, estoy esperando mi pedido ${dataclassHistorial.idPedido}",
                            itemView.context
                        )
                    }

                    binding.LlamadaDriver.setOnClickListener {
                        constantes.llamarCliente(
                            itemView.context,
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


        private fun obtenerItems(
            idTienda: String,
            idArticulo: String,
            cantidad: String,
            onResult: (String) -> Unit,
        ) {
            val db2 = FirebaseFirestore.getInstance().collection("Tiendas").document(idTienda)
                .collection("articulos").document(idArticulo)
            db2.get().addOnSuccessListener { res ->
                if (res.exists()) {
                    val data = res.data
                    val nombre_ART = data?.get("nombreArticulo") as? String ?: ""
                    val precio = data?.get("precio") as? String ?: ""
                    if (nombre_ART != null && precio != null) {
                        val resultado =
                            "Nombre: $nombre_ART\nPrecio: $precio\nCantidad: $cantidad\n\n"
                        onResult(resultado)
                    } else {
                        onResult("No se pudieron obtener algunos valores para el artículo: $idArticulo")
                    }
                } else {
                    onResult("El artículo no existe en la base de datos: $idArticulo")
                }
            }.addOnFailureListener {
                Log.d("error", "articulo no encontrado")
            }
        }

//        private fun actualizarTextViewConProductos(
//            dataclassHistorial: dataclassHistorial,
//            textView: TextView
//        ) {
//            val productosTexto = StringBuilder()
//            val productos = dataclassHistorial.productos ?: return
//
//            var itemsProcesados = 0
//            productos.forEach { (idProducto, cantidad) ->
//                obtenerItems(
//                    dataclassHistorial.idTienda.toString(),
//                    idProducto,
//                    cantidad.toString()
//                ) { resultado ->
//                    productosTexto.append(resultado)
//                    itemsProcesados++
//                    if (itemsProcesados == productos.size) {
//                        textView.text = productosTexto.toString()
//                    }
//                }
//            }
//        }

        private fun obtenerProductos(
            total_productos: String,
            delivery: String,
            total: String,
            idTienda: String,
            listaProductoss: String?,
        ) {
            lista.clear()
            bindingbottomShet =
                BottomSheetMostrarProductosBinding.inflate(LayoutInflater.from(itemView.context))
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
//                            if (res.exists()) {
//                                val data = res.data
//                                val img = data?.get("imgArticulo") as? String ?: ""
//                                val nombreArticulo = data?.get("nombreArticulo") as? String ?: ""
//                                val id = data?.get("id") as? String ?: ""
//                                val productDetails = jsonObject.getJSONObject(productId)
//                                val cantidad = productDetails.getInt("cantidad")
//                                val precio = productDetails.getDouble("precio")
//                                val datos_dataclas = dataclas_item_preview_art_comprar(
//                                    id,
//                                    img,
//                                    nombreArticulo,
//                                    precio,
//                                    cantidad
//                                )
//                                lista.add(datos_dataclas)
//                                activarRecicle(lista, recicleView, itemView.context)
//                            } else {
//                                println("Producto con ID $productId no existe en Firestore")
//                            }
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


        @RequiresApi(Build.VERSION_CODES.O)
        private fun ocultarBotonPedido(dataclassHistorial: dataclassHistorial, button: Button) {
            // Formato de hora esperado en dataclassHistorial.hora (por ejemplo: "10:30:00 AM")
            val formatter = DateTimeFormatter.ofPattern("hh:mm:ss a")

            // Obtener la hora del pedido y la hora actual
            val pedidoTime = LocalTime.parse(dataclassHistorial.hora, formatter)
            val currentTime = LocalTime.now()

            // Calcular la diferencia de tiempo en minutos
            val duration = Duration.between(pedidoTime, currentTime)
            val minutesElapsed = duration.toMinutes()

            // Verificar si han pasado más de 5 minutos desde el pedido
            if (minutesElapsed > 5) {
                button.isVisible = false
            }
        }

        @RequiresApi(Build.VERSION_CODES.O)
        private fun cancelarPedidofun(dataclassHistorial: dataclassHistorial) {
            // Formato de hora esperado en dataclassHistorial.hora (por ejemplo: "10:30:00 AM")
            val formatter = DateTimeFormatter.ofPattern("hh:mm:ss a")

            // Obtener la hora del pedido y la hora actual
            val pedidoTime = LocalTime.parse(dataclassHistorial.hora, formatter)
            val currentTime = LocalTime.now()

            // Calcular la diferencia de tiempo en minutos
            val duration = Duration.between(pedidoTime, currentTime)
            val minutesElapsed = duration.toMinutes()

            // Verificar si han pasado menos de 5 minutos desde el pedido
            if (minutesElapsed <= 1) {
                // Referencia al nodo del pedido en la base de datos de Firebase
                val db = FirebaseDatabase.getInstance().getReference("PedidosUsuario")
                    .child(dataclassHistorial.iduser.toString()).child("tiendas")
                    .child(dataclassHistorial.idTienda.toString()).child("compra")
                    .child(dataclassHistorial.idPedido.toString())

                // Eliminar el pedido de la base de datos
                db.removeValue().addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        // Mostrar mensaje de éxito al cancelar el pedido
                        Toast.makeText(
                            itemView.context,
                            "Pedido cancelado correctamente.",
                            Toast.LENGTH_SHORT
                        ).show()
                        // Aquí puedes realizar cualquier otra acción necesaria después de cancelar el pedido
                    } else {
                        // Mostrar mensaje de error si falla la operación de cancelación
                        Toast.makeText(
                            itemView.context,
                            "Error al cancelar el pedido: ${task.exception?.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            } else {
                // Más de 5 minutos han pasado, no se puede cancelar el pedido
                Toast.makeText(
                    itemView.context,
                    "No se puede cancelar el pedido después de 5 minutos.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        private fun obtnerDatosPromociones(
            idProducto: String,
            idTienda: String,
            Img: ShapeableImageView,
            Titulo: TextView,
            Precio: TextView,
            cantidadSelecionada: String,
            cantidad: TextView,
        ) {

            val db = FirebaseFirestore.getInstance().collection("Tiendas").document(idTienda)
                .collection("promociones").document(idProducto)
            db.get().addOnSuccessListener { res ->
                if (res.exists()) {
                    val data = res.data
                    val img = data?.get("imagenUrl") as? String ?: ""
                    val titulo = data?.get("titulo") as? String ?: ""
                    val precio = data?.get("precio") as? String ?: ""
                    cantidad.text = cantidadSelecionada
                    try {
                        Glide.with(itemView.context)
                            .load(img)
                            .into(Img)
                    } catch (e: Exception) {
                        println("error al setear la img")
                    }
                    Titulo.text = titulo
                    Precio.text = precio
                }
            }

        }


    }


}