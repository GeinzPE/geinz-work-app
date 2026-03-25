package com.geinzz.geinzwork.ui.adapters.ui.pantallas.inmobiliaria

import android.content.Context
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.error
import coil3.request.placeholder
import com.geinzz.geinzwork.R
import com.geinzz.geinzwork.data.model.dataclass_geinz_inmobiliaria_principal
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.ColumnContenedorComun
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.texto_generico_multilinea
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.texto_generico_one_line
import com.geinzz.geinzwork.ui.adapters.ui.dialog_general.spacer_vertical
import com.geinzz.geinzwork.ui.adapters.ui.pantallas.bottom_sheet_general.bottom_shet_filtrado_inmubles
import com.geinzz.geinzwork.ui.adapters.ui.pantallas.promociones_Cercanas.GaleriaHorizontalInstagram
import com.geinzz.geinzwork.ui.adapters.ui.pantallas.socios.ShimmerImagenConMarca
import com.geinzz.geinzwork.ui.adapters.ui.principal.texFiel_fake
import com.geinzz.geinzwork.ui.adapters.ui.ui.theme.banerGeinzWork
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.constantes_lista_localidades.abrir_whattsapp
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.constantes_lista_localidades.capitalizeFirst
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.constantes_lista_localidades.house_capital_whatsap
import com.geinzz.geinzwork.viewModels.viewmodel_inmobiliaria

@Composable
fun pantalla_geinz_inmobiliaria(
    viewmodel: viewmodel_inmobiliaria,
    nombre_user: String,
    coneccion: Boolean,
    localidad_user: String,
    ver_detalles_completos: (id: String, localidad_user: String, nombreuser: String) -> Unit
) {
    val context = LocalContext.current
    val listarepo by viewmodel.estado_carga_inmuebles_principales.collectAsState()

    LaunchedEffect(listarepo) { }

    val listState = rememberLazyListState()
    LaunchedEffect(Unit) {
        viewmodel.obtener_inmubles_dados(localidad_user)
    }

    var mostar_bottom_sheet_filtrado by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        LazyColumn(
            state = listState, verticalArrangement = Arrangement.spacedBy(5.dp)
        ) {

            item {

                texto_generico_multilinea(
                    "House capital group", style = MaterialTheme.typography.banerGeinzWork
                )

                spacer_vertical(5.dp)

                texto_generico_multilinea(
                    "Obten lo mejore de barranca y mira los mejores precios que traemos para ti ",
                    style = MaterialTheme.typography.bodyMedium
                )

            }
            stickyHeader() {
                ColumnContenedorComun {
                    Column(modifier = Modifier.fillMaxSize().background(Color.Red)) {
                        Box(modifier = Modifier.background(MaterialTheme.colorScheme.primary)) {
                            texto_generico_one_line("filtrar", modifier = Modifier.clickable{
                                mostar_bottom_sheet_filtrado=true
                            })
                        }
                    }
                }
            }


            when (listarepo) {

                viewmodel_inmobiliaria.estado_carga_principal_immubles.loading -> {

                    item {
                        ShimmerImagenConMarca("HOUSE CAPITAL GROUP")
                    }
                }

                is viewmodel_inmobiliaria.estado_carga_principal_immubles.succes -> {

                    val lista_inmubles =
                        (listarepo as viewmodel_inmobiliaria.estado_carga_principal_immubles.succes).lista_inmuebles

                    items(lista_inmubles) { i ->

                        estilo_visual_card(
                            viewmodel,
                            i,
                            context,
                            "https://firebasestorage.googleapis.com/v0/b/geinzworkapp.appspot.com/o/logo_geinz_webp.webp?alt=media&token=aa1ef1df-1bcd-48f2-9cad-a85929c3a8d0"
                        ) {
                            ver_detalles_completos(i.id, localidad_user, nombre_user)
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }

                is viewmodel_inmobiliaria.estado_carga_principal_immubles.empty -> {}
                is viewmodel_inmobiliaria.estado_carga_principal_immubles.error -> {}
                viewmodel_inmobiliaria.estado_carga_principal_immubles.idle -> {}
            }

        }

        if(mostar_bottom_sheet_filtrado){
            bottom_shet_filtrado_inmubles{mostar_bottom_sheet_filtrado=false}
        }
    }

}


@Composable
fun estilo_visual_card(
    viewmodel: viewmodel_inmobiliaria,
    i: dataclass_geinz_inmobiliaria_principal,
    context: Context,
    logo: String,
    img_clikeble: () -> Unit
) {

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(bottomStart = 20.dp, bottomEnd = 20.dp))

    ) {
        GaleriaHorizontalInstagram(
            imagenes = i.lista_img,
            modifier = Modifier.fillMaxSize(),
            img_clikeble_valor = { select ->
                img_clikeble()
            },
            long_listatener = {
                Log.d("LONG_PRESS", "Long press en la galería")
            })
        Box(
            modifier = Modifier
                .padding(10.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(Color(0xFF5E00A8))
                .align(Alignment.TopStart)
        ) {
            texto_generico_one_line(
                i.tipo_propieda.capitalizeFirst(),
                modifier = Modifier.padding(horizontal = 15.dp, vertical = 7.dp),
                style = MaterialTheme.typography.bodyMedium
            )
        }

    }

    Column(
        Modifier.padding(start = 4.dp, end = 4.dp, top = 12.dp, bottom = 8.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 3.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = i.nombre_inmobiliara.capitalizeFirst(),
                    style = MaterialTheme.typography.titleSmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                spacer_vertical(5.dp)

                Text(
                    text = i.descripcion.capitalizeFirst(),
                    style = MaterialTheme.typography.labelSmall,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
//            }
            }

            Spacer(modifier = Modifier.width(8.dp))

            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current).data(logo)
                    .placeholder(R.drawable.cargando_img_categorias)
                    .error(R.drawable.cargando_img_categorias).build(),
                contentDescription = null,
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .clickable(
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() }) {},
                contentScale = ContentScale.Crop
            )

        }

        texto_generico_one_line(
            "Trato : ${i.trato}",
            color = Color(0xFFB0B0B0),
            style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(start = 5.dp)
        )

        Row(verticalAlignment = Alignment.CenterVertically) {
            iconos_datos_inmuebles(
                i.cantidad_banos,
                i.cantidad_dormitrios,
                i.cantidad_cochera,
                i.metros_cuadrados.toString()
            )
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            btns_solo_borde(
                modifier = Modifier.size(50.dp),
                color = Color(0xFF031E6C),
                icono = R.drawable.comparir_icon, {
                    viewmodel.compartir_link_tienda(context, i.localidad, i.id)
                }
            )

            btns(
                modifier = Modifier.weight(1f),
                color = Color(0xFF4A0085),
                icono = R.drawable.google_maps_icono,
                text = "Lugares cercanos", {}
            )

            btns(
                modifier = Modifier.weight(1f),
                color = Color(0xFF29A71A),
                icono = R.drawable.whatsapp_icon,
                text = "WhatsApp", {
                    house_capital_whatsap(
                        context,
                        "+1 (555) 167-1924",
                        "Hola quiero mas informacion sobre " +
                                "https://geinzworkapp.web.app/share?t=in&id=${i.id}&l=${i.localidad}"
                    )
                }
            )
        }
    }
}

@Composable
fun iconos_datos_inmuebles(
    banos: String, dormitorios: String, cochera: String, metros: String
) {

    val icon_bano = R.drawable.icono_bano
    val icon_dormitorio = R.drawable.icono_dormitorio
    val icono_cochera = R.drawable.icono_nochera
    val icon_regla = R.drawable.icono_regla

    Row(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        ItemIcono(icon_regla, "${metros} m²")
        ItemIcono(icon_dormitorio, "${dormitorios} dorm.")
        ItemIcono(icon_bano, "${banos} baños.")
        ItemIcono(icono_cochera, "${cochera} estac.")
    }
}

@Composable
fun ItemIcono(icon: Int, texto: String) {
    val numero = texto.filter { it.isDigit() }.toIntOrNull() ?: 0

    if (texto.isBlank() || numero == 0) return
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {

        Image(
            painter = painterResource(id = icon),
            contentDescription = null,
            modifier = Modifier.size(18.dp),
            colorFilter = ColorFilter.tint(Color.White)
        )

        Text(
            text = texto, fontSize = 13.sp
        )
    }
}

@Composable
fun btns(
    modifier: Modifier = Modifier,
    color: Color,
    icono: Int,
    text: String,
    clikeado: () -> Unit
) {

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(55.dp)
            .background(color, shape = RoundedCornerShape(10.dp))
            .clickable {
                clikeado()
            }
            .padding(vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(3.dp)
        ) {

            Text(
                text = text,
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold
            )
            Image(
                painter = painterResource(id = icono),
                contentDescription = null,
                modifier = Modifier.size(30.dp),
            )

        }
    }
}

@Composable
fun btns_solo_borde(
    modifier: Modifier = Modifier,
    color: Color,
    icono: Int, clikc: () -> Unit
) {

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(55.dp)
            .border(
                width = 2.dp,
                color = color,
                shape = RoundedCornerShape(10.dp)
            )
            .clickable {
                clikc()
            }
            .padding(vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(5.dp)
        ) {

            Image(
                painter = painterResource(id = icono),
                contentDescription = null,
                modifier = Modifier.size(28.dp)
            )
        }
    }
}
