package com.example.geinzwork.adapterViewholder

import android.app.Activity
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.geinzwork.DiffUtilClass.difiutl_adapter_seguir_trabajadores
import com.example.geinzwork.constantesGeneral.Variables
import com.example.geinzwork.constantesGeneral.constantes_trabajadores_info
import com.example.geinzwork.constantesGeneral.constatnes_carga_imagenes_general
import com.example.geinzwork.dataclass.dataClasSeguirTrabajdores_info
import com.geinzz.geinzwork.CuentaFreelancer
import com.geinzz.geinzwork.R
import com.geinzz.geinzwork.adapterViewholder.adapter
import com.geinzz.geinzwork.constantesGeneral.constantes
import com.geinzz.geinzwork.databinding.FragmentInfoBinding
import com.geinzz.geinzwork.databinding.ItemMasTrabajadoresSugeridosBinding
import com.google.android.material.progressindicator.CircularProgressIndicator
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions

class adapter_seguirTrabajadores_info(
    private var finish: Boolean,
    private var lista: List<dataClasSeguirTrabajdores_info>,
    private var bindingFragmentInfoBinding:FragmentInfoBinding


    ) : RecyclerView.Adapter<adapter_seguirTrabajadores_info.viewHolderseguirTrabajador>() {

    private lateinit var firebaseAuth: FirebaseAuth

    fun actualizarLista(newList: List<dataClasSeguirTrabajdores_info>) {
        val diffUtil = difiutl_adapter_seguir_trabajadores(lista, newList)
        val result = DiffUtil.calculateDiff(diffUtil)
        lista = newList
        result.dispatchUpdatesTo(this)
        Log.d("ListaTrabajadores", "Quedan ${lista.size} trabajadores en la lista")
        if (lista.size == 0){
            bindingFragmentInfoBinding.noSeEncontraronTrabajadores.isVisible=true
            bindingFragmentInfoBinding.trabajadoresSimilares.isVisible=false
        }else{
            bindingFragmentInfoBinding.noSeEncontraronTrabajadores.isVisible=false
            bindingFragmentInfoBinding.trabajadoresSimilares.isVisible=true
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): viewHolderseguirTrabajador {
        val binding = ItemMasTrabajadoresSugeridosBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return viewHolderseguirTrabajador(binding)

    }

    override fun getItemCount(): Int {
        return lista.size
    }

    override fun onBindViewHolder(holder: viewHolderseguirTrabajador, position: Int) {
        val item = lista[position]
        holder.render(item)
    }

    inner class viewHolderseguirTrabajador(private val binding: ItemMasTrabajadoresSugeridosBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun render(item: dataClasSeguirTrabajdores_info) {
            val placeholderDrawable =
                ContextCompat.getDrawable(itemView.context, R.drawable.img_perfil)

            constatnes_carga_imagenes_general.changer_img(
                binding.CargaIMG,
                itemView.context,
                item.img_perfi.toString(),
                binding.perfilUserImg,
                null,
                "perfil",
                placeholderDrawable
            ) {}
            if (
                !binding.nombreUSer.text.isNullOrBlank() &&
                !binding.localidad.text.isNullOrBlank() &&
                !binding.nacionalidad.text.isNullOrBlank() &&
                !binding.categoria.text.isNullOrBlank() &&
                !binding.subcategoria.text.isNullOrBlank()
            ) {
                binding.cargandoContenido.isVisible = false
                binding.linealCamposTrabajador.isVisible = true
            }

            binding.nombreUSer.text = item.nombreTrabajador
            binding.localidad.text = item.localida
            binding.nacionalidad.text = item.nacionalidad
            binding.categoria.text = item.categoria
            binding.subcategoria.text = item.subcategoria
            binding.cargandoContenido.isVisible = false
            binding.linealCamposTrabajador.isVisible = true



            if (item.verificado == true) {
                binding.verificado.isVisible = true
            } else {
                binding.verificado.isVisible = false
            }
            binding.linealCamposTrabajador.setOnClickListener {

                val vista = Intent(
                    itemView.context,
                    com.geinzz.geinzwork.vistaTrabajador.vistaTrabajador::class.java
                )
                vista.putExtra(Variables.id, item.id)
                vista.putExtra(Variables.imagenPerfil, item.img_perfi)
                vista.putExtra(Variables.nombreUSer, item.nombreTrabajador)
                vista.putExtra(Variables.nacionalidad, item.nacionalidad)
                vista.putExtra(Variables.categoria, item.categoria)
                itemView.context.startActivity(vista)

                if (finish) {
                    (itemView.context as? Activity)?.finish()
                }

            }

            binding.follow.setOnClickListener {
                // Obtener el ID del trabajador actual
                val idTrabajadorActual = item.id

                // Ejecutar función para seguir
                Folllow(idTrabajadorActual.toString())

            }



        }

        private fun Folllow(idTrabajadorActual: String) {
            firebaseAuth = FirebaseAuth.getInstance()
            if (firebaseAuth.currentUser == null) {
                val builder = AlertDialog.Builder(itemView.context)
                builder.setTitle("No estás registrado en Geinz Work")
                builder.setMessage("Regístrate en Geinz Work para que puedas seguir.")
                builder.setPositiveButton("Cuenta Simple") { dialog, _ ->
                    // Mostrar diálogo de carga y redirigir a la pantalla de registro
                    constantes.showLoadingDialog(
                        itemView.context,
                        2000,
                        "Cargando información",
                        "Espere un momento..."
                    )
                    val intent = Intent(itemView.context, CuentaFreelancer::class.java).apply {
                        putExtra("tipoCuenta", "cuentaSimple")
                        putExtra("Title", "Cuenta Simple")
                        putExtra("pasos", "Estás a 1/2 pasos")
                    }
                    itemView.context.startActivity(intent)
                    dialog.dismiss()
                }
                builder.setNegativeButton("Cuenta Trabajador") { dialog, _ ->
                    val intent = Intent(itemView.context, CuentaFreelancer::class.java).apply {
                        putExtra("tipoCuenta", "cuentaTrabajador")
                        putExtra("Title", "Cuenta Freelancer")
                        putExtra("pasos", "Estás a 1/5 pasos")
                    }
                    itemView.context.startActivity(intent)
                    dialog.dismiss()
                }
                builder.create().show()
            } else {
                val db = FirebaseFirestore.getInstance().collection("Trabajadores_Usuarios_Drivers")
                    .document("tokens").collection("tokens").document(firebaseAuth.uid.toString())
                db.get().addOnSuccessListener { res ->
                    if (res.exists()) {
                        val data = res.data
                        val token = data?.get("token") as? String ?: ""
                        AgregarCollectionFollow(token, firebaseAuth.uid.toString(), idTrabajadorActual)
                        // Quitar al trabajador seguido de la lista y actualizar
                        val nuevaLista = lista.toMutableList()
                        nuevaLista.removeAt(adapterPosition)  // elimina el item actual
                        actualizarLista(nuevaLista)
                    } else {
                        println("no se encontro el token del usuario")
                    }
                }
            }

        }

        private fun AgregarCollectionFollow(
            token: String,
            iduserActual: String,
            idTrabajadorActual: String,
        ) {
            val db = FirebaseFirestore.getInstance().collection("Trabajadores_Usuarios_Drivers")
                .document("trabajadores").collection("trabajadores").document(idTrabajadorActual)
                .collection("seguidores").document(iduserActual)
            val hashMap = hashMapOf<String, Any>(
                "id" to iduserActual,
                "token" to token
            )
            db.set(hashMap, SetOptions.merge()).addOnSuccessListener {
                println("Seguido correctamente")

            }.addOnFailureListener {
                println("Error al seguir")
            }
        }

    }

}