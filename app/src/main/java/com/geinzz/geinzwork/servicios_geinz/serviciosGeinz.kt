package com.geinzz.geinzwork.servicios_geinz


import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import com.geinzz.geinzwork.Login
import com.geinzz.geinzwork.R
import com.geinzz.geinzwork.ui.adapters.adapterViewPager
import com.geinzz.geinzwork.databinding.ActivityServiciosGeinzBinding
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDragHandleView
import com.google.firebase.auth.FirebaseAuth

class serviciosGeinz : AppCompatActivity() {
    private lateinit var binding: ActivityServiciosGeinzBinding
    lateinit var bottomSheet: BottomSheetDragHandleView
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var dialog: BottomSheetDialog
    private val categoriasTiendas = ArrayList<String>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityServiciosGeinzBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        firebaseAuth = FirebaseAuth.getInstance()
        if (firebaseAuth.currentUser != null) {
            val viewPage = binding.viewPager
            val tableLayour = binding.tabLayout
            binding.textAboveTabLayout.isVisible = true
            tableLayour.isVisible = true
            viewPage.isVisible = true
            binding.sinRegistro.isVisible = false
            val adapter = adapterViewPager(supportFragmentManager)
            adapter.addFragmet(inicio_servicios_fragment(), "Servicios")
            adapter.addFragmet(servicios_activos(), "Servicios activos")
            viewPage.adapter = adapter
            tableLayour.setupWithViewPager(viewPage)
        } else {
            binding.sinRegistro.isVisible = true
            binding.viewPager.isVisible = false
            binding.tabLayout.isVisible = false
            binding.textAboveTabLayout.isVisible = false
            binding.iniciarSeccion.setOnClickListener {
                val vista = Intent(this, Login::class.java).apply {
                    putExtra("dato", "dispositivos")
                }
                startActivity(vista)
            }
        }

    }

    override fun onResume() {
        super.onResume()
        if (firebaseAuth.currentUser != null) {
            val viewPage = binding.viewPager
            val tableLayour = binding.tabLayout
            binding.textAboveTabLayout.isVisible = true
            tableLayour.isVisible = true
            viewPage.isVisible = true
            binding.sinRegistro.isVisible = false
            val adapter = adapterViewPager(supportFragmentManager)
            adapter.addFragmet(inicio_servicios_fragment(), "Servicios")
            adapter.addFragmet(servicios_activos(), "Servicios activos")

            viewPage.adapter = adapter
            tableLayour.setupWithViewPager(viewPage)
        } else {
            binding.sinRegistro.isVisible = true
            binding.viewPager.isVisible = false
            binding.tabLayout.isVisible = false
            binding.textAboveTabLayout.isVisible = false
            binding.iniciarSeccion.setOnClickListener {
                val vista = Intent(this, Login::class.java).apply {
                    putExtra("dato", "dispositivos")
                }
                startActivity(vista)
            }
        }
    }

}