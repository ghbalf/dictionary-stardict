package com.example.stardict.ui.screen.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.stardict.domain.model.HistoryEntry
import com.example.stardict.domain.model.SearchResult
import com.example.stardict.domain.repository.DictionaryRepository
import com.example.stardict.domain.repository.HistoryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val historyRepository: HistoryRepository,
    private val dictionaryRepository: DictionaryRepository
) : ViewModel() {

    val history: StateFlow<List<HistoryEntry>> = historyRepository.getHistory()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _lookupResult = MutableStateFlow<List<SearchResult>>(emptyList())
    val lookupResult: StateFlow<List<SearchResult>> = _lookupResult.asStateFlow()

    fun clearHistory() {
        viewModelScope.launch {
            historyRepository.clearHistory()
        }
    }

    fun lookupWord(word: String, dictionaryId: Long) {
        viewModelScope.launch {
            val results = dictionaryRepository.searchWord(word, listOf(dictionaryId))
            _lookupResult.value = results
        }
    }
}
