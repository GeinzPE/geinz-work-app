package com.geinzz.geinzwork.viewModels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.geinzz.geinzwork.algolia.AlgoliaHelper
import com.geinzz.geinzwork.algolia.Item
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class SearchViewModel : ViewModel() {

    private val algoliaHelper = AlgoliaHelper(
        appId = "TU_APP_ID",
        apiKey = "TU_API_KEY",
        indexName = "TU_INDEX_NAME"
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
}
