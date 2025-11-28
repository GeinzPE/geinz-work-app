package com.geinzz.geinzwork

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.geinzz.geinzwork.viewModels.viewmodel_carga_img_general

class viewmodel_carga_img_generalFactory(private val context: Context,) : ViewModel() {
    class Factory(private val context: Context) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return viewmodel_carga_img_general(context) as T
        }
    }
}