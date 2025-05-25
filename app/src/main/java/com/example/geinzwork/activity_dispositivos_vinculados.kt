package com.example.geinzwork

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.geinzwork.adapterViewholder.adapter_dispo_vinculados
import com.example.geinzwork.constantesGeneral.constantes_vinculados
import com.example.geinzwork.dataclass.dataclass_dispo_vinculados
import com.geinzz.geinzwork.R
import com.geinzz.geinzwork.adapterViewholder.adaptadorReview
import com.geinzz.geinzwork.constantesGeneral.constantes_cuenta_user
import com.geinzz.geinzwork.databinding.ActivityDispositivosVinculadosBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class activity_dispositivos_vinculados : AppCompatActivity() {
    private lateinit var binding: ActivityDispositivosVinculadosBinding
    private val lista= mutableListOf<dataclass_dispo_vinculados>()
    private lateinit var firebaseAuth: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDispositivosVinculadosBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        firebaseAuth=FirebaseAuth.getInstance()
        obtener_dispositivos_vinculados()
    }

    private fun obtener_dispositivos_vinculados() {
        constantes_vinculados.encontrarUser(firebaseAuth.uid.toString()) { tipo, coleccion ->
            when (tipo) {
                "trabajador" -> {
                    val db = FirebaseFirestore.getInstance()
                        .collection("Trabajadores_Usuarios_Drivers")
                        .document("trabajadores").collection("trabajadores")
                        .document(firebaseAuth.uid.toString()).collection("vinculados")

                    db.get().addOnSuccessListener { res ->
                        lista.clear() // Limpia la lista antes de llenarla
                        for (datos in res) {
                            val data = datos.data
                            val dispositivo = data?.get("dispositivo") as? String ?: ""
                            val fecha_registro = data?.get("fecha_registro") as? String ?: ""
                            val hora_registro = data?.get("hora_registro") as? String ?: ""
                            val datos = dataclass_dispo_vinculados(
                                dispositivo,
                                hora_registro,
                                fecha_registro
                            )
                            lista.add(datos)
                        }

                        // Solo inicializa RecyclerView si la lista no está vacía
                        if (lista.isNotEmpty()) {
                            inicializarRecicle()
                        }
                    }
                }

                "usuario" -> {
                    val db = FirebaseFirestore.getInstance()
                        .collection("Trabajadores_Usuarios_Drivers")
                        .document("usuarios").collection("usuarios")
                        .document(firebaseAuth.uid.toString()).collection("vinculados")

                    db.get().addOnSuccessListener { res ->
                        lista.clear()
                        for (datos in res) {
                            val data = datos.data
                            val dispositivo = data?.get("dispositivo") as? String ?: ""
                            val fecha_registro = data?.get("fecha_registro") as? String ?: ""
                            val hora_registro = data?.get("hora_registro") as? String ?: ""
                            val datos = dataclass_dispo_vinculados(
                                dispositivo,
                                hora_registro,
                                fecha_registro
                            )
                            lista.add(datos)
                        }

                        if (lista.isNotEmpty()) {
                            inicializarRecicle()
                        }
                    }
                }

                else -> Log.d("RESULT", "No se encontró el usuario")
            }
        }
    }


    private fun inicializarRecicle(){
        val recicle = binding.recicleDispositivos
        recicle.layoutManager = LinearLayoutManager(this)
        recicle.adapter = adapter_dispo_vinculados(lista)
    }

}