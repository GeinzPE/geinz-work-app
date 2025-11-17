package com.geinzz.geinzwork.data.model

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.geinzz.geinzwork.model.repo_favoritos
import com.geinzz.geinzwork.viewModels.viewModel_favoritos

class FavoritosFactory(
    private val idUser: String
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(viewModel_favoritos::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return viewModel_favoritos(idUser) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
