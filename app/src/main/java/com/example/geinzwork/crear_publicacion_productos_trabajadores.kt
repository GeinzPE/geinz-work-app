package com.example.geinzwork

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.geinzz.geinzwork.R
import com.geinzz.geinzwork.databinding.ActivityCrearPublicacionProductosTrabajadoresBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class crear_publicacion_productos_trabajadores : AppCompatActivity() {
    private lateinit var binding: ActivityCrearPublicacionProductosTrabajadoresBinding
    private lateinit var firebaseAuth: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCrearPublicacionProductosTrabajadoresBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        firebaseAuth = FirebaseAuth.getInstance()
        binding.publicar.setOnClickListener { crear_publicacion_producto(firebaseAuth.uid.toString()) }
    }

    private fun crear_publicacion_producto(id_trabajador: String) {
        val titulo_producto = binding.tituloPublicacionPrED
        val modelo_producto = binding.modeloProductoED
        val nombre_producto = binding.nombreProductoED
        val stok_producto = binding.stokED
        val condicion_producto = binding.condicionPrED
        val descripcion_producto = binding.descripcionProductoED
        val precioProducto = binding.precioProductoED
        val precio_descuento_nuevo = binding.precioNuevoDescuentoPrED
        val tiempoGarantiaYears = binding.hayGarantiaProductoED
        val marca_producto = binding.marcaProductoED
        val localida_user = binding.agregaUbiED
        val mostra_para = binding.mostrarPublicacionPara
        val lugar_entrega = binding.lugarEntregaED
        val db = FirebaseFirestore.getInstance().collection("Trabajadores_Usuarios_Drivers")
            .document("trabajadores").collection("trabajadores").document(id_trabajador)
            .collection("productos_venta")

        val grupoRadio = binding.grupoRadio
        val grupoEnvioGratis = binding.radioDeliveryGratis
        val yape = binding.yape
        val efectivo = binding.efectivo
        val plin = binding.plin

// Variables booleanas para subir al backend o guardarlas
        var yapeSelected = false
        var efectivoSelected = false
        var plinSelected = false

        var deliveryGratis = false

        when (grupoRadio.checkedRadioButtonId) {
            R.id.yape -> {
                yapeSelected = true
            }

            R.id.efectivo -> {
                efectivoSelected = true
            }

            R.id.plin -> {
                plinSelected = true
            }
        }
        when (grupoEnvioGratis.checkedRadioButtonId) {
            R.id.delivery_gratis_si -> {
                deliveryGratis = true
            }

            R.id.delivery_gratis_no -> {
                deliveryGratis = false
            }
        }
        val radioGroup = binding.metodosEntrega

        val campoLugarEntrega = binding.lugarEntregaED
        val linealDeliverGratis = binding.linealDeliveryGratis

        radioGroup.setOnCheckedChangeListener { _, checkedId ->
            campoLugarEntrega.visibility = if (checkedId == R.id.lugar_entrega) {
                View.VISIBLE
            } else {
                View.GONE
            }

            linealDeliverGratis.visibility = if (checkedId == R.id.delivery) {
                View.VISIBLE
            } else {
                View.GONE
            }
        }


        val isDelivery: Boolean
        val isEntregaDomicilio: Boolean
        val isCoordinarComprador: Boolean

        val metodoEntrega: String

        when (binding.metodosEntrega.checkedRadioButtonId) {
            R.id.delivery -> {
                isDelivery = true
                isEntregaDomicilio = false
                isCoordinarComprador = false
                metodoEntrega = "Delivery"

            }

            R.id.entrega_domicilio -> {
                isDelivery = false
                isEntregaDomicilio = true
                isCoordinarComprador = false
                metodoEntrega = "Entrega a domicilio"
            }

            R.id.coordinar_comprador -> {
                isDelivery = false
                isEntregaDomicilio = false
                isCoordinarComprador = true
                metodoEntrega = "Cordinar con el comprado"

            }

            R.id.lugar_entrega -> {
                isDelivery = false
                isEntregaDomicilio = false
                isCoordinarComprador = false
                metodoEntrega = "Lugar entrega"

            }

            else -> {
                // Nada seleccionado aún
                isDelivery = false
                isEntregaDomicilio = false
                isCoordinarComprador = false
                metodoEntrega = ""
            }
        }
        val descuentoAplicado =
            ((precioProducto.text.toString().toDouble() - precio_descuento_nuevo.text.toString()
                .toDouble()) / precioProducto.text.toString().toDouble()) * 100
        val hasMap = hashMapOf<String, Any>(
            "titulo" to titulo_producto.text.toString(),
            "cantidad_porcentaje_descuento" to descuentoAplicado.toInt(),
            "condicion_producto" to condicion_producto.text.toString(),
            "descripcion" to descripcion_producto.text.toString(),
            "descuento" to binding.siHayDescuento.isChecked,
            "efectivo" to efectivoSelected,
            "entrega_domicilio" to isEntregaDomicilio,
            "fechaPublicada" to "",
            "garantia" to tiempoGarantiaYears.text.toString(),
            "localidadUser" to localida_user.text.toString(),
            "lugarEntrega" to lugar_entrega.text.toString(),
            "marca" to marca_producto.text.toString(),
            "metodoEntrega" to metodoEntrega,
            "envio_gratis" to deliveryGratis,
            "modelo" to modelo_producto.text.toString(),
            "nombre" to nombre_producto.text.toString(),
            "plin" to plinSelected,
            "precio" to precioProducto.text.toString().toDouble(),
            "precioDelivery" to 5,
            "precio_descuento" to precio_descuento_nuevo.text.toString().toDouble(),
            "stok" to stok_producto.text.toString(),
            "yape" to yapeSelected,
            "visivilidad" to mostra_para.text.toString(),
        )
        db.add(hasMap).addOnSuccessListener { res ->
            Toast.makeText(this, "producto agregado correctemente", Toast.LENGTH_SHORT).show()
        }.addOnFailureListener { e ->
            Toast.makeText(this, "Error al agregar el producto", Toast.LENGTH_SHORT).show()
        }
    }

    private fun validarCampos(): Boolean {
        val titulo_producto = binding.tituloPublicacionPrED
        val modelo_producto = binding.modeloProductoED
        val stok_producto = binding.stokED
        val condicion_producto = binding.condicionPrED
        val descripcion_producto = binding.descripcionProductoED
        val precioProducto = binding.precioProductoED
        val precio_descuento_nuevo = binding.precioNuevoDescuentoPrED
        val tiempoGarantiaYears = binding.hayGarantiaProductoED
        val ubicacion = binding.agregaUbiED
        val mostra_para = binding.mostrarPublicacionPara

        var valido = true

        if (titulo_producto.text.toString().isBlank()) {
            titulo_producto.error = "Ingrese el nombre del producto"
            valido = false
        }

        if (modelo_producto.text.toString().isBlank()) {
            modelo_producto.error = "Ingrese el modelo del producto"
            valido = false
        }

        if (stok_producto.text.toString().isBlank()) {
            stok_producto.error = "Ingrese la cantidad de stock disponible"
            valido = false
        }

        if (condicion_producto.text.toString().isBlank()) {
            condicion_producto.error = "Indique la condición del producto (nuevo, usado, etc.)"
            valido = false
        }

        if (descripcion_producto.text.toString().isBlank()) {
            descripcion_producto.error = "Describa brevemente el producto"
            valido = false
        }

        if (precioProducto.text.toString().isBlank()) {
            precioProducto.error = "Ingrese el precio del producto"
            valido = false
        }

        if (precio_descuento_nuevo.text.toString().isBlank()) {
            precio_descuento_nuevo.error = "Ingrese el precio con descuento, si aplica"
            valido = false
        }

        if (tiempoGarantiaYears.text.toString().isBlank()) {
            tiempoGarantiaYears.error = "Indique el tiempo de garantía en años, si aplica"
            valido = false
        }

        if (mostra_para.text.toString().isBlank()) {
            mostra_para.error =
                "Indique para quién está destinada la publicación (todos, seguidores, etc.)"
            valido = false
        }

        return valido

    }


}