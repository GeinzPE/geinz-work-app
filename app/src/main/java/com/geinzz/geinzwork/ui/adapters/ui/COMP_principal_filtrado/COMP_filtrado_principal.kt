package com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.geinzz.geinzwork.ui.adapters.ui.pantallas.principal_ui.retornar_pleaceholder_label

@Composable
fun custom_texFiel(
    value: String,
    onValueChange: (String) -> Unit,
    labelText: String,
    placeholderText: String,
    trailingIcon: @Composable (() -> Unit)? = null,
    isError: Boolean = false,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        shape = RoundedCornerShape(20.dp),
        label = { retornar_pleaceholder_label(labelText) },
        placeholder = { retornar_pleaceholder_label(placeholderText) },
        trailingIcon = trailingIcon,
        textStyle = MaterialTheme.typography.bodyMedium,
        isError = isError,
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
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
fun existencia_dato(){
    Text(
        "No hay coincidencias ingrese otra palabra",
        color = Color.Red,
        style = MaterialTheme.typography.bodyLarge,
        modifier = Modifier.padding(5.dp)
    )
}
