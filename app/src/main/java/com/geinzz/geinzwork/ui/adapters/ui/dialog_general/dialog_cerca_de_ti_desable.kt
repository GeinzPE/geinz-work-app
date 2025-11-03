package com.geinzz.geinzwork.ui.adapters.ui.dialog_general


import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.geinzz.geinzwork.R
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.btn_aceptar_etc_dialog_general
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.btn_cerra_etc_dialog_general
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.icon_geinz_mas_fondo_violeta
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.texto_generico_multilinea
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.texto_generico_one_line
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.constantes_lista_localidades.FuenteControladaApp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun dialog_cerca_de_ti_desable(ondimis: () -> Unit) {
    AlertDialog(
        onDismissRequest = { ondimis() },
        confirmButton = {
            btn_aceptar_etc_dialog_general{
                ondimis()
            }
        },
        dismissButton = {
            btn_cerra_etc_dialog_general{
                ondimis()
            }
        },
        title = {},
        text = {
            FuenteControladaApp{
            Column {
                Box(contentAlignment = Alignment.Center) {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        icon_geinz_mas_fondo_violeta(45.dp)
                    }

                }
                spacer_vertical(7.dp)
                texto_generico_multilinea("El filtro 'Cerca de ti' está desactivado para las categorías de Salud o Seguridad, ya que en una emergencia lo importante es mostrarte toda la ayuda disponible, sin importar la distancia.", style = MaterialTheme.typography.bodyMedium)
                spacer_vertical(10.dp)
            }
            }
        }
    )

}