package com.geinzz.geinzwork.ui.adapters.ui.dialog_general

import android.content.Context
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.error
import coil3.request.placeholder
import com.geinzz.geinzwork.R
import com.geinzz.geinzwork.model.open_apps.fb_tk_ig.open_fb_tk_ig.abrir_whattsapp
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.btn_aceptar_etc_dialog_general
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.btn_cerra_etc_dialog_general
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.texto_generico_multilinea
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.texto_generico_one_line
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.constantes_lista_localidades.FuenteControladaApp
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.constantes_lista_localidades.capitalizeFirst

@Composable
fun dialog_administrar_perfil(
    ondimis: () -> Unit,
    contex: Context,
    id_tienda: String,
    nombre_tienda: String
) {
    AlertDialog(
        onDismissRequest = { ondimis() },
        confirmButton = {
            btn_aceptar_etc_dialog_general(txt_btn = "Iniciar Verificación") {
                abrir_whattsapp(
                    tipo = "normal",
                    id_tienda = "",
                    localidad_tienda = "",
                    context = contex,
                    numero = "958 120 920",
                    mensajePredefinido = "Hola Geinz, soy el PROPIETARIO/ENCARGADO de la tienda y deseo iniciar la verificación presencial para tomar el control. ID: $id_tienda / Negocio: ${nombre_tienda.capitalizeFirst()}"
                )
                ondimis()
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
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(R.drawable.icono_varificado_geinz)
                            .placeholder(R.drawable.cargando_img_categorias)
                            .error(R.drawable.cargando_img_categorias)
                            .build(), contentDescription = "Imagen",
                        modifier = Modifier
                            .size(50.dp)
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                    texto_generico_one_line("¡Queremos asegurar tu perfil")
                    texto_generico_multilinea(
                        "Entendemos lo importante que es tu negocio. Para darte el acceso total y exclusivo al panel de administración (horarios, fotos y estadísticas), nuestro equipo de Geinz realizará una **visita rápida y presencial** a tu local. Simplemente inicia el proceso por WhatsApp y coordinaremos la visita. Si no estás, asegúrate de dejar un encargado de confianza: ¡Queremos verte en el Plan Control lo antes posible!",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    )

}