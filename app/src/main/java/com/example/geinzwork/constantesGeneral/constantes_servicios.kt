package com.geinzz.geinzwork.constantesGeneral

import android.widget.ImageView
import androidx.core.view.isVisible
import com.example.geinzwork.constantesGeneral.Variables
import com.google.firebase.firestore.FirebaseFirestore

object constantes_servicios {

    fun verificarEstado_vericiacion(verificado: ImageView, id: String,verificadoBoll:(Boolean,String)->Unit) {
        val db = FirebaseFirestore.getInstance().collection(Variables.solicitud_servicios)
            .document(Variables.verificaiones).collection(Variables.activos).document(id)
        db.get().addOnSuccessListener { res ->
            if (res.exists()) {
                val data = res.data
                val estado = data?.get(Variables.estado) as? Boolean
                val plan = data?.get(Variables.plan) as? String?:""

                if (estado != false) {
                    verificado.isVisible = true
                    verificadoBoll(true,plan)
                } else {
                    verificado.isVisible = false
                    verificadoBoll(false,"")
                }
            } else {
                verificado.isVisible = false
                verificadoBoll(false,"")
            }
        }
            .addOnFailureListener {
                println("No se encontro la verificacion del usuario")
            }
    }


}