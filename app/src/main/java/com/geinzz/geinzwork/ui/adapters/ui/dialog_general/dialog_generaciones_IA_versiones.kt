package com.geinzz.geinzwork.ui.adapters.ui.dialog_general

import android.util.Log
import android.widget.Toast
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.geinzz.geinzwork.R
import com.geinzz.geinzwork.data.model.GeneracionIA
import com.geinzz.geinzwork.data.model.OpcionPromocionIA
import com.geinzz.geinzwork.data.model.datos_para_generacion_dialog_historial_IA
import com.geinzz.geinzwork.data.model.dialog_generaciones_IA_promo_noti
import com.geinzz.geinzwork.model.repo_pantallas_promocionar
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.chisp_filtrado_busqueda_con_la_IA
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.texto_generico_multilinea
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.texto_generico_one_line
import com.geinzz.geinzwork.ui.adapters.ui.pantallas.socios.FondoIAAnimado
import com.geinzz.geinzwork.viewModels.viewmodel_generaciones_IA
import com.geinzz.geinzwork.viewModels.viewmodel_pantallas_promocionar
import com.valentinilk.shimmer.shimmer

@Composable
fun dailog_generaciones_IA_versiones(
    id_seleccionado:String,
    i: datos_para_generacion_dialog_historial_IA,
    viewmodelGeneracionesIa: viewmodel_generaciones_IA,
    titulo: String,
    texto: String,
    tipo: String,
    ondismis: () -> Unit
) {
    val context= LocalContext.current
    var msje_texto_notificacion_generada by remember { mutableStateOf("Mejorar con IA") }
    var tipo_promp_seleccionado_IA by remember {
        mutableStateOf<repo_pantallas_promocionar.TipoGeneracionIA?>(null)
    }

    var tipo_promp_seleccionado_IA_notificicaciones by remember {
        mutableStateOf<repo_pantallas_promocionar.TipoGeneracionIA?>(null)
    }
    var resultadoIA by remember {
        mutableStateOf<dialog_generaciones_IA_promo_noti?>(null)
    }


    val estado_textos_notificaciones_generadas by viewmodelGeneracionesIa.estado_promociones_ia.collectAsState()

    val estado_notificaion_con_ia_corta by viewmodelGeneracionesIa.estado_notificaion_con_ia_corta.collectAsState()


    val lista_generacions_IA_proms = listOf(
        GeneracionIA(
            tipo = repo_pantallas_promocionar.TipoGeneracionIA.VENTA,
            beneficios = listOf(
                "Lenguaje persuasivo orientado a conversión",
                "Llamados a la acción claros",
                "Genera urgencia moderada",
                "Ideal para ventas rápidas"
            )
        ),
        GeneracionIA(
            tipo = repo_pantallas_promocionar.TipoGeneracionIA.ATENCION,
            beneficios = listOf(
                "Ganchos creativos y llamativos",
                "Preguntas que despiertan curiosidad",
                "Mayor visibilidad en el feed",
                "Ideal para atraer nuevos clientes"
            )
        ),
        GeneracionIA(
            tipo = repo_pantallas_promocionar.TipoGeneracionIA.INFORMATIVO,
            beneficios = listOf(
                "Tono profesional y confiable",
                "Explica claramente el valor",
                "Evita exageraciones",
                "Ideal para rubros técnicos o formales"
            )
        )
    )

    val lista_generacions_IA_notificaciones = listOf(
        GeneracionIA(
            tipo = repo_pantallas_promocionar.TipoGeneracionIA.VENTA,
            beneficios = listOf(
                "Texto corto y persuasivo para acción inmediata",
                "Impulsa clics y compras",
                "Llamado a la acción claro y directo",
                "Ideal para promociones y ventas rápidas"
            )
        ),
        GeneracionIA(
            tipo = repo_pantallas_promocionar.TipoGeneracionIA.ATENCION,
            beneficios = listOf(
                "Captura la atención en segundos",
                "Ganchos que aumentan la apertura de la notificación",
                "Lenguaje intrigante que invita a leer más",
                "Ideal para anunciar novedades y atraer usuarios"
            )
        ),
        GeneracionIA(
            tipo = repo_pantallas_promocionar.TipoGeneracionIA.URGENCIA,
            beneficios = listOf(
                "Genera sensación de escasez o tiempo limitado",
                "Fomenta acción inmediata",
                "Ideal para ofertas que expiran pronto",
                "Aumenta la conversión en notificaciones push"
            )
        ),
        GeneracionIA(
            tipo = repo_pantallas_promocionar.TipoGeneracionIA.NOVEDAD,
            beneficios = listOf(
                "Resalta lo nuevo o destacado",
                "Incentiva al usuario a abrir la notificación",
                "Perfecto para lanzamientos o actualizaciones",
                "Atrae curiosidad sin ser demasiado agresivo"
            )
        )
    )

    LaunchedEffect(estado_textos_notificaciones_generadas) {
        if (estado_textos_notificaciones_generadas is viewmodel_generaciones_IA.EstadoIA_dialog_centrado.Success) {
            ondismis()
        }
    }

    LaunchedEffect(estado_notificaion_con_ia_corta) {
        if(estado_notificaion_con_ia_corta is viewmodel_generaciones_IA.EstadoIA_dialog_centrado.Success){
            ondismis()
        }
    }



    AlertDialog(
        onDismissRequest = { ondismis() },
        confirmButton = {},
        dismissButton = {},
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {


                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    texto_generico_one_line(
                        "Mejora tus textos con IA",
                        style = MaterialTheme.typography.titleLarge,
                    )
                    spacer_horizonta(5.dp)
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = "Mejorar con IA",
                        tint = Color.White
                    )
                }
                texto_generico_multilinea(
                    "Mejora los textos generados con IA o los que redactes de forma original.",
                    style = MaterialTheme.typography.bodyMedium
                )

                spacer_vertical(5.dp)
                Column(modifier = Modifier.clip(RoundedCornerShape(10.dp)).background(MaterialTheme.colorScheme.surfaceVariant).padding(10.dp)) {
                spacer_vertical(5.dp)
                texto_generico_multilinea(titulo, style = MaterialTheme.typography.titleSmall)
                spacer_vertical(5.dp)
                texto_generico_multilinea(texto, style = MaterialTheme.typography.bodySmall)
                    spacer_vertical(5.dp)
                }

                spacer_vertical(10.dp)

                if (tipo == "publicacion" || tipo == "generacion_publicacion_sin_pulicar") {

                    texto_generico_multilinea(
                        "Selecciona el tipo de contenido que quieres generar",
                        style = MaterialTheme.typography.titleSmall
                    )

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(lista_generacions_IA_proms) { subcategoria ->

                            val seleccionado =
                                tipo_promp_seleccionado_IA == subcategoria.tipo

                            chisp_filtrado_busqueda_con_la_IA(
                                carta_selecionada = seleccionado,
                                filtrado = "${subcategoria.tipo.icono} ${subcategoria.tipo.tituloUI}",
                                btn_visible = false,
                                clik_card = {
                                    tipo_promp_seleccionado_IA = subcategoria.tipo
                                },
                                onClick_delete = {}
                            )
                        }
                    }

                    val beneficiosSeleccionados = lista_generacions_IA_proms
                        .firstOrNull { it.tipo == tipo_promp_seleccionado_IA }
                        ?.beneficios


                    if (!beneficiosSeleccionados.isNullOrEmpty()) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 12.dp)
                        ) {
                            beneficiosSeleccionados.forEach { beneficio ->
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(vertical = 4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    texto_generico_multilinea(
                                        texto = beneficio,
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                            }
                        }
                    }

                    if (!beneficiosSeleccionados.isNullOrEmpty()) {
                        spacer_vertical(10.dp)
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(40.dp)
                                .clip(CircleShape)
                        ) {
                            val cargando =
                                estado_textos_notificaciones_generadas is viewmodel_generaciones_IA.EstadoIA_dialog_centrado.Loading
                            val buttonColor by animateColorAsState(
                                targetValue = if (cargando)
                                    Color.Black
                                else
                                    MaterialTheme.colorScheme.primary,
                                label = "buttonColor"
                            )
                            // 🔥 Fondo animado SOLO cuando no carga
                            if (!cargando) {
                                FondoIAAnimado(
                                    modifier = Modifier.matchParentSize()
                                )

                            }
                            Button(
                                onClick = {
                                    if (!cargando) {
                                        tipo_promp_seleccionado_IA?.let { tipoSeleccionado ->
                                            viewmodelGeneracionesIa.mejorar_texto_con_promo_IA(
                                                id_seleccionado,
                                                tipo_generacion = tipoSeleccionado, // ✅ seguro, no null
                                                saldo_tienda = i.monedas_tienda,
                                                localidad_tienda = i.localidad_tienda,
                                                id_tienda = i.id_tienda,
                                                nombre_tienda = i.nombre_tienda,
                                                tituloUsuario = titulo,
                                                descripcionUsuario = texto,
                                                nombreTienda = i.nombre_tienda,
                                                localidad = i.localidad_tienda,"15","Gen IA (REGENERADO)"
                                            )

                                        } ?: run {
                                            // 🚨 null -> opcional: mostrar mensaje de error o toast
                                            Toast.makeText(
                                                context,
                                                "Selecciona un tipo de generacion antes",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                    }

                                },
                                enabled = !cargando,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (cargando) buttonColor else Color.Transparent,
                                    disabledContainerColor = if (cargando) buttonColor else Color.Transparent,
                                    contentColor = Color.White,
                                    disabledContentColor = Color.White
                                ),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                if (cargando) {
                                    Box(
                                        modifier = Modifier
                                            .height(20.dp)
                                            .width(160.dp)
                                            .shimmer(), contentAlignment = Alignment.Center

                                    ) {

                                        Row(
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {

                                            Spacer(modifier = Modifier.width(8.dp))

                                            texto_generico_one_line(
                                                "Generando contenido..",
                                                style = MaterialTheme.typography.bodyMedium
                                            )

                                        }
                                    }
                                } else {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                    ) {
                                        texto_generico_one_line(
                                            msje_texto_notificacion_generada,
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                        spacer_horizonta(5.dp)
                                        Icon(
                                            imageVector = Icons.Default.AutoAwesome,
                                            contentDescription = "Mejorar con IA",
                                            tint = Color.White
                                        )
                                        spacer_horizonta(5.dp)
                                        texto_generico_one_line(
                                            "15",
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                        spacer_horizonta(5.dp)
                                        Image(
                                            painter = painterResource(R.drawable.icon_monedas_3d),
                                            contentDescription = null,
                                            modifier = Modifier.size(20.dp)
                                        )

                                    }
                                }
                            }
                        }
                    }

                } else {
                    texto_generico_multilinea(
                        "Elige el tipo de notificación que deseas generar",
                        style = MaterialTheme.typography.titleSmall
                    )

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(lista_generacions_IA_notificaciones) { subcategoria ->

                            val seleccionado =
                                tipo_promp_seleccionado_IA_notificicaciones == subcategoria.tipo

                            chisp_filtrado_busqueda_con_la_IA(
                                carta_selecionada = seleccionado,
                                filtrado = "${subcategoria.tipo.icono} ${subcategoria.tipo.tituloUI}",
                                btn_visible = false,
                                clik_card = {
                                    tipo_promp_seleccionado_IA_notificicaciones =
                                        subcategoria.tipo
                                },
                                onClick_delete = {}
                            )
                        }
                    }

                    val beneficiosSeleccionados =
                        lista_generacions_IA_notificaciones
                            .firstOrNull { it.tipo == tipo_promp_seleccionado_IA_notificicaciones }
                            ?.beneficios


                    if (!beneficiosSeleccionados.isNullOrEmpty()) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 12.dp)
                        ) {
                            beneficiosSeleccionados.forEach { beneficio ->
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(vertical = 4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    texto_generico_multilinea(
                                        texto = beneficio,
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                            }
                        }
                    }
                    if (!beneficiosSeleccionados.isNullOrEmpty()) {
                        spacer_vertical(10.dp)
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(40.dp)
                                .clip(CircleShape)
                        ) {
                            val cargando =
                                estado_notificaion_con_ia_corta is viewmodel_generaciones_IA.EstadoIA_dialog_centrado_notificaciones.Loading
                            val buttonColor by animateColorAsState(
                                targetValue = if (cargando)
                                    Color.Black
                                else
                                    MaterialTheme.colorScheme.primary,
                                label = "buttonColor"
                            )
                            // 🔥 Fondo animado SOLO cuando no carga
                            if (!cargando) {
                                FondoIAAnimado(
                                    modifier = Modifier.matchParentSize()
                                )

                            }
                            Button(
                                onClick = {
                                    if (!cargando) {

                                    tipo_promp_seleccionado_IA_notificicaciones?.let { tipoSeleccionado ->

                                        viewmodelGeneracionesIa.mejorar_mejorar_notificacion_con_IA_corta(
                                            id_seleccionado,
                                            tipo_select_IA = "Gen IA (Notificación - REGENERADO)",
                                            tipoSeleccionado = tipoSeleccionado,
                                            saldo_tienda = i.monedas_tienda,
                                            localidad_tienda = i.localidad_tienda,
                                            id_tienda = i.id_tienda,
                                            nombre_tienda = i.nombre_tienda,
                                            titulo_publicacion = titulo,
                                            descripcion = texto
                                        )
                                    } ?: run {
                                        // 🚨 null -> opcional: mostrar mensaje de error o toast
                                        Toast.makeText(
                                            context,
                                            "Selecciona un tipo de generacion antes",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                    }
                                },
                                enabled = !cargando,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (cargando) buttonColor else Color.Transparent,
                                    disabledContainerColor = if (cargando) buttonColor else Color.Transparent,
                                    contentColor = Color.White,
                                    disabledContentColor = Color.White
                                ),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                if (cargando) {
                                    Box(
                                        modifier = Modifier
                                            .height(20.dp)
                                            .width(160.dp)
                                            .shimmer(), contentAlignment = Alignment.Center
                                    ) {

                                        Row(
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {

                                            Spacer(modifier = Modifier.width(8.dp))

                                            texto_generico_one_line(
                                                "Generando contenido..",
                                                style = MaterialTheme.typography.bodyMedium
                                            )

                                        }
                                    }
                                } else {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                    ) {
                                        texto_generico_one_line(
                                            msje_texto_notificacion_generada,
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                        spacer_horizonta(5.dp)
                                        Icon(
                                            imageVector = Icons.Default.AutoAwesome,
                                            contentDescription = "Mejorar con IA",
                                            tint = Color.White
                                        )
                                        spacer_horizonta(5.dp)
                                        texto_generico_one_line(
                                            "15",
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                        spacer_horizonta(5.dp)
                                        Image(
                                            painter = painterResource(R.drawable.icon_monedas_3d),
                                            contentDescription = null,
                                            modifier = Modifier.size(20.dp)
                                        )

                                    }
                                }
                            }
                        }
                    }
                }
                spacer_vertical(10.dp)

            }
        }
    )


}
