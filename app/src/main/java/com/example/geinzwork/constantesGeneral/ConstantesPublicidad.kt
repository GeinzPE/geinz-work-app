package com.geinzz.geinzwork.constantesGeneral

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.preference.PreferenceManager
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import androidx.viewpager2.widget.ViewPager2
import com.bumptech.glide.Glide
import com.geinzz.geinzwork.Crea_tu_publicidad
import com.geinzz.geinzwork.CuentaFreelancer
import com.geinzz.geinzwork.Nosotros
import com.geinzz.geinzwork.R
import com.geinzz.geinzwork.databinding.ItemCustomFixedSizeLayout3Binding
import com.geinzz.geinzwork.vistaTiendas.tienda_no_lanzada_spasl
import com.geinzz.geinzwork.vistasPubliciadesGeinz.crear_cuenta_Tienda
import com.geinzz.geinzwork.vistasPubliciadesGeinz.noticiasGeinzpb
import com.geinzz.geinzwork.vistas_anuncios_general
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDragHandleView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import org.imaginativeworld.whynotimagecarousel.ImageCarousel
import org.imaginativeworld.whynotimagecarousel.listener.CarouselListener
import org.imaginativeworld.whynotimagecarousel.listener.CarouselOnScrollListener
import org.imaginativeworld.whynotimagecarousel.model.CarouselItem

object constantesPublicidad {
    private lateinit var bottomSheet: BottomSheetDragHandleView
    private lateinit var dialog: BottomSheetDialog
    private lateinit var firebaseAuth: FirebaseAuth
    private val KEY = "MY_KEY"

    fun obtenerAnunciosGeinz(viewPager: ViewPager2, carucel: ImageCarousel, contexto: Context, filtradoUsuairo: TextView) {
        val db =
            FirebaseFirestore.getInstance().collection("anuncios").document("anunciosPrimarios")
                .collection("anuncios")
        db.get()
            .addOnSuccessListener { contador ->
                for (anuncios in contador.documents) {
                    val AnunciosData = anuncios.data
                    val caption = AnunciosData?.get("caption") as? String
                    val urlimg = AnunciosData?.get("imagenUrl") as? String
                    println("El caption de anuncios es $caption y la URL de su img es $urlimg")

                    if (caption != null && urlimg != null) {
                        val lista = mutableListOf<CarouselItem>()
                        lista.add(CarouselItem(urlimg, caption))
                        carucel.addData(lista)
                    }
                }
            }

        carucel.carouselListener = object : CarouselListener {
            override fun onClick(position: Int, carouselItem: CarouselItem) {
                super.onClick(position, carouselItem)
                when (carouselItem.caption) {
                    "img1" -> {
                        contexto.startActivity(Intent(contexto, Nosotros::class.java))
                    }

                    "img2" -> {
                        contexto.startActivity(Intent(contexto, crear_cuenta_Tienda::class.java))
                    }

                    "img3" -> {
                        firebaseAuth = FirebaseAuth.getInstance()
                        if (firebaseAuth.currentUser == null) {
                            dialog = BottomSheetDialog(contexto)
                            CreacionCuentaBottom_shett(contexto, dialog)
                            dialog.show()
                        } else {
                            Toast.makeText(
                                contexto,
                                "Estas registrado en Geinz work",
                                Toast.LENGTH_SHORT
                            ).show()
                        }

                    }

                    "img4" -> {
                        contexto.startActivity(Intent(contexto, Crea_tu_publicidad::class.java))
                    }

                    "img5" -> {
                        val vista = Intent(contexto, tienda_no_lanzada_spasl::class.java)
                        contexto.startActivity(vista)
                    }

                    "img6" -> {
                        firebaseAuth = FirebaseAuth.getInstance()
                        val pref = PreferenceManager.getDefaultSharedPreferences(contexto)
                        if (firebaseAuth.currentUser == null) {
                            Toast.makeText(
                                contexto,
                                "Registrate en Geinz work para que puedas filtrar las tiendas y trabajadores",
                                Toast.LENGTH_SHORT
                            ).show()
                        } else {
                            dialog = BottomSheetDialog(contexto)
                            filtradoLocalidadElementos.filtradoNacionalidades(
                                "Seleccione su filtrado de Trabajadores y Tiendas",
                                contexto,
                                dialog
                            ) { seleccion ->
                                filtradoUsuairo.text = "$seleccion"
                            }
                            dialog.show()
                        }
                    }

                    "img7" -> {
                        viewPager.currentItem = 1
                    }

                }
            }
        }
    }


    fun obtenerAnunciosIniciosFragment(
        carucel: ImageCarousel,
        contexto: Context,
        documento: String,
    ) {
        val lista = mutableListOf<CarouselItem>()
        val db = FirebaseFirestore.getInstance().collection("anuncios").document(documento)
            .collection("anuncios")
        db.get()
            .addOnSuccessListener { res ->
                // Mezcla aleatoriamente los documentos y toma los primeros 5
                val documentosAleatorios = res.documents.shuffled().take(5)

                for (anuncios in documentosAleatorios) {
                    val AnunciosData = anuncios.data
                    val caption = AnunciosData?.get("caption") as? String
                    val urlimg = AnunciosData?.get("imagenUrl") as? String
                    println("El caption de anuncios es $caption y la URL de su img es $urlimg")

                    if (caption != null && urlimg != null) {
                        lista.add(CarouselItem(urlimg, caption))
                    }
                }
                // Establece los datos del carrusel
                carucel.setData(lista)
            }
        carucel.carouselListener = object : CarouselListener {
            override fun onClick(position: Int, carouselItem: CarouselItem) {
                super.onClick(position, carouselItem)
                when (carouselItem.caption) {
                    "img1" -> obtnerDatosPublicidad("img1", documento, contexto, "anuncio1")
                    "img2" -> obtnerDatosPublicidad("img2", documento, contexto, "anuncio2")
                    "img3" -> obtnerDatosPublicidad("img3", documento, contexto, "anuncio3")
                    "img4" -> obtnerDatosPublicidad("img4", documento, contexto, "anuncio4")
                    "img5" -> obtnerDatosPublicidad("img5", documento, contexto, "anuncio5")
                    "img6" -> obtnerDatosPublicidad("img6", documento, contexto, "anuncio6")
                    "img7" -> obtnerDatosPublicidad("img7", documento, contexto, "anuncio7")
                    "img8" -> obtnerDatosPublicidad("img8", documento, contexto, "anuncio8")
                    "img9" -> obtnerDatosPublicidad("img9", documento, contexto, "anuncio9")
                    "img10" -> obtnerDatosPublicidad("img10", documento, contexto, "anuncio10")
                    "img11" -> obtnerDatosPublicidad("img11", documento, contexto, "anuncio11")
                    "img12" -> obtnerDatosPublicidad("img12", documento, contexto, "anuncio12")
                    "img13" -> obtnerDatosPublicidad("img13", documento, contexto, "anuncio13")
                    "img14" -> obtnerDatosPublicidad("img14", documento, contexto, "anuncio14")
                    "img15" -> obtnerDatosPublicidad("img15", documento, contexto, "anuncio15")
                    "img16" -> obtnerDatosPublicidad("img16", documento, contexto, "anuncio16")
                    "img17" -> obtnerDatosPublicidad("img17", documento, contexto, "anuncio17")
                    "img18" -> obtnerDatosPublicidad("img18", documento, contexto, "anuncio18")
                    "img19" -> obtnerDatosPublicidad("img19", documento, contexto, "anuncio19")
                    "img20" -> obtnerDatosPublicidad("img20", documento, contexto, "anuncio20")
                }
            }
        }

    }

    @SuppressLint("MissingInflatedId")
    fun     CreacionCuentaBottom_shett(contexto: Context, dialog: BottomSheetDialog) {
        val view = LayoutInflater.from(contexto)
            .inflate(R.layout.bottom_sheet_creacion_cuenta, null, false)
        bottomSheet = view.findViewById(R.id.cerrar)

        val cuentaTrabajador = view.findViewById<ImageButton>(R.id.Cuenta_Trabajador)
        val cuentaUsario = view.findViewById<ImageButton>(R.id.Cuenta_usuario)

        cuentaTrabajador.setOnClickListener {
            constantes.showLoadingDialog(contexto, 3000, "Dirigiendo al sitio", "espere un momento")
            val vista = Intent(contexto, CuentaFreelancer::class.java)
            vista.putExtra("tipoCuenta", "cuentaTrabajador")
            vista.putExtra("Title", "Cuenta Freelancer")
            vista.putExtra("pasos", "Estas a 1/5 pasos")
            contexto.startActivity(vista)
            dialog.dismiss()
        }
        cuentaUsario.setOnClickListener {
            constantes.showLoadingDialog(contexto, 3000, "Dirigiendo al sitio", "espere un momento")
            val vista = Intent(contexto, CuentaFreelancer::class.java)
            vista.putExtra("tipoCuenta", "cuentaSimple")
            vista.putExtra("Title", "Cuenta Simple")
            vista.putExtra("pasos", "Estas a 1/2 pasos")
            contexto.startActivity(vista)
            dialog.dismiss()
        }
        bottomSheet.setOnClickListener {
            dialog.dismiss()
        }

        dialog.setContentView(view)
    }


    fun obteneranunciostorage(
        carrucel: ImageCarousel,
        listener: RelativeLayout,
        contexto: Context,
    ) {
        val lista = mutableListOf<CarouselItem>()
        val db =
            FirebaseFirestore.getInstance().collection("anuncios").document("anunciosSegundarios")
                .collection("anuncios")
        db.get()
            .addOnSuccessListener { contador ->
                // Mezcla aleatoriamente los documentos y toma los primeros 5
                val documentosAleatorios = contador.documents.shuffled().take(5) // 5 anuncios aleatorios

                for (anuncios in documentosAleatorios) {
                    val AnunciosData = anuncios.data
                    val caption = AnunciosData?.get("caption") as? String
                    val urlimg = AnunciosData?.get("imagenUrl") as? String
                    val carouselItem = CarouselItem(urlimg, caption)
                    lista.add(carouselItem)
                }

                carrucel.setData(lista)

                carrucel.carouselListener = object : CarouselListener {
                    override fun onCreateViewHolder(
                        layoutInflater: LayoutInflater,
                        parent: ViewGroup,
                    ): ViewBinding {
                        return ItemCustomFixedSizeLayout3Binding.inflate(
                            layoutInflater,
                            parent,
                            false
                        )
                    }

                    override fun onBindViewHolder(
                        binding: ViewBinding,
                        item: CarouselItem,
                        position: Int,
                    ) {
                        val currentBinding = binding as ItemCustomFixedSizeLayout3Binding
                        currentBinding.imageView.apply {
                            scaleType = ImageView.ScaleType.CENTER_CROP
                            Glide.with(contexto)
                                .load(item.imageUrl)
                                .placeholder(R.drawable.cargando_img)
                                .into(this)
                        }
                    }
                }

                carrucel.onScrollListener = object : CarouselOnScrollListener {
                    override fun onScrollStateChanged(
                        recyclerView: RecyclerView,
                        newState: Int,
                        position: Int,
                        carouselItem: CarouselItem?,
                    ) {
                        if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                            carouselItem?.apply {
                                obtenerCaptino(caption.toString(), listener, contexto)
                            }
                        }
                    }
                }

            }
            .addOnFailureListener { e ->
                println("Error al obtener la URL de la imagen: $e")
            }
    }


    fun obtenerCaptino(
        caption: String,
        lineal: RelativeLayout,
        contexto: Context,
    ) {
        lineal.setOnClickListener {
            Toast.makeText(contexto, "Cargando Contenido...", Toast.LENGTH_SHORT).show()
            when (caption) {
                "img1" -> obtnerDatosPublicidad("img1", "anunciosSegundarios", contexto, "anuncio1")
                "img2" -> obtnerDatosPublicidad("img2", "anunciosSegundarios", contexto, "anuncio2")
                "img3" -> obtnerDatosPublicidad("img3", "anunciosSegundarios", contexto, "anuncio3")
                "img4" -> obtnerDatosPublicidad("img4", "anunciosSegundarios", contexto, "anuncio4")
                "img5" -> obtnerDatosPublicidad("img5", "anunciosSegundarios", contexto, "anuncio5")
                "img6" -> obtnerDatosPublicidad("img6", "anunciosSegundarios", contexto, "anuncio6")
                "img7" -> obtnerDatosPublicidad("img7", "anunciosSegundarios", contexto, "anuncio7")
                "img8" -> obtnerDatosPublicidad("img8", "anunciosSegundarios", contexto, "anuncio8")
                "img9" -> obtnerDatosPublicidad("img9", "anunciosSegundarios", contexto, "anuncio9")
                "img10" -> obtnerDatosPublicidad("img10", "anunciosSegundarios", contexto, "anuncio10")
                "img11" -> obtnerDatosPublicidad("img11", "anunciosSegundarios", contexto, "anuncio11")
                "img12" -> obtnerDatosPublicidad("img12", "anunciosSegundarios", contexto, "anuncio12")
                "img13" -> obtnerDatosPublicidad("img13", "anunciosSegundarios", contexto, "anuncio13")
                "img14" -> obtnerDatosPublicidad("img14", "anunciosSegundarios", contexto, "anuncio14")
                "img15" -> obtnerDatosPublicidad("img15", "anunciosSegundarios", contexto, "anuncio15")
                "img16" -> obtnerDatosPublicidad("img16", "anunciosSegundarios", contexto, "anuncio16")
                "img17" -> obtnerDatosPublicidad("img17", "anunciosSegundarios", contexto, "anuncio17")
                "img18" -> obtnerDatosPublicidad("img18", "anunciosSegundarios", contexto, "anuncio18")
                "img19" -> obtnerDatosPublicidad("img19", "anunciosSegundarios", contexto, "anuncio19")
                "img20" -> obtnerDatosPublicidad("img20", "anunciosSegundarios", contexto, "anuncio20")
            }
        }

    }

    private fun obtnerDatosPublicidad(
        captionpasado: String,
        documento: String,
        contexto: Context,
        anuncio: String,
    ) {
        val db = FirebaseFirestore.getInstance().collection("anuncios").document(documento)
            .collection("anuncios").document(anuncio)

        db.get().addOnSuccessListener { res ->
            if (res.exists()) {
                val data = res.data
                val caption = data?.get("caption") as? String
                val disponibleAN = data?.get("disponibleAN") as? Boolean ?: false

                when (disponibleAN) {
                    true -> {
                        contexto.startActivity(Intent(contexto, Crea_tu_publicidad::class.java))
                    }

                    false -> {
                        if (caption == captionpasado) {
                            agregarCantidadClickAnuncios(db, anuncio, "click")
                            val mensaje = data["mensaje"] as? String
                            Intent(contexto, vistas_anuncios_general::class.java).apply {
                                putExtra("anuncio", anuncio)
                                putExtra("docuemnto", documento)
                                putExtra("mensaje", mensaje)
                                contexto.startActivity(this)
                            }
                        }
                    }
                }
            }


        }
    }


    fun agregarCantidadClickAnuncios(documento: DocumentReference, anuncio: String, campo: String, decrementacion: String? = null) {
        val db = documento
        val incremento: Long = if (decrementacion == "decrementar") {
            -1L
        } else {
            1L
        }
        val updates = mapOf(
            "estadisticas_$campo" to FieldValue.increment(incremento)
        )
        db.set(updates, SetOptions.merge())
            .addOnSuccessListener {
                Log.d("Firestore", "Campo $campo actualizado correctamente en $documento -> $anuncio")
            }
            .addOnFailureListener { e ->
                Log.w("Firestore", "Error al actualizar el campo $campo en estadísticas", e)
            }
    }

    fun getDatosUsuarios(callback: (String?, String?, String?, String?) -> Unit) {
        val idPAsado = FirebaseAuth.getInstance().uid.toString()
        val firestore = FirebaseFirestore.getInstance()
        val usuariosRef = firestore.collection("Trabajadores_Usuarios_Drivers")
            .document("trabajadores").collection("trabajadores")
        val usuariosCuentaNormalRef = firestore.collection("Trabajadores_Usuarios_Drivers")
            .document("usuarios").collection("usuarios")

        usuariosRef.whereEqualTo("id", idPAsado).get()
            .addOnSuccessListener { trabajadoresResult ->
                if (!trabajadoresResult.isEmpty) {
                    val datos = trabajadoresResult.documents.first()
                    val genero = datos.getString("genero")
                    val localidad = datos.getString("localidad")
                    val EdadActual = datos.getString("EdadActual")
                    val TipoCuenta = datos.getString("TipoCuenta")
                    callback(genero, EdadActual, localidad, TipoCuenta)
                } else {
                    usuariosCuentaNormalRef.whereEqualTo("id", idPAsado).get()
                        .addOnSuccessListener { usuariosResult ->
                            if (!usuariosResult.isEmpty) {
                                val datos = usuariosResult.documents.first()
                                val genero = datos.getString("genero")
                                val localidad = datos.getString("localidad")
                                val EdadActual = datos.getString("EdadActual")
                                val TipoCuenta = datos.getString("TipoCuenta")
                                callback(genero, EdadActual, localidad, TipoCuenta)
                            } else {
                                // Si no se encuentra en ninguna de las colecciones
                                callback(null, null, null, null)
                            }
                        }
                        .addOnFailureListener { exception ->
                            Log.e(
                                "FirestoreError",
                                "Error al obtener documentos de usuarios",
                                exception
                            )
                            callback(null, null, null, null)
                        }
                }
            }
            .addOnFailureListener { exception ->
                Log.e(
                    "FirestoreError",
                    "Error al obtener documentos de trabajadores",
                    exception
                )
                callback(null, null, null, null)
            }
    }

    fun obtenerLocalidaGeneroTipoCuenta(
        documentReference: DocumentReference,
        tipoEntrante: String
    ) {
        // Obtener los datos del usuario de manera asincrónica
        getDatosUsuarios { genero, edad, localidad, tipo ->
            // Validar que los datos requeridos no sean nulos o vacíos
            if (genero.isNullOrEmpty() || localidad.isNullOrEmpty() || tipo.isNullOrEmpty()) {
                println("Datos incompletos: género, localidad o tipo de cuenta faltan.")
                return@getDatosUsuarios
            }
            // Estructura del mapa a actualizar
            val updates = mutableMapOf<String, Any>()

            // Crear un mapa de estadísticas con incremento de 1 para cada campo relevante
            val estadisticas = mutableMapOf<String, Any>()

            // Incluir los incrementos para "genero", "localidad" y "tipo_cuenta"
            estadisticas["genero"] = mapOf(genero!!.lowercase() to FieldValue.increment(1))
            estadisticas["localidad"] = mapOf(localidad!!.lowercase() to FieldValue.increment(1))

            // Verificar tipoEntrante y agregar al mapa si es necesario
            if (tipoEntrante.lowercase() == "noticia" || tipoEntrante.lowercase() == "publicidad" || tipoEntrante.lowercase() == "verificacion") {
                estadisticas["tipo_cuenta"] = mapOf(tipo!!.lowercase() to FieldValue.increment(1))
            }

            // Agregar el mapa "estadisticas" a las actualizaciones
            updates["estadisticas"] = estadisticas

            // Obtener el documento de Firestore y verificar su estado
            documentReference.get()
                .addOnSuccessListener { res ->
                    if (res.exists()) {
                        // Verificar si el recurso está disponible
                        val disponibleAN = res.getBoolean("disponibleAN") ?: false
                        if (!disponibleAN) {
                            // Mostrar estado antes de la actualización
                            Log.d("Firestore", "Estado antes de actualización: ${res.data}")

                            // Realizar las actualizaciones en Firestore
                            documentReference.set(updates, SetOptions.merge())
                                .addOnSuccessListener {
                                    // Mostrar el estado después de la actualización
                                    documentReference.get().addOnSuccessListener { updatedRes ->
                                        Log.d(
                                            "Firestore",
                                            "Estado después de actualización: ${updatedRes.data}"
                                        )
                                    }
                                }
                                .addOnFailureListener { e ->
                                    Log.e(
                                        "Firestore",
                                        "Error al actualizar los campos: ${e.message}"
                                    )
                                }
                        } else {
                            Log.d(
                                "Firestore",
                                "El recurso está ocupado, no se realizaron actualizaciones."
                            )
                        }
                    } else {
                        Log.d("Firestore", "El documento no existe.")
                    }
                }
                .addOnFailureListener { e ->
                    Log.e("Firestore", "Error al obtener el documento: ${e.message}")
                }

        }
    }


}