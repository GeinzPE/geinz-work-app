package com.geinzz.geinzwork.fragmentos

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.core.view.isVisible
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.geinzwork.constantesGeneral.Variables
import com.geinzz.geinzwork.R
import com.geinzz.geinzwork.adapterViewholder.adapterTrabajos
import com.geinzz.geinzwork.constantesGeneral.constantes
import com.geinzz.geinzwork.constantesGeneral.constantesSubcategoriaszonasTiendas
import com.geinzz.geinzwork.databinding.FragmentCategoriasFracmentBinding
import com.geinzz.geinzwork.dataclass.dataClassTrabajosMostrados
import com.geinzz.geinzwork.vistaTrabajador.vista_CategoriasT
import com.google.firebase.firestore.FirebaseFirestore

class categoriasFracment : Fragment() {

    private lateinit var binding: FragmentCategoriasFracmentBinding
    private lateinit var mContex: Context

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
        constantes.carga(1000, { mostrarDatos() })
        confSwipe()
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
                binding.linealPrincipal.isVisible=false
                binding.swipe.isRefreshing = false
                obtenerCategorias(binding.loading)
            }, 2000)
            binding.swipe.isVisible = true
            binding.linealPrincipal.isVisible=true

        }
    }


    private fun obtenerCategorias(loadingView: View) {
        val trabajos = mutableListOf<dataClassTrabajosMostrados>()
        val db = FirebaseFirestore.getInstance().collection(Variables.categoriasDB)
            .document(Variables.categoriasTrabajo)
        loadingView.isVisible = true
        db.get()
            .addOnSuccessListener { res ->
                if (res.exists()) {
                    val categorias = res.get(Variables.categoriasDB) as? ArrayList<String>
                    categorias?.let { listaCategorias ->

                        var count = 0
                        for (categoria in listaCategorias) {
                            constantesSubcategoriaszonasTiendas.obtenerSubcategorias(Variables.subcategoriasTrabajos,
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
                                                inicalizarREciocle(
                                                    binding.RecicleViewTrabajos,
                                                    trabajos
                                                )
                                                actualizarVisibilidad(true)
                                            }
                                        },
                                        onFailure = { error ->
                                            println("Error al obtener las imágenes para $categoria: ${error.message}")
                                            count++ // Incrementar el contador en caso de error
                                            if (count == listaCategorias.size) {
                                                inicalizarREciocle(
                                                    binding.RecicleViewTrabajos,
                                                    trabajos
                                                )
                                                actualizarVisibilidad(true)
                                            }
                                        }
                                    )
                                },
                                onFailure = { error ->
                                    println("Error al obtener subcategorías para $categoria: ${error.message}")
                                    // Manejar el error si es necesario
                                    count++ // Incrementar el contador en caso de error
                                    if (count == listaCategorias.size) {
                                        // Si se completan todas las categorías, inicializar el RecyclerView
                                        inicalizarREciocle(
                                            binding.RecicleViewTrabajos,
                                            trabajos
                                        )
                                        actualizarVisibilidad(true)
                                    }
                                }
                            )
                        }
                    }
                } else {
                    println("No se encontró el documento 'categoriasTiendas'")
                    loadingView.isVisible = false // Ocultar loading en caso de fallo
                    actualizarVisibilidad(false)
                }
            }
            .addOnFailureListener { exception ->
                println("Error al obtener el documento 'categoriasTiendas': $exception")
                loadingView.isVisible = false // Ocultar loading en caso de fallo
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

}




