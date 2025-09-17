package com.geinzz.geinzwork.viewModels

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.geinzz.geinzwork.data.model.dataclass_review.data_class_resultado_tienda_lugar
import com.geinzz.geinzwork.data.model.dataclass_review.data_class_review
import com.geinzz.geinzwork.model.repo_review
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch

class viewmodel_review : ViewModel() {
    val instacia_repo = repo_review()
    private lateinit var firebaseAuth: FirebaseFirestore

    private val datos_TL_review = MutableLiveData<data_class_resultado_tienda_lugar?>()
    val _datos_TL_review: LiveData<data_class_resultado_tienda_lugar?> get() = datos_TL_review

    fun set_datos_TL_review(data_class_review: data_class_review) {
        viewModelScope.launch {
            try {
                datos_TL_review.value = instacia_repo.obtener_datos_tienda(data_class_review)
                Log.d("datos_TL_review",datos_TL_review.value.toString())
            } catch (e: Exception) {
                datos_TL_review.value = null
            }
        }

    }

}