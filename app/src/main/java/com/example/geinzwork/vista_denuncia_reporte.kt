package com.example.geinzwork

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.HorizontalScrollView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.geinzwork.adapterViewholder.adapter_reporte_denuncia_tb
import com.example.geinzwork.constantesGeneral.Variables
import com.example.geinzwork.constantesGeneral.constantes_vinculados
import com.example.geinzwork.dataclass.dataclass_reporte_denuncia_tb
import com.geinzz.geinzwork.R
import com.geinzz.geinzwork.adapterViewholder.adapterReportes
import com.geinzz.geinzwork.constantesGeneral.constantesCarrito
import com.geinzz.geinzwork.constantesGeneral.constantestextos_general
import com.geinzz.geinzwork.databinding.ActivityVistaDenunciaReporteBinding
import com.geinzz.geinzwork.databinding.BottomSheetInformacionReportesDenunciasBinding
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
        obtner_Denuncias_trabajadores(binding.scroolReporesMismoTrabajador, "recivido")
        binding.grupoReporesUser.setOnCheckedChangeListener { group, checkedId ->
            when (checkedId) {
                R.id.TodosFiltrado -> {
                    obtner_Denuncias_trabajadores(binding.scroolReporesMismoTrabajador, "recivido")
                }

                R.id.aplelado -> {
                    buscarFiltrados("recivido", "apelado")
                }

                R.id.por_apelar -> {
                    buscarFiltrados("recivido", "por apelar")
                }

                R.id.apelcaionAceptada -> {
                    buscarFiltrados("recivido", "apelacion aceptada")
                }

                R.id.aplecion_rechazada -> {
                    buscarFiltrados("recivido", "apelacion rechazada")
                }
            }
        }

        binding.chipGroupestados.setOnCheckedChangeListener { group, checkedId ->
            when (checkedId) {
                R.id.todosFitlrados_reportes_review -> {
                    obtner_Denuncias_trabajadores(binding.linealChipsFiltradoestados, "enviados")
                }

                R.id.enviado -> {
                    buscarFiltrados("enviados", "enviado")
                }

                R.id.proceso -> {
                    buscarFiltrados("enviados", "proceso")
                }

                R.id.aceptado -> {
                    buscarFiltrados("enviados", "aceptado")
                }

                R.id.rechazado -> {
                    buscarFiltrados("enviados", "rechazado")
                }

                R.id.archivado -> {
                    buscarFiltrados("enviados", "archivado")
                }

                R.id.resuelto -> {
                    buscarFiltrados("enviados", "resuelto")
                }

                R.id.cancelado -> {
                    buscarFiltrados("enviados", "cancelado")
                }
            }
        }



        binding.trabajadoresReportes.setOnClickListener {
            // Asegúrate de que primero se limpie y luego se marque el chip deseado
            binding.grupoReporesUser.clearCheck()
            binding.chipGroupestados.clearCheck()

            // Marcar el chip "Todos"
            binding.todosFitlradosReportesReview.isChecked = true

            // Ocultar filtros
            binding.filtradoReviewTrabajadores.isVisible = false
            binding.linealChipsFiltradoestados.isVisible = false

            // Mostrar carga y ocultar contenido
            binding.cargandoContenido.isVisible = true
            binding.scroolReporesMismoTrabajador.isVisible = false

            // Llamar a la función
            obtner_Denuncias_trabajadores(binding.linealChipsFiltradoestados, "enviados")
        }

        binding.reviewReportes.setOnClickListener {
            binding.linealChipsFiltradoestados.isVisible = false
            binding.filtradoReviewTrabajadores.isVisible = false
            obtener_denuncias_Review(binding.linealChipsFiltradoestados)
            binding.grupoReporesUser.clearCheck()
            binding.cargandoContenido.isVisible = true
            binding.scroolReporesMismoTrabajador.isVisible = false
            binding.chipGroupestados.clearCheck()
            binding.todosFitlradosReportesReview.isChecked = false // por si ya estaba marcado antes
            binding.todosFitlradosReportesReview.isChecked = true

        }
        binding.tusReportes.setOnClickListener {
            // Limpiar selecciones antes de setear el chip deseado
            binding.chipGroupestados.clearCheck()
            binding.grupoReporesUser.clearCheck()

            // Forzar el chequeo del chip "Todos"
            binding.TodosFiltrado.isChecked = false // por si ya estaba marcado antes
            binding.TodosFiltrado.isChecked = true
            binding.linealChipsFiltradoestados.isVisible = false

            // Obtener denuncias
            obtner_Denuncias_trabajadores(binding.scroolReporesMismoTrabajador, "recivido")

            // Control de visibilidad
            binding.filtradoReviewTrabajadores.isVisible = false
            binding.cargandoContenido.isVisible = true
            binding.linealChipsFiltradoestados.isVisible = false
        }

    }

    private fun obtner_Denuncias_trabajadores(
        horizonntalScool: HorizontalScrollView,
        enviado_recivido: String
    ) {
        binding.cargandoContenido.isVisible = true
        binding.noEncontrado.isVisible = false
        binding.filtradoReviewTrabajadores.isVisible = false
        val tiempoInicio = System.currentTimeMillis()
        val db = FirebaseFirestore.getInstance().collection("Trabajadores_Usuarios_Drivers")
            .document("trabajadores").collection("trabajadores")
            .document(firebaseAuth.uid.toString()).collection("reporte").document(enviado_recivido)
            .collection(enviado_recivido)
        binding.noEncontrado.isVisible = false
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
                    inicializar_listaReporte(enviado_recivido,enviado_recivido)
                    binding.noEncontrado.isVisible = false
                    binding.filtradoReviewTrabajadores.isVisible = true
                    horizonntalScool.isVisible = true
                } else {
                    binding.noEncontrado.isVisible = true
                    binding.filtradoReviewTrabajadores.isVisible = false
                    horizonntalScool.isVisible = false
                }
                binding.cargandoContenido.isVisible = false
            }, duracion)

            Log.d("FirestoreTiempo", "Tiempo total: $duracion ms (${duracion / 1000.0} segundos)")
        }.addOnFailureListener { e ->
            Log.d("error_econtrado", "Error al encontrar la referencia")
            binding.cargandoContenido.isVisible = false
            binding.noEncontrado.isVisible = true
            binding.filtradoReviewTrabajadores.isVisible = false
            horizonntalScool.isVisible = false
        }
    }

    private fun buscarFiltrados(
        enviado_recivido: String, filtradoSelecionado: String
    ) {
        binding.cargandoContenido.isVisible = true
        binding.filtradoReviewTrabajadores.isVisible = false
        val tiempoInicio = System.currentTimeMillis()
        val db = FirebaseFirestore.getInstance().collection("Trabajadores_Usuarios_Drivers")
            .document("trabajadores").collection("trabajadores")
            .document(firebaseAuth.uid.toString()).collection("reporte").document(enviado_recivido)
            .collection(enviado_recivido)
        binding.noEncontrado.isVisible = false
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
                    inicializar_listaReporte(enviado_recivido,enviado_recivido)
                    binding.noEncontrado.isVisible = false
                    binding.filtradoReviewTrabajadores.isVisible = true
                    binding.cargandoContenido.isVisible = false

                } else {
                    binding.noEncontrado.isVisible = true
                    binding.cargandoContenido.isVisible = false
                    binding.filtradoReviewTrabajadores.isVisible = false
                }
                binding.cargandoContenido.isVisible = false
            }, duracion)

            Log.d("FirestoreTiempo", "Tiempo total: $duracion ms (${duracion / 1000.0} segundos)")
        }.addOnFailureListener { e ->
            Log.d("error_econtrado", "Error al encontrar la referencia")
            binding.cargandoContenido.isVisible = false
            binding.noEncontrado.isVisible = true
            binding.filtradoReviewTrabajadores.isVisible = false
        }
    }


    private fun inicializar_listaReporte(tipo1: String,tipo2:String) {
        val adapter = adapter_reporte_denuncia_tb(lista_reporte) { item ->
            dialog = BottomSheetDialog(this)
            bottomSheet_datos(tipo1, tipo2,item.idreporte.toString())
            dialog.show()

        }
        binding.filtradoReviewTrabajadores.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        binding.filtradoReviewTrabajadores.adapter = adapter
    }

    private fun bottomSheet_datos(tipo1: String, tipo2:String,idSelect: String) {
        val bottomSheet = BottomSheetInformacionReportesDenunciasBinding.inflate(layoutInflater)
        val view = bottomSheet.root

        val db =
            FirebaseFirestore.getInstance().collection("Trabajadores_Usuarios_Drivers")
                .document("trabajadores").collection("trabajadores")
                .document(firebaseAuth.uid.toString()).collection("reporte").document(tipo1)
                .collection(tipo2).document(idSelect)
        db.get().addOnSuccessListener { res ->
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
                bottomSheet.numeroContacto.text = numero_contacto
                bottomSheet.motivoReporte.text = Tipo_reporte
                bottomSheet.incidencia.text = problema
                bottomSheet.estadoReporte.text = estado


            }
        }

        bottomSheet.copyId.setOnClickListener {
            constantestextos_general.copiarTexto_portapapeles(bottomSheet.idReporte, this)

        }
        dialog.setContentView(view)

    }

    private fun obtener_denuncias_Review(horizonntalScool: HorizontalScrollView) {
        binding.noEncontrado.isVisible = false
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
                        val id_usuario_review = data["id_usuario_review"] as? String ?: ""
                        val id_trabajador = data["id_trabajador"] as? String ?: ""
                        val id_review = data["id_review"] as? String ?: ""
                        val incidencia = data["incidencia"] as? String ?: ""
                        val descripcion = data["descripcion"] as? String ?: ""
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
                        inicializar_listaReporte("enviados","review")
                    } else {
                        binding.noEncontrado.isVisible = true
                        binding.filtradoReviewTrabajadores.isVisible = false
                    }
                    val tiempoFin = System.currentTimeMillis()
                    val duracion = tiempoFin - tiempoInicio
                    Handler(Looper.getMainLooper()).postDelayed({
                        binding.cargandoContenido.isVisible = false
                        binding.filtradoReviewTrabajadores.isVisible = true
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