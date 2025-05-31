package com.geinzz.geinzwork.adapterViewholder

import android.annotation.SuppressLint
import android.os.Build
import android.text.SpannableString
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.example.geinzwork.constantesGeneral.Variables
import com.example.geinzwork.constantesGeneral.constantes_bottom_shet_trabaja
import com.example.geinzwork.constantesGeneral.constatnes_carga_imagenes_general
import com.geinzz.geinzwork.R
import com.geinzz.geinzwork.constantesGeneral.constantes
import com.geinzz.geinzwork.constantesGeneral.constantesPublicidad
import com.geinzz.geinzwork.constantesGeneral.constantes_servicios
import com.geinzz.geinzwork.constantesGeneral.constantestextos_general
import com.geinzz.geinzwork.databinding.BottomSheetContactoDirectoBinding
import com.geinzz.geinzwork.databinding.ItemAnunciosBinding
import com.geinzz.geinzwork.databinding.RecicleTrabajosBinding
import com.geinzz.geinzwork.dataclass.dataClassTrabajosd
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import de.hdodenhof.circleimageview.CircleImageView

class adapterCategorias
    (
    private var lista: MutableList<dataClassTrabajosd>,
    private val vermas: (dataClassTrabajosd) -> Unit,
    private val uidString: String

) : RecyclerView.Adapter<adapterCategorias.viewHolderCategorias>() {
    private lateinit var dialog: BottomSheetDialog
    private lateinit var firebaseAuth: FirebaseAuth

    @SuppressLint("SuspiciousIndentation")
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): viewHolderCategorias {
        val binding =
            RecicleTrabajosBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return viewHolderCategorias(binding)

    }

    override fun getItemCount(): Int {
        return if (lista.size >= 4) 4 else lista.size
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onBindViewHolder(holder: viewHolderCategorias, position: Int) {
        val item = lista[position]
        holder.render(item, vermas, uidString)
    }

    inner class viewHolderCategorias(private val binding: RecicleTrabajosBinding) :
        RecyclerView.ViewHolder(binding.root) {
        val name = binding.UserName
        val categoria = binding.categoria
        val btnVermas = binding.btnVermas
        val calificacion = binding.CalificaionStart
        val tipoTrabajo = binding.tipoTrabajo
        val imgaenTrabajo = binding.imgTrabajo
        val activo = binding.activo
        val imagenPerfilcicrle = binding.imgPerfilUser
        val nacionalidad = binding.nacionalidad
        val localidad = binding.localidad
        val actividad = binding.acvidad
        val tuCuenta = binding.tuCuenta
        val verificado = binding.verificados

        @RequiresApi(Build.VERSION_CODES.O)
        fun render(
            dataClassTrabajosd: dataClassTrabajosd,
            vermas: (dataClassTrabajosd) -> Unit,
            uidString: String
        ) {
            firebaseAuth = FirebaseAuth.getInstance()
            setearCampos(dataClassTrabajosd, vermas)
            constantes.obtenerFotoPerfil(
                dataClassTrabajosd,
                itemView.context,
                imgaenTrabajo,
                imagenPerfilcicrle
            )
            btnVermas.setOnLongClickListener {
                if (firebaseAuth.currentUser == null) {
                    dialog = BottomSheetDialog(itemView.context)
                    constantesPublicidad.CreacionCuentaBottom_shett(
                        itemView.context,
                        dialog
                    )
                    dialog.show()
                } else {
                    dialog = BottomSheetDialog(itemView.context)
                    bottomSheet_contacto_directo(dataClassTrabajosd.id.toString())
                    dialog.show()
                }

                true
            }
            constantes.setearBanderas(dataClassTrabajosd, itemView.context, nacionalidad)
            Tu_cuentaMostrado(uidString, dataClassTrabajosd)
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
            constantes.obtenerEstado(actividad, dataClassTrabajosd.id.toString())
        }

        fun bottomSheet_contacto_directo(item_id: String) {
            val bottom_bindig =
                BottomSheetContactoDirectoBinding.inflate(LayoutInflater.from(itemView.context))
            val view = bottom_bindig.root
            constantes_bottom_shet_trabaja.obntener_datos_trabajador(
                dialog,
                itemView.context,
                item_id.toString(),
                bottom_bindig
            )
            dialog.setContentView(view)

        }

        @SuppressLint("SuspiciousIndentation")
        @RequiresApi(Build.VERSION_CODES.O)
        fun setearCampos(
            dataClassTrabajosd: dataClassTrabajosd,
            vermas: (dataClassTrabajosd) -> Unit
        ) {
            name.text = dataClassTrabajosd.nombre
            constantes.setCategoria(dataClassTrabajosd, categoria)
            tipoTrabajo.text = dataClassTrabajosd.tipoT
            calificacion.text = dataClassTrabajosd.start
            localidad.text = dataClassTrabajosd.localidad
            val horaAM = constantes.quitaram(dataClassTrabajosd)
            val horaPM = constantes.quitarpm(dataClassTrabajosd)
            activo.text = "${horaAM} a ${horaPM}"
            btnVermas.setOnClickListener {
                vermas(dataClassTrabajosd)
            }

            if (dataClassTrabajosd.verificados == true) {
                binding.verificados.visibility = View.VISIBLE
            } else {
                binding.verificados.visibility = View.GONE
            }
        }

        fun Tu_cuentaMostrado(uidRegistrado: String, dataClassTrabajosd: dataClassTrabajosd) {
            if (uidRegistrado == dataClassTrabajosd.id.toString()) {
                tuCuenta.isVisible = true
            }
        }


    }
}