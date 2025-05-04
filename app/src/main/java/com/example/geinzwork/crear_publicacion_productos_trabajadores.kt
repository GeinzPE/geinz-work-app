package com.example.geinzwork

import android.content.Context
import android.graphics.Paint
import android.graphics.Typeface
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.Spannable
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.StyleSpan
import android.text.style.UnderlineSpan
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.ArrayAdapter
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.geinzwork.adapterViewholder.anidacion_categorias_productovrprivate
import com.example.geinzwork.dataclass.CategoryWithSubcategories
import com.example.geinzwork.dataclass.MiViewModel
import com.example.geinzwork.dataclass.dataclas_anidacion_productos_vr
import com.example.geinzwork.dataclass.dataclass_texto_descripcion_pr
import com.geinzz.geinzwork.R
import com.geinzz.geinzwork.constantesGeneral.constantesCarrito
import com.geinzz.geinzwork.constantesGeneral.constantesDatosUsuarioTienda
import com.geinzz.geinzwork.databinding.ActivityCrearPublicacionProductosTrabajadoresBinding
import com.geinzz.geinzwork.databinding.BottomSheetCategoriasPrVrBinding
import com.geinzz.geinzwork.databinding.BottomSheetConfiguracionDescripcionPrVrBinding
import com.geinzz.geinzwork.databinding.BottomSheetHastagsFiltradosBinding
import com.geinzz.geinzwork.databinding.BottomSheetPublicacionesParaBinding
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.chip.Chip
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
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
    private val viewModel: MiViewModel by viewModels()
    @RequiresApi(Build.VERSION_CODES.O)
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
        binding.subir.setOnClickListener {
            dialog = BottomSheetDialog(this)
            bottom_shet_Descipcion_cate()
            dialog.show()

        }
        constantesCarrito.obtnerfechaHora(binding.hora,binding.fecha)
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
        val descripcion_producto_titulo = binding.vistraPreviaDescripciontitulo
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
        viewModel.datosDescripcion.observe(this, Observer { datos ->


            val tituloMap = mapOf(
                "titulo_descripcion" to datos.titulo_descripcion,
                "titulo_valor_style" to datos.valor_boldtexto_titulo,
                "titulo_mayus" to datos.minusmayus_titulo
            )
            val texto_map = mapOf(
                "texto_descripcion" to datos.descripcion_texto,
                "texto_valor_style" to datos.valor_boldtexto_texto,
                "texto_mayus" to datos.minusmayus_titulo_texto
            )

            val hasMap = hashMapOf<String, Any>(
                "titulo" to titulo_producto.text.toString(),
                "cantidad_porcentaje_descuento" to descuentoAplicado.toInt(),
                "condicion_producto" to condicion_producto.text.toString(),
                "descuento" to binding.siHayDescuento.isChecked,
                "categoria_producto" to binding.catSelcionado.text.toString(),
                "subcategori_producto" to binding.subcategoriaProducto.text.toString(),
                "efectivo" to efectivo,
                "entrega_domicilio" to isEntregaDomicilio,
                "fechaPublicada" to binding.fecha.text.toString(),
                "horaPublicada" to binding.hora.text.toString(),
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
                "precio_descuento" to (precio_descuento_nuevo.text.toString().toDoubleOrNull()
                    ?: 0.0),
                "stok" to stok_producto.text.toString(),
                "yape" to yape,
                "visivilidad" to mostra_para.text.toString(),
                "descripcion_titulo" to tituloMap,
                "descripcion_texto" to texto_map,
                "descripcion_texto_lista" to datos.listaEncontrados
            )
            if (validarCampos()) {
                db.add(hasMap).addOnSuccessListener { res ->
                    // Obtener el id del documento recién creado
                    val productId = res.id
                    val hasmap = hashMapOf<String, Any>(
                        "id" to productId
                    )
                    db.document(productId).set(hasmap, SetOptions.merge())
                        .addOnSuccessListener { res->
                            println("id subido correcamte")
                        }

                }.addOnFailureListener { e ->
                    Toast.makeText(this, "Error al agregar el producto", Toast.LENGTH_SHORT).show()
                }
            }

        })


    }


    private fun validarCampos(): Boolean {
        val titulo_producto = binding.tituloPublicacionPrED
        val stok_producto = binding.stokED
        val condicion_producto = binding.condicionPrED
//        val descripcion_producto = binding.descripcionProductoED
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

//        if (descripcion_producto.text.toString().isBlank()) {
//            descripcion_producto.error = "Describa brevemente el producto"
//            valido = false
//        }

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


    private fun bottom_shet_Descipcion_cate() {
        val binding_bottom_sheeet =
            BottomSheetConfiguracionDescripcionPrVrBinding.inflate(LayoutInflater.from(this))
        val view = binding_bottom_sheeet.root
        binding_bottom_sheeet.tituloProductoED.addTextChangedListener {
            actualizarTextoFormateado(binding_bottom_sheeet)
        }
        binding_bottom_sheeet.AgregaDescipcionProductoED.addTextChangedListener {
            binding_bottom_sheeet.textoDescripcion.text = it.toString()
            binding_bottom_sheeet.includeAgregarTextosCuales.grupoSubralladoTXT.clearCheck()
            binding_bottom_sheeet.includeAgregarTextosCuales.gurpoMayus.clearCheck()
        }
        binding_bottom_sheeet.colocarBoldAgunasLetrasED.addTextChangedListener {
            actualizarVistaPreviaConNegritas(binding_bottom_sheeet)
        }


        binding_bottom_sheeet.includeAgregarBoldTitulo.bold.setOnCheckedChangeListener { _, _ ->
            actualizarTextoFormateado(
                binding_bottom_sheeet
            )
        }
        binding_bottom_sheeet.includeAgregarBoldTitulo.cursiva.setOnCheckedChangeListener { _, _ ->
            actualizarTextoFormateado(
                binding_bottom_sheeet
            )
        }
        binding_bottom_sheeet.includeAgregarBoldTitulo.subrallado.setOnCheckedChangeListener { _, _ ->
            actualizarTextoFormateado(
                binding_bottom_sheeet
            )
        }
        binding_bottom_sheeet.includeAgregarBoldTitulo.mayuscula.setOnCheckedChangeListener { _, _ ->
            actualizarTextoFormateado(
                binding_bottom_sheeet
            )
        }
        binding_bottom_sheeet.includeAgregarBoldTitulo.minuscula.setOnCheckedChangeListener { _, _ ->
            actualizarTextoFormateado(
                binding_bottom_sheeet
            )
        }

        binding_bottom_sheeet.includeAgregarTextosCuales.bold.setOnCheckedChangeListener { _, _ ->
            actualizarVistaPreviaConNegritas(
                binding_bottom_sheeet
            )
        }
        binding_bottom_sheeet.includeAgregarTextosCuales.cursiva.setOnCheckedChangeListener { _, _ ->
            actualizarVistaPreviaConNegritas(
                binding_bottom_sheeet
            )
        }
        binding_bottom_sheeet.includeAgregarTextosCuales.subrallado.setOnCheckedChangeListener { _, _ ->
            actualizarVistaPreviaConNegritas(
                binding_bottom_sheeet
            )
        }
        binding_bottom_sheeet.includeAgregarTextosCuales.mayuscula.setOnCheckedChangeListener { _, _ ->
            actualizarVistaPreviaConNegritas(
                binding_bottom_sheeet
            )
        }
        binding_bottom_sheeet.includeAgregarTextosCuales.minuscula.setOnCheckedChangeListener { _, _ ->
            actualizarVistaPreviaConNegritas(
                binding_bottom_sheeet
            )
        }
        binding_bottom_sheeet.guardarCambios.setOnClickListener {
            guardar_cabmios_descripcion(binding_bottom_sheeet)
        }
        dialog.setContentView(view)
    }

    private fun actualizarVistaPreviaConNegritas(binding_bottom_sheeet: BottomSheetConfiguracionDescripcionPrVrBinding) {
        var textoOriginal = binding_bottom_sheeet.AgregaDescipcionProductoED.text.toString()
        val partesTexto = binding_bottom_sheeet.colocarBoldAgunasLetrasED.text.toString()
            .split(",")
            .map { it.trim() }

        val spannableBuilder = SpannableStringBuilder(textoOriginal)

        for (parte in partesTexto) {
            if (parte.isEmpty()) continue

            val start = spannableBuilder.indexOf(parte)
            val end = start + parte.length

            if (start != -1) {
                // Aplicar mayúscula o minúscula
                val textoTransformado = when {
                    binding_bottom_sheeet.includeAgregarTextosCuales.mayuscula.isChecked -> parte.uppercase()
                    binding_bottom_sheeet.includeAgregarTextosCuales.minuscula.isChecked -> parte.lowercase()
                    else -> parte
                }

                // Reemplazar el texto encontrado por el texto transformado
                spannableBuilder.replace(start, end, textoTransformado)

                // Aplicar estilos después de reemplazar
                when {
                    binding_bottom_sheeet.includeAgregarTextosCuales.bold.isChecked -> {
                        spannableBuilder.setSpan(
                            StyleSpan(Typeface.BOLD),
                            start,
                            start + textoTransformado.length,
                            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                        )
                    }

                    binding_bottom_sheeet.includeAgregarTextosCuales.cursiva.isChecked -> {
                        spannableBuilder.setSpan(
                            StyleSpan(Typeface.ITALIC),
                            start,
                            start + textoTransformado.length,
                            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                        )
                    }

                    binding_bottom_sheeet.includeAgregarTextosCuales.subrallado.isChecked -> {
                        spannableBuilder.setSpan(
                            UnderlineSpan(),
                            start,
                            start + textoTransformado.length,
                            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                        )
                    }
                }
            }
        }

        binding_bottom_sheeet.textoDescripcion.text = spannableBuilder
    }

    private fun actualizarTextoFormateado(binding_bottom_sheeet: BottomSheetConfiguracionDescripcionPrVrBinding) {
        val texto = binding_bottom_sheeet.tituloProductoED.text.toString()
        val spannable = SpannableString(texto)

        when {
            binding_bottom_sheeet.includeAgregarBoldTitulo.bold.isChecked -> {
                spannable.setSpan(
                    StyleSpan(Typeface.BOLD),
                    0,
                    texto.length,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                )
            }

            binding_bottom_sheeet.includeAgregarBoldTitulo.cursiva.isChecked -> {
                spannable.setSpan(
                    StyleSpan(Typeface.ITALIC),
                    0,
                    texto.length,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                )
            }

            binding_bottom_sheeet.includeAgregarBoldTitulo.subrallado.isChecked -> {
                spannable.setSpan(
                    UnderlineSpan(),
                    0,
                    texto.length,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                )
            }
        }
        // Convertir el texto a mayúsculas o minúsculas, manteniendo los estilos aplicados
        when {
            binding_bottom_sheeet.includeAgregarBoldTitulo.mayuscula.isChecked -> {
                texto.uppercase()
            }

            binding_bottom_sheeet.includeAgregarBoldTitulo.minuscula.isChecked -> {
                texto.lowercase()
            }

            else -> {
                texto // Dejar el texto tal cual si no se selecciona mayúsculas o minúsculas
            }
        }

        binding_bottom_sheeet.previewTextTitulo.text = spannable
        mayus_minus(spannable, binding_bottom_sheeet)
    }

    private fun mayus_minus(
        texto: SpannableString,
        binding_bottom_sheeet: BottomSheetConfiguracionDescripcionPrVrBinding
    ) {
        val textoTransformado = when {
            binding_bottom_sheeet.includeAgregarBoldTitulo.mayuscula.isChecked -> {
                texto.toString().uppercase() // Convertir el texto a mayúsculas
            }

            binding_bottom_sheeet.includeAgregarBoldTitulo.minuscula.isChecked -> {
                texto.toString().lowercase() // Convertir el texto a minúsculas
            }

            else -> {
                texto.toString() // Mantener el texto tal cual si no se selecciona mayúsculas o minúsculas
            }
        }

        // Crear un nuevo SpannableString con el texto transformado
        val textoFinal = SpannableString(textoTransformado).apply {
            // Reaplicar los estilos al nuevo texto transformado si es necesario
            if (binding_bottom_sheeet.includeAgregarBoldTitulo.bold.isChecked) {
                setSpan(
                    StyleSpan(Typeface.BOLD),
                    0,
                    textoTransformado.length,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                )
            }
            if (binding_bottom_sheeet.includeAgregarBoldTitulo.cursiva.isChecked) {
                setSpan(
                    StyleSpan(Typeface.ITALIC),
                    0,
                    textoTransformado.length,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                )
            }
            if (binding_bottom_sheeet.includeAgregarBoldTitulo.subrallado.isChecked) {
                setSpan(
                    UnderlineSpan(),
                    0,
                    textoTransformado.length,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                )
            }
        }

        // Actualizar la vista previa con el texto final
        binding_bottom_sheeet.previewTextTitulo.text = textoFinal
    }


    private fun guardar_cabmios_descripcion(binding_bottom_sheeet: BottomSheetConfiguracionDescripcionPrVrBinding) {
        val radioPadre_bold_cursiva =
            binding_bottom_sheeet.includeAgregarBoldTitulo.grupoSubralladoTXT
        val radio_padre_mayus_minus = binding_bottom_sheeet.includeAgregarBoldTitulo.gurpoMayus
        val titlo_descripcion = binding_bottom_sheeet.tituloProductoED.text.toString()
        val descripcion_desc = binding_bottom_sheeet.AgregaDescipcionProductoED.text.toString()

        val idSeleccionado = radioPadre_bold_cursiva.checkedRadioButtonId
        val id_mayus_minus = radio_padre_mayus_minus.checkedRadioButtonId

        val radio_padre_bold_cursiva_texto_des =
            binding_bottom_sheeet.includeAgregarTextosCuales.grupoSubralladoTXT
        val radio_padre_mayus_minus_texto_des =
            binding_bottom_sheeet.includeAgregarTextosCuales.gurpoMayus
        val idSeleccionado_des = radio_padre_bold_cursiva_texto_des.checkedRadioButtonId
        val id_mayus_minus_des = radio_padre_mayus_minus_texto_des.checkedRadioButtonId


        val valorSeleccionadoTitulo = when (idSeleccionado) {
            R.id.bold -> "Bold"
            R.id.cursiva -> "Cursiva"
            R.id.subrallado -> "Subrayado"
            else -> "" // Por si no seleccionó nada
        }

        val valorMayusMinusTitulo = when (id_mayus_minus) {
            R.id.mayuscula -> "mayuscula"
            R.id.minuscula -> "minuscula"
            else -> "" // Por si no seleccionó nada
        }


        val valorSeleccionadoDes = when (idSeleccionado_des) {
            R.id.bold -> "Bold"
            R.id.cursiva -> "Cursiva"
            R.id.subrallado -> "Subrayado"
            else -> "" // Por si no seleccionó nada
        }

        val valorMayusMinusDes = when (id_mayus_minus_des) {
            R.id.mayuscula -> "mayuscula"
            R.id.minuscula -> "minuscula"
            else -> "" // Por si no seleccionó nada
        }

        val texto = binding_bottom_sheeet.colocarBoldAgunasLetrasED.text.toString().trim()

        val listaFrases = texto
            .split(",") // separamos usando coma
            .map { it.trim() } // quitamos espacios alrededor
            .filter { it.isNotEmpty() } // evitamos frases vacías si las hay

        Log.d("listapalabra", listaFrases.toString())
        Log.d(
            "valores_encontrados",
            "$titlo_descripcion,$valorSeleccionadoTitulo,$valorMayusMinusTitulo,$descripcion_desc,$valorSeleccionadoDes,$valorMayusMinusDes,$listaFrases"
        )


        editar_setar_valores_campos(
            titlo_descripcion,
            valorSeleccionadoTitulo,
            valorMayusMinusTitulo,
            descripcion_desc,
            valorSeleccionadoDes,
            valorMayusMinusDes,
            listaFrases
        )
        val datos = dataclass_texto_descripcion_pr(
            titulo_descripcion = titlo_descripcion,
            valor_boldtexto_titulo = valorSeleccionadoTitulo,
            minusmayus_titulo = valorMayusMinusTitulo,
            descripcion_texto = descripcion_desc,
            valor_boldtexto_texto = valorSeleccionadoDes,
            minusmayus_titulo_texto = valorMayusMinusDes,
            listaEncontrados = listaFrases
        )

        viewModel.datosDescripcion.value = datos

        dialog.dismiss()
    }


    private fun editar_setar_valores_campos(
        titulo: String,
        fuenteTextoTitulo: String,
        mayus_minus: String,
        texto_des: String,
        fuenteTextoTitulo_des: String,
        mayus_minus_des: String,
        listaFrases: List<String>
    ) {
        val tituloFormateado = when (mayus_minus) {
            "mayuscula" -> titulo.uppercase()
            "minuscula" -> titulo.lowercase()
            else -> titulo
        }
        binding.vistraPreviaDescripciontitulo.text = tituloFormateado
        when (fuenteTextoTitulo) {
            "Bold" -> {
                binding.vistraPreviaDescripciontitulo.setTypeface(null, Typeface.BOLD)
                binding.vistraPreviaDescripciontitulo.paintFlags =
                    binding.vistraPreviaDescripciontitulo.paintFlags and Paint.UNDERLINE_TEXT_FLAG.inv()
            }

            "Cursiva" -> {
                binding.vistraPreviaDescripciontitulo.setTypeface(null, Typeface.ITALIC)
                binding.vistraPreviaDescripciontitulo.paintFlags =
                    binding.vistraPreviaDescripciontitulo.paintFlags and Paint.UNDERLINE_TEXT_FLAG.inv()
            }

            "Subrayado" -> {
                binding.vistraPreviaDescripciontitulo.setTypeface(null, Typeface.NORMAL)
                binding.vistraPreviaDescripciontitulo.paintFlags =
                    binding.vistraPreviaDescripciontitulo.paintFlags or Paint.UNDERLINE_TEXT_FLAG
            }

            else -> {
                binding.vistraPreviaDescripciontitulo.setTypeface(null, Typeface.NORMAL)
                binding.vistraPreviaDescripciontitulo.paintFlags =
                    binding.vistraPreviaDescripciontitulo.paintFlags and Paint.UNDERLINE_TEXT_FLAG.inv()
            }
        }


        val spannable = SpannableStringBuilder(texto_des)

        for (fraseOriginal in listaFrases) {
            val textoOriginalLower = texto_des.lowercase()
            val fraseLower = fraseOriginal.lowercase()

            var startIndex = textoOriginalLower.indexOf(fraseLower)

            while (startIndex != -1) {
                val endIndex = startIndex + fraseLower.length

                // Aplicar estilo (Bold, Cursiva, Subrayado)
                when (fuenteTextoTitulo_des) {
                    "Bold" -> spannable.setSpan(
                        StyleSpan(Typeface.BOLD),
                        startIndex,
                        endIndex,
                        Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                    )

                    "Cursiva" -> spannable.setSpan(
                        StyleSpan(Typeface.ITALIC),
                        startIndex,
                        endIndex,
                        Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                    )

                    "Subrayado" -> spannable.setSpan(
                        UnderlineSpan(),
                        startIndex,
                        endIndex,
                        Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                    )
                }

                // Reemplazar visualmente la frase con mayúscula o minúscula (sin alterar el resto del texto)
                val nuevaFrase = when (mayus_minus_des) {
                    "mayuscula" -> texto_des.substring(startIndex, endIndex).uppercase()
                    "minuscula" -> texto_des.substring(startIndex, endIndex).lowercase()
                    else -> texto_des.substring(startIndex, endIndex)
                }

                spannable.replace(startIndex, endIndex, nuevaFrase)

                // Buscar siguiente ocurrencia
                val nuevoTexto = spannable.toString().lowercase()
                startIndex = nuevoTexto.indexOf(fraseLower, startIndex + nuevaFrase.length)
            }
        }

        binding.vistraPreviaDescripcion.text = spannable

    }

}