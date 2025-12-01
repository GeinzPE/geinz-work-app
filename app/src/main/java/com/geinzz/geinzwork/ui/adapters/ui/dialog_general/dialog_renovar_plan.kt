package com.geinzz.geinzwork.ui.adapters.ui.dialog_general

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import coil3.request.error
import coil3.request.placeholder
import com.geinzz.geinzwork.R
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.texto_generico_one_line
import com.geinzz.geinzwork.ui.adapters.ui.ui.theme.baners_geinz_work
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.constantes_lista_localidades.FuenteControladaApp

data class descuentos(
    val meses: String,
    val descuento_off: String,
    val precio_anterior: String,
    val procentaje_ahorro: String,
    val porcentaje_int: Int,
)

val lista_descuentos = listOf(
    descuentos("1 mes", "", "", "", 0),
    descuentos("2 meses", "-10%off", "2000", "10%", 10),
    descuentos("3 meses", "-20%off", "3000", "20%", 20),
    descuentos("4 meses", "-30%off", "4000", "30%", 30)
)

@Composable
fun dialog_renovar_plan(saldo_disponible: String = "4000", ondimis: () -> Unit) {
    val infiniteTransition = rememberInfiniteTransition(label = "")
    var clikeado by remember { mutableStateOf(false) }
    val scale by infiniteTransition.animateFloat(
        initialValue = 0.9f,  // más pequeño al inicio
        targetValue = 1.0f,   // se expande un poco más
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 2800, // más lenta = más natural
                easing = FastOutSlowInEasing // respiración más orgánica
            ),
            repeatMode = RepeatMode.Reverse
        ), label = ""
    )
    var color_select by remember { mutableStateOf(Color(0xFF8700F3)) }
    var descuento_aplicado by remember { mutableStateOf("") }
    var precio_anteterir by remember { mutableStateOf("") }
    var desceunto_numero by remember { mutableStateOf(0.0) }
    var porcentaje_ahorro by remember { mutableStateOf("") }
    var almenos_uno_selecion by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = { ondimis() },
        confirmButton = {},
        dismissButton = {},
        text = {
            FuenteControladaApp {
                Column() {

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(90.dp)
                            .animateContentSize()       // 🔥 clave: el tamaño se anima al desaparecer
                    ) {
                        if (clikeado) {
                            // ⬇️ Esto ya NO debe estar dentro de AnimatedVisibility
                            Box(contentAlignment = Alignment.Center) {
                                Box(
                                    modifier = Modifier
                                        .size(100.dp)
                                        .graphicsLayer(
                                            scaleX = scale,
                                            scaleY = scale
                                        )
                                        .background(
                                            brush = Brush.radialGradient(
                                                colors = listOf(
                                                    color_select.copy(alpha = 0.7f),
                                                    Color.Transparent
                                                ),
                                            ),
                                            shape = RoundedCornerShape(200.dp)
                                        )
                                )

                                AsyncImage(
                                    model = ImageRequest.Builder(LocalContext.current)
                                        .data(R.drawable.logo_geinz_500x500)
                                        .placeholder(R.drawable.cargando_img_categorias)
                                        .error(R.drawable.cargando_img_categorias)
                                        .build(),
                                    contentDescription = "Imagen",
                                    modifier = Modifier
                                        .size(50.dp)
                                        .clip(CircleShape),
                                    contentScale = ContentScale.Crop
                                )
                            }

                            Row(
                                horizontalArrangement = Arrangement.spacedBy(5.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.align(Alignment.TopEnd)
                            ) {
                                texto_generico_one_line("saldo $saldo_disponible")
                                Image(
                                    painter = painterResource(R.drawable.icon_monedas_3d),
                                    contentDescription = "saldo",
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }


                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(
                            "Renueva tu plan",
                            fontFamily = baners_geinz_work,
                            fontSize = 30.sp,
                            color = Color.White
                        )
                        texto_generico_one_line(
                            "Selecciona tus meses a renovar",
                            style = MaterialTheme.typography.bodyMedium,
                        )
                        chips_renovar(lista_descuentos, { i ->
                            porcentaje_ahorro = i.procentaje_ahorro
                            descuento_aplicado = i.descuento_off
                            precio_anteterir = i.precio_anterior
                            desceunto_numero = i.porcentaje_int.toDouble()
                            clikeado = i.descuento_off != ""
                            almenos_uno_selecion = i.meses
                            when (i.meses) {
                                "2 meses" -> {
                                    color_select = Color(0xFF8700F3)
                                }

                                "3 meses" -> {
                                    color_select = Color(0xFF5DF300)
                                }

                                "4 meses" -> {
                                    color_select = Color(0xFFF3D300)
                                }

                            }

                        })
                        spacer_vertical(5.dp)
                        if (almenos_uno_selecion != "") {
                            Crossfade(
                                targetState = descuento_aplicado != "",
                                label = ""
                            ) { descuento ->
                                if (descuento && porcentaje_ahorro != "") {
                                    val precio_double = precio_anteterir.toDouble() ?: 0.0
                                    val total_descuento =
                                        calcularAhorro(precio_double, desceunto_numero)
                                    val descuento_aplicado_puntos =
                                        calcularDescuento(precio_double, desceunto_numero)
                                    val calcular_sado = calcularSaldo(
                                        descuento_aplicado_puntos,
                                        saldo_disponible.toDouble()
                                    )
                                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(5.dp)
                                        ) {
                                            texto_generico_one_line(
                                                "Ahorra",
                                                style = MaterialTheme.typography.titleLarge
                                            )
                                            texto_generico_one_line(
                                                total_descuento.toInt().toString(),
                                                style = MaterialTheme.typography.titleLarge
                                            )
                                            texto_generico_one_line(
                                                "de puntos",
                                                style = MaterialTheme.typography.titleLarge
                                            )
                                        }

                                        texto_generico_one_line("Precio anterior $precio_anteterir")
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(5.dp)
                                        ) {
                                            texto_generico_one_line(porcentaje_ahorro)
                                            texto_generico_one_line(
                                                "de descuento aplicado ${descuento_aplicado_puntos.toInt()}",
                                                style = MaterialTheme.typography.bodyMedium
                                            )
                                        }
                                        texto_generico_one_line("saldo disponible al comprar $calcular_sado")

                                        texto_generico_one_line(
                                            "Total :${descuento_aplicado_puntos.toInt()}",
                                            style = MaterialTheme.typography.headlineSmall
                                        )

                                    }

                                }
                            }
                            if( porcentaje_ahorro == ""){
                            texto_generico_one_line(
                                "Total ",
                                style = MaterialTheme.typography.headlineSmall
                            )
                            }
                        }
                    }
                }
            }
        }
    )
}

@Composable
fun chips_renovar(
    lista_planes: List<descuentos>,
    onSeleccionar: (descuentos) -> Unit
) {
    var seleccionadoIndex by remember { mutableStateOf(-1) }
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        itemsIndexed(lista_planes) { index, i ->

            val esSeleccionado = index == seleccionadoIndex

            Box(
                modifier = Modifier
                    .size(width = 80.dp, height = 40.dp)
                    .clip(CircleShape)
                    .background(
                        if (esSeleccionado) Color.White
                        else MaterialTheme.colorScheme.primary
                    )
                    .clickable(enabled = !esSeleccionado) { // solo clickeable si NO está seleccionado
                        seleccionadoIndex = index
                        onSeleccionar(i)
                    },
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    texto_generico_one_line(
                        i.meses,
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (esSeleccionado) Color.Black
                        else Color.White
                    )

//                    if (i.descuento_off.isNotEmpty()) {
//                        texto_generico_one_line(
//                            i.descuento_off,
//                            style = MaterialTheme.typography.bodySmall,
//                            color = if (esSeleccionado) Color.Black
//                            else Color.White
//                        )
//                    }
                }
            }
        }
    }
}

fun calcularAhorro(precio: Double, descuentoPorcentaje: Double): Double {
    return precio * (descuentoPorcentaje / 100)
}

fun calcularSaldo(doublePrecioFinal: Double, saldoActual: Double): String {
    return if (saldoActual >= doublePrecioFinal) {
        val nuevoSaldo = saldoActual - doublePrecioFinal
        "${"%.2f".format(nuevoSaldo)}"
    } else {
        "Saldo insuficiente"
    }
}


fun calcularDescuento(precio: Double, descuentoPorcentaje: Double): Double {
    val ahorro = precio * (descuentoPorcentaje / 100)
    val precioFinal = precio - ahorro
    return precioFinal
}
