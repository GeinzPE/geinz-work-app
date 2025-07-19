package com.geinzz.geinzwork

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.geinzz.geinzwork.databinding.ActivityCuentaUserBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class CuentaUser : AppCompatActivity() {
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var binding: ActivityCuentaUserBinding
    private lateinit var progressDialog: ProgressDialog


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCuentaUserBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }


        firebaseAuth = FirebaseAuth.getInstance()

        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Espere un momento")
        progressDialog.setCanceledOnTouchOutside(false)

        binding.registrarmeBtn.setOnClickListener {
            validarInfo()
        }

    }


    private var nombre = ""
    private var apellido = ""
    private var fecha = ""
    private var correo = ""
    private var contraseña = ""
    private var contraseñaRep = ""

    private fun validarInfo() {
        nombre = binding.nombre.text.toString().trim()
        apellido = binding.apellido.text.toString().trim()
        fecha = binding.Fechanac.text.toString().trim()
        correo = binding.CorreoElectronico.text.toString().trim()
        contraseña = binding.contraseA.text.toString().trim()
        contraseñaRep = binding.RepetirContra.text.toString().trim()

        if (!Patterns.EMAIL_ADDRESS.matcher(correo).matches()) {
            binding.CorreoElectronico.error = "EMAIL INVALIDO"
            binding.CorreoElectronico.requestFocus()
        } else if (nombre.isEmpty()) {
            binding.nombre.error = "ingrese un nombre"
        } else if (apellido.isEmpty()) {
            binding.apellido.error = "ingrese un apellido"
        } else if (fecha.isEmpty()) {
            binding.Fechanac.error = "ingrese una fecha "
        } else if (correo.isEmpty()) {
            binding.CorreoElectronico.error = "ingrese un correo electronico"
        } else if (contraseña.isEmpty()) {
            binding.contraseA.error = "ingrese una contraseña"
        } else if (contraseñaRep.isEmpty()) {
            binding.RepetirContra.error = "ingrese la misma contraseña"
        } else if (contraseña != contraseñaRep) {
            binding.RepetirContra.error = "las contraseñas no coinciden"
        } else {
            varificarUSER()
        }

    }

    private fun varificarUSER() {
        progressDialog.setMessage("Creando tu cuenta espere un momento")
        progressDialog.show()

        firebaseAuth.createUserWithEmailAndPassword(correo, contraseña)
            .addOnSuccessListener {
                leerinforamcion()
            }
            .addOnFailureListener { e ->
                progressDialog.dismiss()
                Toast.makeText(this, "Error al crear el usuario ${e}", Toast.LENGTH_SHORT).show()

            }
    }

    private fun leerinforamcion() {
        progressDialog.setMessage("creando cuenta")

        var correo = firebaseAuth.currentUser!!.email
        var userid = firebaseAuth.uid
        var nombre = binding.nombre.text.toString().trim()
        var apellido = binding.apellido.text.toString().trim()
        var fecha = binding.Fechanac.text.toString().trim()

        val hashMap = HashMap<String, Any>()
        hashMap["nombre"] = "${nombre}"
        hashMap["apellido"] = "${apellido}"
        hashMap["fechaNac"] = "${fecha}"
        hashMap["email"] = "${correo}"
        hashMap["uid"] = "${userid}"

        val referenciaDB = FirebaseDatabase.getInstance().getReference("usuarios")
        referenciaDB.child(userid!!)
            .setValue(hashMap)

            .addOnSuccessListener {
                progressDialog.dismiss()
                Toast.makeText(this, "usuario registrado correctamente", Toast.LENGTH_SHORT).show()
                var vista : Intent = Intent(this,GraciasRegistro::class.java)
                vista.putExtra("nombreUsuario",nombre)
                startActivity(vista)
                limpiarcampos()
                finishAffinity()

            }
            .addOnFailureListener {
                progressDialog.dismiss()
                Toast.makeText(this, "error al registra el usaurio ", Toast.LENGTH_SHORT).show()
            }


    }

    private fun limpiarcampos(){
        binding.nombre.setText("")
        binding.apellido.setText("")
        binding.Fechanac.setText("")
        binding.CorreoElectronico.setText("")
        binding.contraseA.setText("")
        binding.RepetirContra.setText("")
    }
}