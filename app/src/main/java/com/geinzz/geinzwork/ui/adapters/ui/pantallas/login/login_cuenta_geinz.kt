package com.geinzz.geinzwork.ui.adapters.ui.pantallas.login

import android.R.attr.phoneNumber
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.texto_generico_multilinea
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.texto_generico_one_line
import com.joelkanyi.jcomposecountrycodepicker.component.KomposeCountryCodePicker
import com.joelkanyi.jcomposecountrycodepicker.component.rememberKomposeCountryCodePickerState


@Preview
@Composable
fun login_screen() {
    var text by remember { mutableStateOf("") }
    var phoneNumber by rememberSaveable { mutableStateOf("") }
    val state = rememberKomposeCountryCodePickerState()
    var phone by rememberSaveable { mutableStateOf("") }

    LazyColumn {
        item{
            texto_generico_one_line("Login")
        }
        item {
            texto_generico_multilinea("Registrate gratis en geinz work y empieza a explora diferentes lugares de tu localidad favortita")
        }
        item {
            MyOutlinedTextField(
                value = text,
                onValueChange = { text = it },
                labelText = "Nombre",
                placeholderText = "Escribe tu nombre completo",
                isError = text.isEmpty()
            )
        }
        item {
            MyOutlinedTextField(
                value = text,
                onValueChange = { text = it },
                labelText = "Apellido",
                placeholderText = "Escribe tu apellido",
                isError = text.isEmpty()
            )
        }
        item {
            MyOutlinedTextField(
                value = text,
                onValueChange = { text = it },
                labelText = "Correo electronico",
                placeholderText = "Escribe tu correo electronico",
                isError = text.isEmpty()
            )
        }
        item {
            PhoneNumberWithPicker(
                phoneNumber = phone,
                onPhoneNumberChange = { phone = it }
            )
        }

    }

}
@Composable
fun MyOutlinedTextField(
    value: String,
    onValueChange: (String) -> Unit,
    labelText: String = "Label",
    placeholderText: String = "Escribe aquí",
    isError: Boolean = false
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier
            .fillMaxWidth()
            .padding(10.dp),
        shape = RoundedCornerShape(8.dp),
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
            modifier = Modifier.width(120.dp), // ajusta ancho
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

