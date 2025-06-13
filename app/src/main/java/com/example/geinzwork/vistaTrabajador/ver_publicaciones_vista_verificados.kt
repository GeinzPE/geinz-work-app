package com.example.geinzwork.vistaTrabajador

import android.content.Context
import android.os.Bundle
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
import com.geinzz.geinzwork.databinding.ActivityVerPublicacionesVistaVerificadosBinding
import com.geinzz.geinzwork.databinding.BottomSheetCamposTrPdPBinding
import com.geinzz.geinzwork.databinding.BottomSheetEditarPublicacionesVerificadosBinding

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
    }

    private fun bottomSheet_editar_eliminar_Arhivar_estadi(item: dataclas_trabajos_ralizados_verificados) {
        val bottoSheet = BottomSheetCamposTrPdPBinding.inflate(LayoutInflater.from(this))
        val view = bottoSheet.root
        val eliminar = bottoSheet.eliminar
        val estadisticas = bottoSheet.estadisticas
        val editar = bottoSheet.editar
        val archivar = bottoSheet.archivar

        eliminar.setOnClickListener {
            archivar_eliminar_publicacion(item.id_publicacion.toString(),"eliminados")

        }
        editar.setOnClickListener {
            editar_publicaciones(item.id_publicacion.toString())
        }
        archivar.setOnClickListener {
            archivar_eliminar_publicacion(item.id_publicacion.toString(),"archivados")
        }

        dialog.setContentView(view)
    }

//    private fun eliminarPublicacion(item: dataclas_trabajos_ralizados_verificados) {
//        AlertDialog.Builder(this)
//            .setTitle("Eliminar publicación")
//            .setMessage("¿Estás seguro de que quieres eliminar esta publicación?")
//            .setPositiveButton("Sí") { dialog, which ->
//                // El usuario confirmó, eliminar la publicación
//                val db =
//                    FirebaseFirestore.getInstance().collection(Variables.trabajadores_usuariosDB)
//                        .document(Variables.trabajadoresDB).collection(Variables.trabajadoresDB)
//                        .document(firebaseAuth.uid.toString())
//                        .collection("publicaciones_trabajos")
//                        .document(item.id_publicacion.toString())
//                db.delete().addOnSuccessListener {
//                    Toast.makeText(
//                        this,
//                        "Publicación eliminada correctamente",
//                        Toast.LENGTH_SHORT
//                    ).show()
//                    obtener_publicaciones_realizadas(firebaseAuth.uid.toString())
//                }.addOnFailureListener {
//                    Toast.makeText(
//                        this,
//                        "Ocurrió un error al eliminar la publicación",
//                        Toast.LENGTH_SHORT
//                    ).show()
//                }
//            }
//            .setNegativeButton("No") { dialog, which ->
//                // El usuario canceló, cerrar el diálogo
//                dialog.dismiss()
//            }
//            .show()
//
//    }

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

                val hashtagsGenerales = data?.get("hashtags_generales") as? List<String> ?: emptyList()
                val hashtagsTrabajosPublicados = data?.get("hashtags_trabajos_publicados") as? List<String> ?: emptyList()

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
                        Toast.makeText(this, "Publicación movida a $tipo", Toast.LENGTH_SHORT).show()

                }
            }}
        }.addOnFailureListener {
            Toast.makeText(this, "Error al obtener datos: ${it.message}", Toast.LENGTH_SHORT).show()
        }
    }


    private fun obtener_publicaciones_realizadas(id: String) {
        val db = FirebaseFirestore.getInstance().collection(Variables.trabajadores_usuariosDB)
            .document(Variables.trabajadoresDB).collection(Variables.trabajadoresDB).document(id)
            .collection("publicaciones_trabajos").document("publicados").collection("publicados")
        binding.loading.isVisible = true
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
                binding.loading.isVisible = false
                binding.linealNoCuenta.isVisible = true
            } else {
                binding.loading.isVisible = false
                binding.linealNoCuenta.isVisible = false
                binding.recicleViewTrabajos.isVisible = true
                inicializarRecicle(binding.recicleViewTrabajos, adapter, this)
                binding.linealappLayout.isVisible = true
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