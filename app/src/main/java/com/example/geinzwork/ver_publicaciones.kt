package com.geinzz.geinzwork

import android.content.Context
import android.graphics.PorterDuff

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

        adapter = publicaciones_ralizadas(
            listAdapter,
            { item ->
                dialog = BottomSheetDialog(this)
                bottomSheet_editar_eliminar_Arhivar_estadi(item, "publicados")
                dialog.show()
            },
            { cantidadSeleccionados, listaSeleccionados ->  // 🔴 ahora recibes ambos
                binding.modoSelecion.text = "$cantidadSeleccionados Selecionados"
                binding.modoSelecion.isVisible = true
                binding.titulosCentrado.isVisible = false
                if (cantidadSeleccionados > 0) {
                    binding.cerrarselecion.isVisible = true
                    binding.listartododos.isVisible = true
                    binding.bottomOpciones.isVisible = true

                    // 🔎 Accede a los IDs seleccionados (si tu data class tiene "id")
                    val idsSeleccionados = listaSeleccionados.map { it.id_publicacion }
                    binding.idsObtenidos.text = "$idsSeleccionados"

                    binding.eliminarselect.setOnClickListener {
                        binding.linealEncontrados.isVisible=true
                        binding.cargandoActualzando.text="Cambiando publicaciones"
                        binding.linealPublicacionesFiltrados.isVisible=false
                        binding.bottomOpciones.isVisible=false
                        binding.recicleViewTrabajos.isVisible=false
                        Toast.makeText(
                            this,
                            "pasmos a elminados todos los $idsSeleccionados",
                            Toast.LENGTH_SHORT
                        ).show()
                        eliminar_archivar_lista_ids(idsSeleccionados as List<String>, "eliminados")

                    }

                    binding.archivarselect.setOnClickListener {
                        binding.linealEncontrados.isVisible=true
                        binding.cargandoActualzando.text="Cambiando publicaciones"
                        binding.linealPublicacionesFiltrados.isVisible=false
                        binding.bottomOpciones.isVisible=false
                        binding.recicleViewTrabajos.isVisible=false
                        eliminar_archivar_lista_ids(idsSeleccionados as List<String>, "archivados")

                        Toast.makeText(
                            this,
                            "pasmos a archivados todos los $idsSeleccionados",
                            Toast.LENGTH_SHORT
                        ).show()


                    }


                    val todosSeleccionados = cantidadSeleccionados == listAdapter.size
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
                }

            }
        )

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
            binding.archivarselect.isVisible = true
            binding.eliminarselect.isVisible = true
            binding.reactivar.isVisible = false
            adapter.cancelarModoSeleccion()
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
            binding.archivarselect.isVisible = true
            binding.eliminarselect.isVisible = true
            binding.reactivar.isVisible = false
            adapter.cancelarModoSeleccion()
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
            binding.archivarselect.isVisible = false
            binding.eliminarselect.isVisible = false
            binding.reactivar.isVisible = true
            adapter.cancelarModoSeleccion()
            obtenerPublicaciones(
                "eliminados",
                firebaseAuth.uid.toString(),
                listAdapter,
                binding.recicleViewTrabajos,
                this,
                adapter,
                binding.linealNoCuenta,
            )
            binding.reactivar.setOnClickListener {
                binding.linealEncontrados.isVisible=true
                binding.cargandoActualzando.text="Cambiando publicaciones"
                binding.linealPublicacionesFiltrados.isVisible=false
                binding.bottomOpciones.isVisible=false
                binding.recicleViewTrabajos.isVisible=false
                val ids = binding.idsObtenidos.text
                    .toString()
                    .removePrefix("[")
                    .removeSuffix("]")
                    .split(",")
                    .map { it.trim() }

                activar_publicacion_list("eliminados", ids)
            }
        }
        binding.chipArchivados.setOnClickListener {
            binding.archivarselect.isVisible = false
            binding.eliminarselect.isVisible = false
            binding.reactivar.isVisible = true
            adapter.cancelarModoSeleccion()
            obtenerPublicaciones(
                "archivados",
                firebaseAuth.uid.toString(),
                listAdapter,
                binding.recicleViewTrabajos,
                this,
                adapter,
                binding.linealNoCuenta,
            )
            binding.reactivar.setOnClickListener {
                binding.linealEncontrados.isVisible=true
                binding.cargandoActualzando.text="Cambiando publicaciones"
                binding.linealPublicacionesFiltrados.isVisible=false
                binding.bottomOpciones.isVisible=false
                binding.recicleViewTrabajos.isVisible=false
                val ids = binding.idsObtenidos.text
                    .toString()
                    .removePrefix("[")
                    .removeSuffix("]")
                    .split(",")
                    .map { it.trim() }

                activar_publicacion_list("archivados", ids)
            }
        }

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

    private fun activar_publicacion_list(tipo: String, idsPublicacion: List<String>) {
        val firestore = FirebaseFirestore.getInstance()
        val uid = firebaseAuth.uid.toString()

        idsPublicacion.forEach { idPublicacion ->
            val refOrigen = firestore.collection("Trabajadores_Usuarios_Drivers")
                .document("trabajadores").collection("trabajadores")
                .document(uid).collection("trabajos_realizados")
                .document(tipo).collection(tipo).document(idPublicacion)
            Log.d("ids_bonido", "${refOrigen.path}")

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
                            Toast.makeText(
                                this,
                                "Trabajo $idPublicacion movido correctamente",
                                Toast.LENGTH_SHORT
                            ).show()


                            obtenerPublicaciones(
                                tipo,
                                uid,
                                listAdapter,
                                binding.recicleViewTrabajos,
                                this,
                                adapter,
                                binding.linealNoCuenta
                            )

                        }.addOnFailureListener {
                            Toast.makeText(
                                this,
                                "Error al eliminar: ${it.message}",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }.addOnFailureListener {
                        Toast.makeText(this, "Error al mover: ${it.message}", Toast.LENGTH_SHORT)
                            .show()
                    }

                } else {
                    Toast.makeText(
                        this,
                        "El documento $idPublicacion no existe",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }.addOnFailureListener {
                Toast.makeText(
                    this,
                    "Error al obtener $idPublicacion: ${it.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
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


    private fun eliminar_archivar_lista_ids(idSeleccionados: List<String>, tipo: String) {
        val firestore = FirebaseFirestore.getInstance()
        val uid = firebaseAuth.uid.toString()

        for (id in idSeleccionados) {
            val refOrigen = firestore.collection("Trabajadores_Usuarios_Drivers")
                .document("trabajadores").collection("trabajadores")
                .document(uid).collection("trabajos_realizados")
                .document("publicados").collection("publicados").document(id)

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

                    // Copiar imágenes
                    for ((key, value) in data) {
                        if (key.startsWith("img_url") && value is String) {
                            hashMap[key] = value
                        }
                    }

                    val refDestino = firestore.collection("Trabajadores_Usuarios_Drivers")
                        .document("trabajadores").collection("trabajadores")
                        .document(uid).collection("trabajos_realizados")
                        .document(tipo).collection(tipo).document(id)

                    refDestino.set(hashMap).addOnSuccessListener {
                        refOrigen.delete().addOnSuccessListener {
                            println("✅ Trabajo $id movido a $tipo")
                        }.addOnFailureListener {
                            Toast.makeText(
                                this,
                                "Error al eliminar $id: ${it.message}",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }.addOnFailureListener {
                        Toast.makeText(
                            this,
                            "Error al mover $id: ${it.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }

                } else {
                    Toast.makeText(this, "El documento $id no existe", Toast.LENGTH_SHORT).show()
                }
            }.addOnFailureListener {
                Toast.makeText(this, "Error al obtener $id: ${it.message}", Toast.LENGTH_SHORT)
                    .show()
            }
        }

        listAdapter.clear()
        Handler(Looper.getMainLooper()).postDelayed({
            obtenerPublicaciones(
                "publicados",
                uid,
                listAdapter,
                binding.recicleViewTrabajos,
                this,
                adapter,
                binding.linealNoCuenta,
            )
        }, 3000) // espera 1 segundo a que terminen
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
        binding.linealEncontrados.isVisible=false
        binding.linealPublicacionesFiltrados.isVisible=true
        binding.cerrarselecion.isVisible = false
        binding.listartododos.isVisible = false
        binding.deslistar.isVisible = false
        adapter.cancelarModoSeleccion()
        val db = FirebaseFirestore.getInstance().collection(Variables.trabajadores_usuariosDB)
            .document(Variables.trabajadoresDB).collection(Variables.trabajadoresDB).document(id)
            .collection(Variables.trabajos_realizados).document(filtrado)
            .collection(filtrado)
        lineal_no_cuenta.isVisible = false
        recicleTrabajosRealizados.isVisible = false
        binding.linealEncontrados.isVisible = true
        binding.cargandoActualzando.text="Cargando publicaciones"

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