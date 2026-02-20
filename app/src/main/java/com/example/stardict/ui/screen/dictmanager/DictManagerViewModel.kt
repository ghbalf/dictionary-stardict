package com.example.stardict.ui.screen.dictmanager

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.stardict.domain.model.Dictionary
import com.example.stardict.domain.model.DictionarySource
import com.example.stardict.domain.repository.DictionaryRepository
import com.example.stardict.domain.repository.SourceRepository
import com.example.stardict.domain.usecase.DownloadDictionaryUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DictManagerViewModel @Inject constructor(
    private val dictionaryRepository: DictionaryRepository,
    private val sourceRepository: SourceRepository,
    private val downloadDictionaryUseCase: DownloadDictionaryUseCase
) : ViewModel() {

    val dictionaries: StateFlow<List<Dictionary>> = dictionaryRepository.getAllDictionaries()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val sources: StateFlow<List<DictionarySource>> = sourceRepository.getAllSources()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _showAddDialog = MutableStateFlow(false)
    val showAddDialog: StateFlow<Boolean> = _showAddDialog.asStateFlow()

    fun showAddDialog() { _showAddDialog.value = true }
    fun hideAddDialog() { _showAddDialog.value = false }

    fun addDictionary(name: String, url: String) {
        viewModelScope.launch {
            downloadDictionaryUseCase(name, url)
            _showAddDialog.value = false
        }
    }

    fun toggleDictionary(dictionary: Dictionary) {
        viewModelScope.launch {
            dictionaryRepository.updateDictionary(dictionary.copy(enabled = !dictionary.enabled))
        }
    }

    fun deleteDictionary(dictionary: Dictionary) {
        viewModelScope.launch {
            dictionaryRepository.deleteDictionary(dictionary.id)
        }
    }

    fun deleteSource(source: DictionarySource) {
        viewModelScope.launch {
            sourceRepository.deleteSource(source.id)
        }
    }

    fun retrySource(source: DictionarySource) {
        viewModelScope.launch {
            sourceRepository.deleteSource(source.id)
            downloadDictionaryUseCase(source.name, source.url)
        }
    }

    fun editAndRetrySource(source: DictionarySource, name: String, url: String) {
        viewModelScope.launch {
            sourceRepository.deleteSource(source.id)
            downloadDictionaryUseCase(name, url)
        }
    }
}
