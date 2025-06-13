package com.example.geinzwork.fragmentos

import android.content.ContentValues.TAG
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.SpannableString
import android.util.Log
import android.view.LayoutInflater
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.geinzwork.adapterViewholder.adapter_metodos_entrega
import com.example.geinzwork.adapterViewholder.adapter_metodos_pagos
import com.example.geinzwork.constantesGeneral.Variables
import com.example.geinzwork.crear_publicacion_productos_trabajadores
import com.example.geinzwork.crear_publicaciones_recientes
import com.example.geinzwork.dataclass.dataclass_metodos_entrega
import com.example.geinzwork.dataclass.dataclass_metodos_pagos
import com.geinzz.geinzwork.R
import com.geinzz.geinzwork.constantesGeneral.constantestextos_general
import com.geinzz.geinzwork.crear_trabajos_realizados
import com.geinzz.geinzwork.databinding.ActivityPanelPublicacionTrabajadorBinding
import com.geinzz.geinzwork.databinding.BotomSheetDialogMetodosPagoBinding
import com.geinzz.geinzwork.databinding.BottomSheeetMetodoEntregaBinding
import com.geinzz.geinzwork.databinding.BottomSheetAgregarRedesBinding
import com.geinzz.geinzwork.databinding.BottomSheetNumeroNombrePagoBinding
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions

class panel_publicacion_trabajador : AppCompatActivity() {
    private lateinit var binding: ActivityPanelPublicacionTrabajadorBinding
    private val lista = mutableListOf<dataclass_metodos_pagos>()
    private val lista_entrega = mutableListOf<dataclass_metodos_entrega>()
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var dialog: BottomSheetDialog
    private var yape: Boolean = false
    private var plin: Boolean = false
    private var efectivo: Boolean = false
    private var trasnferecnia: Boolean = false
    private var deliver_gratis: Boolean = false
    private var delivery: Boolean = false
    private var coordinar: Boolean = false
    private var lugaresEntrega: Boolean = false
    private var retiroTienda: Boolean = false
    private var envioCourier: Boolean = false
    private var entregaProgramada: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPanelPublicacionTrabajadorBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        firebaseAuth = FirebaseAuth.getInstance()
        setear_datos_includes()
        binding.panelMetoods.metodoEntrega.setOnClickListener {
            dialog = BottomSheetDialog(this)
            bottomSheet_metodo_entrega()
            dialog.show()
        }

        binding.panelMetoods.metodosPago.setOnClickListener {
            dialog = BottomSheetDialog(this)
            bottomSheet_metodos_pago()
            dialog.show()

        }

        binding.metodoNumeroNombre.setOnClickListener {
            dialog = BottomSheetDialog(this)
            bottomSheet_numero_nombre_pagos()
            dialog.show()
        }

        binding.agregarRedesSociales.setOnClickListener {
            dialog = BottomSheetDialog(this)
            bottomSheet_agregar_redes()
            dialog.show()
        }

    }

    private fun bottomSheet_agregar_redes() {
        val binding_bottom = BottomSheetAgregarRedesBinding.inflate(LayoutInflater.from(this))
        val view = binding_bottom.root

        firebaseAuth = FirebaseAuth.getInstance()
        binding_bottom.Enviar.setOnClickListener {
            agregarRedes(firebaseAuth.uid.toString(), binding_bottom)
        }
        binding_bottom.limpiarFB.setOnClickListener {
            binding_bottom.fbEd.setText("")
        }
        binding_bottom.limpiarIG.setOnClickListener {
            binding_bottom.igED.setText("")
        }
        binding_bottom.limpiarTK.setOnClickListener {
            binding_bottom.tkED.setText("")
        }
        obtnerDatos(firebaseAuth.uid.toString(), binding_bottom)


        dialog.setContentView(view)
    }

    private fun obtnerDatos(id: String, botto_sheet_redes: BottomSheetAgregarRedesBinding) {
        val db = FirebaseFirestore.getInstance().collection(Variables.trabajadores_usuariosDB)
            .document(Variables.trabajadoresDB).collection(Variables.trabajadoresDB).document(id)
        db.get().addOnSuccessListener { res ->
            if (res.exists()) {
                val data = res.data
                val ig = data?.get(Variables.IG) as? String ?: ""
                val fb = data?.get(Variables.FB) as? String ?: ""
                val tk = data?.get(Variables.TK) as? String ?: ""
                botto_sheet_redes.igED.setText(ig)
                botto_sheet_redes.fbEd.setText(fb)
                botto_sheet_redes.tkED.setText(tk)
            } else {
                Log.d(TAG, "El documento no existe")
            }
        }.addOnFailureListener { e ->
            Log.e(TAG, "Error al obtener datos: ${e.message}", e)
        }
    }


    private fun agregarRedes(id: String, botto_sheet_redes: BottomSheetAgregarRedesBinding) {
        val db = FirebaseFirestore.getInstance().collection(Variables.trabajadores_usuariosDB)
            .document(Variables.trabajadoresDB).collection(Variables.trabajadoresDB).document(id)
        val hasmap = hashMapOf<String, Any>(
            Variables.IG to botto_sheet_redes.igED.text.toString(),
            Variables.FB to botto_sheet_redes.fbEd.text.toString(),
            Variables.TK to botto_sheet_redes.tkED.text.toString()
        )
        db.set(hasmap, SetOptions.merge()).addOnSuccessListener {
            Toast.makeText(
                this,
                "Redes agregadas o editadas correctamente",
                Toast.LENGTH_SHORT
            ).show()
        }
    }


    private fun bottomSheet_metodo_entrega() {
        val bottoSheet_entrega = BottomSheeetMetodoEntregaBinding.inflate(LayoutInflater.from(this))
        bottoSheet_entrega.cerrar.setOnClickListener { dialog.dismiss() }
        val view = bottoSheet_entrega.root

        // Checkboxes con visibilidad de layouts
        bottoSheet_entrega.delivery.setOnCheckedChangeListener { _, isChecked ->
            delivery = isChecked
            bottoSheet_entrega.linealEnvioGratis.isVisible = isChecked
            if (!isChecked) bottoSheet_entrega.grupoEnvioGratis.clearCheck()
        }

        bottoSheet_entrega.corrdinar.setOnCheckedChangeListener { _, isChecked ->
            coordinar = isChecked
        }

        bottoSheet_entrega.lugaresEntrega.setOnCheckedChangeListener { _, isChecked ->
            lugaresEntrega = isChecked
            bottoSheet_entrega.linealLugaresEntrega.isVisible = isChecked
            if (!isChecked) {
                bottoSheet_entrega.LugaresEntregaED.text?.clear()
                bottoSheet_entrega.localidadMetodoEntregaED.text?.clear()
            }
        }

        bottoSheet_entrega.retiroTienda.setOnCheckedChangeListener { _, isChecked ->
            retiroTienda = isChecked
            bottoSheet_entrega.puntosVenta.isVisible = isChecked
            if (!isChecked) {
                bottoSheet_entrega.lugarReferenciaPuntoVentaED.text?.clear()
                bottoSheet_entrega.localidadEntregaPuntosED.text?.clear()
                bottoSheet_entrega.nombreTiendaED.text?.clear()
            }
        }

        bottoSheet_entrega.envioCourier.setOnCheckedChangeListener { _, isChecked ->
            envioCourier = isChecked
        }

        bottoSheet_entrega.entregaProgramada.setOnCheckedChangeListener { _, isChecked ->
            entregaProgramada = isChecked
        }
        obtenerMetodosEntrega(bottoSheet_entrega)
        bottoSheet_entrega.CrearMetodo.setOnClickListener {
            crear_metodoEntrega(
                delivery,
                coordinar,
                lugaresEntrega,
                retiroTienda,
                envioCourier,
                entregaProgramada,
                bottoSheet_entrega.nombreReferenciaED.text.toString(),
                bottoSheet_entrega
            )


        }

        dialog.setContentView(view)
    }

    private fun verificarNombreDisponible(
        collectionReference: CollectionReference,
        nombreColeccion: EditText,
        resultado: (Boolean) -> Unit
    ) {
        collectionReference.get().addOnSuccessListener { res ->
            var nombreExistente = false

            for (document in res) {
                val nombreMetodo = document.getString("nombre_metodo") ?: ""
                if (nombreMetodo.equals(
                        nombreColeccion.text.toString().trim(),
                        ignoreCase = true
                    )
                ) {
                    nombreExistente = true
                    break
                }
            }

            if (nombreExistente) {
                Toast.makeText(
                    this,
                    "Elige otro nombre, este nombre ya existe",
                    Toast.LENGTH_SHORT
                ).show()
                nombreColeccion.error = "Elige otro nombre, este nombre ya existe"
                nombreColeccion.requestFocus()
                resultado(false)
            } else {
                resultado(true)
            }
        }.addOnFailureListener {
            Toast.makeText(this, "Error al verificar nombre: ${it.message}", Toast.LENGTH_SHORT)
                .show()
            resultado(false)
        }
    }


    private fun crear_metodoEntrega(
        delivery: Boolean,
        coordinar: Boolean,
        lugaresEntrega: Boolean,
        retiroTienda: Boolean,
        envioCourier: Boolean,
        entregaProgramada: Boolean,
        nombre_referencia: String,
        bottosheetEntrega: BottomSheeetMetodoEntregaBinding
    ) {
        // Validaciones
        if (delivery == true && bottosheetEntrega.grupoEnvioGratis.checkedRadioButtonId == -1) {
            Toast.makeText(this, "Selecciona si el delivery es gratis o no", Toast.LENGTH_SHORT)
                .show()
            return
        }
        if (bottosheetEntrega.nombreReferenciaED.text.toString().isEmpty()) {
            bottosheetEntrega.nombreReferenciaED.error =
                "El nombre del metodo de entrega es obligatorio"
            bottosheetEntrega.nombreReferenciaED.requestFocus()
            return

        }
        if (!(delivery || coordinar || lugaresEntrega || retiroTienda || envioCourier || entregaProgramada)) {
            Toast.makeText(
                this,
                "Selecciona al menos un método de entrega para continuar",
                Toast.LENGTH_SHORT
            ).show()
            return
        }
        if (lugaresEntrega) {
            val textoLugares = bottosheetEntrega.LugaresEntregaED
            val localidadLugares = bottosheetEntrega.localidadMetodoEntregaED

            val textoLugaresStr = textoLugares.text.toString().trim()
            val localidadStr = localidadLugares.text.toString().trim()

            if (textoLugaresStr.isEmpty()) {
                textoLugares.error = "Ingresa los lugares de entrega"
                textoLugares.requestFocus()
                return
            }

            if (localidadStr.isEmpty()) {
                localidadLugares.error = "Ingresa una localidad de entrega"
                localidadLugares.requestFocus()
                return
            }
        }
        if (retiroTienda) {
            val textoPuntoVenta =
                bottosheetEntrega.lugarReferenciaPuntoVentaED.text.toString().trim()
            val localidadPuntoVenta =
                bottosheetEntrega.localidadEntregaPuntosED.text.toString().trim()
            val nombreTienda = bottosheetEntrega.nombreTiendaED.text.toString().trim()

            if (textoPuntoVenta.isEmpty()) {
                bottosheetEntrega.lugarReferenciaPuntoVentaED.error =
                    "Ingresa el punto de venta o tienda"
                bottosheetEntrega.lugarReferenciaPuntoVentaED.requestFocus()
                return
            }

            if (localidadPuntoVenta.isEmpty()) {
                bottosheetEntrega.localidadEntregaPuntosED.error =
                    "Ingresa la localidad del punto de venta"
                bottosheetEntrega.localidadEntregaPuntosED.requestFocus()
                return
            }

            if (nombreTienda.isEmpty()) {
                bottosheetEntrega.nombreTiendaED.error = "Ingresa el nombre de la tienda"
                bottosheetEntrega.nombreTiendaED.requestFocus()
                return
            }
        }
        val deliver_gratis = when (bottosheetEntrega.grupoEnvioGratis.checkedRadioButtonId) {
            R.id.si -> true
            R.id.no -> false
            else -> false
        }
        val hashMap = hashMapOf<String, Any>(
            "delivery" to delivery,
            "coordinar" to coordinar,
            "lugaresEntrega" to lugaresEntrega,
            "retiroTienda" to retiroTienda,
            "envioCourier" to envioCourier,
            "entregaProgramada" to entregaProgramada,
            "nombre_metodo" to nombre_referencia
        )
        if (delivery) {
            hashMap["datos_delivery"] = hashMapOf("gratis" to deliver_gratis)
        }
        if (lugaresEntrega) {
            val lugarTexto = bottosheetEntrega.LugaresEntregaED.text.toString().trim()
            hashMap["datos_lugares_entrega"] = hashMapOf(
                "localidad" to bottosheetEntrega.localidadMetodoEntregaED.text.toString(),
                "descripcion" to lugarTexto
            )
        }
        if (retiroTienda) {
            val puntoVentaTexto =
                bottosheetEntrega.lugarReferenciaPuntoVentaED.text.toString().trim()
            hashMap["datos_retiro_tienda"] = hashMapOf(
                "localidad" to bottosheetEntrega.localidadEntregaPuntosED.text.toString(),
                "referencia" to puntoVentaTexto,
                "nombre_tienda" to bottosheetEntrega.nombreTiendaED.text.toString()
            )
        }
        val db = FirebaseFirestore.getInstance().collection("Trabajadores_Usuarios_Drivers")
            .document("trabajadores").collection("trabajadores")
            .document(firebaseAuth.uid.toString())
            .collection("metodos_entrega")

        val startTime = System.currentTimeMillis()
        verificarNombreDisponible(db, bottosheetEntrega.nombreReferenciaED) { res ->
            if (res) {
                bottosheetEntrega.layoutRecicleTexview.isVisible = false
                bottosheetEntrega.cargandoMetodosPago.isVisible = true
                db.add(hashMap).addOnSuccessListener { documentReference ->
                    val idGenerado = documentReference.id
                    val hashMap = hashMapOf<String, Any>("id" to idGenerado)
                    db.document(idGenerado).set(hashMap, SetOptions.merge()).addOnSuccessListener {
                        val endTime = System.currentTimeMillis() // Fin del proceso
                        val duration = endTime - startTime
                        Handler(Looper.getMainLooper()).postDelayed({
                            bottosheetEntrega.layoutRecicleTexview.isVisible = true
                            bottosheetEntrega.cargandoMetodosPago.isVisible = false
                            Toast.makeText(
                                this,
                                "Método de entrega creado correctamente",
                                Toast.LENGTH_SHORT
                            ).show()
                            // ✅ LIMPIAR TODOS LOS CAMPOS Y CHECKBOXES
                            bottosheetEntrega.delivery.isChecked = false
                            bottosheetEntrega.corrdinar.isChecked = false
                            bottosheetEntrega.lugaresEntrega.isChecked = false
                            bottosheetEntrega.retiroTienda.isChecked = false
                            bottosheetEntrega.envioCourier.isChecked = false
                            bottosheetEntrega.entregaProgramada.isChecked = false

                            bottosheetEntrega.nombreReferenciaED.text?.clear()
                            bottosheetEntrega.grupoEnvioGratis.clearCheck()
                            bottosheetEntrega.LugaresEntregaED.text?.clear()
                            bottosheetEntrega.localidadMetodoEntregaED.text?.clear()
                            bottosheetEntrega.lugarReferenciaPuntoVentaED.text?.clear()
                            bottosheetEntrega.localidadEntregaPuntosED.text?.clear()
                            bottosheetEntrega.nombreTiendaED.text?.clear()
                            obtenerMetodosEntrega(bottosheetEntrega)
                        }, duration)
                        Log.d(
                            "TIEMPO_CREACION",
                            "Tiempo total para crear y actualizar el documento: $duration ms"
                        )
                    }
                }.addOnFailureListener { e ->
                    Log.d("error_crear", "error al crear una referencia: ${e.message}")
                    bottosheetEntrega.cargandoMetodosPago.isVisible = false
                }
            }
        }


    }

    private fun obtenerMetodosEntrega(bottoSheet_entrega: BottomSheeetMetodoEntregaBinding) {
        lista_entrega.clear()
        bottoSheet_entrega.cargandoMetodosPago.isVisible = true
        bottoSheet_entrega.textoSinMetodos.isVisible = false
        bottoSheet_entrega.recicleViewMetodosEntrega.isVisible = false
        val tiempoInicio = System.currentTimeMillis()
        val db = FirebaseFirestore.getInstance().collection("Trabajadores_Usuarios_Drivers")
            .document("trabajadores").collection("trabajadores")
            .document(firebaseAuth.uid.toString())
            .collection("metodos_entrega")
        db.get().addOnSuccessListener { res ->
            for (datos in res) {
                val data = datos.data
                val delivery = data?.get("delivery") as? Boolean ?: false
                val coordinar = data?.get("coordinar") as? Boolean ?: false
                val lugaresEntrega = data?.get("lugaresEntrega") as? Boolean ?: false
                val retiroTienda = data?.get("retiroTienda") as? Boolean ?: false
                val envioCourier = data?.get("envioCourier") as? Boolean ?: false
                val entregaProgramada = data?.get("entregaProgramada") as? Boolean ?: false
                val nombre_metodo = data?.get("nombre_metodo") as? String ?: ""
                val id = data?.get("id") as? String ?: ""
                val listaDataclass = dataclass_metodos_entrega(
                    delivery,
                    coordinar,
                    lugaresEntrega,
                    retiroTienda,
                    envioCourier,
                    entregaProgramada,
                    nombre_metodo, id
                )
                lista_entrega.add(listaDataclass)

            }
            val tiempoFin = System.currentTimeMillis()
            val delay = (tiempoFin - tiempoInicio)
            Handler(Looper.getMainLooper()).postDelayed({
                bottoSheet_entrega.cargandoMetodosPago.isVisible = false
                if (lista_entrega.isNotEmpty()) {
                    incializar_recicle_entrega(bottoSheet_entrega)
                    bottoSheet_entrega.textoSinMetodos.isVisible = false
                    bottoSheet_entrega.recicleViewMetodosEntrega.isVisible = true
                } else {
                    bottoSheet_entrega.textoSinMetodos.isVisible = true
                    bottoSheet_entrega.recicleViewMetodosEntrega.isVisible = false
                }
            }, delay)


        }.addOnFailureListener { e ->
            Log.e("METODOS_ENTREGA", "Error al obtener métodos de entrega", e)
        }
    }


    private fun bottomSheet_metodos_pago() {
        val binding_bottomSheet =
            BotomSheetDialogMetodosPagoBinding.inflate(LayoutInflater.from(this))
        binding_bottomSheet.cerrar.setOnClickListener { dialog.dismiss() }

        val view = binding_bottomSheet.root
        obtner_Metodos_pagosCreados(binding_bottomSheet)

        binding_bottomSheet.checkYape.setOnCheckedChangeListener { _, isChecked ->
            yape = isChecked
        }

        binding_bottomSheet.checkPlin.setOnCheckedChangeListener { _, isChecked ->
            plin = isChecked
        }

        binding_bottomSheet.checkEfectivo.setOnCheckedChangeListener { _, isChecked ->
            efectivo = isChecked
        }

        binding_bottomSheet.checkTransferencia.setOnCheckedChangeListener { _, isChecked ->
            trasnferecnia = isChecked
        }

        binding_bottomSheet.CrearMetodo.setOnClickListener {
            agregar_Referencia(
                yape,
                plin,
                efectivo,
                trasnferecnia,
                binding_bottomSheet.nombreReferenciaED.text.toString(), binding_bottomSheet
            )

        }

        dialog.setContentView(view)

    }

    private fun agregar_Referencia(
        yape: Boolean,
        plin: Boolean,
        efectivo: Boolean,
        tranferencia: Boolean,
        nombre_metodo: String,
        BotomSheetDialogMetodosPagoBinding: BotomSheetDialogMetodosPagoBinding
    ) {
        val startTime = System.currentTimeMillis()
        if (!yape && !plin && !efectivo && !tranferencia) {
            Toast.makeText(
                this,
                "seleciona un metodo de pago para crear la referencia",
                Toast.LENGTH_SHORT
            ).show()
            return
        } else {
            if (BotomSheetDialogMetodosPagoBinding.nombreReferenciaED.text.toString().isEmpty()) {
                BotomSheetDialogMetodosPagoBinding.nombreReferenciaED.error =
                    "El nombre del metodo de entrega es obligatorio"
                BotomSheetDialogMetodosPagoBinding.nombreReferenciaED.requestFocus()
                return
            } else {
                val db = FirebaseFirestore.getInstance().collection("Trabajadores_Usuarios_Drivers")
                    .document("trabajadores").collection("trabajadores")
                    .document(firebaseAuth.uid.toString())
                    .collection("metodos_pago")

                val hashMap = hashMapOf<String, Any>(
                    "yape" to yape,
                    "efectivo" to efectivo,
                    "plin" to plin,
                    "transferenia" to tranferencia,
                    "nombre_metodo" to nombre_metodo
                )
                verificarNombreDisponible(
                    db,
                    BotomSheetDialogMetodosPagoBinding.nombreReferenciaED
                ) { res ->
                    if (res) {
                        BotomSheetDialogMetodosPagoBinding.layoutRecicleTexview.isVisible = false
                        BotomSheetDialogMetodosPagoBinding.cargandoMetodosPago.isVisible = true
                        db.add(hashMap).addOnSuccessListener { documentRef ->
                            val id = documentRef.id
                            val idMap = hashMapOf<String, Any>(
                                "id" to id
                            )
                            documentRef.set(idMap, SetOptions.merge()).addOnSuccessListener {
                                val endTime = System.currentTimeMillis() // ⏱ Fin de medición
                                val elapsedTime = endTime - startTime
                                Handler(Looper.getMainLooper()).postDelayed({
                                    BotomSheetDialogMetodosPagoBinding.layoutRecicleTexview.isVisible =
                                        true
                                    BotomSheetDialogMetodosPagoBinding.cargandoMetodosPago.isVisible =
                                        false
                                    Log.d(
                                        "TIEMPO_FIRESTORE",
                                        "Referencia creada en $elapsedTime ms"
                                    )
                                    Toast.makeText(
                                        this,
                                        "Referencia creada correctamente",
                                        Toast.LENGTH_SHORT
                                    )
                                        .show()
                                    BotomSheetDialogMetodosPagoBinding.checkEfectivo.isChecked =
                                        false
                                    BotomSheetDialogMetodosPagoBinding.checkTransferencia.isChecked =
                                        false
                                    BotomSheetDialogMetodosPagoBinding.checkPlin.isChecked = false
                                    BotomSheetDialogMetodosPagoBinding.checkYape.isChecked = false
                                    BotomSheetDialogMetodosPagoBinding.nombreReferenciaED.setText("")
                                    obtner_Metodos_pagosCreados(BotomSheetDialogMetodosPagoBinding)
                                }, elapsedTime)


                            }
                        }.addOnFailureListener { e ->
                            Log.e("ERROR SUBIR", "Error al subir la referencia: $e")
                        }
                    }
                }

            }
        }


    }


    private fun obtner_Metodos_pagosCreados(
        binding: BotomSheetDialogMetodosPagoBinding
    ) {
        val db = FirebaseFirestore.getInstance().collection("Trabajadores_Usuarios_Drivers")
            .document("trabajadores").collection("trabajadores")
            .document(firebaseAuth.uid.toString())
            .collection("metodos_pago")

        lista.clear()
        binding.cargandoMetodosPago.isVisible = true
        binding.textoSinMetodos.isVisible = false
        binding.recicleViewMetodosPagos.isVisible = false

        val tiempoInicio = System.currentTimeMillis()

        db.get().addOnSuccessListener { res ->
            for (datos in res) {
                val data = datos.data
                val yape = data["yape"] as? Boolean ?: false
                val efectivo = data["efectivo"] as? Boolean ?: false
                val tranferencia = data["transferenia"] as? Boolean ?: false
                val plin = data["plin"] as? Boolean ?: false
                val nombre_colecion = data["nombre_metodo"] as? String ?: ""
                val id = data["id"] as? String ?: ""

                val metodo = dataclass_metodos_pagos(
                    yape, efectivo, plin, tranferencia, nombre_colecion, id
                )
                lista.add(metodo)
            }

            val tiempoFin = System.currentTimeMillis()
            val delay = (tiempoFin - tiempoInicio)

            Handler(Looper.getMainLooper()).postDelayed({
                binding.cargandoMetodosPago.isVisible = false

                if (lista.isNotEmpty()) {
                    inicializarRecicleViewPagos(binding)
                    binding.textoSinMetodos.isVisible = false
                    binding.recicleViewMetodosPagos.isVisible = true
                } else {
                    binding.textoSinMetodos.isVisible = true
                    binding.recicleViewMetodosPagos.isVisible = false
                }
            }, delay)
        }
    }

    private fun incializar_recicle_entrega(bottoSheet_entrega: BottomSheeetMetodoEntregaBinding) {
        val recicles = bottoSheet_entrega.recicleViewMetodosEntrega
        recicles.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, true)
        recicles.adapter = adapter_metodos_entrega(lista_entrega, { selecionado ->
            eliminarReferenciaEntregaSelect(
                selecionado.id.toString(),
                bottoSheet_entrega
            )
        }, { editado ->
            editarCambiosEntrega(editado.id.toString(), bottoSheet_entrega)
        })
    }

    private fun inicializarRecicleViewPagos(BotomSheetDialogMetodosPagoBinding: BotomSheetDialogMetodosPagoBinding) {
        val recicles = BotomSheetDialogMetodosPagoBinding.recicleViewMetodosPagos
        recicles.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, true)
        recicles.adapter = adapter_metodos_pagos(lista, { selecionado ->
            eliminarReferenciaPagoSelect(
                selecionado.id.toString(),
                BotomSheetDialogMetodosPagoBinding
            )
        }, { editado ->
            editarCambiosPAgos(editado.id.toString(), BotomSheetDialogMetodosPagoBinding)
        })
    }

    private fun editarCambiosPAgos(
        selecionado: String,
        BotomSheetDialogMetodosPagoBinding: BotomSheetDialogMetodosPagoBinding
    ) {

        val db = FirebaseFirestore.getInstance().collection("Trabajadores_Usuarios_Drivers")
            .document("trabajadores").collection("trabajadores")
            .document(firebaseAuth.uid.toString())
            .collection("metodos_pago").document(selecionado)
        db.get().addOnSuccessListener { res ->
            if (res.exists()) {
                val data = res.data
                val yape = data?.get("yape") as? Boolean ?: false
                val efectivo = data?.get("efectivo") as? Boolean ?: false
                val tranferencia = data?.get("transferenia") as? Boolean ?: false
                val plin = data?.get("plin") as? Boolean ?: false
                val nombre_colecion = data?.get("nombre_metodo") as? String ?: ""
                val id = data?.get("id") as? String ?: ""

                BotomSheetDialogMetodosPagoBinding.nombreReferenciaED.setText(nombre_colecion)
                BotomSheetDialogMetodosPagoBinding.checkEfectivo.isChecked = efectivo
                BotomSheetDialogMetodosPagoBinding.checkTransferencia.isChecked = tranferencia
                BotomSheetDialogMetodosPagoBinding.checkPlin.isChecked = plin
                BotomSheetDialogMetodosPagoBinding.checkYape.isChecked = yape

                BotomSheetDialogMetodosPagoBinding.CrearMetodo.isVisible = false
                BotomSheetDialogMetodosPagoBinding.GuardarCambios.isVisible = true
                BotomSheetDialogMetodosPagoBinding.nombreReferencia.isEnabled = false
                BotomSheetDialogMetodosPagoBinding.nombreReferenciaED.isEnabled = false
                BotomSheetDialogMetodosPagoBinding.GuardarCambios.setOnClickListener {
                    val hashMap = hashMapOf<String, Any>(
                        "yape" to BotomSheetDialogMetodosPagoBinding.checkYape.isChecked,
                        "efectivo" to BotomSheetDialogMetodosPagoBinding.checkEfectivo.isChecked,
                        "plin" to BotomSheetDialogMetodosPagoBinding.checkPlin.isChecked,
                        "transferenia" to BotomSheetDialogMetodosPagoBinding.checkTransferencia.isChecked,
                    )
                    db.set(hashMap, SetOptions.merge()).addOnSuccessListener {
                        Toast.makeText(
                            this,
                            "Cambios guardados correctamente",
                            Toast.LENGTH_SHORT
                        ).show()
                        BotomSheetDialogMetodosPagoBinding.nombreReferenciaED.setText("")
                        BotomSheetDialogMetodosPagoBinding.nombreReferencia.isEnabled = true
                        BotomSheetDialogMetodosPagoBinding.nombreReferenciaED.isEnabled = true
                        BotomSheetDialogMetodosPagoBinding.checkEfectivo.isChecked = false
                        BotomSheetDialogMetodosPagoBinding.checkTransferencia.isChecked = false
                        BotomSheetDialogMetodosPagoBinding.checkPlin.isChecked = false
                        BotomSheetDialogMetodosPagoBinding.checkYape.isChecked = false
                        BotomSheetDialogMetodosPagoBinding.CrearMetodo.isVisible = true
                        BotomSheetDialogMetodosPagoBinding.GuardarCambios.isVisible = false
                        obtner_Metodos_pagosCreados(BotomSheetDialogMetodosPagoBinding)
                    }.addOnFailureListener { e ->
                        Log.d("error guardado", "error al guardar losd atos $e")
                    }


                }
            }
        }.addOnFailureListener { e ->
            Log.e("error", "no se econtro la referencia $e")
        }
    }

    private fun editarCambiosEntrega(
        selecionado: String,
        bottoSheet_entrega: BottomSheeetMetodoEntregaBinding,
    ) {

        val db = FirebaseFirestore.getInstance().collection("Trabajadores_Usuarios_Drivers")
            .document("trabajadores").collection("trabajadores")
            .document(firebaseAuth.uid.toString())
            .collection("metodos_entrega").document(selecionado)
        db.get().addOnSuccessListener { res ->
            if (res.exists()) {

                val data = res.data
                val delivery = data?.get("delivery") as? Boolean ?: false
                val coordinar = data?.get("coordinar") as? Boolean ?: false
                val lugaresEntrega = data?.get("lugaresEntrega") as? Boolean ?: false
                val retiroTienda = data?.get("retiroTienda") as? Boolean ?: false
                val envioCourier = data?.get("envioCourier") as? Boolean ?: false
                val entregaProgramada = data?.get("entregaProgramada") as? Boolean ?: false
                val nombre_metodo = data?.get("nombre_metodo") as? String ?: ""

                val datosDelivery = data?.get("datos_delivery") as? Map<*, *>
                val datosRetiroTienda = data?.get("datos_retiro_tienda") as? Map<*, *>
                val datosLugaresEntrega = data?.get("datos_lugares_entrega") as? Map<*, *>

                bottoSheet_entrega.nombreReferenciaED.isEnabled = false
                bottoSheet_entrega.nombreReferencia.isEnabled = false
                bottoSheet_entrega.nombreReferenciaED.setText(nombre_metodo)
                bottoSheet_entrega.delivery.isChecked = delivery
                bottoSheet_entrega.corrdinar.isChecked = coordinar
                bottoSheet_entrega.lugaresEntrega.isChecked = lugaresEntrega
                bottoSheet_entrega.retiroTienda.isChecked = retiroTienda
                bottoSheet_entrega.envioCourier.isChecked = envioCourier
                bottoSheet_entrega.entregaProgramada.isChecked = entregaProgramada
                if (datosDelivery != null && datosDelivery is Map<*, *>) {
                    val deliveryGratis = datosDelivery["gratis"] as? Boolean ?: false
                    if (deliveryGratis) {
                        bottoSheet_entrega.si.isChecked = true
                    } else {
                        bottoSheet_entrega.no.isChecked = true
                    }
                }
                if (datosRetiroTienda != null) {
                    val localidadRetiro = datosRetiroTienda["localidad"] as? String ?: ""
                    val nombreTienda = datosRetiroTienda["nombre_tienda"] as? String ?: ""
                    val referencia = datosRetiroTienda["referencia"] as? String ?: ""

                    bottoSheet_entrega.localidadEntregaPuntosED.setText(localidadRetiro)
                    bottoSheet_entrega.nombreTiendaED.setText(nombreTienda)
                    bottoSheet_entrega.lugarReferenciaPuntoVentaED.setText(referencia)
                } else {
                    Log.d("Firestore", "El campo 'datos_retiro_tienda' no está presente")
                }
                if (datosLugaresEntrega != null) {
                    val descripcionLugaresEntrega =
                        datosLugaresEntrega["descripcion"] as? String ?: ""
                    val localidadLugaresEntrega = datosLugaresEntrega["localidad"] as? String ?: ""

                    bottoSheet_entrega.localidadMetodoEntregaED.setText(localidadLugaresEntrega)
                    bottoSheet_entrega.LugaresEntregaED.setText(descripcionLugaresEntrega)
                } else {
                    Log.d(
                        "Firestore",
                        "El campo 'datos_lugares_entrega' no está presente o es inválido"
                    )
                }
                bottoSheet_entrega.CrearMetodo.isVisible = false
                bottoSheet_entrega.GuardarCambios.isVisible = true

                bottoSheet_entrega.GuardarCambios.setOnClickListener {
                    if (bottoSheet_entrega.delivery.isChecked && bottoSheet_entrega.grupoEnvioGratis.checkedRadioButtonId == -1) {
                        Toast.makeText(
                            this,
                            "Selecciona si el delivery es gratis o no",
                            Toast.LENGTH_SHORT
                        )
                            .show()
                        return@setOnClickListener
                    }
                    if (!(bottoSheet_entrega.delivery.isChecked || bottoSheet_entrega.corrdinar.isChecked || bottoSheet_entrega.lugaresEntrega.isChecked || bottoSheet_entrega.retiroTienda.isChecked || bottoSheet_entrega.envioCourier.isChecked || bottoSheet_entrega.entregaProgramada.isChecked)) {
                        Toast.makeText(
                            this,
                            "Selecciona al menos un método de entrega para continuar",
                            Toast.LENGTH_SHORT
                        ).show()
                        return@setOnClickListener
                    }
                    if (bottoSheet_entrega.lugaresEntrega.isChecked) {
                        val textoLugares = bottoSheet_entrega.LugaresEntregaED
                        val localidadLugares = bottoSheet_entrega.localidadMetodoEntregaED

                        val textoLugaresStr = textoLugares.text.toString().trim()
                        val localidadStr = localidadLugares.text.toString().trim()

                        if (textoLugaresStr.isEmpty()) {
                            textoLugares.error = "Ingresa los lugares de entrega"
                            textoLugares.requestFocus()
                            return@setOnClickListener
                        }

                        if (localidadStr.isEmpty()) {
                            localidadLugares.error = "Ingresa una localidad de entrega"
                            localidadLugares.requestFocus()
                            return@setOnClickListener
                        }
                    }
                    if (bottoSheet_entrega.retiroTienda.isChecked) {
                        val textoPuntoVenta =
                            bottoSheet_entrega.lugarReferenciaPuntoVentaED.text.toString().trim()
                        val localidadPuntoVenta =
                            bottoSheet_entrega.localidadEntregaPuntosED.text.toString().trim()
                        val nombreTienda = bottoSheet_entrega.nombreTiendaED.text.toString().trim()

                        if (textoPuntoVenta.isEmpty()) {
                            bottoSheet_entrega.lugarReferenciaPuntoVentaED.error =
                                "Ingresa el punto de venta o tienda"
                            bottoSheet_entrega.lugarReferenciaPuntoVentaED.requestFocus()
                            return@setOnClickListener
                        }

                        if (localidadPuntoVenta.isEmpty()) {
                            bottoSheet_entrega.localidadEntregaPuntosED.error =
                                "Ingresa la localidad del punto de venta"
                            bottoSheet_entrega.localidadEntregaPuntosED.requestFocus()
                            return@setOnClickListener
                        }

                        if (nombreTienda.isEmpty()) {
                            bottoSheet_entrega.nombreTiendaED.error =
                                "Ingresa el nombre de la tienda"
                            bottoSheet_entrega.nombreTiendaED.requestFocus()
                            return@setOnClickListener
                        }
                    }

                    val hashMap = hashMapOf<String, Any>(
                        "delivery" to bottoSheet_entrega.delivery.isChecked,
                        "coordinar" to bottoSheet_entrega.corrdinar.isChecked,
                        "lugaresEntrega" to bottoSheet_entrega.lugaresEntrega.isChecked,
                        "retiroTienda" to bottoSheet_entrega.retiroTienda.isChecked,
                        "envioCourier" to bottoSheet_entrega.envioCourier.isChecked,
                        "entregaProgramada" to bottoSheet_entrega.entregaProgramada.isChecked,
                    )
                    if (bottoSheet_entrega.delivery.isChecked) {
                        hashMap["datos_delivery"] = hashMapOf("gratis" to deliver_gratis)
                    }
                    if (bottoSheet_entrega.lugaresEntrega.isChecked) {
                        val lugarTexto = bottoSheet_entrega.LugaresEntregaED.text.toString().trim()
                        hashMap["datos_lugares_entrega"] = hashMapOf(
                            "localidad" to bottoSheet_entrega.localidadMetodoEntregaED.text.toString(),
                            "descripcion" to lugarTexto
                        )
                    } else {
                        bottoSheet_entrega.LugaresEntregaED.setText("")
                        bottoSheet_entrega.localidadMetodoEntregaED.setText("")
                    }
                    if (bottoSheet_entrega.retiroTienda.isChecked) {
                        val puntoVentaTexto =
                            bottoSheet_entrega.lugarReferenciaPuntoVentaED.text.toString().trim()
                        hashMap["datos_retiro_tienda"] = hashMapOf(
                            "localidad" to bottoSheet_entrega.localidadEntregaPuntosED.text.toString(),
                            "referencia" to puntoVentaTexto,
                            "nombre_tienda" to bottoSheet_entrega.nombreTiendaED.text.toString()
                        )
                    } else {
                        bottoSheet_entrega.localidadEntregaPuntosED.setText("")
                        bottoSheet_entrega.nombreTiendaED.setText("")
                        bottoSheet_entrega.lugarReferenciaPuntoVentaED.setText("")
                    }
                    db.set(hashMap, SetOptions.merge()).addOnSuccessListener {
                        Toast.makeText(
                            this,
                            "Cambios guardados correctamente",
                            Toast.LENGTH_SHORT
                        ).show()
                        bottoSheet_entrega.delivery.isChecked = false
                        bottoSheet_entrega.corrdinar.isChecked = false
                        bottoSheet_entrega.lugaresEntrega.isChecked = false
                        bottoSheet_entrega.retiroTienda.isChecked = false
                        bottoSheet_entrega.envioCourier.isChecked = false
                        bottoSheet_entrega.entregaProgramada.isChecked = false
                        bottoSheet_entrega.nombreReferenciaED.setText("")
                        bottoSheet_entrega.CrearMetodo.isVisible = true
                        bottoSheet_entrega.GuardarCambios.isVisible = false
                        bottoSheet_entrega.nombreReferencia.isEnabled = true
                        bottoSheet_entrega.nombreReferenciaED.isEnabled = true
                        obtenerMetodosEntrega(bottoSheet_entrega)
                    }.addOnFailureListener { e ->
                        Log.d("error guardado", "error al guardar losd atos $e")
                    }


                }
            }
        }
    }

    private fun eliminarReferenciaPagoSelect(
        selecionado: String,
        binding: BotomSheetDialogMetodosPagoBinding
    ) {
        val context = binding.root.context

        AlertDialog.Builder(context)
            .setTitle("Eliminar método de pago")
            .setMessage("¿Estás seguro de que deseas eliminar este método de pago?")
            .setPositiveButton("Sí") { dialog, _ ->
                val db = FirebaseFirestore.getInstance()
                    .collection("Trabajadores_Usuarios_Drivers")
                    .document("trabajadores")
                    .collection("trabajadores")
                    .document(firebaseAuth.uid.toString())
                    .collection("metodos_pago")
                    .document(selecionado)

                db.delete()
                    .addOnSuccessListener {
                        Toast.makeText(
                            context,
                            "Método de entrega eliminado correctamente",
                            Toast.LENGTH_SHORT
                        ).show()
                        obtner_Metodos_pagosCreados(binding)
                    }
                    .addOnFailureListener { e ->
                        Log.e("error_eliminar", "Error al eliminar el método de entrega: $e")
                    }
                dialog.dismiss()
            }
            .setNegativeButton("No") { dialog, _ ->
                dialog.dismiss() // solo se cierra el diálogo sin hacer nada
            }
            .show()
    }

    private fun eliminarReferenciaEntregaSelect(
        selecionado: String,
        bottoSheet_entrega: BottomSheeetMetodoEntregaBinding
    ) {
        val context = bottoSheet_entrega.root.context

        AlertDialog.Builder(context)
            .setTitle("Eliminar método de entrega")
            .setMessage("¿Estás seguro de que deseas eliminar este método de entrega?")
            .setPositiveButton("Sí") { dialog, _ ->
                val db = FirebaseFirestore.getInstance()
                    .collection("Trabajadores_Usuarios_Drivers")
                    .document("trabajadores")
                    .collection("trabajadores")
                    .document(firebaseAuth.uid.toString())
                    .collection("metodos_entrega")
                    .document(selecionado)

                db.delete()
                    .addOnSuccessListener {
                        Toast.makeText(
                            context,
                            "Método de entrega eliminado correctamente",
                            Toast.LENGTH_SHORT
                        ).show()
                        obtenerMetodosEntrega(bottoSheet_entrega)
                    }
                    .addOnFailureListener { e ->
                        Log.e("error_eliminar", "Error al eliminar el método de pago: $e")
                    }
                dialog.dismiss()
            }
            .setNegativeButton("No") { dialog, _ ->
                dialog.dismiss() // solo se cierra el diálogo sin hacer nada
            }
            .show()
    }

    private fun bottomSheet_numero_nombre_pagos() {
        val binding_bottom = BottomSheetNumeroNombrePagoBinding.inflate(LayoutInflater.from(this))
        val view = binding_bottom.root
        binding_bottom.checkYape.setOnCheckedChangeListener { _, isChecked ->

            binding_bottom.metodoYape.linealMetodosPagos.isVisible = isChecked
            yape = isChecked
            binding_bottom.metodoYape.numeroYapePlinCuentaED.hint = "Numero de yape"
            binding_bottom.metodoYape.tituloMetodoEntrega.text = "Campos de yape"
            obtenernumero_nombreSet(
                "yape",
                binding_bottom.metodoYape.nombreYapePlinCuentaED,
                binding_bottom.metodoYape.numeroYapePlinCuentaED, binding_bottom
            ) { existe ->
                if (!existe) {
                    binding_bottom.metodoYape.linealcamposnombreEtc.isVisible = true
                    binding_bottom.metodoYape.cargaContenido.isVisible = false
                }
            }

        }

        binding_bottom.checkPlin.setOnCheckedChangeListener { _, isChecked ->

            binding_bottom.metodoPlin.linealMetodosPagos.isVisible = isChecked
            plin = isChecked
            binding_bottom.metodoPlin.numeroYapePlinCuentaED.hint = "Numero de Plin"
            binding_bottom.metodoPlin.tituloMetodoEntrega.text = "Campos de Plin"
            obtenernumero_nombreSet(
                "plin",
                binding_bottom.metodoPlin.nombreYapePlinCuentaED,
                binding_bottom.metodoPlin.numeroYapePlinCuentaED, binding_bottom
            ) { existe ->
                if (!existe) {
                    binding_bottom.metodoPlin.linealcamposnombreEtc.isVisible = true
                    binding_bottom.metodoPlin.cargaContenido.isVisible = false
                }
            }
        }

        binding_bottom.checkTransferencia.setOnCheckedChangeListener { _, isChecked ->

            binding_bottom.metodoTransferencia.linealMetodosPagos.isVisible = isChecked
            trasnferecnia = isChecked
            binding_bottom.metodoTransferencia.numeroYapePlinCuentaED.hint =
                "Numero de cuenta o cci"
            binding_bottom.metodoTransferencia.tituloMetodoEntrega.text =
                "Campos de Transfercias"
            obtenernumero_nombreSet(
                "transferencia",
                binding_bottom.metodoTransferencia.nombreYapePlinCuentaED,
                binding_bottom.metodoTransferencia.numeroYapePlinCuentaED, binding_bottom
            ) { existe ->
                if (!existe) {
                    binding_bottom.metodoTransferencia.linealcamposnombreEtc.isVisible = true
                    binding_bottom.metodoTransferencia.cargaContenido.isVisible = false
                }
            }
        }

        binding_bottom.metodoYape.guardadCambio.setOnClickListener {
            agregar_numero_nombre_pago(
                "yape",
                binding_bottom.metodoYape.nombreYapePlinCuentaED.text.toString(),
                binding_bottom.metodoYape.numeroYapePlinCuentaED.text.toString()
            )
        }
        binding_bottom.metodoPlin.guardadCambio.setOnClickListener {
            agregar_numero_nombre_pago(
                "plin",
                binding_bottom.metodoPlin.nombreYapePlinCuentaED.text.toString(),
                binding_bottom.metodoPlin.numeroYapePlinCuentaED.text.toString()
            )
        }
        binding_bottom.metodoTransferencia.guardadCambio.setOnClickListener {
            agregar_numero_nombre_pago(
                "transferencia",
                binding_bottom.metodoTransferencia.nombreYapePlinCuentaED.text.toString(),
                binding_bottom.metodoTransferencia.numeroYapePlinCuentaED.text.toString()
            )
        }

        dialog.setContentView(view)
    }

    private fun obtenernumero_nombreSet(
        tipo_pago: String,
        nombreED: EditText,
        numeroED: EditText,
        BottomSheetNumeroNombrePagoBinding: BottomSheetNumeroNombrePagoBinding,
        callback: (Boolean) -> Unit
    ) {
        val db = FirebaseFirestore.getInstance().collection("Trabajadores_Usuarios_Drivers")
            .document("trabajadores").collection("trabajadores")
            .document(firebaseAuth.uid.toString())
            .collection("numero_nombre_pago").document(tipo_pago)

        val start = System.currentTimeMillis()

        db.get().addOnSuccessListener { res ->
            val duration = System.currentTimeMillis() - start

            if (res.exists()) {
                val data = res.data
                val nombre = data?.get("nombre_numero") as? String ?: ""
                val numero = data?.get("numero") ?: ""

                nombreED.setText(nombre)
                numeroED.setText(numero.toString())

                Handler(Looper.getMainLooper()).postDelayed({
                    when (tipo_pago) {
                        "yape" -> {
                            BottomSheetNumeroNombrePagoBinding.metodoYape.linealcamposnombreEtc.isVisible =
                                true
                            BottomSheetNumeroNombrePagoBinding.metodoYape.cargaContenido.isVisible =
                                false
                        }

                        "plin" -> {
                            BottomSheetNumeroNombrePagoBinding.metodoPlin.linealcamposnombreEtc.isVisible =
                                true
                            BottomSheetNumeroNombrePagoBinding.metodoPlin.cargaContenido.isVisible =
                                false
                        }

                        "transferencia" -> {
                            BottomSheetNumeroNombrePagoBinding.metodoTransferencia.linealcamposnombreEtc.isVisible =
                                true
                            BottomSheetNumeroNombrePagoBinding.metodoTransferencia.cargaContenido.isVisible =
                                false
                        }
                    }
                    callback(true) // referencia existe
                }, duration)

            } else {
                callback(false) // no existe
            }
        }.addOnFailureListener {
            callback(false) // error al acceder
        }
    }


    private fun agregar_numero_nombre_pago(
        tipo_pago: String,
        nombre_numero: String, numero: String
    ) {
        val db = FirebaseFirestore.getInstance().collection("Trabajadores_Usuarios_Drivers")
            .document("trabajadores").collection("trabajadores")
            .document(firebaseAuth.uid.toString())
            .collection("numero_nombre_pago").document(tipo_pago)
        val hashMap = hashMapOf<String, Any>(
            "nombre_pago" to tipo_pago,
            "nombre_numero" to nombre_numero,
            "numero" to numero
        )
        db.set(hashMap, SetOptions.merge()).addOnSuccessListener { res ->
            Toast.makeText(this, "Campos guardado correctamente", Toast.LENGTH_SHORT).show()
        }.addOnFailureListener { e ->
            Log.d("error_guardad", "error al guardar $e")
        }
    }

    private fun setear_datos_includes() {
        val plan = intent.getStringExtra(Variables.plan)
        binding.traajosRecientes.tituloServico.text = "Trabajos Recientes"
        binding.publicaciones.tituloServico.text = "Publicaciones"
        binding.productosVenta.tituloServico.text = "Productos en venta"

        obtenerTrabajosRecientes("trabajos_realizados", "publicados", { valor ->
            val trabajos_activos =
                SpannableString("Trabajos activos : ${valor}")
            constantestextos_general.setearInformacionboldDescripcion(
                "Trabajos activos",
                trabajos_activos, binding.traajosRecientes.activos
            )
        })
        obtenerTrabajosRecientes("publicaciones_trabajos", "publicados", { valor ->
            val activas_trabajos =
                SpannableString("Publicaciones activas : ${valor}")
            constantestextos_general.setearInformacionboldDescripcion(
                "Publicaciones activas",
                activas_trabajos, binding.publicaciones.activos
            )
        })



        obtenerTrabajosRecientes("productos_venta", "publicados", { valor ->
            val productos_activos =
                SpannableString("Productos activos : ${valor}")
            constantestextos_general.setearInformacionboldDescripcion(
                "Productos activos",
                productos_activos, binding.productosVenta.activos
            )
        })


        val imageView = binding.traajosRecientes.imgServicio
        imageView.setImageResource(R.drawable.crea_publicaciones)
        val imageView2 = binding.publicaciones.imgServicio
        imageView2.setImageResource(R.drawable.agregar_trabajos)
        val imageView3 = binding.productosVenta.imgServicio
        imageView3.setImageResource(R.drawable.agrega_productos_perfil)



        imageView.setOnClickListener {
            var vista = Intent(this, crear_trabajos_realizados::class.java).apply {
                putExtra(Variables.plan, plan)
            }
            startActivity(vista)


        }
        imageView2.setOnClickListener {
            var vista = Intent(this, crear_publicaciones_recientes::class.java).apply {
                putExtra(Variables.plan, plan)
            }
            startActivity(vista)

        }

        imageView3.setOnClickListener {
            var vista = Intent(this, crear_publicacion_productos_trabajadores::class.java).apply {
                putExtra(Variables.plan, plan)
            }
            startActivity(vista)
        }

    }

    private fun obtenerTrabajosRecientes(col1: String, col: String, callback: (String) -> Unit) {
        val db = FirebaseFirestore.getInstance()
            .collection("Trabajadores_Usuarios_Drivers")
            .document("trabajadores")
            .collection("trabajadores")
            .document(firebaseAuth.uid.toString())
            .collection(col1).document(col).collection(col)

        db.get().addOnSuccessListener { res ->
            val cantidadPublicadas = res.size()
            callback(cantidadPublicadas.toString())
        }.addOnFailureListener {
            callback("No se encontraron")
        }
    }


}