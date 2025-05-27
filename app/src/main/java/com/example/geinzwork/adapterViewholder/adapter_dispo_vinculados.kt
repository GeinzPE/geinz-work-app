package com.example.geinzwork.adapterViewholder

import android.os.Build
import android.text.SpannableString
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.example.geinzwork.dataclass.dataclass_dispo_vinculados
import com.geinzz.geinzwork.R
import com.geinzz.geinzwork.constantesGeneral.constantestextos_general
import com.geinzz.geinzwork.databinding.LayoutDispoVinculadoBinding

class adapter_dispo_vinculados(
    private val lista_dispo: MutableList<dataclass_dispo_vinculados>,
    private val eliminar: (dataclass_dispo_vinculados) -> Unit
) :
    RecyclerView.Adapter<adapter_dispo_vinculados.viewholderDispo>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): viewholderDispo {
        val binding =
            LayoutDispoVinculadoBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return viewholderDispo(binding)

    }

    override fun getItemCount(): Int = lista_dispo.size

    override fun onBindViewHolder(holder: viewholderDispo, position: Int) {
        val item = lista_dispo[position]
        holder.render(item, eliminar)
    }

    class viewholderDispo(private val binding: LayoutDispoVinculadoBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun render(
            item: dataclass_dispo_vinculados,
            eliminar: (dataclass_dispo_vinculados) -> Unit
        ) {
            val dispo = "${Build.MANUFACTURER} ${Build.MODEL}"
            if(dispo==item.nombre_dispo){
                binding.dispoActual.isVisible=true
            }else{
                binding.dispoActual.isVisible=false
            }

            binding.listener.setOnLongClickListener {
                eliminar(item)
                true
            }
            val dispositivo =
                SpannableString("Dispositivo : ${item.nombre_dispo}")
            constantestextos_general.setearInformacionboldDescripcion(
                "Dispositivo",
                dispositivo, binding.dispositivo
            )
            val hora =
                SpannableString("Hora registrado : ${item.hora}")
            constantestextos_general.setearInformacionboldDescripcion(
                "Dispositivo",
                hora, binding.hora
            )
            val fecha =
                SpannableString("Fecha registrado : ${item.fecha}")
            constantestextos_general.setearInformacionboldDescripcion(
                "Dispositivo",
                fecha, binding.fecha
            )




            if(item.ultima_hora.toString().isNotEmpty()){
                binding.ultimaHora.isVisible=true
                val ULTHora =
                    SpannableString("Ultima hora de conexion : ${item.ultima_hora}")
                constantestextos_general.setearInformacionboldDescripcion(
                    "Ultima hora de conexion",
                    ULTHora, binding.ultimaHora
                )
            }else{
                binding.ultimaHora.isVisible=false
            }

            if(item.ultima_fecha.toString().isNotEmpty()){
                binding.ultimaFecha.isVisible=true
                val fechaULT =
                    SpannableString("Ultima fecha de conexion : ${item.ultima_fecha}")
                constantestextos_general.setearInformacionboldDescripcion(
                    "Ultima fecha de conexion",
                    fechaULT, binding.ultimaFecha
                )
            }else{
                binding.ultimaFecha.isVisible=false
            }


            val priamrio =
                SpannableString("Primario : SI")
            val priamrio2 =
                SpannableString("Primario : NO")
            if(item.priamrio==true){
                constantestextos_general.setearInformacionboldDescripcion(
                    "Primario",
                    priamrio,   binding.dispositivoPrimario
                )
            }else{
                constantestextos_general.setearInformacionboldDescripcion(
                    "Primario",
                    priamrio2,   binding.dispositivoPrimario
                )

            }

            when (item.marca_logo) {
                "Samsung" -> binding.marcaLogo.setImageResource(R.drawable.samsung_logo)
                "Xiaomi" -> binding.marcaLogo.setImageResource(R.drawable.xiaomi_logo)
                "Realme" -> binding.marcaLogo.setImageResource(R.drawable.xiaomi_logo)
                "Oppo" -> binding.marcaLogo.setImageResource(R.drawable.oppo_logo)
                "Motorola" -> binding.marcaLogo.setImageResource(R.drawable.motorola_logo)
                "Google" -> binding.marcaLogo.setImageResource(R.drawable.google_logo)
                "Sony" -> binding.marcaLogo.setImageResource(R.drawable.sony_logo)
                "Asus" -> binding.marcaLogo.setImageResource(R.drawable.asus_logo)
                "LG" -> binding.marcaLogo.setImageResource(R.drawable.lg_logo)
                "Infinix" -> binding.marcaLogo.setImageResource(R.drawable.marca_desconocida)
                "Tecno" -> binding.marcaLogo.setImageResource(R.drawable.tecno_logo)
                else -> binding.marcaLogo.setImageResource(R.drawable.marca_desconocida)
            }

        }
    }
}