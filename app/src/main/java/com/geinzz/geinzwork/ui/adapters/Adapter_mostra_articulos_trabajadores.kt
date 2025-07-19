package com.geinzz.geinzwork.ui.adapters


import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.geinzz.geinzwork.utils.constantes.constantes.constatnes_carga_imagenes_general
import com.geinzz.geinzwork.model.dataclas_item_preview_art_comprar
import com.geinzz.geinzwork.utils.constantes.constantes.constantes_publicaciones_general_user_tiendas.obtener_metodoEntrega
import com.geinzz.geinzwork.utils.constantes.constantes.constantestextos_general
import com.geinzz.geinzwork.databinding.ItemProductosUsuariosVerificadosBinding

class adapter_mostra_articulos_trabajadores(
    private val lista: MutableList<dataclas_item_preview_art_comprar>,
    private val listener: (dataclas_item_preview_art_comprar) -> Unit
) :
    RecyclerView.Adapter<adapter_mostra_articulos_trabajadores.viewholderMostrarArticulos>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): viewholderMostrarArticulos {
        val binding = ItemProductosUsuariosVerificadosBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return viewholderMostrarArticulos(binding)
    }

    override fun getItemCount(): Int {
        return if (lista.size >= 5) 5 else lista.size
    }

    override fun onBindViewHolder(holder: viewholderMostrarArticulos, position: Int) {
        val item = lista[position]
        holder.render(item)
    }

    inner class viewholderMostrarArticulos(private val binding: ItemProductosUsuariosVerificadosBinding) :
        RecyclerView.ViewHolder(binding.root) {
        val imgARticulo = binding.imgTrabajo
        val progressVar = binding.progressCargaImagen
        val descuentoPorcentaje = binding.descuentoPorcentaje
        val titulo=binding.nombreProducto
        val precioProducto=binding.precioProducto
        val precioDescuento=binding.precioDescuento
        val carga=binding.cargadoContenido
        val linealCamposTXT=binding.linealCargandoDatos
        fun render(item: dataclas_item_preview_art_comprar) {
            obtener_metodoEntrega(
                item.id_trabajador.toString(), item.metodo_entrega.toString(),
                callback = { metodo_entrega ->
                },
                evio_gratis = { delivery_gratis ->
                    if (delivery_gratis) {
                        binding.envioGrtis.isVisible = true
                    } else {
                        binding.envioGrtis.isVisible = false
                    }
                }
            )
            imgARticulo.setOnClickListener { listener(item) }
            constatnes_carga_imagenes_general.changer_img(
                progressVar,
                itemView.context,
                item.img.toString(),
                null,
                imgARticulo,
                "portada", null
            ) { completado ->

            }
            Handler(Looper.getMainLooper()).postDelayed({
                carga.isVisible = false
                linealCamposTXT.isVisible=true
            }, 2000)

            titulo.text=item.titulo
            if (item.descuentoBoolena == true) {
                descuentoPorcentaje.isVisible = true
                descuentoPorcentaje.text = "-${item.descuentoCantidad.toString()}%"
                precioDescuento.isVisible=true
                precioDescuento.text=item.descuentoCantidad.toString()
                constantestextos_general.marcarDescuentoTxt(precioDescuento)
                constantestextos_general.setearPrecioDescuentoPrecioAntiguo(item.cantidad,precioProducto)
                constantestextos_general.setearPrecioDescuentoPrecioAntiguo(null,null,item.precio,precioDescuento)
            } else {
                constantestextos_general.setearPrecioDescuentoPrecioAntiguo(item.precio,precioProducto)
                descuentoPorcentaje.isVisible = false
                precioDescuento.isVisible=false
            }

        }


    }
}