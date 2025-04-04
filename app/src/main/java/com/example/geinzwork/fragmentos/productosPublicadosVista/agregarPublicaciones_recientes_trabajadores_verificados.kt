package com.example.geinzwork.fragmentos.productosPublicadosVista

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.geinzz.geinzwork.R
import com.geinzz.geinzwork.databinding.ActivityAgregarPublicacionesRecientesTrabajadoresVerificadosBinding

class agregarPublicaciones_recientes_trabajadores_verificados : AppCompatActivity() {
    private lateinit var binding:ActivityAgregarPublicacionesRecientesTrabajadoresVerificadosBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding=ActivityAgregarPublicacionesRecientesTrabajadoresVerificadosBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun crearPublicacionReciente(){

    }
}