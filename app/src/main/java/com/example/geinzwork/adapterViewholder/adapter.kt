package com.geinzz.geinzwork.adapterViewholder

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Build
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.RelativeLayout
import androidx.annotation.RequiresApi
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.example.geinzwork.constantesGeneral.Variables
import com.geinzz.geinzwork.R
import com.geinzz.geinzwork.constantesGeneral.constantes
import com.geinzz.geinzwork.constantesGeneral.constantesPublicidad
import com.geinzz.geinzwork.constantesGeneral.constantes_servicios
import com.geinzz.geinzwork.databinding.BottomSheetContactoDirectoBinding
import com.geinzz.geinzwork.databinding.RecicleTrabajosBinding
import com.geinzz.geinzwork.dataclass.dataClassTrabajosd
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.auth.FirebaseAuth

class adapter(
    private var finish: Boolean,
    private var listaTrabajos: MutableList<dataClassTrabajosd>,
    private val uidString: String,
    private var cantidadMostrado: Int,
    private var match_parent: Boolean

) : RecyclerView.Adapter<adapter.viewHolder>() {
    init {
        listaTrabajos.shuffle()
    }

    private lateinit var dialog: BottomSheetDialog
    private lateinit var firebaseAuth:FirebaseAuth

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): viewHolder {
        val binding =
            RecicleTrabajosBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return viewHolder(binding)
    }

    override fun getItemCount(): Int {
        return if (cantidadMostrado in 1 until listaTrabajos.size) cantidadMostrado else listaTrabajos.size

    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onBindViewHolder(holder: viewHolder, position: Int) {
        val item = listaTrabajos[position]
        holder.render(item, uidString)
    }


    inner class viewHolder(private val binding: RecicleTrabajosBinding) :
        RecyclerView.ViewHolder(binding.root) {
        val name = binding.UserName
        val categoria = binding.categoria
        val tipoTrabajo = binding.tipoTrabajo
        val btnVermas = binding.btnVermas
        val calificacion = binding.CalificaionStart
        val imgaenTrabajo = binding.imgTrabajo
        val activo = binding.activo
        val imagenPerfilcicrle = binding.imgPerfilUser
        val nacionalidad = binding.nacionalidad
        val localidad = binding.localidad
        val actividad = binding.acvidad
        val tuCuenta = binding.tuCuenta


        @RequiresApi(Build.VERSION_CODES.O)
        fun render(
            dataClassTrabajosd: dataClassTrabajosd,
            uidString: String,
        ) {
            firebaseAuth= FirebaseAuth.getInstance()
            btnVermas.setOnLongClickListener {
                if (firebaseAuth.currentUser == null) {
                    dialog = BottomSheetDialog(itemView.context)
                    constantesPublicidad.CreacionCuentaBottom_shett(
                        itemView.context,
                        dialog
                    )
                    dialog.show()
                }else{
                    dialog = BottomSheetDialog(itemView.context)
                    bottomSheet_contacto_directo(dataClassTrabajosd.id.toString())
                    dialog.show()
                }
                true
            }

            if (match_parent) {
                val params = btnVermas.layoutParams as ViewGroup.MarginLayoutParams
                params.width = ViewGroup.LayoutParams.MATCH_PARENT
                btnVermas.layoutParams = params


            }
            setearCompo(dataClassTrabajosd)
            constantes.obtenerFotoPerfil(
                dataClassTrabajosd,
                itemView.context,
                imgaenTrabajo,
                imagenPerfilcicrle
            )
            constantes.setearBanderas(dataClassTrabajosd, itemView.context, nacionalidad)
            Tu_cuentaMostrado(uidString, dataClassTrabajosd)
        }

        fun bottomSheet_contacto_directo(item_id: String) {
            val bottom_bindig =
                BottomSheetContactoDirectoBinding.inflate(LayoutInflater.from(itemView.context))
            val view = bottom_bindig.root

            dialog.setContentView(view)

        }

        fun promedioEstrellas(dataClassTrabajosd: dataClassTrabajosd): String {
            val puntaje = dataClassTrabajosd.start.toString().toIntOrNull()
            return when {
                puntaje == null -> "No se pudo calcular el promedio"
                puntaje >= 70 -> "5"
                puntaje >= 60 -> "4.5"
                puntaje >= 50 -> "4"
                puntaje >= 40 -> "3.5"
                puntaje >= 30 -> "3"
                else -> "2"
            }
        }

        @RequiresApi(Build.VERSION_CODES.O)
        fun setearCompo(
            dataClassTrabajosd: dataClassTrabajosd,
        ) {
            name.text = dataClassTrabajosd.nombre
            tipoTrabajo.text = dataClassTrabajosd.tipoT
            constantes.setCategoria(dataClassTrabajosd, categoria)
            calificacion.text =
                "${promedioEstrellas(dataClassTrabajosd)} (${dataClassTrabajosd.start})"
            localidad.text = dataClassTrabajosd.localidad
            constantes_servicios.verificarEstado_vericiacion(
                binding.verificados,
                dataClassTrabajosd.id.toString()
            ) { v, plan ->
                when (plan) {
                    Variables.plaA -> {
                        binding.verificados.setImageResource(R.drawable.verificado_a)

                    }

                    Variables.planB -> {
                        binding.verificados.setImageResource(R.drawable.icon_verificado)
                    }

                    Variables.PlanC -> {
                        binding.verificados.setImageResource(R.drawable.verificado_c)


                    }
                }

            }


            btnVermas.setOnClickListener {
                val vista = Intent(
                    itemView.context,
                    com.geinzz.geinzwork.vistaTrabajador.vistaTrabajador::class.java
                )
                vista.putExtra(Variables.id, dataClassTrabajosd.id)
                vista.putExtra(Variables.imagenPerfil, dataClassTrabajosd.imgpriamria)
                vista.putExtra(Variables.nombreUSer, dataClassTrabajosd.nombre)
                vista.putExtra(Variables.nacionalidad, dataClassTrabajosd.nacionalidad)
                vista.putExtra(Variables.categoria, dataClassTrabajosd.categoria)
                itemView.context.startActivity(vista)

                if (finish) {
                    (itemView.context as? Activity)?.finish()
                }
            }


            val horaAM = constantes.quitaram(dataClassTrabajosd)
            val horaPM = constantes.quitarpm(dataClassTrabajosd)
            activo.text = "${horaAM} a ${horaPM}"
            constantes.obtenerEstado(actividad, dataClassTrabajosd.id.toString())
        }

        fun Tu_cuentaMostrado(uidRegistrado: String, dataClassTrabajosd: dataClassTrabajosd) {
            if (uidRegistrado == dataClassTrabajosd.id.toString()) {
                tuCuenta.isVisible = true
            }
        }
    }


}