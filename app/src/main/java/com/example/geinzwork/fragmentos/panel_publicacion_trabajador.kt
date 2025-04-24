package com.example.geinzwork.fragmentos

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import com.example.geinzwork.constantesGeneral.Variables
import com.example.geinzwork.crear_publicacion_productos_trabajadores
import com.example.geinzwork.crear_publicaciones_recientes
import com.geinzz.geinzwork.R
import com.geinzz.geinzwork.crear_trabajos_realizados
import com.geinzz.geinzwork.databinding.ActivityPanelPublicacionTrabajadorBinding
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class panel_publicacion_trabajador : AppCompatActivity() {
    private lateinit var binding: ActivityPanelPublicacionTrabajadorBinding
    private lateinit var firebaseAuth: FirebaseAuth


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPanelPublicacionTrabajadorBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        setear_datos_includes()

    }

    private fun setear_datos_includes() {
        val plan = intent.getStringExtra(Variables.plan)
        binding.traajosRecientes.tituloServico.text = "Trabajos Recientes"
        binding.publicaciones.tituloServico.text = "Publicaciones"
        binding.productosVenta.tituloServico.text = "Productos en venta"
        binding.traajosRecientes.fechaActivotxt.text = "Publicaciones activas"
        binding.traajosRecientes.fechatxtVenimiento.text = "Publicaciones restantes"
        binding.productosVenta.fechaActivotxt.text = "Publicaciones activas"
        binding.productosVenta.fechatxtVenimiento.text = "Publicaciones restantes"
        binding.publicaciones.fechaActivotxt.text = "Publicaciones activas"
        binding.publicaciones.fechatxtVenimiento.text = "Publicaciones restantes"

        val imageView = binding.traajosRecientes.imgServicio
        imageView.setImageResource(R.drawable.crea_publicaciones)
        val imageView2 = binding.publicaciones.imgServicio
        imageView2.setImageResource(R.drawable.agregar_trabajos)
        val imageView3 = binding.productosVenta.imgServicio
        imageView3.setImageResource(R.drawable.agrega_productos_perfil)

        imageView.setOnClickListener {
            var vista = Intent(this, crear_trabajos_realizados::class.java).apply {
                putExtra(Variables.plan, plan)
            }
            startActivity(vista)


        }
        imageView2.setOnClickListener {
            var vista = Intent(this, crear_publicaciones_recientes::class.java).apply {
                putExtra(Variables.plan, plan)
            }
            startActivity(vista)

        }

        imageView3.setOnClickListener {
            var vista = Intent(this, crear_publicacion_productos_trabajadores::class.java).apply {
                putExtra(Variables.plan, plan)
            }
            startActivity(vista)
        }
    }





}