package com.geinzz.geinzwork.ui.adapters.ui.dialog_general

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.error
import coil3.request.placeholder
import com.geinzz.geinzwork.R
import com.geinzz.geinzwork.data_store.data_store_localidad
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.btn_aceptar_etc_dialog_general
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.btn_cerra_etc_dialog_general
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.texto_generico_multilinea
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.texto_generico_one_line
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.constantes_lista_localidades.FuenteControladaApp
import kotlinx.coroutines.launch


@Composable
fun dialogo_cerrar_seccion_teinda(txt:String,ondimis: () -> Unit,cerrar_seccion:()-> Unit) {

    AlertDialog(
        onDismissRequest = { ondimis() },
        confirmButton = {
            btn_aceptar_etc_dialog_general (Color.Red){
                ondimis()
                cerrar_seccion()
            }
        },
        dismissButton = {
            btn_cerra_etc_dialog_general {
                ondimis()
            }
        },
        title = {},
        text = {
            FuenteControladaApp {
                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(R.drawable.icon_cerrar_seccion3d)
                                .placeholder(R.drawable.cargando_img_categorias)
                                .error(R.drawable.cargando_img_categorias)
                                .build(), contentDescription = "Imagen",
                            modifier = Modifier
                                .size(30.dp)
                            ,
                            contentScale = ContentScale.Fit
                        )

                    texto_generico_one_line("Cerrar sesión")

                    texto_generico_multilinea(
                        txt,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

            }
        }
    )
}