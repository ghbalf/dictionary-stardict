package com.example.stardict.ui.screen.definition

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.stardict.domain.model.Definition
import com.example.stardict.domain.usecase.GetDefinitionUseCase
import com.example.stardict.domain.usecase.ToggleFavoriteUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.net.URLDecoder
import javax.inject.Inject

@HiltViewModel
class DefinitionViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getDefinitionUseCase: GetDefinitionUseCase,
    private val toggleFavoriteUseCase: ToggleFavoriteUseCase
) : ViewModel() {

    private val word: String = URLDecoder.decode(
        savedStateHandle.get<String>("word") ?: "",
        "UTF-8"
    )
    private val dictionaryId: Long = savedStateHandle.get<Long>("dictionaryId") ?: 0
    private val offset: Long = savedStateHandle.get<Long>("offset") ?: 0
    private val size: Int = savedStateHandle.get<Int>("size") ?: 0

    private val _definition = MutableStateFlow<Definition?>(null)
    val definition: StateFlow<Definition?> = _definition.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    init {
        loadDefinition()
    }

    private fun loadDefinition() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                _definition.value = getDefinitionUseCase(word, dictionaryId, offset, size)
                if (_definition.value == null) {
                    _error.value = "Definition not found"
                }
            } catch (e: Exception) {
                _error.value = e.message ?: "Failed to load definition"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun toggleFavorite() {
        viewModelScope.launch {
            toggleFavoriteUseCase(word, dictionaryId)
            val current = _definition.value
            if (current != null) {
                _definition.value = current.copy(isFavorite = !current.isFavorite)
            }
        }
    }
}
