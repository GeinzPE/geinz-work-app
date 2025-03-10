package com.example.geinzwork.publicaciones_trabajadores

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.geinzwork.adapterViewholder.adapter_trabajos_realizados_trabajador
import com.example.geinzwork.constantesGeneral.constatnes_carga_imagenes_general
import com.example.geinzwork.dataclass.dataclass_adapter_promociones
import com.geinzz.geinzwork.R
import com.geinzz.geinzwork.constantesGeneral.constantestextos_general
import com.geinzz.geinzwork.databinding.ActivityMostrarTodosTrabajosBinding
import com.geinzz.geinzwork.databinding.BottomSheetMostarTrabajosRecientesBinding
import com.geinzz.geinzwork.fragmentos.info
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.firestore.FirebaseFirestore

class mostrarTodosTrabajos : AppCompatActivity() {
    private val listaMas_promo = mutableListOf<dataclass_adapter_promociones>()
    private lateinit var dialog: BottomSheetDialog

    private lateinit var binding: ActivityMostrarTodosTrabajosBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMostrarTodosTrabajosBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        val idTrabajador = intent.getStringExtra("idTrabajador").toString()
        obtenerTodosTRabajos(idTrabajador)
    }

    private fun obtenerTodosTRabajos(idTrabajador: String) {
        val db = FirebaseFirestore.getInstance()
            .collection("Trabajadores_Usuarios_Drivers").document("trabajadores")
            .collection("trabajadores").document(idTrabajador).collection("publicaciones_trabajos")

        db.get().addOnSuccessListener { res ->
            listaMas_promo.clear() // Limpiar la lista para evitar duplicados

            for (datos in res) {
                val data = datos.data
                val img_url = data?.get("img_url") as? String ?: ""
                val titulo = data?.get("titulo") as? String ?: ""
                val contenido = data?.get("contenido") as? String ?: ""
                val id = data?.get("id") as? String ?: ""

                // Agregar a la lista
                val dataClass = dataclass_adapter_promociones(img_url, titulo, contenido, id)
                listaMas_promo.add(dataClass)
            }

            if (listaMas_promo.isNotEmpty()) {
                listaMas_promo.shuffle() // Mezclar aleatoriamente los elementos
                inicializarTrabajosRealizadosVertical(idTrabajador, listaMas_promo)
            } else {
                Log.d("error obtenerDAtos", "No hay datos para mostrar")
            }
        }.addOnFailureListener { e ->
            println("error al encontrar $e")
        }
    }


    private fun showBottomShetDialogAnuncios(
        idTrabajador: String,
        item: dataclass_adapter_promociones,

        ) {
        val bindingMostrar =
            BottomSheetMostarTrabajosRecientesBinding.inflate(LayoutInflater.from(this))
        dialog.setContentView(bindingMostrar.root)
        val cerrar = bindingMostrar.cerrar
        cerrar.setOnClickListener {
            dialog.dismiss()
        }
        constatnes_carga_imagenes_general.changer_img(
            bindingMostrar.progressCargaImagen,
            this, item.img.toString(), null, bindingMostrar.imgTrabajo, "portada", null
        ) {}

        bindingMostrar.linealMostrarTrabajos.isVisible = false
        bindingMostrar.textoTrabajosRealzados.text = item.texto_promo
        bindingMostrar.tituloTrabajosRealizados.text = item.titulo_promo
        constantestextos_general.extender_acortar_texto(
            bindingMostrar.textoTrabajosRealzados,
            bindingMostrar.tvReadMore
        )
    }

    private fun inicializarTrabajosRealizadosVertical(
        idTrabajador: String,
        listaMas_promo: MutableList<dataclass_adapter_promociones>
    ) {
        val recicle = binding.trabajosRealizados
        recicle.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        recicle.adapter = adapter_trabajos_realizados_trabajador(true,
            this.listaMas_promo,
        ) { item ->
            dialog = BottomSheetDialog(this)
            showBottomShetDialogAnuncios(idTrabajador, item)
            dialog.show()
        }
    }
}