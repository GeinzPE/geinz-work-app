package com.geinzz.geinzwork.ui.adapters

import android.text.SpannableString
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.geinzz.geinzwork.utils.constantes.constantes.constatnes_carga_imagenes_general
import com.geinzz.geinzwork.model.dataclas_direcion_lat_log
import com.geinzz.geinzwork.utils.constantes.constantes.constantestextos_general
import com.geinzz.geinzwork.databinding.LayoutLatLogBinding

class adapter_direccion_lat_log(
    private val lista_direcciones: MutableList<dataclas_direcion_lat_log>,
    private val eliminar_editar: (dataclas_direcion_lat_log) -> Unit
) : RecyclerView.Adapter<adapter_direccion_lat_log.viewHolder_lat_log>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): viewHolder_lat_log {
        val binding =
            LayoutLatLogBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return viewHolder_lat_log(binding)
    }

    override fun getItemCount(): Int = lista_direcciones.size

    override fun onBindViewHolder(holder: viewHolder_lat_log, position: Int) {
        val item = lista_direcciones[position]
        holder.render(item)
    }

    inner class viewHolder_lat_log(private val binding: LayoutLatLogBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun render(item: dataclas_direcion_lat_log) {
            binding.eliminarEditar.setOnClickListener {
                eliminar_editar(item)
            }
            val nombre =
                SpannableString("Nombre de referencia : ${item.nombreRef}")
            constantestextos_general.setearInformacionboldDescripcion(
                "Nombre de referencia",
                nombre, binding.nombreColeccion
            )
            val fecha =
                SpannableString("Hora creada : ${item.hora}")
            constantestextos_general.setearInformacionboldDescripcion(
                "Hora creada",
                fecha, binding.horaCreada
            )
            val hora =
                SpannableString("Fecha creada : ${item.fecha}")
            constantestextos_general.setearInformacionboldDescripcion(
                "Fecha creada",
                hora, binding.fechaCreada
            )

            constatnes_carga_imagenes_general.changer_img(
                binding.progressCargaImagenFondo,
                itemView.context,
                item.img.toString(),
                null, binding.img, "portada", null
            ) { cargado -> }
        }

    }
}