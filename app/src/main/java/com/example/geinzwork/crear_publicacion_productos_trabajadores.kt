package com.example.geinzwork

import android.content.Context
import android.graphics.Typeface
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.TextWatcher
import android.text.style.StyleSpan
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.ArrayAdapter
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.text.HtmlCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.geinzwork.adapterViewholder.anidacion_categorias_productovrprivate
import com.example.geinzwork.dataclass.CategoryWithSubcategories
import com.example.geinzwork.dataclass.dataclas_anidacion_productos_vr
import com.geinzz.geinzwork.R
import com.geinzz.geinzwork.constantesGeneral.constantesCarrito
import com.geinzz.geinzwork.constantesGeneral.constantesDatosUsuarioTienda
import com.geinzz.geinzwork.databinding.ActivityCrearPublicacionProductosTrabajadoresBinding
import com.geinzz.geinzwork.databinding.BottomSheetCategoriasPrVrBinding
import com.geinzz.geinzwork.databinding.BottomSheetHastagsFiltradosBinding
import com.geinzz.geinzwork.databinding.BottomSheetPublicacionesParaBinding
import com.geinzz.geinzwork.databinding.ItemCategoriaVrBinding
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.chip.Chip
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlin.math.roundToInt

class crear_publicacion_productos_trabajadores : AppCompatActivity() {
    private lateinit var binding: ActivityCrearPublicacionProductosTrabajadoresBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var dialog: BottomSheetDialog
    private lateinit var categoryAdapter: anidacion_categorias_productovrprivate
    private val hashtagsGenerales = mutableListOf<String>()
    private var yape: Boolean = false
    private var plin: Boolean = false
    private var descuento: Boolean = false
    private var efectivo: Boolean = false
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
        binding.subcategoriaProducto.setOnClickListener {
            dialog = BottomSheetDialog(this)
            agregarCategorias()
            dialog.show()
        }
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
                descuento = true
                binding.precioNuevoDescuentoPrED.setText("0")
                View.VISIBLE
            } else {
                descuento = false
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
        binding.agregarHastagsED.setOnClickListener {
            dialog = BottomSheetDialog(this)
            obtener_hastags_generales(this, hashtagsGenerales, dialog)
            dialog.show()
        }

        binding.agregaUbicaciones.setOnCheckedChangeListener { _, isChecked ->
            binding.selecionLocalidad.visibility = if (isChecked) {
                View.VISIBLE
            } else {
                binding.agregaUbiED.setText("")
                View.GONE
            }
        }

        constantesDatosUsuarioTienda.obtnerLocalidades(binding.agregaUbiED)
        constantesCarrito.setearDatosUsuario { nombre, numero, localid, apellido ->
            binding.agregaUbiED.setText(localid)
            constantesDatosUsuarioTienda.obtnerLocalidades(binding.agregaUbiED)

        }
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

        var editing = false // <- esto debe estar fuera para que no se reinicie siempre

        binding.descripcionProductoED.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                if (s.isNullOrEmpty()) return

                aplicarFormatoWhatsapp(s)
            }
        })

    }

    fun aplicarFormatoWhatsapp(editable: Editable) {
        val boldPattern = Regex("\\*(.*?)\\*")
        val matches = boldPattern.findAll(editable.toString())

        // Limpiar spans antiguos
        val spans = editable.getSpans(0, editable.length, StyleSpan::class.java)
        for (span in spans) {
            editable.removeSpan(span)
        }

        // Aplicar negrita a coincidencias
        for (match in matches) {
            val start = match.range.first
            val end = match.range.last + 1

            editable.setSpan(
                StyleSpan(Typeface.BOLD),
                start + 1,
                end - 1,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }
    }

    fun obtener_hastags_generales(
        contex: Context,
        hashtagsGenerales: MutableList<String>,
        dialog: BottomSheetDialog
    ) {
        val bindig_BottomSheet =
            BottomSheetHastagsFiltradosBinding.inflate(LayoutInflater.from(contex))
        val view = bindig_BottomSheet.root
        bindig_BottomSheet.cerrar.setOnClickListener { dialog.dismiss() }

        val tiempoInicio = System.currentTimeMillis()

        val db = FirebaseFirestore.getInstance().collection("hastags_generales")
            .document("hashtags_productos")

        val chipGroup = bindig_BottomSheet.chipGrupHastagsP

        db.get().addOnSuccessListener { documentSnapshot ->
            val tiempoFin = System.currentTimeMillis()
            val tiempoTotal = tiempoFin - tiempoInicio
            Toast.makeText(
                this,
                "Hashtags generales cargados en $tiempoTotal ms",
                Toast.LENGTH_SHORT
            ).show()

            if (documentSnapshot.exists()) {
                val hashtags = documentSnapshot.get("hashtags_productos_array") as? List<String>
                if (hashtags != null) {
                    chipGroup.removeAllViews()
                    hashtagsGenerales.clear()

                    for (hashtag in hashtags) {
                        val chip = Chip(this).apply {
                            text = hashtag
                            isCheckable = true
                            isClickable = true

                            setOnCheckedChangeListener { chipView, isChecked ->
                                if (isChecked) {
                                    if (hashtagsGenerales.size >= 5) {
                                        chipView.isChecked = false // desmarcar el chip
                                        Toast.makeText(
                                            this@crear_publicacion_productos_trabajadores,
                                            "Solo puedes seleccionar hasta 5 hashtags",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    } else {
                                        hashtagsGenerales.add(text.toString())
                                    }
                                } else {
                                    hashtagsGenerales.remove(text.toString())
                                }

                                Log.d("HashtagsSeleccionados", hashtagsGenerales.toString())
                            }
                        }
                        chipGroup.addView(chip)
                    }

                } else {
                    Log.e("Firestore", "El campo no es un array o está vacío.")
                }
            } else {
                Log.e("Firestore", "El documento no existe.")
            }
        }.addOnFailureListener { exception ->
            Log.e("Firestore", "Error al obtener documento: ${exception.message}")
        }

        // Botón para confirmar y agregar hashtags al EditText
        bindig_BottomSheet.agregarCampos.setOnClickListener {
            agregarhastags_generales_editext(hashtagsGenerales, binding.agregarHastagsED)
            dialog.dismiss()
        }

        dialog.setContentView(view)
    }

    private fun agregarhastags_generales_editext(
        hashtagsGenerales: MutableList<String>,
        textView: TextView
    ) {
        val hashtagsTexto = hashtagsGenerales.joinToString(separator = ", ") { "$it" }
        textView.setText(hashtagsTexto)
    }

    private fun agregarCategorias() {
        val bindingBottomSheetCategorias =
            BottomSheetCategoriasPrVrBinding.inflate(LayoutInflater.from(this))
        bindingBottomSheetCategorias.cerrar.setOnClickListener { dialog.dismiss() }
        val view = bindingBottomSheetCategorias.root

        val startTime = System.currentTimeMillis() // Marca de tiempo antes de la consulta

        val db = FirebaseFirestore.getInstance().collection("categoria_productos")
        db.get()
            .addOnSuccessListener { querySnapshot ->
                val categoriesWithSubcategoriesList = mutableListOf<CategoryWithSubcategories>()
                for (document in querySnapshot) {
                    val categoryName = document.id
                    val subcategories =
                        document.get("subcategorias") as? List<String> ?: emptyList()
                    val category = dataclas_anidacion_productos_vr(categoryName, subcategories)
                    categoriesWithSubcategoriesList.add(
                        CategoryWithSubcategories(category) { subcategory ->
                            handleSubcategoryClick(categoryName, subcategory)
                            dialog.dismiss() // O cualquier otra acción al hacer clic
                        }
                    )
                }
                val endTime =
                    System.currentTimeMillis() // Marca de tiempo después de procesar los documentos
                val elapsedTime = endTime - startTime
                val handler = Handler(Looper.getMainLooper())
                handler.postDelayed({
                    bindingBottomSheetCategorias.progrssCarga.isVisible = false
                    bindingBottomSheetCategorias.vistaCategoria.isVisible = true
                    // Ahora tienes la lista 'categoriesWithSubcategoriesList' lista para pasar a tu CategoryAdapter
                }, elapsedTime)
                setupCategoryRecyclerView(
                    bindingBottomSheetCategorias.idItemCategoriaVr.rvSubcategories,
                    categoriesWithSubcategoriesList
                )


            }
            .addOnFailureListener { e ->
                // Manejar el error al obtener los datos
                println("Error al obtener categorías: $e")
            }
        dialog.setContentView(view)
    }

    private fun setupCategoryRecyclerView(
        recyclerView: RecyclerView,
        categoriesWithSubcategoriesList: List<CategoryWithSubcategories>
    ) {
        val rvCategorias =
            recyclerView // Asegúrate de tener un RecyclerView con este ID en tu layout
        rvCategorias.layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)
        categoryAdapter =
            anidacion_categorias_productovrprivate(categoriesWithSubcategoriesList) { categoria, subcategoria ->
                val categoriasSinMarcaNiModelo = listOf(
                    "Juguetes y juegos",
                    "Arte y antigüedades",
                    "Hobbies y actividades",
                    "Ropa, calzado y accesorios",
                    "Muebles",
                    "Hogar y jardín",
                    "Construcción y materiales"
                )
                val mostrarCamposMarca = categoria !in categoriasSinMarcaNiModelo
                binding.layoutNombreMarca.isVisible = mostrarCamposMarca

                if (!mostrarCamposMarca) {
                    binding.marcaProductoED.setText("")
                    binding.modeloProductoED.setText("")
                }
                binding.subcategoriaProducto.setText(subcategoria)
                binding.catSelcionado.text = categoria

            }
        rvCategorias.adapter = categoryAdapter
    }

    private fun handleSubcategoryClick(categoryName: String, subcategoryName: String) {
        // Aquí implementa la lógica cuando se hace clic en una subcategoría
        println("Clic en la subcategoría '$subcategoryName' de la categoría '$categoryName'")
        // Por ejemplo, podrías filtrar una lista de productos y actualizar otra RecyclerView.
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
            val descuentoCalculado =
                ((precioProducto.text.toString().toDouble() - precio_descuento_nuevo.text.toString()
                    .toDouble()) / precioProducto.text.toString().toDouble()) * 100
            descuentoCalculado.roundToInt() // Redondeamos el valor a un Int
        } else {
            0
        }
        val hashtagsGenerales = binding.agregarHastagsED.text.toString()
            .split(",")
            .map { it.trim() }
            .filter { it.isNotBlank() }

        val hasMap = hashMapOf<String, Any>(
            "titulo" to titulo_producto.text.toString(),
            "cantidad_porcentaje_descuento" to descuentoAplicado.toInt(),
            "condicion_producto" to condicion_producto.text.toString(),
            "descripcion" to descripcion_producto.text.toString(),
            "descuento" to binding.siHayDescuento.isChecked,
            "categoria_producto" to binding.catSelcionado.text.toString(),
            "subcategori_producto" to binding.subcategoriaProducto.text.toString(),
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
            "hashtags_generales" to hashtagsGenerales,
            "nombre" to nombre_producto.text.toString(),
            "plin" to plin,
            "precio" to (precioProducto.text.toString().toDoubleOrNull() ?: 0.0),
            "precioDelivery" to 5,
            "precio_descuento" to (precio_descuento_nuevo.text.toString().toDoubleOrNull() ?: 0.0),
            "stok" to stok_producto.text.toString(),
            "yape" to yape,
            "visivilidad" to mostra_para.text.toString(),
        )
        if (validarCampos()) {
            db.add(hasMap).addOnSuccessListener { res ->
                Toast.makeText(this, "producto agregado correctemente", Toast.LENGTH_SHORT).show()
            }.addOnFailureListener { e ->
                Toast.makeText(this, "Error al agregar el producto", Toast.LENGTH_SHORT).show()
            }
        }
    }


    private fun validarCampos(): Boolean {
        val titulo_producto = binding.tituloPublicacionPrED
        val stok_producto = binding.stokED
        val condicion_producto = binding.condicionPrED
        val descripcion_producto = binding.descripcionProductoED
        val precioProducto = binding.precioProductoED
        val precio_descuento_nuevo = binding.precioNuevoDescuentoPrED
        val tiempoGarantiaYears = binding.hayGarantiaProductoED
        val hastags = binding.agregarHastagsED
        val mostra_para = binding.mostrarPublicacionPara
        val subcategoria_producto = binding.subcategoriaProducto

        var valido = true

        if (titulo_producto.text.toString().isBlank()) {
            titulo_producto.error = "Ingrese el nombre del producto"
            valido = false
        }


        if (subcategoria_producto.text.toString().isBlank()) {
            titulo_producto.error = "Selecciona una categoría de producto"
            valido = false
        }
        if (hastags.text.toString().isBlank()) {
            titulo_producto.error = "Ingrese # "
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
        db.get().addOnSuccessListener { res ->
            if (res.exists()) {
                val data = res.data
                val condicionesList = data?.get("estados") as? List<String> ?: emptyList()
                val adapter =
                    ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, condicionesList)
                binding.condicionPrED.setAdapter(adapter)
            }
        }
    }


//    private fun agregarCategorias() {
//        val textoCategorias = binding.hastagsProductosED.text.toString()  // Obtener el texto del EditText
//        val subcategorias = textoCategorias.split(",")  // Dividir el texto en un array usando la coma como separador
//
//        // Apuntar al documento "Hobbies_y_actividades" dentro de la colección "categoria_productos"
//        val db = FirebaseFirestore.getInstance().collection("categoria_productos").document("Relojes y accesorios")
//
//        // Crear un HashMap para almacenar el array de subcategorías
//        val hasMap = hashMapOf<String, Any>(
//            "subcategorias" to subcategorias  // Asignar el array de subcategorías al campo "subcategorias"
//        )
//
//        // Guardar el documento en Firestore
//        db.set(hasMap).addOnSuccessListener {
//            Log.d("Firestore", "Documento agregado exitosamente")
//        }.addOnFailureListener { e ->
//            Log.w("Firestore", "Error al agregar documento", e)
//        }
//    }


}