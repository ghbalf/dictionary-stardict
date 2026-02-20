package com.example.stardict.domain.usecase

import com.example.stardict.domain.repository.FavoriteRepository
import javax.inject.Inject

class ToggleFavoriteUseCase @Inject constructor(
    private val favoriteRepository: FavoriteRepository
) {
    suspend operator fun invoke(word: String, dictionaryId: Long) {
        favoriteRepository.toggleFavorite(word, dictionaryId)
    }
}
