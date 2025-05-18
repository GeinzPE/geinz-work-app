package com.geinzz.geinzwork.servicios_geinz

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import com.bumptech.glide.Glide
import com.example.geinzwork.constantesGeneral.Variables
import com.example.geinzwork.fragmentos.panel_publicacion_trabajador
import com.geinzz.geinzwork.R
import com.geinzz.geinzwork.constantesGeneral.constantes
import com.geinzz.geinzwork.constantesGeneral.constantesCarrito

import com.geinzz.geinzwork.constantesGeneral.mostrarFechaDialog_horaDialog
import com.geinzz.geinzwork.crear_trabajos_realizados
import com.geinzz.geinzwork.databinding.BottomshetetDetallesServiciosBinding
import com.geinzz.geinzwork.databinding.FragmentServiciosActivosBinding
import com.geinzz.geinzwork.planes
import com.geinzz.geinzwork.vistaTrabajador.ver_detalles_Promociones
import com.geinzz.geinzwork.vistaTrabajador.vistaTrabajador
import com.geinzz.geinzwork.vistas_anuncios_general
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.formatter.ValueFormatter
import com.github.mikephil.charting.utils.ColorTemplate
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.errorprone.annotations.Var
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore


class servicios_activos : Fragment() {

    private lateinit var binding: FragmentServiciosActivosBinding
    private lateinit var mContext: Context
    private lateinit var firebaseAuth: FirebaseAuth
    override fun onAttach(context: Context) {
        super.onAttach(context)
        mContext = context
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        binding = FragmentServiciosActivosBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        firebaseAuth = FirebaseAuth.getInstance()

        binding.noticia.tituloServico.text = getString(R.string.servicioNoticia)
        binding.baner.tituloServico.text = getString(R.string.servicioAnuncio)
        binding.verificacion.tituloServico.text = getString(R.string.verificado)
        binding.verificacion.imgServicio.setImageResource(R.drawable.geinz_verificada)
        confSwipe()
        val dbNoticias =
            FirebaseFirestore.getInstance().collection(Variables.solicitudes_serviciosDB)
                .document(Variables.publicidadNoticiasDB).collection(Variables.activos)
                .document(firebaseAuth.uid.toString())

        val dbBaner = FirebaseFirestore.getInstance().collection(Variables.solicitud_servicios)
            .document(Variables.publicidadBanerDB).collection(Variables.activos)
            .document(firebaseAuth.uid.toString())

        val dbVerificado = FirebaseFirestore.getInstance().collection(Variables.solicitud_servicios)
            .document(Variables.verificacionesDB).collection(Variables.activos)
            .document(firebaseAuth.uid.toString())


        ver_servicios_activos(
            firebaseAuth.uid.toString(),
            noticiaActiva = { activado ->
                if (activado) {
                    binding.noticia.listener.isVisible = true
                    Log.d(
                        "activoAnuncio",
                        "${firebaseAuth.uid.toString()}tiene un anuncio activo noticia"
                    )


                    obtener_datos_noticia("noticia", dbNoticias) { doc1, doc2, plan ->
                        val db = FirebaseFirestore.getInstance().collection(doc1).document(doc2)
                        obtenerImgServicios("noticia", db)
                        binding.noticia.verPreview.setOnClickListener {
                            db.get().addOnSuccessListener { res ->
                                if (res.exists()) {
                                    val data = res.data
                                    val idAnuncio = data?.get("id") as? String ?: ""
                                    val intent =
                                        Intent(context, ver_detalles_Promociones::class.java)
                                    intent.putExtra("idAnuncio", idAnuncio)
                                    intent.putExtra("entrada", "noticia")
                                    startActivity(intent)
                                } else {
                                    println("no se econtro al usuario")
                                }
                            }.addOnFailureListener { e ->
                                println("no se econtro al usuario $e")
                            }
                        }

                    }

                    binding.noticia.listener.setOnClickListener {
                        val intent = Intent(mContext, noticias_servicios_geinz_activity::class.java)
                        startActivity(intent)
                    }
                    binding.noticia.detallesBoleta.setOnClickListener {
                        obtener_datos_noticia("noticia", dbNoticias) { doc1, doc2, plan ->
                            val db = FirebaseFirestore.getInstance().collection(doc1).document(doc2)
                            obtenerFormPagosUser(
                                db,
                                "noticia",
                                firebaseAuth.uid.toString(),
                                "publicidad_noticia"
                            )

                        }
                    }

                } else {
                    binding.noticia.listener.isVisible = false
                }
            },
            banerActiva = { activado ->
                if (activado) {
                    Log.d(
                        "activoAnuncio",
                        "${firebaseAuth.uid.toString()}tiene un anuncio activo baner"
                    )
                    obtener_datos_noticia("baner", dbBaner) { doc1, doc2, plan ->
                        val db =
                            FirebaseFirestore.getInstance().collection("anuncios").document(doc1)
                                .collection("anuncios").document(doc2)
                        obtenerImgServicios("publicidad", db)
                        binding.baner.verPreview.setOnClickListener {
                            val Intent =
                                Intent(context, vistas_anuncios_general::class.java).apply {
                                    putExtra("anuncio", doc2)
                                    putExtra("docuemnto", doc1)
                                    putExtra("mensaje", "cargando...")

                                }
                            startActivity(Intent)
                        }


                    }
                    binding.baner.listener.isVisible = true
                    binding.baner.listener.setOnClickListener {
                        val intent =
                            Intent(mContext, publicidad_servicios_geinz_activity::class.java)
                        startActivity(intent)
                    }
                    binding.baner.detallesBoleta.setOnClickListener {
                        obtener_datos_noticia("baner", dbBaner) { doc1, doc2, plan ->
                            val db =
                                FirebaseFirestore.getInstance().collection("anuncios")
                                    .document(doc1)
                                    .collection("anuncios").document(doc2)
                            obtenerFormPagosUser(
                                db,
                                "baner",
                                firebaseAuth.uid.toString(),
                                "publicidad_baner"
                            )

                        }
                    }
                } else {
                    binding.baner.listener.isVisible = false
                }

            },
            Verificado = { activo ->
                if (activo) {
                    Log.d(
                        "activoAnuncio",
                        "${firebaseAuth.uid.toString()}tiene un anuncio activo verificado"
                    )
                    val db = FirebaseFirestore.getInstance()
                        .collection(Variables.trabajadores_usuariosDB)
                        .document(Variables.trabajadoresDB)
                        .collection(Variables.trabajadoresDB)
                        .document(firebaseAuth.uid.toString())
                    db.get().addOnSuccessListener { res ->
                        if (res.exists()) {
                            val data = res.data
                            val nombreUSer = data?.get("nombreUSer") as? String ?: ""
                            val nacionalidad = data?.get("nacionalidad") as? String ?: ""
                            val categoria = data?.get("categoria") as? String ?: ""
                            val imagenPerfil = data?.get("imagenPerfil") as? String ?: ""
                            binding.verificacion.verPreview.setOnClickListener {
                                val vista = Intent(context, vistaTrabajador::class.java).apply {
                                    putExtra("id", firebaseAuth.uid.toString())
                                    putExtra("nombreUSer", nombreUSer)
                                    putExtra("nacionalidad", nacionalidad)
                                    putExtra("categoria", categoria)
                                    putExtra("imagenPerfil", imagenPerfil)

                                }
                                startActivity(vista)
                            }
                        } else {
                            println("no se econtro al usuario")
                        }
                    }.addOnFailureListener { e ->
                        println("no se econtro al usuario $e")
                    }

                    obtener_datos_noticia("verificado", dbVerificado) { doc1, doc2, plan ->
                        binding.verificacion.listener.setOnClickListener {
                            if (plan == "A") {
                                Toast.makeText(
                                    mContext,
                                    "Plan de verificacion A sin acceso",
                                    Toast.LENGTH_SHORT
                                ).show()
                            } else if (plan == "B" || plan == "C") {
                                val intent =
                                    Intent(mContext, panel_publicacion_trabajador::class.java).apply {
                                        putExtra("plan", plan)
                                    }
                                startActivity(intent)
                            }


                        }


                    }
                    binding.verificacion.listener.isVisible = true

                    binding.verificacion.detallesBoleta.setOnClickListener {
                        obtener_datos_noticia("verificado", dbVerificado) { doc1, doc2, plan ->
                            obtenerFormPagosUser(
                                dbVerificado,
                                "verificado",
                                firebaseAuth.uid.toString(),
                                "verificaciones"
                            )

                        }
                    }
                } else {
                    binding.verificacion.listener.isVisible = false
                }
            }
        )

    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun confSwipe() {
        binding.swipe.setOnRefreshListener {
            binding.swipe.setColorSchemeResources(R.color.violeta)
            Handler(Looper.getMainLooper()).postDelayed({
                binding.swipe.isRefreshing = false
                val dbNoticias =
                    FirebaseFirestore.getInstance().collection(Variables.solicitudes_serviciosDB)
                        .document(Variables.publicidadNoticiasDB).collection(Variables.activos)
                        .document(firebaseAuth.uid.toString())

                val dbBaner = FirebaseFirestore.getInstance().collection(Variables.solicitud_servicios)
                    .document(Variables.publicidadBanerDB).collection(Variables.activos)
                    .document(firebaseAuth.uid.toString())

                val dbVerificado = FirebaseFirestore.getInstance().collection(Variables.solicitud_servicios)
                    .document(Variables.verificacionesDB).collection(Variables.activos)
                    .document(firebaseAuth.uid.toString())
                ver_servicios_activos(
                    firebaseAuth.uid.toString(),
                    noticiaActiva = { activado ->
                        if (activado) {
                            binding.noticia.listener.isVisible = true
                            Log.d(
                                "activoAnuncio",
                                "${firebaseAuth.uid.toString()}tiene un anuncio activo noticia"
                            )


                            obtener_datos_noticia("noticia", dbNoticias) { doc1, doc2, plan ->
                                val db = FirebaseFirestore.getInstance().collection(doc1).document(doc2)
                                obtenerImgServicios("noticia", db)
                                binding.noticia.verPreview.setOnClickListener {
                                    db.get().addOnSuccessListener { res ->
                                        if (res.exists()) {
                                            val data = res.data
                                            val idAnuncio = data?.get("id") as? String ?: ""
                                            val intent =
                                                Intent(context, ver_detalles_Promociones::class.java)
                                            intent.putExtra("idAnuncio", idAnuncio)
                                            intent.putExtra("entrada", "noticia")
                                            startActivity(intent)
                                        } else {
                                            println("no se econtro al usuario")
                                        }
                                    }.addOnFailureListener { e ->
                                        println("no se econtro al usuario $e")
                                    }
                                }

                            }

                            binding.noticia.listener.setOnClickListener {
                                val intent = Intent(mContext, noticias_servicios_geinz_activity::class.java)
                                startActivity(intent)
                            }
                            binding.noticia.detallesBoleta.setOnClickListener {
                                obtener_datos_noticia("noticia", dbNoticias) { doc1, doc2, plan ->
                                    val db = FirebaseFirestore.getInstance().collection(doc1).document(doc2)
                                    obtenerFormPagosUser(
                                        db,
                                        "noticia",
                                        firebaseAuth.uid.toString(),
                                        "publicidad_noticia"
                                    )

                                }
                            }

                        } else {
                            binding.noticia.listener.isVisible = false
                        }
                    },
                    banerActiva = { activado ->
                        if (activado) {
                            Log.d(
                                "activoAnuncio",
                                "${firebaseAuth.uid.toString()}tiene un anuncio activo baner"
                            )

                            obtener_datos_noticia("baner", dbBaner) { doc1, doc2, plan ->
                                val db =
                                    FirebaseFirestore.getInstance().collection("anuncios").document(doc1)
                                        .collection("anuncios").document(doc2)
                                obtenerImgServicios("publicidad", db)
                                binding.baner.verPreview.setOnClickListener {
                                    val Intent =
                                        Intent(context, vistas_anuncios_general::class.java).apply {
                                            putExtra("anuncio", doc2)
                                            putExtra("docuemnto", doc1)
                                            putExtra("mensaje", "cargando...")

                                        }
                                    startActivity(Intent)
                                }


                            }
                            binding.baner.listener.isVisible = true
                            binding.baner.listener.setOnClickListener {
                                val intent =
                                    Intent(mContext, publicidad_servicios_geinz_activity::class.java)
                                startActivity(intent)
                            }
                            binding.baner.detallesBoleta.setOnClickListener {
                                obtener_datos_noticia("baner", dbBaner) { doc1, doc2, plan ->
                                    val db =
                                        FirebaseFirestore.getInstance().collection("anuncios")
                                            .document(doc1)
                                            .collection("anuncios").document(doc2)
                                    obtenerFormPagosUser(
                                        db,
                                        "baner",
                                        firebaseAuth.uid.toString(),
                                        "publicidad_baner"
                                    )

                                }
                            }
                        } else {
                            binding.baner.listener.isVisible = false
                        }

                    },
                    Verificado = { activo ->
                        if (activo) {
                            Log.d(
                                "activoAnuncio",
                                "${firebaseAuth.uid.toString()}tiene un anuncio activo verificado"
                            )
                            val db = FirebaseFirestore.getInstance()
                                .collection(Variables.trabajadores_usuariosDB)
                                .document(Variables.trabajadoresDB)
                                .collection(Variables.trabajadoresDB)
                                .document(firebaseAuth.uid.toString())
                            db.get().addOnSuccessListener { res ->
                                if (res.exists()) {
                                    val data = res.data
                                    val nombreUSer = data?.get("nombreUSer") as? String ?: ""
                                    val nacionalidad = data?.get("nacionalidad") as? String ?: ""
                                    val categoria = data?.get("categoria") as? String ?: ""
                                    val imagenPerfil = data?.get("imagenPerfil") as? String ?: ""
                                    binding.verificacion.verPreview.setOnClickListener {
                                        val vista = Intent(context, vistaTrabajador::class.java).apply {
                                            putExtra("id", firebaseAuth.uid.toString())
                                            putExtra("nombreUSer", nombreUSer)
                                            putExtra("nacionalidad", nacionalidad)
                                            putExtra("categoria", categoria)
                                            putExtra("imagenPerfil", imagenPerfil)

                                        }
                                        startActivity(vista)
                                    }
                                } else {
                                    println("no se econtro al usuario")
                                }
                            }.addOnFailureListener { e ->
                                println("no se econtro al usuario $e")
                            }

                            obtener_datos_noticia("verificado", dbVerificado) { doc1, doc2, plan ->
                                binding.verificacion.listener.setOnClickListener {
                                    if (plan == "A") {
                                        Toast.makeText(
                                            mContext,
                                            "Plan de verificacion A sin acceso",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    } else if (plan == "B" || plan == "C") {
                                        val intent =
                                            Intent(mContext, crear_trabajos_realizados::class.java).apply {
                                                putExtra("plan", plan)
                                            }
                                        startActivity(intent)
                                    }


                                }


                            }
                            binding.verificacion.listener.isVisible = true

                            binding.verificacion.detallesBoleta.setOnClickListener {
                                obtener_datos_noticia("verificado", dbVerificado) { doc1, doc2, plan ->
                                    obtenerFormPagosUser(
                                        dbVerificado,
                                        "verificado",
                                        firebaseAuth.uid.toString(),
                                        "verificaciones"
                                    )

                                }
                            }
                        } else {
                            binding.verificacion.listener.isVisible = false
                        }
                    }
                )
            }, 2000)
        }
    }


    @RequiresApi(Build.VERSION_CODES.O)
    private fun obtenerFormPagosUser(
        db: DocumentReference,
        tipo: String,
        idUser: String,
        documento: String,
    ) {

        fun mostrarDatosBaner(
            bindingBottomSheet: BottomshetetDetallesServiciosBinding,
            clicks: Int,
            vistas: Int,
            compartidas: Int
        ) {
            val pieChart = bindingBottomSheet.vistasCompartilasCliks

            val entries = ArrayList<PieEntry>()
            entries.add(PieEntry(clicks.toFloat(), "Clics"))
            entries.add(PieEntry(vistas.toFloat(), "Vistas"))
            entries.add(PieEntry(compartidas.toFloat(), "Compartidos"))


            val dataSet = PieDataSet(entries, "").apply {
                colors = ColorTemplate.COLORFUL_COLORS.toList()
                valueTextColor = ContextCompat.getColor(mContext, R.color.heartOutlineColor)
                valueTextSize = 12f // Tamaño del texto
            }


            val data = PieData(dataSet)

            data.setValueFormatter(object : ValueFormatter() {
                override fun getFormattedValue(value: Float): String {
                    return value.toInt().toString()
                }
            })

            bindingBottomSheet.vistasCompartilasCliks.apply {
                description.isEnabled = false
                legend.apply {
                    isEnabled = true
                    textColor = ContextCompat.getColor(
                        context,
                        R.color.heartOutlineColor
                    ) // Cambiar el color del texto de la leyenda
                }
                setUsePercentValues(true) // Mostrar los valores en porcentaje
                setExtraOffsets(5f, 10f, 5f, 5f) // Agregar márgenes adicionales
                animateY(1000) // Animación de entrada
                invalidate() // Refrescar el gráfico
            }

            data.setValueTextSize(12f)
            data.setValueTextColor(Color.BLACK)
            pieChart.data = data
            pieChart.description.isEnabled = false
            pieChart.isDrawHoleEnabled = false
            pieChart.invalidate()

            bindingBottomSheet.clics.titulo.text = "Clics"
            bindingBottomSheet.compartidas.titulo.text = "compartidas"
            bindingBottomSheet.vistas.titulo.text = "vistas"
            if (clicks != 0 || compartidas != 0 || vistas != 0) {
                bindingBottomSheet.clics.contador.text = clicks.toString()
                bindingBottomSheet.compartidas.contador.text = compartidas.toString()
                bindingBottomSheet.vistas.contador.text = vistas.toString()
                bindingBottomSheet.vistasCompartilasCliks.isVisible = true
                bindingBottomSheet.noEncontradoClikcCompartidas.isVisible = false
            } else {
                bindingBottomSheet.vistasCompartilasCliks.isVisible = false
                bindingBottomSheet.noEncontradoClikcCompartidas.isVisible = true
            }


        }


        fun obtenerDatosAnuncioGenero_localidaes_cuentas(
            bindingBottomSheet: BottomshetetDetallesServiciosBinding,
        ) {
            db.get().addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val plan = document.get("plan") as? String ?: ""
                    Log.d("planObtenid", plan)
                    if (plan == "basico" || plan == "A") {
                        bindingBottomSheet.linelaEstadisticasGeneral.isVisible = false
                    } else if (plan == "premiun" || plan == "avanzado" || plan == "B" || plan == "C") {
                        bindingBottomSheet.linelaEstadisticasGeneral.isVisible = true
                        val estadisticas = document.get("estadisticas") as? Map<String, Any>
                        val genero =
                            estadisticas?.get("genero") as? Map<String, Number> ?: emptyMap()
                        val localidad =
                            estadisticas?.get("localidad") as? Map<String, Number> ?: emptyMap()
                        val tipoCuenta =
                            estadisticas?.get("tipo_cuenta") as? Map<String, Number> ?: emptyMap()
                        val clicks = document.get("estadisticas_click") as? Number ?: 0
                        val vistas = document.get("estadisticas_vistas") as? Number ?: 0
                        val compartidas = document.get("estadisticas_compartidas") as? Number ?: 0
                        mostrarDatosBaner(
                            bindingBottomSheet,
                            clicks.toInt(),
                            vistas.toInt(),
                            compartidas.toInt()
                        )
                        mostrarGraficoGenero(bindingBottomSheet, genero)
                        mostrarGraficoLocalidades(bindingBottomSheet, localidad)
                        mostrarGraficoCuentaTrabajadores(bindingBottomSheet, tipoCuenta)
                    }
                } else {
                    println("El documento no existe o está vacío")
                }
            }.addOnFailureListener { exception ->
                println("Error al obtener los datos: ${exception.message}")
            }
        }

        fun obtenerDatosNoticias(
            bindingBottomSheet: BottomshetetDetallesServiciosBinding,
        ) {
            db.get().addOnSuccessListener { res ->
                if (res.exists()) {
                    val datares = res.data
                    val plan = datares?.get("plan") as? String ?: ""
                    if (plan == "basico") {
                        bindingBottomSheet.linelaEstadisticasGeneral.isVisible = false
                    } else if (plan == "premiun" || plan == "avanzado") {
                        bindingBottomSheet.linelaEstadisticasGeneral.isVisible = true
                        val estadisticasClick =
                            (datares?.get("estadisticas_click") as? Number)?.toInt() ?: 0
                        val estadisticasCompartido =
                            (datares?.get("estadisticas_compartido") as? Number)?.toInt() ?: 0
                        val estadisticasGuardados =
                            (datares?.get("estadisticas_guardados") as? Number)?.toInt() ?: 0
                        val estadisticasLike =
                            (datares?.get("estadisticas_like") as? Number)?.toInt() ?: 0
                        val estadisticasVistas =
                            (datares?.get("estadisticas_vistas") as? Number)?.toInt() ?: 0
                        val pieChart = bindingBottomSheet.vistasCompartilasCliksNoticias
                        val entries = ArrayList<PieEntry>()
                        entries.add(PieEntry(estadisticasClick.toFloat(), "Clics"))
                        entries.add(PieEntry(estadisticasCompartido.toFloat(), "Compartidos"))
                        entries.add(PieEntry(estadisticasGuardados.toFloat(), "Guardados"))
                        entries.add(PieEntry(estadisticasLike.toFloat(), "likes"))
                        entries.add(PieEntry(estadisticasVistas.toFloat(), "vistas"))

                        val dataSet = PieDataSet(entries, "").apply {
                            colors = ColorTemplate.COLORFUL_COLORS.toList()
                            valueTextColor =
                                ContextCompat.getColor(mContext, R.color.heartOutlineColor)
                            valueTextSize = 12f
                        }
                        val data = PieData(dataSet)

                        data.setValueFormatter(object : ValueFormatter() {
                            override fun getFormattedValue(value: Float): String {
                                return value.toInt().toString()
                            }
                        })
                        bindingBottomSheet.vistasCompartilasCliksNoticias.apply {
                            description.isEnabled = false
                            legend.apply {
                                isEnabled = true
                                textColor = ContextCompat.getColor(
                                    context,
                                    R.color.heartOutlineColor
                                ) // Cambiar el color del texto de la leyenda
                            }
                            setUsePercentValues(true) // Mostrar los valores en porcentaje
                            setExtraOffsets(5f, 10f, 5f, 5f) // Agregar márgenes adicionales
                            animateY(1000) // Animación de entrada
                            invalidate() // Refrescar el gráfico
                        }

                        data.setValueTextSize(12f)
                        data.setValueTextColor(Color.BLACK)
                        pieChart.data = data
                        pieChart.description.isEnabled = false
                        pieChart.isDrawHoleEnabled = false
                        pieChart.invalidate()

                        bindingBottomSheet.clicsNoticias.titulo.text = "Clics"
                        bindingBottomSheet.compartidasNoticias.titulo.text = "compartidas"
                        bindingBottomSheet.vistasNoticias.titulo.text = "vistas"
                        bindingBottomSheet.guardadosNoticias.titulo.text = "guardados"
                        bindingBottomSheet.likeNoticias.titulo.text = "likes"
                        if (estadisticasClick != 0 || estadisticasCompartido != 0 || estadisticasGuardados != 0 || estadisticasLike != 0 || estadisticasVistas != 0) {
                            bindingBottomSheet.clicsNoticias.contador.text =
                                estadisticasClick.toString()
                            bindingBottomSheet.compartidasNoticias.contador.text =
                                estadisticasCompartido.toString()
                            bindingBottomSheet.vistasNoticias.contador.text =
                                estadisticasVistas.toString()
                            bindingBottomSheet.guardadosNoticias.contador.text =
                                estadisticasGuardados.toString()
                            bindingBottomSheet.likeNoticias.contador.text =
                                estadisticasLike.toString()
                            bindingBottomSheet.vistasCompartilasCliksNoticias.isVisible = true
                            bindingBottomSheet.noEcnotradoEstadisitcasNoticias.isVisible = false
                        } else {
                            bindingBottomSheet.vistasCompartilasCliksNoticias.isVisible = false
                            bindingBottomSheet.noEcnotradoEstadisitcasNoticias.isVisible = true
                        }
                    }

                }
            }.addOnFailureListener {
                Toast.makeText(
                    bindingBottomSheet.root.context,
                    "Error al cargar datos",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        fun obtenerVerificados(bindingBottomSheet: BottomshetetDetallesServiciosBinding) {
            db.get().addOnSuccessListener { res ->
                if (res.exists()) {
                    val datares = res.data
                    val plan = datares?.get("plan") as? String ?: ""
                    if (plan == "A") {
                        bindingBottomSheet.linelaEstadisticasGeneral.isVisible = false
                    } else if (plan == "B" || plan == "C") {
                        val estadisticasClick =
                            (datares?.get("estadisticas_click") as? Number)?.toInt() ?: 0
                        val estadisticas_compartidas =
                            (datares?.get("estadisticas_compartidas") as? Number)?.toInt() ?: 0
                        val estadisticas_contactadoWhatsapp =
                            (datares?.get("estadisticas_contactadoWhatsapp") as? Number)?.toInt()
                                ?: 0
                        val estadisticas_llamadas =
                            (datares?.get("estadisticas_llamadas") as? Number)?.toInt() ?: 0
                        val estadisticas_vistas =
                            (datares?.get("estadisticas_vistas") as? Number)?.toInt() ?: 0
                        val pieChart = bindingBottomSheet.vistasCompartilasCliksNoticias
                        val entries = ArrayList<PieEntry>()
                        entries.add(PieEntry(estadisticasClick.toFloat(), "Clics"))
                        entries.add(PieEntry(estadisticas_compartidas.toFloat(), "Compartidos"))
                        entries.add(PieEntry(estadisticas_contactadoWhatsapp.toFloat(), "Whatsapp"))
                        entries.add(PieEntry(estadisticas_llamadas.toFloat(), "Llamadas"))
                        entries.add(PieEntry(estadisticas_vistas.toFloat(), "vistas"))
                        val dataSet = PieDataSet(entries, "").apply {
                            colors = ColorTemplate.COLORFUL_COLORS.toList()
                            valueTextColor =
                                ContextCompat.getColor(mContext, R.color.heartOutlineColor)
                            valueTextSize = 12f
                        }
                        val data = PieData(dataSet)

                        data.setValueFormatter(object : ValueFormatter() {
                            override fun getFormattedValue(value: Float): String {
                                return value.toInt().toString()
                            }
                        })
                        bindingBottomSheet.vistasCompartilasCliksNoticias.apply {
                            description.isEnabled = false
                            legend.apply {
                                isEnabled = true
                                textColor = ContextCompat.getColor(
                                    context,
                                    R.color.heartOutlineColor
                                )
                            }
                            setUsePercentValues(true) // Mostrar los valores en porcentaje
                            setExtraOffsets(5f, 10f, 5f, 5f) // Agregar márgenes adicionales
                            animateY(1000) // Animación de entrada
                            invalidate() // Refrescar el gráfico
                        }

                        data.setValueTextSize(12f)
                        data.setValueTextColor(Color.BLACK)
                        pieChart.data = data
                        pieChart.description.isEnabled = false
                        pieChart.isDrawHoleEnabled = false
                        pieChart.invalidate()

                        bindingBottomSheet.clicsNoticias.titulo.text = "Clics"
                        bindingBottomSheet.compartidasNoticias.titulo.text = "compartidas"
                        bindingBottomSheet.vistasNoticias.titulo.text = "vistas"
                        bindingBottomSheet.guardadosNoticias.titulo.text = "whatsapp"
                        bindingBottomSheet.likeNoticias.titulo.text = "llamadas"
                        if (estadisticasClick != 0 || estadisticas_compartidas != 0 || estadisticas_contactadoWhatsapp != 0 || estadisticas_llamadas != 0 || estadisticas_vistas != 0) {
                            bindingBottomSheet.clicsNoticias.contador.text =
                                estadisticasClick.toString()
                            bindingBottomSheet.compartidasNoticias.contador.text =
                                estadisticas_compartidas.toString()
                            bindingBottomSheet.vistasNoticias.contador.text =
                                estadisticas_vistas.toString()
                            bindingBottomSheet.guardadosNoticias.contador.text =
                                estadisticas_contactadoWhatsapp.toString()
                            bindingBottomSheet.likeNoticias.contador.text =
                                estadisticas_llamadas.toString()
                            bindingBottomSheet.vistasCompartilasCliksNoticias.isVisible = true
                            bindingBottomSheet.noEcnotradoEstadisitcasNoticias.isVisible = false
                        } else {
                            bindingBottomSheet.vistasCompartilasCliksNoticias.isVisible = false
                            bindingBottomSheet.noEcnotradoEstadisitcasNoticias.isVisible = true
                        }
                    }

                }
            }
        }

        val bindingBottomSheet =
            BottomshetetDetallesServiciosBinding.inflate(LayoutInflater.from(mContext))
        val view = bindingBottomSheet.root
        setearIconos(tipo, bindingBottomSheet)
        listenerDialogs(tipo, bindingBottomSheet)

        when (tipo) {
            "baner" -> {
                bindingBottomSheet.titulo.text = "Servicio de publicidad"
                bindingBottomSheet.includeDetalles.nombreServicio.text = "Servicio de publicidad"
                bindingBottomSheet.linealAnuncios.isVisible = true
            }

            "noticia" -> {
                obtenerDatosNoticias(bindingBottomSheet)
                bindingBottomSheet.titulo.text = "Servicio de noticia"
                bindingBottomSheet.linealNoticias.isVisible = true
                bindingBottomSheet.includeDetalles.nombreServicio.text = "Servicio de noticia"


            }

            "verificado" -> {
                obtenerVerificados(bindingBottomSheet)
                bindingBottomSheet.titulo.text = "Verificacion"
                bindingBottomSheet.includeDetalles.nombreServicio.text = "Verificacion"
                bindingBottomSheet.linealNoticias.isVisible = true
            }

            else -> "Servicio"
        }
        bindingBottomSheet.progressBar.visibility = View.VISIBLE
        bindingBottomSheet.scroll.visibility = View.GONE

        val documentoRef =
            FirebaseFirestore.getInstance().collection(Variables.solicitudes_serviciosDB)
                .document(documento)
        setearDatosUser(documentoRef, bindingBottomSheet, idUser)
        obtenerDatosAnuncioGenero_localidaes_cuentas(bindingBottomSheet)

        val dialog = BottomSheetDialog(mContext)
        dialog.setContentView(view)
        dialog.show()
        bindingBottomSheet.renovar.setOnClickListener {
            val vista: Intent? = when (tipo) {
                "baner" -> Intent(mContext, planes::class.java).apply {
                    putExtra("renovacion", "r")
                }

                "noticia" -> Intent(mContext, planes_noticias_servicios_geinz::class.java).apply {
                    putExtra("renovacion", "r")
                }

                "verificado" -> {
                    mostrarMensaje(
                        "Se enviaron tus datos a Geinz para la renovación. Geinz se pondrá en contacto contigo."
                    )
                    null
                }

                else -> null
            }

            vista?.let {
                mContext.startActivity(it)
                dialog.dismiss()
            }
        }
    }


    private fun mostrarGraficoGenero(
        bindingBottomSheet: BottomshetetDetallesServiciosBinding,
        generoData: Map<String, Number>
    ) {
        val entries = ArrayList<PieEntry>()

        // Convertir los datos de género a entradas de PieChart
        generoData.forEach { (key, value) ->
            entries.add(
                PieEntry(
                    value.toFloat(),
                    key
                )
            ) // Cada entrada tiene un valor y una etiqueta
        }

        val pieDataSet = PieDataSet(entries, "").apply {
            colors = listOf(Color.BLUE, Color.MAGENTA) // Colores para las categorías
            valueTextColor = ContextCompat.getColor(mContext, R.color.heartOutlineColor)
            valueTextSize = 12f // Tamaño del texto
        }

        val pieData = PieData(pieDataSet)

        // Configurar el gráfico de pastel
        bindingBottomSheet.includeEstadistica.generoChart.apply {
            data = pieData
            description.isEnabled = false // Desactivar descripción predeterminada
            legend.apply {
                isEnabled = true // Activar leyenda
                textColor = ContextCompat.getColor(
                    context,
                    R.color.heartOutlineColor
                ) // Cambiar el color del texto de la leyenda
            }
            setUsePercentValues(true) // Mostrar los valores en porcentaje
            setExtraOffsets(5f, 10f, 5f, 5f) // Agregar márgenes adicionales
            animateY(1000) // Animación de entrada
            invalidate() // Refrescar el gráfico
        }


        // Asignar los valores de los géneros a los TextView
        val femenino = generoData["femenino"]?.toInt() ?: 0
        val masculino = generoData["masculino"]?.toInt() ?: 0

        bindingBottomSheet.includeEstadistica.includeFemenino.titulo.text = "Femenino"
        bindingBottomSheet.includeEstadistica.includemasculino.titulo.text = "Masculino"

        if (femenino != 0 || masculino != 0) {
            bindingBottomSheet.includeEstadistica.includeFemenino.contador.text =
                femenino.toString()
            bindingBottomSheet.includeEstadistica.includemasculino.contador.text =
                masculino.toString()
            bindingBottomSheet.includeEstadistica.generoChart.isVisible = true
            bindingBottomSheet.includeEstadistica.noEncontradoGenero.isVisible = true
            bindingBottomSheet.includeEstadistica.noEncontradoGenero.isVisible = false
        } else {
            bindingBottomSheet.includeEstadistica.noEncontradoGenero.isVisible = false
            bindingBottomSheet.includeEstadistica.noEncontradoGenero.isVisible = true
        }


    }


    private fun mostrarGraficoLocalidades(
        bindingBottomSheet: BottomshetetDetallesServiciosBinding,
        localidadData: Map<String, Number>
    ) {
        val entries = ArrayList<BarEntry>()
        val labels = ArrayList<String>()
        var index = 0

        localidadData.forEach { (key, value) ->
            entries.add(BarEntry(index.toFloat(), value.toFloat()))
            labels.add(key)
            index++
        }

        val barDataSet = BarDataSet(entries, "Localidades").apply {
            colors = listOf(
                Color.parseColor("#4CAF50"), // Para Paramonga
                Color.parseColor("#FF5722"), // Para Supe
                Color.parseColor("#FFC107"), // Para Praga
                Color.parseColor("#03A9F4")  // Para Pativilca
            )
            valueTextColor = Color.BLACK // Color del texto de los valores
            valueTextSize = 12f // Tamaño del texto
            barBorderWidth = 0.2f // Ancho del borde de las barras
            barBorderColor = Color.parseColor("#ffffff") // Color del borde de las barras
        }

        val barData = BarData(barDataSet)

        // Configuración del gráfico de barras
        bindingBottomSheet.includeEstadistica.lcalidad.apply {
            data = barData
            description.isEnabled = false // Desactivar la descripción predeterminada

            // Configurar el eje izquierdo
            axisLeft.apply {
                textColor = ContextCompat.getColor(
                    context,
                    R.color.heartOutlineColor
                )// Cambiar el color del texto
                gridColor = ContextCompat.getColor(
                    context,
                    R.color.heartOutlineColor
                ) // Cambiar el color de las líneas de cuadrícula
                axisLineColor = ContextCompat.getColor(
                    context,
                    R.color.heartOutlineColor
                ) // Cambiar el color de la línea del eje
            }

            // Configurar el eje derecho
            axisRight.isEnabled = false // Deshabilitar eje derecho

            // Configurar el eje X
            xAxis.apply {
                textColor = Color.WHITE // Cambiar el color del texto
                gridColor = Color.TRANSPARENT // Quitar las líneas de cuadrícula
                axisLineColor = Color.WHITE // Cambiar el color de la línea del eje
                valueFormatter =
                    IndexAxisValueFormatter(labels) // Formatear las etiquetas del eje X
                position =
                    XAxis.XAxisPosition.BOTTOM // Colocar las etiquetas en la parte inferior
            }

            legend.isEnabled = false
            setFitBars(true) // Ajustar las barras al gráfico
            animateY(1000) // Animación de entrada
            invalidate() // Refrescar el gráfico
        }


        val paramonga = localidadData["paramonga"]?.toInt() ?: 0
        val supe = localidadData["supe"]?.toInt() ?: 0
        val pativilca = localidadData["pativilca"]?.toInt() ?: 0
        val barranca = localidadData["barranca"]?.toInt() ?: 0

        bindingBottomSheet.includeEstadistica.paramonga.titulo.text = "Paramonga"
        bindingBottomSheet.includeEstadistica.barranca.titulo.text = "barranca"
        bindingBottomSheet.includeEstadistica.supe.titulo.text = "supe"
        bindingBottomSheet.includeEstadistica.pativilca.titulo.text = "pativilca"


        if (paramonga != 0 || supe != 0 || pativilca != 0 || barranca != 0) {
            bindingBottomSheet.includeEstadistica.paramonga.contador.text = paramonga.toString()
            bindingBottomSheet.includeEstadistica.pativilca.contador.text = pativilca.toString()
            bindingBottomSheet.includeEstadistica.barranca.contador.text = barranca.toString()
            bindingBottomSheet.includeEstadistica.supe.contador.text = supe.toString()
            bindingBottomSheet.includeEstadistica.lcalidad.isVisible = true
            bindingBottomSheet.includeEstadistica.noEncontradoLocalidades.isVisible = false
        } else {
            bindingBottomSheet.includeEstadistica.lcalidad.isVisible = false
            bindingBottomSheet.includeEstadistica.noEncontradoLocalidades.isVisible = true
        }

    }

    private fun mostrarGraficoCuentaTrabajadores(
        bindingBottomSheet: BottomshetetDetallesServiciosBinding,
        cuentas: Map<String, Number>
    ) {
        val entries = ArrayList<BarEntry>()
        val labels = ArrayList<String>()
        var index = 0

        cuentas.forEach { (key, value) ->
            entries.add(BarEntry(index.toFloat(), value.toFloat()))
            labels.add(key) // Nombres como "Cuenta Simple", "Cuenta Trabajador"
            index++
        }

        // Crear un conjunto de datos para las barras
        val barDataSet = BarDataSet(entries, "").apply {
            // Colores personalizados para las barras
            colors = listOf(
                Color.parseColor("#3F51B5"), // Color para "Cuenta Simple"
                Color.parseColor("#FF5722")  // Color para "Cuenta Trabajador"
            )
            valueTextColor = Color.BLACK // Color del texto de los valores
            valueTextSize = 12f // Tamaño del texto
        }

        // Crear los datos para el gráfico
        val barData = BarData(barDataSet)

        // Configuración del gráfico de barras
        // Configuración del gráfico de barras
        bindingBottomSheet.includeEstadistica.cuentas.apply {
            data = barData
            description.isEnabled = false // Desactivar la descripción predeterminada

            // Configurar el eje izquierdo
            axisLeft.apply {
                textColor = ContextCompat.getColor(
                    context,
                    R.color.heartOutlineColor
                )// Cambiar el color del texto
                gridColor = ContextCompat.getColor(
                    context,
                    R.color.heartOutlineColor
                ) // Cambiar el color de las líneas de cuadrícula
                axisLineColor = ContextCompat.getColor(
                    context,
                    R.color.heartOutlineColor
                ) // Cambiar el color de la línea del eje
            }

            // Configurar el eje derecho
            axisRight.isEnabled = false // Deshabilitar eje derecho

            // Configurar el eje X
            xAxis.apply {
                textColor = Color.WHITE // Cambiar el color del texto
                gridColor = Color.TRANSPARENT // Quitar las líneas de cuadrícula
                axisLineColor = Color.WHITE // Cambiar el color de la línea del eje
                valueFormatter =
                    IndexAxisValueFormatter(labels) // Formatear las etiquetas del eje X
                position =
                    XAxis.XAxisPosition.BOTTOM // Colocar las etiquetas en la parte inferior
            }

            legend.isEnabled = false
            setFitBars(true) // Ajustar las barras al gráfico
            animateY(1000) // Animación de entrada
            invalidate() // Refrescar el gráfico
        }


        val simple = cuentas["cuenta simple"]?.toInt() ?: 0
        val trabajador = cuentas["cuenta trabajador"]?.toInt() ?: 0

        bindingBottomSheet.includeEstadistica.user.titulo.text = "usuarios"
        bindingBottomSheet.includeEstadistica.trabajadores.titulo.text = "trabajadores"
        if (simple != 0 || trabajador != 0) {
            bindingBottomSheet.includeEstadistica.user.contador.text = simple.toString()
            bindingBottomSheet.includeEstadistica.trabajadores.contador.text = trabajador.toString()
            bindingBottomSheet.includeEstadistica.cuentas.isVisible = true
            bindingBottomSheet.includeEstadistica.noEncontradoCuentas.isVisible = false
        } else {
            bindingBottomSheet.includeEstadistica.cuentas.isVisible = false
            bindingBottomSheet.includeEstadistica.noEncontradoCuentas.isVisible = true
        }


    }


    @RequiresApi(Build.VERSION_CODES.O)
    private fun setearTextoActivos(
        fechaVencimiento: String,
        bindingBottomSheet: BottomshetetDetallesServiciosBinding
    ) {
        if (fechaVencimiento.isNotEmpty()) {
            val diasRestantes =
                mostrarFechaDialog_horaDialog.calcularDiasRestantes(fechaVencimiento)
            bindingBottomSheet.includeDetalles.diasRestantes.text = if (diasRestantes > 0) {
                "$diasRestantes ${if (diasRestantes > 1) "Días" else "Día"}"
            } else {
                bindingBottomSheet.renovar.isVisible = true
                "Venció"
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun setearDatosUser(
        db: DocumentReference,
        bindingBottomSheet: BottomshetetDetallesServiciosBinding,
        idUser: String
    ) {
        db.collection(Variables.activos).document(idUser).get().addOnSuccessListener { res ->
            if (res.exists()) {
                val doc = res.data ?: return@addOnSuccessListener
                val documento1 = doc["documento1"] as? String ?: ""
                val documento2 = doc["documento2"] as? String ?: ""
                val datosMap = doc["fechas"] as? Map<String, Any>
                val plan = doc["plan"] as? String ?: ""
                val fechaActivo = datosMap?.get("fecha_activacion") as? String ?: ""
                val fechaVencimiento = datosMap?.get("fecha_vencimiento") as? String ?: ""
                val horaActivacion = datosMap?.get("hora_activacion") as? String ?: ""
                val horaVencimiento = datosMap?.get("hora_vencimiento") as? String ?: ""
                setearTextoActivos(fechaVencimiento, bindingBottomSheet)

                bindingBottomSheet.includeDetalles.plan.text = plan
                constantesCarrito.setearDatosUsuario { nombre, numero, localidad, apellido ->
                    bindingBottomSheet.includeDetalles.nombre.text = nombre
                    bindingBottomSheet.includeDetalles.numero.text = numero
                    bindingBottomSheet.includeDetalles.apellido.text = apellido
                }
                bindingBottomSheet.includeDetalles.fActivado.text = fechaActivo
                bindingBottomSheet.includeDetalles.fVencimineto.text = fechaVencimiento
                bindingBottomSheet.includeDetalles.hActivado.text = horaActivacion
                bindingBottomSheet.includeDetalles.hVecimineto.text = horaVencimiento
            }
            db.collection("historial").get().addOnSuccessListener { res ->
                for (datos in res) {
                    val data = datos.data
                    val idUserRef = data["id_usuario"] as? String ?: ""
                    if (idUser == idUserRef) {
                        val total = data["total"] as? String ?: ""
                        val idhistorial = data["id_solicitud"] as? String ?: ""
                        val estado = data["estado"] as? String ?: ""
                        bindingBottomSheet.includeDetalles.idDoc.text = idhistorial
                        bindingBottomSheet.includeDetalles.Total.text = total
                        bindingBottomSheet.includeDetalles.estado.text = estado
                    }
                }

                bindingBottomSheet.progressBar.visibility = View.GONE
                bindingBottomSheet.scroll.visibility = View.VISIBLE
            }.addOnFailureListener {
                bindingBottomSheet.progressBar.visibility = View.GONE
            }
        }

    }

    private fun mostrarMensaje(mensaje: String) {
        Toast.makeText(mContext, mensaje, Toast.LENGTH_SHORT).show()
    }

    private fun listenerDialogs(
        tipo: String,
        bottomSheetDialog: BottomshetetDetallesServiciosBinding
    ) {
        val dialogMapping = mapOf(
            bottomSheetDialog.clics.listener to "clics",
            bottomSheetDialog.vistas.listener to "vista",
            bottomSheetDialog.compartidas.listener to "compartida",
            bottomSheetDialog.clicsNoticias.listener to "clics",
            bottomSheetDialog.vistasNoticias.listener to "vista",
            bottomSheetDialog.compartidasNoticias.listener to "compartida",
            bottomSheetDialog.includeEstadistica.includeFemenino.listener to "femenino",
            bottomSheetDialog.includeEstadistica.includemasculino.listener to "masculino",
            bottomSheetDialog.includeEstadistica.barranca.listener to "localidad",
            bottomSheetDialog.includeEstadistica.supe.listener to "localidad",
            bottomSheetDialog.includeEstadistica.paramonga.listener to "localidad",
            bottomSheetDialog.includeEstadistica.pativilca.listener to "localidad",
            bottomSheetDialog.includeEstadistica.user.listener to "usuarios",
            bottomSheetDialog.includeEstadistica.trabajadores.listener to "trabajadores",
            bottomSheetDialog.likeNoticias.listener to if (tipo == "verificado") "llamadas" else "like",
            bottomSheetDialog.guardadosNoticias.listener to if (tipo == "verificado") "whatsapp" else "guardados"
        )

        dialogMapping.forEach { (view, dialogType) ->
            view.setOnClickListener {
                mostrarDialog(dialogType)
            }
        }
    }


    private fun mostrarDialog(tipo: String) {
        val mensaje = when (tipo) {
            "whatsapp" -> getString(R.string.whatsapp)
            "llamadas" -> getString(R.string.llamadas)
            "clics" -> getString(R.string.cliks)
            "vista" -> getString(R.string.vista)
            "guardados" -> getString(R.string.guardados)
            "like" -> getString(R.string.likes)
            "compartida" -> getString(R.string.compartida)
            "femenino" -> getString(R.string.fem)
            "masculino" -> getString(R.string.mas)
            "localidad" -> getString(R.string.localidad)
            "usuarios" -> getString(R.string.user)
            "trabajdores" -> getString(R.string.trabajadores)
            else -> "Información no disponible para esta categoría."
        }


        AlertDialog.Builder(mContext).apply {
            setTitle(tipo.uppercase())
            setMessage(mensaje)
            setPositiveButton("Aceptar") { dialog, _ ->
                dialog.dismiss()
            }
            setCancelable(true)
        }.show()
    }


    private fun obtenerImgServicios(tipo: String, db: DocumentReference) {
        db.get().addOnSuccessListener { res ->
            if (res.exists()) {
                val datos = res.data
                val imagenUrl = datos?.get("imagenUrl") as? String ?: ""

                when (tipo) {
                    "noticia" -> {
                        Glide.with(mContext)
                            .load(imagenUrl)
                            .placeholder(R.drawable.cargando_img)
                            .error(R.drawable.no_cuenta_img)
                            .into(binding.noticia.imgServicio)
                    }

                    "publicidad" -> {
                        Glide.with(mContext)
                            .load(imagenUrl)
                            .placeholder(R.drawable.cargando_img)
                            .error(R.drawable.no_cuenta_img)
                            .into(binding.baner.imgServicio)
                    }

                    else -> {
                        println("Tipo desconocido: $tipo")
                    }
                }
            } else {
                println("El documento no existe en la base de datos.")
            }
        }.addOnFailureListener { exception ->
            println("Error al obtener el documento: ${exception.message}")
        }
    }

    private fun obtener_datos_noticia(
        tipo: String,
        db: DocumentReference,
        documentos: (String, String, String) -> Unit
    ) {
        db.get().addOnSuccessListener { doc ->
            val datos = doc.data
            if (doc != null && doc.exists()) {
                val datosMap = doc.get("fechas") as? Map<String, Any>
                if (datosMap != null) {
                    val documento1 = datos?.get("documento1") as? String ?: ""
                    val documento2 = datos?.get("documento2") as? String ?: ""
                    val plan = datos?.get("plan") as? String ?: ""
                    documentos(documento1, documento2, plan)
                    val fechaActivo = datosMap["fecha_activacion"] as? String ?: ""
                    val fechaVencimiento = datosMap["fecha_vencimiento"] as? String ?: ""

                    val horaActivacion = datosMap["hora_activacion"] as? String ?: ""
                    val horaVencimiento = datosMap["hora_vencimiento"] as? String ?: ""
                    when (tipo) {
                        "noticia" -> {
                            binding.noticia.fechaActivo.text = fechaActivo
                            binding.noticia.fechaVencimineto.text = fechaVencimiento
                            binding.noticia.estado.text = getString(R.string.activo)
                        }

                        "baner" -> {
                            binding.baner.fechaActivo.text = fechaActivo
                            binding.baner.fechaVencimineto.text = fechaVencimiento
                            binding.baner.estado.text = getString(R.string.activo)
                        }

                        "verificado" -> {
                            binding.verificacion.fechaActivo.text = fechaActivo
                            binding.verificacion.fechaVencimineto.text = fechaVencimiento
                            binding.verificacion.estado.text = getString(R.string.activo)
                        }
                    }
                } else {
                    Log.e("error_fechas", "error al encontrar las fechas")
                }
            } else {
                Log.e("error_fechas", "error al encontrar las fechas")
            }
        }

    }

    private fun ver_servicios_activos(
        idUSer: String,
        noticiaActiva: (Boolean) -> Unit,
        banerActiva: (Boolean) -> Unit,
        Verificado: (Boolean) -> Unit
    ) {
        val General = FirebaseFirestore.getInstance().collection(Variables.solicitud_servicios)
        val dbNoticias = General
            .document(Variables.publicidadNoticiasDB).collection(Variables.activos)
            .document(idUSer)
        val dbBaner = General.document(Variables.publicidadBanerDB).collection(Variables.activos)
            .document(idUSer)
        val dbVerificado =
            General.document(Variables.verificacionesDB).collection(Variables.activos)
                .document(idUSer)

        var servicioActivo = false

        fun verificarActivo(Db: DocumentReference, tipo: String, callback: (Boolean) -> Unit) {
            Db.get().addOnSuccessListener { res ->
                val activo = res.exists()
                if (activo) servicioActivo = true
                callback(activo)
            }.addOnFailureListener {
                callback(false)
            }
        }

        verificarActivo(dbNoticias, "noticia") { activo ->
            estados["noticia"] = activo
            noticiaActiva(activo)
            actualizarVista()
            Log.d("servicios_activos","el noticia $activo")
        }

        verificarActivo(dbBaner, "baner") { activo ->
            estados["baner"] = activo
            banerActiva(activo)
            actualizarVista()
            Log.d("servicios_activos","el baner $activo")

        }

        verificarActivo(dbVerificado, "verificacion") { activo ->
            estados["verificacion"] = activo
            Verificado(activo)
            actualizarVista()
            Log.d("servicios_activos","el verificado $activo")

        }
    }

    val estados = mutableMapOf(
        "noticia" to false,
        "baner" to false,
        "verificacion" to false
    )

    fun actualizarVista() {
        val servicioActivo = estados.values.any { it } // Si al menos uno es true
        binding.cargaServicios.isVisible = false
        binding.LinealSinServicios.isVisible = !servicioActivo // Se muestra si ninguno es true
        Log.d("EstadosServicios", "Estado actual: $estados")
        Log.d("EstadosServiciosCarga","la carga de servicios es :$servicioActivo")
    }


    private fun setearIconos(
        tipo: String,
        bindingBottomSheet: BottomshetetDetallesServiciosBinding
    ) {
        bindingBottomSheet.clics.icono.setImageResource(R.drawable.clikc_drawable)
        bindingBottomSheet.vistas.icono.setImageResource(R.drawable.view_drawable)
        bindingBottomSheet.compartidas.icono.setImageResource(R.drawable.compartir_drawable)
        bindingBottomSheet.clicsNoticias.icono.setImageResource(R.drawable.clikc_drawable)
        bindingBottomSheet.vistasNoticias.icono.setImageResource(R.drawable.view_drawable)
        bindingBottomSheet.compartidasNoticias.icono.setImageResource(R.drawable.compartir_drawable)
        bindingBottomSheet.likeNoticias.icono.setImageResource(R.drawable.baseline_favorite_white)
        bindingBottomSheet.guardadosNoticias.icono.setImageResource(R.drawable.save_icon)

        bindingBottomSheet.includeEstadistica.includeFemenino.icono.setImageResource(R.drawable.femenino_drawable)
        bindingBottomSheet.includeEstadistica.includemasculino.icono.setImageResource(R.drawable.masculono_drawable)
        bindingBottomSheet.includeEstadistica.barranca.icono.setImageResource(R.drawable.location_drawable)
        bindingBottomSheet.includeEstadistica.paramonga.icono.setImageResource(R.drawable.location_drawable)
        bindingBottomSheet.includeEstadistica.supe.icono.setImageResource(R.drawable.location_drawable)
        bindingBottomSheet.includeEstadistica.pativilca.icono.setImageResource(R.drawable.location_drawable)
        bindingBottomSheet.includeEstadistica.user.icono.setImageResource(R.drawable.persona_drawable)
        bindingBottomSheet.includeEstadistica.trabajadores.icono.setImageResource(R.drawable.trabajador_drawable)
        if (tipo == "verificado") bindingBottomSheet.likeNoticias.icono.setImageResource(R.drawable.llamada_iconsvg)
        if (tipo == "verificado") bindingBottomSheet.guardadosNoticias.icono.setImageResource(R.drawable.whatsapp_iconsvg)
    }

}