package com.geinzz.geinzwork.ui.adapters.ui.pantallas.bottom_sheet_general

import android.icu.util.Calendar
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.PopupProperties
import androidx.lifecycle.viewmodel.compose.viewModel
import com.geinzz.geinzwork.data.model.localizate_geinz.login_geinz.login_user
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.retornar_pleaceholder_label
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.texto_generico_one_line
import com.geinzz.geinzwork.viewModels.viewModel_login_user
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.GoogleAuthProvider
import com.joelkanyi.jcomposecountrycodepicker.component.KomposeCountryCodePicker
import com.joelkanyi.jcomposecountrycodepicker.component.rememberKomposeCountryCodePickerState
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

val options_genero = listOf("Masculino", "Femenino", "Otro")
val opciones_localida = listOf("Barranca", "Supe", "Puerto supe", "Paramonga", "Pativilca")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun crear_cuenta_bottom_Sheet(onClose: () -> Unit) {
    Surface() {
        ModalBottomSheet(
            onDismissRequest = { onClose() },
            modifier = Modifier.fillMaxWidth(),
            dragHandle = null,
            containerColor = MaterialTheme.colorScheme.background
        ) {
            componentes_crear_cuenta()
        }
    }

}

@Composable
fun componentes_crear_cuenta() {
    val context = LocalContext.current
    val viewmodel_login: viewModel_login_user = viewModel()

    var phone by rememberSaveable { mutableStateOf("") }
    var nombre by rememberSaveable { mutableStateOf("") }
    var apellido by rememberSaveable { mutableStateOf("") }
    var username by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var password2 by rememberSaveable { mutableStateOf("") }
    var correo by rememberSaveable { mutableStateOf("") }
    var genero by rememberSaveable { mutableStateOf("") }
    var localidad by rememberSaveable { mutableStateOf("") }
    var fechaNacimiento by rememberSaveable { mutableStateOf("") }

    var errorNombre by remember { mutableStateOf(false) }
    var errorApellido by remember { mutableStateOf(false) }
    var errorUsername by remember { mutableStateOf(false) }
    var errorCorreo by remember { mutableStateOf(false) }
    var errorGenero by remember { mutableStateOf(false) }
    var errorLocalidad by remember { mutableStateOf(false) }
    var errorFechaNacimiento by remember { mutableStateOf(false) }
    var errorTelefono by remember { mutableStateOf(false) }
    var error_pass1 by remember { mutableStateOf(false) }
    var error_pass2 by remember { mutableStateOf(false) }


    var show_dialog by remember { mutableStateOf(false) }
    LazyColumn(
        modifier = Modifier
            .padding(10.dp)
            .fillMaxHeight(0.90f)
    ) {
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
                onValueChange = { username = it },
                labelText = "Nombre de usuario",
                placeholderText = "Escribe tu nombre de usuario",
                texto_error = "El campo es obligatorio",
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


        // Correo electrónico
        item {
            MyOutlinedTextField(
                value = correo,
                onValueChange = { correo = it },
                labelText = "Correo electrónico",
                placeholderText = "Escribe tu correo electrónico", keyboardType = KeyboardType.Email
            )
        }

        // Nombre de usuario
        item {
            MyOutlinedTextField(
                value = password,
                onValueChange = { password = it },
                labelText = "Escriba su contraseña",
                placeholderText = "Escriba su contraseña",
                texto_error = "El campo es obligatorio",
                isError = error_pass1
            )
        }
        item {
            MyOutlinedTextField(
                value = password2,
                onValueChange = { password2 = it },
                labelText = "Escriba su contraseña",
                placeholderText = "Escriba su contraseña",
                texto_error = "El campo es obligatorio",
                isError = error_pass2
            )
        }


        //crear cuenta
        item {
            Button(onClick = {
                // Validaciones campo por campo
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

                    Log.d(
                        "datos_para_firebase",
                        "Phone: $phone, Nombre: $nombre, Apellido: $apellido, Username: $username, Correo: $correo, Genero: $genero, Localidad: $localidad, Fecha de Nacimiento: $fechaNacimiento"
                    )
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
    }
}

@Composable
fun MyOutlinedTextField(
    value: String,
    onValueChange: (String) -> Unit,
    labelText: String = "Label",
    placeholderText: String = "Escribe aquí",
    texto_error: String = "",
    isError: Boolean = false,
    keyboardType: KeyboardType = KeyboardType.Text,
    isPassword: Boolean = false
) {
    Column {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            shape = RoundedCornerShape(30),
            label = { Text(text = labelText) },
            placeholder = { Text(text = placeholderText) },
            trailingIcon = {
                if (isError) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = "Error",
                        tint = Color.Red
                    )
                }
            },
            textStyle = MaterialTheme.typography.bodyMedium,
            isError = isError,
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            visualTransformation = if (isPassword) PasswordVisualTransformation() else VisualTransformation.None,
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = MaterialTheme.colorScheme.onBackground,
                unfocusedTextColor = MaterialTheme.colorScheme.onBackground,
                unfocusedBorderColor = MaterialTheme.colorScheme.onBackground,
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                focusedLabelColor = MaterialTheme.colorScheme.primary
            )
        )
        AnimatedVisibility(isError) {
            retornar_pleaceholder_label(texto_error, Color.Red)
        }
    }
}


//@Composable
//fun password_Field(){
//
//}

@Composable
fun PhoneNumberWithPicker(
    phoneNumber: String,
    onPhoneNumberChange: (String) -> Unit
) {
    val state = rememberKomposeCountryCodePickerState()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        KomposeCountryCodePicker(
            state = state,
            modifier = Modifier
                .width(120.dp)
                .height(60.dp),
            text = phoneNumber,
            onValueChange = {}
        )

        Spacer(modifier = Modifier.width(8.dp))

        OutlinedTextField(
            value = phoneNumber,
            onValueChange = onPhoneNumberChange,
            modifier = Modifier.weight(1f),
            placeholder = { Text("Número de teléfono") },
            singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )
    }
}


fun verificarCampo(valor: String): Boolean {
    return valor.isBlank()
}

@Composable
fun dop_down_menu_genero() {
    var expanded by remember { mutableStateOf(false) }
    Box(modifier = Modifier) {
        Button(onClick = { expanded = true }) {
            texto_generico_one_line("ver opciones")
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            properties = PopupProperties(dismissOnClickOutside = true, dismissOnBackPress = true)
        ) {
            DropdownMenuItem(
                text = { texto_generico_one_line("masculino") },
                onClick = { expanded = false })
            DropdownMenuItem(
                text = { texto_generico_one_line("femenino") },
                onClick = { expanded = false })
            DropdownMenuItem(
                text = { texto_generico_one_line("otro") },
                onClick = { expanded = false })
        }
    }

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
        onValueChange = {}, // no editable
        modifier = Modifier
            .fillMaxWidth()
            .padding(10.dp),
        placeholder = { Text("Selecciona tu fecha de nacimiento") },
        singleLine = true,
        readOnly = true,
        enabled = true,
        trailingIcon = {
            IconButton(onClick = { showDialog = true }) { // 👈 ahora el ícono abre el diálogo
                Icon(Icons.Default.DateRange, contentDescription = "Seleccionar fecha")
            }
        }
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
            .padding(10.dp)
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
            onDismissRequest = { expanded = false }
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