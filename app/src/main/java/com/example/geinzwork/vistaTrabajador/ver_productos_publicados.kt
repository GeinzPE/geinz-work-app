package com.example.geinzwork.vistaTrabajador

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.geinzwork.adapterViewholder.adapter_pbl_vr_tb_recientes
import com.example.geinzwork.dataclass.dataclas_trabajos_ralizados_verificados
import com.geinzz.geinzwork.R
import com.geinzz.geinzwork.constantesGeneral.constantestextos_general
import com.geinzz.geinzwork.constantesGeneral.mostrarFechaDialog_horaDialog
import com.geinzz.geinzwork.databinding.ActivityVerProductosPublicadosBinding
import com.geinzz.geinzwork.databinding.BottomSheetCamposTrPdPBinding
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions

class ver_productos_publicados : AppCompatActivity() {
    private lateinit var binding: ActivityVerProductosPublicadosBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var adapter: adapter_pbl_vr_tb_recientes
    private lateinit var dialog: BottomSheetDialog
    private val lista = mutableListOf<dataclas_trabajos_ralizados_verificados>()
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
        })
        val dato_pasado = intent.getStringExtra("tipo").toString()
        Log.d("DebugTipo", "dato_pasado: $dato_pasado")

        when (dato_pasado) {
            "publicadas" -> {
                binding.linealChips.isVisible = true

                obtener_productos("publicados", "No se encontraron publicaciones")

                binding.todos.setOnClickListener {
                    binding.linealEncontrados.isVisible = false

                    obtener_productos("publicados", "No se encontraron publicaciones")


                }
                binding.masClicks.setOnClickListener {
                    binding.linealEncontrados.isVisible = true
                    binding.recicleProductos.isVisible = false
//                    obtener_mayor_menor_cantidad_campos("estadisticas_click") { max, min ->
//                        dialog = BottomSheetDialog(this)
//                        bottom_sheet_chips(max, min) { minC, maxC ->
//                            filtrar_publicaciones(minC, maxC, "estadisticas_click")
//
//                        }
//                        dialog.show()
//                    }

                }
                binding.masVistas.setOnClickListener {
                    binding.linealEncontrados.isVisible = true
                    binding.recicleProductos.isVisible = false
//                    obtener_mayor_menor_cantidad_campos("estadisticas_vistas") { max, min ->
//                        dialog = BottomSheetDialog(this)
//                        bottom_sheet_chips(max, min) { minC, maxC ->
//                            filtrar_publicaciones(minC, maxC, "estadisticas_vistas")
//                        }
//                        dialog.show()
//                    }
                }
                binding.masCompartidas.setOnClickListener {
                    binding.linealEncontrados.isVisible = true
                    binding.recicleProductos.isVisible = false
//                    obtener_mayor_menor_cantidad_campos("estadisticas_compartir") { max, min ->
//
//                        dialog = BottomSheetDialog(this)
//                        bottom_sheet_chips(max, min) { minC, maxC ->
//                            filtrar_publicaciones(minC, maxC, "estadisticas_vistas")
//                        }
//                        dialog.show()
//                    }
                }
                binding.privado.setOnClickListener {
                    binding.linealEncontrados.isVisible = true
                    binding.recicleProductos.isVisible = false

                    obtener_productos("privado", "No se encontraron publicaciones")


                }
                binding.soloSeguidores.setOnClickListener {
                    binding.linealEncontrados.isVisible = true
                    binding.recicleProductos.isVisible = false

                    obtener_productos("solo_seguidores", "No se encontraron publicaciones")

                }
            }

            "archivadas" -> {
                binding.linealChips.isVisible = false

                obtener_productos("archivados", "No se encontraron publicaciones")
            }

            "eliminadas" -> {
                binding.linealChips.isVisible = false
                obtener_productos("eliminados", "No se encontraron publicaciones")
            }

            else -> {
                binding.linealChips.isVisible = true
                obtener_productos("publicados", "No se encontraron publicaciones")
                binding.masClicks.setOnClickListener {
                    binding.linealEncontrados.isVisible = true
                    binding.recicleProductos.isVisible = false
//                    obtener_mayor_menor_cantidad_campos("estadisticas_click") { max, min ->
//                        dialog = BottomSheetDialog(this)
//                        bottom_sheet_chips(max, min) { minC, maxC ->
//                            filtrar_publicaciones(minC, maxC, "estadisticas_click")
//
//                        }
//                        dialog.show()
//                    }

                }
                binding.masVistas.setOnClickListener {
                    binding.linealEncontrados.isVisible = true
                    binding.recicleProductos.isVisible = false
//                    obtener_mayor_menor_cantidad_campos("estadisticas_vistas") { max, min ->
//                        dialog = BottomSheetDialog(this)
//                        bottom_sheet_chips(max, min) { minC, maxC ->
//                            filtrar_publicaciones(minC, maxC, "estadisticas_vistas")
//                        }
//                        dialog.show()
//                    }
                }
                binding.masCompartidas.setOnClickListener {
                    binding.linealEncontrados.isVisible = true
                    binding.recicleProductos.isVisible = false
//                    obtener_mayor_menor_cantidad_campos("estadisticas_compartir") { max, min ->
//
//                        dialog = BottomSheetDialog(this)
//                        bottom_sheet_chips(max, min) { minC, maxC ->
//                            filtrar_publicaciones(minC, maxC, "estadisticas_vistas")
//                        }
//                        dialog.show()
//                    }
                }
                binding.privado.setOnClickListener {
                    binding.linealEncontrados.isVisible = true
                    binding.recicleProductos.isVisible = false
                    obtener_productos("privado", "No se encontraron publicaciones")


                }
                binding.soloSeguidores.setOnClickListener {
                    binding.linealEncontrados.isVisible = true
                    binding.recicleProductos.isVisible = false

                    obtener_productos("solo_seguidores", "No se encontraron publicaciones")

                }
            }
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
//                editar.setOnClickListener {
//                    Toast.makeText(
//                        this,
//                        "solo puedes editar caundo estas en TODOS",
//                        Toast.LENGTH_SHORT
//                    ).show()
//                }
//                eliminar.setOnClickListener {
//                    dialog.dismiss()
//                    eliminar_publicacion_Archivar(
//                        item.id_publicacion.toString(),
//                        "publicados",
//                        "eliminados"
//                    )
//
//                }
//                archivar.setOnClickListener {
//                    dialog.dismiss()
//                    eliminar_publicacion_Archivar(
//                        item.id_publicacion.toString(),
//                        "publicados",
//                        "archivados"
//                    )
//                }
//                privado.setOnClickListener {
//                    dialog.dismiss()
//                    eliminar_publicacion_Archivar(
//                        item.id_publicacion.toString(),
//                        "publicados",
//                        "privado"
//                    )
//
//                }
//                solo_seguidores.setOnClickListener {
//                    dialog.dismiss()
//                    eliminar_publicacion_Archivar(
//                        item.id_publicacion.toString(),
//                        "publicados",
//                        "solo_seguidores"
//                    )
//
//                }

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
//                    archivar_eliminar_publicaciones(
//                        item.id_publicacion.toString(),
//                        "publicados",
//                        "eliminados", "masvistas"
//                    )

                }
                archivar.setOnClickListener {
                    dialog.dismiss()
//                    archivar_eliminar_publicaciones(
//                        item.id_publicacion.toString(),
//                        "publicados",
//                        "archivados", "masvistas"
//                    )
                }
                privado.setOnClickListener {
                    dialog.dismiss()
//                    archivar_eliminar_publicaciones(
//                        item.id_publicacion.toString(),
//                        "publicados",
//                        "privado", "masvistas"
//                    )
                }
                solo_seguidores.setOnClickListener {
                    dialog.dismiss()
//                    archivar_eliminar_publicaciones(
//                        item.id_publicacion.toString(),
//                        "publicados",
//                        "solo_seguidores", "masvistas"
//                    )
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
//                    archivar_eliminar_publicaciones(
//                        item.id_publicacion.toString(),
//                        "publicados",
//                        "eliminados", "mascompartidas"
//                    )

                }
                archivar.setOnClickListener {
                    dialog.dismiss()
//                    archivar_eliminar_publicaciones(
//                        item.id_publicacion.toString(),
//                        "publicados",
//                        "archivados", "mascompartidas"
//                    )
                }
                privado.setOnClickListener {
                    dialog.dismiss()
//                    archivar_eliminar_publicaciones(
//                        item.id_publicacion.toString(),
//                        "publicados",
//                        "privado", "mascompartidas"
//                    )
                }
                solo_seguidores.setOnClickListener {
                    dialog.dismiss()
//                    archivar_eliminar_publicaciones(
//                        item.id_publicacion.toString(),
//                        "publicados",
//                        "solo_seguidores", "mascompartidas"
//                    )
                }
            }


            if (binding.todos.isChecked) {
                vista_previa.setOnClickListener {
                    dialog.dismiss()
                    vista_previa_publicaciones(item.id_publicacion.toString())
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
//                    editar_publicaciones(item.id_publicacion.toString(), "privado")
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
                editar.setOnClickListener {
//                    editar_publicaciones(item.id_publicacion.toString(), "solo_seguidores")
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
//            editar.setOnClickListener {
//                editar_publicaciones(item.id_publicacion.toString(), "solo_seguidores")
//            }
            vista_previa.setOnClickListener {
                dialog.dismiss()
                vista_previa_publicaciones(item.id_publicacion.toString())
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

            vista_previa.setOnClickListener {
                dialog.dismiss()
                vista_previa_publicaciones(item.id_publicacion.toString())
            }
        }


        bottoSheet.eliminarPermanente.setOnClickListener {
            binding.linealEncontrados.isVisible = true
            binding.textoDinamicoProgrees.text = "Actualizando contenido..."
            binding.recicleProductos.isVisible = false
            dialog.dismiss()
            val firestore = FirebaseFirestore.getInstance()
            val uid = firebaseAuth.uid.toString()
            val refOrigen = firestore.collection("Trabajadores_Usuarios_Drivers")
                .document("trabajadores").collection("trabajadores")
                .document(uid).collection("productos_venta")
                .document("eliminados").collection("eliminados")
                .document(item.id_publicacion.toString())
            refOrigen.delete().addOnSuccessListener { res ->
                obtener_productos("eliminados","No se encontraron datos")
                Toast.makeText(this, "publicacion eliminado correctamente", Toast.LENGTH_SHORT)
                    .show()

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
                    "mas_informacio" to masInformacion
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
                    Toast.makeText(this, "agregeado correctamente a eliminados", Toast.LENGTH_SHORT)
                        .show()
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

    private fun vista_previa_publicaciones(id_publicacion: String) {
        val vista =
            Intent(this, vista_ver_productos_trabajadores::class.java).apply {
                putExtra("id_trabajador", firebaseAuth.uid.toString())
                    .putExtra("id_publicacion", id_publicacion)
            }
        startActivity(vista)
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
                    "mas_informacio" to masInformacion
                )
                for ((key, value) in data) {
                    if (key.startsWith("img_url") && value is String) {
                        hasMap[key] = value
                    }
                }
                refEliminado.set(hasMap, SetOptions.merge()).addOnSuccessListener { res ->
                    Toast.makeText(this, "agregeado correctamente a eliminados", Toast.LENGTH_SHORT)
                        .show()
                    binding.linealEncontrados.isVisible = false
                    binding.recicleProductos.isVisible = true
                    when (funcion_carga) {
                        "todos" -> {
                            obtener_productos(
                                "publicados", "No se encontraron datos"
                            )
                        }

                        "mascliks" -> {
//                                obtener_productos(
//                                    binding.min.text.toString().toInt(),
//                                    binding.max.text.toString().toInt(),
//                                    "estadisticas_click"
//                                )
                        }

                        "masvistas" -> {
//                                obtener_productos(
//                                    binding.min.text.toString().toInt(),
//                                    binding.max.text.toString().toInt(),
//                                    "estadisticas_vistas"
//                                )
                        }

                        "mascompartidas" -> {
//                                obtener_productos(
//                                    binding.min.text.toString().toInt(),
//                                    binding.max.text.toString().toInt(),
//                                    "estadisticas_compartir"
//                                )
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

    private fun obtener_productos(tipo: String, texto_sin_encontrar: String) {

        binding.linealNoCuenta.isVisible = false
        binding.recicleProductos.isVisible = false
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
}