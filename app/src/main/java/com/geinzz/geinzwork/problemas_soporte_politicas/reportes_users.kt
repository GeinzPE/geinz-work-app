package com.geinzz.geinzwork.problemas_soporte_politicas

import android.content.Context
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.geinzz.geinzwork.utils.constantes.constantes.Variables
import com.geinzz.geinzwork.R
import com.geinzz.geinzwork.ui.adapters.adapterReportes
import com.geinzz.geinzwork.databinding.ActivityReportesUsersBinding
import com.geinzz.geinzwork.model.dataClassReportes
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class reportes_users : AppCompatActivity() {
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var binding:ActivityReportesUsersBinding
    private val lista = mutableListOf<dataClassReportes>()
    private lateinit var dialog: BottomSheetDialog
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding=ActivityReportesUsersBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        firebaseAuth = FirebaseAuth.getInstance()
        obtener_Reportes(firebaseAuth.uid.toString())
    }

    private fun obtener_Reportes(id: String) {
        val db = FirebaseFirestore.getInstance().collection(Variables.politicas_problemas_verificaciones)
            .document(Variables.problemas).collection(Variables.problemas)

        db.get().addOnSuccessListener { res ->
            for (datos in res) {
                val data = datos.data
                val idTrabajador = data.get(Variables.idTrabajador) as? String?:""
                if (idTrabajador == id) {
                    val tipoR = data.get(Variables.Tipo_reporte) as? String?:""
                    val contenido = data.get(Variables.problema) as? String?:""
                    val fecha = data.get(Variables.fecha_envio) as? String?:""
                    val hora = data.get(Variables.hora_envio) as? String?:""
                    val idReporte= data.get(Variables.idReporte) as? String?:""
                    val datos = dataClassReportes(tipoR, contenido, fecha, hora, idReporte)
                    lista.add(datos)
                }
            }
            if(lista.isEmpty()){
                Toast.makeText(this, "Usted no cuenta con reportes", Toast.LENGTH_SHORT).show()
                binding.linealSinReportes.isVisible=true
            }else{
                binding.reportes.isVisible=true
                binding.linealappLayout.isVisible=true
                binding.TotalDeReportes.text="Total de reportes encontrados :${lista.size}"
                inicializarReclicle(lista,binding.reportes,this)
            }

        }
    }

    private fun inicializarReclicle(lista:MutableList<dataClassReportes>,recicle:RecyclerView,context: Context){
        val recicles=recicle
        recicles.layoutManager= LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        recicles.adapter= adapterReportes(lista)
    }


}