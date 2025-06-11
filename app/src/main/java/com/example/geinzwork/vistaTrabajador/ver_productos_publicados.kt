package com.example.geinzwork.vistaTrabajador

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
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
import com.geinzz.geinzwork.databinding.ActivityVerProductosPublicadosBinding
import com.geinzz.geinzwork.databinding.BottomSheetCamposTrPdPBinding
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

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
//            eliminarPublicacion(item)

        }
        editar.setOnClickListener {
//            editar_publicaciones(item.id_publicacion.toString())
        }

        dialog.setContentView(view)
    }

    private fun obtener_productos() {
        val db = FirebaseFirestore.getInstance().collection("Trabajadores_Usuarios_Drivers")
            .document("trabajadores").collection("trabajadores")
            .document(firebaseAuth.uid.toString()).collection("productos_venta")
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