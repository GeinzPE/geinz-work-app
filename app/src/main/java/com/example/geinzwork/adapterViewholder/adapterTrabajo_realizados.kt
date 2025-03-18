package com.geinzz.geinzwork.adapterViewholder

import ImageDialogFragmentURL
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.example.geinzwork.constantesGeneral.constatnes_carga_imagenes_general
import com.geinzz.geinzwork.constantesGeneral.constantestextos_general
import com.geinzz.geinzwork.databinding.ItemTrabajosRalizadosBinding
import com.geinzz.geinzwork.dataclass.dataclas_trabajos_ralizados

class adapterTrabajo_realizados(
    private val listaTrabajos_realizados: MutableList<dataclas_trabajos_ralizados>,
    private val listenerApp: ((dataclas_trabajos_ralizados) -> Unit)? = null // Permitir null
) : RecyclerView.Adapter<adapterTrabajo_realizados.viewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): viewHolder {
        val binding =
            ItemTrabajosRalizadosBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return viewHolder(binding, listenerApp)
    }

    override fun getItemCount(): Int {
        return if (listaTrabajos_realizados.size >= 5) 4 else listaTrabajos_realizados.size
    }

    override fun onBindViewHolder(holder: viewHolder, position: Int) {
        val item = listaTrabajos_realizados[position]
        holder.render(item)
    }

    class viewHolder(
        private val binding: ItemTrabajosRalizadosBinding,
        private val listenerApp: ((dataclas_trabajos_ralizados) -> Unit)? // Ahora puede ser null
    ) : RecyclerView.ViewHolder(binding.root) {

        fun render(item: dataclas_trabajos_ralizados) {
            val img = binding.imagenTrabajo
            val titulo = binding.titulo
            val contenido = binding.contenido
            val fecha = binding.fecha
            val hora = binding.hora

            // Solo ejecutar el listener si no es null
            img.setOnClickListener {
                listenerApp?.invoke(item)
            }

            // Listener de LongClick para mostrar la imagen en un diálogo
            img.setOnLongClickListener {
                item.img?.let { uri ->
                    val dialogFragment = ImageDialogFragmentURL.newInstance(uri)
                    dialogFragment.show(
                        (itemView.context as AppCompatActivity).supportFragmentManager,
                        "image_dialog"
                    )
                    true
                } ?: run {
                    Toast.makeText(itemView.context, "No URI available", Toast.LENGTH_SHORT).show()
                    false
                }
            }

            // Extender o acortar texto en título y contenido
            constantestextos_general.extender_acortar_texto(
                binding.titulo,
                binding.tvReadMoreTitulo
            )
            constantestextos_general.extender_acortar_texto(
                binding.contenido,
                binding.tvReadMoreContenido
            )

            // Cargar imagen con la función de carga
            constatnes_carga_imagenes_general.changer_img(
                binding.progressCargaImagen,
                itemView.context,
                item.img.toString(),
                null,
                img,
                "portada"
            ) {}

            // Asignar valores a los elementos de la UI
            titulo.text = item.titulo
            contenido.text = item.contenido
            fecha.text = item.fecha
            hora.text = item.hora
        }
    }
}

