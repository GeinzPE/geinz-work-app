package com.geinzz.geinzwork.vistaTiendas

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.geinzz.geinzwork.R
import com.geinzz.geinzwork.ui.adapters.adapterServicios
import com.geinzz.geinzwork.databinding.ActivityVerMasServiciosBinding
import com.geinzz.geinzwork.model.dataclassServicios
import com.google.firebase.firestore.FirebaseFirestore

class ver_mas_servicios : AppCompatActivity() {
    private lateinit var binding:ActivityVerMasServiciosBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding=ActivityVerMasServiciosBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        val idTienda=intent.getStringExtra("idTienda").toString()
        obtenerServicios(idTienda)
    }

    private fun obtenerServicios(idTienda: String) {
        val listaServicios = mutableListOf<dataclassServicios>() // Inicializamos la lista de servicios vacía

        val db = FirebaseFirestore.getInstance()
            .collection("Tiendas")
            .document(idTienda).collection("servicios")
        db.get().addOnSuccessListener { res ->
            for (datos  in res) {
                val nombre = datos.getString("nombre") ?: ""
                val urlimg = datos.getString("UrlImg") ?: ""
                val precio = datos.getString("precio") ?: ""
                val descripcion = datos.getString("descripcion") ?: ""
                val id = datos.getString("id") ?: ""
                val idTienda = datos.getString("idTienda") ?: ""
                val reserva=datos.getBoolean("reserva")?:false
                val plin=datos.getBoolean("plin")?:false
                val yape=datos.getBoolean("yape")?:false
                val efectivo=datos.getBoolean("efectivo")?:false

                val servicio = dataclassServicios(urlimg, nombre, precio,descripcion,id,idTienda,efectivo,yape,plin,reserva)
                listaServicios.add(servicio)
            }

            inicializarRecicleServicios(listaServicios, binding.recicleServiciosCompletos)
        }.addOnFailureListener { e ->

            Log.w("TAG", "Error al obtener servicios", e)
        }
    }
    private fun inicializarRecicleServicios(
        listaServicios: MutableList<dataclassServicios>,
        recyclerContainer: RecyclerView
    ) {

        val adaptador = adapterServicios(listaServicios,{dataclassServicios -> vistaverServicios(dataclassServicios) })

        recyclerContainer.apply {
            layoutManager = GridLayoutManager(this@ver_mas_servicios, 2)
            adapter = adaptador
        }
    }

    private fun vistaverServicios(dataclassServicios: dataclassServicios){
        var vista= Intent(this,verServicios::class.java).apply {
            putExtra("idServicio", dataclassServicios.idServicio)
            putExtra("idTienda", dataclassServicios.idTienda)
        }
        startActivity(vista)
    }

}