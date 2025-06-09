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
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.example.geinzwork.constantesGeneral.constantes_valores
import com.example.geinzwork.constantesGeneral.constantes_vinculados
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
    private val bottomSheet_listener: (dataclass_reporte_denuncia_tb) -> Unit,
    private val apelar: (dataclass_reporte_denuncia_tb) -> Unit,
    private val archivar_cancelar_review: (dataclass_reporte_denuncia_tb) -> Unit,
    private val archivar_cancelar_reportes: (dataclass_reporte_denuncia_tb) -> Unit
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
                apelar(item)
            }

            copy_id.setOnClickListener {
                constantestextos_general.copiarTexto_portapapeles(
                    binding.idDenuncia,
                    itemView.context
                )
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
                    binding.layoutRp.setOnLongClickListener {
                        archivar_cancelar_reportes(item)

                        true
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
                Toast.makeText(itemView.context, "entramos a las review", Toast.LENGTH_SHORT).show()
                constantesCarrito.setearDatosUsuarioImgNombre(item.idUsuario_review.toString()) { nombre, img, apellido, nacionalidad, categoria, verificado, trabajador_user ->
                    // Verificación después de setear
                    binding.nombreTrabajador.text = nombre
                    val nombreYaCargado = binding.nombreTrabajador.text.toString() == nombre

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
                    ) { cargado -> }
                    binding.layoutRp.setOnLongClickListener {
                        archivar_cancelar_review(item)

                        true
                    }

                }
            }


        }
    }
}