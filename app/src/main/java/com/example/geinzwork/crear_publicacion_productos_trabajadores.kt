package com.example.geinzwork

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.Spannable
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.TextWatcher
import android.text.style.StyleSpan
import android.text.style.UnderlineSpan
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.core.widget.doOnTextChanged
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.geinzwork.adapterViewholder.adapter_metodos_entrega
import com.example.geinzwork.adapterViewholder.anidacion_categorias_productovrprivate
import com.example.geinzwork.constantesGeneral.constantes_bottom_shet_trabaja.handler
import com.example.geinzwork.constantesGeneral.constantes_metodo_pago_entrega
import com.example.geinzwork.constantesGeneral.constantes_metodo_pago_entrega.verificar_Estado_metodo_pago
import com.example.geinzwork.constantesGeneral.constantes_productos_publicados
import com.example.geinzwork.dataclass.CategoryWithSubcategories
import com.example.geinzwork.dataclass.MiViewModel
import com.example.geinzwork.dataclass.dataclas_anidacion_productos_vr
import com.example.geinzwork.dataclass.dataclass_metodos_entrega
import com.example.geinzwork.dataclass.dataclass_texto_descripcion_pr
import com.example.geinzwork.vistaTrabajador.ver_productos_publicados

import com.geinzz.geinzwork.R
import com.geinzz.geinzwork.constantesGeneral.constantesCarrito
import com.geinzz.geinzwork.constantesGeneral.constantesDatosUsuarioTienda
import com.geinzz.geinzwork.constantesGeneral.mostrarFechaDialog_horaDialog
import com.geinzz.geinzwork.databinding.ActivityCrearPublicacionProductosTrabajadoresBinding
import com.geinzz.geinzwork.databinding.BottomSheeetMetodoEntregaBinding
import com.geinzz.geinzwork.databinding.BottomSheetAplarReporteBinding
import com.geinzz.geinzwork.databinding.BottomSheetCategoriasPrVrBinding
import com.geinzz.geinzwork.databinding.BottomSheetConfiguracionDescripcionPrVrBinding
import com.geinzz.geinzwork.databinding.BottomSheetHastagsFiltradosBinding
import com.geinzz.geinzwork.databinding.BottomSheetPublicacionesParaBinding
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.chip.Chip
import com.google.android.material.imageview.ShapeableImageView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.storage.FirebaseStorage
import java.io.ByteArrayOutputStream
import kotlin.math.roundToInt

class crear_publicacion_productos_trabajadores : AppCompatActivity() {
    private lateinit var binding: ActivityCrearPublicacionProductosTrabajadoresBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var dialog: BottomSheetDialog
    private val lista_entrega = mutableListOf<dataclass_metodos_entrega>()
    private lateinit var categoryAdapter: anidacion_categorias_productovrprivate
    private val hashtagsGenerales = mutableListOf<String>()
    private lateinit var datosDescripcionGlobal: dataclass_texto_descripcion_pr
    private var yape: Boolean = false
    private var plin: Boolean = false
    private var unidadGarantia: String = ""
    private var descuento: Boolean = false
    private var efectivo: Boolean = false
    private var img1_uir1: Uri? = null
    private var trasnferecnia: Boolean = false
    private var deliver_gratis: Boolean = false

    private val imageViews by lazy {
        listOf(binding.img1, binding.img2, binding.img3, binding.img4, binding.img5)
    }
    private var currentImageIndex = 0

    private val viewModel: MiViewModel by viewModels()
    private val img1Launcher =
        registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
            if (uri != null) {
                img1_uir1 = uri
                Toast.makeText(this, "Imagen seleccionada: $uri", Toast.LENGTH_SHORT).show()

                // ✅ Refrescar imagen en el BottomSheet si está visible
                binding?.imgSubir?.setImageURI(uri)

            } else {
                Toast.makeText(this, getString(R.string.ImgNoSeleccionada), Toast.LENGTH_SHORT)
                    .show()
            }
        }
    private val pickImage =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let {
                // Establece la imagen seleccionada
                imageViews[currentImageIndex].setImageURI(it)

                // Si hay otra ImageView disponible, la hacemos visible
                if (currentImageIndex + 1 < imageViews.size) {
                    imageViews[currentImageIndex + 1].visibility = View.VISIBLE
                }

                // Hacer scroll automático al final
                binding.horizontalScrollView.post {
                    binding.horizontalScrollView.fullScroll(View.FOCUS_RIGHT)
                }
            }
        }


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
        binding.popup.setOnClickListener {
            popup()
        }
        img1_uir1?.let {
            binding.imgSubir.setImageURI(it)
        }

        // ✅ Al hacer clic, lanzar el picker de imagen
        binding.imgSubir.setOnClickListener {
            img1Launcher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }
        firebaseAuth = FirebaseAuth.getInstance()
        binding.subcategoriaProducto.setOnClickListener {
            dialog = BottomSheetDialog(this)
            constantes_productos_publicados.agregarCategorias(
                dialog,
                this,
                binding.layoutNombreMarca,
                binding.marcaProductoED,
                binding.modeloProductoED,
                binding.subcategoriaProducto,
                binding.catSelcionado
            )
            dialog.show()
        }
        binding.subir.setOnClickListener {
            dialog = BottomSheetDialog(this)
            mostrarBottomSheetDescripcion(mostrarVistaPrevia = true)
            dialog.show()

        }
        binding.camposEditar.setOnClickListener {
            dialog = BottomSheetDialog(this)
            mostrarBottomSheetDescripcion(
                datosDescripcionGlobal.titulo_descripcion,
                datosDescripcionGlobal.descripcion_texto,
                datosDescripcionGlobal.valor_boldtexto_titulo,
                datosDescripcionGlobal.minusmayus_titulo, // <-- ¡CORREGIDO! Este es el valor de mayúsculas/minúsculas para el TÍTULO
                datosDescripcionGlobal.valor_boldtexto_texto,
                datosDescripcionGlobal.minusmayus_titulo_texto, // <-- ¡CORREGIDO! Este es el valor de mayúsculas/minúsculas para la DESCRIPCIÓN
                datosDescripcionGlobal.listaEncontrados,
                mostrarVistaPrevia = false
            )
            dialog.show()
        }

        binding.radioGrupPlazoRG.setOnCheckedChangeListener { _, checkedId ->
            unidadGarantia = when (checkedId) {
                R.id.meses -> {
                    "mes"
                }

                R.id.years -> {
                    "año"
                }

                R.id.dias -> {
                    "día"
                }

                else -> ""
            }
        }

        binding.horizontalScrollView.post {
            binding.horizontalScrollView.fullScroll(View.FOCUS_RIGHT)
        }
        imageViews.forEach { it.visibility = View.GONE }

        imageViews[0].visibility = View.VISIBLE

        // Configurar los clics para seleccionar imágenes
        imageViews.forEachIndexed { index, imageView ->
            imageView.setOnClickListener {
                currentImageIndex = index
                pickImage.launch("image/*")
            }
        }

        binding.publicar.setOnClickListener {
            val placeholderCaracteristica =
                ContextCompat.getDrawable(this, R.drawable.agregar_imagen_cuadrado)

            if (!validar_campos()) {
                Toast.makeText(
                    this,
                    "Por favor, completa todos los campos obligatorios",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            val imagenesValidas = obtenerImagenesValidas()
            if (imagenesValidas.isEmpty()) {
                Toast.makeText(
                    this,
                    "Debe agregar al menos una imagen del producto",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            val imgCaracteristica = binding.imgSubir.drawable
            if (imgCaracteristica == null || imgCaracteristica.constantState == placeholderCaracteristica?.constantState) {
                Toast.makeText(
                    this,
                    "Debe agregar una imagen a tus características",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            // Si todo está bien, se publica el producto
            publicar_producto()
        }

        constantes_productos_publicados.obtener_estados_productos(this, binding.condicionPrED)
        binding.mostrarPublicacionPara.setOnClickListener {
            dialog = BottomSheetDialog(this)
            constantes_productos_publicados.mostrar_dialog_para(
                this,
                dialog,
                binding.mostrarPublicacionPara.text.toString()
            ) { selt ->
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
            binding.linealGarantia.visibility = if (isChecked) {
                binding.hayGarantiaProductoED.setText("")
                binding.radioGrupPlazoRG.clearCheck()
                View.VISIBLE
            } else {
                View.GONE
            }
        }
        binding.agregarHastagsED.setOnClickListener {
            dialog = BottomSheetDialog(this)
            constantes_productos_publicados.obtener_hastags_generales(
                binding.agregarHastagsED,
                this,
                hashtagsGenerales,
                dialog
            )
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
        obtener_metodos_pagos()
        obtener_metodos_entrega()
        logearCampos()


    }

    private fun popup() {
        val popup = PopupMenu(this, binding.popup)

        // Agregar opciones al menú
        popup.menu.add(Menu.NONE, 1, 1, "Publicaciones publicadas")
        popup.menu.add(Menu.NONE, 2, 2, "Publicaciones archivadas")
        popup.menu.add(Menu.NONE, 3, 3, "Publicaciones eliminadas")

        // Mostrar el popup
        popup.show()

        // Manejar clics en los ítems del menú
        popup.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                1 -> {
                    val intent = Intent(this, ver_productos_publicados::class.java)
                    intent.putExtra("tipo", "publicadas") // Puedes diferenciar así
                    startActivity(intent)
                    true
                }

                2 -> {
                    val intent = Intent(this, ver_productos_publicados::class.java)
                    intent.putExtra("tipo", "archivadas")
                    startActivity(intent)
                    true
                }

                3 -> {
                    val intent = Intent(this, ver_productos_publicados::class.java)
                    intent.putExtra("tipo", "eliminadas")
                    startActivity(intent)
                    true
                }

                else -> true
            }
        }
    }


    fun logearCampos() {
        val campos = listOf(
            "tituloPublicacionPrED" to binding.tituloPublicacionPrED.text.isNotBlank(),
            "modeloProductoED" to binding.modeloProductoED.text.isNotBlank(),
            "marcaProductoED" to binding.marcaProductoED.text.isNotBlank(),
            "subcategoriaProducto" to binding.subcategoriaProducto.text.isNotBlank(),
            "nombreProductoED" to binding.nombreProductoED.text.isNotBlank(),

            "condicionPrED" to binding.condicionPrED.text.isNotBlank(),

            "agregarHastagsED" to binding.agregarHastagsED.text.isNotBlank(),
            "masInformacionED" to binding.masInformacionED.text.isNotBlank(),
            "metodoPagoSelect" to binding.metodoPagoSelect.text.isNotBlank(),
            "metodoEntregaSelect" to binding.metodoEntregaSelect.text.isNotBlank(),
            "catSelcionado" to binding.catSelcionado.text.isNotBlank(),
            "vistraPreviaDescripciontitulo" to binding.vistraPreviaDescripciontitulo.text.isNotBlank(),
            "vistraPreviaDescripcion" to binding.vistraPreviaDescripcion.text.isNotBlank(),
            "siHayDescuento && precioNuevoDescuentoPrED" to (binding.siHayDescuento.isChecked && binding.precioNuevoDescuentoPrED.text.isNotBlank()),
            "siHayGarantia && hayGarantiaProductoED" to (binding.siHayGarantia.isChecked && binding.hayGarantiaProductoED.text.isNotBlank()),
            "agregaUbicaciones && agregaUbiED" to (binding.agregaUbicaciones.isChecked && binding.agregaUbiED.text.isNotBlank()),
            "imagenesValidas" to (obtenerImagenesValidas().isNotEmpty()),

            )

        for ((nombreCampo, estaLleno) in campos) {
            Log.d(
                "CamposVerificacion",
                "$nombreCampo está ${if (estaLleno) "LLENO o VÁLIDO" else "VACÍO o INVÁLIDO"}"
            )
        }
    }


    override fun onBackPressed() {
        val placeholder = ContextCompat.getDrawable(this, R.drawable.agregar_imagen_cuadrado)

        val precioTexto = binding.precioProductoED.text.toString()
        val precioValido = precioTexto.toIntOrNull()?.let { it > 0 } == true

        val stokTexto = binding.stokED.text.toString()
        val stokValido = stokTexto.toIntOrNull()?.let { it > 0 } == true

        val hayContenido =
            binding.tituloPublicacionPrED.text.isNotBlank() ||
                    binding.modeloProductoED.text.isNotBlank() ||
                    binding.marcaProductoED.text.isNotBlank() ||
                    binding.subcategoriaProducto.text.isNotBlank() ||
                    binding.nombreProductoED.text.isNotBlank() ||
                    stokValido ||
                    binding.condicionPrED.text.isNotBlank() ||
                    precioValido ||
                    binding.agregarHastagsED.text.isNotBlank() ||
                    binding.masInformacionED.text.isNotBlank() ||
                    binding.metodoPagoSelect.text.isNotBlank() ||
                    binding.metodoEntregaSelect.text.isNotBlank() ||
                    binding.catSelcionado.text.isNotBlank() ||
                    (binding.siHayDescuento.isChecked && binding.precioNuevoDescuentoPrED.text.isNotBlank()) ||
                    (binding.siHayGarantia.isChecked && binding.hayGarantiaProductoED.text.isNotBlank()) ||
                    (binding.agregaUbicaciones.isChecked && binding.agregaUbiED.text.isNotBlank()) ||
                    obtenerImagenesValidas().isNotEmpty() ||
                    (binding.imgSubir.drawable != null &&
                            binding.imgSubir.drawable.constantState != placeholder?.constantState)

        if (hayContenido) {
            AlertDialog.Builder(this)
                .setTitle("¿Cancelar publicación?")
                .setMessage("Si sales ahora, se perderá lo que hayas escrito. ¿Deseas continuar?")
                .setPositiveButton("Sí") { dialog, _ ->
                    dialog.dismiss()
                    super.onBackPressed()
                }
                .setNegativeButton("No") { dialog, _ ->
                    dialog.dismiss()
                }
                .show()
        } else {
            super.onBackPressed()
        }
    }


    private fun validar_campos(): Boolean {
        var esValido = true

        val titulo_producto = binding.tituloPublicacionPrED
        val modelo_producto = binding.modeloProductoED
        val marca_producto = binding.marcaProductoED
        val categoria_producto = binding.subcategoriaProducto
        val nombre_producto = binding.nombreProductoED
        val stok_producto = binding.stokED
        val condicion_producto = binding.condicionPrED
        val precioProducto = binding.precioProductoED
        val agregarhastags = binding.agregarHastagsED
        val mas_info = binding.masInformacionED
        val metodoPago = binding.metodoPagoSelect
        val metodo_entrega = binding.metodoEntregaSelect
        val categoria_Selecionada = binding.catSelcionado

        val vista_previa_descripcion_titulo = binding.vistraPreviaDescripciontitulo
        val vista_previa_descripcion = binding.vistraPreviaDescripcion

        val siHayDescuento = binding.siHayDescuento
        val precio_descuento = binding.precioNuevoDescuentoPrED
        val garantia = binding.siHayGarantia
        val hay_garantia = binding.hayGarantiaProductoED
        val ubicacion = binding.agregaUbicaciones
        val ubicacion_prd = binding.agregaUbiED

        val categoriasSinMarcaNiModelo = listOf(
            "Juguetes y juegos",
            "Arte y antigüedades",
            "Hobbies y actividades",
            "Ropa, calzado y accesorios",
            "Muebles",
            "Hogar y jardín",
            "Construcción y materiales"
        )
        val categoriaSeleccionadaText = categoria_Selecionada.text.toString().trim()

        fun validarCampoVacio(campo: EditText, mensaje: String) {
            if (campo.text.toString().isBlank()) {
                campo.error = mensaje
                esValido = false
            }
        }

        validarCampoVacio(titulo_producto, "Ingrese un título")
        validarCampoVacio(categoria_producto, "Seleccione una categoría")
        validarCampoVacio(nombre_producto, "Ingrese el nombre del producto")
        validarCampoVacio(condicion_producto, "Ingrese la condición del producto")
        validarCampoVacio(agregarhastags, "Seleccione al menos un hashtag")
        validarCampoVacio(mas_info, "Ingrese información adicional")

        if (metodoPago.text.toString().isBlank()) {
            Toast.makeText(this, "Seleccione un método de pago", Toast.LENGTH_SHORT).show()
            esValido = false
        }

        if (vista_previa_descripcion_titulo.text.toString().isBlank() ||
            vista_previa_descripcion.text.toString().isBlank()
        ) {
            Toast.makeText(this, "Agrega una característica", Toast.LENGTH_SHORT).show()
            esValido = false
        }

        if (metodo_entrega.text.toString().isBlank()) {
            Toast.makeText(this, "Seleccione un método de entrega", Toast.LENGTH_SHORT).show()
            esValido = false
        }

        // Validar marca y modelo si la categoría lo requiere
        if (categoriaSeleccionadaText.isNotEmpty() &&
            !categoriasSinMarcaNiModelo.contains(categoriaSeleccionadaText)
        ) {
            validarCampoVacio(marca_producto, "Ingrese la marca")
            validarCampoVacio(modelo_producto, "Ingrese el modelo")
        }

        // Validar descuento
        if (siHayDescuento.isChecked) {
            validarCampoVacio(precio_descuento, "Ingrese el precio con descuento")
            val precioOriginal = precioProducto.text.toString().toDoubleOrNull()
            val precioConDescuento = precio_descuento.text.toString().toDoubleOrNull()

            if (precioOriginal != null && precioConDescuento != null) {
                if (precioConDescuento >= precioOriginal) {
                    Toast.makeText(
                        this,
                        "El precio con descuento debe ser menor al precio original",
                        Toast.LENGTH_SHORT
                    ).show()
                    precio_descuento.error = "Cambiar el valor"
                    precio_descuento.requestFocus()
                    esValido = false
                }
            } else {
                Toast.makeText(this, "Precios inválidos", Toast.LENGTH_SHORT).show()
                esValido = false
            }
        }

        // Validar garantía
        if (garantia.isChecked) {
            validarCampoVacio(hay_garantia, "Ingrese la información de la garantía")
        }

        // Validar ubicación si es requerida
        if (ubicacion.isChecked) {
            validarCampoVacio(ubicacion_prd, "Ingrese la ubicación del producto")
        }

        // Validar stock
        val stock = stok_producto.text.toString().toIntOrNull()
        if (stock == null || stock <= 0) {
            stok_producto.error = "El stock debe ser mayor a 0"
            stok_producto.requestFocus()

            esValido = false
        }

        // Validar precio original
        val precio = precioProducto.text.toString().toIntOrNull()
        if (precio == null || precio <= 0) {
            precioProducto.error = "El precio debe ser mayor a 0"
            precioProducto.requestFocus()
            esValido = false
        }

        return esValido
    }


    private fun publicar_producto() {
        binding.linealPublicando.isVisible = true
        binding.scroll.isVisible = false
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

        val db = FirebaseFirestore.getInstance().collection("Trabajadores_Usuarios_Drivers")
            .document("trabajadores").collection("trabajadores")
            .document(firebaseAuth.uid.toString())
            .collection("productos_venta").document("publicados").collection("publicados")

        val descuentoAplicado = if (descuento) {
            val descuentoCalculado =
                ((precioProducto.text.toString().toDouble() - precio_descuento_nuevo.text.toString()
                    .toDouble()) /
                        precioProducto.text.toString().toDouble()) * 100
            descuentoCalculado.roundToInt()
        } else {
            0
        }

        val hashtagsGenerales = binding.agregarHastagsED.text.toString()
            .split(",")
            .map { it.trim() }
            .filter { it.isNotBlank() }

        val datos = viewModel.datosDescripcion.value

        if (datos == null) {
            Toast.makeText(this, "Faltan datos de descripción", Toast.LENGTH_SHORT).show()
            return
        }

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
            "cantidad_porcentaje_descuento" to descuentoAplicado,
            "condicion_producto" to condicion_producto.text.toString(),
            "categoria_producto" to binding.catSelcionado.text.toString(),
            "subcategori_producto" to binding.subcategoriaProducto.text.toString(),
            "fechaPublicada" to mostrarFechaDialog_horaDialog.obtenerFechaActual(),
            "horaPublicada" to mostrarFechaDialog_horaDialog.obtenerHoraActual(),
            "garantia" to "${tiempoGarantiaYears.text} $unidadGarantia${
                if (tiempoGarantiaYears.text.toString().toIntOrNull() != 1)
                    if (unidadGarantia == "mes") "es" else "s" else ""
            }",
            "descuento" to descuento,
            "localidadUser" to localida_user.text.toString(),
            "marca" to marca_producto.text.toString(),
            "metodoEntrega" to binding.metodoEntregaSelect.text.toString(),
            "metodoPago" to binding.metodoPagoSelect.text.toString(),
            "modelo" to modelo_producto.text.toString(),
            "hashtags_generales" to hashtagsGenerales,
            "nombre" to nombre_producto.text.toString(),
            "precio" to (precioProducto.text.toString().toDoubleOrNull() ?: 0.0),
            "precioDelivery" to 5,
            "precio_descuento" to (precio_descuento_nuevo.text.toString().toDoubleOrNull() ?: 0.0),
            "stok" to stok_producto.text.toString(),
            "visivilidad" to mostra_para.text.toString(),
            "descripcion_titulo" to tituloMap,
            "descripcion_texto" to texto_map,
            "descripcion_texto_lista" to datos.listaEncontrados,
            "mas_informacio" to binding.masInformacionED.text.toString()
        )

        db.add(hasMap).addOnSuccessListener { res ->
            val productId = res.id
            val hasmap = hashMapOf<String, Any>("id" to productId)
            subir_imgCaracteristica(productId)
            db.document(productId).set(hasmap, SetOptions.merge())
                .addOnSuccessListener {
                    Toast.makeText(this, "Producto subido correctamente", Toast.LENGTH_SHORT).show()
                    val dbProductos_publicados =
                        FirebaseFirestore.getInstance().collection("productos_publicaciones")
                            .document("producto").collection("producto").document(productId)
                    val hasmap_producto = hashMapOf<String, Any>(
                        "id_trabajador" to firebaseAuth.uid.toString(),
                        "id" to productId,
                        "cantidad_porcentaje_descuento" to descuentoAplicado,

                        "titulo" to titulo_producto.text.toString(),
                        "precio" to (precioProducto.text.toString().toDoubleOrNull() ?: 0.0),
                        "precio_descuento" to (precio_descuento_nuevo.text.toString()
                            .toDoubleOrNull() ?: 0.0),
                        "metodoEntrega" to binding.metodoEntregaSelect.text.toString(),
                        "metodoPago" to binding.metodoPagoSelect.text.toString(),
                        "localidadUser" to localida_user.text.toString()
                    )
                    dbProductos_publicados.set(hasmap_producto, SetOptions.merge())
                        .addOnSuccessListener {
                            registrarPublicacionActiva(
                                binding.metodoEntregaSelect.text.toString(),
                                "publicados",
                                "metodos_entrega", productId
                            )
                            registrarPublicacionActiva(
                                binding.metodoPagoSelect.text.toString(),
                                "publicados",
                                "metodos_pago",
                                productId
                            )


                            binding.tituloPublicacionPrED.setText("")
                            binding.subcategoriaProducto.setText("")
                            binding.marcaProductoED.setText("")
                            binding.modeloProductoED.setText("")
                            binding.nombreProductoED.setText("")
                            binding.condicionPrED.setText("")
                            binding.precioProductoED.setText("0")
                            binding.siHayDescuento.isChecked = false
                            binding.siHayGarantia.isChecked = false
                            binding.agregarHastagsED.setText("")
                            binding.masInformacionED.setText("")
                            binding.agregaUbicaciones.isChecked = false
                            binding.metodoPagoSelect.text = ""
                            binding.chipsPagos.clearCheck()
                            binding.metodoEntregaSelect.text = ""
                            binding.vistraPreviaDescripciontitulo.text = ""
                            binding.vistraPreviaDescripcion.text = ""
                            binding.subir.isVisible = true
                            binding.camposEditar.isVisible = false
                            binding.linealVistaPreviaApartado.isVisible = false
                            binding.linealGarantia.isVisible = false
                            binding.hayGarantiaProductoED.setText("")
                            binding.precioNuevoDescuentoPrED.setText("0")
                            binding.precioNuevoDescuentoPr.isVisible = false
                            binding.radioGrupPlazoRG.clearCheck()
                            binding.chipsEntregas.clearCheck()
                            binding.catSelcionado.text = ""
                            binding.stokED.setText("0")
                            resetearImagenes()
                            binding.linealPublicando.isVisible = false
                            binding.scroll.isVisible = true
                            Toast.makeText(
                                this,
                                "se agrego a la otra coleccion",
                                Toast.LENGTH_SHORT
                            ).show()
                        }.addOnFailureListener { e ->
                            Log.d("erro_agregar", "error ala agregar la referencia")
                        }
                }
            guardar_img_storage(productId)
        }.addOnFailureListener {
            Toast.makeText(this, "Error al agregar el producto", Toast.LENGTH_SHORT).show()
        }
    }


    fun registrarPublicacionActiva(
        identrega_pago: String,
        tipo: String,
        metodo_pago_entrega: String,
        id_publicacion: String
    ) {
        val firestore = FirebaseFirestore.getInstance()
        val uid = firebaseAuth.uid.toString()

        val ref = firestore.collection("Trabajadores_Usuarios_Drivers")
            .document("trabajadores")
            .collection("trabajadores")
            .document(uid)
            .collection(metodo_pago_entrega)
            .document(identrega_pago)

        // Crear la estructura para añadir al mapa
        val data = mapOf(
            "publicaciones_activas.$id_publicacion" to mapOf(
                "archivados" to false,
                "eliminados" to false,
                "privado" to false,
                "publicados" to true,
                "solo_seguidores" to false,
                "productos_publicaciones" to true,
                "id_publicacion" to id_publicacion,
                "activo" to true
            )
        )

        // Usamos update con notación de punto para agregar al mapa sin borrar lo anterior
        ref.update(data)
            .addOnSuccessListener {
                Log.d("Firestore", "Publicación $id_publicacion añadida al mapa correctamente.")
            }
            .addOnFailureListener { e ->
                Log.e("Firestore", "Fallo al hacer update, intentando set: ${e.message}")

                val initData = mapOf(
                    "publicaciones_activas" to mapOf(
                        id_publicacion to mapOf(
                            "archivados" to false,
                            "eliminados" to false,
                            "privado" to false,
                            "publicados" to true,
                            "solo_seguidores" to false,
                            "productos_publicaciones" to true,
                            "activo" to true
                        )
                    )
                )

                ref.set(initData, SetOptions.merge())
                    .addOnSuccessListener {
                        Log.d("Firestore", "Mapa creado con publicación $id_publicacion.")
                    }
                    .addOnFailureListener { ex ->
                        Log.e("Firestore", "Error final: ${ex.message}")
                    }
            }
    }


    private fun resetearImagenes() {
        val placeholderCaracteristica =
            ContextCompat.getDrawable(this, R.drawable.agregar_imagen_cuadrado)
        binding.imgSubir.setImageDrawable(placeholderCaracteristica)

        val drawablePorDefecto = ContextCompat.getDrawable(this, R.drawable.agregar_imagen)

        imageViews.forEachIndexed { index, imageView ->
            imageView.setImageDrawable(drawablePorDefecto)
            imageView.visibility = if (index == 0) View.VISIBLE else View.GONE
        }

        currentImageIndex = 0
    }


    private fun subir_imgCaracteristica(productId: String) {
        val storageRef = FirebaseStorage.getInstance().reference
        val userId = firebaseAuth.uid.toString()
        val productoId = productId
        val fileName = "caracteristica_producto"
        val fileUri: Uri? = img1_uir1 // <- puede ser nulo

        val rutaImagen = storageRef
            .child("usuarios")
            .child(userId)
            .child("productos_publicados")
            .child(productoId)
            .child(fileName)

        if (fileUri != null && fileUri.toString().isNotBlank()) {
            rutaImagen.putFile(fileUri)
                .addOnSuccessListener {
                    rutaImagen.downloadUrl.addOnSuccessListener { uri ->
                        Log.d("Upload", "Imagen subida: $uri")
                        // Puedes guardar este URL en Firestore, RealtimeDB, etc.
                    }
                }
                .addOnFailureListener { e ->
                    Log.e("Upload", "Error al subir imagen", e)
                }
        } else {
            Log.d("Upload", "No se subió imagen porque Uri es nulo o vacío")
        }
    }

    private fun obtenerImagenesValidas(): List<ShapeableImageView> {
        val placeholder = ContextCompat.getDrawable(this, R.drawable.agregar_imagen)
        return imageViews.filter { imageView ->
            imageView.drawable != null && imageView.drawable.constantState != placeholder?.constantState
        }
    }


    private fun guardar_img_storage(id_publicacion: String) {
        val imagenesValidas = obtenerImagenesValidas()
        val firestoreData = hashMapOf<String, Any>()
        var contadorSubidas = 0

        val totalImagenes = imagenesValidas.size
        Log.d("IMAGENES", "Total de imágenes para subir: $totalImagenes")

        val db = FirebaseFirestore.getInstance().collection("Trabajadores_Usuarios_Drivers")
            .document("trabajadores").collection("trabajadores")
            .document(firebaseAuth.uid.toString()).collection("productos_venta")
            .document("publicados").collection("publicados").document(id_publicacion)

        val dbProductos_publicados = FirebaseFirestore.getInstance()
            .collection("productos_publicaciones")
            .document("producto").collection("producto")
            .document(id_publicacion)

        imagenesValidas.forEachIndexed { index, imageView ->
            val bitmap = (imageView.drawable as? BitmapDrawable)?.bitmap
            if (bitmap == null) {
                Log.e("IMAGENES", "Bitmap en índice $index es null. Imagen inválida.")
                return@forEachIndexed
            }

            val baos = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
            val data = baos.toByteArray()

            val nombreArchivo = "imagen_$index.jpg"
            val storageRef = FirebaseStorage.getInstance().reference
                .child("usuarios/${firebaseAuth.uid}/productos_venta/$id_publicacion/$nombreArchivo")

            storageRef.putBytes(data)
                .addOnSuccessListener {
                    Log.d("IMAGENES", "Imagen $index subida correctamente.")
                    storageRef.downloadUrl.addOnSuccessListener { uri ->
                        val campoNombre = if (index == 0) "img_url" else "img_url${index + 1}"
                        firestoreData[campoNombre] = uri.toString()
                        Log.d("IMAGENES", "URL obtenida para $campoNombre: $uri")

                        contadorSubidas++
                        if (contadorSubidas == totalImagenes) {
                            Log.d("IMAGENES", "Todas las URLs listas. Subiendo a Firestore...")
                            db.update(firestoreData)
                                .addOnSuccessListener {
                                    Log.d("Firestore", "URLs actualizadas en productos_venta.")
                                }
                                .addOnFailureListener { e ->
                                    Log.e(
                                        "Firestore",
                                        "Error en update productos_venta: ${e.message}"
                                    )
                                }

                            dbProductos_publicados.update(firestoreData)
                                .addOnSuccessListener {
                                    Log.d(
                                        "Firestore",
                                        "URLs actualizadas en productos_publicaciones."
                                    )
                                }
                                .addOnFailureListener { e ->
                                    Log.e(
                                        "Firestore",
                                        "Error en update productos_publicaciones: ${e.message}"
                                    )
                                }
                        }
                    }.addOnFailureListener { e ->
                        Log.e("Storage", "Error al obtener URL $index: ${e.message}")
                    }
                }
                .addOnFailureListener { e ->
                    Log.e("Storage", "Error al subir imagen $index: ${e.message}")
                }
        }

        // Imagen principal separada
        val imgPrincipal = binding.imgSubir
        if (imgPrincipal.visibility == View.VISIBLE && imgPrincipal.drawable != null) {
            val bitmap = (imgPrincipal.drawable as? BitmapDrawable)?.bitmap
            if (bitmap != null) {
                val baos = ByteArrayOutputStream()
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
                val data = baos.toByteArray()

                val nombreArchivo = "imagen_caracteristica.jpg"
                val storageRef = FirebaseStorage.getInstance().reference
                    .child("usuarios/${firebaseAuth.uid}/productos_venta/$id_publicacion/$nombreArchivo")

                storageRef.putBytes(data)
                    .addOnSuccessListener {
                        Log.d("Principal", "Imagen principal subida correctamente.")
                    }
                    .addOnFailureListener { e ->
                        Log.e("Principal", "Error al subir imagen principal: ${e.message}")
                    }
            } else {
                Log.e("Principal", "Bitmap imagen principal es null")
            }
        }
    }


    fun mostrarVistaPrevia(bindingSheet: BottomSheetConfiguracionDescripcionPrVrBinding) {
        bindingSheet.linealVistaPrevia.apply {
            if (!isVisible) {
                alpha = 0f
                isVisible = true
                animate()
                    .alpha(1f)
                    .setDuration(300)
                    .start()
            }
        }
    }

    fun ocultarVistaPrevia(bindingSheet: BottomSheetConfiguracionDescripcionPrVrBinding) {
        bindingSheet.linealVistaPrevia.animate()
            .alpha(0f)
            .setDuration(200)
            .withEndAction {
                bindingSheet.linealVistaPrevia.isVisible = false
            }.start()
    }


    private fun mostrarBottomSheetDescripcion(
        titulo: String? = null,
        texto_des: String? = null,
        fuenteTextoTitulo: String? = null,
        mayus_minus: String? = null,
        fuenteTextoTitulo_des: String? = null,
        mayus_minus_des: String? = null,
        listaFrases: List<String>? = null,
        mostrarVistaPrevia: Boolean = false
    ) {
        val bindingSheet =
            BottomSheetConfiguracionDescripcionPrVrBinding.inflate(LayoutInflater.from(this))
        val view = bindingSheet.root


        bindingSheet.linealPrinciapl.isVisible = true
        bindingSheet.cargaContenidoActuliazr.isVisible = false

        if (titulo != null && titulo.trim().isNotEmpty()) {
            Log.d("DEBUGsss", "Mostrar vista previa")
            mostrarVistaPrevia(bindingSheet)
        } else {
            Log.d("DEBUGsss", "Ocultar vista previa")
            ocultarVistaPrevia(bindingSheet)
        }
        // Prellenar campos si hay datos
        bindingSheet.tituloProductoED.setText(titulo ?: "")
        bindingSheet.AgregaDescipcionProductoED.setText(texto_des ?: "")
        bindingSheet.colocarBoldAgunasLetrasED.setText(listaFrases?.joinToString(", ") ?: "")
        Log.d("vezllamada", "mayus titulo $mayus_minus , mayus de texto $mayus_minus_des")

        // --- FIX FOR MAYUS/MINUS SELECTION ---

        // Clear selection for title's capitalization options
        bindingSheet.includeAgregarBoldTitulo.gurpoMayus.clearCheck()
        when (mayus_minus?.lowercase()) {
            "mayuscula" -> bindingSheet.includeAgregarBoldTitulo.mayuscula.isChecked = true
            "minuscula" -> bindingSheet.includeAgregarBoldTitulo.minuscula.isChecked = true
            else -> {
                // Optionally, uncheck both if no valid option is provided
                // This ensures a clean slate if previous state was set
                bindingSheet.includeAgregarBoldTitulo.mayuscula.isChecked = false
                bindingSheet.includeAgregarBoldTitulo.minuscula.isChecked = false
            }
        }

        // Clear selection for description's capitalization options
        bindingSheet.includeAgregarTextosCuales.gurpoMayus.clearCheck()
        when (mayus_minus_des?.lowercase()) {
            "mayuscula" -> bindingSheet.includeAgregarTextosCuales.mayusTxt.isChecked = true
            "minuscula" -> bindingSheet.includeAgregarTextosCuales.minusTxt.isChecked = true
            else -> {
                // Optionally, uncheck both if no valid option is provided
                bindingSheet.includeAgregarTextosCuales.mayusTxt.isChecked = false
                bindingSheet.includeAgregarTextosCuales.minusTxt.isChecked = false
            }
        }

        // --- Continue with the rest of your existing logic ---

        // Also do this for bold/italic/underline options for the title
        bindingSheet.includeAgregarBoldTitulo.grupoSubralladoTXT.clearCheck()
        when (fuenteTextoTitulo?.lowercase()) {
            "bold" -> bindingSheet.includeAgregarBoldTitulo.bold.isChecked = true
            "cursiva" -> bindingSheet.includeAgregarBoldTitulo.cursiva.isChecked = true
            "subrayado" -> bindingSheet.includeAgregarBoldTitulo.subrallado.isChecked = true
            else -> {
                bindingSheet.includeAgregarBoldTitulo.bold.isChecked = false
                bindingSheet.includeAgregarBoldTitulo.cursiva.isChecked = false
                bindingSheet.includeAgregarBoldTitulo.subrallado.isChecked = false
            }
        }

        // Also do this for bold/italic/underline options for the description
        bindingSheet.includeAgregarTextosCuales.grupoSubralladoTXT.clearCheck()
        when (fuenteTextoTitulo_des?.lowercase()) {
            "bold" -> bindingSheet.includeAgregarTextosCuales.bold.isChecked = true
            "cursiva" -> bindingSheet.includeAgregarTextosCuales.cursiva.isChecked = true
            "subrayado" -> bindingSheet.includeAgregarTextosCuales.subrallado.isChecked = true
            else -> {
                bindingSheet.includeAgregarTextosCuales.bold.isChecked = false
                bindingSheet.includeAgregarTextosCuales.cursiva.isChecked = false
                bindingSheet.includeAgregarTextosCuales.subrallado.isChecked = false
            }
        }




        bindingSheet.tituloProductoED.addTextChangedListener(object : TextWatcher {
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val texto = s?.toString()?.trim()
                if (!texto.isNullOrEmpty()) {
                    mostrarVistaPrevia(bindingSheet)
                    actualizarTextoFormateado(bindingSheet)
                } else {
                    ocultarVistaPrevia(bindingSheet)
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun afterTextChanged(s: Editable?) {}
        })



        bindingSheet.AgregaDescipcionProductoED.addTextChangedListener {
            bindingSheet.textoDescripcion.text = it.toString()
        }

        bindingSheet.colocarBoldAgunasLetrasED.addTextChangedListener {
            actualizarVistaPreviaConNegritas(bindingSheet)
        }

        // Checkboxes title (keep these as they are)
        bindingSheet.includeAgregarBoldTitulo.bold.setOnCheckedChangeListener { _, _ ->
            actualizarTextoFormateado(bindingSheet)
        }
        bindingSheet.includeAgregarBoldTitulo.cursiva.setOnCheckedChangeListener { _, _ ->
            actualizarTextoFormateado(bindingSheet)
        }
        bindingSheet.includeAgregarBoldTitulo.subrallado.setOnCheckedChangeListener { _, _ ->
            actualizarTextoFormateado(bindingSheet)
        }
        bindingSheet.includeAgregarBoldTitulo.mayuscula.setOnCheckedChangeListener { _, _ ->
            actualizarTextoFormateado(bindingSheet)
        }
        bindingSheet.includeAgregarBoldTitulo.minuscula.setOnCheckedChangeListener { _, _ ->
            actualizarTextoFormateado(bindingSheet)
        }

        // Checkboxes description (keep these as they are)
        bindingSheet.includeAgregarTextosCuales.bold.setOnCheckedChangeListener { _, _ ->
            actualizarVistaPreviaConNegritas(bindingSheet)
        }
        bindingSheet.includeAgregarTextosCuales.cursiva.setOnCheckedChangeListener { _, _ ->
            actualizarVistaPreviaConNegritas(bindingSheet)
        }
        bindingSheet.includeAgregarTextosCuales.subrallado.setOnCheckedChangeListener { _, _ ->
            actualizarVistaPreviaConNegritas(bindingSheet)
        }
        bindingSheet.includeAgregarTextosCuales.mayusTxt.setOnCheckedChangeListener { _, _ ->
            actualizarVistaPreviaConNegritas(bindingSheet)
        }
        bindingSheet.includeAgregarTextosCuales.minusTxt.setOnCheckedChangeListener { _, _ ->
            actualizarVistaPreviaConNegritas(bindingSheet)
        }

        bindingSheet.guardarCambios.setOnClickListener {
            val titulo = bindingSheet.tituloProductoED.text.toString().trim()
            val descripcion = bindingSheet.AgregaDescipcionProductoED.text.toString().trim()

            if (titulo.isEmpty()) {
                bindingSheet.tituloProductoED.error = "Ingrese un título para tu descripción"
                bindingSheet.tituloProductoED.requestFocus()
            } else if (descripcion.isEmpty()) {
                bindingSheet.AgregaDescipcionProductoED.error = "Ingrese una descripción"
                bindingSheet.AgregaDescipcionProductoED.requestFocus()
            } else {
                guardar_cabmios_descripcion(bindingSheet)

                if (mostrarVistaPrevia) {
                    binding.imgSubir.isVisible = true
                    binding.linealVistaPreviaApartado.isVisible = true
                    binding.subir.isVisible = false
                    binding.camposEditar.isVisible = true
                }
            }
        }


        actualizarTextoFormateado(bindingSheet)
        actualizarVistaPreviaConNegritas(bindingSheet)
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

                    binding_bottom_sheeet.includeAgregarTextosCuales.mayusTxt.isChecked -> parte.uppercase()
                    binding_bottom_sheeet.includeAgregarTextosCuales.minusTxt.isChecked -> parte.lowercase()
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
        var texto = binding_bottom_sheeet.tituloProductoED.text.toString()

        // Convertir a mayúsculas o minúsculas si corresponde
        texto = when {
            binding_bottom_sheeet.includeAgregarBoldTitulo.mayuscula.isChecked -> texto.uppercase()
            binding_bottom_sheeet.includeAgregarBoldTitulo.minuscula.isChecked -> texto.lowercase()
            else -> texto
        }

        val spannable = SpannableString(texto)

        // Aplicar estilo
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

        binding_bottom_sheeet.previewTextTitulo.text = spannable
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
            R.id.mayus_txt -> "mayuscula"
            R.id.minus_txt -> "minuscula"
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
        datosDescripcionGlobal = datos
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

    private fun obtener_metodos_pagos() {
        val db = FirebaseFirestore.getInstance().collection("Trabajadores_Usuarios_Drivers")
            .document("trabajadores")
            .collection("trabajadores")
            .document(firebaseAuth.uid.toString())
            .collection("metodos_pago")

        db.get().addOnSuccessListener { res ->
            binding.chipsPagos.removeAllViews()
            if (res.size() == 0) {
                binding.metodoPago.isVisible = true
            } else {
                binding.metodoPago.isVisible = false
                for (datos in res) {
                    val nombreMetodo = datos.getString("nombre_metodo")
                    val id = datos.getString("id") // el ID que quieres mostrar
                    if (!nombreMetodo.isNullOrEmpty() && !id.isNullOrEmpty()) {
                        val chip = Chip(this).apply {
                            text = nombreMetodo
                            isCheckable = true
                            tag = id // guardamos el ID como tag del chip
                        }

                        binding.chipsPagos.addView(chip)
                    }
                }
                binding.chipsPagos.setOnCheckedStateChangeListener { group, checkedIds ->
                    if (checkedIds.isNotEmpty()) {
                        val selectedChip = group.findViewById<Chip>(checkedIds[0])
                        val idSeleccionado = selectedChip.tag?.toString() ?: "Sin ID"
                        Toast.makeText(this, "ID seleccionado: $idSeleccionado", Toast.LENGTH_SHORT)
                            .show()
                        binding.metodoPagoSelect.text = idSeleccionado.toString()
                    }
                }
            }
            binding.metodoPago.setOnClickListener {
                dialog = BottomSheetDialog(this)
                constantes_metodo_pago_entrega.bottomSheet_metodos_pago(dialog,this ,) {
                    obtener_metodos_pagos()
                }
                dialog.show()
            }

        }.addOnFailureListener {
            Toast.makeText(this, "Error al cargar métodos de pago", Toast.LENGTH_SHORT).show()
        }
    }


    private fun obtener_metodos_entrega() {
        val db = FirebaseFirestore.getInstance().collection("Trabajadores_Usuarios_Drivers")
            .document("trabajadores")
            .collection("trabajadores")
            .document(firebaseAuth.uid.toString())
            .collection("metodos_entrega")

        db.get().addOnSuccessListener { res ->
            binding.chipsEntregas.removeAllViews() // Limpiar chips anteriores
            if (res.size() == 0) {
                binding.metodoEntrega.isVisible = true
            } else {
                binding.metodoEntrega.isVisible = false
                for (datos in res) {
                    val nombreMetodo = datos.getString("nombre_metodo")
                    val id =
                        datos.getString("id") // Asegúrate de que este campo exista en Firestore

                    if (!nombreMetodo.isNullOrEmpty() && !id.isNullOrEmpty()) {
                        val chip = Chip(this).apply {
                            text = nombreMetodo
                            isCheckable = true
                            tag = id // Guardamos el ID como tag
                        }

                        binding.chipsEntregas.addView(chip)
                    }
                }
                binding.chipsEntregas.setOnCheckedStateChangeListener { group, checkedIds ->
                    if (checkedIds.isNotEmpty()) {
                        val selectedChip = group.findViewById<Chip>(checkedIds[0])
                        val idSeleccionado = selectedChip.tag?.toString() ?: "Sin ID"
                        Toast.makeText(this, "ID seleccionado: $idSeleccionado", Toast.LENGTH_SHORT)
                            .show()
                        binding.metodoEntregaSelect.text = idSeleccionado.toString()
                    }
                }
            }


        }.addOnFailureListener {
            Toast.makeText(this, "Error al cargar métodos de entrega", Toast.LENGTH_SHORT).show()
        }
        binding.metodoEntrega.setOnClickListener {
            dialog = BottomSheetDialog(this)
            constantes_metodo_pago_entrega.bottomSheet_metodo_entrega(dialog, lista_entrega, this) {
                obtener_metodos_entrega()
            }
            dialog.show()
        }
    }


}