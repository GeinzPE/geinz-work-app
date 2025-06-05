package com.geinzz.geinzwork.constantesGeneral

import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.geinzwork.constantesGeneral.Variables
import com.geinzz.geinzwork.CuentaFreelancer
import com.geinzz.geinzwork.EditarReview
import com.geinzz.geinzwork.adapterViewholder.adaptadorReview
import com.geinzz.geinzwork.dataclass.daclassReview
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions

object constantesReviewComplet {

    val firebaseAuth = FirebaseAuth.getInstance()
    fun verificarSiExisteReview(
        tipoEditado: String,
        idTrabajador: String,
        idUsuario: String,
        context: Context,
        contenidoReview: EditText,
        cantidadEstrellas: EditText,
        hora: TextView,
        fecha: TextView,
        tipoTrabajo: String
    ) {
        if (firebaseAuth.currentUser == null) {
            val builder = AlertDialog.Builder(context)
            builder.setTitle("No estás registrado en Geinz Work")
            builder.setMessage("Regístrate en Geinz Work para que puedas dejar una reseña al trabajador.")
            builder.setPositiveButton("Cuenta Simple") { dialog, _ ->
                constantes.showLoadingDialog(
                    context,
                    2000,
                    "Cargando información",
                    "Espere un momento..."
                )
                val intent = Intent(context, CuentaFreelancer::class.java).apply {
                    putExtra("tipoCuenta", "cuentaSimple")
                    putExtra("Title", "Cuenta Simple")
                    putExtra("pasos", "Estás a 1/2 pasos")
                }
                context.startActivity(intent)
                dialog.dismiss()
            }
            builder.setNegativeButton("Cuenta Trabajador") { dialog, _ ->
                val intent = Intent(context, CuentaFreelancer::class.java).apply {
                    putExtra("tipoCuenta", "cuentaTrabajador")
                    putExtra("Title", "Cuenta Freelancer")
                    putExtra("pasos", "Estás a 1/5 pasos")
                }
                context.startActivity(intent)
                dialog.dismiss()
            }
            builder.create().show()
        } else {
            val dbReview = FirebaseFirestore.getInstance()
                .collection("Trabajadores_Usuarios_Drivers")
                .document("trabajadores")
                .collection("trabajadores")
                .document(idTrabajador)
                .collection("review")
                .document(idUsuario)

            dbReview.get().addOnSuccessListener { document ->
                if (document.exists()) {
                    val cantidad = document.getString(Variables.cantidad) ?: ""
                    val review = document.getString(Variables.reseña) ?: ""
                    val builder = AlertDialog.Builder(context)
                    builder.setTitle("Tienes una reseña registrada")
                    builder.setMessage("¿Deseas editar la reseña que dejaste anteriormente?")
                    builder.setPositiveButton("Sí") { dialog, _ ->
                        constantes.showLoadingDialog(
                            context,
                            2000,
                            "Espere un momento",
                            "Espere un momento..."
                        )
                        val intent = Intent(context, EditarReview::class.java).apply {
                            putExtra("TipoEditado", tipoEditado)
                            putExtra("idReview", idUsuario)
                            putExtra(Variables.iduser, idUsuario)
                            putExtra(Variables.idTrabajdor, idTrabajador)
                            putExtra(Variables.cantidad, cantidad)
                            putExtra("review", review)
                            putExtra("nuevaReseña", contenidoReview.text.toString())
                            putExtra("cantidadStart", cantidadEstrellas.text.toString())
                            putExtra("editado_adaptador",true)
                        }
                        contenidoReview.setText("")
                        cantidadEstrellas.setText("")
                        context.startActivity(intent)
                    }
                    builder.setNegativeButton("No") { dialog, _ -> dialog.dismiss() }
                    builder.create().show()
                } else {
                    val nuevaReview = contenidoReview.text.toString()
                    val estrellas = cantidadEstrellas.text.toString()
                    if (nuevaReview.isEmpty() || estrellas.isEmpty()) {
                        Toast.makeText(context, "Ingrese una reseña y cantidad", Toast.LENGTH_SHORT)
                            .show()
                    } else if (estrellas.toInt() !in 0..5) {
                        Toast.makeText(
                            context,
                            "Solo se permite como máximo 5 estrellas",
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        val estrellasInt = estrellas.toInt()
                        val dbTrabajadores = FirebaseFirestore.getInstance()
                            .collection("Trabajadores_Usuarios_Drivers")
                            .document("trabajadores")
                            .collection("trabajadores")
                            .document(idTrabajador)

                        dbTrabajadores.get().addOnSuccessListener { trabajador ->
                            if (trabajador.exists()) {
                                val estrellasAnteriores =
                                    trabajador.getString("estrellas")?.toIntOrNull() ?: 0
                                val sumaEstrellas = estrellasAnteriores + estrellasInt
                                val hashMap = mapOf("estrellas" to sumaEstrellas.toString())
                                dbTrabajadores.set(hashMap, SetOptions.merge())
                                    .addOnSuccessListener {
                                        Toast.makeText(
                                            context,
                                            "Reseña guardada exitosamente",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                    .addOnFailureListener {
                                        Log.d("error_review", "Error al guardar la reseña")

                                    }
                            }
                        }
                        obtenerUserFirestore(
                            idUsuario,
                            dbReview,
                            nuevaReview,
                            estrellas,
                            context,
                            hora,
                            fecha,
                            tipoTrabajo
                        )
                    }
                }
            }.addOnFailureListener {
                Log.e("verificarSiExisteReview", "Error al obtener la reseña: ${it.message}")
            }
        }
    }


    private fun obtenerUserFirestore(
        idUSer: String,
        ref: DocumentReference,
        review: String,
        cantidad: String, context: Context, hora: TextView, fecha: TextView, TipoTrabajo: String
    ) {
        val db1 = FirebaseFirestore.getInstance().collection(Variables.trabajadores_usuariosDB)
            .document(Variables.trabajadoresDB).collection(Variables.trabajadoresDB)
            .document(idUSer)
        val db2 = FirebaseFirestore.getInstance().collection(Variables.trabajadores_usuariosDB)
            .document(Variables.usuarios_db).collection(Variables.usuarios_db).document(idUSer)
        db1.get()
            .addOnSuccessListener { encontrado ->
                if (encontrado.exists()) {
                    val userData = encontrado.data
                    val idContainer = userData?.get("id") as? String
                    val alertDialogBuilder = AlertDialog.Builder(context)
                    alertDialogBuilder.apply {
                        setTitle("Confirmación de Reseña")
                        setMessage("Si la reseña indicada no corresponde a ningún trabajo realizado por el trabajador, tu cuenta podría ser sancionada por proporcionar información incorrecta o falsa. ¿Estás seguro de dejar esta reseña?")
                        setPositiveButton("Sí") { _, _ ->
                            agregarReview(
                                ref,
                                idContainer.orEmpty(),
                                review,
                                cantidad,
                                context,
                                hora,
                                fecha,
                                TipoTrabajo
                            )
                        }
                        setNegativeButton("No") { dialog, _ ->
                            dialog.dismiss()
                        }
                    }
                    val alertDialog = alertDialogBuilder.create()
                    alertDialog.show()


                } else {
                    db2.get()
                        .addOnSuccessListener { encontrado2 ->
                            if (encontrado2.exists()) {
                                val userData = encontrado2.data
                                val idContainer = userData?.get("id") as? String
                                agregarReview(
                                    ref,
                                    idContainer.orEmpty(),
                                    review,
                                    cantidad, context, hora, fecha, TipoTrabajo
                                )
                            } else {
                                Log.d("error_review", "Usuario no encontrado")
                            }
                        }.addOnFailureListener {
                            Log.d("error_review", "Error al obtener datos del usuario")

                        }
                }
            }.addOnFailureListener {
                Log.d("error_review", "Error al obtener datos del usuario")
            }
    }


    fun agregarReview(
        ref: DocumentReference,
        idconteiner: String,
        reseña: String,
        cantidad: String, context: Context, hora: TextView, fecha: TextView, TipoTrabajo: String
    ) {
        val hashMap = hashMapOf(
            Variables.editado to false,
            Variables.TipoTrabajo to TipoTrabajo,
            Variables.hora to hora.text.toString(),
            Variables.fecha to fecha.text.toString(),
            Variables.iduserReview to idconteiner,
            Variables.reseña to reseña,
            Variables.cantidad to cantidad,

            )

        ref.set(hashMap, SetOptions.merge())
            .addOnSuccessListener {
                Log.d("reviewGuardada", "reseña guardada exitosamente")

            }
            .addOnFailureListener { e ->
                Log.d("error_review", "\"Error al crear la reseña: $e\"")

            }

    }

    fun obtenerReview(
        relativeLayout: RelativeLayout,
        recyclerView: RecyclerView,
        progressBar: ProgressBar? = null,
        idTrabajdor: String,
        recicleContainer: RecyclerView,
        context: Context,
        fitlradoString: String
    ) {
        progressBar?.visibility = View.VISIBLE
        relativeLayout.visibility = View.GONE
        recyclerView.visibility = View.GONE

        if (idTrabajdor != null) {
            val db = FirebaseFirestore.getInstance().collection("Trabajadores_Usuarios_Drivers")
                .document("trabajadores").collection("trabajadores")
                .document(idTrabajdor).collection("review")
            println("id del user desde review $idTrabajdor")

            db.get().addOnSuccessListener { res ->
                val reviewsParaMostrar = mutableListOf<daclassReview>()
                var pendingVerifications = 0
                var reviewsExist = false

                for (datos in res) {
                    val data = datos.data
                    val editado = data?.get(Variables.editado) as? Boolean ?: false
                    val id = data?.get(Variables.iduserReview) as? String ?: ""
                    val fecha = data?.get(Variables.fecha) as? String ?: ""
                    val hora = data?.get(Variables.hora) as? String ?: ""
                    val reseña = data?.get(Variables.reseña) as? String ?: ""
                    val TipoTrabajo = data?.get(Variables.TipoTrabajo) as? String ?: ""
                    val cantidadStr = data?.get(Variables.cantidad) as? String ?: ""
                    val cantidad = cantidadStr.toIntOrNull() ?: 0
                    val review = daclassReview(
                        id,
                        cantidadStr,
                        reseña,
                        hora,
                        fecha,
                        TipoTrabajo,
                        editado
                    )

                    when (fitlradoString) {
                        "uno_tres" -> {
                            if (cantidad in 1..3) {
                                reviewsParaMostrar.add(review)
                                reviewsExist = true
                            }
                        }

                        "cuatro_cinco" -> {
                            if (cantidad in 4..5) {
                                reviewsParaMostrar.add(review)
                                reviewsExist = true
                            }
                        }

                        "verificado" -> {
                            pendingVerifications++
                            constantesCarrito.setearDatosUsuarioImgNombre(idUSer = id) { nombre, img, apellido, nacionalidad, categoria, verificado, trabajador_user ->
                                if (verificado == true) {
                                    reviewsParaMostrar.add(review)
                                    if (!reviewsExist) reviewsExist = true
                                }
                                pendingVerifications--
                                if (pendingVerifications == 0) {
                                    if (reviewsExist) {
                                        inicalizarRecicle(
                                            idTrabajdor,
                                            reviewsParaMostrar,
                                            recicleContainer,
                                            context
                                        )
                                        recyclerView.visibility = View.VISIBLE
                                    } else {
                                        relativeLayout.visibility = View.VISIBLE
                                    }
                                    progressBar?.visibility = View.GONE
                                }
                            }
                        }

                        "todos" -> {
                            reviewsParaMostrar.add(review)
                            reviewsExist = true
                        }

                        "tu_review" -> {
                            println("neceisto_holaaa $id == ${firebaseAuth.uid.toString()}")
                            if (id == firebaseAuth.uid.toString()) {
                                reviewsParaMostrar.add(review)
                                reviewsExist = true
                            }
                            // No hacer nada si no coincide, simplemente se ignora esa review
                        }


                        else -> {
                            reviewsParaMostrar.add(review)
                            reviewsExist = true
                        }
                    }
                }

                // Para los casos donde no se necesita verificación asíncrona
                if (fitlradoString != "verificado") {
                    if (reviewsExist) {
                        inicalizarRecicle(
                            idTrabajdor,
                            reviewsParaMostrar,
                            recicleContainer,
                            context
                        )
                        recyclerView.visibility = View.VISIBLE
                    } else {
                        relativeLayout.visibility = View.VISIBLE
                    }
                    progressBar?.visibility = View.GONE
                } else if (res.isEmpty) { // Si no hay ninguna review para verificar
                    relativeLayout.visibility = View.VISIBLE
                    progressBar?.visibility = View.GONE
                }
            }.addOnFailureListener { e ->
                Log.d("idNull", "No se encontró review $e")
                progressBar?.visibility = View.GONE
                relativeLayout.visibility = View.VISIBLE
            }
        } else {
            Log.d("idNull", "ID de usuario nulo")
            progressBar?.visibility = View.GONE
            relativeLayout.visibility = View.VISIBLE
        }
    }


    fun inicalizarRecicle(
        idTrabajdor: String,
        listaReview: MutableList<daclassReview>,
        recicleContainer: RecyclerView, context: Context
    ) {
        val recicle = recicleContainer
        recicle.layoutManager = LinearLayoutManager(context)
        recicle.adapter = adaptadorReview(listaReview, idTrabajdor)
    }


}