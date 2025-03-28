package com.example.geinzwork.fragmentos.productosPublicadosVista

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.geinzz.geinzwork.R
import com.geinzz.geinzwork.databinding.ActivityVerMasProductosPublicadosTrabajadoresBinding
import com.google.firebase.firestore.FirebaseFirestore

class ver_mas_productos_publicados_trabajadores : AppCompatActivity() {
    private lateinit var binding: ActivityVerMasProductosPublicadosTrabajadoresBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityVerMasProductosPublicadosTrabajadoresBinding.inflate(layoutInflater)

        enableEdgeToEdge()
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val idTrabajdor = intent.getBundleExtra("idTrabajador").toString()

    }

    private fun obtenerCategoriasFiltradosProductos(){

    }

    private fun obtener_categoriasDescuentos(){

    }

    private fun obtenerProductosGenerales(){
        val db = FirebaseFirestore.getInstance().collection("Trabajadores_Usuarios_Drivers")
            .document("trabajadores").collection("trabajadores").document(idTrabajador)
            .collection("productos_venta")
        db.get().addOnSuccessListener { res->
            for(datos in res){
                val data=res.da
            }
        }
    }
}