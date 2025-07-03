package com.geinzz.geinzwork.problemas_soporte_politicas

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
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
import com.geinzz.geinzwork.constantesGeneral.constantesDatosUsuarioTienda
import com.geinzz.geinzwork.constantesGeneral.constantes_cuenta_user
import com.geinzz.geinzwork.constantesGeneral.constantestextos_general
import com.geinzz.geinzwork.databinding.ActivityVerificacionCuentaTrabajadorBinding
import com.geinzz.geinzwork.databinding.LayoutBeneficiosVrBinding
import com.geinzz.geinzwork.vistaTiendas.constantesVistaTiendas
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.android.material.imageview.ShapeableImageView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class verificacion_cuenta_trabajador : AppCompatActivity() {
    // Jurídico
    private lateinit var launcherTitulosJuridico: ActivityResultLauncher<String>
    private lateinit var launcherFotoCarnetJuridico: ActivityResultLauncher<String>

    // Educación
    private lateinit var launcherTitulosEducacion: ActivityResultLauncher<String>
    private lateinit var launcherCertificadoEducacion: ActivityResultLauncher<String>

    // Desarrollo Programación
    private lateinit var launcherCertificadoDesarrollo: ActivityResultLauncher<String>

    // Chofer
    private lateinit var launcherLicenciaChofer: ActivityResultLauncher<String>
    private lateinit var launcherFotoVehiculoChofer: ActivityResultLauncher<String>
    private lateinit var launcherSeguroVehiculoChofer: ActivityResultLauncher<String>

    // Constructor
    private lateinit var launcherImg1Constructor: ActivityResultLauncher<String>
    private lateinit var launcherImg2Constructor: ActivityResultLauncher<String>
    private lateinit var launcherImg3Constructor: ActivityResultLauncher<String>
    private lateinit var launcherImg4Constructor: ActivityResultLauncher<String>
    private lateinit var launcherCertificadoConstructor: ActivityResultLauncher<String>

    // Arte y Antigüedades
    private lateinit var launcherImg1Arte: ActivityResultLauncher<String>
    private lateinit var launcherImg2Arte: ActivityResultLauncher<String>
    private lateinit var launcherImg3Arte: ActivityResultLauncher<String>
    private lateinit var launcherImg4Arte: ActivityResultLauncher<String>

    // Mecánicos
    private lateinit var launcherImg1Mecanico: ActivityResultLauncher<String>
    private lateinit var launcherImg2Mecanico: ActivityResultLauncher<String>
    private lateinit var launcherImg3Mecanico: ActivityResultLauncher<String>
    private lateinit var launcherImg4Mecanico: ActivityResultLauncher<String>
    private lateinit var launcherCertificadoMecanico: ActivityResultLauncher<String>

    // Salud
    private lateinit var launcherCertificadoMedico: ActivityResultLauncher<String>
    private lateinit var launcherCertificadoTecnicoSalud: ActivityResultLauncher<String>
    private lateinit var launcherCarnetMedico: ActivityResultLauncher<String>

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
        constantesDatosUsuarioTienda.obtnerLocalidades(binding.localidadUser)
        constantesCarrito.setearDatosUsuario { nombre, numero, localidad, apellido ->
            if (nombre == null || numero == null || localidad == null || apellido == null) {
                Log.e("user", "Error: Algunos datos son null")
            } else {
                binding.localidadUser.setText(localidad)
                constantesDatosUsuarioTienda.obtnerLocalidades(binding.localidadUser)
            }
        }

// Jurídico
        launcherTitulosJuridico = crearLauncherPara(binding.lyLegal.titulosEducativos)
        launcherFotoCarnetJuridico = crearLauncherPara(binding.lyLegal.fotoCarnet)

// Educación
        launcherTitulosEducacion = crearLauncherPara(binding.lyEducacion.titulosEducativos)
        launcherCertificadoEducacion = crearLauncherPara(binding.lyEducacion.certificaciones)

// Desarrollo Programación
        launcherCertificadoDesarrollo = crearLauncherPara(binding.lyDesarrollo.certificadosTecnicos)

// Chofer
        launcherLicenciaChofer = crearLauncherPara(binding.lyChofer.licenciaConducir)
        launcherFotoVehiculoChofer = crearLauncherPara(binding.lyChofer.fotoVeiculo)
        launcherSeguroVehiculoChofer = crearLauncherPara(binding.lyChofer.seguroVeicular)

// Constructor
        launcherImg1Constructor = crearLauncherPara(binding.lyConstrucion.img1)
        launcherImg2Constructor = crearLauncherPara(binding.lyConstrucion.img2)
        launcherImg3Constructor = crearLauncherPara(binding.lyConstrucion.img3)
        launcherImg4Constructor = crearLauncherPara(binding.lyConstrucion.img4)
        launcherCertificadoConstructor = crearLauncherPara(binding.lyConstrucion.certificadosTecnicos)

// Arte y Antigüedades
        launcherImg1Arte = crearLauncherPara(binding.lyArte.img1)
        launcherImg2Arte = crearLauncherPara(binding.lyArte.img2)
        launcherImg3Arte = crearLauncherPara(binding.lyArte.img3)
        launcherImg4Arte = crearLauncherPara(binding.lyArte.img4)

// Mecánicos
        launcherImg1Mecanico = crearLauncherPara(binding.lyMecanico.img1)
        launcherImg2Mecanico = crearLauncherPara(binding.lyMecanico.img2)
        launcherImg3Mecanico = crearLauncherPara(binding.lyMecanico.img3)
        launcherImg4Mecanico = crearLauncherPara(binding.lyMecanico.img4)
        launcherCertificadoMecanico = crearLauncherPara(binding.lyMecanico.certificadosTecnicos)

// Servicios de Salud
        launcherCertificadoMedico = crearLauncherPara(binding.lyServicioSalud.ceritificadoMedico)
        launcherCertificadoTecnicoSalud = crearLauncherPara(binding.lyServicioSalud.certificadosTecnicos)
        launcherCarnetMedico = crearLauncherPara(binding.lyServicioSalud.carnetMedico)

//        binding.qrYape.setOnClickListener {
//            val dialogFragment = ImageDialogFragment.newInstance(R.drawable.qr_yape)
//            dialogFragment.show(supportFragmentManager, "image_dialog")
//        }

        obtenerCategorias(binding.categoriaTrabajos)
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
        setupListenersLegal()
        llenarAutocompletPlaner()
        binding.verificar.setOnClickListener {
            if (tipo_selecionados(binding.categoriaTrabajos.text.toString())) {
                Toast.makeText(this, "mandamos verificaicon", Toast.LENGTH_SHORT).show()

            } else {
                Toast.makeText(
                    this,
                    "Por favor completa todos los campos obligatorios",
                    Toast.LENGTH_SHORT
                ).show()
            }

//            val nombreEd = binding.nombreED
//            val apellidoEd = binding.apellidoED
//            val descripcionServiciosEd = binding.descripcionServiciosED
//            val dniEd = binding.dniED
//            val numeroTelfEd = binding.numeroTelfED
//
//
//            if (nombreEd.text.isBlank() || apellidoEd.text.isBlank() ||
//                descripcionServiciosEd.text.isBlank() || dniEd.text.isBlank() ||
//                numeroTelfEd.text.isBlank()
//            ) {
//                Toast.makeText(
//                    this,
//                    "Por favor, completa todos los campos.",
//                    Toast.LENGTH_SHORT
//                ).show()
//            } else if (ImagenPerfil == null || DNIFRONTAL == null || DNIPOSTERIOR == null) {
//                Toast.makeText(this, "Por favor, suba Imágenes", Toast.LENGTH_SHORT)
//                    .show()
//            } else if (!binding.checkBoxPoliticas.isChecked) {
//                Toast.makeText(
//                    this,
//                    "Acepta nuestras políticas de privacidad",
//                    Toast.LENGTH_SHORT
//                ).show()
//            } else {
//                binding.progressBarContainer.visibility = View.VISIBLE
//                binding.scroll.isVisible = false
//                val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
//                val localDate =
//                    LocalDate.parse(binding.fecha.text.toString(), formatter)
//
//                val fechaUnMesDespues = localDate.plusMonths(1)
//                constantes.obtenerToken_trabajador(firebaseAuth.uid.toString()) { tokenTrabajador, nombre, apellido ->
//                    obtenertokenIdAdmin.obtenertokenAdmin { token, id ->
//                        enviarNotificacion(token, firebaseAuth.uid.toString())
//                    }
//
//                    val documentId = firebaseAuth.uid.toString()
//                    val hasmap = hashMapOf<String, Any>(
//                        Variables.hora to "${binding.hora.text}",
//                        Variables.fecha to "${binding.fecha.text}",
//                        Variables.verificado to false,
//                        Variables.idTrabajador to firebaseAuth.uid.toString(),
//                        Variables.nombreT to nombreEd.text.toString(),
//                        Variables.apellidoT to apellidoEd.text.toString(),
//                        Variables.descripcion_servicios to descripcionServiciosEd.text.toString(),
//                        Variables.DNI to dniEd.text.toString(),
//                        Variables.numeroT to numeroTelfEd.text.toString(),
////                        Variables.Plan to certificadosEd.text.toString(),
//                        Variables.fecha_vencimiento to fechaUnMesDespues.format(formatter),
//                        Variables.token to tokenTrabajador
//                    )
//
//
//                    val dbRef = FirebaseFirestore.getInstance()
//                        .collection(Variables.solicitudes_serviciosDB)
//                        .document(Variables.verificacionesDB)
//                        .collection(Variables.pendientesDB)
//                        .document(documentId)
//
//                    // Reemplazamos `add()` por `set()` para evitar crear documentos duplicados
//                    dbRef.set(hasmap)
//                        .addOnSuccessListener {
//                            uploadImages(documentId)
//
//                            // Actualizar el documento para añadir el campo id_Comprovante
//                            val hasmaoid = hashMapOf<String, Any>(
//                                Variables.id_Comprovante to documentId
//                            )
//
//                            val db2 = FirebaseFirestore.getInstance()
//                                .collection(Variables.solicitudes_serviciosDB)
//                                .document(Variables.verificacionesDB)
//                                .collection(Variables.pendientesDB)
//                                .document(documentId)
//
//                            db2.set(hasmaoid, SetOptions.merge())
//                                .addOnSuccessListener {
//
//                                }
//                        }
//                        .addOnFailureListener { e ->
//                            Toast.makeText(
//                                this,
//                                "Error al enviar la solicitud: ${e.message}",
//                                Toast.LENGTH_SHORT
//                            ).show()
//                        }
//                }
//            }


        }


    }
    private fun crearLauncherPara(imageView: ShapeableImageView): ActivityResultLauncher<String> {
        return registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            uri?.let { imageView.setImageURI(it) }
        }
    }


    private fun tipo_selecionados(categoria: String): Boolean {
        return when (categoria) {
            "Artes Visuales y Creativas" -> campos_arte_antiguedades()
            "Chofer privado" -> campos_chofer_privado()
            "Conductor de reparto" -> true // Aún sin validación
            "Construcción y hogar" -> campos_constructor_hogares()
            "Desarrollo Personal y Bienestar" -> campos_desarrollo_personal()
            "Desarrollo Web y Programación" -> verificar_campos_desarrollo_programacion()
            "Diseño Gráfico y Multimedia" -> verificar_campos_desing_graphic()
            "Educación" -> verificar_campos_educacion()
            "Escritura Creativa y Periodismo" -> true // Aún sin validación
            "Legal y Jurídico" -> verificar_campos_legal_juridico()
            "Marketing Digital y Publicidad" -> campos_marketing()
            "Mecánicos" -> campos_mecanicos()
            "Redacción y Edición" -> campos_redaccion()
            "Servicios de Salud" -> campos_servicios_salud()
            "Tecnicos" -> campos_tecnicos()
            else -> true
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
            .document("verificaciones").collection("beneficios_planes_verificados")
            .document(selecionado)
        db.get().addOnSuccessListener { res ->
            if (res.exists()) {
                val data = res.data
                val caracteristicas = data?.get("caracteristicas") as? List<String>
                val monto_cancelar_diario = data?.get("monto_cancelar_diario") as? Number ?: 0
                val monto_cancelar_mensual = data?.get("monto_cancelar_mensual") as? Number ?: 0
                if (caracteristicas != null) {
                    binding.layoutBeneficios.linealAnuncioVerificado.isVisible = true
                    when (selecionado) {
                        "plan_a" -> {
                            binding.layoutBeneficios.planSelecionado.text =
                                "Plan selecionado(PLAN A)"
                        }

                        "plan_b" -> {
                            binding.layoutBeneficios.planSelecionado.text =
                                "Plan selecionado(PLAN B)"
                        }

                        "plan_c" -> {
                            binding.layoutBeneficios.planSelecionado.text =
                                "Plan selecionado(PLAN C)"
                        }

                        else -> {
                            binding.layoutBeneficios.planSelecionado.text = "Sin plan "
                        }
                    }
                    binding.layoutBeneficios.montoDiario.text =
                        "¡Impulsa tu crecimiento en Geinz! Verifica tu cuenta con un pago diario de S/$monto_cancelar_diario o mensual de S/$monto_cancelar_mensual y accede a funciones exclusivas que potencian tu perfil como trabajador."

                    caracteristicas?.let {
                        val textoFormateado = it.joinToString("\n") { caracteristica ->
                            "-$caracteristica"
                        }
                        binding.layoutBeneficios.caracteristicasVerificado.text = textoFormateado
                    }

                } else {
                    println("El campo 'caracteristicas' no existe o no es una matriz de strings.")
                }
            }
        }


    }

    fun llenarAutocompletPlaner() {
        var lista = listOf(
            "Plan A (Verificacion cuenta Geinz)",
            "Plan B (Verificacion cuenta Geinz)",
            "Plan C (Verificacion cuenta Geinz)"
        )
        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, lista)
        binding.planes.setAdapter(adapter)
        binding.planes.setOnItemClickListener { parent, view, position, id ->
            val opcionSeleccionada = parent.getItemAtPosition(position) as String
            when (opcionSeleccionada) {
                "Plan A (Verificacion cuenta Geinz)" -> {
                    binding.layoutBeneficios.linealAnuncioVerificado.isVisible = false
                    obtener_categorias_verificados("plan_a")

                }

                "Plan B (Verificacion cuenta Geinz)" -> {
                    binding.layoutBeneficios.linealAnuncioVerificado.isVisible = false
                    obtener_categorias_verificados("plan_b")
                }

                "Plan C (Verificacion cuenta Geinz)" -> {

                    binding.layoutBeneficios.linealAnuncioVerificado.isVisible = false
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
//        binding.comprovantePago.setOnClickListener {
//            yapePick.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
//        }
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
//                if (yapePick != null) imageUrlMap[Variables.YAPEOUtrl] = uriList[3].toString()

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

    fun obtenerCategorias(autoCompleteTextView: AutoCompleteTextView) {
        val db = FirebaseFirestore.getInstance()
        val collection = db.collection(Variables.categoriasDB).document(Variables.categoriasTrabajo)

        collection.get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val categorias = document.get(Variables.categoriasDB) as? ArrayList<String>
                    if (categorias != null) {
                        val adapter = ArrayAdapter<String>(
                            autoCompleteTextView.context,
                            android.R.layout.simple_dropdown_item_1line,
                            categorias
                        )
                        autoCompleteTextView.setAdapter(adapter)
                        autoCompleteTextView.setOnItemClickListener { parent, view, position, id ->
                            val seleccionado = parent.getItemAtPosition(position).toString()
                            when (seleccionado) {
                                "Construcción y hogar" -> {
                                    binding.lyLegal.principal.isVisible = false
                                    binding.lyEducacion.principal.isVisible = false
                                    binding.lyDesing.principal.isVisible = false
                                    binding.lyDesarrollo.principal.isVisible = false
                                    binding.lyChofer.principal.isVisible = false
                                    binding.lyConstrucion.principal.isVisible = true
                                    binding.lyDesarrolloPersonal.principal.isVisible = false
                                    binding.lyArte.principal.isVisible = false
                                    binding.lyTecnicos.principal.isVisible = false
                                    binding.lyServicioSalud.principal.isVisible = false
                                    binding.lyRedacion.principal.isVisible = false
                                    binding.lyMecanico.principal.isVisible = false
                                    binding.lyMarketing.principal.isVisible = false
                                }

                                "Servicios de Salud" -> {
                                    binding.lyLegal.principal.isVisible = false
                                    binding.lyEducacion.principal.isVisible = false
                                    binding.lyDesing.principal.isVisible = false
                                    binding.lyDesarrollo.principal.isVisible = false
                                    binding.lyChofer.principal.isVisible = false
                                    binding.lyConstrucion.principal.isVisible = false
                                    binding.lyDesarrolloPersonal.principal.isVisible = false
                                    binding.lyArte.principal.isVisible = false
                                    binding.lyTecnicos.principal.isVisible = false
                                    binding.lyServicioSalud.principal.isVisible = true
                                    binding.lyRedacion.principal.isVisible = false
                                    binding.lyMecanico.principal.isVisible = false
                                    binding.lyMarketing.principal.isVisible = false
                                }

                                "Educación" -> {
                                    binding.lyLegal.principal.isVisible = false
                                    binding.lyEducacion.principal.isVisible = true
                                    binding.lyDesing.principal.isVisible = false
                                    binding.lyDesarrollo.principal.isVisible = false
                                    binding.lyChofer.principal.isVisible = false
                                    binding.lyConstrucion.principal.isVisible = false
                                    binding.lyDesarrolloPersonal.principal.isVisible = false
                                    binding.lyArte.principal.isVisible = false
                                    binding.lyTecnicos.principal.isVisible = false
                                    binding.lyServicioSalud.principal.isVisible = false
                                    binding.lyRedacion.principal.isVisible = false
                                    binding.lyMecanico.principal.isVisible = false
                                    binding.lyMarketing.principal.isVisible = false
                                }

                                "Legal y Jurídico" -> {
                                    binding.lyLegal.principal.isVisible = true
                                    binding.lyEducacion.principal.isVisible = false
                                    binding.lyDesing.principal.isVisible = false
                                    binding.lyDesarrollo.principal.isVisible = false
                                    binding.lyChofer.principal.isVisible = false
                                    binding.lyConstrucion.principal.isVisible = false
                                    binding.lyDesarrolloPersonal.principal.isVisible = false
                                    binding.lyArte.principal.isVisible = false
                                    binding.lyTecnicos.principal.isVisible = false
                                    binding.lyServicioSalud.principal.isVisible = false
                                    binding.lyRedacion.principal.isVisible = false
                                    binding.lyMecanico.principal.isVisible = false
                                    binding.lyMarketing.principal.isVisible = false
                                }

                                "Redacción y Edición" -> {
                                    binding.lyLegal.principal.isVisible = false
                                    binding.lyEducacion.principal.isVisible = false
                                    binding.lyDesing.principal.isVisible = false
                                    binding.lyDesarrollo.principal.isVisible = false
                                    binding.lyChofer.principal.isVisible = false
                                    binding.lyConstrucion.principal.isVisible = false
                                    binding.lyDesarrolloPersonal.principal.isVisible = false
                                    binding.lyArte.principal.isVisible = false
                                    binding.lyTecnicos.principal.isVisible = false
                                    binding.lyServicioSalud.principal.isVisible = false
                                    binding.lyRedacion.principal.isVisible = true
                                    binding.lyMecanico.principal.isVisible = false
                                    binding.lyMarketing.principal.isVisible = false

                                }

                                "Diseño Gráfico y Multimedia" -> {
                                    binding.lyLegal.principal.isVisible = false
                                    binding.lyEducacion.principal.isVisible = false
                                    binding.lyDesing.principal.isVisible = true
                                    binding.lyDesarrollo.principal.isVisible = false
                                    binding.lyChofer.principal.isVisible = false
                                    binding.lyConstrucion.principal.isVisible = false
                                    binding.lyDesarrolloPersonal.principal.isVisible = false
                                    binding.lyArte.principal.isVisible = false
                                    binding.lyTecnicos.principal.isVisible = false
                                    binding.lyServicioSalud.principal.isVisible = false
                                    binding.lyRedacion.principal.isVisible = false
                                    binding.lyMecanico.principal.isVisible = false
                                    binding.lyMarketing.principal.isVisible = false
                                }

                                "Desarrollo Web y Programación" -> {
                                    binding.lyLegal.principal.isVisible = false
                                    binding.lyEducacion.principal.isVisible = false
                                    binding.lyDesing.principal.isVisible = false
                                    binding.lyDesarrollo.principal.isVisible = true
                                    binding.lyChofer.principal.isVisible = false
                                    binding.lyConstrucion.principal.isVisible = false
                                    binding.lyDesarrolloPersonal.principal.isVisible = false
                                    binding.lyArte.principal.isVisible = false
                                    binding.lyTecnicos.principal.isVisible = false
                                    binding.lyServicioSalud.principal.isVisible = false
                                    binding.lyRedacion.principal.isVisible = false
                                    binding.lyMecanico.principal.isVisible = false
                                    binding.lyMarketing.principal.isVisible = false

                                }

                                "Marketing Digital y Publicidad" -> {
                                    binding.lyLegal.principal.isVisible = false
                                    binding.lyEducacion.principal.isVisible = false
                                    binding.lyDesing.principal.isVisible = false
                                    binding.lyDesarrollo.principal.isVisible = false
                                    binding.lyChofer.principal.isVisible = false
                                    binding.lyConstrucion.principal.isVisible = false
                                    binding.lyDesarrolloPersonal.principal.isVisible = false
                                    binding.lyArte.principal.isVisible = false
                                    binding.lyTecnicos.principal.isVisible = false
                                    binding.lyServicioSalud.principal.isVisible = false
                                    binding.lyRedacion.principal.isVisible = false
                                    binding.lyMecanico.principal.isVisible = false
                                    binding.lyMarketing.principal.isVisible = true
                                }

                                "Artes Visuales y Creativas" -> {
                                    binding.lyLegal.principal.isVisible = false
                                    binding.lyEducacion.principal.isVisible = false
                                    binding.lyDesing.principal.isVisible = false
                                    binding.lyDesarrollo.principal.isVisible = false
                                    binding.lyChofer.principal.isVisible = false
                                    binding.lyConstrucion.principal.isVisible = false
                                    binding.lyDesarrolloPersonal.principal.isVisible = false
                                    binding.lyArte.principal.isVisible = true
                                    binding.lyTecnicos.principal.isVisible = false
                                    binding.lyServicioSalud.principal.isVisible = false
                                    binding.lyRedacion.principal.isVisible = false
                                    binding.lyMecanico.principal.isVisible = false
                                    binding.lyMarketing.principal.isVisible = false
                                }

                                "Desarrollo Personal y Bienestar" -> {
                                    binding.lyLegal.principal.isVisible = false
                                    binding.lyEducacion.principal.isVisible = false
                                    binding.lyDesing.principal.isVisible = false
                                    binding.lyDesarrollo.principal.isVisible = false
                                    binding.lyChofer.principal.isVisible = false
                                    binding.lyConstrucion.principal.isVisible = false
                                    binding.lyDesarrolloPersonal.principal.isVisible = true
                                    binding.lyArte.principal.isVisible = false
                                    binding.lyTecnicos.principal.isVisible = false
                                    binding.lyServicioSalud.principal.isVisible = false
                                    binding.lyRedacion.principal.isVisible = false
                                    binding.lyMecanico.principal.isVisible = false
                                    binding.lyMarketing.principal.isVisible = false
                                }

                                "Escritura Creativa y Periodismo" -> {
                                    binding.lyLegal.principal.isVisible = false
                                    binding.lyEducacion.principal.isVisible = false
                                    binding.lyDesing.principal.isVisible = false
                                    binding.lyDesarrollo.principal.isVisible = false
                                    binding.lyChofer.principal.isVisible = false
                                    binding.lyConstrucion.principal.isVisible = false
                                    binding.lyDesarrolloPersonal.principal.isVisible = false
                                    binding.lyArte.principal.isVisible = false
                                    binding.lyTecnicos.principal.isVisible = false
                                    binding.lyServicioSalud.principal.isVisible = false
                                    binding.lyRedacion.principal.isVisible = true
                                    binding.lyMecanico.principal.isVisible = false
                                    binding.lyMarketing.principal.isVisible = false
                                }

                                "Conductor de reparto" -> {
                                    binding.lyLegal.principal.isVisible = false
                                    binding.lyEducacion.principal.isVisible = false
                                    binding.lyDesing.principal.isVisible = false
                                    binding.lyDesarrollo.principal.isVisible = false
                                    binding.lyChofer.principal.isVisible = true
                                    binding.lyConstrucion.principal.isVisible = false
                                    binding.lyDesarrolloPersonal.principal.isVisible = false
                                    binding.lyArte.principal.isVisible = false
                                    binding.lyTecnicos.principal.isVisible = false
                                    binding.lyServicioSalud.principal.isVisible = false
                                    binding.lyRedacion.principal.isVisible = false
                                    binding.lyMecanico.principal.isVisible = false
                                    binding.lyMarketing.principal.isVisible = false
                                }

                                "Chofer privado" -> {
                                    binding.lyLegal.principal.isVisible = false
                                    binding.lyEducacion.principal.isVisible = false
                                    binding.lyDesing.principal.isVisible = false
                                    binding.lyDesarrollo.principal.isVisible = false
                                    binding.lyChofer.principal.isVisible = true
                                    binding.lyConstrucion.principal.isVisible = false
                                    binding.lyDesarrolloPersonal.principal.isVisible = false
                                    binding.lyArte.principal.isVisible = false
                                    binding.lyTecnicos.principal.isVisible = false
                                    binding.lyServicioSalud.principal.isVisible = false
                                    binding.lyRedacion.principal.isVisible = false
                                    binding.lyMecanico.principal.isVisible = false
                                    binding.lyMarketing.principal.isVisible = false
                                }

                                "Mecánicos" -> {
                                    binding.lyLegal.principal.isVisible = false
                                    binding.lyEducacion.principal.isVisible = false
                                    binding.lyDesing.principal.isVisible = false
                                    binding.lyDesarrollo.principal.isVisible = false
                                    binding.lyChofer.principal.isVisible = false
                                    binding.lyConstrucion.principal.isVisible = false
                                    binding.lyDesarrolloPersonal.principal.isVisible = false
                                    binding.lyArte.principal.isVisible = false
                                    binding.lyTecnicos.principal.isVisible = false
                                    binding.lyServicioSalud.principal.isVisible = false
                                    binding.lyRedacion.principal.isVisible = false
                                    binding.lyMecanico.principal.isVisible = true
                                    binding.lyMarketing.principal.isVisible = false
                                }

                                "Tecnicos" -> {
                                    binding.lyLegal.principal.isVisible = false
                                    binding.lyEducacion.principal.isVisible = false
                                    binding.lyDesing.principal.isVisible = false
                                    binding.lyDesarrollo.principal.isVisible = false
                                    binding.lyChofer.principal.isVisible = false
                                    binding.lyConstrucion.principal.isVisible = false
                                    binding.lyDesarrolloPersonal.principal.isVisible = false
                                    binding.lyArte.principal.isVisible = false
                                    binding.lyTecnicos.principal.isVisible = true
                                    binding.lyServicioSalud.principal.isVisible = false
                                    binding.lyRedacion.principal.isVisible = false
                                    binding.lyMecanico.principal.isVisible = false
                                    binding.lyMarketing.principal.isVisible = false
                                }

                            }
                        }

                    }

                }
            }
    }


    private fun setupListenersLegal() {
        val juridico = binding.lyLegal
        val educacion = binding.lyEducacion
        val desarrollo = binding.lyDesarrollo
        val chofer = binding.lyChofer
        val constructor = binding.lyConstrucion
        val arte = binding.lyArte
        val mecanicos = binding.lyMecanico
        val salud = binding.lyServicioSalud

        // Legal
        juridico.titulosEducativos.setOnClickListener {
            launcherTitulosJuridico.launch("image/*")
        }
        juridico.fotoCarnet.setOnClickListener {
            launcherFotoCarnetJuridico.launch("image/*")
        }

        // Educación
        educacion.titulosEducativos.setOnClickListener {
            launcherTitulosEducacion.launch("image/*")
        }
        educacion.certificaciones.setOnClickListener {
            launcherCertificadoEducacion.launch("image/*")
        }

        // Desarrollo
        desarrollo.certificadosTecnicos.setOnClickListener {
            launcherCertificadoDesarrollo.launch("image/*")
        }

        // Chofer
        chofer.licenciaConducir.setOnClickListener {
            launcherLicenciaChofer.launch("image/*")
        }
        chofer.fotoVeiculo.setOnClickListener {
            launcherFotoVehiculoChofer.launch("image/*")
        }
        chofer.seguroVeicular.setOnClickListener {
            launcherSeguroVehiculoChofer.launch("image/*")
        }

        // Constructor
        constructor.img1.setOnClickListener {
            launcherImg1Constructor.launch("image/*")
        }
        constructor.img2.setOnClickListener {
            launcherImg2Constructor.launch("image/*")
        }
        constructor.img3.setOnClickListener {
            launcherImg3Constructor.launch("image/*")
        }
        constructor.img4.setOnClickListener {
            launcherImg4Constructor.launch("image/*")
        }
        constructor.certificadosTecnicos.setOnClickListener {
            launcherCertificadoConstructor.launch("image/*")
        }

        // Arte y antigüedades
        arte.img1.setOnClickListener {
            launcherImg1Arte.launch("image/*")
        }
        arte.img2.setOnClickListener {
            launcherImg2Arte.launch("image/*")
        }
        arte.img3.setOnClickListener {
            launcherImg3Arte.launch("image/*")
        }
        arte.img4.setOnClickListener {
            launcherImg4Arte.launch("image/*")
        }

        // Mecánicos
        mecanicos.img1.setOnClickListener {
            launcherImg1Mecanico.launch("image/*")
        }
        mecanicos.img2.setOnClickListener {
            launcherImg2Mecanico.launch("image/*")
        }
        mecanicos.img3.setOnClickListener {
            launcherImg3Mecanico.launch("image/*")
        }
        mecanicos.img4.setOnClickListener {
            launcherImg4Mecanico.launch("image/*")
        }
        mecanicos.certificadosTecnicos.setOnClickListener {
            launcherCertificadoMecanico.launch("image/*")
        }

        // Servicios de salud
        salud.ceritificadoMedico.setOnClickListener {
            launcherCertificadoMedico.launch("image/*")
        }
        salud.certificadosTecnicos.setOnClickListener {
            launcherCertificadoTecnicoSalud.launch("image/*")
        }
        salud.carnetMedico.setOnClickListener {
            launcherCarnetMedico.launch("image/*")
        }
    }



    private fun verificar_campos_legal_juridico(): Boolean {
        val juridico = binding.lyLegal
        val areas = juridico.especializacionED.text.toString().trim()
        val yearsExp = juridico.yearsExperienceED.text.toString().trim()
        val especializaciones = juridico.EspecializacionesED.text.toString().trim()

        var valido = true

        if (areas.isEmpty()) {
            juridico.especializacionED.error = "Campo obligatorio"
            juridico.especializacionED.requestFocus()
            valido = false
        }

        if (yearsExp.isEmpty()) {
            juridico.yearsExperienceED.error = "Campo obligatorio"
            juridico.yearsExperienceED.requestFocus()
            valido = false
        }

        if (especializaciones.isEmpty()) {
            juridico.EspecializacionesED.error = "Campo obligatorio"
            juridico.EspecializacionesED.requestFocus()
            valido = false
        }

        return valido
    }

    private fun verificar_campos_educacion(): Boolean {
        val educacion = binding.lyEducacion
        val textoEspecializado = educacion.especializacionED.text.toString().trim()
        val yearsExp = educacion.yearsExperienceED.text.toString().trim()

        var valido = true

        if (textoEspecializado.isEmpty()) {
            educacion.especializacionED.error = "Campo obligatorio"
            educacion.especializacionED.requestFocus()
            valido = false
        }

        if (yearsExp.isEmpty()) {
            educacion.yearsExperienceED.error = "Campo obligatorio"
            educacion.yearsExperienceED.requestFocus()
            valido = false
        }

        return valido
    }

    private fun verificar_campos_desing_graphic(): Boolean {
        val desing = binding.lyDesing

        val portafolio = desing.portafolioED.text.toString().trim()
        val software = desing.softwareED.text.toString().trim()
        val yearsExp = desing.yearsExperienceED.text.toString().trim()
        val especializacion = desing.especializacionED.text.toString().trim()

        var valido = true

        if (portafolio.isEmpty()) {
            desing.portafolioED.error = "Campo obligatorio"
            desing.portafolioED.requestFocus()
            valido = false
        }

        if (software.isEmpty()) {
            desing.softwareED.error = "Campo obligatorio"
            desing.softwareED.requestFocus()
            valido = false
        }

        if (yearsExp.isEmpty()) {
            desing.yearsExperienceED.error = "Campo obligatorio"
            desing.yearsExperienceED.requestFocus()
            valido = false
        }

        if (especializacion.isEmpty()) {
            desing.especializacionED.error = "Campo obligatorio"
            desing.especializacionED.requestFocus()
            valido = false
        }

        return valido
    }

    private fun verificar_campos_desarrollo_programacion(): Boolean {
        val desarrollo = binding.lyDesarrollo

        val repo = desarrollo.repoED.text.toString().trim()
        val portafolio = desarrollo.portafolioED.text.toString().trim()
        val yearsExp = desarrollo.yearsExperienceED.text.toString().trim()

        var valido = true

        if (repo.isEmpty()) {
            desarrollo.repoED.error = "Campo obligatorio"
            desarrollo.repoED.requestFocus()
            valido = false
        }

        if (portafolio.isEmpty()) {
            desarrollo.portafolioED.error = "Campo obligatorio"
            desarrollo.portafolioED.requestFocus()
            valido = false
        }

        if (yearsExp.isEmpty()) {
            desarrollo.yearsExperienceED.error = "Campo obligatorio"
            desarrollo.yearsExperienceED.requestFocus()
            valido = false
        }

        return valido
    }

    private fun campos_chofer_privado(): Boolean {
        val chofer = binding.lyChofer
        val puntosEntrega = chofer.descripcionServiciosED.text.toString().trim()

        var valido = true

        if (puntosEntrega.isEmpty()) {
            chofer.descripcionServiciosED.error = "Campo obligatorio"
            chofer.descripcionServiciosED.requestFocus()
            valido = false
        }

        return valido
    }

    private fun campos_constructor_hogares(): Boolean {
        val constructor = binding.lyConstrucion

        val img1 = constructor.img1
        val img2 = constructor.img2
        val img3 = constructor.img3
        val img4 = constructor.img4
        val certificados = constructor.certificadosTecnicos

        val especializacion = constructor.espezializacionED.text.toString().trim()
        val yearsExp = constructor.yearsExperienceED.text.toString().trim()

        var valido = true

        if (especializacion.isEmpty()) {
            constructor.espezializacionED.error = "Campo obligatorio"
            constructor.espezializacionED.requestFocus()
            valido = false
        }

        if (yearsExp.isEmpty()) {
            constructor.yearsExperienceED.error = "Campo obligatorio"
            constructor.yearsExperienceED.requestFocus()
            valido = false
        }

        return valido
    }

    private fun campos_desarrollo_personal(): Boolean {
        val desarrollo = binding.lyDesarrolloPersonal

        val certificados = desarrollo.certificadosTecnicos

        val especializacion = desarrollo.especialidadesED.text.toString().trim()
        val yearsExp = desarrollo.yearsExperienceED.text.toString().trim()

        var valido = true

        if (especializacion.isEmpty()) {
            desarrollo.especialidadesED.error = "Campo obligatorio"
            desarrollo.especialidadesED.requestFocus()
            valido = false
        }

        if (yearsExp.isEmpty()) {
            desarrollo.yearsExperienceED.error = "Campo obligatorio"
            desarrollo.yearsExperienceED.requestFocus()
            valido = false
        }

        return valido
    }

    private fun campos_arte_antiguedades(): Boolean {
        val arte = binding.lyArte

        val img1 = arte.img1
        val img2 = arte.img2
        val img3 = arte.img3
        val img4 = arte.img4

        val enlaceRedes = arte.enlaceRedesSocialesED.text.toString().trim()
        val portafolio = arte.enlacePortafolioED.text.toString().trim()
        val yearsExp = arte.yearsExperienceED.text.toString().trim()

        var valido = true

        if (enlaceRedes.isEmpty()) {
            arte.enlaceRedesSocialesED.error = "Campo obligatorio"
            arte.enlaceRedesSocialesED.requestFocus()
            valido = false
        }

        if (portafolio.isEmpty()) {
            arte.enlacePortafolioED.error = "Campo obligatorio"
            arte.enlacePortafolioED.requestFocus()
            valido = false
        }

        if (yearsExp.isEmpty()) {
            arte.yearsExperienceED.error = "Campo obligatorio"
            arte.yearsExperienceED.requestFocus()
            valido = false
        }

        return valido
    }

    private fun campos_tecnicos(): Boolean {
        val tecnicos = binding.lyTecnicos

        val certificados = tecnicos.certificadosTecnicos

        val especializacion = tecnicos.espezializacionED.text.toString().trim()
        val yearsExp = tecnicos.yearsExperienceED.text.toString().trim()

        var valido = true

        if (especializacion.isEmpty()) {
            tecnicos.espezializacionED.error = "Campo obligatorio"
            tecnicos.espezializacionED.requestFocus()
            valido = false
        }

        if (yearsExp.isEmpty()) {
            tecnicos.yearsExperienceED.error = "Campo obligatorio"
            tecnicos.yearsExperienceED.requestFocus()
            valido = false
        }

        return valido
    }

    private fun campos_servicios_salud(): Boolean {
        val salud = binding.lyServicioSalud

        val certificadosM = salud.ceritificadoMedico
        val certificadosT = salud.certificadosTecnicos
        val carnetMedico = salud.carnetMedico

        val especializacion = salud.espezializacionED.text.toString().trim()
        val yearsExp = salud.yearsExperienceED.text.toString().trim()

        var valido = true

        if (especializacion.isEmpty()) {
            salud.espezializacionED.error = "Campo obligatorio"
            salud.espezializacionED.requestFocus()
            valido = false
        }

        if (yearsExp.isEmpty()) {
            salud.yearsExperienceED.error = "Campo obligatorio"
            salud.yearsExperienceED.requestFocus()
            valido = false
        }

        return valido
    }

    private fun campos_redaccion(): Boolean {
        val redaccion = binding.lyRedacion

        val portafolio = redaccion.portafolioED.text.toString().trim()
        val herramientas = redaccion.herramientaED.text.toString().trim()
        val especializacion = redaccion.especializacionED.text.toString().trim()
        val yearsExp = redaccion.yearsExperienceED.text.toString().trim()

        var valido = true

        if (portafolio.isEmpty()) {
            redaccion.portafolioED.error = "Campo obligatorio"
            redaccion.portafolioED.requestFocus()
            valido = false
        }

        if (herramientas.isEmpty()) {
            redaccion.herramientaED.error = "Campo obligatorio"
            redaccion.herramientaED.requestFocus()
            valido = false
        }

        if (especializacion.isEmpty()) {
            redaccion.especializacionED.error = "Campo obligatorio"
            redaccion.especializacionED.requestFocus()
            valido = false
        }

        if (yearsExp.isEmpty()) {
            redaccion.yearsExperienceED.error = "Campo obligatorio"
            redaccion.yearsExperienceED.requestFocus()
            valido = false
        }

        return valido
    }

    private fun campos_mecanicos(): Boolean {
        val mecanicos = binding.lyMecanico

        val img1 = mecanicos.img1
        val img2 = mecanicos.img2
        val img3 = mecanicos.img3
        val img4 = mecanicos.img4
        val certificados = mecanicos.certificadosTecnicos

        val especializacion = mecanicos.espezializacionED.text.toString().trim()
        val yearsExp = mecanicos.yearsExperienceED.text.toString().trim()

        var valido = true

        if (especializacion.isEmpty()) {
            mecanicos.espezializacionED.error = "Campo obligatorio"
            mecanicos.espezializacionED.requestFocus()
            valido = false
        }

        if (yearsExp.isEmpty()) {
            mecanicos.yearsExperienceED.error = "Campo obligatorio"
            mecanicos.yearsExperienceED.requestFocus()
            valido = false
        }

        return valido
    }

    private fun campos_marketing(): Boolean {
        val marketing = binding.lyMarketing

        val especializacion = marketing.herramentaMarketingED.text.toString().trim()
        val yearsExp = marketing.yearsExperienceED.text.toString().trim()

        var valido = true

        if (especializacion.isEmpty()) {
            marketing.herramentaMarketingED.error = "Campo obligatorio"
            marketing.herramentaMarketingED.requestFocus()
            valido = false
        }

        if (yearsExp.isEmpty()) {
            marketing.yearsExperienceED.error = "Campo obligatorio"
            marketing.yearsExperienceED.requestFocus()
            valido = false
        }

        return valido
    }


}