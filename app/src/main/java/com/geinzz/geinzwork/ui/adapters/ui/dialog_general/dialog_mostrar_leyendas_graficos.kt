package com.geinzz.geinzwork.ui.adapters.ui.dialog_general

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.texto_generico_multilinea
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.texto_generico_one_line
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.constantes_lista_localidades.FuenteControladaApp

@Composable
fun dialog_mostar_leyendas_graficos(
    icon: Int,
    nombre_leyenda: String,
    txt_leyenda: String,
    onDismis: () -> Unit
) {
    AlertDialog(onDismissRequest = { onDismis() }, confirmButton = {}, dismissButton = {}, text = {
        FuenteControladaApp {
            Column(verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    modifier = Modifier.size(30.dp),
                    painter = painterResource(icon),
                    contentDescription = "Google maps",
                    tint = Color.Unspecified
                )
                spacer_vertical(10.dp)
                texto_generico_one_line(nombre_leyenda, style = MaterialTheme.typography.titleLarge)
                spacer_vertical(5.dp)
                texto_generico_multilinea(txt_leyenda, style = MaterialTheme.typography.bodyMedium)
            }
        }
    })
}