package com.geinzz.geinzwork.ui.adapters.ui.pantallas.socios


import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.geinzz.geinzwork.R

import com.geinzz.geinzwork.data.model.datos_recarga
import com.geinzz.geinzwork.data.model.historial_recargas
import com.geinzz.geinzwork.data_store.data_store_localidad
import com.geinzz.geinzwork.herramientas_geinz.constantes.constante_abrir_navegador.openCustomTab
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.texto_generico_multilinea
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.texto_generico_one_line
import com.geinzz.geinzwork.ui.adapters.ui.dialog_general.spacer_horizonta
import com.geinzz.geinzwork.ui.adapters.ui.dialog_general.spacer_vertical
import com.geinzz.geinzwork.ui.adapters.ui.pantallas.bottom_sheet_general.bottom_sheet_creadtior_quees_geinzz
import com.geinzz.geinzwork.ui.adapters.ui.ui.theme.baners_geinz_work
import com.geinzz.geinzwork.utils.constantes.constantes.mostrarFechaDialog_horaDialog
import com.geinzz.geinzwork.utils.constantes.constantes.mostrarFechaDialog_horaDialog.obtenerFechaActual
import com.geinzz.geinzwork.utils.constantes.constantes.mostrarFechaDialog_horaDialog.obtenerHoraActual
import com.geinzz.geinzwork.utils.constantes.constantes_cobro_monedas.generarIdRecarga
import com.geinzz.geinzwork.viewModels.viewmodel_eres_socio
import com.geinzz.geinzwork.viewModels.viewmodel_recargas
import com.google.firebase.auth.FirebaseAuth

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun pantala_recarga(
    localida_user:String,
    viewmodel_paramo: viewmodel_eres_socio,
    nombre_tienda: String,
    localida_tienda: String,
    id_tienda: String,
    monedas_user: Int,
    cargando:(Boolean)-> Unit
) {
    val firebaseAuth = FirebaseAuth.getInstance()
    val context = LocalContext.current
    val listState = rememberLazyListState()
    val viewmodel_recarga_insta: viewmodel_recargas = viewModel()
    val targetAlpha = if (listState.canScrollForward) 1f else 0f
    val estado by viewmodel_paramo.estadoPaquetes.collectAsState()
    val alphaAnim by animateFloatAsState(
        targetValue = targetAlpha,
        animationSpec = tween(durationMillis = 500)
    )
    val uid_respald_user by data_store_localidad.get_uid_user(context).collectAsState(initial = "")

    val id_user = uid_respald_user.takeIf { it.isNotEmpty() } ?: firebaseAuth.currentUser?.uid
    ?: ""

    var abrir_para_que_monedas by remember { mutableStateOf(false) }
    var mostrarInfoCreditos by remember { mutableStateOf(true) }
    val enviar_webhook_culqui  by viewmodel_recarga_insta.enviar_webhook_culqui.collectAsState()

    LaunchedEffect(enviar_webhook_culqui) {
        Log.d("enviar_webhook_culqui","$enviar_webhook_culqui")
    }
    LaunchedEffect(Unit) {
        viewmodel_paramo.obtener_precios_paquetes()
    }
    Crossfade(targetState = estado) { curren_state ->

        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {

            when (curren_state) {
                is viewmodel_eres_socio.CargaPaquetesPago.Loading -> {
                    ShimmerImagenConMarca()
//                    cargando(true)
                }

                is viewmodel_eres_socio.CargaPaquetesPago.Success -> {
//                    cargando(false)
                    val datos = curren_state.datos // ✅ aquí no hay cast
                    LazyColumn(
                        state = listState,
                        verticalArrangement = Arrangement.spacedBy(30.dp),
                        modifier = Modifier.padding(top = 10.dp, start = 5.dp, end = 5.dp)
                    ) {
                        item {
                            Text(
                                fontFamily = baners_geinz_work,
                                text = "Potencia tu negocio con GEINZ Ads",
                                color = Color.White, fontSize = 25.sp, textAlign = TextAlign.Center
                            )
                            spacer_vertical(10.dp)
                            texto_generico_multilinea(
                                "¡Hola $nombre_tienda! \uD83C\uDFAF Descubre los planes de GEINZ y toma el control de tus promociones, notificaciones y beneficios",
                                MaterialTheme.typography.bodyMedium
                            )

                            if (mostrarInfoCreditos) {
                                Surface(
                                    onClick = { abrir_para_que_monedas=true},
                                    shape = RoundedCornerShape(24.dp),
                                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.10f),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 16.dp),
                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
                                ) {

                                    Box {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(16.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                                        ) {

                                            Column(
                                                modifier = Modifier.weight(1f),
                                                verticalArrangement = Arrangement.spacedBy(8.dp)
                                            ) {
                                                texto_generico_multilinea(
                                                    "¿Cómo funcionan los créditos Geinz?",
                                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                                )

                                                texto_generico_multilinea(
                                                    "Impulsa tu negocio en la plataforma usando créditos Geinz. Descubre cómo funcionan y qué beneficios.",
                                                    style = MaterialTheme.typography.labelSmall.copy(
                                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                                                    )
                                                )
                                            }

                                            Image(
                                                painter = painterResource(R.drawable.icon_monedas_3d),
                                                contentDescription = null,
                                                modifier = Modifier
                                                    .size(120.dp) // Ajustado un poco para que no choque con la X
                                                    .graphicsLayer(alpha = 0.9f),
                                                contentScale = ContentScale.Fit
                                            )
                                        }

                                        // 2. BOTÓN DE CERRAR (X) EN LA ESQUINA
                                        IconButton(
                                            onClick = { mostrarInfoCreditos = false },
                                            modifier = Modifier
                                                .align(Alignment.TopEnd)
                                                .padding(4.dp)
                                                .size(32.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Close,
                                                contentDescription = "Cerrar",
                                                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f),
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                        items(datos) { i ->
                            item_pantalla_recarga(i) { id_plan->
                                val url_pago="https://geinzwork.firebaseapp.com/pagos.html?orderId=${id_tienda}&plan=${id_plan}"
                                openCustomTab(context,url_pago)


//                                val datos_recarga = historial_recargas(
//                                    "recarga",
//                                    fecha = obtenerFechaActual(),
//                                    hora = obtenerHoraActual(),
//                                    id_recarga = generarIdRecarga(),
//                                    localidad_tienda = localida_tienda,
//                                    id_tienda = id_tienda,
//                                    nombre_tienda = nombre_tienda,
//                                    tipo = nombre,
//                                    monto = monedas,
//                                    precio_soles = montosoles,
//                                    yape = true,
//                                    plin = false,
//                                    estado = "Aceptado",
//                                    monto_posterior = monedas_user
//                                )
//                                viewmodel_recarga_insta.crear_cargo__compra_paquete(localida_user.lowercase(),id_tienda,monedas_user.toString(),monedas)
//                                viewmodel_recarga_insta.recargar_puntos(
//                                    i = datos_recarga,
//                                    id_user = id_user
//                                )
                            }
                        }
                        item { spacer_vertical(20.dp) }
                    }
                }

                is viewmodel_eres_socio.CargaPaquetesPago.Error -> {
//                    cargando(false)
                    val text = curren_state.txt // ✅ aquí tampoco hay cast
                    Text(text)
                    Log.d("obtenreodasr", text)
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(40.dp)
                    .align(Alignment.BottomCenter)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color.Black
                            )
                        )
                    )
                    .graphicsLayer { alpha = alphaAnim } // aplicamos el fade
            )
            if(abrir_para_que_monedas){
                bottom_sheet_creadtior_quees_geinzz{abrir_para_que_monedas=false}
            }
        }
    }



}

@Composable
fun item_pantalla_recarga(
    i: datos_recarga,
    plan_select: (plan: String) -> Unit
) {

    Column(
        modifier = Modifier
            .padding(horizontal = 15.dp)
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(Color(0xFF2B2B2B))
            .padding(horizontal = 25.dp, vertical = 20.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            texto_generico_one_line(i.nombre_plan, style = MaterialTheme.typography.titleLarge)
        }
        if (i.monedas_agregadas.equals("0")) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    texto_generico_one_line("Total:", style = MaterialTheme.typography.titleLarge)
                    spacer_horizonta(7.dp)
                    texto_generico_one_line(i.monedas, style = MaterialTheme.typography.titleLarge)
                    spacer_horizonta(3.dp)
                    Image(
                        painter = painterResource(R.drawable.icon_monedas_3d),
                        contentDescription = null,
                        modifier = Modifier.size(25.dp)
                    )
                }
            }
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Row(verticalAlignment = Alignment.CenterVertically) {

                        texto_generico_one_line(
                            i.monedas_inicial,
                            style = MaterialTheme.typography.titleLarge
                        )
                        spacer_horizonta(3.dp)
                        Image(
                            painter = painterResource(R.drawable.icon_monedas_3d),
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                }

            }
        }

        spacer_vertical(2.dp)
        texto_generico_one_line(i.descripcion)

        i.accesos.forEach { datos ->


            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = Color(0xFF2ECC71),
                    modifier = Modifier.size(18.dp)
                )

                Spacer(modifier = Modifier.width(6.dp))

                texto_generico_multilinea(
                    datos,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            // ✅ Si hay monedas agregadas, agregamos un Row con icono de regalo
        }
        if (!i.monedas_agregadas.equals("0")) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.CardGiftcard, // icono de regalo
                    contentDescription = null,
                    tint = Color(0xFFFFC107), // color dorado/amarillo
                    modifier = Modifier.size(18.dp)
                )

                Spacer(modifier = Modifier.width(6.dp))

                texto_generico_one_line(
                    "+${i.monedas_agregadas}",
                    style = MaterialTheme.typography.bodyMedium.copy(color = Color(0xFFFFC107))
                )
                spacer_horizonta(5.dp)
                Image(
                    painter = painterResource(R.drawable.icon_monedas_3d),
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                spacer_horizonta(5.dp)
                texto_generico_one_line(
                    "de regalo",
                    style = MaterialTheme.typography.bodyMedium.copy(color = Color(0xFFFFC107))
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {

                texto_generico_one_line(
                    "Total de recargar: ${i.monedas}",
                    style = MaterialTheme.typography.bodyMedium.copy(color = Color(0xFFFFC107))
                )
                spacer_horizonta(5.dp)
                Image(
                    painter = painterResource(R.drawable.icon_monedas_3d),
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )

            }
        }

        spacer_vertical(5.dp)
        Box(
            modifier = Modifier
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary)
                .clickable() {
                    plan_select(i.id_plan_select)
                }
                .fillMaxWidth(), contentAlignment = Alignment.Center
        ) {
            texto_generico_one_line(
                "Adquirir paquete: S/${i.precio_soles}.00",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(10.dp)
            )
        }
    }


}