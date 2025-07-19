package com.geinzz.geinzwork.FuncionalidadGeinz

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.geinzz.geinzwork.R
import com.geinzz.geinzwork.databinding.ActivityCuentaInfoBinding

class Cuenta_Info : AppCompatActivity() {
    private lateinit var binding:ActivityCuentaInfoBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding=ActivityCuentaInfoBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        binding.Finalizar.setOnClickListener {
            startActivity(Intent(this, Cuenta_Info::class.java))
        }
    }
}