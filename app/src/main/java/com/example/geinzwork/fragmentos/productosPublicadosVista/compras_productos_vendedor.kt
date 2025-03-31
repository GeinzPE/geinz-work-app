package com.example.geinzwork.fragmentos.productosPublicadosVista

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.geinzz.geinzwork.R
import com.geinzz.geinzwork.adapterViewholder.adapter_radioButton_envios
import com.geinzz.geinzwork.constantesGeneral.constantesCarrito
import com.geinzz.geinzwork.constantesGeneral.constantestextos_general
import com.geinzz.geinzwork.databinding.ActivityComprasProductosVendedorBinding
import com.geinzz.geinzwork.dataclass.dataclassradiobtn
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class compras_productos_vendedor : AppCompatActivity() {
    private lateinit var binding: ActivityComprasProductosVendedorBinding
    private val listaLugaresEntrega = mutableListOf<dataclassradiobtn>()
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
        obtnerDireciones(
            firebaseAuth.uid.toString(),
            listaLugaresEntrega,
            binding.verLugaresEntrega, binding.creaDireccion, this
        )
        println("el idtrabajodr $idTrabajdor y el $idProducto")
        obtnerDatosProducto(idProducto, idTrabajdor)

    }

    fun obtnerDireciones(
        idUSer: String,
        lista: MutableList<dataclassradiobtn>,
        RecyclerView: RecyclerView,
        btnCrearDirecion: Button,
        context: Context,
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
                    } else {
                        RecyclerView.isVisible = true
                        btnCrearDirecion.isVisible = false

                    }
                }.addOnFailureListener { e ->
                    Toast.makeText(
                        context, "Error al buscar en usuarios: ${e.message}", Toast.LENGTH_SHORT
                    ).show()
                }
            } else {
                RecyclerView.isVisible = true
                btnCrearDirecion.isVisible = false

            }
        }.addOnFailureListener { e ->
            Toast.makeText(
                context, "Error al buscar en trabajadores: ${e.message}", Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun inicializarRecycle(items: MutableList<dataclassradiobtn>) {
        val adapter = adapter_radioButton_envios(items) { ubicacionSeleccionada ->
            binding.direccionEntregaED.setText(ubicacionSeleccionada.direccion)
            binding.ReferenciaEntregaED.setText(ubicacionSeleccionada.referencia)
            binding.lat.text = ubicacionSeleccionada.lat
            binding.log.text = ubicacionSeleccionada.log
            binding.TexviewIDRefDire.text = ubicacionSeleccionada.id
        }
        binding.verLugaresEntrega.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        binding.verLugaresEntrega.adapter = adapter
    }

    private fun obtnerDatosProducto(idProducto: String, idTrabajador: String) {
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
                    data["cantidad_porcentaje_descuento"] as? Number ?: 0
                val precio = data["precio"] as? Number ?: 0
                val precioDescuento = data["precio_descuento"] as? Number ?: 0
                val totalProducto = data["total_producto"] as? Number ?: 0

                val categoria = data["categoria"] as? String ?: ""
                val condicionProducto = data["condicion_producto"] as? String ?: ""
                val descripcion = data["descripcion"] as? String ?: ""


                val modelo = data["modelo"] as? String ?: ""
                val descuento = data["descuento"] as? Boolean ?: false
                val efectivo = data["efectivo"] as? Boolean ?: false
                val entrega_domicilio = data["entrega_domicilio"] as? Boolean ?: true

                val garantia = data["garantia"] as? String ?: ""
                val id = data["id"] as? String ?: ""
                val fechaPublicada = data["fechaPublicada"] as? String ?: ""
                val metodoEntrega = data["metodoEntrega"] as? String ?: ""


                val lugarDeEntrega = data["lugarEntrega"] as? String ?: ""
                val marca = data["marca"] as? String ?: ""
                val nombre = data["nombre"] as? String ?: ""
                val plin = data["plin"] as? Boolean ?: false
                val stok = data["stok"] as? String ?: ""
                val yape = data["yape"] as? Boolean ?: false
                val cantidad_porcentaje_descuento =
                    data["cantidad_porcentaje_descuento"] as? Number ?: 0
                Log.d(
                    "DatosProducto",
                    "Categoría: $categoria, Marca: $marca, Modelo: $modelo, Stock: $stok, Garantía: $garantia"
                )

                if (descuento) {
                    binding.precioproducto.AntiguoPrecio.isVisible=true
                    binding.precioproducto.descuentoPorcentaje.isVisible=true
                    constantestextos_general.marcarDescuentoTxt(binding.precioproducto.AntiguoPrecio)
                    constantestextos_general.setearPrecioDescuentoPrecioAntiguo(precioDescuento, binding.precioproducto.textoNumero2,precio,binding.precioproducto.AntiguoPrecio,cantidad_porcentaje_descuento,binding.precioproducto.descuentoPorcentaje)
                } else {
                    constantestextos_general.setearPrecioDescuentoPrecioAntiguo(precio,binding.precioproducto.textoNumero2)
                }
                binding.categoriaProductos.textoNumero2.text = categoria
                binding.marcaProducto.textoNumero2.text = marca
                binding.modeloProducto.textoNumero2.text = modelo
                binding.StokDiponible.textoNumero2.text = stok
                binding.garantiaProducto.textoNumero2.text = garantia
                binding.condicionProducto.textoNumero2.text = condicionProducto
                binding.MetodoEntregaProducto.textoNumero2.text = metodoEntrega
                binding.descripcionProducto.text = descripcion
                constantestextos_general.extender_acortar_texto(
                    binding.descripcionProducto,
                    binding.tvReadMore
                )
                constantesCarrito.setearDatosUsuario { nombre, numero, localidad, apellido ->
                    binding.nombresCompletosED.setText("$nombre $apellido")
                    binding.NumeroDeContactoED.setText(numero)
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
            } else {
                Log.e("FirestoreResponse", "Documento no encontrado")
            }
        }.addOnFailureListener { e ->
            Log.e("FirestoreError", "Error obteniendo datos: ", e)
        }

    }
}