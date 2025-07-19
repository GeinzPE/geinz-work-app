package com.geinzz.geinzwork

import android.content.Context
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.widget.HorizontalScrollView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import com.geinzz.geinzwork.ui.adapters.adapter_reporte_denuncia_tb
import com.geinzz.geinzwork.utils.constantes.constantes.constantes_vinculados
import com.geinzz.geinzwork.model.dataclass_reporte_denuncia_tb
import com.geinzz.geinzwork.utils.constantes.constantes.constantesCarrito
import com.geinzz.geinzwork.utils.constantes.constantes.constantestextos_general
import com.geinzz.geinzwork.databinding.ActivityVistaDenunciaReporteBinding
import com.geinzz.geinzwork.databinding.BottomSheetAplarReporteBinding
import com.geinzz.geinzwork.databinding.BottomSheetInformacionReportesDenunciasBinding
import com.geinzz.geinzwork.databinding.BottomSheetReportesGeneralBinding
import com.geinzz.geinzwork.model.dataClassReportes
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions

class vista_denuncia_reporte : AppCompatActivity() {
    private lateinit var binding: ActivityVistaDenunciaReporteBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private val lista = mutableListOf<dataClassReportes>()
    private lateinit var dialog: BottomSheetDialog
    private val lista_reporte = mutableListOf<dataclass_reporte_denuncia_tb>()

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityVistaDenunciaReporteBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        firebaseAuth = FirebaseAuth.getInstance()

        binding.listenerDenunciaReview.setOnClickListener {
            dialog = BottomSheetDialog(this)
            bottomSheet_open_reportes("review")
            dialog.show()
        }
        binding.listenerReportesATrabajadores.setOnClickListener {
            dialog = BottomSheetDialog(this)
            bottomSheet_open_reportes("trabajadores")
            dialog.show()
        }
        binding.listenerTusReportes.setOnClickListener {
            dialog = BottomSheetDialog(this)
            bottomSheet_open_reportes("reportes")
            dialog.show()
        }

        obtener_cantidad_rerportes(
            "enviados",
            "enviados",
            binding.reportesATrbajadores,
            binding.cargandoContadorDenunciaReview, binding.datosDenunciaReview
        )
        obtener_cantidad_rerportes(
            "recivido",
            "recivido",
            binding.tusReportesTxt,
            binding.cargandoContadorReportesTrabajadores, binding.datosReportesTrabajadores
        )
        obtener_cantidad_rerportes(
            "enviados",
            "review",
            binding.guardadoDenunciaReview,
            binding.cargandoContadorTusReportes, binding.datosTusReportes
        )


    }

    private fun obtener_cantidad_rerportes(
        tipo1: String,
        tipo2: String,
        textView: TextView,
        progressBar: ProgressBar, linearLayout: LinearLayout
    ) {
        constantes_vinculados.encotrar_user(firebaseAuth.uid.toString()) { tipo, Coleccion ->
            if (Coleccion != null) {
                val startTime = System.currentTimeMillis()

                val db = Coleccion.document(firebaseAuth.uid.toString())
                    .collection("reporte")
                    .document(tipo1)
                    .collection(tipo2)

                db.get().addOnSuccessListener { res ->
                    val endTime = System.currentTimeMillis()
                    val duration = endTime - startTime
                    val size = res.size()
                    textView.text = size.toString()
                    Handler(Looper.getMainLooper()).postDelayed({
                        progressBar.isVisible = false
                        linearLayout.isVisible = true
                    }, duration)
                    Log.d("tiempo", "Tiempo en obtener reportes: ${duration}ms")

                }.addOnFailureListener { e ->
                    Log.d("error", "Error al obtener la cantidad de reportes")
                }
            }
        }
    }


    @RequiresApi(Build.VERSION_CODES.O)
    private fun bottomSheet_open_reportes(tipo: String) {
        val bottomSheet =
            BottomSheetReportesGeneralBinding.inflate(LayoutInflater.from(this))
        val view = bottomSheet.root

        when (tipo) {
            "trabajadores" -> {
                bottomSheet.linealChipsFiltradoestados.isVisible = true
                bottomSheet.scroolReporesMismoTrabajador.isVisible = false
                bottomSheet.grupoReporesUser.clearCheck()
                bottomSheet.chipGroupestados.clearCheck()
                bottomSheet.todosFitlradosReportesReview.isChecked = true
                bottomSheet.cargandoContenido.isVisible = true
                obtner_Denuncias_trabajadores(
                    bottomSheet,
                    bottomSheet.linealChipsFiltradoestados,
                    "enviados"
                )
                bottomSheet.chipGroupestados.setOnCheckedChangeListener { group, checkedId ->
                    when (checkedId) {
                        R.id.todosFitlrados_reportes_review -> {
                            obtner_Denuncias_trabajadores(
                                bottomSheet,
                                bottomSheet.linealChipsFiltradoestados,
                                "enviados"
                            )
                        }

                        R.id.enviado -> {
                            buscarFiltrados(bottomSheet, "enviados", "enviado")
                        }

                        R.id.proceso -> {
                            buscarFiltrados(bottomSheet, "enviados", "proceso")
                        }

                        R.id.aceptado -> {
                            buscarFiltrados(bottomSheet, "enviados", "aceptado")
                        }

                        R.id.rechazado -> {
                            buscarFiltrados(bottomSheet, "enviados", "rechazado")
                        }

                        R.id.archivado -> {
                            buscarFiltrados(bottomSheet, "enviados", "archivado")
                        }

                        R.id.resuelto -> {
                            buscarFiltrados(bottomSheet, "enviados", "resuelto")
                        }

                        R.id.cancelado -> {
                            buscarFiltrados(bottomSheet, "enviados", "cancelado")
                        }
                    }
                }
            }

            "reportes" -> {
                bottomSheet.scroolReporesMismoTrabajador.isVisible = true
                bottomSheet.linealChipsFiltradoestados.isVisible = false
                bottomSheet.chipGroupestados.clearCheck()
                bottomSheet.grupoReporesUser.clearCheck()
                bottomSheet.TodosFiltrado.isChecked = false
                bottomSheet.TodosFiltrado.isChecked = true
                bottomSheet.cargandoContenido.isVisible = true
                obtner_Denuncias_trabajadores(
                    bottomSheet,
                    bottomSheet.scroolReporesMismoTrabajador,
                    "recivido"
                )

                bottomSheet.grupoReporesUser.setOnCheckedChangeListener { group, checkedId ->
                    when (checkedId) {
                        R.id.TodosFiltrado -> {
                            obtner_Denuncias_trabajadores(
                                bottomSheet,
                                bottomSheet.scroolReporesMismoTrabajador,
                                "recivido"
                            )
                        }

                        R.id.aplelado -> {
                            buscarFiltrados(bottomSheet, "recivido", "apelado")
                        }

                        R.id.por_apelar -> {
                            buscarFiltrados(bottomSheet, "recivido", "por apelar")
                        }

                        R.id.apelcaionAceptada -> {
                            buscarFiltrados(bottomSheet, "recivido", "apelacion aceptada")
                        }

                        R.id.aplecion_rechazada -> {
                            buscarFiltrados(bottomSheet, "recivido", "apelacion rechazada")
                        }
                    }
                }

            }

            "review" -> {
                bottomSheet.linealChipsFiltradoestados.isVisible = true
                bottomSheet.scroolReporesMismoTrabajador.isVisible = false
                obtener_denuncias_Review(bottomSheet, bottomSheet.linealChipsFiltradoestados)
                bottomSheet.grupoReporesUser.clearCheck()
                bottomSheet.cargandoContenido.isVisible = true
                bottomSheet.chipGroupestados.clearCheck()
                bottomSheet.todosFitlradosReportesReview.isChecked = false
                bottomSheet.todosFitlradosReportesReview.isChecked = true
                bottomSheet.chipGroupestados.setOnCheckedChangeListener { group, checkedId ->
                    when (checkedId) {
                        R.id.todosFitlrados_reportes_review -> {
                            bottomSheet.cargandoContenido.isVisible = true
                            bottomSheet.noEncontrado.isVisible = false
                            bottomSheet.filtradoReviewTrabajadores.isVisible = false
                            obtener_denuncias_Review(
                                bottomSheet,
                                bottomSheet.linealChipsFiltradoestados
                            )
                        }

                        R.id.enviado -> {
                            buscarFitlrado_denuncia_review(
                                bottomSheet,
                                "enviados",
                                "review",
                                "enviado"
                            )
                        }

                        R.id.proceso -> {
                            buscarFitlrado_denuncia_review(
                                bottomSheet,
                                "enviados",
                                "review",
                                "proceso"
                            )
                        }

                        R.id.aceptado -> {
                            buscarFitlrado_denuncia_review(
                                bottomSheet,
                                "enviados",
                                "review",
                                "aceptado"
                            )
                        }

                        R.id.rechazado -> {
                            buscarFitlrado_denuncia_review(
                                bottomSheet,
                                "enviados",
                                "review",
                                "rechazado"
                            )
                        }

                        R.id.archivado -> {
                            buscarFitlrado_denuncia_review(
                                bottomSheet,
                                "enviados",
                                "review",
                                "archivado"
                            )
                        }

                        R.id.resuelto -> {
                            buscarFitlrado_denuncia_review(
                                bottomSheet,
                                "enviados",
                                "review",
                                "resuelto"
                            )
                        }

                        R.id.cancelado -> {
                            buscarFitlrado_denuncia_review(
                                bottomSheet,
                                "enviados",
                                "review",
                                "cancelado"
                            )
                        }
                    }
                }
            }

            else -> {
                bottomSheet.scroolReporesMismoTrabajador.isVisible = false
                bottomSheet.linealChipsFiltradoestados.isVisible = false
            }
        }



        dialog.setContentView(view)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun obtner_Denuncias_trabajadores(
        bottomSheet: BottomSheetReportesGeneralBinding,
        horizonntalScool: HorizontalScrollView,
        enviado_recivido: String
    ) {
        bottomSheet.TodosFiltrado.isChecked = true
        bottomSheet.todosFitlradosReportesReview.isChecked = true
        bottomSheet.cargandoContenido.isVisible = true
        bottomSheet.noEncontrado.isVisible = false
        bottomSheet.filtradoReviewTrabajadores.isVisible = false
        bottomSheet.linealGeneralCarga.isVisible = false
        val tiempoInicio = System.currentTimeMillis()
        val db = FirebaseFirestore.getInstance().collection("Trabajadores_Usuarios_Drivers")
            .document("trabajadores").collection("trabajadores")
            .document(firebaseAuth.uid.toString()).collection("reporte").document(enviado_recivido)
            .collection(enviado_recivido)
        bottomSheet.noEncontrado.isVisible = false
        db.get().addOnSuccessListener { res ->
            lista_reporte.clear()
            for (datos in res) {
                val data = datos.data
                val idTrabajador = data?.get("idTrabajador") as? String ?: ""
                val idUsuario = data?.get("idUsuario") as? String ?: ""
                val problema = data?.get("problema") as? String ?: ""
                val Tipo_reporte = data?.get("Tipo_reporte") as? String ?: ""
                val idReporte = data?.get("idReporte") as? String ?: ""
                val estado = data?.get("estado") as? String ?: ""
                val fecha_envio = data["fecha_envio"] as? String ?: ""
                val hora_envio = data["hora_envio"] as? String ?: ""

                val dataclass_reporte = dataclass_reporte_denuncia_tb(
                    idTrabajador,
                    idUsuario,
                    idReporte,
                    Tipo_reporte,
                    problema,
                    estado,
                    enviado_recivido, fecha_envio, hora_envio, ""
                )
                lista_reporte.add(dataclass_reporte)
            }

            val tiempoFin = System.currentTimeMillis()
            val duracion = tiempoFin - tiempoInicio

            Handler(Looper.getMainLooper()).postDelayed({
                if (lista_reporte.isNotEmpty()) {
                    inicializar_listaReporte(bottomSheet, enviado_recivido, enviado_recivido, "")
                    bottomSheet.noEncontrado.isVisible = false
                    bottomSheet.filtradoReviewTrabajadores.isVisible = true
                    bottomSheet.linealGeneralCarga.isVisible = true
                } else {
                    bottomSheet.noEncontrado.isVisible = true
                    bottomSheet.filtradoReviewTrabajadores.isVisible = false
                    bottomSheet.linealGeneralCarga.isVisible = true
                }
                bottomSheet.cargandoContenido.isVisible = false
            }, duracion)

            Log.d("FirestoreTiempo", "Tiempo total: $duracion ms (${duracion / 1000.0} segundos)")
        }.addOnFailureListener { e ->
            Log.d("error_econtrado", "Error al encontrar la referencia")
            bottomSheet.cargandoContenido.isVisible = false
            bottomSheet.noEncontrado.isVisible = true
            bottomSheet.filtradoReviewTrabajadores.isVisible = false
            bottomSheet.linealGeneralCarga.isVisible = true

            horizonntalScool.isVisible = false
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun buscarFiltrados(
        bottomSheet: BottomSheetReportesGeneralBinding,
        enviado_recivido: String, filtradoSelecionado: String
    ) {
        bottomSheet.cargandoContenido.isVisible = true
        bottomSheet.filtradoReviewTrabajadores.isVisible = false
        bottomSheet.linealGeneralCarga.isVisible = false

        val tiempoInicio = System.currentTimeMillis()
        val db = FirebaseFirestore.getInstance().collection("Trabajadores_Usuarios_Drivers")
            .document("trabajadores").collection("trabajadores")
            .document(firebaseAuth.uid.toString()).collection("reporte").document(enviado_recivido)
            .collection(enviado_recivido)
        bottomSheet.noEncontrado.isVisible = false
        db.get().addOnSuccessListener { res ->
            lista_reporte.clear()
            for (datos in res) {
                val data = datos.data
                val idTrabajador = data?.get("idTrabajador") as? String ?: ""
                val idUsuario = data?.get("idUsuario") as? String ?: ""
                val problema = data?.get("problema") as? String ?: ""
                val Tipo_reporte = data?.get("Tipo_reporte") as? String ?: ""
                val idReporte = data?.get("idReporte") as? String ?: ""
                val estado = data?.get("estado") as? String ?: ""
                val fecha_envio = data["fecha_envio"] as? String ?: ""
                val hora_envio = data["hora_envio"] as? String ?: ""
                if (estado.lowercase() == filtradoSelecionado) {
                    val dataclass_reporte = dataclass_reporte_denuncia_tb(
                        idTrabajador,
                        idUsuario,
                        idReporte,
                        Tipo_reporte,
                        problema,
                        estado,
                        enviado_recivido, fecha_envio, hora_envio, ""
                    )
                    lista_reporte.add(dataclass_reporte)
                }
            }

            val tiempoFin = System.currentTimeMillis()
            val duracion = tiempoFin - tiempoInicio

            Handler(Looper.getMainLooper()).postDelayed({
                if (lista_reporte.isNotEmpty()) {
                    inicializar_listaReporte(bottomSheet, enviado_recivido, enviado_recivido, "")
                    bottomSheet.noEncontrado.isVisible = false
                    bottomSheet.filtradoReviewTrabajadores.isVisible = true
                    bottomSheet.linealGeneralCarga.isVisible = true

                    bottomSheet.cargandoContenido.isVisible = false

                } else {
                    bottomSheet.noEncontrado.isVisible = true
                    bottomSheet.cargandoContenido.isVisible = false
                    bottomSheet.filtradoReviewTrabajadores.isVisible = false
                    bottomSheet.linealGeneralCarga.isVisible = true

                }
                bottomSheet.cargandoContenido.isVisible = false
            }, duracion)

            Log.d("FirestoreTiempo", "Tiempo total: $duracion ms (${duracion / 1000.0} segundos)")
        }.addOnFailureListener { e ->
            Log.d("error_econtrado", "Error al encontrar la referencia")
            bottomSheet.cargandoContenido.isVisible = false
            bottomSheet.noEncontrado.isVisible = true
            bottomSheet.filtradoReviewTrabajadores.isVisible = false
            bottomSheet.linealGeneralCarga.isVisible = false

        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun buscarFitlrado_denuncia_review(
        bottomSheet: BottomSheetReportesGeneralBinding,
        enviado_recivido: String, review: String, filtradoSelecionado: String
    ) {
        bottomSheet.cargandoContenido.isVisible = true
        bottomSheet.filtradoReviewTrabajadores.isVisible = false
        bottomSheet.linealGeneralCarga.isVisible = false

        val tiempoInicio = System.currentTimeMillis()
        val db = FirebaseFirestore.getInstance().collection("Trabajadores_Usuarios_Drivers")
            .document("trabajadores").collection("trabajadores")
            .document(firebaseAuth.uid.toString()).collection("reporte").document(enviado_recivido)
            .collection(review)
        bottomSheet.noEncontrado.isVisible = false
        db.get().addOnSuccessListener { res ->
            lista_reporte.clear()
            for (datos in res) {
                val data = datos.data
                val id_registrado = data["idUsuario"] as? String ?: ""
                val id_usuario_review = data["id_review"] as? String ?: ""
                val id_trabajador = data["idTrabajador"] as? String ?: ""
                val id_review = data["idReporte"] as? String ?: ""
                val incidencia = data["Tipo_reporte"] as? String ?: ""
                val descripcion = data["problema"] as? String ?: ""
                val estado = data["estado"] as? String ?: ""
                val fecha_envio = data["fecha_envio"] as? String ?: ""
                val hora_envio = data["hora_envio"] as? String ?: ""
                val idUsuario_review = data["idUsuario_review"] as? String ?: ""
                if (estado == filtradoSelecionado) {
                    val dataclass_reporte = dataclass_reporte_denuncia_tb(
                        id_usuario_review,
                        id_trabajador,
                        id_review,
                        incidencia,
                        descripcion,
                        estado,
                        "", hora_envio, fecha_envio, idUsuario_review
                    )
                    lista_reporte.add(dataclass_reporte)
                }
            }

            val tiempoFin = System.currentTimeMillis()
            val duracion = tiempoFin - tiempoInicio
            Handler(Looper.getMainLooper()).postDelayed({
                if (lista_reporte.isNotEmpty()) {
                    inicializar_listaReporte(bottomSheet, "enviados", "review", "review")
                    bottomSheet.noEncontrado.isVisible = false
                    bottomSheet.filtradoReviewTrabajadores.isVisible = true
                    bottomSheet.linealGeneralCarga.isVisible = true

                    bottomSheet.cargandoContenido.isVisible = false

                } else {
                    bottomSheet.noEncontrado.isVisible = true
                    bottomSheet.cargandoContenido.isVisible = false
                    bottomSheet.filtradoReviewTrabajadores.isVisible = false
                    bottomSheet.linealGeneralCarga.isVisible = true

                }
                bottomSheet.cargandoContenido.isVisible = false
            }, duracion)

            Log.d("FirestoreTiempo", "Tiempo total: $duracion ms (${duracion / 1000.0} segundos)")
        }.addOnFailureListener { e ->
            Log.d("error_econtrado", "Error al encontrar la referencia")
            bottomSheet.cargandoContenido.isVisible = false
            bottomSheet.noEncontrado.isVisible = true
            bottomSheet.filtradoReviewTrabajadores.isVisible = false
            bottomSheet.linealGeneralCarga.isVisible = false

        }
    }


    @RequiresApi(Build.VERSION_CODES.O)
    private fun inicializar_listaReporte(
        bottomSheet: BottomSheetReportesGeneralBinding,
        tipo1: String,
        tipo2: String,
        tipo_bottomSheet: String
    ) {
        val adapter = adapter_reporte_denuncia_tb(lista_reporte, { item ->
            dialog = BottomSheetDialog(this)
            bottomSheet_datos(tipo1, tipo2, item.idreporte.toString(), tipo_bottomSheet)
            dialog.show()
        }, { apelar ->
            dialog = BottomSheetDialog(this)
            habilitarBottomSheet_apelacion(apelar.idreporte.toString(), this, dialog, bottomSheet)
            dialog.show()
        }, { a_c_review ->
            enviar_cancelar_archivado(
                bottomSheet,
                this,
                "enviados",
                "review",
                a_c_review.idreporte.toString()
            )
        }, { a_c_reportes ->
            enviar_cancelar_archivado(
                bottomSheet,
                this,
                "enviados",
                "enviados",
                a_c_reportes.idreporte.toString()
            )
        })
        bottomSheet.filtradoReviewTrabajadores.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        bottomSheet.filtradoReviewTrabajadores.adapter = adapter
    }


    @RequiresApi(Build.VERSION_CODES.O)
    private fun enviar_cancelar_archivado(
        bottomSheet: BottomSheetReportesGeneralBinding,
        context: Context,
        doc1: String,
        doc2: String,
        seleccionado: String
    ) {
        val builder = AlertDialog.Builder(context)
        builder.setTitle("Confirmar acción")
        builder.setMessage("¿Deseas archivar o cancelar el reporte?")

        // Botón positivo - Archivar
        builder.setPositiveButton("Archivar Reporte") { dialog, _ ->
            cancelar_reporte(bottomSheet, "archivado", doc1, doc2, seleccionado)
            Toast.makeText(context, "Reporte archivado", Toast.LENGTH_SHORT).show()
            dialog.dismiss()

        }

        // Botón negativo - Cancelar
        builder.setNegativeButton("Cancelar Reporte") { dialog, _ ->
            cancelar_reporte(bottomSheet, "cancelado", doc1, doc2, seleccionado)
            Toast.makeText(context, "Reporte cancelado", Toast.LENGTH_SHORT).show()
            dialog.dismiss()

        }

        // Botón neutral - Cerrar sin acción
        builder.setNeutralButton("Cerrar") { dialog, _ ->
            dialog.dismiss()

        }

        val dialog = builder.create()
        dialog.show()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun cancelar_reporte(
        bottomSheet: BottomSheetReportesGeneralBinding,
        tipo_cambiado: String,
        doc1: String,
        doc2: String,
        seleccionado: String
    ) {
        firebaseAuth = FirebaseAuth.getInstance()
        constantes_vinculados.encotrar_user(firebaseAuth.uid.toString()) { tipo, collection ->
            if (collection != null) {
                val reporteRef = collection
                    .document(firebaseAuth.uid.toString())
                    .collection("reporte")
                    .document(doc1)
                    .collection(doc2)
                    .document(seleccionado)

                val hashMap = hashMapOf<String, Any>(
                    "estado" to tipo_cambiado
                )

                reporteRef.set(hashMap, SetOptions.merge())
                    .addOnSuccessListener {
                        if (doc2 == "enviados") {
                            obtner_Denuncias_trabajadores(
                                bottomSheet,
                                bottomSheet.linealChipsFiltradoestados,
                                "enviados"
                            )
                        } else if (doc2 == "review") {
                            obtener_denuncias_Review(
                                bottomSheet,
                                bottomSheet.linealChipsFiltradoestados
                            )
                        }
                        Log.d("Reporte", "Estado actualizado correctamente.")
                    }
                    .addOnFailureListener { e ->
                        Log.e("Reporte", "Error al actualizar el estado", e)
                    }
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun habilitarBottomSheet_apelacion(
        id_reporte: String,
        context: Context,
        dialog: BottomSheetDialog,
        bottomSheet: BottomSheetReportesGeneralBinding,
    ) {
        firebaseAuth = FirebaseAuth.getInstance()
        val binding = BottomSheetAplarReporteBinding.inflate(LayoutInflater.from(context))

        binding.btnCancel.setOnClickListener {
            dialog.dismiss()
        }
        constantesCarrito.setearDatosUsuario { nombre, numero, localidad, apellido ->
            binding.nombreED.setText(nombre)
            binding.apellidosED.setText(apellido)
            binding.telefonoed.setText(numero)
        }
        binding.idApelacioProblema.text = id_reporte

        constantesCarrito.obtnerfechaHora(binding.hora, binding.fecha)

        binding.btnApply.setOnClickListener {
            val nombre = binding.nombreED.text.toString().trim()
            val apellido = binding.apellidosED.text.toString().trim()
            val telefono = binding.telefonoed.text.toString().trim()
            val motivo = binding.motivoED.text.toString().trim()
            val detalles = binding.dellatesED.text.toString().trim()

            if (nombre.isEmpty() || apellido.isEmpty() || telefono.isEmpty() || motivo.isEmpty() || detalles.isEmpty()) {
                Toast.makeText(
                    context,
                    "Complete todo los campos para enviar",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                val db =
                    FirebaseFirestore.getInstance().collection("Trabajadores_Usuarios_Drivers")
                        .document("trabajadores").collection("trabajadores")
                        .document(firebaseAuth.uid.toString())
                        .collection("reporte").document("recivido").collection("recivido")
                        .document(id_reporte)
                val hasmap =
                    hashMapOf<String, Any>(
                        "idReporte" to id_reporte,
                        "fecha_envio" to binding.fecha.text.toString(),
                        "hora_envio" to binding.hora.text.toString(),
                        "nombre" to nombre,
                        "apellido" to apellido,
                        "numero" to telefono,
                        "motivo" to motivo,
                        "detalle" to detalles
                    )
                val datos_apelados = hashMapOf<String, Any>(
                    "apelado" to hasmap
                )
                db.set(datos_apelados, SetOptions.merge()).addOnSuccessListener { res ->
                    Toast.makeText(
                        this,
                        "apelacion enviada correctamente",
                        Toast.LENGTH_SHORT
                    ).show()
                    db.get().addOnSuccessListener { res ->
                        obtner_Denuncias_trabajadores(
                            bottomSheet,
                            bottomSheet.scroolReporesMismoTrabajador,
                            "recivido"
                        )
                        val hashmapEstado = hashMapOf<String, Any>(
                            "estado" to "apelado"
                        )
                        db.set(hashmapEstado, SetOptions.merge()).addOnSuccessListener { res ->
                            Log.d("estado_cambiado", "estado cambiado correctamente")

                            dialog.dismiss()
                        }.addOnFailureListener { e ->
                            Log.d("error_cambiarEstado", "error al cambiar el estado")
                        }
                    }

                }.addOnFailureListener { e ->
                    Toast.makeText(
                        this,
                        "error al enviar la apelacion",
                        Toast.LENGTH_SHORT
                    ).show()
                }

            }

        }
        dialog.setContentView(binding.root)
    }

    private fun bottomSheet_datos(
        tipo1: String,
        tipo2: String,
        idSelect: String,
        tipo_bottomSheet: String
    ) {
        val bottomSheet = BottomSheetInformacionReportesDenunciasBinding.inflate(layoutInflater)
        val view = bottomSheet.root
        val startTime = System.currentTimeMillis()
        bottomSheet.progressvarCargando.isVisible = true
        bottomSheet.linealCargadoDatos.isVisible = false
        val db =
            FirebaseFirestore.getInstance().collection("Trabajadores_Usuarios_Drivers")
                .document("trabajadores").collection("trabajadores")
                .document(firebaseAuth.uid.toString()).collection("reporte").document(tipo1)
                .collection(tipo2).document(idSelect)
        db.get().addOnSuccessListener { res ->
            val endTime = System.currentTimeMillis()
            val duration = endTime - startTime
            if (res.exists()) {

                val data = res.data
                val idReporte = data?.get("idReporte") as? String ?: ""
                val idTrabajador = data?.get("idTrabajador") as? String ?: ""
                val idUsuario = data?.get("idUsuario") as? String ?: ""
                val Tipo_reporte = data?.get("Tipo_reporte") as? String ?: ""
                val problema = data?.get("problema") as? String ?: ""
                val estado = data?.get("estado") as? String ?: ""
                val numero_contacto = data?.get("numero_contacto") as? String ?: ""
                val apelacionMap = data?.get("apelado") as? Map<*, *>
                if (tipo_bottomSheet == "review") {
                    val idUsuarioReview = data?.get("idUsuario_review") as? String ?: ""
                    val idREgistrad = data?.get("id_registrado") as? String ?: ""
                    constantesCarrito.setearDatosUsuarioImgNombre(idREgistrad) { nombre, img, apellido, nacionalidad, categoria, verificado, trabajador_user ->
                        bottomSheet.enviadoPor.text = "$nombre $apellido"
                    }
                    constantesCarrito.setearDatosUsuarioImgNombre(idUsuarioReview) { nombre, img, apellido, nacionalidad, categoria, verificado, trabajador_user ->
                        bottomSheet.haciaEl.text = "$nombre $apellido"
                    }
                } else {
                    constantesCarrito.setearDatosUsuarioImgNombre(idUsuario) { nombre, img, apellido, nacionalidad, categoria, verificado, trabajador_user ->
                        bottomSheet.enviadoPor.text = "$nombre $apellido"
                    }
                    constantesCarrito.setearDatosUsuarioImgNombre(idTrabajador) { nombre, img, apellido, nacionalidad, categoria, verificado, trabajador_user ->
                        bottomSheet.haciaEl.text = "$nombre $apellido"
                    }
                }



                Handler(Looper.getMainLooper()).postDelayed({
                    bottomSheet.progressvarCargando.isVisible = false
                    bottomSheet.linealCargadoDatos.isVisible = true
                }, duration)
                if (apelacionMap != null && apelacionMap.isNotEmpty()) {
                    bottomSheet.linealApelado.isVisible = true
                    val nombre = apelacionMap["nombre"] as? String
                    val apellido = apelacionMap["apellido"] as? String
                    val numero = apelacionMap["numero"] as? String
                    val fecha = apelacionMap["fecha_envio"] as? String
                    val hora = apelacionMap["hora_envio"] as? String
                    val motivo = apelacionMap["motivo"] as? String
                    val detalle = apelacionMap["detalle"] as? String
                    bottomSheet.nombreApeladoPor.text = "$nombre $apellido"
                    bottomSheet.fechaApelado.text = fecha
                    bottomSheet.horaApelacion.text = hora
                    bottomSheet.numeroContactoApelacion.text = numero
                    bottomSheet.detalleApelado.text = detalle
                    bottomSheet.motivoApelado.text = motivo
                } else {
                    bottomSheet.linealApelado.isVisible = false
                }
                bottomSheet.idReporte.text = idReporte


                if (numero_contacto.isNullOrEmpty()) {
                    bottomSheet.linealContacto.isVisible = false
                } else {
                    bottomSheet.numeroContacto.text = numero_contacto
                }
                bottomSheet.motivoReporte.text = Tipo_reporte
                bottomSheet.incidencia.text = problema
                bottomSheet.estadoReporte.text = estado


            }
        }.addOnFailureListener { e ->
            Toast.makeText(this, "erro al obtner $e", Toast.LENGTH_SHORT).show()
        }

        bottomSheet.copyId.setOnClickListener {
            constantestextos_general.copiarTexto_portapapeles(bottomSheet.idReporte, this)

        }
        dialog.setContentView(view)

    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun obtener_denuncias_Review(
        bottomSheet: BottomSheetReportesGeneralBinding,
        horizonntalScool: HorizontalScrollView
    ) {
        bottomSheet.todosFitlradosReportesReview.isChecked = true
        bottomSheet.cargandoContenido.isVisible = true
        bottomSheet.noEncontrado.isVisible = false
        bottomSheet.filtradoReviewTrabajadores.isVisible = false
        bottomSheet.linealGeneralCarga.isVisible = false
        val tiempoInicio = System.currentTimeMillis()
        constantes_vinculados.encotrar_user(firebaseAuth.uid.toString()) { tipo, colleccion ->
            if (colleccion != null) {
                val dbUsuario =
                    colleccion.document(firebaseAuth.uid.toString()).collection("reporte")
                        .document("enviados").collection("review")
                dbUsuario.get().addOnSuccessListener { res ->
                    lista_reporte.clear()
                    for (datos in res) {
                        val data = datos.data
                        val id_registrado = data["id_registrado"] as? String ?: ""
                        val id_usuario_review = data["id_review"] as? String ?: ""
                        val id_trabajador = data["idTrabajador"] as? String ?: ""
                        val id_review = data["idReporte"] as? String ?: ""
                        val incidencia = data["Tipo_reporte"] as? String ?: ""
                        val descripcion = data["problema"] as? String ?: ""
                        val estado = data["estado"] as? String ?: ""
                        val idUsuario_review = data["idUsuario_review"] as? String ?: ""
                        val fecha_envio = data["fecha_envio"] as? String ?: ""
                        val hora_envio = data["hora_envio"] as? String ?: ""
                        if (id_registrado == firebaseAuth.uid.toString()) {
                            val dataclass_reporte = dataclass_reporte_denuncia_tb(
                                id_usuario_review,
                                id_trabajador,
                                id_review,
                                incidencia,
                                descripcion,
                                estado,
                                "", hora_envio, fecha_envio, idUsuario_review
                            )
                            lista_reporte.add(dataclass_reporte)
                        }
                    }

                    if (lista_reporte.isNotEmpty()) {
                        inicializar_listaReporte(bottomSheet, "enviados", "review", "review")
                    } else {
                        bottomSheet.noEncontrado.isVisible = true
                        bottomSheet.linealGeneralCarga.isVisible = true
                        bottomSheet.filtradoReviewTrabajadores.isVisible = true

                    }
                    val tiempoFin = System.currentTimeMillis()
                    val duracion = tiempoFin - tiempoInicio
                    Handler(Looper.getMainLooper()).postDelayed({
                        bottomSheet.cargandoContenido.isVisible = false
                        bottomSheet.linealGeneralCarga.isVisible = true
                        bottomSheet.filtradoReviewTrabajadores.isVisible = true

                        horizonntalScool.isVisible = true

                    }, duracion)
                    Log.d(
                        "FirestoreTiempo",
                        "Tiempo total: $duracion ms (${duracion / 1000.0} segundos)"
                    )
                }
            }
        }

    }


}