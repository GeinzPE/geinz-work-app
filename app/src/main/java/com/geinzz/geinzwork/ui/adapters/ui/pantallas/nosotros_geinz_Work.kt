package com.geinzz.geinzwork.ui.adapters.ui.pantallas

import androidx.compose.foundation.Image
import com.geinzz.geinzwork.R
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material.Button
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.texto_generico_multilinea
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.titulos_genericos_one_line
import com.geinzz.geinzwork.ui.adapters.ui.dialog_general.spacer_vertical
import java.time.format.TextStyle

@Preview
@Composable
fun nosotros_geinz_work() {
    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            item {
                Box() {
                    Image(
                        painter = painterResource(R.drawable.logo_geinz_circular),
                        contentDescription = "",
                        modifier = Modifier.size(40.dp)
                    )
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.Center)
                    ) {
                        titulos_genericos_one_line(
                            "Geinz work", MaterialTheme.typography.headlineSmall,
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
            }
            item {
                texto_generico_multilinea("Quienes somos?",MaterialTheme.typography.titleLarge)
                texto_generico_multilinea(
                    "Geinz es una empresa especializada en el desarrollo de" +
                            "soluciones digitales, desde aplicaciones Android hasta" +
                            "sitios web dinámicos y funcionales. En nuestro equipo," +
                            "combinamos la experiencia técnica con la creatividad" +
                            "para ofrecer a nuestros clientes herramientas digitales" +
                            "que impulsan su éxito en el mundo digital."
                )
            }
            item {
                texto_generico_multilinea("Nuestro Enfoque")
                texto_generico_multilinea(
                    "Nos comprometemos a entender a fondo las necesidades\n" +
                            "y metas de nuestros clientes para ofrecer soluciones digitales\n" +
                            "que agreguen valor y potencien su presencia en línea."
                )
            }
            item {
                texto_generico_multilinea("Contactanos")

                texto_generico_multilinea(
                    "¿Tienes una idea o proyecto en mente? ¡Estamos aquí para\n" +
                            "convertirlo en realidad! Contáctanos hoy mismo y descubre\n" +
                            "cómo podemos ayudarte a alcanzar tus objetivos en el mundo"
                )
            }
            item {
                texto_generico_multilinea("Acciones rapidas en Geinz work")
            }
            item {
                btn_acciones_rapidas()
            }
        }

    }
}

@Composable
fun btn_acciones_rapidas() {
    LazyRow() {
        item {
            Button(onClick = {}) { Text("Crea tu publicidad") }
        }
        item {
            Button(onClick = {}) { Text("Crea tu noticia") }
        }
        item {
            Button(onClick = {}) { Text("Como usar Geinz (Trabajadores)") }
        }
        item {
            Button(onClick = {}) { Text("Como usar Geinz (Tiendas)") }
        }
    }
}

