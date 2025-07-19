package com.geinzz.geinzwork.utils.constantes.constantes

import android.R
import android.content.Context
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.TextView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.firestore.FirebaseFirestore
import java.text.DecimalFormat

object constantes_cotizacion_driver {

    fun obtenerCostoDelivery(
        longUser: TextView,
        latUSer: TextView,
        idTienda: String,
        estandar: (Int) -> Unit,
        express: (Int) -> Unit,
    ) {
        val db = FirebaseDatabase.getInstance().getReference("costoDelivery")

        db.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    val costoEstandar = dataSnapshot.child("estandar").getValue(Int::class.java)
                    val costoExpress = dataSnapshot.child("express").getValue(Int::class.java)

                    if (costoEstandar != null && costoExpress != null) {
                        val tiendaRef =
                            FirebaseFirestore.getInstance().collection("Tiendas").document(idTienda)
                        tiendaRef.get().addOnSuccessListener { document ->
                            if (document.exists()) {
                                val latitudTienda = document.getDouble("latitud")
                                val longitudTienda = document.getDouble("longitud")
                                val latitudUser = latUSer.text.toString().toDoubleOrNull()
                                val longitudUser = longUser.text.toString().toDoubleOrNull()

                                if (latitudTienda != null && longitudTienda != null && latitudUser != null && longitudUser != null) {
                                    val distanciaIda = calcularDistancia(
                                        latitudTienda,
                                        longitudTienda,
                                        latitudUser,
                                        longitudUser
                                    )

                                    val tiempoEstimado = 10.0

                                    val costoTotalEstandar = calcularCosto(
                                        distanciaIda,
                                        costoEstandar.toDouble(),
                                        tiempoEstimado
                                    )
                                    val costoTotalExpress = calcularCosto(
                                        distanciaIda,
                                        costoExpress.toDouble(),
                                        tiempoEstimado
                                    )

                                    estandar(costoTotalEstandar.toInt())
                                    express(costoTotalExpress.toInt())
                                } else {
                                    println("Error: La latitud o longitud de la tienda o usuario es nula.")
                                }
                            } else {
                                println("Error: No se encontró el documento de la tienda con ID $idTienda.")
                            }
                        }.addOnFailureListener { e ->
                            println("Error al obtener la tienda desde Firestore: $e")
                        }
                    } else {
                        println("No se encontraron valores de costos de delivery.")
                    }
                } else {
                    println("No se encontraron costos de delivery en la base de datos.")
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                println("Error al leer la base de datos: ${databaseError.message}")
            }
        })
    }

    private fun calcularDistancia(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val radioTierra = 6371.0
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                Math.sin(dLon / 2) * Math.sin(dLon / 2)
        val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
        return radioTierra * c
    }

    private fun calcularCosto(
        distancia: Double,
        costoPorKilometro: Double,
        tiempoEstimado: Double,
    ): Int {
        val costoDistancia = distancia * costoPorKilometro
        val costoTiempo = tiempoEstimado * 0.5
        return (costoDistancia + costoTiempo).toInt()
    }


    fun setearDelivery(deliveryAutoComplete: AutoCompleteTextView, context: Context) {
        val opcionesDelivery = listOf("Delivery estandar(GEINZ)", "Delivery express(GEINZ)")
        val adapter =
            ArrayAdapter(context, R.layout.simple_dropdown_item_1line, opcionesDelivery)
        deliveryAutoComplete.setAdapter(adapter)
    }

    fun formatearNumeros(n1: String): String {
        val number = n1.replace(",", "").toLongOrNull() ?: return n1
        val formatter = DecimalFormat("#,###")
        val formattedNumber = formatter.format(number)
        return formattedNumber
    }

}