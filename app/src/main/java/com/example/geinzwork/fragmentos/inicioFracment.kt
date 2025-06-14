package com.geinzz.geinzwork.fragmentos

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.preference.PreferenceManager
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.example.geinzwork.constantesGeneral.Variables
import com.example.geinzwork.constantesGeneral.constantes_vinculados
import com.example.geinzwork.constantesGeneral.constatnes_carga_imagenes_general
import com.example.geinzwork.oferta_principales_geinz
import com.example.geinzwork.vistaTrabajador.ver_promociones
import com.example.geinzwork.vistaTrabajador.ver_toto_publicaciones_trabajador
import com.example.geinzwork.vistaTrabajador.vista_ver_productos_trabajadores
import com.example.geinzwork.vistaTrabajador.vista_ver_publicaciones_trabajadores
import com.geinzz.geinzwork.MainActivity
import com.geinzz.geinzwork.R
import com.geinzz.geinzwork.adapterViewholder.adapterTrabajosMostrados
import com.geinzz.geinzwork.constantesGeneral.constantes
import com.geinzz.geinzwork.constantesGeneral.constantesCarrito
import com.geinzz.geinzwork.constantesGeneral.constantesPublicidad
import com.geinzz.geinzwork.constantesGeneral.constantesSubcategoriaszonasTiendas
import com.geinzz.geinzwork.constantesGeneral.constantesTrabajadoresTiendasInicioFragmet
import com.geinzz.geinzwork.constantesGeneral.constantes_publicaciones_general_user_tiendas.obtener_metodoEntrega
import com.geinzz.geinzwork.constantesGeneral.constantes_servicios
import com.geinzz.geinzwork.constantesGeneral.constantestextos_general
import com.geinzz.geinzwork.constantesGeneral.conteoUser
import com.geinzz.geinzwork.constantesGeneral.filtradoLocalidadElementos
import com.geinzz.geinzwork.databinding.FragmentInicioFracmentBinding
import com.geinzz.geinzwork.databinding.ItemCustomPublicidadPrincipalBinding
import com.geinzz.geinzwork.databinding.ItemProductsTrabajadoresPrincipalBinding
import com.geinzz.geinzwork.databinding.ItemPublicaiconesRecientesTrabajadoresInicioFragmentBinding
import com.geinzz.geinzwork.dataclass.dataClassCategoriasInicio
import com.geinzz.geinzwork.dataclass.dataClassTrabajosd
import com.geinzz.geinzwork.vistaTiendas.TiendasGenerales
import com.geinzz.geinzwork.vistaTrabajador.vistaTrabajador
import com.geinzz.geinzwork.vistaTrabajador.vista_CategoriasT
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.zxing.integration.android.IntentIntegrator
import org.imaginativeworld.whynotimagecarousel.ImageCarousel
import org.imaginativeworld.whynotimagecarousel.listener.CarouselListener
import org.imaginativeworld.whynotimagecarousel.model.CarouselItem

class inicioFracment : Fragment() {
    private lateinit var binding: FragmentInicioFracmentBinding
    private lateinit var mContex: Context
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var dialog: BottomSheetDialog

    private val listaTrabajo = mutableListOf<dataClassTrabajosd>().toMutableList()
    private val KEY = "MY_KEY"

    override fun onAttach(context: Context) {
        mContex = context
        super.onAttach(context)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        firebaseAuth = FirebaseAuth.getInstance()
        binding = FragmentInicioFracmentBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        obtenerImagenesFirestorage()
        SetAnuncios()
        obtenerTrabajosCat()
        obtenerProductos_trabajadores()
        obterTrabajosRecientes_trabajadores()
        val pref = PreferenceManager.getDefaultSharedPreferences(mContex)



        if (firebaseAuth.currentUser == null) {
            binding.linealAnuncioVerificado.isVisible = false

        } else {
            binding.linealAnuncioVerificado.isVisible = true
            constantes_vinculados.verificaAcceso(
                firebaseAuth.uid.toString(), mContex,
                onStart = {
                },
                onFinish = { dispositivoValido ->
                    if (!dispositivoValido) {
                        val dialogBuilder = AlertDialog.Builder(mContex)
                        dialogBuilder.setTitle("Sesión cerrada")
                        dialogBuilder.setMessage("Tu cuenta fue cerrada desde otro dispositivo. Si no fuiste tú, por favor contáctate con Geinz Work.")
                        dialogBuilder.setCancelable(false)
                        binding.linealAnuncioVerificado.isVisible = false
                        dialogBuilder.setPositiveButton("Contactar con Geinz Work") { dialog, _ ->

                            dialog.dismiss()
                            constantesTrabajadoresTiendasInicioFragmet.obtenerNombre_imgPerfil(
                                binding.includeCabezero.progressCargaImagen,
                                binding.includeCabezero.usuarioRegsitradoName,
                                mContex,
                                binding.includeCabezero.imgPerfilUser,
                                binding.linealAnuncioVerificado
                            ) { verificado ->
                                if (verificado) {
                                    binding.verificadoBoolean.text = "true"
                                } else {
                                    binding.verificadoBoolean.text = "false"
                                }

                            }
                        }

                        dialogBuilder.setNegativeButton("Cancelar") { dialog, _ ->
                            dialog.dismiss()
                            constantesTrabajadoresTiendasInicioFragmet.obtenerNombre_imgPerfil(
                                binding.includeCabezero.progressCargaImagen,
                                binding.includeCabezero.usuarioRegsitradoName,
                                mContex,
                                binding.includeCabezero.imgPerfilUser,
                                binding.linealAnuncioVerificado
                            ) { verificado ->
                                if (verificado) {
                                    binding.verificadoBoolean.text = "true"
                                } else {
                                    binding.verificadoBoolean.text = "false"
                                }

                            }
                        }

                        val alertDialog = dialogBuilder.create()
                        alertDialog.show()
                    } else {
                        constantesTrabajadoresTiendasInicioFragmet.obtenerNombre_imgPerfil(
                            binding.includeCabezero.progressCargaImagen,
                            binding.includeCabezero.usuarioRegsitradoName,
                            mContex,
                            binding.includeCabezero.imgPerfilUser,
                            binding.linealAnuncioVerificado
                        ) { verificado ->
                            if (verificado) {
                                binding.verificadoBoolean.text = "true"
                            } else {
                                binding.verificadoBoolean.text = "false"
                            }

                        }
                    }
                }

            )
        }


        val storedValue = pref?.getString(KEY, "Default Value")
        obtener_mensajes_destacados(binding.verificadoBoolean.text.toString())
        conteoUser.obtenerConteoUSer { usuarios ->
            binding.includeCabezero.usuariosRegistrados.text = usuarios.toString()
        }
        constantes.SolicitarPermisoNotificacion(mContex, permisoNotificaion)
        binding.includeCabezero.relativenotifica.setOnClickListener {
            initScanner()
        }
        binding.Tiendas.setOnClickListener {
            mContex.startActivity(Intent(mContex, ver_promociones::class.java))
        }
        if (firebaseAuth.currentUser == null && storedValue.isNullOrEmpty() || storedValue.equals("Default Value")) {
            constantesTrabajadoresTiendasInicioFragmet.obtenerLocalida(Variables.General)
            binding.includeCabezero.filtradoUsuairo.text = Variables.General
        } else {
            binding.includeCabezero.filtradoUsuairo.text = storedValue
            constantesTrabajadoresTiendasInicioFragmet.obtenerLocalida(storedValue!!)
        }

        if (binding.includeCabezero.filtradoUsuairo.text.toString() == Variables.General) {
            obtnerFiltrado(Variables.General)
        } else {
            constantes.carga(5000, { mostrarDatos(storedValue!!) })
        }
        binding.includeCabezero.imgPerfilUser.setOnClickListener {
            if (firebaseAuth.currentUser == null) {
                dialog = BottomSheetDialog(mContex)
                constantesPublicidad.CreacionCuentaBottom_shett(
                    mContex,
                    dialog
                )
                dialog.show()

            } else {
                dialog = BottomSheetDialog(mContex)
                filtradoLocalidadElementos.filtradoNacionalidades(
                    "Seleccione su filtrado de Trabajadores y Tiendas",
                    mContex,
                    dialog
                ) { seleccion ->
                    binding.includeCabezero.filtradoUsuairo.text = "$seleccion"
                }
                dialog.show()
            }
        }

        binding.includeCabezero.filtradoUsuairo.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                when (s.toString()) {
                    Variables.Supe -> {
                        obtnerFiltrado(Variables.Supe)
                        guardarShaderPref(pref, Variables.Supe)
                        constantesTrabajadoresTiendasInicioFragmet.obtenerLocalida(Variables.Supe)
                    }

                    Variables.Barranca -> {
                        obtnerFiltrado(Variables.Barranca)
                        guardarShaderPref(pref, Variables.Barranca)
                        constantesTrabajadoresTiendasInicioFragmet.obtenerLocalida(Variables.Barranca)
                    }

                    Variables.Paramonga -> {
                        obtnerFiltrado(Variables.Paramonga)
                        guardarShaderPref(pref, Variables.Paramonga)
                        constantesTrabajadoresTiendasInicioFragmet.obtenerLocalida(Variables.Paramonga)
                    }

                    Variables.Pativilca -> {
                        obtnerFiltrado(Variables.Pativilca)
                        guardarShaderPref(pref, Variables.Pativilca)
                        constantesTrabajadoresTiendasInicioFragmet.obtenerLocalida(Variables.Pativilca)
                    }

                    Variables.General -> {
                        obtnerFiltrado(Variables.General)
                        guardarShaderPref(pref, Variables.General)
                        constantesTrabajadoresTiendasInicioFragmet.obtenerLocalida(Variables.General)
                    }

                    else -> {
                        println("Localidad no reconocida")
                    }
                }
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        enviarCategoria()
        constantesTrabajadoresTiendasInicioFragmet.obtenerNombre_imgPerfil(
            binding.includeCabezero.progressCargaImagen,
            binding.includeCabezero.usuarioRegsitradoName,
            mContex,
            binding.includeCabezero.imgPerfilUser,
            binding.linealAnuncioVerificado
        ) { verificado ->
            if (verificado) {
                binding.verificadoBoolean.text = "true"
            } else {
                binding.verificadoBoolean.text = "false"
            }

        }
        binding.verTiendas.setOnClickListener {
            val vista = Intent(mContex, TiendasGenerales::class.java)
            val filtrado = binding.includeCabezero.filtradoUsuairo.text.toString()
            vista.putExtra(Variables.filtradoPasado, filtrado)
            startActivity(vista)
        }
        confSwipe(storedValue!!)


        val activity = requireActivity() as? MainActivity
        setupImageCarouselTouchListener(binding.carruselPimarioInicio, activity!!)
        setupImageCarouselTouchListener(binding.carrusel, activity!!)
        setupImageCarouselTouchListener(binding.carruse2, activity!!)
        setupImageCarouselTouchListener(binding.carrucelPublicacionesRecientes, activity!!)
        setupImageCarouselTouchListener(binding.carrucelProductosTrabajdores, activity!!)
        setupImageCarouselTouchListener(binding.IncludeAnunciosTercero.carrucel, activity!!)
        setupImageCarouselTouchListener(binding.IncludeAnunciosCuarto.carrucel, activity!!)
        setupImageCarouselTouchListener(binding.IncludeAnunciosQuinto.carrucel, activity!!)
        setupImageCarouselTouchListener(binding.IncludeAnunciosSexto.carrucel, activity!!)
        setupRecyclerViewTouchListener(binding.includeTrabajadoresTop.trabajadores, activity!!)
        setupRecyclerViewTouchListener(binding.includeRecicleViewddelivery.trabajadores, activity!!)
        setupRecyclerViewTouchListener(binding.RecicleCategoria, activity!!)
        setupRecyclerViewTouchListener(binding.includeReciclehogar.trabajadores, activity!!)
        setupRecyclerViewTouchListener(binding.includeReciclemecanico.trabajadores, activity!!)
        setupRecyclerViewTouchListener(binding.includeRecicleTecnicos.trabajadores, activity!!)


        binding.verTrabajosPublicados.setOnClickListener {
            val intent = Intent(mContex, ver_toto_publicaciones_trabajador::class.java).apply {

            }
            startActivity(intent)
        }

    }

    val permisoNotificaion =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { esConcedido -> }


    private fun obtener_mensajes_destacados(verificado: String) {
        val db = FirebaseFirestore.getInstance()
            .collection("politicas_problemas_verificaciones")
            .document("anotaciones_princiapales")
            .collection("anotaciones")

        db.get().addOnSuccessListener { res ->
            var listaAnotaciones = res.documents

            // Si está verificado, filtramos el ID que no debe mostrarse
            if (verificado == "true") {
                listaAnotaciones = listaAnotaciones.filter { it.id != "xhGtp1qWLIzA9SOYkd74" }
            }

            if (listaAnotaciones.isNotEmpty()) {
                val aleatorio = listaAnotaciones.random()
                val data = aleatorio.data
                val titulo = data?.get("titulo") as? String ?: ""
                val texto = data?.get("texto") as? String ?: ""
                val actividad = data?.get("actividad") as? String ?: ""

                binding.tituloDestacado.text = titulo
                binding.textoDestacado.text = texto

                binding.cerrarAnuncio.setOnClickListener {
                    binding.linealAnuncioVerificado.isVisible = false
                }

                binding.linealAnuncioVerificado.setOnClickListener {
                    try {
                        val clase = Class.forName(actividad)
                        val vista = Intent(mContex, clase)
                        startActivity(vista)
                    } catch (e: ClassNotFoundException) {
                        Toast.makeText(
                            mContex,
                            "Actividad no encontrada: $actividad",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        }
    }


    fun setupRecyclerViewTouchListener(recyclerView: RecyclerView, activity: MainActivity) {
        recyclerView.addOnItemTouchListener(object : RecyclerView.OnItemTouchListener {
            private var initialX = 0f
            private var initialY = 0f

            override fun onInterceptTouchEvent(rv: RecyclerView, e: MotionEvent): Boolean {
                when (e.action) {
                    MotionEvent.ACTION_DOWN -> {
                        initialX = e.x
                        initialY = e.y
                    }

                    MotionEvent.ACTION_MOVE -> {
                        val diffX = Math.abs(e.x - initialX)
                        val diffY = Math.abs(e.y - initialY)

                        // Solo bloquear si el movimiento es principalmente horizontal
                        if (diffX > diffY) {
                            activity.setViewPagerSwipeEnabled(false)
                            rv.parent?.requestDisallowInterceptTouchEvent(true)
//                            Toast.makeText(recyclerView.context, "RecyclerView en uso", Toast.LENGTH_SHORT).show()
                        }
                    }

                    MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                        activity.setViewPagerSwipeEnabled(true)
                        rv.parent?.requestDisallowInterceptTouchEvent(false)
//                        Toast.makeText(recyclerView.context, "RecyclerView liberado", Toast.LENGTH_SHORT).show()
                    }
                }
                return false
            }

            override fun onTouchEvent(rv: RecyclerView, e: MotionEvent) {}

            override fun onRequestDisallowInterceptTouchEvent(disallowIntercept: Boolean) {}
        })
    }


    fun setupImageCarouselTouchListener(carousel: ImageCarousel, activity: MainActivity) {
        carousel.viewTreeObserver.addOnGlobalLayoutListener(object :
            ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                carousel.viewTreeObserver.removeOnGlobalLayoutListener(this)

                val recyclerView =
                    carousel.findViewById<RecyclerView>(org.imaginativeworld.whynotimagecarousel.R.id.recyclerView)

                if (recyclerView != null) {
                    recyclerView.addOnItemTouchListener(object : RecyclerView.OnItemTouchListener {
                        private var initialX = 0f
                        private var initialY = 0f

                        override fun onInterceptTouchEvent(
                            rv: RecyclerView,
                            e: MotionEvent
                        ): Boolean {
                            when (e.action) {
                                MotionEvent.ACTION_DOWN -> {
                                    initialX = e.x
                                    initialY = e.y
                                    carousel.autoPlay =
                                        false // 🔹 Pausa el auto-play mientras el usuario toca
                                }

                                MotionEvent.ACTION_MOVE -> {
                                    val diffX = Math.abs(e.x - initialX)
                                    val diffY = Math.abs(e.y - initialY)

                                    if (diffX > diffY) {
                                        activity.setViewPagerSwipeEnabled(false)
                                        rv.parent?.requestDisallowInterceptTouchEvent(true)
                                    }
                                }

                                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                                    activity.setViewPagerSwipeEnabled(true)
                                    rv.parent?.requestDisallowInterceptTouchEvent(false)
                                    carousel.autoPlay =
                                        true // 🔹 Reactiva el auto-play cuando el usuario suelta
                                }
                            }
                            return false
                        }

                        override fun onTouchEvent(rv: RecyclerView, e: MotionEvent) {}

                        override fun onRequestDisallowInterceptTouchEvent(disallowIntercept: Boolean) {}
                    })
                } else {
                    Log.e("ImageCarousel", "No se pudo obtener el RecyclerView")
                }
            }
        })
    }


    private fun initScanner() {
        val integrator = IntentIntegrator.forSupportFragment(this)
        integrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE)
        integrator.setPrompt("Escanea un código QR")
        integrator.setCameraId(0)
        integrator.setBeepEnabled(false)
        integrator.setBarcodeImageEnabled(true)
        integrator.initiateScan()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        val result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data)

        if (result != null) {
            if (result.contents == null) {
                Toast.makeText(requireContext(), "Escaneo cancelado", Toast.LENGTH_SHORT).show()
            } else {
                val db = FirebaseFirestore.getInstance()
                    .collection(Variables.trabajadores_usuariosDB)
                    .document(Variables.trabajadoresDB)
                    .collection(Variables.trabajadoresDB)
                    .document(result.contents)


                db.get().addOnSuccessListener { res ->
                    if (res.exists()) {
                        val data = res.data
                        val id = data?.get(Variables.id) as? String ?: ""

                        if (id == result.contents) {
                            val nombreUser = data?.get(Variables.nombre) as? String ?: ""
                            val nacionalidad = data?.get(Variables.nacionalidad) as? String ?: ""
                            val categoria = data?.get(Variables.categoriaTrabajo) as? String ?: ""
                            val img = data?.get(Variables.imagenPerfil) as? String ?: ""
                            val intent =
                                Intent(requireContext(), vistaTrabajador::class.java).apply {

                                    putExtra(Variables.id, result.contents)
                                    putExtra(Variables.nombreUSer, nombreUser)
                                    putExtra(Variables.nacionalidad, nacionalidad)
                                    putExtra(Variables.categoria, categoria)
                                    putExtra(Variables.imagenPerfil, img)
                                }

                            startActivity(intent)
                        } else {
                            Toast.makeText(
                                requireContext(),
                                "Trabajador no encontrado",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    } else {
                        Toast.makeText(
                            requireContext(),
                            "Documento no encontrado en la base de datos",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }.addOnFailureListener { exception ->
                    Toast.makeText(
                        requireContext(),
                        "Error al obtener datos: ${exception.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }


    private fun confSwipe(storedValue: String) {
        binding.swipe.setOnRefreshListener {
            binding.swipe.setColorSchemeResources(R.color.violeta)
            Handler(Looper.getMainLooper()).postDelayed({
                binding.swipe.isRefreshing = false
                SetAnuncios()
                obtenerTrabajosCat()
                obtnerFiltrado(binding.includeCabezero.filtradoUsuairo.text.toString())
                obtenerProductos_trabajadores()
                obterTrabajosRecientes_trabajadores()
                obtener_mensajes_destacados(binding.verificadoBoolean.text.toString())
            }, 2000)
            binding.swipe.isVisible = true
        }
    }

    private fun mostrarDatos(storedValue: String) {
        obtnerFiltrado(storedValue)
        binding.swipe.isVisible = true

    }


    private fun guardarShaderPref(pref: SharedPreferences?, valor: String) {
        val editor = pref?.edit()
        editor?.putString(KEY, valor)
        editor?.apply()
    }

    private fun obtnerFiltrado(filtrado: String) {
        //Mejores trabajadores
        constantesTrabajadoresTiendasInicioFragmet.obtenerMejoresTrabajadores(
            listaTrabajo,
            filtrado,
            mContex,
            binding
        )
        // Trabajadores generales por categoría
        constantesTrabajadoresTiendasInicioFragmet.obtenerTrabajoscategoria(
            binding,
            filtrado,
            mContex
        )

    }

    private fun obtenerTrabajosCat() {
        binding.progresCargaCat.isVisible = true
        binding.RecicleCategoria.isVisible = false
        val trabajos = mutableListOf<dataClassCategoriasInicio>()
        val db = FirebaseFirestore.getInstance()
        val collection = db.collection(Variables.categoriasDB).document(Variables.categoriasTrabajo)
        collection.get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val categorias = document.get(Variables.categoriasDB) as? ArrayList<String>
                    categorias?.let {
                        for (categoria in it) {
                            Log.d(Variables.categoriasDB, categoria)
                            constantesSubcategoriaszonasTiendas.obtenerImagenesCategorias(
                                Variables.IMG_CategoriasGeneral,
                                Variables.categroriasTrabajadores,
                                categoria,
                                onSuccess = { urlImg ->
                                    val data = dataClassCategoriasInicio(
                                        categoria,
                                        urlImg ?: "",
                                    )
                                    binding.progresCargaCat.isVisible = false
                                    binding.RecicleCategoria.isVisible = true
                                    trabajos.add(data)
                                    inicalizarREciocle(
                                        binding.RecicleCategoria,
                                        trabajos
                                    )

                                },
                                onFailure = { error ->
                                    binding.progresCargaCat.isVisible = false
                                    binding.noEncontradocat.isVisible = true
                                    binding.RecicleCategoria.isVisible = false
                                    println("Error al obtener las imágenes para $categoria: ${error.message}")
                                    inicalizarREciocle(
                                        binding.RecicleCategoria,
                                        trabajos
                                    )
                                }
                            )
                        }
                        inicalizarREciocle(binding.RecicleCategoria, trabajos)
                    }
                } else {
                    Log.d(Variables.categoriasDB, "El documento no existe o está vacío.")
                }
            }
            .addOnFailureListener { exception ->
                Log.e(Variables.categoriasDB, "Error al obtener las categorías: $exception")
            }
    }

    private fun SetAnuncios() {
        constantesPublicidad.obtenerAnunciosGeinz(
            (activity as MainActivity).getViewPager,// Pasa la propiedad viewPager de la Activity
            binding.carrusel,
            mContex,
            binding.includeCabezero.filtradoUsuairo
        )
        constantesPublicidad.obteneranunciostorage(binding.carruse2, binding.linealCaption, mContex)
        constantesPublicidad.obtenerAnunciosIniciosFragment(
            binding.IncludeAnunciosTercero.carrucel,
            mContex,
            Variables.anunciosTerceros
        )
        constantesPublicidad.obtenerAnunciosIniciosFragment(
            binding.IncludeAnunciosCuarto.carrucel,
            mContex,
            Variables.anunciosCuartos
        )
        constantesPublicidad.obtenerAnunciosIniciosFragment(
            binding.IncludeAnunciosQuinto.carrucel,
            mContex,
            Variables.anunciosQuintos
        )
        constantesPublicidad.obtenerAnunciosIniciosFragment(
            binding.IncludeAnunciosSexto.carrucel,
            mContex,
            Variables.anunciosSextos
        )
    }

    private fun enviarCategoria() {

        binding.verdesarrollo.setOnClickListener {
            iniciarVistaCategoriasT(
                binding.includeCabezero.filtradoUsuairo.text.toString(),
                Variables.Conductor_de_reparto
            )
        }
        binding.mejoresTrabajadeores.setOnClickListener {
            iniciarVistaCategoriasT(
                binding.includeCabezero.filtradoUsuairo.text.toString(),
                Variables.Mejores_Trabajadores
            )
        }
        binding.vermecanica.setOnClickListener {
            iniciarVistaCategoriasT(
                binding.includeCabezero.filtradoUsuairo.text.toString(),
                Variables.Servicios_de_Salud
            )
        }
        binding.verTrabajohogar.setOnClickListener {
            iniciarVistaCategoriasT(
                binding.includeCabezero.filtradoUsuairo.text.toString(),
                Variables.Construcción_y_hogar
            )
        }
        binding.vertrasnporte.setOnClickListener {
            iniciarVistaCategoriasT(
                binding.includeCabezero.filtradoUsuairo.text.toString(),
                Variables.Mecánicos
            )
        }
        binding.verTecnicos.setOnClickListener {
            iniciarVistaCategoriasT(
                binding.includeCabezero.filtradoUsuairo.text.toString(),
                Variables.Tecnicos
            )
        }
    }

    private fun iniciarVistaCategoriasT(filtrado: String, valor: String) {
        var intent = Intent(mContex, vista_CategoriasT::class.java)
        intent.putExtra(Variables.filtrado, filtrado)
        intent.putExtra(Variables.valor, valor)
        startActivity(intent)
    }

    private fun mandarvistaTrabajos(dataClassCategoriasInicio: dataClassCategoriasInicio) {
        var intent = Intent(mContex, vista_CategoriasT::class.java)
        intent.putExtra(Variables.valor, dataClassCategoriasInicio.cateogiria)
        startActivity(intent)
    }

    private fun inicalizarREciocle(
        recicleTrabajos: RecyclerView,
        lista: MutableList<dataClassCategoriasInicio>,
    ) {
        val recicle = recicleTrabajos
        recicle.layoutManager = LinearLayoutManager(mContex, LinearLayoutManager.HORIZONTAL, false)
        recicle.adapter = adapterTrabajosMostrados(lista) { dataClassCategoriasInicio ->
            mandarvistaTrabajos(dataClassCategoriasInicio)
        }
    }

    private fun obtenerImagenesFirestorage() {
        val db = FirebaseFirestore.getInstance().collection(Variables.anuncios)
            .document(Variables.anunciosprimarios_cortos).collection(Variables.anuncios)
        val lista = mutableListOf<CarouselItem>()

        db.get().addOnSuccessListener { res ->
            for (datos in res) {
                val data = datos.data
                val URLimg = data?.get(Variables.URLimg) as? String ?: ""
                val carouselItem = CarouselItem(URLimg)
                lista.add(carouselItem)
            }

            binding.carruselPimarioInicio.registerLifecycle(lifecycle)
            binding.carruselPimarioInicio.carouselListener = object : CarouselListener {
                override fun onCreateViewHolder(
                    layoutInflater: LayoutInflater,
                    parent: ViewGroup,
                ): ViewBinding? {
                    return ItemCustomPublicidadPrincipalBinding.inflate(
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
                    val currentBinding = binding as ItemCustomPublicidadPrincipalBinding

                    val currentItem = lista[position]
                    val currentData = res.documents[position].data
                    val currentTitulo = currentData?.get(Variables.titulo) as? String ?: ""
                    val currentDescripcion =
                        currentData?.get(Variables.descripcion) as? String ?: ""
                    val currentURLimg = currentData?.get(Variables.URLimg) as? String ?: ""
                    val currentId = currentData?.get(Variables.id) as? String ?: ""

                    currentBinding.titulos.text = currentTitulo
                    currentBinding.descripcion.text = currentDescripcion

                    constatnes_carga_imagenes_general.changer_img(
                        currentBinding.progressCargaImagenFondo,
                        mContex,
                        currentURLimg,
                        null,
                        currentBinding.imgPublicidad,
                        "portada",
                        null
                    ) {}

                    currentBinding.realtiveClikc.setOnClickListener {

                        val vista = Intent(mContex, oferta_principales_geinz::class.java).apply {
                            putExtra(Variables.idPublicidad, currentId)
                        }
                        startActivity(vista)

                    }
                }
            }
            binding.carruselPimarioInicio.setData(lista)

        }.addOnFailureListener { e ->
            println("Error al obtener los datos: $e")
        }

    }

    private fun obterTrabajosRecientes_trabajadores() {
        val lista = mutableListOf<CarouselItem>()
        val documentosFirestore = mutableListOf<DocumentSnapshot>()
        val idTrabajadoresPorDocumento = mutableListOf<String>()

        val db = FirebaseFirestore.getInstance()
            .collection("solicitudes_servicios")
            .document("verificaciones")
            .collection("activos")

        db.get().addOnSuccessListener { res ->
            val trabajadores = res.documents
            if (trabajadores.isEmpty()) {
                binding.carrucelPublicacionesRecientes.isVisible = false
                binding.linealNoEncontrado.isVisible = true
                binding.noEncontrado.text = "No se encontraron publicaciones"
                return@addOnSuccessListener
            }

            var trabajadoresProcesados = 0

            for (document in trabajadores) {
                val id_trabajador_actual = document.id
                Log.d("trabajodenocntrad", id_trabajador_actual)

                val dbUsers = FirebaseFirestore.getInstance()
                    .collection("Trabajadores_Usuarios_Drivers")
                    .document("trabajadores")
                    .collection("trabajadores")
                    .document(id_trabajador_actual)
                    .collection("publicaciones_trabajos")
                    .document("publicados")
                    .collection("publicados")

                dbUsers.get().addOnSuccessListener { productos ->
                    if (!productos.isEmpty) {
                        val aleatorio = productos.documents.random()
                        val img_producto = aleatorio.get("img_url") as? String ?: ""
                        Log.d("la_img_encontrada", img_producto)

                        lista.add(CarouselItem(img_producto))
                        documentosFirestore.add(aleatorio)
                        idTrabajadoresPorDocumento.add(id_trabajador_actual)
                    }

                    trabajadoresProcesados++

                    if (trabajadoresProcesados == trabajadores.size) {
                        if (lista.isNotEmpty()) {
                            binding.carrucelPublicacionesRecientes.isVisible = true
                            binding.linealNoEncontrado.isVisible = false
                            configurarCarruselPublicaicones_ralziadas(
                                lista,
                                documentosFirestore,
                                idTrabajadoresPorDocumento
                            )
                        } else {
                            binding.carrucelPublicacionesRecientes.isVisible = false
                            binding.linealNoEncontrado.isVisible = true
                            binding.noEncontrado.text = "No se encontraron publicaciones"
                        }
                    }

                }.addOnFailureListener {
                    trabajadoresProcesados++

                    if (trabajadoresProcesados == trabajadores.size) {
                        if (lista.isNotEmpty()) {
                            binding.carrucelPublicacionesRecientes.isVisible = true
                            binding.linealNoEncontrado.isVisible = false
                            configurarCarruselPublicaicones_ralziadas(
                                lista,
                                documentosFirestore,
                                idTrabajadoresPorDocumento
                            )
                        } else {
                            binding.carrucelPublicacionesRecientes.isVisible = false
                            binding.linealNoEncontrado.isVisible = true
                            binding.noEncontrado.text = "No se encontraron publicaciones"
                        }
                    }
                }
            }

        }.addOnFailureListener { e ->
            Log.e("ProductosVerificados", "Error al obtener documentos", e)
            binding.carrucelPublicacionesRecientes.isVisible = false
            binding.linealNoEncontrado.isVisible = true
            binding.noEncontrado.text = "No se encontraron publicaciones"
        }
    }


    private fun obtenerProductos_trabajadores() {
        val lista = mutableListOf<CarouselItem>()
        val documentosFirestore = mutableListOf<DocumentSnapshot>()
        val idTrabajadoresPorDocumento = mutableListOf<String>()

        val db = FirebaseFirestore.getInstance().collection("solicitudes_servicios")
            .document("verificaciones").collection("activos")

        db.get().addOnSuccessListener { res ->
            val trabajadores = res.documents
            if (trabajadores.isEmpty()) {
                binding.carrucelProductosTrabajdores.isVisible = false
                binding.linealNoEncontradoProductos.isVisible = true
                binding.noEncontradoProducto.text = "No se encontraron productos"
                return@addOnSuccessListener
            }

            var trabajadoresProcesados = 0

            for (document in trabajadores) {
                val id_trabajador = document.id

                val dbUsers = FirebaseFirestore.getInstance()
                    .collection("Trabajadores_Usuarios_Drivers")
                    .document("trabajadores")
                    .collection("trabajadores")
                    .document(id_trabajador)
                    .collection("productos_venta")
                    .document("publicados")
                    .collection("publicados")

                dbUsers.get().addOnSuccessListener { productos ->
                    if (!productos.isEmpty) {
                        val aleatorio = productos.documents.random()
                        val img_producto = aleatorio.get("img_url") as? String ?: ""

                        lista.add(CarouselItem(img_producto))
                        documentosFirestore.add(aleatorio)
                        idTrabajadoresPorDocumento.add(id_trabajador)
                    }

                    trabajadoresProcesados++

                    if (trabajadoresProcesados == trabajadores.size) {
                        if (lista.isNotEmpty()) {
                            binding.carrucelProductosTrabajdores.isVisible = true
                            binding.linealNoEncontradoProductos.isVisible = false
                            configurarCarrusel(idTrabajadoresPorDocumento, lista, documentosFirestore)
                        } else {
                            binding.carrucelProductosTrabajdores.isVisible = false
                            binding.linealNoEncontradoProductos.isVisible = true
                            binding.noEncontradoProducto.text = "No se encontraron productos"
                        }
                    }

                }.addOnFailureListener {
                    trabajadoresProcesados++

                    if (trabajadoresProcesados == trabajadores.size) {
                        if (lista.isNotEmpty()) {
                            binding.carrucelProductosTrabajdores.isVisible = true
                            binding.linealNoEncontradoProductos.isVisible = false
                            configurarCarrusel(idTrabajadoresPorDocumento, lista, documentosFirestore)
                        } else {
                            binding.carrucelProductosTrabajdores.isVisible = false
                            binding.linealNoEncontradoProductos.isVisible = true
                            binding.noEncontradoProducto.text = "No se encontraron productos"
                        }
                    }
                }
            }

        }.addOnFailureListener { e ->
            Log.e("ProductosVerificados", "Error al obtener documentos", e)
            binding.carrucelProductosTrabajdores.isVisible = false
            binding.linealNoEncontradoProductos.isVisible = true
            binding.noEncontradoProducto.text = "No se encontraron productos"
        }
    }


    private fun configurarCarrusel(
        id_trabajador: List<String>,
        lista: List<CarouselItem>,
        documentos: List<DocumentSnapshot>,
    ) {
        binding.carrucelProductosTrabajdores.registerLifecycle(lifecycle)
        binding.carrucelProductosTrabajdores.carouselListener = object : CarouselListener {
            override fun onCreateViewHolder(
                layoutInflater: LayoutInflater,
                parent: ViewGroup,
            ): ViewBinding? {
                return ItemProductsTrabajadoresPrincipalBinding.inflate(
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
                val currentBinding = binding as ItemProductsTrabajadoresPrincipalBinding
                val doc = documentos[position]
                val id_trabajador = id_trabajador[position]
                val titulo: String = doc.get("nombre") as? String ?: ""
                val descripcionTitulo = doc["descripcion_titulo"] as? Map<String, Any> ?: emptyMap()
                val tituloDescripcion = descripcionTitulo["titulo_descripcion"] as? String ?: ""
                val img_producto: String = doc.get("img_url") as? String ?: ""
                val precio: Number = doc.get("precio") as? Number ?: 0
                val descuento_porcentajeProducto: Number =
                    doc.get("cantidad_porcentaje_descuento") as? Number ?: 0
                val descuentoBoolean: Boolean = doc.get("descuento") as? Boolean ?: false
                val precio_descuento: Number = doc.get("precio_descuento") as? Number ?: 0
                val metodoEntrega: String= doc.get("metodoEntrega") as? String ?: ""
                val id: String = doc.get("id") as? String ?: ""

                constantes_servicios.verificarEstado_vericiacion(
                    currentBinding.verificado,
                    id_trabajador
                ) { v, plan ->
                    when (plan) {
                        Variables.plaA -> {
                            currentBinding.verificado.setImageResource(R.drawable.verificado_a)

                        }

                        Variables.planB -> {
                            currentBinding.verificado.setImageResource(R.drawable.icon_verificado)
                        }

                        Variables.PlanC -> {
                            currentBinding.verificado.setImageResource(R.drawable.verificado_c)

                        }
                    }

                }

                constantesCarrito.setearDatosUsuarioImgNombre(id_trabajador) { nombre, img, apellido, nacionalidad, categoria, verificado, trabajador_user ->
                    currentBinding.NombreVerificado.text = nombre
                }

                currentBinding.tituloProducto.text = titulo
                currentBinding.descripcionProducto.text = tituloDescripcion

                if (descuentoBoolean) {
                    constantestextos_general.setearPrecioDescuentoPrecioAntiguo(
                        precio_descuento,
                        currentBinding.precioProducto,
                        precio,
                        currentBinding.precioDescuento,
                        descuento_porcentajeProducto,
                        currentBinding.descuentoPorcentaje
                    )
                    currentBinding.precioDescuento.isVisible = true
                    currentBinding.descuentoPorcentaje.isVisible = true
                    constantestextos_general.marcarDescuentoTxt(currentBinding.precioDescuento)
                } else {
                    constantestextos_general.setearPrecioDescuentoPrecioAntiguo(
                        precio,
                        currentBinding.precioProducto
                    )
                    currentBinding.precioDescuento.isVisible = false
                    currentBinding.descuentoPorcentaje.isVisible = false
                }
                obtener_metodoEntrega(
                    id_trabajador, metodoEntrega,
                    callback = { metodo_entrega ->
                    },
                    evio_gratis = { delivery_gratis ->
                        if (delivery_gratis) {
                            currentBinding.envioGratis.isVisible = true
                        } else {
                            currentBinding.envioGratis.isVisible = false
                        }
                    }
                )


                currentBinding.cargarContenido.postDelayed({
                    currentBinding.imgProducto.isVisible = true
                    currentBinding.cargarContenido.isVisible = false
                    currentBinding.linealProductosPublicados.isVisible = true
                    currentBinding.descuentoPorcentaje.isVisible = descuentoBoolean
                }, 2000)
                constatnes_carga_imagenes_general.changer_img(
                    currentBinding.cargaImg,
                    mContex,
                    img_producto,
                    null,
                    currentBinding.imgProducto as ImageView,
                    "portada",
                    null
                ) {}
                currentBinding.listener.setOnClickListener {
                    val vista =
                        Intent(mContex, vista_ver_productos_trabajadores::class.java).apply {
                            putExtra("id_trabajador", id_trabajador)
                                .putExtra("id_publicacion", id)
                        }
                    startActivity(vista)
                }
            }
        }

        binding.carrucelProductosTrabajdores.setData(lista)
    }

    private fun configurarCarruselPublicaicones_ralziadas(
        lista: List<CarouselItem>,
        documentos: List<DocumentSnapshot>,
        idTrabajadores: List<String>,
    ) {
        Log.d("DEBUG_CARRUSEL", "Lista CarouselItem (tamaño: ${lista.size}):")
        lista.forEachIndexed { index, item ->
            Log.d("DEBUG_CARRUSEL", "[$index] imageUrl: ${item.imageUrl}")
        }

        Log.d("DEBUG_CARRUSEL", "Lista Documentos Firestore (tamaño: ${documentos.size}):")
        documentos.forEachIndexed { index, doc ->
            Log.d("DEBUG_CARRUSEL", "[$index] ID Publicación: ${doc.getString("id")}, Contenido: ${doc.getString("contenido")}")
        }

        Log.d("DEBUG_CARRUSEL", "Lista ID de Trabajadores (tamaño: ${idTrabajadores.size}):")
        idTrabajadores.forEachIndexed { index, id ->
            Log.d("DEBUG_CARRUSEL", "[$index] ID trabajador: $id")
        }

        binding.carrucelPublicacionesRecientes.registerLifecycle(lifecycle)
        binding.carrucelPublicacionesRecientes.carouselListener = object : CarouselListener {
            override fun onCreateViewHolder(
                layoutInflater: LayoutInflater,
                parent: ViewGroup,
            ): ViewBinding? {
                return ItemPublicaiconesRecientesTrabajadoresInicioFragmentBinding.inflate(
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
                val currentBinding =
                    binding as ItemPublicaiconesRecientesTrabajadoresInicioFragmentBinding
                val doc = documentos[position]
                val id_trabajador = idTrabajadores[position] // <- Ahora sí es el correcto
                val contenidoPublicacion: String = doc.get("contenido") as? String ?: ""
                val img_url: String = doc.get("img_url") as? String ?: ""
                val id: String = doc.get("id") as? String ?: ""

                Log.d("obtenosimgtrabajdor", id_trabajador)

                constantes_servicios.verificarEstado_vericiacion(
                    currentBinding.verificado,
                    id_trabajador
                ) { v, plan ->
                    when (plan) {
                        Variables.plaA -> {
                            currentBinding.verificado.setImageResource(R.drawable.verificado_a)

                        }

                        Variables.planB -> {
                            currentBinding.verificado.setImageResource(R.drawable.icon_verificado)
                        }

                        Variables.PlanC -> {
                            currentBinding.verificado.setImageResource(R.drawable.verificado_c)

                        }
                    }

                }

                constantesTrabajadoresTiendasInicioFragmet.obtnerIMG_trabajador(
                    id_trabajador,
                    currentBinding.imgPerfil,
                    currentBinding.cargaimg,
                    mContex
                )
                constatnes_carga_imagenes_general.changer_img(
                    binding.cargaContenido,
                    mContex,
                    img_url,
                    null,
                    currentBinding.imgPublicidad as ImageView,
                    "portada",
                    null
                ) {}
                currentBinding.realtiveClikc.setOnClickListener {
                    val vista =
                        Intent(mContex, vista_ver_publicaciones_trabajadores::class.java).apply {
                            putExtra("id_trabajador", id_trabajador)
                                .putExtra("id_publicacion", id)
                        }
                    startActivity(vista)
                }

                currentBinding.contenidoPublicacion.text = contenidoPublicacion
            }
        }

        binding.carrucelPublicacionesRecientes.setData(lista)
    }


}


