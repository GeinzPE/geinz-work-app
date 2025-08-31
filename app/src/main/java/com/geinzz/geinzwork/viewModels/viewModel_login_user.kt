package com.geinzz.geinzwork.viewModels

import android.R
import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.geinzz.geinzwork.data.model.localizate_geinz.login_geinz.login_google
import com.geinzz.geinzwork.data.model.localizate_geinz.login_geinz.login_user
import com.geinzz.geinzwork.model.repo_login_user
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.auth
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

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
                    delay(2000)
                    _loginStateCamposInicial.value = LoginState_inicio.Succes("", "", "", "")
                    delay(2000)
                    _registrado_google.value = cuenta_creada
                    _mostrarCarga.value = false
                }

            }
        } catch (e: Exception) {
            _registrado.value = false
            _mostrarCarga.value = false
            _loginStateCamposInicial.value = LoginState_inicio.error("", "")        }
    }

    fun loginWithGoogle(idToken: String?) {
        _mostrarCarga.value = true
        _loginStateCamposInicial.value = LoginState_inicio.Loading

        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d("correo_compatile", "1234")
                    val user = auth.currentUser
                    _loginStateCamposInicial.value = LoginState_inicio.Succes(
                        user?.email,
                        user?.displayName,
                        user?.photoUrl.toString(),
                        "google"
                    )
                    viewModelScope.launch {
                        delay(10_000)
                        _mostrarCarga.value = false
                    }
                } else {
                    Log.d("correo_compatile", "4321")
                    _mostrarCarga.value = false
                    _loginStateCamposInicial.value =
                        LoginState_inicio.error("", task.exception?.message.toString())
                }
            }
    }


    fun logear_user(correo: String, password: String) {
        _mostrarCarga.value = true
        when {
            correo.isBlank() && password.isBlank() -> {
                _mostrarCarga.value = false
                _loginStateCamposInicial.value =
                    LoginState_inicio.error(
                        "correo_no_existe",
                        "Correo y contraseña no pueden estar vacíos"
                    )
                return
            }

            correo.isBlank() -> {
                _mostrarCarga.value = false
                _loginStateCamposInicial.value =
                    LoginState_inicio.error("correo_no_existe", "El correo no puede estar vacío")
                return
            }

            password.isBlank() -> {
                _mostrarCarga.value = false
                _loginStateCamposInicial.value =
                    LoginState_inicio.error("pass_incorrecta", "La contraseña no puede estar vacía")
                return
            }
        }

        _loginStateCamposInicial.value = LoginState_inicio.Loading
        try {
            repo_agregar_user.logear_user(correo, password) { registrado, texto_registrado ->
                if (registrado) {
                    val user = auth.currentUser
                    _loginStateCamposInicial.value = LoginState_inicio.Succes(
                        email = "",
                        name = user?.displayName,
                        photoUrl = user?.photoUrl?.toString(), "normal"
                    )
                    viewModelScope.launch {
                        delay(10_000)
                        _mostrarCarga.value = false
                    }
                } else {
                    when (texto_registrado) {
                        "correo_no_existe" -> {
                            _loginStateCamposInicial.value =
                                LoginState_inicio.error(
                                    texto_registrado,
                                    "El correo no esta registrado, primero crea una cuenta"
                                )
                            _mostrarCarga.value = false
                        }

                        "pass_incorrecta" -> {
                            _loginStateCamposInicial.value =
                                LoginState_inicio.error(
                                    texto_registrado,
                                    "La contraseña es incorrecta"
                                )
                            _mostrarCarga.value = false
                        }

                        else -> {
                            _loginStateCamposInicial.value =
                                LoginState_inicio.error("", "Error desconocido")
                            _mostrarCarga.value = false
                        }
                    }
                }
            }
        } catch (e: Exception) {
            _loginStateCamposInicial.value =
                LoginState_inicio.error("", "Error desconocido")
            _mostrarCarga.value = false
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
                repo_agregar_user.buscar_correo(correo) { existe ->
                    correo_exist.value = existe
                }
            } catch (e: Exception) {
                correo_exist.value = false
            }
        }
    }

    fun verificar_cuenta_google_provider(correo: String, exista: (Boolean) -> Unit) {
        viewModelScope.launch {
            try {
                repo_agregar_user.validar_cuenta_existente_provider_google(correo) { existe ->
                    exista(existe)
                }
            } catch (e: Exception) {
                exista(false)
            }
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