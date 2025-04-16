package com.example.geinzwork.vistaTrabajador

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.geinzwork.adapterViewholder.adapterInicializarRecycleimgProductosTrabajadores
import com.example.geinzwork.constantesGeneral.constantes_vistas_publicaciones_productos_verificados
import com.example.geinzwork.fragmentos.productosPublicadosVista.compras_productos_vendedor
import com.example.geinzwork.fragmentos.productosPublicadosVista.ver_mas_productos_publicados_trabajadores
import com.geinzz.geinzwork.R
import com.geinzz.geinzwork.constantesGeneral.constantestextos_general
import com.geinzz.geinzwork.databinding.ActivityVistaVerProductosTrabajadoresBinding
import com.geinzz.geinzwork.dataclass.dataclassMostarImgProductosVendedor
import com.google.firebase.firestore.FirebaseFirestore

class vista_ver_productos_trabajadores : AppCompatActivity() {
    val listaImg = mutableListOf<dataclassMostarImgProductosVendedor>()
    private lateinit var binding: ActivityVistaVerProductosTrabajadoresBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityVistaVerProductosTrabajadoresBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        val idTrabajador = intent.getStringExtra("id_trabajador").toString()
        val id_publicacion_clikeada = intent.getStringExtra("id_publicacion").toString()
        obtenerCampos_producto(idTrabajador, id_publicacion_clikeada)
        constantes_vistas_publicaciones_productos_verificados.obtener_productosVenta(
            idTrabajador,
            this,
            binding.cargaProductosPromoTrabajos.masTrabajosRealiados
        ) { existe ->
            if (existe) {
                binding.cargaProductosPromoTrabajos.cargandoContenido.isVisible = false
                binding.cargaProductosPromoTrabajos.masTrabajosRealiados.isVisible = true
                binding.cargaProductosPromoTrabajos.linealNoSeEncontraron.isVisible = false

            } else {
                binding.cargaProductosPromoTrabajos.cargandoContenido.isVisible = false
                binding.cargaProductosPromoTrabajos.masTrabajosRealiados.isVisible = false
                binding.cargaProductosPromoTrabajos.linealNoSeEncontraron.isVisible = true
            }
        }
        binding.cargaProductosPromoTrabajos.cambiarTextoTrabajosRealziadosTrabajosRecientes.text =
            "Productos publicados"
        binding.cargaProductosPromoTrabajos.textoCambiarTrabajosOPublicaciones.text =
            "No se encontraron productos"
        constantes_vistas_publicaciones_productos_verificados.obtener_perfil_trabajador(
            idTrabajador,
            binding.perfiltrabajador,
            this
        )
        constantes_vistas_publicaciones_productos_verificados.ver_todos_productos_activity(binding.cargaProductosPromoTrabajos,this,idTrabajador)




    }




    private fun obtenerCampos_producto(idTrabajador: String, productoClikado: String) {
        val startTime = System.currentTimeMillis()
        binding.containerDatos.isVisible = false

        val db = FirebaseFirestore.getInstance().collection("Trabajadores_Usuarios_Drivers")
            .document("trabajadores").collection("trabajadores").document(idTrabajador)
            .collection("productos_venta").document(productoClikado)

        db.get().addOnSuccessListener { res ->
            val endTime = System.currentTimeMillis()
            val totalTime = endTime - startTime
            val tiempoMinimoCarga = 800L
            val esperaExtra =
                if (totalTime < tiempoMinimoCarga) (tiempoMinimoCarga - totalTime) else 0L

            if (res.exists()) {
                val data = res.data ?: emptyMap()

                Handler(Looper.getMainLooper()).postDelayed({
                    try {
                        val cantidadPorcentajeDescuento =
                            data?.get("cantidad_porcentaje_descuento") as? Number ?: 0
                        val precio = data?.get("precio") as? Number ?: 0
                        val precioDescuento = data?.get("precio_descuento") as? Number ?: 0
                        val totalProducto = data?.get("total_producto") as? Number ?: 0

                        val categoria = data?.get("categoria") as? String ?: ""
                        val condicionProducto = data?.get("condicion_producto") as? String ?: ""
                        val descripcion = data?.get("descripcion") as? String ?: ""
                        val modelo = data?.get("modelo") as? String ?: ""
                        val descuento = data?.get("descuento") as? Boolean ?: false
                        val efectivo = data?.get("efectivo") as? Boolean ?: false
                        val entrega_domicilio = data?.get("entrega_domicilio") as? Boolean ?: true
                        val garantia = data?.get("garantia") as? String ?: ""
                        val id = data?.get("id") as? String ?: ""
                        val fechaPublicada = data?.get("fechaPublicada") as? String ?: ""
                        val lugarDeEntrega = data?.get("lugarEntrega") as? String ?: ""
                        val marca = data?.get("marca") as? String ?: ""
                        val nombre = data?.get("nombre") as? String ?: ""
                        val plin = data?.get("plin") as? Boolean ?: false
                        val stok = data?.get("stok") as? String ?: ""
                        val yape = data?.get("yape") as? Boolean ?: false

                        if (entrega_domicilio) {
                            binding.camposProductosUserVerificados.entregaDomicilio.text = "si"
                        } else {
                            binding.camposProductosUserVerificados.entregaDomicilio.text = "no"
                        }

                        if (descuento) {
                            constantestextos_general.marcarDescuentoTxt(binding.camposProductosUserVerificados.precioAntiguo)
                            constantestextos_general.setearPrecioDescuentoPrecioAntiguo(
                                precioDescuento,
                                binding.camposProductosUserVerificados.precioProducto,
                                precio,
                                binding.camposProductosUserVerificados.precioAntiguo,
                                cantidadPorcentajeDescuento,
                                binding.camposProductosUserVerificados.descuentoPorcentaje
                            )
                            binding.camposProductosUserVerificados.precioAntiguo.isVisible = true
                            binding.camposProductosUserVerificados.descuentoPorcentaje.isVisible =
                                true
                        } else {
                            binding.camposProductosUserVerificados.precioAntiguo.isVisible = false
                            binding.camposProductosUserVerificados.descuentoPorcentaje.isVisible =
                                false
                            constantestextos_general.setearPrecioDescuentoPrecioAntiguo(
                                precio,
                                binding.camposProductosUserVerificados.precioProducto
                            )
                        }

                        binding.camposProductosUserVerificados.categoriaProducto.text = categoria
                        binding.camposProductosUserVerificados.marca.text = marca
                        binding.camposProductosUserVerificados.modelo.text = modelo
                        binding.camposProductosUserVerificados.stok.text = stok
                        binding.camposProductosUserVerificados.garantia.text = garantia
                        binding.camposProductosUserVerificados.Condicion.text = condicionProducto
                        binding.camposProductosUserVerificados.descripcion.text = descripcion
                        binding.camposProductosUserVerificados.fechaPublicado.text = fechaPublicada

                        constantestextos_general.extender_acortar_texto(
                            binding.camposProductosUserVerificados.descripcion,
                            binding.camposProductosUserVerificados.tvReadMore
                        )
                        inizializarImgProductosclikeado(this, listaImg, data)

                        binding.camposProductosUserVerificados.comprar.setOnClickListener {
                            val intent =
                                Intent(this, compras_productos_vendedor::class.java).apply {
                                    putExtra("idProducto", productoClikado)
                                    putExtra("idTrabajador", idTrabajador)
                                }
                            startActivity(intent)
                        }

                        binding.containerDatos.isVisible = true
                        binding.cargandoContenido.isVisible = false

                    } catch (e: Exception) {
                        Log.e("obtenerCampos_producto", "Error al procesar los datos: ${e.message}")
                    }

                }, esperaExtra)
            }
        }.addOnFailureListener {
            binding.cargandoContenido.isVisible = false
            Log.e("obtenerCampos_producto", "Error al obtener el documento: ${it.message}")
        }
    }

    fun inizializarImgProductosclikeado(
        context: Context,
        listaImg: MutableList<dataclassMostarImgProductosVendedor>,
        data: Map<String, Any>
    ) {
        // Limpiar la lista para evitar duplicados
        listaImg.clear()

        // Obtener las imágenes del mapa `data`
        val imgPrincipal = data["img_principal"] as? String ?: ""
        val img_url2 = data["img_url2"] as? String ?: ""
        val img_url3 = data["img_url3"] as? String ?: ""
        val img_url4 = data["img_url4"] as? String ?: ""

        // Agregar las imágenes a la lista si no están vacías
        if (imgPrincipal.isNotEmpty()) listaImg.add(dataclassMostarImgProductosVendedor(imgPrincipal))
        if (img_url2.isNotEmpty()) listaImg.add(dataclassMostarImgProductosVendedor(img_url2))
        if (img_url3.isNotEmpty()) listaImg.add(dataclassMostarImgProductosVendedor(img_url3))
        if (img_url4.isNotEmpty()) listaImg.add(dataclassMostarImgProductosVendedor(img_url4))


        // Configurar RecyclerView solo una vez
        val recicle = binding.carrucelImgProductosVentaUser
        if (recicle.adapter == null) {
            recicle.layoutManager =
                LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            recicle.adapter = adapterInicializarRecycleimgProductosTrabajadores(listaImg)
        } else {
            recicle.adapter?.notifyDataSetChanged()
        }
    }


}