package com.geinzz.geinzwork.ui.adapters.ui.pantallas

import android.content.Intent
import androidx.compose.foundation.Image
import com.geinzz.geinzwork.R
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.geinzz.geinzwork.Crea_tu_publicidad
import com.geinzz.geinzwork.FuncionalidadGeinz.comoUsar
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.btn_clasico_shap_50f
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.texto_generico_multilinea
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.titulos_genericos_one_line
import com.geinzz.geinzwork.ui.adapters.ui.dialog_general.spacer_vertical
import com.geinzz.geinzwork.ui.adapters.ui.uso_geinz.como_usar_geinz_trabajadores
import com.geinzz.geinzwork.ui.adapters.ui.uso_geinz.como_usar_geinz_work

@Composable
fun nosotros_geinz_work(innerPadding: PaddingValues) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)
            .padding(10.dp)
    ) {
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                titulos_genericos_one_line(
                    stringResource(R.string.generalGeinz), MaterialTheme.typography.headlineMedium,
                    modifier = Modifier
                        .fillMaxWidth()
                )
                titulos_genericos_one_line(
                    stringResource(R.string.crea_diseña), MaterialTheme.typography.titleMedium,
                    modifier = Modifier
                        .fillMaxWidth()
                )
                spacer_vertical(10.dp)
            }

        }
        item {
            Box(modifier = Modifier.fillMaxWidth()) {
                Image(
                    painter = painterResource(R.drawable.logo_geinz_500x500),
                    contentDescription = "",
                    modifier = Modifier
                        .size(150.dp)
                        .align(Alignment.Center)
                )
            }
            spacer_vertical(15.dp)
        }
        item {
            texto_generico_multilinea(
                stringResource(R.string.titulo_quienes_somos_GW),
                MaterialTheme.typography.titleMedium
            )
            texto_generico_multilinea(
                stringResource(R.string.descripcion_quienes_somos_GW),
                MaterialTheme.typography.bodyMedium
            )
            spacer_vertical(10.dp)


        }
        item {
            texto_generico_multilinea(
                stringResource(R.string.titulo_nuestro_enfoque_GW),
                MaterialTheme.typography.titleMedium
            )
            texto_generico_multilinea(
                stringResource(R.string.descripcion_nuestro_enfoque_GWT),
                MaterialTheme.typography.bodyMedium
            )
            spacer_vertical(10.dp)


        }
        item {
            texto_generico_multilinea(
                stringResource(R.string.boton_contactanos_GWT),
                MaterialTheme.typography.titleMedium
            )
            texto_generico_multilinea(
                stringResource(R.string.mensaje_variedad_GWT), MaterialTheme.typography.bodyMedium
            )
            spacer_vertical(10.dp)
        }
        item {
            texto_generico_multilinea(
                stringResource(R.string.titulo_acciones_rapidas_BWT),
                MaterialTheme.typography.titleMedium
            )
            spacer_vertical(5.dp)

            texto_generico_multilinea(
                stringResource(R.string.descripcion_acciones_rapidas_GWT),
                MaterialTheme.typography.bodyMedium
            )
            spacer_vertical(5.dp)
        }
        item {
            btn_acciones_rapidas()
            spacer_vertical(10.dp)
        }

        item {
            texto_generico_multilinea(
                stringResource(R.string.titulo_crecimiento_GWT),
                MaterialTheme.typography.titleMedium
            )
            spacer_vertical(5.dp)
            texto_generico_multilinea(
                stringResource(R.string.descripcion_registro_tiendas_GWT),
                MaterialTheme.typography.bodyMedium
            )
            spacer_vertical(5.dp)
        }
        item {
            btn_acciones_de_geinz()
        }
    }


}

@Composable
fun btn_acciones_de_geinz() {
    val context = LocalContext.current

    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        item {
            btn_clasico_shap_50f(stringResource(R.string.boton_registro_tiendas_GWT)) {
            }
        }
        item {
            btn_clasico_shap_50f(stringResource(R.string.accion_crear_publicidad_GWT)) {
                context.startActivity(Intent(context, Crea_tu_publicidad::class.java))
            }
        }
        item {
            btn_clasico_shap_50f(stringResource(R.string.accion_crear_noticia_GWT)) {}
        }
    }
}

@Composable
fun btn_acciones_rapidas() {
    val context = LocalContext.current
    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        item {
            btn_clasico_shap_50f(stringResource(R.string.accion_uso_trabajadores_GWT)) {
                context.startActivity(Intent(context, como_usar_geinz_trabajadores::class.java))
            }
        }
        item {
            btn_clasico_shap_50f(stringResource(R.string.accion_uso_tiendas_GWT)) {}
        }
    }
}

