package com.geinzz.geinzwork

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.geinzz.geinzwork.utils.constantes.constantes.Variables
import com.geinzz.geinzwork.databinding.ActivityCaracteristicaTrabajoBinding

class caracteristicaTrabajo : AppCompatActivity() {
    private lateinit var binding: ActivityCaracteristicaTrabajoBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCaracteristicaTrabajoBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        SetearDAtos()
        binding.btnLogin.setOnClickListener {
            enviarDatos()

        }

    }

    private fun enviarDatos() {
        var categoria = binding.cat.text.toString().trim()
        var tipoTrabajo = binding.tipoTrabajores.text.toString().trim()
        var nombre = intent.getStringExtra(Variables.nombre).toString().trim()
        var apellidoUser = intent.getStringExtra(Variables.apellidoUser).toString().trim()
        var fechanaciminetoUSer = intent.getStringExtra(Variables.fechanaciminetoUSer).toString().trim()
        var amUSer = intent.getStringExtra(Variables.amUSer).toString().trim()
        var pmUser = intent.getStringExtra(Variables.pmUser).toString().trim()
        var nacionalidadUSer = intent.getStringExtra(Variables.nacionalidadUSer).toString().trim()
        var descripcion_trabajo=binding.descripcionServiciosED.text.toString().trim()
        var genero=intent.getStringExtra(Variables.genero)
        var localidad=intent.getStringExtra(Variables.localida)
        var countryCode = intent.getStringExtra(Variables.codigo).toString().trim()
        var NumeroCelular=intent.getStringExtra(Variables.numero)
        var edadActualUser = intent.getStringExtra(Variables.edadActual)

        var vista=Intent(this,passwordYuser::class.java)
        vista.putExtra(Variables.tipoCuenta,Variables.cuentaTrabajador)
        vista.putExtra(Variables.Categoria, categoria)
        vista.putExtra(Variables.tipo, tipoTrabajo)
        vista.putExtra(Variables.nombre,nombre)
        vista.putExtra(Variables.apellidoUser,apellidoUser)
        vista.putExtra(Variables.fechanaciminetoUSer,fechanaciminetoUSer)
        vista.putExtra(Variables.amUSer,amUSer)
        vista.putExtra(Variables.pmUser,pmUser)
        vista.putExtra(Variables.nacionalidadUSer,nacionalidadUSer)
        vista.putExtra(Variables.descripcion,descripcion_trabajo)
        vista.putExtra(Variables.genero,genero)
        vista.putExtra(Variables.localida,localidad)
        vista.putExtra(Variables.codigo,countryCode)
        vista.putExtra(Variables.numero,NumeroCelular)
        vista.putExtra(Variables.edadActual,edadActualUser)
        startActivity(vista)
        Log.d("timeFreP", pmUser)
    }

    private fun SetearDAtos() {
        var categoria = intent.getStringExtra(Variables.Categoria)
        var tipoTrabajo = intent.getStringExtra(Variables.tipo)
        var seterocat = categoria.toString().trim()
        var setTipoTrabajo = tipoTrabajo.toString().trim()

        binding.cat.text = seterocat
        binding.tipoTrabajores.text = setTipoTrabajo
    }
}