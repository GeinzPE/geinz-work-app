package com.geinzz.geinzwork.utils.constantes.constantes

import android.content.Context
import android.content.Intent
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.geinzz.geinzwork.CuentaFreelancer
import com.geinzz.geinzwork.model.dataclassTiendas
import com.geinzz.geinzwork.model.dataclass_item_general_tienda
import com.geinzz.geinzwork.vistaTiendas.VistaTienda
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.firestore.FirebaseFirestore

object constantes2 {

    val firebaseAuth = FirebaseAuth.getInstance()

    fun seguirTienda(dataclassTiendas: dataclassTiendas, context: Context) {
        if (constantesReviewComplet.firebaseAuth.currentUser == null) {
            val builder = AlertDialog.Builder(context)
            builder.setTitle("No estás registrado en Geinz Work")
            builder.setMessage("Regístrate en Geinz Work para que puedas seguir")
            builder.setPositiveButton("Cuenta Simple") { dialog, which ->
                constantes.showLoadingDialog(
                    context,
                    2000,
                    "Cargando informacion",
                    "Espere un momento..."
                )
                val intent = Intent(context, CuentaFreelancer::class.java)
                val tipoCuenta = "cuentaSimple"
                val title = "Cuenta Simple"
                val pasos = "Estas a 1/2 pasos"
                intent.putExtra("tipoCuenta", tipoCuenta)
                intent.putExtra("Title", title)
                intent.putExtra("pasos", pasos)
                context.startActivity(intent)
                dialog.dismiss()
            }
            builder.setNegativeButton("Cuenta Trabajador") { dialog, which ->
                val intent = Intent(context, CuentaFreelancer::class.java)
                val tipoCuenta = "cuentaTrabajador"
                val title = "Cuenta Freelancer"
                val pasos = "Estas a 1/5 pasos"
                intent.putExtra("tipoCuenta", tipoCuenta)
                intent.putExtra("Title", title)
                intent.putExtra("pasos", pasos)
                context.startActivity(intent)
                dialog.dismiss()
            }
            val dialog = builder.create()
            dialog.show()
        } else {
            val Realtime = FirebaseDatabase.getInstance().getReference("segidores")
                .child(dataclassTiendas.id.toString()).child(firebaseAuth.uid.toString())
            val hashMap = hashMapOf<String, Any>(
                "id" to firebaseAuth.uid.toString()
            )
            Realtime.setValue(hashMap)
                .addOnSuccessListener {
                    Toast.makeText(context, "segido correctamente", Toast.LENGTH_SHORT).show()
                    val db = FirebaseFirestore.getInstance().collection(Variables.collection_Tiendas)
                        .document(dataclassTiendas.id.toString())
                    db.get()
                        .addOnSuccessListener { res ->
                            if (res.exists()) {
                                val data = res.data
                                val seguidores = data?.get(Variables.collection_seguidores_tiendas) as? String
                                if (seguidores != null || seguidores != "") {
                                    val seguidoresint = seguidores!!.toInt()
                                    val total = (seguidoresint + 1).toString()
                                    val hashMap = hashMapOf<String, Any>(
                                        Variables.collection_seguidores_tiendas to total
                                    )
                                    db.update(hashMap)
                                        .addOnSuccessListener {
                                            Toast.makeText(
                                                context,
                                                "campo Acualizado correctamente desde firestore",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                        .addOnFailureListener {
                                            Toast.makeText(
                                                context,
                                                "error al actualizar firestore",
                                                Toast.LENGTH_SHORT
                                            ).show()

                                        }
                                } else {
                                    Toast.makeText(
                                        context,
                                        "El campo es nulo",
                                        Toast.LENGTH_SHORT
                                    ).show()


                                }

                            }
                        }
                }
                .addOnFailureListener {
                    Toast.makeText(context, "error al agregar el usuario", Toast.LENGTH_SHORT)
                        .show()
                }
        }

    }

    fun dejarDeSeguir(dataclassTiendas: dataclassTiendas, context: Context) {
        val realTime = FirebaseDatabase.getInstance().getReference("segidores")
            .child(dataclassTiendas.id.toString())
            .child(firebaseAuth.uid.toString())
        realTime.removeValue()
            .addOnSuccessListener {
                Toast.makeText(
                    context,
                    "dejaste de seguir correctamete desde realtime",
                    Toast.LENGTH_SHORT
                ).show()
                val db = FirebaseFirestore.getInstance().collection(Variables.collection_Tiendas)
                    .document(dataclassTiendas.id.toString())
                db.get()
                    .addOnSuccessListener { res ->
                        if (res.exists()) {
                            val data = res.data
                            val seguidores = data?.get(Variables.collection_seguidores_tiendas) as? String
                            val seguidoresint = seguidores!!.toInt()
                            val total = (seguidoresint - 1).toString()
                            val hashMap = hashMapOf<String, Any>(
                                Variables.collection_seguidores_tiendas to total
                            )
                            db.update(hashMap)
                                .addOnSuccessListener {
                                    Toast.makeText(
                                        context,
                                        "campo Acualizado correctamente desde firestore",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                                .addOnFailureListener {
                                    Toast.makeText(
                                        context,
                                        "error al actualizar firestore",
                                        Toast.LENGTH_SHORT
                                    ).show()

                                }
                        }
                    }
            }
    }

    fun verificarSiSige(dataclassTiendas: dataclassTiendas, context: Context, btn: Button) {
        val realtime = FirebaseDatabase.getInstance().getReference("segidores")
            .child(dataclassTiendas.id.toString())

        realtime.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                var isFollowing = false
                for (datos in snapshot.children) {
                    val id = datos.child("id").getValue(String::class.java)
                    if (id == firebaseAuth.uid.toString()) {
                        isFollowing = true
                        break
                    }
                }
                if (isFollowing) {
                    btn.text = "Siguiendo"
                } else {
                    btn.text = "seguir"
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(
                    context,
                    "Error al verificar el estado de seguimiento: ${error.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }


    fun verMasInfoTienda(dataclassTiendas: dataclassTiendas, context: Context) {
        val vista = Intent(context, VistaTienda::class.java)
        vista.putExtra("idTienda", dataclassTiendas.id.toString())
        vista.putExtra("ubicacion", dataclassTiendas.ubicacion.toString())
        vista.putExtra("idUSer", firebaseAuth.uid.toString())
        context.startActivity(vista)
    }

    fun verMasInfoTiendaGeneral(dataclassItemGeneralTienda: dataclass_item_general_tienda, context: Context) {
        val vista = Intent(context, VistaTienda::class.java)
        vista.putExtra("idTienda", dataclassItemGeneralTienda.id.toString())
        vista.putExtra("ubicacion", dataclassItemGeneralTienda.ubicacion.toString())

        context.startActivity(vista)
    }










}