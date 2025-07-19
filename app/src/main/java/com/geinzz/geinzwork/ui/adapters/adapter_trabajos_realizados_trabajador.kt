package com.geinzz.geinzwork.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.geinzz.geinzwork.utils.constantes.constantes.constatnes_carga_imagenes_general
import com.geinzz.geinzwork.model.dataclass_adapter_promociones
import com.geinzz.geinzwork.databinding.ItemTrabajosReallizadosTrabajadoresBinding

class adapter_trabajos_realizados_trabajador(
    private val mostrarTodos: Boolean ,
    private val lista_item: MutableList<dataclass_adapter_promociones>,
    private val listener: (dataclass_adapter_promociones) -> Unit,

) : RecyclerView.Adapter<adapter_trabajos_realizados_trabajador.viewHoldertrabajosRealizados>() {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): viewHoldertrabajosRealizados {
        val binding = ItemTrabajosReallizadosTrabajadoresBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return viewHoldertrabajosRealizados(binding)
    }

    override fun getItemCount(): Int = if (mostrarTodos) lista_item.size else lista_item.size.coerceAtMost(5)

    override fun onBindViewHolder(holder: viewHoldertrabajosRealizados, position: Int) {
        val item = lista_item[position]
        holder.render(item)
    }

    inner class viewHoldertrabajosRealizados(private val binding: ItemTrabajosReallizadosTrabajadoresBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun render(item: dataclass_adapter_promociones) {
            constatnes_carga_imagenes_general.changer_img(
                binding.progressCargaImagen,
                itemView.context,
                item.img.toString(),
                null,
                binding.imgTrabajo,
                "portada",
                null
            ){}
            binding.tituloTrabajo.text = item.titulo_promo
            binding.textoTrabajo.text = item.texto_promo

            binding.fechaPublicada.text = item.fecha
            binding.horaPublicadad.text = item.hora

            binding.imgTrabajo.setOnClickListener {
                listener(item)
            }
        }
    }
}
