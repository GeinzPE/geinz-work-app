package com.geinzz.geinzwork.vistaTiendas


import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.geinzz.geinzwork.R
import com.geinzz.geinzwork.ui.adapters.adapterViewPager
import com.geinzz.geinzwork.databinding.ActivityHistorialComprasBinding
import com.geinzz.geinzwork.vistaTiendas.hisrotiralfr.comprasfr
import com.geinzz.geinzwork.vistaTiendas.hisrotiralfr.reserva_serviciosfr
import com.google.firebase.auth.FirebaseAuth


class historialCompras : AppCompatActivity() {
    private lateinit var binding: ActivityHistorialComprasBinding
    private lateinit var firebaseAuth: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHistorialComprasBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        val viewPage=binding.viewPager
        val tableLayour=binding.tabLayout
        firebaseAuth = FirebaseAuth.getInstance()
        val adapter = adapterViewPager(supportFragmentManager)
        adapter.addFragmet(comprasfr(),"COMPRAS")
        adapter.addFragmet(reserva_serviciosfr(),"RESERVAS")
        viewPage.adapter=adapter
        tableLayour.setupWithViewPager(viewPage)
    }

}