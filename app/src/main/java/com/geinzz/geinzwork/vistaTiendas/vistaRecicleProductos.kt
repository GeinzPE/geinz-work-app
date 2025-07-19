package com.geinzz.geinzwork.vistaTiendas

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.widget.ImageButton
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.geinzz.geinzwork.R
import com.geinzz.geinzwork.databinding.ActivityVistaRecicleProductosBinding
import com.geinzz.geinzwork.model.dataclassArticulos
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.firestore.FirebaseFirestore


class vistaRecicleProductos : AppCompatActivity() {
    private lateinit var binding: ActivityVistaRecicleProductosBinding
    private lateinit var showBottomSheetButton: ImageButton
    private lateinit var dialog: BottomSheetDialog
    val listaArticulos = mutableListOf<dataclassArticulos>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityVistaRecicleProductosBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        val idTienda = intent.getStringExtra("idTienda").toString()

        showBottomSheetButton = binding.filtrado
        dialog = BottomSheetDialog(this)
        showBottomSheetButton.setOnClickListener {
            constantesVistaTiendas.filtradoBottom_shett(
                binding.RecicleFiltrado,
                dialog,
                this,
                idTienda, listaArticulos
            )
            dialog.show()
        }
        mostrarProductosGenerales(idTienda, listaArticulos, binding.RecicleFiltrado, this)

    }


    private fun mostrarProductosGenerales(
        idTienda: String,
        listaArticulos: MutableList<dataclassArticulos>,
        RecicleFiltrado: RecyclerView,
        context: Context,
    ) {
        val db2 = FirebaseFirestore.getInstance().collection("Tiendas").document(idTienda)
            .collection("articulos")
        db2.get().addOnSuccessListener { res ->
            for (datos in res) {
                val data = datos.data
                val descripcion = data?.get("descripcion") as? String ?: ""
                val imgArticulo = data?.get("imgArticulo") as? String ?: ""
                val nombreArticulo = data?.get("nombreArticulo") as? String ?: ""
                val precio = data?.get("precio") as? String ?: ""
                val fecha = data?.get("descripcion") as? String ?: ""
                val id = data?.get("id") as? String ?: ""
                val categoria = data?.get("categoriaProducto") as? String ?: ""
                val articulo = dataclassArticulos(
                    nombreArticulo,
                    imgArticulo,
                    precio,
                    descripcion,
                    fecha,
                    id,
                    categoria
                )
                listaArticulos.add(articulo)
                constantesVistaTiendas.inicializarRecicleProductos(
                    true,
                    listaArticulos,
                    RecicleFiltrado,
                    context,
                    idTienda, LinearLayoutManager.VERTICAL
                )
            }
        }.addOnFailureListener { e ->
            Log.d("error_obtener", "error al obtener los dato $e")
        }

    }


}