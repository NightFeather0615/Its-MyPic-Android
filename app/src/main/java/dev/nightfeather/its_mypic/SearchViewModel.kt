package dev.nightfeather.its_mypic

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

class SearchViewModel(
    imageData: List<ImageData>
): ViewModel() {
    private val _queryText = MutableStateFlow("")
    val queryText = MutableStateFlow("")

    private val _imageData = MutableStateFlow(imageData)
    val searchResult = _queryText
        .combine(_imageData) { query, imageData ->
            if (query.isBlank()) {
                imageData
            } else {
                imageData.filter { it.isMatchWithQuery(query) }
            }
        }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            _imageData.value
        )

    fun onQueryTextChanged(query: String) {
        queryText.value = query
        _queryText.value = Utils.StringSearch.formatText(query)
    }
}
