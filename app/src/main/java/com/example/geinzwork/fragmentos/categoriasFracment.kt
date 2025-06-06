package com.geinzz.geinzwork.fragmentos

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SimpleItemAnimator
import com.example.geinzwork.adapterViewholder.adapter_seguidores_seguidos
import com.example.geinzwork.constantesGeneral.Variables
import com.example.geinzwork.constantesGeneral.constantes_trabajadores_info
import com.example.geinzwork.dataclass.dataclass_seguidores_seguidos
import com.geinzz.geinzwork.CuentaFreelancer
import com.geinzz.geinzwork.R
import com.geinzz.geinzwork.adapterViewholder.adapterTrabajos
import com.geinzz.geinzwork.constantesGeneral.constantes
import com.geinzz.geinzwork.constantesGeneral.constantesSubcategoriaszonasTiendas
import com.geinzz.geinzwork.databinding.FragmentCategoriasFracmentBinding
import com.geinzz.geinzwork.dataclass.dataClassTrabajosMostrados
import com.geinzz.geinzwork.vistaTrabajador.vistaTrabajador
import com.geinzz.geinzwork.vistaTrabajador.vista_CategoriasT
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class categoriasFracment : Fragment() {

    private lateinit var binding: FragmentCategoriasFracmentBinding
    private lateinit var mContex: Context
    private lateinit var firebaseAuth:FirebaseAuth
    private val listaanunciosEncontrados = mutableListOf<dataclass_seguidores_seguidos>()
    private lateinit var adapter_seguidores_seguidos: adapter_seguidores_seguidos

    override fun onAttach(context: Context) {
        mContex = context
        super.onAttach(context)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentCategoriasFracmentBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        firebaseAuth=FirebaseAuth.getInstance()
//        constantes.carga(1000, { mostrarDatos() })
        confSwipe()
        obtenerCategorias(binding.loading)
        obtener_trabajadores_nombre()
        (binding.recycleMostrarTrabajadores.itemAnimator as? SimpleItemAnimator)?.supportsChangeAnimations = false

        binding.search.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val textoBusqueda = s.toString().trim()

                if (textoBusqueda.isEmpty()) {
                    // No se está buscando nada
                    binding.RecicleViewTrabajos.isVisible = true
                    binding.recycleMostrarTrabajadores.isVisible = false
                    binding.linealNoSeEncontraron.isVisible = false
                    binding.cargaTrabajadores.isVisible = false

                    // Mostrar los títulos otra vez si no se está buscando
                    binding.titulosTextos.isVisible = true

                } else {
                    // Oculta visualmente los títulos, pero mantiene su espacio (no afecta el focus del EditText)
                    binding.titulosTextos.isVisible = false


                    binding.cargaTrabajadores.isVisible = true
                    binding.RecicleViewTrabajos.isVisible = false
                    binding.recycleMostrarTrabajadores.isVisible = false
                    binding.linealNoSeEncontraron.isVisible = false

                    val resultadosFiltrados = if (textoBusqueda == "#todos") {
                        listaanunciosEncontrados
                    } else {
                        listaanunciosEncontrados.filter {
                            it.nombre_trabajador?.lowercase()?.contains(textoBusqueda.lowercase()) == true
                        }
                    }

                    binding.recycleMostrarTrabajadores.post {
                        adapter_seguidores_seguidos.actualizarLista(resultadosFiltrados)
                        binding.cargaTrabajadores.isVisible = false
                        binding.recycleMostrarTrabajadores.isVisible = resultadosFiltrados.isNotEmpty()
                        binding.linealNoSeEncontraron.isVisible = resultadosFiltrados.isEmpty()
                    }
                }
            }

            override fun afterTextChanged(s: Editable?) {}
        })



    }

    private fun mostrarDatos() {
        binding.loading.isVisible = true
        binding.swipe.isVisible = true
        obtenerCategorias(binding.loading)
    }

    private fun actualizarVisibilidad(hayArticulos: Boolean) {
        binding.loading.isVisible = false
        if (hayArticulos) {
            binding.linealPrincipal.isVisible = true
            binding.loading.isVisible = false
            binding.RecicleViewTrabajos.isVisible = true
        } else {
            binding.RecicleViewTrabajos.isVisible = false
            binding.linealPrincipal.isVisible = false
            binding.loading.isVisible = true
        }
    }


    @RequiresApi(Build.VERSION_CODES.O)
    private fun confSwipe() {
        binding.swipe.setOnRefreshListener {
            binding.swipe.setColorSchemeResources(R.color.violeta)
            Handler(Looper.getMainLooper()).postDelayed({
                binding.linealPrincipal.isVisible = false
                binding.swipe.isRefreshing = false
                obtenerCategorias(binding.loading)
            }, 2000)
            binding.swipe.isVisible = true
            binding.linealPrincipal.isVisible = true
            binding.search.setText("")

        }
    }


    private fun obtenerCategorias(loadingView: View) {
        val trabajos = mutableListOf<dataClassTrabajosMostrados>()
        val db = FirebaseFirestore.getInstance().collection(Variables.categoriasDB)
            .document(Variables.categoriasTrabajo)

        loadingView.isVisible = true

        val startTime = System.currentTimeMillis() // Tiempo inicio

        db.get()
            .addOnSuccessListener { res ->
                if (res.exists()) {
                    val categorias = res.get(Variables.categoriasDB) as? ArrayList<String>
                    categorias?.let { listaCategorias ->

                        var count = 0
                        if (listaCategorias.isEmpty()) {

                            val totalDuration = System.currentTimeMillis() - startTime
                            println("obtenerCategorias terminó en $totalDuration ms (sin categorías)")
                            loadingView.isVisible = false
                            actualizarVisibilidad(false)
                            return@addOnSuccessListener
                        }

                        for (categoria in listaCategorias) {
                            constantesSubcategoriaszonasTiendas.obtenerSubcategorias(
                                Variables.subcategoriasTrabajos,
                                categoria,
                                onSuccess = { subcategorias ->

                                    constantesSubcategoriaszonasTiendas.obtenerImagenesCategorias(
                                        Variables.IMG_CategoriasGeneral,
                                        Variables.categroriasTrabajadores,
                                        categoria,
                                        onSuccess = { urlImg ->

                                            val data = dataClassTrabajosMostrados(
                                                categoria,
                                                urlImg ?: "",
                                                subcategorias
                                            )
                                            trabajos.add(data)

                                            count++
                                            if (count == listaCategorias.size) {
                                                // Aquí terminan todas las cargas
                                                val totalDuration = System.currentTimeMillis() - startTime
                                                println("obtenerCategorias completó todo en $totalDuration ms")
                                                Handler(Looper.getMainLooper()).postDelayed({

                                                    binding.loading.isVisible = false
                                                    binding.swipe.isVisible = true
                                                    inicalizarREciocle(
                                                        binding.RecicleViewTrabajos,
                                                        trabajos
                                                    )
                                                    actualizarVisibilidad(true)
                                                }, totalDuration)


                                            }
                                        },
                                        onFailure = { error ->
                                            println("Error al obtener las imágenes para $categoria: ${error.message}")
                                            count++
                                            if (count == listaCategorias.size) {
                                                val totalDuration = System.currentTimeMillis() - startTime
                                                println("obtenerCategorias completó todo con errores en $totalDuration ms")

                                                inicalizarREciocle(
                                                    binding.RecicleViewTrabajos,
                                                    trabajos
                                                )
                                                actualizarVisibilidad(true)
                                                loadingView.isVisible = false
                                            }
                                        }
                                    )
                                },
                                onFailure = { error ->
                                    println("Error al obtener subcategorías para $categoria: ${error.message}")
                                    count++
                                    if (count == listaCategorias.size) {
                                        val totalDuration = System.currentTimeMillis() - startTime
                                        println("obtenerCategorias completó todo con errores en $totalDuration ms")

                                        inicalizarREciocle(
                                            binding.RecicleViewTrabajos,
                                            trabajos
                                        )
                                        actualizarVisibilidad(true)
                                        loadingView.isVisible = false
                                    }
                                }
                            )
                        }
                    } ?: run {
                        // Si no hay lista (null)
                        val totalDuration = System.currentTimeMillis() - startTime
                        println("No hay categorías, función terminó en $totalDuration ms")
                        loadingView.isVisible = false
                        actualizarVisibilidad(false)
                    }
                } else {
                    println("No se encontró el documento 'categoriasTiendas'")
                    loadingView.isVisible = false
                    actualizarVisibilidad(false)
                }
            }
            .addOnFailureListener { exception ->
                println("Error al obtener el documento 'categoriasTiendas': $exception")
                loadingView.isVisible = false
                actualizarVisibilidad(false)
            }
    }


    private fun inicalizarREciocle(
        recicleTrabajos: RecyclerView,
        lista: MutableList<dataClassTrabajosMostrados>
    ) {
        val recicle = recicleTrabajos
        recicle.layoutManager = GridLayoutManager(mContex, 2)
        recicle.adapter = adapterTrabajos(lista, { dataClassTrabajosMostrados ->
            mandarvistaTrabajos(dataClassTrabajosMostrados)
        })
    }


    private fun mandarvistaTrabajos(dataClassTrabajosMostrados: dataClassTrabajosMostrados) {
        var intent = Intent(mContex, vista_CategoriasT::class.java)
        intent.putExtra(Variables.valor, dataClassTrabajosMostrados.categorias)
        startActivity(intent)
    }

    // 1. Cargar los trabajadores una vez
    private fun obtener_trabajadores_nombre() {
        val db = FirebaseFirestore.getInstance()
            .collection("Trabajadores_Usuarios_Drivers")
            .document("trabajadores").collection("trabajadores")

        db.get().addOnSuccessListener { res ->
            listaanunciosEncontrados.clear()
            for (datos in res) {
                val data = datos.data
                val nombre = data["nombre"] as? String ?: ""
                val apellido = data["apellido"] as? String ?: ""
                val imagenPerfil = data["imagenPerfil"] as? String ?: ""
                val tipoTrabajo = data["tipoTrabajo"] as? String ?: ""
                val nacionalidad = data["nacionalidad"] as? String ?: ""
                val verificado = data["verificado"] as? Boolean ?: false
                val id = data["id"] as? String ?: ""

                val trabajador = dataclass_seguidores_seguidos(
                    id,
                    imagenPerfil,
                    "$nombre $apellido",
                    tipoTrabajo,
                    nacionalidad,
                    verificado
                )
                listaanunciosEncontrados.add(trabajador)
            }

            inizialar_seguir_seguidores(listaanunciosEncontrados)
        }
    }

    private fun inizialar_seguir_seguidores(
        lista_seguidores: MutableList<dataclass_seguidores_seguidos>,
    ) {

        adapter_seguidores_seguidos = adapter_seguidores_seguidos(
            lista_seguidores,"",
            { item ->
                val vista_t = Intent(mContex, vistaTrabajador::class.java).apply {
                    putExtra(Variables.id, item.id_trabajador)
                    putExtra(Variables.imagenPerfil, item.img_perfil)
                    putExtra(Variables.nombreUSer, item.nombre_trabajador)
                    putExtra(Variables.nacionalidad, item.nacionalidad)
                    putExtra(Variables.categoria, item.tipo_trabajado)
                }

                startActivity(vista_t)
            },
            seguir = { item ->
                seguirUsuario(item.id_trabajador)
            },
            dejar_seguir = { item ->
                constantes_trabajadores_info.dejarSeguirTrabajadorcaregoriasFR(
                    item.id_trabajador.toString(),
                )
            },
        )
        binding.recycleMostrarTrabajadores.layoutManager =
            LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        binding.recycleMostrarTrabajadores.adapter =
            adapter_seguidores_seguidos

    }

    private fun seguirUsuario(idTrabajador: String?) {
            if (firebaseAuth.currentUser == null) {
                val builder = androidx.appcompat.app.AlertDialog.Builder(mContex)
                builder.setTitle("No estás registrado en Geinz Work")
                builder.setMessage("Regístrate en Geinz Work para que puedas seguir.")
                builder.setPositiveButton("Cuenta Simple") { dialog, _ ->
                    // Mostrar diálogo de carga y redirigir a la pantalla de registro
                    constantes.showLoadingDialog(
                        mContex,
                        2000,
                        "Cargando información",
                        "Espere un momento..."
                    )
                    val intent = Intent(mContex, CuentaFreelancer::class.java).apply {
                        putExtra("tipoCuenta", "cuentaSimple")
                        putExtra("Title", "Cuenta Simple")
                        putExtra("pasos", "Estás a 1/2 pasos")
                    }
                    mContex.startActivity(intent)
                    dialog.dismiss()
                }
                builder.setNegativeButton("Cuenta Trabajador") { dialog, _ ->
                    val intent = Intent(mContex, CuentaFreelancer::class.java).apply {
                        putExtra("tipoCuenta", "cuentaTrabajador")
                        putExtra("Title", "Cuenta Freelancer")
                        putExtra("pasos", "Estás a 1/5 pasos")
                    }
                    mContex.startActivity(intent)
                    dialog.dismiss()
                }
                builder.create().show()
            } else {
                constantes_trabajadores_info.seguirTrabajadorcategoriasFR(null,idTrabajador!!, false,false)
            }


    }

}




