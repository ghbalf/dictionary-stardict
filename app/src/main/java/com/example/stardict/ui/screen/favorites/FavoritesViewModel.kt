package com.example.stardict.ui.screen.favorites

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.stardict.domain.model.FavoriteEntry
import com.example.stardict.domain.repository.DictionaryRepository
import com.example.stardict.domain.repository.FavoriteRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FavoritesViewModel @Inject constructor(
    private val favoriteRepository: FavoriteRepository,
    private val dictionaryRepository: DictionaryRepository
) : ViewModel() {

    val favorites: StateFlow<List<FavoriteEntry>> = favoriteRepository.getFavorites()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun removeFavorite(entry: FavoriteEntry) {
        viewModelScope.launch {
            favoriteRepository.removeFavorite(entry.word, entry.dictionaryId)
        }
    }
}
