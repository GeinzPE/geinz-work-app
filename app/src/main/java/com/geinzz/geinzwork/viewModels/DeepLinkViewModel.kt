package com.geinzz.geinzwork.viewModels

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow

class DeepLinkViewModel : ViewModel() {
    val pendingLinks = MutableStateFlow<List<String>>(emptyList())

    fun addLink(link: String) {
        pendingLinks.value = pendingLinks.value + link
    }

    fun consumeLink(link: String) {
        pendingLinks.value = pendingLinks.value.filter { it != link }
    }
}
