package com.geinzz.geinzwork.ui.adapters.ui.dialog_general

import android.app.AlertDialog
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.geinzz.geinzwork.R
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.btn_aceptar_etc_dialog_general
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.btn_cerra_etc_dialog_general
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.texto_generico_multilinea
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.texto_generico_one_line
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.constantes_lista_localidades.FuenteControladaApp

@Composable
fun permisos_llamadas(aceptar_permisos: () -> Unit, ondimis: () -> Unit) {
    AlertDialog(
        onDismissRequest = {ondimis()},
        confirmButton = {
          btn_aceptar_etc_dialog_general {
              aceptar_permisos()
              ondimis()
          }
        },
        dismissButton = {
            btn_cerra_etc_dialog_general{ ondimis()}
        },
        title = {
            FuenteControladaApp{
            texto_generico_one_line("Permisos de llamada") }
            },
        text = {
            FuenteControladaApp{

            texto_generico_multilinea(
                "Geinz necesita permiso para realizar llamadas. Por favor, activa el permiso.",
                MaterialTheme.typography.bodyMedium
            )
            }
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


@Composable
fun permiso_primario_notifi(clik_si:()-> Unit, clik_no:()-> Unit, ondimis: () -> Unit) {
    AlertDialog(
        onDismissRequest = {},
        confirmButton = {
            Button(onClick = {
                clik_si()
                ondimis()
            }) {
                texto_generico_one_line("Aceptar")
            }
        },
        dismissButton = {
            TextButton(onClick = {
                clik_no()
                ondimis()
            }) {
                texto_generico_one_line(
                    "Aun no",
                    MaterialTheme.typography.bodyMedium
                )
            }
        },
        title = {
            FuenteControladaApp{
            texto_generico_one_line("Mantente informado") }
            },
        text = {
            FuenteControladaApp{
            texto_generico_multilinea(
                "Entérate de lo que pasa en tu localidad: eventos, novedades y la llegada de nuevos lugares.",
                MaterialTheme.typography.bodyMedium
            )
            }
        },

        icon = {
            Image(
                painter = painterResource(R.drawable.campana_3d_webp),
                contentDescription = "",
                modifier = Modifier.size(30.dp)
            )


        }
    )
}
