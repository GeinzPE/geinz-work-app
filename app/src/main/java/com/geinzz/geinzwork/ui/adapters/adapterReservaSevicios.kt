package com.geinzz.geinzwork.ui.adapters

import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.geinzz.geinzwork.vistaTiendas.agregarPagoComprareservaTiendas
import com.geinzz.geinzwork.databinding.ItemReservaServiciosBinding
import com.geinzz.geinzwork.model.dataclassServiciosHistorial
import com.google.android.material.imageview.ShapeableImageView
import com.google.firebase.firestore.FirebaseFirestore

class adapterReservaSevicios(private var listaReserva: List<dataclassServiciosHistorial>) :
    RecyclerView.Adapter<adapterReservaSevicios.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding =
            ItemReservaServiciosBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return listaReserva.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = listaReserva[position]
        holder.render(item)
    }

    inner class ViewHolder(private val binding: ItemReservaServiciosBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun render(dataclassServiciosHistorial: dataclassServiciosHistorial) {
            binding.nombreuser.text = dataclassServiciosHistorial.nombre
            binding.apellidos.text = dataclassServiciosHistorial.apellido
            binding.metodopago.text = dataclassServiciosHistorial.metodoPago
            binding.dni.text = dataclassServiciosHistorial.dni
            binding.fechaus.text = dataclassServiciosHistorial.fecha
            binding.horaus.text = dataclassServiciosHistorial.hora
            binding.numerous.text = dataclassServiciosHistorial.numero
            binding.estadous.text = dataclassServiciosHistorial.estado
            binding.total.text = dataclassServiciosHistorial.totaPAgar
            binding.precioItem.text = dataclassServiciosHistorial.precioServicio
            binding.codigoReserva.text = dataclassServiciosHistorial.codeReserva
            binding.TipoRerserva.text = dataclassServiciosHistorial.tipoReserva
            binding.horaLlegada.text = dataclassServiciosHistorial.horaLlegada
            binding.totalDelivery.text = dataclassServiciosHistorial.precioDriver
            binding.deliveryPrecio.text = dataclassServiciosHistorial.precioDriver
            binding.fechaLlegada.text = dataclassServiciosHistorial.fechaLlegada
            binding.metodoEntrega.text = dataclassServiciosHistorial.metodoEntrega.toString()

            binding.containerListenr.setOnClickListener {
                agregarPAgo(dataclassServiciosHistorial)
            }

            val pagoString = binding.total.text.toString().toDouble()
            binding.adelanto.text = (pagoString / 2).toString()

            if (!dataclassServiciosHistorial.cantidad.isNullOrEmpty()) {
                binding.cantidadSelecionada.text = dataclassServiciosHistorial.cantidad
            } else {
                binding.LinealCantidad.isVisible = false
            }



            obtnerImagenServicio(
                dataclassServiciosHistorial.idServicio.toString(),
                dataclassServiciosHistorial.idTienda.toString(),
                dataclassServiciosHistorial.tipoReserva.toString()
            )

            obtenerDatosTienda(dataclassServiciosHistorial.idTienda.toString()) { nombreTienda, tipoTienda, localidad, img ->
                if (nombreTienda != null && tipoTienda != null && localidad != null) {
                    try {
                        Glide.with(itemView.context)
                            .load(img)
                            .into(binding.imgTienda)
                    } catch (e: Exception) {
                        println("error al setear la img")
                    }
                    binding.nombreTienda.text = nombreTienda
                    binding.TipoTienda.text = tipoTienda
                    binding.localidadTienda.text = localidad
                } else {
                    Log.e(TAG, "No se encontraron datos de la tienda o hubo un error")
                }
            }
            if (dataclassServiciosHistorial.tipoReserva.equals("Reserva Servicios")) {
                binding.linealPrecioDelivery.isVisible = false
                binding.linealTotalDelivery.isVisible = false

            }

            if (dataclassServiciosHistorial.fechaLlegada.isNullOrEmpty()) {
                binding.linealfechaLegada.isVisible = false
            }
            if (dataclassServiciosHistorial.metodoEntrega.isNullOrEmpty()) {
                binding.linealMetodoEntrega.isVisible = false
            }


        }

        private fun agregarPAgo(dataclassServiciosHistorial: dataclassServiciosHistorial) {
            val vista =
                Intent(itemView.context, agregarPagoComprareservaTiendas::class.java).apply {
                    putExtra("idTienda", dataclassServiciosHistorial.idTienda)
                    putExtra("codigoPedido", dataclassServiciosHistorial.codeReserva)
                    putExtra("metodoPago", dataclassServiciosHistorial.metodoPago)
                }
            itemView.context.startActivity(vista)
        }

        private fun obtenerDatosTienda(
            idTienda: String,
            callback: (nombreTienda: String?, tipoTienda: String?, localidad: String?, ImgTienda: String?) -> Unit,
        ) {
            val db = FirebaseFirestore.getInstance().collection("Tiendas").document(idTienda)
            db.get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        val nombreTienda = document.getString("nombre")
                        val tipoTienda = document.getString("tipoTienda")
                        val localidad = document.getString("localidad")
                        val imgPerfil = document.getString("imgPerfil")
                        callback(nombreTienda, tipoTienda, localidad, imgPerfil)
                    } else {
                        callback(
                            null,
                            null,
                            null,
                            null
                        )
                    }
                }
                .addOnFailureListener { exception ->
                    callback(null, null, null, null)
                    Log.e(TAG, "Error al obtener datos de la tienda", exception)
                }
        }


        private fun obtnerImagenServicio(
            idServicios: String,
            idTienda: String,
            tipo: String,
        ) {
            Log.d("tipoObtenido", tipo)
            when (tipo) {
                "reserva promo" -> {
                    obtenerServicioItem(
                        binding.tituloItem,
                        binding.precioItem,
                        binding.imgRerseva,
                        itemView.context,
                        idServicios,
                        idTienda,
                        "promociones", "titulo", "imagenUrl"
                    )

                }

                "Reserva Servicios" -> {
                    obtenerServicioItem(
                        binding.tituloItem,
                        binding.precioItem,
                        binding.imgRerseva,
                        itemView.context,
                        idServicios,
                        idTienda,
                        "servicios", "nombre", "UrlImg"
                    )

                }

                "reserva articulos" -> {
                    obtenerServicioItem(
                        binding.tituloItem,
                        binding.precioItem,
                        binding.imgRerseva,
                        itemView.context,
                        idServicios,
                        idTienda,
                        "articulos", "nombreArticulo", "imgArticulo"
                    )


                }
            }

        }
    }


    private fun obtenerServicioItem(
        tituloTXT: TextView,
        precioTXT: TextView,
        imgShape: ShapeableImageView,
        context: Context,
        idServicios: String,
        idTienda: String,
        collection2: String,
        pNombre: String,
        pImg: String,
    ) {

        Log.d("obtnemosDatosEnviados", " $idTienda $idServicios $collection2")
        val db = FirebaseFirestore.getInstance().collection("Tiendas").document(idTienda)
            .collection(collection2).document(idServicios)
        db.get().addOnSuccessListener { res ->
            if (res.exists()) {
                val data = res.data
                val img = data?.get(pImg) as? String ?: ""
                val titulo = data?.get(pNombre) as? String ?: ""
                val precio = data?.get("precio") as? String ?: ""
                precioTXT.text = precio
                tituloTXT.text = titulo
                try {
                    Glide.with(context)
                        .load(img)
                        .into(imgShape)
                } catch (e: Exception) {
                    println("error al setear la img")
                }
            }
        }.addOnFailureListener { e ->
            println("error al obtener los datos ")
        }
    }

}