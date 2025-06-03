package com.geinzz.geinzwork


import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.geinzwork.constantesGeneral.Variables
import com.geinzz.geinzwork.adapterViewholder.adaptadorReview
import com.geinzz.geinzwork.databinding.ActivityNoticiasYreviewBinding
import com.geinzz.geinzwork.dataclass.daclassReview
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class noticias_y_review : AppCompatActivity() {
    private lateinit var binding: ActivityNoticiasYreviewBinding
    private lateinit var firebaseAuth: FirebaseAuth

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNoticiasYreviewBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        firebaseAuth = FirebaseAuth.getInstance()
        val title = intent.getStringExtra(Variables.title)
        when (title.toString()) {
            "Tus Reseñas" -> {
                obtener_review()
                binding.texto.text = "Reseñas de tus clientes"
            }

            else -> ""
        }
    }

    private fun obtener_review() {
        firebaseAuth = FirebaseAuth.getInstance()
        val listaReview = mutableListOf<daclassReview>()
        val db = FirebaseFirestore.getInstance().collection("Trabajadores_Usuarios_Drivers")
            .document("trabajadores").collection("trabajadores")
            .document(firebaseAuth.uid.toString()).collection("review")
        db.get().addOnSuccessListener { res ->
            for (datos in res) {
                val data = datos.data
                val editado = data?.get(Variables.editado) as? Boolean ?: false
                val id = data?.get(Variables.iduserReview) as? String ?: ""
                val fecha = data?.get(Variables.fecha) as? String ?: ""
                val hora = data?.get(Variables.hora) as? String ?: ""
                val reseña = data?.get(Variables.reseña) as? String ?: ""
                val TipoTrabajo = data?.get(Variables.TipoTrabajo) as? String ?: ""
                val cantidad = data?.get(Variables.cantidad) as? String ?: ""
                Log.d("el id de los campos son ",id)
                val review = daclassReview(
                    id,
                    cantidad,
                    reseña,
                    hora,
                    fecha,
                    TipoTrabajo,
                    editado
                )
                listaReview.add(review)
                if (listaReview.isNotEmpty()) {
                    // No hay reseñas disponibles
                    // Aquí podrías mostrar un mensaje indicando que no hay reseñas disponibles
                    binding.loading.isVisible = false
                    binding.texto.isVisible = true
                    binding.recicelGuardados.isVisible = true
                    binding.relativeNoEncontrado.isVisible = false
                    inicalizarRecicle(listaReview)
                } else {
                    // Mostrar las reseñas en el RecyclerView
                    binding.recicelGuardados.isVisible = false
                    binding.loading.isVisible = false
                    binding.relativeNoEncontrado.isVisible = true
                    binding.texto.isVisible = false
                }
            }

        }.addOnFailureListener { e ->
            Log.d("idNull", "No se encontro review $e")
            binding.texto.isVisible = false
            binding.relativeNoEncontrado.isVisible = true
            binding.recicelGuardados.isVisible = false
            binding.loading.isVisible = false
        }

    }


    private fun inicalizarRecicle(
        listaReview: MutableList<daclassReview>,
    ) {
        val recicle = binding.recicelGuardados
        recicle.layoutManager = LinearLayoutManager(this)
        recicle.adapter = adaptadorReview(listaReview)
    }



}