package com.example.geinzwork.fragmentos.guardados

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.geinzwork.constantesGeneral.Variables
import com.geinzz.geinzwork.R
import com.geinzz.geinzwork.adapterViewholder.adapterguardados
import com.geinzz.geinzwork.databinding.FragmentGuardadoNoticiaBinding
import com.geinzz.geinzwork.dataclass.dataclassVerGuardados
import com.google.android.material.chip.Chip
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class guardado_noticia : Fragment() {
    private lateinit var binding:FragmentGuardadoNoticiaBinding
    private lateinit var mcontex: Context
    private lateinit var firebaseAuth: FirebaseAuth
    private val lista_categoria_noticias = mutableSetOf<String>()
    private val handler = Handler(Looper.getMainLooper())
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onAttach(context: Context) {
        mcontex = context
        super.onAttach(context)
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding=FragmentGuardadoNoticiaBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        firebaseAuth = FirebaseAuth.getInstance()
        obtenernoticias_guardados()
        binding.encontrados.isNestedScrollingEnabled = false

    }


    private fun obtenernoticias_guardados() {
        val tiempoInicio = System.currentTimeMillis() // MARCA DE TIEMPO INICIAL

        val listaprincipal = mutableListOf<dataclassVerGuardados>()
        val db2N = FirebaseFirestore.getInstance()
            .collection(Variables.trabajadores_usuariosDB)
            .document(Variables.trabajadoresDB)
            .collection(Variables.trabajadoresDB)
            .document(firebaseAuth.uid.toString())
            .collection("guardados").document("guardados").collection("noticias")

        db2N.get().addOnSuccessListener { res ->
            for (datos in res) {
                val idDocumento = datos.id
                val data = datos.data
                val id = data?.get(idDocumento) as? String ?: ""
                val categorias = id.split(",")
                    .map { it.trim() }
                    .filter { it.isNotEmpty() }
                lista_categoria_noticias.addAll(categorias)

                setear_chips_filtrado_noticias(lista_categoria_noticias)
                if (data.isNotEmpty()) {
                    val pendingTasks = data.size
                    var completedTasks = 0

                    for ((key, _) in data) {
                        val db2 = FirebaseFirestore.getInstance().collection(Variables.noticias)
                            .document(key)

                        db2.get().addOnSuccessListener { datos ->
                            if (datos.exists()) {
                                val data = datos.data
                                val id = data?.get(Variables.id) as? String ?: ""
                                val titulo = data?.get(Variables.titulo) as? String ?: ""
                                val imagen = data?.get(Variables.imagenUrl) as? String ?: ""
                                val fechaMap = data?.get(Variables.fechas) as? Map<String, Any>
                                val fechaActivacion =
                                    fechaMap?.get(Variables.fecha_activacion) as? String ?: ""

                                val anuncio = dataclassVerGuardados(
                                    imagen,
                                    titulo,
                                    fechaActivacion,
                                    id
                                )
                                listaprincipal.add(anuncio)
                            }

                            completedTasks++
                            if (completedTasks == pendingTasks) {
                                val tiempoFin = System.currentTimeMillis()
                                val tiempoTotal = tiempoFin - tiempoInicio
                                handler.postDelayed({
                                    if (listaprincipal.isNotEmpty()) {
                                        inicializarRecile_noticias(listaprincipal)
                                    }
                                    binding.scrollGeneral.isVisible=true
                                    binding.encontrados.isVisible = true
                                    binding.linealCargando.isVisible = false
                                    binding.linealFiltradoNoticias.isVisible = true
                                }, tiempoTotal)


                            }
                        }
                    }
                }
            }
        }.addOnFailureListener {
            Log.e("TIEMPO_FIREBASE", "Error al obtener noticias", it)
        }
    }
    private fun setear_chips_filtrado_noticias(categorias: Set<String>) {
        val chipGroup = binding.chipGroupFiltradoNoticia
        chipGroup.removeAllViews()
        chipGroup.isSingleSelection = true

        // Agregar el chip "Todos" primero
        val chipTodos = Chip(chipGroup.context).apply {
            text = "Todos"
            isCheckable = true
            isClickable = true
            isChecked = true // Marcarlo como seleccionado por defecto
            id = View.generateViewId()
        }
        chipGroup.addView(chipTodos)

        // Agregar el resto de categorías
        for (categoria in categorias) {
            val chip = Chip(chipGroup.context).apply {
                text = categoria
                isCheckable = true
                isClickable = true
                id = View.generateViewId()
            }
            chipGroup.addView(chip)
        }

        // Listener
        chipGroup.setOnCheckedChangeListener { group, checkedId ->
            val selectedChip: Chip? = group.findViewById(checkedId)
            selectedChip?.let {
                println("Categoría seleccionada: ${it.text}")
            }
        }
    }

    private fun inicializarRecile_noticias(listaAnuncios: MutableList<dataclassVerGuardados>) {
        val recicle = binding.encontrados
        recicle.layoutManager = LinearLayoutManager(mcontex)
        recicle.adapter = adapterguardados(listaAnuncios)
    }
}