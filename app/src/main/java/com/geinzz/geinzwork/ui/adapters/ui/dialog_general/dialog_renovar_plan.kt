package com.geinzz.geinzwork.ui.adapters.ui.dialog_general

import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
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
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import coil3.request.error
import coil3.request.placeholder
import com.geinzz.geinzwork.R
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.btn_aceptar_etc_dialog_general
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.btn_cerra_etc_dialog_general
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.texto_generico_one_line
import com.geinzz.geinzwork.ui.adapters.ui.ui.theme.baners_geinz_work
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.constantes_lista_localidades.FuenteControladaApp
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.constantes_lista_localidades.shadow_botonm_filtrado_v1
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.constantes_lista_localidades.shadow_botonm_filtrado_v2
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.constantes_lista_localidades.shadow_botonm_filtrado_v21_dialog
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.constantes_lista_localidades.shadow_top_filtrado_v1
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.constantes_lista_localidades.shadow_top_filtrado_v2
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.constantes_lista_localidades.shadow_top_filtrado_v21_dialog

data class Descuentos(
    val meses: String,
    val icono_descuento: ImageVector?,
    val descuento_off: String,
    val precio_anterior: String,
    val procentaje_ahorro: String,
    val porcentaje_int: Int,
    val meses_agregados:String,
)


val lista_descuentos = listOf(

    Descuentos(
        meses = "14 días",
        icono_descuento = null,
        descuento_off = "",
        precio_anterior = "",
        procentaje_ahorro = "",
        porcentaje_int = 0, meses_agregados = "14 días"
    ),
    Descuentos(
        meses = "1 mes",
        icono_descuento = Icons.Filled.LocalFireDepartment,
        descuento_off = "-5%off",
        precio_anterior = "1500",
        procentaje_ahorro ="5%",
        porcentaje_int = 5,"1 mes"
    ),

    Descuentos(
        meses = "2 meses",
        icono_descuento = Icons.Filled.LocalFireDepartment,
        descuento_off = "-10%off",
        precio_anterior = "2000",
        procentaje_ahorro = "10%",
        porcentaje_int = 10,"2 mes"
    ),

    Descuentos(
        meses = "3 meses",
        icono_descuento = Icons.Filled.LocalFireDepartment,
        descuento_off = "-20%off",
        precio_anterior = "3000",
        procentaje_ahorro = "20%",
        porcentaje_int = 20,"3 mes"
    ),

    Descuentos(
        meses = "4 meses",
        icono_descuento = Icons.Filled.LocalFireDepartment,
        descuento_off = "-30%off",
        precio_anterior = "4000",
        procentaje_ahorro = "30%",
        porcentaje_int = 30,"4 mes"
    )
)

@Composable
fun dialog_renovar_plan(saldo_disponible: Long, ondimis: () -> Unit , comprar:(String,String)-> Unit) {
    var clikeado by remember { mutableStateOf(false) }

    var descuento_aplicado by remember { mutableStateOf("") }
    var precio_anteterir by remember { mutableStateOf("") }
    var desceunto_numero by remember { mutableStateOf(0.0) }
    var porcentaje_ahorro by remember { mutableStateOf("") }
    var almenos_uno_selecion by remember { mutableStateOf("") }
    var total_cancelar by remember { mutableStateOf("") }
    var saldo_dispopnible by remember { mutableStateOf("") }
    var mes_aumentar by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = { ondimis() },
        confirmButton = {
            Log.d("sdasdfdgafg2423","$clikeado $saldo_dispopnible")
            if (clikeado && !saldo_dispopnible.equals("Saldo insuficiente")) {
                btn_aceptar_etc_dialog_general(txt_btn = "continuar") {
                    ondimis()
                    comprar(total_cancelar,mes_aumentar)
                }
            }
        },
        dismissButton = {
            if (clikeado && !saldo_dispopnible.equals("Saldo insuficiente")) {
                btn_cerra_etc_dialog_general(txt_btn = "Cancelar") {
                    ondimis()
                }
            }
        },
        text = {
            FuenteControladaApp {
                Column() {
                    spacer_vertical(10.dp)
                    Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(15.dp)
                        ) {

                            Row(
                                horizontalArrangement = Arrangement.spacedBy(5.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.align(Alignment.TopEnd)
                            ) {
                                texto_generico_one_line("$saldo_disponible")
                                Image(
                                    painter = painterResource(R.drawable.icon_monedas_3d),
                                    contentDescription = "saldo",
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(5.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(40.dp)
                        ) {
                            Text(
                                "Renueva tu plan",
                                fontFamily = baners_geinz_work,
                                fontSize = 30.sp,
                                color = Color.White
                            )
                        }

                        texto_generico_one_line(
                            "Selecciona plazo a renovar",
                            style = MaterialTheme.typography.bodyMedium,
                        )
                        spacer_vertical(2.dp)
                        chips_renovar(lista_descuentos, { i ->
                            porcentaje_ahorro = i.procentaje_ahorro
                            descuento_aplicado = i.descuento_off
                            precio_anteterir = i.precio_anterior
                            desceunto_numero = i.porcentaje_int.toDouble()
                            clikeado = i.meses != ""
                            almenos_uno_selecion = i.meses
                            mes_aumentar=i.meses_agregados


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
                                    val descuento_aplicado_puntos = calcularDescuento(precio_double, desceunto_numero)
                                    total_cancelar=descuento_aplicado_puntos.toInt().toString()
                                    val calcular_sado = calcularSaldo(
                                        descuento_aplicado_puntos,
                                        saldo_disponible.toDouble()
                                    )
                                    saldo_dispopnible=calcular_sado
                                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(5.dp)
                                        ) {
                                            texto_generico_one_line(
                                                "Ahorras",
                                                style = MaterialTheme.typography.titleLarge
                                            )
                                            texto_generico_one_line(
                                                total_descuento.toInt().toString(),
                                                style = MaterialTheme.typography.titleLarge
                                            )

                                            Image(
                                                painter = painterResource(R.drawable.icon_monedas_3d),
                                                contentDescription = "total",
                                                modifier = Modifier.size(16.dp)
                                            )

                                        }

                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(5.dp)
                                        ) {
                                            texto_generico_one_line(
                                                "Precio anterior por $almenos_uno_selecion",
                                                style = MaterialTheme.typography.bodyMedium
                                            )
                                            texto_generico_one_line(
                                                precio_anteterir,
                                                )
                                        }

                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(5.dp)
                                        ) {
                                            texto_generico_one_line(porcentaje_ahorro)
                                            texto_generico_one_line(
                                                "de descuento aplicado",
                                                style = MaterialTheme.typography.bodyMedium
                                            )
                                            texto_generico_one_line("${descuento_aplicado_puntos.toInt()}")
                                        }
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(5.dp)
                                        ) {
                                            texto_generico_one_line(
                                                "Saldo disponible al continuar",
                                                style = MaterialTheme.typography.bodyMedium
                                            )
                                            texto_generico_one_line(
                                                "$calcular_sado",
                                            )
                                        }
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(5.dp)
                                        ) {
                                            texto_generico_one_line(
                                                "Total : ${descuento_aplicado_puntos.toInt()}",
                                                style = MaterialTheme.typography.headlineSmall
                                            )
                                            Image(
                                                painter = painterResource(R.drawable.icon_monedas_3d),
                                                contentDescription = "total",
                                                modifier = Modifier.size(30.dp)
                                            )
                                        }


                                    }

                                }
                            }
                            if (porcentaje_ahorro == "") {
                                total_cancelar="1000"
                                val precio_final=1000.0
                                val calcular_sado = calcularSaldo(
                                    precio_final,
                                    saldo_disponible.toDouble()
                                )
                                saldo_dispopnible=calcular_sado
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                                ) {
                                    texto_generico_one_line(
                                        "Saldo disponible al comprar",
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                    texto_generico_one_line(
                                        "$calcular_sado",
                                    )
                                }
                                spacer_vertical(5.dp)

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                                ) {
                                    texto_generico_one_line(
                                        "Total : 1000",
                                        style = MaterialTheme.typography.headlineSmall
                                    )
                                    Image(
                                        painter = painterResource(R.drawable.icon_monedas_3d),
                                        contentDescription = "total",
                                        modifier = Modifier.size(30.dp)
                                    )
                                }
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
    lista_planes: List<Descuentos>,
    onSeleccionar: (Descuentos) -> Unit
) {
    val listState = rememberLazyListState()

    val showLeftShadow by remember {
        derivedStateOf { listState.firstVisibleItemIndex > 0 || listState.firstVisibleItemScrollOffset > 0 }
    }
    val showRightShadow by remember {
        derivedStateOf {
            val lastVisible = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index
            val total = listState.layoutInfo.totalItemsCount
            lastVisible != null && lastVisible < total - 1
        }
    }

    // 🔥 animar alpha, no crear/destruir Box
    val alphaLeft by animateFloatAsState(
        targetValue = if (showLeftShadow) 1f else 0f,
        animationSpec = tween(400),
        label = "alphaLeft"
    )
    val alphaRight by animateFloatAsState(
        targetValue = if (showRightShadow) 1f else 0f,
        animationSpec = tween(400),
        label = "alphaRight"
    )
    var seleccionadoIndex by remember { mutableStateOf(-1) }
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(40.dp), contentAlignment = Alignment.Center
    ) {

    LazyRow(
        state =listState ,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        itemsIndexed(lista_planes) { index, i ->

            val esSeleccionado = index == seleccionadoIndex

            Box(
                modifier = Modifier
                    .height(40.dp)
                    .width(100.dp)
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
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                ) {

                    // 🔥 Si trae ícono, lo muestra
                    if (i.icono_descuento != null) {
                        Icon(
                            imageVector = i.icono_descuento,
                            contentDescription = null,
                            tint = if (esSeleccionado) Color.Black else Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    texto_generico_one_line(
                        i.meses,
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (esSeleccionado) Color.Black else Color.White
                    )

                }

            }
        }
    }
        // 👈 izquierda
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .width(40.dp)

                .align(Alignment.CenterStart)
                .zIndex(1f)
                .alpha(alphaLeft)
             .background(Brush.horizontalGradient(colors = shadow_top_filtrado_v21_dialog))

        )

        // 👉 derecha
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .width(40.dp)

                .align(Alignment.CenterEnd)
                .zIndex(1f)
                .alpha(alphaRight)
             .background(Brush.horizontalGradient(colors = shadow_botonm_filtrado_v21_dialog))

        )
    }
}

fun calcularAhorro(precio: Double, descuentoPorcentaje: Double): Double {
    return precio * (descuentoPorcentaje / 100)
}

fun calcularSaldo(doublePrecioFinal: Double, saldoActual: Double): String {
    return if (saldoActual >= doublePrecioFinal) {
        val nuevoSaldo = saldoActual - doublePrecioFinal
        "${nuevoSaldo.toInt()}"
    } else {
        "Saldo insuficiente"
    }
}


fun calcularDescuento(precio: Double, descuentoPorcentaje: Double): Double {
    val ahorro = precio * (descuentoPorcentaje / 100)
    val precioFinal = precio - ahorro
    return precioFinal
}
