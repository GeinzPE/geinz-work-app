package com.geinzz.geinzwork.viewModels

import android.content.Context
import androidx.lifecycle.ViewModel
import com.geinzz.geinzwork.data.model.localizate_geinz.login_geinz.login_user
import com.geinzz.geinzwork.model.repo_login_user

class viewModel_login_user: ViewModel() {
    val repo_agregar_user= repo_login_user()

    fun agregar_user(login_user: login_user,context: Context){
        repo_agregar_user.agregar_user(login_user,context)
    }
}