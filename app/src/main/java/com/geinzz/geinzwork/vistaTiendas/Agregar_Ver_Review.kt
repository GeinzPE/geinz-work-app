package com.geinzz.geinzwork.vistaTiendas



import android.os.Build
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.geinzz.geinzwork.R
import com.geinzz.geinzwork.databinding.ActivityAgregarVerReviewBinding
import com.google.firebase.auth.FirebaseAuth


class Agregar_Ver_Review : AppCompatActivity() {
    private lateinit var binding: ActivityAgregarVerReviewBinding
    private lateinit var firebaseAuth: FirebaseAuth
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAgregarVerReviewBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        firebaseAuth = FirebaseAuth.getInstance()
        val idUser = firebaseAuth.uid.toString()
        val idTienda = intent.getStringExtra("idTienda").toString()
        val tipo = "CuentaTienda"

//       constantesReviewComplet.obtenerReview(idTienda, "reseñasTiendas",binding.RecicleReview,this)
//        constantesCarrito.obtnerfechaHora(binding.hora,binding.fecha)
//        binding.piblicarReview.setOnClickListener {
//            constantesReviewComplet.verificarSiExisteReview(
//                tipo,
//                "reseñasTiendas",
//                "Tiendas",
//                idUser,
//                idTienda,
//                this,
//                binding.contenidoReseview,
//                binding.cantidadStarts, binding.hora, binding.fecha, ""
//            )
//        }

    }

}