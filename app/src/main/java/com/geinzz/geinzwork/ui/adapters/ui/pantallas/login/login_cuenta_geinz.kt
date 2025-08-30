package com.geinzz.geinzwork.ui.adapters.ui.pantallas.login

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.geinzz.geinzwork.data.model.localizate_geinz.login_geinz.login_google
import com.geinzz.geinzwork.data.model.localizate_geinz.login_geinz.login_user
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.DateButton
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.ExpandDropDown
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.MyOutlinedTextField
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.PhoneNumberWithPicker
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.SeleccionarPais
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.campos_correo_contra
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.input_email_user_name
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.texto_generico_multilinea
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.texto_generico_one_line
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.verificarCampo
import com.geinzz.geinzwork.ui.adapters.ui.dialog_general.spacer_vertical
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.constantes_lista_localidades
import com.geinzz.geinzwork.viewModels.viewModel_login_user
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.delay

val options_genero = listOf("Masculino", "Femenino", "Otro")
val opciones_localida = listOf("Barranca", "Supe", "Puerto supe", "Paramonga", "Pativilca")
private lateinit var firebaseAuth: FirebaseAuth

@Composable
fun login_principal(tipo_cuenta: String, navController: NavController) {
    Log.d("tipo_cuenta", tipo_cuenta)
    componentes_crear_cuenta(tipo_cuenta, navController)
}

@Composable
fun componentes_crear_cuenta(tipo_cuenta: String, navController: NavController) {
    val context = LocalContext.current
    val viewmodel_login: viewModel_login_user = viewModel()
    val registrado = viewmodel_login.registrado_boolean.observeAsState()
    val registrado_google = viewmodel_login.registrado_google.observeAsState()
    val correo_exsit = viewmodel_login._correo_exist.observeAsState()
    val usernameExiste by viewmodel_login._nombre_userexists.observeAsState(false)
    var correo by rememberSaveable(tipo_cuenta) {
        mutableStateOf(
            if (tipo_cuenta == "crear") "" else tipo_cuenta
        )
    }
    var enable_correo by rememberSaveable(tipo_cuenta) {
        mutableStateOf(
            if (tipo_cuenta == "crear") true else false
        )
    }
    var phone by rememberSaveable { mutableStateOf("") }
    var nombre by rememberSaveable { mutableStateOf("") }
    var apellido by rememberSaveable { mutableStateOf("") }
    var genero by rememberSaveable { mutableStateOf("") }
    var localidad by rememberSaveable { mutableStateOf("") }
    var fechaNacimiento by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var password2 by rememberSaveable { mutableStateOf("") }
    var error_texto_username by remember { mutableStateOf("El campo es obligatorio") }
    var username by rememberSaveable { mutableStateOf("") }
    var error_texto_correo by remember { mutableStateOf("") }
    var cod_pais_params by remember { mutableStateOf("") }
    var nombre_pais_params by remember { mutableStateOf("") }


    var nombre_pais_nacionalidad by remember { mutableStateOf("") }
    var cod_pais_nacionalidad by remember { mutableStateOf("") }

    var errorNombre by remember { mutableStateOf(false) }
    var errorApellido by remember { mutableStateOf(false) }
    var errorCorreo by remember { mutableStateOf(false) }
    var errorGenero by remember { mutableStateOf(false) }
    var errorLocalidad by remember { mutableStateOf(false) }
    var errorFechaNacimiento by remember { mutableStateOf(false) }
    var errorTelefono by remember { mutableStateOf(false) }
    var error_pass1 by remember { mutableStateOf(false) }
    var error_pass2 by remember { mutableStateOf(false) }
    var errorUsername by remember { mutableStateOf(false) }
    var error_nacionalidad by remember { mutableStateOf(false) }

    var nombreTocado by remember { mutableStateOf(false) }
    var apellido_tocado by remember { mutableStateOf(false) }
    var numero_tocado by remember { mutableStateOf(false) }
    var correo_tocado by remember { mutableStateOf(false) }
    var contra1_tocado by remember { mutableStateOf(false) }
    var contra2_tocado by remember { mutableStateOf(false) }

    var cargandoUsername by remember { mutableStateOf(false) }
    var username_tocado by remember { mutableStateOf(false) }

    var error_texto_pass1 by remember { mutableStateOf("") }
    var error_texto_pass2 by remember { mutableStateOf("") }

    errorNombre = nombreTocado && nombre.length == 0
    errorApellido = apellido_tocado && apellido.length == 0
    errorTelefono = numero_tocado && phone.length == 0
    errorCorreo = correo_tocado && correo.isEmpty()

    LaunchedEffect(correo, correo_tocado) {
        if (correo_tocado) {
            if (correo.isEmpty()) {
                errorCorreo = true
                error_texto_correo = "El campo es obligatorio"
                return@LaunchedEffect
            }

            if (!constantes_lista_localidades.esGmailValido(correo)) {
                errorCorreo = true
                error_texto_correo = "Debe ser un correo Gmail válido"
                return@LaunchedEffect
            }

            // Si está ok localmente, recién consultamos en Firestore
            viewmodel_login.verificar_exist_correo(correo)
        }
    }

    LaunchedEffect(correo_exsit.value, correo) {
        if (!constantes_lista_localidades.esGmailValido(correo)) {
            // Si el correo ya no es válido, reseteamos el error remoto
            return@LaunchedEffect
        }

        if (correo_exsit.value == true) {
            errorCorreo = true
            error_texto_correo = "El correo ya está registrado"
        } else {
            errorCorreo = false
            error_texto_correo = ""
        }
    }



    LaunchedEffect(username) {
        // Primero validamos si el usuario ya tocó el campo y lo dejó vacío
        if (username_tocado && username.isEmpty()) {
            errorUsername = true
            cargandoUsername = false
            error_texto_username = "El campo es obligatorio"
            return@LaunchedEffect
        }

        // Verificamos si hay espacios
        if (username.contains(" ")) {
            error_texto_username = "No puede contener espacios"
            errorUsername = true
            return@LaunchedEffect
        }

        // Solo validar si tiene más de 3 caracteres
        if (username.length > 3) {
            cargandoUsername = true
            delay(500)
            viewmodel_login.verificar_exist_nombre_user(username)
            cargandoUsername = false
        } else {
            // No mostrar error si aún no está tocado o longitud insuficiente pero no vacío
            errorUsername = false
            error_texto_username = "Tiene que ser mayor a 3 letras"
        }
    }


    LaunchedEffect(usernameExiste) {
        error_texto_username = if (usernameExiste) "Nombre de usuario ya existe" else ""
        errorUsername = usernameExiste
    }
    var mostrarCarga by remember { mutableStateOf(false) }

    LaunchedEffect(registrado.value) {

        if (registrado.value == true) {
            mostrarCarga = true
            delay(5000)
            navController.navigate("pantalla_principal") {
                popUpTo("login_principal") { inclusive = true }
            }
        }
    }

    LaunchedEffect(registrado_google.value) {
        if (registrado_google.value == true) {
            navController.navigate("pantalla_principal") {
                popUpTo("login_principal") { inclusive = true }
            }
        }
    }

    LaunchedEffect(password, password2, contra1_tocado, contra2_tocado) {

        if (contra1_tocado) {
            when {
                password.isEmpty() -> {
                    error_pass1 = true
                    error_texto_pass1 = "Este campo es obligatorio"
                }

                password.length < 3 -> {
                    error_pass1 = true
                    error_texto_pass1 = "La contraseña debe tener al menos 3 caracteres"
                }

                else -> {
                    error_pass1 = false
                    error_texto_pass1 = ""
                }
            }
        }

        // Validar segundo campo
        if (contra2_tocado) {
            when {
                password2.isEmpty() -> {
                    error_pass2 = true
                    error_texto_pass2 = "Este campo es obligatorio"
                }

                password != password2 -> {
                    error_pass2 = true
                    error_texto_pass2 = "Las contraseñas no coinciden"
                }

                else -> {
                    error_pass2 = false
                    error_texto_pass2 = ""
                }
            }
        }
    }



    LazyColumn(
        modifier = Modifier
            .padding(10.dp)
            .imePadding()
    ) {
        if (tipo_cuenta.equals("crear")) {
            item {
                texto_generico_one_line(
                    "Crea tu cuenta en Geinz Work",
                    MaterialTheme.typography.headlineSmall
                )
                spacer_vertical(10.dp)
            }
        } else {
            item {
                texto_generico_one_line(
                    "Termina de crear tu cuenta Geinz",
                    MaterialTheme.typography.headlineSmall
                )
                spacer_vertical(10.dp)
            }
        }
        item {
            texto_generico_multilinea(
                "Descubre las novedades de tu localidad, conecta con las tiendas de tu zona y mantente siempre al tanto de lo que sucede cerca de ti.",
                MaterialTheme.typography.bodyMedium
            )
            spacer_vertical(10.dp)
        }
        // nombre
        item {
            MyOutlinedTextField(
                value = nombre,
                onValueChange = {
                    nombre = it
                    if (!nombreTocado) {
                        nombreTocado = true
                    }
                },
                labelText = "Nombre",
                placeholderText = "Escribe tu nombre completo",
                texto_error = "El campo es obligatorio",
                isError = errorNombre,
            )
            spacer_vertical(8.dp)
        }
        // Apellido
        item {
            MyOutlinedTextField(
                value = apellido,
                onValueChange = {
                    apellido = it
                    if (!apellido_tocado) {
                        apellido_tocado = true
                    }
                },
                labelText = "Apellido",
                placeholderText = "Escribe tu apellido",
                texto_error = "El campo es obligatorio",
                isError = errorApellido,
            )
            spacer_vertical(8.dp)
        }
        // Nombre de usuario
        item {
            input_email_user_name(
                value = username,
                onValueChange = {
                    username = it
                    if (!username_tocado) {
                        username_tocado = true
                    }
                },
                labelText = "Nombre de usuario",
                placeholderText = "Escribe tu nombre de usuario",
                texto_error = error_texto_username,
                isError = errorUsername,
                trailingIconContent = {
                    when {
                        cargandoUsername -> {
                            CircularProgressIndicator(
                                modifier = Modifier
                                    .size(20.dp)
                                    .padding(2.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        errorUsername -> {
                            Icon(Icons.Filled.Error, "Error", tint = Color.Red)
                        }

                        username.length > 3 -> {
                            Icon(Icons.Default.Check, "Correcto", tint = Color(0xFF4CAF50))
                        }
                    }
                }
            )
            spacer_vertical(10.dp)
        }
        // Número celular
        item {
            PhoneNumberWithPicker(
                phoneNumber = phone,
                onPhoneNumberChange = { nuevo ->
                    if (nuevo.all { it.isDigit() }) {
                        phone = nuevo
                        if (!numero_tocado) {
                            numero_tocado = true
                        }
                    }
                },
                isError = errorTelefono,
                texto_error = "El campo es obligatorio"
            ) { cod_pais, nombre_pais ->
                cod_pais_params = cod_pais
                nombre_pais_params = nombre_pais
            }

            spacer_vertical(10.dp)
        }
        // Género
        item {
            ExpandDropDown(
                options_genero,
                errorGenero,
                "Seleciona tu genero",
                "Seleciona tu genero"
            ) { genero_selecionado ->
                genero = genero_selecionado
                errorGenero = false
            }
            spacer_vertical(12.dp)
        }
        // Localidad
        item {
            ExpandDropDown(
                opciones_localida,
                errorLocalidad,
                "Seleciona tu localidad",
                "Seleciona tu localidad"
            ) { localida_selecionada ->
                localidad = localida_selecionada
                errorLocalidad = false
            }
            spacer_vertical(12.dp)
        }
        //nacionalidad
        item {
            SeleccionarPais(
                { nombre_pais, cod_pais ->
                    nombre_pais_nacionalidad = nombre_pais
                    cod_pais_nacionalidad = cod_pais
                },
                error_nacionalidad, nombre_pais_nacionalidad,
            )
            spacer_vertical(10.dp)
        }

        //Fecha
        item {
            DateButton(errorFechaNacimiento, "El campo es obligatorio") { fecha_obtenida ->
                fechaNacimiento = fecha_obtenida
                errorFechaNacimiento = false
            }
            spacer_vertical(10.dp)
        }
        //Correo electronico
        item {
            MyOutlinedTextField(
                value = correo,
                onValueChange = {
                    correo = it
                    if (!correo_tocado) {
                        correo_tocado = true
                    }
                },
                labelText = "Correo electrónico",
                texto_error = error_texto_correo,
                placeholderText = "Escribe tu correo electrónico",
                keyboardType = KeyboardType.Email,
                isError = errorCorreo,
                enabled = enable_correo
            )
            spacer_vertical(10.dp)
        }

        if (tipo_cuenta.equals("crear")) {
            item {
                campos_correo_contra(
                    password = password,
                    onPasswordChange = {
                        password = it
                        if (!contra1_tocado) {
                            contra1_tocado = true
                        }
                    },
                    password2 = password2,
                    onPassword2Change = {
                        password2 = it
                        if (!contra2_tocado) {
                            contra2_tocado = true
                        }
                    },
                    error_pass1,
                    error_pass2, error_texto_pass1, error_texto_pass2
                )
                spacer_vertical(10.dp)
            }
        }

        if (tipo_cuenta.equals("crear")) {
            //crear cuenta
            item {
                Button(onClick = {
                    errorNombre = verificarCampo(nombre)
                    errorApellido = verificarCampo(apellido)
                    errorUsername = verificarCampo(username)
                    errorCorreo = verificarCampo(correo)
                    errorGenero = verificarCampo(genero)
                    errorLocalidad = verificarCampo(localidad)
                    errorFechaNacimiento = verificarCampo(fechaNacimiento)
                    errorTelefono = verificarCampo(phone)
                    error_pass1 = verificarCampo(password)
                    error_pass2 = verificarCampo(password2)
                    error_nacionalidad = verificarCampo(nombre_pais_nacionalidad)


                    val hayError = listOf(
                        errorNombre,
                        errorApellido,
                        errorUsername,
                        errorCorreo,
                        errorGenero,
                        errorLocalidad,
                        errorFechaNacimiento,
                        errorTelefono, error_pass1, error_pass2, error_nacionalidad
                    ).any { it }

                    if (!hayError) {
                        val datos = login_user(
                            nombre = nombre,
                            apellido = apellido,
                            nombre_user = username,
                            correo = correo,
                            numero_celular = phone.toIntOrNull() ?: 0,
                            cod_telefeno = cod_pais_params,
                            nacionalidad_numero = nombre_pais_params,
                            genero = genero,
                            localidad = localidad,
                            fecha_nac = fechaNacimiento,
                            password = password,
                            nacionalidad_nacimiento = nombre_pais_nacionalidad,
                            cod_pais = cod_pais_nacionalidad
                        )
                        viewmodel_login.agregar_user(datos, context)


                    } else {
                        Toast.makeText(context, "Complete todo los campos", Toast.LENGTH_SHORT)
                            .show()
                    }
                }) {
                    texto_generico_one_line("Crear cuenta")
                }
            }
        } else {
            item {
                Button(onClick = {
                    errorNombre = verificarCampo(nombre)
                    errorApellido = verificarCampo(apellido)
                    errorUsername = verificarCampo(username)
                    errorGenero = verificarCampo(genero)
                    errorLocalidad = verificarCampo(localidad)
                    errorFechaNacimiento = verificarCampo(fechaNacimiento)
                    errorTelefono = verificarCampo(phone)
                    error_nacionalidad = verificarCampo(nombre_pais_nacionalidad)


                    val hayError = listOf(
                        errorNombre,
                        errorApellido,
                        errorUsername,
                        errorCorreo,
                        errorGenero,
                        errorLocalidad,
                        errorFechaNacimiento,
                        errorTelefono, error_nacionalidad
                    ).any { it }

                    if (!hayError) {
                        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return@Button
                        val datos = login_google(
                            nombre = nombre,
                            apellido = apellido,
                            nombre_user = username,
                            correo = correo,
                            id = uid,
                            numero_celular = phone.toIntOrNull() ?: 0,
                            cod_telefeno = cod_pais_params,
                            nacionalidad_numero = nombre_pais_params,
                            genero = genero,
                            localidad = localidad,
                            fecha_nac = fechaNacimiento,
                            nacionalidad_nacimiento = nombre_pais_nacionalidad,
                            cod_pais = cod_pais_nacionalidad


                        )
                        Log.d("campos_login_google", datos.toString())
                        viewmodel_login.agregar_user_google(datos, context)

                    } else {
                        Toast.makeText(context, "Complete todo los campos", Toast.LENGTH_SHORT)
                            .show()

                    }
                }) {
                    texto_generico_one_line("terminar de configurar")
                }

            }
        }


    }
}











