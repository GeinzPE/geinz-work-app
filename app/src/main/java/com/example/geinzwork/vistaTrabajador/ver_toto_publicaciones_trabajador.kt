package com.example.geinzwork.vistaTrabajador

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.geinzwork.adapterViewholder.adapter_seguirTrabajadores_info
import com.example.geinzwork.adapterViewholder.adapter_trabajos_realizados_trabajador
import com.example.geinzwork.dataclass.dataClasSeguirTrabajdores_info
import com.example.geinzwork.dataclass.dataclass_adapter_promociones
import com.geinzz.geinzwork.R
import com.geinzz.geinzwork.databinding.ActivityVerTotoPublicacionesTrabajadorBinding
import com.geinzz.geinzwork.databinding.FragmentInfoBinding
import com.geinzz.geinzwork.dataclass.dataClassTrabajosd
import com.geinzz.geinzwork.dataclass.dataclas_trabajos_ralizados
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.firebase.firestore.FirebaseFirestore
import org.imaginativeworld.whynotimagecarousel.model.CarouselItem

class ver_toto_publicaciones_trabajador : AppCompatActivity() {
    private lateinit var binding: ActivityVerTotoPublicacionesTrabajadorBinding
    private var lista_dataclass_trabajos= mutableListOf<dataclass_adapter_promociones>()
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
                        val chip = Chip(this)
                        chip.text = hashtag
                        chip.isCheckable = true
                        chip.isClickable = true
                        chip.setOnClickListener {
                            binding.categoriaTrabajosPublicados.isVisible = false
                            if (hayAlgunChipSeleccionado(chipGroup)) {
                                setear_chips_categorias()
                                binding.linealCategoriasTrabajos.isVisible = true
                            } else {

                                binding.linealCategoriasTrabajos.isVisible = false
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
            // binding.apartadoHashtagsGenerales.visibility = View.GONE
        }
    }


    private fun hayAlgunChipSeleccionado(chipGroup: ChipGroup): Boolean {
        for (i in 0 until chipGroup.childCount) {
            val chip = chipGroup.getChildAt(i) as Chip
            if (chip.isChecked) {
                return true
            }
        }
        return false
    }


    private fun setear_chips_categorias() {
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

                        val chip = Chip(this)
                        chip.text = id
                        chip.isCheckable = true
                        chip.isClickable = true

                        chip.setOnClickListener {
                            // Desactiva todos los chips
                            for (i in 0 until chipGroup.childCount) {
                                val otherChip = chipGroup.getChildAt(i) as Chip
                                if (otherChip != chip) {
                                    otherChip.isChecked = false
                                }
                            }
                            if (hayAlgunChipSeleccionado(chipGroup)) {
                                binding.categoriaTrabajosPublicados.isVisible = true
                                obtenerHastags_cada_cat(id)
                            } else {
                                binding.categoriaTrabajosPublicados.isVisible = false
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


    private fun obtenerHastags_cada_cat(document: String) {
        val db = FirebaseFirestore.getInstance()
        val subcategoriasRef = db.collection("subcategoriasTrabajos").document(document)

        val tiempoInicio = System.currentTimeMillis()

        // Mostrar apartado mientras se carga
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
                        val chip = Chip(this)
                        chip.text = hashtag
                        chip.isCheckable = true
                        chip.isClickable = true
                        chip.setOnClickListener {
                            Toast.makeText(
                                this,
                                "El seleccionado es ${chip.text}",
                                Toast.LENGTH_SHORT
                            ).show()
                            if (hayAlgunChipSeleccionado(chipGroup)) {
                                Toast.makeText(
                                    this,
                                    "¡Hay al menos un chip seleccionado!",
                                    Toast.LENGTH_SHORT
                                ).show()
                            } else {
                                Toast.makeText(
                                    this,
                                    "Debes seleccionar al menos uno",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                        chipGroup.addView(chip)
                    }

                    // Ocultar apartado de carga y mostrar contenido
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
                        val img_producto = aleatorio.get("img_url   x") as? String ?: ""
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
                            hora
                        )
                        lista_dataclass_trabajos.add(lista)

                        Log.d("obtnerDatos", "img: $img_producto | id: $idPublicaciones | titulo: $titulo | contenido: $contenido | fecha: $fecha | hora: $hora")
                    }

                    completados++
                    if (completados == total) {
                        // Solo cuando se hayan completado todos
                        inicializarTrabajosFiltrados(lista_dataclass_trabajos)
                    }

                }.addOnFailureListener { e ->
                    completados++
                    Log.e("ProductosVerificados", "Error al obtener documentos", e)

                    if (completados == total) {
                        inicializarTrabajosFiltrados(lista_dataclass_trabajos)
                    }
                }
            }
        }.addOnFailureListener { e ->
            Log.e("ProductosVerificados", "Error al obtener documentos", e)
        }
    }


    private fun inicializarTrabajosFiltrados(
//        idTrabajador: String,
        listaFiltrada: MutableList<dataclass_adapter_promociones>
    ) {
        val recicle = binding.listaPublicacionesTrabajadores
        recicle.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        recicle.adapter =
            adapter_trabajos_realizados_trabajador(true, listaFiltrada.toMutableList()) { item ->
//                dialog = BottomSheetDialog(this)
//                showBottomShetDialogAnuncios(idTrabajador, item)
//                dialog.show()
            }


    }


}