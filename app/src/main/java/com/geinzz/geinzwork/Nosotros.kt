package com.geinzz.geinzwork

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.geinzz.geinzwork.FuncionalidadGeinz.comoUsar
import com.geinzz.geinzwork.databinding.ActivityNosotrosBinding

class Nosotros : AppCompatActivity() {
    private lateinit var binding: ActivityNosotrosBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNosotrosBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        binding.verMas.setOnClickListener {
            startActivity(Intent(this, Crea_tu_publicidad::class.java))
        }
        binding.comoUsarGeinz.setOnClickListener {
            startActivity(Intent(this, comoUsar::class.java))

        }
    }
}