package com.example.geinzwork.fragmentos

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.geinzwork.adapterViewholder.adapter_metodos_pagos
import com.example.geinzwork.constantesGeneral.Variables
import com.example.geinzwork.crear_publicacion_productos_trabajadores
import com.example.geinzwork.crear_publicaciones_recientes
import com.example.geinzwork.dataclass.dataclass_metodos_pagos
import com.geinzz.geinzwork.R
import com.geinzz.geinzwork.adapterViewholder.adapterReportes
import com.geinzz.geinzwork.crear_trabajos_realizados
import com.geinzz.geinzwork.databinding.ActivityPanelPublicacionTrabajadorBinding
import com.geinzz.geinzwork.databinding.BotomSheetDialogMetodosPagoBinding
import com.geinzz.geinzwork.databinding.BottomSheeetMetodoEntregaBinding
import com.geinzz.geinzwork.databinding.BottomSheetNumeroNombrePagoBinding
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions

class panel_publicacion_trabajador : AppCompatActivity() {
    private lateinit var binding: ActivityPanelPublicacionTrabajadorBinding
    private val lista = mutableListOf<dataclass_metodos_pagos>()
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
        if (delivery && bottosheetEntrega.grupoEnvioGratis.checkedRadioButtonId == -1) {
            Toast.makeText(this, "Selecciona si el delivery es gratis o no", Toast.LENGTH_SHORT).show()
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
            val textoPuntoVenta = bottosheetEntrega.lugarReferenciaPuntoVentaED.text.toString().trim()
            val localidadPuntoVenta = bottosheetEntrega.localidadEntregaPuntosED.text.toString().trim()
            val nombreTienda = bottosheetEntrega.nombreTiendaED.text.toString().trim()

            if (textoPuntoVenta.isEmpty()) {
                bottosheetEntrega.lugarReferenciaPuntoVentaED.error = "Ingresa el punto de venta o tienda"
                bottosheetEntrega.lugarReferenciaPuntoVentaED.requestFocus()
                return
            }

            if (localidadPuntoVenta.isEmpty()) {
                bottosheetEntrega.localidadEntregaPuntosED.error = "Ingresa la localidad del punto de venta"
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
            val puntoVentaTexto = bottosheetEntrega.lugarReferenciaPuntoVentaED.text.toString().trim()
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

        bottosheetEntrega.layoutRecicleTexview.isVisible = false
        bottosheetEntrega.cargandoMetodosPago.isVisible = true

        db.add(hashMap).addOnSuccessListener { documentReference ->
            val idGenerado = documentReference.id
            val hashMap = hashMapOf<String, Any>("id" to idGenerado)

            db.document(idGenerado).set(hashMap, SetOptions.merge()).addOnSuccessListener {
                Toast.makeText(this, "Método de entrega creado correctamente", Toast.LENGTH_SHORT).show()
                bottosheetEntrega.cargandoMetodosPago.isVisible = false

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
            }
        }.addOnFailureListener { e ->
            Log.d("error_crear", "error al crear una referencia: ${e.message}")
            bottosheetEntrega.cargandoMetodosPago.isVisible = false
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
        BotomSheetDialogMetodosPagoBinding.layoutRecicleTexview.isVisible = false
        BotomSheetDialogMetodosPagoBinding.cargandoMetodosPago.isVisible = true

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

        db.add(hashMap).addOnSuccessListener { documentRef ->
            val id = documentRef.id
            val idMap = hashMapOf<String, Any>(
                "id" to id
            )
            documentRef.set(idMap, SetOptions.merge()).addOnSuccessListener {
                val endTime = System.currentTimeMillis() // ⏱ Fin de medición
                val elapsedTime = endTime - startTime
                Handler(Looper.getMainLooper()).postDelayed({
                    BotomSheetDialogMetodosPagoBinding.layoutRecicleTexview.isVisible = true
                    BotomSheetDialogMetodosPagoBinding.cargandoMetodosPago.isVisible = false
                    Log.d("TIEMPO_FIRESTORE", "Referencia creada en $elapsedTime ms")
                    Toast.makeText(this, "Referencia creada correctamente", Toast.LENGTH_SHORT)
                        .show()
                    BotomSheetDialogMetodosPagoBinding.checkEfectivo.isChecked = false
                    BotomSheetDialogMetodosPagoBinding.checkTransferencia.isChecked = false
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

    private fun inicializarRecicleViewPagos(BotomSheetDialogMetodosPagoBinding: BotomSheetDialogMetodosPagoBinding) {
        val recicles = BotomSheetDialogMetodosPagoBinding.recicleViewMetodosPagos
        recicles.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, true)
        recicles.adapter = adapter_metodos_pagos(lista, { selecionado ->
            eliminarReferenciaPagoSelect(
                selecionado.id.toString(),
                BotomSheetDialogMetodosPagoBinding
            )
        }, { editado ->
            editarCambios(editado.id.toString(), BotomSheetDialogMetodosPagoBinding)
        })
    }

    private fun editarCambios(
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
                        BotomSheetDialogMetodosPagoBinding.checkEfectivo.isChecked = false
                        BotomSheetDialogMetodosPagoBinding.checkTransferencia.isChecked = false
                        BotomSheetDialogMetodosPagoBinding.checkPlin.isChecked = false
                        BotomSheetDialogMetodosPagoBinding.checkYape.isChecked = false
                        BotomSheetDialogMetodosPagoBinding.nombreReferenciaED.setText("")
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
                            "Método de pago eliminado correctamente",
                            Toast.LENGTH_SHORT
                        ).show()
                        obtner_Metodos_pagosCreados(binding)
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
                    binding_bottom.metodoYape.linealcamposnombreEtc.isVisible = true
                    binding_bottom.metodoYape.cargaContenido.isVisible = false
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
                    binding_bottom.metodoYape.linealcamposnombreEtc.isVisible = true
                    binding_bottom.metodoYape.cargaContenido.isVisible = false
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
        binding.traajosRecientes.fechaActivotxt.text = "Publicaciones activas"
        binding.traajosRecientes.fechatxtVenimiento.text = "Publicaciones restantes"
        binding.productosVenta.fechaActivotxt.text = "Publicaciones activas"
        binding.productosVenta.fechatxtVenimiento.text = "Publicaciones restantes"
        binding.publicaciones.fechaActivotxt.text = "Publicaciones activas"
        binding.publicaciones.fechatxtVenimiento.text = "Publicaciones restantes"

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


}