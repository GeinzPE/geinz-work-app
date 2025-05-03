package com.geinzz.geinzwork

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.geinzz.geinzwork.databinding.ActivityPasswordYuserBinding
import com.geinzz.geinzwork.databinding.ActivitySeleccionDeTrabajoBinding
import com.google.firebase.firestore.FirebaseFirestore

class passwordYuser : AppCompatActivity() {
    private lateinit var binding: ActivityPasswordYuserBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPasswordYuserBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        var titulo = intent.getStringExtra("titulo")

        binding.EstasPasos.text = "Estas a 2/2 pasos"
        val tipoCuenta = intent.getStringExtra("tipoCuenta")
        when (tipoCuenta) {
            "cuentaTrabajador" -> {
                binding.btnLogin.setOnClickListener {
                    enviarDatos()
                }
                binding.freelancer.text = "Cuenta Trabajador"
            }

            "cuentaSimple" -> {
                binding.btnLogin.setOnClickListener {
                    enviarDatosCuentaSimple()
                }
                binding.freelancer.text = "Cuenta Simple"
            }
        }
    }


    private fun enviarDatos() {
        var categoria = intent.getStringExtra("Categoria").toString().trim()
        var tipoTrabajo = intent.getStringExtra("tipo").toString().trim()
        var nombre = intent.getStringExtra("nombre").toString().trim()
        var apellidoUser = intent.getStringExtra("apellidoUser").toString().trim()
        var fechanaciminetoUSer = intent.getStringExtra("fechanaciminetoUSer").toString().trim()
        var amUSer = intent.getStringExtra("amUSer").toString().trim()
        var pmUser = intent.getStringExtra("pmUser").toString().trim()
        var nacionalidadUSer = intent.getStringExtra("nacionalidadUSer").toString().trim()
        var descripcion = intent.getStringExtra("descripcion").toString().trim()
        var localidad = intent.getStringExtra("localida")
        var countryCode = intent.getStringExtra("codigo").toString().trim()
        var NumeroCelular = intent.getStringExtra("numero")
        var email = binding.correoElectronicoFree.text.toString().trim()
        var pass1 = binding.ContraFree.text.toString().trim()
        var pass2 = binding.repetircontraFree.text.toString().trim()
        var genero = intent.getStringExtra("genero")
        var edadActualUser = intent.getStringExtra("edadActual")
        val Nombre_usuario = binding.nombreUsuario.text.toString()
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.correoElectronicoFree.error = "EMAIL INVALIDO"
            binding.correoElectronicoFree.requestFocus()
        } else if (pass1.isEmpty()) {
            binding.ContraFree.error = "Ingrese una contraseña"
        } else if (pass2.isEmpty()) {
            binding.ContraFree.error = "Ingrese una contraseña"
        } else if (pass1 != pass2) {
            binding.repetircontraFree.error = "las contraseñas no coinciden"
        } else if (Nombre_usuario.isEmpty()) {
            binding.repetircontraFree.error = "Escribe tu nombre de usaurio"
        } else {
            verificar_existencia_nombre_usuario(Nombre_usuario) { existe ->
                if (existe) {
                    binding.nombreUsuario.error = "Este nombre de usuario ya existe, elige otro"
                } else {
                    var vista = Intent(this, veirificacionDatos::class.java)
                    val tipoCuenta = intent.getStringExtra("tipoCuenta")
                    vista.putExtra("tipoCuenta", tipoCuenta)
                    vista.putExtra("Categoria", categoria)
                    vista.putExtra("tipo", tipoTrabajo)
                    vista.putExtra("nombre", nombre)
                    vista.putExtra("apellidoUser", apellidoUser)
                    vista.putExtra("fechanaciminetoUSer", fechanaciminetoUSer)
                    vista.putExtra("amUSer", amUSer)
                    vista.putExtra("pmUser", pmUser)
                    vista.putExtra("nacionalidadUSer", nacionalidadUSer)
                    vista.putExtra("descripcion", descripcion)
                    vista.putExtra("correoUSer", email)
                    vista.putExtra("contra", pass1)
                    vista.putExtra("genero", genero)
                    vista.putExtra("codigo", countryCode)
                    vista.putExtra("numero", NumeroCelular)
                    vista.putExtra("localidad", localidad)
                    vista.putExtra("edadActual", edadActualUser)
                    vista.putExtra("nombre_user", Nombre_usuario)
                    startActivity(vista)
                    Log.d("timeFreP", pmUser)
                }
            }

        }

    }

    private fun enviarDatosCuentaSimple() {
        val tipoCuenta = intent.getStringExtra("tipoCuenta")
        var nombre = intent.getStringExtra("nombreUSer").toString().trim()
        var apellidoUser = intent.getStringExtra("apellidoUser").toString().trim()
        var fechanaciminetoUSer = intent.getStringExtra("fechanaciminetoUSer").toString().trim()
        var nacionalidadUSer = intent.getStringExtra("nacionalidadUSer").toString().trim()
        var localidad = intent.getStringExtra("localida")
        var countryCode = intent.getStringExtra("codigo").toString().trim()
        var NumeroCelular = intent.getStringExtra("numero")
        var email = binding.correoElectronicoFree.text.toString().trim()
        var pass1 = binding.ContraFree.text.toString().trim()
        var pass2 = binding.repetircontraFree.text.toString().trim()
        var genero = intent.getStringExtra("genero")
        var edadActualUser = intent.getStringExtra("edadActual")
        val Nombre_usuario = binding.nombreUsuario.text.toString()
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.correoElectronicoFree.error = "EMAIL INVALIDO"
            binding.correoElectronicoFree.requestFocus()
        } else if (pass1.isEmpty()) {
            binding.ContraFree.error = "Ingrese una contraseña"
        } else if (pass2.isEmpty()) {
            binding.ContraFree.error = "Ingrese una contraseña"
        } else if (pass1 != pass2) {
            binding.repetircontraFree.error = "las contraseñas no coinciden"
        } else if (Nombre_usuario.isEmpty()) {
            binding.repetircontraFree.error = "Escribe tu nombre de usaurio"
        } else {
            verificar_existencia_nombre_usuario(Nombre_usuario) { existe ->
                if (existe) {
                    binding.nombreUsuario.error = "Este nombre de usuario ya existe, elige otro"
                } else {
                    var vista = Intent(this, veirificacionDatos::class.java)
                    var titulo = intent.getStringExtra("Title")
                    vista.putExtra("titulo", titulo)
                    vista.putExtra("tipoCuenta", tipoCuenta)
                    vista.putExtra("nombre", nombre)
                    vista.putExtra("apellidoUser", apellidoUser)
                    vista.putExtra("fechanaciminetoUSer", fechanaciminetoUSer)
                    vista.putExtra("nacionalidadUSer", nacionalidadUSer)
                    vista.putExtra("correoUSer", email)
                    vista.putExtra("contra", pass1)
                    vista.putExtra("genero", genero)
                    vista.putExtra("codigo", countryCode)
                    vista.putExtra("numero", NumeroCelular)
                    vista.putExtra("localidad", localidad)
                    vista.putExtra("edadActual", edadActualUser)
                    vista.putExtra("nombre_user", Nombre_usuario)
                    startActivity(vista)

                }
            }

        }
    }

    private fun verificar_existencia_nombre_usuario(
        rawNombreUsuario: String,
        callback: (Boolean) -> Unit
    ) {
        val nombreUsuario = if (rawNombreUsuario.startsWith("@")) rawNombreUsuario else "@$rawNombreUsuario"

        val db = FirebaseFirestore.getInstance()
            .collection("Trabajadores_Usuarios_Drivers")
            .document("nombres_user")
            .collection("nombres_user")

        db.whereEqualTo("nombres_user", nombreUsuario)
            .get()
            .addOnSuccessListener { res ->
                callback(!res.isEmpty) // true si existe, false si no
            }
            .addOnFailureListener { e ->
                println("Error al verificar nombre de usuario: $e")
                callback(false)
            }
    }

}