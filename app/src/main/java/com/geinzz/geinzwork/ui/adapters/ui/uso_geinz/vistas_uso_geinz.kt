package com.geinzz.geinzwork.ui.adapters.ui.uso_geinz

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.texto_generico_multilinea
import com.geinzz.geinzwork.R
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.texto_generico_one_line
import com.geinzz.geinzwork.ui.adapters.ui.dialog_general.spacer_vertical
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.rememberPagerState
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import com.google.accompanist.pager.*
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.tooling.preview.Preview
import com.geinzz.geinzwork.ui.adapters.ui.dialog_general.spacer_horizonta
import kotlinx.coroutines.launch

@OptIn(ExperimentalPagerApi::class)
@Composable
fun PagerConBotones(innerPadding: PaddingValues) {
    val pagerState = rememberPagerState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        HorizontalPager(
            count = 3,
            state = pagerState,
            modifier = Modifier.weight(1f)
        ) { page ->
            como_usar_geinz_work()
        }

//        Row(
//            modifier = Modifier
//                .fillMaxWidth()
//                .padding(16.dp),
//            horizontalArrangement = Arrangement.SpaceEvenly
//        ) {
//            Button(
//                onClick = {
//                    if (pagerState.currentPage > 0) {
//                        coroutineScope.launch { // <-- aquí lanzas la animación
//                            pagerState.animateScrollToPage(pagerState.currentPage - 1)
//                        }
//                    }
//                },
//                enabled = pagerState.currentPage > 0
//            ) {
//                Text("Anterior")
//            }
//
//            Button(
//                onClick = {
//                    if (pagerState.currentPage < 2) {
//                        coroutineScope.launch { // <-- aquí también
//                            pagerState.animateScrollToPage(pagerState.currentPage + 1)
//                        }
//                    }
//                },
//                enabled = pagerState.currentPage < 2
//            ) {
//                Text("Siguiente")
//            }
//        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun como_usar_geinz_work() {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(10.dp)
    ) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                texto_generico_multilinea(
                    stringResource(R.string.titulo_GWT),
                    MaterialTheme.typography.headlineMedium,
                    modifier = Modifier.weight(1f)
                )
                Image(
                    painter = painterResource(R.drawable.logo_geinz_500x500),
                    contentDescription = "",
                    modifier = Modifier.size(40.dp).clip(CircleShape)
                )
            }
            spacer_vertical(10.dp)
        }
        item {
            texto_generico_multilinea(stringResource(R.string.simplifica_GWT), MaterialTheme.typography.titleSmall)
            spacer_vertical(5.dp)

        }
        item {
            texto_generico_multilinea(stringResource(R.string.texto_simplifica_GWT),MaterialTheme.typography.bodyMedium)
            spacer_vertical(10.dp)

        }
        item {
            texto_generico_multilinea(stringResource(R.string.funcionalidades_GWT),MaterialTheme.typography.titleLarge)
            spacer_vertical(10.dp)

        }
        item {
            texto_generico_multilinea(stringResource(R.string.busqueda_localizada_GWT), MaterialTheme.typography.titleSmall)
            spacer_vertical(5.dp)
        }

        item {
            texto_generico_multilinea(stringResource(R.string.text_busqueda_localizada_GWT),MaterialTheme.typography.bodyMedium)
            spacer_vertical(10.dp)

        }

        item {
            texto_generico_multilinea(stringResource(R.string.variedad_GWT),MaterialTheme.typography.titleSmall)
            spacer_vertical(5.dp)
        }
        item {
            texto_generico_multilinea(stringResource(R.string.texto_variedad_BWT),MaterialTheme.typography.bodyMedium)
            spacer_vertical(10.dp)

        }

        item {
            texto_generico_multilinea(stringResource(R.string.facil_contratacion_GWT),MaterialTheme.typography.titleSmall)
            spacer_vertical(5.dp)
        }

        item {
            texto_generico_multilinea(stringResource(R.string.texto_facil_contratacion_GWT),MaterialTheme.typography.bodyMedium)
            spacer_vertical(10.dp)
        }

        item {
            texto_generico_multilinea(stringResource(R.string.review_GWT),MaterialTheme.typography.titleSmall)
            spacer_vertical(5.dp)
        }

        item {
            texto_generico_multilinea(stringResource(R.string.texto_review_GWT),MaterialTheme.typography.bodyMedium)
            spacer_vertical(10.dp)
        }

        item {
            texto_generico_multilinea(stringResource(R.string.Actividad_aplicacion_GWT),MaterialTheme.typography.titleSmall)
            spacer_vertical(20.dp)
            img_actividad_trabajadores()
        }


    }
}


@Composable
fun img_actividad_trabajadores() {
    Row {
        Image(
            painter = painterResource(R.drawable.img_perfil_trabjador),
            contentDescription = "",
            modifier = Modifier.size(170.dp).clip(RoundedCornerShape(20))
        )

        Column {
            item_estados("Desconectado",Color.Red)
            spacer_vertical(10.dp)
            item_estados("Activo",Color.Green)
        }
    }
}

@Composable
fun item_estados(texto: String,color: Color) {
    Row (verticalAlignment = Alignment.CenterVertically){
        Box(
            modifier = Modifier
                .size(15.dp)
                .clip(RoundedCornerShape(50))
                .background(color)
        )

        spacer_horizonta(5.dp)
        texto_generico_one_line(texto,MaterialTheme.typography.bodyMedium)
    }
}