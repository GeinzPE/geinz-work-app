package com.geinzz.geinzwork.problemas_soporte_politicas

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import com.example.geinzwork.NotificacionRS
import com.example.geinzwork.constantesGeneral.Variables
import com.example.geinzwork.constantesGeneral.obtenertokenIdAdmin
import com.geinzz.geinzwork.R
import com.geinzz.geinzwork.constantesGeneral.constantes
import com.geinzz.geinzwork.constantesGeneral.constantesCarrito
import com.geinzz.geinzwork.constantesGeneral.constantestextos_general
import com.geinzz.geinzwork.databinding.ActivityVerificacionCuentaTrabajadorBinding
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class verificacion_cuenta_trabajador : AppCompatActivity() {
    private var ImagenPerfil: Uri? = null
    private var DNIFRONTAL: Uri? = null
    private var DNIPOSTERIOR: Uri? = null
    private var YAPEO: Uri? = null
    private lateinit var firebaseAuth: FirebaseAuth
    private val pciMEdia =
        registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
            if (uri != null) {
                ImagenPerfil = uri
                binding.imagenPerfil.setImageURI(uri)
            } else {
                println(getString(R.string.ImgNoSeleccionada))
            }
        }
    private val yapePick =
        registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
            if (uri != null) {
                YAPEO = uri
                binding.comprovantePago.setImageURI(uri)
            } else {
                println(getString(R.string.ImgNoSeleccionada))
            }
        }
    private val pciMEdia2 =
        registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
            if (uri != null) {
                DNIFRONTAL = uri
                binding.imagenFrontal.setImageURI(uri)
            } else {
                println(getString(R.string.ImgNoSeleccionada))
            }
        }
    private val pciMEdia3 =
        registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
            if (uri != null) {
                DNIPOSTERIOR = uri
                binding.imagenAtras.setImageURI(uri)
            } else {
                println(getString(R.string.ImgNoSeleccionada))
            }
        }

    private lateinit var binding: ActivityVerificacionCuentaTrabajadorBinding
    private val storage =
        FirebaseStorage.getInstance().reference.child(Variables.VerificadosDBChild)
    private val db = FirebaseFirestore.getInstance().collection(Variables.solicitudes_serviciosDB)
        .document(Variables.verificacionesDB).collection(Variables.pendientesDB)

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityVerificacionCuentaTrabajadorBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        firebaseAuth = FirebaseAuth.getInstance()
        constantesCarrito.obtnerfechaHora(binding.hora, binding.fecha)

        binding.qrYape.setOnClickListener {
            val dialogFragment = ImageDialogFragment.newInstance(R.drawable.qr_yape)
            dialogFragment.show(supportFragmentManager, "image_dialog")
        }

        verificarEstadosCuenta()
        constantestextos_general.subrallarTexto(
            "Politicas de privacidad",
            binding.textoPoliticas
        )
        binding.textoPoliticas.setOnClickListener {
            startActivity(Intent(this, politicas_verificacion::class.java))
        }

        binding.infoDescripcionServicios.setOnClickListener {
            mostrarDialogo(
                getString(R.string.DescripcióndeServiciosTitle),
                getString(R.string.DescripcióndeServiciosTextTXT),
                this
            )
        }

        binding.infoCertificados.setOnClickListener {
            mostrarDialogo(
                getString(R.string.CertificadosoReferenciasTitle),
                getString(R.string.CertificadosoReferenciasTXT),
                this
            )
        }
        constantesCarrito.setearDatosUsuario { nombre, numero, localidad, apellido ->
            binding.nombreED.setText(nombre)
            binding.numeroTelfED.setText(numero)
            binding.apellidoED.setText(apellido)
        }

        configurarClickListenersParaImagenes()

        llenarAutocompletPlaner()
        binding.verificar.setOnClickListener {

            val nombreEd = binding.nombreED
            val apellidoEd = binding.apellidoED
            val descripcionServiciosEd = binding.descripcionServiciosED
            val dniEd = binding.dniED
            val numeroTelfEd = binding.numeroTelfED
            val certificadosEd = binding.planSeleccionado

            if (nombreEd.text.isBlank() || apellidoEd.text.isBlank() ||
                descripcionServiciosEd.text.isBlank() || dniEd.text.isBlank() ||
                numeroTelfEd.text.isBlank()
            ) {
                Toast.makeText(
                    this,
                    "Por favor, completa todos los campos.",
                    Toast.LENGTH_SHORT
                ).show()
            } else if (ImagenPerfil == null || DNIFRONTAL == null || DNIPOSTERIOR == null) {
                Toast.makeText(this, "Por favor, suba Imágenes", Toast.LENGTH_SHORT)
                    .show()
            } else if (!binding.checkBoxPoliticas.isChecked) {
                Toast.makeText(
                    this,
                    "Acepta nuestras políticas de privacidad",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                binding.progressBarContainer.visibility = View.VISIBLE
                binding.scroll.isVisible = false
                val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
                val localDate =
                    LocalDate.parse(binding.fecha.text.toString(), formatter)

                val fechaUnMesDespues = localDate.plusMonths(1)
                constantes.obtenerToken_trabajador(firebaseAuth.uid.toString()) { tokenTrabajador, nombre, apellido ->
                    obtenertokenIdAdmin.obtenertokenAdmin { token, id ->
                        enviarNotificacion(token, firebaseAuth.uid.toString())
                    }

                    val documentId = firebaseAuth.uid.toString()
                    val hasmap = hashMapOf<String, Any>(
                        Variables.hora to "${binding.hora.text}",
                        Variables.fecha to "${binding.fecha.text}",
                        Variables.verificado to false,
                        Variables.idTrabajador to firebaseAuth.uid.toString(),
                        Variables.nombreT to nombreEd.text.toString(),
                        Variables.apellidoT to apellidoEd.text.toString(),
                        Variables.descripcion_servicios to descripcionServiciosEd.text.toString(),
                        Variables.DNI to dniEd.text.toString(),
                        Variables.numeroT to numeroTelfEd.text.toString(),
                        Variables.Plan to certificadosEd.text.toString(),
                        Variables.fecha_vencimiento to fechaUnMesDespues.format(formatter),
                        Variables.token to tokenTrabajador
                    )


                    val dbRef = FirebaseFirestore.getInstance()
                        .collection(Variables.solicitudes_serviciosDB)
                        .document(Variables.verificacionesDB)
                        .collection(Variables.pendientesDB)
                        .document(documentId)

                    // Reemplazamos `add()` por `set()` para evitar crear documentos duplicados
                    dbRef.set(hasmap)
                        .addOnSuccessListener {
                            uploadImages(documentId)

                            // Actualizar el documento para añadir el campo id_Comprovante
                            val hasmaoid = hashMapOf<String, Any>(
                                Variables.id_Comprovante to documentId
                            )

                            val db2 = FirebaseFirestore.getInstance()
                                .collection(Variables.solicitudes_serviciosDB)
                                .document(Variables.verificacionesDB)
                                .collection(Variables.pendientesDB)
                                .document(documentId)

                            db2.set(hasmaoid, SetOptions.merge())
                                .addOnSuccessListener {

                                }
                        }
                        .addOnFailureListener { e ->
                            Toast.makeText(
                                this,
                                "Error al enviar la solicitud: ${e.message}",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                }
            }
        }


    }

    /* cambiar la ruta de abrir el enviado del json */
    private fun enviarNotificacion(token: String, id: String) {
        val notificar = NotificacionRS()
        GlobalScope.launch {
            notificar.sendNotification_con_parametros(
                "idAdmin",
                id,
                "hola",
                this@verificacion_cuenta_trabajador,
                token,
                getString(R.string.titulo_notificacion_verificacion),
                getString(R.string.mensaje_notificacion_verificacion)
            )
        }

    }


    fun verificarEstadosCuenta() {
        val userId = firebaseAuth.uid.toString()

        binding.scroll.isVisible = false
        binding.enviadoExitosamente.isVisible = false

        val pendientesRef =
            FirebaseFirestore.getInstance().collection(Variables.solicitudes_serviciosDB)
                .document(Variables.verificacionesDB).collection(Variables.pendientesDB)
                .document(userId)
        val activosRef =
            FirebaseFirestore.getInstance().collection(Variables.solicitudes_serviciosDB)
                .document(Variables.verificacionesDB).collection(Variables.activos).document(userId)

        val pendienteTask = pendientesRef.get()
        val activoTask = activosRef.get()

        Tasks.whenAllComplete(pendienteTask, activoTask).addOnSuccessListener {
            val pendienteResult = pendienteTask.result
            val activoResult = activoTask.result

            when {
                pendienteResult?.exists() == true -> {
                    binding.enviadoExitosamente.isVisible = true
                }

                activoResult?.exists() == true -> {
                    binding.enviadoExitosamente.isVisible = true
                    binding.lotteSend.setAnimation(R.raw.verificado_animation)
                    binding.lotteSend.playAnimation()
                    binding.texto.text = getString(R.string.su_cuenta_fue_verificada)
                    binding.scroll.isVisible = false
                }

                else -> {
                    binding.scroll.isVisible = true
                    binding.enviadoExitosamente.isVisible = false
                }
            }
        }.addOnFailureListener {

            binding.scroll.isVisible = true
            binding.enviadoExitosamente.isVisible = false
        }
    }

    private fun obtener_categorias_verificados(selecionado: String) {
        val db = FirebaseFirestore.getInstance().collection("solicitudes_servicios")
            .document("verificaciones").collection("beneficios_planes_verificados").document(selecionado)
        db.get().addOnSuccessListener { res ->
            if(res.exists()){
                val data = res.data
                val caracteristicas = data?.get("caracteristicas") as? List<String>
                if (caracteristicas != null) {
                    println("Características obtenidas:")
                    caracteristicas.forEachIndexed { index, caracteristica ->
                        println("$index: $caracteristica")
                    }
                } else {
                    println("El campo 'caracteristicas' no existe o no es una matriz de strings.")
                }
            }
            }



    }

    fun llenarAutocompletPlaner() {
        var lista = listOf(
            "Plan A (Verificacion)",
            "Plan B (Verificacion + 10 publicaciones)",
            "Plan C (Verificacion + 20 publicaciones)"
        )
        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, lista)
        binding.planes.setAdapter(adapter)
        binding.planes.setOnItemClickListener { parent, view, position, id ->
            val opcionSeleccionada = parent.getItemAtPosition(position) as String
            when (opcionSeleccionada) {
                "Plan A (Verificacion)" -> {
                    binding.montoCancelar.text = "S/10.00"
                    binding.PlanB.isVisible = false
                    binding.PlanC.isVisible = false
                    binding.planSeleccionado.text = "A"
                    binding.mostrarRedes.isVisible = false
                    obtener_categorias_verificados("plan_a")
                }

                "Plan B (Verificacion + 10 publicaciones)" -> {
                    binding.PlanB.isVisible = true
                    binding.PlanC.isVisible = false
                    binding.montoCancelar.text = "S/15.00"
                    binding.planSeleccionado.text = "B"
                    binding.mostrarRedes.isVisible = true
                    obtener_categorias_verificados("plan_b")
                }

                "Plan C (Verificacion + 20 publicaciones)" -> {
                    binding.PlanC.isVisible = true
                    binding.mostrarRedes.isVisible = true
                    binding.PlanB.isVisible = false
                    binding.montoCancelar.text = "S/20.00"
                    binding.planSeleccionado.text = "C"
                    obtener_categorias_verificados("plan_c")
                }

            }
        }
    }

    fun mostrarDialogo(titulo: String, mensaje: String, contexto: Context) {
        val builder = AlertDialog.Builder(contexto)
        builder.apply {
            setTitle(titulo)
            setMessage(mensaje)
            setPositiveButton("Entendido") { dialog, _ ->
                dialog.dismiss()
            }
        }
        val dialog = builder.create()
        dialog.show()
    }

    fun configurarClickListenersParaImagenes() {
        binding.imagenPerfil.setOnClickListener {
            pciMEdia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }
        binding.imagenFrontal.setOnClickListener {
            pciMEdia2.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }
        binding.imagenAtras.setOnClickListener {
            pciMEdia3.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }
        binding.comprovantePago.setOnClickListener {
            yapePick.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }
    }

    private fun uploadImages(documentId: String) {
        val imageUploadTasks = mutableListOf<Task<Uri>>()

        ImagenPerfil?.let { uri ->
            val ref = storage.child("$documentId/${Variables.ImagenPerfiljpgv}")
            val uploadTask = ref.putFile(uri)
                .continueWithTask { task ->
                    if (!task.isSuccessful) {
                        task.exception?.let { throw it }
                    }
                    ref.downloadUrl
                }
            imageUploadTasks.add(uploadTask)
        }

        DNIFRONTAL?.let { uri ->
            val ref = storage.child("$documentId/${Variables.DNIFRONTAL}")
            val uploadTask = ref.putFile(uri)
                .continueWithTask { task ->
                    if (!task.isSuccessful) {
                        task.exception?.let { throw it }
                    }
                    ref.downloadUrl
                }
            imageUploadTasks.add(uploadTask)
        }

        DNIPOSTERIOR?.let { uri ->
            val ref = storage.child("$documentId/${Variables.DNIPOSTERIOR}")
            val uploadTask = ref.putFile(uri)
                .continueWithTask { task ->
                    if (!task.isSuccessful) {
                        task.exception?.let { throw it }
                    }
                    ref.downloadUrl
                }
            imageUploadTasks.add(uploadTask)
        }

        YAPEO?.let { uri ->
            val ref = storage.child("$documentId/${Variables.YAPEO}")
            val uploadTask = ref.putFile(uri)
                .continueWithTask { task ->
                    if (!task.isSuccessful) {
                        task.exception?.let { throw it }
                    }
                    ref.downloadUrl
                }
            imageUploadTasks.add(uploadTask)
        }


        Tasks.whenAllSuccess<Uri>(imageUploadTasks)
            .addOnSuccessListener { uriList ->
                val imageUrlMap = mutableMapOf<String, String>()
                if (ImagenPerfil != null) imageUrlMap[Variables.ImagenPerfilUrl] =
                    uriList[0].toString()
                if (DNIFRONTAL != null) imageUrlMap[Variables.DNIFRONTALUrl] = uriList[1].toString()
                if (DNIPOSTERIOR != null) imageUrlMap[Variables.DNIPOSTERIORUrl] =
                    uriList[2].toString()
                if (yapePick != null) imageUrlMap[Variables.YAPEOUtrl] = uriList[3].toString()

                db.document(documentId).update(imageUrlMap as Map<String, Any>)
                    .addOnSuccessListener {
                        if (!isFinishing && !isDestroyed) {
                            binding.progressBarContainer.visibility = View.GONE
                            binding.scroll.isVisible = false

                            val dialogMessage =
                                getString(R.string.solicitud_enviada_correctamente)
                            val builder = AlertDialog.Builder(this)
                            builder.apply {
                                setTitle("Verificación Geinz")
                                setMessage(dialogMessage)
                                setPositiveButton("Entendido") { dialog, _ ->
                                    dialog.dismiss()
                                    onBackPressed()
                                }
                            }
                            val dialog = builder.create()
                            dialog.show()
                        }
                    }
                    .addOnFailureListener { e ->
                        if (!isFinishing && !isDestroyed) {

                            binding.progressBarContainer.visibility = View.GONE
                            binding.scroll.isVisible = true
                            Log.e(
                                "error_actualiazr",
                                "Error al actualizar el documento: ${e.message}"
                            )
                        }
                    }
            }
            .addOnFailureListener { e ->
                if (!isFinishing && !isDestroyed) {
                    binding.progressBarContainer.visibility = View.GONE
                    binding.scroll.isVisible = true
                    Log.e("error_actualiazr", "Error al subir las imágenes: ${e.message}")
                }
            }
    }


}