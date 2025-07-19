package com.geinzz.geinzwork.fragmentos.apartados_compra_venta

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.geinzz.geinzwork.Network_internet.BaseActivity
import com.geinzz.geinzwork.ui.adapters.dapter_compra_venta_producto_trabajador
import com.geinzz.geinzwork.model.dataclassPedidoCompraVenta
import com.geinzz.geinzwork.R
import com.geinzz.geinzwork.databinding.ActivityCompraTrabajadorBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class compra_trabajador : BaseActivity() {
    private lateinit var binding: ActivityCompraTrabajadorBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private val lista_ventas = mutableListOf<dataclassPedidoCompraVenta>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCompraTrabajadorBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        firebaseAuth= FirebaseAuth.getInstance()
        obtener_datos_compra()
    }
    private fun obtener_datos_compra() {
        val db = FirebaseFirestore.getInstance()
            .collection("Trabajadores_Usuarios_Drivers")
            .document("trabajadores")
            .collection("trabajadores")
            .document(firebaseAuth.uid.toString())
            .collection("compra_venta").document("compra")
            .collection("compra")

        db.get().addOnSuccessListener { res ->
            val documentos = res.documents
            if (documentos.isEmpty()) {
                inicializar_recicle_view()
                return@addOnSuccessListener
            }

            var pendientes = documentos.size
            for (documento in documentos) {
                val cod_compra = documento.getString("codigo_compra") ?: continue

                val refPedido = FirebaseFirestore.getInstance()
                    .collection("Trabajadores_Usuarios_Drivers")
                    .document("trabajadores")
                    .collection("compra_venta_trabajadores")
                    .document(cod_compra)

                refPedido.get().addOnSuccessListener { doc ->
                    if (doc.exists()) {
                        val data = doc.data ?: emptyMap<String, Any>()
                        val lista = dataclassPedidoCompraVenta(
                            data["cantidad_adquirida"] as? Number ?: 0,
                            data["codigo_compra"] as? String ?: "",
                            data["estado_pedido"] as? String ?: "",
                            data["fecha_pedido"] as? String ?: "",
                            data["hora_pedido"] as? String ?: "",
                            data["id_producto"] as? String ?: "",
                            data["id_usuario"] as? String ?: "",
                            data["id_vendedor"] as? String ?: "",
                            data["id_venta"] as? String ?: "",
                            data["metodo_entrega"] as? String ?: "",
                            data["metodo_pago"] as? String ?: "",
                            data["observaciones"] as? String ?: "",
                            data["pagado"] as? Boolean ?: false,
                            data["total_cancelar"] as? Number ?: 0
                        )
                        lista_ventas.add(lista)
                    }

                    pendientes--
                    if (pendientes == 0) {
                        inicializar_recicle_view()
                    }
                }.addOnFailureListener {
                    pendientes--
                    if (pendientes == 0) {
                        inicializar_recicle_view()
                    }
                }
            }
        }.addOnFailureListener { e ->
            Log.e("CompraDatos", "Error al obtener compras", e)
        }
    }

    private fun inicializar_recicle_view() {
        val recicle = binding.recicleCompra
        recicle.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        recicle.adapter = dapter_compra_venta_producto_trabajador(
            lista_ventas, "compra"

        )
    }

    override fun getRootView(): View = binding.root
}