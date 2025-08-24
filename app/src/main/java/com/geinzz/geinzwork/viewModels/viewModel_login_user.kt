package com.geinzz.geinzwork.viewModels

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.geinzz.geinzwork.data.model.localizate_geinz.login_geinz.login_user
import com.geinzz.geinzwork.model.repo_login_user
import com.google.firebase.Firebase
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import kotlinx.coroutines.launch

class viewModel_login_user : ViewModel() {
    val repo_agregar_user = repo_login_user()

    private val auth: FirebaseAuth = Firebase.auth
    fun agregar_user(login_user: login_user, context: Context) {
        repo_agregar_user.agregar_user(login_user, context)
    }

    fun login_con_google(credencial: AuthCredential, home: () -> Unit) = viewModelScope.launch {
        try {
            auth.signInWithCredential(credencial).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d("logeado", "logeado exitosamnete")
                }
            }.addOnFailureListener { Log.d("logeado", "fallo al logear con google") }
        } catch (e: Exception) {
            Log.d("logeado", "fallo_logeo$e")
        }
    }


}