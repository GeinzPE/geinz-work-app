package com.geinzz.geinzwork.fragmentos

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.InputType
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import com.bumptech.glide.Glide
import com.example.geinzwork.constantesGeneral.Variables
import com.example.geinzwork.constantesGeneral.constantes_nombre_usuarios
import com.example.geinzwork.constantesGeneral.constatnes_carga_imagenes_general
import com.geinzz.geinzwork.EditarInfo
import com.geinzz.geinzwork.Login
import com.geinzz.geinzwork.R
import com.geinzz.geinzwork.constantesGeneral.constantes
import com.geinzz.geinzwork.constantesGeneral.constantesImagenes
import com.geinzz.geinzwork.constantesGeneral.constantes_cuenta_user
import com.geinzz.geinzwork.constantesGeneral.constantes_servicios
import com.geinzz.geinzwork.databinding.BottomSheetConfigCuentaBinding
import com.geinzz.geinzwork.databinding.BottomSheetEditarCamposBinding
import com.geinzz.geinzwork.databinding.BottomSheetVerificadoMasInfoBinding
import com.geinzz.geinzwork.databinding.FragmentCuentaFracmentBinding
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.auth.User
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch


class cuentaFracment : Fragment() {
    private lateinit var binding: FragmentCuentaFracmentBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var mContex: Context
    private lateinit var dialog: BottomSheetDialog
    private var imgPerfilUri: Uri? = null
    private var portadaFoto: Uri? = null
    private var idUSer = ""
    val imgperfil = Variables.ImagenPerfl
    val foto_portada = Variables.foto_portada
    private val pciMEdia =
        registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
            if (uri != null) {
                imgPerfilUri = uri
                binding.imagenPerfil.setImageURI(uri)
                subirImagenStorage(uri, imgperfil)
            } else {
                println("Imagen no seleccionada")
            }
        }

    private val picmedaiFotoPoprtada =
        registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
            if (uri != null) {
                portadaFoto = uri
                binding.fotoPortada.setImageURI(uri)
                subirImagenStorage(uri, foto_portada)
            } else {
                println("Imagen no seleccionada")
            }
        }


    override fun onAttach(context: Context) {
        mContex = context
        super.onAttach(context)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        binding = FragmentCuentaFracmentBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        firebaseAuth = FirebaseAuth.getInstance()
        constantes.carga(3000, { mostrarDatos() })
        confSwipe()
        verificarDeDondeEsUsuario(firebaseAuth.uid.toString())
        constantes.obtenerEstado(binding.estado, firebaseAuth.uid.toString())

        verificarEstado_vericiacion(firebaseAuth.uid.toString())
        binding.popup.setOnClickListener {
            dialog = BottomSheetDialog(mContex)
            activity?.let { it1 ->
                constantes_cuenta_user.bottom_shett_config(
                    dialog,
                    mContex,
                    it1,
                    binding.tipoCuenta,
                    binding.plan.text.toString()

                )
            }
            dialog.show()
        }
        binding.imagenPerfil.setOnClickListener {
            pciMEdia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }
        binding.fotoPortada.setOnClickListener {
            picmedaiFotoPoprtada.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }

        binding.editarnombre.setOnClickListener {
            dialog = BottomSheetDialog(mContex)
            bottomShettEditar_informaciones(
                binding.tipoCuenta.text.toString(),
                Variables.nombre,
                binding.nombreUser.text.toString()
            )
            dialog.show()

        }
        binding.editarapellido.setOnClickListener {
            dialog = BottomSheetDialog(mContex)
            bottomShettEditar_informaciones(
                binding.tipoCuenta.text.toString(),
                Variables.apellido,
                binding.apellidoUSer.text.toString()
            )
            dialog.show()
//            var apellidoUser = binding.apellidoUSer.text.toString()
//            val vista = Intent(mContex, EditarInfo::class.java)
//            vista.putExtra(Variables.TipoCuenta, binding.tipoCuenta.text.toString())
//            vista.putExtra(Variables.tipo, Variables.apellido)
//            vista.putExtra(Variables.valor, apellidoUser)
//            startActivity(vista)
        }
        binding.editarNumero.setOnClickListener {
            dialog = BottomSheetDialog(mContex)
            bottomShettEditar_informaciones(
                binding.tipoCuenta.text.toString(),
                Variables.numeroT,
                binding.numero.text.toString()
            )
            dialog.show()
//            var telefonouser = binding.numero.text.toString()
//            val vista = Intent(mContex, EditarInfo::class.java)
//            vista.putExtra(Variables.TipoCuenta, binding.tipoCuenta.text.toString())
//            vista.putExtra(Variables.tipo, Variables.numeroT)
//            vista.putExtra(Variables.valor, telefonouser)
//            startActivity(vista)
        }
        binding.informacion.setOnClickListener {
            dialog = BottomSheetDialog(mContex)
            informacion_verificados(firebaseAuth.uid.toString())
            dialog.show()
        }
        binding.editarDescripcion.setOnClickListener {
            dialog = BottomSheetDialog(mContex)
            bottomShettEditar_informaciones(
                binding.tipoCuenta.text.toString(),
                Variables.descripcion,
                binding.descripcion.text.toString()
            )
            dialog.show()
//            var descripcionUser = binding.descripcion.text.toString()
//            val vista = Intent(mContex, EditarInfo::class.java)
//            vista.putExtra(Variables.TipoCuenta, binding.tipoCuenta.text.toString())
//            vista.putExtra(Variables.tipo, Variables.descripcion)
//            vista.putExtra(Variables.valor, descripcionUser)
//            startActivity(vista)
        }
        binding.editarNombreUsuario.setOnClickListener {
            dialog = BottomSheetDialog(mContex)
            bottomShettEditar_informaciones(
                binding.tipoCuenta.text.toString(),
                Variables.Nombre_usuario,
                binding.nombreUsuario.text.toString()
            )
            dialog.show()
        }

    }

    private fun informacion_verificados(id_Trabajador: String) {
        val bottomSheet_verificado =
            BottomSheetVerificadoMasInfoBinding.inflate(LayoutInflater.from(mContex))

        val img_verificado = bottomSheet_verificado.insigniaVerificado
        val fecha_verificiada = bottomSheet_verificado.fechaVerificado
        val fecha_vencimineto = bottomSheet_verificado.fechaVencimiento
        val plan_verificado = bottomSheet_verificado.planVerificado
        val beneficios_verificado = bottomSheet_verificado.beneficiosVerificados


        val db = FirebaseFirestore.getInstance().collection("solicitudes_servicios")
            .document("verificaciones")

        db.collection("activos").get().addOnSuccessListener { res ->
            for (documents in res) {
                val documentId = documents.id
                val data = documents.data
                val plan = data?.get("plan") as? String ?: ""
                val fechas = data.get("fechas") as? Map<*, *>

                if (fechas != null) {
                    val fechaActivacion = fechas["fecha_activacion"] as? String ?: ""
                    val fechaVencimiento = fechas["fecha_vencimiento"] as? String ?: ""
                    bottomSheet_verificado.fechaVerificado.text = fechaActivacion
                    bottomSheet_verificado.fechaVencimiento.text = fechaVencimiento
                }

                Log.d("Firestore_verificados", "ID del documento: $documentId su plan es $plan")

                if (documentId == id_Trabajador) {
                    when (plan) {
                        "A" -> {
                            bottomSheet_verificado.planVerificado.text = "Plan A"
                            setear_caracteristicasVerificados(db,"plan_a",bottomSheet_verificado)
                            bottomSheet_verificado.iconoVerificado.setImageResource(R.drawable.verificado_a)

                        }

                        "B" -> {
                            bottomSheet_verificado.planVerificado.text = "Plan B"
                            setear_caracteristicasVerificados(db,"plan_b",bottomSheet_verificado)
                            bottomSheet_verificado.iconoVerificado.setImageResource(R.drawable.icon_verificado)

                        }

                        "C" -> {
                            bottomSheet_verificado.planVerificado.text = "Plan C"
                            setear_caracteristicasVerificados(db,"plan_c",bottomSheet_verificado)
                            bottomSheet_verificado.iconoVerificado.setImageResource(R.drawable.verificado_c)

                        }
                    }
                }
            }
        }.addOnFailureListener { e ->
            Log.e("Firestore", "Error al obtener documentos", e)
        }

        db.collection("beneficios_planes_verificados").get().addOnSuccessListener { res ->

        }

        val view = bottomSheet_verificado.root
        dialog.setContentView(view)

    }

    private fun setear_caracteristicasVerificados(
        db: DocumentReference,
        documento: String,
        bottomSheet_verificado: BottomSheetVerificadoMasInfoBinding
    ) {
        db.collection("beneficios_planes_verificados").document(documento)
            .get().addOnSuccessListener { res ->
                if (res.exists()) {
                    val data = res.data
                    val caracteristicas = data?.get("caracteristicas") as? List<*>
                    if (caracteristicas != null) {
                        val builder = StringBuilder()
                        for (caracteristica in caracteristicas) {
                            val texto = caracteristica as? String
                            texto?.let {
                                builder.append("- $it\n") // salto de línea después de cada ítem
                            }
                        }
                        bottomSheet_verificado.beneficiosVerificados.text = builder.toString().trim()
                    }
                }
            }
    }


    @RequiresApi(Build.VERSION_CODES.O)
    private fun bottomShettEditar_informaciones(tipoCuenta: String, tipo: String, valor: String) {
        val bottomSheet = BottomSheetEditarCamposBinding.inflate(LayoutInflater.from(mContex))
        val view = bottomSheet.root
        val referenciaTrabajador =
            FirebaseFirestore.getInstance().collection(Variables.trabajadores_usuariosDB)
                .document(Variables.trabajadoresDB).collection(Variables.trabajadoresDB)
                .document(firebaseAuth.uid.toString())
        val referenciaUsuario =
            FirebaseFirestore.getInstance().collection(Variables.trabajadores_usuariosDB)
                .document(Variables.usuarios_db).collection(Variables.usuarios_db)
                .document(firebaseAuth.uid.toString())

        when (tipoCuenta) {
            "Cuenta Simple" -> {
                if (tipo != null && valor != null) {
                    when (tipo) {
                        "nombre" -> {
                            bottomSheet.informacion.text = "nombre"
                            bottomSheet.nombre.textoCambiante.text =
                                getString(R.string.texto_nombre)
                            bottomSheet.nombre.linealCamposGeneral.isVisible = true
                            bottomSheet.nombre.campoReferidoED.setText(valor)
                            bottomSheet.enviar.setOnClickListener {
                                val nombre = bottomSheet.nombre.campoReferidoED.text.toString()
                                ActulizarDatos(
                                    bottomSheet,
                                    referenciaUsuario,
                                    nombre,
                                    "nombre",
                                    false
                                )
                            }
                        }

                        "numeroT" -> {
                            bottomSheet.numero.textoCambiante.text =
                                getString(R.string.texto_numero)
                            bottomSheet.informacion.text = "telefono"
                            bottomSheet.numero.linealCamposGeneral.isVisible = true
                            bottomSheet.numero.campoReferidoED.inputType =
                                InputType.TYPE_CLASS_NUMBER
                            bottomSheet.numero.campoReferidoED.setText(valor)
                            bottomSheet.enviar.setOnClickListener {
                                val telefono = bottomSheet.numero.campoReferidoED.text.toString()
                                ActulizarDatos(
                                    bottomSheet,
                                    referenciaUsuario,
                                    telefono,
                                    "numero",
                                    false
                                )
                            }
                        }

                        "apellido" -> {
                            bottomSheet.informacion.text = "apellido"
                            bottomSheet.apellido.textoCambiante.text =
                                getString(R.string.texto_apellido)
                            println("Se envió el apellido: $valor")
                            bottomSheet.apellido.linealCamposGeneral.isVisible = true
                            bottomSheet.apellido.campoReferidoED.setText(valor)
                            bottomSheet.enviar.setOnClickListener {
                                val apellido = bottomSheet.apellido.campoReferidoED.text.toString()
                                ActulizarDatos(
                                    bottomSheet,
                                    referenciaUsuario,
                                    apellido,
                                    "apellido",
                                    false
                                )
                            }
                        }


                        Variables.Nombre_usuario -> {
                            bottomSheet.informacion.text = "Nombre de usuario"
                            bottomSheet.nombreUser.textoCambiante.text =
                                getString(R.string.texto_nombre_usuario)
                            bottomSheet.nombreUser.linealCamposGeneral.isVisible = true
                            bottomSheet.nombreUser.nuevaFechaEdicion.isVisible = true
                            constantes_nombre_usuarios.obtene_fechas_cambios(firebaseAuth.uid.toString()) { f1, f2 ->
                                bottomSheet.nombreUser.nuevaFechaEdicion.text =
                                    "Fecha de edicion fue $f1 y finaliza el $f2"
                            }
                            bottomSheet.nombreUser.campoReferidoED.setText(valor)
                            bottomSheet.enviar.setOnClickListener {
                                val Nombre_usuario =
                                    bottomSheet.nombreUser.campoReferidoED.text.toString()
                                ActulizarDatos(
                                    bottomSheet,
                                    referenciaUsuario,
                                    Nombre_usuario,
                                    "Nombre_usuario",
                                    true
                                )
                            }
                        }

                        else -> {
                            println("Tipo de valor desconocido: $tipo")
                        }
                    }
                }
            }

            "Cuenta Trabajador" -> {
                if (tipo != null && valor != null) {
                    when (tipo) {
                        "nombre" -> {
                            bottomSheet.informacion.text = "nombre"
                            bottomSheet.nombreUser.linealCamposGeneral.isVisible = true
                            bottomSheet.nombreUser.textoCambiante.text =
                                getString(R.string.texto_nombre)
                            bottomSheet.nombreUser.campoReferidoED.setText(valor)
                            bottomSheet.enviar.setOnClickListener {
                                val nombre = bottomSheet.nombreUser.campoReferidoED.text.toString()
                                ActulizarDatos(
                                    bottomSheet,
                                    referenciaTrabajador,
                                    nombre,
                                    "nombre",
                                    false
                                )
                            }
                        }

                        "numeroT" -> {
                            bottomSheet.informacion.text = "telefono"
                            bottomSheet.numero.textoCambiante.text =
                                getString(R.string.texto_numero)
                            bottomSheet.numero.linealCamposGeneral.isVisible = true
                            bottomSheet.numero.campoReferidoED.setText(valor)
                            bottomSheet.numero.campoReferidoED.inputType =
                                InputType.TYPE_CLASS_NUMBER
                            bottomSheet.enviar.setOnClickListener {
                                val telefono = bottomSheet.numero.campoReferidoED.text.toString()
                                ActulizarDatos(
                                    bottomSheet,
                                    referenciaTrabajador,
                                    telefono,
                                    "numero",
                                    false
                                )
                            }
                        }

                        "apellido" -> {
                            bottomSheet.apellido.textoCambiante.text =
                                getString(R.string.texto_apellido)
                            bottomSheet.informacion.text = "apellido"
                            println("Se envió el apellido: $valor")
                            bottomSheet.apellido.linealCamposGeneral.isVisible = true
                            bottomSheet.apellido.campoReferidoED.setText(valor)
                            bottomSheet.enviar.setOnClickListener {
                                val apellido = bottomSheet.apellido.campoReferidoED.text.toString()
                                ActulizarDatos(
                                    bottomSheet,
                                    referenciaTrabajador,
                                    apellido,
                                    "apellido",
                                    false
                                )
                            }
                        }

                        "descripcion" -> {
                            bottomSheet.descripcion.textoCambiante.text =
                                getString(R.string.texto_descripcion)
                            bottomSheet.informacion.text = "Descripcion"
                            bottomSheet.descripcion.linealCamposGeneral.isVisible = true
                            bottomSheet.descripcion.campoReferidoED.setText(valor)
                            bottomSheet.numero.campoReferidoED.inputType =
                                InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_MULTI_LINE
                            bottomSheet.enviar.setOnClickListener {
                                val fechaN = bottomSheet.descripcion.campoReferidoED.text.toString()
                                ActulizarDatos(
                                    bottomSheet,
                                    referenciaTrabajador,
                                    fechaN,
                                    "descripcion",
                                    false
                                )
                            }
                        }

                        Variables.Nombre_usuario -> {
                            bottomSheet.nombreUser.textoCambiante.text =
                                getString(R.string.texto_nombre_usuario)
                            bottomSheet.informacion.text = "Nombre de usuario"
                            bottomSheet.nombreUser.linealCamposGeneral.isVisible = true
                            bottomSheet.nombreUser.campoReferidoED.setText(valor)
                            bottomSheet.nombreUser.nuevaFechaEdicion.isVisible = true
                            constantes_nombre_usuarios.obtene_fechas_cambios(firebaseAuth.uid.toString()) { f1, f2 ->
                                bottomSheet.nombreUser.nuevaFechaEdicion.text =
                                    "Fecha de edicion fue $f1 y finaliza el $f2"
                            }
                            bottomSheet.enviar.setOnClickListener {
                                val Nombre_usuario =
                                    bottomSheet.nombreUser.campoReferidoED.text.toString()
                                ActulizarDatos(
                                    bottomSheet,
                                    referenciaTrabajador,
                                    Nombre_usuario,
                                    "Nombre_usuario",
                                    true
                                )
                            }
                        }

                        else -> {
                            println("Tipo de valor desconocido: $tipo")
                        }
                    }
                }
            }

        }

        dialog.setContentView(view)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun ActulizarDatos(
        bottomSheetDialog: BottomSheetEditarCamposBinding,
        dbDocumento: DocumentReference,
        valorCambiado: String,
        instaDB: String,
        verificar_Nombre_user: Boolean
    ) {
        if (valorCambiado.isBlank()) {
            Toast.makeText(mContex, "El campo no puede estar vacío", Toast.LENGTH_SHORT).show()
            return
        }
        val nuevoUsername = bottomSheetDialog.nombreUser.campoReferidoED.text.toString()
        if (verificar_Nombre_user) {
            constantes_nombre_usuarios.actualizar_nombre_usuario(
                bottomSheetDialog,
                dialog, dbDocumento, instaDB, valorCambiado,
                firebaseAuth.uid.toString(),
                nuevoUsername, mContex
            )
        } else {
            actualizarCampo(dbDocumento, instaDB, valorCambiado) {}
        }
    }

    private fun actualizarCampo(
        dbDocumento: DocumentReference,
        campo: String,
        valor: String,
        onSuccess: () -> Unit
    ) {
        val hashMap = hashMapOf<String, Any>(campo to valor)
        dbDocumento.update(hashMap)
            .addOnSuccessListener {
                Toast.makeText(
                    mContex,
                    "Campo actualizado correctamente Actualiza para ver tus cambios.",
                    Toast.LENGTH_SHORT
                ).show()
                onSuccess()
                dialog.dismiss()
            }
            .addOnFailureListener { e ->
                Toast.makeText(
                    mContex,
                    "Error al actualizar el campo",
                    Toast.LENGTH_SHORT
                ).show()
                Log.d("erro_actualziad", "${e.message}")
            }
    }


    @RequiresApi(Build.VERSION_CODES.O)
    private fun confSwipe() {
        binding.swipe.setOnRefreshListener {
            binding.swipe.setColorSchemeResources(R.color.violeta)
            Handler(Looper.getMainLooper()).postDelayed({
                binding.swipe.isRefreshing = false
                verificarDeDondeEsUsuario(firebaseAuth.uid.toString())
                constantes.obtenerEstado(binding.estado, firebaseAuth.uid.toString())
            }, 2000)
        }
    }

    private fun mostrarDatos() {
        binding.loading.isVisible = false
        binding.swipe.isVisible = true
    }

    private fun verificarEstado_vericiacion(id: String) {
        val db = FirebaseFirestore.getInstance().collection(Variables.solicitud_servicios)
            .document(Variables.verificaiones).collection(Variables.activos).document(id)
        db.get().addOnSuccessListener { res ->
            if (res.exists()) {
                val data = res.data
                val estado = data?.get(Variables.estado) as? Boolean
                val plan = data?.get(Variables.plan) as? String
                when (plan) {
                    Variables.plaA -> {
                        binding.iconVerificado.setImageResource(R.drawable.verificado_a)

                    }

                    Variables.planB -> {
                        binding.iconVerificado.setImageResource(R.drawable.icon_verificado)
                    }

                    Variables.PlanC -> {
                        binding.iconVerificado.setImageResource(R.drawable.verificado_c)
                    }
                }
                if (estado != false) {
                    binding.verificado.text = "Verificado"
                    binding.iconVerificado.isVisible = true
                } else {
                    binding.verificado.text = "No Verificado"
                }
                if (plan.isNullOrEmpty() || plan.equals(Variables.plaA)) {
                    binding.linealPlan.isVisible = false

                } else if (plan.equals(Variables.planB) || plan.equals(Variables.PlanC)) {
                    binding.linealPlan.isVisible = true
                }
                binding.plan.text = plan
            } else {
                binding.linealPlan.isVisible = false
                binding.verificado.text = "No Verificado"
            }
        }
            .addOnFailureListener {
                println("No se encontro la verificacion del usuario")
            }
    }

    private fun verificarDeDondeEsUsuario(userid: String) {
        val db = FirebaseFirestore.getInstance().collection(Variables.trabajadores_usuariosDB)
            .document(Variables.usuarios_db).collection(Variables.usuarios_db)
        db.document(userid).get()
            .addOnSuccessListener { encontrado ->
                if (encontrado.exists()) {
                    binding.horarioReloj.isVisible = false
                    binding.linealCategoriaT.isVisible = false
                    binding.linealTipoTrabajo.isVisible = false
                    binding.linealHorario.isVisible = false
                    binding.linealVerificado.isVisible = false
                    binding.linealDescripcion.isVisible = false
                    leerinfoNormal()
                    comprovarSeccion(Variables.usuarios_db)
                    binding.fotoPortada.setOnClickListener {
                        Toast.makeText(
                            mContex,
                            "solo los trabajadores pueden subir fotos de portada",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        val db2 = FirebaseFirestore.getInstance().collection(Variables.trabajadores_usuariosDB)
            .document(Variables.trabajadoresDB).collection(Variables.trabajadoresDB)
        db2.document(userid).get()
            .addOnSuccessListener { encontrado ->
                if (encontrado.exists()) {
                    println("encontrado en la usuarios sssss")
                    leerInfo()
                    comprovarSeccion(Variables.trabajadoresDB)
                }
            }
    }

    private fun comprovarSeccion(collection: String) {
        if (firebaseAuth.currentUser == null) {
            startActivity(Intent(mContex, Login::class.java))
        } else {
            pasamosToken(collection)
        }
    }

    @SuppressLint("SuspiciousIndentation")
    private fun leerinfoNormal() {
        val db = FirebaseFirestore.getInstance().collection(Variables.trabajadores_usuariosDB)
            .document(Variables.usuarios_db).collection(Variables.usuarios_db)
            .document("${firebaseAuth.uid}")
        db.get()
            .addOnSuccessListener { documentSnapshot ->
                if (documentSnapshot.exists()) {
                    val userData = documentSnapshot.data
                    val nombre = userData?.get(Variables.nombre) as? String ?: ""
                    val apellido = userData?.get(Variables.apellido) as? String ?: ""
                    val codigoPais = userData?.get(Variables.codigo_pais) as? String ?: ""
                    val correo = userData?.get(Variables.correo) as? String ?: ""
                    val fechaNac = userData?.get(Variables.fechaNac) as? String ?: ""
                    val genero = userData?.get(Variables.genero) as? String ?: ""
                    idUSer = (userData?.get(Variables.id) as? String).toString() ?: ""
                    val imagenPerfil = userData?.get(Variables.imagenPerfil) as? String ?: ""
                    val localidad = userData?.get(Variables.localidad) as? String ?: ""
                    val nacionalidad = userData?.get(Variables.nacionalidad) as? String ?: ""
                    val numero = userData?.get(Variables.numero) as? String ?: ""
                    val edadUSer = userData?.get(Variables.EdadActual) as? String ?: ""
                    val TipoCuenta = userData?.get(Variables.TipoCuenta) as? String ?: ""
                    val nombre_usuario = userData?.get(Variables.Nombre_usuario) as? String ?: ""

                    obtenerPerfil(
                        firebaseAuth.uid!!,
                        binding.imagenPerfil,
                        Variables.trabajadores_usuariosDB,
                        Variables.usuarios_db,
                        Variables.usuarios_db
                    )
                    binding.nombreUsuario.text = "$nombre_usuario"
                    binding.nombreUser.text = nombre
                    binding.apellidoUSer.text = apellido
                    binding.fechaNaciminetoUSer.text = fechaNac
                    binding.nacionnalidadUser.text = nacionalidad
                    binding.genero.text = genero
                    binding.CorreoUsuario.text = correo
                    binding.localidad.text = localidad
                    binding.numero.text = "+${codigoPais} ${numero}"
                    binding.edadUser.text = "${edadUSer} años"
                    binding.tipoCuenta.text = TipoCuenta

                    val placeholderperfil =
                        ContextCompat.getDrawable(mContex, R.drawable.img_perfil)
                    constatnes_carga_imagenes_general.changer_img(
                        binding.progressCargaImagen,
                        mContex,
                        imagenPerfil,
                        binding.imagenPerfil,
                        null,
                        "perfil", placeholderperfil
                    ) {}

                }
            }
    }

    private fun pasamosToken(collection: String) {
        val muid = "${firebaseAuth.uid}"
        FirebaseMessaging.getInstance().token
            .addOnSuccessListener { tokens ->
                val token = hashMapOf<String, Any>(
                    "token" to "$tokens"
                )
                val db =
                    FirebaseFirestore.getInstance().collection(Variables.trabajadores_usuariosDB)
                        .document(collection).collection(collection).document(muid)
                db.update(token)
                    .addOnSuccessListener {
                        agregarListToken(muid, tokens)
                    }
                    .addOnFailureListener { e -> Log.e("token", "error $e") }
            }.addOnFailureListener { e -> Log.e("token", "error $e") }
    }

    private fun agregarListToken(id: String, token: String) {
        val db = FirebaseFirestore.getInstance().collection(Variables.trabajadores_usuariosDB)
            .document(Variables.tokens).collection(Variables.tokens).document(id)
        val hasmap = hashMapOf<String, Any>(
            "token" to token
        )
        db.set(hasmap, SetOptions.merge()).addOnSuccessListener {
            Log.d("tokenUSer", "token subido correctaemnte")
        }.addOnFailureListener { e ->
            Log.e("tokenUSer", "error al subir el token")
        }
    }

    private fun leerInfo() {
        val collectionid =
            FirebaseFirestore.getInstance().collection(Variables.trabajadores_usuariosDB)
                .document(Variables.trabajadoresDB).collection(Variables.trabajadoresDB)
                .document("${firebaseAuth.uid}")

        collectionid.get()
            .addOnSuccessListener { documentSnapshot ->
                if (documentSnapshot.exists()) {
                    val userData = documentSnapshot.data
                    val nombre = userData?.get(Variables.nombre) as? String ?: ""
                    val apellido = userData?.get(Variables.apellido) as? String ?: ""
                    val categoriaTrabajo =
                        userData?.get(Variables.categoriaTrabajo) as? String ?: ""
                    val codigoPais = userData?.get(Variables.codigo_pais) as? String ?: ""
                    val correo = userData?.get(Variables.correo) as? String ?: ""
                    val fechaNac = userData?.get(Variables.fechaNac) as? String ?: ""
                    val genero = userData?.get(Variables.genero) as? String ?: ""
                    val horario1 = userData?.get(Variables.horario1) as? String ?: ""
                    val horario2 = userData?.get(Variables.horario2) as? String ?: ""
                    val imagenPerfil = userData?.get(Variables.imagenPerfil) as? String ?: ""
                    val localidad = userData?.get(Variables.localidad) as? String ?: ""
                    val nacionalidad = userData?.get(Variables.nacionalidad) as? String ?: ""
                    val numero = userData?.get(Variables.numero) as? String ?: ""
                    val tipoTrabajo = userData?.get(Variables.tipoTrabajo) as? String ?: ""
                    val edadUSer = userData?.get(Variables.EdadActual) as? String ?: ""
                    val TipoCuenta = userData?.get(Variables.TipoCuenta) as? String ?: ""
                    val descripcion = userData?.get(Variables.descripcion) as? String ?: ""
                    val nombre_usuario = userData?.get(Variables.Nombre_usuario) as? String ?: ""

                    obtenerPerfil(
                        firebaseAuth.uid!!,
                        binding.imagenPerfil,
                        Variables.trabajadores_usuariosDB,
                        Variables.trabajadoresDB,
                        Variables.trabajadoresDB
                    )
                    binding.nombreUsuario.text = "$nombre_usuario"

                    binding.descripcion.text = descripcion

                    binding.nombreUser.text = nombre
                    binding.apellidoUSer.text = apellido
                    binding.fechaNaciminetoUSer.text = fechaNac
                    binding.nacionnalidadUser.text = nacionalidad
                    binding.genero.text = genero
                    binding.CorreoUsuario.text = correo
                    binding.cat.text = categoriaTrabajo
                    binding.tipoTrabajores.text = tipoTrabajo
                    binding.localidad.text = localidad
                    binding.numero.text = "+${codigoPais} ${numero}"
                    binding.horarioUser.text = "De ${horario1} a ${horario2}"
                    binding.edadUser.text = "${edadUSer} años"
                    binding.tipoCuenta.text = TipoCuenta


                    val placeholderperfil =
                        ContextCompat.getDrawable(mContex, R.drawable.img_perfil)
                    constatnes_carga_imagenes_general.changer_img(
                        binding.progressCargaImagen,
                        mContex,
                        imagenPerfil,
                        binding.imagenPerfil,
                        null,
                        "perfil", placeholderperfil
                    ) {}
                }
            }
        val refStorage =
            FirebaseStorage.getInstance().getReference(Variables.usuarios_db)
                .child(firebaseAuth.uid.toString()).child(foto_portada)
        if (refStorage != null) {
            refStorage.downloadUrl.addOnSuccessListener { uri ->
                val imgUrl = uri.toString()
                try {
                    Glide.with(this)
                        .load(imgUrl)
                        .into(binding.fotoPortada)
                } catch (e: Exception) {
                    println(e)
                }
            }
        }


    }


    private fun subirImagenStorage(uri: Uri, nombreimg: String) {
        val rutaimg = "${Variables.usuarios_db}/${firebaseAuth.uid.toString()}/$nombreimg"
        val contentResolver = mContex.contentResolver
        GlobalScope.launch(Dispatchers.Main) {
            val imagenComprimida = constantesImagenes.resizeAndCompressImage(
                contentResolver,
                uri,
                1000,
                1000
            )
            constantesImagenes.refereciaStorage(rutaimg, imagenComprimida, nombreimg, mContex)
        }
    }

    private fun obtenerPerfil(
        idUsuario: String,
        cartel: ImageView,
        collection: String,
        documento: String,
        collection2: String,
    ) {
        val refStorage = FirebaseStorage.getInstance().reference
        val referenciaImagen = refStorage.child("${Variables.usuarios_db}/$idUsuario/$imgperfil")

        referenciaImagen.downloadUrl.addOnSuccessListener { uri ->
            val urlImg = uri.toString()

            val referenciaDB = FirebaseFirestore.getInstance()
            val documentUsuario =
                referenciaDB.collection(collection).document(documento).collection(collection2)
                    .document(idUsuario)

            documentUsuario.get().addOnSuccessListener { documentSnapshot ->
                if (documentSnapshot.exists()) {
                    val updateData = hashMapOf<String, Any>(
                        Variables.imagenPerfil to urlImg
                    )
                    documentUsuario.update(updateData)
                        .addOnSuccessListener {
                            println("Campo imagenPerfil actualizado correctamente en Firestore")
                        }
                        .addOnFailureListener { e ->
                            println("Error al actualizar campo imagenPerfil en Firestore: $e")
                        }

                    try {
                        Glide.with(mContex)
                            .load(urlImg)
                            .into(cartel)
                    } catch (e: Exception) {
                        println(e)
                    }
                }
            }.addOnFailureListener { e ->
                println("Error al obtener el documento del usuario: $e")
            }
        }.addOnFailureListener { e ->
            println("Error al obtener la URL de la imagen: $e")
        }
    }

}