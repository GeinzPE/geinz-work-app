package com.geinzz.geinzwork.ui.adapters.ui.dialog_general

import android.R
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.btn_aceptar_etc_dialog_general
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.btn_cerra_etc_dialog_general
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.texto_generico_multilinea
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.texto_generico_one_line

@Composable
fun dialog_distancia_map_km_m(distancia: String, ondimis: () -> Unit) {
    AlertDialog(
        onDismissRequest = { ondimis() },
        confirmButton = {
            btn_aceptar_etc_dialog_general {
                ondimis()
            }
        },
        dismissButton = {
            btn_cerra_etc_dialog_general {
                ondimis()
            }
        },
        text = {
            Column {
                texto_generico_one_line("Horario en tiempo real",MaterialTheme.typography.bodyLarge)
                spacer_vertical(5.dp)
                texto_generico_multilinea("El valor mostrado (${distancia}) es una estimación calculada en línea recta y se actualiza al instante con tu GPS. La distancia final puede variar ligeramente debido a las rutas de las calles", style = MaterialTheme.typography.bodyMedium)
                spacer_vertical(10.dp)
                texto_generico_one_line("Horaro en tiempo real",MaterialTheme.typography.bodyLarge)
                spacer_vertical(5.dp)
                texto_generico_multilinea("La disponibilidad  se muestra en el momento exacto. ¡Siempre sabrás si la tienda está lista para recibirte!", style = MaterialTheme.typography.bodyMedium)

            }
        }
    )
}