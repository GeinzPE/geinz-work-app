package com.geinzz.geinzwork.problemas_soporte_politicas

import android.content.Context
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.MultiAutoCompleteTextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.example.geinzwork.NotificacionRS
import com.example.geinzwork.constantesGeneral.Variables
import com.geinzz.geinzwork.R
import com.geinzz.geinzwork.constantesGeneral.constantes
import com.geinzz.geinzwork.constantesGeneral.constantesCarrito
import com.geinzz.geinzwork.databinding.ActivityProbleasUsuariosFormularioBinding
import com.google.auth.oauth2.GoogleCredentials
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.E

class probleas_usuarios_formulario : AppCompatActivity() {
    private lateinit var binding: ActivityProbleasUsuariosFormularioBinding
    private lateinit var firebaseAuth: FirebaseAuth

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProbleasUsuariosFormularioBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        firebaseAuth = FirebaseAuth.getInstance()


        var nombreUSer = intent.getStringExtra(Variables.nombreUSer)
        var categoria = intent.getStringExtra(Variables.categoria)
        var nacionalidad = intent.getStringExtra(Variables.nacionalidad)



        binding.nombreTrabajaror.text = nombreUSer
        binding.categoriaT.text = categoria
        binding.nacionalidadT.text = nacionalidad

        binding.enviar.setOnClickListener {
            mandarDatos()
        }

        constantesCarrito.setearDatosUsuario { nombre, numero, localidad, apellido ->
            binding.nombreEd.setText(nombre)
            binding.numeroTelfED.setText(numero)
            binding.apellidoED.setText(apellido)

        }
        llenarAutocompletTipoReporte(binding.problemas)
        GlobalScope.launch {
            println("el token es de fcm ${getAccessToken(this@probleas_usuarios_formulario)}")
        }

    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun llenarAutocompletTipoReporte(autoCompleteTextView: AutoCompleteTextView) {
        val reportTypes = listOf(
            "Mala Conducta",
            "Fraude",
            "Acoso",
            "Spam",
            "Contenido Ofensivo",
            "Falsificación de Información",
            "Infracción de Derechos",
            "Robo de Identidad",
            "Actividad Sospechosa",
            "Violación de Términos de Servicio",
            "Lenguaje Inapropiado",
            "Suplantación de Identidad",
            "Amenazas",
            "Conducta Discriminatoria",
            "Estafa",
            "Publicación de Información Privada",
            "Solicitud de Información Sensible",
            "Actividad Ilegal",
            "Incitación al Odio",
            "Violencia",
            "Abuso"
        )
        val adapter: ArrayAdapter<String> = ArrayAdapter(
            this,
            android.R.layout.simple_dropdown_item_1line,
            reportTypes
        )
        autoCompleteTextView.setAdapter(adapter)
        constantesCarrito.obtnerfechaHora(binding.hora, binding.fecha)
    }

    private fun mandarDatos() {
        val idTrabajador = intent.getStringExtra(Variables.idTrabajador).toString()
        val nombreTrabajador = binding.nombreTrabajaror.text.toString().trim()
        val nombreUsuario = binding.nombreEd.text.toString().trim()
        val apellidoUsuario = binding.apellidoED.text.toString().trim()
        val problema = binding.descripcionED.text.toString().trim()
        val numeroContacto = binding.numeroTelfED.text.toString().trim()
        val problemasAutocomplet = binding.problemas.text.toString().trim()

        if (nombreTrabajador.isEmpty() || nombreUsuario.isEmpty() || apellidoUsuario.isEmpty() ||
            problema.isEmpty() || numeroContacto.isEmpty() || problemasAutocomplet.isEmpty()
        ) {
            Toast.makeText(this, "Por favor completa todos los campos", Toast.LENGTH_SHORT).show()
            return
        } else {

            val builder = AlertDialog.Builder(this)
            builder.setTitle("Confirmación de Reporte")
            builder.setMessage("Al enviar este reporte, si se determina que es falso, tu cuenta será sancionada. ¿Deseas continuar?")

            builder.setPositiveButton("OK") { dialog, which ->
                val db = FirebaseFirestore.getInstance()
                    .collection(Variables.politicas_problemas_verificaciones)
                    .document(Variables.problemas).collection(Variables.problemas)

                val dbreporteEnviado =
                    FirebaseFirestore.getInstance().collection("Trabajadores_Usuarios_Drivers")
                        .document("trabajadores").collection("trabajadores").document(firebaseAuth.uid.toString())
                        .collection("reporte").document("enviados").collection("enviados")

                val dbreporteRecivido =
                    FirebaseFirestore.getInstance().collection("Trabajadores_Usuarios_Drivers")
                        .document("trabajadores").collection("trabajadores").document(idTrabajador)
                        .collection("reporte").document("recivido").collection("recivido")


                val hasmap = hashMapOf<String, Any>(
                    Variables.Trabajador to nombreTrabajador,
                    Variables.idTrabajador to idTrabajador,
                    Variables.Tipo_reporte to binding.problemas.text.toString(),
                    Variables.fecha_envio to binding.fecha.text.toString(),
                    Variables.hora_envio to binding.hora.text.toString(),
                    Variables.idUsuario to firebaseAuth.uid.toString(),
                    Variables.nombreUsuario to nombreUsuario,
                    Variables.apellidoUsuario to apellidoUsuario,
                    Variables.problema to problema,
                    Variables.numero_contacto to numeroContacto,
                    "estado" to "enviado"
                )

                dbreporteEnviado.add(hasmap).addOnSuccessListener { documentReference ->
                    val reporteId = documentReference.id
                    dbreporteEnviado.document(reporteId).update(Variables.idReporte, reporteId)
                        .addOnSuccessListener {
                            try {
                                val enviar_notificaciones = NotificacionRS()
                                constantes.obtenerToken_trabajador(idTrabajador) { token, nombre, apellido ->
                                    if (token.isNotEmpty()) {
                                        GlobalScope.launch {
                                            try {
                                                enviar_notificaciones.sendNotification_con_parametros(
                                                    "idUSer",
                                                    idTrabajador,
                                                    "REPORTE",
                                                    this@probleas_usuarios_formulario,
                                                    token,
                                                    "REPORTE DE TRABAJADOR GEINZ WORK",
                                                    "Hola $nombre $apellido tienes un reporte de un usuario apelalo antes que Geinz tome medidas con su cuenta"
                                                )
                                                println("Enviamos los valores $idTrabajador, $token")
                                            } catch (e: Exception) {
                                                println("Error sending notification: ${e.message}")
                                            }
                                        }
                                    } else {
                                        println("Token no disponible o vacío.")
                                    }
                                }
                            } catch (e: Exception) {
                                println("Error obtaining token: ${e.message}")
                            }


                            Toast.makeText(
                                this,
                                "En unos momentos nos pondremos en contacto contigo. Gracias por usar Geinz",
                                Toast.LENGTH_SHORT
                            ).show()
                            onBackPressed()


                        }
                        .addOnFailureListener { e ->
                            Toast.makeText(
                                this,
                                "Error al actualizar el ID del reporte: ${e.message}",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                }
                dbreporteRecivido.add(hasmap).addOnSuccessListener { documentReference ->
                    val reporteId = documentReference.id
                    dbreporteRecivido.document(reporteId).update(Variables.idReporte, reporteId)
                        .addOnSuccessListener {
                            try {
                                val enviar_notificaciones = NotificacionRS()
                                constantes.obtenerToken_trabajador(idTrabajador) { token, nombre, apellido ->
                                    if (token.isNotEmpty()) {
                                        GlobalScope.launch {
                                            try {
                                                enviar_notificaciones.sendNotification_con_parametros(
                                                    "idUSer",
                                                    idTrabajador,
                                                    "REPORTE",
                                                    this@probleas_usuarios_formulario,
                                                    token,
                                                    "REPORTE DE TRABAJADOR GEINZ WORK",
                                                    "Hola $nombre $apellido tienes un reporte de un usuario apelalo antes que Geinz tome medidas con su cuenta"
                                                )
                                                println("Enviamos los valores $idTrabajador, $token")
                                            } catch (e: Exception) {
                                                println("Error sending notification: ${e.message}")
                                            }
                                        }
                                    } else {
                                        println("Token no disponible o vacío.")
                                    }
                                }
                            } catch (e: Exception) {
                                println("Error obtaining token: ${e.message}")
                            }


                            Toast.makeText(
                                this,
                                "En unos momentos nos pondremos en contacto contigo. Gracias por usar Geinz",
                                Toast.LENGTH_SHORT
                            ).show()
                            onBackPressed()


                        }
                        .addOnFailureListener { e ->
                            Toast.makeText(
                                this,
                                "Error al actualizar el ID del reporte: ${e.message}",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                }


            }

            builder.setNegativeButton("Cancelar") { dialog, which ->
                dialog.dismiss()
            }

            builder.setCancelable(true)
            val dialog: AlertDialog = builder.create()
            dialog.show()

        }


    }

    suspend fun getAccessToken(context: Context): String? {
        return withContext(Dispatchers.IO) {
            try {
                val inputStream = context.assets.open("service-account-file.json")
                val googleCredentials = GoogleCredentials.fromStream(inputStream)
                    .createScoped(listOf("https://www.googleapis.com/auth/cloud-platform"))
                googleCredentials.refreshIfExpired()
                googleCredentials.accessToken.tokenValue
            } catch (e: Exception) {
                Log.e("error_token", "erro al obtener el token ${e.printStackTrace()}")
                null
            }
        }
    }

}