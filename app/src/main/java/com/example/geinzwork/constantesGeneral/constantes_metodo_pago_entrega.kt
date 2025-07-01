package com.example.geinzwork.constantesGeneral

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.geinzwork.adapterViewholder.adapter_metodos_entrega
import com.example.geinzwork.adapterViewholder.adapter_metodos_pagos
import com.example.geinzwork.dataclass.dataclass_metodos_entrega
import com.example.geinzwork.dataclass.dataclass_metodos_pagos
import com.geinzz.geinzwork.R
import com.geinzz.geinzwork.databinding.BotomSheetDialogMetodosPagoBinding
import com.geinzz.geinzwork.databinding.BottomSheeetMetodoEntregaBinding
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import okhttp3.Callback

object constantes_metodo_pago_entrega {
    private lateinit var firebaseAuth: FirebaseAuth
    private val lista = mutableListOf<dataclass_metodos_pagos>()
    private var delivery: Boolean = false
    private var coordinar: Boolean = false
    private var lugaresEntrega: Boolean = false
    private var retiroTienda: Boolean = false
    private var envioCourier: Boolean = false
    private var entregaProgramada: Boolean = false
    private var yape: Boolean = false
    private var plin: Boolean = false
    private var efectivo: Boolean = false
    private var trasnferecnia: Boolean = false

    fun bottomSheet_metodos_pago(dialog: BottomSheetDialog,context: Context,funcion_params: () -> Unit) {
        val binding_bottomSheet =
            BotomSheetDialogMetodosPagoBinding.inflate(LayoutInflater.from(context))
        binding_bottomSheet.cerrar.setOnClickListener { dialog.dismiss() }

        val view = binding_bottomSheet.root
        obtner_Metodos_pagosCreados(
            dialog,
            context,
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
            agregar_Referencia(
                yape,
                plin,
                efectivo,
                trasnferecnia,
                binding_bottomSheet.nombreReferenciaED.text.toString(),
                binding_bottomSheet,
                context,
                dialog,funcion_params
            )

        }

        dialog.setContentView(view)

    }

    fun verificar_Estado_metodo_pago(
        metodo_pago_o_entrega: String,
        seleccionado: String,
        encontrados: (cantidad: Int, ids: List<String>, tiposPorId: Map<String, List<String>>) -> Unit
    ) {
        firebaseAuth = FirebaseAuth.getInstance()
        val db = FirebaseFirestore.getInstance().collection("Trabajadores_Usuarios_Drivers")
            .document("trabajadores").collection("trabajadores")
            .document(firebaseAuth.uid.toString()).collection(metodo_pago_o_entrega)
            .document(seleccionado)

        db.get().addOnSuccessListener { res ->
            if (res.exists()) {
                val publicacionesActivas =
                    res.get("publicaciones_activas") as? Map<String, Map<String, Any>>

                if (publicacionesActivas != null) {
                    val idsEncontrados = mutableListOf<String>()
                    val tiposPorId = mutableMapOf<String, List<String>>()

                    for ((_, datos) in publicacionesActivas) {
                        val activo = datos["activo"] as? Boolean ?: false
                        val idPublicacion = datos["id_publicacion"] as? String ?: ""

                        if (idPublicacion.isNotEmpty()) {
                            idsEncontrados.add(idPublicacion)

                            val tiposActivos = mutableListOf<String>()
                            if (datos["archivados"] as? Boolean == true) tiposActivos.add("archivados")
                            if (datos["eliminados"] as? Boolean == true) tiposActivos.add("eliminados")
                            if (datos["privado"] as? Boolean == true) tiposActivos.add("privado")
                            if (datos["productos_publicaciones"] as? Boolean == true) tiposActivos.add(
                                "productos_publicaciones"
                            )
                            if (datos["publicados"] as? Boolean == true) tiposActivos.add("publicados")

                            tiposPorId[idPublicacion] = tiposActivos
                        }
                    }

                    encontrados(idsEncontrados.size, idsEncontrados, tiposPorId)
                } else {
                    encontrados(0, emptyList(), emptyMap())
                }
            } else {
                encontrados(0, emptyList(), emptyMap())
            }
        }
    }

    fun eliminarMetodo_pago_o_entrega_pago(
        dialog: BottomSheetDialog,
        binding: BotomSheetDialogMetodosPagoBinding,
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
                obtner_Metodos_pagosCreados(dialog, context, binding)
                Log.d("eliminamosDB", db.path)
            }

            .addOnFailureListener { e ->
                Log.e("error_eliminar", "Error al eliminar el método de entrega: $e")
            }
    }

    fun obtner_Metodos_pagosCreados(
        dialog: BottomSheetDialog,
        context: Context,
        binding: BotomSheetDialogMetodosPagoBinding
    ) {
        firebaseAuth = FirebaseAuth.getInstance()
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
                    inicializarRecicleViewPagos(dialog, context, "todos", binding, "")
                    binding.textoSinMetodos.isVisible = false
                    binding.recicleViewMetodosPagos.isVisible = true
                } else {
                    binding.textoSinMetodos.isVisible = true
                    binding.recicleViewMetodosPagos.isVisible = false
                }
            }, delay)
        }
    }

    private fun inicializarRecicleViewPagos(
        dialog: BottomSheetDialog,
        context: Context,
        tipo_encontrado: String,
        BotomSheetDialogMetodosPagoBinding: BotomSheetDialogMetodosPagoBinding, idAnterior: String
    ) {
        val recicles = BotomSheetDialogMetodosPagoBinding.recicleViewMetodosPagos
        recicles.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, true)
        recicles.adapter = adapter_metodos_pagos(lista, { selecionado ->
            eliminarReferenciaPagoSelect(
                lista.size,
                selecionado.id.toString(),
                BotomSheetDialogMetodosPagoBinding, dialog
            )
        }, { editado ->
            when (tipo_encontrado) {
                "todos" -> {
                    editarCambiosPAgos(
                        dialog,
                        context,
                        editado.id.toString(),
                        BotomSheetDialogMetodosPagoBinding
                    )
                }

                "filtrado" -> {
                    Toast.makeText(context, "pasamos solo los datos ", Toast.LENGTH_SHORT).show()
                    pasar_metodos_pagosNuevo_pagos(
                        BotomSheetDialogMetodosPagoBinding,
                        editado.id.toString(),
                        idAnterior, dialog = dialog, "metodos_pago"
                    )
                }
            }
        })
    }

    private fun eliminarReferenciaPagoSelect(
        lista: Int,
        selecionado: String,
        binding: BotomSheetDialogMetodosPagoBinding, dialog_params: BottomSheetDialog
    ) {
        val context = binding.root.context
        AlertDialog.Builder(context)
            .setTitle("Eliminar método de pago")
            .setMessage("¿Estás seguro de que deseas eliminar este método de pago?")
            .setPositiveButton("Sí") { dialog, _ ->
                verificar_Estado_metodo_pago(
                    "metodos_pago",
                    selecionado
                ) { cantidad, ids, tiposPorId ->

                    if (cantidad > 0) {
                        Log.d("DEBUG_PUBLICACIONES", "Cantidad encontradas: $cantidad")
                        Log.d("DEBUG_PUBLICACIONES", "IDs: $ids")

                        if (cantidad > 0) {
                            val mensaje = buildString {
                                append("Este método de pago está en uso por una o más de una publicación:\n\n")
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
                                        binding.textoSelecionaMetodoPago.text =
                                            "Selecione un metodo de pago ya creado"
                                        binding.linealMetodoPago.isVisible = false
                                        obtener_metodos_pagos_menos_elimiando(
                                            context,
                                            dialog_params,
                                            binding,
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
                            eliminarMetodo_pago_o_entrega_pago(
                                dialog_params,
                                binding,
                                selecionado,
                                "metodos_pago"
                            )
                        }
                    } else {
                        eliminarMetodo_pago_o_entrega_pago(
                            dialog_params,
                            binding,
                            selecionado,
                            "metodos_pago"
                        )
                    }
                }

                dialog.dismiss()
            }
            .setNegativeButton("No") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    fun agregar_Referencia(
        yape: Boolean,
        plin: Boolean,
        efectivo: Boolean,
        tranferencia: Boolean,
        nombre_metodo: String,
        BotomSheetDialogMetodosPagoBinding: BotomSheetDialogMetodosPagoBinding,
        context: Context,
        dialog: BottomSheetDialog,funcion_params: () -> Unit
    ) {
        firebaseAuth = FirebaseAuth.getInstance()
        val startTime = System.currentTimeMillis()
        if (!yape && !plin && !efectivo && !tranferencia) {
            Toast.makeText(
                context,
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
                    context,
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
                                        context,

                                        "Referencia creada correctamente",
                                        Toast.LENGTH_SHORT
                                    )
                                        .show()
                                    funcion_params()
                                    BotomSheetDialogMetodosPagoBinding.checkEfectivo.isChecked =
                                        false
                                    BotomSheetDialogMetodosPagoBinding.checkTransferencia.isChecked =
                                        false
                                    BotomSheetDialogMetodosPagoBinding.checkPlin.isChecked = false
                                    BotomSheetDialogMetodosPagoBinding.checkYape.isChecked = false
                                    BotomSheetDialogMetodosPagoBinding.nombreReferenciaED.setText("")
                                    obtner_Metodos_pagosCreados(
                                        dialog,
                                        context,
                                        BotomSheetDialogMetodosPagoBinding
                                    )
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

    fun verificarNombreDisponible(
        context: Context,
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
                    context,
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
            Toast.makeText(context, "Error al verificar nombre: ${it.message}", Toast.LENGTH_SHORT)
                .show()
            resultado(false)
        }
    }

    private fun obtener_metodos_pagos_menos_elimiando(
        context: Context,
        dialog: BottomSheetDialog,
        binding: BotomSheetDialogMetodosPagoBinding,
        id_selecionado: String
    ) {
        firebaseAuth = FirebaseAuth.getInstance()
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
                if (id_selecionado != id) {
                    val metodo = dataclass_metodos_pagos(
                        yape, efectivo, plin, tranferencia, nombre_colecion, id
                    )
                    lista.add(metodo)
                }
            }

            val tiempoFin = System.currentTimeMillis()
            val delay = (tiempoFin - tiempoInicio)

            Handler(Looper.getMainLooper()).postDelayed({
                binding.cargandoMetodosPago.isVisible = false

                if (lista.isNotEmpty()) {
                    inicializarRecicleViewPagos(
                        dialog,
                        context,
                        "filtrado",
                        binding,
                        id_selecionado
                    )
                    binding.textoSinMetodos.isVisible = false
                    binding.recicleViewMetodosPagos.isVisible = true
                } else {
                    binding.textoSinMetodos.isVisible = true
                    binding.recicleViewMetodosPagos.isVisible = false

                }
            }, delay)
        }

    }

    private fun pasar_metodos_pagosNuevo_pagos(
        binding: BotomSheetDialogMetodosPagoBinding,
        actual_select: String,
        idAnterior: String,
        dialog: BottomSheetDialog,
        metodo_pago_o_entrega: String
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
                val publicacionesActivasAnterior =
                    res.get("publicaciones_activas") as? Map<String, Map<String, Any>>

                if (publicacionesActivasAnterior != null) {
                    docNuevo.get().addOnSuccessListener { resNuevo ->
                        val publicacionesActuales =
                            resNuevo.get("publicaciones_activas") as? Map<String, Map<String, Any>>
                                ?: emptyMap()

                        val fusion = publicacionesActuales.toMutableMap()
                        publicacionesActivasAnterior.forEach { (clave, valor) ->
                            fusion[clave] = valor
                        }

                        val mapFinal = mapOf("publicaciones_activas" to fusion)

                        docNuevo.set(mapFinal, SetOptions.merge()).addOnSuccessListener {
                            verificar_Estado_metodo_pago(
                                "metodos_pago",
                                idAnterior
                            ) { cantidad, ids, tiposPorId ->
                                var tareasEsperadas = 0
                                var tareasCompletadas = 0

                                ids.forEach { id ->
                                    val tipos = tiposPorId[id] ?: emptyList()
                                    val tipoPersonalEncontrado = tipos.firstOrNull {
                                        it in listOf(
                                            "publicados",
                                            "archivados",
                                            "eliminados",
                                            "privado"
                                        )
                                    }

                                    if (tipoPersonalEncontrado != null) {
                                        tareasEsperadas++
                                        val db_productos =
                                            db.collection("Trabajadores_Usuarios_Drivers")
                                                .document("trabajadores")
                                                .collection("trabajadores")
                                                .document(firebaseAuth.uid.toString())
                                                .collection("productos_venta")
                                                .document(tipoPersonalEncontrado)
                                                .collection(tipoPersonalEncontrado)
                                                .document(id)

                                        val hasmap =
                                            hashMapOf<String, Any>("metodoPago" to actual_select)

                                        db_productos.set(hasmap, SetOptions.merge())
                                            .addOnSuccessListener {
                                                tareasCompletadas++
                                                if (tareasCompletadas == tareasEsperadas) {
                                                    eliminarMetodo_pago_o_entrega_pago(
                                                        dialog,
                                                        binding,
                                                        idAnterior,
                                                        "metodos_pago"
                                                    )
                                                    Toast.makeText(
                                                        binding.root.context,
                                                        "Método de pago eliminado correctamente",
                                                        Toast.LENGTH_SHORT
                                                    ).show()
                                                    dialog.dismiss()
                                                }
                                            }
                                            .addOnFailureListener { e ->
                                                Log.e(
                                                    "error_cambio",
                                                    "Error al actualizar en PERSONAL: $e"
                                                )
                                            }
                                    }

                                    val desactivados = listOf("archivados", "eliminados", "privado")
                                    val estaDesactivado = tipos.any { it in desactivados }

                                    if ("productos_publicaciones" in tipos && !estaDesactivado) {
                                        tareasEsperadas++
                                        val db_productos_general =
                                            db.collection("productos_publicaciones")
                                                .document("producto")
                                                .collection("producto")
                                                .document(id)

                                        val hasmap =
                                            hashMapOf<String, Any>("metodoPago" to actual_select)

                                        db_productos_general.set(hasmap, SetOptions.merge())
                                            .addOnSuccessListener {
                                                tareasCompletadas++
                                                if (tareasCompletadas == tareasEsperadas) {
                                                    eliminarMetodo_pago_o_entrega_pago(
                                                        dialog,
                                                        binding,
                                                        idAnterior,
                                                        "metodos_pago"
                                                    )
                                                    Toast.makeText(
                                                        binding.root.context,
                                                        "Método de pago eliminado correctamente",
                                                        Toast.LENGTH_SHORT
                                                    ).show()
                                                    dialog.dismiss()
                                                }
                                            }
                                            .addOnFailureListener { e ->
                                                Log.e(
                                                    "error_cambio",
                                                    "Error al actualizar en GENERAL: $e"
                                                )
                                            }
                                    }
                                }

                                if (ids.isEmpty()) {
                                    eliminarMetodo_pago_o_entrega_pago(
                                        dialog,
                                        binding,
                                        idAnterior,
                                        "metodos_pago"
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
                    }
                } else {
                    Log.d("DEBUG_COPIA", "No hay publicaciones activas en el método anterior.")
                    eliminarMetodo_pago_o_entrega_pago(dialog, binding, idAnterior, "metodos_pago")
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


    private fun editarCambiosPAgos(
        dialog: BottomSheetDialog,
        context: Context,
        selecionado: String,
        BotomSheetDialogMetodosPagoBinding: BotomSheetDialogMetodosPagoBinding
    ) {
        firebaseAuth = FirebaseAuth.getInstance()
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
                            context,
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
                        obtner_Metodos_pagosCreados(
                            dialog,
                            context,
                            BotomSheetDialogMetodosPagoBinding
                        )
                    }.addOnFailureListener { e ->
                        Log.d("error guardado", "error al guardar losd atos $e")
                    }


                }
            }
        }.addOnFailureListener { e ->
            Log.e("error", "no se econtro la referencia $e")
        }
    }


    //metodos de entrega


    fun bottomSheet_metodo_entrega(
        dialog: BottomSheetDialog,
        lista_entrega: MutableList<dataclass_metodos_entrega>,
        context: Context ,funcion_params: () -> Unit
    ) {
        val bottoSheet_entrega =
            BottomSheeetMetodoEntregaBinding.inflate(LayoutInflater.from(context))
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
        obtenerMetodosEntrega(bottoSheet_entrega,lista_entrega,context,dialog)

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
                bottoSheet_entrega, context, lista_entrega, dialog,funcion_params
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
        bottosheetEntrega: BottomSheeetMetodoEntregaBinding,
        context: Context,
        lista_entrega: MutableList<dataclass_metodos_entrega>, dialog: BottomSheetDialog, funcion_params: () -> Unit
    ) {
        firebaseAuth= FirebaseAuth.getInstance()
        // Validaciones
        if (delivery == true && bottosheetEntrega.grupoEnvioGratis.checkedRadioButtonId == -1) {
            Toast.makeText(context, "Selecciona si el delivery es gratis o no", Toast.LENGTH_SHORT)
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
                context,
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
        verificarNombreDisponible(
            context,
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
                                context,
                                "Método de entrega creado correctamente",
                                Toast.LENGTH_SHORT
                            ).show()
                            funcion_params()
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
                            obtenerMetodosEntrega(bottosheetEntrega, lista_entrega, context, dialog)
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

    private fun obtenerMetodosEntrega(
        bottoSheet_entrega: BottomSheeetMetodoEntregaBinding,
        lista_entrega: MutableList<dataclass_metodos_entrega>,
        context: Context,
        dialog: BottomSheetDialog
    ) {
        lista_entrega.clear()
        firebaseAuth= FirebaseAuth.getInstance()
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
                    incializar_recicle_entrega(
                        "todos",
                        bottoSheet_entrega,
                        "",
                        context,
                        lista_entrega,
                        dialog
                    )
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
        bottoSheet_entrega: BottomSheeetMetodoEntregaBinding,
        idAnterior: String,
        context: Context,
        lista_entrega: MutableList<dataclass_metodos_entrega>, dialog: BottomSheetDialog
    ) {
        val recicles = bottoSheet_entrega.recicleViewMetodosEntrega
        recicles.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, true)
        recicles.adapter = adapter_metodos_entrega(lista_entrega, { selecionado ->
            eliminarReferenciaEntregaSelect(
                lista_entrega,
                context,
                dialog, lista_entrega.size, selecionado.id.toString(),
                bottoSheet_entrega
            )
        }, { editado ->
            when (tipo_encontrado) {
                "todos" -> {
                    editarCambiosEntrega(
                        editado.id.toString(),
                        bottoSheet_entrega, lista_entrega, context, dialog
                    )
                }

                "filtrado" -> {
                    pasar_metodos_pagosNuevo(
                        bottoSheet_entrega,
                        editado.id.toString(),
                        idAnterior,
                        dialog,
                        "metodos_entrega", lista_entrega, context
                    )
                }
            }

        })
    }

    private fun eliminarReferenciaEntregaSelect(
        lista_entrega: MutableList<dataclass_metodos_entrega>,
        context: Context, dialog_params: BottomSheetDialog,
        lista: Int,
        selecionado: String,
        bottoSheet_entrega: BottomSheeetMetodoEntregaBinding
    ) {
        val context = bottoSheet_entrega.root.context
        firebaseAuth= FirebaseAuth.getInstance()
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
                                            "Deves agregar un nuevo metodo de entrega ",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    } else {
                                        bottoSheet_entrega.textoSelecionaMetodoEntrega.text =
                                            "Selecione un metodo de entrega ya creado"
                                        bottoSheet_entrega.linealMetodoPago.isVisible = false
                                        obtenerMetodosEntrega_menos_eliminado(
                                            bottoSheet_entrega,
                                            selecionado, lista_entrega, context, dialog_params
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
                                    obtenerMetodosEntrega(
                                        bottoSheet_entrega,
                                        lista_entrega,
                                        context,
                                        dialog_params
                                    )
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
                                obtenerMetodosEntrega(
                                    bottoSheet_entrega,
                                    lista_entrega,
                                    context,
                                    dialog_params
                                )
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

    private fun obtenerMetodosEntrega_menos_eliminado(
        bottoSheet_entrega: BottomSheeetMetodoEntregaBinding,
        id_selecionado: String,
        lista_entrega: MutableList<dataclass_metodos_entrega>,
        context: Context,
        dialog: BottomSheetDialog
    ) {
        firebaseAuth= FirebaseAuth.getInstance()
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
                    incializar_recicle_entrega(
                        "filtrado",
                        bottoSheet_entrega,
                        id_selecionado,
                        context,
                        lista_entrega,
                        dialog
                    )
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

    private fun editarCambiosEntrega(
        selecionado: String,
        bottoSheet_entrega: BottomSheeetMetodoEntregaBinding,
        lista_entrega: MutableList<dataclass_metodos_entrega>,
        context: Context,
        dialog: BottomSheetDialog
    ) {
        firebaseAuth= FirebaseAuth.getInstance()
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
                        context,
                        "guardamos los cambios con el $deliver_gratis_editar",
                        Toast.LENGTH_SHORT
                    ).show()
                    if (bottoSheet_entrega.delivery.isChecked && bottoSheet_entrega.grupoEnvioGratis.checkedRadioButtonId == -1) {
                        Toast.makeText(
                            context,
                            "Selecciona si el delivery es gratis o no",
                            Toast.LENGTH_SHORT
                        )
                            .show()
                        return@setOnClickListener
                    }
                    if (!(bottoSheet_entrega.delivery.isChecked || bottoSheet_entrega.corrdinar.isChecked || bottoSheet_entrega.lugaresEntrega.isChecked || bottoSheet_entrega.retiroTienda.isChecked || bottoSheet_entrega.envioCourier.isChecked || bottoSheet_entrega.entregaProgramada.isChecked)) {
                        Toast.makeText(
                            context,
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
                            context,
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
                        obtenerMetodosEntrega(bottoSheet_entrega, lista_entrega, context, dialog)
                    }.addOnFailureListener { e ->
                        Log.d("error guardado", "error al guardar losd atos $e")
                    }


                }
            }
        }
    }

    private fun pasar_metodos_pagosNuevo(
        binding: BottomSheeetMetodoEntregaBinding,
        actual_select: String,
        idAnterior: String,
        dialog: BottomSheetDialog,
        metodo_pago_o_entrega: String,
        lista_entrega: MutableList<dataclass_metodos_entrega>, context: Context
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
                            nuevoDoc.get("publicaciones_activas") as? Map<String, Map<String, Any>>
                                ?: emptyMap()
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
                                        it in listOf(
                                            "publicados",
                                            "archivados",
                                            "eliminados",
                                            "privado"
                                        )
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
                                                        lista_entrega, context,
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
                                                        lista_entrega, context,
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
                                        lista_entrega, context,
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
                    eliminarMetodo_pago_o_entrega(
                        lista_entrega,
                        context,
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
            } else {
                Log.e("DEBUG_COPIA", "El documento anterior no existe.")
            }
        }

    }

    fun eliminarMetodo_pago_o_entrega(
        lista_entrega: MutableList<dataclass_metodos_entrega>,
        context: Context,
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
                obtenerMetodosEntrega(binding, lista_entrega, context, dialog)
                Log.d("eliminamosDB", db.path)
            }

            .addOnFailureListener { e ->
                Log.e("error_eliminar", "Error al eliminar el método de entrega: $e")
            }
    }

}