package com.geinzz.geinzwork

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.geinzwork.constantesGeneral.Variables
import com.example.geinzwork.constantesGeneral.constatnes_carga_imagenes_general
import com.example.geinzwork.dataclass.dataclas_trabajos_ralizados_verificados
import com.geinzz.geinzwork.adapterViewholder.publicaciones_ralizadas
import com.geinzz.geinzwork.databinding.ActivityVerPublicacionesBinding
import com.geinzz.geinzwork.databinding.BottomSheetCamposTrPdPBinding
import com.geinzz.geinzwork.databinding.BottomSheetEditarPublicacionBinding
import com.geinzz.geinzwork.dataclass.dataclas_trabajos_ralizados
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ver_publicaciones : AppCompatActivity() {
    private lateinit var binding: ActivityVerPublicacionesBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private var listAdapter = mutableListOf<dataclas_trabajos_ralizados>()
    private lateinit var adapter: publicaciones_ralizadas
    private lateinit var dialog: BottomSheetDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityVerPublicacionesBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        firebaseAuth = FirebaseAuth.getInstance()
        adapter = publicaciones_ralizadas(listAdapter, { item ->
            dialog = BottomSheetDialog(
                this
            )
            bottomSheet_editar_eliminar_Arhivar_estadi(item)
            dialog.show()

        })

        obtenerPublicaciones(
            firebaseAuth.uid.toString(),
            listAdapter,
            binding.recicleViewTrabajos,
            this,
            adapter,
            binding.linealNoCuenta,
        )
        binding.CreaPublicacion.setOnClickListener {
            onBackPressed()
        }
    }

    private fun bottomSheet_editar_eliminar_Arhivar_estadi(item: dataclas_trabajos_ralizados) {
        val bottoSheet = BottomSheetCamposTrPdPBinding.inflate(LayoutInflater.from(this))
        val view = bottoSheet.root
        val eliminar = bottoSheet.eliminar
        val estadisticas = bottoSheet.estadisticas
        val editar = bottoSheet.editar
        val archivar = bottoSheet.archivar

        eliminar.setOnClickListener {
            eliminar_archivar(item.id_publicacion.toString(),"eliminados")

        }
        editar.setOnClickListener {
//            editar_publicaciones(item.id_publicacion.toString())
        }
        archivar.setOnClickListener {
            eliminar_archivar(item.id_publicacion.toString(),"archivados")
        }

        dialog.setContentView(view)
    }


    private fun eliminar_archivar(idSeleccionado: String, tipo: String) {
        val firestore = FirebaseFirestore.getInstance()
        val uid = firebaseAuth.uid.toString()

        // Referencia al documento original
        val refOrigen = firestore.collection("Trabajadores_Usuarios_Drivers")
            .document("trabajadores").collection("trabajadores")
            .document(uid).collection("trabajos_realizados")
            .document("publicados").collection("publicados").document(idSeleccionado)

        // Obtener los datos del documento
        refOrigen.get().addOnSuccessListener { res ->
            if (res.exists()) {
                val data = res.data ?: return@addOnSuccessListener

                // Creamos el HashMap con los campos esperados
                val hashMap = hashMapOf<String, Any>(
                    Variables.titulo to (data[Variables.titulo] ?: ""),
                    Variables.descripcion to (data[Variables.descripcion] ?: ""),
                    Variables.hora to (data[Variables.hora] ?: ""),
                    Variables.fecha to (data[Variables.fecha] ?: ""),
                    Variables.id to (data[Variables.id] ?: "")
                )

                // Determinar la nueva colección destino
                val refDestino = firestore.collection("Trabajadores_Usuarios_Drivers")
                    .document("trabajadores").collection("trabajadores")
                    .document(uid).collection("trabajos_realizados")
                    .document(tipo).collection(tipo).document(idSeleccionado)

                // Mover el documento
                refDestino.set(hashMap).addOnSuccessListener {
                    // Eliminar el documento original
                    refOrigen.delete().addOnSuccessListener {
                        Toast.makeText(this, "Trabajo movido a $tipo", Toast.LENGTH_SHORT).show()
                    }.addOnFailureListener {
                        Toast.makeText(this, "Error al eliminar: ${it.message}", Toast.LENGTH_SHORT).show()
                    }
                }.addOnFailureListener {
                    Toast.makeText(this, "Error al mover: ${it.message}", Toast.LENGTH_SHORT).show()
                }

            } else {
                Toast.makeText(this, "El documento no existe", Toast.LENGTH_SHORT).show()
            }
        }.addOnFailureListener {
            Toast.makeText(this, "Error al obtener datos: ${it.message}", Toast.LENGTH_SHORT).show()
        }
    }


    private fun editarPublicacion(item: dataclas_trabajos_ralizados) {
        val bindingBottomShet =
            BottomSheetEditarPublicacionBinding.inflate(LayoutInflater.from(this))
        val view = bindingBottomShet.root

        val dialog = BottomSheetDialog(this)
        dialog.setContentView(view)

        bindingBottomShet.cerrar.setOnClickListener {
            dialog.dismiss()
        }

        bindingBottomShet.editar.setOnClickListener {
            editar_info(
                dialog,
                item.id_publicacion.toString(),
                bindingBottomShet.tituloPublicacionED.text.toString(),
                bindingBottomShet.descripcionServiciosED.text.toString()
            )

        }


        val placeholderperfil = ContextCompat.getDrawable(this, R.drawable.cargando_img)
        constatnes_carga_imagenes_general.changer_img(
            bindingBottomShet.progressCargaImagen,
            this,
            item.img.toString(),
            null,
            bindingBottomShet.imagenTrabajo,
            "portada", placeholderperfil
        ) {}
        bindingBottomShet.tituloPublicacionED.setText(item.titulo)
        bindingBottomShet.descripcionServiciosED.setText(item.contenido)

        dialog.show()
    }

    private fun editar_info(
        bindingBottomShet: BottomSheetDialog,
        idPublicacion: String, nuevoTitulo: String, nuevaDescripcion: String
    ) {
        val db = FirebaseFirestore.getInstance().collection(Variables.trabajadores_usuariosDB)
            .document(Variables.trabajadoresDB)
            .collection(Variables.trabajadoresDB)
            .document(firebaseAuth.uid.toString())
            .collection(Variables.trabajos_realizados)
            .document(idPublicacion)
        val updates = hashMapOf<String, Any>(
            Variables.titulo to nuevoTitulo,
            Variables.descripcion to nuevaDescripcion
        )
        db.update(updates)
            .addOnSuccessListener {
                Toast.makeText(this, "Campos actualizados correctamente", Toast.LENGTH_SHORT).show()
                obtenerPublicaciones(
                    firebaseAuth.uid.toString(),
                    listAdapter,
                    binding.recicleViewTrabajos,
                    this,
                    adapter,
                    binding.linealNoCuenta,
                )
                bindingBottomShet.dismiss()
            }

            .addOnFailureListener { e ->
                Toast.makeText(
                    this,
                    "Error al actualizar los campos: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }

    private fun eliminarPublicacion(item: dataclas_trabajos_ralizados) {
        AlertDialog.Builder(this)
            .setTitle("Eliminar publicación")
            .setMessage("¿Estás seguro de que quieres eliminar esta publicación?")
            .setPositiveButton("Sí") { dialog, which ->
                // El usuario confirmó, eliminar la publicación
                val db =
                    FirebaseFirestore.getInstance().collection(Variables.trabajadores_usuariosDB)
                        .document(Variables.trabajadoresDB)
                        .collection(Variables.trabajadoresDB)
                        .document(firebaseAuth.uid.toString())
                        .collection(Variables.trabajos_realizados)
                        .document(item.id_publicacion.toString())
                db.delete().addOnSuccessListener {
                    Toast.makeText(
                        this,
                        "Publicación eliminada correctamente",
                        Toast.LENGTH_SHORT
                    ).show()
                    obtenerPublicaciones(
                        firebaseAuth.uid.toString(),
                        listAdapter,
                        binding.recicleViewTrabajos,
                        this,
                        adapter,
                        binding.linealNoCuenta,
                    )
                }.addOnFailureListener {
                    Toast.makeText(
                        this,
                        "Ocurrió un error al eliminar la publicación",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
            .setNegativeButton("No") { dialog, which ->
                // El usuario canceló, cerrar el diálogo
                dialog.dismiss()
            }
            .show()
    }


    fun obtenerPublicaciones(
        id: String,
        lista: MutableList<dataclas_trabajos_ralizados>,
        recicleTrabajosRealizados: RecyclerView,
        context: Context,
        adapter: publicaciones_ralizadas, // Cambiado a publicaciones_ralizadas
        lineal_no_cuenta: LinearLayout
    ) {
        val db = FirebaseFirestore.getInstance().collection(Variables.trabajadores_usuariosDB)
            .document(Variables.trabajadoresDB).collection(Variables.trabajadoresDB).document(id)
            .collection(Variables.trabajos_realizados).document("publicados").collection("publicados")

        binding.loading.isVisible = true
        lineal_no_cuenta.isVisible = false
        recicleTrabajosRealizados.isVisible = false

        db.get().addOnSuccessListener { result ->
            lista.clear()
            if (result.isEmpty) {
                binding.loading.isVisible = false
                lineal_no_cuenta.isVisible = true
            } else {
                for (datos in result) {
                    val data = datos.data
                    val titulo = data?.get(Variables.titulo) as? String ?: ""
                    val imageUrl = data?.get(Variables.imageUrl) as? String ?: ""
                    val id = data?.get(Variables.id) as? String ?: ""
                    val hora = data?.get(Variables.hora) as? String ?: ""
                    val fecha = data?.get(Variables.fecha) as? String ?: ""
                    val descripcion = data?.get(Variables.descripcion) as? String ?: ""
                    val trabajoRealizado =
                        dataclas_trabajos_ralizados(imageUrl, titulo, descripcion, fecha, hora, id)
                    lista.add(trabajoRealizado)
                }
                if (lista.isEmpty()) {
                    binding.loading.isVisible = false
                    lineal_no_cuenta.isVisible = true
                } else {
                    binding.loading.isVisible = false
                    lineal_no_cuenta.isVisible = false
                    recicleTrabajosRealizados.isVisible = true
                    inicializarRecicle(recicleTrabajosRealizados, adapter, context)
                    binding.linealappLayout.isVisible = true
                    adapter.notifyDataSetChanged() // Notifica al adaptador que los datos han cambiado
                }
            }
        }.addOnFailureListener {
            binding.loading.isVisible = false
            lineal_no_cuenta.isVisible = true
        }
    }

    private fun inicializarRecicle(
        recycle: RecyclerView,
        adapter: publicaciones_ralizadas, // Cambiado a publicaciones_ralizadas
        context: Context
    ) {
        recycle.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        recycle.adapter = adapter
    }
}