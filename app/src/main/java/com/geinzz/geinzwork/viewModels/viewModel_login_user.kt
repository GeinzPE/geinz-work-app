package com.geinzz.geinzwork.viewModels

import android.R
import android.content.Context
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
import kotlinx.coroutines.launch

class viewModel_login_user : ViewModel() {
    val repo_agregar_user = repo_login_user()

    private val _loginState = MutableLiveData<LoginState>()
    val loginState: LiveData<LoginState> = _loginState

    private val _loginStateCamposInicial = MutableLiveData<LoginState_inicio?>()
    val loginStateCamposInicial: LiveData<LoginState_inicio?> = _loginStateCamposInicial

    private val auth: FirebaseAuth = Firebase.auth

    private val _registrado = MutableLiveData<Boolean>()

    val registrado_boolean: LiveData<Boolean> get() = _registrado

    private val _registrado_google = MutableLiveData<Boolean>()

    val registrado_google: LiveData<Boolean> get() = _registrado_google

    private val nombre_userexists = MutableLiveData<Boolean>()
    val _nombre_userexists: LiveData<Boolean> get() = nombre_userexists
    fun agregar_user(login_user: login_user, context: Context) {
        try {
            repo_agregar_user.agregar_user(login_user, context) { registrado ->
                _registrado.value = registrado
            }
        } catch (e: Exception) {
            _registrado.value = false
        }
    }

    fun agregar_user_google(login_google: login_google, context: Context) {
        try {
            repo_agregar_user.agregar_user_google(login_google, context) { cuenta_creada ->
                _registrado_google.value = cuenta_creada
            }
        } catch (e: Exception) {
            _registrado_google.value = false
        }
    }

    fun loginWithGoogle(idToken: String?) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)

        auth.signInWithCredential(credential)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    _loginState.value = LoginState.Success(
                        email = user?.email,
                        name = user?.displayName,
                        photoUrl = user?.photoUrl.toString()
                    )
                } else {
                    _loginState.value = LoginState.Error(task.exception?.message)
                }
            }
    }

    fun logear_user(correo: String, password: String) {
        _loginStateCamposInicial.value = LoginState_inicio.Loading
        try {
            repo_agregar_user.logear_user(correo, password) { registrado, texto_registrado ->
                if (registrado) {
                    val user = auth.currentUser
                    _loginStateCamposInicial.value = LoginState_inicio.Succes(
                        name = user?.displayName,
                        photoUrl = user?.photoUrl?.toString()
                    )
                } else {
                    when (texto_registrado) {
                        "correo_no_existe" -> {
                            _loginStateCamposInicial.value =
                                LoginState_inicio.error(
                                    texto_registrado,
                                    "El correo no esta registrado"
                                )
                        }

                        "pass_incorrecta" -> {
                            _loginStateCamposInicial.value =
                                LoginState_inicio.error(
                                    texto_registrado,
                                    "La contraseña es incorrecta"
                                )
                        }

                        else -> {
                            _loginStateCamposInicial.value =
                                LoginState_inicio.error("", "Error desconocido")
                        }
                    }
                }
            }
        } catch (e: Exception) {
            _loginStateCamposInicial.value =
                LoginState_inicio.error("", "Error desconocido")
        }
    }

    fun resetLoginState() {
        _loginStateCamposInicial.value = null
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

}


sealed class LoginState {
    data class Success(val email: String?, val name: String?, val photoUrl: String?) : LoginState()
    data class Error(val message: String?) : LoginState()
    object Loading : LoginState()
}

sealed class LoginState_inicio {
    data class Succes(val name: String?, val photoUrl: String?) : LoginState_inicio()
    data class error(val tipo: String, val msje: String) : LoginState_inicio()
    object Loading : LoginState_inicio()
}
