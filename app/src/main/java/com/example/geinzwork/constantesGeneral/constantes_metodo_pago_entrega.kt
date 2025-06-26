package com.example.geinzwork.constantesGeneral

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.geinzwork.adapterViewholder.adapter_metodos_pagos
import com.example.geinzwork.dataclass.dataclass_metodos_pagos
import com.geinzz.geinzwork.databinding.BotomSheetDialogMetodosPagoBinding
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions

object constantes_metodo_pago_entrega {
    private lateinit var firebaseAuth: FirebaseAuth
    private val lista = mutableListOf<dataclass_metodos_pagos>()
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

                        if (activo && idPublicacion.isNotEmpty()) {
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

    fun eliminarMetodo_pago_o_entrega(
        dialog: BottomSheetDialog,
        binding: BotomSheetDialogMetodosPagoBinding, selecionado: String,metodo_pago_o_entrega: String
    ) {
        firebaseAuth=FirebaseAuth.getInstance()
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
        firebaseAuth=FirebaseAuth.getInstance()
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
                BotomSheetDialogMetodosPagoBinding,dialog
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
                    pasar_metodos_pagosNuevo(
                        BotomSheetDialogMetodosPagoBinding,
                        editado.id.toString(),
                        idAnterior, dialog = dialog,"metodos_pago"
                    )
                }
            }
        })
    }

    private fun eliminarReferenciaPagoSelect(
        lista: Int,
        selecionado: String,
        binding: BotomSheetDialogMetodosPagoBinding,dialog_params: BottomSheetDialog
    ) {
        val context = binding.root.context
        AlertDialog.Builder(context)
            .setTitle("Eliminar método de pago")
            .setMessage("¿Estás seguro de que deseas eliminar este método de pago?")
            .setPositiveButton("Sí") { dialog, _ ->
                verificar_Estado_metodo_pago("metodos_pago",selecionado) { cantidad, ids, tiposPorId ->

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
                                        obtener_metodos_pagos_menos_elimiando(context,dialog_params,binding, selecionado)
                                        dialog2.dismiss()
                                    }

                                }
                                .setNegativeButton("Cancelar") { dialog2, _ ->
                                    dialog2.dismiss()
                                }
                                .show()
                        } else {
                            eliminarMetodo_pago_o_entrega(dialog_params ,binding, selecionado,"metodos_pago")
                        }
                    } else {
                        eliminarMetodo_pago_o_entrega(dialog_params,binding, selecionado,"metodos_pago")
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
        BotomSheetDialogMetodosPagoBinding: BotomSheetDialogMetodosPagoBinding,context: Context,dialog: BottomSheetDialog
    ) {
        firebaseAuth=FirebaseAuth.getInstance()
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
                                    Toast.makeText(context,

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
                                    obtner_Metodos_pagosCreados(dialog,context,BotomSheetDialogMetodosPagoBinding)
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

    fun verificarNombreDisponible(context: Context,
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
        firebaseAuth=FirebaseAuth.getInstance()
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
                    inicializarRecicleViewPagos(dialog,context,"filtrado", binding, id_selecionado)
                    binding.textoSinMetodos.isVisible = false
                    binding.recicleViewMetodosPagos.isVisible = true
                } else {
                    binding.textoSinMetodos.isVisible = true
                    binding.recicleViewMetodosPagos.isVisible = false

                }
            }, delay)
        }

    }

    private fun pasar_metodos_pagosNuevo(
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
                            resNuevo.get("publicaciones_activas") as? Map<String, Map<String, Any>> ?: emptyMap()

                        val fusion = publicacionesActuales.toMutableMap()
                        publicacionesActivasAnterior.forEach { (clave, valor) ->
                            fusion[clave] = valor
                        }

                        val mapFinal = mapOf("publicaciones_activas" to fusion)

                        docNuevo.set(mapFinal, SetOptions.merge()).addOnSuccessListener {
                            verificar_Estado_metodo_pago("metodos_pago", idAnterior) { cantidad, ids, tiposPorId ->
                                var tareasEsperadas = 0
                                var tareasCompletadas = 0

                                ids.forEach { id ->
                                    val tipos = tiposPorId[id] ?: emptyList()
                                    val tipoPersonalEncontrado = tipos.firstOrNull {
                                        it in listOf("publicados", "archivados", "eliminados", "privado")
                                    }

                                    if (tipoPersonalEncontrado != null) {
                                        tareasEsperadas++
                                        val db_productos = db.collection("Trabajadores_Usuarios_Drivers")
                                            .document("trabajadores")
                                            .collection("trabajadores")
                                            .document(firebaseAuth.uid.toString())
                                            .collection("productos_venta")
                                            .document(tipoPersonalEncontrado)
                                            .collection(tipoPersonalEncontrado)
                                            .document(id)

                                        val hasmap = hashMapOf<String, Any>("metodoPago" to actual_select)

                                        db_productos.set(hasmap, SetOptions.merge())
                                            .addOnSuccessListener {
                                                tareasCompletadas++
                                                if (tareasCompletadas == tareasEsperadas) {
                                                    eliminarMetodo_pago_o_entrega(dialog, binding, idAnterior, "metodos_pago")
                                                    Toast.makeText(
                                                        binding.root.context,
                                                        "Método de pago eliminado correctamente",
                                                        Toast.LENGTH_SHORT
                                                    ).show()
                                                    dialog.dismiss()
                                                }
                                            }
                                            .addOnFailureListener { e ->
                                                Log.e("error_cambio", "Error al actualizar en PERSONAL: $e")
                                            }
                                    }

                                    val desactivados = listOf("archivados", "eliminados", "privado")
                                    val estaDesactivado = tipos.any { it in desactivados }

                                    if ("productos_publicaciones" in tipos && !estaDesactivado) {
                                        tareasEsperadas++
                                        val db_productos_general = db.collection("productos_publicaciones")
                                            .document("producto")
                                            .collection("producto")
                                            .document(id)

                                        val hasmap = hashMapOf<String, Any>("metodoPago" to actual_select)

                                        db_productos_general.set(hasmap, SetOptions.merge())
                                            .addOnSuccessListener {
                                                tareasCompletadas++
                                                if (tareasCompletadas == tareasEsperadas) {
                                                    eliminarMetodo_pago_o_entrega(dialog, binding, idAnterior, "metodos_pago")
                                                    Toast.makeText(
                                                        binding.root.context,
                                                        "Método de pago eliminado correctamente",
                                                        Toast.LENGTH_SHORT
                                                    ).show()
                                                    dialog.dismiss()
                                                }
                                            }
                                            .addOnFailureListener { e ->
                                                Log.e("error_cambio", "Error al actualizar en GENERAL: $e")
                                            }
                                    }
                                }

                                if (ids.isEmpty()) {
                                    eliminarMetodo_pago_o_entrega(dialog, binding, idAnterior, "metodos_pago")
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
                    eliminarMetodo_pago_o_entrega(dialog, binding, idAnterior, "metodos_pago")
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
        firebaseAuth=FirebaseAuth.getInstance()
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
                        obtner_Metodos_pagosCreados(dialog,context,BotomSheetDialogMetodosPagoBinding)
                    }.addOnFailureListener { e ->
                        Log.d("error guardado", "error al guardar losd atos $e")
                    }


                }
            }
        }.addOnFailureListener { e ->
            Log.e("error", "no se econtro la referencia $e")
        }
    }


}