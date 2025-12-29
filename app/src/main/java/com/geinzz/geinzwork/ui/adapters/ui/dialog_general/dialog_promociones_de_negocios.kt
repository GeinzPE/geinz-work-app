package com.geinzz.geinzwork.ui.adapters.ui.dialog_general


import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

import coil3.compose.AsyncImage
import com.geinzz.geinzwork.R
import com.geinzz.geinzwork.data.model.dataclass_novedades.compartir_promocion
import com.geinzz.geinzwork.data.model.localizate_geinz.modelo_tienda
import com.geinzz.geinzwork.data_store.data_store_localidad
import com.geinzz.geinzwork.model.open_apps.fb_tk_ig.open_fb_tk_ig.abrir_whattsapp
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.btn_aceptar_etc_dialog_general
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.btn_cerra_etc_dialog_general
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.texto_generico_multilinea
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.texto_generico_one_line
import com.geinzz.geinzwork.ui.adapters.ui.ZoomableGalleryFullScreen
import com.geinzz.geinzwork.ui.adapters.ui.pantallas.bottom_sheet_general.bottom_sheet_registrate
import com.geinzz.geinzwork.ui.adapters.ui.pantallas.bottom_sheet_general.bottom_sheet_tiendas_filtradas
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.constantes_lista_localidades.FuenteControladaApp
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.constantes_lista_localidades.capitalizeFirst
import com.geinzz.geinzwork.viewModels.EstadoPromocion
import com.geinzz.geinzwork.viewModels.viewModel_filtado_tiendas
import com.geinzz.geinzwork.viewModels.viewmodel_datos_promociones
import java.net.URLEncoder

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun dialog_promociones_negocios(
    verificar_intener: Boolean,
    id_tienda: String,
    localidad: String,
    index: Int,
    onDismiss: () -> Unit,crear_cuenta:()-> Unit,iniciar_seccion:()-> Unit
) {
    val viewModel: viewmodel_datos_promociones = viewModel()
    val estado by viewModel.estadoPromocion.collectAsState()
    val viewmodel_filtrado: viewModel_filtado_tiendas = viewModel()
    val datosTienda by viewmodel_filtrado._datos_tienda.observeAsState()
    var dataclass_tienda_seleccionada by remember { mutableStateOf(modelo_tienda()) }
    var mostrarDialogozoom by remember { mutableStateOf(false) }
    var valor_img_completa by remember { mutableStateOf("") }
    val context= LocalContext.current
    LaunchedEffect(id_tienda, localidad, index) {
        viewModel.obtener_datos_promociones(id_tienda, localidad, index)
    }
    val uid_respald_user by data_store_localidad.get_uid_user(context).collectAsState(initial = "")

    var mostrar_bottom_datos by remember { mutableStateOf(false) }
    var mostar_dialog_registrate by remember { mutableStateOf(false) }


    // 🔹 OVERLAY OSCURO
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.75f))
            .clickable(indication = null, interactionSource = remember { MutableInteractionSource() }) { onDismiss() }, contentAlignment = Alignment.Center
    ) {

        when (estado) {

            is EstadoPromocion.Cargando -> {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = Color.White
                )
            }

            is EstadoPromocion.Exito -> {
                val promo = (estado as EstadoPromocion.Exito).data
                LaunchedEffect(mostrar_bottom_datos) {
                    if (mostrar_bottom_datos) {
                        viewmodel_filtrado.obtener_campos_tiendas_por_id(
                            promo.localidad, id_tienda
                        )
                    }
                }
                LaunchedEffect(datosTienda) {
                    if (!datosTienda.isNullOrEmpty()) {
                        dataclass_tienda_seleccionada = datosTienda!!.first()
                    }
                }
                val localidad_pasada= when(promo.localidad){
                    "barranca"->"ba"
                    "paramonga"->"par"
                    "pativilca"->"pat"
                    "supe"->"su"
                    "puerto supe"->"pue"
                    else -> promo.localidad
                }

                val link =
                    "https://geinzworkapp.web.app/share?" +
                            "t=p" +
                            "&id=${URLEncoder.encode(id_tienda, "UTF-8")}" +
                            "&l=$localidad_pasada" +
                            "&c=${URLEncoder.encode(promo.categoria, "UTF-8")}" +
                            "&i=$index"
                FuenteControladaApp{

                Column (
                    modifier = Modifier
                        .align(Alignment.Center), horizontalAlignment = Alignment.CenterHorizontally
                ) {

                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.9f)
                            .fillMaxHeight(0.65f)
                            .clip(RoundedCornerShape(topEnd = 24.dp, topStart = 24.dp))
                            .background(Color.Black)
                            .clickable(enabled = false) {}
                        ,contentAlignment = Alignment.Center
                    ) {

                        AsyncImage(
                            model = promo.url_img,
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                                .clickable(indication = null, interactionSource = remember { MutableInteractionSource() }){
                                    mostrarDialogozoom=true
                                    valor_img_completa=promo.url_img
                                }
                        )

                        // ❌ BOTÓN CERRAR
                        IconButton(
                            onClick = { onDismiss() },
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(10.dp)
                                .background(
                                    Color.Black.copy(alpha = 0.5f),
                                    CircleShape
                                )
                                .size(30.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Cerrar",
                                tint = Color.White, modifier = Modifier.padding(5.dp)
                            )
                        }



                    }
                    LazyRow (
                        modifier = Modifier    .fillMaxWidth(0.9f)
                            .clip(RoundedCornerShape(bottomEnd = 24.dp, bottomStart = 24.dp))
                            .height(50.dp)
                            .background(MaterialTheme.colorScheme.background),
                        horizontalArrangement = Arrangement.spacedBy(5.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        item {
                            spacer_horizonta(10.dp)
                            AsyncImage(
                                model = promo.img_logo_tienda,
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .width(30.dp)
                                    .height(30.dp)
                                    .clip(CircleShape)
                            )
                        }

                        item {
                            spacer_horizonta(2.dp)
                            texto_generico_one_line(
                                promo.nombre_tienda.capitalizeFirst(),
                                style = MaterialTheme.typography.bodyMedium
                            )
                            spacer_horizonta(40.dp)
                        }

                        item {
                            Image(painterResource(R.drawable.whatsapp_icon), contentDescription = "whatsapp", modifier = Modifier
                                .width(30.dp)
                                .height(30.dp)
                                .clip(CircleShape).clickable{
                                    abrir_whattsapp(
                                        tipo = "tienda",
                                        id_tienda = promo.id_tienda,
                                        localidad_tienda = promo.localidad,
                                        context = context,
                                        numero = promo.numero_contacto_teinda,
                                        mensajePredefinido = "¡Hola! Vi su promoción en Geinz y me interesa. ¿Podría darme más información, por favor? \n $link"
                                    )
                                })
                            spacer_horizonta(10.dp)
                            Box(modifier = Modifier
                                .clip(CircleShape)
                                .background(Color.White).clickable(indication = null, interactionSource = remember { MutableInteractionSource() }){
                                    if(uid_respald_user.isNotEmpty()){
                                    mostrar_bottom_datos=true

                                    }else{
                                        mostar_dialog_registrate=true
                                    }
                                }) {
                                texto_generico_one_line(
                                    "Ver Perfil",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color.Black,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                                )
                            }
                            spacer_horizonta(10.dp)
                        }

                    }
                }
                }
            }

            is EstadoPromocion.Vacio -> {
                FuenteControladaApp{
                Text(

                    "No hay promoción",
                    modifier = Modifier.align(Alignment.Center),
                    color = Color.White
                )
                }
            }

            is EstadoPromocion.Error -> {
                FuenteControladaApp{

                Text(

                    "Error al cargar",
                    modifier = Modifier.align(Alignment.Center),
                    color = Color.White
                )
                }
            }

        }
        if (mostrarDialogozoom) {
            ZoomableGalleryFullScreen(
                compartir_promocion(),
                imagenes = listOf(valor_img_completa),
                startIndex = 0,
                onDismiss = { mostrarDialogozoom = false }
            )

        }
    }

    if (mostrar_bottom_datos) {

        bottom_sheet_tiendas_filtradas(
            verificar_intener,
            viewmodel_filtrado,
            dataclass_tienda_seleccionada,
            mostrar_bottom_datos
        ) {
            mostrar_bottom_datos = false
        }
    }

    if(mostar_dialog_registrate){
        bottom_sheet_registrate(
            ondimis = { mostar_dialog_registrate = false },
            iniciar_seccion_normal = { iniciar_seccion() },
            crear_cuenta_geinz = { crear_cuenta() },
            texto_bottom_Sheet = "¡Regístrate para ver todos los detalles y disfrutar la experiencia completa!"
        )
    }
}