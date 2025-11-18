package com.geinzz.geinzwork.ui.adapters.ui.pantallas.bottom_sheet_general

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.texto_generico_multilinea
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.texto_generico_one_line
import com.geinzz.geinzwork.ui.adapters.ui.dialog_general.spacer_vertical
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.constantes_lista_localidades.FuenteControladaApp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun verificar_version(nueva_version: String, cambiosrealizados: String, ondimis: () -> Unit) {
    ModalBottomSheet(
        onDismissRequest = { ondimis() },
        containerColor = MaterialTheme.colorScheme.background
    ) {
        FuenteControladaApp {
            Column(Modifier.padding(10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                texto_generico_multilinea(
                    "Nueva version disponible",
                    style = MaterialTheme.typography.titleLarge
                )
                spacer_vertical(10.dp)
                texto_generico_one_line(nueva_version)
                texto_generico_multilinea(
                    cambiosrealizados,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}