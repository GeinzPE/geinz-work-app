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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.geinzz.geinzwork.Crea_tu_publicidad
import com.geinzz.geinzwork.FuncionalidadGeinz.comoUsar
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.btn_clasico_shap_50f
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.texto_generico_multilinea
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.titulos_genericos_one_line
import com.geinzz.geinzwork.ui.adapters.ui.dialog_general.spacer_vertical

@Composable
fun nosotros_geinz_work(innerPadding: PaddingValues) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding).padding(10.dp)
    ) {
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                titulos_genericos_one_line(
                    "Geinz work", MaterialTheme.typography.headlineMedium,
                    modifier = Modifier
                        .fillMaxWidth()
                )
                titulos_genericos_one_line(
                    "Crea-Diseña-Crece", MaterialTheme.typography.titleMedium,
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
                        .align ( Alignment.Center )
                )
            }
            spacer_vertical(15.dp)
        }
        item {
            texto_generico_multilinea("Quienes somos?", MaterialTheme.typography.titleMedium)
            texto_generico_multilinea(
                "Geinz es una empresa especializada en el desarrollo de soluciones digitales, " +
                        "desde aplicaciones Android hasta sitios web dinámicos y funcionales. " +
                        "Nuestro equipo combina experiencia técnica y creatividad para ofrecer " +
                        "herramientas innovadoras que impulsan el éxito de nuestros clientes en el mundo digital.",
                MaterialTheme.typography.bodyMedium
            )
            spacer_vertical(10.dp)


        }
        item {
            texto_generico_multilinea("Nuestro Enfoque", MaterialTheme.typography.titleMedium)
            texto_generico_multilinea(
                "Nos comprometemos a comprender a fondo las necesidades y objetivos " +
                        "de nuestros clientes. Nuestro equipo de profesionales combina experiencia, " +
                        "creatividad y dedicación para desarrollar soluciones digitales que generen valor " +
                        "y fortalezcan su presencia en línea.",
                MaterialTheme.typography.bodyMedium
            )
            spacer_vertical(10.dp)


        }
        item {
            texto_generico_multilinea("Contáctanos", MaterialTheme.typography.titleMedium)
            texto_generico_multilinea(
                "¿Tienes una idea o proyecto en mente? ¡Estamos aquí para convertirlo en realidad! " +
                        "Contáctanos hoy mismo y descubre cómo podemos ayudarte a alcanzar tus objetivos " +
                        "y destacar en el mundo digital.", MaterialTheme.typography.bodyMedium
            )
            spacer_vertical(10.dp)
        }
        item {
            texto_generico_multilinea(
                "Acciones rapidas en Geinz work",
                MaterialTheme.typography.titleMedium
            )
            spacer_vertical(5.dp)

            texto_generico_multilinea(
                "Elige la guía que necesitas y aprende a usar Geinz Work de forma sencilla. Si eres trabajador, descubre cómo ofrecer tus servicios y gestionar tus reservas. Si eres usuario, conoce cómo explorar tiendas, aprovechar promociones y hacer compras rápidas.",
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
                "Mejora tu crecimiento en Geinz",
                MaterialTheme.typography.titleMedium
            )
            spacer_vertical(5.dp)
            texto_generico_multilinea(
                "Potencia la visibilidad de tu negocio en Geinz Work. Registra tu tienda para llegar a más clientes, crea publicidad para destacar tus ofertas y comparte noticias para mantener a tus seguidores informados.",
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
            btn_clasico_shap_50f("Registra tu tienda en Geinz work") {
            }
        }
        item {
            btn_clasico_shap_50f("Crea tu publicidad") {
                context.startActivity(Intent(context, Crea_tu_publicidad::class.java))
            }
        }
        item {
            btn_clasico_shap_50f("Crea tu noticia") {}
        }
    }
}

@Composable
fun btn_acciones_rapidas() {
    val context = LocalContext.current
    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        item {
            btn_clasico_shap_50f("Como usar Geinz (Trabajadores)") {
                context.startActivity(Intent(context, comoUsar::class.java))
            }
        }
        item {
            btn_clasico_shap_50f("Como usar Geinz (Tiendas)") {}
        }
    }
}

