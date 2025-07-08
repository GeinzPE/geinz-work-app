package com.geinzz.geinzwork

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
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
import com.google.firebase.firestore.SetOptions
import com.google.firebase.messaging.FirebaseMessaging

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
        val datos = intent.getStringExtra("dato").toString()


        binding.BtnIngresar.setOnClickListener {
            when (datos) {
                "panel" -> {
                    Toast.makeText(this, "precionamoes el panel", Toast.LENGTH_SHORT).show()
                    verificaruser("regreso") { registrado, texto -> }
                }

                "qr" -> {
                    Toast.makeText(this, "precionamoes el panel", Toast.LENGTH_SHORT).show()
                    verificaruser("regreso") { registrado, texto -> }
                }

                "dispositivos" -> {
                    Toast.makeText(this, "precionamoes el panel", Toast.LENGTH_SHORT).show()
                    verificaruser("regreso") { registrado, texto -> }
                }

                "perfil" -> {
                    Toast.makeText(this, "precionamoes el panel", Toast.LENGTH_SHORT).show()
                    verificaruser("regreso") { registrado, texto -> }
                }


                else -> {
                    verificaruser("directo") { registrado, texto ->
                    }
                }
            }
        }

        verificarSeccion()



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

    private fun verificaruser(
        tipo: String,
        onResult: (Boolean, String?) -> Unit
    ) { // Agregado el callback aquí
        val correo = binding.ingreseSuMail.text.toString().trim()
        val contraseña = binding.txtpassword.text.toString().trim()

        if (correo.isEmpty() || contraseña.isEmpty()) {
            Toast.makeText(this, "Rellene los campos", Toast.LENGTH_SHORT).show()
            onResult(false, "Rellene los campos") // Llama al callback con error
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
                    verificarDispositivosYLogin(
                        tipo,
                        uid,
                        correo,
                        contraseña,
                        "trabajadores",
                        onResult
                    ) // Pasa el callback
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
                                verificarDispositivosYLogin(
                                    tipo,
                                    uid,
                                    correo,
                                    contraseña,
                                    "usuarios",
                                    onResult
                                ) // Pasa el callback
                            } else {
                                // Correo no registrado en ninguna de las colecciones
                                Toast.makeText(this, "Correo no registrado", Toast.LENGTH_SHORT)
                                    .show()
                                onResult(
                                    false,
                                    "Correo no registrado"
                                ) // Llama al callback con error
                            }
                        }
                        .addOnFailureListener { e ->
                            // Error al buscar en usuarios
                            Toast.makeText(
                                this,
                                "Error al buscar usuario: ${e.message}",
                                Toast.LENGTH_SHORT
                            ).show()
                            onResult(
                                false,
                                "Error al buscar usuario: ${e.message}"
                            ) // Llama al callback con error
                        }
                }
            }
            .addOnFailureListener { e ->
                // Error al buscar en trabajadores
                Toast.makeText(this, "Error al buscar trabajador: ${e.message}", Toast.LENGTH_SHORT)
                    .show()
                onResult(
                    false,
                    "Error al buscar trabajador: ${e.message}"
                ) // Llama al callback con error
            }
    }

    // Función para verificar dispositivos vinculados e intentar el login.
// Acepta un callback para reportar el resultado final de la operación.
    private fun verificarDispositivosYLogin(
        tipo_obtenido: String,
        uid: String,
        correo: String,
        contraseña: String,
        tipo: String,
        onResult: (Boolean, String?) -> Unit // Agregado el callback aquí
    ) {
        val db = FirebaseFirestore.getInstance()

        db.collection("Trabajadores_Usuarios_Drivers")
            .document(tipo)
            .collection(tipo)
            .document(uid)
            .collection("vinculados")
            .get()
            .addOnSuccessListener { vinculados ->
                if (vinculados.size() >= 4) { // Límite de 4 dispositivos vinculados
                    Toast.makeText(
                        this,
                        "Tiene 4 dispositivos vinculados con esta cuenta",
                        Toast.LENGTH_SHORT
                    ).show()
                    onResult(
                        false,
                        "Límite de dispositivos excedido"
                    ) // Llama al callback con error
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
                            if (user != null) { // Asegúrate de que el usuario no sea nulo
                                constantes_vinculados.agregar_vinculado(
                                    user.uid,
                                    this,
                                    tipo_obtenido
                                )
                                agregar_token_listaFCM(user.uid)
                                // Si agregar_vinculado es asíncrono y su éxito es el final,
                                // podrías necesitar otro callback aquí.
                                // Por ahora, asumimos que si llega aquí, el login es un éxito para el callback.
                                onResult(true, null) // Llama al callback con éxito
                            } else {
                                dialog.dismiss()
                                Toast.makeText(
                                    this,
                                    "Error: Usuario nulo después del inicio de sesión.",
                                    Toast.LENGTH_SHORT
                                ).show()
                                onResult(
                                    false,
                                    "Usuario nulo después del inicio de sesión"
                                ) // Llama al callback con error
                            }
                        }
                        .addOnFailureListener { e ->
                            dialog.dismiss()
                            Toast.makeText(
                                this,
                                "Error al iniciar sesión: ${e.message}",
                                Toast.LENGTH_SHORT
                            ).show()
                            onResult(
                                false,
                                "Error al iniciar sesión: ${e.message}"
                            ) // Llama al callback con error
                        }
                }
            }
            .addOnFailureListener { e ->
                // Error al obtener la colección de vinculados
                Toast.makeText(
                    this,
                    "Error al verificar vinculados: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
                onResult(
                    false,
                    "Error al verificar vinculados: ${e.message}"
                ) // Llama al callback con error
            }
    }


    private fun verificarSeccion() {
        if (firebaseAuth.currentUser != null) {
            startActivity(Intent(this, MainActivity::class.java))
            finishAffinity()
        }
    }

    private fun agregar_token_listaFCM(id_registrado: String) {
        FirebaseMessaging.getInstance().token.addOnSuccessListener { token ->
            val deviceName = "${Build.MANUFACTURER}-${Build.MODEL}"
                .replace(" ", "_")
                .replace(".", "")
                .lowercase() // 🔧 Normaliza el nombre del dispositivo

            val db = FirebaseFirestore.getInstance()
                .collection("Trabajadores_Usuarios_Drivers")
                .document("tokens")
                .collection(id_registrado)
                .document("dispositivos")

            val tokenMap = mapOf(
                deviceName to token // ✅ Solo el nombre limpio
            )

            val finalMap = mapOf(
                "tokens" to tokenMap // ✅ Lo metemos dentro del campo "tokens"
            )

            db.set(finalMap, SetOptions.merge())
                .addOnSuccessListener {
                    Log.d("FCM", "Token guardado para: $deviceName")
                }
                .addOnFailureListener {
                    Log.e("FCM", "Error al guardar token: ${it.message}")
                }
        }.addOnFailureListener {
            Log.e("FCM", "Error al obtener token FCM: ${it.message}")
        }
    }



}