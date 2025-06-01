package com.example.geinzwork

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.ArrayAdapter
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.geinzwork.constantesGeneral.Variables
import com.geinzz.geinzwork.R
import com.geinzz.geinzwork.adapterViewholder.adapter
import com.geinzz.geinzwork.adapterViewholder.adapterguardados
import com.geinzz.geinzwork.constantesGeneral.constantesNoticias.firebaseAuth
import com.geinzz.geinzwork.databinding.ActivityNoticiasTrabajadoresGuardadosBinding
import com.geinzz.geinzwork.dataclass.dataClassTrabajosd
import com.geinzz.geinzwork.dataclass.dataclassVerGuardados
import com.google.android.material.chip.Chip
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class noticias_trabajadores_guardados : AppCompatActivity() {
    private lateinit var binding: ActivityNoticiasTrabajadoresGuardadosBinding
    private val listaTrabajo = mutableListOf<dataClassTrabajosd>().toMutableList()
    private val listaCategoriasChips = mutableSetOf<String>()
    private val lista_categoria_noticias=mutableSetOf<String>()
    private val handler = Handler(Looper.getMainLooper())
    private lateinit var firebaseAuth: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNoticiasTrabajadoresGuardadosBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        firebaseAuth = FirebaseAuth.getInstance()

// Modo trabajadores por defecto
        obtener_trabajadores_guardados()
        binding.chipGroupFiltro.check(binding.trabajadoresFiltrado.id)
        binding.linealFiltradoNoticias.isVisible = false
        binding.linealFitlradoTrabajadores.isVisible = true

        binding.trabajadoresFiltrado.setOnClickListener {
            // 🔒 Primero oculta todo
            binding.linealFiltradoNoticias.isVisible = false
            binding.linealFitlradoTrabajadores.isVisible = false
            binding.recicleEncontrados.isVisible = false
            binding.linealCargando.isVisible = true

            binding.chipGroupFiltro.check(binding.trabajadoresFiltrado.id)

            val data = arrayOf(
                "-f-@nombre @categoria @localidad",
                "-f-@localidad",
                "-f-@categoria",
                "-f-#numero de estrellas @categoria"
            )

            val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, data)
            binding.editexFilter.setAdapter(adapter)
            binding.editexFilter.threshold = 1

            binding.editexFilter.setOnItemClickListener { _, _, position, _ ->
                val seleccion = adapter.getItem(position).toString()
                binding.editexFilter.setText("")
                binding.inputnombre.hint = seleccion
                binding.editexFilter.hint = seleccion
            }

            // Limpia texto e inicializa hint
            binding.editexFilter.setText("")
            binding.inputnombre.hint = "Filtra tus trabajadores"
            binding.editexFilter.hint = "Filtra tus trabajadores"

            // Mostrar solo la sección correcta
            binding.linealFitlradoTrabajadores.isVisible = true
            obtener_trabajadores_guardados()
            binding.linealCargando.isVisible = false
            binding.recicleEncontrados.isVisible = true
        }

        binding.noticiasFiltrado.setOnClickListener {
            // 🔒 Oculta todo primero
            binding.linealFiltradoNoticias.isVisible = false
            binding.linealFitlradoTrabajadores.isVisible = false
            binding.recicleEncontrados.isVisible = false
            binding.linealCargando.isVisible = true

            binding.chipGroupFiltro.check(binding.noticiasFiltrado.id)

            // Limpia texto y muestra hint
            binding.editexFilter.setText("")
            binding.inputnombre.hint = "Busca tus noticias por categoría"
            binding.editexFilter.hint = "Busca tus noticias por categoría"

            // Mostrar solo la sección correcta
            binding.linealFiltradoNoticias.isVisible = true
            obtenernoticias_guardados()

            // Opcional: si quieres mostrar recicle después de cargar, hazlo desde la función
        }


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
                                    binding.recicleEncontrados.isVisible = true
                                    binding.linealCargando.isVisible = false
                                    binding.linealFiltradoNoticias.isVisible=true
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

    private fun inicializarRecile_noticias(listaAnuncios: MutableList<dataclassVerGuardados>) {
        val recicle = binding.recicleEncontrados
        recicle.layoutManager = LinearLayoutManager(this)
        recicle.adapter = adapterguardados(listaAnuncios)
    }

    private var totalTrabajadores = 0
    private var cargados = 0

    private fun obtener_trabajadores_guardados() {
        binding.linealCargando.isVisible = true
        binding.recicleEncontrados.isVisible = false
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
                        binding.recicleEncontrados.isVisible = true
                        binding.linealCargando.isVisible = false
                        binding.linealFitlradoTrabajadores.isVisible = true
                    }, duracion)

                    setear_chisp_filtrado(listaCategoriasChips)
                    inicarlizarRecicle(listaTrabajo)
                }
            }
    }

    private fun actualizar_guardados(filtrado: String) {
        binding.linealCargando.isVisible = true
        binding.recicleEncontrados.isVisible = false
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
                                binding.recicleEncontrados.isVisible = true
                                binding.linealCargando.isVisible = false
                                binding.linealFitlradoTrabajadores.isVisible = true
                            }, duracion)

                            inicarlizarRecicle(listaTrabajo)
                        }
                    }
            }
        }
        Log.d("entradmos", "entramos a la fun de actualzuiar")
        listaTrabajo.clear()

    }


    private fun setear_chisp_filtrado(categorias: Set<String>) {
        println("la lista chipsssss es $categorias")
        val chipGroup = binding.chipGroupFiltradoTrabajadore
        chipGroup.removeAllViews()
        chipGroup.isSingleSelection = true

        for (categoria in categorias) {
            val chip = Chip(chipGroup.context).apply {
                text = categoria
                isCheckable = true
                isClickable = true
                id = View.generateViewId() // Importante para identificar el chip seleccionado
            }
            chipGroup.addView(chip)
        }

        chipGroup.setOnCheckedChangeListener { group, checkedId ->
            val selectedChip: Chip? = group.findViewById(checkedId)
            selectedChip?.let {
                println("Categoría seleccionada: ${it.text}")

                actualizar_guardados(it.text.toString())
            }
        }


    }

    private fun setear_chips_filtrado_noticias(categorias: Set<String>) {
        println("la lista chipsssss es $categorias")
        val chipGroup = binding.chipGroupFiltradoNoticia
        chipGroup.removeAllViews()
        chipGroup.isSingleSelection = true

        for (categoria in categorias) {
            val chip = Chip(chipGroup.context).apply {
                text = categoria
                isCheckable = true
                isClickable = true
                id = View.generateViewId() // Importante para identificar el chip seleccionado
            }
            chipGroup.addView(chip)
        }

        chipGroup.setOnCheckedChangeListener { group, checkedId ->
            val selectedChip: Chip? = group.findViewById(checkedId)
            selectedChip?.let {
                println("Categoría seleccionada: ${it.text}")


            }
        }
    }

    private fun inicarlizarRecicle(listaTrabajos: MutableList<dataClassTrabajosd>) {
        val recicle = binding.recicleEncontrados
        val layoutManager = GridLayoutManager(this, 2)
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