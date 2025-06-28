package com.geinzz.geinzwork.adapterViewholder

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.example.geinzwork.constantesGeneral.Variables
import com.example.geinzwork.constantesGeneral.constatnes_carga_imagenes_general
import com.example.geinzwork.dataclass.dataclas_trabajos_ralizados_verificados
import com.geinzz.geinzwork.R
import com.geinzz.geinzwork.databinding.ItemProductosTiendasBinding
import com.geinzz.geinzwork.dataclass.dataclas_trabajos_ralizados
import kotlin.collections.addAll
import kotlin.text.clear

class publicaciones_ralizadas(
    private val listaTrabajos_realizados: MutableList<dataclas_trabajos_ralizados>,
    private val editar_eliminar_estadi_archivar: (dataclas_trabajos_ralizados) -> Unit,
    private val onSeleccionCambio: (cantidad: Int, seleccionados: List<dataclas_trabajos_ralizados>) -> Unit

    ) : RecyclerView.Adapter<publicaciones_ralizadas.viewHolder>() {
    private val elementosSeleccionados = mutableSetOf<dataclas_trabajos_ralizados>()
    private var modoSeleccion = false


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): viewHolder {
        val binding =
            ItemProductosTiendasBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return viewHolder(binding)
    }

    override fun getItemCount(): Int {
        return listaTrabajos_realizados.size
    }

    override fun onBindViewHolder(holder: viewHolder, position: Int) {
        val item = listaTrabajos_realizados[position]
        holder.render(
            item,
            position,
            elementosSeleccionados,
            modoSeleccion,
            editar_eliminar_estadi_archivar,
            ::manejarSeleccion,
            ::activarModoSeleccion
        )
    }

    class viewHolder(private val binding: ItemProductosTiendasBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun render(
            item: dataclas_trabajos_ralizados,
            position: Int,
            elementosSeleccionados: Set<dataclas_trabajos_ralizados>, // recibe el conjunto
            modoSeleccion: Boolean,
            editar_eliminar_estadi_archivar: (dataclas_trabajos_ralizados) -> Unit,
            manejarSeleccion: (Int) -> Unit,
            activarModoSeleccion: (Int) -> Unit
        ) {
            binding.chekSelecionado.setOnCheckedChangeListener { _, isChecked ->
                manejarSeleccion(adapterPosition)

                // 🔄 Animación al hacer check
                binding.chekSelecionado.animate()
                    .scaleX(1.2f)
                    .scaleY(1.2f)
                    .setDuration(100)
                    .withEndAction {
                        binding.chekSelecionado.animate().scaleX(1f).scaleY(1f).setDuration(100).start()
                    }.start()
            }

            binding.tituloProducto.text = item.titulo
            binding.descripcionProducto.text = item.contenido

            val placeholderperfil = ContextCompat.getDrawable(
                itemView.context, R.drawable.cargando_img_geinz_500
            )

            constatnes_carga_imagenes_general.changer_img(
                binding.progressCargaImagen,
                itemView.context,
                item.img.toString(),
                null,
                binding.imgProducto,
                "portada", placeholderperfil
            ) {}

            // 🔴 Aquí defines si el ítem está seleccionado o no
            val estaSeleccionado = elementosSeleccionados.contains(item)

            // Fondo si está seleccionado
            if (estaSeleccionado) {
                    ContextCompat.getDrawable(itemView.context, R.drawable.car_selecionados)

            } else {
                binding.listenerPadre.background =
                    ContextCompat.getDrawable(itemView.context, R.drawable.card_trabajos)
            }



            // Mostrar el CheckBox si está en modo selección
            binding.chekSelecionado.isVisible = modoSeleccion

            binding.chekSelecionado.setOnCheckedChangeListener(null) // 🔒 evita loops infinitos

            binding.chekSelecionado.isChecked = estaSeleccionado // 🔁 setea el estado real

// ✅ Solo permitir interacción si está en modo selección
            binding.chekSelecionado.isEnabled = modoSeleccion

            binding.chekSelecionado.setOnCheckedChangeListener { _, isChecked ->
                if (modoSeleccion) {
                    manejarSeleccion(adapterPosition) // Esto ya actualiza fondo y selección
                }
            }


            // Click normal o seleccionar
            binding.listenerPadre.setOnClickListener {
                if (modoSeleccion) {
                    manejarSeleccion(adapterPosition)
                    Toast.makeText(itemView.context, "Seleccionando...", Toast.LENGTH_SHORT).show()
                    ContextCompat.getColor(itemView.context, R.color.selecion_multiple)
                } else {
                    Toast.makeText(itemView.context, "Click normal", Toast.LENGTH_SHORT).show()
                    editar_eliminar_estadi_archivar(item)
                }
            }


            // Long click: activar modo selección
            binding.listenerPadre.setOnLongClickListener {
                activarModoSeleccion(adapterPosition)
                Toast.makeText(itemView.context, "Modo selección activado", Toast.LENGTH_SHORT).show()
                true
            }


        }



    }



    private fun manejarSeleccion(position: Int) {
        val item = listaTrabajos_realizados[position]
        if (elementosSeleccionados.contains(item)) {
            elementosSeleccionados.remove(item)
        } else {
            elementosSeleccionados.add(item)
        }
        notifyItemChanged(position)
        onSeleccionCambio(elementosSeleccionados.size, elementosSeleccionados.toList()) // 🔴 ahora también pasamos la lista
    }


    private fun activarModoSeleccion(position: Int) {
        if (!modoSeleccion) {
            modoSeleccion = true
            manejarSeleccion(position) // ya notifica
            notifyDataSetChanged()
        }
    }

    fun estaEnModoSeleccion(): Boolean = modoSeleccion

    fun cancelarModoSeleccion() {
        modoSeleccion = false
        elementosSeleccionados.clear()
        notifyDataSetChanged()
        onSeleccionCambio(0, emptyList()) // notificar que no hay seleccionados
    }

    fun seleccionarTodos() {
        if (!modoSeleccion) modoSeleccion = true

        elementosSeleccionados.clear()
        elementosSeleccionados.addAll(listaTrabajos_realizados)

        notifyDataSetChanged()
        onSeleccionCambio(elementosSeleccionados.size, elementosSeleccionados.toList())
    }
    fun deseleccionarTodosSinSalirDeModo() {
        elementosSeleccionados.clear()
        notifyDataSetChanged()
        onSeleccionCambio(elementosSeleccionados.size, elementosSeleccionados.toList())
    }







}