package com.geinzz.geinzwork.vistaTiendas

import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import com.bumptech.glide.Glide
import com.geinzz.geinzwork.NotificacionRS
import com.geinzz.geinzwork.R
import com.geinzz.geinzwork.utils.constantes.constantes.constantes
import com.geinzz.geinzwork.utils.constantes.constantes.constantesImagenes
import com.geinzz.geinzwork.databinding.ActivityAgregarPagoComprareservaTiendasBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class agregarPagoComprareservaTiendas : AppCompatActivity() {
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var binding: ActivityAgregarPagoComprareservaTiendasBinding
    private var fotoComprovante: Uri? = null
    private val pciMEdia =
        registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
            if (uri != null) {
                fotoComprovante = uri
                binding.imgQRPago.setImageURI(uri)
            } else {
                println("Imagen no seleccionada")
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAgregarPagoComprareservaTiendasBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        firebaseAuth = FirebaseAuth.getInstance()
        val idTienda = intent.getStringExtra("idTienda").toString()
        val idPedido = intent.getStringExtra("codigoPedido").toString()
        val metodoPAgo = intent.getStringExtra("metodoPago").toString()
        val tipo=intent.getStringExtra("tipo").toString()
        val yapeRuta = "Tiendas/$idTienda/QR_pagos/Yape.jpg"
        val plinrRuta = "Tiendas/$idTienda/QR_pagos/Plin.jpg"
        verificarExistenciaCampos(idTienda, idPedido) { existe ->
            if (!existe) {
                binding.linealSubir.isVisible = true
                binding.LinealPagado.isVisible = false
                obtenerMetodosPago(yapeRuta, plinrRuta, metodoPAgo)

                binding.notificar.setOnClickListener {
                    if (fotoComprovante != null) {
                        ActulizarCampos(fotoComprovante!!, idTienda, idPedido)
                    } else {
                        Toast.makeText(
                            this,
                            "Debes subir una foto para continuar",
                            Toast.LENGTH_SHORT
                        ).show()
                    }

                }

                binding.imgQRPago.setOnClickListener {
                    pciMEdia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                }

            } else {
                binding.linealSubir.isVisible = false
                binding.LinealPagado.isVisible = true
                when(tipo){
                    "reserva"->{
                        obtnerComprovante(tipo,idTienda, idPedido)
                    }
                    "compra"->{
                        obtnerComprovante(tipo,idTienda, idPedido)
                    }
                }

            }
        }
    }

    private fun obtnerComprovante(tipo:String,idTienda: String, idPedido: String) {
        val database = FirebaseDatabase.getInstance()
        val comprasRef = database.getReference("PedidosUsuario/${firebaseAuth.uid}/tiendas/$idTienda/reserva/$idPedido")

        comprasRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val comprobantePago =
                    snapshot.child("comprobante_pago").getValue(String::class.java)
                val comentarioAdicional =
                    snapshot.child("comentario_adicional").getValue(String::class.java)


                binding.comentarioAdicional.text =
                    comentarioAdicional ?: "No hay comentario adicional"

                // Cargar la imagen del comprobante de pago si existe
                if (comprobantePago != null) {
                    try {
                        Glide.with(this@agregarPagoComprareservaTiendas)
                            .load(comprobantePago)
                            .into(binding.comprovante)
                    } catch (e: Exception) {
                        println("Error al setear la imagen: ${e.message}")
                    }
                } else {
                    // Manejar el caso donde no hay comprobante de pago
                    binding.comprovante.setImageDrawable(null) // O cualquier otra acción
                }
            }

            override fun onCancelled(error: DatabaseError) {
                println("Error al verificar la existencia de los campos: ${error.message}")
            }
        })
    }


    private fun obtenerMetodosPago(yapeRuta: String, plinrRuta: String, metodoPAgo: String) {
        Log.d("meotoPAgoPasado",metodoPAgo)
        if (metodoPAgo.equals("YAPE")) {
            binding.imagenYape.isVisible = true
            binding.imagenPlin.isVisible = false
            constantesImagenes.obtenerURLDescarga(
                this,
                binding.imagenYape,
                yapeRuta
            )
        } else if (metodoPAgo.equals("PLIN")) {
            binding.imagenPlin.isVisible = true
            binding.imagenYape.isVisible = false
            constantesImagenes.obtenerURLDescarga(
                this,
                binding.imagenPlin,
                plinrRuta
            )
        }


    }

    private fun ActulizarCampos(uri: Uri, idTienda: String, idPedido: String) {
        val storageRef = FirebaseStorage.getInstance().getReference()
            .child("Tiendas")
            .child(idTienda)
            .child("comprovantes_pago_compras_reserva")
            .child("reservas")
            .child(idPedido)

        val database = FirebaseDatabase.getInstance()
        val comprasRef = database.getReference("PedidosUsuario/${firebaseAuth.uid}/tiendas/$idTienda/reserva/$idPedido")
        val realtimeTienda = database.getReference("CompraTienda").child(idTienda)
            .child(firebaseAuth.uid.toString()).child("reserva").child(idPedido)

        storageRef.putFile(uri)
            .addOnSuccessListener { taskSnapshot ->
                storageRef.downloadUrl.addOnSuccessListener { downloadUri ->
                    val downloadUrl = downloadUri.toString()

                    val updateData = mapOf(
                        "comprobante_pago" to downloadUrl,
                        "comentario_adicional" to binding.comentarioADIED.text.toString()
                    )
                    comprasRef.updateChildren(updateData)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                println("Comprobante de pago actualizado exitosamente en Realtime Database.")
                                constantes.obtnerTokenTienda(idTienda) { token ->
                                    GlobalScope.launch {
                                        val enviar_notificaciones = NotificacionRS()
                                        enviar_notificaciones.sendNotification_con_parametros(
                                            "IDTienda",
                                          idTienda,
                                            "INICIO",
                                            this@agregarPagoComprareservaTiendas,
                                            token,
                                            "Comprovante de pago subido de ${idPedido}",
                                            "El pedido Nº $idPedido subio su comprovante de pago verificalo "
                                        )
                                    }
                                }
                            } else {
                                println("Error al actualizar el comprobante de pago en PedidosUsuario: ${task.exception?.message}")
                            }
                        }

                    realtimeTienda.updateChildren(updateData)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                println("Comprobante de pago actualizado exitosamente en CompraTienda.")
                                Toast.makeText(
                                    this,
                                    "Comprovante subido correctamente",
                                    Toast.LENGTH_SHORT
                                ).show()
                                onBackPressed()
                            } else {
                                println("Error al actualizar el comprobante de pago en CompraTienda: ${task.exception?.message}")
                            }
                        }
                }.addOnFailureListener { exception ->
                    println("Error al obtener la URL de descarga: ${exception.message}")
                }
            }
            .addOnFailureListener { exception ->
                println("Error al subir el archivo a Firebase Storage: ${exception.message}")
            }
    }


    private fun verificarExistenciaCampos(
        idTienda: String,
        idPedido: String,
        callback: (Boolean) -> Unit,
    ) {
        val database = FirebaseDatabase.getInstance()
        val comprasRef =
            database.getReference("PedidosUsuario/${firebaseAuth.uid}/tiendas/$idTienda/reserva/$idPedido")

        comprasRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                // Verifica si el nodo existe y luego si los campos específicos existen
                val existeNodo = snapshot.exists()
                val existeComprobantePago = snapshot.child("comprobante_pago").exists()
                val existeComentarioAdicional = snapshot.child("comentario_adicional").exists()

                // Llama al callback con true si el nodo y los campos específicos existen, false en caso contrario
                callback(existeNodo && existeComprobantePago && existeComentarioAdicional)
            }

            override fun onCancelled(error: DatabaseError) {
                println("Error al verificar la existencia de los campos: ${error.message}")
                callback(false)
            }
        })
    }


}