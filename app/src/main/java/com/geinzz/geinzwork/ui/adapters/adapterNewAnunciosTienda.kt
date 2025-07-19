package com.geinzz.geinzwork.ui.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.RelativeLayout
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.airbnb.lottie.LottieAnimationView
import com.bumptech.glide.Glide
import com.geinzz.geinzwork.R
import com.geinzz.geinzwork.databinding.ItemNewNoticiasTiendasBinding
import com.geinzz.geinzwork.model.dataclassPromociones
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

class adapterNewAnunciosTienda(
    private val with: String,
    private val ListaAnuncios: MutableList<dataclassPromociones>,
    private val ver_mas: (dataclassPromociones) -> Unit,
    private val idUser: String,
    private val mostrarTodo: Boolean
) : RecyclerView.Adapter<adapterNewAnunciosTienda.viewHolder>() {
    private lateinit var firebaseAuth: FirebaseAuth
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): viewHolder {
        val binding=ItemNewNoticiasTiendasBinding.inflate(LayoutInflater.from(parent.context),parent,false)
       return viewHolder(binding)
    }

    override fun getItemCount(): Int {
        return if (mostrarTodo) ListaAnuncios.size else if (ListaAnuncios.size >= 5) 5 else ListaAnuncios.size
    }

    override fun onBindViewHolder(holder: viewHolder, position: Int) {
        val item = ListaAnuncios[position]
        holder.render(with,item, ver_mas, idUser)

    }

    inner class viewHolder(private val binding: ItemNewNoticiasTiendasBinding) : RecyclerView.ViewHolder(binding.root) {
        val imgPrincipal = binding.imgPrincipal
        val titulo = binding.titulo
        val vencimineto = binding.vencimineto
        val guardar = binding.save
        val listener = binding.linealListner
        val layout_width=binding.relative
        var likeAniation = false
        fun render(
            with:String,
            dataclassPromociones: dataclassPromociones,
            ver_mas: (dataclassPromociones) -> Unit,
            idUser: String
        ) {

            firebaseAuth = FirebaseAuth.getInstance()
            titulo.text = dataclassPromociones.tituloPromo
            vencimineto.text = dataclassPromociones.fecha
            obtenerLikeados(firebaseAuth.uid.toString(), itemView.context, dataclassPromociones)

            try {
                Glide.with(itemView.context)
                    .load(dataclassPromociones.img)
                    .into(imgPrincipal)
            } catch (e: Exception) {
                println("ocurrio un error al setear la img")
            }
            listener.setOnClickListener {
                ver_mas(dataclassPromociones)
            }
            guardar.setOnClickListener {
                likeAniation = guardar(guardar, R.raw.save_animation, likeAniation, firebaseAuth.uid.toString(), dataclassPromociones)
            }
            val params = layout_width.layoutParams
            when(with){
                "mediano"->{
                    val density = itemView.context.resources.displayMetrics.density
                    params.width = (250 * density).toInt()
                    binding.relative.layoutParams = params

                }
                "completa"->{
                    params.width = RelativeLayout.LayoutParams.MATCH_PARENT
                    layout_width.layoutParams = params
                }
            }

        }
        fun guardar(
            imageView: LottieAnimationView,
            aniamtion: Int,
            like: Boolean,
            idUser: String,
            dataclassPromociones: dataclassPromociones
        ): Boolean {
            if (!like) {
                imageView.setAnimation(aniamtion)
                imageView.playAnimation()
                retornarid(dataclassPromociones)

            } else {
                eliminarCorazonAnuncio(idUser, dataclassPromociones.id.toString())
                imageView.setImageResource(R.drawable.save_icon)
            }
            return !like
        }

        fun eliminarCorazonAnuncio(idUser: String, idPublicaion: String) {
            val db =
                FirebaseFirestore.getInstance().collection("NoticiasGuardadas").document(idUser)
            val updates = hashMapOf<String, Any>(
                idPublicaion to FieldValue.delete()
            )
            db.update(updates)
                .addOnSuccessListener {
                    println("Campo eliminado exitosamente $idPublicaion")
                }
                .addOnFailureListener { e ->
                    println("Error al eliminar campo: $e")
                }

        }

        fun retornarid(dataclassPromociones: dataclassPromociones) {
            val idPublicaion = dataclassPromociones.id
            println("id pulbicacion likead $idPublicaion")
            firebaseAuth = FirebaseAuth.getInstance()
            println("el id del usuario es  ${firebaseAuth.uid.toString()}")


        }
        fun obtenerLikeados(
            idUser: String,
            context: Context,
            dataclassPromociones: dataclassPromociones
        ) {
            val db =
                FirebaseFirestore.getInstance().collection("NoticiasGuardadas").document(idUser)
            db.get()
                .addOnSuccessListener { document ->
                    if (document != null && document.exists()) {
                        val fields = document.data
                        if (fields != null) {
                            for ((key, value) in fields) {
                                println("ID de publicación: $key")
                                if (key == dataclassPromociones.id.toString()) {
                                    guardar.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.save_icon))
                                    likeAniation = guardar(
                                        guardar,
                                        R.raw.save_animation,
                                        false,
                                        idUser,
                                        dataclassPromociones
                                    )
                                    retornarid(dataclassPromociones)
                                }
                            }
                        }
                    } else {
                        println("No se encontraron datos para el usuario: $idUser")
                    }
                }
                .addOnFailureListener { exception ->
                    println("Error al obtener datos: ${exception.message}")
                }
        }
    }

}