package com.geinzz.geinzwork

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.geinzwork.constantesGeneral.Variables
import com.example.geinzwork.constantesGeneral.constantes_vinculados
import com.geinzz.geinzwork.constantesGeneral.constantesPublicidad
import com.geinzz.geinzwork.databinding.ActivityLoginBinding
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.auth.FirebaseAuth

class Login : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var dialog: BottomSheetDialog
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        firebaseAuth = FirebaseAuth.getInstance()
        verificarSeccion()


        binding.BtnIngresar.setOnClickListener {
            verificaruser()
        }
        binding.registrate.setOnClickListener {
            dialog = BottomSheetDialog(this)
            constantesPublicidad.CreacionCuentaBottom_shett(this,dialog)
            dialog.show()
        }

        binding.contraseAOlv.setOnClickListener {
            val vista=Intent(this,recuperarContra::class.java).apply {
                putExtra(Variables.correoUser,binding.ingreseSuMail.text.toString())
            }
            startActivity(vista)
        }
    }

    private fun verificaruser() {
        val correo = binding.ingreseSuMail.text.toString()
        val contraseña = binding.txtpassword.text.toString()
        if (correo.isEmpty() || contraseña.isEmpty()) {
            Toast.makeText(this, "rellene los campos", Toast.LENGTH_SHORT).show()
        } else {
            firebaseAuth.signInWithEmailAndPassword(correo, contraseña)
                .addOnSuccessListener { resultado ->
                    val user = resultado.user
                    val userId = user?.uid  // Aquí tienes el ID del usuario
                    Toast.makeText(this, "Inicio de sesión exitoso. ID: $userId", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this, MainActivity::class.java))
                    constantes_vinculados.agregar_vinculado(userId.toString(),this)
                    finish()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(
                        this,
                        "Error al iniciar sesión: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
        }
    }


    private fun verificarSeccion() {
        if (firebaseAuth.currentUser != null) {
            startActivity(Intent(this, MainActivity::class.java))
            finishAffinity()
        }
    }
}