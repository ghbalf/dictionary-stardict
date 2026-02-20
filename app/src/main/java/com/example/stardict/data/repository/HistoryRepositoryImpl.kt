package com.example.stardict.data.repository

import com.example.stardict.data.local.db.dao.HistoryDao
import com.example.stardict.data.local.db.entity.HistoryEntryEntity
import com.example.stardict.domain.model.HistoryEntry
import com.example.stardict.domain.repository.HistoryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HistoryRepositoryImpl @Inject constructor(
    private val historyDao: HistoryDao
) : HistoryRepository {

    override fun getHistory(): Flow<List<HistoryEntry>> =
        historyDao.getAll().map { list ->
            list.map {
                HistoryEntry(
                    id = it.id,
                    word = it.word,
                    dictionaryId = it.dictionaryId,
                    timestamp = it.timestamp
                )
            }
        }

    override suspend fun addToHistory(word: String, dictionaryId: Long) {
        // Remove existing entry to avoid duplicates, then re-insert at top
        historyDao.deleteByWordAndDict(word, dictionaryId)
        historyDao.insert(HistoryEntryEntity(word = word, dictionaryId = dictionaryId))
    }

    override suspend fun clearHistory() {
        historyDao.clearAll()
    }
}
