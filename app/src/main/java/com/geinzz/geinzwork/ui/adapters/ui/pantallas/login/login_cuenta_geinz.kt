package com.geinzz.geinzwork.ui.adapters.ui.pantallas.login

import android.icu.util.Calendar
import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.IconButton
import androidx.compose.material.TextButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
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
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.retornar_pleaceholder_label
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.texto_generico_multilinea
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.texto_generico_one_line
import com.geinzz.geinzwork.ui.adapters.ui.dialog_general.spacer_vertical
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
    var errorUsername by remember { mutableStateOf(false) }

    var error_texto_username by remember { mutableStateOf("") }

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
    var username by rememberSaveable { mutableStateOf("") }

    LaunchedEffect(username) {
        val usernameSanitizado = username.replace(" ", "")

        if (usernameSanitizado != username) {
            username = usernameSanitizado
            error_texto_username = "No puede contener espacios"
            errorUsername = true
            return@LaunchedEffect // no seguimos validando mientras haya espacios
        }

        // Solo validar si tiene más de 4 caracteres
        if (usernameSanitizado.length > 3) {
            delay(500) // debounce
            errorUsername = true
            viewmodel_login.verificar_exist_nombre_user(usernameSanitizado)
            error_texto_username = if (usernameExiste) "Nombre de usuario ya existe" else ""
//        } else {
//            // si es muy corto
//            errorUsername = true
//            error_texto_username = "Debe tener al menos 5 caracteres"
//        }
        }
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

    var phone by rememberSaveable { mutableStateOf("") }
    var nombre by rememberSaveable { mutableStateOf("") }
    var apellido by rememberSaveable { mutableStateOf("") }
    var genero by rememberSaveable { mutableStateOf("") }
    var localidad by rememberSaveable { mutableStateOf("") }
    var fechaNacimiento by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var password2 by rememberSaveable { mutableStateOf("") }

    var errorNombre by remember { mutableStateOf(false) }
    var errorApellido by remember { mutableStateOf(false) }
    var errorCorreo by remember { mutableStateOf(false) }
    var errorGenero by remember { mutableStateOf(false) }
    var errorLocalidad by remember { mutableStateOf(false) }
    var errorFechaNacimiento by remember { mutableStateOf(false) }
    var errorTelefono by remember { mutableStateOf(false) }
    var error_pass1 by remember { mutableStateOf(false) }
    var error_pass2 by remember { mutableStateOf(false) }


    LazyColumn(
        modifier = Modifier.padding(10.dp)
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
                onValueChange = { nombre = it },
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
                onValueChange = { apellido = it },
                labelText = "Apellido",
                placeholderText = "Escribe tu apellido",
                texto_error = "El campo es obligatorio",
                isError = errorApellido,

                )
        }
        // Nombre de usuario
        item {
            MyOutlinedTextField(
                value = username,
                onValueChange = {
                    username = it
                },
                labelText = "Nombre de usuario",
                placeholderText = "Escribe tu nombre de usuario",
                texto_error = error_texto_username,
                isError = errorUsername
            )
        }

        // Número celular
        item {
            PhoneNumberWithPicker(
                phoneNumber = phone,
                onPhoneNumberChange = { phone = it }
            )
        }
        // Género
        item {
            ExpandDropDown(options_genero, "Seleciona tu genero") { genero_selecionado ->
                genero = genero_selecionado
            }
        }
        // Localidad
        item {
            ExpandDropDown(opciones_localida, "Seleciona tu localidad") { localida_selecionada ->
                localidad = localida_selecionada
            }
        }

        item {
            DateButton { fecha_obtenida ->
                fechaNacimiento = fecha_obtenida
            }
        }

        item {
            MyOutlinedTextField(
                value = correo,
                onValueChange = { correo = it },
                labelText = "Correo electrónico",
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
                    onPasswordChange = { password = it },
                    password2 = password2,
                    onPassword2Change = { password2 = it },
                    error_pass1,
                    error_pass2
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
                        val datos = login_user(
                            nombre,
                            apellido,
                            username,
                            correo,
                            phone.toInt(),
                            genero,
                            "+51",
                            localidad,
                            fechaNacimiento,
                            password
                        )
                        viewmodel_login.agregar_user(datos, context)


                    } else {
                        Log.d(
                            "datos_para_firebase",
                            "hay un error econtrado \"Phone: $phone, Nombre: $nombre, Apellido: $apellido, Username: $username, Correo: $correo, Genero: $genero, Localidad: $localidad, Fecha de Nacimiento: $fechaNacimiento\""
                        )
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
                            genero = genero,
                            cod_pais = "+51",
                            localidad = localidad,
                            fecha_nac = fechaNacimiento
                        )
                        Log.d("campos_login_google", datos.toString())
                        viewmodel_login.agregar_user_google(datos, context)

                    } else {
                        Log.d(
                            "datos_para_firebase",
                            "hay un error econtrado \"Phone: $phone, Nombre: $nombre, Apellido: $apellido, Username: $username, Correo: $correo, Genero: $genero, Localidad: $localidad, Fecha de Nacimiento: $fechaNacimiento\""
                        )
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
    onPhoneNumberChange: (String) -> Unit
) {
    val state = rememberKomposeCountryCodePickerState()

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
    ) {
        KomposeCountryCodePicker(
            state = state,
            modifier = Modifier.fillMaxWidth(),
            text = phoneNumber,
            onValueChange = { numero ->
                onPhoneNumberChange(numero)
            }, placeholder = { Text("Número de teléfono") },
            selectedCountryFlagSize = FlagSize(width = 20.dp, height = 20.dp)

        )

        Spacer(modifier = Modifier.width(8.dp))

//        OutlinedTextField(
//            value = phoneNumber,
//            onValueChange = onPhoneNumberChange,
//            modifier = Modifier.weight(1f),
//            placeholder = { Text("Número de teléfono") },
//            singleLine = true,
//            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
//            shape = RoundedCornerShape(30)
//        )
    }
}

@Composable
fun campos_correo_contra(
    password: String,
    onPasswordChange: (String) -> Unit,
    password2: String,
    onPassword2Change: (String) -> Unit,
    error_pass1: Boolean,
    error_pass2: Boolean
) {
    Column {

        MyOutlinedTextField(
            value = password,
            onValueChange = onPasswordChange,
            labelText = "Escriba su contraseña",
            placeholderText = "Escriba su contraseña",
            texto_error = "El campo es obligatorio",
            isError = error_pass1
        )
        MyOutlinedTextField(
            value = password2,
            onValueChange = onPassword2Change,
            labelText = "Repita su contraseña",
            placeholderText = "Repita su contraseña",
            texto_error = "El campo es obligatorio",
            isError = error_pass2
        )
    }
}

fun verificarCampo(valor: String): Boolean {
    return valor.isBlank()
}


@Composable
fun DateButton(fecha: (String) -> Unit) {
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
                fecha(selectedDate) // 👈 devolvemos la fecha al padre
            }
        }
    )


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

        shape = RoundedCornerShape(30)
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpandDropDown(lista: List<String>, lable: String, selecionado: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    var selected by remember { mutableStateOf("") }


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
            colors = ExposedDropdownMenuDefaults.textFieldColors()
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
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatePickerExample(
    showDialog: Boolean,
    onDismiss: () -> Unit,
    onDateSelected: (Long?) -> Unit
) {
    val calendar = Calendar.getInstance().apply {
        add(Calendar.DAY_OF_YEAR, +1)
        set(Calendar.MONTH, Calendar.JANUARY)
    }

    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = calendar.timeInMillis,
        initialDisplayedMonthMillis = calendar.timeInMillis,
        yearRange = 2024..2025
    )

    if (showDialog) {
        DatePickerDialog(
            onDismissRequest = onDismiss,
            confirmButton = {
                TextButton(onClick = {
                    onDateSelected(datePickerState.selectedDateMillis)
                    onDismiss()
                }) {
                    texto_generico_one_line("Confirmar")
                }
            },
            colors = DatePickerDefaults.colors()
        ) {
            DatePicker(state = datePickerState)
        }
    }
}


