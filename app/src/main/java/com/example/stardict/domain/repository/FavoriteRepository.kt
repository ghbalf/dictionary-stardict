package com.example.stardict.domain.repository

import com.example.stardict.domain.model.FavoriteEntry
import kotlinx.coroutines.flow.Flow

interface FavoriteRepository {
    fun getFavorites(): Flow<List<FavoriteEntry>>
    suspend fun isFavorite(word: String, dictionaryId: Long): Boolean
    suspend fun toggleFavorite(word: String, dictionaryId: Long)
    suspend fun removeFavorite(word: String, dictionaryId: Long)
}
