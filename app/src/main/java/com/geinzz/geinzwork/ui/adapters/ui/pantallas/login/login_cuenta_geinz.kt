package com.geinzz.geinzwork.ui.adapters.ui.pantallas.login

import android.icu.util.Calendar
import android.util.Log
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.IconButton
import androidx.compose.material.TextButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextField

import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.geinzz.geinzwork.data.model.localizate_geinz.login_geinz.login_google
import com.geinzz.geinzwork.data.model.localizate_geinz.login_geinz.login_user
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.MyOutlinedTextField
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.input_email_user_name
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.input_password
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.retornar_pleaceholder_label
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.texto_generico_multilinea
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.texto_generico_one_line
import com.geinzz.geinzwork.ui.adapters.ui.dialog_general.spacer_vertical
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.constantes_lista_localidades
import com.geinzz.geinzwork.viewModels.viewModel_login_user
import com.google.firebase.auth.FirebaseAuth
import com.joelkanyi.jcomposecountrycodepicker.component.KomposeCountryCodePicker
import com.joelkanyi.jcomposecountrycodepicker.component.rememberKomposeCountryCodePickerState
import com.joelkanyi.jcomposecountrycodepicker.data.FlagSize
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

val options_genero = listOf("Masculino", "Femenino", "Otro")
val opciones_localida = listOf("Barranca", "Supe", "Puerto supe", "Paramonga", "Pativilca")
private lateinit var firebaseAuth: FirebaseAuth

@Composable
fun login_principal(tipo_cuenta: String, navController: NavController) {
    Log.d("tipo_cuenta", tipo_cuenta)
    componentes_crear_cuenta(tipo_cuenta, navController)
}

@Composable
fun componentes_crear_cuenta(tipo_cuenta: String, navController: NavController) {
    val context = LocalContext.current
    val viewmodel_login: viewModel_login_user = viewModel()
    val registrado = viewmodel_login.registrado_boolean.observeAsState()
    val registrado_google = viewmodel_login.registrado_google.observeAsState()
    val usernameExiste by viewmodel_login._nombre_userexists.observeAsState(false)
    var correo by rememberSaveable(tipo_cuenta) {
        mutableStateOf(
            if (tipo_cuenta == "crear") "" else tipo_cuenta
        )
    }
    var enable_correo by rememberSaveable(tipo_cuenta) {
        mutableStateOf(
            if (tipo_cuenta == "crear") true else false
        )
    }
    var phone by rememberSaveable { mutableStateOf("") }
    var nombre by rememberSaveable { mutableStateOf("") }
    var apellido by rememberSaveable { mutableStateOf("") }
    var genero by rememberSaveable { mutableStateOf("") }
    var localidad by rememberSaveable { mutableStateOf("") }
    var fechaNacimiento by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var password2 by rememberSaveable { mutableStateOf("") }
    var error_texto_username by remember { mutableStateOf("El campo es obligatorio") }
    var username by rememberSaveable { mutableStateOf("") }
    var error_texto_correo by remember { mutableStateOf("") }
    var cod_pais_params by remember { mutableStateOf("") }
    var nombre_pais_params by remember { mutableStateOf("") }

    var errorNombre by remember { mutableStateOf(false) }
    var errorApellido by remember { mutableStateOf(false) }
    var errorCorreo by remember { mutableStateOf(false) }
    var errorGenero by remember { mutableStateOf(false) }
    var errorLocalidad by remember { mutableStateOf(false) }
    var errorFechaNacimiento by remember { mutableStateOf(false) }
    var errorTelefono by remember { mutableStateOf(false) }
    var error_pass1 by remember { mutableStateOf(false) }
    var error_pass2 by remember { mutableStateOf(false) }
    var errorUsername by remember { mutableStateOf(false) }

    var nombreTocado by remember { mutableStateOf(false) }
    var apellido_tocado by remember { mutableStateOf(false) }
    var numero_tocado by remember { mutableStateOf(false) }
    var correo_tocado by remember { mutableStateOf(false) }
    var contra1_tocado by remember { mutableStateOf(false) }
    var contra2_tocado by remember { mutableStateOf(false) }

    var cargandoUsername by remember { mutableStateOf(false) }
    var username_tocado by remember { mutableStateOf(false) }

    var error_texto_pass1 by remember { mutableStateOf("") }
    var error_texto_pass2 by remember { mutableStateOf("") }

    errorNombre = nombreTocado && nombre.length == 0
    errorApellido = apellido_tocado && apellido.length == 0
    errorTelefono = numero_tocado && phone.length == 0
    errorCorreo = correo_tocado && correo.isEmpty()

    LaunchedEffect(correo, correo_tocado) {
        if (correo_tocado) {
            if (correo.isEmpty()) {
                errorCorreo = true
                error_texto_correo = "El campo es obligatorio"
            } else if (!constantes_lista_localidades.esGmailValido(correo)) {
                errorCorreo = true
                error_texto_correo = "Debe ser un correo Gmail válido"
            } else {
                errorCorreo = false
                error_texto_correo = ""
            }
        }
    }

    LaunchedEffect(username) {
        // Primero validamos si el usuario ya tocó el campo y lo dejó vacío
        if (username_tocado && username.isEmpty()) {
            errorUsername = true
            cargandoUsername = false
            error_texto_username = "El campo es obligatorio"
            return@LaunchedEffect
        }

        // Verificamos si hay espacios
        if (username.contains(" ")) {
            error_texto_username = "No puede contener espacios"
            errorUsername = true
            return@LaunchedEffect
        }

        // Solo validar si tiene más de 3 caracteres
        if (username.length > 3) {
            cargandoUsername = true
            delay(500)
            viewmodel_login.verificar_exist_nombre_user(username)
            cargandoUsername = false
        } else {
            // No mostrar error si aún no está tocado o longitud insuficiente pero no vacío
            errorUsername = false
            error_texto_username = "Tiene que ser mayor a 3 letras"
        }
    }

    LaunchedEffect(usernameExiste) {
        error_texto_username = if (usernameExiste) "Nombre de usuario ya existe" else ""
        errorUsername = usernameExiste
    }

    LaunchedEffect(registrado.value) {
        if (registrado.value == true) {
            navController.navigate("pantalla_principal") {
                popUpTo("login_principal") { inclusive = true }
            }
        }
    }

    LaunchedEffect(registrado_google.value) {
        if (registrado_google.value == true) {
            navController.navigate("pantalla_principal") {
                popUpTo("login_principal") { inclusive = true }
            }
        }
    }

    LaunchedEffect(password, password2, contra1_tocado, contra2_tocado) {

        if (contra1_tocado) {
            when {
                password.isEmpty() -> {
                    error_pass1 = true
                    error_texto_pass1 = "Este campo es obligatorio"
                }

                password.length < 3 -> {
                    error_pass1 = true
                    error_texto_pass1 = "La contraseña debe tener al menos 3 caracteres"
                }

                else -> {
                    error_pass1 = false
                    error_texto_pass1 = ""
                }
            }
        }

        // Validar segundo campo
        if (contra2_tocado) {
            when {
                password2.isEmpty() -> {
                    error_pass2 = true
                    error_texto_pass2 = "Este campo es obligatorio"
                }

                password != password2 -> {
                    error_pass2 = true
                    error_texto_pass2 = "Las contraseñas no coinciden"
                }

                else -> {
                    error_pass2 = false
                    error_texto_pass2 = ""
                }
            }
        }
    }



    LazyColumn(
        modifier = Modifier
            .padding(10.dp)
            .imePadding()
    ) {
        if (tipo_cuenta.equals("crear")) {
            item {
                texto_generico_one_line(
                    "Crea tu cuenta en Geinz Work",
                    MaterialTheme.typography.headlineSmall
                )
                spacer_vertical(10.dp)
            }
        } else {
            item {
                texto_generico_one_line(
                    "Termina de crear tu cuenta Geinz",
                    MaterialTheme.typography.headlineSmall
                )
                spacer_vertical(10.dp)
            }
        }
        item {
            texto_generico_multilinea(
                "Descubre las novedades de tu localidad, conecta con las tiendas de tu zona y mantente siempre al tanto de lo que sucede cerca de ti.",
                MaterialTheme.typography.bodyMedium
            )
            spacer_vertical(10.dp)
        }
        // nombre
        item {
            MyOutlinedTextField(
                value = nombre,
                onValueChange = {
                    nombre = it
                    if (!nombreTocado) {
                        nombreTocado = true
                    }
                },
                labelText = "Nombre",
                placeholderText = "Escribe tu nombre completo",
                texto_error = "El campo es obligatorio",
                isError = errorNombre,
            )
        }
        // Apellido
        item {
            MyOutlinedTextField(
                value = apellido,
                onValueChange = {
                    apellido = it
                    if (!apellido_tocado) {
                        apellido_tocado = true
                    }
                },
                labelText = "Apellido",
                placeholderText = "Escribe tu apellido",
                texto_error = "El campo es obligatorio",
                isError = errorApellido,
            )
        }
        // Nombre de usuario
        item {
            input_email_user_name(
                value = username,
                onValueChange = {
                    username = it
                    if (!username_tocado) {
                        username_tocado = true
                    }
                },
                labelText = "Nombre de usuario",
                placeholderText = "Escribe tu nombre de usuario",
                texto_error = error_texto_username,
                isError = errorUsername,
                trailingIconContent = {
                    when {
                        cargandoUsername -> {
                            CircularProgressIndicator(
                                modifier = Modifier
                                    .size(20.dp)
                                    .padding(2.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        errorUsername -> {
                            Icon(Icons.Filled.Error, "Error", tint = Color.Red)
                        }

                        username.length > 3 -> {
                            Icon(Icons.Default.Check, "Correcto", tint = Color(0xFF4CAF50))
                        }
                    }
                }
            )
        }
        // Número celular
        item {
            PhoneNumberWithPicker(
                phoneNumber = phone,
                onPhoneNumberChange = {
                    phone = it
                    if (!numero_tocado) {
                        numero_tocado = true
                    }
                },
                isError = errorTelefono,
                texto_error = "El campo es obligatorio",
                { cod_pais, nombre_pais ->
                    cod_pais_params = cod_pais
                    nombre_pais_params = nombre_pais
                }
            )
        }
        // Género
        item {
            ExpandDropDown(
                options_genero,
                errorGenero,
                "Seleciona tu genero",
                "Seleciona tu genero"
            ) { genero_selecionado ->
                genero = genero_selecionado
                errorGenero = false
            }
        }
        // Localidad
        item {
            ExpandDropDown(
                opciones_localida,
                errorLocalidad,
                "Seleciona tu localidad",
                "Seleciona tu localidad"
            ) { localida_selecionada ->
                localidad = localida_selecionada
                errorLocalidad = false
            }
        }
        //Fecha
        item {
            DateButton(errorFechaNacimiento, "El campo es obligatorio") { fecha_obtenida ->
                fechaNacimiento = fecha_obtenida
                errorFechaNacimiento = false
            }
        }
        //Correo electronico
        item {
            MyOutlinedTextField(
                value = correo,
                onValueChange = {
                    correo = it
                    if (!correo_tocado) {
                        correo_tocado = true
                    }
                },
                labelText = "Correo electrónico",
                texto_error = error_texto_correo,
                placeholderText = "Escribe tu correo electrónico",
                keyboardType = KeyboardType.Email,
                isError = errorCorreo,
                enabled = enable_correo
            )
        }

        if (tipo_cuenta.equals("crear")) {
            item {
                campos_correo_contra(
                    password = password,
                    onPasswordChange = {
                        password = it
                        if (!contra1_tocado) {
                            contra1_tocado = true
                        }
                    },
                    password2 = password2,
                    onPassword2Change = {
                        password2 = it
                        if (!contra2_tocado) {
                            contra2_tocado = true
                        }
                    },
                    error_pass1,
                    error_pass2, error_texto_pass1, error_texto_pass2
                )
            }
        }

        if (tipo_cuenta.equals("crear")) {
            //crear cuenta
            item {
                Button(onClick = {
                    errorNombre = verificarCampo(nombre)
                    errorApellido = verificarCampo(apellido)
                    errorUsername = verificarCampo(username)
                    errorCorreo = verificarCampo(correo)
                    errorGenero = verificarCampo(genero)
                    errorLocalidad = verificarCampo(localidad)
                    errorFechaNacimiento = verificarCampo(fechaNacimiento)
                    errorTelefono = verificarCampo(phone)
                    error_pass1 = verificarCampo(password)
                    error_pass2 = verificarCampo(password2)


                    val hayError = listOf(
                        errorNombre,
                        errorApellido,
                        errorUsername,
                        errorCorreo,
                        errorGenero,
                        errorLocalidad,
                        errorFechaNacimiento,
                        errorTelefono, error_pass1, error_pass2
                    ).any { it }

                    if (!hayError) {
                        val datos = login_user(
                            nombre = nombre,
                            apellido = apellido,
                            nombre_user = username,
                            correo = correo,
                            numero_celular = phone.toInt(),
                            cod_pais = cod_pais_params,
                            nacionalidad_numero = nombre_pais_params,
                            genero = genero,
                            localidad = localidad,
                            fecha_nac = fechaNacimiento,
                            password = password
                        )
                        viewmodel_login.agregar_user(datos, context)


                    } else {
                        Toast.makeText(context, "Complete todo los campos", Toast.LENGTH_SHORT)
                            .show()
                    }
                }) {
                    texto_generico_one_line("Crear cuenta")
                }
            }
        } else {
            item {
                Button(onClick = {
                    errorNombre = verificarCampo(nombre)
                    errorApellido = verificarCampo(apellido)
                    errorUsername = verificarCampo(username)
                    errorGenero = verificarCampo(genero)
                    errorLocalidad = verificarCampo(localidad)
                    errorFechaNacimiento = verificarCampo(fechaNacimiento)
                    errorTelefono = verificarCampo(phone)

                    val hayError = listOf(
                        errorNombre,
                        errorApellido,
                        errorUsername,
                        errorCorreo,
                        errorGenero,
                        errorLocalidad,
                        errorFechaNacimiento,
                        errorTelefono
                    ).any { it }

                    if (!hayError) {
                        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return@Button
                        val datos = login_google(
                            nombre = nombre,
                            apellido = apellido,
                            nombre_user = username,
                            correo = correo,
                            id = uid,
                            numero_celular = phone.toInt(),
                            cod_pais = cod_pais_params,
                            nacionalidad_numero = nombre_pais_params,
                            genero = genero,
                            localidad = localidad,
                            fecha_nac = fechaNacimiento

                        )
                        Log.d("campos_login_google", datos.toString())
                        viewmodel_login.agregar_user_google(datos, context)

                    } else {
                        Toast.makeText(context, "Complete todo los campos", Toast.LENGTH_SHORT)
                            .show()

                    }
                }) {
                    texto_generico_one_line("terminar de configurar")
                }

            }
        }


    }
}

@Composable
fun PhoneNumberWithPicker(
    phoneNumber: String,
    onPhoneNumberChange: (String) -> Unit,
    isError: Boolean, texto_error: String,
    datos: (cod_pais: String, nombre_pais: String) -> Unit
) {
    val state = rememberKomposeCountryCodePickerState()
    datos(state.getCountryPhoneCode(), state.getCountryName())

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
    ) {
        Column {

            KomposeCountryCodePicker(
                state = state,
                modifier = Modifier.fillMaxWidth(),
                text = phoneNumber,
                onValueChange = { numero ->
                    onPhoneNumberChange(numero)
                },
                placeholder = { Text("Número de teléfono") },
                selectedCountryFlagSize = FlagSize(width = 20.dp, height = 20.dp),

                error = isError,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = MaterialTheme.colorScheme.onBackground,
                    unfocusedTextColor = MaterialTheme.colorScheme.onBackground,
                    unfocusedBorderColor = MaterialTheme.colorScheme.onBackground,
                    focusedBorderColor = if (isError) Color.Red else MaterialTheme.colorScheme.primary,
                    focusedLabelColor = if (isError) Color.Red else MaterialTheme.colorScheme.primary
                ),
                trailingIcon = {
                    if (isError) {
                        Icon(
                            imageVector = Icons.Filled.Error,
                            contentDescription = "Error",
                            tint = Color.Red
                        )
                    }
                },
            )
            AnimatedVisibility(isError) {
                retornar_pleaceholder_label(texto_error, Color.Red)
            }

        }
        Spacer(modifier = Modifier.width(8.dp))
    }
}

@Composable
fun campos_correo_contra(
    password: String,
    onPasswordChange: (String) -> Unit,
    password2: String,
    onPassword2Change: (String) -> Unit,
    error_pass1: Boolean,
    error_pass2: Boolean,
    texto_error_pass1: String,
    texto_error_pass2: String
) {
    var contra_oculta by rememberSaveable { mutableStateOf(true) }
    var contra_oculta2 by rememberSaveable { mutableStateOf(true) }

    Column {
        input_password(
            contra_oculta,
            error_pass1, texto_error_pass1,
            password,
            { contra_oculta = !contra_oculta },
            { it -> onPasswordChange(it) })

        input_password(
            contra_oculta2,
            error_pass2, texto_error_pass2,
            password2,
            { contra_oculta2 = !contra_oculta2 },
            { it -> onPassword2Change(it) })
    }
}

fun verificarCampo(valor: String): Boolean {
    return valor.isBlank()
}


@Composable
fun DateButton(error_fecha: Boolean, campo_error: String, fecha: (String) -> Unit) {
    var showDialog by remember { mutableStateOf(false) }
    var selectedDate by remember { mutableStateOf("") }

    // Dialog para escoger fecha
    DatePickerExample(
        showDialog = showDialog,
        onDismiss = { showDialog = false },
        onDateSelected = { millis ->
            millis?.let {
                val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                selectedDate = sdf.format(Date(it))
                fecha(selectedDate)
            }
        }
    )

    Column {
        OutlinedTextField(
            value = selectedDate,
            onValueChange = {},
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp),
            placeholder = { Text("Selecciona tu fecha de nacimiento") },
            singleLine = true,
            readOnly = true,
            enabled = true,
            leadingIcon = {
                IconButton(onClick = { showDialog = true }) {
                    Icon(Icons.Default.DateRange, contentDescription = "Seleccionar fecha")
                }
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = MaterialTheme.colorScheme.onBackground,
                unfocusedTextColor = MaterialTheme.colorScheme.onBackground,
                unfocusedBorderColor = MaterialTheme.colorScheme.onBackground,
                focusedBorderColor = if (error_fecha) Color.Red else MaterialTheme.colorScheme.primary,
                focusedLabelColor = if (error_fecha) Color.Red else MaterialTheme.colorScheme.primary
            ),
            isError = error_fecha,

            shape = RoundedCornerShape(30)
        )
        AnimatedVisibility(error_fecha) {

            retornar_pleaceholder_label(campo_error, Color.Red)
        }
    }


}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpandDropDown(
    lista: List<String>,
    isError: Boolean,
    texto_error: String,
    lable: String,
    selecionado: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    var selected by remember { mutableStateOf("") }

    Column {
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded },
            modifier = Modifier
                .padding(vertical = 10.dp)
                .clip(RoundedCornerShape(30))
        ) {
            TextField(
                value = selected,
                onValueChange = {},
                readOnly = true,
                label = { Text(lable) },
                modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth(),
                trailingIcon = {
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                },
                isError = isError,
            )

            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                shape = RoundedCornerShape(bottomStart = 30.dp, bottomEnd = 30.dp)
            ) {
                lista.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option) },
                        onClick = {
                            selected = option
                            expanded = false
                            selecionado(option)
                        }
                    )
                }
            }

        }
        AnimatedVisibility(isError) {
            retornar_pleaceholder_label(texto_error, Color.Red)
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatePickerExample(
    showDialog: Boolean,
    onDismiss: () -> Unit,
    onDateSelected: (Long?) -> Unit
) {
    val calendar = Calendar.getInstance().apply {
        set(Calendar.YEAR, 2007)
        set(Calendar.MONTH, 0)
        set(Calendar.DAY_OF_MONTH, 1)
    }

    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = calendar.timeInMillis,
        initialDisplayedMonthMillis = calendar.timeInMillis,
        yearRange = 1927..2007
    )
    val colors = DatePickerDefaults.colors(
        containerColor = MaterialTheme.colorScheme.surface,
        titleContentColor = MaterialTheme.colorScheme.onBackground,
        weekdayContentColor = MaterialTheme.colorScheme.onBackground,
        headlineContentColor = MaterialTheme.colorScheme.onBackground,
        navigationContentColor = MaterialTheme.colorScheme.onBackground,
        todayContentColor = Color.Red,
        selectedDayContentColor = Color.White,
        dayInSelectionRangeContentColor = Color.White,
        selectedDayContainerColor = Color(0xFF6200EE),
        selectedYearContentColor = MaterialTheme.colorScheme.onBackground
    )



    if (showDialog) {
        DatePickerDialog(
            colors = colors,
            onDismissRequest = onDismiss,
            confirmButton = {
                TextButton(onClick = {
                    onDateSelected(datePickerState.selectedDateMillis)
                    onDismiss()
                }) {
                    texto_generico_one_line("Confirmar")
                }
            },

            ) {
            DatePicker(state = datePickerState, colors = colors)
        }
    }
}



