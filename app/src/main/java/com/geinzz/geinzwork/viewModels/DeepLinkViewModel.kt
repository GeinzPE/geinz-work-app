package com.geinzz.geinzwork.viewModels

import android.util.Log
import androidx.lifecycle.ViewModel
import com.geinzz.geinzwork.data.model.dataclass_promos.datos_para_promocieons_activas
import kotlinx.coroutines.flow.MutableStateFlow

class DeepLinkViewModel : ViewModel() {
    val pendingLinks = MutableStateFlow<List<String>>(emptyList())

    fun addLink(link: String) {
        pendingLinks.value = pendingLinks.value + link
    }

    fun consumeLink(link: String) {
        pendingLinks.value = pendingLinks.value.filter { it != link }
    }

    private val _promo = MutableStateFlow<datos_para_promocieons_activas?>(null)
    val promo = _promo

    fun setPromoData(id: String, lugar:String, index: Int) {
        Log.d("prmocenoasd","$id $lugar $index")
        _promo.value = datos_para_promocieons_activas(id, lugar, index.toString())
    }

    fun clearPromo() {
        _promo.value = null
    }
}
