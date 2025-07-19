package com.geinzz.geinzwork.vistaTiendas

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.geinzz.geinzwork.R
import com.geinzz.geinzwork.ui.adapters.adapterViewPager
import com.geinzz.geinzwork.databinding.ActivityTiendasGeneralesBinding

class TiendasGenerales : AppCompatActivity() {
    private lateinit var binding: ActivityTiendasGeneralesBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTiendasGeneralesBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        val viewPage=binding.viewPager
        val tableLayour=binding.tabLayout
        val filtrado=intent.getStringExtra("filtradoPasado").toString()

        val adapter = adapterViewPager(supportFragmentManager)
        adapter.addFragmet(inicio_tiendas_fr.newInstance(filtrado), "Inicio")
        adapter.addFragmet(categorias_tiendas_fr.newInstance(filtrado),"Categorias")
        adapter.addFragmet(Fragment_trabajaConNosotros_tienda(),"Contacto")

        viewPage.adapter=adapter
        tableLayour.setupWithViewPager(viewPage)

    }

}