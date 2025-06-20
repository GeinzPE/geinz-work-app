package com.geinzz.geinzwork

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
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
import androidx.viewbinding.ViewBinding
import com.example.geinzwork.constantesGeneral.Variables
import com.example.geinzwork.constantesGeneral.constatnes_carga_imagenes_general
import com.example.geinzwork.dataclass.dataclas_trabajos_ralizados_verificados
import com.example.geinzwork.publicaciones_trabajadores.mostrarTodosTrabajos
import com.geinzz.geinzwork.adapterViewholder.publicaciones_ralizadas
import com.geinzz.geinzwork.constantesGeneral.constantesCarrito
import com.geinzz.geinzwork.constantesGeneral.constantestextos_general
import com.geinzz.geinzwork.databinding.ActivityVerPublicacionesBinding
import com.geinzz.geinzwork.databinding.BottomSheetCamposTrPdPBinding
import com.geinzz.geinzwork.databinding.BottomSheetEditarPublicacionBinding
import com.geinzz.geinzwork.databinding.BottomSheetMostarTrabajosRecientesBinding
import com.geinzz.geinzwork.databinding.ItemCustomFixedSizeLayout2Binding
import com.geinzz.geinzwork.dataclass.dataclas_trabajos_ralizados
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import org.imaginativeworld.whynotimagecarousel.listener.CarouselListener
import org.imaginativeworld.whynotimagecarousel.model.CarouselItem
import org.imaginativeworld.whynotimagecarousel.utils.setImage

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
            bottomSheet_editar_eliminar_Arhivar_estadi(item, "publicados")
            dialog.show()

        })

        obtenerPublicaciones(
            "publicados",
            firebaseAuth.uid.toString(),
            listAdapter,
            binding.recicleViewTrabajos,
            this,
            adapter,
            binding.linealNoCuenta,
        )
        binding.todos.setOnClickListener {
            obtenerPublicaciones(
                "publicados",
                firebaseAuth.uid.toString(),
                listAdapter,
                binding.recicleViewTrabajos,
                this,
                adapter,
                binding.linealNoCuenta,
            )
        }
        binding.chipPublicados.setOnClickListener {
            obtenerPublicaciones(
                "publicados",
                firebaseAuth.uid.toString(),
                listAdapter,
                binding.recicleViewTrabajos,
                this,
                adapter,
                binding.linealNoCuenta,
            )
        }
        binding.chipEliminados.setOnClickListener {
            obtenerPublicaciones(
                "eliminados",
                firebaseAuth.uid.toString(),
                listAdapter,
                binding.recicleViewTrabajos,
                this,
                adapter,
                binding.linealNoCuenta,
            )
        }
        binding.chipArchivados.setOnClickListener {
            obtenerPublicaciones(
                "archivados",
                firebaseAuth.uid.toString(),
                listAdapter,
                binding.recicleViewTrabajos,
                this,
                adapter,
                binding.linealNoCuenta,
            )
        }

    }


    private fun bottomSheet_editar_eliminar_Arhivar_estadi(
        item: dataclas_trabajos_ralizados,
        tipo: String
    ) {

        val bottoSheet = BottomSheetCamposTrPdPBinding.inflate(LayoutInflater.from(this))
        val view = bottoSheet.root
        val eliminar = bottoSheet.eliminar
        val estadisticas = bottoSheet.estadisticas
        val editar = bottoSheet.editar
        val archivar = bottoSheet.archivar
        bottoSheet.vistaPrevia.isVisible = false
        bottoSheet.privado.isVisible = false
        bottoSheet.soloSeguidores.isVisible = false

        bottoSheet.idPublicacion.text = item.id_publicacion.toString()
        bottoSheet.copiarId.setOnClickListener {
            constantestextos_general.copiarTexto_portapapeles(
                bottoSheet.idPublicacion,
                this
            )
        }
        var tipo_encontrado = ""
        if (binding.chipEliminados.isChecked) {
            Toast.makeText(this, "eliminado", Toast.LENGTH_SHORT).show()
            bottoSheet.linealIconosPrincipal.isVisible = false
            bottoSheet.eliminarPermanente.isVisible = true
            bottoSheet.regresar.isVisible = true
            tipo_encontrado = "eliminados"

        } else if (binding.chipArchivados.isChecked) {
            Toast.makeText(this, "archiavdo", Toast.LENGTH_SHORT).show()
            bottoSheet.linealIconosPrincipal.isVisible = false
            bottoSheet.eliminarPermanente.isVisible = false
            bottoSheet.regresar.isVisible = true
            tipo_encontrado = "archivados"
        }

        eliminar.setOnClickListener {
            eliminar_archivar(item.id_publicacion.toString(), "eliminados")
            dialog.dismiss()
        }

        editar.setOnClickListener {
            editarPublicacion(item, tipo)
            dialog.dismiss()

        }
        archivar.setOnClickListener {
            eliminar_archivar(item.id_publicacion.toString(), "archivados")
            dialog.dismiss()

        }
        bottoSheet.eliminarPermanente.setOnClickListener {
            val firestore = FirebaseFirestore.getInstance()
            val uid = firebaseAuth.uid.toString()
            val refOrigen = firestore.collection("Trabajadores_Usuarios_Drivers")
                .document("trabajadores").collection("trabajadores")
                .document(uid).collection("trabajos_realizados")
                .document("eliminados").collection("eliminados")
                .document(item.id_publicacion.toString())
            refOrigen.delete().addOnSuccessListener { res ->
                Toast.makeText(this, "publicacion eliminado correctamente", Toast.LENGTH_SHORT)
                    .show()
                obtenerPublicaciones(
                    "eliminados",
                    firebaseAuth.uid.toString(),
                    listAdapter,
                    binding.recicleViewTrabajos,
                    this,
                    adapter,
                    binding.linealNoCuenta,
                )
            }.addOnFailureListener { e ->
                Toast.makeText(this, "Error al eliminar la publicacion", Toast.LENGTH_SHORT)
                    .show()
            }
            dialog.dismiss()
        }
        bottoSheet.regresar.setOnClickListener {
            activar_publicacion(tipo_encontrado, bottoSheet.idPublicacion.text.toString())
        }
        estadisticas.isVisible = false

        dialog.setContentView(view)
    }

    private fun activar_publicacion(tipo: String, idPublicacion: String) {
        val firestore = FirebaseFirestore.getInstance()
        val uid = firebaseAuth.uid.toString()

        val refOrigen = firestore.collection("Trabajadores_Usuarios_Drivers")
            .document("trabajadores").collection("trabajadores")
            .document(uid).collection("trabajos_realizados")
            .document(tipo).collection(tipo).document(idPublicacion)

        refOrigen.get().addOnSuccessListener { res ->
            if (res.exists()) {
                val data = res.data ?: return@addOnSuccessListener

                val hashMap = hashMapOf<String, Any>()

                hashMap[Variables.titulo] = data[Variables.titulo] ?: ""
                hashMap[Variables.descripcion] = data[Variables.descripcion] ?: ""
                hashMap[Variables.hora] = data[Variables.hora] ?: ""
                hashMap[Variables.fecha] = data[Variables.fecha] ?: ""
                hashMap[Variables.id] = data[Variables.id] ?: ""

                for ((key, value) in data) {
                    if (key.startsWith("img_url") && value is String) {
                        hashMap[key] = value
                    }
                }

                val refDestino = firestore.collection("Trabajadores_Usuarios_Drivers")
                    .document("trabajadores").collection("trabajadores")
                    .document(uid).collection("trabajos_realizados")
                    .document("publicados").collection("publicados").document(idPublicacion)

                refDestino.set(hashMap).addOnSuccessListener {
                    refOrigen.delete().addOnSuccessListener {
                        Toast.makeText(this, "Trabajo movido correctamente", Toast.LENGTH_SHORT)
                            .show()
                        binding.linealNoCuenta.isVisible = false
                        binding.recicleViewTrabajos.isVisible = false
                        obtenerPublicaciones(
                            tipo,
                            firebaseAuth.uid.toString(),
                            listAdapter,
                            binding.recicleViewTrabajos,
                            this,
                            adapter,
                            binding.linealNoCuenta,
                        )
                        dialog.dismiss()
                    }.addOnFailureListener {
                        Toast.makeText(this, "Error al eliminar: ${it.message}", Toast.LENGTH_SHORT)
                            .show()
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


    private fun eliminar_archivar(idSeleccionado: String, tipo: String) {
        val firestore = FirebaseFirestore.getInstance()
        val uid = firebaseAuth.uid.toString()

        val refOrigen = firestore.collection("Trabajadores_Usuarios_Drivers")
            .document("trabajadores").collection("trabajadores")
            .document(uid).collection("trabajos_realizados")
            .document("publicados").collection("publicados").document(idSeleccionado)

        refOrigen.get().addOnSuccessListener { res ->
            if (res.exists()) {
                val data = res.data ?: return@addOnSuccessListener

                val hashMap = hashMapOf<String, Any>()

                // Copiar campos comunes
                hashMap[Variables.titulo] = data[Variables.titulo] ?: ""
                hashMap[Variables.descripcion] = data[Variables.descripcion] ?: ""
                hashMap[Variables.hora] = data[Variables.hora] ?: ""
                hashMap[Variables.fecha] = data[Variables.fecha] ?: ""
                hashMap[Variables.id] = data[Variables.id] ?: ""

                // Extraer todas las claves que empiecen con "img_url"
                for ((key, value) in data) {
                    if (key.startsWith("img_url") && value is String) {
                        hashMap[key] = value
                    }
                }

                val refDestino = firestore.collection("Trabajadores_Usuarios_Drivers")
                    .document("trabajadores").collection("trabajadores")
                    .document(uid).collection("trabajos_realizados")
                    .document(tipo).collection(tipo).document(idSeleccionado)

                refDestino.set(hashMap).addOnSuccessListener {
                    refOrigen.delete().addOnSuccessListener {
                        Toast.makeText(this, "Trabajo movido a $tipo", Toast.LENGTH_SHORT).show()
                        binding.linealNoCuenta.isVisible = false
                        binding.recicleViewTrabajos.isVisible = false
                        obtenerPublicaciones(
                            "publicados",
                            firebaseAuth.uid.toString(),
                            listAdapter,
                            binding.recicleViewTrabajos,
                            this,
                            adapter,
                            binding.linealNoCuenta,
                        )
                    }.addOnFailureListener {
                        Toast.makeText(this, "Error al eliminar: ${it.message}", Toast.LENGTH_SHORT)
                            .show()
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


    private fun editarPublicacion(item: dataclas_trabajos_ralizados, tipo: String) {
        val bindingBottomShet =
            BottomSheetEditarPublicacionBinding.inflate(LayoutInflater.from(this))
        val view = bindingBottomShet.root

        val dialog = BottomSheetDialog(this)
        dialog.setContentView(view)

        bindingBottomShet.cerrar.setOnClickListener {
            dialog.dismiss()
        }
        bindingBottomShet.idPublicacion.text = item.id_publicacion.toString()
        bindingBottomShet.editar.setOnClickListener {
            if (bindingBottomShet.tituloPublicacionED.text.isEmpty()) {
                bindingBottomShet.tituloPublicacionED.error = "Ingrese un titulo"
                bindingBottomShet.tituloPublicacionED.requestFocus()
            } else if (bindingBottomShet.descripcionServiciosED.text.isEmpty()) {
                bindingBottomShet.descripcionServiciosED.error = "Ingrese una descripcion"
                bindingBottomShet.descripcionServiciosED.requestFocus()
            } else {
                editar_info(
                    dialog,
                    item.id_publicacion.toString(),
                    bindingBottomShet.tituloPublicacionED.text.toString(),
                    bindingBottomShet.descripcionServiciosED.text.toString(), tipo
                )
            }

        }
        bindingBottomShet.tituloPublicacionED.setText(item.titulo)
        bindingBottomShet.descripcionServiciosED.setText(item.contenido)

        dialog.show()
    }

    private fun editar_info(
        bindingBottomShet: BottomSheetDialog,
        idPublicacion: String, nuevoTitulo: String, nuevaDescripcion: String, tipo: String
    ) {
        val db = FirebaseFirestore.getInstance().collection(Variables.trabajadores_usuariosDB)
            .document(Variables.trabajadoresDB)
            .collection(Variables.trabajadoresDB)
            .document(firebaseAuth.uid.toString())
            .collection(Variables.trabajos_realizados).document(tipo)
            .collection(tipo)
            .document(idPublicacion)
        val updates = hashMapOf<String, Any>(
            Variables.titulo to nuevoTitulo,
            Variables.descripcion to nuevaDescripcion
        )
        db.update(updates)
            .addOnSuccessListener {
                Toast.makeText(this, "Campos actualizados correctamente", Toast.LENGTH_SHORT).show()

                bindingBottomShet.dismiss()
                obtenerPublicaciones(
                    "publicados",
                    firebaseAuth.uid.toString(),
                    listAdapter,
                    binding.recicleViewTrabajos,
                    this,
                    adapter,
                    binding.linealNoCuenta,
                )
            }

            .addOnFailureListener { e ->
                Toast.makeText(
                    this,
                    "Error al actualizar los campos: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }


    fun obtenerPublicaciones(
        filtrado: String,
        id: String,
        lista: MutableList<dataclas_trabajos_ralizados>,
        recicleTrabajosRealizados: RecyclerView,
        context: Context,
        adapter: publicaciones_ralizadas, // Cambiado a publicaciones_ralizadas
        lineal_no_cuenta: LinearLayout
    ) {
        val db = FirebaseFirestore.getInstance().collection(Variables.trabajadores_usuariosDB)
            .document(Variables.trabajadoresDB).collection(Variables.trabajadoresDB).document(id)
            .collection(Variables.trabajos_realizados).document(filtrado)
            .collection(filtrado)
        lineal_no_cuenta.isVisible = false
        recicleTrabajosRealizados.isVisible = false
        binding.linealEncontrados.isVisible = true

        db.get().addOnSuccessListener { result ->
            lista.clear()
            if (result.isEmpty) {
                binding.linealEncontrados.isVisible = false
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
                    binding.linealEncontrados.isVisible = false
                    lineal_no_cuenta.isVisible = true
                } else {
                    binding.linealEncontrados.isVisible = false
                    lineal_no_cuenta.isVisible = false
                    recicleTrabajosRealizados.isVisible = true
                    inicializarRecicle(recicleTrabajosRealizados, adapter, context)

                    adapter.notifyDataSetChanged() // Notifica al adaptador que los datos han cambiado
                }
            }
        }.addOnFailureListener {
            binding.linealEncontrados.isVisible = false
            lineal_no_cuenta.isVisible = true
        }
    }

    private fun inicializarRecicle(
        recycle: RecyclerView,
        adapter: publicaciones_ralizadas,
        context: Context
    ) {
        recycle.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        recycle.adapter = adapter
    }
}