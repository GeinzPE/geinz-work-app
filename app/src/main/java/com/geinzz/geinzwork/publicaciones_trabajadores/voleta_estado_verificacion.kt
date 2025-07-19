package com.geinzz.geinzwork.publicaciones_trabajadores

import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import com.geinzz.geinzwork.R
import com.geinzz.geinzwork.databinding.ActivityVoletaEstadoVerificacionBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

class voleta_estado_verificacion : AppCompatActivity() {
    private lateinit var binding: ActivityVoletaEstadoVerificacionBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private var YAPEO: Uri? = null


    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityVoletaEstadoVerificacionBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        firebaseAuth = FirebaseAuth.getInstance()
        setearCampos(firebaseAuth.uid.toString())
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun setearCampos(id: String) {
        val db = FirebaseFirestore.getInstance().collection("solicitudes_servicios")
            .document("verificaciones").collection("activos").document(id)

        val dbHistoria = FirebaseFirestore.getInstance().collection("Trabajadores_Usuarios_Drivers")
            .document("trabajadores").collection("trabajadores").document(id)

        db.get().addOnSuccessListener { res ->
            if (res.exists()) {
                val data = res.data
                val estado = data?.get("estado") as? Boolean ?: false
                val fechaMap = data?.get("fechas") as? Map<String, Any>
                val fecha_activacion = fechaMap?.get("fecha_activacion") as? String ?: ""
                val fecha_vencimineto = fechaMap?.get("fecha_vencimineto") as? String ?: ""
                val hora_activacion = fechaMap?.get("hora_activacion") as? String ?: ""
                val id_solicitud = data?.get("id_solicitud") as? String
                val plan = data?.get("plan") as? String ?: ""
                binding.fechaPago.text = fecha_activacion
                binding.horaPago.text = hora_activacion
                binding.vence.text = "$fecha_vencimineto"
                binding.codComprovante.text = id_solicitud
                binding.plan.text = plan

                if (estado == true) {
                    binding.estado.text = "Verificado"
                } else {
                    binding.estado.text = "No verificado"
                }

                dbHistoria.get().addOnSuccessListener { res ->
                    if (res.exists()) {
                        val data = res.data
                        val nombre = data?.get("nombre") as? String

                        val apellido = data?.get("apellido") as? String
                        val numero = data?.get("numero") as? String


                        binding.nombre.text = nombre
                        binding.numero.text = numero
                        binding.apellido.text = apellido

                    }
                }
            }

        }


    }
}