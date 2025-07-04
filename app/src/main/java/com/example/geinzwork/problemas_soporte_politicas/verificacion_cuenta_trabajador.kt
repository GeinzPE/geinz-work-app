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
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
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
import com.google.firebase.firestore.DocumentReference
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
        launcherCertificadoConstructor =
            crearLauncherPara(binding.lyConstrucion.certificadosTecnicos)

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
        launcherCertificadoTecnicoSalud =
            crearLauncherPara(binding.lyServicioSalud.certificadosTecnicos)
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

            val nombreEd = binding.nombreED
            val apellidoEd = binding.apellidoED
            val descripcionServiciosEd = binding.descripcionServiciosED
            val dniEd = binding.dniED
            val numeroTelfEd = binding.numeroTelfED

            if(verificar_campos()){
                if (tipo_selecionados(binding.categoriaTrabajos.text.toString())) {
                    binding.progressBarContainer.visibility = View.VISIBLE
                    binding.scroll.isVisible = false
                    val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
                    val localDate = LocalDate.parse(binding.fecha.text.toString(), formatter)
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
                                when (binding.categoriaTrabajos.text.toString()) {
                                    "Artes Visuales y Creativas" -> crearHashmapArte(
                                        dbRef,
                                        documentId
                                    )

                                    "Chofer privado" -> crearHashmapChofer(
                                        dbRef,
                                        documentId
                                    )

                                    "Conductor de reparto" -> true // Aún sin validación
                                    "Construcción y hogar" -> crearHashmapConstructor(
                                        dbRef,
                                        documentId
                                    )

                                    "Desarrollo Personal y Bienestar" -> crearHashmapDesarrolloPersonal(
                                        dbRef,
                                        documentId
                                    )

                                    "Desarrollo Web y Programación" -> crearHashmapDesarrollo(
                                        dbRef,
                                        documentId
                                    )

                                    "Diseño Gráfico y Multimedia" -> crearHashmapDesign(
                                        dbRef,
                                        documentId
                                    )

                                    "Educación" -> crearHashmapEducacion(
                                        dbRef,
                                        documentId
                                    )

                                    "Escritura Creativa y Periodismo" -> true // Aún sin validación
                                    "Legal y Jurídico" -> crearHashmapLegal(
                                        dbRef,
                                        documentId
                                    )

                                    "Marketing Digital y Publicidad" -> crearHashmapMarketing(
                                        dbRef,
                                        documentId
                                    )

                                    "Mecánicos" -> crearHashmapMecanicos(
                                        dbRef,
                                        documentId
                                    )

                                    "Redacción y Edición" -> crearHashmapRedaccion(
                                        dbRef,
                                        documentId
                                    )

                                    "Servicios de Salud" -> crearHashmapSalud(
                                        dbRef,
                                        documentId
                                    )

                                    "Tecnicos" -> crearHashmapTecnicos(
                                        dbRef,
                                        documentId
                                    )

                                    else -> true
                                }
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
                } else {
                    Toast.makeText(
                        this,
                        "Por favor completa todos los campos obligatorios",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }


        }
    }

    private fun verificar_campos(): Boolean {
        val nombreEd = binding.nombreED
        val apellidoEd = binding.apellidoED
        val descripcionServiciosEd = binding.descripcionServiciosED
        val dniEd = binding.dniED
        val numeroTelfEd = binding.numeroTelfED
        val correo = binding.correoElectronidoED
        val categoria_trabajo = binding.categoriaTrabajos
        val plan_verificacion = binding.planes

        var valido = true

        if (nombreEd.text.isBlank()) {
            nombreEd.error = "Ingresa un nombre"
            nombreEd.requestFocus()
            valido = false
        }

        if (apellidoEd.text.isBlank()) {
            apellidoEd.error = "Ingresa un apellido"
            apellidoEd.requestFocus()
            valido = false
        }

        if (descripcionServiciosEd.text.isBlank()) {
            descripcionServiciosEd.error = "Ingresa una descripción"
            descripcionServiciosEd.requestFocus()
            valido = false
        }

        if (dniEd.text.isBlank()) {
            dniEd.error = "Ingresa tu DNI"
            dniEd.requestFocus()
            valido = false
        }

        if (numeroTelfEd.text.isBlank()) {
            numeroTelfEd.error = "Ingresa tu número de teléfono"
            numeroTelfEd.requestFocus()
            valido = false
        }

        if (correo.text.isBlank()) {
            correo.error = "Ingresa tu correo electrónico"
            correo.requestFocus()
            valido = false
        }

        if (categoria_trabajo.text.isNullOrBlank() || categoria_trabajo.text.toString() == "Selecciona una categoría") {
            Toast.makeText(this, "Selecciona una categoría de trabajo", Toast.LENGTH_SHORT).show()
            valido = false
        }

        if (plan_verificacion.text.isNullOrBlank() || plan_verificacion.text.toString() == "Selecciona un plan") {
            Toast.makeText(this, "Selecciona un plan de verificación", Toast.LENGTH_SHORT).show()
            valido = false
        }

        if (ImagenPerfil == null || DNIFRONTAL == null || DNIPOSTERIOR == null) {
            Toast.makeText(this, "Por favor, sube las imágenes requeridas (perfil y DNI)", Toast.LENGTH_SHORT).show()
            valido = false
        }

        if (!binding.checkBoxPoliticas.isChecked) {
            Toast.makeText(this, "Debes aceptar las políticas de privacidad", Toast.LENGTH_SHORT).show()
            valido = false
        }

        return valido
    }


    private fun subirImagenes(
        lista: List<Pair<String, Uri>>,
        idDoc: String,
        onComplete: (Map<String, String>) -> Unit
    ) {
        val storage = storage
        val urlsMap = mutableMapOf<String, String>()
        var subidas = 0

        for ((nombre, uri) in lista) {
            val ref = storage.child("$idDoc/${nombre}")
            ref.putFile(uri).continueWithTask { tarea ->
                if (!tarea.isSuccessful) throw tarea.exception
                    ?: Exception("Error subiendo $nombre")
                ref.downloadUrl
            }.addOnSuccessListener { url ->
                urlsMap[nombre] = url.toString()
                subidas++
                if (subidas == lista.size) {
                    onComplete(urlsMap) // Todas subidas
                }
            }.addOnFailureListener {
                Toast.makeText(this, "Error al subir $nombre: ${it.message}", Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }


    private fun crearLauncherPara(imageView: ShapeableImageView): ActivityResultLauncher<String> {
        return registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            uri?.let {
                imageView.setImageURI(it)
                imageView.tag = it // ✅ Guardar el URI para luego subirlo
            }
        }
    }


    private fun tipo_selecionados(categoria: String): Boolean {
        return when (categoria) {
            "Artes Visuales y Creativas" -> campos_arte_antiguedades()
            "Chofer privado" -> campos_chofer_privado()
            "Conductor de reparto" -> campos_chofer_privado() // Aún sin validación
            "Construcción y hogar" -> campos_constructor_hogares()
            "Desarrollo Personal y Bienestar" -> campos_desarrollo_personal()
            "Desarrollo Web y Programación" -> verificar_campos_desarrollo_programacion()
            "Diseño Gráfico y Multimedia" -> verificar_campos_desing_graphic()
            "Educación" -> verificar_campos_educacion()
            "Escritura Creativa y Periodismo" -> campos_redaccion() // Aún sin validación
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
            notificar.enviarNotificacionFCM(
                token,
                "idAdmin",
                "idasdasda",
                "iadasdasda",
                "entrada",
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
                    binding.cargandoCaracteristicas.isVisible = false
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
                    binding.cargandoCaracteristicas.isVisible = true
                    obtener_categorias_verificados("plan_a")

                }

                "Plan B (Verificacion cuenta Geinz)" -> {
                    binding.layoutBeneficios.linealAnuncioVerificado.isVisible = false
                    binding.cargandoCaracteristicas.isVisible = true
                    obtener_categorias_verificados("plan_b")
                }

                "Plan C (Verificacion cuenta Geinz)" -> {

                    binding.layoutBeneficios.linealAnuncioVerificado.isVisible = false
                    binding.cargandoCaracteristicas.isVisible = true
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

//        YAPEO?.let { uri ->
//            val ref = storage.child("$documentId/${Variables.YAPEO}")
//            val uploadTask = ref.putFile(uri)
//                .continueWithTask { task ->
//                    if (!task.isSuccessful) {
//                        task.exception?.let { throw it }
//                    }
//                    ref.downloadUrl
//                }
//            imageUploadTasks.add(uploadTask)
//        }


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

                            // 👉 Limpiar campos antes de cambiar de categoría
                            limpiarCamposTodasLasCategorias()

                            // 👉 Ocultar todos los layouts primero
                            ocultarTodosLosLayouts()

                            // 👉 Mostrar el layout correspondiente a la categoría seleccionada
                            when (seleccionado) {
                                "Construcción y hogar" -> binding.lyConstrucion.principal.isVisible =
                                    true

                                "Servicios de Salud" -> binding.lyServicioSalud.principal.isVisible =
                                    true

                                "Educación" -> binding.lyEducacion.principal.isVisible = true
                                "Legal y Jurídico" -> binding.lyLegal.principal.isVisible = true
                                "Redacción y Edición" -> binding.lyRedacion.principal.isVisible =
                                    true

                                "Diseño Gráfico y Multimedia" -> binding.lyDesing.principal.isVisible =
                                    true

                                "Desarrollo Web y Programación" -> binding.lyDesarrollo.principal.isVisible =
                                    true

                                "Marketing Digital y Publicidad" -> binding.lyMarketing.principal.isVisible =
                                    true

                                "Artes Visuales y Creativas" -> binding.lyArte.principal.isVisible =
                                    true

                                "Desarrollo Personal y Bienestar" -> binding.lyDesarrolloPersonal.principal.isVisible =
                                    true

                                "Escritura Creativa y Periodismo" -> binding.lyRedacion.principal.isVisible =
                                    true

                                "Conductor de reparto", "Chofer privado" -> binding.lyChofer.principal.isVisible =
                                    true

                                "Mecánicos" -> binding.lyMecanico.principal.isVisible = true
                                "Tecnicos" -> binding.lyTecnicos.principal.isVisible = true
                            }
                        }


                    }

                }
            }
    }

    private fun ocultarTodosLosLayouts() {
        with(binding) {
            lyLegal.principal.isVisible = false
            lyEducacion.principal.isVisible = false
            lyDesing.principal.isVisible = false
            lyDesarrollo.principal.isVisible = false
            lyChofer.principal.isVisible = false
            lyConstrucion.principal.isVisible = false
            lyDesarrolloPersonal.principal.isVisible = false
            lyArte.principal.isVisible = false
            lyTecnicos.principal.isVisible = false
            lyServicioSalud.principal.isVisible = false
            lyRedacion.principal.isVisible = false
            lyMecanico.principal.isVisible = false
            lyMarketing.principal.isVisible = false
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

    private fun imagenValida(imageView: ShapeableImageView): Boolean {
        val defaultDrawable = ContextCompat.getDrawable(this, R.drawable.cargar_foto_500x500)
        return imageView.drawable.constantState != defaultDrawable?.constantState
    }


    private fun limpiarCamposTodasLasCategorias() {
        // Utilidad para limpiar EditText y eerrores
        fun limpiarCampo(vararg editTexts: EditText) {
            editTexts.forEach {
                it.setText("")
                it.error = null
            }
        }

        // Utilidad para resetear imagen a una por defecto
        fun resetearImagen(vararg imageViews: ShapeableImageView) {
            imageViews.forEach {
                it.setImageResource(R.drawable.cargar_foto_500x500) // Usa tu imagen por defecto aquí
            }
        }

        with(binding) {
            // Legal
            limpiarCampo(
                lyLegal.especializacionED,
                lyLegal.yearsExperienceED,
                lyLegal.EspecializacionesED
            )

            // Educación
            limpiarCampo(lyEducacion.especializacionED, lyEducacion.yearsExperienceED)

            // Diseño Gráfico
            limpiarCampo(
                lyDesing.portafolioED,
                lyDesing.softwareED,
                lyDesing.yearsExperienceED,
                lyDesing.especializacionED
            )

            // Desarrollo Web
            limpiarCampo(
                lyDesarrollo.repoED,
                lyDesarrollo.portafolioED,
                lyDesarrollo.yearsExperienceED
            )

            // Chofer
            limpiarCampo(lyChofer.descripcionServiciosED)

            // Construcción
            limpiarCampo(lyConstrucion.espezializacionED, lyConstrucion.yearsExperienceED)
            resetearImagen(
                lyConstrucion.img1,
                lyConstrucion.img2,
                lyConstrucion.img3,
                lyConstrucion.img4
            )

            // Desarrollo Personal
            limpiarCampo(
                lyDesarrolloPersonal.especialidadesED,
                lyDesarrolloPersonal.yearsExperienceED
            )

            // Arte
            limpiarCampo(
                lyArte.enlaceRedesSocialesED,
                lyArte.enlacePortafolioED,
                lyArte.yearsExperienceED
            )
            resetearImagen(lyArte.img1, lyArte.img2, lyArte.img3, lyArte.img4)

            // Técnicos
            limpiarCampo(lyTecnicos.espezializacionED, lyTecnicos.yearsExperienceED)

            // Salud
            limpiarCampo(lyServicioSalud.espezializacionED, lyServicioSalud.yearsExperienceED)

            // Redacción
            limpiarCampo(
                lyRedacion.portafolioED,
                lyRedacion.herramientaED,
                lyRedacion.especializacionED,
                lyRedacion.yearsExperienceED
            )

            // Mecánicos
            limpiarCampo(lyMecanico.espezializacionED, lyMecanico.yearsExperienceED)
            resetearImagen(lyMecanico.img1, lyMecanico.img2, lyMecanico.img3, lyMecanico.img4)

            // Marketing
            limpiarCampo(lyMarketing.herramentaMarketingED, lyMarketing.yearsExperienceED)
        }
    }


    private fun verificar_campos_legal_juridico(): Boolean {
        val juridico = binding.lyLegal
        val titulo = juridico.titulosEducativos
        val carnet = juridico.fotoCarnet // opcional
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

        if (!imagenValida(titulo)) {
            Toast.makeText(
                juridico.root.context,
                "Debes subir tu título educativo",
                Toast.LENGTH_SHORT
            ).show()
            valido = false
        }

        return valido
    }

    private fun crearHashmapLegal(db: DocumentReference, id: String) {
        val juridico = binding.lyLegal
        val titulo = juridico.titulosEducativos
        val carnet = juridico.fotoCarnet
        val areas = juridico.especializacionED.text.toString().trim()
        val yearsExp = juridico.yearsExperienceED.text.toString().trim()
        val especializaciones = juridico.EspecializacionesED.text.toString().trim()

        val hasmap = hashMapOf(
            "caracteristicas" to hashMapOf(
                "areasEspecializacion" to areas,
                "añosExperiencia" to yearsExp,
                "especializaciones" to especializaciones,
                "imagenesValidas" to true
            )
        )
        val uriList = listOfNotNull(
            titulo.tag as? Uri,
            carnet.tag as? Uri,

            )
        val listaImagenes = uriList.mapIndexed { index, uri ->
            "img${index + 1}.jpg" to uri
        }
        subirImagenes(listaImagenes, id) { urlsMap ->
        }

        db.set(hasmap, SetOptions.merge()).addOnSuccessListener {
            Toast.makeText(this, "Jurídico guardado", Toast.LENGTH_SHORT).show()
        }.addOnFailureListener {
            Toast.makeText(this, "Error al guardar jurídico: $it", Toast.LENGTH_SHORT).show()
        }
    }


    private fun verificar_campos_educacion(): Boolean {
        val educacion = binding.lyEducacion
        val titulo = educacion.titulosEducativos
        val certificaciones = educacion.certificaciones
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

        if (!imagenValida(titulo) || !imagenValida(certificaciones)) {
            Toast.makeText(this, "Debes subir título educativo y certificación", Toast.LENGTH_SHORT)
                .show()
            valido = false
        }

        return valido
    }

    private fun crearHashmapEducacion(db: DocumentReference, id: String) {
        val educacion = binding.lyEducacion
        val titulo = educacion.titulosEducativos
        val certificaciones = educacion.certificaciones
        val textoEspecializado = educacion.especializacionED.text.toString().trim()
        val yearsExp = educacion.yearsExperienceED.text.toString().trim()

        val hasmap = hashMapOf(
            "caracteristicas" to hashMapOf(
                "especializacion" to textoEspecializado,
                "añosExperiencia" to yearsExp,
                "imagenesValidas" to true
            )
        )
        val uriList = listOfNotNull(
            titulo.tag as? Uri,
            certificaciones.tag as? Uri,

            )
        val listaImagenes = uriList.mapIndexed { index, uri ->
            "img${index + 1}.jpg" to uri
        }
        subirImagenes(listaImagenes, id) { urlsMap ->
        }

        db.set(hasmap, SetOptions.merge()).addOnSuccessListener {
            Toast.makeText(this, "Educación guardada", Toast.LENGTH_SHORT).show()
        }.addOnFailureListener {
            Toast.makeText(this, "Error al guardar educación: $it", Toast.LENGTH_SHORT).show()
        }
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

    private fun crearHashmapDesign(db: DocumentReference, id: String) {
        val desing = binding.lyDesing
        val portafolio = desing.portafolioED.text.toString().trim()
        val software = desing.softwareED.text.toString().trim()
        val yearsExp = desing.yearsExperienceED.text.toString().trim()
        val especializacion = desing.especializacionED.text.toString().trim()

        val hasmap = hashMapOf(
            "caracteristicas" to hashMapOf(
                "portafolio" to portafolio,
                "software" to software,
                "especializacion" to especializacion,
                "añosExperiencia" to yearsExp
            )
        )

        db.set(hasmap, SetOptions.merge()).addOnSuccessListener {
            Toast.makeText(this, "Diseño gráfico guardado", Toast.LENGTH_SHORT).show()
        }.addOnFailureListener {
            Toast.makeText(this, "Error al guardar diseño: $it", Toast.LENGTH_SHORT).show()
        }
    }


    private fun verificar_campos_desarrollo_programacion(): Boolean {
        val desarrollo = binding.lyDesarrollo
        val certificados = desarrollo.certificadosTecnicos

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

        if (!imagenValida(certificados)) {
            Toast.makeText(
                desarrollo.root.context,
                "Debes subir una imagen de tus certificados técnicos",
                Toast.LENGTH_SHORT
            ).show()
            valido = false
        }

        return valido
    }

    private fun crearHashmapDesarrollo(db: DocumentReference, id: String) {
        val desarrollo = binding.lyDesarrollo
        val repo = desarrollo.repoED.text.toString().trim()
        val portafolio = desarrollo.portafolioED.text.toString().trim()
        val yearsExp = desarrollo.yearsExperienceED.text.toString().trim()

        val hasmap = hashMapOf(
            "caracteristicas" to hashMapOf(
                "repositorio" to repo,
                "portafolio" to portafolio,
                "añosExperiencia" to yearsExp,
                "imagenesValidas" to true
            )
        )

        db.set(hasmap, SetOptions.merge()).addOnSuccessListener {
            Toast.makeText(this, "Desarrollo guardado", Toast.LENGTH_SHORT).show()
        }.addOnFailureListener {
            Toast.makeText(this, "Error al guardar desarrollo: $it", Toast.LENGTH_SHORT).show()
        }
    }


    private fun campos_chofer_privado(): Boolean {
        val chofer = binding.lyChofer

        val licencia = chofer.licenciaConducir
        val fotoveiculo = chofer.fotoVeiculo
        val seguro = chofer.seguroVeicular

        val puntosEntrega = chofer.descripcionServiciosED.text.toString().trim()

        var valido = true

        if (puntosEntrega.isEmpty()) {
            chofer.descripcionServiciosED.error = "Campo obligatorio"
            chofer.descripcionServiciosED.requestFocus()
            valido = false
        }

        if (!imagenValida(fotoveiculo)) {
            Toast.makeText(
                chofer.root.context,
                "Debes subir una imagen del vehículo",
                Toast.LENGTH_SHORT
            ).show()
            valido = false
        }

        if (!imagenValida(seguro)) {
            Toast.makeText(
                chofer.root.context,
                "Debes subir una imagen del seguro vehicular",
                Toast.LENGTH_SHORT
            ).show()
            valido = false
        }

        if (!imagenValida(licencia)) {
            Toast.makeText(
                chofer.root.context,
                "Debes subir la licencia de conducir",
                Toast.LENGTH_SHORT
            ).show()
            valido = false
        }

        return valido
    }

    private fun crearHashmapChofer(db: DocumentReference, id: String) {
        val chofer = binding.lyChofer
        val puntosEntrega = chofer.descripcionServiciosED.text.toString().trim()
        val licencia = chofer.licenciaConducir
        val fotoveiculo = chofer.fotoVeiculo
        val seguro = chofer.seguroVeicular
        val uriList = listOfNotNull(
            licencia.tag as? Uri,
            fotoveiculo.tag as? Uri,
            seguro.tag as? Uri,
        )
        val listaImagenes = uriList.mapIndexed { index, uri ->
            "img${index + 1}.jpg" to uri
        }
        subirImagenes(listaImagenes, id) { urlsMap ->
        }
        val hasmap = hashMapOf(
            "caracteristicas" to hashMapOf(
                "puntosEntrega" to puntosEntrega,
                "imagenesValidas" to true
            )
        )

        db.set(hasmap, SetOptions.merge()).addOnSuccessListener {
            Toast.makeText(this, "Chofer guardado", Toast.LENGTH_SHORT).show()
        }.addOnFailureListener {
            Toast.makeText(this, "Error al guardar chofer: $it", Toast.LENGTH_SHORT).show()
        }
    }


    private fun campos_constructor_hogares(): Boolean {
        val constructor = binding.lyConstrucion

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

        if (!imagenValida(constructor.img1) || !imagenValida(constructor.img2)
            || !imagenValida(constructor.img3) || !imagenValida(constructor.img4)
        ) {
            Toast.makeText(this, "Debes subir las 4 imágenes de construcción", Toast.LENGTH_SHORT)
                .show()
            valido = false
        }

        return valido
    }

    private fun crearHashmapConstructor(db: DocumentReference, id: String) {
        val constructor = binding.lyConstrucion
        val especializacion = constructor.espezializacionED.text.toString().trim()
        val yearsExp = constructor.yearsExperienceED.text.toString().trim()
        val img1 = constructor.img1
        val img2 = constructor.img2
        val img3 = constructor.img3
        val img4 = constructor.img4

        val uriList = listOfNotNull(
            img1.tag as? Uri,
            img2.tag as? Uri,
            img3.tag as? Uri,
            img4.tag as? Uri
        )
        val listaImagenes = uriList.mapIndexed { index, uri ->
            "img${index + 1}.jpg" to uri
        }
        subirImagenes(listaImagenes, id) { urlsMap ->
        }
        val hasmap = hashMapOf(
            "caracteristicas" to hashMapOf(
                "especializacion" to especializacion,
                "añosExperiencia" to yearsExp,
                "imagenesValidas" to true
            )
        )

        db.set(hasmap, SetOptions.merge()).addOnSuccessListener {
            Toast.makeText(this, "Constructor guardado", Toast.LENGTH_SHORT).show()
        }.addOnFailureListener {
            Toast.makeText(this, "Error al guardar constructor: $it", Toast.LENGTH_SHORT).show()
        }
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
        if (!imagenValida(certificados)) {
            Toast.makeText(this, "Debes subir el certificado técnico", Toast.LENGTH_SHORT).show()
            valido = false
        }


        return valido
    }

    private fun crearHashmapDesarrolloPersonal(db: DocumentReference, id: String) {
        val desarrollo = binding.lyDesarrolloPersonal
        val certificados = desarrollo.certificadosTecnicos
        val especializacion = desarrollo.especialidadesED.text.toString().trim()
        val yearsExp = desarrollo.yearsExperienceED.text.toString().trim()
        val uriList = listOfNotNull(
            certificados.tag as? Uri,

            )
        val listaImagenes = uriList.mapIndexed { index, uri ->
            "img${index + 1}.jpg" to uri
        }
        subirImagenes(listaImagenes, id) { urlsMap ->
        }

        val hasmap = hashMapOf(
            "caracteristicas" to hashMapOf(
                "especializacion" to especializacion,
                "añosExperiencia" to yearsExp,
                "imagenesValidas" to true
            )
        )

        db.set(hasmap, SetOptions.merge()).addOnSuccessListener {
            Toast.makeText(this, "Desarrollo personal guardado", Toast.LENGTH_SHORT).show()
        }.addOnFailureListener {
            Toast.makeText(this, "Error al guardar desarrollo personal: $it", Toast.LENGTH_SHORT)
                .show()
        }
    }


    private fun campos_arte_antiguedades(): Boolean {
        val arte = binding.lyArte
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

        if (!imagenValida(arte.img1) || !imagenValida(arte.img2)
            || !imagenValida(arte.img3) || !imagenValida(arte.img4)
        ) {
            Toast.makeText(this, "Debes subir las 4 imágenes de arte", Toast.LENGTH_SHORT).show()
            valido = false
        }

        return valido
    }

    private fun crearHashmapArte(db: DocumentReference, id: String) {
        val arte = binding.lyArte
        val img1 = arte.img1
        val img2 = arte.img2
        val img3 = arte.img3
        val img4 = arte.img4

        val uriList = listOfNotNull(
            img1.tag as? Uri,
            img2.tag as? Uri,
            img3.tag as? Uri,
            img4.tag as? Uri
        )
        val listaImagenes = uriList.mapIndexed { index, uri ->
            "img${index + 1}.jpg" to uri
        }
        subirImagenes(listaImagenes, id) { urlsMap ->
        }
        val enlaceRedes = arte.enlaceRedesSocialesED.text.toString().trim()
        val portafolio = arte.enlacePortafolioED.text.toString().trim()
        val yearsExp = arte.yearsExperienceED.text.toString().trim()
        val hasmap = hashMapOf(
            "caracteristicas" to hashMapOf(
                "enlaceRedes" to enlaceRedes,
                "portafolio" to portafolio,
                "añosExperiencia" to yearsExp,
                "imagenesValidas" to true
            )
        )
        db.set(hasmap, SetOptions.merge()).addOnSuccessListener { res ->
            Toast.makeText(this, "se creo la referencia exitosamente", Toast.LENGTH_SHORT).show()
        }.addOnFailureListener { e ->
            Toast.makeText(this, "no se puedo enviar el map $e", Toast.LENGTH_SHORT).show()

        }
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

        if (!imagenValida(certificados)) {
            Toast.makeText(this, "Debes subir el certificado técnico", Toast.LENGTH_SHORT).show()
            valido = false
        }

        return valido
    }

    private fun crearHashmapTecnicos(db: DocumentReference, id: String) {
        val tecnicos = binding.lyTecnicos
        val certificados = tecnicos.certificadosTecnicos
        val especializacion = tecnicos.espezializacionED.text.toString().trim()
        val yearsExp = tecnicos.yearsExperienceED.text.toString().trim()

        val uriList = listOfNotNull(
            certificados.tag as? Uri,
        )
        val listaImagenes = uriList.mapIndexed { index, uri ->
            "img${index + 1}.jpg" to uri
        }
        subirImagenes(listaImagenes, id) { urlsMap ->
        }
        val hasmap = hashMapOf(
            "caracteristicas" to hashMapOf(
                "especializacion" to especializacion,
                "añosExperiencia" to yearsExp,
                "imagenesValidas" to true
            )
        )

        db.set(hasmap, SetOptions.merge()).addOnSuccessListener {
            Toast.makeText(this, "Características técnicas guardadas", Toast.LENGTH_SHORT).show()
        }.addOnFailureListener {
            Toast.makeText(this, "Error al guardar técnicas: $it", Toast.LENGTH_SHORT).show()
        }
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

        if (!imagenValida(certificadosM) || !imagenValida(certificadosT) || !imagenValida(
                carnetMedico
            )
        ) {
            Toast.makeText(
                this,
                "Debes subir certificado médico, técnico y carnet",
                Toast.LENGTH_SHORT
            ).show()
            valido = false
        }

        return valido
    }

    private fun crearHashmapSalud(db: DocumentReference, id: String) {
        val salud = binding.lyServicioSalud
        val certificadosM = salud.ceritificadoMedico
        val certificadosT = salud.certificadosTecnicos
        val carnetMedico = salud.carnetMedico
        val especializacion = salud.espezializacionED.text.toString().trim()
        val yearsExp = salud.yearsExperienceED.text.toString().trim()
        val uriList = listOfNotNull(
            certificadosM.tag as? Uri,
            certificadosT.tag as? Uri,
            carnetMedico.tag as? Uri,
        )
        val listaImagenes = uriList.mapIndexed { index, uri ->
            "img${index + 1}.jpg" to uri
        }
        subirImagenes(listaImagenes, id) { urlsMap ->
        }
        val hasmap = hashMapOf(
            "caracteristicas" to hashMapOf(
                "especializacion" to especializacion,
                "añosExperiencia" to yearsExp,
                "imagenesValidas" to true
            )
        )

        db.set(hasmap, SetOptions.merge()).addOnSuccessListener {
            Toast.makeText(this, "Características de salud guardadas", Toast.LENGTH_SHORT).show()
        }.addOnFailureListener {
            Toast.makeText(this, "Error al guardar salud: $it", Toast.LENGTH_SHORT).show()
        }
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

    private fun crearHashmapRedaccion(db: DocumentReference, id: String) {
        val redaccion = binding.lyRedacion
        val portafolio = redaccion.portafolioED.text.toString().trim()
        val herramientas = redaccion.herramientaED.text.toString().trim()
        val especializacion = redaccion.especializacionED.text.toString().trim()
        val yearsExp = redaccion.yearsExperienceED.text.toString().trim()

        val hasmap = hashMapOf(
            "caracteristicas" to hashMapOf(
                "portafolio" to portafolio,
                "herramientas" to herramientas,
                "especializacion" to especializacion,
                "añosExperiencia" to yearsExp
            )
        )

        db.set(hasmap, SetOptions.merge()).addOnSuccessListener {
            Toast.makeText(this, "Redacción guardada correctamente", Toast.LENGTH_SHORT).show()
        }.addOnFailureListener {
            Toast.makeText(this, "Error al guardar redacción: $it", Toast.LENGTH_SHORT).show()
        }
    }


    private fun campos_mecanicos(): Boolean {
        val mecanicos = binding.lyMecanico

        val img1 = mecanicos.img1
        val img2 = mecanicos.img2
        val img3 = mecanicos.img3
        val img4 = mecanicos.img4
        val certificado = mecanicos.certificadosTecnicos

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

        if (!imagenValida(img1) || !imagenValida(img2) || !imagenValida(img3) || !imagenValida(
                img4
            )
        ) {
            Toast.makeText(this, "Debes subir las 4 imágenes mecánicas", Toast.LENGTH_SHORT)
                .show()
            valido = false
        }

        if (!imagenValida(certificado)) {
            Toast.makeText(
                this,
                "Debes subir al menos un certificado técnico",
                Toast.LENGTH_SHORT
            ).show()
            valido = false
        }

        return valido
    }

    private fun crearHashmapMecanicos(db: DocumentReference, id: String) {
        val mecanicos = binding.lyMecanico
        val especializacion = mecanicos.espezializacionED.text.toString().trim()
        val yearsExp = mecanicos.yearsExperienceED.text.toString().trim()
        val uriList = listOfNotNull(
            mecanicos.img1 as? Uri,
            mecanicos.img2 as? Uri,
            mecanicos.img3 as? Uri,
            mecanicos.img4 as? Uri
        )
        val listaImagenes = uriList.mapIndexed { index, uri ->
            "img${index + 1}.jpg" to uri
        }
        subirImagenes(listaImagenes, id) { urlsMap ->
        }
        val hasmap = hashMapOf(
            "caracteristicas" to hashMapOf(
                "especializacion" to especializacion,
                "añosExperiencia" to yearsExp,
                "imagenesValidas" to true
            )
        )

        db.set(hasmap, SetOptions.merge()).addOnSuccessListener {
            Toast.makeText(this, "Mecánica guardada correctamente", Toast.LENGTH_SHORT).show()
        }.addOnFailureListener {
            Toast.makeText(this, "Error al guardar mecánica: $it", Toast.LENGTH_SHORT).show()
        }
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

    private fun crearHashmapMarketing(db: DocumentReference, id: String) {
        val marketing = binding.lyMarketing
        val especializacion = marketing.herramentaMarketingED.text.toString().trim()
        val yearsExp = marketing.yearsExperienceED.text.toString().trim()

        val hasmap = hashMapOf(
            "caracteristicas" to hashMapOf(
                "herramientaMarketing" to especializacion,
                "añosExperiencia" to yearsExp
            )
        )


        db.set(hasmap, SetOptions.merge()).addOnSuccessListener {
            Toast.makeText(this, "Marketing guardado correctamente", Toast.LENGTH_SHORT).show()
        }.addOnFailureListener {
            Toast.makeText(this, "Error al guardar marketing: $it", Toast.LENGTH_SHORT).show()
        }
    }

}