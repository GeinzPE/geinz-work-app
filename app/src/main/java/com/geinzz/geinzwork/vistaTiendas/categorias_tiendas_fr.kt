package com.geinzz.geinzwork.vistaTiendas

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.preference.PreferenceManager
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.geinzz.geinzwork.ui.adapters.adapterCategoriasTiendas
import com.geinzz.geinzwork.utils.constantes.constantes.constantes
import com.geinzz.geinzwork.utils.constantes.constantes.constantesSubcategoriaszonasTiendas
import com.geinzz.geinzwork.utils.constantes.constantes.filtradoLocalidadElementos
import com.geinzz.geinzwork.databinding.FragmentCategoriasTiendasFrBinding
import com.geinzz.geinzwork.model.dataClassCategoriasTiendas
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.firestore.FirebaseFirestore

class categorias_tiendas_fr : Fragment() {
    private lateinit var binding: FragmentCategoriasTiendasFrBinding
    private lateinit var mcontex: Context
    private lateinit var dialog: BottomSheetDialog
    private val KEYLOCALIDAD = "MY_KEYLOCALIDA"

    companion object {
        fun newInstance(filtrado: String): categorias_tiendas_fr {
            val fragment = categorias_tiendas_fr()
            val args = Bundle()
            args.putString("filtrado", filtrado)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onAttach(context: Context) {
        mcontex = context
        super.onAttach(context)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentCategoriasTiendasFrBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        constantes.carga(1000, {  obtenerCategorias(binding.loading) })

        val pref = PreferenceManager.getDefaultSharedPreferences(mcontex)
        val storedValue = pref?.getString(KEYLOCALIDAD, "Default Value")
        println("el valor de key es ${storedValue}")
        val filtrado = arguments?.getString("filtrado").toString()

        if (storedValue.isNullOrEmpty() || storedValue == "Default Value") {
            binding.filtradomandado.text = filtrado
        } else {
            binding.filtradomandado.text = storedValue
        }

        binding.filtrado.setOnClickListener {
            val dialog = BottomSheetDialog(mcontex)
            filtradoLocalidadElementos.filtradoNacionalidades(
                "Seleccione su filtrado de Tiendas",
                mcontex,
                dialog
            ) { seleccion ->
                binding.filtradomandado.text = seleccion ?: "General"
                guardarShaderPref(pref, seleccion ?: "General")
            }
            dialog.show()
        }

    }

    private fun obtenerCategorias(loadingView: View) {
        val categoriasTiendas = mutableListOf<dataClassCategoriasTiendas>()
        val db = FirebaseFirestore.getInstance().collection("categorias").document("categoriasTiendas")
        loadingView.isVisible = true // Mostrar loading

        db.get()
            .addOnSuccessListener { res ->
                if (res.exists()) {
                    val categorias = res.get("categorias") as? ArrayList<String>
                    categorias?.let { listaCategorias ->
                        // Contador para manejar la sincronización de las respuestas asíncronas
                        var count = 0
                        for (categoria in listaCategorias) {
                            constantesSubcategoriaszonasTiendas.obtenerSubcategorias("subcategoriasTiendas",categoria,
                                onSuccess = { subcategorias ->
                                    constantesSubcategoriaszonasTiendas.obtenerImagenesCategorias("IMG_CategoriasGeneral","categoriasTienda", categoria,
                                        onSuccess = { urlImg ->
                                            // Aquí creas el objeto dataClassCategoriasTiendas con los datos obtenidos
                                            val data = dataClassCategoriasTiendas(
                                                categoria,
                                                urlImg ?: "", // Usar placeholder o imagen por defecto
                                                subcategorias
                                            )
                                            categoriasTiendas.add(data)

                                            // Incrementar el contador y verificar si se completaron todas las categorías
                                            count++
                                            if (count == listaCategorias.size) {
                                                // Cuando se han obtenido todos los datos, inicializar el RecyclerView
                                                inicializarRecicle(
                                                    binding.recieleCategorias,
                                                    categoriasTiendas
                                                )
                                                actualizarVisibilidad(true)
                                            }
                                        },
                                        onFailure = { error ->
                                            println("Error al obtener las imágenes para $categoria: ${error.message}")
                                            // Manejar el error si es necesario
                                            count++ // Incrementar el contador en caso de error
                                            if (count == listaCategorias.size) {
                                                // Si se completan todas las categorías, inicializar el RecyclerView
                                                inicializarRecicle(
                                                    binding.recieleCategorias,
                                                    categoriasTiendas
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
                                        inicializarRecicle(
                                            binding.recieleCategorias,
                                            categoriasTiendas
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

    private fun actualizarVisibilidad(hayArticulos: Boolean) {
        binding.loading.isVisible = false
        if (hayArticulos) {
            binding.linealPrincipal.isVisible = true
            binding.loading.isVisible = false
            binding.filtrado.isVisible = true
        } else {
            binding.linealPrincipal.isVisible = false
            binding.loading.isVisible = true
            binding.filtrado.isVisible = false
        }
    }


    private fun inicializarRecicle(
        recicleTrabajos: RecyclerView,
        lista: MutableList<dataClassCategoriasTiendas>
    ) {
        val recicle = recicleTrabajos
        recicle.layoutManager = GridLayoutManager(mcontex, 2)
        recicle.adapter = adapterCategoriasTiendas(lista, { dataClassCategoriasTiendas ->
            mandarVistaTrabajos(dataClassCategoriasTiendas)
        })
    }


    private fun mandarVistaTrabajos(dataClassCategoriasTiendas: dataClassCategoriasTiendas) {
        val intent = Intent(mcontex, Vista_mostrarTodos_Tiendas::class.java)
        intent.putExtra("cat", dataClassCategoriasTiendas.categorias)
        intent.putExtra("localidad",binding.filtradomandado.text.toString())
        startActivity(intent)
    }

    private fun guardarShaderPref(pref: SharedPreferences?, valor: String) {
        val editor = pref?.edit()
        editor?.putString(KEYLOCALIDAD, valor)
        editor?.apply()
    }


}