package com.example.geinzwork

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.geinzwork.adapterViewholder.adapter_reporte_denuncia_tb
import com.example.geinzwork.dataclass.dataclass_reporte_denuncia_tb
import com.geinzz.geinzwork.R
import com.geinzz.geinzwork.databinding.ActivityVistaDenunciaReporteBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class vista_denuncia_reporte : AppCompatActivity() {
    private lateinit var binding: ActivityVistaDenunciaReporteBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private val lista_reporte = mutableListOf<dataclass_reporte_denuncia_tb>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityVistaDenunciaReporteBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        firebaseAuth = FirebaseAuth.getInstance()
        obtner_Denuncias_trabajadores()
        binding.trabajadoresReportes.setOnClickListener {
            binding.filtradoReviewTrabajadores.isVisible = false
            obtner_Denuncias_trabajadores()
            binding.cargandoContenido.isVisible = true
            binding.linealChipsFiltradoestados.isVisible = true
            binding.scroolReporesMismoTrabajador.isVisible = false
            binding.chipGroupestados.clearCheck()
            binding.grupoReporesUser.clearCheck()


        }
        binding.reviewReportes.setOnClickListener {
            binding.filtradoReviewTrabajadores.isVisible = false
            obtener_denuncias_Review()
            binding.grupoReporesUser.clearCheck()
            binding.cargandoContenido.isVisible = true
            binding.linealChipsFiltradoestados.isVisible = true
            binding.scroolReporesMismoTrabajador.isVisible = false
            binding.chipGroupestados.clearCheck()

        }
        binding.tusReportes.setOnClickListener {
            binding.chipGroupestados.clearCheck()
            binding.grupoReporesUser.clearCheck()
            binding.scroolReporesMismoTrabajador.isVisible = true
            binding.filtradoReviewTrabajadores.isVisible = false
            binding.cargandoContenido.isVisible = true
            binding.linealChipsFiltradoestados.isVisible = false
        }
    }

    private fun obtner_Denuncias_trabajadores() {
        val tiempoInicio = System.currentTimeMillis()
        val db = FirebaseFirestore.getInstance().collection("politicas_problemas_verificaciones")
            .document("problemas").collection("problemas")
        db.get().addOnSuccessListener { res ->
            lista_reporte.clear()
            for (datos in res) {
                val data = datos.data
                val idTrabajador = data?.get("idTrabajador") as? String ?: ""
                val idUsuario = data?.get("idUsuario") as? String ?: ""
                val problema = data?.get("problema") as? String ?: ""
                val Tipo_reporte = data?.get("Tipo_reporte") as? String ?: ""
                val idReporte = data?.get("idReporte") as? String ?: ""
                val estado = data?.get("estado") as? Number ?: 0
                if (idUsuario == firebaseAuth.uid.toString()) {
                    val dataclass_reporte = dataclass_reporte_denuncia_tb(
                        idTrabajador, idUsuario, idReporte, Tipo_reporte, problema, estado
                    )
                    lista_reporte.add(dataclass_reporte)
                }
            }
            if (lista_reporte.isNotEmpty()) {
                inicializar_listaReporte()
            } else {
                binding.noEncontrado.isVisible = true
                binding.filtradoReviewTrabajadores.isVisible = false
            }
            val tiempoFin = System.currentTimeMillis()
            val duracion = tiempoFin - tiempoInicio
            Handler(Looper.getMainLooper()).postDelayed({
                binding.cargandoContenido.isVisible = false
                binding.filtradoReviewTrabajadores.isVisible = true
            }, duracion)
            Log.d("FirestoreTiempo", "Tiempo total: $duracion ms (${duracion / 1000.0} segundos)")
        }.addOnFailureListener { e ->
            Log.d("error_econtrado", "Error al encontrar la referencia")
        }
    }

    private fun inicializar_listaReporte() {
        val adapter = adapter_reporte_denuncia_tb(lista_reporte)
        binding.filtradoReviewTrabajadores.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        binding.filtradoReviewTrabajadores.adapter = adapter
    }

    private fun boottoSheet_denuncia_trabajadores() {

    }

    private fun obtener_denuncias_Review() {
        val tiempoInicio = System.currentTimeMillis()
        val db = FirebaseFirestore.getInstance().collection("politicas_problemas_verificaciones")
            .document("denuncia_review").collection("denuncia_review")

        db.get().addOnSuccessListener { res ->
            lista_reporte.clear()

            for (datos in res) {
                val data = datos.data
                val id_registrado = data["id_registrado"] as? String ?: ""
                val id_usuario_review = data["id_usuario_review"] as? String ?: ""
                val id_trabajador = data["id_trabajador"] as? String ?: ""
                val id_review = data["id_review"] as? String ?: ""
                val incidencia = data["incidencia"] as? String ?: ""
                val descripcion = data["descripcion"] as? String ?: ""
                val estado = data["estado"] as? Number ?: 0

                if (id_registrado == firebaseAuth.uid.toString()) {
                    val dataclass_reporte = dataclass_reporte_denuncia_tb(
                        id_usuario_review, id_trabajador, id_review, incidencia, descripcion, estado
                    )
                    lista_reporte.add(dataclass_reporte)
                }
            }

            if (lista_reporte.isNotEmpty()) {
                inicializar_listaReporte()
            } else {
                binding.noEncontrado.isVisible = true
                binding.filtradoReviewTrabajadores.isVisible = false
            }

            val tiempoFin = System.currentTimeMillis()
            val duracion = tiempoFin - tiempoInicio
            Handler(Looper.getMainLooper()).postDelayed({
                binding.cargandoContenido.isVisible = false
                binding.filtradoReviewTrabajadores.isVisible = true
            }, duracion)
            Log.d("FirestoreTiempo", "Tiempo total: $duracion ms (${duracion / 1000.0} segundos)")
        }.addOnFailureListener { e ->
            Log.e("FirestoreError", "Error al obtener denuncias: ${e.message}")
        }
    }


}