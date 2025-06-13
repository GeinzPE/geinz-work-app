package com.example.geinzwork.vistaTrabajador

import android.content.Context
import android.os.Bundle
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
        obtener_productos()
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
            eliminar_publicacion_Archivar(item.id_publicacion.toString(),"eliminados")

        }
        editar.setOnClickListener {
//            eliminar_publicacion_Archivar(item.id_publicacion.toString(),"archivados")
        }
        archivar.setOnClickListener {
            eliminar_publicacion_Archivar(item.id_publicacion.toString(),"archivados")
        }

        dialog.setContentView(view)
    }


    private fun eliminar_publicacion_Archivar(id_selecionado: String,colle_dco:String) {
        val refEliminado =
            FirebaseFirestore.getInstance().collection("Trabajadores_Usuarios_Drivers")
                .document("trabajadores").collection("trabajadores")
                .document(firebaseAuth.uid.toString()).collection("productos_venta")
                .document(colle_dco).collection(colle_dco).document(id_selecionado)
        val ref_puplicados =
            FirebaseFirestore.getInstance().collection("Trabajadores_Usuarios_Drivers")
                .document("trabajadores").collection("trabajadores")
                .document(firebaseAuth.uid.toString()).collection("productos_venta")
                .document("publicados").collection("publicados").document(id_selecionado)

        ref_puplicados.get().addOnSuccessListener { res ->
            if (res.exists()) {
                val data = res.data

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
                refEliminado.set(hasMap, SetOptions.merge()).addOnSuccessListener { res ->
                    Toast.makeText(this, "agregeado correctamente a eliminados", Toast.LENGTH_SHORT)
                        .show()
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

    private fun obtener_productos() {
        val db = FirebaseFirestore.getInstance().collection("Trabajadores_Usuarios_Drivers")
            .document("trabajadores").collection("trabajadores")
            .document(firebaseAuth.uid.toString()).collection("productos_venta").document("publicados").collection("publicados")
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
//                binding.linealNoCuenta.isVisible = true
            } else {
//                binding.loading.isVisible = false
//                binding.linealNoCuenta.isVisible = false
//                binding.recicleViewTrabajos.isVisible = true
                inicializarRecicle(binding.recicleProductos, adapter, this)
//                binding.linealappLayout.isVisible = true
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