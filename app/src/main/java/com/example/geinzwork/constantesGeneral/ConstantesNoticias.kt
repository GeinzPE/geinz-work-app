package com.geinzz.geinzwork.constantesGeneral

import android.R
import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.animation.Animation
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.core.view.isVisible
import androidx.core.widget.NestedScrollView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.airbnb.lottie.LottieAnimationView
import com.example.geinzwork.constantesGeneral.Variables
import com.example.geinzwork.constantesGeneral.constantes_bottom_shet_trabaja
import com.geinzz.geinzwork.adapterViewholder.adaptadorAnuncios
import com.geinzz.geinzwork.dataclass.dataClassAnuncios
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDragHandleView
import com.google.android.material.chip.Chip
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.dynamiclinks.androidParameters
import com.google.firebase.dynamiclinks.dynamicLinks
import com.google.firebase.dynamiclinks.googleAnalyticsParameters
import com.google.firebase.dynamiclinks.iosParameters
import com.google.firebase.dynamiclinks.itunesConnectAnalyticsParameters
import com.google.firebase.dynamiclinks.shortLinkAsync
import com.google.firebase.dynamiclinks.socialMetaTagParameters
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions


object constantesNoticias {
    lateinit var firebaseAuth: FirebaseAuth
    lateinit var bottomSheet: BottomSheetDragHandleView

    @RequiresApi(Build.VERSION_CODES.O)
    fun obtenerNoticiasGeneral(
        context: Context,
        econtrado: TextView,
        recielAnuncios: RecyclerView,
        loading: LinearLayoutCompat,
        lineal: LinearLayout,
        btnFiltrado: ImageView,
        zoomout: Animation?,
        zoomouts: Animation?,
        no_resultados: RelativeLayout,
    ) {
        val listaAnunciosGeneral = mutableListOf<dataClassAnuncios>()
        val db = FirebaseFirestore.getInstance().collection(Variables.noticiasDB)


        lineal.isVisible = false
        loading.isVisible = true
        no_resultados.isVisible = false
        recielAnuncios.isVisible = false

        db.get().addOnSuccessListener { data ->
            for (datos in data) {
                val id = datos.id
                if (id != "noticias_geinz") {
                    val anuncio = dataClassAnuncios(
                        propietario = datos.getString("propietario") ?: "",
                        contenido = datos.getString("Contenido") ?: "",
                        fecha = datos.getString("fecha") ?: "",
                        titulo = datos.getString("titulo") ?: "",
                        img = datos.getString("imagenUrl") ?: "",
                        promo = datos.getString("estado") ?: "",
                        id = id,
                        numero = datos.getString("numero") ?: "",
                        mensaje = datos.getString("whatsappmsj") ?: "",
                        fechaVencimiento = (datos.get("fechas") as? Map<String, Any>)?.get("fecha_vencimiento") as? String
                            ?: "",
                        estadoVencimiento = datos.getString("estado") ?: "",
                        categoria = datos.getString("Categoria") ?: "",
                        TipoPromo = datos.getString("tipoPromo") ?: "",
                        idProp = datos.getString("idTiendaProp") ?: "",
                        localida = datos.getString("localidadPublicidad") ?: "",
                        efectivo = datos.getBoolean("efectivo") ?: false,
                        yape = datos.getBoolean("yape") ?: false,
                        tipT = datos.getBoolean("tipo_T") ?: false,
                        listener = datos.getBoolean("listener") ?: false,
                        descuentoBoolean = datos.getBoolean("descuento_boolean") ?: false,
                        nuevoPrecioDescuento = datos.getString("descuento") ?: "",
                        porcentajeDescuento = datos.get("porcentajeDescuento") as? Number ?: 0
                    )

                    listaAnunciosGeneral.add(anuncio)
                    listaAnunciosGeneral.shuffle() // <-- mezcla aleatoriamente la lista
                    econtrado.text = listaAnunciosGeneral.size.toString()
                }
            }

            inicializarReciles(
                Variables.tipoNoticia,
                listaAnunciosGeneral,
                recielAnuncios,
                context,
                zoomout,
                zoomouts
            )

            actualizarVisibilidad(
                listaAnunciosGeneral.isNotEmpty(),
                loading,
                lineal,
                btnFiltrado,
                recielAnuncios,
                no_resultados
            )


            loading.isVisible = false
            lineal.isVisible = true
            recielAnuncios.isVisible = true
        }.addOnFailureListener { e ->
            println("Error al obtener los anuncios: $e")
            loading.isVisible = false
            lineal.isVisible = false
            recielAnuncios.isVisible = false
        }
    }


    @RequiresApi(Build.VERSION_CODES.O)
    private fun ObtenerNoticiasGeinz(
        context: Context,
        econtrado: TextView,
        recielAnuncios: RecyclerView,
        loading: LinearLayoutCompat,
        lineal: LinearLayout,
        btnFiltrado: ImageView,
        zoomout: Animation?,
        zoomouts: Animation?,
        no_resultados: RelativeLayout,
    ) {
        val listaAnunciosGeinz = mutableListOf<dataClassAnuncios>()
        val db = FirebaseFirestore.getInstance()
            .collection(Variables.noticiasDB)
            .document("noticias_geinz")
            .collection(Variables.noticiasDB)

        db.get().addOnSuccessListener { res ->
            for (datos in res) {
                val mapFechas = datos.get("fechas") as? Map<String, Any>
                val fechaVencimientomap = mapFechas?.get("fecha_vencimiento") as? String

                val anuncio = dataClassAnuncios(
                    propietario = datos.getString("propietario") ?: "",
                    contenido = datos.getString("Contenido") ?: "",
                    fecha = datos.getString("fecha") ?: "",
                    titulo = datos.getString("titulo") ?: "",
                    img = datos.getString("imagenUrl") ?: "",
                    promo = datos.getString("estado") ?: "",
                    id = datos.getString("id") ?: "",
                    numero = datos.getString("numero") ?: "",
                    mensaje = datos.getString("whatsappmsj") ?: "",
                    fechaVencimiento = fechaVencimientomap ?: "",
                    estadoVencimiento = datos.getString("estado") ?: "",
                    categoria = datos.getString("Categoria") ?: "",
                    TipoPromo = datos.getString("tipoPromo") ?: "",
                    idProp = datos.getString("idTiendaProp") ?: "",
                    localida = datos.getString("localidad") ?: "",
                    efectivo = datos.getBoolean("efectivo") ?: false,
                    yape = datos.getBoolean("yape") ?: false,
                    tipT = datos.getBoolean("tipo_T") ?: false,
                    listener = datos.getBoolean("listener") ?: false,
                    descuentoBoolean = datos.getBoolean("descuento_boolean") ?: false,
                    nuevoPrecioDescuento = datos.getString("descuento") ?: "",
                    porcentajeDescuento = datos.get("porcentajeDescuento") as? Number ?: 0
                )

                listaAnunciosGeinz.add(anuncio)
                econtrado.text = listaAnunciosGeinz.size.toString()
                Log.d("obtenemosFecha", fechaVencimientomap.toString())
            }

            inicializarReciles(
                Variables.tipoNoticia,
                listaAnunciosGeinz,
                recielAnuncios,
                context,
                zoomout,
                zoomouts
            )

            actualizarVisibilidad(
                listaAnunciosGeinz.isNotEmpty(),
                loading,
                lineal,
                btnFiltrado,
                recielAnuncios,
                no_resultados
            )

            loading.isVisible = false
            lineal.isVisible = true
            recielAnuncios.isVisible = true
        }.addOnFailureListener { e ->
            println("Error al obtener los anuncios: $e")
            loading.isVisible = false
            lineal.isVisible = false
            recielAnuncios.isVisible = false
        }
    }


    @RequiresApi(Build.VERSION_CODES.O)
    fun obtenerFiltradoNoticias(
        zoomout: Animation?,
        zoomouts: Animation?,
        lista: MutableList<dataClassAnuncios>,
        categoriaString: String,
        recielAnuncios: RecyclerView,
        context: Context,
        localidadPasada: String,
        loading: LinearLayoutCompat,
        lineal: LinearLayout,
        btnFiltrado: ImageView,
        encontrados: TextView,
        no_resultados: RelativeLayout,
    ) {

        val db = FirebaseFirestore.getInstance()
        val noticiasColeccion = db.collection(Variables.noticiasDB)

        loading.isVisible = true
        no_resultados.isVisible = false
        lineal.isVisible = false
        recielAnuncios.isVisible = false

        noticiasColeccion.get()
            .addOnSuccessListener { documents ->
                lista.clear()

                for (document in documents) {
                    val data = document.data
                    val mapFechas = data?.get("fechas") as? Map<String, Any>
                    val fechaVencimiento = mapFechas?.get("fecha_vencimiento") as? String

                    val anuncio = dataClassAnuncios(
                        propietario = data["propietario"] as? String ?: "",
                        contenido = data["Contenido"] as? String ?: "",
                        fecha = data["fecha"] as? String ?: "",
                        titulo = data["titulo"] as? String ?: "",
                        img = data["imagenUrl"] as? String ?: "",
                        promo = data["estado"] as? String ?: "",
                        id = data["id"] as? String ?: "",
                        numero = data["numero"] as? String ?: "",
                        mensaje = data["whatsappmsj"] as? String ?: "",
                        fechaVencimiento = fechaVencimiento ?: "",
                        estadoVencimiento = data["estado"] as? String ?: "",
                        categoria = data["Categoria"] as? String ?: "",
                        TipoPromo = data["tipoPromo"] as? String ?: "",
                        idProp = data["idTiendaProp"] as? String ?: "",
                        localida = data["localidad"] as? String ?: "",
                        efectivo = data["efectivo"] as? Boolean ?: false,
                        yape = data["yape"] as? Boolean ?: false,
                        tipT = data["tipo_T"] as? Boolean ?: false,
                        listener = data["listener"] as? Boolean ?: false,
                        descuentoBoolean = data["descuento_boolean"] as? Boolean ?: false,
                        nuevoPrecioDescuento = data["descuento"] as? String ?: "",
                        porcentajeDescuento = data["porcentajeDescuento"] as? Number ?: 0
                    )


                    if ((categoriaString == anuncio.categoria && localidadPasada == anuncio.localida) ||
                        (categoriaString == "General" && localidadPasada == "General")
                    ) {
                        lista.add(anuncio)
                        lista.shuffle() // <-- Mezcla la lista después de agregar
                        encontrados.text = lista.size.toString()
                    }
                }


                inicializarReciles(
                    Variables.tipoNoticia,
                    lista,
                    recielAnuncios,
                    context,
                    zoomout,
                    zoomouts
                )
                actualizarVisibilidad(
                    hayArticulos = lista.isNotEmpty(),
                    loading = loading,
                    lineal = lineal,
                    btnFiltrado = btnFiltrado,
                    recielAnuncios = recielAnuncios,
                    no_resultados = no_resultados
                )

                loading.isVisible = false
                recielAnuncios.isVisible = true
            }
            .addOnFailureListener { e ->
                println("Error al obtener los anuncios: $e")
                loading.isVisible = false
                lineal.isVisible = false
                recielAnuncios.isVisible = false
            }
    }


    private fun actualizarVisibilidad(
        hayArticulos: Boolean,
        loading: LinearLayoutCompat,
        lineal: LinearLayout,
        btnFiltrado: ImageView,
        recielAnuncios: RecyclerView,
        no_resultados: RelativeLayout,
    ) {
        loading.isVisible = false
        if (hayArticulos) {
            lineal.isVisible = true
            loading.isVisible = false
            btnFiltrado.isVisible = true
            recielAnuncios.isVisible = true
            no_resultados.isVisible = false
        } else {
            btnFiltrado.isVisible = false
            lineal.isVisible = false
            loading.isVisible = true
            recielAnuncios.isVisible = false
            no_resultados.isVisible = true
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun inicializarReciles(
        tipo: String,
        listaAnuncios: MutableList<dataClassAnuncios>,
        recicleNoticias: RecyclerView,
        context: Context, zoomout: Animation?, zoomouts: Animation?,
    ) {
        val recicle = recicleNoticias
        recicle.layoutManager = LinearLayoutManager(context)
        recicle.adapter = adaptadorAnuncios(
            false,
            tipo,
            zoomout,
            zoomouts,
            listaAnuncios,
            context,
            { dataClassAnuncios -> createAndShareDynamicLink(dataClassAnuncios, context) }
        )
    }


    fun createAndShareDynamicLink(dataClassAnuncios: dataClassAnuncios, context: Context) {
        Firebase.dynamicLinks.shortLinkAsync {
            link = Uri.parse("https://geinzapp.page.link/?id=${dataClassAnuncios.id}")
            domainUriPrefix = "https://geinzapp.page.link"
            androidParameters("com.geinzz.geinzwork") {
                minimumVersion = 125
            }
            iosParameters("com.geinzz.ios") {
                appStoreId = "123456789"
                minimumVersion = "1.0.1"
            }
            googleAnalyticsParameters {
                source = "orkut"
                medium = "social"
                campaign = "geinzz-promo"
            }
            itunesConnectAnalyticsParameters {
                providerToken = "123456"
                campaignToken = "geinzz-promo"
            }
            socialMetaTagParameters {
                title = dataClassAnuncios.propietario.toString()
                description = dataClassAnuncios.contenido.toString()
                imageUrl = Uri.parse(dataClassAnuncios.img.toString())
            }
        }.addOnSuccessListener { shortDynamicLink ->
            val shortLink = shortDynamicLink.shortLink
            val invitationLink = shortLink.toString()

            val sendIntent: Intent = Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_TEXT, invitationLink)
                type = "text/plain"
            }
            context.startActivity(Intent.createChooser(sendIntent, null))
        }.addOnFailureListener { exception ->
            Log.e("DynamicLinkError", "Hubo un error con los links dinámicos", exception)
        }
    }


    fun mostrarDatos(loading: LinearLayoutCompat, lienal: LinearLayout, filtrado: ImageView) {
        loading.isVisible = false
        lienal.isVisible = true
        filtrado.isVisible = true
    }

    fun obtenerCategorias(autoCompleteCategory: AutoCompleteTextView) {
        val categoriasTiendas = mutableListOf<String>()
        val db =
            FirebaseFirestore.getInstance().collection(Variables.categoriasDB)
                .document(Variables.categoriasTiendaDB)

        db.get()
            .addOnSuccessListener { res ->
                if (res.exists()) {
                    val categorias = res[Variables.categoriasDB] as? ArrayList<String>
                    categorias?.let {
                        categoriasTiendas.addAll(it)
                        val adapter = ArrayAdapter(
                            autoCompleteCategory.context,
                            R.layout.simple_dropdown_item_1line,
                            categoriasTiendas
                        )
                        autoCompleteCategory.setAdapter(adapter)
                    }
                } else {
                    println("El documento de categorías no existe.")
                }
            }
            .addOnFailureListener { exception ->
                println("Error al obtener categorías: ${exception.message}")
            }
    }

    @SuppressLint("MissingInflatedId")
    @RequiresApi(Build.VERSION_CODES.O)
    fun filtrado_bottom(
        econtrado: TextView,
        recielAnuncios: RecyclerView,
        loading: LinearLayoutCompat,
        lineal: LinearLayout,
        btnFiltrado: ImageView,
        zoomout: Animation?,
        zoomouts: Animation?,
        no_resultados: RelativeLayout,
        keyLocalida: String,
        KeyCategoria: String,
        context: Context,
        dialog: BottomSheetDialog,
        textoGeneral: TextView? = null,
        localidadTexview: TextView? = null,
        categoriaTexview: TextView? = null,
        callback: (
            String?
        ) -> Unit,
        callback2: (String?) -> Unit,
    ) {
        val view = LayoutInflater.from(context)
            .inflate(com.geinzz.geinzwork.R.layout.bottom_sheet_recicle_productos, null, false)
        bottomSheet = view.findViewById(com.geinzz.geinzwork.R.id.cerrar)

        val btnApply = view.findViewById<Button>(com.geinzz.geinzwork.R.id.btnApply)
        val visualLocalidad =
            view.findViewById<LinearLayout>(com.geinzz.geinzwork.R.id.lineaLocalida)
        val localidad =
            view.findViewById<AutoCompleteTextView>(com.geinzz.geinzwork.R.id.localidad)
        filtradoLocalidadElementos.listaFiltrado(localidad)
        val btnCancel = view.findViewById<Button>(com.geinzz.geinzwork.R.id.btnCancel)
        val autoCompleteCategory =
            view.findViewById<AutoCompleteTextView>(com.geinzz.geinzwork.R.id.categoria)

        val FiltradoLocalidad =
            view.findViewById<LinearLayout>(com.geinzz.geinzwork.R.id.FiltradoLocalidad)
        val Filtradocategoria =
            view.findViewById<LinearLayout>(com.geinzz.geinzwork.R.id.Filtradocategoria)
        val textoLocalida = view.findViewById<TextView>(com.geinzz.geinzwork.R.id.filtradoUsuairo)
        val textoCategoria =
            view.findViewById<TextView>(com.geinzz.geinzwork.R.id.filtradoCateogoriaPromo)

        val linealContainer =
            view.findViewById<LinearLayout>(com.geinzz.geinzwork.R.id.lineal_filtrado_btn)

        val linealFiltroChips =
            view.findViewById<LinearLayout>(com.geinzz.geinzwork.R.id.lineal_filtros_chips)


        val deGeinz = view.findViewById<Chip>(com.geinzz.geinzwork.R.id.geinz)
        val general = view.findViewById<Chip>(com.geinzz.geinzwork.R.id.general)
        linealContainer.isVisible = true
        linealFiltroChips.isVisible = true
        FiltradoLocalidad.isVisible = true
        Filtradocategoria.isVisible = true
        visualLocalidad.isVisible = true

        textoLocalida.text = keyLocalida
        textoCategoria.text = KeyCategoria

        obtenerCategorias(autoCompleteCategory)


        bottomSheet.setOnClickListener {
            dialog.dismiss()
        }
        btnCancel.setOnClickListener {
            dialog.dismiss()
        }

        deGeinz.setOnClickListener {
            ObtenerNoticiasGeinz(
                context, econtrado, recielAnuncios,
                loading,
                lineal,
                btnFiltrado,
                zoomout,
                zoomouts,
                no_resultados
            )
            dialog.dismiss()
        }
        general.setOnClickListener {
            obtenerNoticiasGeneral(
                context,
                econtrado,
                recielAnuncios,
                loading,
                lineal,
                btnFiltrado,
                zoomout,
                zoomouts,
                no_resultados
            )
            dialog.dismiss()


        }
        if (keyLocalida != "" && KeyCategoria != "") {
            localidad.setText(keyLocalida)
            autoCompleteCategory.setText(KeyCategoria)
            obtenerCategorias(autoCompleteCategory)
            filtradoLocalidadElementos.listaFiltrado(localidad)
        }
        btnApply.setOnClickListener {
            val localidaSelect = localidad.text.toString()
            callback(localidaSelect)

            val categoriaSelect = autoCompleteCategory.text.toString()
            callback2(categoriaSelect)
            dialog.dismiss()
        }
        dialog.setContentView(view)
    }

    fun obtenerLikesNoticias(
        dataClassAnuncios: dataClassAnuncios,
        contadorLike: TextView,
        lineaMegusta: LinearLayout,
        carga: ProgressBar,
        contadorActividades: LinearLayout
    ) {
        carga.isVisible = true
        val postId = dataClassAnuncios.id.toString()
        val databaseReference =
            FirebaseDatabase.getInstance().getReference(Variables.cantidadLikes).child(postId)

        databaseReference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val totalLikes = snapshot.childrenCount.toInt()
                carga.isVisible = false
                contadorActividades.isVisible = true
                lineaMegusta.isVisible = true
                contadorLike.text = "${totalLikes}"
            }

            override fun onCancelled(error: DatabaseError) {
                lineaMegusta.isVisible = false
                contadorActividades.isVisible = false
                println("Error al obtener la cantidad de likes: ${error.message}")
            }
        })
    }

    fun agregarLike(
        dataClassAnuncios: dataClassAnuncios,
        context: Context,
        contadorLike: TextView,
    ) {
        val firebaseAuth = FirebaseAuth.getInstance()
        val idUser = firebaseAuth.uid.toString()
        val realtime = FirebaseDatabase.getInstance().getReference(Variables.cantidadLikes)
            .child(dataClassAnuncios.id.toString())
        realtime.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {

                if (snapshot.hasChild(idUser)) {
                    Toast.makeText(
                        context,
                        "No puedes volver a dejar un like",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {

                    val totalLikes = snapshot.childrenCount.toInt()
                    val nuevoTotal = totalLikes + 1
                    contadorLike.text = "$nuevoTotal"


                    val likeMap = mapOf("id" to idUser)

                    realtime.child(idUser).setValue(likeMap)
                        .addOnSuccessListener {
                            println("Like agregado correctamente")
                        }
                        .addOnFailureListener { e ->
                            println("Error al agregar el like: ${e.message}")
                        }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                println("Error al obtener la cantidad de likes: ${error.message}")
            }
        })
    }

    fun quitarLike(dataClassAnuncios: dataClassAnuncios, context: Context, contadorLike: TextView) {
        val like = contadorLike.text.toString()
        val likeINt = like.toInt()
        val nweres = likeINt - 1
        contadorLike.text = nweres.toString()
        firebaseAuth = FirebaseAuth.getInstance()
        val idUser = firebaseAuth.uid.toString()
        val realtime = FirebaseDatabase.getInstance().getReference(Variables.cantidadLikes)
            .child(dataClassAnuncios.id.toString())

        realtime.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.hasChild(idUser)) {
                    realtime.child(idUser).removeValue()
                        .addOnSuccessListener {
                            println("Like removido correctamente")
                        }
                        .addOnFailureListener { e ->
                            println("Error al remover el like: ${e.message}")
                        }
                } else {
                    Toast.makeText(
                        context,
                        "No has dado like previamente",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                println("Error al obtener la cantidad de likes: ${error.message}")
            }
        })
    }

    fun verificarLike(
        dataClassAnuncios: dataClassAnuncios,
        like: ImageView,
        context: Context,
        like_animation: LottieAnimationView,
        zoomin: Animation?,
        contadorLike: TextView,
    ) {
        val firebaseAuth = FirebaseAuth.getInstance()
        val currentUserUid = firebaseAuth.currentUser?.uid

        val realtime = FirebaseDatabase.getInstance().getReference(Variables.cantidadLikes)
            .child(dataClassAnuncios.id.toString())
        val db = FirebaseFirestore.getInstance().collection(Variables.noticiasDB)
            .document(dataClassAnuncios.id.toString())


        realtime.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                var isLiked: Boolean =
                    if (currentUserUid != null && snapshot.hasChild(currentUserUid)) {
                        like.setImageResource(com.geinzz.geinzwork.R.drawable.baseline_favorite_24)
                        true
                    } else {
                        like.setImageResource(com.geinzz.geinzwork.R.drawable.icon_corazon)
                        false
                    }

                like.setOnClickListener {
                    if (firebaseAuth.currentUser == null) {
                        val newDialog = BottomSheetDialog(context)
                        constantesPublicidad.CreacionCuentaBottom_shett(context, newDialog)
                        newDialog.show()
                    } else {
                        if (isLiked) {
                            like.setImageResource(com.geinzz.geinzwork.R.drawable.icon_corazon)
                            quitarLike(dataClassAnuncios, context, contadorLike)
                            constantesPublicidad.agregarCantidadClickAnuncios(
                                db,
                                "",
                                "like",
                                "decrementar"
                            )

                        } else {

                            constantesPublicidad.agregarCantidadClickAnuncios(db, "", "like")

                            agregarLike(dataClassAnuncios, context, contadorLike)
                            corazonLike(like_animation, com.geinzz.geinzwork.R.raw.bandai_dokkan)
                            like.setImageResource(com.geinzz.geinzwork.R.drawable.baseline_favorite_24)
                            like.startAnimation(zoomin)

                        }

                        isLiked = !isLiked
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {

                Log.e(ContentValues.TAG, "Error al obtener datos: ${error.message}")
            }
        })
    }

    fun corazonLike(imageView: LottieAnimationView, animationResId: Int) {
        imageView.cancelAnimation()


        imageView.setAnimation(animationResId)
        imageView.scaleType = ImageView.ScaleType.CENTER_CROP
        imageView.visibility = View.VISIBLE
        imageView.playAnimation()


        Handler(Looper.getMainLooper()).postDelayed({
            val fadeOutAnimation =
                android.view.animation.AnimationUtils.loadAnimation(
                    imageView.context,
                    android.R.anim.fade_out
                )
            fadeOutAnimation.setAnimationListener(object : Animation.AnimationListener {
                override fun onAnimationStart(animation: Animation?) {}
                override fun onAnimationEnd(animation: Animation?) {
                    imageView.cancelAnimation()
                    imageView.visibility = View.GONE
                }

                override fun onAnimationRepeat(animation: Animation?) {}
            })
            imageView.startAnimation(fadeOutAnimation)
        }, 2000)
    }


    fun retornarid(context: Context, dataClassAnuncios: dataClassAnuncios) {
        val idPublicaion = dataClassAnuncios.id
        firebaseAuth = FirebaseAuth.getInstance()


        val db2 = FirebaseFirestore.getInstance()
            .collection(Variables.trabajadores_usuariosDB)
            .document(Variables.trabajadoresDB)
            .collection(Variables.trabajadoresDB)
            .document(firebaseAuth.uid.toString())
            .collection("guardados").document("guardados").collection("noticias")


        val publicacion = mapOf(
            idPublicaion to "${dataClassAnuncios.categoria}"
        )

        db2.document(idPublicaion!!).set(publicacion, SetOptions.merge()).addOnSuccessListener {
            println("Noticia guardada")
        }.addOnFailureListener {
            println("Error al guardar la reseña")
        }
    }

    fun guardar(
        imageView: LottieAnimationView,
        aniamtion: Int,
        like: Boolean,
        idUser: String,
        dataClassAnuncios: dataClassAnuncios,
        context: Context,
        imagView: ImageView,
    ): Boolean {
        val db = FirebaseFirestore.getInstance().collection(Variables.noticiasDB)
            .document(dataClassAnuncios.id.toString())
        if (!like) {
            imageView.isVisible = true
            imageView.setAnimation(aniamtion)
            imageView.playAnimation()
            retornarid(context, dataClassAnuncios)
            constantesPublicidad.agregarCantidadClickAnuncios(db, "", "guardados")

        } else {
            imageView.isVisible = false
            eliminarGuardado(imageView, imagView, context, idUser, dataClassAnuncios.id.toString())
            imagView.setImageResource(com.geinzz.geinzwork.R.drawable.save_icon)
        }
        return !like
    }

    fun actualizarAnimacion(
        imageView: LottieAnimationView,
        aniamtion: Int,
        isSaved: Boolean,
        imagView: ImageView,
    ) {
        if (isSaved) {
            imageView.setAnimation(aniamtion)
            imageView.playAnimation()
            imageView.isVisible = true
        } else {
            imageView.isVisible = false
            imagView.setImageResource(com.geinzz.geinzwork.R.drawable.save_icon)
        }
    }


    fun eliminarGuardado(
        imageView: LottieAnimationView,
        imagView: ImageView,
        context: Context,
        idUser: String,
        idPublicaion: String,
    ) {

        val db =
            FirebaseFirestore.getInstance().collection(Variables.noticiasDB).document(idPublicaion)
        val db2 = FirebaseFirestore.getInstance()
            .collection(Variables.trabajadores_usuariosDB)
            .document(Variables.trabajadoresDB)
            .collection(Variables.trabajadoresDB)
            .document(idUser)
            .collection("guardados").document("guardados").collection("noticias")


        val referenceToDelete = db2.document(idPublicaion)
        db2.get().addOnSuccessListener { res ->
            for (document in res) {
                val data = document.data
                if (document.id == idPublicaion) {
                    referenceToDelete.delete()
                        .addOnSuccessListener {
                            imageView.cancelAnimation()
                            imageView.isVisible = false
                            imagView.setImageResource(com.geinzz.geinzwork.R.drawable.save_icon)

                            constantesPublicidad.agregarCantidadClickAnuncios(
                                db,
                                "",
                                "guardados",
                                "decrementar"
                            )
                        }
                        .addOnFailureListener { exception ->
                            Log.d("Firestore", "Error al eliminar el documento: ", exception)
                        }
                    break
                }
            }
        }


    }


}