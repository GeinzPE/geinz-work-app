package com.geinzz.geinzwork

import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import com.example.geinzwork.constantesGeneral.constantes_nombre_usuarios
import com.geinzz.geinzwork.constantesGeneral.constantesImagenes
import com.geinzz.geinzwork.constantesGeneral.constantestextos_general
import com.geinzz.geinzwork.constantesGeneral.mostrarFechaDialog_horaDialog
import com.geinzz.geinzwork.databinding.ActivityVeirificacionDatosBinding
import com.geinzz.geinzwork.problemas_soporte_politicas.politicas_creacion_cuenta
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class veirificacionDatos : AppCompatActivity() {
    private lateinit var binding: ActivityVeirificacionDatosBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var progressDialog: ProgressDialog
    private var img_perfil_uri: Uri? = null
    private var img_portada: Uri? = null
    private val pciMEdia =
        registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
            if (uri != null) {
                img_perfil_uri = uri
                binding.imgPerfil.setImageURI(uri)

            } else {
                println("Imagen no seleccionada")
            }
        }
    private val pickMedia_imgPortada =
        registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
            if (uri != null) {
                img_portada = uri
                binding.fotoPortada.setImageURI(uri)
            } else {
                println("Imagen no seleccionada")
            }
        }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityVeirificacionDatosBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        constantestextos_general.subrallarTexto(
            "Politicas de privacidad",
            binding.textoPoliticas
        )
        binding.textoPoliticas.setOnClickListener {
            startActivity(Intent(this, politicas_creacion_cuenta::class.java))
        }
        binding.imgPerfil.setOnClickListener {
            pciMEdia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }
        binding.overlay.setOnClickListener {
            pickMedia_imgPortada.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }

        firebaseAuth = FirebaseAuth.getInstance()
        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Espere un momento")
        progressDialog.setCanceledOnTouchOutside(false)
        val tipoCuenta = intent.getStringExtra("tipoCuenta").toString()
        val titulo = intent.getStringExtra("titulo")
        binding.freelancer.text = titulo
        when (tipoCuenta) {
            "cuentaTrabajador" -> {
                binding.linealHorario.isVisible = true
                binding.linealCaracteristicas.isVisible = true
                binding.linealCategoriaT.isVisible = true
                binding.linealTipoTrabajo.isVisible = true
                obtenerDATOSINTETN()
                binding.btnConfiramrDatos.setOnClickListener {
                    if (!binding.checkBoxPoliticas.isChecked) {
                        Toast.makeText(
                            this,
                            "Acepta nuestras políticas de privacidad",
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        verificarUsuario(tipoCuenta)
                    }

                }
                binding.TipoCuenta.text = "Cuenta Trabajador"
                binding.freelancer.text = "Cuenta Trabajador"
            }

            "cuentaSimple" -> {
                binding.linealHorario.isVisible = false
                binding.linealCaracteristicas.isVisible = false
                binding.linealCategoriaT.isVisible = false
                binding.linealTipoTrabajo.isVisible = false
                obtenerDATOSCuentaNormal()
                binding.btnConfiramrDatos.setOnClickListener {
                    if (!binding.checkBoxPoliticas.isChecked) {
                        Toast.makeText(
                            this,
                            "Acepta nuestras políticas de privacidad",
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        verificarUsuario(tipoCuenta)
                    }

                }
                binding.freelancer.text = "Cuenta Simple"
                binding.TipoCuenta.text = "Cuenta Simple"
            }
        }

    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun verificarUsuario(tipoCuenta: String) {
        progressDialog.setMessage("Registrandose en Geinz espere un momento")
        progressDialog.show()
        val contrauser = intent.getStringExtra("contra").toString()
        val email = intent.getStringExtra("correoUSer").toString()
        if (tipoCuenta == "cuentaTrabajador") {
            firebaseAuth.createUserWithEmailAndPassword(email, contrauser)
                .addOnSuccessListener {
                    leerinfoCreacionUser()
                    if (img_perfil_uri != null) {
                        subir_img_portada_perfil("trabajador")
                    }
                    if (img_portada != null) {
                        subir_img_portada("trabajador")
                    }
                    crearReview()
                }
                .addOnFailureListener { e ->
                    progressDialog.dismiss()
                    Toast.makeText(this, "Error al crear el usuario ${e}", Toast.LENGTH_SHORT)
                        .show()
                    println("Error al crear el usuario ${e}")
                }
        } else if (tipoCuenta == "cuentaSimple") {
            firebaseAuth.createUserWithEmailAndPassword(email, contrauser)
                .addOnSuccessListener {
                    if (img_perfil_uri != null) {
                        subir_img_portada_perfil("usuario")
                    }
                    CrearCueentauserNomral()
                }
                .addOnFailureListener { e ->
                    progressDialog.dismiss()
                    Toast.makeText(this, "Error al crear el usuario ${e}", Toast.LENGTH_SHORT)
                        .show()
                    println("Error al crear el usuario ${e}")
                }
        }

    }

    private var nombre = ""
    private var apellido = ""
    private var fechaNac = ""
    private var horario1 = ""
    private var horario2 = ""
    private var nacionalidad = ""
    private var descripcon = ""
    private var tipo_trabajo = ""
    private var categoria_Trabajo = ""
    private var genero = ""
    private var imgperfil = ""
    private val start = "0"
    private var localidaUser = ""
    private var codigoUser = ""
    private var numeroUSer = ""
    private var EdadActualUser = ""
    private var TipoCuenta = ""


    private fun CrearCueentauserNomral() {
        progressDialog.setMessage("creando cuenta")
        var localidad = intent.getStringExtra("localidad")
        var countryCode = intent.getStringExtra("codigo").toString().trim()
        var NumeroCelular = intent.getStringExtra("numero").toString().trim()
        var edadActualUser = intent.getStringExtra("edadActual")
        var Nombre_usuario = intent.getStringExtra("nombre_user").toString().trim()
        val tipoCuenta = binding.TipoCuenta.text.toString()
        var correo = firebaseAuth.currentUser!!.email
        var id = firebaseAuth.uid
        nombre = binding.nombreUser.text.toString().trim()
        apellido = binding.apellidoUSer.text.toString().trim()
        fechaNac = binding.fechaNaciminetoUSer.text.toString().trim()
        nacionalidad = binding.nacionnalidadUser.text.toString().trim()
        genero = binding.genero.text.toString().trim()
        localidaUser = localidad.toString()
        codigoUser = countryCode
        numeroUSer = NumeroCelular
        EdadActualUser = edadActualUser.toString().trim()
        TipoCuenta = "Cuenta Simple"

        val hashMap = HashMap<String, Any>()
        hashMap["id"] = "${id}"
        hashMap["TipoCuenta"] = "${tipoCuenta}"
        hashMap["correo"] = "${correo}"
        hashMap["nombre"] = "$nombre"
        hashMap["apellido"] = "${apellido}"
        hashMap["fechaNac"] = "${fechaNac}"
        hashMap["nacionalidad"] = "${nacionalidad}"
        hashMap["genero"] = "${genero}"
        hashMap["localidad"] = "${localidaUser}"
        hashMap["numero"] = "${numeroUSer}"
        hashMap["codigo_pais"] = "${codigoUser}"
        hashMap["imagenPerfil"] = "${imgperfil}"
        hashMap["EdadActual"] = "${EdadActualUser}"
        hashMap["TipoCuenta"] = "${TipoCuenta}"
        hashMap["fecha_creacion"] = mostrarFechaDialog_horaDialog.obtenerFechaActual()
        hashMap["Nombre_usuario"] = Nombre_usuario




        val db = FirebaseFirestore.getInstance()
        val userCollections =
            FirebaseFirestore.getInstance().collection("Trabajadores_Usuarios_Drivers")
                .document("usuarios").collection("usuarios")

        if (id != null) {
            userCollections.document(id).set(hashMap)
                .addOnSuccessListener {
                    println("Usuario creado en Firestore")
                    var vista: Intent = Intent(this, GraciasRegistro::class.java)
                    vista.putExtra("nombreUsuario", nombre)
                    startActivity(vista)
                    finishAffinity()
                    constantes_nombre_usuarios.agregar_firestoreNombre_usuario(id,Nombre_usuario,TipoCuenta)
                }
                .addOnFailureListener { e ->
                    println("Error al crear usuario en Firestore: $e")
                }
        } else {
            println("Error: ID de usuario nulo")
        }


    }



    private fun leerinfoCreacionUser() {
        progressDialog.setMessage("creando cuenta")
        val amuser = intent.getStringExtra("amUSer").toString().trim()
        val pmuser = intent.getStringExtra("pmUser").toString().trim()
        val tipoTrabajo = intent.getStringExtra("tipo").toString().trim()
        val categoriaTrabajo = intent.getStringExtra("Categoria").toString().trim()
        var localidad = intent.getStringExtra("localidad").toString().trim()
        var countryCode = intent.getStringExtra("codigo").toString().trim()
        var NumeroCelular = intent.getStringExtra("numero").toString().trim()
        var edadActualUser = intent.getStringExtra("edadActual").toString().trim()
        var descripcion = intent.getStringExtra("descripcion").toString().trim()
        var Nombre_usuario = intent.getStringExtra("nombre_user").toString().trim()
        val tipoCuenta = binding.TipoCuenta.text.toString()
        var correo = firebaseAuth.currentUser!!.email
        var id = firebaseAuth.uid
        nombre = binding.nombreUser.text.toString().trim()
        apellido = binding.apellidoUSer.text.toString().trim()
        fechaNac = binding.fechaNaciminetoUSer.text.toString().trim()
        horario2 = amuser
        horario1 = pmuser
        descripcon = descripcion
        tipo_trabajo = tipoTrabajo
        categoria_Trabajo = categoriaTrabajo
        nacionalidad = binding.nacionnalidadUser.text.toString().trim()
        genero = binding.genero.text.toString().trim()
        localidaUser = localidad
        codigoUser = countryCode
        numeroUSer = NumeroCelular
        EdadActualUser = edadActualUser
        TipoCuenta = "Cuenta Trabajador"
        Log.d("timeFreP", pmuser)
        val hashMap = HashMap<String, Any>()
        hashMap["id"] = "${id}"
        hashMap["TipoCuenta"] = "${tipoCuenta}"
        hashMap["correo"] = "${correo}"
        hashMap["nombre"] = "$nombre"
        hashMap["apellido"] = "${apellido}"
        hashMap["fechaNac"] = "${fechaNac}"
        hashMap["horario2"] = "${pmuser}"
        hashMap["horario1"] = "${amuser}"
        hashMap["nacionalidad"] = "${nacionalidad}"
        hashMap["descripcion"] = "${descripcon}"
        hashMap["tipoTrabajo"] = "${tipo_trabajo}"
        hashMap["categoriaTrabajo"] = "${categoria_Trabajo}"
        hashMap["genero"] = "${genero}"
        hashMap["localidad"] = "${localidaUser}"
        hashMap["numero"] = "${numeroUSer}"
        hashMap["codigo_pais"] = "${codigoUser}"
        hashMap["imagenPerfil"] = "${imgperfil}"
        hashMap["estrellas"] = "${start}"
        hashMap["EdadActual"] = "${EdadActualUser}"
        hashMap["TipoCuenta"] = "${TipoCuenta}"
        hashMap["fecha_creacion"] = mostrarFechaDialog_horaDialog.obtenerFechaActual()
        hashMap["Nombre_usuario"] = Nombre_usuario


        val db = FirebaseFirestore.getInstance()
        val userCollections =
            db.collection("Trabajadores_Usuarios_Drivers").document("trabajadores")
                .collection("trabajadores")
        if (id != null) {
            userCollections.document(id).set(hashMap)
                .addOnSuccessListener {
                    println("Usuario creado en Firestore")
                    var vista: Intent = Intent(this, GraciasRegistro::class.java)
                    vista.putExtra("nombreUsuario", nombre)
                    startActivity(vista)
                    finishAffinity()
                    constantes_nombre_usuarios.agregar_firestoreNombre_usuario(id,Nombre_usuario,TipoCuenta)
                }
                .addOnFailureListener { e ->
                    println("Error al crear usuario en Firestore: $e")
                }
        } else {
            println("Error: ID de usuario nulo")
        }
    }

    private fun crearReview() {
        val idUsuario = firebaseAuth.uid ?: return
        val db = FirebaseDatabase.getInstance().getReference("ReseñasUsuarios")
            .child(idUsuario).setValue("null")
            .addOnSuccessListener {
                println("review creada exitosamente")
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error al crear la review: $e", Toast.LENGTH_SHORT).show()
            }
    }

    private fun obtenerDATOSINTETN() {
        var descripcion = intent.getStringExtra("descripcion").toString().trim()
        val nombreuser = intent.getStringExtra("nombre")
        val apellidouser = intent.getStringExtra("apellidoUser")
        val amuser = intent.getStringExtra("amUSer")
        val pmuser = intent.getStringExtra("pmUser")
        val fechaNaciminetouser = intent.getStringExtra("fechanaciminetoUSer")
        val nacionalidaduser = intent.getStringExtra("nacionalidadUSer")
        val correouser = intent.getStringExtra("correoUSer")
        val categoria = intent.getStringExtra("Categoria")
        val tipo_Trabajo = intent.getStringExtra("tipo")
        val genero = intent.getStringExtra("genero")
        var localidad = intent.getStringExtra("localidad")
        var countryCode = intent.getStringExtra("codigo").toString().trim()
        var NumeroCelular = intent.getStringExtra("numero")
        var edadActualUser = intent.getStringExtra("edadActual")


        binding.nombreUser.text = (nombreuser)
        binding.apellidoUSer.text = (apellidouser)
        binding.fechaNaciminetoUSer.text = (fechaNaciminetouser)
        binding.horarioUser.text = ("${amuser} a ${pmuser.toString().trim()}")
        binding.nacionnalidadUser.text = (nacionalidaduser)
        binding.carracteristicasUser.text = descripcion
        binding.CorreoUsuario.text = (correouser)
        binding.cat.text = (categoria)
        binding.tipoTrabajores.text = (tipo_Trabajo)
        binding.genero.text = (genero)
        binding.LocaludaUser.text = (localidad)
        binding.NumeroTelf.text = ("+$countryCode $NumeroCelular")
        binding.edadUser.text = ("${edadActualUser} años")

    }

    private fun obtenerDATOSCuentaNormal() {
        binding.overlay.setOnClickListener {
            Toast.makeText(
                this,
                "Las cuentas normales no pueden subir fotos de portada",
                Toast.LENGTH_SHORT
            ).show()
        }
        val nombreuser = intent.getStringExtra("nombre")
        val apellidouser = intent.getStringExtra("apellidoUser")
        val fechaNaciminetouser = intent.getStringExtra("fechanaciminetoUSer")
        val nacionalidaduser = intent.getStringExtra("nacionalidadUSer")
        val correouser = intent.getStringExtra("correoUSer")
        val genero = intent.getStringExtra("genero")
        var localidad = intent.getStringExtra("localidad")
        var countryCode = intent.getStringExtra("codigo").toString().trim()
        var NumeroCelular = intent.getStringExtra("numero")
        var edadActualUser = intent.getStringExtra("edadActual")

        binding.nombreUser.text = nombreuser
        binding.apellidoUSer.text = apellidouser
        binding.fechaNaciminetoUSer.text = (fechaNaciminetouser)
        binding.nacionnalidadUser.text = (nacionalidaduser)
        binding.CorreoUsuario.text = (correouser)
        binding.genero.text = (genero)
        binding.LocaludaUser.text = (localidad)
        binding.NumeroTelf.text = ("+$countryCode $NumeroCelular")
        binding.edadUser.text = ("${edadActualUser} años")

    }

    private fun subir_img_portada_perfil(tipo: String) {
        val rutaBase = "usuarios/${firebaseAuth.uid.toString()}/ImagenPerfl"
        val contentResolver = this.contentResolver
        val imgUri = img_perfil_uri

        if (imgUri == null) {
            println("No se ha seleccionado ninguna imagen")
            return
        }

        val (dbPath, storagePath) = when (tipo) {
            "trabajador" -> Pair(
                "Trabajadores_Usuarios_Drivers/trabajadores/trabajadores/${firebaseAuth.uid}",
                rutaBase
            )

            "usuario" -> Pair(
                "Trabajadores_Usuarios_Drivers/usuarios/usuarios/${firebaseAuth.uid}",
                rutaBase
            )

            else -> return
        }

        val db = FirebaseFirestore.getInstance().document(dbPath)
        val ref = FirebaseStorage.getInstance().getReference(storagePath)

        GlobalScope.launch(Dispatchers.Main) {
            try {
                val imagenComprimida = constantesImagenes.resizeAndCompressImage(
                    contentResolver,
                    imgUri,
                    700,
                    700
                )
                ref.putBytes(imagenComprimida).await()
                val urlimg = ref.downloadUrl.await().toString()

                val hashMap = hashMapOf<String, Any>(
                    "ImagenPerfil" to urlimg
                )
                db.set(hashMap, SetOptions.merge()).await()
                println("Imagen subida correctamente")
            } catch (e: Exception) {
                println("Error al subir la imagen: ${e.message}")
            }
        }
    }

    private fun subir_img_portada(tipo: String) {
        var ruta1 = "foto_portada"
        when (tipo) {
            "trabajador" -> {
                val db2 =
                    FirebaseFirestore.getInstance().collection("Trabajadores_Usuarios_Drivers")
                        .document("trabajadores").collection("trabajadores")
                        .document(firebaseAuth.uid.toString())
                val rutaimg = "usuarios/${firebaseAuth.uid.toString()}/$ruta1"
                val contentResolver = this.contentResolver
                GlobalScope.launch(Dispatchers.Main) {
                    val imagenComprimida = constantesImagenes.resizeAndCompressImage(
                        contentResolver,
                        img_portada!!,
                        700,
                        700
                    ) // Ajustar el tamaño máximo según sea necesario
                    val ref = FirebaseStorage.getInstance().getReference(rutaimg)
                    ref.putBytes(imagenComprimida)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                ref.downloadUrl.addOnSuccessListener { res ->
                                    val urlimg = res.toString()
                                    Toast.makeText(
                                        this@veirificacionDatos,
                                        "Imagen subida correctamente storage2",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    val hashMap = hashMapOf<String, Any>(
                                        ruta1 to urlimg
                                    )
                                    db2.set(hashMap, SetOptions.merge())
                                        .addOnSuccessListener {
                                            Toast.makeText(
                                                this@veirificacionDatos,
                                                "Imagen de portada correctamente firesotre2",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                }.addOnFailureListener { e ->
                                    Toast.makeText(
                                        this@veirificacionDatos,
                                        "Error al obtener la URL de la imagen:2 $e",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    println("Error al obtener la URL de la imagen: $e")
                                }
                            } else {
                                Toast.makeText(
                                    this@veirificacionDatos,
                                    "Error al subir la imagen: ${task.exception}",
                                    Toast.LENGTH_SHORT
                                ).show()
                                println("Error al subir la imagen: ${task.exception}")
                            }
                        }
                }
            }
        }
    }


}


