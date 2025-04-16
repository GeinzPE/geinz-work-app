package com.example.geinzwork.vistaTrabajador


import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.viewbinding.ViewBinding
import com.example.geinzwork.constantesGeneral.constantes_vistas_publicaciones_productos_verificados
import com.example.geinzwork.publicaciones_trabajadores.mostrarTodosTrabajos
import com.geinzz.geinzwork.R
import com.geinzz.geinzwork.constantesGeneral.constantestextos_general
import com.geinzz.geinzwork.databinding.ActivityVistaVerPublicacionesTrabajadoresBinding
import com.geinzz.geinzwork.databinding.ItemCustomFixedSizeLayout2Binding
import com.google.firebase.firestore.FirebaseFirestore
import org.imaginativeworld.whynotimagecarousel.listener.CarouselListener
import org.imaginativeworld.whynotimagecarousel.model.CarouselItem
import org.imaginativeworld.whynotimagecarousel.utils.setImage

class vista_ver_publicaciones_trabajadores : AppCompatActivity() {
    private lateinit var binding: ActivityVistaVerPublicacionesTrabajadoresBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityVistaVerPublicacionesTrabajadoresBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        val idTrabajador = intent.getStringExtra("id_trabajador").toString()
        val id_publicacion_clikeada = intent.getStringExtra("id_publicacion").toString()
        obtener_publicacion_actual(idTrabajador, id_publicacion_clikeada)

        constantes_vistas_publicaciones_productos_verificados.obtener_perfil_trabajador(
            idTrabajador,
            binding.perfiltrabajador,
            this
        )
        constantes_vistas_publicaciones_productos_verificados.obtener_productosVenta(
            idTrabajador,
            this,
            binding.cargaProductosPromoTrabajos.masTrabajosRealiados
        ) { existe ->
            if (existe) {
                binding.cargaProductosPromoTrabajos.cargandoContenido.isVisible = false
                binding.cargaProductosPromoTrabajos.masTrabajosRealiados.isVisible = true
                binding.cargaProductosPromoTrabajos.linealNoSeEncontraron.isVisible = false

            } else {
                binding.cargaProductosPromoTrabajos.cargandoContenido.isVisible = false
                binding.cargaProductosPromoTrabajos.masTrabajosRealiados.isVisible = false
                binding.cargaProductosPromoTrabajos.linealNoSeEncontraron.isVisible = true
            }

        }
        constantes_vistas_publicaciones_productos_verificados.obtenerMasTrabajosRealiazdos(
            idTrabajador,
            id_publicacion_clikeada,
            this,
            binding.perfiltrabajador,
            binding.cargaPublicaiconesRealizadas.masTrabajosRealiados, lifecycle
        ) { existe ->
            if (existe) {
                binding.cargaPublicaiconesRealizadas.cargandoContenido.isVisible = false
                binding.cargaPublicaiconesRealizadas.masTrabajosRealiados.isVisible = true
                binding.cargaPublicaiconesRealizadas.linealNoSeEncontraron.isVisible = false

            } else {
                binding.cargaPublicaiconesRealizadas.cargandoContenido.isVisible = false
                binding.cargaPublicaiconesRealizadas.masTrabajosRealiados.isVisible = false
                binding.cargaPublicaiconesRealizadas.linealNoSeEncontraron.isVisible = true
            }
        }
        binding.cargaPublicaiconesRealizadas.cambiarTextoTrabajosRealziadosTrabajosRecientes.text =
            "Mas publicaciones recientes"
        binding.cargaPublicaiconesRealizadas.textoCambiarTrabajosOPublicaciones.text =
            "No se encontraron publicaciones"
        binding.cargaProductosPromoTrabajos.cambiarTextoTrabajosRealziadosTrabajosRecientes.text =
            "Productos publicados"
        binding.cargaProductosPromoTrabajos.textoCambiarTrabajosOPublicaciones.text =
            "No se encontraron productos"

        constantes_vistas_publicaciones_productos_verificados.ver_todos_productos_activity(
            binding.cargaProductosPromoTrabajos,
            this,
            idTrabajador
        )
        constantes_vistas_publicaciones_productos_verificados.ver_todo_publicaciones_activty(
            binding.cargaPublicaiconesRealizadas,
            this,
            idTrabajador
        )

    }

    private fun obtener_publicacion_actual(idTrabajador: String, idpublicaicon: String) {
        val tiempoInicio = System.currentTimeMillis()
        binding.contenidoCargado.isVisible = false

        val db = FirebaseFirestore.getInstance().collection("Trabajadores_Usuarios_Drivers")
            .document("trabajadores").collection("trabajadores").document(idTrabajador)
            .collection("publicaciones_trabajos").document(idpublicaicon)

        db.get().addOnSuccessListener { res ->
            val tiempoFin = System.currentTimeMillis()
            val tiempoTotalMs = tiempoFin - tiempoInicio

            if (res.exists()) {
                val data = res.data
                val titulo = data?.get("titulo") as? String ?: ""
                val contenido = data?.get("contenido") as? String ?: ""
                val img_url = data?.get("img_url") as? String ?: ""
                val img_url2 = data?.get("img_url2") as? String ?: ""
                val img_url3 = data?.get("img_url3") as? String ?: ""
                val img_url4 = data?.get("img_url4") as? String ?: ""
                val fecha_rec = data?.get("fecha_rec") as? String ?: ""
                val hora_rec = data?.get("hora_rec") as? String ?: ""
                val id = data?.get("id") as? String ?: ""

                val listaImg =
                    listOf(img_url, img_url2, img_url3, img_url4).filter { it.isNotEmpty() }
                val itemsCarrusel = listaImg.map { url -> CarouselItem(imageUrl = url) }

                // Esperamos con Handler un tiempo proporcional a la carga o mínimo 800 ms
                val handler = Handler(Looper.getMainLooper())
                val tiempoMinimoCarga = 800L
                val esperaExtra =
                    if (tiempoTotalMs < tiempoMinimoCarga) (tiempoMinimoCarga - tiempoTotalMs) else 0L

                handler.postDelayed({
                    binding.titulo.text = titulo
                    binding.descripcionPublicacion.text = contenido
                    constantestextos_general.extender_acortar_texto(
                        binding.descripcionPublicacion,
                        binding.tvReadMore
                    )

                    binding.carrucelPublicacionesRecientesImg.registerLifecycle(lifecycle)
                    binding.carrucelPublicacionesRecientesImg.carouselListener =
                        object : CarouselListener {
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

                    binding.carrucelPublicacionesRecientesImg.setData(itemsCarrusel)
                    binding.cargandoContenido.isVisible = false
                    binding.contenidoCargado.isVisible = true

                }, esperaExtra)

            }
        }.addOnFailureListener { e ->
            binding.cargandoContenido.isVisible = false
            Log.d("error_carga", "Error al cargar la publicación")
        }
    }


}