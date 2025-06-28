package com.geinzz.geinzwork.vistaTrabajador

import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import com.example.geinzwork.constantesGeneral.Variables
import com.geinzz.geinzwork.Login
import com.geinzz.geinzwork.R
import com.geinzz.geinzwork.adapterViewholder.adapterViewPager
import com.geinzz.geinzwork.constantesGeneral.constantesCarrito
import com.geinzz.geinzwork.constantesGeneral.constantesPublicidad
import com.geinzz.geinzwork.databinding.ActivityVistaTrabajadorBinding
import com.geinzz.geinzwork.fragmentos.addReview
import com.geinzz.geinzwork.fragmentos.cuenta_verificada
import com.geinzz.geinzwork.fragmentos.info
import com.geinzz.geinzwork.fragmentos.review
import com.geinzz.geinzwork.vistaTiendas.Fragment_trabajaConNosotros_tienda
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class vistaTrabajador : AppCompatActivity() {
    private var idTrabajador: String? = null
    private lateinit var dialog: BottomSheetDialog
    private lateinit var firebaseAuth: FirebaseAuth
    private var vistaTimer: CountDownTimer? = null
    private val tiempoParaContarVista: Long = 20000
    private lateinit var binding: ActivityVistaTrabajadorBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityVistaTrabajadorBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        firebaseAuth = FirebaseAuth.getInstance()
        val uri = intent.data
        val tipo_pasado_vistas = uri?.getQueryParameter("registrado") ?: "desconocido"

        if (firebaseAuth.currentUser != null && tipo_pasado_vistas!=null) {
            binding.fracmentoID.isVisible = true
            binding.tabLayout.isVisible = true
            binding.sinRegistro.isVisible = false
            when (tipo_pasado_vistas) {
                "usuario" -> {
                    val idTrabajador =  firebaseAuth.uid.toString()
                    Toast.makeText(this, "pasamos el id $idTrabajador", Toast.LENGTH_SHORT).show()
                    val nombreUSer = intent.getStringExtra(Variables.nombreUSer) ?: ""
                    val nacionalidad = intent.getStringExtra(Variables.nacionalidad) ?: ""
                    val categoria = intent.getStringExtra(Variables.categoria) ?: ""
                    val id_publicacion = intent.getStringExtra("id_publicacion") ?: ""
                    val imgPerfil = intent.getStringExtra(Variables.imagenPerfil).toString().trim()

                    val viewPage = binding.fracmentoID
                    val tableLayour = binding.tabLayout
                    val adapter = adapterViewPager(supportFragmentManager)
                    viewPage.adapter = adapter
                    val userCollections =
                        FirebaseFirestore.getInstance().collection(Variables.solicitud_servicios)
                            .document(Variables.verificaiones).collection(Variables.activos)
                            .document(idTrabajador)
                    userCollections.get().addOnSuccessListener { res ->
                        if (res.exists()) {
                            constantesPublicidad.agregarCantidadClickAnuncios(
                                userCollections,
                                "",
                                Variables.click
                            )
                            constantesPublicidad.obtenerLocalidaGeneroTipoCuenta(
                                userCollections,
                                Variables.verificacion
                            )
                            iniciarContadorVista(idTrabajador)
                            val data = res.data
                            val verificado = data?.get(Variables.estado) as? Boolean ?: false
                            adapter.addFragmet(
                                info.newInstance(
                                    idTrabajador,
                                    imgPerfil,
                                    nombreUSer,
                                    nacionalidad,
                                    categoria, id_publicacion
                                ), "Informacion del usuario"
                            )
                            adapter.addFragmet(review.newInstance(idTrabajador), "Reseñas de usuarios")
                            adapter.addFragmet(addReview.newInstance(idTrabajador), "Agregar Reseña")
                            if (verificado) {
                                adapter.addFragmet(cuenta_verificada(), "Cuenta Verificada")
                            }
                            adapter.addFragmet(Fragment_trabajaConNosotros_tienda(), "Geinz Tienda")
                        } else {
                            adapter.addFragmet(
                                info.newInstance(
                                    idTrabajador,
                                    imgPerfil,
                                    nombreUSer,
                                    nacionalidad,
                                    categoria, id_publicacion
                                ), "Informacion del usuario"
                            )
                            adapter.addFragmet(review.newInstance(idTrabajador), "Reseñas de usuarios")
                            adapter.addFragmet(addReview.newInstance(idTrabajador), "Agregar Reseña")
                            adapter.addFragmet(Fragment_trabajaConNosotros_tienda(), "Geinz Tienda")
                        }
                        adapter.notifyDataSetChanged()
                    }.addOnFailureListener {
                        Toast.makeText(this, "Trabajador no encontrado", Toast.LENGTH_SHORT).show()
                    }
                    tableLayour.setupWithViewPager(viewPage)
                }

                else -> {
                    val idTrabajador = intent.getStringExtra(Variables.id) ?: ""
                    val nombreUSer = intent.getStringExtra(Variables.nombreUSer) ?: ""
                    val nacionalidad = intent.getStringExtra(Variables.nacionalidad) ?: ""
                    val categoria = intent.getStringExtra(Variables.categoria) ?: ""
                    val id_publicacion = intent.getStringExtra("id_publicacion") ?: ""
                    val imgPerfil = intent.getStringExtra(Variables.imagenPerfil).toString().trim()

                    val viewPage = binding.fracmentoID
                    val tableLayour = binding.tabLayout
                    val adapter = adapterViewPager(supportFragmentManager)
                    viewPage.adapter = adapter
                    val userCollections =
                        FirebaseFirestore.getInstance().collection(Variables.solicitud_servicios)
                            .document(Variables.verificaiones).collection(Variables.activos)
                            .document(idTrabajador)
                    userCollections.get().addOnSuccessListener { res ->
                        if (res.exists()) {
                            constantesPublicidad.agregarCantidadClickAnuncios(
                                userCollections,
                                "",
                                Variables.click
                            )
                            constantesPublicidad.obtenerLocalidaGeneroTipoCuenta(
                                userCollections,
                                Variables.verificacion
                            )
                            iniciarContadorVista(idTrabajador)
                            val data = res.data
                            val verificado = data?.get(Variables.estado) as? Boolean ?: false
                            adapter.addFragmet(
                                info.newInstance(
                                    idTrabajador,
                                    imgPerfil,
                                    nombreUSer,
                                    nacionalidad,
                                    categoria, id_publicacion
                                ), "Informacion del usuario"
                            )
                            adapter.addFragmet(review.newInstance(idTrabajador), "Reseñas de usuarios")
                            adapter.addFragmet(addReview.newInstance(idTrabajador), "Agregar Reseña")
                            if (verificado) {
                                adapter.addFragmet(cuenta_verificada(), "Cuenta Verificada")
                            }
                            adapter.addFragmet(Fragment_trabajaConNosotros_tienda(), "Geinz Tienda")
                        } else {
                            adapter.addFragmet(
                                info.newInstance(
                                    idTrabajador,
                                    imgPerfil,
                                    nombreUSer,
                                    nacionalidad,
                                    categoria, id_publicacion
                                ), "Informacion del usuario"
                            )
                            adapter.addFragmet(review.newInstance(idTrabajador), "Reseñas de usuarios")
                            adapter.addFragmet(addReview.newInstance(idTrabajador), "Agregar Reseña")
                            adapter.addFragmet(Fragment_trabajaConNosotros_tienda(), "Geinz Tienda")
                        }
                        adapter.notifyDataSetChanged()
                    }.addOnFailureListener {
                        Toast.makeText(this, "Trabajador no encontrado", Toast.LENGTH_SHORT).show()
                    }
                    tableLayour.setupWithViewPager(viewPage)
                }
            }
        } else {
            binding.sinRegistro.isVisible = true
            binding.fracmentoID.isVisible = false
            binding.tabLayout.isVisible = false
            binding.iniciarSeccion.setOnClickListener {
                val vista = Intent(this, Login::class.java).apply {
                    putExtra("dato", "perfil")
                }
                startActivity(vista)
            }
        }
    }

    override fun onStop() {
        super.onStop()
        cancelarContadorVista()
    }

    override fun onResume() {
        super.onResume()
        val uri = intent.data
        val tipo_pasado_vistas = uri?.getQueryParameter("registrado") ?: "desconocido"
        if (firebaseAuth.currentUser != null && tipo_pasado_vistas!=null) {
            binding.fracmentoID.isVisible = true
            binding.tabLayout.isVisible = true
            binding.sinRegistro.isVisible = false
            when (tipo_pasado_vistas) {
                "usuario" -> {
                    val idTrabajador = uri?.getQueryParameter("uid") ?: firebaseAuth.uid.toString()
                    val nombreUSer = intent.getStringExtra(Variables.nombreUSer) ?: ""
                    val nacionalidad = intent.getStringExtra(Variables.nacionalidad) ?: ""
                    val categoria = intent.getStringExtra(Variables.categoria) ?: ""
                    val id_publicacion = intent.getStringExtra("id_publicacion") ?: ""
                    val imgPerfil = intent.getStringExtra(Variables.imagenPerfil).toString().trim()

                    val viewPage = binding.fracmentoID
                    val tableLayour = binding.tabLayout
                    val adapter = adapterViewPager(supportFragmentManager)
                    viewPage.adapter = adapter
                    val userCollections =
                        FirebaseFirestore.getInstance().collection(Variables.solicitud_servicios)
                            .document(Variables.verificaiones).collection(Variables.activos)
                            .document(idTrabajador)
                    userCollections.get().addOnSuccessListener { res ->
                        if (res.exists()) {
                            constantesPublicidad.agregarCantidadClickAnuncios(
                                userCollections,
                                "",
                                Variables.click
                            )
                            constantesPublicidad.obtenerLocalidaGeneroTipoCuenta(
                                userCollections,
                                Variables.verificacion
                            )
                            iniciarContadorVista(idTrabajador)
                            val data = res.data
                            val verificado = data?.get(Variables.estado) as? Boolean ?: false
                            adapter.addFragmet(
                                info.newInstance(
                                    idTrabajador,
                                    imgPerfil,
                                    nombreUSer,
                                    nacionalidad,
                                    categoria, id_publicacion
                                ), "Informacion del usuario"
                            )
                            adapter.addFragmet(review.newInstance(idTrabajador), "Reseñas de usuarios")
                            adapter.addFragmet(addReview.newInstance(idTrabajador), "Agregar Reseña")
                            if (verificado) {
                                adapter.addFragmet(cuenta_verificada(), "Cuenta Verificada")
                            }
                            adapter.addFragmet(Fragment_trabajaConNosotros_tienda(), "Geinz Tienda")
                        } else {
                            adapter.addFragmet(
                                info.newInstance(
                                    idTrabajador,
                                    imgPerfil,
                                    nombreUSer,
                                    nacionalidad,
                                    categoria, id_publicacion
                                ), "Informacion del usuario"
                            )
                            adapter.addFragmet(review.newInstance(idTrabajador), "Reseñas de usuarios")
                            adapter.addFragmet(addReview.newInstance(idTrabajador), "Agregar Reseña")
                            adapter.addFragmet(Fragment_trabajaConNosotros_tienda(), "Geinz Tienda")
                        }
                        adapter.notifyDataSetChanged()
                    }.addOnFailureListener {
                        Toast.makeText(this, "Trabajador no encontrado", Toast.LENGTH_SHORT).show()
                    }
                    tableLayour.setupWithViewPager(viewPage)
                }

                else -> {
                    val idTrabajador = intent.getStringExtra(Variables.id) ?: ""
                    val nombreUSer = intent.getStringExtra(Variables.nombreUSer) ?: ""
                    val nacionalidad = intent.getStringExtra(Variables.nacionalidad) ?: ""
                    val categoria = intent.getStringExtra(Variables.categoria) ?: ""
                    val id_publicacion = intent.getStringExtra("id_publicacion") ?: ""
                    val imgPerfil = intent.getStringExtra(Variables.imagenPerfil).toString().trim()

                    val viewPage = binding.fracmentoID
                    val tableLayour = binding.tabLayout
                    val adapter = adapterViewPager(supportFragmentManager)
                    viewPage.adapter = adapter
                    val userCollections =
                        FirebaseFirestore.getInstance().collection(Variables.solicitud_servicios)
                            .document(Variables.verificaiones).collection(Variables.activos)
                            .document(idTrabajador)
                    userCollections.get().addOnSuccessListener { res ->
                        if (res.exists()) {
                            constantesPublicidad.agregarCantidadClickAnuncios(
                                userCollections,
                                "",
                                Variables.click
                            )
                            constantesPublicidad.obtenerLocalidaGeneroTipoCuenta(
                                userCollections,
                                Variables.verificacion
                            )
                            iniciarContadorVista(idTrabajador)
                            val data = res.data
                            val verificado = data?.get(Variables.estado) as? Boolean ?: false
                            adapter.addFragmet(
                                info.newInstance(
                                    idTrabajador,
                                    imgPerfil,
                                    nombreUSer,
                                    nacionalidad,
                                    categoria, id_publicacion
                                ), "Informacion del usuario"
                            )
                            adapter.addFragmet(review.newInstance(idTrabajador), "Reseñas de usuarios")
                            adapter.addFragmet(addReview.newInstance(idTrabajador), "Agregar Reseña")
                            if (verificado) {
                                adapter.addFragmet(cuenta_verificada(), "Cuenta Verificada")
                            }
                            adapter.addFragmet(Fragment_trabajaConNosotros_tienda(), "Geinz Tienda")
                        } else {
                            adapter.addFragmet(
                                info.newInstance(
                                    idTrabajador,
                                    imgPerfil,
                                    nombreUSer,
                                    nacionalidad,
                                    categoria, id_publicacion
                                ), "Informacion del usuario"
                            )
                            adapter.addFragmet(review.newInstance(idTrabajador), "Reseñas de usuarios")
                            adapter.addFragmet(addReview.newInstance(idTrabajador), "Agregar Reseña")
                            adapter.addFragmet(Fragment_trabajaConNosotros_tienda(), "Geinz Tienda")
                        }
                        adapter.notifyDataSetChanged()
                    }.addOnFailureListener {
                        Toast.makeText(this, "Trabajador no encontrado", Toast.LENGTH_SHORT).show()
                    }
                    tableLayour.setupWithViewPager(viewPage)
                }
            }
        } else {
            binding.sinRegistro.isVisible = true
            binding.fracmentoID.isVisible = false
            binding.tabLayout.isVisible = false
            binding.iniciarSeccion.setOnClickListener {
                val vista = Intent(this, Login::class.java).apply {
                    putExtra("dato", "perfil")
                }
                startActivity(vista)
            }
        }
    }
    private fun iniciarContadorVista(idTrabajador: String) {
        val db = FirebaseFirestore.getInstance().collection(Variables.solicitud_servicios)
            .document(Variables.verificaiones).collection(Variables.activos).document(idTrabajador)
        vistaTimer = object : CountDownTimer(tiempoParaContarVista, 1000) {
            override fun onTick(millisUntilFinished: Long) {}

            override fun onFinish() {
                constantesPublicidad.agregarCantidadClickAnuncios(db, "", Variables.vistas)
            }
        }.start()
    }

    private fun cancelarContadorVista() {
        vistaTimer?.cancel()
    }


}


