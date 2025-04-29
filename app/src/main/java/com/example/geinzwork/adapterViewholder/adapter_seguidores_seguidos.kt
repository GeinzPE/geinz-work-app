package com.example.geinzwork.adapterViewholder

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.geinzwork.constantesGeneral.Variables
import com.example.geinzwork.constantesGeneral.constatnes_carga_imagenes_general
import com.example.geinzwork.dataclass.dataclass_seguidores_seguidos
import com.geinzz.geinzwork.R
import com.geinzz.geinzwork.databinding.ItemCargaSeguidoresSeguidosBinding
import com.geinzz.geinzwork.dataclass.dataClassTrabajosd
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import de.hdodenhof.circleimageview.CircleImageView

class adapter_seguidores_seguidos(
    private val lista: MutableList<dataclass_seguidores_seguidos>,
    private val listener: (dataclass_seguidores_seguidos) -> Unit
) :
    RecyclerView.Adapter<adapter_seguidores_seguidos.viewholderSeguidores_Seguidos>() {
    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): viewholderSeguidores_Seguidos {
        val binding = ItemCargaSeguidoresSeguidosBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return viewholderSeguidores_Seguidos(binding)
    }

    override fun getItemCount(): Int = lista.size

    override fun onBindViewHolder(holder: viewholderSeguidores_Seguidos, position: Int) {
        val item = lista[position]
        holder.render(item)

    }

    inner class viewholderSeguidores_Seguidos(private val binding: ItemCargaSeguidoresSeguidosBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun render(item: dataclass_seguidores_seguidos) {
            binding.categoriaTrabajo.text = item.tipo_trabajado
            binding.nombreUser.text = item.nombre_trabajador
            binding.nacionalidadUser.text = item.nacionalidad
            if (item.verificado == true) {
                binding.verificadoIcono.isVisible = true
            } else {
                binding.verificadoIcono.isVisible = false
            }
            val drawable = ContextCompat.getDrawable(itemView.context, R.drawable.img_perfil)
            constatnes_carga_imagenes_general.changer_img(
                binding.progreesCarga,
                itemView.context,
                item.img_perfil.toString(),
                binding.imgPerfil,
                null,
                "perfil", drawable
            ) { cargado ->
            }
            when (item.nacionalidad) {
                Variables.Peruano -> {
                    try {
                        Glide.with(itemView.context)
                            .load(R.drawable.bandera_peru)
                            .into(binding.banderaNacionalidad)
                    } catch (e: Exception) {
                        println(e)
                    }

                }

                Variables.Venezolano -> {
                    try {
                        Glide.with(itemView.context)
                            .load(R.drawable.bandera_venezolana)
                            .into(binding.banderaNacionalidad)
                    } catch (e: Exception) {
                        println(e)
                    }
                }
            }
            binding.listener.setOnClickListener {
                listener(item)
            }
            firebaseAuth=FirebaseAuth.getInstance()
            verificar_seguimiento(binding, item.id_trabajador.toString(), firebaseAuth.uid.toString())

        }

    }

    private fun verificar_seguimiento(
        binding: ItemCargaSeguidoresSeguidosBinding,
        id_trabajadores: String,
        id_user_registrado: String
    ) {
        val db = FirebaseFirestore.getInstance().collection("Trabajadores_Usuarios_Drivers")
            .document("trabajadores").collection("trabajadores").document(id_trabajadores)
            .collection("seguidores")

        db.get().addOnSuccessListener { res ->
            for (datos in res) {
                if(datos.exists()){
                    val data = datos.data
                    val id = data?.get("id") as? String ?: ""
                    if (id == id_user_registrado) {
                        binding.seguir.text = "deja de seguir"
                    } else {
                        binding.seguir.text = "seguir"
                    }
                }
            }
        }.addOnFailureListener { e ->
            println("no se econtro el usuario")
        }
    }
}