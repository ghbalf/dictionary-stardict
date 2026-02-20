package com.example.stardict.domain.repository

import com.example.stardict.domain.model.HistoryEntry
import kotlinx.coroutines.flow.Flow

interface HistoryRepository {
    fun getHistory(): Flow<List<HistoryEntry>>
    suspend fun addToHistory(word: String, dictionaryId: Long)
    suspend fun clearHistory()
}
