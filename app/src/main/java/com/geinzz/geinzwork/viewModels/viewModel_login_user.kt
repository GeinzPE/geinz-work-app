package com.geinzz.geinzwork.viewModels

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.geinzz.geinzwork.data.model.localizate_geinz.login_geinz.login_google
import com.geinzz.geinzwork.data.model.localizate_geinz.login_geinz.login_user
import com.geinzz.geinzwork.model.repo_login_user
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.constantes_lista_localidades.eliminarTokenDispositivo
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.auth
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await



class viewModel_login_user : ViewModel() {
    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()

    val repo_agregar_user = repo_login_user()

    private val _loginState = MutableLiveData<LoginState?>()
    val loginState: LiveData<LoginState?> = _loginState

    private val _loginStateCamposInicial = MutableStateFlow<LoginState_inicio?>(null)
    val loginStateCamposInicial: StateFlow<LoginState_inicio?> = _loginStateCamposInicial


    private val auth: FirebaseAuth = Firebase.auth

    private val _registrado = MutableLiveData<Boolean>()
    val registrado_boolean: LiveData<Boolean> get() = _registrado


    private val _registrado_google = MutableLiveData<Boolean>()

    val registrado_google: LiveData<Boolean> get() = _registrado_google

    private val nombre_userexists = MutableLiveData<Boolean>()
    val _nombre_userexists: LiveData<Boolean> get() = nombre_userexists

    private val correo_exist = MutableLiveData<Boolean>()
    val _correo_exist: LiveData<Boolean> get() = correo_exist

    private val _mostrarCarga = MutableLiveData(false)
    val mostrarCarga: LiveData<Boolean> = _mostrarCarga

    private val _recupera_contra = MutableLiveData(false)
    val recuperar_contra: LiveData<Boolean> get() = _recupera_contra

    val _lista_errores = MutableStateFlow<List<String>>(emptyList())

    private val _mostrar_bn_terminar_configurar = MutableStateFlow<Boolean> (false)
    val mostrar_btn_terminar_configurar : StateFlow<Boolean> = _mostrar_bn_terminar_configurar

    val errores = _lista_errores.value

    fun registrarError(error: String) {
        _lista_errores.value = _lista_errores.value + error
    }


    fun recuperar_password(correo: String) {
        viewModelScope.launch {
            try {
                firebaseAuth.sendPasswordResetEmail(correo)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            _recupera_contra.value = true
                        } else {
                            _recupera_contra.value = false

                        }
                    }
            } catch (e: Exception) {
                _recupera_contra.value = false
            }
        }
    }

    fun restaurar_valor_recupear_contra(){
        _recupera_contra.value=false
    }

    fun agregar_user(login_user: login_user, context: Context) {
        viewModelScope.launch {
            _mostrarCarga.value = true // mostrar loader desde ya
            try {
                repo_agregar_user.agregar_user(login_user, context) { registrado ->
                    // 🔹 esperar medio segundo antes de actualizar el estado
                    viewModelScope.launch {
                        delay(5000)
                        _loginStateCamposInicial.value = LoginState_inicio.Succes("", "", "", "")
                        delay(5000)
                        _registrado.value = registrado
                        _mostrarCarga.value = false // ocultar loader
                    }
                }
            } catch (e: Exception) {
                registrarError(e.toString())
                _registrado.value = false
                _mostrarCarga.value = false
                _loginStateCamposInicial.value = LoginState_inicio.error("", "")
            }
        }
    }


    fun agregar_user_google(login_google: login_google, context: Context) {
        _mostrarCarga.value = true
        try {
            repo_agregar_user.agregar_user_google(login_google, context) { cuenta_creada ->
                viewModelScope.launch {
                    if (cuenta_creada) {
                        delay(5000)
                        _loginStateCamposInicial.value = LoginState_inicio.Succes("", "", "", "")
                        delay(5000)
                        _registrado_google.value = true
                    } else {
                        _registrado_google.value = false
                        _loginStateCamposInicial.value = LoginState_inicio.error("", "")
                    }
                    _mostrarCarga.value = false
                }
            }
        } catch (e: Exception) {
            registrarError(e.toString())
            _registrado_google.value = false
            _mostrarCarga.value = false
            _loginStateCamposInicial.value = LoginState_inicio.error("", "")
        }
    }


    fun loginWithGoogle(
        navController: NavController,
        idToken: String?,
        listener_Crear_cuenta: (String) -> Unit
    ) {
        viewModelScope.launch {
            _mostrarCarga.value = true
            _loginStateCamposInicial.value = LoginState_inicio.Loading

            try {
                val credential = GoogleAuthProvider.getCredential(idToken, null)
                val authResult =
                    auth.signInWithCredential(credential).await()  // 🔹 await de Firebase Auth
                val user = authResult.user
                val correo = user?.email ?: ""

                val existe =
                    repo_agregar_user.verificar_cuenta_google(correo) // suspend function que chequea Firestore
                delay(5000)
                if (existe) {
                    _loginStateCamposInicial.value = LoginState_inicio.Succes(
                        correo,
                        user?.displayName,
                        user?.photoUrl.toString(),
                        "google"
                    )
                    navController.navigate("pantalla_principal") {
                        popUpTo("login_principal") { inclusive = true }
                    }
                } else {
                    listener_Crear_cuenta(correo)
                }

            } catch (e: Exception) {
                registrarError(e.toString())
                _loginStateCamposInicial.value = LoginState_inicio.error("", e.message.toString())
            } finally {
                _mostrarCarga.value = false
            }
        }
    }


    fun verificar_campos( correo: String, password: String){
        viewModelScope.launch {
            when {
                correo.isBlank() && password.isBlank() -> {
                    _loginStateCamposInicial.value =
                        LoginState_inicio.error(
                            "correo_no_existe",
                            "Correo y contraseña no pueden estar vacíos"
                        )
                    _mostrarCarga.value = false
                    return@launch
                }

                correo.isBlank() -> {
                    _loginStateCamposInicial.value =
                        LoginState_inicio.error(
                            "correo_no_existe",
                            "El correo no puede estar vacío"
                        )
                    _mostrarCarga.value = false
                    return@launch
                }

                password.isBlank() -> {
                    _loginStateCamposInicial.value =
                        LoginState_inicio.error(
                            "pass_incorrecta",
                            "La contraseña no puede estar vacía"
                        )
                    _mostrarCarga.value = false
                    return@launch
                }
            }
        }
    }

    fun logear_user(navController: NavController, correo: String, password: String) {
        viewModelScope.launch {

            try {
                Log.d("entramos123","campos llenos emvepaz<mos")
                // 🔹 Llamada suspend del repositorio
                val (registrado, texto_registrado) = repo_agregar_user.logear_user(correo, password)
                when (texto_registrado) {
                    "correo_no_existe" -> {
                        Log.d("entramos123","correo_no_existe")
                        _loginStateCamposInicial.value = LoginState_inicio.error(
                            texto_registrado,
                            "El correo no está registrado, primero crea una cuenta"
                        )
                        return@launch // o simplemente return si estás fuera de launch
                    }

                    "pass_incorrecta" -> {
                        Log.d("entramos123","pass_incorrecta")
                        _loginStateCamposInicial.value = LoginState_inicio.error(
                            texto_registrado,
                            "La contraseña es incorrecta"
                        )
                        return@launch
                    }

                    "error_desconocido" -> {
                        Log.d("entramos123","error_desconocido")
                        _loginStateCamposInicial.value = LoginState_inicio.error(
                            texto_registrado,
                            "Error desconocido"
                        )
                        return@launch
                    }
                }

                if (registrado) {
                    _loginStateCamposInicial.value = LoginState_inicio.Loading
                    Log.d("entramos123","$registrado")
                    _mostrarCarga.value = true
                    val user = auth.currentUser
                    delay(2000)
                    _loginStateCamposInicial.value = LoginState_inicio.Succes(
                        email = correo,
                        name = user?.displayName,
                        photoUrl = user?.photoUrl?.toString(),
                        proveedor = "normal"
                    )
                    navController.navigate("pantalla_principal") {
                        popUpTo("login_principal") { inclusive = true }
                    }
                    delay(2000)

                } else {
                    _loginStateCamposInicial.value = LoginState_inicio.error(
                        "",
                        "Error desconocido"
                    )
                }
            } catch (e: Exception) {
                registrarError(e.toString())
                _loginStateCamposInicial.value =
                    LoginState_inicio.error("", "Error desconocido")
            } finally {
                _mostrarCarga.value = false
            }
        }
    }


    fun resetLoginState() {
        _loginStateCamposInicial.value = null
    }

    fun logout() {
        viewModelScope.launch {
            _mostrarCarga.value = true
            _loginStateCamposInicial.value = LoginState_inicio.Loading
            eliminarTokenDispositivo(firebaseAuth.uid.toString())
            firebaseAuth.signOut()
            delay(5000)
            _loginStateCamposInicial.value = LoginState_inicio.LoggedOut
            delay(5000)
            _mostrarCarga.value = false
        }
    }


    fun verificar_exist_nombre_user(nombre_user: String) {
        viewModelScope.launch {
            try {
                repo_agregar_user.buscar_nombre_user(nombre_user) { existe ->
                    nombre_userexists.value = existe
                }
            } catch (e: Exception) {
                nombre_userexists.value = false
            }
        }
    }

    fun verificar_exist_correo(correo: String) {
        viewModelScope.launch {
            try {
                val existe = repo_agregar_user.buscar_correo_suspense(correo)
                correo_exist.value = existe

            } catch (e: Exception) {
                correo_exist.value = false
            }
        }
    }

    suspend fun verificar_cuenta_google_provider(correo: String): Boolean {
        return try {
            repo_agregar_user.verificar_cuenta_google(correo)
        } catch (e: Exception) {
            false
        }
    }


    fun setLoading() {
        _loginState.value = LoginState.Loading
    }

    fun setSuccess(email: String?, name: String?, photoUrl: String?) {
        _loginState.value = LoginState.Success(email, name, photoUrl)
    }

    fun setError(message: String?) {
        _loginState.value = LoginState.Error(message)
    }

    fun clearState() {
        _loginState.value = null
    }
}


sealed class LoginState {
    data class Success(val email: String?, val name: String?, val photoUrl: String?) : LoginState()
    data class Error(val message: String?) : LoginState()
    object Loading : LoginState()
    object LoggedOut : LoginState()
}

sealed class LoginState_inicio {
    data class Succes(
        val email: String?,
        val name: String?,
        val photoUrl: String?,
        val proveedor: String?
    ) : LoginState_inicio()

    data class error(val tipo: String, val msje: String) : LoginState_inicio()
    object Loading : LoginState_inicio()
    object LoggedOut : LoginState_inicio()
}