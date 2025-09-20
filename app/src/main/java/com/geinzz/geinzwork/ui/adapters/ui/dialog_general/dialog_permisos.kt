package com.geinzz.geinzwork.ui.adapters.ui.dialog_general

import android.app.AlertDialog
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.texto_generico_multilinea
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.texto_generico_one_line

@Composable
fun permisos_llamadas(aceptar_permisos: () -> Unit, ondimis: () -> Unit) {
    AlertDialog(
        onDismissRequest = {},
        confirmButton = {
            Button(onClick = {
                aceptar_permisos()
                ondimis()
            }) {
                texto_generico_one_line("Aceptar")
            }
        },
        dismissButton = {
            TextButton(onClick = { ondimis() }) {
                texto_generico_one_line(
                    "cerrar",
                    MaterialTheme.typography.bodyMedium
                )
            }
        },
        title = { texto_generico_one_line("Permisos de llamada") },
        text = {
            texto_generico_multilinea(
                "Geinz necesita permiso para realizar llamadas. Por favor, activa el permiso.",
                MaterialTheme.typography.bodyMedium
            )
        },
        icon = {
            Icon(
                imageVector = Icons.Filled.Call,
                contentDescription = "Ubicación",
                modifier = Modifier.size(25.dp)
            )
        }

    )
}