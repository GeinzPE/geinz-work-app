package com.example.geinzwork.publicaciones_trabajadores

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.viewbinding.ViewBinding
import com.example.geinzwork.adapterViewholder.adapterCategoriasPromocionesFiltrado
import com.example.geinzwork.adapterViewholder.adapter_trabajos_realizados_trabajador
import com.example.geinzwork.constantesGeneral.constatnes_carga_imagenes_general
import com.example.geinzwork.dataclass.dataclasCaterogirasFiltrado
import com.example.geinzwork.dataclass.dataclass_adapter_promociones
import com.geinzz.geinzwork.R
import com.geinzz.geinzwork.constantesGeneral.constantestextos_general
import com.geinzz.geinzwork.databinding.ActivityMostrarTodosTrabajosBinding
import com.geinzz.geinzwork.databinding.BottomSheetMostarTrabajosRecientesBinding
import com.geinzz.geinzwork.databinding.ItemCustomFixedSizeLayout2Binding
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.firestore.FirebaseFirestore
import org.imaginativeworld.whynotimagecarousel.listener.CarouselListener
import org.imaginativeworld.whynotimagecarousel.model.CarouselItem
import org.imaginativeworld.whynotimagecarousel.utils.setImage
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class mostrarTodosTrabajos : AppCompatActivity() {
    private lateinit var adapterCategorias: adapterCategoriasPromocionesFiltrado
    private val listaMas_promo = mutableListOf<dataclass_adapter_promociones>()
    private lateinit var dialog: BottomSheetDialog
    private val listaCategorias = mutableListOf<dataclasCaterogirasFiltrado>()
    private lateinit var binding: ActivityMostrarTodosTrabajosBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMostrarTodosTrabajosBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        val idTrabajador = intent.getStringExtra("idTrabajador").toString()

        obtenerTodosTRabajos(idTrabajador)
        obtenerSucategoriasFiltrado(idTrabajador)
    }

    private fun obtenerTodosTRabajos(idTrabajador: String) {
        val db = FirebaseFirestore.getInstance()
            .collection("Trabajadores_Usuarios_Drivers").document("trabajadores")
            .collection("trabajadores").document(idTrabajador).collection("publicaciones_trabajos")

        binding.progresCargandoContenido.isVisible = true // Mostrar el ProgressBar

        db.get().addOnSuccessListener { res ->
            listaMas_promo.clear() // Limpiar la lista para evitar duplicados

            for (datos in res) {
                val data = datos.data
                val img_url = data?.get("img_url") as? String ?: ""
                val img2 = data?.get("img_url2") as? String ?: ""
                val img3 = data?.get("img_url3") as? String ?: ""
                val img4 = data?.get("img_url4") as? String ?: ""
                val titulo = data?.get("titulo") as? String ?: ""
                val contenido = data?.get("contenido") as? String ?: ""
                val fecha = data?.get("fecha_rec") as? String ?: ""
                val hora = data?.get("hora_rec") as? String ?: ""
                val id = data?.get("id") as? String ?: ""

                val dataClass =
                    dataclass_adapter_promociones(
                        img_url,
                        img2,
                        img3,
                        img4,
                        titulo,
                        contenido,
                        id,
                        fecha,
                        hora
                    )
                listaMas_promo.add(dataClass)
            }

            // Usamos Handler para agregar un pequeño retraso antes de ocultar el ProgressBar
            Handler(Looper.getMainLooper()).postDelayed({
                if (listaMas_promo.isNotEmpty()) {
                    listaMas_promo.shuffle() // Mezclar aleatoriamente los elementos
                    binding.trabajosRealizados.isVisible = true
                    binding.noSeEncontro.isVisible = false
                    inicializarTrabajosRealizadosVertical(idTrabajador)
                } else {
                    binding.trabajosRealizados.isVisible = false
                    binding.noSeEncontro.isVisible = true
                    Log.d("error obtenerDAtos", "No hay datos para mostrar")
                }
                binding.progresCargandoContenido.isVisible = false // Ocultar el ProgressBar
            }, 2000) // 🔹 Retraso de 1 segundo antes de ocultarlo

        }.addOnFailureListener { e ->
            binding.progresCargandoContenido.isVisible = false
            binding.trabajosRealizados.isVisible = false
            binding.noSeEncontro.isVisible = true
            println("error al encontrar $e")
        }
    }


    private fun showBottomShetDialogAnuncios(
        idTrabajador: String,
        item: dataclass_adapter_promociones,

        ) {
        val bindingMostrar =
            BottomSheetMostarTrabajosRecientesBinding.inflate(LayoutInflater.from(this))
        dialog.setContentView(bindingMostrar.root)
        val cerrar = bindingMostrar.cerrar
        cerrar.setOnClickListener {
            dialog.dismiss()
        }
//        constatnes_carga_imagenes_general.changer_img(
//            bindingMostrar.progressCargaImagen,
//            this, item.img.toString(), null, bindingMostrar.imgTrabajo, "portada", null
//        ) {}
        val listaImg =
            listOf(item.img, item.img2, item.img3, item.img4)
        if (listaImg.isNotEmpty()) {
            val carouselItems = listaImg.map { CarouselItem(it) }
            bindingMostrar.carruselImgTrabajos.registerLifecycle(lifecycle)
            bindingMostrar.carruselImgTrabajos.carouselListener = object : CarouselListener {
                override fun onCreateViewHolder(
                    layoutInflater: LayoutInflater,
                    parent: ViewGroup,
                ): ViewBinding? {
                    return ItemCustomFixedSizeLayout2Binding.inflate(
                        layoutInflater,
                        parent,
                        false
                    )
                }

                override fun onBindViewHolder(
                    binding: ViewBinding,
                    item: CarouselItem,
                    position: Int,
                ) {
                    val currentBinding = binding as ItemCustomFixedSizeLayout2Binding
                    currentBinding.imageView.apply {
                        setImage(item, R.drawable.ic_wb_cloudy_with_padding)
                        minimumScale = 1f
                        maximumScale = 10f
                        mediumScale = 5f
                    }
                }

            }
            bindingMostrar.carruselImgTrabajos.setData(carouselItems)

        }

        bindingMostrar.cargaProductosPromoTrabajos.linealMostrarTrabajos.isVisible = false
        bindingMostrar.textoTrabajosRealzados.text = item.texto_promo
        bindingMostrar.tituloTrabajosRealizados.text = item.titulo_promo
        constantestextos_general.extender_acortar_texto(
            bindingMostrar.textoTrabajosRealzados,
            bindingMostrar.tvReadMore
        )
    }

    private fun inicializarTrabajosRealizadosVertical(
        idTrabajador: String,
    ) {
        val recicle = binding.trabajosRealizados
        recicle.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        recicle.adapter = adapter_trabajos_realizados_trabajador(
            true,
            this.listaMas_promo,
        ) { item ->
            dialog = BottomSheetDialog(this)
            showBottomShetDialogAnuncios(idTrabajador, item)
            dialog.show()
        }
    }

    private fun inicializarRecicleCategorias() {
        var recicle = binding.categroiasFiltrado
        recicle.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        recicle.adapter = adapterCategorias
    }

    private fun obtenerSucategoriasFiltrado(idTrabajador: String) {
        listaCategorias.clear()

        // Agregar categorías estáticas
        listaCategorias.add(dataclasCaterogirasFiltrado("Todos"))
        listaCategorias.add(dataclasCaterogirasFiltrado("Hoy"))
        listaCategorias.add(dataclasCaterogirasFiltrado("Ultimamamente"))
        listaCategorias.add(dataclasCaterogirasFiltrado("Última semana"))
        listaCategorias.add(dataclasCaterogirasFiltrado("Último mes"))

        // Inicializar adapterCategorias antes de usarlo

        adapterCategorias = adapterCategoriasPromocionesFiltrado(listaCategorias.toList()) { item ->
            val listaFiltrada = when (item.nombreCategoria) {
                "Hoy" -> listaMas_promo.filter {
                    val fechaTrabajo = convertirFecha(it.fecha.toString())
                    val fechaLimite = convertirFecha(obtenerFechaLimite(0))
                    println("mandamos la fecha de ${it.fecha} y la fecha límite es $fechaLimite")
                    fechaTrabajo != null && fechaLimite != null && fechaTrabajo >= fechaLimite
                }

                "Ultimamamente" -> listaMas_promo.filter {
                    val fechaTrabajo = convertirFecha(it.fecha.toString())
                    val fechaLimite = convertirFecha(obtenerFechaLimite(5))
                    println("mandamos la fecha de ${it.fecha} y la fecha límite es $fechaLimite")
                    fechaTrabajo != null && fechaLimite != null && fechaTrabajo >= fechaLimite
                }

                "Última semana" -> listaMas_promo.filter {
                    val fechaTrabajo = convertirFecha(it.fecha.toString())
                    val fechaLimite = convertirFecha(obtenerFechaLimite(7))
                    println("mandamos la fecha de ${it.fecha} y la fecha límite es $fechaLimite")
                    fechaTrabajo != null && fechaLimite != null && fechaTrabajo >= fechaLimite
                }

                "Último mes" -> listaMas_promo.filter {
                    val fechaTrabajo = convertirFecha(it.fecha.toString())
                    val fechaLimite = convertirFecha(obtenerFechaLimite(30))
                    println("mandamos la fecha de ${it.fecha} y la fecha límite es $fechaLimite")
                    fechaTrabajo != null && fechaLimite != null && fechaTrabajo >= fechaLimite
                }

                else -> listaMas_promo // Si no coincide con ninguna, devolver toda la lista
            }

            // Verificar si se encontraron elementos
            if (listaFiltrada.isNotEmpty()) {
                binding.trabajosRealizados.isVisible = true
                binding.noSeEncontro.isVisible = false
                binding.progresCargandoContenido.isVisible = false
                println("Se encontraron ${listaFiltrada.size} elementos para la categoría ${item.nombreCategoria}")
            } else {
                binding.trabajosRealizados.isVisible = false
                binding.progresCargandoContenido.isVisible = false
                binding.noSeEncontro.isVisible = true
                println("No se encontraron elementos para la categoría ${item.nombreCategoria}")
            }

            // Actualizar el RecyclerView con la lista filtrada
            inicializarTrabajosFiltrados(idTrabajador, listaFiltrada)
        }


        // Inicializar el RecyclerView
        inicializarRecicleCategorias()
    }

    private fun inicializarTrabajosFiltrados(
        idTrabajador: String,
        listaFiltrada: List<dataclass_adapter_promociones>
    ) {
        val recicle = binding.trabajosRealizados
        recicle.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        recicle.adapter =
            adapter_trabajos_realizados_trabajador(true, listaFiltrada.toMutableList()) { item ->
                dialog = BottomSheetDialog(this)
                showBottomShetDialogAnuncios(idTrabajador, item)
                dialog.show()
            }


    }

    fun convertirFecha(fecha: String): Date? {
        return try {
            val formato = SimpleDateFormat(
                "dd/MM/yyyy",
                Locale.getDefault()
            ) // Asegúrate que el formato sea correcto
            formato.parse(fecha)
        } catch (e: Exception) {
            null
        }
    }

    fun obtenerFechaLimite(diasAtras: Int): String {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, -diasAtras)
        val formato = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        return formato.format(calendar.time)
    }


}