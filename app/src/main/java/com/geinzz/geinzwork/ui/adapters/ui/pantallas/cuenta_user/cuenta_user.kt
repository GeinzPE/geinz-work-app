package com.geinzz.geinzwork.ui.adapters.ui.pantallas.cuenta_user

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.error
import coil3.request.placeholder
import com.geinzz.geinzwork.R
import com.geinzz.geinzwork.data_store.data_store_localidad
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.texto_generico_multilinea
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.texto_generico_one_line
import com.geinzz.geinzwork.ui.adapters.ui.dialog_general.spacer_horizonta
import com.geinzz.geinzwork.ui.adapters.ui.dialog_general.spacer_vertical
import com.geinzz.geinzwork.ui.adapters.ui.pantallas.bottom_sheet_general.eres_socio_geinz
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.constantes_lista_localidades.abrir_whattsapp
import com.geinzz.geinzwork.viewModels.LoginState_inicio
import com.geinzz.geinzwork.viewModels.viewModel_login_user
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

val firebaseAuth = FirebaseAuth.getInstance()

@Composable
fun cuenta_user(
    isConnected: Boolean,
    viewModel_login_user: viewModel_login_user,
    correo_registrado: String,
    navController: NavController,
    terminar_configurar: (String) -> Unit
) {
    val loginState_principal by viewModel_login_user.loginStateCamposInicial.collectAsState()
    val registrado_google = viewModel_login_user.registrado_google.observeAsState()
    val mostrar_btn_termianr_configurar by viewModel_login_user.mostrar_btn_terminar_configurar.collectAsState()
//    var falta_termianr_configurar_google by remember { mutableStateOf(false) }
//    var mostrar_btn_termianr_configurar by remember { mutableStateOf(false) }
    var mostrar_fondo by remember { mutableStateOf(true) }
//    var correo_registrado by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()


    LaunchedEffect(loginState_principal) {
        when (loginState_principal) {
            is LoginState_inicio.LoggedOut -> {
                navController.navigate("pantalla_principal") {
                    popUpTo("login_principal") { inclusive = true }
                    launchSingleTop = true
                }
            }

            else -> Unit
        }
    }
    LaunchedEffect(mostrar_btn_termianr_configurar) {
        if (mostrar_btn_termianr_configurar) {
            println("El botón se ocultó")
        } else {
            println("El botón se mostró")
        }
    }
    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {

        img_fondo_user(mostrar_fondo, R.drawable.logo_geinz_500x500, {
            mostrar_fondo = true
        })
        protada_perfil_user(
            isConnected = isConnected,
            terminar_configurar_btn = mostrar_btn_termianr_configurar,
            ocultar_contenido_Boolean = mostrar_fondo,
            ocultar_contenido = { mostrar_fondo = false },
            cerrar_seccion = {
                viewModel_login_user.logout()
            },
            terminar_configurar = {
                terminar_configurar(correo_registrado)
            })

    }


}

@Composable
fun img_fondo_user(
    ocultar_contenido_Boolean: Boolean, img_fondo: Int, ocultar_contenido: () -> Unit
) {
    val alphaCentro by animateFloatAsState(
        targetValue = if (ocultar_contenido_Boolean) 0.85f else 0f,
        animationSpec = tween(durationMillis = 300, easing = LinearOutSlowInEasing)
    )

    val alphaBordes by animateFloatAsState(
        targetValue = if (ocultar_contenido_Boolean) 1f else 1f,
        animationSpec = tween(durationMillis = 300, easing = LinearOutSlowInEasing)
    )
    Box(modifier = Modifier.fillMaxSize()) {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current).data(img_fondo)
                .placeholder(R.drawable.cargando_img_categorias)
                .error(R.drawable.cargando_img_categorias).build(),
            contentDescription = null,
            modifier = Modifier
                .fillMaxSize()
                .clickable {
                    ocultar_contenido()
                },
            contentScale = ContentScale.Crop
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Black.copy(alpha = alphaBordes),
                            Color.Black.copy(alpha = alphaCentro),
                            Color.Black.copy(alpha = alphaBordes)
                        )
                    )
                )
        )

    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun protada_perfil_user(
    isConnected: Boolean,
    terminar_configurar_btn: Boolean,
    ocultar_contenido_Boolean: Boolean,
    ocultar_contenido: () -> Unit,
    cerrar_seccion: () -> Unit,
    terminar_configurar: () -> Unit
) {
    val contex = LocalContext.current
    val scope = rememberCoroutineScope()
    val screenHeight = LocalConfiguration.current.screenHeightDp.dp
    var eres_socio by remember { mutableStateOf(false) }
    AnimatedVisibility(
        visible = ocultar_contenido_Boolean, enter = fadeIn(
            animationSpec = tween(
                durationMillis = 300, // duración más larga = más suave
                easing = LinearOutSlowInEasing // entra suavemente
            )
        ), exit = fadeOut(
            animationSpec = tween(
                durationMillis = 300, easing = FastOutLinearInEasing // sale suavemente
            )
        )
    ) {

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(screenHeight * 0.6f)
                .padding(horizontal = 20.dp)
                .clip(RoundedCornerShape(30.dp))
                .clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() }) {
                    ocultar_contenido()
                },
        ) {

            LazyColumn(
                modifier = Modifier.padding(horizontal = 20.dp)
            ) {
                item {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(R.drawable.logo_geinz_500x500)
                                .placeholder(R.drawable.cargando_img_categorias)
                                .error(R.drawable.cargando_img_categorias).build(),
                            contentDescription = "",
                            modifier = Modifier
                                .size(35.dp)
                                .clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                        spacer_horizonta(10.dp)
                        texto_generico_one_line("Geinz")
                    }
                    spacer_vertical(10.dp)
                }
                item {

                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        texto_generico_one_line(
                            "¡Gracias por ser parte de Geinz!", MaterialTheme.typography.titleLarge
                        )
                    }

                    spacer_vertical(10.dp)
                    texto_generico_multilinea(
                        "Tu apoyo nos impulsa a seguir creciendo y mejorando cada día. Este apartado aún está en desarrollo, pero muy pronto descubrirás nuevas funciones pensadas especialmente para ti y para toda nuestra comunidad.",
                        MaterialTheme.typography.bodyMedium
                    )
                    spacer_vertical(5.dp)
                    texto_generico_multilinea(
                        "Desde el equipo de desarrollo trabajamos con dedicación y mucho cariño para que cada detalle de la app te haga sentir cómodo, acompañado y orgulloso de pertenecer a este proyecto local que crece junto a ti.",
                        MaterialTheme.typography.bodyMedium
                    )
                    spacer_vertical(5.dp)
                    texto_generico_multilinea(
                        "Queremos que Geinz no sea solo una aplicación, sino un espacio que conecte personas, tiendas, servicios y sueños.",
                        MaterialTheme.typography.bodyMedium
                    )
                    spacer_vertical(5.dp)
                    texto_generico_multilinea(
                        "Gracias por tu paciencia, por tu confianza y por ayudarnos a construir algo grande desde nuestra tierra. Porque Geinz no solo crece… Geinz crece contigo.",
                        MaterialTheme.typography.bodyMedium
                    )
                    spacer_vertical(10.dp)
                }
                item {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            buildAnnotatedString {
                                withStyle(
                                    style = SpanStyle(
                                        color = Color.White
                                    )
                                ) {
                                    append("Whatsapp oficial de Geinz  ")
                                }
                                withStyle(
                                    style = SpanStyle(
                                        color = MaterialTheme.colorScheme.primary,
                                        textDecoration = TextDecoration.Underline,
                                        fontWeight = FontWeight.Medium
                                    )
                                ) {
                                    append(" +51 958 120 920")
                                }
                            },
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.clickable {
                                abrir_whattsapp(contex, "958 120 920")
                            })
                    }
                    spacer_vertical(10.dp)

                }

                item {
                    Text(
                        text = "Eres socio de Geinz?",
                        color = MaterialTheme.colorScheme.primary,
                        textDecoration = TextDecoration.Underline,
                        fontWeight = FontWeight.Medium,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.clickable {
                            eres_socio = true
                        })
                    spacer_vertical(20.dp)
                }

                item {

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 30.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary)
                            .clickable {
                                cerrar_seccion()
                                scope.launch {
                                    data_store_localidad.limpiar_datos_autenticacion(contex)
                                }

                            }, contentAlignment = Alignment.Center
                    ) {
                        texto_generico_one_line(
                            "cerrar sesión",
                            MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp)
                        )
                    }
                    spacer_vertical(10.dp)
                }

                item {

                    if (!terminar_configurar_btn) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 30.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary)
                                .clickable {
                                    terminar_configurar()
                                }, contentAlignment = Alignment.Center
                        ) {
                            texto_generico_one_line(
                                "Terminar de configurar cuenta",
                                MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp)
                            )
                        }
                    }
                }
            }

        }
    }
    if (eres_socio) {
        eres_socio_geinz (isConnected,"Benjamin",{eres_socio = false})
    }

}




