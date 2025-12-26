package com.geinzz.geinzwork.viewModels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.geinzz.geinzwork.data.model.localizate_geinz.inicio_geinz.UiAction
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

class UiActionViewModel : ViewModel() {

    private val _actions = MutableSharedFlow<UiAction>(
        replay = 1,
        extraBufferCapacity = 1
    )
    val actions = _actions.asSharedFlow()

    fun emitir(action: UiAction) {
        Log.d("UiAction", "📤 Emitiendo acción: $action")
        _actions.tryEmit(action)
    }
}
