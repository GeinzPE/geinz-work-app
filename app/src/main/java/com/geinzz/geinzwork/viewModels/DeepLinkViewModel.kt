package com.geinzz.geinzwork.viewModels

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.geinzz.geinzwork.data.model.dataclass_promos.datos_para_promocieons_activas
import com.geinzz.geinzwork.model.SessionRepository
import com.geinzz.geinzwork.model.repo_pantallas_promocionar
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

class DeepLinkViewModel(private val sessionRepository: SessionRepository) : ViewModel() {
    val pendingLinks = MutableStateFlow<List<String>>(emptyList())
    val insta_pantallas_promo = repo_pantallas_promocionar()

    fun addLink(link: String) {
        pendingLinks.value = pendingLinks.value + link
    }

    fun consumeLink(link: String) {
        pendingLinks.value = pendingLinks.value.filter { it != link }
    }

    private val _promo = MutableStateFlow<datos_para_promocieons_activas?>(null)
    val promo = _promo


    fun setPromoData(id: String, lugar: String, index: Int) {
        Log.d("prmocenoasd", "$id $lugar $index")
        _promo.value = datos_para_promocieons_activas(id, lugar, index.toString(), "")
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun setPromo_notificacion(tipo:String,id_tienda: String, lugar: String, id_promo: String) {
        viewModelScope.launch {
            try {
            val uid = sessionRepository.getUidOnce()
                _promo.value = datos_para_promocieons_activas(id_tienda, lugar, "", id_promo)
                if(tipo.equals("promo_notificacion")){
                insta_pantallas_promo.registrarEventoNotificacion(
                    localidadTienda = lugar,
                    idTienda = id_tienda,
                    idPromo = id_promo,
                    idUser = uid,
                    evento = repo_pantallas_promocionar.EventoNotificacion.VISTA
                )

                insta_pantallas_promo.registrarEventoNotificacion(
                    localidadTienda = lugar,
                    idTienda = id_tienda,
                    idPromo = id_promo,
                    idUser = uid,
                    evento = repo_pantallas_promocionar.EventoNotificacion.CLICK
                )
                }
            } catch (e: Exception) {
                Log.d("error_obtner", "error_al_psa_rpantala")
            }
        }

    }

    fun clearPromo() {
        _promo.value = null
    }
}
