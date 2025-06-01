package com.example.geinzwork

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.ArrayAdapter
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.viewpager2.widget.ViewPager2
import com.example.geinzwork.adapterViewholder.adapter_viewpager_guarda
import com.example.geinzwork.constantesGeneral.Variables
import com.geinzz.geinzwork.R
import com.geinzz.geinzwork.adapterViewholder.adapter
import com.geinzz.geinzwork.adapterViewholder.adapterguardados
import com.geinzz.geinzwork.constantesGeneral.constantesNoticias.firebaseAuth
import com.geinzz.geinzwork.databinding.ActivityNoticiasTrabajadoresGuardadosBinding
import com.geinzz.geinzwork.dataclass.dataClassTrabajosd
import com.geinzz.geinzwork.dataclass.dataclassVerGuardados
import com.google.android.material.chip.Chip
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class noticias_trabajadores_guardados : AppCompatActivity() {
    private lateinit var binding: ActivityNoticiasTrabajadoresGuardadosBinding
    private lateinit var firebaseAuth: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNoticiasTrabajadoresGuardadosBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        firebaseAuth = FirebaseAuth.getInstance()


        val viewPager = binding.viewPager2
        viewPager.adapter = adapter_viewpager_guarda(this)
        viewPager.orientation = ViewPager2.ORIENTATION_HORIZONTAL // o simplemente elimínala

    }
}