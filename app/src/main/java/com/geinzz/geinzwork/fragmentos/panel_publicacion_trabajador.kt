package com.geinzz.geinzwork.fragmentos

import android.content.ContentValues.TAG
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.SpannableString
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.PopupMenu
import android.widget.ProgressBar
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import com.geinzz.geinzwork.ui.adapters.adapter_metodos_entrega
import com.geinzz.geinzwork.utils.constantes.constantes.PinShortcut_general
import com.geinzz.geinzwork.utils.constantes.constantes.Variables
import com.geinzz.geinzwork.utils.constantes.constantes.constantes_metodo_pago_entrega
import com.geinzz.geinzwork.utils.constantes.constantes.constantes_metodo_pago_entrega.verificar_Estado_metodo_pago
import com.geinzz.geinzwork.crear_publicacion_productos_trabajadores
import com.geinzz.geinzwork.crear_publicaciones_recientes
import com.geinzz.geinzwork.model.dataclass_metodos_entrega
import com.geinzz.geinzwork.model.dataclass_metodos_pagos
import com.geinzz.geinzwork.Login
import com.geinzz.geinzwork.R
import com.geinzz.geinzwork.utils.constantes.constantes.constantestextos_general
import com.geinzz.geinzwork.crear_trabajos_realizados
import com.geinzz.geinzwork.databinding.ActivityPanelPublicacionTrabajadorBinding
import com.geinzz.geinzwork.databinding.BotomSheetDialogMetodosPagoBinding
import com.geinzz.geinzwork.databinding.BottomSheeetMetodoEntregaBinding
import com.geinzz.geinzwork.databinding.BottomSheetAgregarRedesBinding
import com.geinzz.geinzwork.databinding.BottomSheetNumeroNombrePagoBinding
import com.geinzz.geinzwork.ver_publicaciones
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.auth.FirebaseAuth
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

        if (firebaseAuth.currentUser != null) {
            Toast.makeText(this, "entramos a la actividad", Toast.LENGTH_SHORT).show()
            setear_datos_includes()
            binding.menuAccesoDirecto.setOnClickListener {
                popup()
            }
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
            binding.sinRegistro.isVisible = false
            binding.scroolView.isVisible = true
        } else {
            binding.sinRegistro.isVisible = true
            binding.scroolView.isVisible = false
            binding.iniciarSeccion.setOnClickListener {
                val vista = Intent(this, Login::class.java).apply {
                    putExtra("dato", "panel")
                }
                startActivity(vista)
            }
        }

    }

    override fun onResume() {
        super.onResume()

        if (firebaseAuth.currentUser != null) {
            Toast.makeText(this, "entramos a la actividad", Toast.LENGTH_SHORT).show()
            setear_datos_includes()
            if (binding.menuAccesoDirecto.hasOnClickListeners().not()) {
                binding.menuAccesoDirecto.setOnClickListener { popup() }
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

            binding.sinRegistro.isVisible = false
            binding.scroolView.isVisible = true

        } else {
            binding.sinRegistro.isVisible = true
            binding.scroolView.isVisible = false

            binding.iniciarSeccion.setOnClickListener {
                val vista = Intent(this, Login::class.java).apply {
                    putExtra("dato", "panel")
                }
                startActivity(vista)
            }
        }

    }


    private fun popup() {
        val popup = PopupMenu(this, binding.menuAccesoDirecto)

        // Agregar opciones al menú
        popup.menu.add(Menu.NONE, 1, 1, "Crear acceso directo")


        // Mostrar el popup
        popup.show()

        // Manejar clics en los ítems del menú
        popup.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                1 -> {
                    PinShortcut_general.panel_publicacion_trabajador_accesoDirecto_panel(this)
                    true
                }

                else -> true
            }
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

    private fun bottomSheet_metodos_pago() {
        val binding_bottomSheet =
            BotomSheetDialogMetodosPagoBinding.inflate(LayoutInflater.from(this))
        binding_bottomSheet.cerrar.setOnClickListener { dialog.dismiss() }

        val view = binding_bottomSheet.root
        constantes_metodo_pago_entrega.obtner_Metodos_pagosCreados(
            dialog,
            this,
            binding_bottomSheet
        )

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
            constantes_metodo_pago_entrega.agregar_Referencia(
                yape,
                plin,
                efectivo,
                trasnferecnia,
                binding_bottomSheet.nombreReferenciaED.text.toString(),
                binding_bottomSheet,
                this,
                dialog, { parants() }
            )

        }

        dialog.setContentView(view)

    }
    fun parants(){}

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
        constantes_metodo_pago_entrega.verificarNombreDisponible(
            this,
            db,
            bottosheetEntrega.nombreReferenciaED
        ) { res ->
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
                    incializar_recicle_entrega("todos", bottoSheet_entrega, "")
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

    private fun obtenerMetodosEntrega_menos_eliminado(
        bottoSheet_entrega: BottomSheeetMetodoEntregaBinding,
        id_selecionado: String
    ) {
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
                if (id != id_selecionado) {
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


            }
            val tiempoFin = System.currentTimeMillis()
            val delay = (tiempoFin - tiempoInicio)
            Handler(Looper.getMainLooper()).postDelayed({
                bottoSheet_entrega.cargandoMetodosPago.isVisible = false
                if (lista_entrega.isNotEmpty()) {
                    incializar_recicle_entrega("filtrado", bottoSheet_entrega, id_selecionado)
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


    private fun incializar_recicle_entrega(
        tipo_encontrado: String,
        bottoSheet_entrega: BottomSheeetMetodoEntregaBinding, idAnterior: String
    ) {
        val recicles = bottoSheet_entrega.recicleViewMetodosEntrega
        recicles.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, true)
        recicles.adapter = adapter_metodos_entrega(lista_entrega, { selecionado ->
            eliminarReferenciaEntregaSelect(
                lista_entrega.size,
                selecionado.id.toString(),
                bottoSheet_entrega
            )
        }, { editado ->
            when (tipo_encontrado) {
                "todos" -> {
                    editarCambiosEntrega(editado.id.toString(), bottoSheet_entrega)
                }

                "filtrado" -> {
                    pasar_metodos_pagosNuevo(
                        bottoSheet_entrega,
                        editado.id.toString(),
                        idAnterior,
                        dialog,
                        "metodos_entrega"
                    )
                }
            }

        })
    }

    private fun pasar_metodos_pagosNuevo(
        binding: BottomSheeetMetodoEntregaBinding,
        actual_select: String,
        idAnterior: String, dialog: BottomSheetDialog, metodo_pago_o_entrega: String
    ) {
        firebaseAuth = FirebaseAuth.getInstance()
        val db = FirebaseFirestore.getInstance()
        val trabajadorRef = db.collection("Trabajadores_Usuarios_Drivers")
            .document("trabajadores").collection("trabajadores")
            .document(firebaseAuth.uid.toString())
            .collection(metodo_pago_o_entrega)

        val docAnterior = trabajadorRef.document(idAnterior)
        val docNuevo = trabajadorRef.document(actual_select)

        docAnterior.get().addOnSuccessListener { res ->
            if (res.exists()) {
                val publicacionesActivas =
                    res.get("publicaciones_activas") as? Map<String, Map<String, Any>>

                if (publicacionesActivas != null) {
                    // Obtener lo que ya existe en el nuevo documento
                    docNuevo.get().addOnSuccessListener { nuevoDoc ->
                        val actuales =
                            nuevoDoc.get("publicaciones_activas") as? Map<String, Map<String, Any>> ?: emptyMap()
                        val combinados = actuales.toMutableMap()

                        // Combinar los datos sin borrar los anteriores
                        publicacionesActivas.forEach { (clave, valor) ->
                            combinados[clave] = valor // reemplaza si ya existe esa clave
                        }

                        val updateMap = mapOf("publicaciones_activas" to combinados)

                        docNuevo.set(updateMap, SetOptions.merge()).addOnSuccessListener {
                            verificar_Estado_metodo_pago(
                                "metodos_entrega",
                                idAnterior
                            ) { cantidad, ids, tiposPorId ->
                                var tareasEsperadas = 0
                                var tareasCompletadas = 0

                                ids.forEach { id ->
                                    val tipos = tiposPorId[id] ?: emptyList()
                                    val tipoPersonalEncontrado = tipos.firstOrNull {
                                        it in listOf("publicados", "archivados", "eliminados", "privado")
                                    }

                                    if (tipoPersonalEncontrado != null) {
                                        tareasEsperadas++
                                        val db_productos = FirebaseFirestore.getInstance()
                                            .collection("Trabajadores_Usuarios_Drivers")
                                            .document("trabajadores")
                                            .collection("trabajadores")
                                            .document(firebaseAuth.uid.toString())
                                            .collection("productos_venta")
                                            .document(tipoPersonalEncontrado)
                                            .collection(tipoPersonalEncontrado)
                                            .document(id)

                                        val hasmap = hashMapOf<String, Any>(
                                            "metodoEntrega" to actual_select
                                        )

                                        db_productos.set(hasmap, SetOptions.merge())
                                            .addOnSuccessListener {
                                                tareasCompletadas++
                                                if (tareasCompletadas == tareasEsperadas) {
                                                    eliminarMetodo_pago_o_entrega(
                                                        dialog,
                                                        binding,
                                                        idAnterior,
                                                        "metodos_entrega"
                                                    )
                                                    Toast.makeText(
                                                        binding.root.context,
                                                        "Método de pago eliminado correctamente",
                                                        Toast.LENGTH_SHORT
                                                    ).show()
                                                    dialog.dismiss()
                                                }
                                            }
                                    }

                                    val desactivados = listOf("archivados", "eliminados", "privado")
                                    val estaDesactivado = tipos.any { it in desactivados }

                                    if ("productos_publicaciones" in tipos && !estaDesactivado) {
                                        tareasEsperadas++
                                        val db_productos_general = FirebaseFirestore.getInstance()
                                            .collection("productos_publicaciones")
                                            .document("producto")
                                            .collection("producto")
                                            .document(id)

                                        val hasmap = hashMapOf<String, Any>(
                                            "metodoEntrega" to actual_select
                                        )

                                        db_productos_general.set(hasmap, SetOptions.merge())
                                            .addOnSuccessListener {
                                                tareasCompletadas++
                                                if (tareasCompletadas == tareasEsperadas) {
                                                    eliminarMetodo_pago_o_entrega(
                                                        dialog,
                                                        binding,
                                                        idAnterior,
                                                        "metodos_entrega"
                                                    )
                                                    Toast.makeText(
                                                        binding.root.context,
                                                        "Método de pago eliminado correctamente",
                                                        Toast.LENGTH_SHORT
                                                    ).show()
                                                    dialog.dismiss()
                                                }
                                            }
                                    }
                                }

                                if (ids.isEmpty()) {
                                    eliminarMetodo_pago_o_entrega(
                                        dialog,
                                        binding,
                                        idAnterior,
                                        "metodos_entrega"
                                    )
                                    Toast.makeText(
                                        binding.root.context,
                                        "Método de pago eliminado correctamente",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    dialog.dismiss()
                                }
                            }
                        }.addOnFailureListener { e ->
                            Log.e("DEBUG_COPIA", "Error al fusionar publicaciones: $e")
                        }
                    }.addOnFailureListener {
                        Log.e("DEBUG_COPIA", "Error al obtener el doc nuevo: $it")
                    }

                } else {
                    Log.d("DEBUG_COPIA", "No hay publicaciones activas en el método anterior.")
                    eliminarMetodo_pago_o_entrega(dialog, binding, idAnterior, "metodos_entrega")
                    Toast.makeText(
                        binding.root.context,
                        "Método de pago eliminado correctamente",
                        Toast.LENGTH_SHORT
                    ).show()
                    dialog.dismiss()
                }
            } else {
                Log.e("DEBUG_COPIA", "El documento anterior no existe.")
            }
        }

    }

    fun eliminarMetodo_pago_o_entrega(
        dialog: BottomSheetDialog,
        binding: BottomSheeetMetodoEntregaBinding,
        selecionado: String,
        metodo_pago_o_entrega: String
    ) {
        firebaseAuth = FirebaseAuth.getInstance()
        val context = binding.root.context
        val db = FirebaseFirestore.getInstance()
            .collection("Trabajadores_Usuarios_Drivers")
            .document("trabajadores")
            .collection("trabajadores")
            .document(firebaseAuth.uid.toString())
            .collection(metodo_pago_o_entrega)
            .document(selecionado)
        db.delete()
            .addOnSuccessListener {
                Toast.makeText(
                    context,
                    "Método de entrega eliminado",
                    Toast.LENGTH_SHORT
                ).show()
                obtenerMetodosEntrega(binding)
                Log.d("eliminamosDB", db.path)
            }

            .addOnFailureListener { e ->
                Log.e("error_eliminar", "Error al eliminar el método de entrega: $e")
            }
    }


    private fun editarCambiosEntrega(
        selecionado: String,
        bottoSheet_entrega: BottomSheeetMetodoEntregaBinding,
    ) {

        Toast.makeText(this, "entramos a campos editados", Toast.LENGTH_SHORT).show()
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
                    val deliver_gratis_editar =
                        when (bottoSheet_entrega.grupoEnvioGratis.checkedRadioButtonId) {
                            R.id.si -> true
                            R.id.no -> false
                            else -> false
                        }
                    Toast.makeText(
                        this,
                        "guardamos los cambios con el $deliver_gratis_editar",
                        Toast.LENGTH_SHORT
                    ).show()
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
                        hashMap["datos_delivery"] = hashMapOf("gratis" to deliver_gratis_editar)
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


    private fun eliminarReferenciaEntregaSelect(
        lista: Int,
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
                verificar_Estado_metodo_pago(
                    "metodos_entrega",
                    selecionado
                ) { cantidad, ids, tiposPorId ->
                    if (cantidad > 0) {
                        Log.d("DEBUG_PUBLICACIONES", "Cantidad encontradas: $cantidad")
                        Log.d("DEBUG_PUBLICACIONES", "IDs: $ids")

                        if (cantidad > 0) {
                            val mensaje = buildString {
                                append("Este método de entrega está en uso por una o más de una publicación:\n\n")
                                ids.forEach {
                                    append("• $it\n")
                                    val tipos = tiposPorId[it] ?: emptyList()
                                }
                                append("\n¿Estás seguro de que deseas eliminarlo?")
                            }

                            AlertDialog.Builder(context)
                                .setTitle("Advertencia")
                                .setMessage(mensaje)
                                .setPositiveButton("Eliminar de todos modos") { dialog2, _ ->
                                    if (lista <= 1) {
                                        Toast.makeText(
                                            context,
                                            "Deves agregar un nuevo metodo de pago ",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    } else {
                                        bottoSheet_entrega.textoSelecionaMetodoEntrega.text =
                                            "Selecione un metodo de entrega ya creado"
                                        bottoSheet_entrega.linealMetodoPago.isVisible = false
                                        obtenerMetodosEntrega_menos_eliminado(
                                            bottoSheet_entrega,
                                            selecionado
                                        )
                                        dialog2.dismiss()
                                    }

                                }
                                .setNegativeButton("Cancelar") { dialog2, _ ->
                                    dialog2.dismiss()
                                }
                                .show()
                        } else {
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
                                    Log.e(
                                        "error_eliminar",
                                        "Error al eliminar el método de pago: $e"
                                    )
                                }
                        }
                    } else {
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
                    }
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

        obtenerTrabajosRecientes(
            binding.traajosRecientes.cargaDatos,
            binding.traajosRecientes.linealCargaDatos,
            "trabajos_realizados",
            "publicados",
            { valor ->
                val trabajos_activos =
                    SpannableString("Trabajos activos : ${valor}")
                constantestextos_general.setearInformacionboldDescripcion(
                    "Trabajos activos",
                    trabajos_activos, binding.traajosRecientes.activos
                )
            })

        obtenerTrabajosRecientes(
            binding.traajosRecientes.cargaDatos,
            binding.traajosRecientes.linealCargaDatos,
            "trabajos_realizados",
            "archivados",
            { valor ->
                val trabajos_activos =
                    SpannableString("Trabajos archivados : ${valor}")
                constantestextos_general.setearInformacionboldDescripcion(
                    "Trabajos archivados",
                    trabajos_activos, binding.traajosRecientes.archivadas
                )
            })


        obtenerTrabajosRecientes(
            binding.traajosRecientes.cargaDatos,
            binding.traajosRecientes.linealCargaDatos,
            "trabajos_realizados",
            "eliminados",
            { valor ->
                val trabajos_activos =
                    SpannableString("Trabajos eliminados : ${valor}")
                constantestextos_general.setearInformacionboldDescripcion(
                    "Trabajos eliminados",
                    trabajos_activos, binding.traajosRecientes.eliminadas
                )
            })
        binding.traajosRecientes.verTodos.setOnClickListener {
            startActivity(Intent(this, ver_publicaciones::class.java))
        }





        obtenerTrabajosRecientes(
            binding.publicaciones.cargaDatos,
            binding.publicaciones.linealCargaDatos,
            "publicaciones_trabajos",
            "publicados",
            { valor ->
                val activas_trabajos =
                    SpannableString("Publicaciones activas : ${valor}")
                constantestextos_general.setearInformacionboldDescripcion(
                    "Publicaciones activas",
                    activas_trabajos, binding.publicaciones.activos
                )
            })

        obtenerTrabajosRecientes(
            binding.publicaciones.cargaDatos,
            binding.publicaciones.linealCargaDatos,
            "publicaciones_trabajos",
            "archivados",
            { valor ->
                val trabajos_activos =
                    SpannableString("Publicaciones archivados : ${valor}")
                constantestextos_general.setearInformacionboldDescripcion(
                    "Publicaciones archivados",
                    trabajos_activos, binding.publicaciones.archivadas
                )
            })


        obtenerTrabajosRecientes(
            binding.publicaciones.cargaDatos,
            binding.publicaciones.linealCargaDatos,
            "publicaciones_trabajos",
            "eliminados",
            { valor ->
                val trabajos_activos =
                    SpannableString("Publicaciones eliminados : ${valor}")
                constantestextos_general.setearInformacionboldDescripcion(
                    "Publicaciones eliminados",
                    trabajos_activos, binding.publicaciones.eliminadas
                )
            })


        obtenerTrabajosRecientes(
            binding.productosVenta.cargaDatos,
            binding.productosVenta.linealCargaDatos, "productos_venta", "publicados", { valor ->
                val productos_activos =
                    SpannableString("Productos activos : ${valor}")
                constantestextos_general.setearInformacionboldDescripcion(
                    "Productos activos",
                    productos_activos, binding.productosVenta.activos
                )
            })


        obtenerTrabajosRecientes(
            binding.productosVenta.cargaDatos,
            binding.productosVenta.linealCargaDatos, "productos_venta", "archivados", { valor ->
                val trabajos_activos =
                    SpannableString("Productos archivados : ${valor}")
                constantestextos_general.setearInformacionboldDescripcion(
                    "Productos archivados",
                    trabajos_activos, binding.productosVenta.archivadas
                )
            })

        obtenerTrabajosRecientes(
            binding.productosVenta.cargaDatos,
            binding.productosVenta.linealCargaDatos, "productos_venta", "eliminados", { valor ->
                val trabajos_activos =
                    SpannableString("Productos eliminados : ${valor}")
                constantestextos_general.setearInformacionboldDescripcion(
                    "Productos eliminados",
                    trabajos_activos, binding.productosVenta.eliminadas
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

    private val handler = Handler(Looper.getMainLooper())

    private fun obtenerTrabajosRecientes(
        progressbar: ProgressBar,
        lineal: LinearLayout,
        col1: String,
        col: String,
        callback: (String) -> Unit
    ) {
        val db = FirebaseFirestore.getInstance()
            .collection("Trabajadores_Usuarios_Drivers")
            .document("trabajadores")
            .collection("trabajadores")
            .document(firebaseAuth.uid.toString())
            .collection(col1)
            .document(col)
            .collection(col)

        val startTime = System.currentTimeMillis()

        db.get().addOnSuccessListener { res ->
            val endTime = System.currentTimeMillis()
            val tiempo = endTime - startTime
            Log.d("FirestoreTiempo", "Demoró ${tiempo}ms en obtener los datos")

            handler.postDelayed({
                progressbar.isVisible = false
                lineal.isVisible = true
            }, tiempo)

            val cantidadPublicadas = res.size()
            callback(cantidadPublicadas.toString())
        }.addOnFailureListener { e ->
            val endTime = System.currentTimeMillis()
            val tiempo = endTime - startTime
            Log.e("FirestoreTiempo", "Error: ${e.message}. Demoró ${tiempo}ms")

            // Ocultar el progressbar de todos modos en caso de error
            handler.post {
                progressbar.isVisible = false
                lineal.isVisible = true
            }

            callback("No se encontraron")
        }
    }


}
