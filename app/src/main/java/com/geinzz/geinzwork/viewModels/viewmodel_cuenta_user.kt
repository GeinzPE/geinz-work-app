package com.geinzz.geinzwork.viewModels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.geinzz.geinzwork.data.model.localizate_geinz.cuenta_user.cuenta_user
import com.geinzz.geinzwork.model.repo_cuenta_user
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

class viewmodel_cuenta_user: ViewModel() {

    private val instance = repo_cuenta_user()
    private lateinit var firebaseAuth: FirebaseAuth

    private val datos_user = MutableLiveData<cuenta_user>()
    val _datos_user: LiveData<cuenta_user> get() = datos_user

    fun obtener_datos_user() {
        viewModelScope.launch {
            try {
                val datos=instance.get_datos_user(firebaseAuth.uid.toString())
                datos_user.value=datos
            }catch (e: Exception){
                datos_user.value=cuenta_user()
            }
        }
    }
}