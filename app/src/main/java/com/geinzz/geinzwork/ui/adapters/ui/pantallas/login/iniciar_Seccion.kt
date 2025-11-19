package com.geinzz.geinzwork.ui.adapters.ui.pantallas.login

import android.annotation.SuppressLint
import android.util.Log
import android.widget.Toast
import androidx.compose.material3.CircularProgressIndicator
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.with
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.geinzz.geinzwork.R
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.MyOutlinedTextField
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.input_password
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.texto_generico_multilinea
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.texto_generico_one_line
import com.geinzz.geinzwork.ui.adapters.ui.dialog_general.spacer_horizonta
import com.geinzz.geinzwork.ui.adapters.ui.dialog_general.spacer_vertical
import com.geinzz.geinzwork.ui.adapters.ui.pantallas.bottom_sheet_general.ui_bottom_sheet_errores
import com.geinzz.geinzwork.ui.adapters.ui.ui.theme.banerGeinzWork
import com.geinzz.geinzwork.ui.adapters.ui.ui.theme.busquedaGeinzWork
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.constantes_lista_localidades
import com.geinzz.geinzwork.viewModels.LoginState_inicio
import com.geinzz.geinzwork.viewModels.viewModel_login_user
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException

import kotlinx.coroutines.delay


@SuppressLint("UnusedContentLambdaTargetStateParameter")
@OptIn(ExperimentalAnimationApi::class)
@Composable
fun IniciarSeccion(
    viewModel_login_user: viewModel_login_user,
    navController: NavController,
    listener_Crear_cuenta: (String) -> Unit,
) {
    val context = LocalContext.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val listaImg = constantes_lista_localidades.lista_img_local
    var show_bottom_sheet by remember { mutableStateOf(false) }

    val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
        .requestIdToken(context.getString(R.string.default_web_client_id))
        .requestEmail()
        .build()
    var mostar_errores_bottom_sheet by remember { mutableStateOf(false) }

    val googleSignInClient = GoogleSignIn.getClient(context, gso)
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account = task.getResult(ApiException::class.java)!!
            Log.d("LOGIN_GOOGLE", "Correo de Google: ${account.email}")
            viewModel_login_user.loginWithGoogle(navController, account.idToken) { correo ->
                listener_Crear_cuenta(correo)
            }
        } catch (e: Exception) {
            viewModel_login_user.registrarError(e.toString())
            Log.e("LOGIN_GOOGLE", "Excepción: ${e.message}", e)
        }
    }
    val frases = constantes_lista_localidades.lista_frases_login
    var index by remember { mutableStateOf(0) }

    LaunchedEffect(show_bottom_sheet) {
        if (!show_bottom_sheet) {
            while (true) {
                delay(6000L)
                index = (index + 1) % minOf(listaImg.size, frases.size)
            }
        }
    }
    var blurEnabled by remember { mutableStateOf(true) }

    val blurFixed = 16.dp



    Box(modifier = Modifier.fillMaxSize()) {
        AnimatedContent(
            targetState = index,
            transitionSpec = {
                fadeIn(animationSpec = tween(500)) with fadeOut(animationSpec = tween(500))
            }
        ) { index ->
            Image(
                painter = painterResource(id = listaImg[index]),
                contentDescription = null,
                modifier = Modifier
                    .fillMaxSize()
                    .clickable(
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() }
                    ) { blurEnabled = !blurEnabled }
                    .blur(if (blurEnabled) blurFixed else 0.dp),
                contentScale = ContentScale.Crop
            )
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color.Black.copy(alpha = if (blurEnabled) 0.5f else 0f),
                            Color.Black.copy(alpha = if (blurEnabled) 0.55f else 0f)
                        ),
                        startY = 0f,
                        endY = 400f
                    )
                )
        )


        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color.Black.copy(alpha = if (blurEnabled) 0.5f else 0f)
                        )
                    )
                )
        )



        Box(modifier = Modifier.align(Alignment.BottomCenter)) {
            AnimatedVisibility(
                visible = blurEnabled,
                enter = fadeIn(animationSpec = tween(500)),
                exit = fadeOut(animationSpec = tween(500))
            ) {
                Column {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(R.drawable.logo_geinz_blanco),
                            contentDescription = "",
                            modifier = Modifier.size(70.dp).clickable{
//                                mostar_errores_bottom_sheet=true
                            }
                        )
                    }
                    spacer_vertical(5.dp)
                    fraces_filtrado(frases, index)
                    spacer_vertical(10.dp)
                    login_principal_apartado(
                        Modifier
                            .padding(20.dp),
                        { show_bottom_sheet = true },
                        { listener_Crear_cuenta("crear") },
                        {
                            val signInIntent = googleSignInClient.signInIntent
                            launcher.launch(signInIntent)
                        }
                    )
                }
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(40.dp)
                .align(Alignment.BottomCenter)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color.Black
                        )
                    )
                )
            // aplicamos el fade
        )

        if (show_bottom_sheet) {
            bottom_sheet_login(
                viewModel_login_user,
                show_bottom_sheet,
                { show_bottom_sheet = false },
                 { correo, contra ->
                    viewModel_login_user.logear_user(navController, correo, contra)
                    keyboardController?.hide()
                })
        }
        if(mostar_errores_bottom_sheet){
            ui_bottom_sheet_errores(viewModel_login_user,{mostar_errores_bottom_sheet=false})
        }
    }
}

@Composable
fun fraces_filtrado(fraces: List<String>, index1: Int) {
    Crossfade(targetState = index1, label = "fraces") { txt ->
        Text(
            text = fraces[txt],
            style = MaterialTheme.typography.banerGeinzWork,
            fontSize = 30.sp,
            textAlign = TextAlign.Center,
            maxLines = 2,
            minLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 15.dp)
        )


    }
}

@Composable
fun login_principal_apartado(
    modifier: Modifier,
    listener_iniciar_seccion_geinz: () -> Unit,
    listener_Crear_cuenta_geinz: () -> Unit,
    listener_continuar_con_google: () -> Unit,
) {
    Column(modifier = modifier.imePadding()) {
        spacer_vertical(10.dp)
        btn_principal_iniciar_seccion { listener_iniciar_seccion_geinz() }
        spacer_vertical(15.dp)
        btn_continuar_con_google { listener_continuar_con_google() }
        spacer_vertical(15.dp)
        crear_cuenta_geinz { listener_Crear_cuenta_geinz() }
        spacer_vertical(15.dp)
    }

}

@SuppressLint("RememberReturnType")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun bottom_sheet_login(
    viewModel_login_user: viewModel_login_user,
    show: Boolean,
    onClose: () -> Unit,
    listener_iniciar_seccion_geinz: (correo: String, contra: String) -> Unit,
) {
    val context = LocalContext.current
    val cambio_password by viewModel_login_user.recuperar_contra.observeAsState()
    val loginState_principal by viewModel_login_user.loginStateCamposInicial.collectAsState()

    var correo by remember(show) { mutableStateOf("") }
    var password by remember(show) { mutableStateOf("") }
    var error_pass by remember(show) { mutableStateOf(false) }
    var error_correo by remember(show) { mutableStateOf(false) }
    var texto_error_correo by remember(show) { mutableStateOf("") }
    var texto_error_contra by remember(show) { mutableStateOf("") }
    var cambiar_contra by remember(show) { mutableStateOf(true) }
    var contra_oculta by rememberSaveable(show) { mutableStateOf(true) }
    var mostara_carga_progres by remember { mutableStateOf(false) }


    Log.d("error_correo", error_correo.toString())

    LaunchedEffect(show) {
        if (show) {
            correo = ""
            password = ""
            error_pass = false
            error_correo = false
            texto_error_contra = ""
            texto_error_correo = ""
        }
    }

    LaunchedEffect(cambio_password) {
        if (cambio_password == true) {
            Toast.makeText(context, "Correo de recuperación enviado", Toast.LENGTH_SHORT).show()
            error_correo = false
            texto_error_correo = ""
            error_pass = false
            texto_error_contra = ""
            cambiar_contra = true
            onClose()
            viewModel_login_user.restaurar_valor_recupear_contra()
        }
    }
    LaunchedEffect(loginState_principal) {
        Log.d("entramos1234","entramossssss")
        when (val state = loginState_principal) {
            is LoginState_inicio.error -> {
                delay(700)
                when (state.tipo) {
                    "correo_no_existe" -> {
                        Log.d("entramos1234","correo_no_existe")

                        error_correo = true
                        texto_error_correo = state.msje
                        error_pass = false
                        texto_error_contra = ""
                        mostara_carga_progres=false
                    }

                    "pass_incorrecta" -> {

                        Log.d("entramos1234","pass_incorrecta")
                        error_pass = true
                        texto_error_contra = state.msje
                        error_correo = false
                        texto_error_correo = ""
                        mostara_carga_progres=false
                    }

                    else -> {

                        error_correo = false
                        texto_error_correo = ""
                        error_pass = false
                        texto_error_contra = ""
                        mostara_carga_progres=false

                    }
                }
//                delay(200)
//                viewModel_login_user.resetLoginState()
            }


            is LoginState_inicio.Succes -> {
                error_correo = false
                texto_error_correo = ""
                error_pass = false
                texto_error_contra = ""
                onClose()
            }

            LoginState_inicio.Loading ->{
                onClose()
                mostara_carga_progres=true

            }
            LoginState_inicio.LoggedOut -> {

            }

            else -> {
                error_correo = false
                texto_error_correo = ""
                error_pass = false
                texto_error_contra = ""


            }
        }
    }

    ModalBottomSheet(
        onDismissRequest = {
            error_pass = false
            error_correo = false
            texto_error_contra = ""
            texto_error_correo = ""
            password = ""
            onClose()
            viewModel_login_user.resetLoginState()
        },
        modifier = Modifier.fillMaxWidth(),
        dragHandle = null,
        containerColor = Color.Black
    ) {
        Column(
            modifier = Modifier.padding(vertical = 25.dp, horizontal = 15.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = if (cambiar_contra) {
                    "Accede a tu cuenta"
                } else {
                    "Restablecer contraseña"
                },
                style = MaterialTheme.typography.busquedaGeinzWork.copy(
                    color = Color.White,
                    shadow = Shadow(
                        color = Color.White.copy(alpha = 0.8f),
                        offset = Offset(0f, 0f),
                        blurRadius = 16f
                    )
                ),
                textAlign = TextAlign.Center,
                maxLines = 2,
                modifier = Modifier.padding(horizontal = 10.dp)
            )
            spacer_vertical(10.dp)
            texto_generico_multilinea(
                if (cambiar_contra) {
                    "Ingresa tu cuenta Geinz. Las cuentas registradas con Google no son compatibles."
                } else {
                    "Escribe tu correo electrónico y te enviaremos un enlace para restablecer tu contraseña."
                },
                MaterialTheme.typography.bodyMedium
            )
            spacer_vertical(10.dp)
            MyOutlinedTextField(
                value = correo,
                onValueChange = {
                    correo = it
                    if (error_correo) {
                        error_correo = false
                    }
                },
                labelText = "Correo electrónico",
                placeholderText = "Escribe tu correo electrónico",
                texto_error = texto_error_correo,
                keyboardType = KeyboardType.Email,
                isError = error_correo,
            )
            spacer_vertical(10.dp)
            AnimatedVisibility(
                cambiar_contra,
            ) {
                input_password(
                    contra_oculta,
                    error_pass,
                    texto_error_contra,
                    password,
                    { contra_oculta = !contra_oculta },
                    {
                        password = it
                        if (error_pass) {
                            error_pass = false
                        }
                    }
                )

            }
            if (cambiar_contra) {
                spacer_vertical(20.dp)
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 5.dp)
                    .clickable(
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() }
                    ) {
                        cambiar_contra = !cambiar_contra
                        error_pass = false
                        error_correo = false
                        texto_error_contra = ""
                        texto_error_correo = ""
                        password = ""
                    }
            ) {

                texto_generico_one_line(
                    texto = if (cambiar_contra) {
                        "Olvidaste tu contraseña?"
                    } else {
                        "Regresar al login"
                    }
                )
            }

            spacer_vertical(20.dp)
            iniciar_seccion_normal(
                mostrarCarga = mostara_carga_progres,
                textoBtn = if (cambiar_contra) {
                    "Iniciar seccion"
                } else {
                    "Recuperar contraseña"
                }, esLogin = cambiar_contra, onLogin = {
                    if (correo.isEmpty() || password.isEmpty()) {
                        viewModel_login_user.verificar_campos(correo, password)
                        mostara_carga_progres=true
                    } else {
                        viewModel_login_user.resetLoginState()
                        listener_iniciar_seccion_geinz(correo, password)
                        mostara_carga_progres=true
                    }
                }, onRecuperar = {
                    if (correo.isBlank()) {
                        error_correo = true
                        texto_error_correo = "El campo es obligatorio"
                    } else {
                        error_correo = false
                        texto_error_correo = ""
                        viewModel_login_user.recuperar_password(correo)
                    }
                })
            spacer_vertical(10.dp)

        }
    }
}


@Composable
fun btn_continuar_con_google(listener_continuar_con_google: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(50))
            .background(Color.White)
            .padding(vertical = 12.dp)
            .clickable { listener_continuar_con_google() }
    ) {
        Image(
            painter = painterResource(R.drawable.gmail_img),
            contentDescription = "",
            modifier = Modifier
                .size(40.dp)
                .align(Alignment.CenterStart)
                .padding(start = 16.dp)
        )

        Text(
            "Continuar con Google",
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.titleSmall,
            color = Color.Black,
            modifier = Modifier.align(Alignment.Center)
        )
    }
}

@Composable
fun crear_cuenta(listner_crear_cuenta: () -> Unit) {
    Text(
        text = "Crear cuenta",
        modifier = Modifier.clickable { listner_crear_cuenta() },
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.onBackground,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis
    )
}

@Composable
fun iniciar_seccion_normal(
    mostrarCarga: Boolean,
    textoBtn: String,
    esLogin: Boolean = true,
    onLogin: () -> Unit,
    onRecuperar: () -> Unit
) {
    Button(
        onClick = {
            if (esLogin) {
                onLogin()
            } else {
                onRecuperar()
            }
        },
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            texto_generico_one_line(
                textoBtn,
                MaterialTheme.typography.titleSmall,
                modifier = Modifier.padding(vertical = 17.dp)
            )

            if (mostrarCarga) {
                spacer_horizonta(8.dp)
                CircularProgressIndicator(
                    modifier = Modifier
                        .size(18.dp),
                    color = Color.White,
                    strokeWidth = 2.dp
                )
            }
        }

    }
}


@Composable
fun btn_principal_iniciar_seccion(listener_bottom_sheet: () -> Unit) {
    Button(
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary
        ), modifier = Modifier.fillMaxWidth(), onClick = { listener_bottom_sheet() }) {
        texto_generico_one_line(
            "Iniciar sesión",
            MaterialTheme.typography.titleSmall,
            modifier = Modifier.padding(vertical = 17.dp)
        )
    }
}

@Composable
fun crear_cuenta_geinz(listener_Crear_cuenta: () -> Unit) {
    Column(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            texto_generico_one_line("Aun no tienes cuenta ?", MaterialTheme.typography.bodyMedium)
            spacer_horizonta(7.dp)
            crear_cuenta { listener_Crear_cuenta() }
        }
    }


}