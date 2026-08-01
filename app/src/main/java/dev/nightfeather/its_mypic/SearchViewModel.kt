package dev.nightfeather.its_mypic

import androidx.compose.foundation.lazy.LazyListState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SearchViewModel(
    imageData: List<ImageData>,
    resultScrollState: LazyListState,
    private val coroutineScope: CoroutineScope
) {
    private val _queryText = MutableStateFlow("")
    val queryText = MutableStateFlow("")

    private val resultScrollState = resultScrollState

    private val imageDataFlow = MutableStateFlow(imageData)
    val searchResult = _queryText
        .combine(imageDataFlow) { query, imageData ->
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
            coroutineScope,
            SharingStarted.WhileSubscribed(5000),
            imageDataFlow.value
        )

    fun onQueryTextChanged(query: String) {
        queryText.value = query
        _queryText.value = Utils.StringSearch.formatText(query)
        coroutineScope.launch {
            resultScrollState.animateScrollToItem(0)
        }
    }
}
