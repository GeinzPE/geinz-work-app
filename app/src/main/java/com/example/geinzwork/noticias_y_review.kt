package com.geinzz.geinzwork


import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
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
import com.geinzz.geinzwork.constantesGeneral.constantes
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
        confSwipe()
    }

    private fun confSwipe() {
        binding.swipe.setOnRefreshListener {
            binding.swipe.setColorSchemeResources(R.color.violeta)
            obtener_review()
            binding.loading.isVisible = true
            binding.swipe.isRefreshing = false
            binding.noEncontrado.isVisible = false
            binding.scrollReview.isVisible = false
            Handler(Looper.getMainLooper()).postDelayed({
                binding.loading.isVisible = false
            }, 2000)
        }
    }

    private fun obtener_review() {
        binding.swipe.isVisible = true
        binding.scrollReview.isVisible = false
        firebaseAuth = FirebaseAuth.getInstance()
        val listaReview = mutableListOf<daclassReview>()
        val db = FirebaseFirestore.getInstance().collection("Trabajadores_Usuarios_Drivers")
            .document("trabajadores").collection("trabajadores")
            .document(firebaseAuth.uid.toString()).collection("review")

        db.get().addOnSuccessListener { res ->
            if (res.isEmpty) {
                // No hay documentos en la colección review
                binding.noEncontrado.isVisible = true
                binding.texto.isVisible = false
                binding.recicelGuardados.isVisible = false
                binding.loading.isVisible = false
            } else {
                for (datos in res) {
                    val data = datos.data
                    val editado = data[Variables.editado] as? Boolean ?: false
                    val id = data[Variables.iduserReview] as? String ?: ""
                    val fecha = data[Variables.fecha] as? String ?: ""
                    val hora = data[Variables.hora] as? String ?: ""
                    val reseña = data[Variables.reseña] as? String ?: ""
                    val TipoTrabajo = data[Variables.TipoTrabajo] as? String ?: ""
                    val cantidad = data[Variables.cantidad] as? String ?: ""
                    val id_review = data["id_review"] as? String ?: ""

                    val review = daclassReview(
                        id,
                        cantidad,
                        reseña,
                        hora,
                        fecha,
                        TipoTrabajo,
                        editado,
                        id_review
                    )
                    listaReview.add(review)
                }

                // Mostrar las reseñas
                binding.scrollReview.isVisible = true
                binding.loading.isVisible = false
                binding.texto.isVisible = true
                binding.recicelGuardados.isVisible = true
                binding.noEncontrado.isVisible = false
                inicalizarRecicle(listaReview)
            }
        }.addOnFailureListener { e ->
            Log.d("idNull", "No se encontró review: $e")
            binding.scrollReview.isVisible = false
            binding.noEncontrado.isVisible = true
            binding.texto.isVisible = false
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