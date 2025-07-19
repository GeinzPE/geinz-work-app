package com.geinzz.geinzwork.vistaTiendas

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.preference.PreferenceManager
import android.view.LayoutInflater
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.GridLayoutManager
import com.geinzz.geinzwork.R
import com.geinzz.geinzwork.utils.constantes.constantes.constantesNoticias.bottomSheet
import com.geinzz.geinzwork.utils.constantes.constantes.constantesSubcategoriaszonasTiendas
import com.geinzz.geinzwork.databinding.ActivityVistaMostrarTodosTiendasBinding
import com.geinzz.geinzwork.databinding.CustomDialogTiendasFiltradoBinding
import com.geinzz.geinzwork.model.dataclass_item_general_tienda
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.firestore.FirebaseFirestore
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

class Vista_mostrarTodos_Tiendas : AppCompatActivity() {
    private lateinit var binding: ActivityVistaMostrarTodosTiendasBinding
    private var lista = mutableListOf<String>()
    private val tiendas = mutableListOf<dataclass_item_general_tienda>()
    private lateinit var dialog: BottomSheetDialog
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var bindingBottomShet: CustomDialogTiendasFiltradoBinding
    private lateinit var direccion: EditText
    private lateinit var latitudUser: TextView
    private lateinit var longitudUser: TextView
    private val LOCATION_PERMISSION_REQUEST_CODE = 1
    private val KEYZONA = "KEY_ZONA"
    private val KEYESTRELLAS = "KEY_ESTRELLAS"
    private val KEYSEGUIDORES = "KEY_SEGUIDORES"
    private val KEYESTADO = "KEY_ESTADO"
    private val KEYTIPOTIENDA = "KEY_TIPO"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityVistaMostrarTodosTiendasBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        val categoriaSeleccionada = intent.getStringExtra("cat").toString()
        val localidadSeleccionada = intent.getStringExtra("localidad").toString()
        val categoriavermasObtenida = intent.getStringExtra("categoriaPasada")
        val localidadInicio = intent.getStringExtra("localidadInicio").toString()
        constantesSubcategoriaszonasTiendas.obtenerySetearCat(lista, categoriaSeleccionada)
        val pref = PreferenceManager.getDefaultSharedPreferences(this)
        val zonakey = pref?.getString(KEYZONA, "Default Value")
        val estrellakey = pref?.getString(KEYESTRELLAS, "Default Value")
        val seguidoreskey = pref?.getString(KEYSEGUIDORES, "Default Value")
        val keyestado = pref?.getString(KEYESTADO, "Default Value")
        binding.filtrado.setOnClickListener {
            dialog = BottomSheetDialog(this)
            bottomDaialog_filtrado(this@Vista_mostrarTodos_Tiendas, pref, localidadSeleccionada)
            dialog.show()
        }
        if (categoriavermasObtenida.isNullOrEmpty()) {
            obtenrTiendasGeneral(categoriaSeleccionada, localidadSeleccionada)
        } else {
            Toast.makeText(this, "no se obtuvo valores nomrales", Toast.LENGTH_SHORT).show()
            filtrarResultaos(
                localidadInicio,
                zonakey!!,
                estrellakey!!,
                seguidoreskey!!,
                keyestado!!,
                categoriavermasObtenida
            )
            binding.filtradoslineal.isVisible = false
        }

        binding.Todos.setOnClickListener {
            obtenrTiendasGeneral(categoriaSeleccionada, localidadSeleccionada)
        }


    }


    private fun agregarCategorias(autoCompleteTextView: AutoCompleteTextView) {
        val adapter = ArrayAdapter(
            autoCompleteTextView.context,
            android.R.layout.simple_dropdown_item_1line,
            lista
        )
        autoCompleteTextView.setAdapter(adapter)
    }


    private fun obtenrTiendasGeneral(categoriaPasada: String, localidadPasada: String) {
        val Firestore = FirebaseFirestore.getInstance().collection("Tiendas")
        Firestore.get()
            .addOnSuccessListener { res ->
                tiendas.clear()
                for (document in res) {
                    val userData = document.data
                    val imgPerfil = userData["imgPerfil"] as? String
                    val imgPortada = userData["imgPortada"] as? String
                    val nombreTienda = userData["nombre"] as? String
                    val subcategorias = userData["subcategorias"] as? String
                    val calificacion = userData["estrellas"] as? String
                    val estado = userData["estado"] as? String
                    val zona = userData["zona"] as? String
                    val id = userData["id"] as? String
                    val ubicacion = userData["ubicacion"] as? String
                    val seguidores = userData["seguidores"] as? String
                    val localidad = userData["localidad"] as? String
                    val latitud = userData["latitud"] as? Double
                    val longitud = userData["longitud"] as? Double
                    val TipoTienda = userData["tipoTienda"] as? String
                    val categoria = userData["categoria"] as? String
                    val tiendaContenido = dataclass_item_general_tienda(
                        imgPerfil,
                        imgPortada,
                        categoria,
                        nombreTienda,
                        calificacion,
                        estado,
                        zona,
                        id,
                        ubicacion,
                        seguidores,
                        localidad,
                        latitud, longitud, TipoTienda,subcategorias
                    )
                    if (categoria == categoriaPasada && localidad == localidadPasada) {
                        tiendas.add(tiendaContenido)
                    }
                }
                constantesVistaTiendas.inicializarRecicleFiltados(
                    binding.reciclTiendas,
                    tiendas,
                    GridLayoutManager(this, 2),
                    this
                )

            }
    }

    @SuppressLint("MissingInflatedId")
    fun bottomDaialog_filtrado(context: Context, pref: SharedPreferences?, filtradoZona: String) {
        bindingBottomShet = CustomDialogTiendasFiltradoBinding.inflate(LayoutInflater.from(context))
        val view = bindingBottomShet.root
        bottomSheet = bindingBottomShet.cerrar
        bottomSheet.setOnClickListener {
            dialog.dismiss()
        }


        val btnApply = bindingBottomShet.btnApply
        val btnCancel = bindingBottomShet.btnCancel
        val rgStars = bindingBottomShet.GurpoEstrellas
        val seguidoresCantida = bindingBottomShet.GurpoSeguidores
        val rgStatus = bindingBottomShet.GrupoEstado
        val autoCompleteCategory = bindingBottomShet.categoria
        val autoCompleteZone = bindingBottomShet.zona
        val FiltradoLocalidad = bindingBottomShet.FiltradoLocalidad
        val RadioTipoTienda = bindingBottomShet.tipoTienda


        //obtenerDireccion
        val linalLocalizacion = bindingBottomShet.linealubiActual
        val obtenerLocalizacion = bindingBottomShet.obtenerLocalizacion
        direccion = bindingBottomShet.direccion
        latitudUser = bindingBottomShet.latitudUSer
        longitudUser = bindingBottomShet.longituduser
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        //radiobutons
        val masEstrellas = bindingBottomShet.masEstrellas
        val TodosEstrellas = bindingBottomShet.TodosEstrellas
        val masSeguidores = bindingBottomShet.masSeguidores
        val TodosSeguidores = bindingBottomShet.TodosSeguidores
        val AbiertoEstado = bindingBottomShet.AbiertoEstado
        val CerradoEstado = bindingBottomShet.CerradoEstado


        val filtroZonaKey = pref?.getString(KEYZONA, "")
        val filtradoEstrellaskey = pref?.getString(KEYESTRELLAS, "")
        val filtradoseguidoreskey = pref?.getString(KEYSEGUIDORES, "")
        val filtradoporEstadokey = pref?.getString(KEYESTADO, "")

        FiltradoLocalidad.text = filtradoZona

        obtenerLocalizacion.setOnClickListener {
            getLocation()
        }

        when (filtradoEstrellaskey) {
            "Más estrellas" -> masEstrellas.isChecked = true
            "Todos" -> TodosEstrellas.isChecked = true
        }

        when (filtradoseguidoreskey) {
            "Más Seguidores" -> masSeguidores.isChecked = true
            "Todos" -> TodosSeguidores.isChecked = true
        }

        when (filtradoporEstadokey) {
            "Abierto" -> AbiertoEstado.isChecked = true
            "Cerrado" -> CerradoEstado.isChecked = true
        }

        autoCompleteZone.setText(filtroZonaKey, false)


        val defaultzona = filtroZonaKey
        autoCompleteZone.setText(defaultzona, false)

        agregarCategorias(autoCompleteCategory)
        constantesSubcategoriaszonasTiendas.agregarZonas(
            autoCompleteZone,
            context,
            linalLocalizacion
        )

        btnApply.setOnClickListener {

            val subcategoriaSeleccionada = autoCompleteCategory.text.toString()
            val localidadPasada = FiltradoLocalidad.text.toString()
            val cantidaddeEstrellasSeleccionadas = when (rgStars.checkedRadioButtonId) {
                R.id.masEstrellas -> "Más estrellas"
                R.id.TodosEstrellas -> "Todos"
                else -> ""
            }
            val cantidadSeguidoresSeleccionado = when (seguidoresCantida.checkedRadioButtonId) {
                R.id.masSeguidores -> "Más Seguidores"
                R.id.TodosSeguidores -> "Todos"
                else -> ""
            }
            val estadoSeleccionado = when (rgStatus.checkedRadioButtonId) {
                R.id.AbiertoEstado -> "abierto"
                R.id.CerradoEstado -> "cerrado"
                else -> ""
            }
            var tipoTiendaSelecionado = when (RadioTipoTienda.checkedRadioButtonId) {
                R.id.TiendaVirtual -> "virtual"
                R.id.TiendaFisica -> "fisica"
                else -> ""
            }

            // Guardar en SharedPreferences
            guardarShaderPref(KEYTIPOTIENDA, pref, tipoTiendaSelecionado)
            guardarShaderPref(KEYSEGUIDORES, pref, cantidadSeguidoresSeleccionado)
            guardarShaderPref(KEYESTRELLAS, pref, cantidaddeEstrellasSeleccionadas)
            guardarShaderPref(KEYESTADO, pref, estadoSeleccionado)

            if (autoCompleteZone.text.toString() == "Cercanos a ti (BETA)" && direccion.text.toString().isEmpty()) {
                Toast.makeText(context, "Debes obtener tu dirección actual", Toast.LENGTH_SHORT).show()
            } else if (subcategoriaSeleccionada.isEmpty() || estadoSeleccionado.isEmpty()) {
                Toast.makeText(context, "Selecciona campos para filtrar", Toast.LENGTH_SHORT).show()
            } else {

                val db = FirebaseFirestore.getInstance().collection("Tiendas")
                db.get()
                    .addOnSuccessListener { res ->
                        val tiendasFiltradas = mutableListOf<dataclass_item_general_tienda>()

                        for (document in res) {
                            val userData = document.data
                            val imgPerfil = userData["imgPerfil"] as? String
                            val imgPortada = userData["imgPortada"] as? String
                            val nombreTienda = userData["nombre"] as? String
                            val subcategorias = userData["subcategorias"] as? String
                            val calificacion = userData["estrellas"] as? String
                            val estado = userData["estado"] as? String
                            val zona = userData["zona"] as? String
                            val id = userData["id"] as? String
                            val ubicacion = userData["ubicacion"] as? String
                            val seguidores = userData["seguidores"] as? String
                            val localidad = userData["localidad"] as? String
                            val latitud = userData["latitud"] as? Double
                            val longitud = userData["longitud"] as? Double
                            val TipoTienda = userData["tipoTienda"] as? String
                            val categoria = userData["categoria"] as? String

                            val tiendaContenido = dataclass_item_general_tienda(
                                imgPerfil,
                                imgPortada,
                                categoria,
                                nombreTienda,
                                calificacion,
                                estado,
                                zona,
                                id,
                                ubicacion,
                                seguidores,
                                localidad,
                                latitud,
                                longitud,
                                TipoTienda,
                                subcategorias
                            )

                            // Filtrado de condiciones
                            val isLocalidadMatched = localidadPasada == tiendaContenido.localidad
                            val isCategoriaMatched = subcategoriaSeleccionada == tiendaContenido.subcategoria
                            val isCercanosATi = autoCompleteZone.text.toString() == "Cercanos a ti (BETA)"
                            val isTiendaFisica = tipoTiendaSelecionado == tiendaContenido.TipoTienda
                            val isEstadoSeleccionado = estadoSeleccionado == tiendaContenido.estado
                            val isSeguidoresMatched = when (cantidadSeguidoresSeleccionado) {
                                "Más Seguidores" -> (tiendaContenido.seguidores?.toInt() ?: 0) > 20
                                "Todos" -> true // Para "Todos" siempre es verdadero
                                else -> false
                            }
                            val isMasEstrellas = when (cantidaddeEstrellasSeleccionadas) {
                                "Más estrellas" -> (tiendaContenido.estrellas?.toInt() ?: 0) > 20
                                "Todos" -> true // Para "Todos" siempre es verdadero
                                else -> false
                            }

                            // Aplicar filtros
                            if (isLocalidadMatched && isCategoriaMatched &&
                                isCercanosATi && isTiendaFisica && isEstadoSeleccionado
                                && isSeguidoresMatched && isMasEstrellas) {
                                val encontrado = findNearbyPlaces(
                                    latitudUser.text.toString().toDouble(),
                                    longitudUser.text.toString().toDouble(),
                                    tiendas // Usar la lista original para buscar cercanos
                                )
                                tiendasFiltradas.addAll(encontrado)
                            } else if (isLocalidadMatched && isCategoriaMatched &&
                                isTiendaFisica && isEstadoSeleccionado && isSeguidoresMatched
                                && isMasEstrellas && autoCompleteZone.text.toString() == tiendaContenido.zona) {
                                tiendasFiltradas.add(tiendaContenido)
                            }
                        }

                        if (tiendas.isEmpty()) {
                            Toast.makeText(
                                context,
                                "No se encontraron resultados",
                                Toast.LENGTH_SHORT
                            ).show()
                        }

                        constantesVistaTiendas.inicializarRecicleFiltados(
                            binding.reciclTiendas,
                            tiendas,
                            GridLayoutManager(context, 2),
                            context
                        )
                    }
                    .addOnFailureListener {
                        Toast.makeText(context, "Error al obtener los datos", Toast.LENGTH_SHORT).show()
                    }

                dialog.dismiss()
            }
        }





        dialog.setContentView(view)

        btnCancel.setOnClickListener {
            dialog.dismiss()
        }

    }

    private fun guardarShaderPref(KEY: String, pref: SharedPreferences?, valor: String) {
        val editor = pref?.edit()
        editor?.putString(KEY, valor)
        editor?.apply()
    }


    private fun filtrarResultaos(
        localidadPasada: String,
        keyZona: String,
        keyEstrellas: String,
        keySeguidores: String,
        keyEstado: String,
        categoriaSelecionada: String,
    ) {
        tiendas.clear()
        val db = FirebaseFirestore.getInstance().collection("Tiendas")
        db.get()
            .addOnSuccessListener { res ->
                for (document in res) {
                    val userData = document.data
                    val imgPerfil = userData["imgPerfil"] as? String
                    val imgPortada = userData["imgPortada"] as? String
                    val nombreTienda = userData["nombre"] as? String
                    val subcategorias = userData["subcategorias"] as? String
                    val calificacion = userData["estrellas"] as? String
                    val estado = userData["estado"] as? String
                    val zona = userData["zona"] as? String
                    val id = userData["id"] as? String
                    val ubicacion = userData["ubicacion"] as? String
                    val seguidores = userData["seguidores"] as? String
                    val localidad = userData["localidad"] as? String
                    val latitud = userData["latitud"] as? Double
                    val longitud = userData["longitud"] as? Double
                    val TipoTienda = userData["tipoTienda"] as? String
                    val categoria = userData["categoria"] as? String
                    val tiendaContenido = dataclass_item_general_tienda(
                        imgPerfil,
                        imgPortada,
                        categoria,
                        nombreTienda,
                        calificacion,
                        estado,
                        zona,
                        id,
                        ubicacion,
                        seguidores,
                        localidad,
                        latitud, longitud, TipoTienda,subcategorias
                    )

                    println(
                        "Comparando obtenicion de datos : localidadPasadasdsdsdd=$localidadPasada, localidad=$localidad, " +
                                "categoriaSeleccionada=$categoriaSelecionada, categoria=$categoria"
                    )

                    if (localidadPasada == localidad && categoriaSelecionada == categoria) {
                        tiendas.add(tiendaContenido)
                    }
                    constantesVistaTiendas.inicializarRecicleFiltados(
                        binding.reciclTiendas,
                        tiendas,
                        GridLayoutManager(this, 2),
                        this
                    )
                }
            }
    }

    private fun getLocation() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
            return
        }

        fusedLocationClient.lastLocation
            .addOnSuccessListener { location: Location? ->
                if (location != null) {
                    val latitude = location.latitude
                    val longitude = location.longitude
                    direccion.setText("$latitude,$longitude")
                    latitudUser.text = latitude.toString()
                    longitudUser.text = longitude.toString()

                } else {
                    Toast.makeText(
                        this,
                        "Active su ubicacion en el dispositivo",
                        Toast.LENGTH_SHORT
                    )
                        .show()
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(
                    this,
                    "Error al obtener la ubicación: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getLocation()
            } else {
                Toast.makeText(this, "Permiso de ubicación denegado.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun haversine(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val R = 6371e3 // Radio de la Tierra en metros
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = sin(dLat / 2) * sin(dLat / 2) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                sin(dLon / 2) * sin(dLon / 2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return R * c // Distancia en metros
    }

    fun findNearbyPlaces(
        latitude: Double,
        longitude: Double,
        places: List<dataclass_item_general_tienda>,
        radius: Double = 700.0
    ): List<dataclass_item_general_tienda> {
        println("Obtenemos la lista places: $places")
        val nearbyPlaces = places.filter { place ->
            if (place.latitude != null && place.longitud != null) {
                val distance = haversine(
                    latitude,
                    longitude,
                    place.latitude,
                    place.longitud
                )
                if (distance <= radius) {
                    println("Tienda: ${place.nombreTienda}, Distancia: $distance metros")
                    true // Incluir la tienda en la lista filtrada
                } else {
                    false // No incluir la tienda en la lista filtrada
                }
            } else {
                false // No incluir la tienda si falta latitud o longitud
            }
        }

        if (nearbyPlaces.isEmpty()) {
            println("No se encontraron tiendas dentro del radio de $radius metros.")
        }

        return nearbyPlaces
    }


}