package com.geinzz.geinzwork

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.geinzwork.constantesGeneral.Variables
import com.example.geinzwork.constantesGeneral.constantes_vinculados
import com.geinzz.geinzwork.constantesGeneral.constantesPublicidad
import com.geinzz.geinzwork.databinding.ActivityLoginBinding
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

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
            constantesPublicidad.CreacionCuentaBottom_shett(this, dialog)
            dialog.show()
        }

        binding.contraseAOlv.setOnClickListener {
            val vista = Intent(this, recuperarContra::class.java).apply {
                putExtra(Variables.correoUser, binding.ingreseSuMail.text.toString())
            }
            startActivity(vista)
        }
    }

    private fun verificaruser() {
        val correo = binding.ingreseSuMail.text.toString().trim()
        val contraseña = binding.txtpassword.text.toString().trim()

        if (correo.isEmpty() || contraseña.isEmpty()) {
            Toast.makeText(this, "Rellene los campos", Toast.LENGTH_SHORT).show()
            return
        }

        val db = FirebaseFirestore.getInstance().collection("Trabajadores_Usuarios_Drivers")

        // Buscar en trabajadores
        db.document("trabajadores")
            .collection("trabajadores")
            .whereEqualTo("correo", correo)
            .get()
            .addOnSuccessListener { trabajadoresDocs ->
                if (!trabajadoresDocs.isEmpty) {
                    val doc = trabajadoresDocs.documents[0]
                    val uid = doc.getString("id") ?: doc.id
                    verificarDispositivosYLogin(uid, correo, contraseña, "trabajadores")
                } else {
                    // Buscar en usuarios si no está en trabajadores
                    db.document("usuarios")
                        .collection("usuarios")
                        .whereEqualTo("correo", correo)
                        .get()
                        .addOnSuccessListener { usuariosDocs ->
                            if (!usuariosDocs.isEmpty) {
                                val doc = usuariosDocs.documents[0]
                                val uid = doc.getString("id") ?: doc.id
                                verificarDispositivosYLogin(uid, correo, contraseña, "usuarios")
                            } else {
                                Toast.makeText(this, "Correo no registrado", Toast.LENGTH_SHORT)
                                    .show()
                            }
                        }
                }
            }
    }

    private fun verificarDispositivosYLogin(
        uid: String,
        correo: String,
        contraseña: String,
        tipo: String
    ) {
        val db = FirebaseFirestore.getInstance()

        db.collection("Trabajadores_Usuarios_Drivers")
            .document(tipo)
            .collection(tipo)
            .document(uid)
            .collection("vinculados")
            .get()
            .addOnSuccessListener { vinculados ->
                if (vinculados.size() >= 4) {
                    Toast.makeText(
                        this,
                        "Tiene 4 dispositivos vinculados con esta cuenta",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
               
                    val dialog = AlertDialog.Builder(this)
                        .setTitle("Iniciando sesión")
                        .setMessage("Espere un momento...")
                        .setCancelable(false)
                        .create()

                    dialog.show()
                    val startTime = System.currentTimeMillis()

                    firebaseAuth.signInWithEmailAndPassword(correo, contraseña)
                        .addOnSuccessListener { resultado ->
                            val endTime = System.currentTimeMillis()
                            val duration = endTime - startTime

                            Handler(Looper.getMainLooper()).postDelayed({
                                dialog.dismiss()
                            }, duration)

                            val user = resultado.user
                            constantes_vinculados.agregar_vinculado(user!!.uid, this)
                        }
                        .addOnFailureListener { e ->
                            dialog.dismiss()
                            Toast.makeText(
                                this,
                                "Error al iniciar sesión: ${e.message}",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
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