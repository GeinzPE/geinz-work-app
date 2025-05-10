package com.geinzz.geinzwork

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import com.example.geinzwork.constantesGeneral.Variables
import com.geinzz.geinzwork.databinding.ActivityEditarInfoBinding
import com.geinzz.geinzwork.hora.timePickterFracment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore

class EditarInfo : AppCompatActivity() {
    private lateinit var binding: ActivityEditarInfoBinding
    private lateinit var firebaseAuth: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditarInfoBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        firebaseAuth = FirebaseAuth.getInstance()
        val tipoCuenta = intent.getStringExtra(Variables.TipoCuenta).toString()
        val tipo = intent.getStringExtra(Variables.tipo).toString()
        var valor = intent.getStringExtra(Variables.valor).toString()
        val referenciaTrabajador =
            FirebaseFirestore.getInstance().collection(Variables.trabajadores_usuariosDB)
                .document(Variables.trabajadoresDB).collection(Variables.trabajadoresDB)
                .document(firebaseAuth.uid.toString())
        val referenciaUsuario =
            FirebaseFirestore.getInstance().collection(Variables.trabajadores_usuariosDB)
                .document(Variables.usuarios_db).collection(Variables.usuarios_db)
                .document(firebaseAuth.uid.toString())

        when (tipoCuenta) {
            "Cuenta Simple" -> {
                if (tipo != null && valor != null) {
                    when (tipo) {
                        "nombre" -> {
                            binding.informacion.text = "nombre"
                            binding.nombre.setText(valor)
                            binding.apellidoInput.visibility = View.GONE
                            binding.fechaInput.visibility = View.GONE
                            binding.telefonoInput.visibility = View.GONE
                            binding.caracteristicasTrabajos.isVisible = false
                            binding.enviar.setOnClickListener {
                                val nombre = binding.nombre.text.toString()
                                ActulizarDatos(referenciaUsuario, nombre, "nombre")
                            }
                        }

                        "numeroT" -> {
                            binding.informacion.text = "telefono"
                            binding.telefono.setText(valor)
                            binding.telefonoInput.isVisible = true
                            binding.apellidoInput.visibility = View.GONE
                            binding.nombreInput.visibility = View.GONE
                            binding.fechaInput.visibility = View.GONE
                            binding.caracteristicasTrabajos.isVisible = false
                            binding.enviar.setOnClickListener {
                                val telefono = binding.telefono.text.toString()
                                ActulizarDatos(referenciaUsuario, telefono, "numero")
                            }
                        }

                        "apellido" -> {
                            binding.informacion.text = "apellido"
                            println("Se envió el apellido: $valor")
                            binding.apellido.setText(valor)
                            binding.telefonoInput.visibility = View.GONE
                            binding.nombreInput.visibility = View.GONE
                            binding.fechaInput.visibility = View.GONE
                            binding.caracteristicasTrabajos.isVisible = false
                            binding.enviar.setOnClickListener {
                                val apellido = binding.apellido.text.toString()
                                ActulizarDatos(referenciaUsuario, apellido, "apellido")
                            }
                        }

                        "fecha" -> {
                            binding.informacion.text = "fecha de nacimiento"
                            binding.fecha.setText(valor)
                            binding.nombreInput.visibility = View.GONE
                            binding.telefonoInput.visibility = View.GONE
                            binding.apellidoInput.visibility = View.GONE
                            binding.telefonoInput.visibility = View.GONE
                            binding.caracteristicasTrabajos.isVisible = false
                            binding.enviar.setOnClickListener {
                                val fechaN = binding.fecha.text.toString()
                                ActulizarDatos(referenciaUsuario, fechaN, "fechaNac")
                            }
                        }

                        else -> {
                            println("Tipo de valor desconocido: $tipo")
                        }
                    }
                }
            }

            "Cuenta Trabajador" -> {
                if (tipo != null && valor != null) {
                    when (tipo) {
                        "nombre" -> {
                            binding.informacion.text = "nombre"
                            binding.nombre.setText(valor)
                            binding.apellidoInput.visibility = View.GONE
                            binding.fechaInput.visibility = View.GONE
                            binding.telefonoInput.visibility = View.GONE
                            binding.caracteristicasTrabajos.isVisible = false
                            binding.enviar.setOnClickListener {
                                val nombre = binding.nombre.text.toString()
                                ActulizarDatos(referenciaTrabajador, nombre, "nombre")
                            }
                        }

                        "numeroT" -> {
                            binding.informacion.text = "telefono"
                            binding.apellidoInput.visibility = View.GONE
                            binding.nombreInput.visibility = View.GONE
                            binding.fechaInput.visibility = View.GONE
                            binding.caracteristicasTrabajos.isVisible = false
                            binding.enviar.setOnClickListener {
                                val telefono = binding.telefono.text.toString()
                                ActulizarDatos(referenciaTrabajador, telefono, "numero")
                            }
                        }

                        "apellido" -> {
                            binding.informacion.text = "apellido"
                            println("Se envió el apellido: $valor")
                            binding.apellido.setText(valor)
                            binding.telefonoInput.visibility = View.GONE
                            binding.nombreInput.visibility = View.GONE
                            binding.fechaInput.visibility = View.GONE
                            binding.caracteristicasTrabajos.isVisible = false
                            binding.enviar.setOnClickListener {
                                val apellido = binding.apellido.text.toString()
                                ActulizarDatos(referenciaTrabajador, apellido, "apellido")
                            }
                        }

                        "fecha" -> {
                            binding.informacion.text = "fecha de nacimiento"
                            binding.fecha.setText(valor)
                            binding.nombreInput.visibility = View.GONE
                            binding.telefonoInput.visibility = View.GONE
                            binding.apellidoInput.visibility = View.GONE
                            binding.caracteristicasTrabajos.isVisible = false
                            binding.enviar.setOnClickListener {
                                val fechaN = binding.fecha.text.toString()
                                ActulizarDatos(referenciaTrabajador, fechaN, "fechaNac")
                            }
                        }

                        "descripcion" -> {
                            binding.fechaInput.isVisible = false
                            binding.informacion.text = "Descripcion"
                            binding.caracteristicasTrabajosED.setText(valor)
                            binding.nombreInput.visibility = View.GONE
                            binding.telefonoInput.visibility = View.GONE
                            binding.apellidoInput.visibility = View.GONE
                            binding.caracteristicasTrabajos.isVisible = true
                            binding.enviar.setOnClickListener {
                                val fechaN = binding.caracteristicasTrabajosED.text.toString()
                                ActulizarDatos(referenciaTrabajador, fechaN, "descripcion")
                            }
                        }

                        else -> {
                            println("Tipo de valor desconocido: $tipo")
                        }
                    }
                }
            }

        }

    }


    private fun ActulizarDatos(
        dbDocumento: DocumentReference,
        valorCambiado: String,
        instaDB: String
    ) {
        dbDocumento.get()
            .addOnSuccessListener { documentSnapshot ->
                if (documentSnapshot.exists()) {
                    val hashMap = hashMapOf<String, Any>(
                        "$instaDB" to "$valorCambiado"
                    )
                    dbDocumento.update(hashMap)
                        .addOnSuccessListener {
                            Toast.makeText(
                                this,
                                "Campo actualizaco correctamente en unos momentos veras tus cambios",
                                Toast.LENGTH_SHORT
                            )
                                .show()
                        }
                        .addOnFailureListener { e ->
                            Toast.makeText(
                                this,
                                "Error al actualizar usuario: ${e.message}",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                }
            }
    }


}