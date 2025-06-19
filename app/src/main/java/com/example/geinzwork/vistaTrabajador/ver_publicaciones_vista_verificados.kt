package com.example.geinzwork.vistaTrabajador

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.SpannableString
import android.util.Log
import android.view.LayoutInflater
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.geinzwork.adapterViewholder.adapter_pbl_vr_tb_recientes
import com.example.geinzwork.constantesGeneral.Variables
import com.example.geinzwork.constantesGeneral.constantes_hastags_generales
import com.example.geinzwork.crear_publicaciones_recientes
import com.example.geinzwork.dataclass.dataclas_trabajos_ralizados_verificados
import com.geinzz.geinzwork.R
import com.geinzz.geinzwork.constantesGeneral.constantestextos_general
import com.geinzz.geinzwork.constantesGeneral.filtradoLocalidadElementos
import com.geinzz.geinzwork.constantesGeneral.filtradoLocalidadElementos.listaFiltrado
import com.geinzz.geinzwork.constantesGeneral.filtradoLocalidadElementos.obtenerLocalidadUser
import com.geinzz.geinzwork.databinding.ActivityVerPublicacionesVistaVerificadosBinding
import com.geinzz.geinzwork.databinding.BottomSheetCamposTrPdPBinding
import com.geinzz.geinzwork.databinding.BottomSheetEditarPublicacionesVerificadosBinding
import com.geinzz.geinzwork.databinding.BottomSheetMinimoMaxFiltradoBinding

import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions

class ver_publicaciones_vista_verificados : AppCompatActivity() {
    private lateinit var binding: ActivityVerPublicacionesVistaVerificadosBinding
    private val lista = mutableListOf<dataclas_trabajos_ralizados_verificados>()
    private lateinit var adapter: adapter_pbl_vr_tb_recientes
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var dialog: BottomSheetDialog
    private val hashtagsGenerales = mutableListOf<String>()
    private val hashtagsCategoria = mutableListOf<String>()
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityVerPublicacionesVistaVerificadosBinding.inflate(layoutInflater)
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
        })
        val dato_pasado = intent.getStringExtra("tipo").toString()
        Log.d("DebugTipo", "dato_pasado: $dato_pasado")

        when (dato_pasado) {
            "publicadas" -> {
                binding.linealChips.isVisible = true
                obtener_publicaciones_realizadas(
                    firebaseAuth.uid.toString(),
                    "publicados",
                    "No se encontraron datos Publicados"
                )
                binding.todos.setOnClickListener {
                    binding.linealEncontrados.isVisible = false
                    obtener_publicaciones_realizadas(
                        firebaseAuth.uid.toString(),
                        "publicados",
                        "No se encontraron datos Publicados"
                    )
                }
                binding.masClicks.setOnClickListener {
                    binding.linealEncontrados.isVisible = true
                    binding.recicleViewTrabajos.isVisible = false
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
                    binding.recicleViewTrabajos.isVisible = false
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
                    binding.recicleViewTrabajos.isVisible = false
                    obtener_mayor_menor_cantidad_campos("estadisticas_compartir") { max, min ->

                        dialog = BottomSheetDialog(this)
                        bottom_sheet_chips(max, min) { minC, maxC ->
                            filtrar_publicaciones(minC, maxC, "estadisticas_vistas")
                        }
                        dialog.show()
                    }
                }
                binding.privado.setOnClickListener {
                    binding.linealEncontrados.isVisible = true
                    binding.recicleViewTrabajos.isVisible = false
                    obtener_publicaciones_realizadas(
                        firebaseAuth.uid.toString(),
                        "privado",
                        "No se encontraron datos Publicados"
                    )


                }
                binding.soloSeguidores.setOnClickListener {
                    binding.linealEncontrados.isVisible = true
                    binding.recicleViewTrabajos.isVisible = false

                    obtener_publicaciones_realizadas(
                        firebaseAuth.uid.toString(),
                        "solo_seguidores",
                        "No se encontraron datos Publicados"
                    )
                }
            }

            "archivadas" -> {
                binding.linealChips.isVisible = false
                obtener_publicaciones_realizadas(
                    firebaseAuth.uid.toString(),
                    "archivados",
                    "No se encontraron datos archivados"
                )
            }

            "eliminadas" -> {
                binding.linealChips.isVisible = false
                obtener_publicaciones_realizadas(
                    firebaseAuth.uid.toString(),
                    "eliminados",
                    "No se encontraron datos eliminados"
                )
            }

            else -> {
                binding.linealChips.isVisible = true
                obtener_publicaciones_realizadas(
                    firebaseAuth.uid.toString(),
                    "publicados",
                    "No se encontraron datos publicados"
                )
                binding.masClicks.setOnClickListener {
                    binding.linealEncontrados.isVisible = true
                    binding.recicleViewTrabajos.isVisible = false
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
                    binding.recicleViewTrabajos.isVisible = false
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
                    binding.recicleViewTrabajos.isVisible = false
                    obtener_mayor_menor_cantidad_campos("estadisticas_compartir") { max, min ->

                        dialog = BottomSheetDialog(this)
                        bottom_sheet_chips(max, min) { minC, maxC ->
                            filtrar_publicaciones(minC, maxC, "estadisticas_vistas")
                        }
                        dialog.show()
                    }
                }
                binding.privado.setOnClickListener {
                    binding.linealEncontrados.isVisible = true
                    binding.recicleViewTrabajos.isVisible = false
                    obtener_publicaciones_realizadas(
                        firebaseAuth.uid.toString(),
                        "privado",
                        "No se encontraron datos Publicados"
                    )


                }
                binding.soloSeguidores.setOnClickListener {
                    binding.linealEncontrados.isVisible = true
                    binding.recicleViewTrabajos.isVisible = false

                    obtener_publicaciones_realizadas(
                        firebaseAuth.uid.toString(),
                        "solo_seguidores",
                        "No se encontraron datos Publicados"
                    )
                }
            }
        }


    }

    private fun filtrar_publicaciones(min: Int, max: Int, filtado_pasado: String) {
        lista.clear()
        Toast.makeText(
            this@ver_publicaciones_vista_verificados,
            "Filtramos por el mínimo de $min y el máximo de $max",
            Toast.LENGTH_SHORT
        ).show()

        val firestore = FirebaseFirestore.getInstance()
        val uid = firebaseAuth.uid.toString()
        val refPublicacion = firestore.collection("Trabajadores_Usuarios_Drivers")
            .document("trabajadores").collection("trabajadores")
            .document(uid).collection("publicaciones_trabajos")
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
                binding.recicleViewTrabajos.isVisible = false
                binding.linealEncontrados.isVisible = false
            } else {
                binding.linealNoCuenta.isVisible = false
                binding.linealEncontrados.isVisible = false
                binding.recicleViewTrabajos.isVisible = true
                binding.max.text = max.toString()
                binding.min.text = min.toString()
                inicializarRecicle(binding.recicleViewTrabajos, adapter, this)
                adapter.notifyDataSetChanged()
            }
        }
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
            .document(uid).collection("publicaciones_trabajos")
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
                editar.setOnClickListener {
                    Toast.makeText(
                        this,
                        "solo puedes editar caundo estas en TODOS",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                eliminar.setOnClickListener {
                    dialog.dismiss()
                    archivar_eliminar_publicaciones(
                        item.id_publicacion.toString(),
                        "publicados",
                        "eliminados", "mascliks"
                    )

                }
                archivar.setOnClickListener {
                    dialog.dismiss()
                    archivar_eliminar_publicaciones(
                        item.id_publicacion.toString(),
                        "publicados",
                        "archivados", "mascliks"
                    )
                }
                privado.setOnClickListener {
                    dialog.dismiss()
                    archivar_eliminar_publicaciones(
                        item.id_publicacion.toString(),
                        "publicados",
                        "privado", "mascliks"
                    )
                }
                solo_seguidores.setOnClickListener {
                    dialog.dismiss()
                    archivar_eliminar_publicaciones(
                        item.id_publicacion.toString(),
                        "publicados",
                        "solo_seguidores", "mascliks"
                    )
                }
            }
            if (binding.masVistas.isChecked) {
                editar.setOnClickListener {
                    Toast.makeText(
                        this,
                        "solo puedes editar caundo estas en TODOS",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                eliminar.setOnClickListener {
                    dialog.dismiss()
                    archivar_eliminar_publicaciones(
                        item.id_publicacion.toString(),
                        "publicados",
                        "eliminados", "masvistas"
                    )

                }
                archivar.setOnClickListener {
                    dialog.dismiss()
                    archivar_eliminar_publicaciones(
                        item.id_publicacion.toString(),
                        "publicados",
                        "archivados", "masvistas"
                    )
                }
                privado.setOnClickListener {
                    dialog.dismiss()
                    archivar_eliminar_publicaciones(
                        item.id_publicacion.toString(),
                        "publicados",
                        "privado", "masvistas"
                    )
                }
                solo_seguidores.setOnClickListener {
                    dialog.dismiss()
                    archivar_eliminar_publicaciones(
                        item.id_publicacion.toString(),
                        "publicados",
                        "solo_seguidores", "masvistas"
                    )
                }
            }
            if (binding.masCompartidas.isChecked) {
                editar.setOnClickListener {
                    Toast.makeText(
                        this,
                        "solo puedes editar caundo estas en TODOS",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                eliminar.setOnClickListener {
                    dialog.dismiss()
                    archivar_eliminar_publicaciones(
                        item.id_publicacion.toString(),
                        "publicados",
                        "eliminados", "mascompartidas"
                    )

                }
                archivar.setOnClickListener {
                    dialog.dismiss()
                    archivar_eliminar_publicaciones(
                        item.id_publicacion.toString(),
                        "publicados",
                        "archivados", "mascompartidas"
                    )
                }
                privado.setOnClickListener {
                    dialog.dismiss()
                    archivar_eliminar_publicaciones(
                        item.id_publicacion.toString(),
                        "publicados",
                        "privado", "mascompartidas"
                    )
                }
                solo_seguidores.setOnClickListener {
                    dialog.dismiss()
                    archivar_eliminar_publicaciones(
                        item.id_publicacion.toString(),
                        "publicados",
                        "solo_seguidores", "mascompartidas"
                    )
                }
            }
            if (binding.todos.isChecked) {
                eliminar.setOnClickListener {
                    dialog.dismiss()
                    archivar_eliminar_publicaciones(
                        item.id_publicacion.toString(),
                        "publicados",
                        "eliminados", "todos"
                    )

                }
                editar.setOnClickListener {
                    editar_publicaciones(item.id_publicacion.toString(), "publicados")
                }
                archivar.setOnClickListener {
                    dialog.dismiss()
                    archivar_eliminar_publicaciones(
                        item.id_publicacion.toString(),
                        "publicados",
                        "archivados", "todos"
                    )
                }
                privado.setOnClickListener {
                    dialog.dismiss()
                    archivar_eliminar_publicaciones(
                        item.id_publicacion.toString(),
                        "publicados",
                        "privado", "todos"
                    )
                }
                solo_seguidores.setOnClickListener {
                    dialog.dismiss()
                    archivar_eliminar_publicaciones(
                        item.id_publicacion.toString(),
                        "publicados",
                        "solo_seguidores", "todos"
                    )
                }
            }
            if (binding.privado.isChecked) {
                bottoSheet.privado.isVisible = false
                bottoSheet.soloSeguidores.isVisible = true
                bottoSheet.regresar.isVisible = true
                bottoSheet.regresar.setOnClickListener {
                    binding.linealEncontrados.isVisible = true
                    activar_publicacion("privado", item.id_publicacion.toString(), "privado")
                    binding.recicleViewTrabajos.isVisible = false
                    dialog.dismiss()
                }
                eliminar.setOnClickListener {
                    dialog.dismiss()
                    archivar_eliminar_publicaciones(
                        item.id_publicacion.toString(),
                        "privado",
                        "eliminados", "privado"
                    )

                }
                archivar.setOnClickListener {
                    dialog.dismiss()
                    archivar_eliminar_publicaciones(
                        item.id_publicacion.toString(),
                        "privado",
                        "archivados", "privado"
                    )
                }
                solo_seguidores.setOnClickListener {
                    dialog.dismiss()
                    archivar_eliminar_publicaciones(
                        item.id_publicacion.toString(),
                        "privado",
                        "solo_seguidores", "privado"
                    )
                }
                editar.setOnClickListener {
                    editar_publicaciones(item.id_publicacion.toString(), "privado")
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
                    binding.recicleViewTrabajos.isVisible = false
                    dialog.dismiss()
                }
                eliminar.setOnClickListener {
                    dialog.dismiss()
                    archivar_eliminar_publicaciones(
                        item.id_publicacion.toString(),
                        "solo_seguidores",
                        "eliminados", "solo_seguidores"
                    )
                }
                archivar.setOnClickListener {
                    dialog.dismiss()
                    archivar_eliminar_publicaciones(
                        item.id_publicacion.toString(),
                        "solo_seguidores",
                        "archivados", "solo_seguidores"
                    )
                }
                privado.setOnClickListener {
                    dialog.dismiss()
                    archivar_eliminar_publicaciones(
                        item.id_publicacion.toString(),
                        "solo_seguidores",
                        "privado", "solo_seguidores"
                    )
                }
                editar.setOnClickListener {
                    editar_publicaciones(item.id_publicacion.toString(), "solo_seguidores")
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
                binding.recicleViewTrabajos.isVisible = false
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
                binding.recicleViewTrabajos.isVisible = false
                dialog.dismiss()
            }
        }


        bottoSheet.eliminarPermanente.setOnClickListener {
            binding.linealEncontrados.isVisible = true
            binding.textoDinamicoProgrees.text = "Actualizando contenido..."
            binding.recicleViewTrabajos.isVisible = false
            dialog.dismiss()
            val firestore = FirebaseFirestore.getInstance()
            val uid = firebaseAuth.uid.toString()
            val refOrigen = firestore.collection("Trabajadores_Usuarios_Drivers")
                .document("trabajadores").collection("trabajadores")
                .document(uid).collection("publicaciones_trabajos")
                .document("eliminados").collection("eliminados")
                .document(item.id_publicacion.toString())
            refOrigen.delete().addOnSuccessListener { res ->
                Toast.makeText(this, "publicacion eliminado correctamente", Toast.LENGTH_SHORT)
                    .show()
                obtener_publicaciones_realizadas(
                    firebaseAuth.uid.toString(),
                    "eliminados",
                    "No se encontraron datos eliminados"
                )
                binding.linealEncontrados.isVisible = false
            }.addOnFailureListener { e ->
                Toast.makeText(this, "Error al eliminar la publicacion", Toast.LENGTH_SHORT)
                    .show()
            }
            dialog.dismiss()
        }

        dialog.setContentView(view)
    }


    private fun activar_publicacion(tipo: String, idPublicacion: String, tipo_fun: String) {
        val firestore = FirebaseFirestore.getInstance()
        val uid = firebaseAuth.uid.toString()
        val refOrigen = firestore.collection("Trabajadores_Usuarios_Drivers")
            .document("trabajadores").collection("trabajadores")
            .document(uid).collection("publicaciones_trabajos")
            .document(tipo).collection(tipo).document(idPublicacion)

        refOrigen.get().addOnSuccessListener { res ->
            if (res.exists()) {
                val data = res.data ?: return@addOnSuccessListener
                val categoria = data?.get("categoria") as? String ?: ""
                val contenido = data?.get("contenido") as? String ?: ""
                val estadisticas_click = data?.get("estadisticas_click") as? Number ?: 0
                val estadisticas_compartir = data?.get("estadisticas_compartir") as? Number ?: 0
                val estadisticas_vistas = data?.get("estadisticas_vistas") as? Number ?: 0
                val fecha_rec = data?.get("fecha_rec") as? String ?: ""
                val hora_rec = data?.get("hora_rec") as? String ?: ""
                val titulo = data?.get("titulo") as? String ?: ""
                val id = data?.get("id") as? String ?: ""
                val ubicacion = data?.get("ubicacion") as? String ?: ""
                val visibilidad = data?.get("visivilidad") as? String ?: ""
                val hashtags_generales =
                    data?.get("hashtags_generales") as? List<String> ?: emptyList()
                val hashtags_trabajos_publicados =
                    data?.get("hashtags_trabajos_publicados") as? List<String> ?: emptyList()

                val hashMap = hashMapOf<String, Any>(
                    "id" to id,
                    "categoria" to categoria,
                    "contenido" to contenido,
                    "estadisticas_click" to estadisticas_click,
                    "estadisticas_compartir" to estadisticas_compartir,
                    "estadisticas_vistas" to estadisticas_vistas,
                    "fecha_rec" to fecha_rec,
                    "hora_rec" to hora_rec,
                    "titulo" to titulo,
                    "ubicacion" to ubicacion,
                    "visivilidad" to visibilidad,
                    "hashtags_generales" to hashtags_generales,
                    "hashtags_trabajos_publicados" to hashtags_trabajos_publicados
                )
                for ((key, value) in data) {
                    if (key.startsWith("img_url") && value is String) {
                        hashMap[key] = value
                    }
                }
                val refDestino = firestore.collection("Trabajadores_Usuarios_Drivers")
                    .document("trabajadores").collection("trabajadores")
                    .document(uid).collection("publicaciones_trabajos")
                    .document("publicados").collection("publicados").document(idPublicacion)
                refDestino.set(hashMap).addOnSuccessListener {
                    refOrigen.delete().addOnSuccessListener {
                        Toast.makeText(
                            this,
                            "Publicacion m ovido correctamente",
                            Toast.LENGTH_SHORT
                        )
                            .show()
                        binding.linealEncontrados.isVisible = false
                        when (tipo_fun) {
                            "privado" -> {
                                obtener_publicaciones_realizadas(
                                    firebaseAuth.uid.toString(),
                                    "privado", "No se encontraron datos"
                                )
                            }

                            "solo_seguidores" -> {
                                obtener_publicaciones_realizadas(
                                    firebaseAuth.uid.toString(),
                                    "solo_seguidores", "No se encontraron datos"
                                )
                            }

                            "archivados" -> {
                                obtener_publicaciones_realizadas(
                                    firebaseAuth.uid.toString(),
                                    tipo,
                                    "No se encontraron datos "
                                )
                            }

                            "eliminados" -> {
                                obtener_publicaciones_realizadas(
                                    firebaseAuth.uid.toString(),
                                    tipo,
                                    "No se encontraron datos "
                                )
                            }
                        }


                    }.addOnFailureListener { e ->
                        Log.d("erro_pasado", "error al pasar la publicaon")
                    }

                }.addOnFailureListener { e ->
                    Log.d("erro_pasado", "error al pasar la publicaon")
                }

            } else {
                Log.d("erro_pasado", "error al pasar la publicaon")
            }
        }.addOnFailureListener { e ->
            Log.d("erro_pasado", "error al pasar la publicaon")

        }

    }

    private fun archivar_eliminar_publicaciones(
        id_publicacion: String,
        tipo1: String,
        tipo2: String, funciones: String
    ) {
        binding.linealEncontrados.isVisible = true
        binding.recicleViewTrabajos.isVisible = false
        val db = FirebaseFirestore.getInstance().collection(Variables.trabajadores_usuariosDB)
            .document(Variables.trabajadoresDB).collection(Variables.trabajadoresDB)
            .document(firebaseAuth.uid.toString())
            .collection("publicaciones_trabajos").document(tipo1).collection(tipo1)
            .document(id_publicacion)

        db.get().addOnSuccessListener { res ->
            if (res.exists()) {
                val data = res.data ?: return@addOnSuccessListener

                val categoria = data?.get("categoria") as? String ?: ""
                val contenido = data?.get("contenido") as? String ?: ""
                val estadisticas_click = data?.get("estadisticas_click") as? Number ?: 0
                val estadisticas_compartir = data?.get("estadisticas_compartir") as? Number ?: 0
                val estadisticas_vistas = data?.get("estadisticas_vistas") as? Number ?: 0
                val fecha_rec = data?.get("fecha_rec") as? String ?: ""
                val hora_rec = data?.get("hora_rec") as? String ?: ""
                val titulo = data?.get("titulo") as? String ?: ""
                val id = data?.get("id") as? String ?: ""
                val ubicacion = data?.get("ubicacion") as? String ?: ""
                val visibilidad = data?.get("visivilidad") as? String ?: ""
                val hashtags_generales =
                    data?.get("hashtags_generales") as? List<String> ?: emptyList()
                val hashtags_trabajos_publicados =
                    data?.get("hashtags_trabajos_publicados") as? List<String> ?: emptyList()

                val hashMap = hashMapOf<String, Any>(
                    "id" to id,
                    "categoria" to categoria,
                    "contenido" to contenido,
                    "estadisticas_click" to estadisticas_click,
                    "estadisticas_compartir" to estadisticas_compartir,
                    "estadisticas_vistas" to estadisticas_vistas,
                    "fecha_rec" to fecha_rec,
                    "hora_rec" to hora_rec,
                    "titulo" to titulo,
                    "ubicacion" to ubicacion,
                    "visivilidad" to visibilidad,
                    "hashtags_generales" to hashtags_generales,
                    "hashtags_trabajos_publicados" to hashtags_trabajos_publicados
                )
                for ((key, value) in data) {
                    if (key.startsWith("img_url") && value is String) {
                        hashMap[key] = value
                    }
                }
                val db2 =
                    FirebaseFirestore.getInstance().collection(Variables.trabajadores_usuariosDB)
                        .document(Variables.trabajadoresDB).collection(Variables.trabajadoresDB)
                        .document(firebaseAuth.uid.toString())
                        .collection("publicaciones_trabajos").document(tipo2).collection(tipo2)
                        .document(id_publicacion)
                db2.set(hashMap).addOnSuccessListener { res ->
                    Toast.makeText(this, "movido correctamente", Toast.LENGTH_SHORT).show()
                    binding.linealEncontrados.isVisible = false
                    binding.recicleViewTrabajos.isVisible = true
                    when (funciones) {
                        "todos" -> {
                            obtener_publicaciones_realizadas(
                                firebaseAuth.uid.toString(),
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
                            obtener_publicaciones_realizadas(
                                firebaseAuth.uid.toString(),
                                "privado", "No se encontraron datos"
                            )
                        }

                        "solo_seguidores" -> {
                            obtener_publicaciones_realizadas(
                                firebaseAuth.uid.toString(),
                                "solo_seguidores", "No se encontraron datos"
                            )
                        }
                    }

                    db.delete().addOnFailureListener { res ->
                        Log.d("elminadoSucces", "eliminado correctaemte")
                    }
                }.addOnFailureListener { e ->
                    binding.linealEncontrados.isVisible = true
                    binding.recicleViewTrabajos.isVisible = false
                    Toast.makeText(this, "Hubo uin error al mover ", Toast.LENGTH_SHORT).show()
                }
                // Aquí puedes usar `hashMap` para archivar, mover, actualizar o lo que necesites
                Log.d("HASHMAP_FINAL", hashMap.toString())
            }
        }.addOnFailureListener {
            binding.linealEncontrados.isVisible = true
            binding.recicleViewTrabajos.isVisible = false
            Log.e("Firestore", "Error al obtener la publicación: ${it.message}")
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
                    binding.recicleViewTrabajos.isVisible = false
                    maximo_min(valorMinUsuario, valorMaxUsuario)
                    Toast.makeText(this, "Todo bien, filtrando...", Toast.LENGTH_SHORT).show()
                    dialog.dismiss()
                }
            }
        }

        dialog.setContentView(view)
    }


    private fun obtener_publicaciones_realizadas(
        id: String,
        tipo: String,
        texto_sin_encontrar: String
    ) {

        binding.linealNoCuenta.isVisible = false
        binding.recicleViewTrabajos.isVisible = false
        val db = FirebaseFirestore.getInstance().collection(Variables.trabajadores_usuariosDB)
            .document(Variables.trabajadoresDB).collection(Variables.trabajadoresDB).document(id)
            .collection("publicaciones_trabajos").document(tipo).collection(tipo)
//        binding.loading.isVisible = true

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
//                binding.loading.isVisible = false
                binding.textoSinEncontrar.text = texto_sin_encontrar
                binding.linealNoCuenta.isVisible = true
                binding.linealEncontrados.isVisible = false
            } else {
//                binding.loading.isVisible = false
                binding.linealNoCuenta.isVisible = false
                binding.recicleViewTrabajos.isVisible = true
                binding.linealEncontrados.isVisible = false
                inicializarRecicle(binding.recicleViewTrabajos, adapter, this)
                adapter.notifyDataSetChanged() // Notifica al adaptador que los datos han cambiado
            }

        }.addOnFailureListener { e ->

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


    private fun editar_publicaciones(id_publicacion: String, tipo: String) {
        val crea_class = crear_publicaciones_recientes()
        val binding_bottomShet =
            BottomSheetEditarPublicacionesVerificadosBinding.inflate(LayoutInflater.from(this))
        val view = binding_bottomShet.root

        binding_bottomShet.cerrar.setOnClickListener { dialog.dismiss() }

        binding_bottomShet.idPublicacion.text = id_publicacion
        binding_bottomShet.copiarId.setOnClickListener {
            constantestextos_general.copiarTexto_portapapeles(
                binding_bottomShet.idPublicacion,
                this
            )
        }
        val tiempoInicio = System.currentTimeMillis()

        val db = FirebaseFirestore.getInstance().collection("Trabajadores_Usuarios_Drivers")
            .document("trabajadores").collection("trabajadores")
            .document(firebaseAuth.uid.toString())
            .collection("publicaciones_trabajos").document(tipo)
            .collection(tipo).document(id_publicacion)

        db.get().addOnSuccessListener { res ->
            val tiempoFin = System.currentTimeMillis()
            val tiempoTotal = tiempoFin - tiempoInicio
            Log.d("FirestoreTiempo", "Tiempo en obtener datos: $tiempoTotal ms")

            if (res.exists()) {
                val data = res.data
                val categoria = data?.get("categoria") as? String ?: ""
                val contenido = data?.get("contenido") as? String ?: ""
                val fecha_rec = data?.get("fecha_rec") as? String ?: ""
                val hora_rec = data?.get("hora_rec") as? String ?: ""
                val titulo = data?.get("titulo") as? String ?: ""
                val visivilidad = data?.get("visivilidad") as? String ?: ""
                val ubicacion = data?.get("ubicacion") as? String ?: ""
                val trabajos_publicados_hastags =
                    data?.get("hashtags_trabajos_publicados") as? List<String> ?: emptyList()
                val hashtags_generales =
                    data?.get("hashtags_generales") as? List<String> ?: emptyList()

                binding_bottomShet.complete.setText(categoria)
                binding_bottomShet.descripcionServiciosED.setText(contenido)
                binding_bottomShet.tituloPublicacionED.setText(titulo)
                binding_bottomShet.agregarHastagsED.setText(hashtags_generales.joinToString(", "))
                binding_bottomShet.agregarHastagsCategoriasED.setText(
                    trabajos_publicados_hastags.joinToString(
                        ", "
                    )
                )
                binding_bottomShet.agregarHastagsED.setOnClickListener {
                    Toast.makeText(this, "hacemos clij en agrega", Toast.LENGTH_SHORT).show()
                    dialog = BottomSheetDialog(this)
                    constantes_hastags_generales.obtener_hastags_generales(
                        this,
                        hashtagsGenerales,
                        dialog,
                        binding_bottomShet.agregarHastagsED
                    )
                    dialog.show()
                }

                binding_bottomShet.agregarHastagsCategoriasED.setOnClickListener {
                    dialog = BottomSheetDialog(this)
                    constantes_hastags_generales.obtenerHastags_cada_cat(
                        binding_bottomShet.complete.text.toString(),
                        dialog,
                        this,
                        hashtagsCategoria,
                        binding_bottomShet.agregarHastagsCategoriasED
                    )
                    dialog.show()
                }

                if (ubicacion.isNotEmpty()) {
                    binding_bottomShet.agregaUbicaciones.isChecked = true
                    binding_bottomShet.ubicacionAuto.isVisible = true
                    binding_bottomShet.ubicacionAutoP.isVisible = true
                    binding_bottomShet.ubicacionAuto.setText(ubicacion)
                    obtenerLocalidadUser { localidad ->

                        listaFiltrado(binding_bottomShet.ubicacionAuto)
                    }
                } else {
                    obtenerLocalidadUser { localidad ->
                        binding_bottomShet.ubicacionAuto.setText(localidad.toString())
                        listaFiltrado(binding_bottomShet.ubicacionAuto)
                    }
                    binding_bottomShet.ubicacionAutoP.isVisible = false
                    binding_bottomShet.agregaUbicaciones.isChecked = false
                    binding_bottomShet.ubicacionAuto.isVisible = false
                }



                binding_bottomShet.agregaUbicaciones.setOnCheckedChangeListener { _, isChecked ->
                    if (isChecked) {
                        binding_bottomShet.ubicacionAutoP.isVisible = true
                        binding_bottomShet.ubicacionAuto.isVisible = true
                        binding_bottomShet.ubicacionAuto.setText(ubicacion)

                    } else {
                        binding_bottomShet.ubicacionAuto.setText("")
                        binding_bottomShet.ubicacionAutoP.isVisible = false
                        binding_bottomShet.ubicacionAuto.isVisible = false
                    }
                }

                binding_bottomShet.editar.setOnClickListener {
                    if (validar_campos(binding_bottomShet)) {
                        subirCambiosRealziados(
                            id_publicacion,
                            binding_bottomShet,
                            dialog, tipo
                        )


                    }

                }
                Handler(Looper.getMainLooper()).postDelayed({
                    binding_bottomShet.netScrollView.isVisible = true
                    binding_bottomShet.cargandoContenido.isVisible = false

                }, tiempoTotal) // 2000 ms = 2 segundos
            }
        }.addOnFailureListener {
            Log.e("FirestoreTiempo", "Error al obtener datos: ${it.message}")
        }

        dialog.setContentView(view)
    }

    private fun validar_campos(binding_bottomShet: BottomSheetEditarPublicacionesVerificadosBinding): Boolean {
        var esValido = true

        val tituloTrabajo = binding_bottomShet.tituloPublicacionED.text.toString().trim()
        val descripcion = binding_bottomShet.descripcionServiciosED.text.toString().trim()
        val agregarHashtags = binding_bottomShet.agregarHastagsED.text.toString().trim()
        val agregarHashtagsCat =
            binding_bottomShet.agregarHastagsCategoriasED.text.toString().trim()
        val categoria = binding_bottomShet.complete.text.toString().trim()
        val ubicacion = binding_bottomShet.ubicacionAuto.text.toString().trim()

        // Validar título
        if (tituloTrabajo.isEmpty()) {
            binding_bottomShet.tituloPublicacionED.error = "Este campo es obligatorio"
            binding_bottomShet.tituloPublicacionED.requestFocus()
            esValido = false
        }

        // Validar descripción
        if (descripcion.isEmpty()) {
            binding_bottomShet.descripcionServiciosED.error = "Este campo es obligatorio"
            binding_bottomShet.descripcionServiciosED.requestFocus()
            esValido = false
        }

        // Validar hashtags generales
        if (agregarHashtags.isEmpty()) {
            binding_bottomShet.agregarHastagsED.error = "Este campo es obligatorio"
            binding_bottomShet.agregarHastagsED.requestFocus()
            esValido = false
        }

        // Validar hashtags por categoría
        if (agregarHashtagsCat.isEmpty()) {
            binding_bottomShet.agregarHastagsCategoriasED.error = "Este campo es obligatorio"
            binding_bottomShet.agregarHastagsCategoriasED.requestFocus()
            esValido = false
        }

        // Validar categoría
        if (categoria.isEmpty()) {
            binding_bottomShet.complete.error = "Seleccione una categoría"
            binding_bottomShet.complete.requestFocus()
            esValido = false
        }

        if (binding_bottomShet.agregaUbicaciones.isChecked) {
            if (ubicacion.isEmpty()) {
                binding_bottomShet.ubicacionAuto.error = "Seleccione una ubicación"
                binding_bottomShet.ubicacionAuto.requestFocus()

                esValido = false
            } else if (ubicacion == "General") {
                binding_bottomShet.ubicacionAuto.requestFocus()
                binding_bottomShet.ubicacionAuto.error = "No puede seleccionar 'General'"
                Toast.makeText(
                    binding_bottomShet.root.context,
                    "No puede seleccionar 'General' como ubicación",
                    Toast.LENGTH_SHORT
                ).show()
                esValido = false
            }
        }


        return esValido
    }

    private fun subirCambiosRealziados(
        id_publicacion: String,
        binding_bottomShet: BottomSheetEditarPublicacionesVerificadosBinding,
        dialog: BottomSheetDialog, tipo: String
    ) {
        val db = FirebaseFirestore.getInstance().collection("Trabajadores_Usuarios_Drivers")
            .document("trabajadores").collection("trabajadores")
            .document(firebaseAuth.uid.toString())
            .collection("publicaciones_trabajos").document(tipo)
            .collection(tipo).document(id_publicacion)

        val hashtagsGenerales = binding_bottomShet.agregarHastagsED.text.toString()
            .split(",")
            .map { it.trim() }
            .filter { it.isNotBlank() }

        val hashtagscategorias = binding_bottomShet.agregarHastagsCategoriasED.text.toString()
            .split(",")
            .map { it.trim() }
            .filter { it.isNotBlank() }
        val hasmap = hashMapOf(
            "hashtags_generales" to hashtagsGenerales,
            "hashtags_trabajos_publicados" to hashtagscategorias,
            "contenido" to binding_bottomShet.descripcionServiciosED.text.toString(),
            "titulo" to binding_bottomShet.tituloPublicacionED.text.toString(),
            "ubicacion" to binding_bottomShet.ubicacionAuto.text.toString()
        )

        db.set(hasmap, SetOptions.merge()).addOnSuccessListener { res ->
            dialog.dismiss()
            Toast.makeText(this, "Campos actualizados correctamente", Toast.LENGTH_SHORT).show()
            binding.linealEncontrados.isVisible = true

            when (tipo) {
                "privado" -> {
                    obtener_publicaciones_realizadas(
                        firebaseAuth.uid.toString(),
                        "privado", "No se encontraron datos"
                    )
                }

                "publicados" -> {
                    obtener_publicaciones_realizadas(
                        firebaseAuth.uid.toString(),
                        "publicados", "No se encontraron datos"
                    )
                }

                "solo_seguidores" -> {
                    obtener_publicaciones_realizadas(
                        firebaseAuth.uid.toString(),
                        "solo_seguidores", "No se encontraron datos"
                    )
                }
            }

        }
            .addOnFailureListener { e ->
                Toast.makeText(
                    this,
                    "Ocurrio un error al editar la publicacion",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }

}