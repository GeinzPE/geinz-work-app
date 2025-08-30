package com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado

import android.icu.util.Calendar
import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.IconButton
import androidx.compose.material.TextButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldColors
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager

import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.geinzz.geinzwork.ui.adapters.ui.dialog_general.spacer_vertical
import com.joelkanyi.jcomposecountrycodepicker.annotation.RestrictedApi
import com.joelkanyi.jcomposecountrycodepicker.component.CountrySelectionDialog
import com.joelkanyi.jcomposecountrycodepicker.component.KomposeCountryCodePicker
import com.joelkanyi.jcomposecountrycodepicker.component.rememberKomposeCountryCodePickerState
import com.joelkanyi.jcomposecountrycodepicker.data.FlagSize

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun MyOutlinedTextField(
    value: String,
    onValueChange: (String) -> Unit,
    labelText: String = "Label",
    placeholderText: String = "Escribe aquí",
    texto_error: String = "",
    isError: Boolean = false,
    keyboardType: KeyboardType = KeyboardType.Text,
    isPassword: Boolean = false,
    enabled: Boolean = true
) {
    Column {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 5.dp),
            shape = RoundedCornerShape(30),
            label = { Text(text = labelText) },
            placeholder = { Text(text = placeholderText) },
            trailingIcon = {
                if (isError) {
                    androidx.compose.material3.Icon(
                        imageVector = Icons.Filled.Error,
                        contentDescription = "Error",
                        tint = Color.Red
                    )
                }
            },
            enabled = enabled,
            textStyle = MaterialTheme.typography.bodyMedium,
            isError = isError,
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            visualTransformation = if (isPassword) PasswordVisualTransformation() else VisualTransformation.None,
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = MaterialTheme.colorScheme.onBackground,
                unfocusedTextColor = MaterialTheme.colorScheme.onBackground,
                unfocusedBorderColor = MaterialTheme.colorScheme.onBackground,
                focusedBorderColor = if (isError) Color.Red else MaterialTheme.colorScheme.primary,
                focusedLabelColor = if (isError) Color.Red else MaterialTheme.colorScheme.primary
            )
        )
        AnimatedVisibility(isError) {
            Box(modifier = Modifier.padding(top = 5.dp, start = 5.dp)) {
                val campo_error =
                    if (texto_error.isEmpty()) "El campo es obligatorio" else texto_error
                retornar_pleaceholder_label(campo_error, Color.Red)
            }
        }
    }
}

@Composable
fun input_email_user_name(
    value: String,
    onValueChange: (String) -> Unit,
    labelText: String = "Label",
    placeholderText: String = "Escribe aquí",
    texto_error: String = "",
    isError: Boolean = false,
    keyboardType: KeyboardType = KeyboardType.Text,
    trailingIconContent: @Composable (() -> Unit)? = null
) {
    Column {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 5.dp),
            shape = RoundedCornerShape(30),
            label = { Text(text = labelText) },
            placeholder = { Text(text = placeholderText) },
            trailingIcon = trailingIconContent,

            textStyle = MaterialTheme.typography.bodyMedium,
            isError = isError,
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = MaterialTheme.colorScheme.onBackground,
                unfocusedTextColor = MaterialTheme.colorScheme.onBackground,
                unfocusedBorderColor = MaterialTheme.colorScheme.onBackground,
                focusedBorderColor = if (isError) Color.Red else MaterialTheme.colorScheme.primary,
                focusedLabelColor = if (isError) Color.Red else MaterialTheme.colorScheme.primary
            )
        )
        AnimatedVisibility(isError) {
            Box(modifier = Modifier.padding(top = 5.dp, start = 5.dp)) {
                val campo_error =
                    if (texto_error.isEmpty()) "El campo es obligatorio" else texto_error
                Log.d("obtenos_erro", campo_error)
                retornar_pleaceholder_label(campo_error, Color.Red)
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
                Box(modifier = Modifier.padding(top = 5.dp, start = 5.dp)) {
                    retornar_pleaceholder_label(texto_error, Color.Red)
                }
            }

        }
        Spacer(modifier = Modifier.width(8.dp))
    }
}

@OptIn(RestrictedApi::class)
@Composable
fun SeleccionarPais(
    datos: (String, String) -> Unit,
    isError: Boolean,
    seleccionado: String
) {
    val state = rememberKomposeCountryCodePickerState(
        showCountryCode = false,
        showCountryFlag = true
    )

    var touched by remember { mutableStateOf(false) }
    var country by remember { mutableStateOf(seleccionado) }
    var openCountrySelectionDialog by remember { mutableStateOf(false) }

    val focusManager = LocalFocusManager.current

    // Siempre actualizar el nombre del país cuando cambie el código
    LaunchedEffect(state.countryCode) {
        if (state.countryCode.isNotEmpty()) {
            val nombre = state.getCountryName()
            country = nombre
            touched = true
            datos(nombre, state.countryCode)
            Log.d("SeleccionPais", "País seleccionado: $nombre - ${state.countryCode}")
        }
    }

    // Mostrar el diálogo cuando openCountrySelectionDialog sea true
    if (openCountrySelectionDialog) {
        CountrySelectionDialog(
            countryList = state.countryList,
            onDismissRequest = { openCountrySelectionDialog = false },
            onSelect = { countryItem ->
                state.setCode(countryItem.code) // bandera se actualiza
                openCountrySelectionDialog = false
            },
            containerColor = MaterialTheme.colorScheme.background,
            contentColor = MaterialTheme.colorScheme.onBackground
        )
    }

    Column {
        KomposeCountryCodePicker(
            state = state,
            modifier = Modifier
                .fillMaxWidth()
                .onFocusChanged { focusState ->
                    if (focusState.isFocused) {
                        openCountrySelectionDialog = true
                        focusManager.clearFocus()
                    }
                },
            text = if (touched) country else "",
            placeholder = {
                if (!touched) Text("Nacionalidad")
            },
            selectedCountryFlagSize = FlagSize(width = 20.dp, height = 20.dp),
            onValueChange = {},
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
            }
        )

        AnimatedVisibility(isError) {
            Box(modifier = Modifier.padding(top = 5.dp, start = 5.dp)) {
                retornar_pleaceholder_label("El campo es obligatorio", Color.Red)
            }
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
                .padding(vertical = 5.dp)
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
            Box(modifier = Modifier.padding(top = 5.dp, start = 5.dp)) {
                retornar_pleaceholder_label(texto_error, Color.Red)
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
                .padding(top = 5.dp)
                .fillMaxWidth(),
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
            Box(modifier = Modifier.padding(top = 5.dp, start = 5.dp)) {
                retornar_pleaceholder_label(campo_error, Color.Red)
            }
        }
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

        spacer_vertical(10.dp)
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
fun input_password(
    contra_oculta: Boolean,
    isError: Boolean,
    texto_error: String = "",
    user_contra: String,
    mostrar_ocultar_contra: () -> Unit,
    valor_contra: (String) -> Unit
) {
    Column {

        OutlinedTextField(
            value = user_contra,
            onValueChange = { valor_contra(it) },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text(text = "Ingresa tu contraseña") },
            maxLines = 1,
            isError = isError,
            textStyle = MaterialTheme.typography.bodyMedium,
            shape = RoundedCornerShape(30),
            label = { Text("Ingresa tu contraseña") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            visualTransformation = if (contra_oculta) PasswordVisualTransformation() else VisualTransformation.None,
            trailingIcon = {
                val icon = if (contra_oculta) {
                    Icons.Filled.Visibility   // 👁 mostrar contraseña
                } else {
                    Icons.Filled.VisibilityOff // 👁‍🗨 ocultar contraseña
                }

                androidx.compose.material.Icon(
                    tint = MaterialTheme.colorScheme.onBackground,
                    imageVector = icon,
                    contentDescription = if (contra_oculta) "Mostrar contraseña" else "Ocultar contraseña",
                    modifier = Modifier.clickable { mostrar_ocultar_contra() }
                )
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = MaterialTheme.colorScheme.onBackground,
                unfocusedTextColor = MaterialTheme.colorScheme.onBackground,
                unfocusedBorderColor = MaterialTheme.colorScheme.onBackground,
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                focusedLabelColor = MaterialTheme.colorScheme.primary
            )
        )
        AnimatedVisibility(isError) {
            Box(modifier = Modifier.padding(top = 5.dp, start = 5.dp)) {
                val campo_error =
                    if (texto_error.isEmpty()) "El campo es obligatorio" else texto_error
                retornar_pleaceholder_label(campo_error, Color.Red)
            }
        }
    }
}
