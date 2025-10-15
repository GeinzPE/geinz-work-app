package com.geinzz.geinzwork.ui.adapters.ui.pantallas.bottom_sheet_general

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.geinzz.geinzwork.R
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.texto_generico_multilinea
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.texto_generico_one_line
import com.geinzz.geinzwork.ui.adapters.ui.dialog_general.spacer_horizonta
import com.geinzz.geinzwork.ui.adapters.ui.dialog_general.spacer_vertical
import com.geinzz.geinzwork.ui.adapters.ui.ui.theme.baners_geinz_work
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.constantes_lista_localidades.FuenteControladaApp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun bottom_sheet_alerta_llamada(
    texto_bottom_Sheet: String = "Aviso de Emergencia",
    ondimis: () -> Unit,
    mostrar_permiso:()-> Unit
) {
    ModalBottomSheet(
        onDismissRequest = { ondimis() },
        containerColor = MaterialTheme.colorScheme.background
    ) {
        FuenteControladaApp {
            Column(Modifier.padding(10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Box(contentAlignment = Alignment.Center) {
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .background(
                                brush = Brush.radialGradient(
                                    colors = listOf(
                                        Color(0xFF8700F3).copy(alpha = 0.7f),
                                        Color.Transparent
                                    ),
                                ),
                                shape = RoundedCornerShape(200.dp)
                            )
                    )
                    Image(
                        painter = painterResource(R.drawable.corazon_seguridad_webp),
                        contentDescription = "logo",
                        modifier = Modifier.size(60.dp)
                    )

                }
                Text(
                    text = texto_bottom_Sheet,
                    fontSize = 20.sp,
                    textAlign = TextAlign.Center,
                    color = Color.White,
                    fontFamily = baners_geinz_work,
                    modifier = Modifier.padding(horizontal = 10.dp)
                )
                spacer_vertical(20.dp)
                texto_generico_multilinea(
                    "En una situación crítica, cada segundo cuenta. Nuestro objetivo es conectarte con la ayuda de la manera más rápida posible",
                    MaterialTheme.typography.bodyMedium
                )
                spacer_vertical(10.dp)
                texto_generico_multilinea(
                    "Activa el permiso de Teléfono para que las llamadas a los servicios de auxilio se inicien al instante, sin interrupciones ni demoras.",
                    style = MaterialTheme.typography.bodyMedium
                )
                spacer_vertical(10.dp)
                Row(modifier = Modifier.fillMaxWidth().height(60.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.fillMaxWidth().height(60.dp).clip(CircleShape).background(MaterialTheme.colorScheme.surface).weight(1f).clickable{
                        ondimis()
                    }, contentAlignment = Alignment.Center) {
                        texto_generico_one_line("Activar despues", style = MaterialTheme.typography.bodyMedium)
                    }
                    spacer_horizonta(10.dp)
                    Box(modifier = Modifier.fillMaxWidth().height(60.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primary).weight(1f).clickable{mostrar_permiso()
                        ondimis()}, contentAlignment = Alignment.Center) {
                        texto_generico_one_line("Activar ahora",style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        }
    }
}