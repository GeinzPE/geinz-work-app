package com.geinzz.geinzwork

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import com.geinzz.geinzwork.databinding.ActivityCreaTuNoticiaBinding
import com.geinzz.geinzwork.servicios_geinz.planes_noticias_servicios_geinz

class crea_tu_noticia : AppCompatActivity() {
    private lateinit var binding:ActivityCreaTuNoticiaBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding=ActivityCreaTuNoticiaBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        binding.verPlanes.setOnClickListener {
            val intent=Intent(this,planes_noticias_servicios_geinz::class.java)
            startActivity(intent)
        }
        setearNoticias()
    }
    private fun setearNoticias(){
        val itemAnuncios=binding.itemnAnuncios
        itemAnuncios.cargaLike.isVisible=false
        itemAnuncios.contanierActividades.isVisible=true
        itemAnuncios.linealMeGusta.isVisible=true
        itemAnuncios.contadorLike.text="100"
        itemAnuncios.propietario.text="Propietario de la noticia"
        itemAnuncios.contenidotext.text="Contenido de la noticia"
        itemAnuncios.venceTexto.text="12-12-2024"
        itemAnuncios.descuentoPorcentaje.text="-10% OFF"
        itemAnuncios.descuentoPorcentaje.isVisible=true
        itemAnuncios.imganeAnuncio.setImageResource(R.drawable.noticia_crea_geinz)

    }
}


