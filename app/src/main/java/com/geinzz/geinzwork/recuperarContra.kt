package com.geinzz.geinzwork

import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.geinzz.geinzwork.utils.constantes.constantes.Variables
import com.geinzz.geinzwork.databinding.ActivityRecuperarContraBinding
import com.google.firebase.auth.FirebaseAuth

class recuperarContra : AppCompatActivity() {
    private lateinit var binding:ActivityRecuperarContraBinding
    private lateinit var firebaseAuth: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding=ActivityRecuperarContraBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        firebaseAuth=FirebaseAuth.getInstance()

        val correoPasado = intent.getStringExtra(Variables.correoUser).toString()

        if (correoPasado.isEmpty()) {
            binding.ingreseSuMail.setText("")
        } else {
            binding.ingreseSuMail.setText(correoPasado)
        }

        binding.btnRecuperar.setOnClickListener {
            val email = binding.ingreseSuMail.text.toString()
            if (email.isEmpty()) {
                Toast.makeText(this, "Por favor, ingresa tu correo electrónico", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Mostrar el diálogo de confirmación
            val builder = AlertDialog.Builder(this)
            builder.setTitle("Confirmar correo electrónico")
            builder.setMessage("¿Es correcto este correo electrónico?\nCorreo : $email")
            builder.setPositiveButton("Sí") { dialog, _ ->

                firebaseAuth.sendPasswordResetEmail(email)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            Toast.makeText(this, "Correo de recuperación enviado", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(this, "Error al enviar el correo de recuperación", Toast.LENGTH_SHORT).show()
                        }
                    }
                dialog.dismiss()
            }
            builder.setNegativeButton("No") { dialog, _ ->
                // Cerrar el diálogo si el usuario cancela
                dialog.dismiss()
            }
            builder.create().show()
        }

    }
}