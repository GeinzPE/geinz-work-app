package com.geinzz.geinzwork.ui.adapters.ui.pantallas.bottom_sheet_general

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.retornar_pleaceholder_label
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.texto_generico_one_line
import com.joelkanyi.jcomposecountrycodepicker.component.KomposeCountryCodePicker
import com.joelkanyi.jcomposecountrycodepicker.component.rememberKomposeCountryCodePickerState


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
    var text by remember { mutableStateOf("") }
    var phoneNumber by rememberSaveable { mutableStateOf("") }
    val state = rememberKomposeCountryCodePickerState()
    var phone by rememberSaveable { mutableStateOf("") }
    var nombre by remember { mutableStateOf("") }
    var apellido by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var correo by remember { mutableStateOf("") }
    var genero by remember { mutableStateOf("") }
    var localidad by remember { mutableStateOf("") }
    var fechaNacimiento by remember { mutableStateOf("") }


    LazyColumn(
        modifier = Modifier
            .padding(10.dp)
    ) {
        // nombre
        item {
            MyOutlinedTextField(
                value = nombre,
                onValueChange = { nombre = it },
                labelText = "Nombre",
                placeholderText = "Escribe tu nombre completo",

                )
        }
        // Apellido
        item {
            MyOutlinedTextField(
                value = apellido,
                onValueChange = { apellido = it },
                labelText = "Apellido",
                placeholderText = "Escribe tu apellido",
            )
        }
        // Nombre de usuario
        item {
            MyOutlinedTextField(
                value = username,
                onValueChange = { username = it },
                labelText = "Nombre de usuario",
                placeholderText = "Escribe tu nombre de usuario",

                )
        }
        // Correo electrónico
        item {
            MyOutlinedTextField(
                value = correo,
                onValueChange = { correo = it },
                labelText = "Correo electrónico",
                placeholderText = "Escribe tu correo electrónico",
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
            MyOutlinedTextField(
                value = genero,
                onValueChange = { genero = it },
                labelText = "Selecciona tu género",
                placeholderText = "Selecciona tu género",
            )
        }
        // Localidad
        item {
            MyOutlinedTextField(
                value = localidad,
                onValueChange = { localidad = it },
                labelText = "Selecciona tu localidad",
                placeholderText = "Selecciona tu localidad",
            )
        }
        // Fecha de nacimiento
        item {
            MyOutlinedTextField(
                value = fechaNacimiento,
                onValueChange = { fechaNacimiento = it },
                labelText = "Ingresa tu fecha de nacimiento",
                placeholderText = "Ingresa tu fecha de nacimiento",
            )
        }
        //crear cuenta
        item {
            Button(onClick = {}) { texto_generico_one_line("Crear cuenta") }
        }
    }
}

@Composable
fun MyOutlinedTextField(
    value: String,
    onValueChange: (String) -> Unit,
    labelText: String = "Label",
    placeholderText: String = "Escribe aquí",
    texto_error: String="",
    isError: Boolean = false
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
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = MaterialTheme.colorScheme.onBackground,
                unfocusedTextColor = MaterialTheme.colorScheme.onBackground,
                unfocusedBorderColor = MaterialTheme.colorScheme.onBackground,
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                focusedLabelColor = MaterialTheme.colorScheme.primary
            )
        )
        AnimatedVisibility(isError) {
            retornar_pleaceholder_label(texto_error,Color.Red)
        }
    }
}

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
        // Aquí el picker ocupa su propio espacio
        KomposeCountryCodePicker(
            state = state,
            modifier = Modifier
                .width(120.dp)
                .height(60.dp), // ajusta ancho
            text = phoneNumber,
            onValueChange = { /* opcional: actualizar text si quieres */ }
        )

        Spacer(modifier = Modifier.width(8.dp))

        OutlinedTextField(
            value = phoneNumber,
            onValueChange = onPhoneNumberChange,
            modifier = Modifier.weight(1f),
            placeholder = { Text("Número de teléfono") },
            singleLine = true
        )
    }
}


@Composable
fun iniciar_seccion() {

}