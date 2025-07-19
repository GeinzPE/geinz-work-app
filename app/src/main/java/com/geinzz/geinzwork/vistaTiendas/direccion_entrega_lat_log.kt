package com.geinzz.geinzwork.vistaTiendas

import android.Manifest
import android.content.Context
import android.content.Intent
import android.provider.Settings
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import com.geinzz.geinzwork.ui.adapters.adapter_direccion_lat_log
import com.geinzz.geinzwork.model.dataClass_ubicacion_user
import com.geinzz.geinzwork.model.dataclas_direcion_lat_log
import com.geinzz.geinzwork.R
import com.geinzz.geinzwork.utils.constantes.constantes.mostrarFechaDialog_horaDialog
import com.geinzz.geinzwork.databinding.ActivityDireccionEntregaLatLogBinding
import com.geinzz.geinzwork.databinding.BottomSheetEditarElimaarDireccionBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions

class direccion_entrega_lat_log : AppCompatActivity() {
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var binding: ActivityDireccionEntregaLatLogBinding
    private lateinit var dialog: BottomSheetDialog
    private val ubicaciones_lista = mutableListOf<dataclas_direcion_lat_log>()
    private lateinit var firebaseAuth: FirebaseAuth
    private val LOCATION_PERMISSION_REQUEST_CODE = 1
    private val handler = Handler(Looper.getMainLooper())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDireccionEntregaLatLogBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        firebaseAuth = FirebaseAuth.getInstance()
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        confSwipe()
        mostrarDialog(binding.nombreColecciones, binding.infoCasa, binding.infoRef)
        val nombreColeccion = binding.nombreColeccionED
        obtenerUbicaciones(firebaseAuth.uid.toString())
        binding.obtenerLocalizacion.setOnClickListener {
            binding.cargandoLatLog.isVisible = true
            binding.obtenerLocalizacion.isVisible = false
            getLocation(
                binding.cargandoLatLog,
                binding.obtenerLocalizacion,
                binding.direccion,
                binding.latitudUSer,
                binding.longituduser
            ) { completado ->
                if (completado) {
                    binding.cargandoLatLog.isVisible = false
                    binding.obtenerLocalizacion.isVisible = true
                    Toast.makeText(
                        this,
                        "Ubicacion obtenida ",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    binding.cargandoLatLog.isVisible = false
                    binding.obtenerLocalizacion.isVisible = true
                    Toast.makeText(
                        this,
                        "error al obtener la ubicacion trate en un momento",
                        Toast.LENGTH_SHORT
                    ).show()

                }
            }
        }
        binding.crearButtom.setOnClickListener {
            binding.crearButtom.isVisible=false
            binding.crear.isVisible=true
            binding.containerSinUBI.isVisible = false
            binding.linealForm.isVisible = true
            nombreColeccion.isEnabled = true
        }

        binding.crear.setOnClickListener {
            val nombreColeccion = nombreColeccion.text.toString()
            val direccion = binding.direccion.text.toString()
            val direccionCasa = binding.direccionCasaED.text.toString()
            val referencia = binding.referenciaED.text.toString()

            if (binding.crear.text.toString().equals("crear", ignoreCase = true)) {
                if (nombreColeccion.isEmpty() || direccion.isEmpty() || direccionCasa.isEmpty() || referencia.isEmpty()) {
                    if (nombreColeccion.isEmpty()) binding.nombreColeccionED.error =
                        "Este campo es obligatorio"
                    if (direccion.isEmpty()) binding.direccion.error =
                        "Este campo es obligatorio"
                    if (direccionCasa.isEmpty()) binding.direccionCasaED.error =
                        "Este campo es obligatorio"
                    if (referencia.isEmpty()) binding.referenciaED.error =
                        "Este campo es obligatorio"
                } else {
                    agregaDireccionUsuario(firebaseAuth.uid.toString())
                }
            } else {
                binding.crearButtom.isVisible=false
                binding.crear.text = "Crear"
                binding.nombreColeccionED.setText("")
                binding.direccion.setText("")
                binding.direccionCasaED.setText("")
                binding.referenciaED.setText("")
                binding.latitudUSer.text = ""
                binding.longituduser.text = ""
            }
        }


    }

    private fun confSwipe() {
        binding.swipe.setOnRefreshListener {
            binding.linealCargandoDirecciones.isVisible = true
            binding.crear.isVisible = false
            binding.netScrollView.isVisible = false
            binding.swipe.isRefreshing = false
            binding.swipe.setColorSchemeResources(R.color.violeta)
            Handler(Looper.getMainLooper()).postDelayed({
                obtenerUbicaciones(firebaseAuth.uid.toString())
                binding.swipe.isRefreshing = false
                binding.netScrollView.isVisible = true
                binding.crear.isVisible = false
                binding.crearButtom.isVisible=true
            }, 2000)
        }
    }

    private fun BottomSheetEditar_eliminar(ubicacion: dataclas_direcion_lat_log) {
        val bindingBottomSheet =
            BottomSheetEditarElimaarDireccionBinding.inflate(LayoutInflater.from(this))
        val view = bindingBottomSheet.root
        val tiempoInicio = System.currentTimeMillis()
        mostrarDialog(
            bindingBottomSheet.nombreColecciones,
            bindingBottomSheet.infoCasa,
            bindingBottomSheet.infoRef
        )
        bindingBottomSheet.nombreColeccionED.setText(ubicacion.nombreRef)
        bindingBottomSheet.nombreColeccionED.isEnabled = false
        bindingBottomSheet.direccion.setText("${ubicacion.lat},${ubicacion.log}")
        bindingBottomSheet.latitudUSer.text = ubicacion.lat
        bindingBottomSheet.longituduser.text = ubicacion.log
        bindingBottomSheet.direccionCasaED.setText(ubicacion.direccion)
        bindingBottomSheet.referenciaED.setText(ubicacion.referencia)
        bindingBottomSheet.idReferencia.text = ubicacion.id.toString()
        val tiempoFin = System.currentTimeMillis()
        val tiempoTotal = tiempoFin - tiempoInicio
        handler.postDelayed({
            bindingBottomSheet.linealCarganoDatos.isVisible = false
            bindingBottomSheet.linealForm.isVisible = true
        }, tiempoTotal)

        bindingBottomSheet.eliminar.setOnClickListener {
            eliminarReferencia(
                binding.collectionEcontrado.text.toString(),
                firebaseAuth.uid.toString(),
                bindingBottomSheet.idReferencia.text.toString()
            )
        }
        bindingBottomSheet.editar.setOnClickListener {
            editar_ubicacion(
                bindingBottomSheet,
                firebaseAuth.uid.toString(),
                bindingBottomSheet.idReferencia.text.toString()
            )
        }
        bindingBottomSheet.obtenerLocalizacion.setOnClickListener {
            bindingBottomSheet.cargandoLatLog.isVisible = true
            bindingBottomSheet.obtenerLocalizacion.isVisible = false
            getLocation(
                bindingBottomSheet.cargandoLatLog,
                bindingBottomSheet.obtenerLocalizacion,
                bindingBottomSheet.direccion,
                bindingBottomSheet.latitudUSer,
                bindingBottomSheet.longituduser
            ) { completado ->
                if (completado) {
                    bindingBottomSheet.cargandoLatLog.isVisible = false
                    bindingBottomSheet.obtenerLocalizacion.isVisible = true
                    Toast.makeText(
                        this,
                        "Ubicacion obtenida ",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    bindingBottomSheet.cargandoLatLog.isVisible = false
                    bindingBottomSheet.obtenerLocalizacion.isVisible = true
                    Toast.makeText(
                        this,
                        "error al obtener la ubicacion trate en un momento",
                        Toast.LENGTH_SHORT
                    ).show()

                }
            }
        }

        dialog.setContentView(view)
    }

    private fun eliminarReferencia(coleccion_one: String, idUSer: String, documento: String) {
        dialog.dismiss()
        ocultar_datos("Eliminado Referencia...")
        val instance = FirebaseFirestore.getInstance()
        val dbTrabajador = instance.collection("Trabajadores_Usuarios_Drivers")
            .document(coleccion_one).collection(coleccion_one).document(idUSer)
            .collection("ubicacion").document(documento)

        val startTime = System.currentTimeMillis()

        dbTrabajador.delete()
            .addOnSuccessListener { res ->
                val endTime = System.currentTimeMillis()
                val duration = endTime - startTime
                mostrarDatos(duration)
                obtenerUbicaciones(idUSer)
                Toast.makeText(this, "Ubicación eliminada correctamente", Toast.LENGTH_SHORT).show()


            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error al eliminar la ubicación", Toast.LENGTH_SHORT).show()
            }
    }

    private fun editar_ubicacion(
        binding_bottomSheet: BottomSheetEditarElimaarDireccionBinding,
        id: String,
        idRefCreado: String? = null
    ) {
        val firestore = FirebaseFirestore.getInstance()
        val coleccionRaiz = firestore.collection("Trabajadores_Usuarios_Drivers")

        fun actualizarUbicacion(docTipo: String) {
            dialog.dismiss()
            val ubicacionesRef = coleccionRaiz
                .document(docTipo)
                .collection(docTipo)
                .document(id)
                .collection("ubicacion")

            ocultar_datos("Guardando cambios")
            val tiempoInicio = System.currentTimeMillis()

            ubicacionesRef.get().addOnSuccessListener { res ->
                val ubicacionEncontrada = res.firstOrNull {
                    val idReferencia = it.getString("id")
                    idReferencia == idRefCreado
                }

                if (ubicacionEncontrada != null && idRefCreado != null) {
                    val hashMap = hashMapOf<String, Any>(
                        "log" to binding_bottomSheet.longituduser.text.toString(),
                        "lat" to binding_bottomSheet.latitudUSer.text.toString(),
                        "direccion" to binding_bottomSheet.direccionCasaED.text.toString(),
                        "referencia" to binding_bottomSheet.referenciaED.text.toString()
                    )

                    ubicacionesRef.document(idRefCreado).set(hashMap, SetOptions.merge())
                        .addOnSuccessListener {
                            val tiempoTotal = System.currentTimeMillis() - tiempoInicio
                            mostrarDatos(tiempoTotal)
                            Toast.makeText(
                                this,
                                "Cambios realizados correctamente",
                                Toast.LENGTH_SHORT
                            ).show()
                            obtenerUbicaciones(firebaseAuth.uid.toString())
                        }
                        .addOnFailureListener { e ->
                            Log.e("error_actualizar", "Error al actualizar los datos: ${e.message}")
                        }
                } else {
                    Log.d("encontrado", "No se encontró una ubicación con el ID indicado")
                }
            }
        }

        // Buscar primero en trabajadores, luego en usuarios
        coleccionRaiz.document("trabajadores").collection("trabajadores").document(id)
            .get()
            .addOnSuccessListener { trabajadorDoc ->
                if (trabajadorDoc.exists()) {
                    Log.d("encontrado", "Documento encontrado en trabajadores")
                    actualizarUbicacion("trabajadores")
                } else {
                    coleccionRaiz.document("usuarios").collection("usuarios").document(id)
                        .get()
                        .addOnSuccessListener { usuarioDoc ->
                            if (usuarioDoc.exists()) {
                                Log.d("encontrado", "Documento encontrado en usuarios")
                                actualizarUbicacion("usuarios")
                            } else {
                                Log.d(
                                    "encontrado",
                                    "No se encontró el documento ni en trabajadores ni en usuarios"
                                )
                            }
                        }
                        .addOnFailureListener { e ->
                            Log.d("error", "Error al buscar en usuarios: ${e.message}")
                        }
                }
            }
            .addOnFailureListener { e ->
                Log.d("error", "Error al buscar en trabajadores: ${e.message}")
            }
    }

    private fun agregaDireccionUsuario(id: String) {
        val tiempoInicio = System.currentTimeMillis()
        val instance = FirebaseFirestore.getInstance()
        val dbTrabajador = instance.collection("Trabajadores_Usuarios_Drivers")
            .document("trabajadores").collection("trabajadores").document(id)
        val dbUsuario = instance.collection("Trabajadores_Usuarios_Drivers")
            .document("usuarios").collection("usuarios").document(id)
        var encontrado = false

        dbTrabajador.get().addOnSuccessListener { res ->
            val tiempoFin = System.currentTimeMillis()
            val tiempoTotal = tiempoFin - tiempoInicio
            if (res.exists()) {
                encontrado = true
                agregarUbicacion("trabajadores", "trabajadores", id)
                Log.d("encontrado", "Documento encontrado en trabajadores")
                mostrarDatos(tiempoTotal)

            }
            dbUsuario.get().addOnSuccessListener { res ->
                val tiempoFin = System.currentTimeMillis()
                val tiempoTotal = tiempoFin - tiempoInicio
                if (res.exists()) {
                    encontrado = true
                    agregarUbicacion("usuarios", "usuarios", id)
                    Log.d("encontrado", "Documento encontrado en usuarios")
                    mostrarDatos(tiempoTotal)
                }

                if (!encontrado) {
                    Log.d("encontrado", "Documento no encontrado en ninguna colección")
                }
            }.addOnFailureListener { e ->
                Log.d("encontrado", "Error al buscar en usuarios: ${e.message}")
            }
        }.addOnFailureListener { e ->
            Log.d("encontrado", "Error al buscar en trabajadores: ${e.message}")
        }
    }

    private fun obtenerUbicaciones(id: String) {
        ubicaciones_lista.clear()
        binding.crearButtom.isVisible=false
        binding.swipe.isVisible = false
        binding.netScrollView.isVisible = false
        binding.linealCargandoDirecciones.isVisible = true
        val tiempoInicio = System.currentTimeMillis()

        val instance = FirebaseFirestore.getInstance()
        val dbTrabajador = instance.collection("Trabajadores_Usuarios_Drivers")
            .document("trabajadores").collection("trabajadores").document(id)
        val dbUsuario = instance.collection("Trabajadores_Usuarios_Drivers")
            .document("usuarios").collection("usuarios").document(id)


        var encontrado = false

        dbTrabajador.collection("ubicacion").get().addOnSuccessListener { result ->
            if (!result.isEmpty) {
                encontrado = true

                for (document in result) {
                    val ubicacion = dataclas_direcion_lat_log(
                        document.getString("icono") ?: "",
                        document.getString("nombre") ?: "No disponible",
                        document.getString("fecha") ?: "No disponible",
                        document.getString("hora") ?: "No disponible",
                        document.getString("id") ?: "No disponible",
                        document.getString("lat") ?: "No disponible",
                        document.getString("log") ?: "No disponible",
                        document.getString("direccion") ?: "No disponible",
                        document.getString("referencia") ?: "No disponible"
                    )

                    ubicaciones_lista.add(ubicacion)
                }

                mostrarUbicaciones(ubicaciones_lista)
                binding.swipe.isVisible = true
                binding.netScrollView.isVisible = true
                binding.collectionEcontrado.text = "trabajadores"
                binding.containerSinUBI.isVisible = false
                binding.linealCargandoDirecciones.isVisible = false

                val tiempoFinal = System.currentTimeMillis()
                mostrarDatos(tiempoFinal - tiempoInicio)

            } else {
                // No encontró en trabajadores, buscar en usuarios
                dbUsuario.collection("ubicacion").get().addOnSuccessListener { result2 ->
                    if (!result2.isEmpty) {
                        encontrado = true
                        val ubicaciones = mutableListOf<dataClass_ubicacion_user>()
                        for (document in result2) {
                            val ubicacion = dataclas_direcion_lat_log(
                                document.getString("icono") ?: "",
                                document.getString("nombre") ?: "No disponible",
                                document.getString("fecha") ?: "No disponible",
                                document.getString("hora") ?: "No disponible",
                                document.getString("id") ?: "No disponible",
                                document.getString("lat") ?: "No disponible",
                                document.getString("log") ?: "No disponible",
                                document.getString("direccion") ?: "No disponible",
                                document.getString("referencia") ?: "No disponible"
                            )

                            ubicaciones_lista.add(ubicacion)
                        }

                        mostrarUbicaciones(ubicaciones_lista)
                        binding.collectionEcontrado.text = "usuarios"
                        binding.containerSinUBI.isVisible = false
                        binding.linealCargandoDirecciones.isVisible = false
                        binding.swipe.isVisible = true
                        binding.netScrollView.isVisible = true

                        val tiempoFinal = System.currentTimeMillis()
                        mostrarDatos(tiempoFinal - tiempoInicio)

                    } else {
                        // No encontró ubicaciones en ninguno
                        binding.linealCargandoDirecciones.isVisible = false
                        binding.swipe.isVisible = false
                        binding.netScrollView.isVisible = false
                        binding.containerSinUBI.isVisible = true

                        val tiempoFinal = System.currentTimeMillis()
                        mostrarDatos(tiempoFinal - tiempoInicio)
                    }
                }.addOnFailureListener { e ->
                    Log.d("econtrado", "Error al buscar en usuarios: ${e.message}")
                    binding.swipe.isVisible = false
                    binding.netScrollView.isVisible = false
                    binding.containerSinUBI.isVisible = true
                    binding.linealCargandoDirecciones.isVisible = false

                    val tiempoFinal = System.currentTimeMillis()
                    mostrarDatos(tiempoFinal - tiempoInicio)
                }
            }
        }.addOnFailureListener { e ->
            Log.d("econtrado", "Error al buscar en trabajadores: ${e.message}")
            binding.containerSinUBI.isVisible = true
            binding.linealCargandoDirecciones.isVisible = false
            binding.swipe.isVisible = false
            binding.netScrollView.isVisible = false

            val tiempoFinal = System.currentTimeMillis()
            mostrarDatos(tiempoFinal - tiempoInicio)
        }
    }


    private fun mostrarDatos(tiempo: Long) {
        handler.postDelayed({
            binding.netScrollView.isVisible = true
            binding.linealCargandoDirecciones.isVisible = false
            binding.crearButtom.isVisible=true
            binding.crear.isVisible = false
            binding.swipe.isVisible = true
        }, tiempo)
    }

    private fun ocultar_datos(texto_mostrado: String) {
        binding.crearButtom.isVisible=false
        binding.textCambiarTextoCargando.text = texto_mostrado
        binding.linealCargandoDirecciones.isVisible = true
        binding.crear.isVisible = false
        binding.netScrollView.isVisible = false
        binding.swipe.isVisible = false
    }

    private fun mostrarUbicaciones(ubicaciones: MutableList<dataclas_direcion_lat_log>) {
        if (ubicaciones.isEmpty()) {
            binding.listaUbicaciones.isVisible = false
        } else {
            val recicle = binding.listaUbicaciones
            recicle.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
            recicle.adapter = adapter_direccion_lat_log(ubicaciones) { editar_eliminar ->
                dialog = BottomSheetDialog(this)
                BottomSheetEditar_eliminar(editar_eliminar)
                dialog.show()
            }
            binding.listaUbicaciones.isVisible = true
//            val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, ubicaciones)
//            binding.listaUbicaciones.adapter = adapter
//            binding.listaUbicaciones.isVisible = true
        }
    }

    private fun mostrarDialog(
        nombreColecciones: ImageButton,
        infoCasa: ImageButton,
        infoRef: ImageButton
    ) {
        nombreColecciones.setOnClickListener {
            val builder = AlertDialog.Builder(this)
            builder.setTitle(getString(R.string.nombreCollectioTitle))
            builder.setMessage(getString(R.string.nombreCollection))
            builder.setPositiveButton(getString(R.string.dialog_positive)) { dialog, _ -> dialog.dismiss() }
            val dialog: AlertDialog = builder.create()
            dialog.show()
        }
        infoCasa.setOnClickListener {
            val builder = AlertDialog.Builder(this)
            builder.setTitle(getString(R.string.nombrerefUNOTitle))
            builder.setMessage(getString(R.string.nombrerefUNO))
            builder.setPositiveButton(getString(R.string.dialog_positive)) { dialog, _ -> dialog.dismiss() }
            val dialog: AlertDialog = builder.create()
            dialog.show()
        }

        infoRef.setOnClickListener {
            val builder = AlertDialog.Builder(this)
            builder.setTitle(getString(R.string.nombrerefDOSTitle))
            builder.setMessage(getString(R.string.nombrerefDOS))
            builder.setPositiveButton(getString(R.string.dialog_positive)) { dialog, _ -> dialog.dismiss() }
            val dialog: AlertDialog = builder.create()
            dialog.show()
        }
    }

    private fun agregarUbicacion(
        doc1: String,
        doc2: String,
        id: String,
    ) {
        val db = FirebaseFirestore.getInstance()
            .collection("Trabajadores_Usuarios_Drivers")
            .document(doc1)
            .collection(doc2)
            .document(id)
            .collection("ubicacion")


        ocultar_datos("Creando direccion")
        val hashMap = hashMapOf<String, Any>(
            "nombre" to binding.nombreColeccionED.text.toString(),
            "log" to binding.longituduser.text.toString(),
            "lat" to binding.latitudUSer.text.toString(),
            "direccion" to binding.direccionCasaED.text.toString(),
            "referencia" to binding.referenciaED.text.toString(),
            "hora" to mostrarFechaDialog_horaDialog.obtenerHoraActual(),
            "fecha" to mostrarFechaDialog_horaDialog.obtenerFechaActual()
        )
        val tiempoInicio = System.currentTimeMillis()
        db.add(hashMap)
            .addOnSuccessListener { documentReference ->
                // Ahora actualizamos el documento con su ID generado
                val tiempoFin = System.currentTimeMillis()
                val tiempoTotal = tiempoFin - tiempoInicio
                mostrarDatos(tiempoTotal)
                documentReference.update("id", documentReference.id)
                    .addOnSuccessListener {
                        Toast.makeText(
                            this,
                            "Ubicación agregada correctamente ",
                            Toast.LENGTH_SHORT
                        ).show()
                        binding.crear.text = "Crear nuevo"
                    }

                    .addOnFailureListener { e ->
                        Log.d("errroref", "Error al actualizar el ID: ${e.message}")
                    }

                obtenerUbicaciones(firebaseAuth.uid.toString())
                binding.nombreColeccionED.setText("")
                binding.direccionCasaED.setText("")
                binding.referenciaED.setText("")
                binding.direccion.setText("")
                binding.linealForm.isVisible = false
            }
            .addOnFailureListener { e ->
                Toast.makeText(
                    this,
                    "Error al subir la ubicación: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }

    }

    private fun getLocation(
        cargandoLatLog: ProgressBar,
        obtenerLocalizacion: ImageButton,
        direccion: EditText,
        latitudUSer: TextView,
        longituduser: TextView,
        completado: (Boolean) -> Unit
    ) {
        val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val isGpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)

        // Si el GPS está desactivado
        if (!isGpsEnabled) {
            cargandoLatLog.isVisible = false
            obtenerLocalizacion.isVisible = true

            AlertDialog.Builder(this)
                .setTitle("Ubicación desactivada")
                .setMessage("Por favor, active su ubicación para continuar.")
                .setPositiveButton("Activar") { _, _ ->
                    startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
                }
                .setNegativeButton("Cancelar") { dialog, _ ->
                    dialog.dismiss()
                    completado(false)
                }
                .show()
            return
        }

        // Verificación de permisos
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
            completado(false) // Avisar que no se pudo completar por falta de permisos
            return
        }

        // Configuración de la solicitud de ubicación
        val locationRequest = LocationRequest.create().apply {
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
            interval = 1000
            fastestInterval = 2000
            numUpdates = 1
        }

        // Callback para obtener la ubicación
        val locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                fusedLocationClient.removeLocationUpdates(this) // Evitar múltiples llamadas

                val location = locationResult.lastLocation
                if (location != null) {
                    val latitude = location.latitude
                    val longitude = location.longitude

                    direccion.setText("$latitude,$longitude")
                    latitudUSer.text = latitude.toString()
                    longituduser.text = longitude.toString()

                    completado(true)
                } else {
                    Toast.makeText(
                        this@direccion_entrega_lat_log,
                        "No se pudo obtener la ubicación",
                        Toast.LENGTH_SHORT
                    ).show()
                    completado(false)
                }

                cargandoLatLog.isVisible = false
                obtenerLocalizacion.isVisible = true
            }

        }

        // Mostrar cargando
        cargandoLatLog.isVisible = true
        obtenerLocalizacion.isVisible = false

        // Solicitar ubicación
        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            Looper.getMainLooper()
        )
    }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray,
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getLocation(
                    binding.cargandoLatLog,
                    binding.obtenerLocalizacion,
                    binding.direccion,
                    binding.latitudUSer,
                    binding.longituduser
                ) {}
            } else {
                Toast.makeText(this, "Permiso de ubicación denegado.", Toast.LENGTH_SHORT).show()
            }
        }
    }
}