package com.example.geinzwork

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
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.geinzwork.adapterViewholder.adapter_reporte_denuncia_tb
import com.example.geinzwork.constantesGeneral.constantes_vinculados
import com.example.geinzwork.dataclass.dataclass_reporte_denuncia_tb
import com.geinzz.geinzwork.R
import com.geinzz.geinzwork.constantesGeneral.constantesCarrito
import com.geinzz.geinzwork.constantesGeneral.constantestextos_general
import com.geinzz.geinzwork.databinding.ActivityVistaDenunciaReporteBinding
import com.geinzz.geinzwork.databinding.BottomSheetInformacionReportesDenunciasBinding
import com.geinzz.geinzwork.databinding.BottomSheetReportesGeneralBinding
import com.geinzz.geinzwork.dataclass.dataClassReportes
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class vista_denuncia_reporte : AppCompatActivity() {
    private lateinit var binding: ActivityVistaDenunciaReporteBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private val lista = mutableListOf<dataClassReportes>()
    private lateinit var dialog: BottomSheetDialog
    private val lista_reporte = mutableListOf<dataclass_reporte_denuncia_tb>()
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
                obtner_Denuncias_trabajadores(
                    bottomSheet,
                    bottomSheet.scroolReporesMismoTrabajador,
                    "recivido"
                )
                bottomSheet.cargandoContenido.isVisible = true
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

    private fun obtner_Denuncias_trabajadores(
        bottomSheet: BottomSheetReportesGeneralBinding,
        horizonntalScool: HorizontalScrollView,
        enviado_recivido: String
    ) {
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
                    enviado_recivido, fecha_envio, hora_envio
                )
                lista_reporte.add(dataclass_reporte)
            }

            val tiempoFin = System.currentTimeMillis()
            val duracion = tiempoFin - tiempoInicio

            Handler(Looper.getMainLooper()).postDelayed({
                if (lista_reporte.isNotEmpty()) {
                    inicializar_listaReporte(bottomSheet, enviado_recivido, enviado_recivido)
                    bottomSheet.noEncontrado.isVisible = false
                    bottomSheet.filtradoReviewTrabajadores.isVisible = true
                    bottomSheet.linealGeneralCarga.isVisible = true

                } else {
                    bottomSheet.noEncontrado.isVisible = true
                    bottomSheet.filtradoReviewTrabajadores.isVisible = false
                    bottomSheet.linealGeneralCarga.isVisible = false

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

            horizonntalScool.isVisible = false
        }
    }

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
                if (estado.toLowerCase() == filtradoSelecionado) {
                    val dataclass_reporte = dataclass_reporte_denuncia_tb(
                        idTrabajador,
                        idUsuario,
                        idReporte,
                        Tipo_reporte,
                        problema,
                        estado,
                        enviado_recivido, fecha_envio, hora_envio
                    )
                    lista_reporte.add(dataclass_reporte)
                }
            }

            val tiempoFin = System.currentTimeMillis()
            val duracion = tiempoFin - tiempoInicio

            Handler(Looper.getMainLooper()).postDelayed({
                if (lista_reporte.isNotEmpty()) {
                    inicializar_listaReporte(bottomSheet, enviado_recivido, enviado_recivido)
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
                if (estado == filtradoSelecionado) {
                    val dataclass_reporte = dataclass_reporte_denuncia_tb(
                        id_usuario_review,
                        id_trabajador,
                        id_review,
                        incidencia,
                        descripcion,
                        estado,
                        "", hora_envio, fecha_envio
                    )
                    lista_reporte.add(dataclass_reporte)
                }
            }

            val tiempoFin = System.currentTimeMillis()
            val duracion = tiempoFin - tiempoInicio
            Handler(Looper.getMainLooper()).postDelayed({
                if (lista_reporte.isNotEmpty()) {
                    inicializar_listaReporte(bottomSheet, "enviados", "review")
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


    private fun inicializar_listaReporte(
        bottomSheet: BottomSheetReportesGeneralBinding,
        tipo1: String,
        tipo2: String
    ) {
        val adapter = adapter_reporte_denuncia_tb(lista_reporte) { item ->
            dialog = BottomSheetDialog(this)
            bottomSheet_datos(tipo1, tipo2, item.idreporte.toString())
            dialog.show()

        }
        bottomSheet.filtradoReviewTrabajadores.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        bottomSheet.filtradoReviewTrabajadores.adapter = adapter
    }

    private fun bottomSheet_datos(tipo1: String, tipo2: String, idSelect: String) {
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
                constantesCarrito.setearDatosUsuarioImgNombre(idUsuario) { nombre, img, apellido, nacionalidad, categoria, verificado, trabajador_user ->
                    bottomSheet.enviadoPor.text = "$nombre $apellido"
                }
                constantesCarrito.setearDatosUsuarioImgNombre(idTrabajador) { nombre, img, apellido, nacionalidad, categoria, verificado, trabajador_user ->
                    bottomSheet.haciaEl.text = "$nombre $apellido"
                }
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

    private fun obtener_denuncias_Review(
        bottomSheet: BottomSheetReportesGeneralBinding,
        horizonntalScool: HorizontalScrollView
    ) {
        bottomSheet.linealGeneralCarga.isVisible = false
        bottomSheet.noEncontrado.isVisible = false
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
                                "", hora_envio, fecha_envio
                            )
                            lista_reporte.add(dataclass_reporte)
                        }
                    }

                    if (lista_reporte.isNotEmpty()) {
                        inicializar_listaReporte(bottomSheet, "enviados", "review")
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