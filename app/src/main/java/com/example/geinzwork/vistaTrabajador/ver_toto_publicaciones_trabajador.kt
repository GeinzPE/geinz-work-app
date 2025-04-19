package com.example.geinzwork.vistaTrabajador

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.geinzwork.adapterViewholder.adapter_trabajos_realizados_trabajador
import com.example.geinzwork.dataclass.dataclass_adapter_promociones
import com.geinzz.geinzwork.R
import com.geinzz.geinzwork.databinding.ActivityVerTotoPublicacionesTrabajadorBinding
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.firebase.firestore.FirebaseFirestore

class ver_toto_publicaciones_trabajador : AppCompatActivity() {
    private lateinit var binding: ActivityVerTotoPublicacionesTrabajadorBinding
    private var lista_dataclass_trabajos = mutableListOf<dataclass_adapter_promociones>()
    private val hashtagsGenerales = mutableListOf<String>()
    private val hashtagsCategoria = mutableListOf<String>()
    private val hashtagsSubcategoria = mutableListOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityVerTotoPublicacionesTrabajadorBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        obtnerTodo_publicaciones_trabajadaor()
        obtener_generales_publicaciones()

    }

    private fun obtnerTodo_publicaciones_trabajadaor() {
        val tiempoInicio = System.currentTimeMillis()

        val db = FirebaseFirestore.getInstance().collection("hastags_generales")
            .document("hashtags_publicaciones")

        val chipGroup = findViewById<ChipGroup>(R.id.chipGrup_hastagsP)

        // Mostrar apartado de carga
        binding.cargandoHastagsPrincipales.isVisible = true
        binding.chipGrupHastagsP.isVisible = false


        db.get().addOnSuccessListener { documentSnapshot ->
            val tiempoFin = System.currentTimeMillis()
            val tiempoTotal = tiempoFin - tiempoInicio
            Toast.makeText(
                this,
                "Hashtags generales cargados en $tiempoTotal ms",
                Toast.LENGTH_SHORT
            ).show()

            if (documentSnapshot.exists()) {
                val hashtags = documentSnapshot.get("hashtags_publicaciones_array") as? List<String>
                if (hashtags != null) {
                    chipGroup.removeAllViews()
                    for (hashtag in hashtags) {
                        val chip = Chip(this).apply {
                            text = hashtag
                            isCheckable = true
                            isClickable = true
                            setOnClickListener {
                                // Limpiar lista antes de volver a llenar
                                hashtagsGenerales.clear()

                                for (i in 0 until chipGroup.childCount) {
                                    val view = chipGroup.getChildAt(i)
                                    if (view is Chip && view.isChecked) {
                                        hashtagsGenerales.add(view.text.toString())
                                    }
                                }

                                obtenerTrabajosFiltrados(hashtagsGenerales)
                                // Mostrar lista en log
                                Log.d("HashtagsSeleccionados", hashtagsGenerales.toString())

                                if (hayAlgunChipSeleccionado(chipGroup)) {
                                    // Si hay al menos un hashtag seleccionado:
                                    binding.linealCategoriasTrabajosPublicados.isVisible = true
                                    binding.linealCategoriasTrabajos.isVisible = false
                                    setear_chips_categorias(hashtagsGenerales)
                                } else {
                                    // Si NO hay ningún chip seleccionado:
                                    binding.linealCategoriasTrabajosPublicados.isVisible = false
                                    binding.linealCategoriasTrabajos.isVisible = false
                                    obtener_generales_publicaciones()
                                }
                            }

                        }
                        chipGroup.addView(chip)
                    }

                    // Ocultar apartado de carga y mostrar chips
                    binding.cargandoHastagsPrincipales.isVisible = false
                    binding.chipGrupHastagsP.isVisible = true
                } else {
                    Log.e("Firestore", "El campo no es un array o está vacío.")
                }
            } else {
                Log.e("Firestore", "El documento no existe.")
            }
        }.addOnFailureListener { exception ->
            Log.e("Firestore", "Error al obtener documento: ${exception.message}")
        }
    }

    private fun setear_chips_categorias(hashtagsSeleccionados: MutableList<String>) {
        val db = FirebaseFirestore.getInstance()
        val subcategoriasRef = db.collection("subcategoriasTrabajos")
        val chipGroup = findViewById<ChipGroup>(R.id.idGrupoCategoriaTrabajos)

        chipGroup.removeAllViews() // Limpia chips anteriores

        // Mostrar indicador de carga
        binding.cargandoCategoriasTrabajadores.isVisible = true
        binding.linealCategoriasTrabajos.isVisible = false
        binding.linealCategoriasTrabajosPublicados.isVisible = false

        val tiempoInicio = System.currentTimeMillis()

        subcategoriasRef.get()
            .addOnSuccessListener { querySnapshot ->
                val tiempoFin = System.currentTimeMillis()
                val tiempoTotal = tiempoFin - tiempoInicio
                Toast.makeText(this, "Categorías cargadas en $tiempoTotal ms", Toast.LENGTH_SHORT)
                    .show()

                if (!querySnapshot.isEmpty) {
                    for (document in querySnapshot.documents) {
                        val id = document.id // Nombre visible del chip

                        val chip = Chip(this).apply {
                            text = id
                            isCheckable = true
                            isClickable = true
                            setOnClickListener {
                                // Solo permitir un chip seleccionado a la vez
                                for (i in 0 until chipGroup.childCount) {
                                    val otherChip = chipGroup.getChildAt(i) as Chip
                                    if (otherChip != this) {
                                        otherChip.isChecked = false
                                    }
                                }
                                if (isChecked) hashtagsCategoria.add(text.toString())
                                obtenerTrabajosFiltrados(hashtagsGenerales, text.toString())

                                Log.d(
                                    "HashtagsSeleccionados",
                                    "Categoría seleccionada: $hashtagsCategoria - Hashtags: $hashtagsSeleccionados"
                                )

                                if (hayAlgunChipSeleccionado(chipGroup)) {
                                    binding.categoriaTrabajosPublicados.isVisible = true
                                    obtenerHastags_cada_cat(
                                        hashtagsSeleccionados,
                                        text.toString(),
                                        hashtagsCategoria,
                                        id
                                    )
                                } else {
                                    binding.categoriaTrabajosPublicados.isVisible = false
                                }
                            }
                        }

                        chipGroup.addView(chip)
                    }

                    // Ocultar indicador de carga y mostrar chips
                    binding.cargandoCategoriasTrabajadores.isVisible = false
                    binding.linealCategoriasTrabajos.isVisible = true
                } else {
                    Log.d("Firestore", "No hay documentos en la colección.")
                }
            }
            .addOnFailureListener { exception ->
                Log.e("Firestore", "Error al obtener documentos: ${exception.message}")
            }
    }

    private fun obtenerHastags_cada_cat(
        hashtagsSeleccionados: MutableList<String>, categoriaSeleccion: String,
        categoriasSeleccionadas: MutableList<String>,
        id: String
    ) {
        val db = FirebaseFirestore.getInstance()
        val subcategoriasRef = db.collection("subcategoriasTrabajos").document(id)

        val tiempoInicio = System.currentTimeMillis()

        binding.cargandoCategorias.isVisible = true
        binding.linealCategoriasTrabajosPublicados.isVisible = false

        subcategoriasRef.get().addOnSuccessListener { documentSnapshot ->
            val tiempoFin = System.currentTimeMillis()
            val tiempoTotal = tiempoFin - tiempoInicio
            Toast.makeText(this, "Hashtags cargados en $tiempoTotal ms", Toast.LENGTH_SHORT).show()

            if (documentSnapshot.exists()) {
                val hashtags = documentSnapshot.get("hashtags_array") as? List<String>
                if (hashtags != null) {
                    val chipGroup = findViewById<ChipGroup>(R.id.categoriaTrabajos_publicados)
                    chipGroup.removeAllViews()


                    for (hashtag in hashtags) {
                        val chip = Chip(this).apply {
                            text = hashtag
                            isCheckable = true
                            isClickable = true

                            setOnClickListener {
                                if (isChecked) {
                                    hashtagsSubcategoria.add(text.toString())
                                } else {
                                    hashtagsSubcategoria.remove(text.toString())
                                }

                                obtenerTrabajosFiltrados(
                                    hashtagsGenerales,
                                    categoriaSeleccion,
                                    hashtagsSubcategoria
                                )

                                Log.d(
                                    "HashtagsSeleccionados",
                                    "Categoría seleccionada: $categoriasSeleccionadas - Hashtags: $hashtagsSeleccionados Hashtags seleccionados para $id: $hashtagsSubcategoria"
                                )

                            }
                        }

                        chipGroup.addView(chip)
                    }
                    binding.cargandoCategorias.isVisible = false
                    binding.linealCategoriasTrabajosPublicados.isVisible = true

                } else {
                    Log.e("Firestore", "El campo no es un array o está vacío.")
                }
            } else {
                Log.e("Firestore", "El documento no existe.")
            }
        }.addOnFailureListener { exception ->
            Log.e("Firestore", "Error al obtener documento: ${exception.message}")
        }
    }


    private fun obtener_generales_publicaciones() {
        val db = FirebaseFirestore.getInstance().collection("solicitudes_servicios")
            .document("verificaciones").collection("activos")
        binding.listaPublicacionesTrabajadores.isVisible = false
        binding.cargandoContenido.isVisible = true

        db.get().addOnSuccessListener { res ->
            val trabajadores = res.documents
            if (trabajadores.isEmpty()) return@addOnSuccessListener

            var completados = 0
            val total = trabajadores.size

            for (document in trabajadores) {
                val id_trabajador = document.id
                Log.d("obtenosID_trabajoders",id_trabajador)

                val dbUsers = FirebaseFirestore.getInstance()
                    .collection("Trabajadores_Usuarios_Drivers")
                    .document("trabajadores")
                    .collection("trabajadores")
                    .document(id_trabajador)
                    .collection("publicaciones_trabajos")

                dbUsers.get().addOnSuccessListener { productos ->
                    if (!productos.isEmpty) {
                        val aleatorio = productos.documents.random()
                        val img_producto = aleatorio.get("img_url") as? String ?: ""
                        val idPublicaciones = aleatorio.get("id") as? String ?: ""
                        val titulo = aleatorio.get("titulo") as? String ?: ""
                        val contenido = aleatorio.get("contenido") as? String ?: ""
                        val fecha = aleatorio.get("fecha_rec") as? String ?: ""
                        val hora = aleatorio.get("hora_rec") as? String ?: ""

                        val lista = dataclass_adapter_promociones(
                            img_producto, null, null, null,
                            titulo,
                            contenido,
                            idPublicaciones,
                            fecha,
                            hora,id_trabajador
                        )
                        lista_dataclass_trabajos.add(lista)
                        binding.cargandoContenido.isVisible = false
                        binding.listaPublicacionesTrabajadores.isVisible = true
                        Log.d(
                            "obtnerDatos",
                            "img: $img_producto | id: $idPublicaciones | titulo: $titulo | contenido: $contenido | fecha: $fecha | hora: $hora"
                        )
                    }

                    completados++
                    if (completados == total) {
                        // Solo cuando se hayan completado todos
                        inicializarTrabajosFiltrados(id_trabajador,lista_dataclass_trabajos)
                    }

                }.addOnFailureListener { e ->
                    completados++
                    Log.e("ProductosVerificados", "Error al obtener documentos", e)

                    if (completados == total) {
                        inicializarTrabajosFiltrados(id_trabajador,lista_dataclass_trabajos)
                    }
                }
            }
        }.addOnFailureListener { e ->
            Log.e("ProductosVerificados", "Error al obtener documentos", e)
        }
    }

    private fun hayAlgunChipSeleccionado(chipGroup: ChipGroup?): Boolean {
        if (chipGroup == null || chipGroup.childCount == 0) return false

        return try {
            for (i in 0 until chipGroup.childCount) {
                val view = chipGroup.getChildAt(i)
                if (view is Chip && view.isChecked) {
                    return true
                }
            }
            false
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    private fun inicializarTrabajosFiltrados(
        idTrabajador: String,
        listaFiltrada: MutableList<dataclass_adapter_promociones>
    ) {
        val recicle = binding.listaPublicacionesTrabajadores
        recicle.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        recicle.adapter =
            adapter_trabajos_realizados_trabajador(true, listaFiltrada.toMutableList()) { item ->
                val vista = Intent(this, vista_ver_publicaciones_trabajadores::class.java).apply {
                        putExtra("id_trabajador", item.idTrabajador)
                            putExtra("id_publicacion", item.id)
                    }
                startActivity(vista)
            }


    }

    private fun obtenerTrabajosFiltrados(
        hashtagsGenerales: MutableList<String>,
        categorias_Trabajadores: String? = null,
        hastgs_subcategorias_Trabajos: MutableList<String>? = null
    ) {
        val db = FirebaseFirestore.getInstance().collection("solicitudes_servicios")
            .document("verificaciones").collection("activos")
        binding.listaPublicacionesTrabajadores.isVisible = false
        lista_dataclass_trabajos.clear()

        db.get().addOnSuccessListener { res ->
            val trabajadores = res.documents
            if (trabajadores.isEmpty()) return@addOnSuccessListener

            var completados = 0
            val total = trabajadores.size

            for (document in trabajadores) {
                val id_trabajador = document.id

                val dbUsers = FirebaseFirestore.getInstance()
                    .collection("Trabajadores_Usuarios_Drivers")
                    .document("trabajadores")
                    .collection("trabajadores")
                    .document(id_trabajador)
                    .collection("publicaciones_trabajos")

                dbUsers.get().addOnSuccessListener { productos ->
                    if (!productos.isEmpty) {
                        val aleatorio = productos.documents.random()
                        val img_producto = aleatorio.get("img_url") as? String ?: ""
                        val idPublicaciones = aleatorio.get("id") as? String ?: ""
                        val titulo = aleatorio.get("titulo") as? String ?: ""
                        val contenido = aleatorio.get("contenido") as? String ?: ""
                        val fecha = aleatorio.get("fecha_rec") as? String ?: ""
                        val hora = aleatorio.get("hora_rec") as? String ?: ""

                        val hashtags_generales =
                            aleatorio.get("hashtags_generales") as? List<String>
                        val categorias_trabajosDB = aleatorio.get("categoria") as? String ?: ""
                        val hashtags_trabajos_publicadosDB =
                            aleatorio.get("hashtags_trabajos_publicados") as? List<String>

                        val cumpleHashtags =
                            hashtags_generales?.any { it in hashtagsGenerales } == true
                        val cumpleCategoria =
                            categorias_Trabajadores == null || categorias_trabajosDB == categorias_Trabajadores
                        val cumpleSubHashtags =
                            hastgs_subcategorias_Trabajos == null || (hashtags_trabajos_publicadosDB?.any { it in hastgs_subcategorias_Trabajos } == true)

                        if (cumpleHashtags && cumpleCategoria && cumpleSubHashtags) {
                            val yaAgregado =
                                lista_dataclass_trabajos.any { it.id == idPublicaciones }
                            if (!yaAgregado) {
                                hashtagsGenerales.forEach {
                                    Log.d("Hashtag_obtenidos", "$it ,$categorias_Trabajadores")
                                }

                                val lista = dataclass_adapter_promociones(
                                    img_producto, null, null, null,
                                    titulo,
                                    contenido,
                                    idPublicaciones,
                                    fecha,
                                    hora,id_trabajador
                                )
                                binding.cargandoContenido.isVisible = false
                                binding.listaPublicacionesTrabajadores.isVisible = true
                                lista_dataclass_trabajos.add(lista)

                                Log.d(
                                    "obtnerDatos",
                                    "img: $img_producto | id: $idPublicaciones | titulo: $titulo | contenido: $contenido | fecha: $fecha | hora: $hora"
                                )
                            }
                        }
                    }

                    completados++
                    if (completados == total) {
                        inicializarTrabajosFiltrados(id_trabajador,lista_dataclass_trabajos)
                    }

                }.addOnFailureListener { e ->
                    completados++
                    Log.e("ProductosVerificados", "Error al obtener documentos", e)

                    if (completados == total) {
                        inicializarTrabajosFiltrados(id_trabajador,lista_dataclass_trabajos)
                    }
                }
            }
        }.addOnFailureListener { e ->
            Log.e("ProductosVerificados", "Error al obtener documentos", e)
        }
    }


}