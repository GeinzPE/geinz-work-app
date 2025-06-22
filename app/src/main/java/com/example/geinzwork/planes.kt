package com.geinzz.geinzwork

import android.graphics.Typeface
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.echo.holographlibrary.Line
import com.example.geinzwork.NotificacionRS
import com.example.geinzwork.constantesGeneral.Variables
import com.example.geinzwork.constantesGeneral.obtenertokenIdAdmin
import com.geinzz.geinzwork.constantesGeneral.constantes
import com.geinzz.geinzwork.constantesGeneral.constantesCarrito
import com.geinzz.geinzwork.constantesGeneral.constantesPublicidad
import com.geinzz.geinzwork.databinding.ActivityPlanesBinding
import com.geinzz.geinzwork.databinding.BottomSheetAdquirPlanesNoticiasBinding
import com.geinzz.geinzwork.databinding.BottomSheetInfoPublicidadBinding
import com.geinzz.geinzwork.databinding.FragmentSinInternetBinding
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class planes : AppCompatActivity() {
    private lateinit var binding: ActivityPlanesBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var dialog: BottomSheetDialog

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPlanesBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        val intentRenovacion = intent.getStringExtra(Variables.renovacion).toString()
        firebaseAuth = FirebaseAuth.getInstance()
        val caracteristicasbasica = listOf(
            getString(R.string.primario),
            getString(R.string.segundario),
            getString(R.string.tercero),
            getString(R.string.cuarto),
            getString(R.string.quinto)
        )
        val caracteristicaAvanzado = listOf(
            getString(R.string.publicacion_7_dias),
            getString(R.string.publicidad_apartado_medio),
            getString(R.string.compartir_publicidad),
            getString(R.string.orientacion_publicidad),
            getString(R.string.acceso_panel_control),
            getString(R.string.enlace_directo_web),
            getString(R.string.cambio_publicidad_periodo),
        )

        val caracteristicaPremiun = listOf(
            getString(R.string.publicacion_1_mes),
            getString(R.string.publicidad_apartado_alto),
            getString(R.string.compartir_publicidad),
            getString(R.string.orientacion_publicidad),
            getString(R.string.enlace_directo_web),
            getString(R.string.acceso_panel_control),
            getString(R.string.cambio_publicidad_periodo),
            getString(R.string.noticias_incluida)
        )

        planesBasico(caracteristicasbasica, intentRenovacion)
        plan_avanzado(caracteristicaAvanzado, intentRenovacion)
        plan_premiun(caracteristicaPremiun, intentRenovacion)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun planesBasico(caracteristicas: List<String>, renovacion: String) {
        InfladoPlanes(caracteristicas, binding.planBasico.linealCaracteristicasLayout)
        binding.planBasico.precio.text = getString(R.string.precio_plan_basico)
        binding.planBasico.planNombre.text = getString(R.string.plan_nombre_B)
        binding.planBasico.verMas.setOnClickListener {
            dialog = BottomSheetDialog(this)
            enviarFormularioGeinz(
                dialog,
                getString(R.string.plan_basico_descripcion),
                getString(R.string.precio_plan_basico),
                getString(R.string.tipo_servicio_plan_basico), renovacion
            )
            dialog.show()
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun plan_avanzado(caracteristicas: List<String>, renovacion: String) {
        InfladoPlanes(caracteristicas, binding.planAvanzado.linealCaracteristicasLayout)
        binding.planAvanzado.precio.text = getString(R.string.precio_plan_avanzado)
        binding.planAvanzado.planNombre.text = getString(R.string.plan_nombre_A)
        binding.planAvanzado.verMas.setOnClickListener {
            dialog = BottomSheetDialog(this)
            enviarFormularioGeinz(
                dialog,
                getString(R.string.plan_avanzado_descripcion),
                getString(R.string.precio_plan_avanzado),
                getString(R.string.tipo_servicio_plan_avanzado), renovacion
            )
            dialog.show()
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun plan_premiun(caracteristicas: List<String>, renovacion: String) {
        InfladoPlanes(caracteristicas, binding.PlanPremiun.linealCaracteristicasLayout)
        binding.PlanPremiun.precio.text = getString(R.string.precio_plan_premium)
        binding.PlanPremiun.planNombre.text = getString(R.string.plan_nombre_P)
        binding.PlanPremiun.verMas.setOnClickListener {
            dialog = BottomSheetDialog(this)
            enviarFormularioGeinz(
                dialog,
                getString(R.string.plan_premium_descripcion),
                getString(R.string.precio_plan_premium),
                getString(R.string.tipo_servicio_plan_premium), renovacion
            )
            dialog.show()
        }
    }

    private fun InfladoPlanes(caracteristicas: List<String>, layoutParams: LinearLayout) {
        val layout = layoutParams
        layout.removeAllViews()
        for (caracteristicastxt in caracteristicas) {
            val textView = TextView(this)
            val layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            layoutParams.setMargins(0, 8, 0, 8)
            textView.layoutParams = layoutParams

            textView.text = caracteristicastxt
            textView.textSize = 15f
            textView.setTypeface(null, Typeface.BOLD)
            textView.setTextAppearance(R.style.TextoModoOScuroColor)
            layout.addView(textView)
        }
    }

    private fun verificarCampos(bindingBottomShett: BottomSheetAdquirPlanesNoticiasBinding): Boolean {
        val nombre = bindingBottomShett.nombreED.text
        val apellido = bindingBottomShett.apellidosED.text
        val dni = bindingBottomShett.dniED.text
        val telefono = bindingBottomShett.telefonoed.text
        var isValid = true

        if (nombre.isNullOrBlank()) {
            bindingBottomShett.nombreED.error = getString(R.string.campo_nombre)
            isValid = false
        }
        if (apellido.isNullOrBlank()) {
            bindingBottomShett.apellidosED.error = getString(R.string.campo_apellido)
            isValid = false
        }
        if (dni.isNullOrBlank()) {
            bindingBottomShett.dniED.error = getString(R.string.campo_dni)
            isValid = false
        }
        if (telefono.isNullOrBlank()) {
            bindingBottomShett.telefonoed.error = getString(R.string.campo_telefono)
            isValid = false
        }
        return isValid
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun enviarFormularioGeinz(
        dialog: BottomSheetDialog,
        descripcion: String,
        total: String,
        tipo_servicio: String,
        renovacion: String
    ) {
        val bindingBottomSheet =
            BottomSheetAdquirPlanesNoticiasBinding.inflate(LayoutInflater.from(this))
        BottomSheetDialog(this)


        if (firebaseAuth.currentUser == null) {
            constantesPublicidad.CreacionCuentaBottom_shett(this, dialog)
            dialog.show()
            return
        }
        bindingBottomSheet.btnCancel.setOnClickListener {
            dialog.dismiss()
        }

        bindingBottomSheet.descripcion.text = descripcion
        bindingBottomSheet.tipoServicio.text = tipo_servicio
        bindingBottomSheet.Total.text = total

        constantesCarrito.setearDatosUsuario { nombre, numero, localida, apellido ->
            bindingBottomSheet.nombreED.setText(nombre)
            bindingBottomSheet.apellidosED.setText(apellido)
            bindingBottomSheet.telefonoed.setText(numero)
        }
        constantesCarrito.obtnerfechaHora(bindingBottomSheet.hora, bindingBottomSheet.fecha)

        bindingBottomSheet.cerrar.setOnClickListener {
            dialog.dismiss()
        }

        bindingBottomSheet.btnApply.setOnClickListener {
            val dbVerificarPublicidad = FirebaseFirestore.getInstance()
                .collection(Variables.solicitudes_serviciosDB)
                .document(Variables.publicidadBanerDB)
                .collection(Variables.activos)
                .document(firebaseAuth.uid.toString())

            dbVerificarPublicidad.get().addOnSuccessListener { res ->
                if (res.exists()) {
                    val documentId = res.id
                    if (documentId == firebaseAuth.uid && renovacion != "r") {
                        Toast.makeText(this, "Ya tiene un servicio de Anuncio activo", Toast.LENGTH_SHORT).show()
                        dialog.dismiss()
                        return@addOnSuccessListener
                    }
                }

                if (!verificarCampos(bindingBottomSheet)) {
                    return@addOnSuccessListener
                }

                constantes.obtenerToken_trabajador(firebaseAuth.uid.toString()) { tokenUser, nombre, apellido ->
                    val dbGeneral = FirebaseFirestore.getInstance()
                        .collection(Variables.solicitudes_serviciosDB)
                        .document(Variables.publicidadBanerDB)

                    val referencia = if (renovacion == "r") {
                        dbGeneral.collection(Variables.renovarDB)
                    } else {
                        dbGeneral.collection(Variables.pendientesDB)
                    }

                    val tituloNotificacion = getString(
                        if (renovacion == "r") R.string.titulo_notificacion_anuncio_renovacion else R.string.titulo_notificacion_anuncio
                    )
                    val mensajeNotificacion = getString(
                        if (renovacion == "r") R.string.mensaje_notificacion_anuncio_renovacion else R.string.mensaje_notificacion_anuncio,
                        bindingBottomSheet.tipoServicio.text.toString(),
                        bindingBottomSheet.Total.text.toString()
                    )

                    val solicitud = hashMapOf(
                        Variables.nombre to bindingBottomSheet.nombreED.text.toString(),
                        Variables.apellido to bindingBottomSheet.apellidosED.text.toString(),
                        Variables.DNI to bindingBottomSheet.dniED.text.toString(),
                        Variables.telefono to bindingBottomSheet.telefonoed.text.toString(),
                        Variables.total to bindingBottomSheet.Total.text.toString(),
                        Variables.fecha to bindingBottomSheet.fecha.text.toString(),
                        Variables.hora to bindingBottomSheet.hora.text.toString(),
                        Variables.tipo_servicio to bindingBottomSheet.tipoServicio.text.toString(),
                        Variables.id_usuario to firebaseAuth.uid.toString(),
                        Variables.token to tokenUser
                    )

                    referencia.add(solicitud).addOnSuccessListener { documentReference ->
                        val documentId = documentReference.id
                        referencia.document(documentId).update(Variables.idSolicitud, documentId)
                            .addOnSuccessListener {
                                enviarNoticacion("idAdmin", "ADMINISTRADOR_BANER", tituloNotificacion, mensajeNotificacion)
                                Toast.makeText(this, "Solicitud enviada correctamente", Toast.LENGTH_SHORT).show()
                                dialog.dismiss()
                            }.addOnFailureListener { e ->
                                manejarError(e, "Error al actualizar la solicitud.")
                            }
                    }.addOnFailureListener { e ->
                        manejarError(e, "Error al enviar el formulario.")
                    }
                }
            }.addOnFailureListener { e ->
                Log.e("FirestoreError", "Error al verificar documento: ${e.message}")
            }

        }
        dialog.setContentView(bindingBottomSheet.root)
        dialog.show()
    }

    private fun manejarError(e: Exception, mensaje: String) {
        Log.e("FormularioError", mensaje, e)
    }

    fun enviarNoticacion(enviado1: String, vista: String, Titulo: String, texto: String) {
        GlobalScope.launch {
            val notificacionRS = NotificacionRS()
            obtenertokenIdAdmin.obtenertokenAdmin { token, id ->
                GlobalScope.launch {
                    notificacionRS.sendNotification_con_parametros(
                        enviado1,
                        id,
                        vista,
                        this@planes,
                        token,
                        Titulo,
                        texto
                    )
                }
            }
        }
    }

}