package com.geinzz.geinzwork.viewModels

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.geinzz.geinzwork.data.model.localizate_geinz.login_geinz.login_google
import com.geinzz.geinzwork.data.model.localizate_geinz.login_geinz.login_user
import com.geinzz.geinzwork.model.repo_login_user
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.auth
import com.google.firebase.firestore.model.mutation.ArrayTransformOperation
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class viewModel_login_user : ViewModel() {
    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()

    val repo_agregar_user = repo_login_user()

    private val _loginState = MutableLiveData<LoginState?>()
    val loginState: LiveData<LoginState?> = _loginState

    private val _loginStateCamposInicial = MutableLiveData<LoginState_inicio?>()
    val loginStateCamposInicial: LiveData<LoginState_inicio?> = _loginStateCamposInicial

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

    fun agregar_user(login_user: login_user, context: Context) {
        viewModelScope.launch {
            _mostrarCarga.value = true // mostrar loader desde ya
            try {
                repo_agregar_user.agregar_user(login_user, context) { registrado ->
                    // 🔹 esperar medio segundo antes de actualizar el estado
                    viewModelScope.launch {
                        delay(2000)
                        _loginStateCamposInicial.value = LoginState_inicio.Succes("", "", "", "")
                        delay(2000)
                        _registrado.value = registrado
                        _mostrarCarga.value = false // ocultar loader
                    }
                }
            } catch (e: Exception) {
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
                    if (cuenta_creada) {  // ✅ solo si la cuenta se creó correctamente
                        delay(2000)        // mostrar loader
                        _loginStateCamposInicial.value = LoginState_inicio.Succes("", "", "", "")
                        delay(2000)
                        _registrado_google.value = true
                    } else {
                        _registrado_google.value = false
                        _loginStateCamposInicial.value = LoginState_inicio.error("", "")
                    }
                    _mostrarCarga.value = false
                }
            }
        } catch (e: Exception) {
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
                _loginStateCamposInicial.value = LoginState_inicio.error("", e.message.toString())
            } finally {
                _mostrarCarga.value = false
            }
        }
    }


    fun logear_user(navController: NavController, correo: String, password: String) {
        viewModelScope.launch {
            _mostrarCarga.value = true

            // 🔹 Validaciones iniciales
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

            _loginStateCamposInicial.value = LoginState_inicio.Loading

            try {
                // 🔹 Llamada suspend del repositorio
                val (registrado, texto_registrado) = repo_agregar_user.logear_user(correo, password)

                if (registrado) {
                    val user = auth.currentUser
                    _loginStateCamposInicial.value = LoginState_inicio.Succes(
                        email = correo,
                        name = user?.displayName,
                        photoUrl = user?.photoUrl?.toString(),
                        proveedor = "normal"
                    )
                    navController.navigate("pantalla_principal") {
                        popUpTo("login_principal") { inclusive = true }
                    }
                } else {
                    when (texto_registrado) {
                        "correo_no_existe" -> _loginStateCamposInicial.value =
                            LoginState_inicio.error(
                                texto_registrado,
                                "El correo no está registrado, primero crea una cuenta"
                            )

                        "pass_incorrecta" -> _loginStateCamposInicial.value =
                            LoginState_inicio.error(
                                texto_registrado,
                                "La contraseña es incorrecta"
                            )

                        else -> _loginStateCamposInicial.value =
                            LoginState_inicio.error("", "Error desconocido")
                    }
                }
            } catch (e: Exception) {
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
            firebaseAuth.signOut()
            delay(1000)
            _loginStateCamposInicial.value = LoginState_inicio.LoggedOut
            delay(1000)
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