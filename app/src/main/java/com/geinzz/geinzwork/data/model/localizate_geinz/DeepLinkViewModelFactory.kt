package com.geinzz.geinzwork.data.model.localizate_geinz

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.geinzz.geinzwork.model.SessionRepository
import com.geinzz.geinzwork.viewModels.DeepLinkViewModel

class DeepLinkViewModelFactory(
    private val sessionRepository: SessionRepository
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DeepLinkViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return DeepLinkViewModel(sessionRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel")
    }
}