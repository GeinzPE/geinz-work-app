package com.example.geinzwork.vistaTrabajador

import android.content.Context
import android.os.Bundle
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
import com.example.geinzwork.crear_publicaciones_recientes
import com.example.geinzwork.dataclass.dataclas_trabajos_ralizados_verificados
import com.geinzz.geinzwork.R
import com.geinzz.geinzwork.constantesGeneral.constantestextos_general
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
        obtener_publicaciones_realizadas(firebaseAuth.uid.toString())
        adapter = adapter_pbl_vr_tb_recientes(lista, { item ->
            dialog = BottomSheetDialog(this)
            bottomSheet_editar_eliminar_Arhivar_estadi(item)
            dialog.show()
        })
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

    }

    private fun filtrar_publicaciones(min: Int, max: Int, filtado_pasado: String) {
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
            lista.clear() // Asegúrate de limpiar la lista antes de agregar nuevos datos

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
                Log.d("Valores", "No se encontraron datos válidos para el campo: $campoFiltrado")
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
        val archivar = bottoSheet.archivar
        if (binding.masClicks.isChecked) {
            archivar.setOnClickListener {
                dialog.dismiss()
                archivar_eliminar_publicaciones(
                    item.id_publicacion.toString(),
                    "publicados",
                    "archivados", "mascliks"
                )
            }
        }
        if (binding.masVistas.isChecked) {

        }
        if (binding.masCompartidas.isChecked) {

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
                editar_publicaciones(item.id_publicacion.toString())
            }
            archivar.setOnClickListener {
                dialog.dismiss()
                archivar_eliminar_publicaciones(

                    item.id_publicacion.toString(),
                    "publicados",
                    "archivados", "todos"
                )
            }
        }


        dialog.setContentView(view)
    }


    private fun archivar_eliminar_publicacion(idSeleccionado: String, tipo: String) {
        val firestore = FirebaseFirestore.getInstance()
        val uid = firebaseAuth.uid.toString()

        val refPublicacion = firestore.collection("Trabajadores_Usuarios_Drivers")
            .document("trabajadores").collection("trabajadores")
            .document(uid).collection("publicaciones_trabajos")
            .document("publicados").collection("publicados").document(idSeleccionado)

        refPublicacion.get().addOnSuccessListener { res ->
            if (res.exists()) {
                val data = res.data

                val categoria = data?.get("categoria") as? String ?: ""
                val contenido = data?.get("contenido") as? String ?: ""
                val fechaRec = data?.get("fecha_rec") as? String ?: ""
                val horaRec = data?.get("hora_rec") as? String ?: ""
                val titulo = data?.get("titulo") as? String ?: ""
                val visibilidad = data?.get("visivilidad") as? String ?: ""
                val ubicacion = data?.get("ubicacion") as? String ?: ""
                val id = data?.get("id") as? String ?: ""

                val hashtagsGenerales =
                    data?.get("hashtags_generales") as? List<String> ?: emptyList()
                val hashtagsTrabajosPublicados =
                    data?.get("hashtags_trabajos_publicados") as? List<String> ?: emptyList()

                // Creamos el hashmap con los datos para guardar en otra colección (archivados o publicados)
                val hashMap = hashMapOf(
                    "categoria" to categoria,
                    "contenido" to contenido,
                    "fecha_rec" to fechaRec,
                    "hora_rec" to horaRec,
                    "titulo" to titulo,
                    "visivilidad" to visibilidad,
                    "ubicacion" to ubicacion,
                    "id" to id,
                    "hashtags_generales" to hashtagsGenerales,
                    "hashtags_trabajos_publicados" to hashtagsTrabajosPublicados
                )

                // Aquí decides si archivar o publicar de nuevo según `tipo`

                val refDestino = firestore.collection("Trabajadores_Usuarios_Drivers")
                    .document("trabajadores").collection("trabajadores")
                    .document(uid).collection("publicaciones_trabajos")
                    .document(tipo).collection(tipo).document(idSeleccionado)

                refDestino.set(hashMap).addOnSuccessListener {
                    refPublicacion.delete().addOnSuccessListener {
                        Toast.makeText(this, "Publicación movida a $tipo", Toast.LENGTH_SHORT)
                            .show()

                    }
                }
            }
        }.addOnFailureListener {
            Toast.makeText(this, "Error al obtener datos: ${it.message}", Toast.LENGTH_SHORT).show()
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
                            obtener_publicaciones_realizadas(firebaseAuth.uid.toString())
                        }

                        "mascliks" -> {
                            filtrar_publicaciones(
                                binding.min.text.toString().toInt(),
                                binding.max.text.toString().toInt(),
                                "estadisticas_click"
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

                }
            }
        }

        dialog.setContentView(view)
    }


    private fun obtener_publicaciones_realizadas(id: String) {
        val db = FirebaseFirestore.getInstance().collection(Variables.trabajadores_usuariosDB)
            .document(Variables.trabajadoresDB).collection(Variables.trabajadoresDB).document(id)
            .collection("publicaciones_trabajos").document("publicados").collection("publicados")
//        binding.loading.isVisible = true
        binding.linealNoCuenta.isVisible = false
        binding.recicleViewTrabajos.isVisible = false
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
                binding.linealNoCuenta.isVisible = true
            } else {
//                binding.loading.isVisible = false
                binding.linealNoCuenta.isVisible = false
                binding.recicleViewTrabajos.isVisible = true
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


    private fun editar_publicaciones(id_publicacion: String) {
        val crea_class = crear_publicaciones_recientes()
        val binding_bottomShet =
            BottomSheetEditarPublicacionesVerificadosBinding.inflate(LayoutInflater.from(this))
        val view = binding_bottomShet.root
        binding_bottomShet.cerrar.setOnClickListener { dialog.dismiss() }
        val db = FirebaseFirestore.getInstance().collection("Trabajadores_Usuarios_Drivers")
            .document("trabajadores").collection("trabajadores")
            .document(firebaseAuth.uid.toString())
            .collection("publicaciones_trabajos").document(id_publicacion)
        db.get().addOnSuccessListener { res ->
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
                binding_bottomShet.agregarHastagsED.setText(
                    hashtags_generales.joinToString(
                        ", "
                    )
                )
                binding_bottomShet.agregarHastagsCategoriasED.setText(
                    trabajos_publicados_hastags.joinToString(
                        ", "
                    )
                )

                if (ubicacion.isNotEmpty()) {
                    binding_bottomShet.agregaUbicaciones.isChecked = true
                    binding_bottomShet.agregaUbi.isVisible = true
                    binding_bottomShet.agregaUbiED.setText(ubicacion)
                } else {
                    binding_bottomShet.agregaUbicaciones.isChecked = false
                    binding_bottomShet.agregaUbi.isVisible = false
                }
                when (visivilidad.lowercase()) {
                    "todos" -> binding_bottomShet.Todos.isChecked = true
                    "seguidores" -> binding_bottomShet.seguidores.isChecked = true
                    "privado" -> binding_bottomShet.privado.isChecked = true
                }

                binding_bottomShet.grupoVisibilidad.setOnCheckedChangeListener { _, checkedId ->
                    val texto = when (checkedId) {
                        R.id.Todos -> "Todos"
                        R.id.seguidores -> "Seguidores"
                        R.id.privado -> "Privado"
                        else -> ""
                    }
                    binding_bottomShet.mostrarPublicacionPara.text = texto
                }

                binding_bottomShet.agregaUbicaciones.setOnCheckedChangeListener { _, isChecked ->
                    if (isChecked) {
                        binding_bottomShet.agregaUbi.isVisible = true
                    } else {
                        binding_bottomShet.agregaUbi.isVisible = false
                        binding_bottomShet.agregaUbiED.setText("")
                    }
                }

                binding_bottomShet.editar.setOnClickListener {
                    subirCambiosRealziados(
                        id_publicacion,
                        binding_bottomShet,
                        dialog
                    )
                }

            }
        }
        dialog.setContentView(view)
    }

    private fun subirCambiosRealziados(
        id_publicacion: String,
        binding_bottomShet: BottomSheetEditarPublicacionesVerificadosBinding,
        dialog: BottomSheetDialog
    ) {
        val db = FirebaseFirestore.getInstance().collection("Trabajadores_Usuarios_Drivers")
            .document("trabajadores").collection("trabajadores")
            .document(firebaseAuth.uid.toString())
            .collection("publicaciones_trabajos").document(id_publicacion)

        val hasmap = hashMapOf(
            "contenido" to binding_bottomShet.descripcionServiciosED.text.toString(),
            "titulo" to binding_bottomShet.tituloPublicacionED.text.toString(),
            "visivilidad" to binding_bottomShet.mostrarPublicacionPara.text.toString(),
            "ubicacion" to binding_bottomShet.agregaUbiED.text.toString()
        )
        db.set(hasmap, SetOptions.merge()).addOnSuccessListener { res ->
            Toast.makeText(this, "Campos actualizados correctamente", Toast.LENGTH_SHORT).show()
            dialog.dismiss()
            obtener_publicaciones_realizadas(firebaseAuth.uid.toString())
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