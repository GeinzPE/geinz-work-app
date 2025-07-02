package com.example.geinzwork.vistaTrabajador

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.ShortcutInfo
import android.content.pm.ShortcutManager
import android.graphics.PorterDuff
import android.graphics.Typeface
import android.graphics.drawable.Icon
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.TextWatcher
import android.text.style.StyleSpan
import android.text.style.UnderlineSpan
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.geinzwork.adapterViewholder.adapter_pbl_vr_tb_recientes
import com.example.geinzwork.constantesGeneral.Variables
import com.example.geinzwork.constantesGeneral.constantes_metodo_pago_entrega
import com.example.geinzwork.constantesGeneral.constantes_productos_publicados
import com.example.geinzwork.dataclass.dataclas_trabajos_ralizados_verificados
import com.example.geinzwork.dataclass.dataclass_texto_descripcion_pr
import com.geinzz.geinzwork.MainActivity
import com.geinzz.geinzwork.R
import com.geinzz.geinzwork.constantesGeneral.constantesCarrito
import com.geinzz.geinzwork.constantesGeneral.constantesDatosUsuarioTienda
import com.geinzz.geinzwork.constantesGeneral.constantestextos_general
import com.geinzz.geinzwork.constantesGeneral.mostrarFechaDialog_horaDialog
import com.geinzz.geinzwork.databinding.ActivityVerProductosPublicadosBinding
import com.geinzz.geinzwork.databinding.BottoSheetEditarMEntrPagBinding
import com.geinzz.geinzwork.databinding.BottomSheetCamposTrPdPBinding
import com.geinzz.geinzwork.databinding.BottomSheetConfiguracionDescripcionPrVrBinding
import com.geinzz.geinzwork.databinding.BottomSheetEditarCaracteristicasPublicidadBinding
import com.geinzz.geinzwork.databinding.BottomSheetEditarProductoBinding
import com.geinzz.geinzwork.databinding.BottomSheetMinimoMaxFiltradoBinding
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.chip.Chip
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlin.math.roundToInt

class ver_productos_publicados : AppCompatActivity() {
    private lateinit var binding: ActivityVerProductosPublicadosBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var adapter: adapter_pbl_vr_tb_recientes
    private lateinit var dialog: BottomSheetDialog
    private var unidadGarantia: String = ""
    private var descuento: Boolean = false
    private val hashtagsGeneralesList = mutableListOf<String>()
    private var lista = mutableListOf<dataclas_trabajos_ralizados_verificados>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityVerProductosPublicadosBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        firebaseAuth = FirebaseAuth.getInstance()
        adapter = adapter_pbl_vr_tb_recientes(lista, { item ->
            dialog = BottomSheetDialog(this)
            bottomSheet_editar_eliminar_Arhivar_estadi(item)
            dialog.show()
        }, { cantidadSeleccionados, listaSeleccionados ->  // 🔴 ahora recibes ambos
            binding.modoSelecion.text = "$cantidadSeleccionados Selecionados"
            binding.modoSelecion.isVisible = true
            binding.titulosCentrado.isVisible = false
            if (cantidadSeleccionados > 0) {
                binding.cerrarselecion.isVisible = true
                binding.listartododos.isVisible = true
                binding.bottomOpciones.isVisible = true

                val idsSeleccionados = listaSeleccionados.map { it.id_publicacion }
                binding.idsObtenidos.text = "$idsSeleccionados"

                val todosSeleccionados = cantidadSeleccionados == lista.size
                if (todosSeleccionados) {
                    binding.deslistar.setColorFilter(
                        ContextCompat.getColor(
                            this,
                            R.color.blue
                        ),  // reemplaza con tu color deseado
                        PorterDuff.Mode.SRC_IN
                    )
                    binding.deslistar.isVisible = true
                    binding.listartododos.isVisible = false
                    // Aquí puedes hacer algo más si quieres
                    Toast.makeText(this, "se sleeicono todos", Toast.LENGTH_SHORT).show()
                } else {
                    binding.deslistar.isVisible = false
                    binding.listartododos.isVisible = true

                    println("❌ Aún faltan elementos por seleccionar.")
                }
            } else {
                // Modo selección activo pero sin elementos seleccionados
                binding.modoSelecion.isVisible = false
                binding.titulosCentrado.isVisible = true
                binding.bottomOpciones.isVisible = false
                binding.recicleProductos.invalidate()
            }

        })

        if (binding.todos.isChecked) {
            binding.archivarselect.isVisible = true
            binding.reactivar.isVisible = false
            binding.eliminarselect.isVisible = true
            binding.soloSeguidoresMov.isVisible = true
            binding.ocultarPublicaciones.isVisible = true
            adapter.cancelarModoSeleccion()
            binding.eliminarselect.setOnClickListener {
                ejecutarCambioDePublicaciones("eliminados", "todos")
            }

            binding.archivarselect.setOnClickListener {
                ejecutarCambioDePublicaciones("archivados", "todos")
            }

            binding.ocultarPublicaciones.setOnClickListener {
                ejecutarCambioDePublicaciones("privado", "todos")
            }

            binding.soloSeguidoresMov.setOnClickListener {
                ejecutarCambioDePublicaciones("solo_seguidores", "todos")
            }

        }

        binding.cerrarselecion.setOnClickListener {
            Toast.makeText(this, "se realziao clik ", Toast.LENGTH_SHORT).show()
            binding.cerrarselecion.isVisible = false
            binding.listartododos.isVisible = false
            binding.deslistar.isVisible = false
            binding.bottomOpciones.isVisible = false
            binding.modoSelecion.isVisible = false
            adapter.cancelarModoSeleccion()
        }

        binding.listartododos.setOnClickListener {
            binding.deslistar.isVisible = true
            binding.deslistar.setColorFilter(
                ContextCompat.getColor(this, R.color.blue),  // reemplaza con tu color deseado
                PorterDuff.Mode.SRC_IN
            )
            adapter.seleccionarTodos()
        }

        binding.deslistar.setOnClickListener {
            binding.listartododos.isVisible = true
            binding.deslistar.isVisible = false
            adapter.deseleccionarTodosSinSalirDeModo()
        }

        val dato_pasado = intent.getStringExtra("tipo").toString()
        Log.d("DebugTipo", "dato_pasado: $dato_pasado")

        handleIncomingIntent(intent)

        when (dato_pasado) {
            "publicadas" -> {
                binding.linealChips.isVisible = true
                obtener_productos("publicados", "No se encontraron publicaciones")
                binding.todos.setOnClickListener {
                    binding.archivarselect.isVisible = false
                    binding.eliminarselect.isVisible = false
                    binding.reactivar.isVisible = true
                    binding.soloSeguidoresMov.isVisible = true
                    binding.ocultarPublicaciones.isVisible = true
                    binding.linealEncontrados.isVisible = false
                    adapter.cancelarModoSeleccion()
                    obtener_productos("publicados", "No se encontraron publicaciones")
                    binding.eliminarselect.isVisible = true
                    binding.archivarselect.isVisible = true
                    binding.reactivar.isVisible = false
                    binding.eliminarselect.setOnClickListener {
                        ejecutarCambioDePublicaciones("eliminados", "todos")
                    }
                    binding.archivarselect.setOnClickListener {
                        ejecutarCambioDePublicaciones("archivados", "todos")
                    }
                    binding.ocultarPublicaciones.setOnClickListener {
                        ejecutarCambioDePublicaciones("privado", "todos")
                    }
                    binding.soloSeguidoresMov.setOnClickListener {
                        ejecutarCambioDePublicaciones("solo_seguidores", "todos")
                    }
                }
                binding.masClicks.setOnClickListener {
                    adapter.cancelarModoSeleccion()
                    binding.archivarselect.isVisible = true
                    binding.eliminarselect.isVisible = true
                    binding.ocultarPublicaciones.isVisible = true
                    binding.soloSeguidoresMov.isVisible = true
                    binding.reactivar.isVisible = false
                    binding.linealEncontrados.isVisible = true
                    binding.recicleProductos.isVisible = false
                    obtener_mayor_menor_cantidad_campos("estadisticas_click") { max, min ->
                        dialog = BottomSheetDialog(this)
                        bottom_sheet_chips(max, min) { minC, maxC ->
                            filtrar_publicaciones(minC, maxC, "estadisticas_click")
                        }
                        dialog.show()
                    }

                    binding.eliminarselect.setOnClickListener {
                        ejecutarCambioDePublicaciones("eliminados", "mascliks")
                    }

                    binding.archivarselect.setOnClickListener {
                        ejecutarCambioDePublicaciones("archivados", "mascliks")
                    }

                    binding.ocultarPublicaciones.setOnClickListener {
                        ejecutarCambioDePublicaciones("privado", "mascliks")
                    }

                    binding.soloSeguidoresMov.setOnClickListener {
                        ejecutarCambioDePublicaciones("solo_seguidores", "mascliks")
                    }

                }
                binding.masVistas.setOnClickListener {
                    adapter.cancelarModoSeleccion()
                    binding.ocultarPublicaciones.isVisible = true
                    binding.soloSeguidoresMov.isVisible = true
                    binding.archivarselect.isVisible = true
                    binding.eliminarselect.isVisible = true
                    binding.reactivar.isVisible = false
                    binding.linealEncontrados.isVisible = true
                    binding.recicleProductos.isVisible = false
                    obtener_mayor_menor_cantidad_campos("estadisticas_vistas") { max, min ->
                        dialog = BottomSheetDialog(this)
                        bottom_sheet_chips(max, min) { minC, maxC ->
                            filtrar_publicaciones(minC, maxC, "estadisticas_vistas")
                        }
                        dialog.show()
                    }

                    binding.eliminarselect.setOnClickListener {
                        ejecutarCambioDePublicaciones("eliminados", "masvistas")
                    }

                    binding.archivarselect.setOnClickListener {
                        ejecutarCambioDePublicaciones("archivados", "masvistas")
                    }

                    binding.ocultarPublicaciones.setOnClickListener {
                        ejecutarCambioDePublicaciones("privado", "masvistas")
                    }

                    binding.soloSeguidoresMov.setOnClickListener {
                        ejecutarCambioDePublicaciones("solo_seguidores", "masvistas")
                    }

                }
                binding.masCompartidas.setOnClickListener {
                    binding.ocultarPublicaciones.isVisible = true
                    binding.soloSeguidoresMov.isVisible = true
                    adapter.cancelarModoSeleccion()
                    binding.archivarselect.isVisible = true
                    binding.eliminarselect.isVisible = true
                    binding.reactivar.isVisible = false
                    binding.linealEncontrados.isVisible = true
                    binding.recicleProductos.isVisible = false
                    obtener_mayor_menor_cantidad_campos("estadisticas_compartir") { max, min ->

                        dialog = BottomSheetDialog(this)
                        bottom_sheet_chips(max, min) { minC, maxC ->
                            filtrar_publicaciones(minC, maxC, "estadisticas_compartir")
                        }
                        dialog.show()
                    }

                    binding.eliminarselect.setOnClickListener {
                        ejecutarCambioDePublicaciones("eliminados", "mascompartidas")
                    }

                    binding.archivarselect.setOnClickListener {
                        ejecutarCambioDePublicaciones("archivados", "mascompartidas")
                    }

                    binding.ocultarPublicaciones.setOnClickListener {
                        ejecutarCambioDePublicaciones("privado", "mascompartidas")
                    }

                    binding.soloSeguidoresMov.setOnClickListener {
                        ejecutarCambioDePublicaciones("solo_seguidores", "mascompartidas")
                    }
                }

                binding.privado.setOnClickListener {
                    binding.archivarselect.isVisible = false
                    binding.eliminarselect.isVisible = false
                    binding.reactivar.isVisible = true
                    binding.soloSeguidoresMov.isVisible = false
                    binding.ocultarPublicaciones.isVisible = false
                    adapter.cancelarModoSeleccion()
                    binding.linealEncontrados.isVisible = true
                    binding.recicleProductos.isVisible = false
                    obtener_productos("privado", "No se encontraron publicaciones")
                    binding.reactivar.setOnClickListener {
                        binding.linealEncontrados.isVisible = true
                        binding.textoDinamicoProgrees.text = "Cambiando publicaciones"
                        binding.linealPublicacionesFiltrados.isVisible = false
                        binding.bottomOpciones.isVisible = false
                        binding.recicleProductos.isVisible = false
                        val ids = binding.idsObtenidos.text
                            .toString()
                            .removePrefix("[")
                            .removeSuffix("]")
                            .split(",")
                            .map { it.trim() }

                        activar_publicacion_list(
                            "privado",
                            ids,
                            "privado"
                        )

                    }
                }
                binding.soloSeguidores.setOnClickListener {
                    binding.archivarselect.isVisible = false
                    binding.eliminarselect.isVisible = false
                    binding.soloSeguidoresMov.isVisible = false
                    binding.ocultarPublicaciones.isVisible = false
                    binding.reactivar.isVisible = true
                    adapter.cancelarModoSeleccion()
                    binding.linealEncontrados.isVisible = true
                    binding.recicleProductos.isVisible = false
                    obtener_productos("solo_seguidores", "No se encontraron publicaciones")
                    binding.reactivar.setOnClickListener {
                        binding.linealEncontrados.isVisible = true
                        binding.textoDinamicoProgrees.text = "Cambiando publicaciones"
                        binding.linealPublicacionesFiltrados.isVisible = false
                        binding.bottomOpciones.isVisible = false
                        binding.recicleProductos.isVisible = false
                        val ids = binding.idsObtenidos.text
                            .toString()
                            .removePrefix("[")
                            .removeSuffix("]")
                            .split(",")
                            .map { it.trim() }

                        activar_publicacion_list(
                            "solo_seguidores",
                            ids,
                            "solo_seguidores"
                        )

                    }
                }
            }

            "archivadas" -> {
                binding.archivarselect.isVisible = false
                binding.eliminarselect.isVisible = false
                binding.reactivar.isVisible = true
                binding.soloSeguidoresMov.isVisible = false
                binding.ocultarPublicaciones.isVisible = false
                binding.linealChips.isVisible = false

                binding.reactivar.setOnClickListener {
                    binding.linealEncontrados.isVisible = true
                    binding.textoDinamicoProgrees.text = "Cambiando publicaciones"
                    binding.linealPublicacionesFiltrados.isVisible = false
                    binding.bottomOpciones.isVisible = false
                    binding.recicleProductos.isVisible = false
                    val ids = binding.idsObtenidos.text
                        .toString()
                        .removePrefix("[")
                        .removeSuffix("]")
                        .split(",")
                        .map { it.trim() }

                    activar_publicacion_list(
                        "archivados",
                        ids,
                        "archivados"
                    )

                }
                obtener_productos("archivados", "No se encontraron publicaciones")
            }

            "eliminadas" -> {
                binding.archivarselect.isVisible = false
                binding.eliminarselect.isVisible = false
                binding.reactivar.isVisible = true
                binding.soloSeguidoresMov.isVisible = false
                binding.ocultarPublicaciones.isVisible = false
                binding.linealChips.isVisible = false
                obtener_productos("eliminados", "No se encontraron publicaciones")
                binding.reactivar.setOnClickListener {
                    binding.linealEncontrados.isVisible = true
                    binding.textoDinamicoProgrees.text = "Cambiando publicaciones"
                    binding.linealPublicacionesFiltrados.isVisible = false
                    binding.bottomOpciones.isVisible = false
                    binding.recicleProductos.isVisible = false
                    val ids = binding.idsObtenidos.text
                        .toString()
                        .removePrefix("[")
                        .removeSuffix("]")
                        .split(",")
                        .map { it.trim() }

                    activar_publicacion_list(
                        "eliminados",
                        ids,
                        "eliminados"
                    )

                }
            }

            else -> {
                binding.linealChips.isVisible = true
                obtener_productos("publicados", "No se encontraron publicaciones")
                binding.masClicks.setOnClickListener {
                    binding.linealEncontrados.isVisible = true
                    binding.recicleProductos.isVisible = false
                    obtener_mayor_menor_cantidad_campos("estadisticas_click") { max, min ->
                        dialog = BottomSheetDialog(this)
                        bottom_sheet_chips(max, min) { minC, maxC ->
                            filtrar_publicaciones(minC, maxC, "estadisticas_click")

                        }
                        dialog.show()
                    }

                }
                binding.masVistas.setOnClickListener {
                    binding.linealEncontrados.isVisible = true
                    binding.recicleProductos.isVisible = false
                    obtener_mayor_menor_cantidad_campos("estadisticas_vistas") { max, min ->
                        dialog = BottomSheetDialog(this)
                        bottom_sheet_chips(max, min) { minC, maxC ->
                            filtrar_publicaciones(minC, maxC, "estadisticas_vistas")
                        }
                        dialog.show()
                    }
                }
                binding.masCompartidas.setOnClickListener {
                    binding.linealEncontrados.isVisible = true
                    binding.recicleProductos.isVisible = false
                    obtener_mayor_menor_cantidad_campos("estadisticas_compartir") { max, min ->

                        dialog = BottomSheetDialog(this)
                        bottom_sheet_chips(max, min) { minC, maxC ->
                            filtrar_publicaciones(minC, maxC, "estadisticas_compartir")
                        }
                        dialog.show()
                    }
                }
                binding.privado.setOnClickListener {
                    binding.archivarselect.isVisible = false
                    binding.eliminarselect.isVisible = false
                    binding.reactivar.isVisible = true
                    adapter.cancelarModoSeleccion()
                    binding.linealEncontrados.isVisible = true
                    binding.recicleProductos.isVisible = false
                    obtener_productos("privado", "No se encontraron publicaciones")

                }
                binding.soloSeguidores.setOnClickListener {
                    binding.archivarselect.isVisible = false
                    binding.eliminarselect.isVisible = false
                    binding.reactivar.isVisible = true
                    adapter.cancelarModoSeleccion()
                    binding.linealEncontrados.isVisible = true
                    binding.recicleProductos.isVisible = false
                    obtener_productos("solo_seguidores", "No se encontraron publicaciones")

                }
            }
        }


    }

    private fun ejecutarCambioDePublicaciones(destino: String, funcionFiltro: String) {
        Toast.makeText(this, "Seleccionaste la opción: $destino", Toast.LENGTH_SHORT).show()
        binding.linealEncontrados.isVisible = true
        binding.textoDinamicoProgrees.text = "Cambiando publicaciones"
        binding.bottomOpciones.isVisible = false
        binding.recicleProductos.isVisible = false
        binding.cerrarselecion.isVisible = false
        binding.listartododos.isVisible = false
        binding.deslistar.isVisible = false
        adapter.cancelarModoSeleccion()

        val texto = binding.idsObtenidos.text.toString()
        val listaIDs = texto.removePrefix("[")
            .removeSuffix("]")
            .split(",")
            .map { it.trim() }
            .filter { it.isNotEmpty() }

        archivar_eliminar_publicaciones_list(
            listaIDs,
            "publicados", // origen fijo
            destino,      // destino variable
            funcionFiltro // función filtro
        )
    }

    override fun onBackPressed() {
        if (adapter.estaEnModoSeleccion()) {
            // Salir del modo selección en vez de cerrar la actividad
            adapter.cancelarModoSeleccion()

            // Ocultar UI relacionada a la selección
            binding.modoSelecion.text = ""
            binding.bottomOpciones.isVisible = false
            binding.cerrarselecion.isVisible = false
            binding.deslistar.isVisible = false
            binding.listartododos.isVisible = false

            Toast.makeText(this, "Selección cancelada", Toast.LENGTH_SHORT).show()

        } else {
            // Si no hay selección activa, se comporta como siempre
            super.onBackPressed()
        }
    }

    private fun handleIncomingIntent(intent: Intent?) {
        intent?.let {
            // Ejemplo: Si el Intent lleva un "shortcut_action"
            val shortcutAction = it.getStringExtra("shortcut_action")
            when (shortcutAction) {
                "open_feature_X" -> {
                    Toast.makeText(
                        this,
                        "Abriendo Característica X desde acceso directo",
                        Toast.LENGTH_SHORT
                    ).show()
                    // Aquí podrías navegar a un Fragment, o iniciar una nueva Activity
                    // Por ejemplo: supportFragmentManager.beginTransaction().replace(R.id.fragment_container, FeatureXFragment()).commit()
                }
                // Añade más casos según los datos que envíes en tus Intents de atajo
            }
            // Si usas Deeplinks con Uri:
            val data: Uri? = it.data
            data?.let { uri ->
                if (uri.host == "geinzapp.page.link" && uri.pathSegments.contains("chat")) {
                    val chatId = uri.lastPathSegment
                    Toast.makeText(this, "Abriendo chat con ID: $chatId", Toast.LENGTH_SHORT).show()
                    // Aquí podrías abrir una pantalla de chat con el chatId
                }
            }
        }
    }

    // ... (resto de tu código)


    private fun filtrar_publicaciones(min: Int, max: Int, filtado_pasado: String) {
        lista.clear()
        Toast.makeText(
            this@ver_productos_publicados,
            "Filtramos por el mínimo de $min y el máximo de $max",
            Toast.LENGTH_SHORT
        ).show()

        val firestore = FirebaseFirestore.getInstance()
        val uid = firebaseAuth.uid.toString()
        val refPublicacion = firestore.collection("Trabajadores_Usuarios_Drivers")
            .document("trabajadores").collection("trabajadores")
            .document(uid).collection("productos_venta")
            .document("publicados").collection("publicados")

        refPublicacion.get().addOnSuccessListener { res ->
            // Asegúrate de limpiar la lista antes de agregar nuevos datos

            for (datos in res) {
                val data = datos.data

                val cliks = (data[filtado_pasado] as? Number)?.toInt() ?: continue

                // ✅ Aplicar el filtro
                if (cliks in min..max) {
                    val img_url = data["img_url"] as? String ?: ""
                    val titulo = data["titulo"] as? String ?: ""
                    val contenido = data["contenido"] as? String ?: ""
                    val hora_rec = data["hora_rec"] as? String ?: ""
                    val fecha_rec = data["fecha_rec"] as? String ?: ""
                    val id = data["id"] as? String ?: ""
                    val cliks = data["estadisticas_click"] as? Number ?: 0
                    val vista = data["estadisticas_vistas"] as? Number ?: 0
                    val compartidas = data["estadisticas_compartir"] as? Number ?: 0

                    val publicacion = dataclas_trabajos_ralizados_verificados(
                        img_url,
                        titulo,
                        contenido,
                        hora_rec,
                        fecha_rec,
                        id,
                        vista,
                        compartidas,
                        cliks
                    )
                    lista.add(publicacion)
                }
            }

            if (lista.isEmpty()) {
                binding.linealNoCuenta.isVisible = true
                binding.recicleProductos.isVisible = false
                binding.linealEncontrados.isVisible = false
            } else {
                binding.linealNoCuenta.isVisible = false
                binding.linealEncontrados.isVisible = false
                binding.recicleProductos.isVisible = true
                binding.max.text = max.toString()
                binding.min.text = min.toString()
                inicializarRecicle(binding.recicleProductos, adapter, this)
                adapter.notifyDataSetChanged()
            }
        }
    }

    private fun bottom_sheet_chips(
        max: String,
        min: String,
        maximo_min: (Int, Int) -> Unit
    ) {
        val bottomChips = BottomSheetMinimoMaxFiltradoBinding.inflate(LayoutInflater.from(this))
        val view = bottomChips.root

        // Mostrar valores de referencia
        constantestextos_general.setearInformacionboldDescripcion(
            "Valor mínimo", SpannableString("Valor mínimo: $min"), bottomChips.minimo
        )

        constantestextos_general.setearInformacionboldDescripcion(
            "Valor máximo", SpannableString("Valor máximo: $max"), bottomChips.maximo
        )

        bottomChips.Filtrar.setOnClickListener {
            val valorMinUsuario = bottomChips.minED.text.toString().toIntOrNull()
            val valorMaxUsuario = bottomChips.maxED.text.toString().toIntOrNull()
            val minPermitido = min.toIntOrNull()
            val maxPermitido = max.toIntOrNull()

            if (valorMinUsuario == null || valorMaxUsuario == null) {
                Toast.makeText(
                    this,
                    "Por favor completa ambos campos correctamente",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            if (minPermitido == null || maxPermitido == null) {
                Toast.makeText(
                    this,
                    "Error interno al procesar los valores permitidos",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            when {
                valorMinUsuario < minPermitido -> {
                    bottomChips.minED.error = "Debe ser mayor o igual a $minPermitido"
                    bottomChips.minED.requestFocus()
                }

                valorMaxUsuario > maxPermitido -> {
                    bottomChips.maxED.error = "Debe ser menor o igual a $maxPermitido"
                    bottomChips.maxED.requestFocus()
                }

                valorMaxUsuario <= valorMinUsuario -> {
                    bottomChips.maxED.error = "El valor máximo debe ser mayor al mínimo"
                    bottomChips.maxED.requestFocus()
                }

                else -> {
                    binding.linealEncontrados.isVisible = true
                    binding.recicleProductos.isVisible = false
                    maximo_min(valorMinUsuario, valorMaxUsuario)
                    Toast.makeText(this, "Todo bien, filtrando...", Toast.LENGTH_SHORT).show()
                    dialog.dismiss()
                }
            }
        }

        dialog.setContentView(view)
    }

    private fun obtener_mayor_menor_cantidad_campos(
        campoFiltrado: String,
        max_min: (String, String) -> Unit
    ) {
        binding.linealNoCuenta.isVisible = false
        val firestore = FirebaseFirestore.getInstance()
        val uid = firebaseAuth.uid.toString()
        val refPublicacion = firestore.collection("Trabajadores_Usuarios_Drivers")
            .document("trabajadores").collection("trabajadores")
            .document(uid).collection("productos_venta")
            .document("publicados").collection("publicados")

        refPublicacion.get().addOnSuccessListener { res ->
            var valorMaximo: Int? = null
            var valorMinimo: Int? = null

            for (datos in res) {
                val data = datos.data
                val campo = (data[campoFiltrado] as? Number)?.toInt() ?: continue

                if (valorMaximo == null || campo > valorMaximo) {
                    valorMaximo = campo
                }
                if (valorMinimo == null || campo < valorMinimo) {
                    valorMinimo = campo
                }
            }

            if (valorMaximo != null && valorMinimo != null) {
                max_min(valorMaximo.toString(), valorMinimo.toString())
                Log.d("Valores", "Máximo: $valorMaximo - Mínimo: $valorMinimo")
                // Aquí puedes usar los valores como enteros
            } else {
                Handler(Looper.getMainLooper()).postDelayed({
                    binding.linealNoCuenta.isVisible = true
                    binding.textoSinEncontrar.text = "Sin datos para filtrar"
                    binding.linealEncontrados.isVisible = false
                    Log.d(
                        "Valores",
                        "No se encontraron datos válidos para el campo: $campoFiltrado"
                    )
                }, 1500) // 2000 ms = 2 segundos
            }
        }.addOnFailureListener {
            Log.e("Firestore", "Error al obtener documentos: ${it.message}")
        }
    }

    private fun bottomSheet_editar_eliminar_Arhivar_estadi(item: dataclas_trabajos_ralizados_verificados) {
        val bottoSheet = BottomSheetCamposTrPdPBinding.inflate(LayoutInflater.from(this))
        val view = bottoSheet.root
        val eliminar = bottoSheet.eliminar
        val estadisticas = bottoSheet.estadisticas
        val editar = bottoSheet.editar
        val solo_seguidores = bottoSheet.soloSeguidores
        val privado = bottoSheet.privado
        val archivar = bottoSheet.archivar
        val vista_previa = bottoSheet.vistaPrevia
        bottoSheet.idPublicacion.text = item.id_publicacion.toString()
        bottoSheet.copiarId.setOnClickListener {
            constantestextos_general.copiarTexto_portapapeles(
                bottoSheet.idPublicacion,
                this
            )
        }

        val dato_pasado = intent.getStringExtra("tipo").toString()
        if (dato_pasado.equals("publicadas")) {
            bottoSheet.linealIconosPrincipal.isVisible = true
            if (binding.masClicks.isChecked) {
                vista_previa.isVisible = false
                estadisticas.isVisible = false
                editar.isVisible = false
                eliminar.setOnClickListener {
                    dialog.dismiss()

                    eliminar_publicacion_Archivar(
                        item.id_publicacion.toString(),
                        "publicados",
                        "eliminados", "mascliks"
                    )

                }
                archivar.setOnClickListener {
                    dialog.dismiss()
                    eliminar_publicacion_Archivar(
                        item.id_publicacion.toString(),
                        "publicados",
                        "archivados", "mascliks"
                    )
                }
                privado.setOnClickListener {
                    dialog.dismiss()
                    eliminar_publicacion_Archivar(
                        item.id_publicacion.toString(),
                        "publicados",
                        "privado", "mascliks"
                    )

                }
                solo_seguidores.setOnClickListener {
                    dialog.dismiss()
                    eliminar_publicacion_Archivar(
                        item.id_publicacion.toString(),
                        "publicados",
                        "solo_seguidores", "mascliks"
                    )
                }
                binding.eliminarselect.setOnClickListener {
                    Toast.makeText(this, "mas clikscs elimamidno", Toast.LENGTH_SHORT)
                        .show()
                    binding.linealEncontrados.isVisible = true
                    binding.textoDinamicoProgrees.text = "Cambiando publicaciones"
                    binding.linealPublicacionesFiltrados.isVisible = false
                    binding.bottomOpciones.isVisible = false
                    binding.recicleProductos.isVisible = false

                    archivar_eliminar_publicaciones_list(
                        binding.idsObtenidos.text.toString() as List<String>,
                        "publicados",
                        "eliminados", "mascliks"
                    )

                }
                binding.archivarselect.setOnClickListener {
                    Toast.makeText(this, "selecionasr el acrivar desde todos", Toast.LENGTH_SHORT)
                        .show()
                    binding.linealEncontrados.isVisible = true
                    binding.textoDinamicoProgrees.text = "Cambiando publicaciones"
                    binding.linealPublicacionesFiltrados.isVisible = false
                    binding.bottomOpciones.isVisible = false
                    binding.recicleProductos.isVisible = false
                    archivar_eliminar_publicaciones_list(
                        binding.idsObtenidos.text.toString() as List<String>,
                        "publicados",
                        "archivados", "mascliks"
                    )

                }
                binding.ocultarPublicaciones.setOnClickListener {
                    Toast.makeText(this, "selecionasr el acrivar desde todos", Toast.LENGTH_SHORT)
                        .show()
                    binding.linealEncontrados.isVisible = true
                    binding.textoDinamicoProgrees.text = "Cambiando publicaciones"
                    binding.linealPublicacionesFiltrados.isVisible = false
                    binding.bottomOpciones.isVisible = false
                    binding.recicleProductos.isVisible = false
                    archivar_eliminar_publicaciones_list(
                        binding.idsObtenidos.text.toString() as List<String>,
                        "publicados",
                        "privado", "mascliks"
                    )

                }
                binding.soloSeguidoresMov.setOnClickListener {
                    Toast.makeText(this, "selecionasr el acrivar desde todos", Toast.LENGTH_SHORT)
                        .show()
                    binding.linealEncontrados.isVisible = true
                    binding.textoDinamicoProgrees.text = "Cambiando publicaciones"
                    binding.linealPublicacionesFiltrados.isVisible = false
                    binding.bottomOpciones.isVisible = false
                    binding.recicleProductos.isVisible = false
                    archivar_eliminar_publicaciones_list(
                        binding.idsObtenidos.text.toString() as List<String>,
                        "publicados",
                        "solo_seguidores", "mascliks"
                    )

                }
            }
            if (binding.masVistas.isChecked) {
                vista_previa.isVisible = false
                editar.isVisible = false
                estadisticas.isVisible = false
                eliminar.setOnClickListener {
                    dialog.dismiss()
                    eliminar_publicacion_Archivar(
                        item.id_publicacion.toString(),
                        "publicados",
                        "eliminados", "masvistas"
                    )

                }
                archivar.setOnClickListener {
                    dialog.dismiss()
                    eliminar_publicacion_Archivar(
                        item.id_publicacion.toString(),
                        "publicados",
                        "archivados", "masvistas"
                    )
                }
                privado.setOnClickListener {
                    dialog.dismiss()
                    eliminar_publicacion_Archivar(
                        item.id_publicacion.toString(),
                        "publicados",
                        "privado", "masvistas"
                    )

                }
                solo_seguidores.setOnClickListener {
                    dialog.dismiss()
                    eliminar_publicacion_Archivar(
                        item.id_publicacion.toString(),
                        "publicados",
                        "solo_seguidores", "masvistas"
                    )
                }

            }
            if (binding.masCompartidas.isChecked) {
                vista_previa.isVisible = false
                editar.isVisible = false
                estadisticas.isVisible = false
                eliminar.setOnClickListener {
                    dialog.dismiss()
                    eliminar_publicacion_Archivar(
                        item.id_publicacion.toString(),
                        "publicados",
                        "eliminados", "mascompartidas"
                    )

                }
                archivar.setOnClickListener {
                    dialog.dismiss()
                    eliminar_publicacion_Archivar(
                        item.id_publicacion.toString(),
                        "publicados",
                        "archivados", "mascompartidas"
                    )
                }
                privado.setOnClickListener {
                    dialog.dismiss()
                    eliminar_publicacion_Archivar(
                        item.id_publicacion.toString(),
                        "publicados",
                        "privado", "mascompartidas"
                    )

                }
                solo_seguidores.setOnClickListener {
                    dialog.dismiss()
                    eliminar_publicacion_Archivar(
                        item.id_publicacion.toString(),
                        "publicados",
                        "solo_seguidores", "mascompartidas"
                    )
                }
            }

            if (binding.todos.isChecked) {
                binding.archivarselect.isVisible = true
                binding.eliminarselect.isVisible = true
                binding.reactivar.isVisible = false
                binding.soloSeguidoresMov.isVisible = true
                binding.ocultarPublicaciones.isVisible = true
                adapter.cancelarModoSeleccion()
                binding.eliminarselect.setOnClickListener {
                    ejecutarCambioDePublicaciones("eliminados", "todos")
                }

                binding.archivarselect.setOnClickListener {
                    ejecutarCambioDePublicaciones("archivados", "todos")
                }

                binding.ocultarPublicaciones.setOnClickListener {
                    ejecutarCambioDePublicaciones("privado", "todos")
                }

                binding.soloSeguidoresMov.setOnClickListener {
                    ejecutarCambioDePublicaciones("solo_seguidores", "todos")
                }
                vista_previa.setOnClickListener {
                    dialog.dismiss()
                    vista_previa_publicaciones(item.id_publicacion.toString(), "publicados")
                }
                eliminar.setOnClickListener {
                    dialog.dismiss()
                    eliminar_publicacion_Archivar(
                        item.id_publicacion.toString(),
                        "publicados",
                        "eliminados", "todos"
                    )

                }
                archivar.setOnClickListener {
                    dialog.dismiss()
                    eliminar_publicacion_Archivar(
                        item.id_publicacion.toString(),
                        "publicados",
                        "archivados", "todos"
                    )
                }
                privado.setOnClickListener {
                    dialog.dismiss()
                    eliminar_publicacion_Archivar(
                        item.id_publicacion.toString(),
                        "publicados",
                        "privado", "todos"
                    )

                }
                solo_seguidores.setOnClickListener {
                    dialog.dismiss()
                    eliminar_publicacion_Archivar(
                        item.id_publicacion.toString(),
                        "publicados",
                        "solo_seguidores", "todos"
                    )
                }
                editar.setOnClickListener {
                    dialog = BottomSheetDialog(this)
                    bottomSheet_editar_campos(
                        item.id_publicacion.toString(),
                        "publicados",
                        "publicados"
                    )
                    dialog.show()
                }
            }
            if (binding.privado.isChecked) {
                bottoSheet.privado.isVisible = false
                bottoSheet.soloSeguidores.isVisible = true
                bottoSheet.regresar.isVisible = true
                bottoSheet.regresar.setOnClickListener {
                    binding.linealEncontrados.isVisible = true
                    activar_publicacion("privado", item.id_publicacion.toString(), "privado")
                    binding.recicleProductos.isVisible = false
                    dialog.dismiss()
                }
                vista_previa.setOnClickListener {
                    dialog.dismiss()
                    vista_previa_publicaciones(item.id_publicacion.toString(), "privado")
                }
                eliminar.setOnClickListener {
                    dialog.dismiss()
                    dialog.dismiss()
                    eliminar_publicacion_Archivar(
                        item.id_publicacion.toString(),
                        "privado",
                        "eliminados", "privado"
                    )
                }
                archivar.setOnClickListener {
                    dialog.dismiss()
                    eliminar_publicacion_Archivar(
                        item.id_publicacion.toString(),
                        "privado",
                        "archivados", "privado"
                    )
                }
                solo_seguidores.setOnClickListener {
                    dialog.dismiss()
                    eliminar_publicacion_Archivar(
                        item.id_publicacion.toString(),
                        "privado",
                        "solo_seguidores", "privado"
                    )

                }
                editar.setOnClickListener {
                    dialog = BottomSheetDialog(this)
                    bottomSheet_editar_campos(item.id_publicacion.toString(), "privado", "privado")
                    dialog.show()
                }
            }
            if (binding.soloSeguidores.isChecked) {
                bottoSheet.privado.isVisible = true
                bottoSheet.soloSeguidores.isVisible = false
                bottoSheet.regresar.isVisible = true
                bottoSheet.regresar.setOnClickListener {
                    binding.linealEncontrados.isVisible = true
                    activar_publicacion(
                        "solo_seguidores",
                        item.id_publicacion.toString(),
                        "solo_seguidores"
                    )
                    binding.recicleProductos.isVisible = false
                    dialog.dismiss()
                }
                eliminar.setOnClickListener {
                    dialog.dismiss()
                    eliminar_publicacion_Archivar(
                        item.id_publicacion.toString(),
                        "solo_seguidores",
                        "eliminados", "solo_seguidores"
                    )
                }
                archivar.setOnClickListener {
                    dialog.dismiss()
                    eliminar_publicacion_Archivar(
                        item.id_publicacion.toString(),
                        "solo_seguidores",
                        "archivados", "solo_seguidores"
                    )
                }
                privado.setOnClickListener {
                    dialog.dismiss()
                    eliminar_publicacion_Archivar(
                        item.id_publicacion.toString(),
                        "solo_seguidores",
                        "privado", "solo_seguidores"
                    )

                }
                vista_previa.setOnClickListener {
                    dialog.dismiss()
                    vista_previa_publicaciones(item.id_publicacion.toString(), "solo_seguidores")
                }
                editar.setOnClickListener {
                    dialog = BottomSheetDialog(this)
                    bottomSheet_editar_campos(
                        item.id_publicacion.toString(),
                        "solo_seguidores",
                        "solo_seguidores"
                    )
                    dialog.show()
                }
            }
        } else if (dato_pasado.equals("archivadas")) {
            bottoSheet.regresar.isVisible = true
            bottoSheet.eliminarPermanente.isVisible = false
            bottoSheet.linealIconosPrincipal.isVisible = false
            privado.isVisible = false
            solo_seguidores.isVisible = false
            bottoSheet.regresar.setOnClickListener {
                binding.linealEncontrados.isVisible = true
                binding.textoDinamicoProgrees.text = "Actualizando contenido..."
                activar_publicacion("archivados", item.id_publicacion.toString(), "archivados")
                binding.recicleProductos.isVisible = false
                dialog.dismiss()
            }

        } else if (dato_pasado.equals("eliminadas")) {
            bottoSheet.regresar.isVisible = true
            privado.isVisible = false
            solo_seguidores.isVisible = false
            bottoSheet.eliminarPermanente.isVisible = true
            bottoSheet.linealIconosPrincipal.isVisible = false
            bottoSheet.regresar.setOnClickListener {
                binding.linealEncontrados.isVisible = true
                binding.textoDinamicoProgrees.text = "Actualizando contenido..."
                activar_publicacion("eliminados", item.id_publicacion.toString(), "eliminados")
                binding.recicleProductos.isVisible = false
                dialog.dismiss()
            }


        }


        bottoSheet.eliminarPermanente.setOnClickListener {
            binding.linealEncontrados.isVisible = true
            binding.textoDinamicoProgrees.text = "Actualizando contenido..."
            binding.recicleProductos.isVisible = false
            dialog.dismiss()
            eliminar_publicacion_permanente(item)
            dialog.dismiss()
        }

        dialog.setContentView(view)
    }

    private fun eliminar_publicacion_permanente(item: dataclas_trabajos_ralizados_verificados) {
        val firestore = FirebaseFirestore.getInstance()
        val uid = firebaseAuth.uid.toString()
        val refOrigen = firestore.collection("Trabajadores_Usuarios_Drivers")
            .document("trabajadores").collection("trabajadores")
            .document(uid).collection("productos_venta")
            .document("eliminados").collection("eliminados")
            .document(item.id_publicacion.toString())
        refOrigen.get().addOnSuccessListener { res ->
            if (res.exists()) {
                val data = res.data
                val metodoEntrega = data?.get("metodoEntrega") as? String ?: ""
                val metodoPago = data?.get("metodoPago") as? String ?: ""
                eliminar_metodo_pago_entrega_id(
                    "metodos_entrega",
                    metodoEntrega,
                    item.id_publicacion.toString()
                )
                eliminar_metodo_pago_entrega_id(
                    "metodos_pago",
                    metodoPago,
                    item.id_publicacion.toString()
                )
            }
            refOrigen.delete().addOnSuccessListener { res ->
                obtener_productos("eliminados", "No se encontraron datos")
                Toast.makeText(this, "publicacion eliminado correctamente", Toast.LENGTH_SHORT)
                    .show()

                binding.linealEncontrados.isVisible = false
            }.addOnFailureListener { e ->
                Toast.makeText(this, "Error al eliminar la publicacion", Toast.LENGTH_SHORT)
                    .show()
            }
        }.addOnFailureListener { e ->

        }

    }

    private fun eliminar_metodo_pago_entrega_id(
        metodo_pago_entrega: String,
        id_metodo_pago_entrega: String,
        id_publicacion_params: String
    ) {
        val firestore = FirebaseFirestore.getInstance()
        val uid = firebaseAuth.uid.toString()
        val ref = firestore.collection("Trabajadores_Usuarios_Drivers")
            .document("trabajadores").collection("trabajadores")
            .document(uid).collection(metodo_pago_entrega)
            .document(id_metodo_pago_entrega)

        ref.get().addOnSuccessListener { documentSnapshot ->
            if (documentSnapshot.exists()) {
                val publicacionesActivas =
                    documentSnapshot.get("publicaciones_activas") as? Map<*, *>
                if (publicacionesActivas != null) {

                    var eliminado = false

                    publicacionesActivas.forEach { entry ->
                        val mapa = entry.value as? Map<*, *>
                        val idPublicacion = mapa?.get("id_publicacion") as? String

                        if (idPublicacion != null) {
                            Log.d("ID_PUBLICACION_ENCONTRADO", "id_publicacion: $idPublicacion")

                            if (idPublicacion == id_publicacion_params) {
                                val path = "publicaciones_activas.${entry.key}"
                                ref.update(path, FieldValue.delete())
                                    .addOnSuccessListener {
                                        Toast.makeText(
                                            this,
                                            "Publicación eliminada con éxito",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                        Log.d("FirestoreUpdate", "Eliminado $path")
                                    }
                                    .addOnFailureListener { e ->
                                        Toast.makeText(
                                            this,
                                            "Error al eliminar: ${e.message}",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                        Log.e(
                                            "FirestoreError",
                                            "Error al eliminar $path: ${e.message}"
                                        )
                                    }
                                eliminado = true
                                return@forEach // salir del bucle después de eliminar
                            }
                        }
                    }

                    if (!eliminado) {
                        Toast.makeText(this, "No se encontró esa publicación", Toast.LENGTH_SHORT)
                            .show()
                    }

                } else {
                    Toast.makeText(this, "No hay publicaciones activas", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "No existe el documento del trabajador", Toast.LENGTH_SHORT)
                    .show()
            }
        }.addOnFailureListener {
            Toast.makeText(this, "Error: ${it.message}", Toast.LENGTH_SHORT).show()
        }
    }


    private fun activar_publicacion(tipo: String, idPublicacion: String, tipo_fun: String) {
        val firestore = FirebaseFirestore.getInstance()
        val uid = firebaseAuth.uid.toString()
        val refOrigen = firestore.collection("Trabajadores_Usuarios_Drivers")
            .document("trabajadores").collection("trabajadores")
            .document(uid).collection("productos_venta")
            .document(tipo).collection(tipo).document(idPublicacion)

        refOrigen.get().addOnSuccessListener { res ->
            if (res.exists()) {
                val data = res.data ?: return@addOnSuccessListener

                val titulo = data?.get("titulo") as? String ?: ""
                val cantidadPorcentajeDescuento =
                    (data?.get("cantidad_porcentaje_descuento") as? Number)?.toInt() ?: 0
                val condicionProducto = data?.get("condicion_producto") as? String ?: ""
                val categoriaProducto = data?.get("categoria_producto") as? String ?: ""
                val subcategoriaProducto = data?.get("subcategori_producto") as? String ?: ""
                val fechaPublicada = data?.get("fechaPublicada") as? String ?: ""
                val horaPublicada = data?.get("horaPublicada") as? String ?: ""
                val garantia = data?.get("garantia") as? String ?: ""
                val localidadUser = data?.get("localidadUser") as? String ?: ""
                val marca = data?.get("marca") as? String ?: ""
                val metodoEntrega = data?.get("metodoEntrega") as? String ?: ""
                val metodoPago = data?.get("metodoPago") as? String ?: ""
                val modelo = data?.get("modelo") as? String ?: ""
                val nombre = data?.get("nombre") as? String ?: ""
                val precio = (data?.get("precio") as? Number)?.toDouble() ?: 0.0
                val precioDescuento = (data?.get("precio_descuento") as? Number)?.toDouble() ?: 0.0
                val stok = data?.get("stok") as? String ?: ""
                val visibilidad = data?.get("visivilidad") as? String ?: ""
                val masInformacion = data?.get("mas_informacio") as? String ?: ""

                val hashtagsGenerales =
                    data?.get("hashtags_generales") as? List<String> ?: emptyList()

                val descripcionTitulo = data?.get("descripcion_titulo") as? Map<String, String>
                val tituloDescripcion = descripcionTitulo?.get("titulo_descripcion") ?: ""
                val tituloMayus = descripcionTitulo?.get("titulo_mayus") ?: ""
                val tituloValorStyle = descripcionTitulo?.get("titulo_valor_style") ?: ""

                val descripcionTexto = data?.get("descripcion_texto") as? Map<String, String>
                val textoDescripcion = descripcionTexto?.get("texto_descripcion") ?: ""
                val textoMayus = descripcionTexto?.get("texto_mayus") ?: ""
                val textoValorStyle = descripcionTexto?.get("texto_valor_style") ?: ""
                val estadisticas_click = data?.get("estadisticas_click") as? Number ?: 0
                val estadisticas_compartir = data?.get("estadisticas_compartir") as? Number ?: 0
                val estadisticas_vistas = data?.get("estadisticas_vistas") as? Number ?: 0

                val descripcionTextoLista =
                    data?.get("descripcion_texto_lista") as? List<String> ?: emptyList()

                // Mapas a guardar
                val tituloMap = mapOf(
                    "titulo_descripcion" to tituloDescripcion,
                    "titulo_valor_style" to tituloValorStyle,
                    "titulo_mayus" to tituloMayus
                )
                val texto_map = mapOf(
                    "texto_descripcion" to textoDescripcion,
                    "texto_valor_style" to textoValorStyle,
                    "texto_mayus" to textoMayus
                )

                val hasMap = hashMapOf<String, Any>(
                    "id" to idPublicacion,
                    "titulo" to titulo,
                    "cantidad_porcentaje_descuento" to cantidadPorcentajeDescuento,
                    "condicion_producto" to condicionProducto,
                    "categoria_producto" to categoriaProducto,
                    "subcategori_producto" to subcategoriaProducto,
                    "fechaPublicada" to fechaPublicada,
                    "horaPublicada" to horaPublicada,
                    "garantia" to garantia,
                    "localidadUser" to localidadUser,
                    "marca" to marca,
                    "metodoEntrega" to metodoEntrega,
                    "metodoPago" to metodoPago,
                    "modelo" to modelo,
                    "hashtags_generales" to hashtagsGenerales,
                    "nombre" to nombre,
                    "precio" to precio,
                    "precioDelivery" to 5,
                    "precio_descuento" to precioDescuento,
                    "stok" to stok,
                    "visivilidad" to visibilidad,
                    "descripcion_titulo" to tituloMap,
                    "descripcion_texto" to texto_map,
                    "descripcion_texto_lista" to descripcionTextoLista,
                    "mas_informacio" to masInformacion,
                    "estadisticas_click" to estadisticas_click,
                    "estadisticas_compartir" to estadisticas_compartir,
                    "estadisticas_vistas" to estadisticas_vistas,
                )
                for ((key, value) in data) {
                    if (key.startsWith("img_url") && value is String) {
                        hasMap[key] = value
                    }
                }
                val refDestino = firestore.collection("Trabajadores_Usuarios_Drivers")
                    .document("trabajadores").collection("trabajadores")
                    .document(uid).collection("productos_venta")
                    .document("publicados").collection("publicados").document(idPublicacion)
                refDestino.set(hasMap, SetOptions.merge()).addOnSuccessListener { res ->
                    Toast.makeText(this, "agregeado correctamente a publicados", Toast.LENGTH_SHORT)
                        .show()
                    cambiar_datos_archivar_eliminar_ocultar_archivar(
                        idPublicacion,
                        "publicados",
                        metodoEntrega,
                        "metodos_entrega"
                    )
                    cambiar_datos_archivar_eliminar_ocultar_archivar(
                        idPublicacion,
                        "publicados",
                        metodoPago,
                        "metodos_pago"
                    )
                    refOrigen.delete().addOnSuccessListener { res ->
                        binding.linealEncontrados.isVisible = false
                        binding.recicleProductos.isVisible = true
                        when (tipo_fun) {
                            "privado" -> {
                                obtener_productos("privado", "No se encontraron publicaciones")

                            }

                            "solo_seguidores" -> {
                                obtener_productos(
                                    "solo_seguidores",
                                    "No se encontraron publicaciones"
                                )

                            }

                            "archivados" -> {
                                obtener_productos("archivados", "No se encontraron publicaciones")

                            }

                            "eliminados" -> {
                                obtener_productos("eliminados", "No se encontraron publicaciones")

                            }
                        }
                        Toast.makeText(this, "eliminado de publicados", Toast.LENGTH_SHORT).show()
                    }.addOnFailureListener { e ->
                        Toast.makeText(this, "Error al eliminar", Toast.LENGTH_SHORT).show()
                    }
                }.addOnFailureListener { e ->
                    Toast.makeText(this, "error al agregar a elimiandos $e", Toast.LENGTH_SHORT)
                        .show()
                }
            } else {
                Toast.makeText(this, "no existe ", Toast.LENGTH_SHORT).show()

            }

        }
    }

    private fun activar_publicacion_list(
        tipo: String,
        idsPublicaciones: List<String>,
        tipo_fun: String
    ) {
        val firestore = FirebaseFirestore.getInstance()
        val uid = firebaseAuth.uid.toString()

        binding.linealEncontrados.isVisible = true
        binding.recicleProductos.isVisible = false

        var procesadas = 0
        fun checkCompletadas() {
            procesadas++
            if (procesadas == idsPublicaciones.size) {
                binding.linealEncontrados.isVisible = false
                binding.recicleProductos.isVisible = true
                when (tipo_fun) {
                    "privado" -> obtener_productos("privado", "No se encontraron publicaciones")
                    "solo_seguidores" -> obtener_productos(
                        "solo_seguidores",
                        "No se encontraron publicaciones"
                    )

                    "archivados" -> obtener_productos(
                        "archivados",
                        "No se encontraron publicaciones"
                    )

                    "eliminados" -> obtener_productos(
                        "eliminados",
                        "No se encontraron publicaciones"
                    )
                }
            }
        }

        idsPublicaciones.forEach { idPublicacion ->
            val refOrigen = firestore.collection("Trabajadores_Usuarios_Drivers")
                .document("trabajadores").collection("trabajadores")
                .document(uid).collection("productos_venta")
                .document(tipo).collection(tipo).document(idPublicacion)

            refOrigen.get().addOnSuccessListener { res ->
                if (res.exists()) {
                    val data = res.data ?: return@addOnSuccessListener

                    val metodoEntrega = data["metodoEntrega"] as? String ?: ""
                    val metodoPago = data["metodoPago"] as? String ?: ""

                    val hasMap = hashMapOf<String, Any>(
                        "id" to idPublicacion,
                        "titulo" to (data["titulo"] ?: ""),
                        "cantidad_porcentaje_descuento" to (data["cantidad_porcentaje_descuento"]
                            ?: 0),
                        "condicion_producto" to (data["condicion_producto"] ?: ""),
                        "categoria_producto" to (data["categoria_producto"] ?: ""),
                        "subcategori_producto" to (data["subcategori_producto"] ?: ""),
                        "fechaPublicada" to (data["fechaPublicada"] ?: ""),
                        "horaPublicada" to (data["horaPublicada"] ?: ""),
                        "garantia" to (data["garantia"] ?: ""),
                        "localidadUser" to (data["localidadUser"] ?: ""),
                        "marca" to (data["marca"] ?: ""),
                        "metodoEntrega" to metodoEntrega,
                        "metodoPago" to metodoPago,
                        "modelo" to (data["modelo"] ?: ""),
                        "hashtags_generales" to (data["hashtags_generales"] as? List<String>
                            ?: emptyList()),
                        "nombre" to (data["nombre"] ?: ""),
                        "precio" to (data["precio"] ?: 0.0),
                        "precioDelivery" to 5,
                        "precio_descuento" to (data["precio_descuento"] ?: 0.0),
                        "stok" to (data["stok"] ?: ""),
                        "visivilidad" to (data["visivilidad"] ?: ""),
                        "mas_informacio" to (data["mas_informacio"] ?: ""),
                        "estadisticas_click" to (data["estadisticas_click"] ?: 0),
                        "estadisticas_compartir" to (data["estadisticas_compartir"] ?: 0),
                        "estadisticas_vistas" to (data["estadisticas_vistas"] ?: 0),
                        "descripcion_titulo" to (data["descripcion_titulo"] as? Map<*, *>
                            ?: emptyMap<String, String>()),
                        "descripcion_texto" to (data["descripcion_texto"] as? Map<*, *>
                            ?: emptyMap<String, String>()),
                        "descripcion_texto_lista" to (data["descripcion_texto_lista"] as? List<String>
                            ?: emptyList())
                    )

                    for ((key, value) in data) {
                        if (key.startsWith("img_url") && value is String) {
                            hasMap[key] = value
                        }
                    }

                    val refDestino = firestore.collection("Trabajadores_Usuarios_Drivers")
                        .document("trabajadores").collection("trabajadores")
                        .document(uid).collection("productos_venta")
                        .document("publicados").collection("publicados").document(idPublicacion)

                    refDestino.set(hasMap, SetOptions.merge()).addOnSuccessListener {
                        Toast.makeText(
                            this,
                            "Agregado correctamente a publicados",
                            Toast.LENGTH_SHORT
                        ).show()

                        cambiar_datos_archivar_eliminar_ocultar_archivar(
                            idPublicacion,
                            "publicados",
                            metodoEntrega,
                            "metodos_entrega"
                        )
                        cambiar_datos_archivar_eliminar_ocultar_archivar(
                            idPublicacion,
                            "publicados",
                            metodoPago,
                            "metodos_pago"
                        )

                        refOrigen.delete().addOnSuccessListener {
                            Toast.makeText(this, "Eliminado de $tipo", Toast.LENGTH_SHORT).show()
                            checkCompletadas()
                        }.addOnFailureListener {
                            Toast.makeText(this, "Error al eliminar de $tipo", Toast.LENGTH_SHORT)
                                .show()
                            checkCompletadas()
                        }
                    }.addOnFailureListener {
                        Toast.makeText(
                            this,
                            "Error al agregar a publicados: ${it.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                        checkCompletadas()
                    }

                } else {
                    Toast.makeText(
                        this,
                        "No existe publicación con ID: $idPublicacion",
                        Toast.LENGTH_SHORT
                    ).show()
                    checkCompletadas()
                }
            }.addOnFailureListener {
                Toast.makeText(
                    this,
                    "Error al obtener publicación: ${it.message}",
                    Toast.LENGTH_SHORT
                ).show()
                checkCompletadas()
            }
        }
    }

    private fun vista_previa_publicaciones(id_publicacion: String, tipo1: String) {
        Log.d("obtenemos_datos intent", "obtenemos datos $id_publicacion")
        val vista =
            Intent(this, vista_ver_productos_trabajadores::class.java).apply {
                putExtra("id_trabajador", firebaseAuth.uid.toString())
                    .putExtra("id_publicacion", id_publicacion)
                    .putExtra("tipo_ubicado", tipo1)
            }
        startActivity(vista)
    }


    private fun archivar_eliminar_publicaciones_list(
        idsPublicaciones: List<String>,
        tipo1: String,
        tipo2: String,
        funcionFiltro: String
    ) {
        val firestore = FirebaseFirestore.getInstance()
        val uid = firebaseAuth.uid.toString()

        binding.linealEncontrados.isVisible = true
        binding.recicleProductos.isVisible = false

        var procesadas = 0
        fun checkCompletadas() {
            procesadas++
            if (procesadas == idsPublicaciones.size) {
                binding.linealEncontrados.isVisible = false
                binding.recicleProductos.isVisible = true
                lista.clear()
                when (funcionFiltro) {
                    "todos" -> obtener_productos("publicados", "No se encontraron datos")
                    "mascliks" -> filtrar_publicaciones(
                        binding.min.text.toString().toInt(),
                        binding.max.text.toString().toInt(),
                        "estadisticas_click"
                    )

                    "masvistas" -> filtrar_publicaciones(
                        binding.min.text.toString().toInt(),
                        binding.max.text.toString().toInt(),
                        "estadisticas_vistas"
                    )

                    "mascompartidas" -> filtrar_publicaciones(
                        binding.min.text.toString().toInt(),
                        binding.max.text.toString().toInt(),
                        "estadisticas_compartir"
                    )

                    "privado" -> obtener_productos("privado", "No se encontraron datos")
                    "solo_seguidores" -> obtener_productos(
                        "solo_seguidores",
                        "No se encontraron datos"
                    )
                }
            }
        }

        idsPublicaciones.forEach { id_publicacion ->
            val refEliminado = firestore.collection("Trabajadores_Usuarios_Drivers")
                .document("trabajadores").collection("trabajadores")
                .document(uid).collection("productos_venta")
                .document(tipo2).collection(tipo2).document(id_publicacion)

            val ref_puplicados = firestore.collection("Trabajadores_Usuarios_Drivers")
                .document("trabajadores").collection("trabajadores")
                .document(uid).collection("productos_venta")
                .document(tipo1).collection(tipo1).document(id_publicacion)

            ref_puplicados.get().addOnSuccessListener { res ->
                if (res.exists()) {
                    val data = res.data ?: return@addOnSuccessListener

                    val metodoEntrega = data["metodoEntrega"] as? String ?: ""
                    val metodoPago = data["metodoPago"] as? String ?: ""

                    val hasMap = hashMapOf<String, Any>(
                        "id" to (data["id"] ?: id_publicacion),
                        "titulo" to (data["titulo"] ?: ""),
                        "cantidad_porcentaje_descuento" to (data["cantidad_porcentaje_descuento"]
                            ?: 0),
                        "condicion_producto" to (data["condicion_producto"] ?: ""),
                        "categoria_producto" to (data["categoria_producto"] ?: ""),
                        "subcategori_producto" to (data["subcategori_producto"] ?: ""),
                        "fechaPublicada" to (data["fechaPublicada"] ?: ""),
                        "horaPublicada" to (data["horaPublicada"] ?: ""),
                        "garantia" to (data["garantia"] ?: ""),
                        "localidadUser" to (data["localidadUser"] ?: ""),
                        "marca" to (data["marca"] ?: ""),
                        "metodoEntrega" to metodoEntrega,
                        "metodoPago" to metodoPago,
                        "modelo" to (data["modelo"] ?: ""),
                        "hashtags_generales" to (data["hashtags_generales"] as? List<String>
                            ?: emptyList()),
                        "nombre" to (data["nombre"] ?: ""),
                        "precio" to (data["precio"] ?: 0.0),
                        "precioDelivery" to 5,
                        "precio_descuento" to (data["precio_descuento"] ?: 0.0),
                        "stok" to (data["stok"] ?: ""),
                        "visivilidad" to (data["visivilidad"] ?: ""),
                        "mas_informacio" to (data["mas_informacio"] ?: ""),
                        "estadisticas_click" to (data["estadisticas_click"] ?: 0),
                        "estadisticas_compartir" to (data["estadisticas_compartir"] ?: 0),
                        "estadisticas_vistas" to (data["estadisticas_vistas"] ?: 0),
                        "descripcion_titulo" to (data["descripcion_titulo"] as? Map<*, *>
                            ?: emptyMap<String, String>()),
                        "descripcion_texto" to (data["descripcion_texto"] as? Map<*, *>
                            ?: emptyMap<String, String>()),
                        "descripcion_texto_lista" to (data["descripcion_texto_lista"] as? List<String>
                            ?: emptyList())
                    )

                    for ((key, value) in data) {
                        if (key.startsWith("img_url") && value is String) {
                            hasMap[key] = value
                        }
                    }

                    refEliminado.set(hasMap, SetOptions.merge()).addOnSuccessListener {
                        Toast.makeText(
                            this,
                            "agregado correctamente a eliminados",
                            Toast.LENGTH_SHORT
                        ).show()

                        cambiar_datos_archivar_eliminar_ocultar_archivar(
                            id_publicacion, tipo2, metodoEntrega, "metodos_entrega"
                        )

                        cambiar_datos_archivar_eliminar_ocultar_archivar(
                            id_publicacion, tipo2, metodoPago, "metodos_pago"
                        )

                        ref_puplicados.delete().addOnSuccessListener {
                            Toast.makeText(this, "eliminado de publicados", Toast.LENGTH_SHORT)
                                .show()
                            checkCompletadas()
                        }.addOnFailureListener {
                            Toast.makeText(this, "Error al eliminar", Toast.LENGTH_SHORT).show()
                            checkCompletadas()
                        }
                    }.addOnFailureListener {
                        Toast.makeText(
                            this,
                            "error al agregar a eliminados: ${it.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                        checkCompletadas()
                    }
                } else {
                    Toast.makeText(this, "no existe", Toast.LENGTH_SHORT).show()
                    checkCompletadas()
                }
            }.addOnFailureListener {
                Toast.makeText(this, "Error al obtener publicación", Toast.LENGTH_SHORT).show()
                checkCompletadas()
            }
        }
    }


    private fun eliminar_publicacion_Archivar(
        id_selecionado: String,
        tipo1: String,
        tipo2: String,
        funcion_carga: String
    ) {
        binding.linealEncontrados.isVisible = true
        binding.recicleProductos.isVisible = false
        val refEliminado =
            FirebaseFirestore.getInstance().collection("Trabajadores_Usuarios_Drivers")
                .document("trabajadores").collection("trabajadores")
                .document(firebaseAuth.uid.toString()).collection("productos_venta")
                .document(tipo2).collection(tipo2).document(id_selecionado)
        val ref_puplicados =
            FirebaseFirestore.getInstance().collection("Trabajadores_Usuarios_Drivers")
                .document("trabajadores").collection("trabajadores")
                .document(firebaseAuth.uid.toString()).collection("productos_venta")
                .document(tipo1).collection(tipo1).document(id_selecionado)

        ref_puplicados.get().addOnSuccessListener { res ->
            if (res.exists()) {
                val data = res.data ?: return@addOnSuccessListener

                val titulo = data?.get("titulo") as? String ?: ""
                val cantidadPorcentajeDescuento =
                    (data?.get("cantidad_porcentaje_descuento") as? Number)?.toInt() ?: 0
                val condicionProducto = data?.get("condicion_producto") as? String ?: ""
                val categoriaProducto = data?.get("categoria_producto") as? String ?: ""
                val subcategoriaProducto = data?.get("subcategori_producto") as? String ?: ""
                val fechaPublicada = data?.get("fechaPublicada") as? String ?: ""
                val horaPublicada = data?.get("horaPublicada") as? String ?: ""
                val garantia = data?.get("garantia") as? String ?: ""
                val localidadUser = data?.get("localidadUser") as? String ?: ""
                val marca = data?.get("marca") as? String ?: ""
                val metodoEntrega = data?.get("metodoEntrega") as? String ?: ""
                val metodoPago = data?.get("metodoPago") as? String ?: ""
                val modelo = data?.get("modelo") as? String ?: ""
                val nombre = data?.get("nombre") as? String ?: ""
                val precio = (data?.get("precio") as? Number)?.toDouble() ?: 0.0
                val precioDescuento = (data?.get("precio_descuento") as? Number)?.toDouble() ?: 0.0
                val stok = data?.get("stok") as? String ?: ""
                val visibilidad = data?.get("visivilidad") as? String ?: ""
                val masInformacion = data?.get("mas_informacio") as? String ?: ""

                val hashtagsGenerales =
                    data?.get("hashtags_generales") as? List<String> ?: emptyList()

                val descripcionTitulo = data?.get("descripcion_titulo") as? Map<String, String>
                val tituloDescripcion = descripcionTitulo?.get("titulo_descripcion") ?: ""
                val tituloMayus = descripcionTitulo?.get("titulo_mayus") ?: ""
                val tituloValorStyle = descripcionTitulo?.get("titulo_valor_style") ?: ""

                val descripcionTexto = data?.get("descripcion_texto") as? Map<String, String>
                val textoDescripcion = descripcionTexto?.get("texto_descripcion") ?: ""
                val textoMayus = descripcionTexto?.get("texto_mayus") ?: ""
                val textoValorStyle = descripcionTexto?.get("texto_valor_style") ?: ""
                val estadisticas_click = data?.get("estadisticas_click") as? Number ?: 0
                val estadisticas_compartir = data?.get("estadisticas_compartir") as? Number ?: 0
                val estadisticas_vistas = data?.get("estadisticas_vistas") as? Number ?: 0

                val descripcionTextoLista =
                    data?.get("descripcion_texto_lista") as? List<String> ?: emptyList()

                // Mapas a guardar
                val tituloMap = mapOf(
                    "titulo_descripcion" to tituloDescripcion,
                    "titulo_valor_style" to tituloValorStyle,
                    "titulo_mayus" to tituloMayus
                )
                val texto_map = mapOf(
                    "texto_descripcion" to textoDescripcion,
                    "texto_valor_style" to textoValorStyle,
                    "texto_mayus" to textoMayus
                )

                val hasMap = hashMapOf<String, Any>(
                    "id" to id_selecionado,
                    "titulo" to titulo,
                    "cantidad_porcentaje_descuento" to cantidadPorcentajeDescuento,
                    "condicion_producto" to condicionProducto,
                    "categoria_producto" to categoriaProducto,
                    "subcategori_producto" to subcategoriaProducto,
                    "fechaPublicada" to fechaPublicada,
                    "horaPublicada" to horaPublicada,
                    "garantia" to garantia,
                    "localidadUser" to localidadUser,
                    "marca" to marca,
                    "metodoEntrega" to metodoEntrega,
                    "metodoPago" to metodoPago,
                    "modelo" to modelo,
                    "hashtags_generales" to hashtagsGenerales,
                    "nombre" to nombre,
                    "precio" to precio,
                    "precioDelivery" to 5,
                    "precio_descuento" to precioDescuento,
                    "stok" to stok,
                    "visivilidad" to visibilidad,
                    "descripcion_titulo" to tituloMap,
                    "descripcion_texto" to texto_map,
                    "descripcion_texto_lista" to descripcionTextoLista,
                    "mas_informacio" to masInformacion,
                    "estadisticas_click" to estadisticas_click,
                    "estadisticas_compartir" to estadisticas_compartir,
                    "estadisticas_vistas" to estadisticas_vistas,
                )

                for ((key, value) in data) {
                    if (key.startsWith("img_url") && value is String) {
                        hasMap[key] = value
                    }
                }
                refEliminado.set(hasMap, SetOptions.merge()).addOnSuccessListener { res ->
                    Toast.makeText(this, "agregeado correctamente a eliminados", Toast.LENGTH_SHORT)
                        .show()
                    cambiar_datos_archivar_eliminar_ocultar_archivar(
                        id_selecionado,
                        tipo2,
                        metodoEntrega,
                        "metodos_entrega"
                    )
                    cambiar_datos_archivar_eliminar_ocultar_archivar(
                        id_selecionado,
                        tipo2,
                        metodoPago,
                        "metodos_pago"
                    )

                    binding.linealEncontrados.isVisible = false
                    binding.recicleProductos.isVisible = true
                    when (funcion_carga) {
                        "todos" -> {
                            obtener_productos(
                                "publicados", "No se encontraron datos"
                            )
                        }

                        "mascliks" -> {
                            filtrar_publicaciones(
                                binding.min.text.toString().toInt(),
                                binding.max.text.toString().toInt(),
                                "estadisticas_click"
                            )
                        }

                        "masvistas" -> {
                            filtrar_publicaciones(
                                binding.min.text.toString().toInt(),
                                binding.max.text.toString().toInt(),
                                "estadisticas_vistas"
                            )
                        }

                        "mascompartidas" -> {
                            filtrar_publicaciones(
                                binding.min.text.toString().toInt(),
                                binding.max.text.toString().toInt(),
                                "estadisticas_compartir"
                            )
                        }

                        "privado" -> {
                            obtener_productos(
                                "privado", "No se encontraron datos"
                            )
                        }

                        "solo_seguidores" -> {
                            obtener_productos(
                                "solo_seguidores", "No se encontraron datos"
                            )
                        }
                    }
                    ref_puplicados.delete().addOnSuccessListener { res ->
                        Toast.makeText(this, "eliminado de publicados", Toast.LENGTH_SHORT).show()
                    }.addOnFailureListener { e ->
                        Toast.makeText(this, "Error al eliminar", Toast.LENGTH_SHORT).show()
                    }
                }.addOnFailureListener { e ->
                    Toast.makeText(this, "error al agregar a elimiandos $e", Toast.LENGTH_SHORT)
                        .show()
                }
            } else {
                Toast.makeText(this, "no existe ", Toast.LENGTH_SHORT).show()

            }

        }

    }

    private fun cambiar_datos_archivar_eliminar_ocultar_archivar(
        idPublicacion: String,
        cambiado: String,
        id_metodo_pago_entrega: String,
        metodo_pago_o_entrega: String
    ) {
        Log.d("obtenosm_datos", "$cambiado $id_metodo_pago_entrega $metodo_pago_o_entrega")
        val trabajadorRef = FirebaseFirestore.getInstance()
            .collection("Trabajadores_Usuarios_Drivers")
            .document("trabajadores")
            .collection("trabajadores")
            .document(FirebaseAuth.getInstance().uid.toString())
            .collection(metodo_pago_o_entrega)
            .document(id_metodo_pago_entrega)

        trabajadorRef.get().addOnSuccessListener { documentSnapshot ->
            if (documentSnapshot.exists()) {
                val publicacionesActivas =
                    documentSnapshot.get("publicaciones_activas") as? Map<*, *>

                val datos = publicacionesActivas?.get(idPublicacion) as? Map<*, *>
                if (datos != null) {
                    val updates = mutableMapOf<String, Any>()

                    // Campos booleanos posibles
                    val campos = listOf(
                        "activo",
                        "archivados",
                        "eliminados",
                        "privado",
                        "productos_publicaciones",
                        "publicados",
                        "solo_seguidores"
                    )

                    for (campo in campos) {
                        val path = "publicaciones_activas.$idPublicacion.$campo"

                        when {
                            campo == cambiado -> updates[path] = true
                            cambiado == "publicados" && (campo == "activo" || campo == "productos_publicaciones") ->
                                updates[path] = true

                            else -> updates[path] = false
                        }
                    }

                    trabajadorRef.update(updates)
                        .addOnSuccessListener {
                            Log.d("FirestoreUpdate", "Actualización exitosa para $idPublicacion")
                        }
                        .addOnFailureListener { e ->
                            Log.e(
                                "FirestoreError",
                                "Error al actualizar $idPublicacion: ${e.message}"
                            )
                        }
                } else {
                    Log.w("FirestoreWarning", "No se encontró la publicación con ID $idPublicacion")
                }
            }
        }.addOnFailureListener { e ->
            Log.e("FirestoreError", "Error al obtener documento: ${e.message}")
        }
    }


    private fun obtener_productos(tipo: String, texto_sin_encontrar: String) {
        binding.linealEncontrados.isVisible = false
        binding.linealPublicacionesFiltrados.isVisible = true
        binding.cerrarselecion.isVisible = false
        binding.linealNoCuenta.isVisible = false
        binding.recicleProductos.isVisible = false
        binding.deslistar.isVisible = false
        binding.listartododos.isVisible = false
        binding.linealNoCuenta.isVisible = false
        adapter.cancelarModoSeleccion()
        val db = FirebaseFirestore.getInstance().collection("Trabajadores_Usuarios_Drivers")
            .document("trabajadores").collection("trabajadores")
            .document(firebaseAuth.uid.toString()).collection("productos_venta").document(tipo)
            .collection(tipo)
        lista.clear()
        db.get().addOnSuccessListener { res ->
            for (datos in res) {
                val data = datos.data
                val img_url = data?.get("img_url") as? String ?: ""
                val titulo = data?.get("titulo") as? String ?: ""
                val contenido = data?.get("contenido") as? String ?: ""
                val hora_rec = data?.get("hora_rec") as? String ?: ""
                val fecha_rec = data?.get("fecha_rec") as? String ?: ""
                val id = data?.get("id") as? String ?: ""
                val vista = data?.get("estadisticas_vistas") as? Number ?: 0
                val compartidas = data?.get("estadisticas_compartir") as? Number ?: 0
                val cliks = data?.get("estadisticas_click") as? Number ?: 0
                val listapublicaciones = dataclas_trabajos_ralizados_verificados(
                    img_url, titulo, contenido, hora_rec, fecha_rec, id, vista, compartidas, cliks
                )
                lista.add(listapublicaciones)
            }
            if (lista.isEmpty()) {
                binding.textoSinEncontrar.text = texto_sin_encontrar
                binding.linealNoCuenta.isVisible = true
                binding.linealEncontrados.isVisible = false
            } else {
                binding.linealNoCuenta.isVisible = false
                binding.recicleProductos.isVisible = true
                binding.linealEncontrados.isVisible = false
                inicializarRecicle(binding.recicleProductos, adapter, this)
                adapter.notifyDataSetChanged() // Notifica al adaptador que los datos han cambiado
            }
        }
    }


    private fun inicializarRecicle(
        recycle: RecyclerView,
        adapter: adapter_pbl_vr_tb_recientes, // Cambiado a publicaciones_ralizadas
        context: Context
    ) {
        recycle.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        recycle.adapter = adapter
    }

    private fun bottomSheet_editar_campos(id_publicacion: String, tipo: String, tipoRef: String) {
        val bottomSheet = BottomSheetEditarProductoBinding.inflate(LayoutInflater.from(this))
        val view = bottomSheet.root

        bottomSheet.editar.setOnClickListener {
            dialog = BottomSheetDialog(this)
            bottom_sheet_editar_campos_publicidad(id_publicacion, tipo, tipoRef)
            dialog.show()

        }
        bottomSheet.editarCaracterisitcas.setOnClickListener {
            dialog = BottomSheetDialog(this)
            mostrarBottomSheetDescripcion(id_publicacion, tipoRef)
            dialog.show()
        }
        bottomSheet.metodoEntregaPago.setOnClickListener {
            dialog = BottomSheetDialog(this)
            bottomSheet_modificar_metodo_entrega_pago(id_publicacion, tipoRef)
            dialog.show()
        }
        dialog.setContentView(view)
    }

    private fun obtener_metodos_pagos(bottom_sheet: BottoSheetEditarMEntrPagBinding) {
        val db = FirebaseFirestore.getInstance().collection("Trabajadores_Usuarios_Drivers")
            .document("trabajadores")
            .collection("trabajadores")
            .document(firebaseAuth.uid.toString())
            .collection("metodos_pago")

        db.get().addOnSuccessListener { res ->
            bottom_sheet.chipsPagos.removeAllViews()
            for (datos in res) {
                val nombreMetodo = datos.getString("nombre_metodo")
                val id = datos.getString("id") // el ID que quieres mostrar
                if (!nombreMetodo.isNullOrEmpty() && !id.isNullOrEmpty()) {
                    val chip = Chip(this).apply {
                        text = nombreMetodo
                        isCheckable = true
                        tag = id // guardamos el ID como tag del chip
                    }

                    bottom_sheet.chipsPagos.addView(chip)
                }
            }
            bottom_sheet.chipsPagos.setOnCheckedStateChangeListener { group, checkedIds ->
                if (checkedIds.isNotEmpty()) {
                    val selectedChip = group.findViewById<Chip>(checkedIds[0])
                    val idSeleccionado = selectedChip.tag?.toString() ?: "Sin ID"
                    Toast.makeText(this, "ID seleccionado: $idSeleccionado", Toast.LENGTH_SHORT)
                        .show()
                    bottom_sheet.metodoPagoSelect.text = idSeleccionado.toString()
                }
            }


        }.addOnFailureListener {
            Toast.makeText(this, "Error al cargar métodos de pago", Toast.LENGTH_SHORT).show()
        }
    }


    private fun obtener_metodos_entrega(bottom_sheet: BottoSheetEditarMEntrPagBinding) {
        val db = FirebaseFirestore.getInstance().collection("Trabajadores_Usuarios_Drivers")
            .document("trabajadores")
            .collection("trabajadores")
            .document(firebaseAuth.uid.toString())
            .collection("metodos_entrega")

        db.get().addOnSuccessListener { res ->
            bottom_sheet.chipsEntregas.removeAllViews() // Limpiar chips anteriores
            for (datos in res) {
                val nombreMetodo = datos.getString("nombre_metodo")
                val id =
                    datos.getString("id") // Asegúrate de que este campo exista en Firestore

                if (!nombreMetodo.isNullOrEmpty() && !id.isNullOrEmpty()) {
                    val chip = Chip(this).apply {
                        text = nombreMetodo
                        isCheckable = true
                        tag = id // Guardamos el ID como tag
                    }

                    bottom_sheet.chipsEntregas.addView(chip)
                }
            }
            bottom_sheet.chipsEntregas.setOnCheckedStateChangeListener { group, checkedIds ->
                if (checkedIds.isNotEmpty()) {
                    val selectedChip = group.findViewById<Chip>(checkedIds[0])
                    val idSeleccionado = selectedChip.tag?.toString() ?: "Sin ID"
                    Toast.makeText(this, "ID seleccionado: $idSeleccionado", Toast.LENGTH_SHORT)
                        .show()
                    bottom_sheet.metodoEntregaSelect.text = idSeleccionado.toString()
                }
            }
        }.addOnFailureListener {
            Toast.makeText(this, "Error al cargar métodos de entrega", Toast.LENGTH_SHORT).show()
        }
    }

    private fun bottomSheet_modificar_metodo_entrega_pago(
        id_publicacion: String,
        tipo: String
    ) {
        val bottomSheet =
            BottoSheetEditarMEntrPagBinding.inflate(LayoutInflater.from(this))
        val view = bottomSheet.root

        obtener_metodos_pagos(bottomSheet)
        obtener_metodos_entrega(bottomSheet)

        dialog.setContentView(view)
    }

    private fun bottom_sheet_editar_campos_publicidad(
        id_publicacion: String,
        tipo: String,
        tipoRef: String
    ) {
        val bottomSheet =
            BottomSheetEditarCaracteristicasPublicidadBinding.inflate(LayoutInflater.from(this))
        val view = bottomSheet.root
        dialog = BottomSheetDialog(this)
        dialog.setContentView(view)
        dialog.show()

        bottomSheet.idPublicacion.text = id_publicacion
        bottomSheet.copiarId.setOnClickListener {
            constantestextos_general.copiarTexto_portapapeles(bottomSheet.idPublicacion, this)
        }
        bottomSheet.cargaContenidoActuliazr.isVisible = true
        bottomSheet.linealPrinciapl.isVisible = false
        bottomSheet.textocargaactualiza.text = "Cargando datos....."
        val db = FirebaseFirestore.getInstance().collection("Trabajadores_Usuarios_Drivers")
            .document("trabajadores").collection("trabajadores")
            .document(firebaseAuth.uid.toString()).collection("productos_venta")
            .document(tipoRef).collection(tipoRef).document(id_publicacion)
        val tiempoInicio = System.currentTimeMillis()
        db.get().addOnSuccessListener { res ->
            val tiempoFin = System.currentTimeMillis()
            val tiempoTotal = tiempoFin - tiempoInicio
            Handler(Looper.getMainLooper()).postDelayed({
                bottomSheet.cargaContenidoActuliazr.isVisible = false
                bottomSheet.linealPrinciapl.isVisible = true
            }, tiempoTotal) // 2000 ms = 2 segundos
            if (res.exists()) {
                val data = res.data
                val titulo = data?.get("titulo") as? String ?: ""
                val condicionProducto = data?.get("condicion_producto") as? String ?: ""
                val categoriaProducto = data?.get("categoria_producto") as? String ?: ""
                val nombre = data?.get("nombre") as? String ?: ""
                val stok = data?.get("stok") as? String ?: ""
                val precio = (data?.get("precio") as? Number)?.toInt() ?: 0
                val hashtagsGenerales =
                    data?.get("hashtags_generales") as? List<String> ?: emptyList()
                val masInformacion = data?.get("mas_informacio") as? String ?: ""
                val visibilidad = data?.get("visivilidad") as? String ?: ""
                val garantiaTexto = data?.get("garantia") as? String ?: ""
                val marca = data?.get("marca") as? String ?: ""
                val modelo = data?.get("modelo") as? String ?: ""
                val subcategori_producto = data?.get("subcategori_producto") as? String ?: ""
                val descuento = data?.get("descuento") as? Boolean ?: false
                val precioDescuento = (data?.get("precio_descuento") as? Number)?.toInt() ?: 0
                val categoria_producto = data?.get("categoria_producto") as? String ?: ""
                val localidadUser = data?.get("localidadUser") as? String ?: ""

                bottomSheet.tituloPublicacionPrED.setText(titulo)
                bottomSheet.subcategoriaProducto.setText(categoriaProducto)
                bottomSheet.catSelcionado.text = categoria_producto
                bottomSheet.nombreProductoED.setText(nombre)
                bottomSheet.stokED.setText(stok)
                bottomSheet.condicionPrED.setText(condicionProducto)
                bottomSheet.precioProductoED.setText(precio.toString())
                bottomSheet.agregarHastagsED.setText(hashtagsGenerales.joinToString(", "))
                bottomSheet.masInformacionED.setText(masInformacion)
                bottomSheet.mostrarPublicacionPara.setText(visibilidad)
                bottomSheet.hayGarantiaProductoED.setText(garantiaTexto)
                bottomSheet.precioNuevoDescuentoPrED.setText(precioDescuento.toString())
                bottomSheet.agregaUbiED.setText(localidadUser)

                var SwitchSihay_garantia = bottomSheet.siHayGarantia
                SwitchSihay_garantia.isChecked = garantiaTexto.isNotEmpty()
                bottomSheet.linealGarantia.isVisible = SwitchSihay_garantia.isChecked

                if (garantiaTexto.isNotEmpty()) {
                    val partes = garantiaTexto.split(" ")
                    if (partes.size >= 2) {
                        val cantidad = partes[0]
                        val unidad = partes[1].lowercase()
                        bottomSheet.hayGarantiaProductoED.setText(cantidad)
                        when (unidad) {
                            "mes", "meses" -> bottomSheet.meses.isChecked = true
                            "año", "años" -> bottomSheet.years.isChecked = true
                            "día", "días" -> bottomSheet.dias.isChecked = true
                        }
                    }
                }

                bottomSheet.linealGarantia.visibility =
                    if (SwitchSihay_garantia.isChecked) View.VISIBLE else View.GONE
                SwitchSihay_garantia.setOnCheckedChangeListener { _, isChecked ->
                    bottomSheet.linealGarantia.visibility =
                        if (isChecked) View.VISIBLE else View.GONE
                    if (!isChecked) {
                        bottomSheet.hayGarantiaProductoED.setText("")
                        bottomSheet.radioGrupPlazoRG.clearCheck()
                    }
                }

                bottomSheet.siHayDescuento.isChecked = descuento
                bottomSheet.precioNuevoDescuentoPr.visibility =
                    if (descuento) View.VISIBLE else View.GONE
                bottomSheet.siHayDescuento.setOnCheckedChangeListener { _, isChecked ->
                    bottomSheet.precioNuevoDescuentoPr.visibility =
                        if (isChecked) View.VISIBLE else View.GONE
                    if (!isChecked) {
                        bottomSheet.precioNuevoDescuentoPrED.setText("")
                    }
                }

                val mostrarMarcaModelo = marca.isNotEmpty() || modelo.isNotEmpty()
                bottomSheet.layoutNombreMarca.isVisible = mostrarMarcaModelo
                if (mostrarMarcaModelo) {
                    bottomSheet.marcaProductoED.setText(marca)
                    bottomSheet.modeloProductoED.setText(modelo)
                }

                val SwitchUbi = bottomSheet.agregaUbicaciones
                SwitchUbi.isChecked = localidadUser.isNotEmpty()
                bottomSheet.selecionLocalidad.isVisible = SwitchUbi.isChecked
                SwitchUbi.setOnCheckedChangeListener { _, isChecked ->
                    bottomSheet.selecionLocalidad.isVisible = isChecked
                }

                constantes_productos_publicados.obtener_estados_productos(
                    this,
                    bottomSheet.condicionPrED
                )


                constantesDatosUsuarioTienda.obtnerLocalidades(bottomSheet.agregaUbiED)
                constantesCarrito.setearDatosUsuario { nombre, numero, localid, apellido ->
                    bottomSheet.agregaUbiED.setText(localid)
                    bottomSheet.agregaUbiED.setText(localidadUser)
                    constantesDatosUsuarioTienda.obtnerLocalidades(bottomSheet.agregaUbiED)
                }


                bottomSheet.mostrarPublicacionPara.setOnClickListener {
                    val dialog_personales: BottomSheetDialog
                    dialog_personales = BottomSheetDialog(this)
                    constantes_productos_publicados.mostrar_dialog_para(
                        this, dialog_personales, bottomSheet.mostrarPublicacionPara.text.toString()
                    ) { selt ->
                        bottomSheet.mostrarPublicacionPara.text = selt
                    }
                    dialog_personales.show()
                }

                bottomSheet.subcategoriaProducto.setOnClickListener {
                    val dialog_personales: BottomSheetDialog
                    dialog_personales = BottomSheetDialog(this)
                    constantes_productos_publicados.agregarCategorias(
                        dialog_personales,
                        this,
                        bottomSheet.layoutNombreMarca,
                        bottomSheet.marcaProductoED,
                        bottomSheet.modeloProductoED,
                        bottomSheet.subcategoriaProducto,
                        bottomSheet.catSelcionado
                    )
                    dialog_personales.show()
                }

                bottomSheet.agregarHastagsED.setOnClickListener {
                    val dialog_personales: BottomSheetDialog
                    dialog_personales = BottomSheetDialog(this)
                    constantes_productos_publicados.obtener_hastags_generales(
                        bottomSheet.agregarHastagsED,
                        this,
                        hashtagsGeneralesList,
                        dialog_personales
                    )
                    dialog_personales.show()
                }

                bottomSheet.radioGrupPlazoRG.setOnCheckedChangeListener { _, checkedId ->
                    unidadGarantia = when (checkedId) {
                        R.id.meses -> "mes"
                        R.id.years -> "año"
                        R.id.dias -> "día"
                        else -> ""
                    }
                }

                bottomSheet.guardarCambios.setOnClickListener {
                    if (!validar_campos(bottomSheet)) {
                        Toast.makeText(
                            this,
                            "Por favor, completa todos los campos obligatorios",
                            Toast.LENGTH_SHORT
                        ).show()
                        return@setOnClickListener
                    }

                    actualizar_campos(bottomSheet, db) { completado ->
                        if (completado) {
                            obtener_productos(tipo, "No se encontraron productos publicados")
                            dialog.dismiss()
                        } else {
                            Toast.makeText(
                                this,
                                "Error al guardar los cambios",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }


            } else {
                Toast.makeText(this, "No se encontró la publicación", Toast.LENGTH_SHORT).show()
            }
        }.addOnFailureListener { e ->
            Toast.makeText(this, "Error al cargar datos: ${e.message}", Toast.LENGTH_SHORT).show()
            Log.e("Firestore", "Error al obtener datos: ", e)
        }

    }

    private fun actualizar_campos(
        bottomSheet: BottomSheetEditarCaracteristicasPublicidadBinding,
        db: DocumentReference, completados: (Boolean) -> Unit
    ) {
        bottomSheet.cargaContenidoActuliazr.isVisible = true
        bottomSheet.linealPrinciapl.isVisible = false
        bottomSheet.textocargaactualiza.text = "Actualizando contenido..."
        val hashtagsGeneralesFinal = bottomSheet.agregarHastagsED.text.toString()
            .split(",")
            .map { it.trim() }
            .filter { it.isNotBlank() }

        val hasmap = hashMapOf<String, Any>(
            "titulo" to bottomSheet.tituloPublicacionPrED.text.toString(),
            "condicion_producto" to bottomSheet.condicionPrED.text.toString(),
            "categoria_producto" to bottomSheet.catSelcionado.text.toString(),
            "subcategori_producto" to bottomSheet.subcategoriaProducto.text.toString(),
            "marca" to bottomSheet.marcaProductoED.text.toString(),
            "modelo" to bottomSheet.modeloProductoED.text.toString(),
            "hashtags_generales" to hashtagsGeneralesFinal,
            "nombre" to bottomSheet.nombreProductoED.text.toString(),
            "precio" to (bottomSheet.precioProductoED.text.toString().toIntOrNull() ?: 0),
            "precioDelivery" to 5,
            "stok" to bottomSheet.stokED.text.toString(),
            "visivilidad" to bottomSheet.mostrarPublicacionPara.text.toString(),
            "mas_informacio" to bottomSheet.masInformacionED.text.toString()
        )
        // Garantía
        if (bottomSheet.siHayGarantia.isChecked) {
            val tiempo = bottomSheet.hayGarantiaProductoED.text.toString()
            val unidad = when {
                bottomSheet.meses.isChecked -> "mes"
                bottomSheet.years.isChecked -> "año"
                bottomSheet.dias.isChecked -> "día"
                else -> ""
            }
            if (tiempo.isNotEmpty() && unidad.isNotEmpty()) {
                val plural = if (tiempo.toIntOrNull() != 1) {
                    if (unidad == "mes") "es" else "s"
                } else ""
                hasmap["garantia"] = "$tiempo $unidad$plural"
            }
        } else {
            hasmap["garantia"] = ""
        }
        if (bottomSheet.agregaUbicaciones.isChecked) {
            hasmap["localidadUser"] = bottomSheet.agregaUbiED.text.toString()
        } else {
            hasmap["localidadUser"] = FieldValue.delete()
        }
        // Descuento
        if (bottomSheet.siHayDescuento.isChecked) {
            val precioOriginal = bottomSheet.precioProductoED.text.toString().toIntOrNull() ?: 0
            val precioDescuento =
                bottomSheet.precioNuevoDescuentoPrED.text.toString().toIntOrNull() ?: 0
            val descuentoAplicado = if (precioOriginal > 0 && precioDescuento < precioOriginal) {
                val descuentoCalculado =
                    ((precioOriginal - precioDescuento).toDouble() / precioOriginal) * 100
                descuentoCalculado.roundToInt()
            } else {
                0
            }
            hasmap["cantidad_porcentaje_descuento"] = descuentoAplicado
            hasmap["precio_descuento"] = precioDescuento
            hasmap["descuento"] = true
        } else {
            hasmap["cantidad_porcentaje_descuento"] = FieldValue.delete()
            hasmap["precio_descuento"] = FieldValue.delete()
            hasmap["descuento"] = false
        }
        db.set(hasmap, SetOptions.merge()).addOnSuccessListener {
            Toast.makeText(this, "Cambios realizados correctamente", Toast.LENGTH_SHORT).show()
            completados(true)
        }.addOnFailureListener { e ->
            completados(false)
        }
    }


    private fun mostrarBottomSheetDescripcion(
        id_publicacion: String, tipoRef: String
    ) {
        val bindingSheet =
            BottomSheetConfiguracionDescripcionPrVrBinding.inflate(LayoutInflater.from(this))
        val view = bindingSheet.root
        bindingSheet.cargaContenidoActuliazr.isVisible = true
        bindingSheet.linealPrinciapl.isVisible = false
        bindingSheet.textocargaactualiza.text = "Cargando datos....."
        bindingSheet.linealEdicion.isVisible = true
        bindingSheet.idPublicacion.text = id_publicacion.toString()
        bindingSheet.copiarId.setOnClickListener {
            constantestextos_general.copiarTexto_portapapeles(
                bindingSheet.idPublicacion,
                this
            )
        }
        val db = FirebaseFirestore.getInstance()
            .collection("Trabajadores_Usuarios_Drivers")
            .document("trabajadores")
            .collection("trabajadores")
            .document(firebaseAuth.uid.toString())
            .collection("productos_venta")
            .document(tipoRef)
            .collection(tipoRef)
            .document(id_publicacion)
        val tiempoInicio = System.currentTimeMillis()
        db.get().addOnSuccessListener { res ->
            if (res.exists()) {
                val tiempoFin = System.currentTimeMillis()
                val tiempoTotal = tiempoFin - tiempoInicio
                Handler(Looper.getMainLooper()).postDelayed({
                    bindingSheet.cargaContenidoActuliazr.isVisible = false
                    bindingSheet.linealPrinciapl.isVisible = true
                }, tiempoTotal) // 2000 ms = 2 segundos
                // 1. Lista de descripciones
                val descripcionLista =
                    res.get("descripcion_texto_lista") as? List<String> ?: emptyList()
                Log.d("Firestore", "descripcion_texto_lista: $descripcionLista")

                // 2. Mapa: descripcion_texto
                val descripcionTexto = res.get("descripcion_texto") as? Map<*, *>
                val textoDescripcion = descripcionTexto?.get("texto_descripcion") as? String ?: ""
                val textoMayus = descripcionTexto?.get("texto_mayus") as? String ?: ""
                val textoEstilo = descripcionTexto?.get("texto_valor_style") as? String ?: ""
                Log.d(
                    "Firestore",
                    "descripcion_texto: $textoDescripcion, mayus: $textoMayus, estilo: $textoEstilo"
                )

                // 3. Mapa: descripcion_titulo
                val descripcionTitulo = res.get("descripcion_titulo") as? Map<*, *>
                val tituloDescripcion =
                    descripcionTitulo?.get("titulo_descripcion") as? String ?: ""
                val tituloMayus = descripcionTitulo?.get("titulo_mayus") as? String ?: ""
                val tituloEstilo = descripcionTitulo?.get("titulo_valor_style") as? String ?: ""
                Log.d(
                    "Firestore",
                    "descripcion_titulo: $tituloDescripcion, mayus: $tituloMayus, estilo: $tituloEstilo"
                )


                if (tituloDescripcion != null && tituloDescripcion.isNotEmpty()) {
                    Log.d("DEBUGsss", "Mostrar vista previa")
                    mostrarVistaPrevia(bindingSheet)
                } else {
                    Log.d("DEBUGsss", "Ocultar vista previa")
                    ocultarVistaPrevia(bindingSheet)
                }
                // Prellenar campos si hay datos
                bindingSheet.tituloProductoED.setText(tituloDescripcion ?: "")
                bindingSheet.AgregaDescipcionProductoED.setText(textoDescripcion ?: "")
                bindingSheet.colocarBoldAgunasLetrasED.setText(
                    descripcionLista?.joinToString(", ") ?: ""
                )
//                Log.d("vezllamada", "mayus titulo $mayus_minus , mayus de texto $mayus_minus_des")

                // --- FIX FOR MAYUS/MINUS SELECTION ---

                // Clear selection for title's capitalization options
                bindingSheet.includeAgregarBoldTitulo.gurpoMayus.clearCheck()
                when (tituloMayus?.lowercase()) {
                    "mayuscula" -> bindingSheet.includeAgregarBoldTitulo.mayuscula.isChecked = true
                    "minuscula" -> bindingSheet.includeAgregarBoldTitulo.minuscula.isChecked = true
                    else -> {
                        // Optionally, uncheck both if no valid option is provided
                        // This ensures a clean slate if previous state was set
                        bindingSheet.includeAgregarBoldTitulo.mayuscula.isChecked = false
                        bindingSheet.includeAgregarBoldTitulo.minuscula.isChecked = false
                    }
                }

                // Clear selection for description's capitalization options
                bindingSheet.includeAgregarTextosCuales.gurpoMayus.clearCheck()
                when (textoMayus?.lowercase()) {
                    "mayuscula" -> bindingSheet.includeAgregarTextosCuales.mayusTxt.isChecked = true
                    "minuscula" -> bindingSheet.includeAgregarTextosCuales.minusTxt.isChecked = true
                    else -> {
                        // Optionally, uncheck both if no valid option is provided
                        bindingSheet.includeAgregarTextosCuales.mayusTxt.isChecked = false
                        bindingSheet.includeAgregarTextosCuales.minusTxt.isChecked = false
                    }
                }

                // --- Continue with the rest of your existing logic ---

                // Also do this for bold/italic/underline options for the title
                bindingSheet.includeAgregarBoldTitulo.grupoSubralladoTXT.clearCheck()
                when (tituloEstilo?.lowercase()) {
                    "bold" -> bindingSheet.includeAgregarBoldTitulo.bold.isChecked = true
                    "cursiva" -> bindingSheet.includeAgregarBoldTitulo.cursiva.isChecked = true
                    "subrayado" -> bindingSheet.includeAgregarBoldTitulo.subrallado.isChecked = true
                    else -> {
                        bindingSheet.includeAgregarBoldTitulo.bold.isChecked = false
                        bindingSheet.includeAgregarBoldTitulo.cursiva.isChecked = false
                        bindingSheet.includeAgregarBoldTitulo.subrallado.isChecked = false
                    }
                }

                // Also do this for bold/italic/underline options for the description
                bindingSheet.includeAgregarTextosCuales.grupoSubralladoTXT.clearCheck()
                when (textoEstilo?.lowercase()) {
                    "bold" -> bindingSheet.includeAgregarTextosCuales.bold.isChecked = true
                    "cursiva" -> bindingSheet.includeAgregarTextosCuales.cursiva.isChecked = true
                    "subrayado" -> bindingSheet.includeAgregarTextosCuales.subrallado.isChecked =
                        true

                    else -> {
                        bindingSheet.includeAgregarTextosCuales.bold.isChecked = false
                        bindingSheet.includeAgregarTextosCuales.cursiva.isChecked = false
                        bindingSheet.includeAgregarTextosCuales.subrallado.isChecked = false
                    }
                }

            } else {
                Log.d("Firestore", "Documento no encontrado")
            }
        }

        bindingSheet.tituloProductoED.addTextChangedListener(object : TextWatcher {
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val texto = s?.toString()?.trim()
                if (!texto.isNullOrEmpty()) {
                    mostrarVistaPrevia(bindingSheet)
                    actualizarTextoFormateado(bindingSheet)
                } else {
                    ocultarVistaPrevia(bindingSheet)
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun afterTextChanged(s: Editable?) {}
        })


        bindingSheet.AgregaDescipcionProductoED.addTextChangedListener {
            bindingSheet.textoDescripcion.text = it.toString()
        }

        bindingSheet.colocarBoldAgunasLetrasED.addTextChangedListener {
            actualizarVistaPreviaConNegritas(bindingSheet)
        }
        // Checkboxes title (keep these as they are)
        bindingSheet.includeAgregarBoldTitulo.bold.setOnCheckedChangeListener { _, _ ->
            actualizarTextoFormateado(bindingSheet)
        }
        bindingSheet.includeAgregarBoldTitulo.cursiva.setOnCheckedChangeListener { _, _ ->
            actualizarTextoFormateado(bindingSheet)
        }
        bindingSheet.includeAgregarBoldTitulo.subrallado.setOnCheckedChangeListener { _, _ ->
            actualizarTextoFormateado(bindingSheet)
        }
        bindingSheet.includeAgregarBoldTitulo.mayuscula.setOnCheckedChangeListener { _, _ ->
            actualizarTextoFormateado(bindingSheet)
        }
        bindingSheet.includeAgregarBoldTitulo.minuscula.setOnCheckedChangeListener { _, _ ->
            actualizarTextoFormateado(bindingSheet)
        }
        bindingSheet.includeAgregarTextosCuales.bold.setOnCheckedChangeListener { _, _ ->
            actualizarVistaPreviaConNegritas(bindingSheet)
        }
        bindingSheet.includeAgregarTextosCuales.cursiva.setOnCheckedChangeListener { _, _ ->
            actualizarVistaPreviaConNegritas(bindingSheet)
        }
        bindingSheet.includeAgregarTextosCuales.subrallado.setOnCheckedChangeListener { _, _ ->
            actualizarVistaPreviaConNegritas(bindingSheet)
        }
        bindingSheet.includeAgregarTextosCuales.mayusTxt.setOnCheckedChangeListener { _, _ ->
            actualizarVistaPreviaConNegritas(bindingSheet)
        }
        bindingSheet.includeAgregarTextosCuales.minusTxt.setOnCheckedChangeListener { _, _ ->
            actualizarVistaPreviaConNegritas(bindingSheet)
        }

        bindingSheet.guardarCambios.setOnClickListener {
            validar_campos_decripcion_productos(bindingSheet) { completado ->
                if (completado) {
                    val radioPadre_bold_cursiva =
                        bindingSheet.includeAgregarBoldTitulo.grupoSubralladoTXT
                    val radio_padre_mayus_minus = bindingSheet.includeAgregarBoldTitulo.gurpoMayus
                    val titlo_descripcion = bindingSheet.tituloProductoED.text.toString()
                    val descripcion_desc = bindingSheet.AgregaDescipcionProductoED.text.toString()

                    val idSeleccionado = radioPadre_bold_cursiva.checkedRadioButtonId
                    val id_mayus_minus = radio_padre_mayus_minus.checkedRadioButtonId

                    val radio_padre_bold_cursiva_texto_des =
                        bindingSheet.includeAgregarTextosCuales.grupoSubralladoTXT
                    val radio_padre_mayus_minus_texto_des =
                        bindingSheet.includeAgregarTextosCuales.gurpoMayus
                    val idSeleccionado_des = radio_padre_bold_cursiva_texto_des.checkedRadioButtonId
                    val id_mayus_minus_des = radio_padre_mayus_minus_texto_des.checkedRadioButtonId


                    val texto = bindingSheet.colocarBoldAgunasLetrasED.text.toString().trim()

                    val listaFrases = texto
                        .split(",") // separamos usando coma
                        .map { it.trim() } // quitamos espacios alrededor
                        .filter { it.isNotEmpty() } // evitamos frases vacías si las hay

                    val valorSeleccionadoTitulo = when (idSeleccionado) {
                        R.id.bold -> "Bold"
                        R.id.cursiva -> "Cursiva"
                        R.id.subrallado -> "Subrayado"
                        else -> "" // Por si no seleccionó nada
                    }
                    val valorMayusMinusTitulo = when (id_mayus_minus) {
                        R.id.mayuscula -> "mayuscula"
                        R.id.minuscula -> "minuscula"
                        else -> "" // Por si no seleccionó nada
                    }
                    val valorSeleccionadoDes = when (idSeleccionado_des) {
                        R.id.bold -> "Bold"
                        R.id.cursiva -> "Cursiva"
                        R.id.subrallado -> "Subrayado"
                        else -> "" // Por si no seleccionó nada
                    }
                    val valorMayusMinusDes = when (id_mayus_minus_des) {
                        R.id.mayus_txt -> "mayuscula"
                        R.id.minus_txt -> "minuscula"
                        else -> "" // Por si no seleccionó nada
                    }
                    val descripcion_titulo = mapOf(
                        "titulo_descripcion" to titlo_descripcion,
                        "titulo_valor_style" to valorSeleccionadoTitulo,
                        "titulo_mayus" to valorMayusMinusTitulo
                    )
                    val descripcion_texto = mapOf(
                        "texto_descripcion" to descripcion_desc,
                        "texto_valor_style" to valorSeleccionadoDes,
                        "texto_mayus" to valorMayusMinusDes
                    )
                    val datosActualizados = mapOf(
                        "descripcion_titulo" to descripcion_titulo,
                        "descripcion_texto" to descripcion_texto,
                        "descripcion_texto_lista" to listaFrases
                    )
                    db.set(datosActualizados, SetOptions.merge())
                        .addOnSuccessListener {
                            Toast.makeText(
                                this,
                                "Se actulaziron los cambios realizados",
                                Toast.LENGTH_SHORT
                            ).show()
                            bindingSheet.cargaContenidoActuliazr.isVisible = true
                            bindingSheet.linealPrinciapl.isVisible = false
                            bindingSheet.textocargaactualiza.text = "Actualizando contenido..."
                            dialog.dismiss()
                        }
                        .addOnFailureListener { e ->
                            // Manejo de error
                        }
                }
            }


        }
        actualizarTextoFormateado(bindingSheet)
        actualizarVistaPreviaConNegritas(bindingSheet)
        dialog.setContentView(view)

    }


    private fun validar_campos_decripcion_productos(
        bindingSheet: BottomSheetConfiguracionDescripcionPrVrBinding,
        completado: (Boolean) -> Unit
    ) {
        val titulo = bindingSheet.tituloProductoED.text.toString().trim()
        val descripcion = bindingSheet.AgregaDescipcionProductoED.text.toString().trim()

        if (titulo.isEmpty()) {
            bindingSheet.tituloProductoED.error = "Ingrese un título para tu descripción"
            bindingSheet.tituloProductoED.requestFocus()
            completado(false)
        } else if (descripcion.isEmpty()) {
            bindingSheet.AgregaDescipcionProductoED.error = "Ingrese una descripción"
            bindingSheet.AgregaDescipcionProductoED.requestFocus()
            completado(false)
        } else {
            completado(true) // ✅ Todo está bien
        }
    }


    fun mostrarVistaPrevia(bindingSheet: BottomSheetConfiguracionDescripcionPrVrBinding) {
        bindingSheet.linealVistaPrevia.apply {
            if (!isVisible) {
                alpha = 0f
                isVisible = true
                animate()
                    .alpha(1f)
                    .setDuration(300)
                    .start()
            }
        }
    }

    fun ocultarVistaPrevia(bindingSheet: BottomSheetConfiguracionDescripcionPrVrBinding) {
        bindingSheet.linealVistaPrevia.animate()
            .alpha(0f)
            .setDuration(200)
            .withEndAction {
                bindingSheet.linealVistaPrevia.isVisible = false
            }.start()
    }

    private fun actualizarTextoFormateado(binding_bottom_sheeet: BottomSheetConfiguracionDescripcionPrVrBinding) {
        var texto = binding_bottom_sheeet.tituloProductoED.text.toString()

        // Convertir a mayúsculas o minúsculas si corresponde
        texto = when {
            binding_bottom_sheeet.includeAgregarBoldTitulo.mayuscula.isChecked -> texto.uppercase()
            binding_bottom_sheeet.includeAgregarBoldTitulo.minuscula.isChecked -> texto.lowercase()
            else -> texto
        }

        val spannable = SpannableString(texto)

        // Aplicar estilo
        when {
            binding_bottom_sheeet.includeAgregarBoldTitulo.bold.isChecked -> {
                spannable.setSpan(
                    StyleSpan(Typeface.BOLD),
                    0,
                    texto.length,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                )
            }

            binding_bottom_sheeet.includeAgregarBoldTitulo.cursiva.isChecked -> {
                spannable.setSpan(
                    StyleSpan(Typeface.ITALIC),
                    0,
                    texto.length,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                )
            }

            binding_bottom_sheeet.includeAgregarBoldTitulo.subrallado.isChecked -> {
                spannable.setSpan(
                    UnderlineSpan(),
                    0,
                    texto.length,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                )
            }
        }

        binding_bottom_sheeet.previewTextTitulo.text = spannable
    }


    private fun actualizarVistaPreviaConNegritas(binding_bottom_sheeet: BottomSheetConfiguracionDescripcionPrVrBinding) {
        var textoOriginal = binding_bottom_sheeet.AgregaDescipcionProductoED.text.toString()
        val partesTexto = binding_bottom_sheeet.colocarBoldAgunasLetrasED.text.toString()
            .split(",")
            .map { it.trim() }

        val spannableBuilder = SpannableStringBuilder(textoOriginal)

        for (parte in partesTexto) {
            if (parte.isEmpty()) continue

            val start = spannableBuilder.indexOf(parte)
            val end = start + parte.length

            if (start != -1) {
                // Aplicar mayúscula o minúscula

                val textoTransformado = when {

                    binding_bottom_sheeet.includeAgregarTextosCuales.mayusTxt.isChecked -> parte.uppercase()
                    binding_bottom_sheeet.includeAgregarTextosCuales.minusTxt.isChecked -> parte.lowercase()
                    else -> parte
                }

                // Reemplazar el texto encontrado por el texto transformado
                spannableBuilder.replace(start, end, textoTransformado)

                // Aplicar estilos después de reemplazar
                when {
                    binding_bottom_sheeet.includeAgregarTextosCuales.bold.isChecked -> {
                        spannableBuilder.setSpan(
                            StyleSpan(Typeface.BOLD),
                            start,
                            start + textoTransformado.length,
                            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                        )
                    }

                    binding_bottom_sheeet.includeAgregarTextosCuales.cursiva.isChecked -> {
                        spannableBuilder.setSpan(
                            StyleSpan(Typeface.ITALIC),
                            start,
                            start + textoTransformado.length,
                            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                        )
                    }

                    binding_bottom_sheeet.includeAgregarTextosCuales.subrallado.isChecked -> {
                        spannableBuilder.setSpan(
                            UnderlineSpan(),
                            start,
                            start + textoTransformado.length,
                            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                        )
                    }
                }
            }
        }

        binding_bottom_sheeet.textoDescripcion.text = spannableBuilder
    }

    private fun validar_campos(botto_shet: BottomSheetEditarCaracteristicasPublicidadBinding): Boolean {
        var esValido = true

        val titulo_producto = botto_shet.tituloPublicacionPrED
        val modelo_producto = botto_shet.modeloProductoED
        val marca_producto = botto_shet.marcaProductoED
        val categoria_producto = botto_shet.subcategoriaProducto
        val nombre_producto = botto_shet.nombreProductoED
        val stok_producto = botto_shet.stokED
        val condicion_producto = botto_shet.condicionPrED
        val precioProducto = botto_shet.precioProductoED
        val agregarhastags = botto_shet.agregarHastagsED
        val mas_info = botto_shet.masInformacionED
        val categoria_Selecionada = botto_shet.catSelcionado
        val siHayDescuento = botto_shet.siHayDescuento
        val precio_descuento = botto_shet.precioNuevoDescuentoPrED
        val garantia = botto_shet.siHayGarantia
        val hay_garantia = botto_shet.hayGarantiaProductoED
        val ubicacion = botto_shet.agregaUbicaciones
        val ubicacion_prd = botto_shet.agregaUbiED


        val categoriasSinMarcaNiModelo = listOf(
            "Juguetes y juegos",
            "Arte y antigüedades",
            "Hobbies y actividades",
            "Ropa, calzado y accesorios",
            "Muebles",
            "Hogar y jardín",
            "Construcción y materiales"
        )
        val categoriaSeleccionadaText = categoria_Selecionada.text.toString().trim()

        fun validarCampoVacio(campo: EditText, mensaje: String) {
            if (campo.text.toString().isBlank()) {
                campo.error = mensaje
                esValido = false
            }
        }

        validarCampoVacio(titulo_producto, "Ingrese un título")
        validarCampoVacio(categoria_producto, "Seleccione una categoría")
        validarCampoVacio(nombre_producto, "Ingrese el nombre del producto")
        validarCampoVacio(condicion_producto, "Ingrese la condición del producto")
        validarCampoVacio(agregarhastags, "Seleccione al menos un hashtag")
        validarCampoVacio(mas_info, "Ingrese información adicional")


        // Validar marca y modelo si la categoría lo requiere
        if (categoriaSeleccionadaText.isNotEmpty() &&
            !categoriasSinMarcaNiModelo.contains(categoriaSeleccionadaText)
        ) {
            validarCampoVacio(marca_producto, "Ingrese la marca")
            validarCampoVacio(modelo_producto, "Ingrese el modelo")
        }

        // Validar descuento
        if (siHayDescuento.isChecked) {
            validarCampoVacio(precio_descuento, "Ingrese el precio con descuento")
            val precioOriginal = precioProducto.text.toString().toDoubleOrNull()
            val precioConDescuento = precio_descuento.text.toString().toDoubleOrNull()

            if (precioOriginal != null && precioConDescuento != null) {
                if (precioConDescuento >= precioOriginal) {
                    Toast.makeText(
                        this,
                        "El precio con descuento debe ser menor al precio original",
                        Toast.LENGTH_SHORT
                    ).show()
                    precio_descuento.error = "Cambiar el valor"
                    precio_descuento.requestFocus()
                    esValido = false
                }
            } else {
                Toast.makeText(this, "Precios inválidos", Toast.LENGTH_SHORT).show()
                esValido = false
            }
        }

        // Validar garantía
        if (garantia.isChecked) {
            // Validar campo de texto de la garantía
            validarCampoVacio(hay_garantia, "Ingrese la información de la garantía")

            // Validar que se haya seleccionado un plazo
            val radioGroup =
                botto_shet.radioGrupPlazoRG // Asegúrate de que este ID está en tu binding

            if (radioGroup.checkedRadioButtonId == -1) {
                Toast.makeText(
                    this,
                    "Seleccione un plazo para la garantía (días, meses o años)",
                    Toast.LENGTH_SHORT
                ).show()
                esValido = false
            }
        }

        // Validar ubicación si es requerida
        if (ubicacion.isChecked) {
            validarCampoVacio(ubicacion_prd, "Ingrese la ubicación del producto")
        }

        // Validar stock
        val stock = stok_producto.text.toString().toIntOrNull()
        if (stock == null || stock <= 0) {
            stok_producto.error = "El stock debe ser mayor a 0"
            stok_producto.requestFocus()

            esValido = false
        }

        // Validar precio original
        val precio = precioProducto.text.toString().toIntOrNull()
        if (precio == null || precio <= 0) {
            precioProducto.error = "El precio debe ser mayor a 0"
            precioProducto.requestFocus()
            esValido = false
        }

        return esValido
    }

}


