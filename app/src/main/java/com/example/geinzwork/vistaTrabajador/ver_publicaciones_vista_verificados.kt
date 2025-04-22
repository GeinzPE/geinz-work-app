package com.example.geinzwork.vistaTrabajador

import android.content.Context
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.geinzwork.adapterViewholder.adapter_publicaciones_verificados_trabajos_recientes
import com.example.geinzwork.constantesGeneral.Variables
import com.example.geinzwork.dataclass.dataclas_trabajos_ralizados_verificados
import com.geinzz.geinzwork.R
import com.geinzz.geinzwork.adapterViewholder.publicaciones_ralizadas
import com.geinzz.geinzwork.databinding.ActivityVerPublicacionesVistaVerificadosBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ver_publicaciones_vista_verificados : AppCompatActivity() {
    private lateinit var binding: ActivityVerPublicacionesVistaVerificadosBinding
    private val lista = mutableListOf<dataclas_trabajos_ralizados_verificados>()
    private lateinit var adapter: adapter_publicaciones_verificados_trabajos_recientes
    private lateinit var firebaseAuth: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityVerPublicacionesVistaVerificadosBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        firebaseAuth = FirebaseAuth.getInstance()
        obtener_publicaciones_realizadas(firebaseAuth.uid.toString())
        adapter = adapter_publicaciones_verificados_trabajos_recientes(lista, { item ->
//            eliminarPublicacion(item)
        }, { item ->
//            editarPublicacion(item)
        })
    }

    private fun obtener_publicaciones_realizadas(id: String) {
        val db = FirebaseFirestore.getInstance().collection(Variables.trabajadores_usuariosDB)
            .document(Variables.trabajadoresDB).collection(Variables.trabajadoresDB).document(id)
            .collection("publicaciones_trabajos")
        binding.loading.isVisible = true
        binding.linealNoCuenta.isVisible = false
        binding.recicleViewTrabajos.isVisible = false
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
                binding.loading.isVisible = false
                binding.linealNoCuenta.isVisible = true
            } else {
                binding.loading.isVisible = false
                binding.linealNoCuenta.isVisible = false
                binding.recicleViewTrabajos.isVisible = true
                inicializarRecicle(binding.recicleViewTrabajos, adapter, this)
                binding.linealappLayout.isVisible = true
                adapter.notifyDataSetChanged() // Notifica al adaptador que los datos han cambiado
            }

        }.addOnFailureListener { e ->

        }
    }

    private fun inicializarRecicle(
        recycle: RecyclerView,
        adapter: adapter_publicaciones_verificados_trabajos_recientes, // Cambiado a publicaciones_ralizadas
        context: Context
    ) {
        recycle.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        recycle.adapter = adapter
    }
}