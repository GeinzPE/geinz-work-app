package com.example.geinzwork

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.widget.ArrayAdapter
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.viewpager2.widget.ViewPager2
import com.example.geinzwork.constantesGeneral.Variables
import com.geinzz.geinzwork.R
import com.geinzz.geinzwork.adapterViewholder.adapter
import com.geinzz.geinzwork.adapterViewholder.adapterguardados
import com.geinzz.geinzwork.constantesGeneral.constantesNoticias.firebaseAuth
import com.geinzz.geinzwork.databinding.ActivityNoticiasTrabajadoresGuardadosBinding
import com.geinzz.geinzwork.databinding.BottoSheetGuardadoNoticiasBinding
import com.geinzz.geinzwork.databinding.BottomSheettGuardaTrabajadorBinding
import com.geinzz.geinzwork.dataclass.dataClassTrabajosd
import com.geinzz.geinzwork.dataclass.dataclassVerGuardados
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.chip.Chip
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlin.math.abs

class noticias_trabajadores_guardados : AppCompatActivity() {
    private val listaTrabajo = mutableListOf<dataClassTrabajosd>().toMutableList()
    private val lista_categoria_noticias = mutableSetOf<String>()
    private val listaCategoriasChips = mutableSetOf<String>()
    private var totalTrabajadores = 0
    private var cargados = 0
    private val handler = Handler(Looper.getMainLooper())
    private lateinit var binding: ActivityNoticiasTrabajadoresGuardadosBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var dialog: BottomSheetDialog


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

        binding.listenerTrabajadores.setOnClickListener {
            dialog = BottomSheetDialog(this)
            bottomSheet()
            dialog.show()
        }
        binding.listenerNoticiasGuardadas.setOnClickListener {
            dialog = BottomSheetDialog(this)
            bottomSheet_noticias()
            dialog.show()
        }
        obtener_cantidad_guardados(
            "trabajadores",
            binding.guardadoTrabajadores,
            binding.cargandoContador, binding.datosTrabajadores
        )

        obtener_cantidad_guardados(
            "noticias",
            binding.guardadoNoticias,
            binding.cargandoContador2, binding.datosNoticias
        )

    }

    private fun obtener_cantidad_guardados(
        tipo: String,
        texview_seteado: TextView,
        progressBar: ProgressBar,
        Lineal: LinearLayout
    ) {
        val startTime = System.currentTimeMillis()

        val db = FirebaseFirestore.getInstance()
            .collection("Trabajadores_Usuarios_Drivers")
            .document("trabajadores").collection("trabajadores")
            .document(firebaseAuth.uid.toString()).collection("guardados")
            .document("guardados").collection(tipo)

        db.get().addOnSuccessListener { res ->
            val endTime = System.currentTimeMillis()
            val duration = endTime - startTime
            Log.d("FirestoreTiempo", "Tiempo de respuesta Firestore ($tipo): $duration ms")
            val cantidad = res.size()
            texview_seteado.text = "${cantidad} Guardados"
            Handler(Looper.getMainLooper()).postDelayed({
                progressBar.isVisible = false
                Lineal.isVisible = true
            }, duration)

        }.addOnFailureListener { e ->
            val endTime = System.currentTimeMillis()
            val duration = endTime - startTime
            Log.e("FirestoreTiempo", "Error al obtener datos ($tipo) en $duration ms", e)
        }
    }


    private fun bottomSheet_noticias() {
        val bottomSheet = BottoSheetGuardadoNoticiasBinding.inflate(LayoutInflater.from(this))
        val view = bottomSheet.root
        obtenernoticias_guardados(bottomSheet)
        dialog.setContentView(view)
        dialog.show()
    }

    private fun obtenernoticias_guardados(bottomSheet: BottoSheetGuardadoNoticiasBinding) {

        bottomSheet.linealCargando.isVisible = true
        bottomSheet.encontrados.isVisible = false
        val tiempoInicio = System.currentTimeMillis()

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

                setear_chips_filtrado_noticias(lista_categoria_noticias, bottomSheet)
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
                                        inicializarRecile_noticias(listaprincipal, bottomSheet)
                                    }
                                    val transition = android.transition.AutoTransition().apply {
                                        duration = 100
                                    }
                                    android.transition.TransitionManager.beginDelayedTransition(
                                        binding.root as ViewGroup,
                                        transition
                                    )
                                    bottomSheet.scrollGeneral.isVisible = true
                                    bottomSheet.encontrados.isVisible = true
                                    bottomSheet.linealCargando.isVisible = false
                                    bottomSheet.linealFiltradoNoticias.isVisible = true
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

    private fun setear_chips_filtrado_noticias(
        categorias: Set<String>,
        bottomSheet: BottoSheetGuardadoNoticiasBinding
    ) {
        val chipGroup = bottomSheet.chipGroupFiltradoNoticia
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

    private fun inicializarRecile_noticias(
        listaAnuncios: MutableList<dataclassVerGuardados>,
        bottomSheet: BottoSheetGuardadoNoticiasBinding
    ) {
        val recicle = bottomSheet.encontrados
        recicle.layoutManager = LinearLayoutManager(this)
        recicle.adapter = adapterguardados(listaAnuncios)
    }


    private fun bottomSheet() {
        val bottomSheet = BottomSheettGuardaTrabajadorBinding.inflate(LayoutInflater.from(this))
        val view = bottomSheet.root
        obtener_trabajadores_guardados(bottomSheet)
        bottomSheet.encontrados.isNestedScrollingEnabled = false
        bottomSheet.linealGeneralCarga.isVisible = false
        dialog.setContentView(view)
        dialog.show()
    }

    private fun obtener_trabajadores_guardados(bottomSheet: BottomSheettGuardaTrabajadorBinding) {
        Toast.makeText(this, "llegamos a la funcion de obtner", Toast.LENGTH_SHORT).show()
        bottomSheet.encontrados.isVisible = false
        bottomSheet.linealGeneralCarga.isVisible = false
        bottomSheet.linealCargando.isVisible=true
        val db2 = FirebaseFirestore.getInstance()
            .collection(Variables.trabajadores_usuariosDB)
            .document(Variables.trabajadoresDB)
            .collection(Variables.trabajadoresDB)
            .document(firebaseAuth.uid.toString())
            .collection("guardados").document("guardados").collection("trabajadores")

        db2.get().addOnSuccessListener { res ->
            totalTrabajadores = res.size()
            cargados = 0
            listaCategoriasChips.clear()
            listaTrabajo.clear()
            for (datos in res) {
                val data = datos.data
                val id = data?.get("id_trabajador") as? String ?: ""
                guardados(id, bottomSheet)
            }
        }
    }

    private fun guardados(idTrabajador: String, bottomSheet: BottomSheettGuardaTrabajadorBinding) {
        bottomSheet.encontrados.isVisible = false
        bottomSheet.linealGeneralCarga.isVisible = false
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

                        bottomSheet.encontrados.isVisible = true
                        bottomSheet.linealCargando.isVisible = false
                        bottomSheet.linealFitlradoTrabajadores.isVisible =
                            true
                        bottomSheet.linealGeneralCarga.isVisible = true
                    }, duracion)

                    setear_chisp_filtrado(listaCategoriasChips, bottomSheet)
                    inicarlizarRecicle(listaTrabajo, bottomSheet)
                }
            }
    }

    private fun setear_chisp_filtrado(
        categorias: Set<String>,
        bottomSheet: BottomSheettGuardaTrabajadorBinding
    ) {
        println("la lista chipsssss es $categorias")
        val chipGroup = bottomSheet.chipGroupFiltradoTrabajadore
        chipGroup.removeAllViews()
        chipGroup.isSingleSelection = true


        val chipTodos = Chip(chipGroup.context).apply {
            text = "Todos"
            isCheckable = true
            isClickable = true
            isChecked = true
            id = View.generateViewId()
        }
        chipGroup.addView(chipTodos)

        for (categoria in categorias) {
            val chip = Chip(chipGroup.context).apply {
                text = categoria
                isCheckable = true
                isClickable = true
                id = View.generateViewId()
            }
            chipGroup.addView(chip)
        }

        chipGroup.setOnCheckedChangeListener { group, checkedId ->
            val selectedChip: Chip? = group.findViewById(checkedId)
            selectedChip?.let {
                println("Categoría seleccionada: ${it.text}")
                if (it.text.toString() == "Todos") {

                    obtener_trabajadores_guardados(bottomSheet)

                } else {
                    actualizar_guardados(it.text.toString(), bottomSheet)
                }
            }
        }
    }

    private fun actualizar_guardados(
        filtrado: String,
        bottomSheet: BottomSheettGuardaTrabajadorBinding
    ) {

        bottomSheet.linealCargando.isVisible = true
        bottomSheet.encontrados.isVisible = false
        bottomSheet.linealGeneralCarga.isVisible = false
        val db2 = FirebaseFirestore.getInstance()
            .collection(Variables.trabajadores_usuariosDB)
            .document(Variables.trabajadoresDB)
            .collection(Variables.trabajadoresDB)
            .document(firebaseAuth.uid.toString())
            .collection("guardados").document("guardados").collection("trabajadores")

        db2.get().addOnSuccessListener { res ->
            totalTrabajadores = res.size()
            cargados = 0
            listaCategoriasChips.clear()
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
                                bottomSheet.encontrados.isVisible = true
                                bottomSheet.linealCargando.isVisible = false
                                bottomSheet.linealFitlradoTrabajadores.isVisible =
                                    true
                                bottomSheet.linealGeneralCarga.isVisible =
                                    true

                            }, duracion)

                            inicarlizarRecicle(listaTrabajo, bottomSheet)
                        }
                    }
            }
        }
        Log.d("entradmos", "entramos a la fun de actualzuiar")
        listaTrabajo.clear()

    }

    private fun inicarlizarRecicle(
        listaTrabajos: MutableList<dataClassTrabajosd>,
        bottomSheet: BottomSheettGuardaTrabajadorBinding
    ) {
        val recicle = bottomSheet.encontrados
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