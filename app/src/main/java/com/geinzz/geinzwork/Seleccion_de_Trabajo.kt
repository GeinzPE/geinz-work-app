package com.geinzz.geinzwork

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.geinzz.geinzwork.utils.constantes.constantes.Variables
import com.geinzz.geinzwork.databinding.ActivitySeleccionDeTrabajoBinding
import com.google.firebase.firestore.FirebaseFirestore

class Seleccion_de_Trabajo : AppCompatActivity() {
    private lateinit var binding: ActivitySeleccionDeTrabajoBinding
    val db = FirebaseFirestore.getInstance()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySeleccionDeTrabajoBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        binding.enviar.setOnClickListener {
            enviarDatos()
        }
        obtenerCategorias(binding.complete)
    }

    fun obtenerCategorias(autoCompleteTextView: AutoCompleteTextView) {
        val collection = db.collection(Variables.categoriasDB).document(Variables.categoriasTrabajo)

        collection.get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val categorias = document.get(Variables.categoriasDB) as? ArrayList<String>
                    if (categorias != null) {
                        val adapter = ArrayAdapter<String>(
                            autoCompleteTextView.context,
                            android.R.layout.simple_dropdown_item_1line,
                            categorias
                        )
                        autoCompleteTextView.setAdapter(adapter)
                        autoCompleteTextView.setOnItemClickListener { parent, view, position, id ->
                            val seleccionado = parent.getItemAtPosition(position).toString()
                            binding.cat.text = seleccionado
                            obtenerSubcategoriras(seleccionado,binding.subcategoria)
                        }

                    }

                }
            }
    }
    fun obtenerSubcategoriras(categoriaSelecionada:String,autoCompleteTextView: AutoCompleteTextView){
        val collection=db.collection(Variables.subcategoriasTrabajos).document(categoriaSelecionada)
        collection.get().addOnSuccessListener { res->
            if(res.exists()){
                val subcategorias=res.get(Variables.subcategoriasDB) as? ArrayList<String>
                if(subcategorias!=null){
                    val adapter=ArrayAdapter<String>(
                        autoCompleteTextView.context,android.R.layout.simple_dropdown_item_1line,subcategorias
                    )
                    autoCompleteTextView.setAdapter(adapter)
                    autoCompleteTextView.setOnItemClickListener { parent, view, position, id ->
                        val seleccionado = parent.getItemAtPosition(position).toString()
                        binding.tipot.text = seleccionado
                    }

                }
            }
        }
    }

    private fun enviarDatos() {
        var categoria = binding.cat.text.toString().trim()
        var tipoTrabajo = binding.subcategoria.text.toString().trim()
        var nombre = intent.getStringExtra("nombreUSer").toString().trim()
        var apellidoUser = intent.getStringExtra("apellidoUser").toString().trim()
        var fechanaciminetoUSer = intent.getStringExtra("fechanaciminetoUSer").toString().trim()
        var amUSer = intent.getStringExtra("amUSer").toString().trim()
        var pmUser = intent.getStringExtra("pmUser").toString().trim()
        var nacionalidadUSer = intent.getStringExtra("nacionalidadUSer").toString().trim()
        var genero = intent.getStringExtra("genero")
        var localidad = intent.getStringExtra("localida")
        var countryCode = intent.getStringExtra("codigo").toString().trim()
        var NumeroCelular = intent.getStringExtra("numero")
        var edadActualUser = intent.getStringExtra("edadActual")

        var vista = Intent(this, caracteristicaTrabajo::class.java)
        vista.putExtra("Categoria", categoria)
        vista.putExtra("tipo", tipoTrabajo)
        vista.putExtra("nombre", nombre)
        vista.putExtra("apellidoUser", apellidoUser)
        vista.putExtra("fechanaciminetoUSer", fechanaciminetoUSer)
        vista.putExtra("amUSer", amUSer)
        vista.putExtra("pmUser", pmUser)
        vista.putExtra("nacionalidadUSer", nacionalidadUSer)
        vista.putExtra("genero", genero)
        vista.putExtra("localida", localidad)
        vista.putExtra("codigo", countryCode)
        vista.putExtra("numero", NumeroCelular)
        vista.putExtra("edadActual", edadActualUser)
        startActivity(vista)
        Log.d("timeFreP", pmUser)
    }


}