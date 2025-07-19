package com.geinzz.geinzwork

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import com.geinzz.geinzwork.utils.constantes.constantes.constantes
import com.geinzz.geinzwork.utils.constantes.constantes.mostrarFechaDialog_horaDialog
import com.geinzz.geinzwork.databinding.ActivityCuentaFreelancerBinding
import com.geinzz.geinzwork.utils.constantes.hora.timePickterFracment
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.time.temporal.ChronoUnit

class CuentaFreelancer : AppCompatActivity() {
    private lateinit var binding: ActivityCuentaFreelancerBinding


    @SuppressLint("SuspiciousIndentation")
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityCuentaFreelancerBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }


        var tipoCuenta = intent.getStringExtra("tipoCuenta")
        var titulo = intent.getStringExtra("Title")
        var pasos = intent.getStringExtra("pasos")

        when (tipoCuenta) {
            "cuentaSimple" -> {
                binding.freelancer.text = titulo
                binding.EstasA.text = pasos
                binding.horarios.isVisible = false
                binding.titleHorariosDisponivilidad.isVisible = false
                binding.btnLogin.setOnClickListener {
                    verificarFreelancer("cuentaSimple") { completo ->
                        if (completo) {
                            verificarEdadGeneral("cuentaSimple")
                        }
                    }

                }

            }

            "cuentaTrabajador" -> {
                binding.freelancer.text = titulo
                binding.EstasA.text = pasos
                binding.horarios.isVisible = true
                binding.titleHorariosDisponivilidad.isVisible = true
                binding.btnLogin.setOnClickListener {
                    verificarFreelancer("cuentaTrabajador") { comlpeto ->
                        if (comlpeto) {
                            verificarEdadGeneral("cuentaTrabajador")
                        }
                    }

                }
            }
        }
        colocarNacionalidad()
        obtenerGenero()
        colocarLocalida()
        binding.numeroa1.setOnClickListener {
            showTimePicke1()
        }
        binding.numero2.setOnClickListener {
            showTimePicke2()
        }
        binding.fechaNacFre.setOnClickListener {
            mostrarFechaDialog_horaDialog.mostrarFechaDialogVista(binding.fechaNacFre, this)
        }
    }

    private fun verificarFreelancer(tipo: String, onValidationResult: (Boolean) -> Unit) {
        val nombre = binding.nombre.text.toString().trim()
        val apellido = binding.apellido.text.toString().trim()
        val fechaNacimiento = binding.fechaNacFre.text.toString().trim()
        val numero = binding.NumeroCelular.text.toString().trim()
        val nacionalidad = binding.nacionalida.text.toString().trim()
        val genero = binding.Genero.text.toString().trim()
        val localidad = binding.Localidad.text.toString().trim()

        var isValid = true
        isValid = validarCamposComunes(
            nombre,
            apellido,
            fechaNacimiento,
            numero,
            nacionalidad,
            genero,
            localidad
        )

        when (tipo) {
            "cuentaTrabajador" -> {

                val am = binding.numeroa1.text.toString().trim()
                val pm = binding.numero2.text.toString().trim()

                if (am.isEmpty()) {
                    binding.numeroa1.error = "Ingrese el valor de AM"
                    isValid = false
                }

                if (pm.isEmpty()) {
                    binding.numero2.error = "Ingrese el valor de PM"
                    isValid = false
                }

                onValidationResult(isValid)
            }

            "cuentaSimple" -> {
                onValidationResult(isValid)
            }

            else -> {
                onValidationResult(false)
            }
        }
    }

    private fun validarCamposComunes(
        nombre: String,
        apellido: String,
        fechaNacimiento: String,
        numero: String,
        nacionalidad: String,
        genero: String,
        localidad: String,
    ): Boolean {
        var isValid = true

        if (nombre.isEmpty()) {
            binding.nombre.error = "Ingrese sus nombres completos"
            isValid = false
        }

        if (apellido.isEmpty()) {
            binding.apellido.error = "Ingrese sus apellidos"
            isValid = false
        }

        if (fechaNacimiento.isEmpty()) {
            binding.fechaNacFre.error = "Ingrese su fecha de nacimiento"
            isValid = false
        }

        if (numero.isEmpty()) {
            binding.NumeroCelular.error = "Ingrese su número de celular"
            isValid = false
        } else if (numero.length != 9) {
            binding.NumeroCelular.error = "El número debe tener 9 dígitos"
            isValid = false
        }

        if (nacionalidad.isEmpty()) {
            binding.nacionalida.error = "Ingrese su nacionalidad"
            isValid = false
        }

        if (genero.isEmpty()) {
            binding.Genero.error = "Seleccione su género"
            isValid = false
        }

        if (localidad.isEmpty()) {
            binding.Localidad.error = "Ingrese su localidad"
            isValid = false
        }

        return isValid
    }


    @RequiresApi(Build.VERSION_CODES.O)
    private fun verificarEdadGeneral(TipoCuenta: String) {
        val esMayorDeEdad = verificarEdad(binding.fechaNacFre.text.toString().trim())
        if (esMayorDeEdad) {
            verificacionCampos(TipoCuenta)
        } else {
            val builder = AlertDialog.Builder(this)
            builder.setTitle("Eres menor de edad ")
            builder.setMessage("¿Deseas contactar con Geinz Work para crear una cuenta siendo menor de edad?.")
            builder.setPositiveButton("si") { dialog, which ->
                constantes.contactarWhatsapp(
                    "+51 937659216",
                    "Hola Deseo crear una cuenta en Geinz Work siendo menor de edad",
                    this
                )
            }
            builder.setNegativeButton("No") { dialog, which ->
                dialog.dismiss()
            }
            val dialog = builder.create()
            dialog.show()
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun verificarEdad(fechaNacimiento: String): Boolean {
        return try {
            val dateFormatter = DateTimeFormatter.ofPattern("d/MM/yyyy")
            val birthDate = LocalDate.parse(fechaNacimiento, dateFormatter)
            val currentDate = LocalDate.now()
            val age = ChronoUnit.YEARS.between(birthDate, currentDate)
            age >= 18

        } catch (e: DateTimeParseException) {
            e.printStackTrace()
            false
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun obtenerFechaActual(): Int {
        return LocalDate.now().year
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun calcularEdad(fechaNacimiento: String): Int? {
        return try {
            val dateFormatter = DateTimeFormatter.ofPattern("d/MM/yyyy")
            val birthDate = LocalDate.parse(fechaNacimiento, dateFormatter)
            val yearActual = obtenerFechaActual()
            val yearNacimiento = birthDate.year
            yearActual - yearNacimiento
        } catch (e: Exception) {
            e.printStackTrace()
            null // Retorna null en caso de error
        }
    }

    private fun showTimePicke2() {
        val timerPiker = timePickterFracment { onTimeSelect2(it) }
        timerPiker.show(supportFragmentManager, "time")

    }

    private fun showTimePicke1() {
        val timerPiker = timePickterFracment { ontimeSelect1(it) }
        timerPiker.show(supportFragmentManager, "time")
    }

    private fun onTimeSelect2(horaUtc: String) {
        binding.numero2.setText("${horaUtc.trim()}")
    }

    private fun ontimeSelect1(horaUtc: String) {
        binding.numeroa1.setText("${horaUtc.trim()}")

    }


    @RequiresApi(Build.VERSION_CODES.O)
    private fun verificacionCampos(tipocuenta: String) {
        var campoLocalidad = binding.Localidad.text.toString().trim()
        var NumeroCelular = binding.NumeroCelular.text.toString().trim()
        var codigoT = binding.Selector
        val countryCode = codigoT.selectedCountryCode
        val edadActualUser = calcularEdad(binding.fechaNacFre.text.toString().trim()).toString()
        when (tipocuenta) {
            "cuentaTrabajador" -> {
                mandardatosSeleccionTrabajo(
                    campoLocalidad,
                    countryCode,
                    NumeroCelular,
                    edadActualUser
                )
            }

            "cuentaSimple" -> {
                mandarDatosCrearCuentaDirecto(
                    campoLocalidad,
                    countryCode,
                    NumeroCelular,
                    edadActualUser
                )
            }
        }
    }

    private fun mandardatosSeleccionTrabajo(
        campoLocalidad: String,
        countryCode: String,
        NumeroCelular: String,
        edadActualUser: String,
    ) {
        var vista: Intent = Intent(this, Seleccion_de_Trabajo::class.java)
        vista.putExtra("tipoCuenta", "cuentaTrabajador")
        vista.putExtra("nombreUSer", binding.nombre.text.toString().trim())
        vista.putExtra("apellidoUser", binding.apellido.text.toString().trim())
        vista.putExtra("fechanaciminetoUSer", binding.fechaNacFre.text.toString().trim())
        vista.putExtra("amUSer", binding.numeroa1.text.toString().trim())
        vista.putExtra("pmUser", binding.numero2.text.toString().trim())
        vista.putExtra("nacionalidadUSer", binding.nacionalida.text.toString().trim())
        vista.putExtra("genero", binding.Genero.text.toString().trim())
        vista.putExtra("localida", campoLocalidad)
        vista.putExtra("codigo", countryCode)
        vista.putExtra("numero", NumeroCelular)
        vista.putExtra("edadActual", edadActualUser)
        startActivity(vista)
        Log.d("timeFreP", binding.numero2.text.toString().trim())
    }

    private fun mandarDatosCrearCuentaDirecto(
        campoLocalidad: String,
        countryCode: String,
        NumeroCelular: String,
        edadActualUser: String,
    ) {
        var vista: Intent = Intent(this, passwordYuser::class.java)
        var titulo = intent.getStringExtra("Title")
        vista.putExtra("tipoCuenta", "cuentaSimple")
        vista.putExtra("titulo", titulo)
        vista.putExtra("nombreUSer", binding.nombre.text.toString().trim())
        vista.putExtra("apellidoUser", binding.apellido.text.toString().trim())
        vista.putExtra("fechanaciminetoUSer", binding.fechaNacFre.text.toString().trim())
        vista.putExtra("amUSer", binding.numeroa1.text.toString().trim())
        vista.putExtra("pmUser", binding.numero2.text.toString().trim())
        vista.putExtra("nacionalidadUSer", binding.nacionalida.text.toString().trim())
        vista.putExtra("genero", binding.Genero.text.toString().trim())
        vista.putExtra("localida", campoLocalidad)
        vista.putExtra("codigo", countryCode)
        vista.putExtra("numero", NumeroCelular)
        vista.putExtra("edadActual", edadActualUser)
        startActivity(vista)
    }

    private fun obtenerGenero() {
        binding.generoid.setOnCheckedChangeListener { grupo, checkid ->
            when (checkid) {
                R.id.masculino -> {
                    binding.Genero.setText("Masculino")
                    binding.Genero.isEnabled = false
                }

                R.id.femenino -> {
                    binding.Genero.setText("Femenino")
                    binding.Genero.isEnabled = false
                }
            }
        }

    }

    private fun colocarNacionalidad() {
        binding.nacionalida.isEnabled = false
        binding.grupoNacionalidad.setOnCheckedChangeListener { grupo, checkedId ->
            when (checkedId) {
                R.id.peuano -> {
                    binding.nacionalida.setText("Peruano")
                    binding.nacionalida.isEnabled = false
                }

                R.id.venezolano -> {
                    binding.nacionalida.setText("Venezolano")
                    binding.nacionalida.isEnabled = false
                }

                R.id.otro -> {
                    binding.nacionalida.isEnabled = true
                    binding.nacionalida.setText("")
                }
            }
        }

    }

    private fun colocarLocalida() {
        binding.Localidad.isEnabled = false
        binding.grupolocalidadid.setOnCheckedChangeListener { grupo, checkid ->
            when (checkid) {
                R.id.Barranca -> {
                    binding.Localidad.setText("Barranca")
                }

                R.id.Paramonga -> {
                    binding.Localidad.setText("Paramonga")
                }

                R.id.Supe -> {
                    binding.Localidad.setText("Supe")
                }

                R.id.Pativilca -> {
                    binding.Localidad.setText("Pativilca")
                }
            }
        }
    }

}

