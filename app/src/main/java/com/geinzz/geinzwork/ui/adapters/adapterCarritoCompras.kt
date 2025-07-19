package com.geinzz.geinzwork.ui.adapters

import android.content.DialogInterface
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.geinzz.geinzwork.databinding.ItemCarritoComprasBinding
import com.geinzz.geinzwork.model.dataclassCarritoCompras
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class adapterCarritoCompras(
    private var listaItem: MutableList<dataclassCarritoCompras>,
    private val verItems: (dataclassCarritoCompras) -> Unit,

    ) : RecyclerView.Adapter<adapterCarritoCompras.viewHolderCarritoCompras>() {
    private lateinit var firebaseAuth: FirebaseAuth
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): viewHolderCarritoCompras {
        val binding =
            ItemCarritoComprasBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return viewHolderCarritoCompras(binding)
    }

    override fun getItemCount(): Int {
        return listaItem.size
    }

    override fun onBindViewHolder(holder: viewHolderCarritoCompras, position: Int) {
        val item = listaItem[position]
        holder.render(item, verItems)
    }

    inner class viewHolderCarritoCompras(private val binding: ItemCarritoComprasBinding) :
        RecyclerView.ViewHolder(binding.root) {
        val imgART = binding.imgTrabajo
        val nombreART = binding.tituloItem
        val precioART = binding.precioItem
        val nombreTIENDA = binding.NombreTienda
        val sumarItem = binding.mas
        val menosItem = binding.menos
        val listener = binding.NombreTiendaRelative
        val cantidad = binding.cantidad
        val contenidoItem = binding.contenidoItem
        private val handler = Handler(Looper.getMainLooper())
        private var updateRunnable: Runnable? = null
        val Delete = binding.borrarItem
        fun render(
            dataclassCarritoCompras: dataclassCarritoCompras,
            verItems: (dataclassCarritoCompras) -> Unit,
        ) {
            try {
                Glide.with(itemView.context)
                    .load(dataclassCarritoCompras.imgArticulo)
                    .into(imgART)
            } catch (e: Exception) {
                println("error al setear la img")
            }
            Delete.setOnClickListener {
                eliminarItem(dataclassCarritoCompras)
            }
            nombreART.text = dataclassCarritoCompras.nombre
            nombreTIENDA.text = dataclassCarritoCompras.nombreTienda
            precioART.text = dataclassCarritoCompras.precio
            cantidad.text = dataclassCarritoCompras.cantidad
            listener.setOnClickListener { verItems(dataclassCarritoCompras) }
            sumarItem.setOnClickListener { aumentar_cantidad(dataclassCarritoCompras) }
            menosItem.setOnClickListener { restar_cantidad(dataclassCarritoCompras) }
            contenidoItem.text = dataclassCarritoCompras.descripcion

        }


        private fun aumentar_cantidad(dataclassCarritoCompras: dataclassCarritoCompras) {
            val cantidadActual = dataclassCarritoCompras.cantidad?.toIntOrNull() ?: 0
            val cantidadFinal = cantidadActual + 1
            dataclassCarritoCompras.cantidad = cantidadFinal.toString()
            cantidad.text = dataclassCarritoCompras.cantidad
            precioART.text = dataclassCarritoCompras.precio
            programarActualizacionFirebase(dataclassCarritoCompras, true)
        }

        private fun restar_cantidad(dataclassCarritoCompras: dataclassCarritoCompras) {
            val cantidadActual = dataclassCarritoCompras.cantidad?.toIntOrNull() ?: 0
            if (cantidadActual <= 1) {
                Toast.makeText(itemView.context, "El mínimo es un producto", Toast.LENGTH_SHORT)
                    .show()
                return
            }

            val cantidadFinal = cantidadActual - 1
            dataclassCarritoCompras.cantidad = cantidadFinal.toString()
            cantidad.text = dataclassCarritoCompras.cantidad
            precioART.text = dataclassCarritoCompras.precio
            programarActualizacionFirebase(dataclassCarritoCompras, false)
        }

        private fun programarActualizacionFirebase(
            dataclassCarritoCompras: dataclassCarritoCompras,
            sumar: Boolean
        ) {
            updateRunnable?.let { handler.removeCallbacks(it) }
            updateRunnable = Runnable { actualizarEnRealtime(dataclassCarritoCompras, sumar) }
            handler.postDelayed(updateRunnable!!, 5000) // 5 segundos de retraso
        }

        private fun actualizarEnRealtime(
            dataclassCarritoCompras: dataclassCarritoCompras,
            sumar: Boolean
        ) {
            val firebaseAuth = FirebaseAuth.getInstance()
            val realtime = FirebaseDatabase.getInstance().getReference("carritoGeneral")
                .child(firebaseAuth.uid.toString())
            val cantidadActual = dataclassCarritoCompras.cantidad?.toIntOrNull() ?: 0
            realtime.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    for (tiendaSnapshot in snapshot.children) {
                        for (productoSnapshot in tiendaSnapshot.children) {
                            val idProducto = productoSnapshot.key
                            if (dataclassCarritoCompras.id == idProducto) {
                                val cantidadActualdb =
                                    productoSnapshot.child("cantidad").getValue(String::class.java)
                                        ?.toIntOrNull() ?: 0
                                var nuevaCantidad = cantidadActualdb

                                if (sumar) {
                                    nuevaCantidad = cantidadActual
                                } else {
                                    nuevaCantidad = cantidadActual
                                }

                                productoSnapshot.child("cantidad").ref.setValue(nuevaCantidad.toString())
                                    .addOnSuccessListener {
                                        // Éxito al actualizar la cantidad
                                    }
                                    .addOnFailureListener { e ->
                                        println("Error al actualizar la cantidad: ${e.message}")
                                    }
                            }
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    println("Error en la lectura de datos: ${error.message}")
                }
            })
        }

        private fun eliminarItem(dataclassCarritoCompras: dataclassCarritoCompras) {
            val firebaseAuth = FirebaseAuth.getInstance()
            val db = FirebaseDatabase.getInstance().getReference("carritoGeneral")
                .child(firebaseAuth.uid.toString())
                .child(dataclassCarritoCompras.idTienda.toString())
                .child(dataclassCarritoCompras.id.toString())

            // Crear un AlertDialog de confirmación
            val builder = AlertDialog.Builder(itemView.context)
            builder.setTitle("Eliminar Item")
                .setMessage("¿Estás seguro de que deseas eliminar este item?")
                .setPositiveButton("Sí") { dialogInterface: DialogInterface, i: Int ->

                    db.removeValue()
                        .addOnSuccessListener {
                            Toast.makeText(
                                itemView.context,
                                "Item elimindo correctemente en unos momentos veras tus cambios",Toast.LENGTH_SHORT
                            ).show()
                            println("Elemento eliminado exitosamente")
                        }
                        .addOnFailureListener { e ->
                            // Manejo de errores al eliminar el elemento
                            println("Error al eliminar el elemento: ${e.message}")
                        }
                    dialogInterface.dismiss() // Cerrar el diálogo
                }
                .setNegativeButton("No") { dialogInterface: DialogInterface, i: Int ->
                    // Usuario ha cancelado la eliminación del item
                    dialogInterface.dismiss() // Cerrar el diálogo sin hacer nada
                }
                .show()
        }


    }
}