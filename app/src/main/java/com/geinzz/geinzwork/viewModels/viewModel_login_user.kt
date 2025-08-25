package com.geinzz.geinzwork.viewModels

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.geinzz.geinzwork.data.model.localizate_geinz.login_geinz.login_google
import com.geinzz.geinzwork.data.model.localizate_geinz.login_geinz.login_user
import com.geinzz.geinzwork.model.repo_login_user
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.common.api.ApiException
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.auth
import kotlinx.coroutines.launch

class viewModel_login_user : ViewModel() {
    val repo_agregar_user = repo_login_user()

    private val _loginState = MutableLiveData<LoginState>()
    val loginState: LiveData<LoginState> = _loginState

    private val auth: FirebaseAuth = Firebase.auth

    fun agregar_user(login_user: login_user, context: Context) {
        repo_agregar_user.agregar_user(login_user, context)
    }

    fun agregar_user_google(login_google: login_google, context: Context) {
        repo_agregar_user.agregar_user_google(login_google, context)
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
}

sealed class LoginState {
    data class Success(val email: String?, val name: String?, val photoUrl: String?) : LoginState()
    data class Error(val message: String?) : LoginState()
    object Loading : LoginState()
}