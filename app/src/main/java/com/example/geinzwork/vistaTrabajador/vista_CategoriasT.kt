package com.geinzz.geinzwork.vistaTrabajador

import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.geinzwork.adapterViewholder.adapterFiltradoTrabajadoresVerMAs
import com.example.geinzwork.constantesGeneral.Variables
import com.example.geinzwork.dataclass.dataclasFiltradoTrabaadores
import com.geinzz.geinzwork.R
import com.geinzz.geinzwork.adapterViewholder.adapter
import com.geinzz.geinzwork.constantesGeneral.constantesTrabajadoresTiendasInicioFragmet
import com.geinzz.geinzwork.databinding.ActivityVistaCategoriasTBinding
import com.geinzz.geinzwork.dataclass.dataClassTrabajosd
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.time.LocalTime
import java.time.format.DateTimeFormatter

class vista_CategoriasT : AppCompatActivity() {
    private lateinit var binding: ActivityVistaCategoriasTBinding
    private val listaTrabajo = mutableListOf<dataClassTrabajosd>().toMutableList()
    private lateinit var firebaseAuth: FirebaseAuth
    private var img = ""
    private var categoriasFiltrado = mutableListOf<dataclasFiltradoTrabaadores>()
    private lateinit var adapterFiltrado: adapterFiltradoTrabajadoresVerMAs

    @RequiresApi(Build.VERSION_CODES.O)
    @SuppressLint("RestrictedApi", "SuspiciousIndentation")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityVistaCategoriasTBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        firebaseAuth = FirebaseAuth.getInstance()
        val respue = intent.getStringExtra(Variables.valor).toString()
        when (respue) {
            "Mejores Trabajadores" -> {
                mejoresTrabajadores(constantesTrabajadoresTiendasInicioFragmet.localidads)
            }

            else -> obtenerTrabajosdb(constantesTrabajadoresTiendasInicioFragmet.localidads, respue)
        }
        binding.totalUser.text =
            "Filtrado por trabajadores de ${constantesTrabajadoresTiendasInicioFragmet.localidads}"


        binding.categoria.text = respue
        binding.editexFilter.addTextChangedListener { userFilter ->
            val usuariosFiltrados =
                listaTrabajo.filter { trabajos ->
                    trabajos.tipoT!!.lowercase().contains(userFilter.toString().lowercase())
                }
            binding.totalUser.text = "Encontrados (${usuariosFiltrados.size}) Trabajadores"
            if (usuariosFiltrados.isEmpty()) {
                binding.relativeNoEncontrado.isVisible = true
                binding.recicleCategoria.isVisible = false
            } else {
                binding.relativeNoEncontrado.isVisible = false
                binding.recicleCategoria.isVisible = true
                inicarlizarRecicle(usuariosFiltrados.toMutableList())
            }

        }

        agregarCategoriasFiltrado()
        inicizializarFiltradoCat()


    }


    @RequiresApi(Build.VERSION_CODES.O)
    private fun inicizializarFiltradoCat() {
        binding.chipGroupFiltro.setOnCheckedChangeListener { _, checkedId ->
            val filtrado = when (checkedId) {
                R.id.todos -> {
                    binding.linelaFiltrado.isVisible = true
                    binding.editexFilter.setText("")
                    listaTrabajo
                }

                R.id.mas_estrellas -> {
                    binding.linelaFiltrado.isVisible = false
                    binding.editexFilter.setText("")
                    listaTrabajo.filter { trabajo ->
                        (trabajo.start?.toIntOrNull() ?: 0) > 10
                    }
                }

                R.id.activos -> {
                    binding.linelaFiltrado.isVisible = false
                    binding.editexFilter.setText("")
                    listaTrabajo.filter {
                        obtenerActivos(it.horarioam.toString(), it.horariopm.toString())
                    }
                }

                R.id.filtrado_verificado -> {
                    binding.linelaFiltrado.isVisible = false
                    binding.editexFilter.setText("")
                    listaTrabajo.filter {
                        it.verificados == true
                    }
                }

                else -> emptyList()
            }

            aplicarFiltrado(filtrado)
        }
    }

    private fun aplicarFiltrado(lista: List<dataClassTrabajosd>) {
        if (lista.isEmpty()) {
            binding.relativeNoEncontrado.isVisible = true
            binding.recicleCategoria.isVisible = false
        } else {
            binding.relativeNoEncontrado.isVisible = false
            binding.recicleCategoria.isVisible = true
            inicarlizarRecicle(lista.toMutableList())
        }
    }




    @RequiresApi(Build.VERSION_CODES.O)
    private fun obtenerActivos(h1: String, h2: String): Boolean {
        val ahora = LocalTime.now()
        return if (h1.isNotEmpty() && h2.isNotEmpty()) {
            try {
                val formatter = DateTimeFormatter.ofPattern("HH:mm")
                val apertura = LocalTime.parse(h1, formatter)
                val cierre = LocalTime.parse(h2, formatter)

                ahora.isAfter(apertura) && ahora.isBefore(cierre)
            } catch (e: Exception) {
                false
            }
        } else {
            false
        }
    }

    private fun agregarCategoriasFiltrado() {
        categoriasFiltrado.add(dataclasFiltradoTrabaadores(Variables.Todos))
        categoriasFiltrado.add(dataclasFiltradoTrabaadores(Variables.Verificado))
        categoriasFiltrado.add(dataclasFiltradoTrabaadores(Variables.Más_estrellas))
        categoriasFiltrado.add(dataclasFiltradoTrabaadores(Variables.Activos))
    }

    private fun obtenerTrabajosdb(filtrado: String, categoriap: String) {
        listaTrabajo.clear()
        val userCollections =
            FirebaseFirestore.getInstance().collection(Variables.trabajadores_usuariosDB)
                .document(Variables.trabajadoresDB).collection(Variables.trabajadoresDB)
        binding.loading.isVisible = true
        binding.relativeNoEncontrado.isVisible = false
        binding.appbarLayout.isVisible = false
        binding.recicleCategoria.isVisible = false

        userCollections.get()
            .addOnSuccessListener { querySnapshot ->
                if (!querySnapshot.isEmpty) {
                    for (documnet in querySnapshot.documents) {
                        val categoriaTrabajo = documnet.getString(Variables.categoriaTrabajo)
                        if (categoriaTrabajo == categoriap) {
                            val nombre = documnet.getString(Variables.nombre)
                            val apellido = documnet.getString(Variables.apellido)
                            val c1 = documnet.getString(Variables.caracteristica1)
                            val c2 = documnet.getString(Variables.caracteristica2)
                            val c3 = documnet.getString(Variables.caracteristica3)
                            val codigoPais = documnet.getString(Variables.codigo_pais)
                            val estrellas = documnet.getString(Variables.estrellas)
                            val fechaNac = documnet.getString(Variables.fechaNac)
                            val genero = documnet.getString(Variables.genero)
                            val horario1 = documnet.getString(Variables.horario1)
                            val horario2 = documnet.getString(Variables.horario2)
                            val idUSer = documnet.getString(Variables.id)
                            val imagenPerfil = documnet.getString(Variables.imagenPerfil)
                            val localidad = documnet.getString(Variables.localidad)
                            val nacionalidad = documnet.getString(Variables.nacionalidad)
                            val numero = documnet.getString(Variables.numero)
                            val tipoTrabajo = documnet.getString(Variables.tipoTrabajo)
                            val activo = documnet?.get(Variables.activado) as? String
                            val EdadaActual = documnet?.get(Variables.EdadActual) as? String
                            val verificados = documnet?.get(Variables.verificado) as? Boolean
                            val user = dataClassTrabajosd(
                                idUSer,
                                apellido,
                                c1,
                                c2,
                                c3,
                                categoriaTrabajo,
                                fechaNac,
                                genero,
                                horario1,
                                horario2,
                                nacionalidad,
                                nombre,
                                estrellas,
                                tipoTrabajo,
                                localidad,
                                codigoPais,
                                numero,
                                imagenPerfil,
                                activo, EdadaActual, verificados
                            )
                            if (localidad == filtrado) {
                                println("hacemos el filtrado de usuarios trabajoderes $localidad == $filtrado")
                                listaTrabajo.add(user)
                            } else if (filtrado == "General") {
                                listaTrabajo.add(user)
                            }
                        }
                    }
                    inicarlizarRecicle(listaTrabajo)
                    actualizarVisibilidad(
                        listaTrabajo.isNotEmpty(),
                        binding.loading,
                        binding.appbarLayout,
                        binding.recicleCategoria, binding.relativeNoEncontrado
                    )
                    binding.loading.isVisible = false
                    binding.recicleCategoria.isVisible = true
                } else {
                    Toast.makeText(
                        this,
                        "no se encontraro usuarios en este campo",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
            .addOnFailureListener { exeption ->
                Toast.makeText(this, "Error al obtener documentos: $exeption", Toast.LENGTH_SHORT)
                    .show()

            }
    }

    private fun mejoresTrabajadores(filtrado: String) {
        listaTrabajo.clear()
        val userCollections =
            FirebaseFirestore.getInstance().collection(Variables.trabajadores_usuariosDB)
                .document(Variables.trabajadoresDB).collection(Variables.trabajadoresDB)
        binding.loading.isVisible = true
        binding.relativeNoEncontrado.isVisible = false
        binding.appbarLayout.isVisible = false
        binding.recicleCategoria.isVisible = false
        userCollections.get()
            .addOnSuccessListener { querySnapshot ->
                for (document in querySnapshot.documents) {
                    val userData = document.data
                    val estrellas = userData?.get("estrellas") as? String ?:"0"
                    val estrellasInt = estrellas!!.toInt()

                    if (estrellasInt > 40) {
                        val nombre = userData?.get(Variables.nombre) as? String?:""
                        val apellido = userData?.get(Variables.apellido) as? String?:""
                        val caracteristica1 = userData?.get(Variables.caracteristica1) as? String?:""
                        val caracteristica2 = userData?.get(Variables.caracteristica2) as? String?:""
                        val caracteristica3 = userData?.get(Variables.caracteristica3) as? String?:""
                        val categoriaTrabajo = userData?.get(Variables.categoriaTrabajo) as? String?:""
                        val codigoPais = userData?.get(Variables.codigo_pais) as? String?:""
                        val fechaNac = userData?.get(Variables.fechaNac) as? String?:""
                        val genero = userData?.get(Variables.genero) as? String?:""
                        val horario1 = userData?.get(Variables.horario1) as? String?:""
                        val horario2 = userData?.get(Variables.horario2) as? String?:""
                        val id = (userData?.get(Variables.id) as? String).toString()?:""
                        img = (userData?.get(Variables.imagenPerfil) as? String).toString()?:""
                        val localidad = userData?.get(Variables.localidad) as? String?:""
                        val nacionalidad = userData?.get(Variables.nacionalidad) as? String?:""
                        val numero = userData?.get(Variables.numero) as? String?:""
                        val tipoTrabajo = userData?.get(Variables.tipoTrabajo) as? String?:""
                        val activo = userData?.get(Variables.activado) as? String?:""
                        val EdadaActual = userData?.get(Variables.EdadActual) as? String?:""
                        val verificados = userData?.get(Variables.verificado) as? Boolean?:false
                        val usuario = dataClassTrabajosd(
                            id,
                            apellido,
                            caracteristica1,
                            caracteristica2,
                            caracteristica3,
                            categoriaTrabajo,
                            fechaNac,
                            genero,
                            horario1,
                            horario2,
                            nacionalidad,
                            nombre,
                            estrellas.toString(),
                            tipoTrabajo,
                            localidad,
                            codigoPais,
                            numero,
                            img, activo, EdadaActual, verificados
                        )
                        if (localidad == filtrado) {
                            listaTrabajo.add(usuario)
                        } else if (filtrado == "General") {
                            listaTrabajo.add(usuario)
                        }
                    }
                }
                inicarlizarRecicle(listaTrabajo)
                actualizarVisibilidad(
                    listaTrabajo.isNotEmpty(),
                    binding.loading,
                    binding.appbarLayout,
                    binding.recicleCategoria, binding.relativeNoEncontrado
                )
                binding.loading.isVisible = false
                binding.recicleCategoria.isVisible = true
            }
    }

    private fun actualizarVisibilidad(
        hayArticulos: Boolean,
        loading: LinearLayoutCompat,
        lineal: LinearLayout,
        recielAnuncios: RecyclerView,
        no_resultados: RelativeLayout
    ) {
        loading.isVisible = false
        if (hayArticulos) {
            lineal.isVisible = true
            loading.isVisible = false
            recielAnuncios.isVisible = true
            no_resultados.isVisible = false
        } else {
            lineal.isVisible = false
            loading.isVisible = true
            recielAnuncios.isVisible = false
            no_resultados.isVisible = true
        }
    }


    private fun inicarlizarRecicle(listaTrabajos: MutableList<dataClassTrabajosd>) {
        val recicle = binding.recicleCategoria
        val layoutManager = GridLayoutManager(this, 2)
        recicle.layoutManager = layoutManager
        recicle.adapter =
            adapter(
                false,
                listaTrabajos,
                firebaseAuth.uid.toString(), listaTrabajos.size,true
            )

        layoutManager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
            override fun getSpanSize(position: Int): Int {
                return if (position < 2) {
                    1
                } else {
                    1
                }
            }
        }
        val totalUsuarios = listaTrabajos.size
        binding.totalUser.setText("Encontrados ($totalUsuarios) Trabajadores")
        if (totalUsuarios == 0) {
            binding.relativeNoEncontrado.isVisible = true
        } else {
            binding.relativeNoEncontrado.isVisible = false

        }
    }
}