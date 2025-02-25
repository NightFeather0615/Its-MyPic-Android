package dev.nightfeather.its_mypic

import androidx.compose.foundation.lazy.LazyListState
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SearchViewModel(
    imageData: List<ImageData>,
    resultScrollState: LazyListState,
    coroutineScope: CoroutineScope
): ViewModel() {
    private val _queryText = MutableStateFlow("")
    val queryText = MutableStateFlow("")

    private val _coroutineScope = coroutineScope
    private val _resultScrollState = resultScrollState

    private val _imageData = MutableStateFlow(imageData)
    val searchResult = _queryText
        .combine(_imageData) { query, imageData ->
            if (query.isBlank()) {
                imageData
            } else {
                imageData
                    .mapNotNull {
                        val result = it.calcDistanceWithQuery(query)
                        if (result.first == 0) return@mapNotNull null
                        Pair(it, result)
                    }
                    .sortedWith(
                        compareByDescending<Pair<ImageData, Pair<Int, Int>>> {
                            it.second.first
                        }.thenBy {
                            it.second.second
                        }
                    )
                    .map {
                        it.first
                    }
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
        _coroutineScope.launch {
            _resultScrollState.animateScrollToItem(0)
        }
    }
}
