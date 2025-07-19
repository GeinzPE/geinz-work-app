package com.geinzz.geinzwork.ui.adapters

import android.content.Context
import android.os.Build
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.geinzz.geinzwork.utils.constantes.constantes.Variables
import com.geinzz.geinzwork.R
import com.geinzz.geinzwork.utils.constantes.constantes.constantesCarrito
import com.geinzz.geinzwork.databinding.BottomSheetAplarReporteBinding
import com.geinzz.geinzwork.databinding.ItemReportesBinding
import com.geinzz.geinzwork.model.dataClassReportes
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.firestore.FirebaseFirestore

class adapterReportes(
    private var listaR: MutableList<dataClassReportes>,
) :
    RecyclerView.Adapter<adapterReportes.ViewHolder>() {
    lateinit var dialog: BottomSheetDialog
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding =
            ItemReportesBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return listaR.size
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = listaR[position]
        holder.render(item)
    }

    inner class ViewHolder(private val binding: ItemReportesBinding) :
        RecyclerView.ViewHolder(binding.root) {
        @RequiresApi(Build.VERSION_CODES.O)
        fun render(item: dataClassReportes) {

            val tipoR = binding.TipoReporte
            val descripcion = binding.descripcion
            val fecha = binding.Fecha
            val hora = binding.hora
            val bntApelar = binding.apelar

            tipoR.text = item.tipoR
            descripcion.text = item.despcripcion
            fecha.text = item.fecha
            hora.text = item.hora
            binding.reporteID.text = item.idReporte
            verificarAplelado(binding.apelar,binding.apelado,item)
            bntApelar.setOnClickListener {
                dialog = BottomSheetDialog(itemView.context)
                habilitarBottomSheet(item, itemView.context, dialog)
                dialog.show()
            }
        }
    }

    private fun verificarAplelado(btnApelar:Button,apelado:TextView,dataClassReportes: dataClassReportes){
        val firestore = FirebaseFirestore.getInstance()
        val realtime = firestore.collection(Variables.politicas_problemas_verificaciones)
            .document(Variables.problemas)
            .collection(Variables.problemas)
            .document(dataClassReportes.idReporte.toString())
            .collection(Variables.apelado)

        realtime.get()
            .addOnSuccessListener { documents ->
                if (documents.isEmpty) {
                    btnApelar.isVisible=true
                    apelado.isVisible=false
                } else {
                    btnApelar.isVisible=false
                    apelado.isVisible=true
                }
            }
            .addOnFailureListener { exception ->
                Log.w("Firestore", "Error al verificar la colección 'apelado'", exception)
            }

    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun habilitarBottomSheet(
        dataClassReportes: dataClassReportes,
        context: Context,
        dialog: BottomSheetDialog
    ) {
        val binding = BottomSheetAplarReporteBinding.inflate(LayoutInflater.from(context))

        binding.btnCancel.setOnClickListener {
            dialog.dismiss()
        }
        constantesCarrito.setearDatosUsuario { nombre, numero, localidad, apellido ->
            binding.nombreED.setText(nombre)
            binding.apellidosED.setText(apellido)
            binding.telefonoed.setText(numero)
        }
        binding.idApelacioProblema.text = dataClassReportes.idReporte

        constantesCarrito.obtnerfechaHora(binding.hora, binding.fecha)

        binding.btnApply.setOnClickListener {
            val nombre = binding.nombreED.text.toString().trim()
            val apellido = binding.apellidosED.text.toString().trim()
            val telefono = binding.telefonoed.text.toString().trim()
            val motivo = binding.motivoED.text.toString().trim()
            val detalles = binding.dellatesED.text.toString().trim()

            if (nombre.isEmpty() || apellido.isEmpty() || telefono.isEmpty() || motivo.isEmpty() || detalles.isEmpty()) {
                Toast.makeText(
                    context,
                    "Complete todo los campos para enviar",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                val realtime =
                    FirebaseFirestore.getInstance().collection(Variables.politicas_problemas_verificaciones)
                        .document(Variables.problemas).collection(Variables.problemas)
                        .document(dataClassReportes.idReporte.toString()).collection(Variables.apelado)
                val hasmap = hashMapOf<String, Any>(
                    Variables.nombre to binding.nombreED.text.toString(),
                    Variables.apellido to binding.apellidosED.text.toString(),
                    Variables.telefono to binding.telefonoed.text.toString(),
                    Variables.motivo to binding.motivoED.text.toString(),
                    Variables.detalles to binding.dellatesED.text.toString(),
                    Variables.idReporte to binding.idApelacioProblema.text.toString()
                )
                realtime.add(hasmap).addOnSuccessListener {
                    Toast.makeText(
                        context,
                        context.getString(R.string.textoApelacionToast),
                        Toast.LENGTH_SHORT
                    ).show()
                    dialog.dismiss()
                }
                    .addOnFailureListener {
                        Toast.makeText(
                            context,
                            "Error al enviar la apelacion",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
            }
        }
        dialog.setContentView(binding.root)
    }

}