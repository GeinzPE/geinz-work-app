package com.geinzz.geinzwork.ui.adapters.ui.pantallas.inmobiliaria.desing_mapa

import android.annotation.SuppressLint
import android.content.Context
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.geinzz.geinzwork.data.model.localizate_geinz.iconos_creaciones_rutas
import com.geinzz.geinzwork.utils.localizate_geinz.verificarUbiActiva
import com.mapbox.geojson.Feature
import com.mapbox.geojson.FeatureCollection
import com.mapbox.geojson.LineString
import com.mapbox.geojson.Point
import com.mapbox.maps.MapboxMap
import com.mapbox.maps.extension.style.layers.addLayerBelow
import com.mapbox.maps.extension.style.sources.addSource
import com.mapbox.maps.extension.style.sources.generated.GeoJsonSource

@SuppressLint("MissingPermission")
@Composable
fun desing_creacion_ruta(
    distancia: Int,
    context: Context,
    lista: List<iconos_creaciones_rutas>,
    seleccionado: (String, ImageVector) -> Unit,
    cancelacion_ruta: () -> Unit,
    ocultar_dialog_: () -> Unit,
    mostar_dialog_no_ubi_activa: () -> Unit
) {
    var seleccionadoActual by remember { mutableStateOf<String?>(null) }

    val listaVisible = if (seleccionadoActual == null) lista
    else lista.filter { it.tipo == seleccionadoActual }

    val distanciaKm = distancia / 1000.0

    // ✅ Contenedor con fondo redondeado
    Box(
        modifier = Modifier
            .clip(CircleShape)
            .background(Color.White)
            .padding(horizontal = 6.dp, vertical = 6.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            listaVisible.forEach { item ->
                val deshabilitado = item.tipo == "walking" && distanciaKm > 20.0
                val estaActivo = seleccionadoActual == item.tipo

                val colorFondo by animateColorAsState(
                    targetValue = when {
                        deshabilitado -> Color.Gray
                        estaActivo -> Color(0xFF5B21B6)
                        else -> Color(0xFF7C3AED)
                    },
                    animationSpec = tween(250),
                    label = "fondo_${item.tipo}"
                )

                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(colorFondo)
                        .then(
                            if (estaActivo)
                                Modifier.border(2.dp, Color.White.copy(alpha = 0.6f), CircleShape)
                            else Modifier
                        )
                        .clickable(
                            indication = null,
                            interactionSource = remember { MutableInteractionSource() }
                        ) {
                            if (deshabilitado) return@clickable
                            if (verificarUbiActiva(context)) {
                                if (seleccionadoActual == item.tipo) {
                                    seleccionadoActual = null
                                    cancelacion_ruta()
                                } else {
                                    seleccionadoActual = item.tipo
                                    seleccionado(item.tipo, item.icono)
                                    ocultar_dialog_()
                                }
                            } else {
                                mostar_dialog_no_ubi_activa()
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = item.icono,
                        contentDescription = item.tipo,
                        tint = if (deshabilitado) Color.White.copy(alpha = 0.35f) else Color.White,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        }
    }
}


fun limpiarRutaEnMapa(mapboxMap: MapboxMap) {
    mapboxMap.getStyle { style ->
        try {
            style.removeStyleLayer("route_layer")
        } catch (_: Exception) {
        }
        try {
            style.removeStyleSource("route_source")
        } catch (_: Exception) {
        }
    }
}



fun dibujarRutaEnMapa(
    mapboxMap: MapboxMap,
    puntos: List<Point>
) {
    mapboxMap.getStyle { style ->
        // ── Limpia capa y source previos ──────────────
        try {
            style.removeStyleLayer("route_layer")
        } catch (_: Exception) {
        }
        try {
            style.removeStyleSource("route_source")
        } catch (_: Exception) {
        }

        if (puntos.isEmpty()) return@getStyle

        // ── Source con la línea ───────────────────────
        val featureCollection = FeatureCollection.fromFeature(
            Feature.fromGeometry(LineString.fromLngLats(puntos))
        )
        style.addSource(
            GeoJsonSource.Builder("route_source")
                .featureCollection(featureCollection)
                .build()
        )

        // ── Layer con estilo de línea ─────────────────
        style.addLayerBelow(
            com.mapbox.maps.extension.style.layers.generated.lineLayer(
                "route_layer", "route_source"
            ) {
                lineColor("#2563EB")
                lineOpacity(0.35)
                lineWidth(18.0)
                lineCap(com.mapbox.maps.extension.style.layers.properties.generated.LineCap.ROUND)
                lineJoin(com.mapbox.maps.extension.style.layers.properties.generated.LineJoin.ROUND)
                lineOpacity(0.9)
            },
            "road-label"   // se inserta debajo de las etiquetas de calle
        )
    }
}

