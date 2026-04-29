package com.geinzz.geinzwork.herramientas_geinz.constantes

import android.Manifest
import android.app.Activity
import android.content.ClipboardManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import coil3.compose.AsyncImage
import com.geinzz.geinzwork.R
import com.geinzz.geinzwork.data.model.cambiar_datos_pago_contacto
import com.geinzz.geinzwork.data.model.datos_grafico
import com.geinzz.geinzwork.data.model.datos_tienda_fechas
import com.geinzz.geinzwork.data.model.localizate_geinz.metodo_contacto_tienda
import com.geinzz.geinzwork.data.model.localizate_geinz.modelo_pagos_tienda
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.campos_datos_graficos
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.custom_texFiel
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.texto_generico_multilinea
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.texto_generico_one_line
import com.geinzz.geinzwork.ui.adapters.ui.dialog_general.dialog_renovar_plan
import com.geinzz.geinzwork.ui.adapters.ui.dialog_general.spacer_horizonta
import com.geinzz.geinzwork.ui.adapters.ui.dialog_general.spacer_vertical
import com.geinzz.geinzwork.utils.constantes.constantes.constantestextos_general
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.constantes_lista_localidades.capitalizeFirst
import com.geinzz.geinzwork.viewModels.viewmodel_eres_socio
import kotlin.collections.forEach
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocationOff
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.OpenInFull
import androidx.compose.material.icons.filled.Undo
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.input.pointer.pointerInput
import androidx.core.content.ContextCompat
import com.geinzz.geinzwork.data.model.dataclass_novedades.compartir_promocion
import com.geinzz.geinzwork.data.model.datos_tienda
import com.geinzz.geinzwork.data.model.localizate_geinz.filtrado_tiendas.HorarioDia_box
import com.geinzz.geinzwork.data.model.servicio_comodidad
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.custom_textField_150
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.custom_textField_readonly
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.retornar_color_estado_tienda_Box
import com.geinzz.geinzwork.ui.adapters.ui.ZoomableGalleryFullScreen
import com.geinzz.geinzwork.ui.adapters.ui.dialog_general.dialog_sin_ubi__rutas
import com.geinzz.geinzwork.ui.adapters.ui.pantallas.crearBitmapPin
import com.geinzz.geinzwork.ui.adapters.ui.pantallas.filtrado_tiendas.chips_subcategorias_negocio
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.constantes_horas.HorarioSemanal123
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.constantes_lista_localidades.getCategoriaIcon
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.constantes_lista_localidades.shadow_botonm_filtrado_v1
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.constantes_lista_localidades.shadow_top_filtrado_v1
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.constantes_lista_localidades.verificarGPS
import com.geinzz.geinzwork.utils.localizate_geinz.verificarUbiActiva
import com.geinzz.geinzwork.viewModels.viewModel_filtado_tiendas
import com.geinzz.geinzwork.viewModels.viewmodel_recargas
import com.google.android.gms.location.LocationServices
import com.mapbox.geojson.Point
import com.mapbox.maps.CameraOptions
import com.mapbox.maps.MapView
import com.mapbox.maps.extension.compose.MapEffect
import com.mapbox.maps.extension.compose.MapboxMap
import com.mapbox.maps.extension.compose.style.MapStyle
import com.mapbox.maps.extension.style.layers.properties.generated.IconAnchor
import com.mapbox.maps.extension.style.layers.properties.generated.TextAnchor
import com.mapbox.maps.plugin.PuckBearing
import com.mapbox.maps.plugin.animation.MapAnimationOptions
import com.mapbox.maps.plugin.animation.easeTo
import com.mapbox.maps.plugin.annotation.annotations
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationManager
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationOptions
import com.mapbox.maps.plugin.annotation.generated.createPointAnnotationManager
import com.mapbox.maps.plugin.gestures.addOnMapClickListener
import com.mapbox.maps.plugin.locationcomponent.createDefault2DPuck
import com.mapbox.maps.plugin.locationcomponent.location
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


object constantes_expandibles_generales {

    @Composable
    fun expandibles_wrapp_socio_geinzz(
        lsita_datos: List<datos_grafico>,
        txtdescpcion: String,
        texto_params: String,
        expandido: Boolean,
        onClickExpand: () -> Unit
    ) {
        ConstraintLayout(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .padding(horizontal = 10.dp, vertical = 15.dp)
        ) {
            val (texto, btn) = createRefs()
            LazyRow(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(15.dp),
                modifier = Modifier
                    .constrainAs(texto) {
                        top.linkTo(parent.top)
                        bottom.linkTo(parent.bottom)
                    }
                    .clickable(
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() }) {
                        onClickExpand()
                    }) {

                item {

                    AnimatedContent(
                        targetState = expandido, transitionSpec = {
                            fadeIn() togetherWith fadeOut()
                        }) { estado ->

                        if (!estado) {


                            Row(
                                horizontalArrangement = Arrangement.spacedBy(15.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                lsita_datos.forEach { i ->
                                    campos_datos_graficos(i)
                                }
                            }

                        } else {

                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .fillParentMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                texto_generico_one_line(
                                    texto_params, style = MaterialTheme.typography.titleLarge
                                )
                                spacer_vertical(10.dp)
                                texto_generico_multilinea(
                                    txtdescpcion,
                                    style = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier.padding(horizontal = 10.dp)
                                )
                            }

                        }
                    }
                }
            }

        }
    }


    @RequiresApi(Build.VERSION_CODES.O)
    @Composable
    fun expandibles_wrapp_socio_geinzz_datos_tienda(
        nombre_tienda: String,
        viewmodel_recargas: viewmodel_recargas,
        viewModelFiltros: viewmodel_eres_socio,
        context: Context,
        expandido: Boolean,
        datos_tienda_fechas: datos_tienda_fechas,
        onClickExpand: () -> Unit
    ) {
        var por_renovar by remember { mutableStateOf(false) }
        ConstraintLayout(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .padding(horizontal = 10.dp, vertical = 15.dp)
        ) {

            val (texto, btn) = createRefs()

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(15.dp),
                modifier = Modifier.constrainAs(texto) {
                    top.linkTo(parent.top)
                    bottom.linkTo(parent.bottom)
                }

            ) {

                item {

                    AnimatedContent(
                        targetState = expandido, transitionSpec = {
                            fadeIn() togetherWith fadeOut()
                        }) { estado ->

                        if (!estado) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable(
                                        indication = null,
                                        interactionSource = remember { MutableInteractionSource() }) {
                                        onClickExpand()
                                    }) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                                ) {
                                    texto_generico_one_line(
                                        "Datos,fechas y saldo : ",
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                    texto_generico_one_line(
                                        "${datos_tienda_fechas.dias_restantes} días para la renovación del plan.",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = datos_tienda_fechas.color
                                    )
                                }
                            }

                        } else {


                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .fillParentMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(10.dp),
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable(
                                            indication = null,
                                            interactionSource = remember { MutableInteractionSource() }) {
                                            onClickExpand()
                                        }, contentAlignment = Alignment.Center
                                ) {
                                    texto_generico_one_line(
                                        "Datos,fechas y saldo",
                                        style = MaterialTheme.typography.titleLarge
                                    )
                                    spacer_vertical(5.dp)
                                }

                                spacer_vertical(7.dp)
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center,
                                    modifier = Modifier.padding(
                                        start = 5.dp, bottom = 5.dp, end = 10.dp
                                    )
                                ) {

                                    texto_generico_one_line(
                                        "ID :${datos_tienda_fechas.id_tienda}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        modifier = Modifier.weight(1f)
                                    )
                                    Image(
                                        painter = painterResource(R.drawable.baseline_content_copy_24),
                                        contentDescription = "",
                                        modifier = Modifier
                                            .size(20.dp)
                                            .clickable(
                                                indication = null,
                                                interactionSource = remember { MutableInteractionSource() }) {
                                                constantestextos_general.copiarTexto_portapapeles_compouse(
                                                    datos_tienda_fechas.id_tienda, context
                                                )
                                            })
                                }

                                Text(
                                    buildAnnotatedString {
                                        append("Tipo de plan : ")
                                        withStyle(style = SpanStyle(color = Color(0xFFFFD700))) {
                                            append("Premium")
                                        }
                                    },
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color.White,
                                    modifier = Modifier.padding(start = 5.dp, bottom = 5.dp)
                                )

                                texto_generico_one_line(
                                    "fecha de inicio :${datos_tienda_fechas.fecha_ingreso}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier.padding(start = 5.dp, bottom = 5.dp)
                                )

                                if (datos_tienda_fechas.fecha_termino.isNotEmpty()) {
                                    texto_generico_one_line(
                                        "fecha de finalizacion :${datos_tienda_fechas.fecha_termino}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        modifier = Modifier.padding(start = 5.dp, bottom = 5.dp)
                                    )
                                }
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(5.dp),
                                    modifier = Modifier.padding(start = 5.dp)
                                ) {
                                    texto_generico_one_line(
                                        "Saldo disponible",
                                        style = MaterialTheme.typography.bodyMedium,
                                    )

                                    texto_generico_one_line(
                                        datos_tienda_fechas.saldo_cuenta_tienda,
                                    )

                                    Image(
                                        painter = painterResource(R.drawable.icon_monedas_3d),
                                        contentDescription = "saldo",
                                        modifier = Modifier.size(20.dp)
                                    )

                                }

                                texto_generico_one_line(
                                    "${datos_tienda_fechas.dias_restantes} días para la renovación del plan.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = datos_tienda_fechas.color,
                                    modifier = Modifier.padding(start = 5.dp, bottom = 5.dp)
                                )



                                if (datos_tienda_fechas.dias_restantes == "0") {

                                    Box(
                                        modifier = Modifier
                                            .clip(CircleShape)
                                            .background(MaterialTheme.colorScheme.primary)
                                            .clickable {
                                                por_renovar = true
                                            }
                                    ) {
                                        texto_generico_one_line(
                                            "Renovar plan",
                                            style = MaterialTheme.typography.bodyMedium,
                                            modifier = Modifier.padding(
                                                horizontal = 15.dp,
                                                vertical = 10.dp
                                            )
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        if (por_renovar) {
            dialog_renovar_plan(
                saldo_disponible = datos_tienda_fechas.saldo_cuenta_tienda?.toLongOrNull() ?: 0L,
                ondimis = { por_renovar = !por_renovar },
                comprar = { total_cancelar, meses_agregados ->
                    viewModelFiltros.descontar_puntos(
                        viewmodel_recargas,
                        datos_tienda_fechas.saldo_cuenta_tienda.toInt(),
                        nombre_tienda,
                        localidad_tienda = "barranca",
                        id_tienda = datos_tienda_fechas.id_tienda,
                        puntos_descuento = total_cancelar.toInt(),
                        meses_agregados = meses_agregados
                    )
                })
        }
    }


    @RequiresApi(Build.VERSION_CODES.O)
    @Composable
    fun expandibles_wrapp_socio_contacto_tienda(
        viewModelFiltros: viewmodel_eres_socio,
        context: Context,
        expandido: Boolean,
        it: metodo_contacto_tienda,
        datos_tienda: cambiar_datos_pago_contacto,
        onClickExpand: () -> Unit, cambios_guardados: () -> Unit,
    ) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager

        ConstraintLayout(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .padding(horizontal = 10.dp, vertical = 15.dp)
        ) {

            val (texto, btn) = createRefs()

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(15.dp),
                modifier = Modifier.constrainAs(texto) {
                    top.linkTo(parent.top)
                    bottom.linkTo(parent.bottom)
                }

            ) {

                item {

                    AnimatedContent(
                        targetState = expandido, transitionSpec = {
                            fadeIn() togetherWith fadeOut()
                        }) { estado ->

                        if (!estado) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable(
                                        indication = null,
                                        interactionSource = remember { MutableInteractionSource() }) {
                                        onClickExpand()
                                    }) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                                ) {
                                    texto_generico_one_line(
                                        "Contacto y redes :",
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                    contacto_y_redes(it)
                                }
                            }

                        } else {

                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .fillParentMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(10.dp),
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable(
                                            indication = null,
                                            interactionSource = remember { MutableInteractionSource() }) {
                                            onClickExpand()
                                        }, contentAlignment = Alignment.Center
                                ) {
                                    texto_generico_one_line(
                                        "Contacto y redes",
                                        style = MaterialTheme.typography.titleLarge
                                    )
                                    spacer_vertical(5.dp)
                                }
                                texto_generico_multilinea(
                                    "Agrega tus datos de contacto y redes sociales para que tus clientes puedan comunicarse contigo fácilmente.",
                                    style = MaterialTheme.typography.bodyMedium
                                )

                                verificar_activo_contacto(
                                    context,
                                    numero_obtenido = it.llamada.numero,
                                    link_perfil = "",
                                    nombre_user_perfil = "",
                                    logo = R.drawable.llamada_icon,
                                    nombre_metodo = "llamada",
                                    enable = it.llamada.estado, { valor_cambiado ->
                                        viewModelFiltros.cambiar_metodos_pago_tienda(
                                            tipo = "contacto",
                                            id_tienda = datos_tienda.id_tienda,
                                            localidad_tienda = datos_tienda.localida,
                                            metodo_pago = "llamada",
                                            valor_cambiado = valor_cambiado
                                        )
                                        cambios_guardados()
                                    }, { links, nombre, numero ->
                                        viewModelFiltros.cambiar_contacto_redes(
                                            datos_tienda.id_tienda,
                                            datos_tienda.localida,
                                            "llamada",
                                            "",
                                            numero
                                        )
                                        cambios_guardados()
                                    })

                                verificar_activo_contacto(
                                    context,
                                    numero_obtenido = it.whatsapp.numero,
                                    link_perfil = "",
                                    nombre_user_perfil = "",
                                    logo = R.drawable.whatsapp_icon,
                                    nombre_metodo = "whatsapp",
                                    enable = it.whatsapp.estado, { valor_cambiado ->
                                        viewModelFiltros.cambiar_metodos_pago_tienda(
                                            tipo = "contacto",
                                            id_tienda = datos_tienda.id_tienda,
                                            localidad_tienda = datos_tienda.localida,
                                            metodo_pago = "whatsapp",
                                            valor_cambiado = valor_cambiado
                                        )
                                        cambios_guardados()
                                    }, { links, nombre, numero ->
                                        viewModelFiltros.cambiar_contacto_redes(
                                            datos_tienda.id_tienda,
                                            datos_tienda.localida,
                                            "whatsapp",
                                            "",
                                            numero
                                        )
                                        cambios_guardados()
                                    })

                                verificar_activo_contacto(
                                    context,
                                    numero_obtenido = "",
                                    link_perfil = it.instagram.url,
                                    nombre_user_perfil = it.instagram.nombre,
                                    logo = R.drawable.instagram_icon, nombre_metodo = "instagram",
                                    enable = it.instagram.estado, { valor_cambiado ->
                                        viewModelFiltros.cambiar_metodos_pago_tienda(
                                            tipo = "contacto",
                                            id_tienda = datos_tienda.id_tienda,
                                            localidad_tienda = datos_tienda.localida,
                                            metodo_pago = "instagram",
                                            valor_cambiado = valor_cambiado
                                        )
                                        cambios_guardados()
                                    }, { links, nombre, numero ->
                                        viewModelFiltros.cambiar_contacto_redes(
                                            datos_tienda.id_tienda,
                                            datos_tienda.localida,
                                            "instagram",
                                            nombre,
                                            links
                                        )
                                        cambios_guardados()
                                    })

                                verificar_activo_contacto(
                                    context,
                                    numero_obtenido = "",
                                    link_perfil = it.facebook.url,
                                    nombre_user_perfil = it.facebook.nombre,
                                    logo = R.drawable.facebook_icon, nombre_metodo = "facebook",
                                    enable = it.facebook.estado, { valor_cambiado ->
                                        viewModelFiltros.cambiar_metodos_pago_tienda(
                                            tipo = "contacto",
                                            id_tienda = datos_tienda.id_tienda,
                                            localidad_tienda = datos_tienda.localida,
                                            metodo_pago = "facebook",
                                            valor_cambiado = valor_cambiado
                                        )
                                        cambios_guardados()
                                    }, { links, nombre, numero ->
                                        viewModelFiltros.cambiar_contacto_redes(
                                            datos_tienda.id_tienda,
                                            datos_tienda.localida,
                                            "facebook",
                                            nombre,
                                            links
                                        )
                                        cambios_guardados()
                                    })

                                verificar_activo_contacto(
                                    context,
                                    numero_obtenido = "",
                                    link_perfil = it.tiktok.url,
                                    nombre_user_perfil = it.tiktok.nombre,
                                    logo = R.drawable.tik_tok_icon, nombre_metodo = "tiktok",
                                    enable = it.tiktok.estado, { valor_cambiado ->
                                        viewModelFiltros.cambiar_metodos_pago_tienda(
                                            tipo = "contacto",
                                            id_tienda = datos_tienda.id_tienda,
                                            localidad_tienda = datos_tienda.localida,
                                            metodo_pago = "tiktok",
                                            valor_cambiado = valor_cambiado
                                        )
                                        cambios_guardados()
                                    }, { links, nombre, numero ->
                                        viewModelFiltros.cambiar_contacto_redes(
                                            datos_tienda.id_tienda,
                                            datos_tienda.localida,
                                            "tiktok",
                                            nombre,
                                            links
                                        )
                                        cambios_guardados()
                                    })

                                verificar_activo_contacto(
                                    context,
                                    numero_obtenido = "",
                                    link_perfil = it.sitio_web.url,
                                    nombre_user_perfil = it.sitio_web.nombre,
                                    logo = R.drawable.web_icon,
                                    nombre_metodo = "sitio web",
                                    enable = it.sitio_web.estado,
                                    cambiar_valor = { valor_cambiado ->
                                        viewModelFiltros.cambiar_metodos_pago_tienda(
                                            tipo = "contacto",
                                            id_tienda = datos_tienda.id_tienda,
                                            localidad_tienda = datos_tienda.localida,
                                            metodo_pago = "sitio_web",
                                            valor_cambiado = valor_cambiado
                                        )
                                        cambios_guardados()
                                    },
                                    cambios_realizados = { links, nombre, numero ->
                                        viewModelFiltros.cambiar_contacto_redes(
                                            datos_tienda.id_tienda,
                                            datos_tienda.localida,
                                            "sitio_web",
                                            nombre,
                                            links
                                        )
                                        cambios_guardados()
                                    })

                            }
                        }
                    }
                }
            }
        }
    }


    @RequiresApi(Build.VERSION_CODES.O)
    @Composable
    fun expandibles_wrapp_socio_metodos_pago_tienda(
        id_user: String,
        viewModelFiltros: viewmodel_eres_socio,
        context: Context,
        expandido: Boolean,
        metodos_pago: modelo_pagos_tienda,
        datos_tienda: cambiar_datos_pago_contacto,
        onClickExpand: () -> Unit, guardar_dado_datos: () -> Unit
    ) {
        ConstraintLayout(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .padding(horizontal = 10.dp, vertical = 15.dp)
        ) {

            val (texto, btn) = createRefs()

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(15.dp),
                modifier = Modifier.constrainAs(texto) {
                    top.linkTo(parent.top)
                    bottom.linkTo(parent.bottom)
                }
            ) {

                item {
                    AnimatedContent(
                        targetState = expandido, transitionSpec = {
                            fadeIn() togetherWith fadeOut()
                        }) { estado ->

                        if (!estado) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable(
                                        indication = null,
                                        interactionSource = remember { MutableInteractionSource() }) {
                                        onClickExpand()
                                    }) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                                ) {
                                    texto_generico_one_line(
                                        "Metodos de pago :",
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                    metodos_de_pago(metodos_pago)
                                }
                            }

                        } else {

                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .fillParentMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(10.dp),
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable(
                                            indication = null,
                                            interactionSource = remember { MutableInteractionSource() }) {
                                            onClickExpand()
                                        }, contentAlignment = Alignment.Center
                                ) {
                                    texto_generico_one_line(
                                        "Metodos de pago",
                                        style = MaterialTheme.typography.titleLarge
                                    )
                                    spacer_vertical(5.dp)
                                }

                                texto_generico_multilinea(
                                    "Personaliza tu perfil y gestiona tus métodos de pago de manera rápida y sencilla.",
                                    style = MaterialTheme.typography.bodyMedium
                                )

                                verificar_activo_pago(
                                    id_user,
                                    logo = R.drawable.yape_logo,
                                    nombre_metodo = "yape",
                                    enable = metodos_pago.yape.enable,
                                    numero_metodo_param = metodos_pago.yape.numero,
                                    titular_param = metodos_pago.yape.nombre,
                                    qr_metodo_pago = metodos_pago.yape.qr,
                                    cambiar_valor = { valor_cambiado ->
                                        viewModelFiltros.cambiar_metodos_pago_tienda(
                                            tipo = "pago",
                                            id_tienda = datos_tienda.id_tienda,
                                            localidad_tienda = datos_tienda.localida,
                                            metodo_pago = "yape",
                                            valor_cambiado = valor_cambiado
                                        )
                                        guardar_dado_datos()
                                    },
                                    cambiar_datos_internos_pagos = { titular: String, numero: String, uri_params: Uri ->
                                        viewModelFiltros.cambiar_titular_yape_plin(
                                            context,
                                            uri_params,
                                            datos_tienda.id_tienda,
                                            datos_tienda.localida,
                                            "yape",
                                            titular,
                                            numero
                                        )
                                        guardar_dado_datos()
                                    })



                                verificar_activo_pago(
                                    id_user,
                                    logo = R.drawable.logo_plin,
                                    nombre_metodo = "plin",
                                    enable = metodos_pago.plin.enable,
                                    numero_metodo_param = metodos_pago.plin.numero,
                                    titular_param = metodos_pago.plin.nombre,
                                    qr_metodo_pago = metodos_pago.plin.qr,
                                    cambiar_valor = { valor_cambiado ->
                                        viewModelFiltros.cambiar_metodos_pago_tienda(
                                            tipo = "pago",
                                            id_tienda = datos_tienda.id_tienda,
                                            localidad_tienda = datos_tienda.localida,
                                            metodo_pago = "plin",
                                            valor_cambiado = valor_cambiado
                                        )
                                        guardar_dado_datos()
                                    },
                                    cambiar_datos_internos_pagos = { titular: String, numero: String, uri_params ->
                                        viewModelFiltros.cambiar_titular_yape_plin(
                                            context,
                                            uri_params,
                                            datos_tienda.id_tienda,
                                            datos_tienda.localida,
                                            "plin",
                                            titular,
                                            numero
                                        )
                                        guardar_dado_datos()
                                    })

                                verificar_activo_pago(
                                    id_user,
                                    logo = R.drawable.logo_agora,
                                    nombre_metodo = "agora",
                                    enable = metodos_pago.agora.enable,
                                    numero_metodo_param = "",
                                    titular_param = "",
                                    qr_metodo_pago = "",
                                    cambiar_valor = { valor_cambiado ->
                                        viewModelFiltros.cambiar_metodos_pago_tienda(
                                            tipo = "pago",
                                            id_tienda = datos_tienda.id_tienda,
                                            localidad_tienda = datos_tienda.localida,
                                            metodo_pago = "agora",
                                            valor_cambiado = valor_cambiado
                                        )
                                        guardar_dado_datos()
                                    },
                                    cambiar_datos_internos_pagos = { titular: String, numero: String, uri -> })

                                verificar_activo_pago(
                                    id_user,
                                    logo = R.drawable.efectivo_logo,
                                    nombre_metodo = "efectivo",
                                    enable = metodos_pago.efectivo.enable,
                                    numero_metodo_param = "",
                                    titular_param = "",
                                    qr_metodo_pago = "",
                                    cambiar_valor = { valor_cambiado ->
                                        viewModelFiltros.cambiar_metodos_pago_tienda(
                                            tipo = "pago",
                                            id_tienda = datos_tienda.id_tienda,
                                            localidad_tienda = datos_tienda.localida,
                                            metodo_pago = "efectivo",
                                            valor_cambiado = valor_cambiado
                                        )
                                        guardar_dado_datos()
                                    },
                                    cambiar_datos_internos_pagos = { titular: String, numero: String, uri -> })

                                verificar_activo_pago(
                                    id_user,
                                    logo = R.drawable.visa_logo,
                                    nombre_metodo = "visa",
                                    enable = metodos_pago.visa_mastercard.enable,
                                    numero_metodo_param = "",
                                    titular_param = "",
                                    qr_metodo_pago = "",
                                    cambiar_valor = { valor_cambiado ->
                                        viewModelFiltros.cambiar_metodos_pago_tienda(
                                            "pago",
                                            datos_tienda.id_tienda,
                                            datos_tienda.localida,
                                            "visa_mastercard",
                                            valor_cambiado
                                        )
                                        guardar_dado_datos()
                                    },
                                    cambiar_datos_internos_pagos = { titular: String, numero: String, uri -> })

                                verificar_activo_pago(
                                    id_user,
                                    logo = R.drawable.master_car_logo,
                                    nombre_metodo = "mastercard",
                                    enable = metodos_pago.visa_mastercard.enable,
                                    numero_metodo_param = "",
                                    titular_param = "",
                                    qr_metodo_pago = "",
                                    cambiar_valor = { valor_cambiado ->
                                        viewModelFiltros.cambiar_metodos_pago_tienda(
                                            "pago",
                                            datos_tienda.id_tienda,
                                            datos_tienda.localida,
                                            "visa_mastercard",
                                            valor_cambiado
                                        )
                                        guardar_dado_datos()
                                    },
                                    cambiar_datos_internos_pagos = { titular: String, numero: String, uri -> })


                            }
                        }
                    }
                }
            }
        }
    }

    fun normalizar(texto: String): String {
        return texto
            .lowercase()
            .replace("á", "a")
            .replace("é", "e")
            .replace("í", "i")
            .replace("ó", "o")
            .replace("ú", "u")
            .trim()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    @Composable
    fun expandible_wrap_socio_atrubitos(
        id_tienda: String, localidad: String,
        servicios_comodidades: List<servicio_comodidad>,
        viewModelFiltros: viewmodel_eres_socio,
        expandido: Boolean,
        onClickExpand: () -> Unit,
        guardar_dado_datos: () -> Unit
    ) {
        fun estaActivo(
            lista: List<servicio_comodidad>,
            nombre: String
        ): Boolean {
            return lista.any {
                normalizar(it.nombre) == normalizar(nombre) && it.estado
            }
        }

        ConstraintLayout(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .padding(horizontal = 10.dp, vertical = 15.dp)
        ) {
            val (texto, btn) = createRefs()

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(15.dp),
                modifier = Modifier.constrainAs(texto) {
                    top.linkTo(parent.top)
                    bottom.linkTo(parent.bottom)
                }
            ) {
                item {
                    AnimatedContent(
                        targetState = expandido, transitionSpec = {
                            fadeIn() togetherWith fadeOut()
                        }) { estado ->

                        if (!estado) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable(
                                        indication = null,
                                        interactionSource = remember { MutableInteractionSource() }) {
                                        onClickExpand()
                                    }) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                                ) {
                                    texto_generico_one_line(
                                        "Servicios y comodidades :",
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                    servicios_comodidades(servicios_comodidades)
                                }
                            }

                        } else {

                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .fillParentMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(10.dp),
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable(
                                            indication = null,
                                            interactionSource = remember { MutableInteractionSource() }) {
                                            onClickExpand()
                                        }, contentAlignment = Alignment.Center
                                ) {
                                    texto_generico_one_line(
                                        "Servicios y comodidades",
                                        style = MaterialTheme.typography.titleLarge
                                    )
                                    spacer_vertical(5.dp)
                                }

                                texto_generico_multilinea(
                                    "Configura los servicios y comodidades que ofrece tu negocio para que los clientes conozcan qué encontrarán al visitarte.",
                                    style = MaterialTheme.typography.bodyMedium
                                )

                                campos_atributos(
                                    "zona expandida",
                                    R.drawable.icon_zona_expandida,
                                    estaActivo(servicios_comodidades, "zona expandida"),
                                    { it ->
                                        viewModelFiltros.cambiar_atrubitos(
                                            id_tienda,
                                            localidad,
                                            "zona expandida",
                                            it
                                        )
                                        guardar_dado_datos()
                                    })
                                campos_atributos(
                                    "Wifi",
                                    R.drawable.icon_wifi,
                                    estaActivo(servicios_comodidades, "Wifi"),
                                    { it ->
                                        viewModelFiltros.cambiar_atrubitos(
                                            id_tienda,
                                            localidad,
                                            "Wifi",
                                            it
                                        )
                                        guardar_dado_datos()
                                    })
                                campos_atributos(
                                    "servicios higenicos",
                                    R.drawable.icon_servicios_higenicos,
                                    estaActivo(servicios_comodidades, "servicios higenicos"),
                                    { it ->
                                        viewModelFiltros.cambiar_atrubitos(
                                            id_tienda,
                                            localidad,
                                            "servicios higenicos",
                                            it
                                        )
                                        guardar_dado_datos()
                                    })
                                campos_atributos(
                                    "camaras de seguridad",
                                    R.drawable.icon_seguridad,
                                    estaActivo(servicios_comodidades, "camaras de seguridad"),
                                    { it ->
                                        viewModelFiltros.cambiar_atrubitos(
                                            id_tienda,
                                            localidad,
                                            "camaras de seguridad",
                                            it
                                        )
                                        guardar_dado_datos()
                                    })
                                campos_atributos(
                                    "sala de espera",
                                    R.drawable.icon_sala_de_espera,
                                    estaActivo(servicios_comodidades, "sala de espera"),
                                    { it ->
                                        viewModelFiltros.cambiar_atrubitos(
                                            id_tienda,
                                            localidad,
                                            "sala de espera",
                                            it
                                        )
                                        guardar_dado_datos()
                                    })
                                campos_atributos(
                                    "sala de juegos",
                                    R.drawable.icon_sala_para_ninos,
                                    estaActivo(servicios_comodidades, "sala de juegos"),
                                    { it ->
                                        viewModelFiltros.cambiar_atrubitos(
                                            id_tienda,
                                            localidad,
                                            "sala de juegos",
                                            it
                                        )
                                        guardar_dado_datos()
                                    })
                                campos_atributos(
                                    "mesa para niños",
                                    R.drawable.icon_mesa_para_ninos,
                                    estaActivo(servicios_comodidades, "mesa para niños"),
                                    { it ->
                                        viewModelFiltros.cambiar_atrubitos(
                                            id_tienda,
                                            localidad,
                                            "mesa para niños",
                                            it
                                        )
                                        guardar_dado_datos()
                                    })
                                campos_atributos(
                                    "ingreso con mascotas",
                                    R.drawable.icon_ingreso_animales,
                                    estaActivo(servicios_comodidades, "ingreso con mascotas"),
                                    { it ->
                                        viewModelFiltros.cambiar_atrubitos(
                                            id_tienda,
                                            localidad,
                                            "ingreso con mascotas",
                                            it
                                        )
                                        guardar_dado_datos()
                                    })
                                campos_atributos(
                                    "estacionamiento",
                                    R.drawable.icon_estacionamiento,
                                    estaActivo(servicios_comodidades, "estacionamiento"),
                                    { it ->
                                        viewModelFiltros.cambiar_atrubitos(
                                            id_tienda,
                                            localidad,
                                            "estacionamiento",
                                            it
                                        )
                                        guardar_dado_datos()
                                    })
                                campos_atributos(
                                    "enchufe",
                                    R.drawable.icon_enchufa,
                                    estaActivo(servicios_comodidades, "enchufe"),
                                    { it ->
                                        viewModelFiltros.cambiar_atrubitos(
                                            id_tienda,
                                            localidad,
                                            "enchufe",
                                            it
                                        )
                                        guardar_dado_datos()
                                    })
                                campos_atributos(
                                    "aire acondicionado",
                                    R.drawable.icon_aire_acondicionado,
                                    estaActivo(servicios_comodidades, "aire acondicionado"),
                                    { it ->
                                        viewModelFiltros.cambiar_atrubitos(
                                            id_tienda,
                                            localidad,
                                            "aire acondicionado",
                                            it
                                        )
                                        guardar_dado_datos()
                                    })

                            }
                        }
                    }
                }

            }

        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    @Composable
    fun expandido_wrap_socio_atributos(
        id_tienda: String, localida: String,
        aforo_max: String,
        viewModelFiltros: viewmodel_eres_socio,
        expandido: Boolean,
        onClickExpand: () -> Unit,
        guardar_dado_datos: (String) -> Unit
    ) {

        var aforoEditado by remember(aforo_max) {
            mutableStateOf(aforo_max)
        }

        val hayCambios by remember(aforo_max, aforoEditado) {
            mutableStateOf(aforoEditado.trim() != aforo_max.trim())
        }

        ConstraintLayout(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .padding(horizontal = 10.dp, vertical = 15.dp)
        ) {
            val (contenido) = createRefs()

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(15.dp),
                modifier = Modifier.constrainAs(contenido) {
                    top.linkTo(parent.top)
                    bottom.linkTo(parent.bottom)
                }
            ) {
                item {
                    AnimatedContent(
                        targetState = expandido,
                        transitionSpec = { fadeIn() togetherWith fadeOut() }
                    ) { estado ->

                        // 🔽 MODO COLAPSADO
                        if (!estado) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable(
                                        indication = null,
                                        interactionSource = remember { MutableInteractionSource() }
                                    ) { onClickExpand() }
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    texto_generico_one_line(
                                        "Aforo máximo de personas:",
                                        style = MaterialTheme.typography.bodyMedium
                                    )

                                    Image(
                                        painter = painterResource(R.drawable.icon_aforo),
                                        contentDescription = null,
                                        modifier = Modifier.size(22.dp)
                                    )

                                    texto_generico_one_line(
                                        "$aforo_max personas",
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }
                            }

                        } else {

                            // 🔼 MODO EXPANDIDO
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .fillParentMaxWidth(),

                                verticalArrangement = Arrangement.spacedBy(12.dp)

                            ) {

                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable(
                                            indication = null,
                                            interactionSource = remember { MutableInteractionSource() }
                                        ) { onClickExpand() },
                                    contentAlignment = Alignment.Center
                                ) {
                                    texto_generico_one_line(
                                        "Aforo máximo de personas",
                                        style = MaterialTheme.typography.titleLarge
                                    )
                                }


                                texto_generico_multilinea(
                                    "Indica la cantidad máxima de personas permitidas dentro del establecimiento.",
                                    style = MaterialTheme.typography.bodyMedium
                                )

                                custom_texFiel(
                                    value = aforoEditado,
                                    onValueChange = { nuevo ->
                                        // 🔢 Solo números
                                        if (nuevo.all { it.isDigit() }) {
                                            aforoEditado = nuevo
                                        }
                                    },
                                    labelText = "Aforo máximo de personas",
                                    placeholderText = "Ejemplo: 50",
                                    keyboardOptions = KeyboardOptions(
                                        keyboardType = KeyboardType.Number
                                    )
                                )

                                // 💾 BOTÓN SOLO SI HAY CAMBIOS
                                if (hayCambios && aforoEditado.isNotBlank()) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(CircleShape)
                                            .background(MaterialTheme.colorScheme.primary)
                                            .padding(vertical = 12.dp)
                                            .clickable {
                                                guardar_dado_datos(aforoEditado)
                                                onClickExpand()
                                                viewModelFiltros.guardar_aforo(
                                                    aforoEditado,
                                                    id_tienda,
                                                    localida
                                                )
                                            },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        texto_generico_one_line(
                                            "Guardar cambios",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = Color.White
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }


    @Composable
    fun campos_atributos(
        tipo: String,
        icono: Int,
        enable: Boolean,
        cambiar_valor: (Boolean) -> Unit
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(5.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = icono,
                contentDescription = null,
                modifier = Modifier
                    .clip(CircleShape)
                    .size(40.dp),
                contentScale = ContentScale.Crop,
                placeholder = painterResource(R.drawable.cargando_img_categorias),
                error = painterResource(R.drawable.cargando_img_categorias)
            )
            texto_generico_one_line(
                tipo.capitalizeFirst(),
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(modifier = Modifier.weight(1f))
            Switch(
                checked = enable,
                onCheckedChange = {
                    cambiar_valor(it)
                }, colors = SwitchDefaults.colors(
                    checkedThumbColor = MaterialTheme.colorScheme.primary,
                    checkedTrackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                    uncheckedThumbColor = MaterialTheme.colorScheme.outline,
                    uncheckedTrackColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                )
            )
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    @Composable
    fun verificar_activo_pago(
        iduser: String,
        logo: Int,
        nombre_metodo: String,
        enable: Boolean,
        numero_metodo_param: String,
        titular_param: String,
        qr_metodo_pago: String,
        cambiar_valor: (Boolean) -> Unit,
        cambiar_datos_internos_pagos: (
            titular: String,
            numero: String,
            qrResultado: Uri
        ) -> Unit
    ) {

        var mostrarCampos by remember { mutableStateOf(false) }
        var expandirImg by remember { mutableStateOf(false) }
        var valorImgCompleta by remember { mutableStateOf("") }

        // ───── TEXTOS ─────
        var numeroMetodo by remember { mutableStateOf(numero_metodo_param) }
        var titularMetodo by remember { mutableStateOf(titular_param) }

        val numeroOriginal = remember { numero_metodo_param }
        val titularOriginal = remember { titular_param }

        // ───── QR ESTADOS ─────
        val qrOriginalUrl = remember { qr_metodo_pago }
        var qrSeleccionada by remember { mutableStateOf<Uri?>(null) }
        var qrVacio by remember { mutableStateOf(false) }

        // ───── IMAGE PICKER ─────
        val imagePicker = rememberLauncherForActivityResult(
            ActivityResultContracts.GetContent()
        ) { uri ->
            uri?.let {
                qrSeleccionada = it
                qrVacio = false
            }
        }

        // ───── RESULTADO FINAL (CLAVE 🔥) ─────
        val qrResultado: Uri by remember {
            derivedStateOf {
                when {
                    qrSeleccionada != null -> qrSeleccionada!!
                    qrVacio -> Uri.EMPTY
                    qrOriginalUrl.isNotEmpty() -> Uri.parse(qrOriginalUrl)
                    else -> Uri.EMPTY
                }
            }
        }

        // ───── DETECTAR CAMBIOS ─────
        val hayCambios by remember {
            derivedStateOf {
                numeroMetodo != numeroOriginal ||
                        titularMetodo != titularOriginal ||
                        qrSeleccionada != null ||
                        qrVacio
            }
        }

        Column {

            // ───────── HEADER ─────────
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 10.dp)
            ) {

                Image(
                    painter = painterResource(id = logo),
                    contentDescription = null,
                    modifier = Modifier
                        .size(45.dp)
                        .clip(CircleShape)
                )

                spacer_horizonta(10.dp)

                textoMetodoPago(true, nombre_metodo.capitalizeFirst()) {
                    if (enable) mostrarCampos = !mostrarCampos
                }

                Spacer(modifier = Modifier.weight(1f))

                Switch(
                    checked = enable,
                    onCheckedChange = {
                        cambiar_valor(it)
                        if (!it) mostrarCampos = false
                    }, colors = SwitchDefaults.colors(
                        checkedThumbColor = MaterialTheme.colorScheme.primary,
                        checkedTrackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                        uncheckedThumbColor = MaterialTheme.colorScheme.outline,
                        uncheckedTrackColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                    )
                )
            }

            // ───────── CONTENIDO ─────────
            AnimatedVisibility(mostrarCampos) {

                Column {

                    spacer_vertical(7.dp)

                    texto_generico_multilinea(
                        "Completa el titular, número y QR. Solo se mostrarán tus iniciales.",
                        style = MaterialTheme.typography.bodySmall
                    )

                    spacer_vertical(7.dp)

                    LazyRow {

                        // ── CAMPOS ──
                        item {
                            Column(
                                modifier = Modifier
                                    .padding(horizontal = 10.dp)
                                    .width(280.dp)
                            ) {

                                custom_texFiel(
                                    value = numeroMetodo,
                                    onValueChange = { numeroMetodo = it },
                                    labelText = "Número de $nombre_metodo",
                                    placeholderText = "Número de $nombre_metodo",
                                    keyboardOptions = KeyboardOptions(
                                        keyboardType = KeyboardType.Number
                                    )
                                )

                                custom_texFiel(
                                    value = titularMetodo,
                                    onValueChange = { titularMetodo = it },
                                    labelText = "Titular de $nombre_metodo",
                                    placeholderText = "Titular de $nombre_metodo"
                                )
                            }
                        }

                        // ── QR ──
                        item {
                            Box(
                                modifier = Modifier
                                    .size(150.dp)
                                    .clip(RoundedCornerShape(20.dp))
                                    .clickable { imagePicker.launch("image/*") }
                            ) {

                                val imagenActual = when {
                                    qrSeleccionada != null -> qrSeleccionada
                                    qrVacio -> null
                                    else -> qrOriginalUrl
                                }

                                AsyncImage(
                                    model = imagenActual,
                                    contentDescription = null,
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop,
                                    placeholder = painterResource(R.drawable.cargando_img_categorias),
                                    error = painterResource(R.drawable.cargando_img_categorias)
                                )

                                // 🔍 EXPANDIR
                                if (imagenActual != null) {
                                    Icon(
                                        imageVector = Icons.Default.OpenInFull,
                                        contentDescription = null,
                                        modifier = Modifier
                                            .align(Alignment.BottomStart)
                                            .padding(6.dp)
                                            .size(20.dp)
                                            .background(
                                                Color.Black.copy(alpha = 0.5f),
                                                CircleShape
                                            )
                                            .padding(3.dp)
                                            .clickable {
                                                valorImgCompleta =
                                                    qrSeleccionada?.toString() ?: qrOriginalUrl
                                                expandirImg = true
                                            },
                                        tint = Color.White
                                    )
                                }

                                // ❌ ELIMINAR
                                if (qrSeleccionada != null || qrOriginalUrl.isNotEmpty()) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = null,
                                        modifier = Modifier
                                            .align(Alignment.TopEnd)
                                            .padding(6.dp)
                                            .size(20.dp)
                                            .background(
                                                Color.Black.copy(alpha = 0.5f),
                                                CircleShape
                                            )
                                            .padding(3.dp)
                                            .clickable {
                                                qrSeleccionada = null
                                                qrVacio = true
                                            },
                                        tint = Color.White
                                    )
                                }

                                // ↩ REGRESAR
                                if (qrSeleccionada != null || qrVacio) {
                                    Icon(
                                        imageVector = Icons.Default.Undo,
                                        contentDescription = null,
                                        modifier = Modifier
                                            .align(Alignment.BottomEnd)
                                            .padding(6.dp)
                                            .size(20.dp)
                                            .background(
                                                Color.Black.copy(alpha = 0.5f),
                                                CircleShape
                                            )
                                            .padding(3.dp)
                                            .clickable {
                                                qrSeleccionada = null
                                                qrVacio = false
                                            },
                                        tint = Color.White
                                    )
                                }
                            }
                        }
                    }

                    spacer_vertical(10.dp)

                    // ───────── GUARDAR ─────────
                    if (hayCambios) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary)
                                .padding(vertical = 10.dp)
                                .clickable {
                                    cambiar_datos_internos_pagos(
                                        titularMetodo,
                                        numeroMetodo,
                                        qrResultado // 🔥 SIEMPRE LLEGA
                                    )
                                    mostrarCampos = false
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            texto_generico_one_line("Guardar cambios")
                        }
                    }
                }
            }
        }

        // ───────── ZOOM ─────────
        if (expandirImg && valorImgCompleta.isNotEmpty()) {
            ZoomableGalleryFullScreen(
                iduser,
                compartir_promocion(),
                imagenes = listOf(valorImgCompleta),
                startIndex = 0,
                onDismiss = { expandirImg = false }
            )
        }
    }


    @Composable
    fun verificar_activo_contacto(
        context: Context,
        numero_obtenido: String,
        link_perfil: String,
        nombre_user_perfil: String,
        logo: Int,
        nombre_metodo: String,
        enable: Boolean,
        cambiar_valor: (Boolean) -> Unit,
        cambios_realizados: (links: String, nombre: String, numero: String) -> Unit
    ) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager


        var mostrarCampos by remember { mutableStateOf(false) }

        // 🔹 Estados editables
        var numero by remember { mutableStateOf(numero_obtenido) }
        var link_de_perfil by remember { mutableStateOf(link_perfil) }
        var nombre_perfil by remember { mutableStateOf(nombre_user_perfil) }

        // 🔹 Valores originales (para comparar)
        val numeroOriginal = remember { numero_obtenido }
        val linkOriginal = remember { link_perfil }
        val nombreOriginal = remember { nombre_user_perfil }

        // 🔹 Detectar cambios reales según tipo
        val hayCambios by remember {
            derivedStateOf {
                if (nombre_metodo == "whatsapp" || nombre_metodo == "llamada") {
                    numero != numeroOriginal
                } else {
                    link_de_perfil != linkOriginal ||
                            nombre_perfil != nombreOriginal
                }
            }
        }

        Column {

            // ───────── HEADER ─────────
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 10.dp)
            ) {

                Image(
                    painter = painterResource(id = logo),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(45.dp)
                        .clip(CircleShape)
                )

                spacer_horizonta(10.dp)

                textoMetodoPago(
                    true,
                    nombre_metodo = nombre_metodo.capitalizeFirst(),
                    onClick = {
                        if (enable) mostrarCampos = !mostrarCampos
                    }
                )

                Spacer(modifier = Modifier.weight(1f))

                Switch(
                    checked = enable,
                    onCheckedChange = { value ->
                        cambiar_valor(value)
                        if (!value) mostrarCampos = false
                    },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = MaterialTheme.colorScheme.primary,
                        checkedTrackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                        uncheckedThumbColor = MaterialTheme.colorScheme.outline,
                        uncheckedTrackColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                    )
                )
            }

            // ───────── CONTENIDO EXPANDIBLE ─────────
            AnimatedVisibility(mostrarCampos) {

                Column {

                    spacer_vertical(7.dp)

                    texto_generico_multilinea(
                        "Completa tus métodos de contacto para que tus clientes te contacten de forma rápida y sencilla.",
                        style = MaterialTheme.typography.bodySmall
                    )

                    spacer_vertical(7.dp)

                    Column() {

                        // ───── WHATSAPP / LLAMADA ─────
                        if (nombre_metodo == "whatsapp" || nombre_metodo == "llamada") {

                            Column(
                                modifier = Modifier
                                    .padding(horizontal = 10.dp)
                                    .width(280.dp)
                            ) {

                                custom_texFiel(
                                    value = numero,
                                    onValueChange = { numero = it },
                                    labelText = "Número de $nombre_metodo",
                                    placeholderText = "Número de $nombre_metodo",
                                    keyboardOptions = KeyboardOptions(
                                        keyboardType = KeyboardType.Number
                                    )
                                )
                            }

                        }
                        // ───── REDES SOCIALES ─────
                        else {

                            Column(
                                modifier = Modifier
                                    .padding(horizontal = 10.dp)
                                    .fillMaxWidth()
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Box(
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        custom_texFiel(
                                            value = link_de_perfil,
                                            onValueChange = { link_de_perfil = it },
                                            labelText = "Link de perfil de $nombre_metodo",
                                            placeholderText = "Link de perfil de $nombre_metodo",
                                        )
                                    }

                                    pegar_porta_papeles(
                                        clipboard,
                                        context
                                    ) { it ->
                                        link_de_perfil = it
                                    }
                                }
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Box(modifier = Modifier.weight(1f)) {
                                        custom_texFiel(
                                            value = nombre_perfil,
                                            onValueChange = { nombre_perfil = it },
                                            labelText = "Usuario de $nombre_metodo",
                                            placeholderText = "Usuario de $nombre_metodo",
                                        )
                                    }
                                    pegar_porta_papeles(clipboard, context, { it ->
                                        nombre_perfil = it
                                    })
                                }
                            }

                        }
                    }

                    spacer_vertical(10.dp)

                    // ───────── BOTÓN GUARDAR SOLO SI HAY CAMBIOS ─────────
                    if (hayCambios) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary)
                                .padding(vertical = 10.dp)
                                .clickable(
                                    indication = null,
                                    interactionSource = remember { MutableInteractionSource() }
                                ) {
                                    cambios_realizados(
                                        link_de_perfil,
                                        nombre_perfil,
                                        numero
                                    )
                                    mostrarCampos = false
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            texto_generico_one_line(
                                "Guardar cambios",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }

                        spacer_vertical(5.dp)
                    }
                }
            }
        }
    }


    @Composable
    fun textoMetodoPago(
        mostrara_subrallado: Boolean,
        nombre_metodo: String,
        onClick: (() -> Unit)? = null
    ) {
        val esInteractivo = nombre_metodo.equals("yape", true) ||
                nombre_metodo.equals("plin", true) || nombre_metodo.equals("whatsapp", true) ||
                nombre_metodo.equals("tiktok", true) || nombre_metodo.equals("llamada", true) ||
                nombre_metodo.equals("instagram", true) || nombre_metodo.equals(
            "facebook",
            true
        ) || nombre_metodo.equals("sitio web", true)

        texto_generico_one_line(
            nombre_metodo.capitalizeFirst(),
            style = MaterialTheme.typography.bodyMedium.copy(

                textDecoration = if (esInteractivo && mostrara_subrallado)
                    TextDecoration.Underline
                else
                    TextDecoration.None
            ), color = if (esInteractivo && mostrara_subrallado)
                MaterialTheme.colorScheme.primary
            else
                Color.White,
            modifier = if (esInteractivo && onClick != null) {
                Modifier.clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() }) { onClick() }
            } else {
                Modifier
            }
        )
    }

    @Composable
    fun contacto_y_redes(i: metodo_contacto_tienda) {

        val tieneContactosActivos =
            i.llamada.estado ||
                    i.whatsapp.estado ||
                    i.facebook.estado ||
                    i.instagram.estado ||
                    i.tiktok.estado ||
                    i.sitio_web.estado

        if (tieneContactosActivos) {

            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (i.llamada.estado) metodoItem(R.drawable.llamada_icon)
                if (i.whatsapp.estado) metodoItem(R.drawable.whatsapp_icon)
                if (i.facebook.estado) metodoItem(R.drawable.facebook_icon)
                if (i.instagram.estado) metodoItem(R.drawable.instagram_icon)
                if (i.tiktok.estado) metodoItem(R.drawable.tik_tok_icon)
                if (i.sitio_web.estado) metodoItem(R.drawable.web_icon)
            }

        } else {

            texto_generico_one_line(
                "Ningún método de contacto activo",
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = Color.Gray
                )
            )
        }
    }

    @Composable
    fun metodos_de_pago(i: modelo_pagos_tienda) {
        val tieneMetodosActivos =
            i.yape.enable ||
                    i.plin.enable ||
                    i.agora.enable ||
                    i.efectivo.enable ||
                    i.visa_mastercard.enable

        if (tieneMetodosActivos) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (i.yape.enable) metodoItem(R.drawable.yape_logo)
                if (i.plin.enable) metodoItem(R.drawable.logo_plin)
                if (i.agora.enable) metodoItem(R.drawable.logo_agora)
                if (i.efectivo.enable) metodoItem(R.drawable.efectivo_logo)
                if (i.visa_mastercard.enable) {
                    metodoItem(R.drawable.visa_logo)
                    metodoItem(R.drawable.master_car_logo)
                }
            }

        } else {

            texto_generico_one_line(
                "Ningún método de pago activo",
                style = MaterialTheme.typography.bodyMedium, color = Color.White
            )
        }


    }

    @Composable
    fun servicios_comodidades(lista: List<servicio_comodidad>) {

        val activos = lista.filter { it.estado }

        if (activos.isNotEmpty()) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                activos.forEach { servicio ->
                    iconoServicio(servicio.nombre)?.let { icono ->
                        metodoItem(icono)
                    }
                }
            }
        } else {
            texto_generico_one_line(
                "Ninguna comodidad activa",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White
            )
        }
    }

    fun iconoServicio(nombre: String): Int? {
        return when (nombre.lowercase()) {
            "zona expandida" -> R.drawable.icon_zona_expandida
            "wifi" -> R.drawable.icon_wifi
            "servicios higenicos" -> R.drawable.icon_servicios_higenicos
            "camaras de seguridad" -> R.drawable.icon_seguridad
            "sala de espera" -> R.drawable.icon_sala_de_espera
            "sala de juegos" -> R.drawable.icon_sala_para_ninos
            "mesa para niños" -> R.drawable.icon_mesa_para_ninos
            "ingreso con mascotas" -> R.drawable.icon_ingreso_animales
            "estacionamiento" -> R.drawable.icon_estacionamiento
            "enchufe" -> R.drawable.icon_enchufa
            "aire acondicionado" -> R.drawable.icon_aire_acondicionado
            else -> null
        }
    }


    @Composable
    fun metodoItem(icono: Int) {
        Image(
            painter = painterResource(id = icono),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(28.dp)
                .clip(CircleShape)
        )

    }

    @Composable
    fun pegar_porta_papeles(
        clipboard: ClipboardManager,
        context: Context,
        datos_pegado: (String) -> Unit
    ) {

        Box(
            modifier = Modifier
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary)
                .size(45.dp)
                .clickable() {
                    val clipData = clipboard.primaryClip
                    if (clipData != null && clipData.itemCount > 0) {
                        val text =
                            clipData.getItemAt(0)
                                .coerceToText(context)
                                .toString()
                        datos_pegado(text)

                    } else {
                        Toast
                            .makeText(
                                context,
                                "El portapapeles está vacío",
                                Toast.LENGTH_SHORT
                            )
                            .show()
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(R.drawable.pegar_portapales_webp),
                contentDescription = "",
                modifier = Modifier.size(25.dp)
            )
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    @Composable
    fun expandibles_wrapp_socio_geinzz_horario_atencion(
        tick: Long,
        viewModelFiltros: viewModel_filtado_tiendas,
        dia: String,
        isConnected: Boolean,
        viewmodel: viewmodel_eres_socio,
        expandido: Boolean,
        datos: datos_tienda,
        onClickExpand: () -> Unit,
        sin_conexion: () -> Unit,
        campos_vacios_o_incompletos: () -> Unit,
        error_hoario: (String) -> Unit
    ) {


        ConstraintLayout(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .padding(horizontal = 10.dp, vertical = 15.dp)
        ) {

            val (texto, btn) = createRefs()

            Column(
                modifier = Modifier.constrainAs(texto) {
                    top.linkTo(parent.top)
                    bottom.linkTo(parent.bottom)
                }

            ) {

                AnimatedContent(
                    targetState = expandido, transitionSpec = {
                        fadeIn() togetherWith fadeOut()
                    }) { estado ->

                    if (!estado) {
                        Row(
                            modifier = Modifier.clickable(
                                indication = null,
                                interactionSource = remember { MutableInteractionSource() }) { onClickExpand() },
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            texto_generico_one_line(
                                "Horario de hoy $dia :  ",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            retornar_color_estado_tienda_Box(
                                "",
                                viewModelFiltros.horariosTiendas.collectAsState().value[datos.id_tienda]
                                    ?: HorarioDia_box(),
                                tick,
                                true,
                                { color, txt -> })
                        }

                    } else {
                        HorarioSemanal123(
                            "todos",
                            id_tienda = datos.id_tienda,
                            tick = tick,
                            viewModelFiltros = viewModelFiltros,
                            isConnected = isConnected,
                            horario = datos.horario_tiendaMap,
                            cerrar_tienda = { nombre_dia, motivo_cierre, lista ->
                                viewmodel.cambiar_cerrado(
                                    datos.id_tienda, nombre_dia, motivo_cierre, lista
                                )
                            },
                            abrir_tienda = { dia, lista_horarios ->
                                viewmodel.cambiar_abierto(
                                    datos.id_tienda, dia, lista_horarios
                                )
                            },
                            error_sin_internet = {
                                sin_conexion()
                            },
                            onclick_expand = { onClickExpand() },
                            error_campos_incompletos = { campos_vacios_o_incompletos() },
                            { valor ->
                                error_hoario(valor)
                            },
                            shadow_top_filtrado_v1,
                            shadow_botonm_filtrado_v1
                        )
                    }
                }

            }
        }
    }

    @Composable
    fun expandible_wrapp_cambiar_subcateogira_negocio(
        subcateogira: List<String>,
        estado_Carga_subacategoria: viewmodel_eres_socio.Estado_carga_subcategoiras,
        categoria: String, expandido: Boolean, onClickExpand: () -> Unit,
        actualziar_campos: (lista: List<String>) -> Unit
    ) {
        var lista_actualizada by remember(subcateogira) {
            mutableStateOf(subcateogira)
        }
        val hayCambios by remember(subcateogira, lista_actualizada) {
            derivedStateOf {
                subcateogira.map { it.lowercase() }.toSet() !=
                        lista_actualizada.map { it.lowercase() }.toSet()
            }
        }
        ConstraintLayout(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .animateContentSize()
                .padding(horizontal = 10.dp, vertical = 15.dp)
        ) {
            val (texto, btn) = createRefs()

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(15.dp),
                modifier = Modifier.constrainAs(texto) {
                    top.linkTo(parent.top)
                    bottom.linkTo(parent.bottom)
                }

            ) {
                item {

                    AnimatedContent(
                        targetState = expandido, transitionSpec = {
                            fadeIn() togetherWith fadeOut()
                        }) { estado ->
                        if (!estado) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable(
                                        indication = null,
                                        interactionSource = remember { MutableInteractionSource() }) {
                                        onClickExpand()
                                    }) {

                                texto_generico_one_line(
                                    "Categoria y subcategoiras : ${categoria.capitalizeFirst()} ${
                                        getCategoriaIcon(
                                            categoria
                                        )
                                    }",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }

                        } else {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .fillParentMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(10.dp),
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable(
                                            indication = null,
                                            interactionSource = remember { MutableInteractionSource() }) {
                                            onClickExpand()
                                        }, contentAlignment = Alignment.Center
                                ) {
                                    texto_generico_one_line(
                                        "Categoria y subcategoiras",
                                        style = MaterialTheme.typography.titleLarge
                                    )
                                }
                                spacer_vertical(5.dp)
                                texto_generico_multilinea(
                                    "Actualmente estas en $categoria y en la subcategorias :${
                                        subcateogira.joinToString(
                                            ", "
                                        ) { it.capitalizeFirst() }
                                    }"
                                )
                                texto_generico_multilinea(
                                    "Actualiza en tiempo real tus subcategorías para que los clientes identifiquen rápidamente los productos o servicios que ofreces.",
                                    style = MaterialTheme.typography.bodyMedium
                                )

                                when (estado_Carga_subacategoria) {

                                    viewmodel_eres_socio.Estado_carga_subcategoiras.cagando -> {
                                        Box(
                                            modifier = Modifier.fillMaxWidth(),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            CircularProgressIndicator()
                                        }
                                    }

                                    viewmodel_eres_socio.Estado_carga_subcategoiras.empty,
                                    viewmodel_eres_socio.Estado_carga_subcategoiras.idle -> {
                                        Text("No hay subcategorías disponibles")
                                    }

                                    is viewmodel_eres_socio.Estado_carga_subcategoiras.succes -> {
                                        val lista = estado_Carga_subacategoria.lista_datos

                                        chips_subcategorias_negocio(
                                            subcategorias_usuario = subcateogira,
                                            lista_db = lista,
                                            onSeleccionCambia = { nuevaLista ->
                                                lista_actualizada = nuevaLista
                                            }
                                        )

                                        Spacer(modifier = Modifier.height(8.dp))
                                        if (hayCambios) {

                                            Button(
                                                onClick = {
                                                    actualziar_campos(lista_actualizada)
                                                },
                                                modifier = Modifier.fillMaxWidth()
                                            ) {
                                                texto_generico_one_line(
                                                    "Guardar cambios",
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    color = Color.White
                                                )
                                            }

                                            texto_generico_multilinea(
                                                "Al guardar los cambios, asegúrate de actualizar tu descripción o agregar nuevo contenido para que el asistente de WhatsApp refleje en tiempo real lo más reciente de tu negocio.",
                                                style = MaterialTheme.typography.labelSmall
                                            )
                                        }
                                    }

                                    else -> {}
                                }


                                spacer_vertical(7.dp)

                            }
                        }
                    }
                }
            }
        }
    }

    @Composable
    fun expandible_wrapp_ubicacion_direccion_referencia(
        context: Context,
        expandido: Boolean,
        direccion: String,
        ref: String,
        lat: Double,
        long: Double,
        onClickExpand: () -> Unit,
        actualiza_direccion: (String) -> Unit,
        actualiza_referencia: (String) -> Unit,
        onTocandoMapa: (Boolean) -> Unit,
        cambiar_lat_lng: (Double?, Double?) -> Unit
    ) {
        val mapViewState = remember { mutableStateOf<MapView?>(null) }
        val managerLauncher = remember { mutableStateOf<PointAnnotationManager?>(null) }
        val managerNuevoPunto = remember { mutableStateOf<PointAnnotationManager?>(null) }

        val fusedLocationClient = remember {
            LocationServices.getFusedLocationProviderClient(context)
        }

        var tienePermiso by remember {
            mutableStateOf(
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
            )
        }

        // ── Estado del GPS (se refresca cuando el switch se activa) ─────────────
        var gpsActivo by remember {
            mutableStateOf(verificarUbiActiva(context))
        }

        var estadoSwitch by remember { mutableStateOf(false) }
        var tocandoMapa by remember { mutableStateOf(false) }
        var latNueva by remember { mutableStateOf<Double?>(null) }
        var lngNueva by remember { mutableStateOf<Double?>(null) }
        var mapaListo by remember { mutableStateOf(false) }
        var mostrarMapa by remember { mutableStateOf(false) }

        // ── LAUNCHER permiso de ubicación ───────────────────────────────────────
        val permisoLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestPermission()
        ) { granted ->
            if (granted) {
                tienePermiso = true
                gpsActivo = verificarUbiActiva(context)
            } else {
                tienePermiso = false
                estadoSwitch = false
                mapaListo = false
                mostrarMapa = false
                latNueva = null
                lngNueva = null
                Toast.makeText(context, "Se necesita permiso de ubicación", Toast.LENGTH_SHORT).show()
            }
        }

        // ── LAUNCHER diálogo de Google para activar GPS ──────────────────────────
        val launcher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.StartIntentSenderForResult()
        ) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                Log.d("GPS", "✅ El usuario activó el GPS")
                gpsActivo = true
            } else {
                Log.d("GPS", "❌ El usuario canceló el diálogo de ubicación")
                gpsActivo = false
            }
        }

        var direccion_original by remember { mutableStateOf(direccion) }
        var referencia_original by remember { mutableStateOf(ref) }
        var direccion_var by remember(direccion) { mutableStateOf(direccion) }
        var referencia_var by remember(ref) { mutableStateOf(ref) }

        val hayCambioDireccion by remember(direccion_original, direccion_var) {
            derivedStateOf { direccion_original.trim() != direccion_var.trim() }
        }
        val hayCambioReferencia by remember(referencia_original, referencia_var) {
            derivedStateOf { referencia_original.trim() != referencia_var.trim() }
        }

        LaunchedEffect(estadoSwitch) {
            if (estadoSwitch && !tienePermiso) {
                permisoLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            }
            if (estadoSwitch) {
                gpsActivo = verificarUbiActiva(context)
            }
        }

        LaunchedEffect(mapaListo) {
            if (mapaListo) {
                delay(3000)
                mostrarMapa = true
            }
        }

        ConstraintLayout(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .animateContentSize()
                .padding(horizontal = 10.dp, vertical = 15.dp)
        ) {
            val (texto, btn) = createRefs()

            AnimatedContent(
                targetState = expandido,
                transitionSpec = { fadeIn() togetherWith fadeOut() }
            ) { estado ->

                if (!estado) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(
                                indication = null,
                                interactionSource = remember { MutableInteractionSource() }
                            ) { onClickExpand() }
                    ) {
                        texto_generico_one_line(
                            "Ubicacion : 📍 $direccion",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                } else {

                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable(
                                    indication = null,
                                    interactionSource = remember { MutableInteractionSource() }
                                ) { onClickExpand() },
                            contentAlignment = Alignment.Center
                        ) {
                            texto_generico_one_line(
                                "Ubicacion",
                                style = MaterialTheme.typography.titleLarge
                            )
                        }

                        texto_generico_multilinea(
                            "Si cambias de ubicación, actualiza tu dirección, referencia y coordenadas en tiempo real para que tus clientes siempre puedan encontrarte.",
                            style = MaterialTheme.typography.bodyMedium
                        )

                        spacer_vertical(5.dp)

                        custom_textField_150(
                            modifier = Modifier,
                            false,
                            rounder = 35,
                            value = direccion_var,
                            onValueChange = { direccion_var = it },
                            labelText = "Direccion del negocio",
                            placeholderText = "Direccion del negocio"
                        )

                        if (hayCambioDireccion) {
                            Button(
                                onClick = {
                                    actualiza_direccion(direccion_var.trim())
                                    direccion_original = direccion_var.trim()
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                texto_generico_one_line(
                                    "Guardar cambios",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color.White
                                )
                            }
                        }

                        custom_textField_150(
                            modifier = Modifier,
                            false,
                            rounder = 35,
                            value = referencia_var,
                            onValueChange = { referencia_var = it },
                            labelText = "Referencia del negocio",
                            placeholderText = "Referencia del negocio"
                        )

                        if (hayCambioReferencia) {
                            Button(
                                onClick = {
                                    actualiza_referencia(referencia_var.trim())
                                    referencia_original = referencia_var.trim()
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                texto_generico_one_line(
                                    "Guardar cambios",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color.White
                                )
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            texto_generico_one_line(
                                "Cambia tu punto",
                                style = MaterialTheme.typography.titleLarge
                            )
                            Switch(
                                checked = estadoSwitch,
                                onCheckedChange = { checked ->
                                    if (checked) {
                                        tienePermiso = ContextCompat.checkSelfPermission(
                                            context,
                                            Manifest.permission.ACCESS_FINE_LOCATION
                                        ) == PackageManager.PERMISSION_GRANTED
                                        gpsActivo = verificarUbiActiva(context)
                                        estadoSwitch = true
                                    } else {
                                        estadoSwitch = false
                                        latNueva = null
                                        lngNueva = null
                                        mapaListo = false
                                        mostrarMapa = false
                                    }
                                },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = MaterialTheme.colorScheme.primary,
                                    checkedTrackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                                    uncheckedThumbColor = MaterialTheme.colorScheme.outline,
                                    uncheckedTrackColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                                )
                            )
                        }

                        texto_generico_multilinea(
                            "📍 ¿Cambiaste de local? Actualiza tu punto en el mapa para que tus clientes siempre puedan encontrarte sin perderse.",
                            style = MaterialTheme.typography.bodyMedium
                        )

                        if (estadoSwitch && tienePermiso) {

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(250.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .pointerInput(Unit) {
                                        awaitPointerEventScope {
                                            while (true) {
                                                val event = awaitPointerEvent()
                                                val tocando = event.changes.any { it.pressed }
                                                tocandoMapa = tocando
                                                onTocandoMapa(tocando)
                                            }
                                        }
                                    }
                            ) {
                                MapboxMap(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(300.dp)
                                ) {
                                    MapStyle("mapbox://styles/benjaminlopez/cmm9c0hlt003901s54utw9p30")

                                    val estadoSwitchState = rememberUpdatedState(estadoSwitch)

                                    MapEffect(Unit) { mapView ->
                                        val mapboxMap = mapView.getMapboxMap()
                                        mapViewState.value = mapView

                                        managerLauncher.value = mapView.annotations.createPointAnnotationManager()
                                        managerNuevoPunto.value = mapView.annotations.createPointAnnotationManager()

                                        mapView.location.updateSettings {
                                            enabled = true
                                            pulsingEnabled = true
                                            puckBearingEnabled = true
                                            puckBearing = PuckBearing.HEADING
                                            locationPuck = createDefault2DPuck(withBearing = true)
                                        }

                                        fun colocarPinNegocio(punto: Point) {
                                            managerLauncher.value?.deleteAll()
                                            val bitmap = crearBitmapPin()
                                            val imageId = "pin_negocio"
                                            mapboxMap.getStyle { s ->
                                                s.removeStyleImage(imageId)
                                                s.addImage(imageId, bitmap)
                                                managerLauncher.value?.create(
                                                    PointAnnotationOptions()
                                                        .withPoint(punto)
                                                        .withIconImage(imageId)
                                                        .withIconAnchor(IconAnchor.BOTTOM)
                                                        .withIconSize(1.2)
                                                        .withTextField("Tu negocio 📍")
                                                        .withTextOffset(listOf(0.0, -4.5))
                                                        .withTextSize(11.0)
                                                        .withTextColor(Color.Black.hashCode())
                                                        .withTextHaloColor(Color.White.hashCode())
                                                        .withTextHaloWidth(1.5)
                                                        .withTextAnchor(TextAnchor.BOTTOM)
                                                )
                                            }
                                        }

                                        fun colocarPinNuevoPunto(punto: Point) {
                                            managerNuevoPunto.value?.deleteAll()
                                            val bitmap = crearBitmapPin()
                                            val imageId = "pin_nuevo"
                                            mapboxMap.getStyle { s ->
                                                s.removeStyleImage(imageId)
                                                s.addImage(imageId, bitmap)
                                                managerNuevoPunto.value?.create(
                                                    PointAnnotationOptions()
                                                        .withPoint(punto)
                                                        .withIconImage(imageId)
                                                        .withIconAnchor(IconAnchor.BOTTOM)
                                                        .withIconSize(1.2)
                                                        .withTextField("Nuevo punto 🆕")
                                                        .withTextOffset(listOf(0.0, -4.5))
                                                        .withTextSize(11.0)
                                                        .withTextColor(Color.Black.hashCode())
                                                        .withTextHaloColor(Color.White.hashCode())
                                                        .withTextHaloWidth(1.5)
                                                        .withTextAnchor(TextAnchor.BOTTOM)
                                                )
                                            }
                                        }

                                        mapboxMap.addOnStyleLoadedListener {
                                            val puntoInicial = Point.fromLngLat(long, lat)
                                            mapboxMap.setCamera(
                                                CameraOptions.Builder()
                                                    .center(puntoInicial)
                                                    .zoom(15.0)
                                                    .build()
                                            )
                                            colocarPinNegocio(puntoInicial)
                                            mapaListo = true
                                        }

                                        mapboxMap.addOnMapClickListener { point ->
                                            if (!estadoSwitchState.value) return@addOnMapClickListener false
                                            val latClick = point.latitude()
                                            val lngClick = point.longitude()
                                            colocarPinNuevoPunto(point)
                                            latNueva = latClick
                                            lngNueva = lngClick
                                            false
                                        }
                                    }
                                }

                                // Overlay de carga
                                this@Column.AnimatedVisibility(
                                    visible = !mostrarMapa,
                                    modifier = Modifier.matchParentSize(),
                                    enter = fadeIn(),
                                    exit = fadeOut()
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .matchParentSize()
                                            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.85f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Column(
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            verticalArrangement = Arrangement.spacedBy(12.dp)
                                        ) {
                                            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                                            texto_generico_one_line(
                                                "Cargando mapa...",
                                                style = MaterialTheme.typography.bodyMedium
                                            )
                                        }
                                    }
                                }
                            }

                            // ── BOTÓN UBICARME: cambia según estado del GPS ──────────
                            Button(
                                onClick = {
                                    if (gpsActivo) {
                                        val mapView = mapViewState.value ?: return@Button
                                        val mapboxMap = mapView.getMapboxMap()
                                        mapView.location.updateSettings {
                                            enabled = true
                                            pulsingEnabled = true
                                            puckBearingEnabled = true
                                            puckBearing = PuckBearing.HEADING
                                            locationPuck = createDefault2DPuck(withBearing = true)
                                        }
                                        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                                            location?.let {
                                                val punto = Point.fromLngLat(it.longitude, it.latitude)
                                                mapboxMap.easeTo(
                                                    CameraOptions.Builder()
                                                        .center(punto)
                                                        .zoom(15.0)
                                                        .build(),
                                                    MapAnimationOptions.mapAnimationOptions { duration(600) }
                                                )
                                            }
                                        }
                                    } else {
                                        // GPS apagado → lanza diálogo de Google directamente
                                        verificarGPS(context, launcher)
                                    }
                                },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (gpsActivo)
                                        MaterialTheme.colorScheme.primary
                                    else
                                        MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
                                ),
                                border = if (!gpsActivo)
                                    BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                                else null
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    texto_generico_one_line(
                                        if (gpsActivo) "📍 Ubicarme"
                                        else "📡 Encender GPS para ubicarme",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = if (gpsActivo) Color.White
                                        else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                    )
                                }
                            }

                            // Coordenadas actuales
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                custom_textField_readonly(
                                    modifier = Modifier.weight(1f),
                                    rounder = 35,
                                    value = lat.toString(),
                                    labelText = "Lat actual",
                                    placeholderText = "Lat actual"
                                )
                                custom_textField_readonly(
                                    modifier = Modifier.weight(1f),
                                    rounder = 35,
                                    value = long.toString(),
                                    labelText = "Lng actual",
                                    placeholderText = "Lng actual"
                                )
                                Button(
                                    onClick = {
                                        val punto = Point.fromLngLat(long, lat)
                                        mapViewState.value?.getMapboxMap()?.easeTo(
                                            CameraOptions.Builder()
                                                .center(punto)
                                                .zoom(17.0)
                                                .build(),
                                            MapAnimationOptions.mapAnimationOptions { duration(600) }
                                        )
                                    }
                                ) {
                                    texto_generico_one_line(
                                        "Ir",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = Color.White
                                    )
                                }
                            }

                            // Nuevo punto seleccionado
                            if (latNueva != null && lngNueva != null) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    custom_textField_150(
                                        modifier = Modifier.weight(1f),
                                        false,
                                        rounder = 35,
                                        value = latNueva.toString(),
                                        onValueChange = { latNueva = it.toDoubleOrNull() ?: latNueva },
                                        labelText = "Lat nueva",
                                        placeholderText = "Lat nueva"
                                    )
                                    custom_textField_150(
                                        modifier = Modifier.weight(1f),
                                        false,
                                        rounder = 35,
                                        value = lngNueva.toString(),
                                        onValueChange = { lngNueva = it.toDoubleOrNull() ?: lngNueva },
                                        labelText = "Lng nueva",
                                        placeholderText = "Lng nueva"
                                    )
                                    Button(
                                        onClick = {
                                            val punto = Point.fromLngLat(lngNueva!!, latNueva!!)
                                            mapViewState.value?.getMapboxMap()?.easeTo(
                                                CameraOptions.Builder()
                                                    .center(punto)
                                                    .zoom(17.0)
                                                    .build(),
                                                MapAnimationOptions.mapAnimationOptions { duration(600) }
                                            )
                                        }
                                    ) {
                                        texto_generico_one_line(
                                            "Ir",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = Color.White
                                        )
                                    }
                                }

                                texto_generico_multilinea(
                                    "✏️ Recuerda actualizar tu dirección y referencia cada vez que muevas tu punto, así tu perfil siempre muestra datos correctos y tus clientes llegan sin confusiones.",
                                    style = MaterialTheme.typography.labelSmall
                                )

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Button(
                                        onClick = {
                                            managerNuevoPunto.value?.deleteAll()
                                            latNueva = null
                                            lngNueva = null
                                        },
                                        modifier = Modifier.weight(1f),
                                        colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                                    ) {
                                        texto_generico_one_line(
                                            "✕ Deseleccionar",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = Color.White
                                        )
                                    }

                                    Button(
                                        onClick = {
                                            val latGuardar = latNueva
                                            val lngGuardar = lngNueva
                                            managerNuevoPunto.value?.deleteAll()
                                            latNueva = null
                                            lngNueva = null
                                            cambiar_lat_lng(latGuardar, lngGuardar)
                                        },
                                        modifier = Modifier.weight(1f),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = MaterialTheme.colorScheme.primary
                                        )
                                    ) {
                                        texto_generico_one_line(
                                            "💾 Guardar",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = Color.White
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

        }
    }

}