package com.example.geinzwork

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AlertDialog
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.GridLayoutManager
import com.example.geinzwork.adapterViewholder.adapter_agregar_imagenes_panel_publicaciones
import com.example.geinzwork.constantesGeneral.Variables
import com.example.geinzwork.constantesGeneral.constantes_bottom_shet_trabaja.handler
import com.example.geinzwork.vistaTrabajador.ver_publicaciones_vista_verificados
import com.geinzz.geinzwork.R
import com.geinzz.geinzwork.constantesGeneral.mostrarFechaDialog_horaDialog
import com.geinzz.geinzwork.databinding.ActivityCrearPublicacionesRecientesBinding
import com.geinzz.geinzwork.databinding.BottomSheetHastagsFiltradosBinding
import com.geinzz.geinzwork.databinding.BottomSheetPublicacionesParaBinding
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.chip.Chip
import com.google.android.material.imageview.ShapeableImageView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.io.ByteArrayOutputStream

class crear_publicaciones_recientes : AppCompatActivity() {

    private val hashtagsGenerales = mutableListOf<String>()
    val db = FirebaseFirestore.getInstance()
    private val hashtagsCategoria = mutableListOf<String>()
    private val lista_Selecionados = mutableListOf<List<String>>()
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var binding: ActivityCrearPublicacionesRecientesBinding
    private lateinit var dialog: BottomSheetDialog
    private val imagenesSeleccionadas = mutableListOf<Uri>()
    private lateinit var adapter: adapter_agregar_imagenes_panel_publicaciones
    private val imageViews by lazy {
        listOf(binding.img1, binding.img2, binding.img3, binding.img4, binding.img5)
    }
    private var currentImageIndex = 0

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
        binding = ActivityCrearPublicacionesRecientesBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        firebaseAuth = FirebaseAuth.getInstance()
        binding.horizontalScrollView.post {
            binding.horizontalScrollView.fullScroll(View.FOCUS_RIGHT)
        }
        // Ocultar todas las imágenes inicialmente
        imageViews.forEach { it.visibility = View.GONE }

        // Mostrar la primera imagen al inicio
        imageViews[0].visibility = View.VISIBLE

        // Configurar los clics para seleccionar imágenes
        imageViews.forEachIndexed { index, imageView ->
            imageView.setOnClickListener {
                currentImageIndex = index
                pickImage.launch("image/*")
            }
        }


        binding.agregarHastagsED.setOnClickListener {
            dialog = BottomSheetDialog(this)
            obtener_hastags_generales(this, hashtagsGenerales, dialog)
            dialog.show()
        }
        binding.mostrarPublicacionPara.setOnClickListener {
            dialog = BottomSheetDialog(this)
            mostrar_dialog_para(binding.mostrarPublicacionPara.text.toString()) { selt ->
                binding.mostrarPublicacionPara.text = selt
            }
            dialog.show()
        }

        binding.agregarHastagsCategoriasED.setOnClickListener {
            dialog = BottomSheetDialog(this)
            obtenerHastags_cada_cat(binding.complete.text.toString())
            dialog.show()
        }

        obtenerCategorias(binding.complete)
        setearCategoriaDefecto_trabajador(firebaseAuth.uid.toString())
        binding.agregaUbicaciones.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                binding.agregaUbi.isVisible = true
            } else {
                binding.agregaUbi.isVisible = false
            }
        }
        binding.popup.setOnClickListener {
            popup()
        }


        iniciarRecycler()
        binding.publicar.setOnClickListener {
            if (verificarCamposRequeridos()) {
                val imagenesValidas = obtenerImagenesValidas()
                if (imagenesValidas.isEmpty()) {
                    Toast.makeText(
                        this@crear_publicaciones_recientes,
                        "Debe agregar al menos una imagen",
                        Toast.LENGTH_SHORT
                    ).show()
                    return@setOnClickListener
                }

                agregamopsPublicacion(firebaseAuth.uid.toString())
            } else {
                Toast.makeText(
                    this@crear_publicaciones_recientes,
                    "Por favor, completa todos los campos obligatorios",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

    }

    override fun onBackPressed() {
        val hayContenido = binding.tituloPublicacionED.text.isNotBlank() ||
                binding.descripcionServiciosED.text.isNotBlank() ||
                binding.agregarHastagsED.text.isNotBlank() ||
                binding.agregarHastagsCategoriasED.text.isNotBlank() ||
                obtenerImagenesValidas().isNotEmpty()

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

    private fun popup() {
        val popup = PopupMenu(this, binding.popup)
        popup.menu.add(Menu.NONE, 1, 1, "Ver Publicaciones")
        popup.show()
        popup.setOnMenuItemClickListener { item ->
            val itemID = item.itemId
            if (itemID == 1) {
                startActivity(Intent(this, ver_publicaciones_vista_verificados::class.java))
            } else {

            }
            return@setOnMenuItemClickListener true
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
            .document("hashtags_publicaciones")

        val chipGroup = bindig_BottomSheet.chipGrupHastagsP

        db.get().addOnSuccessListener { documentSnapshot ->
            val tiempoFin = System.currentTimeMillis()
            val tiempoTotal = tiempoFin - tiempoInicio
            handler.postDelayed({
                bindig_BottomSheet.netScroolViewHashtag.isVisible = true
                bindig_BottomSheet.cargandoHastag.isVisible = false
            }, tiempoTotal)

            if (documentSnapshot.exists()) {
                val hashtags = documentSnapshot.get("hashtags_publicaciones_array") as? List<String>
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
                                            this@crear_publicaciones_recientes,
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


    private fun obtenerHastags_cada_cat(
        categoriasSeleccionadas: String
    ) {
        Log.d("categori_pasda", categoriasSeleccionadas)
        val bindig_BottomSheet =
            BottomSheetHastagsFiltradosBinding.inflate(LayoutInflater.from(this))
        val view = bindig_BottomSheet.root
        bindig_BottomSheet.cerrar.setOnClickListener { dialog.dismiss() }
        val db = FirebaseFirestore.getInstance()
        val subcategoriasRef =
            db.collection("subcategoriasTrabajos").document(categoriasSeleccionadas)

        val tiempoInicio = System.currentTimeMillis()

        subcategoriasRef.get().addOnSuccessListener { documentSnapshot ->
            val tiempoFin = System.currentTimeMillis()
            val tiempoTotal = tiempoFin - tiempoInicio
            handler.postDelayed({
                bindig_BottomSheet.netScroolViewHashtag.isVisible = true
                bindig_BottomSheet.cargandoHastag.isVisible = false
            }, tiempoTotal)

            if (documentSnapshot.exists()) {
                val hashtags = documentSnapshot.get("hashtags_array") as? List<String>
                if (hashtags != null) {
                    val chipGroup = bindig_BottomSheet.chipGrupHastagsP
                    chipGroup.removeAllViews()
                    hashtagsCategoria.clear()

                    for (hashtag in hashtags) {
                        val chip = Chip(this).apply {
                            text = hashtag
                            isCheckable = true
                            isClickable = true

                            setOnCheckedChangeListener { buttonView, isChecked ->
                                if (isChecked) {
                                    if (hashtagsCategoria.size >= 5) {
                                        buttonView.isChecked = false // desmarcar el chip
                                        Toast.makeText(
                                            this@crear_publicaciones_recientes,
                                            "Solo puedes seleccionar hasta 5 hashtags",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    } else {
                                        hashtagsCategoria.add(text.toString())
                                    }

                                } else {
                                    hashtagsCategoria.remove(text.toString())
                                }
                                Log.d("HashtagCategoria", hashtagsCategoria.toString())
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
            Log.e(
                "Firestore", "Error al obtener documento: ${exception.message}"
            )
        }
        bindig_BottomSheet.agregarCampos.setOnClickListener {
            agregarhastags_generales_editext(hashtagsCategoria, binding.agregarHastagsCategoriasED)
            dialog.dismiss()
        }
        dialog.setContentView(view)
        dialog.show()
    }


    private fun agregarhastags_generales_editext(
        hashtagsGenerales: MutableList<String>,
        textView: TextView
    ) {
        val hashtagsTexto = hashtagsGenerales.joinToString(separator = ", ") { "$it" }
        textView.setText(hashtagsTexto)
    }

    private fun setearCategoriaDefecto_trabajador(id_trabajador: String) {
        val db = FirebaseFirestore.getInstance()
            .collection("Trabajadores_Usuarios_Drivers")
            .document("trabajadores")
            .collection("trabajadores")
            .document(id_trabajador)


        db.get().addOnSuccessListener { res ->
            if (res.exists()) {
                val categoria = res.getString("categoriaTrabajo")
                if (!categoria.isNullOrBlank()) {
                    binding.complete.setText(categoria, false)

                } else {
                    binding.complete.setText("") // O puedes dejarlo sin línea si no quieres modificar nada
                }
            } else {
                binding.complete.setText("") // Documento no existe
            }
        }.addOnFailureListener {
            // Opcional: puedes manejar errores aquí también
            Log.e("Firestore", "Error al obtener trabajador: ${it.message}")
        }
    }

    fun obtenerCategorias(autoCompleteTextView: AutoCompleteTextView) {
        val collection = db.collection(Variables.categoriasDB)
            .document(Variables.categoriasTrabajo)

        collection.get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val categorias =
                        document.get("categorias") as? List<String> // Usa el campo correcto
                    if (!categorias.isNullOrEmpty()) {
                        val adapter = ArrayAdapter(
                            autoCompleteTextView.context,
                            android.R.layout.simple_dropdown_item_1line,
                            categorias
                        )
                        autoCompleteTextView.setAdapter(adapter)

                        autoCompleteTextView.setOnItemClickListener { parent, view, position, id ->
                            val seleccionado = parent.getItemAtPosition(position).toString()
                            autoCompleteTextView.setText(
                                seleccionado,
                                false
                            ) // evita que se abra el dropdown
                            binding.agregarHastagsCategoriasED.setText("")
                        }
                    }
                }
            }
            .addOnFailureListener {
                Log.e("Firestore", "Error al obtener categorías: ${it.message}")
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

    private fun iniciarRecycler() {
        val imagenesSeleccionadas = mutableListOf<Uri>() // Lista de imágenes (vacía por ahora)

        binding.recycleImg.layoutManager = GridLayoutManager(this, 3) // 3 columnas

        // Creamos el adapter
        val adapter = adapter_agregar_imagenes_panel_publicaciones(imagenesSeleccionadas) {
            // Callback al hacer clic para agregar un nuevo recuadro vacío
            if (imagenesSeleccionadas.size < 5) {
                imagenesSeleccionadas.add(Uri.EMPTY) // Agregar un recuadro vacío (Uri vacío por ahora)
                // Al hacer el cambio, notificamos al adapter para actualizar el RecyclerView
                adapter.notifyItemInserted(imagenesSeleccionadas.size - 1)  // Notificar que se agregó un nuevo ítem
            }
        }

        // Asignamos el adapter al RecyclerView
        binding.recycleImg.adapter = adapter
    }


    private fun verificarCamposRequeridos(): Boolean {
        var todosValidos = true
        val titulo = binding.tituloPublicacionED
        val descripcionServiciosED = binding.descripcionServiciosED
        val agregarHastagsED = binding.agregarHastagsED
        val agregarHastagsCategoriasED = binding.agregarHastagsCategoriasED
        val ubicacionSwitch = binding.agregaUbicaciones
        val ubicacionED = binding.agregaUbiED

        // Validar campos vacíos
        if (titulo.text.toString().isBlank()) {
            titulo.error = "El campo título está vacío"
            titulo.requestFocus()
            todosValidos = false
        }

        if (descripcionServiciosED.text.toString().isBlank()) {
            descripcionServiciosED.error = "Agregue una descripción para su trabajo"
            descripcionServiciosED.requestFocus()
            todosValidos = false
        }

        if (agregarHastagsED.text.toString().isBlank()) {
            agregarHastagsED.error = "Debe ingresar al menos un hashtag"
            agregarHastagsED.requestFocus()
            todosValidos = false
        }

        if (agregarHastagsCategoriasED.text.toString().isBlank()) {
            agregarHastagsCategoriasED.error = "Debe ingresar al menos una categoría"
            agregarHastagsCategoriasED.requestFocus()
            todosValidos = false
        }

        // Validar ubicación si el switch está activado
        if (ubicacionSwitch.isChecked) {
            binding.agregaUbi.isVisible = true
            if (ubicacionED.text.toString().isBlank()) {
                ubicacionED.error = "Debe ingresar una ubicación"
                ubicacionED.requestFocus()
                todosValidos = false
            }
        } else {
            binding.agregaUbi.isVisible = false
        }
        val placeholderDrawable = ContextCompat.getDrawable(this, R.drawable.img_perfil)
        val alMenosUnaImagenSeteada = imageViews.any { imageView ->
            imageView.drawable != null &&
                    imageView.drawable.constantState != placeholderDrawable?.constantState
        }
        if (!alMenosUnaImagenSeteada) {
            Toast.makeText(this, "Agrega una imagen minimo", Toast.LENGTH_SHORT).show()
            todosValidos = false
        }

        return todosValidos
    }


    private fun agregamopsPublicacion(id_trabajador: String) {

        val db = FirebaseFirestore.getInstance().collection("Trabajadores_Usuarios_Drivers")
            .document("trabajadores").collection("trabajadores").document(id_trabajador)
            .collection("publicaciones_trabajos")

        val hashtagsGenerales = binding.agregarHastagsED.text.toString()
            .split(",")
            .map { it.trim() }
            .filter { it.isNotBlank() }

        val hashtagscategorias = binding.agregarHastagsCategoriasED.text.toString()
            .split(",")
            .map { it.trim() }
            .filter { it.isNotBlank() }

        // Primero crea el mapa sin el ID
        val hasmap = hashMapOf(
            "categoria" to binding.complete.text.toString(),
            "contenido" to binding.descripcionServiciosED.text.toString(),
            "fecha_rec" to mostrarFechaDialog_horaDialog.obtenerFechaActual(),
            "hora_rec" to mostrarFechaDialog_horaDialog.obtenerHoraActual(),
            "titulo" to binding.tituloPublicacionED.text.toString(),
            "hashtags_generales" to hashtagsGenerales,
            "hashtags_trabajos_publicados" to hashtagscategorias,
            "visivilidad" to binding.mostrarPublicacionPara.text.toString(),
            "ubicacion" to binding.agregaUbiED.text.toString()
        )



        db.add(hasmap).addOnSuccessListener { documentRef ->
            val newId = documentRef.id
            documentRef.update("id", newId)
            Toast.makeText(
                this@crear_publicaciones_recientes,
                "Trabajo publicado",
                Toast.LENGTH_SHORT
            ).show()
            binding.tituloPublicacionED.setText("")
            binding.descripcionServiciosED.setText("")
            binding.agregarHastagsED.setText("")
            binding.agregarHastagsCategoriasED.setText("")
            binding.agregaUbiED.setText("")
            binding.mostrarPublicacionPara.text="Todos"
            guardar_img_storage(newId)
            resetearImagenes()
        }.addOnFailureListener { e ->
            Toast.makeText(
                this@crear_publicaciones_recientes,
                "Error al subir la noticia",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun resetearImagenes() {
        val drawablePorDefecto = ContextCompat.getDrawable(this, R.drawable.agregar_imagen)

        imageViews.forEachIndexed { index, imageView ->
            imageView.setImageDrawable(drawablePorDefecto)
            imageView.visibility = if (index == 0) View.VISIBLE else View.GONE
        }

        currentImageIndex = 0
    }

    private fun obtenerImagenesValidas(): List<ShapeableImageView> {
        val placeholder = ContextCompat.getDrawable(this, R.drawable.agregar_imagen)
        return imageViews.filter { imageView ->
            imageView.drawable != null &&
                    imageView.drawable.constantState != placeholder?.constantState
        }
    }

    private fun guardar_img_storage(id_publicacion: String) {
        val imagenesValidas = obtenerImagenesValidas()

        imagenesValidas.forEachIndexed { index, imageView ->
            val bitmap = (imageView.drawable as BitmapDrawable).bitmap
            val baos = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
            val data = baos.toByteArray()

            val nombreArchivo = "imagen_$index.jpg"
            val storageRef = FirebaseStorage.getInstance().reference
                .child("usuarios/${firebaseAuth.uid.toString()}/publicaciones/$id_publicacion/$nombreArchivo")

            storageRef.putBytes(data)
                .addOnSuccessListener {
                    Log.d("Storage", "Imagen subida con éxito: $nombreArchivo")
                }
                .addOnFailureListener { e ->
                    Log.e("Storage", "Error al subir imagen $nombreArchivo: ${e.message}")
                }
        }

    }

}