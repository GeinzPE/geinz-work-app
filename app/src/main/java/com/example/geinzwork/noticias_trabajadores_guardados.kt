package com.example.geinzwork

import android.content.Intent
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
import androidx.appcompat.app.AlertDialog
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
import com.geinzz.geinzwork.constantesGeneral.constantesTrabajadoresTiendasInicioFragmet
import com.geinzz.geinzwork.constantesGeneral.constantesTrabajadoresTiendasInicioFragmet.verificar_dialog_seguir_guardar_trabajador
import com.geinzz.geinzwork.databinding.ActivityNoticiasTrabajadoresGuardadosBinding
import com.geinzz.geinzwork.databinding.BottoSheetGuardadoNoticiasBinding
import com.geinzz.geinzwork.databinding.BottomSheettGuardaTrabajadorBinding
import com.geinzz.geinzwork.dataclass.dataClassTrabajosd
import com.geinzz.geinzwork.dataclass.dataclassVerGuardados
import com.geinzz.geinzwork.vistaTrabajador.ver_detalles_Promociones
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlin.math.abs

class noticias_trabajadores_guardados : AppCompatActivity() {
    private val listaTrabajo = mutableListOf<dataClassTrabajosd>().toMutableList()
    private val lista_categoria_noticias = mutableSetOf<String>()
    private val listaCategoriasChips = mutableSetOf<String>()
    private val listaprincipal = mutableListOf<dataclassVerGuardados>()
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
            if (binding.guardadoTrabajadores.text.toString().toInt() == 0) {
                Toast.makeText(
                    this,
                    "No tiene ningun trabajador marcado como guardado",
                    Toast.LENGTH_SHORT
                ).show()

            } else {
                dialog = BottomSheetDialog(this)
                bottomSheet_noticias_trabajadores("trabajadores")
                dialog.show()
            }

        }
        binding.listenerNoticiasGuardadas.setOnClickListener {
            if (binding.guardadoNoticias.text.toString().toInt() == 0) {
                Toast.makeText(this, "No tiene ninguna noticia guardada", Toast.LENGTH_SHORT).show()
            } else {
                dialog = BottomSheetDialog(this)
                bottomSheet_noticias_trabajadores("noticias")

                dialog.show()
            }

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

    //funciones generales

    //noticias
    private fun cargarNoticiasGuardadas(
        bottomSheet: BottoSheetGuardadoNoticiasBinding,
        categoriaFiltrar: String? = null
    ) {
        listaprincipal.clear()
        bottomSheet.scrollGeneral.isVisible = false
        bottomSheet.linealCargando.isVisible = true
        bottomSheet.encontrados.isVisible = false
        val tiempoInicio = System.currentTimeMillis()

        val db2N = FirebaseFirestore.getInstance()
            .collection(Variables.trabajadores_usuariosDB)
            .document(Variables.trabajadoresDB)
            .collection(Variables.trabajadoresDB)
            .document(firebaseAuth.uid.toString())
            .collection("guardados").document("guardados").collection("noticias")

        db2N.get().addOnSuccessListener { res ->
            if (res.isEmpty) {
                bottomSheet.linealCargando.isVisible = false
                dialog.dismiss()
                Toast.makeText(this, "No tienes noticias guardadas", Toast.LENGTH_SHORT).show()
                binding.datosNoticias.isVisible = false
                binding.cargandoContador2.isVisible = true

                obtener_cantidad_guardados(
                    "noticias",
                    binding.guardadoNoticias,
                    binding.cargandoContador2,
                    binding.datosNoticias
                )
                return@addOnSuccessListener
            }

            lista_categoria_noticias.clear()
            for (document in res) {
                val data = document.data
                val id = data?.get(document.id) as? String ?: ""
                val categorias = id.split(",").map { it.trim() }.filter { it.isNotEmpty() }
                lista_categoria_noticias.addAll(categorias)

                if (data.isNotEmpty()) {
                    val pendingTasks = data.size
                    var completedTasks = 0

                    for ((key, _) in data) {
                        val db2 = FirebaseFirestore.getInstance().collection(Variables.noticias)
                            .document(key)

                        db2.get().addOnSuccessListener { datos ->
                            if (datos.exists()) {
                                val dataNoticia = datos.data
                                val categoriaNoticia =
                                    dataNoticia?.get("Categoria") as? String ?: ""
                                val id = dataNoticia?.get(Variables.id) as? String ?: ""
                                val titulo = dataNoticia?.get(Variables.titulo) as? String ?: ""
                                val imagen = dataNoticia?.get(Variables.imagenUrl) as? String ?: ""
                                val fechaMap =
                                    dataNoticia?.get(Variables.fechas) as? Map<String, Any>
                                val fechaActivacion =
                                    fechaMap?.get(Variables.fecha_activacion) as? String ?: ""

                                val anuncio = dataclassVerGuardados(
                                    imagen,
                                    titulo,
                                    fechaActivacion,
                                    id
                                )

                                if (categoriaFiltrar == null || categoriaFiltrar == "Todos" || categoriaNoticia == categoriaFiltrar) {
                                    listaprincipal.add(anuncio)
                                }
                            }

                            completedTasks++
                            if (completedTasks == pendingTasks) {
                                val tiempoFin = System.currentTimeMillis()
                                val tiempoTotal = tiempoFin - tiempoInicio

                                handler.postDelayed({
                                    val transition = android.transition.AutoTransition().apply {
                                        duration = 100
                                    }

                                    android.transition.TransitionManager.beginDelayedTransition(
                                        binding.root as ViewGroup,
                                        transition
                                    )

                                    if (listaprincipal.isNotEmpty()) {
                                        inicializarRecile_noticias(listaprincipal, bottomSheet)
                                        bottomSheet.scrollGeneral.isVisible = true
                                        bottomSheet.encontrados.isVisible = true
                                        bottomSheet.linealFiltradoNoticias.isVisible = true
                                    } else {
                                        dialog.dismiss()
                                        Toast.makeText(
                                            this,
                                            "No tienes noticias guardadas",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }

                                    bottomSheet.linealCargando.isVisible = false
                                }, tiempoTotal)
                            }
                        }
                    }
                }
            }

            // Solo generar chips si no hay filtro (es decir, solo al cargar por primera vez)
            if (categoriaFiltrar == null || categoriaFiltrar == "Todos") {
                setear_chips_general_trabajadores_noticias(
                    lista_categoria_noticias,
                    bottomSheet.chipGroupFiltradoNoticia,
                    "noticias",
                    bottomSheet
                )
            }
        }.addOnFailureListener {
            Log.e("FirebaseNoticias", "Error al obtener noticias", it)
        }
    }


    private fun inicializarRecile_noticias(
        listaAnuncios: MutableList<dataclassVerGuardados>,
        bottomSheet: BottoSheetGuardadoNoticiasBinding
    ) {
        val recicle = bottomSheet.encontrados
        recicle.layoutManager = LinearLayoutManager(this)
        recicle.adapter = adapterguardados(listaAnuncios, { listener ->
            var vista = Intent(this, ver_detalles_Promociones::class.java).apply {
                putExtra(Variables.idAnuncio, listener.idNoticia)
                putExtra(Variables.entrada, Variables.tipoNoticia)
            }
            startActivity(vista)

        }, { longlistener ->
            val dialog = AlertDialog.Builder(this)
                .setTitle("Eliminar noticia")
                .setMessage("¿Estás seguro de que quieres eliminar esta noticia guardada?")
                .setPositiveButton("Sí") { _, _ ->
                    eliminar_guardados(longlistener.idNoticia.toString(), "noticias")
                    cargarNoticiasGuardadas(bottomSheet)
                    Log.d("pasamos_id_noticias", "pasamos id ${longlistener.idNoticia}")


                }
                .setNegativeButton("Cancelar", null)
                .create()

            dialog.show()

        })
    }

    private fun bottomSheet_noticias_trabajadores(tipo: String) {
        when (tipo) {
            "noticias" -> {
                val bottomSheet =
                    BottoSheetGuardadoNoticiasBinding.inflate(LayoutInflater.from(this))
                val view = bottomSheet.root
                cargarNoticiasGuardadas(bottomSheet)
                dialog.setContentView(view)
                dialog.show()
            }

            "trabajadores" -> {
                val bottomSheet =
                    BottomSheettGuardaTrabajadorBinding.inflate(LayoutInflater.from(this))
                val view = bottomSheet.root
                obtener_trabajadores_guardados(bottomSheet)
                bottomSheet.encontrados.isNestedScrollingEnabled = false
                bottomSheet.linealGeneralCarga.isVisible = false
                dialog.setContentView(view)
                dialog.show()
            }
        }

    }

    //trabajadores
    private fun obtener_trabajadores_guardados(
        bottomSheet: BottomSheettGuardaTrabajadorBinding,
        onEmpty: (() -> Unit)? = null
    ) {
        Toast.makeText(this, "llegamos a la funcion de obtner", Toast.LENGTH_SHORT).show()
        bottomSheet.encontrados.isVisible = false
        bottomSheet.linealGeneralCarga.isVisible = false
        bottomSheet.linealCargando.isVisible = true
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
            if (res.isEmpty) {
                onEmpty?.invoke()
                bottomSheet.linealCargando.isVisible = false
                return@addOnSuccessListener
            }
            listaTrabajo.clear()
            cargarTrabajadoresGuardados(null, bottomSheet, onEmpty)

        }
    }

    private fun cargarTrabajadoresGuardados(
        filtrado: String? = null,
        bottomSheet: BottomSheettGuardaTrabajadorBinding,
        onEmpty: (() -> Unit)? = null
    ) {
        bottomSheet.linealCargando.isVisible = true
        bottomSheet.encontrados.isVisible = false
        bottomSheet.linealGeneralCarga.isVisible = false

        Toast.makeText(this, "obtnemos las categorias de filtro $filtrado", Toast.LENGTH_SHORT)
            .show()
        val db = FirebaseFirestore.getInstance()
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return

        val guardadosRef = db.collection(Variables.trabajadores_usuariosDB)
            .document(Variables.trabajadoresDB)
            .collection(Variables.trabajadoresDB)
            .document(userId)
            .collection("guardados")
            .document("guardados")
            .collection("trabajadores")

        guardadosRef.get().addOnSuccessListener { snapshot ->
            val totalTrabajadores = snapshot.size()
            if (totalTrabajadores == 0) {
                bottomSheet.linealCargando.isVisible = false
                onEmpty?.invoke()
                Toast.makeText(
                    bottomSheet.root.context,
                    "No tienes trabajadores guardados",
                    Toast.LENGTH_SHORT
                ).show()
                return@addOnSuccessListener
            }

            var cargados = 0
            val handler = android.os.Handler(android.os.Looper.getMainLooper())

            for (doc in snapshot.documents) {
                val idTrabajador = doc.getString("id_trabajador") ?: ""

                val tiempoInicio = System.currentTimeMillis()
                val trabajadorDocRef = db.collection(Variables.trabajadores_usuariosDB)
                    .document(Variables.trabajadoresDB)
                    .collection(Variables.trabajadoresDB)
                    .document(idTrabajador)

                trabajadorDocRef.get().addOnSuccessListener { res ->
                    val tiempoFin = System.currentTimeMillis()
                    val duracion = tiempoFin - tiempoInicio
                    android.util.Log.d(
                        "TIEMPO_FIREBASE",
                        "⏱ Tiempo para $idTrabajador: ${duracion}ms"
                    )

                    if (res.exists()) {
                        val userData = res.data
                        val categoriaTrabajoStr =
                            userData?.get(Variables.categoriaTrabajo) as? String ?: ""

                        if (filtrado == null) {
                            Toast.makeText(this, "entrasmoa a la condicionam", Toast.LENGTH_SHORT)
                                .show()
                            val estrellas = userData?.get("estrellas") as? String ?: "0"
                            val nombre = userData?.get(Variables.nombre) as? String ?: ""
                            val apellido = userData?.get(Variables.apellido) as? String ?: ""
                            val caracteristica1 =
                                userData?.get(Variables.caracteristica1) as? String ?: ""
                            val caracteristica2 =
                                userData?.get(Variables.caracteristica2) as? String ?: ""
                            val caracteristica3 =
                                userData?.get(Variables.caracteristica3) as? String ?: ""
                            val codigoPais = userData?.get(Variables.codigo_pais) as? String ?: ""
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
                            val tipoTrabajo = userData?.get(Variables.tipoTrabajo) as? String ?: ""
                            val activo = userData?.get(Variables.activado) as? String ?: ""
                            val edadActual = userData?.get(Variables.EdadActual) as? String ?: ""
                            val verificados =
                                userData?.get(Variables.verificado) as? Boolean ?: false

                            val usuario = dataClassTrabajosd(
                                id, apellido, caracteristica1, caracteristica2, caracteristica3,
                                categoriaTrabajoStr, fechaNac, genero, horario1, horario2,
                                nacionalidad, nombre, estrellas, tipoTrabajo, localidad,
                                codigoPais, numero, img, activo, edadActual, verificados
                            )
                            listaTrabajo.add(usuario)

                            val categorias = categoriaTrabajoStr.split(",").map { it.trim() }
                                .filter { it.isNotEmpty() }
                            listaCategoriasChips.addAll(categorias)
                        }else if( filtrado!=null && filtrado== categoriaTrabajoStr){

                            val estrellas = userData?.get("estrellas") as? String ?: "0"
                            val nombre = userData?.get(Variables.nombre) as? String ?: ""
                            val apellido = userData?.get(Variables.apellido) as? String ?: ""
                            val caracteristica1 =
                                userData?.get(Variables.caracteristica1) as? String ?: ""
                            val caracteristica2 =
                                userData?.get(Variables.caracteristica2) as? String ?: ""
                            val caracteristica3 =
                                userData?.get(Variables.caracteristica3) as? String ?: ""
                            val codigoPais = userData?.get(Variables.codigo_pais) as? String ?: ""
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
                            val tipoTrabajo = userData?.get(Variables.tipoTrabajo) as? String ?: ""
                            val activo = userData?.get(Variables.activado) as? String ?: ""
                            val edadActual = userData?.get(Variables.EdadActual) as? String ?: ""
                            val verificados =
                                userData?.get(Variables.verificado) as? Boolean ?: false

                            val usuario = dataClassTrabajosd(
                                id, apellido, caracteristica1, caracteristica2, caracteristica3,
                                categoriaTrabajoStr, fechaNac, genero, horario1, horario2,
                                nacionalidad, nombre, estrellas, tipoTrabajo, localidad,
                                codigoPais, numero, img, activo, edadActual, verificados
                            )
                            listaTrabajo.add(usuario)

                        }
                    }

                    cargados++
                    if (cargados == totalTrabajadores) {
                        handler.post {
                            if (listaTrabajo.isNotEmpty()) {
                                inicarlizarRecicle(listaTrabajo, bottomSheet)
                                bottomSheet.encontrados.isVisible = true
                                bottomSheet.linealFitlradoTrabajadores.isVisible = true
                                bottomSheet.linealGeneralCarga.isVisible = true
                                bottomSheet.linealCargando.isVisible = false

                                // Solo generar chips si no hay filtro
                                if (filtrado == null) {
                                    setear_chips_general_trabajadores_noticias(
                                        listaCategoriasChips,
                                        bottomSheet.chipGroupFiltradoTrabajadore,
                                        "trabajadores",
                                        null,
                                        bottomSheet
                                    )
                                }

                            } else {
                                onEmpty?.invoke()
                                Toast.makeText(
                                    bottomSheet.root.context,
                                    "No tienes trabajadores guardados",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }

                            bottomSheet.linealCargando.isVisible = false
                        }
                    }
                }.addOnFailureListener {
                    cargados++
                    if (cargados == totalTrabajadores) {
                        handler.post {
                            if (listaTrabajo.isEmpty()) {
                                onEmpty?.invoke()
                                Toast.makeText(
                                    bottomSheet.root.context,
                                    "No tienes trabajadores guardados",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                            bottomSheet.linealCargando.isVisible = false
                        }
                    } else {
                        Toast.makeText(
                            bottomSheet.root.context,
                            "Error al obtener trabajador",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        }.addOnFailureListener {
            bottomSheet.linealCargando.isVisible = false
            Toast.makeText(
                bottomSheet.root.context,
                "Error al obtener trabajadores guardados",
                Toast.LENGTH_SHORT
            ).show()
        }
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
            ) { longlistener ->
                val dialog = AlertDialog.Builder(this)
                    .setTitle("Eliminar trabajador")
                    .setMessage("¿Estás seguro de que quieres eliminar este trabajador guardada?")
                    .setPositiveButton("Sí") { _, _ ->
                        eliminar_guardados(longlistener.id.toString(), "trabajadores")
                        obtener_trabajadores_guardados(bottomSheet) {
                            dialog.dismiss() // 👈 se cierra si no quedan más trabajadores
                        }
                        obtener_cantidad_guardados(
                            "trabajadores",
                            binding.guardadoTrabajadores,
                            binding.cargandoContador, binding.datosTrabajadores
                        )
                    }

                    .setNegativeButton("Cancelar", null)
                    .create()

                dialog.show()

            }

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


    //funciones dobles
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
            texview_seteado.text = "${cantidad}"
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

    private fun setear_chips_general_trabajadores_noticias(
        categorias: Set<String>,
        chip_group: ChipGroup,
        tipo: String,
        bottomSheet_noticias: BottoSheetGuardadoNoticiasBinding? = null,
        bottomSheet_trabajadores: BottomSheettGuardaTrabajadorBinding? = null
    ) {
        val chipGroup = chip_group
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

        when (tipo) {
            "noticias" -> {
                if (bottomSheet_noticias != null) {
                    chipGroup.setOnCheckedChangeListener { group, checkedId ->
                        val selectedChip: Chip? = group.findViewById(checkedId)
                        selectedChip?.let {
                            if (it.text.toString() == "Todos") {
                                cargarNoticiasGuardadas(bottomSheet_noticias)
                            } else {
                                cargarNoticiasGuardadas(
                                    bottomSheet_noticias, it.text.toString()
                                )
                            }
                        }
                    }
                }

            }

            "trabajadores" -> {
                if (bottomSheet_trabajadores != null) {
                    chipGroup.setOnCheckedChangeListener { group, checkedId ->
                        val selectedChip: Chip? = group.findViewById(checkedId)
                        selectedChip?.let {
                            it.isChecked = true // Forzar visual

                            group.post {
                                if (it.text.toString() == "Todos") {
                                    obtener_trabajadores_guardados(bottomSheet_trabajadores)
                                } else {
                                    listaTrabajo.clear()
                                    cargarTrabajadoresGuardados(
                                        it.text.toString(),
                                        bottomSheet_trabajadores
                                    )
                                }
                            }
                        }
                    }

                }
            }
        }


    }

    private fun eliminar_guardados(selecionado: String, collecion: String) {
        val db2N = FirebaseFirestore.getInstance()
            .collection(Variables.trabajadores_usuariosDB)
            .document(Variables.trabajadoresDB)
            .collection(Variables.trabajadoresDB)
            .document(firebaseAuth.uid.toString())
            .collection("guardados").document("guardados").collection(collecion)
            .document(selecionado)
        db2N.delete().addOnSuccessListener { res ->
            Toast.makeText(this, "Noticia eliminada correctamente", Toast.LENGTH_SHORT).show()
            Toast.makeText(this, "tamano de lista ${listaprincipal.size}", Toast.LENGTH_SHORT)
                .show()
        }.addOnFailureListener { e ->
            Toast.makeText(this, "error al eliminarlo de guardados", Toast.LENGTH_SHORT).show()

        }
    }

}