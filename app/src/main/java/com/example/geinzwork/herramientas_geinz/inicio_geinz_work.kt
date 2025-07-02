package com.example.geinzwork.herramientas_geinz

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.geinzwork.herramientas_geinz.constantes.FirebaseSecundario
import com.geinzz.geinzwork.R
import com.geinzz.geinzwork.databinding.ActivityInicioGeinzWorkBinding



class inicio_geinz_work : AppCompatActivity() {
    private lateinit var binding: ActivityInicioGeinzWorkBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityInicioGeinzWorkBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        FirebaseSecundario.inicializar(this)
        obtener_modelo_celulares_geinz_work()
    }

    private fun obtener_modelo_celulares_geinz_work() {
        // 🔹 Obtener la instancia de Firestore desde FirebaseSecundario
        val db = FirebaseSecundario.getFirestore()

        // 🔹 Acceder al documento específico
        db.collection("componentesMaestros")
            .document("PANTALLA_OLED_SAMSUNG_A52_GEN1")
            .get()
            .addOnSuccessListener { documento ->
                if (documento.exists()) {
                    val data = documento.data
                    Log.d("GeinzWork_obtenos_datos", "Datos: $data")
                    Toast.makeText(this, "Datos: $data", Toast.LENGTH_SHORT).show()


                } else {
                    Log.w("GeinzWork", "El documento no existe.")
                }
            }
            .addOnFailureListener { error ->
                Log.e("GeinzWork", "Error al obtener datos: ", error)
            }
    }

}