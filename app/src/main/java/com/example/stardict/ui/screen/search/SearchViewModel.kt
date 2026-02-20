package com.example.stardict.ui.screen.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.stardict.domain.model.Dictionary
import com.example.stardict.domain.model.SearchResult
import com.example.stardict.domain.repository.DictionaryRepository
import com.example.stardict.domain.usecase.FuzzySearchUseCase
import com.example.stardict.domain.usecase.SearchWordUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val searchWordUseCase: SearchWordUseCase,
    private val fuzzySearchUseCase: FuzzySearchUseCase,
    dictionaryRepository: DictionaryRepository
) : ViewModel() {

    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query.asStateFlow()

    private val _results = MutableStateFlow<List<SearchResult>>(emptyList())
    val results: StateFlow<List<SearchResult>> = _results.asStateFlow()

    private val _isSearching = MutableStateFlow(false)
    val isSearching: StateFlow<Boolean> = _isSearching.asStateFlow()

    private val _selectedDictId = MutableStateFlow<Long?>(null)
    val selectedDictId: StateFlow<Long?> = _selectedDictId.asStateFlow()

    val dictionaries: StateFlow<List<Dictionary>> = dictionaryRepository.getEnabledDictionaries()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        @OptIn(FlowPreview::class)
        viewModelScope.launch {
            _query
                .debounce(300)
                .distinctUntilChanged()
                .collect { query ->
                    performSearch(query)
                }
        }
    }

    fun onQueryChange(newQuery: String) {
        _query.value = newQuery
    }

    fun onDictFilterChange(dictId: Long?) {
        _selectedDictId.value = dictId
        viewModelScope.launch {
            performSearch(_query.value)
        }
    }

    private suspend fun performSearch(query: String) {
        if (query.isBlank()) {
            _results.value = emptyList()
            return
        }

        _isSearching.value = true
        try {
            val dictIds = _selectedDictId.value?.let { listOf(it) }
            var results = searchWordUseCase(query, dictIds)
            if (results.isEmpty()) {
                results = fuzzySearchUseCase(query, dictIds)
            }
            _results.value = results
        } finally {
            _isSearching.value = false
        }
    }
}
