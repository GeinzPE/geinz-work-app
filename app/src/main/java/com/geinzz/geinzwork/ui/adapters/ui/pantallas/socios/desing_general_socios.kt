package com.geinzz.geinzwork.ui.adapters.ui.pantallas.socios

import android.widget.Toast
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.geinzz.geinzwork.R
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.texto_generico_one_line
import com.geinzz.geinzwork.ui.adapters.ui.dialog_general.spacer_horizonta
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.constantes_lista_localidades.capitalizeFirst
import com.valentinilk.shimmer.shimmer

@Composable
fun boton_generador_por_IA(
    cargando: Boolean,
    onclick: () -> Unit,
    texto_button: String,
    cantidad_monedas: String
) {
    val buttonColor by animateColorAsState(
        targetValue = if (cargando)
            Color.Black
        else
            MaterialTheme.colorScheme.primary,
        label = "buttonColor"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(40.dp)
            .clip(CircleShape)
    ) {
        if (!cargando) {
            FondoIAAnimado(
                modifier = Modifier.matchParentSize()
            )
        }
        Button(
            modifier = Modifier.fillMaxWidth(),
            onClick = {
                onclick()
            },
            colors = ButtonDefaults.buttonColors(
                containerColor = if (cargando) buttonColor else Color.Transparent,
                disabledContainerColor = if (cargando) buttonColor else Color.Transparent,
                contentColor = Color.White,
                disabledContentColor = Color.White
            ),
            enabled = !cargando
        ) {
            if (cargando) {
                Box(
                    modifier = Modifier
                        .height(20.dp)
                        .width(160.dp)
                        .shimmer(),
                    contentAlignment = Alignment.Center

                ) {

                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Spacer(modifier = Modifier.width(8.dp))
                        texto_generico_one_line(
                            "Generando contenido..",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            } else {
                texto_generico_one_line("")
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    texto_generico_one_line(
                        texto_button.capitalizeFirst(),
                        style = MaterialTheme.typography.bodyMedium
                    )
                    spacer_horizonta(5.dp)
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = "Mejorar con IA",
                        tint = Color.White
                    )
                    spacer_horizonta(5.dp)
                    texto_generico_one_line(
                        cantidad_monedas,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    spacer_horizonta(5.dp)
                    Image(
                        painter = painterResource(R.drawable.icon_monedas_3d),
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )

                }

            }
        }
    }

}