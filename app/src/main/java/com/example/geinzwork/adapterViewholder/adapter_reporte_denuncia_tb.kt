package com.example.geinzwork.adapterViewholder

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.example.geinzwork.constantesGeneral.constantes_valores
import com.example.geinzwork.constantesGeneral.constatnes_carga_imagenes_general
import com.example.geinzwork.dataclass.dataclass_reporte_denuncia_tb
import com.geinzz.geinzwork.constantesGeneral.constantesCarrito
import com.geinzz.geinzwork.databinding.ItmeDenunciaTrabajadorBinding

class adapter_reporte_denuncia_tb(private val lista: MutableList<dataclass_reporte_denuncia_tb>) :
    RecyclerView.Adapter<adapter_reporte_denuncia_tb.ViewHolderReporte>() {

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

    override fun onBindViewHolder(holder: ViewHolderReporte, position: Int) {
        val item = lista[position]
        holder.render(item)
    }

    inner class ViewHolderReporte(private val binding: ItmeDenunciaTrabajadorBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun render(item: dataclass_reporte_denuncia_tb) {
            val impPerfil = binding.imgTrabajador
            val tipo_reporte = binding.tipoReporteRealziado
            val estado = binding.colorReview
            val problema = binding.textoReporte


            tipo_reporte.text = item.tipoReporte
            problema.text = item.problema
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
                    if(cargado){
                        Toast.makeText(itemView.context,"cagadocorretme",Toast.LENGTH_SHORT).show()

                    }else{
                        Toast.makeText(itemView.context,"no se cargo ajajjaja",Toast.LENGTH_SHORT).show()

                    }

                }

            }


        }

    }
}