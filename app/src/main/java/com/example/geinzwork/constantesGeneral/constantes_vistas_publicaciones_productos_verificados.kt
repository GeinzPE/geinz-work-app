package com.example.geinzwork.constantesGeneral

import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.lifecycle.Lifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.geinzwork.adapterViewholder.adapter_mostra_articulos_trabajadores
import com.example.geinzwork.adapterViewholder.adapter_trabajos_realizados_trabajador
import com.example.geinzwork.dataclass.dataclas_item_preview_art_comprar
import com.example.geinzwork.dataclass.dataclass_adapter_promociones
import com.example.geinzwork.fragmentos.productosPublicadosVista.ver_mas_productos_publicados_trabajadores
import com.example.geinzwork.publicaciones_trabajadores.mostrarTodosTrabajos
import com.geinzz.geinzwork.R
import com.geinzz.geinzwork.constantesGeneral.constantesTrabajadoresTiendasInicioFragmet
import com.geinzz.geinzwork.constantesGeneral.constantes_cuenta_user
import com.geinzz.geinzwork.constantesGeneral.constantes_publicaciones_general_user_tiendas
import com.geinzz.geinzwork.constantesGeneral.constantes_servicios
import com.geinzz.geinzwork.databinding.ItemCargarProductosPromocionesTrabajosBinding
import com.geinzz.geinzwork.databinding.ItemPerfilTrabajadorBinding
import com.geinzz.geinzwork.vistaTrabajador.vistaTrabajador
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import org.imaginativeworld.whynotimagecarousel.model.CarouselItem

object constantes_vistas_publicaciones_productos_verificados {
    private val listaAdapterProductosTRabajdores =
        mutableListOf<dataclas_item_preview_art_comprar>()
    private lateinit var dialog: BottomSheetDialog
    private val lista_publicaciones = mutableListOf<dataclass_adapter_promociones>()

    fun obtener_productosVenta(
        idTrabajador: String,
        context: Context,
        productosDestacados: RecyclerView,
        callback: (Boolean) -> Unit // Nuevo parámetro
    ) {
        val db = FirebaseFirestore.getInstance().collection("Trabajadores_Usuarios_Drivers")
            .document("trabajadores").collection("trabajadores").document(idTrabajador)
            .collection("productos_venta").document("publicados").collection("publicados")

        val listaTemporal = mutableListOf<dataclas_item_preview_art_comprar>()

        db.get().addOnSuccessListener { res ->

            for (datos in res) {
                val data = datos.data
                val imgProducto = data["img_url"] as? String ?: ""
                val descuentoActivo = data["descuento"] as? Boolean ?: false
                val id = data["id"] as? String ?: ""
                val cantidadDescuento = data["cantidad_porcentaje_descuento"] as? Number ?: 0
                val precio_descuento = data["precio_descuento"] as? Number ?: 0
                val precio = data["precio"] as? Number ?: 0
                val nombre = data["nombre"] as? String ?: ""
                val metodoEntrega = data["metodoEntrega"] as? String ?: ""

                val item = dataclas_item_preview_art_comprar(idTrabajador,metodoEntrega,
                    id,
                    imgProducto,
                    nombre,
                    precio,
                    precio_descuento,
                    descuentoActivo,
                    cantidadDescuento
                )

                listaTemporal.add(item)
            }

            listaTemporal.shuffle()
            listaAdapterProductosTRabajdores.clear()
            listaAdapterProductosTRabajdores.addAll(listaTemporal)

            if (listaAdapterProductosTRabajdores.isNotEmpty()) {
                inicializarRecicleProductosVentasTrabajdores(
                    context,
                    listaAdapterProductosTRabajdores,
                    idTrabajador,
                    productosDestacados
                )
                productosDestacados.isVisible = true
                callback(true) // ✅ Hay productos
            } else {
                productosDestacados.isVisible = false
                callback(false) // ❌ No hay productos
            }

        }.addOnFailureListener { e ->
            productosDestacados.isVisible = false
            callback(false) // ❌ Fallo al obtener productos
        }
    }


    private fun inicializarRecicleProductosVentasTrabajdores(
        context: Context,
        listaAdapterProductosTRabajdores: MutableList<dataclas_item_preview_art_comprar>,
        idTrabajador: String, productosDestacados: RecyclerView
    ) {
        val recicle = productosDestacados
        recicle.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        recicle.adapter = adapter_mostra_articulos_trabajadores(
            listaAdapterProductosTRabajdores
        ) { item ->
            dialog = BottomSheetDialog(context)
            constantes_publicaciones_general_user_tiendas.ShowBottomSheetDialogProductosTrabajadores(
                context,
                idTrabajador,
                item.id.toString(), dialog
            )
            dialog.show()
        }
    }


    fun obtener_perfil_trabajador(
        idTrabajador: String,
        ItemPerfilTrabajadorBinding: ItemPerfilTrabajadorBinding,
        context: Context
    ) {
        val db = FirebaseFirestore.getInstance().collection("Trabajadores_Usuarios_Drivers")
            .document("trabajadores").collection("trabajadores").document(idTrabajador)
        db.get().addOnSuccessListener { res ->
            if (res.exists()) {
                val data = res.data
                val nombre = data?.get("nombre") as? String ?: ""
                val apellido = data?.get("apellido") as? String ?: ""
                val categoriaTrabajo = data?.get("categoriaTrabajo") as? String ?: ""
                val tipoTrabajo = data?.get("tipoTrabajo") as? String ?: ""
                val localidad = data?.get("localidad") as? String ?: ""
                val nacionalidad = data?.get("nacionalidad") as? String ?: ""
                val fechaNac = data?.get("fechaNac") as? String ?: ""
                val FB = data?.get("FB") as? String ?: ""
                val IG = data?.get("IG") as? String ?: ""
                val img = data?.get("imagenPerfil") as? String ?: ""
                val TK = data?.get("TK") as? String ?: ""
                constantesTrabajadoresTiendasInicioFragmet.obtnerIMG_trabajador(
                    idTrabajador,
                    ItemPerfilTrabajadorBinding.imgPerfil,
                    ItemPerfilTrabajadorBinding.cargadoImg,
                    context
                )
                constantes_servicios.verificarEstado_vericiacion(
                    ItemPerfilTrabajadorBinding.verificado,
                    idTrabajador
                ) { v, plan ->
                    when (plan) {
                        Variables.plaA -> {
                            ItemPerfilTrabajadorBinding.verificado.setImageResource(R.drawable.verificado_a)

                        }

                        Variables.planB -> {
                            ItemPerfilTrabajadorBinding.verificado.setImageResource(R.drawable.icon_verificado)
                        }

                        Variables.PlanC -> {
                            ItemPerfilTrabajadorBinding.verificado.setImageResource(R.drawable.verificado_c)


                        }
                    }

                }
                constantes_trabajadores_info.contadorSeguidores(
                    ItemPerfilTrabajadorBinding.seguidoresTrabajador,
                    idTrabajador
                )
                ItemPerfilTrabajadorBinding.nombreTrabajador.text = "$nombre $apellido"
                ItemPerfilTrabajadorBinding.categoriaTrabajo.text = categoriaTrabajo
                ItemPerfilTrabajadorBinding.tipoTrabajo.text = tipoTrabajo
                ItemPerfilTrabajadorBinding.localidad.text = localidad
                ItemPerfilTrabajadorBinding.nacionnalidad.text = nacionalidad
                ItemPerfilTrabajadorBinding.nombreTrabajadoroculto.text = nombre
                ItemPerfilTrabajadorBinding.urlTrabajador.text = img
                Handler(Looper.getMainLooper()).postDelayed({
                    ItemPerfilTrabajadorBinding.cargarContenido.isVisible = false
                    ItemPerfilTrabajadorBinding.linealInfoTrabajdor.isVisible = true
                }, 2000)
                if (FB.isNotEmpty()) {
                    ItemPerfilTrabajadorBinding.fb.isVisible = true

                }
                if (IG.isNotEmpty()) {
                    ItemPerfilTrabajadorBinding.ig.isVisible = true

                }
                if (TK.isNotEmpty()) {
                    ItemPerfilTrabajadorBinding.tk.isVisible = true

                }

                constantes_cuenta_user.calcularEdadTrabajador(fechaNac) { edad ->
                    if (edad.isNullOrEmpty()) {
                        ItemPerfilTrabajadorBinding.edadUser.text = "No se calculo los años"
                    } else {
                        ItemPerfilTrabajadorBinding.edadUser.text = "${edad} años"
                    }
                }
                ItemPerfilTrabajadorBinding.linealInfoTrabajdor.setOnClickListener {
                    val vista = Intent(context, vistaTrabajador::class.java).apply {
                        putExtra(Variables.id, idTrabajador)
                        putExtra(
                            Variables.nombreUSer,
                            ItemPerfilTrabajadorBinding.nombreTrabajadoroculto.text
                        )
                        putExtra(
                            Variables.nacionalidad,
                            ItemPerfilTrabajadorBinding.nacionnalidad.text
                        )
                        putExtra(
                            Variables.categoria,
                            ItemPerfilTrabajadorBinding.categoriaTrabajo.text
                        )
                        putExtra(
                            Variables.imagenPerfil,
                            ItemPerfilTrabajadorBinding.urlTrabajador.text
                        )
                    }
                    context.startActivity(vista)
                }

            }
        }.addOnFailureListener { e -> }
    }


    fun obtenerMasTrabajosRealiazdos(
        idTrabajador: String,
        idSelecionado: String,
        context: Context,
        ItemPerfilTrabajadorBinding: ItemPerfilTrabajadorBinding,
        recyclerView: RecyclerView,
        lifecycle: Lifecycle,
        callback: (Boolean) -> Unit
    ) {
        val db = FirebaseFirestore.getInstance()
            .collection("Trabajadores_Usuarios_Drivers").document("trabajadores")
            .collection("trabajadores").document(idTrabajador).collection("publicaciones_trabajos")
            .document("publicados").collection("publicados")

        val trabajos = mutableListOf<Map<String, Any>>()
        val datosTrabajos = mutableListOf<Map<String, Any>>()
        val lista = mutableListOf<CarouselItem>()

        db.get().addOnSuccessListener { res ->
            lista_publicaciones.clear()

            for (datos in res) {
                val data = datos.data
                val img_url = data["img_url"] as? String ?: ""
                val titulo = data["titulo"] as? String ?: ""
                val contenido = data["contenido"] as? String ?: ""
                val id = data["id"] as? String ?: ""
                val fecha = data["fecha_rec"] as? String ?: ""
                val hora = data["hora_rec"] as? String ?: ""
                val img_url2 = data["img_url2"] as? String ?: ""
                val img_url3 = data["img_url3"] as? String ?: ""
                val img_url4 = data["img_url4"] as? String ?: ""
                val listaImg =
                    listOf(img_url, img_url2, img_url3, img_url4).filter { it.isNotEmpty() }

                val trabajoData = mapOf(
                    "titulo" to titulo,
                    "contenido" to contenido,
                    "fecha_rec" to fecha,
                    "hora_rec" to hora,
                    "img_url" to img_url,
                    "id" to id,
                    "listaImg" to listaImg
                )
                trabajos.add(trabajoData)

                if (id != idSelecionado) {
                    val dataClass = dataclass_adapter_promociones(
                        img_url,
                        img_url2,
                        img_url3,
                        img_url4,
                        titulo,
                        contenido,
                        id,
                        fecha,
                        hora
                    )
                    lista_publicaciones.add(dataClass)
                }
            }

            if (lista_publicaciones.isNotEmpty()) {
                lista_publicaciones.shuffle()
                trabajos.shuffle()
                val trabajosSeleccionados = trabajos.take(5)

                for (trabajo in trabajosSeleccionados) {
                    val img_url = trabajo["img_url"] as? String ?: ""
                    val carouselItem1 = CarouselItem(img_url)
                    lista.add(carouselItem1)
                    datosTrabajos.add(trabajo)
                    inicializarTrabajosRealizados(
                        idTrabajador,
                        db,
                        context,
                        lista_publicaciones,
                        lifecycle,
                        recyclerView,
                        ItemPerfilTrabajadorBinding
                    )
                }

                callback(true) // ✅ Hay trabajos

            } else {
                Log.d("error obtenerDAtos", "No hay datos para mostrar")
                callback(false) // ❌ No hay trabajos
            }

        }.addOnFailureListener { e ->
            println("error al encontrar $e")
            callback(false) // ❌ Error al obtener trabajos
        }
    }


    private fun inicializarTrabajosRealizados(
        idTrabajador: String,
        trabajo: CollectionReference,
        context: Context,
        listaMas_promo: MutableList<dataclass_adapter_promociones>,
        lifecycle: Lifecycle,
        recyclerView: RecyclerView,
        ItemPerfilTrabajadorBinding: ItemPerfilTrabajadorBinding
    ) {
        val recicle = recyclerView
        recicle.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        recicle.adapter = adapter_trabajos_realizados_trabajador(
            false,
            listaMas_promo
        ) { item ->
            Toast.makeText(context, "selecionaste tal item", Toast.LENGTH_SHORT).show()
            dialog = BottomSheetDialog(context)
            constantes_publicaciones_general_user_tiendas.showBottomShetDialogAnuncios(
                idTrabajador,
                trabajo,
                context,
                ItemPerfilTrabajadorBinding.nombreTrabajadoroculto.text.toString(),
                listaMas_promo,
                lifecycle,
                item, dialog
            )
            dialog.show()
            Toast.makeText(context, "cambios de hola ${item.id}", Toast.LENGTH_SHORT).show()
        }

    }

    fun ver_todos_productos_activity(
        cargaProductosPromoTrabajos: ItemCargarProductosPromocionesTrabajosBinding,
        context: Context, idTrabajador: String
    ) {
        cargaProductosPromoTrabajos.verTodosTrabajos.setOnClickListener {
            val intent =
                Intent(context, ver_mas_productos_publicados_trabajadores::class.java).apply {
                    putExtra("idTrabajador", idTrabajador)
                }
            context.startActivity(intent)
        }

    }

    fun ver_todo_publicaciones_activty(
        cargaProductosPromoTrabajos: ItemCargarProductosPromocionesTrabajosBinding,
        context: Context, idTrabajador: String
    ) {
        cargaProductosPromoTrabajos.verTodosTrabajos.setOnClickListener {
            val intent = Intent(context, mostrarTodosTrabajos::class.java).apply {
                putExtra("idTrabajador", idTrabajador)
            }
            context.startActivity(intent)

        }

    }

}