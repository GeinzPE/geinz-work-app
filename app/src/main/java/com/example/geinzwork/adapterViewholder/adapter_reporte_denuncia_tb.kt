package com.example.geinzwork.adapterViewholder

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Build
import android.text.SpannableString
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.example.geinzwork.constantesGeneral.constantes_valores
import com.example.geinzwork.constantesGeneral.constatnes_carga_imagenes_general
import com.example.geinzwork.dataclass.dataclass_reporte_denuncia_tb
import com.geinzz.geinzwork.R
import com.geinzz.geinzwork.constantesGeneral.constantesCarrito
import com.geinzz.geinzwork.constantesGeneral.constantestextos_general
import com.geinzz.geinzwork.databinding.BottomSheetAplarReporteBinding
import com.geinzz.geinzwork.databinding.ItmeDenunciaTrabajadorBinding
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions

class adapter_reporte_denuncia_tb(
    private val lista: MutableList<dataclass_reporte_denuncia_tb>,
    private val bottomSheet_listener: (dataclass_reporte_denuncia_tb) -> Unit
) :
    RecyclerView.Adapter<adapter_reporte_denuncia_tb.ViewHolderReporte>() {
    private lateinit var dialog: BottomSheetDialog
    private lateinit var firebaseAuth: FirebaseAuth
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolderReporte {
        val binding = ItmeDenunciaTrabajadorBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolderReporte(binding)
    }

    override fun getItemCount(): Int {
        return lista.size
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onBindViewHolder(holder: ViewHolderReporte, position: Int) {
        val item = lista[position]
        holder.render(item)
    }

    inner class ViewHolderReporte(private val binding: ItmeDenunciaTrabajadorBinding) :
        RecyclerView.ViewHolder(binding.root) {
        @RequiresApi(Build.VERSION_CODES.O)
        fun render(item: dataclass_reporte_denuncia_tb) {

            binding.layoutRp.setOnClickListener {
                bottomSheet_listener(item)
            }

            val impPerfil = binding.imgTrabajador
            val tipo_reporte = binding.tipoReporteRealziado
            val estado = binding.colorReview
            binding.idDenuncia.text = item.idreporte
            val problema = binding.textoReporte
            tipo_reporte.text = item.tipoReporte
            val copy_id = binding.copyId
            binding.apelar.setOnClickListener {
                dialog = BottomSheetDialog(itemView.context)
                habilitarBottomSheet(item, itemView.context, dialog, binding)
                dialog.show()

            }
            copy_id.setOnClickListener {
                constantestextos_general.copiarTexto_portapapeles(binding.idDenuncia,itemView.context)

            }
            val spannableString = SpannableString("Problema : ${item.problema}")
            constantestextos_general.setearInformacionboldDescripcion(
                "Problema",
                spannableString, problema
            )

            val spannableStringTipoReporte =
                SpannableString("Tipo de reporte : ${item.tipoReporte}")
            constantestextos_general.setearInformacionboldDescripcion(
                "Tipo de reporte", spannableStringTipoReporte, binding.tipoReporteRealziado
            )

            val spannableStringFecha =
                SpannableString("Fecha de envio : ${item.fechaEnvio}")
            constantestextos_general.setearInformacionboldDescripcion(
                "Fecha de envio",
                spannableStringFecha, binding.fechaEnvio
            )

            val spannableStringHoraEnvio = SpannableString("Hora de envio : ${item.horaEnvio}")
            constantestextos_general.setearInformacionboldDescripcion(
                "Hora de envio",
                spannableStringHoraEnvio,
                binding.horaEnvio
            )
            if (item.tipo_enviado_recivido.equals("enviados")) {
                constantesCarrito.setearDatosUsuarioImgNombre(item.idtrabajador.toString()) { nombre, img, apellido, nacionalidad, categoria, verificado, trabajador_user ->
                    // Verificación después de setear
                    binding.nombreTrabajador.text = nombre
                    val nombreYaCargado = binding.nombreTrabajador.text.toString() == nombre
                    val nombreValido =
                        nombre.toString().isNotBlank()

                    if (nombreYaCargado) {
                        binding.cargaContenido.isVisible = false
                        binding.cargaNombreImg.isVisible = true
                    } else {
                        binding.cargaContenido.isVisible = false
                        binding.cargaNombreImg.isVisible = false

                    }
                    constatnes_carga_imagenes_general.changer_img(
                        binding.cargaProgresIndicator,
                        itemView.context,
                        img.toString(),
                        impPerfil,
                        null,
                        "perfil",
                        constantes_valores.getDrawableMiIcono(itemView.context)
                    ) { cargado ->

                    }

                }

                when (item.estado!!.lowercase()) {
                    "enviado" -> {
                        estado.setBackgroundResource(R.drawable.reporte_enviado)
                    }

                    "proceso" -> {
                        estado.setBackgroundResource(R.drawable.reporte_proceso)
                    }

                    "aceptado" -> {
                        estado.setBackgroundResource(R.drawable.reporte_aceptado)
                    }

                    "rechazado" -> {
                        estado.setBackgroundResource(R.drawable.reporte_rechazado)
                    }

                    "archivado" -> {
                        estado.setBackgroundResource(R.drawable.reporte_archivado)
                    }

                    "resuelto" -> {
                        estado.setBackgroundResource(R.drawable.reporte_resuelto)
                    }

                    "cancelado" -> {
                        estado.setBackgroundResource(R.drawable.reporte_cancelado)
                    }


                }

            } else if (item.tipo_enviado_recivido.equals("recivido")) {
                constantesCarrito.setearDatosUsuarioImgNombre(item.idusuario.toString()) { nombre, img, apellido, nacionalidad, categoria, verificado, trabajador_user ->
                    // Verificación después de setear
                    binding.nombreTrabajador.text = nombre
                    val nombreYaCargado = binding.nombreTrabajador.text.toString() == nombre
                    val nombreValido =
                        nombre.toString().isNotBlank()

                    if (nombreYaCargado) {
                        binding.cargaContenido.isVisible = false
                        binding.cargaNombreImg.isVisible = true
                    } else {
                        binding.cargaContenido.isVisible = false
                        binding.cargaNombreImg.isVisible = false

                    }
                    constatnes_carga_imagenes_general.changer_img(
                        binding.cargaProgresIndicator,
                        itemView.context,
                        img.toString(),
                        impPerfil,
                        null,
                        "perfil",
                        constantes_valores.getDrawableMiIcono(itemView.context)
                    ) { cargado ->


                    }
                }
                when (item.estado!!.lowercase()) {
                    "apelado" -> {
                        estado.setBackgroundResource(R.drawable.reporte_enviado)
                        binding.apelar.isVisible = false
                    }

                    "por apelar" -> {
                        estado.setBackgroundResource(R.drawable.reporte_proceso)
                        binding.apelar.isVisible = true
                    }

                    "apelacion exitosa" -> {
                        estado.setBackgroundResource(R.drawable.reporte_aceptado)
                        binding.apelar.isVisible = false
                    }

                    "apelacion rechazada" -> {
                        estado.setBackgroundResource(R.drawable.reporte_rechazado)
                        binding.apelar.isVisible = false

                    }

                }
            } else {
                constantesCarrito.setearDatosUsuarioImgNombre(item.idreporte.toString()) { nombre, img, apellido, nacionalidad, categoria, verificado, trabajador_user ->
                    // Verificación después de setear
                    binding.nombreTrabajador.text = nombre
                    val nombreYaCargado = binding.nombreTrabajador.text.toString() == nombre
                    val nombreValido =
                        nombre.toString().isNotBlank()

                    if (nombreYaCargado) {
                        binding.cargaContenido.isVisible = false
                        binding.cargaNombreImg.isVisible = true
                    } else {
                        binding.cargaContenido.isVisible = false
                        binding.cargaNombreImg.isVisible = false

                    }
                    constatnes_carga_imagenes_general.changer_img(
                        binding.cargaProgresIndicator,
                        itemView.context,
                        img.toString(),
                        impPerfil,
                        null,
                        "perfil",
                        constantes_valores.getDrawableMiIcono(itemView.context)
                    ) { cargado ->


                    }
                }
            }


        }

        @RequiresApi(Build.VERSION_CODES.O)
        private fun habilitarBottomSheet(
            dataclass_reporte_denuncia_tb: dataclass_reporte_denuncia_tb,
            context: Context,
            dialog: BottomSheetDialog,
            binding_principal: ItmeDenunciaTrabajadorBinding

        ) {
            firebaseAuth = FirebaseAuth.getInstance()
            val binding = BottomSheetAplarReporteBinding.inflate(LayoutInflater.from(context))

            binding.btnCancel.setOnClickListener {
                dialog.dismiss()
            }
            constantesCarrito.setearDatosUsuario { nombre, numero, localidad, apellido ->
                binding.nombreED.setText(nombre)
                binding.apellidosED.setText(apellido)
                binding.telefonoed.setText(numero)
            }
            binding.idApelacioProblema.text = dataclass_reporte_denuncia_tb.idreporte

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
                    val db =
                        FirebaseFirestore.getInstance().collection("Trabajadores_Usuarios_Drivers")
                            .document("trabajadores").collection("trabajadores")
                            .document(firebaseAuth.uid.toString())
                            .collection("reporte").document("recivido").collection("recivido")
                            .document(dataclass_reporte_denuncia_tb.idreporte.toString())
                    val hasmap =
                        hashMapOf<String, Any>(
                            "idReporte" to dataclass_reporte_denuncia_tb.idreporte.toString(),
                            "fecha_envio" to binding.fecha.text.toString(),
                            "hora_envio" to binding.hora.text.toString(),
                            "nombre" to nombre,
                            "apellido" to apellido,
                            "numero" to telefono,
                            "motivo" to motivo,
                            "detalle" to detalles
                        )
                    val datos_apelados = hashMapOf<String, Any>(
                        "apelado" to hasmap
                    )
                    db.set(datos_apelados, SetOptions.merge()).addOnSuccessListener { res ->
                        Toast.makeText(
                            itemView.context,
                            "apelacion enviada correctamente",
                            Toast.LENGTH_SHORT
                        ).show()
                        db.get().addOnSuccessListener { res ->

                            val hashmapEstado = hashMapOf<String, Any>(
                                "estado" to "apelado"
                            )
                            db.set(hashmapEstado, SetOptions.merge()).addOnSuccessListener { res ->
                                Log.d("estado_cambiado", "estado cambiado correctamente")
                                binding_principal.apelar.isVisible = false
                                binding_principal.colorReview.setBackgroundResource(R.drawable.reporte_enviado)
                                dialog.dismiss()
                            }.addOnFailureListener { e ->
                                Log.d("error_cambiarEstado", "error al cambiar el estado")
                            }
                        }

                    }.addOnFailureListener { e ->
                        Toast.makeText(
                            itemView.context,
                            "error al enviar la apelacion",
                            Toast.LENGTH_SHORT
                        ).show()
                    }

                }

            }
            dialog.setContentView(binding.root)
        }

    }
}