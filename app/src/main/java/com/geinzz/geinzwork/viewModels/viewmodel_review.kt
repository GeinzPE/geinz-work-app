package com.geinzz.geinzwork.viewModels

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.geinzz.geinzwork.data.model.dataclass_review.data_class_resultado_tienda_lugar
import com.geinzz.geinzwork.data.model.dataclass_review.data_class_review
import com.geinzz.geinzwork.data.model.dataclass_review.datos_review
import com.geinzz.geinzwork.data.model.dataclass_review.datos_review_existenet
import com.geinzz.geinzwork.model.repo_review
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch

class viewmodel_review : ViewModel() {
    val instacia_repo = repo_review()


    private val datos_TL_review = MutableLiveData<data_class_resultado_tienda_lugar>()
    val _datos_TL_review: LiveData<data_class_resultado_tienda_lugar> get() = datos_TL_review

    private val review_send = MutableLiveData<Boolean>()
    val _review_send: LiveData<Boolean> get() = review_send

    private val Verificar_exist = MutableLiveData<datos_review_existenet>()
    val _verificar_review_exsit: MutableLiveData<datos_review_existenet> get() = Verificar_exist

    fun set_datos_TL_review(data_class_review: data_class_review) {
        viewModelScope.launch {
            try {
                datos_TL_review.value = instacia_repo.obtener_datos_tienda(data_class_review)
            } catch (e: Exception) {
                datos_TL_review.value = data_class_resultado_tienda_lugar()
            }
        }
    }

    fun agregar_review(datos_review: datos_review) {
        viewModelScope.launch {
            try {
                review_send.value = instacia_repo.agregar_review(datos_review)
            } catch (e: Exception) {
                review_send.value = false
            }
        }
    }

    fun resetar_valor_review() {
        review_send.value = false
    }


    fun verificar_review_existente(id_user: String, data_class_review: data_class_review) {
        viewModelScope.launch {
            try {
                Verificar_exist.value =
                    instacia_repo.verificar_review_exsitente(id_user, data_class_review)
            } catch (e: Exception) {
                Verificar_exist.value = null
            }
        }
    }

}