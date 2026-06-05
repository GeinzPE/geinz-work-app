package com.geinzz.geinzwork.ui.adapters.ui.pantallas.bottom_sheet_general

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import coil3.request.CachePolicy
import coil3.request.ImageRequest
import coil3.request.error
import coil3.request.placeholder
import com.geinzz.geinzwork.R
import com.geinzz.geinzwork.data.model.dataclass_novedades.compartir_promocion
import com.geinzz.geinzwork.data.model.localizate_geinz.HorarioAtencion
import com.geinzz.geinzwork.data.model.localizate_geinz.HorarioAtencion_box
import com.geinzz.geinzwork.data.model.localizate_geinz.HorarioBloque
import com.geinzz.geinzwork.data.model.localizate_geinz.HorarioDia
import com.geinzz.geinzwork.data.model.localizate_geinz.HorarioDia_bloques
import com.geinzz.geinzwork.data.model.localizate_geinz.contacto_numero
import com.geinzz.geinzwork.data.model.localizate_geinz.contacto_red
import com.geinzz.geinzwork.data.model.localizate_geinz.inicio_geinz.datos_tienda_free
import com.geinzz.geinzwork.data.model.localizate_geinz.metodo_contacto_tienda
import com.geinzz.geinzwork.data.model.localizate_geinz.modelo_metodo_individual
import com.geinzz.geinzwork.data.model.localizate_geinz.modelo_pagos_tienda
import com.geinzz.geinzwork.data.model.localizate_geinz.modelo_tienda
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.TextoExpandibleEnLinea
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.texto_generico_multilinea
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.texto_generico_one_line
import com.geinzz.geinzwork.ui.adapters.ui.ZoomableGalleryFullScreen
import com.geinzz.geinzwork.ui.adapters.ui.dialog_general.dialog_sin_pago_tiendas
import com.geinzz.geinzwork.ui.adapters.ui.dialog_general.spacer_horizonta
import com.geinzz.geinzwork.ui.adapters.ui.dialog_general.spacer_vertical
import com.geinzz.geinzwork.ui.adapters.ui.ui.theme.baners_geinz_work
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.constantes_lista_localidades.FuenteControladaApp
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.constantes_lista_localidades.abrir_whattsapp
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.constantes_lista_localidades.capitalizeFirst
import com.geinzz.geinzwork.viewModels.viewModel_filtado_tiendas


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun bottom_sheet_ayudanos_a_creccer(
    id_user:String,
    verificar_intener: Boolean,
    localidad: String,
    ondimis: () -> Unit,
    viewModelFiltros: viewModel_filtado_tiendas,

    ) {
    var hacer_visible_btn by remember { mutableStateOf(false) }
    var seleccion by remember { mutableStateOf("") }
    var visible_free by remember { mutableStateOf(false) }
    var visible_primiun by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val ejemploTienda = modelo_tienda(
        nombre_tienda = "Nombre de tu negocio",
        modelo_negocio = true,
        localidad = "Barranca",
        categoria_tienda = "categoria de tu negocio",
        descripcion = "Descripcion que ofrece tu negocio",
        id_tienda = "T001",
        img_perfil = "https://firebasestorage.googleapis.com/v0/b/geinzworkapp.appspot.com/o/imagenesSubidasPc%2Fcargando_img_categorias.webp?alt=media&token=bf51a285-1ba0-42ec-9597-bb2720408573",
        lista_img = listOf(
            "https://firebasestorage.googleapis.com/v0/b/geinzworkapp.appspot.com/o/imagenesSubidasPc%2Fcargando_img_categorias.webp?alt=media&token=bf51a285-1ba0-42ec-9597-bb2720408573",
            "https://firebasestorage.googleapis.com/v0/b/geinzworkapp.appspot.com/o/imagenesSubidasPc%2Fcargando_img_categorias.webp?alt=media&token=bf51a285-1ba0-42ec-9597-bb2720408573",
            "https://firebasestorage.googleapis.com/v0/b/geinzworkapp.appspot.com/o/imagenesSubidasPc%2Fcargando_img_categorias.webp?alt=media&token=bf51a285-1ba0-42ec-9597-bb2720408573",
            "https://firebasestorage.googleapis.com/v0/b/geinzworkapp.appspot.com/o/imagenesSubidasPc%2Fcargando_img_categorias.webp?alt=media&token=bf51a285-1ba0-42ec-9597-bb2720408573"
        ),
        subcategoria = listOf("sub1", "sub2", "sub3"),
        ubicacion = mapOf(
            "dirección" to "Urb. San Mateo Mz I Lote 4",
            "latitud" to -10.753888095424601,
            "longitud" to -77.75939583663396,
            "referencia" to "Frente al paradero de autos para Supe"
        ),
        pagado = true,
        metodo_contacto_tienda = metodo_contacto_tienda(
            whatsapp = contacto_numero(true, "987654321"),
            llamada = contacto_numero(true, "987654321"),
            instagram = contacto_red(true, "Tu nombre de perfil"),
            facebook = contacto_red(true, "Tu nombre de perfil"),
            tiktok = contacto_red(true, "Tu nombre de perfil"),
            sitio_web = contacto_red(true, "Tu nombre sitio web (opcional)"),
        ),
        horario_atencion = HorarioAtencion(
            lunes = HorarioDia(cerrado = false, h_apertura = "07:00", h_cierre = "20:00"),
            martes = HorarioDia(cerrado = false, h_apertura = "07:00", h_cierre = "20:00"),
            miercoles = HorarioDia(cerrado = false, h_apertura = "07:00", h_cierre = "20:00"),
            jueves = HorarioDia(cerrado = false, h_apertura = "07:00", h_cierre = "20:00"),
            viernes = HorarioDia(cerrado = false, h_apertura = "07:00", h_cierre = "20:00"),
            sabado = HorarioDia(cerrado = false, h_apertura = "07:00", h_cierre = "20:00"),
            domingo = HorarioDia(
                cerrado = true,
                h_apertura = "07:00",
                h_cierre = "20:00",
                motivo = "Día de descanso"
            )
        ),
        horario_tienda_box = HorarioAtencion_box(
            lunes = HorarioDia_bloques(
                cerrado = false,
                motivo = "",
                bloques = listOf(
                    HorarioBloque("07:00", "13:00"),
                    HorarioBloque("14:00", "23:00")
                )
            ),

            martes = HorarioDia_bloques(
                cerrado = false,
                motivo = "",
                bloques = listOf(
                    HorarioBloque("07:00", "13:00"),
                    HorarioBloque("14:00", "23:00")
                )
            ),

            miércoles = HorarioDia_bloques(
                cerrado = false,
                motivo = "",
                bloques = listOf(
                    HorarioBloque("07:00", "13:00"),
                    HorarioBloque("14:00", "23:00")
                )
            ),

            jueves = HorarioDia_bloques(
                cerrado = false,
                motivo = "",
                bloques = listOf(
                    HorarioBloque("07:00", "13:00"),
                    HorarioBloque("14:00", "23:00")
                )
            ),

            viernes = HorarioDia_bloques(
                cerrado = false,
                motivo = "",
                bloques = listOf(
                    HorarioBloque("07:00", "13:00"),
                    HorarioBloque("14:00", "23:00")
                )
            ),

            sábado = HorarioDia_bloques(
                cerrado = false,
                motivo = "",
                bloques = listOf(
                    HorarioBloque("07:00", "13:00"),
                    HorarioBloque("14:00", "23:00")
                )
            ),

            domingo = HorarioDia_bloques(
                cerrado = true,
                motivo = "Cerrado",
                bloques = listOf(
                    HorarioBloque("07:00", "13:00"),
                    HorarioBloque("14:00", "23:00")
                )
            )
        )

        ,
        metodos_pago_tienda = modelo_pagos_tienda(
            plin = modelo_metodo_individual(enable = true),
            agora = modelo_metodo_individual(enable = true),
            efectivo = modelo_metodo_individual(enable = true),
            yape = modelo_metodo_individual(enable = true, numero = "987654321"),
            visa_mastercard = modelo_metodo_individual(enable = true)
        ),timestamp="1764693289927"
    )

    var ejemplo_tienda_free = datos_tienda_free(
        "Nombre de tu negocio",
        "https://firebasestorage.googleapis.com/v0/b/geinzworkapp.appspot.com/o/imagenesSubidasPc%2Fcargando_img_categorias.webp?alt=media&token=bf51a285-1ba0-42ec-9597-bb2720408573",
        "ubicacion de tu negocio",
        "referencia de tu negocio",
        "8AM a 10PM"
    )

    val contex = LocalContext.current
    ModalBottomSheet(
        sheetState = sheetState,
        onDismissRequest = { ondimis() },
        containerColor = MaterialTheme.colorScheme.background,
    ) {
        FuenteControladaApp {
            Box(contentAlignment = Alignment.Center) {

                Column() {
                    val lsita_img = listOf(
                        "https://firebasestorage.googleapis.com/v0/b/geinzworkapp.appspot.com/o/imagenesSubidasPc%2F1.webp?alt=media&token=f6d1d503-8938-499c-8ded-455dc3272964",
                        "https://firebasestorage.googleapis.com/v0/b/geinzworkapp.appspot.com/o/imagenesSubidasPc%2F2.webp?alt=media&token=5dc2e4ec-5c54-437a-a9da-f0b8132a537d",
                        "https://firebasestorage.googleapis.com/v0/b/geinzworkapp.appspot.com/o/imagenesSubidasPc%2F3.webp?alt=media&token=518e8264-9f77-4636-a597-082b87185a25",
                        "https://firebasestorage.googleapis.com/v0/b/geinzworkapp.appspot.com/o/imagenesSubidasPc%2F4.webp?alt=media&token=f8b42b6c-ef97-4593-a9f2-edfff06a6ccd"
                    )
                    Text(
                        text = "Conoces algun negocio? o Quieres registrar tu negocio en Geinz?",
                        fontFamily = baners_geinz_work,
                        fontSize = 22.sp, modifier = Modifier.padding(horizontal = 10.dp)
                    )

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp),
                        modifier = Modifier.padding(vertical = 10.dp)
                    ) {
                        items(lsita_img) { img ->
                            img_baner_informativo(id_usser = id_user,img)
                        }
                    }
                    Column(modifier = Modifier.padding(horizontal = 10.dp)) {

                        texto_generico_multilinea(
                            "Tanto si conoces un negocio como si eres dueño de uno, contáctanos directamente,Nuestro equipo verificará la información y realizará una visita al local para confirmar los datos,asegurando que mas negocios formen parte de Geinz",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        spacer_vertical(5.dp)
                        texto_generico_multilinea(
                            "De esta forma, ayudamos a que más personas descubran lugares auténticos de ${localidad.capitalizeFirst()} y sus alrededores,fortaleciendo nuestra guía local y apoyando el crecimiento de los emprendedores de la zona.",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        spacer_vertical(10.dp)
//                        Text(
//                            text = "¿Quieres ver cómo se vería tu negocio en Geinz?",
//                            color = MaterialTheme.colorScheme.primary,
//                            modifier = Modifier.clickable(
//                                interactionSource = remember { MutableInteractionSource() },
//                                indication = null,
//                            ) {
//                                hacer_visible_btn = !hacer_visible_btn
//                                visible_free = false
//                                visible_primiun = false
//                                seleccion = ""
//                            },
//                            textDecoration = TextDecoration.Underline,
//                            style = MaterialTheme.typography.bodyMedium,
//                        )
//                        spacer_vertical(10.dp)
//                        AnimatedVisibility(hacer_visible_btn, modifier = Modifier.fillMaxWidth()) {
//                            LazyRow(
//                                modifier = Modifier.fillMaxWidth()
//                            ) {
//
//                                item {
//                                    // 🔹 Ficha Premium
//                                    Box(modifier = Modifier.width(300.dp)) {
//                                        FichaOpcion(
//                                            titulo = "Ficha Premium (S/0.34 diario o S/10 mensual)",
//                                            imagen = R.drawable.logo_geinz_blanco,
//                                            seleccionado = seleccion == "premium",
//                                            onClick = {
//                                                seleccion = "premium"
//                                                visible_free = false
//                                                visible_primiun = true
//                                            }
//                                        )
//                                    }
//                                }
//                                item {
//                                    spacer_horizonta(20.dp)
//                                }
//
//                                item {
//                                    // 🔹 Ficha Gratis
//                                    Box(modifier = Modifier.width(300.dp)) {
//                                        FichaOpcion(
//                                            titulo = "Ficha Gratis (sin costo)",
//                                            imagen = R.drawable.logo_geinz_blanco,
//                                            seleccionado = seleccion == "gratis",
//                                            onClick = {
//                                                seleccion = "gratis"
//                                                visible_free = true
//                                                visible_primiun = false
//                                            }
//                                        )
//                                    }
//                                }
//                            }
//                        }
                        Box(
                            modifier = Modifier
                                .padding(top = 10.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary)
                                .clickable {
                                    abrir_whattsapp(
                                        contex,
                                        "958120920",
                                        "Hola 👋, quiero compartir información sobre un negocio para que forme parte de Geinz."
                                    )
                                }
                                .fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "Contactar con Geinz",
                                modifier = Modifier.padding(vertical = 15.dp),
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                        spacer_vertical(20.dp)
                    }
                }


                if (visible_free) {
                    dialog_sin_pago_tiendas(
                        mostrandoCarga_free = false,
                        datos_tienda_free = ejemplo_tienda_free,
                        ondimis = {
                            visible_free = false
                            viewModelFiltros.resetear_estado_sin_pago()
                        })
                }
//                if (visible_primiun) {
//                    bottom_sheet_tiendas_filtradas(
//                        verificar_intener,
//                        viewModelFiltros,
//                        ejemploTienda,
//                        visible_primiun,
//                        false,
//                        onClose = { visible_primiun = false })
//
//                }

            }
        }
    }
}

@Composable
fun img_baner_informativo(id_usser:String,img: String) {
    var mostar_img by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .width(200.dp)
            .height(300.dp)
            .clip(RoundedCornerShape(8.dp))
            .clickable { mostar_img = true }
    ) {
        // Imagen
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(img)
                .memoryCachePolicy(CachePolicy.ENABLED)
                .diskCachePolicy(CachePolicy.ENABLED)
                .error(R.drawable.cargando_img_categorias)
                .placeholder(R.drawable.cargando_img_categorias)
                .build(),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        // 🔹 Máscara negra translúcida suave
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Black.copy(alpha = 0.3f), // arriba más suave
                            Color.Black.copy(alpha = 0.4f)  // abajo un poco más oscuro
                        )
                    )
                )
        )
    }

    // 🔹 Pantalla de zoom
    if (mostar_img) {
        ZoomableGalleryFullScreen(
            id_usser,
            compartir_promocion(),
            imagenes = listOf(img),
            startIndex = 0,
            onDismiss = { mostar_img = false }
        )
    }
}


@Composable
fun FichaOpcion(
    titulo: String,
    imagen: Int,
    seleccionado: Boolean,
    onClick: () -> Unit
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(20.dp))
                .background(
                    if (seleccionado) MaterialTheme.colorScheme.primary
                    else Color.Gray.copy(alpha = 0.5f)
                )
                .fillMaxWidth()
                .height(100.dp)
                .clickable { onClick() },
            contentAlignment = Alignment.Center
        ) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(imagen)
                    .build(),
                contentDescription = null,
                modifier = Modifier
                    .size(100.dp)
                    .graphicsLayer {
                        if (!seleccionado) alpha = 0.5f
                    },
            )

            if (!seleccionado) {
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .background(Color.Black.copy(alpha = 0.3f))
                )
            }
        }

        spacer_vertical(10.dp)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp),
            contentAlignment = Alignment.Center
        ) {
            TextoExpandibleEnLinea(titulo)
        }
//        texto_generico_one_line(
//            titulo,
//            MaterialTheme.typography.bodyMedium.copy(
//                color = if (seleccionado)
//                    MaterialTheme.colorScheme.primary
//                else
//                    Color.Gray
//            )
//        )
    }
}