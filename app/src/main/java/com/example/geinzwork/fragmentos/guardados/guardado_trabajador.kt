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
import androidx.recyclerview.widget.GridLayoutManager
import com.example.geinzwork.constantesGeneral.Variables
import com.geinzz.geinzwork.R
import com.geinzz.geinzwork.adapterViewholder.adapter
import com.geinzz.geinzwork.databinding.FragmentGuardadoTrabajadorBinding
import com.geinzz.geinzwork.dataclass.dataClassTrabajosd
import com.google.android.material.chip.Chip
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore


class guardado_trabajador : Fragment() {

    private lateinit var binding: FragmentGuardadoTrabajadorBinding
    private lateinit var mcontex: Context
    private val listaTrabajo = mutableListOf<dataClassTrabajosd>().toMutableList()
    private val listaCategoriasChips = mutableSetOf<String>()
    private var totalTrabajadores = 0
    private var cargados = 0
    private lateinit var firebaseAuth: FirebaseAuth
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
        binding = FragmentGuardadoTrabajadorBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        firebaseAuth = FirebaseAuth.getInstance()
        obtener_trabajadores_guardados()
        binding.encontrados.isNestedScrollingEnabled = false
    }

    private fun obtener_trabajadores_guardados() {
        binding.linealCargando.isVisible = true
        binding.encontrados.isVisible = false
        val db2 = FirebaseFirestore.getInstance()
            .collection(Variables.trabajadores_usuariosDB)
            .document(Variables.trabajadoresDB)
            .collection(Variables.trabajadoresDB)
            .document(firebaseAuth.uid.toString())
            .collection("guardados").document("guardados").collection("trabajadores")

        db2.get().addOnSuccessListener { res ->
            totalTrabajadores = res.size()
            cargados = 0
            listaCategoriasChips.clear() // Limpia antes de empezar
            listaTrabajo.clear()
            for (datos in res) {
                val data = datos.data
                val id = data?.get("id_trabajador") as? String ?: ""
                guardados(id)
            }
        }
    }

    private fun guardados(idTrabajador: String) {
        binding.linealGeneralCarga.isVisible=false
        val tiempoInicio = System.currentTimeMillis()
        val userCollections =
            FirebaseFirestore.getInstance().collection(Variables.trabajadores_usuariosDB)
                .document(Variables.trabajadoresDB).collection(Variables.trabajadoresDB)
                .document(idTrabajador)

        userCollections.get()
            .addOnSuccessListener { res ->
                val tiempoFin = System.currentTimeMillis()
                val duracion = tiempoFin - tiempoInicio
                Log.d(
                    "TIEMPO_FIREBASE",
                    "⏱ Tiempo en obtener datos de $idTrabajador: ${duracion}ms"
                )

                if (res.exists()) {
                    val userData = res.data
                    val categoriaTrabajoStr =
                        userData?.get(Variables.categoriaTrabajo) as? String ?: ""


                    val estrellas = userData?.get("estrellas") as? String ?: "0"
                    val nombre = userData?.get(Variables.nombre) as? String ?: ""
                    val apellido = userData?.get(Variables.apellido) as? String ?: ""
                    val caracteristica1 = userData?.get(Variables.caracteristica1) as? String ?: ""
                    val caracteristica2 = userData?.get(Variables.caracteristica2) as? String ?: ""
                    val caracteristica3 = userData?.get(Variables.caracteristica3) as? String ?: ""
                    val codigoPais = userData?.get(Variables.codigo_pais) as? String ?: ""
                    val fechaNac = userData?.get(Variables.fechaNac) as? String ?: ""
                    val genero = userData?.get(Variables.genero) as? String ?: ""
                    val horario1 = userData?.get(Variables.horario1) as? String ?: ""
                    val horario2 = userData?.get(Variables.horario2) as? String ?: ""
                    val id = userData?.get(Variables.id) as? String ?: ""
                    val img = userData?.get(Variables.imagenPerfil) as? String ?: ""
                    val localidad = userData?.get(Variables.localidad) as? String ?: ""
                    val nacionalidad = userData?.get(Variables.nacionalidad) as? String ?: ""
                    val numero = userData?.get(Variables.numero) as? String ?: ""
                    val tipoTrabajo = userData?.get(Variables.tipoTrabajo) as? String ?: ""
                    val activo = userData?.get(Variables.activado) as? String ?: ""
                    val edadActual = userData?.get(Variables.EdadActual) as? String ?: ""
                    val verificados = userData?.get(Variables.verificado) as? Boolean ?: false

                    val usuario = dataClassTrabajosd(
                        id, apellido, caracteristica1, caracteristica2, caracteristica3,
                        categoriaTrabajoStr, fechaNac, genero, horario1, horario2,
                        nacionalidad, nombre, estrellas, tipoTrabajo, localidad,
                        codigoPais, numero, img, activo, edadActual, verificados
                    )

                    listaTrabajo.add(usuario)

                    val categorias = categoriaTrabajoStr.split(",")
                        .map { it.trim() }
                        .filter { it.isNotEmpty() }
                    listaCategoriasChips.addAll(categorias)

                    println("La lista agregada es $listaTrabajo")

                }

                cargados++
                if (cargados == totalTrabajadores) {
                    handler.postDelayed({
                        binding.encontrados.isVisible = true
                        binding.linealCargando.isVisible = false
                        binding.linealFitlradoTrabajadores.isVisible = true
                        binding.linealGeneralCarga.isVisible=true
                    }, duracion)

                    setear_chisp_filtrado(listaCategoriasChips)
                    inicarlizarRecicle(listaTrabajo)
                }
            }
    }
    private fun setear_chisp_filtrado(categorias: Set<String>) {
        println("la lista chipsssss es $categorias")
        val chipGroup = binding.chipGroupFiltradoTrabajadore
        chipGroup.removeAllViews()
        chipGroup.isSingleSelection = true

        // Agregar chip "Todos" primero
        val chipTodos = Chip(chipGroup.context).apply {
            text = "Todos"
            isCheckable = true
            isClickable = true
            isChecked = true // Aparece marcado al inicio
            id = View.generateViewId()
        }
        chipGroup.addView(chipTodos)

        // Agregar el resto de chips
        for (categoria in categorias) {
            val chip = Chip(chipGroup.context).apply {
                text = categoria
                isCheckable = true
                isClickable = true
                id = View.generateViewId()
            }
            chipGroup.addView(chip)
        }

        // Listener para cambios
        chipGroup.setOnCheckedChangeListener { group, checkedId ->
            val selectedChip: Chip? = group.findViewById(checkedId)
            selectedChip?.let {
                println("Categoría seleccionada: ${it.text}")
                if (it.text.toString() == "Todos") {
                    obtener_trabajadores_guardados() // ✅ Solo se llama si el usuario hace clic en "Todos"
                } else {
                    actualizar_guardados(it.text.toString())
                }
            }
        }
    }



    private fun actualizar_guardados(filtrado: String) {
        binding.linealCargando.isVisible = true
        binding.encontrados.isVisible = false
        binding.linealGeneralCarga.isVisible=false
        val db2 = FirebaseFirestore.getInstance()
            .collection(Variables.trabajadores_usuariosDB)
            .document(Variables.trabajadoresDB)
            .collection(Variables.trabajadoresDB)
            .document(firebaseAuth.uid.toString())
            .collection("guardados").document("guardados").collection("trabajadores")

        db2.get().addOnSuccessListener { res ->
            totalTrabajadores = res.size()
            cargados = 0
            listaCategoriasChips.clear() // Limpia antes de empezar
            listaTrabajo.clear()
            for (datos in res) {
                val data = datos.data
                val id = data?.get("id_trabajador") as? String ?: ""
                val tiempoInicio = System.currentTimeMillis()


                val userCollections =
                    FirebaseFirestore.getInstance().collection(Variables.trabajadores_usuariosDB)
                        .document(Variables.trabajadoresDB).collection(Variables.trabajadoresDB)
                        .document(id)

                userCollections.get()
                    .addOnSuccessListener { res ->
                        val tiempoFin = System.currentTimeMillis()
                        val duracion = tiempoFin - tiempoInicio
                        Log.d(
                            "TIEMPO_FIREBASE",
                            "⏱ Tiempo en obtener datos de $id: ${duracion}ms"
                        )

                        if (res.exists()) {
                            val userData = res.data
                            val categoriaTrabajoStr =
                                userData?.get(Variables.categoriaTrabajo) as? String ?: ""
                            Log.d("entradmos", "$categoriaTrabajoStr = $filtrado")
                            if (filtrado == categoriaTrabajoStr) {
                                Log.d("entradmos", "estamos bine ")
                                val estrellas = userData?.get("estrellas") as? String ?: "0"
                                val nombre = userData?.get(Variables.nombre) as? String ?: ""
                                val apellido = userData?.get(Variables.apellido) as? String ?: ""
                                val caracteristica1 =
                                    userData?.get(Variables.caracteristica1) as? String ?: ""
                                val caracteristica2 =
                                    userData?.get(Variables.caracteristica2) as? String ?: ""
                                val caracteristica3 =
                                    userData?.get(Variables.caracteristica3) as? String ?: ""
                                val codigoPais =
                                    userData?.get(Variables.codigo_pais) as? String ?: ""
                                val fechaNac = userData?.get(Variables.fechaNac) as? String ?: ""
                                val genero = userData?.get(Variables.genero) as? String ?: ""
                                val horario1 = userData?.get(Variables.horario1) as? String ?: ""
                                val horario2 = userData?.get(Variables.horario2) as? String ?: ""
                                val id = userData?.get(Variables.id) as? String ?: ""
                                val img = userData?.get(Variables.imagenPerfil) as? String ?: ""
                                val localidad = userData?.get(Variables.localidad) as? String ?: ""
                                val nacionalidad =
                                    userData?.get(Variables.nacionalidad) as? String ?: ""
                                val numero = userData?.get(Variables.numero) as? String ?: ""
                                val tipoTrabajo =
                                    userData?.get(Variables.tipoTrabajo) as? String ?: ""
                                val activo = userData?.get(Variables.activado) as? String ?: ""
                                val edadActual =
                                    userData?.get(Variables.EdadActual) as? String ?: ""
                                val verificados =
                                    userData?.get(Variables.verificado) as? Boolean ?: false

                                val usuario = dataClassTrabajosd(
                                    id, apellido, caracteristica1, caracteristica2, caracteristica3,
                                    categoriaTrabajoStr, fechaNac, genero, horario1, horario2,
                                    nacionalidad, nombre, estrellas, tipoTrabajo, localidad,
                                    codigoPais, numero, img, activo, edadActual, verificados
                                )

                                listaTrabajo.add(usuario)



                                println("La lista agregada es $listaTrabajo")
                            }
                        }

                        cargados++
                        if (cargados == totalTrabajadores) {
                            handler.postDelayed({
                                binding.encontrados.isVisible = true
                                binding.linealCargando.isVisible = false
                                binding.linealFitlradoTrabajadores.isVisible = true
                                binding.linealGeneralCarga.isVisible=true
                            }, duracion)

                            inicarlizarRecicle(listaTrabajo)
                        }
                    }
            }
        }
        Log.d("entradmos", "entramos a la fun de actualzuiar")
        listaTrabajo.clear()

    }

    private fun inicarlizarRecicle(listaTrabajos: MutableList<dataClassTrabajosd>) {
        val recicle = binding.encontrados
        val layoutManager = GridLayoutManager(mcontex, 2)
        recicle.layoutManager = layoutManager
        recicle.adapter =
            adapter(
                false,
                listaTrabajos,
                firebaseAuth.uid.toString(), listaTrabajos.size, true
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


    }

}