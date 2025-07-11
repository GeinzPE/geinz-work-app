package com.example.geinzwork.fragmentos.apartados_compra_venta

import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.geinzwork.adapterViewholder.dapter_compra_venta_producto_trabajador
import com.example.geinzwork.dataclass.dataclassPedidoCompraVenta
import com.geinzz.geinzwork.R
import com.geinzz.geinzwork.adapterViewholder.adapterCategorias
import com.geinzz.geinzwork.constantesGeneral.constantes
import com.geinzz.geinzwork.constantesGeneral.constantesTrabajadoresTiendasInicioFragmet
import com.geinzz.geinzwork.constantesGeneral.constantesTrabajadoresTiendasInicioFragmet.verificar_dialog_seguir_guardar_trabajador
import com.geinzz.geinzwork.databinding.ActivityVentaTrabajadorBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import org.w3c.dom.Document

class venta_trabajador : AppCompatActivity() {
    private lateinit var binding: ActivityVentaTrabajadorBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private val lista_ventas = mutableListOf<dataclassPedidoCompraVenta>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityVentaTrabajadorBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        firebaseAuth = FirebaseAuth.getInstance()
        obtener_datos_venta()
    }

    private fun obtener_datos_venta() {
        val db = FirebaseFirestore.getInstance()
            .collection("Trabajadores_Usuarios_Drivers")
            .document("trabajadores").collection("trabajadores")
            .document(firebaseAuth.uid.toString())
            .collection("compra_venta").document("venta")
            .collection("venta")

        db.get().addOnSuccessListener { res ->
            for (documento in res) {
                val data = documento.data
                val cod_compra = data["codigo_compra"] as? String ?: ""
                val dbcompra_venta = FirebaseFirestore.getInstance()
                    .collection("Trabajadores_Usuarios_Drivers")
                    .document("trabajadores").collection("compra_venta_trabajadores")
                    .document(cod_compra)
                dbcompra_venta.get().addOnSuccessListener { res ->
                    if (res.exists()) {
                        val data = res.data
                        val cantidad_adquirida = data?.get("cantidad_adquirida") as? Number ?: 0
                        val codigo_compra =  data?.get("codigo_compra") as? String ?: ""
                        val estado_pedido =  data?.get("estado_pedido") as? String ?: ""
                        val fecha_pedido =  data?.get("fecha_pedido") as? String ?: ""
                        val hora_pedido =  data?.get("hora_pedido") as? String ?: ""
                        val id_producto =  data?.get("id_producto")as? String ?: ""
                        val id_usuario =  data?.get("id_usuario") as? String ?: ""
                        val id_vendedor =  data?.get("id_vendedor") as? String ?: ""
                        val id_venta =  data?.get("id_venta") as? String ?: ""
                        val metodo_entrega =  data?.get("metodo_entrega") as? String ?: ""
                        val metodo_pago =  data?.get("metodo_pago") as? String ?: ""
                        val observaciones =  data?.get("observaciones") as? String ?: ""
                        val pagado =  data?.get("pagado") as? Boolean ?: false
                        val total_cancelar =  data?.get("total_cancelar") as? Number ?: 0
                        val lista = dataclassPedidoCompraVenta(
                            cantidad_adquirida,
                            codigo_compra,
                            estado_pedido,
                            fecha_pedido,
                            hora_pedido,
                            id_producto,
                            id_usuario,
                            id_vendedor,
                            id_venta,
                            metodo_entrega,
                            metodo_pago,
                            observaciones,
                            pagado,
                            total_cancelar
                        )

                        lista_ventas.add(lista)
                        if (lista_ventas.isNotEmpty()) {
                            inicializar_recicle_view(db.document(id_venta))
                        }
                    }
                }


            }
        }.addOnFailureListener { e ->
            Log.e("CompraDatos", "Error al obtener compras", e)
        }
    }

    private fun inicializar_recicle_view(document: DocumentReference) {
        val recicle = binding.recicleVentas
        recicle.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        recicle.adapter = dapter_compra_venta_producto_trabajador(
            lista_ventas, "venta"

        )
    }


}