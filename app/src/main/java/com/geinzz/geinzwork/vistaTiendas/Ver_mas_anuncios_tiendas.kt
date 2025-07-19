package com.geinzz.geinzwork.vistaTiendas

import android.os.Build
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.geinzz.geinzwork.R
import com.geinzz.geinzwork.databinding.ActivityVerMasAnunciosTiendasBinding
import com.geinzz.geinzwork.model.dataClassAnuncios
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class Ver_mas_anuncios_tiendas : AppCompatActivity() {
    private lateinit var binding: ActivityVerMasAnunciosTiendasBinding
    private lateinit var firebaseAuth: FirebaseAuth
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityVerMasAnunciosTiendasBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        firebaseAuth = FirebaseAuth.getInstance()
        val idTiendaProp = intent.getStringExtra("idTienda")
        buscarAnunciosDeTienda(idTiendaProp.toString())

    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun buscarAnunciosDeTienda(idTiendaPropMandado: String) {
        val lista = mutableListOf<dataClassAnuncios>()
        val db = FirebaseFirestore.getInstance().collection("noticias")
        db.get()
            .addOnSuccessListener { res ->
                for (document in res) {
                    val id = document.getString("id")
                    val titulo = document.getString("titulo")
                    val contenido = document.getString("Contenido")
                    val fecha = document.getString("fecha")
                    val imagen = document.getString("imagenUrl")
                    val estado = document.getString("estado")
                    val numeroTelf = document.getString("numero")
                    val mensaje = document.getString("whatsappmsj")
                    val fechaVencimiento = document.getString("vencimineto")
                    val estadoVencimineto = document.getString("estado")
                    val categoria = document.getString("Categoria")
                    val TipoPromo = document.getString("tipoPromo")

                    val idTiendaProp = document.getString("idTiendaProp")
                    val localidad=document.getString("localidad")
                    val propietario = document.getString("propietario")
//                    val anuncio = dataClassAnuncios(propietario,
//                        contenido,
//                        fecha,
//                        titulo,
//                        imagen,
//                        estado,
//                        id,
//                        numeroTelf,
//                        mensaje,
//                        fechaVencimiento,
//                        estadoVencimineto,
//                        categoria,
//                        TipoPromo,
//
//                        idTiendaProp,localidad
//                    )

                    if (idTiendaProp == idTiendaPropMandado) {
                        println("verificamos el id$id y $idTiendaPropMandado")
//                        lista.add(anuncio)
                    }
                }
//                constantesNoticias.inicializarReciles(lista,binding.RecicleViewNoticiasTiendas,this)
            }
    }



}