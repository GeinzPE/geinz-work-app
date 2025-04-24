package com.example.geinzwork

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import com.geinzz.geinzwork.R
import com.geinzz.geinzwork.constantesGeneral.constantesCarrito
import com.geinzz.geinzwork.constantesGeneral.constantesDatosUsuarioTienda
import com.geinzz.geinzwork.databinding.ActivityCrearPublicacionProductosTrabajadoresBinding
import com.geinzz.geinzwork.databinding.BottomSheetPublicacionesParaBinding
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlin.math.roundToInt

class crear_publicacion_productos_trabajadores : AppCompatActivity() {
    private lateinit var binding: ActivityCrearPublicacionProductosTrabajadoresBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var dialog: BottomSheetDialog

    private var yape:Boolean=false
    private var plin:Boolean=false
    private var descuento:Boolean=false

    private var efectivo:Boolean=false
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
        val radioGroup = binding.metodosEntrega
        val campoLugarEntrega = binding.lugarEntregaTXT
        val linealDeliveryGratis = binding.linealDeliveryGratis
        val radioDeliveryGratis =
            binding.radioDeliveryGratis // <- Asegúrate que está en tu ViewBinding
        obtener_estados_productos()
        radioGroup.setOnCheckedChangeListener { _, checkedId ->

            campoLugarEntrega.visibility = if (checkedId == R.id.lugar_entrega) {
                View.VISIBLE
            } else {
                View.GONE
            }

            if (checkedId == R.id.delivery) {
                linealDeliveryGratis.visibility = View.VISIBLE
            } else {
                linealDeliveryGratis.visibility = View.GONE
                radioDeliveryGratis.clearCheck() // <-- Limpiamos la selección de "sí" o "no"
            }
        }
        binding.mostrarPublicacionPara.setOnClickListener {
            dialog = BottomSheetDialog(this)
            mostrar_dialog_para(binding.mostrarPublicacionPara.text.toString()) { selt ->
                binding.mostrarPublicacionPara.text = selt
            }
            dialog.show()
        }

        binding.siHayDescuento.setOnCheckedChangeListener { _, isChecked ->
            binding.precioNuevoDescuentoPr.visibility = if (isChecked) {
                descuento=true
                binding.precioNuevoDescuentoPrED.setText("0")
                View.VISIBLE
            } else {
                descuento=false
                View.GONE
            }
        }
        binding.siHayGarantia.setOnCheckedChangeListener { _, isChecked ->
            binding.hayGarantiaProducto.visibility = if (isChecked) {
                binding.hayGarantiaProductoED.setText("")
                View.VISIBLE

            } else {
                View.GONE
            }
        }

        binding.agregaUbicaciones.setOnCheckedChangeListener { _, isChecked ->
            binding.selecionLocalidad.visibility = if (isChecked) {
                View.VISIBLE
            } else {
                View.GONE
            }
        }
        constantesDatosUsuarioTienda.obtnerLocalidades(binding.agregaUbiED)
        val yapeCheckBox = binding.yape
        val efectivoCheckBox = binding.efectivo
        val plinCheckBox = binding.plin

        yapeCheckBox.setOnCheckedChangeListener { _, isChecked ->
            yape = isChecked
        }
        efectivoCheckBox.setOnCheckedChangeListener { _, isChecked ->
            efectivo = isChecked
        }
        plinCheckBox.setOnCheckedChangeListener { _, isChecked ->
            plin = isChecked
        }


    }

    private fun mostrar_dialog_para(
        selecionado: String,
        select: (String) -> Unit
    ) {
        val bindig_BottomSheet_dialog_para =
            BottomSheetPublicacionesParaBinding.inflate(LayoutInflater.from(this))
        val view = bindig_BottomSheet_dialog_para.root

        val radioGroup = bindig_BottomSheet_dialog_para.RadioGrupCaracterisiticas
        // Preseleccionar opción desde código
        when (selecionado.lowercase()) {
            "todos" -> radioGroup.check(R.id.todos)
            "seguidores" -> radioGroup.check(R.id.seguidores)
            "privado" -> radioGroup.check(R.id.privado)
        }

        // Detectar selección
        radioGroup.setOnCheckedChangeListener { _, checkedId ->
            val seleccion = when (checkedId) {
                R.id.todos -> "Todos"
                R.id.seguidores -> "Seguidores"
                R.id.privado -> "Privado"
                else -> ""
            }

            if (seleccion.isNotEmpty()) {
                select(seleccion)
                dialog.dismiss()
            }
        }

        // Cerrar diálogo manualmente
        bindig_BottomSheet_dialog_para.cerrar.setOnClickListener {
            dialog.dismiss()
        }

        dialog.setContentView(view)
        dialog.show()
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

        val grupoEnvioGratis = binding.radioDeliveryGratis


// Variables booleanas para subir al backend o guardarlas
        var deliveryGratis = false


        when (grupoEnvioGratis.checkedRadioButtonId) {
            R.id.delivery_gratis_si -> {
                deliveryGratis = true
            }

            R.id.delivery_gratis_no -> {
                deliveryGratis = false
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
        val descuentoAplicado = if (descuento) {
            val descuentoCalculado = ((precioProducto.text.toString().toDouble() - precio_descuento_nuevo.text.toString().toDouble()) / precioProducto.text.toString().toDouble()) * 100
            descuentoCalculado.roundToInt() // Redondeamos el valor a un Int
        } else {
            0
        }

        val hasMap = hashMapOf<String, Any>(
            "titulo" to titulo_producto.text.toString(),
            "cantidad_porcentaje_descuento" to descuentoAplicado.toInt(),
            "condicion_producto" to condicion_producto.text.toString(),
            "descripcion" to descripcion_producto.text.toString(),
            "descuento" to binding.siHayDescuento.isChecked,
            "efectivo" to efectivo,
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
            "plin" to plin,
            "precio" to precioProducto.text.toString().toDouble(),
            "precioDelivery" to 5,
            "precio_descuento" to precio_descuento_nuevo.text.toString().toDouble(),
            "stok" to stok_producto.text.toString(),
            "yape" to yape,
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

    private fun obtener_estados_productos() {
        val db =
            FirebaseFirestore.getInstance().collection("estados_condiciones_productos_generales")
                .document("estados")
        db.get().addOnSuccessListener { res->
            if (res.exists()){
                val data=res.data
                val condicionesList = data?.get("estados") as? List<String> ?: emptyList()

                val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, condicionesList)
                binding.condicionPrED.setAdapter(adapter)
            }
        }
    }


}