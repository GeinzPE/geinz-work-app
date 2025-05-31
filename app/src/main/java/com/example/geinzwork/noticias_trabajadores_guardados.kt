package com.example.geinzwork

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.GridLayoutManager
import com.example.geinzwork.constantesGeneral.Variables
import com.geinzz.geinzwork.R
import com.geinzz.geinzwork.adapterViewholder.adapter
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
        obtener_trabajadores_guardados()
    }

    private fun obtenernoticias_guardados(){
        val listaprincipal = mutableListOf<dataclassVerGuardados>()

        val db2N = FirebaseFirestore.getInstance()
            .collection(Variables.trabajadores_usuariosDB)
            .document(Variables.trabajadoresDB)
            .collection(Variables.trabajadoresDB)
            .document(firebaseAuth.uid.toString())
            .collection("guardados").document("guardados").collection("noticias")


        db2N.get().addOnSuccessListener { res ->
            for (datos in res) {
                val data = datos.data
                if (data.isNotEmpty()) {
                    for ((key, value) in data) {
                        val pendingTasks = data.size
                        var completedTasks = 0
                        val db2 = FirebaseFirestore.getInstance().collection(Variables.noticias)
                            .document(key)
                        db2.get()
                            .addOnSuccessListener { datos ->
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
                                    if (listaprincipal.isEmpty()) {
//                                        binding.relativeNoEncontrado.isVisible = true
//                                        binding.recicelGuardados.isVisible = false
//                                        binding.texto.isVisible = false
                                    } else {
//                                        binding.relativeNoEncontrado.isVisible = false
//                                        binding.recicelGuardados.isVisible = true
//                                        binding.texto.isVisible = true
//                                        inicializarRecile(listaprincipal)
                                    }
//                                    binding.loading.isVisible = false
                                }
                            }
                    }

                }
            }
        }
            .addOnFailureListener {

            }
    }

    private var totalTrabajadores = 0
    private var cargados = 0

    private fun obtener_trabajadores_guardados() {
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
        listaTrabajo.clear()
        val userCollections =
            FirebaseFirestore.getInstance().collection(Variables.trabajadores_usuariosDB)
                .document(Variables.trabajadoresDB).collection(Variables.trabajadoresDB)
                .document(idTrabajador)

        userCollections.get()
            .addOnSuccessListener { res ->
                if (res.exists()) {
                    val userData = res.data

                    val estrellas = userData?.get("estrellas") as? String ?: "0"
                    val nombre = userData?.get(Variables.nombre) as? String ?: ""
                    val apellido = userData?.get(Variables.apellido) as? String ?: ""
                    val caracteristica1 = userData?.get(Variables.caracteristica1) as? String ?: ""
                    val caracteristica2 = userData?.get(Variables.caracteristica2) as? String ?: ""
                    val caracteristica3 = userData?.get(Variables.caracteristica3) as? String ?: ""
                    val categoriaTrabajoStr = userData?.get(Variables.categoriaTrabajo) as? String ?: ""
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

                    // Dividir el String de categorías por comas y limpiar espacios

                    val usuario = dataClassTrabajosd(
                        id,
                        apellido,
                        caracteristica1,
                        caracteristica2,
                        caracteristica3,
                        categoriaTrabajoStr, // solo el string completo aquí
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
                        img,
                        activo,
                        edadActual,
                        verificados
                    )

                    listaTrabajo.add(usuario)

                    val categorias = categoriaTrabajoStr.split(",")
                        .map { it.trim() }
                        .filter { it.isNotEmpty() }

// Agregar categorías únicas al conjunto
                    listaCategoriasChips.addAll(categorias)

                    println("la lista agregada es $listaTrabajo")
                }

                cargados++
                if (cargados == totalTrabajadores) {
                    setear_chisp_filtrado(listaCategoriasChips)
                    inicarlizarRecicle(listaTrabajo)
                }

            }
    }


    private fun setear_chisp_filtrado(categorias: Set<String>) {
        println("la lista chipsssss es $categorias")
        val chipGroup = binding.chipGroupFiltradoTrabajadoreNoticias
        chipGroup.removeAllViews()
        chipGroup.isSingleSelection = true
        for (categoria in categorias) {
            val chip = Chip(chipGroup.context).apply {
                text = categoria
                isCheckable = true
                isClickable = true
            }
            chipGroup.addView(chip)
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