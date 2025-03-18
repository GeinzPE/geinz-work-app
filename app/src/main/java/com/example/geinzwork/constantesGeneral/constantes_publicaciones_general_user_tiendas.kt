package com.geinzz.geinzwork.constantesGeneral

import android.annotation.SuppressLint
import android.content.Context
import android.widget.LinearLayout

import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.geinzwork.constantesGeneral.Variables
import com.geinzz.geinzwork.dataclass.dataclas_trabajos_ralizados
import com.google.android.material.appbar.AppBarLayout
import com.google.firebase.firestore.FirebaseFirestore

object constantes_publicaciones_general_user_tiendas {

    @SuppressLint("SuspiciousIndentation")
    fun obtenerPublicaciones(
        plan: String,
        id: String,
        lista: MutableList<dataclas_trabajos_ralizados>,
        recicleTrabajosRealizados: RecyclerView,
        context: Context,
        adapter: RecyclerView.Adapter<*>,
        linealappLayout: AppBarLayout,
        lineal_no_cuenta: LinearLayout,
        linealTrabajosTralizados: LinearLayout
    ) {
        if (plan == Variables.plaA) {
            lineal_no_cuenta.isVisible = false
            return
        }

        if (plan == Variables.planB || plan == Variables.PlanC) {
            val db = FirebaseFirestore.getInstance().collection(Variables.trabajadores_usuariosDB)
                .document(Variables.trabajadoresDB).collection(Variables.trabajadoresDB)
                .document(id)
                .collection(Variables.trabajos_realizados)

            lineal_no_cuenta.isVisible = false
            lista.clear()

            db.get().addOnSuccessListener { res ->
                val listaTemporal = mutableListOf<dataclas_trabajos_ralizados>()
                for (datos in res) {
                    val data = datos.data
                    val trabajoRealizado = dataclas_trabajos_ralizados(
                        data?.get(Variables.imageUrl) as? String ?: "",
                        data?.get(Variables.titulo) as? String ?: "",
                        data?.get(Variables.descripcion) as? String ?: "",
                        data?.get(Variables.fecha) as? String ?: "",
                        data?.get(Variables.hora) as? String ?: "",
                        data?.get(Variables.id) as? String ?: ""
                    )
                    listaTemporal.add(trabajoRealizado)
                }

                // Mezclar los elementos de la lista de manera aleatoria
                listaTemporal.shuffle()
                // Tomar hasta 5 elementos aleatorios
                lista.addAll(listaTemporal.take(5))

                if (lista.isEmpty()) {
                    lineal_no_cuenta.isVisible = true
                    linealTrabajosTralizados.isVisible = false
                } else {
                    lineal_no_cuenta.isVisible = false
                    linealTrabajosTralizados.isVisible = true
                    inicializarRecicle(recicleTrabajosRealizados, adapter, context)
                    adapter.notifyDataSetChanged() // Notificar cambios en la lista
                }
            }
        }
    }


    private fun inicializarRecicle(
        recycle: RecyclerView,
        adapter: RecyclerView.Adapter<*>,
        context: Context
    ) {
        recycle.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        recycle.adapter = adapter
    }


}