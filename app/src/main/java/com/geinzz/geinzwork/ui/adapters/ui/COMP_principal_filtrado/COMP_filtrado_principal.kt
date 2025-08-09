package com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.geinzz.geinzwork.ui.adapters.ui.dialog_general.spacer_horizonta
import com.geinzz.geinzwork.ui.adapters.ui.dialog_general.spacer_vertical
import com.geinzz.geinzwork.ui.adapters.ui.pantallas.principal_ui.retornar_pleaceholder_label
import org.w3c.dom.Text

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


@Composable
fun estados_tiendas(estado: String,color_estado: Color){
    Row (verticalAlignment = Alignment.CenterVertically){
        Text(text = estado, color = color_estado, style = MaterialTheme.typography.bodyMedium)
        spacer_horizonta(5.dp)
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(RoundedCornerShape(50))
                .background(color_estado)
        )
    }
}

@Composable
fun tags_subcateogiras(lista_tags: List<String>) {
    LazyRow(   horizontalArrangement = Arrangement.spacedBy(5.dp)) {
        items(lista_tags){ cap->
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Text(
                    text = cap,
                    modifier = Modifier.padding(horizontal = 7.dp, vertical = 5.dp),
                    color = MaterialTheme.colorScheme.onBackground
                    , style = MaterialTheme.typography.bodySmall
                )
            }
        }

    }
}

@Composable
fun cargando_progess_mas_texto(text: String){

    Row (modifier = Modifier.padding(vertical = 10.dp)){
        Text(
            text = text,
            color = MaterialTheme.colorScheme.onBackground,
            style = MaterialTheme.typography.bodyMedium
        )
        Spacer(modifier = Modifier.width(8.dp))
        CircularProgressIndicator(
            modifier = Modifier.size(20.dp),
            strokeWidth = 2.dp
        )
    }
}
@Composable
fun ColumnContenedorComun(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp))
            .background(MaterialTheme.colorScheme.background)
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        content = content
    )
}
