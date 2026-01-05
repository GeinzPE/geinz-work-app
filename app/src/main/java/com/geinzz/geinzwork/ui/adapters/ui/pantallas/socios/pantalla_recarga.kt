package com.geinzz.geinzwork.ui.adapters.ui.pantallas.socios


import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.geinzz.geinzwork.R

import com.geinzz.geinzwork.data.model.datos_recarga
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.texto_generico_multilinea
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.texto_generico_one_line
import com.geinzz.geinzwork.ui.adapters.ui.dialog_general.spacer_horizonta
import com.geinzz.geinzwork.ui.adapters.ui.dialog_general.spacer_vertical
import com.geinzz.geinzwork.ui.adapters.ui.ui.theme.baners_geinz_work
import com.geinzz.geinzwork.viewModels.viewmodel_eres_socio

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun pantala_recarga(viewmodel: viewmodel_eres_socio,nombre_tienda:String) {
    val listState = rememberLazyListState()
    val targetAlpha = if (listState.canScrollForward) 1f else 0f
    val estado by viewmodel.estadoPaquetes.collectAsState()
    val alphaAnim by animateFloatAsState(
        targetValue = targetAlpha,
        animationSpec = tween(durationMillis = 500)
    )
    LaunchedEffect(Unit) {
        viewmodel.obtener_precios_paquetes()
    }
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {

        when (estado) {
            is viewmodel_eres_socio.CargaPaquetesPago.Loading -> {
                CircularProgressIndicator()
            }

            is viewmodel_eres_socio.CargaPaquetesPago.Success -> {
                val datos = (estado as viewmodel_eres_socio.CargaPaquetesPago.Success).datos
                LazyColumn(state = listState,verticalArrangement = Arrangement.spacedBy(30.dp), modifier = Modifier.padding(top = 10.dp, start = 5.dp, end = 5.dp)) {
                    item {
                        Text(
                            fontFamily = baners_geinz_work,
                            text = "Potencia tu negocio con GEINZ Ads",
                            color =Color.White
                            , fontSize = 25.sp, textAlign = TextAlign.Center
                        )
                        spacer_vertical(10.dp)
                        texto_generico_multilinea("¡Hola $nombre_tienda! \uD83C\uDFAF Descubre los planes de GEINZ y toma el control de tus promociones, notificaciones y beneficios",
                            MaterialTheme.typography.bodyMedium)
                    }
                    items(datos) { i ->
                        item_pantalla_recarga(i)
                    }
                    item { spacer_vertical(20.dp) }
                }
            }

            is viewmodel_eres_socio.CargaPaquetesPago.Error -> {
                val text = (estado as viewmodel_eres_socio.CargaPaquetesPago.Error).txt
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
    }


}

@Composable
fun item_pantalla_recarga(i: datos_recarga) {

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
        if(i.monedas_agregadas.equals("0") ){
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
                    modifier = Modifier.size(20.dp)
                )
            }
        }
        }else{
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Row(verticalAlignment = Alignment.CenterVertically) {

                        texto_generico_one_line(i.monedas_inicial, style = MaterialTheme.typography.titleLarge)
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

            // Row principal con check y texto
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
        Box(modifier = Modifier.clip(CircleShape).background(MaterialTheme.colorScheme.primary).fillMaxWidth(), contentAlignment = Alignment.Center) {
            texto_generico_one_line("Adquirir paquete: S/${i.precio_soles}.00", style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(10.dp))
        }
    }


}