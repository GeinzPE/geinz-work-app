package com.geinzz.geinzwork.vistaTiendas

import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.geinzz.geinzwork.R
import com.geinzz.geinzwork.databinding.ActivityTodasPromocionesSugeridasFiltradoBinding
import com.geinzz.geinzwork.model.dataclassPromociones


class TodasPromocionesSugeridasFiltrado : AppCompatActivity() {
    private lateinit var binding: ActivityTodasPromocionesSugeridasFiltradoBinding
    private val listaPromociones = mutableListOf<dataclassPromociones>()

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTodasPromocionesSugeridasFiltradoBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        val idTienda = intent.getStringExtra("idTienda").toString()
        Log.d("promociones", "obtenemos los $idTienda")
        constantesVistaTiendas.obtenerPromoSugeridas(
            "completa",
            listaPromociones,
            idTienda,
            binding.PromocionesSugeridasRecicle,
            this,
            LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false),true
        )
    }
}