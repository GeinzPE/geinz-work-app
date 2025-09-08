package com.geinzz.geinzwork.viewModels

import Item
import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.geinzz.geinzwork.R
import com.geinzz.geinzwork.aloglia.AlgoliaHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class SearchViewModel(application: Application) : AndroidViewModel(application) {

    private val algoliaHelper = AlgoliaHelper(
        appId =application.getString(R.string.APPID_ALGOLIA)  ,
        apiKey = application.getString(R.string.APIKEY_ALGOLIA_SEARCH) ,
        indexName = application.getString(R.string.IDEX_NAME_ALGOLIA)
    )

    private val _results = MutableStateFlow<List<Item>>(emptyList())
    val results: StateFlow<List<Item>> = _results

    fun search(query: String) {
        viewModelScope.launch {
            try {
                val hits = algoliaHelper.search(query)
                _results.value = hits
            } catch (e: Exception) {
                _results.value = emptyList()
            }
        }
    }
    fun clearResults() {
        _results.value = emptyList()
    }
}
